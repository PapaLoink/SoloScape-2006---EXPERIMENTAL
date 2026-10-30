package com.rs2.model.gameplay.castlewars;

import com.rs2.bot.combat.BotCombatHelper;
import com.rs2.model.Position;
import com.rs2.model.GameplayHelper;
import com.rs2.model.combat.CombatType;
import com.rs2.model.combat.effect.PoisonEffect;
import com.rs2.model.World;
import com.rs2.model.ground.GroundItem;
import com.rs2.model.ground.GroundItemManager;
import com.rs2.model.item.ItemStack;
import com.rs2.model.item.consumable.PotionHandler;
import com.rs2.model.npc.Npc;
import com.rs2.model.objects.DynamicObject;
import com.rs2.model.objects.LoadedWorldObject;
import com.rs2.model.objects.ObjectManager;
import com.rs2.model.objects.WorldObjectLookup;
import com.rs2.model.player.Player;
import com.rs2.model.task.TickTask;
import com.rs2.util.GameUtil;
import com.rs2.util.path.PathFinder;
import com.rs2.util.path.WalkingCollisionMap;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.Map;

public final class CastleWarsManager {
    public static final int SARADOMIN_PORTAL_ID = 4387;
    public static final int ZAMORAK_PORTAL_ID = 4388;
    public static final int GUTHIX_PORTAL_ID = 4408;
    public static final int SARADOMIN_EXIT_PORTAL_ID = 4389;
    public static final int ZAMORAK_EXIT_PORTAL_ID = 4390;
    public static final int SARADOMIN_GAME_EXIT_PORTAL_ID = 4406;
    public static final int ZAMORAK_GAME_EXIT_PORTAL_ID = 4407;
    public static final int BANDAGE_TABLE_ID = 4458;
    public static final int SARADOMIN_ENERGY_BARRIER_ID = 4469;
    public static final int ZAMORAK_ENERGY_BARRIER_ID = 4470;
    public static final int SARADOMIN_SPAWN_LADDER_ID = 6280;
    public static final int ZAMORAK_SPAWN_LADDER_ID = 6281;
    public static final int SARADOMIN_SPAWN_TRAPDOOR_ID = 4471;
    public static final int ZAMORAK_SPAWN_TRAPDOOR_ID = 4472;
    public static final int STEPPING_STONE_ID = 4411;

    public static final int SARADOMIN_HOOD_ID = 4513;
    public static final int SARADOMIN_CLOAK_ID = 4514;
    public static final int ZAMORAK_HOOD_ID = 4515;
    public static final int ZAMORAK_CLOAK_ID = 4516;
    public static final int SARADOMIN_FLAG_ID = 4037;
    public static final int ZAMORAK_FLAG_ID = 4039;
    private static final int SARADOMIN_EMPTY_STAND_ID = 4377;
    private static final int ZAMORAK_EMPTY_STAND_ID = 4378;
    private static final int SARADOMIN_STANDARD_OBJECT_ID = 4900;
    private static final int SARADOMIN_STANDARD_OBJECT_ALT_ID = 4902;
    private static final int ZAMORAK_STANDARD_OBJECT_ID = 4901;
    private static final int ZAMORAK_STANDARD_OBJECT_ALT_ID = 4903;
    public static final int CASTLE_WARS_TICKET_ID = 4067;
    public static final int LANTHUS_NPC_ID = 1526;

    public static final int CASTLE_WARS_MANUAL_ID = 4055;
    private static final int CASTLE_WARS_MANUAL_INTERFACE_ID = 837;
    private static final int CASTLE_WARS_MANUAL_TITLE_TEXT_ID = 903;
    private static final int CASTLE_WARS_MANUAL_FIRST_LINE_ID = 843;
    private static final int CASTLE_WARS_MANUAL_LINE_COUNT = 22;
    private static final int CASTLE_WARS_MANUAL_LEFT_PAGE_TEXT_ID = 14165;
    private static final int CASTLE_WARS_MANUAL_RIGHT_PAGE_TEXT_ID = 14166;
    private static final int CASTLE_WARS_MANUAL_PREVIOUS_BUTTON_ID = 839;
    private static final int CASTLE_WARS_MANUAL_NEXT_BUTTON_ID = 841;
    private static final int CASTLE_WARS_MANUAL_CLOSE_BUTTON_ID = 10162;

    /*
     * The 2006 manual predates later Castle Wars additions such as flares and
     * ballistas. Each entry below is one two-page spread: lines 0-10 are the
     * left page and lines 11-21 are the right page.
     */
    private static final String[][] CASTLE_WARS_MANUAL_PAGES = new String[][]{
        new String[]{
            "@dre@Objective", "",
            "Steal the enemy team's",
            "standard and carry it",
            "back to your own.",
            "",
            "Defend your standard",
            "while your team attacks.",
            "Most captures wins.",
            "", "",
            "@dre@Toolkit", "",
            "Toolkits repair your",
            "team's doors and",
            "catapult.",
            "",
            "Use one on damaged",
            "equipment to repair it.",
            "Keep your defences ready.",
            "", ""
        },
        new String[]{
            "@dre@Bandages", "",
            "Bandages restore some",
            "health and run energy.",
            "",
            "You can also use one",
            "on a team-mate to",
            "help keep them alive.",
            "",
            "Take plenty into battle.",
            "",
            "@dre@Explosive potion", "",
            "Explosive potions can",
            "destroy barricades and",
            "damage enemy equipment.",
            "",
            "They can also affect",
            "rock passages below",
            "the arena.",
            "Use them carefully.", ""
        },
        new String[]{
            "@dre@Barricades", "",
            "Barricades block paths",
            "and slow enemy players.",
            "",
            "Place them at important",
            "doors and approaches.",
            "",
            "Each team may only have",
            "ten placed at once.",
            "",
            "@dre@Buckets of water", "",
            "Water puts out fires",
            "on barricades and the",
            "catapult.",
            "",
            "If enemy equipment is",
            "burning, defenders may",
            "use water to save it.",
            "", ""
        },
        new String[]{
            "@dre@Tinderbox", "",
            "A tinderbox can set",
            "enemy barricades or",
            "the catapult on fire.",
            "",
            "Fire takes time to work,",
            "giving defenders a",
            "chance to put it out.",
            "", "",
            "@dre@Pickaxe", "",
            "Pickaxes clear collapsed",
            "rocks in the tunnels.",
            "",
            "The tunnels give another",
            "route between castles.",
            "",
            "Watch for players trying",
            "to block the passage.",
            ""
        },
        new String[]{
            "@dre@Catapult", "",
            "The catapult fires rocks",
            "across the battlefield.",
            "",
            "Stand by your team's",
            "catapult, choose a target",
            "and fire at the enemy.",
            "",
            "Aim carefully.",
            "",
            "@dre@Catapult rocks", "",
            "Rocks are ammunition",
            "for the catapult.",
            "",
            "Collect them from your",
            "castle supplies before",
            "using the catapult.",
            "",
            "Each shot uses one rock.",
            ""
        }
    };

    // Cache 377 Castle Wars waiting-room transformation NPCs.
    private static final int GUTHIX_SHEEP_TRANSFORMATION_ID = 1529;
    private static final int SARADOMIN_RABBIT_TRANSFORMATION_ID = 1530;
    private static final int ZAMORAK_IMP_TRANSFORMATION_ID = 1531;
    /*
     * Cache 377 wearable god items which trigger the Castle Wars waiting-room
     * transformations. God helmets and capes are intentionally omitted because
     * the lobby portal rejects anything worn in the head/cape slots before this
     * check is reached. God book pages and consumables are not wearable and do
     * not trigger the transformation.
     */
    private static final int[] SARADOMIN_TRANSFORMATION_ITEMS = new int[]{
        1718, // Holy symbol
        2415, // Saradomin staff
        2661, // Saradomin platebody
        2663, // Saradomin platelegs
        2667, // Saradomin kiteshield
        3479, // Saradomin plateskirt
        3839, // Damaged book (Saradomin)
        3840, // Holy book
        6762  // Saradomin mjolnir
    };
    private static final int[] GUTHIX_TRANSFORMATION_ITEMS = new int[]{
        2416, // Guthix staff
        2669, // Guthix platebody
        2671, // Guthix platelegs
        2675, // Guthix kiteshield
        3480, // Guthix plateskirt
        3843, // Damaged book (Guthix)
        3844, // Book of balance
        6760  // Guthix mjolnir
    };
    private static final int[] ZAMORAK_TRANSFORMATION_ITEMS = new int[]{
        1033, // Zamorak robe bottom
        1035, // Zamorak robe top
        1724, // Unholy symbol
        2417, // Zamorak staff
        2653, // Zamorak platebody
        2655, // Zamorak platelegs
        2659, // Zamorak kiteshield
        3478, // Zamorak plateskirt
        3841, // Damaged book (Zamorak)
        3842, // Unholy book
        6764  // Zamorak mjolnir
    };

    public static final int MINIMUM_PLAYERS_PER_TEAM = 1;
    public static final int WAITING_DURATION_SECONDS = 5 * 60;
    public static final int GAME_DURATION_SECONDS = 20 * 60;

    private static final long DROPPED_FLAG_AUTO_RETURN_MILLIS = 45_000L;

    private static final int WAITING_INTERFACE_ID = 6673;
    private static final int WAITING_TIMER_TEXT_ID = 6570;
    private static final int WAITING_ZAMORAK_TEXT_ID = 6572;
    private static final int WAITING_SARADOMIN_TEXT_ID = 6664;
    private static final int GAME_INTERFACE_ID = 11344;
    private static final int GAME_ZAMORAK_SCORE_TEXT_ID = 11345;
    private static final int GAME_SARADOMIN_SCORE_TEXT_ID = 11346;
    private static final int GAME_TIMER_TEXT_ID = 11353;
    private static final int GAME_ZAMORAK_FLAG_TEXT_ID = 11349;
    private static final int GAME_SARADOMIN_FLAG_TEXT_ID = 11350;
    private static final int GAME_MAIN_GATE_TEXT_ID = 11352;
    private static final int GAME_SIDE_DOOR_TEXT_ID = 11356;
    private static final int GAME_TUNNEL_ONE_TEXT_ID = 11358;
    private static final int GAME_TUNNEL_TWO_TEXT_ID = 11360;
    private static final int GAME_CATAPULT_TEXT_ID = 11362;

    private static final int SCOREBOARD_OBJECT_ID = 4484;
    private static final int SCOREBOARD_INTERFACE_ID = 11333;
    private static final int SCOREBOARD_TITLE_TEXT_ID = 11334;
    private static final int SCOREBOARD_SARADOMIN_TEXT_ID = 11335;
    private static final int SCOREBOARD_ZAMORAK_TEXT_ID = 11336;
    private static final int SCOREBOARD_STATE_VERSION = 1;
    private static final File SCOREBOARD_STATE_FILE = new File("./data/castle_wars_scoreboard.dat");

    private static final Position CASTLE_WARS_LOBBY = new Position(2441, 3090, 0);
    private static final Position SARADOMIN_WAITING_ROOM = new Position(2377, 9485, 0);
    private static final Position ZAMORAK_WAITING_ROOM = new Position(2421, 9524, 0);
    private static final Position SARADOMIN_FLAG_STAND = new Position(2429, 3074, 3);
    private static final Position ZAMORAK_FLAG_STAND = new Position(2370, 3133, 3);
    private static final int STEPPING_STONE_JUMP_ANIMATION = 741;
    private static final Position[] SOUTHWEST_STEPPING_STONE_ROUTE = new Position[]{
        new Position(2378, 3083, 0),
        new Position(2378, 3084, 0),
        new Position(2378, 3085, 0),
        new Position(2377, 3085, 0),
        new Position(2377, 3086, 0),
        new Position(2377, 3087, 0),
        new Position(2377, 3088, 0),
        new Position(2377, 3089, 0)
    };
    private static final Position[] NORTHEAST_STEPPING_STONE_ROUTE = new Position[]{
        new Position(2420, 3122, 0),
        new Position(2420, 3123, 0),
        new Position(2419, 3123, 0),
        new Position(2419, 3124, 0),
        new Position(2419, 3125, 0),
        new Position(2418, 3125, 0),
        new Position(2418, 3126, 0)
    };

    private static final Map<Player, Team> waitingPlayers = new IdentityHashMap<Player, Team>();
    private static final Map<Player, Team> gamePlayers = new IdentityHashMap<Player, Team>();
    private static final Map<Player, ReplacementOffer> replacementOffers = new IdentityHashMap<Player, ReplacementOffer>();
    private static final Map<Player, Long> replacementOfferCooldowns = new IdentityHashMap<Player, Long>();

    private static final int REPLACEMENT_OFFER_DURATION_SECONDS = 30;

    private static boolean initialized;
    private static boolean gameInProgress;
    private static long nextGameStartMillis = -1L;
    private static long gameEndMillis = -1L;
    private static long lastProcessedSecond = -1L;
    private static int gameTeamCapacity;
    private static int saradominScore;
    private static int zamorakScore;
    private static int scoreboardSeasonKey;
    private static int saradominSeasonVictories;
    private static int zamorakSeasonVictories;
    private static boolean saradominFlagAtBase = true;
    private static boolean zamorakFlagAtBase = true;
    private static Player saradominFlagHolder;
    private static Player zamorakFlagHolder;
    private static GroundItem saradominDroppedFlag;
    private static GroundItem zamorakDroppedFlag;
    private static long saradominDroppedFlagReturnMillis;
    private static long zamorakDroppedFlagReturnMillis;

    private CastleWarsManager() {
    }

    public static void initialize() {
        if (initialized) {
            return;
        }
        initialized = true;
        loadScoreboardState();
        spawnLanthus();
        World.scheduleTickTask(new CastleWarsTickTask());
    }

    private static void spawnLanthus() {
        if (Npc.findByDefinitionId(LANTHUS_NPC_ID) != null) {
            return;
        }
        // Keep Lanthus beside the lobby return tile so players never spawn on top of him.
        GameplayHelper.spawnNpc(LANTHUS_NPC_ID, 2440, 3089, 0, 2);
    }

    public static boolean relocatePlayerOnLogin(Player player) {
        if (player == null || !isCastleWarsMinigamePosition(player.getPosition())) {
            return false;
        }

        // Purge any stale session entry left by the character that logged out.
        cleanupWaitingPlayers();
        cleanupGamePlayers();
        waitingPlayers.remove(player);
        gamePlayers.remove(player);
        replacementOffers.remove(player);
        replacementOfferCooldowns.remove(player);
        clearWaitingRoomGodTransformation(player);

        returnCarriedFlagToBase(player);
        clearFlagWeapon(player);
        removeTeamColours(player);
        removeBandages(player);
        removeTemporaryCastleWarsInventoryItems(player);
        CastleWarsEngineeringManager.cleanupPlayerSupplies(player);
        restorePlayerAfterCastleWars(player);

        // This runs before World.registerPlayer(), so update the saved position
        // directly instead of using moveTo/applyTeleportPosition (which assume a
        // registered player index).
        Position position = player.getPosition();
        position.set(CASTLE_WARS_LOBBY);
        position.setPreviousX(CASTLE_WARS_LOBBY.getX());
        position.setPreviousY(CASTLE_WARS_LOBBY.getY() + 1);
        return true;
    }

