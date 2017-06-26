package com.ciandt.dragonfly.example.features.feedback

import com.ciandt.dragonfly.data.model.Model
import com.ciandt.dragonfly.example.config.Tenant
import com.ciandt.dragonfly.example.features.feedback.model.Feedback
import com.ciandt.dragonfly.example.infrastructure.DragonflyLogger
import com.ciandt.dragonfly.example.infrastructure.extensions.clearAndAddAll
import com.ciandt.dragonfly.example.infrastructure.extensions.head
import com.ciandt.dragonfly.example.infrastructure.extensions.tail
import com.ciandt.dragonfly.example.shared.BasePresenter
import com.ciandt.dragonfly.lens.data.DragonflyCameraSnapshot
import com.ciandt.dragonfly.tensorflow.Classifier
import com.google.firebase.auth.FirebaseAuth


class FeedbackPresenter(val model: Model, val cameraSnapshot: DragonflyCameraSnapshot, val feedbackInteractor: FeedbackContract.Interactor, val firebaseAuth: FirebaseAuth) : BasePresenter<FeedbackContract.View>(), FeedbackContract.Presenter {
    private val results = ArrayList<Classifier.Recognition>()

    init {
        feedbackInteractor.setOnFeedbackSavedCallback { feedback ->
            DragonflyLogger.debug(LOG_TAG, "onFeedbackSaved(${feedback})")
        }
    }

    override fun setRecognitions(recognitions: List<Classifier.Recognition>) {

        results.clearAndAddAll(recognitions)

        if (results.isEmpty()) {
            view?.showNoRecognitions()
            return
        }

        view?.showRecognitions(results.head(), results.tail())
    }

    override fun markAsPositive() {
        view?.showPositiveRecognition(results.head())

        val identifiedLabels = HashMap<String, Float>()
        for (recognition in results) {
            identifiedLabels.put(recognition.title, recognition.confidence)
        }

        val feedback = Feedback(
                tenant = Tenant.ID,
                project = model.id,
                userId = firebaseAuth.currentUser!!.uid,
                modelVersion = model.version,
                value = Feedback.POSITIVE,
                actualLabel = results.head().title,
                identifiedLabels = identifiedLabels,
                imageLocalPath = cameraSnapshot.path
        )

        feedbackInteractor.saveFeedback(feedback, cameraSnapshot)
    }

    override fun markAsNegative() {
        view?.showNegativeForm(results.tail())
    }

    override fun submitNegative(actualLabel: String) {
        view?.showNegativeRecognition(actualLabel)

        val identifiedLabels = HashMap<String, Float>()
        for (recognition in results) {
            identifiedLabels.put(recognition.title, recognition.confidence)
        }

        val feedback = Feedback(
                tenant = Tenant.ID,
                project = model.id,
                userId = firebaseAuth.currentUser!!.uid,
                modelVersion = model.version,
                value = Feedback.NEGATIVE,
                actualLabel = actualLabel,
                identifiedLabels = identifiedLabels,
                imageLocalPath = cameraSnapshot.path
        )

        feedbackInteractor.saveFeedback(feedback, cameraSnapshot)
    }

    companion object {
        private val LOG_TAG = FeedbackPresenter::class.java.simpleName
    }
}
