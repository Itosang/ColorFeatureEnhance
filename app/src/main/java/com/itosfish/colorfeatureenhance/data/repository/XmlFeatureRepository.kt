package com.itosfish.colorfeatureenhance.data.repository

import android.util.Xml
import com.itosfish.colorfeatureenhance.data.model.AppFeature
import com.itosfish.colorfeatureenhance.data.model.FeatureSubNode
import com.itosfish.colorfeatureenhance.domain.FeatureRepository
import com.itosfish.colorfeatureenhance.utils.ConfigFiles
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.xmlpull.v1.XmlPullParser
import java.io.File

class XmlFeatureRepository : FeatureRepository {

    override suspend fun loadFeatures(configPath: String): List<AppFeature> = withContext(Dispatchers.IO) {
        val file = File(configPath)
        if (!file.exists()) return@withContext emptyList()
        parseXmlFeatures(file)
    }

    override suspend fun saveFeatures(configPath: String, features: List<AppFeature>): Unit =
        withContext(Dispatchers.IO) {
            ConfigFiles.saveWorking(ConfigFiles.Target.APP, buildXml(features))
        }

    private fun parseXmlFeatures(file: File): List<AppFeature> {
        file.inputStream().use { inputStream ->
            val parser = Xml.newPullParser()
            parser.setInput(inputStream, "UTF-8")

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
                            val enabled = args?.startsWith("boolean:") != true ||
                                args.substringAfter("boolean:").equals("true", true)

                            if (name.isNotEmpty()) {
                                currentFeature = AppFeature(name, enabled, args)
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
                            features.add(currentFeature!!.copy(subNodes = currentSubNodes.toList()))
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
                        enabled = list.any { it.enabled },
                        args = first.args,
                        subNodes = list.flatMap { it.subNodes }.distinctBy { it.args }
                    )
                }
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
        if (args?.startsWith("boolean:") == true) {
            return " args=\"boolean:$enabled\""
        }
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
