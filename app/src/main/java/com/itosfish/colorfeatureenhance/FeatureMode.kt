package com.itosfish.colorfeatureenhance

/**
 * 表示当前正在编辑的配置模式
 */
enum class FeatureMode {
    /**
     * my_product/etc/extension/com.oplus.app-features.xml
     */
    APP,

    /**
     * my_product/etc/extension/com.oplus.oplus-feature.xml
     */
    OPLUS,

    /**
     * my_region/etc/extension/com.oplus.oplus-feature.xml
     */
    REGION
}
