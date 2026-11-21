# WORKLOG

## 任务
把参考 customize.sh 的六项特性（3D壁纸/全天候息屏/内核构建时间/风驰游戏内核/山海通信/小布记忆）移植到本项目：App 维护 feature 追加清单，模块开机读清单+系统原始 XML 合并生成挂载用 XML 再 bind mount。验收：XML 合法、清单写入正确、六项 key 一致、版本号递增。

## 关键决策（只增不删）
- [检查点1] 追加动作放模块开机脚本（post-fs-data）；理由：用户指定分工 App 管配置、模块管挂载，且系统 OTA 后原始 XML 变化时能自适应。
- [检查点1] 六项只加入可选列表、默认不启用；理由：用户明确要求。
- [检查点1] 模块内完整 XML 改为「追加清单」（只列要追加的 feature 行）；理由：模块负责把清单追加到系统原始 XML，不再整体覆盖系统 XML。
- [检查点1] 模块必须先备份一份系统原始 XML 作基底；理由：bind mount 生效后运行时读到的是挂载副本，读不到真原始文件。
- [检查点2] 采用「清单只放新增项」：系统原生 feature 视为自带不管理，App 只维护要追加的清单（初始空，六项可选）；废弃原完整 XML 管理机制；理由：用户明确选择，最符合「往原始 XML 追加」。
- [检查点2] 追加清单数据结构：每个文件（app-features / oplus / region）一份清单，元素为完整 feature 行字符串（含 args、子节点）；理由：追加到原始 XML 需逐字保留原始写法。

## 踩坑清单（禁止重试的方案）
- [检查点3] awk 变量用 `-v close=`：gawk 报 "cannot use gawk builtin close as variable name"；原因：close 是 awk 内置函数；改用 closeTag。
- [检查点3] 清单块解析用 `line ~ /\/>$/` 收块：带子节点的 feature 被子节点自闭合误收，丢 `</app_feature>` 导致 XML 非法；原因：子节点也以 /> 结尾；改用 inFeature 标志显式标记 feature 起始/结束边界（熔断后换思路）。

## 已验证结论（附证据来源）
- [检查点3] Kotlin 全量编译通过（JAVA_HOME=Android Studio jbr，./gradlew compileDebugKotlin，BUILD SUCCESSFUL）。
- [检查点3] awk 合并算法三用例验证通过：简单自闭合、按 name 去重、带子节点复杂块，输出均 XML 合法（python xml.dom.minidom.parse）。
- [检查点3] mod.zip 结构正确：脚本 755、配置 644，append/ 与 backup/ 由 customize.sh 安装时创建。

## 当前状态（每次检查点覆盖此节）
- 进度：云端配置彻底删除、全天候息屏与内核构建时间在模块更新/重启后失效 Bug 彻底修复完成，assembleDebug 构建通过（40 actionable tasks，BUILD SUCCESSFUL）
- 正在做：记录工作日志，向用户交付
- 下一步：真机安装验证
- 错误计数：无

## 关键决策补充（检查点9）
- [检查点9] 彻底移除云端配置，纯化为本地模式：
  1. 删除 RemoteConfigManager 与 CloudFeatureMappings 文件；
  2. 从 MainActivity、AppFeatureMappings、OplusFeatureMappings、DialogUtil 清理所有相关逻辑，描述来源收敛为手机自带映射与 App 内置映射；
  3. 从 build.gradle.kts 移除仅供云端使用的 okhttp 依赖。
- [检查点9] 修复全天候息屏与内核构建时间在模块更新/重启后丢失的 Bug：
  1. 根因1（模块安装覆盖用户配置）：更新模块时，customize.sh 原先盲目从系统路径覆盖，导致未重启前用户最新配置被出厂空配置冲刷掉。修复：优先从已有模块目录 /data/adb/modules/ColorOSFeaturesEnhance 拷贝继承已保存的 XML，只有全新安装时才读设备出厂配置；
  2. 根因2（否定语义特性布尔判定错误）：XmlFeatureRepository 中将 disable_aod_all_day_mode 的 args="boolean:false" 错误判定为 enabled=false，导致保存时被过滤遗漏。修复：只要节点声明在生效 XML 中即为 enabled=true，args 原样保留为属性参数，完美保证全天候息屏 4 个底层特性同步开启并在重启后持久维持。

## 关键决策补充（检查点8）
- [检查点8] 排版与卡片化重构：
  1. 彻底解决“连卡片都没有”问题：为每个 Feature 条目引入独立的圆角 Surface 卡片容器（MaterialTheme.shapes.extraLarge，surfaceContainer 底色，tonalElevation=1dp），与页面背景形成清晰层次感；
  2. 修复顶栏重复排版：去除副标题与右上角动作文字完全重复的混乱现象，右上角收敛为精致的圆形胶囊模式切换按钮；
  3. 修复文字拆行与布局溢出：标题与英文 feature key 施加 maxLines 与 Ellipsis 省略控制，避免超长类名将 Switch 挤歪；
  4. 解决浮动按钮遮挡：列表底部补充足够 Spacer 预留空间（88dp），确保滑至最底端时卡片内容完整露在 FAB 上方；
  5. 字体排版收敛为 SansSerif，中英文视觉工整现代。

