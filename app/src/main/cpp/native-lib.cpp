#include <jni.h>
#include <string>
#include "FaceTrack.h"

using namespace std;

extern "C"
JNIEXPORT jlong JNICALL
Java_com_dramascript_inccamera_filter_face_FaceTrack_native_1create(JNIEnv *env, jobject thiz,
                                                                    jstring model_, jstring seeta_) {
    const char *model = env->GetStringUTFChars(model_, 0);
    const char *seeta = env->GetStringUTFChars(seeta_, 0);

    FaceTrack *faceTrack = new FaceTrack(model,seeta);

    env->ReleaseStringUTFChars(model_, model);
    env->ReleaseStringUTFChars(seeta_, seeta);
    // 返回指针
    return reinterpret_cast<jlong>(faceTrack);
}

extern "C"
JNIEXPORT void JNICALL
Java_com_dramascript_inccamera_filter_face_FaceTrack_native_1start(JNIEnv *env, jobject thiz,
                                                                   jlong self) {
    if (self == 0) {
        return;
    }
    FaceTrack *me = (FaceTrack *) self;
    me->startTracking();
}

extern "C"
JNIEXPORT void JNICALL
Java_com_dramascript_inccamera_filter_face_FaceTrack_native_1stop(JNIEnv *env, jobject thiz,
                                                                  jlong self) {
    if (self == 0) {
        return;
    }
    FaceTrack *me = (FaceTrack *) self;
    me->stopTracking();
    delete me;
}

extern "C"
JNIEXPORT jobject JNICALL
Java_com_dramascript_inccamera_filter_face_FaceTrack_native_1detector(JNIEnv *env, jobject thiz,
                                                                      jlong self, jbyteArray data_,
                                                                      jint cameraId, jint width,
                                                                      jint height) {
    if (self == 0) {
        return NULL;
    }
    jbyte *data = env->GetByteArrayElements(data_, NULL);
    FaceTrack *me = (FaceTrack *) self;
    Mat src(height + height / 2, width, CV_8UC1, data);
    //opencv 将yuv转成rgba
    cvtColor(src, src, CV_YUV2RGBA_NV21);
    if (cameraId == 1) {
        //前置
        //逆时针90度
        rotate(src, src, ROTATE_90_COUNTERCLOCKWISE);
        // y 翻转
        flip(src, src, 1);
    } else {
        //后置
        rotate(src, src, ROTATE_90_CLOCKWISE);
    }

    // 灰度化
    cvtColor(src, src, COLOR_RGBA2GRAY);
    //直方图均衡化 增强对比效果
    equalizeHist(src, src);

    vector<Rect2f> rects;
    //送去定位
    me->detector(src, rects);
    env->ReleaseByteArrayElements(data_, data, 0);

    int w = src.cols;
    int h = src.rows;
    src.release();
    int ret = rects.size();
    if (ret) {
        jclass clazz = env->FindClass("com/dramascript/inccamera/filter/face/Face");
        jmethodID costruct = env->GetMethodID(clazz, "<init>", "(IIII[F)V");
        int size = ret * 2;
        //创建java 的float 数组
        jfloatArray floatArray = env->NewFloatArray(size);
        for (int i = 0, j = 0; i < size; j++) {
            float f[2] = {rects[j].x, rects[j].y};
            env->SetFloatArrayRegion(floatArray, i, 2, f);
            i += 2;
        }
        Rect2f faceRect = rects[0];
        int width = faceRect.width;
        int height = faceRect.height;
        jobject face = env->NewObject(clazz, costruct, width, height, w, h,
                                      floatArray);
        return face;
    }
    return NULL;
}