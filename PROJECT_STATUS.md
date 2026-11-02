# Project Status and Operating Log

**Last updated:** 2026-11-02  
**Project:** Personal Autonomous Android Agent  
**Canonical status file:** This document  
**Product target:** Personal-use Android agent, installed by sideloading on the owner's devices  
**Implementation authorization:** **NOT GRANTED** — documentation/research maintenance is authorized; app source-code implementation remains gated pending explicit approval.

> **Mandatory session rule:** Before doing any work in this repository, read this file first. Then inspect the current GitHub branch/HEAD and the relevant source files. Do not rely on a previous chat summary as proof of repository state. At the end of every work session, update this file with what changed, evidence, current phase, blockers and the exact next task.

---

## 1. Product objective and non-negotiable constraints

Build a real Android-native personal agent that can accept a goal (initially text and user-started speech), observe the current device/app state, plan, perform permitted actions, verify each result, recover safely and report what actually happened. Add an optional, separately permissioned home-security mode: a device left at home performs local event-based face detection/known-person matching, alerts only on unfamiliar or uncertain faces, and provides authenticated remote access to event snapshots and device status.

- Personal use and sideloaded installation are the primary deployment model. Google Play publication is not currently a requirement.
- Kotlin/Android-native runtime is preferred.
- Windows-native development workflow; no Docker requirement.
- Physical Android devices are authoritative for device behavior. CI must remain deterministic and should not pretend emulator tests prove microphone, OEM, AccessibilityService or model behavior.
- API 27 / Android 8.1 is a desired compatibility target, not a promise of feature parity. Use capability detection and honest degradation.
- Local-first/offline behavior is preferred for supported tasks. Cloud use must be explicit and privacy-controlled.
- A model may propose actions but must never directly call Android APIs or bypass deterministic validation, permissions, risk controls or approvals.
- No false success: dispatch is not verification. Completion requires evidence of the expected postcondition.
- No hidden recording, authentication bypass, silent sensitive transactions, unrestricted model-generated shell/code execution or secret exfiltration.
- No unsupported feature may be represented as implemented or verified.

## 2. Current project state

### Overall

**Research/specification baseline**

- The repository is a documentation-first project at the last verified baseline.
- Product assumptions, high-level architecture, platform constraints, research, source reuse decisions, safety contracts and phase plans are documented.
- A source-by-source reuse specification exists, including class-level RETAIN / ADAPT / REWRITE / REJECT decisions and known defects.
- Hardware capability architecture is documented.
- Proposed home-security and secure remote-access architecture is documented; it is not implemented or physically validated.
- No destination Android application implementation, integration build or physical-device acceptance run has been established by the documentation audit.
- No model/runtime has been selected based on reproducible measurements on the target physical device.
- Full transitive dependency/license clearance remains open.
- The implementation authorization checklist in the source reuse specification is not complete.

### Phase status

| Phase | Name | Status | Evidence / remaining work |
|---|---|---|---|
| 0 | Repository archaeology, research and requirements freeze | **In progress** | Core audits and reuse specification exist. Still need final device/OS matrix, donor clean builds/tests, complete selected-dependency/license provenance, benchmark freeze and unresolved technical spikes. |
| 1 | Buildable Android shell and stable contracts | Not started | No destination app/build evidence recorded. |
| 2 | Accessibility observation bridge | Not started | Architecture and source candidates documented; no destination implementation or physical-device evidence. |
| 3A | Hardware capability discovery and control | Not started | Subsystem design documented; no adapters or device verification. |
| 3 | Deterministic device action layer and verification | Not started | Contracts and defects documented; no destination implementation. |
| 4 | Single-agent orchestration and autonomous execution loop | Not started | Target loop described; no destination implementation. |
| 5 | Voice input and command interpretation | Not started | Design/research exists; no destination speech pipeline validated. |
| 6 | Local model runtime, model catalogue and tool calling | Not started | Candidates researched; no runtime/model selected by physical-device benchmark. |
| 7 | Memory, skills and user-approved learning | Not started | Design exists; no destination implementation. |
| 8 | Wake word and long-running autonomy | Not started | Explicitly gated behind voice and lifecycle evidence. |
| 9 | Hardening, device profiles and personal release | Not started | Requires all earlier gates and physical-device regression. |
| Optional parallel workstream | Home security monitoring and secure remote access | Proposed / not started | Separate camera, face detection/recognition, event retention, remote VPN/API and alerting pipeline; depends on device camera lifecycle, security controls and physical-device evidence. |

