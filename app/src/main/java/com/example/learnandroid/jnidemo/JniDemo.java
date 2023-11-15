package com.example.learnandroid.jnidemo;

import android.util.Log;

public class JniDemo {
    private final static String TAG = "JniDemo";

    private long nativeContext = 0L; // 8字节64位无符号整数表示内存地址。jni进行赋值。

    public JniDemo(JniDemoConfig config) {
        newInstance(config);
        Log.e(TAG, "JniDemo nativeContext: $nativeContext");
    }

    public native JniDemoConfig getConfig();

    public native void setName(String name);

    // finalize() 是Java中Object的一个protected方法，返回值为空，当该对象被垃圾回收器回收时，会调用该方法。
    // 关于finalize()函数，要说明几点
    // 1. finalize不等价于c++中的析构函数
    // 2. 对象可能不被垃圾机回收器回收
    // 3. 垃圾回收不等于析构
    // 4. 垃圾回收只与内存有关
    // 5. 垃圾回收和finalize()都是靠不住的，只要JVM还没有快到耗尽内存的地步，它是不会浪费时间进行垃圾回收的。
    // 6. 程序强制终结后，那些失去引用的对象将会被垃圾回收。（System.gc()）
    // kotlin的finalize写法。java可以重写这个函数，kotlin没有，直接定义即可
    // https://kotlinlang.org/docs/java-interop.html#finalize
    // https://stackoverflow.com/questions/43784161/how-to-implement-finalize-in-kotlin
    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        if (nativeContext != 0L) {
            nativeFinalize();
        }
    }

    private native void newInstance(JniDemoConfig config);

    // 用来析构jni对象
    private native void nativeFinalize();

    private static native void nativeInit();

    // Java中的静态代码块，在虚拟机加载类的时候就会加载执行，而且只执行一次
    static {
        try {
            System.loadLibrary("learnandroid");
            nativeInit();
        } catch (Exception e) {
            Log.e(TAG, e.toString());
        }
    }
}
