# VGL host-JVM synthetic HTTP, no glasses or Android networking

A deliberately isolated Java 17 loopback-only HTTP server and client test. The client refuses any host other than 127.0.0.1, sends GET without Range and with redirects disabled, and passes generated marker-only JPG fixture bytes into the ACTUAL RecoveryImportStore. Its state machine records FETCH_MAY_HAVE_STARTED before any HTTP request and forbids a second GET after ambiguity.

Run: bash lab/virtual-aimb-g1/host-http/run_selftest.sh

7 scripted HTTP cases: valid local commit and duplicate suppression; HTTP 500; redirect; truncated body; invalid JPEG body; consume-on-GET model; disallowed external host. A local receipt surviving a consumed remote item is NOT original preservation and cannot advance G6A.

**This is not an emulator test, not actual vendor HTTP, not Android production integration, and does not permit physical media GET.**


## V2B — integrated core/HTTP/receipt host cases (2026-10-11)

The host test runner now also compiles and executes `HostCoreIntegrationSelfTest.java` against the **unchanged shared Java core**, `PeerIdentityPolicy`, `VirtualLabPolicies.Catalog/Capture`, a localhost-only synthetic HTTP server and `RecoveryImportStore`. Ten integrated cases `CORE-HTTP-01` through `CORE-HTTP-10` cover peer trust, absent IP evidence, baseline retention, +2 ambiguity, error 500, no blind second GET and post-EXIT rejection. The original seven localhost HTTP cases still run, as do the 96 offline scenario checks in the parent workflow.

Proof: [GitHub workflow run 38137429577](https://github.com/kphomedocs-hue/Glasses-/actions/runs/38137429577) SUCCESS; [hygiene run 38137429591](https://github.com/kphomedocs-hue/Glasses-/actions/runs/38137429591) SUCCESS. Detailed findings: `docs/testing/results/2026-10-11_VGL_V2B_INTEGRATED_HOST_CORE_HTTP_RESULT.md`.

**Important boundary:** This is host JVM integration. Synthetic group membership and IP are injected flags. A distinct `fakeOnlyDownload` procedure is available only in the host test fixture and cannot grant real media GET authorization; `VirtualGlassesCore.mediaGetAuthorized()` stays false. No Android runtime/emulator or physical media result is claimed. G6A OPEN, G6B BLOCKED.
