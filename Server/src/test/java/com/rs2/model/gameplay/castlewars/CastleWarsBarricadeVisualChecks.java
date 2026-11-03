package com.rs2.model.gameplay.castlewars;

/**
 * Regression: Castle Wars barricade object IDs are visual states, not team IDs.
 * Run with the compiled Server classes on the classpath.
 */
public final class CastleWarsBarricadeVisualChecks {
    public static void main(String[] args) {
        require(CastleWarsEngineeringManager.BARRICADE_OBJECT_UNLIT == 4421,
                "Unlit barricade must use the non-burning cache object");
        require(CastleWarsEngineeringManager.BARRICADE_OBJECT_BURNING == 4422,
                "Burning barricade must use the fire-animated cache object");

        // Both teams place the same non-burning barricade. Only fire changes it.
        require(CastleWarsEngineeringManager.barricadeVisualObjectId(false) == 4421,
                "A freshly placed or extinguished barricade must not be on fire");
        require(CastleWarsEngineeringManager.barricadeVisualObjectId(true) == 4422,
                "Tinderbox ignition must choose the burning animation");
        require(CastleWarsEngineeringManager.barricadeVisualObjectId(false)
                        != CastleWarsEngineeringManager.barricadeVisualObjectId(true),
                "Unlit and burning barricades must have different visuals");

        System.out.println("Castle Wars barricade visual checks passed");
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}
