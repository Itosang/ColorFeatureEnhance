package com.itosfish.colorfeatureenhance.ui

import android.util.Log
import androidx.activity.compose.BackHandler

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.neverEqualPolicy
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer

import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.itosfish.colorfeatureenhance.FeatureMode
import com.itosfish.colorfeatureenhance.MainActivity.Companion.app
import com.itosfish.colorfeatureenhance.R
import com.itosfish.colorfeatureenhance.config.ConfigMergeManager
import com.itosfish.colorfeatureenhance.data.model.AppFeature
import com.itosfish.colorfeatureenhance.data.model.AppFeatureMappings
import com.itosfish.colorfeatureenhance.data.model.FeatureGroup
import com.itosfish.colorfeatureenhance.data.model.OplusFeatureMappings
import com.itosfish.colorfeatureenhance.data.repository.XmlFeatureRepository
import com.itosfish.colorfeatureenhance.domain.FeatureRepository
import com.itosfish.colorfeatureenhance.ui.components.ColorOSTopAppBar
import com.itosfish.colorfeatureenhance.ui.components.HighlightedText
import com.itosfish.colorfeatureenhance.ui.components.SearchBar
import com.itosfish.colorfeatureenhance.ui.search.SearchLogic
import com.itosfish.colorfeatureenhance.ui.theme.PatchColors
import com.itosfish.colorfeatureenhance.utils.AddFeatureDialog
import com.itosfish.colorfeatureenhance.utils.EditFeatureDialog
import kotlinx.coroutines.launch

private enum class FeatureSection { SYSTEM, APP_ADDED }

private sealed interface FeatureListEntry {
    data class Header(val section: FeatureSection, val count: Int) : FeatureListEntry
    data class Group(val group: FeatureGroup) : FeatureListEntry
}

