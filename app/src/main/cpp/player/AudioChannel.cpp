extern "C" {
#include <libavutil/time.h>
}

#include "AudioChannel.h"
#include "../macro.h"

void *audioPlay(void *args) {
    AudioChannel *audio = static_cast<AudioChannel *>(args);
    audio->initOpenSL();
    return 0;
}

void *audioDecode(void *args) {
    AudioChannel *audio = static_cast<AudioChannel *>(args);
    audio->decode();
    return 0;
}

//void dropAudioFrame(queue<AVFrame *> &q) {
//    int size = q.size() - 200;
//    while (size > 0) {
//        LOGE("丢弃音频......");
//        AVFrame *frame = q.front();
//        q.pop();
//        BaseChannel::releaseAvFrame(frame);
//        size--;
//    }
//}

AudioChannel::AudioChannel(int id, JavaCallHelper *javaCallHelper, AVCodecContext *avCodecContext,
                           AVRational base) : BaseChannel(
        id, javaCallHelper, avCodecContext, base) {

    //根据布局获取声道数
    out_channels = av_get_channel_layout_nb_channels(AV_CH_LAYOUT_STEREO);
    out_samplesize = av_get_bytes_per_sample(AV_SAMPLE_FMT_S16);
    out_sample_rate = 44100;
    //CD音频标准
    //44100 双声道 2字节
    buffer = (uint8_t *) malloc(out_sample_rate * out_samplesize * out_channels);
//    frame_queue.setSyncHandle(dropAudioFrame);
}


AudioChannel::~AudioChannel() {
    free(buffer);
    buffer = 0;

}


void AudioChannel::play() {
    //重采样为固定的双声道 16位 44100
    swr_ctx = swr_alloc_set_opts(0, AV_CH_LAYOUT_STEREO, AV_SAMPLE_FMT_S16, out_sample_rate,
                                 avCodecContext->channel_layout,
                                 avCodecContext->sample_fmt,
                                 avCodecContext->sample_rate, 0, 0);
    swr_init(swr_ctx);
    startWork();
    isPlaying = true;
    pthread_create(&pid_audio_play, NULL, audioPlay, this);
    pthread_create(&pid_audio_decode, NULL, audioDecode, this);
}


//第一次主动调用在调用线程
//之后在新线程中回调
void bqPlayerCallback(SLAndroidSimpleBufferQueueItf bq, void *context) {
    AudioChannel *audioChannel = static_cast<AudioChannel *>(context);
    int datalen = audioChannel->getPcm();
    if (datalen > 0) {
        (*bq)->Enqueue(bq, audioChannel->buffer, datalen);
    }
}


