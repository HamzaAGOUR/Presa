package com.presa.app.data

import com.presa.app.domain.model.KeyChord
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class KeyRemapCodecTest {
    @Test
    fun encodeSortsByIdAndUsesCompactFormat() {
        val encoded = KeyRemapCodec.encode(
            linkedMapOf(
                "visualizer.next" to KeyChord.plain(0x4F),
                "visualizer.zoom_in" to KeyChord(0x02, 0x2E),
            ),
        )

        assertEquals("visualizer.next=0:79;visualizer.zoom_in=2:46", encoded)
    }

    @Test
    fun roundTripPreservesModifiersAndUsages() {
        val original = mapOf(
            "powerpoint.laser" to KeyChord(0x01, 0x0F),
            "pdf.zoom_in" to KeyChord(0x03, 0x2E),
            "visualizer.reset" to KeyChord(0x00, 0x15),
        )

        assertEquals(original, KeyRemapCodec.decode(KeyRemapCodec.encode(original)))
    }

    @Test
    fun blankInputProducesEmptyMap() {
        assertTrue(KeyRemapCodec.decode(null).isEmpty())
        assertTrue(KeyRemapCodec.decode("").isEmpty())
        assertTrue(KeyRemapCodec.decode("   ").isEmpty())
    }

    @Test
    fun malformedEntriesAreIgnoredWithoutLosingValidOnes() {
        val raw = listOf(
            "missing-equals",
            "empty=",
            "emptyModifier=:4",
            "badModifier=x:4",
            "badUsage=0:y",
            "usageZero=0:0",
            "modifierTooLarge=256:4",
            "usageTooLarge=0:256",
            "tooManyParts=0:4:5",
            "valid.next=0:79",
        ).joinToString(";")

        assertEquals(
            mapOf("valid.next" to KeyChord(0, 79)),
            KeyRemapCodec.decode(raw),
        )
    }

    @Test
    fun duplicateEntriesKeepTheLastValue() {
        val decoded = KeyRemapCodec.decode("visualizer.next=0:79;visualizer.next=1:80")

        assertEquals(KeyChord(1, 80), decoded["visualizer.next"])
        assertEquals(1, decoded.size)
    }

    @Test
    fun boundaryValuesAreAccepted() {
        val decoded = KeyRemapCodec.decode("a=255:255;b=0:1")

        assertEquals(KeyChord(255, 255), decoded["a"])
        assertEquals(KeyChord(0, 1), decoded["b"])
    }

    @Test
    fun whitespaceAroundIdsAndValuesIsTrimmed() {
        val decoded = KeyRemapCodec.decode("  visualizer.next = 3 : 46 ; ")

        assertEquals(KeyChord(3, 46), decoded["visualizer.next"])
    }
}