**Important:** A written design, a donor repository implementation, or a test plan does not count as a feature implemented in this destination repository.

## 3. Canonical implementation roadmap

An optional home-security workstream has been added in [HOME_SECURITY_AND_REMOTE_ACCESS.md](ARCHITECTURE/HOME_SECURITY_AND_REMOTE_ACCESS.md). It is not a replacement for the core phases and must not delay or destabilise the core agent. Its prototype gates are: visible camera uptime, face detection, consented known-person recognition, unknown-event handling, secure remote access, notifications and hardening.

The detailed sequence in [ROADMAP/PHASE_IMPLEMENTATION_BLUEPRINT.md](ROADMAP/PHASE_IMPLEMENTATION_BLUEPRINT.md) is the phase-level execution plan. [ROADMAP/PHASES.md](ROADMAP/PHASES.md) is its short index. This status file tracks progress and the next action.

### Phase 0 — Repository archaeology, research and requirements freeze

**Objective:** Establish what exists, what is safe/legal to reuse, what device capabilities are actually available, and what must be built.

Exit gate:
- [ ] Confirm target device/OS/ABI matrix and supported feature-degradation policy.
- [x] Record personal-use/sideloading requirement.
- [x] Complete initial source-by-source class reuse specification.
- [ ] Build selected donor repositories from clean checkout; run their unit tests, lint/static analysis and debug build.
- [ ] Complete exact source revision, dependency and transitive licence/provenance ledger for components selected for reuse.
- [ ] Compare overlapping donor implementations and select one owner per responsibility.
- [ ] Freeze the initial physical-device task benchmark and independent verifiers.
- [ ] Complete threat model, privacy/data-retention rules and unresolved technical experiment list.
- [ ] Resolve or explicitly defer ADK Kotlin/LiteRT-LM versus custom runtime, API 27 visual observation fallback, wake-word feasibility and encryption/backup design.

### Phase 1 — Buildable Android shell and stable contracts

Create the smallest reliable Kotlin app foundation, canonical domain contracts, device profiler, onboarding/status UI, global stop control, redacted logging, durable task-journal interfaces and deterministic CI. Do not start with a model or wake-word.

### Phase 2 — Accessibility observation bridge

Implement one user-enabled AccessibilityService, immutable bounded UI snapshots, sensitive-field redaction, fresh snapshot IDs, lifecycle handling and diagnostics. Prove behavior on physical devices before allowing autonomous actions.

### Phase 3A — Hardware capability discovery and control

Build a hardware registry and typed adapters. Distinguish hardware presence, API support, permission, runtime availability and verified state. Begin with low-risk capabilities; expand only with permission-aware, device-tested adapters.

### Phase 3 — Deterministic device action layer and verification

Implement a finite typed action vocabulary, target grounding, preconditions, policy checks, timeouts, cancellation, idempotency and independent postcondition verification. No model authority over platform APIs.

### Phase 4 — Single-agent orchestration and autonomous execution loop

Implement one authoritative bounded observe-plan-validate-approve-execute-observe-verify-recover loop. Add durable journal, retry budget, recovery-depth limit, stop behavior and honest partial-completion reporting.

### Phase 5 — Voice input and command interpretation

Add user-initiated speech-to-text first, with cancellation, permission/lifecycle handling, transcript review and offline capability reporting. Evaluate English first, then Telugu/Hindi. Wake-word is not part of this phase.

### Phase 6 — Local model runtime, model catalogue and tool calling

