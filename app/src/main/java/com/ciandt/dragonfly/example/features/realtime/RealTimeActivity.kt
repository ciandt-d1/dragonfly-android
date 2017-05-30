package com.ciandt.dragonfly.example.features.realtime

import android.Manifest
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.annotation.StringRes
import com.ciandt.dragonfly.data.Model
import com.ciandt.dragonfly.example.R
import com.ciandt.dragonfly.example.infrastructure.DragonflyLogger
import com.ciandt.dragonfly.example.shared.FullScreenActivity
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.*
import com.karumi.dexter.listener.multi.DialogOnAnyDeniedMultiplePermissionsListener
import com.karumi.dexter.listener.single.PermissionListener
import kotlinx.android.synthetic.main.activity_real_time.*


class RealTimeActivity : FullScreenActivity(), RealTimeContract.View {
    private lateinit var presenter: RealTimeContract.Presenter

    private var model: Model? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_real_time)

        presenter = RealTimePresenter()

        if (savedInstanceState != null) {
            model = savedInstanceState.getParcelable(MODEL_BUNDLE)
        } else {
            model = intent.extras?.getParcelable<Model>(MODEL_BUNDLE)
        }
    }

    override fun onResume() {
        super.onResume()
        presenter.attachView(this)
    }

    override fun onPause() {
        super.onPause()
        presenter.detachView()
        dragonFlyLens.stop()
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)
        model?.let {
            outState?.putParcelable(MODEL_BUNDLE, it)
        }
    }

    override fun requestRealTimePermissions() {
        Dexter.withActivity(this)
                .withPermission(Manifest.permission.CAMERA)
                .withListener(object : PermissionListener {
                    override fun onPermissionGranted(response: PermissionGrantedResponse) {
                        presenter.onRealTimePermissionsGranted()
                    }

                    override fun onPermissionDenied(response: PermissionDeniedResponse) {
                        if (response.isPermanentlyDenied) {
                            presenter.onRealTimePermissionsPermanentlyDenied()
                        } else {
                            presenter.onRealTimePermissionsDenied()
                        }
                    }

                    override fun onPermissionRationaleShouldBeShown(permission: PermissionRequest, token: PermissionToken) {
                        token.continuePermissionRequest();
                    }
                })
                .withErrorListener(object : PermissionRequestErrorListener {
                    override fun onError(error: DexterError) {
                        DragonflyLogger.error(LOG_TAG, "Dexter error: " + error.toString());
                    }
                })
                .check()
    }

    override fun startRecognition() {
        dragonFlyLens.start(model)
    }

    override fun showRealTimePermissionsError(@StringRes title: Int, @StringRes message: Int) {
        DialogOnAnyDeniedMultiplePermissionsListener.Builder
                .withContext(this@RealTimeActivity)
                .withTitle(title)
                .withMessage(message)
                .withButtonText(android.R.string.ok)
                .withIcon(R.mipmap.ic_launcher)
                .build()
    }

    companion object {
        private val LOG_TAG = RealTimeActivity.javaClass.simpleName

        private val MODEL_BUNDLE = "MODEL_BUNDLE"

        fun create(context: Context, model: Model): Intent {
            val intent = Intent(context, RealTimeActivity::class.java)
            intent.putExtra(MODEL_BUNDLE, model)
            return intent
        }
    }
}
