package com.itosfish.colorfeatureenhance.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
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
import com.itosfish.colorfeatureenhance.utils.AddFeatureDialog
import com.itosfish.colorfeatureenhance.utils.EditFeatureDialog
import com.itosfish.colorfeatureenhance.utils.OriginalNames
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
    var features by remember { mutableStateOf<List<AppFeature>>(emptyList()) }
    var originalNames by remember { mutableStateOf<Set<String>>(emptySet()) }

    val context = LocalContext.current

    var groupToDelete by remember { mutableStateOf<FeatureGroup?>(null) }
    var featureToEdit by remember { mutableStateOf<Pair<AppFeature, String>?>(null) }
    var chooseFromGroup by remember { mutableStateOf<FeatureGroup?>(null) }
    var showAddDialog by remember { mutableStateOf(false) }

    var isSearchActive by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    var isSystemSectionExpanded by remember { mutableStateOf(false) }
    var isAppSectionExpanded by remember { mutableStateOf(false) }

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

    val builtInFeatureNames = remember(features, currentMode) {
        features.map { it.name }.filterTo(mutableSetOf()) { name ->
            if (currentMode == FeatureMode.APP) {
                AppFeatureMappings.getInstance().isBuiltInFeature(name)
            } else {
                OplusFeatureMappings.getInstance().isBuiltInFeature(name)
            }
        }
    }

    val (appAddedGroups, systemGroups) = remember(displayedFeatureGroups, originalNames) {
        displayedFeatureGroups.partition { group ->
            group.features.none { it.name in originalNames }
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
                            isBuiltIn = group.features.all { it.name in builtInFeatureNames },
                            onToggle = { updatedGroup ->
                                save(
                                    features.map { feature ->
                                        if (updatedGroup.features.any { it.name == feature.name }) {
                                            feature.copy(enabled = updatedGroup.isEnabled)
                                        } else {
                                            feature
                                        }
                                    }
                                )
                            },
                            onLongPress = { groupToDelete = group },
                            onClick = {
                                if (group.features.size == 1) {
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
                            .padding(vertical = 32.dp),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }

            item {
                Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.navigationBars))
            }
        }

        if (showAddDialog) {
            AddFeatureDialog(
                onDismiss = { showAddDialog = false },
                context = context,
                currentMode = currentMode,
                onConfirm = { name, _, enabled, args ->
                    save(features + AppFeature(name, enabled, args))
                    isAppSectionExpanded = true
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
                    Column {
                        group.features.forEach { feature ->
                            TextButton(onClick = {
                                featureToEdit = feature to if (currentMode == FeatureMode.APP) {
                                    AppFeatureMappings.getLocalizedDescription(context, feature.name)
                                } else {
                                    OplusFeatureMappings.getLocalizedDescription(context, feature.name)
                                }
                                chooseFromGroup = null
                            }) {
                                Text(text = feature.name)
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

        featureToEdit?.let { (feature, description) ->
            EditFeatureDialog(
                onDismiss = { featureToEdit = null },
                context = context,
                originalName = feature.name,
                originalDescription = if (description == feature.name) "" else description,
                originalEnabled = feature.enabled,
                originalArgs = feature.args,
                originalSubNodes = feature.subNodes,
                currentMode = currentMode,
                onConfirm = { newName, _, newEnabled, newArgs ->
                    save(
                        features.map {
                            if (it.name == feature.name) {
                                it.copy(name = newName, enabled = newEnabled, args = newArgs)
                            } else {
                                it
                            }
                        }
                    )
                    featureToEdit = null
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
private fun FeatureTag(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.primary,
        modifier = modifier
            .background(
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                shape = RoundedCornerShape(50)
            )
            .padding(horizontal = 8.dp, vertical = 2.dp)
    )
}

@Composable
private fun FeatureGroupItem(
    currentMode: FeatureMode,
    group: FeatureGroup,
    onToggle: (FeatureGroup) -> Unit,
    onLongPress: () -> Unit,
    onClick: () -> Unit,
    searchQuery: String = "",
    isBuiltIn: Boolean = false,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongPress
            ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
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
            val description = if (currentMode == FeatureMode.APP) {
                AppFeatureMappings.getLocalizedDescription(app, group.features.first().name)
            } else {
                OplusFeatureMappings.getLocalizedDescription(app, group.features.first().name)
            }
            val displayText = if (group.features.size > 1) {
                "$description (${group.features.size})"
            } else {
                description
            }

            Column(modifier = Modifier.weight(1f)) {
                HighlightedText(
                    text = displayText,
                    query = searchQuery,
                    style = MaterialTheme.typography.bodyLarge
                )

                if (group.features.size == 1) {
                    Text(
                        text = group.features.first().name,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }

            if (isBuiltIn) {
                FeatureTag(
                    text = stringResource(R.string.feature_builtin_tag),
                    modifier = Modifier.padding(start = 8.dp)
                )
            }

            val feature = group.features.first()
            val isComplex = feature.isComplex
            val isUnavailable = currentMode != FeatureMode.APP && feature.args == "unavailable"
            val trailingModifier = Modifier.padding(start = 8.dp)

            if (currentMode != FeatureMode.APP) {
                Switch(
                    checked = group.isEnabled,
                    onCheckedChange = { onToggle(group.withEnabled(it)) },
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
        }
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
