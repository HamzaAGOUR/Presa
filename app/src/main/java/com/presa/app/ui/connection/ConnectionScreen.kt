package com.presa.app.ui.connection

import android.content.Context
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Bluetooth
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.DevicesOther
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.presa.app.R
import com.presa.app.data.BluetoothPermissions
import com.presa.app.di.AppContainer
import com.presa.app.domain.hid.HidHost
import com.presa.app.domain.hid.HidIssue
import com.presa.app.domain.hid.HidState
import com.presa.app.domain.hid.HidStatus
import com.presa.app.ui.components.PresaCard
import com.presa.app.ui.components.PresaPrimaryButton
import com.presa.app.ui.components.PresaScreenHeader
import com.presa.app.ui.components.PresaSecondaryButton
import com.presa.app.ui.components.SectionLabel
import com.presa.app.ui.theme.LocalPresaColors
import com.presa.app.ui.theme.PresaType
import com.presa.app.ui.util.openAppSettings
import com.presa.app.ui.util.openBluetoothSettings

@Composable
fun ConnectionScreen(
    container: AppContainer,
    onBack: () -> Unit,
    onMessage: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel: ConnectionViewModel = viewModel(factory = ConnectionViewModel.factory(container))
    val state by viewModel.state.collectAsStateWithLifecycle()
    val hosts by viewModel.hosts.collectAsStateWithLifecycle()
    val isDiscovering by viewModel.isDiscovering.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val colors = LocalPresaColors.current

    var scanGranted by remember {
        mutableStateOf(BluetoothPermissions.hasScanPermission(context))
    }
    val scanLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions(),
    ) { permissions ->
        scanGranted = BluetoothPermissions.hasScanPermission(context)
        viewModel.onPermissionsChanged()
        if (scanGranted) {
            if (!viewModel.startDiscovery()) {
                onMessage(context.getString(R.string.message_scan_unavailable))
            }
        } else if (permissions.isNotEmpty()) {
            onMessage(context.getString(R.string.message_no_scan_permission))
        }
    }

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        scanGranted = BluetoothPermissions.hasScanPermission(context)
        viewModel.onPermissionsChanged()
    }

    LaunchedEffect(Unit) { viewModel.refresh() }
    DisposableEffect(Unit) {
        onDispose { viewModel.stopDiscovery() }
    }

    fun requestScan() {
        if (!scanGranted) {
            scanLauncher.launch(BluetoothPermissions.scanPermissions())
        } else if (isDiscovering) {
            viewModel.stopDiscovery()
        } else if (!viewModel.startDiscovery()) {
            onMessage(context.getString(R.string.message_scan_unavailable))
        }
    }

    fun requestPair(host: HidHost) {
        if (viewModel.pair(host.address)) {
            onMessage(context.getString(R.string.message_pairing))
        } else {
            onMessage(context.getString(R.string.message_pair_unavailable))
        }
    }

    fun openAppSettings() {
        if (!context.openAppSettings()) {
            onMessage(context.getString(R.string.message_settings_unavailable))
        }
    }

    fun openBluetoothSettings() {
        if (!context.openBluetoothSettings()) {
            onMessage(context.getString(R.string.message_settings_unavailable))
        }
    }

    Column(modifier = modifier.fillMaxSize()) {
        PresaScreenHeader(
            title = stringResource(R.string.connection_screen_title),
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
                text = stringResource(R.string.connection_screen_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = colors.ink2,
            )

            ConnectionStatusCard(
                state = state,
                onDisconnect = viewModel::disconnect,
                onReconnect = viewModel::reconnect,
                onOpenBluetoothSettings = ::openBluetoothSettings,
                onOpenAppSettings = ::openAppSettings,
            )

            if (!scanGranted) {
                PresaCard {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Rounded.Lock,
                            contentDescription = null,
                            tint = colors.gold,
                            modifier = Modifier.size(20.dp),
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = stringResource(R.string.connection_permission_scan_title),
                            style = MaterialTheme.typography.titleSmall,
                            color = colors.ink,
                        )
                    }
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = stringResource(R.string.connection_permission_scan_body),
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.ink2,
                    )
                    Spacer(Modifier.height(10.dp))
                    PresaPrimaryButton(
                        text = stringResource(R.string.connection_permission_scan_button),
                        onClick = {
                            scanLauncher.launch(BluetoothPermissions.scanPermissions())
                        },
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                SectionLabel(
                    text = stringResource(R.string.connection_bonded_title),
                    modifier = Modifier.weight(1f),
                )
            }

            if (hosts.bonded.isEmpty()) {
                PresaCard {
                    Text(
                        text = stringResource(R.string.connection_bonded_empty),
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.muted,
                    )
                }
            } else {
                PresaCard(contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)) {
                    hosts.bonded.forEachIndexed { index, host ->
                        HostRow(
                            host = host,
                            state = state,
                            scanning = false,
                            onConnect = { viewModel.connect(host) },
                            onDisconnect = viewModel::disconnect,
                            onPair = { requestPair(host) },
                        )
                        if (index != hosts.bonded.lastIndex) {
                            HorizontalDivider(color = colors.line)
                        }
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                SectionLabel(
                    text = stringResource(R.string.connection_nearby_title),
                    modifier = Modifier.weight(1f),
                )
                Spacer(Modifier.width(10.dp))
                PresaSecondaryButton(
                    text = if (isDiscovering) {
                        stringResource(R.string.connection_scan_stop)
                    } else {
                        stringResource(R.string.connection_scan)
                    },
                    onClick = ::requestScan,
                    leadingIcon = if (isDiscovering) Icons.Rounded.Close else Icons.Rounded.Search,
                )
            }

            if (isDiscovering) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(14.dp),
                        strokeWidth = 2.dp,
                        color = colors.gold,
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.connection_scanning),
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.muted,
                    )
                }
            }

            if (hosts.nearby.isEmpty()) {
                PresaCard {
                    Text(
                        text = stringResource(R.string.connection_nearby_empty),
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.muted,
                    )
                }
            } else {
                PresaCard(contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)) {
                    hosts.nearby.forEachIndexed { index, host ->
                        HostRow(
                            host = host,
                            state = state,
                            scanning = true,
                            onConnect = { viewModel.connect(host) },
                            onDisconnect = viewModel::disconnect,
                            onPair = { requestPair(host) },
                        )
                        if (index != hosts.nearby.lastIndex) {
                            HorizontalDivider(color = colors.line)
                        }
                    }
                }
            }

            PresaCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Rounded.Bluetooth,
                        contentDescription = null,
                        tint = colors.gold,
                        modifier = Modifier.size(19.dp),
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.connection_tip_title),
                        style = MaterialTheme.typography.titleSmall,
                        color = colors.ink,
                    )
                }
                Spacer(Modifier.height(6.dp))
                Text(
                    text = stringResource(R.string.connection_tip_body),
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.ink2,
                )
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun ConnectionStatusCard(
    state: HidState,
    onDisconnect: () -> Unit,
    onReconnect: () -> Unit,
    onOpenBluetoothSettings: () -> Unit,
    onOpenAppSettings: () -> Unit,
) {
    val colors = LocalPresaColors.current
    val host = state.connectedHost ?: state.lastHost
    val hostName = hostDisplayName(host?.name)
    val dotColor = hidStatusColor(state)
    val issue = hidIssueLabel(state.issue)
    val unsupported = state.status == HidStatus.UNSUPPORTED

    PresaCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .then(Modifier)
                    .background(dotColor),
            )
            Spacer(Modifier.width(9.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = hidStatusLabel(state),
                    style = MaterialTheme.typography.titleSmall,
                    color = colors.ink,
                )
                if (host != null) {
                    Text(
                        text = host.address,
                        style = PresaType.keycap,
                        color = colors.muted,
                    )
                }
            }
            if (state.status == HidStatus.REGISTERING ||
                state.status == HidStatus.CONNECTING
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    strokeWidth = 2.dp,
                    color = colors.gold,
                )
            }
        }

        if (issue != null && !unsupported) {
            Spacer(Modifier.height(8.dp))
            Text(
                text = issue,
                style = MaterialTheme.typography.bodySmall,
                color = if (state.status == HidStatus.DISCONNECTED) colors.muted else colors.alert,
            )
        }

        when (state.status) {
            HidStatus.BLUETOOTH_OFF -> {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.connection_need_bluetooth_body),
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.ink2,
                )
                Spacer(Modifier.height(10.dp))
                PresaPrimaryButton(
                    text = stringResource(R.string.connection_open_bluetooth_settings),
                    onClick = onOpenBluetoothSettings,
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            HidStatus.PERMISSION_MISSING -> {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.connection_need_permission_body),
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.ink2,
                )
                Spacer(Modifier.height(10.dp))
                PresaPrimaryButton(
                    text = stringResource(R.string.connection_open_app_settings),
                    onClick = onOpenAppSettings,
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            HidStatus.UNSUPPORTED -> {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = if (state.issue == HidIssue.REGISTER_FAILED) {
                        stringResource(R.string.connection_register_failed_body)
                    } else {
                        stringResource(R.string.connection_unsupported_body)
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.ink2,
                )
            }

            HidStatus.CONNECTED -> {
                Spacer(Modifier.height(10.dp))
                PresaSecondaryButton(
                    text = stringResource(R.string.connection_disconnect),
                    onClick = onDisconnect,
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            HidStatus.DISCONNECTED -> {
                if (host != null) {
                    Spacer(Modifier.height(10.dp))
                    PresaPrimaryButton(
                        text = stringResource(
                            if (state.issue == HidIssue.CONNECTION_LOST) {
                                R.string.connection_reconnect
                            } else {
                                R.string.connection_connect
                            },
                        ),
                        onClick = onReconnect,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }

            HidStatus.READY -> {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.status_ready_hint),
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.muted,
                )
            }

            HidStatus.REGISTERING,
            HidStatus.CONNECTING,
            -> Unit
        }
    }
}

