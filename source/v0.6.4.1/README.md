# K G1 G6A No-Capture Catalog Stability v0.6.4.1

v0.6.4.1 is the corrected physical candidate after a logic review of v0.6.4.

## Corrections over v0.6.4

1. **Cross-channel parity is now mandatory for PASS.**
   - Each snapshot must have BLE image count == catalog JPG count.
   - This prevents a false PASS if both snapshots repeat the same internal mismatch, e.g. BLE=10 while catalog JPG=8.

2. **Transfer exit must be explicitly confirmed.**
   - The exit BLE write callback and a valid 0x41 exit response must both be observed.
   - The app no longer assumes that waiting three seconds proves the transfer exit completed.

3. **P2P group absence is verified before the quiet interval.**
   - After one removeGroup request, the app reads group state once.
   - If a group is still present, the run fails closed.
   - No removeGroup retry is performed.

4. **Cleanup callbacks are bounded.**
   - A missing cleanup callback cannot hang the diagnostic indefinitely.

5. **Exact operation totals are mandatory for PASS.**
   - media-count queries = 2;
   - P2P enters = 2;
   - transfer exits = 2;
   - catalog GETs = 2;
   - total HTTP GETs = 2;
   - media GETs = 0.

6. **Quiet interval evidence is monotonic.**
   - The app requests a minimum 30000 ms quiet interval and reports the actual elapsed monotonic duration.

7. **Report wording no longer claims to know the user's physical action.**
   - It states that the app issued no capture action and that the user was instructed not to take a photo.

## Physical sequence

1. Snapshot A: one inventory query, one P2P lifecycle, one catalog GET.
2. Confirm transfer exit.
3. Request P2P group removal and verify the group is absent.
4. Minimum 30-second foreground quiet interval.
5. Keep glasses untouched and take no photo.
6. Snapshot B: fresh BLE/P2P cycle, one inventory query, one catalog GET.
7. Confirm transfer exit and group absence.
8. Compare BLE inventory counts, catalog counts, and opaque SHA-256 identity sets.

## Hard boundary

- no capture command or capture instruction from the app;
- no media-file GET code path;
- no import/archive/ledger code;
- no raw remote filename/path persistence or logging;
- one catalog HTTP GET code path reused twice by state machine;
- no glasses file mutation/deletion;
- no hidden retries.

v0.6.4 must not be used for physical promotion. v0.6.4.1 supersedes it.
