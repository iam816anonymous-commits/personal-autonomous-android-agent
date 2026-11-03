# Phase 0 Technical Spikes, Architectural Decisions, and Owner Gate Checklist

**Date:** 2026-11-03  
**Workstream:** Gemini 4 Argon — Phase 0 Donor Validation (`ENGINEERING/ARGON/ARGON_DEVELOPMENT_HANDOFF.md` §2.8, §5)  
**Destination Repository:** `https://github.com/iam816anonymous-commits/personal-autonomous-android-agent`  
**Validation Branch:** `phase-0-argon-handoff`  

---

## Spike 1 — ADK Kotlin + LiteRT-LM vs. Custom Kotlin Orchestration

### 1.1 Empirical Artifact Inspection (Maven Central & Google Maven, 2026-11-03)

We fetched and inspected the actual published Maven artifacts, POM dependencies, AAR manifests, and `.jar` class metadata for both `com.google.adk` and `com.google.ai.edge.litertlm`:

| Artifact Inspected | Latest Published Versions | `minSdk` / Kotlin Metadata | Transitive Dependencies & Packaged Binaries | Technical Verdict for Target Agent |
|---|---|---|---|---|
| `com.google.adk:google-adk-kotlin-core-android` (Maven Central) | `0.1.0`, `0.2.0` (`google/adk-kotlin` repo also publishes JVM `com.google.adk:google-adk:1.4.0` / tag `v1.5.0`) | `minSdkVersion="26"`, compiled with Kotlin `2.1.20` (`abi-version: 1.8.0`) | Pulls in `com.google.mlkit:genai-prompt:1.0.0-beta2` (binds `com.google.android.aicore`), `com.google.genai:google-genai:1.44.0`, `com.google.auth:google-auth-library-oauth2-http:1.20.0`, `com.google.protobuf:protobuf-javalite:3.25.5`, `org.jetbrains.kotlinx:kotlinx-coroutines-rx3:1.10.2`, `kotlinx-datetime:0.6.2`. | **REJECT for Core Agent Loop:** Designed around Gemini cloud / AICore Gemini Nano (`mlkit:genai-prompt`), lacks Android `AccessibilityService` snapshot grounding, lacks `PendingVerification` crash journals, and pulls heavy cloud/protobuf/RxJava3 dependencies. |
| `com.google.ai.edge.litertlm:litertlm-android` (Google Maven) | `0.9.0-alpha01` through `0.10.0` (2026-03-31) and `0.19.0` (2026-09-29) | `minSdkVersion="24"`, License: `Apache-2.0`; `0.10.0` compiled with Kotlin `2.2.21`, `0.19.0` with Kotlin `2.4.0` | Minimal POM (`kotlin-stdlib`, `kotlinx-coroutines-android`, `com.google.code.gson:gson:2.11.0`). Packages `jni/arm64-v8a/liblitertlm_jni.so` (`22,693,320 bytes` / `21.64 MiB`). Classes: `Engine`, `EngineConfig`, `Session`, `Conversation`, `ToolProvider`, `SamplerConfig`, `Backend`. | **DEFER to Phase 6 Optional Module (`:model-litert`):** Clean Apache-2.0 Android API with `minSdk 24` and built-in `ToolProvider` / `Backend`, **but** requires Kotlin `2.2+` compiler compatibility and physical-device benchmarking on the 2 GB Cortex-A53 `TECNO IN6`. |

### 1.2 Architectural Decision (Spike 1)
- **Decision:** Implement a **project-owned deterministic Kotlin `AgentOrchestrator`** in `:core-execution` (Phases 1–4) behind a clean `IntentPlanner` / `ModelRuntime` interface (`proposeNextAction(goal, snapshot, allowedCapabilities, workingMemory): ActionProposalResult`).
- **Rationale:** Keeps Phases 1–5 100% independent of LLM SDK churn, guarantees deterministic pre-dispatch policy/snapshot validation, and allows Phase 6 to plug in `:model-llamacpp` (baseline ARMv8.0 GGUF) or `:model-litert` (`litertlm-android`) without touching orchestration or safety code.

---

