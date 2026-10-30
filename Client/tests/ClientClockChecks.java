package unpackaged;

import jagex.graphics.AbstractImage;

/**
 * Regression check that the normal client chooses the original nanosecond timer
 * instead of silently falling back to Class46_Sub1.
 */
public final class ClientClockChecks {
    public static void main(String[] args) {
        Class46 clock = AbstractImage.method1009((byte) 89);
        require(clock instanceof Class46_Sub2,
                "The client must load Class46_Sub2, not the millisecond fallback");

        clock.method938((byte) -50);
        int firstTicks = clock.method939(4, 1, 20);
        require(firstTicks >= 1 && firstTicks <= 10,
                "The timer must return between 1 and 10 client ticks");

        // Simulate a long pause and confirm the original catch-up limit.
        Class46_Sub2 nanoTimer = (Class46_Sub2) clock;
        nanoTimer.aLong1569 = System.nanoTime() - 500_000_000L;
        require(nanoTimer.method939(4, 1, 20) == 10,
                "A delayed frame must catch up by at most 10 client ticks");

        nanoTimer.method934(0);
        int afterReset = nanoTimer.method939(4, 1, 20);
        require(afterReset >= 1 && afterReset <= 10,
                "Reset must leave the nanosecond timer usable");

        System.out.println("Nanosecond client clock checks passed.");
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}
