package com.rs2.model.player;

import com.rs2.bot.combat.BotCombatHelper;
import com.rs2.ServerSettings;
import com.rs2.model.EntityTargetMovement;
import com.rs2.model.GameplayHelper;
import com.rs2.model.combat.WeaponProfile;
import com.rs2.model.combat.special.SpecialAttackDefinition;
import com.rs2.model.gameplay.castlewars.CastleWarsManager;
import com.rs2.model.gameplay.duel.DuelRule;
import com.rs2.model.item.ItemContainer;
import com.rs2.model.item.ItemContainerType;
import com.rs2.model.item.ItemService;
import com.rs2.model.item.ItemStack;
import com.rs2.model.item.ItemDefinition;
import com.rs2.model.player.EquipmentContainer;
import com.rs2.model.player.Player;
import com.rs2.model.quest.QuestDefinition;
import com.rs2.model.skill.SkillManager;
import com.rs2.util.GameplayTrace;
import com.rs2.util.GameUtil;

public final class EquipmentManager {
    private Player player;
    private ItemContainer container = new EquipmentContainer(this, ItemContainerType.a, 14);
    private int[] equipSoundIds = new int[]{1342, 1343, 1344, 1346};

    public EquipmentManager(Player player) {
        this.player = player;
    }

    public final void refresh() {
        ItemStack[] rawItems = this.container.getRawItems();
        Player player = this.player;
        player.packetSender.sendItemContainer(1688, rawItems);
        if (ServerSettings.clientBuild == 443 && player.getOpenInterfaceId() == 15106) {
            player.packetSender.sendItemContainer(15107, rawItems);
        }
        ItemStack itemStack = rawItems[3];
        if (itemStack != null && (ItemDefinition.isRemovedContent(itemStack.getId())
                || itemStack.getDefinition().isMembersOnly() && ServerSettings.freeToPlayWorld)) {
            itemStack = null;
        }
        this.player.setWeaponProfile(WeaponProfile.forItem((ItemStack)itemStack));
        this.player.getEquipmentManager().refreshWeaponAmmunitionState();
        if (!ServerSettings.freeToPlayWorld) {
            this.player.getEquipmentManager().refreshBarrowsSetEffects();
        }
        this.refreshEquipmentBonuses(this.player);
        this.refreshWeaponInterface();
    }

    public final boolean removeItemWithoutRefresh(ItemStack itemStack) {
        if (itemStack == null || !itemStack.isValid()) {
            return false;
        }
        this.container.remove(itemStack);
        if (itemStack.getId() == 6583 || itemStack.getId() == 7927) {
            this.player.npcTransformationId = -1;
            Player player = this.player;
            player.packetSender.refreshSidebarInterfaces();
            this.player.setAppearanceUpdateRequired(true);
        }
        if (itemStack.getId() == 4024) {
            this.player.getUpdateState().setGraphic(160, 0);
            this.player.npcTransformationId = -1;
            this.player.setAppearanceUpdateRequired(true);
        }
        return true;
    }

    public final boolean removeItem(ItemStack itemStack) {
        if (!itemStack.isValid()) {
            return false;
        }
        EquipmentManager equipmentManager = this;
        ItemStack itemStack2 = itemStack;
        EquipmentManager equipmentManager2 = equipmentManager;
        int amount = itemStack2.getAmount();
        int id = itemStack2.getId();
        equipmentManager2 = equipmentManager;
        if (!(equipmentManager.containsItem(id) && equipmentManager2.container.getItemAmount(id) >= amount)) {
            return false;
        }
        this.container.remove(itemStack);
        this.refresh();
        this.player.getEquipmentManager().refreshCarriedValue();
        return true;
    }

    public final boolean containsItem(int itemId) {
        EquipmentManager equipmentManager = this;
        return equipmentManager.container.indexOfItem(itemId) >= 0;
    }

    public final void finishBulkEquipmentRemoval() {
        this.refresh();
        GameplayHelper.refreshRunecraftingTiaraConfig(this.player, -1);
        this.player.nextActionSequence();
        this.player.setWeaponProfile(null);
        this.player.setSpecialAttackDefinition(null);
        this.player.setAutocastSpell(null);
        this.refreshCarriedValue();
        GameplayHelper.refreshRubberChickenPlayerOption(this.player);
        this.player.setAppearanceUpdateRequired(true);
    }

    public final void replaceSlotItem(int itemId, int value2) {
        ItemStack itemStack = new ItemStack(this.player.getEquipmentManager().getItemIdAtSlot(value2));
        ItemStack itemStack2 = new ItemStack(itemId);
        this.container.removeFromSlot(itemStack, value2);
        this.container.setItem(value2, itemStack2);
        this.refresh();
        this.player.setSpecialAttackEnabled(false);
        this.refreshWeaponInterface();
        EntityTargetMovement.clearMovementTarget(this.player);
        this.player.getInventoryManager().refresh();
        this.player.setAppearanceUpdateRequired(true);
        this.player.getAttributes().put("usedGlory", Boolean.FALSE);
    }

