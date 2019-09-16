package com.dramascript.inccamera;

import android.app.Application;
import android.database.sqlite.SQLiteDatabase;

import com.bumptech.glide.Glide;
import com.dramascript.dlibrary.LibInit;
import com.dramascript.inccamera.mvp.model.MySQLiteOpenHelper;
import com.dramascript.inccamera.mvp.model.gen.DaoMaster;
import com.dramascript.inccamera.mvp.model.gen.DaoSession;

public class App extends Application {

    private static App instance;

    public static App getInstance() {
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        LibInit.init(this);
    }

    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);
        // 程序在内存清理的时候执行
        if (level == TRIM_MEMORY_UI_HIDDEN)
            Glide.get(this).clearMemory();
        Glide.get(this).trimMemory(level);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        // 低内存的时候执行
        Glide.get(this).clearMemory();
    }

}
