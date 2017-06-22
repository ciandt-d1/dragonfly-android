package com.ciandt.dragonfly.example.features.feedback

import com.ciandt.dragonfly.example.shared.BasePresenterContract
import com.ciandt.dragonfly.tensorflow.Classifier

/**
 * Created by iluz on 6/9/17.
 */
interface FeedbackContract {

    interface View {

        fun showNoRecognitions()

        fun showRecognitions(main: Classifier.Recognition, others: List<Classifier.Recognition>)

        fun showPositiveRecognition(recognition: Classifier.Recognition)

        fun showNegativeRecognition(recognition: Classifier.Recognition)

        fun showNegativeForm(others: List<Classifier.Recognition>)
    }

    interface Presenter : BasePresenterContract<View> {

        fun setRecognitions(recognitions: List<Classifier.Recognition>)

        fun markAsPositive()

        fun markAsNegative()
    }

}