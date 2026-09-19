# AIMB-G1 / K Site Capture

Authoritative repository for the AIMB-G1 glasses media-capture integration project.

## Current project state

Evidence-gated status as of 2026-09-19:

- **G0 — software/recovery readiness:** PASS
- **G1 — physical GATT confirmation:** PASS
- **G2 — response-channel confirmation:** PASS
- **G2B — response-semantic correlation:** PASS
- **G2C — initialization parity:** PASS
- **G3 — media-count query:** PASS
- **G3B — P2P transfer lifecycle:** PASS
- **G4A — phone-side Wi-Fi Direct association:** PASS
- **G4A2 — passive glasses P2P-IP notification capture:** PASS
- **G4B — read-only media listing:** HTTP/body read reached; JSON parser unresolved
- **G4B2 — catalog response-shape characterization:** PASS
- **G4B3 — exact line-list parser parity:** SOFTWARE BUILD/VERIFICATION NEXT

G4A2 physically confirmed the glasses-side Wi-Fi Direct client address as `192.168.49.176`. G4B v0.4.2 then reached the exact `/files/media.config` local read path with one GET, but Android `JSONObject` parsing failed with `JSONException`. Exact Cyan bytecode confirms it reads the whole file as text and hands that string to Moshi, so G4B2 now characterizes the physical response safely without widening network scope.

Authoritative checkpoint:
- `LATEST_CHECKPOINT.md`
- `docs/testing/GATE_ROADMAP.md`
- tracked work: GitHub Issue #9

## Confirmed physical AIMB-G1 profile

G1 physically confirmed the Cyan-family BLE profile:

```text
Service  de5bf728-d711-4e47-af26-65e3012a5dc7
Notify   de5bf729-d711-4e47-af26-65e3012a5dc7
Write    de5bf72a-d711-4e47-af26-65e3012a5dc7
```

Sanitized evidence:
`docs/testing/results/2026-09-19_G1_PHYSICAL_DISCOVERY_PASS.md`

The physical hardware revision read successfully as `AM01SPG1_V1.4`.

## Confirmed response channel

K G1 Response Probe v0.2 enabled only the standard CCCD notification subscription on the confirmed Cyan notify characteristic. No proprietary characteristic write was implemented or sent.

During the 30-second passive observation, the glasses emitted three valid framed notifications:

```text
BC 73 03 00 52 31 05 47 00
BC 73 08 00 01 07 01 01 00 00 00 01 00 01
BC 73 03 00 53 A1 05 46 00
```

All three validate against the recovered Cyan envelope:
`BC | command | len_le16 | crc16_modbus(payload) | payload`.

Sanitized evidence:
`docs/testing/results/2026-09-19_G2_NOTIFICATION_ONLY_PASS.md`

The observed `0x73` semantics are now resolved by the user's exact Cyan APK:
- `0x01` = media inventory/count/config report,
- `0x05` = battery/charging-family report.

See `docs/research/CYAN_EXACT_G2B_TRACE_2026-09-19.md`.

## Current verified diagnostic builds

### Frozen reference — K G1 Discovery v0.1

- APK: `releases/v0.1/K_G1_Discovery_v0_1.apk`
- APK SHA-256: `c32758f898b91041bc7e13a272096d2629836e4465542ee00c1fbd9764e7479a`
- frozen source SHA-256: `84fb1b957290abdf2464a97b35b81f2d1e8e976ce9c067ecaf571c8c19f22c68`
- frozen branch: `frozen/discovery-v0.1-readonly`

### G1 diagnostic — K G1 Discovery v0.1.2

- APK: `releases/v0.1.2/K_G1_Discovery_v0_1_2.apk`
- APK SHA-256: `dc08b915581468f2d0d33fb317ca06b05e301b233cb0ced234edf7f0bc282297`
- build/verification run: `35395830630`

### G2 response probe — K G1 Response Probe v0.2

- APK: `releases/v0.2/K_G1_Response_Probe_v0_2.apk`
- APK SHA-256: `86b9738ac909577b173264be233242c17c9715be5bae183fffa6c9d497174ff2`
- source SHA-256: `86fa0f9f72171cb90c4edbc04d1a2db2b2c40c19c976978ea08a63c399700d39`
- package SHA-256: `b4d2fe59d87866dd993454ff58b53d18bf605891c74d2c9ddce1e9f40af18924`
- build/verification run: `35413378362`

