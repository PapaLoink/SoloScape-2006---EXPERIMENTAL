package com.rs2.net.packet.handler;

import com.rs2.model.player.EmoteUnlockManager;
import com.rs2.model.player.Player;
import com.rs2.model.dialogue.DialogueManager;
import com.rs2.model.player.BankManager;
import com.rs2.net.packet.ByteOrder;
import com.rs2.net.packet.ByteTransform;
import com.rs2.net.packet.IncomingPacket;
import com.rs2.net.packet.PacketHandler;
import com.rs2.net.packet.ClientPackets;
import com.rs2.net.packet.InterfaceBridge;
import com.rs2.model.item.ItemStack;
import com.rs2.model.skill.magic.MagicSpellAction;
import com.rs2.model.skill.magic.SpellDefinition;
import com.rs2.model.skill.magic.Spellbook;
import com.rs2.net.packet.SpellWidgets;
import com.rs2.util.GameplayTrace;
import com.rs2.model.music.MusicManager;

/**
 * Decodes revision 443 widget actions without feeding packed 443 widget ids into
 * the legacy 377 button-id dispatcher.
 */
public final class InterfaceActionPacketHandler implements PacketHandler {
    private final ButtonClickPacketHandler buttonHandler = new ButtonClickPacketHandler();

    private boolean handleMusicTrack(Player player, int group, int child, int operation, int parameter) {
        if (group != 239 || (operation != -1 && parameter != 0)
                || !player.isInterfaceIdOpen(4439)) {
            return false;
        }
        if (child == 180) { // Native Auto control.
            MusicManager.setAutomaticMode(player, true);
            new MusicManager().updateForPlayerPosition(player);
            return true;
        }
        if (child == 181) { // Native Manual control.
            MusicManager.setAutomaticMode(player, false);
            return true;
        }
        if (child == 251) { // Native Loop toggle: the client loops songs while config 19 is 1.
            player.configStates[19] = player.configStates[19] == 1 ? 0 : 1;
            // Native 443 varp: sendConfig drops ids outside its verified legacy set.
            com.rs2.net.packet.VarpPacket.send(player, 19, player.configStates[19]);
            return true;
        }
        return com.rs2.model.music.Music.play(player, child);
    }

    private boolean handleAdditionalSkillGuideButton(Player player, int packedWidgetId,
                                                     int operation, int parameter) {
        if ((packedWidgetId >>> 16) != 320
                || (operation != -1 && parameter != 0)) {
            return false;
        }

        int child = packedWidgetId & 0xFFFF;
        // Native skill-tab child 148 is Construction; 149 is Hunter.
        if (child == 148) {
            player.getSkillGuideManager().selectedSkillIndex = 22;
            player.getSkillGuideManager().showConstructionGuide(1);
            return true;
        }
        if (child == 149) {
            player.getSkillGuideManager().selectedSkillIndex = 21;
            player.getSkillGuideManager().showHunterGuide(1);
            return true;
        }
        return false;
    }

    private boolean handleSkillGuideCategory(Player player, int packedWidgetId,
                                             int operation, int parameter) {
        if ((packedWidgetId >>> 16) != 308) {
            return false;
        }

        int child = packedWidgetId & 0xFFFF;

        // Normal button packet is operation == -1. Keep parameter == 0
        // compatible with interface-operation packets too.
        if (operation != -1 && parameter != 0) {
            return false;
        }

        int category;
        switch (child) {
            case 131: category = 1; break;
            case 108: category = 2; break;
            case 109: category = 3; break;
            case 112: category = 4; break;
            case 122: category = 5; break;
            case 125: category = 6; break;
            case 128: category = 7; break;
            case 143: category = 8; break;
            case 146: category = 9; break;
            case 149: category = 10; break;
            case 159: category = 11; break;
            case 162: category = 12; break;
            case 165: category = 13; break;
            default: return false;
        }

        System.out.println("[SKILL GUIDE] category click group=" + (packedWidgetId >>> 16)
                + " child=" + child + " category=" + category);
        player.getSkillGuideManager().showSelectedSkillCategory(category);
        return true;
    }

