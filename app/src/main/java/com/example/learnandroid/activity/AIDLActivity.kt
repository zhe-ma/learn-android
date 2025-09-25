package com.example.learnandroid.activity

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.RemoteException
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.learnandroid.ITaskCallback
import com.example.learnandroid.ITaskManagerService
import com.example.learnandroid.R
import com.example.learnandroid.service.TaskManagerService
import java.text.SimpleDateFormat
import java.util.*

class AIDLActivity : AppCompatActivity() {
    
    companion object {
        private const val TAG = "AIDLActivity"
    }
    
    private var taskManagerService: ITaskManagerService? = null
    private var isServiceBound = false
    
    private lateinit var taskNameEdit: EditText
    private lateinit var taskDurationEdit: EditText
    private lateinit var submitTaskBtn: Button
    private lateinit var cancelTaskBtn: Button
    private lateinit var refreshStatusBtn: Button
    private lateinit var clearCompletedBtn: Button
    private lateinit var statusTextView: TextView
    private lateinit var scrollView: ScrollView
    
    private val handler = Handler(Looper.getMainLooper())
    private val dateFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
    
    // 服务连接
    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            Log.d(TAG, "Service connected")
            taskManagerService = ITaskManagerService.Stub.asInterface(service)
            isServiceBound = true
            
