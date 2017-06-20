package com.ciandt.dragonfly.example.features.feedback

import com.ciandt.dragonfly.example.infrastructure.extensions.clearAndAddAll
import com.ciandt.dragonfly.example.infrastructure.extensions.head
import com.ciandt.dragonfly.example.infrastructure.extensions.tail
import com.ciandt.dragonfly.example.shared.BasePresenter
import com.ciandt.dragonfly.tensorflow.Classifier


class FeedbackPresenter : BasePresenter<FeedbackContract.View>(), FeedbackContract.Presenter {

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
    }

    override fun markAsNegative(newRecognition: Classifier.Recognition?) {
        // TODO: show form, get result and update view

        val onlyForDemo = newRecognition ?: results[results.size - 1]
        view?.showNegativeRecognition(onlyForDemo)
    }

}
