# Development phases

A phase starts only after entry criteria are met.

## Phase 0 — Research and requirements
Confirm personal vs Play distribution, complete policy/platform and license review, define threat model, capability matrix and test tasks. **Exit:** architecture blockers and proof-of-concept plans are documented.

## Phase 1 — Android shell and device profiler
Kotlin app, capability/permission profiler, settings, redacted logs, CI build/unit tests/lint; API 27 and modern API physical-device checks. **Exit:** reliable install, launch and capability reporting.

## Phase 2 — User-initiated voice
Tap-to-talk, speech-to-text, English first, Telugu/Hindi evaluation, offline capability detection, cancel/retry/permission/process lifecycle. No wake-word yet. **Exit:** repeatable physical-device voice tests.

## Phase 3 — Agent core, no device writes
Goal normalization, observation/plan schemas, typed tool registry, state machine, execution journal, cancellation, verifier, model adapter proof-of-concept. **Exit:** valid plans and no unauthorized writes possible.

## Phase 4 — Limited device actions
Permitted app launch/navigation/actions, fresh observation, postcondition verification, approvals, emergency stop and recovery. **Exit:** task benchmark passes with zero unauthorized actions.

## Phase 5 — Model catalog and local inference
Opt-in downloads, checksum verification, resource eligibility, peak-memory budget, tool-call reliability, graceful unload. **Exit:** repeatable physical-device inference measurements.

## Phase 6 — Memory and skills
User-controlled history/preferences, versioned skills, pre/postconditions, redacted trace-based skill proposals, export/delete. **Exit:** learned skills cannot expand permissions or bypass approvals.

## Phase 7 — Wake-word and advanced autonomy
Only after lifecycle, battery, privacy and policy review; prefer on-device keyword spotting and visible status. **Exit:** sustained device/OS-matrix reliability.

## Phase 8 — Release readiness
Security/privacy/license/dependency review, Play policy review if applicable, physical-device regression, performance and documentation.
