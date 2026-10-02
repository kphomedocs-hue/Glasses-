# v0.6.8.4 Version Manifest

- Version: v0.6.8.4
- Build role: bounded one-`0x40` initialization -> media-count -> P2P-enter credential diagnostic
- Package ID: `com.parkarsite.g6ainitdiag684`
- Gate role: **DIAGNOSTIC ONLY**; cannot close G6A or unblock G6B
- Physical status: **NO PHYSICAL CANDIDATE YET**

## Question

Does one already physically proven Cyan-equivalent `0x40` time-sync initialization write immediately before the existing media-count + P2P-enter sequence change the credential response behavior seen in v0.6.8.3?

## Frozen scope

Exactly:
1. subscribe to Cyan notify;
2. one dynamic Cyan-equivalent `0x40` time-sync write;
3. wait for its BLE write callback only;
4. one `0x41 / 02 04` media-count query;
5. one `0x41 / 02 01 04 01` P2P-enter write;
6. 10 s normal + at most 10 s passive late credential observation;
7. one `0x41 / 02 01 09` transfer-exit write.

No photo, Wi-Fi Direct, HTTP, catalog/media GET, persistent import, receipt, delete/mutation, or retry loop.
