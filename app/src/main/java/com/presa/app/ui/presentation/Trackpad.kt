package com.presa.app.ui.presentation

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.TouchApp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerInputScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.disabled
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.presa.app.R
import com.presa.app.ui.components.LocalHaptics
import com.presa.app.ui.components.LocalHapticsEnabled
import com.presa.app.ui.components.PresaSecondaryButton
import com.presa.app.ui.theme.LocalPresaColors
import kotlin.math.max

/**
 * Touch surface that drives the host cursor through the composite HID mouse.
 *
 * One finger moves the cursor, a short tap left-clicks, two fingers scroll and
 * a two-finger tap right-clicks. The explicit buttons below the surface make
 * the same clicks available to TalkBack and to users who prefer them.
 */
@Composable
fun TrackpadSection(
    enabled: Boolean,
    onMove: (Float, Float) -> Unit,
    onScroll: (Float, Float) -> Unit,
    onLeftClick: () -> Unit,
    onRightClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ModeSection(title = stringResource(R.string.trackpad_section), modifier = modifier) {
        TrackpadSurface(
            enabled = enabled,
            onMove = onMove,
            onScroll = onScroll,
            onLeftClick = onLeftClick,
            onRightClick = onRightClick,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            PresaSecondaryButton(
                text = stringResource(R.string.trackpad_left_click),
                onClick = onLeftClick,
                enabled = enabled,
                modifier = Modifier.weight(1f),
            )
            PresaSecondaryButton(
                text = stringResource(R.string.trackpad_right_click),
                onClick = onRightClick,
                enabled = enabled,
                modifier = Modifier.weight(1f),
            )
        }
        if (!enabled) {
            Text(
                text = stringResource(R.string.trackpad_disabled),
                style = MaterialTheme.typography.bodySmall,
                color = LocalPresaColors.current.muted,
            )
        }
    }
}

@Composable
private fun TrackpadSurface(
    enabled: Boolean,
    onMove: (Float, Float) -> Unit,
    onScroll: (Float, Float) -> Unit,
    onLeftClick: () -> Unit,
    onRightClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LocalPresaColors.current
    val haptics = LocalHaptics.current
    val hapticsEnabled = LocalHapticsEnabled.current
    val description = stringResource(R.string.trackpad_content_desc)
    val disabledDescription = stringResource(R.string.trackpad_disabled)
    val leftAction = stringResource(R.string.trackpad_left_click_desc)
    val rightAction = stringResource(R.string.trackpad_right_click_desc)

    var touching by remember { mutableStateOf(false) }
    val shape = RoundedCornerShape(22.dp)
    val borderColor by animateColorAsState(
        targetValue = if (enabled && touching) colors.gold else colors.line,
        label = "trackpadBorder",
    )
    val glow by animateFloatAsState(
        targetValue = if (enabled && touching) 1f else 0f,
        label = "trackpadGlow",
    )
    val contentAlpha by animateFloatAsState(
        targetValue = if (touching) 0.35f else 1f,
        label = "trackpadContent",
    )

    val gestureModifier = if (enabled) {
        Modifier.pointerInput(enabled) {
            detectTrackpadGestures(
                onMove = onMove,
                onScroll = onScroll,
                onTap = {
                    haptics?.press(hapticsEnabled)
                    onLeftClick()
                },
                onSecondaryTap = {
                    haptics?.press(hapticsEnabled)
                    onRightClick()
                },
                onTouchChange = { touching = it },
            )
        }
    } else {
        Modifier
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(168.dp)
            .clip(shape)
            .background(
                Brush.verticalGradient(
                    listOf(colors.panel2, colors.panel),
                ),
            )
            .drawBehind {
                val spacing = 20.dp.toPx()
                val dotRadius = 1.dp.toPx()
                var y = spacing / 2f
                while (y < size.height) {
                    var x = spacing / 2f
                    while (x < size.width) {
                        drawCircle(
                            color = colors.line.copy(alpha = 0.65f),
                            radius = dotRadius,
                            center = Offset(x, y),
                        )
                        x += spacing
                    }
                    y += spacing
                }
                if (glow > 0f) {
                    val radius = size.maxDimension * 0.55f
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                colors.gold.copy(alpha = 0.18f * glow),
                                Color.Transparent,
                            ),
                            center = center,
                            radius = radius,
                        ),
                        radius = radius,
                        center = center,
                    )
                }
            }
            .border(1.dp, borderColor, shape)
            .then(gestureModifier)
            .semantics {
                contentDescription = if (enabled) description else disabledDescription
                if (enabled) {
                    customActions = listOf(
                        CustomAccessibilityAction(leftAction) {
                            onLeftClick()
                            true
                        },
                        CustomAccessibilityAction(rightAction) {
                            onRightClick()
                            true
                        },
                    )
                } else {
                    disabled()
                }
            },
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier.graphicsLayer { alpha = contentAlpha },
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Icon(
                imageVector = Icons.Rounded.TouchApp,
                contentDescription = null,
                tint = colors.muted,
                modifier = Modifier.size(28.dp),
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.trackpad_hint),
                style = MaterialTheme.typography.bodyMedium,
                color = colors.ink2,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(3.dp))
            Text(
                text = stringResource(R.string.trackpad_gestures),
                style = MaterialTheme.typography.bodySmall,
                color = colors.muted,
                textAlign = TextAlign.Center,
            )
        }
    }
}

private suspend fun PointerInputScope.detectTrackpadGestures(
    onMove: (Float, Float) -> Unit,
    onScroll: (Float, Float) -> Unit,
    onTap: () -> Unit,
    onSecondaryTap: () -> Unit,
    onTouchChange: (Boolean) -> Unit,
) {
    awaitEachGesture {
        val down = awaitFirstDown(requireUnconsumed = false)
        down.consume()
        onTouchChange(true)

        var lastPosition = down.position
        var pointerCount = 1
        var maximumPointerCount = 1
        var moved = false
        var travel = 0f
        var lastEventTime = down.uptimeMillis
        val touchSlop = viewConfiguration.touchSlop

        try {
            while (true) {
                val event = awaitPointerEvent()
                lastEventTime = event.changes.maxOfOrNull { it.uptimeMillis } ?: lastEventTime
                val pressed = event.changes.filter { it.pressed }
                if (pressed.isEmpty()) break

                val count = pressed.size
                val centroid = pressed.fold(Offset.Zero) { total, change ->
                    total + change.position
                } / count.toFloat()

                if (count != pointerCount) {
                    pointerCount = count
                    maximumPointerCount = max(maximumPointerCount, count)
                    lastPosition = centroid
                    event.changes.forEach { it.consume() }
                    continue
                }

                val delta = centroid - lastPosition
                lastPosition = centroid
                travel += delta.getDistance()
                if (travel > touchSlop) moved = true

                if (moved) {
                    // Once a second finger joins, keep the whole gesture as a
                    // scroll even if one finger lifts before the others.
                    if (maximumPointerCount >= 2) {
                        onScroll(delta.x, delta.y)
                    } else {
                        onMove(delta.x, delta.y)
                    }
                }

                event.changes.forEach { it.consume() }
            }
        } finally {
            onTouchChange(false)
        }

        val duration = lastEventTime - down.uptimeMillis
        if (!moved && duration <= TAP_TIMEOUT_MS) {
            if (maximumPointerCount >= 2) onSecondaryTap() else onTap()
        }
    }
}

private const val TAP_TIMEOUT_MS = 350L
