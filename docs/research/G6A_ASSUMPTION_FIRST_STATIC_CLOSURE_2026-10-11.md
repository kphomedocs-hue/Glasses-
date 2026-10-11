# G6A assumption-first static closure — 2026-10-11
**Decision:** do not create APK or authorize glasses testing until the remaining physical/product blockers are explicitly classified. Cross-checked against exact K frozen source and earlier verified Cyan APK/DEX trace.

## Finding A — credential gating was an app-level choice, not an Android P2P WPS prerequisite (CLOSED, source scope)
- K `source/v0.6.7/.../MainActivity.java` lines 392–410: `maybeAdvanceAfterEnter` requires BOTH `enterWriteCallbackSucceeded` and `enterCredentialResponseReceived`, then insists `expectedP2pName != null && credentialPassword != null` before discovery.
- K `source/v0.4.0/.../MainActivity.java` lines 221–228, 258–278: `parseTransferCredentials` populates name and password. But `connectPeer` builds `WifiP2pConfig` from the exact selected peer device address and WPS-PBC; **does not pass the parsed password into Android's Wi-Fi Direct connection**.
- Exact Cyan DEX prior forensic source: `MyBluetoothReceiver.connectStatue` constructs a candidate Wi-Fi name from Bluetooth device name/address and assigns `UserConfig.setGlassDeviceWifiName`, independently of a new credential-bearing 0x41 response. Cyan starts peer discovery before ENTER in the traced path.
- Therefore treating absence of a credential-bearing 0x41 as PROOF that no Wi-Fi peer can be identified or group formed was unsupported. It explains *why K v0.6.8 waits/stops*, not why firmware returns short 0x41 or whether present hardware would accept association. Actual current association: NOT TESTED.

## Finding B — exact identity comparison still lacks a current matching peer (CLOSED comparison, OPEN hardware)
- Cyan stored-name formula (from exact DEX): if Bluetooth name contains underscore, take its final underscore-delimited segment; otherwise use whole name. Append `_` and Bluetooth address with colon separators removed. Retain in MMKV-backed `UserConfig.glassDeviceWifiName`.
- Cyan `PictureFragment.onPeersChanged` uses case-insensitive equality with that stored name; alternative `endsWith` with `deviceAddressNoClear`. The latter property was assigned the **raw** `BluetoothDevice.getAddress()` in examined connection code, not normalized at this comparison site.
- K v0.6.8.5 `DiagnosticSafetyRules.matchesPairedPeerName` (lines 16–23) requires the discovered name to end with the **12-hex-character Bluetooth address** from `BluetoothDevice.getAddress().replace(":", "")` (MainActivity lines 602–607). This **is not the full Cyan OR predicate** despite misleading report text.
- K v0.6.8.6 `DiagnosticSafetyRules.classifyPeerName` lines 9–20 classifies suffix FIRST, family prefix SECOND, other THIRD. Its only observed peer was OTHER (nonempty name, no family-prefix, no normalized BLE suffix).
- **Conditional deduction:** If the detected peer were advertising the exact Cyan constructed name from the same Bluetooth address K retrieved, it WOULD have matched K's 12-hex suffix classification regardless of its model prefix. Since it was OTHER, at least one part of that conjunction is false. This does **not** determine which part: peer could be unrelated, generated name could be stale/overwritten, Bluetooth address could differ, or Android peer list could be cached/incomplete. The actual peer was never associated, authenticated or transferred from.
- Adding a generic-prefix fallback or connecting to the sole observed peer is unjustified. Merely adding Cyan's constructed exact-name equality (if using exactly the same address) would not logically explain the observed suffix failure.

