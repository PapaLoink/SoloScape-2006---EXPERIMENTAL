/*
 * Source recovery overlay for CFR type-flow damage.
 */
package com.rs2.net.packet.handler;

import com.rs2.ServerSettings;
import com.rs2.cache.CacheArchive;
import com.rs2.model.EntityUpdateState;
import com.rs2.model.GameplayHelper;
import com.rs2.model.World;
import com.rs2.model.bankpin.BankPinManager;
import com.rs2.model.clue.ChallengeQuestion;
import com.rs2.model.dialogue.DialogueManager;
import com.rs2.model.gameplay.partyroom.PartyRoomManager;
import com.rs2.model.item.ItemStack;
import com.rs2.model.npc.Npc;
import com.rs2.model.player.BankManager;
import com.rs2.model.player.GrandExchangeManager;
import com.rs2.model.player.Player;
import com.rs2.model.shop.ShopManager;
import com.rs2.model.skill.cooking.CookingManager;
import com.rs2.model.skill.crafting.CraftingHandler;
import com.rs2.model.skill.smithing.SmeltingHandler;
import com.rs2.net.packet.ByteOrder;
import com.rs2.net.packet.ByteTransform;
import com.rs2.net.packet.IncomingPacket;
import com.rs2.net.packet.PacketBuffer;
import com.rs2.net.packet.PacketHandler;
import com.rs2.net.packet.PacketWriter;
import com.rs2.util.GameplayTrace;

