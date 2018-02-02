package com.ciandt.dragonfly.example.features.feedback

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Handler
import android.support.annotation.StringRes
import android.support.v4.content.ContextCompat
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import android.widget.Toast
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.ViewTarget
import com.bumptech.glide.request.transition.Transition
import com.ciandt.dragonfly.data.model.Model
import com.ciandt.dragonfly.example.BuildConfig
import com.ciandt.dragonfly.example.R
import com.ciandt.dragonfly.example.components.classifications.ClassificationsView
import com.ciandt.dragonfly.example.config.PermissionsMapping
import com.ciandt.dragonfly.example.data.DatabaseManager
import com.ciandt.dragonfly.example.data.PendingFeedbackRepository
import com.ciandt.dragonfly.example.features.feedback.model.BenchmarkResult
import com.ciandt.dragonfly.example.features.feedback.model.Feedback
import com.ciandt.dragonfly.example.infrastructure.extensions.getRootView
import com.ciandt.dragonfly.example.infrastructure.extensions.hideSoftInputView
import com.ciandt.dragonfly.example.infrastructure.extensions.showSnackbar
import com.ciandt.dragonfly.example.shared.BaseActivity
import com.ciandt.dragonfly.feedback.ui.DragonflyLensFeedbackView
import com.ciandt.dragonfly.lens.data.DragonflyClassificationInput
import com.ciandt.dragonfly.tensorflow.Classifier
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import kotlinx.android.synthetic.main.activity_feedback.*
import kotlinx.android.synthetic.main.partial_feedback_form.*
import kotlinx.android.synthetic.main.partial_feedback_result.*

class FeedbackActivity : BaseActivity(), FeedbackContract.View {

    private lateinit var presenter: FeedbackContract.Presenter
    private lateinit var classificationInput: DragonflyClassificationInput
    private lateinit var model: Model
    private var allowSaveToGallery: Boolean = false
    private var userFeedback: Feedback? = null

