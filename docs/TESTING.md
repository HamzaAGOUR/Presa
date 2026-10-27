# Presa — manual test checklist

Use this checklist before publishing an APK. Unit tests already cover the key maps, keyboard/mouse report bytes, composite descriptor, remap codec, and timer formatting; the items below validate the Bluetooth stack and host applications, which cannot be tested on the JVM.

## 1. Test run header

Record the setup for every run:

| Field | Value |
| --- | --- |
| Date | |
| Tester | |
| App version / commit | |
| Phone model | |
| Android version | |
| OEM / ROM build | |
| HID Device profile available | Yes / No |
| Host 1 (OS + version) | |
| Host 2 (OS + version) | |
| Browser / Office versions | |
| Theme / language / font scale | |

Recommended device coverage:

- [ ] Android 12+ phone with stock or known HID-capable firmware
- [ ] Android 9–11 phone (legacy permission flow)
- [ ] Windows 10 or 11
- [ ] macOS (Ventura or newer)
- [ ] Linux with BlueZ (GNOME or KDE)
- [ ] Small screen (≈ 360 × 640 dp), large phone, and tablet/landscape
- [ ] Font scale 1.0 and 1.3 (and 2.0 if time permits)
- [ ] Light, dark, and system theme
- [ ] French and English

## 2. Install and first launch

- [ ] `./gradlew testDebugUnitTest assembleDebug` succeeds; 46 tests pass.
- [ ] APK installs with `adb install -r app/build/outputs/apk/debug/app-debug.apk`.
- [ ] Launcher shows **Presa** with the adaptive icon in the theme primary color.
- [ ] Cold start shows the permission gate on Android 12+ if Nearby devices has not been granted.
- [ ] Granting the permission opens the remote (or connection) screen without a restart loop.
- [ ] Denying the permission shows the explanatory state and the **Open app settings** action.
- [ ] On Android 9–11, bonded devices are usable after the legacy Bluetooth grant; discovery requests location only when needed.
- [ ] With Bluetooth off, the UI shows **Bluetooth désactivé** and offers a way to open Bluetooth settings.
- [ ] On a phone without the HID Device profile, the UI shows **Profil HID indisponible** and does not crash.
- [ ] Reopening the app does not duplicate the HID registration or leave a stale keyboard entry.

## 3. Pairing and connection

### Windows 10/11

- [ ] Presa is open and advertises **Presa Keyboard**.
- [ ] **Settings → Bluetooth & devices → Add device → Bluetooth** finds the phone.
- [ ] The pairing code (if shown) can be confirmed on both devices.
- [ ] Windows lists the device as a keyboard and a mouse/pointer.
- [ ] Selecting it in Presa connects; the in-app status bar shows **Connecté à …**.
- [ ] Keys arrive in Notepad: left/right arrows, space, letters `B`, `W`, `D`, `P`, `R`, `E`, `Home`, `End`, `F5`.
- [ ] The trackpad moves the Windows pointer and clicks work (see section 5).
- [ ] Removing the device in Windows and re-pairing succeeds.
- [ ] Upgrading from a keyboard-only build: after removing the old entry and re-pairing, both keys and pointer work.
- [ ] A stale **Presa Keyboard** entry can be removed and the device paired again.

### macOS

- [ ] **System Settings → Bluetooth** finds **Presa Keyboard**.
- [ ] Pairing completes and macOS exposes it as a keyboard and pointer.
- [ ] Keyboard Setup Assistant, if it appears, can be closed without affecting keys.
- [ ] Left/right arrows, Space, `Home`, `End`, `F5`, and `Esc` arrive in TextEdit.
- [ ] The trackpad moves the macOS pointer and clicks work (see section 5).
- [ ] Unpairing on macOS and re-pairing succeeds.

### Linux (BlueZ)

