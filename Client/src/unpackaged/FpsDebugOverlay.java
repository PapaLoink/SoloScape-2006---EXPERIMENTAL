package unpackaged;

import jagex.io.FrameBuffer;

/**
 * Client-side ::fps diagnostics. The ping is an application-level round trip
 * to the game server, not an estimate based on time between server updates.
 */
public final class FpsDebugOverlay {
    private static final int PING_OPCODE = 254;
    private static final long PING_INTERVAL_NANOS = 500_000_000L;
    private static final int SAMPLE_WINDOW = 5;
    private static final int SPIKE_THRESHOLD_MILLIS = 200;
    private static final long PING_TIMEOUT_NANOS = 5_000_000_000L;
    private static final int YELLOW = 0xFFFF00;

    private static long lastSentNanos;
    private static volatile long pendingSinceNanos;
    private static int nextPingId;
    private static volatile int pendingPingId;
    private static int latencyMillis = -1;
    private static final int[] latencySamples = new int[SAMPLE_WINDOW];
    private static int latencySampleCount;
    private static int latencySampleCursor;
    private static long lastGameTickNanos;
    private static long longestTickGapNanos;
    private static long clientBufferWaitNanos = -1L;
    private static int serverQueuedBytes = -1;
    private static volatile long socketTransmittedNanos;
    private static volatile int socketTransmittedPingId;
    private static long pendingClientEpochMillis;

    private FpsDebugOverlay() {
    }

    public static void toggle() {
        OndemandRequest.aBoolean1718 = !OndemandRequest.aBoolean1718;
        clearPing();
    }

    private static void clearPing() {
        lastSentNanos = 0L;
        pendingSinceNanos = 0L;
        latencyMillis = -1;
        latencySampleCount = 0;
        latencySampleCursor = 0;
        lastGameTickNanos = 0L;
        longestTickGapNanos = 0L;
        clientBufferWaitNanos = -1L;
        serverQueuedBytes = -1;
        socketTransmittedNanos = 0L;
        socketTransmittedPingId = 0;
        pendingClientEpochMillis = 0L;
    }

    /** Called on the game tick before the outgoing packet buffer is flushed. */
    public static void tick() {
        if (!OndemandRequest.aBoolean1718 || Class37.gameSocket == null) {
            clearPing();
            return;
        }
        long now = System.nanoTime();
        if (lastGameTickNanos != 0L && pendingSinceNanos != 0L) {
            longestTickGapNanos = Math.max(longestTickGapNanos, now - lastGameTickNanos);
        }
        lastGameTickNanos = now;
        if (pendingSinceNanos != 0L) {
            if (now - pendingSinceNanos < PING_TIMEOUT_NANOS) {
                return;
            }
            // A lost response must not leave a stale latency value displayed.
            pendingSinceNanos = 0L;
            latencyMillis = -1;
        }
        if (lastSentNanos != 0L && now - lastSentNanos < PING_INTERVAL_NANOS) {
            return;
        }
        pendingPingId = ++nextPingId;
        lastSentNanos = now;
        pendingSinceNanos = now;
        longestTickGapNanos = 0L;
        clientBufferWaitNanos = -1L;
        serverQueuedBytes = -1;
        socketTransmittedNanos = 0L;
        socketTransmittedPingId = 0;
        pendingClientEpochMillis = System.currentTimeMillis();
        FrameBuffer.outgoingGameBuffer.putFrame(PING_OPCODE);
        FrameBuffer.outgoingGameBuffer.putDword(pendingPingId);
        FrameBuffer.outgoingGameBuffer.putQword(pendingClientEpochMillis);
    }

    /**
     * The packet was passed to JSocket's asynchronous writer.
     * This is NOT a timestamp for actual bytes hitting the network.
     */
    public static void onOutboundBuffered() {
        if (pendingSinceNanos != 0L && clientBufferWaitNanos < 0L) {
            clientBufferWaitNanos = Math.max(0L, System.nanoTime() - pendingSinceNanos);
            if (Class37.gameSocket != null) {
                Class37.gameSocket.markFpsProbe(pendingPingId);
            }
        }
    }

    /** The socket writer reports when the kernel accepted the probe bytes. */
    public static void onSocketTransmitted(int pingId, long transmittedNanos) {
        if (pingId == pendingPingId && pendingSinceNanos != 0L) {
            socketTransmittedNanos = transmittedNanos;
            socketTransmittedPingId = pingId;
        }
    }

