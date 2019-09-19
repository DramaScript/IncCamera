package com.dramascript.inccamera.mvp.view.activity;

import android.content.DialogInterface;
import android.content.res.Configuration;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.Toast;

import com.blankj.utilcode.util.LogUtils;
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
public class IncPlayerActivity extends ImpBaseActivity implements SeekBar.OnSeekBarChangeListener {

    @BindView(R.id.surfaceView)
    SurfaceView surfaceView;
    @BindView(R.id.seekBar)
    SeekBar seekBar;
    private IncPlayer incPlayer;
    public String url;
    private int progress;
    private boolean isTouch;
    private boolean isSeek;
    private boolean isLocal;


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

        incPlayer = new IncPlayer();
        incPlayer.setSurfaceView(surfaceView);
        url = getIntent().getStringExtra("url");
        if (TextUtils.isEmpty(url)){
            showInput();
        }else {
            if (url.startsWith("/sdcard")){
                isLocal = true;
                seekBar.setVisibility(View.GONE);
            }
            incPlayer.setDataSource(url);
        }
        incPlayer.setOnPrepareListener(new IncPlayer.OnPrepareListener() {
            /**
             * 视频信息获取完成 随时可以播放的时候回调
             */
            @Override
            public void onPrepared() {
                //获得时间
                int duration = incPlayer.getDuration();
                //直播： 时间就是0
                if (duration != 0){
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            //显示进度条
                            seekBar.setVisibility(View.VISIBLE);
                        }
                    });
                }
                incPlayer.start();
            }
        });
        incPlayer.setOnErrorListener(new IncPlayer.OnErrorListener() {
            @Override
            public void onError(int error) {

            }
        });
        incPlayer.setOnProgressListener(new IncPlayer.OnProgressListener() {

            @Override
            public void onProgress(final int progress2) {
                if (!isTouch) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            int duration = incPlayer.getDuration();
                            //如果是直播
                            if (duration != 0) {
                                if (isSeek){
                                    isSeek = false;
                                    return;
                                }
                                //更新进度 计算比例
                                seekBar.setProgress(progress2 * 100 / duration);
                            }
                        }
                    });
                }
            }
        });
        seekBar.setOnSeekBarChangeListener(this);

//        incPlayer.setDataSource("/sdcard/1568625054953inc.mp4");

    }

    private void showInput() {
        final EditText et = new EditText(this);
        et.setText("http://hdl.miaobolive.com/live/ceb674ade025be36baa7e996194e1199.flv");
        new AlertDialog.Builder(this).setTitle("请输入RTMP/HTTP播放地址")
                .setView(et)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        url = et.getText().toString();
                        if (url.startsWith("rtmp:")||url.startsWith("http:")){
                            isLocal = false;
                            seekBar.setVisibility(View.GONE);
                            incPlayer.setDataSource(url);
                            incPlayer.prepare();
                        }else if (url.startsWith("/sdcard")){
                            isLocal = true;
                            seekBar.setVisibility(View.VISIBLE);
                            incPlayer.setDataSource(url);
                            incPlayer.prepare();
                        }else {
                            isLocal = false;
                            seekBar.setVisibility(View.GONE);
                            finish();
                            ToastUtils.showShort("地址错误");
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
        incPlayer.setDataSource(url);
        seekBar = findViewById(R.id.seekBar);
        if (isLocal){
            seekBar.setVisibility(View.VISIBLE);
        }else {
            seekBar.setVisibility(View.GONE);
        }
        seekBar.setOnSeekBarChangeListener(this);
        seekBar.setProgress(progress);
    }


    @Override
    protected void onResume() {
        super.onResume();
        if (!TextUtils.isEmpty(url)){
            incPlayer.setDataSource(url);
            incPlayer.prepare();
        }
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

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        isTouch = true;
    }

    /**
     * 停止拖动的时候回调
     * @param seekBar
     */
    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        isSeek = true;
        isTouch = false;
        progress = incPlayer.getDuration() * seekBar.getProgress() / 100;
        //进度调整
        incPlayer.seek(progress);
    }
}
