package com.ciandt.dragonfly.example.features.feedback

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.PersistableBundle
import android.support.design.widget.Snackbar
import android.support.v4.content.ContextCompat
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import android.widget.Toast
import com.ciandt.dragonfly.data.model.Model
import com.ciandt.dragonfly.example.BuildConfig
import com.ciandt.dragonfly.example.R
import com.ciandt.dragonfly.example.infrastructure.DragonflyLogger
import com.ciandt.dragonfly.example.infrastructure.extensions.hideSoftInputView
import com.ciandt.dragonfly.example.shared.BaseActivity
import com.ciandt.dragonfly.lens.data.DragonflyCameraSnapshot
import com.ciandt.dragonfly.lens.exception.DragonflyModelException
import com.ciandt.dragonfly.lens.exception.DragonflyRecognitionException
import com.ciandt.dragonfly.lens.ui.DragonflyLensFeedbackView
import com.ciandt.dragonfly.tensorflow.Classifier
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_feedback.*
import kotlinx.android.synthetic.main.partial_feedback_form.*
import kotlinx.android.synthetic.main.partial_feedback_result.*


class FeedbackActivity : BaseActivity(), FeedbackContract.View {

    private val HIDE_SHOW_ANIMATION_DURATION = 150L
    private val FIRST_APPEAR_ANIMATION_DURATION = 1000L

    private lateinit var presenter: FeedbackContract.Presenter
    private lateinit var cameraSnapshot: DragonflyCameraSnapshot
    private lateinit var model: Model

