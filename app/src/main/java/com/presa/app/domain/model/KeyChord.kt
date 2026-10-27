package com.presa.app.domain.model

/**
 * An 8-byte boot-keyboard event: one modifier byte plus one HID usage ID.
 * The modifier byte uses the standard HID keyboard modifier bits.
 */
data class KeyChord(
    val modifier: Int,
    val usage: Int,
) {
    init {
        require(modifier in 0..0xFF) { "Modifier must fit in one byte" }
        require(usage in 1..0xFF) { "Usage must fit in one non-zero byte" }
    }

    val hasCtrl: Boolean get() = modifier and KeyModifiers.CTRL != 0
    val hasShift: Boolean get() = modifier and KeyModifiers.SHIFT != 0
    val hasAlt: Boolean get() = modifier and KeyModifiers.ALT != 0

    fun withModifier(bit: Int): KeyChord = copy(modifier = modifier or bit)

    fun withoutModifier(bit: Int): KeyChord = copy(modifier = modifier and bit.inv())

    companion object {
        fun plain(usage: Int): KeyChord = KeyChord(0, usage)

        fun ctrl(usage: Int): KeyChord = KeyChord(KeyModifiers.CTRL, usage)

        fun shift(usage: Int): KeyChord = KeyChord(KeyModifiers.SHIFT, usage)

        fun ctrlShift(usage: Int): KeyChord =
            KeyChord(KeyModifiers.CTRL or KeyModifiers.SHIFT, usage)
    }
}

object KeyModifiers {
    const val CTRL = 0x01
    const val SHIFT = 0x02
    const val ALT = 0x04
    const val GUI = 0x08
    const val RIGHT_CTRL = 0x10
    const val RIGHT_SHIFT = 0x20
    const val RIGHT_ALT = 0x40
    const val RIGHT_GUI = 0x80
}
