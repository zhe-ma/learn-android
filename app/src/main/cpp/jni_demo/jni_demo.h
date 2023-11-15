//
// Created by Zhe Ma on 2023/11/6.
//

#ifndef LEARNANDROID_JNIDEMO_H_
#define LEARNANDROID_JNIDEMO_H_

#include "jni_demo/jni_demo_config.h"

namespace jni_demo {

class JniDemo {
public:
  JniDemo(const JniDemoConfig& config);
  ~JniDemo();

  const JniDemoConfig& config() const;

  void SetName(const std::string& name);

private:
  JniDemoConfig config_;
};

}  // jni_demo

#endif  // LEARNANDROID_JNIDEMO_H_
