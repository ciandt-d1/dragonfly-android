package com.ciandt.dragonfly.example.helpers

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import com.ciandt.dragonfly.example.BuildConfig

object IntentHelper {

    fun openUrl(url: String): Intent {
        val intent = Intent(Intent.ACTION_VIEW)
        intent.data = Uri.parse(url)
        return intent
    }

    fun openSettings(): Intent {
        val intent = Intent()
        intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS

        val uri = Uri.fromParts("package", BuildConfig.APPLICATION_ID, null)
        intent.data = uri

        return intent
    }

    fun selectImageFromLibrary(): Intent {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.type = "image/jpeg"

        return intent
    }
}
