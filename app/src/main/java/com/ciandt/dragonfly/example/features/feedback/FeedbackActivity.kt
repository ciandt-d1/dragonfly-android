package com.ciandt.dragonfly.example.features.feedback

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.support.annotation.StringRes
import android.support.design.widget.Snackbar
import android.support.v4.content.ContextCompat
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import android.widget.Toast
import com.ciandt.dragonfly.data.model.Model
import com.ciandt.dragonfly.example.BuildConfig
import com.ciandt.dragonfly.example.R
import com.ciandt.dragonfly.example.features.feedback.model.Feedback
import com.ciandt.dragonfly.example.infrastructure.extensions.hideSoftInputView
import com.ciandt.dragonfly.example.shared.BaseActivity
import com.ciandt.dragonfly.infrastructure.PermissionsMapping
import com.ciandt.dragonfly.lens.data.DragonflyCameraSnapshot
import com.ciandt.dragonfly.tensorflow.Classifier
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import kotlinx.android.synthetic.main.activity_feedback.*
import kotlinx.android.synthetic.main.partial_feedback_form.*
import kotlinx.android.synthetic.main.partial_feedback_result.*


class FeedbackActivity : BaseActivity(), FeedbackContract.View {
    private val HIDE_SHOW_ANIMATION_DURATION = 150L
    private val FIRST_APPEAR_ANIMATION_DURATION = 1000L

    private lateinit var presenter: FeedbackContract.Presenter
    private lateinit var cameraSnapshot: DragonflyCameraSnapshot
    private lateinit var model: Model
    private var userFeedback: Feedback? = null

    private lateinit var classifications: ArrayList<Classifier.Recognition>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_feedback)

        if (savedInstanceState != null) {
            model = savedInstanceState.getParcelable(MODEL_BUNDLE)
            cameraSnapshot = savedInstanceState.getParcelable(SNAPSHOT_BUNDLE)
            classifications = savedInstanceState.getParcelableArrayList(CLASSIFICATIONS_BUNDLE)
            userFeedback = savedInstanceState.getParcelable<Feedback>(USER_FEEDBACK)
        } else {
            cameraSnapshot = intent.extras.getParcelable<DragonflyCameraSnapshot>(SNAPSHOT_BUNDLE)
            model = intent.extras.getParcelable<Model>(MODEL_BUNDLE)
            classifications = intent.extras.getParcelableArrayList(CLASSIFICATIONS_BUNDLE)
            userFeedback = null
        }

        val feedbackInteractor = FeedbackInteractor(FirebaseStorage.getInstance(), FirebaseDatabase.getInstance())
        val saveImageToGalleryInteractor = SaveImageToGalleryInteractor(applicationContext)

        presenter = FeedbackPresenter(model, cameraSnapshot, feedbackInteractor, saveImageToGalleryInteractor, FirebaseAuth.getInstance())
        presenter.attachView(this)
        presenter.setClassifications(classifications)
        presenter.setUserFeedback(userFeedback)

        setupBackButton()
        setupSaveImageButton()
        setupResultsView()
        setupNegativeFeedbackView()

        dragonFlyLensFeedbackView.setSnapshot(cameraSnapshot)
    }

    override fun showSaveImageSuccessMessage(@StringRes message: Int) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    override fun showSaveImageErrorMessage(@StringRes message: Int) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun setupSaveImageButton() {
        btnSaveImage.setOnClickListener({
            Dexter
                    .withActivity(this)
                    .withPermissions(PermissionsMapping.SAVE_IMAGE_TO_GALLERY)
                    .withListener(object : MultiplePermissionsListener {
                        override fun onPermissionsChecked(report: MultiplePermissionsReport) {
                            presenter.saveImageToGallery(cameraSnapshot)
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
                presenter.submitNegative(selected.recognition.title)
            } else {
                presenter.submitNegative(input.getText())
            }
        }
    }

    override fun onResume() {
        super.onResume()
        presenter.apply {
            attachView(this@FeedbackActivity)
            setClassifications(classifications)
            setUserFeedback(userFeedback)
        }
    }

    override fun onPause() {
        super.onPause()
        presenter.detachView()
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)

        outState?.apply {
            putParcelable(MODEL_BUNDLE, model)
            putParcelable(SNAPSHOT_BUNDLE, cameraSnapshot)
            putParcelable(USER_FEEDBACK, userFeedback)
            putParcelableArrayList(CLASSIFICATIONS_BUNDLE, classifications)
        }
    }

    override fun showNoRecognitions() {
        Snackbar.make(getRootView(), "No recognitions found.", Snackbar.LENGTH_LONG).show()
    }

    override fun showRecognitions(mainRecognitionLabel: String, otherRecognitions: List<Classifier.Recognition>) {
        result.text = getString(R.string.feedback_classification, mainRecognitionLabel)

        showOtherRecognitions(otherRecognitions)
        showFeedbackView()
    }

    private fun showOtherRecognitions(otherRecognitions: List<Classifier.Recognition>) {
        if (otherRecognitions.isEmpty()) {
            footer.visibility = View.GONE
        } else {
            val chips = ArrayList<FeedbackChip>()
            otherRecognitions.forEach {
                chips.add(FeedbackChip(it))
            }

            chipsViews.setChips(chips)
        }
    }

    override fun showPositiveRecognition(mainRecognitionLabel: String, otherRecognitions: List<Classifier.Recognition>, collapseResults: Boolean) {
        positiveButton.isEnabled = false
        positiveButton.isActivated = true
        negativeButton.isActivated = false

        result.text = mainRecognitionLabel
        result.setTextColor(ContextCompat.getColor(this, R.color.feedback_submitted))

        if (collapseResults) {
            collapseResults()
        }
    }

    override fun showNegativeRecognition(mainRecognitionLabel: String, otherRecognitions: List<Classifier.Recognition>) {
        showOtherRecognitions(otherRecognitions)
        hideNegativeForm()
        showUnderRevision()

        result.text = mainRecognitionLabel
        result.setTextColor(ContextCompat.getColor(this, R.color.feedback_submitted))
    }

    override fun showNegativeForm(otherRecognitions: List<Classifier.Recognition>) {

        val chips = ArrayList<FeedbackChip>()
        otherRecognitions.forEach {
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
        footer.visibility = View.VISIBLE
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
        private val LOG_TAG = FeedbackActivity::class.java.simpleName

        private val MODEL_BUNDLE = String.format("%s.model_bundle", BuildConfig.APPLICATION_ID)
        private val SNAPSHOT_BUNDLE = String.format("%s.snapshot_bundle", BuildConfig.APPLICATION_ID)
        private val CLASSIFICATIONS_BUNDLE = String.format("%s.classifications", BuildConfig.APPLICATION_ID)
        private val USER_FEEDBACK = String.format("%s.user_feedback", BuildConfig.APPLICATION_ID)

        fun newIntent(context: Context, model: Model, snapshot: DragonflyCameraSnapshot, classifications: List<Classifier.Recognition>): Intent {
            val intent = Intent(context, FeedbackActivity::class.java)
            intent.putExtra(MODEL_BUNDLE, model)
            intent.putExtra(SNAPSHOT_BUNDLE, snapshot)
            intent.putParcelableArrayListExtra(CLASSIFICATIONS_BUNDLE, ArrayList<Classifier.Recognition>(classifications))

            return intent
        }
    }
}

