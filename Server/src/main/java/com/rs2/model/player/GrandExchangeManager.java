package com.rs2.model.player;

import com.rs2.ServerSettings;
import com.rs2.model.grandexchange.GrandExchangeCatalog;
import com.rs2.model.grandexchange.GrandExchangeOffer;
import com.rs2.model.grandexchange.GrandExchangePriceSample;
import com.rs2.model.item.ItemDefinition;
import com.rs2.model.item.ItemStack;
import com.rs2.model.player.Player;
import com.rs2.util.GameUtil;

public final class GrandExchangeManager {
    private static int instantPriceFluctuationPercent = 0;

    public static void openGrandExchange(Player player) {
        if (!ServerSettings.content2007Enabled) {
            player.packetSender.sendGameMessage("The Grand Exchange is post-2006 content. Enable it in the client settings.");
            return;
        }
        if (player.gameMode != 0) {
            player.packetSender.sendGameMessage("You are not playing on normal gamemode and cannot use grand exchange.");
            return;
        }
        Player player2 = player;
        player.selectedGrandExchangeItemId = -1;
        player2.selectedGrandExchangeQuantity = 0;
        player2.selectedGrandExchangeUnitPrice = 0;
        player2.selectedGrandExchangeSlot = 0;
        GrandExchangeManager.refreshOfferSlots(player);
        player.packetSender.showInterface(19018);
    }

    public static void openGrandExchangeCollection(Player player) {
        if (!ServerSettings.content2007Enabled) {
            player.packetSender.sendGameMessage("The Grand Exchange is post-2006 content. Enable it in the client settings.");
            return;
        }
        if (player.gameMode != 0) {
            player.packetSender.sendGameMessage("You are not playing on normal gamemode and cannot use grand exchange.");
            return;
        }
        for (int slot = 0; slot < player.grandExchangePrimaryCollectAmounts.length; ++slot) {
            if (player.grandExchangePrimaryCollectAmounts[slot] > 0 || player.grandExchangeSecondaryCollectAmounts[slot] > 0) {
                player.selectedGrandExchangeSlot = slot;
                GrandExchangeManager.handleButtonClick(player, 19042 + slot * 9);
                return;
            }
        }
        GrandExchangeManager.openGrandExchange(player);
    }

    public static void refreshSelectedOfferDetails(Player player) {
        ItemStack secondaryCollectItem;
        ItemStack primaryCollectItem;
        String text;
        double value = player.grandExchangeCompletedQuantities[player.selectedGrandExchangeSlot];
        double value2 = player.grandExchangeQuantities[player.selectedGrandExchangeSlot];
        double value3 = 100.0 * (value / value2);
        int value4 = (int)value3;
        boolean enabled = value4 == 100;
        boolean enabled2 = player.grandExchangeCancelledFlags[player.selectedGrandExchangeSlot];
        Player player2 = player;
        player2.packetSender.sendInterfaceText("for a total price of " + GameUtil.formatNumber(player.grandExchangeTotalPrices[player.selectedGrandExchangeSlot]) + " coins.", 19002);
        String text2 = enabled || enabled2 ? "" : "have";
        String text3 = text = enabled || enabled2 ? "" : " so far";
        if (player.grandExchangeSellOfferFlags[player.selectedGrandExchangeSlot]) {
            player2 = player;
            player2.packetSender.sendInterfaceText("You " + text2 + " sold a total of " + GameUtil.formatNumber(player.grandExchangeCompletedQuantities[player.selectedGrandExchangeSlot]) + text, 19001);
        } else {
            player2 = player;
            player2.packetSender.sendInterfaceText("You " + text2 + " bought a total of " + GameUtil.formatNumber(player.grandExchangeCompletedQuantities[player.selectedGrandExchangeSlot]) + text, 19001);
        }
        if (enabled2) {
            value4 = 250;
        }
        if (enabled2 || enabled) {
            player2 = player;
            player2.packetSender.setInterfaceHiddenFlag(1, 19016);
        } else {
            player2 = player;
            player2.packetSender.setInterfaceHiddenFlag(0, 19016);
        }
        player2 = player;
        player2.packetSender.sendInterfaceProgress(19011, value4);
        if (player.grandExchangeSellOfferFlags[player.selectedGrandExchangeSlot]) {
            primaryCollectItem = new ItemStack(995, player.grandExchangePrimaryCollectAmounts[player.selectedGrandExchangeSlot]);
            secondaryCollectItem = new ItemStack(player.grandExchangeItemIds[player.selectedGrandExchangeSlot], player.grandExchangeSecondaryCollectAmounts[player.selectedGrandExchangeSlot]);
        } else {
            primaryCollectItem = new ItemStack(player.grandExchangeItemIds[player.selectedGrandExchangeSlot], player.grandExchangePrimaryCollectAmounts[player.selectedGrandExchangeSlot]);
            secondaryCollectItem = new ItemStack(995, player.grandExchangeSecondaryCollectAmounts[player.selectedGrandExchangeSlot]);
        }
        if (primaryCollectItem.getAmount() <= 0) {
            primaryCollectItem = null;
        }
        if (secondaryCollectItem.getAmount() <= 0) {
            secondaryCollectItem = null;
        }
        ItemStack[] collectItems = new ItemStack[]{primaryCollectItem, secondaryCollectItem};
        player2 = player;
        player2.packetSender.sendItemContainer(19006, collectItems);
    }

