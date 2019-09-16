package com.dramascript.inccamera.mvp.presenter.contract;

import com.dramascript.dlibrary.base.BasePresenter;
import com.dramascript.dlibrary.base.BaseView;
import com.dramascript.inccamera.mvp.model.ImageCache;

import java.util.List;

public interface CacheContract {

    public interface View extends BaseView{
        void cahceResult(List<ImageCache> list);
    }

    public interface Presenter extends BasePresenter<View>{
        void getCache();
    }
}
