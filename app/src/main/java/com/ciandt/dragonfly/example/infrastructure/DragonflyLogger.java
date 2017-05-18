package com.ciandt.dragonfly.example.infrastructure;

import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

import com.ciandt.dragonfly.example.BuildConfig;
import com.crashlytics.android.Crashlytics;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Created by iluz on 5/4/16.
 */
@SuppressWarnings({"PointlessBooleanExpression", "ConstantConditions", "unused"})
public class DragonflyLogger {

    private static final String LOGGER_CLASS_NAME = DragonflyLogger.class.getSimpleName();

    private static final String DRAGONFLY_LOG_FILE = "DragonflyLoggerLogFile.txt";

    // ATTENTION: these values must be in match with the values used in build.gradle for log level definitions
    private static final int LOG_LEVEL_ERROR = 1;
    private static final int LOG_LEVEL_WARN = 2;
    private static final int LOG_LEVEL_INFO = 3;
    private static final int LOG_LEVEL_DEBUG = 4;

    private DragonflyLogger() {
        // just to disable default constructor
    }

    public static void debug(String msg) {

        debug(getTag(), msg);
    }

    public static void debug(String tag, String msg) {

        if (shouldLog(LOG_LEVEL_DEBUG) && !TextUtils.isEmpty(msg)) {
            if (!BuildConfig.DEBUG) {
                Crashlytics.log(LOG_LEVEL_DEBUG, tag, msg);
            } else {
                Log.d(tag, msg);
            }
        }
    }

    public static void info(String msg) {

        info(getTag(), msg);
    }

    public static void info(String tag, String msg) {

        if (shouldLog(LOG_LEVEL_INFO) && !TextUtils.isEmpty(msg)) {
            if (!BuildConfig.DEBUG) {
                Crashlytics.log(LOG_LEVEL_INFO, tag, msg);
            } else {
                Log.i(tag, msg);
            }
        }
    }

    public static void warn(String msg) {

        warn(getTag(), msg, null);
    }

    public static void warn(String tag, String msg) {
        warn(tag, msg, null);
    }

    public static void warn(String tag, String msg, Throwable ex) {

        if (shouldLog(LOG_LEVEL_WARN) && !TextUtils.isEmpty(msg)) {
            if (!BuildConfig.DEBUG) {
                Crashlytics.log(LOG_LEVEL_WARN, tag, msg);
            } else {
                Log.w(tag, msg, ex);
            }
        }
    }

    public static void error(String msg) {

        error(getTag(), msg, null);
    }

    public static void error(String tag, String msg) {

        error(tag, msg, null);
    }

    public static void error(Throwable ex) {

        error(getTag(), ex.getMessage(), ex);
    }

    public static void error(String tag, Throwable ex) {

        error(tag, ex.getMessage(), ex);
    }

    public static void error(String tag, String msg, Throwable ex) {

        if (shouldLog(LOG_LEVEL_ERROR) && !TextUtils.isEmpty(msg)) {
            if (!BuildConfig.DEBUG) {
                Crashlytics.log(LOG_LEVEL_ERROR, tag, msg);
            } else {
                Log.e(tag, msg, ex);
            }
        }

        if (ex != null && !BuildConfig.DEBUG) {
            Crashlytics.logException(ex);
        }
    }

    public static void exception(Throwable ex) {

        exception(getTag(), ex);
    }

    public static void exception(String tag, Throwable ex) {
        
        error(tag, ex);
    }

    /**
     * Just for debug purpose
     */
    public static void logToFile(String message) {

        File log = new File(Environment.getExternalStorageDirectory(), DRAGONFLY_LOG_FILE);
        BufferedWriter out = null;

        try {
            out = new BufferedWriter(new FileWriter(log.getAbsolutePath(), log.exists()));
            out.write(message);
            out.close();
        } catch (IOException e) {
            // silent
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    // silent
                }
            }
        }
    }

    private static boolean shouldLog(int logLevel) {
        return BuildConfig.DEBUG || BuildConfig.LOG_LEVEL >= logLevel;
    }

    private static String getTag() {

        StackTraceElement stackTraceElement = getCallerClass();

        try {
            return String.format("%s->%s(%d)", Class.forName(stackTraceElement.getClassName()).getSimpleName(), stackTraceElement.getMethodName(), stackTraceElement.getLineNumber());
        } catch (Throwable e) {
            return LOGGER_CLASS_NAME;
        }
    }

    private static StackTraceElement getCallerClass() {

        StackTraceElement[] stackTraceElements = getStackTrace();

        StackTraceElement lastStackTraceElement = null;

        for (int i = stackTraceElements.length - 1; i >= 0; i--) {
            StackTraceElement stackTraceElement = stackTraceElements[i];

            if (stackTraceElement != null && stackTraceElement.getClassName().contains(LOGGER_CLASS_NAME)) {
                break;
            }

            lastStackTraceElement = stackTraceElement;
        }

        return lastStackTraceElement;
    }

    private static StackTraceElement[] getStackTrace() {

        return Thread.currentThread().getStackTrace();
    }
}
