# K G1 G6A No-Capture Catalog Stability v0.6.4

v0.6.4 is a bounded read-only diagnostic prompted by two physical observations:
- v0.6.2 post-capture remote state changed from 9 JPG to 8 JPG across restart;
- v0.6.3 first post-arm inventory changed from 8 to 10, causing the exact +1 gate to fail closed.

This build asks a narrower question: **does the AIMB-G1 inventory/catalog change when no photo is taken at all?**

## Physical sequence

One button starts the complete test:
1. Snapshot A: one BLE inventory query, one P2P lifecycle, one catalog GET.
2. Exit transfer mode and remove the P2P group.
3. Fixed 30-second foreground quiet interval.
4. Keep the glasses untouched and take no photo.
5. Snapshot B: fresh BLE reconnect, one inventory query, one P2P lifecycle, one catalog GET.
6. Compare inventory counts plus opaque SHA-256 identity sets for all safe catalog entries and all JPG entries.
7. Exit transfer mode.

## Hard boundary

- exactly two inventory queries total;
- exactly two P2P enters total;
- exactly two catalog GETs total;
- exactly zero media-file GETs;
- no capture instruction;
- no media download code path;
- no filesystem import/archive/ledger writes;
- no raw remote filename/path logging or persistence;
- only truncated opaque hash tokens may be reported;
- no glasses mutation/deletion.

## Result classification

- **STABLE**: inventory counts and both catalog identity sets are unchanged.
- **COUNTS STABLE BUT IDENTITIES CHANGED**: counts match but one or more catalog identities differ.
- **INVENTORY/CATALOG MEMBERSHIP CHANGED**: counts or membership differ without capture.

G6A remains open and G6B remains blocked until this physical result is reviewed.