    public static boolean isCastleWarsMinigamePosition(Position position) {
        if (position == null) {
            return false;
        }

        int x = position.getX();
        int y = position.getY();
        int plane = position.getPlane();

        // Surface arena, both castles, spawn rooms and their upper floors.
        boolean surfaceArena = plane >= 0 && plane <= 3
                && x >= 2368 && x <= 2431
                && y >= 3072 && y <= 3135;

        // Waiting caves plus the complete underground tunnel network.
        boolean underground = plane == 0
                && x >= 2360 && x <= 2438
                && y >= 9468 && y <= 9540;

        return surfaceArena || underground;
    }

    public static void process() {
        enforceOwnFlagCarrierCastleRestrictions();

        long now = System.currentTimeMillis();
        long second = now / 1000L;
        if (second == lastProcessedSecond) {
            return;
        }
        lastProcessedSecond = second;

        cleanupWaitingPlayers();
        cleanupGamePlayers();
        cleanupDroppedFlags(now);
        CastleWarsEngineeringManager.processBarricadeFires(now);
        CastleWarsEngineeringManager.processCatapultFires(now);

        if (gameInProgress) {
            if (now >= gameEndMillis) {
                endGame(now);
            } else {
                processReplacementOffers(now);
            }
        } else {
            if (hasMinimumPlayersToStartInternal()) {
                if (nextGameStartMillis < 0L) {
                    nextGameStartMillis = now + WAITING_DURATION_SECONDS * 1000L;
                }
                if (now >= nextGameStartMillis) {
                    startGame(now);
                }
            } else {
                nextGameStartMillis = -1L;
            }
        }

        updateWaitingRoomInterfaces(now);
        updateGameInterfaces(now);
    }

    public static boolean handleFirstObjectAction(Player player, int objectId, int objectX, int objectY) {
        if (objectId == SCOREBOARD_OBJECT_ID) {
            showScoreboard(player);
            return true;
        }
        if (handlePortal(player, objectId)) {
            return true;
        }
        if (objectId == STEPPING_STONE_ID && isInGame(player)) {
            useSteppingStone(player, objectX, objectY);
            return true;
        }
        if (objectId == BANDAGE_TABLE_ID) {
            takeBandages(player, 1);
            return true;
        }
        if (CastleWarsEngineeringManager.handleSupplyTable(player, objectId)) {
            return true;
        }
        if (CastleWarsEngineeringManager.handleRockslideObjectAction(
                player, objectId, objectX, objectY)) {
            return true;
        }
        if (CastleWarsEngineeringManager.handleClimbingRope(player, objectId, objectX, objectY)) {
            return true;
        }
        if (CastleWarsEngineeringManager.operateCatapult(player, objectId)) {
            return true;
        }
        if (CastleWarsEngineeringManager.handleSideDoor(player, objectId, objectX, objectY)) {
            return true;
        }
        if (CastleWarsEngineeringManager.handleMainDoor(player, objectId, objectX, objectY)) {
            return true;
        }
        if (objectId == SARADOMIN_ENERGY_BARRIER_ID) {
            passEnergyBarrier(player, Team.SARADOMIN, objectX, objectY);
            return true;
        }
        if (objectId == ZAMORAK_ENERGY_BARRIER_ID) {
            passEnergyBarrier(player, Team.ZAMORAK, objectX, objectY);
            return true;
        }
        if (objectId == SARADOMIN_SPAWN_LADDER_ID) {
            useSpawnRoomLadder(player, Team.SARADOMIN, true);
            return true;
        }
        if (objectId == ZAMORAK_SPAWN_LADDER_ID) {
            useSpawnRoomLadder(player, Team.ZAMORAK, true);
            return true;
        }
        if (objectId == SARADOMIN_SPAWN_TRAPDOOR_ID) {
            useSpawnRoomLadder(player, Team.SARADOMIN, false);
            return true;
        }
        if (objectId == ZAMORAK_SPAWN_TRAPDOOR_ID) {
            useSpawnRoomLadder(player, Team.ZAMORAK, false);
            return true;
        }
        if (objectId == 4900 || objectId == 4902 || objectId == 4377) {
            handleFlagObject(player, Team.SARADOMIN);
            return true;
        }
        if (objectId == 4901 || objectId == 4903 || objectId == 4378) {
            handleFlagObject(player, Team.ZAMORAK);
            return true;
        }
        if (handleCastleWarsTraversal(player, objectId, objectX, objectY)) {
            return true;
        }
        return false;
    }

    public static boolean handleSecondObjectAction(Player player, int objectId, int objectX, int objectY) {
        if (objectId == SCOREBOARD_OBJECT_ID) {
            showPlayerScoreboardStats(player);
            return true;
        }
        if (objectId == BANDAGE_TABLE_ID) {
            takeBandages(player, 5);
            return true;
        }
        if (CastleWarsEngineeringManager.attackMainDoor(player, objectId, objectX, objectY)) {
            return true;
        }
        return false;
    }

    public static boolean handlePortal(Player player, int objectId) {
        initialize();
        if (objectId == SARADOMIN_EXIT_PORTAL_ID || objectId == ZAMORAK_EXIT_PORTAL_ID) {
            leaveWaitingRoom(player);
            removeTeamColours(player);
            clearCastleWarsInterface(player);
            moveToLobby(player);
            player.getPacketSender().sendGameMessage("You leave the Castle Wars waiting room.");
            return true;
        }
        if (objectId == SARADOMIN_GAME_EXIT_PORTAL_ID || objectId == ZAMORAK_GAME_EXIT_PORTAL_ID) {
            Team requiredTeam = objectId == SARADOMIN_GAME_EXIT_PORTAL_ID ? Team.SARADOMIN : Team.ZAMORAK;
            Team playerTeam = gamePlayers.get(player);
            if (playerTeam != null && playerTeam != requiredTeam) {
                player.getPacketSender().sendGameMessage("You cannot use the other team's exit portal.");
                return true;
            }
            if (playerTeam != null) {
                leaveGame(player);
            } else {
                setCastleWarsAttackOption(player, false);
                removeTeamColours(player);
                removeBandages(player);
                CastleWarsEngineeringManager.cleanupPlayerSupplies(player);
                clearCastleWarsInterface(player);
                restorePlayerAfterCastleWars(player);
                moveToLobby(player);
                player.getPacketSender().sendGameMessage("You return to the Castle Wars lobby.");
            }
            return true;
        }
        return handleLobbyPortal(player, objectId);
    }

    public static boolean handleLobbyPortal(Player player, int objectId) {
        initialize();

        Team requestedTeam;
        if (objectId == SARADOMIN_PORTAL_ID) {
            requestedTeam = Team.SARADOMIN;
        } else if (objectId == ZAMORAK_PORTAL_ID) {
            requestedTeam = Team.ZAMORAK;
        } else if (objectId == GUTHIX_PORTAL_ID) {
            requestedTeam = null;
        } else {
            return false;
        }

        if (hasRestrictedTeamColourEquipment(player)) {
            player.getPacketSender().sendGameMessage("You can't wear anything in the head or cape slots in Castle Wars.");
            player.getPacketSender().sendGameMessage("Remove your headgear and cape before entering the portal.");
            return true;
        }
        if (hasRestrictedInventoryItems(player)) {
            player.getPacketSender().sendGameMessage("You can only bring potions, runes and equippable items into Castle Wars.");
            return true;
        }

        cleanupWaitingPlayers();

        int saradominPlayers = getWaitingPlayerCountInternal(Team.SARADOMIN);
        int zamorakPlayers = getWaitingPlayerCountInternal(Team.ZAMORAK);

        Team team;
        if (requestedTeam == null) {
            if (saradominPlayers < zamorakPlayers) {
                team = Team.SARADOMIN;
            } else if (zamorakPlayers < saradominPlayers) {
                team = Team.ZAMORAK;
            } else {
                team = GameUtil.randomInt(2) == 0 ? Team.SARADOMIN : Team.ZAMORAK;
            }
        } else {
            team = requestedTeam;
            if (team == Team.SARADOMIN && saradominPlayers > zamorakPlayers) {
                player.getPacketSender().sendGameMessage("The Saradomin team has too many players. Try the other team.");
                return true;
            }
            if (team == Team.ZAMORAK && zamorakPlayers > saradominPlayers) {
                player.getPacketSender().sendGameMessage("The Zamorak team has too many players. Try the other team.");
                return true;
            }
        }

        gamePlayers.remove(player);
        replacementOffers.remove(player);
        replacementOfferCooldowns.remove(player);
        waitingPlayers.put(player, team);
        clearWaitingRoomGodTransformation(player);
        // The transformation is determined by the portal the player actually
        // entered. The Guthix portal still turns opposing-god wearers into a
        // sheep even though it subsequently assigns them to Sara or Zamorak.
        applyWaitingRoomGodTransformation(player, objectId);
        equipTeamColours(player, team);
        Position destination = team == Team.SARADOMIN ? SARADOMIN_WAITING_ROOM : ZAMORAK_WAITING_ROOM;
        player.moveTo(new Position(destination.getX(), destination.getY(), destination.getPlane()));
        player.getPacketSender().sendGameMessage("You join the " + getTeamName(team) + " team.");
        updateWaitingRoomInterface(player, System.currentTimeMillis());
        return true;
    }

    public static Team getWaitingTeam(Player player) {
        cleanupWaitingPlayers();
        return waitingPlayers.get(player);
    }

    public static void spreadWaitingPlayer(Player player) {
        Team team = waitingPlayers.get(player);
        if (team == null) {
            return;
        }

        Position center = team == Team.SARADOMIN ? SARADOMIN_WAITING_ROOM : ZAMORAK_WAITING_ROOM;
        for (int attempt = 0; attempt < 40; ++attempt) {
            int x = center.getX() - 9 + GameUtil.randomInt(19);
            int y = center.getY() - 9 + GameUtil.randomInt(19);
            if (WalkingCollisionMap.getTileFlags(x, y, center.getPlane()) != 0) {
                continue;
            }
            if (isOccupiedWaitingTile(x, y, center.getPlane(), player)) {
                continue;
            }
            player.moveTo(new Position(x, y, center.getPlane()));
            return;
        }
    }

    public static Position getWaitingRoomWanderTarget(Player player, int radius) {
        Team team = waitingPlayers.get(player);
        if (team == null) {
            return null;
        }

        Position center = team == Team.SARADOMIN ? SARADOMIN_WAITING_ROOM : ZAMORAK_WAITING_ROOM;
        Position current = player.getPosition();
        int wanderRadius = Math.max(1, Math.min(radius, 5));

        for (int attempt = 0; attempt < 30; ++attempt) {
            int x = current.getX() - wanderRadius + GameUtil.randomInt(wanderRadius * 2 + 1);
            int y = current.getY() - wanderRadius + GameUtil.randomInt(wanderRadius * 2 + 1);
            if (x == current.getX() && y == current.getY()) {
                continue;
            }
            if (Math.abs(x - center.getX()) > 9 || Math.abs(y - center.getY()) > 9) {
                continue;
            }
            if (WalkingCollisionMap.getTileFlags(x, y, center.getPlane()) != 0) {
                continue;
            }
            if (isOccupiedWaitingTile(x, y, center.getPlane(), player)) {
                continue;
            }
            return new Position(x, y, center.getPlane());
        }
        return null;
    }

    public static Team getGameTeam(Player player) {
        cleanupGamePlayers();
        return gamePlayers.get(player);
    }

    public static Team getTeam(Player player) {
        Team team = gamePlayers.get(player);
        if (team != null) {
            return team;
        }
        return waitingPlayers.get(player);
    }

    public static boolean isWaitingPlayer(Player player) {
        return getWaitingTeam(player) != null;
    }

    public static boolean isInGame(Player player) {
        return getGameTeam(player) != null;
    }

    public static boolean areOpponents(Player first, Player second) {
        Team firstTeam = gamePlayers.get(first);
        Team secondTeam = gamePlayers.get(second);
        return firstTeam != null && secondTeam != null && firstTeam != secondTeam;
    }

    public static boolean areTeamMates(Player first, Player second) {
        Team firstTeam = gamePlayers.get(first);
        Team secondTeam = gamePlayers.get(second);
        return firstTeam != null && firstTeam == secondTeam;
    }

    public static void leaveWaitingRoom(Player player) {
        waitingPlayers.remove(player);
        replacementOffers.remove(player);
        replacementOfferCooldowns.remove(player);
        clearWaitingRoomGodTransformation(player);
    }

    public static void leaveGame(Player player) {
        if (gamePlayers.remove(player) == null) {
            return;
        }
        returnCarriedFlagToBase(player);
        setCastleWarsAttackOption(player, false);
        removeTeamColours(player);
        removeBandages(player);
        CastleWarsEngineeringManager.cleanupPlayerSupplies(player);
        clearCastleWarsInterface(player);
        clearFlagHint(player);
        restorePlayerAfterCastleWars(player);
        moveToLobby(player);
        player.getPacketSender().sendGameMessage("You leave Castle Wars and return to the lobby.");
    }

    public static boolean handleReplacementOfferButton(Player player, int buttonId) {
        if (buttonId != 2461 && buttonId != 2462) {
            return false;
        }

        ReplacementOffer offer = replacementOffers.remove(player);
        if (offer == null) {
            return false;
        }

        long now = System.currentTimeMillis();
        player.getPacketSender().closeInterfaces();

        if (buttonId == 2462) {
            replacementOfferCooldowns.put(
                    player,
                    now + REPLACEMENT_OFFER_DURATION_SECONDS * 1000L);
            player.getPacketSender().sendGameMessage("You remain in the waiting room for the next game.");
            if (waitingPlayers.containsKey(player)) {
                updateWaitingRoomInterface(player, now);
            }
            return true;
        }

        cleanupWaitingPlayers();
        cleanupGamePlayers();
        if (!gameInProgress
                || now >= gameEndMillis
                || now >= offer.expiresAtMillis
                || waitingPlayers.get(player) != offer.team
                || !hasReplacementVacancy(offer.team)) {
            player.getPacketSender().sendGameMessage("That place in the Castle Wars game is no longer available.");
            if (waitingPlayers.containsKey(player)) {
                updateWaitingRoomInterface(player, now);
            }
            return true;
        }

        joinReplacementPlayer(player, offer.team, now);
        return true;
    }

    public static boolean isTeamColourEquipmentSlot(int slot) {
        return slot == 0 || slot == 1;
    }

    public static boolean hasMinimumPlayersToStart() {
        cleanupWaitingPlayers();
        return hasMinimumPlayersToStartInternal();
    }

