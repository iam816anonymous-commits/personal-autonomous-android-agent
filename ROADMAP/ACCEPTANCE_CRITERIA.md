# Acceptance criteria

## Cross-cutting
- [ ] Every task has unique ID and durable state.
- [ ] User can stop every active task.
- [ ] Model output cannot bypass schema, policy, permissions or approvals.
- [ ] Actions return typed results; completion requires postcondition evidence.
- [ ] Partial completion is reported honestly.
- [ ] Secrets and sensitive content are redacted.
- [ ] Offline-only mode cannot call cloud endpoints.

## Voice
- [ ] User can start, cancel and restart speech input.
- [ ] Permission denial causes no crash or prompt loop.
- [ ] Raw audio is not retained by default.
- [ ] Offline behavior is accurately reported.

## Device actions
- [ ] Every target is grounded in a fresh observation.
- [ ] Stale targets trigger re-observation, not blind taps.
- [ ] Mutating external actions require explicit confirmation.
- [ ] Verifier detects actions that return success without changing state.
- [ ] Unexpected screens stop or trigger safe replanning.

## Models
- [ ] Download is opt-in; checksum/metadata verified.
- [ ] Eligibility uses conservative measured RAM/storage constraints.
- [ ] Low-memory failure is graceful.
- [ ] Model can be unloaded and changed.
- [ ] Incompatible models are not offered.

## Resilience and privacy
- [ ] Safe restore/stop after process death; no blind replay of non-idempotent actions.
- [ ] User can inspect/delete history and memory.
- [ ] Screenshot/audio retention off by default.
- [ ] Permission revocation is handled without undefined state.
