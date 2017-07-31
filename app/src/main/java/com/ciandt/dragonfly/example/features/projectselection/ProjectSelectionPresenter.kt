package com.ciandt.dragonfly.example.features.projectselection

import com.ciandt.dragonfly.data.model.Model
import com.ciandt.dragonfly.example.shared.BasePresenter

class ProjectSelectionPresenter(private var interactor: ProjectSelectionContract.Interactor) : BasePresenter<ProjectSelectionContract.View>(), ProjectSelectionContract.Presenter {

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
