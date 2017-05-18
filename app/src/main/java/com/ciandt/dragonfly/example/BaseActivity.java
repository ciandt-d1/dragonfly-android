package com.ciandt.dragonfly.example;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.LayoutRes;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;

import com.ciandt.dragonfly.example.config.Features;
import com.ciandt.dragonfly.example.config.CommonBundleNames;
import com.ciandt.dragonfly.example.debug.DebugActionsHelper;

import io.palaima.debugdrawer.DebugDrawer;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

/**
 * Created by iluz on 5/15/17.
 */

public abstract class BaseActivity extends AppCompatActivity implements DebugActionsHelper.DebuggableActivity {

    protected DebugDrawer debugDrawer;

    @Override
    protected void onStart() {
        super.onStart();

        if (debugDrawer != null) {
            debugDrawer.onStart();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (debugDrawer != null) {
            debugDrawer.onResume();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (debugDrawer != null) {
            debugDrawer.onPause();
        }
    }

    @Override
    protected void onStop() {
        if (debugDrawer != null) {
            debugDrawer.onStop();
        }

        super.onStop();
    }

    @Override
    public void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    public void setContentView(@LayoutRes int layoutResID) {
        super.setContentView(layoutResID);

        configDebugDrawer(getIntent());
    }


    @Override
    public void setContentView(View view) {
        super.setContentView(view);

        configDebugDrawer(getIntent());
    }

    @Override
    public void setContentView(View view, ViewGroup.LayoutParams params) {
        super.setContentView(view, params);

        configDebugDrawer(getIntent());
    }

    @Override
    public DebugDrawer getCurrentDebugDrawer() {
        return debugDrawer;
    }

    public void configDebugDrawer(Intent currentIntent) {
        if (Features.ENABLE_DEBUG_DRAWER && currentIntent.getBooleanExtra(CommonBundleNames.SHOW_DEBUG_DRAWER_BUNDLE, true)) {
            buildNewDebugDrawer();
        }
    }

    @Override
    public DebugDrawer buildNewDebugDrawer() {
        return debugDrawer = DebugActionsHelper.buildDebugDrawer(this);
    }

    @Override
    public Activity getActivityInstance() {
        return this;
    }
}
