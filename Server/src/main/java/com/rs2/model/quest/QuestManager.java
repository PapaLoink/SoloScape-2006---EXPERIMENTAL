package com.rs2.model.quest;

import com.rs2.ServerSettings;
import com.rs2.cache.InterfaceDefinition;
import com.rs2.cache.js5.Interfaces;
import com.rs2.net.packet.InterfaceBridge;
import com.rs2.model.Entity;
import com.rs2.model.Position;
import com.rs2.model.player.Player;
import com.rs2.model.quest.QuestDefinition;
import com.rs2.model.quest.QuestEventRegistry;
import com.rs2.model.quest.QuestHook;
import com.rs2.model.quest.QuestScript;

public final class QuestManager {
    private Player player;

    public QuestManager(Player player) {
        this.player = player;
    }

    private static boolean isQuestJournalButtonAvailable(int buttonId) {
        QuestDefinition questDefinition = QuestDefinition.forId(buttonId);
        int journalButtonId = questDefinition.getJournalButtonId();
        // JS5 uses group:child identities; the legacy flat interface table is
        // deliberately not loaded for 443. Testing its count disables every quest.
        if (ServerSettings.cacheVersion == 443) {
            if (buttonId == 0) return true; // Tutorial has no quest-list entry.
            int component = InterfaceBridge.translate(journalButtonId);
            return component != InterfaceBridge.UNMAPPED
                    && Interfaces.forPackedId(component) != null;
        }
        return journalButtonId < InterfaceDefinition.interfaceCount;
    }

    public final boolean handleItemOnItem(int itemId, int value2) {
        QuestHook questHook;
        int index = 0;
        while (index < QuestEventRegistry.eventHookCount) {
            questHook = QuestEventRegistry.getEventHook(index);
            if (questHook.getQuestId() != -1 && questHook.handleItemOnItem(this.player, itemId, value2, this.player.questHookStates[index])) {
                return true;
            }
            ++index;
        }
        index = 1;
        while (index < QuestDefinition.questCount) {
            QuestHook questHook2;
            questHook = QuestDefinition.getQuestScript(index);
            if (!(questHook.getQuestId() == -1 || !QuestManager.isQuestJournalButtonAvailable(questHook.getQuestId()) || QuestDefinition.forId((questHook2 = questHook).getQuestId()).isMembersOnly() && !this.player.isMember() || QuestDefinition.forId((questHook2 = questHook).getQuestId()).isMembersOnly() && ServerSettings.freeToPlayWorld || !questHook.handleItemOnItem(this.player, itemId, value2, this.player.getQuestState(index)))) {
                return true;
            }
            ++index;
        }
        return false;
    }

    public final boolean handleDropItem(int itemId) {
        QuestHook questHook;
        int index = 0;
        while (index < QuestEventRegistry.eventHookCount) {
            questHook = QuestEventRegistry.getEventHook(index);
            if (questHook.getQuestId() != -1 && questHook.handleDropItem(this.player, itemId, this.player.questHookStates[index])) {
                return true;
            }
            ++index;
        }
        index = 1;
        while (index < QuestDefinition.questCount) {
            QuestHook questHook2;
            questHook = QuestDefinition.getQuestScript(index);
            if (!(questHook.getQuestId() == -1 || !QuestManager.isQuestJournalButtonAvailable(questHook.getQuestId()) || QuestDefinition.forId((questHook2 = questHook).getQuestId()).isMembersOnly() && !this.player.isMember() || QuestDefinition.forId((questHook2 = questHook).getQuestId()).isMembersOnly() && ServerSettings.freeToPlayWorld || !questHook.handleDropItem(this.player, itemId, this.player.getQuestState(index)))) {
                return true;
            }
            ++index;
        }
        return false;
    }

    public final boolean handleFirstNpcAction(int npcId) {
        QuestHook questHook;
        int index = 0;
        while (index < QuestEventRegistry.eventHookCount) {
            questHook = QuestEventRegistry.getEventHook(index);
            if (questHook.getQuestId() != -1 && questHook.handleFirstNpcAction(this.player, npcId, this.player.questHookStates[index])) {
                return true;
            }
            ++index;
        }
        index = 1;
        while (index < QuestDefinition.questCount) {
            QuestHook questHook2;
            questHook = QuestDefinition.getQuestScript(index);
            if (!(questHook.getQuestId() == -1 || !QuestManager.isQuestJournalButtonAvailable(questHook.getQuestId()) || QuestDefinition.forId((questHook2 = questHook).getQuestId()).isMembersOnly() && !this.player.isMember() || QuestDefinition.forId((questHook2 = questHook).getQuestId()).isMembersOnly() && ServerSettings.freeToPlayWorld || !questHook.handleFirstNpcAction(this.player, npcId, this.player.getQuestState(index)))) {
                return true;
            }
            ++index;
        }
        return false;
    }

