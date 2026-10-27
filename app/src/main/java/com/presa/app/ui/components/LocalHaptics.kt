package com.presa.app.ui.components

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.staticCompositionLocalOf
import com.presa.app.data.Haptics

val LocalHaptics = staticCompositionLocalOf<Haptics?> { null }
val LocalHapticsEnabled = compositionLocalOf { true }
