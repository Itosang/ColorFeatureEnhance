package com.itosfish.colorfeatureenhance

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.lifecycleScope
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.itosfish.colorfeatureenhance.data.repository.XmlFeatureRepository
import com.itosfish.colorfeatureenhance.data.repository.XmlOplusFeatureRepository
import com.itosfish.colorfeatureenhance.domain.FeatureRepository
import com.itosfish.colorfeatureenhance.ui.DisclaimerScreen
import com.itosfish.colorfeatureenhance.ui.FeatureConfigScreen
import com.itosfish.colorfeatureenhance.ui.theme.ColorFeatureEnhanceTheme
import com.itosfish.colorfeatureenhance.utils.CLog
import com.itosfish.colorfeatureenhance.utils.CSU
import com.itosfish.colorfeatureenhance.utils.DisclaimerManager
import com.itosfish.colorfeatureenhance.utils.ModuleAutoUpdater
import com.itosfish.colorfeatureenhance.utils.ModuleConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        app = this

        val disclaimerManager = DisclaimerManager.getInstance(this)

        setContent {
            ColorFeatureEnhanceTheme {
                var showDisclaimer by remember { mutableStateOf(disclaimerManager.shouldShowDisclaimer()) }

                if (showDisclaimer) {
                    DisclaimerScreen(
                        onAccepted = {
                            showDisclaimer = false
                            CLog.i("MainActivity", "用户已同意免责声明，继续应用启动")
                            initializeApp()
                        },
                        onExit = {
                            CLog.i("MainActivity", "用户拒绝免责声明，退出应用")
                            finish()
                        }
                    )
                } else {
                    var currentMode by remember { mutableStateOf(FeatureMode.APP) }

                    val configPath = ModuleConfig.file(currentMode)

                    // 根据模式记忆 repository，切换模式时重建
                    val repository: FeatureRepository = remember(currentMode) {
                        when (currentMode) {
                            FeatureMode.APP -> XmlFeatureRepository()
                            else -> XmlOplusFeatureRepository(currentMode)
                        }
                    }

                    FeatureConfigScreen(
                        configPath = configPath,
                        currentMode = currentMode,
                        onModeChange = { currentMode = it },
                        repository = repository
                    )
                }
            }
        }

        if (!disclaimerManager.shouldShowDisclaimer()) {
            initializeApp()
        }
    }

    /**
     * 初始化配置与模块。
     */
    private fun initializeApp() {
        CSU.checkRoot()

        // 检查模块更新
        lifecycleScope.launch {
            try {
                val updateResult = ModuleAutoUpdater.checkAndUpdateModule(showUserNotification = true)

                when (updateResult) {
                    is ModuleAutoUpdater.UpdateResult.Success -> {
                        CLog.i("MainActivity", "模块自动更新成功")
                    }
                    is ModuleAutoUpdater.UpdateResult.NoUpdateNeeded -> {
                        CLog.d("MainActivity", "模块无需更新")
                    }
                    is ModuleAutoUpdater.UpdateResult.Failed -> {
                        CLog.e("MainActivity", "模块自动更新失败: ${updateResult.reason}")

                        MaterialAlertDialogBuilder(this@MainActivity)
                            .setTitle(R.string.module_install_fail_title)
                            .setMessage(updateResult.reason)
                            .setPositiveButton(R.string.common_ok) { dialog, _ -> dialog.dismiss() }
                            .show()
                    }
                }
            } catch (e: Exception) {
                CLog.e("MainActivity", "模块自动更新过程中发生异常", e)
            }
        }

        // 检查模块是否安装
        lifecycleScope.launch(Dispatchers.IO) {
            if (ModuleConfig.isInstalled()) {
                CLog.i("MainActivity", "模块已安装")
            } else {
                CLog.w("MainActivity", "模块未安装，请先安装模块")
            }
        }
    }

    companion object {
        @SuppressLint("StaticFieldLeak")
        lateinit var app: MainActivity private set
    }
}

@Preview(showBackground = true)
@Composable
fun MainPreview() {
    ColorFeatureEnhanceTheme {
        FeatureConfigScreen(
            configPath = "",
            currentMode = FeatureMode.APP,
            onModeChange = {},
            repository = XmlFeatureRepository()
        )
    }
}
