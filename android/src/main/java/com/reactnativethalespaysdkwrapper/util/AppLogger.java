package com.reactnativethalespaysdkwrapper.util;

import android.util.Log;

import com.reactnativethalespaysdkwrapper.app.AppBuildConfigurations;


public final class AppLogger {
    public static void i(final String tag, final String message) {
        if (AppBuildConfigurations.IS_LOG_ENABLED) {
            Log.i(tag, message);
        }
    }

    public static void d(final String tag, final String message) {
        if (AppBuildConfigurations.IS_LOG_ENABLED) {
            Log.d(tag, message);
        }
    }

    public static void w(final String tag, final String message) {
        if (AppBuildConfigurations.IS_LOG_ENABLED) {
            Log.w(tag, message);
        }
    }

    public static void e(final String tag, final String message) {
        if (AppBuildConfigurations.IS_LOG_ENABLED) {
            Log.e(tag, message);
        }
    }
    public static void e(final String tag, final String message, final Exception e) {
        if (AppBuildConfigurations.IS_LOG_ENABLED) {
            Log.e(tag, message,e);
        }
    }
}
