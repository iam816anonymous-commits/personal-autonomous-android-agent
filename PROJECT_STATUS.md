# Project Status and Operating Log

**Last updated:** 2026-11-03 18:55 IST  
**Project:** Personal Autonomous Android Agent  
**Canonical status file:** This document  
**Product target:** Personal-use Android agent, installed by sideloading on the owner's devices  
**Implementation authorization:** **KOTLIN CORE + INITIAL ANDROID INTEGRATION SOURCE AUTHORIZED** — on 2026-11-03 the owner explicitly requested both the Hermes-inspired Kotlin core and the Android execution layer. This authorizes the Android app shell, Room journal, Keystore-backed journal-note encryption, AccessibilityService adapters and WorkManager boundary as a source implementation tranche. It does not waive safety, donor provenance, benchmark sign-off, device identity, build, or physical-device acceptance gates.

> **Mandatory session rule:** Before doing any work in this repository, read this file first. Then inspect the current GitHub branch/HEAD and the relevant source files. Do not rely on a previous chat summary as proof of repository state. At the end of every work session, update this file with what changed, evidence, current phase, blockers and the exact next task.

---

## 1. Product objective and non-negotiable constraints

Build a real Android-native personal agent that can accept a goal (initially text and user-started speech), observe the current device/app state, plan, perform permitted actions, verify each result, recover safely and report what actually happened. Home-security monitoring, face recognition and remote surveillance are explicitly out of scope for this agent. They will be designed and built later as a separate Android app that may be installed on the same physical phone.

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

**Kotlin/JVM core plus Android integration on main; durable journal, initial REA bridge and declarative self-tool factory merged; live REA UI now added; latest CI and physical-device validation pending**

- The repository contains a Kotlin/JVM core and an Android application module. GitHub Actions run 41612516639 passed core compilation/tests, three Android verifier unit tests and :agent:app:assembleDebug on commit e3ea4db83d33cc127097427dcabc5358de559d3c; no Android source changed after that passing run.
- Product assumptions, high-level architecture, platform constraints, research, source reuse decisions, safety contracts and phase plans are documented.
- A source-by-source reuse specification exists, including class-level RETAIN / ADAPT / REWRITE / REJECT decisions and known defects.
- Hardware capability architecture is documented.
- A separate-app home-security research specification is retained for future reference; it is not part of this agent's architecture or implementation plan.
- A platform-neutral Kotlin core is under `research/IMPLEMENTATION/`; an Android application module is now added under `app/` and depends on `:core`.
- A Kotlin/JVM Gradle build and seven unit tests now exist. GitHub Actions run `41609551659` passed `:agent:core:compileKotlin` and `:agent:core:test` at commit `199cd7120d42534a642589aa332ce41f366d789f`.
- Initial Android sources now include the API 27 app shell, manifest and AccessibilityService, bounded semantic observer, typed dispatcher, independent postcondition verifier, capability registry, Room WAL task journal, AES-GCM Android Keystore note encryption and a WorkManager due-signal adapter. These sources compiled in CI and three verifier unit tests passed; no AccessibilityService instrumentation or physical-device acceptance run has been performed.
- No model/runtime has been selected based on reproducible measurements on the target physical device.
- Full transitive dependency/license clearance remains open.
- The implementation authorization checklist in the source reuse specification is not complete.

### Phase status

| Phase | Name | Status | Evidence / remaining work |
|---|---|---|---|
| 0 | Repository archaeology, research and requirements freeze | **In progress (Argon validation complete; awaiting Owner sign-off)** | Argon Phase 0 deliverables are archived under `research/research/ENGINEERING/ARGON/` (`DONOR_BUILD_TEST_MATRIX.md`, `DONOR_SOURCE_PROVENANCE_AND_LICENSE_LEDGER.md`, `DONOR_REVISION_AND_DIFF_REPORT.md`, `INITIAL_TASK_BENCHMARK.md`, `PHASE_0_TECHNICAL_SPIKES.md`, plus the development handoff). Awaiting Owner confirmation of final device matrix/API 27 mandate, first-party donor license declaration, benchmark approval, and Phase 1 start authorization. |
| 1 | Buildable Android shell and stable contracts | **Prototype source added; CI build passed; device gate open** | Kotlin/JVM core and API 27-minimum Android :app module compile in CI. Run 41612516639 passed core compile/tests, 3 Android verifier tests and debug APK assembly. No API 27 physical install/smoke test yet. See research/IMPLEMENTATION/HERMES_KOTLIN_PORT_REVIEW.md and research/IMPLEMENTATION/README.md. |
| 2 | Accessibility observation bridge | **Initial adapter source added; device validation pending** | User-enabled service, bounded 250-node semantic observer and stale-target checks added under app/; API 27/OEM behavior NOT RUN on physical device. |
| 3A | Hardware capability discovery and control | **Partial** | Initial capability registry reports accessibility and app-launch availability; torch/vibration/Wi-Fi/Bluetooth hardware adapters are NOT IMPLEMENTED. |
| 3 | Deterministic device action layer and verification | **Initial accessibility actions added; integration validation pending** | Launch, semantic click and set-text dispatch plus fresh-snapshot postcondition verifier are source-present; action UI approval and physical verification remain incomplete. |
| 4 | Single-agent orchestration and autonomous execution loop | **Platform-neutral core prototype; end-to-end app flow not wired** | Bounded observe-plan-validate-approve-dispatch-observe-verify orchestration exists in :core; Android composition root exists. No complete task-entry/session UI, durable recovery or end-to-end physical run. |
| 5 | Voice input and command interpretation | Not started | Design/research exists; no destination speech pipeline validated. |
| 6 | Local model runtime, model catalogue and tool calling | Not started | Candidates researched; no runtime/model selected by physical-device benchmark. |
| 7 | Memory, skills and user-approved learning | Not started | Design exists; no destination implementation. |
| 8 | Wake word and long-running autonomy | Not started | Explicitly gated behind voice and lifecycle evidence. |
| 9 | Hardening, device profiles and personal release | Not started | Requires all earlier gates and physical-device regression. |
| Out-of-scope future project | Standalone home-security Android app | Deferred / separate project | Research specification retained in `research/ARCHITECTURE/HOME_SECURITY_AND_REMOTE_ACCESS.md`; no dependency, module, phase or integration in this agent. It may be installed on the same phone as a separate app. |

