package com.itosfish.colorfeatureenhance.utils

import android.widget.Toast
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.itosfish.colorfeatureenhance.MainActivity.Companion.app
import com.itosfish.colorfeatureenhance.R
import java.io.BufferedReader
import java.io.DataOutputStream
import java.io.InputStreamReader
import kotlin.system.exitProcess

data class ShellResult(
    val output: String,
    val exitCode: Int
)

object CSU {
    const val NONE = -1
    const val MAGISK = 0
    const val KSU = 1
    const val APATCH = 2

    var suType: Int = NONE
        private set

    /**
     * 安装模块。
     *
     * @param modulePath 模块的路径。
     * @return 如果安装成功则返回 true，否则返回 false。
     */
    fun installModule(
        modulePath: String
    ): Boolean {
        // 构建 Shell 命令：
        // 根据不同 Root 实现选择不同 ci 命令
        val command = when (suType) {
            MAGISK -> {
                // 使用 Magisk 安装模块的逻辑
                "magisk --install-module $modulePath"
            }
            KSU -> {
                // 使用 KSU 安装模块的逻辑
                "ksud module install $modulePath"
            }
            APATCH -> {
                // 使用 APatch 安装模块的逻辑
                "apd module install $modulePath"
            }
            else -> return false
        }

        // 使用 runWithSu 执行命令并获取返回值
        // 根据返回值判断模块安装是否成功
        runWithSu(command).let { result ->
            return result.exitCode == 0
        }
    }

    /**
     * 判断指定命令是否存在。
     *
     * @param command 要检查的命令。
     * @return 如果文件存在则返回 true，否则返回 false。
     */
    fun which(command: String): Boolean {
        // 构建 Shell 命令：
        // 使用 which 来检查命令是否存在
        val command = "which $command"

        // 使用 runWithSu 执行命令并获取返回值
        // 根据返回值判断命令是否存在
        return runWithSu(command).exitCode == 0
    }

    /**
     * 判断指定文件是否存在。
     *
     * @param filePath 要检查的文件路径。
     * @return 如果文件存在则返回 true，否则返回 false。
     */
    fun fileExists(filePath: String): Boolean {
        // 构建 Shell 命令：
        // 使用 if 语句和 [ -e ] 来检查文件是否存在
        // 如果存在，输出 "exists"，否则输出 "not exists"
        val command = "[ -e \"$filePath\" ]"

        // 使用 runWithSu 执行命令并获取返回值
        // 根据返回值判断文件是否存在
        return runWithSu(command).exitCode == 0
    }

    /**
     * 判断目录是否存在。
     * @param dirPath 目录路径
     * @return true 存在，false 不存在
     */
    fun dirExists(dirPath: String): Boolean {
        val command = "[ -d \"$dirPath\" ]"
        return runWithSu(command).exitCode == 0
    }

    // 检查是否具有root权限
    fun isRooted(): Boolean {
        return if (checkRootMethod()) {
            val magisk = which("magisk")
            val ksu = which("ksud")
            val ap = which("apd")
            if (magisk && ksu || magisk && ap || ksu && ap) {
                CLog.e("CSU", "不支持多个 Root 实现。 {Magisk: $magisk, KSU: $ksu, APatch: $ap}")
                exitProcess(255)
            }
            suType = when {
                magisk -> {
                    CLog.i("CSU", "检测到 Magisk Root 实现")
                    MAGISK
                }
                ksu -> {
                    CLog.i("CSU", "检测到 KernelSU Root 实现")
                    KSU
                }
                ap -> {
                    CLog.i("CSU", "检测到 APatch Root 实现")
                    APATCH
                }
                else -> NONE
            }
            true
        } else false
    }

    private fun checkRootMethod(): Boolean {
        try {
            val process = Runtime.getRuntime().exec(arrayOf("su", "-c", "id"))
            val reader = BufferedReader(InputStreamReader(process.inputStream))
            val result = reader.readLine()
            return result != null && result.contains("uid=0")  // 0 是 root 的 UID
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return false
    }

    /**
     * 执行具有超级用户权限的 Shell 命令，并返回命令的输出结果。
     *
     * @param cmd 要执行的 Shell 命令，可以包含多个命令，以分号或换行符分隔。
     * @return Shell 命令的输出结果。
     */
    fun runWithSu(cmd: String): ShellResult {
        val output = StringBuilder()
        var exitCode = -1
        try {
            val commandPreview = if (cmd.length > 300) "${cmd.take(300)}..." else cmd
            CLog.i("APP_SHELL", "执行命令: $commandPreview")
            val process = ProcessBuilder("su").redirectErrorStream(true).start()

            runShellScript(process, cmd)

            BufferedReader(InputStreamReader(process.inputStream)).use { reader ->
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    output.append(line).append("\n")
                }
            }

            exitCode = process.waitFor()
            val preview = if (output.length > 200) "${output.take(200)}..." else output.toString()
            CLog.d("APP_SHELL", "退出码: $exitCode, 输出: $preview")
        } catch (e: Exception) {
            CLog.e("APP_SHELL", "执行 Shell 命令失败", e)
        }
        return ShellResult(output = output.toString(), exitCode = exitCode)
    }

    fun checkRoot() {
        val isRooted = isRooted()
        if (!isRooted) {
            CLog.i("MainActivity", "Device not rooted")
            MaterialAlertDialogBuilder(app)
                .setTitle(R.string.root_permission_title)
                .setMessage(R.string.root_permission_message)
                .setCancelable(false)
                .setNegativeButton(
                    R.string.root_permission_exit
                ) { _, _ ->
                    app.finish()
                }
                .setNeutralButton(R.string.root_permission_continue) { _, _ ->
                    Toast.makeText(app, R.string.root_permission_warning, Toast.LENGTH_LONG)
                        .show()
                }
                .show()
        }
    }

    /**
     * 向 Process 的输出流中写入 Shell 命令。
     *
     * @param process 需要写入命令的 Process 对象。
     * @param cmd 要执行的 Shell 命令，可以包含多个命令，以分号或换行符分隔。
     */
    private fun runShellScript(process: Process, cmd: String) {
        try {
            DataOutputStream(process.outputStream).use { outputStream ->
                outputStream.writeBytes("$cmd\n")
                outputStream.writeBytes("exit\n")
                outputStream.flush()
            }
        } catch (e: Exception) {
            CLog.e("APP_SHELL", "写入 Shell 命令失败", e)
            process.destroy()
        }
    }
}