package com.presa.app.domain.model

import androidx.annotation.StringRes

/**
 * A user-facing presentation command and its default keyboard chord.
 * Action ids are stable and also used as DataStore keys for remapping.
 */
data class KeyAction(
    val id: String,
    @StringRes val labelRes: Int,
    val defaultChord: KeyChord,
    val repeatable: Boolean = false,
)
