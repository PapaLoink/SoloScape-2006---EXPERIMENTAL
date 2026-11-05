package com.rs2.model;

import com.rs2.Server;
import com.rs2.ServerSettings;
import com.rs2.model.BotReloginTask;
import com.rs2.model.Entity;
import com.rs2.model.GameplayHelper;
import com.rs2.model.Position;
import com.rs2.model.animation.GraphicEffect;
import com.rs2.model.ground.GroundItemManager;
import com.rs2.model.npc.Npc;
import com.rs2.model.npc.NpcDefinition;
import com.rs2.model.objects.ObjectManager;
import com.rs2.model.objects.WorldObjectRegionIndex;
import com.rs2.model.player.Player;
import com.rs2.model.player.PlayerConnectionState;
import com.rs2.model.player.PlayerUpdateTask;
import com.rs2.model.player.ModernPlayerUpdateTask;
import com.rs2.model.npc.NpcUpdateTask;
import com.rs2.model.task.CycleEventHandler;
import com.rs2.model.task.TaskScheduler;
import com.rs2.model.task.TickTask;
import com.rs2.net.packet.PacketSender;
import com.rs2.util.CharacterFileManager;
import com.rs2.util.GameUtil;
import com.rs2.util.ProfilerRegistry;
import com.rs2.util.ProfilerTimer;
import com.rs2.util.TextUtil;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.concurrent.Executors;

public final class World {
    public static final Player[] players = new Player[ServerSettings.maxPlayers];
    private static Npc[] npcs = new Npc[20000];
    private static TaskScheduler taskScheduler = new TaskScheduler();
    private static NpcDefinition[] npcDefinitions = new NpcDefinition[6433];
    public static int tickCount = 0;
    private static final World instance;
    private WorldObjectRegionIndex objectRegionIndex = new WorldObjectRegionIndex();

    static {
        int runtime = Runtime.getRuntime().availableProcessors();
        Executors.newFixedThreadPool(runtime);
        instance = new World();
    }

    public static void logoutBotAndScheduleRelogin(Player player) {
        if (!player.isBot) {
            return;
        }
        String username = player.getUsername();
        player.packetSender.sendLogout();
        player.disconnect();
        TickTask reloginTask = new BotReloginTask(30, username, player.botMode);
        taskScheduler.schedule(reloginTask);
    }

    /**
     * Revision 443 intentionally bypasses Player.process() to avoid legacy
     * side effects. Keep passive systems alive here: prayer drain and
     * stat/special-energy restoration normally run in Player.process().
     */
    static void processRevision443Player(Player player) {
        player.getPrayerManager().drainPrayerPoints();
        player.getSkillManager().startRestorationTasks();
        // NPCs may move after the client's initial click route.
        // Continue pursuing the target without invoking legacy Player.process().
        player.getTargetMovement().process();
        player.processConnectionTimeout();
    }

