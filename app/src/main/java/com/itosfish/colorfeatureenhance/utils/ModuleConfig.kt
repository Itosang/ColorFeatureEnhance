package com.itosfish.colorfeatureenhance.utils

import android.util.Base64
import com.itosfish.colorfeatureenhance.FeatureMode
import com.itosfish.colorfeatureenhance.MainActivity.Companion.app

/**
 * 直接读写已安装模块目录中的配置，不再复制系统文件、不再维护 working/merged 目录。
 */
object ModuleConfig {
    const val MODULE_ID = "ColorOSFeaturesEnhance"
    private const val MODULE_DIR = "/data/adb/modules/$MODULE_ID"

    private const val APP_FILE = "my_product/etc/extension/com.oplus.app-features.xml"
    private const val OPLUS_FILE = "my_product/etc/extension/com.oplus.oplus-feature.xml"
    private const val REGION_FILE = "my_region/etc/extension/com.oplus.oplus-feature.xml"

    fun file(mode: FeatureMode): String {
        val relative = when (mode) {
            FeatureMode.APP -> APP_FILE
            FeatureMode.OPLUS -> OPLUS_FILE
            FeatureMode.REGION -> REGION_FILE
        }
        return "$MODULE_DIR/$relative"
    }

    fun isInstalled(): Boolean = CSU.fileExists("$MODULE_DIR/module.prop")

    fun readText(mode: FeatureMode): String {
        return CSU.runWithSu("cat \"${file(mode)}\"").output
    }

    fun writeText(mode: FeatureMode, text: String): Boolean {
        return writePath(file(mode), text)
    }

    fun writePath(path: String, text: String): Boolean {
        val encoded = Base64.encodeToString(text.toByteArray(Charsets.UTF_8), Base64.NO_WRAP)
        val parent = path.substringBeforeLast('/')
        val command = buildString {
            append("mkdir -p \"$parent\" ; ")
            append("printf '%s' '$encoded' | base64 -d > \"$path\" ; ")
            append("chmod 644 \"$path\"")
        }
        val result = CSU.runWithSu(command)
        if (result.exitCode != 0) {
            CLog.e("ModuleConfig", "写入失败: $path, output=${result.output}")
        }
        return result.exitCode == 0
    }
}

/**
 * 第一次读取模块配置时记录特性名称，之后用于区分“系统自带”和“应用新增”。
 * 只保存名称，不复制配置文件。
 */
object OriginalNames {
    private const val PREFS_NAME = "original_feature_names"

    private fun prefs() = app.getSharedPreferences(PREFS_NAME, 0)

    private fun key(mode: FeatureMode): String = "names_${mode.name}"

    fun get(mode: FeatureMode): Set<String> {
        return prefs().getStringSet(key(mode), emptySet()) ?: emptySet()
    }

    fun getOrInit(mode: FeatureMode, names: Set<String>): Set<String> {
        val stored = prefs().getStringSet(key(mode), null)
        if (stored != null) return stored

        prefs().edit().putStringSet(key(mode), names).apply()
        return names
    }

    fun clear(mode: FeatureMode) {
        prefs().edit().remove(key(mode)).apply()
    }
}
