# Virtual AIMB-G1 Lab — 96 PLANNED test scenarios
**Created 2026-10-11** · All entries are **PLANNED / NOT RUN**. Passing legacy unit tests are separate evidence; no device gate promoted.

See [strategy](G6A_VIRTUAL_GLASSES_LAB_STRATEGY_2026-10-11.md). Only synthetic fixtures and sanitized generated reports may enter public GitHub. Every test must execute against shared application core; mocks are not evidence of actual current glasses firmware.

## Scenario packs (12 × 8 = 96 planned)

### 1. BLE connection and command lifecycle (G1/G2/G3B)

| ID | Synthetic stimulus | Required assertion | Status |
|---|---|---|---|
| VGL-01-01 | Unique bonded target and valid services | single selected BLE target; read/notify setup succeeds; no unintended write | NOT RUN |
| VGL-01-02 | No bonded AIMB-G1 target | fail closed before GATT; zero P2P/HTTP | NOT RUN |
| VGL-01-03 | Two bonded lookalikes | abort ambiguous device selection without guessing | NOT RUN |
| VGL-01-04 | GATT connect callback never arrives | independent deadline terminates; no P2P | NOT RUN |
| VGL-01-05 | CCCD subscription fails | no ENTER, no peer acceptance | NOT RUN |
| VGL-01-06 | ENTER BLE write callback fails | fail closed with bounded exit/cleanup decision | NOT RUN |
| VGL-01-07 | ENTER callback delayed beyond discovery result | never treat early peer snapshot as readiness | NOT RUN |
| VGL-01-08 | EXIT BLE callback delayed | do not issue overlapping exit writes; final state indeterminate unless verified | NOT RUN |

### 2. 0x41 response and 0x73 event handling (G2/G2B/G3B/G4A2)

| ID | Synthetic stimulus | Required assertion | Status |
|---|---|---|---|
| VGL-02-01 | Historically valid long credential response | parse frame while treating name only as a candidate | NOT RUN |
| VGL-02-02 | Five-byte short 0x41 response | record frame shape only; do not invent meaning | NOT RUN |
| VGL-02-03 | CRC-corrupt 0x41 | reject; never change transfer state | NOT RUN |
| VGL-02-04 | Invalid length field | reject without crashing or allocating large buffer | NOT RUN |
| VGL-02-05 | 0x73/01 inventory notification | update bounded counts with typed evidence | NOT RUN |
| VGL-02-06 | 0x73/08 IP before valid association | do not enable local media request | NOT RUN |
| VGL-02-07 | 0x73/08 IP after formed verified group | check group/network provenance before catalogue request | NOT RUN |
| VGL-02-08 | Out-of-order duplicated 0x73 notifications | no double import or unbounded event memory | NOT RUN |

### 3. Peer identity and adversarial advertisements (G4A/G6A)

| ID | Synthetic stimulus | Required assertion | Status |
|---|---|---|---|
| VGL-03-01 | Exact derived peer name | candidate only; not authenticated or connection-authorized | NOT RUN |
| VGL-03-02 | Normalized full Bluetooth-address suffix | candidate only; no generic-peer fallback | NOT RUN |
| VGL-03-03 | Raw colon-formatted address suffix | separate research bucket; not equal to normalized predicate | NOT RUN |
| VGL-03-04 | Generic AIMB-G1 prefix only | do not select this peer | NOT RUN |
| VGL-03-05 | Sole OTHER peer | NO_CANDIDATE; no connect | NOT RUN |
| VGL-03-06 | Two plausible peers | AMBIGUOUS; no connect | NOT RUN |
| VGL-03-07 | Spoofed exact name plus unrelated peer address | no automatic trust based on name alone | NOT RUN |
| VGL-03-08 | BLE and P2P MAC differ | never assume same interface address | NOT RUN |

### 4. Wi-Fi Direct group, timers and cleanup (G4A/G4A2/G7)

| ID | Synthetic stimulus | Required assertion | Status |
|---|---|---|---|
| VGL-04-01 | Group present before test | read-only preflight abort; preserve existing group | NOT RUN |
| VGL-04-02 | No group and valid simulated peer | association allowed only in approved test adapter | NOT RUN |
| VGL-04-03 | Discovery succeeds but zero peers | not proof glasses absent; bounded timeout | NOT RUN |
| VGL-04-04 | Group formed without selected peer membership | reject group as unverified | NOT RUN |
| VGL-04-05 | removeGroup callback reports BUSY | inspect group; no success declaration without absence | NOT RUN |
| VGL-04-06 | Discovery and BLE timers overlap | independent timers never cancel each other | NOT RUN |
| VGL-04-07 | App activity destroyed mid-P2P | request safe teardown and mark uncertain if not verified | NOT RUN |
| VGL-04-08 | Repeated cleanup callback after terminal | idempotent final state, no duplicate writes | NOT RUN |

