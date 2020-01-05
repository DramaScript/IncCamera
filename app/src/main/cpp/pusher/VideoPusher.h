#ifndef PUSHER_VIDEOCHANNEL_H
#define PUSHER_VIDEOCHANNEL_H

#include <inttypes.h>
#include <x264.h>
#include <pthread.h>
#include "../librtmp/rtmp.h"

class VideoPusher {
    typedef void (*VideoCallback)(RTMPPacket* packet);
public:
    VideoPusher();

    ~VideoPusher();

    //创建x264编码器
    void setVideoEncInfo(int width, int height, int fps, int bitrate);

    void encodeData(int8_t *data);

    void setVideoCallback(VideoCallback videoCallback);

private:
    pthread_mutex_t mutex;
    int mWidth;
    int mHeight;
    int mFps;
    int mBitrate;
    x264_t *videoCodec = 0;
    x264_picture_t *pic_in = 0;

    int ySize;
    int uvSize;
    VideoCallback videoCallback;
    void sendSpsPps(uint8_t *sps, uint8_t *pps, int sps_len, int pps_len);

    void sendFrame(int type, uint8_t *payload, int i_payload);
};


#endif //PUSHER_VIDEOCHANNEL_H
