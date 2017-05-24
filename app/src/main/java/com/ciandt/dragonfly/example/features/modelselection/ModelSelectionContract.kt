package com.ciandt.dragonfly.example.features.modelselection

import com.ciandt.dragonfly.data.Model

interface ModelSelectionContract {

    interface View {

        fun update(models: List<Model>)

        fun update(model: Model)

        fun run(model: Model)
    }

    interface Presenter {

        fun getModelsList()

        fun selectModel(model: Model)
    }

}
