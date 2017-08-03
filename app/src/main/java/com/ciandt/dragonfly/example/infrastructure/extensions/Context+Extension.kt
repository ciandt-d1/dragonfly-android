package com.ciandt.dragonfly.example.infrastructure.extensions

import android.app.DownloadManager
import android.content.Context
import android.net.ConnectivityManager
import android.support.v4.content.LocalBroadcastManager
import android.view.LayoutInflater
import android.view.inputmethod.InputMethodManager

fun Context.getLayoutInflaterService(): LayoutInflater {
    return getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
}

fun Context.getInputMethodService(): InputMethodManager {
    return getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
}

fun Context.getDownloadManager(): DownloadManager {
    return getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
}

fun Context.getLocalBroadcastManager(): LocalBroadcastManager {
    return LocalBroadcastManager.getInstance(this)
}

fun Context.isNetworkAvailable(): Boolean {
    val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val activeNetworkInfo = connectivityManager.activeNetworkInfo ?: return false
    return activeNetworkInfo.isAvailable && activeNetworkInfo.isConnected
}