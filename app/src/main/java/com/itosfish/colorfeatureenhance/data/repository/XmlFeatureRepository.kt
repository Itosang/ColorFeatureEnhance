package com.itosfish.colorfeatureenhance.data.repository

import android.util.Xml
import com.itosfish.colorfeatureenhance.FeatureMode
import com.itosfish.colorfeatureenhance.data.model.AppFeature
import com.itosfish.colorfeatureenhance.data.model.AppendPresets
import com.itosfish.colorfeatureenhance.data.model.FeatureSubNode
import com.itosfish.colorfeatureenhance.domain.FeatureRepository
import com.itosfish.colorfeatureenhance.utils.FeatureCatalog
import com.itosfish.colorfeatureenhance.utils.ModuleConfig
import com.itosfish.colorfeatureenhance.utils.OriginalNames
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.xmlpull.v1.XmlPullParser
import java.io.StringReader

class XmlFeatureRepository : FeatureRepository {

    override suspend fun loadFeatures(configPath: String): List<AppFeature> = withContext(Dispatchers.IO) {
        // 列表来源 = 用户设备上的当前配置（若模块已挂载则为模块配置）
        val deviceXml = ModuleConfig.readEffective(FeatureMode.APP)
        val deviceFeatures = if (deviceXml.isBlank()) emptyList() else parseXmlFeatures(deviceXml)
        if (ModuleConfig.isInstalled()) {
            OriginalNames.getOrInit(FeatureMode.APP, deviceFeatures.mapTo(mutableSetOf()) { it.name })
        }
        val presets = AppendPresets.forMode(FeatureMode.APP).flatMap { it.features }
        FeatureCatalog.remember(FeatureMode.APP, deviceFeatures + presets)

        // 生效 XML 中存在的特性即代表在当前系统中处于启用状态
        val activeNames = deviceFeatures.mapTo(mutableSetOf()) { it.name }
        FeatureCatalog.all(FeatureMode.APP).map { it.copy(enabled = it.name in activeNames) }
    }

    override suspend fun saveFeatures(configPath: String, features: List<AppFeature>): Unit =
        withContext(Dispatchers.IO) {
            // 先持久化记住所有已知条目，确保关闭项留在列表中，绝不丢失
            FeatureCatalog.remember(FeatureMode.APP, features)
            // 只有启用的特性写入生效 XML，关闭的项从生效配置中移除
            ModuleConfig.writeText(FeatureMode.APP, buildXml(features.filter { it.enabled }))
        }

    private fun parseXmlFeatures(xml: String): List<AppFeature> {
        val parser = Xml.newPullParser()
        parser.setInput(StringReader(xml))

        val features = mutableListOf<AppFeature>()
        var eventType = parser.eventType
        var currentFeature: AppFeature? = null
        val currentSubNodes = mutableListOf<FeatureSubNode>()

        while (eventType != XmlPullParser.END_DOCUMENT) {
            when (eventType) {
                XmlPullParser.START_TAG -> {
                    if (parser.name == "app_feature") {
                        val name = parser.getAttributeValue(null, "name") ?: ""
                        val args = parser.getAttributeValue(null, "args")

                        if (name.isNotEmpty()) {
                            // 存在于 XML 文件中的特性节点即视为当前已启用
                            currentFeature = AppFeature(name = name, enabled = true, args = args)
                            currentSubNodes.clear()
                        }
                    } else if (currentFeature != null) {
                        val args = parser.getAttributeValue(null, "args")
                        if (args != null) {
                            currentSubNodes.add(
                                FeatureSubNode(
                                    parser.name,
                                    parser.getAttributeValue(null, "name"),
                                    args
                                )
                            )
                        }
                    }
                }

                XmlPullParser.END_TAG -> {
                    if (parser.name == "app_feature" && currentFeature != null) {
                        features.add(currentFeature.copy(subNodes = currentSubNodes.toList()))
                        currentFeature = null
                    }
                }
            }
            eventType = parser.next()
        }

        return features
            .groupBy { it.name }
            .map { (_, list) ->
                val first = list.first()
                AppFeature(
                    name = first.name,
                    enabled = true,
                    args = first.args,
                    subNodes = list.flatMap { it.subNodes }.distinctBy { it.args }
                )
            }
    }

    private fun buildXml(features: List<AppFeature>): String = buildString {
        appendLine("<?xml version=\"1.0\" encoding=\"UTF-8\"?>")
        appendLine("<extend_features>")
        features.forEach { feature ->
            val args = feature.toArgs()
            if (feature.isSimple) {
                appendLine("\t<app_feature name=\"${escapeXml(feature.name)}\"$args/>")
            } else {
                appendLine("\t<app_feature name=\"${escapeXml(feature.name)}\"$args>")
                feature.subNodes.forEach { subNode ->
                    val name = subNode.name?.let { " name=\"${escapeXml(it)}\"" } ?: ""
                    appendLine("\t\t<${subNode.type}$name args=\"${escapeXml(subNode.args)}\"/>")
                }
                appendLine("\t</app_feature>")
            }
        }
        appendLine("</extend_features>")
    }

    private fun AppFeature.toArgs(): String {
        if (args.isNullOrBlank()) return ""
        return " args=\"${escapeXml(args)}\""
    }

    private fun escapeXml(value: String): String {
        return value
            .replace("&", "&amp;")
            .replace("\"", "&quot;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
    }
}
