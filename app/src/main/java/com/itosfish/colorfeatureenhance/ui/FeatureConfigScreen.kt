@file:OptIn(androidx.compose.material3.ExperimentalMaterial3ExpressiveApi::class)

package com.itosfish.colorfeatureenhance.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedListItem
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.itosfish.colorfeatureenhance.FeatureMode
import com.itosfish.colorfeatureenhance.MainActivity.Companion.app
import com.itosfish.colorfeatureenhance.R
import com.itosfish.colorfeatureenhance.data.model.AppFeature
import com.itosfish.colorfeatureenhance.data.model.AppFeatureMappings
import com.itosfish.colorfeatureenhance.data.model.AppendPresets
import com.itosfish.colorfeatureenhance.data.model.FeatureGroup
import com.itosfish.colorfeatureenhance.data.model.OplusFeatureMappings
import com.itosfish.colorfeatureenhance.data.repository.XmlFeatureRepository
import com.itosfish.colorfeatureenhance.domain.FeatureRepository
import com.itosfish.colorfeatureenhance.ui.components.ColorOSTopAppBar
import com.itosfish.colorfeatureenhance.ui.components.HighlightedText
import com.itosfish.colorfeatureenhance.ui.components.SearchBar
import com.itosfish.colorfeatureenhance.ui.search.SearchLogic
import com.itosfish.colorfeatureenhance.utils.AddFeatureDialog
import com.itosfish.colorfeatureenhance.utils.EditFeatureDialog
import com.itosfish.colorfeatureenhance.utils.FeatureCatalog
import com.itosfish.colorfeatureenhance.utils.OriginalNames
import kotlinx.coroutines.launch

private enum class FeatureSection { ENHANCED, SYSTEM }

private sealed interface FeatureListEntry {
    data class Header(val section: FeatureSection, val count: Int) : FeatureListEntry
    data class Group(val group: FeatureGroup) : FeatureListEntry
}

private data class EditingTarget(
    val feature: AppFeature,
    val description: String,
    val parentGroup: FeatureGroup? = null
)

