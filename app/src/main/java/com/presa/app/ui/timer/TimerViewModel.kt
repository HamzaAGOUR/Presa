package com.presa.app.ui.timer

import android.os.SystemClock
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.presa.app.data.Haptics
import com.presa.app.data.PresaPreferences
import com.presa.app.di.AppContainer
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

data class TimerUiState(
    val elapsedMs: Long = 0L,
    val running: Boolean = false,
    val alertMinutes: Int = 30,
    val alertPending: Boolean = false,
) {
    val elapsedLabel: String get() = TimerFormat.clock(elapsedMs)

    val progress: Float
        get() = (elapsedMs.toFloat() / (alertMinutes.coerceAtLeast(1) * 60_000f))
            .coerceIn(0f, 1f)
}

/**
 * Lesson timer shared by every screen. It keeps its own VM scope, so navigating
 * to connection or settings does not reset the count.
 */
class TimerViewModel(
    preferences: PresaPreferences,
    private val haptics: Haptics,
) : ViewModel() {
    private val _state = MutableStateFlow(TimerUiState())
    val state: StateFlow<TimerUiState> = _state.asStateFlow()

    private var ticker: Job? = null
    private var startedAtElapsed = 0L
    private var elapsedBeforeStart = 0L
    private var alertFired = false

    init {
        viewModelScope.launch {
            preferences.settings.collect { settings ->
                _state.update { it.copy(alertMinutes = settings.timerAlertMinutes) }
                checkAlert()
            }
        }
    }

    fun toggle() {
        if (_state.value.running) pause() else start()
    }

    fun start() {
        if (_state.value.running) return
        elapsedBeforeStart = _state.value.elapsedMs
        startedAtElapsed = SystemClock.elapsedRealtime()
        _state.update { it.copy(running = true) }
        launchTicker()
    }

    fun pause() {
        if (!_state.value.running) return
        ticker?.cancel()
        ticker = null
        val elapsed = currentElapsed()
        elapsedBeforeStart = elapsed
        _state.update { it.copy(elapsedMs = elapsed, running = false) }
        checkAlert()
    }

    fun reset() {
        ticker?.cancel()
        ticker = null
        startedAtElapsed = 0L
        elapsedBeforeStart = 0L
        alertFired = false
        _state.update {
            it.copy(elapsedMs = 0L, running = false, alertPending = false)
        }
    }

    fun acknowledgeAlert() {
        _state.update { it.copy(alertPending = false) }
    }

    private fun launchTicker() {
        ticker?.cancel()
        ticker = viewModelScope.launch {
            while (isActive) {
                val elapsed = currentElapsed()
                _state.update { it.copy(elapsedMs = elapsed) }
                checkAlert()
                delay(TICK_MS)
            }
        }
    }

    private fun currentElapsed(): Long {
        if (!_state.value.running) return elapsedBeforeStart
        return elapsedBeforeStart + (SystemClock.elapsedRealtime() - startedAtElapsed)
    }

    private fun checkAlert() {
        val snapshot = _state.value
        val threshold = snapshot.alertMinutes.coerceAtLeast(1) * 60_000L
        if (!alertFired && snapshot.elapsedMs > 0L && snapshot.elapsedMs >= threshold) {
            alertFired = true
            haptics.timerAlert()
            _state.update { it.copy(alertPending = true) }
        }
    }

    companion object {
        private const val TICK_MS = 250L

        fun factory(container: AppContainer) = viewModelFactory {
            initializer {
                TimerViewModel(
                    preferences = container.preferences,
                    haptics = container.haptics,
                )
            }
        }
    }
}
