package com.ciandt.dragonfly.example

import android.app.Application
import android.os.Build
import android.os.Environment
import com.ciandt.dragonfly.example.config.Features
import com.ciandt.dragonfly.example.config.FirebaseConfig
import com.ciandt.dragonfly.example.data.DatabaseManager
import com.ciandt.dragonfly.example.features.feedback.jobs.ProcessStashedFeedbackJob
import com.ciandt.dragonfly.example.infrastructure.jobs.DragonflyJobCreator
import com.ciandt.dragonfly.infrastructure.DragonflyConfig
import com.ciandt.dragonfly.infrastructure.DragonflyLogger
import com.crashlytics.android.Crashlytics
import com.evernote.android.job.JobManager
import com.facebook.stetho.Stetho
import com.google.firebase.FirebaseApp
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
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
            return
        }
        LeakCanary.install(this)

        setupCrashlytics()
        setupCalligraphy()
        setupStetho()
        setupDragonflyLib()
        setupFirebase()
        setupDatabase()
        setupJobScheduler()
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
        DragonflyConfig.shouldSaveCapturedCameraFramesInDebugMode(Features.SAVE_CAPTURED_IMAGES_FOR_DEBUGGING)
        DragonflyConfig.shouldSaveSelectedExistingBitmapsInDebugMode(Features.SAVE_SELECTED_IMAGE_FOR_DEBUGGING)

        val externalStorageDirectory = Environment.getExternalStorageDirectory().absolutePath

        val stagingPath = externalStorageDirectory + File.separator + BuildConfig.APPLICATION_ID
        DragonflyConfig.setStagingPath(stagingPath)

        val userSavedImagePath = externalStorageDirectory + File.separator + getString(R.string.app_name)
        DragonflyConfig.setUserSavedImagesPath(userSavedImagePath)

        DragonflyConfig.setMaxModelLoadingRetryAttempts(5)
    }

    private fun setupFirebase() {
        FirebaseApp.initializeApp(this)
        FirebaseDatabase.getInstance().setPersistenceEnabled(true)

        val firebaseRemoteConfig = FirebaseRemoteConfig.getInstance()

        firebaseRemoteConfig.setConfigSettings(FirebaseRemoteConfigSettings.Builder()
                .setDeveloperModeEnabled(BuildConfig.DEBUG)
                .build())

        firebaseRemoteConfig.setDefaults(R.xml.remote_config_defaults)

        firebaseRemoteConfig
                .fetch(FirebaseConfig.REMOTE_CONFIG_CACHE_EXPIRATON.toLong())
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        firebaseRemoteConfig.activateFetched()
                    }
                }
    }

    private fun setupDatabase() {
        DatabaseManager.init(this)
    }

    private fun setupJobScheduler() {
        JobManager
                .create(this)
                .addJobCreator(DragonflyJobCreator())

        val allowSmallerIntervalsForMarshmallow = BuildConfig.DEBUG && Build.VERSION.SDK_INT < 24
        JobManager.instance().config.isAllowSmallerIntervalsForMarshmallow = allowSmallerIntervalsForMarshmallow

        ProcessStashedFeedbackJob.schedule()
    }
}
