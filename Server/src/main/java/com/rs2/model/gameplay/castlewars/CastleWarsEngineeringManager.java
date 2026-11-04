package com.rs2.model.gameplay.castlewars;

import com.rs2.ServerSettings;
import com.rs2.cache.InterfaceDefinition;
import com.rs2.model.Position;
import com.rs2.model.World;
import com.rs2.model.combat.AttackBonusType;
import com.rs2.model.combat.AttackStyleDefinition;
import com.rs2.model.combat.CombatManager;
import com.rs2.model.combat.CombatType;
import com.rs2.model.combat.attack.WeaponCombatAttack;
import com.rs2.model.combat.hit.HitType;
import com.rs2.model.ground.GroundItem;
import com.rs2.model.ground.GroundItemManager;
import com.rs2.model.item.ItemStack;
import com.rs2.model.objects.DynamicObject;
import com.rs2.model.objects.LoadedWorldObject;
import com.rs2.model.objects.ObjectDefinition;
import com.rs2.model.objects.ObjectManager;
import com.rs2.model.objects.WorldObjectLookup;
import com.rs2.model.player.Player;
import com.rs2.model.skill.GatheringToolDefinition;
import com.rs2.model.skill.ItemCombinationHandler;
import com.rs2.model.skill.SkillActionHelper;
import com.rs2.model.task.CycleEvent;
import com.rs2.model.task.CycleEventContainer;
import com.rs2.model.task.CycleEventHandler;
import com.rs2.util.GameUtil;
import com.rs2.util.path.ProjectileCollisionMap;
import com.rs2.util.path.WalkingCollisionMap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.Map;

public final class CastleWarsEngineeringManager {
    public static final int ROCK_ITEM_ID = 4043;
    public static final int EXPLOSIVE_POTION_ID = 4045;
    public static final int TOOLKIT_ID = 4051;
    public static final int BARRICADE_ITEM_ID = 4053;
    public static final int CLIMBING_ROPE_ITEM_ID = 4047;
    public static final int BRONZE_PICKAXE_ID = 1265;
    public static final int TINDERBOX_ITEM_ID = 590;
    public static final int EMPTY_BUCKET_ITEM_ID = 1925;
    public static final int BUCKET_OF_WATER_ITEM_ID = 1929;

    // 4421 is the unlit Castle Wars barricade; 4422 plays the burning animation.
    // These are visual states, not Saradomin and Zamorak team-specific objects.
    public static final int BARRICADE_OBJECT_UNLIT = 4421;
    public static final int BARRICADE_OBJECT_BURNING = 4422;
    public static final int COLLAPSED_ROCK_OBJECT_ID = 4437;
    public static final int PARTIAL_ROCK_OBJECT_ID = 4438;
    public static final int CLEARED_ROCK_OBJECT_ID = 4439;
    public static final int CAVE_WALL_OBJECT_ID = 4448;
    public static final int CLIMBING_ROPE_OBJECT_ID = 4444;
    public static final int BATTLEMENT_OBJECT_ID = 4446;
    public static final int BATTLEMENT_OBJECT_ID_ALT = 4447;

    public static final int ZAMORAK_CATAPULT_ID = 4381;
    public static final int SARADOMIN_CATAPULT_ID = 4382;
    public static final int ZAMORAK_BURNING_CATAPULT_ID = 4904;
    public static final int SARADOMIN_BURNING_CATAPULT_ID = 4905;
    public static final int ZAMORAK_DAMAGED_CATAPULT_ID = 4385;
    public static final int SARADOMIN_DAMAGED_CATAPULT_ID = 4386;

    public static final int SARADOMIN_MAIN_DOOR_LEFT_CLOSED_ID = 4423;
    public static final int SARADOMIN_MAIN_DOOR_RIGHT_CLOSED_ID = 4424;
    public static final int SARADOMIN_MAIN_DOOR_LEFT_OPEN_ID = 4425;
    public static final int SARADOMIN_MAIN_DOOR_RIGHT_OPEN_ID = 4426;
    public static final int ZAMORAK_MAIN_DOOR_LEFT_CLOSED_ID = 4427;
    public static final int ZAMORAK_MAIN_DOOR_RIGHT_CLOSED_ID = 4428;
    public static final int ZAMORAK_MAIN_DOOR_LEFT_OPEN_ID = 4429;
    public static final int ZAMORAK_MAIN_DOOR_RIGHT_OPEN_ID = 4430;
    public static final int SARADOMIN_MAIN_DOOR_RIGHT_BROKEN_ID = 4431;
    public static final int SARADOMIN_MAIN_DOOR_LEFT_BROKEN_ID = 4432;
    public static final int ZAMORAK_MAIN_DOOR_LEFT_BROKEN_ID = 4433;
    public static final int ZAMORAK_MAIN_DOOR_RIGHT_BROKEN_ID = 4434;

    public static final int SARADOMIN_SIDE_DOOR_CLOSED_ID = 4465;
    public static final int SARADOMIN_SIDE_DOOR_OPEN_ID = 4466;
    public static final int ZAMORAK_SIDE_DOOR_CLOSED_ID = 4467;
    public static final int ZAMORAK_SIDE_DOOR_OPEN_ID = 4468;

    public static final Position SARADOMIN_CATAPULT = new Position(2413, 3088, 0);
    public static final Position ZAMORAK_CATAPULT = new Position(2384, 3117, 0);

    private static final int CATAPULT_INTERFACE_ID = 11169;
    private static final int CATAPULT_CLOSE_BUTTON_ID = 11259;
    private static final int CATAPULT_AIM_X_TEXT_ID = 11301;
    private static final int CATAPULT_AIM_Y_TEXT_ID = 11302;
    private static final int CATAPULT_AIM_UP_BUTTON_ID = 11321;
    private static final int CATAPULT_AIM_DOWN_BUTTON_ID = 11322;
    private static final int CATAPULT_AIM_RIGHT_BUTTON_ID = 11323;
    private static final int CATAPULT_AIM_LEFT_BUTTON_ID = 11324;
    private static final int CATAPULT_FIRE_BUTTON_ID = 11329;
    private static final int CATAPULT_MAX_AIM = 30;
    // Native 443 group 54: coordinate widgets are type-6 models, not text.
    // Cache digit models 4857..4866 represent 0..9.
    private static final int CATAPULT_443_ZERO_DIGIT_MODEL_ID = 4857;
    private static final int CATAPULT_443_X_TENS_COMPONENT_ID = 19700;
    private static final int CATAPULT_443_X_ONES_COMPONENT_ID = 19701;
    private static final int CATAPULT_443_Y_TENS_COMPONENT_ID = 19702;
    private static final int CATAPULT_443_Y_ONES_COMPONENT_ID = 19703;
    // The stock 443 cache also draws four unconditional models directly on
    // top of the four changing coordinate digits (group 54 children 146..149).
    // Clear them while the native aim interface is open.
    private static final int CATAPULT_443_OVERLAY_X_TENS = 19704;
    private static final int CATAPULT_443_OVERLAY_X_ONES = 19705;
    private static final int CATAPULT_443_OVERLAY_Y_TENS = 19706;
    private static final int CATAPULT_443_OVERLAY_Y_ONES = 19707;
    // Native group 54 child 161 is the red X (model 4856), while children
    // 159/160 are the fixed catapult-site symbols (both model 4853).
    // Only move the red X; moving a catapult-site symbol leaves the X behind.
    private static final int CATAPULT_443_TARGET_CROSS = 19708;
    private static final int CATAPULT_443_MARKER_PIXELS_PER_STEP = 3;
    private static final int CATAPULT_AIM_SCALE = 2;
    private static final int CATAPULT_MISS_RADIUS = 1;

    private static final int MAIN_DOOR_MAX_HITPOINTS = 100;
    public static final int BARRICADE_MAX_HITPOINTS = 50;
    private static final int MAIN_DOOR_PLANE = 0;
    private static final int THIEVING_SKILL_INDEX = 17;
    private static final int SIDE_DOOR_MIN_THIEVING_LEVEL = 1;
    private static final int SIDE_DOOR_ROLL_SIZE = 256;
    private static final int SIDE_DOOR_LEVEL_ONE_THRESHOLD = 16;
    private static final int SIDE_DOOR_LEVEL_NINETY_NINE_THRESHOLD = 256;
    private static final long GAME_TICK_MILLIS = 600L;
    private static final int CLIMBING_ROPE_LIFETIME_TICKS = 100;
    // The Castle Wars rope is attached to the battlement as a wall decoration.
    // The client contains a cache-model fallback for object 4444 so the native
    // rope model still renders if this legacy cache advertises a different type.
    private static final int CLIMBING_ROPE_OBJECT_TYPE = 4;
    private static final int CLIMBING_ROPE_PLANE = 0;
    private static final int CLIMBING_ROPE_DESTINATION_PLANE = 0;
    private static final long BARRICADE_BURN_DURATION_MILLIS = 20L * 1000L;
    private static final long CATAPULT_BURN_DURATION_MILLIS = 20L * 1000L;
    private static final int BUCKET_RESPAWN_DELAY_TICKS = (int) GameUtil.secondsToTicks(30L);

    // Supply pickups belong on the castle's ground floor, never inside the
    // first-floor respawn/barrier room. Zamorak has two bucket pickups.
    // The generic water-source handler fills bucket 1925 into 1929.
    private static final Position[] BUCKET_SPAWN_POSITIONS = new Position[]{
        new Position(2431, 3076, 0), // Saradomin ground floor, south pickup
        new Position(2428, 3079, 0), // Saradomin ground floor, north pickup
        new Position(2371, 3127, 0), // Zamorak ground floor, south pickup
        new Position(2368, 3131, 0)  // Zamorak ground floor, north pickup
    };
    private static final Position[] TINDERBOX_SPAWN_POSITIONS = new Position[]{
        new Position(2431, 3072, 0), // Saradomin ground floor
        new Position(2423, 3080, 1), // Saradomin first floor near the barrier
        new Position(2368, 3135, 0)  // Zamorak ground floor
    };

    private static final Position[] ROCKSLIDE_POSITIONS = new Position[]{
        new Position(2391, 9501, 0),
        new Position(2400, 9512, 0),
        new Position(2409, 9503, 0),
        new Position(2401, 9494, 0)
    };

    private static final Map<String, BarricadeState> barricades = new HashMap<String, BarricadeState>();
    private static final ArrayList<GroundItem> bucketSupplySpawns = new ArrayList<GroundItem>();
    private static final ArrayList<GroundItem> tinderboxSupplySpawns = new ArrayList<GroundItem>();
    private static final Map<String, ClimbingRopeState> climbingRopes =
            new HashMap<String, ClimbingRopeState>();
    private static final boolean[] rockslideCollapsed = new boolean[]{true, true, true, true};
    // Reject mining completions if another player or explosive has changed this rockslide.
    private static final int[] rockslideRevisions = new int[ROCKSLIDE_POSITIONS.length];
    private static final Map<Player, RockslideMiningTask> activeRockslideMining =
            new IdentityHashMap<Player, RockslideMiningTask>();
    // Inventory item IDs do not distinguish a player's items from match-issued copies.
    // Keep a per-player ledger rather than deleting all matching IDs on death or exit.
    private static final Map<Player, Map<Integer, IssuedSupply>> issuedSupplies =
            new IdentityHashMap<Player, Map<Integer, IssuedSupply>>();
    private static boolean saradominCatapultOperational = true;
    private static boolean zamorakCatapultOperational = true;
    private static boolean saradominCatapultBurning;
    private static boolean zamorakCatapultBurning;
    private static long saradominCatapultBurnExpiresAt;
    private static long zamorakCatapultBurnExpiresAt;
    // Catapult reload time is per operator. A team-wide cooldown lets the
    // dedicated catapult bot permanently starve human teammates of the weapon.
    private static final Map<Player, Long> catapultReadyAt =
            new IdentityHashMap<Player, Long>();
    private static final Map<Player, CatapultAim> catapultAims =
            new IdentityHashMap<Player, CatapultAim>();
    private static int catapultAimUpButtonId = CATAPULT_AIM_UP_BUTTON_ID;
    private static int catapultAimDownButtonId = CATAPULT_AIM_DOWN_BUTTON_ID;
    private static int catapultAimLeftButtonId = CATAPULT_AIM_LEFT_BUTTON_ID;
    private static int catapultAimRightButtonId = CATAPULT_AIM_RIGHT_BUTTON_ID;
    private static final Map<Player, Long> mainDoorAttackReadyAt =
            new IdentityHashMap<Player, Long>();
    private static final Map<Player, Long> barricadeAttackReadyAt =
            new IdentityHashMap<Player, Long>();
    private static final MainDoorState saradominMainDoor = new MainDoorState(
            CastleWarsManager.Team.SARADOMIN,
            new MainDoorLeaf(SARADOMIN_MAIN_DOOR_LEFT_CLOSED_ID,
                    SARADOMIN_MAIN_DOOR_LEFT_OPEN_ID, SARADOMIN_MAIN_DOOR_LEFT_BROKEN_ID,
                    2426, 3088, 3, 2426, 3087, 0),
            new MainDoorLeaf(SARADOMIN_MAIN_DOOR_RIGHT_CLOSED_ID,
                    SARADOMIN_MAIN_DOOR_RIGHT_OPEN_ID, SARADOMIN_MAIN_DOOR_RIGHT_BROKEN_ID,
                    2427, 3088, 3, 2427, 3087, 2));
    private static final MainDoorState zamorakMainDoor = new MainDoorState(
            CastleWarsManager.Team.ZAMORAK,
            new MainDoorLeaf(ZAMORAK_MAIN_DOOR_LEFT_CLOSED_ID,
                    ZAMORAK_MAIN_DOOR_LEFT_OPEN_ID, ZAMORAK_MAIN_DOOR_LEFT_BROKEN_ID,
                    2373, 3119, 1, 2373, 3120, 2),
            new MainDoorLeaf(ZAMORAK_MAIN_DOOR_RIGHT_CLOSED_ID,
                    ZAMORAK_MAIN_DOOR_RIGHT_OPEN_ID, ZAMORAK_MAIN_DOOR_RIGHT_BROKEN_ID,
                    2372, 3119, 1, 2372, 3120, 0));
    private static final SideDoorState saradominSideDoor = new SideDoorState(
            CastleWarsManager.Team.SARADOMIN,
            SARADOMIN_SIDE_DOOR_CLOSED_ID, SARADOMIN_SIDE_DOOR_OPEN_ID,
            2415, 3073, 0, 2414, 3073, 1);
    private static final SideDoorState zamorakSideDoor = new SideDoorState(
            CastleWarsManager.Team.ZAMORAK,
            ZAMORAK_SIDE_DOOR_CLOSED_ID, ZAMORAK_SIDE_DOOR_OPEN_ID,
            2384, 3134, 2, 2385, 3134, 3);

    private CastleWarsEngineeringManager() {
    }

    private static boolean isInInteractionRange(Player player, Position target,
                                                int normalMaximumDistance) {
        if (player == null || target == null
                || player.getPosition().getPlane() != target.getPlane()) {
            return false;
        }
        int distance = GameUtil.getDistance(player.getPosition(), target);
        return player.isBot ? distance == 1 : distance <= normalMaximumDistance;
    }

    public static void resetForGame() {
        clearAllBarricades();
        clearAllClimbingRopes();
        cancelAllRockslideMining();
        resetBucketSupplies();
        for (int i = 0; i < ROCKSLIDE_POSITIONS.length; ++i) {
            setRockslideState(i, true);
        }
        setCatapultOperational(CastleWarsManager.Team.SARADOMIN, true);
        setCatapultOperational(CastleWarsManager.Team.ZAMORAK, true);
        resetMainDoors();
        resetSideDoors();
        catapultReadyAt.clear();
        catapultAims.clear();
        mainDoorAttackReadyAt.clear();
        barricadeAttackReadyAt.clear();
    }

    public static void cleanupAfterGame() {
        clearAllBarricades();
        clearAllClimbingRopes();
        cancelAllRockslideMining();
        clearBucketSupplies();
        for (int i = 0; i < ROCKSLIDE_POSITIONS.length; ++i) {
            setRockslideState(i, true);
        }
        setCatapultOperational(CastleWarsManager.Team.SARADOMIN, true);
        setCatapultOperational(CastleWarsManager.Team.ZAMORAK, true);
        resetMainDoors();
        resetSideDoors();
        catapultReadyAt.clear();
        catapultAims.clear();
        mainDoorAttackReadyAt.clear();
        barricadeAttackReadyAt.clear();
    }

