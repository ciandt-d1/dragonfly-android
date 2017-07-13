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

        fun showNoClassifications()

        fun showClassifications(mainClassificationLabel: String, otherClassifications: List<Classifier.Classification>)

        fun showPositiveClassification(mainClassificationLabel: String, otherClassifications: List<Classifier.Classification>, collapseResults: Boolean = true)

        fun showNegativeClassification(mainClassificationLabel: String, otherClassifications: List<Classifier.Classification>)

        fun showNegativeForm(otherClassifications: List<Classifier.Classification>)

        fun setUserFeedback(feedback: Feedback)

        fun showSaveImageSuccessMessage(@StringRes message: Int)

        fun showSaveImageErrorMessage(@StringRes message: Int)
    }

    interface Presenter : BasePresenterContract<View> {

        fun setClassifications(classifications: List<Classifier.Classification>)

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