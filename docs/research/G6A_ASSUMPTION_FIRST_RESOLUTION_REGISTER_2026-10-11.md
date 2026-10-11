# G6A Assumption-First Resolution Register
Date: 2026-10-11; status: static-only review complete, G6A OPEN, G6B BLOCKED.

## Operating constraint
Resolve all questions that can be resolved by **existing** evidence before new device interaction. A "cannot yet resolve" is a valid finding if its missing observation is precisely stated. Do not call this a full 90/90 closure. This is the operational follow-up to `ASSUMPTION_AUDIT_2026-10-11_INDEX.md` and `G6A_ASSUMPTION_FIRST_STATIC_CLOSURE_2026-10-11.md`.

## Static closures — implement no APK yet
| Topic | Closed conclusion | Reason this matters |
|---|---|---|
| BLE vs Wifi password gating | K v0.6.7 explicitly waits for SSID and password; old K Android WPS-PBC `connect` never passed that password. Cyan can construct a candidate Wi-Fi peer name on Bluetooth connect. | Stop treating a long credential-bearing 0x41 as universal precondition to *attempt identifying* a candidate |
| Exact name construction | Cyan builds `(last underscore-segment of BT name OR whole BT name) + '_' + BT address without colons` in its connection handler and stores it in MMKV | Generic `AIMB-G1` prefix is not an exact identity method |
| Suffix-vs-name evidence | If the same BT address underlies both apps, a current Cyan-constructed candidate ends in K's full normalized suffix. The observed v0.6.8.6 OTHER peer did **not** match that suffix. | Simply adding exact-name equality with the same derivation cannot explain this observed nonmatch |
| Cyan alternative matcher | Cyan additionally does `endsWith(peerName, deviceAddressNoClear)`; this saved address is raw in examined setter. | Do not call the K normalized full-suffix-only matcher exact Cyan parity |
| 0x41 interpretation | Prompt short five-byte frame is not the earlier credential frame or recognized work-type-4 response. Its exact semantics remain unknown. | Do not equate a write callback/short response with successful readiness or fatal hardware failure |
| Physical media behavior | In tested JPG GET, exact new remote JPG disappears; remote count decrements, no K explicit delete; no-GET reconnect retains it | Never promise glasses-side-original preservation on that tested path |
| Ledger interpretation | Local file/receipt durable in scoped run; missing remote identity after consumed GET is not a hashing failure by itself | Remove invalid universal remote-catalog-match requirement from later gate design |
| Catalog/capture behavior | config type selects parser/endpoint; newly captured media may appear asynchronously | Keep path/extension verification and exact single-entry delta gates |
| Cleanup/provenance | Android callbacks and CI PASS do not prove device state. | Exact-build review and group absence remain separate evidence |

## Unresolved issues by blocker class
| ID | Severity | Remaining question | Existing evidence | Only defensible closure condition | Requires new physical run now? |
|---|---|---|---|---|---|
| B1 | BLOCKS present association | Is the *current* discovered Android P2P peer the exact AIMB-G1 selected over BLE? | 0.6.8.5/.6 each saw one peer, no exact suffix; .6 category OTHER; no connection initiated | Independently verified unique peer identity from trustworthy exact BLE-name/address derivation plus current observed peer and group membership if association attempted | NOT authorized; needs distinct safe physical proposal after static review |
| B2 | BLOCKS claiming original retention | Is any official supported non-consumptive JPG read path available? | Proven current GET consumes exact JPG; Cyan has mode-dependent catalog routes but no verified alternative with non-consumptive behavior | Verified documented or physically evidenced non-consumptive method; otherwise explicit product acceptance of consumption and local-first commit constraints | No; product requirement unresolved |
| B3 | BLOCKS production auto-sync | Does crash-safe exactly-once storage/receipt import work under partial transfers, process death and repeated sessions? | One persistent JPG+ledger, not all failures | Durable local transaction design plus targeted fault-injection proofs followed by bounded device trials | Not for next identity step |
| B4 | NONBLOCKING until a behavior depends on it | What does short 0x41 `0x89/0x8A` mean? | Valid short frame but outside recognized success parse | Exact firmware/spec/validated response-state correlation; or future logic must not infer status from it | No |
| B5 | NONBLOCKING until later G6B | How do MP4 and OPUS behave and are media timestamps/numbering preserved? | Categories inventoried, only JPG transfer proven | Isolated format-specific transfers, metadata and rename tests | No |
| B6 | Platform limitation | Will Android always deliver Wi-Fi peer broadcasts and cleanup callbacks? | API callbacks sometimes unreliable; timers/preflight tested in bounded scenarios | Cannot prove absolute guarantee; implement bounded fail-closed retries/verification after future approval | No |
| B7 | NONBLOCKING until production | Is uninterrupted automatic sync in the background sustainable and privacy-preserving? | No production background operation tested | Android lifecycle, permissions, power constraints, and on-device production tests | No |

## Red-team adversarial challenge to the next implementation
- **False fix #1:** add generic peer-name prefix and connect sole candidate. Violates exact target identification and creates unrelated-device risk.
- **False fix #2:** assume introducing Cyan-derived Wi-Fi name will make the v0.6.8.6 OTHER peer match. Logical contradiction under identical BLE address inputs (the generated name still has full suffix).
- **False fix #3:** send a reset before ENTER to force long credentials. Cyan only used reset for defined recovery, and unknown short frame gives no evidence of this need.
- **False fix #4:** switch transport/config branch or retry a JPG GET to preserve remote content. Unsupported and may consume irreversible originals.
- **False fix #5:** assume exit write callback means group removed. Group/readiness/exit each require independent evidence.
- **False fix #6:** declare G6A complete because the historical single JPG and receipt persisted. Restart-safe dedup and current connection remain unverified.
- **False fix #7:** replace historical source, release archives or hashes in place. Keep prior exact binary and physical reports immutable.

## Completion policy
- The historical assumptions have been *triaged and evidence-corrected*, not all experimentally resolved.
- Every nonblocking unknown is recorded; only critical blockers constrain what may be called an authorized production behavior.
- Until B1 identity and B2 preservation expectation are addressed, no connection build, bulk media run or G6B.
- No new APK, glasses command, remote write, or physical test is authorized by this document.

**Gate status: G6A OPEN / G6B BLOCKED.**