### 5. Catalog parsing and privacy (G4B/G6A)

| ID | Synthetic stimulus | Required assertion | Status |
|---|---|---|---|
| VGL-05-01 | configFileType=1 line-list catalogue | parse safe relative JPG/OPUS/MP4 paths, correct counts | NOT RUN |
| VGL-05-02 | configFileType=2 JSON catalogue | parse only when explicitly selected, not on type1 | NOT RUN |
| VGL-05-03 | Malformed UTF-8 or binary body | reject without saving raw body | NOT RUN |
| VGL-05-04 | Blank catalog lines | ignore/count explicitly as policy, never fabricate entry | NOT RUN |
| VGL-05-05 | Relative path traversal or absolute URL | reject; no HTTP fetch | NOT RUN |
| VGL-05-06 | Repeated identical entries | deduplicate safely or abort ambiguous new delta | NOT RUN |
| VGL-05-07 | Catalogue response exceeds byte limit | abort boundedly before allocation overflow | NOT RUN |
| VGL-05-08 | Private names and paths in raw input | reports contain only safe aggregate counts | NOT RUN |

### 6. Photo capture visibility and selection (G5/G6A)

| ID | Synthetic stimulus | Required assertion | Status |
|---|---|---|---|
| VGL-06-01 | One photo appears immediately | exact +1 file set accepted if all independent checks pass | NOT RUN |
| VGL-06-02 | One photo appears after delayed notification | bounded wait and set delta, not first/last index | NOT RUN |
| VGL-06-03 | No new photo before deadline | no GET; report no-delta | NOT RUN |
| VGL-06-04 | Two new files after one capture | fail closed on +2 ambiguity | NOT RUN |
| VGL-06-05 | New file is OPUS rather than JPG | do not enter JPG-only transfer gate | NOT RUN |
| VGL-06-06 | Pre-existing catalog reshuffled | never infer newest from first/last position | NOT RUN |
| VGL-06-07 | Pre-GET reconnect retains unconsumed new JPG | no false missing-asset flag | NOT RUN |
| VGL-06-08 | Inventory and catalog counts disagree | no arbitrary media selection | NOT RUN |

### 7. Local HTTP faults and remote-consumptive model (G5.7/G6A/G7)

| ID | Synthetic stimulus | Required assertion | Status |
|---|---|---|---|
| VGL-07-01 | Complete synthetic HTTP 200 JPEG response | single request and verified local commit | NOT RUN |
| VGL-07-02 | Server sends 500/404 | no final artifact; remote uncertain after request | NOT RUN |
| VGL-07-03 | Response content length is too small | reject mismatched byte count | NOT RUN |
| VGL-07-04 | Response content length is too large | abort at cap, preserve uncertain trace | NOT RUN |
| VGL-07-05 | Connection drops mid-body | quarantine partial; no automatic second GET | NOT RUN |
| VGL-07-06 | Response stalls after headers | deadline, no retry, no success receipt | NOT RUN |
| VGL-07-07 | Server consumes item at completed GET | remote absence does not invalidate local receipt | NOT RUN |
| VGL-07-08 | Server consumes item only after EXIT | model distinct semantics, require safe exit/receipt order | NOT RUN |

### 8. JPG, MP4 and OPUS synthetic validation (G6B/G7)

| ID | Synthetic stimulus | Required assertion | Status |
|---|---|---|---|
| VGL-08-01 | Valid synthetic JPG framing and exact count | JPG validator accepts only bounded complete bytes | NOT RUN |
| VGL-08-02 | Fake JPG with valid markers but invalid codec body | full decoder check planned; no false final fidelity proof | NOT RUN |
| VGL-08-03 | Valid synthetic MP4 container | format-specific validator required; no JPG assumptions | NOT RUN |
| VGL-08-04 | Truncated MP4 atom/mdat | reject/flag corrupt without committing | NOT RUN |
| VGL-08-05 | Valid synthetic OPUS payload | format-specific validation and metadata test | NOT RUN |
| VGL-08-06 | Mislabelled OPUS with JPG extension | reject kind mismatch | NOT RUN |
| VGL-08-07 | Large video near configured cap | bounded memory, streaming and storage pressure checks | NOT RUN |
| VGL-08-08 | Zero-byte or missing-media body | reject, preserve fetch-uncertainty record | NOT RUN |

### 9. Durable ledger and crash recovery (G6A/G7)

