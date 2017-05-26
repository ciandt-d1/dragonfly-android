package com.ciandt.dragonfly.example.features.modelselection

import com.ciandt.dragonfly.data.Model
import com.ciandt.dragonfly.example.shared.BasePresenter

interface ModelSelectionContract {

    interface View {

        fun update(models: List<Model>)

        fun update(model: Model)

        fun run(model: Model)
    }

    interface Presenter : BasePresenter<View> {

        fun getModelsList()

        fun selectModel(model: Model)
    }

}
