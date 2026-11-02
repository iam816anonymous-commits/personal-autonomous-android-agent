# Architecture decision log

## ADR-000 — Research-first modular Android-native architecture
**Date:** 2026-11-02 · **Status:** Proposed

Kotlin/Android is the intended product runtime. Keep orchestration, inference, observation, actuation, memory and skills behind replaceable interfaces. Host-side Python remains useful for research/evaluation. No Docker dependency.

## ADR-001 — Personal sideloaded deployment is primary
**Date:** 2026-11-02 · **Status:** Accepted

The owner has confirmed the app is mainly for personal use and is not intended for Play Store publication. Prioritize capabilities useful in a directly installed app. Investigate AccessibilityService as a core UI observation/action adapter. Do not artificially narrow the design around Play policy.

**Constraints that remain:** Android permissions, background execution, microphone/foreground-service lifecycle, app sandboxing, secure windows, OEM restrictions, privacy and user consent. Sideloading does not grant root or unrestricted system privileges.

## ADR-002 — User-initiated speech before wake-word
**Date:** 2026-11-02 · **Status:** Proposed

Build and validate tap-to-talk speech-to-text before always-on wake-word. Microphone lifecycle, battery, privacy and device compatibility must be proven before wake-word work.

## ADR-003 — Verify every atomic action
**Date:** 2026-11-02 · **Status:** Proposed

Every action is schema-validated, permission-checked, bounded, executed, re-observed and verified against a postcondition. API return alone is not proof of task success.

## ADR-004 — Play policy is reference-only
**Date:** 2026-11-02 · **Status:** Accepted

Google Play policy is documented in case distribution goals change, but it is not a current product acceptance criterion. Do not misrepresent accessibility-tool status or bypass platform security protections.

## ADR-005 — Home security is a separate Android application
**Date:** 2026-11-02 · **Status:** Accepted

Home-security monitoring (camera uptime, face detection/recognition, event snapshots, notifications and remote access) is excluded from the autonomous agent's scope, roadmap, modules, permissions, dependencies and release criteria. It will be developed later as a separately packaged Android app that may be installed on the same physical phone. Keep the existing home-security document only as standalone future-project research. Do not integrate its runtime, storage, services or tools into this agent.
