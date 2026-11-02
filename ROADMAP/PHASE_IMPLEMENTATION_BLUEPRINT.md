# Phase-by-phase implementation and reuse blueprint

**Status:** Research baseline; source-level reuse decisions remain provisional until each candidate passes build, test, API-compatibility and licence checks.  
**Date:** 2026-11-02  
**Product target:** Personal-use, sideloaded Android agent. API 27 is a desired compatibility baseline, not a promise of equal capability on every Android version.

## 1. Reuse policy

Every candidate component receives one of three decisions:

- **DIRECT** — copy/adapt only package names, interfaces and minimal dependencies; preserve working implementation and tests.
- **ADAPT** — reuse the algorithm or a bounded part of the code, but change lifecycle, API contracts, security or compatibility.
- **REFERENCE** — study the design/algorithm and implement our own Android/Kotlin version.
- **REJECT** — unsuitable, unmaintained, incompatible, unsafe or licensing cost outweighs value.

A README is not evidence that a feature works. For every DIRECT/ADAPT decision, record exact repository, branch/commit, file path, licence, dependency versions, minSdk, tests, known defects and the verification command. Do not copy code from external projects until exact code and dependency licences have been checked.

## 2. First-party repositories to audit

| Repository | Candidate contribution | Initial disposition |
|---|---|---|
| `iam816anonymous-commits/mobile_agent` | Modular Kotlin app; `:core`, `:ai`, `:accessibility`, `:tools`, `:memory`; screen tree, planning and local model integration | **Primary integration candidate.** App and accessibility modules currently declare minSdk 26, so API 27 is plausible. Verify every transitive dependency and actual source behavior. |
| `iam816anonymous-commits/agent-created-by-gemini` | Accessibility service, UI node snapshots/differencing, action adapters, deterministic intent routing, local llama bridge, approval and remote bridge | **Primary component donor.** Audit actual implementations for simulated success, fabricated device telemetry, sensitive event logging, lifecycle leaks and test coverage before copying. |
| `iam816anonymous-commits/skill-agent` | Pure Kotlin core, capability-scoped skills, permission model, execution engine, checkpoint/recovery and resource management | **Core architecture/component donor.** README targets API 27–36+; verify build and implementation before integration. Avoid importing its entire runtime if it duplicates the selected orchestrator. |
| `iam816anonymous-commits/agent-1` | Pure Kotlin domain contracts, state machine, structured evaluator and source provenance | **Selective reference/reuse** for evaluation contracts and evidence tracking; not the phone-control runtime. |
| `iam816anonymous-commits/tecno-os` | Device-specific TECNO IN6 hardware/kernel research | **Reference only** for device facts where relevant. Do not merge Linux/kernel code into the Android app. |
| `iam816anonymous-commits/personal-autonomous-android-agent` | Current research/specification repository | **Destination** for decisions, phase plans, test evidence and implementation. It is documentation-first at this baseline. |

### Required source audit before copying
1. Pin a commit SHA for each source repository; do not integrate from a moving branch.
2. Inspect Gradle wrapper, AGP/Kotlin/JDK versions, version catalog, minSdk/targetSdk, ABI filters and native libraries.
3. Inspect manifest permissions, exported components, accessibility XML, foreground services, receivers and network security config.
4. Trace every advertised capability from UI/entry point to real Android API and verify the returned result reflects actual device state.
5. Find all TODO/FIXME/mock/fake/simulated-success paths and hardcoded device assumptions.
6. Inspect tests and add missing tests before transplanting code.
7. Compare duplicate implementations and choose one canonical owner for each responsibility.
8. Record licence and notices for code, dependencies, native libraries, model weights and datasets.

## 3. Phase sequence

## Phase 0 — Repository archaeology, research and requirements freeze

**Goal:** Know what already works, what can be reused and what must be built.

**Steps**
1. Audit the three primary donor repositories above at source level.
2. Audit relevant branches/commits, not just README files.
3. Produce a component inventory: file/class, responsibility, dependencies, API level, tests, defects and reuse label.
4. Build donor repositories from clean checkout; run unit tests, lint, static analysis and assembleDebug.
5. Test existing APKs on the physical API 27 device and one modern Android device where available.
6. Compare the three candidates for overlapping agent loops, AccessibilityService implementations, action models, permissions and persistence.
7. Create a single target module map and decide which repository supplies each component.
8. Freeze the first task benchmark: launch app, navigate settings, search UI, enter text into a user-selected field, scroll, back/home, recover from a changed screen, and safely stop.
9. Write threat model, privacy rules, capability matrix and compatibility table.
10. Record all unresolved assumptions as explicit experiments.