**Important:** A written design, a donor repository implementation, or a test plan does not count as a feature implemented in this destination repository.

## 3. Canonical implementation roadmap

Home security is no longer part of this agent's scope. The retained [standalone home-security app research specification](research/ARCHITECTURE/HOME_SECURITY_AND_REMOTE_ACCESS.md) is a future, separate project. It must not create modules, dependencies, permissions, services, roadmap work or integration requirements in this agent. Both apps may be installed on the same Android phone but must remain independently packaged and permission-controlled.

The detailed sequence in [research/ROADMAP/PHASE_IMPLEMENTATION_BLUEPRINT.md](research/ROADMAP/PHASE_IMPLEMENTATION_BLUEPRINT.md) is the phase-level execution plan. [research/ROADMAP/PHASES.md](research/ROADMAP/PHASES.md) is its short index. This status file tracks progress and the next action.

### Phase 0 — Repository archaeology, research and requirements freeze

**Objective:** Establish what exists, what is safe/legal to reuse, what device capabilities are actually available, and what must be built.

Exit gate:
- [ ] Confirm target device/OS/ABI matrix and supported feature-degradation policy (Owner confirmation required; recommended baseline recorded in `research/research/ENGINEERING/ARGON/PHASE_0_TECHNICAL_SPIKES.md` §7).
- [x] Record personal-use/sideloading requirement.
- [x] Complete initial source-by-source class reuse specification (`research/RESEARCH/SOURCE_BY_SOURCE_REUSE_SPECIFICATION.md` + full 46-class audit in `research/research/ENGINEERING/ARGON/DONOR_SOURCE_PROVENANCE_AND_LICENSE_LEDGER.md`).
- [x] Build selected donor repositories from clean checkout; run their unit tests, lint/static analysis and debug build (`research/research/ENGINEERING/ARGON/DONOR_BUILD_TEST_MATRIX.md`).
- [x] Complete exact source revision, dependency and transitive licence/provenance ledger for components selected for reuse (`research/research/ENGINEERING/ARGON/DONOR_SOURCE_PROVENANCE_AND_LICENSE_LEDGER.md` & `research/research/ENGINEERING/ARGON/DONOR_REVISION_AND_DIFF_REPORT.md`; pending Owner root `LICENSE` confirmation for first-party donor repos).
- [x] Compare overlapping donor implementations and select one owner per responsibility (`research/research/ENGINEERING/ARGON/PHASE_0_TECHNICAL_SPIKES.md` §6).
- [x] Freeze the initial physical-device task benchmark and independent verifiers (`research/research/ENGINEERING/ARGON/INITIAL_TASK_BENCHMARK.md` — 26 scenarios + 14 failure-mode contracts, pending Owner sign-off).
- [x] Complete threat model, privacy/data-retention rules and unresolved technical experiment list (`research/ARCHITECTURE/SAFETY_AND_APPROVALS.md`, `research/research/ENGINEERING/ARGON/INITIAL_TASK_BENCHMARK.md` §3, `research/research/ENGINEERING/ARGON/PHASE_0_TECHNICAL_SPIKES.md`).
- [x] Resolve or explicitly defer ADK Kotlin/LiteRT-LM versus custom runtime, API 27 visual observation fallback, wake-word feasibility and encryption/backup design (`research/research/ENGINEERING/ARGON/PHASE_0_TECHNICAL_SPIKES.md` §1–§5 & `research/ARCHITECTURE/DECISION_LOG.md` ADR-006..ADR-010).

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
| `research/RESEARCH/README.md` | KEEP | Research index only; do not duplicate detailed findings. |
| `research/RESEARCH/ANDROID_PLATFORM_CONSTRAINTS.md` | KEEP | Platform/API/lifecycle constraints and official Android references. Recheck before implementation. |
| `research/RESEARCH/GOOGLE_PLAY_POLICY_RESEARCH.md` | KEEP, LOW PRIORITY | Historical/future distribution reference only; not a current acceptance gate. |
| `research/RESEARCH/OPEN_SOURCE_REPOSITORY_AUDIT.md` | KEEP | Comparative repository-level audit and external reference shortlist. |
| `research/RESEARCH/OPEN_SOURCE_LICENSE_AUDIT.md` | KEEP, UPDATE BEFORE REUSE | Triage only; not final legal clearance or a complete dependency SBOM. |
| `research/RESEARCH/MODEL_AND_INFERENCE_RESEARCH.md` | KEEP, UPDATE BEFORE SELECTION | Candidate and measurement requirements; no model is selected yet. |
| `research/RESEARCH/SOURCE_BY_SOURCE_REUSE_SPECIFICATION.md` | KEEP, AUTHORITATIVE FOR REUSE DECISIONS | Class/file-level reuse ledger. Re-fetch exact source revisions before copying; decisions are provisional pending build, tests and license checks. |
| `research/ARCHITECTURE/MASTER_ARCHITECTURE.md` | KEEP | System boundaries, module map and execution invariants. |
| `research/ARCHITECTURE/AGENT_EXECUTION_ENGINE.md` | KEEP | Detailed execution state machine and action lifecycle. |
| `research/ARCHITECTURE/SAFETY_AND_APPROVALS.md` | KEEP | Risk tiers, approval binding, prompt injection and prohibitions. |
| `research/ARCHITECTURE/VOICE_AND_WAKE_WORD.md` | KEEP | Voice capture and wake-word lifecycle, deliberately separated. |
| `research/ARCHITECTURE/DEVICE_CONTROL.md` | KEEP | Accessibility and device-control adapter strategy. |
| `research/ARCHITECTURE/HARDWARE_CAPABILITIES.md` | KEEP | Dedicated hardware discovery/control subsystem. |
| `research/ARCHITECTURE/HOME_SECURITY_AND_REMOTE_ACCESS.md` | KEEP, SEPARATE FUTURE PROJECT REFERENCE | Standalone home-security Android app research only. Explicitly out of scope for this agent; no shared runtime or integration assumed. |
| `research/ARCHITECTURE/MODEL_ROUTER.md` | KEEP | Runtime routing and model eligibility rules. |
| `research/ARCHITECTURE/MEMORY_AND_LEARNING.md` | KEEP | Data classes, retention and safe learning. |
| `research/ARCHITECTURE/DECISION_LOG.md` | KEEP, UPDATE ON DECISIONS | Append accepted/rejected decisions and rationale; do not use as progress tracker. |
| `research/ENGINEERING/API_CONTRACTS.md` | KEEP, CONSOLIDATE LATER | Draft domain contracts. During Phase 1, reconcile with execution-engine examples and source reuse spec into one canonical set of types. |
| `research/ENGINEERING/DATA_MODEL.md` | KEEP, CONSOLIDATE LATER | Proposed persistence schema; refine only when persistence is implemented. |
| `research/ENGINEERING/TEST_STRATEGY.md` | KEEP | Test layers, physical-device policy and benchmark methodology. |
| `research/ENGINEERING/PERFORMANCE_BUDGETS.md` | KEEP | Measurement plan and provisional budgets; replace assumptions with measurements. |
| `research/ENGINEERING/ARGON_DEVELOPMENT_HANDOFF.md` | KEEP | Phase 0 division of work and clean-build protocol between Argon, ChatGPT review pass, and Owner. |
| `research/research/ENGINEERING/ARGON/DONOR_BUILD_TEST_MATRIX.md` | KEEP (Phase 0 Deliverable) | Clean-checkout build, unit test, lint, APK, and CI failure root-cause matrix across all 5 donor repos. |
| `research/research/ENGINEERING/ARGON/DONOR_SOURCE_PROVENANCE_AND_LICENSE_LEDGER.md` | KEEP (Phase 0 Deliverable) | File-level provenance, 46-class audit of `agent-created-by-gemini`, and POM-verified transitive dependency license ledger. |
| `research/research/ENGINEERING/ARGON/DONOR_REVISION_AND_DIFF_REPORT.md` | KEEP (Phase 0 Deliverable) | Pinned commit vs branch head comparison and unmerged branch diff audit across all 5 donor repos. |
| `research/research/ENGINEERING/ARGON/INITIAL_TASK_BENCHMARK.md` | KEEP (Phase 0 Deliverable) | 26 deterministic task scenarios across 8 categories + 14 mandatory fail-closed architectural test contracts. |
| `research/research/ENGINEERING/ARGON/PHASE_0_TECHNICAL_SPIKES.md` | KEEP (Phase 0 Deliverable) | Evidence and decisions for all 5 technical spikes, overlapping donor ownership resolution, and Owner gate checklist. |
| `research/ROADMAP/PHASES.md` | KEEP, ALIGN WITH BLUEPRINT | Short phase index. Phase names/order must match the detailed blueprint and this file. |
| `research/ROADMAP/PHASE_IMPLEMENTATION_BLUEPRINT.md` | KEEP | Detailed phase steps, reuse mapping, technical gates and required evidence. |
| `research/ROADMAP/ACCEPTANCE_CRITERIA.md` | KEEP | Cross-cutting acceptance checklist; add phase-specific tests as implementation begins. |
| `research/REFERENCES/SOURCES.md` | KEEP | Central source link list; remove stale links and add sources used in new decisions. |

