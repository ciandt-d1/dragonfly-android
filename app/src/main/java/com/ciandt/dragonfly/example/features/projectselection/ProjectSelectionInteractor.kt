package com.ciandt.dragonfly.example.features.projectselection

import android.content.Context
import android.net.Uri
import android.os.AsyncTask
import com.ciandt.dragonfly.example.data.ProjectRepository
import com.ciandt.dragonfly.example.features.download.DownloadHelper
import com.ciandt.dragonfly.example.features.download.ProjectChangedReceiver
import com.ciandt.dragonfly.example.infrastructure.extensions.getLocalBroadcastManager
import com.ciandt.dragonfly.example.models.Project
import com.ciandt.dragonfly.example.models.Version
import com.google.android.gms.tasks.Task
import com.google.firebase.storage.FirebaseStorage

class ProjectSelectionInteractor(val context: Context, val firebaseStorage: FirebaseStorage) : ProjectSelectionContract.Interactor {

    private var receiver: ProjectChangedReceiver? = null

    override fun loadProjects(onSuccess: (List<Project>) -> Unit, onFailure: (Exception) -> Unit) {
        LoadProjectsTask(context, onSuccess, onFailure).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
    }

    override fun downloadVersion(title: String, version: Version, onFailure: (Exception) -> Unit) {
        val storageRef = firebaseStorage.reference.child(version.downloadUrl)
        storageRef.downloadUrl.addOnCompleteListener { task: Task<Uri> ->
            if (task.isSuccessful) {
                DownloadHelper.startDownload(context, title, version, task.result)
            } else {
                onFailure(task.exception!!)
            }
        }
    }

    override fun registerProjectObserver(onChanged: (Project) -> Unit) {
        receiver = ProjectChangedReceiver(onChanged)
        context.getLocalBroadcastManager().registerReceiver(receiver, ProjectChangedReceiver.getIntentFilter())
    }

    override fun unregisterProjectObserver() {
        receiver?.let {
            context.getLocalBroadcastManager().unregisterReceiver(it)
        }
    }

    private data class LoadProjectsResult(val projects: List<Project>, val exception: Exception?) {
        fun isSuccessful(): Boolean = exception == null
    }

    private class LoadProjectsTask(context: Context, private var onSuccess: (List<Project>) -> Unit, private var onFailure: (Exception) -> Unit) : AsyncTask<Void, Void, LoadProjectsResult>() {

        private val repository = ProjectRepository(context)

        override fun doInBackground(vararg params: Void?): LoadProjectsResult {
            try {
                val projects = repository.getProjects()
                return LoadProjectsResult(projects, null)
            } catch (e: Exception) {
                return LoadProjectsResult(emptyList(), e)
            }
        }

        override fun onPostExecute(result: LoadProjectsResult) {
            super.onPostExecute(result)

            if (result.isSuccessful()) {
                onSuccess(result.projects)
            } else {
                onFailure(result.exception!!)
            }
        }
    }
}