    public final boolean handleInventoryItemFirstOption(int itemId, int value2) {
        QuestHook questHook;
        int index = 0;
        while (index < QuestEventRegistry.eventHookCount) {
            questHook = QuestEventRegistry.getEventHook(index);
            if (questHook.getQuestId() != -1 && questHook.handleInventoryItemFirstOption(this.player, itemId, value2, this.player.questHookStates[index])) {
                return true;
            }
            ++index;
        }
        index = 1;
        while (index < QuestDefinition.questCount) {
            QuestHook questHook2;
            questHook = QuestDefinition.getQuestScript(index);
            if (!(questHook.getQuestId() == -1 || !QuestManager.isQuestJournalButtonAvailable(questHook.getQuestId()) || QuestDefinition.forId((questHook2 = questHook).getQuestId()).isMembersOnly() && !this.player.isMember() || QuestDefinition.forId((questHook2 = questHook).getQuestId()).isMembersOnly() && ServerSettings.freeToPlayWorld || !questHook.handleInventoryItemFirstOption(this.player, itemId, value2, this.player.getQuestState(index)))) {
                return true;
            }
            ++index;
        }
        return false;
    }

    public final boolean handleFirstObjectAction(int objectId, int value2, int value32) {
        QuestHook questHook;
        int index = 0;
        while (index < QuestEventRegistry.eventHookCount) {
            questHook = QuestEventRegistry.getEventHook(index);
            if (questHook.getQuestId() != -1 && questHook.handleFirstObjectAction(this.player, objectId, value2, value32, this.player.questHookStates[index])) {
                return true;
            }
            ++index;
        }
        index = 0;
        while (index < QuestDefinition.questCount) {
            QuestHook questHook2;
            questHook = QuestDefinition.getQuestScript(index);
            if (!(questHook.getQuestId() == -1 || !QuestManager.isQuestJournalButtonAvailable(questHook.getQuestId()) || QuestDefinition.forId((questHook2 = questHook).getQuestId()).isMembersOnly() && !this.player.isMember() || QuestDefinition.forId((questHook2 = questHook).getQuestId()).isMembersOnly() && ServerSettings.freeToPlayWorld || !questHook.handleFirstObjectAction(this.player, objectId, value2, value32, this.player.getQuestState(index)))) {
                return true;
            }
            ++index;
        }
        return false;
    }

    public final boolean handleSecondObjectAction(int objectId, int value2, int value32) {
        QuestHook questHook;
        int index = 0;
        while (index < QuestEventRegistry.eventHookCount) {
            questHook = QuestEventRegistry.getEventHook(index);
            if (questHook.getQuestId() != -1 && questHook.handleSecondObjectAction(this.player, objectId, value2, value32, this.player.questHookStates[index])) {
                return true;
            }
            ++index;
        }
        index = 0;
        while (index < QuestDefinition.questCount) {
            QuestHook questHook2;
            questHook = QuestDefinition.getQuestScript(index);
            if (!(questHook.getQuestId() == -1 || !QuestManager.isQuestJournalButtonAvailable(questHook.getQuestId()) || QuestDefinition.forId((questHook2 = questHook).getQuestId()).isMembersOnly() && !this.player.isMember() || QuestDefinition.forId((questHook2 = questHook).getQuestId()).isMembersOnly() && ServerSettings.freeToPlayWorld || !questHook.handleSecondObjectAction(this.player, objectId, value2, value32, this.player.getQuestState(index)))) {
                return true;
            }
            ++index;
        }
        return false;
    }

    public final boolean handleGroundItemInteraction(int itemId) {
        QuestHook questHook;
        int index = 0;
        while (index < QuestEventRegistry.eventHookCount) {
            questHook = QuestEventRegistry.getEventHook(index);
            if (questHook.getQuestId() != -1 && questHook.handleGroundItemInteraction(this.player, itemId, this.player.questHookStates[index])) {
                return true;
            }
            ++index;
        }
        index = 1;
        while (index < QuestDefinition.questCount) {
            QuestHook questHook2;
            questHook = QuestDefinition.getQuestScript(index);
            if (!(questHook.getQuestId() == -1 || !QuestManager.isQuestJournalButtonAvailable(questHook.getQuestId()) || QuestDefinition.forId((questHook2 = questHook).getQuestId()).isMembersOnly() && !this.player.isMember() || QuestDefinition.forId((questHook2 = questHook).getQuestId()).isMembersOnly() && ServerSettings.freeToPlayWorld || !questHook.handleGroundItemInteraction(this.player, itemId, this.player.getQuestState(index)))) {
                return true;
            }
            ++index;
        }
        return false;
    }

