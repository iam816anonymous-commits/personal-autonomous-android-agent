# Source-by-Source Reuse Specification

**Status:** Research specification — staged development authorized; donor transplantation remains gated on Phase 0 exit criteria  
**Date:** 2026-11-02  
**Destination:** iam816anonymous-commits/personal-autonomous-android-agent

## 1. Purpose and decision semantics

This document turns the source audit into actionable decisions for the Personal Autonomous Android Agent. It identifies exact classes, files and design documents to retain, adapt, rewrite or reject.

This remains a specification, not permission to transplant donor code immediately. The owner authorized phased development on 2026-11-03; code integration and destination app implementation remain blocked until the Phase 0 exit criteria in `PROJECT_STATUS.md` are evidenced and reviewed. No repository-wide merge is authorized.

### Decision labels

| Label | Meaning |
|---|---|
| RETAIN | Preserve the class/concept with minimal changes, subject to tests and licence/provenance checks. |
| ADAPT | Reuse selected algorithms, contracts or implementation sections, but change behavior or boundaries before integration. |
| REWRITE | Preserve the problem definition and useful design lessons; implement a new version because current code has correctness, lifecycle, safety or architecture defects. |
| REJECT | Do not transplant the implementation or its current design. It may be used as a negative example. |
| REFERENCE ONLY | Use documentation, research or benchmark methodology; no runtime code transplant. |

“Retain” does not mean blindly copy a file. A class can have a reusable concept but require a new implementation.

### Evidence labels

- Inspected: source file fetched and examined on the recorded branch.
- Partial: selected ranges or related files inspected.
- Document-only: design document inspected; no finished runtime implementation was found on the inspected branch.
- Unverified: no physical-device execution or reproducible hardware test evidence was available.

No finding in this document claims a feature passed a physical-device test.

## 2. Reference snapshot and provenance

Decisions are tied to these branch heads. Re-check each branch and file before implementation.

