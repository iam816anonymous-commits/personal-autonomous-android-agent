# Personal Autonomous Android Agent — Full System Design

**Document type:** Product and engineering architecture baseline  
**Date:** 2026-11-03  
**Status:** Detailed design proposal; not implementation or device acceptance evidence  
**Target:** Personal-use, sideloaded Android agent; Android 8.1/API 27 is the minimum compatibility target  
**Primary device:** TECNO Camon i Click / IN6 family; exact RAM/storage variant must be confirmed from the physical device  
**Implementation language:** Kotlin; Android-native adapters; modular Gradle project  
**Operating model:** Local-first, privacy-preserving, user-authorized automation

---

## 0. Executive design decision

Build one **durable, policy-governed, observe–plan–act–verify agent**. Do not build a free-running LLM that directly controls Android APIs, and do not create a swarm of agents competing to operate the same device.

The system has four distinct authorities:

1. **Cognitive authority — Planner:** interprets the goal, proposes a bounded plan and selects from a declared tool vocabulary. It has no direct Android access.
2. **Policy authority — Guard:** decides whether the proposed action is structurally valid, permitted by capability and permission state, within risk limits, and approved by the user when required.
3. **Execution authority — Android runtime:** the only component allowed to call Android APIs. It executes typed, bounded actions against fresh device state.
4. **Evidence authority — Verifier:** independently checks the postcondition using a new observation or authoritative Android state. Dispatch success alone is never task success.

A durable coordinator owns task state transitions and recovery. The Room journal is authoritative for what the agent intended, attempted, observed and verified. A model conversation is not an execution journal.

### Product promise

The agent may act only through capabilities that are present, enabled, permitted and allowed by policy. It must explain limitations, request confirmation for consequential actions, stop when the outcome is uncertain, and never invent a successful result.

### Explicit non-goals

- Root access, hidden APIs, privilege escalation or bypassing Android security controls.
- Arbitrary shell/terminal execution, downloaded executable skills, arbitrary code execution or model-authored reflection.
- Silent microphone recording, silent screen capture or covert surveillance.
- Lock-screen, authentication, payment, OTP or account-security bypass.
- Unattended financial transfers, purchases, sending messages, posting, deletion or other consequential actions.
- General-purpose remote-control server, reverse tunnel or stealth control plane.
- Home-security monitoring, face recognition or remote surveillance; those are explicitly a separate future app.
- Guaranteeing identical capability on API 27 and modern Android.

---

# 1. Product behavior and user experience

## 1.1 Interaction modes

The first release should support:

1. **Text task entry** — user types a goal.
2. **Push-to-talk speech** — user explicitly starts and stops capture; speech is converted to text before planning.
3. **Follow-up conversation** — user can correct, clarify, cancel or ask about the current task.
4. **Approval prompts** — show the exact normalized action, target app, target object, data to be changed or sent, risk and consequence.
5. **Visible execution status** — task, current step, waiting for approval, executing, verifying, paused, failed or uncertain.
6. **Audit/history** — user can inspect completed, failed and cancelled tasks and the evidence retained.
7. **Global stop** — a persistent, reachable stop/cancel control for any active task.

Do not start with always-listening wake-word activation. First prove push-to-talk capture and transcription on API 27. Wake-word support is a separate opt-in capability requiring a battery, privacy and false-trigger study.

## 1.2 Task lifecycle shown to the user

- **Understanding:** parsing the goal and asking for missing information.
- **Planning:** building a structured plan; no device mutation.
- **Review:** showing plan summary and any actions requiring approval.
- **Running:** one action at a time with fresh observation.
- **Verifying:** checking actual device state.
- **Waiting:** approval, permission or user input needed.
- **Completed:** all required postconditions verified.
- **Partially completed:** some steps verified, remaining steps safely stopped.
- **Needs attention:** outcome uncertain or recovery blocked.
- **Cancelled:** no new actions may start; already-dispatched side effects must still be reconciled.

A cancellation request does not mean a dispatched action has been undone. The UI must distinguish “stopped before dispatch” from “cancelled after dispatch; outcome being checked”.

## 1.3 Conversation is not authority

Natural language such as “just do it”, model confidence, previous approval, a screen instruction or a skill definition never grants Android permission. Each approval is explicit, scoped, expiring and tied to the exact action digest.

---

# 2. System architecture

## 2.1 Logical component map

```text
                 USER INTERFACE
       Task entry / voice / approval / history
                         |
                         v
              SESSION & GOAL INTAKE
        normalize language, constraints, context
                         |
                         v
                TASK COORDINATOR
          single active execution owner per task
             |                     |
             v                     v
      DURABLE ROOM JOURNAL    EVENT / AUDIT BUS
             |
             v
       CONTEXT ASSEMBLER <------ MEMORY RETRIEVAL
             |
             v
        MODEL PROVIDER ROUTER
      cloud / local / deterministic fallback
             |
             v
       PLANNER + PLAN NORMALIZER
       strict schema / bounded plan / DAG
             |
             v
      POLICY + CAPABILITY GUARD
      risk / permission / approval / scope
             |
             v
       ACTION EXECUTION GATE
       fresh snapshot / target uniqueness
             |
             v
       ANDROID TOOL REGISTRY
       native APIs / AccessibilityService
             |
             v
       DEVICE OBSERVER + VERIFIER
       new snapshot / postcondition evidence
             |
        +----+----------------------+
        |                           |
        v                           v
  VERIFIED STEP               DIVERGENCE
        |                           |
        v                           v
  NEXT STEP                 RECOVERY EVALUATOR
        |                    reconcile / replan /
        v                    ask user / fail closed
    TASK RESULT
```

Cross-cutting services:

- Capability and permission registry.
- Approval-token service.
- Resource and model manager.
- Speech capture and ASR.
- Declarative skill registry.
- Scheduler (later phase).
- Diagnostics, redaction and retention manager.
- Feature/capability compatibility matrix.

