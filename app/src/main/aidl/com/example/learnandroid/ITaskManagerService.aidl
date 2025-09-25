package com.example.learnandroid;

import com.example.learnandroid.ITaskCallback;

// 任务管理服务接口
interface ITaskManagerService {
    // 提交任务
    int submitTask(String taskName, int duration);
    
    // 取消任务
    boolean cancelTask(int taskId);
    
    // 获取任务状态
    String getTaskStatus(int taskId);
    
    // 获取所有任务状态
    List<String> getAllTasksStatus();
    
    // 注册任务回调
    void registerCallback(ITaskCallback callback);
    
    // 取消注册任务回调
    void unregisterCallback(ITaskCallback callback);
    
    // 获取当前运行任务数量
    int getRunningTaskCount();
    
    // 清除所有已完成的任务
    void clearCompletedTasks();
}