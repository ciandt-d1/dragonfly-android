package com.ciandt.dragonfly.example.features.feedback

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.ciandt.dragonfly.data.model.Model
import com.ciandt.dragonfly.example.BuildConfig
import com.ciandt.dragonfly.example.R
import com.ciandt.dragonfly.lens.data.DragonflyCameraSnapshot

class FeedbackActivity : AppCompatActivity(), FeedbackContract.View {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_feedback)
    }

    companion object {
        private val CLASS_NAME = FeedbackActivity::class.java.simpleName

        private val MODEL_BUNDLE = String.format("%s.model_bundle", BuildConfig.APPLICATION_ID);
        private val SNAPSHOT_BUNDLE = String.format("%s.snapshot_bundle", BuildConfig.APPLICATION_ID)

        fun newIntent(context: Context, model: Model, snapshot: DragonflyCameraSnapshot): Intent {
            val intent = Intent(context, FeedbackActivity::class.java)
            intent.putExtra(MODEL_BUNDLE, model)
            intent.putExtra(SNAPSHOT_BUNDLE, snapshot)

            return intent
        }
    }
}
