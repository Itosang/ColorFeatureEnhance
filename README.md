# ColorOS Feature Enhance 可视化编辑器

一个用于 **可视化编辑与管理 ColorOS 特性开关**（如 `com.oplus.app-features.xml` 与 `com.oplus.oplus-feature.xml`）的开源工具，使用 **Kotlin + Jetpack Compose** 开发，遵循 **Material 3 Expressive** 设计规范。

> ⚠️ 本应用 **需要 Root 权限** 才能正常读写系统配置文件，请确保目标设备已 Root。

## App数据

- 1.**持久化本地数据**
- 2.**支持Magisk、KernelSu模块**

## 项目架构

```text
com.itosfish.colorfeatureenhance/
├── config/                    # 配置管理核心
│   └── ConfigMergeManager.kt  # 配置合并与补丁管理
├── data/
│   ├── model/                 # 数据模型（AppFeature、OplusFeature 等）
│   └── repository/            # XML 读写仓库实现
│       ├── XmlFeatureRepository.kt      # App-Features 仓库
│       └── XmlOplusFeatureRepository.kt # Oplus-Features 仓库
├── domain/
│   └── FeatureRepository.kt   # 领域层接口，抽象数据操作
├── ui/
│   ├── components/            # 可复用 UI 组件
│   │   ├── SearchBar.kt       # 搜索栏组件
│   │   ├── HighlightedText.kt # 高亮文本组件
│   │   └── TopAppBarComponent.kt # 顶部应用栏
│   ├── search/                # 搜索逻辑
│   │   └── SearchLogic.kt     # 搜索算法实现
│   ├── theme/                 # Material 3 主题定义
│   ├── FeatureConfigScreen.kt # 主配置界面
│   └── TextEditorActivity.kt  # 内置文本编辑器
├── utils/
│   ├── ConfigUtils.kt         # 配置文件工具类
│   ├── DialogUtil.kt          # 对话框工具
│   └── CSU.kt                 # Shell 命令工具
├── FeatureMode.kt             # 编辑模式枚举
└── MainActivity.kt            # 应用入口
```

## 技术架构

### 分层架构
| Layer | 关键职责 | 代表文件/目录 |
|-|--|-|
| **UI Layer** | Compose 渲染、用户交互、状态管理 | `ui/` `MainActivity.kt` |
| **Domain Layer** | 业务规则、接口定义 | `domain/FeatureRepository.kt` |
| **Data Layer** | 数据获取、XML 解析与持久化 | `data/` `config/` |
| **Utils Layer** | 工具类、系统集成 | `utils/ConfigUtils.kt` `utils/CSU.kt` |

### 核心设计原则
- **协程异步**：所有 I/O 操作均通过 `Dispatchers.IO` 调度，避免阻塞主线程
- **依赖倒置**：UI 层仅依赖接口，不直接依赖数据实现，便于测试与扩展
- **配置分离**：系统基线配置与用户补丁分离管理，支持 OTA 更新
- **模块化设计**：内置 Magisk 模块，实现系统级配置应用

### 配置管理流程
1. **系统配置复制**：从系统路径复制基线配置到应用目录 (`system_baseline/`)
2. **用户补丁生成**：根据用户修改生成增量补丁文件 (`user_patches/`)
3. **配置合并**：将基线配置与用户补丁合并生成最终配置 (`merged_output/`)
4. **模块同步**：将合并后的配置同步到 Magisk 模块目录
5. **权限设置**：自动设置模块目录权限为 644

### 新架构特点
- **基线+补丁模式**：系统配置与用户修改分离，支持 OTA 更新
- **智能合并**：自动检测配置变更，仅在必要时执行合并操作
- **多级回退**：支持系统基线 → 直接系统路径的多级配置源回退
- **日志追踪**：完整的配置管理操作日志，便于问题排查

## 编译与运行

### 环境要求
- **系统**: 支持任何基于 ColorOS 14/15 的系统，**需要 Root 权限**
- **Root 管理器**: 支持多种 Root 管理器，**不支持 overlayfs 挂载**