    public final void setSlotItem(int itemId, int value2) {
        ItemStack itemStack = new ItemStack(itemId);
        this.container.setItem(value2, itemStack);
        this.refresh();
        this.player.setSpecialAttackEnabled(false);
        this.refreshWeaponInterface();
        EntityTargetMovement.clearMovementTarget(this.player);
        this.player.getInventoryManager().refresh();
        this.player.setAppearanceUpdateRequired(true);
    }

    public final void equipFromInventorySlot(int slot) {
        boolean equipmentStatsOpen = ServerSettings.clientBuild == 443
                && this.player.getOpenInterfaceId() == 15106;
        int value;
        int value2;
        ItemStack itemStack;
        boolean dropCastleWarsFlagForEquipment = false;
        equipFromInventorySlotControlExit1: {
            equipFromInventorySlotControlExit2: {
                equipFromInventorySlotControlExit3: {
                    Object value3;
                    equipFromInventorySlotControlExit4: {
                        boolean enabled;
                        String requirementBlockReason = null;
                        equipFromInventorySlotControlExit5: {
                            itemStack = this.player.getInventoryManager().getContainer().getItemAt(slot);
                            if (itemStack == null) {
                                if (GameplayTrace.enabled()) {
                                    GameplayTrace.log("equipment equip missing-item player=" + GameplayTrace.describe(this.player) + " inventorySlot=" + slot);
                                }
                                return;
                            }
                            if (ItemDefinition.isRemovedContent(itemStack.getId())) {
                                this.player.packetSender.sendGameMessage("This item has been removed from the game.");
                                return;
                            }
                            value2 = itemStack.getDefinition().getEquipmentSlot();
                            if (GameplayTrace.enabled()) {
                                GameplayTrace.log("equipment equip request player=" + GameplayTrace.describe(this.player) + " inventorySlot=" + slot + " itemId=" + itemStack.getId() + " item=" + itemStack.getDefinition().getName() + " equipmentSlot=" + value2);
                            }
                            dropCastleWarsFlagForEquipment =
                                    CastleWarsManager.isCarryingFlag(this.player)
                                            && (value2 == 3 || value2 == 5);
                            if ((CastleWarsManager.isWaitingPlayer(this.player) || CastleWarsManager.isInGame(this.player))
                                    && CastleWarsManager.isTeamColourEquipmentSlot(value2)) {
                                this.player.getPacketSender().sendGameMessage("You can't change your head or cape equipment during Castle Wars.");
                                return;
                            }
                            if (!this.player.getInventoryManager().containsItemStack(itemStack)) {
                                if (GameplayTrace.enabled()) {
                                    GameplayTrace.log("equipment equip missing-stack player=" + GameplayTrace.describe(this.player) + " inventorySlot=" + slot + " itemId=" + itemStack.getId());
                                }
                                return;
                            }
                            int value4 = value2;
                            value = itemStack.getId();
                            value3 = this;
                            if (new ItemStack(value).getDefinition().isMembersOnly() && !((EquipmentManager)value3).player.isMember()) {
                                ((EquipmentManager)value3).player.packetSender.sendGameMessage("You need a members account to access members content.");
                                requirementBlockReason = "members-account";
                                enabled = false;
                            } else if (new ItemStack(value).getDefinition().isMembersOnly() && ServerSettings.freeToPlayWorld) {
                                ((EquipmentManager)value3).player.packetSender.sendGameMessage("You need to be in members world to access members content.");
                                requirementBlockReason = "members-world";
                                enabled = false;
                            } else {
                                char character;
                                int value5;
                                int value6;
                                String text = "wear this item.";
                                if (value4 == 3) {
                                    text = "wield this weapon.";
                                } else if (value4 == 13) {
                                    text = "equip this ammo.";
                                }
                                value4 = 0;
                                while (value4 < 22) {
                                    value6 = ((EquipmentManager)value3).player.getSkillManager().getBaseLevel(value4);
                                    if (value6 < (value5 = new ItemStack(value).getDefinition().getRequiredLevel(value4))) {
                                        String text2 = "a";
                                        character = SkillManager.SKILL_NAMES[value4].charAt(0);
                                        if (character == 'A' || character == 'E' || character == 'I' || character == 'O' || character == 'U') {
                                            text2 = "an";
                                        }
                                        requirementBlockReason = "skill:" + SkillManager.SKILL_NAMES[value4] + ":required=" + value5 + ":base=" + value6;
                                        value3 = ((EquipmentManager)value3).player;
                                        ((Player)value3).packetSender.sendGameMessage("You need " + text2 + " " + SkillManager.SKILL_NAMES[value4] + " level of " + value5 + " to " + text);
                                        enabled = false;
                                        break equipFromInventorySlotControlExit5;
                                    }
                                    ++value4;
                                }
                                value4 = ((EquipmentManager)value3).player.getQuestPoints();
                                if (value4 < (value6 = new ItemStack(value).getDefinition().getRequiredQuestPoints())) {
                                    requirementBlockReason = "quest-points:required=" + value6 + ":current=" + value4;
                                    value3 = ((EquipmentManager)value3).player;
                                    ((Player)value3).packetSender.sendGameMessage("You need " + value6 + " quest points to " + text);
                                    enabled = false;
                                } else {
                                    value5 = 0;
                                    while (value5 < QuestDefinition.questCount) {
                                        value6 = new ItemStack(value).getDefinition().requiresQuest(value5) ? 1 : 0;
                                        char questState = character = ((EquipmentManager)value3).player.getQuestState(value5) == 1 ? (char)'\u0001' : '\u0000';
                                        if (value6 != 0 && character == '\u0000') {
                                            Object value7 = QuestDefinition.forId(value5);
                                            value7 = ((QuestDefinition)value7).getName();
                                            requirementBlockReason = "quest:" + value5 + ":" + (String)value7;
                                            value3 = ((EquipmentManager)value3).player;
                                            ((Player)value3).packetSender.sendGameMessage("You need to complete " + (String)value7 + " to " + text);
                                            enabled = false;
                                            break equipFromInventorySlotControlExit5;
                                        }
                                        ++value5;
                                    }
                                    enabled = true;
                                }
                            }
                        }
                        if (!enabled) {
                            if (GameplayTrace.enabled()) {
                                GameplayTrace.log("equipment equip blocked-requirements player=" + GameplayTrace.describe(this.player) + " inventorySlot=" + slot + " itemId=" + itemStack.getId() + " item=" + itemStack.getDefinition().getName() + " reason=" + requirementBlockReason);
                            }
                            return;
                        }
                        if (this.player.getQuestState(0) < 42 && this.player.getQuestState(0) != 1) {
                            if (GameplayTrace.enabled()) {
                                GameplayTrace.log("equipment equip blocked-tutorial player=" + GameplayTrace.describe(this.player) + " inventorySlot=" + slot + " itemId=" + itemStack.getId() + " item=" + itemStack.getDefinition().getName() + " quest0=" + this.player.getQuestState(0));
                            }
                            this.player.getDialogueManager().showOneLineStatement("You haven't learned how to wield items yet!");
                            return;
                        }
                        if (this.player.getQuestState(0) != 44) break equipFromInventorySlotControlExit1;
                        if (itemStack.getId() != 1171) break equipFromInventorySlotControlExit4;
                        value3 = this.player.getEquipmentManager();
                        if (((EquipmentManager)value3).container.containsItem(1277)) break equipFromInventorySlotControlExit3;
                    }
                    if (itemStack.getId() != 1277) break equipFromInventorySlotControlExit2;
                    value3 = this.player.getEquipmentManager();
                    if (!((EquipmentManager)value3).container.containsItem(1171)) break equipFromInventorySlotControlExit2;
                }
                this.player.advanceTutorialStage();
            }
            this.player.getQuestManager().refreshQuestJournal();
        }
        if (itemStack.getId() == 1205 && this.player.getQuestState(0) == 42) {
            if (!this.player.isTutorialEquipmentStatsOpened()) {
                this.player.getDialogueManager().showOneLineStatement(
                        "First click the Equipment Stats button on your worn inventory.");
                return;
            }
            this.player.advanceTutorialStage();
        }
        if (this.player.isInDuelArena()) {
            int[] integerValues = ServerSettings.FUN_WEAPON_IDS;
            value = 0;
            while (value < 13) {
                int value8 = integerValues[value];
                if (!DuelRule.FUN_WEAPONS.isEnabledFor(this.player) && itemStack.getId() == value8) {
                    Player player = this.player;
                    player.packetSender.sendGameMessage("Usage of 'Fun weapons' haven't been enabled during this fight!");
                    return;
                }
                ++value;
            }
        }
        boolean blockedByDuelRule = false;
        switch (value2) {
            case 0: {
                blockedByDuelRule = DuelRule.NO_HELMET.isEnabledFor(this.player);
                break;
            }
            case 1: {
                blockedByDuelRule = DuelRule.NO_CAPE.isEnabledFor(this.player);
                break;
            }
            case 2: {
                blockedByDuelRule = DuelRule.NO_AMULET.isEnabledFor(this.player);
                break;
            }
            case 13: {
                blockedByDuelRule = DuelRule.NO_AMMO.isEnabledFor(this.player);
                break;
            }
            case 3: {
                blockedByDuelRule = DuelRule.NO_WEAPON.isEnabledFor(this.player) || itemStack.getDefinition().isTwoHanded() && DuelRule.NO_SHIELD.isEnabledFor(this.player);
                break;
            }
            case 4: {
                blockedByDuelRule = DuelRule.NO_BODY.isEnabledFor(this.player);
                break;
            }
            case 5: {
                blockedByDuelRule = DuelRule.NO_SHIELD.isEnabledFor(this.player);
                break;
            }
            case 7: {
                blockedByDuelRule = DuelRule.NO_LEGS.isEnabledFor(this.player);
                break;
            }
            case 9: {
                blockedByDuelRule = DuelRule.NO_GLOVES.isEnabledFor(this.player);
                break;
            }
            case 10: {
                blockedByDuelRule = DuelRule.NO_BOOTS.isEnabledFor(this.player);
                break;
            }
            case 12: {
                blockedByDuelRule = DuelRule.NO_RING.isEnabledFor(this.player);
            }
        }
        if (blockedByDuelRule) {
            Player player = this.player;
            player.packetSender.sendGameMessage("You cannot wear this during this fight!");
            return;
        }
        if (dropCastleWarsFlagForEquipment) {
            CastleWarsManager.dropCarriedFlagManually(this.player);
        }
        int value9;
        value = 0;
        if (itemStack.getDefinition().isStackable()) {
            value9 = value2;
            ItemStack itemStack2 = this.container.getItemAt(value9);
            this.player.getInventoryManager().removeItemFromSlot(itemStack, slot);
            if (this.container.getItemAt(value9) != null) {
                if (itemStack.getId() == itemStack2.getId()) {
                    this.container.setItem(value9, new ItemStack(itemStack.getId(), itemStack.getAmount() + itemStack2.getAmount(), itemStack.getMetadata()));
                } else {
                    this.player.getInventoryManager().setItemInSlot(itemStack2, slot);
                    this.container.setItem(value9, itemStack);
                }
            } else {
                this.container.setItem(value9, itemStack);
            }
        } else {
            value9 = value2;
            if (value9 == 3 && itemStack.getDefinition().isTwoHanded()) {
                if (this.container.getItemAt(3) != null && this.container.getItemAt(5) != null && this.player.getInventoryManager().getContainer().getFirstFreeSlot() == -1) {
                    Player player = this.player;
                    player.packetSender.sendGameMessage("Not enough space in your inventory.");
                    if (GameplayTrace.enabled()) {
                        GameplayTrace.log("equipment equip blocked-twohand-full-inventory player=" + GameplayTrace.describe(this.player) + " inventorySlot=" + slot + " itemId=" + itemStack.getId() + " item=" + itemStack.getDefinition().getName());
                    }
                    return;
                }
                this.player.getInventoryManager().removeItemFromSlot(itemStack, slot);
                value = 1;
                this.unequipSlot(5);
                if (this.container.getItemAt(5) != null) {
                    return;
                }
            }
            if (value9 == 5 && this.container.getItemAt(3) != null && this.container.getItemAt(3).getDefinition().isTwoHanded()) {
                this.player.getInventoryManager().removeItemFromSlot(itemStack, slot);
                value = 1;
                this.unequipSlot(3);
                if (this.container.getItemAt(3) != null) {
                    return;
                }
            }
            if (this.container.getItemAt(value9) != null) {
                ItemStack itemStack3 = this.container.getItemAt(value9);
                if (itemStack3.getId() == 4024) {
                    this.player.getUpdateState().setGraphic(160, 0);
                    this.player.npcTransformationId = -1;
                    this.player.setAppearanceUpdateRequired(true);
                }
                if (value == 0) {
                    this.player.getInventoryManager().removeItemFromSlot(itemStack, slot);
                    this.player.getInventoryManager().setItemInSlot(itemStack3, slot);
                } else {
                    this.player.getInventoryManager().addItem(itemStack3);
                }
            } else if (value == 0) {
                this.player.getInventoryManager().removeItemFromSlot(itemStack, slot);
            }
            this.container.setItem(value9, new ItemStack(itemStack.getId(), itemStack.getAmount(), itemStack.getMetadata()));
        }
        this.player.nextActionSequence();
        this.player.setSpecialAttackEnabled(false);
        EntityTargetMovement.clearMovementTarget(this.player);
        this.player.getInventoryManager().refresh();
        if (value2 == 3) {
            this.player.setWeaponProfile(WeaponProfile.forItem(itemStack));
            this.player.setSpecialAttackDefinition(SpecialAttackDefinition.forItem(itemStack));
            this.player.setAutocastSpell(null);
            BotCombatHelper.syncPrimaryMagicAutocast(this.player);
        }
        if (equipmentStatsOpen && this.player.getOpenInterfaceId() != 15106) {
            // Equipping can advance Tutorial Island and replace the tracked chatbox root.
            this.player.packetSender.showInterface(15106);
        }
        this.refresh();
        this.refreshCarriedValue();
        this.player.getAttributes().put("usedGlory", Boolean.FALSE);
        if (itemStack.getId() == 6583 || itemStack.getId() == 7927) {
            this.player.npcTransformationId = itemStack.getId() == 6583 ? 2626 : 3689 + GameUtil.randomInclusive(5);
            this.player.setAppearanceUpdateRequired(true);
            Player player = this.player;
            player.packetSender.clearSidebarInterfaces();
            player = this.player;
            player.packetSender.setSidebarInterface(3, 6014);
        }
        if (itemStack.getId() == 4024) {
            this.player.getUpdateState().setGraphic(160, 0);
            this.player.npcTransformationId = 1463;
            this.player.setAppearanceUpdateRequired(true);
        }
        GameplayHelper.refreshRubberChickenPlayerOption(this.player);
        GameplayHelper.refreshRunecraftingTiaraConfig(this.player, itemStack.getId());
        Player player = this.player;
        player.packetSender.sendSoundEffect(this.equipSoundIds[GameUtil.randomInt(this.equipSoundIds.length)], 1, 0);
        this.player.setAppearanceUpdateRequired(true);
        if (GameplayTrace.enabled()) {
            GameplayTrace.log("equipment equip success player=" + GameplayTrace.describe(this.player) + " inventorySlot=" + slot + " itemId=" + itemStack.getId() + " item=" + itemStack.getDefinition().getName() + " equipmentSlot=" + value2);
        }
    }

