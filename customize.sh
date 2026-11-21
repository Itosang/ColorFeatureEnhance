SKIPMOUNT=false
PROPFILE=true
POSTFSDATA=true
LATESTARTSERVICE=true

MountExtensionDir="$MODPATH/Mount/my_product/etc/extension"
MountPermissionDir="$MODPATH/Mount/my_product/etc/permissions"

mkdir -p $MountExtensionDir
mkdir -p $MountPermissionDir

cp -rf /my_product/etc/extension/com.oplus.app-features.xml $MountExtensionDir
cp -rf /my_product/etc/extension/com.oplus.oplus-feature.xml $MountExtensionDir
cp -rf /my_product/etc/permissions/oplus.product.display_features.xml $MountPermissionDir

awk '
/<\/extend_features>/ {
  print "<!-- 隔膜触控 -->"
  print "\t<app_feature name=\"feature.super_settings_smart_touch.support\"/>"
  print "\t<!-- 跨屏镜像高分辨模式 -->"
  print "\t<app_feature name=\"com.oplus.padconnect.high_resolution\" args=\"boolean:true\"/>"
  print "\t<!-- 苹果手表互联 -->"
  print "\t<app_feature name=\"com.oplus.interconnect.aw.support\" args=\"int:1\"/>"
  print "\t<app_feature name=\"com.oplus.eyeprotect.ai_intelligent_eye_protect_support\"/>"
  print "\t<!-- 游戏助手辅助键(一键连招) -->"
  print "\t<app_feature name=\"feature.support.game.ASSIST_KEY\"/>"
  print "\t<!-- AI游戏辅助4.0 -->"
  print "\t<app_feature name=\"feature.support.game.AI_PLAY_version4_mlbb\"/>"
  print "\t<app_feature name=\"feature.support.game.AI_PLAY_version4\"/>"
  print "\t<app_feature name=\"feature.support.game.AI_PLAY_version4_audio\"/>"
  print "\t<!-- 直播助手 -->"
  print "\t<app_feature name=\"com.oplus.mediaturbo.game_live\" args=\"boolean:true\"/>"
  print "\t<!-- 侧边栏声音分轨助手 -->"
  print "\t<app_feature name=\"com.oplus.smartmediacontroller.lss_assistant_enable\" args=\"boolean:true\"/>"
  print "\t<!-- AI实况壁纸支持4k和hdr效果 -->"
  print "\t<app_feature name=\"com.oplus.wallpapers.ai_camera_movement\" args=\"boolean:true\"/>"
  print "\t<app_feature name=\"com.oplus.wallpapers.livephoto_wallpaper_support_4k\" args=\"boolean:true\"/>"
  print "\t<app_feature name=\"com.oplus.wallpapers.livephoto_wallpaper_support_hdr\" args=\"boolean:true\"/>"
  print "\t<!-- 全场景旁路供电 -->"
  print "\t<app_feature name=\"com.oplus.plc_charge.support\">"
  print "\t\t<StringList args=\"true\"/>"
  print "\t</app_feature>"
  print "\t<app_feature name=\"com.oplus.fullscene_plc_charge.support\" args=\"boolean:true\"/>"
  print "\t<app_feature name=\"com.oplus.reversecharge\" args=\"boolean:true\"/>"  
  print "\t<!-- 全天候息屏显示完整版 -->"
  print "\t<app_feature name=\"oplus.software.disable_aod_all_day_mode\" args=\"boolean:false\"/>"
  print "\t<app_feature name=\"com.oplus.systemui.panoramic_aod_all_day_default_open.enable\" args=\"boolean:true\"/>"
  print "\t<app_feature name=\"com.oplus.systemui.panoramic_aod_all_day.enable\" args=\"boolean:true\"/>"
  print "\t<app_feature name=\"oplus_keyguard_panoramic_aod_all_day_support\" args=\"boolean:true\"/>"
  print "\t<!-- 3D 壁纸 -->"
  print "\t<app_feature name=\"com.oplus.wallpapers.3d_wallpaper\" args=\"boolean:true\"/>"
  print "\t<app_feature name=\"com.oplus.wallpapers.ai_camera_movement_for_products_before_15\" args=\"boolean:true\"/>"
  print "\t<!--AI 妙绘 -->"
  print "\t<app_feature name=\"com.oplus.aipaint\" args=\"boolean:true\"/>"
  print "\t<app_feature name=\"com.oplus.aipaint.function_switch\" args=\"boolean:true\"/>"
  print "\t<app_feature name=\"com.oplus.setting.ai.action.SETTING_ENTRANCE\"/>"
  print "\t<app_feature name=\"com.android.settings.double_tap_power_button_quickapp\"/>"
  print "\t<!-- 游戏准星 -->"
  print "\t<app_feature name=\"com.oplus.games.shotting_assist\" args=\"boolean:true\"/>"
  print "\t<!-- AI一键成片 -->"
  print "\t<app_feature name=\"os.graphic.gallery.videoeditor.ai_instant_clip\" args=\"boolean:true\"/>"
  print "\t<!-- 开启外设键盘 -->"
  print "\t<app_feature name=\"oplus.software.settings_enable_physical_keyboard\" args=\"int:1\"/>"
  print "\t<!-- 投屏不限制帧率 -->"
  print "\t<app_feature name=\"com.oplus.cast.unlimit_refresh_rate\" args=\"int:1\"/>"
  print "\t<!-- ai图像助手 -->"
  print "\t<app_feature name=\"os.graphic.gallery.os15_secrecy\" args=\"boolean:true\"/>"
  print "\t<!-- 关于本机显示内核构建时间 -->"
  print "\t<app_feature name=\"com.android.settings.kernel_version_show_time\"/>"
}
{ print }
' $MountExtensionDir/com.oplus.app-features.xml > $MountExtensionDir/com.oplus.app-features.xml.tmp && mv $MountExtensionDir/com.oplus.app-features.xml.tmp $MountExtensionDir/com.oplus.app-features.xml