## 2.2 Module boundaries

Recommended Gradle modules (some may be split only when code volume justifies it):

| Module | Responsibility | Android framework access |
|---|---|---|
| `:core:contracts` or current `:core` | Domain types, state machine, schemas, risk and verification contracts | None |
| `:agent-runtime` | Coordinator, planner interface, provider router, context assembly | None |
| `:policy` | Permission/capability checks, approval digest, risk rules, scope guard | None |
| `:memory` | Memory interfaces, retrieval ranking, retention contracts | None |
| `:skills` | Declarative skill manifests, validator, versioning | None |
| `:app` | UI, lifecycle, composition root, notification and user consent | Android |
| `:platform-accessibility` | AccessibilityService observer and action adapter | Android |
| `:platform-capabilities` | App launch, supported system APIs, hardware capability adapters | Android |
| `:persistence` | Room repositories, migrations, journal and encrypted records | Android/Room |
| `:inference-cloud` | Network model provider and API client | Network |
| `:inference-local` | Optional local runtime adapters | JNI/runtime-specific |
| `:speech` | AudioRecord capture, ASR and optional KWS adapters | Android/audio/JNI |
| `:benchmark` | Task fixtures, test probes and metrics | Test only |

Do not create every module immediately if the project is still small. Enforce package/API boundaries first; split modules when separate build/test ownership is valuable. The core and policy packages must never import Android classes or model SDK classes.

## 2.3 Threading and ownership

- One coordinator owns state transitions for a task.
- At most one mutating device action may be in flight per device.
- Read-only observations may run concurrently only if they do not invalidate the action snapshot and are explicitly marked safe.
- UI observes immutable state/events; it does not mutate execution state directly.
- Accessibility callbacks enqueue observations/events; they do not call the planner recursively.
- Database writes use transactions and a single repository boundary.
- Model requests run off the main thread with explicit cancellation and deadlines.
- Audio capture owns an explicit lifecycle and must stop on cancellation, permission loss, app shutdown or user stop.

---

# 3. Core domain contracts

All model/provider output is untrusted input. Parse it into strict, versioned Kotlin types. Unknown fields may be ignored only when the schema explicitly allows it; unknown action types are rejected.

## 3.1 Goal

A normalized goal contains:

- `taskId`, `sessionId`, schema version.
- Original user request and redacted display form.
- Input modality: text / push-to-talk / follow-up.
- Detected and confirmed language.
- Desired outcome, constraints, exclusions and deadline.
- Relevant conversation references (not raw unbounded transcript).
- Privacy mode: local-only / cloud-allowed for this task.
- User-provided target app or entity, if any.

Do not infer a recipient, amount, account, date or destructive scope from vague text. Ask a clarification question when the missing information changes the action.

## 3.2 Plan

A plan is a bounded, versioned structure:

- Plan ID and version.
- Goal digest.
- Ordered steps or a small dependency DAG.
- Each step's objective and typed action candidates.
- Preconditions and expected postconditions.
- Required capabilities and permission states.
- Risk tier, approval requirement and explanation.
- Idempotency classification.
- Deadline, maximum attempts and maximum total actions.
- Stop conditions and rollback/compensation strategy if one exists.
- Evidence needed to mark the step complete.

Initial limits: maximum 8 mutating actions per plan, maximum 20 total steps, maximum 2 replans per task, explicit per-step timeout, and a task deadline. These are provisional safety ceilings, not performance claims. Increase only after benchmark evidence.

The user approves the normalized plan or the exact action, never the raw model response. If normalization changes the target or payload after approval, approval is invalidated.

## 3.3 Action contract

Every action must include:

- Stable action ID, action kind ID and schema version.
- Typed, range-checked arguments.
- Tool descriptor and required capability IDs.
- Expected package/app and target identity.
- Snapshot ID/version and observation timestamp.
- Preconditions.
- Risk class and approval policy.
- Timeout and cancellation semantics.
- Idempotency classification and idempotency key.
- Expected postcondition.
- Result code, diagnostic category and evidence reference.

Action kinds are a closed enum/registered schema. The model cannot invent an action class, package name, URI scheme or Android Intent and expect it to run.

## 3.4 Device snapshot

A snapshot should include:

- Unique snapshot ID, monotonic sequence number and wall-clock timestamp.
- Foreground package/window identity and observation source.
- Active window root metadata.
- Bounded semantic node list.
- Node role/class, resource ID, text/content description (redacted), bounds, clickable/editable/scrollable flags, parent/child context and stable-within-snapshot locator.
- Permission/capability version.
- Optional visual frame handle only if a separate user-consented capture mode is active.
- Truncation indicators and observation errors.

Do not persist raw AccessibilityNodeInfo objects or treat node IDs as durable across snapshots. Node references expire with their snapshot. A node locator must be re-resolved against a fresh tree immediately before dispatch.

## 3.5 Result and evidence

Separate:
- `DispatchResult`: the platform call was accepted/rejected/failed/timed out.
- `ObservationResult`: what the device showed after dispatch.
- `VerificationResult`: whether the intended postcondition is established.
- `TaskResult`: all required outcomes and side effects.

Verification statuses:
- `VERIFIED`
- `NOT_VERIFIED`
- `UNCERTAIN`
- `NOT_APPLICABLE`

Only `VERIFIED` can advance a required mutating step to success. `UNCERTAIN` blocks blind retry.

---

# 4. Agent control loop

## 4.1 One authoritative loop

