# Personal Autonomous Android Agent

Research-first workspace for a personal, local-first Android agent that understands goals, observes device/app state, plans, acts through permitted device capabilities, verifies outcomes and recovers from failures.

> **Product target: personal use and sideloaded installation. Google Play publication is not a requirement.**  
> **Status (2026-11-02): research and architecture baseline; no implementation has been validated yet.**

## Product thesis

Build a reliable Android-native assistant—not a chat wrapper—that can:
- Accept voice/text goals, ultimately including an optional "Hey Jarvis" wake phrase.
- Observe app/device state through AccessibilityService, supported APIs, intents and user-consented visual capture where technically available.
- Plan and execute multi-step tasks using bounded atomic actions.
- Verify each state transition and recover from UI drift, timeouts, app crashes and model errors.
- Work offline for supported tasks with models selected against measured device resources.
- Keep memory, history, permissions and data retention inspectable and user-controlled.

## Distribution decision

This is a **personal-use project**. We are not designing for Play Store publication. Therefore, we can prioritize sideloaded personal capabilities and investigate broader AccessibilityService-based UI automation, subject to Android's technical limits, user-enabled permissions, app sandboxing and privacy/security constraints.

Google Play policy is retained as reference material only; it is not the primary product constraint. Do not misrepresent the app or bypass Android security protections.

## Non-goals

- Hidden surveillance or silent recording.
- Bypassing lock screens, authentication or app security boundaries.
- Unrestricted arbitrary shell/code execution from model output.
- Claiming success without postcondition verification.
- Requiring Docker.
- Requiring cloud connectivity or a large model for basic supported tasks.

## Research-first sequence

1. Confirm personal-device deployment assumptions and device/OS matrix.
2. Audit open-source implementations and licenses at source level.
3. Prototype AccessibilityService observation/actions, voice lifecycle, local inference/tool calling and process recovery.
4. Choose architecture from measured evidence.
5. Implement incrementally with physical-device tests and green CI.

## Repository map

- [Research index](RESEARCH/README.md)
- [Master architecture](ARCHITECTURE/MASTER_ARCHITECTURE.md)
- [Android platform constraints](RESEARCH/ANDROID_PLATFORM_CONSTRAINTS.md)
- [Open-source repository audit](RESEARCH/OPEN_SOURCE_REPOSITORY_AUDIT.md)
- [Source-by-source reuse specification](RESEARCH/SOURCE_BY_SOURCE_REUSE_SPECIFICATION.md)
- [License audit](RESEARCH/OPEN_SOURCE_LICENSE_AUDIT.md)
- [Model/inference research](RESEARCH/MODEL_AND_INFERENCE_RESEARCH.md)
- [Google Play policy reference](RESEARCH/GOOGLE_PLAY_POLICY_RESEARCH.md)
- [Execution engine](ARCHITECTURE/AGENT_EXECUTION_ENGINE.md)
- [Safety and approvals](ARCHITECTURE/SAFETY_AND_APPROVALS.md)
- [Voice and wake-word](ARCHITECTURE/VOICE_AND_WAKE_WORD.md)
- [Device control](ARCHITECTURE/DEVICE_CONTROL.md)
- [Hardware capabilities](ARCHITECTURE/HARDWARE_CAPABILITIES.md)
- [Model router](ARCHITECTURE/MODEL_ROUTER.md)
- [Memory and learning](ARCHITECTURE/MEMORY_AND_LEARNING.md)
- [Phases](ROADMAP/PHASES.md)
- [Phase-by-phase implementation and code reuse blueprint](ROADMAP/PHASE_IMPLEMENTATION_BLUEPRINT.md)
- [Acceptance criteria](ROADMAP/ACCEPTANCE_CRITERIA.md)
- [Decision log](ARCHITECTURE/DECISION_LOG.md)
- [Sources](REFERENCES/SOURCES.md)

## Working rules

1. Date research claims and label hypotheses.
2. Inspect source code and exact revisions before adopting libraries.
3. Audit licenses for code, assets, models and transitive dependencies.
4. Keep orchestration, inference, observation, actuation, memory and skills replaceable.
5. Prefer Kotlin/Android-native runtime; use Python for research and host-side evaluation.
6. No Docker dependency; use Windows-native tooling and physical devices.
7. CI covers compilation, unit tests, lint/static analysis and dependency/license checks. Physical-device behavior is validated separately.
8. Update the decision log when evidence changes a choice.

## Current technical risks

- Android background execution and microphone rules vary by OS/target SDK.
- API 27 is a legacy compatibility target; modern capabilities may be unavailable.
- Accessibility trees and coordinates can be stale or incomplete; every action needs fresh grounding and verification.
- Small local models may produce invalid plans/tool calls.
- Always-on wake-word has battery, lifecycle and privacy implications.
- OEM battery management may terminate long-running services.

## Current decisions

- Personal/sideloaded deployment is the primary target.
- Kotlin-native Android runtime is preferred.
- AccessibilityService is a key candidate for device observation and interaction, to be prototyped on target devices.
- ADK for Kotlin vs custom orchestration remains open pending a working proof-of-concept.
- API 27 compatibility is desired but feature parity is not assumed.

See [decision log](ARCHITECTURE/DECISION_LOG.md).