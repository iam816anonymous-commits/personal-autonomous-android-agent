# Android integration tranche

This is the first Android framework adapter layer over the platform-neutral :core module.

## Implemented source

- API 27 minimum application shell and explicit AccessibilityService declaration.
- User-enabled AccessibilityService lifecycle.
- Bounded semantic accessibility snapshot (250 nodes maximum), password-field redaction, and no screenshot/MediaProjection fallback on API 27.
- Typed app launch, accessible click, and accessible set-text dispatcher.
- Independent postcondition verifier requiring a distinct newer snapshot.
- Capability discovery for app launch and accessibility observation/mutation.
- Room task-step journal using WAL. Journal note payloads are encrypted with AES-256-GCM using an Android Keystore key; metadata columns remain queryable.
- WorkManager unique-work scheduling adapter. The worker emits a due signal only; it does not execute a model-generated action.
- AndroidAgentRuntime composition root and an explicit AlertDialog-backed AndroidApprovalProvider. A missing approval provider defaults to denial; the dialog provider must be invoked from a visible Activity lifecycle scope and is not yet wired into a complete task-entry UI.

## Safety properties and deliberate restrictions

- The model planner receives domain contracts, not Android framework objects.
- No shell, arbitrary intent, coordinate tap, screenshot, MediaProjection, root, or lock-screen bypass is exposed.
- Password nodes are never exposed as text and cannot be click/text targets through this dispatcher.
- Click/text targets are re-found from the current active accessibility tree; stale targets fail closed.
- Dispatch success is not task success. The orchestrator requires independent postcondition verification.
- The default approval provider returns no token. Click and text mutations therefore remain blocked until a real explicit user-confirmation UI is wired to ApprovalPolicy.issueUserApproval().
- WorkManager only signals due jobs. Persisted job definitions, approval-at-execution and recovery are not implemented.

## Build and validation

CI runs :core:compileKotlin, :core:test and :app:assembleDebug. A successful CI APK build is not physical-device evidence. API 27 OEM behavior, accessibility node recycling/lifecycle, Room/Keystore behavior and all device actions still require testing on the exact TECNO hardware variant.

## Not implemented

- Hardware torch, vibration, Wi-Fi, Bluetooth, camera and sensor control adapters.
- End-to-end task-entry UI wiring, global stop control and runtime diagnostics screen.
- Durable scheduler job database and receiver/worker-to-orchestrator handoff.
- In-flight task recovery and safe process-death reconciliation.
- Android instrumentation tests and TECNO API 27 smoke tests.
