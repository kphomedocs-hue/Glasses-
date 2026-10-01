# v0.6.7 Pre-Build Design Review

The only experimental logic change from v0.6.6 is how the first valid post-GET exit inventory is handled.

Required invariants:
- baseline exit mismatch remains fatal;
- post-capture exit mismatch remains fatal;
- pre-GET reconnect must be exact before GET;
- media GET remains exactly one;
- GET remains exact-new-JPG only;
- JPEG/size/temp cleanup unchanged;
- only stage PRE_GET_RETENTION + mediaValidated + mediaGetCount==1 may accept changed post-exit counts;
- exact changed counts must be logged;
- valid post-exit inventory is still required;
- P2P group absence is still required;
- fresh post-GET BLE inventory and catalog read are still required;
- final classification comes from the fresh post-GET state, not the immediate exit event;
- no persistent import/ledger;
- exact operation totals remain 4 inventory / 4 enter / 4 exit / 4 catalog GET / 1 media GET / 5 HTTP GET.
