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
import androidx.compose.material.icons.rounded.ArrowDownward
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.material.icons.rounded.FitScreen
import androidx.compose.material.icons.rounded.FirstPage
import androidx.compose.material.icons.rounded.Fullscreen
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Slideshow
import androidx.compose.material.icons.rounded.ZoomIn
import androidx.compose.material.icons.rounded.ZoomOut
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.presa.app.R
import com.presa.app.domain.model.KeyAction
import com.presa.app.domain.model.KeyChord
import com.presa.app.domain.model.PdfKeyMap
import com.presa.app.domain.model.PowerPointKeyMap
import com.presa.app.domain.model.SlidesSubMode
import com.presa.app.ui.components.KeyTone
import com.presa.app.ui.components.NumberPadCard
import com.presa.app.ui.theme.LocalPresaColors

@Composable
fun SlidesMode(
    subMode: SlidesSubMode,
    actions: List<KeyAction>,
    overrides: Map<String, KeyChord>,
    enabled: Boolean,
    onPress: (KeyAction) -> Unit,
    onRelease: () -> Unit,
    onSendNumber: (String, NumberTarget) -> Unit,
    modifier: Modifier = Modifier,
) {
    when (subMode) {
        SlidesSubMode.POWERPOINT -> PowerPointMode(
            actions = actions,
            overrides = overrides,
            enabled = enabled,
            onPress = onPress,
            onRelease = onRelease,
            onSendNumber = onSendNumber,
            modifier = modifier,
        )

        SlidesSubMode.PDF -> PdfMode(
            actions = actions,
            overrides = overrides,
            enabled = enabled,
            onPress = onPress,
            onRelease = onRelease,
            onSendNumber = onSendNumber,
            modifier = modifier,
        )
    }
}