    public static boolean handleSupplyTable(Player player, int objectId) {
        if (!CastleWarsManager.isInGame(player)) {
            return false;
        }
        int itemId;
        String message;
        if (objectId == 4459) {
            itemId = TOOLKIT_ID;
            message = "You take a toolkit.";
        } else if (objectId == 4460) {
            itemId = ROCK_ITEM_ID;
            message = "You take a rock for the catapult.";
        } else if (objectId == 4461) {
            itemId = BARRICADE_ITEM_ID;
            message = "You take a barricade.";
        } else if (objectId == 4462) {
            itemId = CLIMBING_ROPE_ITEM_ID;
            message = "You take a climbing rope.";
        } else if (objectId == 4463) {
            itemId = EXPLOSIVE_POTION_ID;
            message = "You take an explosive potion.";
        } else if (objectId == 4464) {
            itemId = BRONZE_PICKAXE_ID;
            message = "You take a bronze pickaxe.";
        } else {
            return false;
        }
        // Only animate a successful grant. issueSupply also handles full inventories.
        if (issueSupply(player, itemId, 1) == 0) {
            return true;
        }
        player.getUpdateState().setAnimation(881);
        player.getPacketSender().sendGameMessage(message);
        return true;
    }

    public static boolean handleItemOnObject(Player player, int itemId, int objectId,
                                             int objectX, int objectY, int objectPlane) {
        if (!CastleWarsManager.isInGame(player)) {
            return false;
        }

        if (itemId == CLIMBING_ROPE_ITEM_ID
                && isCastleWarsBattlementObject(objectId, objectX, objectY, objectPlane)) {
            return attachClimbingRope(player, objectId, objectX, objectY, objectPlane);
        }

        if (objectId == BARRICADE_OBJECT_UNLIT || objectId == BARRICADE_OBJECT_BURNING) {
            Position barricadePosition = new Position(objectX, objectY, objectPlane);
            if (itemId == TINDERBOX_ITEM_ID) {
                return igniteBarricade(player, barricadePosition);
            }
            if (itemId == BUCKET_OF_WATER_ITEM_ID) {
                return extinguishBarricade(player, barricadePosition);
            }
        }

        // Using a catapult rock directly on a working catapult should open
        // the same manual aiming controls as operating it.
        if (itemId == ROCK_ITEM_ID
                && (objectId == SARADOMIN_CATAPULT_ID || objectId == ZAMORAK_CATAPULT_ID)) {
            return operateCatapult(player, objectId);
        }

        if (itemId == TINDERBOX_ITEM_ID
                && (objectId == SARADOMIN_CATAPULT_ID || objectId == ZAMORAK_CATAPULT_ID)) {
            return igniteEnemyCatapult(player, objectId);
        }

        if (itemId == BUCKET_OF_WATER_ITEM_ID
                && (objectId == SARADOMIN_BURNING_CATAPULT_ID
                || objectId == ZAMORAK_BURNING_CATAPULT_ID)) {
            return extinguishOwnCatapult(player, objectId);
        }

        if (itemId == EXPLOSIVE_POTION_ID) {
            if (objectId == BARRICADE_OBJECT_UNLIT || objectId == BARRICADE_OBJECT_BURNING) {
                return destroyBarricadeWithExplosive(player,
                        new Position(objectX, objectY, objectPlane));
            }
            int rockIndex = findRockslideIndexForObject(objectId, objectX, objectY);
            if (rockIndex >= 0 && (objectId == CAVE_WALL_OBJECT_ID
                    || objectId == COLLAPSED_ROCK_OBJECT_ID
                    || objectId == PARTIAL_ROCK_OBJECT_ID
                    || objectId == CLEARED_ROCK_OBJECT_ID)) {
                return useRockslideExplosive(player, rockIndex,
                        objectId == CAVE_WALL_OBJECT_ID);
            }
            if (objectId == SARADOMIN_CATAPULT_ID || objectId == ZAMORAK_CATAPULT_ID
                    || objectId == SARADOMIN_BURNING_CATAPULT_ID
                    || objectId == ZAMORAK_BURNING_CATAPULT_ID) {
                return sabotageEnemyCatapult(player);
            }
        }

        if (isPickaxeItemId(itemId)) {
            int rockIndex = findRockslideIndexForObject(objectId, objectX, objectY);
            if (rockIndex >= 0 && (objectId == CAVE_WALL_OBJECT_ID
                    || objectId == COLLAPSED_ROCK_OBJECT_ID
                    || objectId == PARTIAL_ROCK_OBJECT_ID
                    || objectId == CLEARED_ROCK_OBJECT_ID)) {
                return useRockslidePickaxe(player, rockIndex, itemId,
                        objectId == CAVE_WALL_OBJECT_ID);
            }
        }

        if (itemId == TOOLKIT_ID && isBrokenMainDoorObject(objectId)) {
            return repairMainDoor(player, objectId, objectX, objectY);
        }

        if (itemId == TOOLKIT_ID
                && (objectId == SARADOMIN_DAMAGED_CATAPULT_ID
                || objectId == ZAMORAK_DAMAGED_CATAPULT_ID)) {
            CastleWarsManager.Team team = CastleWarsManager.getGameTeam(player);
            CastleWarsManager.Team targetTeam = objectId == SARADOMIN_DAMAGED_CATAPULT_ID
                    ? CastleWarsManager.Team.SARADOMIN : CastleWarsManager.Team.ZAMORAK;
            Position target = targetTeam == CastleWarsManager.Team.SARADOMIN
                    ? SARADOMIN_CATAPULT : ZAMORAK_CATAPULT;
            if (team == targetTeam && GameUtil.getDistance(player.getPosition(), target) <= 3) {
                setCatapultOperational(targetTeam, true);
                player.getUpdateState().setAnimation(898);
                player.getPacketSender().sendGameMessage("You repair your team's catapult.");
                return true;
            }
        }
        return false;
    }

    public static boolean operateCatapult(Player player, int objectId) {
        CastleWarsManager.Team team = CastleWarsManager.getGameTeam(player);
        if (team == null) {
            return false;
        }
        if ((objectId == SARADOMIN_CATAPULT_ID && team != CastleWarsManager.Team.SARADOMIN)
                || (objectId == ZAMORAK_CATAPULT_ID && team != CastleWarsManager.Team.ZAMORAK)) {
            player.getPacketSender().sendGameMessage("Your team can't use this catapult.");
            return true;
        }
        if (objectId != SARADOMIN_CATAPULT_ID && objectId != ZAMORAK_CATAPULT_ID) {
            return false;
        }
        if (!isCatapultOperational(team) || isCatapultBurning(team)) {
            player.getPacketSender().sendGameMessage("The catapult isn't ready to use.");
            return true;
        }
        if (player.getInventoryManager().getItemAmount(ROCK_ITEM_ID) <= 0) {
            player.getPacketSender().sendGameMessage("You need a rock to fire the catapult.");
            return true;
        }
        Position catapult = getCatapultPosition(team);
        if (player.getPosition().getPlane() != 0
                || GameUtil.getDistance(player.getPosition(), catapult) > 3) {
            return true;
        }

        // Open the same manual aiming state machine on both revisions.
        // PacketSender maps this legacy panel to native group 54 for 443.
        // Ammunition is consumed only by the Fire button handler.
        CatapultAim aim = new CatapultAim(team);
        catapultAims.put(player, aim);
        player.getPacketSender().showInterface(CATAPULT_INTERFACE_ID);
        refreshCatapultAimInterface(player, aim);
        return true;
    }

    public static boolean handleCatapultButton(Player player, int buttonId) {
        // This must be safe to call before the generic button-interface guard:
        // several controls in the native 377 catapult panel are nested below
        // child containers rather than being direct children of interface 11169.
        if (player == null || player.getOpenInterfaceId() != CATAPULT_INTERFACE_ID) {
            return false;
        }

        resolveCatapultAimButtons();

        boolean knownControl = buttonId == CATAPULT_CLOSE_BUTTON_ID
                || buttonId == CATAPULT_FIRE_BUTTON_ID
                || buttonId == catapultAimUpButtonId
                || buttonId == catapultAimDownButtonId
                || buttonId == catapultAimLeftButtonId
                || buttonId == catapultAimRightButtonId;
        if (!knownControl) {
            InterfaceDefinition definition = InterfaceDefinition.forId(buttonId);
            if (definition == null
                    || !isInterfaceDescendantOf(buttonId, CATAPULT_INTERFACE_ID)) {
                return false;
            }

            // Unknown descendants used to be silently consumed here, which meant
            // ::debug could never reveal the native 377 arrow widget IDs.
            if (player.isInteractionDebugEnabled()) {
                int parentId = definition.getParentInterfaceId();
                String message = "Unhandled catapult button: " + buttonId
                        + " (parent=" + parentId + ")";
                player.getPacketSender().sendGameMessage(message);
                System.out.println("[catapult-debug] " + player.getUsername()
                        + " " + message);
            }
            return true;
        }

        if (buttonId == CATAPULT_CLOSE_BUTTON_ID) {
            catapultAims.remove(player);
            player.getPacketSender().closeInterfaces();
            return true;
        }

        CatapultAim aim = catapultAims.get(player);
        CastleWarsManager.Team team = CastleWarsManager.getGameTeam(player);
        if (aim == null || team == null || aim.team != team) {
            player.getPacketSender().closeInterfaces();
            return true;
        }

        Position catapult = getCatapultPosition(team);
        if (player.getPosition().getPlane() != 0
                || GameUtil.getDistance(player.getPosition(), catapult) > 3) {
            catapultAims.remove(player);
            player.getPacketSender().closeInterfaces();
            return true;
        }

        if (buttonId == CATAPULT_FIRE_BUTTON_ID) {
            Position selectedTarget = resolveCatapultAimTarget(team, aim.x, aim.y);
            Position impactTarget = scatterCatapultTarget(selectedTarget);
            if (!fireCatapultAt(player, impactTarget)) {
                if (player.getInventoryManager().getItemAmount(ROCK_ITEM_ID) <= 0) {
                    player.getPacketSender().sendGameMessage("You need a rock to fire the catapult.");
                } else {
                    player.getPacketSender().sendGameMessage("The catapult isn't ready to fire yet.");
                }
            } else {
                player.getPacketSender().sendGameMessage("You fire the catapult!");
            }
            return true;
        }

        boolean changed = false;
        if (buttonId == catapultAimUpButtonId && aim.x < CATAPULT_MAX_AIM) {
            ++aim.x;
            changed = true;
        } else if (buttonId == catapultAimDownButtonId && aim.x > 0) {
            --aim.x;
            changed = true;
        } else if (buttonId == catapultAimLeftButtonId && aim.y > 0) {
            --aim.y;
            changed = true;
        } else if (buttonId == catapultAimRightButtonId && aim.y < CATAPULT_MAX_AIM) {
            ++aim.y;
            changed = true;
        }
        if (changed) {
            refreshCatapultAimInterface(player, aim);
        }
        return true;
    }

    private static void refreshCatapultAimInterface(Player player, CatapultAim aim) {
        if (ServerSettings.clientBuild == 443) {
            // Group 54 draws decorative model widgets 146..149 over the four
            // actual digit models (116,117,132,133). Remove that extra layer:
            // otherwise the changing digits appear as overlapping icons.
            player.getPacketSender().sendInterfaceModelId(CATAPULT_443_OVERLAY_X_TENS, -1);
            player.getPacketSender().sendInterfaceModelId(CATAPULT_443_OVERLAY_X_ONES, -1);
            player.getPacketSender().sendInterfaceModelId(CATAPULT_443_OVERLAY_Y_TENS, -1);
            player.getPacketSender().sendInterfaceModelId(CATAPULT_443_OVERLAY_Y_ONES, -1);
            // The native 443 panel draws separate tens/ones 3D digit models.
            // Legacy sendInterfaceText() does not update these widgets.
            player.getPacketSender().sendInterfaceModelId(
                    CATAPULT_443_X_TENS_COMPONENT_ID, catapultDigitModelId(aim.x / 10));
            player.getPacketSender().sendInterfaceModelId(
                    CATAPULT_443_X_ONES_COMPONENT_ID, catapultDigitModelId(aim.x % 10));
            player.getPacketSender().sendInterfaceModelId(
                    CATAPULT_443_Y_TENS_COMPONENT_ID, catapultDigitModelId(aim.y / 10));
            player.getPacketSender().sendInterfaceModelId(
                    CATAPULT_443_Y_ONES_COMPONENT_ID, catapultDigitModelId(aim.y % 10));
            // The crosshair is a single movable model child (54:161), shared
            // by both teams; the two map-side catapult symbols are fixed.
            // Widget position packets use signed offsets relative to cache XY.
            player.getPacketSender().sendInterfacePosition(
                    CATAPULT_443_TARGET_CROSS,
                    catapultMarkerOffsetX(aim.y), catapultMarkerOffsetY(aim.x));
            return;
        }
        player.getPacketSender().sendInterfaceText(twoDigitAimValue(aim.x), CATAPULT_AIM_X_TEXT_ID);
        player.getPacketSender().sendInterfaceText(twoDigitAimValue(aim.y), CATAPULT_AIM_Y_TEXT_ID);
    }

    private static int catapultMarkerOffsetX(int horizontalAim) {
        return (horizontalAim - CATAPULT_MAX_AIM / 2) * CATAPULT_443_MARKER_PIXELS_PER_STEP;
    }

    private static int catapultMarkerOffsetY(int verticalAim) {
        return (CATAPULT_MAX_AIM / 2 - verticalAim) * CATAPULT_443_MARKER_PIXELS_PER_STEP;
    }

    private static int catapultDigitModelId(int digit) {
        return CATAPULT_443_ZERO_DIGIT_MODEL_ID + digit;
    }

    private static String twoDigitAimValue(int value) {
        return value < 10 ? "0" + value : Integer.toString(value);
    }

    private static boolean isInterfaceDescendantOf(int interfaceId, int parentId) {
        int currentId = interfaceId;
        for (int depth = 0; depth < 16; ++depth) {
            InterfaceDefinition current = InterfaceDefinition.forId(currentId);
            if (current == null) {
                return false;
            }
            int parent = current.getParentInterfaceId();
            if (parent == parentId) {
                return true;
            }
            if (parent < 0 || parent == currentId) {
                return false;
            }
            currentId = parent;
        }
        return false;
    }

    private static void resolveCatapultAimButtons() {
        // Native cache-377 Castle Wars catapult controls.
        // These are fixed IDs; keep the old discovery code below only as a
        // fallback if a compatible client changes them at runtime.
        if (catapultAimUpButtonId == CATAPULT_AIM_UP_BUTTON_ID
                && catapultAimDownButtonId == CATAPULT_AIM_DOWN_BUTTON_ID
                && catapultAimLeftButtonId == CATAPULT_AIM_LEFT_BUTTON_ID
                && catapultAimRightButtonId == CATAPULT_AIM_RIGHT_BUTTON_ID) {
            return;
        }

        ArrayList<InterfaceDefinition> controls = new ArrayList<InterfaceDefinition>();
        for (int interfaceId = 0; interfaceId < InterfaceDefinition.interfaceCount; ++interfaceId) {
            InterfaceDefinition definition = InterfaceDefinition.forId(interfaceId);
            if (definition == null
                    || !isInterfaceDescendantOf(interfaceId, CATAPULT_INTERFACE_ID)
                    || interfaceId == CATAPULT_CLOSE_BUTTON_ID
                    || interfaceId == CATAPULT_FIRE_BUTTON_ID
                    || !isCatapultArrowCandidate(definition)) {
                continue;
            }
            controls.add(definition);
        }
        if (controls.size() < 4) {
            return;
        }

        // The 377 catapult arrows are native sprite widgets, but their cache
        // action type is not reliable. Resolve them by their absolute position
        // inside interface 11169 instead of requiring actionType != 0.
        ArrayList<InterfaceDefinition> verticalCandidates =
                new ArrayList<InterfaceDefinition>();
        ArrayList<InterfaceDefinition> horizontalCandidates =
                new ArrayList<InterfaceDefinition>();

        for (InterfaceDefinition candidate : controls) {
            int x = catapultAbsoluteX(candidate);
            int y = catapultAbsoluteY(candidate);
            if (x == Integer.MIN_VALUE || y == Integer.MIN_VALUE) {
                continue;
            }
            if (x >= 330 && y >= 15 && y <= 145) {
                verticalCandidates.add(candidate);
            }
            if (x >= 230 && y >= 145 && y <= 245) {
                horizontalCandidates.add(candidate);
            }
        }

        InterfaceDefinition[] vertical = selectTwoByAxis(
                verticalCandidates.size() >= 2 ? verticalCandidates : controls, true);
        if (vertical == null) {
            return;
        }

        InterfaceDefinition up = catapultAbsoluteY(vertical[0])
                <= catapultAbsoluteY(vertical[1]) ? vertical[0] : vertical[1];
        InterfaceDefinition down = up == vertical[0] ? vertical[1] : vertical[0];

        ArrayList<InterfaceDefinition> horizontalPool =
                horizontalCandidates.size() >= 2 ? horizontalCandidates : controls;
        ArrayList<InterfaceDefinition> withoutVertical =
                new ArrayList<InterfaceDefinition>();
        for (InterfaceDefinition candidate : horizontalPool) {
            if (candidate != up && candidate != down) {
                withoutVertical.add(candidate);
            }
        }

        InterfaceDefinition[] horizontal = selectTwoByAxis(withoutVertical, false);
        if (horizontal == null) {
            return;
        }

        InterfaceDefinition left = catapultAbsoluteX(horizontal[0])
                <= catapultAbsoluteX(horizontal[1]) ? horizontal[0] : horizontal[1];
        InterfaceDefinition right = left == horizontal[0] ? horizontal[1] : horizontal[0];

        catapultAimUpButtonId = up.getInterfaceId();
        catapultAimDownButtonId = down.getInterfaceId();
        catapultAimLeftButtonId = left.getInterfaceId();
        catapultAimRightButtonId = right.getInterfaceId();
    }

