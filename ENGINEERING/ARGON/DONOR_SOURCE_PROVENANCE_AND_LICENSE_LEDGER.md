# Phase 0 Donor Source Provenance, Discrepancy Audit, and License Ledger

**Date:** 2026-11-03  
**Workstream:** Gemini 4 Argon — Phase 0 Donor Validation (`ENGINEERING/ARGON/ARGON_DEVELOPMENT_HANDOFF.md` §2.4, §2.5, §5)  
**Destination Repository:** `https://github.com/iam816anonymous-commits/personal-autonomous-android-agent`  
**Validation Branch:** `phase-0-argon-handoff`  

---

## 1. Repository-Level License, Authorship, and Provenance Status

Each donor repository was inspected at its exact pinned commit for root and nested `LICENSE` / `COPYING` / `NOTICE` files, source file copyright/SPDX headers, git commit author history (`git shortlog -sne --all`), bundled assets, and native binaries:

| Donor Repository | Pinned Commit SHA | Root `LICENSE` File | Nested License Files | Source Copyright / SPDX Headers | Git Commit Authorship (`git shortlog -sne --all`) | Provenance & Reuse Clearance Status |
|---|---|---|---|---|---|---|
| **[agent-created-by-gemini](https://github.com/iam816anonymous-commits/agent-created-by-gemini)** | `90f5cf3ca0eca324f91a43ba96fab72c2e2de5ed` | **NONE** (No `LICENSE`, `COPYING`, or `NOTICE` file in repository) | None (`package.json` is `"private": true` with no `"license"` field) | **0 SPDX/copyright headers** across all 59 Kotlin source/test files (`grep -rnI "Copyright\|SPDX\|Licensed under"` matches only functional strings inside `ModelDownloadManager.kt` and `OfflineBenchmarkHarness.kt`) | `14` commits by `Vivek <iam816anonymous@gmail.com>`; `2` commits by `google-labs-jules[bot]` | First-party owner-authored code (`iam816anonymous-commits`), **PROVISIONAL PENDING EXPLICIT OWNER LICENSE DECLARATION** (recommend adding root `LICENSE` — Apache-2.0 or MIT — before copying/adapting any class into Phase 1+). |
| **[mobile_agent](https://github.com/iam816anonymous-commits/mobile_agent)** | `863723f7aa4b42137ff4ce07e26e541e402ff5a8` | **NONE** | None | **0 SPDX/copyright headers** across all 22 Kotlin source/test files | `6` commits by `google-labs-jules[bot]` (`Co-authored-by: iam816anonymous-commits <198982749+Copilot@users.noreply.github.com>`) | First-party repository, **PROVISIONAL PENDING EXPLICIT OWNER LICENSE DECLARATION**. Architecture/module boundaries used as `REFERENCE` / `ADAPT` only. |
| **[skill-agent](https://github.com/iam816anonymous-commits/skill-agent)** | `86cfa6960b1341ed53b987f5a50b1c623ec18ad4` | **NONE** | None | None (16 markdown files only) | `1` commit by `google-labs-jules[bot]` | First-party specification repository (`REFERENCE` only; no code exists). |
| **[agent-1](https://github.com/iam816anonymous-commits/agent-1)** | `6d0c3002fc4c716dea2ffcba31339f0de79fd087` | **NONE** | None | **0 SPDX/copyright headers** across all 19 Kotlin source/test files | `5` commits by `google-labs-jules[bot]` | First-party repository, **PROVISIONAL PENDING EXPLICIT OWNER LICENSE DECLARATION**. `:app` fails clean build; used for `REFERENCE` / selective `ADAPT` of pure-JVM `:core` concepts only. |
| **[tecno-os](https://github.com/iam816anonymous-commits/tecno-os)** | `c8a23e359de5b30297ca731f713b89077e056cfc` / `d2aeac357141e75b601a935869abb8944ad0d41d` | **MIT License** (`LICENSE`: `Copyright (c) 2026 iam816anonymous-commits`) | `docs/legal/component-licenses.md` (separates MIT original scripts/docs from GPL-2.0 Linux kernel references) | Documented in `LICENSE` and `docs/legal/component-licenses.md` | `31` commits by `google-labs-jules[bot]` | **CLEARED FOR HARDWARE EVIDENCE REFERENCE ONLY** (MIT). No kernel/GPL code is used or linked. |

---

## 2. Source-vs-Specification Discrepancy Audit (`agent-created-by-gemini` @ `90f5cf3ca0eca324f91a43ba96fab72c2e2de5ed`)

Inspection of every file in `agent-created-by-gemini` at `90f5cf3ca0eca324f91a43ba96fab72c2e2de5ed` against `RESEARCH/SOURCE_BY_SOURCE_REUSE_SPECIFICATION.md` uncovered five critical discrepancies that must govern Phase 1–7 engineering:

### 2.1 Commit `90f5cf3` ("feat(hardware): implement Google ML Kit perception integration") Modifies Zero Android/Kotlin Files
- `git show --stat 90f5cf3ca0eca324f91a43ba96fab72c2e2de5ed` shows that commit `90f5cf3` touched **only 7 TypeScript/React web prototype files** (`package.json`, `package-lock.json`, `src/App.tsx`, `src/agent/AgentRuntime.ts`, `src/agent/ExecutionEngine.ts`, `src/hardware/MLKitPerceptionBridge.ts`, `src/hardware/PerceptionTestHarness.ts`).
- **Zero Kotlin or Android files under `app/src/` were modified in `90f5cf3`.** The last commit touching Android Kotlin code in `agent-created-by-gemini` is merge commit `6514b4f903bf4efdb487cc7758101868c6556f32` (merging PR #3 `1c7d8aa8a1b5b9cf27219705ef3ddfa4260e7eb4`, branch `research/verified-execution-safety`).

### 2.2 What Merged PR #3 (`1c7d8aa`) Fixed vs What Remains Broken at `90f5cf3`
Merged commit `1c7d8aa` ("Implement verified execution, safety redaction, and honest telemetry") fixed three previously audited issues:
1. `StepVerifier.verifyRule()` (`app/src/main/java/com/example/myapplication/agent/StepVerifier.kt:132-138`): `VerificationRule.AlwaysPass` now returns `StepStatus.INCONCLUSIVE` (`confidence = 0.0f`) instead of `StepStatus.SUCCESS`.
2. `DeviceCapabilityDetector.getStorageMetrics()` (`app/src/main/java/com/example/myapplication/agent/DeviceCapabilityDetector.kt:129-144`): returns `StorageMetrics? = null` on failure instead of fabricated `64_000L / 24_000L` MB.
3. `AgentExecutionEngine.executeGoal()` (`app/src/main/java/com/example/myapplication/agent/AgentExecutionEngine.kt:212-233`): checks `executionMode == ExecutionMode.LIVE` and refuses to synthesize a mock `[Search Bar]` UI snapshot when the live accessibility snapshot is empty.

**However, the following severe defects remain in `90f5cf3ca0eca324f91a43ba96fab72c2e2de5ed` and violate our core execution invariants:**
1. **`StepVerifier.verify()` still returns hardcoded `StepStatus.SUCCESS` without postcondition evidence (`StepVerifier.kt:93-118`):**
   - Lines `93-98`: `is AgentAction.ControlActuator -> VerificationResult(StepStatus.SUCCESS, "Actuator ${action.actuatorType} command dispatched with state ${action.value}", confidence = 0.9f)` (pure success-by-dispatch).
   - Lines `99-104`: `is AgentAction.Delay -> VerificationResult(StepStatus.SUCCESS, "Delayed for ${action.ms}ms", confidence = 1.0f)`.
   - Lines `105-110`: `is AgentAction.StartTimer -> VerificationResult(StepStatus.SUCCESS, "Timer started for ${action.durationSeconds}s", confidence = 0.9f)`.
   - Lines `111-116`: `is AgentAction.WebResearch -> VerificationResult(StepStatus.SUCCESS, "Web research completed for query: ${action.query}", confidence = 0.9f)`.
   - Lines `117-118`: `else -> VerificationResult(StepStatus.SUCCESS, "Action completed", confidence = 0.9f)`.
2. **`UiSnapshot.monotonicTimestampMs` is still initialized from wall-clock time (`Models.kt:366`):**
   - `val timestampMs: Long = System.currentTimeMillis(), val monotonicTimestampMs: Long = timestampMs`. Any NTP clock jump or user time change corrupts snapshot staleness checks (`currentSnapshot.monotonicTimestampMs - snapshot.monotonicTimestampMs`). Must use `SystemClock.elapsedRealtime()`.
3. **`LocalSlmIntentEngine.extractIntentOnDemand()` still fakes grammar validation on regex fallback (`LocalSlmIntentEngine.kt:238-254`):**
   - When native JNI inference fails or is absent (always true because `libllama-bridge.so` does not exist), `extractIntentOnDemand` falls back to `DeterministicRegexParser.parse(transcript)` and returns `SlmIntentResult(..., confidence = if (intent != null) 0.80f else 0.0f, grammarValidated = true)`.
4. **`LlamaAndroidAdapter.computeMetrics()` still fabricates RAM telemetry (`LlamaAndroidAdapter.kt:153`):**
   - Computes `val peakRamMb = baselineRamMb + profile.requiredRamMb` by adding a static catalog constant to baseline RAM instead of reading `Debug.MemoryInfo` PSS/RSS.
5. **`ModelDownloadManager.isModelDownloaded()` still accepts truncated/corrupted files (`ModelDownloadManager.kt:71-76`):**
   - Returns `file.exists() && file.length() >= (profile.sizeBytes * 0.8).toLong()` with **no SHA-256 verification**.
6. **Duplicate classes in the same package (`com.example.myapplication.agent`):**
   - `SnapshotDiffer.kt` (`class SnapshotDiffer`) duplicates `AccessibilityNodeDiffer.kt` (`object AccessibilityNodeDiffer`).
   - `ExecutionTracer.kt` (`class ExecutionTracer`) collides conceptually with `object ExecutionTracer` defined inside `AccessibilityNodeDiffer.kt:52`.

### 2.3 Complete Inventory of 34 Additional Classes in `agent-created-by-gemini` Not Listed in `RESEARCH/SOURCE_BY_SOURCE_REUSE_SPECIFICATION.md`

`agent-created-by-gemini` contains **46 Kotlin files** under `app/src/main/java/com/example/myapplication/`. Only 12 were tabulated in `RESEARCH/SOURCE_BY_SOURCE_REUSE_SPECIFICATION.md` §3.1. Below is the complete audit and mandatory disposition for all 46 files:

---

## 3. Component-Level Provenance and Reuse Ledger (`agent-created-by-gemini`)

| File Path (`app/src/main/java/com/example/myapplication/...`) | Lines | Recorded Spec Decision | Argon Verified Decision | Provenance & Defects Found at `90f5cf3ca0eca324f91a43ba96fab72c2e2de5ed` |
|---|---|---|---|---|
| `agent/Models.kt` | 531 | **ADAPT** | **ADAPT** | First-party (`Vivek`). Rich sealed classes (`AgentAction`, `VerificationRule`, `StepStatus`, `UiNode`, `UiSnapshot`, `ModelProfile`). **Defects to fix:** (1) `monotonicTimestampMs` defaults to `System.currentTimeMillis()`; (2) `ModelProfile` hardcodes placeholder `"sha256:e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855"` (the SHA-256 of an empty 0-byte string!) on lines `436, 452, 468, 484`. |
| `agent/TargetResolver.kt` | 209 | **ADAPT** | **ADAPT** | First-party. Rejects `isPassword` nodes (`L29`) and rejects ambiguous text matches without `viewId` (`L65`). **Defect to fix:** Still allows ungrounded raw coordinate fallback (`targetX`/`targetY` at `L139`) without requiring a valid `snapshotId` + visual confirmation token. |
| `agent/StepVerifier.kt` | 269 | **REWRITE** | **REWRITE** | First-party. Although `AlwaysPass` was changed to `INCONCLUSIVE` in PR #3 (`L134`), `verify()` (`L93-118`) still returns hardcoded `StepStatus.SUCCESS` for `ControlActuator`, `Delay`, `StartTimer`, `WebResearch`, and `else`. Must be rewritten so every action maps to an independent postcondition check. |
| `agent/CapabilityGatekeeper.kt` | 64 | **ADAPT** | **ADAPT** | First-party. Pure deterministic risk-tier classifier (`SAFE_READ_ONLY`, `LOW_RISK_UI_NAVIGATION`, `MEDIUM_RISK_STATE_CHANGE`, `HIGH_RISK_SENSITIVE_EXTERNAL`, `PROHIBITED_HARD_BLOCK`) and sensitive keyword blocker (`password`, `otp`, `bank`, `upi`, `pay`, `delete account`). Reusable after binding to canonical `ActionEnvelope` and package allow/blocklists. |
| `agent/SafetyApprovalGate.kt` | 150 | **ADAPT** | **ADAPT** | First-party. Coroutine `CompletableDeferred` approval gate with 60s timeout and fail-closed timeout rejection. **Defect to fix:** Must bind approval cryptographically/deterministically to `(actionId, actionHash, targetWindowPackage, snapshotId, expiresAtElapsedRealtimeMs)` so plan mutations invalidate approval. |
| `agent/AgentAccessibilityService.kt` | 401 | **ADAPT** | **ADAPT** | First-party. Extracts bounded UI tree (`MAX_TRAVERSAL_DEPTH = 25`, `MAX_NODE_COUNT = 250`), redacts password text (`[REDACTED_PASSWORD]`), checks `FLAG_SECURE`, and dispatches gestures/clicks. **Defects to fix:** (1) `findTargetNode` (`L354`) falls back to first matching text node without ambiguity rejection if called directly; (2) gesture dispatch (`clickAtCoordinates`, `scroll`, `swipe`) returns `true` as soon as `dispatchGesture` queues the stroke without awaiting `GestureResultCallback.onCompleted`. |
| `agent/DeviceCapabilityDetector.kt` | 178 | **ADAPT** | **ADAPT** | First-party. Queries `ActivityManager.MemoryInfo`, `StatFs`, `Build.SUPPORTED_ABIS`, and `VulkanHardwareProber`. PR #3 removed fake 64 GB / 24 GB storage fallback. **Defect to fix:** Move `ModelCatalog` out of `DeviceCapabilityDetector.kt` into a dedicated model registry and remove empty-string SHA-256 placeholders. |
| `agent/HardwareTelemetryProvider.kt` | 127 | **ADAPT** | **ADAPT** | First-party. Reads `BatteryManager`, `ActivityManager`, `StatFs`, `PowerManager` thermal status (API 29+), and `/sys/class/thermal/thermal_zone0/temp` fallback for API 27–28. Clean and reusable with null-safe error types. |
| `agent/HardwareActuatorControl.kt` | 206 | **REWRITE** | **REWRITE** | First-party. Directly mutates Wi-Fi, Bluetooth, Flashlight (`CameraManager.setTorchMode`), Brightness (`Settings.System`), and Audio stream volume (`AudioManager`), returning boolean dispatch status without postcondition verification or pre-mutation state snapshots for rollback. |
| `agent/AgentExecutionEngine.kt` | 619 | **REWRITE** | **REWRITE** | First-party. Monolithic god-object combining dry-run simulation, live execution, memory lookup, and UI state flows. PR #3 added `ExecutionMode.LIVE` guards, but it still relies on `StepVerifier`'s hardcoded `SUCCESS` branches and lacks a durable crash-recovery journal (`PendingVerification` state). |
| `agent/LocalSlmIntentEngine.kt` | 280 | **REWRITE** | **REWRITE** | First-party. Mixes GBNF grammar string generation, `LlamaAndroidAdapter` JNI bridge, and `DeterministicRegexParser` fallback that falsely sets `grammarValidated = true` (`L252`). Separate deterministic command parsing (Phase 5) from local LLM runtime (Phase 6). |
| `agent/LlamaAndroidAdapter.kt` | 238 | **REJECT / REWRITE** | **REJECT** | First-party stub. Calls `System.loadLibrary("llama-bridge")` (`L43`), which does not exist anywhere in the repo, and fabricates `peakRamMb = baselineRamMb + profile.requiredRamMb` (`L153`). Reject completely. |
| `agent/AccessibilityNodeDiffer.kt` | 88 | *Unlisted in Spec* | **ADAPT** | First-party. Computes structural hash and node-set delta (`addedNodes`, `removedNodes`, `textChanges`) and detects action-state oscillation loops (`detectLoop`). Useful for Phase 2/4 loop detection; remove the duplicate `object ExecutionTracer` embedded at `L52-87`. |
| `agent/SnapshotDiffer.kt` | 83 | *Unlisted in Spec* | **REJECT (Duplicate)** | First-party. Duplicates `AccessibilityNodeDiffer.kt` (`computeDiff`, `hasMeaningfulChange`). Consolidate into a single `UiSnapshotDiffer` in Phase 2. |
| `agent/ExecutionTracer.kt` | 109 | *Unlisted in Spec* | **ADAPT** | First-party. Redacts sensitive values (`password`, `pin`, `otp`, `secret`, `token`, `cvv`, `card`) via regex before recording `TraceEntry`. Adapt redaction rules into Phase 1 structured logger. |
| `agent/SemanticTreePruner.kt` | 150 | *Unlisted in Spec* | **ADAPT** | First-party. Filters non-semantic structural containers while keeping interactive/labeled nodes and normalizes coordinates to `[0, 1000]`. Useful in Phase 2/6 for compact LLM prompt formatting. |
| `agent/RetryPolicyEngine.kt` | 99 | *Unlisted in Spec* | **ADAPT** | First-party. Deterministic exponential backoff with jitter and transient vs permanent failure classification. Reusable in Phase 3/4. |
| `agent/DeterministicCommandParser.kt` | 177 | *Unlisted in Spec* | **ADAPT** | First-party. Deterministic rule/regex parser for high-frequency device commands (flashlight, volume, open app, scroll, home/back, alarm/timer). Honest confidence scoring; reuse in Phase 4/5 as fast-path parser with explicit `grammarValidated = false`. |
| `agent/SystemReadinessValidator.kt` | 89 | *Unlisted in Spec* | **ADAPT** | First-party. Checks accessibility service state, overlay permission, memory headroom, and storage before task execution. Useful for Phase 1/2 diagnostics screen. |
| `agent/VulkanHardwareProber.kt` | 101 | *Unlisted in Spec* | **ADAPT** | First-party. Queries `PackageManager.FEATURE_VULKAN_HARDWARE_LEVEL` / `VERSION` and checks SoC/device properties (`MT6763`, `TECNO IN6`). Align with verified `tecno-os` GPUProbe findings in Phase 3A/6. |
| `agent/PermissionManager.kt` | 140 | *Unlisted in Spec* | **ADAPT** | First-party. Checks runtime permissions, `Settings.canDrawOverlays`, `PowerManager.isIgnoringBatteryOptimizations`, and `Settings.System.canWrite`. Useful for Phase 1/3A permission status checks. |
| `agent/IntentDispatcher.kt` | 206 | *Unlisted in Spec* | **ADAPT** | First-party. Dispatches standard Android `AlarmClock`, `Intent.ACTION_VIEW`, `Intent.ACTION_SENDTO`, and package launch intents with `resolveActivity` checks. Adapt into Phase 3 intent adapter with postcondition verification. |
| `agent/VoiceInteractionController.kt` | 184 | *Unlisted in Spec* | **ADAPT** | First-party. Wraps `SpeechRecognizer` and `TextToSpeech` with state flows (`IDLE`, `LISTENING`, `PROCESSING`, `SPEAKING`, `ERROR`). Candidate for Phase 5 push-to-talk after adding explicit permission & lifecycle teardown guards. |
| `agent/ConversationSessionManager.kt` | 135 | *Unlisted in Spec* | **ADAPT** | First-party. Manages multi-turn conversation context and pronoun/entity carryover (`it`, `that app`) with turn limits. Candidate for Phase 5/7. |
| `agent/WorkingMemoryManager.kt` | 112 | *Unlisted in Spec* | **ADAPT** | First-party. Bounded short-term context window with token estimation and salience-based eviction. Candidate for Phase 4/7. |
| `agent/EpisodicMemoryStore.kt` | 122 | *Unlisted in Spec* | **REWRITE** | First-party. In-memory/SharedPreferences list of task episodes using naive word-overlap similarity and unencrypted JSON persistence. Rewrite onto Room with explicit redaction and retention controls in Phase 7. |
| `agent/SemanticMemoryDatabase.kt` | 127 | *Unlisted in Spec* | **REWRITE** | First-party. In-memory subject-predicate-object triples persisted via SharedPreferences JSON. Rewrite onto Room in Phase 7. |
| `agent/AgentLearningPersistence.kt` | 199 | *Unlisted in Spec* | **REWRITE** | First-party. Stores all episodic memories, skills, semantic facts, and audit logs in a single unencrypted `SharedPreferences("agent_learning_prefs")` JSON blob. Replace with Room + `AndroidKeyStore` encryption in Phase 1/7. |
| `agent/SkillLibraryManager.kt` | 182 | *Unlisted in Spec* | **REWRITE** | First-party. Auto-promotes trajectories into reusable skills without user approval (`generateSkillFromTrajectory`). Violates Phase 7 rule requiring explicit user approval before saving learned skills. |
| `agent/SkillGraphEngine.kt` | 172 | *Unlisted in Spec* | **REFERENCE** | First-party. DAG dependency resolver for composable skills. Reference only for Phase 7. |
| `agent/SelfReflectionEngine.kt` | 130 | *Unlisted in Spec* | **ADAPT** | First-party. Deterministic heuristic failure analyzer (maps `TARGET_NOT_FOUND` -> `SCROLL_DOWN`, modal dialog -> dismiss). Adapt as bounded deterministic recovery heuristics in Phase 4. |
| `agent/HardwareAwarenessEngine.kt` | 109 | *Unlisted in Spec* | **ADAPT** | First-party. Throttles agent concurrency/model tier based on battery percentage and thermal status. Merge with `HardwareTelemetryProvider` in Phase 3A/6. |
| `agent/ModelDownloadManager.kt` | 150 | *Unlisted in Spec* | **REWRITE** | First-party. Uses `DownloadManager`, accepts files `>= 0.8 * sizeBytes` as valid (`L75`), and does not verify SHA-256 digests. Must be rewritten in Phase 6 to enforce exact byte length and streaming SHA-256 verification before atomic file rename. |
| `agent/DynamicToolRegistry.kt` | 162 | *Unlisted in Spec* | **REWRITE** | First-party. Allows runtime registration of arbitrary lambda tools with string map parameters (`Map<String, String>`). Replace with closed, compile-time typed `AgentAction` vocabulary in Phase 1/3. |
| `agent/WakeWordService.kt` | 173 | *Unlisted in Spec* | **REJECT / REWRITE** | First-party. Uses continuous `SpeechRecognizer` restart loop in a foreground service as a pseudo-wake-word detector (high battery drain, triggers system mic indicators/beeps, fails on API 27/OEM background killers). Reject for Phase 8; use dedicated low-power DSP/AudioRecord KWS (`Sherpa-ONNX` / `openWakeWord`) gated behind Phase 8 spike. |
| `agent/AutonomousCuriosityAgent.kt` | 314 | *Unlisted in Spec* | **REJECT** | First-party. Unsupervised random UI explorer ("curiosity-driven autonomous exploration" firing random clicks/scrolls across installed apps). Violates our strict user-goal-driven safety and approval architecture. |
| `agent/OvernightConsolidationEngine.kt` | 127 | *Unlisted in Spec* | **REJECT** | First-party. Background charging consolidation job that synthesizes unapproved skills. Violates Phase 7 user-approved learning constraints. |
| `agent/CognitiveBrainEngine.kt` | 206 | *Unlisted in Spec* | **REJECT** | First-party. Speculative "System 1 / System 2 neurotransmitter/dopamine" simulation layer with hardcoded heuristics. Reject. |
| `agent/FailsafeRecoveryRing.kt` | 156 | *Unlisted in Spec* | **REJECT** | First-party. Blind 4-stage recovery ring that fires `BACK`, `HOME`, and force-restarts packages without checking whether a non-idempotent action is in flight. Violates Phase 3/4 recovery safety rules. |
| `agent/HelioCorePilotAndMemoryOptimizer.kt` | 168 | *Unlisted in Spec* | **REJECT** | First-party. Pretends to tune MediaTek CorePilot scheduler and `madvise` via simulated calculations without native/root access. Reject. |
| `agent/VisualInteractionRecorder.kt` | 174 | *Unlisted in Spec* | **REJECT** | First-party. Records user screen interactions to replay as macros without sensitive-app filtering or redaction. Reject. |
| `agent/VisualOverlayRenderer.kt` | 145 | *Unlisted in Spec* | **REFERENCE** | First-party. Canvas bounding-box overlay model. Reference only for optional debug overlay in Phase 2. |
| `agent/LocalAgentOverlayService.kt` | 191 | *Unlisted in Spec* | **REWRITE** | First-party. `TYPE_APPLICATION_OVERLAY` floating stop/status pill. Rewrite in Phase 1/4 with clean foreground-service lifecycle and immediate global `CancelToken` trigger. |
| `agent/OfflineBenchmarkHarness.kt` | 148 | *Unlisted in Spec* | **REJECT** | First-party. Runs synthetic in-memory mock benchmark tasks with hardcoded pass rates. Replace with real deterministic test harness (`INITIAL_TASK_BENCHMARK.md`). |
| `agent/RemoteAgentBridge.kt` | 198 | *Unlisted in Spec* | **REJECT (Out of Scope / Security Risk)** | First-party. Opens a local TCP `ServerSocket` (`port 8765`) accepting JSON remote-control commands over the network. Violates our non-goals (no remote access server or unauthenticated socket bridge). |
| `MainActivity.kt` | 1152 | *Unlisted in Spec* | **REWRITE** | First-party. 1,152-line single-file Compose UI mixing debug simulation tabs, fake telemetry charts, and curiosity mode controls. Rewrite clean modular Compose UI in Phase 1. |

---

## 4. Component-Level Provenance and Reuse Ledger (`mobile_agent`, `agent-1`, `skill-agent`, `tecno-os`)

| Donor Repository & Commit | Module / File Path | Lines | Recorded Spec Decision | Argon Verified Decision | Provenance & Technical Findings |
|---|---|---|---|---|---|
| **`mobile_agent`** (`863723f`) | `settings.gradle.kts`, `:core`, `:accessibility`, `:ai`, `:memory`, `:tools`, `:app` | 6 modules | **ADAPT (Structure)** | **ADAPT (Structure Only)** | Clean multi-module separation. Do **not** copy build scripts verbatim: upgrade to Kotlin `2.0.21` + KSP and add missing Room KSP compiler. |
| **`mobile_agent`** (`863723f`) | `core/src/main/java/com/android/agentos/core/Models.kt` & `EventBus.kt` | 68 + 26 | **ADAPT** | **REFERENCE** | Simple `Action`, `UIElement`, `ScreenState`, and `SharedFlow` `EventBus`. `agent-created-by-gemini`'s `Models.kt` is more complete for risk tiers and verification rules; merge only clean IDs. |
| **`mobile_agent`** (`863723f`) | `accessibility/src/main/java/com/android/agentos/accessibility/AgentAccessibilityService.kt` | 162 | **ADAPT** | **ADAPT (Gesture Callback Pattern)** | Unlike `agent-created-by-gemini`, `mobile_agent` uses `suspendCancellableCoroutine` with `GestureResultCallback.onCompleted` / `onCancelled` (`L115-146`) so gesture dispatch suspends until the stroke actually completes or fails. **Adopt this exact coroutine callback pattern into Phase 2/3.** |
| **`mobile_agent`** (`863723f`) | `ai/src/main/java/com/android/agentos/ai/LocalLLMService.kt`, `ModelManager.kt`, `Planner.kt` | 91 + 91 + 98 | **REWRITE** | **REJECT / REWRITE** | `LocalLLMService` wraps `com.google.mediapipe:tasks-genai:0.10.11` (`LlmInference`), which fails on TECNO IN6 due to `libOpenCL.so` namespace restrictions. `Planner.kt` falls back to hardcoded Chrome/Settings keyword scripts (`L44-86`). |
| **`mobile_agent`** (`863723f`) | `memory/src/main/java/com/android/agentos/memory/AgentDatabase.kt` & `MemoryManager.kt` | 88 + 64 | **ADAPT** | **REWRITE** | Missing KSP/KAPT `room-compiler` in `memory/build.gradle.kts` causes `AgentDatabase_Impl does not exist` crash at runtime. `MemoryManager.findSimilarEpisode` uses naive string `LIKE %word%`. |
| **`mobile_agent`** (`863723f`) | `tools/src/main/java/com/android/agentos/tools/ShellTool.kt` | 36 | **REJECT** | **REJECT (Hard Prohibition)** | Executes arbitrary `Runtime.getRuntime().exec(command)` (`L13`). Explicitly prohibited by `SAFETY_AND_APPROVALS.md`. |
| **`agent-1`** (`6d0c300`) | `core/src/main/kotlin/com/agent/core/Contracts.kt` & `Orchestrator.kt` | 109 + 146 | **ADAPT / REWRITE** | **REFERENCE** | Pure-JVM contracts (`ExecutionState`, `StepResult`, `ConfirmableSkill`). `Orchestrator.kt` has no postcondition verification or snapshot binding. |
| **`agent-1`** (`6d0c300`) | `core/src/main/kotlin/com/agent/core/learning/LearningEngine.kt` | 127 | **ADAPT** | **ADAPT (Concept)** | Pure-JVM parameter frequency suggestion engine (`suggestParameters`). Adapt concept for Phase 7 user-approved parameter defaults. |
| **`agent-1`** (`6d0c300`) | `infrastructure/src/main/kotlin/com/agent/infrastructure/...` & `app/src/main/java/...` | All files | **REWRITE / REJECT** | **REJECT** | `:app` fails clean build with 3 stacked defects (missing `gradle.properties`, heap thrash, missing `res/` directory, missing Room KSP). `Providers.kt` contains mock calendar/email/notes memory stores. |
| **`skill-agent`** (`86cfa69`) | `docs/03_SKILL_SYSTEM_DESIGN.md`, `docs/05_CAPABILITY_AND_PERMISSION_MODEL.md`, `docs/12_SECURITY_MODEL.md` | Markdown | **ADAPT (Spec)** | **REFERENCE (Spec)** | Specification-only documents. Inform Phase 3 capability gating and Phase 7 declarative skill schema. |
| **`tecno-os`** (`c8a23e3` / `d2aeac3`) | `inventory/properties.txt`, `inventory/cpuinfo.txt`, `docs/hardware/gpuprobe-apk-validation-2026-11-02.md` | Evidence files | **REFERENCE** | **REFERENCE (Hardware Evidence Only)** | MIT-licensed physical device logs from TECNO Spark 2 (`TECNO IN6`, `MT6763`, Android 8.1.0 API 27, 8x Cortex-A53 `r0p4` without `dotprod`/`fp16`, Mali-G71 MP2 Vulkan `1.0.58`). |

---

## 5. Candidate Transitive Dependency and License Ledger

Per `ENGINEERING/ARGON/ARGON_DEVELOPMENT_HANDOFF.md` §2.5 and `PROJECT_STATUS.md` §6.4, this ledger covers **only the candidate dependencies proposed for the destination Android agent** (plus rejected donor dependencies explicitly flagged to prevent accidental inclusion). Every license below was verified directly from the dependency's Maven `.pom` metadata in the Gradle cache (`~/.gradle/caches/modules-2/files-2.1/.../*.pom`):

### 5.1 Approved Candidate Dependencies for Phase 1–7 Destination Architecture

| Maven Coordinate (`group:artifact:version`) | Scope / Target Phase | POM-Verified License | SPDX Identifier | Native Binaries (`.so`) | Provenance & Compliance Notes |
|---|---|---|---|---|---|
| `org.jetbrains.kotlin:kotlin-stdlib:2.0.21` | Phase 1+ Core | The Apache License, Version 2.0 | `Apache-2.0` | None | Official JetBrains Kotlin runtime. |
| `org.jetbrains.kotlinx:kotlinx-coroutines-core-jvm:1.8.0` / `kotlinx-coroutines-android:1.8.0` | Phase 1+ Concurrency | The Apache Software License, Version 2.0 | `Apache-2.0` | None | Structured concurrency for execution loop, timeouts, and `AccessibilityService` gesture callbacks. |
| `org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3` | Phase 1+ Typed Contracts & Journals | The Apache Software License, Version 2.0 | `Apache-2.0` | None | Preferred over Gson/Moshi for closed polymorphic `AgentAction` / `VerificationRule` serialization without reflection or R8 rule fragility. |
| `androidx.core:core-ktx:1.13.1` (`androidx.core:core:1.13.1`) | Phase 1+ Android Compat | The Apache Software License, Version 2.0 | `Apache-2.0` | None | `minSdk 24` compatible (`API 27` safe). |
| `androidx.lifecycle:lifecycle-runtime-ktx:2.8.6` & `lifecycle-viewmodel-compose:2.8.6` | Phase 1+ UI & Service Lifecycle | The Apache Software License, Version 2.0 | `Apache-2.0` | None | `Apache-2.0`. |
| `androidx.activity:activity-compose:1.9.3` | Phase 1+ UI Host | The Apache Software License, Version 2.0 | `Apache-2.0` | None | `Apache-2.0`. |
| `androidx.compose:compose-bom:2024.09.03` (`ui`, `material3`, `foundation`, `runtime`, `graphics`) | Phase 1+ UI | The Apache Software License, Version 2.0 | `Apache-2.0` | `libandroidx.graphics.path.so` (`arm64-v8a`, `armeabi-v7a`, `x86`, `x86_64`) | Official AndroidX Jetpack Compose UI toolkit (`Apache-2.0`). |
| `androidx.room:room-runtime:2.6.1`, `room-ktx:2.6.1`, `room-compiler:2.6.1` (via KSP) | Phase 1 Task Journal & Phase 7 Memory | The Apache Software License, Version 2.0 | `Apache-2.0` | None (uses platform SQLite) | **Mandatory rule:** Must apply `com.google.devtools.ksp` with `ksp(libs.androidx.room.compiler)` so `*_Impl` classes are generated at compile time. |
| `androidx.datastore:datastore-preferences:1.0.0` / `1.1.1` | Phase 1 Settings & Flags | The Apache Software License, Version 2.0 | `Apache-2.0` | None | Replaces unencrypted `SharedPreferences` for lightweight agent settings. |
| `junit:junit:4.13.2` | Phase 1+ JVM Unit Tests (`testImplementation`) | Eclipse Public License 1.0 | `EPL-1.0` | None | Test-only scope (`testImplementation`); never packaged in APK. |
| `io.mockk:mockk:1.13.10` | Phase 1+ JVM Unit Tests (`testImplementation`) | Apache License, Version 2.0 | `Apache-2.0` | None | Test-only scope (`testImplementation`). |
| `org.jetbrains.kotlinx:kotlinx-coroutines-test:1.8.0` | Phase 1+ Coroutine Unit Tests (`testImplementation`) | The Apache Software License, Version 2.0 | `Apache-2.0` | None | Test-only scope (`testImplementation`). |

### 5.2 Conditionally Deferred or Rejected Donor Dependencies

| Maven Coordinate (`group:artifact:version`) | Present In Donor | POM-Verified License | Disposition | Technical & Legal Reason |
|---|---|---|---|---|
| `com.google.mediapipe:tasks-genai:0.10.11` | `mobile_agent` (`:ai`) | The Apache Software License, Version 2.0 (`Apache-2.0`) | **REJECT for TECNO IN6 / API 27** | Bundles `lib/arm64-v8a/libllm_inference_engine_jni.so` (`5.51 MiB`), which links against `libOpenCL.so`. Physical-device testing on TECNO IN6 (`gpuprobe-apk-validation-2026-11-02.md`) proved `dlopen("libOpenCL.so")` is blocked by the Android linker namespace for third-party APKs. |
| `com.google.ai.edge.litertlm:litertlm-android:0.10.0` / `0.19.0` | Candidate evaluated in Spike 1 & 3 | The Apache Software License, Version 2.0 (`Apache-2.0`) | **DEFER to Phase 6 Optional Module (`:model-litert`)** | `minSdk 24`, bundles `liblitertlm_jni.so` (`22.69 MiB` arm64-v8a). Requires Kotlin `2.2+` metadata for newer versions (`0.10.0` = Kotlin `2.2.21`, `0.19.0` = Kotlin `2.4.0`). Keep out of Phase 1–5 core modules; isolate in Phase 6 runtime PoC. |
| `com.google.adk:google-adk-kotlin-core-android:0.2.0` (and JVM `com.google.adk:google-adk:1.4.0`) | Candidate evaluated in Spike 1 | The Apache License, Version 2.0 (`Apache-2.0`) | **REJECT for Core Loop / DEFER** | `minSdk 26`, compiled with Kotlin `2.1.20`, pulls in `com.google.mlkit:genai-prompt:1.0.0-beta2` (AICore-dependent, unsupported on API 27 / TECNO IN6), `com.google.genai:google-genai:1.44.0`, `protobuf-javalite`, and `google-auth-library-oauth2-http`. Overweight and incompatible with local-first API 27 deterministic orchestration. |
| `com.google.code.gson:gson:2.10.1` | `agent-created-by-gemini` (`:app`) | Apache-2.0 | **REPLACE with `kotlinx-serialization-json`** | Reflection-based serialization breaks under R8 minification unless fragile ProGuard rules are maintained (as demonstrated by `agent-created-by-gemini`'s broken `app/proguard-rules.pro`). |
| `androidx.security:security-crypto:1.1.0-alpha06` | `mobile_agent` (declared in TOML, unused) | Apache-2.0 | **REJECT (`Deprecated` by Google)** | `security-crypto` (`EncryptedSharedPreferences` / `EncryptedFile`) is officially deprecated by AndroidX and remained stuck in alpha (`1.1.0-alpha06`). Use standard `java.security.KeyStore` (`AndroidKeyStore`) with `AES/GCM/NoPadding` directly (see `PHASE_0_TECHNICAL_SPIKES.md` §5). |

---

## 6. Model Catalog License & Checksum Audit (`agent-created-by-gemini` `Models.kt:423-496`)

`agent-created-by-gemini` defines four entries in `ModelCatalog` (`Models.kt:423-496`). None of these model weights are bundled in the repository or APK, and **all four entries contain a critical integrity defect**:

| Catalog ID | Model Name | Size Claimed | Min RAM Claimed | URL in `Models.kt` | Recorded `sha256` in `Models.kt` | Upstream Model License | Audit Finding |
|---|---|---|---|---|---|---|---|
| `smollm2-135m-q4` | SmolLM2 135M Instruct (Q4_K_M) | `105 MB` | `512 MB` | HuggingFace `HuggingFaceTB/SmolLM2-135M-Instruct-GGUF` | `sha256:e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855` | `Apache-2.0` | **DEFECT:** The hardcoded hash `e3b0c442...` is the SHA-256 of an **empty 0-byte string**! Must replace with real artifact SHA-256 in Phase 6. |
| `qwen2.5-0.5b-q4` | Qwen2.5 0.5B Instruct (Q4_K_M) | `398 MB` | `1024 MB` | HuggingFace `Qwen/Qwen2.5-0.5B-Instruct-GGUF` | `sha256:e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855` | `Apache-2.0` | **DEFECT:** Uses the same fake empty-string SHA-256 hash. |
| `llama-3.2-1b-q4` | Llama 3.2 1B Instruct (Q4_K_M) | `815 MB` | `2048 MB` | HuggingFace `bartowski/Llama-3.2-1B-Instruct-GGUF` | `sha256:e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855` | **Llama 3.2 Community License Agreement** (Custom source-available license with acceptable-use & attribution requirements) | **DEFECT + LICENSE GATE:** Uses fake empty-string SHA-256 hash, requires custom license acceptance, and exceeds safe RAM headroom on the 2 GB TECNO IN6 (`MemAvailable ~1.03 GiB`). |
| `gemma-2-2b-q4` | Gemma 2 2B Instruct (Q4_K_M) | `1620 MB` | `3072 MB` | HuggingFace `bartowski/gemma-2-2b-it-GGUF` | `sha256:e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855` | **Gemma Terms of Use** (Custom terms) | **DEFECT + INCOMPATIBLE WITH 2 GB RAM:** Uses fake empty-string SHA-256 hash and cannot run on 2 GB RAM devices. |