## Spike 2 — API 27 Accessibility Observation and Visual Fallback Limits

### 2.1 Android Platform API Verification (API 27 vs. API 28 vs. API 30+)

| Observation / Capture Mechanism | Minimum Android API | Behavior on Android 8.1 (`API 27` / `TECNO IN6`) | Security, Privacy & Performance Constraints |
|---|---|---|---|
| `AccessibilityService.getWindows()` + `rootInActiveWindow` (`AccessibilityNodeInfo`) | `API 21+` (`FLAG_RETRIEVE_INTERACTIVE_WINDOWS`) | **Supported** on API 27. Provides view hierarchy, `viewIdResourceName`, text, contentDescription, boundsInScreen, `isClickable`, `isEditable`, `isPassword`, `isScrollable`. | Low overhead (`<25 ms` for bounded 250-node traversal). Cannot see inside custom OpenGL/Canvas views (games, maps, non-accessible custom widgets). |
| `AccessibilityService.performGlobalAction(GLOBAL_ACTION_TAKE_SCREENSHOT)` | **`API 28+` (Android 9)** | **UNAVAILABLE on API 27** (constant added in API 28; returns `false` on API 27). | Saves screenshot to user media store with system UI flash/sound; does not return an in-memory `Bitmap` to the service. |
| `AccessibilityService.takeScreenshot(displayId, executor, callback)` | **`API 30+` (Android 11)** | **UNAVAILABLE on API 27** (`NoSuchMethodError` if called without `@RequiresApi(30)` guard). | Rate-limited by OS (typically 1 call per second); respects `FLAG_SECURE` by failing closed with `ERROR_TAKE_SCREENSHOT_SECURE_WINDOW`. |
| `MediaProjectionManager.createScreenCaptureIntent()` + `VirtualDisplay` + `ImageReader` | `API 21+` | Technically present on API 27, **but** requires a blocking system confirmation dialog per session, persistent foreground notification, and ~15–30 MiB RGBA frame buffers. | High OOM and privacy risk on a 2 GB RAM phone (`TECNO IN6`); violates least-privilege personal agent ergonomics for routine UI tasks. |

### 2.2 Architectural Decision (Spike 2)
- **Decision:**
  1. **API 27 (`Build.VERSION.SDK_INT < 30`):** Operate in **Strict Semantic Accessibility Tree Mode** (`AccessibilityNodeInfo` + `AccessibilityWindowInfo`). Do **not** use `MediaProjection` in Phases 1–6. When the foreground window has `FLAG_SECURE`, `nodes.isEmpty()`, or an unlabelled custom canvas, fail closed with `ObservationError.OpaqueOrSecureWindowOnApi27` and prompt the user for manual takeover.
  2. **API 30+ (`Build.VERSION.SDK_INT >= 30`):** Support on-demand, rate-limited `AccessibilityService.takeScreenshot()` as a secondary fallback only when semantic node grounding returns `INCONCLUSIVE` and the window is not `FLAG_SECURE` / blocklisted.

---

## Spike 3 — Local Inference Integration on Target Hardware (`TECNO IN6` / `MT6763`)

### 3.1 Verified Hardware Evidence from `tecno-os` (`d2aeac357141e75b601a935869abb8944ad0d41d`)

Inspection of `/tmp/phase0-donors/tecno-os/inventory/cpuinfo.txt`, `inventory/properties.txt`, `docs/hardware/device-overview.md`, and `docs/hardware/gpuprobe-apk-validation-2026-11-02.md` establishes hard physical constraints for the **TECNO Spark 2 (`TECNO IN6`)**:

1. **CPU ISA Constraint (No `dotprod`, No `fp16`):**
   - `inventory/cpuinfo.txt` records 8x ARM Cortex-A53 cores (`CPU implementer: 0x41`, `CPU architecture: 8`, `CPU variant: 0x0`, `CPU part: 0xd03`, `CPU revision: 4`, i.e., `r0p4` ARMv8.0-A).
   - Reported kernel feature flags: **`fp asimd evtstrm aes pmull sha1 sha2 crc32 cpuid`**.
   - **Critical Finding:** The MT6763 Cortex-A53 cores **do NOT support `asimddp` (ARMv8.2-A `SDOT`/`UDOT` int8 dot-product) or `fphp`/`asimdhp` (FP16 arithmetic)**. Any prebuilt `.so` compiled with `-march=armv8.2-a+dotprod+fp16` without runtime dispatch will immediately crash the process with **`SIGILL` (`Illegal instruction`)**.
