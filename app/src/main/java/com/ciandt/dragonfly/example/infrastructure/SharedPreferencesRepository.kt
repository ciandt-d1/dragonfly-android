package com.ciandt.dragonfly.example.infrastructure

import android.content.Context
import android.content.SharedPreferences


/**
 * Created by iluz on 5/30/17.
 */
class SharedPreferencesRepository(sharedPreferences: SharedPreferences) : PreferencesRepository {
    val sharedPreferences: SharedPreferences = sharedPreferences

    override fun getBoolean(key: String, defaultValue: Boolean): Boolean =
            sharedPreferences.getBoolean(key, defaultValue)

    override fun getFloat(key: String, defaultValue: Float): Float =
            sharedPreferences.getFloat(key, defaultValue)

    override fun getInt(key: String, defaultValue: Int): Int =
            sharedPreferences.getInt(key, defaultValue)

    override fun getLong(key: String, defaultValue: Long): Long =
            sharedPreferences.getLong(key, defaultValue)

    override fun getString(key: String, defaultValue: String): String =
            sharedPreferences.getString(key, defaultValue)

    override fun getStringSet(key: String, defaultValue: Set<String>): Set<String> =
            sharedPreferences.getStringSet(key, defaultValue)

    override fun putBoolean(key: String, value: Boolean) {
        editAndCommit { editor -> editor.putBoolean(key, value) }
    }

    override fun putFloat(key: String, value: Float) {
        editAndCommit { editor -> editor.putFloat(key, value) }
    }

    override fun putInt(key: String, value: Int) {
        editAndCommit { editor -> editor.putInt(key, value) }
    }

    override fun putLong(key: String, value: Long) {
        editAndCommit { editor -> editor.putLong(key, value) }
    }

    override fun putString(key: String, value: String) {
        editAndCommit { editor -> editor.putString(key, value) }
    }

    override fun putStringSet(key: String, value: Set<String>) {
        editAndCommit { editor -> editor.putStringSet(key, value) }
    }

    override fun exists(key: String): Boolean = sharedPreferences.contains(key)

    override fun remove(key: String) {
        editAndCommit { editor -> editor.remove(key) }
    }

    override fun removeAll() {
        editAndCommit { editor -> editor.clear() }
    }

    private fun editAndCommit(putCode: (editor: SharedPreferences.Editor) -> Unit) {
        val editor: SharedPreferences.Editor = sharedPreferences.edit()
        putCode(editor)
        editor.commit()
    }

    companion object {
        private val NAME = "DragonflyPreferences"

        fun get(context: Context): SharedPreferencesRepository {
            return SharedPreferencesRepository(context.getSharedPreferences(NAME, Context.MODE_PRIVATE))
        }
    }
}