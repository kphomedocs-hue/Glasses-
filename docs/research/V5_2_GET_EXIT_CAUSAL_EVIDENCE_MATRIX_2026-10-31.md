# V5.2-A — GET versus EXIT causal evidence matrix

**Date:** 2026-10-31
**Scope:** offline analysis of existing records only. No device contact, media GET, BLE command, app build/install, or physical test.
**Authority:** current `main`; V5.1's original-bytecode review and frozen physical reports are the evidence base. This document does not repeat V5.1 APK/DEX verification.

## Decision

The only demonstrated association is that one completed JPG GET followed by the normal EXIT/teardown/reconnect lifecycle was followed by disappearance of that exact identity from the remote catalog. The no-media-GET control retained its newly captured JPG after EXIT/teardown/reconnect. This rules against an unconditional “every EXIT deletes the newest photo” explanation, but does not isolate GET, the GET response/connection close, EXIT, teardown, or asynchronous firmware work.

“Missing from the catalog” is the measured outcome. Physical erasure of the media bytes has not been observed. **No transfer behavior is currently verified to preserve originals. C3 remains NO-GO; G6A OPEN; G6B BLOCKED.**

## Evidence ledger

| Evidence ID | Existing observation | Confidence / scope | It does not establish |
|---|---|---|---|
| E1 | v0.6.5: after capture, exactly one new JPG remained present after the no-media-GET flow, EXIT, group teardown, and fresh reconnect | High for this exact session and catalog observation; frozen physical report | GET-path safety; behavior of other media or firmware versions |
| E2 | v0.6.7: one selected JPG returned HTTP 200 (599,645 bytes); after the completed GET+EXIT lifecycle and fresh reconnect, the exact identity/count was absent; prior JPGs remained | High for this exact session and catalog observation; frozen physical report | The instant/operation that caused absence; deletion of storage bytes |
| E3 | V5.1 checked owner-supplied Cyan APK SHA-256 and all six embedded DEX byte-for-byte; 27/27 targeted static assertions passed | High for the bounded methods in the exact APK | Firmware HTTP-server implementation or complete app/library behavior |
| E4 | Cyan's inspected code invokes an ordinary media download call, advances a phone-side queue, locally processes the fetched file, and sends BLE EXIT `02 01 09` after the phone queue empties; inspected EXIT response callback is empty | High for the inspected DEX call paths | Remote firmware semantics, ACK timing, exact execution order across asynchronous callbacks |
| E5 | The named `removeFirst()` and `deleteFile(String)` operations are phone-local; the latter reaches `java.io.File.delete()` | High for the two scoped symbols | Absence of some other remote-side deletion mechanism |

Evidence sources: `releases/v0.6.5/builds/run-36893102299-attempt-1/reports/2026-10-01_G6A_SINGLE_CAPTURE_RETENTION_PHYSICAL_PASS.md`; `releases/v0.6.7/builds/run-36898159938-attempt-1/reports/2026-10-01_G6A_V0_6_7_POST_GET_REMOTE_CONSUMPTION_PHYSICAL_PROOF.md`; V5.1 forensic, red-team and audit reports listed at the end.

## Ranked causal model matrix

Ranks describe how much the existing evidence supports a *claim*, not numeric probabilities. The broad lifecycle association is well evidenced; the specific causes remain poorly distinguished.

