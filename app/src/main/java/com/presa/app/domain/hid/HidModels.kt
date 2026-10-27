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

/**
 * Composite keyboard + relative mouse report descriptor.
 *
 * The keyboard collection keeps the original eight-byte body (modifier,
 * reserved byte, six key slots) under report ID [HidReport.KEYBOARD_REPORT_ID].
 * The mouse collection adds a five-byte body (five buttons + padding, X, Y,
 * vertical wheel, horizontal pan) under report ID [HidReport.MOUSE_REPORT_ID].
 */
object HidDescriptor {
    const val SIZE = 134

    val BYTES: ByteArray = byteArrayOf(
        // Keyboard collection.
        0x05, 0x01, // Usage Page (Generic Desktop)
        0x09, 0x06, // Usage (Keyboard)
        0xA1.toByte(), 0x01, // Collection (Application)
        0x85.toByte(), HidReport.KEYBOARD_REPORT_ID.toByte(), // Report ID (1)
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
        0xC0.toByte(), // End Collection (keyboard)

        // Mouse collection.
        0x05, 0x01, // Usage Page (Generic Desktop)
        0x09, 0x02, // Usage (Mouse)
        0xA1.toByte(), 0x01, // Collection (Application)
        0x85.toByte(), HidReport.MOUSE_REPORT_ID.toByte(), // Report ID (2)
        0x09, 0x01, // Usage (Pointer)
        0xA1.toByte(), 0x00, // Collection (Physical)
        0x05, 0x09, // Usage Page (Button)
        0x19, 0x01, // Usage Minimum (1)
        0x29, 0x05, // Usage Maximum (5)
        0x15, 0x00, // Logical Minimum (0)
        0x25, 0x01, // Logical Maximum (1)
        0x75, 0x01, // Report Size (1)
        0x95.toByte(), 0x05, // Report Count (5)
        0x81.toByte(), 0x02, // Input (Data, Variable, Absolute) - five buttons
        0x75, 0x03, // Report Size (3)
        0x95.toByte(), 0x01, // Report Count (1)
        0x81.toByte(), 0x01, // Input (Constant) - button padding
        0x05, 0x01, // Usage Page (Generic Desktop)
        0x09, 0x30, // Usage (X)
        0x09, 0x31, // Usage (Y)
        0x09, 0x38, // Usage (Wheel)
        0x15, 0x81.toByte(), // Logical Minimum (-127)
        0x25, 0x7F, // Logical Maximum (127)
        0x75, 0x08, // Report Size (8)
        0x95.toByte(), 0x03, // Report Count (3)
        0x81.toByte(), 0x06, // Input (Data, Variable, Relative) - X, Y, wheel
        0x05, 0x0C, // Usage Page (Consumer)
        0x0A, 0x38, 0x02, // Usage (AC Pan)
        0x15, 0x81.toByte(), // Logical Minimum (-127)
        0x25, 0x7F, // Logical Maximum (127)
        0x75, 0x08, // Report Size (8)
        0x95.toByte(), 0x01, // Report Count (1)
        0x81.toByte(), 0x06, // Input (Data, Variable, Relative) - horizontal pan
        0xC0.toByte(), // End Collection (physical)
        0xC0.toByte(), // End Collection (mouse)
    )

    fun isWellFormed(): Boolean = BYTES.size == SIZE
}
