package com.example.learnandroid;

// 任务回调接口
interface ITaskCallback {
    // 任务开始回调
    void onTaskStarted(int taskId, String taskName);
    
    // 任务进度回调
    void onTaskProgress(int taskId, int progress);
    
    // 任务完成回调
    void onTaskCompleted(int taskId, String result);
    
    // 任务失败回调
    void onTaskFailed(int taskId, String error);
    
    // 任务取消回调
    void onTaskCancelled(int taskId);
}