    public final boolean handleItemOnObject(int objectId, int value2) {
        QuestHook questHook;
        int index = 0;
        while (index < QuestEventRegistry.eventHookCount) {
            questHook = QuestEventRegistry.getEventHook(index);
            if (questHook.getQuestId() != -1 && questHook.handleItemOnObject(this.player, objectId, value2, this.player.questHookStates[index])) {
                return true;
            }
            ++index;
        }
        index = 1;
        while (index < QuestDefinition.questCount) {
            QuestHook questHook2;
            questHook = QuestDefinition.getQuestScript(index);
            if (!(questHook.getQuestId() == -1 || !QuestManager.isQuestJournalButtonAvailable(questHook.getQuestId()) || QuestDefinition.forId((questHook2 = questHook).getQuestId()).isMembersOnly() && !this.player.isMember() || QuestDefinition.forId((questHook2 = questHook).getQuestId()).isMembersOnly() && ServerSettings.freeToPlayWorld || !questHook.handleItemOnObject(this.player, objectId, value2, this.player.getQuestState(index)))) {
                return true;
            }
            ++index;
        }
        return false;
    }

    public final boolean handleItemOnNpc(int npcId, int value2) {
        QuestHook questHook;
        int index = 0;
        while (index < QuestEventRegistry.eventHookCount) {
            questHook = QuestEventRegistry.getEventHook(index);
            if (questHook.getQuestId() != -1 && questHook.handleItemOnNpc(this.player, npcId, value2, this.player.questHookStates[index])) {
                return true;
            }
            ++index;
        }
        index = 1;
        while (index < QuestDefinition.questCount) {
            QuestHook questHook2;
            questHook = QuestDefinition.getQuestScript(index);
            if (!(questHook.getQuestId() == -1 || !QuestManager.isQuestJournalButtonAvailable(questHook.getQuestId()) || QuestDefinition.forId((questHook2 = questHook).getQuestId()).isMembersOnly() && !this.player.isMember() || QuestDefinition.forId((questHook2 = questHook).getQuestId()).isMembersOnly() && ServerSettings.freeToPlayWorld || !questHook.handleItemOnNpc(this.player, npcId, value2, this.player.getQuestState(index)))) {
                return true;
            }
            ++index;
        }
        return false;
    }

    public final boolean canAttackNpc(int npcId) {
        QuestHook questHook;
        int index = 0;
        while (index < QuestEventRegistry.eventHookCount) {
            questHook = QuestEventRegistry.getEventHook(index);
            if (questHook.getQuestId() != -1 && !questHook.canAttackNpc(this.player, npcId, this.player.questHookStates[index])) {
                return false;
            }
            ++index;
        }
        index = 1;
        while (index < QuestDefinition.questCount) {
            QuestHook questHook2;
            questHook = QuestDefinition.getQuestScript(index);
            if (!(questHook.getQuestId() == -1 || !QuestManager.isQuestJournalButtonAvailable(questHook.getQuestId()) || QuestDefinition.forId((questHook2 = questHook).getQuestId()).isMembersOnly() && !this.player.isMember() || QuestDefinition.forId((questHook2 = questHook).getQuestId()).isMembersOnly() && ServerSettings.freeToPlayWorld || questHook.canAttackNpc(this.player, npcId, this.player.getQuestState(index)))) {
                return false;
            }
            ++index;
        }
        return true;
    }

    public final boolean handleMovementStep() {
        QuestHook questHook;
        int index = 0;
        while (index < QuestEventRegistry.eventHookCount) {
            questHook = QuestEventRegistry.getEventHook(index);
            if (questHook.getQuestId() != -1 && questHook.handleMovementStep(this.player, this.player.questHookStates[index])) {
                return true;
            }
            ++index;
        }
        index = 1;
        while (index < QuestDefinition.questCount) {
            QuestHook questHook2;
            questHook = QuestDefinition.getQuestScript(index);
            if (!(questHook.getQuestId() == -1 || !QuestManager.isQuestJournalButtonAvailable(questHook.getQuestId()) || QuestDefinition.forId((questHook2 = questHook).getQuestId()).isMembersOnly() && !this.player.isMember() || QuestDefinition.forId((questHook2 = questHook).getQuestId()).isMembersOnly() && ServerSettings.freeToPlayWorld || !questHook.handleMovementStep(this.player, this.player.getQuestState(index)))) {
                return true;
            }
            ++index;
        }
        return false;
    }

