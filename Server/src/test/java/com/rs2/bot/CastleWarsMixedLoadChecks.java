package com.rs2.bot;

import com.rs2.ServerSettings;
import com.rs2.model.Position;
import com.rs2.model.World;
import com.rs2.model.gameplay.castlewars.CastleWarsManager;
import com.rs2.model.objects.ObjectDefinition;
import com.rs2.model.player.Player;
import com.rs2.util.GameUtil;
import com.rs2.util.path.ProjectileCollisionMap;
import com.rs2.util.path.PathFinder;
import com.rs2.util.path.WalkingCollisionMap;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import sun.reflect.ReflectionFactory;

/**
 * Fast, deterministic 443 server-side mixed Castle Wars load simulation.
 *
 * 12 REAL BotPlayer AI instances (roles, pathfinding and movement) plus two
 * Player fixtures representing humans. No external client is connected.
 * This is an isolated server logic/performance gate, not a substitute for a
 * real 20-minute multiplayer client soak. Captures unexpected console spam.
 */
public final class CastleWarsMixedLoadChecks {
    private static final int BOTS_PER_TEAM = 6;
    private static final int TICKS = 500;
    private static final int STALL_TICKS = 80;
    private static final long TICK_BUDGET_NANOS = 600_000_000L;
    private static final List<Player> allPlayers = new ArrayList<Player>();
    private static final List<BotPlayer> bots = new ArrayList<BotPlayer>();
    private static final Map<Player, Position> previous = new IdentityHashMap<Player, Position>();
    private static final Map<Player, Integer> moved = new IdentityHashMap<Player, Integer>();
    private static final Map<Player, Integer> pendingStall = new IdentityHashMap<Player, Integer>();
    private static Field registered;
    private static Field socket;
    private static Map<Player, CastleWarsManager.Team> gamePlayers;

    private CastleWarsMixedLoadChecks() {}

