package com.rs2.net.packet;

import com.rs2.Server;
import com.rs2.ServerSettings;
import com.rs2.cache.InterfaceDefinition;
import com.rs2.model.GameplayHelper;
import com.rs2.model.Position;
import com.rs2.model.World;
import com.rs2.model.ground.GroundItem;
import com.rs2.model.item.ItemStack;
import com.rs2.model.message.MessageOfTheWeek;
import com.rs2.model.music.MusicTrackDefinition;
import com.rs2.model.objects.DynamicObject;
import com.rs2.model.objects.LoadedWorldObject;
import com.rs2.model.objects.WorldObjectLookup;
import com.rs2.model.objects.functions.DoubleDoorHandler;
import com.rs2.model.player.BankRearrangeMode;
import com.rs2.model.player.Player;
import com.rs2.model.quest.QuestDefinition;
import com.rs2.model.quest.QuestHook;
import com.rs2.model.quest.QuestScript;
import com.rs2.model.skill.farming.FarmingTickTask;
import com.rs2.model.skill.magic.Spellbook;
import com.rs2.model.task.CycleEventHandler;
import com.rs2.net.packet.AgilityMovementCompletionEvent;
import com.rs2.net.packet.ByteOrder;
import com.rs2.net.packet.ByteTransform;
import com.rs2.net.packet.DelayedAnimationEvent;
import com.rs2.net.packet.DelayedUnlockEvent;
import com.rs2.net.packet.PacketBuffer;
import com.rs2.net.packet.PacketWriter;
import com.rs2.net.packet.QueuedPositionUnlockEvent;
import com.rs2.net.packet.RelativePositionUnlockEvent;
import com.rs2.net.packet.YAxisPositionUnlockEvent;
import com.rs2.util.GameUtil;
import com.rs2.util.ChatTextCodec;
import com.rs2.util.ChatCodec;
import java.awt.Color;
import java.util.ArrayList;
import java.util.regex.Pattern;

public final class PacketSender {
    private Player player;

    private static boolean isClientInterfaceIdSupported(int interfaceId) {
        return interfaceId >= 0
                && (interfaceId < InterfaceDefinition.interfaceCount
                || interfaceId >= 18890 && interfaceId <= 19102
                || interfaceId >= 19508 && interfaceId <= 19540
                || interfaceId >= 19600 && interfaceId <= 19640);
    }

    public PacketSender(Player player) {
        int[] integerValues = new int[]{17511, 15819, 15812, 15801, 15791, 15774, 15767};
        this.player = player;
    }

    public final PacketSender sendMinimapState(int state) {
        if (this.player.isBot) {
            return this;
        }
        PacketWriter packetWriter = PacketBuffer.allocateWriter(2);
        packetWriter.writeOpcode(this.player.getOutboundCipher(), ServerSettings.clientBuild == 443 ? 87 : 99);
        packetWriter.writeByte(state);
        this.player.writePacketBuffer(packetWriter.getBuffer());
        return this;
    }

    public final void sendMusicTrack(MusicTrackDefinition musicTrackDefinition) {
        if (ServerSettings.clientBuild == 443) {
            sendRevision443MusicTrack(musicTrackDefinition.getName(),
                    com.rs2.model.music.Music.assetForLegacyTrack(musicTrackDefinition.getTrackId()));
            return;
        }
        this.sendInterfaceText(musicTrackDefinition.getName(), 4439);
        int trackId = musicTrackDefinition.getTrackId();
        if (!this.player.isBot && this.player.currentMusicTrackId != trackId) {
            this.player.currentMusicTrackId = trackId;
            if (trackId != -1) {
                PacketWriter packetWriter = PacketBuffer.allocateWriter(3);
                packetWriter.writeOpcode(this.player.getOutboundCipher(), 74);
                packetWriter.writeShort(trackId, ByteOrder.LITTLE);
                this.player.writePacketBuffer(packetWriter.getBuffer());
            }
        }
    }

    /** Legacy component -> 443 widget; with the classic interface, the classic combat tab's widget. */
    private int translateComponent(int legacyId) {
        return this.translateComponent(legacyId, null);
    }

    private int translateComponent(int legacyId, String payload) {
        if (this.player.classicSidebar) {
            int classic = ClassicCombat.component(legacyId);
            if (classic != -1) return classic;
        }
        return InterfaceBridge.translate(legacyId, payload);
    }

    /** Classic combat tabs show special energy from varp 300 instead of moving bar segments. */
    private int translatePositionComponent(int legacyId, String payload) {
        if (this.player.classicSidebar && ClassicCombat.component(legacyId) != -1) return InterfaceBridge.UNMAPPED;
        return InterfaceBridge.translate(legacyId, payload);
    }

    public final void sendRevision443MusicTrack(String name, int trackId) {
        this.sendRevision443InterfaceText("AUTO", 239 << 16 | 183);
        this.sendInterfaceText(name, 4439);
        this.sendInterfaceTextColor(4439, Color.GREEN);
        if (!this.player.isBot && this.player.currentMusicTrackId != trackId) {
            this.player.currentMusicTrackId = trackId;
            PacketWriter packetWriter = PacketBuffer.allocateWriter(3);
            packetWriter.writeOpcode(this.player.getOutboundCipher(), 205);
            packetWriter.writeShort(trackId, ByteOrder.LITTLE);
            this.player.writePacketBuffer(packetWriter.getBuffer());
        }
    }

    public final void lockPlayerForTicks(int ticks) {
        this.player.setActionLocked(true);
        CycleEventHandler.getInstance().schedule(this.player, new DelayedUnlockEvent(this), ticks);
    }

    public final PacketSender sendSystemUpdateTimer(int systemUpdateTimer) {
        if (this.player.isBot) {
            return this;
        }
        if (ServerSettings.clientBuild == 443) {
            PacketWriter packetWriter = PacketBuffer.allocateWriter(3);
            packetWriter.writeOpcode(this.player.getOutboundCipher(), 117);
            packetWriter.writeShort(systemUpdateTimer, ByteTransform.ADD);
            this.player.writePacketBuffer(packetWriter.getBuffer());
            return this;
        }
        PacketWriter packetWriter = PacketBuffer.allocateWriter(4);
        packetWriter.writeOpcode(this.player.getOutboundCipher(), 114);
        packetWriter.writeShort(systemUpdateTimer, ByteOrder.LITTLE);
        this.player.writePacketBuffer(packetWriter.getBuffer());
        return this;
    }

    public final void refreshSidebarInterfaces() {
        int[] integerValues = new int[14];
        integerValues[0] = 2423;
        integerValues[1] = 3917;
        integerValues[2] = 638;
        integerValues[3] = 3213;
        integerValues[4] = 1644;
        integerValues[5] = 5608;
        integerValues[6] = -1;
        integerValues[7] = -1;
        integerValues[8] = 5065;
        integerValues[9] = 5715;
        integerValues[10] = 2449;
        integerValues[11] = 904;
        integerValues[12] = 147;
        integerValues[13] = 962;
        int[] integerValues2 = integerValues;
        int index = 0;
        while (index < 14) {
            this.setSidebarInterface(index, integerValues2[index]);
            if (index == 6) {
                if (this.player.getSpellbook() == Spellbook.MODERN) {
                    this.setSidebarInterface(index, 1151);
                }
                if (this.player.getSpellbook() == Spellbook.ANCIENT) {
                    this.setSidebarInterface(index, 12855);
                }
                if (this.player.getSpellbook() == Spellbook.NECROMANCY) {
                    this.setSidebarInterface(index, 19104);
                }
            }
            ++index;
        }
        this.player.getEquipmentManager().refreshWeaponInterface();
    }

    /** Colours every quest-list entry: red/yellow/green by progress, black for quests not yet in the game. */
    public final void sendQuestListColors() {
        PacketSender value4 = this;
        Object value3;
        Object value2;
        int value;
        int initialValue = 1;
        while (initialValue < QuestDefinition.questCount) {
            Player player;
            value3 = QuestDefinition.forId(initialValue);
            value2 = QuestDefinition.getQuestScript(initialValue);
            value = ((QuestDefinition)value3).getJournalButtonId();
            if (((QuestHook)value2).getQuestId() == -1 && value4.player.getQuestState(initialValue, true) != 1) {
                player = value4.player;
                player.packetSender.sendInterfaceTextColor(value, new Color(8, 8, 8));
            } else if (value4.player.getQuestState(initialValue, true) == 0) {
                player = value4.player;
                player.packetSender.sendInterfaceTextColor(value, Color.RED);
            } else if (value4.player.getQuestState(initialValue, true) == 1) {
                player = value4.player;
                player.packetSender.sendInterfaceTextColor(value, Color.GREEN);
            } else if (value4.player.getQuestState(initialValue, true) >= 2) {
                player = value4.player;
                player.packetSender.sendInterfaceTextColor(value, Color.YELLOW);
            }
            ++initialValue;
        }
        // Quests the stock client lists but the server has no entry for: show as unimplemented.
        for (int unscripted = 20200; unscripted < 20212; ++unscripted) {
            value4.player.packetSender.sendInterfaceTextColor(unscripted, new Color(8, 8, 8));
        }
    }

    public final boolean updateMissingQuestCompletionStates() {
        boolean enabled;
        updateMissingQuestCompletionStatesControlExit1: {
            int value;
            ArrayList<Integer> arrayList;
            updateMissingQuestCompletionStatesControlExit2: {
                enabled = true;
                if (ServerSettings.completeMissingQuestsMode <= 0) break updateMissingQuestCompletionStatesControlExit1;
                ArrayList<Integer> arrayList2 = new ArrayList<Integer>();
                arrayList = new ArrayList<Integer>();
                value = 1;
                while (value < QuestDefinition.questCount) {
                    QuestScript questScript = QuestDefinition.getQuestScript(value);
                    if (questScript.getQuestId() == -1) {
                        arrayList.add(value);
                    } else {
                        arrayList2.add(value);
                    }
                    ++value;
                }
                value = 0;
                while (value < arrayList2.size()) {
                    int integer = (Integer)arrayList2.get(value);
                    if (this.player.getQuestState(integer) != 1) {
                        enabled = false;
                    }
                    ++value;
                }
                if (ServerSettings.completeMissingQuestsMode != 1 && (ServerSettings.completeMissingQuestsMode != 2 || !enabled)) break updateMissingQuestCompletionStatesControlExit2;
                value = 0;
                while (value < arrayList.size()) {
                    int integer2 = (Integer)arrayList.get(value);
                    this.player.setQuestState(integer2, 1);
                    ++value;
                }
                break updateMissingQuestCompletionStatesControlExit1;
            }
            if (ServerSettings.completeMissingQuestsMode != 2 || enabled) break updateMissingQuestCompletionStatesControlExit1;
            value = 0;
            while (value < arrayList.size()) {
                int integer3 = (Integer)arrayList.get(value);
                this.player.setQuestState(integer3, 0);
                ++value;
            }
        }
        return enabled;
    }

    public final PacketSender sendPostLoginState() {
        PacketSender packetSender;
        int value;
        Object value2;
        Object value3;
        PacketSender packetSender2;
        if (!this.player.getHostAddress().equals("127.0.0.1")) {
            Server.getInstance().backupPending = true;
        }
        this.sendPlayerIndex();
        Object value4 = this;
        if (((PacketSender)value4).player.isBot || ServerSettings.clientBuild == 443) {
            packetSender2 = (PacketSender)value4;
        } else {
            PacketWriter packetWriter = PacketBuffer.allocateWriter(1);
            packetWriter.writeOpcode(((PacketSender)value4).player.getOutboundCipher(), 107);
            ((PacketSender)value4).player.writePacketBuffer(packetWriter.getBuffer());
            packetSender2 = (PacketSender)value4;
        }
        if (ServerSettings.clientBuild != 443) {
            this.sendMapRegion();
        }
        this.sendRunEnergy();
        value4 = this;
        ((PacketSender)value4).updateMissingQuestCompletionStates();
        this.sendQuestListColors();
        value4 = this;
        if (((PacketSender)value4).player.isBot) {
            packetSender = (PacketSender)value4;
        } else if (ServerSettings.clientBuild == 443) {
            PacketWriter packetWriter = PacketBuffer.allocateWriter(4);
            packetWriter.writeOpcode(((PacketSender)value4).player.getOutboundCipher(), 91);
            packetWriter.writeByte(((PacketSender)value4).player.getPublicChatMode());
            packetWriter.writeByte(((PacketSender)value4).player.getPrivateChatMode());
            packetWriter.writeByte(((PacketSender)value4).player.getTradeMode());
            ((PacketSender)value4).player.writePacketBuffer(packetWriter.getBuffer());
            packetSender = (PacketSender)value4;
        } else {
            PacketWriter packetWriter = PacketBuffer.allocateWriter(4);
            packetWriter.writeOpcode(((PacketSender)value4).player.getOutboundCipher(), 206);
            packetWriter.writeByte(((PacketSender)value4).player.getPublicChatMode());
            packetWriter.writeByte(((PacketSender)value4).player.getPrivateChatMode());
            packetWriter.writeByte(((PacketSender)value4).player.getTradeMode());
            ((PacketSender)value4).player.writePacketBuffer(packetWriter.getBuffer());
            packetSender = (PacketSender)value4;
        }
        this.player.getEquipmentManager().refreshCarriedValue();
        this.player.getCompostBinManager().processRotting();
        this.player.getAllotmentPatchManager().processGrowth();
        this.player.getFlowerPatchManager().processGrowth();
        this.player.getHerbPatchManager().processGrowth();
        this.player.getHopsPatchManager().processGrowth();
        this.player.getBushPatchManager().processGrowth();
        this.player.getTreePatchManager().processGrowth();
        this.player.getFruitTreePatchManager().processGrowth();
        this.player.getSpecialTreePatchManager().processGrowth();
        this.player.getSpecialCropPatchManager().processGrowth();
        this.player.finishFarmingLoginCatchUp();
        CycleEventHandler.getInstance().schedule(this.player, new FarmingTickTask(this.player), FarmingTickTask.getInitialTickDelay());
        int index = 0;
        while (index < this.player.getQueuedLoginItemIds().length) {
            this.player.getInventoryManager().addItem(new ItemStack(this.player.getQueuedLoginItemIds()[index], this.player.getQueuedLoginItemAmounts()[index]));
            ++index;
        }
        index = 0;
        while (index < 4) {
            this.player.getTreePatchManager().scheduleStumpRegrowth(index);
            ++index;
        }
        this.player.getSocialManager().initializePrivateMessaging();
        if (this.player.getQuestState(0) == 1) {
            value4 = this;
            if (!((PacketSender)value4).player.isBot && ServerSettings.clientBuild != 443) {
                PacketSender packetSender3;
                int daysBetweenMidnights = GameplayHelper.getDaysBetweenMidnights(((PacketSender)value4).player.lastSavedMillis, System.currentTimeMillis());
                value3 = MessageOfTheWeek.getMessageForIndex(Server.messageOfTheWeekIndex);
                int interfaceId = ((MessageOfTheWeek)value3).getInterfaceId();
                value = ((PacketSender)value4).player.getMembershipDaysRemaining();
                int value5 = daysBetweenMidnights;
                daysBetweenMidnights = PacketSender.packLastLoginAddress(((PacketSender)value4).player.lastLoginHostAddress);
                index = 0;
                boolean enabled = ((PacketSender)value4).player.isMember();
                index = 200;
                PacketSender packetSender4 = (PacketSender)value4;
                if (packetSender4.player.isBot) {
                    packetSender3 = packetSender4;
                } else {
                    PacketWriter packetWriter = PacketBuffer.allocateWriter(18);
                    packetWriter.writeOpcode(packetSender4.player.getOutboundCipher(), 176);
                    packetWriter.writeShort(200);
                    packetWriter.writeShort(0, ByteTransform.ADD);
                    packetWriter.writeByte(enabled ? 1 : 0);
                    packetWriter.writeInt(daysBetweenMidnights, ByteOrder.INVERSE_MIDDLE);
                    packetWriter.writeShort(value5);
                    packetWriter.writeShort(value);
                    packetWriter.writeShort(interfaceId);
                    packetSender4.player.writePacketBuffer(packetWriter.getBuffer());
                    packetSender3 = packetSender4;
                }
                value2 = "\\nYou do not have a Bank PIN.\\nPlease visit a bank if you would like one.";
                if (((PacketSender)value4).player.getBankPinManager().hasPin()) {
                    value2 = "\\nYou have set a Bank PIN.";
                }
                ((PacketSender)value4).sendInterfaceText((String)value2, 15270);
                value = 0;
                if (((MessageOfTheWeek)value3).getInterfaceId() == 5993) {
                    value = 1;
                }
                ((PacketSender)value4).sendInterfaceText(((MessageOfTheWeek)value3).getTitle(), value != 0 ? 6002 : ((MessageOfTheWeek)value3).getInterfaceId() + 4);
                ((PacketSender)value4).sendInterfaceText(((MessageOfTheWeek)value3).getLines()[0], value != 0 ? 15491 : ((MessageOfTheWeek)value3).getInterfaceId() + 2);
                ((PacketSender)value4).sendInterfaceText(((MessageOfTheWeek)value3).getLines()[1], value != 0 ? 15492 : ((MessageOfTheWeek)value3).getInterfaceId() + 3);
            }
            this.sendGameMessage("Welcome to RuneScape.");
            if (this.player.loadedCharacterFromBackup) {
                this.sendGameMessage("Your account file was somehow corrupted and had to be loaded from backup.");
            }
        }
        // Tutorial accounts must also save when their session disconnects.
        this.player.loginInitializationComplete = true;
        return this;
    }

