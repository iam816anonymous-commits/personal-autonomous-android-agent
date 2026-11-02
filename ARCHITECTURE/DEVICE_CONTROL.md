# Device control and observation

## Principle

Prefer supported, narrow Android integrations over a single universal automation mechanism. All mechanisms are capability adapters, not planner privileges.

## Adapter candidates

1. Explicit Android intents and app links for launching known workflows.
2. App-specific APIs/deep links/integrations where the target app supports them.
3. Accessibility semantics for permitted, narrowly scoped use cases and appropriate distribution profile.
4. Screenshot/visual observation for cases where semantic structure is unavailable, subject to capture permissions and policy.
5. MediaProjection for user-consented screen capture use cases where applicable; not a universal silent screenshot API.
6. Notification actions and widgets where user-visible and supported.
7. Device-owner / managed-device APIs for dedicated enterprise-managed deployments only.
8. ADB/Shizuku/root only as optional personal/developer profiles, never assumed in Play baseline.

## Capability registry

Each capability should declare:
- platform API range;
- OEM/device limitations;
- required permissions and user setup;
- distribution policy status;
- foreground/background availability;
- whether action is read-only or mutating;
- reversibility and risk tier;
- test evidence and known failure modes.

At runtime, the agent sees only capabilities actually available and enabled.

## Grounding

- Prefer stable semantic selectors and explicit app intents.
- Use bounds and screenshots only as snapshot-local evidence; UI coordinates go stale.
- Every target must be grounded in a fresh observation.
- Verify action result and re-observe before subsequent action.
- Never assume a tap worked because the API returned without throwing.

## Device lab

Minimum test matrix:
- Android 8.1/API 27 physical device (legacy baseline).
- One Android 12/13 device (mid-generation permissions and speech behavior).
- One Android 14+ device (modern foreground service/while-in-use behavior).
- Multiple OEM battery modes if possible.

Record model/device/build fingerprint, Android/API level, target SDK, permissions, memory pressure, screen state, network, OEM power mode, action result and verifier outcome.
