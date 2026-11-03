# Device control and observation

## Principle

For this personal sideloaded agent, AccessibilityService is a primary candidate for observing and interacting with Android app UIs. Keep all device control behind narrow capability adapters. The model does not receive direct Android API access.

## Adapter priority

1. **AccessibilityService:** UI tree, node metadata, bounds, supported click/long-click/scroll/text actions and global navigation where available.
2. **Explicit Android intents/app links:** preferred for deterministic launch and known workflows.
3. **App-specific APIs/deep links:** when the target app provides them.
4. **Screenshot/visual observation:** fallback for custom views or incomplete semantics; respect secure windows and capture permissions.
5. **MediaProjection:** user-consented screen capture where applicable.
6. **Notification actions/widgets:** where supported.
7. **ADB/Shizuku/root:** optional advanced personal/developer profiles with explicit setup; not baseline.

## AccessibilityService implementation requirements

- User explicitly enables service in Android Settings.
- Explain exactly what UI information can be observed and what actions can be performed.
- Provide visible service status, pause/stop and emergency kill switch.
- Take fresh UI snapshots before selecting targets.
- Treat node IDs and coordinates as snapshot-local and stale after transitions.
- Bind every mutating accessibility target to a short-lived snapshot lease. Before click/text dispatch, re-observe the foreground tree and require the same package and semantic fingerprint; reject expired, incomplete, evicted or changed snapshots. Never treat a dispatch-time node lookup alone as proof that the original screen is still active.
- Node identifiers include semantic role and visible non-secret labels; password contents/descriptions must not contribute to target identifiers.
- Re-observe after each action and verify expected postcondition.
- Redact sensitive UI fields before model input/logging.
- Never use the service to bypass authentication, steal secrets or evade app security controls.

## Capability registry

Each capability declares API range, OEM limitations, permissions/setup, foreground/background behavior, read/write status, risk tier, reversibility and test evidence. At runtime expose only capabilities actually available and enabled.

## Grounding and recovery

- Prefer semantic selectors and explicit intents.
- Use coordinates only with a fresh screenshot and confidence threshold.
- If a node becomes stale, observe again; do not blindly retry.
- If the screen differs from expectation, stop and replan.
- Verify action result and state transition before next action.

## Device lab

Test Android 8.1/API 27 physical device, Android 12/13, Android 14+ and multiple OEM battery modes. Record build fingerprint, API, target SDK, permissions, memory pressure, screen/network state, OEM power mode, action and verifier result.