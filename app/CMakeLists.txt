cmake_minimum_required(VERSION 3.4.1)

# 设置 JNILIBS_DIR  赋值为jniLibs的路径
# CMAKE_SOURCE_DIR
set(JNILIBS_DIR ${CMAKE_SOURCE_DIR}/src/main/jniLibs/${ANDROID_ABI})

file(GLOB SOURCE src/main/cpp/*.cpp )
file(GLOB PLAYER src/main/cpp/player/*.cpp )
file(GLOB TRACK src/main/cpp/track/*.cpp )
add_library(
        native-lib
        SHARED
        ${SOURCE}
        ${PLAYER}
        ${TRACK})

# opencv 图像处理
include_directories(${CMAKE_SOURCE_DIR}/src/main/cpp/include)
add_library(libopencv_java3 SHARED IMPORTED)
set_target_properties(libopencv_java3 PROPERTIES IMPORTED_LOCATION
        ${JNILIBS_DIR}/libopencv_java3.so)

# SeetaFaceEngine 人脸检测
add_subdirectory(src/main/cpp/FaceAlignment)
include_directories(src/main/cpp/FaceAlignment/include)

# ffmpeg 下面这种方式和上面的方式差不多，上面的适合动态so库，这种适合静态库
include_directories(${CMAKE_SOURCE_DIR}/src/main/cpp/ffmpegInclude)
set(my_lib_path ${CMAKE_SOURCE_DIR}/src/main/cpp/${ANDROID_ABI})
set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -L${my_lib_path} ")

# 注意FFmpeg的库的顺序 不然会报错，并且 一定要导入z库
target_link_libraries(
        native-lib
        avfilter avformat    avcodec  avutil  swresample swscale
        log
        libopencv_java3
        seeta_fa_lib
        z
        OpenSLES
        android
)