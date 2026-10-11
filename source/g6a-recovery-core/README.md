# G6A Offline Recovery Core

This is a standalone Java 17 research prototype. It is **not an Android app** and has no BLE, Wi-Fi Direct, HTTP, camera, or remote deletion code. Frozen physical releases remain unchanged.

The tested JPG transfer path may consume the remote image. Consequently, the prototype durably records `FETCH_MAY_HAVE_STARTED` **before** invoking a caller-supplied byte writer. If interrupted before verified local commit, startup classifies remote state `REMOTE_UNCERTAIN`, preserves the partial data, and forbids automatic retry.

After complete expected-byte-count and JPEG marker validation, it records a local SHA-256 and `VERIFIED` state. A crash before final receipt can then recover the already-validated bytes locally without another source read. A separate receipt and transaction record are verified against the completed file. Reservation assigns shared daily numeric `.jpg` filenames and persists only SHA-256 opaque media identifiers.

Current offline test command: `bash source/g6a-recovery-core/run_selftest.sh` (Java 17). The initial 61 assertions covered normal commit, six simulated failure barriers, restart, partial length, invalid JPEG, mid-stream source loss, tampering, receipt mismatch, numbering and privacy.

**Limits:** Synthetic exceptions are not physical process-kill or power-cut tests; directory fsync and atomic rename require compatible storage; SOI/EOI are not full JPEG decoding. No real glasses transfer, retention on glasses, Android integration, video/audio, or end-to-end exactly-once delivery is established. Requires independent code review and future authorization before connecting to remote GET.

G6A OPEN; G6B BLOCKED.