    private boolean handleTutorialLogsContinue(Player player) {
        int tutorialStage = player.getQuestState(0);
        if (!player.getInventoryManager().containsItem(1511)) {
            return false;
        }
        if (tutorialStage == 8) {
            player.packetSender.sendEntityHintIcon(1, -1);
            player.advanceTutorialStage();
            return true;
        }
        if (tutorialStage == 9) {
            player.getQuestManager().refreshQuestJournal();
            return true;
        }
        return false;
    }

    private boolean handleDialogueContinue(Player player, int packedWidgetId) {
        int group = packedWidgetId >>> 16;
        int child = packedWidgetId & 0xFFFF;
        // Native group 214 is also reused for Tutorial Island's persistent
        // instruction panel (legacy 6179). Its stock Continue hitbox must not
        // behave like a real dialogue button while that overlay is active.
        if (group == 214 && player.getOpenInterfaceId() == 6179) {
            return true;
        }
        int expectedChild;
        if (group >= 64 && group <= 67) {
            expectedChild = group - 61; // player dialogue: children 3..6
        } else if (group >= 241 && group <= 244) {
            expectedChild = group - 238; // NPC dialogue: children 3..6
        } else if (group == 210) {
            expectedChild = 1; // one-line statement Continue (native 443)
        } else if (group >= 211 && group <= 214) {
            expectedChild = group - 209; // statements: children 2..5
        } else if (group == 102) {
            expectedChild = 3; // two-item hand-off dialogue
        } else if (group == 249) {
            expectedChild = 2; // single-item hand-off dialogue
        } else {
            return false;
        }
        if (child != expectedChild) {
            return false;
        }


        DialogueManager dialogue = player.getDialogueManager();
        if (dialogue.isDialogueInactive()) {
            if (group != InterfaceBridge.translateGroup(player.getOpenInterfaceId())) {
                return false;
            }
            // Standalone statements (including level-ups) have no active
            // dialogue. Use the same continuation path as legacy opcode 40.
            if (!dialogue.continueTutorialStatement()) {
                InterfaceInputPacketHandler.continueLevelUp(player);
            }
            return true;
        }
        int nextStep = dialogue.getDialogueStep() + 1;
        if (dialogue.getDialogueType() == 1) {
            DialogueManager.continueContextDialogue(dialogue.getDialogueContextId(), player,
                    dialogue.getDialogueId(), nextStep, 0,
                    dialogue.getDialogueContextX(), dialogue.getDialogueContextY());
        } else if (dialogue.getDialogueType() == 0) {
            DialogueManager.continueDialogue(player, dialogue.getDialogueId(), nextStep, 0);
        } else {
            return false;
        }
        return true;
    }

    private boolean handleDialogueOptionSelect(Player player, int packedWidgetId) {
        int legacyButtonId = InterfaceBridge.toLegacyComponent(packedWidgetId);
        boolean dialogueOption = legacyButtonId >= 2461 && legacyButtonId <= 2462
                || legacyButtonId >= 2471 && legacyButtonId <= 2473
                || legacyButtonId >= 2482 && legacyButtonId <= 2485
                || legacyButtonId >= 2494 && legacyButtonId <= 2498;
        if (!dialogueOption) {
            return false;
        }
        // Dialogue options are already fully decoded here. Send them straight
        // to the dialogue state machine instead of running through the global
        // button dispatcher first. That dispatcher checks unrelated systems
        // (including gnome gliders) before dialogue options, which can block or
        // even crash a Tutorial Island Yes/No choice before it reaches the
        // RuneScape Guide dialogue.
        player.getDialogueManager().handleOptionButton(legacyButtonId);
        return true;
    }

