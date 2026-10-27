package com.presa.app.ui.presentation

import android.os.SystemClock
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.presa.app.data.PresaPreferences
import com.presa.app.data.PresaSettings
import com.presa.app.data.VolumeKeyRouter
import com.presa.app.di.AppContainer
import com.presa.app.domain.hid.BluetoothHidManager
import com.presa.app.domain.hid.HidSendFailure
import com.presa.app.domain.hid.HidSendResult
import com.presa.app.domain.hid.HidState
import com.presa.app.domain.model.AppMode
import com.presa.app.domain.model.HidKeys
import com.presa.app.domain.model.KeyAction
import com.presa.app.domain.model.KeyChord
import com.presa.app.domain.model.KeyMaps
import com.presa.app.domain.model.PdfKeyMap
import com.presa.app.domain.model.PowerPointKeyMap
import com.presa.app.domain.model.SlidesSubMode
import com.presa.app.domain.model.VisualizerKeyMap
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

data class PresentationUiState(
    val mode: AppMode = AppMode.VISUALIZER,
    val subMode: SlidesSubMode = SlidesSubMode.POWERPOINT,
    val settings: PresaSettings = PresaSettings(),
) {
    val actions: List<KeyAction>
        get() = KeyMaps.actionsFor(mode, subMode)
}

enum class NumberTarget {
    SLIDE,
    PAGE,
}

sealed interface PresentationEvent {
    data object NotConnected : PresentationEvent
    data object SendFailed : PresentationEvent
    data class NumberSent(val number: String, val target: NumberTarget) : PresentationEvent
}

/**
 * Drives the remote itself: persisted mode, key delivery, held-key repeat,
 * optional volume-key mapping and digit sequences for "go to slide/page".
 */
