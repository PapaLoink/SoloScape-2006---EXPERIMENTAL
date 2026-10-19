package com.rs2.model.skill.cooking;

import com.rs2.ServerSettings;
import com.rs2.model.Position;
import com.rs2.model.item.ItemDefinition;
import com.rs2.model.item.ItemStack;
import com.rs2.model.objects.LoadedWorldObject;
import com.rs2.model.objects.ObjectDefinition;
import com.rs2.model.objects.WorldObject;
import com.rs2.model.objects.WorldObjectLookup;
import com.rs2.model.player.Player;
import com.rs2.model.skill.SkillActionHelper;
import com.rs2.model.skill.cooking.CookableFoodDefinition;
import com.rs2.model.skill.cooking.CookingTask;
import com.rs2.model.skill.firemaking.FiremakingHandler;
import com.rs2.model.task.CycleEventHandler;
import com.rs2.util.GameplayTrace;
import com.rs2.util.GameUtil;

public final class CookingManager {
    private Player player;
    public Position firePosition;
    private static int[] waterSourceObjectIds;

    static {
        waterSourceObjectIds = new int[]{153, 879, 880, 2654, 2864, 6232, 10436, 10437, 11007, 11759, 13478, 13479, 13480, 21764, 24161, 24214, 24265, 884, 11793, 43, 873, 874, 4063, 6151, 8699, 9143, 9684, 10175, 12279, 12974, 13563, 13564, 14868, 14917, 15678, 16704, 16705, 20358, 22715, 24112, 24314, 11661, 4176, 4285, 4482, 6827};
    }

    public CookingManager(Player player) {
        this.player = player;
    }

    public final boolean handleItemOnCookingObject(int rawItemId, int objectId, int x, int y) {
        CookableFoodDefinition food = CookableFoodDefinition.forRawItemId(rawItemId);
        if (food == null) {
            return false;
        }

        int plane = this.player.getPosition().getPlane();
        LoadedWorldObject loaded = WorldObjectLookup.findObjectByIdAt(objectId, x, y, plane);
        WorldObject worldObject = SkillActionHelper.findWorldObjectById(objectId, x, y, plane);
        if (loaded == null && worldObject == null) {
            return false;
        }

        int actualObjectId = loaded != null ? loaded.getWorldObject().getObjectId() : worldObject.getObjectId();
        ObjectDefinition definition = ObjectDefinition.forId(actualObjectId);
        String objectName = definition == null ? null : definition.name;
        boolean fire = isFireCookingSurface(actualObjectId, objectName);
        boolean range = !fire && isRangeCookingSurface(objectName);
        if (!fire && !range) {
            return false;
        }
        if (fire && isUnlitFireplace(definition, objectName)) {
            this.player.packetSender.sendGameMessage("You need to light the fireplace first.");
            return true;
        }

        this.player.packetSender.closeInterfaces();
        if (!ServerSettings.cookingEnabled) {
            this.player.packetSender.sendGameMessage("This skill is currently disabled.");
            return true;
        }
        if (this.player.getInventoryManager().getItemAmount(rawItemId) < 1) {
            return true;
        }
        // A lit fireplace counts as a fire, not an oven. Most dough, pies,
        // and other range-only items cannot be cooked here. Raw potatoes are
        // a historical exception: putting them on a fire burns them.
        if (fire && !food.canCookOnFire() && rawItemId != 1942) {
            this.player.packetSender.sendGameMessage("You need to use a range to cook this.");
            return true;
        }
        if (this.player.getSkillManager().getCurrentLevels()[7] < food.getRequiredLevel()) {
            this.player.getDialogueManager().showOneLineStatement(
                    "You need a cooking level of " + food.getRequiredLevel() + " to cook this.");
            return true;
        }

        if (GameplayTrace.enabled()) {
            GameplayTrace.log("cooking item-on-object accepted player=" + GameplayTrace.describe(this.player)
                    + " rawItemId=" + rawItemId + " objectId=" + actualObjectId + " objectName="
                    + objectName + " action=" + (fire ? "cookFire" : "cookRange")
                    + " x=" + x + " y=" + y + " plane=" + plane);
        }
        this.player.beginInterruptibleAction();
        this.player.interfaceAction = fire ? "cookFire" : "cookRange";
        this.player.setCookingObjectId(actualObjectId);
        this.firePosition = fire ? new Position(x, y, plane) : null;
        this.player.setSelectedSkillItemId(rawItemId);
        this.player.getUpdateState().setFacePosition(new Position(x, y, plane));

        // September 2005 Cook-X: a single raw item cooks immediately; with
        // multiple items the chatbox offers Cook 1 / 5 / X / All.
        if (this.player.botEnabled) {
            startCookingTask(this.player, 28);
        } else if (this.player.getQuestState(0) != 1
                || this.player.getInventoryManager().getItemAmount(rawItemId) == 1
                || ServerSettings.cacheVersion < 334) {
            startCookingTask(this.player, 1);
        } else {
            if (ServerSettings.clientBuild == 443) {
                // Open the native chatbox before updating child widgets. The
                // opening packet can restore cached text/positions, otherwise
                // swallowing the food-name update.
                this.player.packetSender.showChatboxInterface(1743);
                // The client creates native 307:8 as a text child because
                // the stock 307:1 is a decorative sprite, not a label.
                this.player.packetSender.sendInterfaceModel(13716, 200, rawItemId);
                this.player.packetSender.sendInterfaceText(
                        ItemDefinition.forId(rawItemId).getName(), 13717);
            } else {
                this.player.packetSender.sendInterfaceModel(13716, 200, rawItemId);
                this.player.packetSender.sendInterfaceText(
                        ItemDefinition.forId(rawItemId).getName(), 13717);
                this.player.packetSender.showChatboxInterface(1743);
            }
        }
        return true;
    }

