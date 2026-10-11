package com.parkarsite.g6apeerdiag686;

import java.util.Locale;

/** Pure passive peer-category/cleanup policy; no network connection or Android dependency. */
final class DiagnosticSafetyRules {
    private DiagnosticSafetyRules(){}
    // Mutually exclusive: 1 missing, 2 generic AIMB-G1 family prefix, 3 full paired BLE suffix, 4 other.
    static int classifyPeerName(String peerName,String pairedSuffix){
        if(peerName==null||peerName.trim().isEmpty())return 1;
        String name=peerName.trim().toUpperCase(Locale.US);
        if(fullSuffixMatch(name,pairedSuffix))return 3;
        if(name.startsWith("AIMB-G1"))return 2;
        return 4;
    }
    static boolean fullSuffixMatch(String upperName,String addressSuffix){
        if(upperName==null||addressSuffix==null||addressSuffix.length()!=12)return false;
        String suffix=addressSuffix.toUpperCase(Locale.US);
        for(int i=0;i<suffix.length();i++){char c=suffix.charAt(i);if(!((c>='0'&&c<='9')||(c>='A'&&c<='F')))return false;}
        return upperName.endsWith(suffix);
    }
    static boolean cleanTerminalClassification(boolean enterAttempted,boolean exitStarted,
                                                boolean exitAcknowledged,boolean groupAbsent,
                                                boolean cleanupIssue) {
        return groupAbsent&&!cleanupIssue&&(!enterAttempted||(exitStarted&&exitAcknowledged));
    }
}