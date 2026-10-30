package com.rs2.model.interaction;

import com.rs2.ServerSettings;
import com.rs2.cache.InterfaceDefinition;
import com.rs2.model.Entity;
import com.rs2.model.GameplayHelper;
import com.rs2.model.Position;
import com.rs2.model.combat.AttackStyleDefinition;
import com.rs2.model.gameplay.castlewars.CastleWarsEngineeringManager;
import com.rs2.model.interaction.InteractionDispatcher;
import com.rs2.model.item.ItemDefinition;
import com.rs2.model.item.ItemService;
import com.rs2.model.item.ItemStack;
import com.rs2.model.objects.DynamicObject;
import com.rs2.model.objects.ObjectDefinition;
import com.rs2.model.objects.ObjectManager;
import com.rs2.model.objects.WorldObject;
import com.rs2.model.objects.functions.FlourMillHandler;
import com.rs2.model.player.Player;
import com.rs2.model.skill.SkillActionHelper;
import com.rs2.model.skill.crafting.CraftingHandler;
import com.rs2.model.skill.crafting.JewelleryCraftingHandler;
import com.rs2.model.skill.runecrafting.RunecraftingObjectHandler;
import com.rs2.model.skill.smithing.DragonSquareShieldSmithing;
import com.rs2.model.skill.smithing.DragonfireShieldSmithing;
import com.rs2.model.skill.smithing.SmeltingHandler;
import com.rs2.model.skill.smithing.SmithingHandler;
import com.rs2.model.skill.woodcutting.JungleCutting;
import com.rs2.model.task.TickTask;
import com.rs2.util.GameplayTrace;
import com.rs2.util.GameUtil;

