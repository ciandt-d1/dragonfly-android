package com.ciandt.dragonfly.example.data.local.dao

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Insert
import android.arch.persistence.room.OnConflictStrategy
import android.arch.persistence.room.Query
import com.ciandt.dragonfly.example.data.local.entities.Model

@Dao
interface ModelDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(model: Model)

    @Query("SELECT * FROM models")
    fun getAll(): List<Model>

    @Query("DELETE FROM models")
    fun clear()
}