package com.dramascript.inccamera.widget;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.view.SurfaceHolder;

/*
 * Cread By DramaScript on 2019/9/10
 */
public class IncView extends GLSurfaceView {

    //默认正常速度
    private Speed mSpeed = Speed.MODE_NORMAL;

    public enum Speed {
        MODE_EXTRA_SLOW, MODE_SLOW, MODE_NORMAL, MODE_FAST, MODE_EXTRA_FAST
    }

    private IncRenderer incRenderer;

    public void setmPath(String name) {
        if (incRenderer!=null)
            incRenderer.setPath(name);
    }

    public IncView(Context context) {
        this(context, null);
    }

    public IncView(Context context, AttributeSet attrs) {
        super(context, attrs);
        //设置EGL版本
        setEGLContextClientVersion(2);
        incRenderer = new IncRenderer(this);
        setRenderer(incRenderer);
        incRenderer.setPath(System.currentTimeMillis()+"inc");
        //设置按需渲染 当我们调用 requestRender 请求GLThread 回调一次 onDrawFrame
        // 连续渲染 就是自动的回调onDrawFrame
        setRenderMode(RENDERMODE_WHEN_DIRTY);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        super.surfaceDestroyed(holder);
        incRenderer.onSurfaceDestroyed();
    }

    public void setSpeed(Speed speed){
        mSpeed = speed;
    }

    public void startRecord() {
        float speed = 1.f;
        switch (mSpeed) {
            case MODE_EXTRA_SLOW:
                speed = 0.3f;
                break;
            case MODE_SLOW:
                speed = 0.5f;
                break;
            case MODE_NORMAL:
                speed = 1.f;
                break;
            case MODE_FAST:
                speed = 1.5f;
                break;
            case MODE_EXTRA_FAST:
                speed = 3.f;
                break;
        }
        incRenderer.startRecord(speed);
    }

    public void stopRecord() {
        incRenderer.stopRecord();
    }

    public void enableBeauty(boolean isChecked) {
        incRenderer.enableBeauty(isChecked);
    }
}