            // 注册回调
            try {
                taskManagerService?.registerCallback(taskCallback)
                updateUI("服务已连接")
                updateButtonStates()
            } catch (e: RemoteException) {
                Log.e(TAG, "注册回调失败", e)
            }
        }
        
        override fun onServiceDisconnected(name: ComponentName?) {
            Log.d(TAG, "Service disconnected")
            taskManagerService = null
            isServiceBound = false
            updateUI("服务已断开连接")
            updateButtonStates()
        }
    }
    
    // 任务回调
    private val taskCallback = object : ITaskCallback.Stub() {
        override fun onTaskStarted(taskId: Int, taskName: String?) {
            handler.post {
                updateUI("✅ 任务开始: ID=$taskId, 名称=$taskName")
                refreshTaskStatus()
            }
        }
        
        override fun onTaskProgress(taskId: Int, progress: Int) {
            handler.post {
                updateUI("📊 任务进度: ID=$taskId, 进度=$progress%")
            }
        }
        
        override fun onTaskCompleted(taskId: Int, result: String?) {
            handler.post {
                updateUI("✅ 任务完成: ID=$taskId, 结果=$result")
                refreshTaskStatus()
            }
        }
        
        override fun onTaskFailed(taskId: Int, error: String?) {
            handler.post {
                updateUI("❌ 任务失败: ID=$taskId, 错误=$error")
                refreshTaskStatus()
            }
        }
        
        override fun onTaskCancelled(taskId: Int) {
            handler.post {
                updateUI("⏹️ 任务取消: ID=$taskId")
                refreshTaskStatus()
            }
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_aidl)
        
        initViews()
        bindService()
    }
    
    override fun onDestroy() {
        super.onDestroy()
        unbindService()
    }
    
    private fun initViews() {
        taskNameEdit = findViewById(R.id.task_name_edit)
        taskDurationEdit = findViewById(R.id.task_duration_edit)
        submitTaskBtn = findViewById(R.id.submit_task_btn)
        cancelTaskBtn = findViewById(R.id.cancel_task_btn)
        refreshStatusBtn = findViewById(R.id.refresh_status_btn)
        clearCompletedBtn = findViewById(R.id.clear_completed_btn)
        statusTextView = findViewById(R.id.status_text_view)
        scrollView = findViewById(R.id.scroll_view)
        
        // 设置默认值
        taskNameEdit.setText("测试任务")
        taskDurationEdit.setText("5")
        
        // 设置点击事件
        submitTaskBtn.setOnClickListener { submitTask() }
        cancelTaskBtn.setOnClickListener { showCancelTaskDialog() }
        refreshStatusBtn.setOnClickListener { refreshTaskStatus() }
        clearCompletedBtn.setOnClickListener { clearCompletedTasks() }
        
        updateButtonStates()
        updateUI("正在连接服务...")
    }
    
    private fun bindService() {
        val intent = Intent(this, TaskManagerService::class.java)
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
    }
    
    private fun unbindService() {
        if (isServiceBound) {
            try {
                taskManagerService?.unregisterCallback(taskCallback)
            } catch (e: RemoteException) {
                Log.e(TAG, "取消注册回调失败", e)
            }
            unbindService(serviceConnection)
            isServiceBound = false
        }
    }
    
    private fun submitTask() {
        val taskName = taskNameEdit.text.toString().trim()
        val durationStr = taskDurationEdit.text.toString().trim()
        
        if (taskName.isEmpty()) {
            Toast.makeText(this, "请输入任务名称", Toast.LENGTH_SHORT).show()
            return
        }
        
        val duration = try {
            durationStr.toInt()
        } catch (e: NumberFormatException) {
            Toast.makeText(this, "请输入有效的持续时间", Toast.LENGTH_SHORT).show()
            return
        }
        
        if (duration <= 0 || duration > 60) {
            Toast.makeText(this, "持续时间应在1-60秒之间", Toast.LENGTH_SHORT).show()
            return
        }
        
        try {
            val taskId = taskManagerService?.submitTask(taskName, duration) ?: -1
            if (taskId > 0) {
                updateUI("📝 提交任务: ID=$taskId, 名称=$taskName, 持续时间=${duration}秒")
                // 清空输入框
                taskNameEdit.setText("")
                taskDurationEdit.setText("5")
            } else {
                Toast.makeText(this, "提交任务失败", Toast.LENGTH_SHORT).show()
            }
        } catch (e: RemoteException) {
            Log.e(TAG, "提交任务失败", e)
            Toast.makeText(this, "提交任务失败: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun showCancelTaskDialog() {
        val input = EditText(this)
        input.hint = "请输入要取消的任务ID"
        
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("取消任务")
            .setView(input)
            .setPositiveButton("取消任务") { _, _ ->
                val taskIdStr = input.text.toString().trim()
                if (taskIdStr.isNotEmpty()) {
                    try {
                        val taskId = taskIdStr.toInt()
                        cancelTask(taskId)
                    } catch (e: NumberFormatException) {
                        Toast.makeText(this, "请输入有效的任务ID", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .setNegativeButton("取消", null)
            .show()
    }
    
    private fun cancelTask(taskId: Int) {
        try {
            val success = taskManagerService?.cancelTask(taskId) ?: false
            if (success) {
                updateUI("⏹️ 取消任务请求已发送: ID=$taskId")
            } else {
                Toast.makeText(this, "取消任务失败，任务可能已完成或不存在", Toast.LENGTH_SHORT).show()
            }
        } catch (e: RemoteException) {
            Log.e(TAG, "取消任务失败", e)
            Toast.makeText(this, "取消任务失败: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun refreshTaskStatus() {
        try {
            val allStatus = taskManagerService?.allTasksStatus ?: emptyList()
            val runningCount = taskManagerService?.runningTaskCount ?: 0
            
            val statusText = StringBuilder()
            statusText.append("=== 任务状态总览 ===\n")
            statusText.append("当前运行任务数: $runningCount\n")
            statusText.append("总任务数: ${allStatus.size}\n\n")
            
            if (allStatus.isEmpty()) {
                statusText.append("暂无任务\n")
            } else {
                statusText.append("=== 任务列表 ===\n")
                allStatus.forEach { status ->
                    statusText.append("$status\n")
                }
            }
            
            updateUI(statusText.toString())
            
        } catch (e: RemoteException) {
            Log.e(TAG, "刷新任务状态失败", e)
            Toast.makeText(this, "刷新状态失败: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun clearCompletedTasks() {
        try {
            taskManagerService?.clearCompletedTasks()
            updateUI("🗑️ 已清除所有已完成的任务")
            refreshTaskStatus()
        } catch (e: RemoteException) {
            Log.e(TAG, "清除已完成任务失败", e)
            Toast.makeText(this, "清除任务失败: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun updateUI(message: String) {
        val timestamp = dateFormat.format(Date())
        val newText = "[$timestamp] $message\n${statusTextView.text}"
        statusTextView.text = newText
        
        // 滚动到顶部显示最新消息
        scrollView.post {
            scrollView.smoothScrollTo(0, 0)
        }
    }
    
    private fun updateButtonStates() {
        val enabled = isServiceBound
        submitTaskBtn.isEnabled = enabled
        cancelTaskBtn.isEnabled = enabled
        refreshStatusBtn.isEnabled = enabled
        clearCompletedBtn.isEnabled = enabled
    }
}