### Known documentation problems to resolve

1. **Phase numbering/order was inconsistent.** The compact roadmap previously placed voice before accessibility and omitted the deterministic-action phase split; the detailed blueprint has the correct dependency-oriented sequence. Align the short roadmap to the detailed sequence.
2. **Contracts are repeated.** `MASTER_ARCHITECTURE.md`, `AGENT_EXECUTION_ENGINE.md`, `API_CONTRACTS.md` and the reuse specification each describe overlapping action/result concepts. Keep their different purposes, but Phase 1 must designate one canonical source for normative Kotlin types and link to it from the others.
3. **Reuse/license language can be mistaken for clearance.** All reuse labels remain provisional until exact revisions, source headers, dependencies, native binaries, assets and licenses are checked.
4. **Some research is time-sensitive.** Revalidate Android API behavior, ADK/LiteRT-LM versions, repository heads, licenses and model availability immediately before adopting them.
5. **No broad deletion yet.** The current documents contain useful audit evidence. Do not delete or merge detailed records merely to reduce file count; consolidate only after preserving unique evidence and links.

## 6. Current next task

**Active phase:** Phase 0 — Repository archaeology, research and requirements freeze (Argon validation complete; ready for ChatGPT review pass and Owner gate sign-off).  
**Next task:** Verify latest Android CI; fix every compile/test failure; add deterministic Android adapter/journal tests; then run the first APK on the exact TECNO variant and record API 27 behavior. Do not call the current integration production-ready.

