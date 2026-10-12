# V4 Steps 02–04 — original preservation and passive identity static requirements
2026-10-12. **STATIC EVIDENCE REVIEW AND TEST SPEC ONLY**. No hardware, APK, radio command or media GET was executed. H1/H2/H3 physical questions remain open.

## Step 02 — Precise original-retention evidence timeline (H1)
**Physically observed no-media-GET control, v0.6.5:** 10 JPG/1 OPUS baseline; one capture added one JPG; all ten baseline identities remained; post-capture inventory 11 JPG; after verified exit, group absence, 5 s and fresh BLE/P2P/catalog the new JPG was still present. Source: `releases/v0.6.5/builds/run-36893102299-attempt-1/reports/2026-10-01_G6A_SINGLE_CAPTURE_RETENTION_PHYSICAL_PASS.md`.

**Physically observed media-GET path, v0.6.7:** 11 baseline JPG/1 OPUS; exact one new capture gave 12 JPG and pre-GET reconnect retained the entire set; single HTTP 200 GET transferred 599645 bytes with basic JPEG markers; after GET + transfer EXIT the immediate inventory was 11; fresh reconnect yielded 11 JPG/1 OPUS with all 11 baseline identities, while the exact fetched JPG identity was missing. No explicit file-deletion command. Source: `releases/v0.6.7/builds/run-36898159938-attempt-1/reports/2026-10-01_G6A_V0_6_7_POST_GET_REMOTE_CONSUMPTION_PHYSICAL_PROOF.md`.

**Strong inference:** the tested complete media-download lifecycle is associated with disappearance of the fetched remote catalog identity. **NOT ESTABLISHED:** whether bytes were deleted at GET completion, a firmware download acknowledgement, EXIT, or a combination; whether storage bytes remain outside catalog; whether all firmware/media types share behavior. A missing catalog identity is not direct flash-level evidence of secure deletion, but fails the user-visible original-preservation acceptance criterion.

**Consequence:** do not call GET+EXIT an original-preserving transfer. No real photo GET permitted. Preserve test provenance and no-GET controls.

## Step 03 — Non-destructive transfer alternatives (H2)
| Candidate | Evidence classification | Current decision |
|---|---|---|
| Existing catalog read without media GET | PHYSICALLY DEMONSTRATED for catalog observation, not local media copy | May inform future read-only scope; does not achieve transfer |
| Existing observed JPG HTTP GET | PHYSICALLY DEMONSTRATED **loss-associated**, not preserving | **HARD BLOCK** for originals requirement |
| Alternative vendor-documented read-without-consume workflow | UNVERIFIED: no such primary reference established in this review | Research official vendor/protocol evidence before coding |
| Different endpoint, URL parameters, HEAD, HTTP Range, AP import, raw proprietary BLE commands | SPECULATIVE / unsupported by reviewed physical reports | Do not probe, implement or presume safe |
| Copy media directly from filesystem/storage without firmware acknowledgement | SPECULATIVE; neither authenticated filesystem access nor semantics established | Reject as current implementation route |
| Retrieve after another app has transferred | SPECULATIVE as a preservation solution; would still require independently proved remote retention | No promotion |

Evidence gap: full Cyan downloader/exit call-site trace and vendor firmware documentation require a distinct source-by-source verification before upgrading any alternative from speculative to supported. Do not characterize this table as completed firmware reverse engineering.

## Step 04 — Passive peer observer acceptance criteria (H3)
1. Record independently bounded *before ENTER*, *after ENTER write callback*, and *after EXIT* discovery observations, with monotonic-relative timestamps and explicit no-result buckets.
2. Mutually distinguish `EXACT_DERIVED`, `NORMALIZED_SUFFIX`, `RAW_COLON_SUFFIX`, `GENERIC_AIMB_G1`, `OTHER`, `MULTIPLE`, and `NONE`. Preserve the actual classifier's evidence semantics and never label normalized/raw-colon matching authenticated.
3. Include enumeration of **every** peer as sanitized classifications, including the previously unidentified OTHER; do not log peer names, BSSIDs, bonded MACs or credentials.
4. Require independently established selected real P2P device identity, selected-peer actual group membership, Android network binding, matching device-to-IP origin, and fresh correlated glasses IPv4. A synthetic boolean is not physical evidence.
5. Identify whether a pre-existing group affects discovery, require bounded cleanup and verify group absence. Exit callback alone is insufficient evidence of protocol-exit completion.
6. Treat short five-byte `0x41` as unknown/non-credential unless supported decoding proves otherwise. Uncorrelated `0x73` event must not authenticate a peer.
7. Zero transfers, zero catalog/media HTTP where the observer's scope is purely passive, zero capture, no delete/mutation, no automatic reconnect loop.
8. Fail closed on ambiguous, unrelated, late-only or uncorrelated peer evidence. No name-match-based connect.
9. Red-team scenarios: genuine target absent, one unrelated OTHER, exact+OTHER, raw-colon+generic, stale prior group, wrong bound network, wrong correlated IP, missing callback, stale after-EXIT events and teardown deadline.
10. Producing/building/running a diagnostic APK or accessing real glasses needs a separate, precisely scoped authorization; this document authorizes **neither**.

## Stop/go
Step 02: completed bounded comparison of cited physical reports, **not** proof of the deletion instant.
Step 03: evidence-ranked assessment completed; **NO verified non-consumptive transfer method**.
Step 04: passive observer acceptance contract specified; **NOT implemented or physically run**.
G6A OPEN. G6B BLOCKED. Production transfer authority must remain OFF.
