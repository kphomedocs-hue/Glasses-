# Virtual AIMB-G1 Lab — 96 PLANNED test scenarios
**Updated 2026-10-11:** all 96 planned IDs have executed OFFLINE synthetic safety-subset assertions. Verified source 34a218ccec0116909cc07fbc75c2a729b63a791a; GitHub Actions 38131350988 SUCCESS. **No complete production-integrated scenario is certified; every row remains INTEGRATION PENDING.** No emulator or physical gate promotion.

See [strategy](G6A_VIRTUAL_GLASSES_LAB_STRATEGY_2026-10-11.md). Only synthetic fixtures and sanitized generated reports may enter public GitHub. Every test must execute against shared application core; mocks are not evidence of actual current glasses firmware.

## Scenario packs (12 × 8 = 96 planned)

### 1. BLE connection and command lifecycle (G1/G2/G3B)

| ID | Synthetic stimulus | Required assertion | Status |
|---|---|---|---|
| VGL-01-01 | Unique bonded target and valid services | single selected BLE target; read/notify setup succeeds; no unintended write | OFFLINE SUBSET RUN; INTEGRATION PENDING |
| VGL-01-02 | No bonded AIMB-G1 target | fail closed before GATT; zero P2P/HTTP | OFFLINE SUBSET RUN; INTEGRATION PENDING |
| VGL-01-03 | Two bonded lookalikes | abort ambiguous device selection without guessing | OFFLINE SUBSET RUN; INTEGRATION PENDING |
| VGL-01-04 | GATT connect callback never arrives | independent deadline terminates; no P2P | OFFLINE SUBSET RUN; INTEGRATION PENDING |
| VGL-01-05 | CCCD subscription fails | no ENTER, no peer acceptance | OFFLINE SUBSET RUN; INTEGRATION PENDING |
| VGL-01-06 | ENTER BLE write callback fails | fail closed with bounded exit/cleanup decision | OFFLINE SUBSET RUN; INTEGRATION PENDING |
| VGL-01-07 | ENTER callback delayed beyond discovery result | never treat early peer snapshot as readiness | OFFLINE SUBSET RUN; INTEGRATION PENDING |
| VGL-01-08 | EXIT BLE callback delayed | do not issue overlapping exit writes; final state indeterminate unless verified | OFFLINE SUBSET RUN; INTEGRATION PENDING |

### 2. 0x41 response and 0x73 event handling (G2/G2B/G3B/G4A2)

| ID | Synthetic stimulus | Required assertion | Status |
|---|---|---|---|
| VGL-02-01 | Historically valid long credential response | parse frame while treating name only as a candidate | OFFLINE SUBSET RUN; INTEGRATION PENDING |
| VGL-02-02 | Five-byte short 0x41 response | record frame shape only; do not invent meaning | OFFLINE SUBSET RUN; INTEGRATION PENDING |
| VGL-02-03 | CRC-corrupt 0x41 | reject; never change transfer state | OFFLINE SUBSET RUN; INTEGRATION PENDING |
| VGL-02-04 | Invalid length field | reject without crashing or allocating large buffer | OFFLINE SUBSET RUN; INTEGRATION PENDING |
| VGL-02-05 | 0x73/01 inventory notification | update bounded counts with typed evidence | OFFLINE SUBSET RUN; INTEGRATION PENDING |
| VGL-02-06 | 0x73/08 IP before valid association | do not enable local media request | OFFLINE SUBSET RUN; INTEGRATION PENDING |
| VGL-02-07 | 0x73/08 IP after formed verified group | check group/network provenance before catalogue request | OFFLINE SUBSET RUN; INTEGRATION PENDING |
| VGL-02-08 | Out-of-order duplicated 0x73 notifications | no double import or unbounded event memory | OFFLINE SUBSET RUN; INTEGRATION PENDING |

### 3. Peer identity and adversarial advertisements (G4A/G6A)

