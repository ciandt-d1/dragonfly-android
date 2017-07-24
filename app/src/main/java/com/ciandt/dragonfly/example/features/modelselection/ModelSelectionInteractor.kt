package com.ciandt.dragonfly.example.features.modelselection

import android.content.Context
import android.net.Uri
import android.os.AsyncTask
import android.support.v4.os.AsyncTaskCompat
import com.ciandt.dragonfly.data.model.Model
import com.ciandt.dragonfly.data.model.ModelManager
import com.ciandt.dragonfly.example.features.download.DownloadHelper
import com.google.android.gms.tasks.Task
import com.google.firebase.storage.FirebaseStorage

class ModelSelectionInteractor(val context: Context, val firebaseStorage: FirebaseStorage) : ModelSelectionContract.Interactor {

    override fun loadModels(onSuccess: (List<Model>) -> Unit, onFailure: (Exception) -> Unit) {
        val task = LoadModelsTask(onSuccess, onFailure)
        AsyncTaskCompat.executeParallel(task)
    }

    data class LoadModelsResult(val models: List<Model>, val exception: Exception?) {
        fun isSuccessful(): Boolean = exception == null
    }

    private class LoadModelsTask(private var onSuccess: (List<Model>) -> Unit, private var onFailure: (Exception) -> Unit) : AsyncTask<Void, Void, LoadModelsResult>() {

        override fun doInBackground(vararg params: Void?): LoadModelsResult {
            try {
                val models = ModelManager.loadModels()
                return LoadModelsResult(models, null)
            } catch (e: Exception) {
                return LoadModelsResult(ArrayList<Model>(), e)
            }
        }

        override fun onPostExecute(result: LoadModelsResult) {
            super.onPostExecute(result)

            if (result.isSuccessful()) {
                onSuccess(result.models)
            } else {
                onFailure(result.exception!!)
            }
        }
    }

    override fun downloadModel(model: Model, onFailure: (Exception) -> Unit) {

        val storageRef = firebaseStorage.getReferenceFromUrl(model.downloadUrl)

        storageRef.downloadUrl.addOnCompleteListener { task: Task<Uri> ->

            if (task.isSuccessful) {
                DownloadHelper.download(context, model.name, task.result)
            } else {
                onFailure(task.exception!!)
            }
        }

    }
}