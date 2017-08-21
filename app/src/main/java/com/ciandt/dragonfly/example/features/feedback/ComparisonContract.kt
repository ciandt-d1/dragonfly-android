package com.ciandt.dragonfly.example.features.feedback

import com.ciandt.dragonfly.example.features.feedback.model.ComparisonResult
import com.ciandt.dragonfly.lens.data.DragonflyClassificationInput

interface ComparisonContract {
    interface Interactor {
        fun compareServices(input: DragonflyClassificationInput, onSuccess: (ComparisonResult) -> Unit, onFailure: (Exception) -> Unit)
    }
}