## 7. Blockers and unresolved decisions

- [ ] **Owner Gate 1:** Exact current target-device matrix and confirmation that API 27 (`TECNO IN6`) is mandatory Tier-1 supported hardware in Semantic Accessibility Tree mode (`research/research/ENGINEERING/ARGON/PHASE_0_TECHNICAL_SPIKES.md` §7).
- [x] Donor repositories' clean build/test/lint evidence at pinned revisions (`research/research/ENGINEERING/ARGON/DONOR_BUILD_TEST_MATRIX.md`).
- [x] Component-level provenance and POM-verified transitive dependency license ledger (`research/research/ENGINEERING/ARGON/DONOR_SOURCE_PROVENANCE_AND_LICENSE_LEDGER.md`), **pending Owner Gate 2:** explicit Owner license declaration (`Apache-2.0` / `MIT`) for the four first-party donor repositories that currently lack a root `LICENSE` file.
- [x] Canonical first-release task benchmark (26 scenarios) and independent verifiers drafted (`research/research/ENGINEERING/ARGON/INITIAL_TASK_BENCHMARK.md`), **pending Owner Gate 3:** Owner sign-off.
- [x] ADK Kotlin + LiteRT-LM versus custom Kotlin orchestration/inference spike completed (`research/research/ENGINEERING/ARGON/PHASE_0_TECHNICAL_SPIKES.md` §1, `ADR-006`).
- [x] API 27 visual observation fallback and realistic screenshot limitations resolved (`research/research/ENGINEERING/ARGON/PHASE_0_TECHNICAL_SPIKES.md` §2, `ADR-007`).
- [x] Local inference CPU/GPU/RAM constraints on `TECNO IN6` (`ARMv8.0-A` Cortex-A53 without `dotprod`/`fp16`; `libOpenCL.so` blocked; Vulkan `1.0.58` verified) resolved (`research/research/ENGINEERING/ARGON/PHASE_0_TECHNICAL_SPIKES.md` §3, `ADR-008`).
- [x] Wake-word feasibility under Android/OEM lifecycle constraints resolved (`research/research/ENGINEERING/ARGON/PHASE_0_TECHNICAL_SPIKES.md` §4, `ADR-009`).
- [x] Encryption/key-management (`AndroidKeyStore` AES-256-GCM) and backup/restore design resolved (`research/research/ENGINEERING/ARGON/PHASE_0_TECHNICAL_SPIKES.md` §5, `ADR-010`).
- [x] **Owner Gate 4 (limited scope):** Owner explicitly requested Hermes-inspired Kotlin source implementation on 2026-11-03. This authorizes the platform-neutral core and the scoped initial Android integration source tranche requested on 2026-11-03; it does not waive Owner Gates 1–3, establish physical-device acceptance, or authorize unrestricted features.

## 8. Session log

### 2026-11-03 — Hermes-inspired Kotlin core prototype

- Owner explicitly requested implementation of Hermes-inspired functionality in Kotlin for the destination Android agent. Created branch `feature/hermes-kotlin-core` from destination `main` HEAD `d1d4accc7191a42a7247ed35835dd3997541e741`.
- Inspected `NousResearch/hermes-agent` at `c314285f077e91937c995d419b9fa40af9eb4981`; confirmed root MIT licence. No Python source was copied verbatim. Added `research/IMPLEMENTATION/HERMES_KOTLIN_PORT_REVIEW.md` with source mapping, decisions, provenance and limitations.
- Added platform-neutral Kotlin source for typed agent contracts, capability registry, action-bound approval policy, bounded observe/plan/execute/verify orchestrator, memory/skill interfaces and scheduled-job interfaces.
- Added explicit limits: max 8 actions, bounded action/task timeouts, bounded UI snapshots, snapshot freshness, exact action digest/target/snapshot/expiry approval binding, capability availability and risk-ceiling checks, independent verification contract, declarative skills only.
- This is **not yet a buildable Android app**. The Kotlin/JVM core build and seven unit tests pass in GitHub Actions run `41609551659` (`:agent:core:compileKotlin`, `:agent:core:test`). No Android manifest/app module, Room implementation, Android adapters or physical-device tests have been added.
- The TECNO IN6 RAM/device-variant discrepancy in the Phase 0 spike remains unresolved; no hardware-specific model or memory budget was derived from this prototype.
- Added Kotlin/JVM Gradle configuration (`settings.gradle.kts`, root `build.gradle.kts`, `agent/core/build.gradle.kts`), GitHub Actions workflow `.github/workflows/kotlin-core.yml`, and seven deterministic tests in `core/src/test/kotlin/com/personalagent/core/AgentCoreTest.kt`.
- GitHub Actions run `41609551659` passed both `:agent:core:compileKotlin` and `:agent:core:test` on commit `199cd7120d42534a642589aa332ce41f366d789f` using Gradle 8.10.2, JDK 17 and Kotlin 2.0.21.
- No Android application build or physical-device test has been run. Next: resolve device matrix/RAM discrepancy and remaining owner gates before creating Android app/Room integration.

## Historical session log

### 2026-11-02 — Documentation control baseline

