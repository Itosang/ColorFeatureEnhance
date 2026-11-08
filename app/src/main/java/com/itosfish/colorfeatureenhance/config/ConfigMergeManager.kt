package com.itosfish.colorfeatureenhance.config

import android.util.Log
import com.itosfish.colorfeatureenhance.data.model.AppFeature
import com.itosfish.colorfeatureenhance.data.model.FeatureSubNode
import com.itosfish.colorfeatureenhance.utils.CLog
import com.itosfish.colorfeatureenhance.utils.CSU
import com.itosfish.colorfeatureenhance.utils.ConfigUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.io.File
import java.io.StringReader

/**
 * 新架构的配置合并管理器
 * 负责系统基线配置与用户补丁的智能合并
 */
object ConfigMergeManager {
    
    private const val TAG = "ConfigMergeManager"
    
    // 获取配置路径
    private val configPaths = ConfigUtils.getConfigPaths()
    
    // JSON序列化器
    private val json = Json { 
        prettyPrint = true
        ignoreUnknownKeys = true
    }
    
    /**
     * 执行配置合并（应用启动时调用）
     */
    suspend fun performConfigMerge(): Boolean = withContext(Dispatchers.IO) {
        try {
            CLog.i(TAG, "开始执行配置合并")

            // 复制缺失的系统基线文件（已存在的不会覆盖）
            CLog.i(TAG, "检查并补齐系统基线配置文件")
            val copySuccess = copySystemConfigsToBaseline()
            if (!copySuccess) {
                CLog.w(TAG, "系统配置复制失败，继续使用已有基线进行合并")
            }

            // 合并 app-features.xml
            mergeAppFeatures()

            // 合并 my_product 的 oplus-feature.xml
            mergeOplusFeatures()

            // 合并 my_region 的 oplus-feature.xml
            mergeRegionFeatures()

            CLog.i(TAG, "所有配置文件合并完成")
            return@withContext true
        } catch (e: Exception) {
            CLog.e(TAG, "配置合并失败", e)
            return@withContext false
        }
    }
    
    /**
     * 合并 app-features.xml
     */
    private fun mergeAppFeatures() {
        try {
            val systemFile = File(configPaths.systemBaselineDir, configPaths.appFeaturesFile)
            val patchFile = File(configPaths.userPatchesDir, "app-features.patch.json")
            val outputFile = File(configPaths.mergedOutputDir, configPaths.appFeaturesFile)

            CLog.d(TAG, "开始合并 app-features.xml")
            CLog.d(TAG, "系统文件: ${systemFile.absolutePath}, 存在: ${systemFile.exists()}")
            CLog.d(TAG, "补丁文件: ${patchFile.absolutePath}, 存在: ${patchFile.exists()}")
            CLog.d(TAG, "输出文件: ${outputFile.absolutePath}")

            // 读取系统基线配置
            val systemFeatures = if (systemFile.exists()) {
                parseAppFeaturesXml(systemFile)
            } else {
                emptyList()
            }

            // 读取用户补丁
            val userPatches = if (patchFile.exists()) {
                loadAppFeaturePatches(patchFile)
            } else {
                emptyList()
            }

            // 执行合并
            val mergedFeatures = applyAppFeaturePatches(systemFeatures, userPatches)

            // 写入合并结果
            writeAppFeaturesXml(outputFile, mergedFeatures)

            CLog.i(TAG, "app-features.xml 合并完成: ${systemFeatures.size} 系统特性 + ${userPatches.size} 用户补丁 = ${mergedFeatures.size} 最终特性")
        } catch (e: Exception) {
            CLog.e(TAG, "app-features.xml 合并失败", e)
            throw e
        }
    }
    
    /**
     * 合并 oplus-feature.xml
     */
    private fun mergeOplusFeatures() {
        mergeOplusFile(
            systemFile = File(configPaths.systemBaselineDir, configPaths.oplusFeaturesFile),
            patchFile = File(configPaths.userPatchesDir, configPaths.oplusPatchFile),
            outputFile = File(configPaths.mergedOutputDir, configPaths.oplusFeaturesFile)
        )
    }

    /**
     * 合并 my_region/etc/extension 下的 oplus-feature.xml
     */
    private fun mergeRegionFeatures() {
        mergeOplusFile(
            systemFile = File(configPaths.regionBaselineDir, configPaths.oplusFeaturesFile),
            patchFile = File(configPaths.userPatchesDir, configPaths.regionPatchFile),
            outputFile = File(configPaths.regionMergedOutputDir, configPaths.oplusFeaturesFile)
        )
    }

