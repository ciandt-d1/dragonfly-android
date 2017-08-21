package com.ciandt.dragonfly.example.data.mapper

import com.ciandt.dragonfly.example.data.remote.entities.BenchmarkService
import com.ciandt.dragonfly.example.features.feedback.model.BenchmarkResult
import com.ciandt.dragonfly.tensorflow.Classifier

class BenchmarkServicesToBenchmarkResult(val services: List<BenchmarkService>) : Mapper<BenchmarkResult>() {

    override fun map(): BenchmarkResult {
        val result = BenchmarkResult()

        services.forEach each@ {
            if (it.id == null || it.name == null) {
                return@each
            }

            val service = BenchmarkResult.BenchmarkService(it.id!!, it.name!!)
            it.classifications.forEachIndexed { index, (label, score) ->
                service.classifications.add(Classifier.Classification(index.toString(), label, score, null))
            }

            result.addBenchmarkService(service)
        }

        return result
    }
}