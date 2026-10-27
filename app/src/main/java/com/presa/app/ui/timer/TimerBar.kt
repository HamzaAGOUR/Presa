package com.presa.app.ui.timer

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.presa.app.R
import com.presa.app.ui.components.PresaCard
import com.presa.app.ui.components.PresaIconButton
import com.presa.app.ui.theme.LocalPresaColors
import com.presa.app.ui.theme.PresaType

@Composable
fun TimerSection(viewModel: TimerViewModel) {
    val timer by viewModel.state.collectAsStateWithLifecycle()
    TimerBar(
        timer = timer,
        onToggle = viewModel::toggle,
        onReset = viewModel::reset,
    )
}

@Composable
fun TimerAlertHost(viewModel: TimerViewModel) {
    val timer by viewModel.state.collectAsStateWithLifecycle()
    if (timer.alertPending) {
        TimerAlertDialog(
            timer = timer,
            onDismiss = viewModel::acknowledgeAlert,
        )
    }
}

@Composable
fun TimerBar(
    timer: TimerUiState,
    onToggle: () -> Unit,
    onReset: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LocalPresaColors.current
    PresaCard(
        modifier = modifier,
        contentPadding = androidx.compose.foundation.layout.PaddingValues(14.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Rounded.Timer,
                contentDescription = null,
                tint = colors.gold,
                modifier = Modifier.size(22.dp),
            )
            Spacer(Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = stringResource(R.string.timer_title),
                        style = MaterialTheme.typography.labelMedium,
                        color = colors.ink2,
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = stringResource(
                            if (timer.running) R.string.timer_running else R.string.timer_paused,
                        ),
                        style = MaterialTheme.typography.labelSmall,
                        color = if (timer.running) colors.success else colors.muted,
                    )
                }
                Text(
                    text = timer.elapsedLabel,
                    style = PresaType.timer,
                    color = colors.ink,
                )
            }
            PresaIconButton(
                icon = if (timer.running) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                contentDescription = stringResource(
                    if (timer.running) R.string.timer_pause else R.string.timer_start,
                ),
                onClick = onToggle,
                tint = colors.navy,
            )
            Spacer(Modifier.width(4.dp))
            PresaIconButton(
                icon = Icons.Rounded.Refresh,
                contentDescription = stringResource(R.string.timer_reset),
                onClick = onReset,
                enabled = timer.elapsedMs > 0L || timer.running,
            )
        }
        Spacer(Modifier.height(9.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(R.string.timer_target, timer.alertMinutes),
                style = MaterialTheme.typography.bodySmall,
                color = colors.muted,
            )
            Spacer(Modifier.weight(1f))
            Text(
                text = "${(timer.progress * 100).toInt()}%",
                style = MaterialTheme.typography.labelSmall,
                color = colors.muted,
            )
        }
        Spacer(Modifier.height(6.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .clip(CircleShape)
                .background(colors.panel3),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(timer.progress)
                    .height(4.dp)
                    .background(if (timer.progress >= 1f) colors.alert else colors.gold),
            )
        }
    }
}

@Composable
fun TimerAlertDialog(
    timer: TimerUiState,
    onDismiss: () -> Unit,
) {
    val colors = LocalPresaColors.current
    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Rounded.Timer,
                contentDescription = null,
                tint = colors.alert,
            )
        },
        title = {
            Text(
                text = stringResource(R.string.timer_alert_title),
                color = colors.ink,
            )
        },
        text = {
            Text(
                text = stringResource(R.string.timer_alert_body, timer.alertMinutes),
                color = colors.ink2,
            )
        },
        confirmButton = {
            androidx.compose.material3.TextButton(onClick = onDismiss) {
                Text(
                    text = stringResource(R.string.dialog_ok),
                    color = colors.navy,
                )
            }
        },
        containerColor = colors.panel,
        titleContentColor = colors.ink,
        textContentColor = colors.ink2,
    )
}
