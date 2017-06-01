package com.ciandt.dragonfly.example.features.modelselection

import android.os.AsyncTask
import android.support.v4.os.AsyncTaskCompat
import com.ciandt.dragonfly.data.Model
import com.ciandt.dragonfly.data.ModelManager

class ModelSelectionInteractor : ModelSelectionContract.Interactor {

    override fun loadModels(onSuccess: (List<Model>) -> Unit, onFailure: (Exception) -> Unit) {
        val task = LoadModelsTask(onSuccess, onFailure)
        AsyncTaskCompat.executeParallel(task)
    }

    data class LoadModelsResult(val models: List<Model>, val exception: Exception?)

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

            if (result.exception != null) {
                onFailure(result.exception)
            } else {
                onSuccess(result.models)
            }
        }
    }
}