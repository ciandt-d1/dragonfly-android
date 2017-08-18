package com.ciandt.dragonfly.example.features.feedback

import android.os.AsyncTask
import com.ciandt.dragonfly.example.config.Network
import com.ciandt.dragonfly.example.data.ClassificationRepository
import com.ciandt.dragonfly.example.features.feedback.model.ComparisonResult

class ComparisonInteractor : ComparisonContract.Interactor {

    override fun compareServices(onSuccess: (ComparisonResult) -> Unit, onFailure: (Exception) -> Unit) {
        CompareServicesTask(onSuccess, onFailure).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
    }

    private data class CompareServicesResult(val comparison: ComparisonResult, val exception: Exception?) {
        fun isSuccessful(): Boolean = exception == null
    }

    private class CompareServicesTask(private var input: DragonflyClassificationInput, private var onSuccess: (ComparisonResult) -> Unit, private var onFailure: (Exception) -> Unit) : AsyncTask<Void, Void, CompareServicesResult>() {
        
        val repository = ClassificationRepository(Network.BASE_URL)

        override fun doInBackground(vararg params: Void?): CompareServicesResult {
            try {
                val result = repository.compareServices("//todo")
                return CompareServicesResult(result, null)
            } catch (e: Exception) {
                return CompareServicesResult(ComparisonResult(), e)
            }
        }

        override fun onPostExecute(result: CompareServicesResult) {
            super.onPostExecute(result)

            if (result.isSuccessful()) {
                onSuccess(result.comparison)
            } else {
                onFailure(result.exception!!)
            }
        }
    }
}