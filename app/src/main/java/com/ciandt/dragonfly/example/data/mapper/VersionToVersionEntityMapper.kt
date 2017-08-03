package com.ciandt.dragonfly.example.data.mapper

import com.ciandt.dragonfly.example.data.local.entities.VersionEntity
import com.ciandt.dragonfly.example.models.Version

class VersionToVersionEntityMapper(val version: Version) : Mapper<VersionEntity>() {

    override fun map(): VersionEntity = with(version) {
        return VersionEntity(
                project,
                version,
                size,
                inputSize,
                imageMean,
                imageStd,
                inputName,
                outputName,
                downloadUrl,
                createdAt,
                modelPath,
                labelsPath,
                status
        )
    }
}