    public static int getWaitingPlayerCount(Team team) {
        cleanupWaitingPlayers();
        return getWaitingPlayerCountInternal(team);
    }

    public static int getGamePlayerCount(Team team) {
        cleanupGamePlayers();
        int count = 0;
        for (Team gameTeam : gamePlayers.values()) {
            if (gameTeam == team) {
                ++count;
            }
        }
        return count;
    }

    public static int getTotalGamePlayerCount() {
        return gamePlayers.size();
    }

    public static Iterable<Player> getGamePlayersView() {
        return gamePlayers.keySet();
    }


    public static boolean isGameInProgress() {
        return gameInProgress;
    }

    public static int getSaradominScore() {
        return saradominScore;
    }

    public static int getZamorakScore() {
        return zamorakScore;
    }

    public static void scorePoint(Team team) {
        if (!gameInProgress) {
            return;
        }
        if (team == Team.SARADOMIN) {
            ++saradominScore;
        } else {
            ++zamorakScore;
        }
    }

    private static void showScoreboard(Player player) {
        ensureCurrentScoreboardSeason();
        player.getPacketSender().sendInterfaceText("Total Wins This Season!", SCOREBOARD_TITLE_TEXT_ID);
        player.getPacketSender().sendInterfaceText(
                "Saradomin: " + saradominSeasonVictories, SCOREBOARD_SARADOMIN_TEXT_ID);
        player.getPacketSender().sendInterfaceText(
                "Zamorak: " + zamorakSeasonVictories, SCOREBOARD_ZAMORAK_TEXT_ID);
        player.getPacketSender().showChatboxInterface(SCOREBOARD_INTERFACE_ID);
    }

    private static void showPlayerScoreboardStats(Player player) {
        int games = Math.max(0, player.reservedSaveInt1);
        int wins = Math.max(0, player.reservedSaveInt2);
        int losses = Math.max(0, player.reservedSaveInt3);
        player.getPacketSender().sendGameMessage(
                "You've played " + games + " games of Castle Wars. - Won: "
                        + wins + " Lost: " + losses + ".");
    }

    private static void recordPlayerScoreboardResult(Player player, Team team) {
        // These versioned reserved save slots are already persisted by CharacterFileManager.
        player.reservedSaveInt1 = incrementCounter(player.reservedSaveInt1);
        if (saradominScore == zamorakScore) {
            return;
        }

        boolean won = (team == Team.SARADOMIN && saradominScore > zamorakScore)
                || (team == Team.ZAMORAK && zamorakScore > saradominScore);
        if (won) {
            player.reservedSaveInt2 = incrementCounter(player.reservedSaveInt2);
        } else {
            player.reservedSaveInt3 = incrementCounter(player.reservedSaveInt3);
        }
    }

    private static int incrementCounter(int value) {
        if (value < 0) {
            return 1;
        }
        return value == Integer.MAX_VALUE ? value : value + 1;
    }

    private static void recordTeamScoreboardVictory(Team team) {
        ensureCurrentScoreboardSeason();
        if (team == Team.SARADOMIN) {
            saradominSeasonVictories = incrementCounter(saradominSeasonVictories);
        } else if (team == Team.ZAMORAK) {
            zamorakSeasonVictories = incrementCounter(zamorakSeasonVictories);
        }
        saveScoreboardState();
    }

    private static int currentScoreboardSeasonKey() {
        Calendar calendar = Calendar.getInstance();
        return calendar.get(Calendar.YEAR) * 12 + calendar.get(Calendar.MONTH);
    }

    private static void ensureCurrentScoreboardSeason() {
        int currentSeasonKey = currentScoreboardSeasonKey();
        if (scoreboardSeasonKey == currentSeasonKey) {
            return;
        }
        scoreboardSeasonKey = currentSeasonKey;
        saradominSeasonVictories = 0;
        zamorakSeasonVictories = 0;
        saveScoreboardState();
    }

    private static void loadScoreboardState() {
        scoreboardSeasonKey = currentScoreboardSeasonKey();
        saradominSeasonVictories = 0;
        zamorakSeasonVictories = 0;

        if (!SCOREBOARD_STATE_FILE.exists()) {
            return;
        }

        try (DataInputStream input = new DataInputStream(new FileInputStream(SCOREBOARD_STATE_FILE))) {
            int version = input.readInt();
            int savedSeasonKey = input.readInt();
            int savedSaradominVictories = input.readInt();
            int savedZamorakVictories = input.readInt();
            if (version == SCOREBOARD_STATE_VERSION && savedSeasonKey == scoreboardSeasonKey) {
                saradominSeasonVictories = Math.max(0, savedSaradominVictories);
                zamorakSeasonVictories = Math.max(0, savedZamorakVictories);
            }
        } catch (IOException exception) {
            System.err.println("Could not load Castle Wars scoreboard state: " + exception.getMessage());
        }
    }

    private static void saveScoreboardState() {
        File parent = SCOREBOARD_STATE_FILE.getParentFile();
        if (parent != null && !parent.exists() && !parent.mkdirs()) {
            System.err.println("Could not create Castle Wars scoreboard data directory.");
            return;
        }

        try (DataOutputStream output = new DataOutputStream(new FileOutputStream(SCOREBOARD_STATE_FILE))) {
            output.writeInt(SCOREBOARD_STATE_VERSION);
            output.writeInt(scoreboardSeasonKey);
            output.writeInt(saradominSeasonVictories);
            output.writeInt(zamorakSeasonVictories);
        } catch (IOException exception) {
            System.err.println("Could not save Castle Wars scoreboard state: " + exception.getMessage());
        }
    }

    public static boolean isFlagAtBase(Team team) {
        return team == Team.SARADOMIN ? saradominFlagAtBase : zamorakFlagAtBase;
    }

    public static Player getFlagHolder(Team team) {
        return team == Team.SARADOMIN ? saradominFlagHolder : zamorakFlagHolder;
    }

    public static boolean isCarryingEnemyFlag(Player player) {
        return isCarryingFlag(player);
    }

    public static boolean isCarryingFlag(Player player) {
        return saradominFlagHolder == player || zamorakFlagHolder == player;
    }

    public static boolean isCarryingOwnFlag(Player player) {
        Team playerTeam = gamePlayers.get(player);
        return playerTeam != null && getCarriedFlagTeam(player) == playerTeam;
    }

    public static boolean isFlagItemId(int itemId) {
        return itemId == SARADOMIN_FLAG_ID || itemId == ZAMORAK_FLAG_ID;
    }

    public static boolean isInTeamSpawnArea(Player player, Team team) {
        Position position = player.getPosition();
        if (position.getPlane() != 1) {
            return false;
        }
        if (team == Team.SARADOMIN) {
            // x=2422 is already outside the west energy barrier.
            return position.getX() >= 2423 && position.getX() <= 2429
                    && position.getY() >= 3074 && position.getY() <= 3080;
        }
        // x=2377/2378 are already outside the east energy barrier.
        return position.getX() >= 2369 && position.getX() <= 2376
                && position.getY() >= 3127 && position.getY() <= 3134;
    }

    public static int giveBandages(Player player, int requestedAmount) {
        if (!isInGame(player) || requestedAmount <= 0) {
            return 0;
        }
        int freeSlots = player.getInventoryManager().getContainer().getFreeSlots();
        if (freeSlots <= 0) {
            return 0;
        }
        int amount = Math.min(requestedAmount, freeSlots);
        return player.getInventoryManager().addItemPartial(new ItemStack(4049, amount));
    }

    public static boolean useBandage(Player player) {
        if (!isInGame(player) || player.getInventoryManager().getItemAmount(4049) <= 0) {
            return false;
        }
        int maxHitpoints = player.getMaxHitpoints();
        int healAmount = Math.max(1, (int)Math.ceil(maxHitpoints * 0.10));
        int healed = Math.min(maxHitpoints, player.getCurrentHitpoints() + healAmount);
        player.getInventoryManager().removeItem(new ItemStack(4049, 1));
        player.setCurrentHitpoints(healed);
        player.setPoisonDamage(0.0);
        player.addRunEnergyPercent(30);
        return true;
    }

    public static boolean takeEnemyFlag(Player player) {
        Team team = gamePlayers.get(player);
        if (team == null) {
            return false;
        }
        Team flagTeam = team == Team.SARADOMIN ? Team.ZAMORAK : Team.SARADOMIN;
        return takeFlag(player, flagTeam);
    }

    public static boolean tryCaptureFlag(Player player) {
        Team team = gamePlayers.get(player);
        if (team == null) {
            return false;
        }
        Team enemyFlagTeam = team == Team.SARADOMIN ? Team.ZAMORAK : Team.SARADOMIN;
        Player holder = getFlagHolder(enemyFlagTeam);
        if (holder != player) {
            return false;
        }
        clearFlagWeapon(player);
        setFlagAtBase(enemyFlagTeam, true, null);
        scorePoint(team);
        player.getPacketSender().sendGameMessage("You capture the " + getTeamName(enemyFlagTeam) + " flag!");
        return true;
    }

    private static void handleFlagObject(Player player, Team flagTeam) {
        Team team = gamePlayers.get(player);
        if (team == null) {
            player.getPacketSender().sendGameMessage("You can only use the standards during a Castle Wars game.");
            return;
        }
        if (team == flagTeam) {
            if (!tryCaptureFlag(player)) {
                player.getPacketSender().sendGameMessage(isFlagAtBase(flagTeam)
                        ? "Your team's flag is safe."
                        : "Your team's flag has been taken.");
            }
            return;
        }
        takeFlag(player, flagTeam);
    }

    private static boolean takeFlag(Player player, Team flagTeam) {
        if (!isFlagAtBase(flagTeam) || getFlagHolder(flagTeam) != null) {
            player.getPacketSender().sendGameMessage("That flag has already been taken.");
            return false;
        }
        if (isCarryingEnemyFlag(player)) {
            player.getPacketSender().sendGameMessage("You are already carrying a flag.");
            return false;
        }
        if (!canEquipFlag(player)) {
            return false;
        }

        equipFlag(player, flagTeam);
        setFlagAtBase(flagTeam, false, player);
        player.getPacketSender().sendGameMessage("You take the " + getTeamName(flagTeam) + " flag!");
        return true;
    }

    public static boolean isDroppedFlagGroundItem(GroundItem groundItem) {
        return getDroppedFlagTeam(groundItem) != null;
    }

    public static boolean handleDroppedFlagPickup(Player player, GroundItem groundItem) {
        Team flagTeam = getDroppedFlagTeam(groundItem);
        if (flagTeam == null) {
            return false;
        }

        Team playerTeam = gamePlayers.get(player);
        if (playerTeam == null) {
            player.getPacketSender().sendGameMessage("You can only take this flag during a Castle Wars game.");
            return true;
        }

        boolean ownFlagInsideOwnCastle = playerTeam == flagTeam
                && getCastleTeamAtPosition(groundItem.getPosition()) == flagTeam;
        if (ownFlagInsideOwnCastle) {
            if (!GroundItemManager.getInstance().removeForPickup(groundItem, player)) {
                return true;
            }
            setDroppedFlag(flagTeam, null);
            setDroppedFlagReturnMillis(flagTeam, 0L);
            setFlagAtBase(flagTeam, true, null);
            player.getPacketSender().sendGameMessage("You return your team's flag to its stand.");
            return true;
        }

        if (isCarryingEnemyFlag(player)) {
            player.getPacketSender().sendGameMessage("You are already carrying a flag.");
            return true;
        }
        if (!canEquipFlag(player)) {
            return true;
        }
        if (!GroundItemManager.getInstance().removeForPickup(groundItem, player)) {
            return true;
        }

        setDroppedFlag(flagTeam, null);
        setDroppedFlagReturnMillis(flagTeam, 0L);
        equipFlag(player, flagTeam);
        setFlagAtBase(flagTeam, false, player);
        player.getPacketSender().sendGameMessage("You take the " + getTeamName(flagTeam) + " flag!");
        return true;
    }

    private static boolean canEquipFlag(Player player) {
        int neededSlots = 0;
        if (player.getEquipmentManager().getContainer().getItemAt(3) != null) {
            ++neededSlots;
        }
        if (player.getEquipmentManager().getContainer().getItemAt(5) != null) {
            ++neededSlots;
        }
        if (player.getInventoryManager().getContainer().getFreeSlots() < neededSlots) {
            player.getPacketSender().sendGameMessage("You need enough inventory space for your weapon and shield.");
            return false;
        }
        return true;
    }

    private static void equipFlag(Player player, Team flagTeam) {
        if (player.getEquipmentManager().getContainer().getItemAt(5) != null) {
            player.getEquipmentManager().unequipSlot(5);
        }
        if (player.getEquipmentManager().getContainer().getItemAt(3) != null) {
            player.getEquipmentManager().unequipSlot(3);
        }

        int flagId = flagTeam == Team.SARADOMIN ? SARADOMIN_FLAG_ID : ZAMORAK_FLAG_ID;
        player.getEquipmentManager().getContainer().setItem(3, new ItemStack(flagId));
        player.setQueuedCombatSpell(null);
        player.setAutocastSpell(null);
        player.setSpecialAttackEnabled(false);
        player.getEquipmentManager().refresh();
        player.setAppearanceUpdateRequired(true);
    }

    private static void setFlagAtBase(Team flagTeam, boolean atBase, Player holder) {
        if (flagTeam == Team.SARADOMIN) {
            saradominFlagAtBase = atBase;
            saradominFlagHolder = holder;
        } else {
            zamorakFlagAtBase = atBase;
            zamorakFlagHolder = holder;
        }
        if (atBase || holder != null) {
            setDroppedFlagReturnMillis(flagTeam, 0L);
        }
        updateFlagStandObject(flagTeam, atBase);
    }

    private static void updateFlagStandObject(Team flagTeam, boolean atBase) {
        Position stand = flagTeam == Team.SARADOMIN ? SARADOMIN_FLAG_STAND : ZAMORAK_FLAG_STAND;
        int emptyStandId = flagTeam == Team.SARADOMIN
                ? SARADOMIN_EMPTY_STAND_ID : ZAMORAK_EMPTY_STAND_ID;

        DynamicObject existing = ObjectManager.findDynamicObjectAt(
                stand.getX(), stand.getY(), stand.getPlane());
        if (existing != null && existing.getWorldObject().getObjectId() == emptyStandId) {
            ObjectManager.getInstance().removeDynamicObjectAt(
                    stand.getX(), stand.getY(), stand.getPlane(), existing.getWorldObject().getType());
        }

        if (atBase) {
            return;
        }

        int[] standardIds = flagTeam == Team.SARADOMIN
                ? new int[]{SARADOMIN_STANDARD_OBJECT_ID, SARADOMIN_STANDARD_OBJECT_ALT_ID}
                : new int[]{ZAMORAK_STANDARD_OBJECT_ID, ZAMORAK_STANDARD_OBJECT_ALT_ID};
        int restoreObjectId = standardIds[0];
        int objectType = 10;
        int orientation = 0;

        for (int standardId : standardIds) {
            LoadedWorldObject loaded = WorldObjectLookup.findObjectByIdAt(
                    standardId, stand.getX(), stand.getY(), stand.getPlane());
            if (loaded == null) {
                continue;
            }
            restoreObjectId = standardId;
            objectType = loaded.getType();
            orientation = loaded.getOrientation();
            break;
        }

        new DynamicObject(emptyStandId, stand.getX(), stand.getY(), stand.getPlane(),
                orientation, objectType, restoreObjectId, 999999999, false);
    }

