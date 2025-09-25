# AIDL 并发任务管理系统使用说明

## 功能概述

这是一个基于AIDL（Android Interface Definition Language）实现的并发任务管理系统demo，展示了如何在Android中使用AIDL进行进程间通信，实现一个完整的任务管理处理系统。

## 系统架构

### 组件说明

1. **ITaskManagerService.aidl** - 任务管理服务接口定义
2. **ITaskCallback.aidl** - 任务回调接口定义
3. **TaskManagerService** - 任务管理服务实现（运行在主进程）
4. **AIDLActivity** - 任务管理界面（与服务进行交互）
5. **Task** - 任务数据模型

### 系统特性

- ✅ **并发任务处理**: 支持最多3个任务同时运行
- ✅ **任务状态管理**: 实时跟踪任务状态（等待、运行、完成、失败、取消）
- ✅ **进度回调**: 实时显示任务执行进度
- ✅ **任务取消**: 支持取消正在运行的任务
- ✅ **状态查询**: 查看所有任务的详细状态
- ✅ **自动清理**: 清除已完成的任务

## 使用方法

### 1. 启动应用
- 打开应用，点击"AIDL"按钮进入任务管理界面

### 2. 提交任务
- 输入任务名称（如：数据处理任务）
- 设置任务持续时间（1-60秒）
- 点击"提交任务"按钮

### 3. 管理任务
- **取消任务**: 点击"取消任务"按钮，输入要取消的任务ID
- **刷新状态**: 点击"刷新状态"查看所有任务的当前状态
- **清除完成**: 点击"清除完成"移除所有已完成的任务

### 4. 监控任务
- 界面下方的状态区域会实时显示：
  - 任务提交信息
  - 任务开始通知
  - 任务进度更新（每1%更新一次）
  - 任务完成/失败/取消通知
  - 任务状态总览

## 技术实现

### AIDL接口设计

```kotlin
// 主要服务接口
interface ITaskManagerService {
    int submitTask(String taskName, int duration);
    boolean cancelTask(int taskId);
    String getTaskStatus(int taskId);
    List<String> getAllTasksStatus();
    void registerCallback(ITaskCallback callback);
    void unregisterCallback(ITaskCallback callback);
    int getRunningTaskCount();
    void clearCompletedTasks();
}

// 回调接口
interface ITaskCallback {
    void onTaskStarted(int taskId, String taskName);
    void onTaskProgress(int taskId, int progress);
    void onTaskCompleted(int taskId, String result);
    void onTaskFailed(int taskId, String error);
    void onTaskCancelled(int taskId);
}
```

### 并发处理

- 使用`ThreadPoolExecutor`管理任务执行
- 核心线程数：2
- 最大线程数：3
- 任务队列：`LinkedBlockingQueue`
- 支持任务中断和取消

### 进程间通信

- Service运行在主进程中
- Activity通过`bindService`连接到Service
- 使用`RemoteCallbackList`管理多个客户端回调
- 支持异步任务状态通知

## 测试场景

### 基本功能测试
1. 提交单个任务，观察执行过程
2. 提交多个任务，验证并发执行
3. 取消正在运行的任务
4. 查看任务状态和进度

### 并发测试
1. 快速提交5个任务，验证最多3个并发执行
2. 在任务运行过程中取消部分任务
3. 清除已完成的任务

### 异常处理测试
1. 输入无效的任务参数
2. 尝试取消不存在的任务
3. 在服务未连接时操作

## 注意事项

1. **任务持续时间**: 建议设置1-60秒，过长的任务可能影响用户体验
2. **并发限制**: 系统最多同时运行3个任务，超出的任务会排队等待
3. **内存管理**: 定期清除已完成的任务以释放内存
4. **线程安全**: 所有任务操作都是线程安全的

## 扩展功能

可以基于此demo扩展以下功能：
- 任务优先级管理
- 任务依赖关系
- 任务结果持久化
- 任务执行统计
- 更复杂的任务类型支持