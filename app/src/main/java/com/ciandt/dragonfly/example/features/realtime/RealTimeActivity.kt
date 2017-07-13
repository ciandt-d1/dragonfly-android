package com.ciandt.dragonfly.example.features.realtime

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.annotation.StringRes
import android.view.View.GONE
import android.view.View.VISIBLE
import com.ciandt.dragonfly.data.model.Model
import com.ciandt.dragonfly.example.BuildConfig
import com.ciandt.dragonfly.example.R
import com.ciandt.dragonfly.example.config.PermissionsMapping
import com.ciandt.dragonfly.example.features.feedback.FeedbackActivity
import com.ciandt.dragonfly.example.helpers.IntentHelper
import com.ciandt.dragonfly.example.infrastructure.DragonflyLogger
import com.ciandt.dragonfly.example.infrastructure.SharedPreferencesRepository
import com.ciandt.dragonfly.example.shared.FullScreenActivity
import com.ciandt.dragonfly.infrastructure.DragonflyConfig
import com.ciandt.dragonfly.lens.data.DragonflyClassificationInput
import com.ciandt.dragonfly.lens.exception.DragonflyModelException
import com.ciandt.dragonfly.lens.exception.DragonflyRecognitionException
import com.ciandt.dragonfly.lens.exception.DragonflySnapshotException
import com.ciandt.dragonfly.lens.ui.DragonflyLensRealtimeView
import com.ciandt.dragonfly.tensorflow.Classifier
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


class RealTimeActivity : FullScreenActivity(), RealTimeContract.View, DragonflyLensRealtimeView.ModelCallbacks, DragonflyLensRealtimeView.SnapshotCallbacks, DragonflyLensRealtimeView.UriAnalysisCallbacks {

    private lateinit var presenter: RealTimeContract.Presenter

    lateinit private var model: Model

