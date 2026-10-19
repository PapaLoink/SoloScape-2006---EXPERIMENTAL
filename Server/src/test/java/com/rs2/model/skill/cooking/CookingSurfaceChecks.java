package com.rs2.model.skill.cooking;

/**
 * Fast regression checks for the 2006 distinction between a range and a fire.
 * Run alongside CookingInterfaceChecks after compiling the server/test sources.
 */
public final class CookingSurfaceChecks {
    public static void main(String[] args) {
        check(CookingManager.isFireCookingSurface(2732, "Fire"), "Lit log fire");
        check(CookingManager.isFireCookingSurface(0, "Fireplace"), "Lit fireplace");
        check(CookingManager.isFireCookingSurface(0, "Campfire"), "Permanent campfire");
        check(!CookingManager.isFireCookingSurface(0, "Unlit fireplace"), "Unlit fireplace");
        check(!CookingManager.isFireCookingSurface(0, "Fireplace space"), "Construction hotspot");
        check(CookingManager.isRangeCookingSurface("Range"), "Ordinary range");
        check(CookingManager.isRangeCookingSurface("Cooking range"), "Cooking range");
        check(CookingManager.isRangeCookingSurface("Stove"), "Stove");
        check(!CookingManager.isRangeCookingSurface("Fireplace"), "Fireplace must not be a range");

        check(CookableFoodDefinition.forRawItemId(2132).canCookOnFire(), "Raw meat on a fire");
        check(CookableFoodDefinition.forRawItemId(317).canCookOnFire(), "Raw shrimps on a fire");
        check(!CookableFoodDefinition.forRawItemId(2307).canCookOnFire(), "Bread needs a range");
        check(!CookableFoodDefinition.forRawItemId(2321).canCookOnFire(), "Pie needs a range");
        check(!CookableFoodDefinition.forRawItemId(1942).canCookOnFire(), "Potato burns on fire");

        System.out.println("Cooking surface checks passed.");
    }

    private static void check(boolean condition, String description) {
        if (!condition) {
            throw new AssertionError(description);
        }
    }
}
