# Acceptance criteria

A demo is not a release criterion. Each capability must satisfy observable conditions.

## Cross-cutting
- [ ] Every task has a unique ID and persisted state.
- [ ] User can stop any active task.
- [ ] No model output bypasses tool validation or permissions.
- [ ] All actions return typed results and diagnostics.
- [ ] Task completion requires postcondition evidence.
- [ ] Partial completion and failure are reported honestly.
- [ ] Secrets and sensitive content are redacted.
- [ ] Offline-only mode cannot call cloud endpoints.

## Voice
- [ ] User can start, cancel and restart speech input.
- [ ] Permission denial is handled without a crash or prompt loop.
- [ ] No raw audio retained by default.
- [ ] Offline behavior is explicitly reported.

## Device actions
- [ ] Every target is grounded in a recent observation.
- [ ] Stale target causes re-observation, not blind tap.
- [ ] Mutating external actions require explicit confirmation.
- [ ] Verification catches actions that returned success but did not change state.
- [ ] Unexpected screen stops or replans safely.

## Model management
- [ ] Download is opt-in.
- [ ] Checksum and metadata are verified.
- [ ] Model eligibility uses conservative measured memory limits.
- [ ] Low-memory load fails gracefully and does not crash app.
- [ ] Model can be unloaded and changed.
- [ ] Incompatible models are not offered as downloadable options.

## Resilience and privacy
- [ ] Task recovers or stops safely after process death.
- [ ] No blind replay of non-idempotent actions.
- [ ] User can inspect/delete history and memory.
- [ ] Screenshot/audio retention is off by default.
- [ ] Permissions can be revoked without undefined runtime state.
