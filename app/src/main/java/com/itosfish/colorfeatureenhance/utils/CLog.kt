package com.itosfish.colorfeatureenhance.utils

import android.util.Log
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.concurrent.ConcurrentLinkedQueue

object CLog {
    data class LogEntry(
        val timestamp: Long,
        val level: String,
        val tag: String,
        val message: String,
        val throwable: Throwable? = null
    )

    private val logQueue = ConcurrentLinkedQueue<LogEntry>()
    private const val MAX_LOG_ENTRIES = 5000

    private val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")
    private val zone = ZoneId.systemDefault()

    fun i(tag: String, message: String) {
        Log.i(tag, message)
        addLogEntry("INFO", tag, message)
    }

    fun e(tag: String, message: String) {
        Log.e(tag, message)
        addLogEntry("ERROR", tag, message)
    }

    fun e(tag: String, message: String, throwable: Throwable) {
        Log.e(tag, message, throwable)
        addLogEntry("ERROR", tag, message, throwable)
    }

    fun d(tag: String, message: String) {
        Log.d(tag, message)
        addLogEntry("DEBUG", tag, message)
    }

    fun w(tag: String, message: String) {
        Log.w(tag, message)
        addLogEntry("WARN", tag, message)
    }

    fun w(tag: String, message: String, throwable: Throwable) {
        Log.w(tag, message, throwable)
        addLogEntry("WARN", tag, message, throwable)
    }

    private fun addLogEntry(
        level: String,
        tag: String,
        message: String,
        throwable: Throwable? = null
    ) {
        logQueue.offer(LogEntry(System.currentTimeMillis(), level, tag, message, throwable))
        while (logQueue.size > MAX_LOG_ENTRIES) {
            logQueue.poll()
        }
    }

    fun getAllLogs(): List<LogEntry> = logQueue.toList()

    fun getFormattedLogs(): String {
        val logs = getAllLogs()
        if (logs.isEmpty()) return "暂无日志记录"

        val builder = StringBuilder()
        builder.append("ColorFeatureEnhance 日志导出\n")
        builder.append("导出时间: ${LocalDateTime.now().format(formatter)}\n")
        builder.append("日志条目数: ${logs.size}\n")
        builder.append("=".repeat(50)).append("\n\n")

        logs.forEach { entry ->
            val time = formatter.format(Instant.ofEpochMilli(entry.timestamp).atZone(zone))
            builder.append("[$time] [${entry.level}] ${entry.tag}: ${entry.message}\n")
            entry.throwable?.let { throwable ->
                builder.append("异常信息: ${throwable.message}\n")
                builder.append("异常堆栈:\n")
                throwable.stackTrace.forEach { builder.append("  at $it\n") }
                builder.append("\n")
            }
        }
        return builder.toString()
    }
}
