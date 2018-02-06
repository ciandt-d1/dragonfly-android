package com.ciandt.dragonfly.example.features.projectselection

import com.ciandt.dragonfly.example.infrastructure.extensions.replace
import com.ciandt.dragonfly.example.models.Project
import com.ciandt.dragonfly.example.models.Version
import com.ciandt.dragonfly.example.shared.BasePresenter
import java.io.Serializable

class ProjectSelectionPresenter(private val interactor: ProjectSelectionContract.Interactor) : BasePresenter<ProjectSelectionContract.View>(), ProjectSelectionContract.Presenter {

    private val updateQueue = ArrayList<Project>()

    private var lastLoad: Long = Long.MAX_VALUE

    private var hasPendingSeeUpdate = false

    override fun attachView(view: ProjectSelectionContract.View) {
        super.attachView(view)

        updateQueue.forEach {
            view.update(it)
        }
        updateQueue.clear()

        if (hasPendingSeeUpdate) {
            hasPendingSeeUpdate = false
            view.showSeeUpdates()
        }
    }

    override fun start() {
        registerProjectObserver()
        registerListObserver()
    }

    override fun stop() {
        interactor.unregisterProjectObserver()
        interactor.unregisterListObserver()
    }

    override fun loadProjects() {

        view?.hideSeeUpdates()

        view?.showLoading()

        interactor.loadProjects(
                onSuccess = { projects ->

                    if (projects.isEmpty()) {
                        view?.showEmpty()
                    } else {
                        view?.update(projects)
                    }

                },
                onFailure = { exception ->
                    view?.showError(exception)
                }
        )

        lastLoad = interactor.getTimestamp()
        registerListObserver()
    }

    override fun run(project: Project) {

        if (project.hasDownloadedVersion()) {
            val version = project.getLastDownloadedVersion()!!

            val others = HashMap<String, Serializable>()
            others.put("benchmark", project.showBenchmark)
            view?.run(version.toLibraryModel(others), project.name)
        }
    }

    override fun download(project: Project) {

        if (project.versions.isEmpty()) {
            view?.showUnavailable(project)
            return
        }

        if (project.hasDownloadingVersion()) {
            view?.showDownloading(project)

        } else {
            view?.confirmDownload(project) {

                val lastVersion = project.getLastVersion()!!
                lastVersion.status = Version.STATUS_DOWNLOADING

                project.versions.replace(lastVersion)
                view?.update(project)

                interactor.downloadVersion(project.name, project.description, lastVersion) { exception ->

                    lastVersion.status = Version.STATUS_NOT_DOWNLOADED
                    view?.update(project)

                    view?.showDownloadError(exception)
                }

            }
        }
    }

    private fun registerProjectObserver() {
        interactor.registerProjectObserver { project ->
            if (view != null) {
                view?.update(project)
            } else {
                updateQueue.add(project)
            }
        }
    }

    private fun registerListObserver() {
        interactor.registerListObserver { changedAt ->
            if (waslistChangedAfterLastLoad(changedAt)) {
                if (view != null) {
                    view?.showSeeUpdates()
                } else {
                    hasPendingSeeUpdate = true
                }
            }
        }
    }

    private fun waslistChangedAfterLastLoad(timestamp: Long): Boolean = (timestamp > lastLoad)
}
