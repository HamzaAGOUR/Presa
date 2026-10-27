package com.presa.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Backspace
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.disabled
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.presa.app.R
import com.presa.app.ui.theme.LocalPresaColors
import com.presa.app.ui.theme.PresaType
import androidx.compose.ui.res.stringResource

@Composable
fun NumberPadCard(
    title: String,
    enabled: Boolean,
    onSend: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LocalPresaColors.current
    var value by rememberSaveable { mutableStateOf("") }
    var tooLong by rememberSaveable { mutableStateOf(false) }

    fun append(digit: Char) {
        if (!enabled) return
        if (value.length >= MAX_DIGITS) {
            tooLong = true
        } else {
            tooLong = false
            value += digit
        }
    }

    PresaCard(
        modifier = modifier,
        contentPadding = androidx.compose.foundation.layout.PaddingValues(14.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = title,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.titleSmall,
                color = colors.ink,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.width(8.dp))
            KeyCap(
                text = stringResource(R.string.numberpad_enter),
                contentColor = colors.ink2,
            )
        }

        Spacer(Modifier.height(10.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 64.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(colors.panel2)
                .border(1.dp, colors.line, RoundedCornerShape(12.dp))
                .padding(horizontal = 16.dp, vertical = 8.dp),
            contentAlignment = Alignment.CenterStart,
        ) {
            AutoSizeText(
                text = if (value.isEmpty()) {
                    stringResource(R.string.numberpad_display_empty)
                } else {
                    value
                },
                style = PresaType.numberPadDisplay,
                color = if (value.isEmpty()) colors.muted else colors.ink,
                maxLines = 1,
                minScale = 0.55f,
            )
        }

        Spacer(Modifier.height(6.dp))
        Text(
            text = if (tooLong) {
                stringResource(R.string.numberpad_too_long)
            } else {
                stringResource(R.string.numberpad_hint)
            },
            style = MaterialTheme.typography.bodySmall,
            color = if (tooLong) colors.alert else colors.muted,
        )

        Spacer(Modifier.height(10.dp))

        listOf(listOf('1', '2', '3'), listOf('4', '5', '6'), listOf('7', '8', '9'))
            .forEach { row ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    row.forEach { digit ->
                        NumberKey(
                            text = digit.toString(),
                            contentDescription = stringResource(
                                R.string.numberpad_digit_content_desc,
                                digit.toString(),
                            ),
                            enabled = enabled,
                            onClick = { append(digit) },
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
                Spacer(Modifier.height(8.dp))
            }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            NumberKey(
                text = stringResource(R.string.numberpad_clear),
                contentDescription = stringResource(R.string.numberpad_clear),
                enabled = enabled && value.isNotEmpty(),
                onClick = {
                    value = ""
                    tooLong = false
                },
                modifier = Modifier.weight(1f),
                textStyleSmall = true,
            )
            NumberKey(
                text = "0",
                contentDescription = stringResource(R.string.numberpad_digit_content_desc, "0"),
                enabled = enabled,
                onClick = { append('0') },
                modifier = Modifier.weight(1f),
            )
            NumberKey(
                text = null,
                contentDescription = stringResource(R.string.numberpad_backspace),
                enabled = enabled && value.isNotEmpty(),
                onClick = {
                    value = value.dropLast(1)
                    tooLong = false
                },
                modifier = Modifier.weight(1f),
                icon = true,
            )
        }

        Spacer(Modifier.height(10.dp))

        val canSend = enabled && value.isNotEmpty()
        val haptics = LocalHaptics.current
        val hapticsEnabled = LocalHapticsEnabled.current
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 52.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(if (canSend) colors.navy else colors.panel3)
                .clickable(enabled = canSend) {
                    haptics?.press(hapticsEnabled)
                    onSend(value)
                    value = ""
                    tooLong = false
                }
                .semantics {
                    role = Role.Button
                    if (canSend) {
                        onClick(label = title) {
                            haptics?.press(hapticsEnabled)
                            onSend(value)
                            value = ""
                            tooLong = false
                            true
                        }
                    } else {
                        disabled()
                    }
                }
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            AutoSizeText(
                text = stringResource(R.string.numberpad_enter),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = if (canSend) colors.onNavy else colors.muted,
                modifier = Modifier.weight(1f, fill = false),
                maxLines = 1,
                minScale = 0.65f,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.width(8.dp))
            KeyCap(
                text = stringResource(R.string.key_enter),
                contentColor = if (canSend) colors.onNavy else colors.muted,
                containerColor = if (canSend) {
                    colors.onNavy.copy(alpha = 0.16f)
                } else {
                    colors.panel2
                },
            )
        }
    }
}

@Composable
private fun NumberKey(
    text: String?,
    contentDescription: String,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: Boolean = false,
    textStyleSmall: Boolean = false,
) {
    val colors = LocalPresaColors.current
    val haptics = LocalHaptics.current
    val hapticsEnabled = LocalHapticsEnabled.current
    val contentColor = if (enabled) colors.ink else colors.muted

    Box(
        modifier = modifier
            .heightIn(min = 50.dp)
            .clip(RoundedCornerShape(11.dp))
            .background(colors.panel2)
            .then(
                if (!enabled) {
                    Modifier.drawBehind {
                        val spacing = 8.dp.toPx()
                        var x = -size.height
                        while (x < size.width) {
                            drawLine(
                                color = colors.hatch.copy(alpha = 0.38f),
                                start = Offset(x, size.height),
                                end = Offset(x + size.height, 0f),
                                strokeWidth = 1.dp.toPx(),
                            )
                            x += spacing
                        }
                    }
                } else {
                    Modifier
                },
            )
            .border(
                1.dp,
                if (enabled) colors.line else Color.Transparent,
                RoundedCornerShape(11.dp),
            )
            .clickable(enabled = enabled, role = Role.Button) {
                haptics?.press(hapticsEnabled)
                onClick()
            }
            .semantics {
                this.contentDescription = contentDescription
                if (!enabled) disabled()
            },
        contentAlignment = Alignment.Center,
    ) {
        if (icon) {
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.Backspace,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(21.dp),
            )
        } else {
            AutoSizeText(
                text = text.orEmpty(),
                style = if (textStyleSmall) {
                    MaterialTheme.typography.labelLarge
                } else {
                    PresaType.numberKey
                },
                color = contentColor,
                maxLines = 1,
                minScale = 0.55f,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 3.dp, vertical = 4.dp),
            )
        }
    }
}

private const val MAX_DIGITS = 4
