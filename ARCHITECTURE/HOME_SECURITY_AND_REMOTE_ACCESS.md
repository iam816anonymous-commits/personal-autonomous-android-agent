# Home Security Monitoring and Secure Remote Access

**Status:** Proposed optional subsystem; not implemented or physically validated.  
**Product purpose:** Turn an owner-controlled Android device left at home into a privacy-conscious event-based security monitor that can be checked remotely.

> This is a separate capability from the autonomous phone-control agent. It must not be treated as an extra prompt or responsibility of the general-purpose LLM.

## 1. User requirement

- Leave a supported Android device at home, powered and connected to the internet.
- Detect relevant activity and faces without continuously recording or retaining video.
- Enrol household members or other authorised regular occupants so they do not generate repeated unfamiliar-person alerts.
- When an unfamiliar face is detected, capture a small event snapshot and notify the owner.
- Let the owner securely view device status and retrieve/delete event snapshots while away.
- Keep recognition, matching and event filtering on the home device where feasible.
- Provide a clear pause/disable control, visible camera-use status, retention settings and an audit history.

## 2. Feasibility verdict

**Feasible as a prototype, but not with the project's current general-purpose LLM alone.** The project has not selected or benchmarked a production model/runtime yet. The current ADK Kotlin, LiteRT-LM, llama.cpp and small language-model candidates are for reasoning/tool use; they are not a validated face-recognition pipeline.

Use a dedicated computer-vision pipeline:

1. Camera capture through CameraX / Camera2.
2. Motion/activity trigger or low-rate frame sampling to avoid analysing every frame.
3. Face detection to locate faces.
4. Face alignment and a dedicated face-embedding model (for example, a TFLite-compatible FaceNet/MobileFaceNet/ArcFace-family candidate) to create an embedding.
5. Compare against locally enrolled, consented household profiles using a calibrated threshold and multiple observations.
6. If confidently matched to an enrolled person, suppress a new-person alert and discard the frame unless a user-configured event requires otherwise.
7. If no match is reliable, create an **unrecognised face event**, capture a bounded snapshot, notify the owner and apply retention rules.

