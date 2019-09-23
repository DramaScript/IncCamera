#include <jni.h>
#include <string>
#include "track/FaceTrack.h"
#include "macro.h"
#include "player/IncFFmpeg.h"
#include <android/native_window_jni.h>

#include "librtmp/rtmp.h"
#include "safe_queue.h"
#include "macro.h"
#include "pusher/VideoPusher.h"
#include "pusher/AudioPusher.h"

using namespace std;

JavaVM *javaVM = NULL;
IncFFmpeg *ffmpeg = 0;
JavaCallHelper *javaCallHelper = 0;
ANativeWindow *window = 0;
pthread_mutex_t mutex = PTHREAD_MUTEX_INITIALIZER;

SafeQueue<RTMPPacket *> packets;
VideoPusher *videoPusher = 0;
int isStart = 0;
pthread_t pid;
int readyPushing = 0;
uint32_t start_time;
AudioPusher *audioPusher = 0;


// -------------------------------------------------------------------RTMP推流--------------------------------------------------------------------

void releasePackets(RTMPPacket *&packet) {
    if (packet) {
        RTMPPacket_Free(packet);
        delete packet;
        packet = 0;
    }
}

void callback(RTMPPacket *packet) {
    if (packet) {
        //设置时间戳
        packet->m_nTimeStamp = RTMP_GetTime() - start_time;
        packets.enQueue(packet);
    }
}

extern "C"
JNIEXPORT void JNICALL
Java_com_dramascript_inccamera_pusher_LivePusher_native_1init(JNIEnv *env, jobject thiz) {

    //准备一个Video编码器的工具类 ：进行编码
    videoPusher = new VideoPusher;
    videoPusher->setVideoCallback(callback);
    audioPusher = new AudioPusher;
    audioPusher->setAudioCallback(callback);
    //准备一个队列,打包好的数据 放入队列，在线程中统一的取出数据再发送给服务器
    packets.setReleaseHandle(releasePackets);

}

extern "C"
JNIEXPORT void JNICALL
Java_com_dramascript_inccamera_pusher_LivePusher_native_1setVideoEncInfo(JNIEnv *env, jobject thiz,
                                                                         jint width, jint height,
                                                                         jint fps, jint bitrate) {
    if (videoPusher) {
        videoPusher->setVideoEncInfo(width, height, fps, bitrate);
    }
}


void *start(void *args) {
    char *url = static_cast<char *>(args);
    RTMP *rtmp = 0;
    do {
        rtmp = RTMP_Alloc();
        if (!rtmp) {
            LOGE("alloc rtmp失败");
            break;
        }
        RTMP_Init(rtmp);
        int ret = RTMP_SetupURL(rtmp, url);
        if (!ret) {
            LOGE("设置地址失败:%s", url);
            break;
        }
        //5s超时时间
        rtmp->Link.timeout = 5;
        RTMP_EnableWrite(rtmp);
        ret = RTMP_Connect(rtmp, 0);
        if (!ret) {
            LOGE("连接服务器:%s", url);
            break;
        }
        ret = RTMP_ConnectStream(rtmp, 0);
        if (!ret) {
            LOGE("连接流:%s", url);
            break;
        }
        //记录一个开始时间
        start_time = RTMP_GetTime();
        //表示可以开始推流了
        readyPushing = 1;
        packets.setWork(1);
        //保证第一个数据是 aac解码数据包
        callback(audioPusher->getAudioTag());
        RTMPPacket *packet = 0;
        while (readyPushing) {
            packets.deQueue(packet);
            if (!readyPushing) {
                break;
            }
            if (!packet) {
                continue;
            }
            packet->m_nInfoField2 = rtmp->m_stream_id;
            //发送rtmp包 1：队列
            // 意外断网？发送失败，rtmpdump 内部会调用RTMP_Close
            // RTMP_Close 又会调用 RTMP_SendPacket
            // RTMP_SendPacket  又会调用 RTMP_Close
            // 将rtmp.c 里面WriteN方法的 Rtmp_Close注释掉
            ret = RTMP_SendPacket(rtmp, packet, 1);
            releasePackets(packet);
            if (!ret) {
                LOGE("发送失败");
                break;
            }
        }
        releasePackets(packet);
    } while (0);
    //
    isStart = 0;
    readyPushing = 0;
    packets.setWork(0);
    packets.clear();
    if (rtmp) {
        RTMP_Close(rtmp);
        RTMP_Free(rtmp);
    }
    delete (url);
    return 0;
}

extern "C"
JNIEXPORT void JNICALL
Java_com_dramascript_inccamera_pusher_LivePusher_native_1start(JNIEnv *env, jobject thiz,
                                                               jstring path_) {
    if (isStart) {
        return;
    }
    isStart = 1;
    const char *path = env->GetStringUTFChars(path_, 0);
    char *url = new char[strlen(path) + 1];
    strcpy(url, path);
    pthread_create(&pid, 0, start, url);
    env->ReleaseStringUTFChars(path_, path);
}

extern "C"
JNIEXPORT void JNICALL
Java_com_dramascript_inccamera_pusher_LivePusher_native_1pushVideo(JNIEnv *env, jobject thiz,
                                                                   jbyteArray data_) {
    if (!videoPusher || !readyPushing) {
        return;
    }
    jbyte *data = env->GetByteArrayElements(data_, NULL);
    videoPusher->encodeData(data);
    env->ReleaseByteArrayElements(data_, data, 0);

}

extern "C"
JNIEXPORT void JNICALL
Java_com_dramascript_inccamera_pusher_LivePusher_native_1stop(JNIEnv *env, jobject thiz) {

    readyPushing = 0;
    //关闭队列工作
    packets.setWork(0);
    pthread_join(pid, 0);
}

