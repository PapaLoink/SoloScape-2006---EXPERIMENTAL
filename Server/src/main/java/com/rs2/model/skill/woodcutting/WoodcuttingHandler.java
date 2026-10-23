package com.rs2.model.skill.woodcutting;

import com.rs2.ServerSettings;
import com.rs2.model.Entity;
import com.rs2.model.GameplayHelper;
import com.rs2.model.Position;
import com.rs2.model.World;
import com.rs2.model.c.ProjectileDefinition;
import com.rs2.model.combat.ProjectileTiming;
import com.rs2.model.item.ItemStack;
import com.rs2.model.npc.Npc;
import com.rs2.model.npc.NpcDefinition;
import com.rs2.model.objects.DynamicObject;
import com.rs2.model.objects.ObjectManager;
import com.rs2.model.objects.ObjectDefinition;
import com.rs2.model.objects.WorldObject;
import com.rs2.model.player.Player;
import com.rs2.model.randomevent.SkillRandomEventNpc;
import com.rs2.model.skill.GatheringToolDefinition;
import com.rs2.model.skill.ItemCombinationHandler;
import com.rs2.model.skill.SkillActionHelper;
import com.rs2.model.skill.woodcutting.TreeDefinition;
import com.rs2.model.skill.woodcutting.WoodcuttingTask;
import com.rs2.model.task.CycleEventHandler;
import com.rs2.util.GameUtil;
import com.rs2.util.GameplayTrace;

public class WoodcuttingHandler {
    private int targetIndex;
    private int endDelay;
    private byte deltaX;
    private byte deltaY;
    private ProjectileDefinition projectileDefinition;
    private Position sourcePosition;
    private int sourceSize;

