package com.itosfish.colorfeatureenhance.data.model

/**
 * 特性分组，将相同描述或属于同一功能的特性组合在一起
 * @param nameResId 显示名称的资源ID
 * @param features 组内所有底层特性
 */
data class FeatureGroup(
    val nameResId: Int,
    val features: List<AppFeature>
) {
    /**
     * 组内是否有任何一个特性处于启用状态。
     * 只要组内有开启的特性，对外即呈现为开启，确保用户可以统一关闭整个功能。
     */
    val isEnabled: Boolean get() = features.any { it.enabled }

    /**
     * 组内是否全部特性均已启用
     */
    val isFullyEnabled: Boolean get() = features.isNotEmpty() && features.all { it.enabled }

    /**
     * 将组内全部特性的启用状态统一设为指定值
     */
    fun withEnabled(enabled: Boolean): FeatureGroup {
        return copy(
            features = features.map { it.copy(enabled = enabled) }
        )
    }
}