## Finding C — short 0x41 is a separate unresolved firmware/response question, not synonymous with failed P2P (CLOSED interpretation boundary, OPEN semantics)
- v0.6.8.1/.3/.4: valid five-byte payload `89 01 8A 00 00` after ENTER; no valid long credential frame in ordinary 10s + additional 10s observation.
- Historic G3B/G4A: long credential-bearing 0x41 and successful association, so packet shape changed across tested sessions; cause unknown.
- Cyan full-frame parser reads `dataType=1`, `glassWorkType=0x8A`; its examined album callback recognizes successful work type 4, not 0x8A. Do NOT label `0x8A` as busy, rejection, firmware reset, success, or a command for new recovery without authoritative evidence.
- `0x40` time sync success and inventory moving 11/0/1 → 0/0/0 did not restore long credentials. Hence missing time-sync alone and nonempty inventory alone are not adequate root causes.
- `discoverPeers` SUCCESS, ENTER write callback SUCCESS, actual matched advertising peer, Wi-Fi group formed, `0x73/08` ready, and successful catalog GET are **distinct facts**; do not collapse these stages.

## Finding D — remote download is consumptive on tested JPG path (CLOSED scoped behavior; OPEN alternatives/product policy)
- v0.6.5 shows newly captured JPG remains after reconnect with no media GET.
- v0.6.6 first detects inventory changed after one JPG GET.
- v0.6.7 reproduces a single exact new JPG GET followed by remote image count -1 and disappearance of that same exact opaque JPG identity after fresh reconnect, with all baseline JPG identities preserved. K sent no explicit delete command.
- Thus the blanket claim `downloading leaves original untouched` is FALSE for that path. **Not** proven: whether a different supported endpoint, transport or firmware setting permits non-consumptive reads. Do not claim one exists. The user requirement to keep originals on glasses needs a product-level decision; no code workaround has been validated.

## Finding E — durable receipt exists, exactly-once sync remains open (CLOSED scoped storage, OPEN crash safety)
- Persistent JPG and one ledger entry physically observed by G6A. Ledger survives restart. The previous `ledger identity absent from remote catalog` check was an invalid universal success criterion under consumptive GET.
- Design invariant warranted: locally committed asset and durable receipt must be treated as separate durable evidence; remote absence after GET cannot prove lost import. Atomicity under crash/power loss, duplicate suppression across reset/reconnect, and mixed media are **not tested**.
- No claim of final automatic sync can be made yet.

## Other static corrections
- G4B response for `configFileType=1`: `/files/media.config` is a safe-validated **line list**, not universal JSON. `configFileType=2` has a different JSON code branch.
- G5 visibility: early no-delta does not prove failed capture; a passive BLE +1 may arrive after many seconds. Require independent inventory and set delta instead of positional newest file.
- No custom code should treat successful Android callbacks as proof of complete cleanup. Preexisting group safety, one ENTER/EXIT, bounded independent timers, exact target membership and final group absence remain requirements.
- Exact source commit, CI-run, attempt, APK checksum, red-team review and operator report establish provenance; CI is not physical success.

## Red-team check / prohibited inferences
1. Cyan-derived candidate name is **not** a verified current advertiser.
2. Absence of Wi-Fi peer match is **not** device failure.
3. A formed group (if later seen) does **not** prove it contains the selected BLE target without membership/identity checks.
4. The nine-character default password in Cyan's BLE-connected path is **not evidence** Android WPS-PBC requires or uses it.
5. The Cyan suffix alternative uses raw saved device address; don't silently replace it with normalized hexadecimal in claims of exact parser parity.
6. Removing the credential gate is **not** itself authorization to connect, to bypass safety review or to run a new APK.
7. Photo GET consumption is not a voluntary K explicit delete and is not shown to apply to every transport or media type.

## Remaining evidence and authorization boundary
- **Blocking current hardware uncertainty:** Can an independently verified, unique exact BLE-derived candidate name be observed as an Android P2P peer with verified glasses membership after ENTER? No current physical evidence. Must not infer yes from observed OTHER peer.
- **Blocking product requirement:** Will the user accept consumptive-on-glasses JPG import, or must originals remain on device? The latter is not validated with current GET.
- **Blocking durability:** exactly-once local commit and receipt across partial download/crashes must be designed and later tested separately.
- **Not necessarily blocking discovery proof:** numeric semantics of unexplained short 0x41, provided a future safe research-only design does not depend on interpreting it.

No source/App/APK modifications and no new physical run authorized by this audit. **G6A OPEN; G6B BLOCKED.**
