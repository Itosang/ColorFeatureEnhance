#!/system/bin/sh
# post-fs-data.sh：开机早期递归 bind mount。
# 把模块内 my_product/ 和 my_region/etc/extension/ 下的所有文件
# 挂载到系统同路径（目标必须已存在，不存在则跳过）。

MODDIR=${0%/*}

for DIR in my_product my_region/etc/extension; do
    [ -d "$MODDIR/$DIR" ] || continue
    find "$MODDIR/$DIR" -type f | while read -r SRC; do
        REL=${SRC#"$MODDIR/"}
        [ -f "/$REL" ] && mount --bind "$SRC" "/$REL"
    done
done

exit 0
