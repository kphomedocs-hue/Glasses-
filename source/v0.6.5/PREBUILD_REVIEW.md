# v0.6.5 Pre-Build Design Review

Purpose: single-capture catalog retention with zero media downloads.

Required physical chain:
1. baseline inventory + catalog;
2. verified exit and P2P-group absence;
3. fresh BLE capture watch;
4. no P2P/HTTP before passive exact +1;
5. exactly one physical photo requested;
6. passive +1 only; +2/other delta fails closed;
7. one active inventory confirmation;
8. one post-capture catalog GET;
9. require exactly one new JPG and zero baseline loss;
10. verified exit and P2P-group absence;
11. fresh retention reconnect after bounded delay;
12. one inventory + one catalog GET;
13. require exact post-capture identity-set retention;
14. verified exit and cleanup.

Successful total boundary:
- 3 inventory writes;
- 3 P2P enters;
- 3 exits;
- 3 catalog GETs;
- 0 media-file GETs;
- 0 glasses file mutations.

The diagnostic must not hard-code the current 10-JPG count. It derives all expected post-capture/retention values from the runtime baseline.
