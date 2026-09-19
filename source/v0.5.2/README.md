# K G1 Disposable Photo Probe v0.5.2 — G5.2

Purpose: retry the bounded one-disposable-JPG gate with the two exact Cyan readiness behaviors missing from v0.5.0.

Physical v0.5.0 stopped safely:
- Phase A catalog: 3 entries / 67 bytes;
- one disposable JPG captured after transfer exit;
- Phase B catalog: unchanged 3 entries / 67 bytes;
- new-entry delta: 0;
- media-file GETs: 0.

Evidence:
- `docs/testing/results/2026-09-19_G5_V0_5_0_SAFE_NO_DELTA.md`
- `docs/research/CYAN_G5_REFRESH_PARITY_2026-09-19.md`

## Exact Cyan parity added

### 1. Media inventory query before transfer

In each phase, immediately after notification subscription, the app sends exactly one already-proven:

```text
0x41 / 02 04
```

The response is parsed using the exact G3 offsets:
- image count: payload bytes 2..3 LE16;
- video count: payload bytes 4..5 LE16;
- recording count: payload bytes 6..7 LE16;
- configFileType: payload byte 8;
- onlySupportApImport: payload byte 9.

The probe refuses to continue unless:
- configFileType == 1;
- onlySupportApImport == false.

Phase A counts are retained for comparison. Phase B reports count deltas, but catalog set difference remains the only media-file selection mechanism.

### 2. Exact 1000 ms catalog readiness delay

After:
- exact P2P group formation,
- phone group-owner confirmation,
- passive `0x73 / 0x08` glasses IP,

the app waits exactly:

```text
1000 ms
```

before the one catalog GET.

This mirrors Cyan:
`PictureFragment.downloadMediaConfig() -> ktxRunOnUiDelay(..., 0x03e8, ...)`.

## Two-phase flow

Phase A:
1. one `02 04` media-count query;
2. one P2P enter;
3. exact peer association;
4. passive glasses IP;
5. exact 1000 ms delay;
6. one GET `/files/media.config`;
7. baseline held in memory only;
8. one transfer exit.

User then captures exactly one disposable JPG.

Phase B:
1. one `02 04` media-count query;
2. one P2P enter;
3. exact peer association;
4. passive glasses IP;
5. exact 1000 ms delay;
6. one GET `/files/media.config`;
7. require complete baseline still present;
8. require exactly one new strict-safe relative `.jpg`;
9. exactly one media GET;
10. stream to app-private cache under 32 MiB;
11. verify HTTP 200, Content-Length when present, JPEG SOI/EOI;
12. one transfer exit and stop.

## Hard limits on a successful two-phase run

- media-count queries: 2 maximum;
- P2P enter writes: 2 maximum;
- transfer-exit writes: 2 maximum;
- catalog GETs: 2 maximum;
- media-file GETs: 1 maximum;
- total HTTP GETs: 3 maximum;
- redirects: disabled;
- retry/resume/Range: absent;
- AP fallback: absent;
- `02 03`: absent.

## Privacy and safety

Never logged/persisted:
- transfer credential values;
- Bluetooth/peer addresses;
- raw catalog;
- catalog line values;
- remote filename/path/URL;
- media hash/fingerprint;
- local cache path.

No glasses file delete/mutation is implemented.

Package ID is intentionally distinct from v0.5.0 to avoid Android debug-signing update conflicts:
`com.parkarsite.g1singlephotoprobe52`.


## Pre-physical recheck hardening

A second static/runtime-order review before physical use found two non-protocol defects in v0.5.1:

1. After a safe Phase B stop, the failure-cleanup branch enabled the Run button without resetting the internal Stage from DOWNLOAD to BASELINE. The completed report was correct and no extra network action occurred, but a subsequent tap could do nothing.
2. The media-count response handler could issue P2P enter immediately on the response notification without explicitly waiting for Android's characteristic-write callback. Physical G3 ordering had been successful, but G5.2 removes the callback-order assumption.

v0.5.2 fixes both:
- any completed failure resets Stage to BASELINE and restores the Start button;
- media-count → P2P enter occurs only after BOTH the successful BLE write callback and a valid `02 04` response have been observed.

All G5.1 network/protocol limits are unchanged.