- Reviewed the existing README, research index, phase roadmap, detailed implementation blueprint, source-by-source reuse specification, architecture documents, engineering contracts, acceptance criteria and research references.
- Identified phase-order/numbering drift between the compact roadmap and detailed blueprint.
- Identified overlapping but distinct contract descriptions; deferred normative contract consolidation to Phase 1 rather than deleting evidence prematurely.
- Established this file as the mandatory session-start checklist and canonical progress ledger.
- Updated README navigation/working rules to point to this file; aligned the short phase roadmap with the detailed dependency-oriented phase sequence.
- Initially documented home-security monitoring as an optional workstream in this agent. The owner then changed scope: remove it from this agent and defer it to a standalone Android app that may be installed on the same phone. The retained research document is reference material only.
- Updated the master architecture, hardware capability notes, Android platform constraints, README, roadmap and source index. No application code, dependencies, build configuration or CI workflow changed.
- No app implementation or physical-device test was performed. Implementation authorization remains **NOT GRANTED**.
- No home-security runtime, integration, module or implementation is planned in this repository. Update navigation to label the retained document as a separate future project reference.
- Repaired literal escaped line breaks introduced during document updates and re-fetched the affected files.
- Documentation commits for the new workstream include `c4731c3`, `baa48ee`, `e80cc05`, `720e7a2`, `378e4bd`, `3b43a67`, `624314f`, `ab3a099`, `734aa8b`, `375d131`, `73d1586`, `bc70a45`, `903027f`, `3747edf`, `ebd2c91` and `e6fd937`.
- No application code, dependencies, build configuration, CI or physical-device tests changed.
- Next: close the core agent's Phase 0 evidence gaps. Do not spend this project's Phase 0 effort on camera uptime, face recognition or remote home monitoring.

### 2026-11-03 — Gemini Argon development handoff and repository revalidation

- The owner instructed that development should begin. Recorded authorization for phased development; Phase 1 source implementation remains blocked until Phase 0 exit evidence is complete and reviewed. No phase gate was waived.
- Re-read this status file first, then inspected the current `main` tree and HEAD. Current destination `main` HEAD is `5b86207998c66d106a93ae551101e233af090701`; the repository remains documentation-only with no destination Android application source.
- The latest destination commit status is pending with zero reported status checks. This is not a green CI result.
- Revalidated donor branch heads. `agent-created-by-gemini`, `mobile_agent`, `skill-agent` and `agent-1` still match the recorded pins. `tecno-os` advanced four commits to `d2aeac357141e75b601a935869abb8944ad0d41d`; the newly added GPU APK validation report must be reviewed and reconciled.
- Observed current donor CI evidence: `agent-created-by-gemini` latest visible run for its pinned commit succeeded, but its workflow makes release build non-blocking and does not establish physical-device behavior. `mobile_agent` has no recent Actions runs returned. `agent-1` latest visible run for its pinned commit failed. `skill-agent` is specification-only.
- Created `research/ENGINEERING/ARGON_DEVELOPMENT_HANDOFF.md` on branch `phase-0-argon-handoff`, assigning Argon clean-checkout builds/tests, donor provenance/licence analysis, benchmark scenarios and technical spikes; assigning ChatGPT architecture/evidence reconciliation, safety review, integration control and phase-gate verification.
- No destination app source, Gradle configuration, dependency, CI workflow or physical-device test was added or changed. No donor code was transplanted.
- Reviewed the newer TECNO IN6 GPUProbe report. Updated `research/ARCHITECTURE/HARDWARE_CAPABILITIES.md` and `research/RESEARCH/MODEL_AND_INFERENCE_RESEARCH.md` with the verified ordinary-APK Vulkan vector-add result and explicit limitations: no neural-network runtime is proven; APK OpenCL loading fails; repeated-dispatch evidence remains provisional due to a one-time-submit command-buffer reuse issue. Updated the tecno-os reference pin in the reuse specification to its current branch head.
- Next: execute the Argon handoff; confirm exact target-device matrix with the owner; inspect the newer `tecno-os` GPU report; complete donor clean-build/test/licence evidence and benchmark before Phase 1.

### 2026-11-03 — Gemini 4 Argon Phase 0 donor validation execution

- Cloned all 5 donor repositories at their exact recorded commits into isolated `/tmp/phase0-donors/` worktrees (`agent-created-by-gemini` @ `90f5cf3ca0eca324f91a43ba96fab72c2e2de5ed`, `mobile_agent` @ `863723f7aa4b42137ff4ce07e26e541e402ff5a8`, `skill-agent` @ `86cfa6960b1341ed53b987f5a50b1c623ec18ad4`, `agent-1` @ `6d0c3002fc4c716dea2ffcba31339f0de79fd087`, and `tecno-os` @ `c8a23e359de5b30297ca731f713b89077e056cfc` / `d2aeac357141e75b601a935869abb8944ad0d41d`).
- Executed clean-checkout builds, unit tests, `lintDebug`, release builds, and dependency reports (`research/research/ENGINEERING/ARGON/DONOR_BUILD_TEST_MATRIX.md`):
  - `agent-created-by-gemini`: `:agent:app:testDebugUnitTest` **PASS** (13 suites, 74 tests, 0 failures); `:agent:app:assembleDebug` **PASS** (`18,858,256` bytes, no `libllama-bridge.so` packaged); `:agent:app:lintDebug` **FAIL** (1 error `PermissionImpliesUnsupportedChromeOsHardware`, 86 warnings); `:agent:app:assembleRelease` **FAIL** (R8 syntax error at `app/proguard-rules.pro:14:29`, masked in GitHub Actions Run `40505149792` by `continue-on-error: true` and `|| true`).
  - `mobile_agent`: `:agent:app:assembleDebug` **PASS** (`17,846,968` bytes, packages `libllm_inference_engine_jni.so` which links blocked `libOpenCL.so`); `test` **PASS** with only **1 test** across the entire repo (`:ai` `PlannerTest`, while 5 of 6 modules have `NO-SOURCE`); `:agent:app:lintDebug` **FAIL** (`QueryAllPackagesPermission`); `:memory` is missing KSP/KAPT `room-compiler`, so `AgentDatabase_Impl` is never generated.
  - `agent-1`: `:agent:core:test` (5 tests) and `:infrastructure:test` (6 tests) **PASS** (11 JVM tests total); `:agent:app:testDebugUnitTest`, `:agent:app:lintDebug`, and `:agent:app:assembleDebug` **FAIL** (reproducing GitHub Actions Run `37294356933` Step 9 failure) due to 3 stacked defects: missing root `gradle.properties` (`android.useAndroidX=true`), 512 MiB default heap GC thrash at `:agent:app:mergeExtDexDebug`, and missing `agent/app/src/main/res/` directory at `:agent:app:processDebugResources`.
  - `skill-agent`: verified specification-only (**NOT RUN**).
  - `tecno-os`: ran all 9 host Python test suites (`tests/host/test_*.py`): 4 **PASS** (`test_boot_safety`, `test_initramfs_shebang`, `test_inventory`, `test_kernel_config`), 5 **BLOCKED** by missing Linux kernel cross-compilation tools (`cpp`, `cpio`, `aarch64-linux-gnu-gcc`) in the Android container.
