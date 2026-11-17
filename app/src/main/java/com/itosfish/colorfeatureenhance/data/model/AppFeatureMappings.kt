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
        "oplus.software.wlan.wifi6" to R.string.oplus_feature_wlan_wifi6,
        "oplus.software.wlan.game_chip" to R.string.oplus_feature_wlan_game_chip,
        "oplus.software.wlan.dual_sta" to R.string.oplus_feature_wlan_dual_sta,
        "oplus.software.wlan.assistant" to R.string.oplus_feature_wlan_assistant,
        "oplus.software.wlan.dbs" to R.string.oplus_feature_wlan_dbs,
        "oplus.software.audio.super_volume_4x" to R.string.oplus_feature_super_volume_4x,
        "oplus.software.audio.super_volume_5x" to R.string.oplus_feature_super_volume_5x,
        "com.oplus.smartmediacontroller.lss_assistant_enable" to R.string.feature_sidebar_audio_split_assistant,
        "com.oplus.mediaturbo.transcoding" to R.string.feature_mediaturbo_transcoding,
        "com.oplus.mediaturbo.jianying_optimize" to R.string.feature_jianying_optimize,
        "com.oplus.mediaturbo.douyin_live_beauty" to R.string.feature_douyin_live_beauty,
        "com.oplus.mediaturbo.game_live" to R.string.feature_live_assistant,
        "com.oplus.mediaturbo.tencent_meeting" to R.string.feature_tencent_meeting_turbo,
        "com.oplus.mediaturbo.wechat_videoCalling" to R.string.feature_wechat_video_call_enhance,
        "com.oplus.mediaturbo.window_mode_notify" to R.string.feature_mediaturbo_window_mode_notify,
        "oplus.software.vip_preload" to R.string.oplus_feature_vip_preload,
        "pre.open.camera.from.launcher" to R.string.feature_preload_camera_launcher,
        "oplus.misc.fan.support" to R.string.oplus_feature_misc_fan_support,
        "oplus.misc.fan.extra_v3.support" to R.string.oplus_feature_misc_fan_extra_v3_support,
        "oplus.misc.fan.fanservice_v2.support" to R.string.oplus_feature_misc_fan_fanservice_v2_support,
        "feature.custom_heat_game.support" to R.string.feature_game_fan_power_mode,
        "os.graphic.gallery.os15_secrecy" to R.string.feature_ai_graphic_gallery_engine,
        "com.oplus.aiwriter.input_entrance_enabled" to R.string.feature_aiwriter_input_entrance,
        "com.oplus.directservice.aitoolbox_enable" to R.string.feature_ai_chat_scene,
        "com.oplus.eyeprotect.ai_intelligent_eye_protect_support" to R.string.feature_ai_intelligent_eye_protect,
        "os.graphic.gallery.collage.livephoto" to R.string.feature_gallery_collage_livephoto,
        "com.coloros.colordirectservice.cm_enable" to R.string.feature_colordirectservice_enable,
        "com.oplus.systemui.panoramic_aod.enable" to R.string.feature_global_aod,
        "os.personalization.fullscreen_aod.enable" to R.string.feature_fullscreen_aod_enable,
        "oplus.aod.wakebyclick.support" to R.string.feature_tap_to_wake_screen,
        "com.oplus.wallpapers.livephoto_wallpaper" to R.string.feature_livephoto_lock_screen,
        "com.oplus.wallpapers.3d_wallpaper" to R.string.feature_3d_wallpaper,
        "com.oplus.fullscene_plc_charge.support" to R.string.feature_all_scene_bypass_charging,
        "com.oplus.plc_charge.support" to R.string.feature_all_scene_bypass_charging,
        "com.oplus.games.show_bypass_charging_when_gameapps" to R.string.feature_game_bypass_charging,
        "com.oplus.reversecharge" to R.string.feature_reverse_charging,
        "os.charge.settings.wirelesscharge.support" to R.string.feature_wireless_charge_support,
        "oplus.power.onwirelesscharger.support" to R.string.feature_wireless_charge_support,
        "oplus.power.wirelesschgwhenwired.support" to R.string.feature_wireless_charge_support,
        "os.charge.settings.wirelesschargingcoil.position" to R.string.feature_wireless_coil_position,
        "os.charge.settings.wirelesscharging.power" to R.string.feature_wireless_charge_power,
        "com.oplus.battery.whitelist_vowifi" to R.string.feature_vowifi_battery_whitelist,
        "com.oplus.battery.autostart_limit_num" to R.string.feature_autostart_limit_num,
        "feature.support.game.AI_PLAY_version3" to R.string.feature_game_ai_v3,
        "feature.support.game.AI_PLAY_version4" to R.string.feature_game_ai_v4,
        "feature.support.game.AI_PLAY_version4_mlbb" to R.string.feature_game_ai_v4,
        "feature.support.game.AI_PLAY_version4_audio" to R.string.feature_game_ai_v4,
        "feature.support.game.ASSIST_KEY" to R.string.feature_game_assist_key,
        "com.coloros.gamespace_game_support_fourd_vibration" to R.string.feature_game_4d_vibration,
        "com.coloros.gamesettings.support" to R.string.feature_game_settings_support,
        "com.oplus.cast.support_game_mode" to R.string.feature_cast_game_mode,
        "com.android.settings.resolution_auto" to R.string.feature_settings_resolution_auto,
        "com.android.settings.network_access_permission" to R.string.feature_network_access_permission,
        "com.android.incallui.share_screen_and_touch_cmd_support" to R.string.feature_phone_touch_share,
        "com.oplus.persona.card_datamining_support" to R.string.feature_card_datamining_support,
        "com.android.launcher.recent_lock_limit_num" to R.string.feature_recent_lock_limit_num,
        "oplus_disallow_run_apps" to R.string.feature_disallow_run_apps,
        "oplus_skip_remove_recent_task" to R.string.feature_skip_remove_recent_task,
        "com.android.systemui.qs_deform_enable" to R.string.feature_qs_icon_deformable,
        "com.oplus.screenrecorder.area_record" to R.string.feature_area_record,
        "oplus.ptc.nfc.ios.settings" to R.string.feature_keyboard_nfc_ios_share,
        "oplus.ptc.nfc.settings" to R.string.feature_ptc_nfc_settings,
        "com.oplus.systemui.not_show_guide_dialog" to R.string.feature_no_guide_dialog
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