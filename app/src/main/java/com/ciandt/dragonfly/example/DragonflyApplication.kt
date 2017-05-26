package com.ciandt.dragonfly.example

import android.app.Application

import com.crashlytics.android.Crashlytics
import com.facebook.stetho.Stetho

import io.fabric.sdk.android.Fabric
import uk.co.chrisjenx.calligraphy.CalligraphyConfig

class DragonflyApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        setupCrashlytics()
        setupCalligraphy()
        setupStetho()
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
}