    static boolean isFireCookingSurface(int objectId, String objectName) {
        if (FiremakingHandler.isFireObjectId(objectId)) {
            return true;
        }
        if (objectName == null) {
            return false;
        }
        String name = objectName.trim().toLowerCase();
        if (name.startsWith("unlit ") || name.startsWith("unlit-")) {
            return false;
        }
        return name.equals("fire")
                || name.equals("fireplace")
                || name.equals("campfire")
                || name.equals("ogre fire")
                || name.startsWith("firepit")
                || name.endsWith(" fireplace");
    }

    // Some unlit fireplace objects share the name "Fireplace" with
    // their lit counterparts. Check the examine text and Light action too.
    static boolean isUnlitFireplace(ObjectDefinition definition, String name) {
        if (definition == null || name == null || !name.toLowerCase().contains("fireplace")) {
            return false;
        }
        if (definition.description != null
                && definition.description.toLowerCase().contains("you can light a fire here")) {
            return true;
        }
        for (int slot = 0; slot < 5; slot++) {
            String action = definition.getAction(slot);
            if (action != null && action.toLowerCase().startsWith("light")) {
                return true;
            }
        }
        return false;
    }

    static boolean isRangeCookingSurface(String objectName) {
        if (objectName == null) {
            return false;
        }
        String name = objectName.trim().toLowerCase();
        return name.equals("stove")
                || name.equals("range")
                || name.equals("cooking range")
                || name.equals("cooking pot")
                || name.equals("cooking pots")
                || name.endsWith(" range");
    }

    public static void startCookingTask(Player player, int amount) {
        CookableFoodDefinition food = CookableFoodDefinition.forRawItemId(player.getSelectedSkillItemId());
        if (amount <= 0 || food == null
                || (!"cookFire".equals(player.interfaceAction) && !"cookRange".equals(player.interfaceAction))) {
            return;
        }
        int available = player.getInventoryManager().getItemAmount(player.getSelectedSkillItemId());
        if (available <= 0) {
            return;
        }
        if (player.getSkillManager().getCurrentLevels()[7] < food.getRequiredLevel()) {
            player.getDialogueManager().showOneLineStatement(
                    "You need a cooking level of " + food.getRequiredLevel() + " to cook this.");
            return;
        }
        if ("cookFire".equals(player.interfaceAction)
                && (!food.canCookOnFire() && player.getSelectedSkillItemId() != 1942
                    || player.getCookingManager().firePosition == null)) {
            return;
        }
        int sequence = player.nextActionSequence();
        player.getMovementQueue().clear();
        player.packetSender.closeInterfaces();
        player.setActiveCycleEvent(new CookingTask(Math.min(amount, available), player, sequence));
        CycleEventHandler.getInstance().schedule(player, player.getActiveCycleEvent(), 1);
    }

