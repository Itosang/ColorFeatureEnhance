package com.itosfish.colorfeatureenhance.data.model

import android.content.Context
import com.itosfish.colorfeatureenhance.R

/**
 * 应用特性映射表管理（基于用户手机系统自带和应用内置资源）
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
        "oplus.misc.fan.fanservice_v2.support" to R.string.oplus_feature_misc_fan_fanservice_v2_support,

        // 3D 壁纸
        "com.oplus.wallpapers.3d_wallpaper" to R.string.preset_3d_wallpaper,
        "com.oplus.wallpapers.ai_camera_movement_for_products_before_15" to R.string.preset_3d_wallpaper,

        // 全天候息屏显示
        "oplus.software.disable_aod_all_day_mode" to R.string.preset_all_day_aod,
        "com.oplus.systemui.panoramic_aod_all_day_default_open.enable" to R.string.preset_all_day_aod,
        "com.oplus.systemui.panoramic_aod_all_day.enable" to R.string.preset_all_day_aod,
        "oplus_keyguard_panoramic_aod_all_day_support" to R.string.preset_all_day_aod,

        // 新增推荐与增强特性
        "business.module.feeladjust.v2.FeelAdjustV2Feature" to R.string.preset_feel_adjust_v2,
        "business.padresolution.PadResolutionFeature" to R.string.preset_pad_resolution,
        "business.module.frameinsert.FrameStableFeature" to R.string.preset_frame_stable,
        "oplus.software.radio.game_assistant_esports_network_enhancement" to R.string.preset_esports_network,
        "oplus.software.wms.panorama_work_station" to R.string.preset_panorama_workstation,
        "oplus.software.display.game.support_vulkan" to R.string.preset_game_support_vulkan,
        "oplus.software.touchpanel.mlbb_player_sensitivity" to R.string.preset_mlbb_sensitivity,
        "oplus.touchdisplay.synchronization.support" to R.string.preset_touch_display_sync
    )

    companion object {
        @Volatile
        private var INSTANCE: AppFeatureMappings? = null

        fun getInstance(): AppFeatureMappings {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: AppFeatureMappings().also { INSTANCE = it }
            }
        }

        /**
         * 获取特性的描述，优先级：App内置资源 > UserFeatureMappings > 特性名称本身
         */
        fun getLocalizedDescription(context: Context, featureName: String): String {
            // 1. 最高优先级：应用内置资源映射
            val resId = getInstance().getResId(featureName)
            if (resId != R.string.feature_unknown) {
                return context.getString(resId)
            }

            // 2. 次级优先级：用户手机自定义映射
            val userMappings = UserFeatureMappings.getInstance(context)
            val userDescription = userMappings.getDescription(featureName)
            if (!userDescription.isNullOrEmpty()) {
                return userDescription
            }

            // 3. 最终回退：特性名称本身
            return featureName
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
     */
    fun isMatchingPresetDescription(context: Context, name: String, description: String): Boolean {
        val resId = getResId(name)
        if (resId == R.string.feature_unknown) return false
        val presetDescription = context.getString(resId)
        return presetDescription == description
    }
}