    public final void unequipSlot(int slot) {
        Object value;
        ItemStack itemStack = this.container.getItemAt(slot);
        if (itemStack == null) {
            if (GameplayTrace.enabled()) {
                GameplayTrace.log("equipment unequip missing-item player=" + GameplayTrace.describe(this.player) + " equipmentSlot=" + slot);
            }
            return;
        }
        if (GameplayTrace.enabled()) {
            GameplayTrace.log("equipment unequip request player=" + GameplayTrace.describe(this.player) + " equipmentSlot=" + slot + " itemId=" + itemStack.getId() + " item=" + itemStack.getDefinition().getName() + " inventoryFree=" + this.player.getInventoryManager().getContainer().getFreeSlots());
        }
        if (slot == 3 && CastleWarsManager.isCarryingFlag(this.player)
                && CastleWarsManager.isFlagItemId(itemStack.getId())) {
            CastleWarsManager.dropCarriedFlagManually(this.player);
            return;
        }
        if ((CastleWarsManager.isWaitingPlayer(this.player) || CastleWarsManager.isInGame(this.player))
                && CastleWarsManager.isTeamColourEquipmentSlot(slot)) {
            this.player.getPacketSender().sendGameMessage("You can't remove your Castle Wars team colours.");
            return;
        }
        if (this.player.getInventoryManager().getContainer().getFirstFreeSlot() == -1) {
            Player player = this.player;
            player.packetSender.sendGameMessage("Not enough space in your inventory.");
            if (GameplayTrace.enabled()) {
                GameplayTrace.log("equipment unequip blocked-full-inventory player=" + GameplayTrace.describe(this.player) + " equipmentSlot=" + slot + " itemId=" + itemStack.getId() + " item=" + itemStack.getDefinition().getName());
            }
            return;
        }
        if (itemStack.getId() == 6583 || itemStack.getId() == 7927) {
            this.player.npcTransformationId = -1;
            value = this.player;
            ((Player)value).packetSender.refreshSidebarInterfaces();
            this.player.setAppearanceUpdateRequired(true);
        }
        if (itemStack.getId() == 4024) {
            this.player.getUpdateState().setGraphic(160, 0);
            this.player.npcTransformationId = -1;
            this.player.setAppearanceUpdateRequired(true);
        }
        if (slot == 0) {
            GameplayHelper.refreshRunecraftingTiaraConfig(this.player, -1);
        }
        value = this.player.getEquipmentManager();
        if (!((EquipmentManager)value).container.containsItem(itemStack.getId())) {
            if (GameplayTrace.enabled()) {
                GameplayTrace.log("equipment unequip missing-container-item player=" + GameplayTrace.describe(this.player) + " equipmentSlot=" + slot + " itemId=" + itemStack.getId() + " item=" + itemStack.getDefinition().getName());
            }
            return;
        }
        this.player.nextActionSequence();
        this.container.removeFromSlot(itemStack, slot);
        this.player.getInventoryManager().addItem(new ItemStack(itemStack.getId(), itemStack.getAmount(), itemStack.getMetadata()));
        if (slot == 3) {
            this.player.setWeaponProfile(null);
            this.player.setSpecialAttackDefinition(null);
            this.player.setAutocastSpell(null);
        }
        ItemStack itemStack2 = new ItemStack(-1, 0);
        int value2 = slot;
        EquipmentManager equipmentManager = this;
        value = equipmentManager.player;
        ((Player)value).packetSender.sendInterfaceSlotItem(value2, 1688, itemStack2);
        if (ServerSettings.clientBuild == 443 && this.player.getOpenInterfaceId() == 15106) {
            this.player.packetSender.sendItemContainer(15107, this.container.getRawItems());
        }
        equipmentManager.player.getEquipmentManager().refreshWeaponAmmunitionState();
        equipmentManager.player.getEquipmentManager().refreshBarrowsSetEffects();
        equipmentManager.refreshEquipmentBonuses(equipmentManager.player);
        equipmentManager.refreshWeaponInterface();
        this.refreshCarriedValue();
        value = this.player;
        ((Player)value).packetSender.sendSoundEffect(this.equipSoundIds[GameUtil.randomInt(this.equipSoundIds.length)], 1, 0);
        GameplayHelper.refreshRubberChickenPlayerOption(this.player);
        this.player.setAppearanceUpdateRequired(true);
        if (GameplayTrace.enabled()) {
            GameplayTrace.log("equipment unequip success player=" + GameplayTrace.describe(this.player) + " equipmentSlot=" + slot + " itemId=" + itemStack.getId() + " item=" + itemStack.getDefinition().getName() + " inventoryFree=" + this.player.getInventoryManager().getContainer().getFreeSlots() + " statRefresh=true");
        }
    }

