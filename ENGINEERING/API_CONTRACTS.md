# Engineering API contracts

**Status:** Draft contracts; refine after proof-of-concept.

## Core types

- Goal: normalized objective, source (voice/text), language, constraints, session ID.
- Plan: ordered subgoals, success criteria, risk summary, version.
- DeviceSnapshot: timestamp, foreground context, semantic node set, screenshot handle, permission/capability snapshot.
- DeviceAction: finite action type, validated arguments, risk, required capability, timeout, idempotency key, expected postcondition.
- ActionResult: success/rejected/timeout/failure plus evidence and diagnostics.
- VerificationResult: verified/not-verified/uncertain with evidence.
- ApprovalDecision: approved/rejected/expired/cancelled bound to action digest.
- TaskRecord: durable state, timestamps, attempts, outcome, failure reason.

## Invariants

- All external/model output is parsed into strict typed structures.
- Unknown action types are rejected.
- Every action has a timeout and cancellation path.
- Every mutating action has a postcondition.
- Approval is checked at execution time, not just plan time.
- Retry is preceded by a new observation.
- A task is never complete without verifier evidence or explicit human acknowledgment.
- Sensitive values are redacted from logs and memory.

## Versioning

Persist schema version and support migrations. Model prompts and tool definitions must be versioned and included in trace metadata so task regressions can be reproduced.
