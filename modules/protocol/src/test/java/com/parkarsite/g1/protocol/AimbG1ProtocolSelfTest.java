package com.parkarsite.g1.protocol;

import java.util.Arrays;

public final class AimbG1ProtocolSelfTest {
    public static void main(String[] args) {
        eq(0x5010, AimbG1Protocol.crc16Modbus(new byte[]{0x02, 0x01, 0x01}), "CRC 02 01 01");

        byte[] p2pExpected = new byte[]{
                (byte)0xBC, 0x41, 0x04, 0x00, (byte)0x93, 0x5C,
                0x02, 0x01, 0x04, 0x01
        };
        byte[] apExpected = new byte[]{
                (byte)0xBC, 0x41, 0x04, 0x00, (byte)0xD3, 0x5D,
                0x02, 0x01, 0x04, 0x02
        };

        truth(Arrays.equals(p2pExpected, AimbG1Protocol.mediaP2pFrame()), "P2P frame");
        truth(Arrays.equals(apExpected, AimbG1Protocol.mediaApFrame()), "AP frame");

        byte[] copy = AimbG1Protocol.mediaP2pPayloadForDiagnostics();
        copy[0] = 0;
        eq(0x02, AimbG1Protocol.mediaP2pPayloadForDiagnostics()[0] & 0xFF, "payload copy is defensive");

        System.out.println("PASS protocol vectors");
    }

    private static void eq(int expected, int actual, String what) {
        if (expected != actual) {
            throw new AssertionError(what + ": expected=0x" + Integer.toHexString(expected)
                    + " actual=0x" + Integer.toHexString(actual));
        }
    }

    private static void truth(boolean value, String what) {
        if (!value) throw new AssertionError(what);
    }
}
