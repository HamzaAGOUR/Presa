package com.presa.app.domain.hid

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class HidDescriptorTest {
    private val bytes: List<Int> = HidDescriptor.BYTES.toList().map { it.toInt() and 0xFF }

    @Test
    fun descriptorIsWellFormed() {
        assertTrue(HidDescriptor.isWellFormed())
        assertEquals(HidDescriptor.SIZE, HidDescriptor.BYTES.size)
    }

    @Test
    fun descriptorStartsWithKeyboardUsagePage() {
        assertEquals(0x05, bytes[0])
        assertEquals(0x01, bytes[1])
        assertEquals(0x09, bytes[2])
        assertEquals(0x06, bytes[3])
        assertEquals(0xA1, bytes[4])
        assertEquals(0x01, bytes[5])
    }

    @Test
    fun descriptorDeclaresKeyboardThenMouseReportIds() {
        val keyboardId = bytes.indexOfFirst { it == 0x85 }
        val mouseId = bytes.indexOfLast { it == 0x85 }

        assertTrue(keyboardId >= 0)
        assertTrue(mouseId > keyboardId)
        assertEquals(HidReport.KEYBOARD_REPORT_ID, bytes[keyboardId + 1])
        assertEquals(HidReport.MOUSE_REPORT_ID, bytes[mouseId + 1])
    }

    @Test
    fun descriptorDefinesMouseUsageAfterKeyboardCollection() {
        val mouseUsage = bytes.windowed(2).indexOf(listOf(0x09, 0x02))

        assertTrue(mouseUsage > 0)
        // Usage Page (Generic Desktop) must immediately precede Usage (Mouse).
        assertEquals(listOf(0x05, 0x01), bytes.subList(mouseUsage - 2, mouseUsage))
    }

    @Test
    fun descriptorEndsWithTwoClosedCollections() {
        assertEquals(listOf(0xC0, 0xC0), bytes.takeLast(2))
    }

    @Test
    fun descriptorDefinesOneModifierByteAndSixKeySlots() {
        // Modifier byte: Report Size 1, Report Count 8.
        assertTrue(bytes.windowed(4).contains(listOf(0x75, 0x01, 0x95, 0x08)))
        // Six key slots: Report Count 6, Report Size 8.
        assertTrue(bytes.windowed(4).contains(listOf(0x95, 0x06, 0x75, 0x08)))
    }

    @Test
    fun descriptorDefinesFiveButtonsPlusThreePaddingBits() {
        // Five button bits, then one constant three-bit padding field.
        assertTrue(bytes.windowed(4).contains(listOf(0x95, 0x05, 0x81, 0x02)))
        assertTrue(bytes.windowed(4).contains(listOf(0x75, 0x03, 0x95, 0x01)))
    }

    @Test
    fun descriptorDefinesThreeRelativeAxesAndHorizontalPan() {
        // X, Y and wheel share one three-byte relative input field.
        assertTrue(bytes.windowed(4).contains(listOf(0x75, 0x08, 0x95, 0x03)))
        // AC Pan (0x0238 on the Consumer page) is a fourth relative byte.
        assertTrue(bytes.windowed(3).contains(listOf(0x0A, 0x38, 0x02)))
        assertTrue(bytes.windowed(4).contains(listOf(0x95, 0x01, 0x81, 0x06)))
    }

    @Test
    fun descriptorUsesOnlyWellFormedItems() {
        var index = 0
        var depth = 0
        var reportIds = 0

        while (index < bytes.size) {
            val prefix = bytes[index]
            assertTrue("Long HID items are not supported here", prefix != 0xFE)
            val sizeCode = prefix and 0x03
            val itemSize = if (sizeCode == 3) 4 else sizeCode
            assertTrue(
                "Item at $index overruns the descriptor",
                index + 1 + itemSize <= bytes.size,
            )

            val itemType = (prefix shr 2) and 0x03
            val itemTag = (prefix shr 4) and 0x0F
            if (itemType == 1 && itemTag == 0x08) reportIds++
            if (itemType == 0) {
                when (itemTag) {
                    0x0A -> depth++
                    0x0C -> depth--
                }
                assertTrue("Collection depth went negative at $index", depth >= 0)
            }

            index += 1 + itemSize
        }

        assertEquals(bytes.size, index)
        assertEquals(0, depth)
        assertEquals(2, reportIds)
    }

    @Test
    fun descriptorUsesSignedByteRangeForRelativeAxes() {
        // Logical Minimum (-127), Logical Maximum (127).
        assertTrue(bytes.windowed(2).contains(listOf(0x15, 0x81)))
        assertTrue(bytes.windowed(2).contains(listOf(0x25, 0x7F)))
    }
}