| ID | Synthetic stimulus | Required assertion | Status |
|---|---|---|---|
| VGL-03-01 | Exact derived peer name | candidate only; not authenticated or connection-authorized | OFFLINE SUBSET RUN; INTEGRATION PENDING |
| VGL-03-02 | Normalized full Bluetooth-address suffix | candidate only; no generic-peer fallback | OFFLINE SUBSET RUN; INTEGRATION PENDING |
| VGL-03-03 | Raw colon-formatted address suffix | separate research bucket; not equal to normalized predicate | OFFLINE SUBSET RUN; INTEGRATION PENDING |
| VGL-03-04 | Generic AIMB-G1 prefix only | do not select this peer | OFFLINE SUBSET RUN; INTEGRATION PENDING |
| VGL-03-05 | Sole OTHER peer | NO_CANDIDATE; no connect | OFFLINE SUBSET RUN; INTEGRATION PENDING |
| VGL-03-06 | Two plausible peers | AMBIGUOUS; no connect | OFFLINE SUBSET RUN; INTEGRATION PENDING |
| VGL-03-07 | Spoofed exact name plus unrelated peer address | no automatic trust based on name alone | OFFLINE SUBSET RUN; INTEGRATION PENDING |
| VGL-03-08 | BLE and P2P MAC differ | never assume same interface address | OFFLINE SUBSET RUN; INTEGRATION PENDING |

### 4. Wi-Fi Direct group, timers and cleanup (G4A/G4A2/G7)

| ID | Synthetic stimulus | Required assertion | Status |
|---|---|---|---|
| VGL-04-01 | Group present before test | read-only preflight abort; preserve existing group | OFFLINE SUBSET RUN; INTEGRATION PENDING |
| VGL-04-02 | No group and valid simulated peer | association allowed only in approved test adapter | OFFLINE SUBSET RUN; INTEGRATION PENDING |
| VGL-04-03 | Discovery succeeds but zero peers | not proof glasses absent; bounded timeout | OFFLINE SUBSET RUN; INTEGRATION PENDING |
| VGL-04-04 | Group formed without selected peer membership | reject group as unverified | OFFLINE SUBSET RUN; INTEGRATION PENDING |
| VGL-04-05 | removeGroup callback reports BUSY | inspect group; no success declaration without absence | OFFLINE SUBSET RUN; INTEGRATION PENDING |
| VGL-04-06 | Discovery and BLE timers overlap | independent timers never cancel each other | OFFLINE SUBSET RUN; INTEGRATION PENDING |
| VGL-04-07 | App activity destroyed mid-P2P | request safe teardown and mark uncertain if not verified | OFFLINE SUBSET RUN; INTEGRATION PENDING |
| VGL-04-08 | Repeated cleanup callback after terminal | idempotent final state, no duplicate writes | OFFLINE SUBSET RUN; INTEGRATION PENDING |

### 5. Catalog parsing and privacy (G4B/G6A)

| ID | Synthetic stimulus | Required assertion | Status |
|---|---|---|---|
| VGL-05-01 | configFileType=1 line-list catalogue | parse safe relative JPG/OPUS/MP4 paths, correct counts | OFFLINE SUBSET RUN; INTEGRATION PENDING |
| VGL-05-02 | configFileType=2 JSON catalogue | parse only when explicitly selected, not on type1 | OFFLINE SUBSET RUN; INTEGRATION PENDING |
| VGL-05-03 | Malformed UTF-8 or binary body | reject without saving raw body | OFFLINE SUBSET RUN; INTEGRATION PENDING |
| VGL-05-04 | Blank catalog lines | ignore/count explicitly as policy, never fabricate entry | OFFLINE SUBSET RUN; INTEGRATION PENDING |
| VGL-05-05 | Relative path traversal or absolute URL | reject; no HTTP fetch | OFFLINE SUBSET RUN; INTEGRATION PENDING |
| VGL-05-06 | Repeated identical entries | deduplicate safely or abort ambiguous new delta | OFFLINE SUBSET RUN; INTEGRATION PENDING |
| VGL-05-07 | Catalogue response exceeds byte limit | abort boundedly before allocation overflow | OFFLINE SUBSET RUN; INTEGRATION PENDING |
| VGL-05-08 | Private names and paths in raw input | reports contain only safe aggregate counts | OFFLINE SUBSET RUN; INTEGRATION PENDING |