    public final void refreshCarriedValue() {
        Object value;
        double value2 = 0.0;
        int index = 0;
        while (index < 11) {
            value = this.player.getEquipmentManager();
            if (((EquipmentManager)value).container.getItemAt(index) != null) {
                value = this.player.getEquipmentManager();
                value = ((EquipmentManager)value).container.getItemAt(index);
                double definition = ((ItemStack)value).getDefinition().getWeight();
                if (((ItemStack)value).getDefinition().isMembersOnly() && ServerSettings.freeToPlayWorld && definition < 0.0) {
                    definition = 0.0;
                }
                value2 += definition;
            }
            ++index;
        }
        index = 0;
        while (index < 28) {
            if (this.player.getInventoryManager().getContainer().getItemAt(index) != null) {
                value2 += this.player.getInventoryManager().getContainer().getItemAt(index).getDefinition().getWeight();
                if (this.player.getInventoryManager().getContainer().getItemAt(index).getId() == 88) {
                    value2 += 4.8;
                }
            }
            ++index;
        }
        this.player.carriedWeight = value2;
        value = this.player;
        ((Player)value).packetSender.sendWeight();
    }

    public final void consumeSlotItemAmount(int itemId, int value2) {
        ItemStack itemStack = this.container.getItemAt(itemId);
        if (itemStack == null) {
            return;
        }
        value2 = this.container.removeFromSlot(itemStack, itemId) - value2;
        if (value2 > 0) {
            ItemStack itemStack2 = new ItemStack(itemStack.getId(), value2, itemStack.getMetadata());
            this.container.add(itemStack2, itemId);
        } else {
            this.container.add(new ItemStack(-1, 0), itemId);
        }
        this.refresh();
        this.player.setAppearanceUpdateRequired(true);
        this.refreshEquipmentBonuses(this.player);
    }

