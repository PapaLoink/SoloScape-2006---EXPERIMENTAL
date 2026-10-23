package com.rs2.model.interaction;

import com.rs2.ServerSettings;
import com.rs2.model.Entity;
import com.rs2.model.GameplayHelper;
import com.rs2.model.Position;
import com.rs2.model.combat.AttackStyleDefinition;
import com.rs2.model.dialogue.DialogueManager;
import com.rs2.model.gameplay.castlewars.CastleWarsEngineeringManager;
import com.rs2.model.gameplay.castlewars.CastleWarsManager;
import com.rs2.model.gameplay.godwars.GodWarsDungeonManager;
import com.rs2.model.gameplay.partyroom.PartyRoomManager;
import com.rs2.model.interaction.InteractionDispatcher;
import com.rs2.model.interaction.MiningShortcutTask;
import com.rs2.model.objects.ObjectDefinition;
import com.rs2.model.objects.WorldObject;
import com.rs2.model.objects.functions.PickableObjectHandler;
import com.rs2.model.player.BankManager;
import com.rs2.model.player.Player;
import com.rs2.model.skill.GatheringToolDefinition;
import com.rs2.model.skill.ItemCombinationHandler;
import com.rs2.model.skill.SkillActionHelper;
import com.rs2.model.skill.crafting.CraftingHandler;
import com.rs2.model.skill.smithing.SmeltingHandler;
import com.rs2.model.skill.thieving.StallThievingHandler;
import com.rs2.model.skill.thieving.ThievingObjectHandler;
import com.rs2.model.task.CycleEventHandler;
import com.rs2.model.task.TickTask;
import com.rs2.util.GameUtil;
import com.rs2.util.path.PathFinder;

