package com.ciandt.dragonfly.example.features.feedback

import com.ciandt.dragonfly.data.model.Model
import com.ciandt.dragonfly.example.R
import com.ciandt.dragonfly.example.config.Tenant
import com.ciandt.dragonfly.example.features.feedback.model.Feedback
import com.ciandt.dragonfly.example.infrastructure.DragonflyLogger
import com.ciandt.dragonfly.example.infrastructure.extensions.clearAndAddAll
import com.ciandt.dragonfly.example.infrastructure.extensions.head
import com.ciandt.dragonfly.example.infrastructure.extensions.tail
import com.ciandt.dragonfly.example.shared.BasePresenter
import com.ciandt.dragonfly.lens.data.DragonflyClassificationInput
import com.ciandt.dragonfly.tensorflow.Classifier
import com.google.firebase.auth.FirebaseAuth

class FeedbackPresenter(val model: Model, val classificationInput: DragonflyClassificationInput, val feedbackInteractor: FeedbackContract.Interactor, val saveImageToGalleryInteractor: SaveImageToGalleryContract.Interactor, val firebaseAuth: FirebaseAuth) : BasePresenter<FeedbackContract.View>(), FeedbackContract.Presenter {
    private val results = ArrayList<Classifier.Classification>()
    private var userFeedback: Feedback? = null

    init {
        feedbackInteractor.setOnFeedbackSavedCallback { feedback ->
            DragonflyLogger.debug(LOG_TAG, "setUserFeedback(${feedback})")
        }

        saveImageToGalleryInteractor.setOnSaveImageSuccessCallback {
            view?.showSaveImageSuccessMessage(R.string.feedback_save_image_success)
        }

        saveImageToGalleryInteractor.setOnSaveImageErrorCallback {
            view?.showSaveImageSuccessMessage(R.string.feedback_save_image_error)
        }
    }

    override fun setClassifications(classifications: List<Classifier.Classification>) {

        results.clearAndAddAll(classifications)

        if (results.isEmpty()) {
            view?.showNoClassifications()
            return
        }

        if (userFeedback == null) {
            view?.showClassifications(results.head().title, results.tail())
        } else {
            userFeedback!!.let {
                if (it.isPositive()) {
                    view?.showPositiveClassification(it.actualLabel, results.tail(), false)
                } else {
                    view?.showNegativeClassification(it.actualLabel, results.tail())
                }
            }
        }
    }

    override fun markAsPositive() {
        view?.showPositiveClassification(results.head().title, results.tail())

        saveFeedback(results.head().title, Feedback.POSITIVE)
    }

    override fun markAsNegative() {
        view?.showNegativeForm(results.tail())
    }

    override fun submitNegative(label: String) {
        view?.showNegativeClassification(label, results.tail())

        saveFeedback(label, Feedback.NEGATIVE)
    }

    override fun setUserFeedback(userFeedback: Feedback?) {
        this.userFeedback = userFeedback
    }

    override fun saveImageToGallery(classificationInput: DragonflyClassificationInput) {
        saveImageToGalleryInteractor.save(classificationInput)
    }

    private fun saveFeedback(label: String, value: Int) {
        val identifiedLabels = HashMap<String, Float>()
        for (classification in results) {
            identifiedLabels.put(classification.title, classification.confidence)
        }

        val feedback = Feedback(
                tenant = Tenant.ID,
                project = model.id,
                userId = firebaseAuth.currentUser!!.uid,
                modelVersion = model.version,
                value = value,
                actualLabel = label,
                identifiedLabels = identifiedLabels,
                imageLocalPath = classificationInput.imagePath
        )

        view?.setUserFeedback(feedback)

        feedbackInteractor.saveFeedback(feedback)
    }

    companion object {
        private val LOG_TAG = FeedbackPresenter::class.java.simpleName
    }
}
