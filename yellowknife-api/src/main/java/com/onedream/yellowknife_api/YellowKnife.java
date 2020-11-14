package com.onedream.yellowknife_api;

import android.app.Activity;

import com.onedream.yellowknife_annotation.UnBinder;

import java.lang.reflect.Method;

/**
 * @author jdallen
 * @since 2020/9/2
 */
public class YellowKnife {

    public static UnBinder bind(Activity activity) {
        try {
            Class<?> activityClass = activity.getClass();
            Class<?> viewBindingClass = Class.forName(activityClass.getName() + "_ViewBinding");
            Method bindMethod = viewBindingClass.getMethod("bind", activityClass);
            UnBinder unBinder = (UnBinder) viewBindingClass.newInstance();
            bindMethod.invoke(unBinder, activity);
            return unBinder;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}

