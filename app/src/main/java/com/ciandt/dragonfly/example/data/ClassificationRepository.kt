package com.ciandt.dragonfly.example.data

import com.ciandt.dragonfly.example.data.mapper.BenchmarkServicesToBenchmarkResult
import com.ciandt.dragonfly.example.data.remote.RemoteDataSource
import com.ciandt.dragonfly.example.features.feedback.model.BenchmarkResult

class ClassificationRepository(baseUrl: String) {

    private val remoteDataSource = RemoteDataSource(baseUrl)

    fun benchmark(image: String): BenchmarkResult {
        val benchmarks = remoteDataSource.benchmark(image)
        return BenchmarkServicesToBenchmarkResult(benchmarks).map()
    }
}