    private void refreshEquipmentBonuses(Player player) {
        Object value;
        int index = 0;
        int index2 = 0;
        while (index2 < 14) {
            player.setCombatBonus(index2, 0);
            ++index2;
        }
        Object value2 = player;
        index2 = 0;
        while (index2 < 14) {
            value = ((Player)value2).getEquipmentManager();
            if (!(((EquipmentManager)value).container.getItemAt(index2) == null || ((Player)value2).isCrystalBowEquipped() && index2 == 13)) {
                value = ((Player)value2).getEquipmentManager();
                value = ((EquipmentManager)value).container.getItemAt(index2);
                if (!ItemDefinition.isRemovedContent(((ItemStack)value).getId())
                        && (!((ItemStack)value).getDefinition().isMembersOnly() || !ServerSettings.freeToPlayWorld)) {
                    int index3 = 0;
                    while (index3 < 14) {
                        int index4 = 0;
                        if (((ItemStack)value).getId() == 11283 && ((ItemStack)value).getMetadata() >= 0 && (index3 == 5 || index3 == 6 || index3 == 7 || index3 == 9)) {
                            index4 = ((ItemStack)value).getMetadata();
                        }
                        ((Player)value2).setCombatBonus(index3, ((ItemStack)value).getDefinition().getBonuses()[index3] + index4 + (Integer)((Player)value2).getCombatBonuses().get(index3));
                        ++index3;
                    }
                }
            }
            ++index2;
        }
        index2 = 0;
        while (index2 < 12) {
            value2 = (Integer)player.getCombatBonuses().get(index2) >= 0 ? String.valueOf(ServerSettings.bonusTypeNames[index2]) + ": +" + player.getCombatBonuses().get(index2) : String.valueOf(ServerSettings.bonusTypeNames[index2]) + ": -" + Math.abs((Integer)player.getCombatBonuses().get(index2));
            if (index2 == 10) {
                index = 1;
            }
            value = player;
            ((Player)value).packetSender.sendInterfaceText((String)value2, index2 + 1675 + index);
            ++index2;
        }
    }

