package com.ciandt.dragonfly.example.shared

import android.app.Activity
import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.support.annotation.LayoutRes
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.view.ViewGroup
import com.ciandt.dragonfly.example.R
import com.ciandt.dragonfly.example.config.CommonBundleNames
import com.ciandt.dragonfly.example.config.Features
import com.ciandt.dragonfly.example.debug.DebugActionsHelper
import com.ciandt.dragonfly.example.features.login.LoginActivity
import com.ciandt.dragonfly.example.helpers.IntentHelper
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.GoogleApiClient
import com.google.firebase.auth.FirebaseAuth
import io.palaima.debugdrawer.DebugDrawer
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper

/**
 * Created by iluz on 5/15/17.
 */

abstract class BaseActivity(protected var hasDebugDrawer: Boolean = true) : AppCompatActivity(), DebugActionsHelper.DebuggableActivity {

    protected var debugDrawer: DebugDrawer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enforceLoginIfNeeded()
    }

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

        configTaskDescription()

        configDebugDrawer(intent)
    }

    override fun setContentView(view: View) {
        super.setContentView(view)

        configTaskDescription()

        configDebugDrawer(intent)
    }

    override fun setContentView(view: View, params: ViewGroup.LayoutParams) {
        super.setContentView(view, params)

        configTaskDescription()

        configDebugDrawer(intent)
    }

    override fun getCurrentDebugDrawer(): DebugDrawer? {
        return debugDrawer
    }

    fun configDebugDrawer(currentIntent: Intent) {
        val showDebugDrawer = currentIntent.getBooleanExtra(CommonBundleNames.SHOW_DEBUG_DRAWER_BUNDLE, true)
        if (hasDebugDrawer && Features.ENABLE_DEBUG_DRAWER && showDebugDrawer) {
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

    fun checkPendingPermissions(permissions: List<String>): List<String> {
        val pendingPermissions = ArrayList<String>()
        permissions.forEach {
            val isPermissionGranted = ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
            if (!isPermissionGranted) {
                pendingPermissions.add(it)
            }
        }

        return pendingPermissions
    }

    open fun requiresUserToBeSignedIn(): Boolean {
        return true
    }

    private fun enforceLoginIfNeeded() {
        if (requiresUserToBeSignedIn() && !isUserLogged()) {
            startActivity(IntentHelper.openLogin(this))
            finish()
        }
    }

    private fun isUserLogged(): Boolean {
        return FirebaseAuth.getInstance().currentUser != null
    }

    protected fun logout() {
        FirebaseAuth.getInstance().signOut()

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build()

        val googleApiClient = GoogleApiClient.Builder(this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build()

        googleApiClient.connect()
        googleApiClient.registerConnectionCallbacks(object : GoogleApiClient.ConnectionCallbacks {

            override fun onConnected(bundle: Bundle?) {
                if (googleApiClient.isConnected) {
                    Auth.GoogleSignInApi.signOut(googleApiClient).setResultCallback {
                        if (googleApiClient.isConnected) {
                            googleApiClient.clearDefaultAccountAndReconnect()
                            googleApiClient.disconnect()
                        }
                    }
                }
            }

            override fun onConnectionSuspended(i: Int) {

            }
        })

        startActivity(LoginActivity.create(this))
    }

    private var icLauncher: Bitmap? = null

    fun configTaskDescription() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            if (icLauncher == null) {
                icLauncher = BitmapFactory.decodeResource(resources, R.mipmap.ic_launcher)
            }
            val color = ContextCompat.getColor(this, R.color.white)
            val taskDescription = ActivityManager.TaskDescription(resources.getString(R.string.app_name), icLauncher, color)
            setTaskDescription(taskDescription)
        }
    }

}
