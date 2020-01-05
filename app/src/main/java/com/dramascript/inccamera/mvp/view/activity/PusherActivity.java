package com.dramascript.inccamera.mvp.view.activity;

import android.hardware.Camera;
import android.view.SurfaceView;
import android.view.View;

import com.dramascript.dlibrary.base.DInject;
import com.dramascript.inccamera.R;
import com.dramascript.inccamera.imp.ImpBaseActivity;
import com.dramascript.inccamera.pusher.LivePusher;

import butterknife.BindView;

@DInject(
        contentViewId = R.layout.ac_pusher
)
public class PusherActivity extends ImpBaseActivity {

    private LivePusher livePusher;
    @BindView(R.id.surfaceView)
    SurfaceView surfaceView;

    @Override
    protected void initToolbar(String title, boolean isWhite) {
        super.initToolbar("", isWhite);// title不为null显示toolbar
    }

    @Override
    protected void init() {
        super.init();
        setToolbarColor(getResources().getColor(R.color.colorPrimary));
        setToolbarTitle(getString(R.string.app_player));
        setToolbarTitleColor(getResources().getColor(R.color.white));
        livePusher = new LivePusher(this, 800, 480, 800_000, 10, Camera.CameraInfo.CAMERA_FACING_BACK);
        //  设置摄像头预览的界面
        livePusher.setPreviewDisplay(surfaceView.getHolder());
    }

    public void switchCamera(View view) {
        livePusher.switchCamera();
    }

    public void startLive(View view) {
        livePusher.startLive("rtmp://118.190.210.169:1935/stream/myxx");
    }

    public void stopLive(View view) {
        livePusher.stopLive();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        livePusher.release();
    }
}
