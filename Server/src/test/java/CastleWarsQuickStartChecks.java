import com.rs2.model.Position;
import com.rs2.model.World;
import com.rs2.model.gameplay.castlewars.CastleWarsManager;
import com.rs2.model.gameplay.castlewars.CastleWarsManager.Team;
import com.rs2.model.objects.ObjectDefinition;
import com.rs2.model.player.Player;
import com.rs2.util.path.WalkingCollisionMap;
import java.lang.reflect.Field;
import java.util.Map;

/** ::cwstart skips only the Castle Wars countdown, not match eligibility. */
public final class CastleWarsQuickStartChecks {
    @SuppressWarnings("unchecked")
    public static void main(String[] args) throws Exception {
        ObjectDefinition.loadRevision443();
        WalkingCollisionMap.loadCollisionMaps();

        Field waitingField = CastleWarsManager.class.getDeclaredField("waitingPlayers");
        waitingField.setAccessible(true);
        Map<Player, Team> waiting = (Map<Player, Team>) waitingField.get(null);
        Field gameField = CastleWarsManager.class.getDeclaredField("gamePlayers");
        gameField.setAccessible(true);
        Map<Player, Team> playing = (Map<Player, Team>) gameField.get(null);
        Field countdownField = CastleWarsManager.class.getDeclaredField("nextGameStartMillis");
        countdownField.setAccessible(true);
        Field endField = CastleWarsManager.class.getDeclaredField("gameEndMillis");
        endField.setAccessible(true);

        Player saradomin = new Player(null);
        Player zamorak = new Player(null);
        Player outsider = new Player(null);
        saradomin.isBot = true;
        zamorak.isBot = true;
        outsider.isBot = true;
        Player[] players = World.getPlayers();
        int[] slots = {75, 76, 77};
        Player[] participants = {saradomin, zamorak, outsider};
        Player[] original = {players[75], players[76], players[77]};
        for (int i = 0; i < participants.length; ++i) {
            participants[i].setIndex(slots[i]);
            participants[i].setEncodedIndex(32768 + slots[i]);
            participants[i].setSize(1);
            players[slots[i]] = participants[i];
        }

        try {
            saradomin.setPosition(new Position(2377, 9485, 0));
            zamorak.setPosition(new Position(2421, 9524, 0));
            outsider.setPosition(new Position(2440, 3090, 0));
            waiting.put(saradomin, Team.SARADOMIN);
            require(!CastleWarsManager.startGameImmediately(saradomin),
                    "Cannot start a game with an empty opposing team");
            require(!CastleWarsManager.isGameInProgress(),
                    "A rejected quick start must not begin the match");

            waiting.put(zamorak, Team.ZAMORAK);
            require(!CastleWarsManager.startGameImmediately(outsider),
                    "Players outside the waiting rooms must not start matches");
            require(!CastleWarsManager.isGameInProgress(),
                    "An outsider must not skip the countdown");
            countdownField.setLong(null,
                    System.currentTimeMillis() + CastleWarsManager.WAITING_DURATION_SECONDS * 1000L);

            // Exercise the actual command dispatcher as a player would.
            long startedAt = System.currentTimeMillis();
            saradomin.handleCommand("cwstart", new String[0], "");
            require(CastleWarsManager.isGameInProgress(),
                    "::cwstart should start immediately while the timer still has five minutes");
            require(playing.get(saradomin) == Team.SARADOMIN
                            && playing.get(zamorak) == Team.ZAMORAK,
                    "Both teams must enter the match through normal startup");
            require(waiting.isEmpty(), "Quick start must empty the waiting rooms");
            require(countdownField.getLong(null) == -1L,
                    "Quick start must clear the waiting countdown");

            long matchEndsAt = endField.getLong(null);
            long expectedDuration = CastleWarsManager.GAME_DURATION_SECONDS * 1000L;
            require(matchEndsAt >= startedAt + expectedDuration
                            && matchEndsAt <= System.currentTimeMillis() + expectedDuration,
                    "Quick start must preserve the normal 20-minute match duration");
            require(!CastleWarsManager.startGameImmediately(saradomin),
                    "Quick start must not restart an active match");
            require(endField.getLong(null) == matchEndsAt,
                    "An active game's timer must remain unchanged");
            System.out.println("Castle Wars ::cwstart quick-start checks passed");
        } finally {
            waiting.remove(saradomin);
            waiting.remove(zamorak);
            playing.remove(saradomin);
            playing.remove(zamorak);
            for (int i = 0; i < participants.length; ++i) {
                players[slots[i]] = original[i];
                participants[i].setIndex(-1);
            }
        }
    }

    private static void require(boolean passed, String failure) {
        if (!passed) {
            throw new AssertionError(failure);
        }
    }
}
