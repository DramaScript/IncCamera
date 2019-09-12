package com.dramascript.inccamera.imp;

import android.view.View;

import com.dramascript.dlibrary.base.BaseFragment;

import butterknife.ButterKnife;
import butterknife.Unbinder;

/*
 * Cread By DramaScript on 2019/3/5
 */
public abstract class ImpBaseFragment extends BaseFragment {

    private Unbinder bind;

    @Override
    protected void init(View view) {
        super.init(view);
        bind = ButterKnife.bind(this, view);
    }

    @Override
    public void onDestroyView() {
        if (bind!=null)
            bind.unbind();
        super.onDestroyView();
    }
}