    public static GroundItem getDroppedFlagGroundItem(Team flagTeam) {
        return getDroppedFlag(flagTeam);
    }

    private static GroundItem getDroppedFlag(Team flagTeam) {
        return flagTeam == Team.SARADOMIN ? saradominDroppedFlag : zamorakDroppedFlag;
    }

    private static String getFlagStatusText(Team flagTeam) {
        if (isFlagAtBase(flagTeam)) {
            return "Safe";
        }
        return getDroppedFlag(flagTeam) != null ? "Dropped" : "Taken";
    }

    private static void setDroppedFlag(Team flagTeam, GroundItem groundItem) {
        if (flagTeam == Team.SARADOMIN) {
            saradominDroppedFlag = groundItem;
        } else {
            zamorakDroppedFlag = groundItem;
        }
    }

    private static long getDroppedFlagReturnMillis(Team flagTeam) {
        return flagTeam == Team.SARADOMIN
                ? saradominDroppedFlagReturnMillis : zamorakDroppedFlagReturnMillis;
    }

    private static void setDroppedFlagReturnMillis(Team flagTeam, long returnMillis) {
        if (flagTeam == Team.SARADOMIN) {
            saradominDroppedFlagReturnMillis = returnMillis;
        } else {
            zamorakDroppedFlagReturnMillis = returnMillis;
        }
    }

    private static Team getDroppedFlagTeam(GroundItem groundItem) {
        if (groundItem == null) {
            return null;
        }
        if (saradominDroppedFlag == groundItem) {
            return Team.SARADOMIN;
        }
        if (zamorakDroppedFlag == groundItem) {
            return Team.ZAMORAK;
        }
        return null;
    }

    private static Team getCarriedFlagTeam(Player player) {
        if (saradominFlagHolder == player) {
            return Team.SARADOMIN;
        }
        if (zamorakFlagHolder == player) {
            return Team.ZAMORAK;
        }
        return null;
    }

    private static void enforceOwnFlagCarrierCastleRestrictions() {
        for (Map.Entry<Player, Team> entry : gamePlayers.entrySet()) {
            Player player = entry.getKey();
            Team playerTeam = entry.getValue();
            if (player == null || playerTeam == null || !isOnline(player)
                    || getCarriedFlagTeam(player) != playerTeam
                    || getCastleTeamAtPosition(player.getPosition()) != playerTeam) {
                continue;
            }
            returnOwnFlagForCastleEntry(player);
        }
    }

    private static void resetOwnFlagIfEnteringCastle(Player player, Position destination) {
        Team playerTeam = gamePlayers.get(player);
        if (playerTeam == null || destination == null
                || getCarriedFlagTeam(player) != playerTeam
                || getCastleTeamAtPosition(destination) != playerTeam) {
            return;
        }
        returnOwnFlagForCastleEntry(player);
    }

    private static void returnOwnFlagForCastleEntry(Player player) {
        Team flagTeam = getCarriedFlagTeam(player);
        if (flagTeam == null || flagTeam != gamePlayers.get(player)) {
            return;
        }
        clearFlagWeapon(player);
        setFlagAtBase(flagTeam, true, null);
        player.getPacketSender().sendGameMessage(
                "You cannot carry your own flag into your castle. It returns to its stand.");
    }

    private static void returnCarriedFlagToBase(Player player) {
        Team flagTeam = getCarriedFlagTeam(player);
        if (flagTeam == null) {
            return;
        }
        clearFlagWeapon(player);
        setFlagAtBase(flagTeam, true, null);
    }

    public static boolean dropCarriedFlagManually(Player player) {
        if (player == null || !isInGame(player) || !isCarryingFlag(player)) {
            return false;
        }
        dropCarriedFlag(player);
        return true;
    }

    private static void dropCarriedFlag(Player player) {
        Team flagTeam = getCarriedFlagTeam(player);
        if (flagTeam == null) {
            return;
        }

        clearFlagWeapon(player);
        int flagId = flagTeam == Team.SARADOMIN ? SARADOMIN_FLAG_ID : ZAMORAK_FLAG_ID;
        GroundItem droppedFlag = new GroundItem(new ItemStack(flagId), player.getPosition(), false, true);
        setFlagAtBase(flagTeam, false, null);
        setDroppedFlag(flagTeam, droppedFlag);
        if (getCastleTeamAtPosition(droppedFlag.getPosition()) == flagTeam) {
            setDroppedFlagReturnMillis(flagTeam, 0L);
        } else {
            setDroppedFlagReturnMillis(
                    flagTeam, System.currentTimeMillis() + DROPPED_FLAG_AUTO_RETURN_MILLIS);
        }
        GroundItemManager.getInstance().spawn(droppedFlag);
        player.getPacketSender().sendGameMessage("You drop the " + getTeamName(flagTeam) + " flag.");
    }

    private static void cleanupDroppedFlags(long now) {
        cleanupDroppedFlag(Team.SARADOMIN, now);
        cleanupDroppedFlag(Team.ZAMORAK, now);
    }

    private static void cleanupDroppedFlag(Team flagTeam, long now) {
        GroundItem droppedFlag = getDroppedFlag(flagTeam);
        if (droppedFlag == null) {
            return;
        }
        if (!GroundItemManager.getInstance().contains(droppedFlag)) {
            setDroppedFlag(flagTeam, null);
            setDroppedFlagReturnMillis(flagTeam, 0L);
            setFlagAtBase(flagTeam, true, null);
            return;
        }

        if (getCastleTeamAtPosition(droppedFlag.getPosition()) == flagTeam) {
            // Inside its own castle the flag remains available for a manual
            // friendly reset instead of being removed by the normal ground-item lifetime.
            droppedFlag.getTimer().reset();
            setDroppedFlagReturnMillis(flagTeam, 0L);
            return;
        }

        long returnMillis = getDroppedFlagReturnMillis(flagTeam);
        if (returnMillis <= 0L) {
            returnMillis = now + DROPPED_FLAG_AUTO_RETURN_MILLIS;
            setDroppedFlagReturnMillis(flagTeam, returnMillis);
        }
        if (now >= returnMillis) {
            returnDroppedFlagToBase(flagTeam);
        }
    }

    private static void returnDroppedFlagToBase(Team flagTeam) {
        GroundItem droppedFlag = getDroppedFlag(flagTeam);
        if (droppedFlag == null) {
            return;
        }
        GroundItemManager.getInstance().remove(droppedFlag);
        setDroppedFlag(flagTeam, null);
        setDroppedFlagReturnMillis(flagTeam, 0L);
        setFlagAtBase(flagTeam, true, null);
    }

    private static void clearFlagWeapon(Player player) {
        ItemStack weapon = player.getEquipmentManager().getContainer().getItemAt(3);
        if (weapon != null && (weapon.getId() == SARADOMIN_FLAG_ID || weapon.getId() == ZAMORAK_FLAG_ID)) {
            player.getEquipmentManager().getContainer().setItem(3, null);
            player.getEquipmentManager().refresh();
            player.setAppearanceUpdateRequired(true);
        }
    }

    private static void takeBandages(Player player, int requestedAmount) {
        if (!isInGame(player)) {
            player.getPacketSender().sendGameMessage("You can only take bandages during a Castle Wars game.");
            return;
        }
        int amount = giveBandages(player, requestedAmount);
        if (amount <= 0) {
            player.getPacketSender().sendGameMessage("Not enough space in your inventory.");
            return;
        }
        player.getUpdateState().setAnimation(881);
        player.getPacketSender().sendGameMessage(amount == 1
                ? "You take a bandage."
                : "You take " + amount + " bandages.");
    }

    private static void passEnergyBarrier(Player player, Team barrierTeam, int objectX, int objectY) {
        Team playerTeam = gamePlayers.get(player);
        if (playerTeam == null) {
            player.getPacketSender().sendGameMessage("You can only pass this barrier during a Castle Wars game.");
            return;
        }
        if (playerTeam != barrierTeam) {
            player.getPacketSender().sendGameMessage("You are not allowed in the other team's spawn room.");
            return;
        }

        Position position = player.getPosition();
        if (barrierTeam == Team.SARADOMIN) {
            if (objectX == 2426 && objectY == 3080) {
                int y = position.getY() <= 3080 ? 3081 : 3080;
                player.moveTo(new Position(2426, y, 1));
                return;
            }
            if (objectX == 2422 && objectY == 3076) {
                int x = position.getX() <= 2422 ? 2423 : 2422;
                player.moveTo(new Position(x, 3076, 1));
                return;
            }
        } else {
            if (objectX == 2373 && objectY == 3126) {
                int y = position.getY() <= 3126 ? 3127 : 3126;
                player.moveTo(new Position(2373, y, 1));
                return;
            }
            if (objectX == 2377 && objectY == 3131) {
                int x = position.getX() <= 2376 ? 2377 : 2376;
                player.moveTo(new Position(x, 3131, 1));
                return;
            }
        }
        player.getPacketSender().sendGameMessage("You cannot pass the barrier from here.");
    }

    private static void useSpawnRoomLadder(Player player, Team ladderTeam, boolean climbUp) {
        player.getUpdateState().setAnimation(climbUp ? 828 : 827);
        if (ladderTeam == Team.SARADOMIN) {
            player.moveTo(new Position(2429, 3075, climbUp ? 2 : 1));
        } else {
            player.moveTo(new Position(2370, 3132, climbUp ? 2 : 1));
        }
    }

    private static void useSteppingStone(Player player, int objectX, int objectY) {
        if (player.getPosition().getPlane() != 0) {
            return;
        }
        if (player.isMovementLocked()) {
            player.getPacketSender().sendGameMessage("You cannot use the stepping stones while unable to move.");
            return;
        }

        Position destination = new Position(objectX, objectY, 0);
        if (!isSteppingStoneTile(destination)) {
            return;
        }
        if (!jumpSteppingStone(player, destination)) {
            player.getPacketSender().sendGameMessage("You need to jump to the next stepping stone.");
        }
    }

    public static Position getSteppingStoneApproach(Team sourceTeam, int routeVariant) {
        Position[] route = steppingStoneRoute(routeVariant);
        if (route == null || sourceTeam == null) {
            return null;
        }
        return sourceTeam == Team.SARADOMIN ? route[0] : route[route.length - 1];
    }

    public static Position getSteppingStoneExit(Team sourceTeam, int routeVariant) {
        Position[] route = steppingStoneRoute(routeVariant);
        if (route == null || sourceTeam == null) {
            return null;
        }
        return sourceTeam == Team.SARADOMIN ? route[route.length - 1] : route[0];
    }

    public static Position getSteppingStoneNextStep(Position current, Team sourceTeam, int routeVariant) {
        Position[] route = steppingStoneRoute(routeVariant);
        if (route == null || current == null || sourceTeam == null) {
            return null;
        }

        int index = steppingStoneRouteIndex(route, current);
        if (index < 0) {
            return null;
        }
        int nextIndex = sourceTeam == Team.SARADOMIN ? index + 1 : index - 1;
        if (nextIndex < 0 || nextIndex >= route.length) {
            return null;
        }
        return route[nextIndex];
    }

    public static Position getSteppingStoneShortcutWaypoint(Position current, Position destination) {
        if (current == null || destination == null
                || current.getPlane() != 0 || destination.getPlane() != 0) {
            return null;
        }

        Position[][] routes = new Position[][]{
            SOUTHWEST_STEPPING_STONE_ROUTE,
            NORTHEAST_STEPPING_STONE_ROUTE
        };

        // Once a bot is on a crossing, keep it moving along the stones toward
        // its target (including when the target is also standing on a stone).
        for (Position[] route : routes) {
            int currentIndex = steppingStoneRouteIndex(route, current);
            if (currentIndex < 0) {
                continue;
            }

            int destinationIndex = steppingStoneRouteIndex(route, destination);
            if (destinationIndex >= 0 && destinationIndex != currentIndex) {
                return route[currentIndex + (destinationIndex > currentIndex ? 1 : -1)];
            }

            int startDistance = GameUtil.getDistance(destination, route[0]);
            int endDistance = GameUtil.getDistance(destination, route[route.length - 1]);
            if (currentIndex == 0) {
                return endDistance + 2 < startDistance ? route[1] : null;
            }
            if (currentIndex == route.length - 1) {
                return startDistance + 2 < endDistance ? route[route.length - 2] : null;
            }
            return endDistance < startDistance ? route[currentIndex + 1] : route[currentIndex - 1];
        }

        Position bestApproach = null;
        int bestScore = Integer.MAX_VALUE;
        for (Position[] route : routes) {
            int last = route.length - 1;
            int destinationIndex = steppingStoneRouteIndex(route, destination);

            // A target already on the stones is always worth pursuing through
            // whichever end gives the shorter chase.
            if (destinationIndex > 0 && destinationIndex < last) {
                int startApproach = GameUtil.getDistance(current, route[0]);
                int endApproach = GameUtil.getDistance(current, route[last]);
                int startScore = startApproach + destinationIndex;
                int endScore = endApproach + (last - destinationIndex);
                if (startApproach <= 12 && startScore < bestScore) {
                    bestApproach = route[0];
                    bestScore = startScore;
                }
                if (endApproach <= 12 && endScore < bestScore) {
                    bestApproach = route[last];
                    bestScore = endScore;
                }
                continue;
            }

            Position[] starts = new Position[]{route[0], route[last]};
            Position[] ends = new Position[]{route[last], route[0]};
            for (int direction = 0; direction < 2; ++direction) {
                int approachDistance = GameUtil.getDistance(current, starts[direction]);
                int destinationExitDistance = GameUtil.getDistance(destination, ends[direction]);
                int destinationEntryDistance = GameUtil.getDistance(destination, starts[direction]);
                if (approachDistance > 10
                        || destinationExitDistance > 18
                        || destinationExitDistance + 3 >= destinationEntryDistance) {
                    continue;
                }

                int score = approachDistance + last + destinationExitDistance;
                if (score < bestScore) {
                    bestScore = score;
                    bestApproach = starts[direction];
                }
            }
        }
        return bestApproach;
    }

