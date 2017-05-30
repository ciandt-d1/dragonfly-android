package com.ciandt.dragonfly.example.features.about

import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.ciandt.dragonfly.example.R
import com.ciandt.dragonfly.example.helpers.IntentHelper
import com.ciandt.dragonfly.example.shared.BaseActivity
import kotlinx.android.synthetic.main.activity_about.*

class AboutActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)

        privacy.setOnClickListener {
            val intent = IntentHelper.openUrl(getString(R.string.about_privacy_url))
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
