package com.presa.app.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.presa.app.R
import com.presa.app.ui.theme.LocalPresaColors
import com.presa.app.ui.theme.PresaType

@Composable
fun PresaBackground(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit,
) {
    val colors = LocalPresaColors.current
    Box(
        modifier = modifier
            .fillMaxSize()
            .drawBehind {
                drawRect(
                    brush = Brush.verticalGradient(
                        listOf(colors.background, colors.panel2),
                    ),
                )
                val goldRadius = size.width * 0.9f
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(colors.backgroundGlow, Color.Transparent),
                        center = Offset(size.width * 0.08f, 0f),
                        radius = goldRadius,
                    ),
                    radius = goldRadius,
                    center = Offset(size.width * 0.08f, 0f),
                )
                val blueRadius = size.width * 0.7f
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0x1A2F6FE4), Color.Transparent),
                        center = Offset(size.width * 0.92f, 0f),
                        radius = blueRadius,
                    ),
                    radius = blueRadius,
                    center = Offset(size.width * 0.92f, 0f),
                )
            },
        content = content,
    )
}

@Composable
fun PresaMark(size: Dp = 40.dp) {
    val colors = LocalPresaColors.current
    Box(
        modifier = Modifier
            .size(size)
            .clip(RoundedCornerShape(size * 0.3f))
            .background(
                Brush.linearGradient(
                    listOf(colors.brandNavy, colors.brandNavyHi),
                ),
            ),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "P",
            color = colors.brandGold,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Black,
        )
    }
}

@Composable
fun PresaHeader(
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LocalPresaColors.current
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        PresaMark()
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(R.string.app_name),
                style = MaterialTheme.typography.headlineMedium,
                color = colors.ink,
                maxLines = 1,
            )
            Text(
                text = stringResource(R.string.app_tagline),
                style = MaterialTheme.typography.bodySmall,
                color = colors.muted,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        PresaIconButton(
            icon = Icons.Rounded.Settings,
            contentDescription = stringResource(R.string.content_desc_settings),
            onClick = onSettingsClick,
        )
    }
}

@Composable
fun PresaScreenHeader(
    title: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    trailing: (@Composable () -> Unit)? = null,
) {
    val colors = LocalPresaColors.current
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        PresaIconButton(
            icon = Icons.AutoMirrored.Rounded.ArrowBack,
            contentDescription = stringResource(R.string.content_desc_back),
            onClick = onBack,
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            color = colors.ink,
            modifier = Modifier.weight(1f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        trailing?.invoke()
    }
}

@Composable
fun PresaIconButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    tint: Color? = null,
) {
    val colors = LocalPresaColors.current
    val haptics = LocalHaptics.current
    val hapticsEnabled = LocalHapticsEnabled.current
    Box(
        modifier = modifier
            .size(42.dp)
            .clip(CircleShape)
            .background(colors.panel)
            .border(1.dp, colors.line, CircleShape)
            .clickable(
                enabled = enabled,
                role = Role.Button,
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
            ) {
                haptics?.press(hapticsEnabled)
                onClick()
            },
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = tint ?: if (enabled) colors.ink2 else colors.muted,
            modifier = Modifier.size(20.dp),
        )
    }
}

@Composable
fun SectionLabel(
    text: String,
    modifier: Modifier = Modifier,
) {
    val colors = LocalPresaColors.current
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = colors.gold,
            maxLines = 1,
        )
        Spacer(Modifier.width(10.dp))
        Box(
            modifier = Modifier
                .weight(1f)
                .height(1.dp)
                .background(colors.line),
        )
    }
}

@Composable
fun PresaCard(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(16.dp),
    content: @Composable ColumnScope.() -> Unit,
) {
    val colors = LocalPresaColors.current
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = colors.panel,
        border = BorderStroke(1.dp, colors.line),
        shadowElevation = 1.dp,
    ) {
        Column(
            modifier = Modifier.padding(contentPadding),
            content = content,
        )
    }
}

