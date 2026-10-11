package com.parkarsite.g6ap2pdiag685;

/** Pure fail-closed policy used by the diagnostic and exercised by host-JVM tests. */
final class DiagnosticSafetyRules {
    private DiagnosticSafetyRules() {}

    static boolean mayRemoveGroup(boolean preflightAbsent, boolean preexistingGroup,
                                  boolean formedByTest, boolean connectRequested,
                                  boolean groupContainsTarget) {
        return preflightAbsent && !preexistingGroup && formedByTest &&
                connectRequested && groupContainsTarget;
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
