# Open-source autonomous-agent repository audit

**Last reviewed:** 2026-11-02  
**Scope:** Curated reference shortlist, not an exhaustive GitHub search. README/source/selected implementation paths were inspected in the preceding research. Re-audit exact revisions before copying code.

## Summary

The strongest references are useful for different layers. None should be assumed to be a complete embeddable Android-native agent.

| Project | Main contribution | Runtime reality | Reuse posture |
|---|---|---|---|
| [MobileUse](https://github.com/MadeAgents/mobile-use) | Planner/operator/answer/reflector/progressor and trajectory reflection patterns; long-horizon mobile task handling | Host Python agent controlling devices through ADB; multimodal model dependencies | Strong algorithmic reference; selectively port ideas |
| [Mobilerun](https://github.com/droidrun/mobilerun) | Manager → Executor reasoning loop, direct CodeAct mode, mobile tools, tracing, accessibility + screenshots | Host Python/CLI/API plus Portal on phone; requires ADB/setup | Strong execution and tool-contract reference; inspect core and Portal separately |
| [Mobilerun Portal](https://github.com/droidrun/mobilerun-portal) | Android-side device interaction and accessibility service | Android companion component | Technically useful; AGPL-3.0 license has copyleft implications |
| [AndroidWorld](https://github.com/google-research/android_world) | 116 tasks across 20 apps, reproducible Android agent benchmark and task interfaces | Emulator-based benchmark environment | Evaluation/task design reference, not app runtime |
| [OpenGUI](https://github.com/Core-Mate/OpenGUI) | Full-stack GUI agent, app/task workflow, visual and semantic control | Full-stack Android client currently requires Android 11/API 30+; other plugins use host-side tools | Study only unless license and SDK constraints change |
| [UI-TARS Desktop](https://github.com/bytedance/UI-TARS-desktop) | Visual GUI perception/action loop, grounding and operator UX | Desktop-focused control | Transfer concepts, not code as Android runtime |
| [LangGraph](https://github.com/langchain-ai/langgraph) | Stateful graph orchestration, persistence, interrupts and human-in-loop | General framework; not Android-specific | Pattern reference; avoid embedding unnecessary stack |
| [Google ADK for Kotlin](https://github.com/google/adk-kotlin) | Kotlin-native agent orchestration and Android-oriented on-device integrations | Kotlin/Android compatible; evaluate dependencies/min SDK/runtime | High-priority proof-of-concept candidate |
| [Pydantic AI](https://github.com/pydantic/pydantic-ai) | Typed tools, structured output, model adapters and evaluation patterns | Primarily Python | Contract and evaluation reference |
| [Microsoft Agent Framework](https://github.com/microsoft/agent-framework) | Workflow, agent handoff and checkpointing concepts | Python/.NET, not Android runtime | Architecture reference |

## Source-level patterns worth adopting

### A. Hierarchical planning and reflection (MobileUse)

Inspected multi-agent flow shows specialist responsibilities such as planner, operator, answer agent, reflector, trajectory/global reflector, progressor and note-taker. The useful pattern is not to run a swarm for every action. It is to separate:
- task decomposition;
- one-step action generation;
- observation and progress evaluation;
- recovery/reflection when execution diverges;
- compact trajectory memory.

**Port:** start with a single orchestrator invoking distinct typed roles/prompts sequentially. Promote roles to separate agents only when metrics prove benefit.

### B. Explicit atomic action contract (AndroidWorld / Mobilerun)

Use a finite action vocabulary rather than free-form device commands. Typical actions include open app, tap/click, long-press, type, swipe/scroll, system navigation, wait, observe/screenshot and complete. Every action needs arguments, permission, timeout, preconditions, and a verifiable postcondition.

### C. Dual observation (Mobilerun / UI-TARS)

Accessibility/UI trees expose semantic labels and bounds; screenshots provide visual context when semantics are missing or wrong. Neither is sufficient alone. Keep an observer interface that can return semantic nodes, screenshot references, device/app state and confidence. Redact sensitive fields before model input or persistence.

### D. Evaluation before claims (AndroidWorld)

Benchmark style: natural-language task, controlled initial state, action trajectory, explicit verifier and reward/success condition. We should build a smaller physical-device suite inspired by AndroidWorld, not claim equivalence or require Docker/emulators.

## Repository links and source notes

- MobileUse: https://github.com/MadeAgents/mobile-use
- Mobilerun: https://github.com/droidrun/mobilerun
- Mobilerun Portal: https://github.com/droidrun/mobilerun-portal
- AndroidWorld: https://github.com/google-research/android_world
- OpenGUI: https://github.com/Core-Mate/OpenGUI
- UI-TARS Desktop: https://github.com/bytedance/UI-TARS-desktop
- LangGraph: https://github.com/langchain-ai/langgraph
- ADK Kotlin: https://github.com/google/adk-kotlin
- Pydantic AI: https://github.com/pydantic/pydantic-ai
- Microsoft Agent Framework: https://github.com/microsoft/agent-framework

## Rejected assumptions

- A host-side ADB agent is not an Android-native runtime.
- A benchmark is not a production implementation.
- A visual desktop agent is not a mobile device actuator.
- More agents do not automatically improve task success.
- A README feature list does not establish stability, compatibility or policy compliance.
