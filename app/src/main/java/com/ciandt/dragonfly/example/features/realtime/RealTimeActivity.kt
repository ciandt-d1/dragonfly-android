package com.ciandt.dragonfly.example.features.realtime

import android.Manifest
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.annotation.StringRes
import android.view.View.VISIBLE
import com.ciandt.dragonfly.data.model.Model
import com.ciandt.dragonfly.example.BuildConfig
import com.ciandt.dragonfly.example.R
import com.ciandt.dragonfly.example.features.feedback.FeedbackActivity
import com.ciandt.dragonfly.example.helpers.IntentHelper
import com.ciandt.dragonfly.example.infrastructure.DragonflyLogger
import com.ciandt.dragonfly.example.infrastructure.PreferencesRepository
import com.ciandt.dragonfly.example.infrastructure.SharedPreferencesRepository
import com.ciandt.dragonfly.example.shared.FullScreenActivity
import com.ciandt.dragonfly.lens.data.DragonflyCameraSnapshot
import com.ciandt.dragonfly.lens.exception.DragonflySnapshotException
import com.ciandt.dragonfly.lens.ui.DragonflyLensRealtimeView
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.karumi.dexter.listener.single.PermissionListener
import kotlinx.android.synthetic.main.activity_real_time.*
import java.security.InvalidParameterException


class RealTimeActivity : FullScreenActivity(), RealTimeContract.View, DragonflyLensRealtimeView.SnapshotCallbacks {
    private lateinit var presenter: RealTimeContract.Presenter

    lateinit private var model: Model

    private var missingPermissionsAlertDialog: AlertDialog? = null
    private var comingFromSettings = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_real_time)

        val preferencesRepository: PreferencesRepository = SharedPreferencesRepository.get(applicationContext)
        presenter = RealTimePresenter(preferencesRepository)

        setupDragonflyLens()

        if (savedInstanceState != null) {
            model = savedInstanceState.getParcelable(MODEL_BUNDLE)
        } else {
            model = intent.extras.getParcelable<Model>(MODEL_BUNDLE)
        }
    }

    private fun setupDragonflyLens() {
        setupDragonflyLensCameraOrnament()

        dragonFlyLens.setSnapshotCallbacks(this)

        setupDragonflyLensPermissionsCallback()
    }

    private fun setupDragonflyLensPermissionsCallback() {
        dragonFlyLens.setPermissionsCallback(DragonflyLensRealtimeView.PermissionsCallback {
            permissions ->

            if (permissions == null || permissions.size == 0) {
                throw InvalidParameterException("Empty permissions list provided.")
            }

            val pendingPermissions = checkPendingPermissions(permissions)

            if (pendingPermissions.isEmpty()) {
                return@PermissionsCallback true
            } else {
                Dexter
                        .withActivity(this)
                        .withPermissions(pendingPermissions)
                        .withListener(object : MultiplePermissionsListener {
                            override fun onPermissionsChecked(report: MultiplePermissionsReport) {
                                DragonflyLogger.debug(LOG_TAG, "onPermissionsChecked()")
                            }

                            override fun onPermissionRationaleShouldBeShown(permissions: MutableList<PermissionRequest>, token: PermissionToken) {
                                token.continuePermissionRequest()
                            }

                        })
                        .check()

                return@PermissionsCallback false
            }
        })
    }

    private fun setupDragonflyLensCameraOrnament() {
        dragonFlyLens.setCameraOrnamentVisibilityCallback { ornament ->
            ornament.alpha = 0f
            ornament.visibility = VISIBLE
            ornament.animate()
                    .alpha(1.0f)
                    .setDuration(3000)
        }
    }

    override fun onResume() {
        super.onResume()

        if (comingFromSettings) {
            comingFromSettings = false
            checkRealTimeRequiredPermissions()
        }

        presenter.attachView(this)
    }

    override fun onPause() {
        super.onPause()

        missingPermissionsAlertDialog?.dismiss()
        presenter.detachView()
        dragonFlyLens.stop()
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)
        model.let {
            outState?.putParcelable(MODEL_BUNDLE, it)
        }
    }

    override fun checkRealTimeRequiredPermissions() {
        Dexter.withActivity(this)
                .withPermission(Manifest.permission.CAMERA)
                .withListener(object : PermissionListener {
                    override fun onPermissionGranted(response: PermissionGrantedResponse) {
                        DragonflyLogger.debug(LOG_TAG, "checkRealTimeRequiredPermissions() - onPermissionGranted()")

                        presenter.onRealTimePermissionsGranted()
                    }

                    override fun onPermissionDenied(response: PermissionDeniedResponse) {
                        DragonflyLogger.debug(LOG_TAG, "checkRealTimeRequiredPermissions() - onPermissionDenied() - permanently? ${response.isPermanentlyDenied}")

                        presenter.onRealTimePermissionsDenied(response.isPermanentlyDenied)
                    }

                    override fun onPermissionRationaleShouldBeShown(permission: PermissionRequest, token: PermissionToken) {
                        DragonflyLogger.debug(LOG_TAG, "${CLASS_NAME}.onPermissionRationaleShouldBeShown()")

                        token.continuePermissionRequest()
                    }
                })
                .withErrorListener { error ->
                    DragonflyLogger.debug(LOG_TAG, "${CLASS_NAME}.onError(): ${error}")
                }
                .check()
    }

    override fun startRecognition() {
        dragonFlyLens.start(model)
    }

    override fun showRealTimePermissionsRequiredAlert(@StringRes title: Int, @StringRes message: Int) {
        missingPermissionsAlertDialog = AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setCancelable(false)
                .setNegativeButton(android.R.string.cancel, {
                    _, _ ->
                    finish()
                })
                .setPositiveButton(R.string.permissions_open_settings, {
                    dialog, _ ->
                    dialog.dismiss()

                    // https://stackoverflow.com/a/27575063/1120207
                    comingFromSettings = true
                    startActivity(IntentHelper.openSettings())
                })
                .setIcon(R.mipmap.ic_launcher)
                .create()

        missingPermissionsAlertDialog?.setCanceledOnTouchOutside(false)
        missingPermissionsAlertDialog?.show()
    }

    override fun onStartTakingSnapshot() {
        DragonflyLogger.debug(LOG_TAG, "onStartTakingSnapshot()")
    }

    override fun onSnapshotTaken(snapshot: DragonflyCameraSnapshot) {
        DragonflyLogger.debug(LOG_TAG, "onSnapshotTaken(${snapshot})")

        intent = FeedbackActivity.newIntent(this, model, snapshot)
        startActivity(intent)
    }

    override fun onSnapshotError(e: DragonflySnapshotException) {
        DragonflyLogger.debug(LOG_TAG, "onSnapshotError(${e})")
    }

    companion object {
        private val CLASS_NAME = RealTimeActivity::class.java.simpleName
        private val LOG_TAG = RealTimeActivity::class.java.simpleName

        private val MODEL_BUNDLE = "${BuildConfig.APPLICATION_ID}.model_bundle"

        fun create(context: Context, model: Model): Intent {
            val intent = Intent(context, RealTimeActivity::class.java)
            intent.putExtra(MODEL_BUNDLE, model)
            return intent
        }
    }
}
