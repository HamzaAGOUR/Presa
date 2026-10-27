package com.presa.app.domain.hid

import com.presa.app.domain.model.HidKeys
import com.presa.app.domain.model.KeyChord
import com.presa.app.domain.model.KeyModifiers
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Test

class HidReportTest {
    @Test
    fun keyDown_hasModifierReservedAndSingleKey() {
        val report = HidReport.keyDown(KeyChord.plain(0x4F))

        assertEquals(8, report.size)
        assertArrayEquals(
            byteArrayOf(0x00, 0x00, 0x4F, 0x00, 0x00, 0x00, 0x00, 0x00),
            report,
        )
    }

    @Test
    fun keyDown_preservesCtrlAndShiftModifierBits() {
        val chord = KeyChord(KeyModifiers.CTRL or KeyModifiers.SHIFT, 0x2E)
        val report = HidReport.keyDown(chord)

        assertEquals(0x03, report[0].toInt())
        assertEquals(0x2E, report[2].toInt())
    }

    @Test
    fun keyDown_shiftFIsTheVisualizerFullscreenShortcut() {
        val report = HidReport.keyDown(KeyChord.shift(HidKeys.F))

        assertArrayEquals(
            byteArrayOf(0x02, 0x00, 0x09, 0x00, 0x00, 0x00, 0x00, 0x00),
            report,
        )
    }

    @Test
    fun keyUp_releasesTheKeyAndAllModifiers() {
        val report = HidReport.keyUp()

        assertEquals(HidReport.REPORT_SIZE, report.size)
        assertArrayEquals(ByteArray(8), report)
    }

    @Test
    fun releaseAll_isIdenticalToKeyUp() {
        assertArrayEquals(HidReport.keyUp(), HidReport.releaseAll())
    }

    @Test
    fun everyKeyboardReportIsExactlyEightBytes() {
        listOf(
            HidReport.keyDown(KeyChord.plain(0x04)),
            HidReport.keyDown(KeyChord.ctrl(0x13)),
            HidReport.keyUp(),
            HidReport.releaseAll(),
        ).forEach { report ->
            assertEquals(HidReport.REPORT_SIZE, report.size)
        }
    }

    @Test
    fun reportIdsMatchTheCompositeDescriptorOrder() {
        assertEquals(1, HidReport.KEYBOARD_REPORT_ID)
        assertEquals(2, HidReport.MOUSE_REPORT_ID)
    }

    @Test
    fun mouseReport_hasButtonsAndFourAxes() {
        val report = HidReport.mouse()

        assertEquals(HidReport.MOUSE_REPORT_SIZE, report.size)
        assertArrayEquals(ByteArray(5), report)
    }

    @Test
    fun mouseMove_packsSignedDeltasAfterButtons() {
        val report = HidReport.mouseMove(dx = 12, dy = -7)

        assertArrayEquals(byteArrayOf(0x00, 0x0C, 0xF9.toByte(), 0x00, 0x00), report)
    }

    @Test
    fun mouseMove_clampsDeltasToTheDescriptorRange() {
        val report = HidReport.mouseMove(dx = 1_000, dy = -1_000)

        assertEquals(HidReport.MAX_RELATIVE.toByte(), report[1])
        assertEquals(HidReport.MIN_RELATIVE.toByte(), report[2])
    }

    @Test
    fun mouseScroll_packsWheelAndPan() {
        val report = HidReport.mouseScroll(wheel = -1, pan = 2)

        assertArrayEquals(byteArrayOf(0x00, 0x00, 0x00, 0xFF.toByte(), 0x02), report)
    }

    @Test
    fun mouseButton_setsExactlyOneButtonMask() {
        assertArrayEquals(
            byteArrayOf(0x01, 0x00, 0x00, 0x00, 0x00),
            HidReport.mouseButton(MouseButton.LEFT, pressed = true),
        )
        assertArrayEquals(
            byteArrayOf(0x02, 0x00, 0x00, 0x00, 0x00),
            HidReport.mouseButton(MouseButton.RIGHT, pressed = true),
        )
        assertArrayEquals(
            byteArrayOf(0x04, 0x00, 0x00, 0x00, 0x00),
            HidReport.mouseButton(MouseButton.MIDDLE, pressed = true),
        )
        assertArrayEquals(
            ByteArray(5),
            HidReport.mouseButton(MouseButton.LEFT, pressed = false),
        )
    }

    @Test
    fun mouseRelease_clearsButtonsAndAxes() {
        assertArrayEquals(HidReport.mouse(), HidReport.mouseRelease())
        assertArrayEquals(ByteArray(5), HidReport.mouseRelease())
    }
}
