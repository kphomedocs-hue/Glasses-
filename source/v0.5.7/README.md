# K G1 Disposable Photo Probe v0.5.7 — diagnostic-hardened G5

v0.5.7 keeps the v0.5.6 protocol/network boundary and improves measurement quality only.

Added:
- explicit stage/status transitions;
- monotonic elapsed timing from ARMED to passive visibility, active confirmation, P2P readiness, catalog parse and media validation;
- separate HTTP 200, Content-Length, size-cap, JPEG SOI and JPEG EOI validation reporting;
- automatic deletion of the app-private temporary JPG after validation;
- Phase-B terminal single-run lock: restart the app for another controlled run;
- foreground integrity guard: leaving the app during the controlled Phase-B test invalidates the baseline and stops;
- zero catalog polling: still exactly one post-visibility catalog GET.

Hard limits remain:
- two 0x41 / 02 04 inventory queries maximum total;
- two P2P enters maximum total;
- two transfer exits maximum total;
- two catalog GETs maximum total;
- one media GET maximum;
- Phase-B P2P blocked until exactly +1 image with unchanged video/recording counts;
- no redirect, retry, resume, Range, AP fallback, 02 03, arbitrary URL, raw catalog/name/path logging, or glasses mutation.

G6 remains blocked.
