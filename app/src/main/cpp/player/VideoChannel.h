#ifndef DNPLAYER_VIDEOCHANNEL_H
#define DNPLAYER_VIDEOCHANNEL_H


#include "BaseChannel.h"
#include "AudioChannel.h"
#include <pthread.h>
#include <android/native_window.h>

typedef void (*RenderFrame)(uint8_t *, int, int, int);

class VideoChannel : public BaseChannel {
public:
    VideoChannel(int id,JavaCallHelper *javaCallHelper, AVCodecContext *avCodecContext, AVRational base, int fps);

    virtual ~VideoChannel();

    virtual void play();

    virtual void stop();

    void decodePacket();

    void synchronizeFrame();

    void setRenderCallback(RenderFrame renderFrame);

public:
    AudioChannel *audioChannel = 0;
private:
    int fps;
    pthread_t pid_video_play;
    pthread_t pid_synchronize;
    RenderFrame renderFrame;
};


#endif //DNPLAYER_VIDEOCHANNEL_H
