# Personal Autonomous Android Agent

Research-first workspace for a personal, local-first Android agent that understands goals, observes device/app state, plans, acts through permitted device capabilities, verifies outcomes and recovers from failures.

> **Start every work session here:** [PROJECT_STATUS.md](PROJECT_STATUS.md). It is the canonical progress ledger, roadmap pointer, blocker list and session log. Read it before inspecting or changing anything; update it before ending the session.
>
> **Product target:** personal use and sideloaded installation. Google Play publication is not a requirement.
> **Status (2026-11-02):** research/specification baseline; no destination app implementation has been validated.

## Product thesis

Build a reliable Android-native assistant—not a chat wrapper—that can:
- Accept voice/text goals, ultimately including an optional "Hey Jarvis" wake phrase.
- Observe app/device state through AccessibilityService, supported APIs, intents and user-consented visual capture where technically available.
- Plan and execute multi-step tasks using bounded atomic actions.
- Verify each state transition and recover from UI drift, timeouts, app crashes and model errors.
- Work offline for supported tasks with models selected against measured device resources.
- Keep memory, history, permissions and data retention inspectable and user-controlled.\n- Optionally use a separate home-monitoring mode for local unfamiliar-face alerts and secure remote event review, without continuous video retention by default.

## Distribution decision

This is a **personal-use project**. We are not designing for Play Store publication. Prioritize capabilities useful in a directly installed app, subject to Android technical limits, user-enabled permissions, app sandboxing and privacy/security constraints.

Google Play policy is retained as reference material only; it is not the primary product constraint. Do not misrepresent the app or bypass Android security protections.

## Non-goals

- Hidden surveillance or silent recording.
- Bypassing lock screens, authentication or app security boundaries.
- Unrestricted arbitrary shell/code execution from model output.
- Claiming success without postcondition verification.
- Requiring Docker.
- Requiring cloud connectivity or a large model for basic supported tasks.

## Repository map

- [Project status, roadmap tracking and session log](PROJECT_STATUS.md)
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
- [Hardware capabilities](ARCHITECTURE/HARDWARE_CAPABILITIES.md)\n- [Home security and secure remote access](ARCHITECTURE/HOME_SECURITY_AND_REMOTE_ACCESS.md)
- [Model router](ARCHITECTURE/MODEL_ROUTER.md)
- [Memory and learning](ARCHITECTURE/MEMORY_AND_LEARNING.md)
- [Phases](ROADMAP/PHASES.md)
- [Phase-by-phase implementation and code reuse blueprint](ROADMAP/PHASE_IMPLEMENTATION_BLUEPRINT.md)
- [Acceptance criteria](ROADMAP/ACCEPTANCE_CRITERIA.md)
- [Decision log](ARCHITECTURE/DECISION_LOG.md)
- [Sources](REFERENCES/SOURCES.md)

## Working rules

1. Read [PROJECT_STATUS.md](PROJECT_STATUS.md) before every work session; inspect actual GitHub state rather than trusting chat history.
2. Follow the canonical phase order and entry/exit gates unless the owner explicitly changes scope.
3. Date research claims and label evidence as verified, observed, hypothesis or decision.
4. Inspect exact source code and pinned revisions before adopting libraries or copying code.
5. Audit licenses for code, assets, models and transitive dependencies before reuse.
6. Keep orchestration, inference, observation, actuation, memory and skills replaceable.
7. Prefer Kotlin/Android-native runtime; use Python for research/evaluation.
8. No Docker dependency; use Windows-native tooling and physical devices.
9. CI covers compilation, unit tests, lint/static analysis and dependency/license checks. Physical-device behavior is validated separately.
10. After every work session, update PROJECT_STATUS.md with changes, test evidence, blockers, phase status and the next exact task.
11. Never claim a test passed if it was not run. Never claim a feature is implemented because it is documented.
12. Do not start app source-code implementation until Phase 0 is closed and explicit authorization is recorded.

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

See [decision log](ARCHITECTURE/DECISION_LOG.md) and [PROJECT_STATUS.md](PROJECT_STATUS.md).