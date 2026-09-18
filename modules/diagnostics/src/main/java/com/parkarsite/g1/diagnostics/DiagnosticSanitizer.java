package com.parkarsite.g1.diagnostics;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Pure redaction helper for reports intended to leave the user's phone. */
public final class DiagnosticSanitizer {
    private static final Pattern MAC = Pattern.compile(
            "(?i)\\b(?:[0-9A-F]{2}[:-]){5}[0-9A-F]{2}\\b");
    private static final Pattern SECRET_FIELD = Pattern.compile(
            "(?i)\\b(password|passphrase|psk|token|secret|serial(?: number)?|bluetooth address)\\s*[:=]\\s*([^\\r\\n]+)");

    private DiagnosticSanitizer() {}

    public static String sanitize(String text) {
        if (text == null || text.isEmpty()) return text == null ? "" : text;
        String out = MAC.matcher(text).replaceAll("<masked-mac>");
        Matcher m = SECRET_FIELD.matcher(out);
        StringBuffer b = new StringBuffer();
        while (m.find()) {
            m.appendReplacement(b, Matcher.quoteReplacement(m.group(1) + ": <redacted>"));
        }
        m.appendTail(b);
        return b.toString();
    }
}