2. **GPU Constraint (`libOpenCL.so` Blocked; Vulkan `1.0.58` Verified for Basic Compute):**
   - `docs/hardware/gpuprobe-apk-validation-2026-11-02.md` proved that on the unrooted stock Android 8.1 ROM (`O11019`), `dlopen("libOpenCL.so")` fails inside an ordinary APK due to vendor linker namespace restrictions.
   - Therefore, **`com.google.mediapipe:tasks-genai:0.10.11` (`libllm_inference_engine_jni.so` used in `mobile_agent`), which dynamically links `libOpenCL.so`, cannot use OpenCL on the TECNO IN6**.
   - Conversely, `libvulkan.so` loads cleanly in an ordinary APK (`Mali-G71`, Vulkan `1.0.58`, `maxComputeWorkGroupInvocations = 384`, `maxStorageBufferRange = 256 MiB`), and executed a 1024-element vector-add compute shader with `0` mismatches.
3. **Memory Headroom Constraint (2 GB Total RAM):**
   - `MemTotal = 1,925,380 kB` (~1.88 GiB); typical idle `MemAvailable = 1,078,348 kB` (~1.03 GiB).
   - After accounting for Android system server, foreground target app (e.g., Chrome/Settings), and the agent's own Kotlin/Compose heap, the **maximum safe native resident memory budget (`PSS`) for local inference on `TECNO IN6` is `<= 450 MiB`**.

### 3.2 Architectural Decision (Spike 3)
- **Decision:**
  1. **Reject `com.google.mediapipe:tasks-genai:0.10.11`** (`mobile_agent`) and **reject `LlamaAndroidAdapter.kt` stub** (`agent-created-by-gemini`).
  2. **Phase 4–5 Deterministic Fast-Path First:** Implement `DeterministicCommandParser` + intent/rule planner first so the 26 core benchmark tasks execute in `<50 ms` with `0 MiB` model RAM overhead.
  3. **Phase 6 Local Model Runtime PoC:** Build a real JNI/NDK `llama.cpp` adapter (`:model-llamacpp`) compiled explicitly with **`-march=armv8-a+crc+crypto`** (baseline NEON `asimd` only, no mandatory `dotprod`/`fp16`), plus optional Vulkan `1.0` compute backend gated behind a physical-device matmul stability test.
  4. **Strict Model Eligibility Gate on 2 GB RAM (`TECNO IN6`):**
     - **Tier 1 (Eligible on `TECNO IN6`):** `SmolLM2-135M-Instruct-Q4_K_M` (~105 MiB GGUF) and `Qwen2.5-0.5B-Instruct-Q4_K_M` (~398 MiB GGUF), loaded only when `ActivityManager.MemoryInfo.availMem >= 650 MiB` and `lowMemory == false`, and unloaded immediately on `onTrimMemory(TRIM_MEMORY_RUNNING_LOW)`.
     - **Tier 2 (Blocked on 2 GB RAM / Eligible on `>= 4 GB` RAM devices only):** `Llama-3.2-1B-Instruct-Q4_K_M` (~815 MiB) and `Gemma-2-2B-Instruct-Q4_K_M` (~1.62 GiB).

---

## Spike 4 — Wake-Word Feasibility Under Android / OEM (`HiOS`) Lifecycle Constraints

### 4.1 Technical Findings
1. **Why `agent-created-by-gemini`'s `WakeWordService.kt` is Unusable:**
   - `WakeWordService.kt` runs a continuous `SpeechRecognizer.startListening()` loop inside a foreground service, restarting every `onError` / `onResults` after a `500 ms` delay (`L119-145`).
   - On Android, `SpeechRecognizer` invokes the system speech service (often network-backed or heavy ASR), plays audible start/stop earcons on many ROMs, holds a high-current microphone wakelock, and fails after screen-off on Transsion **HiOS** due to aggressive `Phone Master` / DuraSpeed background process termination.
