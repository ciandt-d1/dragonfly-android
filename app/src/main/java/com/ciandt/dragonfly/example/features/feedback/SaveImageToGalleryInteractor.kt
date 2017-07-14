package com.ciandt.dragonfly.example.features.feedback

import android.content.ContentValues
import android.content.Context
import android.os.AsyncTask
import android.provider.MediaStore
import android.support.v4.os.AsyncTaskCompat
import com.ciandt.dragonfly.base.ui.BaseInteractorContract
import com.ciandt.dragonfly.example.infrastructure.extensions.lastSegment
import com.ciandt.dragonfly.image_processing.ImageUtils
import com.ciandt.dragonfly.lens.data.DragonflyClassificationInput
import java.io.File

/**
 * Created by iluz on 6/28/17.
 */

class SaveImageToGalleryInteractor(val context: Context) : SaveImageToGalleryContract.Interactor {
    private var onSaveImageSuccessCallback: (() -> Unit)? = null
    private var onSaveImageErrorCallback: (() -> Unit)? = null

    override fun save(classificationInput: DragonflyClassificationInput) {
        val params = SaveImageToGalleryTask.TaskParams(context, classificationInput)
        AsyncTaskCompat.executeParallel(SaveImageToGalleryTask(this), params)
    }

    override fun setOnSaveImageSuccessCallback(callback: (() -> Unit)?) {
        onSaveImageSuccessCallback = callback
    }

    override fun setOnSaveImageErrorCallback(callback: (() -> Unit)?) {
        onSaveImageErrorCallback = callback
    }

    companion object {
        private class SaveImageToGalleryTask(val interactor: SaveImageToGalleryInteractor) : AsyncTask<SaveImageToGalleryTask.TaskParams, Void, BaseInteractorContract.AsyncTaskResult<Boolean, Exception?>>() {
            override fun doInBackground(vararg params: TaskParams): BaseInteractorContract.AsyncTaskResult<Boolean, Exception?> {
                val taskParams = params[0]

                try {
                    val values = ContentValues()

                    val name = taskParams.classificationInput.imagePath.lastSegment(File.separator)

                    val publicPath = ImageUtils.saveBitmapToGallery(name)

                    values.put(MediaStore.Images.Media.TITLE, name)
                    values.put(MediaStore.Images.Media.DISPLAY_NAME, name)
                    values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                    values.put(MediaStore.MediaColumns.DATA, publicPath)

                    // Add the date meta data to ensure the image is added at the front of the gallery
                    values.put(MediaStore.Images.Media.DATE_ADDED, System.currentTimeMillis())
                    values.put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis())

                    taskParams.context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)

                    return BaseInteractorContract.AsyncTaskResult(true, null)
                } catch (e: Exception) {
                    return BaseInteractorContract.AsyncTaskResult(false, e)
                }
            }

            override fun onPostExecute(result: BaseInteractorContract.AsyncTaskResult<Boolean, Exception?>) {
                if (result.hasError()) {
                    interactor.onSaveImageErrorCallback?.invoke()
                } else {
                    interactor.onSaveImageSuccessCallback?.invoke()
                }
            }

            class TaskParams(val context: Context, val classificationInput: DragonflyClassificationInput)
        }
    }
}
