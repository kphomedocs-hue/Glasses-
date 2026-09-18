# K Site Capture — AIMB-G1 v0.4 preflight status

## PASS — software-only checks
- Core Java self-test compiles and passes.
- Mixed photo/video/audio chronological numbering.
- Deterministic ordering when timestamps are identical.
- Duplicate entries in one remote listing import once.
- Restart deduplication.
- Crash-window recovery using hidden per-file provenance sidecars.
- Interrupted-transfer cleanup.
- Declared-size mismatch cleanup.
- 32 MiB streaming video test (no full-file byte[] buffering).
- JPG/JPEG, MP4, M4A, AAC, WAV and OPUS extension handling.
- Daily folder rollover at midnight.
- Android manifest/layout/value XML parses.
- Python capture and Cyan APK scanner scripts compile.
- APK scanner tested against a synthetic APK containing UUID, endpoint and private-IP indicators.
- RealAimbG1Transport still contains no guessed BLE UUID, Wi-Fi password, IP, port, endpoint or command.

## Historical candidate protocol status
The protocol research in this legacy baseline predates the later static Cyan analysis. It is preserved for provenance, not treated as the current truth.

## OPEN at the time of v0.4
- Android build/lint/install checks
- physical AIMB-G1 read-only profile confirmation
- first one-file transfer

The current project checkpoint supersedes these historical OPEN items.
