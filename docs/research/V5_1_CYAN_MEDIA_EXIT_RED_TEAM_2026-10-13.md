# V5.1 independent adversarial review — Cyan media download and EXIT causality
2026-10-13. Scope: challenge the exact original DEX trace and its interpretation. **No physical test, APK execution or HTTP media request.**

## Reviewed evidence
1. Verified Cyan owner-supplied original `base.apk` SHA-256 `1e700628d76fa4fa84047632e2ccce3673f1a01966fbf8c583985568f3aaaf64`, with six extracted DEX byte-for-byte matches.
2. Parsed method ids, class-data and code-unit PC offsets of `AlbumDepository`, `AlbumDepository$downloadGlassFile$2`, `PictureFragment.fileDownloadComplete`, `PictureFragment$fileDownloadComplete$1`, `PictureFragment.fileDownloadError`, `PictureFragment$fileDownloadError$2`, `WifiP2pManagerSingleton.cancelP2pConnection` from original classes4.dex.
3. Locally scoped `GFileUtilKt.deleteFile(String)` from original classes.dex.
4. Frozen v0.6.5 no-GET control and v0.6.7 GET+EXIT remote catalog change.
5. Prior historical static G4B/G5 protocol traces; not treated as independent direct source evidence where newly verified DEX is available.

## Red-team findings — assumption, rebuttal and safe classification

| ID | Tempting interpretation | Independent attack / actual evidence | Severity / decision |
|---|---|---|---|
| RT01 | “`BlockingDeque.removeFirst` deletes the remote original” | Receiver is **`AlbumDepository.fileQueue` on the PHONE**, at callback PC `0x0027`. Neither a firmware endpoint nor file-deletion API | **REJECT** interpretation |
| RT02 | “`GFileUtilKt.deleteFile` explains original loss” | Deletion helper constructs `java.io.File` and calls `File.delete()`; caller acts on downloaded local file path with size guard | **REJECT** remote-delete classification |
| RT03 | “No direct `DELETE` command means glasses originals must survive” | Firmware HTTP GET itself may change device catalogue; physically one JPG disappeared after completed GET+EXIT | **CRITICAL: FAIL** preservation inference |
| RT04 | “The HTTP GET itself definitively removes the file” | Post-GET measurement followed transfer EXIT. GET vs EXIT vs device-ack time was **not independently isolated** | **HIGH: UNKNOWN** trigger |
| RT05 | “EXIT alone is destructive” | No-GET v0.6.5 capture remained after multiple EXIT/teardowns and fresh reconnect | **HIGH: contradicted as universal claim**; conditional EXIT could still matter |
| RT06 | “`fileDownloadComplete` waits for BLE success before Wi-Fi teardown” | `fileDownloadComplete$lambda$21` is empty; UI cleanup scheduled before `glassesControl(02 01 09)`. Actual asynchronous ordering unmeasured | **HIGH: FALSE as a verified guarantee** |
| RT07 | “Cyan automatically retries, so K may safely retry interrupted GET” | `onError` calls `downloadGlassFile` again on a bounded error-count branch, potentially repeating consumptive requests; local receipt and source state may disagree after interruption | **CRITICAL: block automatic remote retry** |
| RT08 | “`onDownloadComplete` is sufficient proof of full local durability” | Downloader callback comes before executor-based local processing; no journal/fsync/durable receipt guarantee from this inspected Cyan method | **HIGH: cannot adopt Cyan completion as durable acceptance** |
| RT09 | “Phone queue empty means all media retained on glasses” | Empty queue is entirely client-side; no fresh remote-catalog re-read or original-retention check in inspected completion method | **CRITICAL: FAIL** |
| RT10 | “`WifiP2pManager.removeGroup` is a remote media delete” | Android `WifiP2pManager` tears down phone association, not an HTTP filesystem mutation. Firmware may react to disconnection; not proven | **REJECT direct delete**, retain firmware timing hypothesis |
| RT11 | “No `HEAD`/Range in `downloadGlassFile` proves such requests are safe/unsafe or impossible” | Only this exact caller inspected for header/method override; bundled library, firmware and other APIs are not comprehensively audited | **HIGH: no novel verb/endpoint experiments** |
| RT12 | “All future MP4/OPUS downloads consume the source” | Only JPG physical media GET was proven loss-associated; container types and other firmware branches not physically tested | **HIGH: UNKNOWN**, treat all remote reads high risk |
| RT13 | “Catalog absence proves erased storage bytes” | v0.6.7 shows exact catalogue identity and count missing; hidden/firmware queue state cannot be distinguished from physical erasure | **MEDIUM: say ‘unavailable in remote catalog’**, not forensic flash deletion |
| RT14 | “No-GET evidence can certify GET+EXIT original safety after another delay” | No-GET is a control against ordinary capture/idle/EXIT disappearance; it does NOT certify GET path | **CRITICAL: FAIL** |
| RT15 | “Source code trace authenticates current Wi-Fi peer” | Historic exact P2P identity and latest v0.6.8.6 current observed OTHER are distinct sessions; no selected-peer-to-BLE binding today | **HIGH: NO current G6A identity pass** |
| RT16 | “An exception path always sends EXIT” | The inspected `PictureFragment.fileDownloadError` / UI cleanup path lacks a direct EXIT call, and a retry can be initiated before/alongside UI cleanup. Other external paths remain unexamined | **HIGH: independent bounded cleanup needed** |
| RT17 | “An `AndroidNetworking.download` call establishes standard HTTP nonmutating semantics” | Network-library invocation only proves a client API usage; glasses firmware server is not analyzed. HTTP conventions cannot override observed behavior | **CRITICAL: no guarantee** |

