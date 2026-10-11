package com.parkarsite.g6ap2pdiag685;

/** No Android dependency. Check all boolean combinations, not just happy paths. */
public final class DiagnosticSafetyRulesTest {
    private static int assertions;
    private static void check(boolean actual,boolean expected,String name){
        assertions++;
        if(actual!=expected)throw new AssertionError(name+" expected="+expected+" actual="+actual);
    }
    public static void main(String[] args) {
        for(int mask=0;mask<32;mask++){
            boolean before=(mask&1)!=0,previous=(mask&2)!=0,formed=(mask&4)!=0;
            boolean requested=(mask&8)!=0,target=(mask&16)!=0;
            boolean should=before&&!previous&&formed&&requested&&target;
            check(DiagnosticSafetyRules.mayRemoveGroup(before,previous,formed,requested,target),
                    should,"remove group combination "+mask);
        }
        for(int mask=0;mask<32;mask++){
            boolean enter=(mask&1)!=0,exitStarted=(mask&2)!=0,exitAck=(mask&4)!=0;
            boolean absence=(mask&8)!=0,issues=(mask&16)!=0;
            boolean should=absence&&!issues&&(!enter||(exitStarted&&exitAck));
            check(DiagnosticSafetyRules.cleanTerminalClassification(enter,exitStarted,exitAck,absence,issues),
                    should,"classify cleanup combination "+mask);
        }
        check(DiagnosticSafetyRules.mayRemoveGroup(false,false,true,true,true),false,"no preflight ownership proof");
        check(DiagnosticSafetyRules.mayRemoveGroup(true,true,true,true,true),false,"never remove preexisting group");
        check(DiagnosticSafetyRules.mayRemoveGroup(true,false,false,true,false),false,"never remove unrelated group");
        check(DiagnosticSafetyRules.mayRemoveGroup(true,false,true,true,false),false,"claimed group formation is insufficient without target identity");
        check(DiagnosticSafetyRules.mayRemoveGroup(true,false,false,true,true),false,"never remove when this test has not confirmed group formation");
        check(DiagnosticSafetyRules.cleanTerminalClassification(true,false,false,true,false),false,"ENTER without EXIT");
        check(DiagnosticSafetyRules.cleanTerminalClassification(true,true,false,true,false),false,"EXIT callback missing");
        check(DiagnosticSafetyRules.cleanTerminalClassification(true,true,true,false,false),false,"group still present");
        check(DiagnosticSafetyRules.cleanTerminalClassification(true,true,true,true,true),false,"cleanup callback error");
        System.out.println("PASS "+assertions+" host-JVM negative-path policy assertions");
    }
}
