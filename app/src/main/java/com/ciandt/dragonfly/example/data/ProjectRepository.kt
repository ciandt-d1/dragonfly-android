package com.ciandt.dragonfly.example.data

import android.content.Context
import com.ciandt.dragonfly.example.data.local.LocalDataSource

class ProjectRepository(context: Context) {

    private val localDataSource = LocalDataSource(context)

    fun clear() {
        localDataSource.clear()
    }
}