    private static boolean isCatapultArrowCandidate(InterfaceDefinition definition) {
        int type = definition.getWidgetType();
        if (type != 5 && type != 17 && type != 18 && type != 19) {
            return false;
        }
        int width = definition.getWidth();
        int height = definition.getHeight();
        return width > 0 && height > 0 && width <= 64 && height <= 64;
    }

    private static InterfaceDefinition[] selectTwoByAxis(
            ArrayList<InterfaceDefinition> controls, boolean useX) {
        if (controls == null || controls.size() < 2) {
            return null;
        }

        InterfaceDefinition first = null;
        InterfaceDefinition second = null;
        int firstValue = Integer.MIN_VALUE;
        int secondValue = Integer.MIN_VALUE;

        for (InterfaceDefinition candidate : controls) {
            int value = useX ? catapultAbsoluteX(candidate) : catapultAbsoluteY(candidate);
            if (value == Integer.MIN_VALUE) {
                continue;
            }
            if (first == null || value > firstValue) {
                second = first;
                secondValue = firstValue;
                first = candidate;
                firstValue = value;
            } else if (candidate != first && (second == null || value > secondValue)) {
                second = candidate;
                secondValue = value;
            }
        }

        return first == null || second == null
                ? null : new InterfaceDefinition[]{first, second};
    }

    private static int catapultAbsoluteX(InterfaceDefinition definition) {
        return catapultAbsoluteCoordinate(definition, true);
    }

    private static int catapultAbsoluteY(InterfaceDefinition definition) {
        return catapultAbsoluteCoordinate(definition, false);
    }

    private static int catapultAbsoluteCoordinate(
            InterfaceDefinition definition, boolean xAxis) {
        if (definition == null) {
            return Integer.MIN_VALUE;
        }

        int coordinate = 0;
        int currentId = definition.getInterfaceId();
        for (int depth = 0; depth < 16 && currentId != CATAPULT_INTERFACE_ID; ++depth) {
            InterfaceDefinition current = InterfaceDefinition.forId(currentId);
            if (current == null) {
                return Integer.MIN_VALUE;
            }
            int childCoordinate = xAxis
                    ? current.getParentChildX() : current.getParentChildY();
            if (childCoordinate == Integer.MIN_VALUE) {
                return Integer.MIN_VALUE;
            }
            coordinate += childCoordinate;

            int parent = current.getParentInterfaceId();
            if (parent < 0 || parent == currentId) {
                return Integer.MIN_VALUE;
            }
            currentId = parent;
        }
        return currentId == CATAPULT_INTERFACE_ID
                ? coordinate : Integer.MIN_VALUE;
    }

    public static Position getSideDoorInteractionApproach(Player player,
                                                          int objectId,
                                                          int objectX,
                                                          int objectY) {
        if (player == null || player.getPosition().getPlane() != MAIN_DOOR_PLANE) {
            return null;
        }
        SideDoorState door = findSideDoor(objectId, objectX, objectY);
        if (door == null) {
            return null;
        }

        // Each side door is a vertical wall edge between exactly two floor
        // tiles. Keep the player on whichever side of that edge they started.
        int leftX = Math.min(door.closedX, door.openX);
        int rightX = Math.max(door.closedX, door.openX);
        int approachX = player.getPosition().getX() <= leftX ? leftX : rightX;
        return new Position(approachX, door.closedY, MAIN_DOOR_PLANE);
    }

    public static boolean handleSideDoor(Player player, int objectId, int objectX, int objectY) {
        SideDoorState door = findSideDoor(objectId, objectX, objectY);
        if (door == null) {
            return false;
        }

        CastleWarsManager.Team team = CastleWarsManager.getGameTeam(player);
        if (team == null) {
            player.getPacketSender().sendGameMessage("The side doors can only be used during a Castle Wars game.");
            return true;
        }

        if (team == door.team) {
            boolean opening = !door.open;
            player.getMovementQueue().clear();
            player.getMovementQueue().clearMovementActions();
            setSideDoorOpen(door, opening);
            if (!player.isBot) {
                door.playerLocked = !opening;
            }
            player.getPacketSender().sendSoundEffect(318, 1, 0);
            player.getPacketSender().sendGameMessage(door.open
                    ? "You unlock the side door."
                    : "You lock the side door.");
            return true;
        }

        if (door.open) {
            player.getPacketSender().sendGameMessage("Only the defending team can lock this door.");
            return true;
        }

        if (!ServerSettings.thievingEnabled) {
            player.getPacketSender().sendGameMessage("This skill is currently disabled.");
            return true;
        }

        int thievingLevel = player.getSkillManager().getCurrentLevels()[THIEVING_SKILL_INDEX];
        if (thievingLevel < SIDE_DOOR_MIN_THIEVING_LEVEL) {
            player.getPacketSender().sendGameMessage("You need a Thieving level of 1 to pick this lock.");
            return true;
        }

        player.getUpdateState().setAnimation(2246);
        player.getPacketSender().sendGameMessage("You attempt to pick the lock...");
        if (!rollSideDoorUnlock(thievingLevel)) {
            player.getPacketSender().sendGameMessage("You fail to pick the lock.");
            return true;
        }

        door.playerLocked = false;
        player.getMovementQueue().clear();
        player.getMovementQueue().clearMovementActions();
        setSideDoorOpen(door, true);
        player.getPacketSender().sendSoundEffect(1502, 1, 0);
        player.getPacketSender().sendGameMessage("You manage to pick the lock.");
        return true;
    }

    /** Both halves of a large door share one opening. A player alongside either
     * closed leaf can operate the pair without stepping across the doorway.
     * The wall loc sits ON its tile: clipped walking can leave a player at
     * the closed leaf's own X/Y, not just at Y +/- 1. That tile is also a
     * valid melee position; never path through the gate to leave it. */
    public static boolean isAtMainDoorInteractionApproach(Player player,
                                                          int objectId,
                                                          int objectX,
                                                          int objectY) {
        if (player == null || !isClosedMainDoorObject(objectId)
                || player.getPosition().getPlane() != MAIN_DOOR_PLANE) {
            return false;
        }
        MainDoorState door = findMainDoor(objectId, objectX, objectY);
        if (door == null || door.mode != MainDoorMode.CLOSED) {
            return false;
        }
        for (MainDoorLeaf leaf : door.leaves) {
            if (player.getPosition().getX() == leaf.closedX
                    && Math.abs(player.getPosition().getY() - leaf.closedY) <= 1) {
                return true;
            }
        }
        return false;
    }

    public static Position getMainDoorInteractionApproach(Player player,
                                                          int objectId,
                                                          int objectX,
                                                          int objectY) {
        if (player == null || !isClosedMainDoorObject(objectId)) {
            return null;
        }
        MainDoorState door = findMainDoor(objectId, objectX, objectY);
        if (door == null || door.mode != MainDoorMode.CLOSED
                || player.getPosition().getPlane() != MAIN_DOOR_PLANE) {
            return null;
        }

        // Keep the interaction on the side the player is already standing on.
        // The main doors sit east-west, so crossing them is a Y-axis change.
        int approachY;
        if (door.team == CastleWarsManager.Team.SARADOMIN) {
            // A character ON the closed wall tile is still on the exterior
            // approach for an enemy attack. Don't route through its clipping.
            approachY = player.getPosition().getY() < objectY
                    ? objectY - 1 : objectY + 1;
        } else {
            approachY = player.getPosition().getY() > objectY
                    ? objectY + 1 : objectY - 1;
        }
        return new Position(objectX, approachY, MAIN_DOOR_PLANE);
    }

    public static boolean handleMainDoor(Player player, int objectId, int objectX, int objectY) {
        MainDoorState door = findMainDoor(objectId, objectX, objectY);
        if (door == null) {
            return false;
        }
        CastleWarsManager.Team team = CastleWarsManager.getGameTeam(player);
        if (team == null) {
            player.getPacketSender().sendGameMessage("The main doors can only be used during a Castle Wars game.");
            return true;
        }
        if (door.mode == MainDoorMode.BROKEN) {
            if (team != door.team) {
                player.getPacketSender().sendGameMessage("You can only repair your own team's main doors.");
                return true;
            }
            if (player.getInventoryManager().getItemAmount(TOOLKIT_ID) <= 0) {
                player.getPacketSender().sendGameMessage("You need a toolkit to repair the main doors.");
                return true;
            }
            return repairMainDoor(player, objectId, objectX, objectY);
        }
        if (team != door.team) {
            player.getPacketSender().sendGameMessage("You can't open the enemy team's main doors.");
            return true;
        }

        // Do not let an object-interaction path continue through the doorway
        // on the same tick that its clipping is removed.
        player.getMovementQueue().clear();
        player.getMovementQueue().clearMovementActions();
        setMainDoorMode(door, door.mode == MainDoorMode.CLOSED
                ? MainDoorMode.OPEN : MainDoorMode.CLOSED);
        player.getPacketSender().sendSoundEffect(318, 1, 0);
        return true;
    }

    /** Continue only while the targeted closed leaf is still enemy-owned. */
    public static boolean canContinueMainDoorAttack(Player player, int objectId,
                                                     int objectX, int objectY) {
        if (player == null || player.isDead() || player.isStunned()) {
            return false;
        }
        MainDoorState door = findMainDoor(objectId, objectX, objectY);
        CastleWarsManager.Team playerTeam = CastleWarsManager.getGameTeam(player);
        return door != null && door.mode == MainDoorMode.CLOSED
                && isClosedMainDoorObject(objectId)
                && playerTeam != null && playerTeam != door.team;
    }

    /**
     * Crush is most effective against a large door; slash is resisted, and
     * stab more strongly resisted. Exact Jagex multipliers are not published,
     * so these ratios are explicitly tunable approximations.
     */
    public static int calculateMainDoorStyleDamage(int rolledHit, AttackBonusType attackType) {
        if (rolledHit <= 0) {
            return 0;
        }
        if (attackType == AttackBonusType.CRUSH) {
            return (rolledHit * 3 + 1) / 2;
        }
        if (attackType == AttackBonusType.SLASH) {
            return Math.max(1, rolledHit * 3 / 4);
        }
        if (attackType == AttackBonusType.STAB) {
            return Math.max(1, rolledHit / 2);
        }
        return rolledHit;
    }

    public static boolean attackMainDoor(Player player, int objectId, int objectX, int objectY) {
        MainDoorState door = findMainDoor(objectId, objectX, objectY);
        if (door == null || door.mode != MainDoorMode.CLOSED
                || !isClosedMainDoorObject(objectId)) {
            return false;
        }

        CastleWarsManager.Team team = CastleWarsManager.getGameTeam(player);
        if (team == null) {
            return false;
        }
        if (team == door.team) {
            player.getPacketSender().sendGameMessage("You can't attack your own team's main doors.");
            return true;
        }

        // Never carry an object-interaction path through the closed doorway
        // while attacking it.
        player.getMovementQueue().clear();
        player.getMovementQueue().clearMovementActions();

        long now = System.currentTimeMillis();
        Long readyAt = mainDoorAttackReadyAt.get(player);
        if (readyAt != null && now < readyAt.longValue()) {
            return true;
        }

        int fightMode = player.getFightMode();
        AttackStyleDefinition[] styles =
                player.getWeaponProfile().getInterfaceDefinition().getAttackStyles();
        if (fightMode < 0 || fightMode >= styles.length
                || fightMode >= player.getWeaponProfile().getAttackAnimations().length) {
            return true;
        }
        AttackStyleDefinition attackStyle = styles[fightMode];
        if (attackStyle.getCombatType() != CombatType.MELEE) {
            player.getPacketSender().sendGameMessage("You need to use a melee attack to damage the main doors.");
            return true;
        }

        player.getUpdateState().setAnimation(
                player.getWeaponProfile().getAttackAnimations()[fightMode]);
        mainDoorAttackReadyAt.put(player, Long.valueOf(now
                + Math.max(1, player.getWeaponProfile().getAttackDelay()) * GAME_TICK_MILLIS));

        WeaponCombatAttack attack =
                new WeaponCombatAttack(player, player, player.getWeaponProfile());
        int maxHit = Math.max(1,
                (int) CombatManager.calculateMeleeMaxHit(player, attack));
        int damage = calculateMainDoorStyleDamage(
                GameUtil.randomInt(maxHit + 1), attackStyle.getAttackBonusType());
        if (damage <= 0) {
            player.getPacketSender().sendGameMessage("Your attack glances off the main doors.");
            return true;
        }

        door.hitpoints = Math.max(0, door.hitpoints - damage);
        if (door.hitpoints == 0) {
            setMainDoorMode(door, MainDoorMode.BROKEN);
            player.getPacketSender().sendGameMessage("The enemy team's main doors collapse!");
        } else {
            player.getPacketSender().sendGameMessage("You damage the enemy team's main doors.");
        }
        return true;
    }

    public static int getMainDoorHitpoints(CastleWarsManager.Team team) {
        return getMainDoorState(team).hitpoints;
    }

    public static boolean isMainDoorBroken(CastleWarsManager.Team team) {
        return getMainDoorState(team).mode == MainDoorMode.BROKEN;
    }

    /** An open or destroyed door can be traversed without stopping to interact. */
    public static boolean isMainDoorPassable(CastleWarsManager.Team team) {
        return team != null && getMainDoorState(team).mode != MainDoorMode.CLOSED;
    }

    public static boolean isSideDoorOpen(CastleWarsManager.Team team) {
        return (team == CastleWarsManager.Team.SARADOMIN
                ? saradominSideDoor : zamorakSideDoor).open;
    }

    public static boolean tryHandleMainDoorForBot(Player player,
                                                   CastleWarsManager.Team doorTeam) {
        if (player == null || doorTeam == null || !player.isBot
                || !CastleWarsManager.isInGame(player)
                || player.getPosition().getPlane() != MAIN_DOOR_PLANE) {
            return false;
        }

        MainDoorState door = getMainDoorState(doorTeam);
        if (door.mode != MainDoorMode.CLOSED) {
            return false;
        }

        MainDoorLeaf nearestLeaf = null;
        int nearestDistance = Integer.MAX_VALUE;
        for (MainDoorLeaf leaf : door.leaves) {
            int distance = GameUtil.getDistance(player.getPosition(),
                    new Position(leaf.closedX, leaf.closedY, MAIN_DOOR_PLANE));
            if (distance < nearestDistance) {
                nearestDistance = distance;
                nearestLeaf = leaf;
            }
        }
        if (nearestLeaf == null || nearestDistance != 1) {
            return false;
        }

        CastleWarsManager.Team playerTeam = CastleWarsManager.getGameTeam(player);
        if (playerTeam == door.team) {
            handleMainDoor(player, nearestLeaf.closedId,
                    nearestLeaf.closedX, nearestLeaf.closedY);
            return true;
        }

        // Enemy main doors are destroyable with melee. Ranged/magic runners
        // hold the doorway while a melee teammate breaks it, then everybody
        // paths through the same double-door opening.
        if (player.botPrimaryCombatStyle == 0 || player.botActiveCombatStyle == 0) {
            return attackMainDoor(player, nearestLeaf.closedId,
                    nearestLeaf.closedX, nearestLeaf.closedY);
        }
        return true;
    }

