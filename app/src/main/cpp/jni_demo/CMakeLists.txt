
# For more information about using CMake with Android Studio, read the
# documentation: https://d.android.com/studio/projects/add-native-code.html

# Sets the minimum version of CMake required to build the native library.

cmake_minimum_required(VERSION 3.18.1)

file(GLOB SRCS
        RELATIVE ${CMAKE_CURRENT_SOURCE_DIR}
        ${CMAKE_CURRENT_SOURCE_DIR}/*.cpp
        ${CMAKE_CURRENT_SOURCE_DIR}/*.h)

set(TARGET_NAME jni_demo)

ADD_LIBRARY(${TARGET_NAME} STATIC ${SRCS})

find_library(
        # Sets the name of the path variable.
        log-lib

        # Specifies the name of the NDK library that
        # you want CMake to locate.
        log )


set(
        LIBS
        # Sets the name of the path variable.
        ${log-lib}
        )

target_link_libraries(${TARGET_NAME} ${LIBS})

