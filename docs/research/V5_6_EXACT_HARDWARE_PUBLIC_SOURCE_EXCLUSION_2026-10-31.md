# V5.6 — exact-hardware public-source exclusion ledger

**Date:** 2026-10-31  
**Scope:** offline public-document and existing-repository review. No device interaction, media GET, BLE command, APK/firmware installation, or external contact.

## Question and decision

Can a public AIMB-G1 firmware/OTA or transfer instruction be assigned to the physically observed hardware revision `AM01SPG1_V1.4`? **No, on the sources reviewed.** A model-name match alone is insufficient. No public source reviewed identifies that hardware revision together with the installed firmware or specifies original retention following a media transfer.

| Candidate | Provenance and content | Match to this unit | Disposition |
|---|---|---|---|
| [AIMB-G1 CyanGlasses manual](https://manuals.plus/m/62705ea125d314ab056115558075f5740abde4741bae199d83e660b6f163b93f) | Third-party converted copy of a one-page PDF. Describes magnetic charging and CyanGlasses **Import** to save photos/audio to the phone. Previously reviewed in G6A and V4. | AIMB-G1 name and Cyan app are relevant, but no `AM01SPG1_V1.4` or firmware revision; no post-Import source-retention statement. | Context for a marketed app flow only; cannot establish preservation or GET/EXIT mechanism. |
| [Other AIMB-G1 manual](https://fccid.io/2BN53-AIMB-G1/User-Manual/User-manual-8453862), [FCC attachment 8453862](https://apps.fcc.gov/eas/GetApplicationAttachment.html?id=8453862) | FCC filing for Shenzhen Huaxinchen Technology Co., Ltd, ID `2BN53-AIMB-G1`, submitted 2025-07-08. The [converted copy](https://manuals.plus/m/7cd49c013077d52bbaf8fd2a829a218aad39fc5e6022c14b92e63f30e8066496) describes USB-C charging, motion/step tracking and a generic app firmware-update instruction. | Its product description differs materially from the CyanGlasses manual. No observed FCC ID or `AM01SPG1_V1.4` match ties this filing to the user's unit. | Exclude the firmware-update instruction as evidence for this unit. FCC provenance proves an applicant's exhibit for the named filing, not equivalence to the observed hardware revision. |
| Related HeyCyan firmware names such as `WIFIAM01G1_V9.2` | Community material previously reviewed in V5.4 concerns other hardware/version strings. | `AM01G1_V9.2` is not `AM01SPG1_V1.4`; installed main and Wi-Fi firmware on this unit remain unknown. | Do not download, flash, or infer compatibility. |

Exact-string public searches for `AM01SPG1`, `AM01SPG1_V1.4`, and `WIFIAM01SPG1` with firmware/OTA terms returned no indexed exact match on this date. This bounds the search; it does not prove no firmware or private documentation exists.

## Red-team check

- The FCC ID and the hardware revision are different identifiers. Without a label or authoritative mapping for the user's unit, the FCC exhibit cannot close that gap.
- An app's **Import** wording and a generic **update firmware** instruction do not state whether the glasses retain originals.
- A phone APK or another model's OTA image cannot reveal the exact glasses-side GET, EXIT, teardown, or deferred state transition.
- A remote catalog identity disappearing after the complete v0.6.7 GET→EXIT lifecycle remains an observation, not proof of flash erasure or a particular commit event.

## Stop condition

The next useful evidence is an already-held, verifiable firmware/version record or authoritative document explicitly mapping `AM01SPG1_V1.4` to firmware and defining media retention. No source reviewed meets it. Do not probe the glasses to manufacture one. **C3 NO-GO; G6A OPEN; G6B BLOCKED.** Continue preserving originals and treat media reads as potentially consumptive.
