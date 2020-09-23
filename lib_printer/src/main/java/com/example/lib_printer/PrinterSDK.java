package com.example.lib_printer;

import android.app.Application;

/**
 * Created by claire on 2020/9/16.
 */
public class PrinterSDK {

    private static Application instance;

    public static Application getApplication() {
        return instance;
    }

    public static void init(Application application) {
        instance = application;
    }
}