## Next method — G2C one-command initialization parity

The exact Cyan receive/init path has been traced, so a passive correlator is no longer required before progression.

The next build must:
- keep the verified bonded-device + notify-subscription path,
- generate Cyan's exact 9-byte dynamic time payload,
- frame it as command `0x40` with the validated length/CRC envelope,
- write exactly that one frame to the confirmed Cyan write characteristic,
- capture the `0x40` response and any `0x73` events,
- disconnect.

It must not contain a `0x41` media/control command, P2P/AP activation, HTTP transfer, reset/OTA operation or generic raw-command input.

Exact trace:
`docs/research/CYAN_EXACT_G2B_TRACE_2026-09-19.md`

The passive event-correlation plan remains in the repository as a fallback for future unknown event types.

## Architecture

See:
- `docs/architecture/ARCHITECTURE.md`
- `docs/architecture/MIGRATION_PLAN.md`
- `PROJECT_MANIFEST.md`

Repository roles:
- `apps/` — product/app lineages
- `modules/` — target modular production boundaries
- `source/v0.1/` — frozen Discovery source
- `source/v0.1.2/` — successful G1 diagnostic source
- `source/v0.2/` — successful G2 notification-only source
- `releases/` — verified installable diagnostics and provenance
- `archive/site-capture/v0.4/` — preserved legacy media-sync engine/reference
- `docs/protocol/` — protocol evidence independent of execution code
- `docs/testing/` — physical gate rules and sanitized results
- `docs/research/` — provenance of third-party research inputs

## Legacy K Site Capture work

The useful v0.4 source tree is preserved under:
`archive/site-capture/v0.4/`

It retains the transport abstraction, remote-media model, chronological import coordinator, daily numbering, streaming file writes, size verification, crash recovery, dedup ledger, OPUS support, core self-tests and historical tools.

Do not merge the legacy transport into the active diagnostic line. Reuse only proven responsibilities after the protocol gates are satisfied.

## Public-repository rule

The third-party Cyan APK, Bluetooth addresses, device-specific name suffixes, credentials, personal data, client/site media and unsanitized diagnostic captures are not committed. Store only reviewed interoperability findings and sanitized physical evidence.


## G2C verified build

K G1 Init Probe v0.2.1 is ready for the one-command physical initialization-parity test.

- APK: `releases/v0.2.1/K_G1_Init_Probe_v0_2_1.apk`
- APK SHA-256: `7c0ce5d859e79c6ee2ed05316f5f5db736919912802fbbab040ce612ea368e5c`
- build/verification run: `35416416013` — PASS

It sends exactly one dynamically generated Cyan-equivalent `0x40` time-sync frame after notification subscription, has no retry, and contains no `0x41` media/control command or network-transfer implementation.

G3 remains blocked until the G2C physical report is reviewed.


## G2C physical result

G2C passed on 2026-09-19.

The AIMB-G1 accepted exactly one Cyan-equivalent `0x40` time-sync write and returned:

```text
BC 40 01 00 BF 40 00
```

The response frame validates completely. No retry or additional proprietary write was sent.

Evidence:
`docs/testing/results/2026-09-19_G2C_TIME_SYNC_PASS.md`

### Next gate

Before entering media mode, use the exact Cyan media-count query:

```text
0x41 payload 02 04
```

This validates the glasses-control path while avoiding an intentional Wi-Fi/P2P/AP transition. P2P/AP media-mode commands remain blocked until that query is reviewed.


## G3 verified build

K G1 Media Count Probe v0.3 is ready for the physical one-query test.

- APK: `releases/v0.3/K_G1_Media_Count_Probe_v0_3.apk`
- APK SHA-256: `31191dc90dac56e9aced7db5a6f6b4c65b969f10ec6d673199ee94c6a2f204e9`
- build/verification run: `35420261264` — PASS

The build sends exactly one validated frame:

```text
BC 41 02 00 01 13 02 04
```

It captures raw response frames only. It contains no P2P/AP media-mode payload, Wi-Fi/network code, transfer code, retry loop, file mutation, reset/OTA behavior or arbitrary command input.

G3B media mode remains blocked until the physical G3 response is reviewed.


## G3 physical result

G3 passed on 2026-09-19.

The exact Cyan media-count query returned:
- 1 image,
- 0 videos,
- 1 recording,
- configFileType 1,
- onlySupportApImport false.

