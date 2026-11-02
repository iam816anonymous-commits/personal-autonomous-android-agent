# Personal Autonomous Android Agent

Research-first engineering workspace for a personal, local-first Android agent that understands natural-language goals, observes device/app state, plans work, executes permitted actions, verifies outcomes, and learns from failures.

> **Status (2026-11-02): Research baseline initialized. No product implementation has been selected or validated yet.**

## Product thesis

Build a reliable Android-native assistant—not a chat wrapper—that can:
- Accept explicit user goals through voice and text, including a configurable wake phrase ("Hey Jarvis") where the OS and distribution rules permit it.
- Understand the current device/app state using accessibility semantics, screenshots, app intents, and supported system APIs.
- Decompose a goal into bounded, observable actions.
- Request confirmation for sensitive or irreversible operations.
- Execute one atomic action at a time and verify the resulting state.
- Recover from UI drift, timeouts, app crashes, permission denial, and model failures.
- Work offline for supported tasks using models selected against measured device resources; use cloud inference only when configured and disclosed.
- Maintain inspectable, user-controlled memory and execution history.

This project will distinguish **personal/sideloaded capability** from **Google Play-distributable capability**. They are not assumed to be identical.

## Non-goals

- Unrestricted or invisible control of every Android app.
- Treating AccessibilityService policy as something to evade.
- Letting an LLM directly execute arbitrary shell/code with device privileges.
- Claiming a task succeeded without observing its postcondition.
- Requiring Docker for development or device testing.
- Requiring a large model or cloud connection for basic operation.

## Research-first sequence

1. Establish product requirements, platform constraints, distribution choices, threat model, and acceptance criteria.
2. Audit relevant open-source implementations and licenses at source level.
3. Prototype the highest-risk technical assumptions: voice lifecycle, UI observation/action, on-device inference/tool calling, process death, and safe confirmation.
4. Decide the architecture using measured evidence.
5. Implement in small phases with physical-device tests and green CI.

## Repository map

- [Research index](RESEARCH/README.md)
- [Master architecture](ARCHITECTURE/MASTER_ARCHITECTURE.md)
- [Platform constraints](RESEARCH/ANDROID_PLATFORM_CONSTRAINTS.md)
- [Open-source project audit](RESEARCH/OPEN_SOURCE_REPOSITORY_AUDIT.md)
- [License audit](RESEARCH/OPEN_SOURCE_LICENSE_AUDIT.md)
- [Model and inference research](RESEARCH/MODEL_AND_INFERENCE_RESEARCH.md)
- [Google Play policy](RESEARCH/GOOGLE_PLAY_POLICY_RESEARCH.md)
- [Execution engine](ARCHITECTURE/AGENT_EXECUTION_ENGINE.md)
- [Safety model](ARCHITECTURE/SAFETY_AND_APPROVALS.md)
- [Voice architecture](ARCHITECTURE/VOICE_AND_WAKE_WORD.md)
- [Device control](ARCHITECTURE/DEVICE_CONTROL.md)
- [Model router](ARCHITECTURE/MODEL_ROUTER.md)
- [Memory and learning](ARCHITECTURE/MEMORY_AND_LEARNING.md)
- [Phased roadmap](ROADMAP/PHASES.md)
- [Acceptance criteria](ROADMAP/ACCEPTANCE_CRITERIA.md)
- [Research sources](REFERENCES/SOURCES.md)

## Working rules

1. Research claims must have dated sources. Mark unverified assumptions as hypotheses.
2. Inspect implementation code, not only README claims, before adopting a library.
3. Do not copy code until its license and transitive obligations have been reviewed.
4. Keep Android platform control, agent orchestration, model runtime, and app-specific skills behind replaceable interfaces.
5. Prefer Kotlin/Android-native runtime for the shipped client. Python tools may support research, evaluation, and host-side experiments.
6. No Docker dependency. Use Windows-native tooling where feasible and test on physical Android devices.
7. CI should validate compilation, unit tests, static analysis, dependency/license checks, and policy-sensitive manifest declarations. Do not pretend emulator jobs validate physical-device behavior.
8. Update the decision log when evidence changes an architectural choice.

## Current top-level risks

- AccessibilityService policy prevents general apps from using it to autonomously initiate, plan, and execute actions on Google Play.
- Android background execution and microphone rules differ by OS version and target SDK.
- Android 8.1/API 27 is a legacy compatibility target; it cannot be assumed to support modern platform APIs.
- Small on-device models may not reliably plan multi-step tasks or emit valid tool calls.
- UI automation is inherently brittle; semantic trees and screenshot grounding both need verification and fallback.
- Long-lived wake-word listening has privacy, battery, permission, and distribution implications.

## Decision status

No final framework choice (ADK, custom runtime, or other) has been made. No Android API, target SDK, minimum SDK, model, or distribution channel has been committed as a final implementation decision. See [decision log](ARCHITECTURE/DECISION_LOG.md).
