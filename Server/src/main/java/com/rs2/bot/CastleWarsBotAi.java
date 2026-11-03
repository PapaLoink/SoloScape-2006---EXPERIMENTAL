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

import java.util.IdentityHashMap;
import java.util.Map;

public final class CastleWarsBotAi {
    private static final Map<BotPlayer, BotState> states = new IdentityHashMap<BotPlayer, BotState>();
    private static final Map<Player, Integer> saradominTargetCounts =
            new IdentityHashMap<Player, Integer>();
    private static final Map<Player, Integer> zamorakTargetCounts =
            new IdentityHashMap<Player, Integer>();
    private static int targetCountCacheTick = -1;

    private CastleWarsBotAi() {
    }

    public static void clear(BotPlayer bot) {
        states.remove(bot);
        CastleWarsBotBarricadeAi.clear(bot);
    }

    public static void process(BotPlayer bot) {
        CastleWarsManager.Team team = CastleWarsManager.getGameTeam(bot);
        if (team == null || bot.isDead() || !bot.isRegistered()) {
            states.remove(bot);
            return;
        }

        BotState state = states.get(bot);
        if (state == null || state.team != team) {
            state = new BotState(team);
            states.put(bot, state);
        }
        if (state.phase == Phase.SUPPLY
                && bot.getPosition().getPlane() == 1
                && !CastleWarsManager.isInTeamSpawnArea(bot, team)) {
            state.phase = Phase.DESCEND_HOME;
            state.repathDelay = 0;
        }

        if (CastleWarsManager.isInTeamSpawnArea(bot, team)
                && !CastleWarsManager.isCarryingEnemyFlag(bot)
                && state.phase != Phase.SUPPLY
                && state.phase != Phase.EXIT_BARRIER
                && state.phase != Phase.DESCEND_HOME) {
            state.resetForSpawn();
        }

        restorePrimaryWeapon(bot);

        if (shouldUseBandage(bot)) {
            CastleWarsManager.useBandage(bot);
        }

        if (processFlagCarrierFocus(bot, state)) {
            return;
        }
        if (processOwnDroppedFlagRecovery(bot, state)) {
            return;
        }

        boolean dedicatedFlagRunner =
                CastleWarsBotRoleAi.isDedicatedFlagRunner(bot);
        boolean prioritizeTraversal = isTraversalPhase(state.phase);
        if (prioritizeTraversal) {
            state.sightChaseTarget = null;
            state.sightChaseTicks = 0;
            if (bot.getCombatTarget() != null) {
                CombatManager.stopCombat(bot);
            }
        }

        if (dedicatedFlagRunner
                && !CastleWarsManager.isCarryingEnemyFlag(bot)) {
            state.sightChaseTarget = null;
            state.sightChaseTicks = 0;
            if (bot.getCombatTarget() != null) {
                CombatManager.stopCombat(bot);
            }
        }

        if (CastleWarsManager.isCarryingEnemyFlag(bot)) {
            state.sightChaseTarget = null;
            state.sightChaseTicks = 0;
            Entity target = bot.getCombatTarget();
            if (target != null && !target.isDead()) {
                CombatManager.stopCombat(bot);
            }

            // Flag carriers never resume the enemy-castle climb once the flag is
            // in hand. They descend to ground level, leave through the main double
            // doors, cross the battlefield, then enter their own castle to score.
            CastleWarsManager.Team castleAtPosition =
                    CastleWarsManager.getCastleTeamAtPosition(bot.getPosition());
            if (bot.getPosition().getPlane() > 0) {
                if (castleAtPosition == opposite(team)) {
                    state.phase = Phase.DESCEND_ENEMY;
                } else if (castleAtPosition == team
                        && state.phase != Phase.CAPTURE_FLAG) {
                    state.phase = Phase.CLIMB_HOME;
                }
            } else if (castleAtPosition == opposite(team)) {
                state.phase = Phase.EXIT_ENEMY_GROUND;
            } else if (castleAtPosition == null) {
                state.phase = Phase.RETURN_FIELD;
            } else if (castleAtPosition == team
                    && state.phase != Phase.CAPTURE_FLAG) {
                state.phase = Phase.CLIMB_HOME;
            }
            state.repathDelay = 0;
        } else if (!dedicatedFlagRunner
                && !CastleWarsManager.isInTeamSpawnArea(bot, team)
                && !prioritizeTraversal) {
            if (hasActiveOpponent(bot, state)) {
                return;
            }
            if (processSightChase(bot, state)) {
                return;
            }
            // Battlefield rushers keep pushing toward the flag, but fight
            // opponents they pass instead of ignoring the battle around them.
            int engageRadius = bot.botPrimaryCombatStyle == 0 ? 5 : 9;
            if (tryEngageNearbyOpponent(bot, state, engageRadius)) {
                return;
            }
        }

        if (!dedicatedFlagRunner) {
            Position enemyBarricade = CastleWarsEngineeringManager.findNearestEnemyBarricade(bot, 1);
            if (enemyBarricade != null
                    && bot.getInventoryManager().getItemAmount(
                            CastleWarsEngineeringManager.EXPLOSIVE_POTION_ID) > 0
                    && CastleWarsEngineeringManager.destroyBarricadeWithExplosive(bot, enemyBarricade)) {
                CastleWarsBotChat.sayEngineering(bot);
                state.delayTicks = 2;
                return;
            }
            if (bot.getInventoryManager().getItemAmount(
                    CastleWarsEngineeringManager.EXPLOSIVE_POTION_ID) > 0
                    && CastleWarsEngineeringManager.sabotageEnemyCatapult(bot)) {
                CastleWarsBotChat.sayCatapult(bot);
                state.delayTicks = 2;
                return;
            }
        }

        if (state.delayTicks > 0) {
            --state.delayTicks;
            return;
        }

        switch (state.phase) {
            case SUPPLY:
                processSupply(bot, state);
                break;
            case EXIT_BARRIER:
                processExitBarrier(bot, state);
                break;
            case DESCEND_HOME:
                processInitialDescent(bot, state);
                break;
            case EXIT_HOME_GROUND:
                processGroundCastleExit(bot, state, team, Phase.CROSS_FIELD);
                break;
            case CROSS_FIELD:
                processCrossField(bot, state, false);
                break;
            case ENTER_ENEMY:
                processGroundCastleEntry(bot, state, opposite(team), Phase.CLIMB_ENEMY);
                break;
            case CLIMB_ENEMY:
                processCastleClimb(bot, state, opposite(team), true, Phase.TAKE_FLAG);
                break;
            case TAKE_FLAG:
                processTakeFlag(bot, state);
                break;
            case DESCEND_ENEMY:
                processCastleClimb(bot, state, opposite(team), false, Phase.EXIT_ENEMY_GROUND);
                break;
            case EXIT_ENEMY_GROUND:
                processGroundCastleExit(bot, state, opposite(team),
                        CastleWarsManager.isCarryingEnemyFlag(bot) ? Phase.RETURN_FIELD : Phase.ROAM_FIELD);
                break;
            case RETURN_FIELD:
                processCrossField(bot, state, true);
                break;
            case ENTER_HOME:
                processGroundCastleEntry(bot, state, team, Phase.CLIMB_HOME);
                break;
            case CLIMB_HOME:
                processCastleClimb(bot, state, team, true, Phase.CAPTURE_FLAG);
                break;
            case CAPTURE_FLAG:
                processCapture(bot, state);
                break;
            case DESCEND_AFTER_SCORE:
                processCastleClimb(bot, state, team, false, Phase.EXIT_AFTER_SCORE);
                break;
            case EXIT_AFTER_SCORE:
                processGroundCastleExit(bot, state, team, Phase.ROAM_FIELD);
                break;
            case ROAM_FIELD:
                processRoam(bot, state);
                break;
            default:
                state.phase = Phase.ROAM_FIELD;
                break;
        }
    }