    private static int packLastLoginAddress(String address) {
        int index = 0;
        int index2 = 0;
        String[] stringValues = new String[4];
        String[] stringValues2 = address.split(Pattern.quote("."));
        int length = stringValues2.length;
        int index3 = 0;
        while (index3 < length) {
            String part = stringValues2[index3];
            stringValues[index2] = part;
            ++index2;
            ++index3;
        }
        String[] reorderedParts = (String.valueOf(stringValues[1]) + "." + stringValues[0] + "." + stringValues[3] + "." + stringValues[2]).split(Pattern.quote("."));
        int length2 = reorderedParts.length;
        length = 0;
        while (length < length2) {
            String text = reorderedParts[length];
            index <<= 8;
            index |= Integer.parseInt(text);
            ++length;
        }
        return index;
    }

    public final PacketSender syncPlayerConfigs() {
        this.refreshAutocastConfig();
        this.sendConfig(166, this.player.getBrightness());
        this.sendConfig(168, this.player.getMusicVolume());
        this.sendConfig(169, this.player.getEffectVolume());
        this.sendConfig(170, this.player.getMouseButtons());
        if (ServerSettings.clientBuild == 443) {
            this.sendConfig(872, this.player.configStates[872]);
        }
        this.sendConfig(171, this.player.getPublicChatEffects());
        this.sendConfig(172, this.player.isAutoRetaliate() ? 0 : 1);
        this.sendConfig(173, this.player.getMovementQueue().isRunning() ? 1 : 0);
        this.sendConfig(287, this.player.getSplitPrivateChat());
        this.sendConfig(427, this.player.isAcceptAidEnabled() ? 1 : 0);
        this.sendConfig(115, this.player.isBankWithdrawNoteMode() ? 1 : 0);
        this.sendConfig(304, this.player.getBankRearrangeMode().equals((Object)BankRearrangeMode.SWAP) ? 0 : 1);
        return this;
    }

    public final PacketSender sendEnterInputPrompt(int enterInputPrompt) {
        this.player.setSelectedInterfaceId(enterInputPrompt);
        if (this.player.isBot) {
            return this;
        }
        if (ServerSettings.clientBuild == 443) {
            PacketWriter packetWriter = PacketBuffer.allocateWriter(1);
            packetWriter.writeOpcode(this.player.getOutboundCipher(), 32);
            this.player.writePacketBuffer(packetWriter.getBuffer());
            return this;
        }
        PacketWriter packetWriter = PacketBuffer.allocateWriter(1);
        packetWriter.writeOpcode(this.player.getOutboundCipher(), 27);
        this.player.writePacketBuffer(packetWriter.getBuffer());
        return this;
    }

    public final PacketSender modifySkillLevel(int level, int value5, boolean level2) {
        int skillManager = this.player.getSkillManager().getBaseLevel(level) + value5;
        if (this.player.getPlayerRights() > 1 && ServerSettings.debugModeEnabled) {
            System.out.println("current: " + this.player.getSkillManager().getBaseLevel(level) + " max: " + skillManager);
        }
        if (!level2) {
            int[] skillManager2 = this.player.getSkillManager().getCurrentLevels();
            int value2 = level;
            skillManager2[value2] = skillManager2[value2] + value5;
            if (this.player.getSkillManager().getCurrentLevels()[level] < 0) {
                this.player.getSkillManager().getCurrentLevels()[level] = 0;
            }
            this.player.getSkillManager().refreshSkill(level);
            return this;
        }
        if (value5 < 0) {
            if (this.player.getSkillManager().getCurrentLevels()[level] < skillManager) {
                return this;
            }
            if (this.player.getSkillManager().getCurrentLevels()[level] + value5 < skillManager) {
                this.player.getSkillManager().getCurrentLevels()[level] = skillManager;
            } else {
                int[] skillManager3 = this.player.getSkillManager().getCurrentLevels();
                int value3 = level;
                skillManager3[value3] = skillManager3[value3] + value5;
            }
        } else {
            if (this.player.getSkillManager().getCurrentLevels()[level] > skillManager) {
                return this;
            }
            if (this.player.getSkillManager().getCurrentLevels()[level] + value5 > skillManager) {
                this.player.getSkillManager().getCurrentLevels()[level] = skillManager;
            } else {
                int[] skillManager4 = this.player.getSkillManager().getCurrentLevels();
                int value4 = level;
                skillManager4[value4] = skillManager4[value4] + value5;
            }
        }
        this.player.getSkillManager().refreshSkill(level);
        return this;
    }

    public final int modifySkillLevelReturningRemainder(int level, int value4, boolean level2) {
        int remainder = 0;
        int skillManager = this.player.getSkillManager().getBaseLevel(level);
        int skillManager2 = this.player.getSkillManager().getCurrentLevels()[level];
        if (value4 < 0) {
            if (skillManager2 + value4 < 0) {
                remainder = skillManager2 + value4;
                this.player.getSkillManager().getCurrentLevels()[level] = 0;
            } else {
                int[] skillManager3 = this.player.getSkillManager().getCurrentLevels();
                int value2 = level;
                skillManager3[value2] = skillManager3[value2] + value4;
            }
        } else if (skillManager2 + value4 > skillManager) {
            remainder = value4 - (skillManager - skillManager2);
            this.player.getSkillManager().getCurrentLevels()[level] = skillManager;
        } else {
            int[] skillManager4 = this.player.getSkillManager().getCurrentLevels();
            int value3 = level;
            skillManager4[value3] = skillManager4[value3] + value4;
        }
        this.player.getSkillManager().refreshSkill(level);
        return Math.abs(remainder);
    }

    public final PacketSender queueAgilityMovement(int value7, int value22, boolean enabled3, int value32, int value42, int value52, double value8, boolean enabled22, String text2) {
        if (this.player.isStunned() || this.player.isMovementLocked()) {
            return this;
        }
        if (enabled22) {
            this.player.getMovementQueue().setRunning(false);
        }
        this.player.setActionLocked(true);
        this.player.forcedMovementActive = true;
        if (value32 > 0) {
            this.player.setWalkAnimationOverride(value32);
            this.player.setAppearanceUpdateRequired(true);
        }
        this.player.getMovementQueue().clear();
        this.player.getMovementQueue().addStep(new Position(this.player.getPosition().getX() + value7, this.player.getPosition().getY() + value22));
        this.player.getMovementQueue().removeFirstStep();
        CycleEventHandler.getInstance().schedule(this.player, new AgilityMovementCompletionEvent(this, text2, value8, enabled22, true), value42 > 0 ? value52 - 3 : value52);
        if (value42 > 0) {
            CycleEventHandler.getInstance().schedule(this.player, new DelayedAnimationEvent(this, value42), value52);
        }
        return this;
    }

    public final PacketSender queueTwoStepMovement(Position[] positionValues, int value2, boolean enabled2) {
        if (this.player.isStunned() || this.player.isMovementLocked()) {
            return this;
        }
        this.player.setActionLocked(true);
        this.player.setInteractionTarget(null);
        this.player.getUpdateState().setFaceEntity(65535);
        this.player.forcedMovementActive = true;
        this.player.getMovementQueue().clear();
        Position[] positionArray = positionValues;
        value2 = 0;
        while (value2 < 2) {
            Position position = positionArray[value2];
            this.player.getMovementQueue().addStep(position);
            ++value2;
        }
        this.player.getMovementQueue().removeFirstStep();
        CycleEventHandler.getInstance().schedule(this.player, new QueuedPositionUnlockEvent(this, true), 2);
        return this;
    }

    public final PacketSender queueRelativeMovementStep(int value3, int value22, boolean enabled2) {
        if (this.player.isStunned() || this.player.isMovementLocked()) {
            return this;
        }
        this.player.setActionLocked(true);
        if (enabled2) {
            this.player.forcedMovementActive = true;
        }
        this.player.getMovementQueue().clear();
        this.player.getMovementQueue().addStep(new Position(this.player.getPosition().getX() + value3, this.player.getPosition().getY() + value22));
        this.player.getMovementQueue().removeFirstStep();
        if (enabled2) {
            this.faceAlongForcedStep(value3, value22);
        }
        CycleEventHandler.getInstance().schedule(this.player, new RelativePositionUnlockEvent(this, enabled2), 1);
        return this;
    }

    /**
     * Passing through a door, gate or gap: the client holds a face order (such as the one from clicking the door)
     * until the step ends and then turns back to it. Face a tile beyond the destination instead, so the player
     * keeps looking the way they walked. A face set after this call (e.g. firemaking) still wins.
     */
    private void faceAlongForcedStep(int deltaX, int deltaY) {
        Position p = this.player.getPosition();
        this.player.getUpdateState().setFacePosition(new Position(p.getX() + deltaX + Integer.signum(deltaX),
                p.getY() + deltaY + Integer.signum(deltaY), p.getPlane()));
    }

    public final PacketSender queueYAxisMovementStep(int value4, int value22, int value32, boolean enabled2) {
        if (this.player.isStunned() || this.player.isMovementLocked()) {
            return this;
        }
        this.player.setActionLocked(true);
        this.player.forcedMovementActive = true;
        this.player.getMovementQueue().clear();
        this.player.getMovementQueue().addStep(new Position(this.player.getPosition().getX(), this.player.getPosition().getY() + value22));
        this.player.getMovementQueue().removeFirstStep();
        this.faceAlongForcedStep(0, value22);
        CycleEventHandler.getInstance().schedule(this.player, new YAxisPositionUnlockEvent(this, true), 2);
        return this;
    }

    public final PacketSender queueAbsoluteMovementStep(int value3, int value22) {
        if (this.player.isStunned() || this.player.isMovementLocked()) {
            return this;
        }
        this.player.getMovementQueue().clear();
        this.player.getMovementQueue().addStep(new Position(value3, value22));
        this.player.getMovementQueue().removeFirstStep();
        return this;
    }

    public final PacketSender sendStillGraphicToNearbyPlayers(int graphicId, int value2, int value32, int value42, int value52) {
        Player[] playerArray = World.getPlayers();
        int length = playerArray.length;
        int index = 0;
        while (index < length) {
            Player player = playerArray[index];
            if (player != null && player.getPosition().getPlane() == value42 && GameUtil.isWithinDistance(value2, value32, player.getPosition().getX(), player.getPosition().getY(), 25)) {
                player.packetSender.sendStillGraphic(graphicId, new Position(value2, value32, value42), value52);
            }
            ++index;
        }
        return this;
    }