@Composable
fun FeatureConfigScreen(
    configPath: String,
    currentMode: FeatureMode,
    onModeChange: (FeatureMode) -> Unit,
    repository: FeatureRepository
) {
    val scope = rememberCoroutineScope()
    var features by remember { mutableStateOf<List<AppFeature>>(emptyList()) }
    var originalNames by remember { mutableStateOf<Set<String>>(emptySet()) }

    val context = LocalContext.current

    var groupToDelete by remember { mutableStateOf<FeatureGroup?>(null) }
    var editingTarget by remember { mutableStateOf<EditingTarget?>(null) }
    var chooseFromGroup by remember { mutableStateOf<FeatureGroup?>(null) }
    var showAddDialog by remember { mutableStateOf(false) }

    var isSearchActive by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    var isSystemSectionExpanded by remember { mutableStateOf(false) }
    var isEnhancedSectionExpanded by remember { mutableStateOf(true) }

    BackHandler(enabled = isSearchActive) {
        isSearchActive = false
        searchQuery = ""
    }

    fun save(updated: List<AppFeature>) {
        features = updated
        scope.launch {
            repository.saveFeatures(configPath, updated)
            features = repository.loadFeatures(configPath)
        }
    }

    fun toggleGroupEnabled(group: FeatureGroup, targetEnabled: Boolean) {
        val targetNames = group.features.map { it.name }.toSet()
        val updated = features.map { feature ->
            if (feature.name in targetNames) {
                feature.copy(enabled = targetEnabled)
            } else {
                feature
            }
        }
        save(updated)
    }

    LaunchedEffect(configPath, currentMode) {
        features = repository.loadFeatures(configPath)
        originalNames = OriginalNames.get(currentMode)
    }

    val featureGroups by remember(features, currentMode) {
        derivedStateOf {
            features.groupBy { feature ->
                val description = if (currentMode == FeatureMode.APP) {
                    AppFeatureMappings.getLocalizedDescription(app, feature.name)
                } else {
                    OplusFeatureMappings.getLocalizedDescription(app, feature.name)
                }
                val resUnknown = if (currentMode == FeatureMode.APP) {
                    AppFeatureMappings.getInstance().getResId(feature.name)
                } else {
                    OplusFeatureMappings.getInstance().getResId(feature.name)
                }
                if (resUnknown == R.string.feature_unknown && description == feature.name) {
                    "unknown_${feature.name}"
                } else {
                    "desc_$description"
                }
            }.map { (_, groupFeatures) ->
                val nameResId = if (currentMode == FeatureMode.APP) {
                    AppFeatureMappings.getInstance().getResId(groupFeatures.first().name)
                } else {
                    OplusFeatureMappings.getInstance().getResId(groupFeatures.first().name)
                }
                FeatureGroup(nameResId, groupFeatures)
            }.sortedWith(
                compareBy<FeatureGroup> { it.nameResId != R.string.feature_unknown }
                    .thenBy {
                        if (currentMode == FeatureMode.APP) {
                            AppFeatureMappings.getLocalizedDescription(app, it.features.first().name)
                        } else {
                            OplusFeatureMappings.getLocalizedDescription(app, it.features.first().name)
                        }
                    }
            )
        }
    }

    val displayedFeatureGroups = remember(featureGroups, searchQuery, currentMode) {
        if (searchQuery.isBlank()) {
            featureGroups
        } else {
            SearchLogic.filterFeatureGroups(featureGroups, searchQuery, context, currentMode)
        }
    }

    val isSearching = searchQuery.isNotBlank()

    val (enhancedGroups, systemGroups) = remember(displayedFeatureGroups, originalNames, currentMode) {
        displayedFeatureGroups.partition { group ->
            group.features.any { feature ->
                AppendPresets.isPresetFeature(feature.name) ||
                    FeatureCatalog.isCustomAdded(currentMode, feature.name) ||
                    feature.name !in originalNames
            }
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
            "unknown_${firstFeature.name}"
        } else {
            "desc_${description}"
        }
    }

    val listEntries: List<FeatureListEntry> = remember(
        enhancedGroups,
        systemGroups,
        isEnhancedSectionExpanded,
        isSystemSectionExpanded,
        isSearching
    ) {
        buildList {
            if (enhancedGroups.isEmpty() && systemGroups.isEmpty()) return@buildList

            if (enhancedGroups.isNotEmpty()) {
                add(FeatureListEntry.Header(FeatureSection.ENHANCED, enhancedGroups.size))
                if (isEnhancedSectionExpanded || isSearching) {
                    enhancedGroups.forEach { add(FeatureListEntry.Group(it)) }
                }
            }

            if (systemGroups.isNotEmpty()) {
                add(FeatureListEntry.Header(FeatureSection.SYSTEM, systemGroups.size))
                if (isSystemSectionExpanded || isSearching) {
                    systemGroups.forEach { add(FeatureListEntry.Group(it)) }
                }
            }
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
                        if (isSearchActive) searchQuery = ""
                        isSearchActive = !isSearchActive
                    }
                )

                SearchBar(
                    query = searchQuery,
                    onQueryChange = { searchQuery = it },
                    onClearQuery = { searchQuery = "" },
                    isVisible = isSearchActive
                )
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
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
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp)
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
                                FeatureSection.ENHANCED -> R.string.section_enhanced_features
                                FeatureSection.SYSTEM -> R.string.section_system_features
                            }
                        ),
                        count = entry.count,
                        isExpanded = when (entry.section) {
                            FeatureSection.ENHANCED -> isEnhancedSectionExpanded || isSearching
                            FeatureSection.SYSTEM -> isSystemSectionExpanded || isSearching
                        },
                        onToggle = {
                            when (entry.section) {
                                FeatureSection.ENHANCED -> isEnhancedSectionExpanded = !isEnhancedSectionExpanded
                                FeatureSection.SYSTEM -> isSystemSectionExpanded = !isSystemSectionExpanded
                            }
                        },
                        modifier = Modifier.animateItem()
                    )

                    is FeatureListEntry.Group -> {
                        val group = entry.group
                        val firstFeature = group.features.first()
                        val isPreset = AppendPresets.isPresetFeature(firstFeature.name)
                        val isCustom = FeatureCatalog.isCustomAdded(currentMode, firstFeature.name)

                        FeatureGroupCard(
                            currentMode = currentMode,
                            group = group,
                            searchQuery = searchQuery,
                            isPreset = isPreset,
                            isCustom = isCustom,
                            onToggle = { targetGroup, targetEnabled ->
                                toggleGroupEnabled(targetGroup, targetEnabled)
                            },
                            onLongPress = { groupToDelete = group },
                            onClick = {
                                if (group.features.size == 1) {
                                    val feature = group.features.first()
                                    val description = if (currentMode == FeatureMode.APP) {
                                        AppFeatureMappings.getLocalizedDescription(context, feature.name)
                                    } else {
                                        OplusFeatureMappings.getLocalizedDescription(context, feature.name)
                                    }
                                    editingTarget = EditingTarget(feature, description, group)
                                } else {
                                    chooseFromGroup = group
                                }
                            },
                            modifier = Modifier.animateItem()
                        )
                    }
                }
            }

            if (displayedFeatureGroups.isEmpty()) {
                item {
                    Text(
                        text = if (searchQuery.isNotBlank()) {
                            stringResource(R.string.search_no_results)
                        } else {
                            stringResource(R.string.no_features)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 48.dp),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // 预留底部足够高度，避免浮动按钮（FAB）挡住最底部的开关卡片
            item {
                Spacer(modifier = Modifier.height(88.dp))
            }
        }

        if (showAddDialog) {
            AddFeatureDialog(
                onDismiss = { showAddDialog = false },
                context = context,
                currentMode = currentMode,
                onConfirm = { name, _, enabled, args ->
                    FeatureCatalog.markCustomAdded(currentMode, name)
                    save(features + AppFeature(name, enabled, args))
                    isEnhancedSectionExpanded = true
                    showAddDialog = false
                }
            )
        }

        groupToDelete?.let { deleting ->
            val namesToDelete = deleting.features.map { it.name }
            val label = if (deleting.nameResId == R.string.feature_unknown) {
                deleting.features.firstOrNull()?.name ?: "Unknown"
            } else {
                stringResource(id = deleting.nameResId)
            }

            AlertDialog(
                onDismissRequest = { groupToDelete = null },
                title = { Text(text = stringResource(id = R.string.delete_confirm_title)) },
                text = { Text(text = stringResource(id = R.string.delete_confirm_message, label)) },
                confirmButton = {
                    TextButton(onClick = {
                        FeatureCatalog.remove(currentMode, namesToDelete)
                        save(features.filterNot { it.name in namesToDelete })
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

        chooseFromGroup?.let { group ->
            AlertDialog(
                onDismissRequest = { chooseFromGroup = null },
                title = { Text(text = stringResource(id = R.string.select_feature_to_edit)) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        group.features.forEach { feature ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(MaterialTheme.shapes.medium)
                                    .clickable {
                                        val description = if (currentMode == FeatureMode.APP) {
                                            AppFeatureMappings.getLocalizedDescription(context, feature.name)
                                        } else {
                                            OplusFeatureMappings.getLocalizedDescription(context, feature.name)
                                        }
                                        editingTarget = EditingTarget(feature, description, group)
                                        chooseFromGroup = null
                                    }
                                    .padding(horizontal = 8.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = feature.name,
                                    modifier = Modifier.weight(1f).padding(end = 8.dp),
                                    style = MaterialTheme.typography.bodyMedium,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Switch(
                                    checked = feature.enabled,
                                    onCheckedChange = { isChecked ->
                                        save(
                                            features.map {
                                                if (it.name == feature.name) it.copy(enabled = isChecked) else it
                                            }
                                        )
                                    }
                                )
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = {
                        toggleGroupEnabled(group, false)
                        chooseFromGroup = null
                    }) {
                        Text(text = stringResource(id = R.string.group_disable_all))
                    }
                },
                dismissButton = {
                    TextButton(onClick = {
                        toggleGroupEnabled(group, true)
                        chooseFromGroup = null
                    }) {
                        Text(text = stringResource(id = R.string.group_enable_all))
                    }
                }
            )
        }

        editingTarget?.let { target ->
            val feature = target.feature
            EditFeatureDialog(
                onDismiss = { editingTarget = null },
                context = context,
                originalName = feature.name,
                originalDescription = if (target.description == feature.name) "" else target.description,
                originalEnabled = feature.enabled,
                originalArgs = feature.args,
                originalSubNodes = feature.subNodes,
                currentMode = currentMode,
                onConfirm = { newName, _, newEnabled, newArgs ->
                    val relatedNames = target.parentGroup?.features?.map { it.name }?.toSet()
                        ?: setOf(feature.name)

                    save(
                        features.map {
                            if (it.name == feature.name) {
                                it.copy(name = newName, enabled = newEnabled, args = newArgs)
                            } else if (it.name in relatedNames && it.enabled != newEnabled) {
                                it.copy(enabled = newEnabled)
                            } else {
                                it
                            }
                        }
                    )
                    editingTarget = null
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
    val motion = MaterialTheme.motionScheme
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1f,
        animationSpec = motion.fastSpatialSpec(),
        label = "sectionScale"
    )
    val arrowRotation by animateFloatAsState(
        targetValue = if (isExpanded) 180f else 0f,
        animationSpec = motion.fastEffectsSpec(),
        label = "sectionArrowRotation"
    )

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(MaterialTheme.shapes.large)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onToggle
            ),
        color = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.6f),
        shape = MaterialTheme.shapes.large
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
                ) {
                    Text(
                        text = stringResource(R.string.section_feature_count, count),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
                Icon(
                    imageVector = Icons.Filled.KeyboardArrowDown,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .padding(start = 6.dp)
                        .graphicsLayer { rotationZ = arrowRotation }
                )
            }
        }
    }
}

@Composable
private fun FeatureTag(
    text: String,
    containerColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.primaryContainer,
    contentColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onPrimaryContainer,
    modifier: Modifier = Modifier
) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelSmall,
        color = contentColor,
        modifier = modifier
            .background(color = containerColor, shape = CircleShape)
            .padding(horizontal = 8.dp, vertical = 2.dp)
    )
}

/**
 * 现代 Material 3 Expressive 卡片式特性条目
 */
@Composable
private fun FeatureGroupCard(
    currentMode: FeatureMode,
    group: FeatureGroup,
    onToggle: (FeatureGroup, Boolean) -> Unit,
    onLongPress: () -> Unit,
    onClick: () -> Unit,
    searchQuery: String = "",
    isPreset: Boolean = false,
    isCustom: Boolean = false,
    modifier: Modifier = Modifier
) {
    val feature = group.features.first()
    val description = if (currentMode == FeatureMode.APP) {
        AppFeatureMappings.getLocalizedDescription(app, feature.name)
    } else {
        OplusFeatureMappings.getLocalizedDescription(app, feature.name)
    }
    val displayText = if (group.features.size > 1) {
        "$description (${group.features.size})"
    } else {
        description
    }

    // 独立圆角卡片容器
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.extraLarge),
        shape = MaterialTheme.shapes.extraLarge,
        color = MaterialTheme.colorScheme.surfaceContainer,
        tonalElevation = 1.dp
    ) {
        SegmentedListItem(
            onClick = onClick,
            shapes = ListItemDefaults.shapes(shape = MaterialTheme.shapes.extraLarge),
            colors = ListItemDefaults.segmentedColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainer
            ),
            modifier = Modifier.fillMaxWidth(),
            onLongClick = onLongPress,
            supportingContent = {
                Text(
                    text = feature.name,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            },
            trailingContent = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (isPreset) {
                        FeatureTag(
                            text = stringResource(R.string.feature_tag_preset),
                            containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                            contentColor = MaterialTheme.colorScheme.primary
                        )
                    } else if (isCustom) {
                        FeatureTag(
                            text = stringResource(R.string.feature_tag_custom),
                            containerColor = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.12f),
                            contentColor = MaterialTheme.colorScheme.tertiary
                        )
                    }

                    if (feature.isComplex) {
                        Text(
                            text = stringResource(id = R.string.complex_feature_indicator),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(start = 6.dp)
                        )
                    }
                    Switch(
                        checked = group.isEnabled,
                        onCheckedChange = { isChecked -> onToggle(group, isChecked) },
                        modifier = Modifier.padding(start = 10.dp)
                    )
                }
            },
            content = {
                HighlightedText(
                    text = displayText,
                    query = searchQuery,
                    style = MaterialTheme.typography.titleMedium
                )
            }
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun FeatureConfigPreview() {
    FeatureConfigScreen(
        configPath = "",
        currentMode = FeatureMode.APP,
        onModeChange = {},
        repository = XmlFeatureRepository()
    )
}
