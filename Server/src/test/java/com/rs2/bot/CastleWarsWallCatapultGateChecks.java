package com.rs2.bot;

import com.rs2.model.EntityTargetMovement;
import com.rs2.model.Position;
import com.rs2.model.gameplay.castlewars.CastleWarsEngineeringManager;
import com.rs2.model.gameplay.castlewars.CastleWarsManager;
import com.rs2.model.objects.ObjectDefinition;
import com.rs2.util.GameUtil;
import com.rs2.util.path.WalkingCollisionMap;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Set;

/** Castle Wars wall-side melee, gate throughput and catapult AI regressions. */
public final class CastleWarsWallCatapultGateChecks {
    private CastleWarsWallCatapultGateChecks() {
    }

    public static void main(String[] args) throws Exception {
        ObjectDefinition.loadRevision443();
        WalkingCollisionMap.loadCollisionMaps();

        checkCatapult(CastleWarsManager.Team.SARADOMIN,
                new Position(2417, 3077, 0), new Position(2415, 3087, 0),
                CastleWarsEngineeringManager.SARADOMIN_CATAPULT);
        checkCatapult(CastleWarsManager.Team.ZAMORAK,
                new Position(2382, 3130, 0), new Position(2382, 3118, 0),
                CastleWarsEngineeringManager.ZAMORAK_CATAPULT);

        require(CastleWarsBotRoleAi.wallGuardCapacity(4) == 2,
                "Small teams should reserve defender and attacker");
        for (int bots = 5; bots <= 8; ++bots) {
            int walls = CastleWarsBotRoleAi.wallGuardCapacity(bots);
            int specialists = CastleWarsBotRoleAi.specialistCapacity(bots - walls);
            require(specialists >= 3,
                    "A team with " + bots + " bots needs a catapult specialist");
            require(bots - walls - specialists >= 1,
                    "Keep at least one flag runner with " + bots + " bots");
        }

        checkDoorPassableState();
        checkAdjacentMeleeCollision();
        System.out.println("Castle Wars wall melee, gate throughput and catapult checks passed");
        System.exit(0);
    }

