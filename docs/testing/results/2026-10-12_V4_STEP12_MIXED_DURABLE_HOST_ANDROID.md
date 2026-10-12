# V4 Step 12 — persistent JPG/MP4/OPUS numbering and local recovery

Date 2026-10-12. **PASS IN OFFLINE HOST + ANDROID 16 EMULATOR SYNTHETIC SCOPE ONLY.** Does NOT establish AIMB-G1 vendor MP4/OPUS fidelity, physical original retention or a final app.

## Final same-source evidence
- Mixed-media durable source: `source/g1-shared-workflow/src/com/parkarsite/g1/workflow/MixedMediaRecoveryStore.java`.
- Host selftest: `source/g1-shared-workflow/selftest/com/parkarsite/g1/workflow/MixedMediaRecoverySelfTest.java`.
- Host Java 17 final test commit `10ea9bcdf71757ca9365313459986af81725d9fd` — [GitHub run 38282545306](https://github.com/kphomedocs-hue/Glasses-/actions/runs/38282545306) **SUCCESS**, raw log `V4-12-MIXED-DURABLE PASS (38 assertions)`; separate hygiene also succeeded for same commit.
- Final Android emulator exact source/workflow commit `9c5100031c0fea010ba1722c94c7461c8ad35c2a` — [GitHub run 38282672292](https://github.com/kphomedocs-hue/Glasses-/actions/runs/38282672292) **SUCCESS**. Final source includes the above legacy migration fixes, plus AndroidTest fixture placement preflight. APKs were temporary **virtual-device-only**, no permissions and no uploaded release.
- Final Android log:
```text
PASS: V4 synthetic fixture implementations are in AndroidTest, not Android main
V4-MIXED-EMU V4_MIXED_JPG_MP4_OPUS_NUMBERED PASS
V4-MIXED-EMU V4_MIXED_REOPEN_NO_DUPLICATES PASS
V4-MIXED-EMU V4_MIXED_MP4_RECOVERED_NO_RETRY PASS
V4-MIXED-EMULATOR PASS: daily mixed JPG MP4 OPUS journal + recovery, synthetic-only, no network
```
- Unchanged same-coordinator Android full-JPG synthetic test and verified-journal process-recovery regressions passed in that same final emulator run; do **not** count the older six storage-only crash barriers as new mixed-media tests.

## Scoped implementation
- One SHA-256 opaque file identity, one per-media `Kind`, a 32 MiB cap and immutable `firstSeenEpochMs` alongside independent `captureEpochMs`.
- Archive day selected using the configured phone-local zone (tested `Asia/Kolkata`), shared sequential ordinal across `.jpg`, `.mp4`, `.opus`, no overwrite and no automatic source retry. Sample verified: `2026-10-12/0001.jpg`, `0002.mp4`, `0003.opus`, followed by `2026-10-13/0001.jpg` after midnight.
- Atomic, fsynced separate `.mixed-journals`, `.mixed-parts`, `.mixed-receipts`; durable `FETCH_MAY_HAVE_STARTED` precedes Source bytes. Uncertain read stays `REMOTE_UNCERTAIN`, VERIFIED bytes may be promoted strictly locally after restart, committed file+receipt checks use size, SHA256 and media shape.
- Android emulator used a fully bitmap-decodable 32x24 synthetic JPG, finite `ftyp`+`mdat` MP4 shape and synthetic Ogg/OpusHead shape. **Neither video nor audio fixture proves playback or glasses-specific content.**
- A real Android instrumentation JVM was forcibly halted with `Runtime.halt(73)` after a VERIFIED synthetic MP4 journal, then fresh instrumentation used **only local data** to rebuild a committed receipt. No network Source was invoked.
- Legacy `RecoveryImportStore` source and frozen release evidence were not modified. Legacy `*.txn` pending JPG ordinal reservations and already present numbered JPGs reserve shared-numbering slots; legacy identity journal or orphan receipt blocks re-import. Malformed/unrecognized legacy journal blocks further migration rather than guessing. The old ledger is never rewritten.

## Red-team issues identified and corrected
1. **Potential collision with old reserved-but-not-yet-final JPG**: initial allocator scanned archive files, but not pending old `.transactions/*.txn` ordinals. Added bounded strict legacy TXN1 path scanner and conservative max slot allocation.
2. **Orphan legacy receipt**: a missing prior transaction cannot imply the media is new; old `.receipts/ID.receipt` now independently blocks re-fetch of the same opaque identity.
3. **Legacy scanner path regex**: corrected over-escaped Java regex before host promotion.
4. **Host fixture wrote literal backslash-t instead of delimiter bytes**: initial enhanced host run [38282455067](https://github.com/kphomedocs-hue/Glasses-/actions/runs/38282455067) FAILED closed with `Unrecognized legacy ledger`. Corrected synthetic test metadata to actual tab-delimited `TXN1` format in `10ea9bcdf71757ca9365313459986af81725d9fd`; final enhanced test 38 assertions PASS.
5. Initial 36-assertion host run `38281952796` and Android `38282170119` predated this migration hardening. The final confirmed results are the **38-assertion host** and **38282672292 Android** runs above; do not substitute earlier test revisions.

## Unresolved production limitations
- V4 new mixed store is still an **offline isolated storage engine**, not connected to an authenticated real BLE/P2P/HTTP media-transfer controller.
- Shape-only MP4 and OPUS fixtures do not prove vendor codec, offsets, validity, duration, playability, capture timestamps or real device metadata; no WAV/MKV or additional formats assumed.
- Existing old-format/unknown schema migration has a conservative stop, not an automatic universal importer.
- Crash semantics are tested on a real Android **instrumentation process halt**, not whole-device power loss; source original still may disappear after real glasses GET+EXIT.
- The android app remains a separate zero-permission simulator, not a signed production Android app.

## Gate
**V4 Step 12 PASS in host+API36 synthetic local-storage scope.**
**V4 Step 13 signed-release isolation NOT COMPLETE.**
**G6A OPEN; G6B BLOCKED.** Do not issue real media GET or build a physical-test APK without separate consent/evidence.
