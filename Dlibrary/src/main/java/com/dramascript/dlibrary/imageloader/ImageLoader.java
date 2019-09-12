package com.dramascript.dlibrary.imageloader;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.os.Looper;
import android.support.v4.app.Fragment;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.io.File;

import jp.wasabeef.glide.transformations.BlurTransformation;
import jp.wasabeef.glide.transformations.CropCircleTransformation;
import jp.wasabeef.glide.transformations.RoundedCornersTransformation;


/**
 * Created by xyp on 2018/3/9.
 */

public class ImageLoader {

    /**
     * 加载网络资源
     */

    //无占位图
    public static void load(Activity activity, String url, ImageView imageView) {
        Glide.with(activity)
                .load(url)
                .crossFade()
                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                .into(imageView);
    }

    public static void load(Fragment fragment, String url, ImageView imageView) {
        Glide.with(fragment)
                .load(url)
                .crossFade()
                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                .into(imageView);
    }

    public static void load(Context context, String url, ImageView imageView) {
        Glide.with(context)
                .load(url)
                .crossFade()
                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                .into(imageView);
    }

    //有占位图
    public static void load(Context context, String url, ImageView imageView, int placeholder, int error) {
        Glide.with(context)
                .load(url)
                .crossFade()
                .error(error)
                .placeholder(placeholder)
                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                .into(imageView);
    }

    public static void load(Activity activity, String url, ImageView imageView, int placeholder, int error) {
        Glide.with(activity)
                .load(url)
                .crossFade()
                .error(error)
                .placeholder(placeholder)
                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                .into(imageView);
    }

    public static void load(Fragment fragment, String url, ImageView imageView, int placeholder, int error) {
        Glide.with(fragment)
                .load(url)
                .crossFade()
                .error(error)
                .placeholder(placeholder)
                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                .into(imageView);
    }

    /**
     * 加载assets资源
     */

    public static void loadAssets(Context context, String assetsName, ImageView imageView) {
        Glide.with(context)
                .load("file:///" + assetsName)
                .into(imageView);
    }

    public static void loadAssets(Activity activity, String assetsName, ImageView imageView) {
        Glide.with(activity)
                .load("file:///" + assetsName)
                .into(imageView);
    }

    public static void loadAssets(Fragment fragment, String assetsName, ImageView imageView) {
        Glide.with(fragment)
                .load("file:///" + assetsName)
                .into(imageView);
    }

    /**
     * 加载File资源
     */

    public static void loadFile(Context context, File file, ImageView imageView) {
        Glide.with(context)
                .load(file)
                .into(imageView);
    }

    public static void loadFile(Activity activity, File file, ImageView imageView) {
        Glide.with(activity)
                .load(file)
                .into(imageView);
    }

    public static void loadFile(Fragment fragment, File file, ImageView imageView) {
        Glide.with(fragment)
                .load(file)
                .into(imageView);
    }

    /**
     * 加载Gif
     */

    public static void loadGif(Context context, String url, ImageView imageView) {
        Glide.with(context)
                .load(url)
                .asGif()
                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                .into(imageView);
    }

    public static void loadGif(Activity activity, String url, ImageView imageView) {
        Glide.with(activity)
                .load(url)
                .asGif()
                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                .into(imageView);
    }

    public static void loadGif(Fragment fragment, String url, ImageView imageView) {
        Glide.with(fragment)
                .load(url)
                .asGif()
                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                .into(imageView);
    }

    /**
     * 显示本地视频
     */

    public static void loadVedio(Context context, String filePath, ImageView imageView) {
        Glide.with(context)
                .load(Uri.fromFile(new File(filePath)))
                .into(imageView);
    }


    public static void loadVedio(Activity activity, String filePath, ImageView imageView) {
        Glide.with(activity)
                .load(Uri.fromFile(new File(filePath)))
                .into(imageView);
    }


    public static void loadVedio(Fragment fragment, String filePath, ImageView imageView) {
        Glide.with(fragment)
                .load(Uri.fromFile(new File(filePath)))
                .into(imageView);
    }