    public final PacketSender sendSkillUpdate(int skillId, int value2, double skillId2) {
        if (this.player.isBot) {
            return this;
        }
        if (ServerSettings.clientBuild == 443) {
            if (skillId == 21) {
                PacketAudit.legacySkill(21, 22, "level=" + value2 + " xp=" + (long)skillId2);
                // The legacy server stores Construction in internal slot 21.
                // Revision 443 has Hunter at 21 and Construction at 22. Hunter
                // is not implemented server-side yet, so expose its real base
                // state (level 1, 0 XP) before sending Construction.
                PacketWriter hunter = PacketBuffer.allocateWriter(7);
                hunter.writeOpcode(this.player.getOutboundCipher(), 58);
                hunter.writeByte(21);
                hunter.writeByte(1, ByteTransform.ADD);
                hunter.writeInt(0, ByteOrder.INVERSE_MIDDLE);
                this.player.writePacketBuffer(hunter.getBuffer());
                skillId = 22;
            }
            PacketWriter packetWriter = PacketBuffer.allocateWriter(7);
            packetWriter.writeOpcode(this.player.getOutboundCipher(), 58);
            packetWriter.writeByte(skillId);
            packetWriter.writeByte(value2, ByteTransform.ADD);
            packetWriter.writeInt((int)skillId2, ByteOrder.INVERSE_MIDDLE);
            this.player.writePacketBuffer(packetWriter.getBuffer());
            return this;
        }
        PacketWriter packetWriter = PacketBuffer.allocateWriter(7);
        packetWriter.writeOpcode(this.player.getOutboundCipher(), 134);
        packetWriter.writeByte(skillId);
        packetWriter.writeInt((int)skillId2, ByteOrder.MIDDLE);
        packetWriter.writeByte(value2);
        this.player.writePacketBuffer(packetWriter.getBuffer());
        return this;
    }

    public final PacketSender sendInterfaceModelRotation(int interfaceId, int value2, int value32, int value42) {
        if (this.player.isBot) {
            return this;
        }
        if (ServerSettings.clientBuild == 443) {
            interfaceId = this.translateComponent(interfaceId, "zoom=" + value2 + " rotation=" + value32 + " angle=" + value42);
            if (interfaceId == InterfaceBridge.UNMAPPED) return this;
            PacketWriter packetWriter = PacketBuffer.allocateWriter(11);
            packetWriter.writeOpcode(this.player.getOutboundCipher(), 147);
            packetWriter.writeInt(interfaceId, ByteOrder.MIDDLE);
            packetWriter.writeShort(value42, ByteOrder.LITTLE);
            packetWriter.writeShort(value2);
            packetWriter.writeShort(value32, ByteTransform.ADD, ByteOrder.LITTLE);
            this.player.writePacketBuffer(packetWriter.getBuffer());
            return this;
        }
        PacketWriter packetWriter = PacketBuffer.allocateWriter(10);
        packetWriter.writeOpcode(this.player.getOutboundCipher(), 230);
        packetWriter.writeShort(value42, ByteTransform.ADD);
        packetWriter.writeShort(interfaceId);
        packetWriter.writeShort(513);
        packetWriter.writeShort(value32, ByteTransform.ADD, ByteOrder.LITTLE);
        this.player.writePacketBuffer(packetWriter.getBuffer());
        return this;
    }

    public final PacketSender selectMagicSidebarTab(int value2) {
        return selectSidebarTab(6);
    }

    public final PacketSender selectSidebarTab(int tab) {
        if (this.player.isBot) {
            return this;
        }
        if (ServerSettings.clientBuild == 443) {
            PacketWriter packetWriter = PacketBuffer.allocateWriter(2);
            packetWriter.writeOpcode(this.player.getOutboundCipher(), 10);
            packetWriter.writeByte(tab);
            this.player.writePacketBuffer(packetWriter.getBuffer());
            return this;
        }
        PacketWriter packetWriter = PacketBuffer.allocateWriter(2);
        packetWriter.writeOpcode(this.player.getOutboundCipher(), 106);
        packetWriter.writeByte(tab, ByteTransform.NEGATE);
        this.player.writePacketBuffer(packetWriter.getBuffer());
        return this;
    }

    public final PacketSender sendInterfaceOffset(int interfaceId, int value2, int value32) {
        if (this.player.isBot) {
            return this;
        }
        if (ServerSettings.clientBuild == 443) {
            interfaceId = this.translatePositionComponent(interfaceId, "x=" + value2 + " y=" + value32);
            if (interfaceId == InterfaceBridge.UNMAPPED) return this;
            PacketWriter packetWriter = PacketBuffer.allocateWriter(9);
            packetWriter.writeOpcode(this.player.getOutboundCipher(), 232);
            packetWriter.writeShort(value32, ByteTransform.ADD);
            packetWriter.writeShort(value2);
            packetWriter.writeInt(interfaceId, ByteOrder.INVERSE_MIDDLE);
            this.player.writePacketBuffer(packetWriter.getBuffer());
            return this;
        }
        PacketWriter packetWriter = PacketBuffer.allocateWriter(7);
        packetWriter.writeOpcode(this.player.getOutboundCipher(), 70);
        packetWriter.writeShort(interfaceId);
        packetWriter.writeShort(value2, ByteOrder.LITTLE);
        packetWriter.writeShort(value32, ByteOrder.LITTLE);
        this.player.writePacketBuffer(packetWriter.getBuffer());
        return this;
    }

    public final PacketSender sendEntityHintIcon(int value3, int value22) {
        this.player.hintedNpcIndex = value22;
        if (this.player.isBot) {
            return this;
        }
        if (ServerSettings.clientBuild == 443) {
            PacketWriter packetWriter = PacketBuffer.allocateWriter(7);
            packetWriter.writeOpcode(this.player.getOutboundCipher(), 96);
            boolean clearHint = value22 < 0 || value3 == 0;
            packetWriter.writeByte(clearHint ? 0 : value3 == 10 ? 10 : 1);
            if (!clearHint) {
                packetWriter.writeShort(value22);
            }
            for (int i = clearHint ? 1 : 3; i < 6; i++) {
                packetWriter.writeByte(0);
            }
            this.player.writePacketBuffer(packetWriter.getBuffer());
            return this;
        }
        PacketWriter packetWriter = PacketBuffer.allocateWriter(7);
        packetWriter.writeOpcode(this.player.getOutboundCipher(), 254);
        packetWriter.writeByte(value3);
        packetWriter.writeShort(value22);
        packetWriter.writeByte(0);
        packetWriter.writeByte(0);
        packetWriter.writeByte(0);
        this.player.writePacketBuffer(packetWriter.getBuffer());
        return this;
    }

    public final PacketSender sendPositionHintIcon(int value5, int value22, int value32, int value42) {
        if (this.player.isBot) {
            return this;
        }
        if (ServerSettings.clientBuild == 443) {
            PacketWriter packetWriter = PacketBuffer.allocateWriter(7);
            packetWriter.writeOpcode(this.player.getOutboundCipher(), 96);
            packetWriter.writeByte(value42);
            packetWriter.writeShort(value5);
            packetWriter.writeShort(value22);
            packetWriter.writeByte(value32);
            this.player.writePacketBuffer(packetWriter.getBuffer());
            return this;
        }
        PacketWriter packetWriter = PacketBuffer.allocateWriter(7);
        packetWriter.writeOpcode(this.player.getOutboundCipher(), 254);
        packetWriter.writeByte(value42);
        packetWriter.writeShort(value5);
        packetWriter.writeShort(value22);
        packetWriter.writeByte(value32);
        this.player.writePacketBuffer(packetWriter.getBuffer());
        return this;
    }

    public final PacketSender sendInterfaceSlotItem(ItemStack itemStack, int interfaceId, int value2, int value32) {
        if (this.player.isBot) {
            return this;
        }
        if (ServerSettings.clientBuild == 443) {
            return this.sendRevision443InterfaceSlotItem(value2, interfaceId,
                    itemStack, value32);
        }
        PacketWriter packetWriter = PacketBuffer.allocateWriter(32);
        packetWriter.startVariableShortPacket(this.player.getOutboundCipher(), 34);
        packetWriter.writeShort(value2);
        packetWriter.writeByte(interfaceId);
        if (itemStack == null) {
            packetWriter.writeShort(0);
            packetWriter.writeByte(0);
        } else {
            packetWriter.writeShort(itemStack.getId() + 1);
            packetWriter.writeByte(value32);
        }
        packetWriter.finishVariableShortPacket();
        this.player.writePacketBuffer(packetWriter.getBuffer());
        return this;
    }

    public final PacketSender sendInterfaceSlotItem(int interfaceId, int value2, ItemStack itemStack) {
        if (this.player.isBot) {
            return this;
        }
        if (ServerSettings.clientBuild == 443) {
            return this.sendRevision443InterfaceSlotItem(value2, interfaceId,
                    itemStack == null || itemStack.getId() == 0 ? null : itemStack,
                    itemStack == null ? 0 : itemStack.getAmount());
        }
        PacketWriter packetWriter = PacketBuffer.allocateWriter(32);
        packetWriter.startVariableShortPacket(this.player.getOutboundCipher(), 34);
        packetWriter.writeShort(value2);
        packetWriter.writeByte(interfaceId);
        if (itemStack.getId() == 0) {
            packetWriter.writeShort(0);
            packetWriter.writeByte(0);
        } else {
            packetWriter.writeShort(itemStack.getId() + 1);
            if (itemStack.getAmount() > 254) {
                packetWriter.writeByte(255);
                packetWriter.writeShort(itemStack.getAmount());
            } else {
                packetWriter.writeByte(itemStack.getAmount());
            }
        }
        packetWriter.finishVariableShortPacket();
        this.player.writePacketBuffer(packetWriter.getBuffer());
        return this;
    }

    private PacketSender sendRevision443InterfaceSlotItem(int componentId, int slot,
            ItemStack itemStack, int amount) {
        if (slot < 0 || slot > 32767) {
            return this;
        }
        componentId = this.translateComponent(componentId, "slot=" + slot + " item=" + (itemStack == null ? -1 : itemStack.getId()) + " amount=" + amount);
        if (componentId == InterfaceBridge.UNMAPPED) {
            return this;
        }
        if (slot == 4 && (componentId == (312 << 16 | 147)
                || componentId == (312 << 16 | 148))) {
            componentId = 312 << 16 | ((componentId & 0xffff) == 147 ? 166 : 170);
            slot = 0;
        }
        PacketWriter packetWriter = PacketBuffer.allocateWriter(16);
        packetWriter.startVariableShortPacket(this.player.getOutboundCipher(), 213);
        packetWriter.writeInt(componentId);
        packetWriter.writeShort(componentId);
        if (slot < 128) {
            packetWriter.writeByte(slot);
        } else {
            packetWriter.writeShort(slot + 32768);
        }
        int itemId = itemStack == null ? 0 : itemStack.getId() + 1;
        packetWriter.writeShort(itemId);
        if (itemId != 0) {
            if (amount >= 255) {
                packetWriter.writeByte(255);
                packetWriter.writeInt(amount);
            } else {
                packetWriter.writeByte(amount);
            }
        }
        packetWriter.finishVariableShortPacket();
        this.player.writePacketBuffer(packetWriter.getBuffer());
        return this;
    }

    public final PacketSender sendSingleItemContainer(int itemId, int value2, int value32) {
        if (this.player.isBot) {
            return this;
        }
        if (ServerSettings.clientBuild == 443) {
            return this.sendItemContainer(itemId, new ItemStack[] {
                    value2 > 0 ? new ItemStack(value2, value32) : null
            });
        }
        PacketWriter packetWriter = PacketBuffer.allocateWriter(8192);
        packetWriter.startVariableShortPacket(this.player.getOutboundCipher(), 53);
        packetWriter.writeShort(itemId);
        packetWriter.writeShort(1);
        if (value2 > 0) {
            if (value32 > 254) {
                packetWriter.writeByte(255);
                packetWriter.writeInt(value32, ByteOrder.INVERSE_MIDDLE);
            } else {
                packetWriter.writeByte(value32);
            }
            packetWriter.writeShort(value2 + 1, ByteTransform.ADD, ByteOrder.LITTLE);
        } else {
            packetWriter.writeByte(0);
            packetWriter.writeShort(0, ByteTransform.ADD, ByteOrder.LITTLE);
        }
        packetWriter.finishVariableShortPacket();
        this.player.writePacketBuffer(packetWriter.getBuffer());
        return this;
    }

    public final PacketSender sendItemContainer(int itemId, ItemStack[] itemStackArray) {
        if (this.player.isBot) {
            return this;
        }
        if (ServerSettings.clientBuild == 443) {
            itemId = this.translateComponent(itemId, "slots=" + itemStackArray.length);
            if (itemId == InterfaceBridge.UNMAPPED) return this;
            PacketWriter packetWriter = PacketBuffer.allocateWriter(8192);
            packetWriter.startVariableShortPacket(this.player.getOutboundCipher(), 228);
            packetWriter.writeInt(itemId);
            packetWriter.writeShort(itemId);
            packetWriter.writeShort(itemStackArray.length);
            for (ItemStack itemStack : itemStackArray) {
                if (itemStack == null) {
                    packetWriter.writeByte(0);
                    packetWriter.writeShort(0);
                } else {
                    int amount = itemStack.getAmount();
                    if (amount > 254) {
                        packetWriter.writeByte(255, ByteTransform.NEGATE);
                        packetWriter.writeInt(amount);
                    } else {
                        packetWriter.writeByte(amount, ByteTransform.NEGATE);
                    }
                    packetWriter.writeShort(itemStack.getId() + 1);
                }
            }
            packetWriter.finishVariableShortPacket();
            this.player.writePacketBuffer(packetWriter.getBuffer());
            return this;
        }
        if (!PacketSender.isClientInterfaceIdSupported(itemId)) return this;
        PacketWriter packetWriter = PacketBuffer.allocateWriter(8192);
        packetWriter.startVariableShortPacket(this.player.getOutboundCipher(), 53);
        packetWriter.writeShort(itemId);
        packetWriter.writeShort(itemStackArray.length);
        ItemStack[] itemStackArray2 = itemStackArray;
        int length = itemStackArray.length;
        int index = 0;
        while (index < length) {
            ItemStack itemStack = itemStackArray2[index];
            if (itemStack != null) {
                if (itemStack.getAmount() > 254) {
                    packetWriter.writeByte(255);
                    packetWriter.writeInt(itemStack.getAmount(), ByteOrder.INVERSE_MIDDLE);
                } else {
                    packetWriter.writeByte(itemStack.getAmount());
                }
                packetWriter.writeShort(itemStack.getId() + 1, ByteTransform.ADD, ByteOrder.LITTLE);
            } else {
                packetWriter.writeByte(0);
                packetWriter.writeShort(0, ByteTransform.ADD, ByteOrder.LITTLE);
            }
            ++index;
        }
        packetWriter.finishVariableShortPacket();
        this.player.writePacketBuffer(packetWriter.getBuffer());
        return this;
    }