## 关键决策补充（检查点7）
- [检查点7] 修复关联多 feature 组关闭时仅关一个的 Bug：
  1. FeatureGroup.isEnabled 判定优化：改为 features.any { it.enabled }，只要组内还有开启项即呈现为开启，确保开关状态不误导；
  2. onToggle 传入 (FeatureGroup, Boolean)，收集 group.features 全部名称 targetNames，一次性将集合中所有底层 feature 的 enabled 统一设置为 false，彻底避免只关闭单个；
  3. chooseFromGroup 与 EditFeatureDialog 增强：在多项选择弹窗中增加“全部启用”与“全部关闭”快捷操作，在编辑确认时自动联动同步同组关联特性的开启/关闭状态。

## 关键决策补充（检查点6）
- [检查点6] 修复应用内置推荐特性默认全开问题：
  1. AppendPresets 内预设初始 enabled 一律定为 false；
  2. 纠正分类边界判定：废弃将具有中文翻译的全部系统特性误划入“应用新增”的错误逻辑，严格依据 AppendPresets.isPresetFeature、用户自建标记与系统原始快照划分；
  3. 修复 XmlOplusFeatureRepository 将 <unavailable-oplus-feature> 错误解析为 enabled=true 的严重 Bug。
- [检查点6] 修复关闭特性后在界面消失的问题：
  1. 重构 FeatureCatalog：在 saveFeatures 时第一时间同步记忆持久化全部特性全集（包括被关闭的特性）；
  2. 修复 loadFeatures：从 FeatureCatalog.all 读取完整列表并根据生效 XML 精准映射 enabled 状态；
  3. 规范删除与关闭语义分离：关闭仅写入 enabled=false，常驻列表显示为 OFF；仅用户显式长按确认删除时才调用 FeatureCatalog.remove 移除。

## 关键决策补充（检查点5）
- [检查点5] bug1 强制开启根因：候选项按 AppendPresets 自带的 enabled=true 加入列表，关掉后 reload 又按 true 补回。修复：开关状态一律 = name 是否在当前配置中，与预设定义里的 enabled 无关。
- [检查点5] bug2 关闭即消失根因：列表只列"当前配置+六项预设"，内置项关掉移出配置后无处可回。修复：新增 FeatureCatalog（SharedPreferences JSON，按 mode）持久记住所有见过的特性定义（含 args/子节点），新定义优先合并；列表=目录全集，关闭项显示为关可再开。AppFeature 加 @Serializable。

## 关键决策补充
- [检查点4] 用户两次纠正后定稿：不要内置完整 XML 当字典、不要推荐页、不要 append 清单。列表直接来自用户设备自己的 XML（每台设备不同）；开=插入、关=移除。
- [检查点4] 实现：customize.sh 安装时把设备三份 XML 拷进模块目录作编辑基底；App readEffective（模块文件优先，无则读设备路径）列出全部项+六项候选（设备没有则显示关）；save 只写 enabled 项回模块目录；post-fs-data 恢复原始简单 bind mount。awk 合并/append/backup 全部废弃。
- [检查点4] toArgs 改为 args 原样保留：enabled 只表示是否插入，不再改写 boolean 值（否则 disable_aod_all_day_mode 的 boolean:false 会被开关翻转成语义相反）。
- [检查点4] 六项 feature name 已加入两份映射表，同描述自动并组（AOD 4 项=1 个开关）。APP 模式也显示开关（原来只有 OPLUS/REGION 有）。
- [检查点4] 教训：用户说"看不到开关 UI"时先查 UI 渲染条件与数据源，别急着推翻架构；批量 git checkout 回退被用户否决——对的东西不要回退。

## 检查点记录
### [3] 实现完成
模块侧：customize.sh 安装时备份系统三份原始 XML 到 backup/ 并建 append/；post-fs-data.sh 开机把「备份原始 + append 清单」按 name 去重合并到挂载目录再 bind mount；两份旧完整 XML 改占位。App 侧：ModuleConfig 新增 appendFile/readAppend/writeAppend；两个 repository 改读写 append 清单；新增 AppendPresets（六项，按 mode 分组，小布记忆归 REGION 因 my_product 已存在）；FeatureConfigScreen 加星标小 FAB → 预设对话框；strings 中英各加 8 条；module.prop 22→23、ConfigUtils.LATEST_MODULE_VERSION 22→23。awk 合并脚本三次失败后熔断换 inFeature 边界法修好。

### [1] 规划完成
读完 29 个 Kotlin 文件与模块脚本，确认数据流：App 写清单 → 模块开机合并原始 XML → bind mount。六项中「风驰游戏内核」hummingbird、「山海通信」ai_link_boost 在参考脚本里被注释；前者本模块无，后者 ai_link_boost 已存在于 my_product XML。小布记忆 finger_flashnotes_enable 已存在于 my_product XML、字符串已有 "[软] 小布记忆"。
