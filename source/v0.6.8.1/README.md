# K G1 Credential Handshake Diagnostic v0.6.8.1

Purpose: isolate the reproducible v0.6.8 baseline P2P-enter credential-handshake failure without capture, Wi-Fi Direct discovery, catalog access, media GET, persistent import, or ledger activity.

Bounded sequence:
1. connect to the single bonded AIMB-G1-family target;
2. subscribe to the known Cyan notification characteristic;
3. issue exactly one `0x41 / 02 04` media-count query;
4. issue exactly one `0x41 / 02 01 04 01` P2P-enter write;
5. observe the normal 10-second credential window;
6. if no accepted credential frame appears, observe a further 10-second passive late window with no additional enter write;
7. classify notification activity structurally and by timing without logging payloads, SSID, password, Bluetooth address, or remote filenames;
8. issue exactly one allow-listed transfer-exit write and observe bounded exit confirmation.

This build is diagnostic only. It cannot perform Wi-Fi Direct discovery or any HTTP/media operation and cannot prove or close G6A.
