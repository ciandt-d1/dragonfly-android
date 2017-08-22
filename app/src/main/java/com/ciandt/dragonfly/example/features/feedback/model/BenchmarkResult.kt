package com.ciandt.dragonfly.example.features.feedback.model

import com.ciandt.dragonfly.tensorflow.Classifier

class BenchmarkResult {

    val benchmarks: MutableList<BenchmarkService> = mutableListOf()

    fun addBenchmarkService(service: BenchmarkService) {
        if (service.classifications.isNotEmpty()) {
            benchmarks.add(service)
        }
    }

    data class BenchmarkService(
            var id: String = "",
            var name: String = "",
            var classifications: MutableList<Classifier.Classification> = mutableListOf()
    )
}

