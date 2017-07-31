package com.ciandt.dragonfly.example.features.projectselection

import com.ciandt.dragonfly.data.model.Model
import com.ciandt.dragonfly.example.models.Project
import com.ciandt.dragonfly.example.shared.BasePresenterContract

interface ProjectSelectionContract {

    interface View {

        fun showLoading()

        fun showEmpty()

        fun showError(exception: Exception)

        fun update(models: List<Model>)

        fun update(model: Model)

        fun run(model: Model)

        fun showDownloading(model: Model)
    }

    interface Presenter : BasePresenterContract<View> {

        fun loadModels()

        fun selectModel(model: Model)
    }

    interface Interactor {

        fun loadModels(onSuccess: (List<Model>) -> Unit, onFailure: (Exception) -> Unit)

        fun loadProjects(onSuccess: (List<Project>) -> Unit, onFailure: (Exception) -> Unit)

    }
}
