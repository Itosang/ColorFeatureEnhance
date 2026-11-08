package com.itosfish.colorfeatureenhance.data.repository

import android.util.Xml
import com.itosfish.colorfeatureenhance.FeatureMode
import com.itosfish.colorfeatureenhance.data.model.AppFeature
import com.itosfish.colorfeatureenhance.data.model.OplusFeatureMappings
import com.itosfish.colorfeatureenhance.domain.FeatureRepository
import com.itosfish.colorfeatureenhance.utils.ConfigFiles
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.xmlpull.v1.XmlPullParser
import java.io.File

/**
 * 直接读写 my_product / my_region 的 oplus-feature.xml，不再走补丁和合并。
 */
class XmlOplusFeatureRepository(
    private val mode: FeatureMode = FeatureMode.OPLUS
) : FeatureRepository {

    private val target = ConfigFiles.targetForMode(mode)

    override suspend fun loadFeatures(configPath: String): List<AppFeature> = withContext(Dispatchers.IO) {
        val file = File(configPath)
        if (!file.exists()) return@withContext emptyList()

        val features = parseXmlFeatures(file).toMutableList()

        // 首次备份存在时，把配置里没有的内置特性补到列表里，默认关闭
        if (ConfigFiles.originalFile(target).exists()) {
            val existingNames = features.mapTo(mutableSetOf()) { it.name }
            OplusFeatureMappings.getInstance().getBuiltInNames()
                .filterNot { it in existingNames }
                .sorted()
                .forEach { name -> features.add(AppFeature(name, enabled = false)) }
        }

        features
    }

    override suspend fun saveFeatures(configPath: String, features: List<AppFeature>): Unit =
        withContext(Dispatchers.IO) {
            val enabledFeatures = features.filter { it.enabled }
            ConfigFiles.saveWorking(target, buildXml(enabledFeatures))
        }

    private fun parseXmlFeatures(file: File): List<AppFeature> {
        file.inputStream().use { inputStream ->
            val parser = Xml.newPullParser()
            parser.setInput(inputStream, "UTF-8")

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
                                features.add(
                                    AppFeature(name, enabled = true, args = "unavailable")
                                )
                            }
                        }
                    }
                }
                eventType = parser.next()
            }

            return features
        }
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
