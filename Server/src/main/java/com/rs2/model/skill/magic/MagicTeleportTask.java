package com.rs2.model.skill.magic;

import com.rs2.model.Position;
import com.rs2.model.item.ItemStack;
import com.rs2.model.player.Player;
import com.rs2.model.skill.magic.TeleportManager;
import com.rs2.model.task.CycleEvent;
import com.rs2.model.task.CycleEventContainer;
import com.rs2.util.GameplayTrace;

public final class MagicTeleportTask
extends CycleEvent {
    private int ticksRemaining = 6;
    private TeleportManager teleportManager;
    private final boolean ancientSpellbookTeleport;
    private final int destinationX;
    private final int destinationY;
    private final int destinationPlane;

    public MagicTeleportTask(TeleportManager teleportManager, boolean ancientSpellbookTeleport, int destinationX, int destinationY, int destinationPlane) {
        this.teleportManager = teleportManager;
        this.ancientSpellbookTeleport = ancientSpellbookTeleport;
        this.destinationX = destinationX;
        this.destinationY = destinationY;
        this.destinationPlane = destinationPlane;
    }

    @Override
    public final void execute(CycleEventContainer cycleEventContainer) {
        --this.ticksRemaining;
        if (!TeleportManager.getPlayer(this.teleportManager).isDead()) {
            if (this.ticksRemaining == 2) {
                if (!this.ancientSpellbookTeleport) {
                    TeleportManager.getPlayer(this.teleportManager).getUpdateState().setAnimation(715);
                }
                if (TeleportManager.getPlayer(this.teleportManager).isInTenthSquadSigilInstance()) {
                    TeleportManager.getPlayer(this.teleportManager).clearTemporaryCutsceneNpcs();
                }
                if (TeleportManager.getPlayer(this.teleportManager).getInventoryManager().containsItem(4033)) {
                    int player = TeleportManager.getPlayer(this.teleportManager).getInventoryManager().getItemAmount(4033);
                    ItemStack itemStack = new ItemStack(4033, player);
                    TeleportManager.getPlayer(this.teleportManager).getInventoryManager().removeItem(itemStack);
                }
                if (TeleportManager.getPlayer(this.teleportManager).getInventoryManager().containsItem(431)) {
                    int player2 = TeleportManager.getPlayer(this.teleportManager).getInventoryManager().getItemAmount(431);
                    Object value = new ItemStack(431, player2);
                    TeleportManager.getPlayer(this.teleportManager).getInventoryManager().removeItem((ItemStack)value);
                    value = TeleportManager.getPlayer(this.teleportManager);
                    ((Player)value).packetSender.sendGameMessage("Why is the rum gone?");
                }
                if (GameplayTrace.enabled()) {
                    GameplayTrace.log("magic teleport move player=" + GameplayTrace.describe(TeleportManager.getPlayer(this.teleportManager)) + " destination=" + this.destinationX + "," + this.destinationY + "," + this.destinationPlane + " ancient=" + this.ancientSpellbookTeleport);
                }
                TeleportManager.getPlayer(this.teleportManager).moveTo(new Position(this.destinationX, this.destinationY, this.destinationPlane));
                com.rs2.model.quest.impl.BiohazardQuest.teleportCrossing(TeleportManager.getPlayer(this.teleportManager));
                if (GameplayTrace.enabled()) {
                    GameplayTrace.log("magic teleport moved player=" + GameplayTrace.describe(TeleportManager.getPlayer(this.teleportManager)));
                }
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
        if (GameplayTrace.enabled()) {
            GameplayTrace.log("magic teleport complete player=" + GameplayTrace.describe(TeleportManager.getPlayer(this.teleportManager)) + " destination=" + this.destinationX + "," + this.destinationY + "," + this.destinationPlane + " ancient=" + this.ancientSpellbookTeleport);
        }
        TeleportManager.getPlayer(this.teleportManager).setActionLocked(false);
        TeleportManager.getPlayer(this.teleportManager).getAttributes().put("canTakeDamage", Boolean.TRUE);
    }
}

