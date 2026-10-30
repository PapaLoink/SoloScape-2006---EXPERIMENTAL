package com.rs2.model.interaction;

import com.rs2.model.item.ItemStack;
import com.rs2.model.player.Player;

/** Toll-controlled passage through the Lumbridge/Al-Kharid border gates. */
public final class AlKharidGateToll {
    private static final int PRINCE_ALI_RESCUE_QUEST = 11;
    private static final int COINS = 995;
    private static final int TOLL = 10;
    private static final int GATE_X = 3268;

    private AlKharidGateToll() {
    }

    public static boolean isTollGate(int objectId, int x, int y, int plane) {
        return plane == 0 && x == GATE_X
                && (objectId == 2882 && y == 3227 || objectId == 2883 && y == 3228);
    }

    public static boolean hasFreePassage(Player player) {
        // Stage 9 is still an unfinished quest; only completion waives the toll.
        return player.getQuestState(PRINCE_ALI_RESCUE_QUEST) == 1;
    }

    /**
     * Used by both the gate's Pay-toll object action and its guard dialogue.
     * Charges once per crossing, from either side of the gate.
     */
    public static boolean tryPass(Player player) {
        if (player.isStunned() || player.isMovementLocked()) {
            return false;
        }
        if (!hasFreePassage(player)) {
            if (!player.getInventoryManager().containsItemAmount(COINS, TOLL)
                    || !player.getInventoryManager().removeItem(new ItemStack(COINS, TOLL))) {
                player.packetSender.sendGameMessage("You need 10 gold coins to pass through the gate.");
                return false;
            }
        }

        player.getPacketSender().queueRelativeMovementStep(
                player.getPosition().getX() < GATE_X ? 1 : -1, 0, true);
        player.getPacketSender().openDoubleDoorPair(2882, 2883, GATE_X, 3227, GATE_X, 3228, 0);
        return true;
    }
}