public final class SecondObjectActionTask
extends TickTask {
    private final Player player;
    private final int actionSequence;
    private final int objectId;
    private final int objectX;
    private final int objectY;
    private final int objectPlane;

    public SecondObjectActionTask(int value7, boolean enabled2, Player player, int actionSequence, int objectId, int objectX, int objectY, int objectPlane) {
        super(1, true);
        this.player = player;
        this.actionSequence = actionSequence;
        this.objectId = objectId;
        this.objectX = objectX;
        this.objectY = objectY;
        this.objectPlane = objectPlane;
    }

    @Override
    public final void execute() {
        if (this.player == null || !this.player.isCurrentActionSequence(this.actionSequence)) {
            this.stop();
            return;
        }
        if (this.player.isMoving()
                || this.player.hasMovedWithinTicks(1)
                || this.player.isStunned()) {
            return;
        }
        WorldObject worldObject = SkillActionHelper.findWorldObjectById(this.objectId, this.objectX, this.objectY, this.objectPlane);
        if (worldObject == null) {
            return;
        }
        Object interactionTargetId = ObjectDefinition.forId(this.player.getInteractionTargetId());
        Position castleWarsMainDoorApproach =
                CastleWarsEngineeringManager.getMainDoorInteractionApproach(
                        this.player, this.objectId, this.objectX, this.objectY);
        if (castleWarsMainDoorApproach != null) {
            if (this.player.getPosition().getX() == castleWarsMainDoorApproach.getX()
                    && this.player.getPosition().getY() == castleWarsMainDoorApproach.getY()) {
                this.player.getMovementQueue().clear();
                this.player.getMovementQueue().clearMovementActions();
                if (CastleWarsManager.handleSecondObjectAction(
                        this.player, this.objectId, this.objectX, this.objectY)) {
                    this.stop();
                    return;
                }
            }

            PathFinder.getInstance();
            boolean foundPath = PathFinder.findPath(this.player,
                    castleWarsMainDoorApproach.getX(), castleWarsMainDoorApproach.getY(),
                    false, 0, 0);
            if (!foundPath) {
                this.stop();
            }
            return;
        }

        Position position = this.player.getPosition();
        if (!InteractionDispatcher.canReachObjectInteraction(this.player, worldObject)) {
            this.stop();
            return;
        }
        position = new Position(this.player.getInteractionTargetX(), this.player.getInteractionTargetY(), this.objectPlane);
        if (interactionTargetId != null) {
            this.player.getUpdateState().setFacePosition(position.centerForSize(((ObjectDefinition)interactionTargetId).getMaxDimension()));
        }
        // "Use-quickly" is the second action on bank booths. Recognize the
        // cache definition instead of relying on an incomplete list of IDs.
        if (interactionTargetId != null
                && "bank booth".equalsIgnoreCase(((ObjectDefinition)interactionTargetId).getName())) {
            BankManager.openBank(this.player);
            this.stop();
            return;
        }
        if (this.player.getQuestManager().handleSecondObjectAction(this.objectId, this.objectX, this.objectY)) {
            this.stop();
            return;
        }
        if (CastleWarsManager.handleSecondObjectAction(this.player, this.objectId, this.objectX, this.objectY)) {
            this.stop();
            return;
        }
        if (DialogueManager.startContextDialogue(2, this.player, this.player.getInteractionTargetId(), this.objectX, this.objectY)) {
            this.stop();
            return;
        }
        if (ThievingObjectHandler.handleThievingObject(this.player, this.objectId, this.objectX, this.objectY)) {
            this.stop();
            return;
        }
        if (ServerSettings.content2007Enabled) {
            if (GodWarsDungeonManager.handleSecondObjectAction(this.player, this.objectId)) {
                this.stop();
                return;
            }
        }
        if (StallThievingHandler.handleStallThieving(this.player, this.objectId, this.objectX, this.objectY)) {
            this.stop();
            return;
        }
        int value = this.objectY;
        int value2 = this.objectX;
        interactionTargetId = this.player;
        if (((Player)interactionTargetId).getAllotmentPatchManager().inspectPatch(value2, value) ? true : (((Player)interactionTargetId).getFlowerPatchManager().inspectPatch(value2, value) ? true : (((Player)interactionTargetId).getHerbPatchManager().inspectPatch(value2, value) ? true : (((Player)interactionTargetId).getHopsPatchManager().inspectPatch(value2, value) ? true : (((Player)interactionTargetId).getBushPatchManager().inspectPatch(value2, value) ? true : (((Player)interactionTargetId).getTreePatchManager().inspectPatch(value2, value) ? true : (((Player)interactionTargetId).getFruitTreePatchManager().inspectPatch(value2, value) ? true : (((Player)interactionTargetId).getSpecialTreePatchManager().inspectPatch(value2, value) ? true : ((Player)interactionTargetId).getSpecialCropPatchManager().inspectPatch(value2, value))))))))) {
            this.stop();
            return;
        }
        if (this.player.getMiningManager().prospectRock(this.objectId)) {
            this.stop();
            return;
        }
        if (PickableObjectHandler.handlePickableObject(this.player, this.objectId, this.objectX, this.objectY)) {
            this.stop();
            return;
        }
        if (this.objectId == 2418 && this.objectX == 2729 && this.objectY == 3470) {
            PartyRoomManager.openPartyChest(this.player);
            this.stop();
            return;
        }
        switch (this.player.getInteractionTargetId()) {
            case 2634: {
                int value3 = this.objectY;
                value = this.objectX;
                value2 = this.objectId;
                interactionTargetId = this.player;
                GatheringToolDefinition gatheringToolDefinition = ItemCombinationHandler.findUsableGatheringTool((Player)interactionTargetId, 14);
                if (gatheringToolDefinition == null) {
                    Object value4 = interactionTargetId;
                    ((Player)value4).packetSender.sendGameMessage("You do not have a pickaxe that you can use.");
                    break;
                }
                if (!SkillActionHelper.checkSkillRequirement((Player)interactionTargetId, 14, 50, "mine here")) break;
                int value5 = ((Entity)interactionTargetId).nextActionSequence();
                ((Player)interactionTargetId).resetAnimation();
                ((Player)interactionTargetId).temporaryActionValue = 0;
                ((Entity)interactionTargetId).getUpdateState().setAnimation(gatheringToolDefinition.getGatherAnimationId());
                Object value6 = interactionTargetId;
                ((Player)value6).packetSender.sendSoundEffect(432, 1, 0);
                ((Entity)interactionTargetId).setActiveCycleEvent(new MiningShortcutTask((Player)interactionTargetId, value5, value, value3, value2, gatheringToolDefinition));
                CycleEventHandler.getInstance().schedule((Entity)interactionTargetId, ((Entity)interactionTargetId).getActiveCycleEvent(), 3);
                break;
            }
            case 2114: {
                interactionTargetId = this.player;
                if (((Player)interactionTargetId).getCoalTruckCoalCount() == 0) {
                    Object value7 = interactionTargetId;
                    ((Player)value7).packetSender.sendGameMessage("There is no coal left in the truck.");
                    break;
                }
                Object value8 = interactionTargetId;
                ((Player)value8).packetSender.sendGameMessage("The truck contains " + ((Player)interactionTargetId).getCoalTruckCoalCount() + " pieces of coal.");
                break;
            }
            case 3194: {
                BankManager.openBank(this.player);
                break;
            }
            case 8930: {
                AttackStyleDefinition.startDelayedObjectMove(this.player, new Position(2545, 10143, 0));
                break;
            }
            case 10177: {
                AttackStyleDefinition.startDelayedObjectMove(this.player, new Position(2544, 3741, 0));
                break;
            }
            case 3433: {
                AttackStyleDefinition.toggleObjectAfterAnimation(this.player, this.objectId, 3432, worldObject);
                break;
            }
            case 1570: {
                AttackStyleDefinition.toggleObjectAfterAnimation(this.player, this.objectId, 1568, worldObject);
                break;
            }
            case 1739: 
            case 4569: 
            case 12537: {
                AttackStyleDefinition.climbOffsetLadder(this.player, "up");
                break;
            }
            case 1748: 
            case 2884: 
            case 8745: 
            case 12965: {
                AttackStyleDefinition.climbOneFloorAtCurrentTile(this.player, "up");
                break;
            }
            case 2781: 
            case 3044: 
            case 9390: 
            case 11666: 
            case 14921: {
                SmeltingHandler.openSmeltingInterface(this.player);
                break;
            }
            case 2644: {
                GameplayHelper.openProductionInterface(this.player, "spinning");
                if (!this.player.botEnabled) break;
                CraftingHandler.startBotSpinningTask(this.player);
                break;
            }
            case 2213: 
            case 5276: 
            case 6084: 
            case 10517: 
            case 11338: 
            case 11758: 
            case 12121: 
            case 14367: {
                BankManager.openBank(this.player);
                break;
            }
            case 8717: {
                GameplayHelper.openProductionInterface(this.player, "weaving");
                break;
            }
            default: {
                Player player = this.player;
                player.packetSender.sendGameMessage("Nothing interesting happens.");
            }
        }
        this.stop();
    }
}