awk '
/<\/oplus-config>/ {
  print "<!-- 5G网络模式选择 -->"
  print "\t<oplus-feature name=\"oplus.software.radio.show_5g_nr_mode\"/>"
  print "\t<!-- 微信小程序支持多窗口/浮窗 -->"
  print "\t<oplus-feature name=\"oplus.software.support.zoom.open_wechat_mini_program\"/>"
  print "\t<!-- 双小窗特性 -->"
  print "\t<oplus-feature name=\"oplus.software.support.zoom.multi_mode\"/>"
  print "\t<!-- 纸质护眼 -->"
  print "\t<oplus-feature name=\"oplus.software.display.eyeprotect_paper_texture_support\"/>"
  print "\t<!-- 会议网络加速 -->"
  print "\t<oplus-feature name=\"oplus.software.radio.meeting_accelerate\"/>"
  print "\t<!-- 演唱会专网加速 -->"
  print "\t<oplus-feature name=\"oplus.software.radio.data_concert_accelerate\"/>"
  print "\t<!-- 护眼提醒 -->"
  print "\t<oplus-feature name=\"oplus.software.display.ai_eyeprotect_v1_support\"/>"
  print "\t<!-- 侧边栏视频助手 -->"
  print "\t<oplus-feature name=\"oplus.software.smart_sidebar_video_assistant\"/>"
  print "\t<!-- 降低白点值 -->"
  print "\t<oplus-feature name=\"oplus.software.display.reduce_white_point\"/>"
  print "\t<!-- 直播场景加速 -->"
  print "\t<oplus-feature name=\"oplus.software.radio.livebroadcast_accelerate\"/>"
  # print "\t<!-- 风驰游戏内核入口 -->"
  # print "\t<oplus-feature name=\"oplus.software.game.hummingbird\"/>"
  print "\t<!-- 支持应用钉在流体云 -->"
  print "\t<oplus-feature name=\"oplus.software.systemui.pin_task\"/>"
  print "\t<!-- 游戏助手切换网络模式 -->"
  print "\t<oplus-feature name=\"oplus.software.hide.game_3_chip\"/>"
  print "\t<oplus-feature name=\"oplus.software.wlan.game_chip\"/>"
  print "\t<!-- 晕车缓解 -->"
  print "\t<oplus-feature name=\"oplus.hardware.display.motion_sickness\"/>"
  # print "\t<!-- 山海通信网络引擎 -->"
  # print "\t<oplus-feature name=\"oplus.software.radio.ai_link_boost_notification\"/>"
  # print "\t<oplus-feature name=\"oplus.software.radio.ai_link_boost\"/>"
  print "\t<!-- 钉应用到流体云 -->"
  print "\t<oplus-feature name=\"oplus.software.systemui.pin_task\"/>"
  print "\t<!-- 与 iPhone 互联 -->"
  print "\t<oplus-feature name=\"oplus.software.radio.hfp_comm_shared_support\"/>"
  print "\t<oplus-feature name=\"oplus.software.game.hummingbird\"/>"
  print "\t<!-- 小布记忆 -->"
  # print "\t<oplus-feature name=\"oplus.software.directservice.finger_flashnotes_enable\"/>"
}
{ print }
' $MountExtensionDir/com.oplus.oplus-feature.xml > $MountExtensionDir/com.oplus.oplus-feature.xml.tmp && mv $MountExtensionDir/com.oplus.oplus-feature.xml.tmp $MountExtensionDir/com.oplus.oplus-feature.xml

set_perm_recursive $MODPATH 0 0 0777 0777

