package com.presa.app.ui.connection

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.presa.app.R
import com.presa.app.domain.hid.HidIssue
import com.presa.app.domain.hid.HidState
import com.presa.app.domain.hid.HidStatus

@Composable
fun hostDisplayName(name: String?): String =
    name?.takeIf { it.isNotBlank() } ?: stringResource(R.string.unknown_device)

@Composable
fun hidStatusLabel(state: HidState): String {
    val host = state.connectedHost ?: state.lastHost
    val hostName = hostDisplayName(host?.name)
    return when (state.status) {
        HidStatus.CONNECTED -> stringResource(R.string.status_connected, hostName)
        HidStatus.CONNECTING -> if (host != null) {
            stringResource(R.string.status_connecting_to, hostName)
        } else {
            stringResource(R.string.status_connecting)
        }

        HidStatus.READY -> stringResource(R.string.status_ready)
        HidStatus.REGISTERING -> stringResource(R.string.status_registering)
        HidStatus.BLUETOOTH_OFF -> stringResource(R.string.status_bluetooth_off)
        HidStatus.PERMISSION_MISSING -> stringResource(R.string.status_permission_missing)
        HidStatus.UNSUPPORTED -> stringResource(R.string.status_unsupported)
        HidStatus.DISCONNECTED -> if (host != null) {
            stringResource(R.string.status_disconnected_host, hostName)
        } else {
            stringResource(R.string.status_disconnected)
        }
    }
}

@Composable
fun hidIssueLabel(issue: HidIssue?): String? = when (issue) {
    HidIssue.PROFILE_UNAVAILABLE -> stringResource(R.string.connection_no_hid_support_short)
    HidIssue.REGISTER_FAILED -> stringResource(R.string.connection_register_failed_body)
    HidIssue.CONNECTION_FAILED -> stringResource(R.string.connection_issue_connect_failed)
    HidIssue.NOT_BONDED -> stringResource(R.string.connection_issue_not_bonded)
    HidIssue.CONNECTION_LOST -> stringResource(R.string.connection_auto_reconnect)
    HidIssue.SEND_FAILED -> stringResource(R.string.connection_issue_send_failed)
    null -> null
}