    public static synchronized void processTick() {
        Object value;
        Object value2;
        Object value3;
        ++tickCount;
        ProfilerTimer profilerTimer = ProfilerRegistry.getTimer("executeTicks");
        LinkedList taskSnapshot = new LinkedList();
        taskSnapshot.addAll(taskScheduler.getTasks());
        java.util.Iterator taskIterator = taskSnapshot.iterator();
        taskScheduler.getTasks().clear();
        profilerTimer.start();
        while (taskIterator.hasNext()) {
            value3 = (TickTask)taskIterator.next();
            try {
                ((TickTask)value3).tick();
                if (!((TickTask)value3).isActive()) continue;
                taskScheduler.getTasks().add(value3);
            }
            catch (Exception exception) {
                value2 = exception;
                exception.printStackTrace();
            }
        }
        profilerTimer.stop();
        try {
            profilerTimer = ProfilerRegistry.getTimer("groundItemsUpdate");
            profilerTimer.start();
            GroundItemManager.getInstance().run();
            profilerTimer.stop();
            profilerTimer = ProfilerRegistry.getTimer("groundObjectsUpdate");
            profilerTimer.start();
            ObjectManager.getInstance().processObjects();
            profilerTimer.stop();
        }
        catch (Exception exception) {
            value3 = exception;
            exception.printStackTrace();
        }
        profilerTimer = ProfilerRegistry.getTimer("processPlayerLogic");
        profilerTimer.start();
        value3 = new java.util.HashSet<String>();
        Entity[] entityArray = players;
        int length = players.length;
        int index = 0;
        while (index < length) {
            value2 = entityArray[index];
            if (value2 != null) {
                if (((java.util.HashSet)value3).contains(((Player)value2).getUsername())) {
                    System.out.println("Disconnecting: " + ((Player)value2).getUsername() + " Reason: Multilog!");
                    value = value2;
                    ((Player)value).packetSender.sendLogout();
                    ((Player)value2).disconnect();
                } else {
                    ((java.util.HashSet)value3).add(((Player)value2).getUsername());
                    try {
                        if (ServerSettings.clientBuild != 443 || ((Player)value2).isBot) {
                            ((Player)value2).process();
                        } else {
                            processRevision443Player((Player)value2);
                        }
                    }
                    catch (Exception exception) {
                        value = exception;
                        exception.printStackTrace();
                        ((Player)value2).disconnect();
                    }
                }
            }
            ++index;
        }
        profilerTimer.stop();
        profilerTimer = ProfilerRegistry.getTimer("processNPCLogic");
        profilerTimer.start();
        entityArray = npcs;
        length = npcs.length;
        index = 0;
        while (index < length) {
            value2 = entityArray[index];
            if (value2 != null) {
                try {
                    ((Npc)value2).process();
                }
                catch (Exception exception) {
                    value = exception;
                    exception.printStackTrace();
                    World.unregisterNpc((Npc)value2);
                }
            }
            ++index;
        }
        profilerTimer.stop();
        CycleEventHandler.getInstance().process();
        profilerTimer = ProfilerRegistry.getTimer("processMovement");
        profilerTimer.start();
        entityArray = players;
        length = players.length;
        index = 0;
        while (index < length) {
            value2 = entityArray[index];
            if (value2 != null) {
                ((Entity)value2).getMovementQueue().process();
                ((Player)value2).restoreRunEnergy();
            }
            ++index;
        }
        entityArray = npcs;
        length = npcs.length;
        index = 0;
        while (index < length) {
            value2 = entityArray[index];
            if (value2 != null) {
                ((Entity)value2).getMovementQueue().process();
            }
            ++index;
        }
        profilerTimer.stop();
        profilerTimer = ProfilerRegistry.getTimer("updatePlayers");
        profilerTimer.start();
        entityArray = players;
        length = players.length;
        index = 0;
        while (index < length) {
            value2 = entityArray[index];
            if (value2 != null) {
                try {
                    if (ServerSettings.clientBuild == 443 && !((Player)value2).isBot) {
                        ProfilerTimer modernPlayerTimer = ProfilerRegistry.getTimer("modernPlayerUpdate");
                        modernPlayerTimer.start();
                        try {
                            ModernPlayerUpdateTask.updatePlayer((Player)value2);
                        } finally {
                            modernPlayerTimer.stop();
                        }

                        ProfilerTimer modernNpcTimer = ProfilerRegistry.getTimer("modernNpcUpdate");
                        modernNpcTimer.start();
                        try {
                            NpcUpdateTask.updatePlayer((Player)value2);
                        } finally {
                            modernNpcTimer.stop();
                        }
                    } else {
                        PlayerUpdateTask.updatePlayer((Player)value2);
                        GameplayHelper.sendNpcUpdatePacket((Player)value2);
                    }
                }
                catch (Exception exception) {
                    value = exception;
                    exception.printStackTrace();
                    ((Player)value2).disconnect();
                }
            }
            ++index;
        }
        profilerTimer.stop();
        profilerTimer = ProfilerRegistry.getTimer("resetPlayers");
        profilerTimer.start();
        entityArray = players;
        length = players.length;
        index = 0;
        while (index < length) {
            value2 = entityArray[index];
            if (value2 != null) {
                try {
                    value3 = value2;
                    ((Entity)value3).getUpdateState().reset();
                    ((Entity)value3).setWalkDirection(-1);
                    ((Entity)value3).setRunDirection(-1);
                    ((Player)value3).setAppearanceUpdateRequired(false);
                    ((Player)value3).setTeleportPlacementUpdateRequired(false);
                    ((Player)value3).setTeleporting(false);
                    ((Player)value3).setPublicChatPayload(null);
                }
                catch (Exception exception) {
                    value = exception;
                    exception.printStackTrace();
                    ((Player)value2).disconnect();
                }
            }
            ++index;
        }
        profilerTimer.stop();
        profilerTimer = ProfilerRegistry.getTimer("resetNPCs");
        profilerTimer.start();
        entityArray = npcs;
        length = npcs.length;
        index = 0;
        while (index < length) {
            value2 = entityArray[index];
            if (value2 != null) {
                try {
                    ((Npc)value2).resetAfterUpdate();
                }
                catch (Exception exception) {
                    value = exception;
                    exception.printStackTrace();
                    World.unregisterNpc((Npc)value2);
                }
            }
            ++index;
        }
        profilerTimer.stop();
    }

