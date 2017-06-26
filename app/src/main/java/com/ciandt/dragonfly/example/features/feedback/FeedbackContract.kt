package com.ciandt.dragonfly.example.features.feedback

import com.ciandt.dragonfly.example.features.feedback.model.Feedback
import com.ciandt.dragonfly.example.shared.BasePresenterContract
import com.ciandt.dragonfly.lens.data.DragonflyCameraSnapshot
import com.ciandt.dragonfly.tensorflow.Classifier

/**
 * Created by iluz on 6/9/17.
 */
interface FeedbackContract {

    interface View {

        fun showNoRecognitions()

        fun showRecognitions(main: Classifier.Recognition, others: List<Classifier.Recognition>)

        fun showPositiveRecognition(recognition: Classifier.Recognition)

        fun showNegativeRecognition(label: String)

        fun showNegativeForm(others: List<Classifier.Recognition>)
    }

    interface Presenter : BasePresenterContract<View> {

        fun setRecognitions(recognitions: List<Classifier.Recognition>)

        fun markAsPositive()

        fun markAsNegative()

        fun submitNegative(label: String)
    }

    interface Interactor {
        fun setOnFeedbackSavedCallback(callback: ((Feedback) -> Unit)?)

        fun saveFeedback(feedback: Feedback, cameraSnapshot: DragonflyCameraSnapshot)
    }
}