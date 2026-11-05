package com.rs2.bot;

import com.rs2.ServerSettings;
import com.rs2.cache.js5.Interfaces;
import com.rs2.model.Position;
import com.rs2.model.World;
import com.rs2.model.gameplay.castlewars.CastleWarsEngineeringManager;
import com.rs2.model.gameplay.castlewars.CastleWarsManager;
import com.rs2.model.item.ItemDefinition;
import com.rs2.model.item.ItemStack;
import com.rs2.model.objects.ObjectDefinition;
import com.rs2.model.player.Player;
import com.rs2.model.quest.QuestDefinition;
import com.rs2.model.task.CycleEventHandler;
import com.rs2.model.task.TaskScheduler;
import com.rs2.model.task.TickTask;
import com.rs2.util.TextUtil;
import com.rs2.util.path.WalkingCollisionMap;
import com.rs2.util.path.ProjectileCollisionMap;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Map;
import sun.reflect.ReflectionFactory;

/** Actual spawn-to-capture navigation, including non-melee closed-gate routes. */
public final class CastleWarsFlagCaptureChecks {
    @SuppressWarnings("unchecked")
    public static void main(String[] args) throws Exception {
        Long seed = Long.getLong("prs.test.castleWarsSeed");
        if (seed != null) com.rs2.util.GameUtil.getRandom().setSeed(seed.longValue());
        System.setProperty("prs.traceGameplay", "false");
        ServerSettings.clientBuild = 443;
        Interfaces.load();
        QuestDefinition.loadDefinitions();
        ItemDefinition.loadDefinitions();
        ObjectDefinition.loadRevision443();
        WalkingCollisionMap.loadCollisionMaps();
        ProjectileCollisionMap.loadCollisionMaps();
        Map<Player, CastleWarsManager.Team> roster =
                (Map<Player, CastleWarsManager.Team>) field(CastleWarsManager.class, "gamePlayers").get(null);
        require(roster.isEmpty(), "Requires an isolated game roster");
        field(CastleWarsManager.class, "initialized").setBoolean(null, true);
        field(CastleWarsManager.class, "gameInProgress").setBoolean(null, true);
        for (CastleWarsManager.Team team : CastleWarsManager.Team.values()) {
            for (int route = 0; route < 3; ++route) {
                checkCapture(roster, team, route, 0, false);
                checkCapture(roster, team, route, 1, true);
                checkCapture(roster, team, route, 2, true);
            }
        }
        System.out.println("Castle Wars capture checks passed: both teams, all three surface routes");
        System.exit(0);
    }

