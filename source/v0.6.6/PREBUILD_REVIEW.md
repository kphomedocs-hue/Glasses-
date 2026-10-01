# v0.6.6 Pre-Build Design Review

The build must preserve the physically proven v0.6.5 capture path and add exactly one new experimental variable: **one media-file GET**.

Hard gates:
- no media GET before pre-GET reconnect proves the new JPG still exists;
- exact new JPG selected from the current catalog by matching its opaque in-memory identity;
- exactly one media GET;
- no redirects, retry, resume, Range or alternate endpoint;
- 32 MiB cap;
- HTTP 200;
- Content-Length consistency when available;
- JPEG SOI/EOI;
- temporary app-cache file only;
- temporary file deleted before leaving the GET stage;
- no persistent import;
- no persistent ledger;
- verified exit + group absence after GET;
- fresh post-GET inventory/catalog comparison;
- exact full-set equality required for PASS;
- exact successful totals 4 inventory / 4 enter / 4 exit / 4 catalog GET / 1 media GET / 5 HTTP GET.

The report must include exact build commit/run/attempt.
