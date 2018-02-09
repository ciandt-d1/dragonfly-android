package com.ciandt.dragonfly.example.data.mapper

import com.ciandt.dragonfly.example.data.local.entities.VersionEntity
import com.ciandt.dragonfly.example.models.Version

class VersionEntityToVersionMapper(val entity: VersionEntity) : Mapper<Version>() {

    override fun map(): Version? = with(entity) {
        return Version(
                project,
                version,
                size,
                inputSize,
                imageMean,
                imageStd,
                inputName,
                outputNames,
                outputDisplayNames,
                closedSet,
                downloadUrl,
                createdAt,
                modelPath,
                labelFilesPaths,
                status
        )
    }
}