# Android Autonomous Agent — GitHub Repository Deep Dive

**Research date:** 2026-11-03  
**Destination:** Personal Autonomous Android Agent  
**Primary compatibility target:** Android 8.1 / API 27 (TECNO Camon i Click / IN6 family; exact RAM variant still needs physical confirmation)  
**Deployment assumption:** Personal sideloaded app; no Google Play publication requirement at present  
**Research scope:** 17 external repositories, selected source files, architecture documents, test fixtures, licenses, and official Android platform documentation. This is a curated engineering audit, not an exhaustive search of all GitHub.

## 1. Executive conclusion

Do not build this as a free-running LLM that repeatedly guesses taps. Build a **durable, policy-governed, observe–plan–act–verify state machine** with one authoritative execution loop and narrow Android adapters.

The strongest references solve different parts of the problem:

- **Google ADK for Kotlin** is now the highest-priority Kotlin framework proof-of-concept: it has Android-specific modules, Room-backed sessions/events/state, human confirmation events, resumability and multiple model backends. It is an orchestration/model framework, not our Android actuator or safety authority.
- **OpenDroid** is the closest Android-native Kotlin reference: a real app structure with plan management, accessibility service, action dispatch, Room history, recovery, model downloads, and instrumentation tests.
- **Mobilerun** demonstrates a useful Manager → Executor split, shared device state, trajectory capture, accessibility + screenshot observation, and differentiated retry/recovery behavior. It is a host-side Python agent, not an APK runtime.
- **Llamatik** provides a Kotlin agent runtime with tool/action registries, capability checks, permission decisions, confirmation, audit and workflows.
- **Hermes** is a strong reference for a single agent loop, tool registry, session persistence, memory, approval and scheduling patterns.
- **AndroidWorld** provides an evaluation methodology and task corpus; it is not an Android app architecture.
- **Maestro, Appium UiAutomator2 and OpenATX uiautomator2** are valuable automation and regression-testing references, not components to embed as the production agent.
- **Mobile-Agent** contributes GUI grounding, progress tracking, reflection and memory research, but its multimodal models are not suitable as the default runtime on a low-resource API 27 phone.
- **LangGraph** contributes checkpointing and durable execution concepts, but its server-oriented stack is not a good embedded Android dependency.
- **Google AI Edge Gallery and llama.cpp** are on-device LLM references, but the inspected Android apps require API 31 and API 33 respectively. They are not API 27 baselines.
- **sherpa-onnx** provides Android-native Kotlin/JNI streaming ASR and keyword spotting samples with minSdk 21, making it a strong candidate for a separate API 27 voice-input proof-of-concept.
- **Mobilerun Portal** exposes a sophisticated Android AccessibilityService, target-window resolver, gesture controller and MediaProjection fallback for API 26–29; it is AGPL-3.0 and has a broad remote-control surface, so study only and do not transplant its code.

The correct strategy is to retain our Kotlin core, strengthen the Android execution boundary, use OpenDroid/Llamatik/Hermes patterns selectively, and use AndroidWorld/Maestro-style test fixtures to prove behavior.

## 2. Repository audit matrix

The commit pins below identify the exact source snapshots inspected during this research. Repository license is only an initial reuse signal; nested dependencies, assets, model weights, datasets and copied source headers still need separate review.

