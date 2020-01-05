package com.dramascript.inccamera.pusher;

import android.app.Activity;
import android.hardware.Camera;
import android.view.SurfaceHolder;

/*
 * Cread By DramaScript on 2019/9/20
 *
 * 视频采集
 */
public class VideoChannel implements Camera.PreviewCallback, PusherCameraHelper.OnChangedSizeListener {


    private LivePusher mLivePusher;
    private PusherCameraHelper cameraHelper;
    private int mBitrate;
    private int mFps;
    private boolean isLiving;

    public VideoChannel(LivePusher livePusher, Activity activity, int width, int height, int bitrate, int fps, int cameraId) {
        mLivePusher = livePusher;
        mBitrate = bitrate;
        mFps = fps;
        cameraHelper = new PusherCameraHelper(activity, cameraId, width, height);
        //1、让camerahelper的
        cameraHelper.setPreviewCallback(this);
        //2、回调 真实的摄像头数据宽、高
        cameraHelper.setOnChangedSizeListener(this);
    }

    public void setPreviewDisplay(SurfaceHolder surfaceHolder) {
        cameraHelper.setPreviewDisplay(surfaceHolder);
    }


    /**
     * 得到nv21数据 已经旋转好的
     *
     * @param data
     * @param camera
     */
    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
        if (isLiving) {
            mLivePusher.native_pushVideo(data);
        }
    }

    public void switchCamera() {
        cameraHelper.switchCamera();
    }

    /**
     * 真实摄像头数据的宽、高
     * @param w
     * @param h
     */
    @Override
    public void onChanged(int w, int h) {
        //初始化编码器
        mLivePusher.native_setVideoEncInfo(w, h, mFps, mBitrate);
    }

    public void startLive() {
        isLiving = true;
    }

    public void stopLive() {
        isLiving = false;
    }

    public void release() {
        cameraHelper.release();
    }
}
