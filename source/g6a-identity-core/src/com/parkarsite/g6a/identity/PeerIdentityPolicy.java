package com.parkarsite.g6a.identity;

import java.util.Collection;
import java.util.Locale;

/**
 * Offline, source-derived CANDIDATE classification only.
 * Names and addresses never leave this method in its results.
 * No result authorizes Wi-Fi Direct connection or establishes device identity.
 */
public final class PeerIdentityPolicy {
    private PeerIdentityPolicy() {}

    public enum Category {
        EXACT_DERIVED_CANDIDATE,
        NORMALIZED_SUFFIX_CANDIDATE,
        RAW_ADDRESS_SUFFIX_RESEARCH,
        GENERIC_FAMILY_ONLY,
        OTHER,
        EMPTY_NAME
    }

    public enum Verdict {
        INVALID_BONDED_INPUT,
        NO_CANDIDATE,
        SINGLE_UNVERIFIED_CANDIDATE,
        AMBIGUOUS_CANDIDATES
    }

    public record Report(int total, int exact, int normalizedSuffix,
                         int rawSuffixResearch, int familyOnly, int other,
                         int missingName, Verdict verdict) {
        /** Intentionally never returns a P2P name or address. */
        public boolean authenticated() { return false; }
        public boolean connectAuthorized() { return false; }
    }

    public static boolean validBondedTargetName(String name) {
        if (name == null) return false;
        String u = name.toUpperCase(Locale.ROOT);
        return "AIMB-G1".equals(u) || (u.startsWith("AIMB-G1_") && u.length() > 8);
    }

    /**
     * Returns a normalized 12-hex bonded BLE address, or null.
     * This address must NOT be presumed equal to WifiP2pDevice.deviceAddress.
     */
    static String normalizeBluetoothAddress(String address) {
        if (address == null) return null;
        if (!address.matches("(?i)(?:[0-9a-f]{2}:){5}[0-9a-f]{2}")
                && !address.matches("(?i)[0-9a-f]{12}")) return null;
        return address.replace(":", "").toUpperCase(Locale.ROOT);
    }

    /**
     * Reconstructs the Cyan candidate from its documented BLE connection handler.
     * Output is a candidate string ONLY. No claim of current over-the-air equality.
     */
    static String deriveCandidate(String bondedName, String bondedBleAddress) {
        if (!validBondedTargetName(bondedName)) return null;
        String suffix = normalizeBluetoothAddress(bondedBleAddress);
        if (suffix == null) return null;
        int underscore = bondedName.lastIndexOf('_');
        String stem = underscore >= 0 ? bondedName.substring(underscore + 1) : bondedName;
        if (stem.isEmpty()) return null;
        return stem + "_" + suffix;
    }

    /** Exact name, normalized suffix, and historic raw suffix remain DISTINCT. */
    public static Category classify(String observedPeerName, String bondedName,
                                    String bondedBleAddress) {
        if (observedPeerName == null || observedPeerName.isEmpty()) return Category.EMPTY_NAME;
        String derived = deriveCandidate(bondedName, bondedBleAddress);
        if (derived == null) return Category.OTHER;
        if (observedPeerName.equalsIgnoreCase(derived)) return Category.EXACT_DERIVED_CANDIDATE;
        String address = normalizeBluetoothAddress(bondedBleAddress);
        if (observedPeerName.toUpperCase(Locale.ROOT).endsWith(address))
            return Category.NORMALIZED_SUFFIX_CANDIDATE;
        // Separate documentary observation: Cyan's alternate endsWith argument was
        // the raw saved Bluetooth address, not the normalized hex value.
        if (bondedBleAddress != null && observedPeerName.endsWith(bondedBleAddress))
            return Category.RAW_ADDRESS_SUFFIX_RESEARCH;
        if (observedPeerName.toUpperCase(Locale.ROOT).startsWith("AIMB-G1"))
            return Category.GENERIC_FAMILY_ONLY;
        return Category.OTHER;
    }

    /**
     * All potentially plausible names are counted; the full list is not returned.
     * A single matching NAME still is not authentication or connection authorization.
     */
    public static Report assess(String bondedName, String bondedBleAddress,
                                Collection<String> observedPeerNames) {
        if (deriveCandidate(bondedName, bondedBleAddress) == null || observedPeerNames == null)
            return new Report(0, 0, 0, 0, 0, 0, 0, Verdict.INVALID_BONDED_INPUT);
        int total=0, exact=0, norm=0, raw=0, family=0, other=0, empty=0;
        for (String name: observedPeerNames) {
            total++;
            switch (classify(name, bondedName, bondedBleAddress)) {
                case EXACT_DERIVED_CANDIDATE -> exact++;
                case NORMALIZED_SUFFIX_CANDIDATE -> norm++;
                case RAW_ADDRESS_SUFFIX_RESEARCH -> raw++;
                case GENERIC_FAMILY_ONLY -> family++;
                case EMPTY_NAME -> empty++;
                case OTHER -> other++;
            }
        }
        int plausible = exact + norm + raw;
        Verdict v = plausible == 0 ? Verdict.NO_CANDIDATE
                : plausible == 1 ? Verdict.SINGLE_UNVERIFIED_CANDIDATE
                : Verdict.AMBIGUOUS_CANDIDATES;
        return new Report(total, exact, norm, raw, family, other, empty, v);
    }
}
