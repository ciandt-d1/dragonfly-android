package com.ciandt.dragonfly.example.features.projectselection

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.support.v4.content.ContextCompat
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.SimpleItemAnimator
import com.ciandt.dragonfly.data.model.Model
import com.ciandt.dragonfly.example.R
import com.ciandt.dragonfly.example.config.CommonBundleNames
import com.ciandt.dragonfly.example.data.remote.RemoteProjectService
import com.ciandt.dragonfly.example.features.about.AboutActivity
import com.ciandt.dragonfly.example.helpers.DialogHelper
import com.ciandt.dragonfly.example.helpers.IntentHelper
import com.ciandt.dragonfly.example.infrastructure.extensions.*
import com.ciandt.dragonfly.example.models.Project
import com.ciandt.dragonfly.example.shared.BaseActivity
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_project_selection.*

class ProjectSelectionActivity : BaseActivity(), ProjectSelectionContract.View {
    private lateinit var presenter: ProjectSelectionContract.Presenter

    private val projects = ArrayList<Project>()

    private var firstTime = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_project_selection)

        RemoteProjectService.start(this)

        presenter = ProjectSelectionPresenter(ProjectSelectionInteractor(applicationContext, FirebaseStorage.getInstance()))
        presenter.attachView(this)
        presenter.start()

        setupList()

        stateRetry.setOnClickListener {
            presenter.loadProjects()
        }

        about.setOnClickListener {
            val intent = AboutActivity.create(this)
            startActivity(intent)
        }

        update.setOnClickListener {
            presenter.loadProjects()
        }

        logout.setOnClickListener {
            logout()
        }

        if (savedInstanceState != null) {
            update(savedInstanceState.getParcelableArrayList(PROJECTS_BUNDLE))
        }
    }

    override fun onResume() {
        super.onResume()
        presenter.attachView(this)

        if (firstTime) {
            firstTime = false

            showLoading()

            Handler().postDelayed({
                presenter.loadProjects()
            }, FIRST_TIME_DELAY)

        } else if (projects.isEmpty()) {
            presenter.loadProjects()
        }
    }

    override fun onPause() {
        super.onPause()
        presenter.detachView()
    }

    override fun onDestroy() {
        presenter.stop()
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

        val adapter = ProjectSelectionAdapter(this, projects) { button, project ->
            when (button) {
                ProjectSelectionViewHolder.BUTTON_DOWNLOAD -> presenter.download(project)
                ProjectSelectionViewHolder.BUTTON_EXPLORE -> presenter.run(project)
            }
        }

        adapter.setHasStableIds(true)

        recyclerView.adapter = adapter
    }

    override fun showLoading() {
        loading.makeVisible()
        subtitle.makeInvisible()
        recyclerView.makeInvisible()
        stateContainer.makeInvisible()
    }

    override fun showEmpty() {
        loading.makeInvisible()
        subtitle.makeInvisible()
        recyclerView.makeInvisible()

        stateIcon.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_projects_empty))
        stateTitle.text = getString(R.string.project_selection_empty_state_title)
        stateMessage.text = getString(R.string.project_selection_empty_state_message)
        stateContainer.makeVisible()
    }

    override fun showError(exception: Exception) {
        loading.makeInvisible()
        subtitle.makeInvisible()
        recyclerView.makeInvisible()

        stateIcon.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_projects_error))
        stateTitle.text = getString(R.string.project_selection_error_state_title)
        stateMessage.text = getString(R.string.project_selection_error_state_message)
        stateContainer.makeVisible()
    }

    override fun update(projects: List<Project>) {
        loading.makeInvisible()
        stateContainer.makeInvisible()

        this.projects.clear()
        this.projects.addAll(projects)

        recyclerView.adapter.notifyDataSetChanged()

        recyclerView.makeVisible()
        subtitle.makeVisible()
    }

    override fun update(project: Project) {
        if (projects.contains(project)) {
            val index = projects.indexOf(project)
            projects[index] = project

            val payload = Bundle().apply {
                putBoolean(CommonBundleNames.PROJECT_CHANGED, true)
            }
            recyclerView.adapter.notifyItemChanged(index, payload)
        } else {
            projects.add(project)
            recyclerView.adapter.notifyItemInserted(projects.size - 1)
        }
    }

    override fun run(model: Model, name: String) {
        val intent = IntentHelper.openRealTime(this, model, name)
        startActivity(intent)
    }

    override fun showDownloading(project: Project) {
        showSnackbar(R.string.project_selection_item_wait_message)
    }

    override fun showDownloadError(exception: Exception) {
        showSnackbar(R.string.download_failed)
    }

    override fun showUnavailable(project: Project) {
        showSnackbar(R.string.project_selection_item_unavailable_message)
    }

    override fun confirmDownload(project: Project, onConfirm: () -> Unit) {
        if (isWifiNetworkConnected()) {
            onConfirm()
        } else {
            DialogHelper.showConfirmation(this,
                    getString(R.string.project_selection_download_confirmation_title),
                    getString(R.string.project_selection_download_confirmation_message)) {
                onConfirm()
            }
        }
    }

    override fun showSeeUpdates() {
        if (updateContainer.isVisible()) {
            return
        }

        updateContainer.apply {
            alpha = 0.0f
            makeVisible()
            animate().alpha(1.0f)
        }
    }

    override fun hideSeeUpdates() {
        updateContainer.makeGone()
    }

    companion object {

        private val PROJECTS_BUNDLE = "PROJECTS_BUNDLE"

        private val FIRST_TIME_DELAY = 1500L

        fun create(context: Context): Intent {
            return Intent(context, ProjectSelectionActivity::class.java)
        }
    }
}