    public final PacketSender sendObjectCreate(int objectId, int value2, int value32, int value42, int value52, int value62) {
        if (this.player.isBot) {
            return this;
        }
        Position position = new Position(value2, value32, value42);
        if (ServerSettings.clientBuild == 443) {
            if (objectId == ServerSettings.placeholderObjectId) {
                ObjectPacket.sendRemove(this.player, position, value52, value62);
            } else {
                ObjectPacket.sendCreate(this.player, objectId, position, value52, value62);
            }
            return this;
        }
        this.sendLocalScenePosition(position);
        PacketWriter packetWriter = PacketBuffer.allocateWriter(5);
        packetWriter.writeOpcode(this.player.getOutboundCipher(), 151);
        packetWriter.writeByte(0, ByteTransform.SUBTRACT);
        packetWriter.writeShort(objectId, ByteOrder.LITTLE);
        packetWriter.writeByte((value62 << 2) + (value52 & 3), ByteTransform.SUBTRACT);
        this.player.writePacketBuffer(packetWriter.getBuffer());
        return this;
    }

    public final PacketSender closeInterface(int interfaceId) {
        if (this.player.isBot) {
            return this;
        }
        if (ServerSettings.clientBuild == 443) {
            if (interfaceId != -1) {
                interfaceId = InterfaceBridge.translateGroup(interfaceId);
                if (interfaceId == InterfaceBridge.UNMAPPED) return this;
            }
            PacketWriter packetWriter = PacketBuffer.allocateWriter(3);
            packetWriter.writeOpcode(this.player.getOutboundCipher(), 140);
            packetWriter.writeShort(interfaceId, ByteTransform.ADD);
            this.player.writePacketBuffer(packetWriter.getBuffer());
            return this;
        }
        if (interfaceId >= InterfaceDefinition.interfaceCount) {
            return this;
        }
        PacketWriter packetWriter = PacketBuffer.allocateWriter(4);
        packetWriter.writeOpcode(this.player.getOutboundCipher(), 218);
        packetWriter.writeShort(interfaceId, ByteTransform.ADD, ByteOrder.LITTLE);
        this.player.writePacketBuffer(packetWriter.getBuffer());
        return this;
    }

    public final PacketSender sendGameMessage(String gameMessage) {
        if (this.player.isBot) {
            return this;
        }
        if (gameMessage == null) {
            return this;
        }
        if (this.player.getQuestState(0) != 1) {
            return this;
        }
        if (ServerSettings.clientBuild == 443) {
            PacketWriter packetWriter = PacketBuffer.allocateWriter(gameMessage.length() + 3);
            packetWriter.startVariableBytePacket(this.player.getOutboundCipher(), 157);
            packetWriter.writeJString(gameMessage);
            packetWriter.finishVariableBytePacket();
            this.player.writePacketBuffer(packetWriter.getBuffer());
            return this;
        }
        PacketWriter packetWriter = PacketBuffer.allocateWriter(gameMessage.length() + 3);
        packetWriter.startVariableBytePacket(this.player.getOutboundCipher(), 253);
        packetWriter.writeString(gameMessage);
        packetWriter.finishVariableBytePacket();
        this.player.writePacketBuffer(packetWriter.getBuffer());
        return this;
    }

    public final PacketSender sendAccountStatus() {
        if (this.player.isBot) {
            return this;
        }
        if (ServerSettings.clientBuild == 443) {
            // Rights and the local-player index are part of the 443 login response.
            this.player.setAppearanceUpdateRequired(true);
            return this;
        }
        int gameMode = this.player.gameMode;
        if (gameMode < 0 || gameMode > 3) {
            System.err.println("Invalid outbound game mode " + gameMode + " for " + this.player.getUsername() + "; resetting to normal mode (0).");
            gameMode = 0;
            this.player.gameMode = 0;
        }
        PacketWriter packetWriter = PacketBuffer.allocateWriter(4);
        packetWriter.writeOpcode(this.player.getOutboundCipher(), 250);
        packetWriter.writeByte(this.player.getPlayerRights());
        packetWriter.writeByte(0);
        packetWriter.writeByte(gameMode);
        this.player.writePacketBuffer(packetWriter.getBuffer());
        this.player.setAppearanceUpdateRequired(true);
        return this;
    }

    public final void clearSidebarInterfaces() {
        int index = 0;
        while (index < 14) {
            this.setSidebarInterface(index, -1);
            ++index;
        }
    }

    public final PacketSender setSidebarInterface(int interfaceId, int value2) {
        this.player.setSidebarInterfaceId(interfaceId, value2);
        if (this.player.isBot) {
            return this;
        }
        if (ServerSettings.clientBuild == 443) {
            boolean clear = value2 == -1;
            int legacyGroup = value2;
            value2 = InterfaceBridge.translateGroup(value2, "tab=" + interfaceId);
            if (value2 == InterfaceBridge.UNMAPPED && !clear) return this;
            value2 = ClassicSidebar.tabGroup(this.player, interfaceId, legacyGroup, value2);
            PacketWriter packetWriter = PacketBuffer.allocateWriter(4);
            packetWriter.writeOpcode(this.player.getOutboundCipher(), 90);
            packetWriter.writeByte(interfaceId);
            packetWriter.writeShort(value2, ByteTransform.ADD);
            this.player.writePacketBuffer(packetWriter.getBuffer());
            return this;
        }
        PacketWriter packetWriter = PacketBuffer.allocateWriter(4);
        packetWriter.writeOpcode(this.player.getOutboundCipher(), 71);
        packetWriter.writeShort(value2);
        packetWriter.writeByte(interfaceId, ByteTransform.ADD);
        this.player.writePacketBuffer(packetWriter.getBuffer());
        return this;
    }

    public final PacketSender sendInterfaceItemModel(int interfaceId, int value2) {
        if (this.player.isBot) {
            return this;
        }
        if (ServerSettings.clientBuild == 443) {
            return this.sendInterfaceModel(interfaceId, interfaceId >= 18890 && interfaceId <= 19102 ? 100 : 1, value2);
        }
        PacketWriter packetWriter = PacketBuffer.allocateWriter(5);
        packetWriter.writeOpcode(this.player.getOutboundCipher(), 16);
        packetWriter.writeShort(interfaceId);
        packetWriter.writeShort(value2);
        this.player.writePacketBuffer(packetWriter.getBuffer());
        return this;
    }

    public final PacketSender sendInterfaceProgress(int interfaceId, int value2) {
        if (this.player.isBot) {
            return this;
        }
        if (ServerSettings.clientBuild == 443) {
            // These bars are flat custom widgets retained by the paired hybrid
            // client. Opcode 18 is its explicit three-byte progress extension.
            if (interfaceId != 19011 && (interfaceId < 19049 || interfaceId > 19094
                    || (interfaceId - 19049) % 9 != 0)) return this;
            PacketWriter packetWriter = PacketBuffer.allocateWriter(4);
            packetWriter.writeOpcode(this.player.getOutboundCipher(), 18);
            packetWriter.writeShort(interfaceId);
            packetWriter.writeByte(Math.max(0, Math.min(value2, 255)));
            this.player.writePacketBuffer(packetWriter.getBuffer());
            return this;
        }
        PacketWriter packetWriter = PacketBuffer.allocateWriter(4);
        if (value2 < 0) {
            value2 = 0;
        }
        packetWriter.writeOpcode(this.player.getOutboundCipher(), 18);
        packetWriter.writeShort(interfaceId);
        packetWriter.writeByte(value2);
        this.player.writePacketBuffer(packetWriter.getBuffer());
        return this;
    }

    public final PacketSender sendProjectile(Position position, int value9, int value23, byte value10, byte value24, int value33, int value42, int value52, int value62, int value72, int value82) {
        if (this.player.isBot) {
            return this;
        }
        if (ServerSettings.clientBuild == 443) {
            Position sourcePosition = value9 > 1
                    ? new Position(position.getX() + value9 / 2,
                            position.getY() + value9 / 2, position.getPlane())
                    : position;
            ZonePacket.sendProjectile(this.player, sourcePosition,
                    value33, value10, value24, value23, value42, value52,
                    value62, value72, value82, 64);
            return this;
        }
        if (value9 > 1) {
            this.sendLocalPosition(new Position(((Position)position).getX() + value9 / 2, ((Position)position).getY() + value9 / 2));
        } else {
            this.sendLocalPosition((Position)position);
        }
        PacketWriter packetWriter = PacketBuffer.allocateWriter(16);
        packetWriter.writeOpcode(this.player.getOutboundCipher(), 117);
        packetWriter.writeByte(50);
        packetWriter.writeByte(value10);
        packetWriter.writeByte(value24);
        packetWriter.writeShort(value23);
        packetWriter.writeShort(value33);
        packetWriter.writeByte(value62);
        packetWriter.writeByte(value72);
        packetWriter.writeShort(value42);
        packetWriter.writeShort(value52);
        packetWriter.writeByte(value82);
        packetWriter.writeByte(64);
        this.player.writePacketBuffer(packetWriter.getBuffer());
        return this;
    }

    private PacketSender sendLocalPosition(Position position) {
        if (this.player.isBot) {
            return this;
        }
        PacketWriter packetWriter = PacketBuffer.allocateWriter(3);
        packetWriter.writeOpcode(this.player.getOutboundCipher(), 85);
        int y = position.getY() - (this.player.getLastKnownRegionPosition().getRegionY() << 3) - 2;
        int x = position.getX() - (this.player.getLastKnownRegionPosition().getRegionX() << 3) - 3;
        packetWriter.writeByte(y, ByteTransform.NEGATE);
        packetWriter.writeByte(x, ByteTransform.NEGATE);
        this.player.writePacketBuffer(packetWriter.getBuffer());
        return this;
    }

    public final PacketSender sendMapRegion() {
        this.player.getLastKnownRegionPosition().set(this.player.getPosition());
        this.player.refreshLocalViewArea();
        int lastKnownRegionPosition = this.player.getLastKnownRegionPosition().getRegionX() << 3;
        int lastKnownRegionPosition2 = this.player.getLastKnownRegionPosition().getRegionY() << 3;
        this.player.localX = this.player.getPosition().getX() - lastKnownRegionPosition;
        this.player.localY = this.player.getPosition().getY() - lastKnownRegionPosition2;
        if (this.player.isBot) {
            return this;
        }
        if (ServerSettings.clientBuild == 443) {
            int centerX = this.player.getPosition().getRegionX() + 6;
            int centerY = this.player.getPosition().getRegionY() + 6;
            PacketWriter region = PacketBuffer.allocateWriter(512);
            RegionPacket.write(region, this.player.getOutboundCipher(),
                    centerX, centerY,
                    this.player.localX, this.player.localY,
                    this.player.getPosition().getPlane());
            System.out.println("[443-REGION] " + this.player.getUsername()
                    + " world=" + this.player.getPosition().getX() + ","
                    + this.player.getPosition().getY() + " center=" + centerX + ","
                    + centerY + " local=" + this.player.localX + ","
                    + this.player.localY + " plane=" + this.player.getPosition().getPlane());
            this.player.writePacketBuffer(region.getBuffer());
            return this;
        }
        PacketWriter packetWriter = PacketBuffer.allocateWriter(5);
        packetWriter.writeOpcode(this.player.getOutboundCipher(), 73);
        packetWriter.writeShort(this.player.getPosition().getRegionX() + 6, ByteTransform.ADD);
        packetWriter.writeShort(this.player.getPosition().getRegionY() + 6);
        this.player.writePacketBuffer(packetWriter.getBuffer());
        return this;
    }

    public final PacketSender sendLogout() {
        this.player.logoutPacketSent = true;
        if (this.player.isBot) {
            return this;
        }
        if (ServerSettings.clientBuild == 443) {
            PacketWriter packetWriter = PacketBuffer.allocateWriter(1);
            packetWriter.writeOpcode(this.player.getOutboundCipher(), 41);
            this.player.writePacketBuffer(packetWriter.getBuffer());
            return this;
        }
        PacketWriter packetWriter = PacketBuffer.allocateWriter(1);
        packetWriter.writeOpcode(this.player.getOutboundCipher(), 109);
        this.player.writePacketBuffer(packetWriter.getBuffer());
        return this;
    }

    public final PacketSender showInterface(int interfaceId) {
        this.clearFletchingCaptions(interfaceId);
        this.clearLevelUpIcon(interfaceId);
        this.player.setOpenInterfaceId(interfaceId);
        if (this.player.isBot) {
            return this;
        }
        if (ServerSettings.clientBuild == 443) {
            interfaceId = InterfaceBridge.translateGroup(interfaceId);
            if (interfaceId == InterfaceBridge.UNMAPPED) return this;
            PacketWriter packetWriter = PacketBuffer.allocateWriter(3);
            packetWriter.writeOpcode(this.player.getOutboundCipher(), 160);
            packetWriter.writeShort(interfaceId, ByteTransform.ADD);
            this.player.writePacketBuffer(packetWriter.getBuffer());
            return this;
        }
        if (interfaceId >= InterfaceDefinition.interfaceCount && interfaceId == 12140) {
            interfaceId = 8680;
        }
        if (!PacketSender.isClientInterfaceIdSupported(interfaceId)) return this;
        PacketWriter packetWriter = PacketBuffer.allocateWriter(3);
        packetWriter.writeOpcode(this.player.getOutboundCipher(), 97);
        packetWriter.writeShort(interfaceId);
        this.player.writePacketBuffer(packetWriter.getBuffer());
        return this;
    }

    public final PacketSender showWalkableInterface(int interfaceId) {
        if (this.player.isBot) {
            return this;
        }
        if (ServerSettings.clientBuild == 443) {
            boolean clear = interfaceId == -1;
            interfaceId = InterfaceBridge.translateGroup(interfaceId);
            if (interfaceId == InterfaceBridge.UNMAPPED && !clear) return this;
            PacketWriter packetWriter = PacketBuffer.allocateWriter(3);
            packetWriter.writeOpcode(this.player.getOutboundCipher(), 219);
            packetWriter.writeShort(interfaceId, ByteTransform.ADD);
            this.player.writePacketBuffer(packetWriter.getBuffer());
            return this;
        }
        if (interfaceId >= InterfaceDefinition.interfaceCount) {
            return this;
        }
        PacketWriter packetWriter = PacketBuffer.allocateWriter(3);
        packetWriter.writeOpcode(this.player.getOutboundCipher(), 208);
        packetWriter.writeShort(interfaceId, ByteOrder.LITTLE);
        this.player.writePacketBuffer(packetWriter.getBuffer());
        return this;
    }

