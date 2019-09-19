package com.dramascript.inccamera.mvp.view.activity;

import android.content.DialogInterface;
import android.content.res.Configuration;
import android.support.v7.app.AlertDialog;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.Toast;

import com.blankj.utilcode.util.ToastUtils;
import com.dramascript.dlibrary.base.DInject;
import com.dramascript.inccamera.R;
import com.dramascript.inccamera.imp.ImpBaseActivity;
import com.dramascript.inccamera.player.IncPlayer;

import butterknife.BindView;

/*
 * Cread By DramaScript on 2019/9/17
 */
@DInject(
        contentViewId = R.layout.activity_play
)
public class IncPlayerActivity extends ImpBaseActivity {

    @BindView(R.id.surfaceView)
    SurfaceView surfaceView;
    private IncPlayer incPlayer;
    public String url = "";


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
//        showInput();
        incPlayer = new IncPlayer();
        incPlayer.setSurfaceView(surfaceView);
        incPlayer.setOnPrepareListener(new IncPlayer.OnPrepareListener() {

            @Override
            public void onPrepare() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(IncPlayerActivity.this, "开始播放", Toast.LENGTH_SHORT).show();
                    }
                });
                incPlayer.start();
            }
        });

        url = getIntent().getStringExtra("url");
        incPlayer.setDataSource(url);
    }

    private void showInput() {
        final EditText et = new EditText(this);
        et.setText("rtmp://live.hkstv.hk.lxdns.com/live/hks");
        new AlertDialog.Builder(this).setTitle("请输入RTMP播放地址")
                .setView(et)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        url = et.getText().toString();
                        if (!url.startsWith("rtmp")){
                            ToastUtils.showShort("地址错误");
                            return;
                        }
                    }
                }).setNegativeButton("取消",null).show();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
//            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager
//                    .LayoutParams.FLAG_FULLSCREEN);
        } else {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
        setContentView(R.layout.activity_play);
        SurfaceView surfaceView = findViewById(R.id.surfaceView);
        incPlayer.setSurfaceView(surfaceView);
    }

    @Override
    protected void onResume() {
        super.onResume();
        incPlayer.prepare();
    }

    @Override
    protected void onStop() {
        super.onStop();
        incPlayer.stop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        incPlayer.release();
    }
}
