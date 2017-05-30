package com.ciandt.dragonfly.example.features.modelselection

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.LinearSnapHelper
import android.support.v7.widget.SimpleItemAnimator
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

        presenter = ModelSelectionPresenter()
        presenter.attachView(this)

        setupList()
        setupAboutButton()

        if (savedInstanceState != null) {
            update(savedInstanceState.getParcelableArrayList(MODELS_BUNDLE))
        } else {
            presenter.getModelsList()
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

    private fun setupAboutButton() {
        about.setOnClickListener {
            val intent = AboutActivity.create(this)
            startActivity(intent)
        }
    }

    override fun update(models: List<Model>) {
        this.models.clear()
        this.models.addAll(models)

        recyclerView.adapter.notifyDataSetChanged()
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
}
