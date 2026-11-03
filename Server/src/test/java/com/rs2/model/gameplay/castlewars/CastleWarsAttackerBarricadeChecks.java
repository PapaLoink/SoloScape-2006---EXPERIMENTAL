package com.rs2.model.gameplay.castlewars;

import com.rs2.bot.CastleWarsBotBarricadeAi;
import com.rs2.model.Position;
import java.lang.reflect.Field;

/** Regression checks for the Castle Wars attacking bots' tinderbox route rescue. */
public final class CastleWarsAttackerBarricadeChecks {
    private CastleWarsAttackerBarricadeChecks() {
    }

    public static void main(String[] args) throws Exception {
        require(CastleWarsEngineeringManager.TINDERBOX_ITEM_ID == 590,
                "Attacker tinderboxes must use the standard tinderbox item");
        require(CastleWarsEngineeringManager.isGameSupplyItemId(
                CastleWarsEngineeringManager.TINDERBOX_ITEM_ID),
                "The game's supply system must permit issuing tinderboxes to bots");
        require(CastleWarsEngineeringManager.isGameSupplyItemId(
                CastleWarsEngineeringManager.EXPLOSIVE_POTION_ID),
                "Existing explosive potion supplies must remain supported");
        require(!CastleWarsEngineeringManager.isGameSupplyItemId(995),
                "Ordinary items must not be issuable as Castle Wars supplies");

        // The barricade must be ahead of the attacker, in the narrow travel
        // corridor, and on the correct plane. The defensive team's identity
        // does not matter: friendly barricades can also obstruct the return.
        Position from = new Position(2420, 3081, 0);
        Position to = new Position(2426, 3081, 0);
        assertAhead(from, to, new Position(2421, 3081, 0), true);
        assertAhead(from, to, new Position(2423, 3082, 0), true);
        assertAhead(from, to, new Position(2419, 3081, 0), false);
        assertAhead(from, to, new Position(2423, 3086, 0), false);
        assertAhead(from, to, new Position(2427, 3081, 0), false);
        assertAhead(from, to, new Position(2421, 3081, 1), false);
        assertAhead(from, from, new Position(2421, 3081, 0), false);

        // Check mirrored north/south routes on the other team's side.
        Position zamorakFrom = new Position(2373, 3122, 0);
        Position zamorakTo = new Position(2373, 3118, 0);
        assertAhead(zamorakFrom, zamorakTo, new Position(2373, 3121, 0), true);
        assertAhead(zamorakFrom, zamorakTo, new Position(2373, 3123, 0), false);

        Field threshold = CastleWarsBotBarricadeAi.class.getDeclaredField(
                "STALLED_ROUTE_ATTEMPTS");
        threshold.setAccessible(true);
        require(threshold.getInt(null) >= 2,
                "Do not ignite barricades on the very first pathfinding attempt");
        require(threshold.getInt(null) <= 12,
                "Stuck attackers must start removing nearby barricades promptly");

        require(!CastleWarsEngineeringManager.isBarricadeBurning(null),
                "An absent barricade must not hold an attacker in the waiting state");

        System.out.println("Castle Wars attacker tinderbox barricade checks passed");
        System.exit(0);
    }

    private static void assertAhead(Position from, Position to,
                                    Position barricade, boolean expected) {
        boolean actual = CastleWarsEngineeringManager.isBarricadeAheadOnRoute(
                from, to, barricade);
        require(actual == expected,
                "Incorrect barricade route detection from " + from
                        + " towards " + to + " via " + barricade);
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}
