# Model and inference research

**Last reviewed:** 2026-11-02  
**Status:** Options under evaluation; no model selected.

## Product requirements

- Offline-capable for a defined subset of tasks, not falsely advertised as universally autonomous offline.
- Model selection based on actual free RAM, total memory class, storage, ABI, accelerator/runtime support, context length, KV cache, and measured peak working set.
- The app must never offer a model that fails a conservative device eligibility check.
- Show download size, estimated disk footprint, supported languages, runtime/accelerator compatibility and measured peak RAM.
- Download only after explicit user action; verify checksum and model metadata before activation.
- Keep inference provider replaceable: on-device first, optional cloud fallback only when enabled.
- Do not assume a model's parameter count or file size predicts its runtime memory accurately.

## High-priority candidate: ADK for Kotlin 1.0

Google announced ADK for Kotlin 1.0 on 2026-09-09, describing Android-first on-device extensions, LiteRT-LM and ML Kit integrations, and state persistence options. Official announcement: https://developers.googleblog.com/announcing-adk-for-kotlin-10-building-production-ready-ai-agents-in-kotlin-android-and-beyond/

Research tasks:
1. Inspect https://github.com/google/adk-kotlin and its Android samples.
2. Determine minimum SDK, Kotlin/AGP constraints, binary size, transitive dependencies and license.
3. Build a minimal tool-calling agent in a clean Android project.
4. Test LiteRT-LM on the intended device classes.
5. Verify process recreation, cancellation, streaming, tool errors and fallback behavior.
6. Compare its persistence approach with a project-owned Room execution journal.

Do not infer that every model/runtime combination supports tool calling. Test each exact model artifact.

## Candidate runtimes / approaches

- LiteRT-LM: investigate local model execution and structured tool interaction.
- ML Kit GenAI / Gemini Nano: investigate device support, API/device availability and exact tool-calling limitations for selected version.
- Ollama: useful for workstation experimentation, not assumed to run inside the APK.
- llama.cpp / GGUF: consider as an alternate mobile inference backend if compatible Android builds, kernels and licenses work for target devices.
- Remote inference: optional provider adapter; data transfer must be visible and controlled.

## Device eligibility algorithm (proposed)

Eligibility must be based on measured peak process behavior, not static marketing RAM:
1. Read device memory class and current available memory.
2. Read app storage availability and model file size.
3. Load a model in an isolated process or controlled test harness.
4. Measure peak PSS/RSS, startup latency, token throughput, context/KV usage, thermal behavior and crash rate.
5. Apply safety margin for Android system pressure and concurrent app usage.
6. Expose model only if device has sufficient headroom under conservative thresholds.
7. Recheck at load time; unload gracefully when pressure signals require it.

The app must not treat vendor "extended RAM"/virtual RAM as equivalent to physical memory. Dynamic RAM/swap may improve capacity but can be much slower and may not be safely counted toward peak model memory.

## Model role separation

One modest local model may handle intent normalization, constrained planning and tool selection. A larger optional model may improve complex tasks. Vision may use separate image-capable inference. Keep these as distinct capabilities rather than assuming one model supports everything.

## Measurements for every candidate

- Exact model and quantization revision;
- license and source URL;
- download size and extracted size;
- runtime and accelerator;
- minimum Android/device requirements;
- peak PSS/RSS over repeated cold/warm runs;
- load/unload time;
- tokens/sec and time to first token;
- context length and KV-cache footprint;
- tool-call validity rate;
- task success on the same fixed test set;
- power/thermal impact;
- offline behavior and language support.

## Decision gate

No model is chosen until it completes the same reproducible benchmark on a physical target device, with peak memory and action success recorded.


## TECNO IN6 Vulkan evidence update — 2026-11-03

The latest device-side APK report confirms ordinary-app Vulkan 1.0.58 compute on the Mali-G71 for vector-add workloads up to 1,048,576 float elements, with zero reported incorrect outputs. This is a useful compute capability signal, but it is **not** evidence that a neural-network inference runtime or any specific model backend works.

OpenCL remains unavailable to the tested ordinary APK because the vendor library is outside its linker namespace. Shell-level OpenCL tests are not representative of a normal sideloaded application.

Do not select Vulkan as the model runtime until a small neural-network proof-of-concept demonstrates supported operators, numerical correctness, model load success, peak process memory, latency and thermal stability. The source report also identifies a command-buffer one-time-submit reuse issue; repeated workload timings remain provisional pending correction and rerun.

Source: [TECNO IN6 GPUProbe APK validation report](https://github.com/iam816anonymous-commits/tecno-os/blob/in6-linux-bringup-infrastructure-4378631429758063630/docs/hardware/gpuprobe-apk-validation-2026-11-02.md).