| ID | Synthetic stimulus | Required assertion | Status |
|---|---|---|---|
| VGL-09-01 | Before durable fetch intent | no source opened; safe NOT_FETCHED | NOT RUN |
| VGL-09-02 | Immediately after fetch-may-have-started marker | REMOTE_UNCERTAIN on restart, never auto-GET | NOT RUN |
| VGL-09-03 | Midstream partial write | preserve part, quarantine, never auto-GET | NOT RUN |
| VGL-09-04 | After length/validation before VERIFIED marker | manual review; no false commit | NOT RUN |
| VGL-09-05 | After VERIFIED marker before final rename | verify staged local bytes and commit locally | NOT RUN |
| VGL-09-06 | After final rename before receipt | reconstruct receipt with no network call | NOT RUN |
| VGL-09-07 | After receipt before final journal state | verify both and idempotently finish | NOT RUN |
| VGL-09-08 | Corrupted committed final file | fail closed; never silently trust ledger | NOT RUN |

### 10. Deterministic naming, timestamps and deduplication (G6/G7)

| ID | Synthetic stimulus | Required assertion | Status |
|---|---|---|---|
| VGL-10-01 | Two JPGs same calendar day | unique sequential names, no overwrite | NOT RUN |
| VGL-10-02 | JPG and OPUS alternate | same shared daily counter | NOT RUN |
| VGL-10-03 | Photo capture exactly at local midnight | correct date bucket and rollover | NOT RUN |
| VGL-10-04 | Two captures share same timestamp | distinct stable identities and paths | NOT RUN |
| VGL-10-05 | Reserved sequence dies before import | do not overwrite or reuse unresolved slot | NOT RUN |
| VGL-10-06 | Repeat catalog presents consumed remote identity | existing durable receipt prevents GET | NOT RUN |
| VGL-10-07 | Remote path reused for a different file | require stronger identity context; prevent silent collision | NOT RUN |
| VGL-10-08 | Device clock changes or timezone differs | separate first-seen/device timestamp semantics | NOT RUN |

### 11. Android lifecycle and permissions (G7/production)

| ID | Synthetic stimulus | Required assertion | Status |
|---|---|---|---|
| VGL-11-01 | Nearby Wi-Fi permission denied | abort without unsafe fallback | NOT RUN |
| VGL-11-02 | BLE connect permission revoked mid-run | fail closed and clean up | NOT RUN |
| VGL-11-03 | Location Mode disables peer discovery | accurately classify platform-blocked vs no target | NOT RUN |
| VGL-11-04 | App backgrounded during discovery | state preserved, bounded timeout, no hidden GET | NOT RUN |
| VGL-11-05 | Process killed during transfer | recovery no blind resume | NOT RUN |
| VGL-11-06 | Phone reboot with pending import | do not auto-GET uncertain file | NOT RUN |
| VGL-11-07 | Low storage and disk-full during write | durable error state, no reported success | NOT RUN |
| VGL-11-08 | App upgrade from old ledger schema | validated migration, no duplicate/corrupt receipts | NOT RUN |

### 12. Privacy, build separation and release (G0/G7/release)

| ID | Synthetic stimulus | Required assertion | Status |
|---|---|---|---|
| VGL-12-01 | Synthetic media in public CI only | no client media or vendor APK uploaded | NOT RUN |
| VGL-12-02 | Debug simulator endpoint configured | production flavor fails build or strips endpoint | NOT RUN |
| VGL-12-03 | Real BLE address appears in report input | sanitizer removes/blocks public output | NOT RUN |
| VGL-12-04 | Raw catalog filename in exception | exception redaction prevents leak | NOT RUN |
| VGL-12-05 | Unknown peer encountered | no automatic connection or HTTP | NOT RUN |
| VGL-12-06 | Media response redirects to outside network | redirect blocked | NOT RUN |
| VGL-12-07 | Arbitrary retry/Range/HEAD added by transport | explicit allow-list tests fail build | NOT RUN |
| VGL-12-08 | CI PASS but no physical evidence | G6A/G6B real gate remains OPEN/BLOCKED | NOT RUN |

## Pass/fail evidence rules
1. A PASS requires automated test execution against the same policy, state machine, persistence and validator code paths that would ship. A scenario outline or synthetically expected result never counts as a pass.
2. Separate JVM/unit, one-emulator Android/HTTP, dual-AVD P2P and real-glasses results. The last category cannot be replaced by the others.
3. A test that depends on physical firmware is NOT TESTABLE IN SIMULATION and must be tracked as BLOCKED/HARDWARE; do not fake a device-specific result.
4. The owner-glasses remote-original preservation requirement remains a blocking product gate. No automated media GET on real glasses is authorized by this matrix.
5. CI must fail on any unexpected duplicate GET, wrong-peer connect, timeout bypass, omitted EXIT/cleanup verification, raw identifier leak or false COMMITTED outcome.

## Suggested next concrete work
Implement the first 24 scenarios (packs 1–3) with a deterministic virtual event-clock harness, followed by groups 4–6. Then build a MockWebServer single-emulator Android harness; only then test dual-emulator 36.5+ feasibility. Freeze reproducible fixture IDs and seeds.
