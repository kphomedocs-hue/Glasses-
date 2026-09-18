package com.parkarsite.g1.network;

import java.net.URI;

public final class LocalNetworkPolicySelfTest {
    public static void main(String[] args) throws Exception {
        allow("http://192.168.0.1/files/media.config");
        allow("http://10.0.0.1:8080/files/");
        allow("http://172.16.2.4/files/");
        allow("http://169.254.1.2/files/");

        reject("https://192.168.0.1/files/");
        reject("http://8.8.8.8/files/");
        reject("http://example.com/files/");
        reject("http://172.15.0.1/files/");
        reject("http://172.32.0.1/files/");

        truth(LocalNetworkPolicy.isPrivateIpv4("192.168.1.1"), "private IPv4");
        truth(!LocalNetworkPolicy.isPrivateIpv4("192.168.001.1"), "non-canonical numeric form rejected");

        System.out.println("PASS device-network local policy");
    }

    private static void allow(String s) throws Exception {
        LocalNetworkPolicy.requireAllowedHttpUri(new URI(s));
    }

    private static void reject(String s) throws Exception {
        try {
            LocalNetworkPolicy.requireAllowedHttpUri(new URI(s));
        } catch (IllegalArgumentException expected) {
            return;
        }
        throw new AssertionError("Expected rejection: " + s);
    }

    private static void truth(boolean value, String what) {
        if (!value) throw new AssertionError(what);
    }
}