    public final void refreshWeaponInterface() {
        Object value = this;
        ItemStack itemStack = ((EquipmentManager)value).container.getItemAt(3);
        if (itemStack != null && (ItemDefinition.isRemovedContent(itemStack.getId())
                || itemStack.getDefinition().isMembersOnly() && ServerSettings.freeToPlayWorld)) {
            itemStack = null;
        }
        WeaponProfile weaponProfile = WeaponProfile.forItem(itemStack);
        if (this.player.getQuestState(0) >= 45 || this.player.getQuestState(0) == 1) {
            value = this.player;
            ((Player)value).packetSender.setSidebarInterface(0, weaponProfile.getInterfaceDefinition().getSidebarInterfaceId());
        }
        value = this.player;
        ((Player)value).packetSender.sendInterfaceText(itemStack == null ? "Unarmed" : itemStack.getDefinition().getName(), weaponProfile.getInterfaceDefinition().getWeaponNameTextId());
        WeaponProfile weaponProfile2 = weaponProfile;
        value = this;
        if (weaponProfile2.getAttackAnimations().length < 4 && ((EquipmentManager)value).player.getFightMode() == 3) {
            ((EquipmentManager)value).player.setFightMode(2);
        }
        value = this.player;
        if (ServerSettings.clientBuild == 443) {
            this.player.packetSender.refreshAutocastConfig();
        } else {
            ((Player)value).packetSender.sendConfig(43, this.player.getFightMode());
        }
        // The 443 tab has no weapon picture; the classic (2004) tab does (if_setobject at zoom 200).
        if (weaponProfile.getInterfaceDefinition().getWeaponModelWidgetId() != -1
                && (ServerSettings.clientBuild != 443 || this.player.classicSidebar)) {
            value = this.player;
            ((Player)value).packetSender.sendInterfaceModel(weaponProfile.getInterfaceDefinition().getWeaponModelWidgetId(), 200, itemStack.getId());
        }
        if (weaponProfile.getInterfaceDefinition().getSpecialBarWidgetId() != -1) {
            if (SpecialAttackDefinition.forItem(itemStack) == null) {
                value = this.player;
                ((Player)value).packetSender.setInterfaceHiddenFlag(1, weaponProfile.getInterfaceDefinition().getSpecialBarWidgetId());
                return;
            }
            value = this.player;
            ((Player)value).packetSender.setInterfaceHiddenFlag(0, weaponProfile.getInterfaceDefinition().getSpecialBarWidgetId());
            this.player.refreshSpecialAttackWidgets();
        }
    }

