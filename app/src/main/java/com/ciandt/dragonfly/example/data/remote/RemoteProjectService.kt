package com.ciandt.dragonfly.example.data.remote

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import com.ciandt.dragonfly.example.data.local.LocalDataSource
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class RemoteProjectService : Service() {

    private val PROJECTS_COLLECTION = "v1/projects"

    private lateinit var databaseRef: DatabaseReference

    private lateinit var listener: RemoteProjectListener

    override fun onCreate() {
        super.onCreate()

        listener = RemoteProjectListener(LocalDataSource(this))

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
