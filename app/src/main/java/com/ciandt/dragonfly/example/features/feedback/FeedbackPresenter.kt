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


class FeedbackPresenter(model: Model, cameraSnapshot: DragonflyCameraSnapshot, feedbackInteractor: FeedbackContract.Interactor, firebaseAuth: FirebaseAuth) : BasePresenter<FeedbackContract.View>(), FeedbackContract.Presenter, FeedbackContract.Interactor.FeedbackCallbacks {
    private val model = model
    private val cameraSnapshot = cameraSnapshot
    private val feedbackInteractor = feedbackInteractor
    private val firebaseAuth = firebaseAuth
    private val results = ArrayList<Classifier.Recognition>()

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
        // TODO: show form, get result and update view

        view?.showNegativeForm(results.tail())

//        val onlyForDemo = results[results.size - 1]
//        view?.showNegativeRecognition(onlyForDemo)
    }

    override fun onFeedbackSaved(feedback: Feedback) {
        DragonflyLogger.debug(LOG_TAG, "onFeedbackSaved(${feedback})")
    }

    companion object {
        private val LOG_TAG = FeedbackPresenter::class.java.simpleName
    }
}
