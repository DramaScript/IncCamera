#ifndef DNPLAYER_BASECHANNEL_H
#define DNPLAYER_BASECHANNEL_H

#include "../safe_queue.h"
#include "../macro.h"
#include "JavaCallHelper.h"

extern "C" {
#include <libavcodec/avcodec.h>
}

class BaseChannel {
public:
    BaseChannel(int id, JavaCallHelper *javaCallHelper, AVCodecContext *avCodecContext,
                AVRational base) : channelId(id),
                                   javaCallHelper(javaCallHelper),
                                   avCodecContext(avCodecContext),
                                   time_base(base) {
        pkt_queue.setReleaseHandle(releaseAvPacket);
        frame_queue.setReleaseHandle(releaseAvFrame);
    };

    virtual ~BaseChannel() {
        if (avCodecContext) {
            avcodec_close(avCodecContext);
            avcodec_free_context(&avCodecContext);
            avCodecContext = 0;
        }
        pkt_queue.clear();
        frame_queue.clear();
        LOGE("释放channel:%d %d", pkt_queue.size(), frame_queue.size());
    };


    virtual void play() = 0;

    virtual void stop() = 0;

    static void releaseAvFrame(AVFrame *&frame) {
        if (frame) {
            av_frame_free(&frame);
            frame = 0;
        }
    }


    static void releaseAvPacket(AVPacket *&packet) {
        if (packet) {
            av_packet_free(&packet);
            packet = 0;
        }
    }

    void clear() {
        pkt_queue.clear();
        frame_queue.clear();
    }

    void stopWork() {
        pkt_queue.setWork(0);
        frame_queue.setWork(0);
    }

    void startWork() {
        pkt_queue.setWork(1);
        frame_queue.setWork(1);
    }

    SafeQueue<AVPacket *> pkt_queue;
    SafeQueue<AVFrame *> frame_queue;
    double clock = 0;
    volatile int channelId;
    volatile bool isPlaying = false;
    AVRational time_base;
    AVCodecContext *avCodecContext;
    JavaCallHelper *javaCallHelper;
};


#endif //DNPLAYER_AUDIOCHANNEL_H
