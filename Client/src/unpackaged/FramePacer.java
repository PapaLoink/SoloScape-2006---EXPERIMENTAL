package unpackaged;

/**
 * Frame rate above 2006's 50 (client setting). The game still ticks every 20 ms (logic, input, network and
 * animation all run in ClientApplet.method16 exactly as before), but the screen is redrawn as often as the
 * chosen rate allows. A draw that lands between ticks shows the camera and every character blended between
 * where they were at the last two ticks, so the extra frames move smoothly instead of repeating; animations
 * already step by "ticks since the last draw" (Class45.anInt856), so extra draws never speed them up.
 * At 50 the original 2006 timer loop runs untouched.
 */
public final class FramePacer {
    public static final int[] RATES = {50, 60, 75, 100, 120, 144, 0};
    public static final String[] NAMES = {"50 (2006)", "60", "75", "100", "120", "144", "Unlimited"};
    public static int fps = 50;                            // 0 = as fast as the computer can draw

    private static final long TICK = 20_000_000L;
    private static long nextTick, lastTick, nextFrame;

    private FramePacer() { }

    public static boolean active() {
        return fps != 50;
    }

    public static void setRate(int index) {
        fps = RATES[Math.max(0, Math.min(RATES.length - 1, index))];
        nextTick = 0;
        ModHook.saveSettings();
    }

    public static int rateIndex() {
        for (int i = 0; i < RATES.length; i++) if (RATES[i] == fps) return i;
        return 0;
    }

    /** One pass of the game loop: the ticks that are due, then one draw, then sleep until the next of either. */
    public static void iterate(ClientApplet app) {
        long now = System.nanoTime();
        if (nextTick == 0 || now - nextTick > 250_000_000L) {   // first pass, or the game stalled: start afresh
            nextTick = now;
            nextFrame = now;
        }
        int ticks = 0;
        while (now >= nextTick && ticks < 5) {
            ticks++;
            nextTick += TICK;
        }
        if (ticks > 0) {
            Interp.beforeTicks();
            Class2.anInt50 = ticks;
            for (int i = 0; i < ticks; i++) app.method16(-1);
            Interp.afterTicks();
            lastTick = nextTick - TICK;
        }
        Interp.alpha = Math.max(0f, Math.min(1f, (System.nanoTime() - lastTick) / (float) TICK));
        Interp.apply();
        try {
            app.method30((byte) 90);
        } finally {
            Interp.restore();
        }
        long frame = fps > 0 ? 1_000_000_000L / fps : 0L;
        nextFrame += frame;
        long after = System.nanoTime();
        if (after - nextFrame > TICK) nextFrame = after;     // can't keep up: don't try to catch up in a burst
        long wake = Math.min(nextFrame, nextTick);
        sleepUntil(wake);
    }

    private static void sleepUntil(long wake) {
        long left = wake - System.nanoTime();
        if (left <= 0) {
            Thread.yield();
            return;
        }
        // Windows may round a sleep up to its 15.6 ms timer tick, which alone would cap the game near 64 FPS,
        // so only waits longer than that tick sleep; the rest is a yield loop (cheap, other threads still run)
        if (left > 17_000_000L) {
            try { Thread.sleep((left - 16_000_000L) / 1_000_000L); } catch (InterruptedException e) { return; }
        }
        while (System.nanoTime() < wake) Thread.yield();
    }
}
