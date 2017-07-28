package com.ciandt.dragonfly.example.data.remote

import com.ciandt.dragonfly.example.infrastructure.DragonflyLogger
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError

class RemoteModelListener : ChildEventListener {

    override fun onCancelled(databaseError: DatabaseError) {
        DragonflyLogger.warn(LOG_TAG, databaseError.message)
    }

    override fun onChildMoved(dataSnapshot: DataSnapshot, prevChildKey: String?) {
    }

    override fun onChildChanged(dataSnapshot: DataSnapshot, prevChildKey: String?) {
    }

    override fun onChildAdded(dataSnapshot: DataSnapshot, prevChildKey: String?) {
    }

    override fun onChildRemoved(dataSnapshot: DataSnapshot) {
    }

    companion object {
        private val LOG_TAG = RemoteModelListener::class.java.simpleName
    }
}