package com.example.learnandroid.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.os.RemoteCallbackList
import android.os.RemoteException
import android.util.Log
import com.example.learnandroid.ITaskCallback
import com.example.learnandroid.ITaskManagerService
import com.example.learnandroid.model.Task
import com.example.learnandroid.model.TaskStatus
import java.util.concurrent.*

/**
 * 任务管理服务 - 运行在主进程中
 */
class TaskManagerService : Service() {
    
    companion object {
        private const val TAG = "TaskManagerService"
        private const val MAX_CONCURRENT_TASKS = 3 // 最大并发任务数
    }
    
    // 线程池执行器
    private val executor = ThreadPoolExecutor(
        2, // 核心线程数
        MAX_CONCURRENT_TASKS, // 最大线程数
        60L, // 空闲线程存活时间
        TimeUnit.SECONDS,
        LinkedBlockingQueue<Runnable>(), // 任务队列
        ThreadFactory { r ->
            Thread(r, "TaskManager-${Thread.currentThread().id}")
        }
    )
    
    // 任务存储
    private val tasks = ConcurrentHashMap<Int, Task>()
    
    // 回调列表
    private val callbacks = RemoteCallbackList<ITaskCallback>()
    
    // 正在运行的任务Future
    private val runningTasks = ConcurrentHashMap<Int, Future<*>>()
    
    private val binder = object : ITaskManagerService.Stub() {
        
        override fun submitTask(taskName: String?, duration: Int): Int {
            Log.d(TAG, "submitTask: $taskName, duration: $duration")
            
            val task = Task(
                id = Task.generateId(),
                name = taskName ?: "未命名任务",
                duration = duration
            )
            
            tasks[task.id] = task
            
            // 提交任务到线程池
            val future = executor.submit {
                executeTask(task)
            }
            
            runningTasks[task.id] = future
            
            return task.id
        }
        
        override fun cancelTask(taskId: Int): Boolean {
            Log.d(TAG, "cancelTask: $taskId")
            
            val task = tasks[taskId] ?: return false
            val future = runningTasks[taskId]
            
            if (future != null && !future.isDone) {
                future.cancel(true)
                task.status = TaskStatus.CANCELLED
                task.endTime = System.currentTimeMillis()
                
                notifyTaskCancelled(taskId)
                runningTasks.remove(taskId)
                return true
            }
            
            return false
        }
        
        override fun getTaskStatus(taskId: Int): String? {
            val task = tasks[taskId]
            return task?.let { "${it.name}: ${it.getStatusString()}" }
        }
        
        override fun getAllTasksStatus(): MutableList<String> {
            return tasks.values.map { task ->
                "ID:${task.id} ${task.name}: ${task.getStatusString()} (${task.getDurationString()})"
            }.toMutableList()
        }
        
        override fun registerCallback(callback: ITaskCallback?) {
            Log.d(TAG, "registerCallback")
            callback?.let { callbacks.register(it) }
        }
        
        override fun unregisterCallback(callback: ITaskCallback?) {
            Log.d(TAG, "unregisterCallback")
            callback?.let { callbacks.unregister(it) }
        }
        
        override fun getRunningTaskCount(): Int {
            return runningTasks.size
        }
        
        override fun clearCompletedTasks() {
            Log.d(TAG, "clearCompletedTasks")
            val completedTasks = tasks.values.filter { 
                it.status == TaskStatus.COMPLETED || 
                it.status == TaskStatus.FAILED || 
                it.status == TaskStatus.CANCELLED 
            }
            
            completedTasks.forEach { task ->
                tasks.remove(task.id)
                runningTasks.remove(task.id)
            }
        }
    }
    