@Composable
fun FeatureConfigScreen(
    configPath: String,
    currentMode: FeatureMode,
    onModeChange: (FeatureMode) -> Unit,
    repository: FeatureRepository
) {
    val scope = rememberCoroutineScope()
    var features by remember { mutableStateOf<List<AppFeature>>(emptyList(), neverEqualPolicy()) }
    var refreshTrigger by remember { mutableStateOf(0) }
    var patchActions by remember { mutableStateOf<Map<String, ConfigMergeManager.PatchAction>>(emptyMap()) }
    val featureGroups by remember(features, currentMode, refreshTrigger) {
        derivedStateOf {
            // 按描述文本分组
            features.groupBy { feature -> 
                // 使用描述作为分组键
                val description = if (currentMode == FeatureMode.APP) {
                    AppFeatureMappings.getLocalizedDescription(app, feature.name)
                } else {
                    OplusFeatureMappings.getLocalizedDescription(app, feature.name)
                }
                // 对于未知特性（没有预设映射且没有用户自定义映射），使用name作为分组键的一部分
                // 以避免不同的未知特性被错误地合并
                val resUnknown = if (currentMode == FeatureMode.APP) {
                    AppFeatureMappings.getInstance().getResId(feature.name)
                } else {
                    OplusFeatureMappings.getInstance().getResId(feature.name)
                }
                if (resUnknown == R.string.feature_unknown && 
                    description == feature.name) {
                    "unknown_${feature.name}" // 未知特性，使用name区分
                } else {
                    "desc_${description}" // 已知特性，仅使用描述分组
                }
            }.map { (_, groupFeatures) ->
                val nameResId = if (currentMode == FeatureMode.APP) {
                    AppFeatureMappings.getInstance().getResId(groupFeatures.first().name)
                } else {
                    OplusFeatureMappings.getInstance().getResId(groupFeatures.first().name)
                }
                FeatureGroup(nameResId, groupFeatures)
            }.sortedWith(
                compareBy<FeatureGroup> { 
                    // 首先按是否为未知特性排序
                    it.nameResId != R.string.feature_unknown 
                }.thenBy { 
                    // 然后按描述文本排序
                    if (currentMode == FeatureMode.APP) {
                        AppFeatureMappings.getLocalizedDescription(app, it.features.first().name)
                    } else {
                        OplusFeatureMappings.getLocalizedDescription(app, it.features.first().name)
                    }
                }
            )
        }
    }
    
    val context = LocalContext.current

    var groupToDelete by remember { mutableStateOf<FeatureGroup?>(null) }
    var featureToEdit by remember { mutableStateOf<Pair<AppFeature, String>?>(null) } // feature and description
    var chooseFromGroup by remember { mutableStateOf<FeatureGroup?>(null) }
    var showAddDialog by remember { mutableStateOf(false) }

    
    // 复杂特性对话框状态
    var showComplexFeatureDialog by remember { mutableStateOf(false) }
    var complexFeatureConfigPath by remember { mutableStateOf("") }
    
    // 搜索相关状态
    var isSearchActive by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    // 分区展开状态，默认收起
    var isSystemSectionExpanded by remember { mutableStateOf(false) }
    var isAppSectionExpanded by remember { mutableStateOf(false) }
    
    // 当搜索栏展开时拦截系统返回键，先关闭搜索栏而非退出界面
    BackHandler(enabled = isSearchActive) {
        isSearchActive = false
        searchQuery = ""
    }

    // 根据搜索查询过滤特性组
    val displayedFeatureGroups = remember(featureGroups, searchQuery, currentMode) {
        if (searchQuery.isBlank()) {
            featureGroups
        } else {
            SearchLogic.filterFeatureGroups(featureGroups, searchQuery, context, currentMode)
        }
    }

    val isSearching = searchQuery.isNotBlank()

    // “应用新增”包含用户手动添加的特性，以及 App 内置映射表中的特性
    val appAddedFeatureNames = remember(features, patchActions, currentMode) {
        val userAddedNames = patchActions
            .filterValues { it == ConfigMergeManager.PatchAction.ADD }
            .keys
        val builtInNames = features
            .map { it.name }
            .filterTo(mutableSetOf()) { name ->
                if (currentMode == FeatureMode.APP) {
                    AppFeatureMappings.getInstance().isBuiltInFeature(name)
                } else {
                    OplusFeatureMappings.getInstance().isBuiltInFeature(name)
                }
            }
        userAddedNames + builtInNames
    }

    val (appAddedGroups, systemGroups) = remember(displayedFeatureGroups, appAddedFeatureNames) {
        displayedFeatureGroups.partition { group ->
            group.features.all { it.name in appAddedFeatureNames }
        }
    }

    fun featureGroupKey(group: FeatureGroup): String {
        val firstFeature = group.features.first()
        val description = if (currentMode == FeatureMode.APP) {
            AppFeatureMappings.getLocalizedDescription(context, firstFeature.name)
        } else {
            OplusFeatureMappings.getLocalizedDescription(context, firstFeature.name)
        }

        return if (group.nameResId == R.string.feature_unknown && description == firstFeature.name) {
            "unknown_${firstFeature.name}_$refreshTrigger"
        } else {
            "desc_${description}_$refreshTrigger"
        }
    }

    val listEntries: List<FeatureListEntry> = remember(
        systemGroups,
        appAddedGroups,
        isSystemSectionExpanded,
        isAppSectionExpanded,
        isSearching
    ) {
        buildList {
            if (systemGroups.isEmpty() && appAddedGroups.isEmpty()) return@buildList

            add(FeatureListEntry.Header(FeatureSection.SYSTEM, systemGroups.size))
            if (isSystemSectionExpanded || isSearching) {
                systemGroups.forEach { add(FeatureListEntry.Group(it)) }
            }

            add(FeatureListEntry.Header(FeatureSection.APP_ADDED, appAddedGroups.size))
            if (isAppSectionExpanded || isSearching) {
                appAddedGroups.forEach { add(FeatureListEntry.Group(it)) }
            }
        }
    }



    // 首次进入或配置路径变化时，自动执行一次合并再加载，避免出现空列表
    LaunchedEffect(configPath) {
        // 先确保系统配置已复制并完成合并
        ConfigMergeManager.performConfigMerge()
        // 再加载特性
        features = repository.loadFeatures(configPath)

        // 加载补丁状态
        if (features.isNotEmpty()) {
            val featureNames = features.map { it.name }
            patchActions = ConfigMergeManager.getFeaturesPatchActions(
                featureNames,
                currentMode == FeatureMode.APP
            )
        }
    }

    Scaffold(
        topBar = {
            Column {
                ColorOSTopAppBar(
                    title = stringResource(id = R.string.app_title),
                    currentMode = currentMode,
                    onModeChange = onModeChange,
                    isSearchActive = isSearchActive,
                    onSearchClick = {
                        if (isSearchActive) {
                            searchQuery = ""
                        }
                        isSearchActive = !isSearchActive
                    },
                    onRefresh = {
                        scope.launch {
                            // 重新合并配置以确保最新复制的系统配置被处理
                            ConfigMergeManager.performConfigMerge()
                            // 重新加载特性列表
                            val updated = repository.loadFeatures(configPath)
                            // 强制触发重组，更新UI
                            features = emptyList()
                            features = updated
                            refreshTrigger++
                        }
                    }
                )

                // 搜索栏
                SearchBar(
                    query = searchQuery,
                    onQueryChange = { searchQuery = it },
                    onClearQuery = { searchQuery = "" },
                    isVisible = isSearchActive
                )
            }
        },
        // 添加按钮浮动（常驻显示）
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = stringResource(id = R.string.add_feature)
                )
            }
        },
        contentWindowInsets = WindowInsets.statusBars
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.padding(innerPadding),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp)
        ) {
            items(listEntries, key = { entry ->
                when (entry) {
                    is FeatureListEntry.Header -> "section_${entry.section.name}"
                    is FeatureListEntry.Group -> featureGroupKey(entry.group)
                }
            }) { entry ->
                when (entry) {
                    is FeatureListEntry.Header -> FeatureSectionHeader(
                        title = stringResource(
                            id = when (entry.section) {
                                FeatureSection.SYSTEM -> R.string.section_system_features
                                FeatureSection.APP_ADDED -> R.string.section_app_added_features
                            }
                        ),
                        count = entry.count,
                        isExpanded = when (entry.section) {
                            FeatureSection.SYSTEM -> isSystemSectionExpanded || isSearching
                            FeatureSection.APP_ADDED -> isAppSectionExpanded || isSearching
                        },
                        onToggle = {
                            when (entry.section) {
                                FeatureSection.SYSTEM -> isSystemSectionExpanded = !isSystemSectionExpanded
                                FeatureSection.APP_ADDED -> isAppSectionExpanded = !isAppSectionExpanded
                            }
                        },
                        modifier = Modifier.animateItem()
                    )

                    is FeatureListEntry.Group -> {
                        val group = entry.group
                        FeatureGroupItem(
                            currentMode = currentMode,
                            group = group,
                            searchQuery = searchQuery,
                            patchAction = patchActions[group.features.first().name],
                            onToggle = { updatedGroup ->
                                // 更新组内所有特性的状态
                                val updatedFeatures = features.map { feature ->
                                    val shouldUpdate = updatedGroup.features.any { it.name == feature.name }
                                    if (shouldUpdate) feature.copy(enabled = updatedGroup.isEnabled) else feature
                                }
                                // 强制触发重组，确保UI立即更新
                                features = emptyList()
                                features = updatedFeatures

                                // 异步保存并重新加载
                                scope.launch {
                                    repository.saveFeatures(configPath, updatedFeatures)

                                    // 重新加载配置文件以获取最新的合并结果
                                    val reloadedFeatures = repository.loadFeatures(configPath)
                                    features = reloadedFeatures

                                    // 更新补丁状态
                                    if (reloadedFeatures.isNotEmpty()) {
                                        val featureNames = reloadedFeatures.map { it.name }
                                        patchActions = ConfigMergeManager.getFeaturesPatchActions(
                                            featureNames,
                                            currentMode == FeatureMode.APP
                                        )
                                    }

                                    // 触发UI刷新
                                    refreshTrigger++
                                }
                            },
                            onLongPress = {
                                // 仅允许删除用户新增的特性
                                val firstName = group.features.first().name
                                val action = patchActions[firstName]
                                Log.d("FeatureDelete", "LongPress: name=$firstName, mode=$currentMode, action=$action")

                                if (currentMode == FeatureMode.OPLUS) {
                                    // OPLUS 模式下仍维持原有逻辑
                                    if (action == ConfigMergeManager.PatchAction.ADD) {
                                        groupToDelete = group
                                    }
                                } else {
                                    // APP 模式
                                    when (action) {
                                        ConfigMergeManager.PatchAction.REMOVE -> {
                                            // 已删除状态 -> 恢复（移除补丁）
                                            scope.launch {
                                                // 1. 读取系统基线，找到原始特性
                                                val configPaths = com.itosfish.colorfeatureenhance.utils.ConfigUtils.getConfigPaths()
                                                val baselineFile = java.io.File(configPaths.systemBaselineDir, configPaths.appFeaturesFile)
                                                val baselineList = if (baselineFile.exists()) {
                                                    XmlFeatureRepository().loadFeatures(baselineFile.absolutePath)
                                                } else emptyList()

                                                // 2. 生成恢复后的列表
                                                val restoredFeatures = features.map { ft ->
                                                    if (group.features.any { it.name == ft.name }) {
                                                        baselineList.find { it.name == ft.name } ?: ft.copy(enabled = true)
                                                    } else ft
                                                }

                                                // 3. 更新 UI
                                                features = emptyList()
                                                features = restoredFeatures

                                                // 4. 保存补丁并重新合并
                                                repository.saveFeatures(configPath, restoredFeatures)

                                                // 5. 重新加载最终配置
                                                val reloaded = repository.loadFeatures(configPath)
                                                features = reloaded

                                                if (reloaded.isNotEmpty()) {
                                                    val names = reloaded.map { it.name }
                                                    patchActions = ConfigMergeManager.getFeaturesPatchActions(
                                                        names,
                                                        true
                                                    )
                                                }
                                                refreshTrigger++
                                            }
                                        }
                                        else -> {
                                            // 其余情况仍弹出删除确认
                                            groupToDelete = group
                                        }
                                    }
                                }
                            },
                            onClick = {
                                if (group.features.size == 1) {
                                    // 检查是否为复杂特性
                                    val feature = group.features.first()
                                    featureToEdit = feature to if (currentMode == FeatureMode.APP) {
                                        AppFeatureMappings.getLocalizedDescription(context, feature.name)
                                    } else {
                                        OplusFeatureMappings.getLocalizedDescription(context, feature.name)
                                    }
                                } else {
                                    chooseFromGroup = group
                                }
                            },
                            modifier = Modifier.animateItem()
                        )
                    }
                }
            }
            // 显示空结果提示
            if (displayedFeatureGroups.isEmpty()) {
                item {
                    Text(
                        text = if (searchQuery.isNotBlank()) 
                            stringResource(R.string.search_no_results) 
                        else 
                            "暂无特性配置",
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
            
            // 添加底部Spacer，确保内容可以滚动到导航栏后面
            item {
                Spacer(
                    Modifier.windowInsetsBottomHeight(WindowInsets.navigationBars)
                )
            }
        }

        // 添加特性对话框
        if (showAddDialog) {
            AddFeatureDialog(
                onDismiss = { showAddDialog = false },
                context = context,
                currentMode = currentMode,
                onConfirm = { name, description, enabled, args ->
                    // 添加新特性
                    val newFeature = AppFeature(name, enabled, args)
                    val newList = features + newFeature
                    // 强制触发重组，确保UI立即更新
                    features = emptyList()
                    features = newList

                    // 新增特性归入“应用新增”分区，展开以便立即看到
                    isAppSectionExpanded = true

                    // 保存到文件并重新加载
                    scope.launch {
                        repository.saveFeatures(configPath, newList)

                        // 重新加载配置文件以获取最新的合并结果
                        val reloadedFeatures = repository.loadFeatures(configPath)
                        features = reloadedFeatures

                        // 更新补丁状态
                        if (reloadedFeatures.isNotEmpty()) {
                            val featureNames = reloadedFeatures.map { it.name }
                            patchActions = ConfigMergeManager.getFeaturesPatchActions(
                                featureNames,
                                currentMode == FeatureMode.APP
                            )
                        }

                        // 触发UI刷新
                        refreshTrigger++
                    }

                    showAddDialog = false
                }
            )
        }

        // 删除确认对话框
        groupToDelete?.let { deleting ->
            Log.d("FeatureDelete", "Show dialog for group: ${deleting.features.map { it.name }}")
            val label = if (deleting.nameResId == R.string.feature_unknown) {
                deleting.features.firstOrNull()?.name ?: "Unknown"
            } else stringResource(id = deleting.nameResId)

            AlertDialog(
                onDismissRequest = { groupToDelete = null },
                title = { Text(text = stringResource(id = R.string.delete_confirm_title)) },
                text = { Text(text = stringResource(id = R.string.delete_confirm_message, label)) },
                confirmButton = {
                    TextButton(onClick = {
                        // 1. 准备删除名称列表
                        val namesToDelete = deleting.features.map { it.name }

                        // 2. UI 保留条目，但标记为禁用（删除态）
                        val displayList = features.map { ft ->
                            if (namesToDelete.contains(ft.name)) ft.copy(enabled = false) else ft
                        }

                        // 3. 保存用列表：真正从配置中移除，生成 REMOVE 补丁
                        val saveList = features.filterNot { namesToDelete.contains(it.name) }

                        // 4. 立即更新界面，避免闪烁
                        features = displayList

                        // 同步更新临时 patchActions，保证指示器立即出现
                        patchActions = patchActions + namesToDelete.associateWith { ConfigMergeManager.PatchAction.REMOVE }

                        // 5. 删除用户映射（保持原来逻辑）
                        AppFeatureMappings.getInstance().removeUserMappings(context, namesToDelete)

                        // 6. 异步保存 & 刷新
                        scope.launch {
                            repository.saveFeatures(configPath, saveList)

                            // 重新加载配置文件以获取最新的合并结果
                            val reloadedFeatures = repository.loadFeatures(configPath)
                            features = reloadedFeatures

                            // 更新补丁状态
                            if (reloadedFeatures.isNotEmpty()) {
                                val featureNames = reloadedFeatures.map { it.name }
                                patchActions = ConfigMergeManager.getFeaturesPatchActions(
                                    featureNames,
                                    currentMode == FeatureMode.APP
                                )
                            }

                            // 触发UI刷新
                            refreshTrigger++
                        }
                        groupToDelete = null
                    }) {
                        Text(text = stringResource(id = android.R.string.ok))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { groupToDelete = null }) {
                        Text(text = stringResource(id = android.R.string.cancel))
                    }
                }
            )
        }

        // 当组内有多特性需要选择编辑对象
        chooseFromGroup?.let { grp ->
            AlertDialog(
                onDismissRequest = { chooseFromGroup = null },
                title = { Text(text = stringResource(id = R.string.select_feature_to_edit)) },
                text = {
                    Column {
                        grp.features.forEach { ft ->
                            TextButton(onClick = {
                                featureToEdit = ft to AppFeatureMappings.getLocalizedDescription(context, ft.name)
                                chooseFromGroup = null
                            }) {
                                Text(text = ft.name)
                            }
                        }
                    }
                },
                confirmButton = {},
                dismissButton = {
                    TextButton(onClick = { chooseFromGroup = null }) {
                        Text(text = stringResource(id = android.R.string.cancel))
                    }
                }
            )
        }

        // 编辑特性对话框
        featureToEdit?.let { (ft, desc) ->
            EditFeatureDialog(
                onDismiss = { featureToEdit = null },
                context = context,
                originalName = ft.name,
                originalDescription = if (desc == ft.name) "" else desc,
                originalEnabled = ft.enabled,
                originalArgs = ft.args,
                originalSubNodes = ft.subNodes,
                currentMode = currentMode,
                onConfirm = { newName, newDesc, newEnabled, newArgs ->
                    // 更新feature list
                    val updatedFeatures = features.map {
                        if (it.name == ft.name) {
                            // 如果是复杂特性，保留原有子节点和参数
                            if (it.isComplex) {
                                it.copy(name = newName, enabled = newEnabled)
                            } else {
                                it.copy(name = newName, enabled = newEnabled, args = newArgs)
                            }
                        } else it
                    }
                    features = updatedFeatures.toList()

                    scope.launch {
                        repository.saveFeatures(configPath, updatedFeatures)

                        // 重新加载配置文件以获取最新的合并结果
                        val reloadedFeatures = repository.loadFeatures(configPath)
                        features = reloadedFeatures

                        // 更新补丁状态
                        if (reloadedFeatures.isNotEmpty()) {
                            val featureNames = reloadedFeatures.map { it.name }
                            patchActions = ConfigMergeManager.getFeaturesPatchActions(
                                featureNames,
                                currentMode == FeatureMode.APP
                            )
                        }

                        // 触发UI刷新
                        refreshTrigger++
                    }

                    featureToEdit = null
                }
            )
        }

        // 复杂特性提示对话框
        if (showComplexFeatureDialog) {
            AlertDialog(
                onDismissRequest = { showComplexFeatureDialog = false },
                title = { Text(text = stringResource(id = R.string.complex_feature_title)) },
                text = { 
                    Text(text = stringResource(id = R.string.complex_feature_message, complexFeatureConfigPath))
                },
                confirmButton = {
                    TextButton(onClick = { showComplexFeatureDialog = false }) {
                        Text(text = stringResource(id = android.R.string.ok))
                    }
                }
            )
        }
    }
}

@Composable
private fun FeatureSectionHeader(
    title: String,
    count: Int,
    isExpanded: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1f,
        label = "sectionScale"
    )
    val arrowRotation by animateFloatAsState(
        targetValue = if (isExpanded) 180f else 0f,
        label = "sectionArrowRotation"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onToggle
            ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 15.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = stringResource(R.string.section_feature_count, count),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
                Icon(
                    imageVector = Icons.Filled.KeyboardArrowDown,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .padding(start = 8.dp)
                        .graphicsLayer { rotationZ = arrowRotation }
                )
            }
        }
    }
}

