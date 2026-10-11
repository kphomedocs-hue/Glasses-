#!/usr/bin/env python3
"""Offline-only crash/recovery reference model. NO HTTP, BLE or Android imports.

Not production Android code; a consumptive GET may irreversibly lose a remote
original before a complete local payload exists. This model fails closed.
"""
import argparse
import hashlib
import json
import os
from pathlib import Path
import re
import sqlite3

MAX_JPG = 33_554_432
ID = re.compile(r"^[0-9a-f]{64}$")
DAY = re.compile(r"^20[0-9]{2}-[0-9]{2}-[0-9]{2}$")
STATES = ("IN_FLIGHT", "COMMITTED", "NEEDS_REVIEW")


class Crash(Exception):
    pass


def fault(at, checkpoint):
    if checkpoint == at:
        if os.environ.get("G6A_KILL_ON_FAULT") == "1":
            os._exit(97)  # Real child process termination for CI fault injection.
        raise Crash(at)


def fsync_dir(path):
    fd = os.open(str(path), os.O_RDONLY | getattr(os, "O_DIRECTORY", 0))
    try:
        os.fsync(fd)
    finally:
        os.close(fd)


def validate_file(path, size=None, digest=None):
    try:
        if path.is_symlink() or not path.is_file():
            return False
        if path.stat().st_size < 5 or path.stat().st_size > MAX_JPG:
            return False
        with path.open("rb") as fh:
            if fh.read(3) != b"\xff\xd8\xff":
                return False
            fh.seek(-2, os.SEEK_END)
            if fh.read() != b"\xff\xd9":
                return False
        if size is not None and path.stat().st_size != size:
            return False
        if digest is not None:
            h = hashlib.sha256()
            with path.open("rb") as fh:
                for block in iter(lambda: fh.read(65536), b""):
                    h.update(block)
            if h.hexdigest() != digest:
                return False
        return True
    except (OSError, ValueError):
        return False