- Audited all 46 Kotlin source files in `agent-created-by-gemini` (`research/research/ENGINEERING/ARGON/DONOR_SOURCE_PROVENANCE_AND_LICENSE_LEDGER.md`), documenting what PR #3 (`1c7d8aa`) fixed vs remaining critical defects (`StepVerifier.verify()` lines `93-118` still returning unconditional `StepStatus.SUCCESS` for `ControlActuator`, `Delay`, `StartTimer`, `WebResearch`, and `else`; `UiSnapshot.monotonicTimestampMs` still defaulting to `System.currentTimeMillis()`; fake empty-string SHA-256 hashes `e3b0c442...` in `ModelCatalog`; `0.8 * sizeBytes` download check; 34 unrecorded classes audited and classified).
- Produced all 5 Phase 0 deliverables (`DONOR_BUILD_TEST_MATRIX.md`, `DONOR_SOURCE_PROVENANCE_AND_LICENSE_LEDGER.md`, `DONOR_REVISION_AND_DIFF_REPORT.md`, `INITIAL_TASK_BENCHMARK.md`, `PHASE_0_TECHNICAL_SPIKES.md`) and recorded `ADR-006` through `ADR-010` in `research/ARCHITECTURE/DECISION_LOG.md`.
- No destination Android application code, Gradle build files, or merges to `main` were performed.
- Next: ChatGPT review pass and Owner confirmation of the 4 gate items in `research/research/ENGINEERING/ARGON/PHASE_0_TECHNICAL_SPIKES.md` §7 before starting Phase 1.



### 2026-11-03 — Initial Android execution layer and Argon archive consolidation

- Owner explicitly requested implementation of the Android execution layer and consolidation of Argon documents into one folder.
- Added the :app Android application module with minSdk 27, target SDK 28, compile SDK 35, Java 17, Kotlin 2.0.21, Room 2.6.1/KSP and WorkManager 2.9.1. Added a minimal onboarding Activity and user-enabled AccessibilityService declaration.
- Added AgentAccessibilityService, bounded semantic AndroidAccessibilityObserver, typed AndroidActionDispatcher (launch/click/set-text only), AndroidPostconditionVerifier, AndroidCapabilityRegistry, RoomTaskStepJournal (WAL), AES-256-GCM AndroidKeystoreJournalCrypto, and unique-work AgentWorkScheduler.
- Journal note payloads are encrypted; task/action/state metadata remains queryable. Auto-backup is disabled in the manifest. No UI is yet wired to approve a protected action; the core must continue to fail closed without a real explicit-approval UI.
- Added Android debug APK assembly to GitHub Actions. Build result for this new app tranche: PENDING / NOT YET VERIFIED at the time this entry was written. Physical TECNO API 27 validation: NOT RUN.
- Consolidated Argon handoff and five Phase 0 deliverables under research/research/ENGINEERING/ARGON/ and added an archive index. Old top-level ENGINEERING copies were removed after copying the source documents.
- Known limitations: no hardware torch/vibration/Wi-Fi/Bluetooth adapters; no end-user approval screen; no app-wide stop control; no durable scheduler job-state store; no Android instrumentation tests; no crash-recovery reconciliation for in-flight non-idempotent actions; no physical device run.
- Next: inspect latest GitHub Actions run on this branch, fix Android compile errors, then add app/journal/observer tests before device acceptance.


### 2026-11-03 — Expanded GitHub repository research

- Added `research/RESEARCH/ANDROID_AUTONOMOUS_AGENT_REPOSITORY_DEEP_DIVE_2026-11-03.md`.
- Inspected 17 repositories at pinned snapshots or default-branch snapshots: OpenDroid, Mobilerun, Llamatik, Hermes, AndroidWorld, Mobile-Agent, Maestro, Appium UiAutomator2 server, OpenATX uiautomator2, LangGraph and Google LiteRT samples.
- Source-level inspection included OpenDroid PlanManager/AgentLoop/PlanValidator/ReEvaluationEngine/AccessibilityNodeTraversal/ActionDispatcher/Room history/model downloader/click-targeting instrumentation tests; Mobilerun Manager/Executor/shared state/tool registry/UI state retry; Llamatik runtime/permission/confirmation/workflow/memory; Hermes loop/approval/SQLite state; AndroidWorld M3A; LiteRT phone tools and offline voice assistant architecture.
- Recorded repository commit pins and root license observations, and separated direct Android-native code from host/ADB automation, benchmark frameworks, model/runtime examples and conceptual references.
- Key architecture conclusion: build one durable observe-plan-validate-approve-dispatch-observe-verify loop; persist every step transition; never replay uncertain non-idempotent actions; require unique target grounding and independent postcondition verification.
- Critical API 27 finding: AccessibilityService.takeScreenshot is API 30 and global screenshot action is API 28. The API 27 baseline must use semantic accessibility observation and user-consented MediaProjection only if visual capture is required. Cite official API documentation and AOSP restricted screen reading references in the deep dive.
- Key compatibility finding: the inspected Google LiteRT offline voice assistant sample requires minSdk 31 and its sample models occupy approximately 2.81 GB; it is a modern-device reference, not an API 27 baseline.
- No application source code changed in this research session. No new build or physical-device test was run. The previous successful CI evidence remains run 41612516639 at commit e3ea4db83d33cc127097427dcabc5358de559d3c.
- Next task: review the research decisions against the current destination source, then implement the durable Room task state machine and crash-recovery reconciliation before expanding hardware or model features.


