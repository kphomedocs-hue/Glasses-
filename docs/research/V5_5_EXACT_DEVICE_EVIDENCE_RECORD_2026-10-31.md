# V5.5 — AIMB-G1 exact-device evidence record

**Date:** 2026-10-31  
**Scope:** existing repository and public-source review only. No device access, new BLE reads or commands, media GET, APK installation, firmware update, or external contact.

## Identity and version ledger

| Field | Value | Evidence grade and source | Limit |
|---|---|---|---|
| Hardware revision | `AM01SPG1_V1.4` | **Physically observed** on 2026-09-19 from standard BLE Device Information Service, hardware revision characteristic `0x2A27`, on the uniquely bonded AIMB-G1-family device. `docs/testing/results/2026-09-19_G1_PHYSICAL_DISCOVERY_PASS.md`; corroborated by `docs/protocol/AIMB_G1_PROTOCOL.md`. | Proven for that read/session; no later hardware revision read is recorded. |
| Glasses firmware revision | **Unknown** | The same discovery attempted standard firmware revision characteristic `0x2A26`, but Android returned GATT status 133. | Error 133 is not a version and says nothing about the installed firmware contents. |
| Wi-Fi firmware revision | **Unknown** | No verified current-unit value found in the repository's physical reports. | Values from other HeyCyan models cannot be assigned to this unit. |
| Device serial | **Unknown / intentionally not read** | The serial characteristic was enumerated but deliberately not read in G1. | Do not seek or publish a serial merely to fill the ledger. |
| Cyan app package available for static research | `com.aitowe.aitoglasses`, version `1.0.2.18_20260811`, versionCode 85 | **Original owner-supplied APK verified** by V5.1 SHA-256 and six DEX matches. | This proves the research APK version, not that this version was installed on the phone in every physical session or the glasses firmware version. |
| K diagnostic during source-loss observation | Promoted v0.6.7, build commit `cb90f25b0bea9cfe308a1d79d9f4235c7f2b21fe` | **Exact physical report/build attribution:** `releases/v0.6.7/builds/run-36898159938-attempt-1/reports/2026-10-01_G6A_V0_6_7_POST_GET_REMOTE_CONSUMPTION_PHYSICAL_PROOF.md`. | Report header printed stale `0.6.6`; this is K diagnostic version, not Cyan or glasses firmware. |

## Artifact inventory and public search

The repository contains no matching glasses firmware image or source, no `.swu`, `.bin`, `.img`, `.fw`, `.hex`, `.dfu` or OTA artifact found by filename scan. The original Cyan APK is a **phone app**, not the glasses firmware. Its DEX trace cannot establish the glasses-side HTTP GET or EXIT state transitions.

Exact public searches on 2026-10-31 for `AM01SPG1_V1.4` with firmware/AIMB-G1 and for `AM01SPG1` with OTA/`.swu` returned no indexed matching source. This is a search limit, not proof that an image or specification does not exist.

## Correction to earlier record

`docs/research/V5_3_VENDOR_SUPPORT_INQUIRY_READY_2026-10-31.md` said that no verified hardware revision existed in the repository. That statement was wrong: G1 physically read `AM01SPG1_V1.4`. The firmware revision and serial remain unknown. The V5.3 email was not sent; the user later explicitly declined contacting support.

## Decision and next research boundary

The known hardware revision narrows future document searches but does not identify the firmware that handled the v0.6.7 transfer. Without matching glasses-side firmware/source or authoritative version-scoped documentation, GET-versus-EXIT causality and an original-preserving method remain unresolved. Do not substitute firmware from a related model or the Cyan phone APK. **C3 NO-GO; G6A OPEN; G6B BLOCKED.**

Next safe step: inspect any already-held device packaging, screenshots or exports for a firmware version only if supplied or previously retained; search public exact-hardware artifacts. If no matching source emerges, stop before claiming a preservation mechanism. No physical test is authorized by this record.