    public static boolean jumpSteppingStone(Player player, Position destination) {
        if (player == null || destination == null || player.isMovementLocked()
                || player.isActionLocked()
                || player.getPosition().getPlane() != 0 || destination.getPlane() != 0
                || !isInGame(player)) {
            return false;
        }

        Position current = player.getPosition();
        if (!areConsecutiveSteppingStoneTiles(current, destination)) {
            return false;
        }

        player.getMovementQueue().clear();
        player.getMovementQueue().clearMovementActions();
        player.setActionLocked(true);
        // Face the stone before the delayed hop so the jump is not played
        // sideways/backwards when the player's previous facing differs.
        player.getUpdateState().setFacePosition(destination.copy());
        player.getUpdateState().setAnimation(STEPPING_STONE_JUMP_ANIMATION);
        World.scheduleTickTask(new SteppingStoneJumpTask(
                player, current.copy(), destination.copy()));
        return true;
    }

    private static Position[] steppingStoneRoute(int routeVariant) {
        if (routeVariant == 1) {
            return SOUTHWEST_STEPPING_STONE_ROUTE;
        }
        if (routeVariant == 2) {
            return NORTHEAST_STEPPING_STONE_ROUTE;
        }
        return null;
    }

    private static boolean isSteppingStoneTile(Position position) {
        return isInteriorSteppingStoneTile(SOUTHWEST_STEPPING_STONE_ROUTE, position)
                || isInteriorSteppingStoneTile(NORTHEAST_STEPPING_STONE_ROUTE, position);
    }

    private static boolean isInteriorSteppingStoneTile(Position[] route, Position position) {
        int index = steppingStoneRouteIndex(route, position);
        return index > 0 && index < route.length - 1;
    }

    private static boolean areConsecutiveSteppingStoneTiles(Position first, Position second) {
        return areConsecutiveInRoute(SOUTHWEST_STEPPING_STONE_ROUTE, first, second)
                || areConsecutiveInRoute(NORTHEAST_STEPPING_STONE_ROUTE, first, second);
    }

    private static boolean areConsecutiveInRoute(Position[] route, Position first, Position second) {
        int firstIndex = steppingStoneRouteIndex(route, first);
        int secondIndex = steppingStoneRouteIndex(route, second);
        return firstIndex >= 0 && secondIndex >= 0 && Math.abs(firstIndex - secondIndex) == 1;
    }

    private static int steppingStoneRouteIndex(Position[] route, Position position) {
        if (route == null || position == null || position.getPlane() != 0) {
            return -1;
        }
        for (int index = 0; index < route.length; ++index) {
            Position routePosition = route[index];
            if (routePosition.getX() == position.getX()
                    && routePosition.getY() == position.getY()) {
                return index;
            }
        }
        return -1;
    }

    public static boolean isGroundCastleExteriorTransitionTile(Position position) {
        if (position == null || position.getPlane() != 0) {
            return false;
        }
        int x = position.getX();
        int y = position.getY();
        return (y == 3089 && (x == 2426 || x == 2427))
                || (y == 3118 && (x == 2372 || x == 2373));
    }

    public static Position getBotMainDoorExteriorPosition(Player player, Team castleTeam) {
        if (player == null || castleTeam == null) {
            return null;
        }
        boolean alternateLane = (player.getNameHash() & 1L) != 0L;
        if (castleTeam == Team.SARADOMIN) {
            // Closed double doors are at y=3088, so y=3089 is the exact
            // battlefield-side melee interaction tile.
            return new Position(alternateLane ? 2427 : 2426, 3089, 0);
        }
        // Closed double doors are at y=3119, so y=3118 is the exact
        // battlefield-side melee interaction tile.
        return new Position(alternateLane ? 2372 : 2373, 3118, 0);
    }

    public static Position getBotMainDoorInteriorPosition(Player player, Team castleTeam) {
        if (player == null || castleTeam == null) {
            return null;
        }
        boolean alternateLane = (player.getNameHash() & 1L) != 0L;
        if (castleTeam == Team.SARADOMIN) {
            return new Position(alternateLane ? 2427 : 2426, 3086, 0);
        }
        return new Position(alternateLane ? 2372 : 2373, 3121, 0);
    }

    private static Position getBotMainDoorInteriorInteractionPosition(Player player,
                                                                      Team castleTeam) {
        if (player == null || castleTeam == null) {
            return null;
        }
        boolean alternateLane = (player.getNameHash() & 1L) != 0L;
        if (castleTeam == Team.SARADOMIN) {
            return new Position(alternateLane ? 2427 : 2426, 3087, 0);
        }
        return new Position(alternateLane ? 2372 : 2373, 3120, 0);
    }

    public static Team getCastleTeamAtPosition(Position position) {
        if (position == null || position.getPlane() < 0 || position.getPlane() > 3) {
            return null;
        }
        if (isGroundCastleExteriorTransitionTile(position)) {
            return null;
        }
        int x = position.getX();
        int y = position.getY();
        if ((x >= 2415 && x <= 2431 && y >= 3072 && y <= 3083)
                || (x >= 2412 && x <= 2431 && y >= 3084 && y <= 3089)) {
            return Team.SARADOMIN;
        }
        if ((x >= 2368 && x <= 2384 && y >= 3124 && y <= 3135)
                || (x >= 2368 && x <= 2387 && y >= 3117 && y <= 3123)) {
            return Team.ZAMORAK;
        }
        return null;
    }

    public static boolean isCastleBattlementFiringPosition(Position position) {
        Team team = getCastleTeamAtPosition(position);
        return team != null
                && position.getPlane() == 0
                && CastleWarsEngineeringManager.isBattlementWalkwayTile(position, team);
    }

    private static boolean isFacingOutFromBattlement(Position wallPosition,
                                                      Position battlefieldPosition) {
        Team team = getCastleTeamAtPosition(wallPosition);
        if (team == null) {
            return false;
        }
        int x = wallPosition.getX();
        int y = wallPosition.getY();
        int targetX = battlefieldPosition.getX();
        int targetY = battlefieldPosition.getY();
        return getCastleTeamAtPosition(new Position(x - 1, y, 0)) != team && targetX < x
                || getCastleTeamAtPosition(new Position(x + 1, y, 0)) != team && targetX > x
                || getCastleTeamAtPosition(new Position(x, y - 1, 0)) != team && targetY < y
                || getCastleTeamAtPosition(new Position(x, y + 1, 0)) != team && targetY > y;
    }

    public static boolean isCastleWallCrossLevelPair(Player first, Player second) {
        if (first == null || second == null
                || !isInGame(first) || !isInGame(second)
                || !areOpponents(first, second)) {
            return false;
        }

        Position firstPosition = first.getPosition();
        Position secondPosition = second.getPosition();
        if (firstPosition.getPlane() != 0 || secondPosition.getPlane() != 0) {
            return false;
        }

        boolean firstOnWall = isCastleBattlementFiringPosition(firstPosition);
        boolean secondOnWall = isCastleBattlementFiringPosition(secondPosition);
        if (firstOnWall == secondOnWall) {
            return false;
        }

        Position wallPosition = firstOnWall ? firstPosition : secondPosition;
        Position battlefieldPosition = firstOnWall ? secondPosition : firstPosition;
        Team wallTeam = getCastleTeamAtPosition(wallPosition);
        if (getCastleTeamAtPosition(battlefieldPosition) == wallTeam
                || !isFacingOutFromBattlement(wallPosition, battlefieldPosition)) {
            return false;
        }
        return GameUtil.getDistance(wallPosition, battlefieldPosition) <= 15;
    }

    public static boolean hasBotCombatLineOfSight(Player attacker, Player target) {
        if (attacker == null || target == null
                || attacker.getPosition().getPlane() != target.getPosition().getPlane()) {
            return false;
        }
        if (attacker.botEnabled && attacker.botPrimaryCombatStyle != 0
                && isCastleWallCrossLevelPair(attacker, target)) {
            return true;
        }
        return GameUtil.hasClearPath(attacker.getPosition(), target.getPosition(), false);
    }

    public static boolean routeBotOneCastleLevel(Player player, Team castleTeam, boolean up) {
        if (player == null || castleTeam == null || !player.isBot) {
            return false;
        }

        int plane = player.getPosition().getPlane();
        Position approach = null;
        int objectId = -1;
        int objectX = 0;
        int objectY = 0;

        if (castleTeam == Team.SARADOMIN) {
            if (up && plane == 0) {
                approach = new Position(2419, 3077, 0);
                objectId = 4417; objectX = 2419; objectY = 3078;
            } else if (up && plane == 1) {
                approach = new Position(2427, 3081, 1);
                objectId = 4417; objectX = 2428; objectY = 3081;
            } else if (up && plane == 2) {
                approach = new Position(2425, 3077, 2);
                objectId = 4417; objectX = 2425; objectY = 3074;
            } else if (!up && plane == 3) {
                approach = new Position(2426, 3074, 3);
                objectId = 4415; objectX = 2425; objectY = 3074;
            } else if (!up && plane == 2) {
                approach = new Position(2430, 3080, 2);
                objectId = 4415; objectX = 2430; objectY = 3081;
            } else if (!up && plane == 1) {
                approach = new Position(2420, 3080, 1);
                objectId = 4415; objectX = 2419; objectY = 3080;
            }
        } else {
            if (up && plane == 0) {
                approach = new Position(2380, 3130, 0);
                objectId = 4418; objectX = 2380; objectY = 3127;
            } else if (up && plane == 1) {
                approach = new Position(2372, 3126, 1);
                objectId = 4418; objectX = 2369; objectY = 3126;
            } else if (up && plane == 2) {
                approach = new Position(2374, 3130, 2);
                objectId = 4418; objectX = 2374; objectY = 3131;
            } else if (!up && plane == 3) {
                approach = new Position(2373, 3133, 3);
                objectId = 4415; objectX = 2374; objectY = 3133;
            } else if (!up && plane == 2) {
                approach = new Position(2369, 3127, 2);
                objectId = 4415; objectX = 2369; objectY = 3126;
            } else if (!up && plane == 1) {
                approach = new Position(2379, 3127, 1);
                objectId = 4415; objectX = 2380; objectY = 3127;
            }
        }

        if (approach == null) {
            return false;
        }
        if (!samePosition(player.getPosition(), approach)) {
            player.getMovementQueue().setRunning(true);
            PathFinder.findPath(player, approach.getX(), approach.getY(), false, 0, 0);
            player.getMovementQueue().clearMovementActions();
            return true;
        }

        handleFirstObjectAction(player, objectId, objectX, objectY);
        return true;
    }

    public static boolean routeBotThroughGroundCastle(Player player, Team castleTeam,
                                                       boolean enteringCastle) {
        if (player == null || castleTeam == null || !player.isBot
                || player.getPosition().getPlane() != 0) {
            return false;
        }

        Position exterior = getBotMainDoorExteriorPosition(player, castleTeam);
        Position interior = getBotMainDoorInteriorPosition(player, castleTeam);
        if (exterior == null || interior == null) {
            return false;
        }

        if (enteringCastle) {
            // Always reach the exact battlefield-side interaction tile first.
            // Previously this was two/three tiles from the actual closed door,
            // so tryHandleMainDoorForBot() could never satisfy distance == 1
            // and melee bots simply stood in front of the castle.
            if (!samePosition(player.getPosition(), exterior)
                    && getCastleTeamAtPosition(player.getPosition()) != castleTeam) {
                player.getMovementQueue().setRunning(true);
                PathFinder.findPath(player, exterior.getX(), exterior.getY(), false, 0, 0);
                player.getMovementQueue().clearMovementActions();
                return true;
            }

            if (CastleWarsEngineeringManager.tryHandleMainDoorForBot(player, castleTeam)) {
                return true;
            }

            if (samePosition(player.getPosition(), interior)) {
                return true;
            }
            player.getMovementQueue().setRunning(true);
            PathFinder.findPath(player, interior.getX(), interior.getY(), false, 0, 0);
            player.getMovementQueue().clearMovementActions();
            return true;
        }

        if (samePosition(player.getPosition(), exterior)) {
            return true;
        }

        Position interiorInteraction =
                getBotMainDoorInteriorInteractionPosition(player, castleTeam);
        if (getCastleTeamAtPosition(player.getPosition()) == castleTeam
                && interiorInteraction != null
                && !samePosition(player.getPosition(), interiorInteraction)) {
            player.getMovementQueue().setRunning(true);
            PathFinder.findPath(player, interiorInteraction.getX(),
                    interiorInteraction.getY(), false, 0, 0);
            player.getMovementQueue().clearMovementActions();
            return true;
        }

        if (CastleWarsEngineeringManager.tryHandleMainDoorForBot(player, castleTeam)) {
            return true;
        }

        player.getMovementQueue().setRunning(true);
        PathFinder.findPath(player, exterior.getX(), exterior.getY(), false, 0, 0);
        player.getMovementQueue().clearMovementActions();
        return true;
    }

    public static boolean canBotTargetAcrossCastleLevels(Player attacker, Player target) {
        return attacker != null
                && attacker.botEnabled
                && attacker.botPrimaryCombatStyle != 0
                && isCastleWallCrossLevelPair(attacker, target);
    }

    public static boolean canBotAttackAcrossCastleLevels(Player attacker, Player target) {
        return attacker != null
                && attacker.botEnabled
                && attacker.botActiveCombatStyle != 0
                && isCastleWallCrossLevelPair(attacker, target);
    }

    public static boolean canAttackAcrossCastleLevels(Player attacker, Player target,
                                                       CombatType combatType) {
        return attacker != null
                && target != null
                && (combatType == CombatType.RANGED || combatType == CombatType.MAGIC)
                && isCastleWallCrossLevelPair(attacker, target);
    }

    public static boolean canBotAttackAcrossCastleLevels(Player attacker, Player target,
                                                          CombatType combatType) {
        return attacker != null
                && attacker.botEnabled
                && canAttackAcrossCastleLevels(attacker, target, combatType);
    }

    public static boolean isCastleWallCrossLevelBotCombatPair(Player first, Player second) {
        if (!isCastleWallCrossLevelPair(first, second)) {
            return false;
        }
        return first.botEnabled && first.botPrimaryCombatStyle != 0
                || second.botEnabled && second.botPrimaryCombatStyle != 0;
    }

    public static int getBotCastleWallEngageRange(Player bot) {
        if (bot == null || !bot.botEnabled) {
            return 0;
        }
        return bot.botPrimaryCombatStyle == 2 ? 10
                : bot.botPrimaryCombatStyle == 1 ? 7 : 0;
    }

    public static boolean isCastleBattlementPosition(Position position) {
        if (position == null || position.getPlane() != 0) {
            return false;
        }

        int x = position.getX();
        int y = position.getY();
        boolean saradominCastle = (x >= 2415 && x <= 2431
                && y >= 3072 && y <= 3083)
                || (x >= 2412 && x <= 2431
                && y >= 3084 && y <= 3089);
        boolean zamorakCastle = (x >= 2368 && x <= 2384
                && y >= 3124 && y <= 3135)
                || (x >= 2368 && x <= 2387
                && y >= 3117 && y <= 3123);
        return saradominCastle || zamorakCastle;
    }

