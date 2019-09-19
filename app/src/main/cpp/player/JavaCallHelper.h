#ifndef DNPLAYER_JAVACALLHELPER_H
#define DNPLAYER_JAVACALLHELPER_H


#include <jni.h>


class JavaCallHelper {


public:
    JavaCallHelper(JavaVM *_javaVM, JNIEnv *_env, jobject &_jobj);

    ~JavaCallHelper();

    void onError(int thread, int code);

    void onParpare(int thread);

    void onProgress(int thread, int progress);

public:
    JavaVM *javaVM;
    JNIEnv *env;
    jobject jobj;
    jmethodID jmid_error;
    jmethodID jmid_prepare;
    jmethodID jmid_progress;


};


#endif //DNPLAYER_JAVACALLHELPER_H
