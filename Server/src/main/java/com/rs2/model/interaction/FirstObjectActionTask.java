package com.rs2.model.interaction;

import com.rs2.CacheCoordinateTranslator;
import com.rs2.ServerSettings;
import com.rs2.cache.CacheArchiveEntry;
import com.rs2.cache.CacheFile;
import com.rs2.model.Entity;
import com.rs2.model.EntityTargetMovement;
import com.rs2.model.GameplayHelper;
import com.rs2.model.Position;
import com.rs2.model.World;
import com.rs2.model.combat.AttackStyleDefinition;
import com.rs2.model.dialogue.DialogueManager;
import com.rs2.model.gameplay.abyss.AbyssManager;
import com.rs2.model.gameplay.barrows.BarrowsManager;
import com.rs2.model.gameplay.castlewars.CastleWarsEngineeringManager;
import com.rs2.model.gameplay.castlewars.CastleWarsManager;
import com.rs2.model.gameplay.duel.DuelHistory;
import com.rs2.model.gameplay.partyroom.PartyRoomManager;
import com.rs2.model.ground.GroundItemManager;
import com.rs2.model.interaction.InteractionDispatcher;
import com.rs2.model.interaction.SarcophagusSneakTask;
import com.rs2.model.interaction.SearchBookcaseTask;
import com.rs2.model.interaction.SearchCrateTask;
import com.rs2.model.interaction.SearchHayTask;
import com.rs2.model.item.ItemStack;
import com.rs2.model.objects.DynamicObject;
import com.rs2.model.objects.LoadedWorldObject;
import com.rs2.model.objects.ObjectDefinition;
import com.rs2.model.objects.ObjectManager;
import com.rs2.model.objects.WorldObject;
import com.rs2.model.objects.WorldObjectLookup;
import com.rs2.model.objects.functions.DoorHandler;
import com.rs2.model.objects.functions.DoubleDoorHandler;
import com.rs2.model.objects.functions.FlourMillHandler;
import com.rs2.model.objects.functions.ObeliskTick;
import com.rs2.model.player.BankManager;
import com.rs2.model.player.Player;
import com.rs2.model.quest.QuestDefinition;
import com.rs2.model.skill.SkillActionHelper;
import com.rs2.model.skill.agility.AgilityObstacleHandler;
import com.rs2.model.skill.magic.Spellbook;
import com.rs2.model.skill.mining.MiningManager;
import com.rs2.model.skill.prayer.PrayerManager;
import com.rs2.model.skill.runecrafting.RunecraftingObjectHandler;
import com.rs2.model.skill.woodcutting.JungleCutting;
import com.rs2.model.skill.woodcutting.TreeDefinition;
import com.rs2.model.skill.woodcutting.WoodcuttingHandler;
import com.rs2.model.task.CycleEventHandler;
import com.rs2.model.task.TickTask;
import com.rs2.model.travel.canoe.CanoeTravelManager;
import com.rs2.model.travel.canoe.CanoeTreeDefinition;
import com.rs2.util.GameUtil;
import com.rs2.util.GameplayTrace;
import com.rs2.util.path.PathFinder;