**Use from your repositories:** all three donor repositories for audit; `agent-1` for structured evaluation/provenance patterns.

**External references:** Android AccessibilityService documentation; Android platform/lifecycle documentation; AndroidWorld for task/verifier design; Mobilerun/MobileUse for execution-loop comparisons.

**Exit evidence:** clean build/test reports for donor projects; source-level reuse matrix; licence/dependency inventory; target architecture; physical-device test plan. No feature is considered implemented in this phase.

## Phase 1 — Buildable Android shell and stable contracts

**Goal:** Establish a reliable app foundation before adding autonomy.

**Steps**
1. Create/choose the Android app shell and pin Gradle, AGP, Kotlin, JDK and SDK versions.
2. Define pure-Kotlin contracts: `Goal`, `Plan`, `PlanStep`, `DeviceSnapshot`, `DeviceAction`, `ActionResult`, `Postcondition`, `VerificationResult`, `Capability` and `TaskState`.
3. Define cancellation, timeout, correlation ID and idempotency semantics.
4. Add capability profiler: API level, ABI, physical memory estimate, available memory, storage, runtime permissions, enabled accessibility service, supported APIs and model-runtime compatibility.
5. Add onboarding/status UI showing exactly which capabilities are enabled and unavailable.
6. Add global stop/cancel control and visible task state.
7. Add redacted structured logging and durable task journal interfaces.
8. Add CI for assembleDebug, unit tests, lint, dependency checks and licence report.
9. Validate install, launch, upgrade and process restart on physical devices.

**Reuse:** candidate UI shell and stable domain classes from `mobile_agent`; pure Kotlin contracts and state machine from `skill-agent` or `agent-1`, but choose one canonical model. Do not merge duplicate classes wholesale.

**Reference:** Android architecture guidance; Kotlin coroutines cancellation and structured concurrency.

**Exit evidence:** clean build; all unit tests/lint pass; no fabricated capability values; app installs and restarts on target devices; task state survives process death without replaying actions.

## Phase 2 — Accessibility observation bridge

**Goal:** Reliably understand the current screen before attempting autonomous interaction.

**Steps**
1. Implement/port one AccessibilityService with explicit user enablement and clear enabled/disabled status.
2. Define service event filters and lifecycle behavior; keep callbacks lightweight.
3. Convert AccessibilityNodeInfo trees into immutable, bounded snapshots with stable per-snapshot IDs.
4. Capture package/activity, node class, text/description, resource ID, clickable/editable/scrollable flags, bounds and supported actions.
5. Prune irrelevant nodes and cap depth/node count to control memory and model context.
6. Redact password, OTP and sensitive fields before persistence or model use.
7. Handle null/stale nodes, detached windows, rapid screen changes, overlays, WebViews, custom canvases and secure windows.
8. Add screenshot/vision observation only as a separate, permission-aware fallback; never assume universal screenshot access.
9. Add snapshot diffing and event debouncing.
10. Build a diagnostic screen to inspect the observed tree and compare it with the real device screen.

**Reuse:** `agent-created-by-gemini` AccessibilityService and node-differencing candidates; `mobile_agent` accessibility module and UI-tree parsing. Compare implementations and select one. `skill-agent` target resolution may be adapted if its contracts fit.

**Reference:** official Android AccessibilityService lifecycle/API; Mobilerun Portal for Android-side service design; OpenGUI/UI-TARS for complementary visual grounding.

**Exit evidence:** fresh snapshots on target apps; stale-node detection; correct redaction; no service crash/leak under rapid navigation; physical-device screenshots/tree evidence; documented API 27 and modern-device differences.



## Dedicated hardware capability workstream (Phase 3A)

