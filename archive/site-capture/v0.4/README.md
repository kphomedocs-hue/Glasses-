# K Site Capture — AIMB-G1 v0.4 preflight

Single-purpose Android proof of concept: AIMB-G1 is the capture device; the phone is an automatic numbered archive.

Target archive:

```
AIMB-G1/
  2026-09-18/
    0001.jpg
    0002.mp4
    0003.opus
```

No AI, no project forms, no site-visit start/end flow.

## Safety boundary
`RealAimbG1Transport` intentionally has no protocol implementation yet. Candidate HeyCyan UUIDs/framing live only under the `research` package and MUST NOT be wired into the real transport until the AIMB-G1 is passively confirmed to use that profile.

## Run software-only tests

```bash
./tools/run_core_selftest.sh
python3 -m py_compile tools/correlate_tshark.py tools/scan_cyan_apk.py
```

## Historical note
This file is an immutable v0.4 preflight snapshot. The later Discovery v0.1 path replaced the need for Developer Options/ADB in the first physical check.
