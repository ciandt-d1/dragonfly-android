package com.ciandt.dragonfly.example.data

import com.ciandt.dragonfly.example.data.remote.RemoteDataSource
import com.ciandt.dragonfly.example.data.remote.entities.Service

class ClassificationRepository(baseUrl: String) {

    private val remoteDataSource = RemoteDataSource(baseUrl)

    fun compareServices(image: String): List<Service> {
        return remoteDataSource.compareServices(image)
    }
}