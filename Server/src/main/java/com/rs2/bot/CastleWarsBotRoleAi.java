package com.rs2.bot;

import com.rs2.model.Entity;
import com.rs2.model.Position;
import com.rs2.model.World;
import com.rs2.model.combat.CombatManager;
import com.rs2.model.gameplay.castlewars.CastleWarsEngineeringManager;
import com.rs2.model.gameplay.castlewars.CastleWarsManager;
import com.rs2.model.ground.GroundItem;
import com.rs2.model.item.ItemStack;
import com.rs2.model.player.Player;
import com.rs2.util.GameUtil;
import com.rs2.util.path.PathFinder;
import com.rs2.util.path.WalkingCollisionMap;
import com.rs2.util.GameplayTrace;

import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.EnumMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

public final class CastleWarsBotRoleAi {
    private static final Map<BotPlayer, RoleState> states =
            new IdentityHashMap<BotPlayer, RoleState>();
    private static BotPlayer saradominDedicatedFlagRunner;
    private static BotPlayer zamorakDedicatedFlagRunner;
    private static final Map<CastleWarsManager.Team, Map<BotPlayer, Integer>> roleRosters =
            new EnumMap<CastleWarsManager.Team, Map<BotPlayer, Integer>>(CastleWarsManager.Team.class);
    private static final Map<CastleWarsManager.Team, Integer> roleGenerations =
            new EnumMap<CastleWarsManager.Team, Integer>(CastleWarsManager.Team.class);
    private static final Map<CastleWarsManager.Team, BotPlayer> catapultOperators =
            new EnumMap<CastleWarsManager.Team, BotPlayer>(CastleWarsManager.Team.class);

    private CastleWarsBotRoleAi() {
    }

    public static void resetCatapultOperators() {
        catapultOperators.clear();
        roleRosters.clear();
    }

    public static void clear(BotPlayer bot) {
        states.remove(bot);
        CastleWarsBotBarricadeAi.clear(bot);
        if (saradominDedicatedFlagRunner == bot) {
            saradominDedicatedFlagRunner = null;
        }
        if (zamorakDedicatedFlagRunner == bot) {
            zamorakDedicatedFlagRunner = null;
        }
    }

    /**
     * @return true when this bot has a specialist role and this class owns its movement.
     *         False means the normal flag-runner AI should process it.
     */
    public static boolean process(BotPlayer bot) {
        CastleWarsManager.Team team = CastleWarsManager.getGameTeam(bot);
        if (team == null || bot.isDead() || !bot.isRegistered()) {
            states.remove(bot);
            return false;
        }

        // The reserved objective runner must use the surface flag AI, even
        // when promoted after another runner dies. Do not let a cached tunnel
        // role divert it into incidental combat instead of capturing flags.
        boolean dedicatedFlagRunner = isDedicatedFlagRunner(bot);
        boolean catapultOperator = bot == catapultOperators.get(team);
        if (bot.isAutoRetaliate() == catapultOperator) {
            bot.setAutoRetaliate(!catapultOperator);
        }
        if (dedicatedFlagRunner) {
            states.remove(bot);
            return false;
        }

        RoleState state = states.get(bot);
        if (state == null || state.team != team
                || state.primaryCombatStyle != bot.botPrimaryCombatStyle
                || state.roleGeneration != roleGenerations.get(team).intValue()
                    && (state.role != assignRole(bot, team)
                        || state.role == Role.DEFENDER && state.defenderIndex != getDefenderIndex(bot, team))) {
            state = new RoleState(bot, team, bot.botPrimaryCombatStyle);
            states.put(bot, state);
        }
        state.roleGeneration = roleGenerations.get(team).intValue();
        if (state.role == Role.ATTACKER) {
            return false;
        }
        if (state.role == Role.UNDERGROUND
                && CastleWarsManager.isCarryingEnemyFlag(bot)) {
            // Once an underground raider gets the flag, hand it to the normal
            // flag-runner AI so it takes the surface side-door route home rather
            // than trying to return through tunnels or wall stairs.
            states.remove(bot);
            return false;
        }

        if (CastleWarsManager.isInTeamSpawnArea(bot, team)
                && !CastleWarsManager.isCarryingEnemyFlag(bot)
                && state.phase != Phase.SPAWN_SUPPLY
                && state.phase != Phase.LEAVE_SPAWN
                && state.phase != Phase.DESCEND_HOME
                && state.phase != Phase.DEFENDER_CLIMB) {
            state.resetForSpawn();
        }

        if (shouldUseBandage(bot)) {
            CastleWarsManager.useBandage(bot);
        }

        if (!catapultOperator && processFlagCarrierFocus(bot, state)) {
            return true;
        }
        if (!catapultOperator && processOwnDroppedFlagRecovery(bot, state)) {
            return true;
        }

        // Melee underground bots are the tunnel clearers. If the next rockslide
        // on their active route is collapsed, clearing it takes priority over
        // ordinary combat so the rest of the team does not bunch up behind it.
        if (state.role == Role.UNDERGROUND
                && bot.botPrimaryCombatStyle == 0
                && prioritizeMeleeRockClearing(bot, state)) {
            return true;
        }

        // Underground raiders should use the tunnel hazards against opponents,
        // not simply run through each other. Give a safe rock-collapse attempt
        // priority over normal combat when an enemy is standing in the crush zone.
        if (state.role == Role.UNDERGROUND
                && tryCollapseRockslideOnOpponent(bot, state)) {
            return true;
        }

        boolean prioritizeTraversal = catapultOperator || isTraversalPhase(state.phase);
        // Spare defender barricades are deployed against the route enemies
        // ACTUALLY take. Do this before combat AI consumes the same tick.
        if (state.role == Role.DEFENDER && !prioritizeTraversal
                && (state.phase == Phase.GROUND_DEFENCE
                    || state.phase == Phase.DEFEND_FLAG)
                && tryReactiveDefenderBarricade(bot, state)) {
            return true;
        }
        if (prioritizeTraversal) {
            state.sightChaseTarget = null;
            state.sightChaseTicks = 0;
            if (bot.getCombatTarget() != null) {
                CombatManager.stopCombat(bot);
            }
        }

        if (CastleWarsManager.isCarryingEnemyFlag(bot)) {
            state.sightChaseTarget = null;
            state.sightChaseTicks = 0;
            Entity combatTarget = bot.getCombatTarget();
            if (combatTarget != null && !combatTarget.isDead()) {
                CombatManager.stopCombat(bot);
            }
        } else if (!CastleWarsManager.isInTeamSpawnArea(bot, team)
                && !prioritizeTraversal
                && !CastleWarsManager.isGroundCastleExteriorTransitionTile(bot.getPosition())) {
            if (hasActiveOpponent(bot, state)) {
                return true;
            }
            if (processSightChase(bot, state)) {
                return true;
            }
            int engageRadius = state.role == Role.WALL_GUARD ? 15
                    : state.role == Role.DEFENDER ? 12
                    : state.role == Role.UNDERGROUND && isUndergroundTunnel(bot) ? 12 : 8;
            if (tryEngageNearbyOpponent(bot, state, engageRadius)) {
                return true;
            }
        }

        // Underground attackers save explosive potions for collapsed rocks.
        // They use their reusable tinderboxes on barricades obstructing a route.
        Position enemyBarricade = catapultOperator || state.role == Role.UNDERGROUND ? null
                : CastleWarsEngineeringManager.findNearestEnemyBarricade(bot, 3);
        if (enemyBarricade != null
                && bot.getInventoryManager().getItemAmount(
                        CastleWarsEngineeringManager.EXPLOSIVE_POTION_ID) > 0) {
            if (CastleWarsEngineeringManager.destroyBarricadeWithExplosive(bot, enemyBarricade)) {
                CastleWarsBotChat.sayEngineering(bot);
                state.delayTicks = 2;
                return true;
            }
        }

        if (state.delayTicks > 0) {
            --state.delayTicks;
            return true;
        }

        Phase previousPhase = state.phase;
        switch (state.phase) {
            case SPAWN_SUPPLY:
                processSpawnSupply(bot, state);
                break;
            case LEAVE_SPAWN:
                processLeaveSpawn(bot, state);
                break;
            case DESCEND_HOME:
                processDescendHome(bot, state);
                break;
            case GROUND_SUPPLY:
                processGroundSupply(bot, state);
                break;
            case DEFENDER_DEPLOY_GROUND:
                processDefenderGroundBarricades(bot, state);
                break;
            case EXIT_HOME:
                processGroundCastleExit(bot, state);
                break;
            case DEFENDER_CLIMB:
                processOwnFlagClimb(bot, state);
                break;
            case DEFEND_FLAG:
                processDefender(bot, state);
                break;
            case GROUND_DEFENCE:
                processGroundDefender(bot, state);
                break;
            case WALL_GUARD_PATROL:
                processWallGuard(bot, state);
                break;
            case CATAPULT_MOVE:
                processCatapult(bot, state);
                break;
            case CATAPULT_RESTOCK:
                processCatapultRestock(bot, state);
                break;
            case CATAPULT_ROAM:
                processCatapultRoam(bot, state);
                break;
            case MID_RUSH:
                processMidRush(bot, state);
                break;
            case MID_FIGHT:
                processMidFight(bot, state);
                break;
            case UNDERGROUND_DESCEND:
                processUndergroundDescent(bot, state);
                break;
            case UNDERGROUND_OUT:
                processUndergroundCross(bot, state, false);
                break;
            case ENEMY_CLIMB:
                processCastleClimb(bot, state, opposite(state.team), true, Phase.TAKE_FLAG);
                break;
            case TAKE_FLAG:
                processTakeFlag(bot, state);
                break;
            case ENEMY_DESCEND:
                processCastleClimb(bot, state, opposite(state.team), false, Phase.UNDERGROUND_RETURN_DESCEND);
                break;
            case UNDERGROUND_RETURN_DESCEND:
                processUndergroundReturnDescent(bot, state);
                break;
            case UNDERGROUND_BACK:
                processUndergroundCross(bot, state, true);
                break;
            case HOME_CLIMB:
                processCastleClimb(bot, state, state.team, true, Phase.CAPTURE_FLAG);
                break;
            case CAPTURE_FLAG:
                processCapture(bot, state);
                break;
            default:
                state.resetForSpawn();
                break;
        }
        if (catapultOperator && (previousPhase != state.phase || World.tickCount % 50 < 4)
                && GameplayTrace.enabled()) {
            GameplayTrace.log("castle-wars catapult team=" + team + " " + previousPhase + " -> "
                    + state.phase + " rocks=" + bot.getInventoryManager().getItemAmount(
                            CastleWarsEngineeringManager.ROCK_ITEM_ID)
                    + " operator=" + GameplayTrace.describe(bot));
        }
        return true;
    }

    private static void processSpawnSupply(BotPlayer bot, RoleState state) {
        if (!CastleWarsManager.isInTeamSpawnArea(bot, state.team)) {
            if (bot.getPosition().getPlane() == 1) {
                state.phase = Phase.DESCEND_HOME;
            } else if (state.role == Role.UNDERGROUND) {
                state.phase = Phase.UNDERGROUND_DESCEND;
            } else if (state.role == Role.DEFENDER) {
                state.phase = Phase.GROUND_SUPPLY;
            } else if (state.role == Role.WALL_GUARD) {
                state.phase = Phase.WALL_GUARD_PATROL;
            } else if (state.role == Role.CATAPULT) {
                state.phase = Phase.CATAPULT_MOVE;
            } else {
                state.phase = Phase.EXIT_HOME;
            }
            return;
        }

        if (!state.bandagesStocked) {
            makeInventorySpace(bot, 10);
            int amount = 6 + GameUtil.randomInt(4);
            if (CastleWarsManager.giveBandages(bot, amount) > 0) {
                bot.getUpdateState().setAnimation(881);
            }
            state.bandagesStocked = true;
            state.delayTicks = 1 + GameUtil.randomInt(4);
            return;
        }

        if (!state.utilityStocked) {
            if (state.role == Role.UNDERGROUND) {
                state.utilityStocked = stockUndergroundSupplies(bot);
                state.delayTicks = 1 + GameUtil.randomInt(3);
                return;
            } else if (state.role == Role.DEFENDER) {
                makeInventorySpace(bot, 6);
                // Three or fewer initial posts, with the remaining supplies
                // reserved for reacting to the enemy's chosen route.
                CastleWarsEngineeringManager.giveSupply(bot,
                        CastleWarsEngineeringManager.BARRICADE_ITEM_ID, 4);
                CastleWarsEngineeringManager.giveSupply(bot,
                        CastleWarsEngineeringManager.EXPLOSIVE_POTION_ID, 2);
            }
            state.utilityStocked = true;
            state.delayTicks = 1 + GameUtil.randomInt(3);
            return;
        }

        // Ground defenders leave the spawn room like everyone else, descend to
        // plane 0, then patrol the resource room and castle interior.
        state.phase = Phase.LEAVE_SPAWN;
        state.repathDelay = 0;
    }

