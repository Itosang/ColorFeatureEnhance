package com.itosfish.colorfeatureenhance.data.model

import com.itosfish.colorfeatureenhance.FeatureMode
import com.itosfish.colorfeatureenhance.R

/**
 * 从参考脚本挖掘、可追加到原始 XML 的推荐特性。
 * 默认处于未启用状态（enabled = false），是否开启严格取决于设备当前配置中是否存在。
 */
data class AppendPreset(
    val nameResId: Int,
    val mode: FeatureMode,
    val features: List<AppFeature>
)

object AppendPresets {

    // 用户给出的 9 个通用增强特性（在 APP、OPLUS、REGION 三个分区全部提供）
    private val COMMON_ENHANCED_PRESETS = listOf(
        R.string.preset_feel_adjust_v2 to "business.module.feeladjust.v2.FeelAdjustV2Feature",
        R.string.preset_pad_resolution to "business.padresolution.PadResolutionFeature",
        R.string.preset_frame_stable to "business.module.frameinsert.FrameStableFeature",
        R.string.preset_wlan_game_chip to "oplus.software.wlan.game_chip",
        R.string.preset_esports_network to "oplus.software.radio.game_assistant_esports_network_enhancement",
        R.string.preset_panorama_workstation to "oplus.software.wms.panorama_work_station",
        R.string.preset_game_support_vulkan to "oplus.software.display.game.support_vulkan",
        R.string.preset_mlbb_sensitivity to "oplus.software.touchpanel.mlbb_player_sensitivity",
        R.string.preset_touch_display_sync to "oplus.touchdisplay.synchronization.support"
    )

    private fun generateCommonPresets(): List<AppendPreset> {
        return FeatureMode.entries.flatMap { mode ->
            COMMON_ENHANCED_PRESETS.map { (resId, featureName) ->
                AppendPreset(
                    nameResId = resId,
                    mode = mode,
                    features = listOf(AppFeature(featureName, false))
                )
            }
        }
    }

    val ALL = listOf(
        AppendPreset(
            nameResId = R.string.preset_3d_wallpaper,
            mode = FeatureMode.APP,
            features = listOf(
                AppFeature("com.oplus.wallpapers.3d_wallpaper", false, "boolean:true"),
                AppFeature("com.oplus.wallpapers.ai_camera_movement_for_products_before_15", false, "boolean:true")
            )
        ),
        AppendPreset(
            nameResId = R.string.preset_all_day_aod,
            mode = FeatureMode.APP,
            features = listOf(
                AppFeature("oplus.software.disable_aod_all_day_mode", false, "boolean:false"),
                AppFeature("com.oplus.systemui.panoramic_aod_all_day_default_open.enable", false, "boolean:true"),
                AppFeature("com.oplus.systemui.panoramic_aod_all_day.enable", false, "boolean:true"),
                AppFeature("oplus_keyguard_panoramic_aod_all_day_support", false, "boolean:true")
            )
        ),
        AppendPreset(
            nameResId = R.string.preset_shanhai_comm,
            mode = FeatureMode.OPLUS,
            features = listOf(
                AppFeature("oplus.software.radio.ai_link_boost_notification", false),
                AppFeature("oplus.software.radio.ai_link_boost", false)
            )
        )
    ) + generateCommonPresets()

    fun forMode(mode: FeatureMode): List<AppendPreset> = ALL.filter { it.mode == mode }

    /**
     * 判断某个特性名称是否属于内置推荐预设
     */
    fun isPresetFeature(name: String): Boolean = ALL.any { preset -> preset.features.any { it.name == name } }
}
