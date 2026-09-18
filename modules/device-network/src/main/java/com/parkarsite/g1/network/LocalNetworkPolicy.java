package com.parkarsite.g1.network;

import java.net.URI;

/**
 * Safety policy for future glasses-local HTTP.
 * No DNS names are accepted here: the transport must use an observed numeric local IP.
 */
public final class LocalNetworkPolicy {
    private LocalNetworkPolicy() {}

    public static void requireAllowedHttpUri(URI uri) {
        if (uri == null) throw new IllegalArgumentException("URI is required");
        if (!"http".equalsIgnoreCase(uri.getScheme())) {
            throw new IllegalArgumentException("Only local HTTP is allowed");
        }
        if (uri.getUserInfo() != null) {
            throw new IllegalArgumentException("User-info is not allowed");
        }
        String host = uri.getHost();
        if (host == null || !isPrivateIpv4(host)) {
            throw new IllegalArgumentException("Host must be a numeric private/local IPv4 address");
        }
        int port = uri.getPort();
        if (port != -1 && (port < 1 || port > 65535)) {
            throw new IllegalArgumentException("Invalid port");
        }
    }

    public static boolean isPrivateIpv4(String host) {
        String[] p = host.split("\\.");
        if (p.length != 4) return false;
        int[] n = new int[4];
        for (int i = 0; i < 4; i++) {
            if (p[i].isEmpty() || p[i].length() > 3) return false;
            try {
                n[i] = Integer.parseInt(p[i]);
            } catch (NumberFormatException e) {
                return false;
            }
            if (n[i] < 0 || n[i] > 255) return false;
            if (!Integer.toString(n[i]).equals(p[i])) return false;
        }

        if (n[0] == 10) return true;
        if (n[0] == 172 && n[1] >= 16 && n[1] <= 31) return true;
        if (n[0] == 192 && n[1] == 168) return true;
        if (n[0] == 169 && n[1] == 254) return true;
        return n[0] == 127;
    }
}
