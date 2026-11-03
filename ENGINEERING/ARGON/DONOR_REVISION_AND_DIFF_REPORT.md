# Phase 0 Donor Revision and Branch Diff Report

**Date:** 2026-11-03  
**Workstream:** Gemini 4 Argon — Phase 0 Donor Validation (`ENGINEERING/ARGON_DEVELOPMENT_HANDOFF.md` §2.6, §3, §5)  
**Destination Repository:** `https://github.com/iam816anonymous-commits/personal-autonomous-android-agent`  
**Validation Branch:** `phase-0-argon-handoff`  

---

## 1. Pinned Commit vs. Current Remote Branch Head Matrix

All five donor repositories were cloned from GitHub and inspected with `git ls-remote`, `git branch -a`, and `git rev-parse` on 2026-11-03:

| Donor Repository | Target Branch | Recorded Pin in Handoff (`ARGON_DEVELOPMENT_HANDOFF.md` §3) | Current Remote Branch HEAD (`git ls-remote`) | Drift Status | Additional Unmerged Remote Branches Found |
|---|---|---|---|---|---|
| **[agent-created-by-gemini](https://github.com/iam816anonymous-commits/agent-created-by-gemini)** | `main` | `90f5cf3ca0eca324f91a43ba96fab72c2e2de5ed` | `90f5cf3ca0eca324f91a43ba96fab72c2e2de5ed` | **MATCH (0 commits drift)** | `origin/jules-6343604263163356623-b163ffdc` (`2f52cf881e7f980f0be73982158db253057390a3`), `origin/roadmap/autonomy-first-working-baseline` (`c2e13128031854c24d3363b8162fd772e963d7f2`), `origin/research/verified-execution-safety` (`1c7d8aa8a1b5b9cf27219705ef3ddfa4260e7eb4` — merged via `6514b4f`) |
| **[mobile_agent](https://github.com/iam816anonymous-commits/mobile_agent)** | `android-local-agent-os-base-65071498209142568` | `863723f7aa4b42137ff4ce07e26e541e402ff5a8` | `863723f7aa4b42137ff4ce07e26e541e402ff5a8` | **MATCH (0 commits drift)** | `origin/feature/local-automation-agent-7534575414768528770` (`334c21553cb6316f199fc6b8681f47c07b9249ef`), `origin/production-readiness-improvements-11132810492529575630` (`2744a627796581fc6f7107257825f3fc4ef1bc82`), `origin/production-ready-improvements-2412666002363103917` (`9b2f5bc48a10f679fc3563a8443d3e530fdba88f`) |
| **[skill-agent](https://github.com/iam816anonymous-commits/skill-agent)** | `phase-0-specifications-14452037546145599308` | `86cfa6960b1341ed53b987f5a50b1c623ec18ad4` | `86cfa6960b1341ed53b987f5a50b1c623ec18ad4` | **MATCH (0 commits drift)** | None |
| **[agent-1](https://github.com/iam816anonymous-commits/agent-1)** | `jules-10149456198484707000-5b5421da` | `6d0c3002fc4c716dea2ffcba31339f0de79fd087` | `6d0c3002fc4c716dea2ffcba31339f0de79fd087` | **MATCH (0 commits drift)** | None |
| **[tecno-os](https://github.com/iam816anonymous-commits/tecno-os)** | `in6-linux-bringup-infrastructure-4378631429758063630` | `c8a23e359de5b30297ca731f713b89077e056cfc` (updated in `RESEARCH/SOURCE_BY_SOURCE_REUSE_SPECIFICATION.md` commit `de6f651` to `d2aeac357141e75b601a935869abb8944ad0d41d`) | `d2aeac357141e75b601a935869abb8944ad0d41d` | **MOVED +4 COMMITS** beyond `c8a23e3` | `origin/jules-17422722695072446061-3a2a3761` (`a67b36b89197fb7bb032df2bf35f1a97a63e6cb2`, ancestor at -5 commits) |

---

## 2. Detailed Diff Analysis: `tecno-os` (`c8a23e359de5b30297ca731f713b89077e056cfc..d2aeac357141e75b601a935869abb8944ad0d41d`)

### 2.1 Commit Log (`c8a23e3..d2aeac3`)
```text
d2aeac357141e75b601a935869abb8944ad0d41d docs: add GPUProbe APK validation report and reconcile hardware status
dd462c1c7640b2bc3be1f43a512bf7e11f217b42 docs: clarify provisional nature of repeated GPU dispatch in validation report
5c372f92c8856fb3e89c4057b7f5a3f1a4cc9a04 docs: add GPUProbe APK validation report for TECNO IN6
47313d653599c8e213fd9cf8d9279619b0ba06cf Merge pull request #1 from iam816anonymous-commits/jules-17422722695072446061-3a2a3761
```

### 2.2 Changed Files (`git diff --stat c8a23e3..d2aeac3`)
- `README.md` (`+8 / -4` lines)
- `STATUS.md` (`+12 / -5` lines)
- `docs/hardware/gpu-compute-feasibility.md` (`+18 / -6` lines)
- `docs/hardware/gpuprobe-apk-validation-2026-11-02.md` (`+94` lines, new file)

### 2.3 Key Technical Evidence Added in `docs/hardware/gpuprobe-apk-validation-2026-11-02.md`
1. **Ordinary Unprivileged APK Validation (`com.example.gpuprobe`):** Executed on the physical **TECNO Spark 2 (`TECNO IN6`, `MT6763`, Android 8.1.0 API 27, build `O11019`)** without root.
2. **Vulkan Compute Verified:**
   - Loaded `libvulkan.so` and initialized `Mali-G71` (`vendorID=0x13b5`, `deviceID=0x70930000`, `apiVersion=1.0.58`, `driverVersion=0x3400000`).
   - Queue family `0`: `queueFlags=0x7` (`GRAPHICS | COMPUTE | TRANSFER`), `queueCount=2`.
   - Limits: `maxComputeSharedMemorySize = 32768` (32 KiB), `maxComputeWorkGroupInvocations = 384`, `maxComputeWorkGroupSize = [384, 384, 64]`, `maxStorageBufferRange = 268435455` (~256 MiB).
   - 1024-element integer vector-add compute shader (`A[i] + B[i] -> C[i]`) executed on GPU with `mismatches=0` (`C[0]=1000`, `C[1]=1002`, `C[1023]=3046`).
3. **OpenCL Blocked in App Linker Namespace:**
   - Both `/vendor/lib64/libOpenCL.so` and `/system/vendor/lib64/libOpenCL.so` failed `dlopen()` inside the ordinary APK due to Android 8.0+ vendor linker namespace isolation.
   - **Direct Impact on Destination Agent:** Any local inference library that links against `libOpenCL.so` from an ordinary APK (such as `com.google.mediapipe:tasks-genai:0.10.11` used in `mobile_agent`) **cannot use OpenCL on the TECNO IN6**.
4. **Provisional Repeated-Dispatch Caveat:**
   - The benchmark loop in `gpuprobe` submitted a command buffer recorded with `VK_COMMAND_BUFFER_USAGE_ONE_TIME_SUBMIT_BIT` 50 times without re-recording (`totalMs=50 ms`, `avgMs=1.000 ms`). Therefore, single-dispatch Vulkan compute is proven, while sustained multi-dispatch throughput requires re-testing without `ONE_TIME_SUBMIT_BIT` reuse.

---

## 3. Detailed Commit & Branch Diff Analysis: `agent-created-by-gemini`

### 3.1 Commit History on `main` (`90f5cf3ca0eca324f91a43ba96fab72c2e2de5ed`)
```text
90f5cf3ca0eca324f91a43ba96fab72c2e2de5ed feat(hardware): implement Google ML Kit perception integration
6514b4f903bf4efdb487cc7758101868c6556f32 Merge pull request #3 from iam816anonymous-commits/research/verified-execution-safety
  └─ 1c7d8aa8a1b5b9cf27219705ef3ddfa4260e7eb4 Implement verified execution, safety redaction, and honest telemetry
e60142a4e8c92a4d09b86c576e5a7f613ff0d809 Merge pull request #2 from iam816anonymous-commits/jules-16779964026275647486-f28d769b
```
- **Commit `90f5cf3ca0eca324f91a43ba96fab72c2e2de5ed`:** Touched **only** web/TypeScript prototype files (`package.json`, `package-lock.json`, `src/App.tsx`, `src/agent/AgentRuntime.ts`, `src/agent/ExecutionEngine.ts`, `src/hardware/MLKitPerceptionBridge.ts`, `src/hardware/PerceptionTestHarness.ts`). Zero Android/Kotlin changes.
- **Commit `1c7d8aa8a1b5b9cf27219705ef3ddfa4260e7eb4` (PR #3):** Modified 13 Kotlin source/test files (`AgentAccessibilityService.kt`, `AgentExecutionEngine.kt`, `AgentLearningPersistence.kt`, `DeviceCapabilityDetector.kt`, `ExecutionTracer.kt`, `LocalSlmIntentEngine.kt`, `MainActivity.kt`, `Models.kt`, `SafetyApprovalGate.kt`, `StepVerifier.kt`, `TargetResolver.kt`, `StepVerifierTest.kt`, and added `TargetResolverSafetyTest.kt`). As documented in `DONOR_SOURCE_PROVENANCE_AND_LICENSE_LEDGER.md` §2.2, PR #3 fixed `VerificationRule.AlwaysPass` (`-> INCONCLUSIVE`) and `TargetResolver` password/ambiguity checks, but left hardcoded `StepStatus.SUCCESS` branches in `StepVerifier.verify()` (`L93-118`) and wall-clock `monotonicTimestampMs` in `Models.kt` (`L366`).

### 3.2 Unmerged Remote Branches in `agent-created-by-gemini`
1. **`origin/jules-6343604263163356623-b163ffdc` (`2f52cf881e7f980f0be73982158db253057390a3`):**
   - Branched from `e60142a` (before PR #3). Adds a single markdown file `docs/AUTONOMOUS_AGENT_CRITICAL_AUDIT_AND_ROADMAP.md` (349 lines). Contains no code changes.
2. **`origin/roadmap/autonomy-first-working-baseline` (`c2e13128031854c24d3363b8162fd772e963d7f2`):**
   - Branched from `e60142a`. Adds `ROADMAP_AUTONOMY_FIRST_WORKING_AGENT.md` (599 lines). Contains no code changes.

---

## 4. Detailed Unmerged Branch Diff Analysis: `mobile_agent`

While the pinned branch `android-local-agent-os-base-65071498209142568` remains at `863723f7aa4b42137ff4ce07e26e541e402ff5a8`, `mobile_agent` contains **three unmerged remote branches** that diverge from `863723f`. All three were inspected via `git log` and `git diff --stat`:

| Unmerged Branch | Tip Commit SHA | Relationship to Pinned `863723f` | Files Changed vs `863723f` | Engineering Evaluation & Reuse Decision |
|---|---|---|---|---|
| `origin/feature/local-automation-agent-7534575414768528770` | `334c21553cb6316f199fc6b8681f47c07b9249ef` | `+1` commit ahead of `863723f` ("Upgrade to full autonomous local Android AI Agent") | 18 files (`+1,093 / -163` lines): adds `DeviceController.kt`, `FileSystemTool.kt`, `IntentTool.kt`, `NotificationListener.kt`, `ScreenCaptureService.kt`, `VoiceService.kt`, `AgentForegroundService.kt`, `SafetyManager.kt`, `SkillManager.kt` | **DO NOT ADOPT BLINDLY (REFERENCE ONLY):** Although it adds a `SafetyManager.kt` regex filter for shell commands and switches `MemoryManager` to a manual `SQLiteOpenHelper` (avoiding the missing Room KSP crash), it retains `ShellTool.kt` (`Runtime.getRuntime().exec`) and adds `ScreenCaptureService` (`MediaProjection` on API 29+ without API 27 guards). Keep pin at `863723f` and do not transplant. |
| `origin/production-readiness-improvements-11132810492529575630` | `2744a627796581fc6f7107257825f3fc4ef1bc82` | `+2` commits ahead of `863723f` | 15 files (`+667 / -141` lines): adds `AppLogger.kt`, `SecurityManager.kt` (using deprecated `EncryptedSharedPreferences`), `WorkManager`, and enables KAPT for `room-compiler` in `memory/build.gradle.kts` | **REFERENCE ONLY:** Confirms our finding that `863723f` was missing Room annotation processing in `:memory`, and illustrates why `EncryptedSharedPreferences` (`security-crypto:1.1.0-alpha06`) was attempted. Keep pin at `863723f`. |
| `origin/production-ready-improvements-2412666002363103917` | `9b2f5bc48a10f679fc3563a8443d3e530fdba88f` | `+1` commit ahead of `863723f` | 14 files (`+623 / -179` lines): replaces `ShellTool` with `SystemInfoTool`, adds `StateVerifier.kt`, adds KAPT for `room-compiler` in `:memory` | **REFERENCE ONLY (`StateVerifier.kt` concept):** Confirms `ShellTool` is unsafe and adds a basic `StateVerifier` concept, which is already superseded by our stricter Phase 3 postcondition verifier design. Keep pin at `863723f`. |

---

## 5. Summary of Pin Recommendations After Phase 0 Revalidation

1. **`agent-created-by-gemini`:** Keep pinned at `90f5cf3ca0eca324f91a43ba96fab72c2e2de5ed` (`main`). Note that Android/Kotlin code is identical to `6514b4f903bf4efdb487cc7758101868c6556f32` (PR #3).
2. **`mobile_agent`:** Keep pinned at `863723f7aa4b42137ff4ce07e26e541e402ff5a8` (`android-local-agent-os-base-65071498209142568`).
3. **`skill-agent`:** Keep pinned at `86cfa6960b1341ed53b987f5a50b1c623ec18ad4` (`phase-0-specifications-14452037546145599308`).
4. **`agent-1`:** Keep pinned at `6d0c3002fc4c716dea2ffcba31339f0de79fd087` (`jules-10149456198484707000-5b5421da`), with `:app` classified as non-buildable (`REJECT`) and `:core` as `REFERENCE` / `ADAPT` concept only.
5. **`tecno-os`:** Reconciled from `c8a23e359de5b30297ca731f713b89077e056cfc` to `d2aeac357141e75b601a935869abb8944ad0d41d` (`in6-linux-bringup-infrastructure-4378631429758063630`), incorporating the physical-device Vulkan/OpenCL findings from `docs/hardware/gpuprobe-apk-validation-2026-11-02.md`.
