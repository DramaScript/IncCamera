extern "C" {
#include <libavutil/imgutils.h>
#include <libavutil/time.h>
}

#include "VideoChannel.h"
#include "macro.h"

void *decode_task(void *args) {
    VideoChannel *channel = static_cast<VideoChannel *>(args);
    channel->decode();
    return 0;
}

void *render_task(void *args) {
    VideoChannel *channel = static_cast<VideoChannel *>(args);
    channel->render();
    return 0;
}

/**
 * 丢包 直到下一个关键帧
 * @param q
 */

void dropAvPacket(queue<AVPacket *> &q) {
    while (!q.empty()) {
        AVPacket *packet = q.front();
        //如果不属于 I 帧
        if (packet->flags != AV_PKT_FLAG_KEY) {
            BaseChannel::releaseAvPacket(&packet);
            q.pop();
        } else {
            break;
        }
    }
}

/**
 * 丢已经解码的图片
 * @param q
 */
void dropAvFrame(queue<AVFrame *> &q) {
    if (!q.empty()) {
        AVFrame *frame = q.front();
        BaseChannel::releaseAvFrame(&frame);
        q.pop();
    }
}

VideoChannel::VideoChannel(int id, AVCodecContext *avCodecContext, AVRational time_base, int fps)
        : BaseChannel(id,
                      avCodecContext, time_base) {

    this->fps = fps;
    //  用于 设置一个 同步操作 队列的一个函数指针
//    packets.setSyncHandle(dropAvPacket);
    frames.setSyncHandle(dropAvFrame);
}

VideoChannel::~VideoChannel() {

}

void VideoChannel::setAudioChannel(AudioChannel *audioChannel) {
    this->audioChannel = audioChannel;
}

void VideoChannel::play() {
    isPlaying = 1;
    frames.setWork(1);
    packets.setWork(1);
    //1、解码
    pthread_create(&pid_decode, 0, decode_task, this);
    //2、播放
    pthread_create(&pid_render, 0, render_task, this);
}

//解码
void VideoChannel::decode() {
    AVPacket *packet = 0;
    while (isPlaying) {
        //取出一个数据包
        int ret = packets.pop(packet);
        if (!isPlaying) {
            break;
        }
        //取出失败
        if (!ret) {
            continue;
        }
        //把包丢给解码器
        ret = avcodec_send_packet(avCodecContext, packet);
        releaseAvPacket(&packet);
        //重试
        if (ret != 0) {
            break;
        }
        //代表了一个图像 (将这个图像先输出来)
        AVFrame *frame = av_frame_alloc();
        //从解码器中读取 解码后的数据包 AVFrame
        ret = avcodec_receive_frame(avCodecContext, frame);
        //需要更多的数据才能够进行解码
        if (ret == AVERROR(EAGAIN)) {
            continue;
        } else if (ret != 0) {
            break;
        }
        //再开一个线程 来播放 (流畅度)
        frames.push(frame);
    }
    releaseAvPacket(&packet);
}

//播放
void VideoChannel::render() {
    //目标： RGBA
    swsContext = sws_getContext(
            avCodecContext->width, avCodecContext->height, avCodecContext->pix_fmt,
            avCodecContext->width, avCodecContext->height, AV_PIX_FMT_RGBA,
            SWS_BILINEAR, 0, 0, 0);
    //每个画面 刷新的间隔 单位：秒
    double frame_delays = 1.0 / fps;
    AVFrame *frame = 0;
    //指针数组
    uint8_t *dst_data[4];
    int dst_linesize[4];
    av_image_alloc(dst_data, dst_linesize,
                   avCodecContext->width, avCodecContext->height, AV_PIX_FMT_RGBA, 1);
    while (isPlaying) {
        int ret = frames.pop(frame);
        if (!isPlaying) {
            break;
        }
        //src_linesize: 表示每一行存放的 字节长度
        sws_scale(swsContext, reinterpret_cast<const uint8_t *const *>(frame->data),
                  frame->linesize, 0,
                  avCodecContext->height,
                  dst_data,
                  dst_linesize);
#if 1
        //获得 当前这一个画面 播放的相对的时间
        double clock = frame->best_effort_timestamp * av_q2d(time_base);
        //额外的间隔时间
        double extra_delay = frame->repeat_pict / (2 * fps);
        // 真实需要的间隔时间
        double delays = extra_delay + frame_delays;
        if (!audioChannel) {
            //休眠
//        //视频快了
//        av_usleep(frame_delays*1000000+x);
//        //视频慢了
//        av_usleep(frame_delays*1000000-x);
            av_usleep(delays * 1000000);
        } else {
            if (clock == 0) {
                av_usleep(delays * 1000000);
            } else {
                //比较音频与视频
                double audioClock = audioChannel->clock;
                //间隔 音视频相差的间隔
                double diff = clock - audioClock;
                if (diff > 0) {
                    //大于0 表示视频比较快
                    LOGE("视频快了：%lf",diff);
                    av_usleep((delays + diff) * 1000000);
                } else if (diff < 0) {
                    //小于0 表示音频比较快
                    LOGE("音频快了：%lf",diff);
                    // 视频包积压的太多了 （丢包）
                    if (fabs(diff) >= 0.05) {
                        releaseAvFrame(&frame);
                        //丢包
                        frames.sync();
                        continue;
                    }else{
                        //不睡了 快点赶上 音频
                    }
                }
            }
        }
#endif
        //回调出去进行播放
        callback(dst_data[0], dst_linesize[0], avCodecContext->width, avCodecContext->height);
        releaseAvFrame(&frame);
    }
    av_freep(&dst_data[0]);
    releaseAvFrame(&frame);
}

void VideoChannel::setRenderFrameCallback(RenderFrameCallback callback) {
    this->callback = callback;
}