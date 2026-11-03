package com.rs2.bot;

import com.rs2.model.Position;
import com.rs2.model.gameplay.castlewars.CastleWarsEngineeringManager;
import com.rs2.model.gameplay.castlewars.CastleWarsManager;
import java.util.IdentityHashMap;
import java.util.Map;

/**
 * Reroute-rescue for attackers blocked by Castle Wars barricades.
 *
 * Attackers first try normal pathfinding. When the same destination is
 * repeatedly requested without movement, ignite a barricade immediately
 * ahead and wait for its 20-second burn-out before attempting the route again.
 * This works for both ordinary flag runners and underground attackers.
 */
public final class CastleWarsBotBarricadeAi {
    private static final int STALLED_ROUTE_ATTEMPTS = 7;
    private static final Map<BotPlayer, BlockedRoute> states =
            new IdentityHashMap<BotPlayer, BlockedRoute>();

    private CastleWarsBotBarricadeAi() {
    }

    public static void clear(BotPlayer bot) {
        states.remove(bot);
    }

    /**
     * @return true when the bot should suspend pathfinding to let a nearby
     * burning barricade disappear, false when it can continue normal movement.
     */
    public static boolean handleBlockedRoute(BotPlayer bot, Position destination) {
        if (bot == null || destination == null || bot.isDead()
                || CastleWarsManager.getGameTeam(bot) == null
                || bot.getPosition().getPlane() != destination.getPlane()) {
            if (bot != null) {
                clear(bot);
            }
            return false;
        }

        BlockedRoute state = states.get(bot);
        if (state == null) {
            state = new BlockedRoute();
            states.put(bot, state);
        }

        if (!destination.equals(state.destination)
                || !bot.getPosition().equals(state.lastPosition)) {
            state.destination = destination.copy();
            state.lastPosition = bot.getPosition().copy();
            state.stalledAttempts = 0;
            state.waitingForFire = null;
        }

        if (state.waitingForFire != null) {
            Position burning = state.waitingForFire;
            if (CastleWarsEngineeringManager.getBarricadeHitpoints(burning) == 0) {
                // The fire has destroyed the object. Retry the original path.
                state.waitingForFire = null;
                state.stalledAttempts = 0;
                return false;
            }
            if (CastleWarsEngineeringManager.isBarricadeBurning(burning)
                    || bot.getInventoryManager().getItemAmount(
                            CastleWarsEngineeringManager.TINDERBOX_ITEM_ID) > 0
                    && CastleWarsEngineeringManager.igniteBarricade(bot, burning)) {
                // A defender may extinguish it; relight rather than endlessly wait.
                bot.getMovementQueue().clear();
                bot.getMovementQueue().clearMovementActions();
                return true;
            }
            state.waitingForFire = null;
        }

        if (++state.stalledAttempts < STALLED_ROUTE_ATTEMPTS) {
            return false;
        }

        if (bot.getInventoryManager().getItemAmount(
                CastleWarsEngineeringManager.TINDERBOX_ITEM_ID) <= 0) {
            return false;
        }

        Position blockade = CastleWarsEngineeringManager.findBarricadeBlockingBotRoute(
                bot, destination);
        if (blockade == null) {
            return false;
        }
        if (CastleWarsEngineeringManager.isBarricadeBurning(blockade)
                || CastleWarsEngineeringManager.igniteBarricade(bot, blockade)) {
            state.waitingForFire = blockade;
            bot.getMovementQueue().clear();
            bot.getMovementQueue().clearMovementActions();
            return true;
        }
        return false;
    }

    private static final class BlockedRoute {
        private Position destination;
        private Position lastPosition;
        private int stalledAttempts;
        private Position waitingForFire;
    }
}
