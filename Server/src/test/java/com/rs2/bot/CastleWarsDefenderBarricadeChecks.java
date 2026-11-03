package com.rs2.bot;

import com.rs2.model.Position;
import com.rs2.model.gameplay.castlewars.CastleWarsEngineeringManager;
import com.rs2.model.gameplay.castlewars.CastleWarsManager;
import com.rs2.model.player.Player;
import com.rs2.model.objects.ObjectDefinition;
import com.rs2.util.path.WalkingCollisionMap;
import com.rs2.util.GameUtil;
import java.util.HashSet;
import java.util.Set;

/**
 * Verify dedicated defenders have non-overlapping, strategically ordered
 * barricade posts that don't seal main-gate lanes, stairways or flag stands.
 */
public final class CastleWarsDefenderBarricadeChecks {
    private CastleWarsDefenderBarricadeChecks() {
    }

    public static void main(String[] args) throws Exception {
        ObjectDefinition.loadRevision443();
        WalkingCollisionMap.loadCollisionMaps();
        check(CastleWarsManager.Team.SARADOMIN, new Position(2429, 3074, 3),
                new Position(2426, 3088, 0), new Position(2427, 3088, 0));
        check(CastleWarsManager.Team.ZAMORAK, new Position(2370, 3133, 3),
                new Position(2372, 3119, 0), new Position(2373, 3119, 0));

        // With only a few players, wall guards must not consume every bot:
        // teams need both a dedicated defender and a flag runner.
        require(CastleWarsBotRoleAi.wallGuardCapacity(2) == 0,
                "A two-bot team must reserve both its defender and flag runner");
        require(CastleWarsBotRoleAi.wallGuardCapacity(4) == 2,
                "A four-bot team must not use all ranged players as wall guards");
        require(CastleWarsBotRoleAi.wallGuardCapacity(8) == 4,
                "Larger teams can retain their four wall specialists");
        require(CastleWarsBotRoleAi.specialistCapacity(2) == 1,
                "Two remaining bots need one defender and one flag runner");
        require(CastleWarsBotRoleAi.specialistCapacity(4) == 3,
                "Four remaining bots should retain an attacking flag runner");

        require(CastleWarsBotRoleAi.getDefenderBarricadeSites(null, 0).length == 0,
                "A defender without a team must not place barricades");
        require(CastleWarsBotRoleAi.getDefenderBarricadeSites(
                CastleWarsManager.Team.SARADOMIN, 2).length == 0,
                "Only the two assigned defenders should get the planned posts");

        // Normal players still use the usual on-self barricade placement API.
        // The adjacent-target API is deliberately reserved for bots.
        Player human = new Player(null);
        require(!CastleWarsEngineeringManager.placeBarricadeForBot(
                human, new Position(2425, 3085, 0)),
                "Human players must not be allowed remote barricade placement");

        System.out.println("Castle Wars defender strategic barricade checks passed");
        System.exit(0);
    }

    private static void check(CastleWarsManager.Team team, Position flag,
                              Position firstDoor, Position secondDoor) {
        Set<String> all = new HashSet<String>();
        for (int defender = 0; defender < 2; defender++) {
            Position[] sites = CastleWarsBotRoleAi.getDefenderBarricadeSites(team, defender);
            require(sites.length == 3, "Each defender needs two ground posts and one flag post");
            for (int index = 0; index < sites.length; index++) {
                Position site = sites[index];
                require(site.getPlane() == (index < 2 ? 0 : 3),
                        "Barricades must cover entrance before upper flag room");
                require(CastleWarsManager.getCastleTeamAtPosition(site) == team,
                        "Defender's barricade must stay within its team's castle");
                require(!CastleWarsManager.isGroundCastleExteriorTransitionTile(site),
                        "Do not block the castle's main-door transition");
                require(!site.equals(firstDoor) && !site.equals(secondDoor),
                        "Do not place a barricade on the main gate");
                require((WalkingCollisionMap.getTileFlags(
                        site.getX(), site.getY(), site.getPlane()) & 0x1280100) == 0,
                        "Barricade location is blocked by map scenery: " + keyOf(site));
                require(!site.equals(flag), "Do not place a barricade on the flag");
                if (index == 2) {
                    require(GameUtil.getDistance(site, flag) >= 2,
                            "Keep space for players to steal or return the flag");
                } else {
                    require(GameUtil.getDistance(site, firstDoor) >= 2
                                    && GameUtil.getDistance(site, firstDoor) <= 8,
                            "Ground posts must defend the gate but leave its approach open");
                }
                String key = keyOf(site);
                require(all.add(key), "Defenders must not compete for the same tile: " + key);
            }
        }
        require(all.size() == 6, "Expected six distinct defensive barricade locations");
    }

    private static String keyOf(Position site) {
        return site.getX() + ":" + site.getY() + ":" + site.getPlane();
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}
