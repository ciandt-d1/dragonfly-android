package com.ciandt.dragonfly.example.features.feedback

import android.support.annotation.StringRes
import com.ciandt.dragonfly.example.features.feedback.model.Feedback
import com.ciandt.dragonfly.example.shared.BasePresenterContract
import com.ciandt.dragonfly.lens.data.DragonflyClassificationInput
import com.ciandt.dragonfly.tensorflow.Classifier

/**
 * Created by iluz on 6/9/17.
 */
interface FeedbackContract {

    interface View {

        fun showNoRecognitions()

        fun showRecognitions(mainRecognitionLabel: String, otherRecognitions: List<Classifier.Recognition>)

        fun showPositiveRecognition(mainRecognitionLabel: String, otherRecognitions: List<Classifier.Recognition>, collapseResults: Boolean = true)

        fun showNegativeRecognition(mainRecognitionLabel: String, otherRecognitions: List<Classifier.Recognition>)

        fun showNegativeForm(otherRecognitions: List<Classifier.Recognition>)

        fun setUserFeedback(feedback: Feedback)

        fun showSaveImageSuccessMessage(@StringRes message: Int)

        fun showSaveImageErrorMessage(@StringRes message: Int)
    }

    interface Presenter : BasePresenterContract<View> {

        fun setClassifications(recognitions: List<Classifier.Recognition>)

        fun markAsPositive()

        fun markAsNegative()

        fun submitNegative(label: String)

        fun setUserFeedback(userFeedback: Feedback?)

        fun saveImageToGallery(classificationInput: DragonflyClassificationInput)
    }

    interface Interactor {
        fun setOnFeedbackSavedCallback(callback: ((Feedback) -> Unit)?)

        fun saveFeedback(feedback: Feedback)
    }
}