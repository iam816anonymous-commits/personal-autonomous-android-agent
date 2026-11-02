# Test strategy

## Test layers

1. Pure unit tests: parser, plan validation, state machine, action schemas, risk classification, approval expiry, router eligibility.
2. Contract tests: model provider output, tool registry, device capability adapters.
3. Instrumentation tests: permission lifecycle, Activity/Service lifecycle, persistence and process recreation.
4. Physical-device acceptance: actual navigation/actions across target OS and OEM builds.
5. Resilience: process kill, app crash, screen lock, stale UI, timeout, low memory, offline mode, denied permission, model invalid output.
6. Security: prompt injection in screen text, secret redaction, unauthorized action, recipient substitution, approval replay, arbitrary tool requests.
7. Performance: cold load, peak PSS/RSS, tokens/sec, latency, battery, thermal behavior.

## Physical-device policy

The physical device is authoritative for OEM behavior. Do not create slow or flaky emulator jobs and then claim they prove microphone, background service, accessibility or model behavior on hardware. Keep CI deterministic and run on-device acceptance as a separate, explicit gate.

## Task benchmark

Build 20–30 initial tasks, grouped by:
- app launch and navigation;
- read-only information retrieval;
- draft creation without submission;
- reversible settings within permitted scope;
- cross-app simple tasks;
- interrupted task recovery;
- memory and follow-up reference;
- refusal / approval cases.

Each scenario must define initial state, user goal, allowed capabilities, expected final state, forbidden side effects, timeout and evidence collection.

## Metrics

- task success rate;
- postcondition verification rate;
- harmful/unauthorized action rate (target zero);
- unnecessary action count;
- recovery success rate;
- average and p95 time;
- crash-free model load;
- peak memory margin;
- permission denial handling;
- offline completion rate;
- user approval correctness.