    public final boolean handleCombatDeath(Entity entity, Entity entity2) {
        QuestHook questHook;
        int index = 0;
        while (index < QuestEventRegistry.eventHookCount) {
            questHook = QuestEventRegistry.getEventHook(index);
            if (questHook.getQuestId() != -1 && questHook.handleCombatDeath(entity, entity2, this.player.questHookStates[index])) {
                return true;
            }
            ++index;
        }
        index = 1;
        while (index < QuestDefinition.questCount) {
            QuestHook questHook2;
            questHook = QuestDefinition.getQuestScript(index);
            if (!(questHook.getQuestId() == -1 || !QuestManager.isQuestJournalButtonAvailable(questHook.getQuestId()) || QuestDefinition.forId((questHook2 = questHook).getQuestId()).isMembersOnly() && !this.player.isMember() || QuestDefinition.forId((questHook2 = questHook).getQuestId()).isMembersOnly() && ServerSettings.freeToPlayWorld || !questHook.handleCombatDeath(entity, entity2, this.player.getQuestState(index)))) {
                return true;
            }
            ++index;
        }
        return false;
    }

    public final int getQuestDamageOverride(Entity entity, Entity entity2) {
        QuestHook questHook;
        int index = 0;
        while (index < QuestEventRegistry.eventHookCount) {
            questHook = QuestEventRegistry.getEventHook(index);
            if (questHook.getQuestId() != -1 && questHook.getQuestDamageOverride(entity, entity2, this.player.questHookStates[index]) != -1) {
                return questHook.getQuestDamageOverride(entity, entity2, this.player.questHookStates[index]);
            }
            ++index;
        }
        index = 1;
        while (index < QuestDefinition.questCount) {
            QuestHook questHook2;
            questHook = QuestDefinition.getQuestScript(index);
            if (!(questHook.getQuestId() == -1 || !QuestManager.isQuestJournalButtonAvailable(questHook.getQuestId()) || QuestDefinition.forId((questHook2 = questHook).getQuestId()).isMembersOnly() && !this.player.isMember() || QuestDefinition.forId((questHook2 = questHook).getQuestId()).isMembersOnly() && ServerSettings.freeToPlayWorld || questHook.getQuestDamageOverride(entity, entity2, this.player.getQuestState(index)) == -1)) {
                return questHook.getQuestDamageOverride(entity, entity2, this.player.getQuestState(index));
            }
            ++index;
        }
        return -1;
    }

    public final boolean handleNpcKill(int npcId) {
        QuestHook questHook;
        int index = 0;
        while (index < QuestEventRegistry.eventHookCount) {
            questHook = QuestEventRegistry.getEventHook(index);
            if (questHook.getQuestId() != -1 && questHook.handleNpcKill(this.player, npcId, this.player.questHookStates[index])) {
                return true;
            }
            ++index;
        }
        index = 1;
        while (index < QuestDefinition.questCount) {
            QuestHook questHook2;
            questHook = QuestDefinition.getQuestScript(index);
            if (!(questHook.getQuestId() == -1 || !QuestManager.isQuestJournalButtonAvailable(questHook.getQuestId()) || QuestDefinition.forId((questHook2 = questHook).getQuestId()).isMembersOnly() && !this.player.isMember() || QuestDefinition.forId((questHook2 = questHook).getQuestId()).isMembersOnly() && ServerSettings.freeToPlayWorld || !questHook.handleNpcKill(this.player, npcId, this.player.getQuestState(index)))) {
                return true;
            }
            ++index;
        }
        return false;
    }

    public final void refreshQuestJournal() {
        QuestScript questScript = QuestDefinition.getQuestScript(0);
        questScript.refreshQuestJournal(this.player, this.player.getQuestState(0));
    }

