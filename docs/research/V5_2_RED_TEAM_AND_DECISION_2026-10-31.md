# V5.2-C — Red-team review of causal claims and transfer proposals

**Date:** 2026-10-31
**Scope:** adversarial review of V5.2-A/B and prior evidence only. No physical test, media GET, BLE command, APK build/install, or external message.

## Red-team decision

The evidence supports only a narrow statement: in one recorded session, a completed JPG GET followed by the usual transfer EXIT/teardown was associated with the exact JPG becoming absent from a fresh remote catalog; a no-media-GET control retained its JPG after EXIT/teardown. The evidence does not establish whether catalog absence means deletion, nor which event caused it.

No manufacturer-supported original-preserving path is presently documented in the sources reviewed. Existing consumer manuals describe importing/saving media to the phone, but omit source-retention semantics. V5.2 therefore makes no transfer proposal executable and does not change any hardware gate.

## Adversarial challenges

| ID | Tempting claim or proposal | Challenge | Disposition |
|---|---|---|---|
| R1 | “GET deletes the photo.” | Measurement followed GET, EXIT, teardown, waiting and reconnect. No single event was isolated. | Reject specific causality; retain lifecycle association. |
| R2 | “EXIT deletes the photo.” | No-GET control retained its new JPG through EXIT. Conditional pending-transfer behavior remains possible. | Reject unconditional EXIT causality; do not vary EXIT. |
| R3 | “The photo was erased from flash.” | Only catalog count/identity was observed. A hidden/imported state is observationally equivalent here. | Say “absent from remote catalog”; storage state unknown. |
| R4 | “Cyan proves originals are retained because its Java code has no delete command.” | Firmware GET semantics are outside the inspected app methods. | Reject. No preservation proof. |
| R5 | “The normal Import feature is manufacturer-approved and therefore satisfies preservation.” | The manual documents Import/save-to-phone but does not promise source retention; project evidence is contrary to assuming safety. | Recognized as documented transfer, not as preserving transfer. Keep blocked. |
| R6 | “Use `HEAD` or `Range` to avoid consuming the file.” | Neither is documented as supported for this device; server may treat any request as a state transition; partial bytes may be useless and may trigger ambiguous state. | Not a proposal. No probing. |
| R7 | “Stop the body before completion, disconnect Wi-Fi, or use AP instead of P2P.” | Changes transport timing/teardown and could leave source and local state uncertain; no evidence of preservation. | Not a proposal. No probing. |
| R8 | “Skip EXIT, delay EXIT, or send EXIT twice to isolate it.” | These are new control sequences against unknown firmware state; could worsen recovery or consume/hide the item. | Not a proposal. No probing. |
| R9 | “Retry after a timeout; Cyan retries.” | Retry may repeat a consumptive GET after the first request changed remote state; client-side callback does not establish durable local bytes. | Automatic retry remains forbidden for remote media. |
| R10 | “The result applies only to JPG; videos/audio are safe.” | Only a JPG lifecycle was observed; other formats remain untested and unproven. | Treat all remote media reads as high risk. |
| R11 | “A seller/OEM product page is enough to establish the user's firmware behavior.” | Listing may represent a variant or seller; exact OEM authority and hardware/firmware mapping are unresolved. | Require exact model/firmware-scoped authoritative answer. |
| R12 | “A vendor’s one-line confirmation is sufficient.” | It may conflate app import, catalog visibility, and source-byte retention or omit error/retry behavior. | Apply the explicit acceptance bar in V5.2-B. |
| R13 | “Static/manual evidence can close G6A/G6B.” | The physical gates concern real peer/hardware behavior; paper research cannot establish a device-level pass. | Keep G6A OPEN and G6B BLOCKED. |
| R14 | “No public preserving method found means none exists.” | Public-document search is not exhaustive; vendor may have a private SDK, firmware option or service tool. | State “not found in reviewed sources,” not “does not exist.” |
| R15 | “Vendor can be contacted now by sending the prepared question.” | User asked for research and repository findings; no request to contact a third party was given. | Draft only; not sent. |
| R16 | “Since originals must be preserved, a speculative alternate path is worth trying.” | The user's explicit no-test limits and preservation requirement rule out experiments on the glasses. | No physical or transport experiment in V5.2. |
| R17 | “The downloaded local file means transfer was safely completed.” | Cyan callback precedes asynchronous local processing; V5.1 found no durable journal/receipt guarantee in those bounded methods. | Do not equate HTTP success with durable phone acceptance. |

## Proposal review

### Proposals accepted for offline work

- Maintain a claim/evidence ledger separating measurement, client-code fact, firmware hypothesis, and interpretation.
- Seek exact-model/firmware written support evidence and source/specification that describes file-state transitions.
- Require a vendor-defined validation method with disposable media and explicit preservation semantics before considering any later device test.

### Proposals rejected or deferred

- Any new HTTP verb/header/range request, partial transfer, cancellation, early disconnect, AP/P2P variation, retry, skipped/repeated EXIT, or timing manipulation on the user's glasses.
- Any APK or command sequence intended to exercise the transfer path.
- Any claim that current public manuals prove originals are retained.

## Exit criteria for the research-only V5.2 slice

- [x] Existing V5.1 work is referenced, not repeated.
- [x] GET-versus-EXIT evidence boundary and alternative models are explicit.
- [x] Publicly documented transfer methods are distinguished from documented preservation guarantees.
- [x] Manufacturer request is prepared but not sent.
- [x] Red-team challenges and stop decisions recorded.
- [x] Hardware gates remain unchanged.

Remaining blocker: authoritative exact-device preservation evidence is not yet available. The next safe action is obtaining that evidence through an authorized channel; until then, no real media GET or syncing release.