    public final PacketSender sendInterfaceScrollPosition(int interfaceId, int value2) {
        if (this.player.isBot) {
            return this;
        }
        if (ServerSettings.clientBuild == 443) {
            interfaceId = this.translateComponent(interfaceId, "scroll=" + value2);
            if (interfaceId == InterfaceBridge.UNMAPPED) return this;
            PacketWriter packetWriter = PacketBuffer.allocateWriter(7);
            packetWriter.writeOpcode(this.player.getOutboundCipher(), 88);
            packetWriter.writeInt(interfaceId, ByteOrder.MIDDLE);
            packetWriter.writeShort(value2, ByteTransform.ADD);
            this.player.writePacketBuffer(packetWriter.getBuffer());
            return this;
        }
        PacketWriter packetWriter = PacketBuffer.allocateWriter(6);
        packetWriter.writeOpcode(this.player.getOutboundCipher(), 79);
        packetWriter.writeShort(8717, ByteOrder.LITTLE);
        packetWriter.writeShort(0, ByteTransform.ADD);
        this.player.writePacketBuffer(packetWriter.getBuffer());
        return this;
    }

    public final PacketSender sendMultiwayAreaState(boolean state) {
        if (state == this.player.multiwayAreaState) {
            return this;
        }
        this.player.multiwayAreaState = state;
        if (this.player.isBot) {
            return this;
        }
        if (ServerSettings.clientBuild == 443) {
            PacketWriter packetWriter = PacketBuffer.allocateWriter(2);
            packetWriter.writeOpcode(this.player.getOutboundCipher(), 236);
            packetWriter.writeByte(state ? 1 : 0);
            this.player.writePacketBuffer(packetWriter.getBuffer());
            return this;
        }
        PacketWriter packetWriter = PacketBuffer.allocateWriter(5);
        packetWriter.writeOpcode(this.player.getOutboundCipher(), 61);
        packetWriter.writeByte(state ? 1 : 0);
        this.player.writePacketBuffer(packetWriter.getBuffer());
        return this;
    }

    public final PacketSender showInterfaceWithInventory(int interfaceId, int value2) {
        this.player.setOpenInterfaceId(interfaceId);
        this.player.setInventoryOverlayInterfaceId(value2);
        if (this.player.isBot) {
            return this;
        }
        if (ServerSettings.clientBuild == 443) {
            interfaceId = InterfaceBridge.translateGroup(interfaceId, "role=main");
            value2 = InterfaceBridge.translateGroup(value2, "role=inventory");
            if (interfaceId == InterfaceBridge.UNMAPPED
                    || value2 == InterfaceBridge.UNMAPPED) return this;
            PacketWriter packetWriter = PacketBuffer.allocateWriter(5);
            packetWriter.writeOpcode(this.player.getOutboundCipher(), 146);
            packetWriter.writeShort(interfaceId, ByteTransform.ADD, ByteOrder.LITTLE);
            packetWriter.writeShort(value2);
            this.player.writePacketBuffer(packetWriter.getBuffer());
            return this;
        }
        if (!PacketSender.isClientInterfaceIdSupported(value2)) return this;
        PacketWriter packetWriter = PacketBuffer.allocateWriter(5);
        packetWriter.writeOpcode(this.player.getOutboundCipher(), 248);
        packetWriter.writeShort(interfaceId, ByteTransform.ADD);
        packetWriter.writeShort(value2);
        this.player.writePacketBuffer(packetWriter.getBuffer());
        return this;
    }

    public final PacketSender flashSidebarIcon(int value2) {
        if (this.player.isBot) {
            return this;
        }
        if (ServerSettings.clientBuild == 443) {
            PacketWriter packetWriter = PacketBuffer.allocateWriter(2);
            packetWriter.writeOpcode(this.player.getOutboundCipher(), 148);
            packetWriter.writeByte(value2, ByteTransform.NEGATE);
            this.player.writePacketBuffer(packetWriter.getBuffer());
            return this;
        }
        PacketWriter packetWriter = PacketBuffer.allocateWriter(3);
        packetWriter.writeOpcode(this.player.getOutboundCipher(), 24);
        packetWriter.writeByte(-value2, ByteTransform.ADD);
        this.player.writePacketBuffer(packetWriter.getBuffer());
        return this;
    }

    /**
     * Native make-item chatboxes are reused for other production skills.
     * Remove fletching-only synthetic name captions whenever that menu closes
     * so a later crafting chatbox cannot display stale bow/shaft names.
     */
    private void clearFletchingCaptions(int nextInterfaceId) {
        if (ServerSettings.clientBuild != 443) return;
        int current = this.player.getOpenInterfaceId();
        if (current == nextInterfaceId) return;
        int start, count;
        if (current == 20120) {
            start = 20203;
            count = 3;
        } else if (current == 20121) {
            start = 20208;
            count = 2;
        } else if (current == 20122) {
            start = 20211;
            count = 1;
        } else {
            return;
        }
        for (int i = 0; i < count; i++) {
            this.sendInterfaceText("", start + i);
        }
    }

    // The level-up chatbox shares native group 211 with ordinary two-line
    // statements. Clear its icon before leaving, so no unrelated dialogue
    // can inherit the previously displayed skill graphic.
    private void clearLevelUpIcon(int nextInterfaceId) {
        if (ServerSettings.clientBuild == 443
                && this.player.getOpenInterfaceId() == 19580
                && nextInterfaceId != 19580) {
            this.sendInterfaceText("", 19581);
        }
    }

    public final PacketSender closeInterfaces() {
        this.clearFletchingCaptions(0);
        this.clearLevelUpIcon(0);
        this.player.setOpenInterfaceId(0);
        this.player.setInventoryOverlayInterfaceId(0);
        this.player.activeBookItemId = 0;
        this.player.activeBookPageIndex = 0;
        if (this.player.isBot) {
            return this;
        }
        if (ServerSettings.clientBuild == 443) {
            PacketWriter packetWriter = PacketBuffer.allocateWriter(1);
            packetWriter.writeOpcode(this.player.getOutboundCipher(), 178);
            this.player.writePacketBuffer(packetWriter.getBuffer());
            return this;
        }
        PacketWriter packetWriter = PacketBuffer.allocateWriter(2);
        packetWriter.writeOpcode(this.player.getOutboundCipher(), 219);
        this.player.writePacketBuffer(packetWriter.getBuffer());
        return this;
    }

    private PacketSender sendLocalScenePosition(Position position) {
        if (this.player.isBot) {
            return this;
        }
        PacketWriter packetWriter = PacketBuffer.allocateWriter(3);
        packetWriter.writeOpcode(this.player.getOutboundCipher(), 85);
        int y = position.getY() - 8 * this.player.getLastKnownRegionPosition().getRegionY();
        int x = position.getX() - 8 * this.player.getLastKnownRegionPosition().getRegionX();
        packetWriter.writeByte(y, ByteTransform.NEGATE);
        packetWriter.writeByte(x, ByteTransform.NEGATE);
        this.player.writePacketBuffer(packetWriter.getBuffer());
        return this;
    }

    public final PacketSender sendGroundItemCreate(GroundItem groundItem) {
        if (this.player.isBot) {
            return this;
        }
        if (ServerSettings.clientBuild == 443) {
            ZonePacket.sendGroundItemCreate(this.player, groundItem);
            return this;
        }
        this.sendLocalScenePosition(groundItem.getPosition());
        PacketWriter packetWriter = PacketBuffer.allocateWriter(8);
        packetWriter.writeOpcode(this.player.getOutboundCipher(), 44);
        packetWriter.writeShort(groundItem.getItem().getId(), ByteTransform.ADD, ByteOrder.LITTLE);
        packetWriter.writeInt(groundItem.getItem().getAmount());
        packetWriter.writeByte(0);
        this.player.writePacketBuffer(packetWriter.getBuffer());
        return this;
    }

    public final PacketSender sendGroundItemRemove(GroundItem groundItem) {
        if (this.player.isBot) {
            return this;
        }
        if (ServerSettings.clientBuild == 443) {
            ZonePacket.sendGroundItemRemove(this.player, groundItem);
            return this;
        }
        this.sendLocalScenePosition(groundItem.getPosition());
        PacketWriter packetWriter = PacketBuffer.allocateWriter(4);
        packetWriter.writeOpcode(this.player.getOutboundCipher(), 156);
        packetWriter.writeByte(0, ByteTransform.SUBTRACT);
        packetWriter.writeShort(groundItem.getItem().getId());
        this.player.writePacketBuffer(packetWriter.getBuffer());
        return this;
    }

    public final PacketSender sendConfig(int value3, int value22) {
        if (this.player.isBot) {
            return this;
        }
        if (ServerSettings.clientBuild == 443) {
            // InitialVarps owns the verified legacy-to-443 varp set.
            // Other callers still use 377 ids and must not mutate unrelated 443 varps.
            boolean musicConfig = com.rs2.model.music.Music.isMusicConfig(value3);
            boolean verified443Varp = InitialVarps.isVerified(value3) || musicConfig;
            PacketAudit.legacyVarp(value3, value22, verified443Varp);
            if (verified443Varp) {
                VarpPacket.send(this.player, value3, musicConfig
                        ? com.rs2.model.music.Music.configValue(this.player, value3) : value22);
            }
            return this;
        }
        if (value22 < 128 && -128 <= value22) {
            PacketWriter packetWriter = PacketBuffer.allocateWriter(4);
            packetWriter.writeOpcode(this.player.getOutboundCipher(), 36);
            packetWriter.writeShort(value3, ByteOrder.LITTLE);
            packetWriter.writeByte(value22);
            this.player.writePacketBuffer(packetWriter.getBuffer());
        } else {
            PacketWriter packetWriter = PacketBuffer.allocateWriter(7);
            packetWriter.writeOpcode(this.player.getOutboundCipher(), 87);
            packetWriter.writeShort(value3, ByteOrder.LITTLE);
            packetWriter.writeInt(value22, ByteOrder.MIDDLE);
            this.player.writePacketBuffer(packetWriter.getBuffer());
        }
        return this;
    }

    public final PacketSender sendInterfaceTextColor(int interfaceId, Color color) {
        if (this.player.isBot) {
            return this;
        }
        if (ServerSettings.clientBuild == 443) {
            interfaceId = this.translateComponent(interfaceId, "rgb=" + color.getRGB());
            if (interfaceId == InterfaceBridge.UNMAPPED) return this;
            int rgb555 = (color.getRed() >> 3 & 0x1F) << 10
                    | (color.getGreen() >> 3 & 0x1F) << 5
                    | color.getBlue() >> 3 & 0x1F;
            PacketWriter packetWriter = PacketBuffer.allocateWriter(7);
            packetWriter.writeOpcode(this.player.getOutboundCipher(), 3);
            packetWriter.writeInt(interfaceId, ByteOrder.MIDDLE);
            packetWriter.writeShort(rgb555, ByteOrder.LITTLE);
            this.player.writePacketBuffer(packetWriter.getBuffer());
            return this;
        }
        if (interfaceId >= InterfaceDefinition.interfaceCount) {
            return this;
        }
        PacketWriter packetWriter = PacketBuffer.allocateWriter(5);
        packetWriter.writeOpcode(this.player.getOutboundCipher(), 122);
        packetWriter.writeShort(interfaceId, ByteTransform.ADD, ByteOrder.LITTLE);
        interfaceId = color.getRed() >> 3 & 0x1F;
        int green = color.getGreen() >> 3 & 0x1F;
        int blue = color.getBlue() >> 3 & 0x1F;
        packetWriter.writeShort(interfaceId << 10 | green << 5 | blue, ByteTransform.ADD, ByteOrder.LITTLE);
        this.player.writePacketBuffer(packetWriter.getBuffer());
        return this;
    }

    public final PacketSender sendInterfaceText(String interfaceId, int interfaceId2) {
        if (this.player.isBot) {
            return this;
        }
        if (ServerSettings.clientBuild == 443) {
            interfaceId2 = this.translateComponent(interfaceId2, "text=\"" + interfaceId + "\"");
            if (interfaceId2 == InterfaceBridge.UNMAPPED) return this;
            return this.sendRevision443InterfaceText(interfaceId, interfaceId2);
        }
        if (interfaceId2 >= InterfaceDefinition.interfaceCount) {
            if (interfaceId2 == 12144) {
                interfaceId2 = 8684;
            } else if (interfaceId2 == 12147) {
                interfaceId2 = 8925;
            } else if (interfaceId2 == 12150) {
                interfaceId2 = 8929;
                this.sendInterfaceText("", 8928);
            } else if (interfaceId2 == 12151) {
                interfaceId2 = 8930;
            } else if (interfaceId2 == 12152) {
                interfaceId2 = 8931;
            } else if (interfaceId2 == 12153) {
                interfaceId2 = 8932;
            } else if (interfaceId2 == 12154) {
                interfaceId2 = 8933;
            }
        }
        if (!PacketSender.isClientInterfaceIdSupported(interfaceId2)) {
            return this;
        }
        PacketWriter packetWriter = PacketBuffer.allocateWriter(interfaceId.length() + 6);
        packetWriter.startVariableShortPacket(this.player.getOutboundCipher(), 126);
        packetWriter.writeString(interfaceId);
        packetWriter.writeShort(interfaceId2, ByteTransform.ADD);
        packetWriter.finishVariableShortPacket();
        this.player.writePacketBuffer(packetWriter.getBuffer());
        return this;
    }

    private PacketSender sendRevision443InterfaceText(String text, int packedId) {
        if (this.player.isBot) return this;
        PacketWriter packetWriter = PacketBuffer.allocateWriter(text.length() + 7);
        packetWriter.startVariableShortPacket(this.player.getOutboundCipher(), 180);
        // Revision 443 packet 180 is decoded as a little-endian component id.
        packetWriter.writeInt(packedId, ByteOrder.LITTLE);
        packetWriter.writeJString(text);
        packetWriter.finishVariableShortPacket();
        this.player.writePacketBuffer(packetWriter.getBuffer());
        return this;
    }

