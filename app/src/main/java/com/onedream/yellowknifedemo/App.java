package com.onedream.yellowknifedemo;

import android.app.Application;

import com.onedream.yellowknife_api.XRouter;

public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        //
        XRouter.init(this);
    }
}