Evidence:
`docs/testing/results/2026-09-19_G3_MEDIA_COUNT_PASS.md`

Cyan's own routing logic therefore selects P2P for this device state.

The exact app also confirms a bounded rollback command:
- enter P2P transfer mode: `02 01 04 01`
- exit transfer mode: `02 01 09`

The next G3B diagnostic will test only that BLE lifecycle. It will not use phone-side Wi-Fi Direct or perform media transfer.


## G3B verified build

K G1 P2P Lifecycle Probe v0.3.1 is ready for physical testing.

- APK: `releases/v0.3.1/K_G1_P2P_Lifecycle_Probe_v0_3_1.apk`
- APK SHA-256: `1578b6540596625825c16a95da64f72f46ee29aaad47c866bb9025a3389dce7c`
- build/verification run: `35421429624` — PASS

The app performs only the bounded BLE lifecycle:
- enter P2P transfer mode once with `02 01 04 01`,
- observe,
- exit transfer mode once with `02 01 09`,
- observe,
- disconnect.

It contains no Android Wi-Fi Direct/network stack or media-transfer implementation.


## G3B physical result

G3B passed on 2026-09-19.

The AIMB-G1 accepted the exact Cyan P2P enter command and returned a structured transfer response containing a 20-byte SSID and 9-byte password. Credential values are deliberately omitted from the public repository.

The exact Cyan exit-transfer command was then accepted, after which normal media-inventory reporting resumed.

Evidence:
`docs/testing/results/2026-09-19_G3B_P2P_LIFECYCLE_PASS.md`

The next gate is **G4A phone-side Wi-Fi Direct discovery/association only**. HTTP and media listing remain blocked until that association path is physically confirmed.


## G4A physical result

G4A passed on 2026-09-19.

The phone discovered and associated with the exact BLE-reported glasses P2P peer. The P2P group formed successfully with the phone as group owner at `192.168.49.1`.

No HTTP/socket request, `media.config` access, file listing or download occurred.

Evidence:
`docs/testing/results/2026-09-19_G4A_P2P_ASSOCIATION_PASS.md`

Because the phone is group owner, the glasses client IP is still needed. The next gate is G4A2 passive `0x73 / 0x08` IP-notify capture only; no additional proprietary query is yet authorized.


## G4A2 verified candidate

K G1 P2P IP Notify Probe v0.4.1 was verified and physically passed G4A2.

- APK: `releases/v0.4.1/K_G1_P2P_IP_Notify_Probe_v0_4_1.apk`
- APK SHA-256: `3c9104c34fbf06fb06631a09c67cac9eab3e2a626078abcc14401cf09a2cddf3`
- canonical build commit: `a1eafae6da7202b0af32ff8ee3fc017422608a4b`
- build/verification run: `35429585616` — PASS
- archived release commit: `1291b782451b12c0bb35436c30f0b660c09b2667`

The build keeps the exact G4A enter/association/exit flow and adds only sanitized `0x73` event IDs plus event-`0x08` IPv4 parsing. It does not contain the `02 03` query, Internet permission, HTTP, sockets or media access.

Physical G4A2 result: `0x73 / 0x08` was observed and resolved the glasses client IP as `192.168.49.176`. The blocked `02 03` query was not needed, and zero HTTP/socket/media operations occurred.

Evidence: `docs/testing/results/2026-09-19_G4A2_P2P_IP_NOTIFY_PASS.md`.

Next gate: G4B3 exact line-list parser parity.

Physical G4B2 v0.4.4 established the exact response shape:
- HTTP 200 / `text/plain`,
- 67 bytes,
- valid UTF-8,
- no BOM,
- line-oriented response,
- zero media-file GETs.

Corrected exact Cyan bytecode shows:
- `configFileType == 2` → `vf_list.txt` + `readText()` + Moshi JSON;
- `configFileType != 2` → `media.config` + Kotlin `readLines()`;
- physical `configFileType=1`, so each line is a catalog media entry;
- Cyan builds each later media URL as `http://<glassDeviceWifiIP>/files/<line>`.

Evidence:
- `docs/testing/results/2026-09-19_G4B2_RESPONSE_SHAPE_PASS.md`
- `docs/research/CYAN_EXACT_G4B_TRACE_2026-09-19.md`

G4B3 will verify exact line-list parity without exposing any line/filename/path value and without requesting any media file. G5 remains blocked.