- [ ] `bluetoothctl scan on` finds the phone while Presa is open.
- [ ] `pair`, `trust`, and `connect` complete.
- [ ] `bluetoothctl info <MAC>` reports `Connected: yes` and the HID service.
- [ ] Keys arrive in a terminal or text editor.
- [ ] The trackpad moves the pointer; check `libinput debug-events` if needed (see section 5).
- [ ] After a host reboot (or Bluetooth restart), Presa reconnects or the host reconnects to the trusted device.

### Status and errors

- [ ] Status bar accurately reflects disconnected / connecting / connected / reconnecting.
- [ ] Turning Bluetooth off shows the Bluetooth-off state; turning it back on allows reconnect.
- [ ] Denying the Android 12+ permission then granting it through app settings updates the state on `onResume`.
- [ ] Tapping **Connect** on a non-bonded device requests pairing rather than failing silently.
- [ ] Failing to connect shows a message and leaves the UI usable.
- [ ] Explicitly disconnecting does **not** trigger auto-reconnect.
- [ ] Losing the host mid-session triggers up to three reconnect attempts; **Reconnect** is available afterwards.
- [ ] Closing and reopening the app returns to the last host and last mode.

## 4. Chrome visualizer

Open the DSA Visualizer (deployed URL or local dev server) in Chrome. Click the page once so it has keyboard focus.

- [ ] **Previous / Next** (`←` / `→`) move backward/forward.
- [ ] Holding **Previous** repeats after the configured initial delay and at the configured interval.
- [ ] Holding **Next** repeats the same way; releasing stops immediately.
- [ ] **Play / pause** (`Space`) toggles playback.
- [ ] **Start** (`Home`) returns to the start.
- [ ] **End** (`End`) jumps to the end.
- [ ] **Slow down** (`↓`) and **Speed up** (`↑`) change animation speed.
- [ ] **Zoom in** (`Shift` + `=`) zooms in repeatedly without a stuck Shift modifier.
- [ ] **Zoom out** (`-`) zooms out.
- [ ] **Fit** (`0`) restores the fitted view.
- [ ] **Bottom panels** (`D`) toggles the panels.
- [ ] **Presentation mode** (`P`) enters presentation mode.
- [ ] **Reset** opens the confirmation dialog; **Cancel** sends nothing; **Reset** sends `R` once.
- [ ] **Main menu** (`Esc`) leaves presentation mode or returns to the menu.
- [ ] After 20 consecutive zoom-in actions, typing `B` in a scratch field produces `b`, not `B` (no stuck Shift).
- [ ] Switching to Dark and Light themes does not affect key delivery.
- [ ] With **Volume keys** enabled, Volume Up/Down repeat the same Previous/Next behavior while the remote screen is visible.

## 5. Trackpad

Connect to one of the hosts above. The **Pavé tactile / Trackpad** section sits below the key controls (scroll the remote screen to reach it) and drives the host pointer through the HID mouse collection.

- [ ] While disconnected, the surface is disabled and shows **Connect a computer to enable the trackpad** / **Connectez un ordinateur…**.
- [ ] After connecting, the surface and both click buttons become active.
- [ ] One-finger drag moves the pointer in the same direction as the finger, at a usable speed.
- [ ] Fast full-width swipes stay smooth (about one report per 16 ms) and do not freeze the UI.
- [ ] A single-finger tap left-clicks a desktop icon or file.
- [ ] A two-finger tap opens the context menu (right-click).
- [ ] Two-finger vertical drag scrolls; dragging up scrolls down (natural direction).
- [ ] Two-finger horizontal drag pans in an app that supports it; record any host app that ignores horizontal pan.
- [ ] The **Left click** and **Right click** buttons click without moving the pointer.
- [ ] Rapid taps and swipes never leave a mouse button held on the host.
- [ ] Press-and-drag selection is not supported (tap-to-click only); confirm this is documented, not treated as a bug.
- [ ] Holding Previous/Next while using the trackpad does not interleave or drop reports.
- [ ] Disconnecting disables the trackpad; reconnecting restores it and the first gesture works.
- [ ] Haptics fire on trackpad taps when enabled and stay silent when disabled.
- [ ] TalkBack reads the trackpad description and exposes the left/right click custom actions.
- [ ] French and English trackpad labels and hints are both correct.
- [ ] On a 360 × 640 dp screen and at 1.3× font scale, the surface and buttons remain reachable by scrolling.
- [ ] After 15 minutes of mixed swipes and taps, the host still responds without requiring a reconnect.