    private static void processSupply(BotPlayer bot, BotState state) {
        Position supply = state.team == CastleWarsManager.Team.SARADOMIN
                ? new Position(2424, 3077, 1)
                : new Position(2375, 3130, 1);

        if (!near(bot, supply, 3)) {
            walk(bot, state, supply);
            return;
        }

        if (!state.stocked) {
            prepareBandageSpace(bot, 13);
            int amount = 8 + GameUtil.randomInt(5);
            int given = CastleWarsManager.giveBandages(bot, amount);
            if (given > 0) {
                bot.getUpdateState().setAnimation(881);
            }
            CastleWarsEngineeringManager.giveSupply(bot,
                    CastleWarsEngineeringManager.EXPLOSIVE_POTION_ID, 1);
            // Flag runners need a reusable tinderbox to burn barricades
            // that block the enemy castle entrance or the return route.
            if (bot.getInventoryManager().getItemAmount(
                    CastleWarsEngineeringManager.TINDERBOX_ITEM_ID) == 0) {
                CastleWarsEngineeringManager.giveSupply(bot,
                        CastleWarsEngineeringManager.TINDERBOX_ITEM_ID, 1);
            }
            if (state.routeVariant == 2) {
                CastleWarsEngineeringManager.giveSupply(bot,
                        CastleWarsEngineeringManager.CLIMBING_ROPE_ITEM_ID, 1);
            }
            state.stocked = true;
            state.delayTicks = 1 + GameUtil.randomInt(4);
        }

        state.phase = Phase.EXIT_BARRIER;
        state.crossedMidpoint = false;
        state.repathDelay = 0;
        state.delayTicks = 1 + GameUtil.randomInt(3);
    }

