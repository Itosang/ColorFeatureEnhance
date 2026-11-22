package com.itosfish.colorfeatureenhance.data.repository

import android.util.Xml
import com.itosfish.colorfeatureenhance.FeatureMode
import com.itosfish.colorfeatureenhance.data.model.AppFeature
import com.itosfish.colorfeatureenhance.data.model.AppendPresets
import com.itosfish.colorfeatureenhance.domain.FeatureRepository
import com.itosfish.colorfeatureenhance.utils.FeatureCatalog
import com.itosfish.colorfeatureenhance.utils.ModuleConfig
import com.itosfish.colorfeatureenhance.utils.OriginalNames
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.xmlpull.v1.XmlPullParser
import java.io.StringReader

/**
 * 读写设备或模块中的 oplus-feature.xml（包括 my_product 和 my_region）
 */
class XmlOplusFeatureRepository(
    private val mode: FeatureMode = FeatureMode.OPLUS
) : FeatureRepository {

    override suspend fun loadFeatures(configPath: String): List<AppFeature> = withContext(Dispatchers.IO) {
        val deviceXml = ModuleConfig.readEffective(mode)
        val deviceFeatures = if (deviceXml.isBlank()) emptyList() else parseXmlFeatures(deviceXml)
        if (ModuleConfig.isInstalled()) {
            OriginalNames.getOrInit(mode, deviceFeatures.mapTo(mutableSetOf()) { it.name })
        }
        val presets = AppendPresets.forMode(mode).flatMap { it.features }
        FeatureCatalog.remember(mode, deviceFeatures + presets)
        // 只有在当前生效 XML 中且判定为启用的特性才为 true
        val enabledNames = deviceFeatures.filter { it.enabled }.mapTo(mutableSetOf()) { it.name }
        FeatureCatalog.all(mode).map { it.copy(enabled = it.name in enabledNames) }
    }

    override suspend fun saveFeatures(configPath: String, features: List<AppFeature>): Unit =
        withContext(Dispatchers.IO) {
            // 先持久化记住所有已知条目，确保关闭项留在列表中，绝不丢失
            FeatureCatalog.remember(mode, features)
            // 只有启用的特性写入生效 XML，关闭的项从生效配置中移除
            ModuleConfig.writeText(mode, buildXml(features.filter { it.enabled }))
        }

    private fun parseXmlFeatures(xml: String): List<AppFeature> {
        val parser = Xml.newPullParser()
        parser.setInput(StringReader(xml))

        val features = mutableListOf<AppFeature>()
        var eventType = parser.eventType

        while (eventType != XmlPullParser.END_DOCUMENT) {
            if (eventType == XmlPullParser.START_TAG) {
                when (parser.name) {
                    "oplus-feature" -> {
                        val name = parser.getAttributeValue(null, "name") ?: ""
                        if (name.isNotEmpty()) features.add(AppFeature(name, enabled = true))
                    }

                    "unavailable-oplus-feature" -> {
                        val name = parser.getAttributeValue(null, "name") ?: ""
                        if (name.isNotEmpty()) {
                            // unavailable-oplus-feature 属于已禁用状态，enabled 必须为 false
                            features.add(AppFeature(name, enabled = false, args = "unavailable"))
                        }
                    }
                }
            }
            eventType = parser.next()
        }

        return features.distinctBy { it.name }
    }

    private fun buildXml(features: List<AppFeature>): String = buildString {
        appendLine("<?xml version=\"1.0\" encoding=\"UTF-8\"?>")
        appendLine("<oplus-config>")
        features.forEach { feature ->
            if (feature.args == "unavailable") {
                appendLine("\t<unavailable-oplus-feature name=\"${escapeXml(feature.name)}\"/>")
            } else {
                appendLine("\t<oplus-feature name=\"${escapeXml(feature.name)}\"/>")
            }
        }
        appendLine("</oplus-config>")
    }

    private fun escapeXml(value: String): String {
        return value
            .replace("&", "&amp;")
            .replace("\"", "&quot;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
    }
}