| Rank | Hypothesis | Fit to existing evidence | Confidence in specific cause | What would distinguish it (research-only here) | Current decision |
|---|---|---|---|---|---|
| 1 (broad, observed) | **Lifecycle-associated catalog state change:** the completed GET→EXIT/teardown flow is associated with disappearance | Directly matches E1/E2 contrast | High that the association occurred in the recorded sessions; low about mechanism | Vendor server/firmware source, authoritative protocol specification, or a vendor statement tied to exact model and firmware | Treat every remote media read as potentially consumptive |
| 2 (mechanism family; not ranked against one another by observations) | **GET-completion commit:** successful full-body transfer causes firmware to mark/remove the item | E2 is consistent; E4 shows the app performs a full media download | Low; the post-GET observation also follows EXIT and teardown | Source/spec identifying GET semantics and the state transition; written confirmation for exact firmware | No assumption of safety or erasure timing |
| 2 (same unresolved tier) | **EXIT-commit of a pending transfer:** GET stages/marks an item and `02 01 09` commits it | E2 is consistent; E1 contradicts unconditional EXIT deletion, but not GET-conditioned EXIT | Low | Source/spec for EXIT transaction semantics and its completion/ACK behavior | Do not skip, repeat, or alter EXIT as a probe |
| 2 (same unresolved tier) | **HTTP completion/connection-close or Wi-Fi teardown commit:** server commits when stream closes or association ends | Both events occur in E2; neither is isolated | Very low | Vendor server code/spec identifying close/disconnect semantics | Do not use early-close, partial, or AP-path experiments |
| 2 (same unresolved tier) | **Asynchronous firmware queue/timer commit:** a deferred task removes/hides the item after transfer lifecycle | Compatible with the interval before fresh catalog read; E1 weighs against unconditional time-based cleanup | Very low | Firmware source/logs or vendor diagnostics describing queue/retention lifetime | No waiting-period inference or timing probe |
| 3 (outcome interpretation) | **Catalog suppression with bytes retained:** item is hidden/unlisted but media remains in flash | Fully compatible with the catalog-only observation | Unknown | Vendor-defined storage semantics or authorized, non-destructive vendor inspection/export | Report “absent from remote catalog,” never “erased” |
| 3 (outcome interpretation) | **Physical media deletion/erasure** | Compatible with disappearance, but no storage-level observation | Unknown | Authoritative firmware implementation or vendor-confirmed storage inspection | Do not claim erasure; preservation is not proven either |

### Causal boundary

The current controlled comparison changes more than one relevant event between sessions. E1 has no media-file GET. E2 has a successful media GET, normal EXIT, Wi-Fi teardown, a delay, and a fresh reconnect/catalog read. Therefore it can distinguish “no media GET flow” from “completed media GET lifecycle” only at the lifecycle level. It cannot assign causality to GET alone or EXIT alone. E1 specifically weakens only the unconditional-EXIT hypothesis.

The 27/27 V5.1 audit narrows interpretation of the client: local queue operations and a local phone-file deletion are not remote deletion commands. It adds no firmware-side causal evidence.

## Explicitly excluded unsafe interpretations

- Do not call the source erased; only catalog absence was measured.
- Do not call GET safe because the client invokes a common HTTP download API.
- Do not call EXIT the cause or harmless based on the no-GET control.
- Do not treat no direct `DELETE`, `HEAD`, or `Range` in one caller as proof that these requests are supported, safe, or available.
- Do not infer the JPG result applies identically to MP4/OPUS; still classify every source read as high risk until vendor evidence says otherwise.
- Do not copy Cyan's retry behavior; interrupted remote reads may leave local and remote state inconsistent.

## Safe next evidence

1. Request exact model/hardware and firmware-scoped written answers from the responsible manufacturer/authorized support channel (prepared, not sent; see V5.2-B).
2. Obtain manufacturer source/specification for the glasses-side file server and media inventory state transitions, including GET completion, connection close, BLE EXIT `02 01 09`, and restart/reconnect behavior.
3. If the vendor offers a preserving mode or tool, require its exact version, supported media types, retention guarantee, failure/retry behavior, and whether it is supported for the user's hardware/firmware. Keep the transfer gate closed until the claim is independently reviewable.

This report proposes no device experiment. Any later physical validation would require a separately scoped plan and explicit approval, and cannot use user/client originals.

## V5.1 source reports (completed; do not repeat)

- `docs/research/V5_1_CYAN_MEDIA_DOWNLOAD_EXIT_FORENSICS_2026-10-13.md`
- `docs/research/V5_1_CYAN_MEDIA_EXIT_RED_TEAM_2026-10-13.md`
- `docs/testing/results/2026-10-13_V5_1_CYAN_MEDIA_EXIT_STATIC_AUDIT.md`