Run controlled proof-of-concepts for candidate runtimes. Download only with user opt-in; verify checksums and metadata; measure peak PSS/RSS, throughput, latency, thermal behavior and structured tool-call validity on physical devices. Model eligibility must be conservative and rechecked at load time.

### Phase 7 — Memory, skills and user-approved learning

Add privacy-preserving task history, explicit preferences, versioned declarative skills, redacted traces, revocation, inspect/export/delete and safe migrations. Do not execute arbitrary third-party skill code.

### Phase 8 — Wake word and long-running autonomy

Only after user-started voice is reliable: evaluate on-device keyword spotting, visible listening status, stop controls, foreground/background lifecycle, battery, thermal and OEM behavior.

### Phase 9 — Hardening, device profiles and personal release

Complete security/privacy/license review, physical-device regression, crash/ANR/performance testing, process-death recovery, backup/recovery, documentation and a reproducible sideloadable release.

## 4. Work order and dependency rules

The default work order is mandatory unless the owner explicitly changes scope:

1. Read this file and inspect current repository state.
2. Confirm the active phase and its entry criteria.
3. Work only on the next unblocked task; do not jump ahead to attractive features.
4. Research and inspect exact source/revision before reuse.
5. Make the smallest coherent change that satisfies the task.
6. Run relevant checks and capture actual output. If a physical device is unavailable, state that clearly.
7. Review the diff for scope creep, privacy/security regressions and documentation consistency.
8. Update this file, the relevant detailed document and decision log if a decision changed.
9. Commit only after verifying the changed files and repository state.
10. Report completed, not completed, test evidence, limitations and next exact task.

### Gate discipline

- A phase is not complete because its document is complete.
- Do not begin a dependent phase while its entry gate is unmet.
- Do not mark CI green without checking the latest commit's actual status.
- Do not claim physical-device validation without the device model, Android version/API, build identifier, steps and observed result.
- Do not claim model compatibility from parameter count, file size, marketing RAM or emulator success.
- Do not blindly replay non-idempotent actions after process death or timeout.
- When a test cannot be run, record it as **NOT RUN**, not PASS.
- If evidence contradicts a design, update the design and decision log rather than preserving the old claim.

## 5. Documentation audit and ownership

Keep documents that have distinct purposes. Avoid duplicating full architecture content into the README or this status file.

