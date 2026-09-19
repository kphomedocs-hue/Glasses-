# Physical AIMB-G1 Test Gate

This document defines what may be done to the physical glasses at each current gate.

## G1 — physical GATT confirmation

Status: **PASS — 2026-09-19**.

Evidence:
`docs/testing/results/2026-09-19_G1_PHYSICAL_DISCOVERY_PASS.md`

Physically confirmed:
- service `de5bf728-d711-4e47-af26-65e3012a5dc7`,
- notify characteristic `de5bf729-d711-4e47-af26-65e3012a5dc7`,
- write characteristic `de5bf72a-d711-4e47-af26-65e3012a5dc7`,
- hardware revision `AM01SPG1_V1.4`.

## G2 — notification response-channel confirmation

Status: **PASS — 2026-09-19**.

Evidence:
`docs/testing/results/2026-09-19_G2_NOTIFICATION_ONLY_PASS.md`

The probe:
- connected to exactly one bonded AIMB-G1-family device,
- enabled notifications on the confirmed notify characteristic,
- wrote only the standard CCCD enable-notification value,
- sent no proprietary characteristic command,
- observed three valid spontaneous `0x73` frames.

## G2B — semantic trace

Status: **PASS by exact Cyan static analysis**.

The passive event-correlator design below is retained as a fallback for future unknown event types, but is not required before the next gate.

A future G2B Event Correlator may:
- connect to exactly one bonded AIMB-G1-family device,
- subscribe only to `de5bf729-d711-4e47-af26-65e3012a5dc7`,
- perform the standard CCCD enable-notification write,
- timestamp every valid notification,
- validate envelope length and CRC,
- allow the user to insert timestamped event markers,
- group repeated frames/payloads,
- export a sanitized report.

It must not:
- call a BLE characteristic-write API,
- include or use `de5bf72a-d711-4e47-af26-65e3012a5dc7`,
- send a Cyan control frame,
- send initialization/time data,
- enter media mode,
- activate Wi-Fi/P2P/AP,
- use HTTP/media transfer,
- pair/unpair/reset the device,
- perform OTA/firmware operations,
- provide a generic raw BLE console.

### Event-correlation discipline

Use safe, observable physical actions only. Mark the event in the app at the time it happens and repeat the same action several times.

Do not assign semantic meaning from one coincidental packet. A proposed mapping must show repeatability and should be checked against Cyan static evidence.

See:
`docs/testing/G2B_PASSIVE_CORRELATION_PLAN.md`

## G2C — completed physical diagnostic

Verified candidate: **K G1 Init Probe v0.2.1**.

- APK SHA-256: `7c0ce5d859e79c6ee2ed05316f5f5db736919912802fbbab040ce612ea368e5c`
- build/verification run: `35416416013` — PASS

One and only one proprietary command may be tested:

- command: `0x40` Cyan-equivalent time synchronization,
- payload: dynamically generated using Cyan's 9-byte time/language/timezone schema,
- frame: standard validated `BC | 40 | len | CRC | payload`,
- destination: confirmed Cyan write characteristic,
- response observation: confirmed notify characteristic,
- stop after this one command.

Not allowed:
- any `0x41` glasses-control command,
- media-mode P2P/AP payload,
- Wi-Fi/P2P/AP activation,
- HTTP/media transfer,
- reset/restart/OTA/firmware operation,
- arbitrary user-entered payloads.

## G2C result

**PASS — 2026-09-19.**

Evidence:
`docs/testing/results/2026-09-19_G2C_TIME_SYNC_PASS.md`

The single `0x40` write completed successfully and a valid `0x40` response frame was received.

## G3 — completed diagnostic

Verified candidate: **K G1 Media Count Probe v0.3**.

- APK SHA-256: `31191dc90dac56e9aced7db5a6f6b4c65b969f10ec6d673199ee94c6a2f204e9`
- build/verification run: `35420261264` — PASS

The next proprietary write is the narrower exact-Cyan media inventory/count query:

```text
command 0x41
payload 02 04
```

It may be sent exactly once after notification subscription.

This gate must not activate P2P/AP or perform media transfer.

## G3B — first media-mode control write

Status: **BLOCKED pending G3**.

Do not send `02 01 04 01` or `02 01 04 02` until G3 media-count query behavior is reviewed.

## Later gates

- G4: local network + read-only media listing.
- G5: one disposable media download.
- G6: automatic media sync.
- G7: hardening.

No destructive maintenance command is part of the roadmap.


## G3 result

**PASS — 2026-09-19.**

Evidence:
`docs/testing/results/2026-09-19_G3_MEDIA_COUNT_PASS.md`

The media-count response identifies:
- 1 image,
- 0 videos,
- 1 recording,
- configFileType 1,
- onlySupportApImport false.

Exact Cyan logic selects P2P.

## G3B — current approved next diagnostic

A verified G3B build may perform exactly two proprietary writes:

1. enter transfer mode:
   `0x41 / 02 01 04 01`
2. exit transfer mode:
   `0x41 / 02 01 09`

The exit payload is directly recovered from the exact Cyan `fileDownloadComplete()` path.

G3B must not:
- invoke Android Wi-Fi/P2P/AP APIs,
- open sockets or HTTP,
- list/download/delete/modify media,
- send AP-mode payload `02 01 04 02`,
- use reset/restart/OTA,
- expose arbitrary command input.