    @SuppressWarnings("unchecked")
    public static void main(String[] args) throws Exception {
        System.setProperty("prs.traceGameplay", "false");
        System.setProperty("prs.traceConsole", "false");
        ServerSettings.clientBuild = 443;
        ObjectDefinition.loadRevision443();
        WalkingCollisionMap.loadCollisionMaps();
        ProjectileCollisionMap.loadCollisionMaps();

        registered = Player.class.getDeclaredField("registered");
        registered.setAccessible(true);
        socket = Player.class.getDeclaredField("socketChannel");
        socket.setAccessible(true);
        Field teams = CastleWarsManager.class.getDeclaredField("gamePlayers");
        teams.setAccessible(true);
        gamePlayers = (Map<Player, CastleWarsManager.Team>) teams.get(null);
        require(gamePlayers.isEmpty(), "Load test needs a clean Castle Wars game roster");
        try {
            for (int i = 0; i < BOTS_PER_TEAM * 2; i++) {
                CastleWarsManager.Team team = i < BOTS_PER_TEAM
                        ? CastleWarsManager.Team.SARADOMIN : CastleWarsManager.Team.ZAMORAK;
                BotPlayer bot = newUnconnectedBot("CWLoadBot" + i);
                bot.botPrimaryCombatStyle = i % 3;
                register(bot, team, i % BOTS_PER_TEAM);
                bots.add(bot);
            }
            for (int i = 0; i < 2; i++) {
                Player human = new Player(null);
                human.setUsername("CWLoadHuman" + i);
                register(human, i == 0
                        ? CastleWarsManager.Team.SARADOMIN
                        : CastleWarsManager.Team.ZAMORAK, 1);
            }

            // The game roster is assembled directly for the load test. Skip
            // NPC-spawning initialize(): a full server boot (including NPC
            // definitions) is not part of this isolated fixture.
            setStaticBoolean("initialized", true);
            setStaticBoolean("gameInProgress", true);
            setStaticLong("gameEndMillis", System.currentTimeMillis() + 20 * 60_000L);
            setStaticInt("gameTeamCapacity", BOTS_PER_TEAM + 1);
            long[] durations = new long[TICKS];
            int maxStall = 0;
            PrintStream originalOut = System.out;
            PrintStream originalErr = System.err;
            ByteArrayOutputStream console = new ByteArrayOutputStream();
            PrintStream collected = new PrintStream(console, true, "UTF-8");
            Throwable failure = null;
            try {
                System.setOut(collected);
                System.setErr(collected);
                for (int tick = 0; tick < TICKS; tick++) {
                    long started = System.nanoTime();
                    World.tickCount++;
                    for (BotPlayer bot : bots) {
                        if (!CastleWarsBotRoleAi.process(bot)) {
                            CastleWarsBotAi.process(bot);
                        }
                    }
                    // The non-bot fixtures generate actual pathfinding work
                    // along each team's ground floor, without fake clients.
                    if (tick % 24 == 0) {
                        for (Player player : allPlayers) {
                            if (player instanceof BotPlayer) continue;
                            CastleWarsManager.Team team = gamePlayers.get(player);
                            Position goal = team == CastleWarsManager.Team.SARADOMIN
                                    ? new Position(tick % 48 == 0 ? 2422 : 2424, 3082, 0)
                                    : new Position(tick % 48 == 0 ? 2377 : 2375, 3125, 0);
                            PathFinder.findPath(player, goal.getX(), goal.getY(), true, 0, 0);
                        }
                    }
                    for (Player player : allPlayers) {
                        player.getMovementQueue().process();
                        Position now = player.getPosition().copy();
                        Position before = previous.put(player, now);
                        boolean changed = before != null && !before.equals(now);
                        if (changed) moved.put(player, moved.get(player) + 1);
                        int stall = (!changed && !player.getMovementQueue().getSteps().isEmpty())
                                ? pendingStall.get(player) + 1 : 0;
                        pendingStall.put(player, stall);
                        if (stall > maxStall) maxStall = stall;
                        if (stall >= STALL_TICKS) {
                            throw new AssertionError("Bot/player queued movement stalled for "
                                    + stall + " ticks: " + player.getUsername() + " at " + now);
                        }
                    }
                    durations[tick] = System.nanoTime() - started;
                }
            } catch (Throwable exception) {
                failure = exception;
            } finally {
                System.setOut(originalOut);
                System.setErr(originalErr);
                collected.close();
            }

            String logs = console.toString("UTF-8");
            if (failure != null) {
                System.out.println("LOAD DIAGNOSTICS (truncated): "
                        + logs.substring(0, Math.min(6000, logs.length())));
                throw new AssertionError("Mixed Castle Wars simulation failed", failure);
            }
            require(!logs.contains("[packet-debug]"),
                    "Gameplay trace flooded console with packet-debug lines");
            require(!logs.contains("Exception") && !logs.contains("Error"),
                    "Unexpected server exception in load simulation: "
                            + logs.substring(0, Math.min(4000, logs.length())));
            int activeSara = 0, activeZam = 0;
            int movedHumans = 0;
            for (Player player : allPlayers) {
                int steps = moved.get(player);
                if (player instanceof BotPlayer) {
                    if (steps > 2) {
                        if (gamePlayers.get(player) == CastleWarsManager.Team.SARADOMIN) activeSara++;
                        else activeZam++;
                    }
                } else if (steps > 2) {
                    movedHumans++;
                }
            }
            require(activeSara >= 2 && activeZam >= 2,
                    "Too few bots navigated: Saradomin=" + activeSara + " Zamorak=" + activeZam);
            require(movedHumans == 2, "Both synthetic human movement paths must progress");
            long[] sorted = durations.clone();
            Arrays.sort(sorted);
            long p95 = sorted[(int)(TICKS * 0.95) - 1];
            long max = sorted[TICKS - 1];
            long total = 0;
            for (long value : durations) total += value;
            System.out.println("CASTLE_WARS_LOAD_METRICS bots=12 simulatedHumans=2 ticks=" + TICKS
                    + " avgMs=" + String.format(java.util.Locale.ROOT, "%.3f",
                        total / (double)TICKS / 1_000_000.0)
                    + " p95Ms=" + String.format(java.util.Locale.ROOT, "%.3f", p95 / 1_000_000.0)
                    + " maxMs=" + String.format(java.util.Locale.ROOT, "%.3f", max / 1_000_000.0)
                    + " movingBotsSara=" + activeSara + " movingBotsZam=" + activeZam
                    + " movingHumanFixtures=" + movedHumans + " maxQueuedStallTicks=" + maxStall
                    + " packetDebugLines=0");
            require(p95 < TICK_BUDGET_NANOS,
                    "p95 AI plus movement budget exceeds the 600ms server tick");
            require(max < TICK_BUDGET_NANOS * 3,
                    "Single server-side AI/movement frame exceeded 1800ms");

            // Fast-forward the 20-minute match deadline and verify the complete
            // participant roster is cleared. This is NOT 20 minutes of wall time.
            setStaticLong("gameEndMillis", System.currentTimeMillis() - 1L);
            setStaticLong("lastProcessedSecond", -1L);
            CastleWarsManager.process();
            require(!CastleWarsManager.isGameInProgress(), "Match did not end");
            require(gamePlayers.isEmpty(), "Match end retained stale participants");
            System.out.println("CASTLE_WARS_LOAD_RESULT PASS (accelerated match end, no live client)");
        } finally {
            for (Player player : allPlayers) {
                try {
                    if (player.getIndex() >= 0) {
                        // This is a synthetic fixture without login credentials.
                        // Never run persistence or disconnect/save side effects.
                        World.getPlayers()[player.getIndex()] = null;
                        player.setIndex(-1);
                    }
                } catch (Exception ignored) {}
                try {
                    SocketChannel channel = (SocketChannel) socket.get(player);
                    if (channel != null) channel.close();
                } catch (Exception ignored) {}
                if (player instanceof BotPlayer) {
                    CastleWarsBotAi.clear((BotPlayer)player);
                    CastleWarsBotRoleAi.clear((BotPlayer)player);
                }
            }
            gamePlayers.clear();
            setStaticBoolean("gameInProgress", false);
            setStaticBoolean("initialized", false);
            World.tickCount = 0;
        }
    }

