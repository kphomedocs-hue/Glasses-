# K G1 Disposable Photo Probe v0.5.6 — visibility-gated G5

Physical v0.5.5 proved one new photo becomes visible asynchronously to BLE inventory. v0.5.6 combines that evidence with the bounded v0.5.2 single-file path.

Phase A baselines inventory + safe media.config, then exits transfer.

Phase B reconnects BLE and ARMS a 60-second visibility window before the user captures exactly one photo. P2P is blocked until exactly +1 image with unchanged video/recording counts is confirmed by the single Phase-B 0x41 / 02 04 query. Only then may the app enter P2P, fetch media.config once, require exactly one new strict-safe relative .jpg by set difference, and perform at most one media GET.

Hard limits: two inventory queries total, two P2P enters total, two exits total, two catalog GETs total, one media GET maximum; no redirects, retries, Range/resume, AP fallback, 02 03, raw catalog/name/path logging, glasses mutation, or multi-file sync.
