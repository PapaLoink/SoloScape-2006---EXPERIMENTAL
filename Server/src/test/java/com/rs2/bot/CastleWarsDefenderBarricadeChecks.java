package com.rs2.bot;

import com.rs2.model.Position;
import com.rs2.model.gameplay.castlewars.CastleWarsEngineeringManager;
import com.rs2.model.gameplay.castlewars.CastleWarsManager;
import com.rs2.model.objects.ObjectDefinition;
import com.rs2.model.player.Player;
import com.rs2.util.GameUtil;
import com.rs2.util.path.WalkingCollisionMap;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/** Verify tactical Castle Wars bot barricade decisions with actual 443 clipping. */
public final class CastleWarsDefenderBarricadeChecks {
    private CastleWarsDefenderBarricadeChecks() {
    }

    public static void main(String[] args) throws Exception {
        ObjectDefinition.loadRevision443();
        WalkingCollisionMap.loadCollisionMaps();
        checkTeam(CastleWarsManager.Team.SARADOMIN,
                new Position(2426, 3081, 0),
                new Position(2427, 3078, 3),
                new Position(2429, 3074, 3),
                new Position(2426, 3088, 0),
                new Position(2423, 3081, 0),
                new Position(2429, 3081, 0));
        checkTeam(CastleWarsManager.Team.ZAMORAK,
                new Position(2373, 3126, 0),
                new Position(2372, 3130, 3),
                new Position(2370, 3133, 3),
                new Position(2372, 3119, 0),
                new Position(2370, 3127, 0),
                new Position(2377, 3127, 0));

        // Simulate the first barricade existing before choosing the next.
        // This catches layouts that appear safe in isolation but leave no
        // valid spots once the first defender blocks a corridor.
        verifySequentialPlacement(CastleWarsManager.Team.SARADOMIN,
                new Position(2426, 3081, 0), new Position(2427, 3078, 3));
        verifySequentialPlacement(CastleWarsManager.Team.ZAMORAK,
                new Position(2373, 3126, 0), new Position(2372, 3130, 3));

        require(CastleWarsBotRoleAi.wallGuardCapacity(2) == 0,
                "Two bots must include a defender and a flag runner");
        require(CastleWarsBotRoleAi.wallGuardCapacity(4) == 2,
                "Small teams must reserve defence and attack roles");
        require(CastleWarsBotRoleAi.wallGuardCapacity(8) == 4,
                "Large teams retain four wall specialists");
        require(CastleWarsBotRoleAi.specialistCapacity(2) == 1,
                "Reserve a non-wall flag runner");
        require(CastleWarsBotRoleAi.chooseDefenderBarricadeSite(
                null, 0, 0, new Position(2426, 3081, 0), null, null) == null,
                "Unassigned bots must not place Castle Wars barricades");
        require(CastleWarsBotRoleAi.chooseDefenderBarricadeSite(
                CastleWarsManager.Team.SARADOMIN, 2, 0,
                new Position(2426, 3081, 0), null, null) == null,
                "Only two defenders have assigned defensive posts");

        Player human = new Player(null);
        require(!CastleWarsEngineeringManager.placeBarricadeForBot(
                human, new Position(2425, 3085, 0)),
                "Humans must not remotely place barricades");
        System.out.println("Castle Wars adaptive defender barricade checks passed");
        System.exit(0);
    }

