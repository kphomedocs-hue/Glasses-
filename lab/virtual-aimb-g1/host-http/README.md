# VGL host-JVM synthetic HTTP, no glasses or Android networking

A deliberately isolated Java 17 loopback-only HTTP server and client test. The client refuses any host other than 127.0.0.1, sends GET without Range and with redirects disabled, and passes generated marker-only JPG fixture bytes into the ACTUAL RecoveryImportStore. Its state machine records FETCH_MAY_HAVE_STARTED before any HTTP request and forbids a second GET after ambiguity.

Run: bash lab/virtual-aimb-g1/host-http/run_selftest.sh

7 scripted HTTP cases: valid local commit and duplicate suppression; HTTP 500; redirect; truncated body; invalid JPEG body; consume-on-GET model; disallowed external host. A local receipt surviving a consumed remote item is NOT original preservation and cannot advance G6A.

**This is not an emulator test, not actual vendor HTTP, not Android production integration, and does not permit physical media GET.**