@Composable
private fun FeatureGroupItem(
    currentMode: FeatureMode,
    group: FeatureGroup,
    onToggle: (FeatureGroup) -> Unit,
    onLongPress: () -> Unit,
    onClick: () -> Unit,
    searchQuery: String = "",
    patchAction: ConfigMergeManager.PatchAction? = null,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongPress
            ),
        colors = PatchColors.getCardColors(patchAction),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 15.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 获取特性描述
            val description = if (currentMode == FeatureMode.APP) {
                AppFeatureMappings.getLocalizedDescription(app, group.features.first().name)
            } else {
                OplusFeatureMappings.getLocalizedDescription(app, group.features.first().name)
            }
            
            // 如果组内有多个项，显示计数
            val displayText = if (group.features.size > 1) {
                "$description (${group.features.size})"
            } else {
                description
            }
            
            // 特性名称
            val featureName = group.features.first().name

            Column(
                modifier = Modifier.weight(1f)
            ) {
                // 描述使用正常字体
                HighlightedText(
                    text = displayText,
                    query = searchQuery,
                    style = MaterialTheme.typography.bodyLarge,
                )
                
                // 名称使用淡色小字体，仅当不是分组卡片时显示
                if (group.features.size == 1) {
                    Text(
                        text = featureName,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }
            
            // 补丁状态指示器
            patchAction?.let { action ->
                Text(
                    text = PatchColors.getPatchActionDescription(action),
                    style = MaterialTheme.typography.labelSmall,
                    color = PatchColors.getIndicatorColor(action) ?: MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }

            val feature = group.features.first()
            feature.args?.startsWith("boolean:") == true
            val isComplex = feature.isComplex
            val isUnavailable = currentMode == FeatureMode.OPLUS && feature.args == "unavailable"
            val trailingModifier = Modifier.padding(start = 8.dp)
            // 仅当 OPLUS 模式且该特性不是用户新增（PATCH_ACTION != ADD）时显示开关
            if (currentMode == FeatureMode.OPLUS && patchAction != ConfigMergeManager.PatchAction.ADD) {
                Switch(
                    checked = group.isEnabled,
                    onCheckedChange = {
                        onToggle(group.withEnabled(it))
                    },
                    modifier = trailingModifier
                )
            } else if (isComplex) {
                Text(
                    text = stringResource(id = R.string.complex_feature_indicator),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.tertiary,
                    modifier = trailingModifier
                )
            } else if (isUnavailable) {
                Text(
                    text = stringResource(id = R.string.unavailable_feature_indicator),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                    modifier = trailingModifier
                )
            }
            // 移除了 args 为 boolean 的配置在列表中显示开关的逻辑
            // args 为 boolean 的配置只在编辑界面显示开关
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun FeatureConfigPreview() {
    // 使用示例配置路径或空列表
    FeatureConfigScreen(
        configPath = "",
        currentMode = FeatureMode.APP,
        onModeChange = {},
        repository = XmlFeatureRepository()
    )
} 