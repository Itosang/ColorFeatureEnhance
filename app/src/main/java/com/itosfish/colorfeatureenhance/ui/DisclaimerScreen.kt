@file:OptIn(androidx.compose.material3.ExperimentalMaterial3ExpressiveApi::class)

package com.itosfish.colorfeatureenhance.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MediumFlexibleTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.itosfish.colorfeatureenhance.R
import com.itosfish.colorfeatureenhance.utils.DisclaimerManager
import kotlinx.coroutines.delay

/**
 * 免责声明屏幕
 * 显示免责声明内容，强制用户阅读5秒钟后才能同意
 */
@Composable
fun DisclaimerScreen(
    onAccepted: () -> Unit,
    onExit: () -> Unit
) {
    val context = LocalContext.current
    val disclaimerManager = remember { DisclaimerManager.getInstance(context) }
    
    var disclaimerText by remember { mutableStateOf("") }
    var countdown by remember { mutableIntStateOf(5) }
    var isCountdownFinished by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(true) }
    
    val scrollState = rememberScrollState()
    
    // 加载免责声明文本
    LaunchedEffect(Unit) {
        disclaimerText = disclaimerManager.getDisclaimerText()
        isLoading = false
    }
    
    // 倒计时逻辑
    LaunchedEffect(isLoading) {
        if (!isLoading) {
            while (countdown > 0) {
                delay(1000)
                countdown--
            }
            isCountdownFinished = true
        }
    }
    
    Scaffold(
        topBar = {
            MediumFlexibleTopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.disclaimer_title)
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                shape = MaterialTheme.shapes.extraLarge,
                color = MaterialTheme.colorScheme.surfaceContainerLow
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    if (isLoading) {
                        // 加载状态
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            LoadingIndicator()
                        }
                    } else {
                        // 滚动提示
                        Text(
                            text = stringResource(R.string.disclaimer_scroll_hint),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        
                        // 免责声明文本
                        Text(
                            text = disclaimerText,
                            style = MaterialTheme.typography.bodyMedium,
                            lineHeight = 20.sp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .verticalScroll(scrollState)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 底部按钮区域
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 倒计时提示
                if (!isCountdownFinished && !isLoading) {
                    Text(
                        text = stringResource(R.string.disclaimer_countdown, countdown),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                } else if (isCountdownFinished) {
                    Text(
                        text = stringResource(R.string.disclaimer_reading_time),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                }
                
                // 按钮行
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // 退出按钮
                    OutlinedButton(
                        onClick = onExit,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(stringResource(R.string.disclaimer_exit))
                    }
                    
                    // 同意按钮
                    Button(
                        onClick = {
                            disclaimerManager.markDisclaimerAccepted()
                            onAccepted()
                        },
                        enabled = isCountdownFinished,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(stringResource(R.string.disclaimer_accept))
                    }
                }
            }
        }
    }
}
