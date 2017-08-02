package com.ciandt.dragonfly.example.infrastructure.extensions

import android.os.AsyncTask

fun <Params, Progress, Result> AsyncTask<Params, Progress, Result>.executeParallel() {
    this.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
}
