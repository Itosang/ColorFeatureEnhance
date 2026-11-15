package com.itosfish.colorfeatureenhance.utils

import android.content.Context
import android.content.pm.PackageManager
import com.itosfish.colorfeatureenhance.MainActivity.Companion.app

/**
 * 应用版本管理工具类
 * 负责检测应用版本变化，触发模块自动更新
 */
object AppVersionManager {
    private const val TAG = "AppVersionManager"
    private const val PREFS_NAME = "app_version_prefs"
    private const val KEY_LAST_VERSION_CODE = "last_version_code"
    private const val KEY_LAST_VERSION_NAME = "last_version_name"
    private const val KEY_FIRST_LAUNCH = "first_launch"
    
    /**
     * 版本变化类型
     */
    enum class VersionChangeType {
        FIRST_LAUNCH,    // 首次启动
        MAJOR_UPDATE,    // 主要更新（版本号增加）
        DOWNGRADE,       // 版本降级
        NO_CHANGE        // 无变化
    }
    
    /**
     * 版本信息数据类
     */
    data class VersionInfo(
        val versionCode: Long,
        val versionName: String,
        val changeType: VersionChangeType,
        val previousVersionCode: Long = -1,
        val previousVersionName: String = ""
    )
    
    /**
     * 获取当前应用版本信息
     */
    fun getCurrentVersionInfo(): VersionInfo? {
        return try {
            val packageInfo = app.packageManager.getPackageInfo(app.packageName, 0)
            val currentVersionCode = packageInfo.longVersionCode
            val currentVersionName = packageInfo.versionName ?: "unknown"
            
            val prefs = app.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val lastVersionCode = prefs.getLong(KEY_LAST_VERSION_CODE, -1)
            val lastVersionName = prefs.getString(KEY_LAST_VERSION_NAME, "") ?: ""
            val isFirstLaunch = prefs.getBoolean(KEY_FIRST_LAUNCH, true)
            
            val changeType = when {
                isFirstLaunch -> VersionChangeType.FIRST_LAUNCH
                currentVersionCode > lastVersionCode -> VersionChangeType.MAJOR_UPDATE
                currentVersionCode < lastVersionCode -> VersionChangeType.DOWNGRADE
                else -> VersionChangeType.NO_CHANGE
            }
            
            VersionInfo(
                versionCode = currentVersionCode,
                versionName = currentVersionName,
                changeType = changeType,
                previousVersionCode = lastVersionCode,
                previousVersionName = lastVersionName
            )
        } catch (e: PackageManager.NameNotFoundException) {
            CLog.e(TAG, "无法获取应用版本信息", e)
            null
        }
    }
    
    /**
     * 保存当前版本信息
     */
    fun saveCurrentVersionInfo() {
        try {
            val packageInfo = app.packageManager.getPackageInfo(app.packageName, 0)
            val currentVersionCode = packageInfo.longVersionCode
            val currentVersionName = packageInfo.versionName ?: "unknown"
            
            val prefs = app.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            prefs.edit().apply {
                putLong(KEY_LAST_VERSION_CODE, currentVersionCode)
                putString(KEY_LAST_VERSION_NAME, currentVersionName)
                putBoolean(KEY_FIRST_LAUNCH, false)
                apply()
            }
            
            CLog.i(TAG, "已保存版本信息: $currentVersionName ($currentVersionCode)")
        } catch (e: Exception) {
            CLog.e(TAG, "保存版本信息失败", e)
        }
    }

    /**
     * 获取版本变化描述
     */
    fun getVersionChangeDescription(versionInfo: VersionInfo): String {
        return when (versionInfo.changeType) {
            VersionChangeType.FIRST_LAUNCH -> 
                "首次启动 ColorFeatureEnhance v${versionInfo.versionName}"
            VersionChangeType.MAJOR_UPDATE -> 
                "应用已更新：v${versionInfo.previousVersionName} → v${versionInfo.versionName}"
            VersionChangeType.DOWNGRADE -> 
                "应用已降级：v${versionInfo.previousVersionName} → v${versionInfo.versionName}"
            VersionChangeType.NO_CHANGE -> 
                "当前版本：v${versionInfo.versionName}"
        }
    }
}
