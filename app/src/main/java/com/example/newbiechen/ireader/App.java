package com.example.newbiechen.ireader;

import android.app.Application;
import android.content.Context;
import android.content.Intent;

import com.example.newbiechen.ireader.service.DownloadService;
import com.squareup.leakcanary.LeakCanary;
import com.tencent.bugly.crashreport.CrashReport;

import cn.jpush.android.api.JPushInterface;

/**
 * Created by newbiechen on 17-4-15.
 */

public class App extends Application {
    private static Context sInstance;

    @Override
    public void onCreate() {
        super.onCreate();
        sInstance = this;
        startService(new Intent(getContext(), DownloadService.class));
        JPushInterface.setDebugMode(true);
        JPushInterface.init(this);
        CrashReport.initCrashReport(getApplicationContext(), "c1022e389b", false);
        // 初始化内存分析工具
//        if (!LeakCanary.isInAnalyzerProcess(this)) {
//            LeakCanary.install(this);
//        }
    }

    public static Context getContext(){
        return sInstance;
    }
}