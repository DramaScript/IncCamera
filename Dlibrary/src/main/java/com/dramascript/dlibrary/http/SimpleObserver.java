package com.dramascript.dlibrary.http;

import android.text.TextUtils;


import com.blankj.utilcode.util.LogUtils;
import com.dramascript.dlibrary.Config;
import com.dramascript.dlibrary.utils.GsonUtil;
import com.google.gson.JsonSyntaxException;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.List;

import javax.net.ssl.SSLHandshakeException;

import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import okhttp3.ResponseBody;

/*
 * Cread By DramaScript on 2019/3/19
 */
public abstract class SimpleObserver<T> implements Observer<ResponseBody> {
    private Class<T> tClass;
    private Class<T[]> tClassList;

    //可以处理两种情况：解析jsonObject和解析jsonArray,两个参数必须要有一个为null
    public SimpleObserver(Class<T> tClass, Class<T[]> tClassList) {
        this.tClass = tClass;
        this.tClassList = tClassList;
    }

    @Override
    public void onSubscribe(Disposable d) {
        addDisposable(d);
    }

    @Override
    public void onNext(ResponseBody responseBody) {
        String res = "";
        try {
            res = responseBody.string();
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (TextUtils.isEmpty(res)) {
            onError("请求失败");
            return;
        }
        try {
            if (tClass!=null&&tClass.equals(String.class)) {
                onSuccess(res);
            } else {
                BaseResponse baseResponse = GsonUtil.getObject(res, BaseResponse.class);
                LogUtils.d("json:" + res + "----data---json:" + baseResponse.getData());
                //如果code跟服务器定下的成功返回不一致
                if (!Config.CODE_SUCCESS.equals(baseResponse.getCode())) {
                    if (Config.TOKEN_ERROR.equals(baseResponse.getCode()))
                        onError(Config.TOKEN_ERROR);
                    onError(baseResponse.getMsg());
                    return;
                }
                if (tClass != null) {
                    T t = GsonUtil.getObject(baseResponse.getData(), tClass);
                    onSuccess(baseResponse.getMsg(), t);
                } else {
                    List<T> list = GsonUtil.getObjects(baseResponse.getData(), tClassList);
                    onSuccess(baseResponse.getMsg(), list);
                }
                onSuccess(baseResponse.getData().toString());
            }
        } catch (Exception e) {
            onError("请求失败");
        }
    }

    @Override
    public void onError(Throwable e) {
        if (e instanceof UnknownHostException) {
            onError("网络错误");
        } else if (e instanceof ConnectException) {
            onError("无网络连接");
        } else if (e instanceof SocketTimeoutException) {
            onError("网络请求超时");
        } else if (e instanceof SSLHandshakeException) {
            onError("当前网络未进行认证,暂不可用");
        } else {
            onError("未知错误");
            LogUtils.d(e.toString());
        }
    }

    @Override
    public void onComplete() {
    }


    public void onSuccess(String msg, T t) {
    }

    public void onSuccess(String msg, List<T> t) {
    }

    public void onSuccess(String json) {

    }

    public abstract void onError(String msg);

    public abstract void addDisposable(Disposable d);
}