    private static void processLeaveSpawn(BotPlayer bot, RoleState state) {
        boolean alternateExit = (bot.getNameHash() & 1L) != 0L;
        Position barrier;
        int objectId;
        int objectX;
        int objectY;

        if (state.team == CastleWarsManager.Team.SARADOMIN) {
            barrier = alternateExit
                    ? new Position(2423, 3076, 1)
                    : new Position(2426, 3080, 1);
            objectId = CastleWarsManager.SARADOMIN_ENERGY_BARRIER_ID;
            objectX = alternateExit ? 2422 : 2426;
            objectY = alternateExit ? 3076 : 3080;
        } else {
            barrier = alternateExit
                    ? new Position(2376, 3131, 1)
                    : new Position(2373, 3127, 1);
            objectId = CastleWarsManager.ZAMORAK_ENERGY_BARRIER_ID;
            objectX = alternateExit ? 2377 : 2373;
            objectY = alternateExit ? 3131 : 3126;
        }

        if (!reachInteractionApproach(bot, state, barrier)) {
            return;
        }
        CastleWarsManager.handleFirstObjectAction(bot, objectId, objectX, objectY);
        if (CastleWarsManager.isInTeamSpawnArea(bot, state.team)) {
            return;
        }
        state.phase = Phase.DESCEND_HOME;
        state.repathDelay = 0;
    }

    private static void processDescendHome(BotPlayer bot, RoleState state) {
        if (CastleWarsManager.isInTeamSpawnArea(bot, state.team)) {
            state.phase = Phase.LEAVE_SPAWN;
            state.repathDelay = 0;
            return;
        }
        if (bot.getPosition().getPlane() == 0) {
            state.phase = Phase.GROUND_SUPPLY;
            return;
        }
        if (state.team == CastleWarsManager.Team.SARADOMIN) {
            useTraversal(bot, state, new Position(2420, 3080, 1), 4415, 2419, 3080);
        } else {
            useTraversal(bot, state, new Position(2379, 3127, 1), 4415, 2380, 3127);
        }
        if (bot.getPosition().getPlane() == 0) {
            state.phase = Phase.GROUND_SUPPLY;
        }
    }

    private static void processGroundSupply(BotPlayer bot, RoleState state) {
        if (state.role == Role.CATAPULT) {
            state.phase = Phase.CATAPULT_RESTOCK;
            state.wallAccessed = false;
            state.repathDelay = 0;
            return;
        }
        Position supply = state.team == CastleWarsManager.Team.SARADOMIN
                ? new Position(2426, 3075, 0)
                : new Position(2373, 3131, 0);
        if (!near(bot, supply, 4)) {
            walk(bot, state, supply);
            return;
        }

        if (!state.utilityStocked) {
            if (state.role == Role.UNDERGROUND) {
                state.utilityStocked = stockUndergroundSupplies(bot);
                if (!state.utilityStocked) {
                    state.delayTicks = 1 + GameUtil.randomInt(3);
                    return;
                }
            } else {
                state.utilityStocked = true;
            }
            bot.getUpdateState().setAnimation(881);
            state.delayTicks = 2 + GameUtil.randomInt(4);
        }

        if (state.role == Role.UNDERGROUND) {
            state.phase = Phase.UNDERGROUND_DESCEND;
        } else if (state.role == Role.DEFENDER) {
            // Defend the main entrance FIRST, then climb to the flag room.
            // Previously bots spent the entire game trying the same upper
            // flag-room tile and often placed no barricades at all.
            state.phase = Phase.DEFENDER_DEPLOY_GROUND;
            state.repathDelay = 0;
        } else if (state.role == Role.WALL_GUARD) {
            state.phase = Phase.WALL_GUARD_PATROL;
            state.wallPatrolTarget = null;
            state.wallPatrolTicks = 0;
        } else {
            state.phase = Phase.EXIT_HOME;
        }
    }

    private static void processGroundCastleExit(BotPlayer bot, RoleState state) {
        Position exterior = CastleWarsManager.getBotMainDoorExteriorPosition(bot, state.team);
        if (state.role == Role.UNDERGROUND
                && CastleWarsBotBarricadeAi.handleBlockedRoute(bot, exterior)) {
            return;
        }
        CastleWarsManager.routeBotThroughGroundCastle(bot, state.team, false);
        if (!near(bot, exterior, 0)) {
            state.repathDelay = 0;
            return;
        }
        if (state.role == Role.CATAPULT) {
            state.phase = Phase.CATAPULT_MOVE;
        } else {
            state.phase = Phase.CATAPULT_ROAM;
        }
        state.repathDelay = 0;
    }

    private static void processOwnFlagClimb(BotPlayer bot, RoleState state) {
        int plane = bot.getPosition().getPlane();
        if (plane == 1) {
            Position ladder = state.team == CastleWarsManager.Team.SARADOMIN
                    ? new Position(2429, 3075, 1)
                    : new Position(2370, 3132, 1);
            Position ladderApproach =
                    CastleWarsManager.getNearestAdjacentInteractionTile(bot, ladder);
            if (ladderApproach == null || !reachInteractionApproach(bot, state, ladderApproach)) {
                return;
            }
            if (state.team == CastleWarsManager.Team.SARADOMIN) {
                CastleWarsManager.handleFirstObjectAction(bot,
                        CastleWarsManager.SARADOMIN_SPAWN_LADDER_ID, 2429, 3075);
            } else {
                CastleWarsManager.handleFirstObjectAction(bot,
                        CastleWarsManager.ZAMORAK_SPAWN_LADDER_ID, 2370, 3132);
            }
            state.repathDelay = 0;
            return;
        }
        if (plane >= 3) {
            state.phase = Phase.DEFEND_FLAG;
            return;
        }
        processCastleClimb(bot, state, state.team, true, Phase.DEFEND_FLAG);
    }

    private static void processGroundDefender(BotPlayer bot, RoleState state) {
        if (bot.getPosition().getPlane() != 0) {
            state.phase = Phase.DESCEND_HOME;
            state.repathDelay = 0;
            return;
        }

        CastleWarsManager.Team ownFlagTeam = state.team;
        Player enemyHolder = CastleWarsManager.getFlagHolder(ownFlagTeam);
        if (enemyHolder != null && !enemyHolder.isDead()
                && enemyHolder.getPosition().getPlane() == 0
                && GameUtil.getDistance(bot.getPosition(), enemyHolder.getPosition()) <= 15) {
            CombatManager.startCombat(bot, enemyHolder);
            return;
        }

        if (state.defenderPatrolTarget == null
                || state.defenderPatrolTarget.getPlane() != 0
                || CastleWarsManager.getCastleTeamAtPosition(state.defenderPatrolTarget) != state.team
                || isDefenderPostCrowded(bot, state.defenderPatrolTarget)) {
            state.defenderPatrolTarget = chooseGroundDefenderPatrolTarget(bot, state);
            state.defenderPatrolTicks = 3 + GameUtil.randomInt(5);
            state.repathDelay = 0;
        }

        if (state.defenderPatrolTarget != null
                && !near(bot, state.defenderPatrolTarget, 1)) {
            walk(bot, state, state.defenderPatrolTarget);
            return;
        }

        if (state.defenderPatrolTarget != null
                && near(bot, state.defenderPatrolTarget, 1)) {
            if (--state.defenderPatrolTicks <= 0) {
                state.defenderPatrolTarget = chooseGroundDefenderPatrolTarget(bot, state);
                state.defenderPatrolTicks = 3 + GameUtil.randomInt(5);
                state.repathDelay = 0;
                if (state.defenderPatrolTarget != null
                        && !near(bot, state.defenderPatrolTarget, 1)) {
                    walk(bot, state, state.defenderPatrolTarget);
                }
                return;
            }
            if (GameUtil.randomInt(18) == 0) {
                CastleWarsBotChat.sayDefence(bot);
            }
        }
    }

    private static Position chooseGroundDefenderPatrolTarget(BotPlayer bot, RoleState state) {
        int minX = state.team == CastleWarsManager.Team.SARADOMIN ? 2415 : 2368;
        int maxX = state.team == CastleWarsManager.Team.SARADOMIN ? 2431 : 2387;
        int minY = state.team == CastleWarsManager.Team.SARADOMIN ? 3072 : 3117;
        int maxY = state.team == CastleWarsManager.Team.SARADOMIN ? 3088 : 3135;
        Position resourceRoom = state.team == CastleWarsManager.Team.SARADOMIN
                ? new Position(2426, 3075, 0)
                : new Position(2373, 3131, 0);
        Position doorInterior = CastleWarsManager.getBotMainDoorInteriorPosition(bot, state.team);

        Position best = null;
        int bestScore = Integer.MAX_VALUE;
        long patrolSalt = bot.getNameHash() + (long)state.defenderPatrolGeneration * 131L;

        for (int x = minX; x <= maxX; ++x) {
            for (int y = minY; y <= maxY; ++y) {
                Position candidate = new Position(x, y, 0);
                if (CastleWarsManager.getCastleTeamAtPosition(candidate) != state.team
                        || CastleWarsManager.isGroundCastleExteriorTransitionTile(candidate)) {
                    continue;
                }

                int clipping = WalkingCollisionMap.getTileFlags(x, y, 0);
                if ((clipping & 0x1280100) != 0) {
                    continue;
                }

                int movementDistance = GameUtil.getDistance(bot.getPosition(), candidate);
                if (movementDistance < 3) {
                    continue;
                }

                int crowding = 0;
                for (Player player : CastleWarsManager.getGamePlayersView()) {
                    if (player == null || player == bot || player.isDead()
                            || CastleWarsManager.getGameTeam(player) != state.team
                            || player.getPosition().getPlane() != 0) {
                        continue;
                    }
                    int distance = GameUtil.getDistance(candidate, player.getPosition());
                    if (distance <= 1) {
                        crowding += 6;
                    } else if (distance <= 3) {
                        crowding += 2;
                    }
                }

                // Alternate the preferred area between the resource room and the
                // interior entrance so defenders visibly patrol the whole ground floor.
                Position focus = (state.defenderPatrolGeneration & 1) == 0
                        ? resourceRoom : doorInterior;
                int focusDistance = focus == null ? 0 : GameUtil.getDistance(candidate, focus);
                int patrolHopCost = Math.abs(movementDistance - 7) * 3;
                int focusCost = Math.abs(focusDistance - 4) * 2;
                int jitter = (int)Math.abs((patrolSalt + x * 31L + y * 17L) % 13L);
                int score = crowding * 100 + patrolHopCost + focusCost + jitter;

                if (score < bestScore) {
                    bestScore = score;
                    best = candidate;
                }
            }
        }

        ++state.defenderPatrolGeneration;
        return best;
    }

    /**
     * Choose barricade posts from real passable tiles rather than always
     * attempting the same two tiles just inside the large double door.
     *
     * Defender 0 establishes one inner gate post, then protects the flag room.
     * Defender 1 builds an outer and an inner ground-floor line and stays
     * downstairs to reinforce the entrance enemies are actually attacking.
     *
     * stage 0 = ground entrance flank
     * stage 1 = inner ground (defender 1) / upper approach (defender 0)
     * stage 2 = second upper flag-room post (defender 0)
     * stage 3 = reactive post near the live enemy approach on this plane
     */
    static Position chooseDefenderBarricadeSite(
            CastleWarsManager.Team team, int defenderIndex, int stage,
            Position botPosition, Position enemyFocus, Set<String> rejected) {
        if (team == null || botPosition == null || defenderIndex < 0
                || defenderIndex > 1 || stage < 0 || stage > 3) {
            return null;
        }
        boolean sara = team == CastleWarsManager.Team.SARADOMIN;
        boolean upper = stage == 3 ? botPosition.getPlane() == 3
                : defenderIndex == 0 && stage > 0;
        int plane = upper ? 3 : 0;
        if (botPosition.getPlane() != plane) {
            return null;
        }
        int minX = sara ? (upper ? 2423 : 2422) : (upper ? 2369 : 2370);
        int maxX = sara ? 2430 : (upper ? 2376 : 2377);
        int minY = sara ? (upper ? 3073 : stage == 1 ? 3075 : 3079)
                : (upper ? 3127 : stage == 1 ? 3128 : 3124);
        int maxY = sara ? (upper ? 3080 : stage == 1 ? 3079 : 3083)
                : (upper ? 3134 : stage == 1 ? 3133 : 3128);

        Position focus = upper
                ? (sara ? new Position(2427, 3077, 3)
                        : new Position(2372, 3130, 3))
                : stage == 1 ? (sara ? new Position(2425, 3077, 0)
                        : new Position(2375, 3130, 0))
                : (sara ? new Position(2426, 3081, 0)
                        : new Position(2373, 3126, 0));
        Position best = null;
        int bestScore = Integer.MAX_VALUE;
        for (int x = minX; x <= maxX; ++x) {
            for (int y = minY; y <= maxY; ++y) {
                Position candidate = new Position(x, y, plane);
                String key = x + ":" + y + ":" + plane;
                if (rejected != null && rejected.contains(key)
                        || !CastleWarsEngineeringManager.isDefensiveBarricadeSiteAvailable(team, candidate)
                        || !CastleWarsEngineeringManager.isDefensiveBarricadeSeparated(team, candidate, 3)) {
                    continue;
                }
                int score = GameUtil.getDistance(candidate, focus) * 4;
                int walkingDistance = GameUtil.getDistance(botPosition, candidate);
                // Do not pick some distant unreachable tile for an active
                // reinforcement while a flag carrier is passing through.
                if (stage == 3 && walkingDistance > 6) {
                    continue;
                }
                score += Math.max(0, walkingDistance - 5) * 2;
                int preferredX = sara ? (defenderIndex == 0 ? 2424 : 2429)
                        : (defenderIndex == 0 ? 2375 : 2370);
                score += Math.abs(x - preferredX) * 3;
                if (enemyFocus != null && enemyFocus.getPlane() == plane) {
                    // Select a post just ahead of the actual attacking lane.
                    // Both outside gate threats and already-in-castle enemies
                    // influence the choice; don't build on the enemy's tile.
                    int distance = GameUtil.getDistance(candidate, enemyFocus);
                    if (distance < 2) {
                        continue;
                    }
                    // The threatened X lane takes priority over a bot's
                    // normal left/right post preference. Otherwise an enemy
                    // switching gate flanks still gets the same barricade.
                    score += Math.abs(x - enemyFocus.getX()) * 16
                            + Math.abs(distance - 3) * 4;
                }
                if (score < bestScore) {
                    bestScore = score;
                    best = candidate;
                }
            }
        }
        return best;
    }

