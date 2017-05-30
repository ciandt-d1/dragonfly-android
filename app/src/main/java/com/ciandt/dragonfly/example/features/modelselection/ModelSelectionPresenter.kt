package com.ciandt.dragonfly.example.features.modelselection

import com.ciandt.dragonfly.data.FakeModelGenerator
import com.ciandt.dragonfly.data.Model
import com.ciandt.dragonfly.example.shared.BasePresenter

class ModelSelectionPresenter : BasePresenter<ModelSelectionContract.View>(), ModelSelectionContract.Presenter {

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