extern "C"
JNIEXPORT void JNICALL
Java_com_dramascript_inccamera_pusher_LivePusher_native_1release(JNIEnv *env, jobject thiz) {
    DELETE(videoPusher);
    DELETE(audioPusher);
}

extern "C"
JNIEXPORT void JNICALL
Java_com_dramascript_inccamera_pusher_LivePusher_native_1setAudioEncInfo(JNIEnv *env, jobject thiz,
                                                                         jint sampleRateInHz,
                                                                         jint channels) {
    if (audioPusher) {
        audioPusher->setAudioEncInfo(sampleRateInHz, channels);
    }
}

extern "C"
JNIEXPORT jint JNICALL
Java_com_dramascript_inccamera_pusher_LivePusher_getInputSamples(JNIEnv *env, jobject thiz) {

    if (audioPusher) {
        return audioPusher->getInputSamples();
    }
    return -1;
}

extern "C"
JNIEXPORT void JNICALL
Java_com_dramascript_inccamera_pusher_LivePusher_native_1pushAudio(JNIEnv *env, jobject thiz,
                                                                   jbyteArray data_) {
    if (!audioPusher || !readyPushing) {
        return;
    }
    jbyte *data = env->GetByteArrayElements(data_, NULL);
    audioPusher->encodeData(data);
    env->ReleaseByteArrayElements(data_, data, 0);
}


// -------------------------------------------------------------------视频播放--------------------------------------------------------------------

extern "C" {

JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM *vm, void *reserved) {
    javaVM = vm;
    return JNI_VERSION_1_6;
}

//画画
void renderFrame(uint8_t *data, int linesize, int w, int h) {
    pthread_mutex_lock(&mutex);
    if (!window) {
        pthread_mutex_unlock(&mutex);
        return;
    }
    //设置窗口属性
    ANativeWindow_setBuffersGeometry(window, w,
                                     h,
                                     WINDOW_FORMAT_RGBA_8888);

    ANativeWindow_Buffer window_buffer;
    if (ANativeWindow_lock(window, &window_buffer, 0)) {
        ANativeWindow_release(window);
        window = 0;
        pthread_mutex_unlock(&mutex);
        return;
    }
    uint8_t *dst_data = static_cast<uint8_t *>(window_buffer.bits);
    //一行需要多少像素 * 4(RGBA)
    int dst_linesize = window_buffer.stride * 4;
    uint8_t *src_data = data;
    int src_linesize = linesize;
    //一次拷贝一行
    for (int i = 0; i < window_buffer.height; ++i) {
        memcpy(dst_data + i * dst_linesize, src_data + i * src_linesize, dst_linesize);
    }
    ANativeWindow_unlockAndPost(window);
    pthread_mutex_unlock(&mutex);
}


JNIEXPORT void JNICALL
Java_com_dramascript_inccamera_player_IncPlayer_native_1prepare(JNIEnv *env, jobject instance,
                                                                jstring dataSource_) {
    const char *dataSource = env->GetStringUTFChars(dataSource_, 0);
    javaCallHelper = new JavaCallHelper(javaVM, env, instance);
    ffmpeg = new IncFFmpeg(javaCallHelper, dataSource);
    ffmpeg->setRenderCallback(renderFrame);
    ffmpeg->prepare();
    env->ReleaseStringUTFChars(dataSource_, dataSource);
}

JNIEXPORT void JNICALL
Java_com_dramascript_inccamera_player_IncPlayer_native_1start(JNIEnv *env, jobject thiz) {
    if (ffmpeg) {
        ffmpeg->start();
    }
}

JNIEXPORT void JNICALL
Java_com_dramascript_inccamera_player_IncPlayer_native_1set_1surface(JNIEnv *env, jobject thiz,
                                                                     jobject surface) {
    pthread_mutex_lock(&mutex);
    //先释放之前的显示窗口
    if (window) {
        ANativeWindow_release(window);
        window = 0;
    }
    //创建新的窗口用于视频显示
    window = ANativeWindow_fromSurface(env, surface);
    pthread_mutex_unlock(&mutex);
}

JNIEXPORT void JNICALL
Java_com_dramascript_inccamera_player_IncPlayer_native_1stop(JNIEnv *env, jobject thiz) {
    if (ffmpeg) {
        ffmpeg->stop();
        ffmpeg = 0;
    }
    if (javaCallHelper) {
        delete javaCallHelper;
        javaCallHelper = 0;
    }
}

JNIEXPORT void JNICALL
Java_com_dramascript_inccamera_player_IncPlayer_native_1release(JNIEnv *env, jobject thiz) {
    pthread_mutex_lock(&mutex);
    if (window) {
        ANativeWindow_release(window);
        window = 0;
    }
    pthread_mutex_unlock(&mutex);
}

JNIEXPORT jint JNICALL
Java_com_dramascript_inccamera_player_IncPlayer_native_1getDuration(JNIEnv *env, jobject thiz) {
    if (ffmpeg) {
        return ffmpeg->getDuration();
    }
    return 0;
}

JNIEXPORT void JNICALL
Java_com_dramascript_inccamera_player_IncPlayer_native_1seek(JNIEnv *env, jobject thiz,
                                                             jint progress) {
    if (ffmpeg){
        ffmpeg->seek(progress);
    }
}

}

// -------------------------------------------------------------------人脸检测--------------------------------------------------------------------

extern "C"
JNIEXPORT jlong JNICALL
Java_com_dramascript_inccamera_filter_face_FaceTrack_native_1create(JNIEnv *env, jobject thiz,
                                                                    jstring model_,
                                                                    jstring seeta_) {
    const char *model = env->GetStringUTFChars(model_, 0);
    const char *seeta = env->GetStringUTFChars(seeta_, 0);

    FaceTrack *faceTrack = new FaceTrack(model, seeta);

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

