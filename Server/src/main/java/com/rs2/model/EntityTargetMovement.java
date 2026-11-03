package com.rs2.model;

import com.rs2.ServerSettings;
import com.rs2.model.Entity;
import com.rs2.model.Position;
import com.rs2.model.combat.CombatManager;
import com.rs2.model.combat.CombatType;
import com.rs2.model.gameplay.castlewars.CastleWarsManager;
import com.rs2.model.npc.Npc;
import com.rs2.model.player.PetManager;
import com.rs2.model.player.Player;
import com.rs2.model.skill.GatheringToolDefinition;
import com.rs2.model.skill.ItemCombinationHandler;
import com.rs2.model.skill.mining.RuneEssenceMiningTask;
import com.rs2.model.skill.runecrafting.RunecraftingHandler;
import com.rs2.model.task.CycleEventHandler;
import com.rs2.util.GameUtil;
import com.rs2.util.path.PathFinder;

public class EntityTargetMovement {
    private Entity entity;

    public static void startRuneEssenceMining(Player player) {
        GatheringToolDefinition gatheringToolDefinition = ItemCombinationHandler.findUsableGatheringTool(player, 14);
        if (gatheringToolDefinition == null) {
            Player player2 = player;
            player2.packetSender.sendGameMessage("You do not have a pickaxe that you can use.");
            if (player.botEnabled) {
                player.currentBotTask.startWalkToBank(player);
            }
            return;
        }
        if (player.getInventoryManager().getContainer().getFreeSlots() <= 0) {
            Player player3 = player;
            player3.packetSender.sendGameMessage("Not enough space in your inventory.");
            player3 = player;
            player3.packetSender.sendSoundEffect(1878, 1, 0);
            if (player.botEnabled) {
                player.currentBotTask.startWalkToBank(player);
            }
            return;
        }
        int value = player.nextActionSequence();
        int skillManager = player.getSkillManager().getBaseLevel(14) >= 30 && player.isMember() && !ServerSettings.freeToPlayWorld ? RunecraftingHandler.PURE_ESSENCE_ITEM_ID : 1436;
        int gatherAnimationId = gatheringToolDefinition.getGatherAnimationId();
        gatheringToolDefinition.getGraphicId();
        int toolSpeed = (int)gatheringToolDefinition.getToolSpeed();
        Player player4 = player;
        player4.packetSender.sendGameMessage("You swing your pick at the rock.");
        player.getUpdateState().setAnimation(gatherAnimationId);
        player.setActiveCycleEvent(new RuneEssenceMiningTask(player, value, gatherAnimationId, skillManager));
        CycleEventHandler.getInstance().schedule(player, player.getActiveCycleEvent(), toolSpeed);
    }

    public EntityTargetMovement(Entity entity) {
        int[][] nArrayArray = new int[8][];
        nArrayArray[0] = new int[]{-1, 1};
        int[] integerValues = new int[2];
        integerValues[1] = 1;
        nArrayArray[1] = integerValues;
        nArrayArray[2] = new int[]{1, 1};
        int[] integerValues2 = new int[2];
        integerValues2[0] = -1;
        nArrayArray[3] = integerValues2;
        int[] integerValues3 = new int[2];
        integerValues3[0] = 1;
        nArrayArray[4] = integerValues3;
        nArrayArray[5] = new int[]{-1, -1};
        int[] integerValues4 = new int[2];
        integerValues4[1] = -1;
        nArrayArray[6] = integerValues4;
        nArrayArray[7] = new int[]{1, -1};
        int[][][] nArrayArray2 = new int[][][]{new int[][]{new int[2], {-1, 2}, new int[2], {-2, 1}, new int[2], new int[2], new int[2], new int[2]}, new int[][]{new int[2], new int[2], new int[2], new int[2], new int[2], new int[2], new int[2], new int[2]}, new int[][]{new int[2], {1, 2}, new int[2], new int[2], {2, 1}, new int[2], new int[2], new int[2]}, new int[][]{new int[2], new int[2], new int[2], new int[2], new int[2], new int[2], new int[2], new int[2]}, new int[][]{new int[2], new int[2], new int[2], new int[2], new int[2], new int[2], new int[2], new int[2]}, new int[][]{new int[2], new int[2], new int[2], {-2, -1}, new int[2], new int[2], {-1, -2}, new int[2]}, new int[][]{new int[2], new int[2], new int[2], new int[2], new int[2], new int[2], new int[2], new int[2]}, new int[][]{new int[2], new int[2], new int[2], new int[2], {2, -1}, new int[2], {1, -2}, new int[2]}};
        this.entity = entity;
    }

