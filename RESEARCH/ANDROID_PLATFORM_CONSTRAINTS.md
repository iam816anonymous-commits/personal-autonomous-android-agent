# Android platform constraints

**Last reviewed:** 2026-11-02  
**Deployment target:** personal-use, sideloaded Android application. No Play Store publication requirement.

## 1. API 27 / Android 8.1 compatibility

API 27 is a desired legacy compatibility target, but it is not a guarantee of full feature parity. Use runtime capability detection and explicit feature degradation.

- Core app, local storage, text interaction and user-started tasks should be tested on API 27.
- AccessibilityService capability and action behavior must be verified on the actual device/OEM.
- Modern model runtimes, microphone service rules and APIs may require newer Android versions.
- Unsupported features must be disabled with a clear explanation.

## 2. Background execution

Android 8.0 introduced background execution limits. A service cannot be assumed to run indefinitely after the app is backgrounded. Design around:
- visible/user-initiated sessions where possible;
- foreground services with required notifications for valid ongoing work;
- WorkManager for deferrable work, not real-time continuous control;
- durable execution state and safe recovery after process death;
- explicit testing of OEM battery management, force-stop and reboot behavior.

Personal sideloading does not remove OS lifecycle restrictions.

## 3. Microphone and wake-word lifecycle

Separate:
1. User-initiated speech capture.
2. Optional wake-word detection.
3. Speech-to-text and agent task execution.

On modern Android versions, microphone foreground services have service-type and while-in-use permission constraints. For apps targeting Android 14/API 34+, a background attempt to create a microphone foreground service can fail with SecurityException unless an exemption applies. Do not assume an invisible background start will work.

For wake-word:
- Prefer on-device keyword spotting.
- Use ephemeral audio buffers and do not retain raw audio by default.
- Show visible listening status and provide one-tap disable/stop.
- Measure false accepts, battery and thermal impact.
- Test screen-off, Doze, app standby, process death, reboot and OEM power management.

## 4. Camera monitoring and background lifecycle

An optional home-security mode may keep the camera active while the device remains at home. This is subject to camera permission, foreground-service requirements, while-in-use restrictions, OEM process management and physical-device validation. On Android 14/API 34+ apps targeting the relevant SDK must declare the camera foreground-service type and permission; a camera service generally must be started while the app is visible because camera access is a while-in-use permission. Android 12+ also restricts background foreground-service starts. Do not assume the app can silently restart camera monitoring after process death or reboot. Keep a visible ongoing notification and explicit pause/stop controls.

Official references: [foreground-service restrictions](https://developer.android.com/develop/background-work/services/fgs/restrictions-bg-start), [foreground-service changes](https://developer.android.com/develop/background-work/services/fgs/changes).

## 5. AccessibilityService — primary automation candidate

Because this is a personal-use sideloaded app, we can investigate AccessibilityService as a central device interaction mechanism without designing around Google Play publication restrictions.

Technical design:
- User explicitly enables the service in Android Settings.
- Service exposes UI hierarchy, node text/metadata, bounds and supported actions.
- Agent uses fresh snapshots; node references are not assumed stable across screen transitions.
- Support click, long-click, scroll, text input and global navigation only where the platform and target UI permit.
- Re-observe after every action and verify the expected state transition.
- Provide an obvious enabled/disabled state and emergency stop.
- Avoid collecting or persisting sensitive UI content unnecessarily.

Important: AccessibilityService does not grant omnipotent control. Apps can hide semantics, secure windows can block capture, custom canvases may expose poor nodes, and Android/OEM behavior varies. It cannot legitimately bypass authentication or app sandboxing.

## 6. Complementary control mechanisms

- Explicit Android intents and app links for deterministic navigation.
- App-specific APIs/deep links where supported.
- Accessibility semantic tree for UI grounding and interaction.
- Screenshot/visual observation where permitted and user-consented.
- MediaProjection for user-approved screen capture; not a universal silent screenshot API.
- Notification actions and widgets where supported.
- ADB/Shizuku/root as optional advanced personal profiles only, with explicit setup and threat model. Not baseline requirements.

## 7. Device controls and boundaries

Do not assume a normal app can:
- toggle arbitrary secure settings;
- read private app databases;
- bypass lock screens/authentication;
- silently send messages or make payments without appropriate user authorization;
- execute privileged shell commands;
- control work profiles or managed devices without the required privileges.

## 8. Storage and process resilience

Persist task ID, goal, plan version/current step, approval decisions, action intent/idempotency key, before/after state summary, retry/timeout counts and outcome. Do not persist screenshots/audio by default. Retention must be configurable.

## 9. Sources

- Background execution: https://developer.android.com/about/versions/oreo/background
- Foreground services: https://developer.android.com/develop/background-work/services/foreground-services
- API 34 foreground-service types: https://developer.android.com/about/versions/14/changes/fgs-types-required
- Background starts/while-in-use permissions: https://developer.android.com/develop/background-work/services/fgs/restrictions-bg-start
- AccessibilityService API: https://developer.android.com/reference/android/accessibilityservice/AccessibilityService
- Accessibility guide: https://developer.android.com/guide/topics/ui/accessibility/service
- Runtime permissions: https://developer.android.com/training/permissions/requesting

## 10. Required proof-of-concepts

- API 27 physical-device install and lifecycle.
- AccessibilityService enable/disable, tree retrieval, actions, stale-node behavior and secure-window limitations.
- Voice capture and cancellation on API 27.
- Modern Android microphone foreground-service behavior on API 34+.
- Background, screen lock, process kill, reboot, low memory and OEM battery modes.