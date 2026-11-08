package com.itosfish.colorfeatureenhance.utils

import android.system.Os
import android.util.Log
import com.itosfish.colorfeatureenhance.MainActivity.Companion.app
import java.io.File
import java.io.FileOutputStream

/**
 * 新架构的配置管理工具类
 * 负责配置目录初始化、模块安装等基础功能
 */
object ConfigUtils {
    const val LATEST_MODULE_VERSION = 21

    private const val TAG = "ConfigUtils"

    // 配置文件名常量
    private const val APP_FEATURES_FILE = "com.oplus.app-features.xml"
    private const val OPLUS_FEATURES_FILE = "com.oplus.oplus-feature.xml"
    private const val OPLUS_PATCH_FILE = "oplus-features.patch.json"
    private const val REGION_PATCH_FILE = "oplus-region-features.patch.json"

    // 配置目录路径
    private val baseDir = app.getExternalFilesDir(null)?.absolutePath!!
    private val configsDir = "$baseDir/configs"
    private val systemBaselineDir = "$configsDir/system_baseline"
    private val regionBaselineDir = "$systemBaselineDir/my_region"
    private val userPatchesDir = "$configsDir/user_patches"
    private val mergedOutputDir = "$configsDir/merged_output"
    private val regionMergedOutputDir = "$mergedOutputDir/my_region"



    /**
     * 初始化新架构的配置管理系统
     * 创建必要的目录结构，但不复制任何配置文件
     * 配置文件的复制由模块脚本负责
     */
    fun initializeConfigSystem(): Boolean {
        try {
            CLog.i(TAG, "开始初始化配置管理系统")
            CLog.d(TAG, "基础目录: $baseDir")
            CLog.d(TAG, "配置目录: $configsDir")

            // 检查基础目录是否可访问
            val baseFile = File(baseDir)
            if (!baseFile.exists()) {
                CLog.e(TAG, "基础目录不存在: $baseDir")
                return false
            }

            CLog.d(TAG, "基础目录检查: 存在=${baseFile.exists()}, 是目录=${baseFile.isDirectory()}, 可写=${baseFile.canWrite()}")

            // 创建目录结构
            CLog.d(TAG, "创建目录结构...")
            createDirectoryStructure()

            // 确保权限正确
            CLog.d(TAG, "检查并修复权限...")
            val permissionFixed = ensureProperPermissions()
            if (!permissionFixed) {
                CLog.w(TAG, "权限修复失败，但继续执行")
            }

            // 验证目录创建结果
            val success = verifyDirectoryStructure()
            if (success) {
                CLog.i(TAG, "配置管理系统初始化完成")
            } else {
                CLog.e(TAG, "目录结构验证失败")
            }

            return success

        } catch (e: Exception) {
            CLog.e(TAG, "配置系统初始化失败", e)
            return false
        }
    }

    /**
     * 创建新架构的目录结构
     */
    private fun createDirectoryStructure() {
        try {
            val directories = listOf(
                systemBaselineDir to "system_baseline",
                regionBaselineDir to "system_baseline/my_region",
                userPatchesDir to "user_patches",
                mergedOutputDir to "merged_output",
                regionMergedOutputDir to "merged_output/my_region"
            )

            directories.forEach { (path, name) ->
                val dir = File(path)
                if (!dir.exists()) {
                    val success = dir.mkdirs()
                    if (success) {
                        CLog.d(TAG, "成功创建目录: $name ($path)")
                    } else {
                        CLog.w(TAG, "创建目录失败: $name ($path)")
                    }
                } else {
                    CLog.d(TAG, "目录已存在: $name ($path)")
                }
            }
        } catch (e: Exception) {
            CLog.e(TAG, "创建目录结构时发生异常", e)
            throw e // 重新抛出异常，因为目录创建失败是严重问题
        }
    }



