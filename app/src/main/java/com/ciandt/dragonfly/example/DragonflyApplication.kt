package com.ciandt.dragonfly.example

import android.app.Application
import android.os.Environment
import com.ciandt.dragonfly.example.config.Features
import com.ciandt.dragonfly.infrastructure.DragonflyConfig
import com.ciandt.dragonfly.infrastructure.DragonflyLogger

import com.crashlytics.android.Crashlytics
import com.facebook.stetho.Stetho
import com.squareup.leakcanary.LeakCanary

import io.fabric.sdk.android.Fabric
import uk.co.chrisjenx.calligraphy.CalligraphyConfig
import java.io.File

class DragonflyApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        if (LeakCanary.isInAnalyzerProcess(this)) {
            // This process is dedicated to LeakCanary for heap analysis.
            // You should not init your app in this process.
            return;
        }
        LeakCanary.install(this);

        setupCrashlytics()
        setupCalligraphy()
        setupStetho()
        setupDragonflyLib()
    }

    private fun setupCrashlytics() {
        Fabric.with(this, Crashlytics())

        Crashlytics.setString("build_user", getString(R.string.build_user))
        Crashlytics.setString("build_date", getString(R.string.build_date))
        Crashlytics.setString("last_commit", getString(R.string.last_commit))
        Crashlytics.setString("branch", getString(R.string.branch))
        Crashlytics.setBool("is_debug", BuildConfig.DEBUG)
    }

    private fun setupCalligraphy() {
        val config = CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/Roboto-Regular.ttf")
                .setFontAttrId(R.attr.fontPath)
                .build()

        CalligraphyConfig.initDefault(config)
    }

    private fun setupStetho() {
        Stetho.initializeWithDefaults(this)
    }

    private fun setupDragonflyLib() {
        DragonflyLogger.setLogLevel(DragonflyLogger.LOG_LEVEL_DEBUG)
        DragonflyConfig.shouldSaveBitmapsInDebugMode(Features.SAVE_CAPTURED_IMAGES_TO_DEVICE)

        val dropboxPath = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + BuildConfig.APPLICATION_ID;
        DragonflyConfig.setDropboxPath(dropboxPath)

        DragonflyConfig.setMaxModelLoadingRetryAttempts(5)
    }
}
