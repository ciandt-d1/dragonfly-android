package com.ciandt.dragonfly.example.features.feedback

import com.ciandt.dragonfly.example.features.feedback.model.BenchmarkResult
import com.ciandt.dragonfly.lens.data.DragonflyClassificationInput

interface BenchmarkContract {
    interface Interactor {
        fun benchmark(input: DragonflyClassificationInput, onSuccess: (BenchmarkResult) -> Unit, onFailure: (Exception) -> Unit)
    }
}
