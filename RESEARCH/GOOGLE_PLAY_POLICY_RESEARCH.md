# Google Play policy reference (not the product target)

**Last reviewed:** 2026-11-02  
**Official source:** https://support.google.com/googleplay/android-developer/answer/10964491?hl=en

## Why this document exists

The project owner has explicitly confirmed that this app is for personal use, primarily installed directly on their own Android devices. It is **not intended for Google Play publication**. Therefore, Play policy is not the primary product design constraint.

This reference is retained in case the distribution goal changes later. If Play distribution is ever considered, re-audit the current policy and actual app behavior before building or submitting a Play release.

## Policy finding to remember

Google Play limits AccessibilityService use and the `isAccessibilityTool` declaration. The policy specifically restricts general automation apps from using AccessibilityService to autonomously initiate, plan and execute actions or decisions. Only qualifying accessibility tools whose primary purpose is assisting people with disabilities may make the corresponding accessibility-tool declaration.

Do not misrepresent the app as a disability accessibility tool. Personal use does not require us to design for Play review, but it also does not justify deceptive declarations or bypassing Android security controls.

## Consequence for this project

- Do not artificially narrow the personal-use architecture solely to satisfy Play Store distribution requirements.
- Investigate AccessibilityService as a core UI observation/action mechanism, with explicit user enablement and visible controls.
- Keep permissions, consent, safety confirmations and sensitive-action protections.
- If publishing becomes a future requirement, create a separate policy-compliant product profile and reassess the feature set.

## Official source

https://support.google.com/googleplay/android-developer/answer/10964491?hl=en

This is engineering research, not legal advice. Policy may change; re-check if the project ever targets Google Play.