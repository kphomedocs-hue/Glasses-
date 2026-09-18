package com.parkarsite.g1.storage;

import java.time.Instant;
import java.time.ZoneId;
import java.util.List;

public final class DailyNumberingSelfTest {
    public static void main(String[] args) {
        eq(4, DailyNumbering.nextNumber(List.of(
                "0001.jpg", "0002.mp4", "0003.opus",
                ".0003.opus.source", "0004.mp4.part", "note.txt"
        )), "mixed media counter");

        eq("0004.jpg", DailyNumbering.finalName(4, ".JPG"), "jpg naming");
        eq("0005.opus", DailyNumbering.finalName(5, "OPUS"), "opus naming");

        ZoneId z = ZoneId.of("Asia/Kolkata");
        long before = Instant.parse("2026-09-18T18:29:59Z").toEpochMilli();
        long after  = Instant.parse("2026-09-18T18:30:01Z").toEpochMilli();
        eq("2026-09-18", DailyNumbering.day(before, z), "IST before midnight");
        eq("2026-09-19", DailyNumbering.day(after, z), "IST after midnight");

        expectFailure(() -> DailyNumbering.finalName(1, ".exe"), "unsafe extension rejected");

        System.out.println("PASS media-storage numbering");
    }

    private interface Throwing { void run(); }

    private static void expectFailure(Throwing x, String what) {
        try {
            x.run();
        } catch (IllegalArgumentException expected) {
            return;
        }
        throw new AssertionError(what);
    }

    private static void eq(Object expected, Object actual, String what) {
        if (!expected.equals(actual)) {
            throw new AssertionError(what + ": expected=" + expected + " actual=" + actual);
        }
    }
}