    public final boolean handleAttackStyleButton(int buttonId) {
        Object value = this;
        value = ((EquipmentManager)value).container.getItemAt(3);
        if (value != null && ((ItemStack)value).getDefinition().isMembersOnly() && ServerSettings.freeToPlayWorld) {
            value = null;
        }
        value = WeaponProfile.forItem((ItemStack)value);
        int index = 0;
        while (index < ((WeaponProfile)((Object)value)).getInterfaceDefinition().getAttackStyles().length) {
            if (buttonId == ((WeaponProfile)((Object)value)).getInterfaceDefinition().getAttackStyles()[index].getButtonId()) {
                this.player.disableAutocast();
                this.player.setFightMode(index);
                return true;
            }
            ++index;
        }
        return false;
    }

    public final int getItemIdAtSlot(int itemId) {
        EquipmentManager equipmentManager = this;
        ItemStack itemStack = equipmentManager.container.getItemAt(itemId);
        if (itemStack != null) {
            return itemStack.getId();
        }
        return 0;
    }

    public final int getStandAnimation() {
        return this.player.getWeaponProfile().getMovementAnimations()[0];
    }

    public final int getWalkAnimation() {
        return this.player.getWeaponProfile().getMovementAnimations()[1];
    }

    public final int getRunAnimation() {
        return this.player.getWeaponProfile().getMovementAnimations()[2];
    }

    public final ItemContainer getContainer() {
        return this.container;
    }

