package com.example.learnandroid.model

import java.util.concurrent.atomic.AtomicInteger

/**
 * 任务状态枚举
 */
enum class TaskStatus {
    PENDING,    // 等待中
    RUNNING,    // 运行中
    COMPLETED,  // 已完成
    FAILED,     // 失败
    CANCELLED   // 已取消
}

/**
 * 任务数据类
 */
data class Task(
    val id: Int,
    val name: String,
    val duration: Int, // 任务持续时间（秒）
    var status: TaskStatus = TaskStatus.PENDING,
    var progress: Int = 0,
    var result: String = "",
    var error: String = "",
    val createTime: Long = System.currentTimeMillis(),
    var startTime: Long = 0,
    var endTime: Long = 0
) {
    companion object {
        private val idGenerator = AtomicInteger(1)
        
        fun generateId(): Int = idGenerator.getAndIncrement()
    }
    
    fun getStatusString(): String {
        return when (status) {
            TaskStatus.PENDING -> "等待中"
            TaskStatus.RUNNING -> "运行中 ($progress%)"
            TaskStatus.COMPLETED -> "已完成"
            TaskStatus.FAILED -> "失败: $error"
            TaskStatus.CANCELLED -> "已取消"
        }
    }
    
    fun getDurationString(): String {
        return when {
            endTime > 0 -> "${(endTime - startTime) / 1000}秒"
            startTime > 0 -> "${(System.currentTimeMillis() - startTime) / 1000}秒"
            else -> "${duration}秒"
        }
    }
}