# ColorOS 16 / 17 适配文档

## 一加 Ace 6T (PLR110 / SM8845) 特性清单

> 💡 **说明**：以下特性条目提取自一加 Ace 6T（代号 `macan`，搭载骁龙 8 Gen 5 / SM8845，系统版本 `PLR110_16.0.10.500(CN01)`）原厂 `/my_product/etc/extension/` 与 `/my_region/` 节点。  
> 提取目的是为后续 ColorOS 16/17 提供官方真实存在的特性键名参考；具体每个开关在不同系统版本中的实际生效效果（如双 WiFi、全场景旁路供电、AOD、AI 帮写等）仍需进一步实机测试与社区反馈验证。

### 1. 网络与无线连接
- **双 WiFi / DBS 加速**：
  - `oplus.software.wlan.dual_sta`（双 WiFi 网络加速）
  - `oplus.software.wlan.game_chip`（游戏专用 WiFi 芯片协同）
  - `oplus.software.wlan.dbs`（双频并发支持）
  - `oplus.software.wlan.assistant`（WLAN 智能助手）
- **5G / 蜂窝网络模式**：
  - `oplus.software.radio.show_5g_nr_mode`（显示 5G 网络模式切换）
  - `oplus.software.radio.smart5g_sa_enabled`（智能 5G SA 模式）

### 2. 音频与多媒体
- **超级音量**：
  - `oplus.software.audio.super_volume_4x`（超级音量 4.0）
  - `oplus.software.audio.super_volume_5x`（超级音量 5.0）
- **多媒体与转码**：
  - `com.oplus.mediaturbo.transcoding`（多媒体硬件转码加速）
  - `com.oplus.smartmediacontroller.lss_assistant_enable`（侧边栏声音分轨助手）
  - `com.oplus.mediaturbo.window_mode_notify`（小窗模式多媒体通知）

### 3. 电源与旁路供电
- **全场景旁路充电**：
  - `com.oplus.fullscene_plc_charge.support`（全场景旁路供电）
  - `com.oplus.games.show_bypass_charging_when_gameapps`（游戏内显示旁路供电开关）
  - `com.oplus.reversecharge`（反向充电开关）

### 4. 视觉与显示
- **全景息屏（AOD）**：
  - `com.oplus.systemui.panoramic_aod.enable`（全景息屏）
  - `os.personalization.fullscreen_aod.enable`（全天候全景息屏）
- **屏幕与画质**：
  - `com.android.settings.resolution_auto`（屏幕分辨率自适应）
  - `oplus.hardware.display.full_brightness_DC`（全亮度 DC 调光）
  - `com.oplus.wallpapers.3d_wallpaper`（3D 景深壁纸）

### 5. AI 与系统智慧能力
- **AI 帮写与智慧服务**：
  - `com.oplus.aiwriter.input_entrance_enabled`（AI 帮写输入法快捷入口）
  - `com.coloros.colordirectservice.cm_enable`（ColorOS 直达服务 / AI 助手）
  - `com.oplus.directservice.aitoolbox_enable`（AI 聊天场景工具箱）
  - `com.oplus.persona.card_datamining_support`（泛在服务卡片数据挖掘）
- **相册与影像增强**：
  - `os.graphic.gallery.collage.livephoto`（相册实况照片拼图支持）
  - `os.graphic.gallery.os15_secrecy`（AI 图像画质引擎）
  - `pre.open.camera.from.launcher`（桌面预热相机）

### 6. 游戏助手与性能
- **游戏增强**：
  - `com.coloros.gamesettings.support`（游戏助手专属设置面板）
  - `com.oplus.cast.support_game_mode`（无线投屏游戏模式）
  - `com.oplus.mediaturbo.game_live`（游戏直播助手加速）
  - `feature.support.game.AI_PLAY_version3`（游戏 AI 辅助 3.0）
  - `com.coloros.gamespace_game_support_fourd_vibration`（游戏 4D 震动）
