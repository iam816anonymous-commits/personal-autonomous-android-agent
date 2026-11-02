# Development phases

**Canonical progress and next task:** [PROJECT_STATUS.md](../PROJECT_STATUS.md) — read it before every work session.
**Detailed execution steps:** [Phase implementation blueprint](PHASE_IMPLEMENTATION_BLUEPRINT.md).

Target: personal-use, sideloaded Android agent. No Play Store release requirement. The order below is dependency-driven and must not be skipped without an explicit owner decision. A phase is complete only when its exit evidence exists; documentation alone is not implementation.

## Phase 0 — Repository archaeology, research and requirements freeze

Confirm device/OS/ABI matrix, inspect donor repositories at pinned revisions, build/test candidate donors, complete selected-code dependency/license provenance, define threat model and privacy rules, freeze initial physical-device benchmark and resolve/defer technical spikes.

**Exit:** evidence-backed reuse plan, device matrix, benchmark and implementation scope. Current status: in progress; see PROJECT_STATUS.md.

## Phase 1 — Buildable Android shell and stable contracts

Create the Kotlin app foundation, pin toolchain, define canonical domain contracts, add device profiler, onboarding/status UI, global stop control, redacted logging, durable journal interfaces and deterministic CI.

**Exit:** clean build/unit tests/lint, truthful capability reporting, install/launch/restart evidence on target physical devices.

## Phase 2 — Accessibility observation bridge

Implement one user-enabled AccessibilityService, bounded immutable UI snapshots, sensitive-field redaction, snapshot-local target IDs, lifecycle handling and diagnostics.

**Exit:** fresh, correctly redacted observations and stable service lifecycle on target apps/devices, including API 27 degradation evidence.

## Phase 3A — Hardware capability discovery and control

Build hardware registry and typed adapters. Distinguish hardware presence, API support, permission, runtime availability and verified state. Start with low-risk operations; add permission-gated capabilities only when supported.

**Exit:** physical-device discovery/control tests with read-back verification; unsupported or unverified operations never report success.

## Phase 3 — Deterministic device action layer and verification

Implement finite typed actions, target grounding, preconditions, capability/policy checks, approvals, timeouts, cancellation, idempotency and independent postcondition verification.

**Exit:** deterministic action suite with fresh observations, evidence-backed results and zero unauthorized dispatches.

## Phase 4 — Single-agent orchestration and autonomous execution loop

Implement one authoritative bounded observe-plan-validate-approve-execute-observe-verify-recover loop, durable execution journal, global retry/recovery budgets, process-death handling and honest partial-result reporting.

**Exit:** fixed multi-step benchmark completes with verified outcomes, bounded recovery and no blind replay.

## Phase 5 — Voice input and command interpretation

Implement user-initiated speech-to-text, transcript validation/review, cancellation, language selection and offline capability reporting. Evaluate English first, then Telugu/Hindi. Do not add wake-word here.

**Exit:** repeatable physical-device speech tests across supported OS versions and offline/network states.

## Phase 6 — Local model runtime, model catalogue and tool calling

Prototype candidate runtimes, measure physical-device PSS/RSS, latency, throughput, thermal behavior and structured tool-call validity. Add opt-in downloads, checksum/metadata verification, conservative eligibility and graceful unload/fallback.

**Exit:** reproducible model benchmark and reliable tool calling on the target device matrix. No model is eligible based on file size or advertised RAM alone.

## Phase 7 — Memory, skills and user-approved learning

Implement redacted task history, explicit preferences, versioned declarative skills, provenance, revocation, inspect/export/delete and safe schema migrations.

**Exit:** privacy, retention, revocation and replay-safety tests pass; learned skills cannot expand permissions.

## Phase 8 — Wake word and long-running autonomy

Evaluate on-device keyword spotting only after tap-to-talk reliability. Add visible listening state, one-tap stop, battery/thermal monitoring and foreground/background lifecycle handling.

**Exit:** false accept/reject, battery, process-death, reboot, Doze and OEM behavior measured on physical devices.

## Phase 9 — Hardening, device profiles and personal release

Complete security/privacy/license review, physical-device regression, crash/ANR/performance testing, backup/recovery, user documentation and reproducible sideloadable release.

**Exit:** all release acceptance criteria satisfied with reproducible evidence and known limitations documented.

---

## Execution rules

1. Read PROJECT_STATUS.md before work and verify the current repository state.
2. Work on the next unblocked task only; do not skip phase gates for convenience.
3. Re-fetch exact donor source revisions before copying.
4. Treat CI and physical-device validation as separate evidence.
5. Mark tests not run as NOT RUN, never PASS.
6. Update PROJECT_STATUS.md after every work session and update the relevant detailed document/decision log when needed.
7. Do not begin app implementation until Phase 0 is closed and explicit implementation authorization is recorded.

## Out-of-scope future project — Standalone home-security app

The home-security concept is explicitly excluded from this agent's phases, modules, dependencies, permissions and release criteria. See [standalone home-security app research](../ARCHITECTURE/HOME_SECURITY_AND_REMOTE_ACCESS.md) for future reference only. It will be designed and built as a separate Android app, potentially installed on the same physical phone. Do not schedule work on it in this repository.
