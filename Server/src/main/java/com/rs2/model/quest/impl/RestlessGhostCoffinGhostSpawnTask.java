package com.rs2.model.quest.impl;

import com.rs2.model.GameplayHelper;
import com.rs2.model.npc.Npc;
import com.rs2.model.npc.NpcMovementMode;
import com.rs2.model.player.Player;
import com.rs2.model.quest.impl.RestlessGhostQuest;
import com.rs2.model.task.TickTask;

public final class RestlessGhostCoffinGhostSpawnTask
extends TickTask {
    private final Player player;

    public RestlessGhostCoffinGhostSpawnTask(RestlessGhostQuest restlessGhostQuest, int value2, Player player) {
        super(value2);
        this.player = player;
    }

    @Override
    public final void execute() {
        // Multiple opens can queue a task before the first ghost appears.
        if (Npc.findByDefinitionId(457) == null) {
            Npc ghost = new Npc(457);
            GameplayHelper.spawnRoamingNpcFacingPlayer(this.player, ghost, 3250, 3195, 0, 1500, false, false);
            ghost.setMovementMode(NpcMovementMode.STATIONARY);
        }
        this.stop();
    }
}

