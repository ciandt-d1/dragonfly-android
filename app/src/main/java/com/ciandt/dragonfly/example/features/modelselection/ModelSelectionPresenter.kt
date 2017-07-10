package com.ciandt.dragonfly.example.features.modelselection

import com.ciandt.dragonfly.data.model.Model
import com.ciandt.dragonfly.example.shared.BasePresenter

class ModelSelectionPresenter(private var interactor: ModelSelectionContract.Interactor) : BasePresenter<ModelSelectionContract.View>(), ModelSelectionContract.Presenter {

    override fun loadModels() {

        view?.showLoading()

        interactor.loadModels(
                onSuccess = { models ->

                    if (models.isEmpty()) {
                        view?.showEmpty()
                    } else {
                        view?.update(models)
                    }

                },
                onFailure = { exception ->
                    view?.showError(exception)
                }
        )
    }

    override fun selectModel(model: Model) {

        if (model.isDownloaded) {
            view?.run(model)

        } else if (model.isDownloading) {
            view?.showDownloading(model)

        } else {
            model.status = Model.STATUS_DOWNLOADING
            view?.update(model)
        }
    }
}
