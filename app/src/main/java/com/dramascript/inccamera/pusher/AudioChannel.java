package com.dramascript.inccamera.pusher;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/*
 * Cread By DramaScript on 2019/9/20
 *
 * 音频采集
 */
public class AudioChannel {

    private int inputSamples;
    private ExecutorService executor;
    private AudioRecord audioRecord;
    private LivePusher mLivePusher;
    private int channels = 1;
    private boolean isLiving;

    public AudioChannel(LivePusher livePusher) {
        mLivePusher = livePusher;
        executor = Executors.newSingleThreadExecutor();
        //准备录音机 采集pcm 数据
        int channelConfig;
        if (channels == 2) {
            channelConfig = AudioFormat.CHANNEL_IN_STEREO;
        } else {
            channelConfig = AudioFormat.CHANNEL_IN_MONO;
        }


        mLivePusher.native_setAudioEncInfo(44100, channels);
        //16 位 2个字节
        inputSamples = mLivePusher.getInputSamples() * 2;

        //最小需要的缓冲区
        int minBufferSize = AudioRecord.getMinBufferSize(44100, channelConfig, AudioFormat.ENCODING_PCM_16BIT) * 2;
        //1、麦克风 2、采样率 3、声道数 4、采样位
        audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, 44100, channelConfig, AudioFormat.ENCODING_PCM_16BIT, minBufferSize > inputSamples ? minBufferSize : inputSamples);
    }


    public void startLive() {
        isLiving = true;
        executor.submit(new AudioTeask());
    }

    public void stopLive() {
        isLiving = false;
    }


    public void release() {
        audioRecord.release();
    }


    class AudioTeask implements Runnable {

        @Override
        public void run() {
            //启动录音机
            audioRecord.startRecording();
            byte[] bytes = new byte[inputSamples];
            while (isLiving) {
                int len = audioRecord.read(bytes, 0, bytes.length);
                if (len > 0) {
                    //送去编码
                    mLivePusher.native_pushAudio(bytes);
                }
            }
            //停止录音机
            audioRecord.stop();
        }
    }
}