Google ML Kit Face Detection can locate faces and track them across frames, but its own documentation explicitly says it does not recognise individuals. It can be used for detection, not as the known-person identity engine. A separate embedding model is required. See [ML Kit face detection](https://developers.google.com/ml-kit/vision/face-detection) and [Android implementation](https://developers.google.com/ml-kit/vision/face-detection/android).

Do not use the language model to infer identity from a photo. Do not query external face databases or attempt to identify unknown visitors by name. The supported output for a non-enrolled person is simply **unrecognised person detected**.

## 3. Known-person enrolment and alert policy

- Enrol a person only through an explicit owner-controlled flow, with that person's informed agreement.
- Capture multiple good-quality reference samples in varied lighting/angles; one selfie is not a robust enrolment set.
- Store face embeddings rather than a large gallery of source photos where practical. Protect embeddings and any retained reference images as sensitive personal data.
- Keep the known-person allowlist local to the home device and encrypted at rest.
- Use conservative matching thresholds calibrated on real home-camera footage. Similarity thresholds are model-specific and must not be guessed.
- If confidence is ambiguous, classify as **unknown/uncertain**, not as a known person.
- Do not automatically enrol a visitor because they appear repeatedly. Let the owner explicitly review and enrol a regular person, after obtaining agreement.
- Deduplicate by event/session and cooldown, not by every video frame. A single person lingering in view should not produce hundreds of alerts.
- Support temporary allowlist entries, revocation, deletion and re-enrolment.
- Never use face matching as the sole basis for unlocking doors, granting access, accusing someone or triggering physical action.

## 4. No-continuous-recording design

Default processing policy:

- Use a short in-memory frame buffer only; no continuous video archive.
- Run motion/activity filtering and face detection locally.
- Discard frames containing only confidently recognised household members, unless the owner explicitly configures a different event.
- For an unfamiliar/uncertain face, save only a selected event snapshot (optionally a short bounded burst if the owner enables it).
- Do not store audio by default.
- Configure event retention with an expiry; allow immediate per-event deletion and bulk deletion.
- Store event metadata separately from image files; avoid face embeddings for unknown people unless a separately justified, time-limited deduplication design is approved.
- Make the camera active state visible on the device and provide a physical/obvious way to pause monitoring.

Face recognition is probabilistic. Lighting, camera angle, masks, motion blur, distance, age changes, twins/lookalikes and low resolution can cause false matches or missed matches. A false match could suppress an alert; therefore ambiguous cases must fail closed to **unknown/uncertain**. The system is an alerting aid, not a certified alarm or access-control system.

## 5. Android camera and lifecycle constraints

Camera monitoring is the largest platform risk. A normal Android app cannot assume it can silently open the camera at any time or restart camera access after process death.

- Camera permission and user-initiated setup are required.
- On Android 14/API 34+ when targeting the corresponding SDK, camera foreground-service type and `FOREGROUND_SERVICE_CAMERA` requirements apply. Camera is a while-in-use permission; a camera foreground service generally needs to be started while the app has a visible activity, subject to narrow platform exemptions.
- Android 12+ restricts starting foreground services from the background. Android 14+ can reject a background start of a service that needs camera while-in-use access.
- A camera foreground service must show the required ongoing notification. No stealth capture or hidden camera mode.
- Android 8.1/API 27 has different service rules, but OEM battery management, process death, thermal limits and camera contention still need physical-device testing.
- Sideloading does not remove Android camera permission, foreground-service, privacy indicator, secure-window or OEM restrictions.
- Reboot recovery must not assume camera access can be silently restarted. Require a tested, user-authorised recovery flow and show monitoring as offline if the service is no longer active.

Official references: [foreground-service restrictions](https://developer.android.com/develop/background-work/services/fgs/restrictions-bg-start), [foreground-service changes by Android version](https://developer.android.com/develop/background-work/services/fgs/changes), and [CameraX](https://developer.android.com/training/camerax).

**Practical recommendation:** use a dedicated, supported Android device that can remain powered and run a visible camera foreground service. Do not assume an old spare phone or a phone with aggressive OEM battery management will be reliable enough for security monitoring. Test screen-off, charging, thermal state, Wi-Fi loss, process kill, reboot and overnight uptime before relying on it.

## 6. Secure remote access

Preferred first implementation: private-network access rather than exposing an HTTP server or camera stream to the public internet.

- Use a private VPN/overlay network such as Tailscale or a properly configured WireGuard network between the home device and the owner's remote device.
- Do not expose camera, admin or file endpoints through router port forwarding by default.
- VPN membership alone is not the application authorisation layer: enforce device allowlists, strong account authentication/MFA where available, per-user permissions and explicit session expiry.
- Use HTTPS/TLS even over the private network; authenticate API requests and protect against replay.
- Encrypt event images and face templates at rest using Android Keystore-backed key management where supported.
- Keep remote operations read-only by default: view status, fetch an alert snapshot, acknowledge and delete. Remote camera configuration changes require re-authentication.
- Maintain an access audit log and notify the owner of new device enrolments or security-setting changes.
- Provide immediate remote disable/revoke for a lost phone, compromised account or stolen home device.
- Never embed reusable passwords, VPN keys or API secrets in source code or the APK.

Tailscale's documentation explains that its network connects devices behind firewalls but does not itself provide the application service; the agent still needs a separately authenticated local API. See [Tailscale device connectivity](https://tailscale.com/docs/how-to/connect-to-devices).

### Remote notification options

1. **VPN pull:** the owner's app connects to the home device over the private network and fetches new events. Strong privacy and no public camera endpoint, but notifications require the owner device to maintain/check the VPN.
2. **Minimal push relay:** send only an opaque event ID and minimal alert metadata through a push service; the image remains on the home device and is fetched through the private VPN. This is more convenient but introduces a cloud notification dependency and must be disclosed.
3. **Encrypted relay:** if direct VPN reachability is unavailable, use a reviewed relay design with end-to-end encryption, short-lived tokens and no plaintext image storage. This is a later phase, not the first implementation.

Do not send full-resolution photos to a third-party cloud by default.

## 7. Proposed module boundaries

- `home-monitor`: monitoring session state, configuration and event lifecycle.
- `camera-capture`: CameraX/Camera2 adapter and lifecycle/permission checks.
- `activity-detection`: motion/frame sampling and event gating.
- `face-detection`: face bounding boxes and quality checks (ML Kit or another validated detector).
- `face-recognition`: embedding model, enrolment, local matching, threshold calibration and known-person registry.
- `security-events`: unknown/uncertain event creation, deduplication, snapshots, retention and deletion.
- `remote-access`: private network connectivity, authenticated API, session control and audit.
- `notifications`: optional push metadata relay; never required for local inference.

The general agent may expose typed tools such as `monitoring_status`, `pause_monitoring`, `list_security_events`, `fetch_event_snapshot`, `delete_security_event` and `revoke_remote_device`. These tools must pass deterministic policy and authentication checks. The LLM must not receive direct camera frames or face embeddings by default.

## 8. Threat model and privacy requirements

Threats include theft of the home device, compromise of the owner's remote account, VPN peer compromise, unauthorised household access, image/embedding leakage, false identity matches, alert flooding and service interruption.

Required controls:

- Explicit notice to household members and regular occupants; obtain informed agreement before enrolling their faces. Visitors should be informed that the area is monitored where appropriate.
- No covert monitoring, hidden recording, audio capture or external identity search.
- Least-privilege network access, MFA, key rotation/revocation and encrypted transport/storage.
- Separate owner authentication from household face recognition. Recognising a face must not authenticate a remote user.
- Short configurable retention, default auto-expiry, deletion controls and access logs.
- Local processing by default; disclose any cloud processing or push metadata.
- No face template or unknown-person image used to train models or shared for unrelated purposes.
- A clear monitoring indicator, pause control and status heartbeat so the owner can distinguish **no events** from **device offline**.
- Review applicable Indian privacy obligations before deployment. India's DPDP Act 2023 and DPDP Rules 2025 establish a data-protection framework; applicability and commencement provisions depend on the actual use and timeline. This document is engineering guidance, not legal advice. See [India Code Act](https://www.indiacode.nic.in/indiacode/handle/123456789/22037?view_type=browse) and [MeitY DPDP Rules](https://www.meity.gov.in/documents/act-and-policies/digital-personal-data-protection-rules-2025-gDOxUjMtQWa).

## 9. Model and hardware decision

Do not select a face-recognition model yet. Candidate evaluation must compare:

- Face detection quality and runtime cost at realistic camera resolutions.
- Face embedding model licence, provenance, Android ABI/runtime support and TFLite/LiteRT compatibility.
- False-match and missed-match rates on a consented test set representative of the camera position and lighting.
- CPU/NPU performance, peak PSS/RSS, temperature and battery/charging stability.
- Minimum face pixel size and recognition performance at the actual mounting distance.
- Resistance to simple photo/video spoofing; document limitations rather than claiming liveness detection.
- Model file size, cold-start time, throughput and offline operation.

Run recognition in a separate bounded inference pipeline from the general LLM. The LLM is optional for summaries or natural-language explanation of events; it is not in the real-time detection path.

## 10. Acceptance tests

- Camera permission denied/revoked; monitoring must stop cleanly and report unavailable.
- Service started by user while app is visible; screen-off operation tested on each target OS/OEM.
- Process kill, reboot, battery optimisation, thermal throttling, charger disconnect and Wi-Fi loss.
- Known enrolled person across lighting, angles and distances; alert suppression measured.
- Unenrolled visitor; one event alert, not one alert per frame.
- Ambiguous match; must be classified unknown/uncertain, never confidently familiar.
- Multiple people in frame; each face event handled without duplicate flood.
- Printed photo/video replay test; document spoof limitations.
- Delete one snapshot, delete all events, revoke an enrolled identity and verify file/embedding deletion.
- Remote access from a non-home network; unauthorised peer denied; revoked peer denied; no public endpoint exposed.
- Verify image encryption at rest, TLS transport, request authentication, replay protection and access audit.
- Distinguish quiet/zero-event state from camera stopped, app crashed, device offline and notification delivery failure.
- Confirm no continuous video/audio is retained by default.

## 11. Delivery gates

1. **Research spike:** select camera target device and test visible camera foreground-service uptime.
2. **Detection prototype:** camera + motion sampling + face detection only; no remote access and no identity matching yet.
3. **Recognition prototype:** consented known-person enrolment, embedding generation, local matching and measured false-match/false-reject rates.
4. **Event engine:** unknown/uncertain event snapshots, deduplication, retention and deletion.
5. **Secure remote access:** private VPN, authenticated read-only event API, device revocation and audit.
6. **Notifications:** VPN pull first; optional minimal push relay only after privacy/security review.
7. **Hardening:** physical-device overnight and network-failure tests, security review and documented operating limits.

**Not a first-release promise:** guaranteed 24/7 camera uptime, certified alarm performance, perfect face recognition, automatic identification of strangers, or secure access without a working network.

## 12. Decision

Adopt this as a proposed optional home-security workstream. Keep it separate from the autonomous agent's core execution phases. It depends on the Android shell, camera/hardware capability foundation, privacy/security controls and physical-device validation. Do not start implementation until the owner explicitly authorises the implementation phase.