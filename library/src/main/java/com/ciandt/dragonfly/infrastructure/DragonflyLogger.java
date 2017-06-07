package com.ciandt.dragonfly.infrastructure;

import com.ciandt.dragonfly.BuildConfig;

import android.annotation.SuppressLint;
import android.os.Environment;
import android.support.annotation.IntDef;
import android.text.TextUtils;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.lang.annotation.Retention;
import java.nio.charset.StandardCharsets;

import static java.lang.annotation.RetentionPolicy.SOURCE;

/**
 * Created by iluz on 5/28/17.
 */
@SuppressWarnings({"ConstantConditions", "unused"})
public class DragonflyLogger {

    private static final String LOGGER_CLASS_NAME = DragonflyLogger.class.getSimpleName();

    private static final String DRAGONFLY_LOGGER_FILE = "DragonflyLogger.txt";

    @Retention(SOURCE)
    @IntDef({LOG_LEVEL_ERROR, LOG_LEVEL_WARN, LOG_LEVEL_INFO, LOG_LEVEL_DEBUG})
    public @interface LogLevel {

    }

    public static final int LOG_LEVEL_ERROR = 1;
    public static final int LOG_LEVEL_WARN = 2;
    public static final int LOG_LEVEL_INFO = 3;
    public static final int LOG_LEVEL_DEBUG = 4;

    @LogLevel
    private static int logLevel = LOG_LEVEL_ERROR;

    private DragonflyLogger() {
        // just to disable default constructor
    }

    public static void setLogLevel(@LogLevel int logLevel) {
        DragonflyLogger.logLevel = logLevel;
    }

    public static void debug(String msg) {

        debug(getTag(), msg);
    }

    public static void debug(String tag, String msg) {

        if (shouldLog(LOG_LEVEL_DEBUG) && !TextUtils.isEmpty(msg)) {
            Log.d(tag, msg);
        }
    }

    public static void info(String msg) {

        info(getTag(), msg);
    }

    public static void info(String tag, String msg) {

        if (shouldLog(LOG_LEVEL_INFO) && !TextUtils.isEmpty(msg)) {
            Log.i(tag, msg);
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
            Log.w(tag, msg, ex);
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
            Log.e(tag, msg, ex);
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

        File log = new File(Environment.getExternalStorageDirectory(), DRAGONFLY_LOGGER_FILE);
        BufferedWriter out = null;

        try {
            out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(log.getAbsolutePath(), log.exists()), StandardCharsets.UTF_8));
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
        return BuildConfig.DEBUG || DragonflyLogger.logLevel >= logLevel;
    }

    @SuppressLint("DefaultLocale")
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
