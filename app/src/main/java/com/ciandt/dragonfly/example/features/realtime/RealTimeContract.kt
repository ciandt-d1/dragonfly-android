package com.ciandt.dragonfly.example.features.realtime

import com.ciandt.dragonfly.data.Model
import com.ciandt.dragonfly.example.shared.BasePresenter

interface RealTimeContract {

    interface View {

        fun showInfo(info: String)
    }

    interface Presenter : BasePresenter<View> {

        fun initModel(model: Model)
    }

}
