package com.presa.app.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.presa.app.R
import com.presa.app.domain.model.HidKeys
import com.presa.app.domain.model.KeyChord
import java.util.Locale

@Composable
fun keyName(usage: Int): String = when (usage) {
    in 0x04..0x1D -> ('A'.code + (usage - 0x04)).toChar().toString()
    in 0x1E..0x26 -> (usage - 0x1D).toString()
    0x27 -> "0"
    HidKeys.ENTER -> stringResource(R.string.key_enter)
    HidKeys.ESCAPE -> stringResource(R.string.key_escape)
    HidKeys.TAB -> stringResource(R.string.key_tab)
    HidKeys.SPACE -> stringResource(R.string.key_space)
    HidKeys.BACKSPACE -> stringResource(R.string.key_backspace)
    HidKeys.LEFT -> stringResource(R.string.key_left)
    HidKeys.RIGHT -> stringResource(R.string.key_right)
    HidKeys.UP -> stringResource(R.string.key_up)
    HidKeys.DOWN -> stringResource(R.string.key_down)
    HidKeys.HOME -> stringResource(R.string.key_home)
    HidKeys.END -> stringResource(R.string.key_end)
    HidKeys.PAGE_UP -> stringResource(R.string.key_page_up)
    HidKeys.PAGE_DOWN -> stringResource(R.string.key_page_down)
    HidKeys.F5 -> stringResource(R.string.key_f5)
    HidKeys.F11 -> stringResource(R.string.key_f11)
    HidKeys.EQUALS -> stringResource(R.string.key_equals)
    HidKeys.MINUS -> stringResource(R.string.key_minus)
    else -> stringResource(
        R.string.key_unknown,
        usage.toString(16).uppercase(Locale.US),
    )
}

@Composable
fun chordLabel(chord: KeyChord): String {
    val parts = ArrayList<String>(4)
    if (chord.hasCtrl) parts += stringResource(R.string.key_mod_ctrl)
    if (chord.hasShift) parts += stringResource(R.string.key_mod_shift)
    if (chord.hasAlt) parts += stringResource(R.string.key_mod_alt)
    parts += keyName(chord.usage)
    return parts.joinToString("+")
}

/** Base keys offered by the per-button remap picker. */
val RemapKeyUsages: List<Int> = buildList {
    addAll(0x04..0x1D) // A-Z
    addAll(0x1E..0x27) // 1-9, 0
    addAll(
        listOf(
            HidKeys.HOME,
            HidKeys.END,
            HidKeys.PAGE_UP,
            HidKeys.PAGE_DOWN,
            HidKeys.LEFT,
            HidKeys.RIGHT,
            HidKeys.UP,
            HidKeys.DOWN,
            HidKeys.SPACE,
            HidKeys.ENTER,
            HidKeys.ESCAPE,
            HidKeys.TAB,
            HidKeys.BACKSPACE,
            HidKeys.F5,
            HidKeys.F11,
            HidKeys.EQUALS,
            HidKeys.MINUS,
        ),
    )
}
