package com.parkarsite.g1.protocol;

import java.util.Arrays;

/**
 * Pure AIMB-G1/Cyan-family protocol constants and testable framing.
 *
 * This module does not perform BLE I/O. It intentionally exposes only named,
 * evidence-backed command frames rather than an arbitrary command console.
 */
public final class AimbG1Protocol {
    public static final String SERVICE_UUID = "de5bf728-d711-4e47-af26-65e3012a5dc7";
    public static final String NOTIFY_UUID  = "de5bf729-d711-4e47-af26-65e3012a5dc7";
    public static final String WRITE_UUID   = "de5bf72a-d711-4e47-af26-65e3012a5dc7";

    private static final int MAGIC = 0xBC;
    private static final int GLASSES_CONTROL = 0x41;

    private static final byte[] MEDIA_P2P = new byte[]{0x02, 0x01, 0x04, 0x01};
    private static final byte[] MEDIA_AP  = new byte[]{0x02, 0x01, 0x04, 0x02};

    private AimbG1Protocol() {}

    public static int crc16Modbus(byte[] payload) {
        int crc = 0xFFFF;
        for (byte value : payload) {
            crc ^= value & 0xFF;
            for (int bit = 0; bit < 8; bit++) {
                crc = (crc & 1) != 0 ? ((crc >>> 1) ^ 0xA001) : (crc >>> 1);
            }
        }
        return crc & 0xFFFF;
    }

    public static byte[] mediaP2pFrame() {
        return frameNamedPayload(MEDIA_P2P);
    }

    public static byte[] mediaApFrame() {
        return frameNamedPayload(MEDIA_AP);
    }

    public static byte[] mediaP2pPayloadForDiagnostics() {
        return Arrays.copyOf(MEDIA_P2P, MEDIA_P2P.length);
    }

    public static byte[] mediaApPayloadForDiagnostics() {
        return Arrays.copyOf(MEDIA_AP, MEDIA_AP.length);
    }

    private static byte[] frameNamedPayload(byte[] payload) {
        if (payload.length > 0xFFFF) throw new IllegalArgumentException("Payload too large");
        int crc = crc16Modbus(payload);
        byte[] out = new byte[payload.length + 6];
        out[0] = (byte) MAGIC;
        out[1] = (byte) GLASSES_CONTROL;
        out[2] = (byte) (payload.length & 0xFF);
        out[3] = (byte) ((payload.length >>> 8) & 0xFF);
        out[4] = (byte) (crc & 0xFF);
        out[5] = (byte) ((crc >>> 8) & 0xFF);
        System.arraycopy(payload, 0, out, 6, payload.length);
        return out;
    }
}
