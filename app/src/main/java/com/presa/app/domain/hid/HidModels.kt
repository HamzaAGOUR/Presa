package com.presa.app.domain.hid

import com.presa.app.domain.model.KeyChord

data class HidHost(
    val name: String?,
    val address: String,
    val bonded: Boolean = true,
    val rssi: Short? = null,
)

enum class HidStatus {
    UNSUPPORTED,
    BLUETOOTH_OFF,
    PERMISSION_MISSING,
    REGISTERING,
    READY,
    CONNECTING,
    CONNECTED,
    DISCONNECTED,
}

enum class HidIssue {
    PROFILE_UNAVAILABLE,
    REGISTER_FAILED,
    CONNECTION_FAILED,
    NOT_BONDED,
    CONNECTION_LOST,
    SEND_FAILED,
}

data class HidState(
    val status: HidStatus = HidStatus.REGISTERING,
    val connectedHost: HidHost? = null,
    val lastHost: HidHost? = null,
    val issue: HidIssue? = null,
) {
    val isConnected: Boolean get() = status == HidStatus.CONNECTED
}

data class HostsState(
    val bonded: List<HidHost> = emptyList(),
    val nearby: List<HidHost> = emptyList(),
)

data class HidSendResult(
    val delivered: Boolean,
    val reason: HidSendFailure? = null,
)

enum class HidSendFailure {
    NOT_CONNECTED,
    PERMISSION,
    WRITE_FAILED,
}

/** Planned keyboard report descriptor: one modifier byte + six key slots = 8 bytes. */
object KeyboardDescriptor {
    val BYTES: ByteArray = byteArrayOf(
        0x05, 0x01, // Usage Page (Generic Desktop)
        0x09, 0x06, // Usage (Keyboard)
        0xA1.toByte(), 0x01, // Collection (Application)
        0x05, 0x07, // Usage Page (Keyboard/Keypad)
        0x19, 0xE0.toByte(), // Usage Minimum (Left Control)
        0x29, 0xE7.toByte(), // Usage Maximum (Right GUI)
        0x15, 0x00, // Logical Minimum (0)
        0x25, 0x01, // Logical Maximum (1)
        0x75, 0x01, // Report Size (1)
        0x95.toByte(), 0x08, // Report Count (8)
        0x81.toByte(), 0x02, // Input (Data, Variable, Absolute) - modifier byte
        0x95.toByte(), 0x01, // Report Count (1)
        0x75, 0x08, // Report Size (8)
        0x81.toByte(), 0x01, // Input (Constant) - reserved byte
        0x95.toByte(), 0x06, // Report Count (6)
        0x75, 0x08, // Report Size (8)
        0x15, 0x00, // Logical Minimum (0)
        0x25, 0x65, // Logical Maximum (101)
        0x05, 0x07, // Usage Page (Keyboard/Keypad)
        0x19, 0x00, // Usage Minimum (0)
        0x29, 0x65, // Usage Maximum (101)
        0x81.toByte(), 0x00, // Input (Data, Array) - six key slots
        0x95.toByte(), 0x05, // Report Count (5)
        0x75, 0x01, // Report Size (1)
        0x05, 0x08, // Usage Page (LEDs)
        0x19, 0x01, // Usage Minimum (Num Lock)
        0x29, 0x05, // Usage Maximum (Kana)
        0x91.toByte(), 0x02, // Output (Data, Variable, Absolute)
        0x95.toByte(), 0x01, // Report Count (1)
        0x75, 0x03, // Report Size (3)
        0x91.toByte(), 0x01, // Output (Constant) - LED padding
        0xC0.toByte(), // End Collection
    )

    fun isWellFormedKeyboardDescriptor(): Boolean = BYTES.size == 63
}
