package com.presa.app.domain.model

import com.presa.app.domain.hid.HidReport
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class KeyMapTest {
    @Test
    fun allActionIdsAreUnique() {
        val ids = KeyMaps.allActions.map(KeyAction::id)
        assertEquals(ids.size, ids.distinct().size)
    }

    @Test
    fun visualizerContainsEveryRequiredCommand() {
        val ids = VisualizerKeyMap.actions.map(KeyAction::id).toSet()
        assertEquals(
            setOf(
                VisualizerKeyMap.PREVIOUS,
                VisualizerKeyMap.NEXT,
                VisualizerKeyMap.PLAY_PAUSE,
                VisualizerKeyMap.START,
                VisualizerKeyMap.END,
                VisualizerKeyMap.SLOWER,
                VisualizerKeyMap.FASTER,
                VisualizerKeyMap.ZOOM_IN,
                VisualizerKeyMap.ZOOM_OUT,
                VisualizerKeyMap.FIT,
                VisualizerKeyMap.FULLSCREEN,
                VisualizerKeyMap.PANELS,
                VisualizerKeyMap.PRESENTATION,
                VisualizerKeyMap.RESET,
                VisualizerKeyMap.BACK,
            ),
            ids,
        )
    }

    @Test
    fun visualizerDefaultsMatchTheWebShortcuts() {
        assertChord(VisualizerKeyMap.PREVIOUS, KeyChord.plain(HidKeys.LEFT))
        assertChord(VisualizerKeyMap.NEXT, KeyChord.plain(HidKeys.RIGHT))
        assertChord(VisualizerKeyMap.PLAY_PAUSE, KeyChord.plain(HidKeys.SPACE))
        assertChord(VisualizerKeyMap.START, KeyChord.plain(HidKeys.HOME))
        assertChord(VisualizerKeyMap.END, KeyChord.plain(HidKeys.END))
        assertChord(VisualizerKeyMap.SLOWER, KeyChord.plain(HidKeys.DOWN))
        assertChord(VisualizerKeyMap.FASTER, KeyChord.plain(HidKeys.UP))
        assertChord(VisualizerKeyMap.ZOOM_IN, KeyChord.shift(HidKeys.EQUALS))
        assertChord(VisualizerKeyMap.ZOOM_OUT, KeyChord.plain(HidKeys.MINUS))
        assertChord(VisualizerKeyMap.FIT, KeyChord.plain(HidKeys.DIGIT_0))
        assertChord(VisualizerKeyMap.FULLSCREEN, KeyChord.shift(HidKeys.F))
        assertChord(VisualizerKeyMap.PANELS, KeyChord.plain(HidKeys.D))
        assertChord(VisualizerKeyMap.PRESENTATION, KeyChord.plain(HidKeys.P))
        assertChord(VisualizerKeyMap.RESET, KeyChord.plain(HidKeys.R))
        assertChord(VisualizerKeyMap.BACK, KeyChord.plain(HidKeys.ESCAPE))
    }

    @Test
    fun powerpointDefaultsMatchOfficeShortcuts() {
        assertChord(PowerPointKeyMap.PREVIOUS, KeyChord.plain(HidKeys.LEFT))
        assertChord(PowerPointKeyMap.NEXT, KeyChord.plain(HidKeys.RIGHT))
        assertChord(PowerPointKeyMap.FIRST, KeyChord.plain(HidKeys.HOME))
        assertChord(PowerPointKeyMap.LAST, KeyChord.plain(HidKeys.END))
        assertChord(PowerPointKeyMap.START_SHOW, KeyChord.plain(HidKeys.F5))
        assertChord(PowerPointKeyMap.FROM_CURRENT, KeyChord.shift(HidKeys.F5))
        assertChord(PowerPointKeyMap.EXIT, KeyChord.plain(HidKeys.ESCAPE))
        assertChord(PowerPointKeyMap.BLACK, KeyChord.plain(HidKeys.B))
        assertChord(PowerPointKeyMap.WHITE, KeyChord.plain(HidKeys.W))
        assertChord(PowerPointKeyMap.LASER, KeyChord.ctrl(HidKeys.L))
        assertChord(PowerPointKeyMap.PEN, KeyChord.ctrl(HidKeys.P))
        assertChord(PowerPointKeyMap.ERASER, KeyChord.plain(HidKeys.E))
    }

    @Test
    fun pdfDefaultsMatchChromeAndAcrobatShortcuts() {
        assertChord(PdfKeyMap.PREVIOUS_PAGE, KeyChord.plain(HidKeys.PAGE_UP))
        assertChord(PdfKeyMap.NEXT_PAGE, KeyChord.plain(HidKeys.PAGE_DOWN))
        assertChord(PdfKeyMap.FIRST, KeyChord.plain(HidKeys.HOME))
        assertChord(PdfKeyMap.LAST, KeyChord.plain(HidKeys.END))
        assertChord(PdfKeyMap.SCROLL_UP, KeyChord.plain(HidKeys.UP))
        assertChord(PdfKeyMap.SCROLL_DOWN, KeyChord.plain(HidKeys.DOWN))
        assertChord(PdfKeyMap.FULLSCREEN, KeyChord.plain(HidKeys.F11))
        assertChord(PdfKeyMap.FULLSCREEN_ALT, KeyChord.ctrl(HidKeys.L))
        assertChord(PdfKeyMap.ZOOM_IN, KeyChord(KeyModifiers.CTRL or KeyModifiers.SHIFT, HidKeys.EQUALS))
        assertChord(PdfKeyMap.ZOOM_OUT, KeyChord.ctrl(HidKeys.MINUS))
        assertChord(PdfKeyMap.FIT, KeyChord.ctrl(HidKeys.DIGIT_0))
        assertChord(PdfKeyMap.EXIT, KeyChord.plain(HidKeys.ESCAPE))
    }

    @Test
    fun previousAndNextAreRepeatableInEveryMode() {
        assertTrue(KeyMaps.find(VisualizerKeyMap.PREVIOUS)!!.repeatable)
        assertTrue(KeyMaps.find(VisualizerKeyMap.NEXT)!!.repeatable)
        assertTrue(KeyMaps.find(PowerPointKeyMap.PREVIOUS)!!.repeatable)
        assertTrue(KeyMaps.find(PowerPointKeyMap.NEXT)!!.repeatable)
        assertTrue(KeyMaps.find(PdfKeyMap.PREVIOUS_PAGE)!!.repeatable)
        assertTrue(KeyMaps.find(PdfKeyMap.NEXT_PAGE)!!.repeatable)
        assertFalse(KeyMaps.find(VisualizerKeyMap.PLAY_PAUSE)!!.repeatable)
    }

    @Test
    fun actionsForModeReturnsTheRightGroup() {
        assertEquals(VisualizerKeyMap.actions, KeyMaps.actionsFor(AppMode.VISUALIZER, SlidesSubMode.PDF))
        assertEquals(
            PowerPointKeyMap.actions,
            KeyMaps.actionsFor(AppMode.SLIDES, SlidesSubMode.POWERPOINT),
        )
        assertEquals(PdfKeyMap.actions, KeyMaps.actionsFor(AppMode.SLIDES, SlidesSubMode.PDF))
    }

    @Test
    fun effectiveChordUsesOverrideWhenPresent() {
        val action = KeyMaps.find(VisualizerKeyMap.NEXT)!!
        val override = KeyChord.ctrl(HidKeys.PAGE_DOWN)

        assertEquals(action.defaultChord, KeyMaps.effectiveChord(action, emptyMap()))
        assertEquals(
            override,
            KeyMaps.effectiveChord(action, mapOf(VisualizerKeyMap.NEXT to override)),
        )
    }

    @Test
    fun digitChordsFollowBootKeyboardUsageOrder() {
        assertEquals(0x1E, KeyMaps.digitChord('1').usage)
        assertEquals(0x1F, KeyMaps.digitChord('2').usage)
        assertEquals(0x26, KeyMaps.digitChord('9').usage)
        assertEquals(0x27, KeyMaps.digitChord('0').usage)
        assertEquals(0, KeyMaps.digitChord('7').modifier)
    }

    @Test
    fun reportsGeneratedFromEveryActionAreValidEightByteReports() {
        KeyMaps.allActions.forEach { action ->
            val report = HidReport.keyDown(action.defaultChord)
            assertEquals(HidReport.REPORT_SIZE, report.size)
            assertEquals(action.defaultChord.modifier, report[0].toInt())
            assertEquals(action.defaultChord.usage, report[2].toInt())
        }
    }

    @Test
    fun findReturnsNullForUnknownId() {
        assertNull(KeyMaps.find("does.not.exist"))
        assertNotNull(KeyMaps.find(VisualizerKeyMap.NEXT))
    }

    private fun assertChord(actionId: String, expected: KeyChord) {
        val action = KeyMaps.find(actionId)
        assertNotNull("Missing action $actionId", action)
        assertEquals("Wrong chord for $actionId", expected, action!!.defaultChord)
    }
}
