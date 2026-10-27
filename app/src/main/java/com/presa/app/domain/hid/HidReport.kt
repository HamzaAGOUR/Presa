package com.presa.app.domain.hid

import com.presa.app.domain.model.KeyChord

/**
 * An 8-byte keyboard report required by the HID boot protocol:
 * [modifier, reserved, key1, key2, key3, key4, key5, key6].
 */
object HidReport {
    const val REPORT_SIZE = 8
    const val REPORT_ID = 0

    fun keyDown(chord: KeyChord): ByteArray = byteArrayOf(
        chord.modifier.toByte(),
        0,
        chord.usage.toByte(),
        0,
        0,
        0,
        0,
        0,
    )

    /**
     * Releases the key AND all modifiers so no Ctrl/Shift state can stay stuck
     * on the host if a connection drops during a chord.
     */
    fun keyUp(): ByteArray = ByteArray(REPORT_SIZE)

    fun releaseAll(): ByteArray = ByteArray(REPORT_SIZE)
}
