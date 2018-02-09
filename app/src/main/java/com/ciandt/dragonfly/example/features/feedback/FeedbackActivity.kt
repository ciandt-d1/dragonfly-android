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
import com.ciandt.dragonfly.example.components.chips.Chip
import com.ciandt.dragonfly.example.components.classifications.ClassificationsView
import com.ciandt.dragonfly.example.config.Benchmark
import com.ciandt.dragonfly.example.config.PermissionsMapping
import com.ciandt.dragonfly.example.data.DatabaseManager
import com.ciandt.dragonfly.example.data.PendingFeedbackRepository
import com.ciandt.dragonfly.example.features.feedback.model.BenchmarkResult
import com.ciandt.dragonfly.example.features.feedback.model.Feedback
import com.ciandt.dragonfly.example.infrastructure.extensions.clearAndAddAll
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
    private val userFeedbackList = ArrayList<Feedback>()

    private val classifications: LinkedHashMap<String, ArrayList<Classifier.Classification>> = LinkedHashMap()

    private val initialClassifications: LinkedHashMap<Int, Chip> = LinkedHashMap()
    private val currentClassifications: LinkedHashMap<Int, Chip> = LinkedHashMap()
    private val negativeClassifications: ArrayList<String> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_feedback)

        val feedbackList: ArrayList<Feedback>? = savedInstanceState?.getParcelableArrayList(USER_FEEDBACK_LIST)
        feedbackList?.let {
            userFeedbackList.clearAndAddAll(it)
        }

        model = intent.extras.getParcelable(MODEL_BUNDLE)
        classificationInput = intent.extras.getParcelable(CLASSIFICATION_INPUT_BUNDLE)
        allowSaveToGallery = intent.extras.getBoolean(ALLOW_SAVE_TO_GALLERY_BUNDLE, false)

        val values = intent.extras.getSerializable(CLASSIFICATIONS_BUNDLE) as HashMap<String, ArrayList<Classifier.Classification>>
        model.outputNames.forEach { key ->
            values[key]?.let { value ->
                classifications.put(key, value)
                initialClassifications.put(model.outputNames.indexOf(key), FeedbackChip(value.first()))
            }
        }

        val feedbackSaverInteractor = FeedbackSaverInteractor(FirebaseDatabase.getInstance(), PendingFeedbackRepository(DatabaseManager.database))
        val saveImageToGalleryInteractor = SaveImageToGalleryInteractor(applicationContext)
        val benchmarkInteractor = BenchmarkInteractor()

        presenter = FeedbackPresenter(model, classificationInput, FirebaseAuth.getInstance().currentUser!!.uid,
                feedbackSaverInteractor,
                saveImageToGalleryInteractor,
                benchmarkInteractor)

        presenter.apply {
            attachView(this@FeedbackActivity)
            setUserFeedbackList(userFeedbackList)
            setClassifications(classifications)
        }

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
        val showBenchmark = if (model.others.contains(Benchmark.SHOW_BENCHMARK)) {
            model.others[Benchmark.SHOW_BENCHMARK] as Boolean
        } else {
            false
        }
        benchmarkContainer.visibility = if (showBenchmark) View.VISIBLE else View.GONE

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
        negativeFormCancelButton.setOnClickListener {
            hideNegativeForm(true)
        }

        negativeFormConfirmButton.setOnClickListener {
            hideNegativeForm(false)
            val selectedItems = ArrayList<String>()
            currentClassifications.forEach { (_, chip) ->
                if (chip is FeedbackChip) {
                    selectedItems.add(chip.classification.title)
                }
            }
            presenter.submitNegative(selectedItems)
            collapseResults()
        }

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
            hideNegativeFormInput()
        }
    }

    override fun onResume() {
        super.onResume()
        presenter.apply {
            attachView(this@FeedbackActivity)
            setUserFeedbackList(userFeedbackList)
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
            putParcelableArrayList(USER_FEEDBACK_LIST, userFeedbackList)
        }
    }

    override fun showMainClassifications(labels: List<android.support.v4.util.Pair<String, Int>>) {
        result.text = resources.getQuantityString(R.plurals.feedback_classification, labels.size)
        dragonFlyLensFeedbackView.setLabels(labels)
    }

    override fun showNoClassifications() {
        showSnackbar(R.string.feedback_no_classifications_found)
    }

    override fun showClassifications(classifications: Map<String, List<Classifier.Classification>>) {

        otherPredictionsContainer.removeAllViews()
        classifications.entries.forEach { entry ->

            val index = classifications.entries.indexOf(entry)

            val chips = ArrayList<FeedbackChip>()
            entry.value.mapTo(chips, { FeedbackChip(it) })

            val classificationsView = ClassificationsView(this)
            classificationsView.tag = index
            classificationsView.setTitle(entry.key)
            classificationsView.setChips(chips)

            var toSelect: FeedbackChip = chips.first()
            chips.forEach {
                val isSelected = negativeClassifications.size == classifications.size && it.classification.title == negativeClassifications[index]
                if (isSelected) {
                    toSelect = it
                }
            }
            classificationsView.select(toSelect)

            otherPredictionsContainer.addView(classificationsView)

            currentClassifications.put(index, toSelect)
        }

        showFeedbackView()
    }

    override fun showPositiveClassification(collapseResults: Boolean) {
        positiveButton.isEnabled = false
        positiveButton.isActivated = true
        negativeButton.isActivated = false

        if (collapseResults) {
            collapseResults()
        }
    }

    override fun showNegativeClassification(classifications: List<String>) {
        negativeClassifications.clearAndAddAll(classifications)
        hideNegativeFormInput()
        showUnderRevision()
    }

    override fun showNegativeForm() {
        positiveButton.visibility = View.GONE
        negativeButton.isEnabled = false

        (0 until otherPredictionsContainer.childCount)
                .map { otherPredictionsContainer.getChildAt(it) as? ClassificationsView }
                .forEach { classificationView ->
                    if (classificationView == null) {
                        return@forEach
                    }

                    classificationView.setSelectable(true)

                    val index = classificationView.tag as Int

                    if (!model.closedSet[index].toBoolean()) {
                        classificationView.addChip(classificationView.getChips().size, FeedbackChip.createOther(OTHER_TITLE))
                    }

                    classificationView.setSelectCallback {

                        if ((it as FeedbackChip).isOther()) {
                            showNegativeFormInput(index, it, classificationView)
                        }

                        currentClassifications.put(index, it)
                        if (currentClassifications != initialClassifications && currentClassifications.size == initialClassifications.size) {
                            enableNegativeFormConfirm()
                        } else {
                            disableNegativeFormConfirm()
                        }
                    }

                    classificationView.setDeselectCallback {
                        currentClassifications.remove(classificationView.tag as Int)
                        disableNegativeFormConfirm()
                    }
                }

        toggleContainer.visibility = View.GONE
        negativeFormButtonsContainer.visibility = View.VISIBLE

        expandResults()
    }

    private fun enableNegativeFormConfirm() {
        negativeFormConfirmButton.isEnabled = true
    }

    private fun disableNegativeFormConfirm() {
        negativeFormConfirmButton.isEnabled = false
    }

    private fun hideNegativeForm(reset: Boolean = true) {
        positiveButton.visibility = View.VISIBLE
        negativeButton.isEnabled = true

        (0 until otherPredictionsContainer.childCount)
                .map { otherPredictionsContainer.getChildAt(it) as? ClassificationsView }
                .forEach {
                    it?.setSelectable(false)
                    if (reset) {
                        it?.deselectAll()
                        it?.select(0)

                        if (!model.closedSet[it?.tag as Int].toBoolean()) {
                            it.removeChip(it.getChips().size - 1)
                        }

                        currentClassifications.put(it.tag as Int, it.getChips().first())
                    }
                }

        disableNegativeFormConfirm()
        negativeFormButtonsContainer.visibility = View.GONE
        toggleContainer.visibility = View.VISIBLE
    }

    override fun setUserFeedbackList(feedbackList: List<Feedback>) {
        userFeedbackList.clearAndAddAll(feedbackList)
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

    private fun showNegativeFormInput(index: Int, chip: FeedbackChip, view: ClassificationsView) {
        input.setText(if (chip.getText() != OTHER_TITLE) {
            chip.getText()
        } else {
            ""
        })
        feedbackView.visibility = View.GONE
        feedbackFormView.visibility = View.VISIBLE

        confirmButton.setOnClickListener {
            val newChip = FeedbackChip.createOther(input.getText())
            view.removeChip(chip)
            view.addChip(view.getChips().size, newChip)

            currentClassifications.put(index, newChip)
            hideNegativeFormInput()
        }
    }

    private fun hideNegativeFormInput() {
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
        private val USER_FEEDBACK_LIST = String.format("%s.user_feedback_list", BuildConfig.APPLICATION_ID)

        private val OTHER_TITLE = "other"
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

