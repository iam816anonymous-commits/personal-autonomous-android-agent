# Memory and learning

## Memory classes

1. **Execution journal:** task state, action results and evidence references; transactional and durable.
2. **User preferences:** only explicit or clearly consented durable preferences.
3. **Skill definitions:** versioned, typed workflows with preconditions and verification.
4. **Knowledge/context cache:** bounded, expiring and rebuildable summaries.
5. **Model conversation context:** ephemeral; not a substitute for durable state.

## Do not remember by default

Passwords, OTPs, tokens, private message contents, payment credentials, sensitive personal data, raw audio or full screenshots.

## Learning safely

- Learn from successful task traces only after stripping sensitive data.
- Convert repeated tasks into candidate skills, not automatically executable code.
- Require validation, versioning and user opt-in before a learned skill can perform writes.
- Keep a rollback path and explain what the skill will do.
- Detect app UI changes and invalidate affected selectors.
- Measure skill success, retries, unexpected actions and recovery.

## Persistence

Room is a candidate for local structured persistence. Use schema migrations and encrypted storage only after threat model and key-management design are specified. Do not invent encryption guarantees by merely placing a database in app-private storage.

## User controls

Inspect, edit, export, delete and disable memory. Retention period and telemetry must be configurable.
