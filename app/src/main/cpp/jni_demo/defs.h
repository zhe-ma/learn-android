//
// Created by Zhe Ma on 2023/11/6.
//

#ifndef LEARNANDROID_DEFS_H_
#define LEARNANDROID_DEFS_H_

#include <string>
#include "jni.h"
#include "android/log.h"

namespace jni_demo {

extern const char* TAG;

std::string ConvertJStringToStdString(JNIEnv* env, jstring jstr);

}  // jni_demo

#define LOGE(format, ...) __android_log_print(ANDROID_LOG_ERROR,   jni_demo::TAG, format, ##__VA_ARGS__)
#define LOGI(format, ...) __android_log_print(ANDROID_LOG_INFO,    jni_demo::TAG, format, ##__VA_ARGS__)
#define LOGD(format, ...) __android_log_print(ANDROID_LOG_DEBUG,   jni_demo::TAG, format, ##__VA_ARGS__)
#define LOGW(format, ...) __android_log_print(ANDROID_LOG_WARN,    jni_demo::TAG, format, ##__VA_ARGS__)
#define LOGV(format, ...) __android_log_print(ANDROID_LOG_VERBOSE, jni_demo::TAG, format, ##__VA_ARGS__)

#endif  // LEARNANDROID_DEFS_H_