    public void process() {
        Npc npc;
        if (this.entity.isNpc() && (npc = (Npc)this.entity).getMovementTarget() == null && npc.getForcedCombatTarget() != null) {
            npc.setMovementTarget(npc.getForcedCombatTarget());
            CombatManager.startCombat(npc, npc.getForcedCombatTarget());
            npc.getUpdateState().setFacePosition(npc.getForcedCombatTarget().getPosition());
        }
        if (this.entity.getMovementTarget() != null) {
            this.processTargetMovement();
        }
    }

    public void processTargetMovement() {
        Object value;
        Entity entity = this.entity.getMovementTarget();
        if (entity == null || entity.isDead() || this.entity.isDead() || !entity.getPosition().isWithinDistance(this.entity.getPosition(), 20)) {
            boolean enabled = false;
            if (this.entity.isNpc() && ((Npc)(value = (Npc)this.entity)).getForcedCombatTarget() != null) {
                enabled = true;
            }
            if (!enabled) {
                EntityTargetMovement.clearMovementTarget(this.entity);
                CombatManager.stopCombat(this.entity);
                return;
            }
        }
        if (this.entity.isTargetMovementDisabled()) {
            return;
        }
        this.entity.getUpdateState().setFaceEntity(entity.getEncodedIndex());
        if (this.entity.isMovementLocked() || this.entity.isStunned()) {
            return;
        }
        if (this.entity.isPlayer() && entity.isNpc() && this.entity.isOverlapping(entity)) {
            this.stepPlayerAwayFromNpc(entity);
            return;
        }
        if (this.entity.isPlayer() && entity.isNpc() && (((Npc)entity).isBanker() || ((Npc)entity).getNpcId() == 736 || ((Npc)entity).getNpcId() == 745 || ((Npc)entity).getNpcId() == 3859 || ((Npc)entity).getNpcId() == 482)) {
            Player player = (Player)this.entity;
            value = ((Npc)entity).getFacingInteractionPosition(2);
            PathFinder.getInstance();
            PathFinder.findPath(player, ((Position)value).getX(), ((Position)value).getY(), true, 0, 0);
            return;
        }
        if (this.entity.isPlayer() && this.entity.getCombatTarget() == null) {
            Player player = (Player)this.entity;
            if (entity.isPlayer() && this.entity.getInteractionTarget() == null) {
                int position = entity.getPosition().getPreviousX();
                int position2 = entity.getPosition().getPreviousY();
                PathFinder.getInstance();
                PathFinder.findPath(player, position, position2, true, 0, 0);
                return;
            }
            EntityTargetMovement.pathPlayerAdjacentToTarget(player, entity);
            return;
        }
        if (this.entity.isPlayer()) {
            Player player = (Player)this.entity;
            if (this.entity.getAttackRange() < 2 && entity.getSize() < 2) {
                EntityTargetMovement.pathPlayerAdjacentToTarget(player, entity);
                return;
            }
            if (this.entity.isOverlapping(entity)) {
                this.moveAwayFromOverlap();
                return;
            }
            if (EntityTargetMovement.canReachTarget(this.entity, entity)) {
                return;
            }
            PathFinder.getInstance();
            PathFinder.findPath(player, entity.getPosition().getX(), entity.getPosition().getY(), true, 0, 0);
            return;
        }
        if (this.entity.isNpc()) {
            Npc npc = (Npc)this.entity;
            if (this.entity.isOverlapping(entity)) {
                this.moveAwayFromOverlap();
                return;
            }
            if (EntityTargetMovement.canReachTarget(this.entity, entity)) {
                return;
            }

            boolean isPet = false;
            int[][] petPairs = PetManager.petItemNpcPairs;
            for (int index = 0; index < petPairs.length; index++) {
                int[] pair = petPairs[index];
                if (pair != null && pair.length > 1 && npc.getNpcId() == pair[1]) {
                    isPet = true;
                    break;
                }
            }

            if (!GameUtil.isWithinDistance(
                    npc.getPosition(), npc.getSpawnPosition(),
                    npc.getDefinition().getChaseRadius())
                    && !isPet && npc.getForcedCombatTarget() == null) {
                CombatManager.stopCombat(npc);
                npc.nextActionSequence();
                npc.setInteractionTarget(null);
                npc.setCombatTarget(null);
                npc.setActiveCycleEvent(null);
                npc.getUpdateState().setFaceEntity(-1);
                EntityTargetMovement.clearMovementTarget(npc);
                PathFinder.findPath(npc,
                        npc.getSpawnPosition().getX(),
                        npc.getSpawnPosition().getY(),
                        false, 1, 1);
                return;
            }

            PathFinder.findPathToAdjacent(npc,
                    entity.getPosition().getX(),
                    entity.getPosition().getY(),
                    Math.max(1, entity.getSize()),
                    Math.max(1, entity.getSize()),
                    true);
        }
    }

