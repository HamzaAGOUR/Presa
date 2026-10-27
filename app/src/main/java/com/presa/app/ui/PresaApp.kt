package com.presa.app.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.presa.app.R
import com.presa.app.data.LocaleStore
import com.presa.app.data.PresaSettings
import com.presa.app.di.AppContainer
import com.presa.app.ui.components.LocalHaptics
import com.presa.app.ui.components.LocalHapticsEnabled
import com.presa.app.ui.components.PresaBackground
import com.presa.app.ui.connection.ConnectionScreen
import com.presa.app.ui.permission.ConnectPermissionGate
import com.presa.app.ui.presentation.MainScreen
import com.presa.app.ui.presentation.NumberTarget
import com.presa.app.ui.presentation.PresentationEvent
import com.presa.app.ui.presentation.PresentationViewModel
import com.presa.app.ui.settings.AboutScreen
import com.presa.app.ui.settings.KeyRemapScreen
import com.presa.app.ui.settings.SettingsScreen
import com.presa.app.ui.settings.SettingsViewModel
import com.presa.app.ui.theme.PresaTheme
import com.presa.app.ui.timer.TimerAlertHost
import com.presa.app.ui.timer.TimerSection
import com.presa.app.ui.timer.TimerViewModel
import com.presa.app.ui.util.openUrl
import kotlinx.coroutines.launch

private object Destinations {
    const val MAIN = "main"
    const val CONNECTION = "connection"
    const val SETTINGS = "settings"
    const val REMAP = "remap"
    const val ABOUT = "about"
}

@Composable
fun PresaApp(
    container: AppContainer,
    onRecreateForLocale: () -> Unit,
) {
    val settingsViewModel: SettingsViewModel = viewModel(
        factory = SettingsViewModel.factory(container),
    )
    val settings by settingsViewModel.settings.collectAsStateWithLifecycle()
    val currentLanguage = LocalConfiguration.current.locales[0].language
    val context = LocalContext.current

    LaunchedEffect(settings.language.tag) {
        if (settings.language.tag != currentLanguage) {
            LocaleStore.writeTag(context, settings.language.tag)
            onRecreateForLocale()
        }
    }

    PresaTheme(themeMode = settings.themeMode) {
        CompositionLocalProvider(
            LocalHaptics provides container.haptics,
            LocalHapticsEnabled provides settings.hapticsEnabled,
        ) {
            ConnectPermissionGate(container = container) {
                AppContent(
                    container = container,
                    settingsViewModel = settingsViewModel,
                    settings = settings,
                )
            }
        }
    }
}

@Composable
private fun AppContent(
    container: AppContainer,
    settingsViewModel: SettingsViewModel,
    settings: PresaSettings,
) {
    val context = LocalContext.current
    val presentationViewModel: PresentationViewModel = viewModel(
        factory = PresentationViewModel.factory(container),
    )
    val timerViewModel: TimerViewModel = viewModel(
        factory = TimerViewModel.factory(container),
    )

    val presentation by presentationViewModel.uiState.collectAsStateWithLifecycle()
    val hidState by presentationViewModel.hidState.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var screen by rememberSaveable { mutableStateOf(Destinations.MAIN) }

    fun deliverMessage(message: String) {
        scope.launch {
            snackbarHostState.currentSnackbarData?.dismiss()
            snackbarHostState.showSnackbar(
                message = message,
                duration = SnackbarDuration.Short,
            )
        }
    }

    val showMessage: (String) -> Unit = { message -> deliverMessage(message) }

    BackHandler(enabled = screen != Destinations.MAIN) {
        screen = when (screen) {
            Destinations.REMAP, Destinations.ABOUT -> Destinations.SETTINGS
            else -> Destinations.MAIN
        }
    }

    LaunchedEffect(Unit) {
        presentationViewModel.events.collect { event ->
            val message = when (event) {
                PresentationEvent.NotConnected -> context.getString(R.string.message_not_connected)
                PresentationEvent.SendFailed -> context.getString(R.string.message_send_failed)
                is PresentationEvent.NumberSent -> when (event.target) {
                    NumberTarget.SLIDE -> context.getString(
                        R.string.numberpad_slide_sent,
                        event.number,
                    )

                    NumberTarget.PAGE -> context.getString(
                        R.string.numberpad_page_sent,
                        event.number,
                    )
                }
            }
            deliverMessage(message)
        }
    }

    LaunchedEffect(screen) {
        presentationViewModel.setVolumeKeysActive(screen == Destinations.MAIN)
    }
    DisposableEffect(Unit) {
        onDispose { presentationViewModel.setVolumeKeysActive(false) }
    }

    TimerAlertHost(timerViewModel)

    PresaBackground {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = Color.Transparent,
            snackbarHost = { SnackbarHost(snackbarHostState) },
        ) { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
            ) {
                when (screen) {
                    Destinations.MAIN -> MainScreen(
                        presentation = presentation,
                        hidState = hidState,
                        timerContent = { TimerSection(timerViewModel) },
                        onOpenConnection = { screen = Destinations.CONNECTION },
                        onOpenSettings = { screen = Destinations.SETTINGS },
                        onReconnect = presentationViewModel::reconnect,
                        onModeChange = presentationViewModel::setMode,
                        onSubModeChange = presentationViewModel::setSubMode,
                        onActionPress = presentationViewModel::onActionPress,
                        onActionRelease = presentationViewModel::onActionRelease,
                        onSendNumber = presentationViewModel::sendNumber,
                        onTrackpadMove = presentationViewModel::onTrackpadMove,
                        onTrackpadScroll = presentationViewModel::onTrackpadScroll,
                        onTrackpadLeftClick = presentationViewModel::onTrackpadLeftClick,
                        onTrackpadRightClick = presentationViewModel::onTrackpadRightClick,
                    )

                    Destinations.CONNECTION -> ConnectionScreen(
                        container = container,
                        onBack = { screen = Destinations.MAIN },
                        onMessage = showMessage,
                    )

                    Destinations.SETTINGS -> SettingsScreen(
                        settings = settings,
                        onBack = { screen = Destinations.MAIN },
                        onThemeChange = settingsViewModel::setThemeMode,
                        onLanguageChange = settingsViewModel::setLanguage,
                        onHapticsChange = settingsViewModel::setHapticsEnabled,
                        onVolumeKeysChange = settingsViewModel::setVolumeKeysEnabled,
                        onRepeatSpeedChange = settingsViewModel::setRepeatSpeed,
                        onTimerMinutesChange = settingsViewModel::setTimerAlertMinutes,
                        onOpenRemap = { screen = Destinations.REMAP },
                        onOpenAbout = { screen = Destinations.ABOUT },
                        onMessage = showMessage,
                    )

                    Destinations.REMAP -> KeyRemapScreen(
                        settings = settings,
                        onBack = { screen = Destinations.SETTINGS },
                        onSetOverride = settingsViewModel::setKeyOverride,
                        onResetOverride = settingsViewModel::resetKeyOverride,
                        onResetAll = settingsViewModel::resetAllKeyOverrides,
                    )

                    Destinations.ABOUT -> AboutScreen(
                        onBack = { screen = Destinations.SETTINGS },
                        onOpenUrl = { url ->
                            if (!context.openUrl(url)) {
                                showMessage(context.getString(R.string.message_link_failed))
                            }
                        },
                    )
                }
            }
        }
    }
}
