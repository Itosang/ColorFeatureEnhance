#!/system/bin/sh
# customize.sh 在模块安装（刷入 ZIP）时执行：安装完成后的提示信息在这里输出。

# 从 module.prop 读取字段（兼容 CRLF 行尾）
prop() {
    grep "^$1=" "$MODPATH/module.prop" 2>/dev/null | head -n 1 | cut -d= -f2- | tr -d '\r'
}

ui_print "  $(prop name) $(prop version)  by $(prop author)"

# 由 post-fs-data.sh 递归挂载的目录
MISSING=""
for DIR in my_product my_region/etc/extension; do
    [ -d "$MODPATH/$DIR" ] || MISSING="$MISSING $DIR"
    [ -d "/${DIR}" ] || MISSING="$MISSING /$DIR"
done

if [ -n "$MISSING" ]; then
    ui_print "  警告: 缺少$MISSING"
    ui_print "        部分挂载不会生效, 可能不是适配机型"
fi

ui_print "  开机 bind mount 模块 my_product/ 与 extension/, 重启生效"

exit 0