class RecoveryLab:
    def __init__(self, root):
        self.root = Path(root)
        self.root.mkdir(parents=True, exist_ok=True)
        self.conn = sqlite3.connect(str(self.root / "imports.sqlite"), timeout=20)
        self.conn.execute("PRAGMA journal_mode=DELETE")
        self.conn.execute("PRAGMA synchronous=FULL")
        self.conn.execute("""CREATE TABLE IF NOT EXISTS imports (
            id TEXT PRIMARY KEY, day TEXT NOT NULL, seq INTEGER NOT NULL,
            state TEXT NOT NULL, size INTEGER, digest TEXT,
            UNIQUE(day, seq))""")
        self.conn.commit()
        fsync_dir(self.root)
        self.armed = set()

    def close(self):
        self.conn.close()

    def get(self, opaque):
        row = self.conn.execute(
            "SELECT id,day,seq,state,size,digest FROM imports WHERE id=?", (opaque,)
        ).fetchone()
        return dict(zip(("id", "day", "seq", "state", "size", "digest"), row)) if row else None

    def reserve(self, opaque, day, crash_at=None):
        if not ID.fullmatch(opaque):
            raise ValueError("Identity must be lowercase opaque SHA-256")
        if not DAY.fullmatch(day):
            raise ValueError("Day format must be yyyy-mm-dd")
        from datetime import date
        date.fromisoformat(day)
        # Atomic reservation persists before ANY potentially consuming remote GET.
        self.conn.execute("BEGIN IMMEDIATE")
        try:
            row = self.get(opaque)
            if row is None:
                seq = self.conn.execute(
                    "SELECT COALESCE(MAX(seq),0)+1 FROM imports WHERE day=?", (day,)
                ).fetchone()[0]
                self.conn.execute("INSERT INTO imports VALUES(?,?,?,'IN_FLIGHT',NULL,NULL)",
                                  (opaque, day, seq))
            self.conn.commit()
        except BaseException:
            self.conn.rollback()
            raise
        if row is None:
            self.armed.add(opaque)
        fault(crash_at, "after_intent")
        return self.get(opaque)

    def paths(self, row):
        daydir = self.root / row["day"]
        stem = f'{row["seq"]:04d}.jpg'
        return daydir, daydir / stem, daydir / (stem + ".part"), daydir / ("." + stem + ".ready.json")

    def ingest_synthetic_chunks(self, opaque, chunks, crash_at=None):
        row = self.get(opaque)
        if row is None or row["state"] != "IN_FLIGHT" or opaque not in self.armed:
            raise PermissionError("Not a freshly armed single-use reservation")
        self.armed.remove(opaque)  # No second attempt even after handled error.
        daydir, final, part, marker = self.paths(row)
        daydir.mkdir(exist_ok=True)
        fsync_dir(self.root)
        if part.exists() or marker.exists() or final.exists():
            raise FileExistsError("Refuse overwrite of any existing transfer artifacts")
        size = 0
        sha = hashlib.sha256()
        with part.open("xb") as out:
            for block in chunks:
                if not isinstance(block, bytes):
                    raise TypeError("Expected byte chunks")
                size += len(block)
                if size > MAX_JPG:
                    raise ValueError("Media byte cap exceeded")
                out.write(block)
                sha.update(block)
                fault(crash_at, "during_stream")
            out.flush()
            os.fsync(out.fileno())
        fault(crash_at, "after_part_sync")
        if not validate_file(part, size, sha.hexdigest()):
            raise ValueError("Incomplete / invalid JPEG; no automatic retry")
        payload = {
            "schema": 1, "id": opaque,
            "relative": f'{row["day"]}/{row["seq"]:04d}.jpg',
            "size": size, "sha256": sha.hexdigest()
        }
        tmp = Path(str(marker) + ".tmp")
        with tmp.open("xb") as out:
            out.write((json.dumps(payload, sort_keys=True) + "\n").encode("ascii"))
            out.flush()
            os.fsync(out.fileno())
        os.replace(tmp, marker)
        fsync_dir(daydir)
        fault(crash_at, "after_marker")
        os.replace(part, final)
        fsync_dir(daydir)
        fault(crash_at, "after_rename")
        self._commit(opaque, size, sha.hexdigest())
        fault(crash_at, "after_commit")
        return self.get(opaque)

    def _commit(self, opaque, size, digest):
        self.conn.execute("BEGIN IMMEDIATE")
        try:
            old = self.get(opaque)
            if old is None or old["state"] not in ("IN_FLIGHT", "COMMITTED"):
                raise ValueError("Unexpected receipt state")
            self.conn.execute("UPDATE imports SET state='COMMITTED',size=?,digest=? WHERE id=?",
                              (size, digest, opaque))
            self.conn.commit()
        except BaseException:
            self.conn.rollback()
            raise

    def _review(self, opaque):
        with self.conn:
            self.conn.execute("UPDATE imports SET state='NEEDS_REVIEW' WHERE id=?", (opaque,))

    def recover(self):
        """No network operations; valid local ready artifacts only may be promoted."""
        self.armed.clear()
        result = []
        rows = self.conn.execute("SELECT id FROM imports ORDER BY day,seq").fetchall()
        for (opaque,) in rows:
            row = self.get(opaque)
            daydir, final, part, marker = self.paths(row)
            try:
                if not marker.is_file() or marker.is_symlink():
                    raise ValueError("Missing durable ready marker")
                payload = json.loads(marker.read_text(encoding="ascii"))
                if payload.get("schema") != 1 or payload.get("id") != opaque:
                    raise ValueError("Ready marker identity mismatch")
                if payload.get("relative") != f'{row["day"]}/{row["seq"]:04d}.jpg':
                    raise ValueError("Ready marker path mismatch")
                size, digest = payload.get("size"), payload.get("sha256")
                if not isinstance(size, int) or size < 5 or size > MAX_JPG:
                    raise ValueError("Bad size")
                if not isinstance(digest, str) or not ID.fullmatch(digest):
                    raise ValueError("Bad checksum")
                if row["state"] == "COMMITTED" and (row["size"], row["digest"]) != (size, digest):
                    raise ValueError("Database and marker disagree")
                if not final.exists():
                    if not validate_file(part, size, digest):
                        raise ValueError("No validated complete staged payload")
                    os.replace(part, final)
                    fsync_dir(daydir)
                if not validate_file(final, size, digest):
                    raise ValueError("Final payload integrity check failed")
                if row["state"] == "NEEDS_REVIEW":
                    raise ValueError("Manual-review decisions are never auto-reopened")
                if row["state"] == "IN_FLIGHT":
                    self._commit(opaque, size, digest)
                result.append((opaque, "COMMITTED"))
            except (OSError, ValueError, TypeError, KeyError, json.JSONDecodeError):
                self._review(opaque)
                result.append((opaque, "NEEDS_REVIEW"))
        return result


SYNTHETIC_JPEG = b"\xff\xd8\xff" + b"synthetic_offline_test_only" + b"\xff\xd9"


def child_main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--child", nargs=4, metavar=("ROOT", "ID", "DAY", "FAULT"))
    args = parser.parse_args()
    if args.child:
        root, opaque, day, checkpoint = args.child
        store = RecoveryLab(root)
        store.reserve(opaque, day, checkpoint)
        store.ingest_synthetic_chunks(opaque, [SYNTHETIC_JPEG[:8], SYNTHETIC_JPEG[8:]], checkpoint)


if __name__ == "__main__":
    child_main()
