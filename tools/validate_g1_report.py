#!/usr/bin/env python3
"""Validate and sanitize a K G1 Discovery report.

This tool is intentionally offline. It does not contact the glasses, Cyan,
Android, GitHub, or the network.

Exit codes:
  0 = sanitized report is safe and structurally valid
  2 = sensitive content was detected
  3 = expected GATT evidence is incomplete
"""
from __future__ import annotations
import argparse
import re
from pathlib import Path

EXPECTED = {
    "service": "de5bf728-d711-4e47-af26-65e3012a5dc7",
    "notify": "de5bf729-d711-4e47-af26-65e3012a5dc7",
    "write": "de5bf72a-d711-4e47-af26-65e3012a5dc7",
}

MAC = re.compile(r"(?i)\b(?:[0-9a-f]{2}[:-]){5}[0-9a-f]{2}\b")
SECRET_LINE = re.compile(
    r"(?i)\b(password|passphrase|psk|token|secret|serial(?:\s+number)?|bluetooth\s+address)\s*[:=]\s*([^\r\n]+)"
)
UUID = re.compile(r"(?i)\b[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}\b")
PROPERTY = re.compile(r"(?i)\b(READ|WRITE_NO_RESPONSE|WRITE|NOTIFY|INDICATE)\b")


def sanitize(text: str) -> tuple[str, list[str]]:
    findings: list[str] = []
    out = text

    if MAC.search(out):
        findings.append("bluetooth-mac")
        out = MAC.sub("<masked-mac>", out)

    def repl(match: re.Match[str]) -> str:
        findings.append(match.group(1).lower().replace(" ", "-"))
        return f"{match.group(1)}: <redacted>"

    out = SECRET_LINE.sub(repl, out)
    return out, sorted(set(findings))


def analyze(text: str) -> dict[str, object]:
    lower = text.lower()
    uuids = sorted(set(m.group(0).lower() for m in UUID.finditer(text)))
    props = sorted(set(m.group(1).upper() for m in PROPERTY.finditer(text)))
    expected = {k: (v in lower) for k, v in EXPECTED.items()}

    return {
        "expected": expected,
        "uuids": uuids,
        "properties": props,
        "has_read_only_marker": "read only" in lower or "read-only" in lower,
        "has_scan_or_connection_evidence": any(
            token in lower for token in ("scan", "connected", "connection", "gatt", "service")
        ),
    }


def main() -> int:
    ap = argparse.ArgumentParser()
    ap.add_argument("report", type=Path)
    ap.add_argument("--sanitized-out", type=Path)
    ap.add_argument(
        "--allow-incomplete-profile",
        action="store_true",
        help="Return success even if one or more expected Cyan-family UUIDs are absent.",
    )
    args = ap.parse_args()

    raw = args.report.read_text(encoding="utf-8")
    clean, sensitive = sanitize(raw)
    info = analyze(clean)

    if args.sanitized_out:
        args.sanitized_out.parent.mkdir(parents=True, exist_ok=True)
        args.sanitized_out.write_text(clean, encoding="utf-8")

    print("AIMB-G1 discovery report validation")
    print("----------------------------------")
    print("Sensitive fields detected:", ", ".join(sensitive) if sensitive else "none")
    print("UUID count:", len(info["uuids"]))
    print("Properties:", ", ".join(info["properties"]) if info["properties"] else "none")
    print("Expected service:", "FOUND" if info["expected"]["service"] else "NOT FOUND")
    print("Expected notify:", "FOUND" if info["expected"]["notify"] else "NOT FOUND")
    print("Expected write:", "FOUND" if info["expected"]["write"] else "NOT FOUND")

    if sensitive:
        print("RESULT: UNSAFE_FOR_PUBLIC_COMMIT until sanitized copy is reviewed")
        return 2

    if not info["has_scan_or_connection_evidence"]:
        print("RESULT: INVALID — no scan/connection/GATT evidence found")
        return 3

    missing = [name for name, found in info["expected"].items() if not found]
    if missing and not args.allow_incomplete_profile:
        print("RESULT: INCOMPLETE_PROFILE — missing:", ", ".join(missing))
        return 3

    print("RESULT: SAFE_AND_STRUCTURALLY_VALID")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