### 6. Photo capture visibility and selection (G5/G6A)

| ID | Synthetic stimulus | Required assertion | Status |
|---|---|---|---|
| VGL-06-01 | One photo appears immediately | exact +1 file set accepted if all independent checks pass | OFFLINE SUBSET RUN; INTEGRATION PENDING |
| VGL-06-02 | One photo appears after delayed notification | bounded wait and set delta, not first/last index | OFFLINE SUBSET RUN; INTEGRATION PENDING |
| VGL-06-03 | No new photo before deadline | no GET; report no-delta | OFFLINE SUBSET RUN; INTEGRATION PENDING |
| VGL-06-04 | Two new files after one capture | fail closed on +2 ambiguity | OFFLINE SUBSET RUN; INTEGRATION PENDING |
| VGL-06-05 | New file is OPUS rather than JPG | do not enter JPG-only transfer gate | OFFLINE SUBSET RUN; INTEGRATION PENDING |
| VGL-06-06 | Pre-existing catalog reshuffled | never infer newest from first/last position | OFFLINE SUBSET RUN; INTEGRATION PENDING |
| VGL-06-07 | Pre-GET reconnect retains unconsumed new JPG | no false missing-asset flag | OFFLINE SUBSET RUN; INTEGRATION PENDING |
| VGL-06-08 | Inventory and catalog counts disagree | no arbitrary media selection | OFFLINE SUBSET RUN; INTEGRATION PENDING |

### 7. Local HTTP faults and remote-consumptive model (G5.7/G6A/G7)

| ID | Synthetic stimulus | Required assertion | Status |
|---|---|---|---|
| VGL-07-01 | Complete synthetic HTTP 200 JPEG response | single request and verified local commit | OFFLINE SUBSET RUN; INTEGRATION PENDING |
| VGL-07-02 | Server sends 500/404 | no final artifact; remote uncertain after request | OFFLINE SUBSET RUN; INTEGRATION PENDING |
| VGL-07-03 | Response content length is too small | reject mismatched byte count | OFFLINE SUBSET RUN; INTEGRATION PENDING |
| VGL-07-04 | Response content length is too large | abort at cap, preserve uncertain trace | OFFLINE SUBSET RUN; INTEGRATION PENDING |
| VGL-07-05 | Connection drops mid-body | quarantine partial; no automatic second GET | OFFLINE SUBSET RUN; INTEGRATION PENDING |
| VGL-07-06 | Response stalls after headers | deadline, no retry, no success receipt | OFFLINE SUBSET RUN; INTEGRATION PENDING |
| VGL-07-07 | Server consumes item at completed GET | remote absence does not invalidate local receipt | OFFLINE SUBSET RUN; INTEGRATION PENDING |
| VGL-07-08 | Server consumes item only after EXIT | model distinct semantics, require safe exit/receipt order | OFFLINE SUBSET RUN; INTEGRATION PENDING |

### 8. JPG, MP4 and OPUS synthetic validation (G6B/G7)

