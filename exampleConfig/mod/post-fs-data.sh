#!/system/bin/sh
# ColorOS Features Enhance - 配置同步与挂载
# post-fs-data 阶段：备份真实系统配置，再把 app 生成的配置 bind mount 回系统路径

MODDIR=${0%/*}

PRODUCT_SYSTEM_DIR="/my_product/etc/extension"
REGION_SYSTEM_DIR="/my_region/etc/extension"
APP_FEATURES_FILE="com.oplus.app-features.xml"
OPLUS_FEATURES_FILE="com.oplus.oplus-feature.xml"

MODULE_BASE="/data/adb/cos_feat_e"
PRODUCT_CONFIG_DIR="$MODULE_BASE/my_product/etc/extension"
REGION_CONFIG_DIR="$MODULE_BASE/my_region/etc/extension"
TEMP_PRODUCT_DIR="$MODULE_BASE/temp_configs/my_product"
TEMP_REGION_DIR="$MODULE_BASE/temp_configs/my_region"

log_info() {
    echo "[ColorFeatureEnhance] $(date '+%Y-%m-%d %H:%M:%S') $1" >> /cache/colorfeature_enhance.log
}

mkdir -p "$PRODUCT_CONFIG_DIR" "$REGION_CONFIG_DIR" "$TEMP_PRODUCT_DIR" "$TEMP_REGION_DIR"

log_info "=== 配置模块启动（post-fs-data）==="

# 挂载前保存真实系统配置，供 App 读取基线
copy_system_to_temp() {
    for FILE in "$APP_FEATURES_FILE" "$OPLUS_FEATURES_FILE"; do
        [ -f "$PRODUCT_SYSTEM_DIR/$FILE" ] || continue
        cp "$PRODUCT_SYSTEM_DIR/$FILE" "$TEMP_PRODUCT_DIR/$FILE" 2>/dev/null
    done

    if [ -f "$REGION_SYSTEM_DIR/$OPLUS_FEATURES_FILE" ]; then
        cp "$REGION_SYSTEM_DIR/$OPLUS_FEATURES_FILE" "$TEMP_REGION_DIR/$OPLUS_FEATURES_FILE" 2>/dev/null
    fi
}

# 模块目录还没有配置时，用系统配置兜底
ensure_default_configs() {
    if [ ! -f "$PRODUCT_CONFIG_DIR/$APP_FEATURES_FILE" ] && [ -f "$PRODUCT_SYSTEM_DIR/$APP_FEATURES_FILE" ]; then
        cp "$PRODUCT_SYSTEM_DIR/$APP_FEATURES_FILE" "$PRODUCT_CONFIG_DIR/"
    fi
    if [ ! -f "$PRODUCT_CONFIG_DIR/$OPLUS_FEATURES_FILE" ] && [ -f "$PRODUCT_SYSTEM_DIR/$OPLUS_FEATURES_FILE" ]; then
        cp "$PRODUCT_SYSTEM_DIR/$OPLUS_FEATURES_FILE" "$PRODUCT_CONFIG_DIR/"
    fi
    if [ ! -f "$REGION_CONFIG_DIR/$OPLUS_FEATURES_FILE" ] && [ -f "$REGION_SYSTEM_DIR/$OPLUS_FEATURES_FILE" ]; then
        cp "$REGION_SYSTEM_DIR/$OPLUS_FEATURES_FILE" "$REGION_CONFIG_DIR/"
    fi
}

# 递归挂载目录下的每个文件，只覆盖系统里真实存在的目标
mount_tree() {
    SOURCE_DIR="$1"
    TARGET_DIR="$2"
    [ -d "$SOURCE_DIR" ] || return 0
    find "$SOURCE_DIR" -type f | while read -r SOURCE_FILE; do
        RELATIVE_PATH=${SOURCE_FILE#"$SOURCE_DIR/"}
        TARGET_FILE="$TARGET_DIR/$RELATIVE_PATH"
        [ -f "$TARGET_FILE" ] || continue
        mount --bind "$SOURCE_FILE" "$TARGET_FILE"
        log_info "已挂载: $TARGET_FILE"
    done
}

copy_system_to_temp
ensure_default_configs

mount_tree "$MODULE_BASE/my_product" "/my_product"
mount_tree "$MODULE_BASE/my_region" "/my_region"

log_info "=== 配置模块完成 ==="