public final class FirstObjectActionTask
extends TickTask {
    private final Player player;
    private final int actionSequence;
    private final int objectId;
    private final int objectX;
    private final int objectY;
    private final int objectPlane;
    private final String objectName;
    private boolean loggedWaitingForMovement;
    private boolean loggedMissingWorldObject;
    private boolean loggedMissingReachPosition;
    private boolean loggedOutsideDistance;

    public FirstObjectActionTask(int value7, boolean enabled2, Player player, int actionSequence, int objectId, int objectX, int objectY, int objectPlane, String objectName) {
        super(1, true);
        this.player = player;
        this.actionSequence = actionSequence;
        this.objectId = objectId;
        this.objectX = objectX;
        this.objectY = objectY;
        this.objectPlane = objectPlane;
        this.objectName = objectName;
    }

    @Override
    public final void execute() {
        if (this.player == null || !this.player.isCurrentActionSequence(this.actionSequence)) {
            if (GameplayTrace.enabled()) {
                GameplayTrace.log("first-object stop invalid-sequence seq=" + this.actionSequence + " player=" + GameplayTrace.describe(this.player) + " objectId=" + this.objectId + " x=" + this.objectX + " y=" + this.objectY + " plane=" + this.objectPlane);
            }
            this.stop();
            return;
        }
        if (this.player.isMoving()
                || this.player.hasMovedWithinTicks(1)
                || this.player.isStunned()) {
            if (GameplayTrace.enabled() && !this.loggedWaitingForMovement) {
                this.loggedWaitingForMovement = true;
                GameplayTrace.log("first-object wait moving-or-stunned seq=" + this.actionSequence + " moving=" + this.player.isMoving() + " stunned=" + this.player.isStunned() + " player=" + GameplayTrace.describe(this.player) + " objectId=" + this.objectId + " x=" + this.objectX + " y=" + this.objectY + " plane=" + this.objectPlane);
            }
            return;
        }
        if (this.objectId == CastleWarsManager.STEPPING_STONE_ID
                && this.objectPlane == 0
                && GameUtil.isWithinDistance(this.player.getPosition().getX(),
                        this.player.getPosition().getY(), this.objectX, this.objectY, 1)
                && CastleWarsManager.handleFirstObjectAction(
                        this.player, this.objectId, this.objectX, this.objectY)) {
            this.stop();
            return;
        }
        Position castleWarsSideDoorApproach =
                CastleWarsEngineeringManager.getSideDoorInteractionApproach(
                        this.player, this.objectId, this.objectX, this.objectY);
        if (castleWarsSideDoorApproach != null) {
            if (this.player.getPosition().getX() == castleWarsSideDoorApproach.getX()
                    && this.player.getPosition().getY() == castleWarsSideDoorApproach.getY()
                    && CastleWarsManager.handleFirstObjectAction(
                            this.player, this.objectId, this.objectX, this.objectY)) {
                this.stop();
                return;
            }

            PathFinder.getInstance();
            boolean foundPath = PathFinder.findPath(this.player,
                    castleWarsSideDoorApproach.getX(), castleWarsSideDoorApproach.getY(),
                    false, 0, 0);
            if (!foundPath) {
                this.stop();
            }
            return;
        }

        Position castleWarsMainDoorApproach =
                CastleWarsEngineeringManager.getMainDoorInteractionApproach(
                        this.player, this.objectId, this.objectX, this.objectY);
        if (castleWarsMainDoorApproach != null) {
            if (CastleWarsEngineeringManager.isAtMainDoorInteractionApproach(
                    this.player, this.objectId, this.objectX, this.objectY)
                    && CastleWarsManager.handleFirstObjectAction(
                            this.player, this.objectId, this.objectX, this.objectY)) {
                this.stop();
                return;
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

        Position questApproach = com.rs2.model.quest.impl.PlagueCityQuest.stairApproach(
                this.player, this.objectId, this.objectX, this.objectY);
        if (questApproach != null) {
            if (!PathFinder.findPath(this.player, questApproach.getX(), questApproach.getY(), false, 0, 0)) {
                this.stop();
            }
            return;
        }

        Position castleWarsStairApproach = CastleWarsManager.getStairTraversalApproach(
                this.player, this.objectId, this.objectX, this.objectY);
        if (castleWarsStairApproach != null) {
            if (CastleWarsManager.isAtStairTraversalApproach(
                    this.player, this.objectId, this.objectX, this.objectY)
                    && CastleWarsManager.handleFirstObjectAction(
                            this.player, this.objectId, this.objectX, this.objectY)) {
                this.stop();
                return;
            }

            PathFinder.getInstance();
            boolean foundPath = PathFinder.findPath(this.player,
                    castleWarsStairApproach.getX(), castleWarsStairApproach.getY(),
                    false, 0, 0);
            if (!foundPath) {
                if (GameplayTrace.enabled()) {
                    GameplayTrace.log("castle-wars stair interaction unreachable player="
                            + GameplayTrace.describe(this.player)
                            + " objectId=" + this.objectId
                            + " object=" + this.objectX + "," + this.objectY + "," + this.objectPlane
                            + " approach=" + GameplayTrace.position(castleWarsStairApproach));
                }
                this.stop();
            }
            return;
        }
        WorldObject worldObject = SkillActionHelper.findWorldObjectById(this.objectId, this.objectX, this.objectY, this.objectPlane);
        if (worldObject == null) {
            if (GameplayTrace.enabled() && !this.loggedMissingWorldObject) {
                this.loggedMissingWorldObject = true;
                GameplayTrace.log("first-object wait missing-world-object seq=" + this.actionSequence + " player=" + GameplayTrace.describe(this.player) + " objectId=" + this.objectId + " x=" + this.objectX + " y=" + this.objectY + " plane=" + this.objectPlane);
            }
            return;
        }
        Object interactionTargetId = ObjectDefinition.forId(this.player.getInteractionTargetId());
        Position position = this.player.getPosition();
        if (!InteractionDispatcher.canReachObjectInteraction(this.player, worldObject)) {
            if (GameplayTrace.enabled()) {
                GameplayTrace.log("first-object stop reach-check-failed seq=" + this.actionSequence + " player=" + GameplayTrace.describe(this.player) + " objectId=" + this.objectId + " targetPos=" + GameplayTrace.position(position) + " object=" + this.objectX + "," + this.objectY + "," + this.objectPlane);
            }
            this.stop();
            return;
        }
        if (GameplayTrace.enabled()) {
            GameplayTrace.log("first-object reachable seq=" + this.actionSequence + " player=" + GameplayTrace.describe(this.player) + " objectId=" + this.objectId + " targetPos=" + GameplayTrace.position(position) + " object=" + this.objectX + "," + this.objectY + "," + this.objectPlane);
        }
        if (this.objectId == 4031) {
            if (this.player.getPosition().getY() > this.objectY) {
                if (!this.player.isMember()) {
                    this.player.packetSender.sendGameMessage("You need a members account to access members content.");
                    this.stop();
                    return;
                }
                if (ServerSettings.freeToPlayWorld) {
                    this.player.packetSender.sendGameMessage("You need to be in members world to access members content.");
                    this.stop();
                    return;
                }
                if (this.player.getInventoryManager().containsItem(1854)) {
                    this.player.getPacketSender().sendGameMessage("You go through the gate.");
                    this.player.getInventoryManager().removeItem(new ItemStack(1854, 1));
                    this.player.getPacketSender().queueRelativeMovementStep(0, -2, true);
                } else {
                    this.player.getPacketSender().sendGameMessage("You need a shantay pass to go through.");
                }
            } else {
                this.player.getPacketSender().queueRelativeMovementStep(0, 2, true);
            }
            this.stop();
            return;
        }
        position = new Position(this.player.getInteractionTargetX(), this.player.getInteractionTargetY(), this.objectPlane);
        if (interactionTargetId != null) {
            this.player.getUpdateState().setFacePosition(position.centerForSize(((ObjectDefinition)interactionTargetId).getMaxDimension()));
        }
        if (BarrowsManager.handleFirstObjectAction(this.player, this.objectId)) {
            this.stop();
            return;
        }
        if (!GameUtil.isWithinDistance(this.player.getPosition().getX(), this.player.getPosition().getY(), this.objectX, this.objectY, 7)) {
            if (GameplayTrace.enabled() && !this.loggedOutsideDistance) {
                this.loggedOutsideDistance = true;
                GameplayTrace.log("first-object wait outside-distance seq=" + this.actionSequence + " player=" + GameplayTrace.describe(this.player) + " objectId=" + this.objectId + " object=" + this.objectX + "," + this.objectY + "," + this.objectPlane);
            }
            return;
        }
        if (GameplayTrace.enabled()) {
            GameplayTrace.log("first-object milestone after-distance seq=" + this.actionSequence + " player=" + GameplayTrace.describe(this.player) + " objectId=" + this.objectId);
        }
        if (this.objectId == 12045 || this.objectId == 12047) {
            if (this.objectX == 2469 && this.player.getPosition().getX() < 2470 || this.objectY == 4434 && this.player.getPosition().getY() > 4433) {
                DialogueManager.startContextDialogue(1, this.player, this.objectId, this.objectX, this.objectY);
            } else if (this.objectX == 2469) {
                this.player.getPacketSender().queueRelativeMovementStep(-1, 0, true);
                this.player.getPacketSender().openDoubleDoorPair(12045, 2469, 4438, 12047, 2469, 4437);
            } else {
                this.player.getPacketSender().queueRelativeMovementStep(0, 1, true);
                this.player.getPacketSender().openDoubleDoorPair(12045, 2466, 4434, 12047, 2465, 4434);
            }
            this.stop();
            return;
        }
        if (this.objectId == 2411) {
            int index = 0;
            int index2 = 0;
            if (CacheCoordinateTranslator.dungeonCoordinateShiftActive) {
                index = 768;
                index2 = 5120;
            }
            if (this.objectX == index + 2470 && this.player.getPosition().getX() < index + 2470 || this.objectY == index2 + 4434 && this.player.getPosition().getY() > index2 + 4433) {
                DialogueManager.startContextDialogue(1, this.player, this.objectId, this.objectX, this.objectY);
            } else if (this.objectX == index + 2470) {
                this.player.getPacketSender().queueRelativeMovementStep(-1, 0, true);
                this.player.getPacketSender().openSingleDoor(2411, index + 2470, index2 + 4438, 0);
            } else {
                this.player.getPacketSender().queueRelativeMovementStep(0, 1, true);
                this.player.getPacketSender().openSingleDoor(2411, index + 2465, index2 + 4434, 0);
            }
            this.stop();
            return;
        }
        if (this.player.getQuestManager().handleFirstObjectAction(this.objectId, this.objectX, this.objectY)) {
            this.stop();
            return;
        }
        if (BarrowsManager.handleTunnelDoorObject(this.player, this.objectId, this.objectX, this.objectY)) {
            this.stop();
            return;
        }
        if (CastleWarsManager.handleFirstObjectAction(this.player, this.objectId, this.objectX, this.objectY)) {
            this.stop();
            return;
        }
        if (DialogueManager.startContextDialogue(1, this.player, this.player.getInteractionTargetId(), this.objectX, this.objectY)) {
            this.stop();
            return;
        }
        if (GameplayTrace.enabled()) {
            GameplayTrace.log("first-object milestone after-dialogue seq=" + this.actionSequence + " player=" + GameplayTrace.describe(this.player) + " objectId=" + this.objectId);
        }
        if (this.player.getSlayerManager().handleMogreLure(this.objectId, this.objectX, this.objectY)) {
            this.stop();
            return;
        }
        if (ObeliskTick.activateForPlayer(this.player, this.objectId)) {
            this.stop();
            return;
        }
        if (GameplayHelper.handleAgilityObjectAction(this.player, this.objectId, this.objectX, this.objectY)) {
            this.stop();
            return;
        }
        if (PartyRoomManager.handleBalloonObjectAction(this.player, this.objectId, this.objectX, this.objectY)) {
            this.stop();
            return;
        }
        if (AbyssManager.handleAbyssObjectAction(this.player, this.objectId, this.objectX, this.objectY)) {
            this.stop();
            return;
        }
        if (RunecraftingObjectHandler.handleAltarOrAbyssObject(this.player, this.objectId) || RunecraftingObjectHandler.handleRuinsOrPortalObject(this.player, this.objectId)) {
            this.stop();
            return;
        }
        if ((((ObjectDefinition)interactionTargetId).getName().toLowerCase().contains("altar") || this.objectId == 4859 || this.objectId >= 10638 && this.objectId <= 10640) && this.objectId != 2640 && this.objectId != 6552) {
            PrayerManager.rechargePrayerAtAltar(this.player);
            this.stop();
            return;
        }
        if (this.player.getAlchemistPlaygroundController().handleObjectAction(this.objectId, this.objectX, this.objectY)) {
            this.stop();
            return;
        }
        if (this.player.getEnchantmentChamberController().handleObjectAction(this.objectId)) {
            this.stop();
            return;
        }
        if (this.player.getCreatureGraveyardController().handleObjectAction(this.objectId, this.objectX, this.objectY, this.objectPlane)) {
            this.stop();
            return;
        }
        if (this.player.getTelekineticTheatreController().handleObjectAction(this.objectId)) {
            this.stop();
            return;
        }
        if (this.objectId == 2332 && this.objectX == 2907 && this.objectY == 3049) {
            if (this.player.getSkillManager().getCurrentLevels()[16] < 25) {
                this.player.getDialogueManager().showOneLineStatement("You need a agility level of 25 to do that.");
            } else {
                AgilityObstacleHandler.startAgilityMovement(this.player, 0.0, 4, 0, -1, 762, -1, 3, "You walk carefully across the slippery log...", "...You make it safely to the other side.");
            }
            this.stop();
            return;
        }
        if (this.objectId == 2332 && this.objectX == 2909 && this.objectY == 3049) {
            if (this.player.getSkillManager().getCurrentLevels()[16] < 25) {
                this.player.getDialogueManager().showOneLineStatement("You need a agility level of 25 to do that.");
            } else {
                AgilityObstacleHandler.startAgilityMovement(this.player, 0.0, -4, 0, -1, 762, -1, 3, "You walk carefully across the slippery log...", "...You make it safely to the other side.");
            }
            this.stop();
            return;
        }
        if (this.objectId == 10093) {
            if (!this.player.isMember()) {
                this.player.packetSender.sendGameMessage("You need a members account to access members content.");
                this.stop();
                return;
            }
            if (ServerSettings.freeToPlayWorld) {
                this.player.packetSender.sendGameMessage("You need to be in members world to access members content.");
                this.stop();
                return;
            }
            GameplayHelper.openProductionInterface(this.player, "dairyChurn");
            this.stop();
            return;
        }
        if (this.objectId == 2417 && this.objectX == 2729 && this.objectY == 3470) {
            this.player.getUpdateState().setAnimation(832);
            ObjectManager.getInstance().removeDynamicObjectAt(2729, 3470, 0, 0);
            ObjectManager.getInstance().addDynamicObject(new DynamicObject(2418, 2729, 3470, 0, 1, 10, 2417, 999999999), true);
            this.stop();
            return;
        }
        if (this.objectId == 2081 || this.objectId == 2083 || this.objectId == 2085 || this.objectId == 2087 || this.objectId == 2412 || this.objectId == 2414) {
            int index3 = 0;
            int index4 = 0;
            if (this.player.getPosition().getX() < this.objectX) {
                index3 = 3;
            }
            if (this.player.getPosition().getY() > this.objectY) {
                index4 = -3;
            }
            this.player.moveTo(new Position(this.player.getPosition().getX() + index3, this.player.getPosition().getY() + index4, this.player.getPosition().getPlane() + 1));
            this.stop();
        }
        if (this.objectId == 2082 || this.objectId == 2084 || this.objectId == 2086 || this.objectId == 2088 || this.objectId == 2413 || this.objectId == 2415) {
            int index5 = 0;
            int index6 = 0;
            if (this.player.getPosition().getX() > this.objectX) {
                index5 = -3;
            }
            if (this.player.getPosition().getY() < this.objectY) {
                index6 = 3;
            }
            this.player.moveTo(new Position(this.player.getPosition().getX() + index5, this.player.getPosition().getY() + index6, this.player.getPosition().getPlane() - 1));
            this.stop();
        }
        if (this.objectId == 190) {
            interactionTargetId = WorldObjectLookup.findObjectByIdAt(this.objectId, this.objectX, this.objectY, this.objectPlane);
            if (interactionTargetId != null) {
                ObjectManager.getInstance().addDynamicObject(new DynamicObject(191, this.objectX, this.objectY, this.objectPlane, ((LoadedWorldObject)interactionTargetId).getOrientation(), ((LoadedWorldObject)interactionTargetId).getType(), this.objectId, 4), true);
                ObjectManager.getInstance().addDynamicObject(new DynamicObject(192, this.objectX + 3, this.objectY, this.objectPlane, ((LoadedWorldObject)interactionTargetId).getOrientation() - 2, ((LoadedWorldObject)interactionTargetId).getType(), ServerSettings.placeholderObjectId, 4), true);
            }
            this.player.getPacketSender().sendSoundEffect(318, 1, 0);
            this.player.getPacketSender().queueYAxisMovementStep(0, this.player.getPosition().getY() < 3385 ? 3 : -3, 2, true);
            this.stop();
            return;
        }
        if (this.objectId == 4577 && this.objectX == 2509 && this.objectY == 3636) {
            this.player.getPacketSender().queueRelativeMovementStep(0, this.player.getPosition().getY() < 3636 ? 1 : -1, true);
            this.player.getPacketSender().sendSoundEffect(318, 1, 0);
            this.stop();
            return;
        }
        if (this.objectId == 2624 && this.objectX == 2902 && this.objectY == 3510 || this.objectId == 2625 && this.objectX == 2902 && this.objectY == 3511) {
            if (this.player.getQuestState(50) == 1) {
                this.player.getPacketSender().queueRelativeMovementStep(this.player.getPosition().getX() < 2902 ? 1 : -1, 0, true);
                this.player.getPacketSender().openDoubleDoorPair(2624, 2625, 2902, 3510, 2902, 3511, 0);
            } else {
                interactionTargetId = QuestDefinition.forId(50);
                String name = ((QuestDefinition)interactionTargetId).getName();
                this.player.getDialogueManager().showOneLineStatement("You need to complete " + name + " to enter the guild.");
            }
            this.stop();
            return;
        }
        if (this.objectId == 2391 && this.objectX == 2728 && this.objectY == 3349 || this.objectId == 2392 && this.objectX == 2729 && this.objectY == 3349) {
            if (this.player.getQuestState(57) > 0) {
                this.player.getPacketSender().queueRelativeMovementStep(0, this.player.getPosition().getY() < 3350 ? 1 : -1, true);
                this.player.getPacketSender().openDoubleDoorPair(2391, 2392, 2728, 3349, 2729, 3349, 0);
            } else {
                interactionTargetId = QuestDefinition.forId(57);
                String name2 = ((QuestDefinition)interactionTargetId).getName();
                this.player.getDialogueManager().showOneLineStatement("You need to start " + name2 + " to go through.");
            }
            this.stop();
            return;
        }
        if (this.objectId == 2514) {
            if (SkillActionHelper.checkSkillRequirement(this.player, 4, 40, "enter the Ranging Guild")) {
                boolean enabled = false;
                if (this.player.getPosition().getX() <= 2658 && this.player.getPosition().getY() >= 3438) {
                    enabled = true;
                }
                this.player.getPacketSender().queueRelativeMovementStep(enabled ? 1 : -1, enabled ? -1 : 1, true);
                this.player.getPacketSender().openSouthShiftedSingleDoor(this.objectId, this.objectX, this.objectY, this.objectPlane);
            }
            this.stop();
            return;
        }
        if (this.objectId == 2025) {
            if (SkillActionHelper.checkSkillRequirement(this.player, 10, 68, "enter the Fishing Guild")) {
                this.player.getPacketSender().queueRelativeMovementStep(0, this.player.getPosition().getY() > 3393 ? -1 : 1, true);
                this.player.getPacketSender().openSingleDoor(this.objectId, this.objectX, this.objectY, this.objectPlane);
            }
            this.stop();
            return;
        }
        if (this.objectId == 1600 && this.objectX == 2597 && this.objectY == 3087 || this.objectId == 1601 && this.objectX == 2597 && this.objectY == 3088) {
            if (SkillActionHelper.checkSkillRequirement(this.player, 6, 66, "enter the Magic Guild")) {
                this.player.getPacketSender().queueRelativeMovementStep(this.player.getPosition().getX() < 2597 ? 1 : -1, 0, true);
                this.player.getPacketSender().openDoubleDoorPair(1600, 1601, 2597, 3087, 2597, 3088, 0);
            }
            this.stop();
            return;
        }
        if (this.objectId == 1601 && this.objectX == 2584 && this.objectY == 3087 || this.objectId == 1600 && this.objectX == 2584 && this.objectY == 3088) {
            if (SkillActionHelper.checkSkillRequirement(this.player, 6, 66, "enter the Magic Guild")) {
                this.player.getPacketSender().queueRelativeMovementStep(this.player.getPosition().getX() < 2585 ? 1 : -1, 0, true);
                this.player.getPacketSender().openDoubleDoorPair(1600, 1601, 2584, 3088, 2584, 3087, 0);
            }
            this.stop();
            return;
        }
        if (this.objectId == 2712) {
            if (SkillActionHelper.checkSkillRequirement(this.player, 7, 32, "enter the Cooks' Guild")) {
                if (this.player.getEquipmentManager().getContainer().getItemAt(0) == null) {
                    if (this.player.getPosition().getY() > 3443) {
                        this.player.getPacketSender().openSingleDoor(this.objectId, this.objectX, this.objectY, this.objectPlane);
                        this.player.getPacketSender().queueRelativeMovementStep(0, -1, true);
                    } else {
                        this.player.getDialogueManager().showTwoLineStatement("You need to wear chef's hat to", "enter the guild.");
                        this.player.setInteractionTargetId(0);
                    }
                } else if (this.player.getPosition().getY() > 3443) {
                    this.player.getPacketSender().openSingleDoor(this.objectId, this.objectX, this.objectY, this.objectPlane);
                    this.player.getPacketSender().queueRelativeMovementStep(0, -1, true);
                } else if (this.player.getEquipmentManager().getContainer().getItemAt(0).getId() == 1949) {
                    this.player.getPacketSender().openSingleDoor(this.objectId, this.objectX, this.objectY, this.objectPlane);
                    this.player.getPacketSender().queueRelativeMovementStep(0, 1, true);
                } else {
                    this.player.getDialogueManager().showTwoLineStatement("You need to wear chef's hat to", "enter the guild.");
                    this.player.setInteractionTargetId(0);
                }
            }
            this.stop();
            return;
        }
        if (this.objectId == 1596 && this.objectX == 2935 && this.objectY == 3451 || this.objectId == 1597 && this.objectX == 2935 && this.objectY == 3450) {
            if (this.player.isMember()) {
                if (ServerSettings.freeToPlayWorld) {
                    this.player.packetSender.sendGameMessage("You need to be in members world to access members content.");
                } else {
                    int index7 = 0;
                    if (this.player.getPosition().getY() > 3451) {
                        index7 = 3451 - this.player.getPosition().getY();
                    }
                    if (this.player.getPosition().getY() < 3450) {
                        index7 = 3450 - this.player.getPosition().getY();
                    }
                    this.player.getPacketSender().queueRelativeMovementStep(this.player.getPosition().getX() < 2936 ? 1 : -1, index7, true);
                    this.player.getPacketSender().openDoubleDoorPair(1596, 2935, 3451, 1597, 2935, 3450);
                    this.player.getPacketSender().sendSoundEffect(318, 1, 0);
                }
            } else {
                this.player.packetSender.sendGameMessage("You need a members account to access members content.");
            }
            this.stop();
            return;
        }
        if (this.objectId == 1596 && this.objectX == 2934 && this.objectY == 3320 || this.objectId == 1597 && this.objectX == 2933 && this.objectY == 3320) {
            if (this.player.isMember()) {
                if (ServerSettings.freeToPlayWorld) {
                    this.player.packetSender.sendGameMessage("You need to be in members world to access members content.");
                } else {
                    this.player.getPacketSender().queueRelativeMovementStep(0, this.player.getPosition().getY() < 3320 ? 1 : -1, true);
                    this.player.getPacketSender().openDoubleDoorPair(1596, 2934, 3320, 1597, 2933, 3320);
                    this.player.getPacketSender().sendSoundEffect(318, 1, 0);
                }
            } else {
                this.player.packetSender.sendGameMessage("You need a members account to access members content.");
            }
            this.stop();
            return;
        }
        if (this.objectId == 1596 && this.objectX == 2816 && this.objectY == 3182 || this.objectId == 1597 && this.objectX == 2816 && this.objectY == 3183) {
            if (this.player.isMember()) {
                if (ServerSettings.freeToPlayWorld) {
                    this.player.packetSender.sendGameMessage("You need to be in members world to access members content.");
                } else {
                    this.player.getPacketSender().queueRelativeMovementStep(this.player.getPosition().getX() < 2816 ? 1 : -1, 0, true);
                    this.player.getPacketSender().openDoubleDoorPair(1596, 2816, 3182, 1597, 2816, 3183);
                    this.player.getPacketSender().sendSoundEffect(318, 1, 0);
                }
            } else {
                this.player.packetSender.sendGameMessage("You need a members account to access members content.");
            }
            this.stop();
            return;
        }
        if (this.objectId == 1596 && this.objectX == 3131 && this.objectY == 9917 || this.objectId == 1597 && this.objectX == 3132 && this.objectY == 9917) {
            if (this.player.isMember()) {
                if (ServerSettings.freeToPlayWorld) {
                    this.player.packetSender.sendGameMessage("You need to be in members world to access members content.");
                } else {
                    this.player.getPacketSender().queueRelativeMovementStep(0, this.player.getPosition().getY() < 9918 ? 1 : -1, true);
                    this.player.getPacketSender().openDoubleDoorPair(1596, 3131, 9917, 1597, 3132, 9917);
                    this.player.getPacketSender().sendSoundEffect(318, 1, 0);
                }
            } else {
                this.player.packetSender.sendGameMessage("You need a members account to access members content.");
            }
            this.stop();
            return;
        }
        if (this.objectId == 1596 && this.objectX == 3312 && this.objectY == 3331 || this.objectId == 1597 && this.objectX == 3312 && this.objectY == 3332) {
            if (this.player.isMember()) {
                if (ServerSettings.freeToPlayWorld) {
                    this.player.packetSender.sendGameMessage("You need to be in members world to access members content.");
                } else {
                    this.player.getPacketSender().queueRelativeMovementStep(this.player.getPosition().getX() < 3312 ? 1 : -1, 0, true);
                    this.player.getPacketSender().openDoubleDoorPair(1596, 3312, 3331, 1597, 3312, 3332);
                    this.player.getPacketSender().sendSoundEffect(318, 1, 0);
                }
            } else {
                this.player.packetSender.sendGameMessage("You need a members account to access members content.");
            }
            this.stop();
            return;
        }
        if (this.objectId == 1596 && this.objectX == 3008 && this.objectY == 3849 || this.objectId == 1597 && this.objectX == 3008 && this.objectY == 3850) {
            if (this.player.isMember()) {
                if (ServerSettings.freeToPlayWorld) {
                    this.player.packetSender.sendGameMessage("You need to be in members world to access members content.");
                } else {
                    this.player.getPacketSender().queueRelativeMovementStep(this.player.getPosition().getX() < 3008 ? 1 : -1, 0, true);
                    this.player.getPacketSender().openDoubleDoorPair(1596, 3008, 3849, 1597, 3008, 3850);
                    this.player.getPacketSender().sendSoundEffect(318, 1, 0);
                }
            } else {
                this.player.packetSender.sendGameMessage("You need a members account to access members content.");
            }
            this.stop();
            return;
        }
        if (this.objectId == 1596 && this.objectX == 3071 && this.objectY == 3857 || this.objectId == 1597 && this.objectX == 3071 && this.objectY == 3856) {
            if (this.player.isMember()) {
                if (ServerSettings.freeToPlayWorld) {
                    this.player.packetSender.sendGameMessage("You need to be in members world to access members content.");
                } else {
                    this.player.getPacketSender().queueRelativeMovementStep(this.player.getPosition().getX() < 3072 ? 1 : -1, 0, true);
                    this.player.getPacketSender().openDoubleDoorPair(1596, 3071, 3857, 1597, 3071, 3856);
                    this.player.getPacketSender().sendSoundEffect(318, 1, 0);
                }
            } else {
                this.player.packetSender.sendGameMessage("You need a members account to access members content.");
            }
            this.stop();
            return;
        }
        if (this.objectId == 1596 && this.objectX == 2948 && this.objectY == 3904 || this.objectId == 1597 && this.objectX == 2947 && this.objectY == 3904) {
            if (this.player.isMember()) {
                if (ServerSettings.freeToPlayWorld) {
                    this.player.packetSender.sendGameMessage("You need to be in members world to access members content.");
                } else {
                    this.player.getPacketSender().queueRelativeMovementStep(0, this.player.getPosition().getY() < 3904 ? 1 : -1, true);
                    this.player.getPacketSender().openDoubleDoorPair(1596, 2948, 3904, 1597, 2947, 3904);
                    this.player.getPacketSender().sendSoundEffect(318, 1, 0);
                }
            } else {
                this.player.packetSender.sendGameMessage("You need a members account to access members content.");
            }
            this.stop();
            return;
        }
        if (this.objectId == 1596 && this.objectX == 3225 && this.objectY == 3904 || this.objectId == 1597 && this.objectX == 3224 && this.objectY == 3904) {
            if (this.player.isMember()) {
                if (ServerSettings.freeToPlayWorld) {
                    this.player.packetSender.sendGameMessage("You need to be in members world to access members content.");
                } else {
                    this.player.getPacketSender().queueRelativeMovementStep(0, this.player.getPosition().getY() < 3904 ? 1 : -1, true);
                    this.player.getPacketSender().openDoubleDoorPair(1596, 3225, 3904, 1597, 3224, 3904);
                    this.player.getPacketSender().sendSoundEffect(318, 1, 0);
                }
            } else {
                this.player.packetSender.sendGameMessage("You need a members account to access members content.");
            }
            this.stop();
            return;
        }
        if (this.objectId == 1596 && this.objectX == 3337 && this.objectY == 3896 || this.objectId == 1597 && this.objectX == 3336 && this.objectY == 3896) {
            if (this.player.isMember()) {
                if (ServerSettings.freeToPlayWorld) {
                    this.player.packetSender.sendGameMessage("You need to be in members world to access members content.");
                } else {
                    this.player.getPacketSender().queueRelativeMovementStep(0, this.player.getPosition().getY() < 3896 ? 1 : -1, true);
                    this.player.getPacketSender().openDoubleDoorPair(1596, 3337, 3896, 1597, 3336, 3896);
                    this.player.getPacketSender().sendSoundEffect(318, 1, 0);
                }
            } else {
                this.player.packetSender.sendGameMessage("You need a members account to access members content.");
            }
            this.stop();
            return;
        }
        if (this.objectId == 1596 && this.objectX == 3202 && this.objectY == 3856 || this.objectId == 1597 && this.objectX == 3201 && this.objectY == 3856) {
            if (this.player.isMember()) {
                if (ServerSettings.freeToPlayWorld) {
                    this.player.packetSender.sendGameMessage("You need to be in members world to access members content.");
                } else {
                    this.player.getPacketSender().queueRelativeMovementStep(0, this.player.getPosition().getY() < 3856 ? 1 : -1, true);
                    this.player.getPacketSender().openDoubleDoorPair(1596, 3202, 3856, 1597, 3201, 3856);
                    this.player.getPacketSender().sendSoundEffect(318, 1, 0);
                }
            } else {
                this.player.packetSender.sendGameMessage("You need a members account to access members content.");
            }
            this.stop();
            return;
        }
        if (this.objectId == 2050 && this.objectX == 3264 && this.objectY == 3405 || this.objectId == 2051 && this.objectX == 3264 && this.objectY == 3406) {
            if (this.player.isMember()) {
                if (ServerSettings.freeToPlayWorld) {
                    this.player.packetSender.sendGameMessage("You need to be in members world to access members content.");
                } else {
                    com.rs2.model.quest.impl.BiohazardQuest.passVarrockGate(this.player);
                }
            } else {
                this.player.packetSender.sendGameMessage("You need a members account to access members content.");
            }
            this.stop();
            return;
        }
        if (this.objectId == 3197 && this.objectX == 3312 && this.objectY == 3234 || this.objectId == 3198 && this.objectX == 3312 && this.objectY == 3235) {
            if (this.player.isMember()) {
                if (ServerSettings.freeToPlayWorld) {
                    this.player.packetSender.sendGameMessage("You need to be in members world to access members content.");
                } else {
                    this.player.getPacketSender().queueRelativeMovementStep(this.player.getPosition().getX() < 3312 ? 1 : -1, 0, true);
                    this.player.getPacketSender().openDoubleDoorPair(3197, 3312, 3234, 3198, 3312, 3235);
                    this.player.getPacketSender().sendSoundEffect(318, 1, 0);
                }
            } else {
                this.player.packetSender.sendGameMessage("You need a members account to access members content.");
            }
            this.stop();
            return;
        }
        if (this.objectId == 1598 && this.objectX == 3320 && this.objectY == 3467 || this.objectId == 1599 && this.objectX == 3320 && this.objectY == 3468) {
            if (this.player.isMember()) {
                if (ServerSettings.freeToPlayWorld) {
                    this.player.packetSender.sendGameMessage("You need to be in members world to access members content.");
                } else {
                    this.player.getPacketSender().queueRelativeMovementStep(this.player.getPosition().getX() < 3320 ? 1 : -1, 0, true);
                    this.player.getPacketSender().openDoubleDoorPair(1598, 3320, 3467, 1599, 3320, 3468);
                    this.player.getPacketSender().sendSoundEffect(318, 1, 0);
                }
            } else {
                this.player.packetSender.sendGameMessage("You need a members account to access members content.");
            }
            this.stop();
            return;
        }
        if (this.objectId == 2647) {
            if (SkillActionHelper.checkSkillRequirement(this.player, 12, 40, "enter the Crafting Guild")) {
                if (this.player.getEquipmentManager().getContainer().getItemAt(4) == null) {
                    if (this.player.getPosition().getY() < 3289) {
                        this.player.getPacketSender().openSingleDoor(this.objectId, this.objectX, this.objectY, this.objectPlane);
                        this.player.getPacketSender().queueRelativeMovementStep(0, 1, true);
                    } else {
                        this.player.getDialogueManager().showTwoLineStatement("You need to wear brown apron to", "enter the guild.");
                        this.player.setInteractionTargetId(0);
                        if (this.player.botEnabled) {
                            this.player.botTaskReturnToBankRequested = true;
                            this.player.botPathWaypointIndex = 0;
                            this.player.botPathSegmentIndex = 0;
                            this.player.botTaskState = "walk to bank";
                            this.player.currentBotTask.continueWalkToBank(this.player, 0);
                        }
                    }
                } else if (this.player.getPosition().getY() < 3289) {
                    this.player.getPacketSender().openSingleDoor(this.objectId, this.objectX, this.objectY, this.objectPlane);
                    this.player.getPacketSender().queueRelativeMovementStep(0, 1, true);
                } else if (this.player.getEquipmentManager().getContainer().getItemAt(4).getId() == 1757) {
                    this.player.getPacketSender().openSingleDoor(this.objectId, this.objectX, this.objectY, this.objectPlane);
                    this.player.getPacketSender().queueRelativeMovementStep(0, -1, true);
                } else {
                    this.player.getDialogueManager().showTwoLineStatement("You need to wear brown apron to", "enter the guild.");
                    this.player.setInteractionTargetId(0);
                    if (this.player.botEnabled) {
                        this.player.botTaskReturnToBankRequested = true;
                        this.player.botPathWaypointIndex = 0;
                        this.player.botPathSegmentIndex = 0;
                        this.player.botTaskState = "walk to bank";
                        this.player.currentBotTask.continueWalkToBank(this.player, 0);
                    }
                }
            }
            this.stop();
            return;
        }
        if (this.objectId == 2551 || this.objectId == 2550 || this.objectId == 2555 || this.objectId == 2556 || this.objectId == 2558 || this.objectId == 2557) {
            this.player.getPacketSender().sendGameMessage("This door is locked.");
            this.stop();
            return;
        }
        if (this.objectId == 2566) {
            this.player.getPacketSender().sendGameMessage("This chest is locked.");
            this.stop();
            return;
        }
        if (GameplayTrace.enabled()) {
            GameplayTrace.log("first-object milestone pre-resource seq=" + this.actionSequence + " player=" + GameplayTrace.describe(this.player) + " objectId=" + this.objectId);
        }
        if (CacheArchiveEntry.searchMapClueObject(this.player, this.objectX, this.objectY)) {
            this.stop();
            return;
        }
        if (CacheFile.searchClueObject(this.player, worldObject)) {
            this.stop();
            return;
        }
        if (GameplayHelper.handleFarmingPatchObjectAction(this.player, this.objectX, this.objectY)) {
            this.stop();
            return;
        }
        if (JungleCutting.isSpecialResource(this.objectId)) {
            JungleCutting.handleFirstObjectAction(this.player, this.objectId,
                    this.objectX, this.objectY, this.objectPlane);
            this.stop();
            return;
        }
        if (JungleCutting.isJungleObject(this.objectId)) {
            JungleCutting.start(this.player, this.objectId, this.objectX, this.objectY);
            this.stop();
            return;
        }
        if (CanoeTreeDefinition.forObjectId(this.objectId) != null) {
            if (!this.player.isMember()) {
                this.player.packetSender.sendGameMessage("You need a members account to access members content.");
            } else if (ServerSettings.freeToPlayWorld) {
                this.player.packetSender.sendGameMessage("You need to be in members world to access members content.");
            } else {
                CanoeTravelManager.handleCanoeTreeClick(this.player, this.objectId, this.objectX, this.objectY);
            }
            this.stop();
            return;
        }
        if (GameplayTrace.enabled()) {
            GameplayTrace.log("first-object milestone pre-woodcutting seq=" + this.actionSequence + " player=" + GameplayTrace.describe(this.player) + " objectId=" + this.objectId + " treeDef=" + TreeDefinition.forObjectId(this.objectId) + " canoeDef=" + CanoeTreeDefinition.forObjectId(this.objectId));
        }
        if (TreeDefinition.forObjectId(this.objectId) != null) {
            if (GameplayTrace.enabled()) {
                GameplayTrace.log("first-object woodcutting-start seq=" + this.actionSequence + " player=" + GameplayTrace.describe(this.player) + " objectId=" + this.objectId + " object=" + this.objectX + "," + this.objectY + "," + this.objectPlane);
            }
            WoodcuttingHandler.startWoodcutting(this.player, this.objectId, this.objectX, this.objectY, false);
            this.stop();
            return;
        }
        if (MiningManager.isMineableRockObjectId(this.objectId)) {
            if (GameplayTrace.enabled()) {
                GameplayTrace.log("first-object mining-start seq=" + this.actionSequence + " player=" + GameplayTrace.describe(this.player) + " objectId=" + this.objectId + " object=" + this.objectX + "," + this.objectY + "," + this.objectPlane);
            }
            if (this.player.getMiningManager().canMineRock(this.objectId)) {
                this.player.getMiningManager().startMining(this.objectId, this.objectX, this.objectY);
            }
            this.stop();
            return;
        }
        if (this.player.getCompostBinManager().handleBinObject(this.objectX, this.objectY)) {
            this.stop();
            return;
        }
        if (this.objectId == 10721) {
            this.player.getPacketSender().queueRelativeMovementStep(0, this.player.getPosition().getY() < 3300 ? 2 : -2, true);
            this.stop();
            return;
        }
        if (DoubleDoorHandler.handleDoubleDoor(this.objectId, this.objectX, this.objectY, this.objectPlane)) {
            if (GameplayTrace.enabled()) {
                GameplayTrace.log("first-object double-door handled seq=" + this.actionSequence + " player=" + GameplayTrace.describe(this.player) + " objectId=" + this.objectId + " object=" + this.objectX + "," + this.objectY + "," + this.objectPlane);
            }
            this.player.getPacketSender().sendSoundEffect(318, 1, 0);
            this.stop();
            return;
        }
        if (this.player.getQuestState(0) == 1 && DoorHandler.handleDoor(this.player, this.objectId, this.objectX, this.objectY, this.objectPlane)) {
            if (GameplayTrace.enabled()) {
                GameplayTrace.log("first-object door handled seq=" + this.actionSequence + " player=" + GameplayTrace.describe(this.player) + " objectId=" + this.objectId + " object=" + this.objectX + "," + this.objectY + "," + this.objectPlane);
            }
            this.stop();
            return;
        }
        // Revision 443 has several regional bank-booth definitions. Keeping
        // every ID in the switch below caused unlisted booths to hit the
        // generic "Nothing interesting happens" fallback.
        if (this.objectId != 3045 && "bank booth".equalsIgnoreCase(this.objectName)) {
            BankManager.openBank(this.player);
            this.stop();
            return;
        }
        if (this.objectName.contains("hay")) {
            this.player.getPacketSender().sendGameMessage("You search the hay bales...");
            this.player.getUpdateState().setAnimation(832);
            this.player.setActionLocked(true);
            CycleEventHandler.getInstance().schedule(this.player, new SearchHayTask(this, this.player), 2);
            this.stop();
            return;
        }
        if (this.objectName.contains("crate")) {
            this.player.getPacketSender().sendGameMessage("You search the crate...");
            this.player.getUpdateState().setAnimation(832);
            this.player.setActionLocked(true);
            CycleEventHandler.getInstance().schedule(this.player, new SearchCrateTask(this, this.player), 2);
            this.stop();
            return;
        }
        if (this.objectName.contains("bookcase")) {
            this.player.getPacketSender().sendGameMessage("You search the books...");
            this.player.setActionLocked(true);
            CycleEventHandler.getInstance().schedule(this.player, new SearchBookcaseTask(this, this.player), 2);
            this.stop();
            return;
        }
        switch (this.objectId) {
            case 2114: {
                GameplayHelper.withdrawCoalTruckCoal(this.player);
                break;
            }
            case 2693: 
            case 2995: 
            case 4483: {
                BankManager.openBank(this.player);
                break;
            }
            case 3045: {
                DialogueManager.startDialogue(this.player, 953);
                break;
            }
            case 2073: 
            case 2074: 
            case 2075: 
            case 2076: 
            case 2077: {
                this.player.getPacketSender().sendGameMessage("You pick a banana.");
                this.player.getInventoryManager().addOrDropItem(new ItemStack(1963, 1));
                ObjectManager.getInstance().removeDynamicObjectAt(this.objectX, this.objectY, this.objectPlane, 0);
                new DynamicObject(worldObject.getObjectId() + 1, this.objectX, this.objectY, this.objectPlane, worldObject.getOrientation(), worldObject.getType(), worldObject.getObjectId(), 10);
                break;
            }
            case 2670: {
                if (this.player.getInventoryManager().containsItem(946)) {
                    int index8 = 0;
                    if (this.player.getInventoryManager().containsItem(1825)) {
                        index8 = 1825;
                    } else if (this.player.getInventoryManager().containsItem(1827)) {
                        index8 = 1827;
                    } else if (this.player.getInventoryManager().containsItem(1829)) {
                        index8 = 1829;
                    } else if (this.player.getInventoryManager().containsItem(1831)) {
                        index8 = 1831;
                    }
                    if (index8 != 0) {
                        this.player.getPacketSender().sendGameMessage("You fill your waterskin with a dose of water.");
                        this.player.getSkillManager().addExperience(8, 10.0);
                        this.player.getInventoryManager().removeItem(new ItemStack(index8, 1));
                        this.player.getInventoryManager().addOrDropItem(new ItemStack(index8 - 2, 1));
                        ObjectManager.getInstance().removeDynamicObjectAt(this.objectX, this.objectY, this.objectPlane, 0);
                        new DynamicObject(worldObject.getObjectId() + 1, this.objectX, this.objectY, this.objectPlane, worldObject.getOrientation(), worldObject.getType(), worldObject.getObjectId(), 100);
                        break;
                    }
                    this.player.getPacketSender().sendGameMessage("You don't have any waterskins to fill.");
                    break;
                }
                this.player.getPacketSender().sendGameMessage("You need a knife to do this.");
                break;
            }
            case 2078: {
                this.player.getPacketSender().sendGameMessage("There are no bananas left on the tree.");
                break;
            }
            case 1408: 
            case 1409: 
            case 1410: 
            case 1411: 
            case 1412: {
                this.player.getPacketSender().sendGameMessage("You pick a pineapple.");
                this.player.getInventoryManager().addOrDropItem(new ItemStack(2114, 1));
                ObjectManager.getInstance().removeDynamicObjectAt(this.objectX, this.objectY, this.objectPlane, 0);
                new DynamicObject(worldObject.getObjectId() + 1, this.objectX, this.objectY, this.objectPlane, worldObject.getOrientation(), worldObject.getType(), worldObject.getObjectId(), 10);
                break;
            }
            case 1413: {
                this.player.getPacketSender().sendGameMessage("There are no pineapples left on this plant.");
                break;
            }
            case 3193: {
                this.player.getUpdateState().setAnimation(832);
                new DynamicObject(3194, this.objectX, this.objectY, this.objectPlane, worldObject.getOrientation(), worldObject.getType(), this.objectId, 500);
                break;
            }
            case 1293: 
            case 1294: 
            case 1317: {
                if (this.player.getQuestState(95) != 1) {
                    interactionTargetId = QuestDefinition.forId(95);
                    String name3 = ((QuestDefinition)interactionTargetId).getName();
                    this.player.getPacketSender().sendGameMessage("You need to complete " + name3 + " to do this.");
                    break;
                }
                DialogueManager.startDialogue(this.player, 10011);
                break;
            }
            case 1805: {
                if (this.player.getQuestPoints() >= 32) {
                    int position3 = this.player.getPosition().getY() > 3362 ? -1 : 1;
                    this.player.getPacketSender().queueRelativeMovementStep(0, position3, true);
                    this.player.getPacketSender().openSingleDoor(this.objectId, this.objectX, this.objectY, this.objectPlane);
                    if (position3 != -1) break;
                    this.player.getDialogueManager().setDialogueNpcId(198);
                    this.player.getDialogueManager().showNpcTwoLineDialogue("Greetings bold adventurer. Welcome to the guild of", "Champions.", 591);
                    this.player.getDialogueManager().finishDialogue();
                    break;
                }
                this.player.getDialogueManager().showOneLineStatement("You need atleast 32 Quest points to enter the Champions' Guild.");
                break;
            }
            case 12094: {
                this.player.moveTo(new Position(3201, 3169));
                break;
            }
            case 12003: {
                this.player.moveTo(new Position(3262, 3171));
                break;
            }
            case 4624: {
                this.player.moveTo(new Position(2208, 4938));
                break;
            }
            case 4622: {
                this.player.moveTo(new Position(2899, 3565));
                break;
            }
            case 5098: {
                this.player.moveTo(new Position(2637, 9517, 0));
                break;
            }
            case 5097: {
                this.player.moveTo(new Position(2637, 9510, 2));
                break;
            }
            case 5096: {
                this.player.moveTo(new Position(2650, 9591, 0));
                break;
            }
            case 5094: {
                this.player.moveTo(new Position(2643, 9595, 2));
                break;
            }
            case 10776: {
                this.player.moveTo(new Position(3360, 3306, 0));
                break;
            }
            case 10773: {
                this.player.moveTo(new Position(3366, 3306, 0));
                break;
            }
            case 10775: {
                this.player.moveTo(new Position(3357, 3307, 1));
                break;
            }
            case 10771: {
                this.player.moveTo(new Position(3369, 3307, 1));
                break;
            }
            case 2834: {
                if (this.player.getPosition().getX() > 2567) {
                    this.player.getUpdateState().setAnimation(839);
                    this.player.getPacketSender().queueRelativeMovementStep(-2, 0, true);
                    break;
                }
                this.player.getUpdateState().setAnimation(2750);
                AgilityObstacleHandler.startForcedMovement(this.player, 2, 0, 1, 80, 2, true, 0, 0);
                break;
            }
            case 1967: 
            case 1968: {
                this.player.getPacketSender().queueRelativeMovementStep(0, this.player.getPosition().getY() < 3492 ? 2 : -2, true);
                break;
            }
            case 4615: 
            case 4616: {
                AgilityObstacleHandler.startForcedMovement(this.player, this.player.getPosition().getX() < 2597 ? 4 : -4, 0, 1, 150, 4, true, 0, this.player.getPosition().getX() < 2597 ? 756 : 754);
                break;
            }
            case 51: {
                this.player.getUpdateState().setAnimation(754);
                AgilityObstacleHandler.startForcedMovement(this.player, this.player.getPosition().getX() < 2662 ? 1 : -1, 0, 1, 80, 2, true, 0, 0);
                break;
            }
            case 2186: {
                this.player.getUpdateState().setAnimation(754);
                AgilityObstacleHandler.startForcedMovement(this.player, 0, this.player.getPosition().getY() < 3161 ? 1 : -1, 1, 80, 2, true, 0, 0);
                break;
            }
            case 5259: {
                this.player.getPacketSender().queueRelativeMovementStep(0, this.player.getPosition().getY() < 3508 ? 1 : -1, true);
                break;
            }
            case 2618: {
                this.player.getUpdateState().setAnimation(839);
                AgilityObstacleHandler.startForcedMovement(this.player, 0, this.player.getPosition().getY() < 3493 ? 1 : -1, 1, 80, 2, true, 0, 0);
                break;
            }
            case 2266: {
                if (this.player.getPosition().getY() > 2963) {
                    this.player.getPacketSender().queueRelativeMovementStep(0, -1, true);
                    this.player.getPacketSender().openSingleDoor(this.objectId, this.objectX, this.objectY, this.objectPlane);
                    break;
                }
                DialogueManager.startDialogue(this.player, 513);
                break;
            }
            case 10177: {
                DialogueManager.startDialogue(this.player, 10005);
                break;
            }
            case 8958: 
            case 8959: 
            case 8960: {
                interactionTargetId = new Position(2490, this.objectY);
                Position position2 = new Position(2490, this.objectY + 2);
                boolean enabled2 = false;
                boolean enabled3 = false;
                GroundItemManager.getInstance();
                if (GroundItemManager.findVisibleItem(this.player, 3695, (Position)interactionTargetId) != null) {
                    enabled2 = true;
                }
                GroundItemManager.getInstance();
                if (GroundItemManager.findVisibleItem(this.player, 3695, position2) != null) {
                    enabled3 = true;
                }
                Player[] playerArray = World.getPlayers();
                int length = playerArray.length;
                int index9 = 0;
                while (index9 < length) {
                    interactionTargetId = playerArray[index9];
                    if (interactionTargetId != null) {
                        if (((Entity)interactionTargetId).getPosition().getX() == 2490 && ((Entity)interactionTargetId).getPosition().getY() == this.objectY) {
                            enabled2 = true;
                        }
                        if (((Entity)interactionTargetId).getPosition().getX() == 2490 && ((Entity)interactionTargetId).getPosition().getY() == this.objectY + 2) {
                            enabled3 = true;
                        }
                    }
                    ++index9;
                }
                if (enabled2 && enabled3) {
                    new DynamicObject(ServerSettings.placeholderObjectId, this.objectX, this.objectY, this.objectPlane, worldObject.getOrientation(), worldObject.getType(), this.objectId, 50);
                    break;
                }
                this.player.getPacketSender().sendGameMessage("The door is locked.");
                break;
            }
            case 8966: {
                this.player.moveTo(new Position(2523, 3739, 0));
                break;
            }
            case 8929: {
                this.player.moveTo(new Position(2442, 10146, 0));
                break;
            }
            case 8930: {
                this.player.moveTo(new Position(2545, 10143, 0));
                break;
            }
            case 10193: {
                AttackStyleDefinition.startDelayedObjectMove(this.player, new Position(2545, 10143, 0));
                break;
            }
            case 10195: {
                AttackStyleDefinition.startDelayedObjectMove(this.player, new Position(1809, 4405, 2));
                break;
            }
            case 10196: {
                AttackStyleDefinition.startDelayedObjectMove(this.player, new Position(1807, 4405, 3));
                break;
            }
            case 10197: {
                AttackStyleDefinition.startDelayedObjectMove(this.player, new Position(1823, 4404, 2));
                break;
            }
            case 10198: {
                AttackStyleDefinition.startDelayedObjectMove(this.player, new Position(1825, 4404, 3));
                break;
            }
            case 10199: {
                AttackStyleDefinition.startDelayedObjectMove(this.player, new Position(1834, 4388, 2));
                break;
            }
            case 10200: {
                AttackStyleDefinition.startDelayedObjectMove(this.player, new Position(1834, 4390, 3));
                break;
            }
            case 10201: {
                AttackStyleDefinition.startDelayedObjectMove(this.player, new Position(1811, 4394, 1));
                break;
            }
            case 10202: {
                AttackStyleDefinition.startDelayedObjectMove(this.player, new Position(1812, 4394, 2));
                break;
            }
            case 10203: {
                AttackStyleDefinition.startDelayedObjectMove(this.player, new Position(1799, 4386, 2));
                break;
            }
            case 10204: {
                AttackStyleDefinition.startDelayedObjectMove(this.player, new Position(1799, 4388, 1));
                break;
            }
            case 10205: {
                AttackStyleDefinition.startDelayedObjectMove(this.player, new Position(1796, 4382, 1));
                break;
            }
            case 10206: {
                AttackStyleDefinition.startDelayedObjectMove(this.player, new Position(1796, 4382, 2));
                break;
            }
            case 10207: {
                AttackStyleDefinition.startDelayedObjectMove(this.player, new Position(1800, 4369, 2));
                break;
            }
            case 10208: {
                AttackStyleDefinition.startDelayedObjectMove(this.player, new Position(1802, 4370, 1));
                break;
            }
            case 10209: {
                AttackStyleDefinition.startDelayedObjectMove(this.player, new Position(1827, 4362, 1));
                break;
            }
            case 10210: {
                AttackStyleDefinition.startDelayedObjectMove(this.player, new Position(1825, 4362, 2));
                break;
            }
            case 10211: {
                AttackStyleDefinition.startDelayedObjectMove(this.player, new Position(1863, 4373, 2));
                break;
            }
            case 10212: {
                AttackStyleDefinition.startDelayedObjectMove(this.player, new Position(1863, 4371, 1));
                break;
            }
            case 10213: {
                AttackStyleDefinition.startDelayedObjectMove(this.player, new Position(1864, 4389, 1));
                break;
            }
            case 10214: {
                AttackStyleDefinition.startDelayedObjectMove(this.player, new Position(1864, 4387, 2));
                break;
            }
            case 10215: {
                AttackStyleDefinition.startDelayedObjectMove(this.player, new Position(1890, 4407, 0));
                break;
            }
            case 10216: {
                AttackStyleDefinition.startDelayedObjectMove(this.player, new Position(1890, 4406, 1));
                break;
            }
            case 10217: {
                AttackStyleDefinition.startDelayedObjectMove(this.player, new Position(1957, 4373, 1));
                break;
            }
            case 10218: {
                AttackStyleDefinition.startDelayedObjectMove(this.player, new Position(1957, 4371, 0));
                break;
            }
            case 10219: {
                AttackStyleDefinition.startDelayedObjectMove(this.player, new Position(1824, 4379, 3));
                break;
            }
            case 10220: {
                AttackStyleDefinition.startDelayedObjectMove(this.player, new Position(1824, 4381, 2));
                break;
            }
            case 10221: {
                AttackStyleDefinition.startDelayedObjectMove(this.player, new Position(1838, 4375, 2));
                break;
            }
            case 10222: {
                AttackStyleDefinition.startDelayedObjectMove(this.player, new Position(1838, 4377, 3));
                break;
            }
            case 10223: {
                AttackStyleDefinition.startDelayedObjectMove(this.player, new Position(1850, 4386, 1));
                break;
            }
            case 10224: {
                AttackStyleDefinition.startDelayedObjectMove(this.player, new Position(1850, 4387, 2));
                break;
            }
            case 10225: {
                AttackStyleDefinition.startDelayedObjectMove(this.player, new Position(1932, 4378, 1));
                break;
            }
            case 10226: {
                AttackStyleDefinition.startDelayedObjectMove(this.player, new Position(1932, 4380, 2));
                break;
            }
            case 10227: {
                if (this.objectX == 1961 && this.objectY == 4392) {
                    AttackStyleDefinition.startDelayedObjectMove(this.player, new Position(1961, 4392, 2));
                    break;
                }
                AttackStyleDefinition.startDelayedObjectMove(this.player, new Position(1932, 4377, 1));
                break;
            }
            case 10228: {
                AttackStyleDefinition.startDelayedObjectMove(this.player, new Position(1961, 4393, 3));
                break;
            }
            case 195: {
                AttackStyleDefinition.startDelayedObjectMove(this.player, new Position(2410, 3421, 0));
                break;
            }
            case 10229: {
                AttackStyleDefinition.startDelayedObjectMove(this.player, new Position(1912, 4367, 0));
                break;
            }
            case 10230: {
                AttackStyleDefinition.startDelayedObjectMove(this.player, new Position(2900, 4449, 0));
                break;
            }
            case 9398: {
                BankManager.openDepositBox(this.player);
                break;
            }
            case 10596: {
                if (!this.player.isMember()) {
                    this.player.packetSender.sendGameMessage("You need a members account to access members content.");
                    break;
                }
                if (ServerSettings.freeToPlayWorld) {
                    this.player.packetSender.sendGameMessage("You need to be in members world to access members content.");
                    break;
                }
                this.player.moveTo(new Position(3056, 9555));
                break;
            }
            case 10595: {
                this.player.moveTo(new Position(3056, 9562));
                break;
            }
            case 5949: {
                if (this.player.getPosition().getY() > this.objectY) {
                    this.player.moveTo(new Position(3221, 9552));
                    break;
                }
                this.player.moveTo(new Position(3221, 9556));
                break;
            }
            case 2623: {
                if (!this.player.getInventoryManager().containsItem(1590) && this.player.getPosition().getX() > 2923) {
                    this.player.getPacketSender().sendGameMessage("The door is locked, you need a dusty key to open it.");
                    break;
                }
                this.player.getPacketSender().openSingleDoor(this.objectId, this.objectX, this.objectY, 0);
                this.player.getPacketSender().queueRelativeMovementStep(this.player.getPosition().getX() > 2923 ? -1 : 1, 0, true);
                break;
            }
            case 2631: {
                if (this.player.getPosition().getY() < 9692) {
                    if (!this.player.getInventoryManager().containsItem(1591) && this.player.getPosition().getY() > 9689) {
                        this.player.getPacketSender().sendGameMessage("The door is locked, you need a jail key to open it.");
                        break;
                    }
                    this.player.getPacketSender().openSingleDoor(this.objectId, this.objectX, this.objectY, 0);
                    this.player.getPacketSender().queueRelativeMovementStep(0, this.player.getPosition().getY() > 9689 ? -1 : 1, true);
                    break;
                }
                if (!this.player.getInventoryManager().containsItem(1591) && this.player.getPosition().getY() < 9695) {
                    this.player.getPacketSender().sendGameMessage("The door is locked, you need a jail key to open it.");
                    break;
                }
                this.player.getPacketSender().openSingleDoor(this.objectId, this.objectX, this.objectY, 0);
                this.player.getPacketSender().queueRelativeMovementStep(0, this.player.getPosition().getY() < 9695 ? 1 : -1, true);
                break;
            }
            case 2320: {
                if (this.objectY < this.player.getPosition().getY()) {
                    this.player.moveTo(new Position(this.player.getPosition().getX(), 9964));
                    break;
                }
                this.player.moveTo(new Position(this.player.getPosition().getX(), 9969));
                break;
            }
            case 5083: {
                if (this.player.isBrimhavenOpen()) {
                    this.player.scheduleDelayedMove(new Position(2713, 9564));
                    this.player.setBrimhavenOpen(false);
                    break;
                }
                DialogueManager.startDialogue(this.player, 1595);
                break;
            }
            case 5084: {
                this.player.moveTo(new Position(2744, 3152));
                break;
            }
            case 4499: {
                this.player.scheduleDelayedMove(new Position(2808, 10002));
                this.player.getPacketSender().sendGameMessage("You enter the cave.");
                break;
            }
            case 4500: {
                this.player.moveTo(new Position(2796, 3615));
                break;
            }
            case 5100: {
                if (this.player.getPosition().getY() < this.objectY) {
                    this.player.moveTo(new Position(2655, 9573));
                    break;
                }
                this.player.moveTo(new Position(2655, 9566));
                break;
            }
            case 5111: {
                this.player.moveTo(new Position(2649, 9562));
                break;
            }
            case 5110: {
                this.player.moveTo(new Position(2647, 9557));
                break;
            }
            case 5088: {
                AgilityObstacleHandler.startAgilityMovement(this.player, 0.0, 5, 0, -1, 762, -1, 2, null, null);
                break;
            }
            case 5090: {
                AgilityObstacleHandler.startAgilityMovement(this.player, 0.0, -5, 0, -1, 762, -1, 2, null, null);
                break;
            }
            case 5099: {
                if (this.objectX == 2698 && this.objectY == 9498) {
                    AgilityObstacleHandler.startAgilityMovement(this.player, 0.0, 0, -8, 746, 844, 748, 7, null, null);
                    break;
                }
                if (this.objectX != 2698 || this.objectY != 9493) break;
                AgilityObstacleHandler.startAgilityMovement(this.player, 0.0, 0, 8, 746, 844, 748, 7, null, null);
                break;
            }
            case 6552: {
                boolean enabled4;
                if (this.player.getQuestState(26) != 1) {
                    interactionTargetId = QuestDefinition.forId(14);
                    String name4 = ((QuestDefinition)interactionTargetId).getName();
                    this.player.getPacketSender().sendGameMessage("You need to complete " + name4 + " to do this.");
                    break;
                }
                this.player.getUpdateState().setAnimation(645);
                boolean spellbook = enabled4 = this.player.getSpellbook() == Spellbook.MODERN;
                if (this.player.getSpellbook() == Spellbook.NECROMANCY) {
                    boolean enabled5 = enabled4 = this.player.previousSpellbookBeforeNecromancy == Spellbook.MODERN;
                }
                if (enabled4) {
                    this.player.getPacketSender().sendGameMessage(" You feel a strange wisdom fill your mind...");
                    this.player.getPacketSender().setSidebarInterface(6, 12855);
                    this.player.setSpellbook(Spellbook.ANCIENT);
                    break;
                }
                this.player.getPacketSender().sendGameMessage("You feel a strange drain upon your memory...");
                this.player.getPacketSender().setSidebarInterface(6, 1151);
                this.player.setSpellbook(Spellbook.MODERN);
                break;
            }
            case 6481: {
                this.player.getPacketSender().sendGameMessage("You sneak into the back of the pyramid...");
                this.player.scheduleDelayedMove(new Position(3229, 9310));
                break;
            }
            case 6515: {
                this.player.getPacketSender().sendGameMessage("You search the sarcophagus, and sneak into it...");
                this.player.getPacketSender().showInterface(8677);
                CycleEventHandler.getInstance().schedule(this.player, new SarcophagusSneakTask(this, this.player), 4);
                break;
            }
            case 1742: {
                if (this.objectX == 2445 && this.objectY == 3434) {
                    this.player.moveTo(new Position(2445, 3433, 1));
                }
                if (this.objectX == 2444 && this.objectY == 3414) {
                    this.player.moveTo(new Position(2445, 3416, 1));
                }
                if (this.objectX == 2475 && this.objectY == 3400) {
                    this.player.moveTo(new Position(2475, 3399, 1));
                }
                if (this.objectX == 2479 && this.objectY == 3408) {
                    this.player.moveTo(new Position(2479, 3407, 1));
                }
                if (this.objectX == 2485 && this.objectY == 3402) {
                    this.player.moveTo(new Position(2485, 3401, 1));
                }
                if (this.objectX == 2488 && this.objectY == 3407) {
                    this.player.moveTo(new Position(2489, 3409, 1));
                }
                if (this.objectX == 2461 && this.objectY == 3416) {
                    this.player.moveTo(new Position(2460, 3417, 1));
                }
                if (this.objectX == 2455 && this.objectY == 3417) {
                    this.player.moveTo(new Position(2457, 3417, 1));
                }
                if (this.objectX == 2440 && this.objectY == 3404) {
                    this.player.moveTo(new Position(2440, 3403, 1));
                }
                if (this.objectX == 2418 && this.objectY == 3417) {
                    this.player.moveTo(new Position(2418, 3416, 1));
                }
                if (this.objectX == 2401 && this.objectY == 3449) {
                    this.player.moveTo(new Position(2400, 3450, 1));
                }
                if (this.objectX == 2395 && this.objectY == 3474) {
                    this.player.moveTo(new Position(2396, 3476, 1));
                }
                if (this.objectX == 2376 && this.objectY == 3489) {
                    this.player.moveTo(new Position(2378, 3489, 1));
                }
                if (this.objectX == 2384 && this.objectY == 3506) {
                    this.player.moveTo(new Position(2384, 3505, 1));
                }
                if (this.objectX == 2389 && this.objectY == 3512) {
                    this.player.moveTo(new Position(2388, 3513, 1));
                }
                if (this.objectX == 2396 && this.objectY == 3501) {
                    this.player.moveTo(new Position(2396, 3500, 1));
                }
                if (this.objectX == 2398 && this.objectY == 3512) {
                    this.player.moveTo(new Position(2397, 3513, 1));
                }
                if (this.objectX == 2413 && this.objectY == 3488) {
                    this.player.moveTo(new Position(2412, 3489, 1));
                }
                if (this.objectX == 2417 && this.objectY == 3490) {
                    this.player.moveTo(new Position(2418, 3492, 1));
                }
                if (this.objectX == 2420 && this.objectY == 3471) {
                    this.player.moveTo(new Position(2421, 3473, 1));
                }
                if (this.objectX != 2416 || this.objectY != 3445) break;
                this.player.moveTo(new Position(2415, 3446, 1));
                break;
            }
            case 1744: {
                if (this.objectX == 2445 && this.objectY == 3434) {
                    this.player.moveTo(new Position(2445, 3436, 0));
                }
                if (this.objectX == 2445 && this.objectY == 3415) {
                    this.player.moveTo(new Position(2445, 3413, 0));
                }
                if (this.objectX == 2475 && this.objectY == 3400) {
                    this.player.moveTo(new Position(2475, 3399, 0));
                }
                if (this.objectX == 2479 && this.objectY == 3408) {
                    this.player.moveTo(new Position(2479, 3407, 0));
                }
                if (this.objectX == 2485 && this.objectY == 3402) {
                    this.player.moveTo(new Position(2485, 3401, 0));
                }
                if (this.objectX == 2489 && this.objectY == 3408) {
                    this.player.moveTo(new Position(2488, 3409, 0));
                }
                if (this.objectX == 2461 && this.objectY == 3417) {
                    this.player.moveTo(new Position(2460, 3417, 0));
                }
                if (this.objectX == 2456 && this.objectY == 3417) {
                    this.player.moveTo(new Position(2457, 3417, 0));
                }
                if (this.objectX == 2440 && this.objectY == 3404) {
                    this.player.moveTo(new Position(2440, 3403, 0));
                }
                if (this.objectX == 2418 && this.objectY == 3417) {
                    this.player.moveTo(new Position(2418, 3416, 0));
                }
                if (this.objectX == 2401 && this.objectY == 3450) {
                    this.player.moveTo(new Position(2400, 3450, 0));
                }
                if (this.objectX == 2396 && this.objectY == 3475) {
                    this.player.moveTo(new Position(2396, 3476, 0));
                }
                if (this.objectX == 2377 && this.objectY == 3489) {
                    this.player.moveTo(new Position(2378, 3489, 0));
                }
                if (this.objectX == 2384 && this.objectY == 3506) {
                    this.player.moveTo(new Position(2384, 3505, 0));
                }
                if (this.objectX == 2389 && this.objectY == 3513) {
                    this.player.moveTo(new Position(2388, 3513, 0));
                }
                if (this.objectX == 2396 && this.objectY == 3501) {
                    this.player.moveTo(new Position(2395, 3501, 0));
                }
                if (this.objectX == 2398 && this.objectY == 3513) {
                    this.player.moveTo(new Position(2397, 3513, 0));
                }
                if (this.objectX == 2413 && this.objectY == 3489) {
                    this.player.moveTo(new Position(2412, 3489, 0));
                }
                if (this.objectX == 2418 && this.objectY == 3491) {
                    this.player.moveTo(new Position(2419, 3491, 0));
                }
                if (this.objectX == 2421 && this.objectY == 3472) {
                    this.player.moveTo(new Position(2421, 3473, 0));
                }
                if (this.objectX != 2416 || this.objectY != 3446) break;
                this.player.moveTo(new Position(2417, 3447, 0));
                break;
            }
            case 2407: {
                if (this.objectX != 2874 || this.objectY != 9750) break;
                this.player.moveTo(new Position(3250, 3772));
                this.player.getPacketSender().sendGameMessage("You feel the world around you dissolve...");
                break;
            }
            case 2408: {
                DialogueManager.startDialogue(this.player, 656);
                break;
            }
            case 2640: {
                PrayerManager.rechargePrayerWithBoost(this.player);
                break;
            }
            case 2641: {
                if (this.player.getSkillManager().getBaseLevel(5) < 31) {
                    this.player.getDialogueManager().showOneLineStatement("You need a Prayer level of 31 to enter the Prayer guild.");
                    break;
                }
                AttackStyleDefinition.climbOneFloorAtCurrentTile(this.player, "up");
                break;
            }
            case 1804: {
                if (this.player.getPosition().getY() < 3450 && !this.player.getInventoryManager().containsItem(983)) {
                    this.player.getPacketSender().sendGameMessage("You need a brass key to enter here.");
                    break;
                }
                this.player.getPacketSender().openSingleDoor(this.objectId, this.objectX, this.objectY, 0);
                this.player.getPacketSender().queueRelativeMovementStep(0, this.player.getPosition().getY() < 3450 ? 1 : -1, true);
                break;
            }
            case 733: {
                AttackStyleDefinition.slashWeb(this.player, this.objectX, this.objectY, this.player.getEquipmentManager().getItemIdAtSlot(3));
                break;
            }
            case 9334: {
                this.player.getPacketSender().sendGameMessage("Please use the trapdoor located a bit north of here.");
                break;
            }
            case 6434: {
                AttackStyleDefinition.toggleObjectAfterAnimation(this.player, this.objectId, 6435, worldObject);
                break;
            }
            case 1568: {
                AttackStyleDefinition.toggleObjectAfterAnimation(this.player, this.objectId, 1570, worldObject);
                break;
            }
            case 882: 
            case 1570: 
            case 1754: 
            case 1759: 
            case 5947: 
            case 6435: 
            case 9472: 
            case 11867: {
                if (this.objectId == 5947 && !this.player.isMember()) {
                    this.player.packetSender.sendGameMessage("You need a members account to access members content.");
                    break;
                }
                if (this.objectId == 5947 && ServerSettings.freeToPlayWorld) {
                    this.player.packetSender.sendGameMessage("You need to be in members world to access members content.");
                    break;
                }
                if (this.objectId == 5947 && !this.player.swampCaveRopeAttached) {
                    this.player.packetSender.sendGameMessage("You need to attach rope first before you can climb.");
                    break;
                }
                AttackStyleDefinition.startDelayedObjectMove(this.player, new Position(this.player.getPosition().getX(), this.player.getPosition().getY() + 6400));
                break;
            }
            case 1755: 
            case 1757: 
            case 2405: 
            case 5946: 
            case 6436: {
                if (this.objectX == 3222 && this.objectY == 9588) {
                    AttackStyleDefinition.startDelayedObjectMove(this.player, new Position(3201, 3169, 0));
                    break;
                }
                if (this.objectX == 3097 && this.objectY == 9867) {
                    AttackStyleDefinition.startDelayedObjectMove(this.player, new Position(3096, 3468, 0));
                    break;
                }
                AttackStyleDefinition.startDelayedObjectMove(this.player, new Position(this.player.getPosition().getX(), this.player.getPosition().getY() - 6400));
                break;
            }
            case 5488: {
                AttackStyleDefinition.startDelayedObjectMove(this.player, new Position(3201, 3169, 0));
                break;
            }
            case 2410: {
                AttackStyleDefinition.startDelayedObjectMove(this.player, new Position(3262, 3171, 0));
                break;
            }
            case 3432: {
                AttackStyleDefinition.toggleObjectAfterAnimation(this.player, this.objectId, 3433, worldObject);
                break;
            }
            case 3433: {
                AttackStyleDefinition.startDelayedObjectMove(this.player, new Position(3440, 9887, 0));
                break;
            }
            case 2156: {
                this.player.moveTo(new Position(3113, 3169, 0));
                break;
            }
            case 2157: {
                this.player.moveTo(new Position(2907, 3332, 0));
                break;
            }
            case 2158: {
                this.player.moveTo(new Position(2703, 3404, 0));
                break;
            }
            case 2492: {
                switch (this.player.getAbyssMageNpcId()) {
                    case 171: {
                        this.player.moveTo(new Position(2388, 9812));
                        break;
                    }
                    case 462: {
                        this.player.moveTo(new Position(2591, 3086));
                        break;
                    }
                    case 553: {
                        this.player.moveTo(new Position(3253, 3401));
                        break;
                    }
                    case 300: {
                        this.player.moveTo(new Position(3101, 9571));
                        break;
                    }
                    case 844: {
                        this.player.moveTo(new Position(2684, 3322));
                    }
                }
                break;
            }
            case 3044: {
                if (this.player.getQuestState(0) == 1) break;
                this.player.getDialogueManager().showThreeLineStatement("This is a furnace for smelting metal. To use it simply click on the", "ore you wish to smelt then click on the furnace you would like to", "use.");
                this.player.setInteractionTargetId(0);
                break;
            }
            case 3039: {
                this.player.getDialogueManager().showThreeLineStatement("To cook something you need to use the item you would like to cook", "on the cooking range. Do this by clicking the item in your inventory", "and then clicking the range.");
                this.player.setInteractionTargetId(0);
                break;
            }
            case 3566: {
                int position4 = this.player.getPosition().getX() > 2768 ? -5 : 5;
                AgilityObstacleHandler.startDelayedShortcutMovement(this.player, 50, position4, 0, 2, 30, 751);
                break;
            }
            case 5492: {
                break;
            }
            case 5581: {
                GameplayHelper.takeAxeFromLog(this.player, this.objectX, this.objectY);
                break;
            }
            case 8689: {
                GameplayHelper.milkCow(this.player);
                break;
            }
            case 2718: 
            case 2719: 
            case 2720: 
            case 2721: {
                FlourMillHandler.operateHopperControls(this.player);
                break;
            }
            case 1781: {
                FlourMillHandler.collectFlourFromBin(this.player);
                break;
            }
            case 2609: {
                AttackStyleDefinition.startDelayedObjectMove(this.player, new Position(2834, 9657, 0));
                break;
            }
            case 2610: {
                AttackStyleDefinition.startDelayedObjectMove(this.player, new Position(2833, 3257, 0));
                break;
            }
            case 2147: {
                AttackStyleDefinition.startDelayedObjectMove(this.player, new Position(3104, 9576, 0));
                break;
            }
            case 4568: 
            case 9582: 
            case 11511: 
            case 11729: 
            case 11732: 
            case 12536: {
                AttackStyleDefinition.climbOffsetLadder(this.player, "up");
                break;
            }
            case 4570: 
            case 9584: 
            case 11731: 
            case 11733: 
            case 12538: {
                AttackStyleDefinition.climbOffsetLadder(this.player, "down");
                break;
            }
            case 1722: 
            case 1725: 
            case 4756: 
            case 9470: 
            case 11734: 
            case 11736: {
                if (this.objectX == 2590 && this.objectY == 3089) {
                    this.player.moveTo(new Position(this.player.getPosition().getX(), 3092, 1));
                    break;
                }
                AttackStyleDefinition.climbDirectionalStairs(this.player, 4, 4, "up");
                break;
            }
            case 1730: 
            case 4493: 
            case 4495: 
            case 11498: {
                int value = 5;
                if (this.objectId == 1730) {
                    value = 4;
                }
                AttackStyleDefinition.climbDirectionalStairs(this.player, value, value, "up");
                break;
            }
            case 1723: 
            case 1726: 
            case 4755: 
            case 9471: 
            case 11735: 
            case 11737: {
                AttackStyleDefinition.climbDirectionalStairs(this.player, 4, 4, "down");
                break;
            }
            case 1731: 
            case 4494: 
            case 4496: 
            case 11499: {
                int value2 = 5;
                if (this.objectId == 1731) {
                    value2 = 4;
                }
                AttackStyleDefinition.climbDirectionalStairs(this.player, value2, value2, "down");
                break;
            }
            case 7057: {
                AttackStyleDefinition.climbFourTileDirectionalStairs(this.player, 4, 4, "up");
                break;
            }
            case 7056: {
                AttackStyleDefinition.climbFourTileDirectionalStairs(this.player, 4, 4, "down");
                break;
            }
            case 1747: 
            case 1750: 
            case 2796: 
            case 2833: 
            case 8744: 
            case 9319: 
            case 9558: 
            case 11727: 
            case 11739: 
            case 12112: 
            case 12964: {
                AttackStyleDefinition.climbOneFloorAtCurrentTile(this.player, "up");
                break;
            }
            case 4163: 
            case 4164: {
                AttackStyleDefinition.climbTwoFloorsAtCurrentTile(this.player, "up", 2);
                break;
            }
            case 4173: 
            case 4174: {
                AttackStyleDefinition.climbTwoFloorsAtCurrentTile(this.player, "down", 2);
                break;
            }
            case 1746: 
            case 1749: 
            case 2797: 
            case 3205: 
            case 4189: 
            case 8746: 
            case 9320: 
            case 9559: 
            case 9560: 
            case 11728: 
            case 11741: 
            case 11742: 
            case 12113: 
            case 12966: {
                AttackStyleDefinition.climbOneFloorAtCurrentTile(this.player, "down");
                break;
            }
            case 2148: {
                AttackStyleDefinition.startDelayedObjectMove(this.player, new Position(3105, 3162, 0));
                break;
            }
            case 881: {
                AttackStyleDefinition.toggleObjectAfterAnimation(this.player, this.objectId, 882, worldObject);
                break;
            }
            case 883: {
                AttackStyleDefinition.toggleObjectAfterAnimation(this.player, this.objectId, 881, worldObject);
                break;
            }
            case 2112: {
                if (this.player.getPosition().getY() > 9756) {
                    if (!SkillActionHelper.checkSkillRequirement(this.player, 14, 60, "enter the Mining Guild")) break;
                    this.player.getPacketSender().openSingleDoor(this.objectId, this.objectX, this.objectY, this.objectPlane);
                    this.player.getPacketSender().queueRelativeMovementStep(0, -1, true);
                    break;
                }
                this.player.getPacketSender().openSingleDoor(this.objectId, this.objectX, this.objectY, this.objectPlane);
                this.player.getPacketSender().queueRelativeMovementStep(0, 1, true);
                break;
            }
            case 2113: {
                if (!SkillActionHelper.checkSkillRequirement(this.player, 14, 60, "enter the Mining Guild")) break;
                AttackStyleDefinition.startDelayedObjectMove(this.player, new Position(this.player.getPosition().getX(), this.player.getPosition().getY() + 6400, 0));
                break;
            }
            case 98: {
                if (this.objectX != 2650 || this.objectY != 9804) break;
                if (this.player.getActiveCaveLightLevel() > 0) {
                    this.player.moveTo(new Position(this.player.getPosition().getX() - 8, this.player.getPosition().getY() - 41, 0));
                    break;
                }
                this.player.moveTo(new Position(this.player.getPosition().getX() - 8, this.player.getPosition().getY() - 64, 0));
                this.player.getDialogueManager().showOneLineStatement("Hmm...bit dark down here! I'm not going to venture far!");
                this.player.getDialogueManager().finishDialogue();
                break;
            }
            case 96: {
                if (this.objectX == 2638 && this.objectY == 9763) {
                    this.player.moveTo(new Position(2649, this.player.getPosition().getY() + 41, 0));
                }
                if (this.objectX != 2638 || this.objectY != 9740) break;
                this.player.moveTo(new Position(2649, this.player.getPosition().getY() + 64, 0));
                break;
            }
            case 2616: {
                this.player.moveTo(new Position(this.player.getPosition().getX() - 38, this.player.getPosition().getY() + 6415, 0));
                this.player.getPacketSender().sendGameMessage("You walk down the stairs...");
                break;
            }
            case 2617: {
                this.player.moveTo(new Position(this.player.getPosition().getX() + 38, this.player.getPosition().getY() - 6415, 0));
                this.player.getPacketSender().sendGameMessage("You walk up the stairs...");
                break;
            }
            case 1733: {
                AttackStyleDefinition.climbDungeonLadder(this.player, 4, 4, "down");
                break;
            }
            case 1734: {
                AttackStyleDefinition.climbDungeonLadder(this.player, 4, 4, "up");
                break;
            }
            case 492: {
                AttackStyleDefinition.startDelayedObjectMove(this.player, new Position(2857, 9569, 0));
                break;
            }
            case 1764: {
                AttackStyleDefinition.startDelayedObjectMove(this.player, new Position(2856, 3167, 0));
                break;
            }
            case 9358: {
                if (this.player.isMember()) {
                    if (ServerSettings.freeToPlayWorld) {
                        this.player.packetSender.sendGameMessage("You need to be in members world to access members content.");
                        break;
                    }
                    this.player.moveToGroundPosition(2480, 5175, 0);
                    break;
                }
                this.player.packetSender.sendGameMessage("You need a members account to access members content.");
                break;
            }
            case 9356: {
                this.player.getFightCaveController().startFightCave();
                break;
            }
            case 9357: {
                this.player.getFightCaveController().leaveFightCave();
                break;
            }
            case 9706: {
                if (this.player.mageArenaProgressStage < 5) break;
                this.player.getPacketSender().sendSoundEffect(319, 1, 0);
                this.player.getUpdateState().setAnimation(835);
                this.player.getPacketSender().sendGameMessage("You pull the lever...");
                this.player.getTeleportManager().startStandardTeleport(3105, 3951, 0, "... and get teleported into the arena!");
                break;
            }
            case 9707: {
                this.player.getPacketSender().sendSoundEffect(319, 1, 0);
                this.player.getUpdateState().setAnimation(835);
                this.player.getPacketSender().sendGameMessage("You pull the lever...");
                this.player.getTeleportManager().startStandardTeleport(3105, 3956, 0, "... and get teleported out of the arena!");
                break;
            }
            case 1767: {
                AttackStyleDefinition.startDelayedObjectMove(this.player, new Position(3016, 10249, 0));
                break;
            }
            case 1768: {
                AttackStyleDefinition.startDelayedObjectMove(this.player, new Position(3069, 3857, 0));
                break;
            }
            case 1816: {
                this.player.getPacketSender().sendSoundEffect(319, 1, 0);
                this.player.getUpdateState().setAnimation(835);
                this.player.getPacketSender().sendGameMessage("You pull the lever...");
                if (ServerSettings.cacheVersion < 319) {
                    this.player.getTeleportManager().startStandardTeleport(2717, 9802, 0, "...And teleport into the Dragon's Lair.");
                    break;
                }
                this.player.getTeleportManager().startStandardTeleport(2272, 4681, 0, "...And teleport into the Dragon's Lair.");
                break;
            }
            case 1817: {
                this.player.getPacketSender().sendSoundEffect(319, 1, 0);
                this.player.getUpdateState().setAnimation(835);
                this.player.getPacketSender().sendGameMessage("You pull the lever...");
                this.player.getTeleportManager().startStandardTeleport(3067, 10254, 0, "...And teleport out of the Dragon's Lair.");
                break;
            }
            case 5959: {
                this.player.getPacketSender().sendSoundEffect(319, 1, 0);
                this.player.getUpdateState().setAnimation(835);
                this.player.getPacketSender().sendGameMessage("You pull the lever...");
                this.player.getTeleportManager().startStandardTeleport(2539, 4712, 0, "... and teleport into the mage's cave.");
                break;
            }
            case 5960: {
                this.player.getPacketSender().sendSoundEffect(319, 1, 0);
                this.player.getUpdateState().setAnimation(835);
                this.player.getPacketSender().sendGameMessage("You pull the lever...");
                this.player.getTeleportManager().startStandardTeleport(3090, 3956, 0, "... and teleport out of the mage's cave.");
                break;
            }
            case 1814: {
                this.player.getPacketSender().sendSoundEffect(319, 1, 0);
                this.player.getUpdateState().setAnimation(835);
                this.player.getPacketSender().sendGameMessage("You pull the lever...");
                this.player.getTeleportManager().startStandardTeleport(3154, 3923, 0, "...And teleport into the wilderness.");
                break;
            }
            case 1815: {
                this.player.getPacketSender().sendSoundEffect(319, 1, 0);
                this.player.getUpdateState().setAnimation(835);
                this.player.getPacketSender().sendGameMessage("You pull the lever...");
                this.player.getTeleportManager().startStandardTeleport(2562, 3311, 0, "...And teleport out of the wilderness.");
                break;
            }
            case 194: {
                this.player.moveToGroundPosition(2409, 9819, 0);
                break;
            }
            case 3735: {
                this.player.moveToGroundPosition(2269, 4752, 0);
                break;
            }
            case 3736: {
                this.player.moveToGroundPosition(2858, 3577, 0);
                break;
            }
            case 3379: {
                this.player.moveToGroundPosition(2647, 9378, 0);
                break;
            }
            case 3381: {
                this.player.moveToGroundPosition(2630, 2997, 0);
                break;
            }
            case 9359: {
                this.player.moveToGroundPosition(2862, 9571, 0);
                break;
            }
            case 2871: {
                AttackStyleDefinition.startDelayedObjectMove(this.player, new Position(2543, 4713, 0));
                break;
            }
            case 2872: {
                AttackStyleDefinition.startDelayedObjectMove(this.player, new Position(3092, 3957, 0));
                break;
            }
            case 2880: {
                if (this.player.getPosition().getY() > 3953) {
                    if (this.player.mageArenaProgressStage < 5) break;
                    this.player.getPacketSender().queueRelativeMovementStep(0, -1, true);
                    break;
                }
                this.player.getPacketSender().queueRelativeMovementStep(0, 1, true);
                break;
            }
            case 6279: {
                AttackStyleDefinition.startDelayedObjectMove(this.player, new Position(3206, 9379, 0));
                break;
            }
            case 6439: {
                AttackStyleDefinition.startDelayedObjectMove(this.player, new Position(3311, 2963, 0));
                break;
            }
            case 3828: {
                AttackStyleDefinition.startDelayedObjectMove(this.player, new Position(3484, 9509, 2));
                break;
            }
            case 3829: {
                AttackStyleDefinition.startDelayedObjectMove(this.player, new Position(3229, 3108, 0));
                break;
            }
            case 3831: {
                AttackStyleDefinition.startDelayedObjectMove(this.player, new Position(3507, 9494, 0));
                break;
            }
            case 3832: {
                AttackStyleDefinition.startDelayedObjectMove(this.player, new Position(3509, 9499, 2));
                break;
            }
            case 10168: {
                int value3 = this.objectPlane > 1 ? 1 : 0;
                AttackStyleDefinition.startDelayedObjectMove(this.player, new Position(this.player.getPosition().getX(), this.player.getPosition().getY(), value3));
                break;
            }
            case 10167: {
                int value4 = this.objectPlane <= 0 ? 1 : 2;
                AttackStyleDefinition.startDelayedObjectMove(this.player, new Position(this.player.getPosition().getX(), this.player.getPosition().getY(), value4));
                break;
            }
            case 1765: {
                AttackStyleDefinition.startDelayedObjectMove(this.player, new Position(3069, 10255, 0));
                break;
            }
            case 1766: {
                AttackStyleDefinition.startDelayedObjectMove(this.player, new Position(3017, 3850, 0));
                break;
            }
            case 1738: {
                AttackStyleDefinition.climbOffsetLadder(this.player, "up");
                break;
            }
            case 1740: {
                AttackStyleDefinition.climbOffsetLadder(this.player, "down");
                break;
            }
            case 1739: 
            case 4569: 
            case 12537: {
                DialogueManager.startDialogue(this.player, 10007);
                break;
            }
            case 1748: 
            case 2884: 
            case 8745: 
            case 12965: {
                DialogueManager.startDialogue(this.player, 10006);
                break;
            }
            case 2882: 
            case 2883: {
                DialogueManager.startDialogue(this.player, 9999);
                break;
            }
            case 3203: {
                DialogueManager.startDialogue(this.player, 10010);
                break;
            }
            case 3192: {
                DuelHistory.openDuelHistoryInterface(this.player);
                break;
            }
            case 2213: 
            case 5276: 
            case 6084: 
            case 10517: 
            case 11338: 
            case 11758: 
            case 12759: 
            case 14367: {
                DialogueManager.startDialogue(this.player, 494);
                break;
            }
            case 2491: {
                EntityTargetMovement.startRuneEssenceMining(this.player);
                break;
            }
            case 2634: {
                this.player.getPacketSender().sendGameMessage("These rocks contain nothing interesting.");
                this.player.getPacketSender().sendGameMessage("They are just in the way.");
                break;
            }
            default: {
                this.player.getPacketSender().sendGameMessage("Nothing interesting happens.");
            }
        }
        this.stop();
    }
}
