package com.presa.app.ui.timer

import org.junit.Assert.assertEquals
import org.junit.Test

class TimerFormatTest {
    @Test
    fun zeroIsZeroedMinutesAndSeconds() {
        assertEquals("00:00", TimerFormat.clock(0L))
    }

    @Test
    fun formatsMinutesAndSecondsWithTwoDigits() {
        assertEquals("01:05", TimerFormat.clock(65_000L))
        assertEquals("59:59", TimerFormat.clock(3_599_000L))
    }

    @Test
    fun includesHoursOnlyAboveOneHour() {
        assertEquals("1:00:00", TimerFormat.clock(3_600_000L))
        assertEquals("2:03:04", TimerFormat.clock(7_384_000L))
    }

    @Test
    fun ignoresSubSecondPrecision() {
        assertEquals("00:01", TimerFormat.clock(1_999L))
    }

    @Test
    fun negativeValuesClampToZero() {
        assertEquals("00:00", TimerFormat.clock(-5_000L))
    }
}
