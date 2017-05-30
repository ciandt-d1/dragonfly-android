package com.ciandt.dragonfly.example.features.realtime

import android.support.annotation.StringRes
import com.ciandt.dragonfly.example.shared.BasePresenterContract

interface RealTimeContract {

    interface View {
        fun requestRealTimePermissions()

        fun startRecognition()

        fun showRealTimePermissionsError(@StringRes title: Int, @StringRes message: Int)
    }

    interface Presenter : BasePresenterContract<View> {

        fun onRealTimePermissionsGranted()

        fun onRealTimePermissionsDenied()

        fun onRealTimePermissionsPermanentlyDenied()
    }
}
