package com.presa.app.domain.hid

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class KeyboardDescriptorTest {
    @Test
    fun descriptorIsWellFormed() {
        assertTrue(KeyboardDescriptor.isWellFormedKeyboardDescriptor())
    }

    @Test
    fun descriptorStartsWithKeyboardUsagePage() {
        val bytes = KeyboardDescriptor.BYTES

        assertEquals(0x05, bytes[0].toInt())
        assertEquals(0x01, bytes[1].toInt())
        assertEquals(0x09, bytes[2].toInt())
        assertEquals(0x06, bytes[3].toInt())
        assertEquals(0xA1, bytes[4].toInt() and 0xFF)
        assertEquals(0x01, bytes[5].toInt())
    }

    @Test
    fun descriptorEndsWithEndCollection() {
        val bytes = KeyboardDescriptor.BYTES

        assertEquals(0xC0, bytes.last().toInt() and 0xFF)
    }

    @Test
    fun descriptorDefinesOneModifierByteAndSixKeySlots() {
        val bytes = KeyboardDescriptor.BYTES.toList().map { it.toInt() and 0xFF }

        // Modifier byte: Report Size 1, Report Count 8.
        assertTrue(bytes.windowed(4).contains(listOf(0x75, 0x01, 0x95, 0x08)))
        // Six key slots: Report Count 6, Report Size 8.
        assertTrue(bytes.windowed(4).contains(listOf(0x95, 0x06, 0x75, 0x08)))
    }
}
