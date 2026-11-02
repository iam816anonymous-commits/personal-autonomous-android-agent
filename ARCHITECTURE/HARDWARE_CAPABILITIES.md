# Hardware capabilities and device-control architecture

**Status:** Proposed dedicated subsystem; not yet implemented or physically validated.  
**Target:** Personal sideloaded Android agent, API 27 desired baseline with capability-based degradation.

## 1. What this subsystem does

The agent must both **discover** and **operate** supported device hardware.

- Discovery: enumerate hardware/features, inspect API support, permission state, runtime availability and current state.
- Control: expose typed, permission-checked operations through Android APIs.
- Verification: read back state or use platform callbacks; report unsupported, unavailable, denied and uncertain states honestly.
- Safety: model output never calls hardware APIs directly. Every operation passes deterministic capability and risk validation.

A device possessing a sensor or radio does not imply that an ordinary app can freely control it. Android permissions, user consent, OS version, hardware vendor implementation and app sandbox rules remain authoritative.

## 2. Capability domains

| Domain | Detect / observe | Potential agent actions | Important limits |
|---|---|---|---|
| Flash / torch | Camera IDs, flash availability, torch state | Turn torch on/off; strength only on supported newer APIs | Camera contention, camera permission/device behavior; strength API is newer than API 27 |
| Camera | Front/back cameras, supported output sizes/facing/capabilities | User-approved photo/video capture, camera preview | CAMERA permission, privacy indicator/OS restrictions, lifecycle and camera contention |
| Microphone / audio | Input/output devices, audio route, recognition availability | User-started speech capture, play notification tones, adjust supported audio stream | Microphone permission and foreground-service rules; do not silently record |
| Vibration / haptics | Vibrator presence and supported effect capability where API allows | Short vibration/haptic feedback | Newer effect capability APIs unavailable on API 27; fallback behavior must be reported |
| Sensors | Accelerometer, gyroscope, proximity, light, magnetic field, pressure, step sensors when present | Read sensor data for explicit tasks; infer orientation/motion with confidence | Runtime availability, sampling/battery limits, privacy; background sensor restrictions vary |
| Location / GNSS | Location providers, permission and provider state | User-authorized current location or navigation handoff | Location permission, user location toggle, background limits; no covert tracking |
| Bluetooth / BLE | Adapter, state, bonded/connected devices where permitted, supported BLE | User-approved discovery, pairing flow, connection to known accessories | API 31+ permission model differs; pairing and connection require user/system consent |
| Wi-Fi / network | Connectivity state, active network capabilities | Open settings, use supported network APIs, network diagnostics | Ordinary apps cannot freely toggle Wi-Fi on modern Android; don't promise arbitrary radio control |
| Telephony / SIM | Telephony feature, subscription and service state where permitted | Open dialer, initiate user-confirmed call flow | Sensitive permissions; no silent calls/SMS or privileged telephony control |
| Display / brightness | Display metrics, brightness mode/state where accessible | Open display settings; adjust only where permitted | WRITE_SETTINGS special access and OS/OEM limits |
| Power / battery / thermal | Battery level/status, power-save state, thermal status where available | Adapt model/task load, reduce work, advise user | App cannot override thermal/power protections |
| Storage / memory / CPU | Storage, available memory, ABI, runtime process memory, CPU info where exposed | Select model, unload models, defer tasks | Do not equate advertised dynamic RAM with physical memory or guarantee other-process memory |
| USB / external peripherals | USB host support, attached devices and interfaces | User-approved USB accessory communication | Device must support host mode; temporary per-device permission |
| NFC | NFC feature and adapter state | User-initiated tag reading / supported interactions | NFC availability, foreground dispatch and OS/device constraints |
| Biometrics / secure hardware | Biometric feature availability through supported APIs | Request system-mediated authentication | Never capture biometric data or bypass system authentication |
| Flashlight / torch | Included in flash domain | Quick on/off tool | Must verify torch callback/state; camera may make it unavailable |

This is a capability inventory, not a promise that every phone exposes every function.

## 3. Architecture

Add a dedicated `hardware` / `device-capabilities` layer with platform adapters.

```text
Agent Orchestrator
       |
Typed Hardware Tools
       |
Hardware Policy + Permission Gate
       |
Hardware Capability Registry
       |
Android Hardware Adapters
 | Camera | Audio | Sensors | Radio | Location |
 | Haptics | Power | Storage | USB | NFC       |
       |
Platform callbacks / state read-back
       |
Hardware State Verifier
       |
Execution Journal + User-visible result
```

Suggested contracts:

```kotlin
data class HardwareCapability(
    val id: String,
    val present: Boolean,
    val supported: Boolean,
    val permissionState: PermissionState,
    val availability: Availability,
    val supportedOperations: Set<String>,
    val limitations: List<String>
)

interface HardwareAdapter {
    suspend fun discover(): List<HardwareCapability>
    suspend fun execute(command: HardwareCommand): HardwareResult
    suspend fun observe(capabilityId: String): HardwareState
}
```

The actual implementation should use typed sealed classes for commands/results, not arbitrary operation strings. The above is illustrative.

