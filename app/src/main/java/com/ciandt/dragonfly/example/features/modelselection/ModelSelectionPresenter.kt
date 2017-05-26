package com.ciandt.dragonfly.example.features.modelselection

import com.ciandt.dragonfly.data.FakeModelGenerator
import com.ciandt.dragonfly.data.Model

class ModelSelectionPresenter : ModelSelectionContract.Presenter {

    private var view: ModelSelectionContract.View? = null

    override fun attachView(view: ModelSelectionContract.View) {
        this.view = view
    }

    override fun detachView() {
        this.view = null
    }

    override fun getModelsList() {

        val models = ArrayList<Model>()
        models.add(FakeModelGenerator.generate("1"))
        models.add(FakeModelGenerator.generate("2"))
        models.add(FakeModelGenerator.generate("3"))
        models.add(FakeModelGenerator.generate("4"))
        models.add(FakeModelGenerator.generate("5"))
        models.add(FakeModelGenerator.generate("6"))

        view?.update(models)
    }

    override fun selectModel(model: Model) {

        if (model.isDownloaded) {
            view?.run(model)

        } else if (model.isDownloading) {
            model.status = Model.STATUS_DOWNLOADED
            view?.update(model)

        } else {
            model.status = Model.STATUS_DOWNLOADING
            view?.update(model)
        }
    }
}
