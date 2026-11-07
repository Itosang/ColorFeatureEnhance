# ColorOS Feature Enhance

一个用于可视化编辑 ColorOS 特性开关（`com.oplus.app-features.xml`、`com.oplus.oplus-feature.xml`）的开源工具，基于 Kotlin + Jetpack Compose 开发，遵循 Material 3 Expressive 设计规范。

> ⚠️ 需要 Root 权限，不支持 overlayfs 挂载。

## 功能特点

- 可视化编辑特性开关，支持搜索、添加、删除、批量操作
- 内置文本编辑器，可直接修改 XML
- 基线 + 补丁配置管理，支持 OTA 更新后自动重新合并
- 修改自动同步到 Magisk / KernelSU 模块
- 本地数据持久化，支持中文 / 英文界面

## 项目结构

```text
com.itosfish.colorfeatureenhance/
├── config/          # 配置合并与补丁管理
├── data/            # 数据模型与 XML 读写仓库
├── domain/          # 领域层接口
├── ui/              # Compose 界面、组件、搜索、主题
├── utils/           # 配置工具、对话框、Shell 命令
├── FeatureMode.kt   # 编辑模式枚举
└── MainActivity.kt  # 应用入口
```

分层架构：UI → Domain → Data / Utils，所有 I/O 通过 `Dispatchers.IO` 异步执行，UI 仅依赖接口。

## 配置管理流程

1. 复制系统配置到应用目录（`system_baseline/`）
2. 根据用户修改生成增量补丁（`user_patches/`）
3. 合并基线与补丁生成最终配置（`merged_output/`）
4. 同步到模块目录并设置权限 644

## 编译与运行

**环境要求**：ColorOS 14/15 设备，已 Root，安装 Magisk / KernelSU / APatch。

```bash
git clone https://github.com/ItosFish/ColorFeatureEnhance.git
cd ColorFeatureEnhance
```

用 Android Studio 打开项目，连接已 Root 并启用 USB 调试的设备，点击 Run 编译安装。首次运行会自动安装配套模块，重启设备后生效。

配置文件来源：自动从系统路径复制、使用 `exampleConfig/` 示例文件，或手动放入外部存储目录。

## 技术栈

- Kotlin 2.0.21，Jetpack Compose + Material 3 Expressive
- 架构：Repository Pattern + 分层架构
- 序列化：Kotlinx Serialization；日志：CLog
- 最低 SDK 34，目标 SDK 36
- 构建：AGP 8.10.1，ProGuard + R8，ARM64-v8a

## 使用说明

- 顶部标签切换 App-Features / Oplus-Features 模式
- 搜索栏定位特性，点击开关切换状态，长按删除
- 悬浮按钮添加特性，菜单进入文本编辑模式

## 贡献

欢迎提交 Issue 或 PR。要求：遵循 Kotlin 官方代码风格、新功能附中英文注释、不破坏现有功能、重要改动同步更新文档。测试需使用已 Root 的 ColorOS 14/15 设备。

## 致谢

- [Shizuku](https://github.com/RikkaApps/Shizuku) - 关于弹窗布局文件

## Star History

[![Star History Chart](https://api.star-history.com/svg?repos=ItosEO/ColorFeatureEnhance&type=Date)](https://www.star-history.com/#ItosEO/ColorFeatureEnhance&Date)
