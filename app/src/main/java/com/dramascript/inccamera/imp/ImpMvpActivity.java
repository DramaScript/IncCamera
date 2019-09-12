package com.dramascript.inccamera.imp;

import com.dramascript.dlibrary.base.BaseMvpActivity;
import com.dramascript.dlibrary.base.BasePresenter;

import butterknife.ButterKnife;
import butterknife.Unbinder;

public abstract class ImpMvpActivity<P extends BasePresenter> extends BaseMvpActivity<P> {

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
