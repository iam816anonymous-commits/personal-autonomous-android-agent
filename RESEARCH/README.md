# Research index

**Project progress and next work item:** [PROJECT_STATUS.md](../PROJECT_STATUS.md). This index catalogs research; it is not the progress tracker.

**Last reviewed:** 2026-11-02

Research is organized to separate primary platform rules, repository evidence, experiments, and architectural conclusions.

| Topic | Document | Current conclusion |
|---|---|---|
| Android platform | [Platform constraints](ANDROID_PLATFORM_CONSTRAINTS.md) | API 27 can be a compatibility target, not a universal capability baseline |
| Accessibility policy | [Google Play policy](GOOGLE_PLAY_POLICY_RESEARCH.md) | General-purpose autonomous AccessibilityService control is prohibited for Play apps |
| GitHub references | [Repository audit](OPEN_SOURCE_REPOSITORY_AUDIT.md) | Borrow patterns; most reference runtimes are host/ADB-based |
| Source reuse | [Source-by-source reuse specification](SOURCE_BY_SOURCE_REUSE_SPECIFICATION.md) | Exact retain/adapt/rewrite/reject decisions; implementation not authorized |
| Licensing | [License audit](OPEN_SOURCE_LICENSE_AUDIT.md) | MIT/Apache references are preferred; AGPL and BUSL require separate decisions |
| Inference | [Model research](MODEL_AND_INFERENCE_RESEARCH.md) | Prototype ADK Kotlin + LiteRT-LM and custom adapter before committing |
| Home security | [Home security and remote access](../ARCHITECTURE/HOME_SECURITY_AND_REMOTE_ACCESS.md) | Separate CV pipeline needed; ML Kit detects but does not recognise people; camera uptime and secure remote access require physical validation |

## Evidence labels

- **Verified:** supported by an official document, repository source, license file, or reproducible experiment.
- **Observed:** directly inspected in source code or tool output, but not yet independently run.
- **Hypothesis:** a design proposal that requires a proof-of-concept.
- **Decision:** accepted project choice recorded in the decision log.

## Research discipline

For every dependency candidate, record repository, exact revision/date, license, Android minimum version, runtime placement, external service dependencies, supported operations, failure modes, and evidence from source/tests. A README is not sufficient evidence for production readiness.