    private static Position findDefenceThreat(BotPlayer bot, RoleState state) {
        int plane = bot.getPosition().getPlane();
        Position focus = state.team == CastleWarsManager.Team.SARADOMIN
                ? plane == 3 ? new Position(2429, 3074, 3)
                        : new Position(2426, 3082, 0)
                : plane == 3 ? new Position(2370, 3133, 3)
                        : new Position(2373, 3125, 0);
        Position nearest = null;
        int closest = Integer.MAX_VALUE;
        for (Player opponent : CastleWarsManager.getGamePlayersView()) {
            if (opponent == null || opponent.isDead()
                    || !CastleWarsManager.areOpponents(bot, opponent)
                    || opponent.getPosition().getPlane() != plane) {
                continue;
            }
            int distance = GameUtil.getDistance(focus, opponent.getPosition());
            if (distance > (plane == 3 ? 12 : 16)) {
                continue;
            }
            // An enemy with our flag takes priority over an incidental fighter.
            if (opponent == CastleWarsManager.getFlagHolder(state.team)) {
                distance -= 12;
            }
            if (distance < closest) {
                closest = distance;
                nearest = opponent.getPosition().copy();
            }
        }
        return nearest;
    }

    private static void processDefenderGroundBarricades(BotPlayer bot, RoleState state) {
        if (bot.getPosition().getPlane() != 0) {
            state.phase = Phase.DESCEND_HOME;
            state.repathDelay = 0;
            return;
        }

        int groundPosts = state.defenderIndex == 0 ? 1 : 2;
        if (state.barricadesPlaced >= groundPosts) {
            // Keep one defender at the gate. Previously both climbed after
            // building the same choke, leaving the whole ground floor empty.
            state.phase = state.defenderIndex == 0
                    ? Phase.DEFENDER_CLIMB : Phase.GROUND_DEFENCE;
            state.repathDelay = 0;
            return;
        }

        deployDefenderBarricade(bot, state);
    }

    /** Retry alternative sites in the same defence zone before abandoning it. */
    private static void deployDefenderBarricade(BotPlayer bot, RoleState state) {
        if (bot.getInventoryManager().getItemAmount(
                CastleWarsEngineeringManager.BARRICADE_ITEM_ID) <= 0
                || CastleWarsEngineeringManager.getBarricadeCount(state.team) >= 10) {
            state.barricadesPlaced = 3;
            state.barricadeTarget = null;
            return;
        }
        Position threat = findDefenceThreat(bot, state);
        Position site = state.barricadeTarget;
        if (site == null || !CastleWarsEngineeringManager.isDefensiveBarricadeSiteAvailable(
                state.team, site)
                || !CastleWarsEngineeringManager.isDefensiveBarricadeSeparated(state.team, site, 3)) {
            site = chooseDefenderBarricadeSite(state.team, state.defenderIndex,
                    state.barricadesPlaced, bot.getPosition(),
                    threat, state.rejectedBarricadeSites);
            state.barricadeTarget = site;
            state.barricadeLastPosition = null;
            state.barricadeStuckTicks = 0;
        }
        if (site == null) {
            advanceDefenderBarricadeSite(state);
            return;
        }
        Position approach = CastleWarsManager.getNearestAdjacentInteractionTile(bot, site);
        if (approach == null) {
            rejectDefenderBarricadeSite(state, site);
            return;
        }
        if (near(bot, approach, 0)) {
            if (CastleWarsEngineeringManager.placeBarricadeForBot(bot, site)) {
                CastleWarsBotChat.sayDefence(bot);
                state.delayTicks = 2;
                advanceDefenderBarricadeSite(state);
            } else {
                rejectDefenderBarricadeSite(state, site);
            }
            return;
        }

        if (state.barricadeLastPosition != null
                && state.barricadeLastPosition.equals(bot.getPosition())) {
            ++state.barricadeStuckTicks;
        } else {
            state.barricadeLastPosition = bot.getPosition().copy();
            state.barricadeStuckTicks = 0;
        }
        if (state.barricadeStuckTicks >= 16) {
            // Try another available tile in this same area; the old logic
            // threw away the whole barricade after one pathfinding failure.
            rejectDefenderBarricadeSite(state, site);
            return;
        }
        walk(bot, state, approach);
    }

    private static void rejectDefenderBarricadeSite(RoleState state, Position site) {
        state.rejectedBarricadeSites.add(
                site.getX() + ":" + site.getY() + ":" + site.getPlane());
        state.barricadeTarget = null;
        state.barricadeLastPosition = null;
        state.barricadeStuckTicks = 0;
        state.repathDelay = 0;
        if (state.rejectedBarricadeSites.size() >= 10) {
            advanceDefenderBarricadeSite(state);
        }
    }

    private static void advanceDefenderBarricadeSite(RoleState state) {
        ++state.barricadesPlaced;
        state.barricadeTarget = null;
        state.barricadeLastPosition = null;
        state.barricadeStuckTicks = 0;
        state.rejectedBarricadeSites.clear();
        state.repathDelay = 0;
    }

    /**
     * Preserve spare supplies until an enemy actually threatens this floor.
     * A ground defender reinforces the active approach, while the upstairs
     * defender closes a flag-room flank. Never spend supplies at a safe flag.
     */
    private static boolean tryReactiveDefenderBarricade(BotPlayer bot, RoleState state) {
        if (state.reinforcementsPlaced >= (state.defenderIndex == 0 ? 1 : 2)
                || bot.getInventoryManager().getItemAmount(
                        CastleWarsEngineeringManager.BARRICADE_ITEM_ID) <= 0
                || CastleWarsEngineeringManager.getBarricadeCount(state.team) >= 10) {
            return false;
        }
        Position enemy = findDefenceThreat(bot, state);
        if (enemy == null) {
            state.reactiveBarricadeTarget = null;
            state.reactiveStuckTicks = 0;
            return false;
        }
        Position site = state.reactiveBarricadeTarget;
        if (site == null || !CastleWarsEngineeringManager.isDefensiveBarricadeSiteAvailable(
                state.team, site)
                || !CastleWarsEngineeringManager.isDefensiveBarricadeSeparated(state.team, site, 3)) {
            site = chooseDefenderBarricadeSite(state.team, state.defenderIndex,
                    3, bot.getPosition(), enemy, state.reactiveRejectedBarricadeSites);
            state.reactiveBarricadeTarget = site;
            state.reactiveLastPosition = null;
            state.reactiveStuckTicks = 0;
        }
        if (site == null) {
            return false;
        }
        Position approach = CastleWarsManager.getNearestAdjacentInteractionTile(bot, site);
        if (approach == null) {
            state.reactiveRejectedBarricadeSites.add(
                    site.getX() + ":" + site.getY() + ":" + site.getPlane());
            state.reactiveBarricadeTarget = null;
            return false;
        }
        if (near(bot, approach, 0)) {
            if (CastleWarsEngineeringManager.placeBarricadeForBot(bot, site)) {
                ++state.reinforcementsPlaced;
                CastleWarsBotChat.sayDefence(bot);
                state.delayTicks = 2;
                state.reactiveRejectedBarricadeSites.clear();
                state.reactiveBarricadeTarget = null;
                return true;
            }
            state.reactiveBarricadeTarget = null;
            return false;
        }
        if (state.reactiveLastPosition != null
                && state.reactiveLastPosition.equals(bot.getPosition())) {
            ++state.reactiveStuckTicks;
        } else {
            state.reactiveLastPosition = bot.getPosition().copy();
            state.reactiveStuckTicks = 0;
        }
        if (state.reactiveStuckTicks >= 10) {
            state.reactiveRejectedBarricadeSites.add(
                    site.getX() + ":" + site.getY() + ":" + site.getPlane());
            state.reactiveBarricadeTarget = null;
            state.reactiveStuckTicks = 0;
            return false;
        }
        walk(bot, state, approach);
        return true;
    }

    private static void processDefender(BotPlayer bot, RoleState state) {
        if (state.barricadesPlaced < 3) {
            deployDefenderBarricade(bot, state);
            return;
        }

        CastleWarsManager.Team ownFlagTeam = state.team;
        Player enemyHolder = CastleWarsManager.getFlagHolder(ownFlagTeam);
        if (enemyHolder != null && !enemyHolder.isDead()
                && enemyHolder.getPosition().getPlane() == bot.getPosition().getPlane()
                && GameUtil.getDistance(bot.getPosition(), enemyHolder.getPosition()) <= 15) {
            CombatManager.startCombat(bot, enemyHolder);
            return;
        }

        Position flag = state.team == CastleWarsManager.Team.SARADOMIN
                ? new Position(2429, 3074, 3)
                : new Position(2370, 3133, 3);

        if (state.defenderPatrolTarget == null
                || state.defenderPatrolTarget.getPlane() != bot.getPosition().getPlane()
                || isDefenderPostCrowded(bot, state.defenderPatrolTarget)) {
            state.defenderPatrolTarget = chooseDefenderPatrolTarget(bot, state, flag);
            state.defenderPatrolTicks = 3 + GameUtil.randomInt(5);
            state.repathDelay = 0;
        }

        if (state.defenderPatrolTarget != null
                && !near(bot, state.defenderPatrolTarget, 1)) {
            walk(bot, state, state.defenderPatrolTarget);
            return;
        }

        // Patrol timing starts only after the defender actually reaches its post.
        // After a short guard pause, choose another post several tiles away so
        // defenders keep visibly moving around the flag room instead of looking AFK.
        if (state.defenderPatrolTarget != null
                && near(bot, state.defenderPatrolTarget, 1)) {
            if (--state.defenderPatrolTicks <= 0) {
                state.defenderPatrolTarget = chooseDefenderPatrolTarget(bot, state, flag);
                state.defenderPatrolTicks = 3 + GameUtil.randomInt(5);
                state.repathDelay = 0;
                if (state.defenderPatrolTarget != null
                        && !near(bot, state.defenderPatrolTarget, 1)) {
                    walk(bot, state, state.defenderPatrolTarget);
                }
                return;
            }
            if (GameUtil.randomInt(18) == 0) {
                CastleWarsBotChat.sayDefence(bot);
            }
        }
    }

    private static Position chooseDefenderPatrolTarget(BotPlayer bot, RoleState state,
                                                        Position flag) {
        int minX = state.team == CastleWarsManager.Team.SARADOMIN ? 2415 : 2368;
        int maxX = state.team == CastleWarsManager.Team.SARADOMIN ? 2431 : 2387;
        int minY = state.team == CastleWarsManager.Team.SARADOMIN ? 3072 : 3117;
        int maxY = state.team == CastleWarsManager.Team.SARADOMIN ? 3089 : 3135;
        int plane = bot.getPosition().getPlane();

        Position best = null;
        int bestScore = Integer.MAX_VALUE;
        long patrolSalt = bot.getNameHash() + (long)state.defenderPatrolGeneration * 131L;

        for (int x = minX; x <= maxX; ++x) {
            for (int y = minY; y <= maxY; ++y) {
                Position candidate = new Position(x, y, plane);
                int flagDistance = GameUtil.getDistance(candidate, flag);
                if (flagDistance < 3 || flagDistance > 10) {
                    continue;
                }

                int clipping = WalkingCollisionMap.getTileFlags(x, y, plane);
                if ((clipping & 0x1280100) != 0) {
                    continue;
                }

                int crowding = 0;
                for (Player player : CastleWarsManager.getGamePlayersView()) {
                    if (player == null || player == bot || player.isDead()
                            || CastleWarsManager.getGameTeam(player) != state.team
                            || player.getPosition().getPlane() != plane) {
                        continue;
                    }
                    int distance = GameUtil.getDistance(candidate, player.getPosition());
                    if (distance <= 1) {
                        crowding += 6;
                    } else if (distance <= 3) {
                        crowding += 2;
                    } else if (distance <= 5) {
                        crowding += 1;
                    }
                }

                int movementDistance = GameUtil.getDistance(bot.getPosition(), candidate);
                int guardBandCost = Math.abs(flagDistance - 6) * 4;
                // Prefer a meaningful patrol hop rather than selecting the nearest
                // legal tile over and over. Around six tiles keeps the movement
                // visible without pulling defenders too far away from their flag.
                int patrolHopCost = Math.abs(movementDistance - 6) * 3;
                if (movementDistance < 3) {
                    patrolHopCost += 18;
                }
                int jitter = (int)Math.abs((patrolSalt + x * 31L + y * 17L) % 11L);
                int score = crowding * 100 + guardBandCost + patrolHopCost + jitter;
                if (score < bestScore) {
                    bestScore = score;
                    best = candidate;
                }
            }
        }

        ++state.defenderPatrolGeneration;
        return best;
    }