    private static void processExitBarrier(BotPlayer bot, BotState state) {
        boolean alternateExit = (bot.getNameHash() & 1L) != 0L;
        Position approach;
        int objectId;
        int objectX;
        int objectY;

        if (state.team == CastleWarsManager.Team.SARADOMIN) {
            approach = alternateExit
                    ? new Position(2423, 3076, 1)
                    : new Position(2426, 3079, 1);
            objectId = CastleWarsManager.SARADOMIN_ENERGY_BARRIER_ID;
            objectX = alternateExit ? 2422 : 2426;
            objectY = alternateExit ? 3076 : 3080;
        } else {
            approach = alternateExit
                    ? new Position(2376, 3131, 1)
                    : new Position(2373, 3127, 1);
            objectId = CastleWarsManager.ZAMORAK_ENERGY_BARRIER_ID;
            objectX = alternateExit ? 2377 : 2373;
            objectY = alternateExit ? 3131 : 3126;
        }

        if (!reachInteractionApproach(bot, state, approach)) {
            return;
        }
        CastleWarsManager.handleFirstObjectAction(bot, objectId, objectX, objectY);
        state.phase = Phase.DESCEND_HOME;
        state.repathDelay = 0;
    }

    private static void processInitialDescent(BotPlayer bot, BotState state) {
        if (bot.getPosition().getPlane() == 0) {
            state.phase = Phase.EXIT_HOME_GROUND;
            return;
        }

        if (state.routeVariant == 2) {
            if (state.team == CastleWarsManager.Team.SARADOMIN) {
                Position approach = new Position(2421, 3074, 1);
                if (!reachInteractionApproach(bot, state, approach)) {
                    return;
                }
                CastleWarsManager.handleFirstObjectAction(bot, 4911, 2421, 3073);
            } else {
                Position approach = new Position(2378, 3133, 1);
                if (!reachInteractionApproach(bot, state, approach)) {
                    return;
                }
                CastleWarsManager.handleFirstObjectAction(bot, 4911, 2378, 3134);
            }
        } else {
            if (state.team == CastleWarsManager.Team.SARADOMIN) {
                Position approach = new Position(2420, 3080, 1);
                if (!reachInteractionApproach(bot, state, approach)) {
                    return;
                }
                CastleWarsManager.handleFirstObjectAction(bot, 4415, 2419, 3080);
            } else {
                Position approach = new Position(2379, 3127, 1);
                if (!reachInteractionApproach(bot, state, approach)) {
                    return;
                }
                CastleWarsManager.handleFirstObjectAction(bot, 4415, 2380, 3127);
            }
        }

        if (bot.getPosition().getPlane() == 0) {
            state.phase = Phase.EXIT_HOME_GROUND;
        }
        state.repathDelay = 0;
    }

    private static void processGroundCastleExit(BotPlayer bot, BotState state,
                                                CastleWarsManager.Team castleTeam,
                                                Phase nextPhase) {
        if (bot.getPosition().getPlane() != 0) {
            return;
        }

        Position exterior = CastleWarsManager.getBotMainDoorExteriorPosition(bot, castleTeam);
        CastleWarsManager.routeBotThroughGroundCastle(bot, castleTeam, false);
        if (!near(bot, exterior, 0)) {
            state.repathDelay = 0;
            return;
        }

        state.phase = nextPhase;
        state.repathDelay = 0;
    }

