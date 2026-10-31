# V5.4 — Public protocol implementation audit

**Date:** 2026-10-31  
**Scope:** public-source research only. No support contact, device access, media GET, BLE command, APK installation, or physical test.

## Finding

Community implementations for HeyCyan compatible devices describe a transfer architecture similar to the one physically observed on this AIMB-G1. They are secondary evidence for protocol design, not independent physical validation of this unit or its firmware:

1. BLE enters transfer mode and supplies or enables discovery of the glasses network address.
2. Wi-Fi Direct carries HTTP traffic to the glasses.
3. The phone reads `/files/media.config`.
4. The phone downloads media from `/files/<filename>`.
5. The phone stores the result locally and exits transfer mode.

These projects describe implementation choices such as network binding, peer selection, timeout handling and local gallery storage. The reviewed public material does **not** document a read-only GET, a keep-original flag, an alternate preserving endpoint, or a supported way to prove that a catalog-hidden item remains in storage.

## Evidence reviewed

| Source | Public claim | Preservation relevance |
|---|---|---|
| FerSaiyan `Alternative-HeyCyan-App-and-SDK` Android transfer notes | Confirms BLE + Wi-Fi Direct HTTP, `/files/media.config`, `/files/<filename>`, BLE IP notification and network binding. | Confirms the same architecture; gives no source-retention guarantee. |
| Same project release notes v1.0.2/v1.0.3 | Describes downloading JPG/MP4/OPUS and saving them into the phone gallery. | Describes local persistence only; says nothing about originals on glasses. |
| Same project release notes v2.1.0/v2.1.1 | Describes stricter vendor-like sync, timeout detection and retry improvements. | Reliability improvements do not establish non-consumptive firmware semantics. Retry remains unsafe for this project while remote state is uncertain. |
| Public HeyCyan SDK repositories | Expose media-count and transfer-related APIs and the same `/files/media.config` pattern. | API availability does not establish copy versus move/hide behavior. |
| Community issue and discussion reports | Describe transfer mode disabling capture during sync and varied device/firmware behavior. | Supports treating model/firmware as material; not authoritative preservation evidence. |

Sources:

- https://github.com/FerSaiyan/Alternative-HeyCyan-App-and-SDK/blob/main/android/AGENTS.md
- https://github.com/FerSaiyan/Alternative-HeyCyan-App-and-SDK/releases
- https://github.com/FerSaiyan/Alternative-HeyCyan-App-and-SDK
- https://github.com/ebowwa/HeyCyanSmartGlassesSDK

## Red-team conclusions

- A second implementation of the same endpoint sequence does not make the sequence safe for original preservation.
- Better network binding or peer selection addresses transport reliability, not the glasses-side file-state transition.
- A local gallery copy proves only that phone bytes were stored; it does not prove source bytes remain available.
- Community retry logic cannot be adopted while a first request may have consumed, hidden or staged the source.
- Community reports cover multiple hardware families and firmware versions; they cannot be silently mapped to this AIMB-G1 unit.
- Public SDK names such as `getDeviceMedia` or media-count APIs do not establish source retention.

## V5.4 decision

No preserving transfer path was found in the reviewed public sources. These sources do not strengthen the physical causality evidence or establish AIMB-G1 compatibility; they supply candidate implementation details for later review. The preservation decision is unchanged:

**C3 NO-GO; G6A OPEN; G6B BLOCKED.** Keep real media GET disabled. No physical validation or experimental APK is authorized by this evidence.

The next safe work remains offline: maintain the evidence ledger, review exact firmware/version evidence if it becomes available through the user, and keep any future validation separately scoped to disposable media with an explicit preservation acceptance rule.