    public static Position getStairTraversalApproach(Player player, int objectId,
                                                    int objectX, int objectY) {
        if (player == null) {
            return null;
        }
        int plane = player.getPosition().getPlane();

        // Spawn-room ladders need an exact reachable approach. Without this,
        // generic object pathing can stop on the wrong side of the surrounding wall.
        if (objectId == SARADOMIN_SPAWN_LADDER_ID
                && objectX == 2429 && objectY == 3075 && plane == 1) {
            return new Position(2429, 3074, 1);
        }
        if (objectId == ZAMORAK_SPAWN_LADDER_ID
                && objectX == 2370 && objectY == 3132 && plane == 1) {
            return new Position(2370, 3133, 1);
        }

        // Saradomin ground stairs.
        if (objectId == 4419 && objectX == 2417 && objectY == 3074 && plane == 0) {
            Position outside = new Position(2416, 3074, 0);
            Position inside = new Position(2417, 3077, 0);
            return nearestPosition(player.getPosition(), outside, inside);
        }

        // Saradomin stairs up.
        if (objectId == 4417) {
            if (objectX == 2419 && objectY == 3078 && plane == 0) {
                return new Position(2419, 3077, 0);
            }
            if (objectX == 2428 && objectY == 3081 && plane == 1) {
                return new Position(2427, 3081, 1);
            }
            if (objectX == 2425 && objectY == 3074 && plane == 2) {
                return new Position(2425, 3077, 2);
            }
        }

        // Shared down-stair object.
        if (objectId == 4415) {
            if (objectX == 2419 && objectY == 3080 && plane == 1) {
                return new Position(2420, 3080, 1);
            }
            if (objectX == 2430 && objectY == 3081 && plane == 2) {
                return new Position(2430, 3080, 2);
            }
            if (objectX == 2425 && objectY == 3074 && plane == 3) {
                return new Position(2426, 3074, 3);
            }
            if (objectX == 2380 && objectY == 3127 && plane == 1) {
                return new Position(2379, 3127, 1);
            }
            if (objectX == 2369 && objectY == 3126 && plane == 2) {
                return new Position(2369, 3127, 2);
            }
            if (objectX == 2374 && objectY == 3133 && plane == 3) {
                return new Position(2373, 3133, 3);
            }
        }

        // Zamorak ground stairs.
        if (objectId == 4420 && objectX == 2382 && objectY == 3131 && plane == 0) {
            Position outside = new Position(2383, 3133, 0);
            Position inside = new Position(2382, 3130, 0);
            return nearestPosition(player.getPosition(), outside, inside);
        }

        // Zamorak stairs up.
        if (objectId == 4418) {
            if (objectX == 2380 && objectY == 3127 && plane == 0) {
                return new Position(2380, 3130, 0);
            }
            if (objectX == 2369 && objectY == 3126 && plane == 1) {
                return new Position(2372, 3126, 1);
            }
            if (objectX == 2374 && objectY == 3131 && plane == 2) {
                return new Position(2374, 3130, 2);
            }
        }
        return null;
    }

    public static boolean isAtStairTraversalApproach(Player player, int objectId,
                                                     int objectX, int objectY) {
        Position approach = getStairTraversalApproach(player, objectId, objectX, objectY);
        return approach != null && samePosition(player.getPosition(), approach);
    }

    private static Position nearestPosition(Position origin, Position first, Position second) {
        return GameUtil.getDistance(origin, first) <= GameUtil.getDistance(origin, second)
                ? first : second;
    }

    public static Position getNearestAdjacentInteractionTile(Player player, Position target) {
        if (player == null || target == null
                || player.getPosition().getPlane() != target.getPlane()) {
            return null;
        }
        Position current = player.getPosition();
        if (GameUtil.getDistance(current, target) == 1) {
            return current.copy();
        }

        Position best = null;
        int bestDistance = Integer.MAX_VALUE;
        for (int dx = -1; dx <= 1; ++dx) {
            for (int dy = -1; dy <= 1; ++dy) {
                if (dx == 0 && dy == 0) {
                    continue;
                }
                int x = target.getX() + dx;
                int y = target.getY() + dy;
                if ((WalkingCollisionMap.getTileFlags(x, y, target.getPlane()) & 0x1280100) != 0) {
                    continue;
                }
                Position candidate = new Position(x, y, target.getPlane());
                int distance = GameUtil.getDistance(current, candidate);
                if (distance < bestDistance) {
                    bestDistance = distance;
                    best = candidate;
                }
            }
        }
        return best == null ? null : best.copy();
    }

    private static boolean samePosition(Position first, Position second) {
        return first != null && second != null
                && first.getX() == second.getX()
                && first.getY() == second.getY()
                && first.getPlane() == second.getPlane();
    }

    private static void moveThroughCastleWarsStairs(Player player, Position destination) {
        resetOwnFlagIfEnteringCastle(player, destination);
        boolean planeChange = player.getPosition().getPlane() != destination.getPlane();
        player.moveTo(destination);
        if (planeChange && !player.isBot) {
            // Same-region height changes can leave the client using stale
            // staircase occlusion until the camera is moved manually.
            player.getPacketSender().resetCamera();
        }
    }

    public static boolean moveBotThroughGroundCastleStairs(Player player, Team castleTeam,
                                                            boolean enteringCastle) {
        if (player == null || castleTeam == null || !player.isBot
                || player.getPosition().getPlane() != 0) {
            return false;
        }

        Position approach;
        Position destination;
        if (castleTeam == Team.SARADOMIN) {
            approach = enteringCastle
                    ? new Position(2416, 3074, 0)
                    : new Position(2417, 3077, 0);
            destination = enteringCastle
                    ? new Position(2417, 3077, 0)
                    : new Position(2416, 3074, 0);
        } else {
            approach = enteringCastle
                    ? new Position(2383, 3133, 0)
                    : new Position(2382, 3130, 0);
            destination = enteringCastle
                    ? new Position(2382, 3130, 0)
                    : new Position(2383, 3133, 0);
        }

        // Only traverse from the exact interaction square. Bots must reach it
        // through normal clipped movement just like a player; never teleport
        // the final tile because an adjacent square may be separated by a wall.
        if (!samePosition(player.getPosition(), approach)) {
            return false;
        }

        player.getMovementQueue().reset();
        moveThroughCastleWarsStairs(player, destination);
        return true;
    }

    private static boolean handleCastleWarsTraversal(Player player, int objectId, int objectX, int objectY) {
        int plane = player.getPosition().getPlane();
        Position stairApproach = getStairTraversalApproach(player, objectId, objectX, objectY);
        if (stairApproach != null && !samePosition(player.getPosition(), stairApproach)) {
            return false;
        }

        // Saradomin castle stairs.
        if (objectId == 4419 && objectX == 2417 && objectY == 3074 && plane == 0) {
            if (player.getPosition().getX() <= 2416) {
                moveThroughCastleWarsStairs(player, new Position(2417, 3077, 0));
            } else {
                moveThroughCastleWarsStairs(player, new Position(2416, 3074, 0));
            }
            return true;
        }
        if (objectId == 4417) {
            if (objectX == 2419 && objectY == 3078 && plane == 0) {
                moveThroughCastleWarsStairs(player, new Position(2420, 3080, 1));
                return true;
            }
            if (objectX == 2428 && objectY == 3081 && plane == 1) {
                moveThroughCastleWarsStairs(player, new Position(2430, 3080, 2));
                return true;
            }
            if (objectX == 2425 && objectY == 3074 && plane == 2) {
                moveThroughCastleWarsStairs(player, new Position(2426, 3074, 3));
                return true;
            }
        }
        if (objectId == 4415) {
            if (objectX == 2419 && objectY == 3080 && plane == 1) {
                moveThroughCastleWarsStairs(player, new Position(2419, 3077, 0));
                return true;
            }
            if (objectX == 2430 && objectY == 3081 && plane == 2) {
                moveThroughCastleWarsStairs(player, new Position(2427, 3081, 1));
                return true;
            }
            if (objectX == 2425 && objectY == 3074 && plane == 3) {
                moveThroughCastleWarsStairs(player, new Position(2425, 3077, 2));
                return true;
            }

            // Zamorak castle stairs.
            if (objectX == 2380 && objectY == 3127 && plane == 1) {
                moveThroughCastleWarsStairs(player, new Position(2380, 3130, 0));
                return true;
            }
            if (objectX == 2369 && objectY == 3126 && plane == 2) {
                moveThroughCastleWarsStairs(player, new Position(2372, 3126, 1));
                return true;
            }
            if (objectX == 2374 && objectY == 3133 && plane == 3) {
                moveThroughCastleWarsStairs(player, new Position(2374, 3130, 2));
                return true;
            }
        }

        // Zamorak castle stairs.
        if (objectId == 4420 && objectX == 2382 && objectY == 3131 && plane == 0) {
            if (player.getPosition().getX() >= 2383) {
                moveThroughCastleWarsStairs(player, new Position(2382, 3130, 0));
            } else {
                moveThroughCastleWarsStairs(player, new Position(2383, 3133, 0));
            }
            return true;
        }
        if (objectId == 4418) {
            if (objectX == 2380 && objectY == 3127 && plane == 0) {
                moveThroughCastleWarsStairs(player, new Position(2379, 3127, 1));
                return true;
            }
            if (objectX == 2369 && objectY == 3126 && plane == 1) {
                moveThroughCastleWarsStairs(player, new Position(2369, 3127, 2));
                return true;
            }
            if (objectX == 2374 && objectY == 3131 && plane == 2) {
                moveThroughCastleWarsStairs(player, new Position(2373, 3133, 3));
                return true;
            }
        }

        // Ladders between the ground floor and first floor outside the spawn rooms.
        if (objectId == 4911 && plane == 1) {
            if (objectX == 2421 && objectY == 3073) {
                player.getUpdateState().setAnimation(827);
                player.moveTo(new Position(2421, 3074, 0));
                return true;
            }
            if (objectX == 2378 && objectY == 3134) {
                player.getUpdateState().setAnimation(827);
                player.moveTo(new Position(2378, 3133, 0));
                return true;
            }
        }
        if (objectId == 1747 && plane == 0) {
            if (objectX == 2421 && objectY == 3073) {
                player.getUpdateState().setAnimation(828);
                player.moveTo(new Position(2421, 3074, 1));
                return true;
            }
            if (objectX == 2378 && objectY == 3134) {
                player.getUpdateState().setAnimation(828);
                player.moveTo(new Position(2378, 3133, 1));
                return true;
            }
        }

        // Castle-to-tunnel ladders.
        if (objectId == 4912 && plane == 0) {
            if (objectX == 2430 && objectY == 3082) {
                player.getUpdateState().setAnimation(827);
                player.moveTo(new Position(2430, 9483, 0));
                return true;
            }
            if (objectX == 2369 && objectY == 3125) {
                player.getUpdateState().setAnimation(827);
                player.moveTo(new Position(2369, 9524, 0));
                return true;
            }
        }
        if (objectId == 1757 && plane == 0) {
            if (objectX == 2430 && objectY == 9482) {
                player.getUpdateState().setAnimation(828);
                moveThroughCastleWarsStairs(player, new Position(2430, 3081, 0));
                return true;
            }
            if (objectX == 2369 && objectY == 9525) {
                player.getUpdateState().setAnimation(828);
                moveThroughCastleWarsStairs(player, new Position(2369, 3126, 0));
                return true;
            }
            if (objectX == 2400 && objectY == 9508) {
                player.getUpdateState().setAnimation(828);
                player.moveTo(new Position(2400, 3107, 0));
                return true;
            }
            if (objectX == 2399 && objectY == 9499) {
                player.getUpdateState().setAnimation(828);
                player.moveTo(new Position(2399, 3100, 0));
                return true;
            }
        }

        // Surface trapdoors to the central tunnels.
        if (objectId == 1568 && plane == 0) {
            player.getUpdateState().setAnimation(827);
            if (objectX == 2399 && objectY == 3099) {
                player.moveTo(new Position(2399, 9500, 0));
            } else if (objectX == 2400 && objectY == 3108) {
                player.moveTo(new Position(2400, 9507, 0));
            } else {
                return false;
            }
            return true;
        }

        return false;
    }

    public static void respawnPlayer(Player player) {
        Team team = gamePlayers.get(player);
        if (team == null) {
            return;
        }
        dropCarriedFlag(player);
        // Castle Wars supplies and bandages are lost on death, but ordinary
        // equipment, runes, potions and weapons remain with the player.
        removeBandages(player);
        CastleWarsEngineeringManager.cleanupPlayerSupplies(player);
        player.clearPvpCombatReferences();
        moveToTeamSpawn(player, team);
        if (player.botEnabled) {
            BotCombatHelper.restorePrimaryCombatGear(player);
            BotCombatHelper.syncPrimaryMagicAutocast(player);
        }
        player.getPacketSender().sendGameMessage("You respawn in your team's castle.");
    }

    public static void moveToLobby(Player player) {
        player.moveTo(new Position(
            CASTLE_WARS_LOBBY.getX() - 2 + GameUtil.randomInt(5),
            CASTLE_WARS_LOBBY.getY() - 2 + GameUtil.randomInt(5),
            CASTLE_WARS_LOBBY.getPlane()
        ));
    }

    public static void openCastleWarsManual(Player player) {
        if (player == null) {
            return;
        }
        player.activeBookItemId = CASTLE_WARS_MANUAL_ID;
        player.activeBookPageIndex = 0;
        renderCastleWarsManual(player);
    }

    public static boolean handleCastleWarsManualButton(Player player, int buttonId) {
        if (player == null || player.activeBookItemId != CASTLE_WARS_MANUAL_ID) {
            return false;
        }

        if (buttonId == CASTLE_WARS_MANUAL_CLOSE_BUTTON_ID) {
            player.getPacketSender().closeInterfaces();
            return true;
        }

        // 839/841 are the native cache-377 book arrows. 3071/3073 are accepted
        // too because some compatible clients remap the same book controls.
        if (buttonId == CASTLE_WARS_MANUAL_PREVIOUS_BUTTON_ID || buttonId == 3071) {
            if (player.activeBookPageIndex > 0) {
                --player.activeBookPageIndex;
                renderCastleWarsManual(player);
            }
            return true;
        }

        if (buttonId == CASTLE_WARS_MANUAL_NEXT_BUTTON_ID || buttonId == 3073) {
            if (player.activeBookPageIndex + 1 < CASTLE_WARS_MANUAL_PAGES.length) {
                ++player.activeBookPageIndex;
                renderCastleWarsManual(player);
            }
            return true;
        }

        return false;
    }

