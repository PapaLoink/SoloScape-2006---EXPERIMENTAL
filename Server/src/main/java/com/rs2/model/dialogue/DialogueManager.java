package com.rs2.model.dialogue;

import com.rs2.CacheCoordinateTranslator;
import com.rs2.Server;
import com.rs2.ServerSettings;
import com.rs2.cache.CacheDefinitionIndex;
import com.rs2.model.GameplayHelper;
import com.rs2.model.Position;
import com.rs2.model.World;
import com.rs2.model.bankpin.BankPinEntryMode;
import com.rs2.model.clue.TreasureTrailManager;
import com.rs2.model.combat.AttackStyleDefinition;
import com.rs2.model.dialogue.TenthSquadSigilTeleportTask;
import com.rs2.model.gameplay.abyss.AbyssManager;
import com.rs2.model.gameplay.barrows.BarrowsManager;
import com.rs2.model.gameplay.duel.DuelRule;
import com.rs2.model.gameplay.partyroom.PartyRoomManager;
import com.rs2.model.ground.GroundItem;
import com.rs2.model.ground.GroundItemManager;
import com.rs2.model.item.ItemDefinition;
import com.rs2.model.item.ItemStack;
import com.rs2.model.interaction.AlKharidGateToll;
import com.rs2.model.item.action.BarrowsRepairHandler;
import com.rs2.model.item.action.GodBookHandler;
import com.rs2.model.npc.Npc;
import com.rs2.model.npc.NpcDefinition;
import com.rs2.model.player.BankManager;
import com.rs2.model.player.GrandExchangeManager;
import com.rs2.model.player.Player;
import com.rs2.model.quest.QuestDefinition;
import com.rs2.model.randomevent.RandomEventManager;
import com.rs2.model.randomevent.sandwichlady.SandwichLadyFoodOffer;
import com.rs2.model.shop.ShopManager;
import com.rs2.model.skill.GatheringToolDefinition;
import com.rs2.model.skill.ItemCombinationHandler;
import com.rs2.model.skill.farming.FarmingFarmerDefinition;
import com.rs2.model.skill.farming.FarmingFarmerHandler;
import com.rs2.model.skill.magic.TeleportManager;
import com.rs2.model.skill.runecrafting.EssencePouchDefinition;
import com.rs2.model.skill.slayer.SlayerMasterDefinition;
import com.rs2.model.skill.slayer.SlayerMonsterGuide;
import com.rs2.model.skill.smithing.DragonSquareShieldSmithing;
import com.rs2.model.travel.HajedyCartRoute;
import com.rs2.model.travel.ShipRoute;
import com.rs2.model.travel.TravelManager;
import com.rs2.util.GameUtil;
import com.rs2.util.GameplayTrace;

public class DialogueManager {
    private Player player;
    private int dialogueId;
    private int dialogueType;
    private int dialogueContextX;
    private int dialogueContextY;
    private int dialogueStep;
    private int dialogueContextId;
    private int dialogueNpcId;
    private boolean tutorialWelcomePending;

    public DialogueManager(Player player) {
        this.player = player;
    }

    private void traceDisplay(String kind, String text2) {
        if (GameplayTrace.enabled()) {
            GameplayTrace.log("dialogue display kind=" + kind + " player=" + GameplayTrace.describe(this.player) + " id=" + this.dialogueId + " step=" + this.dialogueStep + " type=" + this.dialogueType + " npcId=" + this.dialogueNpcId + " text=" + text2);
        }
    }

    public final void setDialogueStep(int dialogueStep) {
        this.dialogueStep = dialogueStep;
    }

    public final void setNextDialogueStep(int nextDialogueStep) {
        this.dialogueStep = nextDialogueStep - 1;
    }

    public final int getDialogueStep() {
        return this.dialogueStep;
    }

    public final void finishDialogue() {
        this.dialogueStep = 9001;
    }

    public final void markDialogueInactive() {
        this.dialogueStep = -1;
    }

    public final boolean isDialogueInactive() {
        return this.dialogueStep > 9000 || this.dialogueStep < 0 || this.dialogueId < 0;
    }

    public final boolean continueTutorialStatement() {
        if (!this.isDialogueInactive()) {
            return false;
        }
        if (this.tutorialWelcomePending && this.player.getOpenInterfaceId() == 374) {
            this.resetDialogueState();
            this.player.packetSender.closeInterfaces();
            return true;
        }
        if (this.player.getQuestState(0) == 1) {
            return false;
        }
        switch (this.player.getOpenInterfaceId()) {
            case 356:
            case 359:
            case 363:
            case 368:
            case 374:
                this.resetDialogueState();
                this.player.getQuestManager().refreshQuestJournal();
                return true;
            default:
                return false;
        }
    }

    public final void setDialogueId(int dialogueId) {
        this.dialogueId = dialogueId;
    }

    public final int getDialogueId() {
        return this.dialogueId;
    }

    public final void setDialogueType(int type) {
        this.dialogueType = type;
    }

    public final int getDialogueType() {
        return this.dialogueType;
    }

    public final int getDialogueContextX() {
        return this.dialogueContextX;
    }

    public final int getDialogueContextY() {
        return this.dialogueContextY;
    }

    public final int getDialogueContextId() {
        return this.dialogueContextId;
    }

    public final void resetDialogueState() {
        this.tutorialWelcomePending = false;
        int index = 0;
        DialogueManager dialogueManager = this;
        this.dialogueStep = index;
        index = -1;
        dialogueManager = this;
        this.dialogueId = index;
        index = 0;
        dialogueManager = this;
        this.dialogueType = index;
        index = -1;
        dialogueManager = this;
        this.dialogueContextX = index;
        index = -1;
        dialogueManager = this;
        this.dialogueContextY = index;
    }

    public final boolean handleOptionButton(int buttonId) {
        if (GameplayTrace.enabled()) {
            GameplayTrace.log("dialogue option-button player=" + GameplayTrace.describe(this.player) + " buttonId=" + buttonId + " id=" + this.dialogueId + " step=" + this.dialogueStep + " type=" + this.dialogueType + " npcId=" + this.dialogueNpcId);
        }
        switch (buttonId) {
            case 2461: 
            case 2471: 
            case 2482: 
            case 2494: {
                DialogueManager dialogueManager = this;
                if (dialogueManager.dialogueType == 0) {
                    DialogueManager dialogueManager2 = this;
                    dialogueManager = dialogueManager2;
                    dialogueManager = this;
                    DialogueManager.continueDialogue(this.player, dialogueManager2.dialogueId, dialogueManager.dialogueStep + 1, 1);
                }
                dialogueManager = this;
                if (dialogueManager.dialogueType == 1) {
                    DialogueManager dialogueManager3 = this;
                    dialogueManager = dialogueManager3;
                    DialogueManager dialogueManager4 = this;
                    dialogueManager = dialogueManager4;
                    DialogueManager dialogueManager5 = this;
                    dialogueManager = dialogueManager5;
                    DialogueManager dialogueManager6 = this;
                    dialogueManager = dialogueManager6;
                    dialogueManager = this;
                    DialogueManager.continueContextDialogue(dialogueManager3.dialogueContextId, this.player, dialogueManager4.dialogueId, dialogueManager5.dialogueStep + 1, 1, dialogueManager6.dialogueContextX, dialogueManager.dialogueContextY);
                }
                return true;
            }
            case 2462: 
            case 2472: 
            case 2483: 
            case 2495: {
                DialogueManager dialogueManager = this;
                if (dialogueManager.dialogueType == 0) {
                    DialogueManager dialogueManager7 = this;
                    dialogueManager = dialogueManager7;
                    dialogueManager = this;
                    DialogueManager.continueDialogue(this.player, dialogueManager7.dialogueId, dialogueManager.dialogueStep + 1, 2);
                }
                dialogueManager = this;
                if (dialogueManager.dialogueType == 1) {
                    DialogueManager dialogueManager8 = this;
                    dialogueManager = dialogueManager8;
                    DialogueManager dialogueManager9 = this;
                    dialogueManager = dialogueManager9;
                    DialogueManager dialogueManager10 = this;
                    dialogueManager = dialogueManager10;
                    DialogueManager dialogueManager11 = this;
                    dialogueManager = dialogueManager11;
                    dialogueManager = this;
                    DialogueManager.continueContextDialogue(dialogueManager8.dialogueContextId, this.player, dialogueManager9.dialogueId, dialogueManager10.dialogueStep + 1, 2, dialogueManager11.dialogueContextX, dialogueManager.dialogueContextY);
                }
                return true;
            }
            case 2473: 
            case 2484: 
            case 2496: {
                DialogueManager dialogueManager = this;
                if (dialogueManager.dialogueType == 0) {
                    DialogueManager dialogueManager12 = this;
                    dialogueManager = dialogueManager12;
                    dialogueManager = this;
                    DialogueManager.continueDialogue(this.player, dialogueManager12.dialogueId, dialogueManager.dialogueStep + 1, 3);
                }
                dialogueManager = this;
                if (dialogueManager.dialogueType == 1) {
                    DialogueManager dialogueManager13 = this;
                    dialogueManager = dialogueManager13;
                    DialogueManager dialogueManager14 = this;
                    dialogueManager = dialogueManager14;
                    DialogueManager dialogueManager15 = this;
                    dialogueManager = dialogueManager15;
                    DialogueManager dialogueManager16 = this;
                    dialogueManager = dialogueManager16;
                    dialogueManager = this;
                    DialogueManager.continueContextDialogue(dialogueManager13.dialogueContextId, this.player, dialogueManager14.dialogueId, dialogueManager15.dialogueStep + 1, 3, dialogueManager16.dialogueContextX, dialogueManager.dialogueContextY);
                }
                return true;
            }
            case 2485: 
            case 2497: {
                DialogueManager dialogueManager = this;
                if (dialogueManager.dialogueType == 0) {
                    DialogueManager dialogueManager17 = this;
                    dialogueManager = dialogueManager17;
                    dialogueManager = this;
                    DialogueManager.continueDialogue(this.player, dialogueManager17.dialogueId, dialogueManager.dialogueStep + 1, 4);
                }
                dialogueManager = this;
                if (dialogueManager.dialogueType == 1) {
                    DialogueManager dialogueManager18 = this;
                    dialogueManager = dialogueManager18;
                    DialogueManager dialogueManager19 = this;
                    dialogueManager = dialogueManager19;
                    DialogueManager dialogueManager20 = this;
                    dialogueManager = dialogueManager20;
                    DialogueManager dialogueManager21 = this;
                    dialogueManager = dialogueManager21;
                    dialogueManager = this;
                    DialogueManager.continueContextDialogue(dialogueManager18.dialogueContextId, this.player, dialogueManager19.dialogueId, dialogueManager20.dialogueStep + 1, 4, dialogueManager21.dialogueContextX, dialogueManager.dialogueContextY);
                }
                return true;
            }
            case 2498: {
                DialogueManager dialogueManager = this;
                if (dialogueManager.dialogueType == 0) {
                    DialogueManager dialogueManager22 = this;
                    dialogueManager = dialogueManager22;
                    dialogueManager = this;
                    DialogueManager.continueDialogue(this.player, dialogueManager22.dialogueId, dialogueManager.dialogueStep + 1, 5);
                }
                dialogueManager = this;
                if (dialogueManager.dialogueType == 1) {
                    DialogueManager dialogueManager23 = this;
                    dialogueManager = dialogueManager23;
                    DialogueManager dialogueManager24 = this;
                    dialogueManager = dialogueManager24;
                    DialogueManager dialogueManager25 = this;
                    dialogueManager = dialogueManager25;
                    DialogueManager dialogueManager26 = this;
                    dialogueManager = dialogueManager26;
                    dialogueManager = this;
                    DialogueManager.continueContextDialogue(dialogueManager23.dialogueContextId, this.player, dialogueManager24.dialogueId, dialogueManager25.dialogueStep + 1, 5, dialogueManager26.dialogueContextX, dialogueManager.dialogueContextY);
                }
                return true;
            }
        }
        return false;
    }

    public final void showTwoOptionsWithTitle(String text4, String text22, String text32) {
        Player player = this.player;
        player.packetSender.sendInterfaceText(text22, 2461);
        player = this.player;
        player.packetSender.sendInterfaceText(text32, 2462);
        player = this.player;
        player.packetSender.sendInterfaceText(text4, 2460);
        player = this.player;
        player.packetSender.setInterfaceHiddenFlag(1, 2465);
        player = this.player;
        player.packetSender.setInterfaceHiddenFlag(0, 2468);
        player = this.player;
        player.packetSender.showChatboxInterface(2459);
        if (ServerSettings.clientBuild == 443) {
            player.packetSender.sendInterfaceText(text22, 2461);
            player.packetSender.sendInterfaceText(text32, 2462);
            player.packetSender.sendInterfaceText(text4, 2460);
        }
    }

    public final void showThreeOptionsWithTitle(String text5, String text22, String text32, String text42) {
        Player player = this.player;
        player.packetSender.sendInterfaceText(text22, 2471);
        player = this.player;
        player.packetSender.sendInterfaceText(text32, 2472);
        player = this.player;
        player.packetSender.sendInterfaceText(text42, 2473);
        player = this.player;
        player.packetSender.sendInterfaceText(text5, 2470);
        player = this.player;
        player.packetSender.setInterfaceHiddenFlag(1, 2476);
        player = this.player;
        player.packetSender.setInterfaceHiddenFlag(0, 2479);
        player = this.player;
        player.packetSender.showChatboxInterface(2469);
        if (ServerSettings.clientBuild == 443) {
            player.packetSender.sendInterfaceText(text22, 2471);
            player.packetSender.sendInterfaceText(text32, 2472);
            player.packetSender.sendInterfaceText(text42, 2473);
            player.packetSender.sendInterfaceText(text5, 2470);
        }
    }

    public final void showFourOptionsWithTitle(String text6, String text22, String text32, String text42, String text52) {
        Player player = this.player;
        player.packetSender.sendInterfaceText(text22, 2482);
        player = this.player;
        player.packetSender.sendInterfaceText(text32, 2483);
        player = this.player;
        player.packetSender.sendInterfaceText(text42, 2484);
        player = this.player;
        player.packetSender.sendInterfaceText(text52, 2485);
        player = this.player;
        player.packetSender.sendInterfaceText(text6, 2481);
        player = this.player;
        player.packetSender.setInterfaceHiddenFlag(1, 2488);
        player = this.player;
        player.packetSender.setInterfaceHiddenFlag(0, 2489);
        player = this.player;
        player.packetSender.showChatboxInterface(2480);
        if (ServerSettings.clientBuild == 443) {
            player.packetSender.sendInterfaceText(text22, 2482);
            player.packetSender.sendInterfaceText(text32, 2483);
            player.packetSender.sendInterfaceText(text42, 2484);
            player.packetSender.sendInterfaceText(text52, 2485);
            player.packetSender.sendInterfaceText(text6, 2481);
        }
    }

    public final void showFiveOptionsWithTitle(String text7, String text22, String text32, String text42, String text52, String text62) {
        Player player = this.player;
        player.packetSender.sendInterfaceText(text22, 2494);
        player = this.player;
        player.packetSender.sendInterfaceText(text32, 2495);
        player = this.player;
        player.packetSender.sendInterfaceText(text42, 2496);
        player = this.player;
        player.packetSender.sendInterfaceText(text52, 2497);
        player = this.player;
        player.packetSender.sendInterfaceText(text62, 2498);
        player = this.player;
        player.packetSender.sendInterfaceText(text7, 2493);
        player = this.player;
        player.packetSender.setInterfaceHiddenFlag(1, 2501);
        player = this.player;
        player.packetSender.setInterfaceHiddenFlag(0, 2502);
        player = this.player;
        player.packetSender.showChatboxInterface(2492);
        if (ServerSettings.clientBuild == 443) {
            player.packetSender.sendInterfaceText(text22, 2494);
            player.packetSender.sendInterfaceText(text32, 2495);
            player.packetSender.sendInterfaceText(text42, 2496);
            player.packetSender.sendInterfaceText(text52, 2497);
            player.packetSender.sendInterfaceText(text62, 2498);
            player.packetSender.sendInterfaceText(text7, 2493);
        }
    }

    public final void showTwoOptions(String text3, String text22) {
        this.traceDisplay("options2", text3 + " | " + text22);
        Player player = this.player;
        player.packetSender.sendInterfaceText(text3, 2461);
        player = this.player;
        player.packetSender.sendInterfaceText(text22, 2462);
        player = this.player;
        player.packetSender.sendInterfaceText("Select an Option", 2460);
        player = this.player;
        player.packetSender.setInterfaceHiddenFlag(0, 2465);
        player = this.player;
        player.packetSender.setInterfaceHiddenFlag(1, 2468);
        player = this.player;
        player.packetSender.showChatboxInterface(2459);
        if (ServerSettings.clientBuild == 443) {
            player.packetSender.sendInterfaceText(text3, 2461);
            player.packetSender.sendInterfaceText(text22, 2462);
            player.packetSender.sendInterfaceText("Select an Option", 2460);
        }
    }

    public final void showThreeOptions(String text4, String text22, String text32) {
        this.traceDisplay("options3", text4 + " | " + text22 + " | " + text32);
        Player player = this.player;
        player.packetSender.sendInterfaceText(text4, 2471);
        player = this.player;
        player.packetSender.sendInterfaceText(text22, 2472);
        player = this.player;
        player.packetSender.sendInterfaceText(text32, 2473);
        player = this.player;
        player.packetSender.sendInterfaceText("Select an Option", 2470);
        player = this.player;
        player.packetSender.setInterfaceHiddenFlag(0, 2476);
        player = this.player;
        player.packetSender.setInterfaceHiddenFlag(1, 2479);
        player = this.player;
        player.packetSender.showChatboxInterface(2469);
        if (ServerSettings.clientBuild == 443) {
            player.packetSender.sendInterfaceText(text4, 2471);
            player.packetSender.sendInterfaceText(text22, 2472);
            player.packetSender.sendInterfaceText(text32, 2473);
            player.packetSender.sendInterfaceText("Select an Option", 2470);
        }
    }

    public final void showFourOptions(String text5, String text22, String text32, String text42) {
        this.traceDisplay("options4", text5 + " | " + text22 + " | " + text32 + " | " + text42);
        Player player = this.player;
        player.packetSender.sendInterfaceText(text5, 2482);
        player = this.player;
        player.packetSender.sendInterfaceText(text22, 2483);
        player = this.player;
        player.packetSender.sendInterfaceText(text32, 2484);
        player = this.player;
        player.packetSender.sendInterfaceText(text42, 2485);
        player = this.player;
        player.packetSender.sendInterfaceText("Select an Option", 2481);
        player = this.player;
        player.packetSender.setInterfaceHiddenFlag(0, 2488);
        player = this.player;
        player.packetSender.setInterfaceHiddenFlag(1, 2489);
        player = this.player;
        player.packetSender.showChatboxInterface(2480);
        if (ServerSettings.clientBuild == 443) {
            player.packetSender.sendInterfaceText(text5, 2482);
            player.packetSender.sendInterfaceText(text22, 2483);
            player.packetSender.sendInterfaceText(text32, 2484);
            player.packetSender.sendInterfaceText(text42, 2485);
            player.packetSender.sendInterfaceText("Select an Option", 2481);
        }
    }

    public final void showFiveOptions(String text6, String text22, String text32, String text42, String text52) {
        this.traceDisplay("options5", text6 + " | " + text22 + " | " + text32 + " | " + text42 + " | " + text52);
        Player player = this.player;
        player.packetSender.sendInterfaceText(text6, 2494);
        player = this.player;
        player.packetSender.sendInterfaceText(text22, 2495);
        player = this.player;
        player.packetSender.sendInterfaceText(text32, 2496);
        player = this.player;
        player.packetSender.sendInterfaceText(text42, 2497);
        player = this.player;
        player.packetSender.sendInterfaceText(text52, 2498);
        player = this.player;
        player.packetSender.sendInterfaceText("Select an Option", 2493);
        player = this.player;
        player.packetSender.setInterfaceHiddenFlag(0, 2501);
        player = this.player;
        player.packetSender.setInterfaceHiddenFlag(1, 2502);
        player = this.player;
        player.packetSender.showChatboxInterface(2492);
        if (ServerSettings.clientBuild == 443) {
            player.packetSender.sendInterfaceText(text6, 2494);
            player.packetSender.sendInterfaceText(text22, 2495);
            player.packetSender.sendInterfaceText(text32, 2496);
            player.packetSender.sendInterfaceText(text42, 2497);
            player.packetSender.sendInterfaceText(text52, 2498);
            player.packetSender.sendInterfaceText("Select an Option", 2493);
        }
    }

    public final void showOptions(String[] stringValues2) {
        switch (stringValues2.length) {
            case 2: {
                this.showTwoOptions(stringValues2[0], stringValues2[1]);
                return;
            }
            case 3: {
                this.showThreeOptions(stringValues2[0], stringValues2[1], stringValues2[2]);
                return;
            }
            case 4: {
                this.showFourOptions(stringValues2[0], stringValues2[1], stringValues2[2], stringValues2[3]);
                return;
            }
            case 5: {
                this.showFiveOptions(stringValues2[0], stringValues2[1], stringValues2[2], stringValues2[3], stringValues2[4]);
            }
        }
    }

    public final void showStatement(String[] state) {
        switch (state.length) {
            case 1: {
                this.showOneLineStatement(state[0]);
                return;
            }
            case 2: {
                this.showTwoLineStatement(state[0], state[1]);
                return;
            }
            case 3: {
                this.showThreeLineStatement(state[0], state[1], state[2]);
                return;
            }
            case 4: {
                this.showFourLineStatement(state[0], state[1], state[2], state[3]);
                return;
            }
            case 5: {
                this.showFiveLineStatement(state[0], state[1], state[2], state[3], state[4]);
            }
        }
    }

    public final void showOneLineStatement(String state) {
        this.traceDisplay("statement1", state);
        Object value = this.player;
        ((Player)value).packetSender.sendInterfaceText(state, 357);
        value = this.player;
        ((Player)value).packetSender.showChatboxInterface(356);
        if (this.player.getQuestState(0) != 1) {
            value = this.player.getDialogueManager();
            this.player.getDialogueManager().dialogueStep = 9001;
        }
    }

    public final void showItemIdMessage(String itemId, int itemId2) {
        Object value = this.player;
        ((Player)value).packetSender.sendInterfaceModel(307, 150, itemId2);
        value = this.player;
        ((Player)value).packetSender.sendInterfaceText(itemId, 308);
        value = this.player;
        ((Player)value).packetSender.showChatboxInterface(306);
        if (this.player.getQuestState(0) != 1) {
            value = this.player.getDialogueManager();
            this.player.getDialogueManager().dialogueStep = 9001;
        }
    }

    public final void showTwoLineStatement(String state, String text22) {
        this.traceDisplay("statement2", state + " | " + text22);
        Object value = this.player;
        ((Player)value).packetSender.sendInterfaceText(state, 360);
        value = this.player;
        ((Player)value).packetSender.sendInterfaceText(text22, 361);
        value = this.player;
        ((Player)value).packetSender.showChatboxInterface(359);
        if (this.player.getQuestState(0) != 1) {
            value = this.player.getDialogueManager();
            this.player.getDialogueManager().dialogueStep = 9001;
        }
    }

    public final void showThreeLineStatement(String state, String text22, String text32) {
        Object value = this.player;
        ((Player)value).packetSender.sendInterfaceText(state, 364);
        value = this.player;
        ((Player)value).packetSender.sendInterfaceText(text22, 365);
        value = this.player;
        ((Player)value).packetSender.sendInterfaceText(text32, 366);
        value = this.player;
        ((Player)value).packetSender.showChatboxInterface(363);
        if (this.player.getQuestState(0) != 1) {
            value = this.player.getDialogueManager();
            this.player.getDialogueManager().dialogueStep = 9001;
        }
    }

    public final void showFourLineStatement(String state, String text22, String text32, String text42) {
        Object value = this.player;
        ((Player)value).packetSender.sendInterfaceText(state, 369);
        value = this.player;
        ((Player)value).packetSender.sendInterfaceText(text22, 370);
        value = this.player;
        ((Player)value).packetSender.sendInterfaceText(text32, 371);
        value = this.player;
        ((Player)value).packetSender.sendInterfaceText(text42, 372);
        value = this.player;
        ((Player)value).packetSender.showChatboxInterface(368);
        if (this.player.getQuestState(0) != 1) {
            value = this.player.getDialogueManager();
            this.player.getDialogueManager().dialogueStep = 9001;
        }
    }

    public final void showFiveLineStatement(String state, String text22, String text32, String text42, String text52) {
        Object value = this.player;
        if (ServerSettings.clientBuild == 443) {
            ((Player)value).packetSender.sendInterfaceText("__dialogue__", 6185);
        }
        ((Player)value).packetSender.sendInterfaceText(state, 375);
        value = this.player;
        ((Player)value).packetSender.sendInterfaceText(text22, 376);
        value = this.player;
        ((Player)value).packetSender.sendInterfaceText(text32, 377);
        value = this.player;
        ((Player)value).packetSender.sendInterfaceText(text42, 378);
        value = this.player;
        ((Player)value).packetSender.sendInterfaceText(text52, 379);
        value = this.player;
        ((Player)value).packetSender.showChatboxInterface(374);
        if (this.player.getQuestState(0) != 1) {
            value = this.player.getDialogueManager();
            this.player.getDialogueManager().dialogueStep = 9001;
        }
    }

    public final void showTutorialWelcomeStatement(String line1, String line2, String line3,
                                                   String line4, String line5) {
        this.resetDialogueState();
        this.showFiveLineStatement(line1, line2, line3, line4, line5);
        this.finishDialogue();
        this.tutorialWelcomePending = true;
    }

    public final void showOneLineChatboxMessage(String text2) {
        Player player = this.player;
        player.packetSender.sendInterfaceText(text2, 12789);
        player = this.player;
        player.packetSender.showChatboxInterface(12788);
    }

    public final void showNpcDialogue(String[] npcId, int npcId2) {
        switch (npcId.length) {
            case 1: {
                this.showNpcOneLineDialogue(npcId[0], 588);
                return;
            }
            case 2: {
                this.showNpcTwoLineDialogue(npcId[0], npcId[1], 588);
                return;
            }
            case 3: {
                this.showNpcThreeLineDialogue(npcId[0], npcId[1], npcId[2], 588);
                return;
            }
            case 4: {
                this.showNpcFourLineDialogue(npcId[0], npcId[1], npcId[2], npcId[3], 588);
            }
        }
    }

    public final void showNpcOneLineDialogue(String line, int animationId) {
        this.traceDisplay("npc1", line);
        int npcId = this.dialogueNpcId < 0 || this.dialogueNpcId > 6433 ? 0 : this.dialogueNpcId;
        String npcName = World.getNpcDefinitions()[npcId].getName();
        this.player.packetSender.sendInterfaceAnimation(4883, animationId);
        this.player.packetSender.sendInterfaceText(npcName, 4884);
        this.player.packetSender.sendInterfaceText(line, 4885);
        this.player.packetSender.sendNpcHeadOnInterface(npcId, 4883);
        this.player.packetSender.showChatboxInterface(4882);
    }

    public final void showNpcTwoLineDialogue(String line1, String line2, int animationId) {
        this.traceDisplay("npc2", line1 + " | " + line2);
        int npcId = this.dialogueNpcId < 0 || this.dialogueNpcId > 6433 ? 0 : this.dialogueNpcId;
        String npcName = World.getNpcDefinitions()[npcId].getName();
        this.player.packetSender.sendInterfaceAnimation(4888, animationId);
        this.player.packetSender.sendInterfaceText(npcName, 4889);
        this.player.packetSender.sendInterfaceText(line1, 4890);
        this.player.packetSender.sendInterfaceText(line2, 4891);
        this.player.packetSender.sendNpcHeadOnInterface(npcId, 4888);
        this.player.packetSender.showChatboxInterface(4887);
    }

    public final void showNpcThreeLineDialogue(String line1, String line2, String line3, int animationId) {
        this.traceDisplay("npc3", line1 + " | " + line2 + " | " + line3);
        int npcId = this.dialogueNpcId < 0 || this.dialogueNpcId > 6433 ? 0 : this.dialogueNpcId;
        String npcName = World.getNpcDefinitions()[npcId].getName();
        this.player.packetSender.sendInterfaceAnimation(4894, animationId);
        this.player.packetSender.sendInterfaceText(npcName, 4895);
        this.player.packetSender.sendInterfaceText(line1, 4896);
        this.player.packetSender.sendInterfaceText(line2, 4897);
        this.player.packetSender.sendInterfaceText(line3, 4898);
        this.player.packetSender.sendNpcHeadOnInterface(npcId, 4894);
        this.player.packetSender.showChatboxInterface(4893);
    }

    public final void showNpcFourLineDialogue(String line1, String line2, String line3, String line4, int animationId) {
        this.traceDisplay("npc4", line1 + " | " + line2 + " | " + line3 + " | " + line4);
        int npcId = this.dialogueNpcId < 0 || this.dialogueNpcId > 6433 ? 0 : this.dialogueNpcId;
        String npcName = World.getNpcDefinitions()[npcId].getName();
        this.player.packetSender.sendInterfaceAnimation(4901, animationId);
        this.player.packetSender.sendInterfaceText(npcName, 4902);
        this.player.packetSender.sendInterfaceText(line1, 4903);
        this.player.packetSender.sendInterfaceText(line2, 4904);
        this.player.packetSender.sendInterfaceText(line3, 4905);
        this.player.packetSender.sendInterfaceText(line4, 4906);
        this.player.packetSender.sendNpcHeadOnInterface(npcId, 4901);
        this.player.packetSender.showChatboxInterface(4900);
    }

    public final void showGuideIconFourLineDialogue(String line1, String line2, String line3, String line4) {
        this.traceDisplay("guide-icon4", line1 + " | " + line2 + " | " + line3 + " | " + line4);
        this.player.packetSender.sendInterfaceText("", 4902);
        this.player.packetSender.sendInterfaceText(line1, 4903);
        this.player.packetSender.sendInterfaceText(line2, 4904);
        this.player.packetSender.sendInterfaceText(line3, 4905);
        this.player.packetSender.sendInterfaceText(line4, 4906);
        // Revision 443 reserves NPC id 65535 here for the mapfunction Guide (?) icon.
        this.player.packetSender.sendNpcHeadOnInterface(0xFFFF, 4901);
        this.player.packetSender.showChatboxInterface(4900);
    }

    public final void showAlternateNpcThreeLineDialogue(String line1, String line2, String line3, int animationId) {
        int npcId = this.dialogueNpcId < 0 || this.dialogueNpcId > 6433 ? 0 : this.dialogueNpcId;
        String npcName = World.getNpcDefinitions()[npcId].getName();
        this.player.packetSender.sendInterfaceAnimation(12384, 591);
        this.player.packetSender.sendInterfaceText(npcName, 12385);
        this.player.packetSender.sendInterfaceText(line1, 12386);
        this.player.packetSender.sendInterfaceText(line2, 12387);
        this.player.packetSender.sendInterfaceText(line3, 12388);
        this.player.packetSender.sendNpcHeadOnInterface(npcId, 12384);
        this.player.packetSender.showChatboxInterface(12383);
    }

    public final void showPlayerOneLineDialogue(String text2, int value2) {
        this.traceDisplay("player1", text2);
        Player player = this.player;
        player.packetSender.sendInterfaceAnimation(969, value2);
        player = this.player;
        player.packetSender.sendInterfaceText(GameUtil.formatDisplayName(this.player.getUsername()), 970);
        player = this.player;
        player.packetSender.sendInterfaceText(text2, 971);
        player = this.player;
        player.packetSender.sendPlayerHeadOnInterface(969);
        player = this.player;
        player.packetSender.showChatboxInterface(968);
    }

    public final void showPlayerTwoLineDialogue(String text3, String text22, int value2) {
        this.traceDisplay("player2", text3 + " | " + text22);
        Player player = this.player;
        player.packetSender.sendInterfaceAnimation(974, value2);
        player = this.player;
        player.packetSender.sendInterfaceText(GameUtil.formatDisplayName(this.player.getUsername()), 975);
        player = this.player;
        player.packetSender.sendInterfaceText(text3, 976);
        player = this.player;
        player.packetSender.sendInterfaceText(text22, 977);
        player = this.player;
        player.packetSender.sendPlayerHeadOnInterface(974);
        player = this.player;
        player.packetSender.showChatboxInterface(973);
    }

    public final void showPlayerThreeLineDialogue(String text4, String text22, String text32, int value2) {
        this.traceDisplay("player3", text4 + " | " + text22 + " | " + text32);
        Player player = this.player;
        player.packetSender.sendInterfaceAnimation(980, 591);
        player = this.player;
        player.packetSender.sendInterfaceText(GameUtil.formatDisplayName(this.player.getUsername()), 981);
        player = this.player;
        player.packetSender.sendInterfaceText(text4, 982);
        player = this.player;
        player.packetSender.sendInterfaceText(text22, 983);
        player = this.player;
        player.packetSender.sendInterfaceText(text32, 984);
        player = this.player;
        player.packetSender.sendPlayerHeadOnInterface(980);
        player = this.player;
        player.packetSender.showChatboxInterface(979);
    }

    public final void showPlayerFourLineDialogue(String text5, String text22, String text32, String text42, int value2) {
        this.traceDisplay("player4", text5 + " | " + text22 + " | " + text32 + " | " + text42);
        Player player = this.player;
        player.packetSender.sendInterfaceAnimation(987, 591);
        player = this.player;
        player.packetSender.sendInterfaceText(GameUtil.formatDisplayName(this.player.getUsername()), 988);
        player = this.player;
        player.packetSender.sendInterfaceText(text5, 989);
        player = this.player;
        player.packetSender.sendInterfaceText(text22, 990);
        player = this.player;
        player.packetSender.sendInterfaceText(text32, 991);
        player = this.player;
        player.packetSender.sendInterfaceText(text42, 992);
        player = this.player;
        player.packetSender.sendPlayerHeadOnInterface(987);
        player = this.player;
        player.packetSender.showChatboxInterface(986);
    }

    public final void showTutorialInstructionOverlay(String text6, String text22, String text32, String text42, String text52, boolean enabled2) {
        this.showTutorialInstructionOverlay(text6, text22, text32, text42, text52, "", enabled2);
    }

    public final void showTutorialInstructionOverlay(String text6, String text22, String text32, String text42, String text52, String text62, boolean enabled2) {
        if (ServerSettings.clientBuild == 443 && !text62.isEmpty()) {
            this.showScrollableTutorialInstructionOverlay(text6, text22, text32, text42,
                    text52, text62, "", enabled2);
            return;
        }
        Player player = this.player;
        if (ServerSettings.clientBuild == 443) {
            player.packetSender.sendInterfaceText("__tutorial__", 6185);
        }
        player.packetSender.sendInterfaceText(
                text6.isEmpty() || text6.startsWith("@") ? text6 : "@blu@" + text6, 6180);
        player = this.player;
        player.packetSender.sendInterfaceText(text22, 6181);
        player = this.player;
        player.packetSender.sendInterfaceText(text32, 6182);
        player = this.player;
        player.packetSender.sendInterfaceText(text42, 6183);
        player = this.player;
        player.packetSender.sendInterfaceText(text52, 6184);
        player = this.player;
        player.packetSender.sendInterfaceText(text62, 6185);
        // Revision 443 redraws an already-open chatbox group when the same
        // interface-open packet is received again. Tutorial stages frequently
        // refresh while 6179 (native group 214) is still visible, which causes
        // the instruction panel to flash. Update its text in place instead.
        if (player.getOpenInterfaceId() != 6179) {
            player.packetSender.showChatboxInterface(6179);
        }
    }

