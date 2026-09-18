package com.parkarsite.g1.diagnostics;

public final class DiagnosticSanitizerSelfTest {
    public static void main(String[] args) {
        String raw = """
                Device: AIMB-G1
                Bluetooth address: AA:BB:CC:DD:EE:FF
                password=supersecret
                Serial Number: G1-123456
                Service: de5bf728-d711-4e47-af26-65e3012a5dc7
                """;
        String clean = DiagnosticSanitizer.sanitize(raw);

        absent(clean, "AA:BB:CC:DD:EE:FF", "MAC");
        absent(clean, "supersecret", "password");
        absent(clean, "G1-123456", "serial");
        present(clean, "<masked-mac>", "masked MAC marker");
        present(clean, "de5bf728-d711-4e47-af26-65e3012a5dc7", "protocol UUID retained");

        System.out.println("PASS diagnostics sanitizer");
    }

    private static void absent(String value, String needle, String what) {
        if (value.contains(needle)) throw new AssertionError(what + " was not redacted");
    }

    private static void present(String value, String needle, String what) {
        if (!value.contains(needle)) throw new AssertionError(what + " missing");
    }
}
