package com.ciandt.dragonfly.example.features.projectselection

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.content.ContextCompat
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.SimpleItemAnimator
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import com.ciandt.dragonfly.data.model.Model
import com.ciandt.dragonfly.example.R
import com.ciandt.dragonfly.example.data.remote.RemoteProjectService
import com.ciandt.dragonfly.example.features.about.AboutActivity
import com.ciandt.dragonfly.example.features.realtime.RealTimeActivity
import com.ciandt.dragonfly.example.models.Project
import com.ciandt.dragonfly.example.shared.BaseActivity
import kotlinx.android.synthetic.main.activity_project_selection.*

class ProjectSelectionActivity : BaseActivity(), ProjectSelectionContract.View {

    private val PROJECTS_BUNDLE = "PROJECTS_BUNDLE"

    private lateinit var presenter: ProjectSelectionContract.Presenter

    private val projects = ArrayList<Project>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_project_selection)

        RemoteProjectService.start(this)

        presenter = ProjectSelectionPresenter(ProjectSelectionInteractor(this))
        presenter.attachView(this)

        setupList()

        messageRetry.setOnClickListener {
            presenter.loadProjects()
        }

        about.setOnClickListener {
            val intent = AboutActivity.create(this)
            startActivity(intent)
        }

        if (savedInstanceState != null) {
            update(savedInstanceState.getParcelableArrayList(PROJECTS_BUNDLE))
        }
    }

    override fun onResume() {
        super.onResume()
        presenter.attachView(this)

        if (projects.isEmpty()) {
            presenter.loadProjects()
        }
    }

    override fun onPause() {
        super.onPause()
        presenter.detachView()
    }

    override fun onDestroy() {
        RemoteProjectService.stop(this)
        super.onDestroy()
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)
        outState?.putParcelableArrayList(PROJECTS_BUNDLE, projects)
    }

    private fun setupList() {
        recyclerView.hasFixedSize()

        recyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        (recyclerView.itemAnimator as? SimpleItemAnimator)?.supportsChangeAnimations = false

        val adapter = ProjectSelectionAdapter(this, projects) { project ->
            presenter.selectProject(project)
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
        messageText.text = getString(R.string.project_selection_empty_message)
        messageContainer.visibility = VISIBLE
    }

    override fun showError(exception: Exception) {
        loading.visibility = INVISIBLE
        recyclerView.visibility = INVISIBLE

        messageIcon.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_error))
        messageText.text = getString(R.string.project_selection_error_message)
        messageContainer.visibility = VISIBLE
    }

    override fun update(projects: List<Project>) {
        loading.visibility = INVISIBLE
        messageContainer.visibility = INVISIBLE

        this.projects.clear()
        this.projects.addAll(projects)

        recyclerView.adapter.notifyDataSetChanged()

        recyclerView.visibility = VISIBLE
    }

    override fun update(project: Project) {
        if (projects.contains(project)) {
            val index = projects.indexOf(project)
            projects[index] = project
            recyclerView.adapter.notifyItemChanged(index)
        } else {
            projects.add(project)
            recyclerView.adapter.notifyItemInserted(projects.size - 1)
        }
    }

    override fun run(model: Model) {
        val intent = RealTimeActivity.create(this, model)
        startActivity(intent)
    }

    override fun showDownloading(project: Project) {
        Snackbar.make(getRootView(), getString(R.string.project_selection_item_wait), Snackbar.LENGTH_LONG).show()
    }

    override fun showUnavailable(project: Project) {
        Snackbar.make(getRootView(), getString(R.string.project_selection_item_unavailable), Snackbar.LENGTH_LONG).show()
    }

    companion object {
        fun create(context: Context): Intent {
            return Intent(context, ProjectSelectionActivity::class.java)
        }
    }
}
