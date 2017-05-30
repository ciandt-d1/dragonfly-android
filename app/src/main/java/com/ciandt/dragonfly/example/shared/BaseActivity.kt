package com.ciandt.dragonfly.example.shared

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.support.annotation.LayoutRes
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.view.ViewGroup
import com.ciandt.dragonfly.example.config.CommonBundleNames
import com.ciandt.dragonfly.example.config.Features
import com.ciandt.dragonfly.example.debug.DebugActionsHelper
import io.palaima.debugdrawer.DebugDrawer
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper

/**
 * Created by iluz on 5/15/17.
 */

abstract class BaseActivity(protected var hasDebugDrawer: Boolean = true) : AppCompatActivity(), DebugActionsHelper.DebuggableActivity {

    protected var debugDrawer: DebugDrawer? = null

    override fun onStart() {
        super.onStart()

        debugDrawer?.onStart()
    }

    override fun onResume() {
        super.onResume()

        debugDrawer?.onResume()
    }

    override fun onPause() {
        super.onPause()

        debugDrawer?.onPause()
    }

    override fun onStop() {
        debugDrawer?.onStop()

        super.onStop()
    }

    public override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase))
    }

    override fun setContentView(@LayoutRes layoutResID: Int) {
        super.setContentView(layoutResID)

        configDebugDrawer(intent)
    }

    override fun setContentView(view: View) {
        super.setContentView(view)

        configDebugDrawer(intent)
    }

    override fun setContentView(view: View, params: ViewGroup.LayoutParams) {
        super.setContentView(view, params)

        configDebugDrawer(intent)
    }

    override fun getCurrentDebugDrawer(): DebugDrawer? {
        return debugDrawer
    }

    fun configDebugDrawer(currentIntent: Intent) {
        if (hasDebugDrawer && Features.ENABLE_DEBUG_DRAWER && currentIntent.getBooleanExtra(CommonBundleNames.SHOW_DEBUG_DRAWER_BUNDLE, true)) {
            buildNewDebugDrawer()
        }
    }

    override fun buildNewDebugDrawer(): DebugDrawer? {
        debugDrawer = DebugActionsHelper.buildDebugDrawer(this)
        return debugDrawer
    }

    override fun getActivityInstance(): Activity {
        return this
    }
}
