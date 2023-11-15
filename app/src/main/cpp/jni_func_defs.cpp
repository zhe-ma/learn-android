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
#include <memory>
#include "jni_demo/defs.h"
#include "jni_demo/jni_class_wrapper.h"
#include "jni_demo/jni_demo.h"
#include "jni_demo/jni_demo_config.h"
#include "spdlog/sinks/android_sink.h"
#include "spdlog/spdlog.h"

namespace jni_demo {
static jfieldID g_jni_demo_native_context_filed_id = nullptr;
}  // jni_demo

/**
 * SPDLOG 测试
 */

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

/**
 * 使用Java包装Jni
 */

extern "C"
JNIEXPORT void JNICALL
Java_com_example_learnandroid_jnidemo_SpdlogHelper_nativeInit(JNIEnv *env, jclass clazz) {
  auto android_logger = spdlog::android_logger_mt("learnandroid", jni_demo::TAG);
  spdlog::set_default_logger(android_logger);
  SPDLOG_WARN("Init Spdlog");
}

extern "C"
JNIEXPORT void JNICALL
Java_com_example_learnandroid_jnidemo_JniDemo_nativeFinalize(JNIEnv *env, jobject thiz) {
  // https://docs.oracle.com/javase/7/docs/technotes/guides/jni/spec/functions.html
  // Get<type>Field Routines
  // This family of accessor routines returns the value of an instance (nonstatic) field of an object.
  // The field to access is specified by a field ID obtained by calling GetFieldID().
  //The following table describes the Get<type>Field routine name and result type.
  // You should replace type in Get<type>Field with the Java type of the field,
  // or use one of the actual routine names from the table,
  // and replace NativeType with the corresponding native type for that routine.
  // 获取对象的nativeContext值转为jni对象进行析构，然后将kotlin中的nativeContext转为0
  jni_demo::JniDemo* jni_demo = reinterpret_cast<jni_demo::JniDemo *>(
          env->GetLongField(thiz,jni_demo::g_jni_demo_native_context_filed_id));
  delete jni_demo;
  env->SetLongField(thiz, jni_demo::g_jni_demo_native_context_filed_id, 0l);
}

extern "C"
JNIEXPORT jobject JNICALL
Java_com_example_learnandroid_jnidemo_JniDemo_getConfig(JNIEnv *env, jobject thiz) {
  jni_demo::JniDemo* jni_demo = reinterpret_cast<jni_demo::JniDemo *>(
          env->GetLongField(thiz,jni_demo::g_jni_demo_native_context_filed_id));
  if (jni_demo == nullptr) {
    return nullptr;
  }

  static jni_demo::JniClassWrapper<jclass> s_config_class(env, env->FindClass("com/example/learnandroid/jnidemo/JniDemoConfig"));
  // 获取构造函数
  static jmethodID s_config_constructor = env->GetMethodID(s_config_class.get(), "<init>", "()V");
  static jfieldID s_config_name = env->GetFieldID(s_config_class.get(), "name", "Ljava/lang/String;");
  static jfieldID s_config_type = env->GetFieldID(s_config_class.get(), "type", "I");

  // 创建一个java对象
  // Constructs a new Java object. The method ID indicates which constructor method to invoke.
  // This ID must be obtained by calling GetMethodID() with <init> as the method name and void (V) as the return type.
  jobject object = env->NewObject(s_config_class.get(), s_config_constructor);
  env->SetIntField(object, s_config_type, jni_demo->config().type);
  env->SetObjectField(object, s_config_name, env->NewStringUTF(jni_demo->config().name.c_str()));

  return object;
}

extern "C"
JNIEXPORT void JNICALL
Java_com_example_learnandroid_jnidemo_JniDemo_setName(JNIEnv *env, jobject thiz, jstring name) {
  jni_demo::JniDemo* jni_demo = reinterpret_cast<jni_demo::JniDemo *>(
          env->GetLongField(thiz,jni_demo::g_jni_demo_native_context_filed_id));
  if (jni_demo != nullptr) {
    jni_demo->SetName(jni_demo::ConvertJStringToStdString(env, name));
  }
}

extern "C"
JNIEXPORT void JNICALL
Java_com_example_learnandroid_jnidemo_JniDemo_newInstance(JNIEnv *env, jobject thiz, jobject config) {
  static jni_demo::JniClassWrapper<jclass> s_config_class(env, env->FindClass("com/example/learnandroid/jnidemo/JniDemoConfig"));
  static jfieldID s_config_name = env->GetFieldID(s_config_class.get(), "name", "Ljava/lang/String;");
  static jfieldID s_config_type = env->GetFieldID(s_config_class.get(), "type", "I");

  // 获取对象成员变量的值
  jstring name = static_cast<jstring>(env->GetObjectField(config, s_config_name));
  jint type = env->GetIntField(config, s_config_type);

  jni_demo::JniDemoConfig demo_config;
  demo_config.type = type;
  demo_config.name = jni_demo::ConvertJStringToStdString(env, name);

  jni_demo::JniDemo* jni_demo = new jni_demo::JniDemo(demo_config);
  // 将对象的地址设置给kotlin对象的nativeContext成员变量
  env->SetLongField(thiz, jni_demo::g_jni_demo_native_context_filed_id, reinterpret_cast<jlong>(jni_demo));
}