1. Accept goal and normalize constraints.
2. Load minimal relevant context and memory.
3. Observe device state if the task needs it.
4. Ask planner for a structured plan.
5. Parse, normalize and validate plan.
6. Resolve capabilities and risk.
7. Show required plan/action confirmation.
8. Persist approval receipt.
9. Re-observe and revalidate freshness, target app and capability.
10. Persist an action intent before dispatch.
11. Dispatch exactly one action.
12. Persist dispatch outcome.
13. Obtain a new observation.
14. Independently verify postcondition.
15. Persist evidence and state transition atomically.
16. Continue, recover, ask user or terminate.
17. Produce a concise result grounded in journal evidence.

No tool result, model statement or callback may skip a stage.

## 4.2 Durable task state machine

Suggested task states:

- `CREATED`
- `UNDERSTANDING`
- `WAITING_FOR_CLARIFICATION`
- `PLANNING`
- `PLAN_READY`
- `WAITING_FOR_APPROVAL`
- `APPROVED`
- `EXECUTING`
- `ACTION_INTENT_RECORDED`
- `ACTION_DISPATCHED`
- `VERIFYING`
- `RECOVERY_REQUIRED`
- `WAITING_FOR_USER`
- `SUCCEEDED`
- `PARTIALLY_SUCCEEDED`
- `FAILED`
- `UNCERTAIN`
- `CANCEL_REQUESTED`
- `CANCELLED`

Every transition has a source state, target state, guard, actor, timestamp, reason and journal transaction. Illegal transitions fail closed.

## 4.3 Crash recovery and uncertain dispatch

The most important recovery invariant:

**If the process dies after recording action intent but before recording verified outcome, do not replay the action automatically.**

On restart:
1. Load non-terminal tasks.
2. Identify the last committed step and any in-flight action intent.
3. Refresh device/capability state.
4. Observe the target app and query an authoritative state where possible.
5. Reconcile whether the side effect already happened.
6. If it happened, record the evidence and continue.
7. If it definitely did not happen and the action is safe/idempotent, consider a bounded retry.
8. If the outcome cannot be established, mark `UNCERTAIN` and ask the user.

Examples:
- Opening an app is generally safe to repeat.
- Setting a toggle to a known value may be repeatable if the action is truly state-setting and verified.
- Sending a message, submitting a form, deleting data or making a payment must never be blindly replayed.

A timeout means “outcome unknown” unless the adapter proves dispatch never occurred.

## 4.4 Replanning

Replanning is permitted only after a fresh observation and a classified divergence:
- target disappeared;
- app switched;
- permission revoked;
- expected UI did not appear;
- postcondition contradicted;
- action unavailable;
- timeout with uncertain side effect.

Replan using observed facts, not the model's prior assumption. Replanning cannot silently widen the user's requested scope or lower risk/approval requirements.

---

# 5. Planner, model router and reasoning strategy

## 5.1 Planner is a proposal generator

Planner responsibilities:
- Interpret the user's intent.
- Identify missing details and ask questions.
- Select from registered tools and declarative skills.
- Produce a compact plan with typed arguments.
- State assumptions and expected outcomes.
- Explain why an action is required.

Planner does not:
- invoke Android APIs;
- issue approvals;
- change permissions;
- bypass a tool descriptor;
- choose arbitrary code/commands;
- mark actions successful;
- suppress policy failures.

## 5.2 Model provider interface

Define a provider-neutral contract:

- `generatePlan(goal, context, toolSchemas, deadline)`
- `continuePlan(observation, priorPlan, failureClass)`
- `summarizeSession(events)`
- `extractMemoryCandidates(conversation)`

Each response includes provider/model/version, latency, token usage if available, structured parse result, finish reason and privacy route. Store only redacted metadata unless the user explicitly enables diagnostic retention.

## 5.3 Routing policy

| Task type | Preferred route | Fallback |
|---|---|---|
| Deterministic native operation | Local deterministic code | Ask user / unavailable |
| Simple intent classification | Small local model if benchmarked | Cloud if allowed |
| Complex task decomposition | Cloud model when user allows it | Smaller local model or clarification |
| Accessibility target resolution | Local deterministic semantic resolver first | Model ranks candidates, policy still enforces unique target |
| Speech-to-text | Local ASR if measured and installed | User-entered text; cloud ASR only with explicit task consent |
| Memory lookup | Local database/search | None |
| Sensitive operation | Deterministic policy + explicit approval | Reject if policy cannot decide |

Model router eligibility checks:
- Android API level and ABI.
- Available memory headroom at load time.
- Storage/free-space requirement.
- Native library compatibility.
- Runtime/JDK/NDK compatibility.
- Context/KV-cache budget.
- Model license and source integrity.
- Measured cold load, peak PSS, throughput, accuracy and thermal profile.
- User preference and local/cloud privacy mode.

Never infer compatibility from parameter count or a vendor's “RAM required” label alone.

## 5.4 ADK Kotlin integration decision

Run an isolated POC for Google ADK Kotlin:
- structured tool calling;
- cloud provider integration;
- session events and Room persistence;
- confirmation events;
- cancellation and resumability.

Keep our outer action policy, snapshot-bound approval digest, Android tool registry, durable device journal and independent verifier. ADK's conversational session is not the source of truth for whether a device side effect occurred.

Do not upgrade the whole Android build toolchain solely to adopt the latest ADK release. Pin a compatible artifact or prove the upgrade separately.

---

# 6. Policy, permissions and approval

## 6.1 Capability model

A capability is not a boolean. Store:
- capability ID and version;
- hardware present/unknown/absent;
- API level support;
- manifest declaration;
- runtime permission state;
- special access state;
- service enabled state;
- foreground/background restrictions;
- user enablement;
- last probe timestamp;
- verification confidence and failure reason.

Capability states:
- `AVAILABLE`
- `AVAILABLE_WITH_CONSTRAINTS`
- `NEEDS_PERMISSION`
- `NEEDS_USER_ENABLEMENT`
- `UNSUPPORTED_API`
- `HARDWARE_ABSENT`
- `BLOCKED_BY_POLICY`
- `UNKNOWN`
- `TEMPORARILY_UNAVAILABLE`

