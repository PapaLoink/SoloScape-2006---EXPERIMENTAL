package com.rs2.net.packet.handler;

import com.rs2.model.player.Player;
import com.rs2.net.packet.IncomingPacket;
import com.rs2.net.packet.PacketHandler;

public final class IdlePacketHandler
implements PacketHandler {
    @Override
    public final void handle(Player player, IncomingPacket incomingPacket) {
        player.setIdlePacketCount(player.getIdlePacketCount() + 1);
        // The client sends this once the player has been idle for the time chosen in its settings
        // (default 90 s); it is the one that decides, so log out here.
        if (!player.isBot && player.getSingleCombatTimer().hasElapsed()) {
            player.packetSender.sendLogout();
            player.disconnect();
        }
    }
}