    public static void startWoodcutting(Player player, int value8, int value22, int value32, boolean enabled2) {
        Object value4;
        Object value5;
        Object value6;
        if (GameplayTrace.enabled()) {
            GameplayTrace.log("woodcutting enter player=" + GameplayTrace.describe(player) + " targetId=" + value8 + " x=" + value22 + " y=" + value32 + " npcTree=" + enabled2);
        }
        if (!enabled2) {
            ObjectManager.getInstance();
            value6 = ObjectManager.findDynamicObjectAt(value22, value32, player.getPosition().getPlane());
            if (value6 != null && ((DynamicObject)value6).getWorldObject().getObjectId() != value8) {
                if (GameplayTrace.enabled()) {
                    GameplayTrace.log("woodcutting blocked dynamic-object-mismatch player=" + GameplayTrace.describe(player) + " targetId=" + value8 + " dynamicId=" + ((DynamicObject)value6).getWorldObject().getObjectId() + " x=" + value22 + " y=" + value32);
                }
                if (player.botEnabled) {
                    player.interactWithBotObjectTargets(player.botInteractionTargetIds);
                }
                return;
            }
        } else {
            value6 = Npc.findByDefinitionIdAtPosition(value8, new Position(value22, value32, player.getPosition().getPlane()));
            if (value6 == null) {
                if (GameplayTrace.enabled()) {
                    GameplayTrace.log("woodcutting blocked missing-npc-tree player=" + GameplayTrace.describe(player) + " targetId=" + value8 + " x=" + value22 + " y=" + value32);
                }
                return;
            }
        }
        if ((value6 = TreeDefinition.forTargetId(value8, enabled2)) == null) {
            if (GameplayTrace.enabled()) {
                GameplayTrace.log("woodcutting blocked missing-tree-definition player=" + GameplayTrace.describe(player) + " targetId=" + value8 + " x=" + value22 + " y=" + value32 + " npcTree=" + enabled2);
            }
            return;
        }
        if (!ServerSettings.woodcuttingEnabled) {
            if (GameplayTrace.enabled()) {
                GameplayTrace.log("woodcutting blocked disabled player=" + GameplayTrace.describe(player) + " tree=" + value6);
            }
            Player player2 = player;
            player2.packetSender.sendGameMessage("This skill is currently disabled.");
            return;
        }
        GatheringToolDefinition gatheringToolDefinition = ItemCombinationHandler.findUsableGatheringTool(player, 8);
        if (gatheringToolDefinition == null) {
            if (GameplayTrace.enabled()) {
                GameplayTrace.log("woodcutting blocked no-axe player=" + GameplayTrace.describe(player) + " tree=" + value6);
            }
            Player player3 = player;
            player3.packetSender.sendGameMessage("You do not have an axe which you have the woodcutting level to use.");
            if (player.botEnabled) {
                player.currentBotTask.startWalkToBank(player);
            }
            return;
        }
        // Historical Ents could be struck even when the player lacked the level for the
        // underlying tree, and they never required inventory space because they yield no logs.
        if (!enabled2 && player.getSkillManager().getCurrentLevels()[8] < ((TreeDefinition)((Object)value6)).getRequiredLevel()) {
            if (GameplayTrace.enabled()) {
                GameplayTrace.log("woodcutting blocked level player=" + GameplayTrace.describe(player) + " current=" + player.getSkillManager().getCurrentLevels()[8] + " required=" + ((TreeDefinition)((Object)value6)).getRequiredLevel() + " tree=" + value6);
            }
            Player player4 = player;
            player4.packetSender.sendGameMessage("You need a Woodcutting level of " + ((TreeDefinition)((Object)value6)).getRequiredLevel() + " to cut this tree.");
            return;
        }
        if (!enabled2 && value6 != TreeDefinition.HOLLOW_TREE
                && ((TreeDefinition)((Object)value6)).getLogItemId() != -1) {
            value5 = new ItemStack(((TreeDefinition)((Object)value6)).getLogItemId(), 1);
            if (player.getInventoryManager().getContainer().getFirstFreeSlot() == -1) {
                if (GameplayTrace.enabled()) {
                    GameplayTrace.log("woodcutting blocked full-inventory player=" + GameplayTrace.describe(player) + " tree=" + value6);
                }
                Player player5 = player;
                player5.packetSender.sendGameMessage("Your inventory is too full to hold any more " + ((ItemStack)value5).getDefinition().getName().toLowerCase() + ".");
                player5 = player;
                player5.packetSender.sendSoundEffect(1878, 1, 0);
                if (player.botEnabled) {
                    player.currentBotTask.startWalkToBank(player);
                }
                return;
            }
        }
        if (player.getQuestState(0) == 8) {
            player.getDialogueManager().showTutorialInstructionOverlay("@blu@Please wait.", "", "Your character is now attempting to cut down the tree. Sit back", "for a moment while he does all the hard work.", "", true);
        } else if (player.getQuestState(0) == 9) {
            player.getQuestManager().refreshQuestJournal();
        } else {
            value4 = player;
            ((Player)value4).packetSender.sendGameMessage("You swing your axe at the " + (value6 == TreeDefinition.VINES ? "vines" : "tree") + ".");
        }
        if (value6 == TreeDefinition.VINES && player.botEnabled) {
            player.botRouteActionPending = true;
        }
        int value7 = player.nextActionSequence();
        // Roll on the validated initial attempt, before scheduling any chopping cycles.
        // Clicking an existing Ent keeps its own axe-breaking behavior.
        if (!enabled2) {
            if (AxeHeadRandomEvent.tryTriggerInitialHit(player, gatheringToolDefinition)) {
                return;
            }
            if (!player.botEnabled && !player.isInTutorialIsland()
                    && ServerSettings.randomEventsMode == 0 && player.ownedNpc == null
                    && GameUtil.randomInt(2001) == 0) {
                GameplayHelper.spawnSkillRandomEventNpc(player, SkillRandomEventNpc.TREE_SPIRIT);
                player.getUpdateState().setAnimation(-1);
                return;
            }
        }
        faceTree(player, value8, value22, value32);
        player.packetSender.sendSoundEffect(472, 1, 0);
        World.scheduleTickTask(new WoodcuttingSwingSoundTask(player, value7));
        player.getUpdateState().setAnimation(gatheringToolDefinition.getGatherAnimationId(), 0);
        player.gatheringHazardCounter = 0;
        if (player.getQuestState(0) == 1 && ServerSettings.randomEventsMode == 0 && GameUtil.randomInt(800) == 0 && ((TreeDefinition)((Object)value6)).getEntNpcIds() != null && !player.botEnabled && !player.isInTutorialIsland()) {
            int objectIdIndex = TreeDefinition.getObjectIdIndex(value8, (TreeDefinition)((Object)value6)) >= ((TreeDefinition)((Object)value6)).getEntNpcIds().length ? 0 : TreeDefinition.getObjectIdIndex(value8, (TreeDefinition)((Object)value6));
            objectIdIndex = ((TreeDefinition)((Object)value6)).getEntNpcIds()[objectIdIndex];
            if (NpcDefinition.isDefined(objectIdIndex)) {
                value5 = new Npc(objectIdIndex);
                value4 = SkillActionHelper.findWorldObjectById(value8, value22, value32, player.getPosition().getPlane());
                ObjectManager.getInstance().addDynamicObject(new DynamicObject(ServerSettings.placeholderObjectId, value22, value32, player.getPosition().getPlane(), ((WorldObject)value4).getOrientation(), ((WorldObject)value4).getType(), value8, 15), true);
                GameplayHelper.spawnNpcWithRemovalDelay((Npc)value5, value22, value32, player.getPosition().getPlane(), 15);
            }
        }
        if (GameplayTrace.enabled()) {
            GameplayTrace.log("woodcutting scheduled player=" + GameplayTrace.describe(player) + " seq=" + value7 + " tree=" + value6 + " objectId=" + value8 + " x=" + value22 + " y=" + value32 + " animation=" + gatheringToolDefinition.getGatherAnimationId());
        }
        player.setActiveCycleEvent(new WoodcuttingTask(player, value7, (TreeDefinition)((Object)value6), value22, value32, gatheringToolDefinition, value8));
        CycleEventHandler.getInstance().schedule(player, player.getActiveCycleEvent(), 4);
    }

