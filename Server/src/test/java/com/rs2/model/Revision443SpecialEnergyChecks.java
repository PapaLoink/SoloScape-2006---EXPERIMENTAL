package com.rs2.model;

import com.rs2.model.player.Player;

/**
 * Revision-443 human players bypass Player.process(), but still need their
 * special attack energy to restore in Castle Wars and the regular world.
 */
public final class Revision443SpecialEnergyChecks {
    public static void main(String[] args) {
        checkRecoveryAt(2441, 3090, "Castle Wars");
        checkRecoveryAt(3222, 3222, "the regular world");
        System.out.println("Revision 443 special-energy restoration checks passed");
    }

    private static void checkRecoveryAt(int x, int y, String area) {
        Player player = new Player(null);
        // Exercise the human 443 tick handler without sending socket packets.
        player.isBot = true;
        // The player-type bit is set when World registers a real player.
        player.setIndex(1);
        player.setEncodedIndex(32769);
        player.loginInitializationComplete = true;
        player.setPosition(new Position(x, y, 0));
        player.setSpecialEnergy(70);

        World.processRevision443Player(player); // Schedule passive restoration.
        for (int tick = 0; tick < 49; tick++) {
            World.processTick();
        }
        require(player.getSpecialEnergy() == 70, area + " restored energy too early");

        World.processTick(); // 50 ticks (30 seconds): restore 10%.
        require(player.getSpecialEnergy() == 80, area + " did not restore 10%");

        for (int tick = 0; tick < 100; tick++) {
            World.processTick();
        }
        require(player.getSpecialEnergy() == 100,
                area + " did not finish restoring to 100%");
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}
