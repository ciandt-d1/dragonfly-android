package com.ciandt.dragonfly.example.helpers

import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

object ZipHelper {

    fun unzip(inputStream: InputStream, path: String, onSuccess: (List<String>) -> Unit, onFailure: (Exception) -> Unit = {}) {
        mkdirs(path)

        val files = ArrayList<String>()

        try {

            ZipInputStream(inputStream).use { it ->
                var zipEntry: ZipEntry? = it.nextEntry

                while (zipEntry != null) {

                    val filePath = "$path${File.separator}${zipEntry.name}"

                    if (zipEntry.isDirectory) {
                        mkdirs(filePath)

                    } else {
                        val fileOutputStream = FileOutputStream(filePath)

                        val buf = ByteArray(1024)
                        var len: Int = 0
                        while ({ len = it.read(buf); len }() > 0) {
                            fileOutputStream.write(buf, 0, len)
                        }

                        it.closeEntry()
                        fileOutputStream.close()

                        files.add(filePath)
                    }

                    zipEntry = it.nextEntry
                }
            }

            onSuccess(files)

        } catch (e: Exception) {
            onFailure(e)
        }
    }

    private fun mkdirs(path: String) {
        val f = File(path)
        if (!f.isDirectory) {
            f.mkdirs()
        }
    }
}