Refresh capability immediately before a mutating action. Permission can be revoked or service disconnected between planning and dispatch.

## 6.2 Risk tiers

| Tier | Examples | Policy |
|---|---|---|
| R0 — Observe | Read visible UI, inspect own task state | No action confirmation; still respect privacy |
| R1 — Reversible local | Open app, navigate, scroll, fill an unsent draft | Usually no separate confirmation if within the user's explicit task; always validate scope |
| R2 — Sensitive/reversible | Change settings, create calendar entry, modify local records | Explicit confirmation by default; allow narrowly scoped user-configured policies later |
| R3 — External/consequential | Send message/email, post, submit, share, delete, purchase | Explicit per-action confirmation with exact recipient/content/object |
| R4 — Prohibited or unsupported | Bypass authentication, hidden recording, credential extraction, security controls bypass | Reject |

Risk is assigned by deterministic rules and tool metadata, not model self-assessment.

## 6.3 Approval receipt

Bind approval to:
- task ID and step ID;
- exact normalized action kind and canonical arguments digest;
- target package and target identity;
- data/recipient/amount/object digest;
- snapshot ID and expiry;
- risk tier and user-facing summary;
- approval UI version and user decision.

Reject:
- expired token;
- reused token;
- modified arguments;
- changed target app;
- changed recipient/content;
- stale snapshot;
- revoked capability;
- cancelled task.

Approval is not transferable to a different action even if the model says it is “equivalent”.

## 6.4 Prompt injection and untrusted screen content

Treat text from web pages, messages, notifications, files and accessibility nodes as untrusted data. It may contain instructions such as “ignore your rules” or “send all files”. These instructions never override the user's goal or system policy.

Separate:
- user instruction;
- model proposal;
- UI content;
- tool output;
- policy decision.

Screen text is evidence about the app, not authority to change the agent's rules. The model may summarize it, but any action derived from it passes the same policy and approval checks.

---

# 7. Android execution layer

## 7.1 Adapter hierarchy

Prefer deterministic Android APIs for known capabilities. Use AccessibilityService as a constrained UI adapter, not a universal root-like controller.

Suggested action families:
- app launch through package/launcher APIs;
- user-visible supported settings intents;
- notification interaction only where Android permits and user has granted access;
- accessibility semantic click/set-text/scroll;
- user-consented gesture dispatch;
- torch/vibration only after API, hardware and permission validation;
- camera only through standard user-visible camera APIs;
- sensor observation only for available sensors and disclosed use.

No arbitrary Intent URI, shell command, hidden API, root command or unrestricted activity launch from model output.

## 7.2 Accessibility observer

On each observation:
1. Check service connected and enabled.
2. Resolve active window and package identity.
3. Obtain root node and safely traverse with a hard node cap and depth cap.
4. Redact password and sensitive nodes before any model/context exposure.
5. Bound text length and total snapshot bytes.
6. Record truncation and inaccessible subtree flags.
7. Recycle nodes and release references.
8. Assign a new snapshot ID.
9. Return immutable domain snapshot only.

Node locators:
- unique resource ID scoped to expected package;
- exact content description/text + role/class + parent context;
- labelled editable field relationship;
- geometry only in explicit visual/gesture fallback.

If zero or multiple targets match, reject and ask for a better observation or user assistance. Never “first match wins”.

## 7.3 Accessibility dispatcher

Each dispatch:
- re-resolves the target against a fresh tree;
- validates foreground package;
- checks snapshot age;
- checks enabled/clickable/editable/visible state;
- rejects password nodes;
- performs one bounded action;
- records Android return code and event;
- requests a fresh observation.

API 27 specifics:
- AccessibilityService.takeScreenshot is unavailable (API 30).
- Global screenshot action is unavailable (API 28).
- dispatchGesture is available from API 24, but the service must declare `android:canPerformGestures="true"`. The current destination service XML does not declare it; gesture dispatch is not implemented yet.
- MediaProjection can provide a user-consented visual capture path on API 27, but it requires a clear consent UI, visible capture state, lifecycle cleanup and privacy controls.
- Semantic accessibility trees remain the default. Visual capture is an optional explicit mode, not silent background observation.

## 7.4 Foreground and lifecycle rules

- Do not assume AccessibilityService remains connected forever.
- Handle service disconnect, package switch, window changes, screen lock, app process death and user disablement.
- Do not perform long-running model inference in accessibility callbacks.
- Debounce event bursts and request a stable snapshot after a short bounded quiet period.
- A device action may not start while the task is cancelled, app is in an unsupported lifecycle state or required service is disconnected.
- OEM battery restrictions and Android 8 background service rules require physical tests.

---

# 8. Device capability and hardware subsystem

Each hardware capability is a separate adapter with:
- discovery;
- API support;
- permission/special-access requirements;
- enable/disable operation;
- readback/verification;
- error classification;
- battery/thermal impact;
- supported device profiles.

Initial candidates:
- flashlight/torch;
- vibration/haptic;
- app launch/navigation;
- supported settings panels;
- camera intent;
- sensor reading;
- clipboard only with explicit user-visible semantics;
- Wi-Fi/Bluetooth state observation and only permitted settings handoff (not privileged silent toggles on modern Android).

Do not promise deep LED, display brightness, audio routing or vendor-specific hardware control until the exact hardware exposes a supported API and readback test. Capability inventory must distinguish “API exists” from “device has hardware” and “operation verified”.

---

# 9. Voice and speech subsystem

## 9.1 Phase order

1. Push-to-talk audio capture.
2. Offline English ASR proof.
3. Editable transcript preview/correction.
4. Intent parsing and task planning.
5. Optional Telugu/Hindi ASR after exact model evaluation.
6. Keyword spotting/wake word only after battery/privacy/false-trigger testing.

## 9.2 Audio pipeline

