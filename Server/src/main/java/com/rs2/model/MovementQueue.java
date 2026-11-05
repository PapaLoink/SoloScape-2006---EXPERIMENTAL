package com.rs2.model;

import com.rs2.ServerSettings;
import com.rs2.model.Entity;
import com.rs2.model.EntityTargetMovement;
import com.rs2.model.GameplayHelper;
import com.rs2.model.MovementStep;
import com.rs2.model.Position;
import com.rs2.model.World;
import com.rs2.model.bankpin.BankPinManager;
import com.rs2.model.gameplay.CaveLightManager;
import com.rs2.model.gameplay.DesertHeatManager;
import com.rs2.model.gameplay.partyroom.PartyRoomManager;
import com.rs2.model.music.MusicManager;
import com.rs2.model.npc.Npc;
import com.rs2.model.objects.LoadedWorldObject;
import com.rs2.model.objects.ObjectDefinition;
import com.rs2.model.objects.StileObject;
import com.rs2.model.objects.WorldObjectLookup;
import com.rs2.model.player.Player;
import com.rs2.net.packet.PacketSender;
import com.rs2.util.GameUtil;
import java.util.Deque;
import java.util.LinkedList;

public final class MovementQueue {
    private final Entity entity;
    private Deque steps = new LinkedList();
    private Deque stepHistory = new LinkedList();
    private boolean running = false;
    private boolean runPath = false;
    private static byte[] directionDeltaX;
    private static byte[] directionDeltaY;
    private static int[][] collisionBypassTiles;

    static {
        byte[] xDeltas = new byte[8];
        xDeltas[0] = -1;
        xDeltas[2] = 1;
        xDeltas[3] = -1;
        xDeltas[4] = 1;
        xDeltas[5] = -1;
        xDeltas[7] = 1;
        directionDeltaX = xDeltas;
        byte[] yDeltas = new byte[8];
        yDeltas[0] = 1;
        yDeltas[1] = 1;
        yDeltas[2] = 1;
        yDeltas[5] = -1;
        yDeltas[6] = -1;
        yDeltas[7] = -1;
        directionDeltaY = yDeltas;
        collisionBypassTiles = new int[][]{{2491, 10146}, {2491, 10147}, {2491, 10148}, {2491, 10162}, {2491, 10163}, {2491, 10164}, {2491, 10130}, {2491, 10131}, {2491, 10132}, {2809, 3437}, {2543, 10143}, {2545, 10141}, {2545, 10145}, {3225, 3238}, {2377, 3089}, {2378, 3083}, {2420, 3122}, {2418, 3126}};
    }

    public final Deque getSteps() {
        return this.steps;
    }

    public MovementQueue(Entity entity) {
        this.entity = entity;
    }

    public final void reset() {
        this.clear();
    }