Every command must include task ID, timeout, cancellation, required permission, risk classification, preconditions and verifiable expected state.

## 4. Implementation steps

1. Create a hardware capability registry independent of the model.
2. Implement a baseline device inventory: API level, ABI, camera/flash, microphone, vibrator, sensors, Bluetooth, Wi-Fi/network, location, NFC, USB, battery/thermal, storage and memory.
3. Separate hardware presence from OS API support, permission grant, runtime availability and current state.
4. Implement low-risk adapters first: torch, vibration, audio notification, sensor read-only queries and battery/thermal observation.
5. Add camera/microphone/location only with explicit user-triggered permission and visible session state.
6. Add Bluetooth, Wi-Fi, NFC, USB and telephony as separate adapters with their own permission and user-consent workflows.
7. Add hardware actions to the same typed tool registry as app/UI actions.
8. Re-check permissions and capability availability immediately before each operation.
9. Verify effects through callbacks or read-back; handle contention, detach, permission revocation and unsupported operations.
10. Add hardware test screen and deterministic diagnostics that show discovered, unavailable, denied and tested capabilities.
11. Profile battery, thermal, memory and sensor sampling overhead.
12. Add physical-device test evidence for every supported device/OS combination.

## 5. Reuse from the user's repositories

Audit these candidates; do not assume they are reusable until source and tests are verified:

- `agent-created-by-gemini`: inspect `HardwareManager.kt`, `DeviceCapabilityDetector.kt`, `ActuatorController.kt`, model contracts and tests. These are first candidates for direct reuse/adaptation because they already address hardware discovery and actuation. Earlier audit work recorded a fix to replace hardcoded storage fallback values with unknown values; preserve that behavior.
- `skill-agent`: reuse capability authorization, revocation and resource-management patterns if compatible with the selected hardware command contracts.
- `mobile_agent`: inspect device profiling and tools modules for existing hardware adapters; reuse only non-duplicated, tested implementations.
- `tecno-os`: hardware inventory can inform the TECNO IN6 profile, but Linux kernel and privileged userspace code are not Android app adapters.

Before copying, verify exact paths, current branch SHA, licence, Android API calls, permission checks, lifecycle handling and test results.

## 6. Android API and version strategy

- API 27 is the baseline compatibility test, not a guarantee that newer hardware APIs exist.
- Guard newer calls with SDK checks and use older supported APIs only where their behavior is safe.
- Build one common capability contract and multiple API-specific implementations where required.
- Example: `CameraManager.setTorchMode` exists from API 23, so torch control is possible in principle on API 27 devices with flash hardware. Torch can become unavailable if camera resources are busy.
- USB host APIs exist from API 12, but only if the hardware supports USB host mode and the user grants access to the attached device.
- Android 12+ Bluetooth permissions differ from older Android permission models; implement version-specific permission flows.
- Newer vibrator effect introspection APIs are not available on API 27; use safe basic vibration behavior there.
- Never promise Wi-Fi, mobile data, GPS, airplane mode, Bluetooth or secure settings toggles unless the specific public API, OS version and required user access permit it. In many cases the correct action is opening the relevant Settings panel for the user.

## 7. Test matrix

For each physical device, record model, build fingerprint, Android/API, ABI and OEM skin. Test:

- Discovery accuracy against known hardware and Android feature declarations.
- Permission denied, granted, revoked during task and permanently denied.
- Hardware absent, busy, disconnected and unavailable.
- User stop/cancel during an operation.
- Process death and app restart.
- Battery saver, thermal pressure and low memory.
- API 27 legacy path and modern Android path separately.
- Actual effect verified independently; do not mark success from method return alone.

## 8. Acceptance criteria

- [ ] Capability registry distinguishes absent, unsupported, permission-denied, temporarily-unavailable and available.
- [ ] No hardware operation is invoked directly by model-generated code.
- [ ] Every operation checks permission and availability immediately before execution.
- [ ] Every mutating operation has a bounded timeout and cancellation path.
- [ ] Every claimed success has callback/read-back or another meaningful verification.
- [ ] Unsupported actions explain the limitation and offer a safe Settings handoff where possible.
- [ ] API 27 and modern-device behavior are recorded separately.
- [ ] Battery, thermal and privacy impacts are tested.
- [ ] Hardware capabilities can be disabled individually by the user.

## 9. Official Android references

- CameraManager / torch: https://developer.android.com/reference/android/hardware/camera2/CameraManager
- Camera2: https://developer.android.com/reference/android/hardware/camera2/package-summary
- Vibrator: https://developer.android.com/reference/android/os/Vibrator
- Sensors overview: https://developer.android.com/develop/sensors-and-location/sensors/sensors_overview
- Bluetooth permissions: https://developer.android.com/develop/connectivity/bluetooth/bt-permissions
- USB host: https://developer.android.com/develop/connectivity/usb/host
- UsbManager: https://developer.android.com/reference/android/hardware/usb/UsbManager
- Location permissions: https://developer.android.com/develop/sensors-and-location/location/permissions
- Audio capture: https://developer.android.com/media/platform/mediarecorder
