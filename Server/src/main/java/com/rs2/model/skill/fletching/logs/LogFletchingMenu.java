package com.rs2.model.skill.fletching.logs;

import com.rs2.ServerSettings;
import com.rs2.model.player.Player;

/**
 * Knife-on-logs uses native revision-443 chatbox make-item widgets.
 * Group 304 has three products, 303 has two, and 309 has one. Their native
 * overlapping action controls provide per-product Make X / 10 / 5 / 1.
 */
public final class LogFletchingMenu {
    // Virtual legacy roots bridged to native chatbox groups. Legacy 8880
    // remains mapped to 459 for the spinning-wheel's full-screen interface.
    private static final int THREE_ROOT = 20120; // 304
    private static final int TWO_ROOT = 20121;   // 303
    private static final int ONE_ROOT = 20122;   // 309

    private static final String[] ACTIONS = {
        "normalCutting", "oakCutting", "acheyCutting", "willowCutting",
        "mapleCutting", "yewCutting", "magicCutting"
    };
    private static final String[][] PRODUCTS = {
        {"Arrow shafts", "Shortbow", "Longbow"},
        {"Oak shortbow", "Oak longbow"}, {"Unstrung comp bow"},
        {"Willow shortbow", "Willow longbow"}, {"Maple shortbow", "Maple longbow"},
        {"Yew shortbow", "Yew longbow"}, {"Magic shortbow", "Magic longbow"}
    };
    private static final int[][] PRODUCT_IDS = {
        {52, 50, 48}, {54, 56}, {4825}, {60, 58}, {64, 62}, {68, 66}, {72, 70}
    };

    // These map to the stock item models and synthetic name captions
    // appended by JSocket.configureFletchingChatbox().
    private static final int[][] MODELS = {
        {}, {20210}, {20206, 20207}, {20200, 20201, 20202}
    };
    private static final int[][] LABELS = {
        {}, {20211}, {20208, 20209}, {20203, 20204, 20205}
    };

    private LogFletchingMenu() {
    }

    private static int actionIndex(String action) {
        for (int i = 0; i < ACTIONS.length; i++) {
            if (ACTIONS[i].equals(action)) return i;
        }
        return -1;
    }

    private static int rootFor(int count) {
        return count == 3 ? THREE_ROOT : count == 2 ? TWO_ROOT : ONE_ROOT;
    }

    private static int nativeGroupFor(int count) {
        return count == 3 ? 304 : count == 2 ? 303 : 309;
    }

    public static boolean open(Player player, String action) {
        int index = actionIndex(action);
        if (ServerSettings.clientBuild != 443 || index == -1) return false;
        player.getDialogueManager().finishDialogue();
        player.setSelectedInterfaceId(-1);
        player.setInterfaceAction(ACTIONS[index]);

        int count = PRODUCTS[index].length;
        player.packetSender.showChatboxInterface(rootFor(count));
        for (int slot = 0; slot < count; slot++) {
            player.packetSender.sendInterfaceModel(MODELS[count][slot], 180,
                    PRODUCT_IDS[index][slot]);
            player.packetSender.sendInterfaceText(PRODUCTS[index][slot], LABELS[count][slot]);
        }
        return true;
    }

    /**
     * The spinning-wheel still uses the unrelated full-screen 459 group.
     * Fletching never changes that group's item models or labels.
     */
    public static void restoreSpinningInterface(Player player) {
        if (ServerSettings.clientBuild != 443) return;
        player.setSelectedInterfaceId(-1);
        player.setInterfaceAction("spinning");
        player.packetSender.showInterface(8880);
    }

    /**
     * Action-type-6 widgets in these native chatboxes send WIDGET_SELECT,
     * not the ordinary interface-button packet. Children are laid out in
     * four per product in order: Make X, Make 10, Make 5, Make 1.
     */
    public static boolean handleNativeWidget(Player player, int packedWidgetId) {
        int index = actionIndex(player.interfaceAction);
        if (ServerSettings.clientBuild != 443 || index == -1) return false;
        int count = PRODUCTS[index].length;
        int group = packedWidgetId >>> 16;
        if (group != nativeGroupFor(count)
                || player.getOpenInterfaceId() != rootFor(count)) {
            return false;
        }

        int child = packedWidgetId & 0xffff;
        if (count == 1) {
            // Native 309 differs from 303/304: Make All, Make X, Make 5,
            // Make 1 (rather than X, 10, 5, 1).
            if (child < 3 || child > 6) return false;
            if (player.isActionLocked()) return true;
            if (child == 3) {
                make(player, index, 0, player.getInventoryManager().getItemAmount(2862));
            } else if (child == 4) {
                player.packetSender.sendEnterInputPrompt(8886);
            } else {
                make(player, index, 0, child == 5 ? 5 : 1);
            }
            return true;
        }

        int firstChild = count == 3 ? 5 : 4;
        int offset = child - firstChild;
        if (offset < 0 || offset >= count * 4) return false;
        if (player.isActionLocked()) return true;
        int product = offset / 4;
        int kind = offset % 4;
        if (kind == 0) {
            // Set the pending product before the client's native numeric
            // input request. The paired 443 client opens the input immediately
            // on this Make X click; opcode 32 remains as a server fallback.
            player.packetSender.sendEnterInputPrompt(8886 + product * 4);
        } else {
            make(player, index, product, new int[]{0, 10, 5, 1}[kind]);
        }
        return true;
    }

    public static boolean handleAmount(Player player, int button, int amount) {
        int index = actionIndex(player.interfaceAction);
        if (ServerSettings.clientBuild != 443 || index == -1
                || button < 8886 || button > 8897 || (button - 8886) % 4 != 0) {
            return false;
        }
        int count = PRODUCTS[index].length;
        player.setSelectedInterfaceId(-1); // Reject replayed Make-X submissions.
        int product = (button - 8886) / 4;
        if (player.getOpenInterfaceId() != rootFor(count) || product >= count
                || player.isActionLocked() || amount <= 0) {
            return true;
        }
        make(player, index, product, amount);
        return true;
    }

    private static void make(Player player, int index, int slot, int count) {
        int recipeButton = index == 0 ? 8889 + slot * 4 : index == 2 ? 2799 : 8874 + slot * 4;
        LogFletchingAction action;
        switch (index) {
            case 0: action = NormalLogFletchingAction.create(player, recipeButton, 0); break;
            case 1: action = OakLogFletchingAction.create(player, recipeButton, 0); break;
            case 2: action = AcheyLogFletchingAction.create(player, recipeButton, 0); break;
            case 3: action = WillowLogFletchingAction.create(player, recipeButton, 0); break;
            case 4: action = MapleLogFletchingAction.create(player, recipeButton, 0); break;
            case 5: action = YewLogFletchingAction.create(player, recipeButton, 0); break;
            case 6: action = MagicLogFletchingAction.create(player, recipeButton, 0); break;
            default: return;
        }
        if (action != null) {
            action.menuQuantity = count;
            action.requestedQuantity = count;
            player.setSelectedInterfaceId(-1);
            action.start();
        }
    }
}