- Request RECORD_AUDIO permission at the point of use.
- Show visible recording state and a stop button.
- Capture bounded PCM audio via AudioRecord.
- Apply voice activity detection only if validated.
- Stream chunks to ASR with backpressure.
- Stop on cancellation, permission loss, focus/lifecycle loss or user stop.
- Return transcript, confidence/segment metadata and language.
- Do not retain raw audio by default; delete temporary files after transcription unless the user explicitly saves them.
- If ASR confidence is low or amount/recipient/date is ambiguous, ask for correction before planning.

## 9.3 API 27 candidate

sherpa-onnx is a promising ASR/KWS POC because inspected Android samples declare minSdk 21 and use Kotlin/JNI. This is not proof that any particular model fits the TECNO. Benchmark model artifacts independently for license, ABI, disk, peak PSS, CPU, real-time factor, latency, English/Telugu accuracy, battery and thermal behavior.

---

# 10. Memory, knowledge and personalization

Use three memory classes, each with different retention and approval.

## 10.1 Session memory

Short-lived context for the current task:
- current goal and constraints;
- plan and completed steps;
- last observation summary;
- unresolved question;
- relevant tool outputs.

Bound size; summarize only at safe boundaries; never drop action/result pairs required for audit.

## 10.2 User-approved long-term memory

Examples:
- explicit user preferences;
- recurring app/workflow choices;
- approved personal defaults;
- named contacts only when user asks to remember them and data policy allows.

Memory record fields:
- ID, type, content, normalized key;
- source/provenance and confidence;
- created/updated/expiry;
- user approval state;
- sensitivity class;
- retention/deletion policy;
- embeddings/index reference if used.

The model can propose a memory candidate; deterministic policy decides whether it is safe to store. Sensitive credentials, passwords, OTPs, authentication secrets and highly sensitive personal data must not be stored as general agent memory.

## 10.3 Operational memory

Verified facts about prior tasks:
- app package aliases;
- known UI navigation patterns;
- task outcome and failure modes;
- device capability probe results with timestamps;
- workflow version and success rate.

Operational memory must expire when app version, device profile or UI evidence changes. Never trust an old selector without re-observation.

## 10.4 Retrieval

Start with SQLite queries and FTS5 when available. Use embeddings only if a benchmark proves a meaningful benefit and storage/RAM cost is acceptable. Retrieved memory is context, not authority. It cannot bypass policy or replace fresh device observation.

---

# 11. Declarative skills and reusable workflows

A skill is a signed/versioned data manifest, not executable code.

Manifest fields:
- skill ID, version, name and description;
- supported app/package and version range;
- input schema and output schema;
- typed step list;
- required capabilities;
- risk tier and approval points;
- preconditions and postconditions;
- timeout/retry bounds;
- provenance and author;
- user-enabled state;
- validation and compatibility status.

Allowed step kinds:
- observe;
- launch registered app;
- find unique semantic target;
- click target;
- set text;
- scroll;
- wait for condition;
- verify;
- request user input/approval.

Reject shell, arbitrary scripts, reflection, dynamic class names, downloaded dex/native libraries, unrestricted URLs/intents and self-modifying skills.

Skills should be dry-run validated, versioned and user-enabled. A skill update invalidates prior approvals and compatibility claims.

---

# 12. Scheduler and background execution

Scheduling is a later phase, not part of the first autonomous loop.

Use:
- Room as source of truth for job definitions and run state;
- WorkManager/JobScheduler for deferrable due signals;
- exact alarms only when the use case and OS policy justify them;
- visible notifications and user controls;
- execution-time capability and approval revalidation.

A worker may signal that a job is due; it must not directly execute arbitrary model actions. On due time:
1. load job definition;
2. check enabled/expiry and user settings;
3. reconstruct a fresh task;
4. obtain fresh device observation;
5. re-evaluate risk/approval;
6. execute only if permitted and lifecycle constraints allow;
7. persist result and notify the user.

Do not promise continuous background operation on API 27. Android 8 background limits and OEM battery management must be tested.

---

# 13. Persistence and database design

Room + SQLite WAL should be the durable source of truth. Use transactions for state transitions and unique constraints for action IDs/idempotency keys.

## 13.1 Core tables

| Table | Key fields |
|---|---|
| `task_sessions` | id, goal_digest, redacted_goal, modality, language, privacy_mode, state, created/updated/completed, plan_version |
| `task_steps` | id, task_id, sequence, objective, state, action_schema, precondition, postcondition, retry_count, deadline |
| `action_attempts` | id, step_id, action_id, action_kind, argument_digest, redacted_args, target_package, snapshot_id, idempotency_key, dispatch_state, timestamps, result |
| `device_snapshots` | id, task_id, sequence, foreground_package, observed_at, source, redacted_semantic_summary, truncation_flags |
| `verification_records` | id, action_attempt_id, status, verifier_id/version, evidence_digest, verified_at, reason |
| `approval_events` | id, task_id, step_id, action_digest, target_digest, decision, issued_at, expires_at, consumed_at |
| `audit_events` | id, task_id, sequence, event_type, redacted_payload, created_at |
| `memory_records` | id, type, normalized_key, encrypted_payload, provenance, approval_state, sensitivity, expiry |
| `skill_manifests` | id, version, manifest, checksum, enabled, validation_state, provenance |
| `scheduled_jobs` | id, skill/task_template, due_rule, state, next_due, expiry, approval_policy |
| `model_artifacts` | id, version, source, checksum, size, license, ABI/API constraints, verified_state |
| `model_metrics` | model_id, device_profile, peak_pss, cold_load_ms, first_token_ms, throughput, accuracy, thermal_result, measured_at |
| `capability_status` | capability_id, status, permission_state, api_range, device_profile, last_probe, evidence |

## 13.2 Transaction boundaries

