package com.ciandt.dragonfly.example.data.remote

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import com.ciandt.dragonfly.example.data.local.LocalModelDataSource
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class RemoteModelService : Service() {

    private val MODELS_COLLECTION = "v1/models"

    private lateinit var databaseRef: DatabaseReference

    private lateinit var listener: RemoteModelListener

    override fun onCreate() {
        super.onCreate()

        listener = RemoteModelListener(LocalModelDataSource(this))

        databaseRef = FirebaseDatabase.getInstance().getReference(MODELS_COLLECTION)
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
        fun createIntent(context: Context): Intent {
            return Intent(context, RemoteModelService::class.java)
        }
    }
}
