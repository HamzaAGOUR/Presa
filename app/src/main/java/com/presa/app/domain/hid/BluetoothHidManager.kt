package com.presa.app.domain.hid

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothHidDevice
import android.bluetooth.BluetoothHidDeviceAppSdpSettings
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.core.content.ContextCompat
import androidx.core.content.IntentCompat
import com.presa.app.R
import com.presa.app.data.BluetoothPermissions
import com.presa.app.data.PresaPreferences
import com.presa.app.domain.model.KeyChord
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Owns the Android Bluetooth HID Device profile.
 *
 * The phone registers a standard boot-keyboard report descriptor, then connects
 * to a bonded HID host (laptop). All report writes go through a Mutex so a held
 * Previous/Next key can never interleave a key-down with another key-down.
 */
@SuppressLint("MissingPermission")
class BluetoothHidManager(
    context: Context,
    private val preferences: PresaPreferences,
    private val scope: CoroutineScope,
) {
    private val appContext = context.applicationContext
    private val bluetoothManager: BluetoothManager? =
        appContext.getSystemService(BluetoothManager::class.java)
    private val adapter: BluetoothAdapter? = bluetoothManager?.adapter

    private val _state = MutableStateFlow(
        if (adapter == null) {
            HidState(HidStatus.UNSUPPORTED, issue = HidIssue.PROFILE_UNAVAILABLE)
        } else {
            HidState(HidStatus.REGISTERING)
        },
    )
    val state: StateFlow<HidState> = _state.asStateFlow()

    private val _hosts = MutableStateFlow(HostsState())
    val hosts: StateFlow<HostsState> = _hosts.asStateFlow()

    private val _isDiscovering = MutableStateFlow(false)
    val isDiscovering: StateFlow<Boolean> = _isDiscovering.asStateFlow()

    private val reportMutex = Mutex()
    private val knownDevices = LinkedHashMap<String, BluetoothDevice>()
    private val discoveredHosts = LinkedHashMap<String, HidHost>()

    private var profile: BluetoothHidDevice? = null
    private var profileRequested = false
    private var registerAttempted = false
    private var appRegistered = false
    private var selectedDevice: BluetoothDevice? = null
    private var lastHost: HidHost? = null
    private var userDisconnected = false
    private var reconnectAttempts = 0
    private var closing = false

    private var profileTimeoutJob: Job? = null
    private var connectTimeoutJob: Job? = null
    private var autoConnectJob: Job? = null
    private var reconnectJob: Job? = null
    private var receiverRegistered = false

    private val hidCallback = object : BluetoothHidDevice.Callback() {
        override fun onAppStatusChanged(pluggedDevice: BluetoothDevice?, registered: Boolean) {
            if (closing) return
            if (!registered) {
                appRegistered = false
                registerAttempted = false
                if (profile != null) {
                    publish(HidStatus.UNSUPPORTED, issue = HidIssue.REGISTER_FAILED)
                }
                return
            }

            appRegistered = true
            registerAttempted = true
            syncConnection()
            maybeAutoConnect()
            refreshHosts()
        }

        override fun onConnectionStateChanged(device: BluetoothDevice, state: Int) {
            if (closing) return
            when (state) {
                BluetoothProfile.STATE_CONNECTING -> {
                    rememberDevice(device)
                    selectedDevice = device
                    publish(
                        HidStatus.CONNECTING,
                        connectedHost = hostOf(device).copy(bonded = true),
                    )
                }

                BluetoothProfile.STATE_CONNECTED -> {
                    connectTimeoutJob?.cancel()
                    reconnectAttempts = 0
                    reconnectJob?.cancel()
                    updateConnected(device)
                    refreshHosts()
                }

                BluetoothProfile.STATE_DISCONNECTING -> Unit

                BluetoothProfile.STATE_DISCONNECTED -> {
                    connectTimeoutJob?.cancel()
                    val wasConnected = _state.value.status == HidStatus.CONNECTED
                    val isSelectedDevice = selectedDevice?.address == device.address
                    if (isSelectedDevice) selectedDevice = null

                    if (userDisconnected) {
                        publish(HidStatus.DISCONNECTED)
                    } else {
                        publish(
                            HidStatus.DISCONNECTED,
                            issue = if (wasConnected) HidIssue.CONNECTION_LOST else null,
                        )
                        if (wasConnected) scheduleReconnect()
                    }
                    refreshHosts()
                }
            }
        }

        override fun onGetReport(
            device: BluetoothDevice,
            type: Byte,
            id: Byte,
            bufferSize: Int,
        ) {
            try {
                if (type == BluetoothHidDevice.REPORT_TYPE_INPUT && id == 0.toByte()) {
                    profile?.replyReport(device, type, id, HidReport.keyUp())
                } else {
                    profile?.reportError(device, BluetoothHidDevice.ERROR_RSP_UNSUPPORTED_REQ)
                }
            } catch (_: SecurityException) {
                // Permission was revoked; the app will refresh on resume.
            }
        }

        override fun onVirtualCableUnplug(device: BluetoothDevice) {
            if (selectedDevice?.address == device.address) selectedDevice = null
            publish(HidStatus.DISCONNECTED, issue = HidIssue.CONNECTION_LOST)
            refreshHosts()
            scheduleReconnect()
        }
    }

    private val profileServiceListener = object : BluetoothProfile.ServiceListener {
        override fun onServiceConnected(profileType: Int, proxy: BluetoothProfile?) {
            if (profileType != BluetoothProfile.HID_DEVICE) return
            profileTimeoutJob?.cancel()

            if (proxy !is BluetoothHidDevice) {
                profileRequested = false
                publish(HidStatus.UNSUPPORTED, issue = HidIssue.PROFILE_UNAVAILABLE)
                return
            }

            profile = proxy
            registerAttempted = false
            registerApplication(proxy)
        }

        override fun onServiceDisconnected(profileType: Int) {
            if (profileType != BluetoothProfile.HID_DEVICE) return
            profile = null
            profileRequested = false
            registerAttempted = false
            appRegistered = false
            selectedDevice = null
            connectTimeoutJob?.cancel()
            publish(HidStatus.UNSUPPORTED, issue = HidIssue.PROFILE_UNAVAILABLE)
        }
    }

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                BluetoothAdapter.ACTION_STATE_CHANGED -> {
                    when (intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1)) {
                        BluetoothAdapter.STATE_ON -> refresh()
                        BluetoothAdapter.STATE_OFF, BluetoothAdapter.STATE_TURNING_OFF -> {
                            handleAdapterOff()
                        }
                    }
                }

                BluetoothDevice.ACTION_BOND_STATE_CHANGED -> {
                    val device = IntentCompat.getParcelableExtra(
                        intent,
                        BluetoothDevice.EXTRA_DEVICE,
                        BluetoothDevice::class.java,
                    )
                    if (device != null) {
                        knownDevices[device.address] = device
                        if (safeBondState(device) == BluetoothDevice.BOND_BONDED) {
                            discoveredHosts.remove(device.address)
                            if (lastHost?.address == device.address) {
                                lastHost = hostOf(device)
                            }
                        }
                    }
                    refreshHosts()
                }

                BluetoothDevice.ACTION_FOUND -> {
                    if (!BluetoothPermissions.hasScanPermission(appContext)) return
                    val device = IntentCompat.getParcelableExtra(
                        intent,
                        BluetoothDevice.EXTRA_DEVICE,
                        BluetoothDevice::class.java,
                    ) ?: return
                    val rssi = intent.getShortExtra(BluetoothDevice.EXTRA_RSSI, Short.MIN_VALUE)
                    rememberDevice(device)
                    if (safeBondState(device) != BluetoothDevice.BOND_BONDED) {
                        discoveredHosts[device.address] = hostOf(device, rssi)
                    }
                    refreshHosts()
                }

                BluetoothAdapter.ACTION_DISCOVERY_STARTED -> _isDiscovering.value = true
                BluetoothAdapter.ACTION_DISCOVERY_FINISHED -> _isDiscovering.value = false
            }
        }
    }

    init {
        registerReceiver()
        scope.launch {
            val stored = preferences.current()
            if (stored.lastDeviceAddress != null) {
                lastHost = HidHost(
                    name = stored.lastDeviceName,
                    address = stored.lastDeviceAddress,
                    bonded = true,
                )
            }
            refresh()
        }
    }

    fun refresh() {
        if (closing) return
        val bluetoothAdapter = adapter
        if (bluetoothAdapter == null) {
            publish(HidStatus.UNSUPPORTED, issue = HidIssue.PROFILE_UNAVAILABLE)
            return
        }
        if (!BluetoothPermissions.hasConnectPermission(appContext)) {
            publish(HidStatus.PERMISSION_MISSING)
            return
        }

        val enabled = try {
            bluetoothAdapter.isEnabled
        } catch (_: SecurityException) {
            publish(HidStatus.PERMISSION_MISSING)
            return
        }
        if (!enabled) {
            publish(HidStatus.BLUETOOTH_OFF)
            return
        }

        profileTimeoutJob?.cancel()
        refreshHosts()

        val hidProfile = profile
        if (hidProfile == null) {
            requestProfile(bluetoothAdapter)
        } else if (appRegistered) {
            syncConnection()
            maybeAutoConnect()
        } else if (!registerAttempted) {
            registerApplication(hidProfile)
        }
    }

    fun onPermissionsChanged() = refresh()

    fun connect(host: HidHost) {
        stopDiscovery()
        userDisconnected = false
        reconnectAttempts = 0
        reconnectJob?.cancel()
        autoConnectJob?.cancel()
        lastHost = host.copy(bonded = true)
        persistHost(host)

        val device = findDevice(host.address)
        if (device == null) {
            publish(HidStatus.DISCONNECTED, issue = HidIssue.CONNECTION_FAILED)
            return
        }
        if (safeBondState(device) != BluetoothDevice.BOND_BONDED) {
            publish(HidStatus.DISCONNECTED, issue = HidIssue.NOT_BONDED)
            return
        }
        if (profile == null || !appRegistered) {
            refresh()
            if (!appRegistered) return
        }
        connectInternal(device, automatic = false)
    }

    fun disconnect() {
        userDisconnected = true
        reconnectAttempts = 0
        reconnectJob?.cancel()
        autoConnectJob?.cancel()
        connectTimeoutJob?.cancel()

        val device = selectedDevice
        selectedDevice = null
        if (device != null) {
            try {
                profile?.disconnect(device)
            } catch (_: SecurityException) {
                // State below is still the safest representation.
            }
        }
        publish(HidStatus.DISCONNECTED)
        refreshHosts()
    }

    fun reconnect() {
        userDisconnected = false
        reconnectAttempts = 0
        val host = lastHost ?: return
        if (profile == null || !appRegistered) {
            refresh()
            return
        }
        val device = findDevice(host.address) ?: return
        if (safeBondState(device) != BluetoothDevice.BOND_BONDED) {
            publish(HidStatus.DISCONNECTED, issue = HidIssue.NOT_BONDED)
            return
        }
        connectInternal(device, automatic = false)
    }

    fun pair(address: String): Boolean {
        val device = findDevice(address) ?: return false
        return try {
            stopDiscovery()
            device.createBond()
        } catch (_: SecurityException) {
            false
        }
    }

    fun startDiscovery(): Boolean {
        val bluetoothAdapter = adapter ?: return false
        if (!BluetoothPermissions.hasConnectPermission(appContext) ||
            !BluetoothPermissions.hasScanPermission(appContext)
        ) {
            return false
        }

        discoveredHosts.clear()
        refreshHosts()
        return try {
            if (bluetoothAdapter.isDiscovering) bluetoothAdapter.cancelDiscovery()
            bluetoothAdapter.startDiscovery()
        } catch (_: SecurityException) {
            false
        }
    }

    fun stopDiscovery() {
        try {
            if (adapter?.isDiscovering == true) adapter.cancelDiscovery()
        } catch (_: SecurityException) {
            // Ignore, the system will stop discovery on the next lifecycle change.
        }
        _isDiscovering.value = false
    }

    suspend fun tap(chord: KeyChord): HidSendResult {
        reportMutex.withLock {
            if (!BluetoothPermissions.hasConnectPermission(appContext)) {
                return HidSendResult(false, HidSendFailure.PERMISSION)
            }
            val hidProfile = profile ?: return HidSendResult(false, HidSendFailure.NOT_CONNECTED)
            val device = connectedDevice() ?: return HidSendResult(
                false,
                HidSendFailure.NOT_CONNECTED,
            )

            var downSent = false
            return try {
                downSent = hidProfile.sendReport(
                    device,
                    HidReport.REPORT_ID,
                    HidReport.keyDown(chord),
                )
                if (!downSent) {
                    handleWriteFailure()
                    return HidSendResult(false, HidSendFailure.WRITE_FAILED)
                }
                delay(12L)
                HidSendResult(true)
            } catch (_: SecurityException) {
                publish(HidStatus.PERMISSION_MISSING)
                HidSendResult(false, HidSendFailure.PERMISSION)
            } catch (_: Exception) {
                handleWriteFailure()
                HidSendResult(false, HidSendFailure.WRITE_FAILED)
            } finally {
                if (downSent) {
                    try {
                        hidProfile.sendReport(
                            device,
                            HidReport.REPORT_ID,
                            HidReport.keyUp(),
                        )
                    } catch (_: Exception) {
                        runCatching {
                            hidProfile.sendReport(
                                device,
                                HidReport.REPORT_ID,
                                HidReport.releaseAll(),
                            )
                        }
                    }
                }
            }
        }
    }

    fun close() {
        closing = true
        profileTimeoutJob?.cancel()
        connectTimeoutJob?.cancel()
        autoConnectJob?.cancel()
        reconnectJob?.cancel()
        if (receiverRegistered) {
            runCatching { appContext.unregisterReceiver(receiver) }
            receiverRegistered = false
        }
        val hidProfile = profile
        if (hidProfile != null) {
            runCatching { hidProfile.unregisterApp() }
            runCatching {
                adapter?.closeProfileProxy(BluetoothProfile.HID_DEVICE, hidProfile)
            }
        }
        profile = null
        _isDiscovering.value = false
    }

    private fun registerReceiver() {
        if (receiverRegistered) return
        val filter = IntentFilter().apply {
            addAction(BluetoothAdapter.ACTION_STATE_CHANGED)
            addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED)
            addAction(BluetoothDevice.ACTION_FOUND)
            addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED)
            addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
        }
        ContextCompat.registerReceiver(
            appContext,
            receiver,
            filter,
            ContextCompat.RECEIVER_EXPORTED,
        )
        receiverRegistered = true
    }

    private fun requestProfile(bluetoothAdapter: BluetoothAdapter) {
        if (profileRequested) return
        profileRequested = true
        val requested = try {
            bluetoothAdapter.getProfileProxy(
                appContext,
                profileServiceListener,
                BluetoothProfile.HID_DEVICE,
            )
        } catch (_: SecurityException) {
            false
        } catch (_: IllegalArgumentException) {
            false
        }

        if (!requested) {
            profileRequested = false
            publish(HidStatus.UNSUPPORTED, issue = HidIssue.PROFILE_UNAVAILABLE)
            return
        }

        profileTimeoutJob?.cancel()
        profileTimeoutJob = scope.launch {
            delay(8_000L)
            if (profile == null && !closing) {
                profileRequested = false
                publish(HidStatus.UNSUPPORTED, issue = HidIssue.PROFILE_UNAVAILABLE)
            }
        }
    }

    private fun registerApplication(hidProfile: BluetoothHidDevice) {
        if (appRegistered || registerAttempted) return
        registerAttempted = true

        val sdp = BluetoothHidDeviceAppSdpSettings(
            appContext.getString(R.string.hid_device_name),
            appContext.getString(R.string.hid_device_description),
            "Presa",
            BluetoothHidDevice.SUBCLASS1_KEYBOARD,
            KeyboardDescriptor.BYTES,
        )

        val registered = try {
            hidProfile.registerApp(
                sdp,
                null,
                null,
                ContextCompat.getMainExecutor(appContext),
                hidCallback,
            )
        } catch (_: SecurityException) {
            registerAttempted = false
            publish(HidStatus.PERMISSION_MISSING)
            return
        } catch (_: Exception) {
            false
        }

        if (!registered) {
            registerAttempted = false
            publish(HidStatus.UNSUPPORTED, issue = HidIssue.REGISTER_FAILED)
        }
    }

    private fun handleAdapterOff() {
        connectTimeoutJob?.cancel()
        autoConnectJob?.cancel()
        reconnectJob?.cancel()
        selectedDevice = null
        appRegistered = false
        registerAttempted = false
        _isDiscovering.value = false

        val hidProfile = profile
        profile = null
        profileRequested = false
        if (hidProfile != null) {
            runCatching {
                adapter?.closeProfileProxy(BluetoothProfile.HID_DEVICE, hidProfile)
            }
        }
        publish(HidStatus.BLUETOOTH_OFF)
        refreshHosts()
    }

    private fun syncConnection() {
        val device = connectedDevice()
        if (device != null) {
            updateConnected(device)
            return
        }

        when (_state.value.status) {
            HidStatus.CONNECTED -> {
                selectedDevice = null
                publish(HidStatus.DISCONNECTED, issue = HidIssue.CONNECTION_LOST)
                scheduleReconnect()
            }

            HidStatus.CONNECTING -> Unit

            else -> {
                publish(HidStatus.READY)
                maybeAutoConnect()
            }
        }
    }

    private fun updateConnected(device: BluetoothDevice) {
        rememberDevice(device)
        selectedDevice = device
        val host = hostOf(device).copy(bonded = true)
        lastHost = host
        _state.value = HidState(
            status = HidStatus.CONNECTED,
            connectedHost = host,
            lastHost = host,
            issue = null,
        )
        persistHost(host)
    }

    private fun connectInternal(device: BluetoothDevice, automatic: Boolean): Boolean {
        val hidProfile = profile
        if (hidProfile == null || !appRegistered) {
            refresh()
            return false
        }

        selectedDevice = device
        val host = hostOf(device).copy(bonded = true)
        lastHost = host
        persistHost(host)
        publish(HidStatus.CONNECTING, connectedHost = host)

        val started = try {
            hidProfile.connect(device)
        } catch (_: SecurityException) {
            publish(HidStatus.PERMISSION_MISSING)
            false
        } catch (_: Exception) {
            false
        }

        if (!started) {
            publish(HidStatus.DISCONNECTED, issue = HidIssue.CONNECTION_FAILED)
            if (automatic) scheduleReconnect()
            return false
        }

        connectTimeoutJob?.cancel()
        connectTimeoutJob = scope.launch {
            delay(15_000L)
            if (_state.value.status == HidStatus.CONNECTING) {
                runCatching { hidProfile.disconnect(device) }
                publish(HidStatus.DISCONNECTED, issue = HidIssue.CONNECTION_FAILED)
                selectedDevice = null
                if (automatic) scheduleReconnect()
            }
        }
        return true
    }

    private fun maybeAutoConnect() {
        if (userDisconnected || !appRegistered) return
        if (autoConnectJob?.isActive == true) return
        if (_state.value.status != HidStatus.READY &&
            _state.value.status != HidStatus.DISCONNECTED
        ) {
            return
        }

        val host = lastHost ?: return
        val device = findDevice(host.address) ?: return
        if (safeBondState(device) != BluetoothDevice.BOND_BONDED) return
        if (connectedDevice() != null) return

        autoConnectJob = scope.launch {
            delay(700L)
            if (!userDisconnected && _state.value.status != HidStatus.CONNECTED) {
                val currentProfile = profile
                if (currentProfile != null && appRegistered) {
                    connectInternal(device, automatic = true)
                }
            }
        }
    }

    private fun scheduleReconnect() {
        if (userDisconnected || closing) return
        if (reconnectAttempts >= MAX_RECONNECT_ATTEMPTS) return
        if (reconnectJob?.isActive == true) return

        val host = lastHost ?: return
        reconnectAttempts += 1
        reconnectJob = scope.launch {
            delay(1_500L * reconnectAttempts)
            if (userDisconnected || _state.value.status == HidStatus.CONNECTED) return@launch

            val device = findDevice(host.address)
            if (device == null || safeBondState(device) != BluetoothDevice.BOND_BONDED) {
                return@launch
            }

            val started = connectInternal(device, automatic = true)
            if (!started) {
                // The previous job is finishing; use a clean slot for the next try.
                reconnectJob = null
                scheduleReconnect()
            }
        }
    }

    private fun handleWriteFailure() {
        if (connectedDevice() == null) {
            publish(HidStatus.DISCONNECTED, issue = HidIssue.CONNECTION_LOST)
            selectedDevice = null
            scheduleReconnect()
        }
    }

    private fun connectedDevice(): BluetoothDevice? = try {
        val connected = profile?.connectedDevices.orEmpty()
        connected.firstOrNull()
    } catch (_: SecurityException) {
        null
    } catch (_: Exception) {
        null
    }

    private fun rememberDevice(device: BluetoothDevice) {
        knownDevices[device.address] = device
    }

    private fun findDevice(address: String): BluetoothDevice? {
        knownDevices[address]?.let { return it }
        return try {
            adapter?.getRemoteDevice(address)?.also { knownDevices[address] = it }
        } catch (_: IllegalArgumentException) {
            null
        } catch (_: SecurityException) {
            null
        }
    }

    private fun refreshHosts() {
        val bluetoothAdapter = adapter
        if (bluetoothAdapter == null ||
            !BluetoothPermissions.hasConnectPermission(appContext)
        ) {
            _hosts.value = HostsState()
            return
        }

        val bonded = try {
            bluetoothAdapter.bondedDevices.orEmpty()
                .map { device ->
                    rememberDevice(device)
                    hostOf(device).copy(bonded = true)
                }
                .sortedWith(compareBy<HidHost>({ it.name?.lowercase() ?: "~" }, { it.address }))
        } catch (_: SecurityException) {
            emptyList()
        }

        val bondedAddresses = bonded.mapTo(HashSet()) { it.address }
        val nearby = discoveredHosts.values
            .filter { it.address !in bondedAddresses }
            .sortedWith(compareByDescending<HidHost> { it.rssi ?: Short.MIN_VALUE }
                .thenBy { it.name?.lowercase() ?: "~" })

        _hosts.value = HostsState(bonded = bonded, nearby = nearby)
    }

    private fun hostOf(device: BluetoothDevice, rssi: Short? = null): HidHost = HidHost(
        name = safeName(device),
        address = device.address,
        bonded = safeBondState(device) == BluetoothDevice.BOND_BONDED,
        rssi = rssi,
    )

    private fun safeName(device: BluetoothDevice): String? = try {
        device.name?.takeIf { it.isNotBlank() }
    } catch (_: SecurityException) {
        null
    }

    private fun safeBondState(device: BluetoothDevice): Int = try {
        device.bondState
    } catch (_: SecurityException) {
        BluetoothDevice.BOND_NONE
    }

    private fun persistHost(host: HidHost) {
        scope.launch {
            preferences.setLastHost(host.address, host.name)
        }
    }

    private fun publish(
        status: HidStatus,
        connectedHost: HidHost? = _state.value.connectedHost,
        issue: HidIssue? = null,
    ) {
        _state.value = HidState(
            status = status,
            connectedHost = if (status == HidStatus.CONNECTED) connectedHost else null,
            lastHost = lastHost,
            issue = issue,
        )
    }

    private companion object {
        const val MAX_RECONNECT_ATTEMPTS = 3
    }
}
