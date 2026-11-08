package com.itosfish.colorfeatureenhance.utils

import com.itosfish.colorfeatureenhance.FeatureMode
import com.itosfish.colorfeatureenhance.MainActivity.Companion.app
import java.io.File

/**
 * 直接管理三份可修改配置：
 * - App / my_product app-features
 * - Oplus / my_product oplus-feature
 * - Region / my_region oplus-feature
 *
 * original：首次启动时从系统复制，只读备份，用于区分“系统自带”
 * working：用户实际编辑的文件
 * module：模块开机挂载使用的文件
 */
object ConfigFiles {
    private const val TAG = "ConfigFiles"
    private const val MODULE_BASE = "/data/adb/cos_feat_e"

    enum class Target(
        val mode: FeatureMode,
        val partition: String,
        val fileName: String
    ) {
        APP(FeatureMode.APP, "my_product", "com.oplus.app-features.xml"),
        OPLUS(FeatureMode.OPLUS, "my_product", "com.oplus.oplus-feature.xml"),
        REGION(FeatureMode.REGION, "my_region", "com.oplus.oplus-feature.xml")
    }

    private val configsDir = File(app.getExternalFilesDir(null) ?: app.filesDir, "configs").absolutePath
    private val originalDir = "$configsDir/original"
    private val workingDir = "$configsDir/working"

    fun targets(): List<Target> = Target.entries

    fun originalFile(target: Target): File =
        File("$originalDir/${target.partition}", target.fileName)

    fun workingFile(target: Target): File =
        File("$workingDir/${target.partition}", target.fileName)

    fun systemPath(target: Target): String =
        "/${target.partition}/etc/extension/${target.fileName}"

    fun modulePath(target: Target): String =
        "$MODULE_BASE/${target.partition}/etc/extension/${target.fileName}"

    /**
     * 首次启动备份系统配置；已存在的备份绝不覆盖。
     * 完成后把 working 同步到模块目录。
     */
    fun ensureInitialized(): Boolean {
        originalDir.let { File(it).mkdirs() }
        workingDir.let { File(it).mkdirs() }

        var copied = 0
        targets().forEach { target ->
            val original = originalFile(target)
            val working = workingFile(target)

            if (!original.exists()) {
                val legacyOriginal = legacyOriginalFile(target)
                if (legacyOriginal.exists()) {
                    original.parentFile?.mkdirs()
                    legacyOriginal.copyTo(original, overwrite = false)
                    copied++
                } else {
                    val system = systemPath(target)
                    if (CSU.fileExists(system)) {
                        original.parentFile?.mkdirs()
                        CSU.runWithSu("cp \"$system\" \"${original.absolutePath}\"")
                        if (original.exists()) copied++
                    }
                }
            }

            if (!working.exists() && original.exists()) {
                working.parentFile?.mkdirs()
                val legacyWorking = legacyWorkingFile(target)
                if (legacyWorking.exists()) {
                    legacyWorking.copyTo(working, overwrite = false)
                } else {
                    original.copyTo(working, overwrite = false)
                }
            }
        }

        if (copied > 0) {
            CLog.i(TAG, "首次备份完成，共复制 $copied 个系统配置文件")
        }

        return syncToModule()
    }

    private fun legacyOriginalFile(target: Target): File {
        val subDir = if (target.partition == "my_region") "my_region/" else ""
        return File("$configsDir/system_baseline/$subDir${target.fileName}")
    }

    private fun legacyWorkingFile(target: Target): File {
        val subDir = if (target.partition == "my_region") "my_region/" else ""
        return File("$configsDir/merged_output/$subDir${target.fileName}")
    }

    fun saveWorking(target: Target, text: String): Boolean {
        val working = workingFile(target)
        working.parentFile?.mkdirs()
        working.writeText(text)
        return syncTarget(target)
    }

    fun saveWorkingBytes(target: Target, bytes: ByteArray): Boolean {
        val working = workingFile(target)
        working.parentFile?.mkdirs()
        working.writeBytes(bytes)
        return syncTarget(target)
    }

    fun syncToModule(): Boolean {
        return targets().map { syncTarget(it) }.all { it }
    }

    private fun syncTarget(target: Target): Boolean {
        val working = workingFile(target)
        if (!working.exists()) {
            CLog.w(TAG, "working 文件不存在，跳过同步: ${working.absolutePath}")
            return false
        }

        val modulePath = modulePath(target)
        val systemPath = systemPath(target)
        val command = buildString {
            append("mkdir -p \"${File(modulePath).parent}\" ; ")
            append("cp \"${working.absolutePath}\" \"$modulePath\" ; ")
            append("chmod 644 \"$modulePath\" 2>/dev/null ; ")
            append("cp \"${working.absolutePath}\" \"$systemPath\" 2>/dev/null ; ")
            append("chmod 644 \"$systemPath\" 2>/dev/null")
        }

        val result = CSU.runWithSu(command)
        val isModuleWritten = CSU.fileExists(modulePath)
        CLog.i(TAG, "同步 ${target.partition}/${target.fileName}: module=$isModuleWritten, output=${result.output}")
        return isModuleWritten
    }

    /**
     * 读取首次备份中的特性名称，用于区分“系统自带”和“应用新增”。
     */
    fun loadOriginalNames(target: Target): Set<String> {
        val original = originalFile(target)
        if (!original.exists()) return emptySet()

        val pattern = Regex("""<(?:oplus-feature|unavailable-oplus-feature|app_feature)\b[^>]*\bname="([^"]+)"""")
        return pattern.findAll(original.readText())
            .map { it.groupValues[1] }
            .toSet()
    }

    fun isInOriginal(target: Target, featureName: String): Boolean {
        return featureName in loadOriginalNames(target)
    }

    fun targetForMode(mode: FeatureMode): Target {
        return Target.entries.first { it.mode == mode }
    }
}