This is a required workstream, not an optional enhancement. The agent must discover and use supported phone hardware through a typed hardware registry and permission-gated adapters. Scope, implementation steps, API 27 caveats, reuse candidates and acceptance criteria are documented in [Hardware capabilities](../ARCHITECTURE/HARDWARE_CAPABILITIES.md).

First audit `agent-created-by-gemini` classes `HardwareManager.kt`, `DeviceCapabilityDetector.kt` and `ActuatorController.kt`; compare with `skill-agent` capability authorization and `mobile_agent` device profiling. Reuse verified code where possible; otherwise adapt or implement against the common hardware contracts. Hardware is not considered complete until physical-device effects are verified.

## Phase 3 — Deterministic device action layer and verification

**Goal:** Make individual actions safe, bounded and observable before giving a model control.

**Steps**
1. Define finite typed actions: launch app/intent, tap node, long press, set text, scroll, back, home, wait, observe and stop.
2. Resolve targets only against a fresh snapshot; reject ambiguous or stale targets.
3. Prefer explicit intents/deep links for deterministic operations; use AccessibilityService when semantic UI interaction is required.
4. Validate action arguments, capability availability, package scope, permissions and risk.
5. Add per-action timeout, cancellation, retry policy and idempotency classification.
6. Execute one action at a time; return truthful typed results (success, rejected, timed out, unsupported, uncertain, failed).
7. Re-observe after each mutating action.
8. Verify a defined postcondition; never equate API return success with task success.
9. Add approval gate for sending messages, payments, deleting data, changing security settings and other consequential actions.
10. Add recovery for app crash, unexpected screen, node drift, timeout and permission revocation.

**Reuse:** `agent-created-by-gemini` action executor, safety gate and recovery classes after fake-success audit; `skill-agent` capability manager and universal skill executor. `mobile_agent` tools only where contracts are compatible.

**Reference:** AndroidWorld task/action/verifier patterns; Mobilerun atomic action/tool contracts; Android API documentation.

**Exit evidence:** deterministic scripted tasks pass without an LLM; every action has a before/after trace; stale targets never trigger blind taps; sensitive actions cannot bypass approval; tests prove false-success detection.

## Phase 4 — Single-agent orchestration and autonomous execution loop

**Goal:** Convert a user goal into a bounded, resumable, verifiable task.

**Steps**
1. Implement one authoritative orchestrator; do not start with a swarm of agents.
2. Separate goal understanding, planning, next-action selection, policy checks, execution, observation, verification and recovery.
3. Keep plans as structured data with explicit preconditions, action, postconditions, risk and timeout.
4. Permit only one atomic action per model decision.
5. Enforce deterministic policy outside the model.
6. Add loop limits, wall-clock budget, retry budget, maximum action count and user cancellation.
7. Persist checkpoints and approval state; resume only after re-observation.
8. Replan when the UI differs from expected state; never blindly replay a non-idempotent action.
9. Report completed, partially completed, blocked and failed states distinctly.
10. Run a fixed benchmark of multi-step tasks and measure completion, unsafe action, recovery and latency rates.

**Reuse:** `skill-agent` workflow/checkpoint/recovery patterns; `agent-created-by-gemini` routing and verifier candidates; `mobile_agent` planner/reflection components if tested.

**External references:** ADK Kotlin for typed tools, session resumability and human-in-loop; MobileUse for planner/operator/reflector separation; Mobilerun Manager→Executor loop; LangGraph only as a state-machine design reference.

**Exit evidence:** benchmark results; process-death recovery; bounded loops; zero unauthorized actions in the test suite; success requires postcondition evidence.

## Phase 5 — Voice input and command interpretation

**Goal:** Speak a request and convert it into a structured task reliably.

**Steps**
1. Implement tap-to-talk first; show recording/listening state and explicit cancel.
2. Evaluate Android SpeechRecognizer availability and on-device/offline recognition on each target OS/device; do not assume API 27 offers the same offline service.
3. Add a replaceable speech-to-text interface with provider capability reporting.
4. Support English first; test Telugu and Hindi separately for accuracy and availability.
5. Normalize transcript, preserve uncertainty and ask for clarification when intent/target/amount/recipient is ambiguous.
6. Separate transcription from intent parsing and execution.
7. Avoid retaining raw audio by default; redact transcripts before logs/memory.
8. Test permission denial, interruption, Bluetooth route changes, screen off, app backgrounding, process death and OEM-specific behavior.

