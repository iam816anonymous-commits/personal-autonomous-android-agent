# Master architecture

**Status:** Proposed architecture for proof-of-concept, not an implementation commitment.  
**Last updated:** 2026-11-02

## 1. Architectural principle

Separate decision-making from device actuation. The model may propose a bounded action; deterministic policy code validates it; a platform adapter performs it; an observer verifies the postcondition. Never allow model output to directly call Android APIs.

## 2. Logical components

```text
User (voice / text / explicit UI)
             |
       Session Manager
             |
      Goal / Intent Parser
             |
      Policy + Approval Gate
             |
       Task Orchestrator
       /      |       \
   Planner  Memory   Model Router
       \      |       /
      Execution Journal
             |
      Typed Tool Registry
             |
       Action Validator
             |
      Android Capability Layer
       /        |          \
  App Intents  UI Adapter  System APIs
                  |
           Accessibility / visual
           observation where permitted
                  |
             State Observer
                  |
       Postcondition Verifier
                  |
       Recovery / Reflector
                  |
             User result
```

## 3. Android-native application modules

Proposed module boundaries:
- `app`: UI, onboarding, user session, settings.
- `agent-core`: goal, plan, step, policy, action/result contracts; pure Kotlin where possible.
- `agent-runtime`: orchestration loop, timeouts, retries, cancellation and checkpoint restore.
- `model-api`: inference provider abstraction, streaming and structured generation.
- `model-adk`: optional ADK/LiteRT-LM integration experiment.
- `device-capabilities`: feature detection, permission status and platform version adapters.
- `device-actions`: approved app intents, system actions and narrow UI actions.
- `observation`: accessibility semantic snapshots, screenshots and app state.
- `memory`: Room-backed execution history, user-approved facts and compact summaries.
- `skills`: typed, permission-scoped task capabilities.
- `safety`: approval decisions, sensitive-action taxonomy, audit log.
- `evaluation`: deterministic test scenarios and trace export.

These are logical boundaries; avoid creating Gradle modules prematurely before package contracts stabilize.

## 4. Interfaces first

```kotlin
interface ModelProvider {
    suspend fun generate(request: ModelRequest): ModelResponse
    fun capabilities(): ModelCapabilities
}

interface DeviceObserver {
    suspend fun observe(scope: ObservationScope): DeviceSnapshot
}

interface ActionExecutor {
    suspend fun execute(action: DeviceAction): ActionResult
}

interface ActionVerifier {
    suspend fun verify(
        expected: Postcondition,
        before: DeviceSnapshot,
        after: DeviceSnapshot
    ): VerificationResult
}

interface ApprovalGate {
    suspend fun authorize(action: DeviceAction): ApprovalDecision
}
```

Every operation must be cancellable, bounded by timeout, and return a typed result. A missing or uncertain result is not success.

## 5. Execution invariant

One action at a time:
1. Observe current state.
2. Select the next subgoal.
3. Generate one typed action.
4. Validate schema, capability, permission and risk.
5. Ask user approval when required.
6. Execute with timeout and idempotency key.
7. Observe again.
8. Verify the expected state transition.
9. Retry, re-plan or stop with a precise reason.

The runtime records enough state to resume safely after process death, without blindly replaying a non-idempotent action.

## 6. Model strategy

Use an interchangeable model provider. Start with one local model serving constrained intent/planning/tool roles. Avoid multi-agent swarm complexity until a benchmark proves it improves completion rate. Vision may be a separate provider. Cloud is disabled unless user configures it.

## 7. Deployment profiles

- Personal sideloaded: optional advanced capabilities subject to OS and user setup.
- Play-compatible: only flows reviewed against Play policy; do not assume broad autonomous AccessibilityService use is allowed.
- API 27 compatibility: explicit capability downgrade and physical-device validation; no promise of feature parity with modern Android.

## 8. Data and privacy

Default to local storage. Keep raw audio/screenshots transient. User-controlled retention, export and deletion. Redact passwords, OTPs, financial/payment data and private message content from logs unless essential and explicitly authorized. Secrets never enter ordinary agent memory.

## 9. Architecture decision gates

- ADK Kotlin vs custom runtime: after minimal tool-calling proof-of-concept.
- Accessibility vs alternative controls: after distribution-profile decision.
- Minimum SDK: after API 27 and modern-device compatibility experiments.
- Wake-word architecture: after microphone lifecycle and battery tests.
