package com.presa.app.data

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

class Haptics(context: Context) {
    private val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        context.getSystemService(VibratorManager::class.java)?.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Vibrator::class.java)
    }

    fun press(enabled: Boolean) {
        if (!enabled) return
        val device = vibrator ?: return
        if (!device.hasVibrator()) return

        runCatching {
            if (device.hasAmplitudeControl()) {
                device.vibrate(VibrationEffect.createOneShot(18L, 150))
            } else {
                device.vibrate(
                    VibrationEffect.createOneShot(18L, VibrationEffect.DEFAULT_AMPLITUDE),
                )
            }
        }
    }

    /** Functional alert: always vibrates, even when button haptics are disabled. */
    fun timerAlert() {
        val device = vibrator ?: return
        if (!device.hasVibrator()) return

        val timings = longArrayOf(0L, 380L, 160L, 380L, 160L, 650L)
        runCatching {
            if (device.hasAmplitudeControl()) {
                val amplitudes = intArrayOf(0, 190, 0, 190, 0, 230)
                device.vibrate(VibrationEffect.createWaveform(timings, amplitudes, -1))
            } else {
                device.vibrate(VibrationEffect.createWaveform(timings, -1))
            }
        }
    }
}
