package com.ciandt.dragonfly.example.data.mapper

import com.ciandt.dragonfly.data.model.Model
import com.ciandt.dragonfly.example.models.Project
import com.ciandt.dragonfly.example.models.Version

class ProjectToLibraryModelMapper(val project: Project) : Mapper<Model>() {

    override fun map(): Model = with(project) {

        val lastVersion = lastVersion ?: Version()

        val model = Model(id)

        model.name = name
        model.description = description
        model.colors = colors.toTypedArray()
        model.version = lastVersion.version
        model.size = lastVersion.size

        model.modelPath = lastVersion.modelPath
        model.labelsPath = lastVersion.labelPath

        model.inputSize = lastVersion.inputSize
        model.imageMean = lastVersion.imageMean
        model.imageStd = lastVersion.imageStd
        model.inputName = lastVersion.inputName
        model.outputName = lastVersion.outputName

        return model
    }
}