    public final void process() {
        Object value;
        processControlExit1: {
            int value2;
            int value3;
            MovementStep movementStep;
            processControlExit2: {
                Entity entity;
                Object value4;
                processControlExit3: {
                    boolean enabled;
                    processControlExit4: {
                        Player player;
                        processControlExit5: {
                            if (this.entity.isDead() || !this.entity.getMovementDelayTimer().hasElapsed()) {
                                return;
                            }
                            movementStep = null;
                            if (this.stepHistory.size() >= 25) {
                                java.util.Iterator stepIterator = this.stepHistory.iterator();
                                while (stepIterator.hasNext() && this.stepHistory.size() >= 10) {
                                    stepIterator.next();
                                    stepIterator.remove();
                                }
                            }
                            if ((value4 = (MovementStep)this.steps.poll()) == null || ((MovementStep)value4).getDirection() == -1) {
                                value = this;
                            }
                            if (!this.entity.isPlayer()) break processControlExit4;
                            player = (Player)this.entity;
                            value = this;
                            if (((MovementQueue)value).running) break processControlExit5;
                            value = this;
                            if (!((MovementQueue)value).runPath) break processControlExit4;
                        }
                        if (player.getRunEnergyPercent() > 0) {
                            movementStep = (MovementStep)this.steps.poll();
                        }
                    }
                    boolean enabled2 = enabled = this.entity.isPlayer() && ((Player)this.entity).forcedMovementActive;
                    if (value4 != null && ((MovementStep)value4).getDirection() != -1) {
                        value3 = directionDeltaX[((MovementStep)value4).getDirection()];
                        value2 = directionDeltaY[((MovementStep)value4).getDirection()];
                        if (!enabled && !this.canStep(value3, value2)) {
                            this.clear();
                            return;
                        }
                        if (this.entity.isNpc()) {
                            entity = (Npc)this.entity;
                            ((Npc)this.entity).lastStepDeltaX = value3;
                            ((Npc)entity).lastStepDeltaY = value2;
                        }
                        this.entity.getPosition().translate(value3, value2);
                        this.entity.recordMovementTick();
                        this.entity.setWalkDirection(((MovementStep)value4).getDirection());
                        this.stepHistory.add(value4);
                    }
                    if (movementStep == null || movementStep.getDirection() == -1) break processControlExit1;
                    value3 = directionDeltaX[movementStep.getDirection()];
                    value2 = directionDeltaY[movementStep.getDirection()];
                    if (!enabled && !this.canStep(value3, value2)) {
                        this.clear();
                        return;
                    }
                    if (!this.entity.isPlayer()) break processControlExit2;
                    entity = (Player)this.entity;
                    value = this;
                    if (((MovementQueue)value).running) break processControlExit3;
                    value = this;
                    if (!((MovementQueue)value).runPath) break processControlExit2;
                }
                if (((Player)entity).getRunEnergyPercent() > 0) {
                    // Debug logging must not grant infinite stamina to every player.
                    // Only the explicit per-player cheats bypass the normal drain.
                    if (((Player)entity).godModeEnabled || ((Player)entity).infiniteRunEnabled) {
                        ((Player)entity).setRunEnergyPercent(100);
                    } else {
                        value = value4 = entity;
                        int value5 = GameUtil.clampRunWeightForEnergyDrain(0, 64, (int)((Player)value).carriedWeight);
                        value5 = 67 + value5 * 67 / 64;
                        ((Player)entity).addRunEnergyRaw(-value5);
                    }
                    if (((Player)entity).getRunEnergyPercent() <= 0) {
                        this.setRunning(false);
                    }
                    // Keep the client's run-energy orb synchronized with the
                    // server while running. Revision 443 uses opcode 226 for
                    // this update; suppressing it left the client showing stale
                    // energy after the server had already exhausted it.
                    ((Player)entity).packetSender.sendRunEnergy();
                }
            }
            this.entity.getPosition().translate(value3, value2);
            this.entity.recordMovementTick();
            this.entity.setRunDirection(movementStep.getDirection());
            this.stepHistory.add(movementStep);
        }
        if (this.entity.isPlayer() && ServerSettings.clientBuild == 443
                && !((Player)this.entity).isBot) {
            // The native client bypasses the legacy movement callbacks below.
            // Refresh music here for walking, region rebuilds and teleports alike.
            new MusicManager().updateForPlayerPosition((Player)this.entity);
            return;
        }
        if (this.entity.isPlayer()) {
            Player player = (Player)this.entity;
            if (player.isInWilderness() && !player.wildernessEntryAcknowledged) {
                player.wildernessEntryAcknowledged = true;
            }
            if (this.entity.isWildernessCoordinate(this.entity.getPosition().getX(), this.entity.getPosition().getY()) && !player.wildernessEntryAcknowledged) {
                player.wildernessEntryAcknowledged = true;
                value = player;
                ((Player)value).packetSender.showInterface(1908);
                EntityTargetMovement.clearMovementTarget(player);
                this.clear();
            }
            if (ServerSettings.clientBuild != 443 || player.isBot) {
                GameplayHelper.refreshPlayerAreaOverlay(player);
            }
            player.getQuestManager().handleMovementStep();
            DesertHeatManager.updateDesertHeatHazard(player);
            CaveLightManager.updateCaveLightHazards(player);
            Player player2 = player;
            if (player.currentBotRoute != null && !player.botRouteActionPending && !player.botRouteTravelPending) {
                player.continueBotRoute();
            }
            BankPinManager.updateSmokeDungeonDamage(player);
            new MusicManager().updateForPlayerPosition(player);
        }
    }

