package com.parkarsite.g1capture.storage;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class NumberingPolicy {
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.US);
    private static final Pattern NUMBERED = Pattern.compile("^(\\d{4,})\\.[A-Za-z0-9]+$");

    private NumberingPolicy() {}

    public static String dailyFolder(long captureEpochMs, ZoneId zoneId) {
        return DATE.format(Instant.ofEpochMilli(captureEpochMs).atZone(zoneId));
    }

    public static int nextNumber(Collection<String> existingNames) {
        int max = 0;
        for (String name : existingNames) {
            Matcher m = NUMBERED.matcher(name);
            if (m.matches()) {
                try { max = Math.max(max, Integer.parseInt(m.group(1))); }
                catch (NumberFormatException ignored) { }
            }
        }
        return max + 1;
    }

    public static String fileName(int number, String extension) {
        String ext = extension.startsWith(".") ? extension.toLowerCase(Locale.ROOT)
                : "." + extension.toLowerCase(Locale.ROOT);
        return String.format(Locale.US, "%04d%s", number, ext);
    }
}
