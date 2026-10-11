# G6A Step 4 — final source-level peer identity and safety review
Date: 2026-10-11 (IST). Research + offline reference only. No phone, glasses, APK, BLE commands, Wi-Fi Direct associations, or media access.

## Authority and source separation
**Cyan source-derived:** Exact prior DEX investigation (verified Cyan `base.apk` SHA-256 `1e700628d76fa4fa84047632e2ccce3673f1a01966fbf8c583985568f3aaaf64`) traced `MyBluetoothReceiver.connectStatue` assignment of an MMKV-backed Wi-Fi candidate name and `PictureFragment.onPeersChanged` stored-name equality plus raw saved-address suffix test.
**K source-derived:** Frozen `source/v0.6.7/`, `source/v0.6.8.5/`, `source/v0.6.8.6/`. K v0.6.8.5 implements **full normalized BLE suffix ONLY** despite its name-or-suffix report wording. K v0.6.8.6 categorizes full normalized BLE suffix FIRST, AIMB family prefix SECOND, all else OTHER.
**Physical result:** v0.6.8.6 on 2026-10-11 observed exactly one P2P peer with nonempty name categorized OTHER; no connection, P2P group, or 0x73; EXIT/group absence confirmed. No current raw peer identity was stored or published.
**Android API description:** https://developer.android.com/reference/android/net/wifi/p2p/WifiP2pDevice — `deviceName` is a user-friendly name and `deviceAddress` is Wi-Fi P2P device MAC; no asserted equality with BLE address. https://developer.android.com/develop/connectivity/wifi/wifip2p — `discoverPeers` success is only discovery initiation; proper permissions and enabled Location Mode matter. Neither is cryptographic identity proof.
**New source-only lab:** `source/g6a-identity-core/` pure Java 17, no transport API or Android dependencies.

## Static identity hypothesis matrix
| Hypothesis | v0.6.8.6 OTHER consistent? | What is actually demonstrated? | Safe next discriminator |
|---|---|---|---|
| H1 Observed peer is unrelated/cached | YES | Peer has a nonempty name but lacks normalized BLE-address suffix | Cannot label it glasses; more data would be needed |
| H2 Glasses had a Wi-Fi name derived using a different BLE/private identity than K read | YES | Cyan source uses `BluetoothDevice.getAddress` in its connected callback; K uses bonded `BluetoothDevice.getAddress`; live identity equality between sessions not proved | Compare source of addresses and acquisition timing, not a random peer connection |
| H3 Glasses did not enter usable P2P advertising after current ENTER | YES | ENTER characteristic callback succeeded; no currently verified readiness and no 0x73 in passive diagnostic | Separate readiness signal with current safe evidence if obtainable; do not decode unknown short 0x41 speculatively |
| H4 Firmware uses an unexpected Wi-Fi advertisement name | YES | Candidate derivation from 2026 Cyan app source not a readback of current advertiser | Need independent trustworthy current advertisement identity; avoid fabricated suffix rules |
| H5 K's model prefix matcher mistakenly missed Cyan's valid last-segment name | INSUFFICIENT as sole explanation | Cyan constructed name would still end in normalized BLE address if both use same address; K v0.6.8.6 checked this suffix | Synthetic comparator exercise with same-address and different-address cases |
| H6 The old long-credential prerequisite alone caused all current failures | NO | Explains v0.6.7's application halt, not v0.6.8.5/.6's unknown peer | Do not reintroduce credential requirement; leave current identity unresolved |

