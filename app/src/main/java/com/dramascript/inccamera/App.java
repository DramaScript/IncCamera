package com.dramascript.inccamera;

import android.app.Application;

import com.dramascript.dlibrary.LibInit;

public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        LibInit.init(this);
    }
}