    private static void checkTeam(CastleWarsManager.Team team,
            Position ground, Position upper, Position flag,
            Position gate, Position leftThreat, Position rightThreat) {
        Set<String> used = new HashSet<String>();
        // Defender 0: one ground gate flank and two different upstairs
        // flag-approach lines. Defender 1: gate and inner ground choke.
        Position a = choose(team, 0, 0, ground, null, used);
        Position b = choose(team, 1, 0, ground, null, used);
        Position c = choose(team, 1, 1, ground, null, used);
        Position d = choose(team, 0, 1, upper, null, used);
        Position e = choose(team, 0, 2, upper, null, used);

        for (Position post : new Position[]{a, b, c, d, e}) {
            require(CastleWarsManager.getCastleTeamAtPosition(post) == team,
                    "Barricades must defend their own castle");
            require(!CastleWarsEngineeringManager.isProtectedCastlePassage(team, post),
                    "Barricade must never block a gate, stairway, or flag interaction");
            require((WalkingCollisionMap.getTileFlags(
                    post.getX(), post.getY(), post.getPlane()) & 0x1280100) == 0,
                    "Barricade chosen on clipped tile " + key(post));
            require(!post.equals(gate) && !post.equals(flag),
                    "Do not block the large door or the flag stand");
        }
        require(a.getPlane() == 0 && b.getPlane() == 0 && c.getPlane() == 0
                        && d.getPlane() == 3 && e.getPlane() == 3,
                "Defenders must spread barricades across the ground and flag floors");
        require(GameUtil.getDistance(c, gate) > GameUtil.getDistance(a, gate),
                "Inner fallback barricade must not cluster at the main door");
        require(GameUtil.getDistance(d, flag) >= 3
                        && GameUtil.getDistance(e, flag) >= 3,
                "Keep usable space around the flag");
        require(used.size() == 5, "Defenders should use five distinct initial sites");

        // A threatened flank should move the next barricade in that direction,
        // not blindly choose the same hard-coded tile every round.
        Position left = choose(team, 1, 3, ground, leftThreat, null);
        Position right = choose(team, 1, 3, ground, rightThreat, null);
        require(GameUtil.getDistance(left, leftThreat)
                        <= GameUtil.getDistance(right, leftThreat),
                "Left-flank attack must influence placement");
        require(GameUtil.getDistance(right, rightThreat)
                        <= GameUtil.getDistance(left, rightThreat),
                "Right-flank attack must influence placement");
        require(!left.equals(right),
                "Defenders should adapt when attackers switch the entrance flank");

        Position upperReinforcement = choose(team, 0, 3, upper,
                new Position(flag.getX() - (team == CastleWarsManager.Team.SARADOMIN ? 2 : -2),
                        flag.getY() + (team == CastleWarsManager.Team.SARADOMIN ? 4 : -4),
                        3), null);
        require(upperReinforcement.getPlane() == 3,
                "Flag-room attackers should trigger an upstairs reinforcement");
        System.out.println(team + " placed initial positions " + used
                + "; enemy pressure moves post " + key(left) + " to " + key(right));
    }

    @SuppressWarnings("unchecked")
    private static void verifySequentialPlacement(CastleWarsManager.Team team,
            Position ground, Position upper) throws Exception {
        Field activeField = CastleWarsEngineeringManager.class.getDeclaredField("barricades");
        activeField.setAccessible(true);
        Map<String, Object> active = (Map<String, Object>) activeField.get(null);
        Map<String, Object> snapshot = new HashMap<String, Object>(active);
        Class<?> stateType = Class.forName(
                "com.rs2.model.gameplay.castlewars.CastleWarsEngineeringManager$BarricadeState");
        Constructor<?> constructor = stateType.getDeclaredConstructor(
                CastleWarsManager.Team.class, Position.class, int.class);
        constructor.setAccessible(true);
        try {
            Position[] positions = new Position[]{
                choose(team, 0, 0, ground, null, null),
                null, null, null, null
            };
            addFakeBarricade(active, constructor, team, positions[0]);
            positions[1] = choose(team, 1, 0, ground, null, null);
            addFakeBarricade(active, constructor, team, positions[1]);
            positions[2] = choose(team, 1, 1, ground, null, null);
            addFakeBarricade(active, constructor, team, positions[2]);
            positions[3] = choose(team, 0, 1, upper, null, null);
            addFakeBarricade(active, constructor, team, positions[3]);
            positions[4] = choose(team, 0, 2, upper, null, null);
            addFakeBarricade(active, constructor, team, positions[4]);

            for (int a = 0; a < positions.length; a++) {
                for (int b = a + 1; b < positions.length; b++) {
                    if (positions[a].getPlane() == positions[b].getPlane()) {
                        require(GameUtil.getDistance(positions[a], positions[b]) >= 3,
                                "Defenders must not cluster barricades: "
                                        + key(positions[a]) + " and " + key(positions[b]));
                    }
                }
            }
            System.out.println(team + " simulated deployed posts: "
                    + java.util.Arrays.toString(positions));
        } finally {
            active.clear();
            active.putAll(snapshot);
        }
    }

    private static void addFakeBarricade(Map<String, Object> active,
            Constructor<?> constructor, CastleWarsManager.Team team,
            Position position) throws Exception {
        active.put(key(position), constructor.newInstance(team, position,
                CastleWarsEngineeringManager.BARRICADE_OBJECT_UNLIT));
    }

    private static Position choose(CastleWarsManager.Team team, int defender,
            int stage, Position bot, Position enemy, Set<String> consumed) {
        Position result = CastleWarsBotRoleAi.chooseDefenderBarricadeSite(
                team, defender, stage, bot, enemy, consumed);
        require(result != null, "No usable " + team + " barricade position for stage " + stage);
        if (consumed != null) {
            require(consumed.add(key(result)), "Duplicate barricade position " + key(result));
        }
        return result;
    }

    private static String key(Position site) {
        return site.getX() + ":" + site.getY() + ":" + site.getPlane();
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}
