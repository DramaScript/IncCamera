package com.dramascript.dlibrary.http;


import com.blankj.utilcode.util.LogUtils;
import com.dramascript.dlibrary.Config;
import com.dramascript.dlibrary.http.upload.ProgressObserver;
import com.dramascript.dlibrary.http.upload.ProgressRequestBody;
import com.dramascript.dlibrary.security.AesEncrypt;
import com.dramascript.dlibrary.utils.FileUtil;
import com.google.gson.Gson;

import java.io.File;
import java.util.Map;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.ObservableTransformer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;

/*
 * Cread By DramaScript on 2019/3/19
 */
public class HttpManager {
    private static HttpManager httpManager;

    private HttpManager() {
    }

    public static HttpManager getInstance() {
        if (httpManager == null) {
            synchronized (HttpManager.class) {
                if (httpManager == null)
                    httpManager = new HttpManager();
            }
        }
        return httpManager;
    }


    /**
     * 转换器，例如将发射为integer的发射器转换成String的发射器，有点类似glider中的图形变换transform
     * @param <T>
     * @return
     */
    public <T> ObservableTransformer<T, T> schedulersTransformer() {
        return new ObservableTransformer<T, T>() {
            @Override
            public ObservableSource<T> apply(Observable<T> upstream) {
                return upstream.subscribeOn(Schedulers.io())
                        .unsubscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread());
            }
        };
    }

    /**
     * 同步，没有立刻执行，需要到doNext之后才执行的get请求，返回的数据需要自己json转换为实体类
     * 详细参考：https://www.jianshu.com/p/e9e03194199e
     * @param url
     * @param parameters
     * @return
     */
    public Observable<ResponseBody> get(String url, Map<String, String> parameters) {
        return RetrofitHelper.getInstance().getService()
                .executeGet(url, parameters);
    }


    public Observable<ResponseBody> get(String url) {
        return RetrofitHelper.getInstance().getService()
                .executeGet(url);
    }

    /**
     * 异步，调用了compose，会立刻执行。
     * @param url
     * @return
     */
    public Observable<ResponseBody> executeGet(String url) {
        return RetrofitHelper.getInstance().getService()
                .executeGet(url)
                .compose(this.<ResponseBody>schedulersTransformer());
    }

    public Observable<ResponseBody> executeGet(String url, Map<String, String> parameters) {
        return RetrofitHelper.getInstance().getService()
                .executeGet(url, parameters)
                .compose(this.<ResponseBody>schedulersTransformer());
    }

    public Observable<ResponseBody> executePost(String url) {
        return RetrofitHelper.getInstance().getService()
                .executePost(url)
                .compose(this.<ResponseBody>schedulersTransformer());
    }

    public Observable<ResponseBody> executePost(String url, Map<String, String> parameters) {
        return RetrofitHelper.getInstance().getService()
                .executePost(url, parameters)
                .compose(this.<ResponseBody>schedulersTransformer());
    }

    public Observable<ResponseBody> executePostJson(String url, String json) {
        RequestBody body=RequestBody.create(MediaType.parse("application/json; charset=utf-8"),json);
        return RetrofitHelper.getInstance().getService()
                .executePost(url, body)
                .compose(this.<ResponseBody>schedulersTransformer());
    }

    /**
     * AES加密的post
     *
     * @param url
     * @param parameters
     * @return
     */
    public Observable<ResponseBody> executeAesPost(String url, Map<String, String> parameters,String aesKey) {
        String temp = new Gson().toJson(parameters);
        LogUtils.d("接口json", "url:" + url + "-----" + temp);
        byte[] bytes = AesEncrypt.getInstance().encrypt(aesKey.getBytes(), temp);
        RequestBody body = FormBody.create(MediaType.parse("application/x-www-form-urlencoded"), bytes);
        return RetrofitHelper.getInstance().getService()
                .executePost(url, body)
                .compose(this.<ResponseBody>schedulersTransformer());
    }

    //同步
    public Observable<ResponseBody> aesPost(String url, Map<String, String> parameters) {
        String temp = new Gson().toJson(parameters);
        LogUtils.d("接口json", "url:" + url + "-----" + temp);
        byte[] bytes = AesEncrypt.getInstance().encrypt(Config.AES_KEY.getBytes(), temp);
        RequestBody body = FormBody.create(MediaType.parse("application/x-www-form-urlencoded"), bytes);
        return RetrofitHelper.getInstance().getService()
                .executePost(url, body);
    }

    public void upLoadFile(String url, Map<String, String> parameters, File file, ProgressObserver observer) {
        // 创建 RequestBody，用于封装构建RequestBody
        RequestBody requestFile =
                RequestBody.create(MediaType.parse("multipart/form-data"), file);

        // MultipartBody.Part  和后端约定好Key，这里的partName是用image
        MultipartBody.Part body =
                MultipartBody.Part.createFormData("image", file.getName()
                        , new ProgressRequestBody(requestFile, observer));

        // 添加描述
        String descriptionString = "hello, 这是文件描述";
        RequestBody description =
                RequestBody.create(
                        MediaType.parse("multipart/form-data"), descriptionString);
        RetrofitHelper.getInstance().getService()
                .upLoadFile(url, description, parameters, body)
                .compose(this.<ResponseBody>schedulersTransformer())
                .subscribe(observer);
    }

    public void downLoadFile(String url, final String savePath, final String fileName, ProgressObserver observer) {
        RetrofitHelper.getInstance()
                .addDownloadInterceptor(observer)
                .getService()
                .downloadFile(url)
                .subscribeOn(Schedulers.io())//请求在io线程
                .observeOn(Schedulers.io())//保存文件在io线程
                .doOnNext(new Consumer<ResponseBody>() {
                    @Override
                    public void accept(ResponseBody responseBody) throws Exception {
                        FileUtil.saveFile(responseBody.byteStream(), savePath, fileName);
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(observer);
    }

    //在拦截器里添加公共参数
    public HttpManager addQueryParameter() {
        RetrofitHelper.getInstance()
                .addQueryParameter();
        return httpManager;
    }

    //在拦截器里添加头
    public HttpManager addHeader() {
        RetrofitHelper.getInstance()
                .addHeader();
        return httpManager;
    }

    //添加公共参数,直接在Map里添加，不使用拦截器
    public HttpManager addCommonParams(Map<String, String> params) {
        return httpManager;
    }
}