void AudioChannel::decode() {
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


void AudioChannel::initOpenSL() {
    //创建引擎
    SLresult result;
    // 创建引擎engineObject
    result = slCreateEngine(&engineObject, 0, NULL, 0, NULL, NULL);
    if (SL_RESULT_SUCCESS != result) {
        return;
    }
    // 初始化引擎engineObject
    result = (*engineObject)->Realize(engineObject, SL_BOOLEAN_FALSE);
    if (SL_RESULT_SUCCESS != result) {
        return;
    }
    // 获取引擎接口engineEngine
    result = (*engineObject)->GetInterface(engineObject, SL_IID_ENGINE,
                                           &engineInterface);
    if (SL_RESULT_SUCCESS != result) {
        return;
    }

    // 创建混音器outputMixObject
    result = (*engineInterface)->CreateOutputMix(engineInterface, &outputMixObject, 0, 0, 0);
    if (SL_RESULT_SUCCESS != result) {
        return;
    }

    // 初始化混音器outputMixObject
    result = (*outputMixObject)->Realize(outputMixObject, SL_BOOLEAN_FALSE);
    if (SL_RESULT_SUCCESS != result) {
        return;
    }


    /**
     * 配置输入声音信息
     */
    //创建buffer缓冲类型的队列 2个队列
    SLDataLocator_AndroidSimpleBufferQueue android_queue = {SL_DATALOCATOR_ANDROIDSIMPLEBUFFERQUEUE,
                                                            2};
    //pcm数据格式
    SLDataFormat_PCM pcm = {SL_DATAFORMAT_PCM, 2, SL_SAMPLINGRATE_44_1, SL_PCMSAMPLEFORMAT_FIXED_16,
                            SL_PCMSAMPLEFORMAT_FIXED_16,
                            SL_SPEAKER_FRONT_LEFT | SL_SPEAKER_FRONT_RIGHT,
                            SL_BYTEORDER_LITTLEENDIAN};

    //数据源 将上述配置信息放到这个数据源中
    SLDataSource slDataSource = {&android_queue, &pcm};


    //设置混音器
    SLDataLocator_OutputMix outputMix = {SL_DATALOCATOR_OUTPUTMIX, outputMixObject};
    SLDataSink audioSnk = {&outputMix, NULL};
    //需要的接口
    const SLInterfaceID ids[1] = {SL_IID_BUFFERQUEUE};
    const SLboolean req[1] = {SL_BOOLEAN_TRUE};
    //创建播放器
    (*engineInterface)->CreateAudioPlayer(engineInterface, &bqPlayerObject, &slDataSource,
                                          &audioSnk, 1,
                                          ids, req);
    //初始化播放器
    (*bqPlayerObject)->Realize(bqPlayerObject, SL_BOOLEAN_FALSE);

//    得到接口后调用  获取Player接口
    (*bqPlayerObject)->GetInterface(bqPlayerObject, SL_IID_PLAY, &bqPlayerInterface);

//    获得播放器接口
    (*bqPlayerObject)->GetInterface(bqPlayerObject, SL_IID_BUFFERQUEUE,
                                    &bqPlayerBufferQueue);
    //设置回调
    (*bqPlayerBufferQueue)->RegisterCallback(bqPlayerBufferQueue, bqPlayerCallback, this);
//    设置播放状态
    (*bqPlayerInterface)->SetPlayState(bqPlayerInterface, SL_PLAYSTATE_PLAYING);

    bqPlayerCallback(bqPlayerBufferQueue, this);
}


int AudioChannel::getPcm() {
    int data_size = 0;
    AVFrame *frame = 0;
    while (isPlaying) {
        int ret = frame_queue.deQueue(frame);
        if (!isPlaying) {
            break;
        }
        if (!ret) {
            continue;
        }
        // 计算转换后的sample个数  类似 a * b / c
        // swr_get_delay： 延迟时间:  输入了10个 数据，可能这次转换只转换了8个数据，那么还剩余2个 这一个函数就是得到上一次剩余的这个2
        // av_rescale_rnd： 以3为单位的1 转为以2为单位
        // 10个2=20
        // 转成 以4 为单位
        // 10*4/2
        uint64_t dst_nb_samples = av_rescale_rnd(
                swr_get_delay(swr_ctx, frame->sample_rate) + frame->nb_samples,
                out_sample_rate,
                frame->sample_rate,
                AV_ROUND_UP);
        // 转换，返回值为转换后的sample个数
        int nb = swr_convert(swr_ctx, &buffer, dst_nb_samples,
                             (const uint8_t **) frame->data, frame->nb_samples);
        //转换后多少数据
        data_size = nb * out_channels * out_samplesize;
        //音频的时间
        clock = frame->best_effort_timestamp * av_q2d(time_base);
        if (javaCallHelper) {
            javaCallHelper->onProgress(THREAD_CHILD, clock);
        }
        break;
    }
    releaseAvFrame(frame);
    return data_size;
}

void AudioChannel::releaseOpenSL() {
    //设置停止状态
    if (bqPlayerInterface) {
        (*bqPlayerInterface)->SetPlayState(bqPlayerInterface, SL_PLAYSTATE_STOPPED);
        bqPlayerInterface = 0;
    }
    //销毁播放器
    if (bqPlayerObject) {
        (*bqPlayerObject)->Destroy(bqPlayerObject);
        bqPlayerObject = 0;
        bqPlayerBufferQueue = 0;
    }
    //销毁混音器
    if (outputMixObject) {
        (*outputMixObject)->Destroy(outputMixObject);
        outputMixObject = 0;
    }
    //销毁引擎
    if (engineObject) {
        (*engineObject)->Destroy(engineObject);
        engineObject = 0;
        engineInterface = 0;
    }
}

void AudioChannel::stop() {
    isPlaying = 0;
    javaCallHelper = 0;
    stopWork();
    pthread_join(pid_audio_decode, 0);
    pthread_join(pid_audio_play, 0);

    releaseOpenSL();
    if (swr_ctx) {
        swr_free(&swr_ctx);
        swr_ctx = 0;
    }
}


