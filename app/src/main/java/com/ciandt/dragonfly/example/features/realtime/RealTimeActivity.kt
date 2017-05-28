package com.ciandt.dragonfly.example.features.realtime

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import com.ciandt.dragonfly.data.Model
import com.ciandt.dragonfly.example.R
import com.ciandt.dragonfly.example.shared.BaseActivity

class RealTimeActivity : BaseActivity(), RealTimeContract.View {

    private lateinit var presenter: RealTimeContract.Presenter

    private lateinit var info: TextView

    private var model: Model? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_real_time)

        info = findViewById(R.id.info) as TextView

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

    override fun showInfo(info: String) {
        this.info.text = info
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