    public static boolean tryHandleNearbyDoorForBot(Player player) {
        if (player == null || !player.isBot || !CastleWarsManager.isInGame(player)
                || player.getPosition().getPlane() != MAIN_DOOR_PLANE) {
            return false;
        }

        CastleWarsManager.Team playerTeam = CastleWarsManager.getGameTeam(player);
        if (playerTeam == null) {
            return false;
        }

        SideDoorState[] sideDoors = new SideDoorState[]{saradominSideDoor, zamorakSideDoor};
        for (SideDoorState door : sideDoors) {
            if (door.open) {
                continue;
            }
            if (playerTeam == door.team && door.playerLocked) {
                continue;
            }
            // When the side door is closed, bots must interact with the
            // actual closed-door tile. The open-door display tile is the exterior
            // standing square, so including it in the distance calculation makes
            // a bot standing exactly outside the door appear distance 0 and it
            // never attempts to open/pick the lock.
            int distance = GameUtil.getDistance(player.getPosition(),
                    new Position(door.closedX, door.closedY, MAIN_DOOR_PLANE));
            if (distance != 1) {
                continue;
            }
            if (playerTeam != door.team && !ServerSettings.thievingEnabled) {
                continue;
            }
            handleSideDoor(player, door.closedId, door.closedX, door.closedY);
            return true;
        }

        MainDoorState[] mainDoors = new MainDoorState[]{saradominMainDoor, zamorakMainDoor};
        for (MainDoorState door : mainDoors) {
            if (door.mode != MainDoorMode.CLOSED) {
                continue;
            }

            MainDoorLeaf nearestLeaf = null;
            int nearestDistance = Integer.MAX_VALUE;
            for (MainDoorLeaf leaf : door.leaves) {
                int distance = GameUtil.getDistance(player.getPosition(),
                        new Position(leaf.closedX, leaf.closedY, MAIN_DOOR_PLANE));
                if (distance < nearestDistance) {
                    nearestDistance = distance;
                    nearestLeaf = leaf;
                }
            }
            if (nearestLeaf == null || nearestDistance != 1) {
                continue;
            }

            if (playerTeam == door.team) {
                handleMainDoor(player, nearestLeaf.closedId,
                        nearestLeaf.closedX, nearestLeaf.closedY);
                return true;
            }
            if (player.botPrimaryCombatStyle == 0) {
                return attackMainDoor(player, nearestLeaf.closedId,
                        nearestLeaf.closedX, nearestLeaf.closedY);
            }
        }
        return false;
    }

    private static boolean repairMainDoor(Player player, int objectId, int objectX, int objectY) {
        MainDoorState door = findMainDoor(objectId, objectX, objectY);
        if (door == null || door.mode != MainDoorMode.BROKEN) {
            return false;
        }
        CastleWarsManager.Team team = CastleWarsManager.getGameTeam(player);
        if (team != door.team) {
            player.getPacketSender().sendGameMessage("You can only repair your own team's main doors.");
            return true;
        }
        if (player.getInventoryManager().getItemAmount(TOOLKIT_ID) <= 0) {
            return false;
        }

        player.getUpdateState().setAnimation(898);
        door.hitpoints = MAIN_DOOR_MAX_HITPOINTS;
        setMainDoorMode(door, MainDoorMode.CLOSED);
        player.getPacketSender().sendGameMessage("You repair your team's main doors.");
        return true;
    }

    private static void resetMainDoors() {
        saradominMainDoor.hitpoints = MAIN_DOOR_MAX_HITPOINTS;
        zamorakMainDoor.hitpoints = MAIN_DOOR_MAX_HITPOINTS;
        setMainDoorMode(saradominMainDoor, MainDoorMode.CLOSED);
        setMainDoorMode(zamorakMainDoor, MainDoorMode.CLOSED);
    }

    private static void resetSideDoors() {
        saradominSideDoor.playerLocked = false;
        zamorakSideDoor.playerLocked = false;
        setSideDoorOpen(saradominSideDoor, false);
        setSideDoorOpen(zamorakSideDoor, false);
    }

    private static SideDoorState findSideDoor(int objectId, int objectX, int objectY) {
        if (saradominSideDoor.matches(objectId, objectX, objectY)) {
            return saradominSideDoor;
        }
        if (zamorakSideDoor.matches(objectId, objectX, objectY)) {
            return zamorakSideDoor;
        }
        return null;
    }

    private static boolean rollSideDoorUnlock(int thievingLevel) {
        int level = Math.max(1, Math.min(99, thievingLevel));
        int range = SIDE_DOOR_LEVEL_NINETY_NINE_THRESHOLD - SIDE_DOOR_LEVEL_ONE_THRESHOLD;
        int threshold = SIDE_DOOR_LEVEL_ONE_THRESHOLD
                + (int) Math.round((level - 1) * (double) range / 98.0);
        return GameUtil.randomInt(SIDE_DOOR_ROLL_SIZE) < threshold;
    }

    private static void setSideDoorOpen(SideDoorState door, boolean open) {
        removeSideDoorDynamicObject(door.closedX, door.closedY);
        removeSideDoorDynamicObject(door.openX, door.openY);

        if (open) {
            new DynamicObject(ServerSettings.placeholderObjectId,
                    door.closedX, door.closedY, MAIN_DOOR_PLANE,
                    door.closedOrientation, 0, door.closedId, 999999999);
            new DynamicObject(door.openId, door.openX, door.openY, MAIN_DOOR_PLANE,
                    door.openOrientation, 0, door.openId, 999999999);
        } else {
            new DynamicObject(door.closedId, door.closedX, door.closedY, MAIN_DOOR_PLANE,
                    door.closedOrientation, 0, door.closedId, 999999999);
            new DynamicObject(ServerSettings.placeholderObjectId,
                    door.openX, door.openY, MAIN_DOOR_PLANE,
                    door.openOrientation, 0, door.openId, 999999999);
        }

        // DynamicObject's legacy collision-removal helper swaps loc type and
        // orientation. That is invisible for orientation 0, but leaves the
        // orientation-2/3 Castle Wars side doors blocking the passage after
        // their models have opened. Normalize both wall edges explicitly.
        applySideDoorCollisionState(door, open);
        door.open = open;
    }

    private static void applySideDoorCollisionState(SideDoorState door, boolean open) {
        WalkingCollisionMap.removeObjectCollision(
                door.closedId, door.closedX, door.closedY, MAIN_DOOR_PLANE,
                door.closedOrientation, 0);
        ProjectileCollisionMap.removeObjectCollision(
                door.closedId, door.closedX, door.closedY, MAIN_DOOR_PLANE,
                door.closedOrientation, 0);
        WalkingCollisionMap.removeObjectCollision(
                door.openId, door.openX, door.openY, MAIN_DOOR_PLANE,
                door.openOrientation, 0);
        ProjectileCollisionMap.removeObjectCollision(
                door.openId, door.openX, door.openY, MAIN_DOOR_PLANE,
                door.openOrientation, 0);

        if (open) {
            WalkingCollisionMap.addObjectCollision(
                    door.openId, door.openX, door.openY, MAIN_DOOR_PLANE,
                    door.openOrientation, 0, false);
            ProjectileCollisionMap.addObjectCollision(
                    door.openId, door.openX, door.openY, MAIN_DOOR_PLANE,
                    door.openOrientation, 0, false);
        } else {
            WalkingCollisionMap.addObjectCollision(
                    door.closedId, door.closedX, door.closedY, MAIN_DOOR_PLANE,
                    door.closedOrientation, 0, false);
            ProjectileCollisionMap.addObjectCollision(
                    door.closedId, door.closedX, door.closedY, MAIN_DOOR_PLANE,
                    door.closedOrientation, 0, false);
        }
    }

    private static void removeSideDoorDynamicObject(int x, int y) {
        if (ObjectManager.findDynamicObjectAt(x, y, MAIN_DOOR_PLANE) != null) {
            ObjectManager.getInstance().removeDynamicObjectAt(x, y, MAIN_DOOR_PLANE, 0);
        }
    }

    private static MainDoorState getMainDoorState(CastleWarsManager.Team team) {
        return team == CastleWarsManager.Team.SARADOMIN
                ? saradominMainDoor : zamorakMainDoor;
    }

    private static MainDoorState findMainDoor(int objectId, int objectX, int objectY) {
        if (saradominMainDoor.matches(objectId, objectX, objectY)) {
            return saradominMainDoor;
        }
        if (zamorakMainDoor.matches(objectId, objectX, objectY)) {
            return zamorakMainDoor;
        }
        return null;
    }

    /** All large Castle Wars gate models, including open and destroyed leaves. */
    public static boolean isMainDoorObject(int objectId) {
        return objectId >= SARADOMIN_MAIN_DOOR_LEFT_CLOSED_ID
                && objectId <= ZAMORAK_MAIN_DOOR_RIGHT_BROKEN_ID;
    }

    /**
     * A client can keep a closed static gate model after the server opened it.
     * Re-send both leaves if an option targets a model that is no longer live,
     * rather than silently waiting for an object that no longer exists.
     */
    public static boolean resyncStaleMainDoorClick(Player player, int objectId,
                                                    int objectX, int objectY) {
        if (player == null || !isMainDoorObject(objectId)) {
            return false;
        }
        MainDoorState door = findMainDoor(objectId, objectX, objectY);
        if (door == null) {
            return false;
        }
        for (MainDoorLeaf leaf : door.leaves) {
            if (!leaf.matches(objectId, objectX, objectY)) {
                continue;
            }
            boolean live = door.mode == MainDoorMode.CLOSED && objectId == leaf.closedId
                    || door.mode == MainDoorMode.OPEN && objectId == leaf.openId
                    || door.mode == MainDoorMode.BROKEN && objectId == leaf.brokenId;
            if (live) {
                return false;
            }
            sendMainDoorStateToPlayer(player, door);
            return true;
        }
        return false;
    }

    private static void sendMainDoorStateToPlayer(Player player, MainDoorState door) {
        for (MainDoorLeaf leaf : door.leaves) {
            if (door.mode == MainDoorMode.CLOSED) {
                player.getPacketSender().sendObjectCreate(leaf.closedId, leaf.closedX,
                        leaf.closedY, MAIN_DOOR_PLANE, leaf.closedOrientation, 0);
                player.getPacketSender().sendObjectCreate(ServerSettings.placeholderObjectId,
                        leaf.openX, leaf.openY, MAIN_DOOR_PLANE, leaf.openOrientation, 0);
            } else {
                player.getPacketSender().sendObjectCreate(ServerSettings.placeholderObjectId,
                        leaf.closedX, leaf.closedY, MAIN_DOOR_PLANE, leaf.closedOrientation, 0);
                int visibleId = door.mode == MainDoorMode.OPEN ? leaf.openId : leaf.brokenId;
                player.getPacketSender().sendObjectCreate(visibleId, leaf.openX,
                        leaf.openY, MAIN_DOOR_PLANE, leaf.openOrientation, 0);
            }
        }
    }

    public static boolean isClosedMainDoorObject(int objectId) {
        return objectId == SARADOMIN_MAIN_DOOR_LEFT_CLOSED_ID
                || objectId == SARADOMIN_MAIN_DOOR_RIGHT_CLOSED_ID
                || objectId == ZAMORAK_MAIN_DOOR_LEFT_CLOSED_ID
                || objectId == ZAMORAK_MAIN_DOOR_RIGHT_CLOSED_ID;
    }

    private static boolean isBrokenMainDoorObject(int objectId) {
        return objectId == SARADOMIN_MAIN_DOOR_LEFT_BROKEN_ID
                || objectId == SARADOMIN_MAIN_DOOR_RIGHT_BROKEN_ID
                || objectId == ZAMORAK_MAIN_DOOR_LEFT_BROKEN_ID
                || objectId == ZAMORAK_MAIN_DOOR_RIGHT_BROKEN_ID;
    }

    private static void setMainDoorMode(MainDoorState door, MainDoorMode mode) {
        for (MainDoorLeaf leaf : door.leaves) {
            removeMainDoorDynamicObject(leaf.closedX, leaf.closedY);
            removeMainDoorDynamicObject(leaf.openX, leaf.openY);
        }

        for (MainDoorLeaf leaf : door.leaves) {
            if (mode == MainDoorMode.CLOSED) {
                new DynamicObject(leaf.closedId, leaf.closedX, leaf.closedY, MAIN_DOOR_PLANE,
                        leaf.closedOrientation, 0, leaf.closedId, 999999999);
                new DynamicObject(ServerSettings.placeholderObjectId,
                        leaf.openX, leaf.openY, MAIN_DOOR_PLANE,
                        leaf.openOrientation, 0, ServerSettings.placeholderObjectId, 999999999);
            } else {
                new DynamicObject(ServerSettings.placeholderObjectId,
                        leaf.closedX, leaf.closedY, MAIN_DOOR_PLANE,
                        leaf.closedOrientation, 0, leaf.closedId, 999999999);
                int displayId = mode == MainDoorMode.OPEN ? leaf.openId : leaf.brokenId;
                new DynamicObject(displayId, leaf.openX, leaf.openY, MAIN_DOOR_PLANE,
                        leaf.openOrientation, 0, ServerSettings.placeholderObjectId, 999999999);
            }
        }

        // DynamicObject's legacy removal path is not reliable for rotated wall
        // objects. Normalize both leaves explicitly so opening/breaking the large
        // Castle Wars gates also clears the original closed-door clipping.
        applyMainDoorCollisionState(door, mode);
        door.mode = mode;
    }

    private static void applyMainDoorCollisionState(MainDoorState door, MainDoorMode mode) {
        for (MainDoorLeaf leaf : door.leaves) {
            WalkingCollisionMap.removeObjectCollision(
                    leaf.closedId, leaf.closedX, leaf.closedY, MAIN_DOOR_PLANE,
                    leaf.closedOrientation, 0);
            ProjectileCollisionMap.removeObjectCollision(
                    leaf.closedId, leaf.closedX, leaf.closedY, MAIN_DOOR_PLANE,
                    leaf.closedOrientation, 0);

            WalkingCollisionMap.removeObjectCollision(
                    leaf.openId, leaf.openX, leaf.openY, MAIN_DOOR_PLANE,
                    leaf.openOrientation, 0);
            ProjectileCollisionMap.removeObjectCollision(
                    leaf.openId, leaf.openX, leaf.openY, MAIN_DOOR_PLANE,
                    leaf.openOrientation, 0);

            WalkingCollisionMap.removeObjectCollision(
                    leaf.brokenId, leaf.openX, leaf.openY, MAIN_DOOR_PLANE,
                    leaf.openOrientation, 0);
            ProjectileCollisionMap.removeObjectCollision(
                    leaf.brokenId, leaf.openX, leaf.openY, MAIN_DOOR_PLANE,
                    leaf.openOrientation, 0);

            if (mode == MainDoorMode.CLOSED) {
                WalkingCollisionMap.addObjectCollision(
                        leaf.closedId, leaf.closedX, leaf.closedY, MAIN_DOOR_PLANE,
                        leaf.closedOrientation, 0, false);
                ProjectileCollisionMap.addObjectCollision(
                        leaf.closedId, leaf.closedX, leaf.closedY, MAIN_DOOR_PLANE,
                        leaf.closedOrientation, 0, false);
            } else if (mode == MainDoorMode.OPEN) {
                WalkingCollisionMap.addObjectCollision(
                        leaf.openId, leaf.openX, leaf.openY, MAIN_DOOR_PLANE,
                        leaf.openOrientation, 0, false);
                ProjectileCollisionMap.addObjectCollision(
                        leaf.openId, leaf.openX, leaf.openY, MAIN_DOOR_PLANE,
                        leaf.openOrientation, 0, false);
            }
            // Door definitions in some 443 caches are not solid. Enforce the
            // two physical gate edges independently of the cache definition, so
            // bots cannot path through the closed model and players are not
            // blocked by an opened or destroyed gate.
            WalkingCollisionMap.setCastleWarsGateEdge(leaf.closedX, leaf.closedY,
                    MAIN_DOOR_PLANE, leaf.closedOrientation, mode == MainDoorMode.CLOSED);
            ProjectileCollisionMap.setCastleWarsGateEdge(leaf.closedX, leaf.closedY,
                    MAIN_DOOR_PLANE, leaf.closedOrientation, mode == MainDoorMode.CLOSED);
            // Broken gates intentionally add no wall collision so the opening is
            // fully passable even if the broken-door model itself has clipping.
        }
    }

    private static void removeMainDoorDynamicObject(int x, int y) {
        if (ObjectManager.findDynamicObjectAt(x, y, MAIN_DOOR_PLANE) != null) {
            ObjectManager.getInstance().removeDynamicObjectAt(x, y, MAIN_DOOR_PLANE, 0);
        }
    }

    private static void resetBucketSupplies() {
        clearBucketSupplies();
        for (Position position : BUCKET_SPAWN_POSITIONS) {
            spawnRespawningSupply(EMPTY_BUCKET_ITEM_ID, position, bucketSupplySpawns);
        }
        for (Position position : TINDERBOX_SPAWN_POSITIONS) {
            spawnRespawningSupply(TINDERBOX_ITEM_ID, position, tinderboxSupplySpawns);
        }
    }