    @Override
    public void handle(Player player, IncomingPacket packet) {
        int opcode = packet.getOpcode();
        if (opcode == ClientPackets.WIDGET_DRAG_DROP) {
            int targetChild = packet.getReader().readSignedShort(ByteTransform.ADD, ByteOrder.LITTLE) & 0xFFFF;
            int sourceChild = packet.getReader().readSignedShort(ByteTransform.ADD, ByteOrder.LITTLE) & 0xFFFF;
            int targetWidgetId = ClientPackets.readIntLittle(packet.getReader());
            int sourceWidgetId = ClientPackets.readIntInverseMiddle(packet.getReader());
            if (GameplayTrace.enabled()) {
                GameplayTrace.log("443 widget-drag-drop player=" + GameplayTrace.describe(player)
                        + " sourceWidget=" + sourceWidgetId + " sourceChild=" + sourceChild
                        + " targetWidget=" + targetWidgetId + " targetChild=" + targetChild);
            }
            if (player.isInteractionDebugEnabled()) {
                player.packetSender.sendGameMessage("443 widget drag/drop: " + sourceWidgetId + ":" + sourceChild
                        + " -> " + targetWidgetId + ":" + targetChild);
            }
            return;
        }
        if (opcode == ClientPackets.SPELL_ON_WIDGET) {
            int spellWidgetId = packet.getReader().readInt();
            int targetWidgetId = packet.getReader().readInt();
            int spellChild = packet.getReader().readSignedShort(ByteTransform.ADD) & 0xFFFF;
            int targetParameter = packet.getReader().readSignedShort(ByteTransform.ADD, ByteOrder.LITTLE) & 0xFFFF;
            if (GameplayTrace.enabled()) {
                GameplayTrace.log("443 spell-on-widget player=" + GameplayTrace.describe(player)
                        + " spellWidget=" + spellWidgetId + " spellChild=" + spellChild
                        + " targetWidget=" + targetWidgetId + " targetParameter=" + targetParameter);
            }
            if (player.isInteractionDebugEnabled()) {
                player.packetSender.sendGameMessage("443 spell-on-widget: spell=" + spellWidgetId
                        + ":" + spellChild + " target=" + targetWidgetId + ":" + targetParameter);
            }
            if (SpellWidgets.isSpellWidget(spellWidgetId)
                    && InterfaceBridge.toLegacyComponent(targetWidgetId) == 3214
                    && targetParameter < 28 && player.isInterfaceIdOpen(3214)) {
                int legacySpellButton = SpellWidgets.toLegacySpellButton(spellWidgetId, spellChild);
                SpellDefinition spell = Spellbook.getSpellForButtonId(player, legacySpellButton);
                ItemStack item = player.getInventoryManager().getContainer().getItemAt(targetParameter);
                if (spell != null && item != null && item.isValid()) {
                    MagicSpellAction.castItemSpell(player, spell, item.getId(), targetParameter);
                }
            }
            return;
        }
        if (opcode == ClientPackets.WIDGET_SELECT) {
            int child = packet.getReader().readSignedShort(ByteOrder.LITTLE) & 0xFFFF;
            int packedWidgetId = packet.getReader().readInt();
            if (GameplayTrace.enabled()) {
                GameplayTrace.log("443 widget-select player=" + GameplayTrace.describe(player)
                        + " widget=" + packedWidgetId + " group=" + (packedWidgetId >>> 16)
                        + " child=" + child);
            }
            if (player.isInteractionDebugEnabled()) {
                player.packetSender.sendGameMessage("443 widget select: widget=" + packedWidgetId
                        + " child=" + child);
            }
            // Skill-guide categories also use native action-type-6 controls.
            // Refreshing the guide resets the client's Continue wait state.
            if (handleSkillGuideCategory(player, packedWidgetId, -1, child)) {
                return;
            }
            // Group 307's four Cook buttons are action-type-6 widgets and
            // therefore send WIDGET_SELECT (153), not INTERFACE_BUTTON (54).
            // Ignoring opcode 153 leaves the client's action text at
            // "Please wait..." without scheduling a cooking animation.
            // Restrict the route to the open cooking quantity chatbox.
            if ((packedWidgetId >>> 16) == 307
                    && player.getOpenInterfaceId() == 1743) {
                int cookingChild = packedWidgetId & 0xffff;
                if (cookingChild >= 3 && cookingChild <= 6) {
                    int cookingButton = InterfaceBridge.toLegacyComponent(packedWidgetId);
                    if (cookingButton != InterfaceBridge.UNMAPPED) {
                        buttonHandler.handleButton(player, cookingButton);
                    }
                    return;
                }
            }
            // Native 443 dialogue controls use WIDGET_SELECT rather than the
            // ordinary interface-action packet. Continue buttons and option
            // choices must both be routed into the legacy dialogue state machine
            // so the next chatbox replaces the client's local "Please wait..." text.
            if ((packedWidgetId >>> 16) == 249 && handleTutorialLogsContinue(player)) {
                return;
            }
            if (handleDialogueContinue(player, packedWidgetId)) {
                return;
            }
            handleDialogueOptionSelect(player, packedWidgetId);
            return;
        }
        if (opcode == ClientPackets.WIDGET_ITEM_DRAG) {
            int sourceSlot = packet.getReader().readSignedShort() & 0xFFFF;
            int packedWidgetId = packet.getReader().readInt();
            int insertMode = packet.getReader().readUnsignedByte(false);
            int targetSlot = packet.getReader().readSignedShort() & 0xFFFF;
            if (GameplayTrace.enabled()) {
                GameplayTrace.log("443 widget-item-drag player=" + GameplayTrace.describe(player)
                        + " widget=" + packedWidgetId + " group=" + (packedWidgetId >>> 16)
                        + " child=" + (packedWidgetId & 0xFFFF) + " sourceSlot=" + sourceSlot
                        + " targetSlot=" + targetSlot + " insertMode=" + insertMode);
            }
            if (player.isInteractionDebugEnabled()) {
                player.packetSender.sendGameMessage("443 widget drag: widget=" + packedWidgetId
                        + " " + sourceSlot + "->" + targetSlot + " mode=" + insertMode);
            }
            int legacyWidgetId = InterfaceBridge.toLegacyComponent(packedWidgetId);
            if (!player.isActionLocked() && legacyWidgetId == 3214
                    && player.isInterfaceIdOpen(3214)
                    && sourceSlot < 28 && targetSlot < 28) {
                ItemStack item = player.getInventoryManager().getContainer().getItemAt(sourceSlot);
                if (item != null && player.getInventoryManager().containsItemStack(item)) {
                    player.getInventoryManager().swapSlots(sourceSlot, targetSlot);
                    player.getInventoryManager().refresh();
                }
            } else if (!player.isActionLocked() && legacyWidgetId == 5382
                    && player.getOpenInterfaceId() == 5292) {
                BankManager.rearrangeRevision443BankItem(player, sourceSlot, targetSlot);
            }
            return;
        }
        int packedWidgetId = packet.getReader().readInt();
        int operation = ClientPackets.getInterfaceOperation(opcode);
        int parameter = -1;
        if (operation != -1) {
            parameter = packet.getReader().readSignedShort();
        }

        int widgetGroup = packedWidgetId >>> 16;
        int widgetChild = packedWidgetId & 0xffff;
        // Classic (2004) combat tabs: handle each click as the native 443 combat tab widget it stands for.
        if (com.rs2.net.packet.ClassicCombat.isClassicGroup(widgetGroup)) {
            int legacyComponent = com.rs2.net.packet.ClassicCombat.legacyComponent(packedWidgetId);
            if (com.rs2.net.packet.ClassicSidebar.handleCombat(player, legacyComponent)) return;
            int nativeWidget = legacyComponent == -1 ? InterfaceBridge.UNMAPPED : InterfaceBridge.translate(legacyComponent);
            if (nativeWidget == InterfaceBridge.UNMAPPED) return;
            packedWidgetId = nativeWidget;
            widgetGroup = nativeWidget >>> 16;
            widgetChild = nativeWidget & 0xffff;
        }
        // Classic (2004) sidebar tabs: settings use the legacy actions, emotes the native emote tab below.
        if ((widgetGroup == com.rs2.net.packet.ClassicSidebar.OPTIONS_GROUP
                || widgetGroup == com.rs2.net.packet.ClassicSidebar.CONTROLS_GROUP)
                && (operation == -1 || parameter == 0)) {
            if (com.rs2.net.packet.ClassicSidebar.handleSetting(player, widgetGroup, widgetChild)) {
                return;
            }
            int emote = com.rs2.net.packet.ClassicSidebar.nativeEmoteChild(widgetGroup, widgetChild);
            if (emote == -1) return;
            widgetGroup = 464;
            widgetChild = emote;
        }
        if (widgetGroup == 271 && player.isInterfaceIdOpen(5609)
                && (operation == -1 || parameter == 0)
                && player.getPrayerManager().handleAdditionalPrayerClick(widgetChild)) {
            return;
        }
        // Revision 443 uses one toggle on each combat tab, whereas the legacy
        // dispatcher has separate On/Off buttons (150/151).
        if (isAutoRetaliateButton(widgetGroup, widgetChild)
                && (operation == -1 || parameter == 0)) {
            player.setAutoRetaliate(!player.isAutoRetaliate());
            return;
        }
        if (widgetGroup == 387 && widgetChild == 24
                && (operation == -1 || parameter == 0)) {
            if (player.getQuestState(0) == 42) {
                player.setTutorialEquipmentStatsOpened();
            }
            player.getEquipmentManager().refresh();
            player.packetSender.sendItemContainer(15107,
                    player.getEquipmentManager().getContainer().getRawItems());
            if (player.getQuestState(0) != 1) {
                player.getQuestManager().refreshQuestJournal();
            }
            // Open the viewport after the tutorial chatbox refresh so it stays visible.
            player.packetSender.selectSidebarTab(3);
            player.packetSender.showInterface(15106);
            return;
        }
        if (handleMusicTrack(player, widgetGroup, widgetChild, operation, parameter)) {
            return;
        }
        if (widgetGroup == 464 && widgetChild >= 1 && widgetChild <= 37
                && (operation == -1 || parameter == 0)) {
            if (EmoteUnlockManager.isLockedChild(widgetChild)
                    && !EmoteUnlockManager.isUnlocked(player, widgetChild)) {
                player.packetSender.sendGameMessage(EmoteUnlockManager.getUnlockHint(widgetChild));
                return;
            }
            if (widgetChild == 37) { // Skill Cape
                int capeId = player.getEquipmentManager().getItemIdAtSlot(1);
                int[] skillCapeAnimations = {
                    4959, 4981, 4961, 4973, 4979, 4939, 4947, 4971, 4977, 4969, 4965,
                    4949, 4937, 4967, 4953, 4941, 4943, 4951, 4955, 4975, 4957, 4963
                };
                int[] skillCapeGraphics = {
                    823, 828, 824, 832, 829, 813, 817, 833, 830, 835, 826,
                    818, 812, 827, 820, 814, 815, 819, 821, 831, 822, 825
                };
                int skillCapeIndex = -1;
                if (capeId >= 9747 && capeId <= 9811) {
                    int capeOffset = capeId - 9747;
                    if (capeOffset % 3 < 2) {
                        skillCapeIndex = capeOffset / 3;
                    }
                }
                int animationId = -1;
                int graphicId = -1;
                if (skillCapeIndex >= 0 && skillCapeIndex < skillCapeAnimations.length) {
                    animationId = skillCapeAnimations[skillCapeIndex];
                    graphicId = skillCapeGraphics[skillCapeIndex];
                } else if (capeId == 9813) {
                    animationId = 4945;
                    graphicId = 816;
                } else if (capeId == 9948 || capeId == 9949) {
                    animationId = 5158;
                    graphicId = 907;
                }
                if (animationId == -1) {
                    player.packetSender.sendGameMessage("You need to wear a skillcape to perform this emote.");
                } else {
                    player.getUpdateState().setAnimation(animationId, 0);
                    player.getUpdateState().setGraphic(graphicId);
                }
                return;
            }

            // Children 1-36 in cache order: Yes ... Glass Wall, Slap Head, Stamp, Flap, Idea,
            // Zombie Walk, Zombie Dance, Scared, Rabbit Hop (37 is Skill Cape, above).
            int[] emoteAnimations = {
                855, 856, 858, 859, 857, 863, 2113, 862, 864, 861, 2109, 2111,
                866, 2106, 2107, 2108, 860, 1368, 2105, 2110, 865, 2112,
                2127, 2128, 1131, 1130, 1129, 1128, 4275, 4278, 4280, 4276,
                3544, 3543, 2836, 6111
            };
            int animationId = emoteAnimations[widgetChild - 1];
            player.getUpdateState().setAnimation(animationId, 0);
            if (widgetChild == 18) {
                player.getUpdateState().setGraphic(574);
            } else if (widgetChild == 32) { // Idea
                player.getUpdateState().setGraphic(712);
            }
            if (player.getQuestState(0) == 23 && !player.isTutorialEmoteCompleted()) {
                player.setTutorialEmoteCompleted();
                player.getQuestManager().refreshQuestJournal();
            }
            if (GameplayTrace.enabled()) {
                GameplayTrace.log("443 emote click player=" + GameplayTrace.describe(player)
                        + " child=" + widgetChild + " animation=" + animationId);
            }
            return;
        }

        if ((packedWidgetId >>> 16) == 249 && handleTutorialLogsContinue(player)) {
            return;
        }

        if (handleAdditionalSkillGuideButton(player, packedWidgetId, operation, parameter)
                || handleSkillGuideCategory(player, packedWidgetId, operation, parameter)) {
            return;
        }

        if (GameplayTrace.enabled()) {
            GameplayTrace.log("443 interface action player=" + GameplayTrace.describe(player)
                    + " opcode=" + opcode
                    + " operation=" + operation
                    + " widget=" + packedWidgetId
                    + " group=" + (packedWidgetId >>> 16)
                    + " child=" + (packedWidgetId & 0xFFFF)
                    + " parameter=" + parameter);
        }
        if (player.isInteractionDebugEnabled()) {
            String action = operation == -1 ? "button" : "op" + operation;
            player.packetSender.sendGameMessage("443 interface " + action
                    + ": widget=" + packedWidgetId + " param=" + parameter);
        }
        // The native staff tab has two Spell controls for its attack-mode
        // variants; both open the same picker in the legacy gameplay handler.
        if (packedWidgetId == (90 << 16 | 5)
                && (operation == -1 || parameter == 0)) {
            buttonHandler.handleButton(player, 353);
            return;
        }
        // Cache group 261 child 1 toggles varp 171 (0 = show effects, 1 = hide).
        if (packedWidgetId == (261 << 16 | 1)
                && (operation == -1 || parameter == 0)) {
            int setting = player.getPublicChatEffects() == 0 ? 1 : 0;
            player.setPublicChatEffects(setting);
            player.packetSender.sendConfig(171, setting);
            return;
        }
        if (handleDialogueContinue(player, packedWidgetId)) return;
        int legacyButtonId = InterfaceBridge.toLegacyComponent(packedWidgetId);

        System.out.println(
                "[SKILL DEBUG] packed=" + packedWidgetId
                        + " group=" + (packedWidgetId >>> 16)
                        + " child=" + (packedWidgetId & 0xFFFF)
                        + " legacy=" + legacyButtonId
                        + " operation=" + operation
                        + " parameter=" + parameter
        );
        if (SpellWidgets.isSpellWidget(packedWidgetId)
                && legacyButtonId != InterfaceBridge.UNMAPPED
                && (operation == -1 || parameter == 0)) {
            SpellDefinition spell = Spellbook.getSpellForButtonId(player, legacyButtonId);
            if (spell != null) {
                MagicSpellAction.castSelfSpell(player, spell);
                return;
            }
        }
        if (legacyButtonId != InterfaceBridge.UNMAPPED
                && legacyButtonId != 3214 && legacyButtonId != 1688
                && (operation == -1 || parameter == 0)) {
            buttonHandler.handleButton(player, legacyButtonId);
        } else if (legacyButtonId == InterfaceBridge.UNMAPPED) {
            GameplayTrace.logInteraction(player, "interface-action unhandled group=" + (packedWidgetId >>> 16)
                    + " child=" + (packedWidgetId & 0xffff) + " operation=" + operation
                    + " reason=no-native-handler-or-legacy-mapping");
        }
    }

    private static boolean isAutoRetaliateButton(int group, int child) {
        // Click targets verified against the revision 443 cache (actionType 4).
        switch (group) {
            case 75: case 78: case 81: case 82: case 83:
            case 87: case 88: case 89:
                return child == 26;
            case 76: case 77: case 79: case 84: case 85:
            case 91: case 92: case 93:
                return child == 24;
            case 80: return child == 8;
            case 86: return child == 12;
            case 90: return child == 9;
            default: return false;
        }
    }
}
