//
// Created by Zhe Ma on 2023/11/6.
//

#include "jni_demo/jni_demo.h"

#include <utility>
#include <jni.h>

#include "jni_demo/defs.h"

namespace jni_demo {

JniDemo::JniDemo(const JniDemoConfig& config)
    : config_(config) {
  LOGE("JniDemo(const JniDemoConfig& config)");
}

JniDemo::~JniDemo() {
  LOGE("~JniDemo()");
}

const JniDemoConfig &JniDemo::config() const {
  return config_;
}

void JniDemo::SetName(const std::string &name) {
  config_.name = name;
  LOGE("JniDemo::SetName: %s", name.c_str());
}

}  // jni_demo
