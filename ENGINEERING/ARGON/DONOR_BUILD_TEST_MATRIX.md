# Phase 0 Donor Clean-Checkout Build, Test, Lint, and CI Matrix

**Date:** 2026-11-03  
**Workstream:** Gemini 4 Argon — Phase 0 Donor Validation (`ENGINEERING/ARGON/ARGON_DEVELOPMENT_HANDOFF.md` §2, §4, §5)  
**Destination Repository:** `https://github.com/iam816anonymous-commits/personal-autonomous-android-agent`  
**Validation Branch:** `phase-0-argon-handoff`  

> **Verification Rule:** Every status in this matrix reflects an actual command executed against a fresh, isolated git clone at the exact pinned commit in `/tmp/phase0-donors/`. No result is inferred from prior summaries, documentation, or unverified GitHub Actions badges. Blocked, skipped, or non-existent tests are explicitly classified as **FAIL**, **BLOCKED**, or **NOT RUN** — never **PASS**.

---

## 1. Execution Environment Metadata

All clean-checkout donor builds, unit tests, lint checks, and dependency inspections were executed in the following isolated environment:

| Property | Recorded Value | Verification Command |
|---|---|---|
| **Operating System** | `Linux localhost 4.19.0 #1 SMP Mon Jan 12 12:00:00 MSK 2015 x86_64 GNU/Linux` (Debian GNU/Linux 12 Bookworm container) | `uname -a && cat /etc/os-release` |
| **JDK / JVM** | `openjdk version "21.0.13" 2026-10-20 LTS` (`OpenJDK Runtime Environment Temurin-21.0.13+9 (build 21.0.13+9-LTS)`, 64-Bit Server VM) | `java -version` |
| **`JAVA_HOME`** | `/usr/lib/jvm/temurin-21-jdk-amd64` | `echo $JAVA_HOME` |
| **System Gradle** | `Gradle 9.3.1` (donor builds executed via each repository's pinned `./gradlew` wrapper where present) | `gradle --version` |
| **Android SDK Root** | `ANDROID_SDK_ROOT=/opt/android/sdk` (`ANDROID_HOME=/opt/android/sdk`) | `env \| grep ANDROID` |
| **Installed SDK Platforms** | `android-34`, `android-36`, `android-36.1` (`/opt/android/sdk/platforms`) | `ls /opt/android/sdk/platforms` |
| **Installed Build-Tools** | `34.0.0`, `36.0.0` (`/opt/android/sdk/build-tools`) | `ls /opt/android/sdk/build-tools` |
| **Available Disk Space** | `756G available` on `/` (`983G total`, `228G used`, `24%` utilization) | `df -h` |
| **Physical Android Device / ADB** | **NONE** (`adb` / hardware device not attached in cloud build container; physical-device validation is classified as **NOT RUN**) | `which adb` |
| **Clean Checkout Root** | `/tmp/phase0-donors/{agent-created-by-gemini,mobile_agent,skill-agent,agent-1,tecno-os}` | `git rev-parse HEAD` |
| **Raw Artifact Logs** | `/tmp/phase0-logs/*.log` | Preserved per command |

---

## 2. Executive Summary Matrix Across All 5 Donor Repositories

| Donor Repository | Pinned Commit SHA | Wrapper / Toolchain | Wrapper & Projects | Unit Tests | Android Lint (`lintDebug`) | Debug APK (`assembleDebug`) | Release APK (`assembleRelease`) | Dependency Graph | GitHub Actions CI Audit |
|---|---|---|---|---|---|---|---|---|---|
| **[agent-created-by-gemini](https://github.com/iam816anonymous-commits/agent-created-by-gemini)** | `90f5cf3ca0eca324f91a43ba96fab72c2e2de5ed` (`main`) | Gradle `8.10.2`, AGP `8.7.2`, Kotlin `2.0.21`, `compileSdk 36`, `minSdk 24`, `targetSdk 34` | **PASS** (`:app`) | **PASS** (13 suites, **74 tests**, 0 failed, 0 skipped) | **FAIL** (exit `1`: **1 error, 86 warnings** — `PermissionImpliesUnsupportedChromeOsHardware`) | **PASS** (`app-debug.apk`, 18,858,256 B) | **FAIL** (exit `1`: R8 fails at `app/proguard-rules.pro:14:29`) | **PASS** (`:app:dependencies` captured) | Run `40505149792` shows green only because `lintDebug` is omitted and `assembleRelease` swallows failure via `continue-on-error: true` + `\|\| true` |
| **[mobile_agent](https://github.com/iam816anonymous-commits/mobile_agent)** | `863723f7aa4b42137ff4ce07e26e541e402ff5a8` (`android-local-agent-os-base-65071498209142568`) | Gradle `8.5`, AGP `8.2.2`, Kotlin `1.9.22`, `compileSdk 34`, `minSdk 26`, `targetSdk 34` | **PASS** (6 modules: `:app`, `:accessibility`, `:ai`, `:core`, `:memory`, `:tools`) | **PASS (1 test only)** (`:ai` `PlannerTest` = 1 test; 5 of 6 modules have **0 tests** `NO-SOURCE`) | **FAIL** (exit `1` on `:app:lintDebug`: **1 error, 1 warning** — `QueryAllPackagesPermission`; 5 library modules PASS) | **PASS** (`app-debug.apk`, 17,846,968 B) | **PASS (Unsigned / Unminified)** (`app-release-unsigned.apk`, 14,339,843 B; `isMinifyEnabled=false`) | **PASS** (all 6 modules captured; `:memory` missing KSP/KAPT for Room) | No `.github/workflows` directory exists; 0 GitHub Actions runs |
| **[agent-1](https://github.com/iam816anonymous-commits/agent-1)** | `6d0c3002fc4c716dea2ffcba31339f0de79fd087` (`jules-10149456198484707000-5b5421da`) | Gradle `8.8`, AGP `8.2.2`, Kotlin `1.9.22`, `compileSdk 34`, `minSdk 26`, `targetSdk 34` | **PASS** (3 modules: `:app`, `:core`, `:infrastructure`) | **PARTIAL PASS / FAIL**: `:core:test` (5 tests) & `:infrastructure:test` (6 tests) **PASS**; `:app:testDebugUnitTest` **FAIL** (exit `1`) | **FAIL / BLOCKED** (fails during `:app` resource/AndroidX pre-flight) | **FAIL** (exit `1`: 3 stacked defects — missing `android.useAndroidX=true`, 512m heap thrash, missing `app/src/main/res/`) | **FAIL** (same 3 stacked `:app` defects) | **PASS** (`:app`, `:core`, `:infrastructure` dependency trees captured) | Run `37294356933` **FAILED** in Step 9 (`./gradlew build`) at `:app:mapDebugSourceSetPaths` due to missing `gradle.properties` (`android.useAndroidX=true`) |
| **[skill-agent](https://github.com/iam816anonymous-commits/skill-agent)** | `86cfa6960b1341ed53b987f5a50b1c623ec18ad4` (`phase-0-specifications-14452037546145599308`) | Specification-only markdown (`README.md`, `AGENTS.md`, `docs/`) | **NOT RUN** (no Gradle project) | **NOT RUN** (no source/test files) | **NOT RUN** (no code) | **NOT RUN** (no Android app) | **NOT RUN** (no Android app) | **NOT RUN** (no dependencies) | No `.github/workflows`; specification-only repository |
| **[tecno-os](https://github.com/iam816anonymous-commits/tecno-os)** | Recorded pin: `c8a23e359de5b30297ca731f713b89077e056cfc` / Branch head: `d2aeac357141e75b601a935869abb8944ad0d41d` | Python 3.11 `unittest` host harness + shell scripts (`tests/host/`) | **NOT RUN** (not a Gradle project) | **PARTIAL (4 PASS / 5 BLOCKED)** out of 9 `tests/host/test_*.py` suites | **NOT RUN** (not an Android app repo) | **NOT RUN** (`gpuprobe` is pre-recorded markdown/device evidence, not built in repo) | **NOT RUN** | **NOT RUN** | Hardware evidence repository only |

---

## 3. Detailed Per-Repository Clean-Build & Test Evidence

### 3.1 `agent-created-by-gemini`

- **Repository URL:** `https://github.com/iam816anonymous-commits/agent-created-by-gemini.git`
- **Branch / Pinned Commit:** `main` @ `90f5cf3ca0eca324f91a43ba96fab72c2e2de5ed` (`git rev-parse HEAD` matches recorded pin; remote `origin/main` has not moved).
- **Build System:** Gradle Wrapper `8.10.2`, AGP `8.7.2`, Kotlin `2.0.21`, KSP `2.0.21-1.0.27`, `compileSdk = 36`, `minSdk = 24`, `targetSdk = 34`, `jvmTarget = "11"`. (Also contains a root Vite/React/TypeScript prototype in `package.json` and `src/`).

#### Command-by-Command Execution Table (`agent-created-by-gemini`)

| Step | Exact Command Executed | Exit Code | Classification | Log Reference | Summary of Actual Output |
|---|---|---|---|---|---|
| 1. Clone & Verify Pin | `git clone https://github.com/iam816anonymous-commits/agent-created-by-gemini.git && git checkout 90f5cf3ca0eca324f91a43ba96fab72c2e2de5ed` | `0` | **PASS** | `/tmp/phase0-logs/acbg-version-projects.log` | `HEAD is now at 90f5cf3 feat(hardware): implement Google ML Kit perception integration` |
| 2. Wrapper & Projects | `./gradlew --version && ./gradlew projects` | `0` | **PASS** | `/tmp/phase0-logs/acbg-version-projects.log` | `Gradle 8.10.2`, Kotlin `2.0.20` (embedded), JVM `21.0.13 (Eclipse Adoptium 21.0.13+9-LTS)`. Root project `My Application`, single subproject `:app`. Note: `app/build.gradle.kts` prints `Warning: .env file not found, using system environment variables.` when `.env` is absent. |
| 3. Unit Tests (Clean, no `.env`) | `./gradlew :app:testDebugUnitTest --stacktrace` | `0` | **PASS** | `/tmp/phase0-logs/acbg-testDebugUnitTest.log` | `BUILD SUCCESSFUL in 1m 25s`, `33 actionable tasks: 33 executed`. Executed **13 test classes, 74 unit tests, 0 failures, 0 errors, 0 skipped**. |
| 4. Unit Tests (With `.env` from `.env.example`) | `cp .env.example .env && ./gradlew :app:testDebugUnitTest --rerun-tasks --stacktrace` | `0` | **PASS** | `/tmp/phase0-logs/acbg-ci-exact-verification.log` | `BUILD SUCCESSFUL in 27s`. All **74 unit tests** re-executed cleanly without the `.env` missing warning. |
| 5. Android Lint | `./gradlew :app:lintDebug --stacktrace` | `1` | **FAIL** | `/tmp/phase0-logs/acbg-lintDebug.log` | `> Task :app:lintDebug FAILED`. `Lint found 1 errors, 86 warnings`. First error: `app/src/main/AndroidManifest.xml:20: Error: Permission exists without corresponding hardware <uses-feature android:name="android.hardware.camera" required="false"> tag [PermissionImpliesUnsupportedChromeOsHardware]`. |
| 6. Debug APK Build | `./gradlew :app:assembleDebug --stacktrace` | `0` | **PASS** | `/tmp/phase0-logs/acbg-assembleDebug.log` | `BUILD SUCCESSFUL in 11s`, `38 actionable tasks: 12 executed, 26 up-to-date`. Output APK: `app/build/outputs/apk/debug/app-debug.apk` (**18,858,256 bytes**, SHA-256: `3dd0fa768cd4bb6c52619a66c7e6e9d317bc4cc78776802853ec9cff5104b11b`). |
| 7. Release APK Build | `./gradlew :app:assembleRelease --stacktrace` | `1` | **FAIL** | `/tmp/phase0-logs/acbg-assembleRelease.log` | `> Task :app:minifyReleaseWithR8 FAILED`. R8 throws `com.android.tools.r8.CompilationFailedException: Compilation failed to complete, position: offset: 491, line: 14, column: 29, origin: /tmp/phase0-donors/agent-created-by-gemini/app/proguard-rules.pro` (`Expected field or method name at @com.squareup.moshi.Json(name=*) <fields>;`). |
| 8. Dependency Tree | `./gradlew :app:dependencies` | `0` | **PASS** | `/tmp/phase0-logs/acbg-dependencies.log` | Full compile/runtime/test dependency graph exported (`688 KB` log). |

#### Unit Test Suite Breakdown (`agent-created-by-gemini` — `app/build/test-results/testDebugUnitTest/*.xml`)

| Test Suite (`com.example.myapplication.agent.*`) | Tests Run | Passed | Failed | Skipped | Duration (s) | What It Actually Tests |
|---|---|---|---|---|---|---|
| `ActuatorAndTelemetryTest` | 9 | 9 | 0 | 0 | `0.159` | Mocked `HardwareActuatorControl` (`toggleWifi`, `toggleBluetooth`, `toggleFlashlight`, `setScreenBrightness`, `setVolume`) and `HardwareTelemetryProvider` (`BatteryManager`, `ActivityManager`, `StatFs` storage). |
| `AdvancedRepoMethodsTest` | 5 | 5 | 0 | 0 | `0.068` | `SkillLibraryManager` (`generateSkillFromTrajectory`, `exportSkillsJson`/`importSkillsJson`), `EpisodicMemoryStore.pruneLowUtilityMemories`, `SemanticMemoryDatabase.consolidateFacts`, `WorkingMemoryManager.compressHistory`. |
| `AgentCoreHardeningTest` | 9 | 9 | 0 | 0 | `0.214` | `CapabilityGatekeeper` risk classification, `ExecutionTracer` trace log/audit export, `SystemReadinessValidator`, `AccessibilityNodeDiffer` loop detection, `OvernightConsolidationEngine`. |
| `AutonomousCuriosityAgentTest` | 4 | 4 | 0 | 0 | `2.078` | `AutonomousCuriosityAgent` mock exploration session, loop stop, exploration metrics, and `SkillGraphEngine` invocation. |
| `CommandParserAndApprovalTest` | 6 | 6 | 0 | 0 | `0.072` | `DeterministicCommandParser` intent matching (`ToggleFlashlight`, `OpenApp`, `Scroll`, `NavigateHome`) and `SafetyApprovalGate` approval request/resolution. |
| `EvaluatorAndToolTest` | 3 | 3 | 0 | 0 | `0.036` | `OfflineBenchmarkHarness` simulated benchmark execution, `DynamicToolRegistry` registration/schema export, `HardwareAwarenessEngine.assessThermalState`. |
| `LearningPersistenceTest` | 8 | 8 | 0 | 0 | `0.207` | `AgentLearningPersistence` (`SharedPreferences` serialization of episodic memories, skills, semantic facts, and `SafetyAuditEntry`). |
| `LocalSlmEngineTest` | 5 | 5 | 0 | 0 | `0.089` | `LocalSlmIntentEngine.validateGbnfGrammar`, `DeterministicRegexParser`, `LlamaAndroidAdapter.unloadModel`, and `extractIntentOnDemand` regex fallback. |
| `OptimizerAndPrunerTest` | 5 | 5 | 0 | 0 | `0.075` | `SemanticTreePruner` node filtering/bounding-box normalization, `HelioCorePilotAndMemoryOptimizer`, and `FailsafeRecoveryRing` strategy cycle. |
| `ReflectionAndRecoveryTest` | 7 | 7 | 0 | 0 | `0.095` | `SelfReflectionEngine.analyzeFailure`, `RetryPolicyEngine` backoff calculation, `ConversationSessionManager`, `VoiceInteractionController` state transitions. |
| `StepVerifierTest` | 7 | 7 | 0 | 0 | `0.063` | `StepVerifier` rule evaluation (`PackageMatches`, `NodeTextContains`, `NodeExists`, `ScreenChanged`, `Custom`, and `AlwaysPass -> INCONCLUSIVE`). |
| `TargetResolverSafetyTest` | 2 | 2 | 0 | 0 | `0.041` | `TargetResolver` password-field rejection (`isPassword = true`) and ambiguous multi-node rejection when text matches multiple nodes without viewId. |
| `ThreeFunctionalitiesBatchTest` | 4 | 4 | 0 | 0 | `0.052` | `ModelDownloadManager` status check, `VisualOverlayRenderer`, `SkillGraphEngine` DAG construction. |
| **Total** | **74** | **74** | **0** | **0** | **`3.249`** | **All 74 tests are pure JVM unit tests using JUnit 4 + MockK (`testOptions.unitTests.isReturnDefaultValues = true`); 0 physical-device or instrumentation tests.** |

#### Native Libraries Packaged in `agent-created-by-gemini` Debug APK

Inspection via `unzip -l app/build/outputs/apk/debug/app-debug.apk | grep "\.so$"`:
- `lib/arm64-v8a/libandroidx.graphics.path.so` (`10,096` bytes)
- `lib/armeabi-v7a/libandroidx.graphics.path.so` (`7,252` bytes)
- `lib/x86/libandroidx.graphics.path.so` (`10,036` bytes)
- `lib/x86_64/libandroidx.graphics.path.so` (`10,424` bytes)
- **Finding:** Although `LlamaAndroidAdapter.kt` calls `System.loadLibrary("llama-bridge")`, **no `libllama-bridge.so` or C++/JNI source exists anywhere in the repository or built APK**.

#### CI Workflow Audit (`agent-created-by-gemini` — `.github/workflows/ci.yml`)

- Latest GitHub Actions run for `90f5cf3ca0eca324f91a43ba96fab72c2e2de5ed`: Run ID `40505149792` (`conclusion: success`).
- **Why CI is green despite broken `lintDebug` and broken `assembleRelease`:**
  1. `.github/workflows/ci.yml` never runs `./gradlew :app:lintDebug`.
  2. Step `Build Release APK (non-blocking)` sets `continue-on-error: true` and executes `./gradlew :app:assembleRelease --stacktrace || true`, swallowing exit code `1` from R8's syntax error at `app/proguard-rules.pro:14:29`.

---

### 3.2 `mobile_agent`

- **Repository URL:** `https://github.com/iam816anonymous-commits/mobile_agent.git`
- **Branch / Pinned Commit:** `android-local-agent-os-base-65071498209142568` @ `863723f7aa4b42137ff4ce07e26e541e402ff5a8` (`git rev-parse HEAD` matches recorded pin; remote branch has not moved).
- **Build System:** Gradle Wrapper `8.5`, AGP `8.2.2`, Kotlin `1.9.22`, `compileSdk = 34`, `minSdk = 26`, `targetSdk = 34`, `jvmTarget = "17"`. Multi-module structure: `:app`, `:accessibility`, `:ai`, `:core`, `:memory`, `:tools`.

#### Command-by-Command Execution Table (`mobile_agent`)

| Step | Exact Command Executed | Exit Code | Classification | Log Reference | Summary of Actual Output |
|---|---|---|---|---|---|
| 1. Clone & Verify Pin | `git clone https://github.com/iam816anonymous-commits/mobile_agent.git && git checkout 863723f7aa4b42137ff4ce07e26e541e402ff5a8` | `0` | **PASS** | `/tmp/phase0-logs/mobile_agent-version-projects.log` | `HEAD is now at 863723f Implement core Android Local AI Agent OS architecture and modules` |
| 2. Wrapper & Projects | `./gradlew --version && ./gradlew projects` | `0` | **PASS** | `/tmp/phase0-logs/mobile_agent-version-projects.log` | `Gradle 8.5`. Root project `AndroidAgentOS` with 6 subprojects: `:accessibility`, `:ai`, `:app`, `:core`, `:memory`, `:tools`. |
| 3. Unit Tests (All Modules) | `./gradlew test --continue --stacktrace` | `0` | **PASS (1 test only; 5 modules NO-SOURCE)** | `/tmp/phase0-logs/mobile_agent-test.log` | `:ai:testDebugUnitTest` and `:ai:testReleaseUnitTest` executed **1 test** (`com.android.agentos.ai.PlannerTest#testChromePlanGeneration`, `0.044s`, PASS). `:app`, `:accessibility`, `:core`, `:memory`, `:tools` all reported `NO-SOURCE` (0 tests). |
| 4. Android Lint (All Modules) | `./gradlew lintDebug --continue --stacktrace` | `1` | **FAIL** | `/tmp/phase0-logs/mobile_agent-lintDebug.log` | `:accessibility:lintDebug`, `:ai:lintDebug`, `:core:lintDebug`, `:memory:lintDebug`, `:tools:lintDebug` passed. **`:app:lintDebug` FAILED** (`1 error, 1 warning`): `app/src/main/AndroidManifest.xml:4: Error: A <queries> declaration should generally be used instead of QUERY_ALL_PACKAGES [QueryAllPackagesPermission]`. |
| 5. Debug APK Build | `./gradlew :app:assembleDebug --stacktrace` | `0` | **PASS** | `/tmp/phase0-logs/mobile_agent-assembleDebug.log` | `BUILD SUCCESSFUL in 8s`, `126 actionable tasks: 19 executed, 107 up-to-date`. Output APK: `app/build/outputs/apk/debug/app-debug.apk` (**17,846,968 bytes**, SHA-256: `77cc18b815ac108c5da8aa5e239b711f5f396a8001f30e4c615b66548d22191d`). |
| 6. Release APK Build | `./gradlew :app:assembleRelease --stacktrace` | `0` | **PASS (Unsigned / Unminified)** | `/tmp/phase0-logs/mobile_agent-assembleRelease.log` | `BUILD SUCCESSFUL in 15s`. Output APK: `app/build/outputs/apk/release/app-release-unsigned.apk` (**14,339,843 bytes**, SHA-256: `b83044e20243dbb06a4e570d49b05865705b6b0d77a6c`). Note: `isMinifyEnabled = false` and no release signingConfig is configured. |
| 7. Dependency Tree | `./gradlew :app:dependencies :core:dependencies :accessibility:dependencies :ai:dependencies :memory:dependencies :tools:dependencies` | `0` | **PASS** | `/tmp/phase0-logs/mobile_agent-dependencies.log` | Full multi-module dependency graph exported (`399 KB` log). |

#### Critical Build/Runtime Findings in `mobile_agent`

1. **Broken Room Annotation Processing in `:memory`:** `gradle/libs.versions.toml` defines `androidx-room-compiler = { group = "androidx.room", name = "room-compiler", version.ref = "room" }`, but `memory/build.gradle.kts` only adds `implementation(libs.androidx.room.runtime)` and `implementation(libs.androidx.room.ktx)` with **no KSP or KAPT plugin or `room-compiler` processor**. Consequently, `AgentDatabase_Impl` is never generated during compilation (`find memory/build -name "*AgentDatabase*"` returns only `AgentDatabase.class`), and calling `MemoryManager(context)` at runtime will immediately throw `RuntimeException: cannot find implementation for com.android.agentos.memory.AgentDatabase. AgentDatabase_Impl does not exist`.
2. **Native Libraries Packaged in `mobile_agent` APK:**
   - `lib/arm64-v8a/libllm_inference_engine_jni.so` (`5,508,272` bytes, from `com.google.mediapipe:tasks-genai:0.10.11`)
   - `lib/arm64-v8a/libandroidx.graphics.path.so` (`10,088` bytes, plus `armeabi-v7a`, `x86`, `x86_64` variants)
   - As proven by the `tecno-os` GPUProbe physical-device report (`docs/hardware/gpuprobe-apk-validation-2026-11-02.md`), `libllm_inference_engine_jni.so` dynamically links against `libOpenCL.so`, which is blocked by the Android linker namespace for ordinary third-party APKs on the TECNO IN6.

---

### 3.3 `agent-1`

- **Repository URL:** `https://github.com/iam816anonymous-commits/agent-1.git`
- **Branch / Pinned Commit:** `jules-10149456198484707000-5b5421da` @ `6d0c3002fc4c716dea2ffcba31339f0de79fd087` (`git rev-parse HEAD` matches recorded pin; remote branch has not moved).
- **Build System:** Gradle Wrapper `8.8`, AGP `8.2.2`, Kotlin `1.9.22`, `compileSdk = 34`, `minSdk = 26`, `targetSdk = 34`. Modules: `:app` (Android application), `:core` (pure Kotlin JVM library), `:infrastructure` (pure Kotlin JVM library).

#### Command-by-Command Execution Table (`agent-1`)

| Step | Exact Command Executed | Exit Code | Classification | Log Reference | Summary of Actual Output |
|---|---|---|---|---|---|
| 1. Clone & Verify Pin | `git clone https://github.com/iam816anonymous-commits/agent-1.git && git checkout 6d0c3002fc4c716dea2ffcba31339f0de79fd087` | `0` | **PASS** | `/tmp/phase0-logs/agent-1-version-projects.log` | `HEAD is now at 6d0c300 Complete full lifecycle architecture and multi-skill verification for Autonomous Personal Agent.` |
| 2. Wrapper & Projects | `./gradlew --version && ./gradlew projects` | `0` | **PASS** | `/tmp/phase0-logs/agent-1-version-projects.log` | `Gradle 8.8`. Root project `AutonomousAgent` with subprojects `:app`, `:core`, `:infrastructure`. |
| 3. Pure-JVM Module Unit Tests (`:core` & `:infrastructure`) | `./gradlew :core:test :infrastructure:test --stacktrace` | `0` | **PASS** | `/tmp/phase0-logs/agent-1-jvm-tests.log` | `BUILD SUCCESSFUL in 25s`. Executed **4 test classes, 11 unit tests, 0 failures**: `com.agent.core.learning.LearningEngineTest` (1), `com.agent.core.OrchestratorEndToEndTest` (`MultiSkillEndToEndTest`, 4), `com.agent.infrastructure.documents.DocumentManagerTest` (3), `com.agent.infrastructure.providers.ProvidersTest` (3). |
| 4. Clean Unpatched `:app` Test / Build / Lint (Reproducing CI Failure) | `./gradlew test --continue --stacktrace` / `./gradlew :app:assembleDebug --stacktrace` / `./gradlew lintDebug --continue --stacktrace` | `1` | **FAIL** | `/tmp/phase0-logs/agent-1-test.log`, `/tmp/phase0-logs/agent-1-assembleDebug.log`, `/tmp/phase0-logs/agent-1-lintDebug.log` | `> Task :app:mapDebugSourceSetPaths FAILED`: `Configuration :app:debugRuntimeClasspath contains AndroidX dependencies, but the android.useAndroidX property is not enabled, which may lead to runtime issues. Set android.useAndroidX=true in the gradle.properties file and retry.` |
| 5. Diagnostic Probe #1 (Pass `-Pandroid.useAndroidX=true` only) | `./gradlew :app:assembleDebug -Pandroid.useAndroidX=true --stacktrace` | `1` | **FAIL (Diagnostic)** | `/tmp/phase0-logs/agent-1-diagnostic-androidx.log` | `> Task :app:mergeExtDexDebug` crashes with `org.gradle.launcher.daemon.client.DaemonStoppedException: Gradle build daemon has been stopped: since the JVM garbage collector is thrashing` because missing `gradle.properties` leaves Gradle daemon at default `-Xmx512m`. |
| 6. Diagnostic Probe #2 (Pass `-Pandroid.useAndroidX=true` + `-Xmx2048m`) | `./gradlew :app:assembleDebug -Pandroid.useAndroidX=true -Dorg.gradle.jvmargs="-Xmx2048m -XX:MaxMetaspaceSize=512m" --stacktrace` | `1` | **FAIL (Diagnostic)** | `/tmp/phase0-logs/agent-1-diagnostic-androidx-heap.log` | `> Task :app:processDebugResources FAILED`: `Android resource linking failed`: `AndroidManifest.xml:9: error: resource mipmap/ic_launcher not found`, `error: resource mipmap/ic_launcher_round not found`, `AndroidManifest.xml:12: error: resource android:style/Theme.Material.NoTitleBar not found`. Directory `app/src/main/res` does not exist in `agent-1`. |
| 7. Dependency Tree | `./gradlew :app:dependencies :core:dependencies :infrastructure:dependencies` | `0` | **PASS** | `/tmp/phase0-logs/agent-1-dependencies.log` | Full dependency graph exported (`98 KB` log). |

#### Root-Cause Analysis of `agent-1` GitHub Actions CI Failure (Run `37294356933`)

- **Failing Workflow Run:** `https://github.com/iam816anonymous-commits/agent-1/actions/runs/37294356933` (Job `39570853864`, workflow `.github/workflows/android.yml`, commit `6d0c3002fc4c716dea2ffcba31339f0de79fd087`, Step 9 `Build with Gradle` executing `./gradlew build`).
- **Three Stacked Defects Exposed:**
  1. **Defect 1 (Immediate CI Failure at `:app:mapDebugSourceSetPaths`):** `agent-1` has no `gradle.properties` file at the repository root. Earlier commit `108ede6` (`Fix Gradle build failure by disabling checkAarMetadata for AndroidX compatibility`) attempted to bypass AndroidX checks by adding `tasks.matching { it.name.startsWith("check") && it.name.endsWith("AarMetadata") }.configureEach { enabled = false }` in `app/build.gradle.kts`, which does not satisfy AGP `8.2.2`'s AndroidX enforcement during `:app:mapDebugSourceSetPaths`.
  2. **Defect 2 (JVM Heap Exhaustion at `:app:mergeExtDexDebug`):** Because `gradle.properties` is absent, `org.gradle.jvmargs` is unset and defaults to a 512 MiB heap, causing R8/D8 dex merging of Compose + Coroutines + Room + OkHttp to thrash the GC and kill the Gradle daemon.
  3. **Defect 3 (Missing `app/src/main/res/` Directory at `:app:processDebugResources` and Missing Room KSP/KAPT):** `app/src/main/AndroidManifest.xml` references `@mipmap/ic_launcher`, `@mipmap/ic_launcher_round`, and `@style/Theme.Material.NoTitleBar`, but `app/src/main/` contains only `AndroidManifest.xml` and `java/` — **no `res/` directory exists at all**. Additionally, `app/build.gradle.kts` declares `implementation("androidx.room:room-runtime:2.6.1")` and `implementation("androidx.room:room-ktx:2.6.1")` without KSP/KAPT or `room-compiler`, so `AppDatabase` in `Persistence.kt` has no generated implementation.
- **Reuse Consequence:** The `:app` module of `agent-1` was never successfully compiled or packaged into an APK by its author. Only the pure-JVM `:core` domain concepts (`Contracts.kt`, `LearningEngine.kt`) are candidates for `REFERENCE` / `ADAPT`, and even those must not be copied verbatim.

---

### 3.4 `skill-agent`

- **Repository URL:** `https://github.com/iam816anonymous-commits/skill-agent.git`
- **Branch / Pinned Commit:** `phase-0-specifications-14452037546145599308` @ `86cfa6960b1341ed53b987f5a50b1c623ec18ad4` (`git rev-parse HEAD` matches recorded pin).
- **Contents:** 16 files total (`README.md`, `AGENTS.md`, and 14 markdown specification files under `docs/00_INDEX.md` through `docs/12_SECURITY_MODEL.md`).
- **Build / Test / Lint Status:** **NOT RUN (Specification-Only)**. No Gradle wrapper, build script, or source code exists.

---

### 3.5 `tecno-os`

- **Repository URL:** `https://github.com/iam816anonymous-commits/tecno-os.git`
- **Branch / Commits Inspected:**
  - Recorded pin in `ENGINEERING/ARGON/ARGON_DEVELOPMENT_HANDOFF.md`: `c8a23e359de5b30297ca731f713b89077e056cfc`
  - Current branch head on `in6-linux-bringup-infrastructure-4378631429758063630`: `d2aeac357141e75b601a935869abb8944ad0d41d` (4 commits ahead; `tests/host/` is identical between both commits).
- **Host Python Validation Suite (`tests/host/test_*.py`):** Executed individually with `python3 <test_file>` (Log: `/tmp/phase0-logs/tecno-os-host-tests.log`).

| Host Test Script | Exit Code | Classification | Actual Result & Reason |
|---|---|---|---|
| `tests/host/test_boot_safety.py` | `0` | **PASS** | `Ran 1 test in 0.002s — OK` (verifies flash scripts reject `boot_a`/`boot_b` and non- fastboot modes). |
| `tests/host/test_initramfs_shebang.py` | `0` | **PASS** | `Ran 1 test in 0.036s — OK` (verifies `build-initramfs.sh` uses `#!/usr/bin/env bash` and validatesinitramfs structure). |
| `tests/host/test_inventory.py` | `0` | **PASS** | `Ran 4 tests in 0.001s — OK` (verifies `inventory/boot-header.txt`, `cmdline.txt`, `partitions-by-name.txt`, `properties.txt`). |
| `tests/host/test_kernel_config.py` | `0` | **PASS** | `Ran 1 test in 0.015s — OK` (verifies `mt6763_in6_min_defconfig` flags). |
| `tests/host/test_boot_image.py` | `1` | **BLOCKED (Missing `cpp` / `cpio` in container)** | `Ran 1 test — FAILED`: `scripts/build/build-dts.sh` requires C preprocessor (`cpp`) and `cpio`, which are not installed in the Android build container. |
| `tests/host/test_dts.py` | `1` | **BLOCKED (Missing `cpp` in container)** | `Ran 2 tests — FAILED (failures=1)`: `test_dts_syntax` passes, `test_dts_compilation` fails because `cpp: command not found`. |
| `tests/host/test_initramfs.py` | `1` | **BLOCKED (Missing `cpio` in container)** | `Ran 1 test — FAILED`: `ERROR: Missing required tool: cpio`. |
| `tests/host/test_kernel_build.py` | `1` | **BLOCKED (Missing ARM64 kernel cross-compiler)** | `Ran 1 test — FAILED`: `ERROR: Missing required tool: aarch64-linux-gnu-gcc`. |
| `tests/host/test_qemu_boot.py` | `1` | **BLOCKED (Requires built kernel `Image.gz`)** | `Ran 1 test — FAILED`: `Kernel Image.gz not found. Run build-kernel.sh first.` |
