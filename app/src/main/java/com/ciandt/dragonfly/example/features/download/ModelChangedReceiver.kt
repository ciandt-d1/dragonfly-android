package com.ciandt.dragonfly.example.features.download

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import com.ciandt.dragonfly.data.model.Model
import com.ciandt.dragonfly.example.BuildConfig

class ModelChangedReceiver(private val onChanged: (Model) -> Unit) : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val model = intent.getParcelableExtra<Model>(MODEL_BUNDLE)
        onChanged(model)
    }

    companion object {
        private val FILTER = "${BuildConfig.APPLICATION_ID}.download_status_receiver"
        private val MODEL_BUNDLE = "${BuildConfig.APPLICATION_ID}.model_bundle"

        fun create(model: Model): Intent {
            val intent = Intent(FILTER)
            intent.putExtra(MODEL_BUNDLE, model)
            return intent
        }

        fun getIntentFilter(): IntentFilter = IntentFilter(FILTER)
    }
}