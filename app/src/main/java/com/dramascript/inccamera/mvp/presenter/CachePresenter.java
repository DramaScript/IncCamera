package com.dramascript.inccamera.mvp.presenter;

import android.annotation.SuppressLint;

import com.dramascript.dlibrary.base.RxPresenter;
import com.dramascript.inccamera.mvp.model.DataBase;
import com.dramascript.inccamera.mvp.model.ImageCache;
import com.dramascript.inccamera.mvp.presenter.contract.CacheContract;

import java.util.List;

import io.reactivex.functions.Consumer;

public class CachePresenter extends RxPresenter<CacheContract.View> implements CacheContract.Presenter {

    @SuppressLint("CheckResult")
    @Override
    public void getCache() {
        DataBase.getDataBase().getAllCacheList().subscribe(new Consumer<List<ImageCache>>() {
            @Override
            public void accept(List<ImageCache> list) throws Exception {
                if (list!=null){
                    mView.cahceResult(list);
                }
            }
        });
    }

}
