package com.presa.app.data

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.presa.app.domain.model.AppLanguage
import com.presa.app.domain.model.AppMode
import com.presa.app.domain.model.KeyChord
import com.presa.app.domain.model.RepeatSpeed
import com.presa.app.domain.model.SlidesSubMode
import com.presa.app.domain.model.ThemeMode
import java.io.IOException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.presaDataStore by preferencesDataStore(name = "presa_settings")

class PresaPreferences(private val context: Context) {
    private val dataStore = context.presaDataStore

    val settings: Flow<PresaSettings> = dataStore.data
        .catch { throwable ->
            if (throwable is IOException) emit(emptyPreferences()) else throw throwable
        }
        .map(::readSettings)

    suspend fun current(): PresaSettings = settings.first()

    suspend fun setThemeMode(mode: ThemeMode) = update { it.copy(themeMode = mode) }

    suspend fun setLanguage(language: AppLanguage) {
        LocaleStore.writeTag(context, language.tag)
        update { it.copy(language = language) }
    }

    suspend fun setHapticsEnabled(enabled: Boolean) = update { it.copy(hapticsEnabled = enabled) }

    suspend fun setVolumeKeysEnabled(enabled: Boolean) =
        update { it.copy(volumeKeysEnabled = enabled) }

    suspend fun setRepeatSpeed(speed: RepeatSpeed) = update { it.copy(repeatSpeed = speed) }

    suspend fun setTimerAlertMinutes(minutes: Int) =
        update { it.copy(timerAlertMinutes = minutes.coerceIn(5, 120)) }

    suspend fun setLastMode(mode: AppMode) = update { it.copy(lastMode = mode) }

    suspend fun setLastSubMode(subMode: SlidesSubMode) =
        update { it.copy(lastSubMode = subMode) }

    suspend fun setLastHost(address: String, name: String?) = update {
        it.copy(lastDeviceAddress = address, lastDeviceName = name)
    }

    suspend fun clearLastHost() = update {
        it.copy(lastDeviceAddress = null, lastDeviceName = null)
    }

    suspend fun setKeyOverride(actionId: String, chord: KeyChord) = update {
        it.copy(keyOverrides = it.keyOverrides + (actionId to chord))
    }

    suspend fun resetKeyOverride(actionId: String) = update {
        it.copy(keyOverrides = it.keyOverrides - actionId)
    }

    suspend fun resetAllKeyOverrides() = update {
        it.copy(keyOverrides = emptyMap())
    }

    private suspend fun update(transform: (PresaSettings) -> PresaSettings) {
        dataStore.edit { preferences ->
            val updated = transform(readSettings(preferences))
            preferences[Keys.THEME] = updated.themeMode.name
            preferences[Keys.LANGUAGE] = updated.language.tag
            preferences[Keys.HAPTICS] = updated.hapticsEnabled
            preferences[Keys.VOLUME_KEYS] = updated.volumeKeysEnabled
            preferences[Keys.REPEAT_SPEED] = updated.repeatSpeed.name
            preferences[Keys.TIMER_ALERT_MINUTES] = updated.timerAlertMinutes
            preferences[Keys.LAST_MODE] = updated.lastMode.name
            preferences[Keys.LAST_SUB_MODE] = updated.lastSubMode.name
            preferences[Keys.KEY_OVERRIDES] = KeyRemapCodec.encode(updated.keyOverrides)

            updated.lastDeviceAddress?.let { preferences[Keys.LAST_DEVICE_ADDRESS] = it }
                ?: preferences.remove(Keys.LAST_DEVICE_ADDRESS)
            updated.lastDeviceName?.let { preferences[Keys.LAST_DEVICE_NAME] = it }
                ?: preferences.remove(Keys.LAST_DEVICE_NAME)
        }
    }

    private fun readSettings(preferences: Preferences): PresaSettings {
        val defaults = PresaSettings()
        return PresaSettings(
            themeMode = preferences[Keys.THEME]
                ?.let { raw -> ThemeMode.entries.firstOrNull { it.name == raw } }
                ?: defaults.themeMode,
            language = preferences[Keys.LANGUAGE]
                ?.let(AppLanguage::fromTag)
                ?: defaults.language,
            hapticsEnabled = preferences[Keys.HAPTICS] ?: defaults.hapticsEnabled,
            volumeKeysEnabled = preferences[Keys.VOLUME_KEYS] ?: defaults.volumeKeysEnabled,
            repeatSpeed = preferences[Keys.REPEAT_SPEED]
                ?.let { raw -> RepeatSpeed.entries.firstOrNull { it.name == raw } }
                ?: defaults.repeatSpeed,
            timerAlertMinutes = (preferences[Keys.TIMER_ALERT_MINUTES]
                ?: defaults.timerAlertMinutes).coerceIn(5, 120),
            lastMode = preferences[Keys.LAST_MODE]
                ?.let { raw -> AppMode.entries.firstOrNull { it.name == raw } }
                ?: defaults.lastMode,
            lastSubMode = preferences[Keys.LAST_SUB_MODE]
                ?.let { raw -> SlidesSubMode.entries.firstOrNull { it.name == raw } }
                ?: defaults.lastSubMode,
            lastDeviceAddress = preferences[Keys.LAST_DEVICE_ADDRESS],
            lastDeviceName = preferences[Keys.LAST_DEVICE_NAME],
            keyOverrides = KeyRemapCodec.decode(preferences[Keys.KEY_OVERRIDES]),
        )
    }

    private object Keys {
        val THEME = stringPreferencesKey("theme")
        val LANGUAGE = stringPreferencesKey("language")
        val HAPTICS = booleanPreferencesKey("haptics")
        val VOLUME_KEYS = booleanPreferencesKey("volume_keys")
        val REPEAT_SPEED = stringPreferencesKey("repeat_speed")
        val TIMER_ALERT_MINUTES = intPreferencesKey("timer_alert_minutes")
        val LAST_MODE = stringPreferencesKey("last_mode")
        val LAST_SUB_MODE = stringPreferencesKey("last_sub_mode")
        val LAST_DEVICE_ADDRESS = stringPreferencesKey("last_device_address")
        val LAST_DEVICE_NAME = stringPreferencesKey("last_device_name")
        val KEY_OVERRIDES = stringPreferencesKey("key_overrides")
    }
}
