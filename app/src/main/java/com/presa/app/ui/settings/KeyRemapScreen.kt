package com.presa.app.ui.settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.presa.app.R
import com.presa.app.data.PresaSettings
import com.presa.app.domain.model.KeyAction
import com.presa.app.domain.model.KeyChord
import com.presa.app.domain.model.KeyMaps
import com.presa.app.domain.model.KeyModifiers
import com.presa.app.domain.model.PdfKeyMap
import com.presa.app.domain.model.PowerPointKeyMap
import com.presa.app.domain.model.VisualizerKeyMap
import com.presa.app.ui.components.HatchOverlay
import com.presa.app.ui.components.KeyCap
import com.presa.app.ui.components.LocalHaptics
import com.presa.app.ui.components.LocalHapticsEnabled
import com.presa.app.ui.components.PresaCard
import com.presa.app.ui.components.PresaScreenHeader
import com.presa.app.ui.components.PresaSecondaryButton
import com.presa.app.ui.components.RemapKeyUsages
import com.presa.app.ui.components.SegmentOption
import com.presa.app.ui.components.SegmentedControl
import com.presa.app.ui.components.chordLabel
import com.presa.app.ui.components.keyName
import com.presa.app.ui.theme.LocalPresaColors
import com.presa.app.ui.theme.PresaType

private enum class RemapGroup {
    VISUALIZER,
    POWERPOINT,
    PDF,
}

