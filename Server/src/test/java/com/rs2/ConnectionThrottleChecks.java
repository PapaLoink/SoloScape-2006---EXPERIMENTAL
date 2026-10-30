package com.rs2;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

public final class ConnectionThrottleChecks {
    public static void main(String[] args) throws Exception {
        checkLimit("127.0.0.1", 16);
        checkLimit("::1", 16);
        checkLimit("192.168.0.50", 3);
        // Two fully connected browser clients each hold a JS5 and a game socket.
        for (int n = 0; n < 4; n++) require(ConnectionThrottle.tryAcquireConnectionSlot("127.0.0.1"),
                "A second browser client was rejected");
        for (int n = 0; n < 4; n++) ConnectionThrottle.releaseConnectionSlot("127.0.0.1");

        for (int pass = 0; pass < 10; pass++) {
            String host = "192.168.0.60";
            CountDownLatch start = new CountDownLatch(1);
            CountDownLatch attempted = new CountDownLatch(32);
            CountDownLatch release = new CountDownLatch(1);
            AtomicInteger accepted = new AtomicInteger();
            List<Thread> threads = new ArrayList<Thread>();
            for (int n = 0; n < 32; n++) {
                Thread thread = new Thread(() -> {
                    try {
                        start.await();
                        boolean acquired = ConnectionThrottle.tryAcquireConnectionSlot(host);
                        if (acquired) accepted.incrementAndGet();
                        attempted.countDown();
                        release.await();
                        if (acquired) ConnectionThrottle.releaseConnectionSlot(host);
                    } catch (InterruptedException failure) {
                        Thread.currentThread().interrupt();
                    }
                });
                threads.add(thread);
                thread.start();
            }
            start.countDown();
            attempted.await();
            int count = accepted.get();
            release.countDown();
            for (Thread thread : threads) thread.join();
            require(count == 3, "Concurrent acquisition exceeded or lost the host limit: " + count);
            checkLimit(host, 3); // All releases must have restored exactly the available capacity.
        }
        ConnectionThrottleSettings.connectionsEnabled = false;
        require(!ConnectionThrottle.tryAcquireConnectionSlot("127.0.0.1"),
                "Disabled connections were accepted");
        ConnectionThrottleSettings.connectionsEnabled = true;
        System.out.println("Concurrent gateway client and connection throttle checks passed.");
    }

    private static void checkLimit(String host, int limit) {
        for (int n = 0; n < limit; n++) require(ConnectionThrottle.tryAcquireConnectionSlot(host),
                "Connection rejected before limit for " + host);
        require(!ConnectionThrottle.tryAcquireConnectionSlot(host), "Host limit was not enforced");
        ConnectionThrottle.releaseConnectionSlot(host);
        require(ConnectionThrottle.tryAcquireConnectionSlot(host), "Released slot was not reusable");
        for (int n = 0; n < limit; n++) ConnectionThrottle.releaseConnectionSlot(host);
        ConnectionThrottle.releaseConnectionSlot(host); // Cleanup must tolerate duplicate release.
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
