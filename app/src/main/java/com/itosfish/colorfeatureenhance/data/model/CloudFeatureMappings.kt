package com.itosfish.colorfeatureenhance.data.model

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.itosfish.colorfeatureenhance.utils.CLog
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * 云端特性描述映射管理类
 * 使用 SharedPreferences 持久化存储云端下载的多语言特性描述映射
 * 
 * 存储格式：feature_key → {"zh":"中文描述","en":"English description"}
 */
class CloudFeatureMappings private constructor(context: Context) {
    
    companion object {
        private const val PREFS_NAME = "cloud_feature_mappings"
        private const val KEY_REMOTE_VERSION = "remote_version"
        private const val TAG = "CloudFeatureMappings"
        
        @Volatile
        private var INSTANCE: CloudFeatureMappings? = null
        
        fun getInstance(context: Context): CloudFeatureMappings {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: CloudFeatureMappings(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
    
    private val prefs: SharedPreferences = context.getSharedPreferences(
        PREFS_NAME, Context.MODE_PRIVATE
    )
    
    private val json = Json { 
        ignoreUnknownKeys = true
        encodeDefaults = false
    }
    
    /**
     * 多语言描述数据类
     */
    @Serializable
    data class MultiLanguageDescription(
        val zh: String? = null,
        val en: String? = null
    )
    
    /**
     * 批量保存特性映射
     * @param mappings 特性名称到多语言描述的映射
     */
    fun saveMappings(mappings: Map<String, MultiLanguageDescription>) {
        try {
            prefs.edit(commit = true) {
                mappings.forEach { (featureName, description) ->
                    val jsonString = json.encodeToString(description)
                    putString(featureName, jsonString)
                }
            }
            CLog.i(TAG, "批量保存云端映射: ${mappings.size} 个特性")
        } catch (e: Exception) {
            CLog.e(TAG, "批量保存云端映射失败", e)
        }
    }
    
    /**
     * 获取特性的描述，支持语言回退机制
     * @param featureName 特性名称
     * @param language 目标语言代码（如 "zh", "en"）
     * @return 描述字符串，如果不存在则返回null
     */
    fun getDescription(featureName: String, language: String): String? {
        try {
            val jsonString = prefs.getString(featureName, null) ?: return null
            val description = json.decodeFromString<MultiLanguageDescription>(jsonString)
            
            // 优先返回指定语言的描述
            val targetDescription = when (language.lowercase()) {
                "zh", "zh-cn", "zh-hans" -> description.zh
                "en", "en-us" -> description.en
                else -> description.en // 默认回退到英文
            }
            
            // 如果目标语言不存在，回退到英文
            if (targetDescription != null && targetDescription.isNotEmpty()) {
                return targetDescription
            }
            
            // 如果英文也不存在，尝试中文
            if (description.en != null && description.en.isNotEmpty()) {
                return description.en
            }
            
            if (description.zh != null && description.zh.isNotEmpty()) {
                return description.zh
            }
            
            return null
            
        } catch (e: Exception) {
            CLog.e(TAG, "获取云端描述失败: $featureName", e)
            return null
        }
    }
    
    /**
     * 检查特性是否存在云端映射
     * @param featureName 特性名称
     * @return 是否存在映射
     */
    fun hasMapping(featureName: String): Boolean {
        return prefs.contains(featureName) && prefs.getString(featureName, null) != null
    }
    
    /**
     * 删除特性映射
     * @param featureName 特性名称
     */
    fun removeMapping(featureName: String) {
        prefs.edit(commit = true) {
            remove(featureName)
        }
        CLog.d(TAG, "删除云端映射: $featureName")
    }
    
    /**
     * 保存远程配置版本
     * @param version 版本字符串
     */
    fun saveRemoteVersion(version: String) {
        prefs.edit(commit = true) {
            putString(KEY_REMOTE_VERSION, version)
        }
        CLog.d(TAG, "保存远程版本: $version")
    }
}
