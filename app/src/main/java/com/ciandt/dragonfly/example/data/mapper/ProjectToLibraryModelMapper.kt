package com.ciandt.dragonfly.example.data.mapper

import com.ciandt.dragonfly.data.model.Model
import com.ciandt.dragonfly.example.models.Project
import com.ciandt.dragonfly.example.models.Version

class ProjectToLibraryModelMapper(val project: Project) : Mapper<Model>() {

    override fun map(): Model {

        val lastVersion = project.lastVersion ?: Version()

        return Model(project.id).apply {
            name = project.name
            description = project.description
            colors = project.colors.toTypedArray()
            version = lastVersion.version
            size = lastVersion.size
            modelPath = lastVersion.modelPath
            labelsPath = lastVersion.labelPath
            inputSize = lastVersion.inputSize
            imageMean = lastVersion.imageMean
            imageStd = lastVersion.imageStd
            inputName = lastVersion.inputName
            outputName = lastVersion.outputName
        }
    }
}