| Repository and pinned revision | License observed | What was inspected | Reuse decision |
|---|---|---|---|
| [google/adk-kotlin](https://github.com/google/adk-kotlin/tree/b961c9e4aa50c79dbf4c17d870ab5629424a5e49) — `b961c9e4aa50c79dbf4c17d870ab5629424a5e49` | Apache-2.0 | ADK Kotlin 1.5.0 README, Android examples, RoomSessionService, Room DB, confirmation-capable FunctionTool, ResumabilityConfig, LiteRT-LM and ML Kit module build files | **Highest-priority isolated POC for model/tool orchestration and Room session history; keep our outer policy/actuator authority.** |
| [JMAN730/opendroid](https://github.com/JMAN730/opendroid/tree/7c5dcd40f465fd8affe6c227c332fba9af80a600) — `7c5dcd40f465fd8affe6c227c332fba9af80a600` | Apache-2.0 | Architecture doc, AccessibilityService, node traversal, PlanManager, AgentLoop, ActionDispatcher, PlanValidator, ReEvaluationEngine, AutoApprovalPolicy, Room database/history, model downloader, instrumentation fixtures | **Highest-priority Android-native reference. Adapt patterns; do not wholesale copy.** |
| [droidrun/mobilerun](https://github.com/droidrun/mobilerun/tree/11dcdebdec9ea99f172dd7e80af7ba1eadbeb6c8) — `11dcdebdec9ea99f172dd7e80af7ba1eadbeb6c8` | MIT | MobileAgent wrapper, ManagerAgent, ExecutorAgent, shared state, ToolRegistry, UI StateProvider and retry/recovery policy | **Adapt orchestration, state and retry concepts.** Host Python/ADB architecture is not an in-app Android runtime. |
| [llamatik/Llamatik](https://github.com/llamatik/Llamatik/tree/10f3a99d5f74d727c43b3e1475b8931403e592d2) — `10f3a99d5f74d727c43b3e1475b8931403e592d2` | MIT | AgentRuntime, AgentExecutionEngine, permission and confirmation models, tool registry, workflow engine, memory, tests | **Adapt Kotlin contracts and permission/confirmation/audit pipeline.** Add durable execution and independent verification beyond its basic workflow engine. |
| [NousResearch/hermes-agent](https://github.com/NousResearch/hermes-agent/tree/c314285f077e91937c995d419b9fa40af9eb4981) — `c314285f077e91937c995d419b9fa40af9eb4981` | MIT | Conversation loop, approval module, SQLite state store, architecture and agent-loop docs | **Adapt loop, registry, persistence, memory and approval concepts.** Do not port its Python runtime, shell or broad terminal tools. |
| [google-research/android_world](https://github.com/google-research/android_world/tree/55ddaf5edc6f6c25673aed343907d5b9dca66f84) — `55ddaf5edc6f6c25673aed343907d5b9dca66f84` | Apache-2.0 | Benchmark README, agent interface, M3A implementation and task methodology | **Reference-only for benchmark and verifier design.** |
| [X-PLUG/MobileAgent](https://github.com/X-PLUG/MobileAgent/tree/c4cd0a8f9ddedad24dd8a3daf3a56ca69343849f) — `c4cd0a8f9ddedad24dd8a3daf3a56ca69343849f` | MIT | Mobile-Agent family README and v3/v3.5 research claims | **Reference-only for multimodal grounding, progress, reflection and memory.** Model/runtime cost is a poor fit for the baseline API 27 device. |
| [mobile-dev-inc/Maestro](https://github.com/mobile-dev-inc/Maestro/tree/b80bd24a42892eb75440bb8e9d8bee24f1292cc0) — `b80bd24a42892eb75440bb8e9d8bee24f1292cc0` | Apache-2.0 | README, accessibility-driven test philosophy, smart waits, YAML flow model | **Use as a testing and repeatable-flow reference**, not as the agent's planner or runtime. |
| [appium/appium-uiautomator2-server](https://github.com/appium/appium-uiautomator2-server/tree/56bf27a222b9aa242e6a40cf87825b78b443b7be) — `56bf27a222b9aa242e6a40cf87825b78b443b7be` | Apache-2.0 | README, server model and instrumentation build/run approach | **Testing reference only.** Repository README says development moved to appium-android monorepo. |
| [openatx/uiautomator2](https://github.com/openatx/uiautomator2/tree/5d373ad83a94d85ebfd088cacb58d06ac40a00d2) — `5d373ad83a94d85ebfd088cacb58d06ac40a00d2` | MIT | README, device-side HTTP server + Python client architecture, selector and hierarchy APIs | **Testing/diagnostics reference only.** Not a direct in-app dependency. |
| [langchain-ai/langgraph](https://github.com/langchain-ai/langgraph/tree/3c916d750575b22c464790e9774494d810ccd322) — `3c916d750575b22c464790e9774494d810ccd322` | MIT | Checkpoint package and persistence/checkpointer docs | **Adapt checkpoint semantics.** Do not embed server-oriented Python/DB infrastructure. |
| [MadeAgents/mobile-use](https://github.com/MadeAgents/mobile-use/tree/babec07fd0e5faa7e7bcc7d3d0ee2320f6b83347) — `babec07fd0e5faa7e7bcc7d3d0ee2320f6b83347` | MIT | HierarchicalAgent, MultiAgent, planner/operator/answer/reflector/progressor/note-taker composition and trajectory | **Adapt task-role separation and trajectory reflection; host ADB/VLM runtime is not an Android app.** |
| [droidrun/mobilerun-portal](https://github.com/droidrun/mobilerun-portal/tree/00a77c01e2ee4fcc71889a3992bc1753d64e9ed9) — `00a77c01e2ee4fcc71889a3992bc1753d64e9ed9` | AGPL-3.0 | AccessibilityService, foreground window resolver, node/action APIs, gesture controller, API30 screenshot adapter, API26–29 MediaProjection screenshotter; minSdk 26 | **Reference-only. Do not copy code without explicit AGPL decision; remote-control/auto-accept features are out of scope.** |
| [google-ai-edge/gallery](https://github.com/google-ai-edge/gallery/tree/6d09e2ea0823b0868800ef3b5752852715b58b91) — `6d09e2ea0823b0868800ef3b5752852715b58b91` | Apache-2.0 | Function_Calling_Guide, typed Action/Tool callback path, Android app Gradle config; minSdk 31 | **Reference-only for on-device model catalog and tool integration; too new for API 27.** |
| [k2-fsa/sherpa-onnx](https://github.com/k2-fsa/sherpa-onnx/tree/935e5ded3d949a8b557ac44ef4dad03a7d2038e7) — `935e5ded3d949a8b557ac44ef4dad03a7d2038e7` | Apache-2.0 | Android streaming ASR and KWS samples, Kotlin JNI wrappers, AudioRecord loop; minSdk 21 | **High-priority API 27 speech/KWS POC; audit each downloaded model's licence and memory footprint.** |
| [ggml-org/llama.cpp](https://github.com/ggml-org/llama.cpp/tree/71efc559a00b236851f6ba00a629d776f2416d2b) — `71efc559a00b236851f6ba00a629d776f2416d2b` | MIT | C/C++ inference engine and Android Kotlin sample build; sample minSdk 33 | **Runtime research only; Android sample is not compatible with API 27 as shipped.** |
| [google-ai-edge/litert-samples](https://github.com/google-ai-edge/litert-samples/tree/e98147beea80ee90f7f74ed975bca9c78667d87c) — `e98147beea80ee90f7f74ed975bca9c78667d87c` | Apache-2.0 | Android offline voice assistant README, tool execution source, model catalog/download architecture | **High-value inference/model-management reference, but not API 27-compatible as inspected.** Verify exact revision and model/runtime license before adoption. |

### Important distinction

“Repository studied” does not mean every line was audited or every build was run. The source paths listed below were fetched and inspected; some repositories were studied at README/architecture level, others at implementation and test-source level. No claim is made that every project was independently built during this research.

## 3. Findings by repository

### 3.1 OpenDroid — closest Android-native implementation reference

Inspected source paths:

- `docs/architecture.md`
- `app/src/main/java/com/opendroid/ai/accessibility/OpenDroidAccessibilityService.kt`
- `app/src/main/java/com/opendroid/ai/accessibility/AccessibilityNodeTraversal.kt`
- `app/src/main/java/com/opendroid/ai/accessibility/GenericAppAutomator.kt`
- `app/src/main/java/com/opendroid/ai/accessibility/TargetAppGuard.kt`
- `app/src/main/java/com/opendroid/ai/core/agent/PlanManager.kt`
- `app/src/main/java/com/opendroid/ai/core/agent/AgentLoop.kt`
- `app/src/main/java/com/opendroid/ai/core/agent/PlanValidator.kt`
- `app/src/main/java/com/opendroid/ai/core/agent/ReEvaluationEngine.kt`
- `app/src/main/java/com/opendroid/ai/core/agent/AutoApprovalPolicy.kt`
- `app/src/main/java/com/opendroid/ai/actions/ActionDispatcher.kt`
- `app/src/main/java/com/opendroid/ai/data/db/OpenDroidDatabase.kt`
- `app/src/main/java/com/opendroid/ai/data/db/entities/TaskHistoryEntity.kt`
- `app/src/main/java/com/opendroid/ai/core/llm/ModelDownloadWorker.kt`
- `app/src/androidTest/java/com/opendroid/ai/accessibility/ClickTargetingAccuracyInstrumentationTest.kt`

Useful engineering lessons:

1. **Plan state is shared mutable state and needs synchronization.** PlanManager explicitly serializes read-modify-write operations with a Mutex because the execution coroutine and plan editor can mutate the same plan concurrently. Our own plan state must have one authoritative writer or a transaction/lock around every transition.
2. **Validate a plan before presenting/approving it.** PlanManager returns the validated plan, not the raw model plan, because validation can insert or modify steps. The user must approve the exact normalized plan that will execute.
3. **Use an explicit action schema and unknown-action behavior.** PlanValidator checks registered actions and can create user-input steps. The destination should validate tool ID, typed parameters, capability requirements, target package, risk, dependencies and action bounds before dispatch.
4. **Separate the planner, plan state, dispatcher and recovery engine.** AgentLoop sequences execution; PlanManager owns state; ActionDispatcher resolves a finite action set; ReEvaluationEngine handles selected failures. This is a better separation than a single giant class.
5. **Retry only while the target is expected to become available.** GenericAppAutomator uses a bounded 5-second wait with 300 ms polling after cold app launches. This is appropriate for UI readiness, not a blanket retry policy for payments, messages or other non-idempotent actions.
6. **Target-app identity matters.** TargetAppGuard requires a known target package to match the active window; unknown target is a rejection. Our dispatcher must not click a same-labelled node in our own UI or a different app.
7. **Accessibility traversal should be a pure/testable component.** OpenDroid extracts node traversal so tests can supply a UiAutomation root without needing the bound service. This pattern is especially valuable for API 27 regression tests.
8. **Test the actual target, not just a re-read of the tree.** ClickTargetingAccuracyInstrumentationTest stages decoy and real controls and checks per-view click counters. Merely observing that a node disappeared or a screen changed is not sufficient proof that the correct target was clicked.
9. **Model downloads are a subsystem.** The inspected downloader resolves URL, destination, size and checksum from a trusted local model registry rather than trusting WorkManager input. It checks memory compatibility, uses foreground WorkManager for long transfers, preserves partial files on cancellation and resumes with HTTP Range. This is a strong pattern for our later model manager.

Do not copy OpenDroid's approval shortcut uncritically. Its AutoApprovalPolicy includes an explicit YOLO mode that approves every plan and bypasses its never-auto-approve guard. That is not acceptable as our default. Our agent must remain deny-by-default for sensitive actions and must never let a model prompt or model-generated flag grant its own authority.

Do not assume OpenDroid's README/roadmap claims prove API 27 physical-device behavior. Its roadmap reports minSdk 26 and an API 26/36 CI matrix; that is useful source evidence, not a test result for our TECNO device.

### 3.2 Mobilerun — orchestration and state management

Inspected source paths:

- `mobilerun/agent/droid/droid_agent.py`
- `mobilerun/agent/droid/state.py`
- `mobilerun/agent/manager/manager_agent.py`
- `mobilerun/agent/executor/executor_agent.py`
- `mobilerun/agent/tool_registry.py`
- `mobilerun/tools/ui/provider.py`

Useful lessons:

- The **Manager** owns task interpretation, subgoals, progress and completion decisions.
- The **Executor** receives one subgoal, inspects current UI state and selects a concrete action.
- A shared state object keeps current and previous UI state, accessibility tree, screenshot, foreground package/activity, plan, subgoal, action history, outcomes and progress summary.
- The system supports both a fast one-agent path and a more deliberate manager/executor path. We should use a single-step executor and a compact planner, not create a multi-agent swarm for every tap.
- UI StateProvider validates required state fields and applies retry/recovery logic. Its retry policy distinguishes timeouts from other errors: timeouts get a limited retry because a device-side request may still be running, while other failures use a bounded backoff and may trigger service recovery.
- Trajectory/state capture is a first-class concern. We need a bounded local event trail so failures can be diagnosed and recovery can use observed facts rather than model recollection.

Limitations for our use:

- Mobilerun is a host-side Python framework using Android drivers/ADB and a companion Portal setup. It is not a Kotlin library to drop into our APK.
- Its screenshots and host-side device drivers are not the same as API 27 AccessibilityService capabilities.
- Its retry logic is a reference, not permission to retry uncertain side effects.

### 3.3 Llamatik — Kotlin-native runtime contracts

Inspected source paths:

- `sdk-agent/.../runtime/AgentRuntime.kt`
- `sdk-agent/.../runtime/AgentExecutionEngine.kt`
- `sdk-agent/.../runtime/AgentWorkflow.kt`
- `sdk-agent/.../registry/ToolRegistry.kt`
- `sdk-agent/.../confirmation/ConfirmationModels.kt`
- `sdk-agent/.../permissions/PermissionModels.kt`
- `sdk-agent/.../memory/AgentMemory.kt`
- `sdk-agent/src/commonTest/.../ExecutionEngineTest.kt`

Useful lessons:

- Execution order is explicit: tool lookup → availability check → permission check → risk/confirmation policy → action validation → action/tool execution → audit result.
- Permission states distinguish GRANTED, DENIED, ASK_EVERY_TIME, REQUIRE_CONFIRMATION and NOT_SET. This is more expressive than a single boolean.
- Confirmation policy is separate from Android runtime permissions. A granted Android permission does not imply the user approved this specific action.
- The workflow engine supports conditional steps, step outputs, retry limits and stop-on-error.
- Runtime events expose planning, execution and completion states for UI.
- Tests cover unknown tools, unavailable tools, denied permissions and confirmation decisions.

What needs improvement before adopting:

- Its example workflow retries exceptions and does not independently verify a device postcondition. Our agent must not equate a returned success string with a verified state change.
- Its sample memory is a small Settings-backed store with substring search and a capped entry count. Use Room, explicit memory provenance, retention and user controls instead.
- Tool registration should reject duplicates and freeze or version the registry for a task. The inspected ToolRegistry overwrites an existing tool ID.
- A workflow should persist each transition and be resumable after process death; an in-memory Flow alone is not durable.

### 3.4 Hermes — mature single-loop and persistence reference

Previously inspected in depth at commit `c314285f077e91937c995d419b9fa40af9eb4981`:

- `agent/conversation_loop.py`
- `tools/registry.py`
- `tools/approval.py`
- `hermes_state.py`
- `hermes_state_fts.py`
- memory and skill modules
- scheduler/cron modules

Useful lessons:

- One authoritative conversation loop is easier to reason about than several competing execution loops.
- Central tool schema/registry/dispatch provides a single place to validate and route calls.
- SQLite WAL, full-text search and explicit session lineage provide useful persistence and recall patterns.
- Approval must be a separate policy/transport decision, not a free-form instruction to the model.
- Context compression must preserve tool-call/result pairs and save important state before lossy summarization.
- Scheduler lifecycle and restart guards deserve their own subsystem.

Do not port its Python runtime, terminal/shell tools, import-time dynamic tool registration or broad server infrastructure to the Android app.

### 3.5 AndroidWorld — evaluation, not runtime

The repository describes 116 hand-crafted tasks across 20 apps, dynamically instantiated into many task variations. Its agent interface repeatedly observes state, takes an action and processes feedback. M3A uses visual + textual UI data; T3A is text-only; SeeAct uses visual grounding.

Our adaptation:

- Build a smaller, deterministic task set for our own APK and controlled probe apps.
- Every test needs a known initial state and an independent verifier.
- Include ambiguous labels, duplicate controls, stale nodes, keyboard transitions, scrolling, dialogs, app launch delays, permission denial, process death and action interruption.
- Use text-only semantic interaction as the baseline on API 27; visual grounding is an optional later capability.
- AndroidWorld's emulator/API33 setup is not a substitute for the physical TECNO API 27 acceptance matrix.

### 3.6 Mobile-Agent — visual grounding and long-horizon reasoning

The repository describes Mobile-Agent-v3 as a multimodal, multi-platform framework with planning, progress management, reflection and memory. Its newer GUI-Owl family is a large vision-language model family.

Transfer only the principles:

- Separate task progress from the immediate next action.
- Maintain a compact progress summary and action history.
- Compare current observation with the expected result.
- Replan when the observed state diverges from the plan.
- Use visual grounding only when semantic UI information is insufficient and the device supports a safe capture path.

Do not make a 7B/32B VLM a default dependency for the old phone. The model size and inference requirements are not aligned with the target hardware.

### 3.7 Maestro — repeatable UI regression tests

Maestro drives Android/iOS/web through accessibility and offers a declarative YAML flow model, assertions and smart waits. Its README explicitly emphasizes accessibility-based interaction and waiting for UI state rather than fixed sleeps.

Use the pattern to create readable test scenarios such as:

- launch probe app;
- wait until unique target appears;
- click the exact target;
- assert the intended counter changed;
- assert decoy counters remain unchanged.

Maestro itself should remain an optional external QA tool, not a dependency inside the APK.

### 3.8 Appium UiAutomator2 and OpenATX uiautomator2 — automation transport

Appium's UiAutomator2 server is a device-side Netty server executing commands via UiAutomator V2, driven by a host-side test runner. Its README says the project has moved into the appium-android monorepo.

OpenATX uiautomator2 similarly separates a device-side HTTP automation service from a Python client and supports selectors, hierarchy dumps and waits. Its documented Android support reaches back to Android 4.4.

These are excellent references for selectors, hierarchy inspection, instrumentation and regression testing. They are not appropriate as the production app's control plane because they require host/ADB or test-server setup and do not provide the user-facing in-app authority model we need.

### 3.9 LangGraph — durable checkpoint semantics

LangGraph's persistence design distinguishes:
- thread-scoped checkpoints for resumable execution, human-in-the-loop and fault tolerance;
- longer-lived stores for cross-thread facts/preferences.

A checkpoint is a snapshot of graph state after a super-step. This concept maps well to our Room journal: commit the task state and step outcome at each safe boundary, then resume from the last committed state after process death.

Do not add LangGraph itself to the APK. Reimplement the required small state machine in Kotlin/Room.

### 3.10 Google ADK for Kotlin — strongest framework candidate

Inspected source paths:

- `README.md` (version 1.5.0, module map and Android setup)
- `examples/android/README.md`
- `examples/android/build.gradle.kts`
- `core/build.gradle.kts`
- `core/src/androidMain/kotlin/com/google/adk/kt/sessions/room/RoomSessionService.kt`
- `core/src/androidMain/kotlin/com/google/adk/kt/sessions/room/AdkSessionsDatabase.kt`
- `core/src/commonMain/kotlin/com/google/adk/kt/tools/FunctionTool.kt`
- `core/src/commonMain/kotlin/com/google/adk/kt/tools/ToolContext.kt`
- `core/src/commonMain/kotlin/com/google/adk/kt/agents/ResumabilityConfig.kt`
- `litertlm/README.md`, `litertlm/build.gradle.kts`, `mlkit/build.gradle.kts`

Why it matters:

1. The Android RoomSessionService persists sessions, events and scoped app/user state in four tables and uses Room transactions for session creation and state seeding. Its documentation explicitly says sessions/events/state survive process death and reboot. This is a much stronger persistence reference than a simple append-only task journal.
2. FunctionTool supports a per-call `requiresConfirmation(args)` predicate. When approval is missing it emits a tool-confirmation request; the run can be resumed with a ToolConfirmation result. This is a useful human-in-the-loop pattern.
3. ADK supports sequential, loop and parallel agents, tool schemas, streaming events, session state, memory/artifacts and resumability.
4. The Android examples separate Room session persistence, cloud Firebase model calls, ML Kit Gemini Nano and LiteRT-LM local inference.
5. The official Android developer guide documents minSdk 24 for its published Android setup. The inspected current repository snapshot's root build defaults to minSdk 26, compileSdk 37, AGP 9.3.1 and Kotlin 2.3.21. The current destination uses AGP 8.7.3, compileSdk 35 and Kotlin 2.0.21. A direct latest-version dependency may therefore require a Gradle/SDK toolchain upgrade or version pin; test an isolated integration before changing the project baseline.
6. The ML Kit module in this snapshot sets minSdk 26, but Gemini Nano still depends on device/AICore availability. API compatibility alone does not mean the TECNO IN6 can run it.
7. The LiteRT-LM module requires JDK 21+ to build because the runtime artifacts use Java 21 class files. The sample Gemma 4 E2B model is about 2.5 GB and the example says it wants 8 GB RAM or more. This is not suitable as the baseline model for a 4 GB-class legacy phone.

Decision:

- **Run an isolated ADK core + cloud-model POC first** to evaluate structured tool calling, session events, state persistence, confirmation and streaming.
- Keep our own action schema, risk/approval policy, capability registry, snapshot freshness, Android dispatcher and independent postcondition verifier outside the LLM framework. ADK confirmation is not a substitute for an approval token bound to our exact action payload and observed device snapshot.
- Reuse ADK RoomSessionService only if it coexists cleanly with our Room task journal. Do not store the authoritative device execution journal only as conversational session state.
- Do not adopt the LiteRT-LM module until the build toolchain and physical-device model benchmark prove it can run within the target's actual RAM/storage budget.

### 3.12 MobileUse — reflection-driven long-horizon task decomposition

Inspected source paths:

- `mobile_use/agents/hierarchical_agent.py`
- `mobile_use/agents/multi_agent.py`
- `mobile_use/agents/sub_agent.py`
- README and task/trajectory schema

The hierarchical agent composes optional Planner, Operator, AnswerAgent, Reflector, TrajectoryReflector, GlobalReflector, Progressor, NoteTaker, TaskClassifier, TaskOrchestrator, TaskExtractor and TaskRewriter roles. It records an episode trajectory and supports pre-reflection, on-demand reflection and bounded action retries.

Take the *separation of cognitive responsibilities*, not the swarm architecture. Our first release should use one planner plus one executor/verifier, with a progress summary and a recovery evaluator invoked only on divergence. MobileUse controls real devices through ADB and multimodal VLMs, so its runtime is not embeddable in our APK and its model cost is too high for the legacy device.

### 3.13 Mobilerun Portal — Android accessibility and API 27 visual fallback

Inspected source paths:

- `app/src/main/java/com/mobilerun/portal/service/MobilerunAccessibilityService.kt`
- `ActionDispatcher.kt`
- `ForegroundApplicationWindowResolver.kt`
- `GestureController.kt`
- `AccessibilityScreenshotApi30.kt`
- `MediaProjectionScreenshotter.kt`
- `app/build.gradle.kts`

The inspected app uses minSdk 26, Java/Kotlin bytecode target 1.8 and a dedicated AccessibilityService. It resolves the foreground package from window descriptors and inspected root nodes, rather than assuming the first node in a flattened list is the foreground app. It provides bounded gesture APIs and an API30 screenshot adapter. For API26–29, its screenshot path uses MediaProjection, which requires the user-visible consent flow.

This is important for our device: **API27 can support a user-consented MediaProjection screenshot fallback even though AccessibilityService.takeScreenshot is unavailable.** That should remain an explicit opt-in visual observation mode, with a persistent visible capture state and no silent capture. The service's dispatchGesture path is a possible API27 gesture fallback, but we should prefer unique semantic node actions and use coordinate gestures only when the user approves the visual fallback.

Do not reuse this code directly: the repository is AGPL-3.0. Its HTTP/WebSocket servers, reverse connection, WebRTC streaming, overlay, APK auto-install and auto-accept functions materially expand the attack surface and are outside our product scope. Use only the architectural observations.

### 3.14 sherpa-onnx — API 27 speech recognition and keyword spotting

Inspected source paths:

- `android/README.md`
- `android/SherpaOnnxKws/app/build.gradle`
- `android/SherpaOnnxKws/app/src/main/AndroidManifest.xml`
- `KeywordSpotter.kt`
- `OnlineRecognizer.kt`
- `MainActivity.kt`

The Android examples declare minSdk 21, Java/Kotlin target 1.8 and use native JNI libraries. The repository lists Android support for arm32 and arm64 and includes streaming ASR, keyword spotting, VAD, TTS and related examples. The KWS example uses AudioRecord at 16 kHz mono PCM16 and explicitly avoids PCM float APIs that are newer than its minSdk.

This is the strongest inspected candidate for a separate local voice proof-of-concept on API27. The sequence should be:
1. User-started push-to-talk ASR first.
2. Measure CPU, memory, latency and transcription accuracy on English speech.
3. Add Telugu model support only after exact model availability and licence review.
4. Evaluate wake-word spotting last, with visible listening status, battery/thermal measurements and a stop control.

The Android runtime licence is Apache-2.0, but every ASR/KWS/TTS model artifact has its own licence and size. JNI/native ABI support must be checked for the exact device ABI.

### 3.15 Google AI Edge Gallery — local model catalog and function calling

Inspected source paths:

- `Android/src/app/build.gradle.kts`
- `Function_Calling_Guide.md`
- `MobileActionsTools.kt`
- `MobileActionsViewModel.kt`

The app uses typed action classes, annotated function tools and a callback to route a recognized function into Android-side code. Its guide cleanly separates the tool schema from the app's actual Android action implementation. This is a useful architecture boundary.

Two cautions:
- The inspected app sets minSdk 31, so it cannot be our API27 runtime.
- Its sample tool functions return a generic success response after invoking the action callback. Our design must not copy that success semantics: the callback may only queue/dispatch work. The model should receive the actual result after the action executor independently verifies the postcondition.

### 3.16 llama.cpp Android sample — native inference reference

Inspected source paths:

- `README.md`
- `examples/llama.android/app/build.gradle.kts`
- `examples/llama.android/app/src/main/java/com/example/llama/MainActivity.kt`

llama.cpp is a portable C/C++ inference runtime with an MIT root licence. The current Android sample uses minSdk 33, compileSdk 36 and Java 17, so the sample is not API27 compatible as shipped. A custom NDK build may have different compatibility, but that must be proven with the exact Android NDK, ABI and runtime path. Do not infer API27 support from the cross-platform C++ core alone.

### 3.17 LiteRT samples — model lifecycle and tool calling

The inspected Google offline voice assistant sample uses a recognizer, a tool-calling LLM and TTS. It declares tools, executes them in app code, sends tool results back to the model and checks Android's own state after setting an alarm. The model manager uses catalog metadata, free-space preflight, HTTPS, resumable download and SHA-256 verification.

Important compatibility finding: the inspected sample reports **minSdk 31 / Android 12+**, and its sample model set totals about 2.81 GB. That sample cannot be used as the API 27 implementation. It is useful as a modern-device optional inference path and as a model-download integrity reference.

For our legacy target, the inference adapter must be independently selected and benchmarked. Do not assume LiteRT-LM, ML Kit GenAI, llama.cpp, Vulkan or a specific model supports API 27 just because it works on a recent flagship.

## 4. Android platform facts that materially affect the architecture

### 4.1 API 27 visual observation

Android's AccessibilityService `takeScreenshot()` was added in API 30. The global screenshot action is API 28. Therefore, neither can be used on Android 8.1/API 27.

On API 27, baseline observation should be:

- AccessibilityNodeInfo tree;
- package/window metadata available through Accessibility APIs;
- event-driven invalidation and on-demand fresh snapshots;
- native Android APIs for deterministic tasks;
- optional MediaProjection only after a user-visible consent flow.

Do not silently capture the screen through privileged screen-buffer permissions. Android's restricted screen-reading documentation explains the security change and MediaProjection consent path.

### 4.2 Accessibility is not omnipotent

The service must be enabled by the user. Apps may expose incomplete or misleading node trees; secure windows and custom-rendered surfaces may not provide actionable semantics. Node handles are short-lived and must not be treated as stable identifiers across transitions. Every action must re-ground against a fresh active window and fail closed on ambiguity.

### 4.3 Background execution

Android 8.0/API 26 introduced background service and broadcast restrictions. WorkManager/JobScheduler are for deferrable work, not continuous real-time UI control. An agent task that is actively interacting with the phone needs a visible user-initiated lifecycle and a durable journal. OEM battery optimization, process death, force-stop and reboot must be tested separately.

### 4.4 Distribution policy

Google Play policy restricts general-purpose autonomous AccessibilityService automation. The current product is personal sideloaded use, so Play publication is not the immediate product constraint. If distribution goals change, reassess policy; do not misdeclare the app as an accessibility tool.

## 5. Recommended destination architecture

The following architecture is the synthesis of the inspected projects, adapted to our API 27 and local-first constraints.

```text
User text / explicit voice input
             |
             v
     Session & Goal Intake
             |
             v
     Planner (LLM adapter)
             |
             v
  Plan Normalizer + Validator
  - typed action schema
  - dependencies / DAG
  - capability check
  - risk classification
  - bounded plan size
             |
             v
     Policy / Approval Gate
  - deny by default
  - action-bound approval
  - expiry + snapshot binding
  - cancellation / stop
             |
             v
  Durable Execution Coordinator
  - one active task owner
  - Room state machine
  - step journal / idempotency
  - bounded retry / timeout
             |
             v
        Device Observer
  - Accessibility tree
  - active package/window
  - semantic node metadata
  - optional consented visual input
             |
             v
        Action Dispatcher
  - direct Android API first
  - narrow Accessibility action
  - capability/permission guard
             |
             v
     Fresh Observer + Verifier
  - expected postcondition
  - actual state evidence
  - success / failed / uncertain
             |
             +----> bounded recovery/replan
             |
             v
     User-visible result + audit
```

### Required task state machine

Use explicit durable states. A suggested minimum:

- `CREATED`
- `PLANNING`
- `PLAN_READY`
- `WAITING_FOR_APPROVAL`
- `APPROVED`
- `EXECUTING`
- `ACTION_DISPATCHED`
- `VERIFYING`
- `SUCCEEDED`
- `FAILED`
- `CANCELLED`
- `UNCERTAIN`
- `RECOVERY_REQUIRED`

The critical rule is: **never blindly replay a non-idempotent action after process death.** If the journal says dispatch may have happened but the postcondition was not recorded, observe the device and reconcile first. If the outcome cannot be established, ask the user rather than retrying.

### Device action contract

Every action should carry:

- stable action ID and schema version;
- typed arguments with strict size/range validation;
- required capabilities and Android permissions;
- risk class and whether explicit approval is mandatory;
- target package and target locator;
- originating snapshot ID and snapshot timestamp;
- timeout and cancellation behavior;
- idempotency classification;
- precondition and expected postcondition;
- result evidence and outcome certainty.

### UI locator contract

Prefer, in order:

1. unique resource/view ID within the expected package;
2. unique exact text/content-description plus class/role and parent context;
3. label-to-editable-field relationship;
4. bounded geometry only as a last-resort, explicit visual fallback.

Reject multiple matches. Do not use a substring match and click the first item. Re-ground before every action and verify the actual target, not merely that a click API returned true.

### Planner policy

The model proposes a structured plan; it does not authorize it. Normalize and validate the plan before showing it. Keep the plan bounded and sequential at first. Add DAG parallelism only for independent read-only work. After each state-changing action, re-observe and verify. Replan only with the new observed state and failure classification.

## 6. Gaps in the current destination implementation

The current Kotlin/JVM core and Android app adapter are a useful first tranche, not a finished autonomous agent. Based on the current source tranche:

1. **Durable journal is append-only and incomplete.** The current Room adapter writes task-step rows, but does not yet provide a complete task/step state repository, transactional transitions, recovery query or in-flight reconciliation.
2. **WorkManager is only a due-signal boundary.** A worker broadcast is not a durable execution handoff unless a receiver/queue and persisted job state consume it. It must not report task success merely because the signal was emitted.
3. **No complete task-entry/approval lifecycle.** The approval dialog provider exists, but it must be wired to a visible task flow and must show the normalized action, target, risk and data effect.
4. **Observer target identity needs strengthening.** Snapshot foreground package should come from the root/window metadata, not assume the first emitted node represents the foreground package. Node IDs should be paired with snapshot version and freshness.
5. **No robust UI matching ambiguity model.** Require unique target resolution and exact package binding before any mutation.
6. **No hardware adapters.** Torch, vibration, Wi-Fi, Bluetooth, camera and sensor controls need per-capability availability/permission/runtime detection and independent verification.
7. **No full recovery controller.** Process-death, service disconnection, target-app switch, timeouts and uncertain dispatch need explicit state transitions.
8. **No Room/Keystore instrumentation tests.** The three current Android unit tests exercise the verifier only. Add database migration, encryption round-trip/tamper, crash recovery and service harness tests.
9. **No physical API 27 proof.** CI APK assembly does not establish OEM accessibility behavior, memory pressure, battery behavior or model runtime support on the TECNO device.
10. **No measured model choice.** Do not select a model/runtime from parameter count or marketing RAM. Measure real peak PSS/RSS, free-memory headroom, cold load, first token, tokens/sec, KV-cache, thermal behavior and tool-call validity on the exact device.

## 7. Prioritized build plan derived from the research

### Gate A — prove the Android actuator independently

- Build an owned accessibility probe app with unique controls and decoys.
- Test observe, exact unique click, parent click, editable field set-text, scroll, back and app launch.
- Add service reconnect, app switch, stale snapshot, duplicate text, secure window and empty tree cases.
- Test on API 27 physical device before building a broad tool catalog.

### Gate B — make execution durable and safe

- Replace append-only journal with Room task/step state tables and transactional transitions.
- Persist plan version, action intent, approval receipt, snapshot ID, idempotency classification and result evidence.
- Implement recovery reconciliation before retry.
- Wire a real user task UI, approval, cancellation and global stop.
- Ensure unknown tools, missing capabilities, stale snapshots and ambiguous targets fail closed.

### Gate C — complete deterministic Android capabilities

- Implement direct APIs for supported tasks first (alarms/timers, app launch, supported settings intents, vibration/torch only where permitted).
- Add accessibility as a semantic generic UI fallback.
- Keep every adapter behind typed contracts and verify postconditions independently.
- Add hardware inventory and runtime capability refresh.

### Gate D — integrate model and voice

- Keep planner/provider interfaces replaceable.
- Evaluate cloud and local model paths separately.
- Start with text task input; add user-started speech capture and STT after API 27 recording/transcription behavior is measured.
- Model manager must support opt-in download, resumable transfer, checksum verification, free-space preflight, compatibility checks, activation rollback and safe unload.
- Treat API 27 model support as a separate proof, not a consequence of compiling the APK.

### Gate E — memory, skills and scheduling

- Add session memory, user-approved long-term memory and full-text retrieval.
- Skills are declarative, typed and capability-scoped; no downloaded executable scripts.
- Scheduler persists job definitions and due state; every scheduled action rechecks capability and approval at execution time.
- Add trajectory export with redaction and retention controls.

## 8. Validation and acceptance metrics

Every task scenario should record:

- task completion rate;
- correct-target action rate;
- false-action rate (must be zero in the owned probe corpus);
- postcondition verification rate;
- ambiguous-target rejection rate;
- recovery success after injected failures;
- no blind duplicate side effects after process death;
- median and p95 action latency;
- memory peak and memory-pressure behavior;
- battery/thermal impact for long tasks;
- user approval/denial and cancellation correctness.

CI should cover JVM unit tests, Android unit tests, lint, schema/migration checks and debug APK assembly. Physical API 27 testing should be a separately reported acceptance gate, not disguised as CI or emulator evidence.

## 9. Licensing and reuse posture

The repository licenses observed for the inspected set are MIT or Apache-2.0. That is encouraging but not blanket permission to copy every dependency or asset. Before direct code transplantation:

1. pin the exact commit and archive its LICENSE;
2. inspect source-file copyright headers and provenance;
3. audit transitive dependencies and bundled native code;
4. audit model weights/tokenizers/datasets independently;
5. preserve MIT notices and Apache LICENSE/NOTICE/patent terms;
6. record each copied file and modification in a donor ledger.

Prefer new Kotlin implementations for Android-specific execution and safety policy. Use external source code as a reference until exact file-level reuse is justified.

## 10. Source links and official platform references

- OpenDroid architecture: https://github.com/JMAN730/opendroid/blob/7c5dcd40f465fd8affe6c227c332fba9af80a600/docs/architecture.md
- OpenDroid AccessibilityNodeTraversal: https://github.com/JMAN730/opendroid/blob/7c5dcd40f465fd8affe6c227c332fba9af80a600/app/src/main/java/com/opendroid/ai/accessibility/AccessibilityNodeTraversal.kt
- OpenDroid PlanManager: https://github.com/JMAN730/opendroid/blob/7c5dcd40f465fd8affe6c227c332fba9af80a600/app/src/main/java/com/opendroid/ai/core/agent/PlanManager.kt
- OpenDroid click targeting instrumentation test: https://github.com/JMAN730/opendroid/blob/7c5dcd40f465fd8affe6c227c332fba9af80a600/app/src/androidTest/java/com/opendroid/ai/accessibility/ClickTargetingAccuracyInstrumentationTest.kt
- Mobilerun Manager: https://github.com/droidrun/mobilerun/blob/11dcdebdec9ea99f172dd7e80af7ba1eadbeb6c8/mobilerun/agent/manager/manager_agent.py
- Mobilerun Executor: https://github.com/droidrun/mobilerun/blob/11dcdebdec9ea99f172dd7e80af7ba1eadbeb6c8/mobilerun/agent/executor/executor_agent.py
- MobileUse: https://github.com/MadeAgents/mobile-use/tree/babec07fd0e5faa7e7bcc7d3d0ee2320f6b83347
- Mobilerun Portal AccessibilityService: https://github.com/droidrun/mobilerun-portal/blob/00a77c01e2ee4fcc71889a3992bc1753d64e9ed9/app/src/main/java/com/mobilerun/portal/service/MobilerunAccessibilityService.kt
- sherpa-onnx Android KWS: https://github.com/k2-fsa/sherpa-onnx/blob/935e5ded3d949a8b557ac44ef4dad03a7d2038e7/android/SherpaOnnxKws/app/src/main/java/com/k2fsa/sherpa/onnx/KeywordSpotter.kt
- Google AI Edge Gallery: https://github.com/google-ai-edge/gallery/tree/6d09e2ea0823b0868800ef3b5752852715b58b91
- llama.cpp Android sample: https://github.com/ggml-org/llama.cpp/tree/71efc559a00b236851f6ba00a629d776f2416d2b/examples/llama.android
- Google ADK Kotlin: https://github.com/google/adk-kotlin/tree/b961c9e4aa50c79dbf4c17d870ab5629424a5e49
- ADK RoomSessionService: https://github.com/google/adk-kotlin/blob/b961c9e4aa50c79dbf4c17d870ab5629424a5e49/core/src/androidMain/kotlin/com/google/adk/kt/sessions/room/RoomSessionService.kt
- Llamatik AgentExecutionEngine: https://github.com/llamatik/Llamatik/blob/10f3a99d5f74d727c43b3e1475b8931403e592d2/sdk-agent/src/commonMain/kotlin/com/llamatik/sdk/agent/runtime/AgentExecutionEngine.kt
- Hermes agent loop: https://github.com/NousResearch/hermes-agent/blob/c314285f077e91937c995d419b9fa40af9eb4981/agent/conversation_loop.py
- AndroidWorld agents: https://github.com/google-research/android_world/tree/55ddaf5edc6f6c25673aed343907d5b9dca66f84/android_world/agents
- Mobile-Agent: https://github.com/X-PLUG/MobileAgent/tree/c4cd0a8f9ddedad24dd8a3daf3a56ca69343849f
- Maestro: https://github.com/mobile-dev-inc/Maestro/tree/b80bd24a42892eb75440bb8e9d8bee24f1292cc0
- Appium UiAutomator2 server: https://github.com/appium/appium-uiautomator2-server/tree/56bf27a222b9aa242e6a40cf87825b78b443b7be
- OpenATX uiautomator2: https://github.com/openatx/uiautomator2/tree/5d373ad83a94d85ebfd088cacb58d06ac40a00d2
- LangGraph persistence: https://github.com/langchain-ai/langgraph/blob/3c916d750575b22c464790e9774494d810ccd322/libs/checkpoint/README.md
- LiteRT offline voice assistant: https://github.com/google-ai-edge/litert-samples/tree/e98147beea80ee90f7f74ed975bca9c78667d87c/samples/litert_lm/voice_assistant
- Android AccessibilityService API: https://developer.android.com/reference/android/accessibilityservice/AccessibilityService
- Android 8 background limits: https://developer.android.com/about/versions/oreo/background
- Android restricted screen reading: https://source.android.com/docs/core/permissions/restricted-screen-reading
- Google Play AccessibilityService policy: https://support.google.com/googleplay/android-developer/answer/10964491?hl=en

---

**Bottom line:** The next engineering milestone is not “add more AI.” It is a deterministic, crash-safe Android execution engine with unique-target interaction, independent verification, explicit authority and API 27 physical-device evidence. Once that foundation is reliable, the planner and model can become more capable without making the phone less predictable.
