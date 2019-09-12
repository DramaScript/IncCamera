package com.dramascript.dlibrary.base;

import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;

/*
 * Cread By DramaScript on 2019/3/5
 */
public class RxPresenter<T extends BaseView> implements BasePresenter<T> {

    protected T mView;
    //将所有正在处理的Subscription都添加到CompositeSubscription中。统一退出的时候注销观察
    protected CompositeDisposable mCompositeDisposable;

    /**
     * 添加Disposable
     *
     * @param subscription
     */
    protected void addDisposable(Disposable subscription) {
        //如果解绑了的话添加需要新的实例否则绑定时无效的
        if (mCompositeDisposable == null || mCompositeDisposable.isDisposed()) {
            mCompositeDisposable = new CompositeDisposable();
        }
        mCompositeDisposable.add(subscription);
    }

    /**
     * 在界面退出等需要解绑观察者的情况下调用此方法统一解绑，防止Rx造成的内存泄漏
     */
    protected void unDisposable() {
        if (mCompositeDisposable != null) {
            mCompositeDisposable.dispose();
        }
    }

    @Override
    public void attachView(T view) {
        this.mView = view;
    }

    @Override
    public void detachView() {
        unDisposable();
        this.mView = null;
    }
}
