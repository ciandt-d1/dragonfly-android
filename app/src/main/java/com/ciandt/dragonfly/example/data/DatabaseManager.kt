package com.ciandt.dragonfly.example.data

import android.arch.persistence.room.Room
import android.content.Context
import com.ciandt.dragonfly.example.config.Database
import com.ciandt.dragonfly.example.data.local.AppDatabase

/**
 * Created by iluz on 8/10/17.
 */
object DatabaseManager {
    private lateinit var context: Context

    val database by lazy {
        Room
                .databaseBuilder(context.applicationContext, AppDatabase::class.java, Database.NAME)
                .fallbackToDestructiveMigration()
                .build()
    }

    fun init(context: Context) {
        this.context = context.applicationContext
    }
}