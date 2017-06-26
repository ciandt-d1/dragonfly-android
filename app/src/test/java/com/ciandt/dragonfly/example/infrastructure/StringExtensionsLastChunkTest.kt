package com.ciandt.dragonfly.example.infrastructure

import com.ciandt.dragonfly.example.infrastructure.extensions.lastChunk
import org.junit.Test
import java.io.File
import kotlin.test.assertEquals

/**
 * Created by iluz on 6/26/17.
 */
class StringExtensionsLastChunkTest {

    @Test
    fun shouldReturnLastChunk() {
        val filePath = "/storage/emulated/0/com.ciandt.dragonfly.example.dev/30d7d205-1378-3173-a7e2-9fd49a3d816c.jpg"
        val fileName = filePath.lastChunk(File.separator)
        assertEquals("30d7d205-1378-3173-a7e2-9fd49a3d816c.jpg", fileName)
    }

    @Test
    fun shouldReturnEmptyStringIfStringIsEmpty() {
        val fileName = "".lastChunk(File.separator)
        assertEquals(fileName, "")
    }

    @Test
    fun shouldReturnStringItselfIfSeparatorIsMissing() {
        val fileName = "name".lastChunk(File.separator)
        assertEquals(fileName, "name")
    }
}
