package com.ciandt.dragonfly.example.models

data class Version(
        var project: String = "",
        var version: Int = 0,
        var size: Long = 0L,
        var inputSize: Int = 0,
        var imageMean: Int = 0,
        var imageStd: Float = 0.0f,
        var inputName: String = "",
        var outputName: String = "",
        var downloadUrl: String = "",
        var createdAt: Long = 0L,
        var modelPath: String = "",
        var labelPath: String = "",
        var status: Int = 0
)