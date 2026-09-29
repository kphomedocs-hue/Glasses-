# G6A v0.6.3 physical Part 1 — AMBIGUOUS +2 VISIBILITY ABORT

Date: 2026-09-29 (+05:30)

## Result

**Controlled abort before P2P/media transfer.**

v0.6.3 established a valid baseline, armed the BLE-only visibility watch, and then rejected the first post-capture inventory event because image count increased by two rather than exactly one.

## Baseline

- app version: 0.6.3
- images=8
- videos=0
- recordings=1
- configFileType=1
- catalog entries=9
- safe entries=9
- extensions: .jpg=8, .opus=1
- media-file GETs=0
- transfer mode exited before capture: YES

## Visibility watch

After ARMED and the user's intended single capture:
- passive `0x73/0x01` reported images=10;
- videos=0;
- recordings=1;
- image delta from baseline=+2.

The exact +1 visibility condition was therefore not met.

The app immediately stopped with:
`G6A RESULT: FAILED — Ambiguous inventory change during visibility watch; P2P remains blocked.`

## Safety consequence

Because the ambiguity occurred before the Phase-B confirmation/P2P gate:
- no Phase-B P2P entry occurred;
- no Phase-B catalog GET occurred;
- no media-file GET occurred;
- no persistent import occurred;
- no retention capsule was committed;
- no glasses mutation/deletion command exists.

This is the intended fail-closed behavior.

## Interpretation

The +2 event must not be treated as proof that two shutter captures occurred. The current evidence only proves that the glasses' reported image inventory changed from 8 to 10 by the time of the first usable post-arm inventory event.

Given the immediately preceding v0.6.2 cross-session finding (9 JPG after capture/import, then 8 JPG after restart), the remote inventory/catalog may be changing independently of the current capture timing. A fresh identical v0.6.3 import attempt would not isolate that behavior.

## Gate decision

- v0.6.3 Part 1: NOT PASSED.
- retention capsule: NOT CREATED.
- G6A remains open.
- G6B remains blocked.

## Recommended next diagnostic

Before another capture/import attempt, run a no-capture remote catalog stability diagnostic:
1. read inventory + catalog snapshot A;
2. exit transfer mode;
3. keep glasses untouched and take no photo;
4. after a bounded quiet interval/reconnection, read inventory + catalog snapshot B;
5. compare full privacy-safe JPG hash sets;
6. zero media-file GETs throughout.

This isolates spontaneous inventory/catalog churn from capture-related behavior.
