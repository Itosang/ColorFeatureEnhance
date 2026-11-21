#!/system/bin/sh
# customize.sh 在模块安装（刷入 ZIP）时执行。
# 更新模块时优先继承用户在旧模块中已保存的配置，避免覆盖丢失；
# 首次安装时从设备系统原始路径拷贝作为编辑基底。

prop() {
    grep "^$1=" "$MODPATH/module.prop" 2>/dev/null | head -n 1 | cut -d= -f2- | tr -d '\r'
}

ui_print "  $(prop name) $(prop version)  by $(prop author)"

mkdir -p "$MODPATH/my_product/etc/extension"
mkdir -p "$MODPATH/my_region/etc/extension"

OLD_MOD="/data/adb/modules/ColorOSFeaturesEnhance"

copy_config() {
    REL="$1"
    SRC_OLD="$OLD_MOD/$REL"
    SRC_SYS="/$REL"
    DST="$MODPATH/$REL"

    mkdir -p "$(dirname "$DST")"
    if [ -f "$SRC_OLD" ]; then
        cp -f "$SRC_OLD" "$DST" && ui_print "  保留已有模块配置: $REL"
    elif [ -f "$SRC_SYS" ]; then
        cp -f "$SRC_SYS" "$DST" && ui_print "  已读取设备配置: $REL"
    else
        ui_print "  设备无此文件, 跳过: $REL"
    fi
}

copy_config "my_product/etc/extension/com.oplus.app-features.xml"
copy_config "my_product/etc/extension/com.oplus.oplus-feature.xml"
copy_config "my_region/etc/extension/com.oplus.oplus-feature.xml"

MISSING=""
for DIR in my_product my_region/etc/extension; do
    [ -d "$MODPATH/$DIR" ] || MISSING="$MISSING $DIR"
    [ -d "/${DIR}" ] || MISSING="$MISSING /$DIR"
done

if [ -n "$MISSING" ]; then
    ui_print "  警告: 缺少$MISSING"
    ui_print "        部分挂载不会生效, 可能不是适配机型"
fi

ui_print "  开机 bind mount 模块配置, 重启生效"

exit 0