extern "C"
JNIEXPORT void JNICALL
Java_com_example_learnandroid_jnidemo_JniDemo_nativeInit(JNIEnv *env, jclass clazz) {
  // GetFieldID获取类成员变量的的id，获取的不是一个对象的内存地址，而是这个变量的id，方便通过ID来查找变量地址
  // Returns the field ID for an instance (nonstatic) field of a class.
  // The field is specified by its name and signature.
  // The Get<type>Field and Set<type>Field families of accessor functions use field IDs to retrieve object fields.
  // nativeContext是JniDemo中的成员变量，J是变量的类型，代表Long
  jni_demo::g_jni_demo_native_context_filed_id = env->GetFieldID(clazz, "nativeContext","J");
}

/**
 * 使用kotlin包装jni
 */

extern "C"
JNIEXPORT jobject JNICALL
Java_com_example_learnandroid_jnidemo_JniDemoKotlin_getConfig(JNIEnv *env, jobject thiz) {
  jni_demo::JniDemo* jni_demo = reinterpret_cast<jni_demo::JniDemo *>(
          env->GetLongField(thiz,jni_demo::g_jni_demo_native_context_filed_id));
  if (jni_demo == nullptr) {
    return nullptr;
  }

  static jni_demo::JniClassWrapper<jclass> s_config_class(env, env->FindClass("com/example/learnandroid/jnidemo/JniDemoConfig"));
  // 获取构造函数
  static jmethodID s_config_constructor = env->GetMethodID(s_config_class.get(), "<init>", "()V");
  static jfieldID s_config_name = env->GetFieldID(s_config_class.get(), "name", "Ljava/lang/String;");
  static jfieldID s_config_type = env->GetFieldID(s_config_class.get(), "type", "I");

  // 创建一个java对象
  // Constructs a new Java object. The method ID indicates which constructor method to invoke.
  // This ID must be obtained by calling GetMethodID() with <init> as the method name and void (V) as the return type.
  jobject object = env->NewObject(s_config_class.get(), s_config_constructor);
  env->SetIntField(object, s_config_type, jni_demo->config().type);
  env->SetObjectField(object, s_config_name, env->NewStringUTF(jni_demo->config().name.c_str()));

  return object;
}
extern "C"
JNIEXPORT void JNICALL
Java_com_example_learnandroid_jnidemo_JniDemoKotlin_setName(JNIEnv *env, jobject thiz,
                                                            jstring name) {
  jni_demo::JniDemo* jni_demo = reinterpret_cast<jni_demo::JniDemo *>(
          env->GetLongField(thiz,jni_demo::g_jni_demo_native_context_filed_id));
  if (jni_demo != nullptr) {
    jni_demo->SetName(jni_demo::ConvertJStringToStdString(env, name));
  }
}
extern "C"
JNIEXPORT void JNICALL
Java_com_example_learnandroid_jnidemo_JniDemoKotlin_nativeFinalize(JNIEnv *env, jobject thiz) {
  jni_demo::JniDemo* jni_demo = reinterpret_cast<jni_demo::JniDemo *>(
          env->GetLongField(thiz,jni_demo::g_jni_demo_native_context_filed_id));
  delete jni_demo;
  env->SetLongField(thiz, jni_demo::g_jni_demo_native_context_filed_id, 0l);
}
extern "C"
JNIEXPORT void JNICALL
Java_com_example_learnandroid_jnidemo_JniDemoKotlin_newInstance(JNIEnv *env, jobject thiz,
                                                                jobject config) {
  static jni_demo::JniClassWrapper<jclass> s_config_class(env, env->FindClass("com/example/learnandroid/jnidemo/JniDemoConfig"));
  static jfieldID s_config_name = env->GetFieldID(s_config_class.get(), "name", "Ljava/lang/String;");
  static jfieldID s_config_type = env->GetFieldID(s_config_class.get(), "type", "I");

  // 获取对象成员变量的值
  jstring name = static_cast<jstring>(env->GetObjectField(config, s_config_name));
  jint type = env->GetIntField(config, s_config_type);

  jni_demo::JniDemoConfig demo_config;
  demo_config.type = type;
  demo_config.name = jni_demo::ConvertJStringToStdString(env, name);

  jni_demo::JniDemo* jni_demo = new jni_demo::JniDemo(demo_config);
  // 将对象的地址设置给kotlin对象的nativeContext成员变量
  env->SetLongField(thiz, jni_demo::g_jni_demo_native_context_filed_id, reinterpret_cast<jlong>(jni_demo));
}
extern "C"
JNIEXPORT void JNICALL
Java_com_example_learnandroid_jnidemo_JniDemoKotlin_nativeInit(JNIEnv *env, jclass clazz) {
  jni_demo::g_jni_demo_native_context_filed_id = env->GetFieldID(clazz, "nativeContext","J");
}