- Record action intent before platform dispatch.
- Record dispatch result in a separate transaction.
- Record post-dispatch observation and verification together with the step transition.
- Consume approval token atomically with action intent.
- Never hold a DB transaction open during model inference, UI wait or audio capture.
- Migrations must be tested with representative old databases.

## 13.3 Encryption and retention

- Use Android Keystore-backed encryption for sensitive payloads; metadata should be minimized.
- Keystore keys may be invalidated or unavailable; define recovery behavior and never silently fall back to plaintext.
- Do not persist raw screenshots/audio by default.
- Redact password fields, OTPs, auth tokens and secret-like strings before logging or model context.
- Provide task-history clear/export and memory review/deletion.
- Retention defaults should be conservative and documented.

---

# 14. Model and artifact manager

Model manager is separate from agent planner.

Responsibilities:
- signed/allowlisted catalog metadata;
- model/runtime compatibility matrix;
- user opt-in download;
- storage and RAM preflight;
- resumable HTTPS transfer;
- checksum verification;
- license display and acceptance;
- atomic activation;
- rollback to previous known-good artifact;
- safe unload and deletion;
- performance telemetry.

Do not select a model based only on “available RAM” from one sample. Measure peak PSS and leave headroom for Android, foreground app, accessibility tree, database, audio buffers and recovery.

Model lifecycle:
`DISCOVERED → ELIGIBLE → USER_APPROVED → DOWNLOADING → CHECKSUM_VERIFIED → INSTALLED → SELF_TESTED → ACTIVE`.
Any failure moves to a safe non-active state; partially downloaded or corrupt files are never loaded.

---

# 15. Privacy and security model

## 15.1 Data classification

- Public: static capability metadata and non-personal documentation.
- Task data: user goals and task summaries.
- Sensitive task data: message contents, names, financial values, personal records.
- Secrets: passwords, OTPs, session cookies, auth tokens and key material — never general memory.

## 15.2 Cloud boundary

Before sending task context to a cloud model:
- task must allow cloud use;
- explain what context is sent where appropriate;
- redact secrets and unrelated content;
- minimize screenshot/tree content;
- never upload raw audio/screenshots by default;
- record provider/model and data categories in the audit.

Local-only mode must fail closed rather than silently use cloud fallback.

## 15.3 Threats and mitigations

| Threat | Mitigation |
|---|---|
| Prompt injection in screen text | Treat UI as untrusted data; policy remains outside model |
| Malformed model tool call | Strict schema parser, size limits, unknown action rejection |
| Stale node click | Fresh snapshot and target re-resolution |
| Wrong app/recipient | Package/target binding and exact approval digest |
| Approval replay | Single-use token, expiry, digest and snapshot binding |
| Process death after side effect | Durable intent journal and reconciliation; no blind replay |
| Secret leakage | Redaction before context, logs and memory |
| Compromised model provider | Narrow tool allowlist, deterministic policy, independent verifier |
| Permission revoked mid-task | Recheck immediately before dispatch; fail closed |
| Malicious skill | Declarative schema only, signature/provenance, validator and capability scope |
| Corrupt model artifact | HTTPS, checksum, atomic install and self-test |
| Database corruption | WAL, transactional writes, migrations, backup/export policy |

---

# 16. Capability catalog and API 27 compatibility

The agent must publish a capability matrix per device, not a universal list of promises.

## 16.1 Compatibility classes

- **Baseline API 27:** core UI, Room, app launch, accessibility semantic tree, user-started AudioRecord, MediaProjection with consent, dispatchGesture if service metadata declares it, standard Android APIs available at that API.
- **Conditional API 27:** specific hardware and OEM operations after capability probe and physical verification.
- **Newer Android only:** AccessibilityService.takeScreenshot (API 30+), newer on-device model services and APIs.
- **Unavailable/forbidden:** privileged hidden APIs, root-only operations, bypasses and unsupported silent controls.

## 16.2 Capability discovery

At app start, service connect, permission return and before action:
- inspect SDK level;
- inspect package/service state;
- inspect permissions and special access;
- query PackageManager features;
- inspect sensor/camera/flash availability;
- inspect hardware-specific API support;
- update timestamped capability status.

Do not treat a failed probe as hardware absence without evidence. Use `UNKNOWN` where the OS does not expose enough information.

## 16.3 Device identity gate

Before freezing RAM/model thresholds, record from the physical target:
- manufacturer, model, product/device identifiers;
- Android build fingerprint and API level;
- ABI and native library support;
- total RAM and MemAvailable under idle and foreground load;
- storage free space;
- battery and thermal behavior;
- installed OS security patch.

There is a documented discrepancy between the owner's earlier 4 GB description and donor hardware evidence describing a 2 GB variant. Resolve this before setting model eligibility.

---

# 17. Evaluation and acceptance criteria

## 17.1 Test layers

1. Pure unit tests: parsers, risk, state machine, digest, expiry, target resolver, retry classifier.
2. Contract tests: planner schema, tool registry, provider cancellation, capability adapters.
3. Room tests: transaction atomicity, migration, uniqueness, crash recovery, encryption failure.
4. Android instrumentation: service lifecycle, accessibility traversal, node recycling, permission flow, MediaProjection consent.
5. Probe app tests: exact click, decoy controls, duplicate labels, edit fields, scroll, dialogs, package switch.
6. Physical API 27 tests: install, service enable, task run, audio, memory, background restrictions and OEM behavior.
7. Security tests: prompt injection, approval replay, recipient substitution, secret redaction, malicious skill and model output.
8. Resilience: process kill at every state transition, offline mode, low memory, DB failure, service disconnect, permission revoke, timeout.
9. Performance: PSS, cold start, latency, CPU, battery, thermal, model throughput and ASR real-time factor.

## 17.2 Initial benchmark corpus

