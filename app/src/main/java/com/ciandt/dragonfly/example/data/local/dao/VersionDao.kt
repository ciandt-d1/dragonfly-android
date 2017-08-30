package com.ciandt.dragonfly.example.data.local.dao

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Insert
import android.arch.persistence.room.OnConflictStrategy
import android.arch.persistence.room.Query
import com.ciandt.dragonfly.example.data.local.entities.VersionEntity

@Dao
interface VersionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(version: VersionEntity)

    @Query("DELETE " +
            "FROM versions " +
            "WHERE project = :arg0")
    fun delete(project: String)

    @Query("SELECT * " +
            "FROM versions " +
            "WHERE project = :arg0 " +
            "AND version = :arg1 " +
            "AND status != 0")
    fun downloadingOrDownloaded(project: String, version: Int): VersionEntity?

    @Query("UPDATE versions " +
            "SET status = :arg2 " +
            "WHERE project = :arg0 " +
            "AND version = :arg1")
    fun updateStatus(project: String, version: Int, status: Int)

    @Query("DELETE " +
            "FROM versions")
    fun clear()

    @Query("SELECT versions.* " +
            "FROM versions, downloads " +
            "WHERE versions.project = downloads.project " +
            "AND versions.version = downloads.version " +
            "AND downloads.id = :arg0")
    fun getByDownload(id: Long): VersionEntity?
}