    public final PacketSender sendFriendStatus(long value3, int value4) {
        if (ServerSettings.clientBuild == 443) {
            if (!this.player.isBot) {
                PacketWriter packetWriter = PacketBuffer.allocateWriter(11);
                packetWriter.writeOpcode(this.player.getOutboundCipher(), 179);
                packetWriter.writeLong(value3);
                packetWriter.writeShort(value4);
                this.player.writePacketBuffer(packetWriter.getBuffer());
            }
            return this;
        }
        if (this.player.isBot) {
            return this;
        }
        PacketWriter packetWriter = PacketBuffer.allocateWriter(10);
        packetWriter.writeOpcode(this.player.getOutboundCipher(), 50);
        if (value4 != 0) {
            value4 += 9;
        }
        packetWriter.writeLong(value3);
        packetWriter.writeByte(value4);
        this.player.writePacketBuffer(packetWriter.getBuffer());
        return this;
    }

    public final PacketSender sendPrivateMessagingStatus(int privateMessagingStatus) {
        if (ServerSettings.clientBuild == 443) {
            if (!this.player.isBot) {
                PacketWriter packetWriter = PacketBuffer.allocateWriter(2);
                packetWriter.writeOpcode(this.player.getOutboundCipher(), 57);
                packetWriter.writeByte(privateMessagingStatus);
                this.player.writePacketBuffer(packetWriter.getBuffer());
            }
            return this;
        }
        if (this.player.isBot) {
            return this;
        }
        PacketWriter packetWriter = PacketBuffer.allocateWriter(2);
        packetWriter.writeOpcode(this.player.getOutboundCipher(), 221);
        packetWriter.writeByte(2);
        this.player.writePacketBuffer(packetWriter.getBuffer());
        return this;
    }

    public final PacketSender sendPrivateMessage(long value5, int value6, int value23, int value32, byte[] byteValues2, int value42) {
        if (ServerSettings.clientBuild == 443) {
            if (this.player.isBot) return this;
            String message = ChatTextCodec.decode(byteValues2, value42).replaceAll("\\s+$", "");
            byte[] body = ChatCodec.get().encode(message);
            PacketWriter packetWriter = PacketBuffer.allocateWriter(20 + body.length);
            packetWriter.startVariableBytePacket(this.player.getOutboundCipher(), 25);
            packetWriter.writeLong(value5);
            int messageId = this.player.getSocialManager().nextPrivateMessageId();
            packetWriter.writeShort(messageId >>> 16);
            packetWriter.writeByte(messageId >>> 16);
            packetWriter.writeByte(messageId >>> 8);
            packetWriter.writeByte(messageId);
            packetWriter.writeByte(value6);
            packetWriter.writeBytes(body, body.length);
            packetWriter.finishVariableBytePacket();
            this.player.writePacketBuffer(packetWriter.getBuffer());
            return this;
        }
        if (this.player.isBot) {
            return this;
        }
        PacketWriter packetWriter = PacketBuffer.allocateWriter(2048);
        packetWriter.startVariableBytePacket(this.player.getOutboundCipher(), 196);
        packetWriter.writeLong(value5);
        packetWriter.writeInt(this.player.getSocialManager().nextPrivateMessageId());
        packetWriter.writeByte(value6);
        packetWriter.writeByte(0);
        packetWriter.writeByte(value32);
        packetWriter.writeBytes(byteValues2, value42);
        packetWriter.finishVariableBytePacket();
        this.player.writePacketBuffer(packetWriter.getBuffer());
        return this;
    }

    public final PacketSender sendInterfaceModel(int interfaceId, int value2, int value32) {
        if (this.player.isBot) {
            return this;
        }
        if (ServerSettings.clientBuild == 443) {
            interfaceId = this.translateComponent(interfaceId, "itemModel=" + value32 + " legacyZoom=" + value2);
            if (interfaceId == InterfaceBridge.UNMAPPED) return this;
            PacketWriter packetWriter = PacketBuffer.allocateWriter(11);
            packetWriter.writeOpcode(this.player.getOutboundCipher(), 137);
            // Native 443 old-format model widgets (including the item hand-off
            // chatboxes, groups 102 and 249) use this first int as the item
            // scale/zoom divisor. Passing media type 4 here makes the model far
            // too large and clips it out of the widget. Preserve the legacy
            // zoom value so tinderboxes, axes, etc. render at the intended size.
            packetWriter.writeInt(value32 < 0 ? 0 : value2);
            packetWriter.writeInt(interfaceId);
            packetWriter.writeShort(value32);
            this.player.writePacketBuffer(packetWriter.getBuffer());
            return this;
        }
        if (interfaceId >= InterfaceDefinition.interfaceCount && interfaceId == 12145) {
            interfaceId = 8923;
        }
        if (interfaceId >= InterfaceDefinition.interfaceCount) {
            return this;
        }
        PacketWriter packetWriter = PacketBuffer.allocateWriter(7);
        packetWriter.writeOpcode(this.player.getOutboundCipher(), 246);
        packetWriter.writeShort(interfaceId == 0 ? -1 : interfaceId, ByteOrder.LITTLE);
        packetWriter.writeShort(value2);
        packetWriter.writeShort(value32);
        this.player.writePacketBuffer(packetWriter.getBuffer());
        return this;
    }

    public final PacketSender sendInterfaceModelId(int interfaceId, int value2) {
        if (this.player.isBot) {
            return this;
        }
        if (ServerSettings.clientBuild == 443) {
            interfaceId = this.translateComponent(interfaceId, "model=" + value2);
            if (interfaceId == InterfaceBridge.UNMAPPED) return this;
            PacketWriter packetWriter = PacketBuffer.allocateWriter(7);
            packetWriter.writeOpcode(this.player.getOutboundCipher(), 24);
            packetWriter.writeShort(value2, ByteOrder.LITTLE);
            packetWriter.writeInt(interfaceId, ByteOrder.MIDDLE);
            this.player.writePacketBuffer(packetWriter.getBuffer());
            return this;
        }
        PacketWriter packetWriter = PacketBuffer.allocateWriter(5);
        packetWriter.writeOpcode(this.player.getOutboundCipher(), 8);
        packetWriter.writeShort(interfaceId, ByteTransform.ADD, ByteOrder.LITTLE);
        packetWriter.writeShort(value2);
        this.player.writePacketBuffer(packetWriter.getBuffer());
        return this;
    }

    public final PacketSender sendStillGraphic(int graphicId, Position position, int value2) {
        if (this.player.isBot) {
            return this;
        }
        if (ServerSettings.clientBuild == 443) {
            ZonePacket.sendSpotAnimation(this.player, position,
                    graphicId, 0, value2);
            return this;
        }
        this.sendLocalScenePosition(position);
        PacketWriter packetWriter = PacketBuffer.allocateWriter(7);
        packetWriter.writeOpcode(this.player.getOutboundCipher(), 4);
        packetWriter.writeByte(0);
        packetWriter.writeShort(graphicId);
        packetWriter.writeByte(position.getPlane());
        packetWriter.writeShort(value2);
        this.player.writePacketBuffer(packetWriter.getBuffer());
        return this;
    }

    public final PacketSender showChatboxInterface(int interfaceId) {
        this.clearFletchingCaptions(interfaceId);
        this.clearLevelUpIcon(interfaceId);
        this.player.setOpenInterfaceId(interfaceId);
        if (this.player.isBot) {
            return this;
        }
        if (ServerSettings.clientBuild == 443) {
            // Child 5 of native group 214 doubles as a client-only layout
            // control channel. Tutorial Island uses the exact 2006/317 text
            // baselines for NPC/player dialogue, statements, options and item
            // hand-offs; ordinary gameplay keeps the stock 443 coordinates.
            // Interface 6179 is the instruction panel and sends its own
            // __tutorial__ marker before opening, so do not overwrite it here.
            if (interfaceId != 6179) {
                this.sendInterfaceText(this.player.getQuestState(0) != 1
                        ? "__tutorial_dialogue__" : "__dialogue__", 6185);
            }
            interfaceId = InterfaceBridge.translateGroup(interfaceId);
            if (interfaceId == InterfaceBridge.UNMAPPED) return this;
            PacketWriter packetWriter = PacketBuffer.allocateWriter(3);
            packetWriter.writeOpcode(this.player.getOutboundCipher(), 97);
            packetWriter.writeShort(interfaceId, ByteTransform.ADD, ByteOrder.LITTLE);
            this.player.writePacketBuffer(packetWriter.getBuffer());
            return this;
        }
        if (interfaceId >= InterfaceDefinition.interfaceCount) return this;
        PacketWriter packetWriter = PacketBuffer.allocateWriter(3);
        packetWriter.writeOpcode(this.player.getOutboundCipher(), 164);
        packetWriter.writeShort(interfaceId, ByteOrder.LITTLE);
        this.player.writePacketBuffer(packetWriter.getBuffer());
        return this;
    }

    public final PacketSender sendInterfaceAnimation(int interfaceId, int value2) {
        if (this.player.isBot) {
            return this;
        }
        if (ServerSettings.clientBuild == 443) {
            interfaceId = this.translateComponent(interfaceId, "animation=" + value2);
            if (interfaceId == InterfaceBridge.UNMAPPED) return this;
            PacketWriter packetWriter = PacketBuffer.allocateWriter(7);
            packetWriter.writeOpcode(this.player.getOutboundCipher(), 95);
            packetWriter.writeInt(interfaceId, ByteOrder.LITTLE);
            packetWriter.writeShort(value2, ByteTransform.ADD, ByteOrder.LITTLE);
            this.player.writePacketBuffer(packetWriter.getBuffer());
            return this;
        }
        if (interfaceId >= InterfaceDefinition.interfaceCount) {
            return this;
        }
        PacketWriter packetWriter = PacketBuffer.allocateWriter(5);
        packetWriter.writeOpcode(this.player.getOutboundCipher(), 200);
        packetWriter.writeShort(interfaceId);
        packetWriter.writeShort(value2);
        this.player.writePacketBuffer(packetWriter.getBuffer());
        return this;
    }

    public final PacketSender sendObjectAnimation(int objectId, int value2, int value32, int value42) {
        if (this.player.isBot) {
            return this;
        }
        LoadedWorldObject loadedWorldObject = WorldObjectLookup.findObjectAt(objectId, value2, value32);
        if (loadedWorldObject == null) {
            return this;
        }
        Position position = new Position(objectId, value2, value32);
        if (ServerSettings.clientBuild == 443) {
            ObjectPacket.sendAnimation(this.player, position,
                    loadedWorldObject.getOrientation(), loadedWorldObject.getType(), value42);
            return this;
        }
        this.sendLocalScenePosition(position);
        PacketWriter packetWriter = PacketBuffer.allocateWriter(5);
        packetWriter.writeOpcode(this.player.getOutboundCipher(), 160);
        packetWriter.writeByte(0, ByteTransform.SUBTRACT);
        packetWriter.writeByte((loadedWorldObject.getType() << 2) + (loadedWorldObject.getOrientation() & 3), ByteTransform.SUBTRACT);
        packetWriter.writeShort(127, ByteTransform.ADD);
        this.player.writePacketBuffer(packetWriter.getBuffer());
        return this;
    }

    public final PacketSender sendPlayerHeadOnInterface(int interfaceId) {
        if (this.player.isBot) {
            return this;
        }
        if (ServerSettings.clientBuild == 443) {
            interfaceId = this.translateComponent(interfaceId);
            if (interfaceId == InterfaceBridge.UNMAPPED) return this;
            PacketWriter packetWriter = PacketBuffer.allocateWriter(5);
            packetWriter.writeOpcode(this.player.getOutboundCipher(), 31);
            packetWriter.writeInt(interfaceId, ByteOrder.LITTLE);
            this.player.writePacketBuffer(packetWriter.getBuffer());
            return this;
        }
        PacketWriter packetWriter = PacketBuffer.allocateWriter(3);
        packetWriter.writeOpcode(this.player.getOutboundCipher(), 185);
        packetWriter.writeShort(interfaceId, ByteTransform.ADD, ByteOrder.LITTLE);
        this.player.writePacketBuffer(packetWriter.getBuffer());
        return this;
    }

    public final PacketSender sendNpcHeadOnInterface(int npcId, int legacyComponentId) {
        if (this.player.isBot) {
            return this;
        }
        if (ServerSettings.clientBuild == 443) {
            int packedComponentId = this.translateComponent(legacyComponentId, "npc=" + npcId);
            if (packedComponentId == InterfaceBridge.UNMAPPED) return this;
            PacketWriter packetWriter = PacketBuffer.allocateWriter(7);
            packetWriter.writeOpcode(this.player.getOutboundCipher(), 227);
            packetWriter.writeShort(npcId);
            packetWriter.writeInt(packedComponentId);
            this.player.writePacketBuffer(packetWriter.getBuffer());
            return this;
        }
        if (legacyComponentId >= InterfaceDefinition.interfaceCount) {
            return this;
        }
        PacketWriter packetWriter = PacketBuffer.allocateWriter(5);
        packetWriter.writeOpcode(this.player.getOutboundCipher(), 75);
        packetWriter.writeShort(npcId, ByteTransform.ADD, ByteOrder.LITTLE);
        packetWriter.writeShort(legacyComponentId, ByteTransform.ADD, ByteOrder.LITTLE);
        this.player.writePacketBuffer(packetWriter.getBuffer());
        return this;
    }

    public final PacketSender sendSoundEffect(int soundId, int value2, int value32) {
        if (this.player.isBot) {
            return this;
        }
        if (soundId < 0) {
            return this;
        }
        if (ServerSettings.clientBuild == 443) {
            soundId = AudioIds443.sound(soundId);
            if (soundId < 0) {
                return this;
            }
            PacketWriter packetWriter = PacketBuffer.allocateWriter(6);
            packetWriter.writeOpcode(this.player.getOutboundCipher(), 81);
            packetWriter.writeShort(soundId);
            packetWriter.writeByte(value2);
            packetWriter.writeShort(value32);
            this.player.writePacketBuffer(packetWriter.getBuffer());
            return this;
        }
        PacketWriter packetWriter = PacketBuffer.allocateWriter(7);
        packetWriter.writeOpcode(this.player.getOutboundCipher(), 174);
        packetWriter.writeShort(soundId);
        packetWriter.writeByte(1);
        packetWriter.writeShort(value32);
        this.player.writePacketBuffer(packetWriter.getBuffer());
        return this;
    }