    static void faceTree(Player player, int objectId, int x, int y) {
        int plane = player.getPosition().getPlane();
        int width = 1;
        int length = 1;
        // Ent NPC IDs must not be interpreted as object definitions.
        if (TreeDefinition.forObjectId(objectId) != null) {
            ObjectDefinition definition = ObjectDefinition.forId(objectId);
            if (definition != null) {
                int orientation = SkillActionHelper.getObjectOrientation(objectId, x, y, plane);
                width = definition.getWidthForOrientation(orientation);
                length = definition.getLengthForOrientation(orientation);
            }
        }
        player.getUpdateState().setFaceEntity(65535);
        player.getUpdateState().setFacePosition(new Position(x, y, plane), width, length);
    }

    public WoodcuttingHandler(Position position, int sourceSize, Position position2, int targetIndex, ProjectileDefinition projectileDefinition) {
        this.sourcePosition = position;
        this.sourceSize = sourceSize;
        this.targetIndex = targetIndex;
        this.projectileDefinition = projectileDefinition;
        sourceSize = GameUtil.getDistance(position, position2);
        targetIndex = projectileDefinition.getTiming().getSpeed();
        this.endDelay = projectileDefinition.getTiming().getStartDelay() + targetIndex + sourceSize * 5;
        this.deltaX = (byte)(position2.getX() - position.getX());
        this.deltaY = (byte)(position2.getY() - position.getY());
    }

    public WoodcuttingHandler(Entity entity, Entity entity2, Position position, ProjectileDefinition projectileDefinition) {
        this(entity.getPosition(), entity.getSize(), position, entity2.isPlayer() ? -entity2.getIndex() - 1 : entity2.getIndex() + 1, projectileDefinition);
    }

    public WoodcuttingHandler(Entity entity, Entity entity2, ProjectileDefinition projectileDefinition) {
        this(entity, entity2, entity2.getPosition(), projectileDefinition);
    }

    /** Override arrival for scripted projectiles with a fixed client-cycle timeline. */
    public WoodcuttingHandler setEndDelay(int endDelay) {
        if (endDelay <= this.projectileDefinition.getTiming().getStartDelay()) {
            throw new IllegalArgumentException("Projectile arrival must follow its launch");
        }
        this.endDelay = endDelay;
        return this;
    }

    /** Arrival time in 20 ms client cycles, including the launch delay. */
    public int getEndDelay() {
        return this.endDelay;
    }

    public void sendProjectileToNearbyPlayers() {
        if (this.projectileDefinition.getProjectileId() == -1) {
            return;
        }
        ProjectileTiming projectileTiming = this.projectileDefinition.getTiming();
        Player[] playerArray = World.getPlayers();
        int length = playerArray.length;
        int index = 0;
        while (index < length) {
            Player player = playerArray[index];
            if (player != null && this.sourcePosition.isWithinViewport(player.getPosition())) {
                player.packetSender.sendProjectile(this.sourcePosition, this.sourceSize, this.targetIndex, this.deltaX, this.deltaY, this.projectileDefinition.getProjectileId(), projectileTiming.getStartDelay(), this.endDelay, projectileTiming.getStartHeight(), projectileTiming.getEndHeight(), projectileTiming.getSlope());
            }
            ++index;
        }
    }
}
