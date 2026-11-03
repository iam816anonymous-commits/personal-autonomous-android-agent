# Voice input and wake-word architecture

## Distinguish voice capture from always-on activation

These are different features and must not be conflated.

### Stage 1: user-initiated speech-to-text

- User taps a microphone control or invokes a platform-supported voice interaction.
- Record audio only during the visible session.
- Convert to text on-device when a compatible speech engine is installed/available; disclose cloud processing if selected.
- Display recognized text before high-impact actions.
- Support cancel, retry, language selection and permission revocation.
- Android's speech recognition services and OEM availability vary; detect capability at runtime.

### Stage 2: wake-word detection

- Separate low-power keyword spotting from full speech recognition.
- Prefer on-device keyword spotting with a short rolling buffer.
- Do not persist raw audio by default.
- Visible status, enable/disable control, battery impact disclosure and emergency stop required.
- Verify lifecycle under screen-off, Doze, app standby, process death, reboot and OEM battery management.
- Do not promise background microphone availability across Android versions.

### Stage 3: task execution

Speech-to-text only produces a candidate user utterance. The orchestrator still parses intent, observes state, validates actions, asks for approval and verifies results.

## API compatibility

Android 8.1/API 27 behavior must be tested on hardware. Modern Android target SDKs impose additional microphone foreground-service type and while-in-use permission rules. On Android 14+ the service start path can fail when microphone permission is not currently available. Do not design a silent background service and expect it to work.

## Initial tests

- tap-to-talk, permission grant/deny, repeated sessions;
- recognition timeout, no speech, noisy environment;
- English, Telugu and Hindi quality;
- network disabled;
- no speech service installed;
- screen lock/background transitions;
- process kill and reboot;
- battery/thermal measurement;
- wake-word false accept/reject rate;
- explicit cancel and microphone indicator behavior.

## Decision

Ship user-initiated speech input before wake-word. Wake-word is a gated feature only after lifecycle, policy and battery evidence.