    public final PacketSender sendMusicJingle(int value3, int value22) {
        if (this.player.isBot) {
            return this;
        }
        if (ServerSettings.clientBuild == 443) {
            value3 = AudioIds443.jingle(value3);
            if (value3 < 0) {
                return this;
            }
            PacketWriter packetWriter = PacketBuffer.allocateWriter(6);
            packetWriter.writeOpcode(this.player.getOutboundCipher(), 155);
            packetWriter.writeByte(value22 >> 16);
            packetWriter.writeByte(value22);
            packetWriter.writeByte(value22 >> 8);
            packetWriter.writeShort(value3, ByteTransform.ADD);
            this.player.writePacketBuffer(packetWriter.getBuffer());
            return this;
        }
        PacketWriter packetWriter = PacketBuffer.allocateWriter(5);
        packetWriter.writeOpcode(this.player.getOutboundCipher(), 121);
        packetWriter.writeShort(value3, ByteTransform.ADD, ByteOrder.LITTLE);
        packetWriter.writeShort(value22, ByteTransform.ADD, ByteOrder.BIG);
        this.player.writePacketBuffer(packetWriter.getBuffer());
        return this;
    }

    public final PacketSender sendPlayerOption(String text2, int value2, boolean enabled2) {
        if (this.player.isBot) {
            return this;
        }
        if (value2 > 0 && value2 <= 5) {
            if (this.player.playerOptionTextCache[value2 - 1] != null && this.player.playerOptionTextCache[value2 - 1].equals(text2)) {
                return this;
            }
            this.player.playerOptionTextCache[value2 - 1] = text2;
        }
        if (ServerSettings.clientBuild == 443) {
            PacketWriter packetWriter = PacketBuffer.allocateWriter(text2.length() + 5);
            packetWriter.startVariableBytePacket(this.player.getOutboundCipher(), 130);
            packetWriter.writeByte(value2);
            packetWriter.writeJString(text2);
            packetWriter.writeByte(enabled2 ? 1 : 0, ByteTransform.ADD);
            packetWriter.finishVariableBytePacket();
            this.player.writePacketBuffer(packetWriter.getBuffer());
            return this;
        }
        PacketWriter packetWriter = PacketBuffer.allocateWriter(text2.length() + 5);
        packetWriter.startVariableBytePacket(this.player.getOutboundCipher(), 104);
        packetWriter.writeByte(value2, ByteTransform.NEGATE);
        packetWriter.writeByte(0, ByteTransform.ADD);
        packetWriter.writeString(text2);
        packetWriter.finishVariableBytePacket();
        this.player.writePacketBuffer(packetWriter.getBuffer());
        return this;
    }

    public final PacketSender sendPlayerIndex() {
        if (this.player.isBot || ServerSettings.clientBuild == 443) {
            // The revision 443 login success frame already contains this index.
            // Opcode 249 is the older game's separate index packet.
            return this;
        }
        PacketWriter packetWriter = PacketBuffer.allocateWriter(4);
        packetWriter.writeOpcode(this.player.getOutboundCipher(), 249);
        packetWriter.writeByte(1, ByteTransform.ADD);
        packetWriter.writeShort(this.player.getIndex(), ByteTransform.ADD, ByteOrder.LITTLE);
        this.player.writePacketBuffer(packetWriter.getBuffer());
        return this;
    }

    public final PacketSender sendRunEnergy() {
        if (this.player.isBot) {
            return this;
        }
        if (ServerSettings.clientBuild == 443) {
            PacketWriter packetWriter = PacketBuffer.allocateWriter(2);
            packetWriter.writeOpcode(this.player.getOutboundCipher(), 226);
            packetWriter.writeByte(this.player.getRunEnergyPercent());
            this.player.writePacketBuffer(packetWriter.getBuffer());
            return this;
        }
        PacketWriter packetWriter = PacketBuffer.allocateWriter(2);
        packetWriter.writeOpcode(this.player.getOutboundCipher(), 110);
        packetWriter.writeByte(this.player.getRunEnergyPercent());
        this.player.writePacketBuffer(packetWriter.getBuffer());
        return this;
    }

    public final PacketSender sendWeight() {
        if (this.player.isBot) {
            return this;
        }
        if (ServerSettings.clientBuild == 443) {
            PacketWriter packetWriter = PacketBuffer.allocateWriter(3);
            packetWriter.writeOpcode(this.player.getOutboundCipher(), 72);
            packetWriter.writeShort((int)Math.floor(this.player.carriedWeight));
            this.player.writePacketBuffer(packetWriter.getBuffer());
            return this;
        }
        PacketWriter packetWriter = PacketBuffer.allocateWriter(3);
        packetWriter.writeOpcode(this.player.getOutboundCipher(), 240);
        Player player = this.player;
        packetWriter.writeShort((int)Math.floor(player.carriedWeight));
        this.player.writePacketBuffer(packetWriter.getBuffer());
        return this;
    }

    public final void refreshAutocastConfig() {
        if (ServerSettings.clientBuild == 443) {
            // The native staff interface selects its spell attack with varp 43
            // and reads autocast state from varbit 2668 (varp 439, bit 8).
            // Legacy autocast varp 108 has no native 443 counterpart.
            this.sendConfig(439, this.player.isAutocastEnabled() ? 256 : 0);
            this.sendConfig(43, this.player.isAutocastEnabled() ? 3 : this.player.getFightMode());
            ClassicSidebar.refreshAutocast(this.player);
            return;
        }
        if (this.player.getAutocastSpell() == null) {
            this.sendConfig(108, 0);
            this.sendConfig(43, this.player.getFightMode());
            this.sendInterfaceText("", 352);
            return;
        }
        this.sendConfig(43, this.player.getFightMode());
        this.sendConfig(108, 2);
    }

    public final PacketSender setInterfaceHiddenFlag(int interfaceId, int value2) {
        if (this.player.isBot) {
            return this;
        }
        if (ServerSettings.clientBuild == 443) {
            value2 = this.translateComponent(value2, "hidden=" + (interfaceId != 0));
            if (value2 == InterfaceBridge.UNMAPPED) return this;
            PacketWriter packetWriter = PacketBuffer.allocateWriter(6);
            packetWriter.writeOpcode(this.player.getOutboundCipher(), 234);
            packetWriter.writeByte(interfaceId, ByteTransform.ADD);
            packetWriter.writeInt(value2, ByteOrder.INVERSE_MIDDLE);
            this.player.writePacketBuffer(packetWriter.getBuffer());
            return this;
        }
        if (!PacketSender.isClientInterfaceIdSupported(value2)) {
            return this;
        }
        PacketWriter packetWriter = PacketBuffer.allocateWriter(4);
        packetWriter.writeOpcode(this.player.getOutboundCipher(), 171);
        packetWriter.writeByte(interfaceId);
        packetWriter.writeShort(value2);
        this.player.writePacketBuffer(packetWriter.getBuffer());
        return this;
    }

    public final PacketSender sendCameraShake(int value5, int value22, int value32, int value42) {
        if (this.player.isBot) {
            return this;
        }
        if (ServerSettings.clientBuild == 443) {
            PacketWriter packetWriter = PacketBuffer.allocateWriter(7);
            packetWriter.writeOpcode(this.player.getOutboundCipher(), 96);
            packetWriter.writeByte(value5);
            if (value5 == 1 || value5 == 2 || value5 == 3 || value5 == 4
                    || value5 == 5 || value5 == 6 || value5 == 10) {
                if (value5 == 1 || value5 == 10) {
                    packetWriter.writeShort(value22);
                } else {
                    packetWriter.writeShort(value22);
                    packetWriter.writeShort(value32);
                    packetWriter.writeByte(value42);
                }
            }
            this.player.writePacketBuffer(packetWriter.getBuffer());
            return this;
        }
        PacketWriter packetWriter = PacketBuffer.allocateWriter(5);
        packetWriter.writeOpcode(this.player.getOutboundCipher(), 35);
        packetWriter.writeByte(2);
        packetWriter.writeByte(3);
        packetWriter.writeByte(2);
        packetWriter.writeByte(3);
        this.player.writePacketBuffer(packetWriter.getBuffer());
        return this;
    }

    public final PacketSender setInterfaceVisible(int interfaceId, boolean interfaceId2) {
        if (this.player.isBot) {
            return this;
        }
        if (interfaceId >= InterfaceDefinition.interfaceCount) {
            return this;
        }
        if (ServerSettings.clientBuild == 443) {
            interfaceId = this.translateComponent(interfaceId, "visible=" + interfaceId2);
            if (interfaceId == InterfaceBridge.UNMAPPED) return this;
            PacketWriter packetWriter = PacketBuffer.allocateWriter(6);
            packetWriter.writeOpcode(this.player.getOutboundCipher(), 234);
            packetWriter.writeByte(interfaceId2 ? 0 : 1, ByteTransform.ADD);
            packetWriter.writeInt(interfaceId, ByteOrder.INVERSE_MIDDLE);
            this.player.writePacketBuffer(packetWriter.getBuffer());
            return this;
        }
        PacketWriter packetWriter = PacketBuffer.allocateWriter(4);
        packetWriter.writeOpcode(this.player.getOutboundCipher(), 171);
        packetWriter.writeByte(interfaceId2 ? 0 : 1);
        packetWriter.writeShort(interfaceId);
        this.player.writePacketBuffer(packetWriter.getBuffer());
        return this;
    }

    public final PacketSender sendInterfacePosition(int interfaceId, int value2, int value32) {
        if (this.player.isBot) {
            return this;
        }
        if (ServerSettings.clientBuild == 443) {
            interfaceId = this.translatePositionComponent(interfaceId, "x=" + value2 + " y=" + value32);
            if (interfaceId == InterfaceBridge.UNMAPPED) return this;
            PacketWriter packetWriter = PacketBuffer.allocateWriter(9);
            packetWriter.writeOpcode(this.player.getOutboundCipher(), 232);
            // Revision 443 reads signed Y (big endian, low byte +128), then signed X.
            packetWriter.writeShort(value32, ByteTransform.ADD);
            packetWriter.writeShort(value2);
            packetWriter.writeInt(interfaceId, ByteOrder.INVERSE_MIDDLE);
            this.player.writePacketBuffer(packetWriter.getBuffer());
            return this;
        }
        if (interfaceId >= InterfaceDefinition.interfaceCount && interfaceId == 12145) {
            interfaceId = 8923;
        }
        if (interfaceId >= InterfaceDefinition.interfaceCount) {
            return this;
        }
        PacketWriter packetWriter = PacketBuffer.allocateWriter(7);
        packetWriter.writeOpcode(this.player.getOutboundCipher(), 70);
        packetWriter.writeShort(value2);
        packetWriter.writeShort(value32, ByteOrder.LITTLE);
        packetWriter.writeShort(interfaceId, ByteOrder.LITTLE);
        this.player.writePacketBuffer(packetWriter.getBuffer());
        return this;
    }

    public final void refreshSpecialAttackConfig() {
        if (this.player.isSpecialAttackEnabled()) {
            this.sendConfig(301, 1);
            return;
        }
        this.sendConfig(301, 0);
    }

    public final void refreshSpecialEnergyBar(int value5) {
        // Classic combat tabs read special energy from varp 300 on LostCity's 0-1000 scale.
        if (!this.player.isBot && this.player.classicSidebar) {
            VarpPacket.send(this.player, 300, this.player.getSpecialEnergy() * 10);
        }
        int value2 = 10;
        int specialEnergy = this.player.getSpecialEnergy() / 10;
        int index = 0;
        while (index < 10) {
            PacketSender packetSender;
            int value3 = --value5;
            int value4 = specialEnergy >= value2 ? 500 : 0;
            PacketSender packetSender2 = this;
            if (packetSender2.player.isBot) {
                packetSender = packetSender2;
            } else if (ServerSettings.clientBuild == 443) {
                packetSender = packetSender2.sendInterfacePosition(value3, value4, 0);
            } else {
                PacketWriter packetWriter = PacketBuffer.allocateWriter(7);
                packetWriter.writeOpcode(packetSender2.player.getOutboundCipher(), 70);
                packetWriter.writeShort(value4);
                packetWriter.writeShort(0, ByteOrder.LITTLE);
                packetWriter.writeShort(value3, ByteOrder.LITTLE);
                packetSender2.player.writePacketBuffer(packetWriter.getBuffer());
                packetSender = packetSender2;
            }
            --value2;
            ++index;
        }
    }

    public final void sendCameraPosition(int value6, int value22, int value32, int value42, int value52) {
        if (this.player.isBot) {
            return;
        }
        if (ServerSettings.clientBuild == 443) {
            PacketWriter packetWriter = PacketBuffer.allocateWriter(7);
            packetWriter.writeOpcode(this.player.getOutboundCipher(), 111);
            packetWriter.writeByte(value6 / 64);
            packetWriter.writeByte(value22 / 64);
            packetWriter.writeShort(value32);
            packetWriter.writeByte(value42);
            packetWriter.writeByte(value52);
            this.player.writePacketBuffer(packetWriter.getBuffer());
            return;
        }
        PacketWriter packetWriter = PacketBuffer.allocateWriter(7);
        packetWriter.writeOpcode(this.player.getOutboundCipher(), 177);
        packetWriter.writeByte(value6 / 64);
        packetWriter.writeByte(value22 / 64);
        packetWriter.writeShort(value32);
        packetWriter.writeByte(0);
        packetWriter.writeByte(value52);
        this.player.writePacketBuffer(packetWriter.getBuffer());
    }

    public final void sendCameraLookAt(int value6, int value22, int value32, int value42, int value52) {
        if (this.player.isBot) {
            return;
        }
        if (ServerSettings.clientBuild == 443) {
            PacketWriter packetWriter = PacketBuffer.allocateWriter(7);
            packetWriter.writeOpcode(this.player.getOutboundCipher(), 241);
            packetWriter.writeByte(value6 / 64);
            packetWriter.writeByte(value22 / 64);
            packetWriter.writeShort(value32);
            packetWriter.writeByte(value42);
            packetWriter.writeByte(value52);
            this.player.writePacketBuffer(packetWriter.getBuffer());
            return;
        }
        PacketWriter packetWriter = PacketBuffer.allocateWriter(7);
        packetWriter.writeOpcode(this.player.getOutboundCipher(), 166);
        packetWriter.writeByte(value6 / 64);
        packetWriter.writeByte(value22 / 64);
        packetWriter.writeShort(value32);
        packetWriter.writeByte(0);
        packetWriter.writeByte(value52);
        this.player.writePacketBuffer(packetWriter.getBuffer());
    }