    public static void collectOfferItem(Player player, int itemId, int value8, int value32) {
        ItemStack itemStack;
        int value2;
        int value4;
        double value5 = player.grandExchangeCompletedQuantities[player.selectedGrandExchangeSlot];
        double value6 = player.grandExchangeQuantities[player.selectedGrandExchangeSlot];
        double value7 = 100.0 * (value5 / value6);
        value32 = (int)value7;
        ItemStack itemStack2 = new ItemStack(value8);
        value32 = value32 == 100 ? 1 : 0;
        boolean enabled = player.grandExchangeCancelledFlags[player.selectedGrandExchangeSlot];
        if (itemId == 0) {
            value4 = player.grandExchangeSellOfferFlags[player.selectedGrandExchangeSlot] ? 995 : player.grandExchangeItemIds[player.selectedGrandExchangeSlot];
            value2 = player.grandExchangePrimaryCollectAmounts[player.selectedGrandExchangeSlot];
        } else {
            value4 = player.grandExchangeSellOfferFlags[player.selectedGrandExchangeSlot] ? player.grandExchangeItemIds[player.selectedGrandExchangeSlot] : 995;
            value2 = player.grandExchangeSecondaryCollectAmounts[player.selectedGrandExchangeSlot];
        }
        if (value8 < 0 || value4 != itemStack2.getId() || !itemStack2.isValid()) {
            return;
        }
        if (value8 > 11883) {
            Player player2 = player;
            player2.packetSender.sendGameMessage("This item is not supported yet.");
            return;
        }
        if (value2 <= 0) {
            return;
        }
        ItemDefinition collectDefinition = itemStack2.getDefinition();
        if (!collectDefinition.isStackable()
                && value2 > player.getInventoryManager().getContainer().getFreeSlots()) {
            if (collectDefinition.hasNote()) {
                // Original GE behavior: use notes only when the full quantity cannot fit.
                value8 = collectDefinition.getNotedId();
            } else {
                value2 = player.getInventoryManager().getContainer().getFreeSlots();
                if (value2 == 0) {
                    player.getInventoryManager().canAddItem(itemStack2);
                    return;
                }
            }
        }
        ItemStack itemStack3 = new ItemStack(value8, value2);
        if (!player.getInventoryManager().canAddItem(itemStack3)) {
            return;
        }
        if (itemId == 0) {
            player.grandExchangePrimaryCollectAmounts[player.selectedGrandExchangeSlot] -= value2;
        } else {
            player.grandExchangeSecondaryCollectAmounts[player.selectedGrandExchangeSlot] -= value2;
        }
        if (player.grandExchangeSellOfferFlags[player.selectedGrandExchangeSlot]) {
            itemStack2 = new ItemStack(995, player.grandExchangePrimaryCollectAmounts[player.selectedGrandExchangeSlot]);
            itemStack = new ItemStack(player.grandExchangeItemIds[player.selectedGrandExchangeSlot], player.grandExchangeSecondaryCollectAmounts[player.selectedGrandExchangeSlot]);
        } else {
            itemStack2 = new ItemStack(player.grandExchangeItemIds[player.selectedGrandExchangeSlot], player.grandExchangePrimaryCollectAmounts[player.selectedGrandExchangeSlot]);
            itemStack = new ItemStack(995, player.grandExchangeSecondaryCollectAmounts[player.selectedGrandExchangeSlot]);
        }
        if (itemStack2.getAmount() <= 0) {
            itemStack2 = null;
        }
        if (itemStack.getAmount() <= 0) {
            itemStack = null;
        }
        ItemStack[] itemStackArray = new ItemStack[]{itemStack2, itemStack};
        Player player3 = player;
        player3.packetSender.sendItemContainer(19006, itemStackArray);
        player.getInventoryManager().addItem(itemStack3);
        if (itemStack2 == null && itemStack == null && (value32 != 0 || enabled)) {
            GrandExchangeManager.clearSelectedOfferSlot(player);
        }
    }

