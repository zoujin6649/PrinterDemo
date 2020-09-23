package com.example.lib_printer;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by Administrator
 *
 * @author 猿史森林
 *         Date: 2017/10/23
 *         Class description:
 */
public class SharedPreferencesUtil {
    private static final String NAME = "Configs";
    private static SharedPreferences sharedPreferences;
    private static SharedPreferencesUtil sharedPreferencesUtil;

    private SharedPreferencesUtil(){

    }

    public static SharedPreferencesUtil getInstantiation(Context context) {
        if (sharedPreferences == null) {
            sharedPreferencesUtil = new SharedPreferencesUtil();
            getSharedPreferences(context);
        }
        return sharedPreferencesUtil;
    }

    private static void getSharedPreferences(Context context) {
        sharedPreferences = context.getSharedPreferences(NAME, Context.MODE_PRIVATE);
    }

    public void putInt(int value, String key) {
        sharedPreferences.edit().putInt(key, value).apply();
    }

    public void putString(String value, String key) {
        sharedPreferences.edit().putString(key, value).apply();
    }

    public void putBoolean(boolean value, String key) {
        sharedPreferences.edit().putBoolean(key, value).apply();
    }

    public void putFloat(float value, String key) {
        sharedPreferences.edit().putFloat(key, value).apply();
    }

    public void putLong(long value, String key) {
        sharedPreferences.edit().putLong(key, value).apply();
    }

    public int getInt(int defaultValue, String key) {
        return sharedPreferences.getInt(key, defaultValue);
    }

    public String getString(String defaultValue, String key) {
        return sharedPreferences.getString(key, defaultValue);
    }

    public boolean getBoolean(boolean defaultValue, String key) {
        return sharedPreferences.getBoolean(key, defaultValue);
    }

    public float getFloat(float defaultValue, String key) {
        return sharedPreferences.getFloat(key, defaultValue);
    }

    public void clear() {
        sharedPreferences.edit().clear().apply();
    }
}