If the enter command fails, the diagnostic must not attempt any unrelated fallback command.


## G3B verified candidate

K G1 P2P Lifecycle Probe v0.3.1 is verified for physical testing.

- APK SHA-256: `1578b6540596625825c16a95da64f72f46ee29aaad47c866bb9025a3389dce7c`
- build/verification run: `35421429624` — PASS

Only two proprietary writes are implemented:
1. `0x41 / 02 01 04 01` — enter P2P transfer mode
2. `0x41 / 02 01 09` — exit transfer mode

No Android Wi-Fi/P2P/network APIs are present. No HTTP or media transfer occurs.


## G3B physical result

**PASS — 2026-09-19.**

Evidence:
`docs/testing/results/2026-09-19_G3B_P2P_LIFECYCLE_PASS.md`

The enter response carried a 20-byte SSID and a 9-byte transfer password. Their actual values are not stored in this public repository.

## G4A — next approved physical diagnostic

Phone-side Wi-Fi Direct association only.

Allowed:
- one confirmed P2P enter command,
- in-memory credential parsing with no logging/persistence,
- Android Wi-Fi Direct discovery/association,
- connection/group metadata,
- one confirmed transfer-exit command,
- cleanup/disconnect.

Not allowed:
- HTTP or sockets,
- media.config,
- file listing/download,
- file deletion/modification,
- AP mode,
- reset/restart/OTA,
- arbitrary command entry.


## G4A physical result

**PASS — 2026-09-19.**

Evidence:
`docs/testing/results/2026-09-19_G4A_P2P_ASSOCIATION_PASS.md`

Confirmed:
- exact BLE-reported P2P peer discovered,
- association request succeeded,
- P2P group formed,
- phone is group owner at `192.168.49.1`,
- no HTTP/socket/media request occurred,
- transfer exit succeeded.

The glasses-side client IP remains unresolved.

## G4A2 — next approved physical diagnostic

**PASS — 2026-09-19.**

App: K G1 P2P IP Notify Probe v0.4.1  
APK: `releases/v0.4.1/K_G1_P2P_IP_Notify_Probe_v0_4_1.apk`  
APK SHA-256: `3c9104c34fbf06fb06631a09c67cac9eab3e2a626078abcc14401cf09a2cddf3`  
Build run: `35429585616` — PASS

Passive P2P-IP notification capture passed using the same bounded G4A lifecycle.

Observed:
- event IDs `0x0B`, `0x08`, `0x01`,
- `0x73 / 0x08` resolved the glasses client IP as `192.168.49.176`,
- phone group owner remained `192.168.49.1`,
- `0x41 / 02 03` was not used,
- HTTP/socket/media operations remained 0,
- transfer exit succeeded,
- `removeGroup` reason 2 remained a non-blocking cleanup anomaly.

Evidence:
`docs/testing/results/2026-09-19_G4A2_P2P_IP_NOTIFY_PASS.md`

## G4B — physical result

**READ PATH CONFIRMED; initial parser assumption corrected.**

v0.4.2 physically reached `/files/media.config` with one GET and read the bounded response. Its `JSONException` came from our diagnostic applying the wrong config-type parser.

Evidence:
`docs/testing/results/2026-09-19_G4B_CATALOG_PARSE_FAIL.md`

## G4B2 — physical result

**PASS — 2026-09-19.**

App: K G1 Catalog Shape Probe v0.4.4

Physical result:
- exact P2P association repeated successfully,
- passive glasses IP resolved,
- exactly one GET to `/files/media.config`,
- HTTP 200,
- `Content-Type: text/plain`,
- 67 bytes,
- valid UTF-8,
- no BOM,
- diagnostic line count 4,
- zero media-file GETs,
- transfer exit succeeded,
- no raw catalog body, filename/path values or response fingerprints were logged.

Corrected exact Cyan bytecode proves the physical `configFileType=1` branch uses Kotlin `readLines()` and treats each line as a catalog entry. JSON/Moshi applies only to `configFileType==2`.

Evidence:
- `docs/testing/results/2026-09-19_G4B2_RESPONSE_SHAPE_PASS.md`
- `docs/research/CYAN_EXACT_G4B_TRACE_2026-09-19.md`

## G4B3 — next approved physical diagnostic

**Verified build ready; physical result pending.**

App: K G1 Catalog Line Probe v0.4.5  
APK: `releases/v0.4.5/K_G1_Catalog_Line_Probe_v0_4_5.apk`  
APK SHA-256: `2f62c8798ac42ff0d619962490b9066b5cae2a1b2eb7498bf96e053ddc03ca72`  
Build run: `35433799612` — PASS

Allowed:
- same P2P enter once and transfer exit once;
- exact BLE-reported peer only;
- passive `0x73 / 0x08` glasses IP only;
- exactly one GET to `/files/media.config`;
- parse in memory with line semantics equivalent to Cyan/Kotlin `readLines()`;
- report only total/non-empty/blank counts, extension counts and path-safety counts.

Still prohibited:
- actual catalog lines, filenames or paths;
- raw response logging or response fingerprints;
- media-file GET/download;
- `vf_list.txt` or JSON fallback;
- redirects or retry;
- file writes/deletes;
- `02 03` query;
- AP fallback;
- arbitrary URL input;
- reset/restart/OTA.

Run exactly once and return the complete sanitized report. **Stop before G5.**
