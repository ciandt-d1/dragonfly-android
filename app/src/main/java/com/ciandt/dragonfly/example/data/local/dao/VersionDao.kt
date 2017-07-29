package com.ciandt.dragonfly.example.data.local.dao

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Insert
import android.arch.persistence.room.OnConflictStrategy
import android.arch.persistence.room.Query
import com.ciandt.dragonfly.example.data.local.entities.Version

@Dao
interface VersionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(version: Version)

    @Query("DELETE FROM versions WHERE idModel = :arg0")
    fun delete(idModel: String)

    @Query("SELECT * FROM versions WHERE idModel = :arg0 AND version = :arg1 AND status != 0")
    fun downloadingOrDownloaded(idModel: String, version: Int): Version?

    @Query("DELETE FROM versions")
    fun clear()
}