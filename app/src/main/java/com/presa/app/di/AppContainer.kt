package com.presa.app.di

import android.content.Context
import com.presa.app.data.Haptics
import com.presa.app.data.PresaPreferences
import com.presa.app.data.VolumeKeyRouter
import com.presa.app.domain.hid.BluetoothHidManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

class AppContainer(context: Context) {
    val appContext: Context = context.applicationContext

    val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    val preferences = PresaPreferences(appContext)
    val haptics = Haptics(appContext)
    val volumeKeys = VolumeKeyRouter()
    val hidManager = BluetoothHidManager(appContext, preferences, applicationScope)
}
