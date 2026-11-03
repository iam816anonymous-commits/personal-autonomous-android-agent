# Open-source license audit

**Last reviewed:** 2026-11-02  
**Purpose:** Engineering triage, not legal advice. Confirm the exact LICENSE file and dependency licenses at the commit actually used.

| Repository | License observed in research | Initial reuse posture |
|---|---|---|
| MobileUse | MIT | Generally permissive; retain notices and verify dependencies |
| Mobilerun core | MIT | Generally permissive; inspect dependencies and Portal separately |
| Mobilerun Portal | AGPL-3.0 | High legal review priority; network/copy-left obligations may affect distribution |
| AndroidWorld | Apache-2.0 | Permissive with NOTICE/patent/attribution conditions |
| OpenGUI | BUSL-1.1, change date 2030-04-29 to Apache-2.0 | Do not incorporate into a commercial product without explicit license review/permission |
| UI-TARS Desktop | Apache-2.0 | Permissive subject to license/NOTICE/patent terms |
| LangGraph | MIT | Generally permissive |
| Google ADK for Kotlin | Verify exact repository license before adoption | Candidate; do not assume |
| Pydantic AI | MIT | Generally permissive |
| Microsoft Agent Framework | MIT | Generally permissive |

## Rules before code reuse

1. Pin the exact repository commit SHA and archive the LICENSE file used for review.
2. Inspect source headers, bundled assets, model weights, datasets, prompts, fonts, icons and videos separately. Repository license may not cover all assets.
3. Generate a dependency SBOM and retain third-party notices.
4. Check model weights and tokenizer licenses independently of inference-runtime code.
5. Do not copy AGPL components into a proprietary app without a deliberate legal decision.
6. Do not treat source-available BUSL as permissive open source. OpenGUI's stated production/commercial restrictions are material.
7. Use ideas and reimplement interfaces when direct code reuse creates unacceptable obligations.

## Initial preferred strategy

Prefer first-party Kotlin implementation for the Android device layer. Borrow MIT/Apache patterns for orchestration and testing after source and dependency checks. Keep experimental Python reference systems isolated from the shipped APK.
