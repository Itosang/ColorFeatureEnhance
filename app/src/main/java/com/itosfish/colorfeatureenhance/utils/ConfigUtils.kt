package com.itosfish.colorfeatureenhance.utils

import android.util.Log
import com.itosfish.colorfeatureenhance.MainActivity.Companion.app
import java.io.File
import java.io.FileOutputStream

/**
 * 配置初始化与模块安装。配置本身由 ModuleConfig 直接读写模块目录。
 */
object ConfigUtils {
    const val LATEST_MODULE_VERSION = 22

    private const val TAG = "ConfigUtils"

    /**
     * 配置直接读写模块目录，这里只检查模块是否已安装。
     */
    fun initializeConfigSystem(): Boolean {
        val isInstalled = ModuleConfig.isInstalled()
        if (isInstalled) {
            CLog.i(TAG, "模块已安装，可直接编辑配置")
        } else {
            CLog.w(TAG, "模块未安装，请先安装模块")
        }
        return isInstalled
    }

    /**
     * 安装模块：从 assets 解压模块 zip 后交给 Magisk / KernelSU / APatch。
     */
    fun installModule(): Boolean {
        return try {
            Log.i(TAG, "开始安装模块")

            val tempDir = File(app.cacheDir, "moduleTemp").apply {
                if (exists()) deleteRecursively()
                mkdirs()
            }

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

    /**
     * 读取已安装模块的 versionCode。
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
}
