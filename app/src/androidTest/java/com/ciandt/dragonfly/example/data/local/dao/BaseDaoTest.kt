package com.ciandt.dragonfly.example.data.local.dao

import android.arch.persistence.room.Room
import android.support.test.InstrumentationRegistry
import com.ciandt.dragonfly.example.data.local.AppDatabase
import org.junit.AfterClass
import org.junit.BeforeClass

/**
 * Created by iluz on 8/15/17.
 */
open class BaseDaoTest {
    companion object {
        lateinit var DATABASE: AppDatabase

        @Suppress("unused")
        @BeforeClass
        @JvmStatic
        fun initDb() {
            DATABASE = Room.inMemoryDatabaseBuilder(
                    InstrumentationRegistry.getContext(),
                    AppDatabase::class.java)
                    .build()
        }

        @Suppress("unused")
        @AfterClass
        @JvmStatic
        fun closeDb() {
            DATABASE.close()
        }
    }
}