package com.rs2.model.skill.magic;

import com.rs2.model.Position;
import com.rs2.model.player.Player;
import com.rs2.model.skill.magic.TeleportManager;
import com.rs2.model.task.CycleEvent;
import com.rs2.model.task.CycleEventContainer;

public final class StandardTeleportTask
extends CycleEvent {
    private int ticksRemaining = 8;
    private TeleportManager teleportManager;
    private final int destinationX;
    private final int destinationY;
    private final int destinationPlane;
    private final String arrivalMessage;

    public StandardTeleportTask(TeleportManager teleportManager, int destinationX, int destinationY, int destinationPlane, String arrivalMessage) {
        this.teleportManager = teleportManager;
        this.destinationX = destinationX;
        this.destinationY = destinationY;
        this.destinationPlane = destinationPlane;
        this.arrivalMessage = arrivalMessage;
    }

    public final void startImmediately() {
        this.ticksRemaining = 4;
        this.playDepartureEffects();
    }

    private void playDepartureEffects() {
        Player player = TeleportManager.getPlayer(this.teleportManager);
        player.getUpdateState().setAnimation(714);
        player.getUpdateState().setGraphicHeight100(301);
        player.packetSender.sendSoundEffect(202, 1, 0);
    }

    @Override
    public final void execute(CycleEventContainer cycleEventContainer) {
        --this.ticksRemaining;
        if (!TeleportManager.getPlayer(this.teleportManager).isDead()) {
            if (this.ticksRemaining == 4) {
                this.playDepartureEffects();
            }
            if (this.ticksRemaining == 2) {
                TeleportManager.getPlayer(this.teleportManager).getUpdateState().setAnimation(715);
                TeleportManager.getPlayer(this.teleportManager).moveTo(new Position(this.destinationX, this.destinationY, this.destinationPlane));
                if (this.arrivalMessage != null) {
                    Player player = TeleportManager.getPlayer(this.teleportManager);
                    player.packetSender.sendGameMessage(this.arrivalMessage);
                }
                com.rs2.model.quest.impl.BiohazardQuest.teleportCrossing(TeleportManager.getPlayer(this.teleportManager));
            }
        } else {
            this.ticksRemaining = 0;
        }
        if (this.ticksRemaining <= 0) {
            cycleEventContainer.stop();
        }
    }

    @Override
    public final void onStop() {
        TeleportManager.getPlayer(this.teleportManager).setActionLocked(false);
        TeleportManager.getPlayer(this.teleportManager).getAttributes().put("canTakeDamage", Boolean.TRUE);
    }
}