    public static void cookSelectedItem(Player player) {
        CookableFoodDefinition cookableFoodDefinition = CookableFoodDefinition.forRawItemId(player.getSelectedSkillItemId());
        if (cookableFoodDefinition == null) {
            return;
        }
        if (player.getSkillManager().getCurrentLevels()[7] < cookableFoodDefinition.getRequiredLevel()) {
            player.getDialogueManager().showOneLineStatement("You need a cooking level of " + cookableFoodDefinition.getRequiredLevel() + " to cook this.");
            if (player.botEnabled) {
                player.currentBotTask.startWalkToBank(player);
            }
            return;
        }
        Player player2 = player;
        player2.packetSender.closeInterfaces();
        player.getInventoryManager().removeItem(new ItemStack(player.getSelectedSkillItemId()));
        player2 = player;
        if ("cookFire".equals(player2.interfaceAction)) {
            player.getUpdateState().setAnimation(897);
        } else {
            player2 = player;
            if ("cookRange".equals(player2.interfaceAction)) {
                player.getUpdateState().setAnimation(883);
            }
        }
        player2 = player;
        player2.packetSender.sendSoundEffect(357, 1, 0);
        if (player.getQuestState(0) != 1) {
            if (player.getQuestState(0) == 13) {
                CookingManager.processCookingResult(player, player.getSelectedSkillItemId(), true);
                player.advanceTutorialStage();
            } else if (player.getQuestState(0) == 14) {
                CookingManager.processCookingResult(player, player.getSelectedSkillItemId(), false);
                player.advanceTutorialStage();
            } else if (player.getQuestState(0) == 19 && player.getSelectedSkillItemId() == 2307) {
                CookingManager.processCookingResult(player, player.getSelectedSkillItemId(), false);
                player2 = player;
                player2.packetSender.sendEntityHintIcon(1, -1);
                player.advanceTutorialStage();
            } else {
                CookingManager.processCookingResult(player, player.getSelectedSkillItemId(), false);
            }
            player.getQuestManager().refreshQuestJournal();
            return;
        }
        if (!cookableFoodDefinition.canCookOnFire()) {
            player2 = player;
            if ("cookFire".equals(player2.interfaceAction)) {
                CookingManager.processCookingResult(player, player.getSelectedSkillItemId(), true);
                return;
            }
        }
        CookingManager.processCookingResult(player, player.getSelectedSkillItemId(), false);
    }

    private static void processCookingResult(Player player, int value2, boolean enabled2) {
        CookableFoodDefinition cookableFoodDefinition = CookableFoodDefinition.forRawItemId(value2);
        int rawItemId = value2;
        int successChanceLow = cookableFoodDefinition.getSuccessChanceLow();
        int successChanceHigh = cookableFoodDefinition.getSuccessChanceHigh();
        // Ranges are less likely to burn food than fires or fireplaces.
        // These 0-256 cooking chance thresholds gain a small range advantage.
        if ("cookRange".equals(player.interfaceAction)) {
            successChanceLow += 5;
            successChanceHigh += 5;
        }
        if (player.getEquipmentManager().getContainer().getItemAt(9) != null && player.getEquipmentManager().getContainer().getItemAt(9).getId() == 775 && (value2 == 377 || value2 == 371 || value2 == 383)) {
            successChanceLow += 14;
            successChanceHigh += 31;
        }
        boolean skillManager = GameUtil.rollLevelScaledChance(successChanceLow, successChanceHigh, player.getSkillManager().getCurrentLevels()[7]);
        value2 = skillManager ? 1 : 0;
        if (skillManager && !enabled2 || player.getQuestState(0) == 14 || player.getQuestState(0) == 19) {
            player.getInventoryManager().addItem(new ItemStack(cookableFoodDefinition.getCookedItemId()));
            player.getSkillManager().addExperience(7, cookableFoodDefinition.getExperience());
            if (GameplayTrace.enabled()) {
                GameplayTrace.log("cooking result player=" + GameplayTrace.describe(player) + " rawItemId=" + rawItemId + " cookedItemId=" + cookableFoodDefinition.getCookedItemId() + " result=success xp=" + cookableFoodDefinition.getExperience() + " action=" + player.interfaceAction);
            }
            if (cookableFoodDefinition.getCookedItemId() != 2146) {
                player.packetSender.sendGameMessage("You successfully cook a " + ItemDefinition.forId(cookableFoodDefinition.getCookedItemId()).getName().toLowerCase() + ".");
                return;
            }
            player.packetSender.sendGameMessage("You deliberately burn the perfectly good piece of meat.");
            return;
        }
        player.getInventoryManager().addItem(new ItemStack(cookableFoodDefinition.getBurntItemId()));
        if (GameplayTrace.enabled()) {
            GameplayTrace.log("cooking result player=" + GameplayTrace.describe(player) + " rawItemId=" + rawItemId + " burntItemId=" + cookableFoodDefinition.getBurntItemId() + " result=burn action=" + player.interfaceAction);
        }
        if (cookableFoodDefinition.getCookedItemId() != 2146) {
            player.packetSender.sendGameMessage("You accidentally burn the " + ItemDefinition.forId(cookableFoodDefinition.getCookedItemId()).getName().toLowerCase() + ".");
            return;
        }
        player.packetSender.sendGameMessage("You deliberately burn the perfectly good piece of meat.");
    }

    public static boolean handleCookingButton(Player player, int buttonId) {
        switch (buttonId) {
            case 13720: {
                CookingManager.startCookingTask(player, 1);
                return true;
            }
            case 13719: {
                CookingManager.startCookingTask(player, 5);
                return true;
            }
            case 13717: {
                CookingManager.startCookingTask(player, 28);
                return true;
            }
        }
        return false;
    }

