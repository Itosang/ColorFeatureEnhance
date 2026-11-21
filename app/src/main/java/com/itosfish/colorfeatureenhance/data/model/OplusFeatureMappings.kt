package com.itosfish.colorfeatureenhance.data.model

import android.content.Context
import com.itosfish.colorfeatureenhance.R

/**
 * Oplus 系统级特性映射表管理（基于用户手机系统自带和应用内置资源）
 */
class OplusFeatureMappings private constructor() {

    /**
     * feature name -> string resId
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
        "oplus.misc.fan.fanservice_v2.support" to R.string.oplus_feature_misc_fan_fanservice_v2_support,

        // 新增推荐与增强特性
        "business.module.feeladjust.v2.FeelAdjustV2Feature" to R.string.preset_feel_adjust_v2,
        "business.padresolution.PadResolutionFeature" to R.string.preset_pad_resolution,
        "business.module.frameinsert.FrameStableFeature" to R.string.preset_frame_stable,
        "oplus.software.radio.game_assistant_esports_network_enhancement" to R.string.preset_esports_network,
        "oplus.software.wms.panorama_work_station" to R.string.preset_panorama_workstation,
        "oplus.software.display.game.support_vulkan" to R.string.preset_game_support_vulkan,
        "oplus.software.touchpanel.mlbb_player_sensitivity" to R.string.preset_mlbb_sensitivity,
        "oplus.touchdisplay.synchronization.support" to R.string.preset_touch_display_sync,

        // 山海通信网络引擎
        "oplus.software.radio.ai_link_boost_notification" to R.string.preset_shanhai_comm,
        "oplus.software.radio.ai_link_boost" to R.string.preset_shanhai_comm
    )

    companion object {
        @Volatile private var INSTANCE: OplusFeatureMappings? = null

        fun getInstance(): OplusFeatureMappings {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: OplusFeatureMappings().also { INSTANCE = it }
            }
        }

        /**
         * 获取特性的描述，优先级：Oplus内置资源 > UserFeatureMappings > 特性名称本身
         */
        fun getLocalizedDescription(context: Context, featureName: String): String {
            // 1. 最高优先级：应用内置资源映射
            val resId = getInstance().getResId(featureName)
            if (resId != R.string.feature_unknown) {
                return context.getString(resId)
            }

            // 2. 次级优先级：用户手机自定义映射
            val userMappings = UserFeatureMappings.getInstance(context)
            val userDesc = userMappings.getDescription(featureName)
            if (!userDesc.isNullOrEmpty()) return userDesc

            // 3. 最终回退：特性名称本身
            return featureName
        }

        /** 保存用户自定义映射 */
        fun saveUserMapping(context: Context, name: String, description: String) {
            UserFeatureMappings.getInstance(context).saveMapping(name, description)
        }

        /** 删除用户映射 */
        fun removeUserMapping(context: Context, name: String) {
            UserFeatureMappings.getInstance(context).removeMapping(name)
        }

        fun isMatchingPresetDescription(context: Context, name: String, description: String): Boolean {
            val resId = getInstance().getResId(name)
            if (resId == R.string.feature_unknown) return false
            val preset = context.getString(resId)
            return preset == description
        }
    }

    fun getResId(name: String): Int = NAME_TO_RES_ID[name] ?: R.string.feature_unknown

    /**
     * 获取应用内置的全部特性名称。
     */
    fun getBuiltInNames(): Set<String> = NAME_TO_RES_ID.keys

    /**
     * 是否为应用内置映射中的特性。
     */
    fun isBuiltInFeature(name: String): Boolean = NAME_TO_RES_ID.containsKey(name)
}
