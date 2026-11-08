#!/system/bin/sh
# ColorOS Features Enhance - 把开机时保存的系统配置复制到 App 基线目录

APP_DATA_DIR="/storage/emulated/0/Android/data/com.itosfish.colorfeatureenhance/files/configs"
BASELINE_DIR="$APP_DATA_DIR/system_baseline"
REGION_BASELINE_DIR="$BASELINE_DIR/my_region"

TEMP_PRODUCT_DIR="/data/adb/cos_feat_e/temp_configs/my_product"
TEMP_REGION_DIR="/data/adb/cos_feat_e/temp_configs/my_region"

APP_FEATURES_FILE="com.oplus.app-features.xml"
OPLUS_FEATURES_FILE="com.oplus.oplus-feature.xml"

log_info() {
    echo "[ColorFeatureEnhance-Copy] $(date '+%Y-%m-%d %H:%M:%S') $1" >> /cache/colorfeature_enhance.log
    echo "[ColorFeatureEnhance-Copy] $1"
}

copy_file() {
    SOURCE_FILE="$1"
    TARGET_DIR="$2"
    APP_UID="$3"

    [ -f "$SOURCE_FILE" ] || return 0

    FILE_NAME=${SOURCE_FILE##*/}
    cp "$SOURCE_FILE" "$TARGET_DIR/$FILE_NAME" 2>/dev/null || return 1
    chmod 660 "$TARGET_DIR/$FILE_NAME" 2>/dev/null
    chown "${APP_UID}:ext_data_rw" "$TARGET_DIR/$FILE_NAME" 2>/dev/null
    log_info "已复制基线: $TARGET_DIR/$FILE_NAME"
}

main() {
    APP_UID="$1"
    if [ -z "$APP_UID" ]; then
        log_info "未传入应用 UID，跳过复制"
        return 0
    fi

    mkdir -p "$BASELINE_DIR" "$REGION_BASELINE_DIR" 2>/dev/null

    copy_file "$TEMP_PRODUCT_DIR/$APP_FEATURES_FILE" "$BASELINE_DIR" "$APP_UID"
    copy_file "$TEMP_PRODUCT_DIR/$OPLUS_FEATURES_FILE" "$BASELINE_DIR" "$APP_UID"
    copy_file "$TEMP_REGION_DIR/$OPLUS_FEATURES_FILE" "$REGION_BASELINE_DIR" "$APP_UID"

    log_info "=== 基线复制完成 ==="
}

main "$@"