    public final void showScrollableTutorialInstructionOverlay(String title,
            String line1, String line2, String line3, String line4,
            String line5, String line6, boolean enabled) {
        if (ServerSettings.clientBuild != 443) {
            // Legacy clients only expose the original six text slots.
            this.showTutorialInstructionOverlay(title, line1, line2, line3,
                    line4, line5 + " " + line6, enabled);
            return;
        }
        Player player = this.player;
        player.packetSender.sendInterfaceText("__tutorial__", 6185);
        player.packetSender.sendInterfaceText(
                title.isEmpty() || title.startsWith("@") ? title : "@blu@" + title, 6180);
        player.packetSender.sendInterfaceText(line1, 6181);
        player.packetSender.sendInterfaceText(line2, 6182);
        player.packetSender.sendInterfaceText(line3, 6183);
        player.packetSender.sendInterfaceText(line4, 6184);
        // Native group 214 child 5 is the stock blue continue row. Keep that
        // row empty and use two normal-style synthetic rows for overflow text.
        player.packetSender.sendInterfaceText("", 6185);
        player.packetSender.sendInterfaceText(line5, 6186);
        player.packetSender.sendInterfaceText(line6, 6187);
        player.packetSender.sendInterfaceScrollPosition(6188, 0);
        if (player.getOpenInterfaceId() != 6179) {
            player.packetSender.showChatboxInterface(6179);
        }
    }

    public final void showTwoItemMessage(String itemId, String value2, ItemStack itemStack, ItemStack itemStack2) {
        // Restore the standard placement after item-specific preview adjustments.
        this.player.packetSender.sendInterfacePosition(4957, 0, 0);
        Player player = this.player;
        player.packetSender.sendInterfaceText("", 4953);
        player = this.player;
        player.packetSender.sendInterfaceText(itemId, 4952);
        player = this.player;
        player.packetSender.sendInterfaceText(value2, 4955);
        player = this.player;
        player.packetSender.sendInterfaceText("", 4956);
        player = this.player;
        player.packetSender.sendInterfaceModel(4951, 170, itemStack.getId());
        player = this.player;
        player.packetSender.sendInterfaceModel(4957, 170, itemStack2.getId());
        player = this.player;
        player.packetSender.showChatboxInterface(4950);
    }

    public final void showItemMessage(String itemId, ItemStack itemStack) {
        Player player = this.player;
        player.packetSender.sendInterfaceText(itemId, 308);
        player = this.player;
        player.packetSender.sendInterfaceModel(307, 200, itemStack.getId());
        player = this.player;
        player.packetSender.showChatboxInterface(306);
    }

    public final void showThreeLineItemMessage(String itemId, String value2, String text32, ItemStack itemStack) {
        Player player = this.player;
        player.packetSender.sendInterfaceText(itemId, 318);
        player = this.player;
        player.packetSender.sendInterfaceText(value2, 317);
        player = this.player;
        player.packetSender.sendInterfaceText(text32, 320);
        player = this.player;
        player.packetSender.sendInterfaceModel(316, 200, itemStack.getId());
        player = this.player;
        player.packetSender.showChatboxInterface(315);
    }

    public final void setDialogueNpcId(int npcId) {
        this.dialogueNpcId = npcId;
    }

    public static boolean startDialogue(Player player, int value2) {
        if (GameplayTrace.enabled()) {
            GameplayTrace.log("dialogue start player=" + GameplayTrace.describe(player) + " id=" + value2);
        }
        player.getDialogueManager().resetDialogueState();
        return DialogueManager.continueDialogue(player, value2, 1, 0);
    }

    public static boolean continueDialogue(Player player, int value4, int value22, int value32) {
        if (GameplayTrace.enabled()) {
            GameplayTrace.log("dialogue continue player=" + GameplayTrace.describe(player) + " id=" + value4 + " step=" + value22 + " option=" + value32);
        }
        return DialogueManager.continueDialogueWithNpcId(player, value4, value22, value32, value4);
    }

    public static void prepareAnagramClueDialogue(Player player, int nextStep) {
        DialogueManager dialogueManager = player.getDialogueManager();
        dialogueManager.dialogueId = 10009;
        dialogueManager.dialogueStep = nextStep - 1;
    }

    public static boolean startContextDialogue(int value5, Player player, int value22, int value32, int value42) {
        player.getDialogueManager().resetDialogueState();
        return DialogueManager.continueContextDialogue(value5, player, value22, 1, 0, value32, value42);
    }

    /*
     * Enabled aggressive block sorting
     */
    public static boolean continueContextDialogue(int value9, Player player, int value22, int value32, int value42, int value52, int value62) {
        int value7 = value62;
        value62 = value52;
        value52 = value22;
        int value8 = value32;
        Object dialogueManager = player.getDialogueManager();
        player.getDialogueManager().dialogueStep = value8;
        value8 = value9;
        dialogueManager = player.getDialogueManager();
        player.getDialogueManager().dialogueContextId = value8;
        value8 = value22;
        dialogueManager = player.getDialogueManager();
        player.getDialogueManager().dialogueId = value8;
        value8 = 1;
        dialogueManager = player.getDialogueManager();
        player.getDialogueManager().dialogueType = value8;
        value8 = value62;
        dialogueManager = player.getDialogueManager();
        player.getDialogueManager().dialogueContextX = value8;
        value8 = value7;
        dialogueManager = player.getDialogueManager();
        player.getDialogueManager().dialogueContextY = value8;
        value8 = value52;
        dialogueManager = player.getDialogueManager();
        player.getDialogueManager().dialogueNpcId = value8;
        if (player.getQuestManager().handleContextDialogue(value9, value22, value32, value42, value52, value62, value7)) {
            return true;
        }
        continueContextDialogueControlSwitch1 : switch (value22) {
            case 2411: 
            case 12045: 
            case 12047: {
                value9 = 3307;
                if (!NpcDefinition.isDefined(3307)) {
                    value9 = 33;
                }
                value8 = value9;
                dialogueManager = player.getDialogueManager();
                player.getDialogueManager().dialogueNpcId = value8;
                dialogueManager = player.getDialogueManager();
                switch (((DialogueManager)dialogueManager).dialogueStep) {
                    case 1: {
                        player.getDialogueManager().showNpcTwoLineDialogue("You may not pass through this door without paying the", "trading tax.", 591);
                        return true;
                    }
                    case 2: {
                        player.getDialogueManager().showPlayerOneLineDialogue("So how much is the tax?", 591);
                        return true;
                    }
                    case 3: {
                        player.getDialogueManager().showNpcOneLineDialogue("The cost is one diamond.", 591);
                        return true;
                    }
                    case 4: {
                        player.getDialogueManager().showFourOptions("Okay...", "A diamond? Are you crazy?", "I haven't brought my diamonds with me.", "What do you do with all the diamonds you get?");
                        return true;
                    }
                    case 5: {
                        switch (value42) {
                            case 1: {
                                player.getDialogueManager().showPlayerOneLineDialogue("Okay...", 591);
                                return true;
                            }
                        }
                        break continueContextDialogueControlSwitch1;
                    }
                    case 6: {
                        if (!player.getInventoryManager().containsItemAmount(1601, 1)) {
                            player.getDialogueManager().showNpcOneLineDialogue("You don't have a diamond with you!", 591);
                            dialogueManager = player.getDialogueManager();
                            player.getDialogueManager().dialogueStep = 9001;
                            return true;
                        }
                        player.getInventoryManager().removeItem(new ItemStack(1601, 1));
                        if (value22 == 2411) {
                            value9 = 0;
                            value22 = 0;
                            if (CacheCoordinateTranslator.dungeonCoordinateShiftActive) {
                                value9 = 768;
                                value22 = 5120;
                            }
                            if (value62 == value9 + 2470) {
                                dialogueManager = player;
                                ((Player)dialogueManager).packetSender.queueRelativeMovementStep(1, 0, true);
                                dialogueManager = player;
                                ((Player)dialogueManager).packetSender.openSingleDoor(2411, value9 + 2470, value22 + 4438, 0);
                                break continueContextDialogueControlSwitch1;
                            }
                            dialogueManager = player;
                            ((Player)dialogueManager).packetSender.queueRelativeMovementStep(0, -1, true);
                            dialogueManager = player;
                            ((Player)dialogueManager).packetSender.openSingleDoor(2411, value9 + 2465, value22 + 4434, 0);
                            break continueContextDialogueControlSwitch1;
                        }
                        if (value62 == 2469) {
                            dialogueManager = player;
                            ((Player)dialogueManager).packetSender.queueRelativeMovementStep(1, 0, true);
                            dialogueManager = player;
                            ((Player)dialogueManager).packetSender.openDoubleDoorPair(12045, 2469, 4438, 12047, 2469, 4437);
                            break continueContextDialogueControlSwitch1;
                        }
                        dialogueManager = player;
                        ((Player)dialogueManager).packetSender.queueRelativeMovementStep(0, -1, true);
                        dialogueManager = player;
                        ((Player)dialogueManager).packetSender.openDoubleDoorPair(12045, 2466, 4434, 12047, 2465, 4434);
                        break continueContextDialogueControlSwitch1;
                    }
                }
                break;
            }
            case 2416: {
                dialogueManager = player.getDialogueManager();
                continueContextDialogueControlSwitch2 : switch (((DialogueManager)dialogueManager).dialogueStep) {
                    case 1: {
                        player.getDialogueManager().showThreeOptions("Balloon Bonanza.", "Nightly Dance.", "No action.");
                        return true;
                    }
                    case 2: {
                        switch (value42) {
                            case 1: {
                                if (!player.getInventoryManager().containsItemAmount(995, 1000)) {
                                    player.getDialogueManager().showOneLineStatement("Balloon Bonanza costs 1000 coins.");
                                    dialogueManager = player.getDialogueManager();
                                    player.getDialogueManager().dialogueStep = 9001;
                                    return true;
                                }
                                if (PartyRoomManager.hasActiveDropParty()) {
                                    player.getDialogueManager().showOneLineStatement("Drop party already in progress!");
                                    dialogueManager = player.getDialogueManager();
                                    player.getDialogueManager().dialogueStep = 9001;
                                    return true;
                                }
                                if (Server.serverStatus == 3) {
                                    player.getDialogueManager().showOneLineStatement("You can't start a party during a system update!");
                                    dialogueManager = player.getDialogueManager();
                                    player.getDialogueManager().dialogueStep = 9001;
                                    return true;
                                }
                                if (PartyRoomManager.partyChestContainer.getFreeSlots() == PartyRoomManager.partyChestContainer.getCapacity()) {
                                    player.getDialogueManager().showOneLineStatement("There are no items to be dropped!");
                                    dialogueManager = player.getDialogueManager();
                                    player.getDialogueManager().dialogueStep = 9001;
                                    return true;
                                }
                                player.getDialogueManager().showTwoOptionsWithTitle("This will cost you 1000 coins.", "Pay to continue.", "Never mind.");
                                value8 = 3;
                                dialogueManager = player.getDialogueManager();
                                player.getDialogueManager().dialogueStep = value8 - 1;
                                return true;
                            }
                            case 2: {
                                if (!player.getInventoryManager().containsItemAmount(995, 500)) {
                                    player.getDialogueManager().showOneLineStatement("Nightly Dance costs 500 coins.");
                                    dialogueManager = player.getDialogueManager();
                                    player.getDialogueManager().dialogueStep = 9001;
                                    return true;
                                }
                                if (PartyRoomManager.startNightlyDance(player)) break;
                                player.getDialogueManager().showOneLineStatement("Dance event already in progress!");
                                dialogueManager = player.getDialogueManager();
                                player.getDialogueManager().dialogueStep = 9001;
                                return true;
                            }
                        }
                        break;
                    }
                    case 3: {
                        switch (value42) {
                            case 1: {
                                PartyRoomManager.startBalloonBonanza(player);
                                break continueContextDialogueControlSwitch2;
                            }
                        }
                    }
                }
                break;
            }
            case 2878: {
                dialogueManager = player.getDialogueManager();
                switch (((DialogueManager)dialogueManager).dialogueStep) {
                    case 1: {
                        if (player.mageArenaProgressStage < 5) return false;
                        player.getDialogueManager().showTwoLineStatement("You step into the pool of sparkling water. You feel energy rush", "through your veins.");
                        return true;
                    }
                    case 2: {
                        player.getTeleportManager().startScriptedTeleport(2509, 4689, 0, null, 5, 804, -1, 68);
                    }
                }
                break;
            }
            case 2879: {
                dialogueManager = player.getDialogueManager();
                switch (((DialogueManager)dialogueManager).dialogueStep) {
                    case 1: {
                        player.getDialogueManager().showTwoLineStatement("You step into the pool of sparkling water. You feel energy rush", "through your veins.");
                        return true;
                    }
                    case 2: {
                        player.getTeleportManager().startScriptedTeleport(2542, 4718, 0, null, 5, 804, -1, 68);
                        break continueContextDialogueControlSwitch1;
                    }
                }
                break;
            }
            case 2874: {
                if (player.hasMageArenaGodCape()) {
                    player.getDialogueManager().showOneLineStatement("You already have a God cape!");
                    dialogueManager = player.getDialogueManager();
                    player.getDialogueManager().dialogueStep = 9001;
                    return true;
                }
                if (value32 == 1) {
                    dialogueManager = player;
                    ((Player)dialogueManager).packetSender.queueAbsoluteMovementStep(2516, 4719);
                }
                dialogueManager = player.getDialogueManager();
                switch (((DialogueManager)dialogueManager).dialogueStep) {
                    case 1: {
                        player.getDialogueManager().showOneLineStatement("You kneel and begin to chant to Zamorak...");
                        return true;
                    }
                    case 2: {
                        player.getUpdateState().setFacePosition(new Position(player.getPosition().getX(), player.getPosition().getY() + 1));
                        player.getUpdateState().setAnimation(645);
                        player.startGodCapeDrop(player, value22);
                        break continueContextDialogueControlSwitch1;
                    }
                    case 10: {
                        player.getDialogueManager().showTwoLineStatement("You feel a rush of energy charge through your veins. Suddenly a", "cape appears before you.");
                        dialogueManager = player.getDialogueManager();
                        player.getDialogueManager().dialogueStep = 9001;
                        return true;
                    }
                }
                break;
            }
            case 2875: {
                if (player.hasMageArenaGodCape()) {
                    player.getDialogueManager().showOneLineStatement("You already have a God cape!");
                    dialogueManager = player.getDialogueManager();
                    player.getDialogueManager().dialogueStep = 9001;
                    return true;
                }
                if (value32 == 1) {
                    dialogueManager = player;
                    ((Player)dialogueManager).packetSender.queueAbsoluteMovementStep(2507, 4722);
                }
                dialogueManager = player.getDialogueManager();
                switch (((DialogueManager)dialogueManager).dialogueStep) {
                    case 1: {
                        player.getDialogueManager().showOneLineStatement("You kneel and begin to chant to Guthix...");
                        return true;
                    }
                    case 2: {
                        player.getUpdateState().setFacePosition(new Position(player.getPosition().getX(), player.getPosition().getY() + 1));
                        player.getUpdateState().setAnimation(645);
                        player.startGodCapeDrop(player, value22);
                        break continueContextDialogueControlSwitch1;
                    }
                    case 10: {
                        player.getDialogueManager().showTwoLineStatement("You feel a rush of energy charge through your veins. Suddenly a", "cape appears before you.");
                        dialogueManager = player.getDialogueManager();
                        player.getDialogueManager().dialogueStep = 9001;
                        return true;
                    }
                }
                break;
            }
            case 2873: {
                if (player.hasMageArenaGodCape()) {
                    player.getDialogueManager().showOneLineStatement("You already have a God cape!");
                    dialogueManager = player.getDialogueManager();
                    player.getDialogueManager().dialogueStep = 9001;
                    return true;
                }
                if (value32 == 1) {
                    dialogueManager = player;
                    ((Player)dialogueManager).packetSender.queueAbsoluteMovementStep(2500, 4719);
                }
                dialogueManager = player.getDialogueManager();
                switch (((DialogueManager)dialogueManager).dialogueStep) {
                    case 1: {
                        player.getDialogueManager().showOneLineStatement("You kneel and begin to chant to Saradomin...");
                        return true;
                    }
                    case 2: {
                        player.getUpdateState().setFacePosition(new Position(player.getPosition().getX(), player.getPosition().getY() + 1));
                        player.getUpdateState().setAnimation(645);
                        player.startGodCapeDrop(player, value22);
                        break continueContextDialogueControlSwitch1;
                    }
                    case 10: {
                        player.getDialogueManager().showTwoLineStatement("You feel a rush of energy charge through your veins. Suddenly a", "cape appears before you.");
                        dialogueManager = player.getDialogueManager();
                        player.getDialogueManager().dialogueStep = 9001;
                        return true;
                    }
                }
            }
        }
        dialogueManager = player.getDialogueManager();
        if (((DialogueManager)dialogueManager).dialogueStep > 1) {
            dialogueManager = player;
            ((Player)dialogueManager).packetSender.closeInterfaces();
        }
        dialogueManager = player.getDialogueManager();
        if (((DialogueManager)dialogueManager).dialogueId < 0) return false;
        player.getDialogueManager().resetDialogueState();
        return false;
    }

    /*
     * Enabled aggressive block sorting
     */
    public static boolean continueUtilityNpcDialogue(Player player, int npcId, int value6) {
        switch (npcId) {
            case 2260: {
                DialogueManager dialogueManager = player.getDialogueManager();
                switch (dialogueManager.dialogueStep) {
                    case 1: {
                        if (!player.isMember()) {
                            player.packetSender.sendGameMessage("You need a members account to access members content.");
                            return true;
                        }
                        if (ServerSettings.freeToPlayWorld) {
                            player.packetSender.sendGameMessage("You need to be in members world to access members content.");
                            return true;
                        }
                        if (player.enterTheAbyssMiniquestState == 4) {
                            player.getDialogueManager().showPlayerTwoLineDialogue("So... that's my end of the deal upheld.", "What do I get in return?", 591);
                            return true;
                        }
                        if (player.enterTheAbyssMiniquestState == 3) {
                            if (!player.ownsItem(5518) && !player.ownsItem(5519)) {
                                player.getDialogueManager().showPlayerOneLineDialogue("I lost the orb.", 591);
                                int value2 = 37;
                                dialogueManager = player.getDialogueManager();
                                player.getDialogueManager().dialogueStep = value2 - 1;
                                return true;
                            }
                            player.getDialogueManager().showNpcThreeLineDialogue("Well?", "Have you managed to use my scrying orb to obtain the", "information yet?", 591);
                            return true;
                        }
                        if (player.enterTheAbyssMiniquestState == 2) {
                            player.getDialogueManager().showNpcFourLineDialogue("Ah, you again.", "What was it you wanted?", "The wilderness is hardly the appropriate place for a", "conversation now, is it?", 591);
                            return true;
                        }
                        player.getDialogueManager().showNpcOneLineDialogue("I'm busy right now.", 591);
                        dialogueManager = player.getDialogueManager();
                        player.getDialogueManager().dialogueStep = 9001;
                        return true;
                    }
                    case 2: {
                        if (player.enterTheAbyssMiniquestState == 4) {
                            player.getDialogueManager().showNpcOneLineDialogue("Indeed, a deal is always a deal.", 591);
                            int value3 = 39;
                            dialogueManager = player.getDialogueManager();
                            player.getDialogueManager().dialogueStep = value3 - 1;
                            return true;
                        }
                        if (player.enterTheAbyssMiniquestState != 3) {
                            player.getDialogueManager().showFourOptions("I'd like to buy some runes!", "Where do you get your runes from?", "All hail Zamorak!", "Nothing, thanks.");
                            return true;
                        }
                        if (!player.ownsItem(5518)) {
                            player.getDialogueManager().showPlayerOneLineDialogue("No, not yet.", 591);
                            dialogueManager = player.getDialogueManager();
                            player.getDialogueManager().dialogueStep = 9001;
                            return true;
                        }
                        if (player.ownsItem(5518) && !player.getInventoryManager().containsItem(5518)) {
                            player.getDialogueManager().showPlayerOneLineDialogue("Yes, I just need to go and get it.", 591);
                            dialogueManager = player.getDialogueManager();
                            player.getDialogueManager().dialogueStep = 9001;
                            return true;
                        }
                        if (!player.getInventoryManager().containsItem(5518)) return true;
                        player.getDialogueManager().showPlayerTwoLineDialogue("Yes I have!", "I've got it right here!", 591);
                        int value4 = 38;
                        dialogueManager = player.getDialogueManager();
                        player.getDialogueManager().dialogueStep = value4 - 1;
                        return true;
                    }
                    case 3: {
                        switch (value6) {
                            case 2: {
                                player.getDialogueManager().showPlayerThreeLineDialogue("Where do you get your runes from?", "No offence, but people around here don't exactly like", "'your type'.", 591);
                                return true;
                            }
                        }
                        return false;
                    }
                    case 4: {
                        player.getDialogueManager().showNpcOneLineDialogue("My 'type'? Explain.", 591);
                        return true;
                    }
                    case 5: {
                        player.getDialogueManager().showPlayerThreeLineDialogue("You know...", "Scary bearded men in dark clothing with unhealthy", "obsessions with destruction and stuff.", 591);
                        return true;
                    }
                    case 6: {
                        player.getDialogueManager().showNpcFourLineDialogue("Hmmm.", "Well, you may be right, the foolish Saradominists that", "own this pathetic city don't appreciate loyal Zamorakians,", "it is true.", 591);
                        return true;
                    }
                    case 7: {
                        player.getDialogueManager().showPlayerTwoLineDialogue("So you can't be getting your runes anywhere around", "here...", 591);
                        return true;
                    }
                    case 8: {
                        player.getDialogueManager().showNpcThreeLineDialogue("That is correct stranger.", "The mysteries of manufacturing Runes is a closely", "guarded secret of the Zamorakian brotherhood.", 591);
                        return true;
                    }
                    case 9: {
                        player.getDialogueManager().showPlayerFourLineDialogue("Oh, you mean the whole teleporting to the Rune", "Essence mine, mining some essence, then using the", "talismans to locate the Rune Temples, then binding", "runes there?", 591);
                        return true;
                    }
                    case 10: {
                        player.getDialogueManager().showPlayerOneLineDialogue("I know all about it...", 591);
                        return true;
                    }
                    case 11: {
                        player.getDialogueManager().showNpcTwoLineDialogue("WHAT?", "I... but... you...", 591);
                        return true;
                    }
                    case 12: {
                        player.getDialogueManager().showPlayerFourLineDialogue("Well, I helped deliver some research notes to Sedridor", "at the Wizards Tower, and he teleported me to a huge", "mine he said was hidden off to the North somewhere", "where I could mine essence.", 591);
                        return true;
                    }
                    case 13: {
                        player.getDialogueManager().showNpcTwoLineDialogue("And there is an abundant supply of this 'essence' there", "you say?", 591);
                        return true;
                    }
                    case 14: {
                        player.getDialogueManager().showPlayerThreeLineDialogue("Yes, but I thought you said that you knew how to make", "runes?", "All this stuff is fairly basic knowledge I thought.", 591);
                        return true;
                    }
                    case 15: {
                        player.getDialogueManager().showNpcTwoLineDialogue("No.", "No, not at all.", 591);
                        return true;
                    }
                    case 16: {
                        player.getDialogueManager().showNpcFourLineDialogue("We occasionally manage to plunder small samples of this", "'essence' and we have recently discovered these temples", "you speak of, but I have never heard of these talismans", "before, and I was certainly not aware that this 'essence'", 591);
                        return true;
                    }
                    case 17: {
                        player.getDialogueManager().showNpcOneLineDialogue("This changes everything.", 591);
                        return true;
                    }
                    case 18: {
                        player.getDialogueManager().showPlayerOneLineDialogue("How do you mean?", 591);
                        return true;
                    }
                    case 19: {
                        player.getDialogueManager().showNpcFourLineDialogue("For many years there has been a struggle for power", "on this world.", "You may dispute the morality of each side as you wish,", "but the stalemate that exists between my Lord Zamorak", 591);
                        return true;
                    }
                    case 20: {
                        player.getDialogueManager().showNpcFourLineDialogue("and that pathetic meddling fool Saradomin has meant", "that our struggles have become more secretive.", "We exist in a 'cold war' if you will, each side fearful of", "letting the other gain too much power, and each side", 591);
                        return true;
                    }
                    case 21: {
                        player.getDialogueManager().showPlayerOneLineDialogue("You mean Guthix?", 591);
                        return true;
                    }
                    case 22: {
                        player.getDialogueManager().showNpcFourLineDialogue("Indeed.", "Amongst others.", "But you now tell me that the Saradominist Wizards", "have the capability to mass produce runes, I can only", 591);
                        return true;
                    }
                    case 23: {
                        player.getDialogueManager().showNpcTwoLineDialogue("conclude that they have been doing so secretly for some", "time now.", 591);
                        return true;
                    }
                    case 24: {
                        player.getDialogueManager().showNpcFourLineDialogue("Will you help me and my fellow Zamorakians to access", "this 'essence' mine?", "In return I will share with you the research we have", "gathered.", 591);
                        return true;
                    }
                    case 25: {
                        player.getDialogueManager().showTwoOptionsWithTitle("Help the Zamorakian Mage?", "Yes", "No");
                        return true;
                    }
                    case 26: {
                        switch (value6) {
                            case 1: {
                                player.getDialogueManager().showPlayerTwoLineDialogue("Okay, I'll help you.", "What can I do?", 591);
                                return true;
                            }
                        }
                        return false;
                    }
                    case 27: {
                        player.getDialogueManager().showNpcFourLineDialogue("All I need from you is the spell that will teleport me to", "this essence mine.", "That should be sufficient for the armies of Zamorak to", "once more begin stockpiling magic for war.", 591);
                        return true;
                    }
                    case 28: {
                        player.getDialogueManager().showPlayerThreeLineDialogue("Oh.", "Erm....", "I don't actually know that spell.", 591);
                        return true;
                    }
                    case 29: {
                        player.getDialogueManager().showNpcTwoLineDialogue("What?", "Then how do you access this location?", 591);
                        return true;
                    }
                    case 30: {
                        player.getDialogueManager().showPlayerFourLineDialogue("Oh, well, people who do know the spell teleport me there", "directly.", "Apparently they wouldn't teach it to me to try and keep", "the location secret.", 591);
                        return true;
                    }
                    case 31: {
                        player.getDialogueManager().showNpcFourLineDialogue("Hmmm.", "Yes, yes I see.", "Very well then, you may still assist us in finding this", "mysterious essence mine.", 591);
                        return true;
                    }
                    case 32: {
                        player.getDialogueManager().showPlayerOneLineDialogue("How would I do that?", 591);
                        return true;
                    }
                    case 33: {
                        player.getDialogueManager().showNpcThreeLineDialogue("I'll give you a scrying orb.", "I have cast a standard cypher spell upon it, so that it", "will absorb mystical energies that it is exposed to.", 591);
                        return true;
                    }
                    case 34: {
                        player.getDialogueManager().showNpcThreeLineDialogue("Bring it with you and teleport to the rune essence", "location, and it will absorb the mechanics of the spell and", "allow us to reverse-engineer the magic behind it.", 591);
                        return true;
                    }
                    case 35: {
                        player.getDialogueManager().showNpcThreeLineDialogue("More than three may be helpful to us, but we need a", "minimum of three in order to triangulate the position of", "this essence mine.", 591);
                        return true;
                    }
                    case 36: {
                        player.getInventoryManager().addOrDropItem(new ItemStack(5519));
                        player.enterTheAbyssMiniquestState = 3;
                        return false;
                    }
                    case 37: {
                        player.getDialogueManager().showNpcTwoLineDialogue("Heres a new one.", "Try to not lose it this time.", 591);
                        int value5 = 36;
                        dialogueManager = player.getDialogueManager();
                        player.getDialogueManager().dialogueStep = value5 - 1;
                        return true;
                    }
                    case 38: {
                        player.getDialogueManager().showNpcThreeLineDialogue("Excellent.", "Give it here, and I shall examine the findings.", "Speak to me in a small while.", 591);
                        if (player.getInventoryManager().removeItem(new ItemStack(5518))) {
                            player.enterTheAbyssMiniquestState = 4;
                        }
                        dialogueManager = player.getDialogueManager();
                        player.getDialogueManager().dialogueStep = 9001;
                        return true;
                    }
                    case 39: {
                        player.getDialogueManager().showNpcTwoLineDialogue("I offer you three things as reward for your efforts on", "behalf of my Lord Zamorak;", 591);
                        return true;
                    }
                    case 40: {
                        player.getDialogueManager().showNpcFourLineDialogue("The first is knowledge.", "I offer you my collected research on the abyss.", "I also offer you 1000 points of experience in", "RuneCrafting for your trouble.", 591);
                        return true;
                    }
                    case 41: {
                        player.getDialogueManager().showNpcThreeLineDialogue("Your final gift is that of movement.", "I will from now on offer you a teleport to the abyss", "whenever you should require it.", 591);
                        return true;
                    }
                    case 42: {
                        player.getDialogueManager().showPlayerFourLineDialogue("Huh?", "Abyss?", "What are you talking about?", "You told me that you would help me with", 591);
                        return true;
                    }
                    case 43: {
                        player.enterTheAbyssMiniquestState = 1;
                        player.refreshEnterTheAbyssConfig();
                        player.getInventoryManager().addOrDropItem(new ItemStack(5520));
                        if (!player.ownsItem(5509)) {
                            player.getInventoryManager().addOrDropItem(new ItemStack(5509));
                        }
                        player.getSkillManager().addQuestExperience(20, 1000.0);
                        player.getDialogueManager().showNpcThreeLineDialogue("And so I have done.", "Read my research notes, they may enlighten you", "somewhat.", 591);
                        dialogueManager = player.getDialogueManager();
                        player.getDialogueManager().dialogueStep = 9001;
                        return true;
                    }
                }
                return false;
            }
            case 2262: {
                Object dialogueManager2 = player.getDialogueManager();
                switch (((DialogueManager)dialogueManager2).dialogueStep) {
                    case 1: {
                        player.getDialogueManager().showPlayerOneLineDialogue("Hello there.", 591);
                        return true;
                    }
                    case 2: {
                        boolean enabled;
                        player.getDialogueManager().showNpcTwoLineDialogue("Quiet!", "You must not break my concentration!", 591);
                        dialogueManager2 = player;
                        ItemStack[] itemStackArray = ((Player)dialogueManager2).getInventoryManager().getContainer().getItems();
                        int length = itemStackArray.length;
                        int index = 0;
                        while (true) {
                            EssencePouchDefinition essencePouchDefinition;
                            if (index >= length) {
                                enabled = false;
                                break;
                            }
                            ItemStack itemStack = itemStackArray[index];
                            if (itemStack != null && (essencePouchDefinition = EssencePouchDefinition.forItemOrIndex(itemStack.getId())) != null && itemStack.getId() == essencePouchDefinition.getDegradedItemId()) {
                                enabled = true;
                                break;
                            }
                            ++index;
                        }
                        if (enabled) return true;
                        dialogueManager2 = player.getDialogueManager();
                        player.getDialogueManager().dialogueStep = 9001;
                        return true;
                    }
                    case 3: {
                        player.getDialogueManager().showFourOptions("Why not?", "What are you doing here?", "Ok, sorry", "I need your help with something...");
                        return true;
                    }
                    case 4: {
                        switch (value6) {
                            case 4: {
                                player.getDialogueManager().showPlayerTwoLineDialogue("Sorry to disturb you, I just needed your help with", "something quickly.", 591);
                                return true;
                            }
                        }
                        return false;
                    }
                    case 5: {
                        player.getDialogueManager().showNpcThreeLineDialogue("What?", "Oh...", "Very well. What did you want?", 591);
                        return true;
                    }
                    case 6: {
                        player.getDialogueManager().showPlayerTwoLineDialogue("I think my essence pouches might be degrading...", "Can you restore them for me?", 591);
                        return true;
                    }
                    case 7: {
                        player.getDialogueManager().showNpcThreeLineDialogue("A simple transfiguration spell should resolve that for", "you.", "Now leave me be!", 591);
                        dialogueManager2 = player;
                        ItemStack[] itemStackArray = ((Player)dialogueManager2).getInventoryManager().getContainer().getItems();
                        int length2 = itemStackArray.length;
                        int index2 = 0;
                        while (true) {
                            EssencePouchDefinition essencePouchDefinition;
                            if (index2 >= length2) {
                                dialogueManager2 = player.getDialogueManager();
                                player.getDialogueManager().dialogueStep = 9001;
                                return true;
                            }
                            ItemStack itemStack = itemStackArray[index2];
                            if (itemStack != null && (essencePouchDefinition = EssencePouchDefinition.forItemOrIndex(itemStack.getId())) != null && itemStack.getId() == essencePouchDefinition.getDegradedItemId()) {
                                ((Player)dialogueManager2).getInventoryManager().removeItem(new ItemStack(essencePouchDefinition.getDegradedItemId()));
                                ((Player)dialogueManager2).getInventoryManager().addItem(new ItemStack(essencePouchDefinition.getItemId()));
                            }
                            ++index2;
                        }
                    }
                }
                return false;
            }
            case 1834: {
                DialogueManager dialogueManager = player.getDialogueManager();
                switch (dialogueManager.dialogueStep) {
                    case 1: {
                        player.getDialogueManager().showNpcOneLineDialogue("Do you want a lit candle for 1000 gold?", 591);
                        return true;
                    }
                    case 2: {
                        player.getDialogueManager().showThreeOptions("Yes please.", "One thousand gold?!", "No thanks, I'd rather curse the darkness.");
                        return true;
                    }
                    case 3: {
                        switch (value6) {
                            case 1: {
                                player.getDialogueManager().showPlayerOneLineDialogue("Yes please.", 591);
                                return true;
                            }
                        }
                        return false;
                    }
                    case 4: {
                        if (player.getInventoryManager().removeItem(new ItemStack(995, 1000))) {
                            player.getDialogueManager().showNpcOneLineDialogue("Here you go.", 591);
                            player.getInventoryManager().addOrDropItem(new ItemStack(33));
                            return true;
                        }
                        player.getDialogueManager().showPlayerOneLineDialogue("Looks like I don't have enough coins.", 599);
                        dialogueManager = player.getDialogueManager();
                        player.getDialogueManager().dialogueStep = 9001;
                        return true;
                    }
                }
                return false;
            }
            case 367: {
                DialogueManager dialogueManager = player.getDialogueManager();
                switch (dialogueManager.dialogueStep) {
                    case 1: {
                        if (ServerSettings.cacheVersion < 303) {
                            player.getDialogueManager().showNpcOneLineDialogue("I'm busy right now.", 591);
                            dialogueManager = player.getDialogueManager();
                            player.getDialogueManager().dialogueStep = 9001;
                            return true;
                        }
                        player.getDialogueManager().showPlayerOneLineDialogue("Hi, I need fuel for a lamp.", 591);
                        return true;
                    }
                    case 2: {
                        player.getDialogueManager().showNpcTwoLineDialogue("Hello there, the fuel you need is lamp oil, do you need.", "help making it?", 591);
                        return true;
                    }
                    case 3: {
                        player.getDialogueManager().showTwoOptions("Yes please.", "No thanks.");
                        return true;
                    }
                    case 4: {
                        switch (value6) {
                            case 1: {
                                player.getDialogueManager().showPlayerOneLineDialogue("Yes please.", 591);
                                return true;
                            }
                        }
                        return false;
                    }
                    case 5: {
                        player.getDialogueManager().showNpcTwoLineDialogue("It's really quite simple. You use the small still in here.", "It's all set up, so there's no fiddling around with dials...", 591);
                        return true;
                    }
                    case 6: {
                        player.getDialogueManager().showNpcTwoLineDialogue("Just put ordinary swamp tar in, and then use a lantern", "or lamp to get the oil out.", 591);
                        return true;
                    }
                    case 7: {
                        player.getDialogueManager().showPlayerOneLineDialogue("Thanks.", 591);
                        dialogueManager = player.getDialogueManager();
                        player.getDialogueManager().dialogueStep = 9001;
                        return true;
                    }
                }
                return false;
            }
        }
        return false;
    }

