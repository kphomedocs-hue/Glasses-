# V5.7 — v0.6.7 media-close / EXIT ordering audit

**Date:** 2026-10-31  
**Scope:** offline review of frozen v0.6.7 source and physical report. No device access, BLE write, media GET, app build/install, or external contact.

## Source-level sequence

Exact source: [`source/v0.6.7/app/src/main/java/com/parkarsite/g6aobserver67/MainActivity.java`](../../source/v0.6.7/app/src/main/java/com/parkarsite/g6aobserver67/MainActivity.java), notably `downloadMediaOnce()` around lines 852–901, `streamMediaBounded()` around lines 903–918, and `sendExit()` around lines 1021–1042. Physical outcome: [promoted v0.6.7 report](../../releases/v0.6.7/builds/run-36898159938-attempt-1/reports/2026-10-01_G6A_V0_6_7_POST_GET_REMOTE_CONSUMPTION_PHYSICAL_PROOF.md). The report's `0.6.6` header is a documented hard-coded defect; build commit/run identify v0.6.7.

| Order supported by client code | Event | What is and is not known |
|---|---|---|
| 1 | One HTTP GET obtains status 200; `streamMediaBounded()` reads until EOF, closes the input stream and local output stream via try-with-resources, flushes and syncs the local temporary file. | Client-side full-body completion is established. The precise glasses-side moment of state commit is unknown. |
| 2 | Client checks byte count and JPEG markers, deletes the temporary phone cache file, and posts a runnable to the Android UI handler. | Local cache deletion is not a glasses deletion command. |
| 3a | The UI runnable records success and calls `sendExit()`, starting one BLE `02 01 09` write. | The runnable is asynchronous relative to the GET worker. |
| 3b | The GET worker's `finally` calls `conn.disconnect()`. | Source order places the post before `finally`, but **does not order the actual BLE EXIT write against `disconnect()`**. Either may run first. Stream closure in step 1 does precede both. |
| 4 | After successful EXIT write callback, the app accepts a valid `0x73/0x01` inventory response. It saw 11 images versus 12 before GET; a later fresh catalog lacked the selected JPG while baseline JPG identities remained. | This is the first recorded post-GET inventory observation. There is no intervening pre-EXIT inventory measurement, server trace, or timestamped firmware commit event. |

The source confirms a successful EOF/full-body client read **before the EXIT request is posted**. It does not prove that the glasses changed catalog state at EOF, stream close, connection disconnect, BLE EXIT, Wi-Fi teardown, or a deferred task. In particular, the report label “immediate post-GET exit inventory” describes *when the app observed the count*, not when the firmware changed it.

## Impact on the V5.2 hypothesis matrix

| Mechanism | New constraint | Ranking change |
|---|---|---|
| Full-body/EOF or stream-close commit | Full-body read and client input-stream close happened before `sendExit()` was invoked. | Still possible; no direct pre-EXIT state observation. |
| HTTP connection-disconnect commit | Explicit `disconnect()` is in the worker `finally`; UI-thread EXIT can race it. Stream closure may also affect the connection independently. | Still possible; no observed socket/server close time. |
| GET-conditioned BLE EXIT commit | EXIT follows validation in the UI runnable; no-GET v0.6.5 EXIT retained the new JPG. | Still possible; no isolation from prior full GET/close. |
| Teardown or deferred commit | Fresh reconnect catalog was already missing the identity. | Still possible; no firmware-side transition trace. |

These mechanisms remain in the same unresolved tier. This audit adds execution ordering, **not** new device evidence or a preservation method.

## Red-team and decision

Do not infer a safe “copy then skip EXIT” workflow from this ordering: it is untested, changes the supported lifecycle, and could strand transfer state. Do not infer that a temporary phone-file deletion caused the remote disappearance. A post-EXIT inventory of 11 cannot be backdated to pre-EXIT. No new command or test is proposed.

The only defensible physical conclusion remains that the one validated JPG GET plus normal EXIT/teardown lifecycle was associated with disappearance of that exact identity from the remote inventory/catalog. Storage-byte erasure is unproven. **C3 NO-GO; G6A OPEN; G6B BLOCKED.**
