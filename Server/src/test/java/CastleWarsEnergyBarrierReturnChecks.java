import com.rs2.model.Position;
import com.rs2.model.gameplay.castlewars.CastleWarsManager;
import com.rs2.model.player.Player;
import java.lang.reflect.Field;
import java.util.Map;

/** Both Castle Wars energy barriers must allow their own team to return to spawn. */
public final class CastleWarsEnergyBarrierReturnChecks {
    @SuppressWarnings("unchecked")
    public static void main(String[] args) throws Exception {
        Field field = CastleWarsManager.class.getDeclaredField("gamePlayers");
        field.setAccessible(true);
        Map<Player, CastleWarsManager.Team> teams =
                (Map<Player, CastleWarsManager.Team>) field.get(null);

        Player player = new Player(null);
        player.isBot = true;
        try {
            teams.put(player, CastleWarsManager.Team.SARADOMIN);
            checkRoundTrip(player, CastleWarsManager.SARADOMIN_ENERGY_BARRIER_ID,
                    2426, 3080, new Position(2426, 3079, 1),
                    new Position(2426, 3081, 1), new Position(2426, 3080, 1));
            checkRoundTrip(player, CastleWarsManager.SARADOMIN_ENERGY_BARRIER_ID,
                    2422, 3076, new Position(2423, 3076, 1),
                    new Position(2422, 3076, 1), new Position(2423, 3076, 1));

            // Players outside the barrier must be routed to its outside face.
            player.setPosition(new Position(2426, 3083, 1));
            require(new Position(2426, 3081, 1).equals(
                    CastleWarsManager.getEnergyBarrierInteractionApproach(player,
                            CastleWarsManager.SARADOMIN_ENERGY_BARRIER_ID, 2426, 3080)),
                    "Saradomin north entrance must be approached from outside");
            player.setPosition(new Position(2420, 3076, 1));
            require(new Position(2422, 3076, 1).equals(
                    CastleWarsManager.getEnergyBarrierInteractionApproach(player,
                            CastleWarsManager.SARADOMIN_ENERGY_BARRIER_ID, 2422, 3076)),
                    "Saradomin west entrance must be approached from outside");

            teams.put(player, CastleWarsManager.Team.ZAMORAK);
            checkRoundTrip(player, CastleWarsManager.ZAMORAK_ENERGY_BARRIER_ID,
                    2373, 3126, new Position(2373, 3127, 1),
                    new Position(2373, 3126, 1), new Position(2373, 3127, 1));
            checkRoundTrip(player, CastleWarsManager.ZAMORAK_ENERGY_BARRIER_ID,
                    2377, 3131, new Position(2376, 3131, 1),
                    new Position(2377, 3131, 1), new Position(2376, 3131, 1));

            player.setPosition(new Position(2426, 3081, 1));
            CastleWarsManager.handleFirstObjectAction(player,
                    CastleWarsManager.SARADOMIN_ENERGY_BARRIER_ID, 2426, 3080);
            require(player.getPosition().equals(new Position(2426, 3081, 1)),
                    "Zamorak player must not enter Saradomin spawn");
            teams.put(player, CastleWarsManager.Team.SARADOMIN);
            player.setPosition(new Position(2377, 3131, 1));
            CastleWarsManager.handleFirstObjectAction(player,
                    CastleWarsManager.ZAMORAK_ENERGY_BARRIER_ID, 2377, 3131);
            require(player.getPosition().equals(new Position(2377, 3131, 1)),
                    "Saradomin player must not enter Zamorak spawn");

            player.setPosition(new Position(2426, 3081, 0));
            require(CastleWarsManager.getEnergyBarrierInteractionApproach(player,
                    CastleWarsManager.SARADOMIN_ENERGY_BARRIER_ID, 2426, 3080) == null,
                    "Energy barrier route must not activate on another floor");
            System.out.println("Castle Wars energy barrier re-entry checks passed");
        } finally {
            teams.remove(player);
        }
    }

    private static void checkRoundTrip(Player player, int id, int objectX, int objectY,
                                       Position inside, Position outside, Position returned) {
        player.setPosition(inside.copy());
        require(inside.equals(CastleWarsManager.getEnergyBarrierInteractionApproach(
                player, id, objectX, objectY)), "Wrong inside barrier approach: " + id);
        require(CastleWarsManager.handleFirstObjectAction(player, id, objectX, objectY),
                "Exit barrier action was not handled: " + id);
        require(outside.equals(player.getPosition()), "Exit did not cross barrier: " + id);
        require(outside.equals(CastleWarsManager.getEnergyBarrierInteractionApproach(
                player, id, objectX, objectY)), "Wrong return approach: " + id);
        require(CastleWarsManager.handleFirstObjectAction(player, id, objectX, objectY),
                "Return barrier action was not handled: " + id);
        require(returned.equals(player.getPosition()), "Return did not enter spawn: " + id);
    }

    private static void require(boolean ok, String message) {
        if (!ok) throw new AssertionError(message);
    }
}
