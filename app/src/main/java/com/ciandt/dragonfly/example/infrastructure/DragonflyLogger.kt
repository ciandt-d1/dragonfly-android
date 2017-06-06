package com.ciandt.dragonfly.example.infrastructure

import android.os.Environment
import android.text.TextUtils
import android.util.Log
import com.ciandt.dragonfly.example.BuildConfig
import com.crashlytics.android.Crashlytics
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.util.Locale

/**
 * Created by iluz on 5/4/16.
 */
object DragonflyLogger {

    private val LOGGER_CLASS_NAME = DragonflyLogger::class.java.simpleName

    private val DRAGONFLY_LOG_FILE = "DragonflyLoggerLogFile.txt"

    // ATTENTION: these values must be in match with the values used in build.gradle for log level definitions
    private val LOG_LEVEL_ERROR = 1
    private val LOG_LEVEL_WARN = 2
    private val LOG_LEVEL_INFO = 3
    private val LOG_LEVEL_DEBUG = 4

    fun debug(tag: String = this.tag, msg: String) {
        if (shouldLog(LOG_LEVEL_DEBUG) && !TextUtils.isEmpty(msg)) {
            if (!BuildConfig.DEBUG) {
                Crashlytics.log(LOG_LEVEL_DEBUG, tag, msg)
            } else {
                Log.d(tag, msg)
            }
        }
    }

    fun info(tag: String = this.tag, msg: String) {
        if (shouldLog(LOG_LEVEL_INFO) && !TextUtils.isEmpty(msg)) {
            if (!BuildConfig.DEBUG) {
                Crashlytics.log(LOG_LEVEL_INFO, tag, msg)
            } else {
                Log.i(tag, msg)
            }
        }
    }

    fun warn(tag: String = this.tag, msg: String, ex: Throwable? = null) {
        if (shouldLog(LOG_LEVEL_WARN) && !TextUtils.isEmpty(msg)) {
            if (!BuildConfig.DEBUG) {
                Crashlytics.log(LOG_LEVEL_WARN, tag, msg)
            } else {
                Log.w(tag, msg, ex)
            }
        }
    }

    fun error(tag: String = this.tag, ex: Throwable) {
        error(tag, ex.message, ex)
    }

    fun error(tag: String = this.tag, msg: String?, ex: Throwable? = null) {
        if (shouldLog(LOG_LEVEL_ERROR) && !TextUtils.isEmpty(msg)) {
            if (!BuildConfig.DEBUG) {
                Crashlytics.log(LOG_LEVEL_ERROR, tag, msg)
            } else {
                Log.e(tag, msg, ex)
            }
        }

        if (ex != null && !BuildConfig.DEBUG) {
            Crashlytics.logException(ex)
        }
    }

    fun exception(tag: String = this.tag, ex: Throwable) {
        error(tag, ex)
    }

    /**
     * Just for debug purpose
     */
    fun logToFile(message: String) {

        val log = File(Environment.getExternalStorageDirectory(), DRAGONFLY_LOG_FILE)
        var out: BufferedWriter? = null

        try {
            out = BufferedWriter(FileWriter(log.absolutePath, log.exists()))
            out.write(message)
            out.close()
        } catch (e: IOException) {
            // silent
        } finally {
            if (out != null) {
                try {
                    out.close()
                } catch (e: IOException) {
                    // silent
                }

            }
        }
    }

    private fun shouldLog(logLevel: Int): Boolean {
        return BuildConfig.DEBUG || BuildConfig.LOG_LEVEL >= logLevel
    }

    private val tag: String
        get() {

            val stackTraceElement = callerClass

            try {
                return String.format(Locale.getDefault(), "%s->%s(%d)", Class.forName(stackTraceElement?.className)?.simpleName, stackTraceElement?.methodName, stackTraceElement?.lineNumber)
            } catch (e: Throwable) {
                return LOGGER_CLASS_NAME
            }

        }

    private val callerClass: StackTraceElement?
        get() {

            val stackTraceElements = stackTrace

            val lastStackTraceElement: StackTraceElement? = stackTraceElements.indices.reversed()
                    .map { stackTraceElements[it] }
                    .takeWhile { !it.className.contains(LOGGER_CLASS_NAME) }
                    .lastOrNull()

            return lastStackTraceElement
        }

    private val stackTrace: Array<StackTraceElement>
        get() = Thread.currentThread().stackTrace
}
