package com.parkarsite.g6ap2pdiag685;

import java.util.Locale;

/** Pure fail-closed policy used by the diagnostic and exercised by host-JVM tests. */
final class DiagnosticSafetyRules {
    private DiagnosticSafetyRules() {}

    static boolean mayRemoveGroup(boolean preflightAbsent, boolean preexistingGroup,
                                  boolean formedByTest, boolean connectRequested,
                                  boolean groupContainsTarget) {
        return preflightAbsent && !preexistingGroup && formedByTest &&
                connectRequested && groupContainsTarget;
    }

    static boolean matchesPairedPeerName(String peerName, String addressSuffix) {
        if(peerName==null||addressSuffix==null||addressSuffix.length()!=12)return false;
        String suffix=addressSuffix.toUpperCase(Locale.US);
        for(int i=0;i<suffix.length();i++){
            char c=suffix.charAt(i);
            if(!((c>='0'&&c<='9')||(c>='A'&&c<='F')))return false;
        }
        return peerName.trim().toUpperCase(Locale.US).endsWith(suffix);
    }

    static boolean cleanTerminalClassification(boolean enterAttempted,
                                               boolean exitWriteStarted,
                                               boolean exitCallbackSucceeded,
                                               boolean groupAbsenceConfirmed,
                                               boolean cleanupIssue) {
        return groupAbsenceConfirmed && !cleanupIssue &&
                (!enterAttempted || (exitWriteStarted && exitCallbackSucceeded));
    }
}
