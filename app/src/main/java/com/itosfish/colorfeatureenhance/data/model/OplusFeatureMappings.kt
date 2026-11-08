package com.itosfish.colorfeatureenhance.data.model

import android.content.Context
import com.itosfish.colorfeatureenhance.R

/**
 * Oplus 系统级特性映射表管理
 */
class OplusFeatureMappings private constructor() {

    /**
     * feature name -> string resId
     */
    val NAME_TO_RES_ID: Map<String, Int> = mapOf(
        "oplus.software.systemui.navbar_pick_color" to R.string.oplus_feature_pick_color_optimization,
        "oplus.software.string_gc_support" to R.string.oplus_feature_gcstring,
        "oplus.software.display.1.5k_resolution_switch_support" to R.string.oplus_feature_compatibility_code,
        "com.oplus.eyeprotect.ai_intelligent_eye_protect_support" to R.string.oplus_feature_ai_eye_protect,
        "oplus.software.radio.tt_satellite_support" to R.string.oplus_feature_satellite_network,
        "oplus.software.radio.satellite_aging" to R.string.oplus_feature_satellite_network,
        "oplus.software.radio.smart_game_qoe_recovery" to R.string.oplus_feature_game_acceleration,
        "oplus.software.gamehyper" to R.string.oplus_feature_hyper_engine,
        "oplus.software.enable_touch_preload" to R.string.oplus_feature_touch_optimization,
        "oplus.software.display.game_color_enhance_1.0" to R.string.oplus_feature_game_color_button,
        "oplus.software.display.ai_eyeprotect_v1_support" to R.string.oplus_feature_eyeprotect_reminder,
        "oplus.software.display.reduce_white_point" to R.string.oplus_feature_reduce_white_point,
        "oplus.software.shoulderkey_support" to R.string.oplus_feature_genshin_shoulder_key,
        "oplus.software.display.lux_small_debounce_expand_support" to R.string.oplus_feature_min_brightness_expand,
        "oplus.hardware.display.no_bright_eyes_low_freq_strobe" to R.string.oplus_feature_low_brightness_flicker,
        "oplus.software.display.game.ifs_enable" to R.string.oplus_feature_game_ifs,
        "oplus.video.audio.volume.enhancement" to R.string.oplus_feature_video_volume_enhance,
        "oplus.software.smart_sidebar_video_assistant" to R.string.oplus_feature_sidebar_video_assistant,
        "oplus.misc.fan.support" to R.string.oplus_feature_cooling_fan,
        "oplus.software.radio.livebroadcast_accelerate" to R.string.oplus_feature_live_scene_acceleration,
        "oplus.software.audio.record.sound_focus_support" to R.string.oplus_feature_sound_focus_record,
        "oplus.softwore.top.preload" to R.string.oplus_feature_foreground_preload,
        "default" to R.string.feature_unknown,
        "oplus.software.directservice.finger_flashnotes_enable" to R.string.oplus_feature_finger_flashnotes,
        "com.android.launcher.is_support_icon_blur" to R.string.oplus_feature_icon_blur,
        "oplus.software.display.game.htfi_enable" to R.string.oplus_feature_game_htfi_high_temp,
        "oplus.hardware.display.bl_pwm_dc" to R.string.oplus_feature_pwm_dimming,
        "oplus.hardware.display.full_brightness_DC" to R.string.oplus_feature_full_brightness_dc,
        "oplus.software.audio.super_volume_4x" to R.string.oplus_feature_super_volume_4x,
        "oplus.hardware.audio.voice_isolation_support" to R.string.oplus_feature_voice_isolation,
        "oplus.software.spatializer_speaker" to R.string.oplus_feature_speaker_spatializer,
        "oplus.software.radio.support_5g" to R.string.oplus_feature_5g_support,
        "oplus.software.radio.support_dual_nr" to R.string.oplus_feature_dual_nr,
        "oplus.software.radio.nr_always_sa_pre" to R.string.oplus_feature_nr_always_sa_pre,
        "oplus.software.radio.smart5g_sa_enabled" to R.string.oplus_feature_smart5g_sa,
        "oplus.software.radio.game_accelerate" to R.string.oplus_feature_game_accelerate_concert,
        "oplus.software.radio.meeting_accelerate" to R.string.oplus_feature_meeting_accelerate,
        "oplus.software.radio.data_concert_accelerate" to R.string.oplus_feature_data_concert_accelerate,
        "oplus.software.display.osie_aipq_support" to R.string.oplus_feature_aipq,
        "oplus.software.display.osie_aisdr2hdr_support" to R.string.oplus_feature_aisdr2hdr,
        "oplus.software.game.yuanshenvibration" to R.string.oplus_feature_genshin_vibration,
        "oplus.software.vibrator.screenshot_wave_support" to R.string.oplus_feature_screenshot_wave_vibration,
        "oplus.software.smart_loop_drag" to R.string.oplus_feature_smart_loop_drag,
        "oplus.software.screenon_before_special_action_open_camera" to R.string.oplus_feature_screenon_before_camera,
        "com.oplus.camera.under.water.support" to R.string.oplus_feature_underwater_camera,
        "oplus.software.fingerprint.ultrasonic_underwater_shutter" to R.string.oplus_feature_ultrasonic_underwater_shutter,
        "oplus.software.floatwindow_hide_alertwindow_notification" to R.string.oplus_feature_floatwindow_hide_alert_notification,
        "oplus.software.tvconnect.support" to R.string.oplus_feature_tv_connect,
        "oplus.software.tv_videocall.support" to R.string.oplus_feature_tv_videocall,
        "oplus.software.flexible_freeform.support" to R.string.oplus_feature_flexible_freeform,
        "oplus.software.audio.magicvoice_loopback_support" to R.string.oplus_feature_magicvoice_loopback,
        "oplus.software.display.game.support_vulkan" to R.string.oplus_feature_game_support_vulkan,
        "oplus.software.display.game_color_enhance_2.0" to R.string.oplus_feature_game_color_enhance_2,
        "oplus.software.display.game_dark_eyeprotect_support" to R.string.oplus_feature_game_dark_eyeprotect,
        "oplus.software.support.zoom.game_enter" to R.string.oplus_feature_zoom_game_enter,
        "oplus.software.support.zoom.center_exit" to R.string.oplus_feature_zoom_center_exit,
        "oplus.software.radio.comm_scene_switch_enable" to R.string.oplus_feature_radio_comm_scene_switch_enable,
        "oplus.software.radio.nwpower_linkpower" to R.string.oplus_feature_radio_nwpower_linkpower,
        "oplus.software.radio.sido_rat_enhance_nr_backoff_opt" to R.string.oplus_feature_radio_sido_rat_enhance_nr_backoff_opt,
        "oplus.software.radio.sido_rat_enhance_lte_backoff_opt" to R.string.oplus_feature_radio_sido_rat_enhance_lte_backoff_opt,
        "oplus.software.radio.sido_inter_cell_coop_dual_cell_strong_sig_opt" to R.string.oplus_feature_radio_sido_inter_cell_coop_dual_cell_strong_sig_opt,
        "oplus.software.radio.sido_inter_cell_coop_lte_rej_15_opt" to R.string.oplus_feature_radio_sido_inter_cell_coop_lte_rej_15_opt,
        "oplus.software.radio.sido_gw_lte_pingpong_lte_poor_signal_opt" to R.string.oplus_feature_radio_sido_gw_lte_pingpong_lte_poor_signal_opt,
        "oplus.software.radio.sido_nr_icon_pingpong_opt" to R.string.oplus_feature_radio_sido_nr_icon_pingpong_opt,
        "oplus.software.radio.enable_signal_map" to R.string.oplus_feature_radio_enable_signal_map,
        "oplus.software.radio.sido_lte_srv_poor_signal_ngbr_strong_opt" to R.string.oplus_feature_radio_sido_lte_srv_poor_signal_ngbr_strong_opt,
        "oplus.software.radio.sido_screen_on_no_service_opt" to R.string.oplus_feature_radio_sido_screen_on_no_service_opt,
        "oplus.software.radio.sido_dual_sim_coop_no_service_opt" to R.string.oplus_feature_radio_sido_dual_sim_coop_no_service_opt,
        "oplus.software.radio.sido_screen_on_no_45g_opt" to R.string.oplus_feature_radio_sido_screen_on_no_45g_opt,
        "oplus.software.radio.sido_dual_sim_coop_no4g_no5g_opt" to R.string.oplus_feature_radio_sido_dual_sim_coop_no4g_no5g_opt,
        "oplus.software.radio.game_ims_reg" to R.string.oplus_feature_radio_game_ims_reg,
        "oplus.software.radio.enable_weak_signal_backoff" to R.string.oplus_feature_radio_enable_weak_signal_backoff,
        "oplus.software.radio.signal_recovery_enabled" to R.string.oplus_feature_radio_signal_recovery_enabled,
        "oplus.software.radio.sim_detect_check_support" to R.string.oplus_feature_radio_sim_detect_check_support,
        "oplus.software.radio.apn_recovery" to R.string.oplus_feature_radio_apn_recovery,
        "oplus.software.radio.qoe_network_recovery" to R.string.oplus_feature_radio_qoe_network_recovery,
        "oplus.software.radio.smart5g_thermal_nr_deprio" to R.string.oplus_feature_radio_smart5g_thermal_nr_deprio,
        "oplus.software.radio.qct_smart_data_flow" to R.string.oplus_feature_radio_qct_smart_data_flow,
        "oplus.software.radio.sido_airplane_mode_low_power_opt" to R.string.oplus_feature_radio_sido_airplane_mode_low_power_opt,
        "oplus.software.radio.sido_sim_tag_opt" to R.string.oplus_feature_radio_sido_sim_tag_opt,
        "oplus.software.radio.sido_mmr_reg_req_stuck_opt" to R.string.oplus_feature_radio_sido_mmr_reg_req_stuck_opt,
        "oplus.software.radio.nsa_signal_optim" to R.string.oplus_feature_radio_nsa_signal_optim,
        "oplus.software.radio.disable_5g_nr_5gdump" to R.string.oplus_feature_radio_disable_5g_nr_5gdump,
        "oplus.software.radio.nr_nrca_rus_control" to R.string.oplus_feature_radio_nr_nrca_rus_control,
        "oplus.software.radio.nr_bwp_rus_control" to R.string.oplus_feature_radio_nr_bwp_rus_control,
        "oplus.software.radio.china_roaming" to R.string.oplus_feature_radio_china_roaming,
        "oplus.software.radio.sim_overdue_opt" to R.string.oplus_feature_radio_sim_overdue_opt,
        "oplus.software.radio.ul_priority_policy_one_vs_one" to R.string.oplus_feature_radio_ul_priority_policy_one_vs_one,
        "oplus.software.radio.cybersense_residence_enabled" to R.string.oplus_feature_radio_cybersense_residence_enabled,
        "oplus.software.radio.smart5g_Low_Power_control" to R.string.oplus_feature_radio_smart5g_Low_Power_control,
        "oplus.software.radio.sido_stamp_db" to R.string.oplus_feature_radio_sido_stamp_db,
        "oplus.software.radio.sido_ss_update_abnormal_opt" to R.string.oplus_feature_radio_sido_ss_update_abnormal_opt,
        "oplus.software.radio.tcp_syn_timeout_tuning" to R.string.oplus_feature_radio_tcp_syn_timeout_tuning,
        "oplus.software.radio.tcp_congest_control" to R.string.oplus_feature_radio_tcp_congest_control,
        "oplus.software.radio.enable_sido_rollback" to R.string.oplus_feature_radio_enable_sido_rollback,
        "oplus.software.radio.use_customized_thresholds" to R.string.oplus_feature_radio_use_customized_thresholds,
        "oplus.software.radio.sido_roaming_fast_search_opt" to R.string.oplus_feature_radio_sido_roaming_fast_search_opt,
        "oplus.software.radio.sido_fence" to R.string.oplus_feature_radio_sido_fence,
        "oplus.software.radio.sido_lte_fast_return_to_nr_opt" to R.string.oplus_feature_radio_sido_lte_fast_return_to_nr_opt,
        "oplus.software.radio.nwpower_power_recovery_switch_enable" to R.string.oplus_feature_radio_nwpower_power_recovery_switch_enable,
        "oplus.software.radio.nwpower_app_network_monitor_switch_enable" to R.string.oplus_feature_radio_nwpower_app_network_monitor_switch_enable,
        "oplus.software.radio.sido_lte_nr_pingpong_mitigation" to R.string.oplus_feature_radio_sido_lte_nr_pingpong_mitigation,
        "oplus.software.radio.nwpower_proc_net_control_switch_enable" to R.string.oplus_feature_radio_nwpower_proc_net_control_switch_enable,
        "oplus.software.radio.dual_ps" to R.string.oplus_feature_radio_dual_ps,
        "oplus.software.radio.find_phone" to R.string.oplus_feature_radio_find_phone,
        "oplus.software.radio.low_latency" to R.string.oplus_feature_radio_low_latency,
        "oplus.software.radio.low_latency_mode_de3" to R.string.oplus_feature_radio_low_latency_mode_de3,
        "oplus.software.radio.data_eval3" to R.string.oplus_feature_radio_data_eval3,
        "oplus.software.radio.data_eval4" to R.string.oplus_feature_radio_data_eval4,
        "oplus.software.radio.smart_data_eval" to R.string.oplus_feature_radio_smart_data_eval,
        "oplus.software.radio.stuck_analyse" to R.string.oplus_feature_radio_stuck_analyse,
        "oplus.software.radio.smart_commu" to R.string.oplus_feature_radio_smart_commu,
        "oplus.software.radio.pull_wifi_cell" to R.string.oplus_feature_radio_pull_wifi_cell,
        "oplus.software.radio.subway_slow_recovery" to R.string.oplus_feature_radio_subway_slow_recovery,
        "oplus.software.radio.virtualmodem_support" to R.string.oplus_feature_radio_virtualmodem_support,
        "oplus.software.radio.ignore_stk_display_text" to R.string.oplus_feature_radio_ignore_stk_display_text,
        "oplus.software.radio.air_interface_detect_support" to R.string.oplus_feature_radio_air_interface_detect_support,
        "oplus.software.radio.sido_airplane_mode_low_power_opt_v2" to R.string.oplus_feature_radio_sido_airplane_mode_low_power_opt_v2,
        "oplus.software.radio.sido_elevator" to R.string.oplus_feature_radio_sido_elevator,
        "oplus.software.radio.sido_intelligent_recovery" to R.string.oplus_feature_radio_sido_intelligent_recovery,
        "oplus.software.radio.nwpower_supprese_signal_report_enable" to R.string.oplus_feature_radio_nwpower_supprese_signal_report_enable,
        "oplus.software.radio.nwpower_amc" to R.string.oplus_feature_radio_nwpower_amc,
        "oplus.software.radio.nwpower_sys_net_control_switch_enable" to R.string.oplus_feature_radio_nwpower_sys_net_control_switch_enable,
        "oplus.software.radio.nwpower_amc_airplanemode" to R.string.oplus_feature_radio_nwpower_amc_airplanemode,
        "oplus.software.radio.nwpower_amc_oossearch" to R.string.oplus_feature_radio_nwpower_amc_oossearch,
        "oplus.software.radio.smart5g_speed_control" to R.string.oplus_feature_radio_smart5g_speed_control,
        "oplus.software.radio.nwpower_bg_app_net_control_switch_enable" to R.string.oplus_feature_radio_nwpower_bg_app_net_control_switch_enable,
        "oplus.software.pcconnect.support" to R.string.oplus_feature_pcconnect_support,
        "oplus.software.pctelephone.support" to R.string.oplus_feature_pctelephone_support,
        "oplus.software.radio.engnw_sts_interface_support" to R.string.oplus_feature_radio_engnw_sts_interface_support,
        "oplus.software.radio.support_r16" to R.string.oplus_feature_radio_support_r16,
        "oplus.software.radio.vonr_switch_enabled" to R.string.oplus_feature_radio_vonr_switch_enabled,
        "oplus.software.radio.nwpower_network_disable_white_list" to R.string.oplus_feature_radio_nwpower_network_disable_white_list,
        "oplus.software.radio.custom_ecclist_writable" to R.string.oplus_feature_radio_custom_ecclist_writable,
        "oplus.software.radio.disabled_cached_uid" to R.string.oplus_feature_radio_disabled_cached_uid,
        "oplus.software.radio.sa_call_control_disabled" to R.string.oplus_feature_radio_sa_call_control_disabled,
        "oplus.software.radio.vonr_city_control" to R.string.oplus_feature_radio_vonr_city_control,
        "oplus.software.radio.silent_redial_supported" to R.string.oplus_feature_radio_silent_redial_supported,
        "oplus.software.radio.dds_switch" to R.string.oplus_feature_radio_dds_switch,
        "oplus.software.radio.vowifi_city_control" to R.string.oplus_feature_radio_vowifi_city_control,
        "oplus.software.radio.update_dcdc_sleep_state" to R.string.oplus_feature_radio_update_dcdc_sleep_state,
        "oplus.software.radio.link_evaluation" to R.string.oplus_feature_radio_link_evaluation,
        "oplus.software.logkit.ocloud.support" to R.string.oplus_feature_logkit_ocloud_support,
        "oplus.software.radio.vendor_diag" to R.string.oplus_feature_radio_vendor_diag,
        "oplus.software.audio.voice_wakeup_support" to R.string.oplus_feature_audio_voice_wakeup_support,
        "oplus.software.audio.voice_wakeup_xbxb_support" to R.string.oplus_feature_audio_voice_wakeup_xbxb_support,
        "oplus.software.speechassist.oneshot.support" to R.string.oplus_feature_speechassist_oneshot_support,
        "oplus.software.radio.hyper_uplink_mode_enable" to R.string.oplus_feature_radio_hyper_uplink_mode_enable,
        "oplus.software.support_quick_launchapp" to R.string.oplus_feature_support_quick_launchapp,
        "oplus.software.radio.cybersense_residence_v2_enabled" to R.string.oplus_feature_radio_cybersense_residence_v2_enabled,
        "oplus.software.radio.multi_task_multi_link_schedule" to R.string.oplus_feature_radio_multi_task_multi_link_schedule,
        "oplus.software.support_blockable_animation" to R.string.oplus_feature_support_blockable_animation,
        "oplus.velocitytracker.strategy.support" to R.string.oplus_feature_velocitytracker_strategy_support,
        "os.charge.endurance.mode.support" to R.string.oplus_feature_charge_endurance_mode_support,
        "oplus.software.aon_distance_detection_enable" to R.string.oplus_feature_aon_distance_detection_enable,
        "oplus.software.aon_phone_camera_gesture_recognition" to R.string.oplus_feature_aon_phone_camera_gesture_recognition,
        "oplus.software.aon_phone_enable" to R.string.oplus_feature_aon_phone_enable,
        "oplus.software.aon_phone_mute" to R.string.oplus_feature_aon_phone_mute,
        "oplus.software.aon_pose_detection_enable" to R.string.oplus_feature_aon_pose_detection_enable,
        "oplus.hardware.aon_enable" to R.string.oplus_feature_hardware_aon_enable,
        "oplus.software.aon_enable" to R.string.oplus_feature_aon_enable,
        "oplus.software.radio.cybersense_travel_enabled" to R.string.oplus_feature_radio_cybersense_travel_enabled,
        "oplus.software.aon_pay_qrcode_enable" to R.string.oplus_feature_aon_pay_qrcode_enable,
        "oplus.software.aon_sensorhub_enable" to R.string.oplus_feature_aon_sensorhub_enable,
        "oplus.software.vibrator.kill_program_wave_support" to R.string.oplus_feature_vibrator_kill_program_wave_support,
        "oplus.software.preload.camera" to R.string.oplus_feature_preload_camera,
        "oplus.software.joystick.game_joystick_support" to R.string.oplus_feature_joystick_game_joystick_support,
        "oplus.software.radio.smart_nw_data_low_latency" to R.string.oplus_feature_radio_smart_nw_data_low_latency,
        "oplus.software.radio.nwpower_amc_mdwakeup_control" to R.string.oplus_feature_radio_nwpower_amc_mdwakeup_control,
        "oplus.software.radio.sched_ppq" to R.string.oplus_feature_radio_sched_ppq,
        "oplus.software.radio.nwpower_amc_lowmpss_recovery" to R.string.oplus_feature_radio_nwpower_amc_lowmpss_recovery,
        "oplus.software.radio.support_vonr_plus" to R.string.oplus_feature_radio_support_vonr_plus,
        "oplus.software.radio.ul_priority_policy_stream" to R.string.oplus_feature_radio_ul_priority_policy_stream,
        "oplus.software.radio.ul_priority_policy_game_optimize" to R.string.oplus_feature_radio_ul_priority_policy_game_optimize,
        "oplus.software.radio.power_save_dual_network_control" to R.string.oplus_feature_radio_power_save_dual_network_control,
        "oplus.software.radio.wifi_data_control" to R.string.oplus_feature_radio_wifi_data_control,
        "oplus.software.radio.wifi_force_rel_dual_data" to R.string.oplus_feature_radio_wifi_force_rel_dual_data,
        "oplus.hardware.display.low.blue.light" to R.string.oplus_feature_hardware_display_low_blue_light,
        "oplus.hardware.display.ultra_tr_diamond_screen" to R.string.oplus_feature_hardware_display_ultra_tr_diamond_screen,
        "oplus.software.radio.concert_detect_service" to R.string.oplus_feature_radio_concert_detect_service,
        "oplus.software.radio.qoe_game_cell_suggest" to R.string.oplus_feature_radio_qoe_game_cell_suggest,
        "oplus.hardware.display.no_classic_low_freq_strobe" to R.string.oplus_feature_hardware_display_no_classic_low_freq_strobe,
        "oplus.software.display.adfr_v32_lp" to R.string.oplus_feature_display_adfr_v32_lp,
        "oplus.software.radio.enable_ct_network_scan" to R.string.oplus_feature_radio_enable_ct_network_scan,
        "oplus.hardware.pwd_attestation_locksettings" to R.string.oplus_feature_hardware_pwd_attestation_locksettings,
        "oplus.software.radio.subsystem_camera_service" to R.string.oplus_feature_radio_subsystem_camera_service,
        "oplus.software.radio.engnw_vsim_support" to R.string.oplus_feature_radio_engnw_vsim_support,
        "oplus.software.radio.virtualmodem_cellinfo" to R.string.oplus_feature_radio_virtualmodem_cellinfo,
        "oplus.software.radio.car_sharing_network" to R.string.oplus_feature_radio_car_sharing_network,
        "oplus.software.radio.network_vip_task" to R.string.oplus_feature_radio_network_vip_task,
        "oplus.software.radio.enable_cmcc_nitz" to R.string.oplus_feature_radio_enable_cmcc_nitz,
        "oplus.software.wlan.qoe_estimate" to R.string.oplus_feature_wlan_qoe_estimate,
        "oplus.software.radio.smart5g_game_latency_check" to R.string.oplus_feature_radio_smart5g_game_latency_check,
        "oplus.software.radio.network_time_slice" to R.string.oplus_feature_radio_network_time_slice,
        "oplus.software.app_resolution_auto" to R.string.oplus_feature_app_resolution_auto,
        "oplus.software.camera_volume_quick_launch" to R.string.oplus_feature_camera_volume_quick_launch,
        "oplus.software.gsa_refresh" to R.string.oplus_feature_gsa_refresh,
        "oplus.software.radio.networkless_support" to R.string.oplus_feature_radio_networkless_support,
        "oplus.software.radio.pcdr_high_speed_move_fast_search_opt" to R.string.oplus_feature_radio_pcdr_high_speed_move,
        "oplus.software.radio.pcdr_lte_fast_return_to_nr_accurate_meas_recovery_opt" to R.string.oplus_feature_radio_pcdr_lte_fast_return_nr,
        "oplus.software.radio.pcdr_lte_intra_rat_ping_pong_opt" to R.string.oplus_feature_radio_pcdr_lte_ping_pong,
        "oplus.software.radio.pcdr_nr_intra_rat_ping_pong_opt" to R.string.oplus_feature_radio_pcdr_nr_ping_pong,
        "oplus.software.radio.pcdr_portrait" to R.string.oplus_feature_radio_pcdr_portrait,
        "oplus.software.radio.qoe_collaborative_center" to R.string.oplus_feature_radio_qoe_collaborative_center,
        "oplus.software.radio.rf_power_boost" to R.string.oplus_feature_radio_rf_power_boost,
        "oplus.software.radio.ul_priority_policy_foreground" to R.string.oplus_feature_radio_ul_priority_foreground,
        "oplus.software.touch_preload" to R.string.oplus_feature_touch_preload,
        "oplus.software.view.rgbnormalize" to R.string.oplus_feature_view_rgbnormalize,
        "oplus.software.view.spring_over_scroller" to R.string.oplus_feature_view_spring_over_scroller,
        "oplus.software.radio.show_5g_nr_mode" to R.string.oplus_feature_radio_show_5g_nr_mode,
        "oplus.software.display.eyeprotect_paper_texture_support" to R.string.oplus_feature_eyeprotect_paper_texture_support,
        "oplus.software.support.zoom.multi_mode" to R.string.oplus_feature_zoom_multi_mode,
        "oplus.software.display.dynamic_screen_bar_support" to R.string.oplus_feature_dynamic_screen_bar_support,
        "oplus.software.display.rhythm_eyeprotect_2.0_support" to R.string.oplus_feature_rhythm_eyeprotect_2_0_support,
        "oplus.software.audio.super_volume_5x" to R.string.oplus_feature_super_volume_5x
    )

    companion object {
        @Volatile private var INSTANCE: OplusFeatureMappings? = null

        fun getInstance(): OplusFeatureMappings {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: OplusFeatureMappings().also { INSTANCE = it }
            }
        }

        /**
         * 获取特性的描述，支持三级优先级查询
         * 优先级：CloudFeatureMappings > Oplus内置资源 > UserFeatureMappings
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
            val userDesc = userMappings.getDescription(featureName)
            if (!userDesc.isNullOrEmpty()) return userDesc

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