## Alternate causal models (all still open)

Representing a *single observed remote-catalog loss-associated lifecycle*, **not** recommending another experiment:

- **H-G (GET-complete consumes):** the glasses local HTTP server updates its inventory after successful complete GET.
- **H-E (EXIT applies pending transaction):** device retains a pending transfer marker and commits the catalog change when BLE EXIT completes.
- **H-N (network-disconnect/ack commit):** device mutates catalog after completed HTTP response/connection closure or group removal.
- **H-T (time/passive firmware queue):** device asynchronously dequeues media after transfer with timing dependent on the lifecycle.
- **H-C (catalog hides, bytes persist):** original is hidden from the listed inventory, though media storage still contains it.

The v0.6.5 no-GET control is evidence against *unconditional* H-E or H-T. The v0.6.7 exact JPG delta supports a transfer-associated effect but does not distinguish H-G/H-E/H-N/H-T/H-C on internal firmware causality.

### Can any hypothesis authorize a new physical test?

**NO.** No proposed HTTP HEAD/Range, premature disconnect, skipped EXIT, AP fallback, partial GET or altered BLE acknowledgement should be called non-destructive. Interruption may make both the remote and local bytes unavailable. A static inference is insufficient to conduct those probes on the actual user's glasses.

## Red-team verdict

- Targeted **original Cyan DEX call-path extraction: PASS**; bounded method-level observations are independently reproducible from the previously verified `base.apk` by checksum and code-unit offset.
- **Local-vs-remote deletion identification: PASS** for the specific named `deleteFile` and `removeFirst` symbols. Not a whole-firmware deletion proof.
- **Causal event discrimination: OPEN**; no GET-only/EXIT-only isolation with authoritative firmware evidence.
- **Non-consumptive copy mechanism: NOT VERIFIED**.
- **Application release gates: G6A OPEN, G6B BLOCKED, C3 NO-GO**. No media transfer capability or automatic retry promotion.

Next: V5.2 can construct a **purely offline causal evidence matrix and manufacturer source enquiry**, without issuing any glass/HTTP commands. Manufacturer answer must be model/firmware scoped, and any later disposable-photo hardware test must require a separate user approval and clear rollback/stop rules. Do not infer permission from completion of this forensic review.

Public repository contains only analysis; private Cyan APK, DEX binaries, SSIDs, MACs, network credentials, user photos, raw remote catalogue identities and local download paths are NOT included.
