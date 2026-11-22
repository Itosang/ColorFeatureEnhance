package com.itosfish.colorfeatureenhance.utils

import com.itosfish.colorfeatureenhance.FeatureMode
import com.itosfish.colorfeatureenhance.MainActivity.Companion.app
import com.itosfish.colorfeatureenhance.data.model.AppFeature
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * 记住每个模式见过的全部特性定义（含 args 与子节点）。
 * 特性被关闭（从配置移除或停用）后仍留在目录和列表中，开关显示为关，可随时再开启。
 * 只有用户主动删除时，才会从目录中彻底移除。
 */
object FeatureCatalog {
    private const val PREFS_NAME = "feature_catalog"
    private const val PREFS_CUSTOM_ADDED = "custom_added_features"
    private val json = Json { ignoreUnknownKeys = true }

    private fun key(mode: FeatureMode): String = "catalog_${mode.name}"
    private fun customKey(mode: FeatureMode): String = "custom_${mode.name}"

    fun all(mode: FeatureMode): List<AppFeature> {
        val raw = app.getSharedPreferences(PREFS_NAME, 0).getString(key(mode), null)
            ?: return emptyList()
        return try {
            json.decodeFromString<List<AppFeature>>(raw)
        } catch (e: Exception) {
            CLog.e("FeatureCatalog", "特性目录解析失败: $mode", e)
            emptyList()
        }
    }

    fun remember(mode: FeatureMode, features: List<AppFeature>) {
        if (features.isEmpty()) return
        val current = all(mode)
        // 新传入的定义优先（例如用户编辑修改后的 args），旧条目保留以支持关闭后再开启
        val merged = (features + current).distinctBy { it.name }
        try {
            val serialized = json.encodeToString(merged)
            app.getSharedPreferences(PREFS_NAME, 0).edit()
                .putString(key(mode), serialized)
                .commit()
        } catch (e: Exception) {
            CLog.e("FeatureCatalog", "特性目录保存失败: $mode", e)
        }
    }

    /**
     * 当用户主动在 UI 点击“删除特性”时调用，彻底从记忆库中注销
     */
    fun remove(mode: FeatureMode, names: Collection<String>) {
        if (names.isEmpty()) return
        val remaining = all(mode).filterNot { it.name in names }
        try {
            app.getSharedPreferences(PREFS_NAME, 0).edit()
                .putString(key(mode), json.encodeToString(remaining))
                .commit()
        } catch (e: Exception) {
            CLog.e("FeatureCatalog", "删除特性失败: $mode", e)
        }
        unmarkCustomAdded(mode, names)
    }

    /**
     * 标记由用户手动在 App 中添加的特性
     */
    fun markCustomAdded(mode: FeatureMode, name: String) {
        val prefs = app.getSharedPreferences(PREFS_CUSTOM_ADDED, 0)
        val set = prefs.getStringSet(customKey(mode), emptySet())?.toMutableSet() ?: mutableSetOf()
        set.add(name)
        prefs.edit().putStringSet(customKey(mode), set).commit()
    }

    fun getCustomAddedNames(mode: FeatureMode): Set<String> {
        return app.getSharedPreferences(PREFS_CUSTOM_ADDED, 0)
            .getStringSet(customKey(mode), emptySet()) ?: emptySet()
    }

    fun isCustomAdded(mode: FeatureMode, name: String): Boolean {
        return name in getCustomAddedNames(mode)
    }

    private fun unmarkCustomAdded(mode: FeatureMode, names: Collection<String>) {
        val prefs = app.getSharedPreferences(PREFS_CUSTOM_ADDED, 0)
        val set = prefs.getStringSet(customKey(mode), emptySet())?.toMutableSet() ?: mutableSetOf()
        set.removeAll(names.toSet())
        prefs.edit().putStringSet(customKey(mode), set).commit()
    }
}