@Composable
fun KeyRemapScreen(
    settings: PresaSettings,
    onBack: () -> Unit,
    onSetOverride: (String, KeyChord) -> Unit,
    onResetOverride: (String) -> Unit,
    onResetAll: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LocalPresaColors.current
    var groupName by rememberSaveable { mutableStateOf(RemapGroup.VISUALIZER.name) }
    var editingActionId by rememberSaveable { mutableStateOf<String?>(null) }
    var showResetAll by rememberSaveable { mutableStateOf(false) }

    val group = RemapGroup.valueOf(groupName)
    val actions = when (group) {
        RemapGroup.VISUALIZER -> VisualizerKeyMap.actions
        RemapGroup.POWERPOINT -> PowerPointKeyMap.actions
        RemapGroup.PDF -> PdfKeyMap.actions
    }
    val groupOptions = listOf(
        SegmentOption(RemapGroup.VISUALIZER.name, stringResource(R.string.remap_mode_visualizer)),
        SegmentOption(RemapGroup.POWERPOINT.name, stringResource(R.string.remap_mode_powerpoint)),
        SegmentOption(RemapGroup.PDF.name, stringResource(R.string.remap_mode_pdf)),
    )

    Column(modifier = modifier.fillMaxSize()) {
        PresaScreenHeader(
            title = stringResource(R.string.remap_title),
            onBack = onBack,
        )
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Text(
                text = stringResource(R.string.remap_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = colors.ink2,
            )
            SegmentedControl(
                options = groupOptions,
                selectedId = groupName,
                onSelect = { groupName = it },
            )

            PresaCard(contentPadding = PaddingValues(0.dp)) {
                actions.forEachIndexed { index, action ->
                    val override = settings.keyOverrides[action.id]
                    val chord = KeyMaps.effectiveChord(action, settings.keyOverrides)
                    ActionRow(
                        action = action,
                        chord = chord,
                        custom = override != null,
                        onClick = { editingActionId = action.id },
                    )
                    if (index != actions.lastIndex) {
                        HorizontalDivider(color = colors.line)
                    }
                }
            }

            PresaSecondaryButton(
                text = stringResource(R.string.remap_reset_all),
                onClick = { showResetAll = true },
                modifier = Modifier.fillMaxWidth(),
                enabled = settings.keyOverrides.isNotEmpty(),
            )
            Spacer(Modifier.height(8.dp))
        }
    }

    val editingAction = editingActionId?.let(KeyMaps::find)
    if (editingAction != null) {
        val currentChord = KeyMaps.effectiveChord(editingAction, settings.keyOverrides)
        KeyPickerDialog(
            action = editingAction,
            currentChord = currentChord,
            isCustom = settings.keyOverrides.containsKey(editingAction.id),
            onDismiss = { editingActionId = null },
            onConfirm = { chord ->
                onSetOverride(editingAction.id, chord)
                editingActionId = null
            },
            onReset = {
                onResetOverride(editingAction.id)
                editingActionId = null
            },
        )
    }

    if (showResetAll) {
        AlertDialog(
            onDismissRequest = { showResetAll = false },
            title = {
                Text(
                    text = stringResource(R.string.remap_reset_title),
                    color = colors.ink,
                )
            },
            text = {
                Text(
                    text = stringResource(R.string.remap_reset_body),
                    color = colors.ink2,
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showResetAll = false
                        onResetAll()
                    },
                ) {
                    Text(
                        text = stringResource(R.string.remap_reset_all),
                        color = colors.alert,
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetAll = false }) {
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

@Composable
private fun ActionRow(
    action: KeyAction,
    chord: KeyChord,
    custom: Boolean,
    onClick: () -> Unit,
) {
    val colors = LocalPresaColors.current
    val label = stringResource(action.labelRes)
    val haptics = LocalHaptics.current
    val hapticsEnabled = LocalHapticsEnabled.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(role = Role.Button) {
                haptics?.press(hapticsEnabled)
                onClick()
            }
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.titleSmall,
                color = colors.ink,
            )
            Spacer(Modifier.height(5.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                KeyCap(text = chordLabel(chord))
                Spacer(Modifier.width(7.dp))
                OverrideBadge(custom = custom)
            }
        }
        Spacer(Modifier.width(10.dp))
        Icon(
            imageVector = Icons.Rounded.Edit,
            contentDescription = stringResource(R.string.remap_edit_content_desc, label),
            tint = colors.muted,
            modifier = Modifier.size(19.dp),
        )
    }
}

@Composable
private fun OverrideBadge(custom: Boolean) {
    val colors = LocalPresaColors.current
    Surface(
        shape = RoundedCornerShape(999.dp),
        color = if (custom) colors.goldSoft else colors.panel3,
    ) {
        Text(
            text = stringResource(
                if (custom) R.string.remap_custom_badge else R.string.remap_default_badge,
            ),
            style = MaterialTheme.typography.labelSmall,
            color = if (custom) colors.gold else colors.muted,
            modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp),
            maxLines = 1,
        )
    }
}

@Composable
private fun KeyPickerDialog(
    action: KeyAction,
    currentChord: KeyChord,
    isCustom: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (KeyChord) -> Unit,
    onReset: () -> Unit,
) {
    val colors = LocalPresaColors.current
    val haptics = LocalHaptics.current
    val hapticsEnabled = LocalHapticsEnabled.current
    var modifiers by remember(action.id) {
        mutableIntStateOf(currentChord.modifier and MODIFIER_MASK)
    }
    var usage by remember(action.id) { mutableIntStateOf(currentChord.usage) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(
                    R.string.remap_picker_title,
                    stringResource(action.labelRes),
                ),
                color = colors.ink,
            )
        },
        text = {
            Column {
                Text(
                    text = stringResource(R.string.remap_picker_modifiers),
                    style = MaterialTheme.typography.labelMedium,
                    color = colors.ink2,
                )
                Spacer(Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                    ModifierChip(
                        text = stringResource(R.string.key_mod_ctrl),
                        selected = modifiers and KeyModifiers.CTRL != 0,
                        onToggle = {
                            modifiers = modifiers xor KeyModifiers.CTRL
                            haptics?.press(hapticsEnabled)
                        },
                    )
                    ModifierChip(
                        text = stringResource(R.string.key_mod_shift),
                        selected = modifiers and KeyModifiers.SHIFT != 0,
                        onToggle = {
                            modifiers = modifiers xor KeyModifiers.SHIFT
                            haptics?.press(hapticsEnabled)
                        },
                    )
                    ModifierChip(
                        text = stringResource(R.string.key_mod_alt),
                        selected = modifiers and KeyModifiers.ALT != 0,
                        onToggle = {
                            modifiers = modifiers xor KeyModifiers.ALT
                            haptics?.press(hapticsEnabled)
                        },
                    )
                }
                Spacer(Modifier.height(10.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    KeyCap(text = chordLabel(KeyChord(modifiers, usage)))
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.remap_picker_key),
                        style = MaterialTheme.typography.labelMedium,
                        color = colors.muted,
                    )
                }
                Spacer(Modifier.height(9.dp))
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 54.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(226.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    items(RemapKeyUsages, key = { it }) { keyUsage ->
                        val selected = keyUsage == usage
                        Surface(
                            modifier = Modifier
                                .height(38.dp)
                                .clip(RoundedCornerShape(9.dp))
                                .clickable(role = Role.Button) {
                                    usage = keyUsage
                                    haptics?.press(hapticsEnabled)
                                },
                            shape = RoundedCornerShape(9.dp),
                            color = if (selected) colors.navy else colors.panel2,
                            border = BorderStroke(
                                1.dp,
                                if (selected) colors.navy else colors.line,
                            ),
                        ) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    text = keyName(keyUsage),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (selected) colors.onNavy else colors.ink2,
                                    textAlign = TextAlign.Center,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.padding(horizontal = 3.dp),
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(KeyChord(modifiers, usage)) },
            ) {
                Text(
                    text = stringResource(R.string.remap_picker_confirm),
                    color = colors.navy,
                )
            }
        },
        dismissButton = {
            Row {
                if (isCustom) {
                    TextButton(onClick = onReset) {
                        Text(
                            text = stringResource(R.string.remap_picker_reset_default),
                            color = colors.alert,
                        )
                    }
                }
                TextButton(onClick = onDismiss) {
                    Text(
                        text = stringResource(R.string.dialog_cancel),
                        color = colors.ink2,
                    )
                }
            }
        },
        containerColor = colors.panel,
        titleContentColor = colors.ink,
        textContentColor = colors.ink2,
    )
}

@Composable
private fun ModifierChip(
    text: String,
    selected: Boolean,
    onToggle: () -> Unit,
) {
    val colors = LocalPresaColors.current
    Surface(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .clickable(role = Role.Checkbox, onClick = onToggle),
        shape = RoundedCornerShape(999.dp),
        color = if (selected) colors.navy else colors.panel2,
        border = BorderStroke(1.dp, if (selected) colors.navy else colors.line),
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            color = if (selected) colors.onNavy else colors.ink2,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            maxLines = 1,
        )
    }
}

private const val MODIFIER_MASK = KeyModifiers.CTRL or KeyModifiers.SHIFT or KeyModifiers.ALT