    public final boolean canEquipItem(int itemId) {
        if (ItemDefinition.isRemovedContent(itemId)) return false;
        int value;
        int value2;
        int index = 0;
        while (index < 22) {
            value2 = this.player.getSkillManager().getBaseLevel(index);
            if (value2 < (value = new ItemStack(itemId).getDefinition().getRequiredLevel(index))) {
                return false;
            }
            ++index;
        }
        index = this.player.getQuestPoints();
        if (index < (value2 = new ItemStack(itemId).getDefinition().getRequiredQuestPoints())) {
            return false;
        }
        value = 0;
        while (value < QuestDefinition.questCount) {
            index = new ItemStack(itemId).getDefinition().requiresQuest(value) ? 1 : 0;
            int questState = value2 = this.player.getQuestState(value) == 1 ? 1 : 0;
            if (index != 0 && value2 == 0) {
                return false;
            }
            ++value;
        }
        return true;
    }

    public final void refreshBarrowsSetEffects() {
        this.player.setAhrimSetEffectActive(this.isEquippedSet("ahrims hood", "ahrims top", "ahrims robetop", "ahrims skirt", "ahrims robeskirt", "ahrims staff"));
        this.player.setKarilSetEffectActive(this.isEquippedSet("karils coif", "karils top", "karils leathertop", "karils skirt", "karils leatherskirt", "karils x-bow", "karils crossbow"));
        this.player.setDharokSetEffectActive(this.isEquippedSet("dharoks helm", "dharoks body", "dharoks platebody", "dharoks legs", "dharoks platelegs", "dharoks axe", "dharoks greataxe"));
        this.player.setVeracSetEffectActive(this.isEquippedSet("veracs helm", "veracs top", "veracs brassard", "veracs skirt", "veracs plateskirt", "veracs flail"));
        this.player.setToragSetEffectActive(this.isEquippedSet("torags helm", "torags body", "torags platebody", "torags legs", "torags platelegs", "torags hammer", "torags hammers"));
        this.player.setGuthanSetEffectActive(this.isEquippedSet("guthans helm", "guthans body", "guthans platebody", "guthans skirt", "guthans chainskirt", "guthans spear", "guthans warspear"));
    }

    private boolean isEquippedSet(String helmName, String bodyName, String alternateBodyName, String legsName, String alternateLegsName, String weaponName) {
        return this.hasEquippedName(0, helmName)
            && this.hasEquippedName(4, bodyName, alternateBodyName)
            && this.hasEquippedName(7, legsName, alternateLegsName)
            && this.hasEquippedName(3, weaponName);
    }

    private boolean isEquippedSet(String helmName, String bodyName, String alternateBodyName, String legsName, String alternateLegsName, String weaponName, String alternateWeaponName) {
        return this.hasEquippedName(0, helmName)
            && this.hasEquippedName(4, bodyName, alternateBodyName)
            && this.hasEquippedName(7, legsName, alternateLegsName)
            && this.hasEquippedName(3, weaponName, alternateWeaponName);
    }

    private boolean hasEquippedName(int slot, String nameFragment) {
        ItemStack itemStack = this.container.getItemAt(slot);
        return itemStack != null && itemStack.getDefinition().getName().toLowerCase().contains(nameFragment);
    }

    private boolean hasEquippedName(int slot, String nameFragment, String alternateNameFragment) {
        return this.hasEquippedName(slot, nameFragment) || this.hasEquippedName(slot, alternateNameFragment);
    }

    public final void refreshWeaponAmmunitionState() {
        String weaponProfile = this.player.getWeaponProfile().name().toLowerCase();
        boolean enabled = ((weaponProfile = weaponProfile.replaceAll("_", " ")).contains("seercull") || weaponProfile.contains("bow")) && !weaponProfile.contains("crossbow");
        Player player = this.player;
        boolean enabled2 = weaponProfile.contains("crossbow") || weaponProfile.contains("x-bow");
        Player player2 = this.player;
        boolean enabled3 = weaponProfile.contains("knife") || weaponProfile.contains("dart") || weaponProfile.contains("javelin") || weaponProfile.contains("thrownaxe") || weaponProfile.contains("toktz-xil-ul") || weaponProfile.contains("throwing axe");
        Player player3 = this.player;
        this.player.setCrystalBowEquipped(weaponProfile.contains("crystal bow"));
        ItemService.getInstance();
        String itemName = ItemService.getItemName(this.player.getEquipmentManager().getItemIdAtSlot(13)).toLowerCase();
        boolean enabled4 = itemName.contains("arrow");
        Player player4 = this.player;
        boolean enabled5 = itemName.contains("bolt");
        Player player5 = this.player;
        this.player.setAmmunitionDropsEnabled(!weaponProfile.contains("crystal") && !weaponProfile.contains("karils"));
    }

    static Player getPlayer(EquipmentManager equipmentManager) {
        return equipmentManager.player;
    }
}