    private static void processGroundCastleEntry(BotPlayer bot, BotState state,
                                                 CastleWarsManager.Team castleTeam,
                                                 Phase nextPhase) {
        if (bot.getPosition().getPlane() != 0) {
            state.phase = nextPhase;
            return;
        }

        if (castleTeam != state.team && state.routeVariant == 2
                && !CastleWarsBotRoleAi.isDedicatedFlagRunner(bot)) {
            Position battlement =
                    CastleWarsEngineeringManager.findNearestClimbableBattlement(
                            bot, castleTeam);
            if (battlement != null) {
                Position ropeApproach =
                        CastleWarsManager.getNearestAdjacentInteractionTile(bot, battlement);
                if (ropeApproach == null
                        || !reachInteractionApproach(bot, state, ropeApproach)) {
                    return;
                }
                if (CastleWarsEngineeringManager.useClimbingRopeForBot(bot, battlement)) {
                    if (bot.getPosition().getPlane() != 0) {
                        state.phase = nextPhase;
                    }
                    state.repathDelay = 0;
                    return;
                }
            }
        }

        Position inside = CastleWarsManager.getBotMainDoorInteriorPosition(bot, castleTeam);
        CastleWarsManager.routeBotThroughGroundCastle(bot, castleTeam, true);
        if (!near(bot, inside, 0)) {
            state.repathDelay = 0;
            return;
        }

        state.phase = nextPhase;
        state.repathDelay = 0;
    }

    private static void processCrossField(BotPlayer bot, BotState state, boolean returningHome) {
        if (bot.getPosition().getPlane() != 0) {
            return;
        }

        if (!state.crossedMidpoint) {
            if (state.routeVariant == 0) {
                Position midpoint = fieldWaypoint(state);
                if (!near(bot, midpoint, 3)) {
                    walk(bot, state, midpoint);
                    return;
                }
            } else {
                CastleWarsManager.Team sourceTeam =
                        returningHome ? opposite(state.team) : state.team;
                Position crossingExit =
                        CastleWarsManager.getSteppingStoneExit(sourceTeam, state.routeVariant);
                if (!near(bot, crossingExit, 0)) {
                    Position nextStone = CastleWarsManager.getSteppingStoneNextStep(
                            bot.getPosition(), sourceTeam, state.routeVariant);
                    if (nextStone != null) {
                        CastleWarsManager.jumpSteppingStone(bot, nextStone);
                        state.repathDelay = 0;
                        return;
                    }

                    Position approach =
                            CastleWarsManager.getSteppingStoneApproach(sourceTeam, state.routeVariant);
                    if (!near(bot, approach, 0)) {
                        walk(bot, state, approach);
                    }
                    return;
                }
            }

            state.crossedMidpoint = true;
            state.repathDelay = 0;
            if (!returningHome
                    && !CastleWarsBotRoleAi.isDedicatedFlagRunner(bot)) {
                state.delayTicks = 3 + GameUtil.randomInt(6);
                if (GameUtil.randomInt(4) == 0) {
                    CastleWarsBotChat.sayMid(bot);
                }
                return;
            }
        }

        CastleWarsManager.Team destination = returningHome ? state.team : opposite(state.team);
        Position outside = CastleWarsManager.getBotMainDoorExteriorPosition(bot, destination);
        if (!near(bot, outside, 0)) {
            walk(bot, state, outside);
            return;
        }

        state.crossedMidpoint = false;
        state.phase = returningHome ? Phase.ENTER_HOME : Phase.ENTER_ENEMY;
        state.repathDelay = 0;
    }

    private static Position fieldWaypoint(BotState state) {
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

    private static void processCastleClimb(BotPlayer bot, BotState state,
                                           CastleWarsManager.Team castleTeam,
                                           boolean up,
                                           Phase completePhase) {
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
            processSaradominStairs(bot, state, up);
        } else {
            processZamorakStairs(bot, state, up);
        }
    }

    private static void processSaradominStairs(BotPlayer bot, BotState state, boolean up) {
        int plane = bot.getPosition().getPlane();
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
    }

