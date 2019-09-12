package com.dramascript.inccamera;

import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.RadioGroup;

import com.dramascript.dlibrary.base.DInject;
import com.dramascript.inccamera.imp.ImpBaseActivity;
import com.dramascript.inccamera.widget.IncView;
import com.dramascript.inccamera.widget.RecordButton;

@DInject(
        contentViewId = R.layout.ac_camera
)
public class CameraActivity extends ImpBaseActivity {

    IncView incView;

    @Override
    protected void init() {
        incView = findViewById(R.id.incView);

        RecordButton recordButton = findViewById(R.id.btn_record);
        recordButton.setOnRecordListener(new RecordButton.OnRecordListener() {
            /**
             * 开始录制
             */
            @Override
            public void onRecordStart() {
                incView.startRecord();
            }

            /**
             * 停止录制
             */
            @Override
            public void onRecordStop() {
                incView.stopRecord();
            }
        });
        RadioGroup radioGroup = findViewById(R.id.rg_speed);
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

        ((CheckBox)findViewById(R.id.beauty)).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                incView.enableBeauty(isChecked);
            }
        });
    }
}
