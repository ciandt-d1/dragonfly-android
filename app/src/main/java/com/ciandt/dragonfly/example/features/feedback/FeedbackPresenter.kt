package com.ciandt.dragonfly.example.features.feedback

import android.support.v4.util.Pair
import com.ciandt.dragonfly.data.model.Model
import com.ciandt.dragonfly.example.R
import com.ciandt.dragonfly.example.config.Tenant
import com.ciandt.dragonfly.example.features.feedback.model.Feedback
import com.ciandt.dragonfly.example.infrastructure.DragonflyLogger
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
    private val oldResults = ArrayList<Classifier.Classification>()
    private var userFeedback: Feedback? = null

    init {
        feedbackSaverInteractor.setOnFeedbackSavedCallback { feedback ->
            DragonflyLogger.debug(LOG_TAG, "setUserFeedback(${feedback})")
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

        if (userFeedback == null) {

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
        } else {
            userFeedback!!.let {
                if (it.isPositive()) {
                    view?.showPositiveClassification(false)
                } else {
                    view?.showNegativeClassification()
                }
            }
        }
    }

    override fun markAsPositive() {
        view?.showPositiveClassification()

//        saveFeedback(oldResults.head().title, Feedback.POSITIVE)
    }

    override fun markAsNegative() {
        view?.showNegativeForm()
    }

    override fun submitNegative(labels: List<String>) {
        view?.showNegativeClassification()

//        saveFeedback(labels, Feedback.NEGATIVE)
    }

    override fun setUserFeedback(userFeedback: Feedback?) {
        this.userFeedback = userFeedback
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

    private fun saveFeedback(label: String, value: Int) {
        val identifiedLabels = HashMap<String, Float>()
        for (classification in oldResults) {
            identifiedLabels.put(classification.title, classification.confidence)
        }

        val feedback = Feedback(
                tenant = Tenant.ID,
                project = model.id,
                userId = userId,
                modelVersion = model.version,
                value = value,
                actualLabel = label,
                identifiedLabels = identifiedLabels,
                imageLocalPath = classificationInput.imagePath
        )

        view?.setUserFeedback(feedback)

        feedbackSaverInteractor.saveFeedback(feedback)
    }

    private fun formatConfidence(confidence: Float): Int {
        return Math.round(confidence * 100)
    }

    companion object {
        private val LOG_TAG = FeedbackPresenter::class.java.simpleName
    }
}