    @SuppressWarnings("unchecked")
    private static void checkCapture(Map<Player, CastleWarsManager.Team> roster,
                                     CastleWarsManager.Team team, int route,
                                     int style, boolean closedGates) throws Exception {
        setGates(closedGates ? "CLOSED" : "OPEN");
        Method resetSideDoors = CastleWarsEngineeringManager.class.getDeclaredMethod("resetSideDoors");
        resetSideDoors.setAccessible(true);
        resetSideDoors.invoke(null);
        Constructor<Player> base = Player.class.getDeclaredConstructor(SelectionKey.class);
        Constructor<?> fixture = ReflectionFactory.getReflectionFactory()
                .newConstructorForSerialization(BotPlayer.class, base);
        fixture.setAccessible(true);
        BotPlayer bot = (BotPlayer) fixture.newInstance((Object)null);
        bot.isBot = true;
        bot.botEnabled = true;
        bot.botPrimaryCombatStyle = style;
        bot.botActiveCombatStyle = style;
        bot.setUsername("CaptureCheck");
        bot.setNameHash(TextUtil.encodeNameHash("capturecheck"));
        bot.setClientBuild(443);
        bot.setPosition(team == CastleWarsManager.Team.SARADOMIN
                ? new Position(2424, 3076, 1) : new Position(2371, 3130, 1));
        for (int bonus = 0; bonus < 14; ++bonus) bot.setCombatBonus(bonus, 0);
        try (SocketChannel transport = SocketChannel.open()) {
            transport.close(); // Discard packets from this unconnected fixture.
            field(Player.class, "socketChannel").set(bot, transport);
            field(Player.class, "registered").setBoolean(bot, true);
            require(World.getPlayers()[100] == null, "Fixture player slot occupied");
            bot.setIndex(100);
            bot.setEncodedIndex(100 + 32768);
            bot.setSize(1);
            World.getPlayers()[100] = bot;
            roster.put(bot, team);
            for (String prefix : new String[]{"saradomin", "zamorak"}) {
                field(CastleWarsManager.class, prefix + "FlagAtBase").setBoolean(null, true);
                field(CastleWarsManager.class, prefix + "FlagHolder").set(null, null);
                field(CastleWarsManager.class, prefix + "Score").setInt(null, 0);
            }
            checkFailedPickups(bot, team);
            Map<BotPlayer, ?> states =
                    (Map<BotPlayer, ?>) field(CastleWarsBotAi.class, "states").get(null);
            boolean scored = false;
            boolean filledBag = false;
            for (int tick = 0; tick < 2000; ++tick) {
                processScheduledTasks();
                Object currentState = states.get(bot);
                if (!filledBag && currentState != null
                        && "TAKE_FLAG".equals(field(currentState.getClass(), "phase").get(currentState).toString())) {
                    // Real bots arrive with weapons and a full bag of supplies.
                    // The old naked fixtures could never expose pickup refusal.
                    bot.getEquipmentManager().getContainer().setItem(3, new ItemStack(1381));
                    bot.getEquipmentManager().getContainer().setItem(5, new ItemStack(1171));
                    bot.getInventoryManager().getContainer().clear();
                    for (int slot = 0; slot < 28; ++slot) {
                        bot.getInventoryManager().getContainer().setItem(slot, new ItemStack(4049));
                    }
                    bot.getEquipmentManager().refresh();
                    filledBag = true;
                }
                // Exercise the flag runner directly. In a live one-bot team,
                // the role scheduler reserves that bot for the catapult.
                if (tick % 2 == 0) {
                    CastleWarsBotAi.process(bot);
                }
                if (tick == 0) field(states.get(bot).getClass(), "routeVariant").setInt(states.get(bot), route);
                bot.getMovementQueue().process();
                if (CastleWarsManager.getSaradominScore() + CastleWarsManager.getZamorakScore() > 0) {
                    scored = true;
                    break;
                }
            }
            Object state = states.get(bot);
            require(scored, "No capture: " + team + " route=" + route
                    + " style=" + style + " closedGates=" + closedGates
                    + " position=" + bot.getPosition()
                    + " phase=" + field(state.getClass(), "phase").get(state));
            require(bot.getPosition().getPlane() == 3, "Capture must occur in the home flag room");
            require(filledBag, "Full-inventory pickup was not exercised");
            require(bot.getInventoryManager().getItemAmount(1381) == 1, "Flag pickup lost the weapon");
            require(bot.getInventoryManager().getItemAmount(1171) == 1, "Flag pickup lost the shield");
            require(!CastleWarsManager.isCarryingEnemyFlag(bot), "Captured flag still carried");
            System.out.println("Capture passed: " + team + " route=" + route
                    + " style=" + style + " closedGates=" + closedGates);
        } finally {
            CastleWarsBotAi.clear(bot);
            CastleWarsBotRoleAi.clear(bot);
            if (bot.getIndex() == 100) World.getPlayers()[100] = null;
            bot.setIndex(-1);
            roster.clear();
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static void checkFailedPickups(BotPlayer bot, CastleWarsManager.Team team) throws Exception {
        Position previous = bot.getPosition().copy();
        bot.setPosition(team == CastleWarsManager.Team.SARADOMIN
                ? new Position(2371, 3133, 3) : new Position(2428, 3074, 3));
        bot.getEquipmentManager().getContainer().setItem(3, new ItemStack(1381));
        bot.getEquipmentManager().getContainer().setItem(5, new ItemStack(1171));
        bot.getInventoryManager().getContainer().clear();
        for (int slot = 0; slot < 28; ++slot) {
            bot.getInventoryManager().getContainer().setItem(slot, new ItemStack(560));
        }
        for (Class<?> ai : new Class<?>[]{CastleWarsBotAi.class, CastleWarsBotRoleAi.class}) {
            Class<?> stateType = Class.forName(ai.getName()
                    + (ai == CastleWarsBotAi.class ? "$BotState" : "$RoleState"));
            Constructor<?> constructor = ai == CastleWarsBotAi.class
                    ? stateType.getDeclaredConstructor(CastleWarsManager.Team.class)
                    : stateType.getDeclaredConstructor(BotPlayer.class, CastleWarsManager.Team.class, int.class);
            constructor.setAccessible(true);
            Object state = ai == CastleWarsBotAi.class ? constructor.newInstance(team)
                    : constructor.newInstance(bot, team, bot.botPrimaryCombatStyle);
            Field phase = field(stateType, "phase");
            phase.set(state, Enum.valueOf((Class)phase.getType(), "TAKE_FLAG"));
            Method take = ai.getDeclaredMethod("processTakeFlag", BotPlayer.class, stateType);
            take.setAccessible(true);
            take.invoke(null, bot, state);
            require("TAKE_FLAG".equals(phase.get(state).toString()), "Failed pickup sent bot out of flag room");
            require(!CastleWarsManager.isCarryingEnemyFlag(bot), "Pickup unexpectedly discarded rune stacks");
        }
        require(bot.getInventoryManager().getItemAmount(560) == 28, "Pickup discarded runes");
        bot.getInventoryManager().getContainer().setItem(0, new ItemStack(4049));
        bot.getInventoryManager().getContainer().setItem(1, new ItemStack(4049));
        bot.isBot = false;
        try {
            require(!CastleWarsManager.takeEnemyFlag(bot), "Human pickup silently discarded supplies");
            require(bot.getInventoryManager().getItemAmount(4049) == 2, "Human bandages were discarded");
        } finally {
            bot.isBot = true;
            bot.getInventoryManager().getContainer().clear();
            bot.getEquipmentManager().getContainer().clear();
            bot.setPosition(previous);
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static void setGates(String mode) throws Exception {
        for (String name : new String[]{"saradominMainDoor", "zamorakMainDoor"}) {
            Object door = field(CastleWarsEngineeringManager.class, name).get(null);
            Class modeType = field(door.getClass(), "mode").getType();
            Method setMode = CastleWarsEngineeringManager.class.getDeclaredMethod(
                    "setMainDoorMode", door.getClass(), modeType);
            setMode.setAccessible(true);
            setMode.invoke(null, door, Enum.valueOf(modeType, mode));
        }
    }

    @SuppressWarnings("unchecked")
    private static void processScheduledTasks() throws Exception {
        ++World.tickCount;
        CycleEventHandler.getInstance().process();
        TaskScheduler scheduler = (TaskScheduler) field(World.class, "taskScheduler").get(null);
        ArrayList<TickTask> snapshot = new ArrayList<TickTask>(scheduler.getTasks());
        scheduler.getTasks().clear();
        for (TickTask task : snapshot) {
            task.tick();
            if (task.isActive()) scheduler.getTasks().add(task);
        }
    }

    private static Field field(Class<?> owner, String name) throws Exception {
        Field field = owner.getDeclaredField(name);
        field.setAccessible(true);
        return field;
    }

    private static void require(boolean value, String message) {
        if (!value) throw new AssertionError(message);
    }
}
