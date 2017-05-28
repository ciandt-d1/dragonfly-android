package com.ciandt.dragonfly.example.features.realtime

import com.ciandt.dragonfly.data.Model

class RealTimePresenter : RealTimeContract.Presenter {

    private var view: RealTimeContract.View? = null

    override fun attachView(view: RealTimeContract.View) {
        this.view = view
    }

    override fun detachView() {
        this.view = null
    }


    override fun initModel(model: Model) {
        view?.showInfo(model.toString())
    }
}
