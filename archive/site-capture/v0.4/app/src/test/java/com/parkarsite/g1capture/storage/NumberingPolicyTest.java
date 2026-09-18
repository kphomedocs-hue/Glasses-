package com.parkarsite.g1capture.storage;

import org.junit.Test;
import java.util.List;
import static org.junit.Assert.assertEquals;

public class NumberingPolicyTest {
    @Test public void mixedExtensionsShareOneCounter() {
        int next = NumberingPolicy.nextNumber(List.of("0001.jpg", "0002.mp4", "0003.m4a"));
        assertEquals(4, next);
        assertEquals("0004.jpg", NumberingPolicy.fileName(next, ".JPG"));
    }

    @Test public void unrelatedFilesDoNotBreakCounter() {
        int next = NumberingPolicy.nextNumber(List.of("note.txt", ".DS_Store", "0099.jpg"));
        assertEquals(100, next);
    }
}