### 快速开始
1. **克隆仓库**
   ```bash
   git clone https://github.com/ItosFish/ColorFeatureEnhance.git
   cd ColorFeatureEnhance
   ```

2. **导入项目**
   - 使用 Android Studio 打开项目根目录
   - 等待 Gradle 同步完成

3. **准备设备**
   - 连接已 Root 的 Android 设备
   - 启用 USB 调试模式
   - 确保设备已安装 Magisk/KernelSU/APatch（**不支持 overlayfs 挂载**）

4. **安装运行**
   - 点击 Run ▶️ 按钮编译并安装应用
   - 首次运行时应用会自动安装配套的 Magisk 模块
   - 重启设备以激活模块功能

### 配置文件准备
应用支持多种配置文件来源：
- **自动获取**：应用会自动从系统路径复制配置文件
- **示例配置**：使用 `exampleConfig/` 中的示例文件快速体验
- **手动导入**：将配置文件放置到应用的外部存储目录

## 技术栈

### 核心技术
- **开发语言**: Kotlin 2.0.21 (100% Kotlin)
- **UI 框架**: Jetpack Compose + Material 3 Expressive
- **架构模式**: Repository Pattern + Domain Layer + 分层架构
- **序列化**: Kotlinx Serialization JSON
- **日志系统**: 统一 CLog 日志管理，支持内存存储和导出

### Android 组件
- **最低 SDK**: Android 14 (API 34)
- **目标 SDK**: Android 16 (API 36)
- **版本号**: v0.83 (Build 25072302)
- **Compose BOM**: 2025.06.01
- **Navigation**: Compose Navigation 2.9.1
- **生命周期**: Lifecycle Runtime KTX 2.9.1

### 系统集成
- **权限管理**: Root 权限 (通过 su 命令)
- **模块系统**: Magisk 模块集成，不支持原版 KernelSU
- **文件操作**: XML Pull 解析 + 文件 I/O
- **数据持久化**: SharedPreferences + JSON 配置文件
- **国际化**: 支持中文/英文双语界面

### 开发工具
- **构建工具**: Android Gradle Plugin 8.10.1
- **代码混淆**: ProGuard + R8 优化
- **多架构支持**: ARM64-v8a

## 使用说明

### 基本操作
1. **切换模式**：点击顶部标签在 App-Features 和 Oplus-Features 模式间切换
2. **搜索特性**：使用搜索栏快速定位特定功能
3. **编辑特性**：点击开关切换功能状态，长按可删除特性
4. **添加特性**：点击悬浮按钮添加新的功能配置
5. **文本编辑**：通过菜单进入文本编辑模式直接修改 XML

### 高级功能
- **配置同步**：修改会自动同步到 Magisk 模块
- **OTA 兼容**：系统更新后会自动重新合并配置
- **批量操作**：支持批量启用/禁用相关功能
- **配置备份**：用户修改以补丁形式保存，便于恢复

## 贡献指南

欢迎提交 Issue、Pull Request 或功能建议！

### 贡献要求
1. **代码规范**：遵循 Kotlin 官方代码风格
2. **注释完整**：新功能需要中英文注释
3. **向下兼容**：不破坏现有功能
4. **文档更新**：重要改动需同步更新文档

### 开发环境
- 使用最新版 Android Studio
- 确保有已 Root 的且搭载基于 ColorOS 14/15 的系统的设备用于测试
- 熟悉 Magisk 模块开发（如需修改模块部分）

## 致谢

- [Shizuku](https://github.com/RikkaApps/Shizuku) - 提供 关于弹窗 布局文件

## Activity
![Alt](https://repobeats.axiom.co/api/embed/5c0a87c2274bce91d5b9840f6a3c2bb791fb5ae9.svg "Repobeats analytics image")

## Star History

[![Star History Chart](https://api.star-history.com/svg?repos=ItosEO/ColorFeatureEnhance&type=Date)](https://www.star-history.com/#ItosEO/ColorFeatureEnhance&Date)
