package com.ciandt.dragonfly.example.helpers

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException


object ImageHelper {

    fun encodeToBase64(bitmap: Bitmap, compressFormat: Bitmap.CompressFormat = Bitmap.CompressFormat.JPEG, quality: Int = 100): String? {
        val byteArrayOS = ByteArrayOutputStream()
        bitmap.compress(compressFormat, quality, byteArrayOS)
        return Base64.encodeToString(byteArrayOS.toByteArray(), Base64.DEFAULT)
    }

    fun encodeToBase64(filePath: String, compressFormat: Bitmap.CompressFormat = Bitmap.CompressFormat.JPEG, quality: Int = 100): String? {
        try {
            FileInputStream(File(filePath)).use {
                val bitmap = BitmapFactory.decodeStream(it)
                return encodeToBase64(bitmap, compressFormat, quality)
            }

        } catch (e: FileNotFoundException) {
            return null
        }
    }
}