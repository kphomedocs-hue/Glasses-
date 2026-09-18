package com.parkarsite.g1.storage;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Pure local naming policy; no Android or device dependency. */
public final class DailyNumbering {
    private static final DateTimeFormatter DAY = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.ROOT);
    private static final Pattern NUMBERED = Pattern.compile("^(\\d{4,})\\.([A-Za-z0-9]+)$");
    private static final Set<String> ALLOWED = Set.of("jpg", "jpeg", "mp4", "opus");

    private DailyNumbering() {}

    public static String day(long captureEpochMs, ZoneId zoneId) {
        return DAY.format(Instant.ofEpochMilli(captureEpochMs).atZone(zoneId));
    }

    public static int nextNumber(Collection<String> names) {
        int max = 0;
        for (String name : names) {
            Matcher m = NUMBERED.matcher(name);
            if (!m.matches()) continue;
            String ext = m.group(2).toLowerCase(Locale.ROOT);
            if (!ALLOWED.contains(ext)) continue;
            try {
                max = Math.max(max, Integer.parseInt(m.group(1)));
            } catch (NumberFormatException ignored) {
            }
        }
        return max + 1;
    }

    public static String finalName(int number, String extension) {
        String normalized = normalizeExtension(extension);
        return String.format(Locale.ROOT, "%04d.%s", number, normalized);
    }

    public static String normalizeExtension(String extension) {
        if (extension == null) throw new IllegalArgumentException("Missing extension");
        String e = extension.toLowerCase(Locale.ROOT);
        if (e.startsWith(".")) e = e.substring(1);
        if (!ALLOWED.contains(e)) throw new IllegalArgumentException("Unsupported media extension: " + extension);
        return e;
    }
}
