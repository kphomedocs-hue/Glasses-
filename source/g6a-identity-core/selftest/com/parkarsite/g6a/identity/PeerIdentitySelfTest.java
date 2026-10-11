package com.parkarsite.g6a.identity;

import java.util.List;

public final class PeerIdentitySelfTest {
    private static int checks;
    private static final String RAW = String.join(":", "AA", "BB", "CC", "DD", "EE", "FF");
    private static final String HEX = "AABBCCDDEEFF";
    private static final String DIFFERENT = "AABBCCDDEE01";
    private static final String BONDED = "AIMB-G1_TEST";

    private PeerIdentitySelfTest(){}

    public static void main(String[] args) {
        check(PeerIdentityPolicy.validBondedTargetName("AIMB-G1"), "bare BLE family");
        check(PeerIdentityPolicy.validBondedTargetName(BONDED), "exact BLE family+suffix");
        check(PeerIdentityPolicy.validBondedTargetName("aimb-g1_small"), "family case insensitive");
        check(!PeerIdentityPolicy.validBondedTargetName("AIMB-G1_"), "empty BLE suffix invalid");
        check(!PeerIdentityPolicy.validBondedTargetName("AIMB-G11_TEST"), "different model invalid");
        check(!PeerIdentityPolicy.validBondedTargetName("X_AIMB-G1_TEST"), "embedded model invalid");
        check(!PeerIdentityPolicy.validBondedTargetName("AIMB-G1 TEST"), "nonfamily invalid");
        check(!PeerIdentityPolicy.validBondedTargetName(null), "null name invalid");

        eq(HEX, PeerIdentityPolicy.normalizeBluetoothAddress(RAW), "normalize paired BLE address");
        eq(HEX, PeerIdentityPolicy.normalizeBluetoothAddress("aabbccddeeff"), "plain hex normalized");
        check(PeerIdentityPolicy.normalizeBluetoothAddress("AABBCC") == null, "truncated rejected");
        check(PeerIdentityPolicy.normalizeBluetoothAddress("AABBCCDDEEGG") == null, "nonhex rejected");
        check(PeerIdentityPolicy.normalizeBluetoothAddress("AABBCCDDEEFF00") == null, "too long rejected");
        check(PeerIdentityPolicy.normalizeBluetoothAddress(RAW+" ") == null, "whitespace rejected");
        check(PeerIdentityPolicy.normalizeBluetoothAddress(null) == null, "missing address rejected");

        eq("TEST_"+HEX, PeerIdentityPolicy.deriveCandidate(BONDED, RAW), "Cyan suffix name");
        eq("LAST_"+HEX, PeerIdentityPolicy.deriveCandidate("AIMB-G1_MIDDLE_LAST", RAW), "last underscore segment");
        eq("AIMB-G1_"+HEX, PeerIdentityPolicy.deriveCandidate("AIMB-G1", RAW), "no underscore branch");
        eq("small_"+HEX, PeerIdentityPolicy.deriveCandidate("AIMB-G1_small", RAW), "preserve segment case");
        check(PeerIdentityPolicy.deriveCandidate("AIMB-G1_", RAW) == null, "blank segment");
        check(PeerIdentityPolicy.deriveCandidate("OTHER_TEST", RAW) == null, "not bonded family");
        check(PeerIdentityPolicy.deriveCandidate(BONDED, "wrong") == null, "invalid bonded address");

        eq(PeerIdentityPolicy.Category.EXACT_DERIVED_CANDIDATE,
             PeerIdentityPolicy.classify("test_"+HEX, BONDED, RAW), "exact ignores case");
        eq(PeerIdentityPolicy.Category.NORMALIZED_SUFFIX_CANDIDATE,
             PeerIdentityPolicy.classify("ALTERNATE_"+HEX, BONDED, RAW), "suffix-only not exact");
        eq(PeerIdentityPolicy.Category.RAW_ADDRESS_SUFFIX_RESEARCH,
             PeerIdentityPolicy.classify("UNRELATED_"+RAW, BONDED, RAW), "raw address reference distinct");
        eq(PeerIdentityPolicy.Category.GENERIC_FAMILY_ONLY,
             PeerIdentityPolicy.classify("AIMB-G1_NOT_THE_BONDED_SUFFIX", BONDED, RAW), "generic model insufficient");
        eq(PeerIdentityPolicy.Category.OTHER,
             PeerIdentityPolicy.classify("UNRELATED_"+DIFFERENT, BONDED, RAW), "different address peer is OTHER");
        eq(PeerIdentityPolicy.Category.OTHER,
             PeerIdentityPolicy.classify("OTHER", BONDED, RAW), "unknown peer is OTHER");
        eq(PeerIdentityPolicy.Category.EMPTY_NAME,
             PeerIdentityPolicy.classify("", BONDED, RAW), "empty peer name");
        eq(PeerIdentityPolicy.Category.EMPTY_NAME,
             PeerIdentityPolicy.classify(null, BONDED, RAW), "null peer name");
        eq(PeerIdentityPolicy.Category.OTHER,
             PeerIdentityPolicy.classify("TEST_"+HEX+" ", BONDED, RAW), "do not trim exact identity");
        eq(PeerIdentityPolicy.Category.EXACT_DERIVED_CANDIDATE,
             PeerIdentityPolicy.classify("TEST_"+HEX, BONDED, RAW), "suffix means exact if exact full construction");

        var empty=PeerIdentityPolicy.assess(BONDED, RAW, List.of());
        eq(PeerIdentityPolicy.Verdict.NO_CANDIDATE,empty.verdict(),"zero peers no candidate");
        eq(0,empty.total(),"empty total");
        var one=PeerIdentityPolicy.assess(BONDED, RAW, List.of("TEST_"+HEX));
        eq(PeerIdentityPolicy.Verdict.SINGLE_UNVERIFIED_CANDIDATE, one.verdict(),"single candidate NOT verified");
        eq(1,one.exact(),"one exact");
        check(!one.authenticated(),"a name is not authentication");
        check(!one.connectAuthorized(),"no API connect authorization");
        var other=PeerIdentityPolicy.assess(BONDED, RAW, List.of("UNRELATED_"+DIFFERENT));
        eq(PeerIdentityPolicy.Verdict.NO_CANDIDATE, other.verdict(),"OTHER cannot identify glasses");
        eq(1,other.other(),"other counted");
        var family=PeerIdentityPolicy.assess(BONDED, RAW, List.of("AIMB-G1_DIFF"));
        eq(PeerIdentityPolicy.Verdict.NO_CANDIDATE, family.verdict(),"generic prefix no candidate");
        eq(1,family.familyOnly(),"generic family category");
        var two=PeerIdentityPolicy.assess(BONDED, RAW, List.of("TEST_"+HEX, "DIFFERENT_"+HEX));
        eq(PeerIdentityPolicy.Verdict.AMBIGUOUS_CANDIDATES,two.verdict(),"two plausible peers fail closed");
        eq(1,two.exact(),"one exact among two");
        eq(1,two.normalizedSuffix(),"one suffix-only among two");
        var duplicate=PeerIdentityPolicy.assess(BONDED, RAW,List.of("TEST_"+HEX,"test_"+HEX));
        eq(PeerIdentityPolicy.Verdict.AMBIGUOUS_CANDIDATES,duplicate.verdict(),"duplicate-looking peer names ambiguous");
        var noisy=PeerIdentityPolicy.assess(BONDED, RAW,List.of("OTHER","AIMB-G1_OTHER","TEST_"+HEX,nullNameAlternative()));
        eq(PeerIdentityPolicy.Verdict.SINGLE_UNVERIFIED_CANDIDATE,noisy.verdict(),"single exact with irrelevant peers");
        eq(4,noisy.total(),"report count only");
        eq(1,noisy.missingName(),"missing count");
        var bad=PeerIdentityPolicy.assess(BONDED,"invalid",List.of("TEST_"+HEX));
        eq(PeerIdentityPolicy.Verdict.INVALID_BONDED_INPUT,bad.verdict(),"invalid BLE cannot classify");
        var nullPeers=PeerIdentityPolicy.assess(BONDED,RAW,null);
        eq(PeerIdentityPolicy.Verdict.INVALID_BONDED_INPUT,nullPeers.verdict(),"unknown peer list invalid");

        String representation=two.toString();
        check(!representation.contains(HEX), "report never exposes raw BLE normalized address");
        check(!representation.contains(BONDED), "report never exposes BLE name");
        check(!representation.contains(RAW), "report never exposes raw address");
        System.out.println("PASS G6A identity reference: "+checks+" assertions, all local and synthetic");
    }

    private static String nullNameAlternative(){return "";}
    private static void check(boolean condition,String label) {
        checks++;
        if(!condition) throw new AssertionError(label);
    }
    private static void eq(Object expected,Object actual,String label) {
        checks++;
        if(expected==null?actual!=null:!expected.equals(actual))
            throw new AssertionError(label+": expected "+expected+" got "+actual);
    }
}