    private static boolean isDefenderPostCrowded(BotPlayer bot, Position post) {
        if (post == null) {
            return false;
        }
        int nearby = 0;
        CastleWarsManager.Team team = CastleWarsManager.getGameTeam(bot);
        for (Player player : CastleWarsManager.getGamePlayersView()) {
            if (player == null || player == bot || player.isDead()
                    || CastleWarsManager.getGameTeam(player) != team
                    || player.getPosition().getPlane() != post.getPlane()) {
                continue;
            }
            if (GameUtil.getDistance(player.getPosition(), post) <= 1 && ++nearby >= 2) {
                return true;
            }
        }
        return false;
    }

    private static void processMidRush(BotPlayer bot, RoleState state) {
        if (bot.getPosition().getPlane() != 0) {
            return;
        }
        if (tryEngageNearbyOpponent(bot, state, 16)) {
            state.phase = Phase.MID_FIGHT;
            return;
        }

        Position rally = midRallyPoint(state);
        if (!near(bot, rally, 4)) {
            walk(bot, state, rally);
            return;
        }

        state.phase = Phase.MID_FIGHT;
        state.midPatrolTicks = 12 + GameUtil.randomInt(18);
        state.repathDelay = 0;
    }

    private static void processMidFight(BotPlayer bot, RoleState state) {
        if (bot.getPosition().getPlane() != 0) {
            state.phase = Phase.MID_RUSH;
            return;
        }

        if (tryEngageNearbyOpponent(bot, state, 18)) {
            state.midPatrolTicks = 12 + GameUtil.randomInt(18);
            return;
        }

        if (--state.midPatrolTicks <= 0) {
            state.midPatrolTicks = 12 + GameUtil.randomInt(18);
            state.routeVariant = GameUtil.randomInt(3);
            state.routeOffsetX = -4 + GameUtil.randomInt(9);
            state.routeOffsetY = -4 + GameUtil.randomInt(9);
            if (GameUtil.randomInt(5) == 0) {
                CastleWarsBotChat.sayMid(bot);
            }
        }

        Position patrol = midRallyPoint(state);
        if (!near(bot, patrol, 3)) {
            walk(bot, state, patrol);
        }
    }

    private static Position midRallyPoint(RoleState state) {
        int x;
        int y;
        if (state.routeVariant == 0) {
            x = 2400;
            y = 3104;
        } else if (state.routeVariant == 1) {
            x = 2396;
            y = 3100;
        } else {
            x = 2404;
            y = 3108;
        }
        return new Position(x + state.routeOffsetX, y + state.routeOffsetY, 0);
    }

    private static void processWallGuard(BotPlayer bot, RoleState state) {
        if (bot.botPrimaryCombatStyle == 0) {
            state.phase = Phase.EXIT_HOME;
            state.repathDelay = 0;
            return;
        }
        if (bot.getPosition().getPlane() != 0) {
            state.phase = Phase.DESCEND_HOME;
            state.repathDelay = 0;
            return;
        }

        if (!state.wallAccessed) {
            if (CastleWarsEngineeringManager.isBattlementWalkwayTile(
                    bot.getPosition(), state.team)) {
                state.wallAccessed = true;
            } else {
                Position stairApproach = state.team == CastleWarsManager.Team.SARADOMIN
                        ? new Position(2416, 3074, 0)
                        : new Position(2383, 3133, 0);
                Position wallLanding = state.team == CastleWarsManager.Team.SARADOMIN
                        ? new Position(2417, 3077, 0)
                        : new Position(2382, 3130, 0);

                if (near(bot, wallLanding, 0)) {
                    state.wallAccessed = true;
                    state.wallPatrolTarget = null;
                    state.repathDelay = 0;
                } else if (reachInteractionApproach(bot, state, stairApproach)) {
                    if (CastleWarsManager.moveBotThroughGroundCastleStairs(
                            bot, state.team, true)) {
                        state.wallAccessed = true;
                        state.wallPatrolTarget = null;
                        state.repathDelay = 0;
                    }
                }
                return;
            }
        }

        if (state.wallPatrolTarget != null
                && !CastleWarsEngineeringManager.isBattlementWalkwayTile(
                bot.getPosition(), state.team)) {
            state.wallAccessed = false;
            state.wallPatrolTarget = null;
            state.repathDelay = 0;
            return;
        }

        if (state.wallPatrolTarget == null
                || --state.wallPatrolTicks <= 0
                || isWallPostCrowded(bot, state.wallPatrolTarget)) {
            state.wallPatrolTarget = chooseWallPatrolTarget(bot, state);
            state.wallPatrolTicks = 12 + GameUtil.randomInt(22);
            state.repathDelay = 0;
        }

        if (state.wallPatrolTarget != null
                && !near(bot, state.wallPatrolTarget, 0)) {
            walk(bot, state, state.wallPatrolTarget);
            return;
        }

        // If combat movement ever pulls a guard off the actual battlement
        // walkway, force it back through the wall-access stairs.
        if (!CastleWarsEngineeringManager.isBattlementWalkwayTile(
                bot.getPosition(), state.team)) {
            state.wallAccessed = false;
            state.wallPatrolTarget = null;
            state.repathDelay = 0;
            return;
        }

        if (state.wallPatrolTarget != null && GameUtil.randomInt(24) == 0) {
            CastleWarsBotChat.sayDefence(bot);
        }
    }

    private static Position chooseWallPatrolTarget(BotPlayer bot, RoleState state) {
        Position current = bot.getPosition();
        long salt = bot.getNameHash() + (long)state.wallPatrolGeneration * 173L;
        Position best = null;
        int bestScore = Integer.MAX_VALUE;

        // Once a guard reaches the wall, it patrols only one clipped step at a
        // time between actual battlement walkway tiles. This prevents pathing
        // around the ground outside the castle to reach another wall post.
        if (CastleWarsEngineeringManager.isBattlementWalkwayTile(current, state.team)) {
            for (int dx = -1; dx <= 1; ++dx) {
                for (int dy = -1; dy <= 1; ++dy) {
                    if (dx == 0 && dy == 0) {
                        continue;
                    }
                    Position candidate = new Position(
                            current.getX() + dx, current.getY() + dy, current.getPlane());
                    if (!CastleWarsEngineeringManager.isBattlementWalkwayTile(
                            candidate, state.team)
                            || !bot.canStepToOffset(dx, dy)) {
                        continue;
                    }
                    int score = wallPatrolCrowding(bot, state.team, candidate) * 100
                            + (int)Math.abs((salt
                            + candidate.getX() * 37L + candidate.getY() * 19L) % 17L);
                    if (score < bestScore) {
                        bestScore = score;
                        best = candidate;
                    }
                }
            }
        }

        // The staircase landing can sit immediately beside the first true wall
        // tile, so allow one initial path onto the nearest battlement tile.
        if (best == null) {
            int minX = state.team == CastleWarsManager.Team.SARADOMIN ? 2412 : 2368;
            int maxX = state.team == CastleWarsManager.Team.SARADOMIN ? 2431 : 2387;
            int minY = state.team == CastleWarsManager.Team.SARADOMIN ? 3072 : 3117;
            int maxY = state.team == CastleWarsManager.Team.SARADOMIN ? 3089 : 3135;
            for (int x = minX; x <= maxX; ++x) {
                for (int y = minY; y <= maxY; ++y) {
                    Position candidate = new Position(x, y, 0);
                    if (!CastleWarsEngineeringManager.isBattlementWalkwayTile(
                            candidate, state.team)) {
                        continue;
                    }
                    int distance = GameUtil.getDistance(current, candidate);
                    int score = distance * 10
                            + wallPatrolCrowding(bot, state.team, candidate) * 100
                            + (int)Math.abs((salt + x * 37L + y * 19L) % 17L);
                    if (score < bestScore) {
                        bestScore = score;
                        best = candidate;
                    }
                }
            }
        }

        ++state.wallPatrolGeneration;
        return best;
    }

    private static int wallPatrolCrowding(BotPlayer bot,
                                           CastleWarsManager.Team team,
                                           Position candidate) {
        int crowding = 0;
        for (Player player : CastleWarsManager.getGamePlayersView()) {
            if (player == null || player == bot || player.isDead()
                    || CastleWarsManager.getGameTeam(player) != team
                    || player.getPosition().getPlane() != candidate.getPlane()) {
                continue;
            }
            int distance = GameUtil.getDistance(candidate, player.getPosition());
            if (distance <= 1) {
                crowding += 8;
            } else if (distance <= 3) {
                crowding += 3;
            } else if (distance <= 5) {
                crowding += 1;
            }
        }
        return crowding;
    }

    private static boolean isWallPostCrowded(BotPlayer bot, Position post) {
        if (post == null) {
            return false;
        }
        CastleWarsManager.Team team = CastleWarsManager.getGameTeam(bot);
        for (Player player : CastleWarsManager.getGamePlayersView()) {
            if (player == null || player == bot || player.isDead()
                    || CastleWarsManager.getGameTeam(player) != team
                    || player.getPosition().getPlane() != post.getPlane()) {
                continue;
            }
            if (GameUtil.getDistance(player.getPosition(), post) <= 2) {
                return true;
            }
        }
        return false;
    }

    private static void processCatapult(BotPlayer bot, RoleState state) {
        if (bot.getPosition().getPlane() != 0) {
            state.phase = Phase.DESCEND_HOME;
            state.wallAccessed = false;
            state.repathDelay = 0;
            return;
        }

        // Ground tiles near wall scenery can look like battlements. Use the
        // actual stair landing as the waypoint before heading to the catapult.
        boolean saradomin = state.team == CastleWarsManager.Team.SARADOMIN;
        Position stairApproach = saradomin
                ? new Position(2418, 3077, 0) : new Position(2382, 3130, 0);
        if (near(bot, stairApproach, 0) && !bot.castleWarsOnBattlements) {
            state.wallAccessed = false;
        }
        if (!state.wallAccessed) {
            Position wallLanding = saradomin
                    ? new Position(2416, 3074, 0) : new Position(2383, 3133, 0);
            if (near(bot, wallLanding, 0)) {
                state.wallAccessed = true;
                state.repathDelay = 0;
            } else if (bot.getInventoryManager().getItemAmount(
                    CastleWarsEngineeringManager.TINDERBOX_ITEM_ID) <= 0) {
                state.phase = Phase.CATAPULT_RESTOCK;
                state.repathDelay = 0;
                return;
            } else {
                if (reachInteractionApproach(bot, state, stairApproach)) {
                    if (CastleWarsManager.moveBotThroughGroundCastleStairs(
                            bot, state.team, false)) {
                        CastleWarsManager.setPlayerOnBattlements(bot, true);
                        state.wallAccessed = true;
                        state.repathDelay = 0;
                        if (GameplayTrace.enabled()) {
                            GameplayTrace.log("castle-wars catapult used stairs team=" + state.team
                                    + " operator=" + GameplayTrace.describe(bot));
                        }
                    }
                }
                return;
            }
        }

        // Keep stair access for this trip. Some tiles on the clipped route
        // from the landing to the weapon lack the battlement scenery marker.
        if (!canOperateCatapultFrom(bot.getPosition(), state.team)) {
            if (state.repathDelay > 0) {
                --state.repathDelay;
                return;
            }
            Position operatingPosition = findCatapultBotOperatingPosition(
                    state.team, bot.getPosition(),
                    tile -> PathFinder.isReachable(bot, tile.getX(), tile.getY()));
            if (operatingPosition == null) {
                state.repathDelay = 6;
                return;
            }
            reachInteractionApproach(bot, state, operatingPosition);
            return;
        }

        if (!CastleWarsEngineeringManager.isCatapultOperational(state.team)) {
            if (bot.getInventoryManager().getItemAmount(CastleWarsEngineeringManager.TOOLKIT_ID) <= 0) {
                state.phase = Phase.CATAPULT_RESTOCK;
                state.repathDelay = 0;
            } else if (CastleWarsEngineeringManager.repairOwnCatapult(bot)) {
                CastleWarsBotChat.sayCatapult(bot);
                state.delayTicks = 4;
            }
            return;
        }

        if (bot.getInventoryManager().getItemAmount(
                CastleWarsEngineeringManager.ROCK_ITEM_ID) <= 0) {
            // Stay at the siege weapon and fetch replacement rocks.
            // The old behaviour abandoned the cannon for aimless field roaming.
            state.phase = Phase.CATAPULT_RESTOCK;
            state.repathDelay = 0;
            return;
        }

        if (CastleWarsEngineeringManager.fireCatapult(bot)) {
            if (GameplayTrace.enabled()) {
                GameplayTrace.log("castle-wars catapult fired team=" + state.team
                        + " operator=" + GameplayTrace.describe(bot));
            }
            if (GameUtil.randomInt(3) == 0) {
                CastleWarsBotChat.sayCatapult(bot);
            }
            state.delayTicks = 4;
        }
    }

