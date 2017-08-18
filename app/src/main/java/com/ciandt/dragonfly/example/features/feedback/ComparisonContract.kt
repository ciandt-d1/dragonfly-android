package com.ciandt.dragonfly.example.features.feedback

import com.ciandt.dragonfly.example.features.feedback.model.ComparisonResult

interface ComparisonContract {
    interface Interactor {
        fun compareServices(onSuccess: (ComparisonResult) -> Unit, onFailure: (Exception) -> Unit)
    }
}