public final class InterfaceInputPacketHandler
implements PacketHandler {
    @Override
    public final void handle(Player player, IncomingPacket incomingPacket) {
        switch (incomingPacket.getOpcode()) {
            case 22: {
                long nameHash = incomingPacket.getReader().readLong();
                if (GameplayTrace.enabled()) {
                    GameplayTrace.log("443 name input player=" + GameplayTrace.describe(player)
                            + " nameHash=" + nameHash);
                }
                if (player.isInteractionDebugEnabled()) {
                    player.packetSender.sendGameMessage("443 name input: " + nameHash);
                }
                return;
            }
            case 40: {
                if (ServerSettings.debugModeEnabled) {
                    player.packetSender.sendGameMessage("dialogue: " + player.getDialogueManager().getDialogueId() + " chat: " + player.getDialogueManager().getDialogueStep());
                }
                if (player.getDialogueManager().isDialogueInactive()) {
                    if (player.getDialogueManager().continueTutorialStatement()) {
                        break;
                    }
                    continueLevelUp(player);
                    break;
                }
                if (player.getDialogueManager().getDialogueType() == 1) {
                    DialogueManager.continueContextDialogue(player.getDialogueManager().getDialogueContextId(), player, player.getDialogueManager().getDialogueId(), player.getDialogueManager().getDialogueStep() + 1, 0, player.getDialogueManager().getDialogueContextX(), player.getDialogueManager().getDialogueContextY());
                    break;
                }
                if (player.getDialogueManager().getDialogueType() == 0) {
                    DialogueManager.continueDialogue(player, player.getDialogueManager().getDialogueId(), player.getDialogueManager().getDialogueStep() + 1, 0);
                    break;
                }
                System.out.println("Unhandled dialogue type: " + player.getDialogueManager().getDialogueType());
                return;
            }
            case 135: {
                player.setSelectedInterfaceSlot(incomingPacket.getReader().readSignedShort(ByteOrder.LITTLE));
                player.setSelectedInterfaceId(incomingPacket.getReader().readSignedShort(ByteTransform.ADD));
                player.setSelectedInterfaceItemId(incomingPacket.getReader().readSignedShort(ByteOrder.LITTLE));
                if (GameplayTrace.enabled() && (player.getSelectedInterfaceId() == 3900 || player.getSelectedInterfaceId() == 3823)) {
                    GameplayTrace.log("shop item amount-hundred decoded player=" + GameplayTrace.describe(player) + " interfaceId=" + player.getSelectedInterfaceId() + " slot=" + player.getSelectedInterfaceSlot() + " itemId=" + player.getSelectedInterfaceItemId() + " amount=100 openInterfaceId=" + player.getOpenInterfaceId() + " currentShopId=" + player.getCurrentShopId());
                }
                if (player.getSelectedInterfaceId() == 3900) {
                    ShopManager.buyItem(player, player.getSelectedInterfaceSlot(), player.getSelectedInterfaceItemId(), 100);
                    break;
                }
                if (player.getSelectedInterfaceId() == 3823) {
                    ShopManager.sellItem(player, player.getSelectedInterfaceSlot(), player.getSelectedInterfaceItemId(), 100);
                    break;
                }
                PacketWriter packetWriter = PacketBuffer.allocateWriter(2);
                packetWriter.writeOpcode(player.getOutboundCipher(), 27);
                player.writePacketBuffer(packetWriter.getBuffer());
                return;
            }
            case 74:
            case 208: {
                int amount = incomingPacket.getReader().readInt();
                if (amount <= 0) {
                    break;
                }
                if (GameplayTrace.enabled() && (player.getSelectedInterfaceId() == 3900 || player.getSelectedInterfaceId() == 3823)) {
                    GameplayTrace.log("shop item amount-custom input player=" + GameplayTrace.describe(player) + " interfaceId=" + player.getSelectedInterfaceId() + " slot=" + player.getSelectedInterfaceSlot() + " itemId=" + player.getSelectedInterfaceItemId() + " amount=" + amount + " openInterfaceId=" + player.getOpenInterfaceId() + " currentShopId=" + player.getCurrentShopId());
                }
                if (player.getSelectedInterfaceId() == 5064 && player.getOpenInterfaceId() == 5292
                        || player.getSelectedInterfaceId() == 7423 && player.getOpenInterfaceId() == 4465) {
                    BankManager.depositInventoryItem(player, player.getSelectedInterfaceSlot(), player.getSelectedInterfaceItemId(), amount);
                    return;
                }
                if (player.getSelectedInterfaceId() == 2006 && player.getOpenInterfaceId() == 2156) {
                    PartyRoomManager.stageInventoryItemForChest(player, player.getSelectedInterfaceSlot(), player.getSelectedInterfaceItemId(), amount);
                    return;
                }
                if (player.getSelectedInterfaceId() == 2274 && player.getOpenInterfaceId() == 2156) {
                    PartyRoomManager.withdrawStagedChestItem(player, player.getSelectedInterfaceSlot(), player.getSelectedInterfaceItemId(), amount);
                    return;
                }
                if (ServerSettings.clientBuild == 443 && player.getSelectedInterfaceId() == 5382
                        && player.getOpenInterfaceId() == 5292) {
                    BankManager.withdrawRevision443Item(player, player.getSelectedInterfaceSlot(),
                            player.getSelectedInterfaceItemId(), amount);
                    return;
                }
                if (InterfaceInputPacketHandler.isBankTabInterface(player.getSelectedInterfaceId()) && player.getOpenInterfaceId() == 5292) {
                    BankManager.withdrawItemFromTab(player, player.getSelectedInterfaceSlot(), player.getSelectedInterfaceItemId(), amount, player.getSelectedInterfaceId());
                    return;
                }
                if (player.getSelectedInterfaceId() == 3322) {
                    if (player.interfaceAction == "duel") {
                        player.getDuelSession().addStakeItem(new ItemStack(player.getSelectedInterfaceItemId(), amount), player.getInventoryManager().getContainer().indexOfItem(player.getSelectedInterfaceItemId()));
                        break;
                    }
                    GameplayHelper.addTradeOfferItem(player, player.getSelectedInterfaceSlot(), player.getSelectedInterfaceItemId(), amount);
                    return;
                }
                if (player.getSelectedInterfaceId() == 3415) {
                    GameplayHelper.removeTradeOfferItem(player, player.getSelectedInterfaceSlot(), player.getSelectedInterfaceItemId(), amount);
                    return;
                }
                if (player.getSelectedInterfaceId() == 15682 || player.getSelectedInterfaceId() == 15683) {
                    player.getFarmingToolStore().withdrawItem(player.getSelectedItemId(), amount);
                    return;
                }
                if (player.getSelectedInterfaceId() == 15594 || player.getSelectedInterfaceId() == 15595) {
                    player.getFarmingToolStore().depositItem(player.getSelectedItemId(), amount);
                    return;
                }
                if (player.getSelectedInterfaceId() == 6669) {
                    player.getDuelSession().removeStakeItem(new ItemStack(player.getSelectedInterfaceItemId(), amount));
                    return;
                }
                if (player.getSelectedInterfaceId() == 207) {
                    InterfaceInputPacketHandler.handleChallengeQuestionInput(player, amount);
                    return;
                }
                if (player.getSelectedInterfaceId() == 13718) {
                    // The Cook-X amount applies to the one pending cooking
                    // prompt; don't let a replayed input restart cooking.
                    player.setSelectedInterfaceId(0);
                    CookingManager.startCookingTask(player, amount);
                    return;
                }
                if (SmeltingHandler.handleSmeltingButton(player, player.getSelectedInterfaceId(), amount) || CraftingHandler.handlePotteryButton(player, player.getSelectedInterfaceId(), amount) || CraftingHandler.handleSilverCraftingButton(player, player.getSelectedInterfaceId(), amount) || CraftingHandler.handleSpinningButton(player, player.getSelectedInterfaceId(), amount) || CraftingHandler.handleGlassblowingButton(player, player.getSelectedInterfaceId(), amount) || GameplayHelper.handleCraftedArmorButton(player, player.getSelectedInterfaceId(), amount) || CraftingHandler.handleWeavingButton(player, player.getSelectedInterfaceId(), amount) || CraftingHandler.handleDramenStaffButton(player, player.getSelectedInterfaceId(), amount) || EntityUpdateState.handleOrbChargeButton(player, player.getSelectedInterfaceId(), amount) || CacheArchive.handleLogCuttingButton(player, player.getSelectedInterfaceId(), amount)) {
                    break;
                }
                if (InterfaceInputPacketHandler.handleTanningInput(player, player.getSelectedInterfaceId(), amount) || SkillMenuPacketHandler.handleSetLevelInput(player, player.getSelectedInterfaceId(), amount)) {
                    break;
                }
                if (player.getSelectedInterfaceId() == 18902) {
                    int inventoryAmount = player.getInventoryManager().getItemAmount(player.temporaryActionValue);
                    if (amount > inventoryAmount || amount <= 0) {
                        amount = inventoryAmount;
                    }
                    if (amount > player.getInventoryManager().getContainer().getFreeSlots()) {
                        amount = player.getInventoryManager().getContainer().getFreeSlots();
                    }
                    int unnotedId = new ItemStack(player.temporaryActionValue, inventoryAmount).getDefinition().getUnnotedId();
                    player.getInventoryManager().removeItem(new ItemStack(player.temporaryActionValue, amount));
                    player.getInventoryManager().addItem(new ItemStack(unnotedId, amount));
                    return;
                }
                if (player.getSelectedInterfaceId() == 18900) {
                    GrandExchangeManager.setSelectedOfferQuantity(player, amount);
                    return;
                }
                if (player.getSelectedInterfaceId() == 18901) {
                    GrandExchangeManager.setSelectedOfferUnitPrice(player, amount);
                }
            }
        }
    }

    public static void continueLevelUp(Player player) {
        if (player.currentLevelUpSkillId != -1 && player.getQuestState(0) == 1 && ServerSettings.showSkillUnlocks) {
            boolean showedUnlock = BankPinManager.showSkillUnlockMessage(player, player.currentLevelUpSkillId);
            player.currentLevelUpSkillId = -1;
            if (showedUnlock) {
                return;
            }
        }
        if (player.queuedLevelUpSkillIds.size() > 0) {
            player.getSkillManager().showLevelUpInterface(((Integer)player.queuedLevelUpSkillIds.get(0)).intValue());
            player.queuedLevelUpSkillIds.remove(0);
            return;
        }
        player.packetSender.closeInterfaces();
        player.getDialogueManager().resetDialogueState();
    }

    private static boolean isBankTabInterface(int interfaceId) {
        return interfaceId == 5382 || interfaceId == 19532 || interfaceId == 19533 || interfaceId == 19534 || interfaceId == 19535 || interfaceId == 19536 || interfaceId == 19537 || interfaceId == 19538 || interfaceId == 19539 || interfaceId == 19540;
    }

    private static void handleChallengeQuestionInput(Player player, int value2) {
        ChallengeQuestion challengeQuestion = ChallengeQuestion.forClueItemId(player.activeClueItemId);
        if (challengeQuestion == null || !player.getInventoryManager().getContainer().containsItem(challengeQuestion.getAnswerItemId())) {
            return;
        }
        Npc npc = World.getNpcs()[player.getInteractionTargetIndex()];
        player.getDialogueManager().setDialogueNpcId(npc != null ? npc.getNpcId() : 0);
        if (value2 == challengeQuestion.getAnswerValue()) {
            player.getDialogueManager().setDialogueId(10009);
            player.getDialogueManager().setNextDialogueStep(2);
            player.getDialogueManager().showNpcOneLineDialogue("Thank you, this is the right answer.", 588);
            player.clueRequiredItems = new ItemStack[2];
            player.clueRequiredItems[0] = new ItemStack(challengeQuestion.getClueItemId(), 1);
            player.clueRequiredItems[1] = new ItemStack(challengeQuestion.getAnswerItemId(), 1);
            return;
        }
        player.getDialogueManager().setDialogueId(10009);
        player.getDialogueManager().setNextDialogueStep(3);
        player.getDialogueManager().showNpcOneLineDialogue("Sorry, this is the wrong answer, try again.", 588);
    }

    private static boolean handleTanningInput(Player player, int value3, int value22) {
        if (player.interfaceAction != "tanning") {
            return false;
        }
        switch (value3) {
            case 7442:
            case 8687:
            case 14801: {
                GameplayHelper.tanHide(player, value22, 1, 1739, 1741);
                return true;
            }
            case 7454:
            case 8691:
            case 14802: {
                GameplayHelper.tanHide(player, value22, 3, 1739, 1743);
                return true;
            }
            case 14803: {
                GameplayHelper.tanHide(player, value22, 20, 6287, 6289);
                return true;
            }
            case 8696:
            case 14805: {
                GameplayHelper.tanHide(player, value22, 20, 1753, 1745);
                return true;
            }
            case 8701:
            case 14806: {
                GameplayHelper.tanHide(player, value22, 20, 1751, 2505);
                return true;
            }
            case 8706:
            case 14807: {
                GameplayHelper.tanHide(player, value22, 20, 1749, 2507);
                return true;
            }
            case 8711:
            case 14808: {
                GameplayHelper.tanHide(player, value22, 20, 1747, 2509);
                return true;
            }
        }
        return false;
    }
}