    override fun onBind(intent: Intent?): IBinder {
        Log.d(TAG, "onBind")
        return binder
    }
    
    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "onCreate")
    }
    
    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy")
        
        // 关闭线程池
        executor.shutdown()
        try {
            if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                executor.shutdownNow()
            }
        } catch (e: InterruptedException) {
            executor.shutdownNow()
        }
        
        // 清理回调
        callbacks.kill()
    }
    
    /**
     * 执行任务
     */
    private fun executeTask(task: Task) {
        try {
            Log.d(TAG, "开始执行任务: ${task.name}")
            
            // 更新任务状态为运行中
            task.status = TaskStatus.RUNNING
            task.startTime = System.currentTimeMillis()
            notifyTaskStarted(task.id, task.name)
            
            // 模拟任务执行过程
            val totalSteps = 100
            val stepDuration = (task.duration * 1000) / totalSteps // 每步的时间（毫秒）
            
            for (i in 1..totalSteps) {
                // 检查任务是否被取消
                if (Thread.currentThread().isInterrupted) {
                    task.status = TaskStatus.CANCELLED
                    task.endTime = System.currentTimeMillis()
                    notifyTaskCancelled(task.id)
                    return
                }
                
                // 模拟工作
                Thread.sleep(stepDuration.toLong())
                
                // 更新进度
                task.progress = i
                notifyTaskProgress(task.id, i)
            }
            
            // 任务完成
            task.status = TaskStatus.COMPLETED
            task.progress = 100
            task.endTime = System.currentTimeMillis()
            task.result = "任务 ${task.name} 执行成功！"
            
            notifyTaskCompleted(task.id, task.result)
            runningTasks.remove(task.id)
            
            Log.d(TAG, "任务完成: ${task.name}")
            
        } catch (e: InterruptedException) {
            Log.d(TAG, "任务被中断: ${task.name}")
            task.status = TaskStatus.CANCELLED
            task.endTime = System.currentTimeMillis()
            notifyTaskCancelled(task.id)
            runningTasks.remove(task.id)
            
        } catch (e: Exception) {
            Log.e(TAG, "任务执行失败: ${task.name}", e)
            task.status = TaskStatus.FAILED
            task.endTime = System.currentTimeMillis()
            task.error = e.message ?: "未知错误"
            
            notifyTaskFailed(task.id, task.error)
            runningTasks.remove(task.id)
        }
    }
    
    /**
     * 通知任务开始
     */
    private fun notifyTaskStarted(taskId: Int, taskName: String) {
        val count = callbacks.beginBroadcast()
        try {
            for (i in 0 until count) {
                try {
                    callbacks.getBroadcastItem(i).onTaskStarted(taskId, taskName)
                } catch (e: RemoteException) {
                    Log.e(TAG, "通知任务开始失败", e)
                }
            }
        } finally {
            callbacks.finishBroadcast()
        }
    }
    
    /**
     * 通知任务进度
     */
    private fun notifyTaskProgress(taskId: Int, progress: Int) {
        val count = callbacks.beginBroadcast()
        try {
            for (i in 0 until count) {
                try {
                    callbacks.getBroadcastItem(i).onTaskProgress(taskId, progress)
                } catch (e: RemoteException) {
                    Log.e(TAG, "通知任务进度失败", e)
                }
            }
        } finally {
            callbacks.finishBroadcast()
        }
    }
    
    /**
     * 通知任务完成
     */
    private fun notifyTaskCompleted(taskId: Int, result: String) {
        val count = callbacks.beginBroadcast()
        try {
            for (i in 0 until count) {
                try {
                    callbacks.getBroadcastItem(i).onTaskCompleted(taskId, result)
                } catch (e: RemoteException) {
                    Log.e(TAG, "通知任务完成失败", e)
                }
            }
        } finally {
            callbacks.finishBroadcast()
        }
    }
    
    /**
     * 通知任务失败
     */
    private fun notifyTaskFailed(taskId: Int, error: String) {
        val count = callbacks.beginBroadcast()
        try {
            for (i in 0 until count) {
                try {
                    callbacks.getBroadcastItem(i).onTaskFailed(taskId, error)
                } catch (e: RemoteException) {
                    Log.e(TAG, "通知任务失败失败", e)
                }
            }
        } finally {
            callbacks.finishBroadcast()
        }
    }
    
    /**
     * 通知任务取消
     */
    private fun notifyTaskCancelled(taskId: Int) {
        val count = callbacks.beginBroadcast()
        try {
            for (i in 0 until count) {
                try {
                    callbacks.getBroadcastItem(i).onTaskCancelled(taskId)
                } catch (e: RemoteException) {
                    Log.e(TAG, "通知任务取消失败", e)
                }
            }
        } finally {
            callbacks.finishBroadcast()
        }
    }
}