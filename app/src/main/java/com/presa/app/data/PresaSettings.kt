package com.presa.app.data

import com.presa.app.domain.model.AppLanguage
import com.presa.app.domain.model.AppMode
import com.presa.app.domain.model.KeyChord
import com.presa.app.domain.model.RepeatSpeed
import com.presa.app.domain.model.SlidesSubMode
import com.presa.app.domain.model.ThemeMode

data class PresaSettings(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val language: AppLanguage = AppLanguage.FRENCH,
    val hapticsEnabled: Boolean = true,
    val volumeKeysEnabled: Boolean = false,
    val repeatSpeed: RepeatSpeed = RepeatSpeed.NORMAL,
    val timerAlertMinutes: Int = 30,
    val lastMode: AppMode = AppMode.VISUALIZER,
    val lastSubMode: SlidesSubMode = SlidesSubMode.POWERPOINT,
    val lastDeviceAddress: String? = null,
    val lastDeviceName: String? = null,
    val keyOverrides: Map<String, KeyChord> = emptyMap(),
)
