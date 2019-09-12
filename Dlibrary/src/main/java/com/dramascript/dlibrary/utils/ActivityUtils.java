package com.dramascript.dlibrary.utils;

import android.app.Activity;
import android.support.v4.util.ArrayMap;

import java.util.Map;

/*
 * Cread By DramaScript on 2019/3/5
 */
public class ActivityUtils {

    //保存应用内所有任务栈中的Activity
    private static Map<String, Activity> activityMap = new ArrayMap<>();

    public static void addActivity(String name, Activity activity) {
        activityMap.put(name, activity);
    }

    public static void removeActivity(String name) {
        activityMap.remove(name);
    }

    public static void finishActivity(String name) {
        Activity activity = activityMap.get(name);
        if (activity != null && !activity.isFinishing())
            activity.finish();
    }

    /**
     * 清空除了这个activity外的其他activity
     *
     * @param name
     */
    public static void finishActivityExcept(String name) {
        for (String value : activityMap.keySet()) {
            if (value.equals(name))
                break;
            else
                finishActivity(value);
        }
    }

    //退出应用
    public static void exitApp() {
        synchronized (activityMap) {
            for (Activity activity : activityMap.values()) {
                if (activity != null && !activity.isFinishing())
                    activity.finish();
            }
        }
        android.os.Process.killProcess(android.os.Process.myPid());
        System.exit(0);
    }

}
