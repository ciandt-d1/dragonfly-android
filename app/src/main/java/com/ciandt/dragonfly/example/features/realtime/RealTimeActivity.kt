package com.ciandt.dragonfly.example.features.realtime

import android.Manifest
import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.ciandt.dragonfly.data.Model
import com.ciandt.dragonfly.example.R
import com.ciandt.dragonfly.example.shared.BaseActivity
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.DialogOnAnyDeniedMultiplePermissionsListener
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import kotlinx.android.synthetic.main.activity_real_time.*

class RealTimeActivity : BaseActivity(), RealTimeContract.View {
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
                .withPermissions(Manifest.permission.CAMERA)
                .withListener(object : MultiplePermissionsListener {

                    override fun onPermissionsChecked(report: MultiplePermissionsReport) {
                        if (report.areAllPermissionsGranted()) {
                            presenter.onRealTimePermissionsGranted()
                        } else {
                            presenter.onRealTimePermissionsDenied()
                        }
                    }

                    override fun onPermissionRationaleShouldBeShown(permissions: List<PermissionRequest>, token: PermissionToken) {

                    }
                }).check()
    }

    override fun startRecognition() {
        dragonFlyLens.start(model)
    }

    override fun showRealTimePermissionsError(title: String, message: String) {
        DialogOnAnyDeniedMultiplePermissionsListener.Builder
                .withContext(this@RealTimeActivity)
                .withTitle(title)
                .withMessage(message)
                .withButtonText(android.R.string.ok)
                .withIcon(R.mipmap.ic_launcher)
                .build()
    }

    companion object {
        private val MODEL_BUNDLE = "MODEL_BUNDLE"

        fun create(context: Context, model: Model): Intent {
            val intent = Intent(context, RealTimeActivity::class.java)
            intent.putExtra(MODEL_BUNDLE, model)
            return intent
        }
    }
}
