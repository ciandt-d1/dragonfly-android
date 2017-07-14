package com.ciandt.dragonfly.example.features.feedback

import com.ciandt.dragonfly.lens.data.DragonflyClassificationInput

/**
 * Created by iluz on 6/28/17.
 */
interface SaveImageToGalleryContract {
    interface Interactor {
        fun save(classificationInput: DragonflyClassificationInput)

        fun setOnSaveImageSuccessCallback(callback: (() -> Unit)?)

        fun setOnSaveImageErrorCallback(callback: (() -> Unit)?)
    }
}
