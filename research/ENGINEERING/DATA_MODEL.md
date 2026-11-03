# Local data model

## Proposed tables

- `task_sessions`: ID, goal summary, input modality, language, state, created/updated/completed timestamps, plan version.
- `task_steps`: ID, task ID, sequence, subgoal summary, state, retry count, expected postcondition, result code.
- `action_attempts`: ID, step ID, action type, redacted argument summary, risk tier, idempotency key, start/end time, result, evidence reference.
- `approval_events`: ID, action digest, summary, decision, created/expiry time.
- `user_preferences`: key, typed value, consent/source metadata, updated time.
- `skills`: ID, name, version, definition, enabled flag, risk scope, validation state.
- `model_artifacts`: ID, version, source, checksum, size, license, compatibility metadata, verified state.
- `model_metrics`: model ID, device profile, peak memory, latency, throughput, thermal result, test date.
- `capability_registry`: capability ID, OS/API range, permission state, distribution profile, enabled state.

## Retention

Default: retain task summaries and structured results; do not retain raw audio/screenshots unless user enables a specific debugging capture. Redact arguments. Provide deletion and export. Define migrations before implementation.
