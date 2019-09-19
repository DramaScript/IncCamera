extern "C" {

#include <libavutil/imgutils.h>
#include <libavutil/time.h>
#include <libswscale/swscale.h>
}

#include "macro.h"
#include "VideoChannel.h"

void *decode(void *args) {
    VideoChannel *videoChannel = static_cast<VideoChannel *>(args);
    videoChannel->decodePacket();
    return 0;
}

void *synchronize(void *args) {
    VideoChannel *videoChannel = static_cast<VideoChannel *>(args);
    videoChannel->synchronizeFrame();
    return 0;
}


void dropPacket(queue<AVPacket *> &q) {
    //再丢到 当前到下一个关键帧之间所有的帧
    while (!q.empty()) {
        LOGE("丢弃视频......");
        AVPacket *pkt = q.front();
        if (pkt->flags != AV_PKT_FLAG_KEY) {
            q.pop();
            BaseChannel::releaseAvPacket(pkt);
        } else {
            break;
        }
    }
}

void dropFrame(queue<AVFrame *> &q) {
    if (!q.empty()) {
        AVFrame *frame = q.front();
        q.pop();
        BaseChannel::releaseAvFrame(frame);
    }
}

VideoChannel::VideoChannel(int id, JavaCallHelper *javaCallHelper, AVCodecContext *avCodecContext,
                           AVRational base, int fps)
        : BaseChannel(
        id, javaCallHelper, avCodecContext, base), fps(fps) {
    frame_queue.setReleaseHandle(releaseAvFrame);
    frame_queue.setSyncHandle(dropFrame);
}

VideoChannel::~VideoChannel() {
}

void VideoChannel::play() {
    startWork();
    isPlaying = true;
    pthread_create(&pid_video_play, NULL, decode, this);
    pthread_create(&pid_synchronize, NULL, synchronize, this);
}


void VideoChannel::decodePacket() {
    AVPacket *packet = 0;
    while (isPlaying) {
        int ret = pkt_queue.deQueue(packet);
        if (!isPlaying) {
            break;
        }
        if (!ret) {
            continue;
        }

        ret = avcodec_send_packet(avCodecContext, packet);
        releaseAvPacket(packet);
        if (ret == AVERROR(EAGAIN)) {
            //需要更多数据
            continue;
        } else if (ret < 0) {
            //失败
            break;
        }
        AVFrame *frame = av_frame_alloc();
        ret = avcodec_receive_frame(avCodecContext, frame);
        if (ret == AVERROR(EAGAIN)) {
            //需要更多数据
            continue;
        } else if (ret < 0) {
            break;
        }
        while (frame_queue.size() > 100 && isPlaying) {
            av_usleep(1000 * 10);
            continue;
        }
        frame_queue.enQueue(frame);
    }
    releaseAvPacket(packet);
}


void VideoChannel::synchronizeFrame() {
    //转换rgba
    SwsContext *sws_ctx = sws_getContext(
            avCodecContext->width, avCodecContext->height, avCodecContext->pix_fmt,
            avCodecContext->width, avCodecContext->height, AV_PIX_FMT_RGBA,
            SWS_BILINEAR, 0, 0, 0);

    //1s
    double frame_delay = 1.0 / fps;
    uint8_t *dst_data[4];
    int dst_linesize[4];
    av_image_alloc(dst_data, dst_linesize,
                   avCodecContext->width, avCodecContext->height, AV_PIX_FMT_RGBA, 1);
    AVFrame *frame = 0;
    while (isPlaying) {
        int ret = frame_queue.deQueue(frame);
        if (!isPlaying) {
            break;
        }
        if (!ret) {
            continue;
        }
#if 1
        /**
         *  seek需要注意的点：编码器中存在缓存
         *  100s 的图像,用户seek到第 50s 的位置
         *  音频是50s的音频，但是视频 你获得的是100s的视频
         */

        //显示时间戳 什么时候显示这个frame
        if ((clock = frame->best_effort_timestamp) == AV_NOPTS_VALUE) {
            clock = 0;
        }
        //pts 单位就是time_base
        //av_q2d转为双精度浮点数 乘以 pts 得到pts --- 显示时间:秒
        clock = clock * av_q2d(time_base);
        //frame->repeat_pict = 当解码时，这张图片需要要延迟多久显示
        //需要求出扩展延时：
        //extra_delay = repeat_pict / (2*fps) 需要延迟这么久来显示
        double repeat_pict = frame->repeat_pict;
        double extra_delay = repeat_pict / (2 * fps);

        // double frame_base = av_q2d(time_base);
        double delay = extra_delay + frame_delay;
        if (clock == 0) {
            //正常播放
            av_usleep(delay * 1000000);
        } else {
            double audioClock = audioChannel ? audioChannel->clock : 0;
            double diff = fabs(clock - audioClock);
            LOGE("当前和音频比较:%f - %f = %f", clock, audioClock, diff);
            //允许误差 diff > 0.04 &&
            if (audioChannel) {
                //如果视频比音频快，延迟差值播放，否则直接播放
                if (clock > audioClock) {
                    if (diff > 1) {
                        //差的太久了， 那只能慢慢赶 不然就是卡好久
                        av_usleep((delay * 2) * 1000000);
                    } else {
                        //差的不多，尝试一次赶上去
                        av_usleep((delay + diff) * 1000000);
                    }
                } else {
                    //音频比视频快
                    //视频慢了 0.05s 已经比较明显了 (丢帧)
                    if (diff > 1) {
                        //一种可能： 快进了(因为解码器中有缓存数据，这样获得的avframe就和seek的匹配了)
                    } else if (diff >= 0.05) {
                        releaseAvFrame(frame);
                        //执行同步操作 删除到最近的key frame
                        frame_queue.sync();
                        continue;
                    } else {
                        //不休眠 加快速度赶上去
                    }
                }
            } else {
                //正常播放
                av_usleep(delay * 1000000);
            }
        }
#endif
        //diff太大了不回调了
        if (javaCallHelper && !audioChannel) {
            javaCallHelper->onProgress(THREAD_CHILD, clock);
        }
        sws_scale(sws_ctx,
                  reinterpret_cast<const uint8_t *const *>(frame->data), frame->linesize, 0,
                  frame->height,
                  dst_data, dst_linesize);
        //绘画
        renderFrame(dst_data[0], dst_linesize[0], avCodecContext->width, avCodecContext->height);
        releaseAvFrame(frame);
    }
    av_freep(&dst_data[0]);
    isPlaying = false;
    releaseAvFrame(frame);
    sws_freeContext(sws_ctx);
}

void VideoChannel::setRenderCallback(RenderFrame renderFrame) {
    this->renderFrame = renderFrame;
}


void VideoChannel::stop() {
    isPlaying = 0;
    javaCallHelper = 0;
    stopWork();
    pthread_join(pid_synchronize, 0);

    pthread_join(pid_video_play, 0);
}