    /**
     * 设置缩略图，先显示缩略图再显示完整图片
     */

    public static void loadThumbnail(Context context, String url, ImageView imageView) {
        Glide.with(context)
                .load(url)
                .crossFade()
                .thumbnail(0.1f)
                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                .into(imageView);
    }

    public static void loadThumbnail(Activity activity, String url, ImageView imageView) {
        Glide.with(activity)
                .load(url)
                .crossFade()
                .thumbnail(0.1f)
                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                .into(imageView);
    }

    public static void loadThumbnail(Fragment fragment, String url, ImageView imageView) {
        Glide.with(fragment)
                .load(url)
                .crossFade()
                .thumbnail(0.1f)
                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                .into(imageView);
    }


    /**
     * 图片实现高斯模糊，毛玻璃
     * radius:1-25, 值越大越模糊
     */
    public static void loadBlur(Context context, String url, int radius, ImageView imageView) {
        Glide.with(context)
                .load(url)
                .bitmapTransform(new BlurTransformation(context, radius))
                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                .into(imageView);
    }

    public static void loadBlur(Activity activity, String url, int radius, ImageView imageView) {
        Glide.with(activity)
                .load(url)
                .bitmapTransform(new BlurTransformation(activity, radius))
                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                .into(imageView);
    }

    public static void loadBlur(Fragment fragment, String url, int radius, ImageView imageView) {
        Glide.with(fragment)
                .load(url)
                .bitmapTransform(new BlurTransformation(fragment.getContext(), radius))
                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                .into(imageView);
    }

    /**
     * 原图基础上设置圆形图
     */
    public static void loadCircle(Context context, String url, ImageView imageView) {
        Glide.with(context)
                .load(url)
                .bitmapTransform(new CropCircleTransformation(context))
                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                .into(imageView);
    }

    public static void loadCircle(Activity activity, String url, ImageView imageView) {
        Glide.with(activity)
                .load(url)
                .bitmapTransform(new CropCircleTransformation(activity))
                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                .into(imageView);
    }

    public static void loadCircle(Context context, String url, ImageView imageView, int error) {
        Glide.with(context)
                .load(url)
                .error(error)
                .placeholder(error)
                .bitmapTransform(new CropCircleTransformation(context))
                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                .into(imageView);
    }

    public static void loadCircle(Fragment fragment, String url, ImageView imageView) {
        Glide.with(fragment)
                .load(url)
                .bitmapTransform(new CropCircleTransformation(fragment.getContext()))
                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                .into(imageView);
    }

    /**
     * 设置圆角
     */
    public static void loadRound(Context context, String url, int radius, ImageView imageView) {
        Glide.with(context)
                .load(url)
                .bitmapTransform(new RoundedCornersTransformation(context, radius, 0, RoundedCornersTransformation.CornerType.ALL))
                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                .into(imageView);
    }

    public static void loadRound(Activity activity, String url, int radius, ImageView imageView) {
        Glide.with(activity)
                .load(url)
                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                .bitmapTransform(new RoundedCornersTransformation(activity, radius, 0, RoundedCornersTransformation.CornerType.ALL))
                .into(imageView);
    }

    public static void loadRound(Fragment fragment, String url, int radius, ImageView imageView) {
        Glide.with(fragment)
                .load(url)
                .bitmapTransform(new RoundedCornersTransformation(fragment.getContext(), radius, 0, RoundedCornersTransformation.CornerType.ALL))
                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                .into(imageView);
    }


    //清理内存缓存 可以在主线程
    public static void clearMemory(Context context) {
        try {
            if (Looper.myLooper() == Looper.getMainLooper()) {
                Glide.get(context).clearMemory();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //清理磁盘缓存 需要在子线程
    public static void clearDiskCache(final Context context) {
        try {
            if (Looper.myLooper() == Looper.getMainLooper()) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Glide.get(context).clearDiskCache();
                    }
                }).start();
            } else {
                Glide.get(context).clearDiskCache();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
