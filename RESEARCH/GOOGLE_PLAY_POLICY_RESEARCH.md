# Google Play policy research: AccessibilityService and automation

**Last reviewed:** 2026-11-02  
**Source:** https://support.google.com/googleplay/android-developer/answer/10964491?hl=en

## Verified policy finding

Google Play limits use of AccessibilityService and the `isAccessibilityTool` declaration. Only services genuinely designed to help people with disabilities access their device or overcome disability-related challenges may declare themselves accessibility tools.

For automation apps, Google Play says actions performed on a user's behalf must be for a narrow and clearly understood purpose. It specifically prohibits use of AccessibilityService that enables an app to autonomously initiate, plan, and execute actions or decisions.

This is a critical product constraint. Do not build a general-purpose agent whose central Play-distributed execution model depends on autonomous AccessibilityService orchestration and assume a disclosure dialog will make it acceptable.

## Product implications

Maintain separate release profiles:
1. **Personal / sideloaded profile:** may evaluate capabilities available on the device through standard Android APIs with explicit user configuration and control. It must still respect Android permissions, security boundaries, privacy and applicable law.
2. **Google Play profile:** must use only functionality compatible with Play policy. Potential candidates include user-initiated narrow automation, explicit intents, app-specific APIs, integrations with permissioned services, and guided confirmation flows. These options are not automatically approved; review each use case and policy.

Do not fake disability use cases or set `isAccessibilityTool=true` for discoverability or policy circumvention.

## Required product decisions

- Is the first release a personal sideloaded tool or a Play-distributed application?
- Will broad UI automation remain a personal-only feature?
- What tasks can be exposed in a Play build without violating the narrow-purpose policy?
- How will the app explain data accessed through accessibility, screenshots, notifications, microphone and connected services?
- How will user consent, revoke controls, logs, and emergency stop be implemented?

## Review checklist

Before every Play submission, re-read the official policy and check:
- AccessibilityService use case and scope;
- `isAccessibilityTool` declaration and eligibility;
- prominent disclosure and consent;
- permission declaration and review forms;
- app listing and in-app behavior consistency;
- data safety and privacy policy declarations.

## Important limitation

This is engineering research, not legal advice or a Play approval guarantee. Policy enforcement and interpretation can change. Obtain a policy review for the actual app behavior before distributing.
