@file:OptIn(androidx.compose.material3.ExperimentalMaterial3ExpressiveApi::class)

package com.itosfish.colorfeatureenhance.ui.components

import android.app.Activity
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MediumFlexibleTopAppBar
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import com.itosfish.colorfeatureenhance.FeatureMode
import com.itosfish.colorfeatureenhance.R
import com.itosfish.colorfeatureenhance.utils.CLog
import com.itosfish.colorfeatureenhance.utils.CSU
import com.itosfish.colorfeatureenhance.utils.ModuleAutoUpdater
import com.itosfish.colorfeatureenhance.utils.showAboutDialog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ColorOSTopAppBar(
    title: String,
    currentMode: FeatureMode,
    onModeChange: (FeatureMode) -> Unit,
    isSearchActive: Boolean = false,
    onSearchClick: (() -> Unit)? = null
) {
    val context = LocalContext.current
    var showMoreMenu by remember { mutableStateOf(false) }

    val windowInfo = LocalWindowInfo.current
    val density = LocalDensity.current
    val menuWidth = remember(windowInfo.containerSize.width) {
        with(density) {
            (windowInfo.containerSize.width.toDp() / 2) + 10.dp
        }
    }

    val modeLabelRes = when (currentMode) {
        FeatureMode.APP -> R.string.mode_app
        FeatureMode.OPLUS -> R.string.mode_oplus
        FeatureMode.REGION -> R.string.mode_region
    }

    MediumFlexibleTopAppBar(
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge
            )
        },
        actions = {
            // 模式切换胶囊按钮（带背景与图标，不再与副标题重复）
            Surface(
                modifier = Modifier
                    .padding(end = 4.dp)
                    .clip(CircleShape)
                    .clickable {
                        val next = when (currentMode) {
                            FeatureMode.APP -> FeatureMode.OPLUS
                            FeatureMode.OPLUS -> FeatureMode.REGION
                            FeatureMode.REGION -> FeatureMode.APP
                        }
                        onModeChange(next)
                    },
                shape = CircleShape,
                color = MaterialTheme.colorScheme.secondaryContainer
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(id = modeLabelRes),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )
                }
            }

            // 搜索按钮
            if (onSearchClick != null) {
                IconButton(onClick = onSearchClick) {
                    Icon(
                        imageVector = Icons.Filled.Search,
                        contentDescription = stringResource(id = R.string.search_feature),
                        tint = if (isSearchActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            // 更多菜单按钮
            Box {
                IconButton(onClick = { showMoreMenu = true }) {
                    Icon(
                        imageVector = Icons.Filled.MoreVert,
                        contentDescription = stringResource(R.string.more_menu)
                    )
                }

                DropdownMenu(
                    expanded = showMoreMenu,
                    onDismissRequest = { showMoreMenu = false },
                    offset = DpOffset(
                        x = 0.dp,
                        y = -TopAppBarDefaults.TopAppBarExpandedHeight
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.width(menuWidth)
                ) {
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = stringResource(R.string.update_modules),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        },
                        onClick = {
                            showMoreMenu = false
                            forceUpdateModule(context as Activity)
                        }
                    )

                    DropdownMenuItem(
                        text = {
                            Text(
                                text = stringResource(R.string.export_logs),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        },
                        onClick = {
                            showMoreMenu = false
                            exportLogs(context as Activity)
                        }
                    )

                    DropdownMenuItem(
                        text = {
                            Text(
                                text = stringResource(id = R.string.about),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        },
                        onClick = {
                            showMoreMenu = false
                            showAboutDialog(context as Activity)
                        }
                    )
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    )
}

private fun forceUpdateModule(activity: Activity) {
    CoroutineScope(Dispatchers.Main).launch {
        try {
            Toast.makeText(activity, R.string.module_updating, Toast.LENGTH_SHORT).show()
            CLog.i("TopAppBar", "用户手动触发模块更新")

            when (val updateResult = ModuleAutoUpdater.forceUpdateModule()) {
                is ModuleAutoUpdater.UpdateResult.Success -> {
                    Toast.makeText(activity, R.string.module_update_success, Toast.LENGTH_SHORT).show()
                    CLog.i("TopAppBar", "手动模块更新成功")
                }
                is ModuleAutoUpdater.UpdateResult.Failed -> {
                    Toast.makeText(
                        activity,
                        activity.getString(R.string.module_update_failed, updateResult.reason),
                        Toast.LENGTH_LONG
                    ).show()
                    CLog.e("TopAppBar", "手动模块更新失败: ${updateResult.reason}")
                }
                else -> {
                    Toast.makeText(activity, R.string.module_update_unknown, Toast.LENGTH_SHORT).show()
                    CLog.w("TopAppBar", "手动模块更新状态未知")
                }
            }
        } catch (e: Exception) {
            Toast.makeText(
                activity,
                activity.getString(R.string.module_update_error, e.message),
                Toast.LENGTH_LONG
            ).show()
            CLog.e("TopAppBar", "手动模块更新异常", e)
        }
    }
}

private fun exportLogs(activity: Activity) {
    try {
        val logs = CLog.getFormattedLogs()

        if (CLog.getAllLogs().isEmpty()) {
            Toast.makeText(activity, R.string.no_logs_to_export, Toast.LENGTH_SHORT).show()
            return
        }

        val dateFormat = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
        val timestamp = dateFormat.format(Date())
        val fileName = "ColorFeatureEnhance_logs_$timestamp.txt"
        val filePath = "/sdcard/$fileName"

        val tempFile = java.io.File(activity.cacheDir, fileName)
        tempFile.writeText(logs)

        val result = CSU.runWithSu("cp \"${tempFile.absolutePath}\" \"$filePath\"")

        if (result.exitCode == 0 && CSU.fileExists(filePath)) {
            Toast.makeText(
                activity,
                activity.getString(R.string.export_logs_success, filePath),
                Toast.LENGTH_LONG
            ).show()
            CLog.i("TopAppBar", "日志导出成功: $filePath")
        } else {
            Toast.makeText(activity, R.string.export_logs_failed, Toast.LENGTH_SHORT).show()
            CLog.e("TopAppBar", "日志导出失败, 退出码: ${result.exitCode}")
        }

        tempFile.delete()
    } catch (e: Exception) {
        Toast.makeText(activity, R.string.export_logs_failed, Toast.LENGTH_SHORT).show()
        CLog.e("TopAppBar", "日志导出失败", e)
    }
}
