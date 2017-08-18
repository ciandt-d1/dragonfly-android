package com.ciandt.dragonfly.example.features.feedback.model

import com.ciandt.dragonfly.tensorflow.Classifier

class ComparisonResult {

    val services: MutableList<Service> = mutableListOf()

    fun addService(service: Service) {
        services.add(service)
    }

    data class Service(
            var id: String = "",
            var name: String = "",
            var classifications: MutableList<Classifier.Classification> = mutableListOf()
    )
}

