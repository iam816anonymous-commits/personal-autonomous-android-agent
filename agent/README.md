# Agent implementation

This folder contains the real executable project code. Research documents and donor-repository reports do not belong here.

- `app/` — Android application, AccessibilityService, REA UI/controller, Android adapters, Room journal, scheduling and Android-specific tests.
- `core/` — platform-neutral Kotlin agent contracts, orchestrator, approval policy, durable execution protocol, REA/skill model, tool factory and JVM tests.

Gradle project paths: `:agent:core` and `:agent:app`. The root settings file maps them to these directories.

Rules: keep executable source/tests here; keep research and architecture documents under `research/`; treat model output as untrusted; generated tools may compose registered native actions only; require independent verification before claiming an action succeeded.
