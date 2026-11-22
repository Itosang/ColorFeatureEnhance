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
        "oplus.software.wlan.wifi6" to R.string.oplus_feature_wlan_wifi6,
        "oplus.software.wlan.game_chip" to R.string.oplus_feature_wlan_game_chip,
        "oplus.software.wlan.dual_sta" to R.string.oplus_feature_wlan_dual_sta,
        "oplus.software.wlan.assistant" to R.string.oplus_feature_wlan_assistant,
        "oplus.software.wlan.dbs" to R.string.oplus_feature_wlan_dbs,
        "oplus.software.audio.super_volume_4x" to R.string.oplus_feature_super_volume_4x,
        "com.oplus.mediaturbo.transcoding" to R.string.feature_mediaturbo_transcoding,
        "com.oplus.mediaturbo.game_live" to R.string.feature_live_assistant,
        "com.oplus.mediaturbo.tencent_meeting" to R.string.feature_tencent_meeting_turbo,
        "com.oplus.mediaturbo.window_mode_notify" to R.string.feature_mediaturbo_window_mode_notify,
        "oplus.software.vip_preload" to R.string.oplus_feature_vip_preload,
        "pre.open.camera.from.launcher" to R.string.feature_preload_camera_launcher,
        "oplus.misc.fan.support" to R.string.oplus_feature_misc_fan_support,
        "oplus.misc.fan.extra_v3.support" to R.string.oplus_feature_misc_fan_extra_v3_support,
        "oplus.misc.fan.fanservice_v2.support" to R.string.oplus_feature_misc_fan_fanservice_v2_support,
        "com.oplus.aiwriter.input_entrance_enabled" to R.string.feature_aiwriter_input_entrance,
        "os.graphic.gallery.collage.livephoto" to R.string.feature_gallery_collage_livephoto,
        "com.oplus.systemui.panoramic_aod.enable" to R.string.feature_global_aod,
        "com.oplus.wallpapers.livephoto_wallpaper" to R.string.feature_livephoto_lock_screen,
        "com.oplus.wallpapers.3d_wallpaper" to R.string.feature_3d_wallpaper,
        "com.oplus.fullscene_plc_charge.support" to R.string.feature_all_scene_bypass_charging,
        "com.coloros.gamesettings.support" to R.string.feature_game_settings_support,
        "com.oplus.cast.support_game_mode" to R.string.feature_cast_game_mode,
        "com.android.settings.resolution_auto" to R.string.feature_settings_resolution_auto,
        "com.android.settings.network_access_permission" to R.string.feature_network_access_permission,
        "com.oplus.persona.card_datamining_support" to R.string.feature_card_datamining_support,
        "com.android.launcher.recent_lock_limit_num" to R.string.feature_recent_lock_limit_num,
        "oplus_disallow_run_apps" to R.string.feature_disallow_run_apps,
        "oplus_skip_remove_recent_task" to R.string.feature_skip_remove_recent_task,
        "com.oplus.screenrecorder.area_record" to R.string.feature_area_record,
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
