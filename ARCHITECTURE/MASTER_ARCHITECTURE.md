# Master architecture

**Status:** Proposed architecture for personal-use proof-of-concept.  
**Last updated:** 2026-11-02

## 1. Product and deployment target

This is a personal-use Android agent installed directly on the owner's devices. Google Play publication is not a requirement. Therefore, the design may investigate broader AccessibilityService-based interaction rather than artificially constraining the agent to a Play-compatible subset. Android security boundaries, permissions, privacy and lifecycle limits still apply.

## 2. Architectural principle

Separate decision-making from device actuation. The model proposes bounded actions; deterministic policy code validates them; platform adapters execute them; observers verify postconditions. Never let model output call Android APIs directly.

## 3. Logical architecture

```text
Voice / Text / User UI
        |
Session + Wake-word Manager
        |
Goal / Intent Parser
        |
Task Orchestrator <----> Memory + Execution Journal
        |                       |
Planner / Reflector         Model Router
        |
Policy + Approval Gate
        |
Typed Tool Registry
        |
Action Validator
        |
Android Capability Layer
  |       |          |
Intents  AccessibilityService  System APIs
             |
       UI Tree + Actions
             |
       Screenshot / Vision (optional)
             |
       State Observer
             |
       Postcondition Verifier
             |
       Recovery / Replanning
             |
          User
```

## 4. Android-native modules (proposed)

- `app`: UI, onboarding, settings and session controls.
- `agent-core`: pure Kotlin goal/plan/action/result contracts.
- `agent-runtime`: execution loop, timeout, retries, cancellation, checkpoints.
- `model-api`: provider abstraction, structured generation and streaming.
- `model-adk`: optional ADK/LiteRT-LM experiment.
- `device-capabilities`: API/OEM/permission capability detection.
- `hardware`: hardware inventory, typed hardware adapters, permission/risk gates and hardware-state verification (camera/torch, audio, sensors, haptics, connectivity, location, USB/NFC, power/thermal and storage).
- `accessibility-bridge`: AccessibilityService lifecycle, node snapshots and permitted actions.
- `device-actions`: intents, navigation and system action adapters.
- `observation`: semantic snapshots, screenshot references and app state.
- `memory`: Room-backed task history and user-approved preferences.
- `skills`: typed, permission-scoped workflows.
- `safety`: approvals, risk classification, audit log.
- `evaluation`: deterministic scenarios and trace export.\n- `home-monitor` (optional): event-based camera monitoring, local face detection/recognition, known-person enrolment, unfamiliar-person alerts and retention.\n- `remote-access` (optional): private-network connectivity, authenticated event API, remote device revocation and audit.

These are logical boundaries; avoid premature Gradle module proliferation.

## 5. Interfaces first

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
    suspend fun verify(expected: Postcondition, before: DeviceSnapshot,
                       after: DeviceSnapshot): VerificationResult
}
interface ApprovalGate {
    suspend fun authorize(action: DeviceAction): ApprovalDecision
}
```

Every operation must be cancellable, bounded by timeout and typed. Uncertain results are not success.

## 6. Execution invariant

1. Observe current UI/device state.
2. Select next subgoal.
3. Generate one typed action.
4. Validate schema, capability, permission and risk.
5. Ask approval when required.
6. Execute with timeout/idempotency key.
7. Observe again.
8. Verify expected state transition.
9. Retry, replan or stop with a precise reason.

Persist enough state to recover after process death without blindly replaying non-idempotent actions.

## 7. Personal-use capability profile

- AccessibilityService is a primary candidate for semantic UI observation and interaction.
- App intents and deep links are preferred when deterministic.
- Screenshot/vision is a complementary fallback, subject to capture permissions and secure-window limitations.
- Optional ADB/Shizuku/root profiles may be evaluated separately; never assume they are installed.
- The runtime exposes only capabilities actually available and enabled on the current device.

## 8. Model strategy

Use replaceable model providers. Start with one local model for constrained intent/planning/tool roles. Avoid multi-agent swarm complexity until benchmark evidence proves value. Vision may be a separate provider. Cloud stays disabled unless explicitly configured.

## 9. Data and privacy

Default local storage. Raw audio/screenshots transient by default. User-controlled retention, export and deletion. Redact passwords, OTPs, financial credentials and private message content from logs. Never put secrets into ordinary agent memory.

## 10. Optional home-security subsystem\n\nHome monitoring is a separate subsystem and must not be coupled to the core agent LLM. It uses CameraX/Camera2, motion/frame sampling, face detection, a dedicated face-embedding model and a local known-person registry. Only unrecognised/uncertain events generate snapshots by default. Remote viewing uses a private VPN/overlay network and an authenticated API; no public camera port is exposed. See [HOME_SECURITY_AND_REMOTE_ACCESS.md](HOME_SECURITY_AND_REMOTE_ACCESS.md).\n\n## 11. Decision gates

- ADK Kotlin vs custom runtime: after minimal tool-calling POC.
- Accessibility bridge implementation: after API 27 and modern physical-device tests.
- Minimum SDK: after compatibility experiments.
- Wake-word: after microphone lifecycle and battery tests.