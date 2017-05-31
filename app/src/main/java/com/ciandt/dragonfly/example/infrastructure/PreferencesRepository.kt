package com.ciandt.dragonfly.example.infrastructure


/**
 * Created by iluz on 5/30/17.
 */
interface PreferencesRepository {
    fun getBoolean(key: String, defaultValue: Boolean): Boolean

    fun getFloat(key: String, defaultValue: Float): Float

    fun getInt(key: String, defaultValue: Int): Int

    fun getLong(key: String, defaultValue: Long): Long

    fun getString(key: String, defaultValue: String): String

    fun getStringSet(key: String, defaultValue: Set<String>): Set<String>

    fun putBoolean(key: String, value: Boolean)

    fun putFloat(key: String, value: Float)

    fun putInt(key: String, value: Int)

    fun putLong(key: String, value: Long)

    fun putString(key: String, value: String)

    fun putStringSet(key: String, value: Set<String>)

    fun exists(key: String): Boolean

    fun remove(key: String)

    fun removeAll()
}
