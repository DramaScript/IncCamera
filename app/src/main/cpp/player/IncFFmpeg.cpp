#include "IncFFmpeg.h"
#include "macro.h"


void *prepareFFmpeg_(void *args) {
    IncFFmpeg *dnfFmpeg = static_cast<IncFFmpeg *>(args);
    dnfFmpeg->prepareFFmpeg();
    return 0;

}


IncFFmpeg::IncFFmpeg(JavaCallHelper *javaCallHelper, const char *dataSource)
        : javaCallHelper(javaCallHelper) {
    url = new char[strlen(dataSource) + 1];
    strcpy(url, dataSource);
    isPlaying = false;
    duration = 0;
    pthread_mutex_init(&seekMutex, 0);
}

IncFFmpeg::~IncFFmpeg() {
    pthread_mutex_destroy(&seekMutex);
    javaCallHelper = 0;
    DELETE(url);
}

void IncFFmpeg::prepare() {
    pthread_create(&pid_prepare, NULL, prepareFFmpeg_, this);
}


void IncFFmpeg::prepareFFmpeg() {
    //todo 最新版本好像不用 regiest_all了
    avformat_network_init();
    // 代表一个 视频/音频 包含了视频、音频的各种信息
    formatContext = avformat_alloc_context();
    //1、打开URL
    AVDictionary *opts = NULL;
    //设置超时5秒  这里记住是 stimeout  不是 timeout
    av_dict_set(&opts, "stimeout", "5000000", 0);
    //强制指定AVFormatContext中AVInputFormat的。这个参数一般情况下可以设置为NULL，这样FFmpeg可以自动检测AVInputFormat。
    //输入文件的封装格式
//    av_find_input_format("avi")
    int ret = avformat_open_input(&formatContext, url, NULL, &opts);
    //av_err2str(ret)
    LOGE("%s open %d  %s", url, ret, av_err2str(ret));
    if (ret != 0) {
        if (javaCallHelper) {
            javaCallHelper->onError(THREAD_CHILD, FFMPEG_CAN_NOT_OPEN_URL);
        }
        return;
    }
    //2.查找流
    if (avformat_find_stream_info(formatContext, NULL) < 0) {
        if (javaCallHelper) {
            javaCallHelper->onError(THREAD_CHILD, FFMPEG_CAN_NOT_FIND_STREAMS);
        }
        return;
    }
    //视频时长（单位：微秒us，转换为秒需要除以1000000）
    duration = formatContext->duration / 1000000;
    for (int i = 0; i < formatContext->nb_streams; ++i) {
        AVCodecParameters *codecpar = formatContext->streams[i]->codecpar;
        //找到解码器
        AVCodec *dec = avcodec_find_decoder(codecpar->codec_id);
        if (!dec) {
            if (javaCallHelper) {
                javaCallHelper->onError(THREAD_CHILD, FFMPEG_FIND_DECODER_FAIL);
            }
            return;
        }
        //创建上下文
        AVCodecContext *codecContext = avcodec_alloc_context3(dec);
        if (!codecContext) {
            if (javaCallHelper)
                javaCallHelper->onError(THREAD_CHILD, FFMPEG_ALLOC_CODEC_CONTEXT_FAIL);
            return;
        }
        //复制参数
        if (avcodec_parameters_to_context(codecContext, codecpar) < 0) {
            if (javaCallHelper)
                javaCallHelper->onError(THREAD_CHILD, FFMPEG_CODEC_CONTEXT_PARAMETERS_FAIL);
            return;
        }
        //打开解码器
        if (avcodec_open2(codecContext, dec, 0) != 0) {
            if (javaCallHelper)
                javaCallHelper->onError(THREAD_CHILD, FFMPEG_OPEN_DECODER_FAIL);
            return;
        }
        //时间基
        AVRational base = formatContext->streams[i]->time_base;
        //音频
        if (codecpar->codec_type == AVMEDIA_TYPE_AUDIO) {
            LOGE("创建audio");
            audioChannel = new AudioChannel(i, javaCallHelper, codecContext, base);
        } else if (codecpar->codec_type == AVMEDIA_TYPE_VIDEO) {
            LOGE("创建video");
            //视频
//            int num = formatContext->streams[i]->avg_frame_rate.num;
//            int den = formatContext->streams[i]->avg_frame_rate.den;
            //帧率 num：分子
            int fps = av_q2d(formatContext->streams[i]->avg_frame_rate);
            videoChannel = new VideoChannel(i, javaCallHelper, codecContext, base, fps);
            videoChannel->setRenderCallback(renderFrame);
        }
    }

    //音视频都没有
    if (!audioChannel && !videoChannel) {
        if (javaCallHelper)
            javaCallHelper->onError(THREAD_CHILD, FFMPEG_NOMEDIA);
        return;
    }
    if (javaCallHelper)
        javaCallHelper->onParpare(THREAD_CHILD);
}