| ID | Synthetic stimulus | Required assertion | Status |
|---|---|---|---|
| VGL-08-01 | Valid synthetic JPG framing and exact count | JPG validator accepts only bounded complete bytes | OFFLINE SUBSET RUN; INTEGRATION PENDING |
| VGL-08-02 | Fake JPG with valid markers but invalid codec body | full decoder check planned; no false final fidelity proof | OFFLINE SUBSET RUN; INTEGRATION PENDING |
| VGL-08-03 | Valid synthetic MP4 container | format-specific validator required; no JPG assumptions | OFFLINE SUBSET RUN; INTEGRATION PENDING |
| VGL-08-04 | Truncated MP4 atom/mdat | reject/flag corrupt without committing | OFFLINE SUBSET RUN; INTEGRATION PENDING |
| VGL-08-05 | Valid synthetic OPUS payload | format-specific validation and metadata test | OFFLINE SUBSET RUN; INTEGRATION PENDING |
| VGL-08-06 | Mislabelled OPUS with JPG extension | reject kind mismatch | OFFLINE SUBSET RUN; INTEGRATION PENDING |
| VGL-08-07 | Large video near configured cap | bounded memory, streaming and storage pressure checks | OFFLINE SUBSET RUN; INTEGRATION PENDING |
| VGL-08-08 | Zero-byte or missing-media body | reject, preserve fetch-uncertainty record | OFFLINE SUBSET RUN; INTEGRATION PENDING |

### 9. Durable ledger and crash recovery (G6A/G7)

| ID | Synthetic stimulus | Required assertion | Status |
|---|---|---|---|
| VGL-09-01 | Before durable fetch intent | no source opened; safe NOT_FETCHED | OFFLINE SUBSET RUN; INTEGRATION PENDING |
| VGL-09-02 | Immediately after fetch-may-have-started marker | REMOTE_UNCERTAIN on restart, never auto-GET | OFFLINE SUBSET RUN; INTEGRATION PENDING |
| VGL-09-03 | Midstream partial write | preserve part, quarantine, never auto-GET | OFFLINE SUBSET RUN; INTEGRATION PENDING |
| VGL-09-04 | After length/validation before VERIFIED marker | manual review; no false commit | OFFLINE SUBSET RUN; INTEGRATION PENDING |
| VGL-09-05 | After VERIFIED marker before final rename | verify staged local bytes and commit locally | OFFLINE SUBSET RUN; INTEGRATION PENDING |
| VGL-09-06 | After final rename before receipt | reconstruct receipt with no network call | OFFLINE SUBSET RUN; INTEGRATION PENDING |
| VGL-09-07 | After receipt before final journal state | verify both and idempotently finish | OFFLINE SUBSET RUN; INTEGRATION PENDING |
| VGL-09-08 | Corrupted committed final file | fail closed; never silently trust ledger | OFFLINE SUBSET RUN; INTEGRATION PENDING |

### 10. Deterministic naming, timestamps and deduplication (G6/G7)

| ID | Synthetic stimulus | Required assertion | Status |
|---|---|---|---|
| VGL-10-01 | Two JPGs same calendar day | unique sequential names, no overwrite | OFFLINE SUBSET RUN; INTEGRATION PENDING |
| VGL-10-02 | JPG and OPUS alternate | same shared daily counter | OFFLINE SUBSET RUN; INTEGRATION PENDING |
| VGL-10-03 | Photo capture exactly at local midnight | correct date bucket and rollover | OFFLINE SUBSET RUN; INTEGRATION PENDING |
| VGL-10-04 | Two captures share same timestamp | distinct stable identities and paths | OFFLINE SUBSET RUN; INTEGRATION PENDING |
| VGL-10-05 | Reserved sequence dies before import | do not overwrite or reuse unresolved slot | OFFLINE SUBSET RUN; INTEGRATION PENDING |
| VGL-10-06 | Repeat catalog presents consumed remote identity | existing durable receipt prevents GET | OFFLINE SUBSET RUN; INTEGRATION PENDING |
| VGL-10-07 | Remote path reused for a different file | require stronger identity context; prevent silent collision | OFFLINE SUBSET RUN; INTEGRATION PENDING |
| VGL-10-08 | Device clock changes or timezone differs | separate first-seen/device timestamp semantics | OFFLINE SUBSET RUN; INTEGRATION PENDING |

### 11. Android lifecycle and permissions (G7/production)

