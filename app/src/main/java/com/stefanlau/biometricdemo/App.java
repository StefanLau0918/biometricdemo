package com.stefanlau.biometricdemo;

import android.app.Application;
import android.content.Context;

/**
 * Created by Stefan Lau on 2018/12/11.
 */
public class App extends Application {

    private static Context context;
    private static App instance;

    @Override
    public void onCreate() {
        super.onCreate();

        context = getApplicationContext();

        if (instance == null) {
            instance = this;
        }

    }

    public static Context getContext() {
        return context;
    }

    public static App getInstance() {
        return instance;
    }
}