2. **Android 8.1 (API 27) vs. Android 14+ (API 34+) Microphone Service Rules:**
   - On API 27, a foreground service with a persistent notification can read `AudioRecord`, provided the user exempts the app from HiOS battery optimization (`ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS`) and HiOS Auto-Start manager.
   - On API 30–34+, `foregroundServiceType="microphone"` cannot be started from the background; the service must be promoted while the app is visible in the foreground.

### 4.2 Architectural Decision (Spike 4)
- **Decision:**
  1. **Strict Separation (Phase 5 vs. Phase 8):** Phase 5 implements **push-to-talk / user-initiated voice only** (triggered from the main app button, quick-settings tile, or floating overlay pill while visible).
  2. **Reject `SpeechRecognizer` Loops for Wake-Word:** In Phase 8, implement wake-word detection exclusively via a lightweight on-device `AudioRecord` (`16 kHz`, `CHANNEL_IN_MONO`, `ENCODING_PCM_16BIT`) ring buffer feeding an Acoustic Keyword Spotting model (`Sherpa-ONNX` KWS or `openWakeWord` TFLite/ONNX `< 5 MiB` RAM, `< 3%` single Cortex-A53 core utilization), with a persistent visible notification containing an instant **"Stop Listening" / "Mute Mic"** action and automatic pause when battery `< 20%` or thermal status degrades.

---

## Spike 5 — Encryption, Key Management, and Backup / Restore Design

### 5.1 Technical Findings
1. **`androidx.security:security-crypto` Deprecation:**
   - `mobile_agent` declared `androidx.security:security-crypto:1.1.0-alpha06` in `gradle/libs.versions.toml`. Google has officially deprecated `security-crypto` (`EncryptedSharedPreferences` and `EncryptedFile`) without a 1.1.0 stable release.
2. **Platform `AndroidKeyStore` Availability on API 27+:**
   - `java.security.KeyStore.getInstance("AndroidKeyStore")` with `KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")` and `BLOCK_MODE_GCM` / `ENCRYPTION_PADDING_NONE` (`256-bit` AES-GCM) has been part of the standard Android framework since **API 23 (Android 6.0)** and requires zero external alpha dependencies.
   - On the `TECNO IN6` (`MT6763`), ARMv8 hardware AES instructions (`aes pmull sha1 sha2` in `/proc/cpuinfo`) accelerate AES-GCM in hardware.

### 5.2 Architectural Decision (Spike 5)
- **Decision:**
  1. **Zero `security-crypto` Dependency:** Implement a small (~90-line), dependency-free `EnvelopeCryptoManager` using `AndroidKeyStore` (`AES/GCM/NoPadding`, 256-bit master key, 12-byte random IV per record, 128-bit authentication tag) to encrypt sensitive columns in Room (`TaskJournal`, `EpisodicMemory`, `SemanticPreference`).
  2. **Disable OS Auto-Backup (`android:allowBackup="false"`):** Set `android:allowBackup="false"` and `android:fullBackupContent="false"` in `AndroidManifest.xml` so Android Auto Backup / `adb backup` cannot exfiltrate SQLite databases or preferences out of band (and because `AndroidKeyStore` keys are non-exportable across devices anyway).
  3. **Explicit User-Controlled Encrypted Export/Import (Phase 7/9):** Provide an explicit user-triggered export/import flow that derives a 256-bit key from a user-supplied passphrase via `PBKDF2WithHmacSHA256` (`210,000` iterations + 16-byte random salt) and encrypts exported skills/preferences with `AES/GCM/NoPadding` plus a manifest schema version and SHA-256 integrity check.

---

## Spike 6 — Overlapping Donor Implementations: Single Owner Per Responsibility

Per `PROJECT_STATUS.md` §3 (Phase 0 exit gate item 6) and `ENGINEERING/ARGON/ARGON_DEVELOPMENT_HANDOFF.md` §2.4, the table below resolves every overlapping class across the donor repositories and designates a **single canonical owner module and implementation strategy** for Phase 1+:

