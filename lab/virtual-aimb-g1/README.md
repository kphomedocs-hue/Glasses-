# Virtual AIMB-G1 Lab — Lane A / first 24 scenarios

**State:** source-only Java 17 candidate, packs VGL-01 through VGL-03, no APK or physical tests. Run with:

    bash lab/virtual-aimb-g1/run_selftest.sh

Inputs are synthetic, e.g. the fabricated locally administered Bluetooth address in the tests. Prints fixture IDs and aggregate status only.

## What actually runs
- One event-driven candidate reusable policy core with injected BLE + peer ports; an independent deterministic virtual clock scopes each deadline by name.
- Historic CRC framing reused unchanged from modules/protocol; valid envelopes do not imply validated credential contents. In particular five-byte short 0x41 remains UNKNOWN.
- Source-derived exact/normalized/full-raw Bluetooth suffix identity candidate policy reused unchanged from source/g6a-identity-core. No candidate, even exact-name, is authenticated or connection-authorized.
- Synthetic callback order, timeout, CCCD/ENTER/EXIT behavior, 0x73 event-ID counter bounds, IP/group provenance gating and adversarial peer classification.
- Independent adverse timing and unsafe-state checks in VirtualRedTeamSelfTest.

## Limits that must remain visible
The new VirtualGlassesCore is a **candidate production-shared core**, not yet wired to the actual Android app. Thus 24/24 executed synthetic safety assertion sets does not mean 24 complete matrix scenarios are fully production-path validated. VGL-02-01 covers long-frame envelope shape only, not an evidence-backed credential field decoder; typed synthetic inventory/IP/group evidence is not actual BLE payload decoding or verified Android WifiP2p group membership. VGL-03-07/08 block trust of a spoofed name, but do not verify real peer-address provenance. Do not label these full production/physical PASS.

For preservation safety, LocalCatalogPort, OneShotMediaPort and DurableStorePort are intentionally unimplemented here. No peer connection or HTTP GET capability exists. In a later approved integration, use RecoveryImportStore for local commit and never treat a generic FAILED-to-DOWNLOADING ledger transition as permission for a second potentially consumptive GET.

The 72 planned scenarios in packs 4–12 remain unimplemented. G6A OPEN and G6B BLOCKED. The observed tested JPG GET/exit path removes the source JPG from the glasses, so automatic production import stays blocked pending a proven originals-preserving path.
