package com.ciandt.dragonfly.example.features.realtime

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.support.annotation.StringRes
import android.view.View.GONE
import android.view.WindowManager
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.ViewTarget
import com.bumptech.glide.request.transition.Transition
import com.ciandt.dragonfly.data.model.Model
import com.ciandt.dragonfly.example.BuildConfig
import com.ciandt.dragonfly.example.R
import com.ciandt.dragonfly.example.config.PermissionsMapping
import com.ciandt.dragonfly.example.config.RealTimeConfig
import com.ciandt.dragonfly.example.features.feedback.FeedbackActivity
import com.ciandt.dragonfly.example.helpers.IntentHelper
import com.ciandt.dragonfly.example.infrastructure.DragonflyLogger
import com.ciandt.dragonfly.example.infrastructure.SharedPreferencesRepository
import com.ciandt.dragonfly.example.infrastructure.extensions.isVisible
import com.ciandt.dragonfly.example.infrastructure.extensions.makeVisible
import com.ciandt.dragonfly.example.shared.InvisibleToolbarActivity
import com.ciandt.dragonfly.infrastructure.ClassificationConfig
import com.ciandt.dragonfly.infrastructure.DragonflyConfig
import com.ciandt.dragonfly.lens.data.DragonflyClassificationInput
import com.ciandt.dragonfly.lens.exception.DragonflyClassificationException
import com.ciandt.dragonfly.lens.exception.DragonflyModelException
import com.ciandt.dragonfly.lens.exception.DragonflyNoMemoryAvailableException
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

class RealTimeActivity : InvisibleToolbarActivity(), RealTimeContract.View, DragonflyLensRealtimeView.ModelCallbacks, DragonflyLensRealtimeView.SnapshotCallbacks, DragonflyLensRealtimeView.UriAnalysisCallbacks {
    private lateinit var presenter: RealTimeContract.Presenter
    private lateinit var model: Model
    private lateinit var modelName: String

    private var noMemoryAvailableDialog: AlertDialog? = null

    private var missingPermissionsAlertDialog: AlertDialog? = null
    private var comingFromSettings = false

    private var uriUnderAnalysis: Uri? = null
    private var lastAnalyzedUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        setContentView(R.layout.activity_real_time)

        val preferencesRepository = SharedPreferencesRepository.get(applicationContext)
        presenter = RealTimePresenter(preferencesRepository)

        if (savedInstanceState != null) {
            savedInstanceState.apply {
                model = getParcelable(MODEL_BUNDLE)
                modelName = getString(MODEL_NAME_BUNDLE)
                uriUnderAnalysis = getParcelable(IMAGE_URI_BUNDLE)
                lastAnalyzedUri = getParcelable(LAST_ANALYZED_IMAGE_URI_BUNDLE)
            }
        } else {
            model = intent.extras.getParcelable(MODEL_BUNDLE)
            modelName = intent.extras.getString(MODEL_NAME_BUNDLE)
            uriUnderAnalysis = null
        }