    public final boolean handleNpcDeathDrop(int npcId, Player player, Position position) {
        QuestHook questHook;
        int index = 0;
        while (index < QuestEventRegistry.eventHookCount) {
            questHook = QuestEventRegistry.getEventHook(index);
            if (questHook.getQuestId() != -1 && questHook.handleNpcDeathDrop(player, npcId, position, this.player.questHookStates[index])) {
                return true;
            }
            ++index;
        }
        index = 1;
        while (index < QuestDefinition.questCount) {
            QuestHook questHook2;
            questHook = QuestDefinition.getQuestScript(index);
            if (!(questHook.getQuestId() == -1 || !QuestManager.isQuestJournalButtonAvailable(questHook.getQuestId()) || QuestDefinition.forId((questHook2 = questHook).getQuestId()).isMembersOnly() && !this.player.isMember() || QuestDefinition.forId((questHook2 = questHook).getQuestId()).isMembersOnly() && ServerSettings.freeToPlayWorld || !questHook.handleNpcDeathDrop(player, npcId, position, this.player.getQuestState(index)))) {
                return true;
            }
            ++index;
        }
        return false;
    }

    public final boolean handleContextDialogue(int value8, int value22, int value32, int value42, int value52, int value62, int value72) {
        QuestHook questHook;
        value52 = 0;
        while (value52 < QuestEventRegistry.eventHookCount) {
            questHook = QuestEventRegistry.getEventHook(value52);
            if (questHook.getQuestId() != -1 && questHook.handleContextDialogue(value8, this.player, value22, value32, value42, value62, value72, this.player.questHookStates[value52])) {
                return true;
            }
            ++value52;
        }
        value52 = 0;
        while (value52 < QuestDefinition.questCount) {
            QuestHook questHook2;
            questHook = QuestDefinition.getQuestScript(value52);
            if (!(questHook.getQuestId() == -1 || !QuestManager.isQuestJournalButtonAvailable(questHook.getQuestId()) || QuestDefinition.forId((questHook2 = questHook).getQuestId()).isMembersOnly() && !this.player.isMember() || QuestDefinition.forId((questHook2 = questHook).getQuestId()).isMembersOnly() && ServerSettings.freeToPlayWorld || !questHook.handleContextDialogue(value8, this.player, value22, value32, value42, value62, value72, this.player.getQuestState(value52)))) {
                return true;
            }
            ++value52;
        }
        return false;
    }

    public final boolean handleNpcDialogue(int npcId, int value5, int value32, int value42) {
        QuestHook questHook;
        value42 = 0;
        while (value42 < QuestEventRegistry.eventHookCount) {
            questHook = QuestEventRegistry.getEventHook(value42);
            if (questHook.getQuestId() != -1 && questHook.handleNpcDialogue(this.player, npcId, value5, value32, this.player.questHookStates[value42])) {
                return true;
            }
            ++value42;
        }
        value42 = 0;
        while (value42 < QuestDefinition.questCount) {
            Object value2;
            questHook = QuestDefinition.getQuestScript(value42);
            if (!(questHook.getQuestId() == -1 || !QuestManager.isQuestJournalButtonAvailable(questHook.getQuestId()) || QuestDefinition.forId(((QuestHook)(value2 = questHook)).getQuestId()).isMembersOnly() && !this.player.isMember() || QuestDefinition.forId(((QuestHook)(value2 = questHook)).getQuestId()).isMembersOnly() && ServerSettings.freeToPlayWorld || !questHook.handleNpcDialogue(this.player, npcId, value5, value32, this.player.getQuestState(value42)))) {
                if (this.player.getQuestState(questHook.getQuestId()) == 0 && questHook.getQuestId() >= 105 && !this.player.hasMemberFlag()) {
                    value2 = this.player;
                    ((Player)value2).packetSender.closeInterfaces();
                    Player player = this.player;
                    value2 = player;
                    value2 = questHook;
                    player.packetSender.sendGameMessage("You need a donator rank to start this quest: " + QuestDefinition.forId(((QuestHook)value2).getQuestId()).getName());
                    return false;
                }
                return true;
            }
            ++value42;
        }
        return false;
    }

    public final boolean refreshQuestJournalStatuses() {
        int index = 0;
        while (index < QuestDefinition.questCount) {
            QuestScript questScript;
            QuestScript questScript2 = QuestDefinition.getQuestScript(index);
            if (!(questScript2.getQuestId() == -1 || !QuestManager.isQuestJournalButtonAvailable(questScript2.getQuestId()) || QuestDefinition.forId((questScript = questScript2).getQuestId()).isMembersOnly() && !this.player.isMember() || QuestDefinition.forId((questScript = questScript2).getQuestId()).isMembersOnly() && ServerSettings.freeToPlayWorld)) {
                questScript2.refreshQuestJournalStatus(this.player, this.player.getQuestState(index));
            }
            ++index;
        }
        return false;
    }

