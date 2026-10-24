package com.rs2.model.npc;

import com.rs2.ServerSettings;
import com.rs2.bot.combat.BotCombatHelper;
import com.rs2.model.Entity;
import com.rs2.model.EntityUpdateState;
import com.rs2.model.GameplayHelper;
import com.rs2.model.MovementStep;
import com.rs2.model.Position;
import com.rs2.model.World;
import com.rs2.model.clue.ClueKeyHandler;
import com.rs2.model.combat.CombatType;
import com.rs2.model.combat.hit.HitType;
import com.rs2.model.gameplay.abyss.AbyssManager;
import com.rs2.model.gameplay.godwars.GodWarsDungeonManager;
import com.rs2.model.gameplay.partyroom.PartyRoomManager;
import com.rs2.model.ground.GroundItem;
import com.rs2.model.ground.GroundItemManager;
import com.rs2.model.item.ItemDefinition;
import com.rs2.model.item.ItemStack;
import com.rs2.model.npc.AbyssMageTeleportEvent;
import com.rs2.model.npc.MageArenaChallengeStartTask;
import com.rs2.model.npc.NpcDefinition;
import com.rs2.model.npc.NpcDialogueTeleportEvent;
import com.rs2.model.npc.NpcMovementMode;
import com.rs2.model.npc.NpcRelocationEvent;
import com.rs2.model.npc.NpcSequenceAdvanceTask;
import com.rs2.model.npc.NpcStageAdvanceTask;
import com.rs2.model.npc.NpcStatRestoreTask;
import com.rs2.model.npc.combat.NpcCombatDefinition;
import com.rs2.model.npc.drop.NpcDropManager;
import com.rs2.model.player.Player;
import com.rs2.model.skill.woodcutting.UndeadTreeCutting;
import com.rs2.model.task.CycleEventHandler;
import com.rs2.model.task.TickTask;
import com.rs2.util.GameUtil;
import com.rs2.util.RectangularArea;
import com.rs2.util.path.WalkingCollisionMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public final class Npc
extends Entity {
    private int npcId;
    private int originalNpcId;
    private NpcDefinition definition;
    private Position spawnMinPosition = new Position(0, 0);
    private Position spawnMaxPosition = new Position(0, 0);
    private Position spawnPosition;
    private NpcMovementMode movementMode = NpcMovementMode.STATIONARY;
    private int spawnX;
    private int spawnY;
    private int currentHitpoints;
    private int currentAttackLevel = 0;
    private int currentStrengthLevel = 0;
    private int currentDefenceLevel = 0;
    private int currentMagicLevel = 0;
    private int currentRangedLevel = 0;
    private TickTask[] statRestoreTasks = new TickTask[6];
    private int transformedNpcId;
    private int transformTicksRemaining;
    private int facingDirection;
    private boolean active = true;
    private boolean transformed;
    private boolean respawnEnabled;
    private boolean interactable;
    private int ownerPlayerIndex;
    private boolean faceEntityUpdateDisabled = false;
    private Entity forcedCombatTarget = null;
    private int removalDelayTicks = -1;
    private int undeadTreeAttackTicks;
    public Player questOwnerPlayer = null;
    public boolean teleportUpdateRequired = false;
    public boolean chronozonHitByWindBlast = false;
    public boolean chronozonHitByWaterBlast = false;
    public boolean chronozonHitByEarthBlast = false;
    public boolean chronozonHitByFireBlast = false;
    private NpcCombatDefinition combatDefinition;
    private static int nextReferenceId;
    public static int[] combatTransformNpcIds;
    public static int[] scriptedMovementNpcIds;
    public static int[] targetMovementDisabledNpcIds;
    public static int[] autoRetaliateDisabledNpcIds;
    public static int[] faceEntityUpdateDisabledNpcIds;
    public static int scriptedStageCursor;
    public static List scriptedStageNpcs;
    private int scriptedPathTargetX = -1;
    private int scriptedPathTargetY = -1;
    private Position[] waypointLoop;
    private int waypointLoopIndex = -1;
    public int scriptedSequenceLoopCount = 0;
    public int scriptedPathStage = -1;
    private boolean skipNextPathAdvance = false;
    public int lastStepDeltaX = 0;
    public int lastStepDeltaY = 0;

    static {
        new Random();
        nextReferenceId = 0;
        combatTransformNpcIds = new int[]{1266, 1268, 2453, 2886, 2890};
        scriptedMovementNpcIds = new int[]{1266, 1268, 2453, 2886, 2890, 1827, 2892, 2894, 2896};
        targetMovementDisabledNpcIds = new int[]{1827, 2892, 2894, 2896};
        autoRetaliateDisabledNpcIds = new int[]{2440, 2443, 2446};
        faceEntityUpdateDisabledNpcIds = new int[]{767, 1274, 1275, 1276, 1277, 1826, 1827, 1740, 1746, 1719, 1720, 1721, 1721, 1721, 1722, 1722, 1722, 1723, 1727, 1727, 1728, 1729, 1741, 1742, 1743, 1743, 1744, 1744, 1744, 1732, 1725, 1727, 1745, 1721, 1719, 1721, 1722, 1726, 1747, 1747, 1747, 1722, 1727, 1727, 1739, 1736, 1737, 1737, 1738, 2535, 1735, 1749, 1750, 2534, 1734};
        scriptedStageCursor = -1;
        scriptedStageNpcs = new ArrayList();
    }

    public final boolean isFaceEntityUpdateDisabled() {
        return this.faceEntityUpdateDisabled;
    }

    public final void setFaceEntityUpdateDisabled(boolean faceEntityUpdateDisabled) {
        this.faceEntityUpdateDisabled = true;
    }

    public final Entity getForcedCombatTarget() {
        return this.forcedCombatTarget;
    }

    public final void setForcedCombatTarget(Entity entity) {
        this.forcedCombatTarget = entity;
    }

    public Npc(int value3) {
        NpcDefinition npcDefinition = World.getNpcDefinitions()[value3];
        int value2 = value3;
        Npc npc = this;
        this.npcId = value2;
        int initialValue = 1;
        Npc npc2 = this;
        this.interactable = true;
        initialValue = value3;
        npc2 = this;
        this.originalNpcId = initialValue;
        this.getUpdateState().setUpdateRequired(true);
        this.definition = npcDefinition == null ? NpcDefinition.createFallback(value3) : npcDefinition;
        npc2 = this;
        npc2.getAttributes().put("doDamage", Boolean.FALSE);
        npc2.getAttributes().put("canTakeDamage", Boolean.TRUE);
        this.setReferenceId(nextReferenceId++);
        this.combatDefinition = NpcCombatDefinition.forNpcId(value3);
        Npc npc3 = this;
        this.currentHitpoints = npc3.definition.getHitpoints();
        this.resetCombatLevels();
        npc3 = npc2 = this;
        if (npc2.npcId == 896) {
            npc2.startWaypointLoop(new Position[]{new Position(2904, 3463, 0), new Position(2930, 3463, 0)});
        }
    }

    public final void process() {
        Object value;
        processControlExit1: {
            Npc npc;
            processControlExit2: {
                processControlExit3: {
                    double value2;
                    Object value3;
                    processControlExit4: {
                        processControlExit5: {
                            Npc npc2;
                            processControlExit6: {
                                processControlExit7: {
                                    Position position;
                                    int value4;
                                    int value5;
                                    processControlExit8: {
                                        npc2 = this;
                                        value = npc2;
                                        if (npc2.transformTicksRemaining > 0) {
                                            value = npc2;
                                            if (((Npc)value).transformTicksRemaining < 999999) {
                                                value = npc2;
                                                int value6 = ((Npc)value).transformTicksRemaining - 1;
                                                value = npc2;
                                                npc2.transformTicksRemaining = value6;
                                                value = npc2;
                                                if (((Npc)value).transformTicksRemaining <= 0) {
                                                    value = npc2;
                                                    npc2.transformToNpcId(((Npc)value).originalNpcId, 0);
                                                }
                                            }
                                        }
                                        this.pruneExpiredDamageContributions();
                                        npc2 = this;
                                        if (npc2 == null || npc2.isDead()) break processControlExit3;
                                        value3 = npc2;
                                        if (!((Npc)value3).teleportUpdateRequired) {
                                            value = value3;
                                            if (((Npc)value).npcId == 708 && GameUtil.randomInt(30) == 0 && WalkingCollisionMap.getTileFlags(value5 = ((Npc)value3).spawnX + (GameUtil.randomInt(2) == 0 ? -3 - GameUtil.randomInt(4) : 3 + GameUtil.randomInt(4)), value4 = ((Npc)value3).spawnY + (GameUtil.randomInt(2) == 0 ? -3 - GameUtil.randomInt(4) : 3 + GameUtil.randomInt(4)), ((Entity)value3).getPosition().getPlane()) == 0) {
                                                ((Entity)value3).getUpdateState().setGraphic(86, 25);
                                                ((Npc)value3).moveTo(new Position(value5, value4));
                                            }
                                        }
                                        if (npc2.hasCombatTarget() || npc2.getMovementTarget() != null || npc2.getInteractionTarget() != null || npc2.getCombatTarget() != null) break processControlExit3;
                                        if (!npc2.isScriptedMovementEnabled()) break processControlExit8;
                                        if (npc2.waypointLoop != null && ((Entity)(value3 = npc2)).getPosition().getX() == ((Npc)value3).waypointLoop[((Npc)value3).waypointLoopIndex].getX() && ((Entity)value3).getPosition().getY() == ((Npc)value3).waypointLoop[((Npc)value3).waypointLoopIndex].getY()) {
                                            ((Npc)value3).waypointLoopIndex = ((Npc)value3).waypointLoop.length > ((Npc)value3).waypointLoopIndex + 1 ? ++((Npc)value3).waypointLoopIndex : 0;
                                            ((Entity)value3).getMovementQueue().clear();
                                            ((Entity)value3).getMovementQueue().addStep(((Npc)value3).waypointLoop[((Npc)value3).waypointLoopIndex]);
                                            ((Entity)value3).getMovementQueue().removeFirstStep();
                                        }
                                        if (npc2.scriptedPathTargetX != -1 && npc2.scriptedPathTargetY != -1 || !npc2.getMovementQueue().getSteps().isEmpty()) {
                                            value3 = npc2;
                                            if (((Npc)value3).waypointLoop != null) {
                                                ((Entity)value3).getMovementQueue().clear();
                                                ((Entity)value3).getMovementQueue().addStep(((Npc)value3).waypointLoop[((Npc)value3).waypointLoopIndex]);
                                                ((Entity)value3).getMovementQueue().removeFirstStep();
                                            } else if (((Entity)value3).getPosition().getX() == ((Npc)value3).scriptedPathTargetX && ((Entity)value3).getPosition().getY() == ((Npc)value3).scriptedPathTargetY || ((Entity)value3).getPosition().getX() == ((MovementStep)((Entity)value3).getMovementQueue().getSteps().getLast()).getX() && ((Entity)value3).getPosition().getY() == ((MovementStep)((Entity)value3).getMovementQueue().getSteps().getLast()).getY()) {
                                                ((Npc)value3).scriptedPathTargetX = -1;
                                                ((Npc)value3).scriptedPathTargetY = -1;
                                                if (((Npc)value3).skipNextPathAdvance) {
                                                    ((Npc)value3).skipNextPathAdvance = false;
                                                } else if (((Npc)value3).npcId == 1454) {
                                                    World.getTaskScheduler().schedule(new NpcSequenceAdvanceTask((Npc)value3, 10));
                                                } else if (((Npc)value3).npcId == 1431 || ((Npc)value3).npcId == 1432) {
                                                    if (((Npc)value3).scriptedPathStage == 5) {
                                                        int value7 = scriptedStageCursor;
                                                        if (++value7 + 1 > scriptedStageNpcs.size()) {
                                                            value7 = 0;
                                                        }
                                                        Npc npc3 = (Npc)scriptedStageNpcs.get(value7);
                                                        ((Npc)scriptedStageNpcs.get(value7)).skipNextPathAdvance = true;
                                                        npc3.queueScriptedPath(new Position[]{new Position(2768, 2803, 0)});
                                                    }
                                                    if (((Npc)value3).scriptedPathStage == 6) {
                                                        if (++scriptedStageCursor + 1 > scriptedStageNpcs.size()) {
                                                            scriptedStageCursor = 0;
                                                        }
                                                        value = (Npc)scriptedStageNpcs.get(scriptedStageCursor);
                                                        ((Npc)scriptedStageNpcs.get(scriptedStageCursor)).scriptedPathStage = 0;
                                                        ((Npc)value).queueStageAdvancePath(((Npc)value).scriptedPathStage);
                                                    } else {
                                                        World.getTaskScheduler().schedule(new NpcStageAdvanceTask((Npc)value3, 10));
                                                    }
                                                } else {
                                                    ((Entity)value3).setScriptedMovementEnabled(false);
                                                }
                                            }
                                        }
                                        break processControlExit3;
                                    }
                                    value = npc2;
                                    if (((Npc)value).movementMode != NpcMovementMode.STATIONARY) break processControlExit7;
                                    EntityUpdateState entityUpdateState = npc2.getUpdateState();
                                    value = npc2;
                                    int value8 = ((Npc)value).facingDirection;
                                    value3 = npc2.getPosition();
                                    value5 = ((Position)value3).getX();
                                    value4 = ((Position)value3).getY();
                                    switch (value8) {
                                        case 2: {
                                            position = new Position(value5, value4 + 1);
                                            break;
                                        }
                                        case 3: {
                                            position = new Position(value5, value4 - 1);
                                            break;
                                        }
                                        case 4: {
                                            position = new Position(value5 + 1, value4);
                                            break;
                                        }
                                        case 5: {
                                            position = new Position(value5 - 1, value4);
                                            break;
                                        }
                                        default: {
                                            position = new Position(value5, value4 - 1);
                                        }
                                    }
                                    entityUpdateState.setFacePosition(position);
                                    break processControlExit6;
                                }
                                if (npc2.isMovementLocked() || npc2.isStunned() || !GameUtil.rollChance(0.26)) break processControlExit6;
                                if (!World.hasNearbyNonBotPlayer(npc2)) break processControlExit3;
                                int x = npc2.spawnMinPosition.getX();
                                int y = npc2.spawnMinPosition.getY();
                                int x2 = npc2.spawnMaxPosition.getX() - npc2.spawnMinPosition.getX();
                                int y2 = npc2.spawnMaxPosition.getY() - npc2.spawnMinPosition.getY();
                                x2 = GameUtil.getRandom().nextInt(x2);
                                y2 = GameUtil.getRandom().nextInt(y2);
                                value3 = new Position(x + x2, y + y2, npc2.getPosition().getPlane());
                                // Wander like the period game: step straight toward the target and stop at the
                                // first obstacle. Pathfinding around walls let NPCs leave rooms through void tiles.
                                com.rs2.model.path.PathResult wander = new com.rs2.model.path.DirectPathStrategy()
                                        .buildPath(npc2, (Position)value3, true);
                                npc2.getMovementQueue().clear();
                                while (!wander.getSteps().isEmpty()) {
                                    com.rs2.model.path.PathStep step = (com.rs2.model.path.PathStep)wander.getSteps().poll();
                                    npc2.getMovementQueue().addStep(new Position(step.getX(), step.getY(), npc2.getPosition().getPlane()));
                                }
                                npc2.getMovementQueue().removeFirstStep();
                            }
                            value = value3 = npc2;
                            if (((Npc)value3).npcId == 1765) break processControlExit5;
                            value = value3;
                            if (((Npc)value).npcId != 43) break processControlExit4;
                        }
                        if (GameUtil.randomInt(75) == 0) {
                            ((Entity)value3).getUpdateState().setForcedText("Baa!");
                        }
                    }
                    value = value3;
                    if (((Npc)value).npcId == 81 && GameUtil.randomInt(75) == 0) {
                        ((Entity)value3).getUpdateState().setForcedText("Moo!");
                    }
                    value = value3;
                    if (((Npc)value).npcId == 767 && GameUtil.randomInt(75) == 0) {
                        ((Entity)value3).getUpdateState().setForcedText("Mew!");
                    }
                    if (((Npc)value3).isBanker() && PartyRoomManager.partyChestValue >= 50000 && PartyRoomManager.balloonDropPending && GameUtil.randomInt(10) == 0) {
                        double remainingTicks = PartyRoomManager.balloonDropTask.getRemainingTicks();
                        value2 = remainingTicks * 0.6;
                        int value9 = (int)value2;
                        value = String.valueOf(value9) + " seconds";
                        if (value9 > 60) {
                            value = String.valueOf(value9 / 60 + 1) + " mins";
                        }
                        ((Entity)value3).getUpdateState().setForcedText("Drop party in Party Room in " + (String)value + "! Value: " + GameUtil.formatCompactAmount(PartyRoomManager.partyChestValue));
                    }
                    value = value3;
                    if (((Npc)value).npcId == 659 && PartyRoomManager.balloonDropPending && GameUtil.randomInt(5) == 0) {
                        double remainingTicks2 = PartyRoomManager.balloonDropTask.getRemainingTicks();
                        value2 = remainingTicks2 * 0.6;
                        int value10 = (int)value2;
                        value = String.valueOf(value10) + " seconds!";
                        if (value10 > 60) {
                            value = String.valueOf(value10 / 60 + 1) + " mins!";
                        }
                        ((Entity)value3).getUpdateState().setForcedText("Balloons dropping in: " + (String)value);
                    }
                }
                if (ServerSettings.content2007Enabled) {
                    GodWarsDungeonManager.handleBossBattleCry(this);
                }
                this.processStatRestoration();
                this.getTargetMovement().process();
                Npc npc4 = npc = this;
                if (npc.ownerPlayerIndex <= 0 || npc.isDead()) break processControlExit1;
                if (npc.getOwnerPlayer() == null) break processControlExit2;
                if (GameUtil.isWithinDistance(npc.getPosition(), npc.getOwnerPlayer().getPosition(), 15)) break processControlExit1;
                value = npc;
                if (((Npc)value).npcId == 3098) break processControlExit1;
            }
            GameplayHelper.unregisterTemporaryNpc(npc);
        }
        this.processUndeadTreeAttack();
        if (this.removalDelayTicks == 0) {
            boolean enabled = false;
            value = this;
            this.active = enabled;
            World.unregisterNpc(this);
        }
        if (this.removalDelayTicks > 0) {
            --this.removalDelayTicks;
        }
    }

    public final void setRemovalDelayTicks(int delayTicks) {
        this.removalDelayTicks = delayTicks;
    }

    /** Ambient hazard, independent of chopping, quest progress and combat aggression. */
    private void processUndeadTreeAttack() {
        if (!UndeadTreeCutting.isUndeadTree(this.npcId) || !this.active || this.isDead()) return;
        if (this.undeadTreeAttackTicks > 0) {
            --this.undeadTreeAttackTicks;
            if (this.undeadTreeAttackTicks > 0) return;
        }
        for (Player player : World.getPlayers()) {
            if (player == null || player.isDead() || player.getCurrentHitpoints() <= 0
                    || player.isTeleporting() || player.getPosition().getPlane() != this.getPosition().getPlane()
                    || !this.isWithinReach(player, 1) || this.isOverlapping(player)) continue;
            this.getUpdateState().setFacePosition(player.getPosition());
            this.getUpdateState().setAnimation(73, 0);
            player.applyDirectHit(GameUtil.randomInclusive(3), HitType.NORMAL);
            // Four ticks is an approximation; the exact 443 swipe cadence is undocumented.
            this.undeadTreeAttackTicks = 4;
            return;
        }
    }

    private void processStatRestoration() {
        int index = 0;
        while (index < 6) {
            int value = index;
            Npc npc = this;
            int value2 = value;
            value2 = value2 == 0 ? 0 : (value2 == 1 ? 2 : (value2 == 2 ? 1 : (value2 == 3 ? 6 : (value2 == 4 ? 4 : (value2 == 5 ? 3 : -1)))));
            Object value3 = npc.statRestoreTasks[value];
            if (npc.getCurrentLevelForSkill(value2) != npc.getBaseLevelForSkill(value2)) {
                int value4 = value2;
                Npc npc2 = npc;
                value3 = npc2;
                if (npc2.statRestoreTasks[Npc.getStatRestoreTaskSlot(value4)] == null || !npc2.statRestoreTasks[Npc.getStatRestoreTaskSlot(value4)].isActive()) {
                    npc2.statRestoreTasks[Npc.getStatRestoreTaskSlot((int)value4)] = new NpcStatRestoreTask(npc2, 100, (Npc)value3, value4);
                    World.getTaskScheduler().schedule(npc2.statRestoreTasks[Npc.getStatRestoreTaskSlot(value4)]);
                }
            } else if (value3 != null) {
                ((TickTask)value3).stop();
                ((TickTask)value3).setIntervalTicks(100);
                ((TickTask)value3).setRemainingTicks(100);
            }
            ++index;
        }
    }

    public final boolean isStatModified(int value2) {
        return this.getCurrentLevelForSkill(value2) != this.getBaseLevelForSkill(value2);
    }

    private static int getStatRestoreTaskSlot(int slot) {
        if (slot == 0) {
            return 0;
        }
        if (slot == 2) {
            return 1;
        }
        if (slot == 1) {
            return 2;
        }
        if (slot == 6) {
            return 3;
        }
        if (slot == 4) {
            return 4;
        }
        if (slot == 3) {
            return 5;
        }
        return -1;
    }

    public final void adjustCurrentLevel(int level, int value22) {
        if (level == 0) {
            this.currentAttackLevel += value22;
        }
        if (level == 2) {
            this.currentStrengthLevel += value22;
        }
        if (level == 1) {
            this.currentDefenceLevel += value22;
        }
        if (level == 6) {
            this.currentMagicLevel += value22;
        }
        if (level == 4) {
            this.currentRangedLevel += value22;
        }
        if (level == 3) {
            this.currentHitpoints += value22;
        }
    }

    public final int getCurrentLevelForSkill(int level) {
        if (level == 0) {
            return this.currentAttackLevel;
        }
        if (level == 2) {
            return this.currentStrengthLevel;
        }
        if (level == 1) {
            return this.currentDefenceLevel;
        }
        if (level == 6) {
            return this.currentMagicLevel;
        }
        if (level == 4) {
            return this.currentRangedLevel;
        }
        if (level == 3) {
            return this.currentHitpoints;
        }
        return -1;
    }

    public final int getBaseLevelForSkill(int level) {
        if (level == 0) {
            Npc npc = this;
            return npc.definition.getAttackLevel();
        }
        if (level == 2) {
            Npc npc = this;
            return npc.definition.getStrengthLevel();
        }
        if (level == 1) {
            Npc npc = this;
            return npc.definition.getDefenceLevel();
        }
        if (level == 6) {
            Npc npc = this;
            return npc.definition.getMagicLevel();
        }
        if (level == 4) {
            Npc npc = this;
            return npc.definition.getRangedLevel();
        }
        if (level == 3) {
            Npc npc = this;
            return npc.definition.getHitpoints();
        }
        return -1;
    }

    public final void startDialogueTeleport(Player player, int value4, int value22, int value32, String text2) {
        player.getDialogueManager().finishDialogue();
        Player player2 = player;
        player2.packetSender.closeInterfaces();
        this.getUpdateState().setAnimation(1818);
        this.getUpdateState().setGraphic(343);
        this.getUpdateState().setForcedText(text2);
        player.setActionLocked(true);
        CycleEventHandler.getInstance().schedule(player, new NpcDialogueTeleportEvent(this, player, value4, value22, 0), 4);
    }

    public final void startMageArenaChallenge(Player player2, int value4, int value22, int value32, String text2) {
        Npc npc;
        ((Player)player2).getDialogueManager().finishDialogue();
        Player player = player2;
        player.packetSender.closeInterfaces();
        Npc npc2 = npc = new Npc(907 + ((Player)player2).mageArenaProgressStage);
        player = player2;
        ((Player)player2).ownedNpc = npc2;
        this.getUpdateState().setAnimation(717);
        ((Player)player2).getTeleportManager().startStandardTeleport(3105, 3934, 0, null);
        World.getTaskScheduler().schedule(new MageArenaChallengeStartTask(this, 12, (Player)player2, npc));
    }

    public final void startAbyssMageTeleport(Player player, int value4, int value22, int value32, String text2) {
        player.getDialogueManager().finishDialogue();
        player.packetSender.closeInterfaces();
        int id = this.definition.getId();
        id = id == 171 ? 200 : 717;
        this.getUpdateState().setAnimation(id);
        this.getUpdateState().setGraphic(108);
        this.getUpdateState().setForcedText(text2);
        player.setActionLocked(true);
        player.getAttributes().put("canTakeDamage", Boolean.FALSE);
        CycleEventHandler.getInstance().schedule(player, new AbyssMageTeleportEvent(this, player, 2911, 4832, 0), 4);
    }

    public final void startNpcRelocation(Player player, int npcId, int value2, int value32, int value42, int value52, String npcId2, boolean npcId3) {
        player.getDialogueManager().finishDialogue();
        player.packetSender.closeInterfaces();
        this.getUpdateState().setAnimation(402);
        player.getUpdateState().setAnimation(2304);
        player.packetSender.showInterface(8677);
        if (npcId2 != null) {
            this.getUpdateState().setForcedText(npcId2);
        }
        player.setActionLocked(true);
        CycleEventHandler.getInstance().schedule(player, new NpcRelocationEvent(this, player, value32, value42, value52, npcId3, this), 4);
    }

    public final void resetAfterUpdate() {
        this.getUpdateState().setAnimation(-1);
        this.getUpdateState().reset();
        this.transformed = false;
        this.setWalkDirection(-1);
        this.getUpdateState().setFaceEntity(-1);
    }

    @Override
    public final void heal(int currentHitpoints) {
        Npc npc = this;
        Npc npc2 = npc;
        npc2 = this;
        if (npc.currentHitpoints + currentHitpoints >= npc2.definition.getHitpoints()) {
            npc2 = this;
            currentHitpoints = npc2.definition.getHitpoints();
            npc2 = this;
            this.currentHitpoints = currentHitpoints;
            return;
        }
        npc2 = this;
        currentHitpoints = npc2.currentHitpoints + currentHitpoints;
        npc2 = this;
        this.currentHitpoints = currentHitpoints;
    }

    public final void resetCombatLevels() {
        Npc npc = this;
        this.currentAttackLevel = npc.definition.getAttackLevel();
        npc = this;
        this.currentStrengthLevel = npc.definition.getStrengthLevel();
        npc = this;
        this.currentDefenceLevel = npc.definition.getDefenceLevel();
        npc = this;
        this.currentMagicLevel = npc.definition.getMagicLevel();
        npc = this;
        this.currentRangedLevel = npc.definition.getRangedLevel();
    }

    private void startWaypointLoop(Position[] positionArray) {
        this.setScriptedMovementEnabled(true);
        this.waypointLoop = positionArray;
        this.waypointLoopIndex = 0;
        try {
            this.getMovementQueue().clear();
            this.getMovementQueue().addStep(this.waypointLoop[this.waypointLoopIndex]);
            this.getMovementQueue().removeFirstStep();
            return;
        }
        catch (Exception exception) {
            return;
        }
    }

    public final void queueScriptedPath(Position[] positions) {
        this.setScriptedMovementEnabled(true);
        this.getMovementQueue().clear();
        for (Position position : positions) {
            this.getMovementQueue().addStep(position);
        }
        this.scriptedPathTargetX = ((MovementStep)this.getMovementQueue().getSteps().getLast()).getX();
        this.scriptedPathTargetY = ((MovementStep)this.getMovementQueue().getSteps().getLast()).getY();
        this.getMovementQueue().removeFirstStep();
    }

    public final void queueStageAdvancePath(int stageAdvancePath) {
        if (stageAdvancePath == 0) {
            this.queueScriptedPath(new Position[]{new Position(2768, 2798, 0), new Position(2770, 2798, 0), new Position(2770, 2801, 0), new Position(2772, 2801, 0)});
            return;
        }
        if (stageAdvancePath == 1) {
            this.queueScriptedPath(new Position[]{new Position(2772, 2796, 0)});
            return;
        }
        if (stageAdvancePath == 2) {
            this.queueScriptedPath(new Position[]{new Position(2771, 2796, 0)});
            return;
        }
        if (stageAdvancePath == 3) {
            this.queueScriptedPath(new Position[]{new Position(2772, 2796, 0)});
            return;
        }
        if (stageAdvancePath == 4) {
            this.queueScriptedPath(new Position[]{new Position(2772, 2801, 0)});
            return;
        }
        if (stageAdvancePath == 5) {
            this.queueScriptedPath(new Position[]{new Position(2770, 2801, 0), new Position(2770, 2798, 0), new Position(2768, 2798, 0), new Position(2767, 2799, 0)});
            return;
        }
        if (stageAdvancePath == 6) {
            this.queueScriptedPath(new Position[]{new Position(2767, 2801, 0), new Position(2764, 2803, 0)});
        }
    }

    public final void queueSequenceAdvancePath(int sequenceAdvancePath) {
        if (sequenceAdvancePath == 0) {
            this.queueScriptedPath(new Position[]{new Position(2743, 2787, 0)});
            return;
        }
        if (sequenceAdvancePath == 1) {
            this.queueScriptedPath(new Position[]{new Position(2740, 2786, 0)});
            return;
        }
        if (sequenceAdvancePath == 2) {
            this.queueScriptedPath(new Position[]{new Position(2736, 2786, 0)});
            return;
        }
        if (sequenceAdvancePath == 3) {
            this.queueScriptedPath(new Position[]{new Position(2736, 2792, 0)});
            return;
        }
        if (sequenceAdvancePath == 4) {
            this.queueScriptedPath(new Position[]{new Position(2733, 2792, 0)});
            return;
        }
        if (sequenceAdvancePath == 5) {
            this.queueScriptedPath(new Position[]{new Position(2734, 2794, 0), new Position(2737, 2794, 0)});
            return;
        }
        if (sequenceAdvancePath == 6) {
            this.queueScriptedPath(new Position[]{new Position(2743, 2794, 0)});
            return;
        }
        if (sequenceAdvancePath == 7) {
            this.queueScriptedPath(new Position[]{new Position(2743, 2792, 0)});
        }
    }

    public final int getLastStepFacingDirection() {
        if (Math.abs(this.lastStepDeltaX) > Math.abs(this.lastStepDeltaY)) {
            if (this.lastStepDeltaX > 0) {
                return 4;
            }
            return 5;
        }
        if (this.lastStepDeltaY > 0) {
            return 2;
        }
        return 0;
    }

    public final int getWaypointFacingDirection() {
        int x = this.waypointLoop[this.waypointLoopIndex].getX();
        int y = this.waypointLoop[this.waypointLoopIndex].getY();
        x = this.getPosition().getX() - x;
        y = this.getPosition().getY() - y;
        if (Math.abs(x) > Math.abs(y)) {
            if (x < 0) {
                return 4;
            }
            return 5;
        }
        if (y < 0) {
            return 2;
        }
        return 0;
    }

    public final void transformToNpcId(int npcId, int value3) {
        this.transformedNpcId = npcId;
        int value2 = value3;
        Npc npc = this;
        this.transformTicksRemaining = value2;
        this.transformed = true;
        value2 = npcId;
        npc = this;
        this.npcId = value2;
        this.getUpdateState().setUpdateRequired(true);
        this.definition = World.getNpcDefinitions()[npcId];
        this.combatDefinition = NpcCombatDefinition.forNpcId(npcId);
        npc = this;
        this.currentHitpoints = npc.definition.getHitpoints();
        this.resetCombatLevels();
    }

    public final void transformToNpcIdWithAnimation(int npcId, int value2, int transformTicksRemaining) {
        this.transformedNpcId = npcId;
        transformTicksRemaining = 100000;
        Npc npc = this;
        this.transformTicksRemaining = transformTicksRemaining;
        this.transformed = true;
        transformTicksRemaining = npcId;
        npc = this;
        this.npcId = transformTicksRemaining;
        this.getUpdateState().setUpdateRequired(true);
        this.definition = World.getNpcDefinitions()[npcId];
        this.combatDefinition = NpcCombatDefinition.forNpcId(npcId);
        this.heal(38);
        this.resetCombatLevels();
    }

    public final int getNpcId() {
        return this.npcId;
    }

    public final int getOriginalNpcId() {
        return this.originalNpcId;
    }

    public final void setActive(boolean active) {
        this.active = active;
    }

    public final boolean isActive() {
        return this.active;
    }

    public final Player getOwnerPlayer() {
        if (this.ownerPlayerIndex == -1) {
            boolean enabled = false;
            Npc npc = this;
            this.active = enabled;
            World.unregisterNpc(this);
            return null;
        }
        return World.getPlayers()[this.ownerPlayerIndex];
    }

    public final void setOwnerPlayerIndex(int index) {
        this.ownerPlayerIndex = index;
    }

    public final void setSpawnMinPosition(Position position) {
        this.spawnMinPosition = position;
    }

    public final Position getSpawnMinPosition() {
        return this.spawnMinPosition;
    }

    public final void setSpawnMaxPosition(Position position) {
        this.spawnMaxPosition = position;
    }

    public final Position getSpawnMaxPosition() {
        return this.spawnMaxPosition;
    }

    public final void setMovementMode(NpcMovementMode npcMovementMode) {
        this.movementMode = npcMovementMode;
    }

    public final NpcMovementMode getMovementMode() {
        return this.movementMode;
    }

    public final boolean isTransformed() {
        return this.transformed;
    }

    public final int getTransformedNpcId() {
        return this.transformedNpcId;
    }

    public final int getRespawnDelayTicks() {
        return this.combatDefinition.getRespawnDelayTicks();
    }

    public final void setRespawnEnabled(boolean respawnEnabled) {
        this.respawnEnabled = respawnEnabled;
    }

    public final boolean isRespawnEnabled() {
        return this.respawnEnabled;
    }

    public final void setSpawnPosition(Position position) {
        this.spawnPosition = position;
    }

    public final Position getSpawnPosition() {
        return this.spawnPosition;
    }

    public final void setSpawnX(int spawnX) {
        this.spawnX = spawnX;
    }

    public final int getSpawnX() {
        return this.spawnX;
    }

    public final void setSpawnY(int spawnY) {
        this.spawnY = spawnY;
    }

    public final int getSpawnY() {
        return this.spawnY;
    }

    @Override
    public final int getCurrentHitpoints() {
        return this.currentHitpoints;
    }

    @Override
    public final int getMaxHitpoints() {
        return this.definition.getHitpoints();
    }

    @Override
    public final int getDeathAnimationId() {
        return this.definition.getDeathAnimationId();
    }

    public final int getAttackSoundId() {
        return this.definition.getAttackSoundId();
    }

    public final int getHitSoundId() {
        return this.definition.getHitSoundId();
    }

    public final int getDeathSoundId() {
        return this.definition.getDeathSoundId();
    }

    @Override
    public final int getBlockAnimationId() {
        return this.definition.getBlockAnimationId();
    }

    @Override
    public final int getDeathDelayTicks() {
        return this.combatDefinition.getDeathDelayTicks();
    }

    @Override
    public final int getAttackLevelFor(CombatType combatType) {
        return this.definition.getCombatLevel() / 2;
    }

    @Override
    public final int getDefenceLevelFor(CombatType combatType) {
        return this.definition.getCombatLevel() / 2;
    }

    public final int getCurrentDefenceLevel() {
        return this.currentDefenceLevel;
    }

    public final int getCurrentMagicLevel() {
        return this.currentMagicLevel;
    }

    public final int getCurrentRangedLevel() {
        return this.currentRangedLevel;
    }

    public final int getCurrentAttackLevel() {
        return this.currentAttackLevel;
    }

    public final int getCurrentStrengthLevel() {
        return this.currentStrengthLevel;
    }

    public final int getBaseDefenceLevel() {
        return this.definition.getDefenceLevel();
    }

    public final int getBaseMagicLevel() {
        return this.definition.getMagicLevel();
    }

    public final int getBaseRangedLevel() {
        return this.definition.getRangedLevel();
    }

    public final int getBaseAttackLevel() {
        return this.definition.getAttackLevel();
    }

    public final int getBaseStrengthLevel() {
        return this.definition.getStrengthLevel();
    }

    public final int getDefenceBonus(int value2) {
        return this.definition.getDefenceBonus(value2);
    }

    public final int getMeleeAttackBonus() {
        return this.definition.getMeleeAttackBonus();
    }

    public final int getMagicAttackBonus() {
        return this.definition.getMagicAttackBonus();
    }

    public final int getRangedAttackBonus() {
        return this.definition.getRangedAttackBonus();
    }

    @Override
    public final boolean isProtectedFrom(CombatType combatType) {
        if (combatType == CombatType.MELEE) {
            return this.definition.isProtectedFromMelee();
        }
        if (combatType == CombatType.RANGED) {
            return this.definition.isProtectedFromRanged();
        }
        if (combatType == CombatType.MAGIC) {
            return this.definition.isProtectedFromMagic();
        }
        return false;
    }

    @Override
    public final void moveTo(Position position) {
        this.active = false;
        this.teleportUpdateRequired = true;
        this.setPosition(position);
        this.getMovementQueue().clear();
        this.active = true;
    }

    @Override
    public final void setCurrentHitpoints(int currentHitpoints) {
        this.currentHitpoints = currentHitpoints;
    }

    public final void setCurrentAttackLevel(int level) {
        this.currentAttackLevel = level;
    }

    public final void setCurrentStrengthLevel(int level) {
        this.currentStrengthLevel = level;
    }

    public final void setCurrentDefenceLevel(int level) {
        this.currentDefenceLevel = level;
    }

    public final void setCurrentMagicLevel(int level) {
        this.currentMagicLevel = level;
    }

    public final void setCurrentRangedLevel(int level) {
        this.currentRangedLevel = level;
    }

    private void dropLootForKiller(Entity entity) {
        GroundItem groundItem;
        Object value;
        if (entity.isPlayer()) {
            value = (Player)entity;
            if (((Player)value).botEnabled) {
                ((Player)value).botLootGroundItems.clear();
            }
        }
        Npc npc = this;
        ItemStack[] itemStackArray = NpcDropManager.rollDrops(entity, npc.npcId, false);
        int length = itemStackArray.length;
        int index = 0;
        while (index < length) {
            value = itemStackArray[index];
            if (value != null && ((ItemStack)value).getId() != -1 && ((ItemStack)value).getAmount() > 0) {
                groundItem = null;
                if (((ItemStack)value).getAmount() > 1 && !((ItemStack)value).getDefinition().isNote() && !((ItemStack)value).getDefinition().isStackable()) {
                    int index2 = 0;
                    while (index2 < ((ItemStack)value).getAmount()) {
                        groundItem = new GroundItem(new ItemStack(((ItemStack)value).getId(), 1), this, entity, this.getDeathPosition());
                        GroundItemManager.getInstance().spawn(groundItem);
                        ++index2;
                    }
                } else {
                    groundItem = new GroundItem(new ItemStack(((ItemStack)value).getId(), ((ItemStack)value).getAmount() <= 0 ? 1 : ((ItemStack)value).getAmount()), this, entity, this.getDeathPosition());
                    GroundItemManager.getInstance().spawn(groundItem);
                }
                if (entity.isPlayer()) {
                    Player player = (Player)entity;
                    if (player.botEnabled && player.botCombatState != null && player.botCombatState.equals("wait for loot")) {
                        player.botLootGroundItems.add(groundItem);
                    }
                }
            }
            ++index;
        }
        if (entity.isPlayer()) {
            value = (Player)entity;
            if (((Player)value).ringOfWealthShinePending) {
                Object value2 = value;
                ((Player)value2).packetSender.sendGameMessage("Your ring of wealth shines more brightly!");
                ((Player)value).ringOfWealthShinePending = false;
            }
            if (((Player)value).botEnabled) {
                if (((Player)value).botCombatStyle == 1) {
                    int equipmentManager = ((Player)value).getEquipmentManager().getItemIdAtSlot(3);
                    length = 0;
                    if (equipmentManager > 0) {
                        length = ItemDefinition.forId(equipmentManager).isStackable() ? 1 : 0;
                    }
                    if (((Player)value).getEquipmentManager().getItemIdAtSlot(13) != 0 || length != 0) {
                        int value3 = length != 0 ? 3 : 13;
                        GroundItemManager.getInstance();
                        groundItem = GroundItemManager.findVisibleItem((Player)value, ((Player)value).getEquipmentManager().getItemIdAtSlot(value3), this.getPosition());
                        if (groundItem != null) {
                            ((Player)value).botLootGroundItems.add(groundItem);
                        }
                    }
                }
                if (((Player)value).botLootGroundItems.size() > 0) {
                    ((Player)value).botCombatState = "loot items";
                    BotCombatHelper.processBotLootQueue((Player)value);
                    return;
                }
                if (((Player)value).currentBotTask != null) {
                    ((Player)value).interactWithBotNpcTargets(((Player)value).botInteractionTargetIds);
                }
            }
        }
    }

    @Override
    public final void dropDeathItems(Entity entity) {
        if (entity != null) {
            Object value;
            Player player;
            if (entity.isPlayer()) {
                player = (Player)entity;
                if (player.currentGroup != null) {
                    entity = player.currentGroup.selectLootRecipient(player, this.getDeathPosition());
                }
            }
            this.dropLootForKiller(entity);
            if (entity.isPlayer()) {
                player = (Player)entity;
                if (player.getSlayerManager().slayerMasterId == 3887 && !player.getSlayerManager().slayerTaskName.equalsIgnoreCase("") && player.isInWilderness()) {
                    int value2;
                    value = this;
                    value = ((Npc)value).definition.getName().toLowerCase();
                    if (((String)value).contains(player.getSlayerManager().slayerTaskName) && GameUtil.randomInt(value2 = player.skulled ? 1 : 2) == 0) {
                        this.dropLootForKiller(entity);
                    }
                }
            }
            if (entity.isPlayer()) {
                value = this;
                ((Player)entity).getQuestManager().handleNpcDeathDrop(((Npc)value).npcId, (Player)entity, this.getDeathPosition());
                AbyssManager.rollAbyssPouchDrop((Player)entity, this);
                ClueKeyHandler.dropRequiredKeyFromNpc((Player)entity, this);
                Player.rollActionReward();
            }
        }
    }

    public final NpcCombatDefinition getCombatDefinition() {
        return this.combatDefinition;
    }

    public final NpcDefinition getDefinition() {
        return this.definition;
    }

    public final int getFacingDirection() {
        return this.facingDirection;
    }

    public final void setFacingDirection(int direction) {
        this.facingDirection = direction;
    }

    public static Npc findByDefinitionId(int value2) {
        Npc npc = null;
        Npc[] npcArray = World.getNpcs();
        int length = npcArray.length;
        int index = 0;
        while (index < length) {
            Npc npc2 = npcArray[index];
            if (npc2 != null) {
                Npc npc3;
                if (npc2.isDead()) {
                    npc3 = npc2;
                    if (npc3.definition.getId() == value2) {
                        npc = npc2;
                    }
                } else {
                    npc3 = npc2;
                    if (npc3.definition.getId() == value2) {
                        return npc2;
                    }
                }
            }
            ++index;
        }
        return npc;
    }

    public static Npc[] findActiveInArea(RectangularArea rectangularArea) {
        ArrayList<Npc> arrayList = new ArrayList<Npc>();
        Npc[] npcArray = World.getNpcs();
        int length = npcArray.length;
        int index = 0;
        while (index < length) {
            Npc npc = npcArray[index];
            if (npc != null && !npc.isDead() && rectangularArea.contains(npc.getPosition())) {
                arrayList.add(npc);
            }
            ++index;
        }
        return arrayList.toArray(new Npc[arrayList.size()]);
    }

    public static Npc findByDefinitionIdAtPosition(int value2, Position position) {
        Npc[] npcArray = World.getNpcs();
        int length = npcArray.length;
        int index = 0;
        while (index < length) {
            Npc npc = npcArray[index];
            if (npc != null) {
                Npc npc2 = npc;
                if (npc2.definition.getId() == value2 && npc.getPosition().equals(position)) {
                    return npc;
                }
            }
            ++index;
        }
        return null;
    }

    public static void refreshNearbyTransformedNpcs(Player player) {
        Npc[] npcArray = World.getNpcs();
        int length = npcArray.length;
        int index = 0;
        while (index < length) {
            Npc npc = npcArray[index];
            if (npc != null && GameUtil.isWithinDistance(player.getPosition(), npc.getPosition(), 25) && player.getPosition().getPlane() == npc.getPosition().getPlane()) {
                Npc npc2 = npc;
                if (npc2.transformTicksRemaining > 0) {
                    npc.getUpdateState().setUpdateRequired(true);
                }
            }
            ++index;
        }
    }

    public final int getTransformTicksRemaining() {
        return this.transformTicksRemaining;
    }

    public final boolean isInteractable() {
        return this.interactable;
    }

    public final boolean canTraverseStep(int value3, int value22) {
        return this.canStepToOffset(value3, value22);
    }

    public final boolean wouldCollideWithNpc(int npcId, int value4) {
        Npc[] npcArray = World.getNpcs();
        int length = npcArray.length;
        int index = 0;
        while (index < length) {
            Npc npc = npcArray[index];
            if (npc != null && npc != this && !this.isDead() && !npc.isDead() && npc.getPosition().getPlane() == this.getPosition().getPlane() && GameUtil.isWithinDistance(this.getPosition().getX(), this.getPosition().getY(), npc.getPosition().getX(), npc.getPosition().getY(), npc.getSize() + this.getSize())) {
                boolean enabled;
                wouldCollideWithNpcControlExit1: {
                    int value2 = value4;
                    int value3 = npcId;
                    Npc npc2 = npc;
                    npc = this;
                    int position = npc.getPosition().getX();
                    while (position < npc.getPosition().getX() + npc.getSize()) {
                        int position2 = npc.getPosition().getY();
                        while (position2 < npc.getPosition().getY() + npc.getSize()) {
                            int position3 = npc2.getPosition().getX();
                            while (position3 < npc2.getPosition().getX() + npc2.getSize()) {
                                int position4 = npc2.getPosition().getY();
                                while (position4 < npc2.getPosition().getY() + npc2.getSize()) {
                                    if (position3 == position + value3 && position4 == position2 + value2) {
                                        enabled = true;
                                        break wouldCollideWithNpcControlExit1;
                                    }
                                    ++position4;
                                }
                                ++position3;
                            }
                            ++position2;
                        }
                        ++position;
                    }
                    enabled = false;
                }
                if (enabled) {
                    return true;
                }
            }
            ++index;
        }
        return false;
    }

    public static boolean wouldCollideWithPlayer(Npc npc, Player player, int value3, int value22) {
        int position = npc.getPosition().getX();
        while (position < npc.getPosition().getX() + npc.getSize()) {
            int position2 = npc.getPosition().getY();
            while (position2 < npc.getPosition().getY() + npc.getSize()) {
                int position3 = player.getPosition().getX();
                while (position3 < player.getPosition().getX() + player.getSize()) {
                    int position4 = player.getPosition().getY();
                    while (position4 < player.getPosition().getY() + player.getSize()) {
                        if (position3 == position + value3 && position4 == position2 + value22) {
                            return true;
                        }
                        ++position4;
                    }
                    ++position3;
                }
                ++position2;
            }
            ++position;
        }
        return false;
    }

    public final boolean isFacingInteractionPosition(Position position, int value2) {
        value2 = this.getPosition().getX();
        int position2 = this.getPosition().getY();
        int position3 = this.getPosition().getPlane();
        switch (this.facingDirection) {
            case 0: {
                return position.equals(new Position(value2, position2 - 2, position3));
            }
            case 2: {
                return position.equals(new Position(value2, position2 + 2, position3));
            }
            case 3: {
                return position.equals(new Position(value2, position2 - 2, position3));
            }
            case 4: {
                return position.equals(new Position(value2 + 2, position2, position3));
            }
            case 5: {
                return position.equals(new Position(value2 - 2, position2, position3));
            }
        }
        return GameUtil.isWithinDistance(this.getPosition(), position, 1);
    }

    public final boolean isWithinInteractionDistance(Position position, int distance) {
        return GameUtil.isWithinDistance(this.getPosition(), position, distance);
    }

    public final Position getFacingInteractionPosition(int value2) {
        value2 = this.getPosition().getX();
        int position = this.getPosition().getY();
        int position2 = this.getPosition().getPlane();
        switch (this.facingDirection) {
            case 0: {
                return new Position(value2, position - 2, position2);
            }
            case 2: {
                return new Position(value2, position + 2, position2);
            }
            case 3: {
                return new Position(value2, position - 2, position2);
            }
            case 4: {
                return new Position(value2 + 2, position, position2);
            }
            case 5: {
                return new Position(value2 - 2, position, position2);
            }
        }
        return new Position(value2, position + 2, position2);
    }

    public final boolean isBanker() {
        if (this.facingDirection == 1) {
            return false;
        }
        Npc npc = this;
        if (npc.npcId != 166) {
            npc = this;
            if (npc.npcId != 494) {
                npc = this;
                if (npc.npcId != 495) {
                    npc = this;
                    if (npc.npcId != 496) {
                        npc = this;
                        if (npc.npcId != 499) {
                            npc = this;
                            if (npc.npcId != 2619) {
                                return false;
                            }
                        }
                    }
                }
            }
        }
        return true;
    }

    public static boolean isUndead(Entity entity) {
        if (entity.isPlayer()) {
            return false;
        }
        String npcName = ((Npc)entity).definition.getName().toLowerCase();
        return npcName.contains("spectre") || npcName.contains("banshee") || npcName.contains("shade") || npcName.contains("zombie") || npcName.contains("skeleton") || npcName.contains("ghost") || npcName.contains("crawling hand") || npcName.contains("skeletal hand") || npcName.contains("zombie hand") || npcName.contains("zogre") || npcName.contains("skorge") || npcName.contains("ankous");
    }

}
