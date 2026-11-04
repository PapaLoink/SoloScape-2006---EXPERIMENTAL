import com.rs2.model.Position;
import com.rs2.model.gameplay.castlewars.CastleWarsManager;
import com.rs2.model.gameplay.castlewars.CastleWarsManager.Team;
import com.rs2.model.player.Player;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;

/**
 * Verify the real Castle Wars barrier crossing tiles in both directions.
 * Invoke the barrier logic directly: ordinary object dispatch boots the
 * entire minigame (including NPCs) and is not needed for this unit test.
 */
public final class CastleWarsEnergyBarrierReturnChecks {
    @SuppressWarnings("unchecked")
    public static void main(String[] args) throws Exception {
        Field field = CastleWarsManager.class.getDeclaredField("gamePlayers");
        field.setAccessible(true);
        Map<Player, Team> teams = (Map<Player, Team>) field.get(null);
        Method pass = CastleWarsManager.class.getDeclaredMethod(
                "passEnergyBarrier", Player.class, Team.class, int.class, int.class);
        pass.setAccessible(true);

        Player player = new Player(null);
        player.isBot = true;
        player.setSize(1);
        try {
            teams.put(player, Team.SARADOMIN);
            // The clicked north-barrier object's Y is not necessarily the
            // same as the 3080/3081 movement tiles in all cache revisions.
            for (int objectY : new int[]{3079, 3080, 3081}) {
                checkRoundTrip(pass, player, CastleWarsManager.SARADOMIN_ENERGY_BARRIER_ID,
                        Team.SARADOMIN, 2426, objectY,
                        new Position(2426, 3080, 1), new Position(2426, 3081, 1));
            }
            checkRoundTrip(pass, player, CastleWarsManager.SARADOMIN_ENERGY_BARRIER_ID,
                    Team.SARADOMIN, 2422, 3076,
                    new Position(2423, 3076, 1), new Position(2422, 3076, 1));

            player.setPosition(new Position(2426, 3078, 1));
            require(new Position(2426, 3080, 1).equals(
                    CastleWarsManager.getEnergyBarrierInteractionApproach(player,
                            CastleWarsManager.SARADOMIN_ENERGY_BARRIER_ID, 2426, 3081)),
                    "North barrier must route players inside to the 3080 crossing tile");
            player.setPosition(new Position(2426, 3083, 1));
            require(new Position(2426, 3081, 1).equals(
                    CastleWarsManager.getEnergyBarrierInteractionApproach(player,
                            CastleWarsManager.SARADOMIN_ENERGY_BARRIER_ID, 2426, 3079)),
                    "North barrier must route returning players to the 3081 crossing tile");

            teams.put(player, Team.ZAMORAK);
            checkRoundTrip(pass, player, CastleWarsManager.ZAMORAK_ENERGY_BARRIER_ID,
                    Team.ZAMORAK, 2373, 3126,
                    new Position(2373, 3127, 1), new Position(2373, 3126, 1));
            checkRoundTrip(pass, player, CastleWarsManager.ZAMORAK_ENERGY_BARRIER_ID,
                    Team.ZAMORAK, 2377, 3131,
                    new Position(2376, 3131, 1), new Position(2377, 3131, 1));

            // A player may not cross the enemy team's spawn-room barrier.
            player.setPosition(new Position(2426, 3081, 1));
            pass.invoke(null, player, Team.SARADOMIN, 2426, 3081);
            require(new Position(2426, 3081, 1).equals(player.getPosition()),
                    "Zamorak player must not enter Saradomin spawn");
            teams.put(player, Team.SARADOMIN);
            player.setPosition(new Position(2377, 3131, 1));
            pass.invoke(null, player, Team.ZAMORAK, 2377, 3131);
            require(new Position(2377, 3131, 1).equals(player.getPosition()),
                    "Saradomin player must not enter Zamorak spawn");

            // Incorrect floor or approach must not teleport through the barrier.
            player.setPosition(new Position(2426, 3080, 0));
            pass.invoke(null, player, Team.SARADOMIN, 2426, 3081);
            require(new Position(2426, 3080, 0).equals(player.getPosition()),
                    "Barrier cannot be used across different floors");
            require(CastleWarsManager.getEnergyBarrierInteractionApproach(player,
                    CastleWarsManager.SARADOMIN_ENERGY_BARRIER_ID, 2426, 3081) == null,
                    "Wrong-floor barrier route must be ignored");
            player.setPosition(new Position(2426, 3078, 1));
            pass.invoke(null, player, Team.SARADOMIN, 2426, 3081);
            require(new Position(2426, 3078, 1).equals(player.getPosition()),
                    "North barrier cannot teleport players from distant tiles");

            System.out.println("Castle Wars energy barrier re-entry checks passed");
        } finally {
            teams.remove(player);
        }
    }

    private static void checkRoundTrip(Method pass, Player player, int objectId,
                                       Team team, int objectX, int objectY,
                                       Position inside, Position outside) throws Exception {
        player.setPosition(inside.copy());
        require(inside.equals(CastleWarsManager.getEnergyBarrierInteractionApproach(
                player, objectId, objectX, objectY)), "Wrong inside approach: " + objectId);
        pass.invoke(null, player, team, objectX, objectY);
        require(outside.equals(player.getPosition()), "Failed to leave spawn: " + objectId);
        require(outside.equals(CastleWarsManager.getEnergyBarrierInteractionApproach(
                player, objectId, objectX, objectY)), "Wrong return approach: " + objectId);
        pass.invoke(null, player, team, objectX, objectY);
        require(inside.equals(player.getPosition()), "Failed to return to spawn: " + objectId);
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
