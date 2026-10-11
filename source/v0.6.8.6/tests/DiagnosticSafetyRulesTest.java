package com.parkarsite.g6apeerdiag686;

public final class DiagnosticSafetyRulesTest {
 private static int checks=0;
 private static void c(boolean ok,String name){checks++;if(!ok)throw new AssertionError(name);}
 public static void main(String[] args){
  String suffix="A1B2C3D4E5F6";
  c(DiagnosticSafetyRules.classifyPeerName(null,suffix)==1,"null name");
  c(DiagnosticSafetyRules.classifyPeerName(" ",suffix)==1,"empty name");
  c(DiagnosticSafetyRules.classifyPeerName("AIMB-G1",suffix)==2,"model alone untrusted");
  c(DiagnosticSafetyRules.classifyPeerName("AIMB-G1_other",suffix)==2,"family only");
  c(DiagnosticSafetyRules.classifyPeerName("AIMB-G1_A1B2C3D4E5F6",suffix)==3,"full suffix");
  c(DiagnosticSafetyRules.classifyPeerName("other_A1B2C3D4E5F6",suffix)==3,"suffix without model");
  c(DiagnosticSafetyRules.classifyPeerName("AIMB-G1_A1B2C3D4E5F0",suffix)==2,"wrong suffix not matched");
  c(DiagnosticSafetyRules.classifyPeerName("Nearby-Printer",suffix)==4,"other name");
  c(DiagnosticSafetyRules.classifyPeerName("AIMB-G1_A1B2C3D4E5F6","F6")==2,"truncated suffix unsafe");
  c(DiagnosticSafetyRules.classifyPeerName("AIMB-G1_A1B2C3D4E5F6","A1B2C3D4E5GG")==2,"nonhex suffix unsafe");
  for(int mask=0;mask<32;mask++){
   boolean enter=(mask&1)!=0,exit=(mask&2)!=0,ack=(mask&4)!=0,abs=(mask&8)!=0,issue=(mask&16)!=0;
   boolean expected=abs&&!issue&&(!enter||(exit&&ack));
   c(DiagnosticSafetyRules.cleanTerminalClassification(enter,exit,ack,abs,issue)==expected,"cleanup "+mask);
  }
  System.out.println("PASS "+checks+" host-JVM passive diagnostic policy assertions");
 }
}