    /**
     * The bot only fires after taking the wall stairs, but may stand on any
     * legal tile within three squares of its catapult. An exact hard-coded
     * tile is unreliable when scenery or other players obstruct it.
     */
    static boolean canOperateCatapultFrom(Position position, CastleWarsManager.Team team) {
        if (position == null || team == null || position.getPlane() != 0
                || CastleWarsManager.getCastleTeamAtPosition(position) != team
                || (WalkingCollisionMap.getTileFlags(
                        position.getX(), position.getY(), 0) & 0x1280100) != 0) {
            return false;
        }
        Position catapult = team == CastleWarsManager.Team.SARADOMIN
                ? CastleWarsEngineeringManager.SARADOMIN_CATAPULT
                : CastleWarsEngineeringManager.ZAMORAK_CATAPULT;
        return GameUtil.getDistance(position, catapult) <= 3;
    }

    static Position findCatapultBotOperatingPosition(
            CastleWarsManager.Team team, Position origin, Predicate<Position> canReach) {
        if (team == null || origin == null || origin.getPlane() != 0) {
            return null;
        }
        Position catapult = team == CastleWarsManager.Team.SARADOMIN
                ? CastleWarsEngineeringManager.SARADOMIN_CATAPULT
                : CastleWarsEngineeringManager.ZAMORAK_CATAPULT;
        Position preferred = team == CastleWarsManager.Team.SARADOMIN
                ? new Position(2415, 3087, 0) : new Position(2382, 3118, 0);
        Position best = null;
        int bestScore = Integer.MAX_VALUE;
        for (int x = catapult.getX() - 3; x <= catapult.getX() + 3; ++x) {
            for (int y = catapult.getY() - 3; y <= catapult.getY() + 3; ++y) {
                Position candidate = new Position(x, y, 0);
                if (!canOperateCatapultFrom(candidate, team)) {
                    continue;
                }
                int score = 5 * GameUtil.getDistance(candidate, preferred)
                        + GameUtil.getDistance(candidate, origin);
                if (!CastleWarsEngineeringManager.isBattlementWalkwayTile(candidate, team)) {
                    score += 100;
                }
                if (score < bestScore && canReach.test(candidate)) {
                    bestScore = score;
                    best = candidate;
                }
            }
        }
        return best;
    }

    private static void processCatapultRestock(BotPlayer bot, RoleState state) {
        Position supply = state.team == CastleWarsManager.Team.SARADOMIN
                ? new Position(2426, 3075, 0) : new Position(2373, 3131, 0);
        if (bot.getPosition().getPlane() != 0) {
            state.phase = Phase.DESCEND_HOME;
            state.repathDelay = 0;
            return;
        }
        Position wallLanding = state.team == CastleWarsManager.Team.SARADOMIN
                ? new Position(2416, 3074, 0) : new Position(2383, 3133, 0);
        if (state.wallAccessed || near(bot, wallLanding, 1)
                && !PathFinder.isReachable(bot, supply.getX(), supply.getY())) {
            if (!reachInteractionApproach(bot, state, wallLanding)) {
                return;
            }
            if (CastleWarsManager.moveBotThroughGroundCastleStairs(
                    bot, state.team, true)) {
                CastleWarsManager.setPlayerOnBattlements(bot, false);
                state.wallAccessed = false;
                state.repathDelay = 0;
            }
            return;
        }
        if (!near(bot, supply, 4)) {
            // Return through the actual wall-access stairs to the supply room,
            // rather than trying to path through the battlement collision.
            walk(bot, state, supply);
            return;
        }
        makeInventorySpace(bot, 8);
        int rocksGranted = CastleWarsEngineeringManager.giveSupply(bot,
                CastleWarsEngineeringManager.ROCK_ITEM_ID, 6 - Math.min(6,
                        bot.getInventoryManager().getItemAmount(CastleWarsEngineeringManager.ROCK_ITEM_ID)));
        if (rocksGranted > 0) {
            bot.getUpdateState().setAnimation(881);
        }
        if (bot.getInventoryManager().getItemAmount(CastleWarsEngineeringManager.TOOLKIT_ID) <= 0) {
            CastleWarsEngineeringManager.giveSupply(bot, CastleWarsEngineeringManager.TOOLKIT_ID, 1);
        }
        if (bot.getInventoryManager().getItemAmount(CastleWarsEngineeringManager.TINDERBOX_ITEM_ID) <= 0) {
            CastleWarsEngineeringManager.giveSupply(bot, CastleWarsEngineeringManager.TINDERBOX_ITEM_ID, 1);
        }
        state.wallAccessed = false;
        CastleWarsManager.setPlayerOnBattlements(bot, false);
        state.phase = Phase.CATAPULT_MOVE;
        state.repathDelay = 0;
    }

    private static void processCatapultRoam(BotPlayer bot, RoleState state) {
        if (tryEngageNearbyOpponent(bot, state, 10)) {
            return;
        }
        Position mid = new Position(
                2395 + GameUtil.randomInt(12),
                3099 + GameUtil.randomInt(12),
                0);
        if (GameUtil.randomInt(5) == 0) {
            walk(bot, state, mid);
        }
    }

    private static void processUndergroundDescent(BotPlayer bot, RoleState state) {
        if (bot.getPosition().getY() >= 9400) {
            state.phase = Phase.UNDERGROUND_OUT;
            state.undergroundStage = 0;
            state.repathDelay = 0;
            return;
        }

        // Stand on the exact staircase interaction square before clicking the
        // tunnel entrance. Using near(..., 1) let bots activate the stairs from
        // diagonal tiles, which looked like a teleport into the tunnel.
        Position approach = state.team == CastleWarsManager.Team.SARADOMIN
                ? new Position(2430, 3081, 0)
                : new Position(2369, 3126, 0);
        if (!reachInteractionApproach(bot, state, approach)) {
            return;
        }

        int objectX = state.team == CastleWarsManager.Team.SARADOMIN ? 2430 : 2369;
        int objectY = state.team == CastleWarsManager.Team.SARADOMIN ? 3082 : 3125;
        if (CastleWarsManager.handleFirstObjectAction(bot, 4912, objectX, objectY)) {
            state.repathDelay = 0;
        }
    }

    private static boolean prioritizeMeleeRockClearing(BotPlayer bot, RoleState state) {
        if (!isUndergroundTunnel(bot)) {
            return false;
        }

        boolean returningHome;
        if (state.phase == Phase.UNDERGROUND_OUT) {
            returningHome = false;
        } else if (state.phase == Phase.UNDERGROUND_BACK) {
            returningHome = true;
        } else {
            return false;
        }

        int[] rocks = undergroundRockRoute(state.team, state.routeVariant, returningHome);
        int rockIndex;
        if (state.undergroundStage == 0) {
            rockIndex = rocks[0];
        } else if (state.undergroundStage == 2) {
            rockIndex = rocks[1];
        } else {
            return false;
        }

        if (!CastleWarsEngineeringManager.isRockslideCollapsed(rockIndex)) {
            return false;
        }

        Position rock = CastleWarsEngineeringManager.getRockslidePosition(rockIndex);
        if (rock == null) {
            return false;
        }

        // Clearing the route is more important than taking an ordinary tunnel fight.
        if (bot.getCombatTarget() != null) {
            CombatManager.stopCombat(bot);
        }
        state.sightChaseTarget = null;
        state.sightChaseTicks = 0;

        if (!near(bot, rock, 3)) {
            walk(bot, state, rock);
            return true;
        }

        if (CastleWarsEngineeringManager.clearRockslideWithPickaxe(bot, rockIndex)) {
            state.delayTicks = 2;
            state.repathDelay = 0;
            CastleWarsBotChat.sayUnderground(bot);
            return true;
        }

        // This should be rare because underground bots stock a bronze pickaxe,
        // but do not deadlock the route if the tool has somehow disappeared.
        return false;
    }

    private static boolean tryCollapseRockslideOnOpponent(BotPlayer bot, RoleState state) {
        if (!isUndergroundTunnel(bot)
                || bot.getInventoryManager().getItemAmount(
                CastleWarsEngineeringManager.EXPLOSIVE_POTION_ID) <= 0) {
            return false;
        }

        int bestRock = -1;
        int bestEnemyDistance = Integer.MAX_VALUE;

        for (int rockIndex = 0; rockIndex < 4; ++rockIndex) {
            if (CastleWarsEngineeringManager.isRockslideCollapsed(rockIndex)) {
                continue;
            }

            Position rock = CastleWarsEngineeringManager.getRockslidePosition(rockIndex);
            if (rock == null) {
                continue;
            }

            int botDistance = GameUtil.getDistance(bot.getPosition(), rock);
            // crushPlayersAtRockslide() kills everyone within one tile, including
            // the bot that caused it, so never collapse while standing in that zone.
            if (botDistance < 2 || botDistance > 4) {
                continue;
            }

            boolean enemyInCrushZone = false;
            boolean friendlyInCrushZone = false;
            int nearestEnemy = Integer.MAX_VALUE;

            for (Player player : CastleWarsManager.getGamePlayersView()) {
                if (player == null || player == bot || player.isDead()
                        || !player.isRegistered()
                        || player.getPosition().getPlane() != rock.getPlane()) {
                    continue;
                }

                int rockDistance = GameUtil.getDistance(player.getPosition(), rock);
                if (rockDistance > 1) {
                    continue;
                }

                if (CastleWarsManager.areOpponents(bot, player)) {
                    enemyInCrushZone = true;
                    nearestEnemy = Math.min(nearestEnemy, rockDistance);
                } else if (CastleWarsManager.getGameTeam(player) == state.team) {
                    friendlyInCrushZone = true;
                }
            }

            if (enemyInCrushZone && !friendlyInCrushZone
                    && nearestEnemy < bestEnemyDistance) {
                bestRock = rockIndex;
                bestEnemyDistance = nearestEnemy;
            }
        }

        if (bestRock < 0) {
            return false;
        }

        if (bot.getCombatTarget() != null) {
            CombatManager.stopCombat(bot);
        }
        state.sightChaseTarget = null;
        state.sightChaseTicks = 0;

        if (CastleWarsEngineeringManager.collapseRockslide(bot, bestRock)) {
            state.pendingCollapseRock = -1;
            state.delayTicks = 2;
            state.repathDelay = 0;
            CastleWarsBotChat.sayUnderground(bot);
            return true;
        }
        return false;
    }

    private static boolean isUndergroundTunnel(BotPlayer bot) {
        return bot != null
                && bot.getPosition().getPlane() == 0
                && bot.getPosition().getY() >= 9400;
    }

    private static void processUndergroundCross(BotPlayer bot, RoleState state, boolean returningHome) {
        if (bot.getPosition().getY() < 9400) {
            if (returningHome) {
                state.phase = Phase.HOME_CLIMB;
            } else {
                state.phase = Phase.ENEMY_CLIMB;
            }
            return;
        }

        int[] rocks = undergroundRockRoute(state.team, state.routeVariant, returningHome);
        Position center = undergroundCenter(state.routeVariant);

        if (state.pendingCollapseRock >= 0) {
            Position previous = CastleWarsEngineeringManager.getRockslidePosition(state.pendingCollapseRock);
            int previousDistance = previous == null ? Integer.MAX_VALUE
                    : GameUtil.getDistance(bot.getPosition(), previous);
            if (previous != null
                    && previousDistance >= 2 && previousDistance <= 4
                    && !CastleWarsEngineeringManager.isRockslideCollapsed(state.pendingCollapseRock)
                    && bot.getInventoryManager().getItemAmount(
                            CastleWarsEngineeringManager.EXPLOSIVE_POTION_ID) > 1
                    && GameUtil.randomInt(3) == 0) {
                CastleWarsEngineeringManager.collapseRockslide(bot, state.pendingCollapseRock);
                state.pendingCollapseRock = -1;
                CastleWarsBotChat.sayUnderground(bot);
            }
        }

        if (state.undergroundStage == 0) {
            if (processRockPoint(bot, state, rocks[0])) {
                state.pendingCollapseRock = rocks[0];
                state.undergroundStage = 1;
            }
            return;
        }
        if (state.undergroundStage == 1) {
            if (!near(bot, center, 3)) {
                walk(bot, state, center);
                return;
            }
            state.undergroundStage = 2;
            state.repathDelay = 0;
            return;
        }
        if (state.undergroundStage == 2) {
            if (processRockPoint(bot, state, rocks[1])) {
                state.pendingCollapseRock = rocks[1];
                state.undergroundStage = 3;
            }
            return;
        }

        CastleWarsManager.Team destinationTeam =
                returningHome ? state.team : opposite(state.team);
        Position exitApproach = destinationTeam == CastleWarsManager.Team.SARADOMIN
                ? new Position(2430, 9483, 0)
                : new Position(2369, 9524, 0);
        if (!reachInteractionApproach(bot, state, exitApproach)) {
            return;
        }

        int exitX = destinationTeam == CastleWarsManager.Team.SARADOMIN ? 2430 : 2369;
        int exitY = destinationTeam == CastleWarsManager.Team.SARADOMIN ? 9482 : 9525;
        if (!CastleWarsManager.handleFirstObjectAction(bot, 1757, exitX, exitY)) {
            return;
        }

        // The staircase action moves the bot back to the surface immediately.
        // Only advance the route after that transition has actually happened.
        if (bot.getPosition().getY() >= 9400) {
            return;
        }
        state.undergroundStage = 0;
        state.pendingCollapseRock = -1;
        state.repathDelay = 0;
        state.phase = returningHome ? Phase.HOME_CLIMB : Phase.ENEMY_CLIMB;
    }