## 6. PowerPoint

Create a 5-slide deck with a text box and a shape for drawing tests.

- [ ] **Start slideshow** (`F5`) starts from slide 1.
- [ ] **From current slide** (`Shift` + `F5`) starts at the current slide.
- [ ] **Previous / Next** move one slide; hold repeats correctly.
- [ ] **First** (`Home`) goes to slide 1; **Last** (`End`) goes to the last slide.
- [ ] **Black screen** (`B`) and **White screen** (`W`) toggle as expected; the same keys restore the slide.
- [ ] **Laser pointer** (`Ctrl` + `L`) enables the laser; moving the mouse on the host shows it (Presa itself only sends the shortcut).
- [ ] **Pen** (`Ctrl` + `P`) enables the pen; draw with the host mouse to confirm.
- [ ] **Eraser** (`E`) erases annotations.
- [ ] **Exit** (`Esc`) ends the slideshow.
- [ ] Number pad `3` then **Send** jumps to slide 3 during the slideshow.
- [ ] Number pad with a leading zero (`03`) still reaches slide 3.
- [ ] Number pad with 5 digits stops at 4 digits and shows the limit message.
- [ ] **Clear** and backspace behave correctly; sending an empty value does nothing.
- [ ] After leaving the slideshow, `Esc` does not leave PowerPoint in an inconsistent state.
- [ ] Focus PowerPoint before pressing keys; verify that a focused text editor receives the same characters when the app is in edit mode (document this, do not treat as a bug).

## 7. Chrome PDF viewer

Open a multi-page PDF in Chrome's built-in viewer.

- [ ] **Previous page** (`Page Up`) moves toward the previous page.
- [ ] **Next page** (`Page Down`) moves toward the next page.
- [ ] **Scroll up/down** (`↑` / `↓`) scrolls within the document.
- [ ] **First** (`Home`) reaches the start of the document.
- [ ] **Last** (`End`) reaches the end of the document.
- [ ] **Fullscreen** (`F11`) toggles Chrome fullscreen.
- [ ] **Zoom in** (`Ctrl` + `Shift` + `=`) zooms in.
- [ ] **Zoom out** (`Ctrl` + `-`) zooms out.
- [ ] **Fit** (`Ctrl` + `0`) resets the zoom to fit.
- [ ] **Exit** (`Esc`) leaves fullscreen.
- [ ] Hold-to-repeat works for Page Up/Page Down without overshooting more than expected.
- [ ] Number pad digits + Enter are sent (verify in a text field), but do **not** change the page in Chrome's PDF viewer. Record this as a known host limitation; the in-app “Page X envoyée” message only confirms the keys were sent.

## 8. Acrobat PDF

Open a multi-page PDF in Adobe Acrobat (desktop).

- [ ] **Previous / Next page** (`Page Up` / `Page Down`) change pages.
- [ ] **First / Last** (`Home` / `End`) go to the first/last page.
- [ ] **Scroll up/down** works in continuous mode.
- [ ] **Fullscreen (alt.)** (`Ctrl` + `L`) toggles fullscreen.
- [ ] **Zoom in/out/fit** behave as labelled.
- [ ] **Exit** (`Esc`) leaves fullscreen.
- [ ] Number pad `4` + Enter jumps to page 4.
- [ ] Number pad works when the page field has focus and when it does not (document any focus requirement).
- [ ] Holding Previous/Next repeats at the configured speed.

## 9. Timer