@Composable
fun KeyCap(
    text: String,
    modifier: Modifier = Modifier,
    contentColor: Color? = null,
    containerColor: Color? = null,
) {
    val colors = LocalPresaColors.current
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(6.dp),
        color = containerColor ?: colors.panel3,
    ) {
        AutoSizeText(
            text = text,
            style = PresaType.keycap,
            color = contentColor ?: colors.ink2,
            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
            maxLines = 1,
            minScale = 0.7f,
        )
    }
}

data class SegmentOption(
    val id: String,
    val label: String,
)

@Composable
fun SegmentedControl(
    options: List<SegmentOption>,
    selectedId: String,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier,
    activeColor: Color? = null,
) {
    val colors = LocalPresaColors.current
    val active = activeColor ?: colors.navy
    val haptics = LocalHaptics.current
    val hapticsEnabled = LocalHapticsEnabled.current

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(999.dp))
            .background(colors.panel2)
            .border(1.dp, colors.line, RoundedCornerShape(999.dp))
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        options.forEach { option ->
            val isSelected = option.id == selectedId
            val background by animateColorAsState(
                targetValue = if (isSelected) active else Color.Transparent,
                label = "segmentBackground",
            )
            val content by animateColorAsState(
                targetValue = if (isSelected) {
                    when (active) {
                        colors.navy -> colors.onNavy
                        colors.forwardBlue -> colors.onForward
                        colors.gold -> colors.onGold
                        else -> colors.ink
                    }
                } else {
                    colors.ink2
                },
                label = "segmentContent",
            )
            Box(
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = 42.dp)
                    .clip(RoundedCornerShape(999.dp))
                    .background(background)
                    .semantics { this.selected = isSelected }
                    .clickable(
                        role = Role.Tab,
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                    ) {
                        if (!isSelected) {
                            haptics?.press(hapticsEnabled)
                            onSelect(option.id)
                        }
                    },
                contentAlignment = Alignment.Center,
            ) {
                AutoSizeText(
                    text = option.label,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = content,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
                    maxLines = 1,
                    minScale = 0.6f,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

@Composable
fun PresaPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    containerColor: Color? = null,
    contentColor: Color? = null,
) {
    val colors = LocalPresaColors.current
    val haptics = LocalHaptics.current
    val hapticsEnabled = LocalHapticsEnabled.current
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(enabled = enabled) {
                haptics?.press(hapticsEnabled)
                onClick()
            },
        shape = RoundedCornerShape(12.dp),
        color = if (enabled) (containerColor ?: colors.navy) else colors.panel3,
    ) {
        AutoSizeText(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            color = if (enabled) (contentColor ?: colors.onNavy) else colors.muted,
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 13.dp),
            maxLines = 2,
            minScale = 0.75f,
        )
    }
}

@Composable
fun PresaSecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    leadingIcon: ImageVector? = null,
) {
    val colors = LocalPresaColors.current
    val haptics = LocalHaptics.current
    val hapticsEnabled = LocalHapticsEnabled.current
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(enabled = enabled) {
                haptics?.press(hapticsEnabled)
                onClick()
            },
        shape = RoundedCornerShape(12.dp),
        color = colors.panel,
        border = BorderStroke(1.dp, colors.line),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (leadingIcon != null) {
                Icon(
                    imageVector = leadingIcon,
                    contentDescription = null,
                    tint = if (enabled) colors.ink2 else colors.muted,
                    modifier = Modifier.size(17.dp),
                )
                Spacer(Modifier.width(8.dp))
            }
            AutoSizeText(
                text = text,
                style = MaterialTheme.typography.labelMedium,
                color = if (enabled) colors.ink2 else colors.muted,
                modifier = Modifier.weight(1f, fill = false),
                maxLines = 2,
                minScale = 0.75f,
            )
        }
    }
}

@Composable
fun HatchOverlay(modifier: Modifier = Modifier) {
    val colors = LocalPresaColors.current
    Box(
        modifier = modifier.drawBehind {
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
        },
    )
}
