# Local data model

## Proposed tables

- `task_sessions` (**implemented in schema v3**): task ID, AES-GCM encrypted goal and terminal reason, input modality, lifecycle state, and created/updated epoch timestamps. Goal text is decrypted only through the journal boundary. Language and plan version remain future fields.
- `task_steps`: ID, task ID, sequence, subgoal summary, state, retry count, expected postcondition, result code.
- `action_attempts`: ID, step ID, action type, redacted argument summary, risk tier, idempotency key, start/end time, result, evidence reference.
- `approval_events`: ID, action digest, summary, decision, created/expiry time.
- `user_preferences`: key, typed value, consent/source metadata, updated time.
- `skills`: ID, name, version, definition, enabled flag, risk scope, validation state.
- `model_artifacts`: ID, version, source, checksum, size, license, compatibility metadata, verified state.
- `model_metrics`: model ID, device profile, peak memory, latency, throughput, thermal result, test date.
- `capability_registry`: capability ID, OS/API range, permission state, distribution profile, enabled state.

## Retention

Default: retain task summaries and structured results; do not retain raw audio/screenshots unless user enables a specific debugging capture. Task goal/reason and journal notes are encrypted with Android Keystore AES-GCM; redact arguments. Provide deletion and export. Room schema v3 adds task_sessions through migration 2→3; migration execution still needs instrumentation coverage on a physical device.
