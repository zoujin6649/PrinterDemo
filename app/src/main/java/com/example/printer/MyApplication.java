package com.example.printer;

import android.app.Application;

import com.example.lib_printer.PrinterSDK;

/**
 * Created by claire on 2020/9/4.
 */
public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        SystemTTS.getInstance(this);

        PrinterSDK.init(this);

    }
}
