package com.rs2.model.quest.impl;

import com.rs2.model.player.Player;
import com.rs2.model.task.TickTask;

/** Drinking the potion is a mistake, not an alternative quest completion. */
public final class CadavaPotionDrinkRecoveryTask extends TickTask {
    private final Player player;

    public CadavaPotionDrinkRecoveryTask(Player player) {
        super(5);
        this.player = player;
    }

    @Override
    public void execute() {
        player.packetSender.resetCamera();
        player.getMovementLockTimer().setDelayTicks(0);
        player.getDialogueManager().showOneLineStatement("You wake up feeling dreadful. The potion is gone.");
        player.packetSender.sendGameMessage("You will need another Cadava potion for Juliet.");
        stop();
    }
}