    private val classifications: LinkedHashMap<String, ArrayList<Classifier.Classification>> = LinkedHashMap()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_feedback)

        userFeedback = savedInstanceState?.getParcelable(USER_FEEDBACK)

        model = intent.extras.getParcelable(MODEL_BUNDLE)
        classificationInput = intent.extras.getParcelable(CLASSIFICATION_INPUT_BUNDLE)
        allowSaveToGallery = intent.extras.getBoolean(ALLOW_SAVE_TO_GALLERY_BUNDLE, false)

        val values = intent.extras.getSerializable(CLASSIFICATIONS_BUNDLE) as HashMap<String, ArrayList<Classifier.Classification>>
        model.outputNames.forEach { key ->
            values[key]?.let { value ->
                classifications.put(key, value)
            }
        }

        val feedbackSaverInteractor = FeedbackSaverInteractor(FirebaseDatabase.getInstance(), PendingFeedbackRepository(DatabaseManager.database))
        val saveImageToGalleryInteractor = SaveImageToGalleryInteractor(applicationContext)
        val benchmarkInteractor = BenchmarkInteractor()

        presenter = FeedbackPresenter(model, classificationInput, FirebaseAuth.getInstance().currentUser!!.uid,
                feedbackSaverInteractor,
                saveImageToGalleryInteractor,
                benchmarkInteractor)

        presenter.attachView(this)
        presenter.setUserFeedback(userFeedback)
        presenter.setClassifications(classifications)

        setupBackButton()
        setupBenchmarkButtons()
        setupSaveImageButton()
        setupResultsView()
        setupNegativeFeedbackView()

        dragonFlyLensFeedbackView.setClassificationInput(classificationInput)

        Glide
                .with(this)
                .load(R.drawable.dragonfly_lens_grid)
                .into(object : ViewTarget<DragonflyLensFeedbackView, Drawable>(dragonFlyLensFeedbackView) {
                    override fun onResourceReady(resource: Drawable?, anim: Transition<in Drawable>?) {
                        view.setOrnamentDrawable(resource)
                    }
                })
    }

    override fun showSaveImageSuccessMessage(@StringRes message: Int) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    override fun showSaveImageErrorMessage(@StringRes message: Int) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    override fun showBenchmarkLoading() {
        benchmarkButton.visibility = View.GONE
        benchmarkErrorState.visibility = View.GONE
        benchmarkLoading.visibility = View.VISIBLE
    }

    override fun showBenchmarkResult(result: BenchmarkResult) {
        benchmarkButton.visibility = View.GONE
        benchmarkLoading.visibility = View.GONE
        benchmarkErrorState.visibility = View.GONE

        result.benchmarks.forEach { (_, name, classifications) ->

            val chips = ArrayList<FeedbackChip>()
            classifications.mapTo(chips, { FeedbackChip(it) })

            val classificationsView = ClassificationsView(this)
            classificationsView.setTitle(name)
            classificationsView.setChips(chips)

            benchmarkContainer.addView(classificationsView)
        }
    }

    override fun showBenchmarkError(exception: Exception) {
        benchmarkButton.visibility = View.GONE
        benchmarkLoading.visibility = View.GONE
        benchmarkErrorState.visibility = View.VISIBLE
        benchmarkErrorMessage.text = getString(R.string.feedback_benchmark_error)
    }

    override fun showBenchmarkEmpty() {
        benchmarkButton.visibility = View.GONE
        benchmarkLoading.visibility = View.GONE
        benchmarkErrorState.visibility = View.VISIBLE
        benchmarkErrorMessage.text = getString(R.string.feedback_benchmark_empty)
    }

    private fun setupSaveImageButton() {
        btnSaveImage.visibility = if (allowSaveToGallery) View.VISIBLE else View.INVISIBLE
        btnSaveImage.setOnClickListener({
            Dexter
                    .withActivity(this)
                    .withPermissions(PermissionsMapping.SAVE_IMAGE_TO_GALLERY)
                    .withListener(object : MultiplePermissionsListener {
                        override fun onPermissionsChecked(report: MultiplePermissionsReport) {
                            presenter.saveImageToGallery()
                        }

                        override fun onPermissionRationaleShouldBeShown(permissions: MutableList<PermissionRequest>, token: PermissionToken) {
                            token.continuePermissionRequest()
                        }
                    })
                    .check()
        })
    }

    private fun setupBackButton() {
        btnBack.setOnClickListener({
            super.onBackPressed()
        })
    }

    private fun setupBenchmarkButtons() {
        benchmarkButton.setOnClickListener {
            presenter.benchmark()
        }

        benchmarkErrorRetry.setOnClickListener {
            presenter.benchmark()
        }
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

    private fun setupNegativeFeedbackView() {
        input.setText("")
        input.setOnEditorActionListener(TextView.OnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                getRootView().hideSoftInputView()
                return@OnEditorActionListener true
            }
            false
        })

        input.setOnTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if (s?.isBlank() ?: true) {
                    disableConfirm()
                } else {
                    enableConfirm()
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        cancelButton.setOnClickListener {
            hideNegativeForm()
        }

        confirmButton.setOnClickListener {
            val selected = formChipsViews.getSelectedItems().firstOrNull()
            if (selected != null && selected is FeedbackChip) {
                presenter.submitNegative(selected.classification.title)
            } else {
                presenter.submitNegative(input.getText())
            }
        }
    }

    override fun onResume() {
        super.onResume()
        presenter.apply {
            attachView(this@FeedbackActivity)
            setUserFeedback(userFeedback)
            setClassifications(classifications)
        }
    }

    override fun onPause() {
        super.onPause()
        presenter.detachView()
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)

        outState?.apply {
            putParcelable(USER_FEEDBACK, userFeedback)
        }
    }

    override fun showMainClassifications(labels: List<android.support.v4.util.Pair<String, Int>>) {
        dragonFlyLensFeedbackView.setLabels(labels)
    }

    override fun showNoClassifications() {
        showSnackbar(R.string.feedback_no_classifications_found)
    }

    override fun showClassifications(classifications: Map<String, List<Classifier.Classification>>) {

        otherPredictionsContainer.removeAllViews()
        classifications.entries.forEach { entry ->

            val chips = ArrayList<FeedbackChip>()
            entry.value.mapTo(chips, { FeedbackChip(it) })

            val classificationsView = ClassificationsView(this)
            classificationsView.setTitle(entry.key)
            classificationsView.setChips(chips)
            classificationsView.select(chips.first())

            otherPredictionsContainer.addView(classificationsView)
        }

        showFeedbackView()
    }

    override fun showPositiveClassification(mainClassificationLabel: String, otherClassifications: List<Classifier.Classification>, collapseResults: Boolean) {
        positiveButton.isEnabled = false
        positiveButton.isActivated = true
        negativeButton.isActivated = false

        result.text = mainClassificationLabel
        result.setTextColor(ContextCompat.getColor(this, R.color.feedback_submitted))

        if (collapseResults) {
            collapseResults()
        }
    }

    override fun showNegativeClassification(mainClassificationLabel: String, otherClassifications: List<Classifier.Classification>) {
        hideNegativeForm()
        showUnderRevision()

        result.text = mainClassificationLabel
        result.setTextColor(ContextCompat.getColor(this, R.color.feedback_submitted))
    }

    override fun showNegativeForm(otherClassifications: List<Classifier.Classification>) {

        val chips = ArrayList<FeedbackChip>()
        otherClassifications.forEach {
            chips.add(FeedbackChip(it))
        }

        if (chips.isEmpty()) {

            formChipsLabel.visibility = View.GONE
            formChipsViews.visibility = View.GONE
            input.setHint(getString(R.string.feedback_form_hint))

        } else {

            formChipsViews.setChips(chips)

            formChipsViews.setSelectCallback { _ ->
                disableInput()
                enableConfirm()
            }

            formChipsViews.setDeselectCallback { _ ->
                enableInput()
                disableConfirm()
            }
        }

        feedbackView.visibility = View.GONE
        feedbackFormView.visibility = View.VISIBLE
    }

    override fun setUserFeedback(feedback: Feedback) {
        userFeedback = feedback
    }

    private fun showUnderRevision() {
        positiveButton.visibility = View.GONE
        negativeButton.visibility = View.GONE
        underRevision.visibility = View.VISIBLE
    }

    private fun enableInput() {
        input.isEnabled = true
        input.alpha = 1.0f
    }

    private fun disableInput() {
        input.isEnabled = false
        input.alpha = 0.4f
    }

    private fun enableConfirm() {
        confirmButton.isEnabled = true
    }

    private fun disableConfirm() {
        confirmButton.isEnabled = false
    }

    private fun hideNegativeForm() {
        getRootView().hideSoftInputView()

        // wait for the keyboard to disappear to show the view, otherwise a flicking occurs.
        Handler().postDelayed({
            feedbackFormView.visibility = View.GONE
            feedbackView.visibility = View.VISIBLE
        }, 100)
    }

    private fun expandResults() {

        dragonFlyLensFeedbackView.hideLabels(HIDE_SHOW_ANIMATION_DURATION)

        toggleButton.isClickable = false
        toggleButton.animate()
                .alpha(0.0f)
                .setDuration(HIDE_SHOW_ANIMATION_DURATION)
                .setListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator?) {
                        super.onAnimationEnd(animation)

                        toggleButton.apply {
                            text = getString(R.string.feedback_close)
                            setTextColor(ContextCompat.getColor(this@FeedbackActivity, R.color.feedback_close))
                            animate()
                                    .alpha(1.0f)
                                    .setDuration(HIDE_SHOW_ANIMATION_DURATION)
                                    .setListener(object : AnimatorListenerAdapter() {
                                        override fun onAnimationEnd(animation: Animator?) {
                                            super.onAnimationEnd(animation)
                                            toggleButton.isClickable = true
                                        }
                                    })
                        }
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

                        toggleButton.apply {
                            text = getString(R.string.feedback_more_info)
                            setTextColor(ContextCompat.getColor(this@FeedbackActivity, R.color.feedback_more_info))
                            animate()
                                    .alpha(1.0f)
                                    .setDuration(HIDE_SHOW_ANIMATION_DURATION)
                                    .setListener(object : AnimatorListenerAdapter() {
                                        override fun onAnimationEnd(animation: Animator?) {
                                            super.onAnimationEnd(animation)

                                            toggleButton.isClickable = true

                                            dragonFlyLensFeedbackView.showLabels(HIDE_SHOW_ANIMATION_DURATION)
                                        }
                                    })
                        }

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

    companion object {
        private val HIDE_SHOW_ANIMATION_DURATION = 150L
        private val FIRST_APPEAR_ANIMATION_DURATION = 1000L

        private val MODEL_BUNDLE = String.format("%s.model", BuildConfig.APPLICATION_ID)
        private val CLASSIFICATION_INPUT_BUNDLE = String.format("%s.classification_input", BuildConfig.APPLICATION_ID)
        private val ALLOW_SAVE_TO_GALLERY_BUNDLE = String.format("%s.allow_save_to_gallery", BuildConfig.APPLICATION_ID)
        private val CLASSIFICATIONS_BUNDLE = String.format("%s.classifications", BuildConfig.APPLICATION_ID)
        private val USER_FEEDBACK = String.format("%s.user_feedback", BuildConfig.APPLICATION_ID)

        fun newIntent(context: Context, model: Model, classificationInput: DragonflyClassificationInput, allowSavingToGallery: Boolean, classifications: Map<String, List<Classifier.Classification>>): Intent {
            val intent = Intent(context, FeedbackActivity::class.java)
            intent.putExtra(MODEL_BUNDLE, model)
            intent.putExtra(CLASSIFICATION_INPUT_BUNDLE, classificationInput)
            intent.putExtra(ALLOW_SAVE_TO_GALLERY_BUNDLE, allowSavingToGallery)
            (classifications as? HashMap<*, *>)?.let {
                intent.putExtra(CLASSIFICATIONS_BUNDLE, it)
            }

            return intent
        }
    }
}

