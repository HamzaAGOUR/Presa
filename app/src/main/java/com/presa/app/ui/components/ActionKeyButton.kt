package com.presa.app.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.disabled
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.presa.app.ui.theme.LocalPresaColors
import com.presa.app.ui.theme.PresaType

enum class KeyTone {
    Neutral,
    Navy,
    Next,
    Previous,
    Gold,
    Danger,
    Success,
}

private data class ToneColors(
    val container: Color,
    val content: Color,
    val border: Color,
)

@Composable
private fun toneColors(tone: KeyTone): ToneColors {
    val colors = LocalPresaColors.current
    return when (tone) {
        KeyTone.Neutral -> ToneColors(colors.panel, colors.ink, colors.line)
        KeyTone.Navy -> ToneColors(colors.navy, colors.onNavy, colors.navy)
        KeyTone.Next -> ToneColors(colors.forwardBlue, colors.onForward, colors.forwardBlue)
        KeyTone.Previous -> ToneColors(
            colors.previousOrange,
            colors.onPrevious,
            colors.previousOrange,
        )
        KeyTone.Gold -> ToneColors(colors.gold, colors.onGold, colors.gold)
        KeyTone.Danger -> ToneColors(colors.alertSoft, colors.alert, colors.alert.copy(alpha = 0.35f))
        KeyTone.Success -> ToneColors(
            colors.successSoft,
            colors.success,
            colors.success.copy(alpha = 0.35f),
        )
    }
}

/**
 * A large touch target that sends on press (not release), supports held repeat,
 * exposes a TalkBack click action, and uses hatching for disabled keys.
 */
@Composable
fun ActionKeyButton(
    label: String,
    onPress: () -> Unit,
    modifier: Modifier = Modifier,
    hint: String? = null,
    icon: ImageVector? = null,
    tone: KeyTone = KeyTone.Neutral,
    enabled: Boolean = true,
    minHeight: Dp = 64.dp,
    big: Boolean = false,
    onRelease: (() -> Unit)? = null,
    hintOnTop: Boolean = true,
) {
    val colors = LocalPresaColors.current
    val toneColors = toneColors(tone)
    val haptics = LocalHaptics.current
    val hapticsEnabled = LocalHapticsEnabled.current
    val shape = RoundedCornerShape(if (big) 22.dp else 15.dp)

    var pressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.965f else 1f,
        label = "keyPressScale",
    )

    val background = if (enabled) toneColors.container else colors.panel3
    val content = if (enabled) toneColors.content else colors.muted
    val border = if (enabled) toneColors.border else colors.line
    val hintContainer = if (enabled && tone != KeyTone.Neutral) {
        content.copy(alpha = 0.17f)
    } else {
        colors.panel3
    }
    val hintContent = if (enabled) content.copy(alpha = 0.92f) else colors.muted

    val gestureModifier = if (enabled) {
        Modifier.pointerInput(enabled) {
            detectTapGestures(
                onPress = {
                    pressed = true
                    haptics?.press(hapticsEnabled)
                    onPress()
                    tryAwaitRelease()
                    pressed = false
                    onRelease?.invoke()
                },
            )
        }
    } else {
        Modifier
    }

    val semanticsModifier = Modifier.semantics(mergeDescendants = true) {
        role = Role.Button
        if (enabled) {
            onClick(label = label) {
                haptics?.press(hapticsEnabled)
                onPress()
                onRelease?.invoke()
                true
            }
        } else {
            disabled()
        }
    }

    Box(
        modifier = modifier
            .heightIn(min = minHeight)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(shape)
            .background(background)
            .then(
                if (!enabled) {
                    Modifier.drawBehind {
                        val spacing = 9.dp.toPx()
                        val stroke = 1.dp.toPx()
                        var x = -size.height
                        while (x < size.width) {
                            drawLine(
                                color = colors.hatch.copy(alpha = 0.45f),
                                start = Offset(x, size.height),
                                end = Offset(x + size.height, 0f),
                                strokeWidth = stroke,
                            )
                            x += spacing
                        }
                    }
                } else {
                    Modifier
                },
            )
            .border(1.dp, border, shape)
            .then(gestureModifier)
            .then(semanticsModifier)
            .padding(
                horizontal = if (big) 18.dp else 14.dp,
                vertical = if (big) 16.dp else 12.dp,
            ),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            if (hintOnTop && (hint != null || icon != null)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    if (icon != null) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = content,
                            modifier = Modifier.size(if (big) 40.dp else 20.dp),
                        )
                    } else {
                        Spacer(Modifier.width(1.dp))
                    }
                    if (hint != null) {
                        KeyCap(
                            text = hint,
                            contentColor = hintContent,
                            containerColor = hintContainer,
                        )
                    }
                }
                Spacer(Modifier.height(if (big) 12.dp else 8.dp))
            }

            AutoSizeText(
                text = label,
                style = if (big) {
                    MaterialTheme.typography.headlineSmall
                } else {
                    MaterialTheme.typography.titleSmall
                },
                fontWeight = if (big) FontWeight.ExtraBold else FontWeight.SemiBold,
                color = content,
                maxLines = 2,
                minScale = 0.68f,
            )

            if (!hintOnTop && hint != null) {
                Spacer(Modifier.height(6.dp))
                KeyCap(
                    text = hint,
                    contentColor = hintContent,
                    containerColor = hintContainer,
                )
            }
        }
    }
}
