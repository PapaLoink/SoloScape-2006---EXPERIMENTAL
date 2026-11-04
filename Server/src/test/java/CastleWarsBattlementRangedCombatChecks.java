import com.rs2.cache.InterfaceDefinition;
import com.rs2.model.EntityTargetMovement;
import com.rs2.model.Position;
import com.rs2.model.combat.CombatType;
import com.rs2.model.gameplay.castlewars.CastleWarsManager;
import com.rs2.model.objects.ObjectDefinition;
import com.rs2.model.player.Player;
import com.rs2.model.skill.magic.SpellDefinition;
import com.rs2.util.GameUtil;
import com.rs2.util.path.WalkingCollisionMap;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;

/** Regression checks for ranged and magic attacks against Castle Wars battlements. */
public final class CastleWarsBattlementRangedCombatChecks {
    @SuppressWarnings("unchecked")
    public static void main(String[] args) throws Exception {
        ObjectDefinition.loadRevision443();
        WalkingCollisionMap.loadCollisionMaps();

        Field field = CastleWarsManager.class.getDeclaredField("gamePlayers");
        field.setAccessible(true);
        Map<Player, CastleWarsManager.Team> teams =
                (Map<Player, CastleWarsManager.Team>) field.get(null);
        Player wallPlayer = new Player(null);
        Player groundPlayer = new Player(null);
        wallPlayer.isBot = true;
        try {
            checkInsideCastle(CastleWarsManager.Team.SARADOMIN,
                    CastleWarsManager.Team.ZAMORAK, wallPlayer, groundPlayer, teams);
            checkInsideCastle(CastleWarsManager.Team.ZAMORAK,
                    CastleWarsManager.Team.SARADOMIN, wallPlayer, groundPlayer, teams);
            checkOuterEdgeOffset();
            System.out.println("Castle Wars battlement ranged and magic combat checks passed");
        } finally {
            teams.remove(wallPlayer);
            teams.remove(groundPlayer);
        }
    }

    private static void checkInsideCastle(CastleWarsManager.Team castleTeam,
                                          CastleWarsManager.Team enemyTeam,
                                          Player wall, Player ground,
                                          Map<Player, CastleWarsManager.Team> teams) {
        Position[] pair = findWallAndInsideGround(castleTeam);
        require(pair != null, "No valid wall/inner ground fixture for " + castleTeam);
        wall.setPosition(pair[0]);
        ground.setPosition(pair[1]);
        wall.botEnabled = true;
        wall.botPrimaryCombatStyle = 1;
        wall.botActiveCombatStyle = 1;
        ground.botEnabled = false;
        teams.put(wall, enemyTeam); // Opponent climbed the castle wall.
        teams.put(ground, castleTeam);

        require(CastleWarsManager.getCastleTeamAtPosition(ground.getPosition())
                == castleTeam, "Fixture must put the defender inside its own castle");
        require(CastleWarsManager.isCastleWallCrossLevelPair(wall, ground),
                "A rope climber must be targetable from inside the same castle");
        require(CastleWarsManager.isCastleWallCrossLevelPair(ground, wall),
                "The wall/ground distinction must work in both attack directions");
        for (CombatType type : new CombatType[]{CombatType.RANGED, CombatType.MAGIC}) {
            require(CastleWarsManager.canAttackAcrossCastleLevels(ground, wall, type),
                    "Ground defender must " + type + " a climber on the wall");
            require(CastleWarsManager.canAttackAcrossCastleLevels(wall, ground, type),
                    "Wall climber must " + type + " the defender below");
            require(EntityTargetMovement.canReachTarget(ground, wall, 10, type),
                    "Ground defender's " + type + " attack must reach wall");
            require(EntityTargetMovement.canReachTarget(wall, ground, 10, type),
                    "Wall climber's " + type + " attack must reach ground");
        }
        require(CastleWarsManager.hasBotCombatLineOfSight(wall, ground),
                "A ranged wall bot must recognise a defender below as visible");
        ground.setQueuedCombatSpell(SpellDefinition.WIND_STRIKE);
        try {
            require(EntityTargetMovement.canReachTarget(ground, wall, 10),
                    "A human casting magic must not try to path through the battlement");
        } finally {
            ground.setQueuedCombatSpell(null);
        }

        require(!CastleWarsManager.canAttackAcrossCastleLevels(
                        ground, wall, CombatType.MELEE),
                "Ranged fix must not allow melee through battlements");
        require(!EntityTargetMovement.canReachTarget(
                        ground, wall, 1, CombatType.MELEE),
                "Melee still needs a physically adjacent walkable tile");
        teams.put(ground, enemyTeam);
        require(!CastleWarsManager.isCastleWallCrossLevelPair(wall, ground),
                "Teammates must not qualify for wall-versus-ground PvP");
        teams.put(ground, castleTeam);
        ground.setPosition(new Position(pair[1].getX(), pair[1].getY(), 1));
        require(!CastleWarsManager.isCastleWallCrossLevelPair(wall, ground),
                "Wall override must not permit attacking between actual map planes");
    }

    private static Position[] findWallAndInsideGround(CastleWarsManager.Team team) {
        int minX = team == CastleWarsManager.Team.SARADOMIN ? 2412 : 2368;
        int maxX = team == CastleWarsManager.Team.SARADOMIN ? 2431 : 2387;
        int minY = team == CastleWarsManager.Team.SARADOMIN ? 3072 : 3117;
        int maxY = team == CastleWarsManager.Team.SARADOMIN ? 3089 : 3135;
        for (int x = minX; x <= maxX; ++x) {
            for (int y = minY; y <= maxY; ++y) {
                Position wall = new Position(x, y, 0);
                if (!CastleWarsManager.isCastleBattlementFiringPosition(wall)) {
                    continue;
                }
                for (int dx = -8; dx <= 8; ++dx) {
                    for (int dy = -8; dy <= 8; ++dy) {
                        Position ground = new Position(x + dx, y + dy, 0);
                        int distance = GameUtil.getDistance(wall, ground);
                        if (distance < 3 || distance > 8
                                || CastleWarsManager.getCastleTeamAtPosition(ground) != team
                                || CastleWarsManager.isCastleBattlementFiringPosition(ground)
                                || (WalkingCollisionMap.getTileFlags(
                                        ground.getX(), ground.getY(), 0) & 0x1280100) != 0) {
                            continue;
                        }
                        return new Position[]{wall, ground};
                    }
                }
            }
        }
        return null;
    }

    private static void checkOuterEdgeOffset() throws Exception {
        Method method = CastleWarsManager.class.getDeclaredMethod(
                "isFacingOutFromBattlement", Position.class, Position.class);
        method.setAccessible(true);
        require((Boolean) method.invoke(null,
                        new Position(2417, 3077, 0), new Position(2411, 3077, 0)),
                "Saradomin west wall must face outward even when two tiles inset");
        require((Boolean) method.invoke(null,
                        new Position(2382, 3130, 0), new Position(2389, 3130, 0)),
                "Zamorak east wall must face outward even when two tiles inset");
        require(!(Boolean) method.invoke(null,
                        new Position(2417, 3077, 0), new Position(2426, 3077, 0)),
                "An inset west battlement must not claim to face across the keep");
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
