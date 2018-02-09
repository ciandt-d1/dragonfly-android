package com.ciandt.dragonfly.example.features.feedback

import android.support.v4.util.Pair
import com.ciandt.dragonfly.data.model.Model
import com.ciandt.dragonfly.example.R
import com.ciandt.dragonfly.example.config.Tenant
import com.ciandt.dragonfly.example.features.feedback.model.Feedback
import com.ciandt.dragonfly.example.infrastructure.DragonflyLogger
import com.ciandt.dragonfly.example.infrastructure.extensions.clearAndAddAll
import com.ciandt.dragonfly.example.shared.BasePresenter
import com.ciandt.dragonfly.lens.data.DragonflyClassificationInput
import com.ciandt.dragonfly.tensorflow.Classifier

class FeedbackPresenter(
        val model: Model,
        val classificationInput: DragonflyClassificationInput,
        val userId: String,
        val feedbackSaverInteractor: FeedbackContract.SaverInteractor,
        val saveImageToGalleryInteractor: SaveImageToGalleryContract.Interactor,
        val benchmarkInteractor: BenchmarkContract.Interactor
) : BasePresenter<FeedbackContract.View>(), FeedbackContract.Presenter {
    private val results = LinkedHashMap<String, ArrayList<Classifier.Classification>>()
    private var userFeedbacks = ArrayList<Feedback>()

    init {
        feedbackSaverInteractor.setOnFeedbackSavedCallback { feedback ->
            DragonflyLogger.debug(LOG_TAG, "setUserFeedbackList(${feedback})")
        }

        saveImageToGalleryInteractor.setOnSaveImageSuccessCallback {
            view?.showSaveImageSuccessMessage(R.string.feedback_save_image_success)
        }

        saveImageToGalleryInteractor.setOnSaveImageErrorCallback {
            view?.showSaveImageSuccessMessage(R.string.feedback_save_image_error)
        }
    }


    override fun setClassifications(classifications: LinkedHashMap<String, ArrayList<Classifier.Classification>>) {
        results.clear()
        results.putAll(classifications)

        var labels: ArrayList<Pair<String, Int>> = ArrayList()

        classifications.entries.forEach { entry ->

            val list = entry.value
            if (list.isEmpty()) {
                return
            }

            if (!list[0].hasTitle()) {
                return
            }

            labels.add(Pair.create(list[0].title, formatConfidence(list[0].confidence)))
        }

        view?.showMainClassifications(labels)

        if (userFeedbacks.isNotEmpty()) {
            if (userFeedbacks.none { it.isNegative() }) {
                view?.showPositiveClassification(false)
            } else {
                view?.showNegativeClassification(userFeedbacks.map { it.actualLabel })
            }
        }

        showClassifications()
    }

    private fun showClassifications() {
        val displayResults = LinkedHashMap<String, ArrayList<Classifier.Classification>>()
        results.forEach { (key, value) ->
            val index = results.keys.indexOf(key)
            val title = if (index >= 0) {
                model.outputDisplayNames[index]
            } else {
                key
            }
            displayResults.put(title, value)
        }

        view?.showClassifications(displayResults)
    }

    override fun markAsPositive() {
        view?.showPositiveClassification()

        val labels = ArrayList<String>()
        results.forEach { (_, classifications) ->
            labels.add(classifications.first().title)
        }

        saveFeedback(labels)
    }

    override fun markAsNegative() {
        view?.showNegativeForm()
    }

    override fun submitNegative(labels: List<String>) {
        view?.showNegativeClassification(labels)

        saveFeedback(labels)
    }

    override fun setUserFeedbackList(userFeedbackList: List<Feedback>) {
        this.userFeedbacks.clearAndAddAll(userFeedbackList)
    }

    override fun saveImageToGallery() {
        saveImageToGalleryInteractor.save(classificationInput)
    }

    override fun benchmark() {

        view?.showBenchmarkLoading()

        benchmarkInteractor.benchmark(classificationInput,
                onSuccess = { result ->
                    if (result.benchmarks.isEmpty()) {
                        view?.showBenchmarkEmpty()
                    } else {
                        view?.showBenchmarkResult(result)
                    }
                },
                onFailure = { exception ->
                    view?.showBenchmarkError(exception)
                }
        )
    }

    private fun saveFeedback(labels: List<String>) {

        val feedbackList = ArrayList<Feedback>()

        results.forEach { (outputName, classifications) ->
            val identifiedLabels = LinkedHashMap<String, Float>()
            for (classification in classifications) {
                identifiedLabels.put(classification.title, classification.confidence)
            }

            val index = results.keys.indexOf(outputName)
            val actualLabel = labels[index]
            val positive = if (classifications.first().title == actualLabel) 1 else 0

            val isOther = classifications.none { it.title == actualLabel }
            if (isOther) {
                results[outputName]?.add(createOtherClassification(actualLabel))
            }

            val feedback = Feedback(
                    tenant = Tenant.ID,
                    project = model.id,
                    userId = userId,
                    modelVersion = model.version,
                    modelOutputName = outputName,
                    value = positive,
                    actualLabel = actualLabel,
                    identifiedLabels = identifiedLabels,
                    imageLocalPath = classificationInput.imagePath
            )

            feedbackList.add(feedback)
            feedbackSaverInteractor.saveFeedback(feedback)
        }

        view?.setUserFeedbackList(feedbackList)
        showClassifications()
    }

    private fun createOtherClassification(actualLabel: String): Classifier.Classification {
        return FeedbackChip.createOtherClassification(actualLabel)
    }

    private fun formatConfidence(confidence: Float): Int {
        return Math.round(confidence * 100)
    }

    companion object {
        private val LOG_TAG = FeedbackPresenter::class.java.simpleName
    }
}