    public final void refreshQuestPointText() {
        Player player = this.player;
        player.packetSender.sendInterfaceText("QP: " + this.player.getQuestPoints(), 3985);
        player.packetSender.sendQuestListColors();
    }

    /** Quests listed by the stock 443 client that have no entry in the legacy quest table (flat ids 20200+). */
    private static final String[] UNSCRIPTED_QUESTS = {
        "Royal Trouble", "Death to the Dorgeshuun", "Fairytale II - Cure a Queen", "Lunar Diplomacy",
        "The Eyes of Glouphrie", "Darkness of Hallowvale", "Slug Menace", "Elemental Workshop II",
        "My Arm's Big Adventure", "Enlightened Journey", "Eagles' Peak", "Animal Magnetism"};

    private boolean showUnscriptedQuest(int buttonId) {
        int index = buttonId - 20200;
        if (index < 0 || index >= UNSCRIPTED_QUESTS.length) return false;
        Player p = this.player;
        p.packetSender.sendInterfaceText("", 8145);
        for (int line = 8147; line <= 8195; line++) p.packetSender.sendInterfaceText("", line);
        for (int line = 12174; line <= 12223; line++) p.packetSender.sendInterfaceText("", line);
        p.packetSender.sendInterfaceText(UNSCRIPTED_QUESTS[index], 8144);
        p.packetSender.sendInterfaceText("This quest has not been added to the server yet.", 8145);
        p.packetSender.sendInterfaceText("", 8147);
        p.packetSender.sendInterfaceText("It appears in the quest list because the", 8148);
        p.packetSender.sendInterfaceText("game client includes it, but there is no", 8149);
        p.packetSender.sendInterfaceText("quest content for it here yet.", 8150);
        p.packetSender.showInterface(8134);
        return true;
    }

    public final boolean handleButtonClick(int buttonId) {
        boolean enabled;
        if (showUnscriptedQuest(buttonId)) return true;
        handleButtonClickControlExit1: {
            int value;
            int value2;
            if (ServerSettings.cacheVersion > 245) {
                value2 = 1;
                while (value2 < QuestDefinition.questCount) {
                    QuestDefinition questDefinition = QuestDefinition.forId(value2);
                    String name = questDefinition.getName();
                    value = questDefinition.getJournalButtonId();
                    if (buttonId == value) {
                        QuestManager questManager = this;
                        Player player = questManager.player;
                        player.packetSender.sendInterfaceText("", 8145);
                        value = 8147;
                        while (value <= 8195) {
                            player = questManager.player;
                            player.packetSender.sendInterfaceText("", value);
                            ++value;
                        }
                        value = 12174;
                        while (value <= 12223) {
                            player = questManager.player;
                            player.packetSender.sendInterfaceText("", value);
                            ++value;
                        }
                        player = this.player;
                        player.packetSender.sendInterfaceText(name, 8144);
                        this.player.getQuestPoints();
                        value = value2;
                        questManager = this;
                        QuestScript questScript = QuestDefinition.getQuestScript(value);
                        String[] questState = questScript.buildQuestJournal(questManager.player, questManager.player.getQuestState(value));
                        player = questManager.player;
                        player.packetSender.sendInterfaceText(questState[0], 8145);
                        int initialValue = 1;
                        while (initialValue < questState.length) {
                            player = questManager.player;
                            player.packetSender.sendInterfaceText(questState[initialValue], initialValue + 8146);
                            ++initialValue;
                        }
                        player = this.player;
                        player.packetSender.showInterface(8134);
                        return true;
                    }
                    ++value2;
                }
            }
            value = buttonId;
            QuestManager questManager = this;
            value2 = 1;
            while (value2 < QuestDefinition.questCount) {
                QuestScript questScript = QuestDefinition.getQuestScript(value2);
                if (questScript.getQuestId() != -1 && QuestManager.isQuestJournalButtonAvailable(questScript.getQuestId())) {
                    if (!QuestDefinition.forId(questScript.getQuestId()).isMembersOnly() || questManager.player.isMember()) {
                        if (!(QuestDefinition.forId(questScript.getQuestId()).isMembersOnly() && ServerSettings.freeToPlayWorld || !questScript.handleButtonClick(questManager.player, value, questManager.player.getQuestState(value2)))) {
                            enabled = true;
                            break handleButtonClickControlExit1;
                        }
                    }
                }
                ++value2;
            }
            enabled = false;
        }
        return enabled;
    }

}

