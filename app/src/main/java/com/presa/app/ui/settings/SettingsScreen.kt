package com.presa.app.ui.settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.presa.app.BuildConfig
import com.presa.app.R
import com.presa.app.data.PresaSettings
import com.presa.app.domain.model.AppLanguage
import com.presa.app.domain.model.RepeatSpeed
import com.presa.app.domain.model.ThemeMode
import com.presa.app.ui.components.LocalHaptics
import com.presa.app.ui.components.LocalHapticsEnabled
import com.presa.app.ui.components.PresaCard
import com.presa.app.ui.components.PresaIconButton
import com.presa.app.ui.components.PresaScreenHeader
import com.presa.app.ui.components.SectionLabel
import com.presa.app.ui.components.SegmentOption
import com.presa.app.ui.components.SegmentedControl
import com.presa.app.ui.theme.LocalPresaColors
import com.presa.app.ui.theme.PresaType

@Composable
fun SettingsScreen(
    settings: PresaSettings,
    onBack: () -> Unit,
    onThemeChange: (ThemeMode) -> Unit,
    onLanguageChange: (AppLanguage) -> Unit,
    onHapticsChange: (Boolean) -> Unit,
    onVolumeKeysChange: (Boolean) -> Unit,
    onRepeatSpeedChange: (RepeatSpeed) -> Unit,
    onTimerMinutesChange: (Int) -> Unit,
    onOpenRemap: () -> Unit,
    onMessage: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LocalPresaColors.current
    val context = LocalContext.current

    val themeOptions = listOf(
        SegmentOption(ThemeMode.SYSTEM.name, stringResource(R.string.theme_system)),
        SegmentOption(ThemeMode.LIGHT.name, stringResource(R.string.theme_light)),
        SegmentOption(ThemeMode.DARK.name, stringResource(R.string.theme_dark)),
    )
    val languageOptions = listOf(
        SegmentOption(AppLanguage.FRENCH.name, stringResource(R.string.language_french)),
        SegmentOption(AppLanguage.ENGLISH.name, stringResource(R.string.language_english)),
    )
    val repeatOptions = listOf(
        SegmentOption(RepeatSpeed.SLOW.name, stringResource(R.string.repeat_slow)),
        SegmentOption(RepeatSpeed.NORMAL.name, stringResource(R.string.repeat_normal)),
        SegmentOption(RepeatSpeed.FAST.name, stringResource(R.string.repeat_fast)),
    )

    Column(modifier = modifier.fillMaxSize()) {
        PresaScreenHeader(
            title = stringResource(R.string.settings_title),
            onBack = onBack,
        )
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            PresaCard {
                SectionLabel(text = stringResource(R.string.settings_section_appearance))
                Spacer(Modifier.height(12.dp))
                Text(
                    text = stringResource(R.string.settings_theme),
                    style = MaterialTheme.typography.titleSmall,
                    color = colors.ink,
                )
                Spacer(Modifier.height(7.dp))
                SegmentedControl(
                    options = themeOptions,
                    selectedId = settings.themeMode.name,
                    onSelect = { id -> onThemeChange(ThemeMode.valueOf(id)) },
                )
                Spacer(Modifier.height(14.dp))
                Text(
                    text = stringResource(R.string.settings_language),
                    style = MaterialTheme.typography.titleSmall,
                    color = colors.ink,
                )
                Spacer(Modifier.height(7.dp))
                SegmentedControl(
                    options = languageOptions,
                    selectedId = settings.language.name,
                    onSelect = { id -> onLanguageChange(AppLanguage.valueOf(id)) },
                )
            }

            PresaCard {
                SectionLabel(text = stringResource(R.string.settings_section_controls))
                Spacer(Modifier.height(6.dp))
                SwitchRow(
                    title = stringResource(R.string.settings_haptics),
                    description = stringResource(R.string.settings_haptics_desc),
                    checked = settings.hapticsEnabled,
                    onCheckedChange = onHapticsChange,
                )
                SwitchRow(
                    title = stringResource(R.string.settings_volume),
                    description = stringResource(R.string.settings_volume_desc),
                    checked = settings.volumeKeysEnabled,
                    onCheckedChange = { enabled ->
                        onVolumeKeysChange(enabled)
                        if (enabled) {
                            onMessage(context.getString(R.string.message_volume_keys_on))
                        }
                    },
                )
                Spacer(Modifier.height(10.dp))
                Text(
                    text = stringResource(R.string.settings_repeat),
                    style = MaterialTheme.typography.titleSmall,
                    color = colors.ink,
                )
                Spacer(Modifier.height(7.dp))
                SegmentedControl(
                    options = repeatOptions,
                    selectedId = settings.repeatSpeed.name,
                    onSelect = { id -> onRepeatSpeedChange(RepeatSpeed.valueOf(id)) },
                )
            }

            PresaCard {
                SectionLabel(text = stringResource(R.string.settings_section_timer))
                Spacer(Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(R.string.settings_timer_alert),
                            style = MaterialTheme.typography.titleSmall,
                            color = colors.ink,
                        )
                        Text(
                            text = stringResource(R.string.settings_timer_alert_desc),
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.muted,
                        )
                    }
                    PresaIconButton(
                        icon = Icons.Rounded.Remove,
                        contentDescription = stringResource(R.string.content_desc_decrease),
                        onClick = {
                            onTimerMinutesChange((settings.timerAlertMinutes - 5).coerceAtLeast(5))
                        },
                        enabled = settings.timerAlertMinutes > 5,
                    )
                    Text(
                        text = stringResource(
                            R.string.settings_timer_minutes,
                            settings.timerAlertMinutes,
                        ),
                        style = PresaType.timer,
                        color = colors.ink,
                        modifier = Modifier.padding(horizontal = 8.dp),
                    )
                    PresaIconButton(
                        icon = Icons.Rounded.Add,
                        contentDescription = stringResource(R.string.content_desc_increase),
                        onClick = {
                            onTimerMinutesChange((settings.timerAlertMinutes + 5).coerceAtMost(120))
                        },
                        enabled = settings.timerAlertMinutes < 120,
                    )
                }
            }

            val remapCount = settings.keyOverrides.size
            val haptics = LocalHaptics.current
            val hapticsEnabled = LocalHapticsEnabled.current
            PresaCard(
                modifier = Modifier.clickable(role = Role.Button) {
                    haptics?.press(hapticsEnabled)
                    onOpenRemap()
                },
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Rounded.Edit,
                        contentDescription = null,
                        tint = colors.gold,
                        modifier = Modifier.size(22.dp),
                    )
                    Spacer(Modifier.width(11.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(R.string.settings_remap),
                            style = MaterialTheme.typography.titleSmall,
                            color = colors.ink,
                        )
                        Text(
                            text = if (remapCount == 0) {
                                stringResource(R.string.settings_remap_desc)
                            } else {
                                pluralStringResource(
                                    R.plurals.settings_remap_count,
                                    remapCount,
                                    remapCount,
                                )
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.muted,
                        )
                    }
                    Icon(
                        imageVector = Icons.Rounded.ChevronRight,
                        contentDescription = null,
                        tint = colors.muted,
                        modifier = Modifier.size(20.dp),
                    )
                }
            }

            PresaCard {
                SectionLabel(text = stringResource(R.string.settings_section_about))
                Spacer(Modifier.height(10.dp))
                Text(
                    text = stringResource(R.string.settings_about_text),
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.ink2,
                )
                Spacer(Modifier.height(10.dp))
                Text(
                    text = stringResource(R.string.settings_version, BuildConfig.VERSION_NAME),
                    style = MaterialTheme.typography.labelMedium,
                    color = colors.muted,
                )
                Text(
                    text = stringResource(R.string.settings_license),
                    style = MaterialTheme.typography.labelMedium,
                    color = colors.muted,
                )
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun SwitchRow(
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    val colors = LocalPresaColors.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(role = Role.Switch) { onCheckedChange(!checked) }
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = colors.ink,
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = colors.muted,
            )
        }
        Spacer(Modifier.width(12.dp))
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = colors.onNavy,
                checkedTrackColor = colors.navy,
                checkedBorderColor = colors.navy,
                uncheckedThumbColor = colors.panel,
                uncheckedTrackColor = colors.panel3,
                uncheckedBorderColor = colors.lineStrong,
            ),
        )
    }
}