        setupToolbar()
        setupDragonflyLens()
        setupSelectExistingImageButton()
    }

    override fun onDestroy() {
        super.onDestroy()
        dragonFlyLens.unloadModel()
    }

    private fun setupToolbar() {
        btnBack.setOnClickListener({
            super.onBackPressed()
        })

        titleView.text = modelName
    }

    private fun setupDragonflyLens() {
        hideActionButtons()

        val classificationAlgorithm = RealTimeConfig.CLASSIFICATION_ATENUATION_ALGORITHM
        val classificationConfigBuilder = ClassificationConfig.newBuilder().withClassificationAtenuationAlgorithm(classificationAlgorithm)
        if (classificationAlgorithm == ClassificationConfig.CLASSIFICATION_ATENUATION_ALGORITHM_DECAY) {
            classificationConfigBuilder.apply {
                withDecayAtenuationDecayValue(RealTimeConfig.CLASSIFICATION_ATENUATION_ALGORITHM_DECAY__DECAY_VALUE)
                withDecayAtenuationUpdateValue(RealTimeConfig.CLASSIFICATION_ATENUATION_ALGORITHM_DECAY__UPDATE_VALUE)
                withDecayAtenuationMinimumThreshold(RealTimeConfig.CLASSIFICATION_ATENUATION_ALGORITHM_DECAY__MINIMUM_THRESHOLD)
            }
        }

        dragonFlyLens.setClassificationConfig(classificationConfigBuilder.build())
        dragonFlyLens.setModelCallbacks(this)
        dragonFlyLens.loadModel(model)
        dragonFlyLens.setSnapshotCallbacks(this)
        dragonFlyLens.setUriAnalysisCallbacks(this)

        Glide
                .with(this)
                .load(R.drawable.dragonfly_lens_grid)
                .into(object : ViewTarget<DragonflyLensRealtimeView, Drawable>(dragonFlyLens) {
                    override fun onResourceReady(resource: Drawable?, anim: Transition<in Drawable>?) {
                        view.setOrnamentDrawable(resource)
                    }
                })

        setupDragonflyLensPermissionsCallback()
    }

    private fun setupSelectExistingImageButton() {
        btnSelectExistingPicture.setOnClickListener({
            presenter.classifyExistingPicture()
        })
    }

    private fun setupDragonflyLensPermissionsCallback() {
        dragonFlyLens.setPermissionsCallback(DragonflyLensRealtimeView.PermissionsCallback { permissions ->

            if (permissions == null || permissions.size == 0) {
                throw IllegalArgumentException("Empty permissions list provided.")
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

        uriUnderAnalysis?.let {
            dragonFlyLens.analyzeFromUri(it)
        }
    }

    override fun onPause() {
        super.onPause()

        noMemoryAvailableDialog?.dismiss()
        missingPermissionsAlertDialog?.dismiss()

        presenter.detachView()

        dragonFlyLens.stop()
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)

        outState?.apply {
            putParcelable(MODEL_BUNDLE, model)
            putString(MODEL_NAME_BUNDLE, modelName)
            putParcelable(IMAGE_URI_BUNDLE, uriUnderAnalysis)
            putParcelable(LAST_ANALYZED_IMAGE_URI_BUNDLE, lastAnalyzedUri)
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

                DragonflyLogger.debug(LOG_TAG, "requestCode: $requestCode")

                val fileUri = data.data
                val takeFlags = data.flags and
                        (Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)

                contentResolver.takePersistableUriPermission(fileUri, takeFlags)

                dragonFlyLens.analyzeFromUri(fileUri)

                uriUnderAnalysis = fileUri
                lastAnalyzedUri = fileUri

                DragonflyLogger.debug(LOG_TAG, "imageUri: $fileUri")
            }
        }
    }

    override fun checkRealTimeRequiredPermissions() {
        Dexter.withActivity(this)
                .withPermission(PermissionsMapping.REAL_TIME)
                .withListener(CameraPermissionListener(presenter))
                .withErrorListener { error ->
                    DragonflyLogger.debug(LOG_TAG, "Dexter error: $error")
                }
                .check()
    }

    override fun checkSelectImageFromLibraryRequiredPermissions() {
        Dexter.withActivity(this)
                .withPermission(PermissionsMapping.SELECT_IMAGE_FROM_LIBRARY)
                .withListener(object : PermissionListener {
                    override fun onPermissionGranted(response: PermissionGrantedResponse) {
                        DragonflyLogger.debug(LOG_TAG, "SelectImageFromLibraryPermissionListener.onPermissionGranted()")

                        this@RealTimeActivity.selectImageFromLibrary()
                    }

                    override fun onPermissionDenied(response: PermissionDeniedResponse) {
                        DragonflyLogger.debug(LOG_TAG, "SelectImageFromLibraryPermissionListener.onPermissionDenied() - permanently? ${response.isPermanentlyDenied}")

                        if (response.isPermanentlyDenied) {
                            this@RealTimeActivity.showPermissionsRequiredAlert(R.string.permissions_required_title, R.string.permissions_required_description, false)
                        }
                    }

                    override fun onPermissionRationaleShouldBeShown(permission: PermissionRequest, token: PermissionToken) {
                        DragonflyLogger.debug(LOG_TAG, "SelectImageFromLibraryPermissionListener.onPermissionRationaleShouldBeShown()")

                        token.continuePermissionRequest()
                    }

                })
                .withErrorListener { error ->
                    DragonflyLogger.debug("tag", "Dexter error: $error")
                }
                .check()
    }

    override fun startRealTimeClassification() {
        dragonFlyLens.start()
    }

    override fun showPermissionsRequiredAlert(@StringRes title: Int, @StringRes message: Int, finishActivityOnCancel: Boolean) {
        missingPermissionsAlertDialog = AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setCancelable(false)
                .setNegativeButton(android.R.string.cancel, { dialog, _ ->
                    if (finishActivityOnCancel) {
                        finish()
                    } else {
                        dialog.dismiss()
                    }
                })
                .setPositiveButton(R.string.permissions_open_settings, { dialog, _ ->
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

    override fun onModelLoadFailure(e: DragonflyModelException) {
        if (e is DragonflyNoMemoryAvailableException || e.cause is DragonflyNoMemoryAvailableException) {
            showNoMemoryAvailableError()
        }
    }

    override fun onStartTakingSnapshot() {
        DragonflyLogger.debug(LOG_TAG, "onStartTakingSnapshot()")
    }

    override fun selectImageFromLibrary() {
        val intent = IntentHelper.selectImageFromLibrary()
        startActivityForResult(intent, REQUEST_CODE_SELECT_IMAGE)
    }

    override fun onSnapshotTaken(snapshot: DragonflyClassificationInput) {
        DragonflyLogger.debug(LOG_TAG, "onSnapshotTaken($snapshot)")

        // FIXME: Temporary code to be able to use the app without full refactor
        val fixMe = dragonFlyLens.lastClassifications.entries.first().value

        intent = FeedbackActivity.newIntent(this, model, snapshot, true, fixMe)
        startActivity(intent)
    }

    override fun onSnapshotError(e: DragonflySnapshotException) {
        DragonflyLogger.debug(LOG_TAG, "onSnapshotError($e)")
    }

    override fun onUriAnalysisFinished(uri: Uri, classificationInput: DragonflyClassificationInput, classifications: Map<String, List<Classifier.Classification>>) {
        uriUnderAnalysis = null
        lastAnalyzedUri = null

        DragonflyLogger.debug(LOG_TAG, "onUriAnalysisFinished($classifications)")

        // FIXME: Temporary code to be able to use the app without full refactor
        val fixMe = classifications.entries.first().value

        intent = FeedbackActivity.newIntent(this, model, classificationInput, false, fixMe)
        startActivity(intent)

        showActionButtons()
    }

    override fun onUriAnalysisFailed(e: DragonflyClassificationException) {
        uriUnderAnalysis = null

        DragonflyLogger.error(LOG_TAG, e)
        hideActionButtons()
    }

    private fun hideActionButtons() {
        btnSelectExistingPicture.visibility = GONE
    }

    private fun showActionButtons(animate: Boolean = false) {
        if (btnSelectExistingPicture.isVisible()) {
            return
        }

        if (animate) {
            val duration = DragonflyConfig.getRealTimeControlsVisibilityAnimationDuration()

            btnSelectExistingPicture.alpha = 0f
            btnSelectExistingPicture.makeVisible()
            btnSelectExistingPicture.animate()
                    .alpha(1.0f)
                    .setDuration(duration)
        } else {
            btnSelectExistingPicture.makeVisible()
        }
    }

    private fun showNoMemoryAvailableError() {
        dragonFlyLens.stop()

        noMemoryAvailableDialog = AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(R.string.project_selection_error_no_memory)
                .setCancelable(false)
                .setNegativeButton(R.string.project_selection_error_no_memory_negative_action, { dialog, _ ->
                    dialog.dismiss()
                    onBackPressed()
                })
                .setPositiveButton(R.string.project_selection_error_no_memory_positive_action, { dialog, _ ->
                    dialog.dismiss()

                    presenter.attachView(this@RealTimeActivity)

                    setupDragonflyLens()
                    lastAnalyzedUri?.let {
                        dragonFlyLens.analyzeFromUri(it)
                    }
                })
                .setIcon(R.mipmap.ic_launcher)
                .create()

        noMemoryAvailableDialog?.apply {
            setCanceledOnTouchOutside(false)
            show()
        }
    }

    companion object {
        private val LOG_TAG = RealTimeActivity::class.java.simpleName

        private val MODEL_BUNDLE = "${BuildConfig.APPLICATION_ID}.model_bundle"
        private val MODEL_NAME_BUNDLE = "${BuildConfig.APPLICATION_ID}.model_name_bundle"
        private val IMAGE_URI_BUNDLE = "${BuildConfig.APPLICATION_ID}.image_uri"
        private val LAST_ANALYZED_IMAGE_URI_BUNDLE = "${BuildConfig.APPLICATION_ID}.last_analyzed_image_uri"

        private val REQUEST_CODE_SELECT_IMAGE = 1

        fun create(context: Context, model: Model, modelName: String = ""): Intent {
            val intent = Intent(context, RealTimeActivity::class.java)
            intent.putExtra(MODEL_BUNDLE, model)
            intent.putExtra(MODEL_NAME_BUNDLE, modelName)
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
