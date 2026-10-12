# V4 Steps 07–08 — integrated host shared coordinator / red-team review

Date: 2026-10-12. **Host-JVM synthetic scope only; no real glasses, physical phone, radio, network GET, emulator or physical APK.** This report does not claim G6A closure.

## Source under test and exact CI
- Shared Java controller: `source/g1-shared-workflow/src/com/parkarsite/g1/workflow/SharedTransferCoordinator.java`.
- Adversarial host driver, **not part of Android main source sets**: `source/g1-shared-workflow/selftest/com/parkarsite/g1/workflow/HostSharedWorkflowSelfTest.java`.
- Existing *unchanged* durable implementation: `source/g6a-recovery-core/src/com/parkarsite/g6a/recovery/RecoveryImportStore.java`.
- Final tested host commit: `ba7f3e4619ed88249f531a082e6b38ad0388be7e`; Java 17 [workflow 38277694218](https://github.com/kphomedocs-hue/Glasses-/actions/runs/38277694218) **SUCCESS**, repository hygiene [38277694277](https://github.com/kphomedocs-hue/Glasses-/actions/runs/38277694277) **SUCCESS**.
- Exact job log: `V4-05-PURE-CONTRACT PASS`; `V4-OBSERVATION-FAIL-CLOSED PASS (57 assertions)`; `V4-07-SHARED-STATE-TO-STORE PASS (33 assertions)`; `V4-08-SHARED-ADVERSARIAL PASS (43 assertions)`. These are assertion counts in bounded host code, **not independent hardware cases**.

## New integration: what was ACTUALLY executed
1. New production-default-read-only shared coordinator consumes one synthetic baseline catalog and cross-checks the fake inventory JPG count.
2. A second catalog carries exactly one new opaque JPG and retains all baseline IDs and immutable descriptor metadata. Two newly appearing images, duplicate IDs, swapped media kinds and inconsistent inventory are rejected.
3. One explicitly **host-only, package-private** DI test grant enables the common coordinator's synthetic import path. The normal public two-argument constructor has no grant and rejects the same input with zero Source reads and no transaction ledger creation.
4. A host test adapter uses real `RecoveryImportStore.prepare` and `execute`; the journal's durable `FETCH_MAY_HAVE_STARTED` entry is checked **inside the fixture byte-source callback**, before its one synthetic write.
5. RecoveryImportStore validates seven-byte **marker-shaped**, not decodable, fake JPG bytes; fsyncs, atomically moves, commits SHA-256-backed opaque receipt. Host driver confirms intact receipt/file from a separately constructed store and `recover` returns COMPLETE, without another Source read.
6. Observer asks once for EXIT plus P2P cleanup, reports only after separately injected exit-write, valid post-exit inventory and group-absent flags. This is **simulated evidence**, not real Android/firmware identity or cleanup.
7. Repeated import attempt is refused; interrupted synthetic Source is classified `REMOTE_STATE_UNCERTAIN`, recovered locally without any repeat source callback.

No HTTP fixture server was created; existing seven standalone HTTP faults + 10 older integration tests were not rerun just to increase their count. This new test drives the **new shared** coordinator and the real local file-backed receipt.

## Red-team corrections before accepted host CI
- Baseline-only identity membership could have permitted a changed original's metadata; fixed by persisting the original immutable opaque descriptors and requiring exact equality at the second catalog.
- JPG count alone could have accepted a mislabeled `+1` after replacing an existing JPG's kind; fixed by requiring post-capture JPG inventory to equal **baseline JPG count + exactly one**.
- A cached single peer-eligibility boolean could survive a changed passive snapshot between catalog and transfer. Coordinator now refreshes the observer's candidate evidence separately before baseline, before delta and immediately before any fixture Source.
- Exceptions from the injected fixture-capability callback or null adapters must deny and initiate teardown, never leave a normal transfer permission.

## Step 08 adversarial cases newly exercised through this coordinator
- Unselected unrelated candidate; missing correlated IP; two candidate peers.
- Exact +2 new JPGs; duplicate opaque identities; changed media-kind of a retained identity.
- Candidate evidence revoked between baseline and second catalog and after exact delta but before byte-source invocation.
- Synthetic timeout before catalog; failing host-only test-grant callback.
- Source error after durable intent; manual/remote uncertain recovery and no blind replay.
- Normal public constructor cannot enable remote download even when all injected evidence looks perfect.

## Safety and integration limits
- The host fixture permit is an internal **test injection**, not physical authority. Do not compile host selftest files into any Android application's production main source set. Step 13 must independently audit application build provenance and explicitly reject leaked fixture implementations.
- Real physical identity, correlated source IP, group membership and `0x41`/0x73 meanings remain unresolved; host booleans do not prove those.
- This does **not** integrate live BLE, selected Android Wi-Fi Direct peer, bound `Network`, HTTP byte source or decoded JPG/MP4/OPUS into a production Android app.
- JPG validation remains marker-shape only; shared mixed-media numbering and real metadata remain unimplemented.
- The existing GET+EXIT physical path was previously associated with disappearance of the transferred JPG. **No non-consumptive real method has been proved.** The no-original-loss requirement blocks physical media transfer.
- Latest Android API36 **compile and emulator orchestration are separate gates**; this report does not claim either without a separately verified workflow.

## Result
**V4 Step 07 PASS — integrated synthetic host state-to-store path.**
**V4 Step 08 PASS — host adversarial integration checks.**
**V4 Step 09 only eligible for a separate zero-permission compile audit.**
**G6A OPEN; G6B BLOCKED. No hardware or real media GET authorized.**
