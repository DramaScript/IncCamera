package com.dramascript.inccamera.mvp.view.activity;

import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.RadioGroup;

import com.blankj.utilcode.util.ToastUtils;
import com.dramascript.dlibrary.base.DInject;
import com.dramascript.inccamera.R;
import com.dramascript.inccamera.imp.ImpBaseActivity;
import com.dramascript.inccamera.widget.CircleProgressBar;
import com.dramascript.inccamera.widget.IncView;
import com.dramascript.inccamera.widget.RecordButton;

import butterknife.BindView;

@DInject(
        contentViewId = R.layout.ac_camera
)
public class CameraActivity extends ImpBaseActivity {

    @BindView(R.id.incView)
    IncView incView;
    @BindView(R.id.cpb)
    CircleProgressBar cpb;
    @BindView(R.id.btn_record)
    RecordButton recordButton;
    @BindView(R.id.rg_speed)
    RadioGroup radioGroup;
    @BindView(R.id.beauty)
    CheckBox checkBox;
    private boolean isRecord;
    private long time;

    @Override
    protected void init() {
        super.init();

        recordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isRecord){
                    if (System.currentTimeMillis()-time<5000){
                        ToastUtils.showShort("录制时间太短了");
                    }else {
                        isRecord = false;
                        incView.stopRecord();
                        cpb.stop();
                        ToastUtils.showShort("停止录制");
                    }
                }else {
                    incView.setmPath(System.currentTimeMillis()+"inc");
                    time = System.currentTimeMillis();
                    isRecord = true;
                    incView.startRecord();
                    cpb.setProgress(1f, 66000);
                    ToastUtils.showShort("开始录制");
                }
            }
        });
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            /**
             * 选择录制模式
             * @param group
             * @param checkedId
             */
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId){
                    case R.id.rb_extra_slow: //极慢
                        incView.setSpeed(IncView.Speed.MODE_EXTRA_SLOW);
                        break;
                    case R.id.rb_slow:
                        incView.setSpeed(IncView.Speed.MODE_SLOW);
                        break;
                    case R.id.rb_normal:
                        incView.setSpeed(IncView.Speed.MODE_NORMAL);
                        break;
                    case R.id.rb_fast:
                        incView.setSpeed(IncView.Speed.MODE_FAST);
                        break;
                    case R.id.rb_extra_fast: //极快
                        incView.setSpeed(IncView.Speed.MODE_EXTRA_FAST);
                        break;
                }
            }
        });

        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                incView.enableBeauty(isChecked);
            }
        });
    }
}