| Document | Decision | Role / required treatment |
|---|---|---|
| `README.md` | KEEP + simplify | Landing page, product summary, navigation and operating rules. Link prominently to this status file. |
| `PROJECT_STATUS.md` | CANONICAL / REQUIRED | Single current progress ledger, phase status, next task, blockers, authorization and session history. |
| `RESEARCH/README.md` | KEEP | Research index only; do not duplicate detailed findings. |
| `RESEARCH/ANDROID_PLATFORM_CONSTRAINTS.md` | KEEP | Platform/API/lifecycle constraints and official Android references. Recheck before implementation. |
| `RESEARCH/GOOGLE_PLAY_POLICY_RESEARCH.md` | KEEP, LOW PRIORITY | Historical/future distribution reference only; not a current acceptance gate. |
| `RESEARCH/OPEN_SOURCE_REPOSITORY_AUDIT.md` | KEEP | Comparative repository-level audit and external reference shortlist. |
| `RESEARCH/OPEN_SOURCE_LICENSE_AUDIT.md` | KEEP, UPDATE BEFORE REUSE | Triage only; not final legal clearance or a complete dependency SBOM. |
| `RESEARCH/MODEL_AND_INFERENCE_RESEARCH.md` | KEEP, UPDATE BEFORE SELECTION | Candidate and measurement requirements; no model is selected yet. |
| `RESEARCH/SOURCE_BY_SOURCE_REUSE_SPECIFICATION.md` | KEEP, AUTHORITATIVE FOR REUSE DECISIONS | Class/file-level reuse ledger. Re-fetch exact source revisions before copying; decisions are provisional pending build, tests and license checks. |
| `ARCHITECTURE/MASTER_ARCHITECTURE.md` | KEEP | System boundaries, module map and execution invariants. |
| `ARCHITECTURE/AGENT_EXECUTION_ENGINE.md` | KEEP | Detailed execution state machine and action lifecycle. |
| `ARCHITECTURE/SAFETY_AND_APPROVALS.md` | KEEP | Risk tiers, approval binding, prompt injection and prohibitions. |
| `ARCHITECTURE/VOICE_AND_WAKE_WORD.md` | KEEP | Voice capture and wake-word lifecycle, deliberately separated. |
| `ARCHITECTURE/DEVICE_CONTROL.md` | KEEP | Accessibility and device-control adapter strategy. |
| `ARCHITECTURE/HARDWARE_CAPABILITIES.md` | KEEP | Dedicated hardware discovery/control subsystem. |\n| `ARCHITECTURE/HOME_SECURITY_AND_REMOTE_ACCESS.md` | KEEP, OPTIONAL WORKSTREAM | Camera-based event monitoring, consented known-person matching, unfamiliar-face alerts, secure remote access and retention requirements. |
| `ARCHITECTURE/MODEL_ROUTER.md` | KEEP | Runtime routing and model eligibility rules. |
| `ARCHITECTURE/MEMORY_AND_LEARNING.md` | KEEP | Data classes, retention and safe learning. |
| `ARCHITECTURE/DECISION_LOG.md` | KEEP, UPDATE ON DECISIONS | Append accepted/rejected decisions and rationale; do not use as progress tracker. |
| `ENGINEERING/API_CONTRACTS.md` | KEEP, CONSOLIDATE LATER | Draft domain contracts. During Phase 1, reconcile with execution-engine examples and source reuse spec into one canonical set of types. |
| `ENGINEERING/DATA_MODEL.md` | KEEP, CONSOLIDATE LATER | Proposed persistence schema; refine only when persistence is implemented. |
| `ENGINEERING/TEST_STRATEGY.md` | KEEP | Test layers, physical-device policy and benchmark methodology. |
| `ENGINEERING/PERFORMANCE_BUDGETS.md` | KEEP | Measurement plan and provisional budgets; replace assumptions with measurements. |
| `ROADMAP/PHASES.md` | KEEP, ALIGN WITH BLUEPRINT | Short phase index. Phase names/order must match the detailed blueprint and this file. |
| `ROADMAP/PHASE_IMPLEMENTATION_BLUEPRINT.md` | KEEP | Detailed phase steps, reuse mapping, technical gates and required evidence. |
| `ROADMAP/ACCEPTANCE_CRITERIA.md` | KEEP | Cross-cutting acceptance checklist; add phase-specific tests as implementation begins. |
| `REFERENCES/SOURCES.md` | KEEP | Central source link list; remove stale links and add sources used in new decisions. |

### Known documentation problems to resolve

1. **Phase numbering/order was inconsistent.** The compact roadmap previously placed voice before accessibility and omitted the deterministic-action phase split; the detailed blueprint has the correct dependency-oriented sequence. Align the short roadmap to the detailed sequence.
2. **Contracts are repeated.** `MASTER_ARCHITECTURE.md`, `AGENT_EXECUTION_ENGINE.md`, `API_CONTRACTS.md` and the reuse specification each describe overlapping action/result concepts. Keep their different purposes, but Phase 1 must designate one canonical source for normative Kotlin types and link to it from the others.
3. **Reuse/license language can be mistaken for clearance.** All reuse labels remain provisional until exact revisions, source headers, dependencies, native binaries, assets and licenses are checked.
4. **Some research is time-sensitive.** Revalidate Android API behavior, ADK/LiteRT-LM versions, repository heads, licenses and model availability immediately before adopting them.
5. **No broad deletion yet.** The current documents contain useful audit evidence. Do not delete or merge detailed records merely to reduce file count; consolidate only after preserving unique evidence and links.

## 6. Current next task

**Active phase:** Phase 0 — Repository archaeology, research and requirements freeze.  
**Next task:** Close the remaining Phase 0 evidence gaps before any app implementation.

