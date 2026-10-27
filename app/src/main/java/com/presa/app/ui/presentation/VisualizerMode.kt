package com.presa.app.ui.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.automirrored.rounded.ExitToApp
import androidx.compose.material.icons.automirrored.rounded.LastPage
import androidx.compose.material.icons.rounded.FitScreen
import androidx.compose.material.icons.rounded.FirstPage
import androidx.compose.material.icons.rounded.Fullscreen
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.RestartAlt
import androidx.compose.material.icons.rounded.Slideshow
import androidx.compose.material.icons.rounded.ViewWeek
import androidx.compose.material.icons.rounded.ZoomIn
import androidx.compose.material.icons.rounded.ZoomOut
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.presa.app.R
import com.presa.app.domain.model.KeyAction
import com.presa.app.domain.model.KeyChord
import com.presa.app.domain.model.VisualizerKeyMap
import com.presa.app.ui.components.KeyTone
import com.presa.app.ui.theme.LocalPresaColors

@Composable
fun VisualizerMode(
    actions: List<KeyAction>,
    overrides: Map<String, KeyChord>,
    enabled: Boolean,
    onPress: (KeyAction) -> Unit,
    onRelease: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val byId = remember(actions) { actions.associateBy(KeyAction::id) }
    val previous = byId.getValue(VisualizerKeyMap.PREVIOUS)
    val next = byId.getValue(VisualizerKeyMap.NEXT)
    val playPause = byId.getValue(VisualizerKeyMap.PLAY_PAUSE)
    val start = byId.getValue(VisualizerKeyMap.START)
    val end = byId.getValue(VisualizerKeyMap.END)
    val slower = byId.getValue(VisualizerKeyMap.SLOWER)
    val faster = byId.getValue(VisualizerKeyMap.FASTER)
    val zoomIn = byId.getValue(VisualizerKeyMap.ZOOM_IN)
    val zoomOut = byId.getValue(VisualizerKeyMap.ZOOM_OUT)
    val fit = byId.getValue(VisualizerKeyMap.FIT)
    val fullscreen = byId.getValue(VisualizerKeyMap.FULLSCREEN)
    val panels = byId.getValue(VisualizerKeyMap.PANELS)
    val presentation = byId.getValue(VisualizerKeyMap.PRESENTATION)
    val reset = byId.getValue(VisualizerKeyMap.RESET)
    val back = byId.getValue(VisualizerKeyMap.BACK)

    var showReset by rememberSaveable { mutableStateOf(false) }
    val colors = LocalPresaColors.current

    Column(modifier = modifier.fillMaxWidth()) {
        ModeSection(title = stringResource(R.string.visualizer_section_navigation)) {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                PresaActionButton(
                    action = previous,
                    overrides = overrides,
                    enabled = enabled,
                    onPress = onPress,
                    onRelease = onRelease,
                    tone = KeyTone.Previous,
                    big = true,
                    minHeight = 112.dp,
                    icon = Icons.AutoMirrored.Rounded.ArrowBack,
                    modifier = Modifier.weight(1f),
                )
                PresaActionButton(
                    action = next,
                    overrides = overrides,
                    enabled = enabled,
                    onPress = onPress,
                    onRelease = onRelease,
                    tone = KeyTone.Next,
                    big = true,
                    minHeight = 112.dp,
                    icon = Icons.AutoMirrored.Rounded.ArrowForward,
                    modifier = Modifier.weight(1f),
                )
            }
            Text(
                text = stringResource(R.string.visualizer_repeat_hint),
                style = MaterialTheme.typography.bodySmall,
                color = colors.muted,
            )
        }

        Spacer(Modifier.height(16.dp))

        ModeSection(title = stringResource(R.string.visualizer_section_playback)) {
            PresaActionButton(
                action = playPause,
                overrides = overrides,
                enabled = enabled,
                onPress = onPress,
                onRelease = onRelease,
                tone = KeyTone.Navy,
                big = true,
                minHeight = 86.dp,
                icon = Icons.Rounded.PlayArrow,
                modifier = Modifier.fillMaxWidth(),
            )
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                PresaActionButton(
                    action = start,
                    overrides = overrides,
                    enabled = enabled,
                    onPress = onPress,
                    onRelease = onRelease,
                    icon = Icons.Rounded.FirstPage,
                    modifier = Modifier.weight(1f),
                )
                PresaActionButton(
                    action = end,
                    overrides = overrides,
                    enabled = enabled,
                    onPress = onPress,
                    onRelease = onRelease,
                    icon = Icons.AutoMirrored.Rounded.LastPage,
                    modifier = Modifier.weight(1f),
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                PresaActionButton(
                    action = slower,
                    overrides = overrides,
                    enabled = enabled,
                    onPress = onPress,
                    onRelease = onRelease,
                    icon = Icons.Rounded.KeyboardArrowDown,
                    modifier = Modifier.weight(1f),
                )
                PresaActionButton(
                    action = faster,
                    overrides = overrides,
                    enabled = enabled,
                    onPress = onPress,
                    onRelease = onRelease,
                    icon = Icons.Rounded.KeyboardArrowUp,
                    modifier = Modifier.weight(1f),
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        ModeSection(title = stringResource(R.string.visualizer_section_zoom)) {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                PresaActionButton(
                    action = zoomIn,
                    overrides = overrides,
                    enabled = enabled,
                    onPress = onPress,
                    onRelease = onRelease,
                    icon = Icons.Rounded.ZoomIn,
                    modifier = Modifier.weight(1f),
                )
                PresaActionButton(
                    action = zoomOut,
                    overrides = overrides,
                    enabled = enabled,
                    onPress = onPress,
                    onRelease = onRelease,
                    icon = Icons.Rounded.ZoomOut,
                    modifier = Modifier.weight(1f),
                )
                PresaActionButton(
                    action = fit,
                    overrides = overrides,
                    enabled = enabled,
                    onPress = onPress,
                    onRelease = onRelease,
                    icon = Icons.Rounded.FitScreen,
                    modifier = Modifier.weight(1f),
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        ModeSection(title = stringResource(R.string.visualizer_section_view)) {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                PresaActionButton(
                    action = panels,
                    overrides = overrides,
                    enabled = enabled,
                    onPress = onPress,
                    onRelease = onRelease,
                    icon = Icons.Rounded.ViewWeek,
                    modifier = Modifier.weight(1f),
                )
                PresaActionButton(
                    action = presentation,
                    overrides = overrides,
                    enabled = enabled,
                    onPress = onPress,
                    onRelease = onRelease,
                    icon = Icons.Rounded.Slideshow,
                    modifier = Modifier.weight(1f),
                )
                PresaActionButton(
                    action = reset,
                    overrides = overrides,
                    enabled = enabled,
                    onPress = { showReset = true },
                    onRelease = onRelease,
                    tone = KeyTone.Danger,
                    icon = Icons.Rounded.RestartAlt,
                    modifier = Modifier.weight(1f),
                )
            }
            PresaActionButton(
                action = fullscreen,
                overrides = overrides,
                enabled = enabled,
                onPress = onPress,
                onRelease = onRelease,
                icon = Icons.Rounded.Fullscreen,
                modifier = Modifier.fillMaxWidth(),
            )
            PresaActionButton(
                action = back,
                overrides = overrides,
                enabled = enabled,
                onPress = onPress,
                onRelease = onRelease,
                icon = Icons.AutoMirrored.Rounded.ExitToApp,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }

    if (showReset) {
        AlertDialog(
            onDismissRequest = { showReset = false },
            title = {
                Text(
                    text = stringResource(R.string.visualizer_reset_title),
                    color = colors.ink,
                )
            },
            text = {
                Text(
                    text = stringResource(R.string.visualizer_reset_body),
                    color = colors.ink2,
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showReset = false
                        onPress(reset)
                        onRelease()
                    },
                ) {
                    Text(
                        text = stringResource(R.string.visualizer_reset_confirm),
                        color = colors.alert,
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showReset = false }) {
                    Text(
                        text = stringResource(R.string.dialog_cancel),
                        color = colors.ink2,
                    )
                }
            },
            containerColor = colors.panel,
            titleContentColor = colors.ink,
            textContentColor = colors.ink2,
        )
    }
}
