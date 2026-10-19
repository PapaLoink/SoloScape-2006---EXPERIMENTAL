package com.rs2.model.skill.cooking;

import com.rs2.bot.combat.BotCombatHelper;
import com.rs2.model.item.ItemStack;
import com.rs2.model.player.Player;
import com.rs2.model.skill.SkillActionHelper;
import com.rs2.model.skill.cooking.CookableFoodDefinition;
import com.rs2.model.skill.cooking.CookingManager;
import com.rs2.model.task.CycleEvent;
import com.rs2.model.task.CycleEventContainer;

public final class CookingTask
extends CycleEvent {
    private int remainingActions;
    private final Player player;
    private final int actionSequence;

    public CookingTask(int remainingActions, Player player, int actionSequence) {
        this.player = player;
        this.actionSequence = actionSequence;
        this.remainingActions = remainingActions;
    }

    @Override
    public final void execute(CycleEventContainer cycleEventContainer) {
        if (!(this.player.isInterruptibleActionActive() && this.player.isCurrentActionSequence(this.actionSequence) && this.player.getInventoryManager().getContainer().containsItem(this.player.getSelectedSkillItemId()) && this.remainingActions != 0)) {
            if (this.player.botEnabled) {
                CookableFoodDefinition cookableFoodDefinition = CookableFoodDefinition.forRawItemId(this.player.getSelectedSkillItemId());
                if (cookableFoodDefinition != null && this.player.getInventoryManager().getContainer().containsItem(cookableFoodDefinition.getBurntItemId())) {
                    ItemStack[] itemStackArray = this.player.getInventoryManager().getContainer().getItems();
                    int length = itemStackArray.length;
                    int index = 0;
                    while (index < length) {
                        ItemStack itemStack = itemStackArray[index];
                        if (itemStack != null && itemStack.getId() == cookableFoodDefinition.getBurntItemId()) {
                            BotCombatHelper.dropInventoryItem(this.player, itemStack);
                        }
                        ++index;
                    }
                }
                this.player.setSelectedSkillItemId(0);
                this.player.currentBotTask.startWalkToBank(this.player);
            }
            cycleEventContainer.stop();
            return;
        }
        // Firemaking fires expire. A fireplace remains usable while its
        // world object exists, but never continue after that object disappears.
        com.rs2.model.Position firePosition = this.player.getCookingManager().firePosition;
        Object value = this.player;
        if ("cookFire".equals(this.player.interfaceAction)
                && (firePosition == null || !SkillActionHelper.isObjectPresent(
                        this.player.getCookingObjectId(), firePosition.getX(),
                        firePosition.getY(), firePosition.getPlane()))) {
            if (this.player.botEnabled) {
                value = CookableFoodDefinition.forRawItemId(this.player.getSelectedSkillItemId());
                if (value != null && this.player.getInventoryManager().getContainer().containsItem(((CookableFoodDefinition)((Object)value)).getBurntItemId())) {
                    ItemStack[] itemStackArray = this.player.getInventoryManager().getContainer().getItems();
                    int length2 = itemStackArray.length;
                    int index2 = 0;
                    while (index2 < length2) {
                        ItemStack itemStack = itemStackArray[index2];
                        if (itemStack != null && itemStack.getId() == ((CookableFoodDefinition)((Object)value)).getBurntItemId()) {
                            BotCombatHelper.dropInventoryItem(this.player, itemStack);
                        }
                        ++index2;
                    }
                }
                this.player.currentBotTask.startWalkToBank(this.player);
            }
            cycleEventContainer.stop();
            return;
        }
        CookableFoodDefinition food = CookableFoodDefinition.forRawItemId(this.player.getSelectedSkillItemId());
        if (food == null || this.player.getSkillManager().getCurrentLevels()[7] < food.getRequiredLevel()) {
            cycleEventContainer.stop();
            return;
        }
        CookingManager.cookSelectedItem(this.player);
        --this.remainingActions;
        cycleEventContainer.setTickDelay(4);
    }

    @Override
    public final void onStop() {
        this.player.resetAnimation();
    }
}

