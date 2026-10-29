package unpackaged;

/** Lightweight regressions for the ::fps command and diagnostic labels. */
public final class FpsDebugOverlayChecks {
    public static void main(String[] args) {
        OndemandRequest.aBoolean1718 = false;
        FpsDebugOverlay.toggle();
        require(OndemandRequest.aBoolean1718, "First ::fps must show diagnostics");
        FpsDebugOverlay.toggle();
        require(!OndemandRequest.aBoolean1718, "Second ::fps must hide diagnostics");
        require(FpsDebugOverlay.typicalLatencyMillis() == -1,
                "Hiding diagnostics must discard old ping samples");

        String[] lines = FpsDebugOverlay.formatLines(60, 42, 499,
                48L * 1024 * 1024, 96L * 1024 * 1024, 256L * 1024 * 1024);
        require(lines.length == 4, "Expected four diagnostics");
        require(lines[0].equals("Framerate: 60 FPS"), "Wrong framerate");
        require(lines[1].equals("Latency: 42ms (last 499)"),
                "Raw RTT spikes must stay visible beside the median");
        require(lines[2].equals("RAM usage: 48 MB"), "Wrong used heap");
        require(lines[3].equals("Memory: 96 / 256 MB"), "Wrong committed/max heap");
        require(FpsDebugOverlay.formatLines(0, -1, -1, 0, 0, -1)[1]
                .equals("Latency: N/A"), "Unavailable latency must not be fabricated");
        require(FpsDebugOverlay.formatLines(0, 42, -1, 0, 0, -1)[1]
                .equals("Latency: 42ms (timeout)"),
                "Timed-out ping should not be printed as negative latency");

        require(FpsDebugOverlay.formatLines(60, 583, 579,
                77L * 1024 * 1024, 243L * 1024 * 1024, 3596L * 1024 * 1024)[1]
                .equals("Latency: 583ms (last 579)"),
                "Latency line should stay compact at high RTT values");
        FpsDebugOverlay.onOutboundBuffered(); // Safe when no ping is pending.
        FpsDebugOverlay.onPingReply(123, 0, 0, 0, 0L, 0L, 0, 0, 0); // Ignore unsolicited replies.

        require(FpsDebugOverlay.diagnosticTimings(true, 1_000L, 1_020L, 1_021L, 1_050L)
                .equals("request to server: 20 ms; server processing: 1 ms; server reply to client: 29 ms"),
                "Loopback ping must split request, server and response delays");
        require(FpsDebugOverlay.diagnosticTimings(false, 1_000L, 1_020L, 1_021L, 1_050L)
                .contains("one-way timings: unavailable"),
                "Remote clocks must not be assumed synchronized");
        require(FpsDebugOverlay.diagnosticTimings(true, 1_020L, 1_000L, 1_021L, 1_050L)
                .contains("one-way timings: unavailable"),
                "Negative inbound times must not be displayed");
        require(FpsDebugOverlay.diagnosticTimings(true, 1_000L, 1_020L, 1_021L, 1_010L)
                .contains("one-way timings: unavailable"),
                "Negative outbound times must not be displayed");

        FpsDebugOverlay.addLatencySample(20);
        FpsDebugOverlay.addLatencySample(22);
        FpsDebugOverlay.addLatencySample(499);
        FpsDebugOverlay.addLatencySample(21);
        FpsDebugOverlay.addLatencySample(23);
        require(FpsDebugOverlay.typicalLatencyMillis() == 22,
                "One slow reply must not distort typical RTT");
        FpsDebugOverlay.addLatencySample(500);
        require(FpsDebugOverlay.typicalLatencyMillis() == 23,
                "The oldest sample must leave the rolling window");
        FpsDebugOverlay.addLatencySample(501);
        require(FpsDebugOverlay.typicalLatencyMillis() == 499,
                "Repeated slow RTT samples must raise the reported median");

        FpsDebugOverlay.toggle();
        require(OndemandRequest.aBoolean1718, "Re-enabled diagnostics must be visible");
        require(FpsDebugOverlay.typicalLatencyMillis() == -1,
                "Re-enabling must not reuse a previous ping history");
        FpsDebugOverlay.toggle();
        System.out.println("FPS diagnostics checks passed.");
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}