    private static boolean processRockPoint(BotPlayer bot, RoleState state, int rockIndex) {
        Position rock = CastleWarsEngineeringManager.getRockslidePosition(rockIndex);
        if (rock == null) {
            return true;
        }
        if (!near(bot, rock, 3)) {
            walk(bot, state, rock);
            return false;
        }
        if (CastleWarsEngineeringManager.isRockslideCollapsed(rockIndex)) {
            boolean started = bot.botPrimaryCombatStyle == 0
                    ? CastleWarsEngineeringManager.clearRockslideWithPickaxe(bot, rockIndex)
                    : CastleWarsEngineeringManager.clearRockslide(bot, rockIndex);
            if (!started) {
                return false;
            }
            state.delayTicks = 2;
            // Pickaxe clearing is asynchronous. Do not advance the bot's tunnel
            // route until the passage actually opens; repeated calls only wait.
            if (CastleWarsEngineeringManager.isRockslideCollapsed(rockIndex)) {
                return false;
            }
            CastleWarsBotChat.sayUnderground(bot);
        }
        return true;
    }

    private static void processUndergroundReturnDescent(BotPlayer bot, RoleState state) {
        if (bot.getPosition().getPlane() > 0) {
            processCastleClimb(bot, state, opposite(state.team), false, Phase.UNDERGROUND_RETURN_DESCEND);
            return;
        }

        CastleWarsManager.Team enemy = opposite(state.team);
        Position approach = enemy == CastleWarsManager.Team.SARADOMIN
                ? new Position(2430, 3081, 0)
                : new Position(2369, 3126, 0);
        if (!reachInteractionApproach(bot, state, approach)) {
            return;
        }

        int objectX = enemy == CastleWarsManager.Team.SARADOMIN ? 2430 : 2369;
        int objectY = enemy == CastleWarsManager.Team.SARADOMIN ? 3082 : 3125;
        if (!CastleWarsManager.handleFirstObjectAction(bot, 4912, objectX, objectY)) {
            return;
        }
        if (bot.getPosition().getY() < 9400) {
            return;
        }

        state.undergroundStage = 0;
        state.phase = Phase.UNDERGROUND_BACK;
        state.repathDelay = 0;
    }

    private static void processTakeFlag(BotPlayer bot, RoleState state) {
        CastleWarsManager.Team enemy = opposite(state.team);
        Position flag = enemy == CastleWarsManager.Team.SARADOMIN
                ? new Position(2429, 3074, 3)
                : new Position(2370, 3133, 3);
        Position approach = CastleWarsManager.getNearestAdjacentInteractionTile(bot, flag);
        if (approach == null || !reachInteractionApproach(bot, state, approach)) {
            return;
        }

        if (CastleWarsManager.isFlagAtBase(enemy)) {
            if (!CastleWarsManager.takeEnemyFlag(bot)) {
                return;
            }
            CastleWarsBotChat.sayFlagCarrier(bot);
        }
        state.phase = Phase.ENEMY_DESCEND;
        state.repathDelay = 0;
    }

    private static void processCapture(BotPlayer bot, RoleState state) {
        Position ownFlag = state.team == CastleWarsManager.Team.SARADOMIN
                ? new Position(2429, 3074, 3)
                : new Position(2370, 3133, 3);
        Position approach = CastleWarsManager.getNearestAdjacentInteractionTile(bot, ownFlag);
        if (approach == null || !reachInteractionApproach(bot, state, approach)) {
            return;
        }

        if (CastleWarsManager.isCarryingEnemyFlag(bot)
                && CastleWarsManager.tryCaptureFlag(bot)) {
            CastleWarsBotChat.sayScore(bot);
        }
        state.resetForSpawn();
    }

    private static void processCastleClimb(BotPlayer bot, RoleState state,
                                           CastleWarsManager.Team castleTeam,
                                           boolean up, Phase completePhase) {
        int plane = bot.getPosition().getPlane();
        if (up && plane >= 3) {
            state.phase = completePhase;
            state.repathDelay = 0;
            return;
        }
        if (!up && plane <= 0) {
            state.phase = completePhase;
            state.repathDelay = 0;
            return;
        }

        if (castleTeam == CastleWarsManager.Team.SARADOMIN) {
            if (up) {
                if (plane == 0) {
                    useTraversal(bot, state, new Position(2419, 3077, 0), 4417, 2419, 3078);
                } else if (plane == 1) {
                    useTraversal(bot, state, new Position(2427, 3081, 1), 4417, 2428, 3081);
                } else if (plane == 2) {
                    useTraversal(bot, state, new Position(2425, 3077, 2), 4417, 2425, 3074);
                }
            } else {
                if (plane == 3) {
                    useTraversal(bot, state, new Position(2426, 3074, 3), 4415, 2425, 3074);
                } else if (plane == 2) {
                    useTraversal(bot, state, new Position(2430, 3080, 2), 4415, 2430, 3081);
                } else if (plane == 1) {
                    useTraversal(bot, state, new Position(2420, 3080, 1), 4415, 2419, 3080);
                }
            }
        } else {
            if (up) {
                if (plane == 0) {
                    useTraversal(bot, state, new Position(2380, 3130, 0), 4418, 2380, 3127);
                } else if (plane == 1) {
                    useTraversal(bot, state, new Position(2372, 3126, 1), 4418, 2369, 3126);
                } else if (plane == 2) {
                    useTraversal(bot, state, new Position(2374, 3130, 2), 4418, 2374, 3131);
                }
            } else {
                if (plane == 3) {
                    useTraversal(bot, state, new Position(2373, 3133, 3), 4415, 2374, 3133);
                } else if (plane == 2) {
                    useTraversal(bot, state, new Position(2369, 3127, 2), 4415, 2369, 3126);
                } else if (plane == 1) {
                    useTraversal(bot, state, new Position(2379, 3127, 1), 4415, 2380, 3127);
                }
            }
        }
    }

    private static void useTraversal(BotPlayer bot, RoleState state, Position approach,
                                     int objectId, int objectX, int objectY) {
        if (!reachInteractionApproach(bot, state, approach)) {
            return;
        }
        CastleWarsManager.handleFirstObjectAction(bot, objectId, objectX, objectY);
        state.repathDelay = 0;
    }

    private static int[] undergroundRockRoute(CastleWarsManager.Team team,
                                               int routeVariant,
                                               boolean returningHome) {
        int[] outward;
        if (team == CastleWarsManager.Team.SARADOMIN) {
            outward = routeVariant == 0 ? new int[]{3, 0} : new int[]{2, 1};
        } else {
            outward = routeVariant == 0 ? new int[]{0, 3} : new int[]{1, 2};
        }
        if (!returningHome) {
            return outward;
        }
        return new int[]{outward[1], outward[0]};
    }

    private static Position undergroundCenter(int routeVariant) {
        return routeVariant == 0
                ? new Position(2398, 9499, 0)
                : new Position(2400, 9508, 0);
    }

    private static boolean isTraversalPhase(Phase phase) {
        switch (phase) {
            case CATAPULT_MOVE:
            case CATAPULT_RESTOCK:
            case LEAVE_SPAWN:
            case DESCEND_HOME:
            case EXIT_HOME:
            case DEFENDER_CLIMB:
            case DEFENDER_DEPLOY_GROUND:
            case UNDERGROUND_DESCEND:
            case ENEMY_CLIMB:
            case ENEMY_DESCEND:
            case UNDERGROUND_RETURN_DESCEND:
            case HOME_CLIMB:
                return true;
            default:
                return false;
        }
    }

    private static boolean stockUndergroundSupplies(BotPlayer bot) {
        makeInventorySpace(bot, 8);

        int pickaxes = bot.getInventoryManager().getItemAmount(
                CastleWarsEngineeringManager.BRONZE_PICKAXE_ID);
        if (pickaxes < 1) {
            CastleWarsEngineeringManager.giveSupply(bot,
                    CastleWarsEngineeringManager.BRONZE_PICKAXE_ID, 1 - pickaxes);
        }

        int explosives = bot.getInventoryManager().getItemAmount(
                CastleWarsEngineeringManager.EXPLOSIVE_POTION_ID);
        if (explosives < 4) {
            CastleWarsEngineeringManager.giveSupply(bot,
                    CastleWarsEngineeringManager.EXPLOSIVE_POTION_ID, 4 - explosives);
        }

        if (bot.getInventoryManager().getItemAmount(
                CastleWarsEngineeringManager.BARRICADE_ITEM_ID) < 1) {
            CastleWarsEngineeringManager.giveSupply(bot,
                    CastleWarsEngineeringManager.BARRICADE_ITEM_ID, 1);
        }
        if (bot.getInventoryManager().getItemAmount(
                CastleWarsEngineeringManager.TINDERBOX_ITEM_ID) < 1) {
            CastleWarsEngineeringManager.giveSupply(bot,
                    CastleWarsEngineeringManager.TINDERBOX_ITEM_ID, 1);
        }

        return bot.getInventoryManager().getItemAmount(
                CastleWarsEngineeringManager.BRONZE_PICKAXE_ID) >= 1
                && bot.getInventoryManager().getItemAmount(
                CastleWarsEngineeringManager.EXPLOSIVE_POTION_ID) >= 4
                && bot.getInventoryManager().getItemAmount(
                CastleWarsEngineeringManager.TINDERBOX_ITEM_ID) >= 1;
    }

    private static boolean shouldUseBandage(BotPlayer bot) {
        if (bot.getInventoryManager().getItemAmount(4049) <= 0) {
            return false;
        }
        return bot.getCurrentHitpoints() * 100 <= bot.getMaxHitpoints() * 60
                || bot.getRunEnergyPercent() <= 30;
    }

    private static void makeInventorySpace(BotPlayer bot, int desiredFreeSlots) {
        int free = bot.getInventoryManager().getContainer().getFreeSlots();
        if (free >= desiredFreeSlots) {
            return;
        }

        if (bot.botFoodItemId > 0) {
            int food = bot.getInventoryManager().getItemAmount(bot.botFoodItemId);
            int remove = Math.min(food, desiredFreeSlots - free);
            if (remove > 0) {
                bot.getInventoryManager().removeItem(new ItemStack(bot.botFoodItemId, remove));
                free = bot.getInventoryManager().getContainer().getFreeSlots();
            }
        }

        if (free < desiredFreeSlots) {
            int bandages = bot.getInventoryManager().getItemAmount(4049);
            int remove = Math.min(bandages, desiredFreeSlots - free);
            if (remove > 0) {
                bot.getInventoryManager().removeItem(new ItemStack(4049, remove));
            }
        }
    }

    private static boolean processOwnDroppedFlagRecovery(BotPlayer bot, RoleState state) {
        if (isTraversalPhase(state.phase)
                || CastleWarsManager.isCarryingFlag(bot)
                || CastleWarsManager.isInTeamSpawnArea(bot, state.team)) {
            return false;
        }
        GroundItem droppedFlag = CastleWarsManager.getDroppedFlagGroundItem(state.team);
        if (droppedFlag == null) {
            return false;
        }

        Position flagPosition = droppedFlag.getPosition();
        if (bot.getCombatTarget() != null) {
            CombatManager.stopCombat(bot);
        }
        bot.getMovementQueue().setRunning(true);

        if (bot.getPosition().getPlane() == flagPosition.getPlane()
                && GameUtil.getDistance(bot.getPosition(), flagPosition) <= 1) {
            CastleWarsManager.handleDroppedFlagPickup(bot, droppedFlag);
            state.repathDelay = 0;
            return true;
        }

        if (navigateFlagRecovery(bot, state, flagPosition)) {
            state.repathDelay = 0;
            return true;
        }
        walk(bot, state, flagPosition);
        return true;
    }

    private static boolean navigateFlagRecovery(BotPlayer bot, RoleState state,
                                                Position targetPosition) {
        Position botPosition = bot.getPosition();
        CastleWarsManager.Team botCastle =
                CastleWarsManager.getCastleTeamAtPosition(botPosition);
        CastleWarsManager.Team targetCastle =
                CastleWarsManager.getCastleTeamAtPosition(targetPosition);
        int botPlane = botPosition.getPlane();
        int targetPlane = targetPosition.getPlane();

        if (botPlane > 0 && botCastle != null
                && (botPlane > targetPlane || botCastle != targetCastle)) {
            return CastleWarsManager.routeBotOneCastleLevel(bot, botCastle, false);
        }
        if (botPlane == 0 && botCastle != null && botCastle != targetCastle) {
            Position exterior = CastleWarsManager.getBotMainDoorExteriorPosition(bot, botCastle);
            return state.role == Role.UNDERGROUND
                    && CastleWarsBotBarricadeAi.handleBlockedRoute(bot, exterior)
                    || CastleWarsManager.routeBotThroughGroundCastle(bot, botCastle, false);
        }
        if (botPlane == 0 && botCastle == null && targetCastle != null) {
            Position interior = CastleWarsManager.getBotMainDoorInteriorPosition(bot, targetCastle);
            return state.role == Role.UNDERGROUND
                    && CastleWarsBotBarricadeAi.handleBlockedRoute(bot, interior)
                    || CastleWarsManager.routeBotThroughGroundCastle(bot, targetCastle, true);
        }
        if (botPlane < targetPlane && botCastle != null && botCastle == targetCastle) {
            return CastleWarsManager.routeBotOneCastleLevel(bot, botCastle, true);
        }
        if (botPlane > targetPlane && botCastle != null) {
            return CastleWarsManager.routeBotOneCastleLevel(bot, botCastle, false);
        }
        return false;
    }

