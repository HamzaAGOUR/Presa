package com.presa.app.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.presa.app.data.LocaleStore
import com.presa.app.data.PresaPreferences
import com.presa.app.data.PresaSettings
import com.presa.app.di.AppContainer
import com.presa.app.domain.model.AppLanguage
import com.presa.app.domain.model.KeyChord
import com.presa.app.domain.model.RepeatSpeed
import com.presa.app.domain.model.ThemeMode
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val preferences: PresaPreferences,
    private val container: AppContainer,
) : ViewModel() {
    val settings: StateFlow<PresaSettings> = preferences.settings.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = PresaSettings(
            language = AppLanguage.fromTag(LocaleStore.readTag(container.appContext)),
        ),
    )

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch { preferences.setThemeMode(mode) }
    }

    fun setLanguage(language: AppLanguage) {
        viewModelScope.launch { preferences.setLanguage(language) }
    }

    fun setHapticsEnabled(enabled: Boolean) {
        viewModelScope.launch { preferences.setHapticsEnabled(enabled) }
    }

    fun setVolumeKeysEnabled(enabled: Boolean) {
        viewModelScope.launch { preferences.setVolumeKeysEnabled(enabled) }
    }

    fun setRepeatSpeed(speed: RepeatSpeed) {
        viewModelScope.launch { preferences.setRepeatSpeed(speed) }
    }

    fun setTimerAlertMinutes(minutes: Int) {
        viewModelScope.launch { preferences.setTimerAlertMinutes(minutes) }
    }

    fun setKeyOverride(actionId: String, chord: KeyChord) {
        viewModelScope.launch { preferences.setKeyOverride(actionId, chord) }
    }

    fun resetKeyOverride(actionId: String) {
        viewModelScope.launch { preferences.resetKeyOverride(actionId) }
    }

    fun resetAllKeyOverrides() {
        viewModelScope.launch { preferences.resetAllKeyOverrides() }
    }

    companion object {
        fun factory(container: AppContainer) = viewModelFactory {
            initializer { SettingsViewModel(container.preferences, container) }
        }
    }
}