| ID | Synthetic stimulus | Required assertion | Status |
|---|---|---|---|
| VGL-11-01 | Nearby Wi-Fi permission denied | abort without unsafe fallback | OFFLINE SUBSET RUN; INTEGRATION PENDING |
| VGL-11-02 | BLE connect permission revoked mid-run | fail closed and clean up | OFFLINE SUBSET RUN; INTEGRATION PENDING |
| VGL-11-03 | Location Mode disables peer discovery | accurately classify platform-blocked vs no target | OFFLINE SUBSET RUN; INTEGRATION PENDING |
| VGL-11-04 | App backgrounded during discovery | state preserved, bounded timeout, no hidden GET | OFFLINE SUBSET RUN; INTEGRATION PENDING |
| VGL-11-05 | Process killed during transfer | recovery no blind resume | OFFLINE SUBSET RUN; INTEGRATION PENDING |
| VGL-11-06 | Phone reboot with pending import | do not auto-GET uncertain file | OFFLINE SUBSET RUN; INTEGRATION PENDING |
| VGL-11-07 | Low storage and disk-full during write | durable error state, no reported success | OFFLINE SUBSET RUN; INTEGRATION PENDING |
| VGL-11-08 | App upgrade from old ledger schema | validated migration, no duplicate/corrupt receipts | OFFLINE SUBSET RUN; INTEGRATION PENDING |

### 12. Privacy, build separation and release (G0/G7/release)

| ID | Synthetic stimulus | Required assertion | Status |
|---|---|---|---|
| VGL-12-01 | Synthetic media in public CI only | no client media or vendor APK uploaded | OFFLINE SUBSET RUN; INTEGRATION PENDING |
| VGL-12-02 | Debug simulator endpoint configured | production flavor fails build or strips endpoint | OFFLINE SUBSET RUN; INTEGRATION PENDING |
| VGL-12-03 | Real BLE address appears in report input | sanitizer removes/blocks public output | OFFLINE SUBSET RUN; INTEGRATION PENDING |
| VGL-12-04 | Raw catalog filename in exception | exception redaction prevents leak | OFFLINE SUBSET RUN; INTEGRATION PENDING |
| VGL-12-05 | Unknown peer encountered | no automatic connection or HTTP | OFFLINE SUBSET RUN; INTEGRATION PENDING |
| VGL-12-06 | Media response redirects to outside network | redirect blocked | OFFLINE SUBSET RUN; INTEGRATION PENDING |
| VGL-12-07 | Arbitrary retry/Range/HEAD added by transport | explicit allow-list tests fail build | OFFLINE SUBSET RUN; INTEGRATION PENDING |
| VGL-12-08 | CI PASS but no physical evidence | G6A/G6B real gate remains OPEN/BLOCKED | OFFLINE SUBSET RUN; INTEGRATION PENDING |

## Pass/fail evidence rules
1. A PASS requires automated test execution against the same policy, state machine, persistence and validator code paths that would ship. A scenario outline or synthetically expected result never counts as a pass.
2. Separate JVM/unit, one-emulator Android/HTTP, dual-AVD P2P and real-glasses results. The last category cannot be replaced by the others.
3. A test that depends on physical firmware is NOT TESTABLE IN SIMULATION and must be tracked as BLOCKED/HARDWARE; do not fake a device-specific result.
4. The owner-glasses remote-original preservation requirement remains a blocking product gate. No automated media GET on real glasses is authorized by this matrix.
5. CI must fail on any unexpected duplicate GET, wrong-peer connect, timeout bypass, omitted EXIT/cleanup verification, raw identifier leak or false COMMITTED outcome.

## Suggested next concrete work
All 96 source-only subsets now execute. Next, unify candidate policies within real Android production code paths, then run a test-only single-emulator HTTP/lifecycle harness, and only if feasible a separate two-emulator Wi-Fi Direct experiment. Obtain independent physical-test consent; never infer a hardware result from synthetic PASS.
