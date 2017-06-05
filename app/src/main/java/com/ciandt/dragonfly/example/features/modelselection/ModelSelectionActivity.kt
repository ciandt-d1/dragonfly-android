package com.ciandt.dragonfly.example.features.modelselection

import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.content.ContextCompat
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.LinearSnapHelper
import android.support.v7.widget.SimpleItemAnimator
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import com.ciandt.dragonfly.data.Model
import com.ciandt.dragonfly.example.R
import com.ciandt.dragonfly.example.features.about.AboutActivity
import com.ciandt.dragonfly.example.features.realtime.RealTimeActivity
import com.ciandt.dragonfly.example.shared.BaseActivity
import kotlinx.android.synthetic.main.activity_model_selection.*

class ModelSelectionActivity : BaseActivity(), ModelSelectionContract.View {

    private val MODELS_BUNDLE = "MODELS_BUNDLE"

    private lateinit var presenter: ModelSelectionContract.Presenter

    private val models = ArrayList<Model>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_model_selection)

        presenter = ModelSelectionPresenter(ModelSelectionInteractor())
        presenter.attachView(this)

        setupList()

        messageRetry.setOnClickListener {
            presenter.loadModels()
        }

        about.setOnClickListener {
            val intent = AboutActivity.create(this)
            startActivity(intent)
        }

        if (savedInstanceState != null) {
            update(savedInstanceState.getParcelableArrayList(MODELS_BUNDLE))
        } else {
            presenter.loadModels()
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
        outState?.putParcelableArrayList(MODELS_BUNDLE, models)
    }

    private fun setupList() {
        recyclerView.hasFixedSize()

        recyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        LinearSnapHelper().attachToRecyclerView(recyclerView)

        (recyclerView.itemAnimator as? SimpleItemAnimator)?.supportsChangeAnimations = false

        val adapter = ModelSelectionAdapter(this, models) { model ->
            presenter.selectModel(model)
        }

        adapter.setHasStableIds(true)

        recyclerView.adapter = adapter
    }

    override fun showLoading() {
        loading.visibility = VISIBLE
        recyclerView.visibility = INVISIBLE
        messageContainer.visibility = INVISIBLE
    }

    override fun showEmpty() {
        loading.visibility = INVISIBLE
        recyclerView.visibility = INVISIBLE

        messageIcon.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_warning))
        messageText.text = getString(R.string.model_selection_empty_message)
        messageContainer.visibility = VISIBLE
    }

    override fun showError(exception: Exception) {
        loading.visibility = INVISIBLE
        recyclerView.visibility = INVISIBLE

        messageIcon.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_error))
        messageText.text = getString(R.string.model_selection_error_message)
        messageContainer.visibility = VISIBLE
    }

    override fun update(models: List<Model>) {
        loading.visibility = INVISIBLE
        messageContainer.visibility = INVISIBLE

        this.models.clear()
        this.models.addAll(models)

        recyclerView.adapter.notifyDataSetChanged()

        recyclerView.visibility = VISIBLE
    }

    override fun update(model: Model) {
        if (models.contains(model)) {
            val index = models.indexOf(model)
            models[index] = model
            recyclerView.adapter.notifyItemChanged(index)
        } else {
            models.add(model)
            recyclerView.adapter.notifyItemInserted(models.size - 1)
        }
    }

    override fun run(model: Model) {
        val intent = RealTimeActivity.create(this, model)
        startActivity(intent)
    }

    override fun showDownloading(model: Model) {
        Snackbar.make(getRootView(), getString(R.string.model_selection_item_wait), Snackbar.LENGTH_LONG).show()
    }
}
