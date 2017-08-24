package com.ciandt.dragonfly.example.features.projectselection

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import com.ciandt.dragonfly.example.BuildConfig

class ProjectListChangedReceiver(private val onChanged: (Long) -> Unit) : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        onChanged(intent.getLongExtra(TIMESTAMP_BUNDLE, -1))
    }

    companion object {
        private val FILTER = "${BuildConfig.APPLICATION_ID}.project_list_changed_receiver"
        private val TIMESTAMP_BUNDLE = "${BuildConfig.APPLICATION_ID}.timestamp_bundle"

        fun create(timestamp: Long): Intent {
            val intent = Intent(FILTER)
            intent.putExtra(TIMESTAMP_BUNDLE, timestamp)
            return intent
        }

        fun getIntentFilter(): IntentFilter = IntentFilter(FILTER)
    }
}