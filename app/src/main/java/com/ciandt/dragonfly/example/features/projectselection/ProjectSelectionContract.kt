package com.ciandt.dragonfly.example.features.projectselection

import com.ciandt.dragonfly.data.model.Model
import com.ciandt.dragonfly.example.models.Project
import com.ciandt.dragonfly.example.shared.BasePresenterContract

interface ProjectSelectionContract {

    interface View {

        fun showLoading()

        fun showEmpty()

        fun showError(exception: Exception)

        fun update(projects: List<Project>)

        fun update(project: Project)

        fun run(model: Model)

        fun showDownloading(project: Project)

        fun showUnavailable(project: Project)
    }

    interface Presenter : BasePresenterContract<View> {

        fun loadProjects()

        fun selectProject(project: Project)
    }

    interface Interactor {

        fun loadProjects(onSuccess: (List<Project>) -> Unit, onFailure: (Exception) -> Unit)
    }
}