public final class ItemOnObjectTask
extends TickTask {
    private final Player player;
    private final int actionSequence;
    private final int objectId;
    private final int objectX;
    private final int objectY;
    private final int objectPlane;
    private final int itemId;

    public ItemOnObjectTask(int value8, boolean enabled2, Player player, int actionSequence, int objectId, int objectX, int objectY, int objectPlane, int itemId) {
        super(1, true);
        this.player = player;
        this.actionSequence = actionSequence;
        this.objectId = objectId;
        this.objectX = objectX;
        this.objectY = objectY;
        this.objectPlane = objectPlane;
        this.itemId = itemId;
    }

    @Override
    public final void execute() {
        boolean enabled;
        Object value;
        if (this.player == null || !this.player.isCurrentActionSequence(this.actionSequence)) {
            if (GameplayTrace.enabled() && this.player != null) {
                GameplayTrace.log("item-on-object task stopped stale-sequence player=" + GameplayTrace.describe(this.player) + " itemId=" + this.itemId + " objectId=" + this.objectId + " seq=" + this.actionSequence);
            }
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
            if (GameplayTrace.enabled()) {
                GameplayTrace.log("item-on-object task missing-object player=" + GameplayTrace.describe(this.player) + " itemId=" + this.itemId + " objectId=" + this.objectId + " x=" + this.objectX + " y=" + this.objectY + " plane=" + this.objectPlane);
            }
            return;
        }
        Object interactionTargetId = ObjectDefinition.forId(this.player.getInteractionTargetId());
        Object reachableInteractionPosition = this.player.getPosition();
        if (!InteractionDispatcher.canReachObjectInteraction(this.player, worldObject)) {
            if (GameplayTrace.enabled()) {
                GameplayTrace.log("item-on-object task blocked-path player=" + GameplayTrace.describe(this.player) + " itemId=" + this.itemId + " objectId=" + this.objectId + " objectX=" + this.objectX + " objectY=" + this.objectY + " reachX=" + this.player.getPosition().getX() + " reachY=" + this.player.getPosition().getY());
            }
            this.stop();
            return;
        }
        reachableInteractionPosition = new Position(this.player.getInteractionTargetX(), this.player.getInteractionTargetY(), this.objectPlane);
        if (interactionTargetId != null) {
            this.player.getUpdateState().setFacePosition(((Position)reachableInteractionPosition).centerForSize(((ObjectDefinition)interactionTargetId).getMaxDimension()));
        }
        if (GameplayTrace.enabled()) {
            GameplayTrace.log("item-on-object task reached player=" + GameplayTrace.describe(this.player) + " itemId=" + this.itemId + " item=" + ItemDefinition.forId(this.itemId).getName() + " objectId=" + this.objectId + " object=" + ((ObjectDefinition)interactionTargetId).name + " x=" + this.objectX + " y=" + this.objectY + " plane=" + this.objectPlane);
        }
        if (com.rs2.model.quest.event.GublinchChristmasEvent.useShardsOnCage(
                this.player, this.itemId, this.objectId, this.objectX, this.objectY, this.objectPlane)) {
            this.stop();
            return;
        }
        if (this.player.getQuestManager().handleItemOnObject(this.itemId, this.objectId)) {
            this.stop();
            return;
        }
        if (JungleCutting.handleItemOnObject(this.player, this.itemId, this.objectId,
                this.objectX, this.objectY, this.objectPlane)) {
            this.stop();
            return;
        }
        if (CastleWarsEngineeringManager.handleItemOnObject(
                this.player, this.itemId, this.objectId,
                this.objectX, this.objectY, this.objectPlane)) {
            this.stop();
            return;
        }
        if (this.objectId == 2693 || this.objectId == 2995 || this.objectId == 4483 || this.objectId == 3194 || this.objectId == 12121 || this.objectId == 2213 || this.objectId == 6084 || this.objectId == 5276 || this.objectId == 11338 || this.objectId == 14367 || this.objectId == 10517 || this.objectId == 11758) {
            int inventoryManager = this.player.getInventoryManager().getItemAmount(this.itemId);
            reachableInteractionPosition = new ItemStack(this.itemId, inventoryManager);
            value = ((ItemStack)reachableInteractionPosition).getDefinition();
            if (((ItemDefinition)value).hasNote()) {
                int notedId = ((ItemDefinition)value).getNotedId();
                this.player.getInventoryManager().removeItem((ItemStack)reachableInteractionPosition);
                this.player.getInventoryManager().addItem(new ItemStack(notedId, inventoryManager));
                this.stop();
                return;
            }
            if (((ItemDefinition)value).isNote()) {
                if (inventoryManager > 1) {
                    this.player.temporaryActionValue = this.itemId;
                    value = this.player;
                    ((Player)value).packetSender.sendEnterInputPrompt(18902);
                } else if (inventoryManager == 1) {
                    int unnotedId = ((ItemDefinition)value).getUnnotedId();
                    this.player.getInventoryManager().removeItem((ItemStack)reachableInteractionPosition);
                    this.player.getInventoryManager().addItem(new ItemStack(unnotedId, inventoryManager));
                }
                this.stop();
                return;
            }
        }
        if (ServerSettings.content2007Enabled) {
            if (DragonfireShieldSmithing.handleItemOnAnvil(this.player, this.itemId, this.objectId)) {
                this.stop();
                return;
            }
        }
        if (this.objectId == 2644 && CraftingHandler.startSpinningAtWheel(this.player, this.itemId)) {
            this.stop();
            return;
        }
        if (DragonSquareShieldSmithing.handleItemOnAnvil(this.player, this.itemId, this.objectId)) {
            this.stop();
            return;
        }
        if (this.player.getCookingManager().handleItemOnCookingObject(this.itemId, this.objectId, this.objectX, this.objectY)) {
            if (GameplayTrace.enabled()) {
                GameplayTrace.log("item-on-object cooking-handled player=" + GameplayTrace.describe(this.player) + " itemId=" + this.itemId + " objectId=" + this.objectId + " x=" + this.objectX + " y=" + this.objectY + " plane=" + this.objectPlane);
            }
            this.stop();
            return;
        }
        if (this.player.getCookingManager().handleWaterSourceItem(this.itemId, this.objectId)) {
            if (GameplayTrace.enabled()) {
                GameplayTrace.log("item-on-object water-source-handled player=" + GameplayTrace.describe(this.player) + " itemId=" + this.itemId + " item=" + ItemDefinition.forId(this.itemId).getName() + " objectId=" + this.objectId + " x=" + this.objectX + " y=" + this.objectY + " plane=" + this.objectPlane);
            }
            this.player.getInventoryManager().refresh();
            this.stop();
            return;
        }
        if (this.objectId == 5947 && this.itemId == 954) {
            if (!this.player.swampCaveRopeAttached) {
                this.player.getInventoryManager().removeItem(new ItemStack(this.itemId));
                this.player.swampCaveRopeAttached = true;
                value = this.player;
                ((Player)value).packetSender.sendGameMessage("You attach the rope.");
            } else {
                value = this.player;
                ((Player)value).packetSender.sendGameMessage("You have already put a rope there!");
            }
            this.stop();
            return;
        }
        if (this.objectId == 5908 && this.itemId == 1939) {
            if (!this.player.lampOilStillFilled) {
                this.player.getInventoryManager().removeItem(new ItemStack(this.itemId));
                this.player.lampOilStillFilled = true;
                value = this.player;
                ((Player)value).packetSender.sendGameMessage("You refine some swamp tar into lamp oil.");
            } else {
                value = this.player;
                ((Player)value).packetSender.sendGameMessage("You need to take out the oil first before making new oil!");
            }
            this.stop();
            return;
        }
        if (this.objectId == 5908 && (this.itemId == 4525 || this.itemId == 4535 || this.itemId == 4546 || this.itemId == 4700)) {
            if (this.player.lampOilStillFilled) {
                this.player.getInventoryManager().removeItem(new ItemStack(this.itemId));
                this.player.lampOilStillFilled = false;
                int value2 = 4522;
                reachableInteractionPosition = "lamp";
                if (this.itemId == 4535) {
                    reachableInteractionPosition = "lantern";
                    value2 = 4537;
                } else if (this.itemId == 4546) {
                    reachableInteractionPosition = "lantern";
                    value2 = 4548;
                } else if (this.itemId == 4700) {
                    reachableInteractionPosition = "lantern";
                    value2 = 4701;
                }
                this.player.getInventoryManager().addItem(new ItemStack(value2));
                value = this.player;
                ((Player)value).packetSender.sendGameMessage("You put some oil in the " + (String)reachableInteractionPosition + ".");
            } else {
                value = this.player;
                ((Player)value).packetSender.sendGameMessage("You need to make oil first!");
            }
            this.stop();
            return;
        }
        if (this.objectId == 1781 && this.itemId == 1931) {
            FlourMillHandler.collectFlourFromBin(this.player);
            this.stop();
            return;
        }
        if ((this.objectId == 14921 || this.objectId == 11666 || this.objectId == 9390 || this.objectId == 2781 || this.objectId == 3044) && this.itemId == 446) {
            SmeltingHandler.handleOreOnFurnace(this.player, this.itemId);
            this.stop();
            return;
        }
        if (this.objectId == 3044 && this.player.getQuestState(0) != 1 && (this.itemId == 438 || this.itemId == 436)) {
            SmeltingHandler.handleOreOnFurnace(this.player, this.itemId);
            this.stop();
            return;
        }
        if (this.objectId == 2783 && this.player.getQuestState(0) == 37 && this.itemId == 2349) {
            value = this.player;
            ((Player)value).packetSender.sendEntityHintIcon(1, -1);
            // Refresh stage 38 before opening the smithing menu so the tutorial
            // overlay cannot replace the active-interface tracking for the menu.
            this.player.advanceTutorialStage();
            SmithingHandler.openSmithingInterface(this.player, 2349);
            this.stop();
            return;
        }
        int value3 = this.objectY;
        int value4 = this.objectX;
        int value5 = this.objectId;
        int value6 = this.itemId;
        interactionTargetId = this.player;
        if (((Player)interactionTargetId).getPlantPotHandler().fillPlantPotWithSoil(value6, value4, value3)) {
            enabled = true;
        } else if (((Player)interactionTargetId).getAllotmentPatchManager().curePatch(value4, value3, value6)) {
            enabled = true;
        } else if (((Player)interactionTargetId).getAllotmentPatchManager().compostPatch(value4, value3, value6)) {
            enabled = true;
        } else if (((Player)interactionTargetId).getAllotmentPatchManager().clearPatch(value4, value3, value6)) {
            enabled = true;
        } else if (value6 >= 3422 && value6 <= 3428 && value5 == 4090) {
            ((Player)interactionTargetId).getInventoryManager().removeItem(new ItemStack(value6));
            ((Player)interactionTargetId).getInventoryManager().addItem(new ItemStack(value6 + 8));
            ((Entity)interactionTargetId).getUpdateState().setAnimation(832);
            Object value7 = interactionTargetId;
            ((Player)value7).packetSender.sendGameMessage("You put the olive oil on the fire, and turn it into sacred oil.");
            enabled = true;
        } else {
            enabled = value6 <= 5340 && value6 > 5332 && ((Player)interactionTargetId).getAllotmentPatchManager().waterPatch(value4, value3, value6) ? true : (((Player)interactionTargetId).getAllotmentPatchManager().plantSeed(value4, value3, value6) ? true : (((Player)interactionTargetId).getFlowerPatchManager().plantScarecrow(value4, value3, value6) ? true : (((Player)interactionTargetId).getFlowerPatchManager().curePatch(value4, value3, value6) ? true : (((Player)interactionTargetId).getFlowerPatchManager().compostPatch(value4, value3, value6) ? true : (((Player)interactionTargetId).getFlowerPatchManager().clearPatch(value4, value3, value6) ? true : (value6 <= 5340 && value6 > 5332 && ((Player)interactionTargetId).getFlowerPatchManager().waterPatch(value4, value3, value6) ? true : (((Player)interactionTargetId).getFlowerPatchManager().plantSeed(value4, value3, value6) ? true : (((Player)interactionTargetId).getCompostBinManager().fillBin(value6, value5, value4, value3) ? true : (((Player)interactionTargetId).getHerbPatchManager().curePatch(value4, value3, value6) ? true : (((Player)interactionTargetId).getHerbPatchManager().compostPatch(value4, value3, value6) ? true : (((Player)interactionTargetId).getHerbPatchManager().clearPatch(value4, value3, value6) ? true : (((Player)interactionTargetId).getHerbPatchManager().plantSeed(value4, value3, value6) ? true : (((Player)interactionTargetId).getHopsPatchManager().curePatch(value4, value3, value6) ? true : (((Player)interactionTargetId).getHopsPatchManager().compostPatch(value4, value3, value6) ? true : (((Player)interactionTargetId).getHopsPatchManager().clearPatch(value4, value3, value6) ? true : (value6 <= 5340 && value6 > 5332 && ((Player)interactionTargetId).getHopsPatchManager().waterPatch(value4, value3, value6) ? true : (((Player)interactionTargetId).getHopsPatchManager().plantSeed(value4, value3, value6) ? true : (((Player)interactionTargetId).getBushPatchManager().curePatch(value4, value3, value6) ? true : (((Player)interactionTargetId).getBushPatchManager().compostPatch(value4, value3, value6) ? true : (((Player)interactionTargetId).getBushPatchManager().clearPatch(value4, value3, value6) ? true : (((Player)interactionTargetId).getBushPatchManager().plantSeed(value4, value3, value6) ? true : (((Player)interactionTargetId).getTreePatchManager().prunePatch(value4, value3, value6) ? true : (((Player)interactionTargetId).getTreePatchManager().compostPatch(value4, value3, value6) ? true : (((Player)interactionTargetId).getTreePatchManager().plantSapling(value4, value3, value6) ? true : (((Player)interactionTargetId).getTreePatchManager().clearPatch(value4, value3, value6) ? true : (((Player)interactionTargetId).getFruitTreePatchManager().prunePatch(value4, value3, value6) ? true : (((Player)interactionTargetId).getFruitTreePatchManager().compostPatch(value4, value3, value6) ? true : (((Player)interactionTargetId).getFruitTreePatchManager().clearPatch(value4, value3, value6) ? true : (((Player)interactionTargetId).getFruitTreePatchManager().plantSapling(value4, value3, value6) ? true : (((Player)interactionTargetId).getSpecialTreePatchManager().curePatch(value4, value3, value6) ? true : (((Player)interactionTargetId).getSpecialTreePatchManager().compostPatch(value4, value3, value6) ? true : (((Player)interactionTargetId).getSpecialTreePatchManager().clearPatch(value4, value3, value6) ? true : (((Player)interactionTargetId).getSpecialTreePatchManager().plantSapling(value4, value3, value6) ? true : (((Player)interactionTargetId).getSpecialCropPatchManager().curePatch(value4, value3, value6) ? true : (((Player)interactionTargetId).getSpecialCropPatchManager().compostPatch(value4, value3, value6) ? true : (((Player)interactionTargetId).getSpecialCropPatchManager().clearPatch(value4, value3, value6) ? true : ((Player)interactionTargetId).getSpecialCropPatchManager().plantSeed(value4, value3, value6)))))))))))))))))))))))))))))))))))));
        }
        if (enabled) {
            this.stop();
            return;
        }
        if (RunecraftingObjectHandler.handleTalismanOnMysteriousRuins(this.player, this.itemId, this.objectId)) {
            this.stop();
            return;
        }
        if (GameplayHelper.handleTiaraCrafting(this.player, this.itemId, this.objectId)) {
            this.stop();
            return;
        }
        if (GameplayHelper.handleCombinationRunecrafting(this.player, this.itemId, this.objectId)) {
            this.stop();
            return;
        }
        if (this.itemId >= 3422 && this.itemId <= 3428 && this.objectId == 4090) {
            this.player.getInventoryManager().removeItem(new ItemStack(this.itemId));
            this.player.getInventoryManager().addItem(new ItemStack(this.itemId + 8));
            this.player.getUpdateState().setAnimation(832);
            Player player = this.player;
            player.packetSender.sendGameMessage("You put the olive oil on the fire, and turn it into sacred oil.");
            this.stop();
            return;
        }
        switch (this.objectId) {
            case 2114: {
                if (this.itemId != 453) break;
                interactionTargetId = this.player;
                value6 = 120 - ((Player)interactionTargetId).getCoalTruckCoalCount();
                if (value6 == 0) {
                    Object value8 = interactionTargetId;
                    ((Player)value8).packetSender.sendGameMessage("The coal truck is already full.");
                    break;
                }
                int inventoryManager2 = ((Player)interactionTargetId).getInventoryManager().getItemAmount(453);
                if (inventoryManager2 == 0) break;
                int value9 = value4 = value6 < inventoryManager2 ? value6 : inventoryManager2;
                if (!((Player)interactionTargetId).getInventoryManager().removeItem(new ItemStack(453, value4))) break;
                ((Player)interactionTargetId).setCoalTruckCoalCount(((Player)interactionTargetId).getCoalTruckCoalCount() + value4);
                break;
            }
            case 172: {
                if (this.itemId != 989 || !((Player)(interactionTargetId = this.player)).getInventoryManager().removeItem(new ItemStack(989))) break;
                ((Entity)interactionTargetId).getUpdateState().setAnimation(832);
                Object value10 = interactionTargetId;
                ((Player)value10).packetSender.sendGameMessage("You unlock the chest with your key.");
                new DynamicObject(173, 2914, 3452, 0, 2, 10, 172, 2);
                ((Player)interactionTargetId).getInventoryManager().addOrDropItem(new ItemStack(1631));
                String[] stringValues = new String[]{"1000/3765", "100/1067", "100/1067", "100/1067", "10/128", "10/128", "10/128", "1/16", "1/64", "1/128", "1000/7529"};
                int value11 = GameUtil.rollFractionWeightIndex(stringValues);
                if (value11 == 0) {
                    ((Player)interactionTargetId).getInventoryManager().addOrDropItem(new ItemStack(995, 2000));
                    ((Player)interactionTargetId).getInventoryManager().addOrDropItem(new ItemStack(1969));
                    break;
                }
                if (value11 == 1) {
                    ((Player)interactionTargetId).getInventoryManager().addOrDropItem(new ItemStack(554, 50));
                    ((Player)interactionTargetId).getInventoryManager().addOrDropItem(new ItemStack(555, 50));
                    ((Player)interactionTargetId).getInventoryManager().addOrDropItem(new ItemStack(556, 50));
                    ((Player)interactionTargetId).getInventoryManager().addOrDropItem(new ItemStack(557, 50));
                    ((Player)interactionTargetId).getInventoryManager().addOrDropItem(new ItemStack(558, 50));
                    ((Player)interactionTargetId).getInventoryManager().addOrDropItem(new ItemStack(559, 50));
                    ((Player)interactionTargetId).getInventoryManager().addOrDropItem(new ItemStack(560, 10));
                    ((Player)interactionTargetId).getInventoryManager().addOrDropItem(new ItemStack(561, 10));
                    ((Player)interactionTargetId).getInventoryManager().addOrDropItem(new ItemStack(562, 10));
                    ((Player)interactionTargetId).getInventoryManager().addOrDropItem(new ItemStack(563, 10));
                    ((Player)interactionTargetId).getInventoryManager().addOrDropItem(new ItemStack(564, 10));
                    break;
                }
                if (value11 == 2) {
                    ((Player)interactionTargetId).getInventoryManager().addOrDropItem(new ItemStack(1617, 2));
                    ((Player)interactionTargetId).getInventoryManager().addOrDropItem(new ItemStack(1619, 2));
                    break;
                }
                if (value11 == 3) {
                    ((Player)interactionTargetId).getInventoryManager().addOrDropItem(new ItemStack(2363, 3));
                    break;
                }
                if (value11 == 4) {
                    ((Player)interactionTargetId).getInventoryManager().addOrDropItem(new ItemStack(995, 750));
                    ((Player)interactionTargetId).getInventoryManager().addOrDropItem(new ItemStack(GameUtil.randomInclusive(1) == 0 ? 985 : 987));
                    break;
                }
                if (value11 == 5) {
                    ((Player)interactionTargetId).getInventoryManager().addOrDropItem(new ItemStack(441, 150));
                    break;
                }
                if (value11 == 6) {
                    ((Player)interactionTargetId).getInventoryManager().addOrDropItem(new ItemStack(454, 100));
                    break;
                }
                if (value11 == 7) {
                    ((Player)interactionTargetId).getInventoryManager().addOrDropItem(new ItemStack(995, 1000));
                    ((Player)interactionTargetId).getInventoryManager().addOrDropItem(new ItemStack(371, 5));
                    break;
                }
                if (value11 == 8) {
                    ((Player)interactionTargetId).getInventoryManager().addOrDropItem(new ItemStack(1183));
                    break;
                }
                if (value11 != 9) break;
                ((Player)interactionTargetId).getInventoryManager().addOrDropItem(new ItemStack(((Player)interactionTargetId).getGender() == 0 ? 1079 : 1093));
                break;
            }
            case 3827: {
                if (this.itemId != 954) break;
                this.player.getInventoryManager().removeItem(new ItemStack(954, 1));
                ObjectManager.getInstance().removeDynamicObjectAt(this.objectX, this.objectY, this.objectPlane, 0);
                new DynamicObject(worldObject.getObjectId() + 1, this.objectX, this.objectY, this.objectPlane, worldObject.getOrientation(), worldObject.getType(), worldObject.getObjectId(), 30);
                break;
            }
            case 3830: {
                if (this.itemId != 954) break;
                this.player.getInventoryManager().removeItem(new ItemStack(954, 1));
                ObjectManager.getInstance().removeDynamicObjectAt(this.objectX, this.objectY, this.objectPlane, 0);
                new DynamicObject(worldObject.getObjectId() + 1, this.objectX, this.objectY, this.objectPlane, worldObject.getOrientation(), worldObject.getType(), worldObject.getObjectId(), 30);
                break;
            }
            case 170: {
                if (this.itemId != 991 || !((Player)(interactionTargetId = this.player)).getInventoryManager().removeItem(new ItemStack(991))) break;
                ((Entity)interactionTargetId).getUpdateState().setAnimation(832);
                Object value12 = interactionTargetId;
                ((Player)value12).packetSender.sendGameMessage("You unlock the chest with your key.");
                new DynamicObject(171, 3089, 3859, 0, 1, 10, 170, 2);
                ((Player)interactionTargetId).getInventoryManager().addOrDropItem(new ItemStack(1619, 1));
                ((Player)interactionTargetId).getInventoryManager().addOrDropItem(new ItemStack(2359, 1));
                ((Player)interactionTargetId).getInventoryManager().addOrDropItem(new ItemStack(1209, 1));
                ((Player)interactionTargetId).getInventoryManager().addOrDropItem(new ItemStack(2297, 1));
                ((Player)interactionTargetId).getInventoryManager().addOrDropItem(new ItemStack(563, 2));
                ((Player)interactionTargetId).getInventoryManager().addOrDropItem(new ItemStack(560, 2));
                ((Player)interactionTargetId).getInventoryManager().addOrDropItem(new ItemStack(562, 10));
                ((Player)interactionTargetId).getInventoryManager().addOrDropItem(new ItemStack(995, 50));
                break;
            }
            case 733: {
                AttackStyleDefinition.slashWeb(this.player, this.objectX, this.objectY, this.itemId);
                break;
            }
            case 2782: 
            case 2783: {
                if (GameplayTrace.enabled()) {
                    GameplayTrace.log("item-on-object smithing-interface-open player=" + GameplayTrace.describe(this.player) + " itemId=" + this.itemId + " item=" + ItemDefinition.forId(this.itemId).getName() + " objectId=" + this.objectId + " x=" + this.objectX + " y=" + this.objectY + " plane=" + this.objectPlane);
                }
                SmithingHandler.openSmithingInterface(this.player, this.itemId);
                break;
            }
            case 2714: 
            case 2715: 
            case 2716: 
            case 2717: {
                FlourMillHandler.addGrainToHopper(this.player);
                break;
            }
            case 2638: {
                if (this.itemId != 1704 && this.itemId != 1706 && this.itemId != 1708 && this.itemId != 1710) break;
                Player player = this.player;
                player.packetSender.sendGameMessage("You dip your amulet into the fountain...");
                this.player.getUpdateState().setAnimation(827, 0);
                int index = 0;
                while (index < 28) {
                    int[] integerValues = new int[]{1704, 1706, 1708, 1710};
                    integerValues = integerValues;
                    int index2 = 0;
                    while (index2 < 4) {
                        int value13 = integerValues[index2];
                        if (this.player.getInventoryManager().getContainer().containsItem(value13)) {
                            this.player.getInventoryManager().setItemInSlot(new ItemStack(1712, 1), this.player.getInventoryManager().getContainer().indexOfItem(value13));
                        }
                        ++index2;
                    }
                    ++index;
                }
                this.player.getDialogueManager().showThreeLineItemMessage("You feel a power emanating from the fountain as it", "recharges your amulet. You can now rub the amulet to", "teleport and wear it to get more gems whilst mining.", new ItemStack(this.itemId, 1));
                this.player.getDialogueManager().finishDialogue();
                break;
            }
            case 2645: 
            case 10814: {
                if (this.itemId != 1925) break;
                interactionTargetId = this.player;
                if (!ServerSettings.craftingEnabled) {
                    Object value14 = interactionTargetId;
                    ((Player)value14).packetSender.sendGameMessage("This skill is currently disabled.");
                    break;
                }
                if (!((Player)interactionTargetId).isMember()) {
                    ((Player)interactionTargetId).packetSender.sendGameMessage("You need a members account to access members content.");
                    break;
                }
                if (ServerSettings.freeToPlayWorld) {
                    ((Player)interactionTargetId).packetSender.sendGameMessage("You need to be in members world to access members content.");
                    break;
                }
                if (!((Player)interactionTargetId).getInventoryManager().getContainer().containsItem(1925)) {
                    ((Player)interactionTargetId).getDialogueManager().showOneLineStatement("You need a wooden bucket to do that.");
                    break;
                }
                ((Entity)interactionTargetId).getUpdateState().setAnimation(895);
                Object value15 = interactionTargetId;
                ((Player)value15).packetSender.sendGameMessage("You fill your bucket with sand.");
                ((Player)interactionTargetId).getInventoryManager().removeItem(new ItemStack(1925));
                ((Player)interactionTargetId).getInventoryManager().addItem(new ItemStack(1783));
                break;
            }
            case 2781: 
            case 2785: 
            case 2966: 
            case 3044: 
            case 3294: 
            case 4304: 
            case 4305: 
            case 6189: 
            case 6190: 
            case 9390: 
            case 11009: 
            case 11010: 
            case 11666: 
            case 12100: 
            case 12809: 
            case 14921: {
                if (this.itemId == 1783) {
                    interactionTargetId = this.player;
                    if (!ServerSettings.craftingEnabled) {
                        Object value16 = interactionTargetId;
                        ((Player)value16).packetSender.sendGameMessage("This skill is currently disabled.");
                        break;
                    }
                    if (!((Player)interactionTargetId).isMember()) {
                        ((Player)interactionTargetId).packetSender.sendGameMessage("You need a members account to access members content.");
                        break;
                    }
                    if (ServerSettings.freeToPlayWorld) {
                        ((Player)interactionTargetId).packetSender.sendGameMessage("You need to be in members world to access members content.");
                        break;
                    }
                    if (!((Player)interactionTargetId).getInventoryManager().getContainer().containsItem(1783)) {
                        ((Player)interactionTargetId).getDialogueManager().showOneLineStatement("You need a bucket of sand to do that.");
                        break;
                    }
                    if (!((Player)interactionTargetId).getInventoryManager().getContainer().containsItem(1781)) {
                        ((Player)interactionTargetId).getDialogueManager().showOneLineStatement("You need soda ash to do that.");
                        break;
                    }
                    ((Entity)interactionTargetId).getUpdateState().setAnimation(899);
                    ((Player)interactionTargetId).getSkillManager().addExperience(12, 20.0);
                    Object value17 = interactionTargetId;
                    ((Player)value17).packetSender.sendGameMessage("You heat the sand and soda ash in the furnace to make glass.");
                    ((Player)interactionTargetId).getInventoryManager().removeItem(new ItemStack(1783));
                    ((Player)interactionTargetId).getInventoryManager().removeItem(new ItemStack(1781));
                    ((Player)interactionTargetId).getInventoryManager().addItem(new ItemStack(1925));
                    ((Player)interactionTargetId).getInventoryManager().addItem(new ItemStack(1775));
                    break;
                }
                if (this.itemId == 2357 || this.itemId == 2365) {
                    JewelleryCraftingHandler.openJewelleryCraftingInterface(this.player, this.itemId);
                    break;
                }
                if (this.itemId == 2355) {
                    GameplayHelper.openProductionInterface(this.player, "silverCrafting");
                    break;
                }
                ItemService.getInstance();
                if (!ItemService.getItemName(this.itemId).toLowerCase().endsWith("ore")) {
                    ItemService.getInstance();
                    if (!ItemService.getItemName(this.itemId).toLowerCase().equals("coal")) break;
                }
                SmeltingHandler.handleOreOnFurnace(this.player, this.itemId);
                break;
            }
            case 2642: {
                if (this.itemId != 1761) break;
                GameplayHelper.openProductionInterface(this.player, "potteryUnfired");
                break;
            }
            case 2643: 
            case 11601: {
                if (InterfaceDefinition.interfaceCount <= 8888) {
                    CraftingHandler.startPotteryFiring(this.player, this.itemId);
                    break;
                }
                GameplayHelper.openProductionInterface(this.player, "potteryFired");
                break;
            }
            default: {
                if (GameplayTrace.enabled()) {
                    GameplayTrace.log("item-on-object unhandled player=" + GameplayTrace.describe(this.player) + " itemId=" + this.itemId + " objectId=" + this.objectId + " x=" + this.objectX + " y=" + this.objectY + " plane=" + this.objectPlane);
                }
                Player player = this.player;
                player.packetSender.sendGameMessage("Nothing interesting happens.");
            }
        }
        this.stop();
    }
}

