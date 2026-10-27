package com.presa.app.ui.connection

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Bluetooth
import androidx.compose.material.icons.rounded.LinkOff
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.presa.app.R
import com.presa.app.domain.hid.HidIssue
import com.presa.app.domain.hid.HidState
import com.presa.app.domain.hid.HidStatus
import com.presa.app.ui.components.PresaPrimaryButton
import com.presa.app.ui.theme.LocalPresaColors
import com.presa.app.ui.theme.PresaType

@Composable
fun hidStatusColor(state: HidState): Color {
    val colors = LocalPresaColors.current
    return when (state.status) {
        HidStatus.CONNECTED -> colors.success
        HidStatus.CONNECTING, HidStatus.REGISTERING -> colors.gold
        HidStatus.READY -> colors.forwardBlue
        HidStatus.DISCONNECTED -> colors.muted
        HidStatus.BLUETOOTH_OFF,
        HidStatus.PERMISSION_MISSING,
        HidStatus.UNSUPPORTED,
        -> colors.alert
    }
}

/** Sticky, compact HID status shown on top of the remote. */
@Composable
fun ConnectionStatusBar(
    state: HidState,
    onOpenConnection: () -> Unit,
    onReconnect: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LocalPresaColors.current
    val dotColor = hidStatusColor(state)
    val host = state.connectedHost ?: state.lastHost
    val canReconnect = state.status == HidStatus.DISCONNECTED && state.lastHost != null
    val actionLabel = when {
        canReconnect -> stringResource(R.string.connection_reconnect)
        state.status == HidStatus.CONNECTED -> stringResource(R.string.connection_manage)
        else -> stringResource(R.string.connection_connect)
    }
    val detail = when {
        host != null -> host.address
        state.status == HidStatus.READY -> stringResource(R.string.status_ready_hint)
        else -> null
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable(role = Role.Button, onClick = onOpenConnection),
        shape = RoundedCornerShape(14.dp),
        color = colors.panel2,
        border = androidx.compose.foundation.BorderStroke(1.dp, colors.line),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 13.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(9.dp)
                    .background(dotColor, CircleShape)
                    .border(2.dp, dotColor.copy(alpha = 0.22f), CircleShape),
            )
            Spacer(Modifier.width(10.dp))
            Icon(
                imageVector = Icons.Rounded.Bluetooth,
                contentDescription = null,
                tint = dotColor,
                modifier = Modifier.size(18.dp),
            )
            Spacer(Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = hidStatusLabel(state),
                    style = MaterialTheme.typography.labelLarge,
                    color = colors.ink,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (detail != null) {
                    Text(
                        text = detail,
                        style = PresaType.keycap,
                        color = colors.muted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            Spacer(Modifier.width(8.dp))
            Text(
                text = actionLabel,
                style = MaterialTheme.typography.labelMedium,
                color = colors.navy,
                maxLines = 1,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable {
                        if (canReconnect) onReconnect() else onOpenConnection()
                    }
                    .padding(horizontal = 8.dp, vertical = 6.dp),
            )
        }
    }
}

@Composable
fun DisconnectionBanner(
    state: HidState,
    onReconnect: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LocalPresaColors.current
    if (state.issue != HidIssue.CONNECTION_LOST || state.lastHost == null) return
    val hostName = hostDisplayName(state.connectedHost?.name ?: state.lastHost.name)

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = colors.alertSoft,
        border = androidx.compose.foundation.BorderStroke(1.dp, colors.alert.copy(alpha = 0.3f)),
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Rounded.LinkOff,
                    contentDescription = null,
                    tint = colors.alert,
                    modifier = Modifier.size(19.dp),
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.connection_lost_title),
                    style = MaterialTheme.typography.titleSmall,
                    color = colors.alert,
                )
            }
            Spacer(Modifier.height(5.dp))
            Text(
                text = stringResource(R.string.connection_lost_body, hostName),
                style = MaterialTheme.typography.bodySmall,
                color = colors.alert,
            )
            Spacer(Modifier.height(10.dp))
            PresaPrimaryButton(
                text = stringResource(R.string.connection_reconnect),
                onClick = onReconnect,
                containerColor = colors.alert,
                contentColor = colors.onAlert,
            )
        }
    }
}