void *startThread(void *args) {
    IncFFmpeg *ffmpeg = static_cast<IncFFmpeg *>(args);
    ffmpeg->play();
    return 0;
}


void IncFFmpeg::start() {
    isPlaying = true;
    if (audioChannel) {
        audioChannel->play();
    }
    if (videoChannel) {
        videoChannel->audioChannel = audioChannel;
        videoChannel->play();
    }
    pthread_create(&pid_play, NULL, startThread, this);
}


void IncFFmpeg::play() {
    int ret = 0;
    while (isPlaying) {
        //读取文件的时候没有网络请求，一下子读完了，可能导致oom
        if (audioChannel && audioChannel->pkt_queue.size() > 100) {
            av_usleep(1000 * 10);
            continue;
        }
        if (videoChannel && videoChannel->pkt_queue.size() > 100) {
            av_usleep(1000 * 10);
            continue;
        }
        //锁住formatContext
        pthread_mutex_lock(&seekMutex);
        //读取包
        AVPacket *packet = av_packet_alloc();
        // 从媒体中读取音频、视频包
        ret = av_read_frame(formatContext, packet);
        pthread_mutex_unlock(&seekMutex);
        if (ret == 0) {
            //将数据包加入队列
            if (audioChannel && packet->stream_index == audioChannel->channelId) {
                audioChannel->pkt_queue.enQueue(packet);
            } else if (videoChannel && packet->stream_index == videoChannel->channelId) {
                videoChannel->pkt_queue.enQueue(packet);
            }
        } else if (ret == AVERROR_EOF) {
            //读取完毕 但是不一定播放完毕
            if (videoChannel->pkt_queue.empty() && videoChannel->frame_queue.empty() &&
                audioChannel->pkt_queue.empty() && audioChannel->frame_queue.empty()) {
                LOGE("播放完毕。。。");
                break;
            }
            //因为seek 的存在，就算读取完毕，依然要循环 去执行av_read_frame(否则seek了没用...)
        } else {
            break;
        }

    }
    isPlaying = 0;
    audioChannel->stop();
    videoChannel->stop();
}

void IncFFmpeg::setRenderCallback(RenderFrame renderFrame) {
    this->renderFrame = renderFrame;
}

void *async_stop(void *args) {
    IncFFmpeg *ffmpeg = static_cast<IncFFmpeg *>(args);
    pthread_join(ffmpeg->pid_prepare, 0);
    ffmpeg->isPlaying = 0;
    pthread_join(ffmpeg->pid_play, 0);
    DELETE(ffmpeg->audioChannel);
    DELETE(ffmpeg->videoChannel);
    if (ffmpeg->formatContext) {
        avformat_close_input(&ffmpeg->formatContext);
        avformat_free_context(ffmpeg->formatContext);
        ffmpeg->formatContext = NULL;
    }
    DELETE(ffmpeg);
    LOGE("释放");
    return 0;
}

void IncFFmpeg::stop() {
    javaCallHelper = 0;
    if (audioChannel) {
        audioChannel->javaCallHelper = 0;
    }
    if (videoChannel) {
        videoChannel->javaCallHelper = 0;
    }
    pthread_create(&pid_stop, 0, async_stop, this);
}


void IncFFmpeg::seek(int i) {
    //进去必须 在0- duration 范围之类
    if (i< 0 || i >= duration) {
        return;
    }
    if (!audioChannel && !videoChannel) {
        return;
    }
    if (!formatContext) {
        return;
    }
    isSeek = 1;
    pthread_mutex_lock(&seekMutex);
    //单位是 微妙
    int64_t seek = i * 1000000;
    //seek到请求的时间 之前最近的关键帧
    // 只有从关键帧才能开始解码出完整图片
    av_seek_frame(formatContext, -1,seek, AVSEEK_FLAG_BACKWARD);
//    avformat_seek_file(formatContext, -1, INT64_MIN, seek, INT64_MAX, 0);
    // 音频、与视频队列中的数据 是不是就可以丢掉了？
    if (audioChannel) {
        //暂停队列
        audioChannel->stopWork();
        //可以清空缓存
//        avcodec_flush_buffers();
        audioChannel->clear();
        //启动队列
        audioChannel->startWork();
    }
    if (videoChannel) {
        videoChannel->stopWork();
        videoChannel->clear();
        videoChannel->startWork();
    }
    pthread_mutex_unlock(&seekMutex);
    isSeek = 0;
}


