package com.dramascript.inccamera;


import android.support.design.widget.FloatingActionButton;
import android.view.View;

import com.blankj.utilcode.util.ActivityUtils;
import com.dramascript.dlibrary.base.BaseActivity;
import com.dramascript.dlibrary.base.DInject;

@DInject(
        contentViewId = R.layout.activity_main
)
public class MainActivity extends BaseActivity {


    @Override
    protected void initToolbar(String title, boolean isWhite) {
        super.initToolbar("", isWhite);// title不为null显示toolbar
    }

    @Override
    protected void init() {
        super.init();
        setToolbarColor(getResources().getColor(R.color.colorPrimary));
        setToolbarTitle(getString(R.string.app_name));
        setToolbarTitleColor(getResources().getColor(R.color.white));
        setBackGone();
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ActivityUtils.startActivity(CameraActivity.class);
            }
        });
    }

}
