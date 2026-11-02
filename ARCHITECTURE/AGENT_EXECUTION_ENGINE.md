# Agent execution engine

## Goal

A deterministic, inspectable runtime that converts a user goal into a bounded sequence of permitted device actions and verifies each outcome.

## State machine

```text
IDLE
  -> CAPTURING
  -> UNDERSTANDING
  -> OBSERVING
  -> PLANNING
  -> VALIDATING
  -> AWAITING_APPROVAL (when needed)
  -> EXECUTING
  -> VERIFYING
       |-> COMPLETED
       |-> REPLANNING -> OBSERVING
       |-> FAILED
       |-> PAUSED
```

All transitions are explicit and persisted. Cancellation is valid from every active state. Process restoration must revalidate the device state before resuming.

## Typed action contract

Every action includes:
- stable action type and schema version;
- arguments with strict validation;
- required device capability and permission;
- risk classification;
- preconditions;
- timeout and retry policy;
- idempotency classification/key;
- expected postcondition;
- user-facing summary;
- evidence reference.

Illustrative Kotlin model:

```kotlin
data class DeviceAction(
    val id: String,
    val type: ActionType,
    val arguments: Map<String, String>,
    val risk: RiskLevel,
    val timeoutMillis: Long,
    val idempotencyKey: String?,
    val expected: Postcondition
)

sealed interface ActionResult {
    data class Success(val evidence: Evidence) : ActionResult
    data class Rejected(val reason: String) : ActionResult
    data class TimedOut(val retryable: Boolean) : ActionResult
    data class Failed(val code: String, val detail: String?) : ActionResult
}
```

## Initial action vocabulary

Only add actions with clear capability boundaries:
- observe current app/device state;
- open an installed app or explicit activity intent;
- tap a grounded visible control where allowed;
- long-press or swipe a grounded visible target where allowed;
- enter text only into an explicitly identified field;
- press Back/Home only through permitted platform mechanisms;
- wait with bounded duration;
- ask user for a choice/approval;
- complete with verified evidence.

Do not initially add arbitrary shell, arbitrary reflection, unrestricted file access, hidden settings changes, payment execution, or message sending without recipient/content confirmation.

## Observation model

A DeviceSnapshot should include:
- timestamp and session ID;
- foreground package/activity where available;
- semantic UI nodes with text redaction, bounds, enabled/clickable state and stable snapshot-local IDs;
- screenshot handle (not necessarily persisted image);
- screen bounds/orientation;
- permission/capability summary;
- confidence and observation errors.

Never pass unbounded raw UI trees directly to a small model. Prune irrelevant nodes and redact sensitive text.

## Recovery

- Stale node: observe again and re-ground target.
- Unexpected screen: stop the current step and replan.
- Timeout: observe before retry; do not blindly repeat.
- App crash: record failure, reopen only when safe, restore task context.
- Permission denied: stop and explain; do not loop permission prompts.
- Low memory: cancel inference/action safely, unload model and report.
- Model invalid output: reject schema and retry with bounded count; no direct execution.
- Verification uncertain: ask user or report partial completion; never mark complete.

## Completion

A task completes only when:
1. all required steps have terminal results;
2. postconditions are verified or explicitly user-confirmed;
3. no unresolved approval is pending;
4. durable execution record is committed;
5. user receives outcome and partial failures.
