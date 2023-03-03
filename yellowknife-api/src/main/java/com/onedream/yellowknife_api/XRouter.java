package com.onedream.yellowknife_api;

import android.app.Application;

public class XRouter {
    private static Application mApplication;

    public static void init(Application application){
        mApplication = application;
    }

    public static Application getApplication() {
        return mApplication;
    }

    public static XRouterParams build(String path) {
        XRouterParams xRouterParams = new XRouterParams();
        xRouterParams.setPath(path);
        return xRouterParams;
    }
}