@Composable
private fun PowerPointMode(
    actions: List<KeyAction>,
    overrides: Map<String, KeyChord>,
    enabled: Boolean,
    onPress: (KeyAction) -> Unit,
    onRelease: () -> Unit,
    onSendNumber: (String, NumberTarget) -> Unit,
    modifier: Modifier = Modifier,
) {
    val byId = remember(actions) { actions.associateBy(KeyAction::id) }
    val previous = byId.getValue(PowerPointKeyMap.PREVIOUS)
    val next = byId.getValue(PowerPointKeyMap.NEXT)
    val first = byId.getValue(PowerPointKeyMap.FIRST)
    val last = byId.getValue(PowerPointKeyMap.LAST)
    val startShow = byId.getValue(PowerPointKeyMap.START_SHOW)
    val fromCurrent = byId.getValue(PowerPointKeyMap.FROM_CURRENT)
    val exit = byId.getValue(PowerPointKeyMap.EXIT)
    val black = byId.getValue(PowerPointKeyMap.BLACK)
    val white = byId.getValue(PowerPointKeyMap.WHITE)
    val laser = byId.getValue(PowerPointKeyMap.LASER)
    val pen = byId.getValue(PowerPointKeyMap.PEN)
    val eraser = byId.getValue(PowerPointKeyMap.ERASER)
    val colors = LocalPresaColors.current

    Column(modifier = modifier.fillMaxWidth()) {
        ModeSection(title = stringResource(R.string.slides_section_navigation)) {
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
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                PresaActionButton(
                    action = first,
                    overrides = overrides,
                    enabled = enabled,
                    onPress = onPress,
                    onRelease = onRelease,
                    icon = Icons.Rounded.FirstPage,
                    modifier = Modifier.weight(1f),
                )
                PresaActionButton(
                    action = last,
                    overrides = overrides,
                    enabled = enabled,
                    onPress = onPress,
                    onRelease = onRelease,
                    icon = Icons.AutoMirrored.Rounded.LastPage,
                    modifier = Modifier.weight(1f),
                )
            }
            Text(
                text = stringResource(R.string.slides_repeat_hint),
                style = MaterialTheme.typography.bodySmall,
                color = colors.muted,
            )
        }

        Spacer(Modifier.height(16.dp))

        ModeSection(title = stringResource(R.string.slides_section_show)) {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                PresaActionButton(
                    action = startShow,
                    overrides = overrides,
                    enabled = enabled,
                    onPress = onPress,
                    onRelease = onRelease,
                    tone = KeyTone.Navy,
                    icon = Icons.Rounded.Slideshow,
                    modifier = Modifier.weight(1f),
                )
                PresaActionButton(
                    action = fromCurrent,
                    overrides = overrides,
                    enabled = enabled,
                    onPress = onPress,
                    onRelease = onRelease,
                    icon = Icons.Rounded.PlayArrow,
                    modifier = Modifier.weight(1f),
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        ModeSection(title = stringResource(R.string.slides_section_tools)) {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                PresaActionButton(
                    action = black,
                    overrides = overrides,
                    enabled = enabled,
                    onPress = onPress,
                    onRelease = onRelease,
                    tone = KeyTone.Navy,
                    modifier = Modifier.weight(1f),
                )
                PresaActionButton(
                    action = white,
                    overrides = overrides,
                    enabled = enabled,
                    onPress = onPress,
                    onRelease = onRelease,
                    modifier = Modifier.weight(1f),
                )
                PresaActionButton(
                    action = laser,
                    overrides = overrides,
                    enabled = enabled,
                    onPress = onPress,
                    onRelease = onRelease,
                    tone = KeyTone.Gold,
                    modifier = Modifier.weight(1f),
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                PresaActionButton(
                    action = pen,
                    overrides = overrides,
                    enabled = enabled,
                    onPress = onPress,
                    onRelease = onRelease,
                    modifier = Modifier.weight(1f),
                )
                PresaActionButton(
                    action = eraser,
                    overrides = overrides,
                    enabled = enabled,
                    onPress = onPress,
                    onRelease = onRelease,
                    modifier = Modifier.weight(1f),
                )
                Spacer(Modifier.weight(1f))
            }
        }

        Spacer(Modifier.height(16.dp))

        NumberPadCard(
            title = stringResource(R.string.slides_goto_slide),
            enabled = enabled,
            onSend = { number -> onSendNumber(number, NumberTarget.SLIDE) },
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(Modifier.height(16.dp))

        ModeSection(title = stringResource(R.string.slides_section_navigation)) {
            PresaActionButton(
                action = exit,
                overrides = overrides,
                enabled = enabled,
                onPress = onPress,
                onRelease = onRelease,
                tone = KeyTone.Danger,
                icon = Icons.AutoMirrored.Rounded.ExitToApp,
                modifier = Modifier.fillMaxWidth(),
            )
            Text(
                text = stringResource(R.string.slides_exit_note),
                style = MaterialTheme.typography.bodySmall,
                color = colors.muted,
            )
        }
    }
}

@Composable
private fun PdfMode(
    actions: List<KeyAction>,
    overrides: Map<String, KeyChord>,
    enabled: Boolean,
    onPress: (KeyAction) -> Unit,
    onRelease: () -> Unit,
    onSendNumber: (String, NumberTarget) -> Unit,
    modifier: Modifier = Modifier,
) {
    val byId = remember(actions) { actions.associateBy(KeyAction::id) }
    val previous = byId.getValue(PdfKeyMap.PREVIOUS_PAGE)
    val next = byId.getValue(PdfKeyMap.NEXT_PAGE)
    val first = byId.getValue(PdfKeyMap.FIRST)
    val last = byId.getValue(PdfKeyMap.LAST)
    val scrollUp = byId.getValue(PdfKeyMap.SCROLL_UP)
    val scrollDown = byId.getValue(PdfKeyMap.SCROLL_DOWN)
    val fullscreen = byId.getValue(PdfKeyMap.FULLSCREEN)
    val fullscreenAlt = byId.getValue(PdfKeyMap.FULLSCREEN_ALT)
    val zoomIn = byId.getValue(PdfKeyMap.ZOOM_IN)
    val zoomOut = byId.getValue(PdfKeyMap.ZOOM_OUT)
    val fit = byId.getValue(PdfKeyMap.FIT)
    val exit = byId.getValue(PdfKeyMap.EXIT)
    val colors = LocalPresaColors.current

    Column(modifier = modifier.fillMaxWidth()) {
        ModeSection(title = stringResource(R.string.slides_section_navigation)) {
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
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                PresaActionButton(
                    action = first,
                    overrides = overrides,
                    enabled = enabled,
                    onPress = onPress,
                    onRelease = onRelease,
                    icon = Icons.Rounded.FirstPage,
                    modifier = Modifier.weight(1f),
                )
                PresaActionButton(
                    action = last,
                    overrides = overrides,
                    enabled = enabled,
                    onPress = onPress,
                    onRelease = onRelease,
                    icon = Icons.AutoMirrored.Rounded.LastPage,
                    modifier = Modifier.weight(1f),
                )
            }
            Text(
                text = stringResource(R.string.slides_repeat_hint),
                style = MaterialTheme.typography.bodySmall,
                color = colors.muted,
            )
        }

        Spacer(Modifier.height(16.dp))

        ModeSection(title = stringResource(R.string.slides_section_pdf_view)) {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                PresaActionButton(
                    action = scrollUp,
                    overrides = overrides,
                    enabled = enabled,
                    onPress = onPress,
                    onRelease = onRelease,
                    icon = Icons.Rounded.ArrowUpward,
                    modifier = Modifier.weight(1f),
                )
                PresaActionButton(
                    action = scrollDown,
                    overrides = overrides,
                    enabled = enabled,
                    onPress = onPress,
                    onRelease = onRelease,
                    icon = Icons.Rounded.ArrowDownward,
                    modifier = Modifier.weight(1f),
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                PresaActionButton(
                    action = fullscreen,
                    overrides = overrides,
                    enabled = enabled,
                    onPress = onPress,
                    onRelease = onRelease,
                    tone = KeyTone.Navy,
                    icon = Icons.Rounded.Fullscreen,
                    modifier = Modifier.weight(1f),
                )
                PresaActionButton(
                    action = fullscreenAlt,
                    overrides = overrides,
                    enabled = enabled,
                    onPress = onPress,
                    onRelease = onRelease,
                    icon = Icons.Rounded.Fullscreen,
                    modifier = Modifier.weight(1f),
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        ModeSection(title = stringResource(R.string.slides_section_pdf_zoom)) {
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

        NumberPadCard(
            title = stringResource(R.string.slides_goto_page),
            enabled = enabled,
            onSend = { number -> onSendNumber(number, NumberTarget.PAGE) },
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(Modifier.height(16.dp))

        ModeSection(title = stringResource(R.string.slides_section_navigation)) {
            PresaActionButton(
                action = exit,
                overrides = overrides,
                enabled = enabled,
                onPress = onPress,
                onRelease = onRelease,
                tone = KeyTone.Danger,
                icon = Icons.AutoMirrored.Rounded.ExitToApp,
                modifier = Modifier.fillMaxWidth(),
            )
            Text(
                text = stringResource(R.string.slides_exit_note),
                style = MaterialTheme.typography.bodySmall,
                color = colors.muted,
            )
        }
    }
}
