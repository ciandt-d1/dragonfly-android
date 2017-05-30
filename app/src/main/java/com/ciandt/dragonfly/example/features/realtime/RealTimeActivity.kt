package com.ciandt.dragonfly.example.features.realtime

import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.ciandt.dragonfly.data.Model
import com.ciandt.dragonfly.example.R
import com.ciandt.dragonfly.example.shared.FullScreenActivity
import kotlinx.android.synthetic.main.activity_real_time.*

class RealTimeActivity : FullScreenActivity(), RealTimeContract.View {

    private lateinit var presenter: RealTimeContract.Presenter

    private var model: Model? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_real_time)

        presenter = RealTimePresenter()
        presenter.attachView(this)

        if (savedInstanceState != null) {
            model = savedInstanceState.getParcelable(MODEL_BUNDLE)
        } else {
            model = intent.extras?.getParcelable<Model>(MODEL_BUNDLE)
        }

        model?.let {
            presenter.initModel(it)
        }
    }

    override fun onResume() {
        super.onResume()
        presenter.attachView(this)
    }

    override fun onPause() {
        super.onPause()
        presenter.detachView()
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)
        model?.let {
            outState?.putParcelable(MODEL_BUNDLE, it)
        }
    }

    override fun showInfo(text: String) {
        info.text = text
    }

    companion object {
        private val MODEL_BUNDLE = "MODEL_BUNDLE"

        fun create(context: Context, model: Model): Intent {
            val intent = Intent(context, RealTimeActivity::class.java)
            intent.putExtra(MODEL_BUNDLE, model)
            return intent
        }
    }
}
