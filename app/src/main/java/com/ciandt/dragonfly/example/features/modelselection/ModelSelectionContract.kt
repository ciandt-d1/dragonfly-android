package com.ciandt.dragonfly.example.features.modelselection

import com.ciandt.dragonfly.data.Model
import com.ciandt.dragonfly.example.shared.BasePresenterContract

interface ModelSelectionContract {

    interface View {

        fun showLoading()

        fun showEmpty()

        fun showError(exception: Exception)

        fun update(models: List<Model>)

        fun update(model: Model)

        fun run(model: Model)
    }

    interface Presenter : BasePresenterContract<View> {

        fun loadModels()

        fun selectModel(model: Model)
    }

    interface Interactor {

        fun loadModels(onSuccess: (List<Model>) -> Unit, onFailure: (Exception) -> Unit)

    }
}
