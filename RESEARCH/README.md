# Research index

**Project progress and next work item:** [PROJECT_STATUS.md](../PROJECT_STATUS.md). This index catalogs research; it is not the progress tracker.

**Last reviewed:** 2026-11-03

Research is organized to separate primary platform rules, repository evidence, experiments, and architectural conclusions.

| Topic | Document | Current conclusion |
|---|---|---|
| Android platform | [Platform constraints](ANDROID_PLATFORM_CONSTRAINTS.md) | API 27 can be a compatibility target, not a universal capability baseline |
| Accessibility policy | [Google Play policy](GOOGLE_PLAY_POLICY_RESEARCH.md) | General-purpose autonomous AccessibilityService control is prohibited for Play apps |
| GitHub references | [Repository audit](OPEN_SOURCE_REPOSITORY_AUDIT.md) + [Deep dive](ANDROID_AUTONOMOUS_AGENT_REPOSITORY_DEEP_DIVE_2026-11-03.md) | 11 additional/expanded repository reviews; distinguish Android app code from host automation and benchmarks |
| Source reuse | [Source-by-source reuse specification](SOURCE_BY_SOURCE_REUSE_SPECIFICATION.md) | Exact retain/adapt/rewrite/reject decisions; implementation not authorized |
| Licensing | [License audit](OPEN_SOURCE_LICENSE_AUDIT.md) | MIT/Apache references are preferred; AGPL and BUSL require separate decisions |
| Inference | [Model research](MODEL_AND_INFERENCE_RESEARCH.md) | Prototype ADK Kotlin + LiteRT-LM and custom adapter before committing |
| Separate future project | [Standalone home-security app research](../ARCHITECTURE/HOME_SECURITY_AND_REMOTE_ACCESS.md) | Reference material only; excluded from this agent's scope and roadmap. A future app may be installed on the same phone independently. |

## Evidence labels

- **Verified:** supported by an official document, repository source, license file, or reproducible experiment.
- **Observed:** directly inspected in source code or tool output, but not yet independently run.
- **Hypothesis:** a design proposal that requires a proof-of-concept.
- **Decision:** accepted project choice recorded in the decision log.

## Research discipline

For every dependency candidate, record repository, exact revision/date, license, Android minimum version, runtime placement, external service dependencies, supported operations, failure modes, and evidence from source/tests. A README is not sufficient evidence for production readiness.