## Offline implementation and test result
`source/g6a-identity-core/src/com/parkarsite/g6a/identity/PeerIdentityPolicy.java` implements:
- strict bonded AIMB-G1 family name and 12-hex Bluetooth-address validation;
- Cyan-derived candidate name for documentary comparison (no raw value returned in public report);
- distinct categories: EXACT_DERIVED_CANDIDATE, NORMALIZED_SUFFIX_CANDIDATE, RAW_ADDRESS_SUFFIX_RESEARCH, GENERIC_FAMILY_ONLY, OTHER, EMPTY_NAME;
- per-snapshot *counts only*: INVALID_BONDED_INPUT / NO_CANDIDATE / SINGLE_UNVERIFIED_CANDIDATE / AMBIGUOUS_CANDIDATES;
- hard-coded `authenticated() == false` and `connectAuthorized() == false`, regardless of category.
- address/name derivation helpers restricted to package visibility after red-team privacy review; no public API returns a raw Bluetooth address or derived candidate string.
The Java 17 CI on source version before helper-visibility hardening executed **54 synthetic assertions PASS**: mismatched address, family-only match, duplicate names, exact vs suffix, no-connect, privacy and invalid input. Revised helper-visibility CI must be green independently before concluding final source PASS.

## Red-team adversarial checks
1. **Spoofed user-friendly name:** A name can be imitated by another peer; even an exact constructed candidate is not proof of ownership. The model never outputs an `AUTHENTICATED` state.
2. **P2P address vs BLE address:** Android `WifiP2pDevice.deviceAddress` is a P2P interface identifier and not silently equated with `BluetoothDevice.getAddress`; suffix only examines the *advertised name*.
3. **One-peer fallacy:** Exactly one OTHER peer is NO_CANDIDATE; never connect by nearest/only/generic model prefix.
4. **Duplicate ambiguity:** Two plausible matches lead to AMBIGUOUS, never first-match selection.
5. **Timing/caching:** Discovery success and one 16s snapshot do not prove glasses advertise or are absent; no stale-state conclusion from aggregate alone.
6. **Raw Cyan suffix branch:** The DEX trace observes raw `deviceAddressNoClear` argument to `endsWith`; unlike normalized K suffix. The lab categorizes it separately as research-only compatibility. No claim is made that its exact case handling or current device success has been physically verified.
7. **Bluetooth-name shape:** A Bluetooth name ending in underscore yields empty Cyan stem; the research lab deliberately rejects it rather than constructing a broad target name.
8. **Privacy:** Derived candidate itself embeds the Bluetooth address. It must never be logged, persisted, displayed, copied to reports, transmitted to GitHub, or used as identity proof. Pure `Report` returns only aggregate counts and enum.
9. **Consent/safety:** NO new Android app is made here. Previously frozen build source unchanged. Offline name-classification PASS is not authorization to write ENTER/EXIT or connect to a peer.
10. **Product separation:** Step 3 recovery lab may recover durable local bytes without GET. It does not solve hardware peer identity or glasses-side original retention.

## Hypothesis-discriminating research-only physical specification (NOT AUTHORIZED)
A next test would be justified **only** if it can observe an identity datum missing from v0.6.8.5/.6, not merely repeat the same 16s scan:
- Confirm bonded BLE target is unique, Bluetooth/GATT/notifications work; read-only preflight must prove no pre-existing Wi-Fi Direct group.
- Obtain or validate *independent trustworthy glasses identification* that can be compared with the current P2P advertisement; do not assume sole peer or model prefix is that identification.
- Use one bounded discovery/ENTER and at most one EXIT only in a separately approved exact binary; distinguish start success from actual scan result, and defer EXIT while ENTER write pending.
- Report sanitized aggregate classifications and precise event timing, not names, Bluetooth/P2P addresses, SSIDs, passwords, raw frames, device IPs or user media.
- **If still OTHER:** stop; do not connect an anonymous peer and do not repeat an identical test.
- Any future connection test requires another red-team review, selected P2P group membership verification and cleanup proof; no automatic media GET (known consumptive risk), reset, AP fallback, forced group removal or retry.
- No current physical candidate is promoted by this note.

## Formal decision
**Step 4 source-only identity comparison and defensive classification can close as a reviewed reference, subject to green CI after privacy hardening.** Current physical peer identity **OPEN**. Short 0x41 meaning **UNKNOWN**. Originals-on-glasses preservation **UNMET for previously tested JPG GET + EXIT interval**. No real image transfer warranted.

**G6A OPEN; G6B BLOCKED.**
