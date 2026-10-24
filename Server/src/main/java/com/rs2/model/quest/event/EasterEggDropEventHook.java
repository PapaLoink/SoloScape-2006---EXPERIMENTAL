package com.rs2.model.quest.event;

import com.rs2.ServerSettings;
import com.rs2.model.Position;
import com.rs2.model.World;
import com.rs2.model.ground.GroundItem;
import com.rs2.model.ground.GroundItemManager;
import com.rs2.model.item.ItemStack;
import com.rs2.model.quest.QuestHook;
import com.rs2.model.quest.event.EasterEggDropRefreshTask;
import com.rs2.util.GameUtil;
import com.rs2.util.path.WalkingCollisionMap;

public final class EasterEggDropEventHook
extends QuestHook {
    private int spawnedDropCount = 0;

    public EasterEggDropEventHook(int value3, int value22) {
        super(-1, value22);
    }

    @Override
    public final void initialize() {
        this.spawnDrops();
        EasterEggDropRefreshTask easterEggDropRefreshTask = new EasterEggDropRefreshTask(this, ServerSettings.holidayDropRespawnTicks);
        World.getTaskScheduler().schedule(easterEggDropRefreshTask);
    }

    final void spawnDrops() {
        int index = 0;
        while (index < ServerSettings.holidayDropSets * 6) {
            // Surface only: underground, the void outside rooms has no collision flags either,
            // so a drop there would look like free floor but be out of reach.
            int value;
            int value2;
            do {
                value = 2145 + GameUtil.randomInt(1695);
                value2 = 2560 + GameUtil.randomInt(1410);
            } while (WalkingCollisionMap.getTileFlags(value, value2, 0) != 0);
            Object value3 = new Position(value, value2, 0);
            value3 = new GroundItem(new ItemStack(1961, 1), (Position)value3, false);
            GroundItemManager.getInstance().spawn((GroundItem)value3);
            ++this.spawnedDropCount;
            ++index;
        }
    }

}