    private var lastRecognizedObjects: ArrayList<Classifier.Recognition>? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_feedback)

        if (savedInstanceState != null) {
            model = savedInstanceState.getParcelable(MODEL_BUNDLE)
            cameraSnapshot = savedInstanceState.getParcelable(SNAPSHOT_BUNDLE)
            lastRecognizedObjects = savedInstanceState.getParcelableArrayList(RECOGNITIONS_BUNDLE)
        } else {
            cameraSnapshot = intent.extras.getParcelable<DragonflyCameraSnapshot>(SNAPSHOT_BUNDLE)
            model = intent.extras.getParcelable<Model>(MODEL_BUNDLE)
            lastRecognizedObjects = null
        }

        val interactor = FeedbackInteractor(FirebaseStorage.getInstance(), FirebaseDatabase.getInstance())
        presenter = FeedbackPresenter(model, cameraSnapshot, interactor, FirebaseAuth.getInstance())
        presenter.attachView(this)

        setupBackButton()
        setupSaveImageButton()

        if (!hasRecognizedObjectsCached()) {
            setupModelCallbacks()
            setupBitmapAnalysisCallbacks()
        }

        setupResultsView()

        dragonFlyLensFeedbackView.setSnapshot(cameraSnapshot)
    }

    private fun setupSaveImageButton() {
        btnSaveImage.setOnClickListener({
            Toast.makeText(this, "Save image", Toast.LENGTH_SHORT).show()
        })
    }

    private fun setupBackButton() {
        btnBack.setOnClickListener({
            super.onBackPressed()
        })
    }

    private fun setupModelCallbacks() {
        dragonFlyLensFeedbackView.setModelCallbacks(object : DragonflyLensFeedbackView.ModelCallbacks {
            override fun onStartedLoadingModel(model: Model?) {
                DragonflyLogger.debug(LOG_TAG, "DragonflyLensFeedbackView.ModelCallbacks.onStartedLoadingModel(${model})")
            }

            override fun onModelReady(model: Model?) {
                DragonflyLogger.debug(LOG_TAG, "DragonflyLensFeedbackView.ModelCallbacks.onModelReady(${model})")

                dragonFlyLensFeedbackView.analyzeSnapshot()
            }

            override fun onModelFailure(e: DragonflyModelException?) {
                DragonflyLogger.debug(LOG_TAG, "DragonflyLensFeedbackView.ModelCallbacks.onModelFailure(${e})")
            }
        })
    }

    private fun setupBitmapAnalysisCallbacks() {
        dragonFlyLensFeedbackView.setSnapshotAnalysisCallbacks(object : DragonflyLensFeedbackView.SnapshotAnalysisCallbacks {
            override fun onSnapshotAnalyzed(results: MutableList<Classifier.Recognition>) {
                DragonflyLogger.debug(LOG_TAG, "DragonflyLensFeedbackView.SnapshotAnalysisCallbacks.onSnapshotAnalyzed(${results})")

                lastRecognizedObjects = ArrayList(results)
                presenter.setRecognitions(results)
            }

            override fun onSnapshotAnalysisFailed(e: DragonflyRecognitionException?) {
                DragonflyLogger.debug(LOG_TAG, "DragonflyLensFeedbackView.SnapshotAnalysisCallbacks.onSnapshotAnalysisFailed(${e})")

                lastRecognizedObjects = null
            }
        })
    }

    private fun setupResultsView() {

        val duration = HIDE_SHOW_ANIMATION_DURATION * 2
        feedbackContainer.layoutTransition?.setDuration(duration)
        feedbackView.layoutTransition?.setDuration(duration)
        feedbackFormView.layoutTransition?.setDuration(duration)
        feedbackResultContainer.layoutTransition?.setDuration(duration)
        footer.layoutTransition?.setDuration(duration)

        toggleButton.setOnClickListener {
            if (chipsContainer.visibility == View.GONE) {
                expandResults()
            } else {
                collapseResults()
            }
        }

        positiveButton.setOnClickListener {
            presenter.markAsPositive()
        }

        negativeButton.setOnClickListener {
            presenter.markAsNegative()
        }
    }

    override fun onResume() {
        super.onResume()
        presenter.attachView(this)

        if (hasRecognizedObjectsCached()) {
            presenter.setRecognitions(lastRecognizedObjects!!)
        } else {
            dragonFlyLensFeedbackView.start(model)
        }
    }

    override fun onPause() {
        super.onPause()
        presenter.detachView()

        dragonFlyLensFeedbackView.stop()
    }

    override fun onSaveInstanceState(outState: Bundle?, outPersistentState: PersistableBundle?) {
        super.onSaveInstanceState(outState, outPersistentState)

        outState?.putParcelable(MODEL_BUNDLE, model)
        outState?.putParcelable(SNAPSHOT_BUNDLE, cameraSnapshot)

        if (lastRecognizedObjects != null) {
            outState?.putParcelableArrayList(RECOGNITIONS_BUNDLE, lastRecognizedObjects)
        }
    }

    override fun showNoRecognitions() {
        Snackbar.make(getRootView(), "No recognitions found.", Snackbar.LENGTH_LONG).show()
    }

    override fun showRecognitions(main: Classifier.Recognition, others: List<Classifier.Recognition>) {

        result.text = getString(R.string.feedback_classification, main.title)

        if (others.isEmpty()) {
            footer.visibility = View.GONE

        } else {

            val chips = ArrayList<FeedbackChip>()
            others.forEach {
                chips.add(FeedbackChip(it))
            }

            chipsViews.setChips(chips)
        }

        showFeedbackView()
    }

    override fun showPositiveRecognition(recognition: Classifier.Recognition) {

        positiveButton.isActivated = true
        negativeButton.isActivated = false

        result.text = recognition.title
        result.setTextColor(ContextCompat.getColor(this, R.color.feedback_submitted))

        collapseResults()
    }

    override fun showNegativeRecognition(recognition: Classifier.Recognition) {

        positiveButton.visibility = View.GONE
        negativeButton.visibility = View.GONE
        underRevision.visibility = View.VISIBLE

        result.text = recognition.title
        result.setTextColor(ContextCompat.getColor(this, R.color.feedback_submitted))

        footer.visibility = View.GONE
    }

    override fun showNegativeForm(others: List<Classifier.Recognition>) {

        if (others.isEmpty()) {
            formChipsLabel.visibility = View.GONE
            formChipsViews.visibility = View.GONE
            input.setHint(getString(R.string.feedback_form_hint))

        } else {

            val chips = ArrayList<FeedbackChip>()
            others.forEach {
                chips.add(FeedbackChip(it))
            }

            formChipsViews.setChips(chips)
        }

        input.setText("")
        input.setOnEditorActionListener(TextView.OnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                currentFocus.hideSoftInputView()
                return@OnEditorActionListener true
            }
            false
        })

        cancelButton.setOnClickListener {
            hideNegativeForm()
        }

        confirmButton.setOnClickListener {
            var actualLabel = input.getText()
            formChipsViews.getSelected()?.let {
                actualLabel = (it as FeedbackChip).recognition.title
            }

            presenter.saveNegativeFeedback(actualLabel)
            hideNegativeForm()
        }

        feedbackView.visibility = View.GONE
        feedbackFormView.visibility = View.VISIBLE
    }

    fun hideNegativeForm() {
        currentFocus.hideSoftInputView()
        feedbackFormView.visibility = View.GONE
        feedbackView.visibility = View.VISIBLE
    }

    private fun expandResults() {

        toggleButton.isClickable = false
        toggleButton.animate()
                .alpha(0.0f)
                .setDuration(HIDE_SHOW_ANIMATION_DURATION)
                .setListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator?) {
                        super.onAnimationEnd(animation)
                        toggleButton.text = getString(R.string.feedback_close)
                        toggleButton.setTextColor(ContextCompat.getColor(this@FeedbackActivity, R.color.feedback_close))
                        toggleButton.animate()
                                .alpha(1.0f)
                                .setDuration(HIDE_SHOW_ANIMATION_DURATION)
                                .setListener(object : AnimatorListenerAdapter() {
                                    override fun onAnimationEnd(animation: Animator?) {
                                        super.onAnimationEnd(animation)
                                        toggleButton.isClickable = true
                                    }
                                })
                    }
                })

        arrow.animate()
                .alpha(0.0f)
                .setDuration(HIDE_SHOW_ANIMATION_DURATION)
                .setListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator?) {
                        super.onAnimationEnd(animation)
                        arrow.setImageDrawable(ContextCompat.getDrawable(this@FeedbackActivity, R.drawable.ic_arrow_down))
                        arrow.animate()
                                .alpha(1.0f)
                                .setDuration(HIDE_SHOW_ANIMATION_DURATION)
                                .setListener(null)

                    }
                })

        chipsContainer.visibility = View.VISIBLE
    }

    private fun collapseResults() {

        toggleButton.isClickable = false
        toggleButton.animate()
                .alpha(0.0f)
                .setDuration(HIDE_SHOW_ANIMATION_DURATION)
                .setListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator?) {
                        super.onAnimationEnd(animation)
                        toggleButton.text = getString(R.string.feedback_more_info)
                        toggleButton.setTextColor(ContextCompat.getColor(this@FeedbackActivity, R.color.feedback_more_info))
                        toggleButton.animate()
                                .alpha(1.0f)
                                .setDuration(HIDE_SHOW_ANIMATION_DURATION)
                                .setListener(object : AnimatorListenerAdapter() {
                                    override fun onAnimationEnd(animation: Animator?) {
                                        super.onAnimationEnd(animation)

                                        toggleButton.isClickable = true
                                    }
                                })

                    }
                })

        arrow.animate()
                .alpha(0.0f)
                .setDuration(HIDE_SHOW_ANIMATION_DURATION)
                .setListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator?) {
                        super.onAnimationEnd(animation)
                        arrow.setImageDrawable(ContextCompat.getDrawable(this@FeedbackActivity, R.drawable.ic_arrow_up))
                        arrow.animate()
                                .alpha(1.0f)
                                .setDuration(HIDE_SHOW_ANIMATION_DURATION)
                                .setListener(null)
                    }
                })

        chipsContainer.visibility = View.GONE
    }

    private fun showFeedbackView() {

        if (feedbackView.alpha > 0) {
            return
        }

        feedbackView.isClickable = false
        feedbackView.animate()
                .alpha(1.0f)
                .setDuration(FIRST_APPEAR_ANIMATION_DURATION)
                .setListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator?) {
                        super.onAnimationEnd(animation)
                        feedbackView.isClickable = true
                    }
                })
    }

    private fun hasRecognizedObjectsCached() = lastRecognizedObjects != null

    companion object {
        private val LOG_TAG = FeedbackActivity::class.java.simpleName

        private val MODEL_BUNDLE = String.format("%s.model_bundle", BuildConfig.APPLICATION_ID)
        private val SNAPSHOT_BUNDLE = String.format("%s.snapshot_bundle", BuildConfig.APPLICATION_ID)
        private val RECOGNITIONS_BUNDLE = String.format("%s.recognitions", BuildConfig.APPLICATION_ID)

        fun newIntent(context: Context, model: Model, snapshot: DragonflyCameraSnapshot): Intent {
            val intent = Intent(context, FeedbackActivity::class.java)
            intent.putExtra(MODEL_BUNDLE, model)
            intent.putExtra(SNAPSHOT_BUNDLE, snapshot)

            return intent
        }
    }
}