    private static boolean processFlagCarrierFocus(BotPlayer bot, RoleState state) {
        if (isTraversalPhase(state.phase)
                || CastleWarsManager.isCarryingFlag(bot)
                || CastleWarsManager.isInTeamSpawnArea(bot, state.team)) {
            return false;
        }

        Player enemyCarrier = findEnemyFlagCarrier(bot, state.team);
        if (enemyCarrier != null) {
            state.sightChaseTarget = null;
            state.sightChaseTicks = 0;
            if (bot.getCombatTarget() != null && bot.getCombatTarget() != enemyCarrier) {
                CombatManager.stopCombat(bot);
            }
            bot.getMovementQueue().setRunning(true);
            if (CastleWarsManager.hasBotCombatLineOfSight(bot, enemyCarrier)) {
                CombatManager.startCombat(bot, enemyCarrier);
                return true;
            }
            if (navigateSightChase(bot, state, enemyCarrier)) {
                state.repathDelay = 0;
                return true;
            }
            walk(bot, state, enemyCarrier.getPosition());
            return true;
        }

        Player friendlyCarrier = findFriendlyFlagCarrier(bot, state.team);
        if (friendlyCarrier == null) {
            return false;
        }

        Player threat = findFlagCarrierThreat(bot, friendlyCarrier);
        if (threat != null) {
            if (bot.getCombatTarget() != null && bot.getCombatTarget() != threat) {
                CombatManager.stopCombat(bot);
            }
            bot.getMovementQueue().setRunning(true);
            if (CastleWarsManager.hasBotCombatLineOfSight(bot, threat)) {
                CombatManager.startCombat(bot, threat);
                return true;
            }
            if (navigateSightChase(bot, state, threat)) {
                state.repathDelay = 0;
                return true;
            }
            walk(bot, state, threat.getPosition());
            return true;
        }

        if (bot.getCombatTarget() != null) {
            CombatManager.stopCombat(bot);
        }
        int escortDistance = GameUtil.getDistance(bot.getPosition(), friendlyCarrier.getPosition());
        if (bot.getPosition().getPlane() != friendlyCarrier.getPosition().getPlane()
                || escortDistance > 4) {
            bot.getMovementQueue().setRunning(true);
            if (navigateSightChase(bot, state, friendlyCarrier)) {
                state.repathDelay = 0;
                return true;
            }
            walk(bot, state, friendlyCarrier.getPosition());
        }
        return true;
    }

    private static Player findEnemyFlagCarrier(BotPlayer bot, CastleWarsManager.Team team) {
        Player holder = CastleWarsManager.getFlagHolder(team);
        if (isEnemyCarrier(bot, holder)) {
            return holder;
        }
        holder = CastleWarsManager.getFlagHolder(opposite(team));
        return isEnemyCarrier(bot, holder) ? holder : null;
    }

    private static Player findFriendlyFlagCarrier(BotPlayer bot, CastleWarsManager.Team team) {
        Player holder = CastleWarsManager.getFlagHolder(opposite(team));
        if (isFriendlyCarrier(bot, team, holder)) {
            return holder;
        }
        holder = CastleWarsManager.getFlagHolder(team);
        return isFriendlyCarrier(bot, team, holder) ? holder : null;
    }

    private static boolean isEnemyCarrier(BotPlayer bot, Player holder) {
        return holder != null && holder != bot && holder.isRegistered() && !holder.isDead()
                && CastleWarsManager.areOpponents(bot, holder);
    }

    private static boolean isFriendlyCarrier(BotPlayer bot, CastleWarsManager.Team team,
                                             Player holder) {
        return holder != null && holder != bot && holder.isRegistered() && !holder.isDead()
                && CastleWarsManager.getGameTeam(holder) == team;
    }

    private static Player findFlagCarrierThreat(BotPlayer bot, Player carrier) {
        Player best = null;
        int bestScore = Integer.MAX_VALUE;
        for (Player player : CastleWarsManager.getGamePlayersView()) {
            if (player == null || player == bot || player.isDead()
                    || !player.isRegistered() || !CastleWarsManager.areOpponents(bot, player)) {
                continue;
            }
            int carrierDistance = GameUtil.getDistance(carrier.getPosition(), player.getPosition());
            if (carrierDistance > 10) {
                continue;
            }
            int score = carrierDistance * 4
                    + GameUtil.getDistance(bot.getPosition(), player.getPosition());
            if (player.getCombatTarget() == carrier) {
                score -= 30;
            }
            if (score < bestScore) {
                best = player;
                bestScore = score;
            }
        }
        return best;
    }

    private static boolean hasActiveOpponent(BotPlayer bot, RoleState state) {
        Entity target = bot.getCombatTarget();
        if (target == null || target.isDead() || !target.isPlayer()) {
            return false;
        }
        Player targetPlayer = (Player)target;
        if (!CastleWarsManager.areOpponents(bot, targetPlayer)) {
            CombatManager.stopCombat(bot);
            return false;
        }
        int targetDistance = GameUtil.getDistance(
                bot.getPosition(), targetPlayer.getPosition());
        if (state.role == Role.WALL_GUARD) {
            int wallRange = CastleWarsManager.getBotCastleWallEngageRange(bot);
            if (wallRange <= 0 || targetDistance > wallRange) {
                CombatManager.stopCombat(bot);
                return false;
            }
        } else if (targetDistance > 12) {
            CombatManager.stopCombat(bot);
            return false;
        }
        if (!CastleWarsManager.hasBotCombatLineOfSight(bot, targetPlayer)) {
            if (state.role != Role.WALL_GUARD && state.role != Role.DEFENDER) {
                state.sightChaseTarget = targetPlayer;
                state.sightChaseTicks = 36;
            }
            CombatManager.stopCombat(bot);
            return false;
        }
        state.sightChaseTarget = null;
        state.sightChaseTicks = 0;
        return true;
    }

    private static boolean processSightChase(BotPlayer bot, RoleState state) {
        Player target = state.sightChaseTarget;
        if (target == null) {
            return false;
        }
        if (target.isDead() || !target.isRegistered()
                || !CastleWarsManager.areOpponents(bot, target)
                || GameUtil.getDistance(bot.getPosition(), target.getPosition()) > 20
                || state.sightChaseTicks-- <= 0) {
            state.sightChaseTarget = null;
            state.sightChaseTicks = 0;
            return false;
        }

        if (CastleWarsManager.hasBotCombatLineOfSight(bot, target)) {
            state.sightChaseTarget = null;
            state.sightChaseTicks = 0;
            bot.getMovementQueue().setRunning(true);
            CombatManager.startCombat(bot, target);
            return true;
        }

        if (bot.getCombatTarget() != null) {
            CombatManager.stopCombat(bot);
        }
        if (navigateSightChase(bot, state, target)) {
            state.repathDelay = 0;
            return true;
        }
        walk(bot, state, target.getPosition());
        return true;
    }

    private static boolean navigateSightChase(BotPlayer bot, RoleState state, Player target) {
        Position botPosition = bot.getPosition();
        Position targetPosition = target.getPosition();
        CastleWarsManager.Team botCastle =
                CastleWarsManager.getCastleTeamAtPosition(botPosition);
        CastleWarsManager.Team targetCastle =
                CastleWarsManager.getCastleTeamAtPosition(targetPosition);
        int botPlane = botPosition.getPlane();
        int targetPlane = targetPosition.getPlane();

        if (botPlane > 0 && botCastle != null
                && (botPlane > targetPlane || botCastle != targetCastle)) {
            return CastleWarsManager.routeBotOneCastleLevel(bot, botCastle, false);
        }

        if (botPlane == 0 && botCastle != null && botCastle != targetCastle) {
            return CastleWarsManager.routeBotThroughGroundCastle(bot, botCastle, false);
        }

        if (botPlane == 0 && botCastle == null && targetCastle != null) {
            return CastleWarsManager.routeBotThroughGroundCastle(bot, targetCastle, true);
        }

        if (botPlane < targetPlane && botCastle != null && botCastle == targetCastle) {
            return CastleWarsManager.routeBotOneCastleLevel(bot, botCastle, true);
        }

        if (botPlane > targetPlane && botCastle != null) {
            return CastleWarsManager.routeBotOneCastleLevel(bot, botCastle, false);
        }
        return false;
    }

    private static boolean tryEngageNearbyOpponent(BotPlayer bot, RoleState state, int radius) {
        Player bestVisible = null;
        Player bestHidden = null;
        int bestVisibleDistance = Integer.MAX_VALUE;
        int bestHiddenDistance = Integer.MAX_VALUE;
        for (Player player : CastleWarsManager.getGamePlayersView()) {
            if (player == null || player == bot || player.isDead()
                    || !CastleWarsManager.areOpponents(bot, player)) {
                continue;
            }
            int distance = GameUtil.getDistance(bot.getPosition(), player.getPosition());
            if (distance > radius) {
                continue;
            }
            if (state.role == Role.WALL_GUARD) {
                int wallRange = CastleWarsManager.getBotCastleWallEngageRange(bot);
                if (wallRange <= 0 || distance > wallRange) {
                    continue;
                }
            }
            if (CastleWarsManager.hasBotCombatLineOfSight(bot, player)) {
                if (distance < bestVisibleDistance) {
                    bestVisible = player;
                    bestVisibleDistance = distance;
                }
            } else if (state.role != Role.WALL_GUARD
                    && state.role != Role.DEFENDER
                    && distance < bestHiddenDistance) {
                bestHidden = player;
                bestHiddenDistance = distance;
            }
        }
        if (bestVisible != null) {
            bot.getMovementQueue().setRunning(true);
            CombatManager.startCombat(bot, bestVisible);
            return true;
        }
        if (bestHidden != null) {
            state.sightChaseTarget = bestHidden;
            state.sightChaseTicks = 36;
            return processSightChase(bot, state);
        }
        return false;
    }

    private static boolean reachInteractionApproach(BotPlayer bot, RoleState state,
                                                     Position approach) {
        if (approach == null || bot.getPosition().getPlane() != approach.getPlane()) {
            return false;
        }
        if (near(bot, approach, 0)) {
            return true;
        }
        if ((state.role == Role.UNDERGROUND || state.role == Role.CATAPULT)
                && CastleWarsBotBarricadeAi.handleBlockedRoute(bot, approach)) {
            return false;
        }
        if (state.repathDelay > 0) {
            --state.repathDelay;
            return false;
        }
        state.repathDelay = 2;
        bot.getMovementQueue().setRunning(true);
        boolean pathFound = PathFinder.findPath(bot, approach.getX(), approach.getY(), false, 0, 0);
        if (!pathFound && state.role == Role.CATAPULT && World.tickCount % 50 < 10
                && GameplayTrace.enabled()) {
            GameplayTrace.log("castle-wars catapult no path team=" + state.team
                    + " approach=" + approach + " operator=" + GameplayTrace.describe(bot));
        }
        bot.getMovementQueue().clearMovementActions();
        return false;
    }

    private static void walk(BotPlayer bot, RoleState state, Position target) {
        if (bot.getPosition().getPlane() != target.getPlane() || bot.isMovementLocked()) {
            return;
        }
        Position navigationTarget = target;
        Position steppingWaypoint = CastleWarsManager.getSteppingStoneShortcutWaypoint(
                bot.getPosition(), target);
        if (steppingWaypoint != null) {
            if (CastleWarsManager.jumpSteppingStone(bot, steppingWaypoint)) {
                state.repathDelay = 0;
                return;
            }
            navigationTarget = steppingWaypoint;
        }

        if ((state.role == Role.UNDERGROUND || state.role == Role.CATAPULT)
                && CastleWarsBotBarricadeAi.handleBlockedRoute(bot, navigationTarget)) {
            return;
        }
        if (state.repathDelay > 0) {
            --state.repathDelay;
            return;
        }
        state.repathDelay = 3 + GameUtil.randomInt(4);
        bot.getMovementQueue().setRunning(true);
        PathFinder.findPath(bot, navigationTarget.getX(), navigationTarget.getY(), true, 0, 0);
        bot.getMovementQueue().clearMovementActions();
    }

    private static boolean near(BotPlayer bot, Position target, int distance) {
        return bot.getPosition().getPlane() == target.getPlane()
                && GameUtil.isWithinDistance(bot.getPosition(), target, distance);
    }

    private static CastleWarsManager.Team opposite(CastleWarsManager.Team team) {
        return team == CastleWarsManager.Team.SARADOMIN
                ? CastleWarsManager.Team.ZAMORAK
                : CastleWarsManager.Team.SARADOMIN;
    }

    private static boolean isWallGuardCandidate(BotPlayer bot,
                                                       CastleWarsManager.Team team) {
        return bot != null
                && bot.botPrimaryCombatStyle != 0
                && containsNameHash(getWallGuardNameHashes(team), bot.getNameHash());
    }

    public static boolean isDedicatedFlagRunner(BotPlayer bot) {
        if (bot == null) {
            return false;
        }
        CastleWarsManager.Team team = CastleWarsManager.getGameTeam(bot);
        if (team == null) {
            return false;
        }
        refreshRoleRoster(team);
        if (bot == catapultOperators.get(team)) {
            return false;
        }

        BotPlayer dedicated = team == CastleWarsManager.Team.SARADOMIN
                ? saradominDedicatedFlagRunner : zamorakDedicatedFlagRunner;
        if (dedicated == null || dedicated.isDead() || !dedicated.isRegistered()
                || CastleWarsManager.getGameTeam(dedicated) != team) {
            dedicated = findDedicatedFlagRunner(team);
            if (team == CastleWarsManager.Team.SARADOMIN) {
                saradominDedicatedFlagRunner = dedicated;
            } else {
                zamorakDedicatedFlagRunner = dedicated;
            }
        }
        return bot == dedicated;
    }