    private static void spawnRespawningSupply(int itemId, Position position,
                                              ArrayList<GroundItem> trackedSpawns) {
        GroundItem groundItem = new GroundItem(
                new ItemStack(itemId, 1), position, BUCKET_RESPAWN_DELAY_TICKS, true);
        trackedSpawns.add(groundItem);
        GroundItemManager.getInstance().spawn(groundItem);
    }

    private static void clearBucketSupplies() {
        GroundItemManager manager = GroundItemManager.getInstance();
        for (GroundItem groundItem : bucketSupplySpawns) {
            if (manager.contains(groundItem)) {
                manager.remove(groundItem);
            }
        }
        bucketSupplySpawns.clear();
        for (GroundItem groundItem : tinderboxSupplySpawns) {
            if (manager.contains(groundItem)) {
                manager.remove(groundItem);
            }
        }
        tinderboxSupplySpawns.clear();
    }

    private static boolean isSupplyItem(int itemId) {
        return itemId == ROCK_ITEM_ID || itemId == EXPLOSIVE_POTION_ID
                || itemId == BARRICADE_ITEM_ID || itemId == CLIMBING_ROPE_ITEM_ID
                || itemId == BRONZE_PICKAXE_ID || itemId == TOOLKIT_ID
                || itemId == TINDERBOX_ITEM_ID || itemId == EMPTY_BUCKET_ITEM_ID
                || itemId == BUCKET_OF_WATER_ITEM_ID;
    }

    private static final class IssuedSupply {
        private int ownedAmount;
        private int outstandingAmount;

        private IssuedSupply(int ownedAmount, int outstandingAmount) {
            this.ownedAmount = ownedAmount;
            this.outstandingAmount = outstandingAmount;
        }
    }

    /**
     * Grant supplies and record only the amount actually placed in this player's
     * inventory. Kept separate from giveSupply's in-game guard for ownership tests.
     */
    public static int issueSupply(Player player, int itemId, int amount) {
        if (player == null || amount <= 0 || !isSupplyItem(itemId)) {
            return 0;
        }

        int before = player.getInventoryManager().getItemAmount(itemId);
        player.getInventoryManager().addItemPartial(new ItemStack(itemId, amount));
        int granted = Math.max(0, player.getInventoryManager().getItemAmount(itemId) - before);
        if (granted == 0) {
            return 0;
        }

        Map<Integer, IssuedSupply> supplies = issuedSupplies.get(player);
        if (supplies == null) {
            supplies = new HashMap<Integer, IssuedSupply>();
            issuedSupplies.put(player, supplies);
        }
        IssuedSupply supply = supplies.get(Integer.valueOf(itemId));
        if (supply == null) {
            supplies.put(Integer.valueOf(itemId), new IssuedSupply(before, granted));
        } else {
            // A consumed issued item must not remain in the removal budget.
            // Keep the originally owned quantity (or less if it was depleted).
            supply.ownedAmount = Math.min(supply.ownedAmount, before);
            supply.outstandingAmount = Math.min(supply.outstandingAmount,
                    Math.max(0, before - supply.ownedAmount)) + granted;
        }
        return granted;
    }

    public static int giveSupply(Player player, int itemId, int amount) {
        if (!CastleWarsManager.isInGame(player) || amount <= 0) {
            return 0;
        }
        if (!isGameSupplyItemId(itemId)) {
            return 0;
        }
        return issueSupply(player, itemId, amount);
    }

    /** Tinderboxes are issued to attackers but remain reusable after ignition. */
    static boolean isGameSupplyItemId(int itemId) {
        return itemId == ROCK_ITEM_ID || itemId == EXPLOSIVE_POTION_ID
                || itemId == BARRICADE_ITEM_ID || itemId == CLIMBING_ROPE_ITEM_ID
                || itemId == BRONZE_PICKAXE_ID || itemId == TOOLKIT_ID
                || itemId == TINDERBOX_ITEM_ID;
    }

    public static void cleanupPlayerSupplies(Player player) {
        if (player == null) {
            return;
        }
        barricadeAttackReadyAt.remove(player);
        Map<Integer, IssuedSupply> supplies = issuedSupplies.remove(player);
        if (supplies == null) {
            return;
        }
        for (Map.Entry<Integer, IssuedSupply> entry : supplies.entrySet()) {
            int id = entry.getKey().intValue();
            IssuedSupply supply = entry.getValue();
            int inInventory = player.getInventoryManager().getItemAmount(id);
            // Identical item IDs are fungible; reserve the pre-issuance owned
            // quantity and remove no more than the outstanding match supply.
            int removable = Math.min(supply.outstandingAmount,
                    Math.max(0, inInventory - supply.ownedAmount));
            if (removable > 0) {
                player.getInventoryManager().removeItem(new ItemStack(id, removable));
            }
        }
    }

    public static boolean handleClimbingRope(Player player, int objectId, int objectX, int objectY) {
        if (objectId != CLIMBING_ROPE_OBJECT_ID
                && !isCastleWarsBattlementObject(objectId, objectX, objectY,
                player == null ? CLIMBING_ROPE_PLANE : player.getPosition().getPlane())) {
            return false;
        }
        if (!CastleWarsManager.isInGame(player)) {
            return false;
        }
        if (player.getPosition().getPlane() != CLIMBING_ROPE_PLANE) {
            player.getPacketSender().sendGameMessage("The rope can only be climbed from below.");
            return true;
        }

        ClimbingRopeState rope = getActiveClimbingRope(objectX, objectY);
        if (rope == null) {
            return false;
        }
        if (!isInInteractionRange(player, rope.position, 2)) {
            return false;
        }

        player.getMovementQueue().clear();
        player.getUpdateState().setAnimation(828);
        player.moveTo(rope.destination.copy());
        player.getMovementQueue().clearMovementActions();
        if (!player.isBot) {
            player.getPacketSender().resetCamera();
        }
        player.getPacketSender().sendGameMessage("You climb the rope onto the battlements.");
        return true;
    }

    private static boolean isCastleWarsBattlementObject(int objectId,
                                                          int objectX,
                                                          int objectY,
                                                          int objectPlane) {
        if (objectPlane != CLIMBING_ROPE_PLANE
                || getBattlementTeam(objectX, objectY) == null) {
            return false;
        }
        if (objectId == BATTLEMENT_OBJECT_ID || objectId == BATTLEMENT_OBJECT_ID_ALT) {
            return true;
        }

        ObjectDefinition definition = ObjectDefinition.forId(objectId);
        return definition != null
                && definition.name != null
                && "battlements".equalsIgnoreCase(definition.name.trim());
    }

    private static boolean isCastleWarsBattlementAt(int x, int y, int plane) {
        return getCastleWarsBattlementObjectId(x, y, plane) >= 0;
    }

    private static int getCastleWarsBattlementObjectId(int x, int y, int plane) {
        if (plane != CLIMBING_ROPE_PLANE || getBattlementTeam(x, y) == null) {
            return -1;
        }
        if (SkillActionHelper.isObjectPresent(BATTLEMENT_OBJECT_ID, x, y, plane)) {
            return BATTLEMENT_OBJECT_ID;
        }
        if (SkillActionHelper.isObjectPresent(BATTLEMENT_OBJECT_ID_ALT, x, y, plane)) {
            return BATTLEMENT_OBJECT_ID_ALT;
        }

        // Castle Wars maps can contain cache variants of the same battlement
        // model under different object IDs. Resolve the object by its cache name
        // directly from the region bucket instead of scanning every object ID.
        LoadedWorldObject battlement =
                WorldObjectLookup.findObjectByNameAt("battlements", x, y, plane);
        if (battlement != null) {
            return battlement.getWorldObject().getObjectId();
        }
        return -1;
    }

    private static boolean attachClimbingRope(Player player, int battlementId,
                                              int objectX, int objectY, int objectPlane) {
        if (objectPlane != CLIMBING_ROPE_PLANE) {
            return false;
        }
        CastleWarsManager.Team targetTeam = getBattlementTeam(objectX, objectY);
        if (targetTeam == null) {
            return false;
        }

        CastleWarsManager.Team playerTeam = CastleWarsManager.getGameTeam(player);
        if (playerTeam == null) {
            return false;
        }
        if (playerTeam == targetTeam) {
            player.getPacketSender().sendGameMessage("You can only attach climbing ropes to the enemy castle.");
            return true;
        }
        if (player.getInventoryManager().getItemAmount(CLIMBING_ROPE_ITEM_ID) <= 0) {
            return true;
        }
        if (!isInInteractionRange(player,
                new Position(objectX, objectY, objectPlane), 2)) {
            return false;
        }

        if (getActiveClimbingRope(objectX, objectY) != null) {
            player.getPacketSender().sendGameMessage("A climbing rope is already attached here.");
            return true;
        }

        Position destination = findClimbingRopeDestination(objectX, objectY);
        if (destination == null) {
            player.getPacketSender().sendGameMessage("You cannot attach a rope to this part of the wall.");
            return true;
        }

        Position battlementPosition = new Position(objectX, objectY, objectPlane);
        int battlementOrientation = SkillActionHelper.getObjectOrientation(
                battlementId, objectX, objectY, objectPlane);
        Position ropeObjectPosition = getClimbingRopeExteriorPosition(
                battlementPosition, battlementOrientation);
        if (ropeObjectPosition == null) {
            return false;
        }

        DynamicObject existing = ObjectManager.findDynamicObjectAt(
                ropeObjectPosition.getX(), ropeObjectPosition.getY(),
                ropeObjectPosition.getPlane());
        if (existing != null) {
            if (existing.getWorldObject().getObjectId() == CLIMBING_ROPE_OBJECT_ID) {
                player.getPacketSender().sendGameMessage("A climbing rope is already attached here.");
                return true;
            }
            player.getPacketSender().sendGameMessage("You cannot attach a rope here.");
            return true;
        }

        int orientation = (battlementOrientation + 2) & 3;

        player.getInventoryManager().removeItem(new ItemStack(CLIMBING_ROPE_ITEM_ID, 1));
        new DynamicObject(CLIMBING_ROPE_OBJECT_ID,
                ropeObjectPosition.getX(), ropeObjectPosition.getY(),
                ropeObjectPosition.getPlane(),
                orientation, CLIMBING_ROPE_OBJECT_TYPE, ServerSettings.placeholderObjectId,
                CLIMBING_ROPE_LIFETIME_TICKS, false);
        climbingRopes.put(key(battlementPosition),
                new ClimbingRopeState(battlementPosition, ropeObjectPosition, destination));
        player.getPacketSender().sendGameMessage("You attach the climbing rope to the battlements.");
        return true;
    }

    private static Position getClimbingRopeExteriorPosition(
            Position battlementPosition, int battlementOrientation) {
        if (battlementPosition == null) {
            return null;
        }

        int x = battlementPosition.getX();
        int y = battlementPosition.getY();
        switch (battlementOrientation & 3) {
            case 0: // west wall
                --x;
                break;
            case 1: // north wall
                ++y;
                break;
            case 2: // east wall
                ++x;
                break;
            case 3: // south wall
                --y;
                break;
            default:
                return null;
        }
        return new Position(x, y, battlementPosition.getPlane());
    }

    private static ClimbingRopeState getActiveClimbingRope(int objectX, int objectY) {
        String battlementKey = key(new Position(objectX, objectY, CLIMBING_ROPE_PLANE));
        ClimbingRopeState rope = climbingRopes.get(battlementKey);
        if (rope == null) {
            for (ClimbingRopeState candidate : climbingRopes.values()) {
                if (candidate.objectPosition.getX() == objectX
                        && candidate.objectPosition.getY() == objectY
                        && candidate.objectPosition.getPlane() == CLIMBING_ROPE_PLANE) {
                    rope = candidate;
                    battlementKey = key(candidate.position);
                    break;
                }
            }
        }
        if (rope == null) {
            return null;
        }

        DynamicObject dynamicObject = ObjectManager.findDynamicObjectByIdAt(
                CLIMBING_ROPE_OBJECT_ID,
                rope.objectPosition.getX(), rope.objectPosition.getY(),
                rope.objectPosition.getPlane());
        if (dynamicObject == null) {
            climbingRopes.remove(battlementKey);
            return null;
        }
        return rope;
    }

    private static Position findClimbingRopeDestination(int objectX, int objectY) {
        Position best = null;
        int bestDistance = Integer.MAX_VALUE;
        for (int x = objectX - 2; x <= objectX + 2; ++x) {
            for (int y = objectY - 2; y <= objectY + 2; ++y) {
                Position candidate = new Position(x, y, CLIMBING_ROPE_DESTINATION_PLANE);
                int clipping = WalkingCollisionMap.getTileFlags(
                        x, y, CLIMBING_ROPE_DESTINATION_PLANE);
                if (!CastleWarsManager.isCastleBattlementPosition(candidate)
                        || (clipping & 0x1280100) != 0) {
                    continue;
                }
                int distance = Math.abs(x - objectX) + Math.abs(y - objectY);
                if (distance < bestDistance) {
                    bestDistance = distance;
                    best = candidate;
                }
            }
        }
        return best;
    }

    private static CastleWarsManager.Team getBattlementTeam(int x, int y) {
        if ((x >= 2415 && x <= 2431 && y >= 3072 && y <= 3083)
                || (x >= 2412 && x <= 2431 && y >= 3084 && y <= 3089)) {
            return CastleWarsManager.Team.SARADOMIN;
        }
        if ((x >= 2368 && x <= 2384 && y >= 3124 && y <= 3135)
                || (x >= 2368 && x <= 2387 && y >= 3117 && y <= 3123)) {
            return CastleWarsManager.Team.ZAMORAK;
        }
        return null;
    }