| Repository | Branch | Head commit | Role |
|---|---|---|---|
| [agent-created-by-gemini](https://github.com/iam816anonymous-commits/agent-created-by-gemini) | main | 90f5cf3ca0eca324f91a43ba96fab72c2e2de5ed | Primary implementation reference |
| [mobile_agent](https://github.com/iam816anonymous-commits/mobile_agent) | android-local-agent-os-base-65071498209142568 | 863723f7aa4b42137ff4ce07e26e541e402ff5a8 | Modular Android-agent reference |
| [skill-agent](https://github.com/iam816anonymous-commits/skill-agent) | phase-0-specifications-14452037546145599308 | 86cfa6960b1341ed53b987f5a50b1c623ec18ad4 | Security and skill specification |
| [agent-1](https://github.com/iam816anonymous-commits/agent-1) | jules-10149456198484707000-5b5421da | 6d0c3002fc4c716dea2ffcba31339f0de79fd087 | Domain/evaluation/provenance reference |
| [tecno-os](https://github.com/iam816anonymous-commits/tecno-os) | in6-linux-bringup-infrastructure-4378631429758063630 | d2aeac357141e75b601a935869abb8944ad0d41d | Device hardware reference only |

Destination repository snapshot: 8a0a353f74bb9485a675983e84f28ecb473d70b9.\n\nThe tecno-os reference was revalidated on 2026-11-03 and advanced four commits beyond the prior pin. The current reference pin is updated to the branch head above; the new GPUProbe report is evidence only and its command-buffer lifecycle caveat must be retained.

### Licence and provenance gate

GitHub repository metadata did not identify an explicit repository-level licence for agent-created-by-gemini, mobile_agent, skill-agent or agent-1 in the inspected snapshot. These are first-party repositories, but that does not automatically clear third-party code, copied snippets, model weights, assets or transitive dependencies.

Before copying a file:
1. Confirm authorship and provenance of the exact file.
2. Inspect its history and any upstream-derived implementation.
3. Inspect root/nested licence files and dependency licences.
4. Record source repository, commit, path and original licence in a reuse ledger.
5. Preserve required copyright and licence notices.
6. Do not copy external code merely because it appears in a first-party repository.

tecno-os declares MIT. That does not make kernel/device-tree code appropriate for an Android application. It is hardware evidence, not an app-runtime donor.

## 3. Executive decisions

| Source | Decision | What to take |
|---|---|---|
| agent-created-by-gemini | Primary donor, selective | Typed command concepts, plan validation, target resolution, selected snapshot/diff concepts, recovery concepts and hardware inventory |
| mobile_agent | Architecture reference, selective | Module boundaries, provider interfaces, basic action/plan vocabulary, memory/evaluation concepts |
| skill-agent | Specification donor | Capability checks, skill manifest, permission revocation, audit events and resource governance |
| agent-1 | Supporting domain reference | Source provenance and evaluation-contract ideas |
| tecno-os | Reference only | Device/hardware facts and physical-validation discipline |
| External projects | Reference/prototype | ADK Kotlin and LiteRT-LM prototype; AndroidWorld verifier methodology; MobileUse/Mobilerun execution patterns |

Do not merge any entire repository into the destination. The new project must have one authoritative Kotlin execution loop, one policy gate, one device-action boundary and one independent verification layer.

---

# 4. agent-created-by-gemini: exact source decisions

Repository: [agent-created-by-gemini](https://github.com/iam816anonymous-commits/agent-created-by-gemini)  
Snapshot: 90f5cf3ca0eca324f91a43ba96fab72c2e2de5ed

## 4.1 Domain models and command contracts

File: app/src/main/java/com/example/core/model/Models.kt

| Exact type | Decision | Required treatment |
|---|---|---|
| TaskCommand | ADAPT | Preserve sealed-command concept. Rebuild around destination typed, capability-scoped action contracts. Every action needs preconditions, typed parameters, timeout, cancellation, risk, idempotency and a verifiable postcondition. |
| TargetSelector | ADAPT | Retain semantic selectors (text, view ID, class, content description, package and bounds). Add snapshot generation/freshness. Ambiguous selectors must fail closed. Coordinates are not stable identity. |
| VerificationRule | ADAPT | Retain explicit postconditions such as package match, text present and element exists. Remove AlwaysPass from production paths. |
| RectData | RETAIN concept | Immutable geometry is appropriate. Validate coordinate space, display rotation and bounds. |
| UiNode | ADAPT | Retain normalized immutable node-tree concept. Add sensitivity classification, visibility/enabled/action flags and source metadata. Redact before persistence or model exposure. |
| UiSnapshot | ADAPT | Retain identity/generation concept. Add capture source, window/display identity, foreground package, monotonic timestamp, completeness and sensitivity policy. Snapshots expire for action targeting. |
| SnapshotComparisonDiff | ADAPT | Retain diff output as a signal. A tree diff is not proof that the user goal was achieved. |
| TargetMatch | ADAPT | Retain score and match-reason diagnostics. Require calibrated score separation. Reject ambiguous or unsafe ancestor/coordinate fallbacks unless separately authorized. |
| VerificationOutcome | RETAIN concept | Keep SUCCESS / FAILURE / INCONCLUSIVE, reason and evidence. Confidence must be evidence-derived, not model-supplied. |
| StepExecutionRecord | ADAPT | Add action ID, idempotency key, attempt, approval decision, capability decision, timestamps, evidence references and durable commit state. |
| ExecutionStatus, StepStatus, ExecutionMode | ADAPT | Reuse state vocabulary but define one canonical destination state machine. Live, test and simulation modes must not be confused. |
| DeviceCapabilities | REWRITE | Current aggregate fields are insufficient. Use per-capability support, API availability, permission, runtime availability, current state, evidence time and error reason. |
| CognitiveThought, CognitivePhase | REJECT for execution authority | Do not store free-form internal reasoning as an authority source. Store concise structured decisions and action evidence. |
| VerificationRule.AlwaysPass | REJECT | It provides no evidence and must not be accepted by a production plan validator. |
| WebResearch, MorningBriefing, ExportDurableVault and unrelated commands | REJECT from initial device-control scope | Keep unrelated research/briefing/vault operations out of the minimal device-control action vocabulary. They may become separately permissioned skills later. |

Do not copy the entire Models.kt file. It mixes device commands, research, durable memory, telemetry, snapshots and cognitive display state. Split domain contracts from Android/UI/research-specific models.

## 4.2 Execution and planning

File: app/src/main/java/com/example/core/engine/AgentExecutionEngine.kt

| Exact type / method | Decision | Required treatment |
|---|---|---|
| AgentExecutionEngine | REWRITE | Preserve observe → plan → gate → execute → observe → verify → recover. Build a new engine around injected interfaces and an owned lifecycle scope. |
| startTask(...) | ADAPT | Keep single-active-task admission and explicit execution mode. Return a task handle/result instead of silently ignoring duplicate requests. |
| startCustomTask(...) | REWRITE | Accept only validated typed plans. Caller-supplied command lists must not bypass policy or schema validation. |
| cancelCurrentTask(...) | ADAPT | Cancellation must be cooperative and durable. Cancellation after dispatch means outcome uncertain until device re-observation. |
| stopExecution() / triggerEmergencyKillSwitch(...) | ADAPT | Stop future dispatch, cancel waits, release resources and record in-flight side-effect state. Hardware shutdown is best-effort and separately verified. |
| captureFreshObservation() | ADAPT | Use an injected observation provider with monotonic timestamp and snapshot generation. |
| AgentEngineState | ADAPT | Keep UI-observable state, but do not make UI state the durable source of truth. Persist task and step journal separately. |

Observed issues:
- The class creates CoroutineScope(Dispatchers.Default) internally without a visible owner cancellation contract.
- It directly wires parser, resolver, verifier, actuator, recovery, cognitive brain, memory manager, approval gate and intent router. Replace with narrow interfaces.
- Approval is not fully integrated into execution in the inspected path. Approval must be mandatory before dispatch when policy requires it.
- The existing retry path re-observes before retrying and tries to avoid duplicate dispatch. Preserve that principle, but classify actions as idempotent, conditionally idempotent or non-idempotent.
- Timeout does not prove an action did not happen. Reconcile by observation before retry.
- Do not persist free-form internal reasoning. Persist plan, evidence, policy decision, action and outcome.

File: app/src/main/java/com/example/core/engine/HierarchicalTaskPlanner.kt

- SubGoalStatus, SubGoal, HierarchicalPlan, PlanExecutionDecision and HierarchicalTaskPlanner: ADAPT.
- Retain bounded subgoal decomposition and explicit continue/complete/rollback/finish decisions.
- Rewrite rollback semantics. UI actions are not generally reversible. A rollback command must be separately authorized and proven safe; otherwise use compensation or stop-and-report.
- Add maximum depth, action count, elapsed time, token budget and recovery budget.
- Planner output is a proposal; execution and policy remain authoritative.

Related recovery source:
- AutonomousRecoveryEngine and reflection/recovery tests: ADAPT recovery taxonomy and evidence-driven re-observation only.
- Do not transplant autonomous retry/rollback behavior until every path has bounded depth and side-effect classification.

## 4.3 Target resolution and plan validation

File: app/src/main/java/com/example/core/resolver/TargetResolver.kt

- TargetResolver, ResolutionResult.Success and ResolutionResult.Failure: ADAPT.
- Preserve deterministic matching and explainable reasons.
- Require snapshot freshness, correct package/window, visible/enabled state and a unique or sufficiently separated best candidate.
- Reject ambiguous matches instead of clicking the first candidate.
- Parent/ancestor click fallback must be explicit and recorded.
- Coordinate fallback must be bound to the same display/window/snapshot and invalidated after layout change, keyboard appearance, rotation or scrolling.

File: app/src/main/java/com/example/core/intent/PlanSchemaValidator.kt

- PlanSchemaValidator, ValidationResult.Valid and ValidationResult.Invalid: ADAPT; high-value reuse candidate.
- Retain deterministic schema validation and error collection.
- Rebuild mapping against destination action types and capability IDs.
- Validate required/forbidden parameters, numeric bounds, package allowlists where appropriate, URI schemes, text size, action count, nesting depth and selectors.
- Reject unknown actions, undeclared capabilities, arbitrary code, shell strings, reflection requests and model-supplied executable payloads.
- Reject AlwaysPass, unbounded waits and model-declared success.
- Add schema versioning and compatibility tests.

## 4.4 Approval and safety

File: app/src/main/java/com/example/core/approval/TaskApprovalGate.kt

- RiskLevel, ProposedStep, ExecutionPlan and TaskApprovalGate: ADAPT.
- Preserve risk assessment and explicit propose/approve/reject state.
- Separate policy engine from approval UI and task orchestration.
- Bind approval to an immutable plan hash/version. Any change to target, parameters, sequence or risk invalidates approval.
- Approval expires and is scoped to task/action. No global approve-all default.
- Permission grant and task approval are separate checks.
- Require explicit confirmation for consequential actions such as sending messages, purchases/payments, deleting user data, changing security settings or sharing private information.
- Revocation prevents subsequent dispatch and cancels active work where safe.
- Test absent, stale, modified-plan, revoked and cross-task approval.

## 4.5 Verification

File: app/src/main/java/com/example/core/verifier/StepVerifier.kt

- StepVerifier: REWRITE.
- Keep command-specific verifier concept and tri-state outcome.
- Rewrite every verifier to evaluate a declared postcondition using fresh observations or hardware read-back.
- Current ControlActuator branch reports success after signal dispatch. This is not physical-state verification.
- Current default branch reports success for otherwise-unhandled commands. Remove it.
- A delay completing proves only that time elapsed.
- A timer request proves only that a timer request was issued unless a platform result/state confirms it.
- Web research requires its own evidence contract and is outside initial phone-control scope.
- AlwaysPass must cause invalid-plan rejection or INCONCLUSIVE, never success.
- Verification distinguishes SUCCESS, FAILURE, INCONCLUSIVE and TIMEOUT as appropriate.
- An action API returning true is dispatch evidence, not automatically postcondition evidence.

Required invariant: no task or step reaches COMPLETED_SUCCESS unless every mandatory postcondition has independent evidence.

## 4.6 Hardware discovery and control

File: app/src/main/java/com/example/core/diagnostics/DeviceCapabilityDetector.kt

- DeviceCapabilityDetector: ADAPT.
- Reuse Android service discovery and device metadata probes.
- Replace nullable aggregate guesses with per-capability records and provenance.
- Storage metrics distinguish total, available, unavailable and query failure. Unknown remains unknown; never invent a fallback.
- Keep runtime permission, API support, hardware presence and transient availability separate.

File: app/src/main/java/com/example/core/diagnostics/DeviceCapabilityRegistry.kt

- DeviceCapabilityRegistry and CapabilityProfile: ADAPT.
- Retain registry/inventory concept.
- Avoid process-global mutable singleton as the only authority. Use injected repository/service with refresh timestamps and invalidation.
- Registry describes support and observed state; it does not grant permission to execute.

File: app/src/main/java/com/example/core/hardware/HardwareManager.kt

- LiveSensorTelemetry and HardwareManager: REWRITE.
- Retain hardware-domain inventory and useful Android API discovery after review.
- Split into typed adapters: TorchAdapter, VibrationAdapter, AudioAdapter, SensorAdapter, BatteryThermalAdapter, CameraAdapter, LocationAdapter, ConnectivityAdapter, and optional NFC/USB adapters.
- Remove internally created unowned CoroutineScope. Every adapter needs an explicit owner and cancellation lifecycle.
- Sensor registration requires explicit start/stop subscription and matching unregister path.
- Broadcast receiver updates require stored receiver reference and matching unregister lifecycle.
- Replace synthetic sensor fallback values with unavailable/unknown readings.
- Do not expose direct Wi-Fi/Bluetooth mutation as guaranteed operations. Use supported OS flows/settings panels and report handoff separately.
- Re-check permission and availability immediately before each action.
- Read hardware state after operation and record evidence.
- Do not maintain duplicated state in HardwareManager and ActuatorController.

File: app/src/main/java/com/example/core/actuator/ActuatorController.kt

- ActuatorResult and ActuatorController: REWRITE.
- Preserve individual torch, vibration, audio, volume and settings-handoff operations as adapter-level references only.
- Remove the second independently constructed HardwareManager.
- Replace string-based actuator dispatch with typed action contracts.
- Results distinguish DISPATCHED, STATE_VERIFIED, USER_ACTION_REQUIRED, UNSUPPORTED, PERMISSION_DENIED, FAILED or UNKNOWN.
- Implement bounded torch auto-off with cancellation and state verification.
- A Settings handoff is not a completed hardware change.
- Release audio/vibration resources on cancellation, service stop and shutdown.

## 4.7 Memory and persistence

File: app/src/main/java/com/example/core/memory/MemoryBudgetManager.kt

- MemorySnapshot and MemoryBudgetManager: ADAPT.
- Retain a single resource snapshot/admission concept.
- Separate Java heap, native heap, process PSS/RSS, system available memory, model memory, storage and thermal pressure.
- System.gc() is not proof that memory was returned to the kernel.
- Model admission uses measured headroom, runtime overhead, context/KV cache and safety margin; static profile alone is insufficient.
- Add hysteresis and unload/cooldown to prevent repeated load/unload thrashing.

MemoryStack, DurableMemoryStorageManager and DurableMemoryBackup: ADAPT selectively after persistence/privacy review.
- Keep explicit backup/restore and integrity concepts.
- Do not store raw screen text, password fields, OTPs or sensitive accessibility payloads in long-term memory.
- Define retention, encryption, export, deletion and migration before cross-app memory is enabled.

File: app/src/main/java/com/example/core/security/KeyStoreManager.kt

- KeyStoreManager: ADAPT after cryptographic review.
- Inspect cipher, IV/nonce generation, authentication tag handling, key purpose, API 27 fallback, exception behavior and backup interaction.
- Use Android Keystore-backed keys where supported; define explicit API 27 compatibility path.
- Never log secrets or silently fall back to plaintext.
- Add tamper, wrong-key, process-restart and backup/restore tests.

## 4.8 Accessibility and UI observation

File: app/src/main/java/com/example/service/LocalAgentAccessibilityService.kt

- LocalAgentAccessibilityService: ADAPT service shell; rewrite sensitive capture and action paths.
- Retain AccessibilityService integration, lifecycle hooks and live-snapshot concept.
- Redact sensitive content before logs, recorder, memory or model context.
- The inspected recorder path records text from TYPE_VIEW_TEXT_CHANGED. Do not preserve raw text recording by default.
- Define explicit recording consent, foreground indication, stop behavior and deletion.
- Ensure AccessibilityNodeInfo objects are released safely and not retained beyond observation/action transaction.
- Separate observation from action dispatch; service is not a second orchestrator.
- Volume-key kill switch is an optional device/OEM-dependent shortcut, not the only emergency stop.
- Test service restart, interrupt, window change, permission revocation and process death.
- AccessibilityService must not bypass authentication or secure-window protections.

File: app/src/main/java/com/example/core/diff/AccessibilityNodeDiffer.kt

- CompactNode, ViewNodeSnapshot, TreeDiffResult, ExecutionTraceRecord and AccessibilityNodeDiffer: ADAPT.
- Retain compact tree snapshots and structural diff after checking memory and node recycling.
- ExecutionTracer: REWRITE around redaction-first structured events, bounded storage, retention and opt-in diagnostics.
- Changed node count is not proof of goal completion.

## 4.9 Parsing and local inference

File: app/src/main/java/com/example/core/parser/NaturalLanguageCommandParser.kt

- NaturalLanguageCommandParser and ParseResult: ADAPT.
- Keep deterministic, explicitly supported fast paths with exact intent and parameter validation.
- Broad substring/prefix matches must not authorize actions.
- Unknown/ambiguous instructions return Unsupported or request clarification.
- Parser output passes the same schema, capability and approval gates as model output.

Files:
- app/src/main/java/com/example/core/intent/LocalSlmBackend.kt
- app/src/main/java/com/example/core/slm/LocalSlmIntentEngine.kt
- app/src/main/java/com/example/core/slm/LlamaAndroidAdapter.kt
- app/src/main/java/com/example/core/slm/LlamaNativeBridge.kt
- app/src/main/java/com/example/core/slm/LocalGbnfGrammar.kt

Decision: REJECT current runtime implementation; REFERENCE provider/session/grammar concepts.

Reasons:
1. Inspected tree contains JNI declarations for libllama-bridge.so but no tracked native library, C/C++ implementation, CMake build or native packaging evidence.
2. LocalSlmIntentEngine.loadModel() can set loaded state without creating a native inference session.
3. Memory manager is optional in one path and bypassed by the separate local backend.
4. LlamaAndroidAdapter.measureInference() derives peak memory from configured profile values, not measured process memory.
5. Fallback results can carry grammarValidated=true although no model grammar validation occurred.
6. Confidence is not consistently grounded in verified output.
7. A unit test that permits fallback does not prove real inference.

Build a provider interface and run a fresh proof-of-concept. Compare ADK Kotlin + LiteRT-LM with a verified Android llama.cpp build. Do not carry over static RAM estimates or claim physical inference until a real device trace proves it.

File: app/src/main/java/com/example/core/slm/ModelDownloadManager.kt

- DownloadStatus, ModelDownloadProgress and ModelDownloadManager: REWRITE.
- Current isModelDownloaded() accepts a file at 80% of expected profile size. Reject this.
- SHA-256 is optional in the current API. Require a trusted expected digest for catalog downloads.
- Use temporary files, resumable transfer with server/range validation, exact size, checksum, atomic rename/activation and crash recovery.
- Verify disk space before download and memory before model load.
- Catalog entries identify model revision, format, quantization, tokenizer/template, licence, source URL, expected size and digest.
- Model files are untrusted data. Never execute model-provided code.

## 4.10 Tests and build files

| Test file | Decision | Required changes |
|---|---|---|
| ActuatorAndTelemetryTest.kt | ADAPT | Test physical-state verification and adapter lifecycle; dispatch-only success must fail. |
| CommandParserAndApprovalTest.kt | ADAPT | Add stale approval, modified plan, revoked capability and unknown command cases. |
| StepVerifierTest.kt | ADAPT | Prove unhandled/default actions cannot return success. |
| TargetResolverSafetyTest.kt | ADAPT | Add stale snapshot, ambiguity, package mismatch, hidden/disabled node and coordinate invalidation. |
| LocalSlmEngineTest.kt | REWRITE | Separate fallback unit tests from native integration tests. Native tests must fail—not silently pass—when runtime unavailable. |
| ReflectionAndRecoveryTest.kt | ADAPT | Add bounded recursion, no blind replay, non-idempotent uncertainty and process-death recovery. |
| AgentCoreHardeningTest.kt and broad tests | AUDIT INDIVIDUALLY | Do not migrate by filename or passing status; inspect every assertion. |
| ExampleInstrumentedTest.kt | REWRITE | Package-name smoke test is insufficient; add service, permission, action and verification scenarios on physical devices. |

Build files app/build.gradle.kts, root build.gradle.kts, settings.gradle.kts, gradle/libs.versions.toml and .github/workflows/android.yml: REFERENCE, THEN REWRITE FOR DESTINATION.
- Inspected app declares Java/Kotlin 21 while workflow setup uses JDK 17 and SDK installation does not align cleanly with compile SDK declaration. Resolve as one tested toolchain matrix.
- Audit every Android dependency for minSdk 27 and transitive native ABI compatibility.
- CI is not a substitute for physical-device tests.

---

# 5. mobile_agent: exact source decisions

Repository: [mobile_agent](https://github.com/iam816anonymous-commits/mobile_agent)  
Snapshot: 863723f7aa4b42137ff4ce07e26e541e402ff5a8

## 5.1 Execution

File: core/src/main/java/com/android/agentos/core/engine/Executor.kt

- Executor: REJECT current implementation; REFERENCE observe/execute/verify ordering.
- It has a useful step loop and provider boundaries, but missing OutcomeProvider defaults final goal verification to true.
- Repair plans execute recursively without a sufficiently clear global recovery-depth/work budget.
- Generic retries do not distinguish non-idempotent side effects.
- Fixed delays are used where observation-driven waits are required.
- New engine requires final verifier; absence means unsupported/inconclusive, never success.
- Keep one global task budget and bounded recovery loop.

Provider interfaces in the same file:

| Type | Decision | Treatment |
|---|---|---|
| AccessibilityProvider | ADAPT | Split observe/action ports; add snapshot identity, typed result, cancellation and capability checks. |
| VerificationProvider | ADAPT | Require evidence-rich tri-state results. |
| MemoryProvider | ADAPT | Use destination memory repository and redaction rules. |
| ReflectionProvider | ADAPT | Planner may propose repair; it cannot dispatch. Re-enter policy and approval. |
| OutcomeProvider | ADAPT | Mandatory for goals that claim completion; no permissive null fallback. |
| WorldModelProvider | REFERENCE / OPTIONAL | Prediction is advisory only and never substitutes for observation. |

## 5.2 Accessibility

File: accessibility/src/main/java/com/android/agentos/accessibility/AgentAccessibilityService.kt

- AgentAccessibilityService: REWRITE.
- Retain provider-adapter role and semantic lookup strategies.
- Remove blocking Thread.sleep() loops in monitorForElement() and scrollToElement(); use cancellable suspend operations and bounded observation polling.
- Current verifyAction() returns success by default for action types other than a limited subset. Remove.
- Current OPEN_APP verifier uses non-empty screen context as proxy. Verify actual foreground package.
- Rework node recycling and exception safety.
- Add snapshot freshness, package/window checks, redaction and secure-window handling.
- Keep service thin; it is not a planner or autonomous executor.

## 5.3 Planning and models

File: ai/src/main/java/com/android/agentos/ai/Planner.kt

- Planner: ADAPT.
- Retain plan-generation separation and action history as optional provider-backed service.
- Require structured plan validation; model confidence is not authorization.
- Plan generation and verification use separate contracts.

File: ai/src/main/java/com/android/agentos/ai/outcome/OutcomeVerifier.kt

- OutcomeVerifier: REJECT current implementation; REWRITE contract.
- It converts a natural-language goal into VERIFY_ELEMENT and asks the LLM provider to verify it. A goal is not necessarily an element selector.
- Build explicit goal postconditions from typed plan data. Deterministic device evidence comes first; an LLM may summarize evidence but cannot invent it.

File: core/src/main/java/com/android/agentos/core/models/LLMProvider.kt

- LLMProvider, UsageMetrics and VerificationResult: ADAPT.
- Retain provider abstraction and metrics concept.
- Split generation, structured tool-call parsing and verification roles.
- Use explicit unavailable/error states. Metrics distinguish measured and estimated values.

File: core/src/main/java/com/android/agentos/core/models/Models.kt

- AgentAction, ActionType, Plan, Subgoal, PlanStatus, ExecutionResult, FailureLog, FailureCategory, ScreenElement, ScreenState and Rect: ADAPT.
- Keep minimal domain concepts only; avoid duplicating destination canonical command/state definitions.
- Add typed parameters, action risk, capability, idempotency, deadline, evidence references and approval binding.

## 5.4 Tool registry

Files:
- core/src/main/java/com/android/agentos/core/models/Tool.kt
- core/src/main/java/com/android/agentos/core/models/ToolRegistry.kt

- Tool, ToolResult and ToolRegistry: ADAPT.
- Retain registry-driven dispatch instead of a large string conditional.
- Each tool declares immutable metadata: ID/version, input schema, required capabilities, risk, supported API range, timeout and verification contract.
- Registry lookup is not authorization. Policy checks run on every dispatch.
- Reject duplicate/ambiguous tool IDs and undeclared tools.
- Tools receive narrow adapters, never raw Android service handles or unrestricted application context.

## 5.5 Memory and world model

| Source | Decision | Treatment |
|---|---|---|
| EpisodicMemory | ADAPT | Keep task episode concept; add privacy filters, provenance, retention and user deletion. |
| SemanticMemory | ADAPT | Keep only user-approved stable facts; avoid raw screen data. |
| InteractionPatternLearner | REFERENCE / ADAPT later | Learn only from verified workflows and user approval. |
| RecoveryLearner | REFERENCE / ADAPT later | Learn failure patterns; never grant capabilities or replay unsafe steps automatically. |
| WorkflowReplayer | REJECT blind replay | Rebuild as state-checked execution. Resolve fresh targets and revalidate permissions per step. |
| WorldModel | OPTIONAL REFERENCE | Predictions guide observation but never establish truth. |
| LongTermMemorySuite / LongitudinalMemorySuite | ADAPT after privacy design | Consider only after encryption, deletion, retention and provenance contracts. |

The module split (:app, :ai, :accessibility, :core, :memory, :tools) is a REFERENCE / ADAPT candidate. Destination module boundaries must follow dependency direction and capability boundaries, not be copied mechanically.

The inspected app/accessibility Gradle files declare minSdk 26, so API 27 is plausible at those module boundaries. This does not prove every dependency, permission flow or runtime feature works on API 27. Audit the full dependency graph and test on the actual API 27 device.

---

# 6. skill-agent: specification-level reuse

Repository: [skill-agent](https://github.com/iam816anonymous-commits/skill-agent)  
Snapshot: 86cfa6960b1341ed53b987f5a50b1c623ec18ad4

The inspected branch contains design specifications, not a verified finished Android runtime. These are requirements to implement, not classes to copy.

| Source / concept | Decision | Destination treatment |
|---|---|---|
| docs/SECURITY_MODEL.md | ADAPT | Capability authorization, manifest declaration, runtime OS permission checks, dynamic revocation and audit events. |
| docs/SKILL_MANIFEST_SCHEMA.md | ADAPT | Versioned declarative manifest with identity, tools, input schema, capabilities and risk. |
| docs/SKILL_DEFINITION.md | ADAPT | Skill metadata and explicit input/output contracts. |
| docs/RESOURCE_BUDGET.md | REFERENCE ONLY until measured | Budget numbers are acceptance targets, not device measurements. Recalculate from PSS/native heap/model context and physical traces. |
| docs/ISOLATION_MODEL.md | ADAPT with caution | Preserve trust-boundary principles. A ClassLoader is not a security sandbox. |
| CapabilityManager concept | IMPLEMENT NEW | Check manifest declaration, platform support, OS permission, user grant, runtime revocation and action risk. |
| SkillExecutor concept | IMPLEMENT NEW | Single mediated execution boundary. Skills cannot call AccessibilityService or hardware APIs directly. |
| EventLogger concept | IMPLEMENT NEW | Redaction-first audit events with bounded retention. |

Important budget conflict: the skill-agent total-process PSS target cannot be combined blindly with another repository's model-only native-memory estimates. Define independent budgets for Java heap, native inference, UI tree/screenshot buffers, storage, total PSS and system available RAM.

Initial release restriction: do not load arbitrary third-party executable skill code. Start with built-in declarative skills calling the typed tool registry. Dynamic executable plugins require a separate threat model and process-isolation decision.

---

# 7. agent-1: exact source decisions

Repository: [agent-1](https://github.com/iam816anonymous-commits/agent-1)  
Snapshot: 6d0c3002fc4c716dea2ffcba31339f0de79fd087

| Source | Decision | Treatment |
|---|---|---|
| SourceProvenance | RETAIN concept; ADAPT model | Keep source type, source ID and provenance metadata. Extend with timestamp, source revision, verification status and user approval. |
| Contracts.kt / ReasoningProvider | REFERENCE | Provider abstraction is useful, but document/learning domain contracts do not belong in Android action domain. |
| SkillRepository | REFERENCE / ADAPT | Repository boundary can inform skill storage. Use destination manifest/capability contracts. |
| AgentController | REJECT for phone execution | It orchestrates learning sessions and skill-card verification, not Android device actions. |
| LearningEngine | REFERENCE for future learning | Consider only after execution and workflow verification are stable. |

The destination should record provenance for every learned workflow: user-created, system-provided, model-proposed, imported or derived from a verified execution trace.

---

# 8. tecno-os: hardware reference decisions

Repository: [tecno-os](https://github.com/iam816anonymous-commits/tecno-os)  
Snapshot: c8a23e359de5b30297ca731f713b89077e056cfc

Decision: REFERENCE ONLY. No Android application classes are to be copied.

Useful documents:
- docs/hardware-overview.md
- docs/hardware-compatibility.md
- docs/hardware/feature-status.md
- docs/testing/physical-validation-plan.md
- docs/testing/real-hardware-bringup-report.md

Use these as device-family evidence only where exact hardware variant matches. A Linux driver/kernel capability does not prove availability to an ordinary Android app. Android permissions, framework APIs, firmware, SELinux and OEM policy are separate constraints.

---

# 9. External project decisions

| Project | Decision | What to learn/test |
|---|---|---|
| Google ADK Kotlin | PROTOTYPE | Tool calling, session lifecycle, cancellation, API compatibility and integration complexity. |
| LiteRT-LM | PROTOTYPE | On-device inference, structured tool calls, packaging, ABI support, memory and API 27 viability. |
| MobileUse | REFERENCE ONLY | Hierarchical planning, reflection and recovery; host/ADB setup is not target runtime. |
| Mobilerun framework | REFERENCE ONLY | Manager/executor boundaries, tool contracts and execution traces. |
| Mobilerun Portal | REFERENCE ONLY; licence gate | Accessibility companion design. Inspected release history includes AGPL-3.0; no transplant without deliberate licence decision. |
| AndroidWorld | ADOPT METHODOLOGY | Task setup, explicit goal verifier and repeatable benchmark design. |
| OpenGUI | REFERENCE ONLY | Visual grounding. Current client API requirements and BUSL-1.1 terms make it unsuitable as API 27 baseline dependency. |
| UI-TARS Desktop | REFERENCE ONLY | Visual grounding and multimodal interaction; desktop runtime is not Android app foundation. |

A prototype result must report real build compatibility and physical-device measurements. README feature lists are not acceptance evidence.

---

# 10. Destination architecture mapping

This is the proposed mapping, not a list of existing destination files.

| Destination module | Approved source concepts | Must be newly implemented |
|---|---|---|
| domain | Adapted TaskCommand, TargetSelector, VerificationOutcome, SourceProvenance | Canonical typed actions, goals, plans, capabilities, risk, idempotency and evidence |
| device-profile | DeviceCapabilityDetector and DeviceCapabilityRegistry concepts | Per-capability inventory and measured memory/storage/thermal profile |
| accessibility | AccessibilityService shell, UiNode, UiSnapshot, resolver, tree differ | Redaction-first observer, fresh snapshots, safe node lifecycle and action adapter |
| hardware | Hardware inventory and selected actuator algorithms | Typed adapters, permission gate, lifecycle and state read-back |
| execution | Agent execution loop and hierarchical subgoal concepts | Single authoritative bounded task state machine and durable action journal |
| policy | TaskApprovalGate and skill-agent capability model | Immutable approval binding, runtime checks, revocation and risk policy |
| ai | Planner/provider concepts and model metadata | Replaceable inference providers and strict structured-output validation |
| voice | Android speech API research | Speech input abstraction, transcript validation and optional wake-word |
| memory | Episodic memory and provenance concepts | Encrypted, redacted, user-controlled task memory and checkpoints |
| skills | Skill manifest and tool registry concepts | Declarative built-in skills with explicit capability declarations |
| diagnostics | Execution trace and resource snapshot concepts | Redaction-first logs, metrics and reproducible device reports |

Dependency direction:
1. domain has no Android dependency.
2. Platform modules implement domain ports.
3. execution depends on ports, not concrete Android services.
4. policy is checked for every action immediately before dispatch.
5. ai cannot directly invoke platform APIs.
6. Skills call the typed tool registry and receive structured results only.

---

# 11. Mandatory destination contracts

## 11.1 Action contract

Every action contains:
- Stable action type and schema version.
- Typed, validated parameters.
- Required capability set.
- Target package/window/snapshot reference where relevant.
- Risk class.
- Idempotency classification.
- Timeout and cancellation behavior.
- Preconditions.
- Postcondition/verifier specification.
- Approval binding if required.

## 11.2 Action result contract

Results distinguish:
- REJECTED_POLICY
- REJECTED_INVALID_INPUT
- UNSUPPORTED
- PERMISSION_DENIED
- USER_ACTION_REQUIRED
- DISPATCHED
- STATE_VERIFIED
- FAILED
- TIMED_OUT_OUTCOME_UNKNOWN
- CANCELLED_OUTCOME_UNKNOWN

Do not compress these states into a Boolean.

## 11.3 Verification contract

A successful result references actual evidence:
- Fresh post-action accessibility snapshot.
- Foreground package/window confirmation.
- Hardware state read-back.
- Platform callback with documented semantics.
- Explicit user confirmation only when the postcondition cannot be observed independently.

The action API return value is dispatch evidence, not automatically postcondition evidence.

## 11.4 Retry contract

- Never retry a non-idempotent action blindly.
- Re-observe before retry after timeout, process death or lost callback.
- If outcome remains uncertain, stop and ask the user or report uncertainty.
- Enforce one global retry budget and recovery-depth limit.
- A repair plan is a new plan and passes schema, capability and approval checks.

## 11.5 Privacy contract

- Redact before logging, recording, model context or persistent memory.
- Treat password, OTP, payment, authentication and secure-window content as sensitive by default.
- No raw text-entry recording by default.
- Explicit consent for workflow recording.
- User-visible listening/recording state.
- User controls for inspection, export and deletion.

---

# 12. Reuse implementation order and gates

| Gate | Work | Required evidence |
|---|---|---|
| G0 | Freeze source revisions, licence/provenance ledger and dependency compatibility | Exact source refs and legal/provenance clearance |
| G1 | Destination domain contracts and build foundation | Clean build, unit tests, API 27 compile/dependency review |
| G2 | Device profile and accessibility observation | Accurate physical-device inventory and redaction tests |
| G3 | Deterministic action and target resolution | Real device actions with fresh snapshot and independent verification |
| G4 | Hardware adapters | Physical-device state read-back and lifecycle tests |
| G5 | Policy and approval | Negative tests prove unauthorized actions cannot dispatch |
| G6 | Voice input | Offline/online speech behavior and privacy lifecycle measured |
| G7 | Local model proof-of-concept | Real physical-device inference, structured output, measured PSS and unload |
| G8 | Autonomous execution | Multi-step tasks with bounded recovery and no false success |
| G9 | Memory and declarative skills | Provenance, redaction, retention, revocation and replay safety |
| G10 | Wake-word and personal release | Battery, thermal, lifecycle, process-death and device regression evidence |

No gate is complete merely because documentation exists. Each requires executable tests and evidence.

---

# 13. Mandatory acceptance tests for reused concepts

| Area | Minimum tests |
|---|---|
| Target resolution | Unique match; duplicate labels; stale snapshot; wrong package; hidden/disabled node; changed display bounds |
| Plan validation | Valid plan; unknown action; missing parameter; out-of-range value; unknown capability; oversized payload; invalid schema version |
| Approval | No approval; expired approval; modified plan; revoked permission; approval for another task |
| Accessibility | Service disconnected; null root; secure window; keyboard overlay; process restart; sensitive text redaction |
| Hardware | Unsupported hardware; permission denied; contention; callback timeout; state not reached; adapter destroyed mid-operation |
| Verification | Dispatch success but state unchanged; ambiguous observation; no verifier; unsupported action; stale evidence |
| Retry/recovery | Non-idempotent timeout; action succeeded but callback lost; duplicate side-effect prevention; recovery-depth exceeded |
| Model runtime | Native library absent; wrong ABI; corrupted model; checksum mismatch; low memory/disk; cancellation; unload/reload |
| Memory | High heap; native allocation spike; low system RAM; process PSS pressure; thermal throttling |
| Privacy | Password; OTP; payment form; text-change event; recorder on/off; diagnostic export; deletion |
| Skills | Undeclared capability; revoked capability; malformed manifest; path traversal; duplicate tool ID; direct platform access attempt |

---

# 14. Explicit first-release non-goals

Do not implement these until core gates pass:
1. Arbitrary executable third-party skills.
2. Unrestricted shell/script/code execution from model output.
3. Automatic financial transactions, purchases or message sending.
4. Silent recording or hidden monitoring.
5. Authentication, lock-screen or secure-window bypass.
6. Model-generated direct Android API calls.
7. Multiple autonomous agents independently dispatching device actions.
8. Automatic workflow replay without fresh observation and permission checks.
9. Always-on wake-word before tap-to-talk and lifecycle reliability.
10. Model catalogue targets unsupported by measured memory and compatibility.

---

# 15. Audit limitations and unresolved decisions

- No physical-device tests were run during this audit.
- No destination implementation or integration build was performed.
- JNI native model library was not found in the inspected tracked tree; external binary supply is not ruled out, but must be proven.
- Model peak-memory and tokens/second claims are not validated measurements.
- skill-agent security/resource documents are specifications, not evidence of a secure runtime.
- mobile_agent benchmark claims were not independently reproduced.
- Full dependency and transitive licence graph has not been completed.
- API 27 compatibility of every modern library/capability remains open.
- OEM background-service, microphone and battery behavior must be tested on actual phones.

Technical spikes still required:
1. ADK Kotlin + LiteRT-LM versus custom Kotlin orchestration + verified local inference.
2. Minimum supported Android version and feature-degradation contract.
3. Visual observation fallback for API 27.
4. Wake-word feasibility under actual Android/OEM restrictions.
5. Encryption and backup design for user-controlled memory.
6. Whether dynamic skill execution is needed at all for personal use.

---

# 16. Implementation authorization checklist

- [ ] Source-by-source decisions reviewed.
- [ ] Licence and provenance ledger completed for selected files.
- [ ] Destination module and contract design approved.
- [ ] API 27 and modern-device compatibility matrix approved.
- [ ] Physical-device test devices and scenarios identified.
- [ ] G0/G1 acceptance criteria agreed.
- [ ] Explicit implementation authorization received.

**Current authorization state: NOT AUTHORIZED.**

The next approved action should be a narrowly scoped implementation phase, not a repository-wide merge.
