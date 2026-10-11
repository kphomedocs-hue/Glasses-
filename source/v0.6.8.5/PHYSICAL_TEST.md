# Physical diagnostic procedure — NOT YET AUTHORIZED

Do not execute these steps unless a verified CI build and a separate manual review explicitly authorize the exact artifact. No APK has been authorized by the 2026-10-11 source corrections.

# One physical diagnostic run

1. Install only the CI-produced v0.6.8.5 APK whose report header matches its
   immutable GitHub build directory.
2. Force-stop Cyan Glasses. Keep Bluetooth and Wi-Fi enabled; do not take a
   photo or use any other glasses app during the run.
3. Open the diagnostic, grant its Bluetooth and Nearby Wi-Fi permissions, and
   tap the run button once.
4. Wait for its terminal report. Do not retry after any error.
5. Copy the whole sanitized report and provide it for classification.

Expected terminal classifications:

- P2P group formed and `0x73/08` observed: Cyan-style readiness confirmed;
  the credential gate is wrong for this path.
- P2P group formed but no `0x73/08`: P2P works; BLE readiness remains open.
- No Cyan-matching peer/group without an Android error: inconclusive.
- P2P/permission/API error: diagnose that platform error before a glasses-side
  conclusion.

The run performs no HTTP, media, capture, import, reset, retry, or deletion.

If terminal report says `CLEANUP RESULT: FAILED OR INDETERMINATE`, do not rerun. Inspect Android Wi-Fi Direct state manually without deleting an unrelated group. Return the full report for analysis. Android cannot guarantee cleanup if the OS forcibly kills the process.
