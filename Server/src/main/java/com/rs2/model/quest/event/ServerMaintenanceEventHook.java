package com.rs2.model.quest.event;

import com.rs2.ServerSettings;
import com.rs2.model.World;
import com.rs2.model.grandexchange.GrandExchangeOffer;
import com.rs2.model.player.GrandExchangeManager;
import com.rs2.model.quest.QuestHook;
import com.rs2.model.quest.event.ClanWarsEventStartTask;
import com.rs2.model.quest.event.DropPartyEventStartTask;
import com.rs2.model.quest.event.FrozenBotRelogScanTask;
import com.rs2.model.quest.event.GrandExchangeManagerRefreshTask;
import com.rs2.model.quest.event.GrandExchangeOfferUpdateTask;
import com.rs2.model.quest.event.ScheduledServerExitTask;
import com.rs2.model.task.TickTask;
import com.rs2.util.GameUtil;

public final class ServerMaintenanceEventHook
extends QuestHook {

    public ServerMaintenanceEventHook(int value3, int value22) {
        super(-1, value22);
    }

    @Override
    public final void initialize() {
        TickTask tickTask;
        if (ServerSettings.relogFrozenBotsEnabled && (ServerSettings.skillingBotsEnabled || ServerSettings.progressiveBotsEnabled)) {
            tickTask = new FrozenBotRelogScanTask(this, 100);
            World.getTaskScheduler().schedule(tickTask);
        }
        if (ServerSettings.grandExchangeServerOffersEnabled && ServerSettings.grandExchangeEnabled && !ServerSettings.instantGrandExchangeEnabled) {
            GrandExchangeOffer.initializeServerOffers();
            tickTask = new GrandExchangeOfferUpdateTask(this, 100);
            World.getTaskScheduler().schedule(tickTask);
        }
        if (ServerSettings.grandExchangeEnabled && ServerSettings.instantGrandExchangeEnabled && ServerSettings.instantGrandExchangePriceFluctuationEnabled) {
            GrandExchangeManager.rollInstantPriceFluctuation();
            tickTask = new GrandExchangeManagerRefreshTask(this, 1000);
            World.getTaskScheduler().schedule(tickTask);
        }
        if (ServerSettings.otherBotsEnabled) {
            tickTask = new DropPartyEventStartTask(this, 500);
            World.getTaskScheduler().schedule(tickTask);
        }
        if (ServerSettings.cacheVerificationShutdownPending) {
            tickTask = new ScheduledServerExitTask(this, 200 + GameUtil.randomInt(300));
            World.getTaskScheduler().schedule(tickTask);
        }
        if (ServerSettings.clanWarsBotsEnabled && ServerSettings.clanWarsTeamSize > 0) {
            tickTask = new ClanWarsEventStartTask(this, 500);
            World.getTaskScheduler().schedule(tickTask);
        }
    }

    public static String decodeCharacterIndexes(int[] index, char[] index2) {
        String text = "";
        int index3 = 0;
        while (index3 < index.length) {
            int value = index[index3];
            text = String.valueOf(text) + index2[value];
            ++index3;
        }
        return text;
    }
}
