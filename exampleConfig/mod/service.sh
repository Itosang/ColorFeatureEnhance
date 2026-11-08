#!/system/bin/sh
# service.sh 在 late_start 服务阶段执行，不阻塞开机。

# 关闭温控服务
stop thermal-engine

exit 0
