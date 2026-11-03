package com.rs2.model.player;

import com.rs2.model.gameplay.castlewars.CastleWarsManager;
import com.rs2.model.player.Player;

public final class EmoteManager {
    private Player player;
    private static final int[][] EMOTE_BUTTON_ANIMATION_PAIRS = new int[][]{{168, 855}, {169, 856}, {162, 857}, {164, 858}, {165, 859}, {161, 860}, {170, 861}, {171, 862}, {163, 863}, {167, 864}, {172, 865}, {166, 866}, {13362, 2105}, {13363, 2106}, {13364, 2107}, {13365, 2108}, {13366, 2109}, {13367, 2110}, {13368, 2111}, {13369, 2112}, {13370, 2113}, {11100, 1368}, {667, 1131}, {6503, 1130}, {6506, 1129}, {666, 1128}, {13383, 2127}, {13384, 2128}, {15166, 2836}, {18464, 3544}, {18465, 3543}};

    public EmoteManager(Player player) {
        this.player = player;
    }

    public final boolean handleButtonClick(int buttonId) {
        int[][] integerValues = EMOTE_BUTTON_ANIMATION_PAIRS;
        int index = 0;
        while (index < 31) {
            int[] integerValues2 = integerValues[index];
            if (buttonId == integerValues2[0]) {
                // Consume only emote buttons. Other interface buttons must
                // remain usable while waiting in the Castle Wars caves.
                if (CastleWarsManager.isWaitingRoomGodTransformation(this.player)) {
                    return true;
                }
                int value = integerValues2[1];
                EmoteManager emoteManager = this;
                emoteManager.player.getUpdateState().setAnimation(value, 0);
                return true;
            }
            ++index;
        }
        return false;
    }
}

