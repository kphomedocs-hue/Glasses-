#!/usr/bin/env python3
"""Read-only static scanner for an extracted Cyan APK/XAPK/APKS bundle.
It never modifies or patches the app. It reports protocol indicators only.
"""
import argparse, re, zipfile
from pathlib import Path

NEEDLES = [
    b"de5bf728", b"de5bf72a", b"de5bf729",
    b"6e40fff0", b"6e400002", b"6e400003",
    b"media.config", b"WifiP2pManager", b"AIMB-G1",
    b"glasses_sdk", b"LargeDataHandler", b"OPUS", b"opus",
]
URL = re.compile(rb"https?://[^\x00\s\"'<>]{4,200}")
IP = re.compile(rb"(?<!\d)(?:10|127|169\.254|192\.168|172\.(?:1[6-9]|2\d|3[01]))(?:\.\d{1,3}){2}(?!\d)")

def scan_bytes(name, data):
    hits=[]
    low=data.lower()
    for n in NEEDLES:
        if n.lower() in low:
            hits.append((name, "needle", n.decode(errors="replace")))
    for m in URL.findall(data): hits.append((name, "url", m.decode(errors="replace")))
    for m in IP.findall(data): hits.append((name, "private-ip", m.decode(errors="replace")))
    return hits

def scan_path(path: Path):
    hits=[]
    if zipfile.is_zipfile(path):
        with zipfile.ZipFile(path) as z:
            for info in z.infolist():
                if info.file_size > 64*1024*1024: continue
                try: data=z.read(info)
                except Exception: continue
                if info.filename.endswith('.apk') and data[:2] == b'PK':
                    import io
                    try:
                        with zipfile.ZipFile(io.BytesIO(data)) as inner:
                            for ii in inner.infolist():
                                if ii.file_size > 64*1024*1024: continue
                                try: idata=inner.read(ii)
                                except Exception: continue
                                hits += scan_bytes(f"{info.filename}!{ii.filename}", idata)
                    except Exception: pass
                hits += scan_bytes(info.filename, data)
    else:
        hits += scan_bytes(path.name, path.read_bytes())
    return hits

def main():
    ap=argparse.ArgumentParser()
    ap.add_argument('package', type=Path)
    args=ap.parse_args()
    hits=scan_path(args.package)
    seen=set()
    for row in hits:
        if row in seen: continue
        seen.add(row)
        print("\t".join(row))
    print(f"\nUnique indicators: {len(seen)}")

if __name__=='__main__': main()
