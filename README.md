# Personal Autonomous Android Agent

A Kotlin-native, local-first Android agent with controlled app observation, planning, execution, verification, recovery, REA (Reverse Engineering Agent), reusable skills and declarative self-created tools.

## Repository layout

- [`agent/`](agent/README.md) — the actual product implementation: Android application, Kotlin execution core, platform adapters and tests.
- [`research/`](research/README.md) — all research, architecture, design, roadmap, donor audits, benchmarks, licensing and engineering evidence.
- [`PROJECT_STATUS.md`](PROJECT_STATUS.md) — canonical progress ledger, current phase, verified test evidence and next engineering task.
- [`.github/workflows/`](.github/workflows/) — CI workflows.
- Root Gradle files — build orchestration only; implementation modules live under `agent/`.

## Build

Gradle modules are `:agent:core` and `:agent:app`.

```bash
gradle :agent:core:test
gradle :agent:app:testDebugUnitTest
gradle :agent:app:assembleDebug
```

Read [PROJECT_STATUS.md](PROJECT_STATUS.md) before continuing implementation. Documentation is not proof of implementation; CI is not proof of physical-device behavior.
