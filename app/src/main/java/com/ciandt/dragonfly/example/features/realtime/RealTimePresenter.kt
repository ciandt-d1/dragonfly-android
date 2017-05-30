package com.ciandt.dragonfly.example.features.realtime

import com.ciandt.dragonfly.example.shared.BasePresenter

class RealTimePresenter : BasePresenter<RealTimeContract.View>(), RealTimeContract.Presenter {
    override fun attachView(view: RealTimeContract.View) {
        super.attachView(view)
        view.requestRealTimePermissions();
    }

    override fun onRealTimePermissionsDenied() {
        view?.showRealTimePermissionsError("Permissions required", "Both camera and write to external storage permissions are needed.");
    }

    override fun onRealTimePermissionsGranted() {
        view?.startRecognition();
    }
}
