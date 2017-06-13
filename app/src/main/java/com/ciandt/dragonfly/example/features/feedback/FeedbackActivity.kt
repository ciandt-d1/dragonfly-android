package com.ciandt.dragonfly.example.features.feedback

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.PersistableBundle
import android.support.design.widget.Snackbar
import com.ciandt.dragonfly.data.model.Model
import com.ciandt.dragonfly.example.BuildConfig
import com.ciandt.dragonfly.example.R
import com.ciandt.dragonfly.example.infrastructure.DragonflyLogger
import com.ciandt.dragonfly.example.shared.FullScreenActivity
import com.ciandt.dragonfly.lens.data.DragonflyCameraSnapshot
import com.ciandt.dragonfly.lens.exception.DragonflyModelException
import com.ciandt.dragonfly.lens.exception.DragonflyRecognitionException
import com.ciandt.dragonfly.lens.ui.DragonflyLensFeedbackView
import com.ciandt.dragonfly.tensorflow.Classifier
import kotlinx.android.synthetic.main.activity_feedback.*


class FeedbackActivity : FullScreenActivity(), FeedbackContract.View {

    lateinit private var cameraSnapshot: DragonflyCameraSnapshot
    lateinit private var model: Model

    var lastRecognizedObjects: ArrayList<Classifier.Recognition>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_feedback)

        if (savedInstanceState != null) {
            model = savedInstanceState.getParcelable(MODEL_BUNDLE)
            cameraSnapshot = savedInstanceState.getParcelable(SNAPSHOT_BUNDLE)
            lastRecognizedObjects = savedInstanceState.getParcelableArrayList(RECOGNITIONS_BUNDLE)
        } else {
            cameraSnapshot = intent.extras.getParcelable<DragonflyCameraSnapshot>(SNAPSHOT_BUNDLE)
            model = intent.extras.getParcelable<Model>(MODEL_BUNDLE)
            lastRecognizedObjects = null
        }

        if (!hasRecognizedObjectsCached()) {
            setupModelCallbacks()
            setupBitmapAnalysisCallbacks()
        }

        dragonFlyLensFeedbackView.setSnapshot(cameraSnapshot)
    }

    private fun setupModelCallbacks() {
        dragonFlyLensFeedbackView.setModelCallbacks(object : DragonflyLensFeedbackView.ModelCallbacks {
            override fun onStartedLoadingModel(model: Model?) {
                DragonflyLogger.debug(LOG_TAG, "DragonflyLensFeedbackView.ModelCallbacks.onStartedLoadingModel(${model})")
            }

            override fun onModelReady(model: Model?) {
                DragonflyLogger.debug(LOG_TAG, "DragonflyLensFeedbackView.ModelCallbacks.onModelReady(${model})")

                dragonFlyLensFeedbackView.analyzeSnapshot()
            }

            override fun onModelFailure(e: DragonflyModelException?) {
                DragonflyLogger.debug(LOG_TAG, "DragonflyLensFeedbackView.ModelCallbacks.onModelFailure(${e})")
            }
        })
    }

    private fun setupBitmapAnalysisCallbacks() {
        dragonFlyLensFeedbackView.setSnapshotAnalysisCallbacks(object : DragonflyLensFeedbackView.SnapshotAnalysisCallbacks {
            override fun onSnapshotAnalyzed(results: MutableList<Classifier.Recognition>) {
                DragonflyLogger.debug(LOG_TAG, "DragonflyLensFeedbackView.SnapshotAnalysisCallbacks.onSnapshotAnalyzed(${results})")

                lastRecognizedObjects = ArrayList(results)
                Snackbar.make(getRootView(), results.toString(), Snackbar.LENGTH_INDEFINITE).show()
            }

            override fun onSnapshotAnalysisFailed(e: DragonflyRecognitionException?) {
                DragonflyLogger.debug(LOG_TAG, "DragonflyLensFeedbackView.SnapshotAnalysisCallbacks.onSnapshotAnalysisFailed(${e})")

                lastRecognizedObjects = null
            }
        })
    }

    override fun onResume() {
        super.onResume()

        if (!hasRecognizedObjectsCached()) {
            dragonFlyLensFeedbackView.start(model)
        }
    }

    override fun onPause() {
        super.onPause()

        dragonFlyLensFeedbackView.stop()
    }

    override fun onSaveInstanceState(outState: Bundle?, outPersistentState: PersistableBundle?) {
        super.onSaveInstanceState(outState, outPersistentState)

        outState?.putParcelable(MODEL_BUNDLE, model)
        outState?.putParcelable(SNAPSHOT_BUNDLE, cameraSnapshot)

        if (lastRecognizedObjects != null) {
            outState?.putParcelableArrayList(RECOGNITIONS_BUNDLE, lastRecognizedObjects)
        }
    }

    private fun hasRecognizedObjectsCached() = lastRecognizedObjects != null

    companion object {
        private val CLASS_NAME = FeedbackActivity::class.java.simpleName
        private val LOG_TAG = FeedbackActivity::class.java.simpleName

        private val MODEL_BUNDLE = String.format("%s.model_bundle", BuildConfig.APPLICATION_ID)
        private val SNAPSHOT_BUNDLE = String.format("%s.snapshot_bundle", BuildConfig.APPLICATION_ID)
        private val RECOGNITIONS_BUNDLE = String.format("%s.recognitions", BuildConfig.APPLICATION_ID)

        fun newIntent(context: Context, model: Model, snapshot: DragonflyCameraSnapshot): Intent {
            val intent = Intent(context, FeedbackActivity::class.java)
            intent.putExtra(MODEL_BUNDLE, model)
            intent.putExtra(SNAPSHOT_BUNDLE, snapshot)

            return intent
        }
    }
}
