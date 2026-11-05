import com.rs2.model.EntityTargetMovement;
import com.rs2.model.Position;
import com.rs2.model.World;
import com.rs2.model.combat.CombatType;
import com.rs2.model.gameplay.castlewars.CastleWarsManager;
import com.rs2.model.player.Player;
import com.rs2.model.skill.magic.SpellDefinition;
import com.rs2.util.GameUtil;
import com.rs2.util.path.ProjectileCollisionMap;
import com.rs2.util.path.WalkingCollisionMap;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;

/** Indoor walls must block both AI targeting and combat-cycle projectile reach. */
public final class CastleWarsIndoorLineOfSightChecks {
    @SuppressWarnings("unchecked")
    public static void main(String[] args) throws Exception {
        Map<Player, CastleWarsManager.Team> teams = (Map<Player, CastleWarsManager.Team>)
                field(CastleWarsManager.class, "gamePlayers").get(null);
        Field regions = field(ProjectileCollisionMap.class, "regions");
        Object previous = regions.get(null);
        Constructor<ProjectileCollisionMap> constructor = ProjectileCollisionMap.class.getDeclaredConstructor(int.class);
        constructor.setAccessible(true);
        Method setFlag = ProjectileCollisionMap.class.getDeclaredMethod("setTileFlag", int.class, int.class, int.class, int.class);
        setFlag.setAccessible(true);
        Field walkingRegions = field(WalkingCollisionMap.class, "regionLookup");
        Object previousWalking = walkingRegions.get(null);
        Constructor<WalkingCollisionMap> walkingConstructor =
                WalkingCollisionMap.class.getDeclaredConstructor(int.class);
        walkingConstructor.setAccessible(true);
        Method setWalkingFlag = WalkingCollisionMap.class.getDeclaredMethod(
                "setTileFlag", int.class, int.class, int.class, int.class);
        setWalkingFlag.setAccessible(true);
        Player mage = new Player(null);
        Player target = new Player(null);
        mage.isBot = true;
        mage.botEnabled = true;
        mage.botPrimaryCombatStyle = 2;
        mage.botActiveCombatStyle = 2;
        mage.castleWarsOnBattlements = true;
        mage.setQueuedCombatSpell(SpellDefinition.WIND_STRIKE);
        mage.setSize(1);
        target.setSize(1);
        mage.setIndex(75);
        mage.setEncodedIndex(32768 + 75);
        target.setIndex(76);
        target.setEncodedIndex(32768 + 76);
        if (World.players[75] != null || World.players[76] != null) throw new AssertionError("Fixture slots occupied");
        World.players[75] = mage;
        World.players[76] = target;
        try {
            for (CastleWarsManager.Team castle : CastleWarsManager.Team.values()) {
                regions.set(null, new ProjectileCollisionMap[]{constructor.newInstance((37 << 8) | 48)});
                WalkingCollisionMap[] walkingLookup = new WalkingCollisionMap[65536];
                int regionId = (37 << 8) | 48;
                walkingLookup[regionId] = walkingConstructor.newInstance(regionId);
                walkingRegions.set(null, walkingLookup);
                teams.put(mage, castle == CastleWarsManager.Team.SARADOMIN
                        ? CastleWarsManager.Team.ZAMORAK : CastleWarsManager.Team.SARADOMIN);
                teams.put(target, castle);
                int mageX = castle == CastleWarsManager.Team.SARADOMIN ? 2417 : 2382;
                int targetX = castle == CastleWarsManager.Team.SARADOMIN ? 2424 : 2375;
                int wallX = castle == CastleWarsManager.Team.SARADOMIN ? 2420 : 2379;
                int y = castle == CastleWarsManager.Team.SARADOMIN ? 3077 : 3130;
                mage.setPosition(new Position(mageX, y, 0));
                target.setPosition(new Position(targetX, y, 0));
                require(GameUtil.hasClearPath(mage.getPosition(), target.getPosition(), false), "Fixture sightline not clear");
                for (CombatType type : new CombatType[]{CombatType.MAGIC, CombatType.RANGED}) {
                    require(EntityTargetMovement.canReachTarget(mage, target, 10, type), "Open indoor sightline blocked");
                }
                setFlag.invoke(null, wallX, y, 0, 0x100);
                require(!GameUtil.hasClearPath(mage.getPosition(), target.getPosition(), false), "Fixture wall did not block projectiles");
                require(!CastleWarsManager.hasBotCombatLineOfSight(mage, target), "Mage AI sees through indoor wall");
                require(!EntityTargetMovement.canReachTarget(mage, target, 10), "Movement reach bypassed indoor wall");
                for (CombatType type : new CombatType[]{CombatType.MAGIC, CombatType.RANGED}) {
                    require(!EntityTargetMovement.canReachTarget(mage, target, 10, type), "Attack penetrated indoor wall: " + type);
                    require(!EntityTargetMovement.canReachTarget(target, mage, 10, type), "Reverse attack penetrated indoor wall: " + type);
                }
                regions.set(null, new ProjectileCollisionMap[]{constructor.newInstance((37 << 8) | 48)});
                setWalkingFlag.invoke(null, wallX, y, 0, 0x100);
                require(GameUtil.hasClearPath(mage.getPosition(), target.getPosition(), false),
                        "Walking-only fixture unexpectedly has projectile clipping");
                require(!CastleWarsManager.hasBotCombatLineOfSight(mage, target),
                        "Mage AI ignored a walking-clipped wall");
                require(!EntityTargetMovement.canReachTarget(mage, target, 10, CombatType.MAGIC),
                        "Mage cast through a walking-clipped wall");
            }
        } finally {
            regions.set(null, previous);
            walkingRegions.set(null, previousWalking);
            teams.remove(mage);
            teams.remove(target);
            World.players[75] = null;
            World.players[76] = null;
        }
        System.out.println("Castle Wars indoor line-of-sight checks passed: both castles, magic and ranged");
        System.exit(0);
    }

    private static Field field(Class<?> type, String name) throws Exception {
        Field field = type.getDeclaredField(name);
        field.setAccessible(true);
        return field;
    }

    private static void require(boolean value, String message) {
        if (!value) throw new AssertionError(message);
    }
}
