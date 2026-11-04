import com.rs2.model.EntityTargetMovement;
import com.rs2.model.Position;
import com.rs2.model.World;
import com.rs2.model.combat.CombatType;
import com.rs2.model.gameplay.castlewars.CastleWarsManager;
import com.rs2.model.player.Player;
import com.rs2.model.skill.magic.SpellDefinition;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;

/** Regression: Castle Wars battlements and the lower ground share map plane 0. */
public final class CastleWarsBattlementRangedCombatChecks {
    @SuppressWarnings("unchecked")
    public static void main(String[] args) throws Exception {
        Field field = CastleWarsManager.class.getDeclaredField("gamePlayers");
        field.setAccessible(true);
        Map<Player, CastleWarsManager.Team> teams =
                (Map<Player, CastleWarsManager.Team>) field.get(null);

        Player climber = new Player(null);
        Player defender = new Player(null);
        // Uninitialized test players have size zero; combat reach uses
        // bounding rectangles and must receive normal one-tile player sizes.
        climber.setSize(1);
        defender.setSize(1);
        climber.isBot = true;
        // Real player entities have a one-tile hitbox; an uninitialised test
        // Player defaults to size zero and fails the Rectangle reach check.
        climber.setSize(1);
        defender.setSize(1);
        // CastleWarsManager.getGameTeam() removes players that are not in the
        // active World registry, so register both test participants.
        climber.setIndex(75);
        climber.setEncodedIndex(32768 + 75);
        defender.setIndex(76);
        defender.setEncodedIndex(32768 + 76);
        World.players[75] = climber;
        World.players[76] = defender;
        try {
            checkCastle(CastleWarsManager.Team.SARADOMIN,
                    CastleWarsManager.Team.ZAMORAK, climber, defender, teams,
                    new Position(2416, 3074, 0), new Position(2417, 3077, 0),
                    new Position(2424, 3077, 0));
            checkCastle(CastleWarsManager.Team.ZAMORAK,
                    CastleWarsManager.Team.SARADOMIN, climber, defender, teams,
                    new Position(2383, 3133, 0), new Position(2382, 3130, 0),
                    new Position(2375, 3130, 0));
            checkOuterEdgeOffset();
            System.out.println("Castle Wars battlement ranged and magic combat checks passed");
        } finally {
            teams.remove(climber);
            teams.remove(defender);
            World.players[75] = null;
            World.players[76] = null;
            climber.setIndex(-1);
            defender.setIndex(-1);
        }
    }

    private static void checkCastle(CastleWarsManager.Team castleTeam,
                                    CastleWarsManager.Team climbingTeam,
                                    Player climber, Player defender,
                                    Map<Player, CastleWarsManager.Team> teams,
                                    Position stairBase, Position wallLanding,
                                    Position groundPosition) {
        teams.put(climber, climbingTeam);
        teams.put(defender, castleTeam);
        climber.setPosition(stairBase.copy());
        climber.castleWarsOnBattlements = false;
        defender.setPosition(groundPosition.copy());
        defender.castleWarsOnBattlements = false;

        // Actual same-plane stair traversal is authoritative even if the
        // 443 server's battlement scenery object index is not populated.
        require(CastleWarsManager.moveBotThroughGroundCastleStairs(
                        climber, castleTeam, true), "Could not climb wall stairs");
        require(wallLanding.equals(climber.getPosition()),
                "Wall stair landing must be the raised walkway");
        require(climber.castleWarsOnBattlements,
                "Climbing wall stairs must mark the player as elevated");
        require(CastleWarsManager.getCastleTeamAtPosition(defender.getPosition())
                        == castleTeam,
                "Defender fixture must stand on ground inside the same castle");

        require(CastleWarsManager.isCastleWallCrossLevelPair(climber, defender),
                "Rope/stair climber must be targetable from the ground inside castle");
        require(CastleWarsManager.isCastleWallCrossLevelPair(defender, climber),
                "Height-aware target selection must work in either direction");

        for (CombatType type : new CombatType[]{CombatType.RANGED, CombatType.MAGIC}) {
            require(CastleWarsManager.canAttackAcrossCastleLevels(defender, climber, type),
                    "Ground defender must attack wall climber with " + type);
            require(CastleWarsManager.canAttackAcrossCastleLevels(climber, defender, type),
                    "Climber must attack ground defender with " + type);
            require(EntityTargetMovement.canReachTarget(defender, climber, 10, type),
                    "Ground defender's " + type + " attack must reach wall");
            require(EntityTargetMovement.canReachTarget(climber, defender, 10, type),
                    "Climber's " + type + " attack must reach ground");
        }

        climber.botEnabled = true;
        climber.botPrimaryCombatStyle = 1;
        climber.botActiveCombatStyle = 1;
        require(CastleWarsManager.hasBotCombatLineOfSight(climber, defender),
                "Ranged wall bot must see ground defender even through scenery");

        defender.setQueuedCombatSpell(SpellDefinition.WIND_STRIKE);
        try {
            require(EntityTargetMovement.canReachTarget(defender, climber, 10),
                    "A human casting magic must not try to chase through the wall");
        } finally {
            defender.setQueuedCombatSpell(null);
        }
        require(!CastleWarsManager.canAttackAcrossCastleLevels(
                        defender, climber, CombatType.MELEE),
                "Melee must not go through a battlement");
        require(!EntityTargetMovement.canReachTarget(
                        defender, climber, 1, CombatType.MELEE),
                "Ground defender must not melee a distant wall climber");

        teams.put(defender, climbingTeam);
        require(!CastleWarsManager.isCastleWallCrossLevelPair(climber, defender),
                "Teammates must not be treated as battlement PvP opponents");
        teams.put(defender, castleTeam);

        defender.setPosition(new Position(groundPosition.getX(),
                groundPosition.getY(), 1));
        require(!CastleWarsManager.isCastleWallCrossLevelPair(climber, defender),
                "Wall override must not permit combat across different map planes");
        defender.setPosition(groundPosition.copy());

        require(CastleWarsManager.moveBotThroughGroundCastleStairs(
                        climber, castleTeam, false), "Could not descend wall stairs");
        require(stairBase.equals(climber.getPosition()),
                "Descending wall stairs must return to ground");
        require(!climber.castleWarsOnBattlements,
                "Descending must clear the elevated wall state");
        require(!CastleWarsManager.isCastleWallCrossLevelPair(climber, defender),
                "After descending the attacker must not be treated as on the wall");

        climber.castleWarsOnBattlements = true;
        climber.setPosition(new Position(stairBase.getX(), stairBase.getY(), 1));
        require(!CastleWarsManager.isCastleWallCrossLevelPair(climber, defender),
                "Stale battlement state must never bypass a real plane difference");
        climber.castleWarsOnBattlements = false;
        climber.botEnabled = false;
    }

    private static void checkOuterEdgeOffset() throws Exception {
        Method facing = CastleWarsManager.class.getDeclaredMethod(
                "isFacingOutFromBattlement", Position.class, Position.class);
        facing.setAccessible(true);
        require((Boolean) facing.invoke(null,
                        new Position(2417, 3077, 0), new Position(2411, 3077, 0)),
                "Saradomin wall must face outward from an inset walkway");
        require((Boolean) facing.invoke(null,
                        new Position(2382, 3130, 0), new Position(2389, 3130, 0)),
                "Zamorak wall must face outward from an inset walkway");
        require(!(Boolean) facing.invoke(null,
                        new Position(2417, 3077, 0), new Position(2426, 3077, 0)),
                "An inward target is not outside the west wall");
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