    private static void refreshRoleRoster(CastleWarsManager.Team team) {
        Map<BotPlayer, Integer> roster = new IdentityHashMap<BotPlayer, Integer>();
        for (Player player : CastleWarsManager.getGamePlayersView()) {
            if (player instanceof BotPlayer && CastleWarsManager.getGameTeam(player) == team) {
                roster.put((BotPlayer)player, Integer.valueOf(player.botPrimaryCombatStyle));
            }
        }
        BotPlayer previousOperator = catapultOperators.get(team);
        BotPlayer operator = previousOperator;
        // Keep the same operator through respawns and late arrivals. If a
        // replacement is needed, leave any current flag carrier on its run.
        if (!roster.containsKey(operator)) {
            operator = null;
            for (BotPlayer candidate : roster.keySet()) {
                if (!CastleWarsManager.isCarryingFlag(candidate)
                        && (operator == null || candidate.getNameHash() < operator.getNameHash())) {
                    operator = candidate;
                }
            }
            catapultOperators.put(team, operator);
        }
        if (operator != previousOperator || !roster.equals(roleRosters.get(team))) {
            roleRosters.put(team, roster);
            Integer previous = roleGenerations.get(team);
            roleGenerations.put(team, Integer.valueOf(previous == null ? 1 : previous.intValue() + 1));
            if (team == CastleWarsManager.Team.SARADOMIN) {
                saradominDedicatedFlagRunner = null;
            } else {
                zamorakDedicatedFlagRunner = null;
            }
        }
    }

    private static BotPlayer findDedicatedFlagRunner(CastleWarsManager.Team team) {
        long[] wallGuardHashes = getWallGuardNameHashes(team);
        long[] specialistHashes = getSpecialistNonWallNameHashes(team, wallGuardHashes);
        BotPlayer firstAttacker = null;
        BotPlayer firstMeleeAttacker = null;

        for (Player player : CastleWarsManager.getGamePlayersView()) {
            if (!(player instanceof BotPlayer)
                    || CastleWarsManager.getGameTeam(player) != team) {
                continue;
            }
            BotPlayer other = (BotPlayer)player;
            long nameHash = other.getNameHash();
            if (other == catapultOperators.get(team) || containsNameHash(wallGuardHashes, nameHash)
                    || containsNameHash(specialistHashes, nameHash)) {
                continue;
            }
            if (firstAttacker == null
                    || nameHash < firstAttacker.getNameHash()) {
                firstAttacker = other;
            }
            if (other.botPrimaryCombatStyle == 0
                    && (firstMeleeAttacker == null
                    || nameHash < firstMeleeAttacker.getNameHash())) {
                firstMeleeAttacker = other;
            }
        }

        // Prefer a melee runner so the dedicated objective bot can personally
        // break a closed enemy main door instead of waiting for somebody else.
        return firstMeleeAttacker != null ? firstMeleeAttacker : firstAttacker;
    }

    /**
     * Don't allocate every ranged bot to the walls when teams are small.
     * Reserve one flag runner and (when possible) two castle defenders.
     */
    static int wallGuardCapacity(int teamBotCount) {
        if (teamBotCount < 3) {
            return 0;
        }
        int defenderReserve = teamBotCount >= 5 ? 2 : 1;
        int catapultReserve = teamBotCount >= 5 ? 1 : 0;
        // Leave two defenders, a catapult operator, and a flag runner when
        // at least five bots are present; otherwise all specialists can be
        // consumed by ranged wall guards and the catapult is never used.
        return Math.min(4, Math.max(0, teamBotCount
                - defenderReserve - catapultReserve - 1));
    }

    /** Leave at least one non-wall bot to run the enemy flag. */
    static int specialistCapacity(int nonWallBotCount) {
        return Math.min(9, Math.max(0, nonWallBotCount - 1));
    }

    private static long[] getWallGuardNameHashes(CastleWarsManager.Team team) {
        int botCount = 0;
        for (Player player : CastleWarsManager.getGamePlayersView()) {
            if (player instanceof BotPlayer
                    && CastleWarsManager.getGameTeam(player) == team) {
                ++botCount;
            }
        }
        long[] hashes = createEmptyHashSelection(
                Math.min(wallGuardCapacity(botCount), Math.max(0, botCount - 2)));
        for (Player player : CastleWarsManager.getGamePlayersView()) {
            if (!(player instanceof BotPlayer)
                    || CastleWarsManager.getGameTeam(player) != team) {
                continue;
            }
            BotPlayer bot = (BotPlayer)player;
            if (bot == catapultOperators.get(team) || bot.botPrimaryCombatStyle == 0) {
                continue;
            }
            insertLowestNameHash(hashes, bot.getNameHash());
        }
        return hashes;
    }

    private static long[] getSpecialistNonWallNameHashes(
            CastleWarsManager.Team team, long[] wallGuardHashes) {
        int nonWallBotCount = 0;
        for (Player player : CastleWarsManager.getGamePlayersView()) {
            if (player instanceof BotPlayer
                    && CastleWarsManager.getGameTeam(player) == team
                    && player != catapultOperators.get(team)
                    && !containsNameHash(wallGuardHashes, player.getNameHash())) {
                ++nonWallBotCount;
            }
        }
        long[] hashes = createEmptyHashSelection(Math.min(8, specialistCapacity(nonWallBotCount)));
        for (Player player : CastleWarsManager.getGamePlayersView()) {
            if (!(player instanceof BotPlayer)
                    || CastleWarsManager.getGameTeam(player) != team) {
                continue;
            }
            BotPlayer bot = (BotPlayer)player;
            if (bot == catapultOperators.get(team) || containsNameHash(wallGuardHashes, bot.getNameHash())) {
                continue;
            }
            insertLowestNameHash(hashes, bot.getNameHash());
        }
        return hashes;
    }

    private static long[] createEmptyHashSelection(int size) {
        long[] hashes = new long[size];
        for (int i = 0; i < hashes.length; ++i) {
            hashes[i] = Long.MAX_VALUE;
        }
        return hashes;
    }

    private static void insertLowestNameHash(long[] hashes, long nameHash) {
        for (int i = 0; i < hashes.length; ++i) {
            if (nameHash >= hashes[i]) {
                continue;
            }
            for (int j = hashes.length - 1; j > i; --j) {
                hashes[j] = hashes[j - 1];
            }
            hashes[i] = nameHash;
            return;
        }
    }

    private static boolean containsNameHash(long[] hashes, long nameHash) {
        for (long hash : hashes) {
            if (hash == nameHash) {
                return true;
            }
        }
        return false;
    }

    private static int getDefenderIndex(BotPlayer bot, CastleWarsManager.Team team) {
        long[] wallGuards = getWallGuardNameHashes(team);
        long[] specialistHashes = getSpecialistNonWallNameHashes(team, wallGuards);
        return specialistHashes.length > 1 && specialistHashes[1] == bot.getNameHash() ? 1 : 0;
    }

    private static Role assignRole(BotPlayer bot, CastleWarsManager.Team team) {
        if (isDedicatedFlagRunner(bot)) {
            return Role.ATTACKER;
        }
        if (bot == catapultOperators.get(team)) {
            return Role.CATAPULT;
        }
        long[] wallGuardHashes = getWallGuardNameHashes(team);
        if (containsNameHash(wallGuardHashes, bot.getNameHash())) {
            return Role.WALL_GUARD;
        }

        long[] specialistHashes = getSpecialistNonWallNameHashes(team, wallGuardHashes);
        for (int nonWallRank = 0; nonWallRank < specialistHashes.length; ++nonWallRank) {
            if (specialistHashes[nonWallRank] != bot.getNameHash()) {
                continue;
            }
            if (nonWallRank < 2) {
                return Role.DEFENDER;
            }
            return Role.UNDERGROUND;
        }

        // The remaining bots are the normal rushers. Alternate them by stable
        // name-hash rank so approximately 50% take the tunnel route while the
        // other 50% continue using the normal surface flag-runner AI.
        return shouldRusherUseUndergroundRoute(bot, team, wallGuardHashes, specialistHashes)
                ? Role.UNDERGROUND : Role.ATTACKER;
    }

    private static boolean shouldRusherUseUndergroundRoute(
            BotPlayer bot, CastleWarsManager.Team team,
            long[] wallGuardHashes, long[] specialistHashes) {
        long botHash = bot.getNameHash();
        int rusherRank = 0;

        for (Player player : CastleWarsManager.getGamePlayersView()) {
            if (!(player instanceof BotPlayer)
                    || CastleWarsManager.getGameTeam(player) != team) {
                continue;
            }

            BotPlayer candidate = (BotPlayer)player;
            long candidateHash = candidate.getNameHash();
            if (candidate == catapultOperators.get(team) || containsNameHash(wallGuardHashes, candidateHash)
                    || containsNameHash(specialistHashes, candidateHash)) {
                continue;
            }
            if (candidateHash < botHash) {
                ++rusherRank;
            }
        }

        return (rusherRank & 1) == 0;
    }

    private enum Role {
        ATTACKER,
        UNDERGROUND,
        DEFENDER,
        WALL_GUARD,
        CATAPULT
    }

    private enum Phase {
        SPAWN_SUPPLY,
        LEAVE_SPAWN,
        DESCEND_HOME,
        GROUND_SUPPLY,
        EXIT_HOME,
        DEFENDER_CLIMB,
        DEFENDER_DEPLOY_GROUND,
        DEFEND_FLAG,
        GROUND_DEFENCE,
        WALL_GUARD_PATROL,
        CATAPULT_MOVE,
        CATAPULT_RESTOCK,
        CATAPULT_ROAM,
        MID_RUSH,
        MID_FIGHT,
        UNDERGROUND_DESCEND,
        UNDERGROUND_OUT,
        ENEMY_CLIMB,
        TAKE_FLAG,
        ENEMY_DESCEND,
        UNDERGROUND_RETURN_DESCEND,
        UNDERGROUND_BACK,
        HOME_CLIMB,
        CAPTURE_FLAG
    }

    private static final class RoleState {
        private final CastleWarsManager.Team team;
        private final int primaryCombatStyle;
        private final Role role;
        private int roleGeneration;
        private Phase phase;
        private boolean bandagesStocked;
        private boolean utilityStocked;
        private int routeVariant;
        private int delayTicks;
        private int repathDelay;
        private int undergroundStage;
        private int pendingCollapseRock;
        private int barricadesPlaced;
        private final int defenderIndex;
        private Position barricadeTarget;
        private Position barricadeLastPosition;
        private int barricadeStuckTicks;
        private final Set<String> rejectedBarricadeSites = new HashSet<String>();
        private int reinforcementsPlaced;
        private Position reactiveBarricadeTarget;
        private Position reactiveLastPosition;
        private int reactiveStuckTicks;
        private final Set<String> reactiveRejectedBarricadeSites = new HashSet<String>();
        private Position defenderPatrolTarget;
        private int defenderPatrolTicks;
        private int defenderPatrolGeneration;
        private Position wallPatrolTarget;
        private int wallPatrolTicks;
        private int wallPatrolGeneration;
        private boolean wallAccessed;
        private int routeOffsetX;
        private int routeOffsetY;
        private int midPatrolTicks;
        private Player sightChaseTarget;
        private int sightChaseTicks;

        private RoleState(BotPlayer bot, CastleWarsManager.Team team, int primaryCombatStyle) {
            this.team = team;
            this.primaryCombatStyle = primaryCombatStyle;
            this.role = assignRole(bot, team);
            this.defenderIndex = this.role == Role.DEFENDER ? getDefenderIndex(bot, team) : 0;
            resetForSpawn();
        }

        private void resetForSpawn() {
            this.phase = Phase.SPAWN_SUPPLY;
            this.bandagesStocked = false;
            this.utilityStocked = false;
            this.routeVariant = GameUtil.randomInt(3);
            this.routeOffsetX = -3 + GameUtil.randomInt(7);
            this.routeOffsetY = -3 + GameUtil.randomInt(7);
            this.midPatrolTicks = 12 + GameUtil.randomInt(18);
            this.delayTicks = GameUtil.randomInt(8);
            this.repathDelay = 0;
            this.undergroundStage = 0;
            this.pendingCollapseRock = -1;
            this.barricadesPlaced = 0;
            this.barricadeTarget = null;
            this.barricadeLastPosition = null;
            this.barricadeStuckTicks = 0;
            this.rejectedBarricadeSites.clear();
            this.reinforcementsPlaced = 0;
            this.reactiveBarricadeTarget = null;
            this.reactiveLastPosition = null;
            this.reactiveStuckTicks = 0;
            this.reactiveRejectedBarricadeSites.clear();
            this.defenderPatrolTarget = null;
            this.defenderPatrolTicks = 4 + GameUtil.randomInt(8);
            this.defenderPatrolGeneration = GameUtil.randomInt(16);
            this.wallPatrolTarget = null;
            this.wallPatrolTicks = 0;
            this.wallPatrolGeneration = GameUtil.randomInt(16);
            this.wallAccessed = false;
            this.sightChaseTarget = null;
            this.sightChaseTicks = 0;
        }
    }
}