### 2026-11-03 — Research status correction

- Corrected the Android build status: CI run 41612516639 passed :agent:core:compileKotlin, :agent:core:test, :agent:app:testDebugUnitTest (3 tests) and :agent:app:assembleDebug at source commit e3ea4db83d33cc127097427dcabc5358de559d3c. Later app source changes were not made; documentation updates followed.
- Phase 1 is a buildable prototype with CI passing, not physically accepted. Phase 4 has a platform-neutral orchestrator prototype, but no end-to-end task UI, durable recovery or device acceptance.
- Research index now links the 2026-11-03 17-repository deep dive. The LiteRT samples snapshot is pinned to e98147beea80ee90f7f74ed975bca9c78667d87c.
- Exact API 27 screenshot limitation is documented: AccessibilityService.takeScreenshot requires API 30; global screenshot action requires API 28. API 27 should use semantic tree observation, with visual capture only through an explicitly consented MediaProjection flow if needed.
- No code changes or new tests were performed in this research session.
- Next engineering task: replace the append-only Room journal with a transactional task/step state machine and implement safe recovery reconciliation before adding more action tools.


### 2026-11-03 — Android, speech and local-inference repository expansion

- Expanded the GitHub deep dive from 12 to 17 repositories: added MobileUse, Mobilerun Portal, Google AI Edge Gallery, sherpa-onnx and llama.cpp, with commit pins and file-level findings.
- Mobilerun Portal confirms a MediaProjection screenshot path for API 26–29 (user consent required), while AccessibilityService.takeScreenshot is API30. It also demonstrates dispatchGesture, which is API24 but requires android:canPerformGestures=true. The destination manifest does not currently request that capability; gesture dispatch remains NOT IMPLEMENTED.
- Mobilerun Portal is AGPL-3.0. Treat as reference-only; do not transplant its code without an explicit licensing decision. Its reverse control, auto-accept and streaming features are outside this app's scope.
- sherpa-onnx provides Android Kotlin/JNI streaming ASR and keyword spotting examples with minSdk21. It is now the highest-priority API27 speech proof-of-concept candidate, subject to model licence, ABI, memory, CPU and accuracy tests.
- Google AI Edge Gallery sample sets minSdk31; llama.cpp's current Android example sets minSdk33. Neither sample is API27 compatible as shipped. A native runtime's cross-platform core does not prove old Android compatibility.
- No application source code changed. No build, voice or physical-device test was run in this research pass.
- Next engineering task remains durable Room task/step state transitions and crash-recovery reconciliation. After that, create isolated POCs for ADK Kotlin cloud-model orchestration and sherpa-onnx push-to-talk ASR before selecting model/runtime dependencies.


### 2026-11-03 — Full agent architecture designed

- Added `research/DESIGN/FULL_AGENT_ARCHITECTURE.md` as the integrated product and engineering architecture baseline.
- Design covers product behavior, component/module boundaries, single authoritative control loop, typed contracts, durable task state machine, crash recovery, model routing, ADK Kotlin evaluation boundary, capability/permission model, risk and approval, accessibility/API 27 constraints, hardware adapters, speech, memory, declarative skills, scheduler, Room schema, privacy/security, model artifact lifecycle, evaluation corpus, release gates and phased implementation backlog.
- Architecture is based on the 17-repository deep dive and the existing API contracts, data model, performance budgets and test strategy. It does not claim the design is implemented.
- No application source code changed and no build or device tests were run in this design session.
- Immediate implementation priority remains: transactional Room task/step/action journal, startup recovery and uncertain-dispatch reconciliation, exact-target guard tests, then explicit approval UI.


### 2026-11-03 — Durable execution, REA and self-tool implementation tranche

- Created branch `feature/durable-engine-rea-tool-factory` and draft PR #4: https://github.com/iam816anonymous-commits/personal-autonomous-android-agent/pull/4
- Added platform-neutral durable action lifecycle contracts: `PREPARED → DISPATCHING → DISPATCHED → VERIFIED`, with explicit `REJECTED`, `FAILED` and `OUTCOME_UNKNOWN` terminal/recovery states.
- Added startup process-death reconciliation rules. A process death during `DISPATCHING` or `DISPATCHED` becomes `OUTCOME_UNKNOWN`; no automatic replay is allowed. The protocol only permits retry after safe rejection or verified non-application under bounded conditions.
- Added a Room `action_attempts` table, unique task/action key, migration 1→2, transactional insert/transition methods and a recovery reconciliation query. Sensitive notes are encrypted through the existing JournalCrypto boundary.
- Wired the durable Room journal into AgentApplication and made AgentOrchestrator require a durable journal. The orchestrator now commits action intent and dispatch state before calling the Android dispatcher, then commits dispatch result and verification separately.
- Added REA app-surface data contracts for app identity/version, screen fingerprints, transitions and interaction probes. Skill certification rejects mismatched app versions, unverified probe evidence, insufficient confidence and invalid/unapproved skill manifests.
- Added a declarative SelfToolFactory. It can compose only registered action kinds and approved capabilities; it cannot create executable Kotlin/Java/shell/DEX/JNI code. Tool creation is bounded, risk checked and user approved.
- Added tests for uncertain non-idempotent dispatch recovery, state transition validation, tool factory rejection/creation and REA evidence gating; existing orchestrator tests now inject a durable journal.
- Added `.github/workflows/android-agent-ci.yml` to run core compilation/tests, Android unit tests and debug APK assembly on PRs and main pushes.
- CI evidence: **PENDING / NOT VERIFIED**. The GitHub connector has not returned a workflow run for the new workflow/PR yet. No local Gradle execution was available in this session.
- Physical TECNO API 27 installation, AccessibilityService interaction, Room migration on-device and REA probes: **NOT RUN**.
- Current work is a source implementation tranche, not a complete REA runtime, a UI-integrated tool builder, or a release-ready app.
- Next exact work: obtain a CI run and fix all compile/test failures; then add app-facing REA capture/probe workflow, explicit approval UI and durable task recovery integration. Do not merge PR #4 until CI passes and review is complete.


