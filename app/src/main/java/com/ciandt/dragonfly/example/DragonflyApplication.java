package com.ciandt.dragonfly.example;

import android.app.Application;

import com.crashlytics.android.Crashlytics;
import com.facebook.stetho.Stetho;

import io.fabric.sdk.android.Fabric;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;

public class DragonflyApplication extends Application {

    private static final String LOG_TAG = DragonflyApplication.class.getSimpleName();

    @Override
    public void onCreate() {
        super.onCreate();

        setupCrashlytics();
        setupCalligraphy();
        setupStetho();
    }

    private void setupCrashlytics() {
        Fabric.with(this, new Crashlytics());

        Crashlytics.setString("build_user", getString(R.string.build_user));
        Crashlytics.setString("build_date", getString(R.string.build_date));
        Crashlytics.setString("last_commit", getString(R.string.last_commit));
        Crashlytics.setString("branch", getString(R.string.branch));
        Crashlytics.setBool("is_debug", BuildConfig.DEBUG);
    }

    private void setupCalligraphy() {
        CalligraphyConfig config = new CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/Roboto-Regular.ttf")
                .setFontAttrId(R.attr.fontPath)
                .build();

        CalligraphyConfig.initDefault(config);
    }

    private void setupStetho() {
        Stetho.initializeWithDefaults(this);
    }
}