    private static BotPlayer newUnconnectedBot(String name) throws Exception {
        // Construct the ordinary Player state without invoking BotPlayer's
        // disk-backed login/cheat-command constructor. Java 8 CI only.
        Constructor<Player> base = Player.class.getDeclaredConstructor(SelectionKey.class);
        Constructor<?> fake = ReflectionFactory.getReflectionFactory()
                .newConstructorForSerialization(BotPlayer.class, base);
        fake.setAccessible(true);
        BotPlayer bot = (BotPlayer) fake.newInstance((Object)null);
        bot.isBot = true;
        bot.botEnabled = true;
        bot.setUsername(name);
        bot.setNameHash(com.rs2.util.TextUtil.encodeNameHash(name.toLowerCase()));
        return bot;
    }

    private static void register(Player player, CastleWarsManager.Team team, int offset)
            throws Exception {
        player.setClientBuild(443);
        // Closed socket discards outgoing packets without network effects;
        // this measures server decisions, not actual client packet latency.
        socket.set(player, SocketChannel.open());
        ((SocketChannel)socket.get(player)).close();
        player.setPosition(team == CastleWarsManager.Team.SARADOMIN
                ? new Position(2424 + offset % 3, 3076 + offset / 3, 1)
                : new Position(2371 + offset % 3, 3130 + offset / 3, 1));
        World.registerPlayer(player);
        registered.setBoolean(player, true);
        gamePlayers.put(player, team);
        allPlayers.add(player);
        previous.put(player, player.getPosition().copy());
        moved.put(player, 0);
        pendingStall.put(player, 0);
    }

    private static void setStaticLong(String name, long value) throws Exception {
        Field f = CastleWarsManager.class.getDeclaredField(name);
        f.setAccessible(true);
        f.setLong(null, value);
    }
    private static void setStaticBoolean(String name, boolean value) throws Exception {
        Field f = CastleWarsManager.class.getDeclaredField(name);
        f.setAccessible(true);
        f.setBoolean(null, value);
    }
    private static void setStaticInt(String name, int value) throws Exception {
        Field f = CastleWarsManager.class.getDeclaredField(name);
        f.setAccessible(true);
        f.setInt(null, value);
    }
    private static void require(boolean ok, String message) {
        if (!ok) throw new AssertionError(message);
    }
}
