//
// Created by Zhe Ma on 2023/11/9.
//

#ifndef LEARNANDROID_JNIDEMOCONFIG_H_
#define LEARNANDROID_JNIDEMOCONFIG_H_

#include <string>

namespace jni_demo {

class JniDemoConfig {
public:
  int type = 0;
  std::string name;

public:
  JniDemoConfig();
  ~JniDemoConfig();
};

}  // jni_demo

#endif  // LEARNANDROID_JNIDEMOCONFIG_H_
