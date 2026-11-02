# Development phases

Target: personal-use sideloaded Android agent. No Play Store release requirement. Start a phase only after entry criteria are met.

## Phase 0 — Research and personal-device requirements
Confirm device/OS matrix, audit reference code/licenses, define threat model, AccessibilityService capability map and test tasks. **Exit:** risks and proof-of-concept plans documented.

## Phase 1 — Android shell and capability profiler
Kotlin app, API/ABI/RAM/storage profiler, permission/service onboarding, visible agent status and emergency stop, redacted logs, CI build/unit tests/lint. Test API 27 and modern physical devices. **Exit:** stable install/launch and truthful capability reporting.

## Phase 2 — Voice input
Tap-to-talk speech-to-text, English first, Telugu/Hindi evaluation, offline detection, cancellation, permission and lifecycle handling. **Exit:** repeatable voice tests on physical devices.

## Phase 3 — Accessibility observation bridge
Implement user-enabled AccessibilityService; inspect UI tree, package/activity, node metadata and bounds; add redaction and snapshot IDs. Test stale nodes, secure windows and OEM differences. **Exit:** reliable observation on target apps/devices.

## Phase 4 — Agent core and safe actions
Typed tool registry, planner, execution journal, app launch, grounded UI actions, approval gate, verifier and recovery. **Exit:** fixed task suite with zero unauthorized actions and verified final state.

## Phase 5 — Local model runtime and catalog
Prototype ADK Kotlin/LiteRT-LM versus custom adapter; opt-in model downloads, checksum verification, measured RAM eligibility and unload/fallback. **Exit:** stable inference and valid tool calls on target hardware.

## Phase 6 — Cross-app skills, memory and learning
User-controlled history/preferences, versioned workflows, pre/postconditions, redacted trace-based skill proposals, export/delete. **Exit:** learned skills cannot expand permissions or bypass approval.

## Phase 7 — Wake-word and long-running autonomy
On-device keyword spotting, visible listening status, one-tap stop, battery and process lifecycle tests. **Exit:** stable behavior across device/OS matrix.

## Phase 8 — Personal release readiness
Security/privacy/license review, physical-device regression, crash/ANR and performance checks, backup/recovery and user documentation. Play policy review is out of scope unless distribution intent changes.