    public static void sendOfferCompletionMessage(Player player, int value2) {
        String definition = new ItemStack(player.grandExchangeItemIds[value2], 1).getDefinition().getName();
        String text = "buying";
        if (player.grandExchangeSellOfferFlags[value2]) {
            text = "selling";
        }
        Player player2 = player;
        player2.packetSender.sendGameMessage("Grand Exchange: Finished " + text + " " + GameUtil.formatNumber(player.grandExchangeQuantities[value2]) + " x " + definition + ".");
        player.grandExchangeFinishMessagePending[value2] = false;
    }

    private static void clearSelectedOfferSlot(Player player) {
        player.grandExchangeSellOfferFlags[player.selectedGrandExchangeSlot] = true;
        player.grandExchangeItemIds[player.selectedGrandExchangeSlot] = -1;
        player.grandExchangeQuantities[player.selectedGrandExchangeSlot] = 0;
        player.grandExchangeUnitPrices[player.selectedGrandExchangeSlot] = 0;
        player.grandExchangeCancelledFlags[player.selectedGrandExchangeSlot] = false;
        player.grandExchangeCompletedQuantities[player.selectedGrandExchangeSlot] = 0;
        player.grandExchangeTotalPrices[player.selectedGrandExchangeSlot] = 0;
        player.grandExchangePrimaryCollectAmounts[player.selectedGrandExchangeSlot] = 0;
        player.grandExchangeSecondaryCollectAmounts[player.selectedGrandExchangeSlot] = 0;
        GrandExchangeManager.openGrandExchange(player);
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    public static boolean handleButtonClick(Player player, int buttonId) {
        switch (buttonId) {
            case 19024: 
            case 19027: 
            case 19030: 
            case 19033: 
            case 19036: 
            case 19039: {
                player.selectedGrandExchangeSlot = (buttonId - 19024) / 3;
                Player player2 = player;
                player2.packetSender.showInterface(18890);
                return true;
            }
            case 19025: 
            case 19028: 
            case 19031: 
            case 19034: 
            case 19037: 
            case 19040: {
                player.selectedGrandExchangeSlot = (buttonId - 19025) / 3;
                ItemStack[] itemStackArray = player.getInventoryManager().getContainer().getRawItems();
                Player player3 = player;
                player3.packetSender.sendItemContainer(19102, itemStackArray);
                player3 = player;
                player3.packetSender.showInterfaceWithInventory(18939, 19101);
                return true;
            }
            case 18907: 
            case 18956: 
            case 18990: {
                GrandExchangeManager.openGrandExchange(player);
                return true;
            }
            case 19042: 
            case 19051: 
            case 19060: 
            case 19069: 
            case 19078: 
            case 19087: {
                Object value;
                player.selectedGrandExchangeSlot = (buttonId - 19042) / 9;
                double value2 = player.grandExchangeCompletedQuantities[player.selectedGrandExchangeSlot];
                double value3 = player.grandExchangeQuantities[player.selectedGrandExchangeSlot];
                double value4 = 100.0 * (value2 / value3);
                buttonId = (int)value4;
                boolean enabled = buttonId == 100;
                boolean enabled2 = player.grandExchangeCancelledFlags[player.selectedGrandExchangeSlot];
                player.selectedGrandExchangeItemId = player.grandExchangeItemIds[player.selectedGrandExchangeSlot];
                player.selectedGrandExchangeQuantity = player.grandExchangeQuantities[player.selectedGrandExchangeSlot];
                player.selectedGrandExchangeUnitPrice = player.grandExchangeUnitPrices[player.selectedGrandExchangeSlot];
                Player player4 = player;
                player4.packetSender.sendInterfaceText(GameUtil.formatNumber(GrandExchangeManager.getGuidePrice(player.grandExchangeItemIds[player.selectedGrandExchangeSlot])), 18997);
                player4 = player;
                // The paired client's sprite widget uses the model packet's first value as its stack count.
                player4.packetSender.sendInterfaceModel(19008,
                        ServerSettings.clientBuild == 443 ? player.selectedGrandExchangeQuantity : 100,
                        player.selectedGrandExchangeItemId);
                player4 = player;
                player4.packetSender.sendInterfaceText(GameUtil.formatNumber(player.selectedGrandExchangeQuantity), 18998);
                player4 = player;
                player4.packetSender.sendInterfaceText(String.valueOf(GameUtil.formatNumber(player.selectedGrandExchangeUnitPrice)) + " coins", 18999);
                player4 = player;
                player4.packetSender.sendInterfaceText(String.valueOf(GameUtil.formatNumber(player.selectedGrandExchangeUnitPrice * player.selectedGrandExchangeQuantity)) + " coins", 19000);
                player4 = player;
                player4.packetSender.sendInterfaceText("for a total price of " + GameUtil.formatNumber(player.grandExchangeTotalPrices[player.selectedGrandExchangeSlot]) + " coins.", 19002);
                Object value5 = enabled || enabled2 ? "" : "have";
                Object value6 = value = enabled || enabled2 ? "" : " so far";
                if (player.grandExchangeSellOfferFlags[player.selectedGrandExchangeSlot]) {
                    player4 = player;
                    player4.packetSender.setInterfaceHiddenFlag(0, 19012);
                    player4 = player;
                    player4.packetSender.setInterfaceHiddenFlag(1, 19014);
                    player4 = player;
                    player4.packetSender.sendInterfaceText("Sell offer", 18992);
                    player4 = player;
                    player4.packetSender.sendInterfaceText("You " + (String)value5 + " sold a total of " + GameUtil.formatNumber(player.grandExchangeCompletedQuantities[player.selectedGrandExchangeSlot]) + (String)value, 19001);
                } else {
                    player4 = player;
                    player4.packetSender.setInterfaceHiddenFlag(1, 19012);
                    player4 = player;
                    player4.packetSender.setInterfaceHiddenFlag(0, 19014);
                    player4 = player;
                    player4.packetSender.sendInterfaceText("Buy offer", 18992);
                    player4 = player;
                    player4.packetSender.sendInterfaceText("You " + (String)value5 + " bought a total of " + GameUtil.formatNumber(player.grandExchangeCompletedQuantities[player.selectedGrandExchangeSlot]) + (String)value, 19001);
                }
                if (enabled2) {
                    buttonId = 250;
                }
                if (enabled2 || enabled) {
                    player4 = player;
                    player4.packetSender.setInterfaceHiddenFlag(1, 19016);
                } else {
                    player4 = player;
                    player4.packetSender.setInterfaceHiddenFlag(0, 19016);
                }
                player4 = player;
                player4.packetSender.sendInterfaceProgress(19011, buttonId);
                if (player.grandExchangeSellOfferFlags[player.selectedGrandExchangeSlot]) {
                    value5 = new ItemStack(995, player.grandExchangePrimaryCollectAmounts[player.selectedGrandExchangeSlot]);
                    value = new ItemStack(player.grandExchangeItemIds[player.selectedGrandExchangeSlot], player.grandExchangeSecondaryCollectAmounts[player.selectedGrandExchangeSlot]);
                } else {
                    value5 = new ItemStack(player.grandExchangeItemIds[player.selectedGrandExchangeSlot], player.grandExchangePrimaryCollectAmounts[player.selectedGrandExchangeSlot]);
                    value = new ItemStack(995, player.grandExchangeSecondaryCollectAmounts[player.selectedGrandExchangeSlot]);
                }
                if (((ItemStack)value5).getAmount() <= 0) {
                    value5 = null;
                }
                if (((ItemStack)value).getAmount() <= 0) {
                    value = null;
                }
                ItemStack[] itemStackArray = new ItemStack[]{(ItemStack)value5, (ItemStack)value};
                player4 = player;
                player4.packetSender.sendItemContainer(19006, itemStackArray);
                player4 = player;
                player4.packetSender.showInterface(18984);
                if (value5 != null || value != null || !enabled && !enabled2) return true;
                GrandExchangeManager.clearSelectedOfferSlot(player);
                return true;
            }
            case 18908: 
            case 18957: {
                GrandExchangeManager.adjustSelectedOfferQuantity(player, -1);
                return true;
            }
            case 18898: 
            case 18909: 
            case 18947: 
            case 18958: {
                GrandExchangeManager.adjustSelectedOfferQuantity(player, 1);
                return true;
            }
            case 18899: 
            case 18948: {
                GrandExchangeManager.adjustSelectedOfferQuantity(player, 10);
                return true;
            }
            case 18900: 
            case 18949: {
                GrandExchangeManager.adjustSelectedOfferQuantity(player, 100);
                return true;
            }
            case 18901: {
                GrandExchangeManager.adjustSelectedOfferQuantity(player, 1000);
                return true;
            }
            case 18950: {
                String text = "all";
                if (text.equals("all")) {
                    player.selectedGrandExchangeQuantity = player.getInventoryManager().getContainer().getItemAmount(player.selectedGrandExchangeItemId);
                    ItemStack itemStack = new ItemStack(player.selectedGrandExchangeItemId, 1);
                    boolean definition = itemStack.getDefinition().hasNote();
                    if (definition) {
                        int definition2 = itemStack.getDefinition().getNotedId();
                        player.selectedGrandExchangeQuantity += player.getInventoryManager().getContainer().getItemAmount(definition2);
                    }
                }
                GrandExchangeManager.refreshSelectedOfferTotals(player);
                return true;
            }
            case 18902: 
            case 18951: {
                Player player5 = player;
                player5.packetSender.sendEnterInputPrompt(18900);
                return true;
            }
            case 18910: 
            case 18959: {
                GrandExchangeManager.adjustSelectedOfferUnitPrice(player, -1);
                return true;
            }
            case 18911: 
            case 18960: {
                GrandExchangeManager.adjustSelectedOfferUnitPrice(player, 1);
                return true;
            }
            case 18903: 
            case 18952: {
                GrandExchangeManager.applySelectedOfferPricePreset(player, "-5%");
                return true;
            }
            case 18906: 
            case 18955: {
                GrandExchangeManager.applySelectedOfferPricePreset(player, "+5%");
                return true;
            }
            case 18904: 
            case 18953: {
                GrandExchangeManager.applySelectedOfferPricePreset(player, "guide");
                return true;
            }
            case 18905: 
            case 18954: {
                if (ServerSettings.instantGrandExchangeEnabled) {
                    Player player6 = player;
                    player6.packetSender.sendGameMessage("Price can't be modified with instant Grand Exchange trades.");
                    return true;
                }
                Player player7 = player;
                player7.packetSender.sendEnterInputPrompt(18901);
                return true;
            }
            case 18896: 
            case 18945: {
                if (player.selectedGrandExchangeItemId < 0 || player.selectedGrandExchangeQuantity <= 0 || player.selectedGrandExchangeSlot < 0 || player.selectedGrandExchangeSlot > 5) return true;
                if (player.selectedGrandExchangeUnitPrice < 1) {
                    player.packetSender.sendGameMessage("Grand Exchange offers must be at least 1 coin each.");
                    return true;
                }
                if (!GrandExchangeCatalog.isExchangeable(player.selectedGrandExchangeItemId)) {
                    player.packetSender.sendGameMessage("That item cannot be traded on this Grand Exchange.");
                    return true;
                }
                int initialValue = 1;
                if (player.getOpenInterfaceId() == 18890) {
                    initialValue = 0;
                }
                if (initialValue == 0) {
                    int value7 = player.selectedGrandExchangeUnitPrice * player.selectedGrandExchangeQuantity;
                    if (!player.getInventoryManager().containsItemAmount(995, value7)) {
                        Player player8 = player;
                        player8.packetSender.sendGameMessage("Not enough coins!");
                        return true;
                    }
                    player.getInventoryManager().removeItem(new ItemStack(995, value7));
                } else {
                    int value8 = player.selectedGrandExchangeQuantity;
                    ItemStack itemStack = new ItemStack(player.selectedGrandExchangeItemId, 1);
                    int index = 0;
                    int initialValue2 = -1;
                    boolean definition3 = itemStack.getDefinition().hasNote();
                    if (definition3) {
                        initialValue2 = itemStack.getDefinition().getNotedId();
                        index = player.getInventoryManager().getContainer().getItemAmount(initialValue2);
                    }
                    if (index >= value8) {
                        player.getInventoryManager().removeItem(new ItemStack(initialValue2, value8));
                    } else {
                        if (index > 0) {
                            player.getInventoryManager().removeItem(new ItemStack(initialValue2, index));
                        }
                        player.getInventoryManager().removeItem(new ItemStack(player.selectedGrandExchangeItemId, value8 - index));
                    }
                }
                player.grandExchangeSellOfferFlags[player.selectedGrandExchangeSlot] = initialValue != 0;
                player.grandExchangeItemIds[player.selectedGrandExchangeSlot] = player.selectedGrandExchangeItemId;
                player.grandExchangeQuantities[player.selectedGrandExchangeSlot] = player.selectedGrandExchangeQuantity;
                player.grandExchangeUnitPrices[player.selectedGrandExchangeSlot] = player.selectedGrandExchangeUnitPrice;
                if (!ServerSettings.instantGrandExchangeEnabled) {
                    new GrandExchangeOffer(player.getUsername(), player.selectedGrandExchangeSlot, initialValue != 0, player.selectedGrandExchangeItemId, player.selectedGrandExchangeQuantity, player.selectedGrandExchangeQuantity, player.selectedGrandExchangeUnitPrice);
                } else {
                    int value9 = player.selectedGrandExchangeSlot;
                    Player player9 = player;
                    int value10 = player9.grandExchangeQuantities[value9];
                    initialValue = player9.grandExchangeUnitPrices[value9] * player9.grandExchangeQuantities[value9];
                    player9.grandExchangeCompletedQuantities[value9] = value10;
                    player9.grandExchangeTotalPrices[value9] = initialValue;
                    player9.grandExchangePrimaryCollectAmounts[value9] = !player9.grandExchangeSellOfferFlags[value9] ? player9.grandExchangeQuantities[value9] : initialValue;
                }
                GrandExchangeManager.openGrandExchange(player);
                return true;
            }
            case 19017: {
                ItemStack[] itemStackArray;
                int value11 = 100 * (player.grandExchangeCompletedQuantities[player.selectedGrandExchangeSlot] / player.grandExchangeQuantities[player.selectedGrandExchangeSlot]);
                boolean enabled3 = value11 == 100;
                boolean enabled4 = player.grandExchangeCancelledFlags[player.selectedGrandExchangeSlot];
                if (enabled4 || enabled3) return true;
                GrandExchangeOffer.cancelOffer(player, player.selectedGrandExchangeSlot);
                player.grandExchangeCancelledFlags[player.selectedGrandExchangeSlot] = true;
                Player player10 = player;
                player10.packetSender.setInterfaceHiddenFlag(1, 19016);
                if (player.grandExchangeSellOfferFlags[player.selectedGrandExchangeSlot]) {
                    ItemStack itemStack = new ItemStack(995, player.grandExchangePrimaryCollectAmounts[player.selectedGrandExchangeSlot]);
                    ItemStack itemStack2 = new ItemStack(player.grandExchangeItemIds[player.selectedGrandExchangeSlot], player.grandExchangeQuantities[player.selectedGrandExchangeSlot] - player.grandExchangeCompletedQuantities[player.selectedGrandExchangeSlot]);
                    player.grandExchangeSecondaryCollectAmounts[player.selectedGrandExchangeSlot] = itemStack2.getAmount();
                    if (itemStack.getAmount() <= 0) {
                        itemStack = null;
                    }
                    if (itemStack2.getAmount() <= 0) {
                        itemStack2 = null;
                    }
                    itemStackArray = new ItemStack[]{itemStack, itemStack2};
                } else {
                    ItemStack itemStack = new ItemStack(player.grandExchangeItemIds[player.selectedGrandExchangeSlot], player.grandExchangePrimaryCollectAmounts[player.selectedGrandExchangeSlot]);
                    ItemStack itemStack3 = new ItemStack(995, (player.grandExchangeQuantities[player.selectedGrandExchangeSlot] - player.grandExchangeCompletedQuantities[player.selectedGrandExchangeSlot]) * player.grandExchangeUnitPrices[player.selectedGrandExchangeSlot]);
                    int value12 = player.selectedGrandExchangeSlot;
                    player.grandExchangeSecondaryCollectAmounts[value12] = player.grandExchangeSecondaryCollectAmounts[value12] + itemStack3.getAmount();
                    if (itemStack.getAmount() <= 0) {
                        itemStack = null;
                    }
                    if (itemStack3.getAmount() <= 0) {
                        itemStack3 = null;
                    }
                    itemStackArray = new ItemStack[]{itemStack, itemStack3};
                }
                player10 = player;
                player10.packetSender.sendInterfaceProgress(19011, 250);
                player10 = player;
                player10.packetSender.sendItemContainer(19006, itemStackArray);
                GrandExchangeManager.refreshSelectedOfferDetails(player);
                return true;
            }
        }
        return false;
    }

    public static void refreshOfferSlots(Player player) {
        int index = 0;
        while (index < 6) {
            Player player2;
            if (player.grandExchangeQuantities[index] == 0) {
                player2 = player;
                player2.packetSender.sendInterfaceText("Empty", index + 19095);
                player2 = player;
                player2.packetSender.setInterfaceHiddenFlag(0, 19023 + index * 3);
                player2 = player;
                player2.packetSender.setInterfaceHiddenFlag(1, 19041 + index * 9);
                player2 = player;
                player2.packetSender.sendSingleItemContainer(19044 + index * 9, -1, 0);
                player2 = player;
                player2.packetSender.sendInterfaceText("Item name", 19045 + index * 9);
                player2 = player;
                player2.packetSender.sendInterfaceText("0 coins", 19046 + index * 9);
                player2 = player;
                player2.packetSender.sendInterfaceProgress(19049 + index * 9, 0);
            } else {
                player2 = player;
                player2.packetSender.sendInterfaceText(player.grandExchangeSellOfferFlags[index] ? "Sell" : "Buy", index + 19095);
                player2 = player;
                player2.packetSender.sendSingleItemContainer(19044 + index * 9, player.grandExchangeItemIds[index], player.grandExchangeQuantities[index]);
                player2 = player;
                player2.packetSender.sendInterfaceText(new ItemStack(player.grandExchangeItemIds[index], 1).getDefinition().getName(), 19045 + index * 9);
                player2 = player;
                player2.packetSender.sendInterfaceText(String.valueOf(GameUtil.formatNumber(player.grandExchangeUnitPrices[index])) + " coins", 19046 + index * 9);
                double value = player.grandExchangeCompletedQuantities[index];
                double value2 = player.grandExchangeQuantities[index];
                double value3 = 100.0 * (value / value2);
                int value4 = (int)value3;
                boolean enabled = player.grandExchangeCancelledFlags[index];
                if (enabled) {
                    value4 = 250;
                }
                player2 = player;
                player2.packetSender.sendInterfaceProgress(19049 + index * 9, value4);
                player2 = player;
                player2.packetSender.setInterfaceHiddenFlag(1, 19023 + index * 3);
                player2 = player;
                player2.packetSender.setInterfaceHiddenFlag(0, 19041 + index * 9);
            }
            ++index;
        }
    }

    public static void rollInstantPriceFluctuation() {
        int value = GameUtil.rollPriceFluctuationPercent(11);
        if (GameUtil.randomInt(2) == 0) {
            value = -value;
        }
        instantPriceFluctuationPercent = value;
    }

    public static boolean isExchangeableItem(int itemId) {
        return GrandExchangeCatalog.isExchangeable(itemId);
    }

    public static int getGuidePrice(int itemId) {
        if (itemId == 995) return 1;
        ItemDefinition definition = ItemDefinition.forId(itemId);
        int unnotedId = definition.isNote() ? definition.getUnnotedId() : definition.getId();
        int price = GrandExchangePriceSample.getAveragePrice(unnotedId);
        if (price == -1) {
            price = GrandExchangeCatalog.getGuidePrice(unnotedId);
            if (price == -1) price = definition.getValue();
        }
        if (ServerSettings.instantGrandExchangeEnabled && ServerSettings.instantGrandExchangePriceFluctuationEnabled) {
            price += price / 100 * instantPriceFluctuationPercent;
            if (price <= 0) price = 1;
        }
        return price;
    }

    public static void setSelectedOfferQuantity(Player player, int quantity) {
        player.selectedGrandExchangeQuantity = quantity;
        if (player.selectedGrandExchangeQuantity <= 0) {
            player.selectedGrandExchangeQuantity = 1;
        }
        if (player.getOpenInterfaceId() == 18939) {
            quantity = player.getInventoryManager().getContainer().getItemAmount(player.selectedGrandExchangeItemId);
            ItemStack itemStack = new ItemStack(player.selectedGrandExchangeItemId, 1);
            boolean definition = itemStack.getDefinition().hasNote();
            if (definition) {
                int definition2 = itemStack.getDefinition().getNotedId();
                quantity += player.getInventoryManager().getContainer().getItemAmount(definition2);
            }
            if (player.selectedGrandExchangeQuantity >= quantity) {
                player.selectedGrandExchangeQuantity = quantity;
            }
        }
        GrandExchangeManager.refreshSelectedOfferTotals(player);
    }

    public static void setSelectedOfferUnitPrice(Player player, int value2) {
        player.selectedGrandExchangeUnitPrice = value2;
        if (player.selectedGrandExchangeUnitPrice < 0) {
            player.selectedGrandExchangeUnitPrice = 0;
        }
        GrandExchangeManager.refreshSelectedOfferTotals(player);
    }

    private static void adjustSelectedOfferQuantity(Player player, int quantity) {
        player.selectedGrandExchangeQuantity += quantity;
        if (player.selectedGrandExchangeQuantity <= 0) {
            player.selectedGrandExchangeQuantity = 1;
        }
        if (player.getOpenInterfaceId() == 18939) {
            quantity = player.getInventoryManager().getContainer().getItemAmount(player.selectedGrandExchangeItemId);
            ItemStack itemStack = new ItemStack(player.selectedGrandExchangeItemId, 1);
            boolean definition = itemStack.getDefinition().hasNote();
            if (definition) {
                int definition2 = itemStack.getDefinition().getNotedId();
                quantity += player.getInventoryManager().getContainer().getItemAmount(definition2);
            }
            if (player.selectedGrandExchangeQuantity >= quantity) {
                player.selectedGrandExchangeQuantity = quantity;
            }
        }
        GrandExchangeManager.refreshSelectedOfferTotals(player);
    }

    private static void adjustSelectedOfferUnitPrice(Player player, int value2) {
        if (ServerSettings.instantGrandExchangeEnabled) {
            player.packetSender.sendGameMessage("Price can't be modified with instant Grand Exchange trades.");
            return;
        }
        player.selectedGrandExchangeUnitPrice += value2;
        if (player.selectedGrandExchangeUnitPrice < 0) {
            player.selectedGrandExchangeUnitPrice = 0;
        }
        GrandExchangeManager.refreshSelectedOfferTotals(player);
    }

    private static void applySelectedOfferPricePreset(Player player, String text2) {
        double value;
        if (ServerSettings.instantGrandExchangeEnabled) {
            player.packetSender.sendGameMessage("Price can't be modified with instant Grand Exchange trades.");
            return;
        }
        if (text2.equals("guide")) {
            player.selectedGrandExchangeUnitPrice = GrandExchangeManager.getGuidePrice(player.selectedGrandExchangeItemId);
        }
        if (text2.equals("+5%")) {
            int value2;
            double value3 = player.selectedGrandExchangeUnitPrice;
            value = value3 * 1.05;
            player.selectedGrandExchangeUnitPrice = value2 = (int)value;
        }
        if (text2.equals("-5%")) {
            int value4;
            double value5 = player.selectedGrandExchangeUnitPrice;
            value = value5 * 0.95;
            player.selectedGrandExchangeUnitPrice = value4 = (int)value;
        }
        if (player.selectedGrandExchangeUnitPrice < 0) {
            player.selectedGrandExchangeUnitPrice = 0;
        }
        GrandExchangeManager.refreshSelectedOfferTotals(player);
    }

    private static void refreshSelectedOfferTotals(Player player) {
        int value = 18920;
        if (player.getOpenInterfaceId() == 18939) {
            value = 18969;
        }
        Player player2 = player;
        player2.packetSender.sendInterfaceText(GameUtil.formatNumber(player.selectedGrandExchangeQuantity), value);
        player2 = player;
        player2.packetSender.sendInterfaceText(String.valueOf(GameUtil.formatNumber(player.selectedGrandExchangeUnitPrice)) + " coins", value + 1);
        player2 = player;
        player2.packetSender.sendInterfaceText(String.valueOf(GameUtil.formatNumber(player.selectedGrandExchangeUnitPrice * player.selectedGrandExchangeQuantity)) + " coins", value + 2);
    }

    public static void selectSellOfferItem(Player player, int slot, int itemId, int value32) {
        ItemStack item = player.getInventoryManager().getContainer().getItemAt(slot);
        if (item == null || item.getId() != itemId || !item.isValid()) {
            return;
        }
        ItemDefinition definition = item.getDefinition();
        if (definition.getId() > 11883) {
            player.packetSender.sendGameMessage("This item is not supported yet.");
            return;
        }
        int exchangeItemId = definition.isNote() ? definition.getUnnotedId() : definition.getId();
        if (exchangeItemId == 995) {
            return;
        }
        if (!GrandExchangeCatalog.isExchangeable(exchangeItemId)) {
            player.packetSender.sendGameMessage("That item cannot be traded on this Grand Exchange.");
            return;
        }
        player.selectedGrandExchangeItemId = exchangeItemId;
        player.selectedGrandExchangeQuantity = item.getAmount();
        player.selectedGrandExchangeUnitPrice = GrandExchangeManager.getGuidePrice(exchangeItemId);
        player.packetSender.sendInterfaceText(GameUtil.formatNumber(player.selectedGrandExchangeUnitPrice), 18968);
        player.packetSender.sendInterfaceItemModel(18983, exchangeItemId);
        String examine = ItemDefinition.forId(exchangeItemId).getDescription();
        player.packetSender.sendInterfaceText(examine == null ? "" : examine, 18967);
        GrandExchangeManager.refreshSelectedOfferTotals(player);
    }
}

