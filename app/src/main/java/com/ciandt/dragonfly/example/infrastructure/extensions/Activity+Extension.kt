package com.ciandt.dragonfly.example.infrastructure.extensions

import android.app.Activity
import android.support.annotation.StringRes
import android.support.design.widget.Snackbar
import android.view.ViewGroup
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability


fun Activity.getRootView(): ViewGroup {
    return window.decorView.findViewById(android.R.id.content)
}

fun Activity.showSnackbar(text: CharSequence, duration: Int = Snackbar.LENGTH_LONG) {
    Snackbar.make(getRootView(), text, duration).show()
}

fun Activity.showSnackbar(@StringRes resId: Int, duration: Int = Snackbar.LENGTH_LONG) {
    showSnackbar(getString(resId), duration)
}

fun Activity.proceedOnlyIfRequiredGooglePlayServicesIsAvailable() {
    val googleApiAvailability = GoogleApiAvailability.getInstance()
    val googlePlayServicesCheck = googleApiAvailability.isGooglePlayServicesAvailable(this)

    when (googlePlayServicesCheck) {
        ConnectionResult.SERVICE_DISABLED,
        ConnectionResult.SERVICE_INVALID,
        ConnectionResult.SERVICE_MISSING,
        ConnectionResult.SERVICE_VERSION_UPDATE_REQUIRED -> {
            val dialog = googleApiAvailability.getErrorDialog(this, googlePlayServicesCheck, 0)
            dialog.setOnCancelListener { this.finish() }
            dialog.show()
        }
    }
}