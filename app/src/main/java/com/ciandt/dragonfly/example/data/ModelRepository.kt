package com.ciandt.dragonfly.example.data

import android.content.Context
import com.ciandt.dragonfly.example.data.local.LocalModelDataSource

class ModelRepository(context: Context) {

    private val localDataSource = LocalModelDataSource(context)

    fun clear() {
        localDataSource.clear()
    }
}