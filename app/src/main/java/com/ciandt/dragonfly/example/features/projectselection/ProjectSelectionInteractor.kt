package com.ciandt.dragonfly.example.features.projectselection

import android.content.Context
import android.os.AsyncTask
import com.ciandt.dragonfly.data.model.Model
import com.ciandt.dragonfly.data.model.ModelManager
import com.ciandt.dragonfly.example.data.ProjectRepository
import com.ciandt.dragonfly.example.models.Project

class ProjectSelectionInteractor(val context: Context) : ProjectSelectionContract.Interactor {

    override fun loadModels(onSuccess: (List<Model>) -> Unit, onFailure: (Exception) -> Unit) {
        onSuccess(ModelManager.loadModels())
    }

    override fun loadProjects(onSuccess: (List<Project>) -> Unit, onFailure: (Exception) -> Unit) {
        LoadProjectsTask(context, onSuccess, onFailure).execute()
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