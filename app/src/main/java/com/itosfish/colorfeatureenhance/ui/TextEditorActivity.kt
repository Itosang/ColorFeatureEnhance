package com.itosfish.colorfeatureenhance.ui

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.itosfish.colorfeatureenhance.R
import com.itosfish.colorfeatureenhance.ui.theme.ColorFeatureEnhanceTheme
import com.itosfish.colorfeatureenhance.utils.ModuleConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

class TextEditorActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val filePath = intent.getStringExtra(KEY_PATH) ?: run {
            finish()
            return
        }
        setContent {
            ColorFeatureEnhanceTheme {
                TextEditorScreen(filePath = filePath, onFinish = { finish() })
            }
        }
    }

    companion object {
        const val KEY_PATH = "filePath"
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TextEditorScreen(filePath: String, onFinish: () -> Unit) {
    val scope = rememberCoroutineScope()
    var content by remember { mutableStateOf("") }
    var isModified by remember { mutableStateOf(false) }
    val context = LocalContext.current

    // 读取文件内容
    LaunchedEffect(filePath) {
        kotlin.runCatching {
            content = File(filePath).readText()
            isModified = false
        }
    }

    BackHandler(enabled = true) {
        onFinish()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = { onFinish() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                },
                title = { Text(text = File(filePath).name) },
                actions = {
                    IconButton(onClick = {
                        scope.launch(Dispatchers.IO) {
                            kotlin.runCatching {
                                ModuleConfig.writePath(filePath, content)
                            }.onSuccess { isSynced: Boolean ->
                                launch(Dispatchers.Main) {
                                    val message = if (isSynced) {
                                        R.string.save_and_sync_success
                                    } else {
                                        R.string.save_success_sync_failed
                                    }
                                    Toast.makeText(context, context.getString(message), Toast.LENGTH_SHORT).show()
                                    isModified = false
                                }
                            }.onFailure { exception: Throwable ->
                                launch(Dispatchers.Main) {
                                    Toast.makeText(context, "保存失败: ${exception.message}", Toast.LENGTH_LONG).show()
                                }
                            }
                        }
                    }, enabled = isModified) {
                        Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "保存")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            OutlinedTextField(
                value = content,
                onValueChange = {
                    content = it
                    isModified = true
                },
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp),
                label = { Text(stringResource(R.string.config_content)) },
                // 默认使用较小字体以便在小屏幕上显示更多配置内容
                textStyle = LocalTextStyle.current.copy(fontSize = 12.sp, lineHeight = 16.sp),
                singleLine = false,
                maxLines = Int.MAX_VALUE
            )
        }
    }
} 