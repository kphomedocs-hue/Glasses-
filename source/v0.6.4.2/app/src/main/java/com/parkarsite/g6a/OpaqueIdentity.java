package com.parkarsite.g6a;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

public final class OpaqueIdentity {
    private OpaqueIdentity(){}

    public static String sha256(String privateRemoteIdentity){
        if(privateRemoteIdentity==null||privateRemoteIdentity.isEmpty()) throw new IllegalArgumentException("identity");
        try{
            byte[] d=MessageDigest.getInstance("SHA-256").digest(privateRemoteIdentity.getBytes(StandardCharsets.UTF_8));
            StringBuilder b=new StringBuilder(64);
            for(byte x:d)b.append(String.format("%02x",x&255));
            return b.toString();
        }catch(Exception e){
            throw new IllegalStateException(e);
        }
    }

    public static boolean isOpaqueId(String s){
        return s!=null && s.matches("[0-9a-f]{64}");
    }
}
