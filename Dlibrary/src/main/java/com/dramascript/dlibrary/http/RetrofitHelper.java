package com.dramascript.dlibrary.http;



import com.blankj.utilcode.util.LogUtils;
import com.dramascript.dlibrary.ApiConstant;
import com.dramascript.dlibrary.BuildConfig;
import com.dramascript.dlibrary.http.download.ProgressResponseBody;
import com.dramascript.dlibrary.http.upload.ProgressObserver;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

/*
 * Cread By DramaScript on 2019/3/19
 */
public class RetrofitHelper {
    private static RetrofitHelper retrofitHelper;
    private Retrofit retrofit;
    private OkHttpClient okHttpClient;
    private static final int DEFAULT_TIME_OUT = 10;
    private boolean addQueryParameter;
    private boolean addHeader;
    private boolean addDownloadInterceptor;
    private ProgressObserver observer;

    private RetrofitHelper() {
        OkHttpClient.Builder builder = new OkHttpClient.Builder()
                .connectTimeout(DEFAULT_TIME_OUT, TimeUnit.SECONDS)
                .writeTimeout(DEFAULT_TIME_OUT, TimeUnit.SECONDS)
                .readTimeout(DEFAULT_TIME_OUT, TimeUnit.SECONDS);

        if (BuildConfig.DEBUG) {//如果当前是debug模式就开启日志过滤器
            HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
            loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
            builder.addInterceptor(loggingInterceptor);
        }

        builder.addInterceptor(new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                if (!addQueryParameter) {
                    addQueryParameter = false;
                    return chain.proceed(chain.request());
                }
                Request oldRequest = chain.request();
                // 添加新的参数,公共参数
                HttpUrl.Builder authorizedUrlBuilder = oldRequest.url()
                        .newBuilder()
                        .scheme(oldRequest.url().scheme())
                        .host(oldRequest.url().host())
                        .addQueryParameter("time", "12:20")
                        .addQueryParameter("type", "android");

                // 新的请求
                Request newRequest = oldRequest.newBuilder()
                        .method(oldRequest.method(), oldRequest.body())
                        .url(authorizedUrlBuilder.build())
                        .build();

                //打印添加新的参数后的完整url
                LogUtils.d(newRequest.url().toString());
                addQueryParameter = false;

                return chain.proceed(newRequest);
            }
        });

        builder.addInterceptor(new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                if (!addHeader) {
                    addHeader = false;
                    return chain.proceed(chain.request());
                }
                //添加head
                Request originalRequest = chain.request();
                Request.Builder builder = originalRequest.newBuilder();
                //设置具体的header内容
//                builder.header("Accept-Language", "zh-CN");
//                builder.header("User-Agent", "");

                Request.Builder requestBuilder =
                        builder.method(originalRequest.method(), originalRequest.body());
                Request request = requestBuilder.build();
                addHeader = false;
                return chain.proceed(request);
            }
        });

        builder.addInterceptor(new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                if (!addDownloadInterceptor) {
                    addDownloadInterceptor = false;
                    return chain.proceed(chain.request());
                }
                addDownloadInterceptor = false;
                Response originalResponse = chain.proceed(chain.request());
                return originalResponse.newBuilder()
                        .body(new ProgressResponseBody(originalResponse.body(), observer))
                        .build();
            }
        });

        okHttpClient = builder.build();
    }

    public static RetrofitHelper getInstance() {
        if (retrofitHelper == null) {
            synchronized (HttpManager.class) {
                if (retrofitHelper == null)
                    retrofitHelper = new RetrofitHelper();
            }
        }
        return retrofitHelper;
    }

    public BaseService getService() {
        return getRetrofit().create(BaseService.class);
    }

    public Retrofit getRetrofit() {
        if (retrofit == null) {
            synchronized (Retrofit.class) {
                if (retrofit == null) {
                    retrofit = new Retrofit.Builder()
                            .baseUrl(ApiConstant.BASE_URL)
                            .client(okHttpClient)
                            .addConverterFactory(GsonConverterFactory.create())
                            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                            .build();
                }
            }
        }
        return retrofit;
    }

    public RetrofitHelper addQueryParameter() {
        addQueryParameter = true;
        return retrofitHelper;
    }

    public RetrofitHelper addHeader() {
        addHeader = true;
        return retrofitHelper;
    }

    public RetrofitHelper addDownloadInterceptor(ProgressObserver observer) {
        if (observer != null) {
            addDownloadInterceptor = true;
            this.observer = observer;
        }
        return retrofitHelper;
    }
}
