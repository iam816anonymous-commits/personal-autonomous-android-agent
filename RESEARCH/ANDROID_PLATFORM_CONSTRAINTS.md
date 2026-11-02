# Android platform constraints

**Last reviewed:** 2026-11-02  
**Primary evidence:** Android Developers documentation. Re-check these rules against the selected target SDK before each release.

## 1. API 27 / Android 8.1 compatibility

API 27 is a legacy compatibility requirement, not permission to design the whole product around the lowest API. The app should use capability detection and explicit feature degradation.

Proposed support tiers (hypotheses until device tests):
- **Tier A – core baseline:** app installation, user-started sessions, foreground UI, local storage, text command input, basic explicit user-triggered actions.
- **Tier B – supported device automation:** only APIs and interaction methods available and permitted on the installed OS, OEM and distribution channel.
- **Tier C – modern enhancements:** newer foreground-service types, newer system APIs, modern inference accelerators and APIs requiring newer Android versions.
- Features unsupported on a device must be hidden/disabled with an explanation, not exposed as controls that fail later.

## 2. Background execution

Android 8.0 introduced background execution limits. A service cannot be assumed to run indefinitely after its app becomes backgrounded. Design the runtime around:
- visible/user-initiated sessions where possible;
- foreground services only for valid, user-visible use cases and with required notifications;
- WorkManager for deferrable work, not continuous real-time control;
- persisted execution state and idempotent recovery after process death;
- explicit handling of OEM battery management and force-stop.

Do not promise an always-on agent across all OEMs and Android versions.

## 3. Microphone and wake-word lifecycle

A voice command system has distinct modes:
1. User taps a microphone/voice control and grants runtime permission.
2. A user-visible voice session is active under the applicable platform rules.
3. Optional wake-word listening is separately enabled, explained, and tested for the device/OS/distribution route.

On modern Android versions, microphone foreground services have service-type and while-in-use permission constraints. For apps targeting Android 14/API 34+, a background attempt to create a microphone foreground service can fail with SecurityException unless a documented exemption applies. A notification or visible activity can be part of a valid start path; an app must not rely on launching such a service invisibly from the background.

The wake-word feature must not silently record, upload, or retain raw audio. Prefer on-device keyword spotting, ephemeral audio buffers, visible status and a one-tap kill switch. These are product requirements, not claims that a particular wake-word implementation is already feasible on API 27.

## 4. AccessibilityService

Technically, AccessibilityService can expose UI semantics and support gestures/actions on devices where enabled. That does not mean unrestricted autonomous control is permitted for every distribution route.

- Use only narrow, documented actions with clear purpose and user control.
- Do not misrepresent the app as an accessibility tool if it is not primarily intended to assist people with disabilities.
- Google Play policy has a specific restriction against apps using AccessibilityService to autonomously initiate, plan, and execute actions or decisions.
- Treat sideloaded/personal use and Play distribution as separate product profiles, but do not interpret sideloading as removing Android security, consent, or privacy obligations.
- Where AccessibilityService is unsuitable, evaluate supported intents, app-specific integrations, user-confirmed flows, notification actions, MediaProjection where appropriate, and other Android APIs. Each has separate permission and policy limitations.

## 5. Device controls and privileged operations

Do not assume a normal app can:
- toggle arbitrary secure settings;
- read private data from other apps;
- bypass lock screens or authentication;
- silently send messages or make payments;
- execute privileged shell commands;
- act across work profiles or managed devices.

Root, Shizuku, ADB, device-owner and OEM-specific modes are optional deployment profiles, not baseline requirements. Each needs a threat model, explicit setup and capability disclosure.

## 6. Storage and process resilience

Use a durable local execution journal. Persist:
- task ID and user goal;
- plan version and current step;
- permission/approval decisions;
- action intent and idempotency key;
- observed before/after state summary;
- retry and timeout counts;
- completion or failure reason.

Do not persist screenshots or audio by default. Retention must be configurable and understandable.

## 7. Source references

- Android background execution limits: https://developer.android.com/about/versions/oreo/background
- Foreground services overview: https://developer.android.com/develop/background-work/services/foreground-services
- Foreground service types required for target API 34+: https://developer.android.com/about/versions/14/changes/fgs-types-required
- Background starts and while-in-use permissions: https://developer.android.com/develop/background-work/services/fgs/restrictions-bg-start
- AccessibilityService reference: https://developer.android.com/reference/android/accessibilityservice/AccessibilityService
- Accessibility overview: https://developer.android.com/guide/topics/ui/accessibility/service
- Runtime permissions: https://developer.android.com/training/permissions/requesting

## 8. Required proof-of-concepts

Before finalizing minimum SDK and service design:
- Build and install on API 27 physical hardware.
- Verify user-started voice capture, cancellation, permission denial and repeated sessions.
- Verify app backgrounding, screen lock, process kill and reboot behavior.
- Verify accessibility service enable/disable, tree retrieval, action return values and stale-node handling.
- Verify compatibility on at least one modern Android version (API 34+) as a separate profile.
