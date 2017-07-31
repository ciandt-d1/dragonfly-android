package com.ciandt.dragonfly.example.data.remote

import com.ciandt.dragonfly.example.data.local.LocalModelDataSource
import com.ciandt.dragonfly.example.data.mapper.DataSnapshotToModelMapper
import com.ciandt.dragonfly.example.infrastructure.DragonflyLogger
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError

class RemoteModelListener(private val localModelDataSource: LocalModelDataSource) : ChildEventListener {

    override fun onChildAdded(dataSnapshot: DataSnapshot, prevChildKey: String?) {
        val model = DataSnapshotToModelMapper(dataSnapshot).map()
        model?.let {
            runOnBackgroundThread {
                localModelDataSource.save(it)
            }
        }
    }

    override fun onChildChanged(dataSnapshot: DataSnapshot, prevChildKey: String?) {
        onChildAdded(dataSnapshot, prevChildKey)
    }

    override fun onChildRemoved(dataSnapshot: DataSnapshot) {
        val model = DataSnapshotToModelMapper(dataSnapshot).map()
        model?.let {
            runOnBackgroundThread {
                localModelDataSource.delete(it)
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
        private val LOG_TAG = RemoteModelListener::class.java.simpleName
    }
}