    public static boolean continueDialogueWithNpcId(Player player, int dialogueId, int dialogueStep, int optionIndex, int npcId) {
        if (GameplayTrace.enabled()) {
            GameplayTrace.log("dialogue continue-npc player=" + GameplayTrace.describe(player) + " id=" + dialogueId + " step=" + dialogueStep + " option=" + optionIndex + " npcId=" + npcId);
        }
        Npc npc1 = null;
        Npc npc2 = null;
        Player targetPlayer = null;
        int[] itemIds = null;
        GatheringToolDefinition primaryGatheringTool = null;
        GatheringToolDefinition secondaryGatheringTool = null;
        ItemStack firstItem = null;
        ItemStack secondItem = null;
        QuestDefinition questDefinition1 = null;
        QuestDefinition questDefinition2 = null;
        Npc npc3 = null;
        ItemStack thirdItem = null;
        QuestDefinition questDefinition3 = null;
        Npc npc4 = null;
        SandwichLadyFoodOffer sandwichLadyOffer = null;
        String text1 = null;
        int value1 = 0;
        TenthSquadSigilTeleportTask sigilTeleportTask = null;
        String text2 = null;
        String text3 = null;
        String text4 = null;
        String text5 = null;
        long timestamp = 0L;
        int value2 = 0;
        String text6 = null;
        boolean conditionMet = false;
        String text7 = null;
        int value3 = 0;
        int value4 = 0;
        int value5 = 0;
        int value6 = 0;
        int value7 = 0;
        int value8 = 0;
        FarmingFarmerDefinition farmingFarmerDefinition = null;
        Npc npc5 = null;
        ItemStack[] primaryItemList = null;
        SlayerMasterDefinition slayerMasterDefinition = null;
        double amount1 = 0.0;
        double amount2 = 0.0;
        double amount3 = 0.0;
        double amount4 = 0.0;
        SlayerMonsterGuide primarySlayerGuide = null;
        SlayerMonsterGuide secondarySlayerGuide = null;
        ItemStack[] secondaryItemList = null;
        ItemStack fourthItem = null;
        ItemStack fifthItem = null;
        player.getDialogueManager().setDialogueStep(dialogueStep);
        player.getDialogueManager().setDialogueId(dialogueId);
        player.getDialogueManager().setDialogueType(0);
        player.getDialogueManager().setDialogueNpcId(npcId);
        if (dialogueId >= HistoricalNpcDialogues.ID_BASE) {
            return HistoricalNpcDialogues.continueDialogue(player, dialogueId, dialogueStep, optionIndex);
        }
        if (player.getQuestManager().handleNpcDialogue(dialogueId, dialogueStep, optionIndex, npcId)) {
            return true;
        }
        if (DialogueManager.continueUtilityNpcDialogue(player, dialogueId, optionIndex)) {
            return true;
        }
        if (dialogueStep == 1 && dialogueId == npcId && HistoricalNpcDialogues.start(player, npcId)) {
            return true;
        }
        continueContextDialogueControlSwitch1 : switch (dialogueId) {
            case 1: 
            case 2: 
            case 3: 
            case 4: 
            case 5: 
            case 6: 
            case 16: 
            case 24: 
            case 25: 
            case 351: 
            case 352: 
            case 353: 
            case 354: 
            case 359: 
            case 360: 
            case 361: 
            case 362: 
            case 363: 
            case 605: 
            case 663: 
            case 726: 
            case 727: 
            case 728: 
            case 729: 
            case 730: 
            case 1024: 
            case 1025: 
            case 1026: 
            case 1027: 
            case 1028: 
            case 1029: 
            case 1086: 
            case 2675: 
            case 2776: 
            case 3223: 
            case 3224: 
            case 3225: 
            case 3226: 
            case 3227: {
                switch (player.getDialogueManager().getDialogueStep()) {
                    case 1: {
                        player.getDialogueManager().showPlayerOneLineDialogue("Hello, how's it going?", 588);
                        return true;
                    }
                    case 2: {
                        dialogueId = GameUtil.randomInclusive(12);
                        if (dialogueId == 0) {
                            player.getDialogueManager().showNpcOneLineDialogue("How can I help you?", 591);
                        } else if (dialogueId == 1) {
                            player.getDialogueManager().showNpcOneLineDialogue("I'm fine, how are you?", 588);
                            player.getDialogueManager().setNextDialogueStep(5);
                        } else if (dialogueId == 2) {
                            player.getDialogueManager().showNpcOneLineDialogue("I'm busy right now.", 591);
                            player.getDialogueManager().finishDialogue();
                        } else if (dialogueId == 3) {
                            player.getDialogueManager().showNpcOneLineDialogue("No, I don't want to buy anything!", 614);
                            player.getDialogueManager().finishDialogue();
                        } else if (dialogueId == 4) {
                            player.getDialogueManager().showNpcOneLineDialogue("No I don't have any spare change.", 595);
                            player.getDialogueManager().finishDialogue();
                        } else if (dialogueId == 5) {
                            player.getDialogueManager().showNpcOneLineDialogue("I'm very well thank you.", 588);
                            player.getDialogueManager().finishDialogue();
                        } else if (dialogueId == 6) {
                            player.getDialogueManager().showNpcOneLineDialogue("Hello there! Nice weather we've been having.", 588);
                            player.getDialogueManager().finishDialogue();
                        } else if (dialogueId == 7) {
                            player.getDialogueManager().showNpcOneLineDialogue("That is classified information.", 591);
                            player.getDialogueManager().finishDialogue();
                        } else if (dialogueId == 8) {
                            player.getDialogueManager().showNpcOneLineDialogue("Get out of my way, I'm in a hurry!", 614);
                            player.getDialogueManager().finishDialogue();
                        } else if (dialogueId == 9) {
                            player.getDialogueManager().showNpcOneLineDialogue("Hello.", 588);
                            player.getDialogueManager().finishDialogue();
                        } else if (dialogueId == 10) {
                            player.getDialogueManager().showNpcOneLineDialogue("Do I know you? I'm in a hurry!", 588);
                            player.getDialogueManager().finishDialogue();
                        } else if (dialogueId == 11) {
                            player.getDialogueManager().showNpcOneLineDialogue("I'm sorry I can't help you there.", 595);
                            player.getDialogueManager().finishDialogue();
                        } else if (dialogueId == 12) {
                            player.getDialogueManager().showNpcOneLineDialogue("Not too bad thanks.", 588);
                            player.getDialogueManager().finishDialogue();
                        }
                        return true;
                    }
                    case 3: {
                        player.getDialogueManager().showThreeOptions("Do you wish to trade?", "I'm in search of a quest.", "I'm in search of enemies to kill.");
                        return true;
                    }
                    case 4: {
                        switch (optionIndex) {
                            case 1: {
                                player.getDialogueManager().showNpcThreeLineDialogue("No, I have nothing I wish to get rid of.", "If you want to do some trading, there are", "plent of shops and market stalls around though.", 591);
                                player.getDialogueManager().finishDialogue();
                                return true;
                            }
                            case 2: {
                                player.getDialogueManager().showNpcOneLineDialogue("I'm sorry I can't help you there.", 591);
                                player.getDialogueManager().finishDialogue();
                                return true;
                            }
                            case 3: {
                                player.getDialogueManager().showNpcTwoLineDialogue("I've heard there are many fearsome creatures", "that dwell under the ground...", 591);
                                player.getDialogueManager().finishDialogue();
                                return true;
                            }
                        }
                        break continueContextDialogueControlSwitch1;
                    }
                    case 5: {
                        player.getDialogueManager().showPlayerOneLineDialogue("Very well thank you.", 588);
                        player.getDialogueManager().finishDialogue();
                        return true;
                    }
                }
                break;
            }
            case 11286: {
                player.getDialogueManager().setDialogueNpcId(747);
                switch (player.getDialogueManager().getDialogueStep()) {
                    case 1: {
                        player.getDialogueManager().showPlayerOneLineDialogue("Could you make me a dragonfire shield?", 588);
                        return true;
                    }
                    case 2: {
                        player.getDialogueManager().showNpcTwoLineDialogue("Of course I could.", "I will do it for 1,250,000 coins.", 589);
                        return true;
                    }
                    case 3: {
                        player.getDialogueManager().showTwoOptions("Here you go.", "Maybe some other time.");
                        return true;
                    }
                    case 4: {
                        switch (optionIndex) {
                            case 1: {
                                player.getDialogueManager().showPlayerOneLineDialogue("Here you go.", 591);
                                return true;
                            }
                            case 2: {
                                player.getDialogueManager().showPlayerOneLineDialogue("Maybe some other time.", 601);
                                player.getDialogueManager().finishDialogue();
                                return true;
                            }
                        }
                        break continueContextDialogueControlSwitch1;
                    }
                    case 5: {
                        if (player.getInventoryManager().containsItemStack(new ItemStack(995, 1250000)) && player.getInventoryManager().containsItemStack(new ItemStack(1540)) && player.getInventoryManager().containsItemStack(new ItemStack(11286))) {
                            player.getInventoryManager().removeItem(new ItemStack(1540, 1));
                            player.getInventoryManager().removeItem(new ItemStack(11286, 1));
                            player.getInventoryManager().removeItem(new ItemStack(995, 1250000));
                            player.getInventoryManager().addOrDropItem(new ItemStack(11284));
                            player.getPacketSender().sendGameMessage("Oziach makes you a dragonfire shield.");
                            break continueContextDialogueControlSwitch1;
                        }
                        player.getDialogueManager().showPlayerOneLineDialogue("Oops, I forgot to bring the money with me.", 599);
                        player.getDialogueManager().finishDialogue();
                        return true;
                    }
                }
                break;
            }
            case 11287: {
                switch (player.getDialogueManager().getDialogueStep()) {
                    case 1: {
                        player.getDialogueManager().showTwoLineStatement("You set to work trying to fix the ancient shield. It's seen some", "heavy action and needs some serious work doing to it.");
                        return true;
                    }
                    case 2: {
                        DragonSquareShieldSmithing.forgeDragonSquareShield(player);
                        player.getDialogueManager().showThreeLineStatement("Even for an experienced armourer it is not an easy task, but", "eventually it is ready. You have restored the dragon square shield to", "its former glory.");
                        player.getDialogueManager().finishDialogue();
                        return true;
                    }
                }
                break;
            }
            case 13001: {
                switch (player.getDialogueManager().getDialogueStep()) {
                    case 1: {
                        player.getDialogueManager().showFourOptionsWithTitle("Select a relevant passage", "Wedding Ceremony", "Last Rites", "Blessings", "Preach");
                        return true;
                    }
                    case 2: {
                        GodBookHandler.startRecitation(player, optionIndex);
                    }
                }
                break;
            }
            case 3097: {
                if (player.ownsProgressHat() && dialogueStep == 1) {
                    return false;
                }
                switch (player.getDialogueManager().getDialogueStep()) {
                    case 1: {
                        player.getDialogueManager().showPlayerOneLineDialogue("Hi.", 591);
                        return true;
                    }
                    case 2: {
                        player.getDialogueManager().showNpcOneLineDialogue("Greetings. What wisdom do you seek?", 591);
                        return true;
                    }
                    case 3: {
                        player.getDialogueManager().showTwoOptions("I'm new to this place. Where am I?", "None, I don't really care.");
                        return true;
                    }
                    case 4: {
                        switch (optionIndex) {
                            case 1: {
                                player.getDialogueManager().showPlayerOneLineDialogue("I'm new to this place. Where am I?", 591);
                                return true;
                            }
                            case 2: {
                                player.getDialogueManager().showPlayerOneLineDialogue("None, I don't really care.", 591);
                                player.getDialogueManager().finishDialogue();
                                return true;
                            }
                        }
                        break;
                    }
                    case 5: {
                        player.getDialogueManager().showNpcFourLineDialogue("Well young one, you have entered the Magic Training", "Arena. It was built at the start of the Fifth Age, when", "runestones were first discovered. It was made because", "of the many pointless accidents caused by inexperienced", 591);
                        return true;
                    }
                    case 6: {
                        player.getDialogueManager().showNpcOneLineDialogue("mages.", 591);
                        return true;
                    }
                    case 7: {
                        player.getDialogueManager().showPlayerOneLineDialogue("Who created it?", 591);
                        return true;
                    }
                    case 8: {
                        player.getDialogueManager().showNpcFourLineDialogue("Good question. It was originally made by the ancestors", "of the wizards in the Wizards Tower. However, it was", "destroyed by melee and ranged warriors who took", "offence at the use of this new 'Magic Art'. Recently,", 591);
                        return true;
                    }
                    case 9: {
                        player.getDialogueManager().showNpcFourLineDialogue("the current denizens of the Wizards Tower have", "resurrected the arena including various Guardians you", "will see as you look around. We are here to help and to", "ensure things run smoothly.", 591);
                        return true;
                    }
                    case 10: {
                        player.getDialogueManager().showPlayerOneLineDialogue("Interesting. So what can I do here?", 591);
                        return true;
                    }
                    case 11: {
                        player.getDialogueManager().showNpcFourLineDialogue("You may train up your skill in the magic arts by", "travelling through one of the portals at the back of this", "entrance hall. By training up in one of these areas you", "will be awarded special Pizazz Points unique to each", 591);
                        return true;
                    }
                    case 12: {
                        player.getDialogueManager().showNpcTwoLineDialogue("room. With these points you may claim a variety of", "items from my fellow guardian up the stairs.", 591);
                        return true;
                    }
                    case 13: {
                        player.getDialogueManager().showPlayerOneLineDialogue("How do you record the points I have earned?", 591);
                        return true;
                    }
                    case 14: {
                        player.getDialogueManager().showNpcThreeLineDialogue("You really are full of questions! You will need a special", "Pizazz Progress Hat! I can give you one if you so", "wish to train here.", 591);
                        return true;
                    }
                    case 15: {
                        player.getDialogueManager().showPlayerOneLineDialogue("Yes Please!", 591);
                        return true;
                    }
                    case 16: {
                        player.getInventoryManager().addOrDropItem(new ItemStack(6885, 1));
                        player.getDialogueManager().showNpcTwoLineDialogue("Here you go. Talk to the hat to find out your current", "Pizazz Point totals.", 591);
                        return true;
                    }
                    case 17: {
                        player.getDialogueManager().showPlayerOneLineDialogue("Talk to it?", 591);
                        return true;
                    }
                    case 18: {
                        player.getDialogueManager().showNpcTwoLineDialogue("Well of course, it's a magic Pizazz Progress Hat! Mind", "your manners though, hats have feelings too!", 591);
                        return true;
                    }
                    case 19: {
                        player.getDialogueManager().showPlayerOneLineDialogue("Er... if you insist.", 591);
                        return true;
                    }
                    case 20: {
                        player.getDialogueManager().showNpcFourLineDialogue("Oh, and a word of warning: should you decide to leave", "the rooms by any method other than the exit portals,", "you will be teleported to the entrance and have any", "items that you picked up in the room removed.", 591);
                        return true;
                    }
                    case 21: {
                        player.getDialogueManager().showPlayerOneLineDialogue("Can you explain the different portals?", 591);
                        return true;
                    }
                    case 22: {
                        player.getDialogueManager().showNpcThreeLineDialogue("They lead to four areas to train your magic: The", "Telekinetic Theatre, The Alchemists' Playground, The", "Enchanting Chamber, and The Creature Graveyard.", 591);
                        return true;
                    }
                    case 23: {
                        player.getDialogueManager().showFiveOptions("What's the Telekinetic Theatre?", "What's the Alchemists' Playground?", "What's the Enchanting Chamber?", "What's the Creature Graveyard?", "Thanks, Bye!");
                        return true;
                    }
                    case 24: {
                        switch (optionIndex) {
                            case 1: 
                            case 2: 
                            case 3: 
                            case 4: {
                                player.getDialogueManager().showOneLineStatement("This option is currently missing...");
                                player.getDialogueManager().setNextDialogueStep(23);
                                return true;
                            }
                            case 5: {
                                player.getDialogueManager().showPlayerOneLineDialogue("Thanks, Bye!", 591);
                                player.getDialogueManager().finishDialogue();
                                return true;
                            }
                        }
                    }
                }
                break;
            }
            case 1526: {
                switch (player.getDialogueManager().getDialogueStep()) {
                    case 1: {
                        player.getDialogueManager().showNpcOneLineDialogue("Welcome To Castle Wars!", 588);
                        player.getDialogueManager().finishDialogue();
                        return true;
                    }
                }
                break;
            }
            case 1334: {
                switch (player.getDialogueManager().getDialogueStep()) {
                    case 1: {
                        player.getDialogueManager().showNpcTwoLineDialogue("Hello adventurer.", "What brings you this way?", 591);
                        return true;
                    }
                    case 2: {
                        player.getDialogueManager().showTwoOptions("Can I see your wares?", "Have you found any new prayerbooks?");
                        return true;
                    }
                    case 3: {
                        switch (optionIndex) {
                            case 1: {
                                player.getDialogueManager().showPlayerOneLineDialogue("Can I see your wares?", 591);
                                player.getDialogueManager().setNextDialogueStep(4);
                                return true;
                            }
                            case 2: {
                                player.getDialogueManager().showPlayerOneLineDialogue("Have you found any new prayerbooks?", 591);
                                player.getDialogueManager().setNextDialogueStep(6);
                                return true;
                            }
                        }
                        break;
                    }
                    case 4: {
                        player.getDialogueManager().showNpcTwoLineDialogue("Sure thing!", "I think you'll find my prices are remarkable!", 591);
                        return true;
                    }
                    case 5: {
                        ShopManager.openShop(player, GameplayHelper.getNpcShopId(dialogueId));
                        player.getDialogueManager().markDialogueInactive();
                        break;
                    }
                    case 6: {
                        player.temporaryActionValue = GameUtil.bitFlag(1) + GameUtil.bitFlag(2) + GameUtil.bitFlag(3);
                        dialogueId = 0;
                        if (!player.ownsItem(GodBookHandler.damagedSaradominBookId) && !player.ownsItem(GodBookHandler.holyBookId)) {
                            ++dialogueId;
                            player.temporaryActionValue -= GameUtil.bitFlag(1);
                        }
                        if (!player.ownsItem(GodBookHandler.damagedZamorakBookId) && !player.ownsItem(GodBookHandler.unholyBookId)) {
                            ++dialogueId;
                            player.temporaryActionValue -= GameUtil.bitFlag(2);
                        }
                        if (!player.ownsItem(GodBookHandler.damagedGuthixBookId) && !player.ownsItem(GodBookHandler.bookOfBalanceId)) {
                            ++dialogueId;
                            player.temporaryActionValue -= GameUtil.bitFlag(3);
                        }
                        if (dialogueId == 0) {
                            player.getDialogueManager().showNpcOneLineDialogue("No haven't found anything.", 591);
                            player.getDialogueManager().finishDialogue();
                        } else if (dialogueId == 1) {
                            player.getDialogueManager().showNpcThreeLineDialogue("Funnily enough I have! I found this book in", "casket just the other day! I'll sell it to you for 5000", "coins; What do you say?", 591);
                        } else {
                            text1 = "two";
                            if (dialogueId > 2) {
                                text1 = "three";
                            }
                            player.getDialogueManager().showNpcThreeLineDialogue("Funnily enough I have! I found these " + text1 + " books in", "caskets just the other day! I'll sell one to you for 5000", "coins; What do you say?", 591);
                        }
                        return true;
                    }
                    case 7: {
                        if ((player.temporaryActionValue & GameUtil.bitFlag(1)) == 0 && (player.temporaryActionValue & GameUtil.bitFlag(2)) == 0 && (player.temporaryActionValue & GameUtil.bitFlag(3)) == 0) {
                            player.getDialogueManager().showFourOptions("Buy a book of Saradomin", "Buy a book of Zamorak", "Buy a book of Guthix", "Don't buy anything");
                        } else if ((player.temporaryActionValue & GameUtil.bitFlag(1)) == 0 && (player.temporaryActionValue & GameUtil.bitFlag(2)) == 0 && (player.temporaryActionValue & GameUtil.bitFlag(3)) != 0) {
                            player.getDialogueManager().showThreeOptions("Buy a book of Saradomin", "Buy a book of Zamorak", "Don't buy anything");
                        } else if ((player.temporaryActionValue & GameUtil.bitFlag(1)) == 0 && (player.temporaryActionValue & GameUtil.bitFlag(2)) != 0 && (player.temporaryActionValue & GameUtil.bitFlag(3)) == 0) {
                            player.getDialogueManager().showThreeOptions("Buy a book of Saradomin", "Buy a book of Guthix", "Don't buy anything");
                        } else if ((player.temporaryActionValue & GameUtil.bitFlag(1)) != 0 && (player.temporaryActionValue & GameUtil.bitFlag(2)) == 0 && (player.temporaryActionValue & GameUtil.bitFlag(3)) == 0) {
                            player.getDialogueManager().showThreeOptions("Buy a book of Zamorak", "Buy a book of Guthix", "Don't buy anything");
                        } else if ((player.temporaryActionValue & GameUtil.bitFlag(1)) == 0 && (player.temporaryActionValue & GameUtil.bitFlag(2)) != 0 && (player.temporaryActionValue & GameUtil.bitFlag(3)) != 0) {
                            player.getDialogueManager().showTwoOptions("Buy a book of Saradomin", "Don't buy anything");
                        } else if ((player.temporaryActionValue & GameUtil.bitFlag(1)) != 0 && (player.temporaryActionValue & GameUtil.bitFlag(2)) == 0 && (player.temporaryActionValue & GameUtil.bitFlag(3)) != 0) {
                            player.getDialogueManager().showTwoOptions("Buy a book of Zamorak", "Don't buy anything");
                        } else if ((player.temporaryActionValue & GameUtil.bitFlag(1)) != 0 && (player.temporaryActionValue & GameUtil.bitFlag(2)) != 0 && (player.temporaryActionValue & GameUtil.bitFlag(3)) == 0) {
                            player.getDialogueManager().showTwoOptions("Buy a book of Guthix", "Don't buy anything");
                        }
                        return true;
                    }
                    case 8: {
                        switch (optionIndex) {
                            case 1: {
                                if (player.getInventoryManager().containsItemAmount(995, 5000)) {
                                    player.getDialogueManager().showNpcOneLineDialogue("Here you go!", 591);
                                    player.getInventoryManager().removeItem(new ItemStack(995, 5000));
                                    if ((player.temporaryActionValue & GameUtil.bitFlag(1)) == 0 && (player.temporaryActionValue & GameUtil.bitFlag(2)) != 0 && (player.temporaryActionValue & GameUtil.bitFlag(3)) != 0 || (player.temporaryActionValue & GameUtil.bitFlag(1)) == 0 && (player.temporaryActionValue & GameUtil.bitFlag(2)) == 0 && (player.temporaryActionValue & GameUtil.bitFlag(3)) == 0 || (player.temporaryActionValue & GameUtil.bitFlag(1)) == 0 && (player.temporaryActionValue & GameUtil.bitFlag(2)) == 0 && (player.temporaryActionValue & GameUtil.bitFlag(3)) != 0 || (player.temporaryActionValue & GameUtil.bitFlag(1)) == 0 && (player.temporaryActionValue & GameUtil.bitFlag(2)) != 0 && (player.temporaryActionValue & GameUtil.bitFlag(3)) == 0) {
                                        GodBookHandler.giveReplacementBook(player, GodBookHandler.damagedSaradominBookId);
                                    } else if ((player.temporaryActionValue & GameUtil.bitFlag(1)) != 0 && (player.temporaryActionValue & GameUtil.bitFlag(2)) == 0 && (player.temporaryActionValue & GameUtil.bitFlag(3)) != 0 || (player.temporaryActionValue & GameUtil.bitFlag(1)) != 0 && (player.temporaryActionValue & GameUtil.bitFlag(2)) == 0 && (player.temporaryActionValue & GameUtil.bitFlag(3)) == 0) {
                                        GodBookHandler.giveReplacementBook(player, GodBookHandler.damagedZamorakBookId);
                                    } else if ((player.temporaryActionValue & GameUtil.bitFlag(1)) != 0 && (player.temporaryActionValue & GameUtil.bitFlag(2)) != 0 && (player.temporaryActionValue & GameUtil.bitFlag(3)) == 0) {
                                        GodBookHandler.giveReplacementBook(player, GodBookHandler.damagedGuthixBookId);
                                    }
                                } else {
                                    player.getDialogueManager().showPlayerOneLineDialogue("Sorry, don't have enough money right now.", 591);
                                }
                                player.getDialogueManager().finishDialogue();
                                return true;
                            }
                            case 2: {
                                if ((player.temporaryActionValue & GameUtil.bitFlag(1)) == 0 && (player.temporaryActionValue & GameUtil.bitFlag(2)) != 0 && (player.temporaryActionValue & GameUtil.bitFlag(3)) != 0 || (player.temporaryActionValue & GameUtil.bitFlag(1)) != 0 && (player.temporaryActionValue & GameUtil.bitFlag(2)) == 0 && (player.temporaryActionValue & GameUtil.bitFlag(3)) != 0 || (player.temporaryActionValue & GameUtil.bitFlag(1)) != 0 && (player.temporaryActionValue & GameUtil.bitFlag(2)) != 0 && (player.temporaryActionValue & GameUtil.bitFlag(3)) == 0) {
                                    player.getDialogueManager().showPlayerOneLineDialogue("Maybe next time.", 591);
                                } else if (player.getInventoryManager().containsItemAmount(995, 5000)) {
                                    player.getDialogueManager().showNpcOneLineDialogue("Here you go!", 591);
                                    player.getInventoryManager().removeItem(new ItemStack(995, 5000));
                                    if ((player.temporaryActionValue & GameUtil.bitFlag(1)) == 0 && (player.temporaryActionValue & GameUtil.bitFlag(2)) == 0 && (player.temporaryActionValue & GameUtil.bitFlag(3)) == 0 || (player.temporaryActionValue & GameUtil.bitFlag(1)) == 0 && (player.temporaryActionValue & GameUtil.bitFlag(2)) == 0 && (player.temporaryActionValue & GameUtil.bitFlag(3)) != 0) {
                                        GodBookHandler.giveReplacementBook(player, GodBookHandler.damagedZamorakBookId);
                                    } else if ((player.temporaryActionValue & GameUtil.bitFlag(1)) == 0 && (player.temporaryActionValue & GameUtil.bitFlag(2)) != 0 && (player.temporaryActionValue & GameUtil.bitFlag(3)) == 0 || (player.temporaryActionValue & GameUtil.bitFlag(1)) != 0 && (player.temporaryActionValue & GameUtil.bitFlag(2)) == 0 && (player.temporaryActionValue & GameUtil.bitFlag(3)) == 0) {
                                        GodBookHandler.giveReplacementBook(player, GodBookHandler.damagedGuthixBookId);
                                    }
                                } else {
                                    player.getDialogueManager().showPlayerOneLineDialogue("Sorry, don't have enough money right now.", 591);
                                }
                                player.getDialogueManager().finishDialogue();
                                return true;
                            }
                            case 3: {
                                if ((player.temporaryActionValue & GameUtil.bitFlag(1)) == 0 && (player.temporaryActionValue & GameUtil.bitFlag(2)) == 0 && (player.temporaryActionValue & GameUtil.bitFlag(3)) != 0 || (player.temporaryActionValue & GameUtil.bitFlag(1)) == 0 && (player.temporaryActionValue & GameUtil.bitFlag(2)) != 0 && (player.temporaryActionValue & GameUtil.bitFlag(3)) == 0 || (player.temporaryActionValue & GameUtil.bitFlag(1)) != 0 && (player.temporaryActionValue & GameUtil.bitFlag(2)) == 0 && (player.temporaryActionValue & GameUtil.bitFlag(3)) == 0) {
                                    player.getDialogueManager().showPlayerOneLineDialogue("Maybe next time.", 591);
                                } else if (player.getInventoryManager().containsItemAmount(995, 5000)) {
                                    player.getDialogueManager().showNpcOneLineDialogue("Here you go!", 591);
                                    player.getInventoryManager().removeItem(new ItemStack(995, 5000));
                                    if ((player.temporaryActionValue & GameUtil.bitFlag(1)) == 0 && (player.temporaryActionValue & GameUtil.bitFlag(2)) == 0 && (player.temporaryActionValue & GameUtil.bitFlag(3)) == 0) {
                                        GodBookHandler.giveReplacementBook(player, GodBookHandler.damagedGuthixBookId);
                                    }
                                } else {
                                    player.getDialogueManager().showPlayerOneLineDialogue("Sorry, don't have enough money right now.", 591);
                                }
                                player.getDialogueManager().finishDialogue();
                                return true;
                            }
                            case 4: {
                                player.getDialogueManager().showPlayerOneLineDialogue("Maybe next time.", 591);
                                player.getDialogueManager().finishDialogue();
                                return true;
                            }
                        }
                    }
                }
                break;
            }
            case 659: {
                switch (player.getDialogueManager().getDialogueStep()) {
                    case 1: {
                        player.getDialogueManager().showPlayerOneLineDialogue("Hi!", 591);
                        return true;
                    }
                    case 2: {
                        player.getDialogueManager().showNpcOneLineDialogue("Hi! I'm Party Pete. Welcome to the Party Room!", 591);
                        return true;
                    }
                    case 3: {
                        player.getDialogueManager().showFourOptions("So what's this room for?", "What's the big lever over there for?", "What's the gold chest for?", "I wanna party!");
                        return true;
                    }
                    case 4: {
                        switch (optionIndex) {
                            case 1: {
                                player.getDialogueManager().showPlayerOneLineDialogue("So what's this room for?", 591);
                                player.getDialogueManager().setNextDialogueStep(5);
                                return true;
                            }
                            case 2: {
                                player.getDialogueManager().showPlayerOneLineDialogue("What's the big lever over there for?", 591);
                                player.getDialogueManager().setNextDialogueStep(10);
                                return true;
                            }
                            case 3: {
                                player.getDialogueManager().showPlayerOneLineDialogue("What's the gold chest for?", 591);
                                player.getDialogueManager().setNextDialogueStep(15);
                                return true;
                            }
                            case 4: {
                                player.getDialogueManager().showPlayerOneLineDialogue("I wanna party!", 591);
                                player.getDialogueManager().setNextDialogueStep(19);
                                return true;
                            }
                        }
                        break;
                    }
                    case 5: {
                        player.getDialogueManager().showNpcOneLineDialogue("This room is for partying the night away!", 591);
                        return true;
                    }
                    case 6: {
                        player.getDialogueManager().showPlayerOneLineDialogue("How do you have a party in RuneScape?", 591);
                        return true;
                    }
                    case 7: {
                        player.getDialogueManager().showNpcOneLineDialogue("Get a few mates round, get the beers in and have fun!", 591);
                        return true;
                    }
                    case 8: {
                        player.getDialogueManager().showNpcOneLineDialogue("Some players organise parties so keep an eye open!", 591);
                        return true;
                    }
                    case 9: {
                        player.getDialogueManager().showPlayerOneLineDialogue("Woop! Thanks Pete!", 591);
                        player.getDialogueManager().finishDialogue();
                        return true;
                    }
                    case 10: {
                        player.getDialogueManager().showNpcOneLineDialogue("Simple. With the lever you can do some fun stuff.", 591);
                        return true;
                    }
                    case 11: {
                        player.getDialogueManager().showPlayerOneLineDialogue("What kind of stuff?", 591);
                        return true;
                    }
                    case 12: {
                        player.getDialogueManager().showNpcFourLineDialogue("A balloon drop costs 1000 gold. For this you get 200", "balloons dropped across the whole of the party room.", "You can then have fun popping the balloons! If there", "are items in the Party Drop Chest they will be inside", 591);
                        return true;
                    }
                    case 13: {
                        player.getDialogueManager().showNpcTwoLineDialogue("the balloons! For 500 gold you can summon the Party", "Room Knights who will dance for your delight.", 591);
                        return true;
                    }
                    case 14: {
                        player.getDialogueManager().showNpcOneLineDialogue("Their singing isn't a delight though!", 591);
                        player.getDialogueManager().finishDialogue();
                        return true;
                    }
                    case 15: {
                        player.getDialogueManager().showNpcTwoLineDialogue("Any items that are in the chest will be dropped inside", "the balloons when you pull the lever!", 591);
                        return true;
                    }
                    case 16: {
                        player.getDialogueManager().showPlayerOneLineDialogue("Cool! Sounds like a fun way to do a drop party!", 591);
                        return true;
                    }
                    case 17: {
                        player.getDialogueManager().showNpcOneLineDialogue("Exactly!", 591);
                        return true;
                    }
                    case 18: {
                        player.getDialogueManager().showNpcThreeLineDialogue("A word of warning though. Any items that you put into", "the chest can't be taken out again and it costs 1000", "gold pieces for each balloon drop.", 591);
                        player.getDialogueManager().finishDialogue();
                        return true;
                    }
                    case 19: {
                        player.getDialogueManager().showNpcTwoLineDialogue("I've won the Dance Trophy at the Kandarin Ball three", "years in a trot!", 591);
                        return true;
                    }
                    case 20: {
                        player.getDialogueManager().showPlayerOneLineDialogue("Show me your moves Pete!", 591);
                        return true;
                    }
                    case 21: {
                        npc1 = Npc.findByDefinitionId(659);
                        if (npc1 == null) break;
                        npc1.getUpdateState().setAnimation(866);
                    }
                }
                break;
            }
            case 3098: {
                if (!player.getTelekineticTheatreController().mazeSolved) {
                    return false;
                }
                switch (player.getDialogueManager().getDialogueStep()) {
                    case 1: {
                        player.getDialogueManager().showPlayerOneLineDialogue("Hi!", 591);
                        return true;
                    }
                    case 2: {
                        player.getDialogueManager().showNpcOneLineDialogue("Would you like to try another maze?", 591);
                        return true;
                    }
                    case 3: {
                        player.getDialogueManager().showTwoOptions("Yes please!", "No thanks.");
                        return true;
                    }
                    case 4: {
                        switch (optionIndex) {
                            case 1: {
                                player.getDialogueManager().showPlayerOneLineDialogue("Yes please!", 591);
                                return true;
                            }
                            case 2: {
                                player.getDialogueManager().showPlayerOneLineDialogue("No thanks.", 591);
                                player.getDialogueManager().finishDialogue();
                                return true;
                            }
                        }
                        break;
                    }
                    case 5: {
                        player.getDialogueManager().showNpcOneLineDialogue("Very well, I shall teleport you.", 591);
                        return true;
                    }
                    case 6: {
                        player.getTelekineticTheatreController().startNextMaze();
                        player.getDialogueManager().finishDialogue();
                    }
                }
                break;
            }
            case 3307: {
                switch (player.getDialogueManager().getDialogueStep()) {
                    case 1: {
                        player.getDialogueManager().showPlayerTwoLineDialogue("What happened to the old man who used to be the", "doorman?", 591);
                        return true;
                    }
                    case 2: {
                        player.getDialogueManager().showNpcTwoLineDialogue("You mean my father? He went into retirement. I've", "taken over the family business instead.", 591);
                        return true;
                    }
                    case 3: {
                        player.getDialogueManager().showPlayerOneLineDialogue("Your father! But you don't look anything like him!", 591);
                        return true;
                    }
                    case 4: {
                        player.getDialogueManager().showNpcTwoLineDialogue("No, fortunately for me I inherited my good looks from", "my mother.", 591);
                        player.getDialogueManager().finishDialogue();
                        return true;
                    }
                }
                break;
            }
            case 389: {
                switch (player.getDialogueManager().getDialogueStep()) {
                    case 1: {
                        if (player.getQuestState(80) != 1) {
                            return false;
                        }
                        player.getDialogueManager().showNpcTwoLineDialogue("Hello there, would you like me to enchant a battlestaff", "for 40k coins for you?", 591);
                        return true;
                    }
                    case 2: {
                        player.getDialogueManager().showTwoOptions("Yes.", "No thanks.");
                        return true;
                    }
                    case 3: {
                        switch (optionIndex) {
                            case 1: {
                                player.getDialogueManager().finishDialogue();
                                player.getDialogueManager().markDialogueInactive();
                                player.getPacketSender().sendInterfaceModel(1734, 200, 1397);
                                player.getPacketSender().sendInterfaceModel(1735, 200, 1395);
                                player.getPacketSender().sendInterfaceModel(1736, 200, 1399);
                                player.getPacketSender().sendInterfaceModel(1737, 200, 1393);
                                player.getPacketSender().sendInterfaceModel(1738, 200, 3053);
                                player.getPacketSender().sendInterfaceModel(15348, 200, 6562);
                                player.getPacketSender().showInterface(205);
                                return true;
                            }
                            case 2: {
                                player.getDialogueManager().showPlayerOneLineDialogue("No thanks.", 588);
                                player.getDialogueManager().finishDialogue();
                                return true;
                            }
                        }
                    }
                }
                break;
            }
            case 904: {
                switch (player.getDialogueManager().getDialogueStep()) {
                    case 1: {
                        player.getDialogueManager().showPlayerOneLineDialogue("Hi.", 591);
                        if (player.mageArenaProgressStage == 6 || !player.hasMageArenaGodCape()) {
                            player.getDialogueManager().finishDialogue();
                        }
                        return true;
                    }
                    case 2: {
                        player.getDialogueManager().showNpcOneLineDialogue("Hello adventurer, have you made your choice?", 591);
                        return true;
                    }
                    case 3: {
                        player.getDialogueManager().showPlayerOneLineDialogue("I have.", 591);
                        return true;
                    }
                    case 4: {
                        player.getDialogueManager().showNpcFourLineDialogue("Good, good, I hope you have chosen well. I will now", "present you with a magic staff. This, along with the", "cape awarded to you by your chosen god, are all the", "weapons and armour you will need here.", 591);
                        return true;
                    }
                    case 5: {
                        dialogueId = 1;
                        if (player.ownsItem(2412)) {
                            dialogueId = 2415;
                        }
                        if (player.ownsItem(2413)) {
                            dialogueId = 2416;
                        }
                        if (player.ownsItem(2414)) {
                            dialogueId = 2417;
                        }
                        player.getDialogueManager().showItemMessage("The guardian hands you an ornate magic staff.", new ItemStack(dialogueId, 1));
                        player.getInventoryManager().addOrDropItem(new ItemStack(dialogueId, 1));
                        player.mageArenaProgressStage = 6;
                        return true;
                    }
                }
                break;
            }
            case 905: {
                switch (player.getDialogueManager().getDialogueStep()) {
                    case 1: {
                        if (player.mageArenaProgressStage == 6) {
                            return false;
                        }
                        if (player.mageArenaProgressStage == 0) {
                            player.getDialogueManager().showPlayerOneLineDialogue("Hello there. What is this place?", 591);
                            return true;
                        }
                        if (player.mageArenaProgressStage <= 5) {
                            player.getDialogueManager().showPlayerOneLineDialogue("Hello, Kolodion.", 591);
                            if (player.mageArenaProgressStage == 5) {
                                player.getDialogueManager().setNextDialogueStep(52);
                            } else {
                                player.getDialogueManager().setNextDialogueStep(10);
                            }
                            return true;
                        }
                    }
                    case 2: {
                        player.getDialogueManager().showNpcThreeLineDialogue("I am the great Kolodion, master of battle magic, and", "this is my battle arena. Top wizards travel from all over", "RuneScape to fight here.", 591);
                        return true;
                    }
                    case 3: {
                        player.getDialogueManager().showThreeOptions("Can I fight here?", "What's the point of that?", "That's barbaric!");
                        return true;
                    }
                    case 4: {
                        switch (optionIndex) {
                            case 1: {
                                player.getDialogueManager().showPlayerOneLineDialogue("Can I fight here?", 591);
                                player.getDialogueManager().setNextDialogueStep(5);
                                return true;
                            }
                        }
                        player.getDialogueManager().showOneLineStatement("This option is currently missing...");
                        player.getDialogueManager().setNextDialogueStep(3);
                        return true;
                    }
                    case 5: {
                        player.getDialogueManager().showNpcThreeLineDialogue("My arena is open to any high level wizard, but this is", "no game. Many wizards fall in this arena, never to rise", "again. The strongest mages have been destroyed.", 591);
                        return true;
                    }
                    case 6: {
                        player.getDialogueManager().showNpcOneLineDialogue("If you're sure you want in?", 591);
                        return true;
                    }
                    case 7: {
                        player.getDialogueManager().showTwoOptions("Yes indeedy.", "No I don't.");
                        return true;
                    }
                    case 8: {
                        switch (optionIndex) {
                            case 1: {
                                player.getDialogueManager().showPlayerOneLineDialogue("Yes indeedy.", 591);
                                player.getDialogueManager().setNextDialogueStep(9);
                                return true;
                            }
                            case 2: {
                                player.getDialogueManager().showPlayerOneLineDialogue("No I don't.", 591);
                                player.getDialogueManager().finishDialogue();
                                return true;
                            }
                        }
                        break continueContextDialogueControlSwitch1;
                    }
                    case 9: {
                        player.getDialogueManager().showNpcOneLineDialogue("Good, good. You have a healthy sense of competition.", 591);
                        return true;
                    }
                    case 10: {
                        player.getDialogueManager().showNpcFourLineDialogue("Remember, traveller - in my arena, hand-to-hand", "combat is useless. Your strength will diminish as you", "enter the arena, but the spells you can learn are", "amongst the most powerful in all of RuneScape.", 591);
                        return true;
                    }
                    case 11: {
                        player.getDialogueManager().showNpcOneLineDialogue("Before I can accept you in, we must duel.", 591);
                        return true;
                    }
                    case 12: {
                        player.getDialogueManager().showTwoOptions("Okay, let's fight.", "No thanks.");
                        return true;
                    }
                    case 13: {
                        switch (optionIndex) {
                            case 1: {
                                player.getDialogueManager().showPlayerOneLineDialogue("Okay, let's fight.", 591);
                                player.getDialogueManager().setNextDialogueStep(14);
                                return true;
                            }
                            case 2: {
                                player.getDialogueManager().showPlayerOneLineDialogue("No thanks.", 591);
                                player.getDialogueManager().finishDialogue();
                                return true;
                            }
                        }
                        break continueContextDialogueControlSwitch1;
                    }
                    case 14: {
                        player.getDialogueManager().showNpcOneLineDialogue("I must first check that you are up to scratch.", 591);
                        return true;
                    }
                    case 15: {
                        if (player.getSkillManager().getBaseLevel(6) >= 60) {
                            player.getDialogueManager().showPlayerOneLineDialogue("You don't need to worry about that.", 591);
                        } else {
                            player.getDialogueManager().showNpcOneLineDialogue("Sorry, you are not experienced enough to enter.", 591);
                            player.getDialogueManager().finishDialogue();
                        }
                        return true;
                    }
                    case 16: {
                        player.getDialogueManager().showNpcFourLineDialogue("Not just any magician can enter - only the most", "powerful and most feared. Before you can use the", "power of this arena, you must prove yourself against", "me.", 591);
                        return true;
                    }
                    case 17: {
                        if (player.getInteractionTarget().isNpc() && (npc2 = (Npc)player.getInteractionTarget()).getNpcId() == 905) {
                            npc2.startMageArenaChallenge(player, 3105, 3934, 0, null);
                        }
                        return false;
                    }
                    case 52: {
                        player.getDialogueManager().showNpcOneLineDialogue("Hello, young mage. You're a tough one.", 591);
                        return true;
                    }
                    case 53: {
                        player.getDialogueManager().showPlayerOneLineDialogue("What now?", 591);
                        return true;
                    }
                    case 54: {
                        player.getDialogueManager().showNpcThreeLineDialogue("Step into the magic pool. It will take you to a chamber.", "There, you must decide which god you will represent in", "the arena.", 591);
                        return true;
                    }
                    case 55: {
                        player.getDialogueManager().showPlayerOneLineDialogue("Thanks, Kolodion.", 591);
                        return true;
                    }
                    case 56: {
                        player.getDialogueManager().showNpcOneLineDialogue("That's what I'm here for.", 591);
                        player.getDialogueManager().finishDialogue();
                        return true;
                    }
                }
                break;
            }
            case 1263: {
                switch (player.getDialogueManager().getDialogueStep()) {
                    case 1: {
                        if (player.isMember()) {
                            if (ServerSettings.freeToPlayWorld) {
                                player.packetSender.sendGameMessage("You need to be in members world to access members content.");
                            } else {
                                player.getDialogueManager().showNpcOneLineDialogue("Hello there, can I help you?", 591);
                            }
                        } else {
                            player.packetSender.sendGameMessage("You need a members account to access members content.");
                        }
                        return true;
                    }
                    case 2: {
                        player.getDialogueManager().showFourOptions("What do you do here?", "What's that you're wearing?", "Can you make me some armour please?", "No thanks.");
                        return true;
                    }
                    case 3: {
                        switch (optionIndex) {
                            case 3: {
                                player.getDialogueManager().showPlayerOneLineDialogue("Can you make me some armour please?", 591);
                                player.getDialogueManager().setNextDialogueStep(4);
                                return true;
                            }
                            case 1: 
                            case 2: 
                            case 4: {
                                player.getDialogueManager().showOneLineStatement("This option is currently missing...");
                                player.getDialogueManager().setNextDialogueStep(2);
                                return true;
                            }
                        }
                        break continueContextDialogueControlSwitch1;
                    }
                    case 4: {
                        player.getDialogueManager().showNpcOneLineDialogue("Certainly, what would you like me to make?", 591);
                        return true;
                    }
                    case 5: {
                        player.getDialogueManager().finishDialogue();
                        player.setInterfaceAction("splitbark");
        GameplayHelper.showSplitbarkProductionInterface(player, 3385, 3387, 3389, 3391, 3393, "Helm", "Body", "Legs", "Gauntlets", "Boots", "Splitbark Armour");
                        return true;
                    }
                    case 6: {
                        player.getDialogueManager().showNpcOneLineDialogue("There you go, enjoy your new armour!", 591);
                        player.getDialogueManager().finishDialogue();
                        return true;
                    }
                }
                break;
            }
            case 736: {
                switch (player.getDialogueManager().getDialogueStep()) {
                    case 1: {
                        player.getDialogueManager().showNpcOneLineDialogue("Heya! What can I get you?", 591);
                        return true;
                    }
                    case 2: {
                        player.getDialogueManager().showPlayerOneLineDialogue("What ales are you serving?", 591);
                        return true;
                    }
                    case 3: {
                        player.getDialogueManager().showNpcTwoLineDialogue("Well, we've got Asgarnian Ale, Wizard's Mind Bomb", "and Dwarven Stout, all for only 3 coins.", 591);
                        return true;
                    }
                    case 4: {
                        player.getDialogueManager().showFourOptions("One Asgarnian Ale, please.", "I'll try the Mind Bomb.", "Can I have a Dwarven Stout?", "I don't feel like any of those.");
                        return true;
                    }
                    case 5: {
                        switch (optionIndex) {
                            case 1: {
                                player.getDialogueManager().showPlayerOneLineDialogue("One Asgarnian Ale, please.", 591);
                                player.setTemporaryActionValue(1917);
                                player.getDialogueManager().setNextDialogueStep(6);
                                return true;
                            }
                            case 2: {
                                player.getDialogueManager().showPlayerOneLineDialogue("I'll try the Mind Bomb.", 591);
                                player.setTemporaryActionValue(1907);
                                player.getDialogueManager().setNextDialogueStep(6);
                                return true;
                            }
                            case 3: {
                                player.getDialogueManager().showPlayerOneLineDialogue("Can I have a Dwarven Stout?", 591);
                                player.setTemporaryActionValue(1913);
                                player.getDialogueManager().setNextDialogueStep(6);
                                return true;
                            }
                            case 4: {
                                player.getDialogueManager().showPlayerOneLineDialogue("I don't feel like any of those.", 591);
                                player.getDialogueManager().finishDialogue();
                                return true;
                            }
                        }
                        break continueContextDialogueControlSwitch1;
                    }
                    case 6: {
                        if (player.getInventoryManager().containsItemAmount(995, 3)) {
                            player.getDialogueManager().showPlayerOneLineDialogue("Thanks, " + new Npc(dialogueId).getDefinition().getName() + ".", 591);
                            player.getInventoryManager().removeItem(new ItemStack(995, 3));
                            player.getInventoryManager().addOrDropItem(new ItemStack(player.getTemporaryActionValue(), 1));
                            player.getDialogueManager().finishDialogue();
                        } else {
                            player.getDialogueManager().showNpcOneLineDialogue("I said 3 coins! You haven't got 3 coins!", 614);
                            player.getDialogueManager().finishDialogue();
                        }
                        return true;
                    }
                }
                break;
            }
            case 734: {
                switch (player.getDialogueManager().getDialogueStep()) {
                    case 1: {
                        player.getDialogueManager().showTwoOptions("Could I buy a beer please?", "Have you heard any rumours here?");
                        return true;
                    }
                    case 2: {
                        switch (optionIndex) {
                            case 1: {
                                player.getDialogueManager().showPlayerOneLineDialogue("Could I buy a beer please?", 591);
                                player.getDialogueManager().setNextDialogueStep(3);
                                return true;
                            }
                            case 2: {
                                player.getDialogueManager().showPlayerOneLineDialogue("Have you heard any rumours here?", 591);
                                player.getDialogueManager().setNextDialogueStep(6);
                                return true;
                            }
                        }
                        break continueContextDialogueControlSwitch1;
                    }
                    case 3: {
                        player.getDialogueManager().showNpcOneLineDialogue("Sure, that will be 2 gold coins please.", 591);
                        return true;
                    }
                    case 4: {
                        if (player.getInventoryManager().containsItemAmount(995, 2)) {
                            player.getDialogueManager().showPlayerOneLineDialogue("Ok, here you go.", 591);
                        } else {
                            player.getDialogueManager().showPlayerOneLineDialogue("I don't have enough coins.", 591);
                            player.getDialogueManager().finishDialogue();
                        }
                        return true;
                    }
                    case 5: {
                        player.getInventoryManager().removeItem(new ItemStack(995, 2));
                        player.getInventoryManager().addOrDropItem(new ItemStack(1917, 1));
                        player.getDialogueManager().showTwoItemMessage("You buy a pint of beer!", "", new ItemStack(-1, 1), new ItemStack(1917, 1));
                        player.getDialogueManager().finishDialogue();
                        return true;
                    }
                    case 6: {
                        player.getDialogueManager().showNpcOneLineDialogue("No, it hasn't been very busy lately.", 591);
                        return true;
                    }
                }
                break;
            }
            case 731: {
                switch (player.getDialogueManager().getDialogueStep()) {
                    case 1: {
                        player.getDialogueManager().showNpcOneLineDialogue("Can I help you?", 591);
                        return true;
                    }
                    case 2: {
                        player.getDialogueManager().showThreeOptions("I'll have a beer please.", "Any hints where I can go adventuring?", "Heard any good gossip?");
                        return true;
                    }
                    case 3: {
                        switch (optionIndex) {
                            case 1: {
                                player.getDialogueManager().showPlayerOneLineDialogue("I'll have a pint of beer please.", 591);
                                player.getDialogueManager().setNextDialogueStep(4);
                                return true;
                            }
                            case 2: {
                                player.getDialogueManager().showPlayerOneLineDialogue("Any hints where I can go adventuring?", 591);
                                player.getDialogueManager().setNextDialogueStep(6);
                                return true;
                            }
                            case 3: {
                                player.getDialogueManager().showPlayerOneLineDialogue("Heard any good gossip?", 591);
                                player.getDialogueManager().setNextDialogueStep(10);
                                return true;
                            }
                        }
                        break continueContextDialogueControlSwitch1;
                    }
                    case 4: {
                        player.getDialogueManager().showNpcOneLineDialogue("Ok, that'll be two coins please.", 591);
                        return true;
                    }
                    case 5: {
                        if (player.getInventoryManager().containsItemAmount(995, 2)) {
                            player.getPacketSender().sendGameMessage("You buy a pint of beer.");
                            player.getInventoryManager().removeItem(new ItemStack(995, 2));
                            player.getInventoryManager().addOrDropItem(new ItemStack(1917, 1));
                            break continueContextDialogueControlSwitch1;
                        }
                        player.getDialogueManager().showPlayerOneLineDialogue("Oh dear, I don't seem to have enough money!", 591);
                        player.getDialogueManager().finishDialogue();
                        return true;
                    }
                    case 6: {
                        player.getDialogueManager().showNpcOneLineDialogue("Ooh, now. Let me see...", 591);
                        return true;
                    }
                    case 7: {
                        player.getDialogueManager().showNpcThreeLineDialogue("Well there is the Varrock sewers. There are tales of", "untold horrors coming out at night and stealing babies", "from houses.", 591);
                        return true;
                    }
                    case 8: {
                        player.getDialogueManager().showPlayerOneLineDialogue("Sounds perfect! Where's the entrance?", 591);
                        return true;
                    }
                    case 9: {
                        player.getDialogueManager().showNpcOneLineDialogue("It's just to the east of the palace.", 591);
                        player.getDialogueManager().finishDialogue();
                        return true;
                    }
                    case 10: {
                        player.getDialogueManager().showNpcFourLineDialogue("I'm not that well up on the gossip out here. I've heard", "that the bartender in the Blue Moon Inn has gone a", "little crazy, he keeps claiming he is a part of something", "called a computer game.", 591);
                        return true;
                    }
                    case 11: {
                        player.getDialogueManager().showNpcTwoLineDialogue("What that means, I don't know.", "That's probably old news by now though.", 591);
                        player.getDialogueManager().finishDialogue();
                        return true;
                    }
                }
                break;
            }
            case 733: {
                switch (player.getDialogueManager().getDialogueStep()) {
                    case 1: {
                        player.getDialogueManager().showNpcOneLineDialogue("What can I do yer for?", 591);
                        return true;
                    }
                    case 2: {
                        player.getDialogueManager().showThreeOptions("A glass of your finest ale please.", "Can you recommend where an adventurer might make his fortune?", "Do you know where I can get some good equipment?");
                        return true;
                    }
                    case 3: {
                        switch (optionIndex) {
                            case 1: {
                                player.getDialogueManager().showPlayerOneLineDialogue("A glass of your finest ale please.", 591);
                                player.getDialogueManager().setNextDialogueStep(4);
                                return true;
                            }
                            case 2: {
                                player.getDialogueManager().showPlayerTwoLineDialogue("Can you recommend where an adventurer", "might make his fortune?", 591);
                                player.getDialogueManager().setNextDialogueStep(6);
                                return true;
                            }
                            case 3: {
                                player.getDialogueManager().showPlayerOneLineDialogue("Do you know where I can get some good equipment?", 591);
                                player.getDialogueManager().setNextDialogueStep(14);
                                return true;
                            }
                        }
                        break continueContextDialogueControlSwitch1;
                    }
                    case 4: {
                        player.getDialogueManager().showNpcOneLineDialogue("No problemo. That'll be 2 coins.", 591);
                        return true;
                    }
                    case 5: {
                        if (player.getInventoryManager().containsItemAmount(995, 2)) {
                            player.getPacketSender().sendGameMessage("You buy a pint of beer.");
                            player.getInventoryManager().removeItem(new ItemStack(995, 2));
                            player.getInventoryManager().addOrDropItem(new ItemStack(1917, 1));
                            break continueContextDialogueControlSwitch1;
                        }
                        player.getDialogueManager().showPlayerOneLineDialogue("Sorry, don't have that right now.", 591);
                        player.getDialogueManager().finishDialogue();
                        return true;
                    }
                    case 6: {
                        player.getDialogueManager().showNpcTwoLineDialogue("Ooh I don't know if I should be giving away information,", "makes the computer game too easy.", 591);
                        return true;
                    }
                    case 7: {
                        player.getDialogueManager().showThreeOptions("Oh ah well...", "Computer game? What are you talking about?", "Just a small clue?");
                        return true;
                    }
                    case 8: {
                        switch (optionIndex) {
                            case 1: {
                                player.getDialogueManager().showPlayerOneLineDialogue("Oh ah well...", 591);
                                player.getDialogueManager().finishDialogue();
                                return true;
                            }
                            case 2: {
                                player.getDialogueManager().showPlayerOneLineDialogue("Computer game? What are you talking about?", 591);
                                player.getDialogueManager().setNextDialogueStep(9);
                                return true;
                            }
                            case 3: {
                                player.getDialogueManager().showPlayerOneLineDialogue("Just a small clue?", 591);
                                player.getDialogueManager().setNextDialogueStep(13);
                                return true;
                            }
                        }
                        break continueContextDialogueControlSwitch1;
                    }
                    case 9: {
                        player.getDialogueManager().showNpcTwoLineDialogue("This world around us... is a computer game...", "called RuneScape.", 591);
                        return true;
                    }
                    case 10: {
                        player.getDialogueManager().showPlayerTwoLineDialogue("Nope, still don't understand what you are talking about.", "What's a computer?", 591);
                        return true;
                    }
                    case 11: {
                        player.getDialogueManager().showNpcTwoLineDialogue("It's a sort of magic box thing,", "which can do all sorts of stuff.", 591);
                        return true;
                    }
                    case 12: {
                        player.getDialogueManager().showPlayerOneLineDialogue("I give up. You're obviously completely mad!", 591);
                        player.getDialogueManager().finishDialogue();
                        return true;
                    }
                    case 13: {
                        player.getDialogueManager().showNpcTwoLineDialogue("Go and talk to bartender at the Jolly Boar Inn,", "he doesn't seem to mind giving away clues.", 591);
                        player.getDialogueManager().finishDialogue();
                        return true;
                    }
                    case 14: {
                        player.getDialogueManager().showNpcTwoLineDialogue("Well, there's the sword shop across the road, or ", "there's also all sort of shops up around the market.", 591);
                        player.getDialogueManager().finishDialogue();
                        return true;
                    }
                }
                break;
            }
            case 36: {
                switch (player.getDialogueManager().getDialogueStep()) {
                    case 1: {
                        player.getDialogueManager().showNpcThreeLineDialogue("I'm the head gardener around here.", "If you're looking for woad leaves, or if you need help", "with owt, I'm yer man.", 589);
                        return true;
                    }
                    case 2: {
                        player.getDialogueManager().showThreeOptionsWithTitle("What would you like to say?", "Yes please, I need woad leaves.", "How about ME helping YOU instead?", "Sorry, but I'm not interested.");
                        return true;
                    }
                    case 3: {
                        switch (optionIndex) {
                            case 1: {
                                player.getDialogueManager().showPlayerOneLineDialogue("Yes please, I need woad leaves.", 589);
                                player.getDialogueManager().setNextDialogueStep(4);
                                return true;
                            }
                            case 2: {
                                player.getDialogueManager().showOneLineStatement("This option is currently missing...");
                                player.getDialogueManager().setNextDialogueStep(2);
                                return true;
                            }
                            case 3: {
                                player.getDialogueManager().finishDialogue();
                                return true;
                            }
                        }
                        break continueContextDialogueControlSwitch1;
                    }
                    case 4: {
                        player.getDialogueManager().showNpcOneLineDialogue("How much are you willing to pay?", 595);
                        return true;
                    }
                    case 5: {
                        player.getDialogueManager().showFourOptionsWithTitle("What would you like to say?", "How about 5 coins?", "How about 10 coins?", "How about 15 coins?", "How about 20 coins?");
                        return true;
                    }
                    case 6: {
                        switch (optionIndex) {
                            case 4: {
                                player.getDialogueManager().showPlayerOneLineDialogue("How about 20 coins?", 589);
                                player.getDialogueManager().setNextDialogueStep(7);
                                return true;
                            }
                            case 1: 
                            case 2: 
                            case 3: {
                                player.getDialogueManager().showOneLineStatement("This option is currently missing...");
                                player.getDialogueManager().setNextDialogueStep(5);
                                return true;
                            }
                        }
                        break continueContextDialogueControlSwitch1;
                    }
                    case 7: {
                        player.getDialogueManager().showNpcOneLineDialogue("Ok that's more than fair.", 589);
                        return true;
                    }
                    case 8: {
                        player.getDialogueManager().showNpcOneLineDialogue("Here, have two, you're a generous person.", 589);
                        return true;
                    }
                    case 9: {
                        if (player.getInventoryManager().containsItemAmount(995, 20)) {
                            player.getDialogueManager().showPlayerOneLineDialogue("Thanks.", 589);
                            player.getInventoryManager().removeItem(new ItemStack(995, 20));
                            player.getInventoryManager().addOrDropItem(new ItemStack(1793, 2));
                            player.getDialogueManager().finishDialogue();
                        } else {
                            player.getDialogueManager().showNpcOneLineDialogue("You don't have enough coins with you.", 589);
                            player.getDialogueManager().finishDialogue();
                        }
                        return true;
                    }
                }
                break;
            }
            case 922: {
                switch (player.getDialogueManager().getDialogueStep()) {
                    case 1: {
                        player.getDialogueManager().showNpcOneLineDialogue("What can I help you with?", 589);
                        return true;
                    }
                    case 2: {
                        player.getDialogueManager().showFourOptions("What could you make for me?", "Cool, do you turn people into frogs?", "You mad old witch, you can't help me.", "Can you make dyes for me please?");
                        return true;
                    }
                    case 3: {
                        switch (optionIndex) {
                            case 1: {
                                player.getDialogueManager().showPlayerOneLineDialogue("What could you make for me?", 589);
                                player.getDialogueManager().setNextDialogueStep(11);
                                return true;
                            }
                            case 2: 
                            case 3: {
                                player.getDialogueManager().showOneLineStatement("This option is currently missing...");
                                player.getDialogueManager().setNextDialogueStep(2);
                                return true;
                            }
                            case 4: {
                                player.getDialogueManager().showPlayerOneLineDialogue("Can you make dyes for me please?", 589);
                                player.getDialogueManager().setNextDialogueStep(4);
                                return true;
                            }
                        }
                        break continueContextDialogueControlSwitch1;
                    }
                    case 4: {
                        player.getDialogueManager().showNpcOneLineDialogue("What sort of dye would you like? Red, yellow or blue?", 589);
                        return true;
                    }
                    case 5: {
                        player.getDialogueManager().showThreeOptions("What do you need to make red dye?", "What do you need to make yellow dye?", "What do you need to make blue dye?");
                        return true;
                    }
                    case 6: {
                        switch (optionIndex) {
                            case 1: {
                                player.getDialogueManager().showPlayerOneLineDialogue("What do you need to make red dye?", 589);
                                player.getDialogueManager().setNextDialogueStep(7);
                                return true;
                            }
                            case 2: {
                                player.getDialogueManager().showPlayerOneLineDialogue("What do you need to make yellow dye?", 589);
                                player.getDialogueManager().setNextDialogueStep(14);
                                return true;
                            }
                            case 3: {
                                player.getDialogueManager().showPlayerOneLineDialogue("What do you need to make blue dye?", 589);
                                player.getDialogueManager().setNextDialogueStep(18);
                                return true;
                            }
                        }
                        break continueContextDialogueControlSwitch1;
                    }
                    case 7: {
                        player.getDialogueManager().showNpcOneLineDialogue("3 lots of redberries and 5 coins to you.", 589);
                        return true;
                    }
                    case 8: {
                        player.getDialogueManager().showFiveOptions("Okay, make me some red dye please.", "I don't think I have all the ingredients yet.", "I can do without dye at that price.", "Where do I get redberries?", "What other colours can you make?");
                        return true;
                    }
                    case 9: {
                        switch (optionIndex) {
                            case 1: {
                                player.getDialogueManager().showPlayerOneLineDialogue("Ok make me some red dye please.", 589);
                                player.getDialogueManager().setNextDialogueStep(10);
                                return true;
                            }
                            case 2: 
                            case 3: {
                                player.getDialogueManager().finishDialogue();
                                return true;
                            }
                            case 4: {
                                player.getDialogueManager().showOneLineStatement("This option is currently missing...");
                                player.getDialogueManager().setNextDialogueStep(8);
                                return true;
                            }
                            case 5: {
                                player.getDialogueManager().showPlayerOneLineDialogue("What other colours can you make?", 589);
                                player.getDialogueManager().setNextDialogueStep(4);
                                return true;
                            }
                        }
                        break continueContextDialogueControlSwitch1;
                    }
                    case 10: {
                        if (player.getInventoryManager().containsItemAmount(1951, 3) && player.getInventoryManager().containsItemAmount(995, 5)) {
                            player.getDialogueManager().showTwoItemMessage("You hand the berries and payment to Aggie. Aggie", "produces a red bottle and hands it to you.", new ItemStack(-1, 1), new ItemStack(1763, 1));
                            player.getInventoryManager().removeItem(new ItemStack(1951, 3));
                            player.getInventoryManager().removeItem(new ItemStack(995, 5));
                            player.getInventoryManager().addOrDropItem(new ItemStack(1763, 1));
                            player.getDialogueManager().finishDialogue();
                        } else {
                            player.getDialogueManager().showNpcOneLineDialogue("You don't have the ingredients for me to do that.", 589);
                            player.getDialogueManager().finishDialogue();
                        }
                        return true;
                    }
                    case 11: {
                        player.getDialogueManager().showNpcFourLineDialogue("I mostly just make what I find pretty. I sometimes", "make dye for the women's clothes to brighten the place", "up. I can make red, yellow and blue dyes. If you'd like", "some, just bring me the appropriate ingredients.", 589);
                        return true;
                    }
                    case 12: {
                        player.getDialogueManager().showFourOptions("What do you need to make red dye?", "What do you need to make yellow dye?", "What do you need to make blue dye?", "No thanks, I am happy the colour I am.");
                        return true;
                    }
                    case 13: {
                        switch (optionIndex) {
                            case 1: {
                                player.getDialogueManager().showPlayerOneLineDialogue("What do you need to make red dye?", 589);
                                player.getDialogueManager().setNextDialogueStep(7);
                                return true;
                            }
                            case 2: {
                                player.getDialogueManager().showPlayerOneLineDialogue("What do you need to make yellow dye?", 589);
                                player.getDialogueManager().setNextDialogueStep(14);
                                return true;
                            }
                            case 3: {
                                player.getDialogueManager().showPlayerOneLineDialogue("What do you need to make blue dye?", 589);
                                player.getDialogueManager().setNextDialogueStep(18);
                                return true;
                            }
                            case 4: {
                                player.getDialogueManager().showPlayerOneLineDialogue("No thanks, I am happy the colour I am.", 589);
                                player.getDialogueManager().finishDialogue();
                                return true;
                            }
                        }
                        break continueContextDialogueControlSwitch1;
                    }
                    case 14: {
                        player.getDialogueManager().showNpcTwoLineDialogue("Yellow is a strange colour to get, comes from onion", "skins. I need 2 onions and 5 coins to make yellow dye.", 589);
                        return true;
                    }
                    case 15: {
                        player.getDialogueManager().showFiveOptions("Okay, make me some yellow dye please.", "I don't think I have all the ingredients yet.", "I can do without dye at that price.", "Where do I get onions?", "What other colours can you make?");
                        return true;
                    }
                    case 16: {
                        switch (optionIndex) {
                            case 1: {
                                player.getDialogueManager().showPlayerOneLineDialogue("Ok make me some yellow dye please.", 589);
                                player.getDialogueManager().setNextDialogueStep(17);
                                return true;
                            }
                            case 2: 
                            case 3: {
                                player.getDialogueManager().finishDialogue();
                                return true;
                            }
                            case 4: {
                                player.getDialogueManager().showOneLineStatement("This option is currently missing...");
                                player.getDialogueManager().setNextDialogueStep(15);
                                return true;
                            }
                            case 5: {
                                player.getDialogueManager().showPlayerOneLineDialogue("What other colours can you make?", 589);
                                player.getDialogueManager().setNextDialogueStep(4);
                                return true;
                            }
                        }
                        break continueContextDialogueControlSwitch1;
                    }
                    case 17: {
                        if (player.getInventoryManager().containsItemAmount(1957, 2) && player.getInventoryManager().containsItemAmount(995, 5)) {
                            player.getDialogueManager().showTwoItemMessage("You hand the onions and payment to Aggie. Aggie", "produces a yellow bottle and hands it to you.", new ItemStack(-1, 1), new ItemStack(1765, 1));
                            player.getInventoryManager().removeItem(new ItemStack(1957, 2));
                            player.getInventoryManager().removeItem(new ItemStack(995, 5));
                            player.getInventoryManager().addOrDropItem(new ItemStack(1765, 1));
                            player.getDialogueManager().finishDialogue();
                        } else {
                            player.getDialogueManager().showNpcOneLineDialogue("You don't have the ingredients for me to do that.", 589);
                            player.getDialogueManager().finishDialogue();
                        }
                        return true;
                    }
                    case 18: {
                        player.getDialogueManager().showNpcOneLineDialogue("2 woad leaves and 5 coins to you.", 589);
                        return true;
                    }
                    case 19: {
                        player.getDialogueManager().showFiveOptions("Okay, make me some blue dye please.", "I don't think I have all the ingredients yet.", "I can do without dye at that price.", "Where do I get woad leaves?", "What other colours can you make?");
                        return true;
                    }
                    case 20: {
                        switch (optionIndex) {
                            case 1: {
                                player.getDialogueManager().showPlayerOneLineDialogue("Ok make me some blue dye please.", 589);
                                player.getDialogueManager().setNextDialogueStep(21);
                                return true;
                            }
                            case 2: 
                            case 3: {
                                player.getDialogueManager().finishDialogue();
                                return true;
                            }
                            case 4: {
                                player.getDialogueManager().showOneLineStatement("This option is currently missing...");
                                player.getDialogueManager().setNextDialogueStep(19);
                                return true;
                            }
                            case 5: {
                                player.getDialogueManager().showPlayerOneLineDialogue("What other colours can you make?", 589);
                                player.getDialogueManager().setNextDialogueStep(4);
                                return true;
                            }
                        }
                        break continueContextDialogueControlSwitch1;
                    }
                    case 21: {
                        if (player.getInventoryManager().containsItemAmount(1793, 2) && player.getInventoryManager().containsItemAmount(995, 5)) {
                            player.getDialogueManager().showTwoItemMessage("You hand the woad leaves and payment to Aggie. Aggie", "produces a blue bottle and hands it to you.", new ItemStack(-1, 1), new ItemStack(1767, 1));
                            player.getInventoryManager().removeItem(new ItemStack(1793, 2));
                            player.getInventoryManager().removeItem(new ItemStack(995, 5));
                            player.getInventoryManager().addOrDropItem(new ItemStack(1767, 1));
                            player.getDialogueManager().finishDialogue();
                        } else {
                            player.getDialogueManager().showNpcOneLineDialogue("You don't have the ingredients for me to do that.", 589);
                            player.getDialogueManager().finishDialogue();
                        }
                        return true;
                    }
                }
                break;
            }
            case 918: {
                switch (player.getDialogueManager().getDialogueStep()) {
                    case 1: {
                        player.getDialogueManager().showNpcThreeLineDialogue("Why, hello there, lad. Me friends call me Ned. I was a", "man of the sea, but it's past me now. Could I be", "making or selling you some rope?", 591);
                        return true;
                    }
                    case 2: {
                        player.getDialogueManager().showTwoOptions("Yes, I would like some rope.", "No thanks Ned, I don't need any.");
                        return true;
                    }
                    case 3: {
                        switch (optionIndex) {
                            case 1: {
                                player.getDialogueManager().showPlayerOneLineDialogue("Yes, I would like some rope.", 591);
                                return true;
                            }
                            case 2: {
                                player.getDialogueManager().showPlayerOneLineDialogue("No thanks Ned, I don't need any.", 591);
                                player.getDialogueManager().finishDialogue();
                                return true;
                            }
                        }
                        break;
                    }
                    case 4: {
                        player.getDialogueManager().showNpcThreeLineDialogue("Well, I can sell you some rope for 15 coins. Or I can", "be making you some if you gets me 4 balls of wool. I", "strands them together I does, makes em strong.", 591);
                        return true;
                    }
                    case 5: {
                        player.getDialogueManager().showPlayerOneLineDialogue("You make rope from wool?", 591);
                        return true;
                    }
                    case 6: {
                        player.getDialogueManager().showNpcOneLineDialogue("Of course you can!", 591);
                        return true;
                    }
                    case 7: {
                        player.getDialogueManager().showPlayerOneLineDialogue("I thought you needed hemp or jute.", 591);
                        return true;
                    }
                    case 8: {
                        player.getDialogueManager().showNpcOneLineDialogue("Do you want some rope or not?", 591);
                        return true;
                    }
                    case 9: {
                        player.getDialogueManager().showThreeOptions("Okay, please sell me some rope.", "I have balls of wool. Could you make me some rope?", "That's a little more than I want to pay.");
                        return true;
                    }
                    case 10: {
                        switch (optionIndex) {
                            case 1: {
                                player.getDialogueManager().showPlayerOneLineDialogue("Okay, please sell me some rope.", 591);
                                player.getDialogueManager().setNextDialogueStep(11);
                                if (!player.getInventoryManager().containsItemAmount(995, 15)) {
                                    player.getDialogueManager().finishDialogue();
                                }
                                return true;
                            }
                            case 2: {
                                player.getDialogueManager().showPlayerOneLineDialogue("I have balls of wool. Could you make me some rope?", 591);
                                player.getDialogueManager().setNextDialogueStep(14);
                                if (!player.getInventoryManager().containsItemAmount(1759, 4)) {
                                    player.getDialogueManager().finishDialogue();
                                }
                                return true;
                            }
                            case 3: {
                                player.getDialogueManager().showPlayerOneLineDialogue("That's a little more than I want to pay.", 591);
                                player.getDialogueManager().finishDialogue();
                                return true;
                            }
                        }
                        break;
                    }
                    case 11: {
                        player.getDialogueManager().showNpcOneLineDialogue("There you go, finest rope in RuneScape.", 591);
                        return true;
                    }
                    case 12: {
                        player.getDialogueManager().showOneLineStatement("You hand Ned 15 coins. Ned gives you a coil of rope.");
                        return true;
                    }
                    case 13: {
                        if (!player.getInventoryManager().containsItemAmount(995, 15)) break;
                        player.getInventoryManager().removeItem(new ItemStack(995, 15));
                        player.getInventoryManager().addOrDropItem(new ItemStack(954, 1));
                        player.getDialogueManager().finishDialogue();
                        break;
                    }
                    case 14: {
                        player.getDialogueManager().showNpcOneLineDialogue("There you go, finest rope in RuneScape.", 591);
                        return true;
                    }
                    case 15: {
                        player.getDialogueManager().showOneLineStatement("You hand Ned 4 balls of wool. Ned gives you a coil of rope.");
                        return true;
                    }
                    case 16: {
                        if (!player.getInventoryManager().containsItemAmount(1759, 4)) break;
                        player.getInventoryManager().removeItem(new ItemStack(1759, 4));
                        player.getInventoryManager().addOrDropItem(new ItemStack(954, 1));
                        player.getDialogueManager().finishDialogue();
                    }
                }
                break;
            }
            case 379: {
                if (player.piratesTreasureBananaCrateCount != 10) {
                    switch (player.getDialogueManager().getDialogueStep()) {
                        case 1: {
                            player.getDialogueManager().showNpcOneLineDialogue("Hello I'm Luthas, I run the banana plantation here.", 591);
                            return true;
                        }
                        case 2: {
                            player.getDialogueManager().showTwoOptions("Could you offer me employment on your plantation?", "That customs officer is annoying isn't she?");
                            return true;
                        }
                        case 3: {
                            switch (optionIndex) {
                                case 1: {
                                    player.getDialogueManager().showPlayerOneLineDialogue("Could you offer me employment on your plantation?", 591);
                                    return true;
                                }
                                case 2: {
                                    player.getDialogueManager().finishDialogue();
                                }
                            }
                            break continueContextDialogueControlSwitch1;
                        }
                        case 4: {
                            player.getDialogueManager().showNpcTwoLineDialogue("Yes, I can sort something out. There's a crate ready to", "be loaded onto the ship.", 591);
                            return true;
                        }
                        case 5: {
                            player.getDialogueManager().showNpcThreeLineDialogue("You wouldn't believe the demand for bananas from", "Wydin's shop over in Port Sarim. I think this is the", "third crate I've shipped him this month..", 591);
                            return true;
                        }
                        case 6: {
                            player.getDialogueManager().showNpcTwoLineDialogue("If you could fill it up with bananas, I'll pay you 30", "gold.", 591);
                            player.getDialogueManager().finishDialogue();
                            return true;
                        }
                    }
                    break;
                }
                switch (player.getDialogueManager().getDialogueStep()) {
                    case 1: {
                        player.getDialogueManager().showPlayerOneLineDialogue("I've filled a crate with bananas.", 591);
                        return true;
                    }
                    case 2: {
                        player.getDialogueManager().showNpcOneLineDialogue("Well done, here's your payment.", 591);
                        return true;
                    }
                    case 3: {
                        player.piratesTreasureBananaCrateCount = 0;
                        if (player.getQuestState(10) == 3) {
                            player.setQuestState(10, 4);
                        }
                        player.getPacketSender().sendGameMessage("Luthas hands you 30 coins.");
                        player.getInventoryManager().addOrDropItem(new ItemStack(995, 30));
                        player.getDialogueManager().finishDialogue();
                    }
                }
                break;
            }
            case 488: {
                switch (player.getDialogueManager().getDialogueStep()) {
                    case 1: {
                        player.getDialogueManager().showPlayerOneLineDialogue("Hi, are you busy?", 591);
                        return true;
                    }
                    case 2: {
                        player.getDialogueManager().showNpcOneLineDialogue("What would you like to talk about?", 591);
                        return true;
                    }
                    case 3: {
                        player.getDialogueManager().showTwoOptions("Talk about the Observatory Quest.", "Talk about Treasure Trails.");
                        return true;
                    }
                    case 4: {
                        switch (optionIndex) {
                            case 1: {
                                player.getDialogueManager().finishDialogue();
                                player.getPacketSender().closeInterfaces();
                                return true;
                            }
                            case 2: {
                                if (player.ownsItem(2576)) {
                                    player.getDialogueManager().showNpcOneLineDialogue("I have already given you a chart!", 591);
                                    player.getDialogueManager().finishDialogue();
                                    return true;
                                }
                                if (player.getInventoryManager().containsItem(2574) && player.getInventoryManager().containsItem(2575)) {
                                    player.getDialogueManager().showPlayerOneLineDialogue("I've got the sextant and watch!", 591);
                                    player.getDialogueManager().setNextDialogueStep(14);
                                    return true;
                                }
                                player.getDialogueManager().showPlayerOneLineDialogue("Can you teach me to solve Treasure Trail clues?", 591);
                                player.getDialogueManager().setNextDialogueStep(5);
                                return true;
                            }
                        }
                        break continueContextDialogueControlSwitch1;
                    }
                    case 5: {
                        player.getDialogueManager().showNpcTwoLineDialogue("Ah, I get asked about treasure trails all the time!", "Listen carefully and I shall tell you what I know...", 591);
                        return true;
                    }
                    case 6: {
                        player.getDialogueManager().showNpcThreeLineDialogue("Lots of clues have degrees and minutes written on", "them. These are the coordinates of the place where the", "treasure is buried.", 591);
                        return true;
                    }
                    case 7: {
                        player.getDialogueManager().showNpcThreeLineDialogue("You have to walk to the correct spot, so that your", "coordinates are exactly the same as the values written", "on the clue scroll.", 591);
                        return true;
                    }
                    case 8: {
                        player.getDialogueManager().showNpcTwoLineDialogue("To do this, you must use a sextant, a watch and a", "chart to find the coordinates of where you are.", 591);
                        return true;
                    }
                    case 9: {
                        player.getDialogueManager().showNpcThreeLineDialogue("Once you know the coordinates of your position, you", "know which way you have to walk to get to the", "treasure's coordinates!", 591);
                        return true;
                    }
                    case 10: {
                        player.getDialogueManager().showPlayerOneLineDialogue("Riiight. So where do I get these items from?", 591);
                        return true;
                    }
                    case 11: {
                        player.getDialogueManager().showNpcFourLineDialogue("I think Murphy, the owner of the Fishing Trawler", "moored at Port Khazard, might be able to spare you a", "sextant. After that, the nearest clock tower is south of", "Ardougne - you could probably get a watch there. I've", 591);
                        return true;
                    }
                    case 12: {
                        player.getDialogueManager().showNpcThreeLineDialogue("got plenty of charts myself; just come back here when", "you've got the sextant and watch, and I'll give you one", "and teach you how to use them.", 591);
                        player.treasureTrailNavigationTaught = true;
                        return true;
                    }
                    case 13: {
                        player.getDialogueManager().showPlayerOneLineDialogue("Thanks, I'll see you later.", 591);
                        player.getDialogueManager().finishDialogue();
                        return true;
                    }
                    case 14: {
                        player.getDialogueManager().showNpcOneLineDialogue("Well done!", 591);
                        return true;
                    }
                    case 15: {
                        player.getDialogueManager().showNpcThreeLineDialogue("You use the sextant to measure the angle that the sun", "is currently at. You need the watch so that you know", "what the time is back here at the observatory.", 591);
                        return true;
                    }
                    case 16: {
                        player.getDialogueManager().showNpcFourLineDialogue("You then need this chart to work out your position.", "Your position is recorded in terms of latitude and", "longitude. Latitude is your position above the equator", "and longitude is your position relative to here.", 591);
                        return true;
                    }
                    case 17: {
                        player.getInventoryManager().addOrDropItem(new ItemStack(2576, 1));
                        player.getDialogueManager().showItemIdMessage("The professor has given you a navigation chart.", 2576);
                        return true;
                    }
                    case 18: {
                        player.getDialogueManager().showNpcTwoLineDialogue("So, if you have your sextant, watch and chart with you", "then you can work out exactly where you are!", 591);
                        player.getDialogueManager().finishDialogue();
                        return true;
                    }
                }
                break;
            }
            case 3863: {
                switch (player.getDialogueManager().getDialogueStep()) {
                    case 1: {
                        player.getDialogueManager().showNpcOneLineDialogue("What can I do for you?", 591);
                        return true;
                    }
                    case 2: {
                        player.getDialogueManager().showTwoOptions("I'd like to set up trade offers please.", "I'm fine, thanks.");
                        return true;
                    }
                    case 3: {
                        switch (optionIndex) {
                            case 1: {
                                player.getDialogueManager().showPlayerOneLineDialogue("I'd like to set up trade offers please.", 591);
                                player.getDialogueManager().setNextDialogueStep(4);
                                return true;
                            }
                            case 2: {
                                player.getDialogueManager().showPlayerOneLineDialogue("I'm fine, thanks.", 591);
                                player.getDialogueManager().finishDialogue();
                                return true;
                            }
                        }
                        break;
                    }
                    case 4: {
                        GrandExchangeManager.openGrandExchange(player);
                        player.getDialogueManager().markDialogueInactive();
                    }
                }
                break;
            }
            case 223: {
                switch (player.getDialogueManager().getDialogueStep()) {
                    case 1: {
                        if (player.ownsItem(2575) || !player.treasureTrailNavigationTaught) {
                            return false;
                        }
                        player.getDialogueManager().showPlayerOneLineDialogue("Hello.", 591);
                        return true;
                    }
                    case 2: {
                        player.getDialogueManager().showNpcOneLineDialogue("Hello, traveller, how can I help?", 591);
                        return true;
                    }
                    case 3: {
                        player.getDialogueManager().showPlayerOneLineDialogue("I'm trying to learn how to be a navigator.", 591);
                        return true;
                    }
                    case 4: {
                        player.getDialogueManager().showNpcOneLineDialogue("I don't know if I can help you there.", 591);
                        return true;
                    }
                    case 5: {
                        player.getDialogueManager().showPlayerTwoLineDialogue("The professor from the Observatory says that I need a", "watch.", 591);
                        return true;
                    }
                    case 6: {
                        player.getDialogueManager().showNpcFourLineDialogue("Ah, that I can help you with. I've been tinkering with", "this new idea of a watch and made a few. The problem", "is the villagers don't see the point as they have the Clock", "Tower!", 591);
                        return true;
                    }
                    case 7: {
                        player.getDialogueManager().showPlayerOneLineDialogue("Can I have one?", 591);
                        return true;
                    }
                    case 8: {
                        player.getDialogueManager().showNpcOneLineDialogue("You can have this one! It's the display model.", 591);
                        return true;
                    }
                    case 9: {
                        player.getInventoryManager().addOrDropItem(new ItemStack(2575, 1));
                        player.getDialogueManager().showItemIdMessage("Brother Kojo has given you a watch.", 2575);
                        player.getDialogueManager().finishDialogue();
                        return true;
                    }
                }
                break;
            }
            case 463: {
                switch (player.getDialogueManager().getDialogueStep()) {
                    case 1: {
                        if (player.ownsItem(2574) || !player.treasureTrailNavigationTaught) {
                            return false;
                        }
                        player.getDialogueManager().showPlayerOneLineDialogue("Ahoy there!", 591);
                        return true;
                    }
                    case 2: {
                        player.getDialogueManager().showNpcOneLineDialogue("Ahoy!", 591);
                        return true;
                    }
                    case 3: {
                        player.getDialogueManager().showPlayerOneLineDialogue("I'm trying to learn how to be a navigator.", 591);
                        return true;
                    }
                    case 4: {
                        player.getDialogueManager().showNpcTwoLineDialogue("Well, you've come to the right place, m'hearty! What do", "you need to know?", 591);
                        return true;
                    }
                    case 5: {
                        player.getDialogueManager().showPlayerTwoLineDialogue("The professor said that I need to have a sextant. Do", "you know where I can get one?", 591);
                        return true;
                    }
                    case 6: {
                        player.getDialogueManager().showNpcTwoLineDialogue("Hmm. I used to use a sextant when I was a young", "fella.", 591);
                        return true;
                    }
                    case 7: {
                        player.getDialogueManager().showPlayerOneLineDialogue("Do you still have it?", 591);
                        return true;
                    }
                    case 8: {
                        player.getDialogueManager().showNpcOneLineDialogue("Aye.", 591);
                        return true;
                    }
                    case 9: {
                        player.getDialogueManager().showPlayerOneLineDialogue("Could I have it?", 591);
                        return true;
                    }
                    case 10: {
                        player.getDialogueManager().showNpcOneLineDialogue("Aye.", 591);
                        return true;
                    }
                    case 11: {
                        player.getInventoryManager().addOrDropItem(new ItemStack(2574, 1));
                        player.getDialogueManager().showItemIdMessage("Murphy has given you his old sextant.", 2574);
                        return true;
                    }
                    case 12: {
                        player.getDialogueManager().showPlayerOneLineDialogue("Don't you still need it?", 591);
                        return true;
                    }
                    case 13: {
                        player.getDialogueManager().showNpcTwoLineDialogue("I can tell from the taste of the sea spray where I am,", "m'hearty!", 591);
                        return true;
                    }
                    case 14: {
                        player.getDialogueManager().showPlayerOneLineDialogue("Wow!", 591);
                        player.getDialogueManager().finishDialogue();
                        return true;
                    }
                }
                break;
            }
            case 543: {
                switch (player.getDialogueManager().getDialogueStep()) {
                    case 1: {
                        player.getDialogueManager().showNpcOneLineDialogue("Would you like to buy a nice kebab? Only one gold.", 589);
                        return true;
                    }
                    case 2: {
                        player.getDialogueManager().showTwoOptions("I think I'll give it a miss.", "Yes please.");
                        return true;
                    }
                    case 3: {
                        switch (optionIndex) {
                            case 1: {
                                player.getDialogueManager().showPlayerOneLineDialogue("I think I'll give it a miss.", 601);
                                player.getDialogueManager().finishDialogue();
                                return true;
                            }
                            case 2: {
                                player.getDialogueManager().showPlayerOneLineDialogue("Yes please.", 591);
                                return true;
                            }
                        }
                        break continueContextDialogueControlSwitch1;
                    }
                    case 4: {
                        if (player.getInventoryManager().containsItemStack(new ItemStack(995))) {
                            player.getInventoryManager().removeItem(new ItemStack(995));
                            player.getInventoryManager().addOrDropItem(new ItemStack(1971));
                            player.getPacketSender().sendGameMessage("You buy a kebab.");
                            break continueContextDialogueControlSwitch1;
                        }
                        player.getDialogueManager().showPlayerOneLineDialogue("Oops, I forgot to bring any money with me.", 599);
                        player.getDialogueManager().finishDialogue();
                        return true;
                    }
                }
                break;
            }
            case 539: {
                switch (player.getDialogueManager().getDialogueStep()) {
                    case 1: {
                        player.getDialogueManager().showNpcOneLineDialogue("Do you want to buy any fine silks?", 591);
                        return true;
                    }
                    case 2: {
                        player.getDialogueManager().showTwoOptions("How much are they?", "No, silk doesn't suit me.");
                        return true;
                    }
                    case 3: {
                        switch (optionIndex) {
                            case 1: {
                                player.getDialogueManager().showPlayerOneLineDialogue("How much are they?", 591);
                                return true;
                            }
                            case 2: {
                                player.getDialogueManager().showPlayerOneLineDialogue("No, silk doesn't suit me.", 591);
                                player.getDialogueManager().finishDialogue();
                                return true;
                            }
                        }
                        break continueContextDialogueControlSwitch1;
                    }
                    case 4: {
                        player.getDialogueManager().showNpcOneLineDialogue("3gp.", 591);
                        return true;
                    }
                    case 5: {
                        player.getDialogueManager().showTwoOptions("No, that's too much for me.", "Okay, that sounds good.");
                        return true;
                    }
                    case 6: {
                        switch (optionIndex) {
                            case 1: {
                                player.getDialogueManager().showPlayerOneLineDialogue("No, that's too much for me.", 591);
                                return true;
                            }
                            case 2: {
                                player.getDialogueManager().showPlayerOneLineDialogue("Okay, that sounds good.", 591);
                                player.getDialogueManager().setNextDialogueStep(12);
                                return true;
                            }
                        }
                        break continueContextDialogueControlSwitch1;
                    }
                    case 7: {
                        player.getDialogueManager().showNpcOneLineDialogue("2gp and that's as low as I'll go.", 591);
                        return true;
                    }
                    case 8: {
                        player.getDialogueManager().showNpcTwoLineDialogue("I'm not selling it for any less. You'll only", "go and sell it in Varrock for a profit.", 591);
                        return true;
                    }
                    case 9: {
                        player.getDialogueManager().showTwoOptions("2gp sounds good.", "No, really, I don't want it.");
                        return true;
                    }
                    case 10: {
                        switch (optionIndex) {
                            case 1: {
                                player.getDialogueManager().showPlayerOneLineDialogue("2gp sounds good.", 591);
                                player.getDialogueManager().setNextDialogueStep(14);
                                return true;
                            }
                            case 2: {
                                player.getDialogueManager().showPlayerOneLineDialogue("No, really, I don't want it.", 591);
                                return true;
                            }
                        }
                        break continueContextDialogueControlSwitch1;
                    }
                    case 11: {
                        player.getDialogueManager().showNpcOneLineDialogue("Okay, but that's the best price your going to get.", 591);
                        player.getDialogueManager().finishDialogue();
                        return true;
                    }
                    case 12: {
                        if (player.getInventoryManager().containsItemStack(new ItemStack(995, 3))) {
                            player.getInventoryManager().removeItem(new ItemStack(995, 3));
                            player.getInventoryManager().addOrDropItem(new ItemStack(950));
                            player.getDialogueManager().showItemMessage("You buy some silk for 3gp.", new ItemStack(950));
                            player.getDialogueManager().finishDialogue();
                        } else {
                            player.getDialogueManager().showPlayerOneLineDialogue("Oh dear. I don't have enough money.", 599);
                        }
                        return true;
                    }
                    case 13: {
                        player.getDialogueManager().showNpcOneLineDialogue("Well, come back when you do have some money.", 614);
                        player.getDialogueManager().finishDialogue();
                        return true;
                    }
                    case 14: {
                        if (player.getInventoryManager().containsItemStack(new ItemStack(995, 2))) {
                            player.getInventoryManager().removeItem(new ItemStack(995, 2));
                            player.getInventoryManager().addOrDropItem(new ItemStack(950));
                            player.getDialogueManager().showItemMessage("You buy some silk for 2gp.", new ItemStack(950));
                            player.getDialogueManager().finishDialogue();
                        } else {
                            player.getDialogueManager().showPlayerOneLineDialogue("Oh dear. I don't have enough money.", 599);
                            player.getDialogueManager().setNextDialogueStep(13);
                        }
                        return true;
                    }
                }
                break;
            }
            case 2238: {
                switch (player.getDialogueManager().getDialogueStep()) {
                    case 1: {
                        player.getDialogueManager().showNpcOneLineDialogue("Hello there, can I help you?", 588);
                        dialogueId = GameUtil.randomInclusive(2);
                        player.getDialogueManager().setNextDialogueStep(dialogueId == 0 ? 2 : (dialogueId == 1 ? 6 : 4));
                        return true;
                    }
                    case 2: {
                        player.getDialogueManager().showFourOptions("Where am I?", "How are you today?", "Are there any quests I can do here?", "Where can I get a haircut like yours?");
                        return true;
                    }
                    case 3: {
                        switch (optionIndex) {
                            case 1: {
                                player.getDialogueManager().showNpcOneLineDialogue("This is the town of Lumbridge my friend.", 588);
                                return true;
                            }
                            case 2: {
                                player.getDialogueManager().showNpcTwoLineDialogue("Aye, not too bad thank you. Lovely weather", "in 06Scape this fine day.", 588);
                                player.getDialogueManager().setNextDialogueStep(10);
                                return true;
                            }
                            case 3: {
                                player.getDialogueManager().showNpcOneLineDialogue("What kind of quest are you looking for?", 595);
                                player.getDialogueManager().setNextDialogueStep(20);
                                return true;
                            }
                            case 4: {
                                player.getDialogueManager().showNpcOneLineDialogue("Yes, it does look like you need a hairdresser.", 588);
                                player.getDialogueManager().setNextDialogueStep(15);
                                return true;
                            }
                        }
                        break continueContextDialogueControlSwitch1;
                    }
                    case 4: {
                        player.getDialogueManager().showThreeOptions("How are you today?", "Are there any quests I can do here?", "Your shoe lace is united.");
                        return true;
                    }
                    case 5: {
                        switch (optionIndex) {
                            case 1: {
                                player.getDialogueManager().showNpcTwoLineDialogue("Aye, not too bad thank you. Lovely weather", "in 06Scape this fine day.", 588);
                                player.getDialogueManager().setNextDialogueStep(10);
                                return true;
                            }
                            case 2: {
                                player.getDialogueManager().showNpcOneLineDialogue("What kind of quest are you looking for?", 595);
                                player.getDialogueManager().setNextDialogueStep(20);
                                return true;
                            }
                            case 3: {
                                player.getDialogueManager().showNpcOneLineDialogue("No it's not!", 614);
                                player.getDialogueManager().setNextDialogueStep(18);
                                return true;
                            }
                        }
                        break continueContextDialogueControlSwitch1;
                    }
                    case 6: {
                        player.getDialogueManager().showThreeOptions("Do you have anything of value which I can have?", "Are there any quests I can do here?", "Can I buy your stick?");
                        return true;
                    }
                    case 7: {
                        switch (optionIndex) {
                            case 1: {
                                player.getDialogueManager().showNpcOneLineDialogue("Are you asking for free stuff?", 595);
                                return true;
                            }
                            case 2: {
                                player.getDialogueManager().showNpcOneLineDialogue("What kind of quest are you looking for?", 595);
                                player.getDialogueManager().setNextDialogueStep(20);
                                return true;
                            }
                            case 3: {
                                player.getDialogueManager().showNpcTwoLineDialogue("It's not a stick! I'll have you know it's", "a very powerful staff!", 591);
                                player.getDialogueManager().finishDialogue();
                                return true;
                            }
                        }
                        break continueContextDialogueControlSwitch1;
                    }
                    case 8: {
                        player.getDialogueManager().showPlayerOneLineDialogue("Well... er... yes.", 595);
                        return true;
                    }
                    case 9: {
                        player.getDialogueManager().showNpcThreeLineDialogue("No I dont not have anything I can give you.", "If I did have anything of value I wouldn't", "want to give it away.", 614);
                        player.getDialogueManager().finishDialogue();
                        return true;
                    }
                    case 10: {
                        player.getDialogueManager().showPlayerOneLineDialogue("Weather?", 589);
                        return true;
                    }
                    case 11: {
                        player.getDialogueManager().showNpcOneLineDialogue("Yes weather, you know.", 588);
                        return true;
                    }
                    case 12: {
                        player.getDialogueManager().showNpcFourLineDialogue("The state or condition of the atmosphere", "at a time and place, with respect to variables", "such as temperature, moisture, wind velocity,", "and barometric pressure.", 595);
                        return true;
                    }
                    case 13: {
                        player.getDialogueManager().showPlayerOneLineDialogue("...", 589);
                        return true;
                    }
                    case 14: {
                        player.getDialogueManager().showNpcOneLineDialogue("Not just a pretty face eh? Ha ha ha.", 605);
                        player.getDialogueManager().finishDialogue();
                        return true;
                    }
                    case 15: {
                        player.getDialogueManager().showPlayerOneLineDialogue("Oh thanks.", 614);
                        return true;
                    }
                    case 16: {
                        player.getDialogueManager().showNpcTwoLineDialogue("No problem. The hairdresser in Falador", "will probably be able to sort you out.", 605);
                        return true;
                    }
                    case 17: {
                        player.getDialogueManager().showNpcTwoLineDialogue("The Lumbridge general store sells useful maps", "if you don't know the way.", 605);
                        player.getDialogueManager().finishDialogue();
                        return true;
                    }
                    case 18: {
                        player.getDialogueManager().showPlayerOneLineDialogue("No you're right. I have nothing to back that up.", 614);
                        return true;
                    }
                    case 19: {
                        player.getDialogueManager().showNpcOneLineDialogue("Fool! Leave me alone!", 614);
                        player.getDialogueManager().finishDialogue();
                        return true;
                    }
                    case 20: {
                        player.getDialogueManager().showNpcOneLineDialogue("Sorry, quests have not been added yet.", 591);
                        player.getDialogueManager().finishDialogue();
                        return true;
                    }
                }
                break;
            }
            case 801: {
                continueDialogueWithNpcIdControlSwitch1 : switch (player.getDialogueManager().getDialogueStep()) {
                    case 1: {
                        player.getDialogueManager().showNpcOneLineDialogue("Hello brother, welcome to our monastery.", 591);
                        return true;
                    }
                    case 2: {
                        player.getDialogueManager().showThreeOptions("Can you heal me? I'm injured.", "Can I climb up those stairs?", "Good bye.");
                        return true;
                    }
                    case 3: {
                        switch (optionIndex) {
                            case 1: {
                                if (player.getSkillManager().getCurrentLevels()[3] < player.getSkillManager().getBaseLevel(3)) {
                                    if (player.getInteractionTarget() != null) {
                                        player.getInteractionTarget().getUpdateState().setAnimation(717);
                                    }
                                    player.getDialogueManager().showNpcOneLineDialogue("Sure, here you go.", 591);
                                    player.heal((int)((double)player.getSkillManager().getBaseLevel(3) * 0.3));
                                    player.getUpdateState().setGraphic(84);
                                    break continueDialogueWithNpcIdControlSwitch1;
                                }
                                player.getDialogueManager().showNpcOneLineDialogue("You already have full hp.", 591);
                                player.getDialogueManager().finishDialogue();
                                return true;
                            }
                            case 2: {
                                player.getDialogueManager().showNpcFourLineDialogue("Up those stairs is the prayer guild. You need a", "level of 31 Prayer to enter. There you will find", "an altar that can boost your prayer by 2 points,", "as well as some monk robes.", 591);
                                player.getDialogueManager().setNextDialogueStep(2);
                                return true;
                            }
                        }
                    }
                }
                break;
            }
            case 9998: {
                switch (player.getDialogueManager().getDialogueStep()) {
                    case 1: {
                        player.getDialogueManager().showTwoOptionsWithTitle("Let the sigil teleport you when worn?", "Yes", "No.");
                        return true;
                    }
                    case 2: {
                        switch (optionIndex) {
                            case 1: {
                                player.getPacketSender().sendGameMessage("The 10th squad sigil begins to shake violently!");
                                player.setActionLocked(true);
                                player.getPacketSender().showInterface(8677);
                                targetPlayer = player;
                                sigilTeleportTask = new TenthSquadSigilTeleportTask(5, targetPlayer);
                                World.getTaskScheduler().schedule(sigilTeleportTask);
                                return true;
                            }
                        }
                    }
                }
                break;
            }
            case 9999: {
                player.getDialogueManager().setDialogueNpcId(926);
                continueDialogueWithNpcIdControlSwitch2 : switch (player.getDialogueManager().getDialogueStep()) {
                    case 1: {
                        if (player.botEnabled) {
                            AlKharidGateToll.tryPass(player);
                            break;
                        }
                        player.getDialogueManager().showPlayerOneLineDialogue("Can I come through this gate?", 591);
                        return true;
                    }
                    case 2: {
                        if (AlKharidGateToll.hasFreePassage(player)) {
                            player.getDialogueManager().showNpcOneLineDialogue("You may pass for free, you are a friend of Al-Kharid.", 591);
                            player.getDialogueManager().setNextDialogueStep(19);
                        } else {
                            player.getDialogueManager().showNpcOneLineDialogue("You must pay a toll of 10 gold coins to pass.", 591);
                            if (!player.getInventoryManager().containsItemAmount(995, 10)) {
                                player.getDialogueManager().setNextDialogueStep(7);
                            }
                        }
                        return true;
                    }
                    case 3: {
                        player.getDialogueManager().showThreeOptions("Ok, I'll pay.", "Who does the money go to?", "No thanks, I'll walk around.");
                        return true;
                    }
                    case 4: {
                        switch (optionIndex) {
                            case 1: {
                                AlKharidGateToll.tryPass(player);
                                break continueDialogueWithNpcIdControlSwitch2;
                            }
                            case 2: {
                                player.getDialogueManager().showNpcTwoLineDialogue("The money goes to the city of Al-Kharid.", "Will you pay the toll?", 591);
                                return true;
                            }
                            case 3: {
                                player.getDialogueManager().showNpcOneLineDialogue("As you wish. Don't go too near the scorpions.", 591);
                                player.getDialogueManager().finishDialogue();
                                return true;
                            }
                        }
                        break;
                    }
                    case 5: {
                        player.getDialogueManager().showTwoOptions("Ok, I'll pay.", "No thanks, I'll walk around.");
                        return true;
                    }
                    case 6: {
                        switch (optionIndex) {
                            case 1: {
                                AlKharidGateToll.tryPass(player);
                                break continueDialogueWithNpcIdControlSwitch2;
                            }
                            case 2: {
                                player.getDialogueManager().showNpcOneLineDialogue("As you wish. Don't go too near the scorpions.", 591);
                                player.getDialogueManager().finishDialogue();
                                return true;
                            }
                        }
                        break;
                    }
                    case 7: {
                        player.getDialogueManager().showTwoOptions("Who does the money go to?", "I haven't got that much.");
                        return true;
                    }
                    case 19: {
                        AlKharidGateToll.tryPass(player);
                        break;
                    }
                    case 8: {
                        switch (optionIndex) {
                            case 1: {
                                player.getDialogueManager().showNpcOneLineDialogue("The money goes to the city of Al-Kharid.", 591);
                                player.getDialogueManager().finishDialogue();
                                return true;
                            }
                        }
                    }
                }
                break;
            }
            case 166: 
            case 494: 
            case 495: 
            case 496: 
            case 498: 
            case 902: 
            case 2619: {
                switch (player.getDialogueManager().getDialogueStep()) {
                    case 1: {
                        player.getDialogueManager().showNpcOneLineDialogue("What can I do for you?", 591);
                        return true;
                    }
                    case 2: {
                        if (ServerSettings.cacheVersion >= 336) {
                            if (PartyRoomManager.partyChestValue >= 1000000 && PartyRoomManager.balloonDropPending) {
                                player.getDialogueManager().showFourOptions("I would like to access my bank account.", "I would like to edit my Bank Pin settings.", "Teleport me to Party Room.", "Nothing.");
                            } else {
                                player.getDialogueManager().showThreeOptions("I would like to access my bank account.", "I would like to edit my Bank Pin settings.", "Nothing.");
                            }
                        } else if (PartyRoomManager.partyChestValue >= 1000000 && PartyRoomManager.balloonDropPending) {
                            player.getDialogueManager().showThreeOptions("I would like to access my bank account.", "Teleport me to Party Room.", "Nothing.");
                        } else {
                            player.getDialogueManager().showTwoOptions("I would like to access my bank account.", "Nothing.");
                        }
                        return true;
                    }
                    case 3: {
                        if (ServerSettings.cacheVersion >= 336) {
                            switch (optionIndex) {
                                case 1: {
                                    player.getDialogueManager().showPlayerOneLineDialogue("I would like to access my bank account.", 591);
                                    return true;
                                }
                                case 2: {
                                    player.getDialogueManager().showPlayerOneLineDialogue("I would like to edit my Bank Pin settings.", 591);
                                    player.getDialogueManager().setNextDialogueStep(6);
                                    return true;
                                }
                                case 3: {
                                    if (PartyRoomManager.partyChestValue >= 1000000 && PartyRoomManager.balloonDropPending) {
                                        player.getDialogueManager().showPlayerOneLineDialogue("Teleport me to Party Room please.", 591);
                                        player.getDialogueManager().setNextDialogueStep(30);
                                    } else {
                                        player.getDialogueManager().showPlayerOneLineDialogue("Nothing.", 591);
                                        player.getDialogueManager().setNextDialogueStep(5);
                                    }
                                    return true;
                                }
                                case 4: {
                                    player.getDialogueManager().showPlayerOneLineDialogue("Nothing.", 591);
                                    player.getDialogueManager().setNextDialogueStep(5);
                                    return true;
                                }
                            }
                            break;
                        }
                        switch (optionIndex) {
                            case 1: {
                                player.getDialogueManager().showPlayerOneLineDialogue("I would like to access my bank account.", 591);
                                return true;
                            }
                            case 2: {
                                if (PartyRoomManager.partyChestValue >= 1000000 && PartyRoomManager.balloonDropPending) {
                                    player.getDialogueManager().showPlayerOneLineDialogue("Teleport me to Party Room please.", 591);
                                    player.getDialogueManager().setNextDialogueStep(30);
                                } else {
                                    player.getDialogueManager().showPlayerOneLineDialogue("Nothing.", 591);
                                    player.getDialogueManager().setNextDialogueStep(5);
                                }
                                return true;
                            }
                            case 3: {
                                player.getDialogueManager().showPlayerOneLineDialogue("Nothing.", 591);
                                player.getDialogueManager().setNextDialogueStep(5);
                                return true;
                            }
                        }
                        break;
                    }
                    case 4: {
                        BankManager.openBank(player);
                        player.getDialogueManager().markDialogueInactive();
                        break;
                    }
                    case 5: {
                        player.getDialogueManager().showNpcOneLineDialogue("Well, just let me know when I can help.", 591);
                        player.getDialogueManager().finishDialogue();
                        return true;
                    }
                    case 6: {
                        player.getDialogueManager().showNpcOneLineDialogue("What would you like to do?", 591);
                        return true;
                    }
                    case 7: {
                        if (player.getBankPinManager().hasPin() && !player.getBankPinManager().hasPendingPinChange()) {
                            player.getDialogueManager().showTwoOptions("I would like to change my bank pin.", "I would like to delete my bank pin.");
                        } else if (player.getBankPinManager().hasPin() && player.getBankPinManager().hasPendingPinChange()) {
                            player.getDialogueManager().showTwoOptions("I would like to delete my pending bank pin request.", "No, nevermind.");
                            player.getDialogueManager().setNextDialogueStep(22);
                        } else {
                            player.getDialogueManager().showTwoOptions("I would like to set a bank pin.", "No, nevermind.");
                            player.getDialogueManager().setNextDialogueStep(28);
                        }
                        return true;
                    }
                    case 8: {
                        switch (optionIndex) {
                            case 1: {
                                player.getDialogueManager().showPlayerOneLineDialogue("I would like to change my bank pin.", 591);
                                return true;
                            }
                            case 2: {
                                player.getDialogueManager().showPlayerOneLineDialogue("I would like to delete my bank pin.", 591);
                                player.getDialogueManager().setNextDialogueStep(18);
                                return true;
                            }
                        }
                        break;
                    }
                    case 9: {
                        player.getDialogueManager().showNpcOneLineDialogue("Please carefully select your bank pin.", 591);
                        return true;
                    }
                    case 10: {
                        player.getBankPinManager().setEntryMode(BankPinEntryMode.b);
                        player.getDialogueManager().markDialogueInactive();
                        break;
                    }
                    case 11: {
                        itemIds = player.getBankPinManager().getPendingPin();
                        player.getDialogueManager().showNpcTwoLineDialogue("Your bank pin will be set to " + itemIds[0] + " " + itemIds[1] + " " + itemIds[2] + " " + itemIds[3] + ".", "Does that sound correct?", 591);
                        return true;
                    }
                    case 12: {
                        player.getDialogueManager().showThreeOptions("Yes.", "No, may I try again?", "No, nevermind.");
                        return true;
                    }
                    case 13: {
                        switch (optionIndex) {
                            case 1: {
                                player.getBankPinManager().requestPinChange();
                                player.getDialogueManager().showPlayerOneLineDialogue("Yes.", 591);
                                return true;
                            }
                            case 2: {
                                player.getDialogueManager().showPlayerOneLineDialogue("No, may I try again?", 591);
                                player.getDialogueManager().setNextDialogueStep(16);
                                return true;
                            }
                            case 3: {
                                player.getDialogueManager().showPlayerOneLineDialogue("No, nevermind.", 591);
                                player.getDialogueManager().setNextDialogueStep(17);
                                return true;
                            }
                        }
                        break;
                    }
                    case 14: {
                        if (player.getBankPinManager().hasPin()) {
                            player.getDialogueManager().showNpcTwoLineDialogue("Changes will take affect in 7 days.", "Return to me to edit or delete this change.", 591);
                        } else {
                            player.getDialogueManager().showNpcOneLineDialogue("Your bank pin will be set accordingly.", 591);
                        }
                        player.getBankPinManager().processPendingPinChanges();
                        return true;
                    }
                    case 15: {
                        player.getDialogueManager().showPlayerOneLineDialogue("Will do.", 591);
                        player.getDialogueManager().finishDialogue();
                        return true;
                    }
                    case 16: {
                        player.getDialogueManager().showNpcOneLineDialogue("Sure.", 591);
                        player.getBankPinManager().clearPendingPinChange();
                        player.getDialogueManager().setNextDialogueStep(10);
                        return true;
                    }
                    case 17: {
                        player.getDialogueManager().showNpcOneLineDialogue("Return to me if you change your mind.", 591);
                        player.getBankPinManager().clearPendingPinChange();
                        player.getDialogueManager().finishDialogue();
                        return true;
                    }
                    case 18: {
                        player.getDialogueManager().showPlayerOneLineDialogue("I would like to delete my bank pin.", 591);
                        return true;
                    }
                    case 19: {
                        player.getDialogueManager().showNpcOneLineDialogue("Are you sure you would like to delete your bank pin?", 591);
                        return true;
                    }
                    case 20: {
                        player.getDialogueManager().showTwoOptions("Yes.", "No, nevermind.");
                        return true;
                    }
                    case 21: {
                        switch (optionIndex) {
                            case 1: {
                                player.getBankPinManager().requestPinDeletion();
                                player.getDialogueManager().showPlayerOneLineDialogue("Yes.", 591);
                                player.getDialogueManager().setNextDialogueStep(14);
                                return true;
                            }
                            case 2: {
                                player.getDialogueManager().showPlayerOneLineDialogue("No, nevermind.", 591);
                                player.getDialogueManager().setNextDialogueStep(29);
                                return true;
                            }
                        }
                        break;
                    }
                    case 22: {
                        switch (optionIndex) {
                            case 1: {
                                player.getDialogueManager().showPlayerOneLineDialogue("I would like to delete my pending bank pin request.", 591);
                                return true;
                            }
                        }
                        break;
                    }
                    case 23: {
                        player.getDialogueManager().showNpcTwoLineDialogue("Are you sure?", "This clears any deletion or change request.", 591);
                        return true;
                    }
                    case 24: {
                        player.getDialogueManager().showTwoOptions("Yes.", "No, nevermind.");
                        return true;
                    }
                    case 25: {
                        switch (optionIndex) {
                            case 1: {
                                player.getDialogueManager().showPlayerOneLineDialogue("Yes.", 591);
                                return true;
                            }
                            case 2: {
                                player.getDialogueManager().showPlayerOneLineDialogue("No, nevermind.", 591);
                                player.getDialogueManager().setNextDialogueStep(29);
                                return true;
                            }
                        }
                        break;
                    }
                    case 26: {
                        player.getBankPinManager().clearPendingPinChange();
                        player.getDialogueManager().showNpcOneLineDialogue("Your pending bank pin request has been deleted.", 591);
                        return true;
                    }
                    case 27: {
                        player.getDialogueManager().showPlayerOneLineDialogue("Thanks.", 591);
                        player.getDialogueManager().finishDialogue();
                        return true;
                    }
                    case 28: {
                        switch (optionIndex) {
                            case 1: {
                                player.getDialogueManager().showPlayerOneLineDialogue("I would like to set my bank pin.", 591);
                                player.getDialogueManager().setNextDialogueStep(9);
                                return true;
                            }
                            case 2: {
                                player.getDialogueManager().showPlayerOneLineDialogue("No, nevermind.", 591);
                                return true;
                            }
                        }
                        break;
                    }
                    case 29: {
                        player.getDialogueManager().showNpcOneLineDialogue("Return to me if you change your mind.", 591);
                        player.getDialogueManager().finishDialogue();
                        return true;
                    }
                    case 30: {
                        player.getTeleportManager().castItemTeleport(new Position(2736, 3477, 0));
                    }
                }
                break;
            }
            case 10001: {
                continueDialogueWithNpcIdControlSwitch3 : switch (player.getDialogueManager().getDialogueStep()) {
                    case 1: {
                        player.getDialogueManager().showOneLineStatement("You've found a hidden tunnel, do you want to enter?");
                        return true;
                    }
                    case 2: {
                        player.getDialogueManager().showTwoOptions("Yeah I'm fearless!", "No way, that looks scary!");
                        return true;
                    }
                    case 3: {
                        switch (optionIndex) {
                            case 1: {
                                BarrowsManager.generateTunnelRoute(player, true);
                                break continueDialogueWithNpcIdControlSwitch3;
                            }
                            case 2: {
                                player.getDialogueManager().showPlayerOneLineDialogue("No way, that looks scary!", 596);
                                player.getDialogueManager().finishDialogue();
                                return true;
                            }
                        }
                    }
                }
                break;
            }
            case 960: 
            case 961: 
            case 962: {
                switch (player.getDialogueManager().getDialogueStep()) {
                    case 1: {
                        player.getDialogueManager().showNpcThreeLineDialogue("Hey, what's up? you can call me " + NpcDefinition.forId(player.getInteractionTargetId()).getName(), "my mission here is to take care of those players", "who would need some help to cure their wounds", 591);
                        return true;
                    }
                    case 2: {
                        player.getDialogueManager().showNpcOneLineDialogue("So, anything I can do for someone like you?", 591);
                        return true;
                    }
                    case 3: {
                        player.getDialogueManager().showTwoOptions("Yes, I would like you to heal me please.", "No, I am just playing around.");
                        return true;
                    }
                    case 4: {
                        switch (optionIndex) {
                            case 1: {
                                player.getDialogueManager().showPlayerOneLineDialogue("Yes, I would like you to heal me please.", 591);
                                return true;
                            }
                            case 2: {
                                player.getDialogueManager().showPlayerOneLineDialogue("No, I am just playing around.", 591);
                                player.getDialogueManager().setNextDialogueStep(6);
                                return true;
                            }
                        }
                    }
                    case 5: {
                        player.getDuelSession().restoreHitpoints();
                        break continueContextDialogueControlSwitch1;
                    }
                    case 6: {
                        player.getDialogueManager().showNpcOneLineDialogue("So don't waste my time, seriously...", 614);
                        player.getDialogueManager().finishDialogue();
                        return true;
                    }
                }
                break;
            }
            case 741: {
                continueDialogueWithNpcIdControlSwitch4 : switch (player.getDialogueManager().getDialogueStep()) {
                    case 1: {
                        if (player.getQuestState(5) == 1 && !player.ownsItem(1540)) {
                            player.getDialogueManager().showNpcOneLineDialogue("Hello, welcome to my kingdom.", 591);
                            return true;
                        }
                    }
                    case 2: {
                        player.getDialogueManager().showTwoOptions("Can I have an anti-fire shield?", "Nevermind.");
                        return true;
                    }
                    case 3: {
                        switch (optionIndex) {
                            case 1: {
                                player.getDialogueManager().showNpcOneLineDialogue("Sure, it will only cost you 1k.", 591);
                                return true;
                            }
                            case 2: {
                                player.getDialogueManager().showPlayerOneLineDialogue("Nevermind.", 591);
                                player.getDialogueManager().finishDialogue();
                                return true;
                            }
                        }
                        break;
                    }
                    case 4: {
                        player.getDialogueManager().showTwoOptions("Ok, here you go.", "Nevermind.");
                        return true;
                    }
                    case 5: {
                        switch (optionIndex) {
                            case 1: {
                                if (!player.getInventoryManager().containsItemStack(new ItemStack(995, 1000))) {
                                    player.getDialogueManager().showPlayerOneLineDialogue("Sorry, I don't have enough coins.", 599);
                                } else if (player.getInventoryManager().getContainer().getFreeSlots() <= 0 && player.getInventoryManager().getContainer().getItemAmount(995) != 1000) {
                                    player.getDialogueManager().showNpcOneLineDialogue("Looks like you don't have enough inventory space.", 591);
                                } else {
                                    player.getInventoryManager().removeItem(new ItemStack(995, 1000));
                                    player.getInventoryManager().addItem(new ItemStack(1540, 1));
                                    break continueDialogueWithNpcIdControlSwitch4;
                                }
                                player.getDialogueManager().finishDialogue();
                                return true;
                            }
                            case 2: {
                                player.getDialogueManager().showPlayerOneLineDialogue("Nevermind.", 591);
                                player.getDialogueManager().finishDialogue();
                                return true;
                            }
                        }
                    }
                }
                break;
            }
            case 656: {
                switch (player.getDialogueManager().getDialogueStep()) {
                    case 1: {
                        player.getDialogueManager().showNpcThreeLineDialogue("Be careful going in there! You are unarmed, and there", "is much evilness lurking down there! The evilness seems", "to block off our contact with our gods,", 591);
                        return true;
                    }
                    case 2: {
                        player.getDialogueManager().showNpcThreeLineDialogue("so our prayers seem to have less effect down there. Oh,", "also, you won't be able to come back this way - This", "ladder only goes one way!", 591);
                        return true;
                    }
                    case 3: {
                        player.getDialogueManager().showNpcTwoLineDialogue("The only exit from the caves below is a portal which", "leads only to the deepest wilderness!", 591);
                        return true;
                    }
                    case 4: {
                        player.getDialogueManager().showTwoOptions("I don't think I'm strong enough to enter then.", "Well that is a risk I will have to take.");
                        return true;
                    }
                    case 5: {
                        switch (optionIndex) {
                            case 1: {
                                player.getDialogueManager().showPlayerOneLineDialogue("I don't think I'm strong enough to enter then.", 591);
                                player.getDialogueManager().finishDialogue();
                                return true;
                            }
                            case 2: {
                                player.getDialogueManager().showPlayerOneLineDialogue("Well that is a risk I will have to take.", 591);
                                return true;
                            }
                        }
                        break;
                    }
                    case 6: {
                        AttackStyleDefinition.startDelayedObjectMove(player, new Position(2822, 9774, 0));
                        player.getSkillManager().setCurrentLevel(5, 1);
                        player.getSkillManager().refreshSkill(5);
                        player.getDialogueManager().finishDialogue();
                    }
                }
                break;
            }
            case 658: 
            case 2728: 
            case 2729: {
                continueDialogueWithNpcIdControlSwitch5 : switch (player.getDialogueManager().getDialogueStep()) {
                    case 1: {
                        if (player.isMember()) {
                            if (ServerSettings.freeToPlayWorld) {
                                player.packetSender.sendGameMessage("You need to be in members world to access members content.");
                            } else {
                                player.getDialogueManager().showNpcTwoLineDialogue("Would you like to sail to Entrana?", "I will need to check you for dangerous equipment.", 591);
                            }
                        } else {
                            player.packetSender.sendGameMessage("You need a members account to access members content.");
                        }
                        return true;
                    }
                    case 2: {
                        player.getDialogueManager().showTwoOptions("Yes please.", "No thanks.");
                        return true;
                    }
                    case 3: {
                        switch (optionIndex) {
                            case 1: {
                                TravelManager.handleShipRoute(player, ShipRoute.PORT_SARIM_TO_ENTRANA);
                                player.getDialogueManager().markDialogueInactive();
                                break continueDialogueWithNpcIdControlSwitch5;
                            }
                            case 2: {
                                player.getDialogueManager().showPlayerOneLineDialogue("No thanks.", 591);
                                player.getDialogueManager().finishDialogue();
                                return true;
                            }
                        }
                    }
                }
                break;
            }
            case 1304: {
                switch (player.getDialogueManager().getDialogueStep()) {
                    case 1: {
                        player.getDialogueManager().showPlayerOneLineDialogue("Hello. Can I get a ride on your ship?", 591);
                        return true;
                    }
                    case 2: {
                        player.getDialogueManager().showNpcTwoLineDialogue("Hello again, brother Rilkal. If you're ready to jump", "aboard, we're all ready to set sail with the tide!", 591);
                        return true;
                    }
                    case 3: {
                        player.getDialogueManager().showTwoOptions("Let's go!", "Actually, no.");
                        return true;
                    }
                    case 4: {
                        switch (optionIndex) {
                            case 1: {
                                player.getDialogueManager().showPlayerOneLineDialogue("Let's go!", 591);
                                return true;
                            }
                            case 2: {
                                player.getDialogueManager().showPlayerOneLineDialogue("Actually, no.", 591);
                                player.getDialogueManager().finishDialogue();
                                return true;
                            }
                        }
                        break;
                    }
                    case 5: {
                        TravelManager.handleShipRoute(player, ShipRoute.RELLEKKA_TO_MISCELLANIA);
                        player.getDialogueManager().markDialogueInactive();
                        player.getPacketSender().sendGameMessage("You board the longship...");
                    }
                }
                break;
            }
            case 1385: {
                switch (player.getDialogueManager().getDialogueStep()) {
                    case 1: {
                        player.getDialogueManager().showPlayerOneLineDialogue("Hello. Can I get a ride on your ship?", 591);
                        return true;
                    }
                    case 2: {
                        player.getDialogueManager().showNpcTwoLineDialogue("Hello again, brother Rilkal. If you're ready to jump", "aboard, we're all ready to set sail with the tide!", 591);
                        return true;
                    }
                    case 3: {
                        player.getDialogueManager().showTwoOptions("Let's go!", "Actually, no.");
                        return true;
                    }
                    case 4: {
                        switch (optionIndex) {
                            case 1: {
                                player.getDialogueManager().showPlayerOneLineDialogue("Let's go!", 591);
                                return true;
                            }
                            case 2: {
                                player.getDialogueManager().showPlayerOneLineDialogue("Actually, no.", 591);
                                player.getDialogueManager().finishDialogue();
                                return true;
                            }
                        }
                        break;
                    }
                    case 5: {
                        TravelManager.handleShipRoute(player, ShipRoute.MISCELLANIA_TO_RELLEKKA);
                        player.getDialogueManager().markDialogueInactive();
                        player.getPacketSender().sendGameMessage("You board the longship...");
                    }
                }
                break;
            }
            case 657: {
                switch (player.getDialogueManager().getDialogueStep()) {
                    case 1: {
                        player.getDialogueManager().showNpcOneLineDialogue("Would you like to sail back to Port Sarim?", 591);
                        return true;
                    }
                    case 2: {
                        player.getDialogueManager().showTwoOptions("Yes please.", "No thanks.");
                        return true;
                    }
                    case 3: {
                        switch (optionIndex) {
                            case 1: {
                                TravelManager.handleShipRoute(player, ShipRoute.ENTRANA_TO_PORT_SARIM);
                                player.getDialogueManager().markDialogueInactive();
                                break continueContextDialogueControlSwitch1;
                            }
                            case 2: {
                                player.getDialogueManager().showPlayerOneLineDialogue("No thanks.", 591);
                                player.getDialogueManager().finishDialogue();
                                return true;
                            }
                        }
                        break continueContextDialogueControlSwitch1;
                    }
                    case 4: {
                        player.getDialogueManager().showNpcTwoLineDialogue("How dare you try to take dangerous equipment?", "Come back when you have left it all behind.", 614);
                        player.getDialogueManager().finishDialogue();
                        return true;
                    }
                }
                break;
            }
            case 10088: {
                continueDialogueWithNpcIdControlSwitch6 : switch (player.getDialogueManager().getDialogueStep()) {
                    case 1: {
                        primaryGatheringTool = ItemCombinationHandler.forBrokenToolItemId(player.temporaryActionValue);
                        if (primaryGatheringTool == null) break;
                        player.getDialogueManager().setDialogueNpcId(player.sharedActionValue);
                        text2 = "free";
                        if (primaryGatheringTool.getRepairCostCoins() != 0) {
                            text2 = String.valueOf(primaryGatheringTool.getRepairCostCoins()) + "gp";
                        }
                        text3 = primaryGatheringTool.getSkillId() == 14 ? "pickaxe" : "axe";
                        player.getDialogueManager().showNpcTwoLineDialogue("This " + text3 + " is quite badly damaged, but easy to repair.", "Would you like me to fix it for you for " + text2 + "?", 591);
                        return true;
                    }
                    case 2: {
                        player.getDialogueManager().showTwoOptions("Yes, please.", "No, thank you.");
                        return true;
                    }
                    case 3: {
                        switch (optionIndex) {
                            case 1: {
                                secondaryGatheringTool = ItemCombinationHandler.forBrokenToolItemId(player.temporaryActionValue);
                                if (secondaryGatheringTool == null) break continueDialogueWithNpcIdControlSwitch6;
                                text7 = "axe";
                                if (secondaryGatheringTool.getSkillId() == 14) {
                                    text7 = "pickaxe";
                                }
                                text3 = "free";
                                if (secondaryGatheringTool.getRepairCostCoins() != 0) {
                                    text3 = String.valueOf(secondaryGatheringTool.getRepairCostCoins()) + "gp";
                                }
                                npc5 = new Npc(player.sharedActionValue);
                                if (ItemCombinationHandler.repairBrokenGatheringTool(player, player.temporaryActionValue)) {
                                    player.getPacketSender().sendGameMessage(String.valueOf(npc5.getDefinition().getName()) + " fixes your " + text7 + " for " + text3 + ".");
                                }
                                player.temporaryActionValue = -1;
                                player.sharedActionValue = -1;
                                player.getDialogueManager().finishDialogue();
                                break continueDialogueWithNpcIdControlSwitch6;
                            }
                            case 2: {
                                player.temporaryActionValue = -1;
                                player.sharedActionValue = -1;
                                player.getDialogueManager().finishDialogue();
                            }
                        }
                    }
                }
                break;
            }
            case 10089: {
                switch (player.getDialogueManager().getDialogueStep()) {
                    case 1: {
                        firstItem = player.pendingDialogueItem;
                        if (firstItem == null) break continueContextDialogueControlSwitch1;
                        player.getDialogueManager().setDialogueNpcId(player.sharedActionValue);
                        player.getDialogueManager().showNpcOneLineDialogue("That'll cost you " + GameUtil.formatNumber((long)BarrowsRepairHandler.calculateRepairCost(firstItem)) + " gold coins to fix, are you sure?", 591);
                        return true;
                    }
                    case 2: {
                        player.getDialogueManager().showTwoOptions("Yes I'm sure!", "On second thoughts, no thanks.");
                        return true;
                    }
                    case 3: {
                        switch (optionIndex) {
                            case 1: {
                                player.getDialogueManager().showPlayerOneLineDialogue("Yes I'm sure!", 591);
                                return true;
                            }
                            case 2: {
                                player.getDialogueManager().showPlayerOneLineDialogue("On second thoughts, no thanks.", 591);
                                player.sharedActionValue = -1;
                                player.pendingDialogueItem = null;
                                player.getDialogueManager().finishDialogue();
                                return true;
                            }
                        }
                        break continueContextDialogueControlSwitch1;
                    }
                    case 4: {
                        secondItem = player.pendingDialogueItem;
                        if (secondItem == null) break continueContextDialogueControlSwitch1;
                        player.getDialogueManager().setDialogueNpcId(player.sharedActionValue);
                        if (BarrowsRepairHandler.repairItem(player, secondItem)) {
                            player.getDialogueManager().showNpcOneLineDialogue("There you go, happy doing business with you!", 591);
                        } else {
                            player.getDialogueManager().showNpcOneLineDialogue("You do not have enough gold coins!", 614);
                        }
                        player.sharedActionValue = -1;
                        player.pendingDialogueItem = null;
                        player.getDialogueManager().finishDialogue();
                        return true;
                    }
                }
                break;
            }
            case 10002: {
                switch (player.getDialogueManager().getDialogueStep()) {
                    case 1: {
                        if (player.isMember()) {
                            if (ServerSettings.freeToPlayWorld) {
                                player.packetSender.sendGameMessage("You need to be in members world to access members content.");
                            } else {
                                player.getDialogueManager().showTwoOptions("Burthorpe", "Nowhere");
                            }
                        } else {
                            player.packetSender.sendGameMessage("You need a members account to access members content.");
                        }
                        return true;
                    }
                    case 2: {
                        switch (optionIndex) {
                            case 1: {
                                GameplayHelper.castSelectedItemTeleport(player, TeleportManager.BURTHORPE_TELEPORT_POSITION);
                            }
                        }
                    }
                }
                break;
            }
            case 10003: {
                continueDialogueWithNpcIdControlSwitch7 : switch (player.getDialogueManager().getDialogueStep()) {
                    case 1: {
                        if (player.isMember()) {
                            if (ServerSettings.freeToPlayWorld) {
                                player.packetSender.sendGameMessage("You need to be in members world to access members content.");
                            } else {
                                player.getDialogueManager().showFiveOptions("Edgeville", "Karamja", "Draynor Village", "Al Kharid", "Nowhere");
                            }
                        } else {
                            player.packetSender.sendGameMessage("You need a members account to access members content.");
                        }
                        return true;
                    }
                    case 2: {
                        switch (optionIndex) {
                            case 1: {
                                GameplayHelper.castSelectedItemTeleport(player, TeleportManager.EDGEVILLE_TELEPORT_POSITION);
                                break continueDialogueWithNpcIdControlSwitch7;
                            }
                            case 2: {
                                GameplayHelper.castSelectedItemTeleport(player, TeleportManager.KARAMJA_TELEPORT_POSITION);
                                break continueDialogueWithNpcIdControlSwitch7;
                            }
                            case 3: {
                                GameplayHelper.castSelectedItemTeleport(player, TeleportManager.DRAYNOR_VILLAGE_TELEPORT_POSITION);
                                break continueDialogueWithNpcIdControlSwitch7;
                            }
                            case 4: {
                                GameplayHelper.castSelectedItemTeleport(player, TeleportManager.AL_KHARID_TELEPORT_POSITION);
                            }
                        }
                    }
                }
                break;
            }
            case 10004: {
                continueDialogueWithNpcIdControlSwitch8 : switch (player.getDialogueManager().getDialogueStep()) {
                    case 1: {
                        if (player.isMember()) {
                            if (ServerSettings.freeToPlayWorld) {
                                player.packetSender.sendGameMessage("You need to be in members world to access members content.");
                            } else {
                                player.getDialogueManager().showThreeOptions("Duel Arena", "Castle Wars", "Nowhere");
                            }
                        } else {
                            player.packetSender.sendGameMessage("You need a members account to access members content.");
                        }
                        return true;
                    }
                    case 2: {
                        switch (optionIndex) {
                            case 1: {
                                GameplayHelper.castSelectedItemTeleport(player, TeleportManager.DUEL_ARENA_TELEPORT_POSITION);
                                break continueDialogueWithNpcIdControlSwitch8;
                            }
                            case 2: {
                                GameplayHelper.castSelectedItemTeleport(player, TeleportManager.CASTLE_WARS_TELEPORT_POSITION);
                            }
                        }
                    }
                }
                break;
            }
            case 170: 
            case 1800: 
            case 3809: 
            case 3810: 
            case 3811: 
            case 3812: {
                switch (player.getDialogueManager().getDialogueStep()) {
                    case 1: {
                        if (player.isMember()) {
                            if (ServerSettings.freeToPlayWorld) {
                                player.packetSender.sendGameMessage("You need to be in members world to access members content.");
                            } else {
                                if (player.getQuestState(46) != 1) {
                                    questDefinition1 = QuestDefinition.forId(46);
                                    text4 = questDefinition1.getName();
                                    player.getPacketSender().sendGameMessage("You need to complete " + text4 + " to use the glider.");
                                    player.getDialogueManager().finishDialogue();
                                    return true;
                                }
                                dialogueId = GameUtil.getRegionId(player.getPosition().getX(), player.getPosition().getY());
                                if (dialogueId == 13109) {
                                    return false;
                                }
                                player.getDialogueManager().showNpcOneLineDialogue("Would you like to fly somewhere on the glider?", 591);
                            }
                        } else {
                            player.packetSender.sendGameMessage("You need a members account to access members content.");
                        }
                        return true;
                    }
                    case 2: {
                        player.getDialogueManager().showTwoOptions("Sure.", "No thanks.");
                        return true;
                    }
                    case 3: {
                        switch (optionIndex) {
                            case 1: {
                                player.getPacketSender().showInterface(802);
                                player.getDialogueManager().markDialogueInactive();
                            }
                        }
                    }
                }
                break;
            }
            case 510: {
                continueDialogueWithNpcIdControlSwitch9 : switch (player.getDialogueManager().getDialogueStep()) {
                    case 1: {
                        if (player.getQuestState(85) != 1) {
                            questDefinition2 = QuestDefinition.forId(85);
                            text5 = questDefinition2.getName();
                            player.getDialogueManager().showOneLineStatement("You need to complete " + text5 + " to do this.");
                            player.getDialogueManager().finishDialogue();
                            return true;
                        }
                        value3 = player.getInventoryManager().getItemAmount(995);
                        amount1 = value3 * 0.05;
                        dialogueId = (int)amount1;
                        if (dialogueId < 10) {
                            dialogueId = 10;
                        }
                        if (dialogueId > 200) {
                            dialogueId = 200;
                        }
                        player.getDialogueManager().showNpcTwoLineDialogue("Would you like to travel to Shilo village?", "It will only cost you " + dialogueId + "gp.", 591);
                        return true;
                    }
                    case 2: {
                        player.getDialogueManager().showTwoOptions("Yes please.", "No thanks.");
                        return true;
                    }
                    case 3: {
                        switch (optionIndex) {
                            case 1: {
                                value4 = player.getInventoryManager().getItemAmount(995);
                                amount2 = value4 * 0.05;
                                dialogueId = (int)amount2;
                                if (dialogueId < 10) {
                                    dialogueId = 10;
                                }
                                if (dialogueId > 200) {
                                    dialogueId = 200;
                                }
                                if (player.getInventoryManager().removeItem(new ItemStack(995, dialogueId))) {
                                    TravelManager.handleHajedyCartRoute(player, HajedyCartRoute.BRIMHAVEN_TO_SHILO);
                                    player.getDialogueManager().markDialogueInactive();
                                    break continueDialogueWithNpcIdControlSwitch9;
                                }
                                player.getDialogueManager().showNpcOneLineDialogue("It looks like you don't have enough money!", 614);
                                player.getDialogueManager().finishDialogue();
                                return true;
                            }
                        }
                    }
                }
                break;
            }
            case 511: {
                continueDialogueWithNpcIdControlSwitch10 : switch (player.getDialogueManager().getDialogueStep()) {
                    case 1: {
                        value5 = player.getInventoryManager().getItemAmount(995);
                        amount3 = value5 * 0.05;
                        dialogueId = (int)amount3;
                        if (dialogueId < 10) {
                            dialogueId = 10;
                        }
                        if (dialogueId > 200) {
                            dialogueId = 200;
                        }
                        player.getDialogueManager().showNpcTwoLineDialogue("Would you like to travel to Brimhaven?", "It will cost " + dialogueId + "gp.", 591);
                        return true;
                    }
                    case 2: {
                        player.getDialogueManager().showTwoOptions("Yes please.", "No thanks.");
                        return true;
                    }
                    case 3: {
                        switch (optionIndex) {
                            case 1: {
                                value6 = player.getInventoryManager().getItemAmount(995);
                                amount4 = value6 * 0.05;
                                dialogueId = (int)amount4;
                                if (dialogueId < 10) {
                                    dialogueId = 10;
                                }
                                if (dialogueId > 200) {
                                    dialogueId = 200;
                                }
                                if (player.getInventoryManager().removeItem(new ItemStack(995, dialogueId))) {
                                    TravelManager.handleHajedyCartRoute(player, HajedyCartRoute.SHILO_TO_BRIMHAVEN);
                                    player.getDialogueManager().markDialogueInactive();
                                    break continueDialogueWithNpcIdControlSwitch10;
                                }
                                player.getDialogueManager().showNpcOneLineDialogue("It looks like you don't have enough money!", 614);
                                player.getDialogueManager().finishDialogue();
                                return true;
                            }
                        }
                    }
                }
                break;
            }
            case 0: {
                switch (player.getDialogueManager().getDialogueStep()) {
                    case 1: {
                        player.getDialogueManager().showNpcOneLineDialogue("Hello, what are you doing here?", 591);
                        return true;
                    }
                    case 2: {
                        if (ServerSettings.membershipRequirementMode == 4 && !player.hasMemberFlag()) {
                            player.getDialogueManager().showFourOptions("I'm looking for whoever is in charge of this place.", "I have come to kill everyone in this castle!", "I don't know. I'm lost. Where am I?", "I would like to buy membership.");
                        } else {
                            player.getDialogueManager().showThreeOptions("I'm looking for whoever is in charge of this place.", "I have come to kill everyone in this castle!", "I don't know. I'm lost. Where am I?");
                        }
                        return true;
                    }
                    case 3: {
                        switch (optionIndex) {
                            case 1: {
                                player.getDialogueManager().showNpcTwoLineDialogue("The person in charge here is Duke Horacio.", "You can usually find him upstairs in his castle.", 591);
                                player.getDialogueManager().finishDialogue();
                                return true;
                            }
                            case 2: {
                                player.getDialogueManager().showPlayerOneLineDialogue("I have come to kill everyone in this castle!", 614);
                                return true;
                            }
                            case 3: {
                                player.getDialogueManager().showNpcOneLineDialogue("You are at the Lumbridge Castle.", 591);
                                player.getDialogueManager().finishDialogue();
                                return true;
                            }
                            case 4: {
                                if (ServerSettings.membershipDaysPerPurchase <= 0) {
                                    player.getDialogueManager().showNpcOneLineDialogue("That will cost you " + GameUtil.formatNumber((long)ServerSettings.membershipRequirementValue) + " coins.", 591);
                                } else {
                                    player.getDialogueManager().showNpcTwoLineDialogue("That will cost you " + GameUtil.formatNumber((long)ServerSettings.membershipRequirementValue) + " coins", "for " + ServerSettings.membershipDaysPerPurchase + " days of membership.", 591);
                                }
                                player.getDialogueManager().setNextDialogueStep(10);
                                return true;
                            }
                        }
                        break;
                    }
                    case 4: {
                        npc3 = World.getNpcs()[player.getInteractionTargetIndex()];
                        npc3.getUpdateState().setForcedTextAndMarkUpdated("Help! Help!");
                        npc3.getTargetMovement().moveAwayFromOverlap();
                        break;
                    }
                    case 10: {
                        player.getDialogueManager().showTwoOptions("Here you go.", "Maybe next time.");
                        return true;
                    }
                    case 11: {
                        switch (optionIndex) {
                            case 1: {
                                if (player.getInventoryManager().removeItem(new ItemStack(995, ServerSettings.membershipRequirementValue))) {
                                    if (ServerSettings.membershipDaysPerPurchase <= 0) {
                                        player.setMemberFlag(true);
                                        player.getPacketSender().sendGameMessage("You are now a member!");
                                        player.getDialogueManager().showNpcOneLineDialogue("You have now been granted members access!", 588);
                                    } else {
                                        if (player.isMember()) {
                                            player.getPacketSender().sendGameMessage("Your membership was extended by " + ServerSettings.membershipDaysPerPurchase + " days!");
                                            player.getDialogueManager().showNpcOneLineDialogue("Your membership has been extended by " + ServerSettings.membershipDaysPerPurchase + " days!", 588);
                                        } else {
                                            player.getPacketSender().sendGameMessage("You are now a member!");
                                            player.getDialogueManager().showNpcTwoLineDialogue("You have now been granted members access", "for " + ServerSettings.membershipDaysPerPurchase + " days!", 588);
                                        }
                                        timestamp = System.currentTimeMillis();
                                        if (player.isMember()) {
                                            timestamp = player.membershipExpiresMillis;
                                        }
                                        player.membershipExpiresMillis = GameplayHelper.addDaysToTimestamp(timestamp, ServerSettings.membershipDaysPerPurchase);
                                    }
                                    player.getDialogueManager().finishDialogue();
                                    return true;
                                }
                                player.getDialogueManager().showNpcOneLineDialogue("It looks like you don't have enough money!", 614);
                                player.getDialogueManager().finishDialogue();
                                return true;
                            }
                        }
                    }
                }
                break;
            }
            case 10005: {
                continueDialogueWithNpcIdControlSwitch11 : switch (player.getDialogueManager().getDialogueStep()) {
                    case 1: {
                        player.getDialogueManager().showThreeOptions("Climb up the ladder.", "Climb down the ladder.", "Never mind");
                        return true;
                    }
                    case 2: {
                        switch (optionIndex) {
                            case 1: {
                                AttackStyleDefinition.startDelayedObjectMove(player, new Position(2544, 3741, 0));
                                break continueDialogueWithNpcIdControlSwitch11;
                            }
                            case 2: {
                                AttackStyleDefinition.startDelayedObjectMove(player, new Position(1798, 4407, 3));
                            }
                        }
                    }
                }
                break;
            }
            case 2437: {
                switch (player.getDialogueManager().getDialogueStep()) {
                    case 1: {
                        if (player.getQuestState(40) == 1) {
                            player.getDialogueManager().showNpcOneLineDialogue("Would you like to sail to Waterbirth Island?", 591);
                        } else {
                            player.getDialogueManager().showNpcTwoLineDialogue("Would you like to sail to Waterbirth Island?", "It will only cost you 1000 coins.", 591);
                        }
                        return true;
                    }
                    case 2: {
                        player.getDialogueManager().showTwoOptions("YES", "NO");
                        return true;
                    }
                    case 3: {
                        switch (optionIndex) {
                            case 1: {
                                TravelManager.handleShipRoute(player, ShipRoute.RELLEKKA_TO_WATERBIRTH);
                                player.getDialogueManager().markDialogueInactive();
                            }
                        }
                        break;
                    }
                    case 4: {
                        player.getDialogueManager().showNpcTwoLineDialogue("So do you have the 1000 coins for my service, and are", "you ready to leave now?", 591);
                        player.getDialogueManager().setNextDialogueStep(3);
                    }
                }
                break;
            }
            case 2436: {
                switch (player.getDialogueManager().getDialogueStep()) {
                    case 1: {
                        player.getDialogueManager().showNpcOneLineDialogue("Would you like to sail back to Rellekka?", 591);
                        return true;
                    }
                    case 2: {
                        player.getDialogueManager().showTwoOptions("YES", "NO");
                        return true;
                    }
                    case 3: {
                        switch (optionIndex) {
                            case 1: {
                                TravelManager.handleShipRoute(player, ShipRoute.WATERBIRTH_TO_RELLEKKA);
                                player.getDialogueManager().markDialogueInactive();
                            }
                        }
                    }
                }
                break;
            }
            case 802: {
                switch (player.getDialogueManager().getDialogueStep()) {
                    case 1: {
                        player.getDialogueManager().showNpcTwoLineDialogue("Hello brother, would you like me to bless all", "of your unblessed symbols?", 591);
                        return true;
                    }
                    case 2: {
                        player.getDialogueManager().showTwoOptions("Yes please.", "No thanks.");
                        return true;
                    }
                    case 3: {
                        switch (optionIndex) {
                            case 1: {
                                if (!player.getInventoryManager().containsItem(1716)) {
                                    player.getDialogueManager().showNpcTwoLineDialogue("It look's like you dont have any symbols", "in your inventory. Come back when you do.", 591);
                                } else {
                                    primaryItemList = player.getInventoryManager().getContainer().getItems();
                                    value7 = primaryItemList.length;
                                    value2 = 0;
                                    while (value2 < value7) {
                                        thirdItem = primaryItemList[value2];
                                        if (thirdItem != null && thirdItem.getId() == 1716) {
                                            player.getInventoryManager().replaceItem(new ItemStack(1716), new ItemStack(1718));
                                        }
                                        ++value2;
                                    }
                                    player.getDialogueManager().showNpcOneLineDialogue("There you go, your welcome.", 591);
                                }
                                player.getDialogueManager().finishDialogue();
                                return true;
                            }
                        }
                    }
                }
                break;
            }
            case 10006: {
                continueDialogueWithNpcIdControlSwitch12 : switch (player.getDialogueManager().getDialogueStep()) {
                    case 1: {
                        player.getDialogueManager().showThreeOptions("Climb up the ladder.", "Climb down the ladder.", "Never mind");
                        return true;
                    }
                    case 2: {
                        switch (optionIndex) {
                            case 1: {
                                AttackStyleDefinition.climbOneFloorAtCurrentTile(player, "up");
                                break continueDialogueWithNpcIdControlSwitch12;
                            }
                            case 2: {
                                AttackStyleDefinition.climbOneFloorAtCurrentTile(player, "down");
                            }
                        }
                    }
                }
                break;
            }
            case 10007: {
                continueDialogueWithNpcIdControlSwitch13 : switch (player.getDialogueManager().getDialogueStep()) {
                    case 1: {
                        player.getDialogueManager().showThreeOptions("Go up the stairs.", "Go down the stairs.", "Never mind.");
                        return true;
                    }
                    case 2: {
                        switch (optionIndex) {
                            case 1: {
                                AttackStyleDefinition.climbOffsetLadder(player, "up");
                                break continueDialogueWithNpcIdControlSwitch13;
                            }
                            case 2: {
                                AttackStyleDefinition.climbOffsetLadder(player, "down");
                            }
                        }
                    }
                }
                break;
            }
            case 222: {
                switch (player.getDialogueManager().getDialogueStep()) {
                    case 1: {
                        player.getDialogueManager().showNpcOneLineDialogue("Greetings traveller.", 591);
                        return true;
                    }
                    case 2: {
                        player.getDialogueManager().showTwoOptions("Can you heal me? I'm injured.", "Nevermind.");
                        return true;
                    }
                    case 3: {
                        switch (optionIndex) {
                            case 1: {
                                if (player.getSkillManager().getCurrentLevels()[3] >= player.getSkillManager().getBaseLevel(3)) {
                                    player.getDialogueManager().showNpcOneLineDialogue("You already have full hp.", 591);
                                    player.getDialogueManager().finishDialogue();
                                    return true;
                                }
                                if (player.getInteractionTarget() == null || player.getInteractionTarget().isDead()) {
                                    break continueContextDialogueControlSwitch1;
                                }
                                player.getInteractionTarget().getUpdateState().setAnimation(717);
                                player.getDialogueManager().showNpcOneLineDialogue("Sure, here you go.", 591);
                                player.heal((int)((double)player.getSkillManager().getBaseLevel(3) * 0.3));
                                player.getUpdateState().setGraphic(84);
                                player.getDialogueManager().finishDialogue();
                                return true;
                            }
                        }
                    }
                }
                break;
            }
            case 2257: {
                switch (player.getDialogueManager().getDialogueStep()) {
                    case 1: {
                        if (player.isMember()) {
                            if (ServerSettings.freeToPlayWorld) {
                                player.packetSender.sendGameMessage("You need to be in members world to access members content.");
                            } else if (player.enterTheAbyssMiniquestState == 1) {
                                player.getDialogueManager().showNpcOneLineDialogue("How can I help a fellow Zamorak follower?", 591);
                            } else if (player.getQuestState(14) == 1 && (player.enterTheAbyssMiniquestState == 0 || player.enterTheAbyssMiniquestState == 2)) {
                                player.getDialogueManager().showNpcTwoLineDialogue("Meet me in Varrock's Chaos Temple.", "Here is not the place to talk.", 591);
                                player.enterTheAbyssMiniquestState = 2;
                                player.refreshEnterTheAbyssConfig();
                                player.getDialogueManager().finishDialogue();
                            } else {
                                player.getDialogueManager().showNpcOneLineDialogue("I'm busy right now.", 591);
                                player.getDialogueManager().finishDialogue();
                            }
                        } else {
                            player.packetSender.sendGameMessage("You need a members account to access members content.");
                        }
                        return true;
                    }
                    case 2: {
                        player.getDialogueManager().showThreeOptions("Can you teleport me to the abyss?", "Can I see your shop?", "I'm not a Zamorak follower!");
                        return true;
                    }
                    case 3: {
                        switch (optionIndex) {
                            case 1: {
                                player.getDialogueManager().showPlayerOneLineDialogue("Can you teleport me to the abyss?", 591);
                                return true;
                            }
                            case 2: {
                                player.getDialogueManager().showPlayerOneLineDialogue("Can I see your shop?", 591);
                                player.getDialogueManager().setNextDialogueStep(5);
                                return true;
                            }
                            case 3: {
                                player.getDialogueManager().showPlayerOneLineDialogue("I'm not a Zamorak follower!", 591);
                                player.getDialogueManager().setNextDialogueStep(6);
                                return true;
                            }
                        }
                        break continueContextDialogueControlSwitch1;
                    }
                    case 4: {
                        if (player.getQuestState(14) != 1) {
                            questDefinition3 = QuestDefinition.forId(14);
                            text6 = questDefinition3.getName();
                            player.getDialogueManager().showOneLineStatement("You need to complete " + text6 + " to do this.");
                            player.getDialogueManager().finishDialogue();
                            return true;
                        }
                        npc4 = World.getNpcs()[player.getInteractionTargetIndex()];
                        if (player.enterTheAbyssMiniquestState == 1) {
                            AbyssManager.startAbyssMageTeleport(player, npc4);
                            break continueContextDialogueControlSwitch1;
                        }
                        player.getDialogueManager().showOneLineStatement("You need to complete Enter the Abyss miniquest to do this.");
                        player.getDialogueManager().finishDialogue();
                        break continueContextDialogueControlSwitch1;
                    }
                    case 5: {
                        ShopManager.openShop(player, GameplayHelper.getNpcShopId(dialogueId));
                        player.getDialogueManager().markDialogueInactive();
                        break continueContextDialogueControlSwitch1;
                    }
                    case 6: {
                        player.getDialogueManager().showNpcOneLineDialogue("Then get out of my sight!", 614);
                        player.getDialogueManager().finishDialogue();
                        return true;
                    }
                }
                break;
            }
            case 376: 
            case 377: 
            case 378: {
                switch (player.getDialogueManager().getDialogueStep()) {
                    case 1: {
                        if (!player.botEnabled) {
                            player.getDialogueManager().showNpcTwoLineDialogue("Would you like to sail to Karamja?", "It will only cost you 30gp.", 591);
                            return true;
                        }
                        TravelManager.handleShipRoute(player, ShipRoute.PORT_SARIM_TO_KARAMJA);
                        player.getDialogueManager().markDialogueInactive();
                        break;
                    }
                    case 2: {
                        player.getDialogueManager().showTwoOptions("Yes please.", "No thanks.");
                        return true;
                    }
                    case 3: {
                        switch (optionIndex) {
                            case 1: {
                                TravelManager.handleShipRoute(player, ShipRoute.PORT_SARIM_TO_KARAMJA);
                                player.getDialogueManager().markDialogueInactive();
                            }
                        }
                    }
                }
                break;
            }
            case 3852: {
                continueDialogueWithNpcIdControlSwitch14 : switch (player.getDialogueManager().getDialogueStep()) {
                    case 1: {
                        player.getDialogueManager().showTwoOptions("Access shop", player.skeletonSkinUnlocked != 1 ? "Unlock skeleton skin for 700 donator points." : "Switch to skeleton skin.");
                        return true;
                    }
                    case 2: {
                        switch (optionIndex) {
                            case 1: {
                                ShopManager.openShop(player, GameplayHelper.getNpcShopId(dialogueId));
                                player.getDialogueManager().markDialogueInactive();
                                break continueDialogueWithNpcIdControlSwitch14;
                            }
                            case 2: {
                                if (player.skeletonSkinUnlocked != 1) {
                                    player.getDialogueManager().showTwoOptionsWithTitle("Are you sure you want to spend 700 donator poins?", "Yes", "No");
                                    return true;
                                }
                                player.applyDefaultMaleAppearance();
                                player.getDialogueManager().showNpcOneLineDialogue("Come back anytime.", 591);
                                player.getDialogueManager().finishDialogue();
                                return true;
                            }
                        }
                        break;
                    }
                    case 3: {
                        switch (optionIndex) {
                            case 1: {
                                if (player.getDonatorPoints() >= 700) {
                                    player.applyDefaultMaleAppearance();
                                    player.subtractDonatorPoints(700);
                                    player.skeletonSkinUnlocked = 1;
                                }
                                player.getDialogueManager().showNpcOneLineDialogue("Come back anytime.", 591);
                                player.getDialogueManager().finishDialogue();
                                return true;
                            }
                            case 2: {
                                player.getDialogueManager().showNpcOneLineDialogue("Come back anytime.", 591);
                                player.getDialogueManager().finishDialogue();
                                return true;
                            }
                        }
                    }
                }
                break;
            }
            case 380: {
                switch (player.getDialogueManager().getDialogueStep()) {
                    case 1: {
                        if (player.getPosition().getX() <= 2815) {
                            player.getDialogueManager().showNpcTwoLineDialogue("Would you like to sail back to Ardougne?", "It will cost 30gp.", 591);
                            return true;
                        }
                        if (!player.botEnabled) {
                            player.getDialogueManager().showNpcTwoLineDialogue("Would you like to sail back to Port Sarim?", "It will cost 30gp.", 591);
                            return true;
                        }
                        TravelManager.handleShipRoute(player, ShipRoute.KARAMJA_TO_PORT_SARIM);
                        player.getDialogueManager().markDialogueInactive();
                        break continueContextDialogueControlSwitch1;
                    }
                    case 2: {
                        player.getDialogueManager().showTwoOptions("Yes please.", "No thanks.");
                        return true;
                    }
                    case 3: {
                        switch (optionIndex) {
                            case 1: {
                                if (player.getPosition().getX() > 2815) {
                                    TravelManager.handleShipRoute(player, ShipRoute.KARAMJA_TO_PORT_SARIM);
                                } else {
                                    TravelManager.handleShipRoute(player, ShipRoute.BRIMHAVEN_TO_ARDOUGNE);
                                }
                                player.getDialogueManager().markDialogueInactive();
                            }
                        }
                    }
                }
                break;
            }
            case 381: {
                switch (player.getDialogueManager().getDialogueStep()) {
                    case 1: {
                        player.getDialogueManager().showNpcTwoLineDialogue("Would you like to sail to Brimhaven?", "It will cost 30gp.", 591);
                        return true;
                    }
                    case 2: {
                        player.getDialogueManager().showTwoOptions("Yes please.", "No thanks.");
                        return true;
                    }
                    case 3: {
                        switch (optionIndex) {
                            case 1: {
                                TravelManager.handleShipRoute(player, ShipRoute.ARDOUGNE_TO_BRIMHAVEN);
                                player.getDialogueManager().markDialogueInactive();
                            }
                        }
                    }
                }
                break;
            }
            case 3117: {
                sandwichLadyOffer = new SandwichLadyFoodOffer(true);
                switch (player.getDialogueManager().getDialogueStep()) {
                    case 1: {
                        player.getDialogueManager().showNpcTwoLineDialogue("You look hungry to me. I tell you what - ", "have a " + sandwichLadyOffer.getOfferedFoodNames()[0] + " on me.", 588);
                        player.getSandwichLadyManager().selectedOfferIndex = sandwichLadyOffer.getOfferIndex();
                        return true;
                    }
                    case 2: {
                        player.getSandwichLadyManager().openSelectionInterface(3117);
                        player.getDialogueManager().markDialogueInactive();
                    }
                }
                break;
            }
            case 1595: {
                switch (player.getDialogueManager().getDialogueStep()) {
                    case 1: {
                        if (player.botEnabled) {
                            if (player.getInventoryManager().removeItem(new ItemStack(995, 875))) {
                                player.scheduleDelayedMove(new Position(2713, 9564));
                                player.setBrimhavenOpen(false);
                            } else {
                                player.currentBotTask.startWalkToBank(player);
                            }
                            return true;
                        }
                        if (player.isBrimhavenOpen()) {
                            player.getDialogueManager().showNpcTwoLineDialogue("You have already paid the entrance fee.", "You may enter the dungeon whenever you wish.", 591);
                            player.getDialogueManager().finishDialogue();
                            return true;
                        }
                        player.getDialogueManager().showNpcTwoLineDialogue("Hey, there is a 875 coin fee if you wish", "to enter this dungeon.", 591);
                        return true;
                    }
                    case 2: {
                        player.getDialogueManager().showTwoOptions("Ok, here's 875 coins.", "Nevermind.");
                        return true;
                    }
                    case 3: {
                        switch (optionIndex) {
                            case 1: {
                                if (player.isBrimhavenOpen()) {
                                    player.getDialogueManager().showNpcTwoLineDialogue("You have already paid the entrance fee.", "You may enter the dungeon whenever you wish.", 591);
                                    player.getDialogueManager().finishDialogue();
                                    return true;
                                }
                                player.getDialogueManager().showPlayerOneLineDialogue("Ok, here's 875 coins", 591);
                                return true;
                            }
                        }
                        break continueContextDialogueControlSwitch1;
                    }
                    case 4: {
                        if (player.getInventoryManager().removeItem(new ItemStack(995, 875))) {
                            player.getDialogueManager().showOneLineStatement("You give Saniboch 875 coins");
                            player.setBrimhavenOpen(true);
                        } else {
                            player.getDialogueManager().showPlayerOneLineDialogue("Looks like I don't have enough coins.", 599);
                            player.getDialogueManager().finishDialogue();
                        }
                        return true;
                    }
                    case 5: {
                        player.getDialogueManager().showNpcTwoLineDialogue("Many thanks. You may now pass the door.", "May your death be a glorious one!", 591);
                        player.getDialogueManager().finishDialogue();
                        return true;
                    }
                }
                break;
            }
            case 10008: {
                switch (player.getDialogueManager().getDialogueStep()) {
                    case 1: {
                        player.getDialogueManager().showNpcTwoLineDialogue("Hello " + player.getUsername() + ",", "would you like to see my shop?", 591);
                        return true;
                    }
                    case 2: {
                        player.getDialogueManager().showTwoOptions("Yes please.", "No thanks.");
                        return true;
                    }
                    case 3: {
                        switch (optionIndex) {
                            case 1: {
                                if (player.getInteractionTarget() != null && player.getInteractionTarget().isNpc()) {
                                    GameplayHelper.openNpcShop(player, ((Npc)player.getInteractionTarget()).getNpcId());
                                }
                                player.getDialogueManager().markDialogueInactive();
                            }
                        }
                    }
                }
                break;
            }
            case 513: {
                continueDialogueWithNpcIdControlSwitch15 : switch (player.getDialogueManager().getDialogueStep()) {
                    case 1: {
                        player.getDialogueManager().showPlayerOneLineDialogue("Can I come through this door?", 591);
                        return true;
                    }
                    case 2: {
                        player.getDialogueManager().showNpcOneLineDialogue("Sure, you can enter for a small fee of 20 coins.", 591);
                        return true;
                    }
                    case 3: {
                        player.getDialogueManager().showTwoOptions("Ok, I'll pay.", "No thanks.");
                        return true;
                    }
                    case 4: {
                        switch (optionIndex) {
                            case 1: {
                                if (player.getInventoryManager().containsItemAmount(995, 20)) {
                                    player.getInventoryManager().removeItem(new ItemStack(995, 20));
                                    player.getPacketSender().queueRelativeMovementStep(0, player.getPosition().getY() == 2963 ? 1 : 2, true);
                                    player.getPacketSender().openSingleDoor(2266, 2856, 2963, 0);
                                    break continueDialogueWithNpcIdControlSwitch15;
                                }
                                player.getDialogueManager().showPlayerOneLineDialogue("Sorry, I don't have that many coins.", 599);
                                player.getDialogueManager().finishDialogue();
                                return true;
                            }
                            case 3: {
                                player.getDialogueManager().showPlayerOneLineDialogue("No thanks.", 591);
                                player.getDialogueManager().finishDialogue();
                                return true;
                            }
                        }
                    }
                }
                break;
            }
            case 798: {
                switch (player.getDialogueManager().getDialogueStep()) {
                    case 1: {
                        player.getDialogueManager().showNpcTwoLineDialogue("Thank you for rescuing me! It isn't very comfy", "in this cell!", 591);
                        return true;
                    }
                    case 2: {
                        player.getDialogueManager().showTwoOptions("So... do you know anywhere good to explore?", "Do I get a reward?");
                        return true;
                    }
                    case 3: {
                        switch (optionIndex) {
                            case 1: {
                                player.getDialogueManager().showPlayerOneLineDialogue("So... do you know anywhere good to explore?", 591);
                                return true;
                            }
                            case 2: {
                                player.getDialogueManager().showPlayerOneLineDialogue("Do I get a reward?", 591);
                                player.getDialogueManager().setNextDialogueStep(10);
                                return true;
                            }
                        }
                        break continueContextDialogueControlSwitch1;
                    }
                    case 4: {
                        player.getDialogueManager().showNpcFourLineDialogue("Well, this dungeon was quite good to explore ...until I", "got captured, anyway. I was given a key to an inner", "part of this dungeon by a mysterious cloaked", "stranger!", 591);
                        return true;
                    }
                    case 5: {
                        player.getDialogueManager().showNpcThreeLineDialogue("It's rather tough for me to get that far into the", "dungeon however... I just keep getting", "captured! Would you like to give it a go?", 591);
                        return true;
                    }
                    case 6: {
                        player.getDialogueManager().showTwoOptions("Yes please!", "No, it's too dangerous for me too.");
                        return true;
                    }
                    case 7: {
                        switch (optionIndex) {
                            case 1: {
                                player.getDialogueManager().showPlayerOneLineDialogue("Yes please!", 591);
                                return true;
                            }
                            case 2: {
                                player.getDialogueManager().showPlayerOneLineDialogue("No, it's too dangerous for me too.", 591);
                                player.getDialogueManager().finishDialogue();
                                return true;
                            }
                        }
                        break continueContextDialogueControlSwitch1;
                    }
                    case 8: {
                        player.getDialogueManager().showOneLineStatement("Velrak reaches somewhere mysterious and passes you a key.");
                        return true;
                    }
                    case 9: {
                        if (player.getInventoryManager().getContainer().getFreeSlots() <= 0) {
                            player.getDialogueManager().showNpcTwoLineDialogue("Looks like you don't have enough room", "in your inventory.", 591);
                        } else if (player.ownsItem(1590)) {
                            player.getDialogueManager().showNpcOneLineDialogue("I already gave you the key!", 591);
                        } else {
                            player.getInventoryManager().addItem(new ItemStack(1590));
                            break continueContextDialogueControlSwitch1;
                        }
                        player.getDialogueManager().finishDialogue();
                        return true;
                    }
                    case 10: {
                        player.getDialogueManager().showNpcOneLineDialogue("I don't have anything expensive to give, sorry.", 591);
                        player.getDialogueManager().finishDialogue();
                        return true;
                    }
                }
                break;
            }
            case 10009: {
                conditionMet = true;
                switch (player.getDialogueManager().getDialogueStep()) {
                    case 1: {
                        if (player.clueRequiredItems != null) {
                            if (!player.getInventoryManager().containsItemStack(player.clueRequiredItems[0])) {
                                conditionMet = false;
                            }
                            if (conditionMet) {
                                value8 = 0;
                                while (value8 < player.clueRequiredItems.length) {
                                    player.getInventoryManager().removeItem(player.clueRequiredItems[value8]);
                                    ++value8;
                                }
                                player.clueRequiredItems = null;
                            } else {
                                return false;
                            }
                        }
                        player.cluePuzzleSolved = false;
                        TreasureTrailManager.completeTreasureTrail(player, player.activeClueLevel);
                        player.getDialogueManager().markDialogueInactive();
                        break;
                    }
                    case 2: {
                        if (player.clueRequiredItems != null) {
                            if (!player.getInventoryManager().containsItemStack(player.clueRequiredItems[0])) {
                                conditionMet = false;
                            }
                            if (!conditionMet) {
                                return false;
                            }
                        }
                        player.cluePuzzleSolved = false;
                        TreasureTrailManager.advanceOrCompleteTrail(player, player.activeClueLevel, "You recieve another clue!", true, "Here is your reward");
                        return true;
                    }
                    case 3: {
                        if (player.clueRequiredItems != null) {
                            if (!player.getInventoryManager().containsItemStack(player.clueRequiredItems[0])) {
                                conditionMet = false;
                            }
                            if (!conditionMet) {
                                return false;
                            }
                        }
                        player.getPacketSender().closeInterfaces();
                        player.getDialogueManager().resetDialogueState();
                        player.getPacketSender().sendEnterInputPrompt(207);
                        player.getDialogueManager().markDialogueInactive();
                    }
                }
                break;
            }
            case 804: 
            case 1041: 
            case 2824: {
                switch (player.getDialogueManager().getDialogueStep()) {
                    case 1: {
                        player.getDialogueManager().showNpcOneLineDialogue("Greetings friend. I am a manufacturer of leather.", 591);
                        return true;
                    }
                    case 2: {
                        if (ServerSettings.cacheVersion < 270) {
                            player.getDialogueManager().showThreeOptions("Can I buy some leather then?", "Leather is rather weak stuff.", "Tan my hides.");
                        } else {
                            player.getDialogueManager().showTwoOptions("Can I buy some leather then?", "Leather is rather weak stuff.");
                        }
                        return true;
                    }
                    case 3: {
                        switch (optionIndex) {
                            case 1: {
                                player.getDialogueManager().showPlayerOneLineDialogue("Can I buy some leather then?", 591);
                                return true;
                            }
                            case 2: {
                                player.getDialogueManager().showPlayerOneLineDialogue("Leather is rather weak stuff.", 591);
                                player.getDialogueManager().setNextDialogueStep(5);
                                return true;
                            }
                            case 3: {
                    GameplayHelper.openTanningInterface(player);
                                return true;
                            }
                        }
                        break continueContextDialogueControlSwitch1;
                    }
                    case 4: {
                        player.getDialogueManager().showNpcThreeLineDialogue("I make leather from animal hides. Bring me some", "cowhides and one gold per hide, and I'll tan them", "into soft leather for you.", 588);
                        player.getDialogueManager().finishDialogue();
                        return true;
                    }
                    case 5: {
                        player.getDialogueManager().showNpcFourLineDialogue("Normal leather may be quite weak, but it's", "very cheap - I make it from cowhides for only 1 gp", "per hide - and it's so easy to craft that anyone", "can work with it.", 588);
                        return true;
                    }
                    case 6: {
                        player.getDialogueManager().showNpcFourLineDialogue("Alternatively you could try hard leather. It's", "not so easy to craft, but I only charge 3 gp", "per cowhide to prepare it, and it makes much", "sturdier armour.", 588);
                        return true;
                    }
                    case 7: {
                        player.getDialogueManager().showNpcThreeLineDialogue("I can also tan snake hides and dragonhides,", "suitable for crafting into the highest quality", "armour for rangers.", 588);
                        return true;
                    }
                    case 8: {
                        player.getDialogueManager().showPlayerOneLineDialogue("Thanks, I'll bear it in mind.", 591);
                        player.getDialogueManager().finishDialogue();
                        return true;
                    }
                }
                break;
            }
            case 12345: {
                switch (player.getDialogueManager().getDialogueStep()) {
                    case 1: {
                        player.getDialogueManager().showTwoOptionsWithTitle("Drop your " + player.pendingDialogueItem.getDefinition().getName() + " for " + player.pendingItemDropTarget.getUsername() + " to take?", "Yes", "No");
                        return true;
                    }
                    case 2: {
                        switch (optionIndex) {
                            case 1: {
                                if (player.pendingItemDropTarget != null && !player.pendingItemDropTarget.isDead()) {
                                    GroundItemManager.getInstance().spawn(new GroundItem(new ItemStack(player.pendingDialogueItem.getId(), 1), player.pendingItemDropTarget));
                                    player.getInventoryManager().removeItem(new ItemStack(player.pendingDialogueItem.getId(), 1));
                                    player.getPacketSender().sendGameMessage(String.valueOf(player.pendingItemDropTarget.getUsername()) + " can now take the item from the ground.");
                                    player.pendingItemDropTarget.getPacketSender().sendGameMessage(String.valueOf(player.getUsername()) + " has dropped something for you to take.");
                                    player.pendingDialogueItem = null;
                                    player.pendingItemDropTarget = null;
                                }
                            }
                            case 2: {
                                player.pendingDialogueItem = null;
                                player.pendingItemDropTarget = null;
                            }
                        }
                    }
                }
                break;
            }
            case 10011: {
                player.getDialogueManager().setDialogueNpcId(3637);
                if (player.getDialogueManager().getDialogueStep() == 1 && !NpcDefinition.isDefined(3637)) {
                    player.getDialogueManager().setDialogueStep(2);
                }
                switch (player.getDialogueManager().getDialogueStep()) {
                    case 1: {
                        player.getDialogueManager().showNpcTwoLineDialogue("Hello " + player.getUsername(), "Would you like a free trip somewhere?", -1);
                        return true;
                    }
                    case 2: {
                        player.getDialogueManager().showFiveOptions("The Tree Gnome Village", "The Gnome Stronghold", "Battlefield", "Varrock", "Nowhere");
                        return true;
                    }
                    case 3: {
                        switch (optionIndex) {
                            case 1: {
                                player.getDialogueManager().showTwoLineStatement("You place your hands on the dry tough bark of the spirit tree, and", "feel a surge of energy run through your veins.");
                                return true;
                            }
                            case 2: {
                                player.getDialogueManager().showTwoLineStatement("You place your hands on the dry tough bark of the spirit tree, and", "feel a surge of energy run through your veins.");
                                player.getDialogueManager().setNextDialogueStep(5);
                                return true;
                            }
                            case 3: {
                                player.getDialogueManager().showTwoLineStatement("You place your hands on the dry tough bark of the spirit tree, and", "feel a surge of energy run through your veins.");
                                player.getDialogueManager().setNextDialogueStep(6);
                                return true;
                            }
                            case 4: {
                                player.getDialogueManager().showTwoLineStatement("You place your hands on the dry tough bark of the spirit tree, and", "feel a surge of energy run through your veins.");
                                player.getDialogueManager().setNextDialogueStep(7);
                                return true;
                            }
                        }
                        break;
                    }
                    case 4: {
                        AttackStyleDefinition.startDelayedObjectMove(player, new Position(2542, 3169, 0), () -> com.rs2.model.quest.impl.BiohazardQuest.teleportCrossing(player));
                        break;
                    }
                    case 5: {
                        AttackStyleDefinition.startDelayedObjectMove(player, new Position(2462, 3444, 0), () -> com.rs2.model.quest.impl.BiohazardQuest.teleportCrossing(player));
                        break;
                    }
                    case 6: {
                        AttackStyleDefinition.startDelayedObjectMove(player, new Position(2556, 3259, 0), () -> com.rs2.model.quest.impl.BiohazardQuest.teleportCrossing(player));
                        break;
                    }
                    case 7: {
                        AttackStyleDefinition.startDelayedObjectMove(player, new Position(3179, 3506, 0), () -> com.rs2.model.quest.impl.BiohazardQuest.teleportCrossing(player));
                    }
                }
                break;
            }
            case 3636: {
                switch (player.getDialogueManager().getDialogueStep()) {
                    case 1: {
                        player.getDialogueManager().showNpcThreeLineDialogue("Hello " + player.getUsername() + ", First of all -", "Thank you for giving me life !", "Let me thank you by offering you a trip", -1);
                        return true;
                    }
                    case 2: {
                        player.getDialogueManager().showFiveOptions("The Tree Gnome Village", "The Gnome Stronghold", "Battlefield", "Varrock", "Nowhere");
                        return true;
                    }
                    case 3: {
                        switch (optionIndex) {
                            case 1: {
                                player.getDialogueManager().showTwoLineStatement("You place your hands on the dry tough bark of the spirit tree, and", "feel a surge of energy run through your veins.");
                                return true;
                            }
                            case 2: {
                                player.getDialogueManager().showTwoLineStatement("You place your hands on the dry tough bark of the spirit tree, and", "feel a surge of energy run through your veins.");
                                player.getDialogueManager().setNextDialogueStep(5);
                                return true;
                            }
                            case 3: {
                                player.getDialogueManager().showTwoLineStatement("You place your hands on the dry tough bark of the spirit tree, and", "feel a surge of energy run through your veins.");
                                player.getDialogueManager().setNextDialogueStep(6);
                                return true;
                            }
                            case 4: {
                                player.getDialogueManager().showTwoLineStatement("You place your hands on the dry tough bark of the spirit tree, and", "feel a surge of energy run through your veins.");
                                player.getDialogueManager().setNextDialogueStep(7);
                                return true;
                            }
                        }
                        break;
                    }
                    case 4: {
                        AttackStyleDefinition.startDelayedObjectMove(player, new Position(2542, 3169, 0), () -> com.rs2.model.quest.impl.BiohazardQuest.teleportCrossing(player));
                        break;
                    }
                    case 5: {
                        AttackStyleDefinition.startDelayedObjectMove(player, new Position(2462, 3444, 0), () -> com.rs2.model.quest.impl.BiohazardQuest.teleportCrossing(player));
                        break;
                    }
                    case 6: {
                        AttackStyleDefinition.startDelayedObjectMove(player, new Position(2556, 3259, 0), () -> com.rs2.model.quest.impl.BiohazardQuest.teleportCrossing(player));
                        break;
                    }
                    case 7: {
                        AttackStyleDefinition.startDelayedObjectMove(player, new Position(3179, 3506, 0), () -> com.rs2.model.quest.impl.BiohazardQuest.teleportCrossing(player));
                    }
                }
                break;
            }
            case 2323: 
            case 2324: 
            case 2325: 
            case 2326: 
            case 2327: 
            case 2330: 
            case 2331: 
            case 2332: 
            case 2333: 
            case 2334: 
            case 2335: 
            case 2336: 
            case 2337: 
            case 2338: 
            case 2339: 
            case 2340: 
            case 2341: 
            case 2342: 
            case 2343: 
            case 2344: {
                if (!ServerSettings.farmingEnabled) {
                    player.getPacketSender().sendGameMessage("This skill is currently disabled.");
                    break;
                }
                farmingFarmerDefinition = FarmingFarmerDefinition.forNpcId(player.getInteractionTargetId());
                switch (player.getDialogueManager().getDialogueStep()) {
                    case 1: {
                        if (player.isMember()) {
                            if (ServerSettings.freeToPlayWorld) {
                                player.packetSender.sendGameMessage("You need to be in members world to access members content.");
                            } else {
                                player.getDialogueManager().showNpcThreeLineDialogue("Hey, I am one of the master farmers of this world", "but you can call me " + NpcDefinition.forId(player.getInteractionTargetId()).getName(), "So, what do you need from me?", 591);
                                if (farmingFarmerDefinition.getPatchType() != "tree") {
                                    player.getDialogueManager().setNextDialogueStep(16);
                                }
                            }
                        } else {
                            player.packetSender.sendGameMessage("You need a members account to access members content.");
                        }
                        return true;
                    }
                    case 2: {
                        player.getDialogueManager().showFiveOptions("Would you chop my tree down for me?", "Could you take care of my crops for me?", "Can you give me any farming advice?", "Can you sell me something?", "That's all, thanks");
                        return true;
                    }
                    case 3: {
                        switch (optionIndex) {
                            case 1: {
                                player.getDialogueManager().showPlayerOneLineDialogue("Would you chop my tree down for me?", 591);
                                player.getDialogueManager().setNextDialogueStep(11);
                                return true;
                            }
                            case 2: {
                                player.getDialogueManager().showPlayerOneLineDialogue("Could you take care of my crops for me?", 591);
                                if (farmingFarmerDefinition.getPatchType() != "allotment") {
                                    player.getDialogueManager().setNextDialogueStep(7);
                                }
                                return true;
                            }
                            case 3: {
                                player.getDialogueManager().showPlayerOneLineDialogue("Can you give me any farming advice?", 591);
                                player.getDialogueManager().setNextDialogueStep(8);
                                return true;
                            }
                            case 4: {
                                player.getDialogueManager().showPlayerOneLineDialogue("Can you sell me something?", 591);
                                player.getDialogueManager().setNextDialogueStep(9);
                                return true;
                            }
                        }
                        break;
                    }
                    case 4: {
                        player.getDialogueManager().showNpcOneLineDialogue("I Might, Which one were you thinking of?", 588);
                        return true;
                    }
                    case 5: {
                        player.getDialogueManager().showOptions(farmingFarmerDefinition.getPatchLabels());
                        return true;
                    }
                    case 6: {
                        switch (optionIndex) {
                            case 1: {
                                FarmingFarmerHandler.handlePatchProtectionDialogue(player, 0, "allotment", player.getInteractionTargetId(), 1);
                                return true;
                            }
                            case 2: {
                                FarmingFarmerHandler.handlePatchProtectionDialogue(player, 1, "allotment", player.getInteractionTargetId(), 1);
                                return true;
                            }
                        }
                        break;
                    }
                    case 7: {
                        FarmingFarmerHandler.handlePatchProtectionDialogue(player, -1, farmingFarmerDefinition.getPatchType(), player.getInteractionTargetId(), 1);
                        return true;
                    }
                    case 8: {
                        FarmingFarmerHandler.showRandomFarmingAdvice(player);
                        player.getDialogueManager().markDialogueInactive();
                        return true;
                    }
                    case 9: {
                        player.getDialogueManager().showNpcOneLineDialogue("Sure, I have a bunch of tools for you to use.", 588);
                        return true;
                    }
                    case 10: {
                        ShopManager.openShop(player, GameplayHelper.getNpcShopId(dialogueId));
                        player.getDialogueManager().markDialogueInactive();
                        break;
                    }
                    case 11: {
                        player.getDialogueManager().showNpcOneLineDialogue("Sure, for only 200gp, I will chop it down for you", 591);
                        return true;
                    }
                    case 12: {
                        player.getDialogueManager().showTwoOptions("Sure, here you go", "Sorry, I am a little broke.");
                        return true;
                    }
                    case 13: {
                        switch (optionIndex) {
                            case 1: {
                                player.getDialogueManager().showPlayerOneLineDialogue("Sure, Here you go", 591);
                                return true;
                            }
                            case 2: {
                                player.getDialogueManager().showPlayerOneLineDialogue("Sorry, I am a litle broke", 595);
                                player.getDialogueManager().finishDialogue();
                                return true;
                            }
                        }
                        break;
                    }
                    case 14: {
                        FarmingFarmerHandler.chopTreeForFee(player, player.getInteractionTargetId());
                        player.getDialogueManager().markDialogueInactive();
                        return true;
                    }
                    case 15: {
                        player.getDialogueManager().showNpcOneLineDialogue("Sorry, but you have no tree growing in this patch.", 591);
                        player.getDialogueManager().finishDialogue();
                        return true;
                    }
                    case 16: {
                        player.getDialogueManager().showFourOptions("Could you take care of my crops for me?", "Can you give me any farming advice?", "Can you sell me something?", "That's all, thanks");
                        return true;
                    }
                    case 17: {
                        switch (optionIndex) {
                            case 1: {
                                player.getDialogueManager().showPlayerOneLineDialogue("Could you take care of my crops for me?", 591);
                                if (farmingFarmerDefinition.getPatchType() != "allotment") {
                                    player.getDialogueManager().setNextDialogueStep(7);
                                } else {
                                    player.getDialogueManager().setNextDialogueStep(4);
                                }
                                return true;
                            }
                            case 2: {
                                player.getDialogueManager().showPlayerOneLineDialogue("Can you give me any farming advice?", 591);
                                player.getDialogueManager().setNextDialogueStep(8);
                                return true;
                            }
                            case 3: {
                                player.getDialogueManager().showPlayerOneLineDialogue("Can you sell me something?", 591);
                                player.getDialogueManager().setNextDialogueStep(9);
                                return true;
                            }
                        }
                        break;
                    }
                    case 18: {
                        player.getDialogueManager().showTwoOptions("Sure, here you go", "Sorry, I don't have those at the moment.");
                        return true;
                    }
                    case 19: {
                        switch (optionIndex) {
                            case 1: {
                                player.getDialogueManager().showPlayerOneLineDialogue("Sure, here you go", 591);
                                return true;
                            }
                            case 2: {
                                player.getDialogueManager().showPlayerOneLineDialogue("Sorry, I don't have those at the moment.", 595);
                                player.getDialogueManager().finishDialogue();
                                return true;
                            }
                        }
                        break;
                    }
                    case 20: {
                        FarmingFarmerHandler.handlePatchProtectionDialogue(player, player.getSelectedSkillItemId(), farmingFarmerDefinition.getPatchType(), player.getInteractionTargetId(), 2);
                        player.getDialogueManager().markDialogueInactive();
                    }
                }
                break;
            }
            case 953: {
                switch (player.getDialogueManager().getDialogueStep()) {
                    case 1: {
                        player.getDialogueManager().showNpcOneLineDialogue("Good day, would you like to access your bank account?", 588);
                        return true;
                    }
                    case 2: {
                        player.getDialogueManager().showTwoOptions("Yes.", "No thanks.");
                        return true;
                    }
                    case 3: {
                        switch (optionIndex) {
                            case 1: {
                                BankManager.openBank(player);
                                player.getDialogueManager().markDialogueInactive();
                            }
                        }
                    }
                }
                break;
            }
            case 3021: {
                switch (player.getDialogueManager().getDialogueStep()) {
                    case 1: {
                        if (player.isMember()) {
                            if (ServerSettings.freeToPlayWorld) {
                                player.packetSender.sendGameMessage("You need to be in members world to access members content.");
                            } else {
                                player.getDialogueManager().showNpcThreeLineDialogue("Ah, 'tis a foine day to be sure! Were yez wantin' me to", "store yer tools, or maybe ye might be wantin' yer stuff", "back from me?", 591);
                            }
                        } else {
                            player.packetSender.sendGameMessage("You need a members account to access members content.");
                        }
                        return true;
                    }
                    case 2: {
                        player.getDialogueManager().showThreeOptions("What tools can you store?", "Open your tool store, please.", "Actually, I'm fine.");
                        return true;
                    }
                    case 3: {
                        switch (optionIndex) {
                            case 1: {
                                player.getDialogueManager().showPlayerOneLineDialogue("What tools can you store?", 591);
                                return true;
                            }
                            case 2: {
                                player.getDialogueManager().showPlayerOneLineDialogue("Open your tool store, please.", 591);
                                player.getDialogueManager().setNextDialogueStep(7);
                                return true;
                            }
                            case 3: {
                                player.getDialogueManager().showPlayerOneLineDialogue("Actually, I'm fine.", 591);
                                player.getDialogueManager().finishDialogue();
                                return true;
                            }
                        }
                        break;
                    }
                    case 4: {
                        player.getDialogueManager().showNpcThreeLineDialogue("We'll hold onto yer rake, seed dibber, spade, secateurs,", "waterin' can and trowel - but mind it's not one of them", "fancy trowels only archaeologist use.", 588);
                        return true;
                    }
                    case 5: {
                        player.getDialogueManager().showNpcThreeLineDialogue("We'll take a few buckets off yer hand if you want", "too, and even yer compost and supercompost. There's", "room in our shed for plenty of compost, so bring it on.", 588);
                        return true;
                    }
                    case 6: {
                        player.getDialogueManager().showNpcTwoLineDialogue("Also, if ye hands us yer Farming produce,", "we might be able to change it into banknotes.", 588);
                        player.getDialogueManager().setNextDialogueStep(2);
                        return true;
                    }
                    case 7: {
                        player.getFarmingToolStore().open();
                        player.getDialogueManager().markDialogueInactive();
                    }
                }
                break;
            }
            case 2244: {
                switch (player.getDialogueManager().getDialogueStep()) {
                    case 1: {
                        player.getDialogueManager().showNpcThreeLineDialogue("Greetings adventurer. I am Phileas the Lumbridge", "Guide. I am here to give information and directions to", "new players. Do you require any help?", 591);
                        return true;
                    }
                    case 2: {
                        player.getDialogueManager().showTwoOptions("Yes please.", "No, I can find things myself thank you.");
                        return true;
                    }
                    case 3: {
                        switch (optionIndex) {
                            case 1: {
                                player.getDialogueManager().showPlayerOneLineDialogue("Yes please.", 591);
                                return true;
                            }
                            case 2: {
                                player.getDialogueManager().showNpcOneLineDialogue("If you ever need help, you can talk to me again.", 591);
                                player.getDialogueManager().finishDialogue();
                                return true;
                            }
                        }
                        break continueContextDialogueControlSwitch1;
                    }
                    case 4: {
                        player.getDialogueManager().showNpcFourLineDialogue("First I must warn you to take every precaution to", "keep your password and PIN secure. The", "most important thing to remember is to never give your", "password to, or share your account with, anyone.", 591);
                        return true;
                    }
                    case 5: {
                        player.getDialogueManager().showNpcTwoLineDialogue("I have much more information to impart; what would", "you like to know about?", 591);
                        return true;
                    }
                    case 6: {
                        player.getDialogueManager().showFiveOptions("Where can I find a quest to go on?", "What monsters should I fight?", "Where can I make money?", "How can I heal myself?", "Where can I find a bank?");
                        return true;
                    }
                    case 7: {
                        switch (optionIndex) {
                            case 1: {
                                player.getDialogueManager().showPlayerOneLineDialogue("Where can I find a quest to go on?", 591);
                                player.getDialogueManager().setNextDialogueStep(16);
                                return true;
                            }
                            case 2: {
                                player.getDialogueManager().showPlayerOneLineDialogue("What monsters should I fight?", 591);
                                player.getDialogueManager().setNextDialogueStep(34);
                                return true;
                            }
                            case 3: {
                                player.getDialogueManager().showPlayerOneLineDialogue("Where can I make money?", 591);
                                player.getDialogueManager().setNextDialogueStep(18);
                                return true;
                            }
                            case 4: {
                                player.getDialogueManager().showPlayerOneLineDialogue("How can I heal myself?", 591);
                                player.getDialogueManager().setNextDialogueStep(12);
                                return true;
                            }
                            case 5: {
                                player.getDialogueManager().showPlayerOneLineDialogue("Where can I find a bank?", 591);
                                return true;
                            }
                        }
                        break continueContextDialogueControlSwitch1;
                    }
                    case 8: {
                        player.getDialogueManager().showNpcTwoLineDialogue("The nearest bank is in Draynor Village - go", "west from here.", 591);
                        return true;
                    }
                    case 9: {
                        player.getDialogueManager().showNpcOneLineDialogue("Is there anything else you need help with?", 591);
                        return true;
                    }
                    case 10: {
                        player.getDialogueManager().showTwoOptions("No thank you.", "Yes please.");
                        return true;
                    }
                    case 11: {
                        switch (optionIndex) {
                            case 1: {
                                player.getDialogueManager().showNpcOneLineDialogue("If you ever need help, you can talk to me again.", 591);
                                player.getDialogueManager().finishDialogue();
                                return true;
                            }
                            case 2: {
                                player.getDialogueManager().showPlayerOneLineDialogue("Yes please.", 591);
                                player.getDialogueManager().setNextDialogueStep(6);
                                return true;
                            }
                        }
                        break continueContextDialogueControlSwitch1;
                    }
                    case 12: {
                        player.getDialogueManager().showNpcTwoLineDialogue("You will always heal slowly over time, but people", "normally choose to heal themselves faster by eating food.", 591);
                        return true;
                    }
                    case 13: {
                        player.getDialogueManager().showNpcThreeLineDialogue("There are many different foods in the game such as", "cabbage, fish, meat and many more. Which do you wish", "to hear about?", 591);
                        return true;
                    }
                    case 14: {
                        player.getDialogueManager().showFourOptions("How do I get cabbages?", "How do I fish?", "Where can I find meat?", "Nevermind.");
                        return true;
                    }
                    case 15: {
                        switch (optionIndex) {
                            case 1: {
                                player.getDialogueManager().showPlayerOneLineDialogue("How do I get cabbages?", 591);
                                player.getDialogueManager().setNextDialogueStep(19);
                                return true;
                            }
                            case 2: {
                                player.getDialogueManager().showPlayerOneLineDialogue("How do I fish?", 591);
                                player.getDialogueManager().setNextDialogueStep(23);
                                return true;
                            }
                            case 3: {
                                player.getDialogueManager().showPlayerOneLineDialogue("Where can I find meat?", 591);
                                player.getDialogueManager().setNextDialogueStep(29);
                                return true;
                            }
                            case 4: {
                                player.getDialogueManager().showNpcOneLineDialogue("If you ever need help, you can talk to me again.", 591);
                                player.getDialogueManager().finishDialogue();
                                return true;
                            }
                        }
                        break continueContextDialogueControlSwitch1;
                    }
                    case 16: {
                        player.getDialogueManager().showNpcThreeLineDialogue("Well, I heard my friend the cook was in need of a spot", "of help. He'll be in the kitchen of this here castle. Just", "talk to him and he'll set you off.", 591);
                        player.getDialogueManager().setNextDialogueStep(9);
                        return true;
                    }
                    case 18: {
                        player.getDialogueManager().showNpcThreeLineDialogue("There are many ways to make money in the game. I", "would suggest either killing monsters or doing a trade", "skill such as Smithing or Fishing.", 591);
                        player.getDialogueManager().setNextDialogueStep(31);
                        return true;
                    }
                    case 19: {
                        player.getDialogueManager().showNpcTwoLineDialogue("There is a field a little distance to the north of here", "packed full of cabbages which are there for the picking.", 591);
                        player.getDialogueManager().setNextDialogueStep(20);
                        return true;
                    }
                    case 20: {
                        player.getDialogueManager().showNpcOneLineDialogue("Is there anything else you need help with?", 591);
                        return true;
                    }
                    case 21: {
                        player.getDialogueManager().showThreeOptions("No thank you.", "I'd like to know about other food.", "Yes please.");
                        return true;
                    }
                    case 22: {
                        switch (optionIndex) {
                            case 1: {
                                player.getDialogueManager().showNpcOneLineDialogue("If you ever need help, you can talk to me again.", 591);
                                player.getDialogueManager().finishDialogue();
                                return true;
                            }
                            case 2: {
                                player.getDialogueManager().showFourOptions("How do I get cabbages?", "How do I fish?", "Where can I find meat?", "Nevermind.");
                                player.getDialogueManager().setNextDialogueStep(15);
                                return true;
                            }
                            case 3: {
                                player.getDialogueManager().showPlayerOneLineDialogue("Yes please.", 591);
                                player.getDialogueManager().setNextDialogueStep(6);
                                return true;
                            }
                        }
                        break continueContextDialogueControlSwitch1;
                    }
                    case 23: {
                        player.getDialogueManager().showNpcFourLineDialogue("Fishing spots require different levels and equipment to", "use. To start Fishing, you'll want to talk to the Fishing", "tutor who can be found in the swamps south of here.", "He will also give you a small fishing net if you don't", 591);
                        return true;
                    }
                    case 24: {
                        player.getDialogueManager().showNpcOneLineDialogue("own one already.", 591);
                        return true;
                    }
                    case 25: {
                        player.getDialogueManager().showNpcTwoLineDialogue("You will need some Fishing equipment. At the Fishing", "spots to the south you can only use a small fishing net.", 591);
                        return true;
                    }
                    case 26: {
                        player.getDialogueManager().showPlayerOneLineDialogue("Where could I find one of those?", 591);
                        return true;
                    }
                    case 27: {
                        player.getDialogueManager().showNpcFourLineDialogue("You can get them from a Fishing shop or our Fishing", "Tutor south of here in the swamp. There is a Fishing", "shop in Port Sarim; you can find it on the world map.", "Port Sarim is some way to the west of here, beyond", 591);
                        return true;
                    }
                    case 28: {
                        player.getDialogueManager().showNpcOneLineDialogue("the village of Draynor.", 591);
                        player.getDialogueManager().setNextDialogueStep(20);
                        return true;
                    }
                    case 29: {
                        player.getDialogueManager().showNpcFourLineDialogue("I suggest you go and kill some chickens. The roads on", "either side if this river eventually go past a chicken", "farm. When you have killed some chickens, cook them.", "You cold either make a fire or use a range.", 591);
                        return true;
                    }
                    case 30: {
                        player.getDialogueManager().showNpcThreeLineDialogue("There is a range at the southern end in this town and", "a Cooking tutor in south Lumbridge near Bob's Brilliant", "Axes shop.", 591);
                        player.getDialogueManager().setNextDialogueStep(20);
                        return true;
                    }
                    case 31: {
                        player.getDialogueManager().showNpcFourLineDialogue("Please don't try to get money by begging of other", "players. It will make you unpopular. Nobody likes a", "beggar. It is very irritating to have other players asking", "for your hard-earned cash.", 591);
                        return true;
                    }
                    case 32: {
                        player.getDialogueManager().showThreeOptions("Where can I smith?", "How do I fish?", "What monsters should I fight?");
                        return true;
                    }
                    case 33: {
                        switch (optionIndex) {
                            case 1: {
                                player.getDialogueManager().showPlayerOneLineDialogue("Where can I smith?", 591);
                                player.getDialogueManager().setNextDialogueStep(45);
                                return true;
                            }
                            case 2: {
                                player.getDialogueManager().showPlayerOneLineDialogue("How do I fish?", 591);
                                player.getDialogueManager().setNextDialogueStep(23);
                                return true;
                            }
                            case 3: {
                                player.getDialogueManager().showPlayerOneLineDialogue("What monsters should I fight?", 591);
                                return true;
                            }
                        }
                        break continueContextDialogueControlSwitch1;
                    }
                    case 34: {
                        player.getDialogueManager().showNpcFourLineDialogue("There's lots of beasts to fight in the woods around here,", "especially to the west. There are certainly some goblins", "and spiders that are pests and could do with being", "cleared out. There's also a chicken farm or two up the", 591);
                        return true;
                    }
                    case 35: {
                        player.getDialogueManager().showNpcFourLineDialogue("road for some fairly easy picking. Non-player", "characters usually appear as yellow dots on your mini-", "map, although there are some that you won't be able to", "fight, such as myself. A monster's combat level is shown", 591);
                        return true;
                    }
                    case 36: {
                        player.getDialogueManager().showNpcThreeLineDialogue("next to their 'Attack' option. If that level is coloured", "green it means the monster is weaker than you. If it is", "red, it means that the monster is tougher than you.", 591);
                        return true;
                    }
                    case 37: {
                        player.getDialogueManager().showNpcThreeLineDialogue("Remember, you will do better if you have better", "armour and weapons and it's always worth carrying a", "bit of food to heal yourself.", 591);
                        return true;
                    }
                    case 38: {
                        player.getDialogueManager().showFiveOptions("Where can I get food to heal myself?", "Where can I get better armour and weapons?", "Okay, thanks, I will go and kill things.", "Can I kill other players?", "I'd like to know about something else.");
                        return true;
                    }
                    case 39: {
                        switch (optionIndex) {
                            case 1: {
                                player.getDialogueManager().showPlayerOneLineDialogue("Where can I get food to heal myself?", 591);
                                player.getDialogueManager().setNextDialogueStep(13);
                                return true;
                            }
                            case 2: {
                                player.getDialogueManager().showPlayerOneLineDialogue("Where can I get better armour and weapons?", 591);
                                return true;
                            }
                            case 3: {
                                player.getDialogueManager().showPlayerOneLineDialogue("Okay, thanks, I will go and kill things.", 591);
                                player.getDialogueManager().finishDialogue();
                                return true;
                            }
                            case 4: {
                                player.getDialogueManager().showPlayerOneLineDialogue("Can I kill other players?", 591);
                                player.getDialogueManager().setNextDialogueStep(49);
                                return true;
                            }
                            case 5: {
                                player.getDialogueManager().showPlayerOneLineDialogue("I'd like to know about something else.", 591);
                                player.getDialogueManager().setNextDialogueStep(6);
                                return true;
                            }
                        }
                        break continueContextDialogueControlSwitch1;
                    }
                    case 40: {
                        player.getDialogueManager().showNpcTwoLineDialogue("Well, you can make them, you buy them or talk to the", "combat tutors just west of here.", 591);
                        return true;
                    }
                    case 41: {
                        player.getDialogueManager().showThreeOptions("How do I make a weapon?", "Where can I buy a weapon?", "Could I get a staff like yours?");
                        return true;
                    }
                    case 42: {
                        switch (optionIndex) {
                            case 1: {
                                player.getDialogueManager().showPlayerOneLineDialogue("How do I make a weapon?", 591);
                                return true;
                            }
                            case 2: {
                                player.getDialogueManager().showPlayerOneLineDialogue("Where can I buy a weapon?", 591);
                                player.getDialogueManager().setNextDialogueStep(47);
                                return true;
                            }
                            case 3: {
                                player.getDialogueManager().showPlayerOneLineDialogue("Could I get a staff like yours?", 591);
                                player.getDialogueManager().setNextDialogueStep(48);
                                return true;
                            }
                        }
                        return true;
                    }
                    case 43: {
                        player.getDialogueManager().showNpcThreeLineDialogue("The Smithing skill allows you to make armour and", "weapons. Talk to the boy who smelts metal in the", "furnace, I'm sure he can help", 591);
                        return true;
                    }
                    case 44: {
                        player.getDialogueManager().showPlayerOneLineDialogue("Where can I smith?", 591);
                        return true;
                    }
                    case 45: {
                        player.getDialogueManager().showNpcThreeLineDialogue("You will find a helpful Smithing tutor in the west of", "Varrock - that's north of here. Follow the path across", "the river and head north", 591);
                        return true;
                    }
                    case 46: {
                        player.getDialogueManager().showNpcFourLineDialogue("I suggest you go and mine some ore; find the Mining", "symbol - with the guide symbol near it - in the swamp", "south of here. The Mining guide there can teach you", "how to mine ore.", 591);
                        player.getDialogueManager().setNextDialogueStep(9);
                        return true;
                    }
                    case 47: {
                        player.getDialogueManager().showNpcFourLineDialogue("You can buy a sword from any sword shop, such as", "the one in Varrock - located north of here. Simply", "look for the sword icon on the mini-map, and you'll", "find the store.", 591);
                        player.getDialogueManager().setNextDialogueStep(9);
                        return true;
                    }
                    case 48: {
                        player.getDialogueManager().showNpcThreeLineDialogue("Sorry, my staff is not for sale. However, if your", "interested in buy a staff, visit Zeke's staff", "shop located in Varrock, abit north of here.", 591);
                        player.getDialogueManager().setNextDialogueStep(9);
                        return true;
                    }
                    case 49: {
                        player.getDialogueManager().showNpcFourLineDialogue("To fight other players, you need to visit the duel", "arena, where you can fight players for fun or", "for stakes. However, if you want a more dangerous", "challenge, you can visit the wilderness.", 591);
                        player.getDialogueManager().setNextDialogueStep(9);
                        return true;
                    }
                }
                break;
            }
            case 599: {
                continueDialogueWithNpcIdControlSwitch16 : switch (player.getDialogueManager().getDialogueStep()) {
                    case 1: {
                        player.getDialogueManager().showNpcTwoLineDialogue("Greetings, " + GameUtil.formatDisplayName(player.getUsername()) + ".", "How may I assist you?", 591);
                        return true;
                    }
                    case 2: {
                        player.getDialogueManager().showThreeOptions("Can you change my appearance?", "That's a nice necklace you have, can I buy one?", "I'm fine, thanks.");
                        return true;
                    }
                    case 3: {
                        switch (optionIndex) {
                            case 1: {
                                player.getDialogueManager().showPlayerOneLineDialogue("Can you change my appearance?", 591);
                                return true;
                            }
                            case 2: {
                                player.getDialogueManager().showPlayerOneLineDialogue("That's a nice necklace you have, can I buy one?", 591);
                                player.getDialogueManager().setNextDialogueStep(8);
                                return true;
                            }
                            case 3: {
                                player.getDialogueManager().showPlayerOneLineDialogue("I'm fine, thanks.", 591);
                                player.getDialogueManager().setNextDialogueStep(7);
                                return true;
                            }
                        }
                        break;
                    }
                    case 4: {
                        player.getDialogueManager().showNpcOneLineDialogue("Sure. It will only cost you 1000 coins.", 591);
                        return true;
                    }
                    case 5: {
                        player.getDialogueManager().showTwoOptions("Alright, here you go.", "Nevermind.");
                        return true;
                    }
                    case 6: {
                        switch (optionIndex) {
                            case 1: {
                                if (player.getInventoryManager().removeItem(new ItemStack(995, 1000))) {
                                    player.getPacketSender().showInterface(3559);
                                    player.getDialogueManager().markDialogueInactive();
                                    break continueDialogueWithNpcIdControlSwitch16;
                                }
                                player.getDialogueManager().showPlayerOneLineDialogue("Sorry, looks like I don't have enough coins for that.", 599);
                                player.getDialogueManager().finishDialogue();
                                return true;
                            }
                            case 2: {
                                player.getDialogueManager().showPlayerOneLineDialogue("I'm fine, thanks.", 591);
                                return true;
                            }
                        }
                        break;
                    }
                    case 7: {
                        player.getDialogueManager().showNpcTwoLineDialogue("I'm a busy man.", "Come back when you need something.", 591);
                        player.getDialogueManager().finishDialogue();
                        return true;
                    }
                    case 8: {
                        if (ItemDefinition.isDefined(7803)) {
                            player.getDialogueManager().showNpcOneLineDialogue("Sure, I can sell you a copy for 100 coins.", 591);
                        } else {
                            player.getDialogueManager().showNpcOneLineDialogue("Sorry, I only have this one.", 591);
                            player.getDialogueManager().finishDialogue();
                        }
                        return true;
                    }
                    case 9: {
                        player.getDialogueManager().showTwoOptions("Alright, here you go.", "Nevermind.");
                        return true;
                    }
                    case 10: {
                        switch (optionIndex) {
                            case 1: {
                                if (player.getInventoryManager().removeItem(new ItemStack(995, 100))) {
                                    player.getInventoryManager().addItem(new ItemStack(7803));
                                    player.getDialogueManager().showNpcOneLineDialogue("Thanks, here's your amulet.", 591);
                                } else {
                                    player.getDialogueManager().showPlayerOneLineDialogue("Sorry, looks like I don't have enough coins for that.", 599);
                                }
                                player.getDialogueManager().finishDialogue();
                                return true;
                            }
                        }
                    }
                }
                break;
            }
            case 10010: {
                if (!player.getDuelSession().isActiveDuelStarted()) break;
                if (DuelRule.NO_FORFEIT.isEnabledFor(player)) {
                    player.getPacketSender().sendGameMessage("Forfeiting is disabled in this match!");
                    break;
                }
                switch (player.getDialogueManager().getDialogueStep()) {
                    case 1: {
                        player.getDialogueManager().showOneLineStatement("Are you sure you want to forfeit?");
                        return true;
                    }
                    case 2: {
                        player.getDialogueManager().showTwoOptions("Yes, I want to give up.", "No, I'll keep fighting!");
                        return true;
                    }
                    case 3: {
                        switch (optionIndex) {
                            case 1: {
                                player.getDuelSession().finishDuelLoss(true);
                                return true;
                            }
                        }
                    }
                }
                break;
            }
            case 10012: {
                if (player.getSlayerManager().slayerMasterId <= 0) {
                    player.getDialogueManager().showTwoLineStatement("You have currently no task assigned. Talk to any", "slayer master to recieve one.");
                    player.getDialogueManager().finishDialogue();
                    return true;
                }
                slayerMasterDefinition = SlayerMasterDefinition.forNpcId(player.getSlayerManager().slayerMasterId);
                player.getDialogueManager().setDialogueNpcId(player.getSlayerManager().slayerMasterId);
                switch (player.getDialogueManager().getDialogueStep()) {
                    case 1: {
                        player.getDialogueManager().showNpcOneLineDialogue("Hello there, " + player.getUsername() + ", what can I help you with?", 588);
                        return true;
                    }
                    case 2: {
                        if (player.getSlayerManager().slayerMasterId == 3887 && !player.skulled) {
                            player.getDialogueManager().showOptions(new String[]{"How am I doing so far?", "Who are you?", "Where are you?", "Got any tips for me?", "Activate skull"});
                        } else {
                            player.getDialogueManager().showOptions(new String[]{"How am I doing so far?", "Who are you?", "Where are you?", "Got any tips for me?", "Nothing really."});
                        }
                        return true;
                    }
                    case 3: {
                        switch (optionIndex) {
                            case 1: {
                                player.getDialogueManager().showNpcTwoLineDialogue("You're currently assigned to kill " + player.getSlayerManager().slayerTaskName + "s;", "only " + player.getSlayerManager().taskAmount + " more to go.", 588);
                                player.getDialogueManager().setNextDialogueStep(2);
                                return true;
                            }
                            case 2: {
                                player.getDialogueManager().showNpcOneLineDialogue("My name's " + new Npc(player.getSlayerManager().slayerMasterId).getDefinition().getName() + "; I'm a Slayer Master.", 588);
                                player.getDialogueManager().setNextDialogueStep(2);
                                return true;
                            }
                            case 3: {
                                player.getDialogueManager().showNpcOneLineDialogue("I'm in " + slayerMasterDefinition.getLocationName() + ". Only a fool would forget that.", 588);
                                player.getDialogueManager().setNextDialogueStep(2);
                                return true;
                            }
                            case 4: {
                                primarySlayerGuide = SlayerMonsterGuide.forMonsterName(player.getSlayerManager().slayerTaskName);
                                if (primarySlayerGuide == null) {
                                    player.getDialogueManager().showNpcOneLineDialogue("There is no tips about this npc yet.", 588);
                                } else {
                                    player.getDialogueManager().showNpcDialogue(primarySlayerGuide.getGuideTextLines(), 588);
                                }
                                player.getDialogueManager().setNextDialogueStep(2);
                                return true;
                            }
                            case 5: {
                                if (player.getSlayerManager().slayerMasterId == 3887 && !player.skulled) {
                                    player.addPvpCombatReference(player, 2000);
                                    player.getPacketSender().sendGameMessage("You are now skulled!");
                                }
                                break continueContextDialogueControlSwitch1;
                            }
                        }
                    }
                }
                break;
            }
            case 70: 
            case 1596: 
            case 1597: 
            case 1598: 
            case 1599: 
            case 3887: {
                switch (player.getDialogueManager().getDialogueStep()) {
                    case 1: {
                        if (player.isMember()) {
                            if (ServerSettings.freeToPlayWorld) {
                                player.packetSender.sendGameMessage("You need to be in members world to access members content.");
                            } else {
                                player.getDialogueManager().showNpcOneLineDialogue("'Ello, and what are you after then?", 588);
                            }
                        } else {
                            player.packetSender.sendGameMessage("You need a members account to access members content.");
                        }
                        return true;
                    }
                    case 2: {
                        player.getDialogueManager().showThreeOptions("I need another assignment", "Do you have anything for trade?", "Er...nothing");
                        return true;
                    }
                    case 3: {
                        switch (optionIndex) {
                            case 1: {
                                if (dialogueId != 70) {
                                    if (player.getSlayerManager().slayerMasterId != 0 && !player.getSlayerManager().slayerTaskName.equals("")) {
                                        player.getDialogueManager().showNpcTwoLineDialogue("You're still hunting " + player.getSlayerManager().slayerTaskName + "s; come back", "when you've finished your task.", 588);
                                        player.getDialogueManager().finishDialogue();
                                    } else {
                                        player.getSlayerManager().assignTaskFromMaster(dialogueId);
                                    }
                                } else if (player.getSlayerManager().slayerMasterId == 70 && !player.getSlayerManager().slayerTaskName.equals("")) {
                                    player.getDialogueManager().showNpcTwoLineDialogue("You're still hunting " + player.getSlayerManager().slayerTaskName + "s; come back", "when you've finished your task.", 588);
                                    player.getDialogueManager().finishDialogue();
                                } else {
                                    player.getSlayerManager().assignTaskFromMaster(dialogueId);
                                }
                                return true;
                            }
                            case 2: {
                                player.getDialogueManager().showNpcOneLineDialogue("I have a wide selection of Slayer equipment; take a look!", 588);
                                player.getDialogueManager().setNextDialogueStep(6);
                                return true;
                            }
                        }
                        break;
                    }
                    case 4: {
                        player.getDialogueManager().showTwoOptions("Do you have any tips for me?", "Thanks, I'll be on my way.");
                        return true;
                    }
                    case 5: {
                        switch (optionIndex) {
                            case 1: {
                                secondarySlayerGuide = SlayerMonsterGuide.forMonsterName(player.getSlayerManager().slayerTaskName);
                                if (secondarySlayerGuide == null) {
                                    player.getDialogueManager().showNpcOneLineDialogue("There is no tips about this npc yet.", 588);
                                } else {
                                    player.getDialogueManager().showNpcDialogue(secondarySlayerGuide.getGuideTextLines(), 588);
                                }
                                player.getDialogueManager().finishDialogue();
                                return true;
                            }
                            case 2: {
                                player.getDialogueManager().showPlayerOneLineDialogue("Thanks, I'll be on my way.", 588);
                                player.getDialogueManager().finishDialogue();
                                return true;
                            }
                        }
                        break;
                    }
                    case 6: {
                        ShopManager.openShop(player, GameplayHelper.getNpcShopId(dialogueId));
                        player.getDialogueManager().markDialogueInactive();
                    }
                }
                break;
            }
            case 956: {
                switch (player.getDialogueManager().getDialogueStep()) {
                    case 1: {
                        player.getDialogueManager().showNpcOneLineDialogue("'Ere, matey, 'ave some 'o the good stuff.", 588);
                        return true;
                    }
                    case 2: {
                        player.getInventoryManager().addOrDropItem(new ItemStack(1971));
                        player.getInventoryManager().addOrDropItem(new ItemStack(1917));
                        player.getDialogueManager().showTwoItemMessage("The dwarf gives you beer and a kebab.", "", new ItemStack(1971), new ItemStack(1917));
                        CacheDefinitionIndex.dismissRandomEventNpc(player);
                        player.getDialogueManager().finishDialogue();
                        return true;
                    }
                }
                break;
            }
            case 409: {
                switch (player.getDialogueManager().getDialogueStep()) {
                    case 1: {
                        player.getDialogueManager().showNpcOneLineDialogue("Here you go " + player.getUsername() + ".", 588);
                        return true;
                    }
                    case 2: {
                        player.getInventoryManager().addOrDropItem(new ItemStack(2528));
                        player.getDialogueManager().showItemMessage("The genie gives you a lamp.", new ItemStack(2528));
                        CacheDefinitionIndex.dismissRandomEventNpc(player);
                        player.getDialogueManager().finishDialogue();
                        return true;
                    }
                }
                break;
            }
            case 2476: {
                switch (player.getDialogueManager().getDialogueStep()) {
                    case 1: {
                        player.getDialogueManager().showNpcThreeLineDialogue("Today is your lucky day, sirrah!", "I am  donating to the victims of crime to atone", "for my past actions!", 588);
                        return true;
                    }
                    case 2: {
                        secondaryItemList = new ItemStack[]{new ItemStack(995, 50), new ItemStack(1969), new ItemStack(985), new ItemStack(987), new ItemStack(1623), new ItemStack(1621), new ItemStack(1619), new ItemStack(1617)};
                        fifthItem = secondaryItemList[GameUtil.randomExclusive(8)];
                        player.getInventoryManager().addOrDropItem(fifthItem);
                        player.getDialogueManager().showItemMessage("Rick hands you " + fifthItem.getDefinition().getName().toLowerCase() + ".", fifthItem);
                        CacheDefinitionIndex.dismissRandomEventNpc(player);
                        player.getDialogueManager().finishDialogue();
                        return true;
                    }
                }
                break;
            }
            case 2540: {
                switch (player.getDialogueManager().getDialogueStep()) {
                    case 1: {
                        if (player.getRandomEventRequestedItem() == null) {
                            player.setRandomEventRequestedItem(RandomEventManager.selectJekyllRequestedHerb());
                        }
                        player.getDialogueManager().showNpcTwoLineDialogue("Hello " + player.getUsername() + ",", "would you happen to have a " + player.getRandomEventRequestedItem().getDefinition().getName().toLowerCase() + "?", 588);
                        return true;
                    }
                    case 2: {
                        player.getDialogueManager().showTwoOptions("Yes I do, here you go.", "No I don't, sorry.");
                        return true;
                    }
                    case 3: {
                        switch (optionIndex) {
                            case 1: {
                                if (player.getInventoryManager().removeItem(player.getRandomEventRequestedItem())) {
                                    player.getDialogueManager().showNpcOneLineDialogue("Oh thank you so much, here have this potion.", 588);
                                } else {
                                    player.getDialogueManager().showNpcTwoLineDialogue("Looks like you don't have it. Oh well,", "have this potion I don't need anyways.", 588);
                                    player.setRandomEventRequestedItem(new ItemStack(1));
                                }
                                return true;
                            }
                            case 2: {
                                player.setRandomEventRequestedItem(new ItemStack(1));
                                player.getDialogueManager().showNpcTwoLineDialogue("Oh well, was worth a try.", "Here, have this potion I don't need.", 588);
                                return true;
                            }
                        }
                        break continueContextDialogueControlSwitch1;
                    }
                    case 4: {
                        fourthItem = RandomEventManager.getJekyllPotionRewardForHerb(player.getRandomEventRequestedItem().getId());
                        player.getInventoryManager().addOrDropItem(fourthItem);
                        player.getDialogueManager().showTwoItemMessage("Jekyll hands you " + fourthItem.getDefinition().getName().toLowerCase() + ".", "", new ItemStack(-1, 1), fourthItem);
                        player.setRandomEventRequestedItem(null);
                        CacheDefinitionIndex.dismissRandomEventNpc(player);
                        player.getDialogueManager().finishDialogue();
                        return true;
                    }
                }
                break;
            }
            case 500: {
                switch (player.getDialogueManager().getDialogueStep()) {
                    case 1: {
                        player.getDialogueManager().showNpcThreeLineDialogue("Would you like to enter this village?", "Note that once you enter, you cannot get out", "through this way.", 591);
                        return true;
                    }
                    case 2: {
                        player.getDialogueManager().showTwoOptions("Yes let me in please.", "No thanks.");
                        return true;
                    }
                    case 3: {
                        switch (optionIndex) {
                            case 1: {
                                player.scheduleDelayedMove(new Position(2876, 2952));
                                return true;
                            }
                        }
                    }
                }
            }
        }
        if (player.getDialogueManager().getDialogueStep() > 1) {
            player.getPacketSender().closeInterfaces();
        }
        if (player.getDialogueManager().getDialogueId() >= 0) {
            player.getDialogueManager().resetDialogueState();
        }
        return false;
    }
}
