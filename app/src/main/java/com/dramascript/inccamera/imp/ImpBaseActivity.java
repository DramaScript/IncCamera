package com.dramascript.inccamera.imp;

import com.dramascript.dlibrary.base.BaseActivity;

import butterknife.ButterKnife;
import butterknife.Unbinder;

/*
 * Cread By DramaScript on 2019/3/5
 */
public abstract class ImpBaseActivity extends BaseActivity {

    private Unbinder bind;

    @Override
    protected void init() {
        super.init();
        bind = ButterKnife.bind(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (bind!=null)
            bind.unbind();
    }

}
