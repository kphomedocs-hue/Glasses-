# G6A v0.6.8.6 physical interpretation — 2026-10-11
## Evidence
- Exact v0.6.8.6 run: `38118060511`, attempt 1, commit `90c2acb4f8736acf6ff024bfa3ba660ead09dad9`; complete report: `releases/v0.6.8.6/builds/run-38118060511-attempt-1/reports/2026-10-11_G6A_V0_6_8_6_PHYSICAL_INCONCLUSIVE.md`.
- Exact earlier v0.6.8.5 run: `38116572973`, attempt 1; full report under its immutable build folder.
- Historical G4A/G4A2 P2P association PASSED with a credential-derived exact Wi-Fi Direct peer name (20-byte SSID, 9-byte password, values withheld).

## Compared evidence
| Check | v0.6.8.5 | v0.6.8.6 |
|---|---|---|
| Exactly one bonded glasses-family target | yes | yes |
| Pre-existing Android P2P group | absent | absent |
| BLE subscription / discoverPeers start / ENTER callback | success | success |
| Peer count at last snapshot | 1 | 1 |
| Full paired-BLE-address suffix matched | no | no |
| Family-prefix matched | not observed by v0.6.8.5 | no |
| Nonempty OTHER peer name | not classified | yes, one |
| CRC-valid 0x73 during pending ENTER callback | one, event ID discarded | none |
| Post-ENTER 0x73 event | none | none |
| Android WifiP2pManager.connect | never invoked | not implemented |
| EXIT callback and final absent-group check | success | success |

## Interpretation and source defect
The Android-visible peer name was nonempty but neither a generic AIMB-G1-family prefix nor a full 12-character bonded-BLE hex suffix. Its actual identity was deliberately withheld and is not established. Never infer that a sole visible peer is the glasses or automatically connect to it.

The 0x73 event seen only in v0.6.8.5 does not reproduce in v0.6.8.6 and should not be used as proof of P2P readiness.

The v0.6.8.6 final stop message is inherited from the association probe: `Cyan-matching P2P peer not found within 16 seconds.` This is inaccurate for a diagnostic which *never tries to match/connect*. A future version should say `Passive peer observation completed at 16-second deadline` and reserve peer identity statements for the actual category summary. This report wording defect does not affect safety; do not patch and rerun unchanged just to alter text.

## Next research boundary
A third nearly identical on-device scan is not justified by the available evidence. Prioritize **static, no-glasses** research on trustworthy exact Wi-Fi Direct identity: Cyan's stored Wi-Fi name provenance and whether an already proven and safe BLE response supplies it without credential-bearing 0x41. Since Cyan's private storage is not shared with K, do not assume recoverability. Alternatively consider user-assisted validation using an independently verified official-app display of P2P name, without copying passwords or exposing identifiers in reports. Do not add a generic-prefix/only-peer auto-connection fallback; no resets, retries, media transfer or group manipulation until separately justified.

## Decision
One authorized v0.6.8.6 physical run CONSUMED; result INCONCLUSIVE, CLEANUP VERIFIED. No repeat of v0.6.8.5 or v0.6.8.6 unchanged. G6A OPEN; G6B BLOCKED.