#### Implementation tranche follow-up

- Added `ReverseEngineeringSession`: bounded screen fingerprint capture, transition graph collection, app-package scoping and secret-redacted labels from the existing accessibility observer.
- Added `AndroidReverseEngineeringController` and exposed it from `AgentApplication`. A caller must explicitly begin inspection for a selected installed package, capture current foreground screens, record verified probes and finish/cancel the session.
- Tightened self-tool creation: every step must reference a registered action and its exact argument schema; typed parameter bindings must match the action argument types.
- Added a REA test for screen fingerprint transitions and password/OTP redaction.
- Found and reused the existing `.github/workflows/kotlin-core.yml` workflow. It runs on `feature/**` pushes and pull requests to main, including core tests and Android APK assembly. The newly added duplicate workflow was removed.
- Latest PR #4 head at the time of this update: `fa380d901c5e0fe4b0bc846eda32be5dea66506b`. CI result remains **NOT VERIFIED** through the available status endpoint; do not claim green.


#### CI validation and first integration fix

- GitHub Actions run **41644788869** on the earlier implementation head passed `:agent:core:compileKotlin` and `:agent:core:test`, then failed at `:agent:app:testDebugUnitTest` because `AndroidAgentRuntime` did not pass the newly required `DurableExecutionJournal` constructor dependency. APK assembly was skipped on that failed attempt.
- Fixed the composition root: `AndroidAgentRuntime` now receives and injects `RoomDurableExecutionJournal` into `AgentOrchestrator`.
- GitHub Actions run **41645034090** then passed all steps:
  - `:agent:core:compileKotlin` — PASS
  - `:agent:core:test` — PASS
  - `:agent:app:testDebugUnitTest` — PASS
  - `:agent:app:assembleDebug` — PASS
- This is CI evidence for compilation, JVM/unit tests and APK assembly only. It is not proof of installation or runtime behavior on TECNO API 27.
- The new REA controller is exposed from `AgentApplication`; end-user REA controls/screens and durable generated-tool persistence remain follow-up work.
- PR #4 was subsequently merged into main. Physical device validation, actual third-party app exploration and Room migration testing on a device remain NOT RUN.


### 2026-11-03 — Resume implementation: live REA capture and user-facing inspection flow

- Re-read this canonical status file and inspected the current main branch. PR #4 is already merged at merge commit `91e61c9fa3bc92c79e4db89a230e1e363e5061ec`; the earlier session-log statement that it remained draft/unmerged was stale and has been corrected.
- Confirmed that the REA core contracts, screen fingerprinting, transition collection, evidence-gated skill certification and declarative self-tool factory are present. Confirmed that generated tools are currently in-memory only and that no executable-code generation is permitted.
- Found a functional integration defect: the previous REA controller attempted to capture a screen only when called from the agent app, which meant it could not observe a different target app while that target was foregrounded.
- Fixed the capture path: AccessibilityService now forwards visible foreground-package events to a single app-scoped listener; the REA controller captures bounded, redacted accessibility snapshots on a dedicated serial worker, scoped to the selected package and throttled to avoid event floods.
- Added a basic REA user flow to MainActivity: enumerate launchable installed apps, select a target, begin an inspection, launch the target, explore it manually, return and finish/cancel, and display the discovered screen/transition/probe/limitation counts. No skill is falsely reported as created.
- Changed files: `agent/app/src/main/java/com/personalagent/android/accessibility/AndroidAccessibilityObserver.kt`, `AgentAccessibilityService.kt`, `agent/app/src/main/java/com/personalagent/android/rea/AndroidReverseEngineeringController.kt`, and `agent/app/src/main/java/com/personalagent/android/MainActivity.kt`.
- Commits created directly on main: `e9859e9`, `03e93ab`, `d29cb36`, `99e98e0`.
- Validation: source changes committed; **CI NOT YET VERIFIED** for the new main commits. No local Gradle execution was available through this session. Physical-device REA capture and app-version drift behavior remain **NOT RUN**.
- Known limitations remain: REA currently collects passive screen fingerprints/transitions, not automated exploratory probes; no persistent REA session/skill store or user-facing probe approval flow exists; generated-tool persistence/management UI is not implemented; the core planner is not yet connected to a task-entry UI.
- Next exact implementation tasks: verify CI for the new main commits; add persistent, versioned REA behavior/skill and generated-tool records with migration tests; build explicit user approval and revocation UI; then add safe probe execution using only the already-registered typed actions and independent postcondition verification. Continue the phase order; do not add wake-word or model runtime before these deterministic controls are proven.