- [ ] Timer starts, pauses, and resumes without losing elapsed time.
- [ ] Reset returns to 00:00 and stops the timer.
- [ ] Navigating to Connection, Settings, and Remap keeps the timer counting.
- [ ] The main-screen timer display updates without resetting the remote state.
- [ ] Setting the alert to 5 minutes fires a vibration and dialog at 00:05:00 (allow a few seconds of ticker tolerance).
- [ ] Dismissing the dialog stops the alert but leaves the timer in its elapsed state.
- [ ] Changing the alert minutes while the timer runs recalculates the threshold sensibly.
- [ ] Killing the app process resets the timer (expected; document it).
- [ ] With haptics disabled, key presses do not vibrate; confirm whether the timer alert still vibrates (it is a separate safety alert) and document the behavior.

## 10. Settings, theme, language, and remapping

- [ ] Theme switches immediately among System / Light / Dark; System follows the OS setting.
- [ ] Both themes meet readability expectations (status colors, disabled states, dialogs, snackbars).
- [ ] Language switches instantly between French and English; no screen keeps stale strings.
- [ ] French is the default on a clean install, even on an English system.
- [ ] Haptics toggle affects key presses and settings interactions.
- [ ] Repeat speed changes take effect on the next hold.
- [ ] Volume-key mapping changes take effect without restarting.
- [ ] Timer minutes step by 5 and clamp to 5–120.
- [ ] Remap screen lists all actions for Visualizer, PowerPoint, and PDF.
- [ ] Changing one action sends the new chord and shows the “Personnalisée” badge.
- [ ] The same action ID is not accidentally remapped in another mode.
- [ ] **Reset this key** restores that action's default.
- [ ] **Reset all keys** asks for confirmation and clears every override.
- [ ] Remaps survive an app restart.
- [ ] A remap to a modifier-only or unusual combination is handled without crashing.
- [ ] All remapped actions are still delivered after reconnecting to the host.
- [ ] Remap UI is usable with TalkBack content descriptions and at 1.3× font scale.

## 11. Layout, accessibility, and lifecycle

- [ ] Main screen fits a 360 × 640 dp viewport; content scrolls rather than clipping.
- [ ] On a tablet or landscape, spacing remains intentional and no button is unreachable.
- [ ] Status bar stays compact and readable in both themes.
- [ ] All icon-only buttons have content descriptions.
- [ ] Touch targets are at least 48 dp.
- [ ] Screen does not sleep while the remote is open.
- [ ] Backgrounding the app and returning restores connection status, mode, sub-mode, and timer.
- [ ] System back from Connection/Settings/Remap returns to the expected screen.
- [ ] Rotating the device does not lose the current screen, unsent number, or mode.
- [ ] Snackbars appear above the content and do not block controls.
- [ ] No crash or ANR after 15 minutes of repeated key presses.

## 12. Error handling matrix

| Scenario | Expected behavior |
| --- | --- |
| Bluetooth off | Status shows off; no crash; settings shortcut works. |
| Nearby devices denied | Permission gate explains why; app settings shortcut. |
| HID profile missing | Dedicated unsupported state, no connection attempts. |
| Registration fails | Error message; retry available. |
| Host not bonded | Pairing request or clear “must pair first” message. |
| Send while disconnected | “Connect first” message, throttled. |
| Host disappears mid-press | Repeat loop stops; reconnect attempts start. |
| HID write failure | “Command could not be sent” message, no stuck key. |
| Trackpad gesture while disconnected | Surface is disabled; no commands queued; no crash. |
| Mouse report write failure | Message is throttled; no stuck mouse button; trackpad remains usable after reconnect. |
| Rapid mixed moves and clicks | Reports are serialized; clicks stay atomic; no interleaved press/release. |
| Rapid pressing / holding | Reports are serialized; no interleaved key-down/key-up. |

## 13. Sign-off

- [ ] Unit tests pass (`46/46` at the time of writing).
- [ ] All critical items above pass on at least one phone and one host.
- [ ] Known host-specific limitations are recorded in the README.
- [ ] Real screenshots replace the placeholders in `docs/screenshots/`.
- [ ] Version code/name bumped if this is a release candidate.

### Known issues to record

| ID | Device / host | Description | Workaround | Status |
| --- | --- | --- | --- | --- |
| | | | | |
| | | | | |
