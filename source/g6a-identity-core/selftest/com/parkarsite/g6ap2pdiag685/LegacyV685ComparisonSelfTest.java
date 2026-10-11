package com.parkarsite.g6ap2pdiag685;

import com.parkarsite.g6a.identity.PeerIdentityPolicy;

/** Executes actual v0.6.8.5 DiagnosticSafetyRules without patching it. */
public final class LegacyV685ComparisonSelfTest {
    private static final String RAW = String.join(":", "AA","BB","CC","DD","EE","FF");
    private static final String HEX = "AABBCCDDEEFF";
    private static int assertions;

    private static void check(boolean v,String label) {
        assertions++;
        if(!v) throw new AssertionError(label);
    }

    public static void main(String[] args) {
        String rawPeer = "OTHER_" + RAW;
        String hexPeer = "OTHER_" + HEX;
        String exact = "TEST_" + HEX;
        check(!DiagnosticSafetyRules.matchesPairedPeerName(rawPeer,HEX),
              "v685 misses raw stored-BLE-address suffix");
        check(PeerIdentityPolicy.classify(rawPeer,"AIMB-G1_TEST",RAW)
              == PeerIdentityPolicy.Category.RAW_ADDRESS_SUFFIX_RESEARCH,
              "source-derived research identifies raw suffix bucket");
        check(DiagnosticSafetyRules.matchesPairedPeerName(hexPeer,HEX),
              "v685 matches full normalized BLE suffix");
        check(DiagnosticSafetyRules.matchesPairedPeerName(exact,HEX),
              "v685 matches full suffix despite missing model prefix");
        check(!DiagnosticSafetyRules.matchesPairedPeerName("AIMB-G1",HEX),
              "v685 never accepts generic model alone");
        check(!DiagnosticSafetyRules.matchesPairedPeerName(null,HEX),
              "v685 rejects null");
        check(!DiagnosticSafetyRules.matchesPairedPeerName(rawPeer,"INVALID"),
              "v685 rejects invalid paired suffix");
        check(!PeerIdentityPolicy.assess("AIMB-G1_TEST",RAW,java.util.List.of(rawPeer)).authenticated(),
              "new raw suffix is not authentication");
        System.out.println("PASS v0.6.8.5 exact legacy-source comparison: "+assertions+" assertions");
    }
}
