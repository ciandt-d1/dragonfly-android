package com.ciandt.dragonfly.example.features.modelselection

import com.ciandt.dragonfly.data.model.Model
import com.ciandt.dragonfly.example.shared.BasePresenterContract

interface ModelSelectionContract {

    interface View {

        fun showLoading()

        fun showEmpty()

        fun showError(exception: Exception)

        fun update(models: List<Model>)

        fun update(model: Model)

        fun run(model: Model)

        fun showDownloading(model: Model)

        fun showDownloadError(exception: Exception)
    }

    interface Presenter : BasePresenterContract<View> {

        fun loadModels()

        fun selectModel(model: Model)

        fun registerModelsObserver()

        fun unregisterModelsObserver()
    }

    interface Interactor {

        fun loadModels(onSuccess: (List<Model>) -> Unit, onFailure: (Exception) -> Unit)

        fun downloadModel(model: Model, onFailure: (Exception) -> Unit)

        fun registerModelsObserver(onChanged: (Model) -> Unit)

        fun unregisterModelsObserver()
    }
}
