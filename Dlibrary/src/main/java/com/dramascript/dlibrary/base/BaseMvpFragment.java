package com.dramascript.dlibrary.base;

import android.text.TextUtils;
import android.view.View;

import com.blankj.utilcode.util.ToastUtils;

/*
 * Cread By DramaScript on 2019/3/5
 */
public abstract class BaseMvpFragment<P extends BasePresenter> extends BaseFragment implements BaseView {

    protected P mPresenter;

    @Override
    protected void init(View view) {
        mPresenter = initPresenter();
        if (mPresenter != null)
            mPresenter.attachView(this);

        super.init(view);
    }

    protected abstract P initPresenter();

    @Override
    public void showError(String msg) {
        if (!TextUtils.isEmpty(msg))
            ToastUtils.showShort(msg);
    }

    @Override
    public void showMsg(String msg) {
        if (!TextUtils.isEmpty(msg))
            ToastUtils.showShort(msg);
    }

    @Override
    public void onDestroy() {
        if (mPresenter != null) {
            mPresenter.detachView();
            mPresenter = null;
        }
        super.onDestroy();
    }
}