    private fun mergeOplusFile(systemFile: File, patchFile: File, outputFile: File) {
        if (!systemFile.exists() && !patchFile.exists()) {
            CLog.i(TAG, "跳过合并，基线与补丁均不存在: ${systemFile.absolutePath}")
            return
        }

        val systemFeatures = if (systemFile.exists()) {
            parseOplusFeaturesXml(systemFile)
        } else {
            emptyList()
        }

        val userPatches = if (patchFile.exists()) {
            loadOplusFeaturePatches(patchFile)
        } else {
            emptyList()
        }

        val mergedFeatures = applyOplusFeaturePatches(systemFeatures, userPatches)

        writeOplusFeaturesXml(outputFile, mergedFeatures)

        CLog.i(TAG, "${outputFile.name} 合并完成: ${systemFeatures.size} 系统特性 + ${userPatches.size} 用户补丁 = ${mergedFeatures.size} 最终特性")
    }
    
    /**
     * 保存用户对 app-features 的修改为补丁
     */
    suspend fun saveAppFeaturePatches(originalFeatures: List<AppFeature>, modifiedFeatures: List<AppFeature>) = withContext(Dispatchers.IO) {
        val patches = generateAppFeaturePatches(originalFeatures, modifiedFeatures)
        val patchFile = File(configPaths.userPatchesDir, "app-features.patch.json")
        
        patchFile.writeText(json.encodeToString(patches))

        CLog.i(TAG, "保存 app-features 补丁: ${patches.size} 个变更")
    }
    
    /**
     * 保存用户对 oplus-features 的修改为补丁
     */
    suspend fun saveOplusFeaturePatches(originalFeatures: List<OplusFeature>, modifiedFeatures: List<OplusFeature>) =
        saveOplusFeaturePatches(configPaths.oplusPatchFile, originalFeatures, modifiedFeatures)

    /**
     * 保存用户对 my_region oplus-features 的修改为补丁
     */
    suspend fun saveRegionFeaturePatches(originalFeatures: List<OplusFeature>, modifiedFeatures: List<OplusFeature>) =
        saveOplusFeaturePatches(configPaths.regionPatchFile, originalFeatures, modifiedFeatures)

    private suspend fun saveOplusFeaturePatches(
        patchFileName: String,
        originalFeatures: List<OplusFeature>,
        modifiedFeatures: List<OplusFeature>
    ) = withContext(Dispatchers.IO) {
        val patches = generateOplusFeaturePatches(originalFeatures, modifiedFeatures)
        val patchFile = File(configPaths.userPatchesDir, patchFileName)

        patchFile.writeText(json.encodeToString(patches))

        CLog.i(TAG, "保存 $patchFileName: ${patches.size} 个变更")
    }

    /**
     * 获取特性的补丁状态
     * @param featureName 特性名称
     * @param isAppFeature 是否为app-features模式
     * @return 补丁动作类型，如果没有补丁则返回null
     */
    suspend fun getFeaturePatchAction(featureName: String, isAppFeature: Boolean): PatchAction? = withContext(Dispatchers.IO) {
        return@withContext try {
            if (isAppFeature) {
                val patchFile = File(configPaths.userPatchesDir, "app-features.patch.json")
                if (patchFile.exists()) {
                    val patches = loadAppFeaturePatches(patchFile)
                    patches.find { it.name == featureName }?.action
                } else {
                    null
                }
            } else {
                val patchFile = File(configPaths.userPatchesDir, "oplus-features.patch.json")
                if (patchFile.exists()) {
                    val patches = loadOplusFeaturePatches(patchFile)
                    val patch = patches.find { it.name == featureName }
                    // 将OplusPatchAction转换为PatchAction
                    when (patch?.action) {
                        OplusPatchAction.ENABLE -> {
                            // 检查是否为新增特性：在系统基线中不存在
                            if (isFeatureInSystemBaseline(featureName, false)) {
                                PatchAction.MODIFY // 系统基线中存在，是修改
                            } else {
                                PatchAction.ADD // 系统基线中不存在，是新增
                            }
                        }
                        OplusPatchAction.DISABLE -> {
                            // DISABLE总是修改操作（将Standard改为Unavailable）
                            PatchAction.MODIFY
                        }
                        OplusPatchAction.REMOVE -> PatchAction.REMOVE // REMOVE状态显示为删除标识
                        null -> null
                    }
                } else {
                    null
                }
            }
        } catch (e: Exception) {
            CLog.e(TAG, "获取特性补丁状态失败: $featureName", e)
            null
        }
    }