Task sequence:
1. Confirm the exact target physical devices (model, Android version/API, ABI, available storage and memory) and classify API 27 as required or best-effort.
2. Re-fetch the five first-party donor repositories at pinned commits; verify the branch/commit still matches the reuse specification.
3. Clean-build and run tests/lint on the selected donor candidates; record exact commands and outputs. Do not claim success from prior summaries.
4. Complete a component-level dependency and license/provenance ledger for the subset actually proposed for reuse. Do not create a giant SBOM for components we will not use.
5. Freeze 20–30 initial tasks with explicit initial state, allowed capabilities, expected final state, forbidden side effects, timeout and independent verifier.
6. Close or defer each Phase 0 technical spike with a documented reason and decision owner.
7. Add the home-security camera/face-recognition/remote-access spike to the Phase 0 device and threat-model review.
8. Review the implementation authorization checklist with the owner. Until explicit approval, stop at research/specification and documentation changes.

**Do not start Phase 1 code implementation yet.** The project has not cleared Phase 0 and the previous explicit authorization state was NOT AUTHORIZED.

## 7. Blockers and unresolved decisions

- [ ] Exact current target-device matrix and whether API 27 is a hard requirement.
- [ ] Donor repositories' clean build/test/lint evidence at pinned revisions.
- [ ] Full license/provenance review for the exact selected code and transitive dependencies.
- [ ] Canonical first-release task benchmark and independent verifiers.
- [ ] ADK Kotlin + LiteRT-LM versus custom Kotlin orchestration/inference proof-of-concept.
- [ ] API 27 visual observation fallback and realistic screenshot limitations.
- [ ] Encryption/key-management and backup/restore design.
- [ ] Wake-word feasibility under Android/OEM lifecycle constraints.\n- [ ] Home-device camera foreground-service uptime, face-recognition model evaluation and false-match/false-reject measurements.\n- [ ] Private VPN/remote API authentication and alert-delivery design.
- [ ] Explicit authorization for app source-code implementation.

## 8. Session log

### 2026-11-02 — Documentation control baseline

- Reviewed the existing README, research index, phase roadmap, detailed implementation blueprint, source-by-source reuse specification, architecture documents, engineering contracts, acceptance criteria and research references.
- Identified phase-order/numbering drift between the compact roadmap and detailed blueprint.
- Identified overlapping but distinct contract descriptions; deferred normative contract consolidation to Phase 1 rather than deleting evidence prematurely.
- Established this file as the mandatory session-start checklist and canonical progress ledger.
- Updated README navigation/working rules to point to this file; aligned the short phase roadmap with the detailed dependency-oriented phase sequence.
- Added the optional home-security and secure remote-access requirement, with a dedicated architecture document.
- Research indicates ML Kit face detection locates faces but does not identify individuals; a separate on-device face-embedding model is required. No recognition model has been selected or benchmarked.
- Recommended private VPN/overlay networking plus an authenticated application API; no public camera port should be exposed by default.
- Android camera foreground-service restrictions and OEM uptime are major feasibility gates; no 24/7 reliability claim is made.
- Updated the master architecture, hardware capability notes, Android platform constraints, README, roadmap and source index. No application code, dependencies, build configuration or CI workflow changed.
- No app implementation or physical-device test was performed. Implementation authorization remains **NOT GRANTED**.
- Added `ARCHITECTURE/HOME_SECURITY_AND_REMOTE_ACCESS.md` and indexed it from README, research index, roadmap, master architecture and platform/hardware documentation.\n- Repaired literal escaped line breaks introduced during document updates and re-fetched the affected files.\n- Documentation commits for the new workstream include `c4731c3`, `baa48ee`, `e80cc05`, `720e7a2`, `378e4bd`, `3b43a67`, `624314f`, `ab3a099`, `734aa8b`, `375d131`, `73d1586`, `bc70a45`, `903027f`, `3747edf`, `ebd2c91` and `e6fd937`.\n- No application code, dependencies, build configuration, CI or physical-device tests changed.\n- Next: close Phase 0 evidence gaps, including target home-device camera uptime and computer-vision model evaluation.
