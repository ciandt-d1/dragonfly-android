package com.ciandt.dragonfly.example.features.feedback

/**
 * Created by iluz on 6/28/17.
 */
interface SaveImageToGalleryContract {
    interface Interactor {
        fun save(cameraSnapshot: com.ciandt.dragonfly.lens.data.DragonflyCameraSnapshot)

        fun setOnSaveImageSuccessCallback(callback: (() -> Unit)?)

        fun setOnSaveImageErrorCallback(callback: (() -> Unit)?)
    }
}
