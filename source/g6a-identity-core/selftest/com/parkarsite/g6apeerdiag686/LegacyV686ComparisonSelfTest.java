package com.parkarsite.g6apeerdiag686;

import com.parkarsite.g6a.identity.PeerIdentityPolicy;
import java.util.List;

/** Executes the actual immutable v0.6.8.6 DiagnosticSafetyRules, not a rewrite. */
public final class LegacyV686ComparisonSelfTest {
    private static final String RAW = String.join(":", "AA","BB","CC","DD","EE","FF");
    private static final String HEX = "AABBCCDDEEFF";
    private static final String BOND = "AIMB-G1_TEST";
    private static int count;

    private static void ok(boolean yes, String label) {
        count++;
        if (!yes) throw new AssertionError(label);
    }
    private static void match(int n, String name, String label) {
        ok(DiagnosticSafetyRules.classifyPeerName(name, HEX) == n, label);
    }
    public static void main(String[] args) {
        String exact = "TEST_" + HEX;
        String unrelatedRaw = "DIFFERENT_" + RAW;
        String genericRaw = "AIMB-G1_" + RAW;
        String normalizedOnly = "DIFFERENT_" + HEX;
        String unrelated = "DIFFERENT_AABBCCDDEE00";
        // This is the exact 0.6.8.6 source method. Its OTHER bucket can hide
        // a name satisfying the Cyan raw-address alternate suffix predicate.
        match(4, unrelatedRaw, "historical v686 classifies Cyan raw suffix as OTHER");
        ok(PeerIdentityPolicy.classify(unrelatedRaw, BOND, RAW)
           == PeerIdentityPolicy.Category.RAW_ADDRESS_SUFFIX_RESEARCH,
           "new source-derived audit distinguishes formerly OTHER raw suffix");
        match(2, genericRaw, "historical v686 family category masks raw suffix");
        ok(PeerIdentityPolicy.classify(genericRaw, BOND, RAW)
           == PeerIdentityPolicy.Category.RAW_ADDRESS_SUFFIX_RESEARCH,
           "new classifier records raw suffix before generic family");
        match(3, exact, "historical v686 recognizes normalized constructed suffix");
        ok(PeerIdentityPolicy.classify(exact, BOND, RAW)
           == PeerIdentityPolicy.Category.EXACT_DERIVED_CANDIDATE,
           "exact constructed name distinguished");
        match(3, normalizedOnly, "historical v686 normalized suffix");
        ok(PeerIdentityPolicy.classify(normalizedOnly, BOND, RAW)
           == PeerIdentityPolicy.Category.NORMALIZED_SUFFIX_CANDIDATE,
           "normalized suffix candidate distinguished");
        match(4, unrelated, "different address remains OTHER");
        match(1, null, "missing name remains missing");
        match(4, "OTHER", "single anonymous peer still OTHER");
        ok(PeerIdentityPolicy.assess(BOND, RAW, List.of(unrelatedRaw)).verdict()
           == PeerIdentityPolicy.Verdict.SINGLE_UNVERIFIED_CANDIDATE,
           "even Cyan raw-address suffix is unverified");
        ok(!PeerIdentityPolicy.assess(BOND, RAW, List.of(unrelatedRaw)).connectAuthorized(),
           "raw match never grants connection authorization");
        ok(PeerIdentityPolicy.assess(BOND, RAW, List.of(unrelatedRaw,exact)).verdict()
           == PeerIdentityPolicy.Verdict.AMBIGUOUS_CANDIDATES,
           "raw + exact creates multiple plausible matches");
        ok(PeerIdentityPolicy.assess(BOND, RAW, List.of("OTHER")).verdict()
           == PeerIdentityPolicy.Verdict.NO_CANDIDATE,
           "one OTHER never auto-selects");
        System.out.println("PASS v0.6.8.6 exact legacy-source comparison: "+count+" assertions");
    }
}
