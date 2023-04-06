package com.onedream.yellowknife_api;

import android.app.Application;
import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class XRouterParams {
    private String path;
    private final Map<String, String> withStringMap = new HashMap<>();
    private final Map<String, Integer> withIntMap =new HashMap<>();
    private final Map<String, Boolean> withBooleanMap =new HashMap<>();

    public void setPath(String path) {
        this.path = path;
    }

    public XRouterParams withString(@NonNull String key, String value) {
        withStringMap.put(key, value);
        return this;
    }

    public XRouterParams withInt(@NonNull String key, Integer value) {
        withIntMap.put(key, value);
        return this;
    }

    public XRouterParams withBoolean(@NonNull String key, Boolean value) {
        withBooleanMap.put(key, value);
        return this;
    }

    public void navigation() {
        navigation(XRouter.getApplication());
    }

    public void navigation(Context context) {
        try {
            Class<?> XRouterMapClass = Class.forName("com.onedream.XRouterMap");
            Method bindMethod = XRouterMapClass.getMethod("get", String.class);
            Class clazz = (Class) bindMethod.invoke(XRouterMapClass.newInstance(), path);
            Intent intent = new Intent(context, clazz);
            //
            Iterator<Map.Entry<String, String>> withStringMApIterator = withStringMap.entrySet().iterator();
            while (withStringMApIterator.hasNext()) {
                Map.Entry<String, String> map = withStringMApIterator.next();
                intent.putExtra(map.getKey(), map.getValue());
            }
            //
            Iterator<Map.Entry<String, Integer>> withIntMapIterator = withIntMap.entrySet().iterator();
            while (withIntMapIterator.hasNext()) {
                Map.Entry<String, Integer> map = withIntMapIterator.next();
                intent.putExtra(map.getKey(), map.getValue());
            }
            //
            Iterator<Map.Entry<String, Boolean>> withBooleanMapIterator = withBooleanMap.entrySet().iterator();
            while (withBooleanMapIterator.hasNext()) {
                Map.Entry<String, Boolean> map = withBooleanMapIterator.next();
                intent.putExtra(map.getKey(), map.getValue());
            }
            if(context instanceof Application){
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            }
            //
            context.startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
