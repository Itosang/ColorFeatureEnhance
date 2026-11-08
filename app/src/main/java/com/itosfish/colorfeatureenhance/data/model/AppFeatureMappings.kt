package com.itosfish.colorfeatureenhance.data.model

import android.content.Context
import com.itosfish.colorfeatureenhance.R

/**
 * 应用特性映射表管理
 */
class AppFeatureMappings private constructor() {

    /**
     * App feature name -> string resId
     */
    val NAME_TO_RES_ID: Map<String, Int> = mapOf(
        // WLAN / WiFi
        "oplus.software.wlan.wifi6" to R.string.oplus_feature_wlan_wifi6,
        "oplus.software.wlan.game_chip" to R.string.oplus_feature_wlan_game_chip,
        "oplus.software.wlan.dual_sta" to R.string.oplus_feature_wlan_dual_sta,
        "oplus.software.wlan.assistant" to R.string.oplus_feature_wlan_assistant,
        "oplus.software.wlan.dbs" to R.string.oplus_feature_wlan_dbs,

        // 音频
        "oplus.software.audio.super_volume_4x" to R.string.oplus_feature_super_volume_4x,

        // 预加载
        "oplus.software.vip_preload" to R.string.oplus_feature_vip_preload,

        // 散热风扇
        "oplus.misc.fan.support" to R.string.oplus_feature_misc_fan_support,
        "oplus.misc.fan.extra_v3.support" to R.string.oplus_feature_misc_fan_extra_v3_support,
        "oplus.misc.fan.fanservice_v2.support" to R.string.oplus_feature_misc_fan_fanservice_v2_support
    )

    /**
     * 单例实例
     */
    companion object {
        @Volatile
        private var INSTANCE: AppFeatureMappings? = null
        
        /**
         * 获取映射表实例
         */
        fun getInstance(): AppFeatureMappings {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: AppFeatureMappings().also { INSTANCE = it }
            }
        }

        /**
         * 获取特性的描述，支持三级优先级查询
         * 优先级：CloudFeatureMappings > App内置资源 > UserFeatureMappings
         * @param context 上下文
         * @param featureName 特性名称
         * @return 描述字符串
         */
        fun getLocalizedDescription(context: Context, featureName: String): String {
            // 1. 最高优先级：云端配置映射
            try {
                val cloudMappings = CloudFeatureMappings.getInstance(context)
                val currentLanguage = getCurrentLanguage(context)
                val cloudDescription = cloudMappings.getDescription(featureName, currentLanguage)
                if (cloudDescription != null && cloudDescription.isNotEmpty()) {
                    return cloudDescription
                }
            } catch (e: Exception) {
                // 云端配置获取失败时静默忽略，继续使用应用内置资源
            }

            // 2. 中等优先级：应用内置资源映射
            val resId = getInstance().getResId(featureName)
            if (resId != R.string.feature_unknown) {
                return context.getString(resId)
            }

            // 3. 最低优先级：用户自定义映射
            val userMappings = UserFeatureMappings.getInstance(context)
            val userDescription = userMappings.getDescription(featureName)
            if (userDescription != null && userDescription.isNotEmpty()) {
                return userDescription
            }

            // 4. 最终回退：特性名称本身
            return featureName
        }

        /**
         * 获取当前语言代码
         * @param context 上下文
         * @return 语言代码
         */
        private fun getCurrentLanguage(context: Context): String {
            val locale = context.resources.configuration.locales[0]
            return when (locale.language.lowercase()) {
                "zh" -> "zh"
                "en" -> "en"
                else -> "en"
            }
        }
    }

    /**
     * 获取中文描述，若未收录则返回 "待确认"。
     */
    fun getResId(featureName: String): Int = NAME_TO_RES_ID[featureName] ?: R.string.feature_unknown

    /**
     * 是否为应用内置映射中的特性。
     */
    fun isBuiltInFeature(featureName: String): Boolean = NAME_TO_RES_ID.containsKey(featureName)

    /**
     * 保存用户自定义映射
     */
    fun saveUserMapping(context: Context, name: String, description: String) {
        UserFeatureMappings.getInstance(context).saveMapping(name, description)
    }

    /**
     * 删除用户自定义映射
     */
    fun removeUserMapping(context: Context, name: String) {
        UserFeatureMappings.getInstance(context).removeMapping(name)
    }

    /**
     * 批量删除用户自定义映射
     */
    fun removeUserMappings(context: Context, names: List<String>) {
        val userMappings = UserFeatureMappings.getInstance(context)
        names.forEach { userMappings.removeMapping(it) }
    }

    /**
     * 检查特性名称和描述是否与预设映射匹配
     * @return 如果匹配返回true，否则返回false
     */
    fun isMatchingPresetDescription(context: Context, name: String, description: String): Boolean {
        val resId = getResId(name)
        // 如果是未知特性，直接返回false
        if (resId == R.string.feature_unknown) {
            return false
        }

        // 比较预设描述和用户输入的描述
        val presetDescription = context.getString(resId)
        return presetDescription == description
    }

    /**
     * 检查特性是否存在云端配置描述
     * @param context 上下文
     * @param name 特性名称
     * @return 如果存在云端配置返回true，否则返回false
     */
    fun isMatchingCloudDescription(context: Context, name: String): Boolean {
        return try {
            val cloudMappings = CloudFeatureMappings.getInstance(context)
            cloudMappings.hasMapping(name)
        } catch (e: Exception) {
            false
        }
    }
} 