    private static void renderCastleWarsManual(Player player) {
        int spread = player.activeBookPageIndex;
        if (spread < 0) {
            spread = 0;
        } else if (spread >= CASTLE_WARS_MANUAL_PAGES.length) {
            spread = CASTLE_WARS_MANUAL_PAGES.length - 1;
        }
        player.activeBookPageIndex = spread;

        String[] lines = CASTLE_WARS_MANUAL_PAGES[spread];
        for (int index = 0; index < CASTLE_WARS_MANUAL_LINE_COUNT; ++index) {
            String text = index < lines.length ? lines[index] : "";
            player.getPacketSender().sendInterfaceText(
                    text, CASTLE_WARS_MANUAL_FIRST_LINE_ID + index);
        }

        int leftPage = spread * 2 + 1;
        int rightPage = leftPage + 1;
        player.getPacketSender().sendInterfaceText(
                "Castle Wars Manual", CASTLE_WARS_MANUAL_TITLE_TEXT_ID);
        player.getPacketSender().sendInterfaceText(
                "Page " + leftPage, CASTLE_WARS_MANUAL_LEFT_PAGE_TEXT_ID);
        player.getPacketSender().sendInterfaceText(
                "Page " + rightPage, CASTLE_WARS_MANUAL_RIGHT_PAGE_TEXT_ID);
        player.getPacketSender().showInterface(CASTLE_WARS_MANUAL_INTERFACE_ID);
    }

    private static void startGame(long now) {
        cleanupWaitingPlayers();
        if (!hasMinimumPlayersToStartInternal()) {
            nextGameStartMillis = -1L;
            return;
        }

        IdentityHashMap<Player, Team> starters = new IdentityHashMap<Player, Team>(waitingPlayers);
        waitingPlayers.clear();
        replacementOffers.clear();
        replacementOfferCooldowns.clear();

        saradominScore = 0;
        zamorakScore = 0;
        CastleWarsEngineeringManager.resetForGame();
        saradominFlagHolder = null;
        zamorakFlagHolder = null;
        saradominDroppedFlag = null;
        zamorakDroppedFlag = null;
        saradominDroppedFlagReturnMillis = 0L;
        zamorakDroppedFlagReturnMillis = 0L;
        setFlagAtBase(Team.SARADOMIN, true, null);
        setFlagAtBase(Team.ZAMORAK, true, null);
        gameInProgress = true;
        gameEndMillis = now + GAME_DURATION_SECONDS * 1000L;
        nextGameStartMillis = -1L;

        for (Map.Entry<Player, Team> entry : starters.entrySet()) {
            Player player = entry.getKey();
            Team team = entry.getValue();
            if (!isOnline(player)) {
                continue;
            }
            clearWaitingRoomGodTransformation(player);
            gamePlayers.put(player, team);
            if (!isWearingTeamColours(player, team)) {
                equipTeamColours(player, team);
            }
            setCastleWarsAttackOption(player, true);
            player.clearPvpCombatReferences();
            moveToTeamSpawn(player, team);
            player.getPacketSender().sendGameMessage("The Castle Wars game has begun!");
            updateGameInterface(player, now);
        }

        gameTeamCapacity = Math.max(
                getGamePlayerCountInternal(Team.SARADOMIN),
                getGamePlayerCountInternal(Team.ZAMORAK));
    }

    private static void endGame(long now) {
        int saradominReward = saradominScore > zamorakScore ? 2 : saradominScore == zamorakScore ? 1 : 0;
        int zamorakReward = zamorakScore > saradominScore ? 2 : saradominScore == zamorakScore ? 1 : 0;

        if (saradominScore > zamorakScore) {
            recordTeamScoreboardVictory(Team.SARADOMIN);
        } else if (zamorakScore > saradominScore) {
            recordTeamScoreboardVictory(Team.ZAMORAK);
        }

        returnDroppedFlagToBase(Team.SARADOMIN);
        returnDroppedFlagToBase(Team.ZAMORAK);

        IdentityHashMap<Player, Team> finishers = new IdentityHashMap<Player, Team>(gamePlayers);
        gamePlayers.clear();
        closeAllReplacementOfferInterfaces();
        replacementOffers.clear();
        replacementOfferCooldowns.clear();
        gameTeamCapacity = 0;
        gameInProgress = false;
        gameEndMillis = -1L;

        for (Map.Entry<Player, Team> entry : finishers.entrySet()) {
            Player player = entry.getKey();
            Team team = entry.getValue();
            if (!isOnline(player)) {
                continue;
            }

            recordPlayerScoreboardResult(player, team);
            returnCarriedFlagToBase(player);
            setCastleWarsAttackOption(player, false);
            removeTeamColours(player);
            removeBandages(player);
            CastleWarsEngineeringManager.cleanupPlayerSupplies(player);
            clearCastleWarsInterface(player);
            clearFlagHint(player);
            restorePlayerAfterCastleWars(player);

            int reward = team == Team.SARADOMIN ? saradominReward : zamorakReward;
            if (reward > 0) {
                player.getInventoryManager().addItem(new ItemStack(CASTLE_WARS_TICKET_ID, reward));
            }

            if (saradominScore == zamorakScore) {
                player.getPacketSender().sendGameMessage("The Castle Wars game ended in a draw.");
            } else if ((team == Team.SARADOMIN && saradominScore > zamorakScore)
                    || (team == Team.ZAMORAK && zamorakScore > saradominScore)) {
                player.getPacketSender().sendGameMessage("Your team won the Castle Wars game.");
            } else {
                player.getPacketSender().sendGameMessage("Your team lost the Castle Wars game.");
            }
            if (reward > 0) {
                player.getPacketSender().sendGameMessage("You receive " + reward + " Castle Wars ticket" + (reward == 1 ? "." : "s."));
            }
            moveToLobby(player);
        }

        CastleWarsEngineeringManager.cleanupAfterGame();
        nextGameStartMillis = hasMinimumPlayersToStartInternal()
            ? now + WAITING_DURATION_SECONDS * 1000L
            : -1L;
    }

    /**
     * Castle Wars should not leave combat damage-over-time or reduced health
     * behind when a player returns to the lobby. Stopping the PoisonEffect task
     * is important: clearing only poisonDamage would leave the scheduled poison
     * task alive and it could keep hitting bots after the game has finished.
     */
    private static void restorePlayerAfterCastleWars(Player player) {
        if (player == null) {
            return;
        }

        player.clearCombatEffectTasks(PoisonEffect.class);
        player.setPoisonDamage(0.0);
        player.resetCombatState();
        player.setCurrentHitpoints(player.getMaxHitpoints());
    }

    private static void applyWaitingRoomGodTransformation(Player player, int portalId) {
        int transformationId = -1;

        if (portalId == SARADOMIN_PORTAL_ID) {
            if (hasGodEquipment(player, God.GUTHIX) || hasGodEquipment(player, God.ZAMORAK)) {
                transformationId = SARADOMIN_RABBIT_TRANSFORMATION_ID;
            }
        } else if (portalId == GUTHIX_PORTAL_ID) {
            if (hasGodEquipment(player, God.SARADOMIN) || hasGodEquipment(player, God.ZAMORAK)) {
                transformationId = GUTHIX_SHEEP_TRANSFORMATION_ID;
            }
        } else if (portalId == ZAMORAK_PORTAL_ID) {
            if (hasGodEquipment(player, God.SARADOMIN) || hasGodEquipment(player, God.GUTHIX)) {
                transformationId = ZAMORAK_IMP_TRANSFORMATION_ID;
            }
        }

        if (transformationId > 0) {
            player.npcTransformationId = transformationId;
            // Clear any animation that was already active before the morph.
            player.getUpdateState().setAnimation(-1);
            player.setAppearanceUpdateRequired(true);
        }
    }

    private static boolean hasGodEquipment(Player player, God god) {
        if (player == null) {
            return false;
        }
        // Castle Wars checks what the player is wearing when they enter the
        // portal. Merely carrying a god item in the inventory does not count.
        return containsGodItem(player.getEquipmentManager().getContainer().getItems(), god);
    }

