# Gemini 4 Argon Development Handoff

**Destination repository:** `iam816anonymous-commits/personal-autonomous-android-agent`  
**Handoff branch:** `phase-0-argon-handoff`  
**Created:** 2026-11-03  
**Current phase:** Phase 0 — repository archaeology, research and requirements freeze  
**Purpose:** Divide engineering work between Gemini 4 Argon and the ChatGPT engineering/review pass without bypassing project gates.

## 1. Operating agreement

The owner has instructed that development should begin. Interpret this as authorization to proceed through the existing phased roadmap, not permission to bypass acceptance gates.

- Continue Phase 0 closeout first. Phase 1 application implementation begins only after the Phase 0 exit criteria are evidenced.
- Do not commit directly to `main`. Work on a dedicated branch and submit a reviewable diff/PR.
- Do not transplant whole donor repositories.
- Do not claim a test passed unless the exact command ran and its exit code/output is recorded.
- Do not claim physical-device validation from CI or emulator output.
- Do not treat repository ownership as a substitute for source provenance or dependency-license clearance.
- Keep home-security monitoring, face recognition and remote surveillance outside this agent.
- The target is a personal-use, sideloaded, Kotlin-native Android agent. Windows-native development is preferred; Docker is not required.
- API 27 / Android 8.1 is a desired compatibility target. Feature parity must be capability-based, not assumed.
- Preserve the core invariant: model proposes typed actions; deterministic code validates policy/permissions; adapters execute; independent observers verify postconditions.

## 2. Division of work

### Gemini 4 Argon — primary deep-research and coding-agent work

Argon should own the tasks that benefit from broad repository traversal, clean builds, iterative compilation and multi-file reasoning:

1. Clone the donor repositories at the exact pinned revisions below.
2. Run clean-checkout builds, unit tests, lint/static analysis and dependency reports. Record exact commands, environment, exit codes and relevant logs.
3. Investigate current CI failures in `agent-1`; do not assume its code is reusable merely because it is a first-party repository.
4. Inspect donor implementation classes against the source-by-source reuse specification. Identify discrepancies between the current code and the recorded RETAIN / ADAPT / REWRITE / REJECT decisions.
5. Produce a component-level provenance and licence ledger for only the candidate components. Include nested licences, source headers, transitive dependencies, native binaries and assets.
6. Update the donor revision comparison if a pinned branch has moved.
7. Draft 20–30 deterministic task benchmark scenarios with initial state, goal, allowed capabilities, forbidden side effects, expected final state, timeout and independent verifier.
8. Run focused technical spikes for the unresolved decisions: ADK Kotlin vs custom orchestration, API 27 accessibility observation, local inference integration, wake-word feasibility and encryption/backup.
9. Return a concise engineering report plus patch/diff. Do not merge donor code into the destination repository during Phase 0.

### ChatGPT engineering/review pass — architecture, evidence and integration control

I will own:

1. Inspect the destination repository state and keep `PROJECT_STATUS.md` authoritative.
2. Reconcile Argon's findings with Android platform behavior, the documented architecture and device evidence.
3. Re-check donor branch heads, file-level source decisions and the security boundaries before any reuse.
4. Review the benchmark for determinism, meaningful postconditions and forbidden side effects.
5. Resolve architectural conflicts and designate one owner for each responsibility.
6. Reject unsafe or unproven implementation patterns, including model-controlled Android API calls, ambiguous target selection, unbounded retries, arbitrary shell/code execution and success-by-dispatch.
7. Define canonical Kotlin contracts and module boundaries for Phase 1 after Phase 0 passes.
8. Review every patch, run or inspect available checks, verify CI against the exact commit, and update status/decision/evidence documents.
9. Integrate accepted changes through a branch/PR; do not merge to `main` without owner approval.

### Owner — physical-device and product decisions

The owner must supply or confirm facts that cannot be inferred from source code:

- Exact target-device list, model numbers, Android versions/API levels and ABIs.
- Whether API 27 is mandatory for the first release or best-effort.
- Physical-device results for accessibility, voice, permissions, OEM lifecycle and hardware adapters.
- Approval of the final Phase 0 benchmark and risk/approval policy.
- Final authorization to start Phase 1 after reviewing Phase 0 evidence.

## 3. Donor repository pins to revalidate

These are the pins recorded in the destination reuse specification. Re-fetch branch heads and confirm they still match before relying on them.

