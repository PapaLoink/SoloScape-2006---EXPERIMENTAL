package com.rs2;

import com.rs2.BotLoginBatchTask;
import com.rs2.CacheCoordinateTranslator;
import com.rs2.ConfigFile;
import com.rs2.ConnectionThrottle;
import com.rs2.HiscoresDatabase;
import com.rs2.LanDiscoveryService;
import com.rs2.MinuteMaintenanceTickTask;
import com.rs2.ServerSettings;
import com.rs2.StartingRareItemList;
import com.rs2.bot.BotPlayer;
import com.rs2.bot.BotTaskDefinition;
import com.rs2.bot.ClanWarsBotManager;
import com.rs2.bot.combat.BotCombatLoadoutManager;
import com.rs2.bot.combat.WildernessBotSettings;
import com.rs2.cache.CacheStore;
import com.rs2.cache.InterfaceDefinition;
import com.rs2.launcher.ControlPanel;
import com.rs2.model.EntityTargetMovement;
import com.rs2.model.GameplayHelper;
import com.rs2.model.NoopStartupHook;
import com.rs2.model.Position;
import com.rs2.model.World;
import com.rs2.model.area.MultiwayAreaDefinition;
import com.rs2.model.c.ProjectileDefinition;
import com.rs2.model.clue.TreasureTrailManager;
import com.rs2.model.combat.CombatManager;
import com.rs2.model.gameplay.castlewars.CastleWarsManager;
import com.rs2.model.gameplay.duel.DuelSession;
import com.rs2.model.gameplay.magetrainingarena.AlchemistPlaygroundController;
import com.rs2.model.gameplay.magetrainingarena.CreatureGraveyardController;
import com.rs2.model.gameplay.magetrainingarena.EnchantmentChamberController;
import com.rs2.model.gameplay.partyroom.PartyRoomManager;
import com.rs2.model.grandexchange.GrandExchangePriceSample;
import com.rs2.model.item.ItemDefinition;
import com.rs2.model.item.ItemStack;
import com.rs2.model.item.consumable.FoodHandler;
import com.rs2.model.message.MessageOfTheWeek;
import com.rs2.model.music.MusicAreaDefinition;
import com.rs2.model.music.MusicTrackDefinition;
import com.rs2.model.npc.NpcDefinition;
import com.rs2.model.npc.drop.NpcDropTable;
import com.rs2.model.objects.ObjectDefinition;
import com.rs2.model.objects.WorldObjectLookup;
import com.rs2.model.player.Player;
import com.rs2.model.player.PlayerConnectionState;
import com.rs2.model.quest.QuestDefinition;
import com.rs2.model.quest.QuestEventRegistry;
import com.rs2.model.shop.ShopManager;
import com.rs2.model.skill.fishing.FishingSpotManager;
import com.rs2.model.skill.guide.SkillGuideManager;
import com.rs2.model.task.TickTask;
import com.rs2.model.travel.DefaultTravelBootstrap;
import com.rs2.model.travel.TravelManager;
import com.rs2.net.DedicatedReactor;
import com.rs2.net.LoginProtocol;
import com.rs2.net.packet.PacketDispatcher;
import com.rs2.util.CharacterFileManager;
import com.rs2.util.db.DatabaseService;
import com.rs2.util.db.SqliteDatabase;
import com.rs2.util.DelayedShutdownTask;
import com.rs2.util.ElapsedTimer;
import com.rs2.util.ProfilerRegistry;
import com.rs2.util.ProfilerTimer;
import com.rs2.util.SaveAllPlayersShutdownHook;
import com.rs2.util.TimestampedPrintStream;
import com.rs2.util.path.ProjectileCollisionMap;
import com.rs2.util.path.WalkingCollisionMap;
import com.rs2.util.plugin.PluginManager;
import java.awt.GraphicsEnvironment;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.AbstractInterruptibleChannel;
import java.nio.channels.spi.AbstractSelectableChannel;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Server
implements Runnable {
    private static Server instance;
    private final String bindHost;
    private final int port;
    private final int cycleMillis;
    private static long elapsedMinutes;
    public static final int DEFAULT_MINUTE_INTERVAL_TICKS = 100;
    private static MinuteMaintenanceTickTask minuteMaintenanceTickTask;
    private static ArrayList startingRareItems;
    public static ArrayList trackedRareItems;
    private Selector selector;
    private InetSocketAddress bindAddress;
    private ServerSocketChannel serverSocketChannel;
    private ElapsedTimer cycleTimer;
    private final Queue loginQueue = new ConcurrentLinkedQueue();
    private static final Queue disconnectQueue;
    private static volatile long idleLogoutConfigModified = Long.MIN_VALUE;
    private static volatile long idleLogoutConfigLength = Long.MIN_VALUE;
    private static Thread serverThread;
    private static Thread engineThread;
    public static int serverStatus;
    public static boolean shutdownRequested;
    public static int runtimeMinutes;
    public boolean backupPending = false;
    private boolean hourlyBackupArmed = false;
    public static boolean halloweenEventActive;
    public static boolean christmasEventActive;
    public static boolean easterEventActive;
    public static int messageOfTheWeekIndex;
    private static int botLoginBatchIndex;
    private static int configuredBotCount;
    public static int botLoginBatchSize;
    public static int botLoginBatchIntervalTicks;
    private static int wildernessBotLoginCount;
    private static int skillingBotLoginCount;
    private static int progressiveBotLoginCount;
    private static int tradeBotLoginCount;
    private static int clanWarsBotLoginCount;
    private static int minigameBotLoginCount;
    private static int otherBotLoginCount;
    public static int onlinePlayerCount;
    public static int adminPlayerCount;
    public static int moderatorPlayerCount;

    static {
        new TravelManager(new DefaultTravelBootstrap());
        startingRareItems = new StartingRareItemList();
        trackedRareItems = new ArrayList();
        disconnectQueue = new LinkedList();
        serverStatus = 0;
        shutdownRequested = false;
        runtimeMinutes = 0;
        halloweenEventActive = false;
        christmasEventActive = false;
        easterEventActive = false;
        messageOfTheWeekIndex = 0;
        botLoginBatchIndex = 0;
        configuredBotCount = 0;
        botLoginBatchSize = 150;
        botLoginBatchIntervalTicks = 5;
        wildernessBotLoginCount = 0;
        skillingBotLoginCount = 0;
        progressiveBotLoginCount = 0;
        tradeBotLoginCount = 0;
        clanWarsBotLoginCount = 0;
        minigameBotLoginCount = 0;
        otherBotLoginCount = 0;
        onlinePlayerCount = 0;
        adminPlayerCount = 0;
        moderatorPlayerCount = 0;
    }

    private Server(String bindHost, int port, int value22) {
        this.bindHost = bindHost;
        this.port = port;
        this.cycleMillis = 600;
    }

    public static void loadConfig() {
        String configPath = "./config/server.cfg";
        File configFile = new File(configPath);
        if (!configFile.exists()) {
            ConfigFile.writeDefaultConfig();
        }
        BufferedReader bufferedReader = null;
        try {
            bufferedReader = new BufferedReader(new FileReader(configPath));
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                if (!line.startsWith("[")) continue;
                String[] parts = line.split(";");
                String key = parts[0].replace("[", "").replace("]", "");
                String[] values = new String[parts.length - 1];
                int index = 0;
                while (index < parts.length - 1) {
                    values[index] = parts[index + 1];
                    ++index;
                }
                ConfigFile.applyConfigEntry(key, values);
            }
        }
        catch (FileNotFoundException fileNotFoundException) {
            System.out.println(String.valueOf(configPath) + ": file not found.");
        }
        catch (IOException iOException) {
            System.out.println(String.valueOf(configPath) + ": error loading file.");
        }
        finally {
            if (bufferedReader != null) {
                try {
                    bufferedReader.close();
                }
                catch (IOException iOException) {
                    iOException.printStackTrace();
                }
            }
        }
    }

    /**
     * Isolated parser for the server-side AFK switch. The last configured
     * value wins, matching the normal loadConfig() behavior. A malformed value
     * does not silently enable AFK logout or override the current setting.
     */
    public static Boolean parseIdleLogoutSetting(BufferedReader reader) throws IOException {
        Boolean result = null;
        String line;
        while ((line = reader.readLine()) != null) {
            line = line.trim();
            if (!line.startsWith("[IDLE_LOGOUT]")) continue;
            String[] parts = line.split(";", -1);
            if (parts.length != 2 || !parts[0].trim().equals("[IDLE_LOGOUT]")
                    || (!parts[1].trim().equals("0") && !parts[1].trim().equals("1"))) {
                throw new IOException("Invalid idle logout config: " + line
                        + " (expected [IDLE_LOGOUT];0 or [IDLE_LOGOUT];1)");
            }
            result = parts[1].trim().equals("1");
        }
        return result;
    }

    /**
     * The desktop control panel loads server.cfg once in its constructor.
     * Watch the file's metadata on each game tick so changing IDLE_LOGOUT
     * takes effect without requiring the user to close the Java GUI.
     */
    private static synchronized void reloadIdleLogoutIfChanged() {
        File file = new File("./config/server.cfg");
        if (!file.isFile()) return;
        long modified = file.lastModified();
        long length = file.length();
        if (modified == idleLogoutConfigModified && length == idleLogoutConfigLength)
            return;
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            Boolean enabled = parseIdleLogoutSetting(reader);
            if (enabled == null) {
                System.err.println("[config] No [IDLE_LOGOUT] entry found in "
                        + file.getAbsolutePath() + "; current setting remains "
                        + (ServerSettings.idleLogoutEnabled ? "enabled" : "disabled"));
            } else {
                ServerSettings.idleLogoutEnabled = enabled;
                System.out.println("[config] IDLE_LOGOUT="
                        + (enabled ? "1 (enabled)" : "0 (disabled)")
                        + " from " + file.getAbsolutePath());
            }
            idleLogoutConfigModified = modified;
            idleLogoutConfigLength = length;
        } catch (IOException exception) {
            // Keep the last known valid setting while the file is being edited.
            // A subsequent metadata change triggers another parse attempt.
            System.err.println("[config] Unable to reload IDLE_LOGOUT: " + exception.getMessage());
            idleLogoutConfigModified = modified;
            idleLogoutConfigLength = length;
        }
    }

    public static void main(String[] args) {
        // server.cfg is normally loaded only when the control panel is created.
        // Also pick up edits made while that window has remained open.
        reloadIdleLogoutIfChanged();
        String bindHost = System.getProperty("prs.bindHost", "0.0.0.0");
        runtimeMinutes = 0;
        shutdownRequested = false;
        ConnectionThrottleSettings.connectionsEnabled = true;
        halloweenEventActive = false;
        christmasEventActive = false;
        easterEventActive = false;
        onlinePlayerCount = 0;
        adminPlayerCount = 0;
        moderatorPlayerCount = 0;
        disconnectQueue.clear();
        trackedRareItems.clear();
        int value = ServerSettings.serverPort;
        Server server = new Server(bindHost, value, 600);
        if (instance != null) {
            throw new IllegalStateException("Singleton already set!");
        }
        instance = server;
        serverStatus = 1;
        serverThread = new Thread(instance);
        serverThread.start();
    }

    public static Queue getDisconnectQueue() {
        return disconnectQueue;
    }

    public final void queueLogin(Player player) {
        long nameHash = player.getNameHash();
        if (this.loginQueue.contains(nameHash)) {
            System.out.println(String.valueOf(player.getUsername()) + " was already on login queue!");
            player.disconnect();
            return;
        }
        this.loginQueue.add(player);
    }
    @Override
    public void run() {
        try {
            Object value;
            engineThread = Thread.currentThread();
            engineThread.setName("ServerEngine");
            System.setOut(new TimestampedPrintStream(System.out));
            System.setErr(new TimestampedPrintStream((OutputStream)System.err, "./data/err.log"));
            this.bindAddress = new InetSocketAddress(this.bindHost, this.port);
            System.out.println("Starting " + ServerSettings.serverName + " on " + this.bindAddress + "...");
            Calendar calendar = Calendar.getInstance();
            Calendar calendar2 = Calendar.getInstance();
            calendar2.set(2, 9);
            calendar2.set(5, 31);
            long timeInMillis = calendar.getTimeInMillis();
            long timeInMillis2 = calendar2.getTimeInMillis();
            long value2 = timeInMillis2 - timeInMillis;
            int value3 = (int)Math.abs(value2 / 24L / 60L / 60L / 1000L);
            if (value3 <= 7) {
                halloweenEventActive = true;
            }
            calendar = Calendar.getInstance();
            calendar2 = Calendar.getInstance();
            calendar2.set(2, 11);
            calendar2.set(5, 24);
            timeInMillis = calendar.getTimeInMillis();
            timeInMillis2 = calendar2.getTimeInMillis();
            value2 = timeInMillis2 - timeInMillis;
            value3 = (int)Math.abs(value2 / 24L / 60L / 60L / 1000L);
            if (value3 <= 7) {
                christmasEventActive = true;
            }
            calendar = Calendar.getInstance();
            int value4 = calendar.get(1);
            Calendar calendar3 = Calendar.getInstance();
            boolean enabled = false;
            int value5 = (int)((((double)value4 / 38.0 - (double)(value4 / 38)) * 1440.0 % 60.0 / 2.0 + 56.0) % 30.0) + 1;
            if ((value4 - 5) % 19 == 0) {
                value5 += 28;
                enabled = true;
            }
            calendar3.set(value4, 4, value5);
            if (enabled && calendar3.get(7) == 7) {
                calendar3.add(5, 7);
            }
            calendar3.add(5, -calendar3.get(7) - 34);
            long timeInMillis3 = calendar.getTimeInMillis();
            long timeInMillis4 = calendar3.getTimeInMillis();
            long value6 = timeInMillis4 - timeInMillis3;
            int value7 = (int)Math.abs(value6 / 24L / 60L / 60L / 1000L);
            if (value7 <= 7) {
                easterEventActive = true;
            }
            serverStatus = 1;
            Server.refreshControlPanelStats();
            MessageOfTheWeek.loadAndRotateMessage();
            Object value8 = new SaveAllPlayersShutdownHook();
            Runtime.getRuntime().addShutdownHook((Thread)value8);
            PacketDispatcher.registerHandlers();
            if (ServerSettings.cacheVersion != 443) {
                CacheStore.initializeCacheStore();
            }
            ProjectileDefinition.registerNpcCombatDefinitions();
            QuestDefinition.loadDefinitions();
            ItemDefinition.loadDefinitions();
            NpcDefinition.loadDefinitions();
            // Convert old binary saves after definitions are ready, before accepting logins.
            CharacterFileManager.migrateLegacyCharacters();
            FoodHandler.loadPotionDefinitions();
            ShopManager.loadShops();
            InterfaceDefinition.loadDefinitions();
            PluginManager.loadPlugins();
            new WorldObjectLookup();
            WorldObjectLookup.loadWorldObjects();
            WalkingCollisionMap.loadCollisionMaps();
            ProjectileCollisionMap.loadCollisionMaps();
            CacheCoordinateTranslator.detectDungeonCoordinateShift();
            CombatManager.initialize();
            value8 = this;
            try {
                value = new BufferedReader(new FileReader("./data/minutes.log"));
                elapsedMinutes = Integer.parseInt(((BufferedReader)value).readLine());
            }
            catch (Exception exception) {
                value = exception;
                exception.printStackTrace();
            }
            minuteMaintenanceTickTask = new MinuteMaintenanceTickTask((Server)value8, DEFAULT_MINUTE_INTERVAL_TICKS);
            World.scheduleTickTask(minuteMaintenanceTickTask);
            GameplayHelper.loadGroundItemSpawns();
            FishingSpotManager.spawnFishingSpots();
            MusicTrackDefinition.loadDefinitions();
            MusicAreaDefinition.loadDefinitions();
            MultiwayAreaDefinition.loadDefinitions();
            PartyRoomManager.loadPartyChest();
            NpcDropTable.loadDropTables();
            GameplayHelper.loadNpcSpawns();
            // March 2006 Romeo & Juliet rework supporting characters.
            com.rs2.model.quest.impl.RomeoJulietSupportingNpcs.spawnMissingNpcs();
            if (ServerSettings.grandExchangeEnabled) {
                GameplayHelper.spawnNpc(3863, 3183, 3436, 0, 4);
            }
            TreasureTrailManager.filterRewardItemPools();
            BotCombatLoadoutManager.initializeCombatLoadoutTypes();
            SkillGuideManager.initialize();
            GrandExchangePriceSample.loadPriceSamples();
            new NoopStartupHook();
            com.rs2.model.quest.impl.QuestExtras.start();
            value8 = CacheStore.getInstance();
            if (value8 != null) {
                ((CacheStore)value8).close();
            }
            AlchemistPlaygroundController.startCupboardRotation();
            EnchantmentChamberController.startBonusColorCycle();
            CreatureGraveyardController.startFallingBoneHazards();
            value8 = this;
            this.selector = Selector.open();
            ((Server)value8).serverSocketChannel = ServerSocketChannel.open();
            DedicatedReactor.setInstance(new DedicatedReactor(Selector.open()));
            ((Server)value8).serverSocketChannel.configureBlocking(false);
            ((Server)value8).serverSocketChannel.socket().bind(((Server)value8).bindAddress);
            value = DedicatedReactor.getInstance();
            synchronized (value) {
                DedicatedReactor.getInstance().getSelector().wakeup();
                ((Server)value8).serverSocketChannel.register(DedicatedReactor.getInstance().getSelector(), 16);
            }
            DedicatedReactor.getInstance().start();
            ((Server)value8).cycleTimer = new ElapsedTimer();
            serverStatus = 2;
            long value9 = System.currentTimeMillis();
            shutdownRequested = false;
            for (Object itemStackObject : startingRareItems) {
                ItemStack itemStack = (ItemStack)itemStackObject;
                trackedRareItems.add(itemStack);
            }
            System.out.println("Online!");
            BotTaskDefinition.initializeProgressiveTaskPool();
            BotTaskDefinition.initializeTradeAdvertTaskPool();
            BotTaskDefinition.initializeDropPartyTaskPool();
            CastleWarsManager.initialize();
            QuestEventRegistry.initializeEventHooks();
            BotPlayer.removeConfiguredBotNames();
            if (ServerSettings.progressiveBotsPrioritizeExisting && ServerSettings.progressiveBotsEnabled && ServerSettings.progressiveBotCount > 0) {
                BotPlayer.loadExistingProgressiveBotNames();
            }
            BotPlayer.prepareBotNamePools();
            wildernessBotLoginCount = 0;
            skillingBotLoginCount = 0;
            progressiveBotLoginCount = 0;
            tradeBotLoginCount = 0;
            clanWarsBotLoginCount = 0;
            minigameBotLoginCount = 0;
            otherBotLoginCount = 0;
            if (ServerSettings.progressiveBotsEnabled) {
                ServerSettings.walkingBotsEnabled = true;
            }
            if (WildernessBotSettings.wildyBotCount <= 0 && ServerSettings.wildyBotsEnabled) {
                ServerSettings.wildyBotsEnabled = false;
            }
            if (ServerSettings.skillingBotCount <= 0 && ServerSettings.skillingBotsEnabled) {
                ServerSettings.skillingBotsEnabled = false;
            }
            if (ServerSettings.progressiveBotCount <= 0 && ServerSettings.progressiveBotsEnabled) {
                ServerSettings.progressiveBotsEnabled = false;
            }
            if (ServerSettings.tradeBotCount <= 0 && ServerSettings.tradeBotsEnabled) {
                ServerSettings.tradeBotsEnabled = false;
            }
            if (ServerSettings.clanWarsTeamSize <= 0 && ServerSettings.clanWarsBotsEnabled) {
                ServerSettings.clanWarsBotsEnabled = false;
            }
            if (ServerSettings.minigameBotCount <= 0 && ServerSettings.minigameBotsEnabled) {
                ServerSettings.minigameBotsEnabled = false;
            }
            configuredBotCount = 0;
            if (ServerSettings.wildyBotsEnabled) {
                configuredBotCount += WildernessBotSettings.wildyBotCount;
            }
            if (ServerSettings.skillingBotsEnabled) {
                configuredBotCount += ServerSettings.skillingBotCount;
            }
            if (ServerSettings.progressiveBotsEnabled) {
                configuredBotCount += ServerSettings.progressiveBotCount;
            }
            if (ServerSettings.tradeBotsEnabled) {
                configuredBotCount += ServerSettings.tradeBotCount;
            }
            if (ServerSettings.clanWarsBotsEnabled) {
                configuredBotCount += ServerSettings.clanWarsTeamSize << 1;
            }
            if (ServerSettings.minigameBotsEnabled) {
                configuredBotCount += ServerSettings.minigameBotCount;
            }
            if (ServerSettings.otherBotsEnabled) {
                configuredBotCount += ServerSettings.otherBotCount;
            }
            botLoginBatchIndex = 0;
            int value10 = configuredBotCount / botLoginBatchSize;
            if (value10 == 0) {
                Server.loginAllConfiguredBots();
            } else {
                World.getTaskScheduler().schedule(new BotLoginBatchTask(botLoginBatchIntervalTicks, value10));
            }
            if (ServerSettings.lanConnectionsEnabled) {
                LanDiscoveryService.startListener();
            }
            if (ServerSettings.sqlitePlayerSaveEnabled) {
                SqliteDatabase.initialize();
                DatabaseService.setInstance(new DatabaseService(8, ServerSettings.databaseDriverClass, ServerSettings.databaseJdbcUrl, ServerSettings.databaseUsername, ServerSettings.databasePassword));
            }
            if (ServerSettings.sqliteHiscoresEnabled) {
                HiscoresDatabase.connect();
            }
            if (Boolean.getBoolean("prs.traceGameplay")) {
                System.out.println("[server-trace] entering main loop interrupted=" + Thread.currentThread().isInterrupted() + " shutdownRequested=" + shutdownRequested);
            }
            HiscoresHttp.start();
            while (!Thread.interrupted() && !shutdownRequested) {
                try {
                    long value11 = System.currentTimeMillis() - value9;
                    runtimeMinutes = (int)(value11 / 1000L / 60L);
                    if (ServerSettings.backupCharactersEnabled) {
                        if (runtimeMinutes % 60 == 0 && this.hourlyBackupArmed) {
                            if (!this.backupPending && World.getPlayerCount() > 0) {
                                this.backupPending = true;
                            }
                            if (this.backupPending) {
                                CharacterFileManager.saveAllPlayers();
                                CharacterFileManager.createTimestampedBackup();
                                System.out.println("Creating hourly backups of character files.");
                            } else {
                                System.out.println("No backup files created, reason: No players have been online");
                            }
                            this.backupPending = false;
                            this.hourlyBackupArmed = false;
                        }
                        if (runtimeMinutes % 60 != 0) {
                            this.hourlyBackupArmed = true;
                        }
                    }
                    if (!(ServerSettings.wildyBotsEnabled || ServerSettings.skillingBotsEnabled || ServerSettings.tradeBotsEnabled)) {
                    }
                    Server.refreshControlPanelStats();
                    this.processGameCycle();
                    this.sleepUntilNextCycle();
                }
                catch (Exception exception) {
                    CharacterFileManager.saveAllPlayers();
                    if (ServerSettings.sqliteHiscoresEnabled) {
                        HiscoresDatabase.disconnect();
                    }
                    exception.printStackTrace();
                }
            }
            if (Boolean.getBoolean("prs.traceGameplay")) {
                System.out.println("[server-trace] main loop exited interrupted=" + Thread.currentThread().isInterrupted() + " shutdownRequested=" + shutdownRequested);
            }
            CharacterFileManager.saveAllPlayers();
            World.resetForServerRestart();
            if (ServerSettings.sqliteHiscoresEnabled) {
                HiscoresDatabase.disconnect();
            }
            DatabaseService.shutdownInstance();
            LanDiscoveryService.stopListener();
            DedicatedReactor.shutdown();
            try {
                if (this.serverSocketChannel != null) {
                    this.serverSocketChannel.close();
                    this.serverSocketChannel = null;
                }
            }
            catch (IOException iOException) {
                iOException.printStackTrace();
            }
            try {
                if (this.selector != null) {
                    this.selector.close();
                    this.selector = null;
                }
            }
            catch (IOException iOException) {
                iOException.printStackTrace();
            }

            serverStatus = 0;
            runtimeMinutes = 0;
            shutdownRequested = false;
            onlinePlayerCount = 0;
            adminPlayerCount = 0;
            moderatorPlayerCount = 0;
            instance = null;
            serverThread = null;
            engineThread = null;
            Server.refreshControlPanelStats();
            if (Boolean.getBoolean("prs.traceGameplay")) {
                System.out.println("[server-trace] game server stopped; launcher remains open");
            }
        }
        catch (Exception exception) {
            exception.printStackTrace();
            serverStatus = 0;
            shutdownRequested = false;
            instance = null;
            serverThread = null;
            engineThread = null;
            try {
                LanDiscoveryService.stopListener();
                DedicatedReactor.shutdown();
                DatabaseService.shutdownInstance();
                if (this.serverSocketChannel != null) {
                    this.serverSocketChannel.close();
                    this.serverSocketChannel = null;
                }
                if (this.selector != null) {
                    this.selector.close();
                    this.selector = null;
                }
            }
            catch (Exception cleanupException) {
                cleanupException.printStackTrace();
            }
            Server.refreshControlPanelStats();
        }
        PluginManager.shutdownGlobalPlugins();
        if (Boolean.getBoolean("prs.exitOnServerShutdown")) {
            System.exit(0);
        }
    }

    private static void loginAllConfiguredBots() {
        int initialValue = 1;
        while (initialValue <= configuredBotCount) {
            if (initialValue > 0 && initialValue < ServerSettings.botLoginIdLimit) {
                BotPlayer.createBotFromPool(initialValue, "zxcvbn", Server.selectNextBotType());
            }
            ++initialValue;
        }
    }

    public static int selectNextBotType() {
        if (ServerSettings.wildyBotsEnabled && wildernessBotLoginCount < WildernessBotSettings.wildyBotCount) {
            ++wildernessBotLoginCount;
            return 1;
        }
        if (ServerSettings.skillingBotsEnabled && skillingBotLoginCount < ServerSettings.skillingBotCount) {
            ++skillingBotLoginCount;
            return 0;
        }
        if (ServerSettings.progressiveBotsEnabled && progressiveBotLoginCount < ServerSettings.progressiveBotCount) {
            ++progressiveBotLoginCount;
            return 4;
        }
        if (ServerSettings.tradeBotsEnabled && tradeBotLoginCount < ServerSettings.tradeBotCount) {
            ++tradeBotLoginCount;
            return 2;
        }
        if (ServerSettings.clanWarsBotsEnabled && clanWarsBotLoginCount < ServerSettings.clanWarsTeamSize << 1) {
            int value = ClanWarsBotManager.clanWarsTeamOneBots.size() == ServerSettings.clanWarsTeamSize ? 6 : 5;
            ++clanWarsBotLoginCount;
            return value;
        }
        if (ServerSettings.minigameBotsEnabled && minigameBotLoginCount < ServerSettings.minigameBotCount) {
            ++minigameBotLoginCount;
            return 7;
        }
        if (ServerSettings.otherBotsEnabled && otherBotLoginCount < ServerSettings.otherBotCount) {
            ++otherBotLoginCount;
            return 3;
        }
        System.out.println("ERROR at getBotType, did not find type for bot!");
        return -1;
    }

    private static void refreshControlPanelStats() {
        onlinePlayerCount = World.getPlayerCount();
        adminPlayerCount = World.getAdminCount();
        moderatorPlayerCount = World.getModeratorCount();
        if (!GraphicsEnvironment.isHeadless()) {
            ControlPanel.refreshStatusDisplay();
        }
    }

    public static void broadcastServerMessage(String text2) {
        Player[] playerArray = World.getPlayers();
        int length = playerArray.length;
        int index = 0;
        while (index < length) {
            Player player = playerArray[index];
            if (player != null && player.getIndex() != -1) {
                player.packetSender.sendGameMessage("[SERVER]: " + text2);
            }
            ++index;
        }
    }

    public static void acceptConnection(SelectionKey selectionKey) {
        try {
            Object serverSocketChannel = (ServerSocketChannel)selectionKey.channel();
            if ((serverSocketChannel = ((ServerSocketChannel)serverSocketChannel).accept()) == null) {
                return;
            }
            if (!ConnectionThrottle.tryAcquireConnectionSlot(((SocketChannel)serverSocketChannel).socket().getInetAddress().getHostAddress())) {
                ((AbstractInterruptibleChannel)serverSocketChannel).close();
                return;
            }
            // Keep small gameplay and diagnostic packets from waiting on Nagle.
            ((SocketChannel)serverSocketChannel).socket().setTcpNoDelay(true);
            ((AbstractSelectableChannel)serverSocketChannel).configureBlocking(false);
            selectionKey = ((SelectableChannel)serverSocketChannel).register(selectionKey.selector(), 1);
            serverSocketChannel = new Player(selectionKey);
            selectionKey.attach(serverSocketChannel);
        }
        catch (IOException iOException) {
            throw new RuntimeException(iOException);
        }
    }

    private void processGameCycle() {
        // Hot-reload just this setting, without overwriting the other live
        // settings managed by the control panel.
        reloadIdleLogoutIfChanged();
        ProfilerTimer elapsedTimer = ProfilerRegistry.getTimer("loginQueue");
        elapsedTimer.start();
        Player player;
        while ((player = (Player)this.loginQueue.poll()) != null) {
            try {
                player.processPostLogin();
                player.setConnectionState(PlayerConnectionState.IN_GAME);
            }
            catch (Exception exception) {
                exception.printStackTrace();
                player.disconnect();
            }
            finally {
                LoginProtocol.releaseLoginUsername(player);
            }
        }
        elapsedTimer.stop();
        elapsedTimer = ProfilerRegistry.getTimer("handleNetworkPackets");
        elapsedTimer.start();
        try {
            this.selector.selectNow();
        }
        catch (IOException iOException) {
            throw new RuntimeException(iOException);
        }
        for (Object selectedKeyObject : this.selector.selectedKeys()) {
            SelectionKey selectionKey = (SelectionKey)selectedKeyObject;
            if (!selectionKey.isValid() || !selectionKey.isReadable()) continue;
            PacketDispatcher.processIncoming((Player)selectionKey.attachment());
        }
        elapsedTimer.stop();
        PluginManager.tickGlobalPlugins();
        World.processTick();
        elapsedTimer = ProfilerRegistry.getTimer("disconnectingPlayers");
        elapsedTimer.start();
        synchronized (Server.disconnectQueue) {
            Iterator iterator = Server.disconnectQueue.iterator();
            while (iterator.hasNext()) {
                Player disconnectingPlayer = (Player)iterator.next();
                boolean keepQueued = disconnectingPlayer.grandExchangeSettlementInProgress || !disconnectingPlayer.logoutPacketSent && (disconnectingPlayer.getConnectionState() != PlayerConnectionState.DISCONNECTING || disconnectingPlayer.getDisconnectGraceExpiresAtMillis() >= System.currentTimeMillis()) && !disconnectingPlayer.getRecentCombatTimer().hasElapsed();
                if (keepQueued) continue;
                try {
                    try {
                        ProfilerTimer cleanupTimer = ProfilerRegistry.getTimer("tradeDecline");
                        cleanupTimer.start();
                        if (disconnectingPlayer.getTradePartner() != null) {
                            GameplayHelper.declineTrade(disconnectingPlayer);
                        }
                        PartyRoomManager.returnStagedChestItems(disconnectingPlayer);
                        cleanupTimer.stop();
                        cleanupTimer = ProfilerRegistry.getTimer("duelDecline");
                        cleanupTimer.start();
                        if (disconnectingPlayer.getDuelSession().getOpponent() != null) {
                            if (disconnectingPlayer.getDuelSession().isStarted()) {
                                DuelSession.finishDuelVictory(disconnectingPlayer.getDuelSession().getOpponent(), disconnectingPlayer);
                            } else {
                                disconnectingPlayer.getDuelSession().getOpponent().getDuelController().resetDuel(true);
                                disconnectingPlayer.getDuelController().resetDuel(true);
                            }
                        }
                        disconnectingPlayer.clearTemporaryCutsceneNpcs();
                        cleanupTimer.stop();
                        cleanupTimer = ProfilerRegistry.getTimer("petUnregister");
                        cleanupTimer.start();
                        if (disconnectingPlayer.getPetManager().getActivePetNpc() != null) {
                            disconnectingPlayer.getPetManager().pickupPet();
                        }
                        cleanupTimer.stop();
                        cleanupTimer = ProfilerRegistry.getTimer("endFightCave");
                        cleanupTimer.start();
                        disconnectingPlayer.getFightCaveController().cleanupIfInFightCave();
                        cleanupTimer.stop();
                        cleanupTimer = ProfilerRegistry.getTimer("unlockMovement");
                        cleanupTimer.start();
                        disconnectingPlayer.getMovementQueue();
                        cleanupTimer.stop();
                        cleanupTimer = ProfilerRegistry.getTimer("resetFollowing");
                        cleanupTimer.start();
                        if (disconnectingPlayer.getMovementTarget() != null) {
                            EntityTargetMovement.clearMovementTarget(disconnectingPlayer);
                        }
                        cleanupTimer.stop();
                        cleanupTimer = ProfilerRegistry.getTimer("logoutPrivatemessage");
                        cleanupTimer.start();
                        disconnectingPlayer.getSocialManager().refreshFriendStatuses(true);
                        cleanupTimer.stop();
                        if (disconnectingPlayer.isInArea(1790, 2045, 4800, 4915)) {
                            disconnectingPlayer.moveTo(new Position(3053, 3246, 0));
                        }
                        CharacterFileManager.savePlayer(disconnectingPlayer);
                        CharacterFileManager.refreshLiveHiscoreRecord(disconnectingPlayer);
                        if (ServerSettings.sqliteHiscoresEnabled) {
                            HiscoresDatabase.savePlayer(disconnectingPlayer);
                        }
                    }
                    catch (Exception exception) {
                        exception.printStackTrace();
                    }
                }
                finally {
                    ProfilerTimer unregisterTimer = ProfilerRegistry.getTimer("unregisterPlayer");
                    unregisterTimer.start();
                    World.unregisterPlayer(disconnectingPlayer);
                    unregisterTimer.stop();
                }
                iterator.remove();
            }
        }
        elapsedTimer.stop();
    }

    private void sleepUntilNextCycle() {
        try {
            long elapsedMillis = this.cycleTimer.elapsedMillis();
            long remainingMillis = (long)this.cycleMillis - elapsedMillis;
            if (remainingMillis > 0L && remainingMillis <= 600L) {
                Thread.sleep(remainingMillis);
            } else {
                long loadPercent = 100L + (Math.abs(remainingMillis) - (long)this.cycleMillis) / 6L;
                System.out.println("[WARNING] Server Load is at " + loadPercent + "%");
                System.out.println("[PROFILE] cycle=" + elapsedMillis
                        + "ms phases={" + ProfilerRegistry.formatSnapshot() + "}");
                System.out.println();
            }
        }
        catch (Exception exception) {
            exception.printStackTrace();
        }
        finally {
            this.cycleTimer.reset();
            int packetId = 0;
            while (packetId < 256) {
                ProfilerTimer packetTimer = PacketDispatcher.packetTimers[packetId];
                packetTimer.getAccumulatedMillis();
                packetTimer.reset();
                ++packetId;
            }
            ProfilerRegistry.resetAll();
        }
    }

    public static void scheduleShutdown(boolean enabled2) {
        int value = 300;
        if (World.getPlayerCount() == 0 || enabled2) {
            value = 1;
        }
        Player[] playerArray = World.getPlayers();
        int length = playerArray.length;
        int index = 0;
        while (index < length) {
            Player player = playerArray[index];
            if (player != null && player.getIndex() != -1) {
                player.packetSender.sendSystemUpdateTimer(value / 30 * 50 + 1);
            }
            ++index;
        }
        Runnable runnable = new DelayedShutdownTask(value + 3);
        runnable = new Thread(runnable);
        ((Thread)runnable).start();
        serverStatus = 3;
    }

    public static void setElapsedMinutes(long elapsedMinutes) {
        Server.elapsedMinutes = elapsedMinutes;
        try {
            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter("./data/minutes.log"));
            bufferedWriter.write(Long.toString(elapsedMinutes));
            bufferedWriter.close();
            return;
        }
        catch (IOException iOException) {
            IOException iOException2 = iOException;
            iOException.printStackTrace();
            return;
        }
    }

    public static long getElapsedMinutes() {
        return elapsedMinutes;
    }

    public static int getMinuteIntervalTicks() {
        return minuteMaintenanceTickTask == null
                ? DEFAULT_MINUTE_INTERVAL_TICKS
                : minuteMaintenanceTickTask.getIntervalTicks();
    }

    public static void setMinuteIntervalTicks(int intervalTicks) {
        if (intervalTicks < 1) {
            throw new IllegalArgumentException("Tick interval must be at least 1.");
        }
        if (minuteMaintenanceTickTask == null) {
            throw new IllegalStateException("Minute maintenance task has not started.");
        }
        minuteMaintenanceTickTask.setIntervalTicks(intervalTicks);
        minuteMaintenanceTickTask.setRemainingTicks(intervalTicks);
        com.rs2.model.task.CycleEventHandler.getInstance().retimeFarmingEvents(intervalTicks);
    }

    public static Server getInstance() {
        return instance;
    }

    public final Selector getSelector() {
        return this.selector;
    }

    static int getBotLoginBatchIndex() {
        return botLoginBatchIndex;
    }

    static int getConfiguredBotCount() {
        return configuredBotCount;
    }

    static void setBotLoginBatchIndex(int index) {
        botLoginBatchIndex = index;
    }
}
