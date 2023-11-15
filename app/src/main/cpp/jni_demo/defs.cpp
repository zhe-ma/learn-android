//
// Created by Zhe Ma on 2023/11/14.
//

#include "jni_demo/defs.h"

namespace jni_demo {

const char* TAG = "jni_demo";

std::string ConvertJStringToStdString(JNIEnv* env, jstring jstr) {
  const char* str = env->GetStringUTFChars(jstr, nullptr);
  std::string result(str);
  env->ReleaseStringUTFChars(jstr, str);
  return result;
}

}  // jni_demo
