package com.presa.app.domain.hid

import com.presa.app.domain.model.KeyChord

/**
 * Builders for the two input reports exposed by the composite HID descriptor.
 *
 * Keyboard body: [modifier, reserved, key1, key2, key3, key4, key5, key6].
 * Mouse body: [buttons, dx, dy, vertical wheel, horizontal pan].
 *
 * Android sends the report ID separately, so the byte arrays below never
 * contain it.
 */
object HidReport {
    const val KEYBOARD_REPORT_ID = 1
    const val MOUSE_REPORT_ID = 2

    /** Keyboard report body size for a boot-style six-key report. */
    const val REPORT_SIZE = 8
    const val MOUSE_REPORT_SIZE = 5

    const val MIN_RELATIVE = -127
    const val MAX_RELATIVE = 127

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

    /**
     * Builds a relative mouse report. Movement and wheel values are clamped to
     * the descriptor's signed-byte range, so a fast swipe never overflows.
     */
    fun mouse(
        buttons: Int = 0,
        dx: Int = 0,
        dy: Int = 0,
        wheel: Int = 0,
        pan: Int = 0,
    ): ByteArray = byteArrayOf(
        buttons.toByte(),
        dx.coerceIn(MIN_RELATIVE, MAX_RELATIVE).toByte(),
        dy.coerceIn(MIN_RELATIVE, MAX_RELATIVE).toByte(),
        wheel.coerceIn(MIN_RELATIVE, MAX_RELATIVE).toByte(),
        pan.coerceIn(MIN_RELATIVE, MAX_RELATIVE).toByte(),
    )

    fun mouseMove(dx: Int, dy: Int): ByteArray = mouse(dx = dx, dy = dy)

    fun mouseScroll(wheel: Int, pan: Int = 0): ByteArray = mouse(wheel = wheel, pan = pan)

    fun mouseButton(button: MouseButton, pressed: Boolean): ByteArray =
        mouse(buttons = if (pressed) button.mask else 0)

    fun mouseRelease(): ByteArray = mouse()
}

/** Buttons exposed by the five-button mouse descriptor. */
enum class MouseButton(val mask: Int) {
    LEFT(0x01),
    RIGHT(0x02),
    MIDDLE(0x04),
}
