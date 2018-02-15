package com.ciandt.dragonfly.example.features.feedback

import android.support.annotation.StringRes
import android.support.v4.util.Pair
import com.ciandt.dragonfly.example.features.feedback.model.BenchmarkResult
import com.ciandt.dragonfly.example.features.feedback.model.Feedback
import com.ciandt.dragonfly.example.shared.BasePresenterContract
import com.ciandt.dragonfly.tensorflow.Classifier

/**
 * Created by iluz on 6/9/17.
 */
interface FeedbackContract {

    interface View {

        fun showMainClassifications(labels: List<Pair<String, Int>>)

        fun showNoClassifications()

        fun showClassifications(classifications: Map<String, List<Classifier.Classification>>)

        fun showPositiveClassification(collapseResults: Boolean = true)

        fun showNegativeClassification(classifications: List<String>)

        fun showNegativeForm()

        fun setUserFeedbackList(feedbackList: List<Feedback>)

        fun showSaveImageSuccessMessage(@StringRes message: Int)

        fun showSaveImageErrorMessage(@StringRes message: Int)

        fun showBenchmarkLoading()

        fun showBenchmarkResult(result: BenchmarkResult)

        fun showBenchmarkError(exception: Exception)

        fun showBenchmarkEmpty()
    }

    interface Presenter : BasePresenterContract<View> {

        fun setClassifications(classifications: LinkedHashMap<String, ArrayList<Classifier.Classification>>)

        fun markAsPositive()

        fun markAsNegative()

        fun submitNegative(labels: List<String>)

        fun setUserFeedbackList(userFeedbackList: List<Feedback>)

        fun saveImageToGallery()

        fun benchmark()
    }

    interface SaverInteractor {
        fun setOnFeedbackSavedCallback(callback: ((Feedback) -> Unit)?)

        fun setOnFeedbackSaveErrorCallback(callback: ((Feedback, Exception) -> Unit)?)

        fun saveFeedback(feedback: Feedback)
    }
}