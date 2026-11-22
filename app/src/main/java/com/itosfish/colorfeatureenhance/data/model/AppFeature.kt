package com.itosfish.colorfeatureenhance.data.model

import kotlinx.serialization.Serializable

/**
 * 表示特性的子节点，如 <StringList args="xxx"/> 或 <StringList name="pkgs" args="xxx"/>
 */
@Serializable
data class FeatureSubNode(
    val type: String,  // 如 "StringList"
    val name: String?, // 如 "pkgs"，可能为空
    val args: String   // 如 "com.xxx.yyy"
)

@Serializable
data class AppFeature(
    val name: String,
    val enabled: Boolean = true,
    /** 
     * 原始 args 字符串，如 "int:1", "String:xxxxx"，boolean 情况可为空 
     * 对于简单特性，这个字段存储 args 属性值
     */
    val args: String? = null,
    /**
     * 子节点列表，用于复杂特性
     * 如 <StringList args="xxx"/> 或 <StringList name="pkgs" args="xxx"/>
     */
    val subNodes: List<FeatureSubNode> = emptyList()
) {
    val isComplex: Boolean
        get() = subNodes.isNotEmpty()

    /**
     * 判断是否为简单特性（无子节点）
     */
    val isSimple: Boolean get() = !isComplex
} 