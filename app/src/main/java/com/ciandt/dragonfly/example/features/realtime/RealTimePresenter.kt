package com.ciandt.dragonfly.example.features.realtime

import com.ciandt.dragonfly.example.R
import com.ciandt.dragonfly.example.shared.BasePresenter

class RealTimePresenter : BasePresenter<RealTimeContract.View>(), RealTimeContract.Presenter {
    override fun attachView(view: RealTimeContract.View) {
        super.attachView(view)
        view.requestRealTimePermissions()
    }

    override fun onRealTimePermissionsDenied() {
        view?.requestRealTimePermissions()
    }

    override fun onRealTimePermissionsPermanentlyDenied() {
        view?.showRealTimePermissionsError(R.string.permissions_required_title, R.string.permissions_required_description_realtime)
    }

    override fun onRealTimePermissionsGranted() {
        view?.startRecognition()
    }
}
