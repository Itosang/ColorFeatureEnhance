package com.itosfish.colorfeatureenhance.data.model

/**
 * 特性分组，将相同描述的特性组合在一起
 * @param nameResId 显示名称的资源ID
 * @param features 组内所有特性
 */
data class FeatureGroup(
    val nameResId: Int,
    val features: List<AppFeature>
) {
    /**
     * 组内是否所有特性都已启用
     */
    val isEnabled: Boolean get() = features.all { it.enabled }

    /**
     * 根据开启状态创建新的分组
     */
    fun withEnabled(enabled: Boolean): FeatureGroup {
        return copy(
            features = features.map { it.copy(enabled = enabled) }
        )
    }
} 