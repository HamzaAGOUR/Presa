package com.presa.app.ui.presentation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.presa.app.R
import com.presa.app.domain.hid.HidIssue
import com.presa.app.domain.hid.HidState
import com.presa.app.domain.model.AppMode
import com.presa.app.domain.model.KeyAction
import com.presa.app.domain.model.SlidesSubMode
import com.presa.app.ui.components.PresaHeader
import com.presa.app.ui.components.SegmentOption
import com.presa.app.ui.components.SegmentedControl
import com.presa.app.ui.connection.ConnectionStatusBar
import com.presa.app.ui.connection.DisconnectionBanner
import com.presa.app.ui.theme.LocalPresaColors

@Composable
fun MainScreen(
    presentation: PresentationUiState,
    hidState: HidState,
    timerContent: @Composable () -> Unit,
    onOpenConnection: () -> Unit,
    onOpenSettings: () -> Unit,
    onReconnect: () -> Unit,
    onModeChange: (AppMode) -> Unit,
    onSubModeChange: (SlidesSubMode) -> Unit,
    onActionPress: (KeyAction) -> Unit,
    onActionRelease: () -> Unit,
    onSendNumber: (String, NumberTarget) -> Unit,
    onTrackpadMove: (Float, Float) -> Unit,
    onTrackpadScroll: (Float, Float) -> Unit,
    onTrackpadLeftClick: () -> Unit,
    onTrackpadRightClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LocalPresaColors.current
    val enabled = hidState.isConnected

    val modeOptions = listOf(
        SegmentOption(AppMode.VISUALIZER.name, stringResource(R.string.mode_visualizer)),
        SegmentOption(AppMode.SLIDES.name, stringResource(R.string.mode_slides)),
    )
    val subModeOptions = listOf(
        SegmentOption(SlidesSubMode.POWERPOINT.name, stringResource(R.string.submode_powerpoint)),
        SegmentOption(SlidesSubMode.PDF.name, stringResource(R.string.submode_pdf)),
    )

    Column(modifier = modifier.fillMaxSize()) {
        PresaHeader(onSettingsClick = onOpenSettings)

        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            ConnectionStatusBar(
                state = hidState,
                onOpenConnection = onOpenConnection,
                onReconnect = onReconnect,
            )
            if (hidState.issue == HidIssue.CONNECTION_LOST) {
                Spacer(Modifier.height(10.dp))
                DisconnectionBanner(state = hidState, onReconnect = onReconnect)
            }
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp),
        ) {
            timerContent()
            Spacer(Modifier.height(14.dp))
            SegmentedControl(
                options = modeOptions,
                selectedId = presentation.mode.name,
                onSelect = { id -> onModeChange(AppMode.valueOf(id)) },
            )
            if (presentation.mode == AppMode.SLIDES) {
                Spacer(Modifier.height(9.dp))
                SegmentedControl(
                    options = subModeOptions,
                    selectedId = presentation.subMode.name,
                    onSelect = { id -> onSubModeChange(SlidesSubMode.valueOf(id)) },
                    activeColor = colors.forwardBlue,
                )
            }
            Spacer(Modifier.height(18.dp))

            when (presentation.mode) {
                AppMode.VISUALIZER -> VisualizerMode(
                    actions = presentation.actions,
                    overrides = presentation.settings.keyOverrides,
                    enabled = enabled,
                    onPress = onActionPress,
                    onRelease = onActionRelease,
                    modifier = Modifier.fillMaxWidth(),
                )

                AppMode.SLIDES -> SlidesMode(
                    subMode = presentation.subMode,
                    actions = presentation.actions,
                    overrides = presentation.settings.keyOverrides,
                    enabled = enabled,
                    onPress = onActionPress,
                    onRelease = onActionRelease,
                    onSendNumber = onSendNumber,
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            Spacer(Modifier.height(18.dp))

            TrackpadSection(
                enabled = enabled,
                onMove = onTrackpadMove,
                onScroll = onTrackpadScroll,
                onLeftClick = onTrackpadLeftClick,
                onRightClick = onTrackpadRightClick,
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(Modifier.height(20.dp))
        }
    }
}
