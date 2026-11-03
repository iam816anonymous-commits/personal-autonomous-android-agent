# Hermes-inspired Kotlin implementation

This directory is the first native-Kotlin implementation tranche for the Personal Autonomous Android Agent.

## Porting rule

Hermes Agent is a design/source reference. This implementation does **not** embed its Python runtime or copy its tool/terminal execution surface. It ports selected architectural patterns into platform-neutral Kotlin contracts that Android adapters can implement.

Hermes source inspected: `NousResearch/hermes-agent`, branch `main`, inspected 2026-11-03. Its root licence is MIT. No Hermes source file is copied verbatim in this tranche; the implementation is an independent Kotlin design informed by the inspected concepts.

## Components in this tranche

- `AgentContracts.kt`: closed action vocabulary, evidence, snapshots, postconditions and results.
- `ToolRegistry.kt`: explicit immutable registry; no import-time/self-registration.
- `ApprovalPolicy.kt`: capability checks, risk policy and expiring action-bound approvals.
- `AgentOrchestrator.kt`: bounded observe-plan-validate-approve-execute-observe-verify loop.
- `MemoryAndSkills.kt`: memory interfaces and declarative, non-executable skill validation.
- `ScheduledJobs.kt`: durable-job boundary and lifecycle guards; Android WorkManager adapter comes later.

## Deliberate limitations

This is a platform-neutral core tranche, not yet a complete Android application. It contains no Android API calls, Room database implementation, AccessibilityService, model SDK, WorkManager adapter, Gradle build, or physical-device proof. Those belong in the corresponding Android integration phases and must not be represented as implemented.

The initial core is dependency-free Kotlin. Android-specific implementations will be injected behind interfaces. All model output remains untrusted input and must be decoded into these closed types before dispatch.

## Required next integration work

1. Establish the Android Gradle application and module graph.
2. Add unit-test/build CI and compile this core with the selected Kotlin toolchain.
3. Add Room-backed write-ahead task journal and encrypted memory adapter.
4. Add API-aware Android capability registry and AccessibilityService observer.
5. Implement Android dispatchers and independent verifiers.
6. Run the frozen benchmark and physical TECNO API 27 acceptance tests.

Do not mark an action successful because an Android API call returned or a dispatch callback fired. Only a matching independent postcondition can produce `VERIFIED`.