**Reuse:** inspect any existing voice/speech code in `agent-created-by-gemini`, `mobile_agent` and `meeting-summarizer`; only reuse components whose lifecycle and recognition result are verified. Meeting recording/transcription code is not automatically suitable for command capture.

**Reference:** Android SpeechRecognizer/RecognizerIntent and microphone/foreground-service documentation; on-device ASR model documentation if Android speech services are insufficient.

**Exit evidence:** repeatable physical-device speech tests; transcript confidence/unsupported state is surfaced; cancellation works; offline claims are backed by airplane-mode tests.

## Phase 6 — Local model runtime, model catalogue and tool calling

**Goal:** Add local reasoning without letting model/runtime instability compromise device control.

**Steps**
1. Prototype at least two approaches independently: ADK Kotlin + LiteRT-LM, and a minimal custom `ModelProvider` adapter around a supported inference runtime.
2. Test ML Kit Gemini Nano separately; do not assume it supports tool calling. Current ADK documentation lists LiteRT-LM tool support, while ML Kit tool calling is not yet supported in the Kotlin integration.
3. Verify exact ADK artifact/version, Android dependency, JDK/Kotlin requirements, minSdk and transitive dependency compatibility. Official Android ADK setup currently documents minSdk 24; current ADK Kotlin releases and Android modules have version differences that must be pinned and built, not guessed.
4. Test model formats and exact model cards; never assume arbitrary GGUF models run in LiteRT-LM without conversion/support.
5. Create model registry with file size, checksum, quantization, supported language, context limit, runtime backend, licence and measured peak memory.
6. Download only on explicit user action; use resumable downloads, temporary file, checksum verification and atomic activation.
7. Profile cold start, token speed, peak PSS/RSS, memory pressure, thermal throttling and battery on actual devices.
8. Use measured headroom, not advertised dynamic/virtual RAM, for eligibility.
9. Keep model output as an untrusted proposal; validate every tool call in the deterministic action layer.
10. Add unload/switch/fallback and low-memory graceful failure.

**Reuse:** `agent-created-by-gemini` local llama/native bridge only if native ABI, licence, crash behavior and API compatibility pass; `mobile_agent` model abstraction; existing model download/registry code from the user's Android projects only after lifecycle tests.

**External references:** Google ADK Kotlin and Android guide; LiteRT-LM Kotlin API and Android examples; Google AI Edge Gallery as a behavior reference; model-specific official cards and licences.

**Exit evidence:** offline airplane-mode tests; valid constrained tool calls; no model offered outside measured device budget; checksum failure test; no main-thread model initialization; stable close/unload lifecycle; physical-device performance report.

## Phase 7 — Memory, skills and user-approved learning

**Goal:** Remember useful context and repeat successful workflows without expanding authority.

**Steps**
1. Store task journal, user-approved preferences, app-specific skill definitions and execution traces separately.
2. Add retention, inspect, export and delete controls.
3. Define versioned skills with manifest, required capabilities, allowed actions, parameters, preconditions and postconditions.
4. Keep skills deterministic where possible; model may select/parameterize a skill but cannot grant it new permissions.
5. Propose a reusable skill only from a successful, user-approved trace.
6. Strip credentials, OTPs, private text and sensitive screenshots from traces.
7. Revalidate target state and ask approval when a skill has consequential effects.
8. Add migration/versioning and rollback for stored workflows.

**Reuse:** `skill-agent` skill manifests, capability scoping and lifecycle; `agent-1` source provenance/evaluation; `mobile_agent` Room persistence if it is the selected database layer.

**External references:** ADK SkillToolset/progressive disclosure; LangGraph persistence concepts; Android Room/backup documentation.

**Exit evidence:** user can inspect/delete memory; skill permission cannot exceed owner policy; stale workflows stop safely; no secrets in skill traces.

## Phase 8 — Wake word and long-running autonomy

**Goal:** Add optional hands-free activation only after the foreground workflow is stable.

