package com.itosfish.colorfeatureenhance.utils

// Linkify 可能引入歧义，已不再需要
import android.app.Activity
import android.content.Context
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.TextView
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.itosfish.colorfeatureenhance.FeatureMode
import com.itosfish.colorfeatureenhance.R
import com.itosfish.colorfeatureenhance.data.model.AppFeature
import com.itosfish.colorfeatureenhance.data.model.AppFeatureMappings
import com.itosfish.colorfeatureenhance.data.model.FeatureSubNode
import com.itosfish.colorfeatureenhance.data.model.OplusFeatureMappings


/**
 * 使用 XML 布局展示 About 弹窗，模仿 Shizuku 实现。
 */
fun showAboutDialog(activity: Activity) {
    val inflater = LayoutInflater.from(activity)
    val root = inflater.inflate(R.layout.dialog_about, null, false)

    // 图标
    val iconView = root.findViewById<ImageView>(R.id.icon)
    iconView.setImageDrawable(activity.packageManager.getApplicationIcon(activity.applicationInfo))

    // 版本号
    val versionNameView = root.findViewById<TextView>(R.id.version_name)
    val versionName = activity.packageManager.getPackageInfo(activity.packageName, 0).versionName
    versionNameView.text = "$versionName"

    // 富文本链接
    val sourceCodeView = root.findViewById<TextView>(R.id.source_code)
    sourceCodeView.movementMethod = LinkMovementMethod.getInstance()
    MaterialAlertDialogBuilder(activity)
        .setView(root)
        .show()
}

@Composable
fun AddFeatureDialog(
    onDismiss: () -> Unit,
    onConfirm: (name: String, description: String, enabled: Boolean, args: String?) -> Unit,
    context: Context,
    currentMode: FeatureMode
) {
    var featureName by remember { mutableStateOf("") }
    var featureDescription by remember { mutableStateOf("") }
    var featureEnabled by remember { mutableStateOf(true) }
    var argValue by remember { mutableStateOf("") }
    var showPresetMatchDialog by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = stringResource(id = R.string.add_feature)) },
        text = {
            Column {
                // 特性描述输入
                OutlinedTextField(
                    value = featureDescription,
                    onValueChange = { featureDescription = it },
                    label = { Text(stringResource(id = R.string.feature_description)) },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                // 特性名称输入
                OutlinedTextField(
                    value = featureName,
                    onValueChange = { featureName = it },
                    label = { Text(stringResource(id = R.string.feature_name)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(16.dp))

                if (currentMode == FeatureMode.APP) {
                    OutlinedTextField(
                        value = argValue,
                        onValueChange = { argValue = it },
                        label = { Text("配置值 (留空表示无 args)") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (argValue.startsWith("boolean:")) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = stringResource(id = R.string.feature_enabled))
                            Switch(
                                checked = featureEnabled,
                                onCheckedChange = {
                                    featureEnabled = it
                                    argValue = if (it) "boolean:true" else "boolean:false"
                                }
                            )
                        }
                    }
                } else {
                    featureEnabled = true // 保持启用状态，无 args 默认为启用
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (featureName.isNotEmpty()) {
                        // 只有当描述不为空时才保存映射
                        if (featureDescription.isNotEmpty()) {
                            // 检查是否与预设映射匹配
                            val isPresetMatch = if (currentMode == FeatureMode.APP) {
                                AppFeatureMappings.getInstance().isMatchingPresetDescription(
                                    context, featureName, featureDescription
                                )
                            } else {
                                OplusFeatureMappings.isMatchingPresetDescription(
                                    context,
                                    featureName,
                                    featureDescription
                                )
                            }

                            // 检查是否存在云端配置
                            val hasCloudConfig = if (currentMode == FeatureMode.APP) {
                                AppFeatureMappings.getInstance().isMatchingCloudDescription(context, featureName)
                            } else {
                                OplusFeatureMappings.isMatchingCloudDescription(context, featureName)
                            }

                            if (isPresetMatch) {
                                // 显示提示对话框
                                showPresetMatchDialog = true
                            } else if (hasCloudConfig) {
                                // 存在云端配置时，不保存用户映射，直接添加特性
                                onConfirm(
                                    featureName,
                                    featureDescription,
                                    featureEnabled,
                                    if (argValue.isBlank()) null else argValue
                                )
                            } else {
                                // 保存用户自定义映射
                                if (currentMode == FeatureMode.APP) {
                                    AppFeatureMappings.getInstance()
                                        .saveUserMapping(context, featureName, featureDescription)
                                } else {
                                    OplusFeatureMappings.saveUserMapping(
                                        context,
                                        featureName,
                                        featureDescription
                                    )
                                }
                                // 添加特性并关闭对话框
                                onConfirm(
                                    featureName,
                                    featureDescription,
                                    featureEnabled,
                                    if (argValue.isBlank()) null else argValue
                                )
                                // 不要在这里关闭对话框，因为showPresetMatchDialog=true时需要继续显示
                            }
                        } else {
                            // 描述为空，直接添加特性并关闭对话框
                            onConfirm(
                                featureName,
                                featureDescription,
                                featureEnabled,
                                if (argValue.isBlank()) null else argValue
                            )
                        }
                    }
                },
                enabled = featureName.isNotEmpty() // 只要名称不为空即可
            ) {
                Text(text = stringResource(id = android.R.string.ok))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(id = android.R.string.cancel))
            }
        }
    )

    // 预设映射匹配提示对话框
    if (showPresetMatchDialog) {
        AlertDialog(
            onDismissRequest = { showPresetMatchDialog = false },
            title = { Text(text = stringResource(id = R.string.preset_mapping_match_title)) },
            text = { Text(text = stringResource(id = R.string.preset_mapping_match_message)) },
            confirmButton = {
                TextButton(onClick = {
                    // 关闭提示对话框，添加特性并关闭主对话框
                    showPresetMatchDialog = false
                    onConfirm(
                        featureName,
                        featureDescription,
                        featureEnabled,
                        if (argValue.isBlank()) null else argValue
                    )
                }) {
                    Text(text = stringResource(id = android.R.string.ok))
                }
            }
        )
    }
}

