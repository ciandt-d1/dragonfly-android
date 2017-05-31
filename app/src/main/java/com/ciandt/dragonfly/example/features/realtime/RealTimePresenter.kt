package com.ciandt.dragonfly.example.features.realtime

import com.ciandt.dragonfly.example.R
import com.ciandt.dragonfly.example.config.PreferenceKeys
import com.ciandt.dragonfly.example.infrastructure.PreferencesRepository
import com.ciandt.dragonfly.example.shared.BasePresenter

class RealTimePresenter(preferencesRepository: PreferencesRepository) : BasePresenter<RealTimeContract.View>(), RealTimeContract.Presenter {
    val preferenceRepository: PreferencesRepository = preferencesRepository

    override fun attachView(view: RealTimeContract.View) {
        super.attachView(view)

        if (preferenceRepository.getBoolean(PreferenceKeys.REAL_TIME_PERMISSIONS_PERMANENTLY_DENIED, false)) {
            view.showRealTimePermissionsRequiredAlert(R.string.permissions_required_title, R.string.permissions_required_description_realtime)
        } else {
            view.checkRealTimeRequiredPermissions()
        }
    }

    override fun onRealTimePermissionsDenied(permanently: Boolean) {
        if (permanently) {
            preferenceRepository.putBoolean(PreferenceKeys.REAL_TIME_PERMISSIONS_PERMANENTLY_DENIED, true)
            view?.showRealTimePermissionsRequiredAlert(R.string.permissions_required_title, R.string.permissions_required_description_realtime)
        } else {
            view?.checkRealTimeRequiredPermissions()
        }
    }

    override fun onRealTimePermissionsGranted() {
        preferenceRepository.remove(PreferenceKeys.REAL_TIME_PERMISSIONS_PERMANENTLY_DENIED)
        view?.startRecognition()
    }
}