    /**
     * 验证目录结构是否正确创建
     */
    private fun verifyDirectoryStructure(): Boolean {
        val directories = listOf(
            systemBaselineDir to "system_baseline",
            regionBaselineDir to "system_baseline/my_region",
            userPatchesDir to "user_patches",
            mergedOutputDir to "merged_output",
            regionMergedOutputDir to "merged_output/my_region"
        )

        var allSuccess = true
        directories.forEach { (path, name) ->
            val dir = File(path)
            val exists = dir.exists()
            val isDirectory = dir.isDirectory()

            CLog.d(TAG, "目录检查: $name - 存在:$exists, 是目录:$isDirectory")

            if (exists && isDirectory) {
                CLog.d(TAG, "目录验证成功: $name")
            } else {
                CLog.e(TAG, "目录验证失败: $name ($path) - 存在:$exists, 是目录:$isDirectory")
                allSuccess = false
            }
        }

        return allSuccess
    }

    /**
     * 确保配置目录有正确的权限（简化版：统一使用660权限）
     */
    private fun ensureProperPermissions(): Boolean {
        return try {
            val configDir = File(configsDir)
            if (!configDir.exists()) {
                CLog.d(TAG, "配置目录不存在，无需修复权限")
                return true
            }

            // 检查是否可写
            if (!configDir.canWrite()) {
                CLog.w(TAG, "配置目录无写权限，使用简化权限修复（660权限）")

                val fixCmd = """
                    CONFIG_DIR="$configsDir"

                    # 检查配置目录是否存在
                    if [ ! -d "${'$'}CONFIG_DIR" ]; then
                        echo "配置目录不存在: ${'$'}CONFIG_DIR"
                        exit 1
                    fi

                    echo "设置配置目录及所有子目录和文件为660权限..."

                    # 统一设置660权限
                    chmod 660 "${'$'}CONFIG_DIR" 2>/dev/null
                    find "${'$'}CONFIG_DIR" -type d -exec chmod 660 {} \; 2>/dev/null
                    find "${'$'}CONFIG_DIR" -type f -exec chmod 660 {} \; 2>/dev/null

                    # 验证结果
                    FINAL_PERM=${'$'}(stat -c %a "${'$'}CONFIG_DIR" 2>/dev/null)
                    echo "简化权限修复完成: 权限=${'$'}FINAL_PERM"
                """.trimIndent()

                val result = CSU.runWithSu(fixCmd)
                CLog.i(TAG, "简化权限修复结果: ${result.output}")

                // 重新检查是否可写
                val canWriteAfterFix = configDir.canWrite()
                CLog.i(TAG, "简化权限修复后可写状态: $canWriteAfterFix")
                return canWriteAfterFix
            } else {
                CLog.d(TAG, "配置目录权限正常")
                return true
            }
        } catch (e: Exception) {
            CLog.e(TAG, "简化权限检查失败", e)
            false
        }
    }

    /**
     * 获取配置目录路径
     */
    fun getConfigPaths(): ConfigPaths {
        return ConfigPaths(
            configsDir = configsDir,
            systemBaselineDir = systemBaselineDir,
            regionBaselineDir = regionBaselineDir,
            userPatchesDir = userPatchesDir,
            mergedOutputDir = mergedOutputDir,
            regionMergedOutputDir = regionMergedOutputDir,
            appFeaturesFile = APP_FEATURES_FILE,
            oplusFeaturesFile = OPLUS_FEATURES_FILE,
            oplusPatchFile = OPLUS_PATCH_FILE,
            regionPatchFile = REGION_PATCH_FILE
        )
    }

    data class ConfigPaths(
        val configsDir: String,
        val systemBaselineDir: String,
        val regionBaselineDir: String,
        val userPatchesDir: String,
        val mergedOutputDir: String,
        val regionMergedOutputDir: String,
        val appFeaturesFile: String,
        val oplusFeaturesFile: String,
        val oplusPatchFile: String,
        val regionPatchFile: String
    )

