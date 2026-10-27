package com.presa.app.ui.connection

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.presa.app.di.AppContainer
import com.presa.app.domain.hid.BluetoothHidManager
import com.presa.app.domain.hid.HidHost
import com.presa.app.domain.hid.HostsState
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ConnectionViewModel(
    private val hidManager: BluetoothHidManager,
) : ViewModel() {
    val state = hidManager.state
    val hosts: StateFlow<HostsState> = hidManager.hosts
    val isDiscovering = hidManager.isDiscovering

    fun refresh() = hidManager.refresh()

    fun connect(host: HidHost) = hidManager.connect(host)

    fun disconnect() = hidManager.disconnect()

    fun reconnect() = hidManager.reconnect()

    fun pair(address: String): Boolean = hidManager.pair(address)

    fun startDiscovery(): Boolean = hidManager.startDiscovery()

    fun stopDiscovery() = hidManager.stopDiscovery()

    fun onPermissionsChanged() {
        viewModelScope.launch { hidManager.onPermissionsChanged() }
    }

    companion object {
        fun factory(container: AppContainer) = viewModelFactory {
            initializer { ConnectionViewModel(container.hidManager) }
        }
    }
}
