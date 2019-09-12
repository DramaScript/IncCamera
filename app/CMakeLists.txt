cmake_minimum_required(VERSION 3.4.1)

# 设置 JNILIBS_DIR  赋值为jniLibs的路径
# CMAKE_SOURCE_DIR
set(JNILIBS_DIR ${CMAKE_SOURCE_DIR}/src/main/jniLibs/${ANDROID_ABI})

add_library(
        native-lib
        SHARED
        src/main/cpp/native-lib.cpp
        src/main/cpp/FaceTrack.cpp)

include_directories(${CMAKE_SOURCE_DIR}/src/main/cpp/include)
add_library(libopencv_java3 SHARED IMPORTED)
set_target_properties(libopencv_java3 PROPERTIES IMPORTED_LOCATION
        ${JNILIBS_DIR}/libopencv_java3.so)

add_subdirectory(src/main/cpp/FaceAlignment)
include_directories(src/main/cpp/FaceAlignment/include)

target_link_libraries(
        native-lib
        log
        libopencv_java3
        seeta_fa_lib
)