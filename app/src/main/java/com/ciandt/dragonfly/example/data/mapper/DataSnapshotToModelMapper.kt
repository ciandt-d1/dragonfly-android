package com.ciandt.dragonfly.example.data.mapper

import com.ciandt.dragonfly.example.data.local.entities.Model
import com.ciandt.dragonfly.example.data.local.entities.Version
import com.ciandt.dragonfly.example.infrastructure.DragonflyLogger
import com.google.firebase.database.DataSnapshot

class DataSnapshotToModelMapper(val dataSnapshot: DataSnapshot) : Mapper<Model>() {

    override fun map(): Model? {

        listOf("name", "description", "colors", "versions").forEach {
            if (!dataSnapshot.hasChild(it)) {
                DragonflyLogger.error(LOG_TAG, IllegalArgumentException("child '$it' not found"))
                return null
            }
        }

        try {
            val model = Model(
                    id = dataSnapshot.key,
                    name = dataSnapshot.child("name").getValue(String::class.java)!!,
                    description = dataSnapshot.child("description").getValue(String::class.java)!!
            )

            val colors = ArrayList<String>()
            dataSnapshot.child("colors").children.forEach {
                colors.add(it.getValue(String::class.java)!!)
            }

            val versions = ArrayList<Version>()
            dataSnapshot.child("versions").children.forEach { child ->
                mapVersion(dataSnapshot.key, child)?.let { version ->
                    versions.add(version)
                }
            }

            model.colors = colors.joinToString(",")
            model.versions = versions
            return model

        } catch (e: Exception) {
            DragonflyLogger.error(LOG_TAG, e.message, e)
            return null
        }
    }

    private fun mapVersion(idModel: String, dataSnapshot: DataSnapshot): Version? {
        try {
            val version = dataSnapshot.getValue(Version::class.java)!!
            version.idModel = idModel
            return version

        } catch (e: Exception) {
            DragonflyLogger.error(LOG_TAG, e.message, e)
            return null
        }
    }

    companion object {
        private val LOG_TAG = DataSnapshotToModelMapper::class.java.simpleName
    }
}