    /**
     * 安装模块（简化版）
     * 从 assets 解压模块到指定目录
     */
    fun installModule(): Boolean {
        return try {
            Log.i(TAG, "开始安装模块")

            // 临时工作目录
            val tempDir = File(app.cacheDir, "moduleTemp").apply {
                if (exists()) deleteRecursively()
                mkdirs()
            }

            // 解压模块文件
            val tempZip = File(tempDir, "mod.zip")
            if (!extractModuleAssets(tempZip)) {
                Log.e(TAG, "解压模块资源失败")
                return false
            }

            if (CSU.installModule(tempZip.absolutePath)) {
                Log.i(TAG, "模块安装成功")
                true
            } else {
                Log.e(TAG, "模块安装失败")
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "模块安装过程中发生异常", e)
            false
        }
    }

    /**
     * 从 assets 提取模块文件
     */
    private fun extractModuleAssets(tempZip: File): Boolean {
        return try {
            app.assets.open("mod.zip").use { input ->
                FileOutputStream(tempZip).use { output ->
                    input.copyTo(output)
                }
            }
            true
        } catch (e: Exception) {
            Log.e(TAG, "提取模块资源失败", e)
            false
        }
    }

    /*
        /**
         * 解压 zip 文件到指定目录
         */
        private fun unzip(zipFile: File, destDir: File): Boolean {
            return try {
                ZipInputStream(zipFile.inputStream()).use { zis ->
                    var entry: ZipEntry?
                    while (zis.nextEntry.also { entry = it } != null) {
                        val newFile = File(destDir, entry!!.name)
                        if (entry!!.isDirectory) {
                            newFile.mkdirs()
                        } else {
                            newFile.parentFile?.mkdirs()
                            FileOutputStream(newFile).use { fos ->
                                zis.copyTo(fos)
                            }
                        }
                    }
                }
                true
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        }
    */

    /**
     * 检查模块是否已安装（基于模块版本）
     * 注意：新的自动更新策略不再使用此方法，而是基于应用版本变化
     */
    fun shouldInstallModule(): Boolean {
        return moduleVersion < LATEST_MODULE_VERSION
    }

    /**
     * 获取模块版本信息
     */
    val moduleVersion: Int
        get() {
            val modulePropsPath = "/data/adb/modules/ColorOSFeaturesEnhance/module.prop"
            if (!CSU.fileExists(modulePropsPath)) return -1

            return try {
                val output =
                    CSU.runWithSu("grep '^versionCode=' \"$modulePropsPath\" | cut -d'=' -f2").output
                output.trim().takeIf { it.isNotEmpty() }?.toInt() ?: -1
            } catch (e: Exception) {
                CLog.e(TAG, "获取模块版本失败", e)
                -1
            }
        }

    /**
     * 将merged_output的配置复制到模块挂载目录
     * 每次保存配置后调用此方法
     */
    fun copyMergedConfigToModule(): Boolean {
        return try {
            CLog.i(TAG, "开始复制合并配置到模块目录")

            val configPaths = getConfigPaths()
            val moduleBase = "/data/adb/cos_feat_e"
            val productDir = "$moduleBase/my_product/etc/extension"
            val regionDir = "$moduleBase/my_region/etc/extension"

            val shellCmd = StringBuilder("mkdir -p \"$productDir\" \"$regionDir\"")
            var hasFilesToCopy = false

            listOf(configPaths.appFeaturesFile, configPaths.oplusFeaturesFile).forEach { fileName ->
                val source = File(configPaths.mergedOutputDir, fileName)
                if (source.exists()) {
                    shellCmd.append(" && cp \"${source.absolutePath}\" \"$productDir/\"")
                    hasFilesToCopy = true
                } else {
                    CLog.w(TAG, "源文件不存在: ${source.absolutePath}")
                }
            }

            val regionSource = File(configPaths.regionMergedOutputDir, configPaths.oplusFeaturesFile)
            if (regionSource.exists()) {
                shellCmd.append(" && cp \"${regionSource.absolutePath}\" \"$regionDir/\"")
                hasFilesToCopy = true
            } else {
                CLog.w(TAG, "区域配置不存在: ${regionSource.absolutePath}")
            }

            if (!hasFilesToCopy) {
                CLog.e(TAG, "没有找到任何需要复制的文件")
                return false
            }

            CSU.runWithSu(shellCmd.toString())
            setModulePermissions(moduleBase)
            syncMergedConfigToSystem(configPaths)

            val isProductCopied = CSU.fileExists("$productDir/${configPaths.appFeaturesFile}") ||
                CSU.fileExists("$productDir/${configPaths.oplusFeaturesFile}")
            val isRegionCopied = CSU.fileExists("$regionDir/${configPaths.oplusFeaturesFile}")
            CLog.i(TAG, "模块配置复制结果: my_product=$isProductCopied, my_region=$isRegionCopied")
            isProductCopied || isRegionCopied

        } catch (e: Exception) {
            CLog.e(TAG, "复制配置到模块目录时发生异常", e)
            false
        }
    }

    /**
     * 同时直接写入系统路径，避免未挂载时看不到新配置
     */
    private fun syncMergedConfigToSystem(configPaths: ConfigPaths) {
        val copies = listOf(
            File(configPaths.mergedOutputDir, configPaths.appFeaturesFile) to
                "/my_product/etc/extension/${configPaths.appFeaturesFile}",
            File(configPaths.mergedOutputDir, configPaths.oplusFeaturesFile) to
                "/my_product/etc/extension/${configPaths.oplusFeaturesFile}",
            File(configPaths.regionMergedOutputDir, configPaths.oplusFeaturesFile) to
                "/my_region/etc/extension/${configPaths.oplusFeaturesFile}"
        ).filter { (source, _) -> source.exists() }

        if (copies.isEmpty()) {
            CLog.w(TAG, "没有可写入系统路径的合并配置")
            return
        }

        val command = copies.joinToString(" ; ") { (source, target) ->
            "cp \"${source.absolutePath}\" \"$target\" 2>&1 ; chmod 644 \"$target\" 2>/dev/null"
        }
        val result = CSU.runWithSu(command)
        CLog.i(TAG, "直接写入系统路径: exit=${result.exitCode}, output=${result.output}")
        if (result.output.contains("Read-only", ignoreCase = true)) {
            CLog.w(TAG, "系统分区只读，直接写入失败，将在重启后通过模块挂载生效")
        }
    }

    /**
     * 目录保持 755 以便系统进程遍历，文件统一 644
     */
    private fun setModulePermissions(moduleBase: String) {
        val targetDirs = listOf("$moduleBase/my_product", "$moduleBase/my_region")
            .filter { CSU.dirExists(it) }

        if (targetDirs.isEmpty()) {
            CLog.w(TAG, "没有需要设置权限的目录")
            return
        }

        val permissionCmd = targetDirs.joinToString(" && ") { dir ->
            "chmod -R 755 \"$dir\" && find \"$dir\" -type f -exec chmod 644 {} +"
        }
        CLog.d(TAG, "执行权限设置命令: $permissionCmd")
        val result = CSU.runWithSu(permissionCmd).output
        CLog.d(TAG, "权限设置命令执行结果: $result")
        CLog.i(TAG, "模块目录权限设置完成")
    }

    fun copySystemConfig() {
        CLog.i(TAG, "开始复制系统原本配置")
        try {
            CSU.runWithSu("""
                busybox ash /data/adb/modules/ColorOSFeaturesEnhance/copy.sh ${Os.getuid()}
            """.trimIndent()).apply {
                CLog.d(TAG, "复制系统原本配置结果: $exitCode")
            }
        } catch (e: Exception) {
            CLog.e(TAG, "复制系统原本配置时发生异常", e)
        }
    }
}