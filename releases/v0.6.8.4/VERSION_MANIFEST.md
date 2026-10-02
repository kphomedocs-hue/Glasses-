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

### run-36973860827-attempt-1
- exact bundle: `builds/run-36973860827-attempt-1/`
- build commit: `9773de8611d4762a127a3d09e9ff5521e0976451`
- APK SHA-256: `73c01e539cd0dd6183def175a0a53cca9ed8c99c98c83e8d4e8b655638bf984d`
- exact source ZIP SHA-256: `02dc3f8b07c713e535b43ac121fc39d642e6042a94649b3de16a89ca996be56a`
- CI/static status: verified
- physical status: not yet run

### run-36973886568-attempt-1
- exact bundle: `builds/run-36973886568-attempt-1/`
- build commit: `506b3f311be7e2dd8a6cd5ce8f7bf36615f95e25`
- APK SHA-256: `06184f7c634ccb940fa64989c035a03ed96cf43835d0c413aea3a43931fae7e1`
- exact source ZIP SHA-256: `16a139b81cc05caadd2f05b93d74bea4b4be46ddc1e17f4c84af7c006f112789`
- CI/static status: verified
- physical status: not yet run
