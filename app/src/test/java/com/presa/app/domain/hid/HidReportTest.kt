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
    fun everyReportIsExactlyEightBytes() {
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
    fun reportIdIsZeroForBootKeyboard() {
        assertEquals(0, HidReport.REPORT_ID)
    }
}
