package com.ciandt.dragonfly.example.features.realtime

import com.ciandt.dragonfly.example.shared.BasePresenterContract

interface RealTimeContract {

    interface View {
        fun requestRealTimePermissions()

        fun startRecognition()

        fun showRealTimePermissionsError(title: String, message: String)
    }

    interface Presenter : BasePresenterContract<View> {

        fun onRealTimePermissionsGranted()

        fun onRealTimePermissionsDenied()
    }
}
