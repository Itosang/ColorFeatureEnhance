#!/system/bin/sh
# ColorOS Features Enhance - 开机挂载 working 配置
# 参考 exoplus：递归遍历目录，逐个文件 bind mount 到系统同路径。
# 只覆盖系统里真实存在的目标文件，不创建新文件。

MODULE_BASE="/data/adb/cos_feat_e"

log_info() {
    echo "[ColorFeatureEnhance] $(date '+%Y-%m-%d %H:%M:%S') $1" >> /cache/colorfeature_enhance.log
}

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

mount_tree "$MODULE_BASE/my_product" "/my_product"
mount_tree "$MODULE_BASE/my_region" "/my_region"

log_info "=== 配置挂载完成 ==="
