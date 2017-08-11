package com.ciandt.dragonfly.example.data.local.dao

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Insert
import android.arch.persistence.room.OnConflictStrategy
import android.arch.persistence.room.Query
import com.ciandt.dragonfly.example.data.local.entities.DownloadEntity

@Dao
interface DownloadDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(version: DownloadEntity)

    @Query("DELETE FROM downloads WHERE id = :arg0")
    fun delete(id: Long)

    @Query("SELECT * FROM downloads WHERE id = :arg0")
    fun get(id: Long): DownloadEntity?

    @Query("DELETE FROM downloads")
    fun clear()
}