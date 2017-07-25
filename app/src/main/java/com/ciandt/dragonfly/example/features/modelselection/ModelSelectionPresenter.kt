package com.ciandt.dragonfly.example.features.modelselection

import com.ciandt.dragonfly.data.model.Model
import com.ciandt.dragonfly.example.shared.BasePresenter

class ModelSelectionPresenter(private var interactor: ModelSelectionContract.Interactor) : BasePresenter<ModelSelectionContract.View>(), ModelSelectionContract.Presenter {

    private val updateQueue = ArrayList<Model>()

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

            interactor.downloadModel(model) { exception ->

                model.status = Model.STATUS_DEFAULT
                view?.update(model)

                view?.showDownloadError(exception)
            }
        }
    }

    override fun attachView(view: ModelSelectionContract.View) {
        super.attachView(view)

        updateQueue.forEach {
            view.update(it)
            updateQueue.remove(it)
        }
    }

    override fun registerModelsObserver() {
        interactor.registerModelsObserver { model ->

            if (view != null) {
                view?.update(model)
            } else {
                updateQueue.add(model)
            }
        }
    }

    override fun unregisterModelsObserver() {
        interactor.unregisterModelsObserver()
    }
}
