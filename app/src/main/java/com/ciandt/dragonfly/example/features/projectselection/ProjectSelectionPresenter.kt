package com.ciandt.dragonfly.example.features.projectselection

import com.ciandt.dragonfly.example.infrastructure.extensions.replace
import com.ciandt.dragonfly.example.models.Project
import com.ciandt.dragonfly.example.models.Version
import com.ciandt.dragonfly.example.shared.BasePresenter

class ProjectSelectionPresenter(private var interactor: ProjectSelectionContract.Interactor) : BasePresenter<ProjectSelectionContract.View>(), ProjectSelectionContract.Presenter {

    private val updateQueue = ArrayList<Project>()

    override fun attachView(view: ProjectSelectionContract.View) {
        super.attachView(view)

        updateQueue.forEach {
            view.update(it)
            updateQueue.remove(it)
        }
    }

    override fun loadProjects() {

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
    }

    override fun selectProject(project: Project) {

        if (project.versions.isEmpty()) {
            view?.showUnavailable(project)
            return
        }

        if (project.hasDownloadedVersion()) {
            view?.run(project.getLastDownloadedVersion()!!.toLibraryModel())

        } else if (project.hasDownloadingVersion()) {
            view?.showDownloading(project)

        } else {
            val lastVersion = project.getLastVersion()!!
            lastVersion.status = Version.STATUS_DOWNLOADING

            project.versions.replace(lastVersion)
            view?.update(project)

            interactor.downloadVersion(project.name, lastVersion) { exception ->

                lastVersion.status = Version.STATUS_NOT_DOWNLOADED
                view?.update(project)

                view?.showDownloadError(exception)
            }
        }
    }

    override fun registerProjectObserver() {
        interactor.registerProjectObserver { project ->
            if (view != null) {
                view?.update(project)
            } else {
                updateQueue.add(project)
            }
        }
    }

    override fun unregisterProjectObserver() {
        interactor.unregisterProjectObserver()
    }
}
