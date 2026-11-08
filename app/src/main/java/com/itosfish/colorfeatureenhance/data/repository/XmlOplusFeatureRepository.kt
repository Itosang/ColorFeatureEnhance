package com.itosfish.colorfeatureenhance.data.repository

import android.util.Xml
import com.itosfish.colorfeatureenhance.FeatureMode
import com.itosfish.colorfeatureenhance.data.model.AppFeature
import com.itosfish.colorfeatureenhance.data.model.OplusFeatureMappings
import com.itosfish.colorfeatureenhance.domain.FeatureRepository
import com.itosfish.colorfeatureenhance.utils.ModuleConfig
import com.itosfish.colorfeatureenhance.utils.OriginalNames
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.xmlpull.v1.XmlPullParser
import java.io.StringReader

/**
 * 直接读写模块目录中的 my_product / my_region oplus-feature.xml。
 */
class XmlOplusFeatureRepository(
    private val mode: FeatureMode = FeatureMode.OPLUS
) : FeatureRepository {

    override suspend fun loadFeatures(configPath: String): List<AppFeature> = withContext(Dispatchers.IO) {
        val xml = ModuleConfig.readText(mode)
        val features = if (xml.isBlank()) {
            mutableListOf()
        } else {
            parseXmlFeatures(xml).toMutableList()
        }

        OriginalNames.getOrInit(mode, features.map { it.name }.toSet())

        val existingNames = features.mapTo(mutableSetOf()) { it.name }
        OplusFeatureMappings.getInstance().getBuiltInNames()
            .filterNot { it in existingNames }
            .sorted()
            .forEach { name -> features.add(AppFeature(name, enabled = false)) }

        features
    }

    override suspend fun saveFeatures(configPath: String, features: List<AppFeature>): Unit =
        withContext(Dispatchers.IO) {
            val enabledFeatures = features.filter { it.enabled }
            ModuleConfig.writeText(mode, buildXml(enabledFeatures))
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
                            features.add(AppFeature(name, enabled = true, args = "unavailable"))
                        }
                    }
                }
            }
            eventType = parser.next()
        }

        return features
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
