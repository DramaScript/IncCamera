package com.dramascript.dlibrary.http;

import java.util.Map;

import io.reactivex.Observable;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.PartMap;
import retrofit2.http.QueryMap;
import retrofit2.http.Streaming;
import retrofit2.http.Url;

/*
 * Cread By DramaScript on 2019/3/19
 */
public interface BaseService {

    @GET
    Observable<ResponseBody> executeGet(@Url String url);


    @GET
    Observable<ResponseBody> executeGet(
            @Url String url,
            @QueryMap Map<String, String> maps);


    @POST
    Observable<ResponseBody> executePost(@Url String url);

    @POST
    Observable<ResponseBody> executePost(
            @Url String url,
            @QueryMap Map<String, String> maps);

    @POST
    Observable<ResponseBody> executePost(
            @Url String url,
            @Body RequestBody body);

    @Headers({"Content-Type: application/json","Accept: application/json"})
    @POST
    Observable<ResponseBody> executePostJson(
            @Url String url,
            @Body RequestBody body);


    @Multipart
    @POST
    Observable<ResponseBody> upLoadFile(
            @Url String url,
            @Part("description") RequestBody description,
            @PartMap Map<String, String> map,
            @Part MultipartBody.Part file);


/*    @POST
    Observable<ResponseBody> uploadFiles(
            @Url String url,
            //@Path("headers") Map<String, String> headers,
            @Part("description") String description,
            @PartMap() Map<String, MultipartBody.Part> maps);*/


    @Streaming
    @GET
    Observable<ResponseBody> downloadFile(@Url String fileUrl);
}