    public final void resetCamera() {
        if (this.player.isBot) {
            return;
        }
        PacketWriter packetWriter = PacketBuffer.allocateWriter(1);
        packetWriter.writeOpcode(this.player.getOutboundCipher(), ServerSettings.clientBuild == 443 ? 242 : 107);
        this.player.writePacketBuffer(packetWriter.getBuffer());
        this.player.getUpdateState().setUpdateRequired(true);
    }

    public final void openDoubleDoorPair(int value7, int value22, int value32, int value42, int value52, int value62) {
        int position = this.player.getPosition().getPlane();
        LoadedWorldObject loadedWorldObject = WorldObjectLookup.findObjectByIdAt(value7, value22, value32, position);
        LoadedWorldObject loadedWorldObject2 = WorldObjectLookup.findObjectByIdAt(value42, value52, value62, position);
        boolean enabled = DoubleDoorHandler.usesWideDoubleDoorOffset(value7);
        if (loadedWorldObject.getOrientation() == 0) {
            LoadedWorldObject loadedWorldObject3;
            LoadedWorldObject loadedWorldObject4;
            if (value32 < value62) {
                loadedWorldObject4 = loadedWorldObject;
                loadedWorldObject3 = loadedWorldObject2;
            } else {
                loadedWorldObject4 = loadedWorldObject2;
                loadedWorldObject3 = loadedWorldObject;
            }
            PacketSender.repositionDoubleDoorLeaf(loadedWorldObject4, true, enabled);
            PacketSender.repositionDoubleDoorLeaf(loadedWorldObject3, false, enabled);
            return;
        }
        if (loadedWorldObject.getOrientation() == 1) {
            LoadedWorldObject loadedWorldObject5;
            LoadedWorldObject loadedWorldObject6;
            if (value22 < value52) {
                loadedWorldObject6 = loadedWorldObject;
                loadedWorldObject5 = loadedWorldObject2;
            } else {
                loadedWorldObject6 = loadedWorldObject2;
                loadedWorldObject5 = loadedWorldObject;
            }
            PacketSender.repositionDoubleDoorLeaf(loadedWorldObject6, true, enabled);
            PacketSender.repositionDoubleDoorLeaf(loadedWorldObject5, false, enabled);
            return;
        }
        if (loadedWorldObject.getOrientation() == 2) {
            LoadedWorldObject loadedWorldObject7;
            LoadedWorldObject loadedWorldObject8;
            if (value32 < value62) {
                loadedWorldObject8 = loadedWorldObject2;
                loadedWorldObject7 = loadedWorldObject;
            } else {
                loadedWorldObject8 = loadedWorldObject;
                loadedWorldObject7 = loadedWorldObject2;
            }
            PacketSender.repositionDoubleDoorLeaf(loadedWorldObject8, true, enabled);
            PacketSender.repositionDoubleDoorLeaf(loadedWorldObject7, false, enabled);
            return;
        }
        if (loadedWorldObject.getOrientation() == 3) {
            LoadedWorldObject loadedWorldObject9;
            LoadedWorldObject loadedWorldObject10;
            if (value22 < value52) {
                loadedWorldObject10 = loadedWorldObject;
                loadedWorldObject9 = loadedWorldObject2;
            } else {
                loadedWorldObject10 = loadedWorldObject2;
                loadedWorldObject9 = loadedWorldObject;
            }
            PacketSender.repositionDoubleDoorLeaf(loadedWorldObject10, true, enabled);
            PacketSender.repositionDoubleDoorLeaf(loadedWorldObject9, false, enabled);
        }
    }

    private static void repositionDoubleDoorLeaf(LoadedWorldObject loadedWorldObject, boolean firstDoor, boolean wideOffset) {
        int xOffset = 0;
        int yOffset = 0;
        int orientation = loadedWorldObject.getOrientation();
        if (orientation == 0) {
            xOffset = !firstDoor && wideOffset ? -2 : -1;
        } else if (orientation == 1) {
            if (!firstDoor && wideOffset) {
                xOffset = -1;
                yOffset = 2;
            } else {
                yOffset = 1;
            }
        } else if (orientation == 2) {
            if (!firstDoor && wideOffset) {
                xOffset = 2;
                yOffset = 1;
            } else {
                xOffset = 1;
            }
        } else if (orientation == 3) {
            if (!firstDoor && wideOffset) {
                xOffset = -1;
                yOffset = -2;
            } else {
                yOffset = -1;
            }
        }
        new DynamicObject(ServerSettings.placeholderObjectId, loadedWorldObject.getPosition().getX(), loadedWorldObject.getPosition().getY(), loadedWorldObject.getPosition().getPlane(), orientation, loadedWorldObject.getType(), loadedWorldObject.getWorldObject().getObjectId(), 1, false);
        int openedOrientation = 0;
        if (firstDoor) {
            if (orientation == 0) {
                openedOrientation = 3;
            } else if (orientation == 2) {
                openedOrientation = 1;
            }
        } else if (orientation == 0) {
            openedOrientation = wideOffset ? 3 : 1;
        } else if (orientation == 1) {
            openedOrientation = wideOffset ? 0 : 2;
        } else if (orientation == 2) {
            openedOrientation = wideOffset ? 1 : 3;
        } else if (orientation == 3) {
            openedOrientation = wideOffset ? 0 : 2;
        }
        new DynamicObject(loadedWorldObject.getWorldObject().getObjectId(), loadedWorldObject.getPosition().getX() + xOffset, loadedWorldObject.getPosition().getY() + yOffset, loadedWorldObject.getPosition().getPlane(), openedOrientation, loadedWorldObject.getType(), ServerSettings.placeholderObjectId, 1, false);
    }

    public final void openWestShiftedDoubleDoorPair(int value8, int value22, int value32, int value42, int value52, int value62, int value72) {
        LoadedWorldObject loadedWorldObject = WorldObjectLookup.findObjectByIdAt(value8, value32, value42, 0);
        LoadedWorldObject loadedWorldObject2 = WorldObjectLookup.findObjectByIdAt(value22, value52, value62, 0);
        int position = this.player.getPosition().getPlane();
        new DynamicObject(ServerSettings.placeholderObjectId, value32, value42, position, loadedWorldObject.getOrientation(), 0, value8, 3, false);
        new DynamicObject(ServerSettings.placeholderObjectId, value52, value62, position, loadedWorldObject2.getOrientation(), 0, value22, 3, false);
        new DynamicObject(value8, value32 - 1, value42, position, loadedWorldObject.getOrientation() + 3, 0, ServerSettings.placeholderObjectId, 3, false);
        new DynamicObject(value22, value52 - 1, value62, position, loadedWorldObject2.getOrientation() + 1, 0, ServerSettings.placeholderObjectId, 3, false);
        this.sendSoundEffect(318, 1, 0);
    }

    public final void openSingleDoor(int objectId, int x, int y, int plane) {
        LoadedWorldObject loadedWorldObject = WorldObjectLookup.findObjectByIdAt(objectId, x, y, plane);
        int closedOrientation = loadedWorldObject.getOrientation() & 3;
        int openOrientation = (closedOrientation + 3) & 3;
        int openX = x;
        int openY = y;

        // Keep the same counter-clockwise swing this helper always used, but
        // move the opened model around its actual hinge instead of rotating the
        // handle/free edge in place.
        if (closedOrientation == 0) {
            ++openY;
        } else if (closedOrientation == 1) {
            ++openX;
        } else if (closedOrientation == 2) {
            --openY;
        } else if (closedOrientation == 3) {
            --openX;
        }

        new DynamicObject(ServerSettings.placeholderObjectId, x, y, plane,
                closedOrientation, loadedWorldObject.getType(), objectId, 2,
                closedOrientation, x, y, false);
        new DynamicObject(objectId, openX, openY, plane, openOrientation,
                loadedWorldObject.getType(), ServerSettings.placeholderObjectId, 2,
                0, openX, openY, false);
        this.sendSoundEffect(318, 1, 0);
    }

    /**
     * Opens a scripted pass-through door using explicit faces, matching the
     * 2006Scape scripted face changes with the normal Doors hinge geometry.
     * The opened model is moved onto the neighbouring tile so the hinge stays
     * fixed and the handle/free edge swings. Static clipping is left unchanged
     * while forced movement crosses the doorway, then the closed model returns.
     */
    public final void passThroughDoor(int objectId, int x, int y, int plane,
            int openOrientation, int closedOrientation, int objectType,
            int deltaX, int deltaY) {
        int openX = x;
        int openY = y;
        int turn = (openOrientation - closedOrientation + 4) & 3;

        // A wall-door model is anchored at one end. Rotating it in the same
        // tile makes the handle/free edge behave like the hinge. Move the
        // opened model to the neighbouring tile so the hinge endpoint stays
        // fixed, using the same geometry as 2006Scape's normal Doors system.
        if (turn == 1) {
            if (closedOrientation == 0) {
                --openX;
            } else if (closedOrientation == 1) {
                ++openY;
            } else if (closedOrientation == 2) {
                ++openX;
            } else if (closedOrientation == 3) {
                --openY;
            }
        } else if (turn == 3) {
            if (closedOrientation == 0) {
                ++openY;
            } else if (closedOrientation == 1) {
                ++openX;
            } else if (closedOrientation == 2) {
                --openY;
            } else if (closedOrientation == 3) {
                --openX;
            }
        }

        // Object expiry runs before movement too; allow one walking tick per
        // tile and restore the closed model only after the crossing finishes.
        int openTicks = Math.max(2, Math.max(Math.abs(deltaX), Math.abs(deltaY)) + 1);
        new DynamicObject(ServerSettings.placeholderObjectId, x, y, plane,
                closedOrientation, objectType, objectId, openTicks,
                closedOrientation, x, y, false);
        new DynamicObject(objectId, openX, openY, plane, openOrientation,
                objectType, ServerSettings.placeholderObjectId, openTicks,
                0, openX, openY, false);
        this.queueRelativeMovementStep(deltaX, deltaY, true);
        this.sendSoundEffect(318, 1, 0);
    }

    /**
     * Short-lived two-leaf version of the 2006Scape scripted gate/door
     * animation.  Both closed leaves are hidden, both opened leaves are drawn
     * at explicit coordinates/faces, and normal static clipping is left alone;
     * the forced tutorial step is the only movement that bypasses it.
     */
    public final void passThroughDoubleDoor(
            int objectId1, int closedX1, int closedY1, int closedOrientation1,
            int objectId2, int closedX2, int closedY2, int closedOrientation2,
            int openX1, int openY1, int openOrientation1,
            int openX2, int openY2, int openOrientation2,
            int plane, int deltaX, int deltaY, int ticks) {
        new DynamicObject(ServerSettings.placeholderObjectId,
                closedX1, closedY1, plane, closedOrientation1, 0,
                objectId1, ticks, false);
        new DynamicObject(ServerSettings.placeholderObjectId,
                closedX2, closedY2, plane, closedOrientation2, 0,
                objectId2, ticks, false);
        new DynamicObject(objectId1, openX1, openY1, plane,
                openOrientation1, 0, ServerSettings.placeholderObjectId,
                ticks, false);
        new DynamicObject(objectId2, openX2, openY2, plane,
                openOrientation2, 0, ServerSettings.placeholderObjectId,
                ticks, false);
        this.queueRelativeMovementStep(deltaX, deltaY, true);
        this.sendSoundEffect(318, 1, 0);
    }

    public final void openSouthShiftedSingleDoor(int value5, int value22, int value32, int value42) {
        LoadedWorldObject loadedWorldObject = WorldObjectLookup.findObjectByIdAt(value5, value22, value32, value42);
        new DynamicObject(ServerSettings.placeholderObjectId, value22, value32, value42, loadedWorldObject.getOrientation() - 1, loadedWorldObject.getType(), value5, 1, loadedWorldObject.getOrientation(), value22, value32, false);
        new DynamicObject(value5, value22, value32 - 1, value42, loadedWorldObject.getOrientation() - 1, loadedWorldObject.getType(), ServerSettings.placeholderObjectId, 1, loadedWorldObject.getOrientation(), value22, value32, false);
        this.sendSoundEffect(318, 1, 0);
    }

    public final void openDoubleDoorPair(int value8, int value22, int value32, int value42, int value52, int value62, int value72) {
        LoadedWorldObject loadedWorldObject = WorldObjectLookup.findObjectByIdAt(value8, value32, value42, 0);
        LoadedWorldObject loadedWorldObject2 = WorldObjectLookup.findObjectByIdAt(value22, value52, value62, 0);
        new DynamicObject(value8, value32, value42, 0, loadedWorldObject.getOrientation() - 1, 0, value8, 1, loadedWorldObject.getOrientation(), value32, value42, false);
        new DynamicObject(value22, value52, value62, 0, loadedWorldObject2.getOrientation() + 1, 0, value22, 1, loadedWorldObject2.getOrientation(), value52, value62, false);
        this.sendSoundEffect(318, 1, 0);
    }

    public final void openNorthShiftedDoubleDoorPair(int value8, int value22, int value32, int value42, int value52, int value62, int value72) {
        LoadedWorldObject loadedWorldObject = WorldObjectLookup.findObjectByIdAt(value8, value32, value42, 0);
        LoadedWorldObject loadedWorldObject2 = WorldObjectLookup.findObjectByIdAt(value22, value52, value62, 0);
        new DynamicObject(ServerSettings.placeholderObjectId, value32, value42, 0, loadedWorldObject.getOrientation() - 1, 0, value8, 1, loadedWorldObject.getOrientation(), value32, value42, false);
        new DynamicObject(ServerSettings.placeholderObjectId, value52, value62, 0, loadedWorldObject2.getOrientation() + 1, 0, value22, 1, loadedWorldObject2.getOrientation(), value52, value62, false);
        new DynamicObject(value8, value32, value42 + 1, 0, loadedWorldObject.getOrientation() - 1, 0, ServerSettings.placeholderObjectId, 1, loadedWorldObject.getOrientation(), value32, value42, false);
        new DynamicObject(value22, value52, value62 + 1, 0, loadedWorldObject2.getOrientation() + 1, 0, ServerSettings.placeholderObjectId, 1, loadedWorldObject2.getOrientation(), value52, value62, false);
        this.sendSoundEffect(318, 1, 0);
    }

    static Player getPlayer(PacketSender packetSender) {
        return packetSender.player;
    }
}
