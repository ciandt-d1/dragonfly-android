package com.ciandt.dragonfly.example.features.projectselection

import com.ciandt.dragonfly.example.models.Project
import com.ciandt.dragonfly.example.models.Version
import com.ciandt.dragonfly.example.shared.BasePresenter

class ProjectSelectionPresenter(private var interactor: ProjectSelectionContract.Interactor) : BasePresenter<ProjectSelectionContract.View>(), ProjectSelectionContract.Presenter {

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

        if (project.isDownloaded()) {
            view?.run(project.toLibraryModel())

        } else if (project.isDownloading()) {
            view?.showDownloading(project)

        } else {
            project.status = Version.STATUS_DOWNLOADING
            view?.update(project)
        }
    }
}