/**
 * 编辑特性对话框
 * @param originalName 原始特性名称
 * @param originalDescription 原始描述
 * @param originalEnabled 原始启用状态
 */
@Composable
fun EditFeatureDialog(
    onDismiss: () -> Unit,
    context: Context,
    originalName: String,
    originalDescription: String,
    originalEnabled: Boolean,
    originalArgs: String?,
    currentMode: FeatureMode,
    originalSubNodes: List<FeatureSubNode> = emptyList(),
    onConfirm: (newName: String, newDescription: String, newEnabled: Boolean, newArgs: String?) -> Unit
) {
    var featureName by remember { mutableStateOf(originalName) }
    var featureDescription by remember { mutableStateOf(originalDescription) }
    var featureEnabled by remember { mutableStateOf(originalEnabled) }
    var argValue by remember { mutableStateOf(originalArgs ?: "") }

    // 首次备份里存在的特性视为系统自带，名称只读
    val isSystemFeature = remember(originalName, currentMode) {
        ConfigFiles.isInOriginal(ConfigFiles.targetForMode(currentMode), originalName)
    }

    // 检查是否为预设描述
    val isPresetDesc = if (currentMode == FeatureMode.APP) {
        AppFeatureMappings.getInstance()
            .isMatchingPresetDescription(context, originalName, originalDescription)
    } else {
        OplusFeatureMappings.isMatchingPresetDescription(context, originalName, originalDescription)
    }

    // 检查是否存在云端配置描述
    val hasCloudDesc = if (currentMode == FeatureMode.APP) {
        AppFeatureMappings.getInstance().isMatchingCloudDescription(context, originalName)
    } else {
        OplusFeatureMappings.isMatchingCloudDescription(context, originalName)
    }

    // 当存在预设描述或云端配置时，禁用描述输入框
    val isDescriptionDisabled = isPresetDesc || hasCloudDesc

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = stringResource(id = R.string.edit_feature)) },
        text = {
            Column {
                OutlinedTextField(
                    value = featureDescription,
                    onValueChange = { if (!isDescriptionDisabled) featureDescription = it },
                    label = { Text(stringResource(id = R.string.feature_description)) },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isDescriptionDisabled,
                    readOnly = isDescriptionDisabled
                )
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = featureName,
                    onValueChange = { if (!isSystemFeature) featureName = it },
                    label = { Text(stringResource(id = R.string.feature_name)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    enabled = !isSystemFeature,
                    readOnly = isSystemFeature
                )

//                // 显示系统特性提示
//                if (isSystemFeature) {
//                    Text(
//                        text = stringResource(id = R.string.system_feature_name_readonly_hint),
//                        style = MaterialTheme.typography.bodySmall,
//                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
//                        modifier = Modifier.fillMaxWidth()
//                    )
//                }
                Spacer(modifier = Modifier.height(16.dp))
                // 检查是否为复杂特性
                val tempFeature = AppFeature(
                    name = originalName,
                    enabled = originalEnabled,
                    args = originalArgs,
                    subNodes = originalSubNodes
                )

                if (tempFeature.isComplex) {
                    Text(
                        text = stringResource(R.string.complex_feature_edit_restriction),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                    // 复杂特性不显示参数编辑控件
                    return@Column
                } else {
                    val hasBoolean = originalArgs?.startsWith("boolean:") == true

                    if (currentMode == FeatureMode.APP && hasBoolean) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = stringResource(id = R.string.feature_enabled))
                            Switch(
                                checked = featureEnabled,
                                onCheckedChange = {
                                    featureEnabled = it
                                    // 同步更新argValue以确保boolean参数正确
                                    argValue = if (it) "boolean:true" else "boolean:false"
                                })
                        }
                    } else if (!hasBoolean && currentMode == FeatureMode.APP) {
                        OutlinedTextField(
                            value = argValue,
                            onValueChange = { argValue = it },
                            label = { Text(stringResource(R.string.config_value)) },
                            modifier = Modifier.fillMaxWidth()
                        )
                        featureEnabled = true
                    } else {
                        featureEnabled = true
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                // 若描述修改为空，则删除用户映射
                if (featureDescription.isEmpty()) {
                    if (currentMode == FeatureMode.APP) {
                        AppFeatureMappings.getInstance().removeUserMapping(context, featureName)
                    } else {
                        OplusFeatureMappings.removeUserMapping(context, featureName)
                    }
                } else {
                    // 检查是否与预设映射匹配
                    val matchPreset = if (currentMode == FeatureMode.APP) {
                        AppFeatureMappings.getInstance()
                            .isMatchingPresetDescription(context, featureName, featureDescription)
                    } else {
                        OplusFeatureMappings.isMatchingPresetDescription(
                            context,
                            featureName,
                            featureDescription
                        )
                    }

                    // 检查是否存在云端配置
                    val hasCloudConfig = if (currentMode == FeatureMode.APP) {
                        AppFeatureMappings.getInstance().isMatchingCloudDescription(context, featureName)
                    } else {
                        OplusFeatureMappings.isMatchingCloudDescription(context, featureName)
                    }

                    // 只有在不匹配预设描述且不存在云端配置时才保存用户映射
                    if (!matchPreset && !hasCloudConfig) {
                        if (currentMode == FeatureMode.APP) {
                            AppFeatureMappings.getInstance()
                                .saveUserMapping(context, featureName, featureDescription)
                        } else {
                            OplusFeatureMappings.saveUserMapping(
                                context,
                                featureName,
                                featureDescription
                            )
                        }
                    }
                }
                onConfirm(
                    featureName,
                    featureDescription,
                    featureEnabled,
                    if (argValue.isBlank()) null else argValue
                )
                // force ui update even if feature list unchanged
                // handled outside
            }) {
                Text(text = stringResource(id = android.R.string.ok))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(text = stringResource(id = android.R.string.cancel)) }
        }
    )
} 