| Architectural Responsibility | Overlapping Donor Implementations Inspected | Designated Canonical Owner Module (Destination Repo) | Selected Implementation Strategy |
|---|---|---|---|
| **Domain Action, Snapshot & Verification Contracts** | `agent-created-by-gemini` (`Models.kt`), `mobile_agent` (`core/Models.kt`), `agent-1` (`core/Contracts.kt`) | `:core-contracts` | **Adapt `agent-created-by-gemini/Models.kt`** into `@Serializable` Kotlin sealed interfaces (`AgentAction`, `VerificationRule`, `UiSnapshot`, `CapabilityId`, `IdempotencyClass`). Fix `elapsedRealtimeMs` and delete fake model SHA-256 hashes. |
| **Accessibility Observation & Gesture Dispatch** | `agent-created-by-gemini` (`AgentAccessibilityService.kt`, `AccessibilityNodeDiffer.kt`, `SnapshotDiffer.kt`, `SemanticTreePruner.kt`), `mobile_agent` (`accessibility/AgentAccessibilityService.kt`) | `:observation-accessibility` | Combine `agent-created-by-gemini`'s bounded traversal (`maxDepth=25`, `maxNodes=250`), password redaction, and `AccessibilityNodeDiffer` with **`mobile_agent`'s `suspendCancellableCoroutine` `GestureResultCallback` completion handler**. Reject duplicate `SnapshotDiffer.kt`. |
| **Target Grounding & Selector Resolution** | `agent-created-by-gemini` (`TargetResolver.kt`), `mobile_agent` (`Planner.kt` coordinates) | `:execution-actions` | **Adapt `agent-created-by-gemini/TargetResolver.kt`** (preserving PR #3's password block and multi-node ambiguity rejection) and remove ungrounded raw coordinate fallback. |
| **Risk Policy, Capability Gating & User Approvals** | `agent-created-by-gemini` (`CapabilityGatekeeper.kt`, `SafetyApprovalGate.kt`), `skill-agent` (`docs/05_CAPABILITY_AND_PERMISSION_MODEL.md`), `agent-1` (`ConfirmableSkill`) | `:core-policy` | **Adapt `CapabilityGatekeeper.kt` + `SafetyApprovalGate.kt`**, upgraded with cryptographic/hash approval token binding `(actionHash, targetPackage, snapshotId, expiresAtElapsedMs)` per `skill-agent` security model. |
| **Postcondition Verification** | `agent-created-by-gemini` (`StepVerifier.kt`), `mobile_agent` (`StateVerifier.kt` on unmerged branch) | `:execution-verifier` | **Clean Rewrite (`StepVerifier`)**: retain PR #3's rule evaluators (`PackageMatches`, `NodeExists`, `NodeTextContains`, `ScreenChanged`, `AlwaysPass -> INCONCLUSIVE`) and **delete lines `93-118`** where `ControlActuator`, `Delay`, `StartTimer`, `WebResearch`, and `else` returned unconditional `StepStatus.SUCCESS`. |
| **Execution Loop & Crash-Recovery Journal** | `agent-created-by-gemini` (`AgentExecutionEngine.kt`, `RetryPolicyEngine.kt`, `SelfReflectionEngine.kt`, `FailsafeRecoveryRing.kt`), `mobile_agent` (`MainViewModel.kt`), `agent-1` (`Orchestrator.kt`) | `:core-execution` | **Clean Rewrite (`AgentOrchestrator`)** backed by a write-ahead Room `TaskStepJournal` (`PendingVerification` state), adapting `RetryPolicyEngine` and `SelfReflectionEngine` heuristics while rejecting `FailsafeRecoveryRing` and `AutonomousCuriosityAgent`. |
| **Hardware Capability Discovery, Telemetry & Actuation** | `agent-created-by-gemini` (`DeviceCapabilityDetector.kt`, `HardwareTelemetryProvider.kt`, `HardwareActuatorControl.kt`, `VulkanHardwareProber.kt`), `tecno-os` (`gpuprobe` evidence) | `:hardware-capabilities` | **Adapt `DeviceCapabilityDetector.kt` + `HardwareTelemetryProvider.kt` + `VulkanHardwareProber.kt`**; **rewrite `HardwareActuatorControl.kt`** into typed `HardwareCapabilityAdapter`s with `PreMutationSnapshot` and independent callback/state postcondition verification. |
| **Memory, Task History & User-Approved Skills** | `agent-created-by-gemini` (`AgentLearningPersistence.kt`, `EpisodicMemoryStore.kt`, `SemanticMemoryDatabase.kt`, `SkillLibraryManager.kt`), `mobile_agent` (`memory/AgentDatabase.kt`), `agent-1` (`LearningEngine.kt`) | `:memory-store` | **Clean Rewrite onto Room + KSP + `AndroidKeyStore` AES-GCM (`EnvelopeCryptoManager`)**, adapting `agent-1`'s `LearningEngine` parameter suggestion concept and enforcing explicit user approval before saving any skill. |

---

## 7. Concise List of Unresolved Owner Decisions and Exact Evidence Needed (`ARGON_DEVELOPMENT_HANDOFF.md` §5.6)

The following items cannot be inferred from source code or container builds and require explicit **Owner confirmation or physical-device execution** to close the final Phase 0 gate:

| # | Decision / Evidence Item Required from Owner | Why It Cannot Be Inferred from Code | Recommended Default (If Owner Approves) | Exact Evidence Needed to Close Gate |
|---|---|---|---|---|
| **1** | **Target Physical Device Matrix & API 27 Mandate** | `tecno-os` proves the owner has a **TECNO Spark 2 (`TECNO IN6`, `MT6763`, Android 8.1.0 API 27, `arm64-v8a`, 2 GB RAM, 16 GB storage)**, while donor repos also reference modern API 34 devices. | Set `minSdk = 26` (covering API 27 `TECNO IN6` as **Tier-1 Mandatory Supported Hardware** in Semantic Accessibility Tree + Deterministic/Tier-1 135M–500M Model mode) and `targetSdk = 34` (supporting modern Android 11–14+ devices with API 30+ visual fallback). | Owner confirmation of: (a) complete list of target physical phones (model, Android version/API, RAM), and (b) whether API 27 (`TECNO IN6`) is mandatory for Release 1.0. |
| **2** | **First-Party Donor Repository License Declaration** | `agent-created-by-gemini`, `mobile_agent`, `skill-agent`, and `agent-1` are owned by `iam816anonymous-commits` (`Vivek`), but **none of the four repositories contain a root `LICENSE` file**. | Owner authorizes reuse of first-party `iam816anonymous-commits` donor code under **Apache License 2.0** (or MIT) in the destination repository and adds a root `LICENSE` file to the destination repository. | Written owner confirmation in `PROJECT_STATUS.md` / PR review (or adding `LICENSE` to donor/destination repos). |
| **3** | **Approval of the Frozen 26-Scenario Task Benchmark & Risk Policy** | `ENGINEERING/ARGON/INITIAL_TASK_BENCHMARK.md` defines 26 deterministic scenarios and the 14 fail-closed architectural contracts (`BM-01` to `BM-26`). | Freeze `INITIAL_TASK_BENCHMARK.md` (26 scenarios) and the 5-tier risk policy in `ARCHITECTURE/SAFETY_AND_APPROVALS.md` as the Phase 1–9 acceptance baseline. | Owner sign-off on `ENGINEERING/ARGON/INITIAL_TASK_BENCHMARK.md`. |
| **4** | **Authorization to Begin Phase 1 Application Code Implementation** | `PROJECT_STATUS.md` and `ENGINEERING/ARGON/ARGON_DEVELOPMENT_HANDOFF.md` §1 & §7 block Phase 1 Gradle/Kotlin creation until Phase 0 deliverables are reviewed and accepted. | Once items 1–3 above are confirmed, authorize **Phase 1 only** (minimal buildable multi-module Kotlin shell, canonical `:core-contracts`, `:core-policy`, Room `TaskStepJournal`, redacted logger, global stop control, and deterministic CI). | Explicit owner instruction to proceed from Phase 0 to Phase 1 after reviewing this branch (`phase-0-argon-handoff`). |
