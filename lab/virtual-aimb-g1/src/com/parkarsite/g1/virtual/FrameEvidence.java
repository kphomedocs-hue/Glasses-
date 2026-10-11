package com.parkarsite.g1.virtual;

import com.parkarsite.g1.protocol.AimbG1Protocol;
import java.util.Arrays;

/** CRC and envelope only. No guessed short-0x41 semantics or credential decoding. */
public final class FrameEvidence {
    public enum Kind { INVALID, SHORT_41_UNKNOWN, OPAQUE_LONG_41, EVENT_73, OTHER_VALID }
    public record Parsed(Kind kind, int eventId) {
        public boolean valid() { return kind != Kind.INVALID; }
    }
    private FrameEvidence() {}
    public static Parsed parse(byte[] raw) {
        if (raw == null || raw.length < 6 || (raw[0] & 255) != 0xBC)
            return new Parsed(Kind.INVALID, -1);
        int length = (raw[2] & 255) | ((raw[3] & 255) << 8);
        if (length > 4096 || length != raw.length - 6)
            return new Parsed(Kind.INVALID, -1);
        byte[] payload = Arrays.copyOfRange(raw, 6, raw.length);
        int crc = (raw[4] & 255) | ((raw[5] & 255) << 8);
        if (crc != AimbG1Protocol.crc16Modbus(payload))
            return new Parsed(Kind.INVALID, -1);
        int cmd = raw[1] & 255;
        if (cmd == 0x41) return new Parsed(length < 8 ? Kind.SHORT_41_UNKNOWN : Kind.OPAQUE_LONG_41, -1);
        if (cmd == 0x73) return length == 0 ? new Parsed(Kind.INVALID, -1) : new Parsed(Kind.EVENT_73, payload[0] & 255);
        return new Parsed(Kind.OTHER_VALID, -1);
    }
    /** Synthetic fixture builder; never transmits a frame. */
    public static byte[] synthetic(int command, byte[] payload) {
        int crc = AimbG1Protocol.crc16Modbus(payload);
        byte[] frame = new byte[6 + payload.length];
        frame[0] = (byte)0xBC; frame[1] = (byte)command;
        frame[2] = (byte)payload.length; frame[3] = (byte)(payload.length >>> 8);
        frame[4] = (byte)crc; frame[5] = (byte)(crc >>> 8);
        System.arraycopy(payload, 0, frame, 6, payload.length);
        return frame;
    }
}
