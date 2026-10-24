package com.rs2.model.quest.event;

import com.rs2.ServerSettings;
import com.rs2.model.Position;
import com.rs2.model.World;
import com.rs2.model.ground.GroundItem;
import com.rs2.model.ground.GroundItemManager;
import com.rs2.model.item.ItemStack;
import com.rs2.model.quest.QuestHook;
import com.rs2.model.quest.event.ChristmasDropRefreshTask;
import com.rs2.util.GameUtil;
import com.rs2.util.path.WalkingCollisionMap;

public final class ChristmasDropEventHook
extends QuestHook {
    private int spawnedSetCount = 0;
    private final int[] dropItemIds = new int[]{962, 1050};

    public ChristmasDropEventHook(int value3, int value22) {
        super(-1, value22);
    }

    @Override
    public final void initialize() {
        this.spawnDrops();
        ChristmasDropRefreshTask christmasDropRefreshTask = new ChristmasDropRefreshTask(this, ServerSettings.holidayDropRespawnTicks);
        World.getTaskScheduler().schedule(christmasDropRefreshTask);
    }

    final void spawnDrops() {
        int index = 0;
        while (index < ServerSettings.holidayDropSets * 3) {
            ++this.spawnedSetCount;
            int index2 = 0;
            while (index2 < this.dropItemIds.length) {
                int value = this.dropItemIds[index2];
                // Surface only: underground, the void outside rooms has no collision flags either,
                // so a drop there would look like free floor but be out of reach.
                int value2;
                int value3;
                do {
                    value2 = 2145 + GameUtil.randomInt(1695);
                    value3 = 2560 + GameUtil.randomInt(1410);
                } while (WalkingCollisionMap.getTileFlags(value2, value3, 0) != 0);
                Position position = new Position(value2, value3, 0);
                GroundItem groundItem = new GroundItem(new ItemStack(value, 1), position, false, true);
                GroundItemManager.getInstance().spawn(groundItem);
                ++index2;
            }
            ++index;
        }
    }

}