class PresentationViewModel(
    private val preferences: PresaPreferences,
    private val hidManager: BluetoothHidManager,
    private val volumeKeys: VolumeKeyRouter,
) : ViewModel() {
    private val mode = MutableStateFlow(AppMode.VISUALIZER)
    private val subMode = MutableStateFlow(SlidesSubMode.POWERPOINT)

    val uiState: StateFlow<PresentationUiState> =
        combine(preferences.settings, mode, subMode) { settings, selectedMode, selectedSubMode ->
            PresentationUiState(
                mode = selectedMode,
                subMode = selectedSubMode,
                settings = settings,
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = PresentationUiState(),
        )

    val hidState: StateFlow<HidState> = hidManager.state

    private val _events = Channel<PresentationEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    private var actionRepeatJob: Job? = null
    private var volumeRepeatJob: Job? = null
    private var volumeScreenActive = false
    private var lastFailureNoticeAt = 0L

    private val volumeHandler: (Boolean, Boolean, Int) -> Boolean = { isNext, isDown, repeatCount ->
        handleVolumeKey(isNext, isDown, repeatCount)
    }

    init {
        volumeKeys.handler = volumeHandler
        viewModelScope.launch {
            val stored = preferences.current()
            mode.value = stored.lastMode
            subMode.value = stored.lastSubMode
        }
    }

    fun setMode(newMode: AppMode) {
        if (mode.value == newMode) return
        mode.value = newMode
        viewModelScope.launch { preferences.setLastMode(newMode) }
    }

    fun setSubMode(newSubMode: SlidesSubMode) {
        if (subMode.value == newSubMode) return
        subMode.value = newSubMode
        viewModelScope.launch { preferences.setLastSubMode(newSubMode) }
    }

    /** Sends immediately; repeatable actions keep sending until [onActionRelease]. */
    fun onActionPress(action: KeyAction) {
        actionRepeatJob?.cancel()

        if (!action.repeatable) {
            viewModelScope.launch { deliverAction(action.id, reportFailure = true) }
            return
        }

        actionRepeatJob = viewModelScope.launch {
            var firstSend = true
            while (isActive && hidManager.state.value.isConnected) {
                deliverAction(action.id, reportFailure = true)
                val speed = uiState.value.settings.repeatSpeed
                delay(if (firstSend) speed.initialDelayMs else speed.intervalMs)
                firstSend = false
            }
        }
    }

    fun onActionRelease() {
        actionRepeatJob?.cancel()
        actionRepeatJob = null
    }

    fun sendNumber(raw: String, target: NumberTarget) {
        val number = raw.filter(Char::isDigit).take(MAX_NUMBER_DIGITS)
        if (number.isEmpty()) return

        actionRepeatJob?.cancel()
        volumeRepeatJob?.cancel()
        viewModelScope.launch {
            if (typeNumber(number)) {
                _events.send(PresentationEvent.NumberSent(number, target))
            }
        }
    }

    fun setVolumeKeysActive(active: Boolean) {
        volumeScreenActive = active
        if (!active) {
            volumeRepeatJob?.cancel()
            volumeRepeatJob = null
        }
    }

    fun reconnect() = hidManager.reconnect()

    private suspend fun deliverAction(actionId: String, reportFailure: Boolean) {
        val action = KeyMaps.find(actionId) ?: return
        val chord = KeyMaps.effectiveChord(action, uiState.value.settings.keyOverrides)
        val result = hidManager.tap(chord)
        if (reportFailure) reportFailure(result)
    }

    private suspend fun typeNumber(number: String): Boolean {
        for (digit in number) {
            val result = hidManager.tap(KeyMaps.digitChord(digit))
            if (!result.delivered) {
                reportFailure(result)
                return false
            }
            delay(DIGIT_INTERVAL_MS)
        }
        val enter = hidManager.tap(KeyChord.plain(HidKeys.ENTER))
        if (!enter.delivered) {
            reportFailure(enter)
            return false
        }
        return true
    }

    private fun reportFailure(result: HidSendResult) {
        if (result.delivered) return
        val now = SystemClock.elapsedRealtime()
        if (now - lastFailureNoticeAt < FAILURE_NOTICE_INTERVAL_MS) return
        lastFailureNoticeAt = now

        _events.trySend(
            if (result.reason == HidSendFailure.NOT_CONNECTED) {
                PresentationEvent.NotConnected
            } else {
                PresentationEvent.SendFailed
            },
        )
    }

    private fun handleVolumeKey(isNext: Boolean, isDown: Boolean, repeatCount: Int): Boolean {
        val state = uiState.value
        if (!volumeScreenActive ||
            !state.settings.volumeKeysEnabled ||
            !hidManager.state.value.isConnected
        ) {
            return false
        }

        val action = volumeActionForDirection(state, isNext) ?: return false
        if (isDown) {
            if (repeatCount == 0) startVolumeRepeat(action)
            return true
        }

        volumeRepeatJob?.cancel()
        volumeRepeatJob = null
        return true
    }

    private fun startVolumeRepeat(action: KeyAction) {
        volumeRepeatJob?.cancel()
        volumeRepeatJob = viewModelScope.launch {
            var firstSend = true
            while (isActive && hidManager.state.value.isConnected) {
                deliverAction(action.id, reportFailure = firstSend)
                val speed = uiState.value.settings.repeatSpeed
                delay(if (firstSend) speed.initialDelayMs else speed.intervalMs)
                firstSend = false
            }
        }
    }

    private fun volumeActionForDirection(state: PresentationUiState, isNext: Boolean): KeyAction? {
        val actionId = when (state.mode) {
            AppMode.VISUALIZER -> if (isNext) VisualizerKeyMap.NEXT else VisualizerKeyMap.PREVIOUS
            AppMode.SLIDES -> when (state.subMode) {
                SlidesSubMode.POWERPOINT -> if (isNext) {
                    PowerPointKeyMap.NEXT
                } else {
                    PowerPointKeyMap.PREVIOUS
                }

                SlidesSubMode.PDF -> if (isNext) PdfKeyMap.NEXT_PAGE else PdfKeyMap.PREVIOUS_PAGE
            }
        }
        return KeyMaps.find(actionId)
    }

    override fun onCleared() {
        actionRepeatJob?.cancel()
        volumeRepeatJob?.cancel()
        if (volumeKeys.handler === volumeHandler) volumeKeys.handler = null
        super.onCleared()
    }

    companion object {
        private const val MAX_NUMBER_DIGITS = 4
        private const val DIGIT_INTERVAL_MS = 45L
        private const val FAILURE_NOTICE_INTERVAL_MS = 1_200L

        fun factory(container: AppContainer) = viewModelFactory {
            initializer {
                PresentationViewModel(
                    preferences = container.preferences,
                    hidManager = container.hidManager,
                    volumeKeys = container.volumeKeys,
                )
            }
        }
    }
}
