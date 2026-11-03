# Performance budgets

Budgets are provisional and must be calibrated from actual test devices.

## Required device profiles

- Legacy: Android 8.1/API 27, lower RAM and slower CPU.
- Mid-range: Android 12/13.
- Modern: Android 14+ with current target SDK behavior.

## Measure before optimizing

- APK size and module contribution;
- app cold-start time;
- service start/stop latency;
- voice capture and transcription latency;
- model cold-load/warm-load time;
- peak PSS/RSS under realistic context length;
- tokens/sec and time to first token;
- screenshot capture and UI tree processing time;
- action execution and verification latency;
- idle/burst battery drain and thermal state.

## Safety margin

Do not allocate all free RAM to model inference. Reserve memory for OS, foreground app, agent process, image buffers and recovery. Model eligibility thresholds must be derived from physical measurements and must account for memory pressure changing after the UI offers the model.
