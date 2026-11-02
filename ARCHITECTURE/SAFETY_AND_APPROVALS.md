# Safety, permissions and approvals

## Core rule

Model output is untrusted input. Deterministic code enforces permissions, allowed tools, risk limits, schemas and approvals.

## Risk tiers

| Tier | Examples | Default handling |
|---|---|---|
| 0 – read-only | inspect visible app, summarize non-sensitive screen | Allowed only within granted permissions; redact secrets |
| 1 – reversible | open app, navigate, enter draft text | User-configured allowlist; visible execution status |
| 2 – externally visible | send message, publish, submit form, share file | Show exact target/content and obtain explicit confirmation |
| 3 – financial/security | payments, purchases, bank transfer, password/OTP/security settings | No autonomous execution; require direct user control or a separately designed high-assurance flow |
| 4 – prohibited | bypass authentication, exfiltrate secrets, evade access controls, hidden surveillance | Reject |

## Approval content

A confirmation must present the exact action, app, destination/recipient, amount or content, and consequence. “Proceed?” without showing the meaningful details is inadequate.

Approvals are:
- bound to one action and exact arguments;
- short-lived;
- invalidated if arguments or context change;
- persisted with timestamp and decision;
- never inferred from past approvals for materially different operations.

## Prompt injection

Treat app content, webpages, messages, notification text, QR codes and OCR as untrusted data. They cannot change system policy, permissions, tool registry or approval rules.

## Data boundaries

- Do not store passwords, OTPs, auth tokens or payment credentials in model context or ordinary memory.
- Do not upload screenshots/audio unless cloud processing is explicitly enabled and disclosed.
- Keep a local audit trail with configurable retention and deletion.
- Redact sensitive UI fields before telemetry/export.
- Provide a stop control and revoke permission guidance.

## Security testing

Test malicious screen text, deceptive buttons, spoofed recipients, UI drift, stale nodes, repeated actions, cancellation races, model hallucinated tool names, overlong arguments and failed verification.

## Hard prohibitions

No hidden recording, silent message sending, silent payment, auth bypass, privilege escalation, or unrestricted model-generated shell execution.
