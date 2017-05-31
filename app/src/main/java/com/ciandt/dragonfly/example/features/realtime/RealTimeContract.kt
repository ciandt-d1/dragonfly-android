package com.ciandt.dragonfly.example.features.realtime

import android.support.annotation.StringRes
import com.ciandt.dragonfly.example.shared.BasePresenterContract

interface RealTimeContract {

    interface View {
        fun checkRealTimeRequiredPermissions()

        fun startRecognition()

        fun showRealTimePermissionsRequiredAlert(@StringRes title: Int, @StringRes message: Int)
    }

    interface Presenter : BasePresenterContract<View> {

        fun onRealTimePermissionsGranted()

        fun onRealTimePermissionsDenied(permanently: Boolean)
    }
}