    public static void pathPlayerAdjacentToTarget(Player player, Entity entity) {
        if (player == null || entity == null) {
            return;
        }

        PathFinder.findPathToAdjacent(player,
                entity.getPosition().getX(),
                entity.getPosition().getY(),
                Math.max(1, entity.getSize()),
                Math.max(1, entity.getSize()),
                true);
    }

    public static void clearMovementTarget(Entity entity) {
        entity.getUpdateState().setFaceEntity(65535);
        entity.setMovementTarget(null);
    }

    public static boolean canReachTarget(Entity entity, Entity entity2) {
        return EntityTargetMovement.canReachTarget(entity, entity2, entity.getAttackRange());
    }

    public static boolean canReachTarget(Entity entity, Entity entity2, int value2) {
        if (entity.isPlayer() && entity2.isPlayer()
                && CastleWarsManager.canBotAttackAcrossCastleLevels(
                        (Player)entity, (Player)entity2)) {
            return entity.isWithinReach(entity2, value2);
        }
        if (entity.isOverlapping(entity2)) {
            return false;
        }
        if (entity.getCombatTarget() != null && entity.isPlayer() && value2 == 1 && entity2.getSize() < 2 && !entity2.isMoving() && entity.getPosition().getX() != entity2.getPosition().getX() && entity.getPosition().getY() != entity2.getPosition().getY()) {
            return false;
        }
        if (!entity.isWithinReach(entity2, value2)) {
            return false;
        }
        if (entity2.isDoorSupportNpc()) {
            return true;
        }
        if (entity2.isNpc()) {
            Npc npc = (Npc)entity2;
            if (npc.getNpcId() == 221) {
                return true;
            }
            if (npc.getNpcId() == 2892) {
                return true;
            }
        }
        return GameUtil.hasClearPath(entity.getPosition(), entity2.getPosition(), value2 < 2);
    }

    public static boolean canReachTarget(Entity entity, Entity entity2, int value2,
                                         CombatType combatType) {
        if (entity.isPlayer() && entity2.isPlayer()
                && CastleWarsManager.isCastleWallCrossLevelPair(
                        (Player)entity, (Player)entity2)) {
            if (combatType == CombatType.MELEE) {
                // Only permit adjacent melee if both ground tiles genuinely
                // connect. Projectile-clipping near Castle Wars battlements
                // may mark a reachable enemy as being on the wall.
                return value2 <= 1 && entity.isWithinReach(entity2, value2)
                        && GameUtil.hasClearPath(entity.getPosition(),
                                entity2.getPosition(), true);
            }
            return CastleWarsManager.canAttackAcrossCastleLevels(
                    (Player)entity, (Player)entity2, combatType)
                    && entity.isWithinReach(entity2, value2);
        }
        return canReachTarget(entity, entity2, value2);
    }

