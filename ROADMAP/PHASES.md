# Development phases

**Status:** Research baseline only. A phase may start only when its entry criteria are met.

## Phase 0 — Research, requirements and product constraints
- Confirm personal-only vs Google Play distribution target.
- Complete platform and policy review.
- Audit candidate repositories and licenses.
- Define threat model, capability matrix and acceptance tasks.
- Record decisions and open questions.

**Exit:** no unresolved blocker affecting the fundamental architecture; key risks have reproducible test plans.

## Phase 1 — Android shell and capability profiler
- Kotlin Android app builds reliably.
- Device/API/ABI/RAM/storage capability profile.
- Permission onboarding and explicit session UI.
- Structured logging with redaction.
- CI: build, unit tests, lint/static analysis.
- Verify API 27 and modern API device builds.

**Exit:** installs, launches, persists settings and reports capabilities correctly on physical devices.

## Phase 2 — Safe voice input
- User-initiated speech-to-text.
- English first; evaluate Telugu/Hindi.
- Offline capability detection and fallback.
- Cancel/retry/permission denial/process lifecycle.
- No wake-word yet.

**Exit:** repeatable voice-to-text acceptance tests on target physical devices.

## Phase 3 — Agent core without device writes
- Goal normalization, observation schema, planning schema.
- Typed tool registry and state machine.
- Execution journal and cancellation.
- Deterministic verifier tests.
- Model adapter proof-of-concept.

**Exit:** model produces valid plans; no unauthorized device action possible.

## Phase 4 — Limited device actions
- Explicit app launch/navigation and permitted narrow actions.
- Fresh observation and postcondition verification.
- Approval gate and stop control.
- Recover from stale UI and failed action.
- Validate distribution profile separately.

**Exit:** task benchmark passes with zero unauthorized actions.

## Phase 5 — Model downloads and local inference
- Model catalog, checksum verification and install lifecycle.
- Device resource eligibility and hard peak-memory budget.
- Tool-call reliability tests and fallback behavior.
- Unload under memory pressure.

**Exit:** measured stable inference on target devices, never based on advertised RAM alone.

## Phase 6 — Skills, memory and learning
- User-controlled preferences and task history.
- Versioned skills with preconditions and postconditions.
- Candidate skill learning from redacted traces.
- Export/delete/disable controls.

**Exit:** learned skills cannot expand permissions or bypass approval.

## Phase 7 — Wake-word and advanced autonomy
- Only after microphone lifecycle, battery, privacy and policy review.
- On-device keyword spotting preferred.
- Explicit opt-in and visible status.
- Long-horizon execution with checkpoints and recovery.

**Exit:** sustained reliability and battery tests across device/OS matrix.

## Phase 8 — Release readiness
- Security/privacy review, dependency and license review.
- Store policy review if distributing through Play.
- Accessibility and data safety declarations.
- Physical-device regression suite, crash/ANR and performance.
- User documentation and recovery instructions.
