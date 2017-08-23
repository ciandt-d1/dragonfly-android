package com.ciandt.dragonfly.example.data.remote

import android.content.Context
import com.ciandt.dragonfly.example.data.local.LocalDataSource
import com.ciandt.dragonfly.example.data.mapper.DataSnapshotToProjectEntityMapper
import com.ciandt.dragonfly.example.features.projectselection.ProjectListChangedReceiver
import com.ciandt.dragonfly.example.infrastructure.DragonflyLogger
import com.ciandt.dragonfly.example.infrastructure.extensions.getLocalBroadcastManager
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError

class RemoteProjectListener(private val context: Context, private val localDataSource: LocalDataSource) : ChildEventListener {

    override fun onChildAdded(dataSnapshot: DataSnapshot, prevChildKey: String?) {
        val project = DataSnapshotToProjectEntityMapper(dataSnapshot).map()
        project?.let {
            runOnBackgroundThread {
                localDataSource.save(it)
                sendBroadcast()
            }
        }
    }

    override fun onChildChanged(dataSnapshot: DataSnapshot, prevChildKey: String?) {
        val project = DataSnapshotToProjectEntityMapper(dataSnapshot).map()
        project?.let {
            runOnBackgroundThread {
                localDataSource.update(it)
                sendBroadcast()
            }
        }
    }

    override fun onChildRemoved(dataSnapshot: DataSnapshot) {
        val project = DataSnapshotToProjectEntityMapper(dataSnapshot).map()
        project?.let {
            runOnBackgroundThread {
                localDataSource.delete(it)
            }
        }
    }

    override fun onChildMoved(dataSnapshot: DataSnapshot, prevChildKey: String?) {
    }

    override fun onCancelled(databaseError: DatabaseError) {
        DragonflyLogger.warn(LOG_TAG, databaseError.message)
    }

    private fun sendBroadcast() {
        context.getLocalBroadcastManager().sendBroadcast(ProjectListChangedReceiver.create(System.currentTimeMillis()))
    }

    private fun runOnBackgroundThread(action: () -> Unit) {
        Thread(Runnable { action() }).start()
    }

    companion object {
        private val LOG_TAG = RemoteProjectListener::class.java.simpleName
    }
}