    public final void clear() {
        boolean enabled = false;
        Object value = this;
        this.runPath = enabled;
        this.steps.clear();
        value = this.entity.getPosition();
        this.steps.add(new MovementStep(this, ((Position)value).getX(), ((Position)value).getY(), -1));
    }

    public final void removeFirstStep() {
        if (this.steps.size() > 0) {
            this.steps.removeFirst();
        }
    }

    public final void addStep(Position position) {
        if (this.steps.size() == 0) {
            this.clear();
        }
        MovementStep movementStep = (MovementStep)this.steps.peekLast();
        int x = position.getX() - movementStep.getX();
        int y = position.getY() - movementStep.getY();
        int value = Math.max(Math.abs(x), Math.abs(y));
        int index = 0;
        while (index < value) {
            Object value2;
            if (x < 0) {
                ++x;
            } else if (x > 0) {
                --x;
            }
            if (y < 0) {
                ++y;
            } else if (y > 0) {
                --y;
            }
            int y2 = position.getY() - y;
            int x2 = position.getX() - x;
            MovementQueue movementQueue = this;
            int value3 = 100;
            if (movementQueue.entity.isPlayer()) {
                value2 = (Player)movementQueue.entity;
                if (((Player)value2).botEnabled) {
                    value3 = 2000;
                }
            }
            if (movementQueue.steps.size() < value3) {
                value2 = (MovementStep)movementQueue.steps.peekLast();
                value3 = x2 - ((Position)value2).getX();
                int y3 = y2 - ((Position)value2).getY();
                if ((value3 = GameUtil.getDirectionForDelta(value3, y3)) >= 0) {
                    movementQueue.steps.add(new MovementStep(movementQueue, x2, y2, value3));
                }
            }
            ++index;
        }
    }

    public final void setRunning(boolean running) {
        this.running = running;
        if (this.entity.isPlayer()) {
            Player player = (Player)this.entity;
            // Saved movement state is restored before the login response. Keep
            // that restore silent; post-login config sync sends varp 173.
            if (player.loginInitializationComplete) {
                player.packetSender.sendConfig(173, this.running ? 1 : 0);
            }
        }
    }

    public final boolean isRunning() {
        return this.running;
    }

    public final void setRunPath(boolean runPath) {
        this.runPath = runPath;
    }

    public final boolean isRunPath() {
        return this.runPath;
    }

    public final void clearMovementActions() {
        if (this.entity.isPlayer()) {
            Player player = (Player)this.entity;
            player.getAttributes().put("isBanking", Boolean.FALSE);
            player.getAttributes().put("isShopping", Boolean.FALSE);
            GameplayHelper.declineTrade(player);
            PartyRoomManager.returnStagedChestItems(player);
            if (player.getQuestState(0) == 1) {
                Player player2 = player;
                player2.packetSender.closeInterfaces();
            }
            player.queuedLevelUpSkillIds.clear();
            player.currentLevelUpSkillId = -1;
        }
        this.entity.getUpdateState().setFaceEntity(65535);
    }

    private static boolean isActiveStileEntryTile(Player player,
                                                   int destinationX,
                                                   int destinationY) {
        if (player == null) {
            return false;
        }

        int objectId = player.getInteractionTargetId();
        if (objectId < 0) {
            return false;
        }

        ObjectDefinition definition = ObjectDefinition.forId(objectId);
        if (!StileObject.isStile(objectId)) {
            return false;
        }

        int plane = player.getInteractionTargetPlane() & 3;
        if (player.getPosition().getPlane() != plane) {
            return false;
        }

        int objectX = player.getInteractionTargetX();
        int objectY = player.getInteractionTargetY();
        LoadedWorldObject stile = WorldObjectLookup.findObjectByIdAt(
                objectId, objectX, objectY, plane);
        if (stile == null || !StileObject.isStile(stile.getWorldObject())) {
            return false;
        }

        if (definition == null) {
            return false;
        }

        int footprintWidth = Math.max(1,
                definition.getWidthForOrientation(stile.getOrientation()));
        int footprintLength = Math.max(1,
                definition.getLengthForOrientation(stile.getOrientation()));

        return destinationX >= objectX
                && destinationX < objectX + footprintWidth
                && destinationY >= objectY
                && destinationY < objectY + footprintLength;
    }

