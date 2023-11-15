//
// Created by Zhe Ma on 2023/11/14.
//

#ifndef LEARNANDROID_JNI_CLASS_WRAPPER_H_
#define LEARNANDROID_JNI_CLASS_WRAPPER_H_

#include <jni.h>

namespace jni_demo {

template <typename T>
class JniClassWrapper {
public:
  JniClassWrapper(JNIEnv* env, T ref) {
    reset(env, ref);
  }

  ~JniClassWrapper() {
    reset(nullptr, nullptr);
  }

  void reset(JNIEnv* env, T ref) {
    if (global_ref_ == ref) {
      return;
    }

    if (env_ != nullptr && global_ref_ != nullptr) {
      env_->DeleteGlobalRef(global_ref_);
      global_ref_ = nullptr;
      env_ = nullptr;
    }

    if (env != nullptr && ref != nullptr) {
      env_ = env;
      // 由局部引用转换成的全局引用。
      // https://docs.oracle.com/javase/7/docs/technotes/guides/jni/spec/functions.html
      // Creates a new global reference to the object referred to by the obj argument.
      // The obj argument may be a global or local reference.
      // Global references must be explicitly disposed of by calling DeleteGlobalRef().
      global_ref_ = static_cast<T>(env_->NewGlobalRef(ref));
    }
  }

  T get() const {
    return global_ref_;
  }

private:
  JNIEnv* env_ = nullptr;
  T global_ref_ = nullptr;
};

}  // jni_demo

#endif  // LEARNANDROID_JNI_CLASS_WRAPPER_H_
//template <typename T>
//class Global {
//public:
//  Global() : mEnv(nullptr), mRef(nullptr) {
//  }
//
//  Global(JNIEnv* env, T ref) : mEnv(nullptr), mRef(nullptr) {
//    reset(env, ref);
//  }
//
//  ~Global() {
//    reset(nullptr, nullptr);
//  }
//
//  void reset(JNIEnv* env, T ref) {
//    if (mRef == ref) {
//      return;
//    }
//    if (mRef != nullptr) {
//      if (env == nullptr) {
//        env = tav::JNIEnvironment::Current();
//        if (env == nullptr) {
//          return;
//        }
//      }
//
//      env->DeleteGlobalRef(mRef);
//      mRef = nullptr;
//    }
//    mEnv = env;
//    if (ref == nullptr) {
//      mRef = nullptr;
//    } else {
//      mRef = (T)mEnv->NewGlobalRef(ref);
//    }
//  }
//
//  T get() const {
//    return mRef;
//  }
//
//private:
//  JNIEnv* mEnv;
//  T mRef;
//};