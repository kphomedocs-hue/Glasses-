package com.parkarsite.g1capture.research;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Candidate protocol learned from a different HeyCyan hardware family.
 * NOT VERIFIED FOR AIMB-G1. RealAimbG1Transport must not use this until a
 * passive trace or read-only GATT discovery confirms compatibility.
 */
public final class HeyCyanCandidateProtocol {
    public static final String SERVICE_UUID = "de5bf728-d711-4e47-af26-65e3012a5dc7";
    public static final String WRITE_UUID   = "de5bf72a-d711-4e47-af26-65e3012a5dc7";
    public static final String NOTIFY_UUID  = "de5bf729-d711-4e47-af26-65e3012a5dc7";
    public static final int MAGIC = 0xBC;
    public static final int CMD_GLASSES_CONTROL = 0x41;
    public static final int MAX_BLE_CHUNK = 244;

    private HeyCyanCandidateProtocol() {}

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

    public static byte[] frame(int commandId, byte[] payload) {
        if (payload == null) payload = new byte[0];
        if (payload.length > 0xFFFF) throw new IllegalArgumentException("payload too large");
        int crc = payload.length == 0 ? 0xFFFF : crc16Modbus(payload);
        ByteArrayOutputStream out = new ByteArrayOutputStream(payload.length + 6);
        out.write(MAGIC);
        out.write(commandId & 0xFF);
        out.write(payload.length & 0xFF);
        out.write((payload.length >>> 8) & 0xFF);
        out.write(crc & 0xFF);
        out.write((crc >>> 8) & 0xFF);
        out.writeBytes(payload);
        return out.toByteArray();
    }

    public static List<byte[]> fragment(byte[] frame) {
        List<byte[]> chunks = new ArrayList<>();
        for (int p = 0; p < frame.length; p += MAX_BLE_CHUNK) {
            chunks.add(Arrays.copyOfRange(frame, p, Math.min(frame.length, p + MAX_BLE_CHUNK)));
        }
        return chunks;
    }

    public static byte[] candidateTransferPayload() {
        return new byte[]{0x02, 0x01, 0x04};
    }
}
