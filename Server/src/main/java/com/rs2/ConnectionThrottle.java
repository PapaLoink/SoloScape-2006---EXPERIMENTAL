package com.rs2;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

public final class ConnectionThrottle {
    // Browser clients share the gateway's loopback address and each need cache
    // and login sockets. Leave room for five players plus reconnect handshakes.
    private static final int LOOPBACK_LIMIT = 16;
    private static final int REMOTE_LIMIT = 3;
    private static final ConcurrentHashMap<String, Integer> connectionCountsByHost =
            new ConcurrentHashMap<String, Integer>();

    private static int limitFor(String host) {
        return host.startsWith("127.") || host.equals("::1")
                || host.equals("0:0:0:0:0:0:0:1") ? LOOPBACK_LIMIT : REMOTE_LIMIT;
    }

    public static boolean tryAcquireConnectionSlot(String host) {
        if (!ConnectionThrottleSettings.connectionsEnabled) return false;
        AtomicBoolean acquired = new AtomicBoolean(false);
        connectionCountsByHost.compute(host, (key, count) -> {
            int current = count == null ? 0 : count;
            if (current >= limitFor(key)) return count;
            acquired.set(true);
            return current + 1;
        });
        return acquired.get();
    }

    public static void releaseConnectionSlot(String host) {
        connectionCountsByHost.computeIfPresent(host,
                (key, count) -> count <= 1 ? null : count - 1);
    }
}
