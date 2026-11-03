# Hermes Agent to Kotlin Port Review

**Reviewed:** 2026-11-03  
**Hermes repository:** https://github.com/NousResearch/hermes-agent  
**Pinned inspected revision:** `c314285f077e91937c995d419b9fa40af9eb4981`  
**Licence:** MIT (root `LICENSE`; retain the copyright and permission notice if any substantial Hermes code is copied in a later change).  
**Destination implementation:** `IMPLEMENTATION/KOTLIN_PORT/`

## 1. Scope and provenance

Inspected the Hermes conversation loop, model/tool schema boundary, tool registry, memory manager/store, FTS5 state indexing, skill manager and write guards, approval gateway, cron job store and lifecycle guard.

The Kotlin tranche in this repository is an independent implementation informed by those designs. No Hermes Python source was copied verbatim. No Hermes Python runtime, shell/terminal executor, browser automation, gateway, third-party skill executable, or external tool execution surface is included.

## 2. Port mapping

| Hermes source/design | Kotlin destination | Decision |
|---|---|---|
| `agent/conversation_loop.py` | `AgentOrchestrator.kt` | Rewrite as bounded suspend-based observe/plan/policy/dispatch/verify loop |
| `model_tools.py` + `tools/registry.py` | `AgentContracts.kt`, `ToolRegistry.kt` | Adapt schema/dispatch separation; explicit immutable registry, no import-time registration |
| `agent/memory_manager.py`, `tools/memory_tool.py`, `hermes_state_fts.py` | `MemoryAndSkills.kt` interfaces | Adapt memory separation and search boundary; Room/FTS implementation not yet written |
| `tools/skill_manager_tool.py`, `tools/skill_manager_guards.py` | `MemoryAndSkills.kt` | Rewrite as declarative, non-executable, capability-scoped skills; no dynamic code execution |
| `tools/approval.py` | `ApprovalPolicy.kt` | Rewrite as exact action digest + target + snapshot + expiry bound approval |
| `cron/jobs.py`, `cron/lifecycle_guard.py` | `ScheduledJobs.kt` | Adapt durable unique-job contract and cancellation; WorkManager adapter not yet written |

## 3. Implemented in this tranche

- Closed Kotlin action vocabulary for app launch, accessible-node click, accessible text entry and foreground observation.
- Bounded action timeouts, attempt limits, snapshot node count and text lengths.
- Immutable capability descriptors and explicit registry freeze.
- Per-capability hardware/API/permission/runtime status fields.
- Action-specific approval digest, target-package binding, snapshot binding, monotonic expiry and stale-snapshot rejection.
- Risk-ceiling check at dispatch boundary.
- Bounded orchestration (maximum eight actions and a task deadline).
- Independent post-dispatch observation and verification interface.
- Memory and skill interfaces, plus a declarative skill manifest validator.
- Durable scheduled-job/store/enqueuer interfaces and unique-work boundary.

## 4. Important limitations / issues to resolve before integration

This is a source tranche, **not a buildable Android application yet**.

- No Android Gradle project, Gradle wrapper, manifest, application module or CI workflow exists in this branch at this point.
- Kotlin sources have not been compiled or unit-tested. Mark build/test status **NOT RUN**.
- No Room-backed task journal or memory store exists yet.
- No encryption adapter, Android Keystore integration or FTS implementation exists yet.
- No Android AccessibilityService, platform capability probe, device action adapter, independent Android verifier, WorkManager adapter or model provider exists yet.
- `ApprovalProvider` and `issueUserApproval` must be wired exclusively to explicit user UI; model output must never be able to mint approval tokens.
- The orchestrator's in-memory completed-action list is not durable recovery. The future Room journal must reconcile in-flight actions after process death and must never blindly replay non-idempotent actions.
- Capability discovery snapshots must be refreshed at execution time; caller-supplied stale maps are not sufficient for production.
- A timeout after dispatch leaves outcome uncertain. The Android adapter must re-observe before any retry.
- Skill approval, version pinning, revocation, migration and per-step execution revalidation still need implementation.
- Scheduler insertion/enqueue crash windows need a reconciliation worker.

## 5. Device target and Phase 0 gates

The core contracts are platform-neutral and do not depend on the disputed memory figure. Before Android packaging and model eligibility are implemented, reconcile the device inventory: owner-provided TECNO Camon i Click / IN6 / H633 description and 4 GB RAM versus the donor hardware report identifying TECNO IN6 / Spark 2 with 2 GB RAM. Preserve raw device evidence and resolve the exact physical variant; do not silently select the more generous RAM figure.

The project status file remains authoritative for owner gates. This branch does not claim Phase 0 is fully closed, and does not claim Android API 27 physical-device support has been demonstrated.

## 6. Validation

- Source files committed: yes.
- Kotlin compilation: **NOT RUN** (no Gradle project/wrapper in destination repository).
- Unit tests: **NOT RUN**.
- Android build: **NOT RUN**.
- CI: **NOT CONFIGURED / NOT RUN** for this source tranche.
- Physical-device test: **NOT RUN**.

## 7. Required next implementation steps

1. Add a reproducible Gradle/Kotlin build and unit tests; compile this source tranche before adding Android dependencies.
2. Fix any compiler/test findings and add deterministic policy/orchestrator tests.
3. Create the minimal Android application shell and module graph.
4. Implement Room WAL journal with explicit in-flight/uncertain states.
5. Add Android-specific registry adapters, AccessibilityService observation and verified action adapters.
6. Add encrypted memory, FTS search, approved skill persistence and WorkManager integration in their roadmap phases.