    private static void checkCatapult(CastleWarsManager.Team team,
                                      Position wallLanding, Position preferred,
                                      Position catapult) {
        Position operating = CastleWarsBotRoleAi.findCatapultBotOperatingPosition(
                team, wallLanding, tile -> hasWalkableRoute(wallLanding, tile, team, catapult));
        require(operating != null,
                "No valid firing post for " + team + " catapult from wall stairs");
        require(CastleWarsBotRoleAi.canOperateCatapultFrom(operating, team),
                "Selected post outside the allowed cannon firing range");
        require(GameUtil.getDistance(operating, catapult) <= 3,
                "Catapult needs a post within three tiles of its object");
        require(hasWalkableRoute(wallLanding, operating, team, catapult),
                "Catapult post is not reachable from its wall-access stairway: "
                        + operating);
        require(operating.getPlane() == 0
                        && CastleWarsManager.getCastleTeamAtPosition(operating) == team,
                "Catapult bots must remain inside their own castle's upper walkway");
        require((WalkingCollisionMap.getTileFlags(
                operating.getX(), operating.getY(), 0) & 0x1280100) == 0,
                "Catapult post has blocked movement clipping");
        require(!CastleWarsBotRoleAi.canOperateCatapultFrom(
                new Position(catapult.getX(), catapult.getY(), 1), team),
                "Operators must not fire from a different level");
        require(!CastleWarsBotRoleAi.canOperateCatapultFrom(null, team),
                "Null position cannot operate a catapult");
        Position alternate = CastleWarsBotRoleAi.findCatapultBotOperatingPosition(
                team, wallLanding, tile -> !tile.equals(operating)
                        && hasWalkableRoute(wallLanding, tile, team, catapult));
        require(alternate != null && !alternate.equals(operating),
                "Blocked catapult post should fall back to another reachable tile");
        System.out.println(team + " catapult post " + operating
                + " (old fixed post " + preferred + ")");
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static void checkDoorPassableState() throws Exception {
        Field sara = CastleWarsEngineeringManager.class.getDeclaredField("saradominMainDoor");
        Field zam = CastleWarsEngineeringManager.class.getDeclaredField("zamorakMainDoor");
        sara.setAccessible(true);
        zam.setAccessible(true);
        checkDoor(CastleWarsManager.Team.SARADOMIN, sara.get(null));
        checkDoor(CastleWarsManager.Team.ZAMORAK, zam.get(null));
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static void checkDoor(CastleWarsManager.Team team, Object door)
            throws Exception {
        Field mode = door.getClass().getDeclaredField("mode");
        mode.setAccessible(true);
        Object old = mode.get(door);
        Class<Enum> enumType = (Class<Enum>) mode.getType();
        try {
            mode.set(door, Enum.valueOf(enumType, "CLOSED"));
            require(!CastleWarsEngineeringManager.isMainDoorPassable(team),
                    "Closed doors must still stop a bot for interaction");
            mode.set(door, Enum.valueOf(enumType, "OPEN"));
            require(CastleWarsEngineeringManager.isMainDoorPassable(team),
                    "Open doors should be traversed without pause");
            mode.set(door, Enum.valueOf(enumType, "BROKEN"));
            require(CastleWarsEngineeringManager.isMainDoorPassable(team),
                    "Destroyed doors should be traversed without pause");
        } finally {
            mode.set(door, old);
        }
    }

    private static void checkAdjacentMeleeCollision() throws Exception {
        Field lookup = WalkingCollisionMap.class.getDeclaredField("regionLookup");
        lookup.setAccessible(true);
        WalkingCollisionMap[] old = (WalkingCollisionMap[]) lookup.get(null);
        Constructor<WalkingCollisionMap> construct =
                WalkingCollisionMap.class.getDeclaredConstructor(int.class);
        construct.setAccessible(true);
        int region = (37 << 8) | 48;
        WalkingCollisionMap[] fake = new WalkingCollisionMap[65536];
        fake[region] = construct.newInstance(region);
        lookup.set(null, fake);
        try {
            Position sameSide = new Position(2426, 3087, 0);
            Position beside = new Position(2427, 3087, 0);
            Position acrossGate = new Position(2426, 3088, 0);
            require(EntityTargetMovement.hasWalkableAdjacentCastleMeleeReach(
                    sameSide, beside, 1),
                    "Two connected fighters next to a wall should be able to melee");
            require(EntityTargetMovement.hasWalkableAdjacentCastleMeleeReach(
                    sameSide, acrossGate, 1),
                    "Test begins with a passable gate edge");
            WalkingCollisionMap.setCastleWarsGateEdge(2426, 3088, 0, 3, true);
            require(!EntityTargetMovement.hasWalkableAdjacentCastleMeleeReach(
                    sameSide, acrossGate, 1),
                    "A closed wall/door edge must still block melee");
            require(!EntityTargetMovement.hasWalkableAdjacentCastleMeleeReach(
                    sameSide, new Position(2427, 3088, 0), 1),
                    "Melee must not attack diagonally through a wall corner");
            require(!EntityTargetMovement.hasWalkableAdjacentCastleMeleeReach(
                    sameSide, new Position(2426, 3089, 0), 2),
                    "Melee cannot use long-range or two-tile wall shortcuts");
        } finally {
            lookup.set(null, old);
        }
    }

    /** Check real one-tile movement, including turns around wall scenery. */
    private static boolean hasWalkableRoute(Position from, Position to,
            CastleWarsManager.Team team, Position catapult) {
        ArrayDeque<Position> pending = new ArrayDeque<Position>();
        Set<String> visited = new HashSet<String>();
        pending.add(from);
        while (!pending.isEmpty()) {
            Position current = pending.poll();
            String key = current.getX() + ":" + current.getY();
            if (!visited.add(key)) {
                continue;
            }
            if (current.equals(to)) {
                return true;
            }
            for (int dx = -1; dx <= 1; ++dx) {
                for (int dy = -1; dy <= 1; ++dy) {
                    if (dx == 0 && dy == 0) {
                        continue;
                    }
                    int x = current.getX() + dx;
                    int y = current.getY() + dy;
                    Position next = new Position(x, y, 0);
                    if (Math.abs(x - catapult.getX()) > 15
                            || Math.abs(y - catapult.getY()) > 20
                            || CastleWarsManager.getCastleTeamAtPosition(next) != team
                            || (WalkingCollisionMap.getTileFlags(x, y, 0) & 0x1280100) != 0
                            || !WalkingCollisionMap.canTravelBetween(
                                    current.getX(), current.getY(), x, y, 0, 1, 1)) {
                        continue;
                    }
                    String nextKey = x + ":" + y;
                    if (!visited.contains(nextKey)) {
                        pending.add(next);
                    }
                }
            }
        }
        return false;
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}
