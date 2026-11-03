# Architecture decision log

## ADR-000 — Research-first modular Android-native architecture
**Date:** 2026-11-02 · **Status:** Proposed

Kotlin/Android is the intended product runtime. Keep orchestration, inference, observation, actuation, memory and skills behind replaceable interfaces. Host-side Python remains useful for research/evaluation. No Docker dependency.

## ADR-001 — Personal sideloaded deployment is primary
**Date:** 2026-11-02 · **Status:** Accepted

The owner has confirmed the app is mainly for personal use and is not intended for Play Store publication. Prioritize capabilities useful in a directly installed app. Investigate AccessibilityService as a core UI observation/action adapter. Do not artificially narrow the design around Play policy.

**Constraints that remain:** Android permissions, background execution, microphone/foreground-service lifecycle, app sandboxing, secure windows, OEM restrictions, privacy and user consent. Sideloading does not grant root or unrestricted system privileges.

## ADR-002 — User-initiated speech before wake-word
**Date:** 2026-11-02 · **Status:** Proposed

Build and validate tap-to-talk speech-to-text before always-on wake-word. Microphone lifecycle, battery, privacy and device compatibility must be proven before wake-word work.

## ADR-003 — Verify every atomic action
**Date:** 2026-11-02 · **Status:** Proposed

Every action is schema-validated, permission-checked, bounded, executed, re-observed and verified against a postcondition. API return alone is not proof of task success.

## ADR-004 — Play policy is reference-only
**Date:** 2026-11-02 · **Status:** Accepted

Google Play policy is documented in case distribution goals change, but it is not a current product acceptance criterion. Do not misrepresent accessibility-tool status or bypass platform security protections.

## ADR-005 — Home security is a separate Android application
**Date:** 2026-11-02 · **Status:** Accepted

Home-security monitoring (camera uptime, face detection/recognition, event snapshots, notifications and remote access) is excluded from the autonomous agent's scope, roadmap, modules, permissions, dependencies and release criteria. It will be developed later as a separately packaged Android app that may be installed on the same physical phone. Keep the existing home-security document only as standalone future-project research. Do not integrate its runtime, storage, services or tools into this agent.

## ADR-006 — Custom deterministic Kotlin orchestrator for Phases 1–5; isolate LiteRT-LM / llama.cpp to Phase 6
**Date:** 2026-11-03 · **Status:** Proposed (Phase 0 Spike 1)

Artifact inspection of `com.google.adk:google-adk-kotlin-core-android:0.2.0` shows it pulls in `com.google.mlkit:genai-prompt:1.0.0-beta2` (requiring AICore, absent on API 27 / `TECNO IN6`), `com.google.genai:google-genai:1.44.0`, `protobuf-javalite`, and `kotlinx-coroutines-rx3`, without Android `AccessibilityService` snapshot grounding or write-ahead `PendingVerification` journals. `com.google.ai.edge.litertlm:litertlm-android` (`0.10.0` / `0.19.0`) is Apache-2.0 with `minSdk 24`, but bundles a 21.64 MiB native `.so` and requires Kotlin `2.2+` metadata. Phases 1–5 will use a lightweight project-owned deterministic Kotlin `AgentOrchestrator` behind a `ModelRuntime` interface; `litertlm-android` and `llama.cpp` remain isolated to optional Phase 6 modules (`:model-litert`, `:model-llamacpp`). See `ENGINEERING/ARGON/PHASE_0_TECHNICAL_SPIKES.md` §1.

## ADR-007 — Strict semantic Accessibility tree mode on API 27; visual screenshot fallback gated to API 30+
**Date:** 2026-11-03 · **Status:** Proposed (Phase 0 Spike 2)

`AccessibilityService.takeScreenshot()` requires API 30 and `GLOBAL_ACTION_TAKE_SCREENSHOT` requires API 28, while `MediaProjection` on API 27 incurs per-session confirmation dialogs and heavy frame-buffer overhead on 2 GB RAM devices. On API 27–29, observation operates strictly on bounded `AccessibilityNodeInfo` / `AccessibilityWindowInfo` trees and fails closed (`OpaqueOrSecureWindowOnApi27`) on unlabelled custom canvases or `FLAG_SECURE` windows. See `ENGINEERING/ARGON/PHASE_0_TECHNICAL_SPIKES.md` §2.

## ADR-008 — Baseline ARMv8.0-A CPU + optional Vulkan 1.0 local inference on TECNO IN6; reject OpenCL-dependent `tasks-genai`
**Date:** 2026-11-03 · **Status:** Proposed (Phase 0 Spike 3)

Physical hardware inventory from `tecno-os` (`inventory/cpuinfo.txt` and `docs/hardware/gpuprobe-apk-validation-2026-11-02.md`) proves that the `TECNO IN6` (`MT6763`) has 8x ARM Cortex-A53 (`r0p4`, ARMv8.0-A) cores **without `asimddp` (`dotprod`) or `fphp`/`asimdhp` (`fp16`)**, and that `libOpenCL.so` fails `dlopen()` inside third-party APKs. Consequently, `com.google.mediapipe:tasks-genai:0.10.11` (`libllm_inference_engine_jni.so` used in `mobile_agent`) is rejected for `TECNO IN6`. Local inference binaries in Phase 6 must be compiled for baseline `-march=armv8-a+crc+crypto` (with optional Mali-G71 Vulkan `1.0.58` compute) and capped at `<= 450 MiB` resident PSS (`SmolLM2-135M` / `Qwen2.5-0.5B` Q4) on 2 GB RAM devices. See `ENGINEERING/ARGON/PHASE_0_TECHNICAL_SPIKES.md` §3.

## ADR-009 — Reject `SpeechRecognizer` restart loops for wake-word; use `AudioRecord` KWS in Phase 8
**Date:** 2026-11-03 · **Status:** Proposed (Phase 0 Spike 4)

Continuous `SpeechRecognizer` restart loops (`agent-created-by-gemini`'s `WakeWordService.kt`) cause battery drain, audio earcons, and OEM background termination on HiOS. Phase 5 is strictly user-initiated push-to-talk; Phase 8 wake-word will use a dedicated 16 kHz mono PCM `AudioRecord` keyword-spotting engine (`Sherpa-ONNX` / `openWakeWord`) inside a user-toggled `foregroundServiceType="microphone"` service with an instant mute/stop action. See `ENGINEERING/ARGON/PHASE_0_TECHNICAL_SPIKES.md` §4.

## ADR-010 — Platform `AndroidKeyStore` AES-256-GCM encryption + `allowBackup="false"`; reject deprecated `androidx.security:security-crypto`
**Date:** 2026-11-03 · **Status:** Proposed (Phase 0 Spike 5)

`androidx.security:security-crypto` (`1.1.0-alpha06`) is deprecated by Google. Sensitive Room columns (`TaskJournal`, `EpisodicMemory`, `SemanticPreference`) will be encrypted via a dependency-free `EnvelopeCryptoManager` backed by `AndroidKeyStore` (`AES/GCM/NoPadding`, API 23+), with `android:allowBackup="false"` in `AndroidManifest.xml` and explicit passphrase-encrypted (`PBKDF2WithHmacSHA256` + `AES-256-GCM`) user export/import in Phase 7/9. See `ENGINEERING/ARGON/PHASE_0_TECHNICAL_SPIKES.md` §5.