    /*
     * Enabled aggressive block sorting
     */
    public final boolean handleWaterSourceItem(int itemId, int value9) {
        int index = 0;
        while (index < 46) {
            if (value9 == waterSourceObjectIds[index]) {
                if (itemId >= 5331 && itemId < 5340) {
                    this.player.getInventoryManager().getContainer().replaceItemId(itemId, 5340);
                    this.player.getUpdateState().setAnimation(832);
                    Player player = this.player;
                    Object value2 = player;
                    value2 = ObjectDefinition.forId(value9);
                    player.packetSender.sendGameMessage("You fill the " + ItemDefinition.forId(5331).getName().toLowerCase() + " from the " + ((ObjectDefinition)value2).name.toLowerCase() + ".");
                    value2 = this.player;
                    ((Player)value2).packetSender.sendSoundEffect(1039, 1, 0);
                    return true;
                }
                switch (itemId) {
                    case 1825: 
                    case 1827: 
                    case 1829: 
                    case 1831: {
                        this.player.getInventoryManager().getContainer().replaceItemId(itemId, 1823);
                        this.player.getUpdateState().setAnimation(832);
                        Player player = this.player;
                        Object value3 = player;
                        value3 = ObjectDefinition.forId(value9);
                        player.packetSender.sendGameMessage("You fill the " + ItemDefinition.forId(itemId).getName().toLowerCase() + " from the " + ((ObjectDefinition)value3).name.toLowerCase() + ".");
                        value3 = this.player;
                        ((Player)value3).packetSender.sendSoundEffect(1039, 1, 0);
                        return true;
                    }
                    case 229: {
                        this.player.getInventoryManager().getContainer().replaceItemId(229, 227);
                        this.player.getUpdateState().setAnimation(832);
                        Player player = this.player;
                        Object value4 = player;
                        value4 = ObjectDefinition.forId(value9);
                        player.packetSender.sendGameMessage("You fill the " + ItemDefinition.forId(itemId).getName().toLowerCase() + " from the " + ((ObjectDefinition)value4).name.toLowerCase() + ".");
                        value4 = this.player;
                        ((Player)value4).packetSender.sendSoundEffect(1039, 1, 0);
                        return true;
                    }
                    case 1925: {
                        this.player.getInventoryManager().getContainer().replaceItemId(1925, 1929);
                        this.player.getUpdateState().setAnimation(832);
                        Player player = this.player;
                        Object value5 = player;
                        value5 = ObjectDefinition.forId(value9);
                        player.packetSender.sendGameMessage("You fill the " + ItemDefinition.forId(itemId).getName().toLowerCase() + " from the " + ((ObjectDefinition)value5).name.toLowerCase() + ".");
                        value5 = this.player;
                        ((Player)value5).packetSender.sendSoundEffect(1039, 1, 0);
                        return true;
                    }
                    case 1923: {
                        this.player.getInventoryManager().getContainer().replaceItemId(1923, 1921);
                        this.player.getUpdateState().setAnimation(832);
                        Player player = this.player;
                        Object value6 = player;
                        value6 = ObjectDefinition.forId(value9);
                        player.packetSender.sendGameMessage("You fill the " + ItemDefinition.forId(itemId).getName().toLowerCase() + " from the " + ((ObjectDefinition)value6).name.toLowerCase() + ".");
                        value6 = this.player;
                        ((Player)value6).packetSender.sendSoundEffect(1039, 1, 0);
                        return true;
                    }
                    case 1935: {
                        this.player.getInventoryManager().getContainer().replaceItemId(1935, 1937);
                        this.player.getUpdateState().setAnimation(832);
                        Player player = this.player;
                        Object value7 = player;
                        value7 = ObjectDefinition.forId(value9);
                        player.packetSender.sendGameMessage("You fill the " + ItemDefinition.forId(itemId).getName().toLowerCase() + " from the " + ((ObjectDefinition)value7).name.toLowerCase() + ".");
                        value7 = this.player;
                        ((Player)value7).packetSender.sendSoundEffect(1039, 1, 0);
                        return true;
                    }
                    case 1980: {
                        this.player.getInventoryManager().getContainer().replaceItemId(1980, 4458);
                        this.player.getUpdateState().setAnimation(832);
                        Player player = this.player;
                        Object value8 = player;
                        value8 = ObjectDefinition.forId(value9);
                        player.packetSender.sendGameMessage("You fill the " + ItemDefinition.forId(itemId).getName().toLowerCase() + " from the " + ((ObjectDefinition)value8).name.toLowerCase() + ".");
                        value8 = this.player;
                        ((Player)value8).packetSender.sendSoundEffect(1039, 1, 0);
                        return true;
                    }
                }
            }
            ++index;
        }
        return false;
    }
}