**Steps**
1. Define wake-word feature as optional, separately switchable and visibly indicated.
2. Evaluate on-device keyword spotting engines and model licences.
3. Measure false accept/reject rate, latency, CPU, battery and thermal impact.
4. Test microphone lifecycle by API level, including Android 14+ foreground-service restrictions.
5. Handle screen-off, Doze, standby, process kill, reboot and OEM battery management.
6. Add immediate stop/mute and clear listening indicator.
7. Keep wake-word detection separate from speech recognition and task execution.
8. Do not retain continuous raw audio.

**Reuse:** any wake-word engine already present in your own Android repositories only after battery, lifecycle and privacy tests; do not copy a recording workflow as a wake-word solution.

**Reference:** official Android foreground-service/microphone rules and the selected keyword-spotting engine's official Android integration.

**Exit evidence:** sustained physical-device test with measured battery/thermal data; false-trigger thresholds agreed; stop control works; no silent recording.

## Phase 9 — Hardening, device profiles and personal release

**Goal:** Make the system dependable across the devices you actually use.

**Steps**
1. Run clean build, unit, instrumentation, lint, static analysis, dependency and licence checks.
2. Maintain separate device profiles: API 27 legacy device and modern Android device(s).
3. Test accessibility enable/disable, permissions revoked mid-task, app updates, low storage, low memory, thermal throttling and process death.
4. Run regression task suite and record screen/tree evidence for failures.
5. Test backup/restore and database migration.
6. Review privacy: logs, audio, screenshots, model files, network access and exported components.
7. Remove mocks/simulated success from production paths; label diagnostics and unsupported capabilities honestly.
8. Produce signed sideload APK, install/upgrade instructions and rollback procedure.

**Reuse:** test utilities and CI patterns from your Android repositories; hardware/device baseline from `tecno-os` only where applicable.

**Reference:** Android security and performance guidance; AndroidWorld-style scenario evaluation; Mobilerun/MobileUse trace analysis.

**Exit evidence:** CI green; physical-device regression passed; documented known limitations; no critical/high security defects; signed APK installs and upgrades on intended devices.

## 4. External reference map by capability

| Capability | Primary reference | What to learn, not blindly copy |
|---|---|---|
| Android accessibility bridge | Android official AccessibilityService docs; Mobilerun Portal | Lifecycle, event filtering, semantic node actions and Android-side bridge |
| Long-horizon mobile planning | MobileUse | Planner/operator/reflector/progressor separation and trajectory reflection |
| Execution and tool contracts | Mobilerun | Manager/executor loop, atomic tools, traceability and failure feedback |
| Benchmarking | AndroidWorld | Task setup, controlled initial state and explicit success verifier |
| Visual grounding | UI-TARS Desktop, OpenGUI | Visual target grounding and screenshot/tree complementarity |
| Kotlin agent orchestration | Google ADK Kotlin | Typed tools, session handling, confirmation and resumability |
| Local inference/tool use | LiteRT-LM + ADK Kotlin examples | Model compatibility, tool execution, streaming and engine lifecycle |
| State graph/recovery | LangGraph | Checkpoint, interrupt and replan concepts; implement a lightweight Kotlin equivalent if needed |

## 5. Technical decision gates

- **ADK vs custom orchestrator:** choose only after Phase 4/6 POC proves pause/resume, tool-call validation, cancellation, local runtime integration and API compatibility. ADK is a candidate, not an architectural commitment.
- **Accessibility implementation:** choose after API 27 and modern physical-device tests, not README feature claims.
- **Minimum SDK:** preserve API 27 only if the complete dependency graph and required features support it. A modern-only model backend can be optional without raising the whole app's minSdk.
- **Local model:** select by measured task success and peak memory, not parameter count or advertised RAM.
- **Wake word:** last, not first. Tap-to-talk must already be reliable.
- **Multi-agent architecture:** do not introduce a swarm unless measured results show a clear improvement over one orchestrator with sequential specialist roles.

## 6. Evidence required in every phase report

Each phase report must include:
1. What was researched and source URLs/revisions.
2. Which of our own files/classes were reused directly, adapted, referenced or rejected.
3. Exact changed files and dependency changes.
4. Build/test commands and actual results.
5. Physical-device model, Android version/API and observed results.
6. Known failures, limitations and deferred work.
7. Exit-criteria checklist with evidence links.

**No phase is complete because its code was written or its README was updated. It is complete only when its acceptance tests pass.**
