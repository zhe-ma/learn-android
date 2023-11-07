// Write C++ code here.
//
// Do not forget to dynamically load the C++ library into your application.
//
// For instance,
//
// In MainActivity.java:
//    static {
//       System.loadLibrary("learnandroid");
//    }
//
// Or, in MainActivity.kt:
//    companion object {
//      init {
//         System.loadLibrary("learnandroid")
//      }
//    }

#include <jni.h>
#include <string>
#include "jni_demo/defs.h"
#include "spdlog/sinks/android_sink.h"
#include "spdlog/spdlog.h"

extern "C"
JNIEXPORT jstring JNICALL
Java_com_example_learnandroid_jnidemo_SpdlogHelper_getLoggerTag(JNIEnv *env, jclass clazz) {
  // https://blog.islinjw.cn/2020/04/08/JNI%E5%86%85%E5%AD%98%E7%AE%A1%E7%90%86/
  // 从局部引用讲起,这里通过NewStringUTF创建的jstring就是局部引用,那它有什么特点?
  // 在c层大多数调用jni方法创建的引用都是局部引用,它会别存放在一张局部引用表里。它的内存有四种释放方式:
  // 1. 程序员可以手动调用DeleteLocalRef去释放
  // 2. c层方法执行完成返回java层的时候,jvm会遍历局部引用表去释放
  // 3. 使用PushLocalFrame/PopLocalFrame创建/销毁局部引用栈帧的时候,在PopLocalFrame里会释放帧内创建的引用
  // 4. 如果使用AttachCurrentThread附加原生线程,在调用DetachCurrentThread的时候会释放该线程创建的局部引用
  // 所以上面的问题我们就能回答了, jstr可以不用手动delete,可以等方法结束的时候jvm自己去释放(当然如果返回之后在java层将这个引用保存了起来,那也是不会立马释放内存的)
  // 局部引用表是有大小限制的,如果new的内存太多的话可能造成局部引用表的内存溢出,例如我们在for循环里面不断创建对象
  return env->NewStringUTF(jni_demo::TAG);
}

extern "C"
JNIEXPORT void JNICALL
Java_com_example_learnandroid_jnidemo_SpdlogHelper_nativeInit(JNIEnv *env, jclass clazz) {
  auto android_logger = spdlog::android_logger_mt("learnandroid", jni_demo::TAG);
  spdlog::set_default_logger(android_logger);
  SPDLOG_WARN("Init Spdlog");
}