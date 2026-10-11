#!/usr/bin/env python3
"""Pure synthetic offline fault-injection tests. No device/media/network access."""
import hashlib
import os
from pathlib import Path
import subprocess
import sys
import tempfile
import unittest

import g6a_consumptive_recovery_lab as lab

KEY_A = hashlib.sha256(b"synthetic_test_device_A/item_001").hexdigest()
KEY_B = hashlib.sha256(b"synthetic_test_device_A/item_002").hexdigest()
DAY = "2026-10-11"


class RecoveryLabTest(unittest.TestCase):
    def setUp(self):
        self.tmp = tempfile.TemporaryDirectory(prefix="g6a-consumptive-lab-")
        self.root = Path(self.tmp.name)

    def tearDown(self):
        self.tmp.cleanup()

    def store(self):
        return lab.RecoveryLab(self.root)

    def kill_child_at(self, checkpoint):
        cmd = [
            sys.executable, str(Path(lab.__file__).resolve()),
            "--child", str(self.root), KEY_A, DAY, checkpoint
        ]
        env = os.environ.copy()
        env["G6A_KILL_ON_FAULT"] = "1"
        result = subprocess.run(cmd, env=env, stdout=subprocess.PIPE,
                                stderr=subprocess.PIPE, timeout=15, check=False)
        self.assertEqual(result.returncode, 97, (checkpoint, result.stderr.decode()))
        self.assertNotIn(b"Traceback", result.stderr)

    def assert_review_without_auto_retry(self, store):
        self.assertEqual(store.get(KEY_A)["state"], "NEEDS_REVIEW")
        old = store.reserve(KEY_A, DAY)
        self.assertEqual(old["state"], "NEEDS_REVIEW")
        with self.assertRaises(PermissionError):
            store.ingest_synthetic_chunks(KEY_A, [lab.SYNTHETIC_JPEG])
        self.assertEqual(store.get(KEY_A)["state"], "NEEDS_REVIEW")

    def test_normal_commit_and_idempotent_restart(self):
        s = self.store()
        first = s.reserve(KEY_A, DAY)
        self.assertEqual(first["seq"], 1)
        finished = s.ingest_synthetic_chunks(KEY_A, [lab.SYNTHETIC_JPEG], expected_size=len(lab.SYNTHETIC_JPEG), transfer_complete=True)
        self.assertEqual(finished["state"], "COMMITTED")
        daydir, final, part, marker = s.paths(finished)
        self.assertTrue(final.is_file())
        self.assertFalse(part.exists())
        self.assertTrue(marker.is_file())
        self.assertEqual(finished["size"], len(lab.SYNTHETIC_JPEG))
        self.assertEqual(finished["digest"], hashlib.sha256(lab.SYNTHETIC_JPEG).hexdigest())
        s.close()
        s = self.store()
        self.assertEqual(s.recover()[0][1], "COMMITTED")
        duplicate = s.reserve(KEY_A, DAY)
        self.assertEqual(duplicate["seq"], 1)
        with self.assertRaises(PermissionError):
            s.ingest_synthetic_chunks(KEY_A, [lab.SYNTHETIC_JPEG], expected_size=len(lab.SYNTHETIC_JPEG), transfer_complete=True)
        self.assertEqual(final.read_bytes(), lab.SYNTHETIC_JPEG)
        s.close()

    def test_after_intent_crash_no_automatic_rerequest(self):
        self.kill_child_at("after_intent")
        s = self.store()
        self.assertEqual(s.get(KEY_A)["state"], "IN_FLIGHT")
        self.assertEqual(s.recover()[0][1], "NEEDS_REVIEW")
        self.assert_review_without_auto_retry(s)
        s.close()

    def test_during_stream_crash_no_automatic_rerequest(self):
        self.kill_child_at("during_stream")
        s = self.store()
        self.assertEqual(s.recover()[0][1], "NEEDS_REVIEW")
        self.assert_review_without_auto_retry(s)
        s.close()

    def test_full_unmarked_part_never_promoted(self):
        self.kill_child_at("after_part_sync")
        s = self.store()
        row = s.get(KEY_A)
        _, final, part, marker = s.paths(row)
        self.assertTrue(part.exists())
        self.assertFalse(marker.exists())
        self.assertFalse(final.exists())
        self.assertEqual(s.recover()[0][1], "NEEDS_REVIEW")
        self.assertTrue(part.exists(), "Keep partial file for forensic recovery")
        s.close()

    def test_crash_after_ready_marker_promotes_verified_part(self):
        self.kill_child_at("after_marker")
        s = self.store()
        self.assertEqual(s.recover()[0][1], "COMMITTED")
        row = s.get(KEY_A)
        _, final, part, marker = s.paths(row)
        self.assertTrue(final.exists())
        self.assertTrue(marker.exists())
        self.assertFalse(part.exists())
        self.assertEqual(final.read_bytes(), lab.SYNTHETIC_JPEG)
        s.close()

    def test_crash_after_rename_recovers_ledger(self):
        self.kill_child_at("after_rename")
        s = self.store()
        self.assertEqual(s.recover()[0][1], "COMMITTED")
        self.assertEqual(s.get(KEY_A)["size"], len(lab.SYNTHETIC_JPEG))
        s.close()

    def test_crash_after_db_commit_is_idempotent(self):
        self.kill_child_at("after_commit")
        s = self.store()
        self.assertEqual(s.get(KEY_A)["state"], "COMMITTED")
        self.assertEqual(s.recover()[0][1], "COMMITTED")
        self.assertEqual(s.recover()[0][1], "COMMITTED")
        s.close()

    def test_tampered_final_is_fail_closed(self):
        s = self.store()
        s.reserve(KEY_A, DAY)
        row = s.ingest_synthetic_chunks(KEY_A, [lab.SYNTHETIC_JPEG], expected_size=len(lab.SYNTHETIC_JPEG), transfer_complete=True)
        _, final, _, _ = s.paths(row)
        final.write_bytes(b"\xff\xd8\xffnotoriginal\xff\xd9")
        self.assertEqual(s.recover()[0][1], "NEEDS_REVIEW")
        self.assert_review_without_auto_retry(s)
        s.close()

    def test_tampered_ready_marker_cannot_promote(self):
        self.kill_child_at("after_marker")
        s = self.store()
        row = s.get(KEY_A)
        _, final, part, marker = s.paths(row)
        value = marker.read_text()
        marker.write_text(value.replace(KEY_A, KEY_B))
        self.assertEqual(s.recover()[0][1], "NEEDS_REVIEW")
        self.assertTrue(part.exists())
        self.assertFalse(final.exists())
        s.close()

    def test_invalid_jpeg_stays_in_flight_then_review(self):
        s = self.store()
        s.reserve(KEY_A, DAY)
        with self.assertRaises(ValueError):
            s.ingest_synthetic_chunks(KEY_A, [b"invalid_frame"], expected_size=len(b"invalid_frame"), transfer_complete=True)
        self.assertEqual(s.get(KEY_A)["state"], "IN_FLIGHT")
        self.assertEqual(s.recover()[0][1], "NEEDS_REVIEW")
        self.assert_review_without_auto_retry(s)
        s.close()

    def test_reservations_do_not_reuse_numbers_after_failed_transfer(self):
        s = self.store()
        s.reserve(KEY_A, DAY)
        with self.assertRaises(ValueError):
            s.ingest_synthetic_chunks(KEY_A, [b"invalid_frame"], expected_size=len(b"invalid_frame"), transfer_complete=True)
        s.recover()
        next_row = s.reserve(KEY_B, DAY)
        self.assertEqual(next_row["seq"], 2)
        s.ingest_synthetic_chunks(KEY_B, [lab.SYNTHETIC_JPEG], expected_size=len(lab.SYNTHETIC_JPEG), transfer_complete=True)
        self.assertEqual(s.get(KEY_A)["seq"], 1)
        self.assertEqual(s.get(KEY_B)["seq"], 2)
        s.close()

    def test_date_partition_and_bad_input(self):
        s = self.store()
        s.reserve(KEY_A, DAY)
        row = s.reserve(KEY_B, "2026-10-12")
        self.assertEqual(row["seq"], 1)
        for identity, day in [("not-opaque", DAY), (KEY_A, "../outside"), (KEY_A, "2026-02-31")]:
            with self.assertRaises(ValueError):
                s.reserve(identity, day)
        s.close()

    def test_hard_cap_enforced_with_no_ready_marker(self):
        s = self.store()
        s.reserve(KEY_A, DAY)
        original = lab.MAX_JPG
        try:
            lab.MAX_JPG = 10
            with self.assertRaises(ValueError):
                s.ingest_synthetic_chunks(KEY_A, [lab.SYNTHETIC_JPEG], expected_size=len(lab.SYNTHETIC_JPEG), transfer_complete=True)
        finally:
            lab.MAX_JPG = original
        self.assertEqual(s.recover()[0][1], "NEEDS_REVIEW")
        s.close()

    def test_refuses_missing_transport_completion_proof(self):
        s = self.store()
        s.reserve(KEY_A, DAY)
        with self.assertRaises(ValueError):
            s.ingest_synthetic_chunks(KEY_A, [lab.SYNTHETIC_JPEG],
                                      expected_size=len(lab.SYNTHETIC_JPEG))
        self.assertEqual(s.recover()[0][1], "NEEDS_REVIEW")
        s.close()

    def test_refuses_missing_expected_byte_count(self):
        s = self.store()
        s.reserve(KEY_A, DAY)
        with self.assertRaises(ValueError):
            s.ingest_synthetic_chunks(KEY_A, [lab.SYNTHETIC_JPEG],
                                      transfer_complete=True)
        self.assertEqual(s.recover()[0][1], "NEEDS_REVIEW")
        s.close()

    def test_refuses_jpeg_shaped_truncation_even_with_eoi(self):
        s = self.store()
        s.reserve(KEY_A, DAY)
        short_jpeg = b"\\xff\\xd8\\xff" + b"short" + b"\\xff\\xd9"
        with self.assertRaises(ValueError):
            s.ingest_synthetic_chunks(KEY_A, [short_jpeg],
                expected_size=len(short_jpeg)+10, transfer_complete=True)
        self.assertEqual(s.recover()[0][1], "NEEDS_REVIEW")
        s.close()

    def test_no_remote_string_or_secret_in_journal(self):
        s = self.store()
        s.reserve(KEY_A, DAY)
        s.ingest_synthetic_chunks(KEY_A, [lab.SYNTHETIC_JPEG], expected_size=len(lab.SYNTHETIC_JPEG), transfer_complete=True)
        all_text = " ".join(
            p.read_text(errors="ignore") for p in self.root.rglob("*")
            if p.is_file() and p.suffix == ".json"
        )
        self.assertIn(KEY_A, all_text)
        self.assertNotIn("synthetic_test_device_A", all_text)
        self.assertNotIn("item_001", all_text)
        s.close()


if __name__ == "__main__":
    unittest.main(verbosity=2)
