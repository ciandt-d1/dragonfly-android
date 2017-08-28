package com.ciandt.dragonfly.example.helpers

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import com.ciandt.dragonfly.data.model.Model
import com.ciandt.dragonfly.example.BuildConfig
import com.ciandt.dragonfly.example.features.login.LoginActivity
import com.ciandt.dragonfly.example.features.projectselection.ProjectSelectionActivity
import com.ciandt.dragonfly.example.features.realtime.RealTimeActivity

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

    fun openLogin(context: Context): Intent {
        return LoginActivity.create(context)
    }

    fun openProjectSelection(context: Context): Intent {
        return ProjectSelectionActivity.create(context)
    }

    fun openRealTime(context: Context, model: Model, name: String = ""): Intent {
        return RealTimeActivity.create(context, model, name)
    }
}
