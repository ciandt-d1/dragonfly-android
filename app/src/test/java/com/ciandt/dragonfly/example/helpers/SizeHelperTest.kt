package com.ciandt.dragonfly.example.helpers

import org.amshove.kluent.shouldEqualTo
import org.junit.Test
import java.text.NumberFormat
import java.util.*

private val numberFormat = NumberFormat.getInstance(Locale.US)

class SizeHelperTest {

    @Test
    fun `0`() {
        SizeHelper.toReadable(0, numberFormat) shouldEqualTo "0"
    }

    @Test
    fun `1`() {
        SizeHelper.toReadable(1, numberFormat) shouldEqualTo "1 B"
    }

    @Test
    fun `1000`() {
        SizeHelper.toReadable(1000, numberFormat) shouldEqualTo "1,000 B"
    }

    @Test
    fun `1024`() {
        SizeHelper.toReadable(1024, numberFormat) shouldEqualTo "1 kB"
    }

    @Test
    fun `1025`() {
        SizeHelper.toReadable(1025, numberFormat) shouldEqualTo "1.001 kB"
    }

    @Test
    fun `2048`() {
        SizeHelper.toReadable(2048, numberFormat) shouldEqualTo "2 kB"
    }

    @Test
    fun `1048575`() {
        SizeHelper.toReadable(1048575, numberFormat) shouldEqualTo "1,023.999 kB"
    }

    @Test
    fun `1048576`() {
        SizeHelper.toReadable(1048576, numberFormat) shouldEqualTo "1 MB"
    }

    @Test
    fun `1049600`() {
        SizeHelper.toReadable(1049600, numberFormat) shouldEqualTo "1.001 MB"
    }

    @Test
    fun `2097152`() {
        SizeHelper.toReadable(2097152, numberFormat) shouldEqualTo "2 MB"
    }

    @Test
    fun `1073741824`() {
        SizeHelper.toReadable(1073741824, numberFormat) shouldEqualTo "1 GB"
    }

    @Test
    fun `1074790400`() {
        SizeHelper.toReadable(1074790400, numberFormat) shouldEqualTo "1.001 GB"
    }

    @Test
    fun `2147483648`() {
        SizeHelper.toReadable(2147483648, numberFormat) shouldEqualTo "2 GB"
    }

    @Test
    fun `1099511627776`() {
        SizeHelper.toReadable(1099511627776, numberFormat) shouldEqualTo "1 TB"
    }

    @Test
    fun `1100585369600`() {
        SizeHelper.toReadable(1100585369600, numberFormat) shouldEqualTo "1.001 TB"
    }

    @Test
    fun `2199023255552`() {
        SizeHelper.toReadable(2199023255552, numberFormat) shouldEqualTo "2 TB"
    }

    @Test
    fun `1099511627776000`() {
        SizeHelper.toReadable(1125899906842624, numberFormat) shouldEqualTo "1 PB"
    }

    // PT-BR
    @Test
    fun `0-pt-BR`() {
        val numberFormat = NumberFormat.getNumberInstance(Locale("pt", "BR"))
        SizeHelper.toReadable(0, numberFormat) shouldEqualTo "0"
    }

    @Test
    fun `1-pt-BR`() {
        val numberFormat = NumberFormat.getNumberInstance(Locale("pt", "BR"))
        SizeHelper.toReadable(1, numberFormat) shouldEqualTo "1 B"
    }

    @Test
    fun `1000-pt-BR`() {
        val numberFormat = NumberFormat.getNumberInstance(Locale("pt", "BR"))
        SizeHelper.toReadable(1000, numberFormat) shouldEqualTo "1.000 B"
    }

    @Test
    fun `1024-pt-BR`() {
        val numberFormat = NumberFormat.getNumberInstance(Locale("pt", "BR"))
        SizeHelper.toReadable(1024, numberFormat) shouldEqualTo "1 kB"
    }

    @Test
    fun `1536-pt-BR`() {
        val numberFormat = NumberFormat.getNumberInstance(Locale("pt", "BR"))
        SizeHelper.toReadable(1536, numberFormat) shouldEqualTo "1,5 kB"
    }

    // Units
    @Test
    fun noUnits() {
        SizeHelper.toReadable(2, units = arrayOf()) shouldEqualTo "2"
        SizeHelper.toReadable(1024, units = arrayOf()) shouldEqualTo "1024"
        SizeHelper.toReadable(1048576, units = arrayOf()) shouldEqualTo "1048576"
    }

    @Test
    fun limitedUnits() {
        val units = arrayOf("B", "kB")
        SizeHelper.toReadable(2, units = units) shouldEqualTo "2 B"
        SizeHelper.toReadable(1024, units = units) shouldEqualTo "1 kB"
        SizeHelper.toReadable(1048576, units = units) shouldEqualTo "1048576"
    }

    @Test
    fun otherUnits() {
        val units = arrayOf("B", "KiB", "MiB", "GiB", "TiB", "PiB")
        SizeHelper.toReadable(1, units = units) shouldEqualTo "1 B"
        SizeHelper.toReadable(1024, units = units) shouldEqualTo "1 KiB"
        SizeHelper.toReadable(1048576, units = units) shouldEqualTo "1 MiB"
        SizeHelper.toReadable(1073741824, units = units) shouldEqualTo "1 GiB"
    }
}