package com.ciandt.dragonfly.example.features.feedback

import android.os.AsyncTask
import com.ciandt.dragonfly.example.config.Benchmark
import com.ciandt.dragonfly.example.config.Network
import com.ciandt.dragonfly.example.data.ClassificationRepository
import com.ciandt.dragonfly.example.features.feedback.model.BenchmarkResult
import com.ciandt.dragonfly.example.helpers.ImageHelper
import com.ciandt.dragonfly.lens.data.DragonflyClassificationInput

class BenchmarkInteractor : BenchmarkContract.Interactor {

    override fun benchmark(input: DragonflyClassificationInput, onSuccess: (BenchmarkResult) -> Unit, onFailure: (Exception) -> Unit) {
        BenchmarkTask(input, onSuccess, onFailure).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
    }

    private data class BenchmarkTaskResult(val result: BenchmarkResult, val exception: Exception?) {
        fun isSuccessful(): Boolean = exception == null
    }

    private class BenchmarkTask(private var input: DragonflyClassificationInput, private var onSuccess: (BenchmarkResult) -> Unit, private var onFailure: (Exception) -> Unit) : AsyncTask<Void, Void, BenchmarkTaskResult>() {

        val repository = ClassificationRepository(Network.BASE_URL)

        override fun doInBackground(vararg params: Void?): BenchmarkTaskResult {
            try {
                val image = ImageHelper.encodeToBase64(input.imagePath, Benchmark.IMAGE_FORMAT, Benchmark.IMAGE_QUALITY) ?: throw RuntimeException("Image could not be converted to base64")
                val result = repository.benchmark(image)
                return BenchmarkTaskResult(result, null)
            } catch (e: Exception) {
                return BenchmarkTaskResult(BenchmarkResult(), e)
            }
        }

        override fun onPostExecute(result: BenchmarkTaskResult) {
            super.onPostExecute(result)

            if (result.isSuccessful()) {
                onSuccess(result.result)
            } else {
                onFailure(result.exception!!)
            }
        }
    }
}