    /**
     * 检查特性是否存在于系统基线配置中
     * @param featureName 特性名称
     * @param isAppFeature 是否为app-features模式
     * @param isRegion 是否为 my_region 的 oplus-feature.xml
     * @return true表示存在于系统基线中
     */
    suspend fun isFeatureInSystemBaseline(
        featureName: String,
        isAppFeature: Boolean,
        isRegion: Boolean = false
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            val systemFile = getBaselineFile(isAppFeature, isRegion)
            if (!systemFile.exists()) return@withContext false

            val featureNames = if (isAppFeature) {
                parseAppFeaturesXml(systemFile).map { it.name }
            } else {
                parseOplusFeaturesXml(systemFile).map { it.name }
            }
            featureNames.contains(featureName)
        } catch (e: Exception) {
            CLog.e(TAG, "检查系统基线特性失败: $featureName", e)
            false
        }
    }

    private fun getBaselineFile(isAppFeature: Boolean, isRegion: Boolean): File {
        return when {
            isAppFeature -> File(configPaths.systemBaselineDir, configPaths.appFeaturesFile)
            isRegion -> File(configPaths.regionBaselineDir, configPaths.oplusFeaturesFile)
            else -> File(configPaths.systemBaselineDir, configPaths.oplusFeaturesFile)
        }
    }

    /**
     * 批量获取多个特性的补丁状态
     * @param featureNames 特性名称列表
     * @param isAppFeature 是否为app-features模式
     * @param isRegion 是否为 my_region 的 oplus-feature.xml
     * @return 特性名称到补丁动作的映射
     */
    suspend fun getFeaturesPatchActions(
        featureNames: List<String>,
        isAppFeature: Boolean,
        isRegion: Boolean = false
    ): Map<String, PatchAction> = withContext(Dispatchers.IO) {
        return@withContext try {
            if (isAppFeature) {
                val patchFile = File(configPaths.userPatchesDir, "app-features.patch.json")
                if (patchFile.exists()) {
                    val patches = loadAppFeaturePatches(patchFile)
                    patches.mapNotNull { patch ->
                        if (patch.name in featureNames) {
                            // 对于app-features，直接使用补丁中的action
                            patch.name to patch.action
                        } else {
                            null
                        }
                    }.toMap()
                } else {
                    emptyMap()
                }
            } else {
                val patchFile = File(configPaths.userPatchesDir, if (isRegion) configPaths.regionPatchFile else configPaths.oplusPatchFile)
                if (patchFile.exists()) {
                    val patches = loadOplusFeaturePatches(patchFile)
                    patches.mapNotNull { patch ->
                        if (patch.name in featureNames) {
                            val action = when (patch.action) {
                                OplusPatchAction.ENABLE -> {
                                    // 检查是否为新增特性：在系统基线中不存在
                                    if (isFeatureInSystemBaseline(patch.name, false, isRegion)) {
                                        PatchAction.MODIFY // 系统基线中存在，是修改
                                    } else {
                                        PatchAction.ADD // 系统基线中不存在，是新增
                                    }
                                }
                                OplusPatchAction.DISABLE -> {
                                    // DISABLE总是修改操作（将Standard改为Unavailable）
                                    PatchAction.MODIFY
                                }
                                OplusPatchAction.REMOVE -> PatchAction.REMOVE // REMOVE状态返回删除标识
                            }
                            patch.name to action
                        } else {
                            null
                        }
                    }.toMap()
                } else {
                    emptyMap()
                }
            }
        } catch (e: Exception) {
            CLog.e(TAG, "批量获取特性补丁状态失败", e)
            emptyMap()
        }
    }

    /**
     * 从系统路径复制缺失的配置文件到 system_baseline
     * 已存在的基线不会被覆盖
     */
    private fun copySystemConfigsToBaseline(): Boolean {
        val pendingCopies = listOf(
            "/my_product/etc/extension/${configPaths.appFeaturesFile}" to
                File(configPaths.systemBaselineDir, configPaths.appFeaturesFile),
            "/my_product/etc/extension/${configPaths.oplusFeaturesFile}" to
                File(configPaths.systemBaselineDir, configPaths.oplusFeaturesFile),
            "/my_region/etc/extension/${configPaths.oplusFeaturesFile}" to
                File(configPaths.regionBaselineDir, configPaths.oplusFeaturesFile)
        ).filter { (_, target) -> !target.exists() }

        if (pendingCopies.isEmpty()) {
            CLog.i(TAG, "系统基线配置已存在，跳过复制")
            return true
        }

        val copiedCount = pendingCopies.count { (source, target) ->
            if (!CSU.fileExists(source)) {
                CLog.w(TAG, "系统配置文件不存在: $source")
                return@count false
            }

            target.parentFile?.mkdirs()
            CSU.runWithSu("cp \"$source\" \"${target.absolutePath}\"")
            val isCopied = target.exists()
            if (isCopied) {
                CLog.i(TAG, "成功复制系统配置: ${target.name}")
            } else {
                CLog.w(TAG, "复制失败: $source -> ${target.absolutePath}")
            }
            isCopied
        }

        CLog.i(TAG, "系统配置复制完成: $copiedCount/${pendingCopies.size} 个文件")
        return copiedCount > 0
    }

    // ========== 数据模型 ==========
    
    @Serializable
    data class AppFeaturePatch(
        val name: String,
        val action: PatchAction,
        val enabled: Boolean = true,
        val args: String? = null,
        val subNodes: List<FeatureSubNode> = emptyList()
    )
    
    @Serializable
    data class OplusFeaturePatch(
        val name: String,
        val action: OplusPatchAction
    )
    
    @Serializable
    enum class PatchAction {
        ADD, MODIFY, REMOVE
    }
    
    @Serializable
    enum class OplusPatchAction {
        ENABLE,    // 转为 <oplus-feature>
        DISABLE,   // 转为 <unavailable-oplus-feature>
        REMOVE     // 完全移除
    }
    
    @Serializable
    sealed class OplusFeature {
        abstract val name: String
        
        @Serializable
        data class Standard(override val name: String) : OplusFeature()
        
        @Serializable
        data class Unavailable(override val name: String) : OplusFeature()
    }
    
    // ========== XML解析方法 ==========
    
    /**
     * 解析 app-features.xml 文件
     */
    private fun parseAppFeaturesXml(file: File): List<AppFeature> {
        val features = mutableListOf<AppFeature>()
        
        try {
            val factory = XmlPullParserFactory.newInstance()
            val parser = factory.newPullParser()
            parser.setInput(StringReader(file.readText()))
            
            var eventType = parser.eventType
            var currentFeature: AppFeature? = null
            val currentSubNodes = mutableListOf<FeatureSubNode>()
            
            while (eventType != XmlPullParser.END_DOCUMENT) {
                when (eventType) {
                    XmlPullParser.START_TAG -> {
                        when (parser.name) {
                            "app_feature" -> {
                                val name = parser.getAttributeValue(null, "name") ?: ""
                                val args = parser.getAttributeValue(null, "args")
                                currentFeature = AppFeature(name = name, args = args)
                                currentSubNodes.clear()
                            }
                            "StringList" -> {
                                val subNodeName = parser.getAttributeValue(null, "name")
                                val subNodeArgs = parser.getAttributeValue(null, "args") ?: ""
                                currentSubNodes.add(FeatureSubNode("StringList", subNodeName, subNodeArgs))
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
        } catch (e: Exception) {
            Log.e(TAG, "解析 app-features.xml 失败", e)
        }
        
        return features
    }
    
    /**
     * 解析 oplus-feature.xml 文件
     */
    private fun parseOplusFeaturesXml(file: File): List<OplusFeature> {
        val features = mutableListOf<OplusFeature>()
        
        try {
            val factory = XmlPullParserFactory.newInstance()
            val parser = factory.newPullParser()
            parser.setInput(StringReader(file.readText()))
            
            var eventType = parser.eventType
            
            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.START_TAG) {
                    when (parser.name) {
                        "oplus-feature" -> {
                            val name = parser.getAttributeValue(null, "name") ?: ""
                            features.add(OplusFeature.Standard(name))
                        }
                        "unavailable-oplus-feature" -> {
                            val name = parser.getAttributeValue(null, "name") ?: ""
                            features.add(OplusFeature.Unavailable(name))
                        }
                    }
                }
                eventType = parser.next()
            }
        } catch (e: Exception) {
            CLog.e(TAG, "解析 oplus-feature.xml 失败", e)
        }
        
        return features
    }
    
    // ========== 私有辅助方法（待实现） ==========
    
    fun loadAppFeaturePatches(file: File): List<AppFeaturePatch> {
        return try {
            val content = file.readText()
            json.decodeFromString<List<AppFeaturePatch>>(content)
        } catch (e: Exception) {
            Log.e(TAG, "加载 app-features 补丁失败", e)
            emptyList()
        }
    }

    fun loadOplusFeaturePatches(file: File): List<OplusFeaturePatch> {
        return try {
            val content = file.readText()
            json.decodeFromString<List<OplusFeaturePatch>>(content)
        } catch (e: Exception) {
            Log.e(TAG, "加载 oplus-features 补丁失败", e)
            emptyList()
        }
    }
    
    private fun applyAppFeaturePatches(systemFeatures: List<AppFeature>, patches: List<AppFeaturePatch>): List<AppFeature> {
        val result = systemFeatures.toMutableList()

        patches.forEach { patch ->
            when (patch.action) {
                PatchAction.ADD -> {
                    // 添加新特性（如果不存在）
                    if (result.none { it.name == patch.name }) {
                        result.add(AppFeature(
                            name = patch.name,
                            enabled = patch.enabled,
                            args = patch.args,
                            subNodes = patch.subNodes
                        ))
                    }
                }
                PatchAction.MODIFY -> {
                    // 修改现有特性
                    val index = result.indexOfFirst { it.name == patch.name }
                    if (index >= 0) {
                        result[index] = AppFeature(
                            name = patch.name,
                            enabled = patch.enabled,
                            args = patch.args,
                            subNodes = patch.subNodes
                        )
                    }
                }
                PatchAction.REMOVE -> {
                    // 移除特性
                    result.removeAll { it.name == patch.name }
                }
            }
        }

        return result
    }

    fun applyOplusFeaturePatches(systemFeatures: List<OplusFeature>, patches: List<OplusFeaturePatch>): List<OplusFeature> {
        val result = systemFeatures.toMutableList()

        patches.forEach { patch ->
            when (patch.action) {
                OplusPatchAction.ENABLE -> {
                    // 移除同名的所有特性，添加启用特性
                    result.removeAll { it.name == patch.name }
                    result.add(OplusFeature.Standard(patch.name))
                }
                OplusPatchAction.DISABLE -> {
                    // 移除同名的所有特性，添加禁用特性
                    result.removeAll { it.name == patch.name }
                    result.add(OplusFeature.Unavailable(patch.name))
                }
                OplusPatchAction.REMOVE -> {
                    // 完全移除该特性
                    result.removeAll { it.name == patch.name }
                }
            }
        }

        return result
    }
    
    private fun writeAppFeaturesXml(file: File, features: List<AppFeature>) {
        try {
            CLog.d(TAG, "开始写入 app-features.xml: ${file.absolutePath}")
            CLog.d(TAG, "要写入的特性数量: ${features.size}")

            // 确保父目录存在
            file.parentFile?.let { parentDir ->
                if (!parentDir.exists()) {
                    val mkdirSuccess = parentDir.mkdirs()
                    CLog.d(TAG, "创建父目录: ${parentDir.absolutePath}, 结果: $mkdirSuccess")
                }
            }

            file.bufferedWriter().use { writer ->
                writer.appendLine("<?xml version=\"1.0\" encoding=\"UTF-8\"?>")
                writer.appendLine("<extend_features>")

                features.forEach { feature ->
                    if (feature.subNodes.isEmpty()) {
                        // 简单特性
                        if (feature.args.isNullOrEmpty()) {
                            writer.appendLine("\t<app_feature name=\"${escapeXml(feature.name)}\"/>")
                        } else {
                            writer.appendLine("\t<app_feature name=\"${escapeXml(feature.name)}\" args=\"${escapeXml(feature.args)}\"/>")
                        }
                    } else {
                        // 复杂特性
                        if (feature.args.isNullOrEmpty()) {
                            writer.appendLine("\t<app_feature name=\"${escapeXml(feature.name)}\">")
                        } else {
                            writer.appendLine("\t<app_feature name=\"${escapeXml(feature.name)}\" args=\"${escapeXml(feature.args)}\">")
                        }

                        feature.subNodes.forEach { subNode ->
                            if (subNode.name.isNullOrEmpty()) {
                                writer.appendLine("\t\t<${subNode.type} args=\"${escapeXml(subNode.args)}\"/>")
                            } else {
                                writer.appendLine("\t\t<${subNode.type} name=\"${escapeXml(subNode.name)}\" args=\"${escapeXml(subNode.args)}\"/>")
                            }
                        }

                        writer.appendLine("\t</app_feature>")
                    }
                }

                writer.appendLine("</extend_features>")
                writer.appendLine("<!-- Generated by ConfigMergeManager at ${System.currentTimeMillis()} -->")
            }

            // 验证文件是否写入成功
            if (file.exists()) {
                CLog.i(TAG, "app-features.xml 写入成功: ${file.absolutePath}, 大小: ${file.length()} 字节")
            } else {
                CLog.e(TAG, "app-features.xml 写入失败: 文件不存在")
            }

        } catch (e: Exception) {
            CLog.e(TAG, "写入 app-features.xml 失败: ${file.absolutePath}", e)
            throw e // 重新抛出异常，让调用者知道写入失败
        }
    }

    private fun writeOplusFeaturesXml(file: File, features: List<OplusFeature>) {
        try {
            CLog.d(TAG, "开始写入 oplus-feature.xml: ${file.absolutePath}")
            CLog.d(TAG, "要写入的特性数量: ${features.size}")

            // 确保父目录存在
            file.parentFile?.let { parentDir ->
                if (!parentDir.exists()) {
                    val mkdirSuccess = parentDir.mkdirs()
                    CLog.d(TAG, "创建父目录: ${parentDir.absolutePath}, 结果: $mkdirSuccess")
                }
            }

            file.bufferedWriter().use { writer ->
                writer.appendLine("<?xml version=\"1.0\" encoding=\"UTF-8\"?>")
                writer.appendLine("<oplus-config>")

                features.forEach { feature ->
                    when (feature) {
                        is OplusFeature.Standard -> {
                            writer.appendLine("\t<oplus-feature name=\"${escapeXml(feature.name)}\"/>")
                        }
                        is OplusFeature.Unavailable -> {
                            writer.appendLine("\t<unavailable-oplus-feature name=\"${escapeXml(feature.name)}\"/>")
                        }
                    }
                }

                writer.appendLine("</oplus-config>")
                writer.appendLine("<!-- Generated by ConfigMergeManager at ${System.currentTimeMillis()} -->")
            }

            // 验证文件是否写入成功
            if (file.exists()) {
                CLog.i(TAG, "oplus-feature.xml 写入成功: ${file.absolutePath}, 大小: ${file.length()} 字节")
            } else {
                CLog.e(TAG, "oplus-feature.xml 写入失败: 文件不存在")
            }

        } catch (e: Exception) {
            CLog.e(TAG, "写入 oplus-feature.xml 失败: ${file.absolutePath}", e)
            throw e // 重新抛出异常，让调用者知道写入失败
        }
    }

    /**
     * XML转义
     */
    private fun escapeXml(value: String): String {
        return value
            .replace("&", "&amp;")
            .replace("\"", "&quot;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
    }
    
    private fun generateAppFeaturePatches(original: List<AppFeature>, modified: List<AppFeature>): List<AppFeaturePatch> {
        val patches = mutableListOf<AppFeaturePatch>()

        // 检查修改和新增
        modified.forEach { modifiedFeature ->
            val originalFeature = original.find { it.name == modifiedFeature.name }

            if (originalFeature == null) {
                // 新增特性
                patches.add(AppFeaturePatch(
                    name = modifiedFeature.name,
                    action = PatchAction.ADD,
                    enabled = modifiedFeature.enabled,
                    args = modifiedFeature.args,
                    subNodes = modifiedFeature.subNodes
                ))
            } else {
                // 检查是否真的有变化 - 特别处理boolean参数
                val hasChanged = if (modifiedFeature.args?.startsWith("boolean:") == true &&
                                    originalFeature.args?.startsWith("boolean:") == true) {
                    // 对于boolean参数，比较enabled状态而不是args字符串
                    originalFeature.enabled != modifiedFeature.enabled ||
                    originalFeature.name != modifiedFeature.name ||
                    originalFeature.subNodes != modifiedFeature.subNodes
                } else {
                    // 对于非boolean参数，使用标准比较
                    originalFeature != modifiedFeature
                }

                if (hasChanged) {
                    // 修改特性 - 关键修复：正确处理布尔值逻辑
                    val finalArgs = if (modifiedFeature.args?.startsWith("boolean:") == true) {
                        // 对于布尔类型，根据enabled状态生成正确的args
                        "boolean:${modifiedFeature.enabled}"
                    } else {
                        // 对于非布尔类型，直接使用args
                        modifiedFeature.args
                    }

                    patches.add(AppFeaturePatch(
                        name = modifiedFeature.name,
                        action = PatchAction.MODIFY,
                        enabled = modifiedFeature.enabled,
                        args = finalArgs,
                        subNodes = modifiedFeature.subNodes
                    ))
                }
            }
        }

        // 检查删除
        original.forEach { originalFeature ->
            if (modified.none { it.name == originalFeature.name }) {
                patches.add(AppFeaturePatch(
                    name = originalFeature.name,
                    action = PatchAction.REMOVE
                ))
            }
        }

        return patches
    }

    private fun generateOplusFeaturePatches(original: List<OplusFeature>, modified: List<OplusFeature>): List<OplusFeaturePatch> {
        val patches = mutableListOf<OplusFeaturePatch>()

        // 检测可能的name修改：如果数量相同且只有一个name变化，视为修改而非删除+新增
        val nameChanges = detectOplusFeatureNameChanges(original, modified)

        // 检查修改和新增
        modified.forEach { modifiedFeature ->
            val originalFeature = original.find { it.name == modifiedFeature.name }

            // 检查是否是name修改的目标特性
            val isNameChangeTarget = nameChanges.values.contains(modifiedFeature.name)

            if (originalFeature == null && !isNameChangeTarget) {
                // 真正的新增特性（不是name修改的结果）
                val action = when (modifiedFeature) {
                    is OplusFeature.Standard -> OplusPatchAction.ENABLE
                    is OplusFeature.Unavailable -> OplusPatchAction.DISABLE
                }
                patches.add(OplusFeaturePatch(modifiedFeature.name, action))
            } else if (originalFeature != null && originalFeature::class != modifiedFeature::class) {
                // 类型变更（Standard ↔ Unavailable）
                val action = when (modifiedFeature) {
                    is OplusFeature.Standard -> OplusPatchAction.ENABLE
                    is OplusFeature.Unavailable -> OplusPatchAction.DISABLE
                }
                patches.add(OplusFeaturePatch(modifiedFeature.name, action))
            }
        }

        // 处理name修改：为新name生成MODIFY补丁
        nameChanges.forEach { (_, newName) ->
            val modifiedFeature = modified.find { it.name == newName }
            if (modifiedFeature != null) {
                val action = when (modifiedFeature) {
                    is OplusFeature.Standard -> OplusPatchAction.ENABLE
                    is OplusFeature.Unavailable -> OplusPatchAction.DISABLE
                }
                patches.add(OplusFeaturePatch(newName, action))
            }
        }

        // 检查删除（排除name修改的情况）
        original.forEach { originalFeature ->
            val isNameChangeSource = nameChanges.containsKey(originalFeature.name)
            if (!isNameChangeSource && modified.none { it.name == originalFeature.name }) {
                patches.add(OplusFeaturePatch(
                    name = originalFeature.name,
                    action = OplusPatchAction.REMOVE
                ))
            }
        }

        return patches
    }

    /**
     * 检测oplus特性的name修改
     * 如果数量相同且只有一个name变化，视为修改而非删除+新增
     */
    private fun detectOplusFeatureNameChanges(original: List<OplusFeature>, modified: List<OplusFeature>): Map<String, String> {
        // 只有在数量相同时才考虑name修改
        if (original.size != modified.size) return emptyMap()

        val originalNames = original.map { it.name }.toSet()
        val modifiedNames = modified.map { it.name }.toSet()

        val removedNames = originalNames - modifiedNames
        val addedNames = modifiedNames - originalNames

        // 只有当恰好有一个name被移除和一个name被添加时，才视为name修改
        if (removedNames.size == 1 && addedNames.size == 1) {
            val oldName = removedNames.first()
            val newName = addedNames.first()

            // 确保类型匹配（Standard对Standard，Unavailable对Unavailable）
            val originalFeature = original.find { it.name == oldName }
            val modifiedFeature = modified.find { it.name == newName }

            if (originalFeature != null && modifiedFeature != null &&
                originalFeature::class == modifiedFeature::class) {
                return mapOf(oldName to newName)
            }
        }

        return emptyMap()
    }
}
