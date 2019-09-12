package com.dramascript.dlibrary.base;

/*
 * Cread By DramaScript on 2019/3/5
 */
public interface BasePresenter<T extends BaseView> {
    void attachView(T view);

    void detachView();
}