Start with 30–40 deterministic scenarios:
- 5 app launch/navigation;
- 5 semantic target resolution and decoy rejection;
- 5 text entry/draft without submission;
- 5 reversible native/device actions;
- 5 permission and approval cases;
- 5 process-death/timeout recovery;
- 5 prompt injection and unsafe request refusal;
- 5 speech transcription cases when ASR exists.

Each scenario must specify:
- initial device/app state;
- user goal;
- allowed capabilities;
- expected final state;
- forbidden side effects;
- approval requirement;
- timeout;
- evidence source;
- cleanup/reset procedure.

## 17.3 Release gates

- Unknown action acceptance: zero.
- Unauthorized mutation: zero.
- Wrong-target action in probe corpus: zero.
- Blind replay of uncertain non-idempotent action: zero.
- Mutating action marked successful without verifier evidence: zero.
- Approval digest mismatch accepted: zero.
- Password/OTP/secret leakage into model context or logs: zero in test corpus.
- Every supported task has a defined verifier.
- API 27 physical acceptance completed for every advertised baseline capability.
- CI green on clean checkout; no test suite silently skipped without a documented reason.

Task success rate is meaningful only when these safety invariants hold. Do not trade away safety to improve completion percentage.

---

# 18. Performance and resource budgets

The exact device variant remains unresolved; therefore do not set a fixed model size or claim an inference budget yet.

Measure:
- app cold start and memory baseline;
- service memory and accessibility-tree worst case;
- DB and cache footprint;
- ASR model peak PSS and real-time factor;
- LLM cold load, peak PSS, first token and tokens/sec;
- CPU saturation and thermal throttling;
- foreground app impact;
- battery drain during 5, 15 and 30 minute tasks;
- behavior under low-memory kill and app switching.

Resource router must reserve headroom for the OS, target app, agent process, database, native heap, audio and recovery. Model availability is recalculated at load time, not only when displayed in a catalog.

---

# 19. Implementation roadmap

The order below is dependency-driven. Do not skip a gate because a model demo looks impressive.

## Phase 0 — requirements and hardware freeze
- Confirm exact TECNO variant, RAM, ABI and storage.
- Freeze supported task categories and explicit exclusions.
- Complete donor provenance and license ledger.
- Approve initial benchmark and acceptance definitions.
- Sign off product/API 27 baseline.

**Exit:** exact device matrix and acceptance criteria approved.

## Phase 1 — reliable Android shell and actuator proof
- Build clean API 27 app shell.
- Validate service enable/disable lifecycle.
- Build owned accessibility probe app with decoys.
- Implement semantic snapshot and fresh unique target resolution.
- Implement click, set-text, scroll and app launch with independent verifier.
- Add service-disconnect and target-app guards.

**Exit:** exact-target interaction passes probe corpus on physical API 27.

## Phase 2 — durable execution engine
- Room task/step/action schema.
- Transactional state transitions and action-intent journal.
- Approval receipt consumption.
- Process-death recovery reconciliation.
- Cancellation/global stop.
- User task UI and history.

**Exit:** kill/restart at every action boundary causes no duplicate side effect and produces honest task status.

## Phase 3 — planner and model provider
- Provider interface and strict JSON/schema output.
- ADK Kotlin isolated POC.
- Cloud consent/privacy route.
- Plan normalizer, validator and deterministic fallback.
- Model output fuzz tests and prompt-injection corpus.

**Exit:** planner can propose bounded plans but cannot bypass policy or call Android directly.

## Phase 4 — Android capability catalog
- App launch and supported system panels.
- Flashlight/vibration/sensor adapters where supported.
- Gesture/MediaProjection opt-in flow if required.
- Capability discovery and diagnostics.
- Independent readback verifier per capability.

**Exit:** every advertised capability has a device/API-specific test and truthful status.

## Phase 5 — voice
- Push-to-talk UI and AudioRecord.
- sherpa-onnx English ASR POC.
- Transcript preview/correction.
- Telugu/Hindi model evaluation.
- Wake word only after battery/privacy gates.

**Exit:** voice reliably produces corrected text on API 27; no hidden capture.

## Phase 6 — memory and skills
- Session context compaction.
- User-approved memory lifecycle and deletion.
- FTS search and provenance.
- Declarative skill manifest, validation and versioning.
- Skill compatibility checks and safe execution.

**Exit:** memory never overrides user/policy; skills cannot execute arbitrary code.

## Phase 7 — scheduler and autonomy
- Durable scheduled-job schema.
- WorkManager due signal and explicit execution handoff.
- User-visible notification, stop and edit controls.
- Revalidate capability, approval and device state at run time.
- Recovery and missed-run policy.

**Exit:** scheduled task behavior survives reboot/process death without unauthorized background action.

## Phase 8 — hardening and release
- Security/privacy review.
- Dependency/license audit.
- Physical API 27 and newer-device matrix.
- Battery, memory, thermal and latency benchmarks.
- Backup/export, migrations, accessibility and voice UX.
- Signed release and rollback plan.

**Exit:** all release gates in section 17 pass with evidence.

---

# 20. Decisions that must remain open until evidence exists

1. Exact target RAM variant and model memory ceiling.
2. Whether API 27 gesture dispatch should be enabled in the accessibility service.
3. Whether MediaProjection visual observation is needed at all; semantic accessibility may be enough for baseline tasks.
4. ADK Kotlin dependency/version versus a smaller provider-neutral implementation.
5. Local LLM runtime and model. No baseline selected yet.
6. sherpa-onnx model choice for English/Telugu and wake-word requirement.
7. Which hardware actions are genuinely supported on the exact TECNO variant.
8. Whether any background scheduled actions are in first release scope.
9. Memory retention defaults and which memory classes are user-editable.
10. Whether newer Android devices receive extra features or share only the API 27 common denominator.

Do not silently decide these by convenience. Record each owner decision and evidence in the project status.

---

# 21. Immediate next engineering backlog

