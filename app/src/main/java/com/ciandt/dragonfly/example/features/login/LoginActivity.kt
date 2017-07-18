package com.ciandt.dragonfly.example.features.login

import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.ciandt.dragonfly.example.R
import com.ciandt.dragonfly.example.shared.BaseActivity

class LoginActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
    }

    companion object {
        fun create(context: Context): Intent {
            val intent = Intent(context, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            return intent
        }
    }
}
