#ifndef DNPLAYER_DNFFMPEG_H
#define DNPLAYER_DNFFMPEG_H


#include <pthread.h>
#include <android/native_window.h>
#include "JavaCallHelper.h"
#include "AudioChannel.h"
#include "VideoChannel.h"

extern "C" {
#include <libavformat/avformat.h>
#include <libavutil/time.h>

}


class IncFFmpeg {
    friend void* async_stop(void* args);
public:
    IncFFmpeg(JavaCallHelper *javaCallHelper, const char *dataSource);

    ~IncFFmpeg();

    void prepare();

    void prepareFFmpeg();

    void start();

    void play();

    void setRenderCallback(RenderFrame renderFrame);

    void stop();

    int getDuration() {
        return duration;
    }

    void seek(int i);

private:
    char *url;
    JavaCallHelper *javaCallHelper;

    pthread_t pid_prepare;
    pthread_t pid_play;
    pthread_t pid_stop;

    pthread_mutex_t seekMutex;
    AVFormatContext *formatContext = 0;

    int duration;

    RenderFrame renderFrame;

    AudioChannel *audioChannel = 0;
    VideoChannel *videoChannel = 0;

    bool isPlaying;
    bool isSeek = 0;

};


#endif //DNPLAYER_DNFFMPEG_H