    /** A reply is accepted only for the current in-flight request. */
    public static void onPingReply(int pingId, int queuedBytes,
            int serverLockWaitMs, int serverReceiveGapMs,
            long serverReceivedMillis, long serverRepliedMillis,
            int serverReadToHandlerMs, int serverLongestDispatchMs,
            int serverLongestReactorGapMs) {
        if (!OndemandRequest.aBoolean1718 || pendingSinceNanos == 0L
                || pingId != pendingPingId) {
            return;
        }
        long duration = System.nanoTime() - pendingSinceNanos;
        long receivedClientMillis = System.currentTimeMillis();
        serverQueuedBytes = Math.max(0, queuedBytes);
        if (duration >= 0L) {
            latencyMillis = (int) Math.min(Integer.MAX_VALUE,
                    (duration + 500_000L) / 1_000_000L);
            addLatencySample(latencyMillis);
            if (latencyMillis >= SPIKE_THRESHOLD_MILLIS) {
                long longestTickGapMillis = (longestTickGapNanos + 500_000L) / 1_000_000L;
                long clientBufferedMs = clientBufferWaitNanos < 0L ? -1L
                        : (clientBufferWaitNanos + 500_000L) / 1_000_000L;
                long writerDelayMs = socketTransmittedPingId != pingId
                        ? -1L : (Math.max(0L, socketTransmittedNanos - pendingSinceNanos)
                                + 500_000L) / 1_000_000L;
                long afterSocketWriteMs = socketTransmittedPingId != pingId
                        ? -1L : (Math.max(0L, System.nanoTime() - socketTransmittedNanos)
                                + 500_000L) / 1_000_000L;
                boolean sameHost = Class37.gameSocket != null
                        && Class37.gameSocket.socket != null
                        && Class37.gameSocket.socket.getInetAddress().isLoopbackAddress();
                String timingBreakdown = diagnosticTimings(sameHost,
                        pendingClientEpochMillis, serverReceivedMillis,
                        serverRepliedMillis, receivedClientMillis);
                System.out.println("[::fps] RTT spike: " + latencyMillis
                        + " ms; " + timingBreakdown
                        + "; socket writer delay: "
                        + (writerDelayMs < 0 ? "unknown" : writerDelayMs + " ms")
                        + "; after socket write: "
                        + (afterSocketWriteMs < 0 ? "unknown" : afterSocketWriteMs + " ms")
                        + "; client buffer wait: "
                        + (clientBufferedMs < 0L ? "unknown" : clientBufferedMs + " ms")
                        + "; server lock wait: " + serverLockWaitMs
                        + " ms; ping assembly delay: " + serverReadToHandlerMs
                        + " ms; longest reactor read/dispatch: " + serverLongestDispatchMs
                        + " ms; longest reactor loop gap: " + serverLongestReactorGapMs
                        + " ms; server receive interval: " + serverReceiveGapMs
                        + " ms; server queued outbound: " + serverQueuedBytes
                        + " bytes; longest client game-tick gap: "
                        + longestTickGapMillis + " ms");
            }
        }
        pendingSinceNanos = 0L;
    }

    /**
     * Directional wall-clock timings are valid for loopback clients (one OS
     * clock). Remote clients may have unsynchronized clocks: never present a
     * made-up upstream/downstream split for those connections.
     */
    static String diagnosticTimings(boolean loopback, long clientSent, long serverReceived,
            long serverReplied, long clientReceived) {
        long serverProcessing = serverReplied - serverReceived;
        if (serverProcessing < 0L) {
            return "server processing: unavailable";
        }
        if (!loopback || clientSent <= 0L || serverReceived < clientSent
                || clientReceived < serverReplied) {
            return "server processing: " + serverProcessing
                    + " ms; one-way timings: unavailable (clocks not shared)";
        }
        return "request to server: " + (serverReceived - clientSent)
                + " ms; server processing: " + serverProcessing
                + " ms; server reply to client: " + (clientReceived - serverReplied) + " ms";
    }

    static void addLatencySample(int milliseconds) {
        latencySamples[latencySampleCursor] = milliseconds;
        latencySampleCursor = (latencySampleCursor + 1) % SAMPLE_WINDOW;
        latencySampleCount = Math.min(SAMPLE_WINDOW, latencySampleCount + 1);
    }

    // A rolling median shows typical network responsiveness without disguising
    // individual slow replies: the latest raw RTT is shown beside it.
    static int typicalLatencyMillis() {
        if (latencySampleCount == 0) {
            return -1;
        }
        int[] samples = new int[latencySampleCount];
        System.arraycopy(latencySamples, 0, samples, 0, latencySampleCount);
        java.util.Arrays.sort(samples);
        return samples[latencySampleCount / 2];
    }

    public static void draw() {
        if (!OndemandRequest.aBoolean1718 || Class39_Sub5_Sub14.p12fullFont == null) {
            return;
        }
        Runtime runtime = Runtime.getRuntime();
        long allocated = runtime.totalMemory();
        String[] lines = formatLines(Class39_Sub5_Sub18.anInt2119,
                typicalLatencyMillis(), latencyMillis,
                allocated - runtime.freeMemory(), allocated, runtime.maxMemory());
        // The scene framebuffer is 512 pixels wide. method630 right-aligns text.
        for (int index = 0; index < lines.length; index++) {
            Class39_Sub5_Sub14.p12fullFont.method630(
                    Class39_Sub5_Sub9.createJstring(lines[index]),
                    507, 20 + index * 15, YELLOW);
        }
    }

    static String[] formatLines(int fps, int typicalPingMs, int latestPingMs,
            long used, long allocated, long maximum) {
        // Keep all four diagnostics inside the 512px game viewport.
        // The median is shown by default; raw samples remain visible as "last".
        String pingText = typicalPingMs < 0 ? "N/A"
                : typicalPingMs + "ms"
                        + (latestPingMs == typicalPingMs ? ""
                                : latestPingMs < 0 ? " (timeout)"
                                : " (last " + latestPingMs + ")");
        return new String[] {
                "Framerate: " + fps + " FPS",
                "Latency: " + pingText,
                "RAM usage: " + toMiB(used) + " MB",
                "Memory: " + toMiB(allocated) + " / "
                        + (maximum <= 0L ? "N/A" : toMiB(maximum) + " MB")
        };
    }

    private static long toMiB(long bytes) {
        return Math.max(0L, bytes) / (1024L * 1024L);
    }
}
