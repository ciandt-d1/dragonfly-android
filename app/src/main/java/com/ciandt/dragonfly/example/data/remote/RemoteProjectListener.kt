package com.ciandt.dragonfly.example.data.remote

import com.ciandt.dragonfly.example.data.local.LocalDataSource
import com.ciandt.dragonfly.example.data.mapper.DataSnapshotToProjectEntityMapper
import com.ciandt.dragonfly.example.infrastructure.DragonflyLogger
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError

class RemoteProjectListener(private val localDataSource: LocalDataSource) : ChildEventListener {

    override fun onChildAdded(dataSnapshot: DataSnapshot, prevChildKey: String?) {
        val project = DataSnapshotToProjectEntityMapper(dataSnapshot).map()
        project?.let {
            runOnBackgroundThread {
                localDataSource.save(it)
            }
        }
    }

    override fun onChildChanged(dataSnapshot: DataSnapshot, prevChildKey: String?) {
        onChildAdded(dataSnapshot, prevChildKey)
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

    fun runOnBackgroundThread(action: () -> Unit) {
        Thread(Runnable { action() }).start()
    }

    companion object {
        private val LOG_TAG = RemoteProjectListener::class.java.simpleName
    }
}