    private boolean canStep(int value8, int value23) {
        Entity entity;
        int value;
        int value2;
        Entity[] entityArray;
        Object value3;
        boolean enabled = false;
        int[][] integerValues = collisionBypassTiles;
        int index = 0;
        while (index < integerValues.length) {
            int[] bypassTile = integerValues[index];
            if (this.entity.getPosition().getX() + value8 == bypassTile[0]
                    && this.entity.getPosition().getY() + value23 == bypassTile[1]
                    && (bypassTile.length < 3 || this.entity.getPosition().getPlane() == bypassTile[2])) {
                enabled = true;
                break;
            }
            ++index;
        }
        if (!enabled && this.entity.isPlayer()) {
            Player player = (Player)this.entity;
            enabled = isActiveStileEntryTile(player,
                    this.entity.getPosition().getX() + value8,
                    this.entity.getPosition().getY() + value23);
        }
        if (!enabled && !this.entity.canTravelBetween(this.entity.getPosition().getX(), this.entity.getPosition().getY(), this.entity.getPosition().getX() + value8, this.entity.getPosition().getY() + value23, this.entity.getPosition().getPlane(), this.entity.getSize(), this.entity.getSize())) {
            return false;
        }
        if (this.entity.isNpc()) {
            boolean enabled2;
            canStepControlExit1: {
                Npc npc = (Npc)this.entity;
                value3 = npc;
                int value4 = value23;
                index = value8;
                Npc npc2 = npc;
                entityArray = World.getPlayers();
                value2 = entityArray.length;
                value = 0;
                while (value < value2) {
                    entity = entityArray[value];
                    if (entity != null && !npc2.isDead() && !entity.isDead() && entity.getPosition().getPlane() == npc2.getPosition().getPlane() && GameUtil.isWithinDistance(npc2.getPosition().getX(), npc2.getPosition().getY(), entity.getPosition().getX(), entity.getPosition().getY(), entity.getSize() + npc2.getSize()) && Npc.wouldCollideWithPlayer(npc2, (Player)entity, index, value4)) {
                        enabled2 = true;
                        break canStepControlExit1;
                    }
                    ++value;
                }
                enabled2 = false;
            }
            if (enabled2) {
                return false;
            }
            if (((Npc)value3).wouldCollideWithNpc(value8, value23)) {
                return false;
            }
        }
        if (this.entity.isPlayer()) {
            Player player = (Player)this.entity;
            value3 = player;
            if (!player.forcedMovementActive) {
                boolean enabled3;
                canStepControlExit2: {
                    int value5 = value23;
                    index = value8;
                    Object value6 = value3;
                    entityArray = World.getNpcs();
                    value2 = entityArray.length;
                    value = 0;
                    while (value < value2) {
                        entity = entityArray[value];
                        if (entity != null && !((Entity)value6).isDead() && !entity.isDead() && entity.getPosition().getPlane() == ((Entity)value6).getPosition().getPlane() && (((Npc)entity).getNpcId() == 1459 || ((Npc)entity).getNpcId() == 1461 || ((Npc)entity).getNpcId() == 1462) && GameUtil.isWithinDistance(((Entity)value6).getPosition().getX(), ((Entity)value6).getPosition().getY(), entity.getPosition().getX(), entity.getPosition().getY(), entity.getSize() + ((Entity)value6).getSize()) && Player.isNpcAtRelativeOffset((Player)value6, (Npc)entity, index, value5)) {
                            enabled3 = true;
                            break canStepControlExit2;
                        }
                        ++value;
                    }
                    enabled3 = false;
                }
                if (enabled3) {
                    return false;
                }
            }
        }
        if (this.entity.getCombatTarget() != null) {
            return !EntityTargetMovement.canReachTarget(this.entity, this.entity.getCombatTarget());
        }
        return true;
    }
}
