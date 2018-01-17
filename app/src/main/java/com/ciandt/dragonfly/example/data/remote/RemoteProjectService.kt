package com.ciandt.dragonfly.example.data.remote

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import com.ciandt.dragonfly.example.data.DatabaseManager
import com.ciandt.dragonfly.example.data.local.LocalDataSource
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class RemoteProjectService : Service() {

    private lateinit var databaseRef: DatabaseReference

    private lateinit var listener: RemoteProjectListener

    override fun onCreate() {
        super.onCreate()

        listener = RemoteProjectListener(this, LocalDataSource(DatabaseManager.database))

        databaseRef = FirebaseDatabase.getInstance().getReference(PROJECTS_COLLECTION)
        databaseRef.addChildEventListener(listener)
    }

    override fun onDestroy() {
        databaseRef.removeEventListener(listener)
        super.onDestroy()
    }

    override fun onBind(i: Intent?): IBinder? {
        return null
    }

    companion object {

        private val PROJECTS_COLLECTION = "v2/projects"

        fun start(context: Context) {
            val intent = Intent(context, RemoteProjectService::class.java)
            context.startService(intent)
        }

        fun stop(context: Context) {
            val intent = Intent(context, RemoteProjectService::class.java)
            context.stopService(intent)
        }
    }
}
