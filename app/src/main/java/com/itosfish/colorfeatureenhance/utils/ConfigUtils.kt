package com.itosfish.colorfeatureenhance.utils

import android.util.Log
import com.itosfish.colorfeatureenhance.MainActivity.Companion.app
import java.io.File
import java.io.FileOutputStream

/**
 * 模块安装与版本查询。
 */
object ConfigUtils {
    const val LATEST_MODULE_VERSION = 23

    private const val TAG = "ConfigUtils"

    /**
     * 把内置模块交给 Magisk / KernelSU / APatch 安装。
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
     * 已安装模块的 versionCode，未安装返回 -1。
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
