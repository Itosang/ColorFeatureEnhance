package com.itosfish.colorfeatureenhance.utils

import android.util.Base64
import com.itosfish.colorfeatureenhance.FeatureMode
import com.itosfish.colorfeatureenhance.MainActivity.Companion.app

/**
 * 读写模块目录里的配置文件。
 */
object ModuleConfig {
    const val MODULE_ID = "ColorOSFeaturesEnhance"
    private const val MODULE_DIR = "/data/adb/modules/$MODULE_ID"

    private const val APP_FILE = "my_product/etc/extension/com.oplus.app-features.xml"
    private const val OPLUS_FILE = "my_product/etc/extension/com.oplus.oplus-feature.xml"
    private const val REGION_FILE = "my_region/etc/extension/com.oplus.oplus-feature.xml"

    private fun relative(mode: FeatureMode): String = when (mode) {
        FeatureMode.APP -> APP_FILE
        FeatureMode.OPLUS -> OPLUS_FILE
        FeatureMode.REGION -> REGION_FILE
    }

    fun file(mode: FeatureMode): String = "$MODULE_DIR/${relative(mode)}"

    fun deviceFile(mode: FeatureMode): String = "/${relative(mode)}"

    fun isInstalled(): Boolean = CSU.fileExists("$MODULE_DIR/module.prop")

    /**
     * 读用户设备上的当前配置（已挂载时读到的是上次保存的内容）。
     */
    fun readDevice(mode: FeatureMode): String {
        val result = CSU.runWithSu("cat \"${deviceFile(mode)}\"")
        return if (result.exitCode == 0) result.output else ""
    }

    /**
     * 读当前生效的配置：模块目录里的文件安装时从设备拷来、保存时更新，
     * 是设备配置的编辑副本；没有则直接读设备路径。
     */
    fun readEffective(mode: FeatureMode): String {
        return if (CSU.fileExists(file(mode))) readText(mode) else readDevice(mode)
    }

    fun readText(mode: FeatureMode): String {
        val result = CSU.runWithSu("cat \"${file(mode)}\"")
        return if (result.exitCode == 0) result.output else ""
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
 * 记录模块首次读取时的特性名，用来区分系统配置和后来新增的。
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
}
