# G6A remaining hardware blocker — source-preservation evidence request
2026-10-12. **Evidence questions only; no contact/message was sent. No device operation was authorized.**

## Exact known physical evidence
- AIMB-G1 family target, P2P catalog `/files/media.config`, a successful single photo HTTP GET, then EXIT/reconnect.
- No-GET capture/control (v0.6.5): new JPG remained in the remote catalog after reconnect.
- Single successful GET+EXIT (v0.6.7): the exact imported JPG identity disappeared from catalog and image inventory, while all baseline images remained.
- The causal instant (GET, acknowledgement, EXIT or device firmware state) is not established.
- Existing Cyan APK analysis identifies ordinary HTTP file download plus exit; reviewed methods **do not show a verified 'preserve original' switch**.
- A publicly available AIMB-G1 manual (https://manuals.plus/m/62705ea125d314ab056115558075f5740abde4741bae199d83e660b6f163b93f) says the user presses Import to save photos to the phone; **it does not say media is retained on glasses**. The official Cyan app listing likewise does not certify retention.

## Exact questions requiring PRIMARY-SOURCE evidence
1. For AIMB-G1 (not merely other A03/AIMB-G2 models), does CyanGlasses photo/audio/video **Import** intentionally move/dequeue or delete the original from glasses storage, or only hide its catalog entry?
2. Is removal committed when the HTTP media GET finishes, on an acknowledgement, a BLE/P2P EXIT command, or at another point? Provide a protocol specification or manufacturer trace, not a guess.
3. Does supported firmware expose a **COPY WITHOUT CONSUME** operation for original full-resolution JPG, MP4 and OPUS? Specify exact supported firmware/hardware versions, API semantics and required permission/authentication. Do not suggest an undocumented speculative verb/URL.
4. Can the same original be enumerated again after a successful manufacturer-supported copy-only transfer, and is it still accessible from CyanGlasses? What constitutes proof?
5. Are media retrieval modes `configFileType=1` P2P, `configFileType=2` AP/JSON and other firmware variants semantically different regarding original retention? Only source-backed answers should be considered.
6. What authenticated, stable identity should the phone use to correlate a BLE-bonded AIMB-G1 to the actual selected Wi-Fi Direct member, bound Android network and peer IPv4? Is a BLE/name-derived Wi-Fi advertisement guaranteed?
7. Is there a firmware release or official SDK that exposes a preserving media API? If so, provide signed documentation, firmware build, API/SDK version and changelog.
8. Are there any manufacturer-approved non-destructive, no-media-GET observation steps to resolve which discovered Wi-Fi Direct peer is the exact pair of glasses?

## Acceptance criteria before resuming physical GET
- Source-backed, model/firmware-scoped guarantee that existing originals remain accessible after a **fully completed** transfer and shutdown/next reboot.
- Independent test plan using a disposable photograph, with distinct before/after identities and catalog+inventory checks, strictly no bulk import.
- A separate owner's explicit authorization for any physical observer build or test, with bounded operations and cleanup.
- Every path preserving historical release evidence and avoiding raw SSIDs/MACs/client media in public logs.
- Until then: **NO REAL MEDIA GET, NO FINAL AUTOMATIC SYNC APK**. A local receipt only proves phone-side persistence, not retention of original on glasses.

The research was performed without attempting any additional device operation or sending an external inquiry.
**G6A OPEN; G6B BLOCKED.**
