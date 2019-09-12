package com.dramascript.dlibrary.imageloader;

import android.content.Context;

import com.bumptech.glide.Glide;
import com.bumptech.glide.GlideBuilder;
import com.bumptech.glide.load.engine.bitmap_recycle.LruBitmapPool;
import com.bumptech.glide.load.engine.cache.ExternalCacheDiskCacheFactory;
import com.bumptech.glide.load.engine.cache.LruResourceCache;
import com.bumptech.glide.module.GlideModule;

/**
 * Created by xyp on 2018/3/11.
 * 设置图片格式（默认是RGB-565),以及缓存目录
 */

public class GlideModuleConfig implements GlideModule {
    @Override
    public void applyOptions(Context context, GlideBuilder builder) {
        //内部存储/Android/data/包名/cache/glide-images
        builder.setDiskCache(new ExternalCacheDiskCacheFactory(context, "glide-images", 20 * 1024 * 1024));
        //获取系统分配给应用的最大内存
        int maxMemory = (int) Runtime.getRuntime().maxMemory();
        int memoryCacheSize = maxMemory / 8;//设置图片内存缓存大小为八分之一
        builder.setMemoryCache(new LruResourceCache(memoryCacheSize));
        //设置BitmapPool缓存内存大小
        builder.setBitmapPool(new LruBitmapPool(memoryCacheSize));


        //将默认的RGB_565效果转换到ARGB_8888
        //默认格式RGB_565使用内存是ARGB_8888的一半，但是图片质量就没那么高了，而且不支持透明度
        //builder.setDecodeFormat(DecodeFormat.PREFER_ARGB_8888);
    }

    @Override
    public void registerComponents(Context context, Glide glide) {

    }
}