Priority order:

1. Read current source and reconcile this architecture with existing app/core contracts.
2. Fix Room journal to support task/step states, action intent, dispatch result and verification transaction boundaries.
3. Implement startup recovery query and uncertain-dispatch reconciliation.
4. Add exact-target uniqueness and foreground package guard tests.
5. Add explicit approval UI and single-use token consumption; mutations stay blocked until this is complete.
6. Build a probe APK/test harness and physical API 27 test checklist.
7. Run isolated ADK Kotlin cloud tool-call POC.
8. Run isolated sherpa-onnx push-to-talk ASR POC.
9. Reconcile device RAM/ABI before model benchmarking.

No feature is complete because its interface or README exists. It is complete only when its code, tests, runtime evidence and status record agree.

---

## 22. Research basis

This design synthesizes, rather than blindly copies, the inspected source patterns from:

- Google ADK Kotlin — tool calling, confirmation, Room session events and resumability.
- OpenDroid — Android plan manager, accessibility observer, dispatcher, validator, recovery and click-targeting tests.
- Mobilerun — manager/executor split, shared device state and differentiated retry.
- Llamatik — Kotlin tool registry, permission and confirmation contracts.
- Hermes — one authoritative loop, tool registry, memory, approval and scheduling.
- AndroidWorld — task-based benchmark and independent evaluation.
- MobileUse — progress tracking and reflection patterns.
- Mobilerun Portal — API 27 accessibility/MediaProjection architecture (reference only; AGPL-3.0).
- sherpa-onnx — Android API 21+ ASR/KWS examples.
- Maestro, Appium and OpenATX — deterministic UI automation testing.
- LangGraph — checkpoint and durable execution concepts.
- Google AI Edge Gallery, LiteRT samples and llama.cpp — modern-device inference/tool-call examples with API/runtime limitations.

The repository-by-repository analysis and pinned source links are in `RESEARCH/ANDROID_AUTONOMOUS_AGENT_REPOSITORY_DEEP_DIVE_2026-11-03.md`. Existing contracts, data model, performance budgets and test strategy remain complementary documents; this file is the integrated product architecture proposal.

**Final design rule:** the model proposes; the policy authorizes; the Android adapter executes; the verifier proves; the journal remembers.

---

# 23. Reverse Engineering Agent (REA)

REA is an app-behavior discovery subsystem. Its purpose is to learn repeatable, permitted workflows in installed apps and express them as versioned declarative skills.

## 23.1 Observation boundary

REA may inspect:
- package identity, version and SDK metadata exposed by PackageManager;
- visible accessibility nodes, labels, view IDs, bounds and interaction flags;
- foreground package/activity and permitted lifecycle signals;
- screen fingerprints captured from bounded, redacted snapshots;
- state changes observed after user-authorized probes.

REA does not imply:
- reading another app's private databases, internal files or private APIs;
- bypassing authentication, OTP, payment confirmation or app security;
- silently capturing screenshots, credentials, clipboard or microphone data;
- decompiling protected code or defeating anti-tamper controls.

## 23.2 Discovery workflow

1. Identify app package/version and check whether its UI can be observed.
2. Capture a bounded semantic snapshot and redact password/secret fields.
3. Group observations into screen fingerprints using package, visible labels and stable view identifiers.
4. Ask the user to select or approve a low-risk exploration goal.
5. Execute one probe at a time through the ordinary policy/approval/dispatch pipeline.
6. Capture a new snapshot and verify the expected postcondition independently.
7. Record the screen transition, target grounding, result and limitations.
8. Repeat probes to measure stability and reject duplicate-label/decoy ambiguity.
9. Propose a skill manifest only from observed steps; never infer an unobserved mutation.
10. Validate the manifest against registered capabilities, action schemas, app version and evidence.
11. Require explicit user approval before enabling the skill.
12. Invalidate or mark the skill stale when package version, screen fingerprint or target semantics materially change.

## 23.3 Skill evidence and compatibility

A certified skill records:
- exact package and app version;
- versioned input/output schema;
- ordered typed action steps;
- preconditions and postconditions;
- required capabilities and risk tier;
- verified probe names and evidence digest;
- known limitations and confidence;
- creation provenance, approval and last validation time.

A skill is not a universal selector script. Every execution re-observes the current screen, resolves a unique target and runs the normal guard. App updates or UI drift require revalidation.

---

# 24. Self-created tool system

The agent may identify that a repeated task needs a new tool and create a tool definition. This is **controlled composition**, not unrestricted self-modifying code.

## 24.1 Permitted creation path

1. Detect a missing operation or repeated workflow.
2. Search the existing tool registry and capability catalog.
3. Define a versioned input schema and output/postcondition contract.
4. Compose one to eight existing registered action templates.
5. Bind inputs only through typed parameter references.
6. Calculate the maximum risk across all steps; never lower an underlying action's risk.
7. Validate capabilities, app scope, parameter types, action registrations, bounds and approval requirements.
8. Run dry-run and deterministic tests against fixtures/probe apps.
9. Present the exact tool definition, permissions and effects to the user.
10. Persist a pending tool version; activate only after explicit user approval.
11. Audit executions and disable the tool when its dependencies or app version become incompatible.

## 24.2 Prohibited creation path

- No generated shell commands or arbitrary scripts.
- No generated Kotlin/Java, DEX, native libraries or dynamic class loading.
- No reflection-based access to hidden Android APIs.
- No arbitrary permission escalation or capability invention.
- No bypass of the ordinary action registry, approval policy, journal or verifier.
- No self-approval and no tool that can modify its own safety policy.

## 24.3 Future extension boundary

A later, separately reviewed developer mode may support compiling new first-party modules during development. That is not an in-app runtime capability and must use a build sandbox, code review, dependency/license checks, signed artifacts and explicit installation. The on-device agent remains restricted to declarative tool composition.
