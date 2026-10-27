package com.presa.app.ui.permission

import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Bluetooth
import androidx.compose.material.icons.rounded.CloudOff
import androidx.compose.material.icons.rounded.Keyboard
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import com.presa.app.R
import com.presa.app.data.BluetoothPermissions
import com.presa.app.di.AppContainer
import com.presa.app.ui.components.LocalHaptics
import com.presa.app.ui.components.LocalHapticsEnabled
import com.presa.app.ui.components.PresaBackground
import com.presa.app.ui.components.PresaCard
import com.presa.app.ui.components.PresaMark
import com.presa.app.ui.components.PresaPrimaryButton
import com.presa.app.ui.components.PresaSecondaryButton
import com.presa.app.ui.theme.LocalPresaColors
import com.presa.app.ui.util.openAppSettings

/**
 * On Android 12+ BLUETOOTH_CONNECT is required before the HID profile can even
 * be requested. On older versions the permission is install-time, so the app
 * content is shown immediately.
 */
@Composable
fun ConnectPermissionGate(
    container: AppContainer,
    content: @Composable () -> Unit,
) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
        content()
        return
    }

    val context = LocalContext.current
    var granted by remember { mutableStateOf(BluetoothPermissions.hasConnectPermission(context)) }
    var denied by rememberSaveable { mutableStateOf(false) }

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions(),
    ) {
        granted = BluetoothPermissions.hasConnectPermission(context)
        denied = !granted
        container.hidManager.onPermissionsChanged()
    }

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        granted = BluetoothPermissions.hasConnectPermission(context)
        if (granted) denied = false
        container.hidManager.onPermissionsChanged()
    }

    if (granted) {
        content()
    } else {
        PermissionScreen(
            denied = denied,
            onGrant = {
                launcher.launch(BluetoothPermissions.connectionPermissions())
            },
            onOpenSettings = {
                if (!context.openAppSettings()) denied = true
            },
        )
    }
}

@Composable
private fun PermissionScreen(
    denied: Boolean,
    onGrant: () -> Unit,
    onOpenSettings: () -> Unit,
) {
    val colors = LocalPresaColors.current
    val haptics = LocalHaptics.current
    val hapticsEnabled = LocalHapticsEnabled.current

    PresaBackground {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing),
            contentAlignment = Alignment.TopCenter,
        ) {
            Column(
                modifier = Modifier
                    .widthIn(max = 540.dp)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp, vertical = 28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                PresaMark(size = 68.dp)
                Spacer(Modifier.height(18.dp))
                Text(
                    text = stringResource(R.string.permission_title),
                    style = MaterialTheme.typography.headlineLarge,
                    color = colors.ink,
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.permission_subtitle),
                    style = MaterialTheme.typography.bodyLarge,
                    color = colors.ink2,
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.height(24.dp))

                PresaCard {
                    Text(
                        text = stringResource(R.string.permission_body),
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.ink2,
                    )
                    Spacer(Modifier.height(16.dp))
                    FeatureRow(
                        icon = Icons.Rounded.Keyboard,
                        text = stringResource(R.string.permission_feature_hid),
                    )
                    FeatureRow(
                        icon = Icons.Rounded.CloudOff,
                        text = stringResource(R.string.permission_feature_offline),
                    )
                    FeatureRow(
                        icon = Icons.Rounded.Lock,
                        text = stringResource(R.string.permission_feature_local),
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = stringResource(R.string.permission_note),
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.muted,
                    )
                }

                if (denied) {
                    Spacer(Modifier.height(14.dp))
                    PresaCard(
                        modifier = Modifier,
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(14.dp),
                    ) {
                        Row(verticalAlignment = Alignment.Top) {
                            Icon(
                                imageVector = Icons.Rounded.Bluetooth,
                                contentDescription = null,
                                tint = colors.alert,
                                modifier = Modifier.size(20.dp),
                            )
                            Spacer(Modifier.width(10.dp))
                            Text(
                                text = stringResource(R.string.permission_denied),
                                style = MaterialTheme.typography.bodySmall,
                                color = colors.alert,
                            )
                        }
                    }
                }

                Spacer(Modifier.height(22.dp))
                PresaPrimaryButton(
                    text = stringResource(R.string.permission_grant),
                    onClick = {
                        haptics?.press(hapticsEnabled)
                        onGrant()
                    },
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(10.dp))
                PresaSecondaryButton(
                    text = stringResource(R.string.permission_open_settings),
                    onClick = onOpenSettings,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(20.dp))
            }
        }
    }
}

@Composable
private fun FeatureRow(
    icon: ImageVector,
    text: String,
) {
    val colors = LocalPresaColors.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .background(colors.goldSoft, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = colors.gold,
                modifier = Modifier.size(17.dp),
            )
        }
        Spacer(Modifier.width(12.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = colors.ink,
        )
    }
}