    public static boolean isBattlementWalkwayTile(Position position,
                                                     CastleWarsManager.Team team) {
        if (position == null || team == null || position.getPlane() != CLIMBING_ROPE_PLANE
                || getBattlementTeam(position.getX(), position.getY()) != team) {
            return false;
        }
        if ((WalkingCollisionMap.getTileFlags(position.getX(), position.getY(),
                position.getPlane()) & 0x1280100) != 0) {
            return false;
        }

        // A real wall patrol tile must sit on the inside edge of an actual
        // battlement object. The old broad castle footprint also included ground
        // around the outside of the wall, which let guards patrol off the wall.
        for (int x = position.getX() - 2; x <= position.getX() + 2; ++x) {
            for (int y = position.getY() - 2; y <= position.getY() + 2; ++y) {
                if (Math.abs(x - position.getX()) + Math.abs(y - position.getY()) > 2
                        || getBattlementTeam(x, y) != team) {
                    continue;
                }
                if (isCastleWarsBattlementAt(x, y, CLIMBING_ROPE_PLANE)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static Position findNearestClimbableBattlement(Player player,
                                                           CastleWarsManager.Team targetTeam) {
        if (player == null || targetTeam == null || player.getPosition().getPlane() != 0) {
            return null;
        }
        CastleWarsManager.Team playerTeam = CastleWarsManager.getGameTeam(player);
        if (playerTeam == null || playerTeam == targetTeam) {
            return null;
        }

        int minX = targetTeam == CastleWarsManager.Team.SARADOMIN ? 2412 : 2368;
        int maxX = targetTeam == CastleWarsManager.Team.SARADOMIN ? 2431 : 2387;
        int minY = targetTeam == CastleWarsManager.Team.SARADOMIN ? 3072 : 3117;
        int maxY = targetTeam == CastleWarsManager.Team.SARADOMIN ? 3089 : 3135;
        Position best = null;
        int bestDistance = Integer.MAX_VALUE;

        for (int x = minX; x <= maxX; ++x) {
            for (int y = minY; y <= maxY; ++y) {
                boolean activeRope = getActiveClimbingRope(x, y) != null;
                boolean battlement = isCastleWarsBattlementAt(
                        x, y, CLIMBING_ROPE_PLANE);
                if (!activeRope && !battlement) {
                    continue;
                }

                Position candidate = new Position(x, y, CLIMBING_ROPE_PLANE);
                int distance = GameUtil.getDistance(player.getPosition(), candidate);
                if (distance < bestDistance) {
                    bestDistance = distance;
                    best = candidate;
                }
            }
        }
        return best == null ? null : best.copy();
    }

    public static boolean useClimbingRopeForBot(Player player, Position battlement) {
        if (player == null || battlement == null || !player.isBot
                || !CastleWarsManager.isInGame(player)
                || player.getPosition().getPlane() != CLIMBING_ROPE_PLANE
                || battlement.getPlane() != CLIMBING_ROPE_PLANE
                || !isInInteractionRange(player, battlement, 2)) {
            return false;
        }

        CastleWarsManager.Team playerTeam = CastleWarsManager.getGameTeam(player);
        CastleWarsManager.Team targetTeam =
                getBattlementTeam(battlement.getX(), battlement.getY());
        if (playerTeam == null || targetTeam == null || playerTeam == targetTeam) {
            return false;
        }

        if (getActiveClimbingRope(battlement.getX(), battlement.getY()) != null) {
            return handleClimbingRope(player, CLIMBING_ROPE_OBJECT_ID,
                    battlement.getX(), battlement.getY());
        }

        int battlementId = getCastleWarsBattlementObjectId(
                battlement.getX(), battlement.getY(), CLIMBING_ROPE_PLANE);
        if (battlementId < 0
                || player.getInventoryManager().getItemAmount(CLIMBING_ROPE_ITEM_ID) <= 0) {
            return false;
        }

        if (!attachClimbingRope(player, battlementId,
                battlement.getX(), battlement.getY(), CLIMBING_ROPE_PLANE)) {
            return false;
        }
        return handleClimbingRope(player, CLIMBING_ROPE_OBJECT_ID,
                battlement.getX(), battlement.getY());
    }

    private static void clearAllClimbingRopes() {
        for (ClimbingRopeState rope : climbingRopes.values()) {
            DynamicObject dynamicObject = ObjectManager.findDynamicObjectByIdAt(
                    CLIMBING_ROPE_OBJECT_ID,
                    rope.objectPosition.getX(), rope.objectPosition.getY(),
                    rope.objectPosition.getPlane());
            if (dynamicObject != null) {
                dynamicObject.remainingTicks = 0;
            }
        }
        climbingRopes.clear();
    }

    public static boolean placeBarricade(Player player) {
        return player != null && placeBarricadeAt(player, player.getPosition().copy());
    }

    /**
     * Defender bots put barricades on an adjacent, preselected defensive tile.
     * A bot must NOT stand inside its newly blocking object: doing so can trap
     * it, and letting all defenders use their current tile causes pile-ups.
     * Human placement continues to use the player's own tile as before.
     */
    public static boolean placeBarricadeForBot(Player bot, Position target) {
        if (bot == null || !bot.isBot || target == null
                || bot.getPosition().getPlane() != target.getPlane()
                || GameUtil.getDistance(bot.getPosition(), target) != 1) {
            return false;
        }
        CastleWarsManager.Team team = CastleWarsManager.getGameTeam(bot);
        return isDefensiveBarricadeSiteAvailable(team, target)
                && placeBarricadeAt(bot, target.copy());
    }

    /**
     * Only use free tiles inside the defender's own castle. Never block an
     * occupied tile, a doorway, an existing barricade or a staircase/object.
     */
    public static boolean isDefensiveBarricadeSiteAvailable(
            CastleWarsManager.Team team, Position position) {
        if (team == null || position == null
                || (position.getPlane() != 0 && position.getPlane() != 3)
                || CastleWarsManager.getCastleTeamAtPosition(position) != team
                || CastleWarsManager.isGroundCastleExteriorTransitionTile(position)
                || isProtectedCastlePassage(team, position)
                || (WalkingCollisionMap.getTileFlags(position.getX(), position.getY(),
                        position.getPlane()) & 0x1280100) != 0
                || barricades.containsKey(key(position))
                || ObjectManager.findDynamicObjectAt(
                        position.getX(), position.getY(), position.getPlane()) != null) {
            return false;
        }
        for (Player occupant : CastleWarsManager.getGamePlayersView()) {
            if (occupant != null && !occupant.isDead()
                    && position.equals(occupant.getPosition())) {
                return false;
            }
        }
        return true;
    }

    /**
     * Never allow bot barricades on the central double-door approach, the
     * castle's stair access, or the flag's immediate interaction tiles.
     * Bot placement is intentionally stricter than human placement.
     */
    public static boolean isProtectedCastlePassage(CastleWarsManager.Team team,
                                                    Position position) {
        if (team == null || position == null) {
            return false;
        }
        int x = position.getX();
        int y = position.getY();
        int plane = position.getPlane();
        if (plane == 0) {
            return team == CastleWarsManager.Team.SARADOMIN
                    ? (x >= 2425 && x <= 2428 && y >= 3082)
                    || (x >= 2418 && x <= 2420 && y >= 3076 && y <= 3079)
                    : (x >= 2371 && x <= 2374 && y <= 3125)
                    || (x >= 2379 && x <= 2381 && y >= 3127 && y <= 3130);
        }
        if (plane == 3) {
            Position flag = team == CastleWarsManager.Team.SARADOMIN
                    ? new Position(2429, 3074, 3) : new Position(2370, 3133, 3);
            Position stairs = team == CastleWarsManager.Team.SARADOMIN
                    ? new Position(2425, 3074, 3) : new Position(2374, 3133, 3);
            return GameUtil.getDistance(position, flag) <= 2
                    || GameUtil.getDistance(position, stairs) <= 1;
        }
        return false;
    }

    /** Keep independent barricade lines spread out instead of stacking the gate. */
    public static boolean isDefensiveBarricadeSeparated(
            CastleWarsManager.Team team, Position position, int minimumDistance) {
        if (team == null || position == null) {
            return false;
        }
        for (BarricadeState barricade : barricades.values()) {
            if (barricade.team == team
                    && barricade.position.getPlane() == position.getPlane()
                    && GameUtil.getDistance(position, barricade.position) < minimumDistance) {
                return false;
            }
        }
        return true;
    }

    private static boolean placeBarricadeAt(Player player, Position position) {
        CastleWarsManager.Team team = CastleWarsManager.getGameTeam(player);
        if (team == null || player.getInventoryManager().getItemAmount(BARRICADE_ITEM_ID) <= 0) {
            return false;
        }
        if (getBarricadeCount(team) >= 10) {
            player.getPacketSender().sendGameMessage("Your team already has ten barricades set up.");
            return false;
        }

        String key = key(position);
        if (barricades.containsKey(key) || ObjectManager.findDynamicObjectAt(
                position.getX(), position.getY(), position.getPlane()) != null) {
            return false;
        }

        int objectId = barricadeVisualObjectId(false);
        player.getInventoryManager().removeItem(new ItemStack(BARRICADE_ITEM_ID, 1));
        new DynamicObject(objectId, position.getX(), position.getY(), position.getPlane(),
                0, 10, ServerSettings.placeholderObjectId, 99999, false);
        WalkingCollisionMap.addObjectCollision(objectId, position.getX(), position.getY(),
                position.getPlane(), 0, 10, true);
        ProjectileCollisionMap.addObjectCollision(objectId, position.getX(), position.getY(),
                position.getPlane(), 0, 10, true);
        barricades.put(key, new BarricadeState(team, position, objectId));
        player.getUpdateState().setAnimation(827);
        return true;
    }

    public static boolean isBarricadeObject(int objectId) {
        return objectId == BARRICADE_OBJECT_UNLIT
                || objectId == BARRICADE_OBJECT_BURNING;
    }

    public static int getBarricadeHitpoints(Position position) {
        BarricadeState state = position == null ? null : barricades.get(key(position));
        return state == null ? 0 : state.getHitpoints();
    }

    static long barricadeAttackDelayMillis(int weaponDelayTicks) {
        return Math.max(1, weaponDelayTicks) * GAME_TICK_MILLIS;
    }

    /**
     * Called each interaction tick until the target is destroyed or the player
     * interrupts the action. Returns true while melee attacks should continue.
     */
    public static boolean attackBarricade(Player player, int objectId, Position position) {
        if (player == null || position == null || !isBarricadeObject(objectId)
                || player.isDead() || player.isStunned()
                || !CastleWarsManager.isInGame(player)) {
            return false;
        }
        BarricadeState state = barricades.get(key(position));
        if (state == null || state.objectId != objectId
                || !isInInteractionRange(player, state.position, 1)) {
            return false;
        }

        int fightMode = player.getFightMode();
        AttackStyleDefinition[] styles =
                player.getWeaponProfile().getInterfaceDefinition().getAttackStyles();
        if (fightMode < 0 || fightMode >= styles.length
                || fightMode >= player.getWeaponProfile().getAttackAnimations().length) {
            return false;
        }
        if (styles[fightMode].getCombatType() != CombatType.MELEE) {
            player.getPacketSender().sendGameMessage(
                    "You need to use a melee attack to damage a barricade.");
            return false;
        }

        // The target occupies a blocked tile. Never path through it while attacking.
        player.getMovementQueue().clear();
        player.getMovementQueue().clearMovementActions();
        long now = System.currentTimeMillis();
        Long readyAt = barricadeAttackReadyAt.get(player);
        if (readyAt != null && now < readyAt.longValue()) {
            return true;
        }
        player.getUpdateState().setFacePosition(position.centerForSize(1));
        player.getUpdateState().setAnimation(
                player.getWeaponProfile().getAttackAnimations()[fightMode]);
        barricadeAttackReadyAt.put(player, Long.valueOf(now
                + barricadeAttackDelayMillis(player.getWeaponProfile().getAttackDelay())));

        // Use melee max-hit calculation without hitting an entity or awarding XP.
        WeaponCombatAttack attack =
                new WeaponCombatAttack(player, player, player.getWeaponProfile());
        int maxHit = Math.max(1,
                (int) CombatManager.calculateMeleeMaxHit(player, attack));
        int damage = GameUtil.randomInt(maxHit + 1);
        if (damage > 0 && state.takeDamage(damage) == 0) {
            removeBarricade(state);
            player.getPacketSender().sendGameMessage("You destroy the barricade.");
            return false;
        }
        return true;
    }

    public static Position findNearestEnemyBarricade(Player player, int radius) {
        CastleWarsManager.Team team = CastleWarsManager.getGameTeam(player);
        if (team == null) {
            return null;
        }
        Position best = null;
        int bestDistance = Integer.MAX_VALUE;
        for (BarricadeState barricade : barricades.values()) {
            if (barricade.team == team || barricade.position.getPlane() != player.getPosition().getPlane()) {
                continue;
            }
            int distance = GameUtil.getDistance(player.getPosition(), barricade.position);
            if (distance <= radius && distance < bestDistance) {
                bestDistance = distance;
                best = barricade.position;
            }
        }
        return best == null ? null : best.copy();
    }

    /**
     * Select an actual barricade in the bot's next two tiles of travel.
     * Look at both teams' barricades: even a friendly barricade can trap a
     * returning flag runner. Avoid igniting nearby but irrelevant structures.
     */
    public static Position findBarricadeBlockingBotRoute(Player bot, Position destination) {
        if (bot == null || !bot.isBot || destination == null
                || bot.getPosition().getPlane() != destination.getPlane()
                || CastleWarsManager.getGameTeam(bot) == null) {
            return null;
        }
        Position best = null;
        int bestDistance = Integer.MAX_VALUE;
        for (BarricadeState barricade : barricades.values()) {
            if (barricade.position.getPlane() != bot.getPosition().getPlane()
                    || !isBarricadeAheadOnRoute(bot.getPosition(), destination,
                            barricade.position)) {
                continue;
            }
            int distance = GameUtil.getDistance(bot.getPosition(), barricade.position);
            if (distance <= 2 && distance < bestDistance) {
                bestDistance = distance;
                best = barricade.position;
            }
        }
        return best == null ? null : best.copy();
    }

    /** A two-tile-wide corridor in front of the bot towards its current goal. */
    public static boolean isBarricadeAheadOnRoute(
            Position from, Position destination, Position barricade) {
        if (from == null || destination == null || barricade == null
                || from.getPlane() != destination.getPlane()
                || from.getPlane() != barricade.getPlane()) {
            return false;
        }
        long dx = destination.getX() - from.getX();
        long dy = destination.getY() - from.getY();
        long bx = barricade.getX() - from.getX();
        long by = barricade.getY() - from.getY();
        long lengthSquared = dx * dx + dy * dy;
        long forward = dx * bx + dy * by;
        if (lengthSquared == 0 || forward <= 0 || forward > lengthSquared) {
            return false;
        }
        long side = dx * by - dy * bx;
        return side * side <= 4L * lengthSquared;
    }

    public static boolean isBarricadeBurning(Position position) {
        BarricadeState barricade = position == null ? null : barricades.get(key(position));
        return barricade != null && barricade.burning;
    }

    public static boolean igniteBarricade(Player player, Position position) {
        if (player.getInventoryManager().getItemAmount(TINDERBOX_ITEM_ID) <= 0 || position == null) {
            return false;
        }
        BarricadeState state = barricades.get(key(position));
        if (state == null || GameUtil.getDistance(player.getPosition(), state.position) > 2) {
            return false;
        }
        if (state.burning) {
            player.getPacketSender().sendGameMessage("The barricade is already on fire.");
            return true;
        }

        state.burning = true;
        state.burnExpiresAt = System.currentTimeMillis() + BARRICADE_BURN_DURATION_MILLIS;
        updateBarricadeAppearance(state);
        player.getUpdateState().setAnimation(733);
        player.getPacketSender().sendGameMessage("You set fire to the barricade.");
        return true;
    }

    public static boolean extinguishBarricade(Player player, Position position) {
        if (player.getInventoryManager().getItemAmount(BUCKET_OF_WATER_ITEM_ID) <= 0 || position == null) {
            return false;
        }
        BarricadeState state = barricades.get(key(position));
        if (state == null || GameUtil.getDistance(player.getPosition(), state.position) > 2) {
            return false;
        }
        if (!state.burning) {
            player.getPacketSender().sendGameMessage("The barricade isn't on fire.");
            return true;
        }

        if (!player.getInventoryManager().removeItem(new ItemStack(BUCKET_OF_WATER_ITEM_ID, 1))) {
            return false;
        }
        player.getInventoryManager().addItem(new ItemStack(EMPTY_BUCKET_ITEM_ID, 1));
        state.burning = false;
        state.burnExpiresAt = 0L;
        updateBarricadeAppearance(state);
        player.getPacketSender().sendGameMessage("You extinguish the barricade.");
        return true;
    }

    public static boolean destroyBarricadeWithExplosive(Player player, Position position) {
        if (player.getInventoryManager().getItemAmount(EXPLOSIVE_POTION_ID) <= 0 || position == null) {
            return false;
        }
        BarricadeState state = barricades.get(key(position));
        if (state == null || GameUtil.getDistance(player.getPosition(), state.position) > 2) {
            return false;
        }
        if (state.burning) {
            player.getPacketSender().sendGameMessage("The burning barricade resists the explosive potion.");
            return true;
        }
        player.getInventoryManager().removeItem(new ItemStack(EXPLOSIVE_POTION_ID, 1));
        removeBarricade(state);
        player.getPacketSender().sendStillGraphicToNearbyPlayers(176,
                position.getX(), position.getY(), position.getPlane(), 0);
        return true;
    }

    public static void processBarricadeFires(long now) {
        if (barricades.isEmpty()) {
            return;
        }
        ArrayList<BarricadeState> burnedOut = new ArrayList<BarricadeState>();
        for (BarricadeState state : barricades.values()) {
            if (state.burning && state.burnExpiresAt > 0L && now >= state.burnExpiresAt) {
                burnedOut.add(state);
            }
        }
        for (BarricadeState state : burnedOut) {
            removeBarricade(state);
        }
    }

    // The client cache animates object 4422 with flames. Keep all freshly
    // placed barricades on object 4421 regardless of the placing team.
    static int barricadeVisualObjectId(boolean burning) {
        return burning ? BARRICADE_OBJECT_BURNING : BARRICADE_OBJECT_UNLIT;
    }

    private static void updateBarricadeAppearance(BarricadeState state) {
        Position position = state.position;
        // Placement supplies collision explicitly, so swapping the visual object
        // must not alter the barricade's walking or projectile blocking.
        ObjectManager.getInstance().removeDynamicObjectAt(
                position.getX(), position.getY(), position.getPlane(), 10);
        new DynamicObject(barricadeVisualObjectId(state.burning),
                position.getX(), position.getY(), position.getPlane(),
                0, 10, ServerSettings.placeholderObjectId, 99999, false);
    }

    public static int getBarricadeCount(CastleWarsManager.Team team) {
        int count = 0;
        for (BarricadeState state : barricades.values()) {
            if (state.team == team) {
                ++count;
            }
        }
        return count;
    }

    public static boolean isRockslideCollapsed(int index) {
        return index >= 0 && index < rockslideCollapsed.length && rockslideCollapsed[index];
    }

    public static Position getRockslidePosition(int index) {
        if (index < 0 || index >= ROCKSLIDE_POSITIONS.length) {
            return null;
        }
        return ROCKSLIDE_POSITIONS[index].copy();
    }

    public static boolean clearRockslide(Player player, int index) {
        if (player == null || !CastleWarsManager.isInGame(player)
                || !isRockslideCollapsed(index)) {
            return false;
        }
        Position position = ROCKSLIDE_POSITIONS[index];
        if (player.getPosition().getPlane() != position.getPlane()
                || GameUtil.getDistance(player.getPosition(), position) > 3) {
            return false;
        }

        if (player.getInventoryManager().getItemAmount(EXPLOSIVE_POTION_ID) > 0) {
            cancelRockslideMining(player);
            player.getInventoryManager().removeItem(new ItemStack(EXPLOSIVE_POTION_ID, 1));
            player.getPacketSender().sendStillGraphicToNearbyPlayers(176,
                    position.getX(), position.getY(), 0, 0);
            setRockslideState(index, false);
            return true;
        }

        GatheringToolDefinition pickaxe =
                ItemCombinationHandler.findUsableGatheringTool(player, 14);
        return pickaxe != null
                && useRockslidePickaxe(player, index, pickaxe.getToolItemId(), false);
    }

    public static boolean clearRockslideWithPickaxe(Player player, int index) {
        if (player == null || !CastleWarsManager.isInGame(player)
                || !isRockslideCollapsed(index)) {
            return false;
        }
        Position position = ROCKSLIDE_POSITIONS[index];
        if (player.getPosition().getPlane() != position.getPlane()
                || GameUtil.getDistance(player.getPosition(), position) > 3) {
            return false;
        }

        GatheringToolDefinition pickaxe =
                ItemCombinationHandler.findUsableGatheringTool(player, 14);
        return pickaxe != null
                && useRockslidePickaxe(player, index, pickaxe.getToolItemId(), false);
    }

    public static boolean collapseRockslide(Player player, int index) {
        if (player == null || !CastleWarsManager.isInGame(player)
                || index < 0 || index >= ROCKSLIDE_POSITIONS.length
                || isRockslideCollapsed(index)) {
            return false;
        }
        Position position = ROCKSLIDE_POSITIONS[index];
        if (player.getPosition().getPlane() != position.getPlane()
                || GameUtil.getDistance(player.getPosition(), position) > 4) {
            return false;
        }

        if (player.getInventoryManager().getItemAmount(EXPLOSIVE_POTION_ID) > 0) {
            cancelRockslideMining(player);
            player.getInventoryManager().removeItem(new ItemStack(EXPLOSIVE_POTION_ID, 1));
            player.getPacketSender().sendStillGraphicToNearbyPlayers(176,
                    position.getX(), position.getY(), 0, 0);
            setRockslideState(index, true);
            crushPlayersAtRockslide(position);
            return true;
        }

        GatheringToolDefinition pickaxe =
                ItemCombinationHandler.findUsableGatheringTool(player, 14);
        return pickaxe != null
                && useRockslidePickaxe(player, index, pickaxe.getToolItemId(), true);
    }

    private static boolean useRockslideExplosive(Player player, int index,
                                                   boolean collapse) {
        if (index < 0 || index >= ROCKSLIDE_POSITIONS.length) {
            return false;
        }
        Position position = ROCKSLIDE_POSITIONS[index];
        if (player.getPosition().getPlane() != position.getPlane()
                || GameUtil.getDistance(player.getPosition(), position) > 4
                || player.getInventoryManager().getItemAmount(EXPLOSIVE_POTION_ID) <= 0) {
            return false;
        }
        if (rockslideCollapsed[index] == collapse) {
            player.getPacketSender().sendGameMessage(collapse
                    ? "This passage is already collapsed."
                    : "These rocks have already been cleared.");
            return true;
        }

        cancelRockslideMining(player);
        player.getInventoryManager().removeItem(new ItemStack(EXPLOSIVE_POTION_ID, 1));
        player.getPacketSender().sendStillGraphicToNearbyPlayers(
                176, position.getX(), position.getY(), 0, 0);
        setRockslideState(index, collapse);
        if (collapse) {
            crushPlayersAtRockslide(position);
            player.getPacketSender().sendGameMessage("You collapse the tunnel.");
        } else {
            player.getPacketSender().sendGameMessage("You clear the fallen rocks.");
        }
        return true;
    }

    private static boolean useRockslidePickaxe(Player player, int index, int itemId,
                                                boolean collapse) {
        if (player == null || !CastleWarsManager.isInGame(player)
                || index < 0 || index >= ROCKSLIDE_POSITIONS.length) {
            return false;
        }
        RockslideMiningTask pending = activeRockslideMining.get(player);
        if (pending != null && pending.index == index && pending.collapse == collapse
                && player.isCurrentActionSequence(pending.actionSequence)
                && !pending.cancelled) {
            return true; // Bots retry this operation while waiting; don't restart the timer.
        }
        GatheringToolDefinition pickaxe = getPickaxeDefinition(itemId);
        if (pickaxe == null) {
            return false;
        }
        Position position = ROCKSLIDE_POSITIONS[index];
        if (player.getPosition().getPlane() != position.getPlane()
                || GameUtil.getDistance(player.getPosition(), position) > 4) {
            return false;
        }
        if (player.getInventoryManager().getItemAmount(itemId) <= 0
                && player.getEquipmentManager().getContainer().getItemAmount(itemId) <= 0) {
            return true;
        }
        if (player.getSkillManager().getCurrentLevels()[14] < pickaxe.getRequiredLevel()) {
            player.getPacketSender().sendGameMessage(
                    "You need a Mining level of " + pickaxe.getRequiredLevel()
                    + " to use this pickaxe.");
            return true;
        }
        if (rockslideCollapsed[index] == collapse) {
            player.getPacketSender().sendGameMessage(collapse
                    ? "This passage is already collapsed."
                    : "These rocks have already been cleared.");
            return true;
        }

        cancelRockslideMining(player);
        // The existing pickaxe speeds are 8 ticks for bronze down to 3 for rune.
        // Explosives remain instant; only mining waits for these swings to finish.
        int durationTicks = Math.max(3, (int) Math.ceil(pickaxe.getToolSpeed()));
        int sequence = player.nextActionSequence();
        player.beginInterruptibleAction();
        RockslideMiningTask task = new RockslideMiningTask(player, sequence, index,
                collapse, itemId, pickaxe.getGatherAnimationId(), durationTicks,
                rockslideRevisions[index]);
        activeRockslideMining.put(player, task);
        player.getPacketSender().sendGameMessage(collapse
                ? "You attempt to collapse the passage..."
                : "You attempt to mine the rocks...");
        player.getUpdateState().setAnimation(pickaxe.getGatherAnimationId());
        CycleEventHandler.getInstance().schedule(player, task, 1);
        return true;
    }

    private static void cancelRockslideMining(Player player) {
        RockslideMiningTask task = activeRockslideMining.get(player);
        if (task != null) {
            task.cancel();
        }
    }

    private static void cancelAllRockslideMining() {
        for (RockslideMiningTask task : new ArrayList<RockslideMiningTask>(
                activeRockslideMining.values())) {
            task.cancel();
        }
    }

    private static final class RockslideMiningTask extends CycleEvent {
        private final Player player;
        private final Position startingPosition;
        private final int actionSequence;
        private final int index;
        private final boolean collapse;
        private final int itemId;
        private final int animationId;
        private final int revision;
        private int remainingTicks;
        private boolean cancelled;

        private RockslideMiningTask(Player player, int actionSequence, int index,
                                    boolean collapse, int itemId, int animationId,
                                    int durationTicks, int revision) {
            this.player = player;
            this.startingPosition = player.getPosition().copy();
            this.actionSequence = actionSequence;
            this.index = index;
            this.collapse = collapse;
            this.itemId = itemId;
            this.animationId = animationId;
            this.remainingTicks = durationTicks;
            this.revision = revision;
        }

        @Override
        public void execute(CycleEventContainer container) {
            Position position = player.getPosition();
            GatheringToolDefinition pickaxe = getPickaxeDefinition(itemId);
            if (cancelled || activeRockslideMining.get(player) != this
                    || !player.isCurrentActionSequence(actionSequence)
                    || !player.isInterruptibleActionActive()
                    || !CastleWarsManager.isInGame(player) || player.isDead()
                    || player.isStunned() || player.isMoving()
                    || player.getCombatTarget() != null
                    || position.getX() != startingPosition.getX()
                    || position.getY() != startingPosition.getY()
                    || position.getPlane() != startingPosition.getPlane()
                    || GameUtil.getDistance(position, ROCKSLIDE_POSITIONS[index]) > 4
                    || rockslideRevisions[index] != revision
                    || rockslideCollapsed[index] == collapse
                    || pickaxe == null
                    || (player.getInventoryManager().getItemAmount(itemId) <= 0
                        && player.getEquipmentManager().getContainer().getItemAmount(itemId) <= 0)
                    || player.getSkillManager().getCurrentLevels()[14] < pickaxe.getRequiredLevel()) {
                container.stop();
                return;
            }

            if (--remainingTicks > 0) {
                if (remainingTicks % 3 == 0) {
                    player.getUpdateState().setAnimation(animationId);
                }
                return;
            }

            Position rock = ROCKSLIDE_POSITIONS[index];
            setRockslideState(index, collapse);
            if (collapse) {
                crushPlayersAtRockslide(rock);
                player.getPacketSender().sendGameMessage("You collapse the tunnel.");
            } else {
                player.getPacketSender().sendGameMessage("You clear the fallen rocks.");
            }
            container.stop();
        }

        private void cancel() {
            cancelled = true;
            if (activeRockslideMining.get(player) == this) {
                activeRockslideMining.remove(player);
                if (player.isCurrentActionSequence(actionSequence)) {
                    player.getUpdateState().setAnimation(-1);
                }
            }
        }

        @Override
        public void onStop() {
            cancel();
        }
    }

    public static boolean handleRockslideObjectAction(Player player, int objectId,
                                                       int objectX, int objectY) {
        if (player == null || !CastleWarsManager.isInGame(player)
                || (objectId != CAVE_WALL_OBJECT_ID
                && objectId != COLLAPSED_ROCK_OBJECT_ID
                && objectId != PARTIAL_ROCK_OBJECT_ID)) {
            return false;
        }
        int index = findRockslideIndexForObject(objectId, objectX, objectY);
        if (index < 0) {
            return false;
        }

        GatheringToolDefinition pickaxe =
                ItemCombinationHandler.findUsableGatheringTool(player, 14);
        if (pickaxe == null) {
            player.getPacketSender().sendGameMessage(
                    "You do not have a pickaxe that you can use.");
            return true;
        }
        return useRockslidePickaxe(player, index, pickaxe.getToolItemId(),
                objectId == CAVE_WALL_OBJECT_ID);
    }

    private static void toggleRockslide(Player player, int index, Position position) {
        boolean collapsing = !rockslideCollapsed[index];
        setRockslideState(index, collapsing);
        if (collapsing) {
            crushPlayersAtRockslide(position);
            player.getPacketSender().sendGameMessage("You collapse the tunnel.");
        } else {
            player.getPacketSender().sendGameMessage("You clear the fallen rocks.");
        }
    }

    private static boolean isPickaxeItemId(int itemId) {
        return getPickaxeDefinition(itemId) != null;
    }

    private static GatheringToolDefinition getPickaxeDefinition(int itemId) {
        for (GatheringToolDefinition tool : GatheringToolDefinition.values()) {
            if (tool.getSkillId() == 14 && tool.getToolItemId() == itemId) {
                return tool;
            }
        }
        return null;
    }

    private static int findRockslideIndex(int x, int y) {
        for (int i = 0; i < ROCKSLIDE_POSITIONS.length; ++i) {
            Position position = ROCKSLIDE_POSITIONS[i];
            if (position.getX() == x && position.getY() == y) {
                return i;
            }
        }
        return -1;
    }

    private static int findRockslideIndexForObject(int objectId, int x, int y) {
        int exact = findRockslideIndex(x, y);
        if (exact >= 0) {
            return exact;
        }
        if (objectId != CAVE_WALL_OBJECT_ID) {
            return -1;
        }

        // Cave-wall objects span the 4x4 collapse zones around each rock pile.
        if (x >= 2390 && x <= 2393 && y >= 9500 && y <= 9503) {
            return 0;
        }
        if (x >= 2399 && x <= 2402 && y >= 9511 && y <= 9514) {
            return 1;
        }
        if (x >= 2408 && x <= 2411 && y >= 9502 && y <= 9505) {
            return 2;
        }
        if (x >= 2400 && x <= 2403 && y >= 9493 && y <= 9496) {
            return 3;
        }
        return -1;
    }

    public static boolean fireCatapult(Player player) {
        // Automatic target selection is reserved for bots. Human players
        // must choose their target using the catapult aiming interface.
        if (player == null || !player.isBot) {
            return false;
        }
        CastleWarsManager.Team team = CastleWarsManager.getGameTeam(player);
        if (team == null) {
            return false;
        }
        Position target = chooseCatapultTarget(team);
        return target != null && fireCatapultAt(player, target);
    }

    private static boolean fireCatapultAt(Player player, Position target) {
        CastleWarsManager.Team team = CastleWarsManager.getGameTeam(player);
        if (team == null || target == null
                || player.getInventoryManager().getItemAmount(ROCK_ITEM_ID) <= 0) {
            return false;
        }
        if (!isCatapultOperational(team) || isCatapultBurning(team)) {
            return false;
        }

        Position catapult = getCatapultPosition(team);
        if (GameUtil.getDistance(player.getPosition(), catapult) > 3
                || player.getPosition().getPlane() != 0) {
            return false;
        }

        long now = System.currentTimeMillis();
        Long readyAt = catapultReadyAt.get(player);
        if (readyAt != null && now < readyAt.longValue()) {
            return false;
        }

        if (!player.getInventoryManager().removeItem(new ItemStack(ROCK_ITEM_ID, 1))) {
            return false;
        }
        catapultReadyAt.put(player, Long.valueOf(now + 12000L));
        player.getPacketSender().sendStillGraphicToNearbyPlayers(287,
                target.getX(), target.getY(), 0, 0);
        damageCatapultArea(target);
        return true;
    }

    private static Position resolveCatapultAimTarget(CastleWarsManager.Team team,
                                                      int aimX, int aimY) {
        int xDistance = (aimX + CATAPULT_AIM_SCALE - 1) / CATAPULT_AIM_SCALE;
        int yDistance = (aimY + CATAPULT_AIM_SCALE - 1) / CATAPULT_AIM_SCALE;
        if (team == CastleWarsManager.Team.SARADOMIN) {
            return new Position(SARADOMIN_CATAPULT.getX() - 1 - xDistance,
                    SARADOMIN_CATAPULT.getY() + 1 + yDistance, 0);
        }
        return new Position(ZAMORAK_CATAPULT.getX() + 1 + xDistance,
                ZAMORAK_CATAPULT.getY() - 1 - yDistance, 0);
    }

    private static Position scatterCatapultTarget(Position selectedTarget) {
        int spread = CATAPULT_MISS_RADIUS * 2 + 1;
        return new Position(selectedTarget.getX() - CATAPULT_MISS_RADIUS + GameUtil.randomInt(spread),
                selectedTarget.getY() - CATAPULT_MISS_RADIUS + GameUtil.randomInt(spread), 0);
    }

    public static boolean igniteEnemyCatapult(Player player, int objectId) {
        CastleWarsManager.Team team = CastleWarsManager.getGameTeam(player);
        CastleWarsManager.Team targetTeam = getCatapultTeamForObject(objectId);
        if (team == null || targetTeam == null
                || player.getInventoryManager().getItemAmount(TINDERBOX_ITEM_ID) <= 0) {
            return false;
        }
        if (team == targetTeam) {
            player.getPacketSender().sendGameMessage("You can't set fire to your own team's catapult.");
            return true;
        }

        Position target = getCatapultPosition(targetTeam);
        if (player.getPosition().getPlane() != 0
                || GameUtil.getDistance(player.getPosition(), target) > 3) {
            return false;
        }
        if (!isCatapultOperational(targetTeam)) {
            return false;
        }
        if (isCatapultBurning(targetTeam)) {
            player.getPacketSender().sendGameMessage("The catapult is already on fire.");
            return true;
        }

        player.getUpdateState().setAnimation(733);
        setCatapultBurning(targetTeam, true);
        player.getPacketSender().sendGameMessage("You set fire to the enemy catapult.");
        return true;
    }

    public static boolean extinguishOwnCatapult(Player player, int objectId) {
        CastleWarsManager.Team team = CastleWarsManager.getGameTeam(player);
        CastleWarsManager.Team targetTeam = getCatapultTeamForObject(objectId);
        if (team == null || targetTeam == null
                || player.getInventoryManager().getItemAmount(BUCKET_OF_WATER_ITEM_ID) <= 0) {
            return false;
        }
        if (team != targetTeam) {
            player.getPacketSender().sendGameMessage("You can only put out your own team's catapult.");
            return true;
        }

        Position target = getCatapultPosition(targetTeam);
        if (player.getPosition().getPlane() != 0
                || GameUtil.getDistance(player.getPosition(), target) > 3) {
            return false;
        }
        if (!isCatapultBurning(targetTeam)) {
            player.getPacketSender().sendGameMessage("The catapult isn't on fire.");
            return true;
        }
        if (!player.getInventoryManager().removeItem(new ItemStack(BUCKET_OF_WATER_ITEM_ID, 1))) {
            return false;
        }

        player.getInventoryManager().addItem(new ItemStack(EMPTY_BUCKET_ITEM_ID, 1));
        setCatapultBurning(targetTeam, false);
        player.getPacketSender().sendGameMessage("You extinguish your team's catapult.");
        return true;
    }

    public static void processCatapultFires(long now) {
        if (saradominCatapultBurning && saradominCatapultBurnExpiresAt > 0L
                && now >= saradominCatapultBurnExpiresAt) {
            setCatapultOperational(CastleWarsManager.Team.SARADOMIN, false);
        }
        if (zamorakCatapultBurning && zamorakCatapultBurnExpiresAt > 0L
                && now >= zamorakCatapultBurnExpiresAt) {
            setCatapultOperational(CastleWarsManager.Team.ZAMORAK, false);
        }
    }

    public static boolean sabotageEnemyCatapult(Player player) {
        CastleWarsManager.Team team = CastleWarsManager.getGameTeam(player);
        if (team == null || player.getInventoryManager().getItemAmount(EXPLOSIVE_POTION_ID) <= 0) {
            return false;
        }
        CastleWarsManager.Team enemy = team == CastleWarsManager.Team.SARADOMIN
                ? CastleWarsManager.Team.ZAMORAK : CastleWarsManager.Team.SARADOMIN;
        Position target = getCatapultPosition(enemy);
        if (!isInInteractionRange(player, target, 3)) {
            return false;
        }
        if (!isCatapultOperational(enemy) && !isCatapultBurning(enemy)) {
            return false;
        }

        player.getInventoryManager().removeItem(new ItemStack(EXPLOSIVE_POTION_ID, 1));
        player.getPacketSender().sendStillGraphicToNearbyPlayers(176,
                target.getX(), target.getY(), 0, 0);
        setCatapultOperational(enemy, false);
        return true;
    }

    /** Preserve a distinct burning state in the in-match castle status panel. */
    public static String getCatapultStatusText(CastleWarsManager.Team team) {
        if (team == null || !isCatapultOperational(team)) {
            return "Destroyed";
        }
        return isCatapultBurning(team) ? "Burning" : "Operational";
    }

    public static boolean isCatapultOperational(CastleWarsManager.Team team) {
        return team == CastleWarsManager.Team.SARADOMIN
                ? saradominCatapultOperational : zamorakCatapultOperational;
    }

    public static boolean isCatapultBurning(CastleWarsManager.Team team) {
        return team == CastleWarsManager.Team.SARADOMIN
                ? saradominCatapultBurning : zamorakCatapultBurning;
    }

    public static boolean repairOwnCatapult(Player player) {
        CastleWarsManager.Team team = CastleWarsManager.getGameTeam(player);
        if (team == null || isCatapultOperational(team) || isCatapultBurning(team)
                || player.getInventoryManager().getItemAmount(TOOLKIT_ID) <= 0) {
            return false;
        }
        Position target = team == CastleWarsManager.Team.SARADOMIN
                ? SARADOMIN_CATAPULT : ZAMORAK_CATAPULT;
        if (player.getPosition().getPlane() != 0
                || GameUtil.getDistance(player.getPosition(), target) > 3) {
            return false;
        }
        player.getUpdateState().setAnimation(898);
        setCatapultOperational(team, true);
        player.getPacketSender().sendGameMessage("You repair your team's catapult.");
        return true;
    }

    public static boolean isHomeTunnelCollapsed(CastleWarsManager.Team team, int route) {
        if (team == CastleWarsManager.Team.SARADOMIN) {
            return rockslideCollapsed[route == 0 ? 3 : 2];
        }
        return rockslideCollapsed[route == 0 ? 0 : 1];
    }

    private static void setCatapultOperational(CastleWarsManager.Team team, boolean operational) {
        Position position = getCatapultPosition(team);
        int normalId = team == CastleWarsManager.Team.SARADOMIN
                ? SARADOMIN_CATAPULT_ID : ZAMORAK_CATAPULT_ID;
        int damagedId = team == CastleWarsManager.Team.SARADOMIN
                ? SARADOMIN_DAMAGED_CATAPULT_ID : ZAMORAK_DAMAGED_CATAPULT_ID;

        removeCatapultDynamicObject(position);

        if (!operational) {
            new DynamicObject(damagedId, position.getX(), position.getY(), 0,
                    0, 10, normalId, 99999, false);
        }

        if (team == CastleWarsManager.Team.SARADOMIN) {
            saradominCatapultOperational = operational;
            saradominCatapultBurning = false;
            saradominCatapultBurnExpiresAt = 0L;
        } else {
            zamorakCatapultOperational = operational;
            zamorakCatapultBurning = false;
            zamorakCatapultBurnExpiresAt = 0L;
        }
    }

    private static void setCatapultBurning(CastleWarsManager.Team team, boolean burning) {
        Position position = getCatapultPosition(team);
        int normalId = team == CastleWarsManager.Team.SARADOMIN
                ? SARADOMIN_CATAPULT_ID : ZAMORAK_CATAPULT_ID;
        int burningId = team == CastleWarsManager.Team.SARADOMIN
                ? SARADOMIN_BURNING_CATAPULT_ID : ZAMORAK_BURNING_CATAPULT_ID;

        removeCatapultDynamicObject(position);
        if (burning) {
            new DynamicObject(burningId, position.getX(), position.getY(), 0,
                    0, 10, normalId, 99999, false);
        }

        long expiresAt = burning
                ? System.currentTimeMillis() + CATAPULT_BURN_DURATION_MILLIS : 0L;
        if (team == CastleWarsManager.Team.SARADOMIN) {
            saradominCatapultBurning = burning;
            saradominCatapultBurnExpiresAt = expiresAt;
        } else {
            zamorakCatapultBurning = burning;
            zamorakCatapultBurnExpiresAt = expiresAt;
        }
    }

    private static void removeCatapultDynamicObject(Position position) {
        DynamicObject existing = ObjectManager.findDynamicObjectAt(
                position.getX(), position.getY(), position.getPlane());
        if (existing != null) {
            ObjectManager.getInstance().removeDynamicObjectAt(
                    position.getX(), position.getY(), position.getPlane(), 10);
        }
    }

    private static Position getCatapultPosition(CastleWarsManager.Team team) {
        return team == CastleWarsManager.Team.SARADOMIN
                ? SARADOMIN_CATAPULT : ZAMORAK_CATAPULT;
    }

    private static CastleWarsManager.Team getCatapultTeamForObject(int objectId) {
        if (objectId == SARADOMIN_CATAPULT_ID
                || objectId == SARADOMIN_BURNING_CATAPULT_ID
                || objectId == SARADOMIN_DAMAGED_CATAPULT_ID) {
            return CastleWarsManager.Team.SARADOMIN;
        }
        if (objectId == ZAMORAK_CATAPULT_ID
                || objectId == ZAMORAK_BURNING_CATAPULT_ID
                || objectId == ZAMORAK_DAMAGED_CATAPULT_ID) {
            return CastleWarsManager.Team.ZAMORAK;
        }
        return null;
    }

    private static void setRockslideState(int index, boolean collapsed) {
        Position position = ROCKSLIDE_POSITIONS[index];
        DynamicObject existing = ObjectManager.findDynamicObjectAt(
                position.getX(), position.getY(), position.getPlane());
        if (rockslideCollapsed[index] == collapsed && existing == null) {
            return;
        }
        if (existing != null) {
            ObjectManager.getInstance().removeDynamicObjectAt(
                    position.getX(), position.getY(), position.getPlane(), 10);
        }

        if (collapsed) {
            WalkingCollisionMap.addObjectCollision(COLLAPSED_ROCK_OBJECT_ID,
                    position.getX(), position.getY(), 0, 0, 10, false);
            ProjectileCollisionMap.addObjectCollision(COLLAPSED_ROCK_OBJECT_ID,
                    position.getX(), position.getY(), 0, 0, 10, false);
        } else {
            new DynamicObject(CLEARED_ROCK_OBJECT_ID, position.getX(), position.getY(), 0,
                    0, 10, COLLAPSED_ROCK_OBJECT_ID, 99999, false);
            WalkingCollisionMap.addObjectCollision(CLEARED_ROCK_OBJECT_ID,
                    position.getX(), position.getY(), 0, 0, 10, false);
            ProjectileCollisionMap.addObjectCollision(CLEARED_ROCK_OBJECT_ID,
                    position.getX(), position.getY(), 0, 0, 10, false);
        }
        if (rockslideCollapsed[index] != collapsed) {
            ++rockslideRevisions[index];
        }
        rockslideCollapsed[index] = collapsed;
    }

    private static void crushPlayersAtRockslide(Position position) {
        for (Player target : World.getPlayers()) {
            if (target == null || target.isDead() || !CastleWarsManager.isInGame(target)) {
                continue;
            }
            if (target.getPosition().getPlane() != 0
                    || GameUtil.getDistance(target.getPosition(), position) > 1) {
                continue;
            }
            target.applyDirectHit(target.getCurrentHitpoints(), HitType.NORMAL);
        }
    }

    private static Position chooseCatapultTarget(CastleWarsManager.Team firingTeam) {
        Player best = null;
        int bestNearbyEnemies = -1;
        for (Player candidate : World.getPlayers()) {
            if (candidate == null || candidate.isDead()
                    || CastleWarsManager.getGameTeam(candidate) == null
                    || CastleWarsManager.getGameTeam(candidate) == firingTeam
                    || candidate.getPosition().getPlane() != 0
                    || candidate.getPosition().getY() >= 9000) {
                continue;
            }

            int nearby = 0;
            for (Player other : World.getPlayers()) {
                if (other == null || other.isDead()
                        || CastleWarsManager.getGameTeam(other) == null
                        || CastleWarsManager.getGameTeam(other) == firingTeam
                        || other.getPosition().getPlane() != 0) {
                    continue;
                }
                if (GameUtil.getDistance(candidate.getPosition(), other.getPosition()) <= 3) {
                    ++nearby;
                }
            }
            if (nearby > bestNearbyEnemies) {
                bestNearbyEnemies = nearby;
                best = candidate;
            }
        }

        if (best != null) {
            return new Position(best.getPosition().getX() - 1 + GameUtil.randomInt(3),
                    best.getPosition().getY() - 1 + GameUtil.randomInt(3), 0);
        }

        int[][] fallback = firingTeam == CastleWarsManager.Team.SARADOMIN
                ? new int[][]{{2381,3104},{2385,3110},{2394,3119},{2380,3111},{2400,3125}}
                : new int[][]{{2417,3103},{2409,3093},{2402,3086},{2397,3079},{2420,3096}};
        int[] point = fallback[GameUtil.randomInt(fallback.length)];
        return new Position(point[0], point[1], 0);
    }

    private static void damageCatapultArea(Position target) {
        for (Player player : World.getPlayers()) {
            if (player == null || player.isDead() || !CastleWarsManager.isInGame(player)
                    || player.getPosition().getPlane() != 0) {
                continue;
            }
            if (Math.abs(player.getPosition().getX() - target.getX()) <= 2
                    && Math.abs(player.getPosition().getY() - target.getY()) <= 2) {
                player.applyDirectHit(5 + GameUtil.randomInt(11), HitType.NORMAL);
            }
        }
    }

    private static void clearAllBarricades() {
        ArrayList<BarricadeState> copy = new ArrayList<BarricadeState>(barricades.values());
        for (BarricadeState state : copy) {
            removeBarricade(state);
        }
        barricades.clear();
    }

    private static void removeBarricade(BarricadeState state) {
        ObjectManager.getInstance().removeDynamicObjectAt(
                state.position.getX(), state.position.getY(), state.position.getPlane(), 10);
        WalkingCollisionMap.removeObjectCollision(state.objectId,
                state.position.getX(), state.position.getY(), state.position.getPlane(), 0, 10);
        ProjectileCollisionMap.removeObjectCollision(state.objectId,
                state.position.getX(), state.position.getY(), state.position.getPlane(), 0, 10);
        barricades.remove(key(state.position));
    }

    private static String key(Position position) {
        return position.getX() + ":" + position.getY() + ":" + position.getPlane();
    }

    private static final class CatapultAim {
        private final CastleWarsManager.Team team;
        private int x;
        private int y;

        private CatapultAim(CastleWarsManager.Team team) {
            this.team = team;
            // Start in the middle of the 0..30 aiming range so every arrow
            // immediately has room to move in both directions.
            this.x = CATAPULT_MAX_AIM / 2;
            this.y = CATAPULT_MAX_AIM / 2;
        }
    }

    private static final class SideDoorState {
        private final CastleWarsManager.Team team;
        private final int closedId;
        private final int openId;
        private final int closedX;
        private final int closedY;
        private final int closedOrientation;
        private final int openX;
        private final int openY;
        private final int openOrientation;
        private boolean open;
        private boolean playerLocked;

        private SideDoorState(CastleWarsManager.Team team,
                              int closedId, int openId,
                              int closedX, int closedY, int closedOrientation,
                              int openX, int openY, int openOrientation) {
            this.team = team;
            this.closedId = closedId;
            this.openId = openId;
            this.closedX = closedX;
            this.closedY = closedY;
            this.closedOrientation = closedOrientation;
            this.openX = openX;
            this.openY = openY;
            this.openOrientation = openOrientation;
        }

        private boolean matches(int objectId, int x, int y) {
            return objectId == closedId && x == closedX && y == closedY
                    || objectId == openId && x == openX && y == openY;
        }
    }

    private enum MainDoorMode {
        CLOSED,
        OPEN,
        BROKEN
    }

    private static final class MainDoorLeaf {
        private final int closedId;
        private final int openId;
        private final int brokenId;
        private final int closedX;
        private final int closedY;
        private final int closedOrientation;
        private final int openX;
        private final int openY;
        private final int openOrientation;

        private MainDoorLeaf(int closedId, int openId, int brokenId,
                             int closedX, int closedY, int closedOrientation,
                             int openX, int openY, int openOrientation) {
            this.closedId = closedId;
            this.openId = openId;
            this.brokenId = brokenId;
            this.closedX = closedX;
            this.closedY = closedY;
            this.closedOrientation = closedOrientation;
            this.openX = openX;
            this.openY = openY;
            this.openOrientation = openOrientation;
        }

        private boolean matches(int objectId, int x, int y) {
            if (objectId == closedId) {
                return x == closedX && y == closedY;
            }
            return (objectId == openId || objectId == brokenId)
                    && x == openX && y == openY;
        }
    }

    private static final class MainDoorState {
        private final CastleWarsManager.Team team;
        private final MainDoorLeaf[] leaves;
        private int hitpoints = MAIN_DOOR_MAX_HITPOINTS;
        private MainDoorMode mode = MainDoorMode.CLOSED;

        private MainDoorState(CastleWarsManager.Team team,
                              MainDoorLeaf first, MainDoorLeaf second) {
            this.team = team;
            this.leaves = new MainDoorLeaf[]{first, second};
        }

        private boolean matches(int objectId, int x, int y) {
            for (MainDoorLeaf leaf : leaves) {
                if (leaf.matches(objectId, x, y)) {
                    return true;
                }
            }
            return false;
        }
    }

    private static final class ClimbingRopeState {
        private final Position position;
        private final Position objectPosition;
        private final Position destination;

        private ClimbingRopeState(Position position, Position objectPosition,
                                  Position destination) {
            this.position = position.copy();
            this.objectPosition = objectPosition.copy();
            this.destination = destination.copy();
        }
    }

    static final class BarricadeState {
        private final CastleWarsManager.Team team;
        private final Position position;
        private final int objectId;
        private int hitpoints = BARRICADE_MAX_HITPOINTS;
        private boolean burning;
        private long burnExpiresAt;

        BarricadeState(CastleWarsManager.Team team, Position position, int objectId) {
            this.team = team;
            this.position = position.copy();
            this.objectId = objectId;
        }

        int getHitpoints() {
            return hitpoints;
        }

        int takeDamage(int damage) {
            if (damage > 0) {
                hitpoints = Math.max(0, hitpoints - damage);
            }
            return hitpoints;
        }
    }
}