    public void moveAwayFromOverlap() {
        Npc npc;
        this.entity.getMovementQueue().clear();
        int position = this.entity.getPosition().getX();
        int position2 = this.entity.getPosition().getY();
        Npc npc2 = npc = this.entity.isNpc() ? (Npc)this.entity : null;
        if (this.entity.canStepToOffset(-1, 0) && (npc == null || !npc.wouldCollideWithNpc(-1, 0))) {
            this.entity.queuePathTo(position - 1, position2, true);
            return;
        }
        if (this.entity.canStepToOffset(1, 0) && (npc == null || !npc.wouldCollideWithNpc(1, 0))) {
            this.entity.queuePathTo(position + 1, position2, true);
            return;
        }
        if (this.entity.canStepToOffset(0, -1) && (npc == null || !npc.wouldCollideWithNpc(0, -1))) {
            this.entity.queuePathTo(position, position2 - 1, true);
            return;
        }
        if (this.entity.canStepToOffset(0, 1) && (npc == null || !npc.wouldCollideWithNpc(0, 1))) {
            this.entity.queuePathTo(position, position2 + 1, true);
        }
    }

    public static boolean isDiagonalTo(Position position, Position position2) {
        return position.getX() != position2.getX() && position.getY() != position2.getY();
    }

    private void stepPlayerAwayFromNpc(Entity target) {
        MovementQueue queue = this.entity.getMovementQueue();
        queue.clear();
        int x = this.entity.getPosition().getX();
        int y = this.entity.getPosition().getY();
        // Prefer a straight step. Diagonals use the same collision checks as
        // normal walking, including both edges of a corner.
        int[][] offsets = {{-1, 0}, {1, 0}, {0, -1}, {0, 1},
                {-1, -1}, {1, -1}, {-1, 1}, {1, 1}};
        for (int[] offset : offsets) {
            if (!this.entity.canStepToOffset(offset[0], offset[1])) {
                continue;
            }
            int nextX = x + offset[0];
            int nextY = y + offset[1];
            boolean occupied = false;
            for (Npc npc : World.getNpcs()) {
                if (npc == null || npc.isDead()
                        || npc.getPosition().getPlane() != this.entity.getPosition().getPlane()) {
                    continue;
                }
                if (nextX < npc.getPosition().getX() + npc.getSize()
                        && nextX + this.entity.getSize() > npc.getPosition().getX()
                        && nextY < npc.getPosition().getY() + npc.getSize()
                        && nextY + this.entity.getSize() > npc.getPosition().getY()) {
                    occupied = true;
                    break;
                }
            }
            // Also check the target when it is not in the world list yet.
            if (occupied || (nextX < target.getPosition().getX() + target.getSize()
                    && nextX + this.entity.getSize() > target.getPosition().getX()
                    && nextY < target.getPosition().getY() + target.getSize()
                    && nextY + this.entity.getSize() > target.getPosition().getY())) {
                continue;
            }
            queue.addStep(new Position(nextX, nextY, this.entity.getPosition().getPlane()));
            queue.removeFirstStep();
            return;
        }
        // Inside a large NPC, leaving its footprint may require several ticks.
        // Use a clipped route to its edge, but take only one step this tick.
        if (target.getSize() > 1 && PathFinder.findPathToAdjacent(this.entity,
                target.getPosition().getX(), target.getPosition().getY(),
                target.getSize(), target.getSize(), false)) {
            while (queue.getSteps().size() > 1) {
                queue.getSteps().removeLast();
            }
        }
    }
}