@Composable
private fun HostRow(
    host: HidHost,
    state: HidState,
    scanning: Boolean,
    onConnect: () -> Unit,
    onDisconnect: () -> Unit,
    onPair: () -> Unit,
) {
    val colors = LocalPresaColors.current
    val name = hostDisplayName(host.name)
    val selectedAddress = state.connectedHost?.address ?: state.lastHost?.address
    val isConnected = state.status == HidStatus.CONNECTED && selectedAddress == host.address
    val isConnecting = state.status == HidStatus.CONNECTING && selectedAddress == host.address

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 11.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(
                    if (isConnected) colors.successSoft else colors.panel2,
                ),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = if (scanning) Icons.Rounded.DevicesOther else Icons.Rounded.Bluetooth,
                contentDescription = null,
                tint = if (isConnected) colors.success else colors.ink2,
                modifier = Modifier.size(18.dp),
            )
        }
        Spacer(Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = name,
                style = MaterialTheme.typography.titleSmall,
                color = colors.ink,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = host.address,
                    style = PresaType.keycap,
                    color = colors.muted,
                )
                if (isConnected) {
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = stringResource(R.string.connection_current),
                        style = MaterialTheme.typography.labelSmall,
                        color = colors.success,
                    )
                } else if (isConnecting) {
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = stringResource(R.string.status_connecting),
                        style = MaterialTheme.typography.labelSmall,
                        color = colors.gold,
                    )
                }
            }
        }
        Spacer(Modifier.width(8.dp))
        when {
            isConnected -> MiniAction(
                text = stringResource(R.string.connection_disconnect),
                onClick = onDisconnect,
            )

            isConnecting -> CircularProgressIndicator(
                modifier = Modifier.size(16.dp),
                strokeWidth = 2.dp,
                color = colors.gold,
            )

            host.bonded -> MiniAction(
                text = stringResource(R.string.connection_connect),
                onClick = onConnect,
            )

            else -> MiniAction(
                text = stringResource(R.string.connection_pair),
                onClick = onPair,
            )
        }
    }
}

@Composable
private fun MiniAction(
    text: String,
    onClick: () -> Unit,
) {
    val colors = LocalPresaColors.current
    Surface(
        modifier = Modifier
            .clip(RoundedCornerShape(9.dp))
            .clickable(role = Role.Button, onClick = onClick),
        shape = RoundedCornerShape(9.dp),
        color = colors.panel2,
        border = BorderStroke(1.dp, colors.line),
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            color = colors.navy,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            maxLines = 1,
        )
    }
}
