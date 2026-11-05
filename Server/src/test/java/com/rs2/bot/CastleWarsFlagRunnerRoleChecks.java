package com.rs2.bot;

import com.rs2.model.Position;
import com.rs2.model.World;
import com.rs2.model.gameplay.castlewars.CastleWarsManager;
import com.rs2.model.player.Player;
import com.rs2.util.TextUtil;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.channels.SelectionKey;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import sun.reflect.ReflectionFactory;

/** The reserved objective runner must actually receive the objective AI. */
public final class CastleWarsFlagRunnerRoleChecks {
    @SuppressWarnings("unchecked")
    public static void main(String[] args) throws Exception {
        Field rosterField = CastleWarsManager.class.getDeclaredField("gamePlayers");
        rosterField.setAccessible(true);
        Map<Player, CastleWarsManager.Team> roster =
                (Map<Player, CastleWarsManager.Team>) rosterField.get(null);
        require(roster.isEmpty(), "Requires an isolated game roster");
        Field registered = Player.class.getDeclaredField("registered");
        registered.setAccessible(true);
        Constructor<Player> base = Player.class.getDeclaredConstructor(SelectionKey.class);
        Constructor<?> fixture = ReflectionFactory.getReflectionFactory()
                .newConstructorForSerialization(BotPlayer.class, base);
        fixture.setAccessible(true);
        Method assign = CastleWarsBotRoleAi.class.getDeclaredMethod(
                "assignRole", BotPlayer.class, CastleWarsManager.Team.class);
        assign.setAccessible(true);
        int checks = 0;
        for (int size : new int[]{1, 2, 4, 6, 12, 20}) {
            for (int styleOffset = 0; styleOffset < 3; ++styleOffset) {
                List<BotPlayer> bots = new ArrayList<BotPlayer>();
                try {
                    for (CastleWarsManager.Team team : CastleWarsManager.Team.values()) {
                        if (team != CastleWarsManager.Team.SARADOMIN
                                && team != CastleWarsManager.Team.ZAMORAK) continue;
                        for (int i = 0; i < size; ++i) {
                            BotPlayer bot = (BotPlayer) fixture.newInstance((Object)null);
                            bot.isBot = true;
                            bot.setUsername("Runner" + team.ordinal() + "x" + i);
                            bot.setNameHash(TextUtil.encodeNameHash(bot.getUsername().toLowerCase()));
                            bot.setPosition(new Position(2400, 3104, 0));
                            bot.botPrimaryCombatStyle = (i + styleOffset) % 3;
                            registered.setBoolean(bot, true);
                            int index = 100 + bots.size();
                            require(World.getPlayers()[index] == null, "Fixture player slot occupied");
                            bot.setIndex(index);
                            bot.setEncodedIndex(index + 32768);
                            bot.setSize(1);
                            World.getPlayers()[index] = bot;
                            roster.put(bot, team);
                            bots.add(bot);
                        }
                        int runners = 0;
                        int operators = 0;
                        for (BotPlayer bot : bots) {
                            if (roster.get(bot) != team) continue;
                            Object role = assign.invoke(null, bot, team);
                            if ("CATAPULT".equals(role.toString())) ++operators;
                            if (!CastleWarsBotRoleAi.isDedicatedFlagRunner(bot)) continue;
                            ++runners;
                            require("ATTACKER".equals(role.toString()),
                                    "Dedicated runner diverted to " + role + " for " + team
                                    + " teamSize=" + size + " styleOffset=" + styleOffset);
                            require(!CastleWarsBotRoleAi.process(bot),
                                    "Specialist AI intercepted dedicated flag runner");
                        }
                        require(operators == 1, "Expected exactly one catapult operator per team");
                        require(runners == (size == 1 ? 0 : 1),
                                "Expected a dedicated runner whenever a second bot is available");
                        ++checks;
                    }
                } finally {
                    for (BotPlayer bot : bots) {
                        CastleWarsBotRoleAi.clear(bot);
                        CastleWarsBotAi.clear(bot);
                        World.getPlayers()[bot.getIndex()] = null;
                    }
                    roster.clear();
                }
            }
        }
        System.out.println("Castle Wars flag runner role checks passed: " + checks + " team configurations");
        System.exit(0);
    }

    private static void require(boolean value, String message) {
        if (!value) throw new AssertionError(message);
    }
}
