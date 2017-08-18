package com.ciandt.dragonfly.example.data.mapper

import com.ciandt.dragonfly.example.data.remote.entities.Service
import com.ciandt.dragonfly.example.features.feedback.model.ComparisonResult
import com.ciandt.dragonfly.tensorflow.Classifier

class ServicesToComparisonResult(val services: List<Service>) : Mapper<ComparisonResult>() {

    override fun map(): ComparisonResult {
        val result = ComparisonResult()

        services.forEach each@ {
            if (it.id == null || it.name == null) {
                return@each
            }

            val service = ComparisonResult.Service(it.id!!, it.name!!)
            it.classifications.forEachIndexed { index, (label, score) ->
                service.classifications.add(Classifier.Classification(index.toString(), label, score, null))
            }

            result.addService(service)
        }

        return result
    }
}