    private static void processZamorakStairs(BotPlayer bot, BotState state, boolean up) {
        int plane = bot.getPosition().getPlane();
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

    private static void useTraversal(BotPlayer bot, BotState state, Position approach,
                                     int objectId, int objectX, int objectY) {
        if (!reachInteractionApproach(bot, state, approach)) {
            return;
        }
        CastleWarsManager.handleFirstObjectAction(bot, objectId, objectX, objectY);
        state.repathDelay = 0;
    }

    private static void processTakeFlag(BotPlayer bot, BotState state) {
        CastleWarsManager.Team enemyTeam = opposite(state.team);
        Position flag = enemyTeam == CastleWarsManager.Team.SARADOMIN
                ? new Position(2429, 3074, 3)
                : new Position(2370, 3133, 3);

        Position approach = CastleWarsManager.getNearestAdjacentInteractionTile(bot, flag);
        if (approach == null || !reachInteractionApproach(bot, state, approach)) {
            return;
        }

        if (CastleWarsManager.isFlagAtBase(enemyTeam) && CastleWarsManager.takeEnemyFlag(bot)) {
            CastleWarsBotChat.sayFlagCarrier(bot);
        }
        state.phase = Phase.DESCEND_ENEMY;
        state.repathDelay = 0;
    }

    private static void processCapture(BotPlayer bot, BotState state) {
        Position ownFlag = state.team == CastleWarsManager.Team.SARADOMIN
                ? new Position(2429, 3074, 3)
                : new Position(2370, 3133, 3);

        Position approach = CastleWarsManager.getNearestAdjacentInteractionTile(bot, ownFlag);
        if (approach == null || !reachInteractionApproach(bot, state, approach)) {
            return;
        }

        if (!CastleWarsManager.isCarryingEnemyFlag(bot)) {
            state.phase = Phase.DESCEND_AFTER_SCORE;
            return;
        }

        if (CastleWarsManager.tryCaptureFlag(bot)) {
            CastleWarsBotChat.sayScore(bot);
            restorePrimaryWeapon(bot);
            state.phase = Phase.DESCEND_AFTER_SCORE;
            state.delayTicks = 2 + GameUtil.randomInt(5);
        }
    }

    private static void processRoam(BotPlayer bot, BotState state) {
        if (bot.getPosition().getPlane() != 0) {
            state.phase = Phase.DESCEND_ENEMY;
            state.repathDelay = 0;
            return;
        }

        // Attackers do not idle around the field. If they are not already in
        // combat, immediately start another push toward the enemy flag.
        state.routeVariant = GameUtil.randomInt(3);
        state.routeOffsetX = -2 + GameUtil.randomInt(5);
        state.routeOffsetY = -2 + GameUtil.randomInt(5);
        state.crossedMidpoint = false;
        state.roamTicks = 0;
        state.phase = Phase.CROSS_FIELD;
        state.repathDelay = 0;
    }

    private static boolean processOwnDroppedFlagRecovery(BotPlayer bot, BotState state) {
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

    private static boolean navigateFlagRecovery(BotPlayer bot, BotState state,
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

    private static boolean processFlagCarrierFocus(BotPlayer bot, BotState state) {
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

    private static boolean hasActiveOpponent(BotPlayer bot, BotState state) {
        Entity target = bot.getCombatTarget();
        if (target == null || target.isDead() || !target.isPlayer()) {
            return false;
        }
        Player targetPlayer = (Player)target;
        if (!CastleWarsManager.areOpponents(bot, targetPlayer)) {
            CombatManager.stopCombat(bot);
            return false;
        }
        if (CastleWarsManager.getFlagHolder(state.team) != targetPlayer
                && countTeamBotsTargeting(bot, targetPlayer) > 2) {
            CombatManager.stopCombat(bot);
            return false;
        }
        int maxDistance = bot.botPrimaryCombatStyle == 0 ? 6 : 12;
        if (GameUtil.getDistance(bot.getPosition(), targetPlayer.getPosition()) > maxDistance) {
            CombatManager.stopCombat(bot);
            return false;
        }
        if (!CastleWarsManager.hasBotCombatLineOfSight(bot, targetPlayer)) {
            state.sightChaseTarget = targetPlayer;
            state.sightChaseTicks = 30;
            CombatManager.stopCombat(bot);
            return false;
        }
        state.sightChaseTarget = null;
        state.sightChaseTicks = 0;
        return true;
    }

    private static boolean processSightChase(BotPlayer bot, BotState state) {
        Player target = state.sightChaseTarget;
        if (target == null) {
            return false;
        }
        if (target.isDead() || !target.isRegistered()
                || !CastleWarsManager.areOpponents(bot, target)
                || GameUtil.getDistance(bot.getPosition(), target.getPosition()) > 16
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

    private static boolean navigateSightChase(BotPlayer bot, BotState state, Player target) {
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

    private static boolean tryEngageNearbyOpponent(BotPlayer bot, BotState state, int radius) {
        Player bestVisible = null;
        Player bestHidden = null;
        int bestVisibleDistance = Integer.MAX_VALUE;
        int bestHiddenDistance = Integer.MAX_VALUE;
        Map<Player, Integer> teamTargetCounts = getTeamBotTargetCounts(bot);
        for (Player player : CastleWarsManager.getGamePlayersView()) {
            if (player == null || player == bot || player.isDead()
                    || !CastleWarsManager.areOpponents(bot, player)) {
                continue;
            }
            int distance = GameUtil.getDistance(bot.getPosition(), player.getPosition());
            if (distance > radius) {
                continue;
            }
            Integer targetingCount = teamTargetCounts.get(player);
            if (CastleWarsManager.getFlagHolder(state.team) != player
                    && targetingCount != null && targetingCount >= 2) {
                continue;
            }
            if (CastleWarsManager.hasBotCombatLineOfSight(bot, player)) {
                if (distance < bestVisibleDistance) {
                    bestVisible = player;
                    bestVisibleDistance = distance;
                }
            } else if (distance < bestHiddenDistance) {
                bestHidden = player;
                bestHiddenDistance = distance;
            }
        }
        if (bestVisible != null) {
            bot.getMovementQueue().setRunning(true);
            CombatManager.startCombat(bot, bestVisible);
            if (GameUtil.randomInt(12) == 0) {
                CastleWarsBotChat.sayCombat(bot);
            }
            return true;
        }
        if (bestHidden != null) {
            state.sightChaseTarget = bestHidden;
            state.sightChaseTicks = 30;
            return processSightChase(bot, state);
        }
        return false;
    }

    private static Map<Player, Integer> getTeamBotTargetCounts(BotPlayer bot) {
        refreshTeamBotTargetCounts();
        CastleWarsManager.Team team = CastleWarsManager.getGameTeam(bot);
        if (team == CastleWarsManager.Team.SARADOMIN) {
            return saradominTargetCounts;
        }
        if (team == CastleWarsManager.Team.ZAMORAK) {
            return zamorakTargetCounts;
        }
        return new IdentityHashMap<Player, Integer>();
    }

    private static void refreshTeamBotTargetCounts() {
        if (targetCountCacheTick == World.tickCount) {
            return;
        }
        saradominTargetCounts.clear();
        zamorakTargetCounts.clear();

        for (Player player : CastleWarsManager.getGamePlayersView()) {
            if (!(player instanceof BotPlayer)) {
                continue;
            }
            CastleWarsManager.Team team = CastleWarsManager.getGameTeam(player);
            if (team == null) {
                continue;
            }
            Entity target = player.getCombatTarget();
            if (!(target instanceof Player)) {
                continue;
            }

            Map<Player, Integer> counts = team == CastleWarsManager.Team.SARADOMIN
                    ? saradominTargetCounts : zamorakTargetCounts;
            Player targetPlayer = (Player)target;
            Integer current = counts.get(targetPlayer);
            counts.put(targetPlayer, current == null ? 1 : current + 1);
        }
        targetCountCacheTick = World.tickCount;
    }

    private static int countTeamBotsTargeting(BotPlayer bot, Player target) {
        if (target == null) {
            return 0;
        }
        Integer count = getTeamBotTargetCounts(bot).get(target);
        return count == null ? 0 : count;
    }

    private static boolean isTraversalPhase(Phase phase) {
        switch (phase) {
            case EXIT_BARRIER:
            case DESCEND_HOME:
            case EXIT_HOME_GROUND:
            case ENTER_ENEMY:
            case CLIMB_ENEMY:
            case DESCEND_ENEMY:
            case EXIT_ENEMY_GROUND:
            case ENTER_HOME:
            case CLIMB_HOME:
            case DESCEND_AFTER_SCORE:
            case EXIT_AFTER_SCORE:
                return true;
            default:
                return false;
        }
    }

    private static boolean shouldUseBandage(BotPlayer bot) {
        if (bot.getInventoryManager().getItemAmount(4049) <= 0) {
            return false;
        }
        return bot.getCurrentHitpoints() * 100 <= bot.getMaxHitpoints() * 62
                || bot.getRunEnergyPercent() <= 35;
    }

    private static void prepareBandageSpace(BotPlayer bot, int desiredFreeSlots) {
        int free = bot.getInventoryManager().getContainer().getFreeSlots();
        if (free >= desiredFreeSlots) {
            return;
        }
        int foodId = bot.botFoodItemId;
        if (foodId <= 0) {
            return;
        }
        int foodAmount = bot.getInventoryManager().getItemAmount(foodId);
        int remove = Math.min(foodAmount, desiredFreeSlots - free);
        if (remove > 0) {
            bot.getInventoryManager().removeItem(new ItemStack(foodId, remove));
        }
    }

    private static void restorePrimaryWeapon(BotPlayer bot) {
        if (CastleWarsManager.isCarryingFlag(bot)) {
            return;
        }
        if (bot.botWeaponItemId > 0
                && bot.getEquipmentManager().getItemIdAtSlot(3) != bot.botWeaponItemId) {
            int weaponSlot = bot.getInventoryManager().getContainer().indexOfItem(bot.botWeaponItemId);
            if (weaponSlot >= 0) {
                bot.getEquipmentManager().equipFromInventorySlot(weaponSlot);
            }
        }
        if (bot.botShieldItemId > 0
                && bot.getEquipmentManager().getItemIdAtSlot(5) != bot.botShieldItemId) {
            int shieldSlot = bot.getInventoryManager().getContainer().indexOfItem(bot.botShieldItemId);
            if (shieldSlot >= 0) {
                bot.getEquipmentManager().equipFromInventorySlot(shieldSlot);
            }
        }
    }

    private static boolean reachInteractionApproach(BotPlayer bot, BotState state,
                                                     Position approach) {
        if (approach == null || bot.getPosition().getPlane() != approach.getPlane()) {
            return false;
        }
        if (near(bot, approach, 0)) {
            return true;
        }
        if (CastleWarsBotBarricadeAi.handleBlockedRoute(bot, approach)) {
            return false;
        }
        if (state.repathDelay > 0) {
            --state.repathDelay;
            return false;
        }
        state.repathDelay = 2;
        bot.getMovementQueue().setRunning(true);
        PathFinder.findPath(bot, approach.getX(), approach.getY(), false, 0, 0);
        bot.getMovementQueue().clearMovementActions();
        return false;
    }

    private static void walk(BotPlayer bot, BotState state, Position target) {
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

        if (CastleWarsBotBarricadeAi.handleBlockedRoute(bot, navigationTarget)) {
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

    private enum Phase {
        SUPPLY,
        EXIT_BARRIER,
        DESCEND_HOME,
        EXIT_HOME_GROUND,
        CROSS_FIELD,
        ENTER_ENEMY,
        CLIMB_ENEMY,
        TAKE_FLAG,
        DESCEND_ENEMY,
        EXIT_ENEMY_GROUND,
        RETURN_FIELD,
        ENTER_HOME,
        CLIMB_HOME,
        CAPTURE_FLAG,
        DESCEND_AFTER_SCORE,
        EXIT_AFTER_SCORE,
        ROAM_FIELD
    }

    private static final class BotState {
        private final CastleWarsManager.Team team;
        private Phase phase;
        private boolean stocked;
        private boolean crossedMidpoint;
        private int routeVariant;
        private int routeOffsetX;
        private int routeOffsetY;
        private int delayTicks;
        private int repathDelay;
        private int roamTicks;
        private Player sightChaseTarget;
        private int sightChaseTicks;

        private BotState(CastleWarsManager.Team team) {
            this.team = team;
            resetForSpawn();
        }

        private void resetForSpawn() {
            this.phase = Phase.SUPPLY;
            this.stocked = false;
            this.crossedMidpoint = false;
            this.routeVariant = GameUtil.randomInt(3);
            this.routeOffsetX = -2 + GameUtil.randomInt(5);
            this.routeOffsetY = -2 + GameUtil.randomInt(5);
            this.delayTicks = GameUtil.randomInt(7);
            this.repathDelay = 0;
            this.roamTicks = 20 + GameUtil.randomInt(40);
            this.sightChaseTarget = null;
            this.sightChaseTicks = 0;
        }
    }
}
