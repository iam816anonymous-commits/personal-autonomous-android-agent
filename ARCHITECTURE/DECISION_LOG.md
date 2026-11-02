# Architecture decision log

## ADR-000 — Research-first modular Android-native architecture
**Date:** 2026-11-02 · **Status:** Proposed

Kotlin/Android is the intended product runtime. Keep orchestration, inference, observation, actuation, memory and skills behind replaceable interfaces. Host-side Python remains useful for research/evaluation. No Docker dependency.

## ADR-001 — Distribution profiles are separate
**Date:** 2026-11-02 · **Status:** Required constraint

Google Play AccessibilityService policy prohibits general apps from using AccessibilityService to autonomously initiate, plan and execute actions or decisions. Do not make unrestricted autonomous AccessibilityService execution a Play baseline. Do not misdeclare accessibility-tool status. Personal/sideloaded use remains a separate profile, subject to Android security and privacy constraints.

## ADR-002 — User-initiated speech before wake-word
**Date:** 2026-11-02 · **Status:** Proposed

Build and validate tap-to-talk speech-to-text before always-on wake-word. Microphone lifecycle, battery, policy and device compatibility must be proven before wake-word work.

## ADR-003 — Verify every atomic action
**Date:** 2026-11-02 · **Status:** Proposed

Every action is schema-validated, permission-checked, bounded, executed, re-observed and verified against a postcondition. API return alone is not proof of task success.