    private static boolean containsGodItem(ItemStack[] items, God god) {
        if (items == null) {
            return false;
        }

        int[] godItems;
        if (god == God.SARADOMIN) {
            godItems = SARADOMIN_TRANSFORMATION_ITEMS;
        } else if (god == God.GUTHIX) {
            godItems = GUTHIX_TRANSFORMATION_ITEMS;
        } else {
            godItems = ZAMORAK_TRANSFORMATION_ITEMS;
        }

        for (ItemStack item : items) {
            if (item == null) {
                continue;
            }
            int itemId = item.getId();
            for (int godItemId : godItems) {
                if (itemId == godItemId) {
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean isWaitingRoomGodTransformation(Player player) {
        if (player == null) {
            return false;
        }
        int transformationId = player.npcTransformationId;
        return transformationId == GUTHIX_SHEEP_TRANSFORMATION_ID
                || transformationId == SARADOMIN_RABBIT_TRANSFORMATION_ID
                || transformationId == ZAMORAK_IMP_TRANSFORMATION_ID;
    }

    private static void clearWaitingRoomGodTransformation(Player player) {
        if (!isWaitingRoomGodTransformation(player)) {
            return;
        }
        player.npcTransformationId = -1;
        player.setAppearanceUpdateRequired(true);
    }

    private static void equipTeamColours(Player player, Team team) {
        int hoodId = team == Team.SARADOMIN ? SARADOMIN_HOOD_ID : ZAMORAK_HOOD_ID;
        int cloakId = team == Team.SARADOMIN ? SARADOMIN_CLOAK_ID : ZAMORAK_CLOAK_ID;
        player.getEquipmentManager().getContainer().setItem(0, new ItemStack(hoodId));
        player.getEquipmentManager().getContainer().setItem(1, new ItemStack(cloakId));
        player.getEquipmentManager().refresh();
        player.setAppearanceUpdateRequired(true);
    }

    private static void setCastleWarsAttackOption(Player player, boolean enabled) {
        if (player == null || player.isBot) {
            return;
        }
        player.getPacketSender().sendPlayerOption(enabled ? "Attack" : "null", 1, false);
    }

    private static void removeTemporaryCastleWarsInventoryItems(Player player) {
        int[] temporaryItemIds = new int[]{
                SARADOMIN_HOOD_ID,
                SARADOMIN_CLOAK_ID,
                ZAMORAK_HOOD_ID,
                ZAMORAK_CLOAK_ID,
                SARADOMIN_FLAG_ID,
                ZAMORAK_FLAG_ID
        };
        for (int itemId : temporaryItemIds) {
            int amount = player.getInventoryManager().getItemAmount(itemId);
            if (amount > 0) {
                player.getInventoryManager().removeItem(new ItemStack(itemId, amount));
            }
        }
    }

    private static void removeBandages(Player player) {
        int amount = player.getInventoryManager().getItemAmount(4049);
        if (amount > 0) {
            player.getInventoryManager().removeItem(new ItemStack(4049, amount));
        }
    }

    private static boolean isWearingTeamColours(Player player, Team team) {
        int expectedHood = team == Team.SARADOMIN ? SARADOMIN_HOOD_ID : ZAMORAK_HOOD_ID;
        int expectedCloak = team == Team.SARADOMIN ? SARADOMIN_CLOAK_ID : ZAMORAK_CLOAK_ID;
        return player.getEquipmentManager().getItemIdAtSlot(0) == expectedHood
                && player.getEquipmentManager().getItemIdAtSlot(1) == expectedCloak;
    }

    private static void removeTeamColours(Player player) {
        ItemStack head = player.getEquipmentManager().getContainer().getItemAt(0);
        ItemStack cape = player.getEquipmentManager().getContainer().getItemAt(1);
        if (head != null && (head.getId() == SARADOMIN_HOOD_ID || head.getId() == ZAMORAK_HOOD_ID)) {
            player.getEquipmentManager().getContainer().setItem(0, null);
        }
        if (cape != null && (cape.getId() == SARADOMIN_CLOAK_ID || cape.getId() == ZAMORAK_CLOAK_ID)) {
            player.getEquipmentManager().getContainer().setItem(1, null);
        }
        player.getEquipmentManager().refresh();
        player.setAppearanceUpdateRequired(true);
    }

    public static void moveBotToUndergroundEntrance(Player player, Team team) {
        if (player == null || team == null || isInGame(player)) {
            return;
        }
        player.getMovementQueue().reset();
        player.resetInteractionState();
        player.resetCombatState();
        if (team == Team.SARADOMIN) {
            player.moveTo(new Position(2430, 3081, 0));
        } else {
            player.moveTo(new Position(2369, 3126, 0));
        }
    }

    public static void moveBotToGroundBattlefield(Player player, Team team) {
        if (player == null || team == null || isInGame(player)) {
            return;
        }
        player.getMovementQueue().reset();
        player.resetInteractionState();
        player.resetCombatState();
        if (team == Team.SARADOMIN) {
            player.moveTo(new Position(2416 + GameUtil.randomInt(3), 3073 + GameUtil.randomInt(3), 0));
        } else {
            player.moveTo(new Position(2381 + GameUtil.randomInt(3), 3132 + GameUtil.randomInt(3), 0));
        }
    }

    private static void moveToTeamSpawn(Player player, Team team) {
        if (team == Team.SARADOMIN) {
            player.moveTo(new Position(2424 + GameUtil.randomInt(5), 3075 + GameUtil.randomInt(4), 1));
        } else {
            player.moveTo(new Position(2370 + GameUtil.randomInt(5), 3128 + GameUtil.randomInt(4), 1));
        }
    }

    public static boolean isAllowedLobbyPortalInventoryItem(Player player, ItemStack item) {
        if (item == null) {
            return true;
        }
        if (item.getDefinition().getEquipmentSlot() >= 0) {
            return true;
        }
        PotionHandler potionHandler = new PotionHandler(player);
        if (potionHandler.selectPotionForItemId(item.getId())) {
            return true;
        }
        String itemName = item.getDefinition().getName();
        return itemName != null && itemName.toLowerCase().endsWith(" rune");
    }

    private static boolean hasRestrictedInventoryItems(Player player) {
        ItemStack[] inventoryItems = player.getInventoryManager().getContainer().getItems();
        for (ItemStack item : inventoryItems) {
            if (!isAllowedLobbyPortalInventoryItem(player, item)) {
                return true;
            }
        }
        return false;
    }

    private static boolean hasRestrictedTeamColourEquipment(Player player) {
        return player.getEquipmentManager().getItemIdAtSlot(0) > 0
                || player.getEquipmentManager().getItemIdAtSlot(1) > 0;
    }

    private static boolean hasMinimumPlayersToStartInternal() {
        return getWaitingPlayerCountInternal(Team.SARADOMIN) >= MINIMUM_PLAYERS_PER_TEAM
                && getWaitingPlayerCountInternal(Team.ZAMORAK) >= MINIMUM_PLAYERS_PER_TEAM;
    }

    private static int getWaitingPlayerCountInternal(Team team) {
        int count = 0;
        for (Team waitingTeam : waitingPlayers.values()) {
            if (waitingTeam == team) {
                ++count;
            }
        }
        return count;
    }

    private static int getGamePlayerCountInternal(Team team) {
        int count = 0;
        for (Team gameTeam : gamePlayers.values()) {
            if (gameTeam == team) {
                ++count;
            }
        }
        return count;
    }

    private static boolean hasReplacementVacancy(Team team) {
        return gameInProgress
                && gameTeamCapacity > 0
                && getGamePlayerCountInternal(team) < gameTeamCapacity;
    }

    private static void processReplacementOffers(long now) {
        Iterator<Map.Entry<Player, Long>> cooldownIterator = replacementOfferCooldowns.entrySet().iterator();
        while (cooldownIterator.hasNext()) {
            Map.Entry<Player, Long> entry = cooldownIterator.next();
            if (!waitingPlayers.containsKey(entry.getKey()) || now >= entry.getValue()) {
                cooldownIterator.remove();
            }
        }

        Iterator<Map.Entry<Player, ReplacementOffer>> offerIterator = replacementOffers.entrySet().iterator();
        while (offerIterator.hasNext()) {
            Map.Entry<Player, ReplacementOffer> entry = offerIterator.next();
            Player player = entry.getKey();
            ReplacementOffer offer = entry.getValue();
            boolean expired = now >= offer.expiresAtMillis;
            boolean valid = isOnline(player)
                    && waitingPlayers.get(player) == offer.team
                    && !expired
                    && hasReplacementVacancy(offer.team);
            if (valid) {
                continue;
            }

            offerIterator.remove();
            if (isOnline(player) && waitingPlayers.get(player) == offer.team) {
                if (expired) {
                    replacementOfferCooldowns.put(
                            player,
                            now + REPLACEMENT_OFFER_DURATION_SECONDS * 1000L);
                }
                closeReplacementOfferInterface(player);
                updateWaitingRoomInterface(player, now);
            }
        }

        offerReplacementPlayers(Team.SARADOMIN, now);
        offerReplacementPlayers(Team.ZAMORAK, now);
    }

    private static void offerReplacementPlayers(Team team, long now) {
        int reserved = 0;
        for (ReplacementOffer offer : replacementOffers.values()) {
            if (offer.team == team) {
                ++reserved;
            }
        }

        int vacancies = gameTeamCapacity - getGamePlayerCountInternal(team) - reserved;
        if (vacancies <= 0) {
            return;
        }

        IdentityHashMap<Player, Team> candidates = new IdentityHashMap<Player, Team>(waitingPlayers);
        for (Map.Entry<Player, Team> entry : candidates.entrySet()) {
            if (vacancies <= 0) {
                break;
            }

            Player player = entry.getKey();
            Long cooldownUntil = replacementOfferCooldowns.get(player);
            if (entry.getValue() != team
                    || replacementOffers.containsKey(player)
                    || (cooldownUntil != null && now < cooldownUntil)
                    || !isOnline(player)) {
                continue;
            }

            if (player.isBot) {
                joinReplacementPlayer(player, team, now);
            } else {
                replacementOffers.put(player, new ReplacementOffer(
                        team,
                        now + REPLACEMENT_OFFER_DURATION_SECONDS * 1000L));
                player.getDialogueManager().showTwoOptionsWithTitle(
                        "A place is available on the " + getTeamName(team) + " team.",
                        "Join the game.",
                        "Stay in the waiting room.");
                player.getPacketSender().sendGameMessage(
                        "A place has become available in the current Castle Wars game.");
            }
            --vacancies;
        }
    }

    private static void joinReplacementPlayer(Player player, Team team, long now) {
        if (waitingPlayers.remove(player) == null) {
            return;
        }

        replacementOffers.remove(player);
        replacementOfferCooldowns.remove(player);
        clearWaitingRoomGodTransformation(player);
        gamePlayers.put(player, team);
        if (!isWearingTeamColours(player, team)) {
            equipTeamColours(player, team);
        }
        setCastleWarsAttackOption(player, true);
        player.resetCombatState();
        moveToTeamSpawn(player, team);
        player.getPacketSender().sendGameMessage(
                "You join the ongoing Castle Wars game for the " + getTeamName(team) + " team.");
        updateGameInterface(player, now);
    }

    private static void cleanupWaitingPlayers() {
        Iterator<Map.Entry<Player, Team>> iterator = waitingPlayers.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<Player, Team> entry = iterator.next();
            Player player = entry.getKey();
            if (!isOnline(player)) {
                iterator.remove();
                continue;
            }
            if (!isInWaitingRoom(player, entry.getValue())) {
                clearWaitingRoomGodTransformation(player);
                iterator.remove();
            }
        }
    }

    private static void cleanupGamePlayers() {
        Iterator<Map.Entry<Player, Team>> iterator = gamePlayers.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<Player, Team> entry = iterator.next();
            if (!isOnline(entry.getKey())) {
                returnCarriedFlagToBase(entry.getKey());
                iterator.remove();
            }
        }
    }

    private static void closeReplacementOfferInterface(Player player) {
        if (player != null
                && (player.isInterfaceIdOpen(2461) || player.isInterfaceIdOpen(2462))) {
            player.getPacketSender().closeInterfaces();
        }
    }

    private static void closeAllReplacementOfferInterfaces() {
        for (Player player : replacementOffers.keySet()) {
            closeReplacementOfferInterface(player);
        }
    }

    private static final class ReplacementOffer {
        private final Team team;
        private final long expiresAtMillis;

        private ReplacementOffer(Team team, long expiresAtMillis) {
            this.team = team;
            this.expiresAtMillis = expiresAtMillis;
        }
    }

    private static boolean isOnline(Player player) {
        if (player == null) {
            return false;
        }
        int index = player.getIndex();
        Player[] players = World.getPlayers();
        return index >= 0 && index < players.length && players[index] == player;
    }

    private static boolean isInWaitingRoom(Player player, Team team) {
        Position position = player.getPosition();
        Position center = team == Team.SARADOMIN ? SARADOMIN_WAITING_ROOM : ZAMORAK_WAITING_ROOM;
        return position.getPlane() == center.getPlane()
                && Math.abs(position.getX() - center.getX()) <= 16
                && Math.abs(position.getY() - center.getY()) <= 16;
    }

    private static boolean isOccupiedWaitingTile(int x, int y, int plane, Player ignoredPlayer) {
        Player[] players = World.getPlayers();
        for (Player player : players) {
            if (player == null || player == ignoredPlayer) {
                continue;
            }
            Position position = player.getPosition();
            if (position.getX() == x && position.getY() == y && position.getPlane() == plane) {
                return true;
            }
        }
        return false;
    }

    private static void updateWaitingRoomInterfaces(long now) {
        for (Map.Entry<Player, Team> entry : waitingPlayers.entrySet()) {
            updateWaitingRoomInterface(entry.getKey(), now);
        }
    }

    private static void updateWaitingRoomInterface(Player player, long now) {
        String timerText;
        if (gameInProgress) {
            long untilNextGame = Math.max(0L, gameEndMillis - now) + WAITING_DURATION_SECONDS * 1000L;
            timerText = "Next game in: " + formatTime(untilNextGame);
        } else if (nextGameStartMillis >= 0L && hasMinimumPlayersToStartInternal()) {
            timerText = "Next game in: " + formatTime(Math.max(0L, nextGameStartMillis - now));
        } else {
            timerText = "Waiting for the other team...";
        }

        player.getPacketSender().showWalkableInterface(WAITING_INTERFACE_ID);
        // The stock 443 waiting overlay displays its countdown from varp 380.
        // Retain the old string updates for 377 clients.
        player.getPacketSender().sendConfig(380, countdownMinutes(
                gameInProgress ? Math.max(0L, gameEndMillis - now)
                        + WAITING_DURATION_SECONDS * 1000L
                        : nextGameStartMillis >= 0L && hasMinimumPlayersToStartInternal()
                        ? Math.max(0L, nextGameStartMillis - now) : 0L));
        player.getPacketSender().sendInterfaceText(timerText, WAITING_TIMER_TEXT_ID);
        player.getPacketSender().sendInterfaceText("", WAITING_ZAMORAK_TEXT_ID);
        player.getPacketSender().sendInterfaceText("", WAITING_SARADOMIN_TEXT_ID);
    }

    private static void updateGameInterfaces(long now) {
        if (!gameInProgress) {
            return;
        }
        for (Player player : gamePlayers.keySet()) {
            updateGameInterface(player, now);
        }
    }

    private static void updateGameInterface(Player player, long now) {
        player.getPacketSender().showWalkableInterface(GAME_INTERFACE_ID);
        player.getPacketSender().sendConfig(380, countdownMinutes(Math.max(0L, gameEndMillis - now)));
        player.getPacketSender().sendInterfaceText("Zamorak = " + zamorakScore, GAME_ZAMORAK_SCORE_TEXT_ID);
        player.getPacketSender().sendInterfaceText(saradominScore + " = Saradomin", GAME_SARADOMIN_SCORE_TEXT_ID);
        player.getPacketSender().sendInterfaceText(formatTime(Math.max(0L, gameEndMillis - now)), GAME_TIMER_TEXT_ID);
        player.getPacketSender().sendInterfaceText(getFlagStatusText(Team.ZAMORAK), GAME_ZAMORAK_FLAG_TEXT_ID);
        player.getPacketSender().sendInterfaceText(getFlagStatusText(Team.SARADOMIN), GAME_SARADOMIN_FLAG_TEXT_ID);
        Team team = gamePlayers.get(player);
        updateEnemyFlagHint(player, team);
        if (team != null) {
            Team enemyTeam = team == Team.SARADOMIN ? Team.ZAMORAK : Team.SARADOMIN;
            int ownConfigId = team == Team.ZAMORAK ? 377 : 378;
            int enemyConfigId = team == Team.ZAMORAK ? 378 : 377;
            player.getPacketSender().sendConfig(ownConfigId, buildCastleWarsInterfaceConfig(team));
            player.getPacketSender().sendConfig(enemyConfigId, buildCastleWarsInterfaceConfig(enemyTeam));

            player.getPacketSender().sendInterfaceText(
                    "Health " + CastleWarsEngineeringManager.getMainDoorHitpoints(team) + "%",
                    GAME_MAIN_GATE_TEXT_ID);
            player.getPacketSender().sendInterfaceText(
                    CastleWarsEngineeringManager.isSideDoorOpen(team) ? "Unlocked" : "Locked",
                    GAME_SIDE_DOOR_TEXT_ID);
            player.getPacketSender().sendInterfaceText(
                    CastleWarsEngineeringManager.isHomeTunnelCollapsed(team, 0) ? "Collapsed" : "Cleared",
                    GAME_TUNNEL_ONE_TEXT_ID);
            player.getPacketSender().sendInterfaceText(
                    CastleWarsEngineeringManager.isHomeTunnelCollapsed(team, 1) ? "Collapsed" : "Cleared",
                    GAME_TUNNEL_TWO_TEXT_ID);
            player.getPacketSender().sendInterfaceText(
                    CastleWarsEngineeringManager.isCatapultOperational(team) ? "Operational" : "Destroyed",
                    GAME_CATAPULT_TEXT_ID);
        }
    }

    private static int buildCastleWarsInterfaceConfig(Team team) {
        int config = Math.max(0, Math.min(100,
                CastleWarsEngineeringManager.getMainDoorHitpoints(team)));

        // Original 377 Castle Wars interface bit layout:
        // 0-6 main door health, 7 side door, 8/9 tunnels, 10 catapult,
        // 21-22 flag state, 24+ score.
        if (CastleWarsEngineeringManager.isSideDoorOpen(team)) {
            config += 128;
        }
        if (!CastleWarsEngineeringManager.isHomeTunnelCollapsed(team, 0)) {
            config += 256;
        }
        if (!CastleWarsEngineeringManager.isHomeTunnelCollapsed(team, 1)) {
            config += 512;
        }
        if (!CastleWarsEngineeringManager.isCatapultOperational(team)) {
            config += 1024;
        }

        int flagState = isFlagAtBase(team) ? 0 : (getDroppedFlag(team) != null ? 2 : 1);
        config += 2097152 * flagState;

        int score = team == Team.SARADOMIN ? saradominScore : zamorakScore;
        config += 16777216 * Math.max(0, Math.min(127, score));
        return config;
    }

    private static void updateEnemyFlagHint(Player player, Team team) {
        if (team == null) {
            clearFlagHint(player);
            return;
        }

        Team enemyFlagTeam = team == Team.SARADOMIN ? Team.ZAMORAK : Team.SARADOMIN;
        if (isFlagAtBase(enemyFlagTeam)) {
            clearFlagHint(player);
            return;
        }

        Position target = getFlagHintPosition(enemyFlagTeam);
        if (target == null) {
            clearFlagHint(player);
            return;
        }

        int targetY = target.getY();
        if (targetY >= 9468 && targetY <= 9540) {
            targetY -= 6400;
        }
        player.getPacketSender().sendPositionHintIcon(target.getX(), targetY, 0, 2);
    }

    private static Position getFlagHintPosition(Team flagTeam) {
        Player holder = getFlagHolder(flagTeam);
        if (holder != null) {
            return holder.getPosition();
        }
        GroundItem droppedFlag = getDroppedFlag(flagTeam);
        return droppedFlag == null ? null : droppedFlag.getPosition();
    }

    private static void clearFlagHint(Player player) {
        player.getPacketSender().sendEntityHintIcon(0, 0);
    }

    private static void clearCastleWarsInterface(Player player) {
        player.getPacketSender().showWalkableInterface(-1);
    }

    private static int countdownMinutes(long millis) {
        return (int) Math.min(255L, Math.max(0L, (millis + 59999L) / 60000L));
    }

    private static String formatTime(long millis) {
        long totalSeconds = (millis + 999L) / 1000L;
        long minutes = totalSeconds / 60L;
        long seconds = totalSeconds % 60L;
        return minutes + ":" + (seconds < 10L ? "0" : "") + seconds;
    }

    private static String getTeamName(Team team) {
        return team == Team.SARADOMIN ? "Saradomin" : "Zamorak";
    }

    private static final class SteppingStoneJumpTask extends TickTask {
        private final Player player;
        private final Position source;
        private final Position destination;
        private int phase;

        private SteppingStoneJumpTask(Player player, Position source, Position destination) {
            super(1, false);
            this.player = player;
            this.source = source;
            this.destination = destination;
        }

        @Override
        public void execute() {
            if (player == null || player.isDead() || !isInGame(player)) {
                finish();
                return;
            }

            if (phase == 0) {
                if (!samePosition(player.getPosition(), source)) {
                    finish();
                    return;
                }
                player.getMovementQueue().clear();
                player.getUpdateState().setFacePosition(destination.copy());
                player.moveTo(destination.copy());
                player.getMovementQueue().clearMovementActions();
                phase = 1;
                return;
            }

            finish();
        }

        private void finish() {
            if (player != null) {
                player.getUpdateState().setAnimation(65535);
                player.setActionLocked(false);
            }
            stop();
        }
    }

    private enum God {
        SARADOMIN,
        GUTHIX,
        ZAMORAK
    }

    public enum Team {
        SARADOMIN,
        ZAMORAK
    }
}
