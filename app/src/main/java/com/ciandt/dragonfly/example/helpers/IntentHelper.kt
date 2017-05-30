package com.ciandt.dragonfly.example.helpers

import android.content.Intent
import android.net.Uri

object IntentHelper {

    fun openUrl(url: String): Intent {
        val intent = Intent(Intent.ACTION_VIEW)
        intent.data = Uri.parse(url)
        return intent
    }
}
