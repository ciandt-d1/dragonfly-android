package com.ciandt.dragonfly.example.features.download

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import com.ciandt.dragonfly.example.BuildConfig
import com.ciandt.dragonfly.example.models.Project

class ProjectChangedReceiver(private val onChanged: (Project) -> Unit) : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val project = intent.getParcelableExtra<Project>(PROJECT_BUNDLE)
        onChanged(project)
    }

    companion object {
        private val FILTER = "${BuildConfig.APPLICATION_ID}.project_changed_receiver"
        private val PROJECT_BUNDLE = "${BuildConfig.APPLICATION_ID}.project_bundle"

        fun create(project: Project): Intent {
            val intent = Intent(FILTER)
            intent.putExtra(PROJECT_BUNDLE, project)
            return intent
        }

        fun getIntentFilter(): IntentFilter = IntentFilter(FILTER)
    }
}