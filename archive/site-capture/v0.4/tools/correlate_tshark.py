#!/usr/bin/env python3
"""Create compact timestamped views from a Bluetooth btsnoop and a network pcap.
Requires tshark in PATH. This does not decrypt application payloads; it only makes
correlation easier and highlights ATT writes / IP conversations around the trigger.
"""
from __future__ import annotations
import argparse, shutil, subprocess, sys
from pathlib import Path

def run(cmd):
    return subprocess.run(cmd, check=True, text=True, capture_output=True).stdout

def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("--bt", type=Path, required=True, help="Bluetooth btsnoop/pcap")
    ap.add_argument("--net", type=Path, required=True, help="PCAPdroid/network pcap")
    ap.add_argument("--out", type=Path, default=Path("correlated"))
    args = ap.parse_args()
    if not shutil.which("tshark"):
        sys.exit("tshark not found. Install Wireshark/tshark first.")
    args.out.mkdir(parents=True, exist_ok=True)

    bt_fields = [
        "frame.time_epoch", "frame.number", "btatt.opcode", "btatt.handle",
        "btatt.value", "bluetooth.src", "bluetooth.dst"
    ]
    cmd = ["tshark", "-r", str(args.bt), "-Y", "btatt", "-T", "fields"]
    for f in bt_fields: cmd += ["-e", f]
    cmd += ["-E", "separator=,", "-E", "quote=d"]
    (args.out / "bluetooth_att.csv").write_text(run(cmd), encoding="utf-8")

    net_fields = [
        "frame.time_epoch", "frame.number", "ip.src", "ip.dst", "tcp.srcport",
        "tcp.dstport", "udp.srcport", "udp.dstport", "http.request.method",
        "http.request.uri", "tcp.len"
    ]
    cmd = ["tshark", "-r", str(args.net), "-T", "fields"]
    for f in net_fields: cmd += ["-e", f]
    cmd += ["-E", "separator=,", "-E", "quote=d"]
    (args.out / "network_timeline.csv").write_text(run(cmd), encoding="utf-8")

    print("Wrote:")
    print(args.out / "bluetooth_att.csv")
    print(args.out / "network_timeline.csv")

if __name__ == "__main__":
    main()
