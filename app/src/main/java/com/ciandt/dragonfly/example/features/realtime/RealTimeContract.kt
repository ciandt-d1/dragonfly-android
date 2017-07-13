package com.ciandt.dragonfly.example.features.realtime

import android.support.annotation.StringRes
import com.ciandt.dragonfly.example.shared.BasePresenterContract

interface RealTimeContract {

    interface View {
        fun checkRealTimeRequiredPermissions()

        fun startRealTimeClassification()

        fun showPermissionsRequiredAlert(@StringRes title: Int, @StringRes message: Int)

        fun selectImageFromLibrary()
    }

    interface Presenter : BasePresenterContract<View> {

        fun onRealTimePermissionsGranted()

        fun onRealTimePermissionsDenied(permanently: Boolean)

        fun classifyExistingPicture()
    }
}
