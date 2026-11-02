# Model router

## Responsibilities

Select an inference provider and model based on task requirements, user settings, capability availability, device resources and privacy mode.

## Provider categories

- Local text LLM;
- local vision-language model;
- OS-provided/on-device model API;
- optional remote/cloud LLM;
- deterministic parser/rules fallback.

## Routing inputs

- task category and complexity;
- required modality;
- tool-calling/structured-output capability;
- language;
- context budget;
- current free RAM and device memory class;
- model disk footprint and available storage;
- runtime/ABI/accelerator support;
- thermal and battery state;
- user privacy/offline setting.

## Safety rules

- User's offline-only setting is absolute.
- Never silently route private UI content to cloud.
- Never download a model without explicit opt-in.
- Never expose an incompatible model for download or activation.
- Never treat dynamic/virtual RAM as equivalent to physical RAM.
- Keep a reserve for OS and app operation; recheck at load time.
- If no eligible model is available, degrade to deterministic/user-guided behavior.

## Model manifest

Each artifact requires:
- name/version/revision;
- source and download URL;
- cryptographic checksum;
- license;
- quantization and architecture;
- tokenizer/config metadata;
- file and extracted size;
- supported languages/modalities;
- runtime and accelerator;
- tested Android/API/device set;
- measured peak PSS/RSS and throughput;
- tool-call schema test results;
- safety notes.

## Fallback order

1. selected eligible local model;
2. other locally installed eligible model with required capabilities;
3. deterministic intent grammar / user-guided flow;
4. cloud only if user expressly configured it for the current privacy mode;
5. fail with an honest reason.

## Evaluation

Router correctness is measured by crash-free load, memory margin, correct capability matching, task success, privacy compliance and fallback behavior—not by model size alone.