| Repository | Branch | Recorded commit | Intended use |
|---|---|---|---|
| [agent-created-by-gemini](https://github.com/iam816anonymous-commits/agent-created-by-gemini) | `main` | `90f5cf3ca0eca324f91a43ba96fab72c2e2de5ed` | Primary selective implementation donor |
| [mobile_agent](https://github.com/iam816anonymous-commits/mobile_agent) | `android-local-agent-os-base-65071498209142568` | `863723f7aa4b42137ff4ce07e26e541e402ff5a8` | Modular architecture reference |
| [skill-agent](https://github.com/iam816anonymous-commits/skill-agent) | `phase-0-specifications-14452037546145599308` | `86cfa6960b1341ed53b987f5a50b1c623ec18ad4` | Security/skills specification |
| [agent-1](https://github.com/iam816anonymous-commits/agent-1) | `jules-10149456198484707000-5b5421da` | `6d0c3002fc4c716dea2ffcba31339f0de79fd087` | Supporting domain/evaluation reference |
| [tecno-os](https://github.com/iam816anonymous-commits/tecno-os) | `in6-linux-bringup-infrastructure-4378631429758063630` | `c8a23e359de5b30297ca731f713b89077e056cfc` | Device hardware evidence only |

### Known repository-state changes found on 2026-11-03

- Destination repository `main` HEAD: `5b86207998c66d106a93ae551101e233af090701`.
- `tecno-os` has moved four commits beyond the recorded pin to `d2aeac357141e75b601a935869abb8944ad0d41d`. The four changed files include a new `docs/hardware/gpuprobe-apk-validation-2026-11-02.md`; inspect it and reconcile the evidence. Do not silently update the pin.
- `agent-created-by-gemini` remains at the recorded commit. Its latest visible GitHub Actions run for that commit completed successfully. The workflow runs unit tests and a debug APK build, but the release build is explicitly non-blocking and swallowed with `|| true`; this is not release validation.
- `mobile_agent` has Gradle module build files and wrapper but no recent Actions runs were returned. A clean local build/test is still required.
- `agent-1` latest visible workflow run for the recorded commit failed. Inspect the actual failing job/logs before deciding whether any component remains a candidate.
- `skill-agent` is specification-only at the recorded pin; do not waste time trying to build it as an Android app.

## 4. Required clean-build protocol

For each Android donor:

1. Clone the exact commit into a fresh directory; do not reuse an existing Gradle cache/worktree as the only evidence.
2. Record OS, JDK, Gradle wrapper version, Android SDK/platform/build-tools versions and available disk space.
3. Run the wrapper version and project listing.
4. Run all unit tests.
5. Run lint and available static/security checks.
6. Build the debug APK.
7. Build release only if signing/configuration permits; report signing blockers separately.
8. Inspect dependency graph and native libraries; export a machine-readable dependency report where possible.
9. Preserve full logs and test reports as artifacts.
10. Classify each command PASS / FAIL / BLOCKED / NOT RUN. Never turn a blocked or skipped test into PASS.

At minimum, inspect whether the donor's existing CI truly covers the commands above. A green workflow is evidence only for the steps it actually executes.

## 5. Phase 0 deliverables

Argon must return these artifacts:

- `DONOR_BUILD_TEST_MATRIX.md`
- `DONOR_SOURCE_PROVENANCE_AND_LICENSE_LEDGER.md`
- `DONOR_REVISION_AND_DIFF_REPORT.md`
- `INITIAL_TASK_BENCHMARK.md` (20–30 scenarios)
- `PHASE_0_TECHNICAL_SPIKES.md`
- A concise list of unresolved owner decisions and the exact evidence needed.

Each file must cite exact repository URLs, commit SHAs, file paths and command outputs. Avoid generic statements such as "build works" or "license checked".

## 6. Mandatory architectural review checklist

Argon must explicitly test the proposed design against these failure modes:

- A model returns an unknown action or malformed parameters.
- A model proposes an action requiring an unavailable capability.
- A UI snapshot is stale, incomplete or belongs to another package/window.
- Multiple UI nodes match the same selector.
- The target app hides accessibility semantics or exposes a secure window.
- Permission is revoked after planning but before dispatch.
- User approval is stale or the approved plan changes after approval.
- A non-idempotent action times out after dispatch and may already have succeeded.
- Process death occurs between dispatch and verification.
- An action API returns success but the actual device state does not change.
- Recovery repeatedly fails or exceeds its budget.
- Prompt injection appears in screen text, notifications or app content.
- Logs or memory contain passwords, OTPs, private messages or financial credentials.
- Android 8.1 and modern Android versions expose different API/lifecycle behavior.

Expected behavior is fail-closed, bounded, auditable and honest about uncertain outcomes.

## 7. Phase 1 implementation is not part of the Phase 0 Argon task

Do not create the destination app, Gradle modules, Android services or runtime implementation until the Phase 0 exit checklist is reviewed and passed. The first implementation slice will be the smallest buildable Kotlin shell plus canonical typed contracts and deterministic CI—not a model, wake-word or autonomous feature bundle.

## 8. Argon response format

Return:

1. Executive findings (what is genuinely reusable and what must be rewritten).
2. Exact commands and environment.
3. Test matrix with raw result links/log references.
4. Provenance/licence table.
5. Changed files and patch/commit SHA.
6. Risks and unresolved decisions.
7. Recommended next task, with evidence.
8. Explicit list of work NOT performed.

Do not claim the destination agent is implemented. This is a Phase 0 donor-validation task.