    private var missingPermissionsAlertDialog: AlertDialog? = null
    private var comingFromSettings = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_real_time)

        val preferencesRepository = SharedPreferencesRepository.get(applicationContext)
        presenter = RealTimePresenter(preferencesRepository)

        if (savedInstanceState != null) {
            savedInstanceState.apply {
                model = getParcelable(MODEL_BUNDLE)
            }
        } else {
            model = intent.extras.getParcelable<Model>(MODEL_BUNDLE)
        }

        setupBackButton()
        setupDragonflyLens()
        setupSelectExistingImageButton()
    }

    override fun onDestroy() {
        super.onDestroy()
        dragonFlyLens.unloadModel();
    }

    private fun setupBackButton() {
        btnBack.setOnClickListener({
            super.onBackPressed()
        })
    }

    private fun setupDragonflyLens() {
        hideActionButtons()

        dragonFlyLens.setModelCallbacks(this)
        dragonFlyLens.loadModel(model)
        dragonFlyLens.setSnapshotCallbacks(this)
        dragonFlyLens.setUriAnalysisCallbacks(this)

        setupDragonflyLensPermissionsCallback()
    }

    private fun setupSelectExistingImageButton() {
        btnSelectExistingPicture.setOnClickListener({
            presenter.classifyExistingPicture()
        })
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

        outState?.apply {
            putParcelable(MODEL_BUNDLE, model)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode != Activity.RESULT_OK) {
            return
        }

        if (data == null) {
            return
        }

        when (requestCode) {
            REQUEST_CODE_SELECT_IMAGE -> {
                hideActionButtons()

                DragonflyLogger.debug(LOG_TAG, "requestCode: ${requestCode}")

                val fileUri = data.getData()
                val takeFlags = data.getFlags() and
                        (Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)

                contentResolver.takePersistableUriPermission(fileUri, takeFlags);

                dragonFlyLens.analyzeFromUri(fileUri)

                DragonflyLogger.debug(LOG_TAG, "imageUri: ${fileUri}")
            }
        }
    }

    override fun checkRealTimeRequiredPermissions() {
        Dexter.withActivity(this)
                .withPermission(PermissionsMapping.REAL_TIME)
                .withListener(CameraPermissionListener(presenter))
                .withErrorListener { error ->
                    DragonflyLogger.debug(LOG_TAG, "Dexter error: ${error}")
                }
                .check()
    }

    override fun startRealTimeClassification() {
        dragonFlyLens.start()
    }

    override fun showPermissionsRequiredAlert(@StringRes title: Int, @StringRes message: Int) {
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

    override fun onStartLoadingModel(model: Model?) {
        hideActionButtons()
    }

    override fun onModelReady(model: Model?) {
        showActionButtons(true)
    }

    override fun onModelLoadFailure(e: DragonflyModelException?) {
        showActionButtons(true)
    }

    override fun onStartTakingSnapshot() {
        DragonflyLogger.debug(LOG_TAG, "onStartTakingSnapshot()")
    }

    override fun selectImageFromLibrary() {
        val intent = IntentHelper.selectImageFromLibrary()
        startActivityForResult(intent, REQUEST_CODE_SELECT_IMAGE)
    }

    override fun onSnapshotTaken(snapshot: DragonflyClassificationInput) {
        DragonflyLogger.debug(LOG_TAG, "onSnapshotTaken(${snapshot})")

        intent = FeedbackActivity.newIntent(this, model, snapshot, dragonFlyLens.lastClassifications)
        startActivity(intent)
    }

    override fun onSnapshotError(e: DragonflySnapshotException) {
        DragonflyLogger.debug(LOG_TAG, "onSnapshotError(${e})")
    }

    override fun onUriAnalysisFinished(uri: Uri, classificationInput: DragonflyClassificationInput, recognitions: List<Classifier.Recognition>) {
        DragonflyLogger.debug(LOG_TAG, "onUriAnalysisFinished(${recognitions})")

        intent = FeedbackActivity.newIntent(this, model, classificationInput, recognitions)
        startActivity(intent)

        showActionButtons()
    }

    override fun onUriAnalysisFailed(e: DragonflyRecognitionException) {
        DragonflyLogger.error(LOG_TAG, e)
        hideActionButtons()
    }

    private fun hideActionButtons() {
        btnSelectExistingPicture.visibility = GONE
    }

    private fun showActionButtons(animate: Boolean = false) {
        if (animate) {
            val duration = DragonflyConfig.getRealTimeControlsVisibilityAnimationDuration()

            btnSelectExistingPicture.alpha = 0f
            btnSelectExistingPicture.visibility = VISIBLE
            btnSelectExistingPicture.animate()
                    .alpha(1.0f)
                    .setDuration(duration)
        } else {
            btnSelectExistingPicture.visibility = VISIBLE
        }
    }

    companion object {
        private val LOG_TAG = RealTimeActivity::class.java.simpleName

        private val MODEL_BUNDLE = "${BuildConfig.APPLICATION_ID}.model_bundle"

        private val REQUEST_CODE_SELECT_IMAGE = 1

        fun create(context: Context, model: Model): Intent {
            val intent = Intent(context, RealTimeActivity::class.java)
            intent.putExtra(MODEL_BUNDLE, model)
            return intent
        }

        class CameraPermissionListener(val presenter: RealTimeContract.Presenter) : PermissionListener {
            override fun onPermissionGranted(response: PermissionGrantedResponse) {
                DragonflyLogger.debug(LOG_TAG, "CameraPermissionListener.onPermissionGranted()")

                presenter.onRealTimePermissionsGranted()
            }

            override fun onPermissionDenied(response: PermissionDeniedResponse) {
                DragonflyLogger.debug(LOG_TAG, "CameraPermissionListener.onPermissionDenied() - permanently? ${response.isPermanentlyDenied}")

                presenter.onRealTimePermissionsDenied(response.isPermanentlyDenied)
            }

            override fun onPermissionRationaleShouldBeShown(permission: PermissionRequest, token: PermissionToken) {
                DragonflyLogger.debug(LOG_TAG, "CameraPermissionListener.onPermissionRationaleShouldBeShown()")

                token.continuePermissionRequest()
            }
        }
    }
}
