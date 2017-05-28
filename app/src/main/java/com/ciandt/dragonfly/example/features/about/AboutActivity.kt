package com.ciandt.dragonfly.example.features.about

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import com.ciandt.dragonfly.example.R
import com.ciandt.dragonfly.example.helpers.IntentHelper
import com.ciandt.dragonfly.example.shared.BaseActivity


class AboutActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)

        val privacy = findViewById(R.id.privacy) as TextView
        privacy.setOnClickListener {
            val intent = IntentHelper.openUrl(this, getString(R.string.about_privacy_url))
            startActivity(intent)
        }
    }

    companion object {
        fun create(context: Context): Intent {
            val intent = Intent(context, AboutActivity::class.java)
            return intent
        }
    }
}
