package com.ciandt.dragonfly.example.features.realtime

import android.Manifest
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.annotation.StringRes
import android.view.View.VISIBLE
import com.ciandt.dragonfly.data.Model
import com.ciandt.dragonfly.example.R
import com.ciandt.dragonfly.example.helpers.IntentHelper
import com.ciandt.dragonfly.example.infrastructure.DragonflyLogger
import com.ciandt.dragonfly.example.infrastructure.PreferencesRepository
import com.ciandt.dragonfly.example.infrastructure.SharedPreferencesRepository
import com.ciandt.dragonfly.example.shared.FullScreenActivity
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.*
import com.karumi.dexter.listener.single.PermissionListener
import kotlinx.android.synthetic.main.activity_real_time.*


class RealTimeActivity : FullScreenActivity(), RealTimeContract.View {
    private lateinit var presenter: RealTimeContract.Presenter

    private var model: Model? = null

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
            model = intent.extras?.getParcelable<Model>(MODEL_BUNDLE)
        }
    }

    private fun setupDragonflyLens() {
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
        model?.let {
            outState?.putParcelable(MODEL_BUNDLE, it)
        }
    }

    override fun checkRealTimeRequiredPermissions() {
        Dexter.withActivity(this)
                .withPermission(Manifest.permission.CAMERA)
                .withListener(object : PermissionListener {
                    override fun onPermissionGranted(response: PermissionGrantedResponse) {
                        DragonflyLogger.debug(LOG_TAG, String.format("%s.onPermissionGranted()", CLASS_NAME))

                        presenter.onRealTimePermissionsGranted()
                    }

                    override fun onPermissionDenied(response: PermissionDeniedResponse) {
                        DragonflyLogger.debug(LOG_TAG, String.format("%s.onPermissionDenied() - permanently? %s", CLASS_NAME, response.isPermanentlyDenied))

                        presenter.onRealTimePermissionsDenied(response.isPermanentlyDenied)
                    }

                    override fun onPermissionRationaleShouldBeShown(permission: PermissionRequest, token: PermissionToken) {
                        DragonflyLogger.debug(LOG_TAG, String.format("%s.onPermissionRationaleShouldBeShown()", CLASS_NAME))

                        token.continuePermissionRequest()
                    }
                })
                .withErrorListener { error ->
                    DragonflyLogger.debug(LOG_TAG, String.format("%s.onError(): %s", CLASS_NAME, error))
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
                    dialog, which ->
                    finish()
                })
                .setPositiveButton(R.string.permissions_open_settings, {
                    dialog, which ->
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

    companion object {
        private val CLASS_NAME = RealTimeActivity::class.java.simpleName
        private val LOG_TAG = RealTimeActivity::class.java.simpleName

        private val MODEL_BUNDLE = "MODEL_BUNDLE"

        fun create(context: Context, model: Model): Intent {
            val intent = Intent(context, RealTimeActivity::class.java)
            intent.putExtra(MODEL_BUNDLE, model)
            return intent
        }
    }
}