    public static synchronized void registerPlayer(Player player) {
        int initialValue = 1;
        while (initialValue < players.length) {
            if (players[initialValue] == null) {
                World.players[initialValue] = player;
                player.setIndex(initialValue);
                player.setEncodedIndex(initialValue + 32768);
                player.setSize(1);
                return;
            }
            ++initialValue;
        }
        throw new IllegalStateException("Server is full!");
    }

    public static synchronized void registerNpc(Npc npc) {
        int initialValue = 1;
        while (initialValue < npcs.length) {
            if (npcs[initialValue] == null) {
                World.npcs[initialValue] = npc;
                npc.setIndex(initialValue);
                npc.setEncodedIndex(initialValue);
                npc.setSize(npcDefinitions[npc.getNpcId()].getSize());
                int[] integerValues = Npc.combatTransformNpcIds;
                int length = Npc.combatTransformNpcIds.length;
                int index = 0;
                while (index < length) {
                    initialValue = integerValues[index];
                    if (initialValue == npc.getNpcId()) {
                        npc.setCombatTransformNpcId(npc.getNpcId() - 1);
                        break;
                    }
                    ++index;
                }
                integerValues = Npc.scriptedMovementNpcIds;
                length = Npc.scriptedMovementNpcIds.length;
                index = 0;
                while (index < length) {
                    initialValue = integerValues[index];
                    if (initialValue == npc.getNpcId()) {
                        npc.setScriptedMovementEnabled(true);
                        break;
                    }
                    ++index;
                }
                integerValues = Npc.targetMovementDisabledNpcIds;
                length = Npc.targetMovementDisabledNpcIds.length;
                index = 0;
                while (index < length) {
                    initialValue = integerValues[index];
                    if (initialValue == npc.getNpcId()) {
                        npc.setTargetMovementDisabled(true);
                        break;
                    }
                    ++index;
                }
                integerValues = Npc.autoRetaliateDisabledNpcIds;
                length = Npc.autoRetaliateDisabledNpcIds.length;
                index = 0;
                while (index < length) {
                    initialValue = integerValues[index];
                    if (initialValue == npc.getNpcId()) {
                        npc.setAutoRetaliateDisabled(true);
                        break;
                    }
                    ++index;
                }
                integerValues = Npc.faceEntityUpdateDisabledNpcIds;
                length = Npc.faceEntityUpdateDisabledNpcIds.length;
                index = 0;
                while (index < length) {
                    initialValue = integerValues[index];
                    if (initialValue == npc.getNpcId()) {
                        npc.setFaceEntityUpdateDisabled(true);
                        return;
                    }
                    ++index;
                }
                return;
            }
            ++initialValue;
        }
        throw new IllegalStateException("Server is full!");
    }

    public static synchronized void unregisterPlayer(Player player) {
        try {
            CharacterFileManager.savePlayer(player);
            player.setRegistered(false);
            player.setConnectionState(PlayerConnectionState.DISCONNECTED);
            if (player.getIndex() == -1) {
                return;
            }
            player.clearCombatEffectTasks();
            World.players[player.getIndex()] = null;
            player.setIndex(-1);
            return;
        }
        catch (Exception exception) {
            exception.printStackTrace();
            return;
        }
    }

    public static synchronized void unregisterNpc(Npc npc) {
        if (npc.getIndex() == -1) {
            return;
        }
        npc.clearCombatEffectTasks();
        World.npcs[npc.getIndex()] = null;
        npc.setIndex(-1);
    }

