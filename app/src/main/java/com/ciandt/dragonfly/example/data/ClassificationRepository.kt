package com.ciandt.dragonfly.example.data

import com.ciandt.dragonfly.example.data.mapper.ServicesToComparisonResult
import com.ciandt.dragonfly.example.data.remote.RemoteDataSource
import com.ciandt.dragonfly.example.features.feedback.model.ComparisonResult

class ClassificationRepository(baseUrl: String) {

    private val remoteDataSource = RemoteDataSource(baseUrl)

    fun compareServices(image: String): ComparisonResult {
        val services = remoteDataSource.compareServices(image)
        return ServicesToComparisonResult(services).map()
    }
}