    public static int getPlayerCount() {
        int index = 0;
        Player[] playerArray = players;
        int length = players.length;
        int index2 = 0;
        while (index2 < length) {
            Player player = playerArray[index2];
            if (player != null) {
                ++index;
            }
            ++index2;
        }
        return index;
    }

    public static int getNonBotPlayerCount() {
        int index = 0;
        Player[] playerArray = players;
        int length = players.length;
        int index2 = 0;
        while (index2 < length) {
            Player player = playerArray[index2];
            if (player != null && !player.isBot) {
                ++index;
            }
            ++index2;
        }
        return index;
    }

    public static boolean hasNearbyNonBotPlayer(Npc npc) {
        Player[] playerArray = players;
        int length = players.length;
        int index = 0;
        while (index < length) {
            Player player = playerArray[index];
            if (player != null && !player.isBot && GameUtil.getDistance(npc.getPosition(), player.getPosition()) <= 32) {
                return true;
            }
            ++index;
        }
        return false;
    }

    public static int getAdminCount() {
        int index = 0;
        Player[] playerArray = players;
        int length = players.length;
        int index2 = 0;
        while (index2 < length) {
            Player player = playerArray[index2];
            if (player != null && player.getPlayerRights() >= 2) {
                ++index;
            }
            ++index2;
        }
        return index;
    }

    public static int getModeratorCount() {
        int index = 0;
        Player[] playerArray = players;
        int length = players.length;
        int index2 = 0;
        while (index2 < length) {
            Player player = playerArray[index2];
            if (player != null && player.getPlayerRights() == 1) {
                ++index;
            }
            ++index2;
        }
        return index;
    }

    public static Player findPlayerByUsername(String username) {
        long nameHash = TextUtil.encodeNameHash(username);
        Player[] playerArray = players;
        int length = players.length;
        int index = 0;
        while (index < length) {
            Player player = playerArray[index];
            if (player != null && player.getNameHash() == nameHash) {
                return player;
            }
            ++index;
        }
        return null;
    }

    public static void logoutBotsAndScheduleShutdown(boolean enabled2) {
        Player[] playerArray = players;
        int length = players.length;
        int index = 0;
        while (index < length) {
            Player player = playerArray[index];
            if (player != null && player.isBot) {
                Player player2 = player;
                player2.packetSender.sendLogout();
                player.disconnect();
            }
            ++index;
        }
        Server.scheduleShutdown(true);
    }

    public static void scheduleTickTask(TickTask tickTask) {
        taskScheduler.schedule(tickTask);
    }

    public static Player[] getPlayers() {
        return players;
    }

    public static Npc[] getNpcs() {
        return npcs;
    }

    public static synchronized void resetForServerRestart() {
        Player[] playerSnapshot = players.clone();
        for (Player player : playerSnapshot) {
            if (player == null) {
                continue;
            }

            try {
                player.disconnect();
            }
            catch (Exception exception) {
                exception.printStackTrace();
            }

            if (player.getIndex() != -1) {
                unregisterPlayer(player);
            }
        }

        Npc[] npcSnapshot = npcs.clone();
        for (Npc npc : npcSnapshot) {
            if (npc != null && npc.getIndex() != -1) {
                unregisterNpc(npc);
            }
        }

        taskScheduler = new TaskScheduler();
        tickCount = 0;
        instance.objectRegionIndex = new WorldObjectRegionIndex();
    }

    public static TaskScheduler getTaskScheduler() {
        return taskScheduler;
    }

    public static NpcDefinition[] getNpcDefinitions() {
        return npcDefinitions;
    }

    public static World getInstance() {
        return instance;
    }

    public final WorldObjectRegionIndex getObjectRegionIndex() {
        return this.objectRegionIndex;
    }

    public static void sendStillGraphicToNearbyPlayers(GraphicEffect graphicEffect, Position position) {
        Player[] playerArray = players;
        int length = players.length;
        int index = 0;
        while (index < length) {
            Object value = playerArray[index];
            if (value != null && position.isWithinViewport(((Entity)value).getPosition())) {
                Position position2 = position;
                GraphicEffect graphicEffect2 = graphicEffect;
                value = ((Player)value).packetSender;
                ((PacketSender)value).sendStillGraphic(graphicEffect2.getId(), position2, graphicEffect2.getPackedDelay());
            }
            ++index;
        }
    }
}
