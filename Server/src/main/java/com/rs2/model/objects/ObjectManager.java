package com.rs2.model.objects;

import com.rs2.ServerSettings;
import com.rs2.model.Entity;
import com.rs2.model.Position;
import com.rs2.model.World;
import com.rs2.model.gameplay.magetrainingarena.CreatureGraveyardController;
import com.rs2.model.gameplay.partyroom.PartyRoomManager;
import com.rs2.model.ground.GroundItem;
import com.rs2.model.ground.GroundItemManager;
import com.rs2.model.item.ItemStack;
import com.rs2.model.objects.DynamicObject;
import com.rs2.model.objects.functions.DoorHandler;
import com.rs2.model.objects.functions.DoubleDoorHandler;
import com.rs2.model.player.Player;
import com.rs2.model.skill.SkillActionHelper;
import com.rs2.model.skill.farming.MithrilSeedFlowerHandler;
import com.rs2.model.skill.firemaking.FiremakingHandler;
import com.rs2.util.GameUtil;
import com.rs2.util.ProfilerRegistry;
import com.rs2.util.ProfilerTimer;
import com.rs2.util.path.ProjectileCollisionMap;
import com.rs2.util.path.WalkingCollisionMap;
import java.util.ArrayList;

public final class ObjectManager {
    public static ArrayList activeDynamicObjects = new ArrayList();
    private static ArrayList pendingRemovalObjects = new ArrayList();
    private static ObjectManager instance;
    private static int[][] objectTraversalMappings;

    static {
        int[][] nArrayArray = new int[12][];
        nArrayArray[0] = new int[]{4415, 2425, 3074, 3, 2426, 3074, 2425, 3077, 2};
        nArrayArray[1] = new int[]{4417, 2425, 3074, 2, 2425, 3077, 2426, 3074, 3};
        nArrayArray[2] = new int[]{4415, 2430, 3081, 2, 2430, 3080, 2427, 3081, 1};
        nArrayArray[3] = new int[]{4417, 2428, 3081, 1, 2427, 3081, 2430, 3080, 2};
        int[] traversal4 = new int[9];
        traversal4[0] = 4415;
        traversal4[1] = 2419;
        traversal4[2] = 3080;
        traversal4[3] = 1;
        traversal4[4] = 2420;
        traversal4[5] = 3080;
        traversal4[6] = 2419;
        traversal4[7] = 3077;
        nArrayArray[4] = traversal4;
        int[] traversal5 = new int[9];
        traversal5[0] = 4417;
        traversal5[1] = 2419;
        traversal5[2] = 3078;
        traversal5[4] = 2419;
        traversal5[5] = 3077;
        traversal5[6] = 2420;
        traversal5[7] = 3080;
        traversal5[8] = 1;
        nArrayArray[5] = traversal5;
        int[] traversal6 = new int[9];
        traversal6[0] = 4418;
        traversal6[1] = 2380;
        traversal6[2] = 3127;
        traversal6[4] = 2380;
        traversal6[5] = 3130;
        traversal6[6] = 2379;
        traversal6[7] = 3127;
        traversal6[8] = 1;
        nArrayArray[6] = traversal6;
        int[] traversal7 = new int[9];
        traversal7[0] = 4415;
        traversal7[1] = 2380;
        traversal7[2] = 3127;
        traversal7[3] = 1;
        traversal7[4] = 2379;
        traversal7[5] = 3127;
        traversal7[6] = 2380;
        traversal7[7] = 3130;
        nArrayArray[7] = traversal7;
        nArrayArray[8] = new int[]{4418, 2369, 3126, 1, 2372, 3126, 2369, 3127, 2};
        nArrayArray[9] = new int[]{4415, 2369, 3126, 2, 2369, 3127, 2372, 3126, 1};
        nArrayArray[10] = new int[]{4418, 2374, 3131, 2, 2374, 3130, 2373, 3133, 3};
        nArrayArray[11] = new int[]{4415, 2374, 3133, 3, 2373, 3133, 2374, 3130, 2};
        objectTraversalMappings = nArrayArray;
    }

    public ObjectManager() {
        int[] integerValues = new int[]{14829, 14830, 14827, 14828, 14826, 14831};
        int[][] nArrayArray = new int[][]{{3154, 3618}, {3225, 3665}, {3033, 3730}, {3104, 3792}, {2978, 3864}, {3305, 3914}};
    }

    public static ObjectManager getInstance() {
        if (instance == null) {
            instance = new ObjectManager();
        }
        return instance;
    }

    public final void processObjects() {
        ArrayList<DynamicObject> arrayList = new ArrayList<DynamicObject>();
        ProfilerTimer profilerTimer = ProfilerRegistry.getTimer("tickObjects");
        profilerTimer.start();
        for (Object dynamicObjectObject : activeDynamicObjects) {
            DynamicObject dynamicObject = (DynamicObject)dynamicObjectObject;
            if (dynamicObject.remainingTicks > 0 && dynamicObject.remainingTicks < 99999) {
                --dynamicObject.remainingTicks;
                continue;
            }
            if (dynamicObject.remainingTicks >= 99999) continue;
            pendingRemovalObjects.add(dynamicObject);
            this.revertDynamicObject(dynamicObject);
            if (!(dynamicObject.getWorldObject().getObjectId() <= 2078 && dynamicObject.getWorldObject().getObjectId() > 2073 || dynamicObject.getWorldObject().getObjectId() <= 1413 && dynamicObject.getWorldObject().getObjectId() > 1408) && (dynamicObject.getWorldObject().getObjectId() < 10725 || dynamicObject.getWorldObject().getObjectId() > 10727)) continue;
            arrayList.add(dynamicObject);
        }
        for (Object dynamicObjectObject : pendingRemovalObjects) {
            DynamicObject dynamicObject = (DynamicObject)dynamicObjectObject;
            activeDynamicObjects.remove(dynamicObject);
            if (dynamicObject.getWorldObject().getObjectId() < 115 || dynamicObject.getWorldObject().getObjectId() > 122) continue;
            PartyRoomManager.activeBalloonObjects.remove(dynamicObject);
        }
        for (DynamicObject dynamicObject : arrayList) {
            if (CreatureGraveyardController.advanceBonePileRespawnStage(dynamicObject)) continue;
            int worldObject = dynamicObject.getWorldObject().getPosition().getX();
            int worldObject2 = dynamicObject.getWorldObject().getPosition().getY();
            int worldObject3 = dynamicObject.getWorldObject().getPosition().getPlane();
            int worldObject4 = dynamicObject.getWorldObject().getOrientation();
            int worldObject5 = dynamicObject.getWorldObject().getType();
            new DynamicObject(dynamicObject.getWorldObject().getObjectId() - 1, worldObject, worldObject2, worldObject3, worldObject4, worldObject5, dynamicObject.getWorldObject().getObjectId() - 1, 10);
        }
        pendingRemovalObjects.clear();
        arrayList.clear();
        profilerTimer.stop();
    }

    public final void revertDynamicObject(DynamicObject dynamicObject) {
        Object value;
        if (dynamicObject.getWorldObject().getObjectId() == 734) {
            ObjectManager.restoreObjectCollision(dynamicObject.restoreObjectId, dynamicObject.getWorldObject().getPosition().getX(), dynamicObject.getWorldObject().getPosition().getY(), dynamicObject.getWorldObject().getPosition().getPlane(), dynamicObject.getWorldObject().getOrientation(), dynamicObject.getWorldObject().getType());
        } else if (!(!dynamicObject.updatesCollision || dynamicObject.getWorldObject().getObjectId() <= 0 || dynamicObject.getWorldObject().getObjectId() == ServerSettings.placeholderObjectId || FiremakingHandler.isFireObjectId(dynamicObject.getWorldObject().getObjectId()) || MithrilSeedFlowerHandler.isMithrilSeedFlowerObjectId(dynamicObject.getWorldObject().getObjectId()) || DoorHandler.hasDoorAt(dynamicObject.getWorldObject().getObjectId(), dynamicObject.getWorldObject().getPosition().getX(), dynamicObject.getWorldObject().getPosition().getY(), dynamicObject.getWorldObject().getPosition().getPlane()) || DoubleDoorHandler.hasDoubleDoorAt(dynamicObject.getWorldObject().getPosition().getX(), dynamicObject.getWorldObject().getPosition().getY(), dynamicObject.getWorldObject().getPosition().getPlane()))) {
            ObjectManager.restoreObjectCollision(dynamicObject.restoreObjectId, dynamicObject.getWorldObject().getPosition().getX(), dynamicObject.getWorldObject().getPosition().getY(), dynamicObject.getWorldObject().getPosition().getPlane(), dynamicObject.getWorldObject().getOrientation(), dynamicObject.getWorldObject().getType());
        }
        if (FiremakingHandler.isFireObjectId(dynamicObject.getWorldObject().getObjectId())) {
            value = new GroundItem(new ItemStack(592), new Position(dynamicObject.getWorldObject().getPosition().getX(), dynamicObject.getWorldObject().getPosition().getY(), dynamicObject.getWorldObject().getPosition().getPlane()), false, dynamicObject.owner);
            GroundItemManager.getInstance().spawn((GroundItem)value);
        }
        Player[] playerArray = World.getPlayers();
        int length = playerArray.length;
        int index = 0;
        while (index < length) {
            value = playerArray[index];
            if (value != null && shouldSendDynamicObjectRestore(
                    ((Player)value).getPosition(),
                    dynamicObject.getWorldObject().getPosition(),
                    ((Player)value).visibleDynamicObjects.contains(dynamicObject))) {
                // A player who saw this object may still have its old model in
                // their loaded scene after walking beyond the normal 60-tile
                // visibility radius. Restore it without requiring a death or
                // region rebuild (important for returned Castle Wars flags).
                ((Player)value).packetSender.sendObjectCreate(dynamicObject.restoreObjectId, dynamicObject.getWorldObject().getPosition().getX(), dynamicObject.getWorldObject().getPosition().getY(), dynamicObject.getWorldObject().getPosition().getPlane(), dynamicObject.orientation, dynamicObject.getWorldObject().getType());
            }
            ++index;
        }
    }

    /** Previously streamed objects must be restored even outside spawn radius.
     * The packet writer will ignore objects outside the client's loaded scene. */
    public static boolean shouldSendDynamicObjectRestore(
            Position playerPosition, Position objectPosition, boolean previouslyVisible) {
        if (playerPosition == null || objectPosition == null
                || playerPosition.getPlane() != objectPosition.getPlane()) {
            return false;
        }
        return previouslyVisible || GameUtil.isWithinDistance(
                objectPosition.getX(), objectPosition.getY(),
                playerPosition.getX(), playerPosition.getY(), 60);
    }

    public static DynamicObject findDynamicObjectAt(int objectId, int value2, int value32) {
        for (Object dynamicObjectObject : activeDynamicObjects) {
            DynamicObject dynamicObject = (DynamicObject)dynamicObjectObject;
            if (dynamicObject.getWorldObject().getPosition().getX() != objectId || dynamicObject.getWorldObject().getPosition().getY() != value2 || dynamicObject.getWorldObject().getPosition().getPlane() != value32) continue;
            return dynamicObject;
        }
        return null;
    }

    public static DynamicObject findDynamicObjectByIdAt(int objectId, int value2, int value32, int value42) {
        for (Object dynamicObjectObject : activeDynamicObjects) {
            DynamicObject dynamicObject = (DynamicObject)dynamicObjectObject;
            if (dynamicObject.getWorldObject().getObjectId() != objectId || dynamicObject.getWorldObject().getPosition().getX() != value2 || dynamicObject.getWorldObject().getPosition().getY() != value32 || dynamicObject.getWorldObject().getPosition().getPlane() != value42) continue;
            return dynamicObject;
        }
        return null;
    }

    public final void removeDynamicObjectAt(int objectId, int value2, int value32, int value42) {
        DynamicObject dynamicObject = ObjectManager.findDynamicObjectAt(objectId, value2, value32);
        if (dynamicObject != null) {
            if (dynamicObject.updatesCollision) {
                ObjectManager.removeObjectCollision(
                        dynamicObject.getWorldObject().getObjectId(),
                        objectId, value2, value32,
                        dynamicObject.getWorldObject().getOrientation(),
                        dynamicObject.getWorldObject().getType());
            }
            if (dynamicObject.getWorldObject().getObjectId() >= 115 && dynamicObject.getWorldObject().getObjectId() <= 122) {
                PartyRoomManager.activeBalloonObjects.remove(dynamicObject);
            }
            activeDynamicObjects.remove(dynamicObject);
            this.revertDynamicObject(dynamicObject);
        }
    }

    public final void refreshDynamicObjectsForPlayer(Player player) {
        if (player == null) {
            return;
        }
        for (Object visibleDynamicObjectObject : player.visibleDynamicObjects) {
            DynamicObject dynamicObject = (DynamicObject)visibleDynamicObjectObject;
            if (activeDynamicObjects.size() == 0) {
                if (!ObjectManager.isVisibleToPlayer(dynamicObject, player)) continue;
                player.pendingDynamicObjectRemovals.add(dynamicObject);
                Player player2 = player;
                player2.packetSender.sendObjectCreate(dynamicObject.restoreObjectId, dynamicObject.getWorldObject().getPosition().getX(), dynamicObject.getWorldObject().getPosition().getY(), dynamicObject.getWorldObject().getPosition().getPlane(), dynamicObject.orientation, dynamicObject.getWorldObject().getType());
                continue;
            }
            boolean enabled = false;
            for (Object dynamicObject2Object : activeDynamicObjects) {
                DynamicObject dynamicObject2 = (DynamicObject)dynamicObject2Object;
                if (dynamicObject != dynamicObject2) continue;
                enabled = true;
                break;
            }
            if (enabled || !ObjectManager.isVisibleToPlayer(dynamicObject, player)) continue;
            player.pendingDynamicObjectRemovals.add(dynamicObject);
            Player player3 = player;
            player3.packetSender.sendObjectCreate(dynamicObject.restoreObjectId, dynamicObject.getWorldObject().getPosition().getX(), dynamicObject.getWorldObject().getPosition().getY(), dynamicObject.getWorldObject().getPosition().getPlane(), dynamicObject.orientation, dynamicObject.getWorldObject().getType());
        }
        for (Object pendingDynamicObjectObject : player.pendingDynamicObjectRemovals) {
            DynamicObject dynamicObject = (DynamicObject)pendingDynamicObjectObject;
            player.visibleDynamicObjects.remove(dynamicObject);
        }
        player.pendingDynamicObjectRemovals.clear();
        for (Object dynamicObjectObject : activeDynamicObjects) {
            DynamicObject dynamicObject = (DynamicObject)dynamicObjectObject;
            if (!ObjectManager.isVisibleToPlayer(dynamicObject, player)) continue;
            boolean enabled2 = false;
            for (Object dynamicObject2Object : player.visibleDynamicObjects) {
                DynamicObject dynamicObject2 = (DynamicObject)dynamicObject2Object;
                if (dynamicObject2 != dynamicObject) continue;
                enabled2 = true;
                break;
            }
            if (!enabled2) {
                player.visibleDynamicObjects.add(dynamicObject);
            }
            Player player4 = player;
            player4.packetSender.sendObjectCreate(com.rs2.model.quest.event.GublinchChristmasEvent.cageDisplayId(player4, dynamicObject.getWorldObject().getObjectId(), dynamicObject.getWorldObject().getPosition().getX(), dynamicObject.getWorldObject().getPosition().getY(), dynamicObject.getWorldObject().getPosition().getPlane()), dynamicObject.getWorldObject().getPosition().getX(), dynamicObject.getWorldObject().getPosition().getY(), dynamicObject.getWorldObject().getPosition().getPlane(), dynamicObject.getWorldObject().getOrientation(), dynamicObject.getWorldObject().getType());
        }
    }

    public static void removeWallObjectCollision(int x, int y, int plane, int wallType) {
        WalkingCollisionMap.removeStraightWallCollision(x, y, plane, wallType);
        ProjectileCollisionMap.removeWallCollision(x, y, plane, 0, wallType, true);
    }

    private static void restoreObjectCollision(int x, int y, int plane, int collisionFlag, int orientation, int value62) {
        WalkingCollisionMap.addObjectCollision(x, y, plane, collisionFlag, orientation, value62, false);
        ProjectileCollisionMap.addObjectCollision(x, y, plane, collisionFlag, orientation, value62, false);
    }

    public static void removeTypeNineObjectCollision(int x, int y, int plane, int collisionFlag, int orientation) {
        WalkingCollisionMap.removeObjectCollision(1, x, y, plane, orientation, 9);
        ProjectileCollisionMap.removeObjectCollision(1, x, y, plane, orientation, 9);
    }

    public static void removeObjectCollision(int x, int y, int plane, int collisionFlag, int orientation, int value62) {
        WalkingCollisionMap.removeObjectCollision(x, y, plane, collisionFlag, value62, orientation);
        ProjectileCollisionMap.removeObjectCollision(x, y, plane, collisionFlag, value62, orientation);
    }

    private static boolean isVisibleToPlayer(DynamicObject dynamicObject, Player player) {
        if (dynamicObject == null || player == null) {
            return false;
        }
        return player.getPosition().getPlane() == dynamicObject.getWorldObject().getPosition().getPlane() && GameUtil.isWithinDistance(dynamicObject.getWorldObject().getPosition().getX(), dynamicObject.getWorldObject().getPosition().getY(), player.getPosition().getX(), player.getPosition().getY(), 60);
    }

    public final void addDynamicObject(DynamicObject dynamicObject, boolean updateCollision) {
        if (ObjectManager.findDynamicObjectAt(dynamicObject.getWorldObject().getPosition().getX(), dynamicObject.getWorldObject().getPosition().getY(), dynamicObject.getWorldObject().getPosition().getPlane()) == null) {
            activeDynamicObjects.add(dynamicObject);
            if (updateCollision) {
                if (dynamicObject.getWorldObject().getObjectId() > 0 && dynamicObject.getWorldObject().getObjectId() != ServerSettings.placeholderObjectId && !FiremakingHandler.isFireObjectId(dynamicObject.getWorldObject().getObjectId()) && !MithrilSeedFlowerHandler.isMithrilSeedFlowerObjectId(dynamicObject.getWorldObject().getObjectId())) {
                    ObjectManager.restoreObjectCollision(dynamicObject.restoreObjectId, dynamicObject.getWorldObject().getPosition().getX(), dynamicObject.getWorldObject().getPosition().getY(), dynamicObject.getWorldObject().getPosition().getPlane(), dynamicObject.getWorldObject().getOrientation(), dynamicObject.getWorldObject().getType());
                } else {
                    ObjectManager.removeObjectCollision(dynamicObject.restoreObjectId, dynamicObject.getWorldObject().getPosition().getX(), dynamicObject.getWorldObject().getPosition().getY(), dynamicObject.getWorldObject().getPosition().getPlane(), dynamicObject.getWorldObject().getOrientation(), dynamicObject.getWorldObject().getType());
                }
            }
            Player[] playerArray = World.getPlayers();
            int length = playerArray.length;
            int index = 0;
            while (index < length) {
                Player player = playerArray[index];
                if (player != null && player.getPosition().getPlane() == dynamicObject.getWorldObject().getPosition().getPlane() && GameUtil.isWithinDistance(dynamicObject.getWorldObject().getPosition().getX(), dynamicObject.getWorldObject().getPosition().getY(), player.getPosition().getX(), player.getPosition().getY(), 60)) {
                    player.packetSender.sendObjectCreate(com.rs2.model.quest.event.GublinchChristmasEvent.cageDisplayId(player, dynamicObject.getWorldObject().getObjectId(), dynamicObject.getWorldObject().getPosition().getX(), dynamicObject.getWorldObject().getPosition().getY(), dynamicObject.getWorldObject().getPosition().getPlane()), dynamicObject.getWorldObject().getPosition().getX(), dynamicObject.getWorldObject().getPosition().getY(), dynamicObject.getWorldObject().getPosition().getPlane(), dynamicObject.getWorldObject().getOrientation(), dynamicObject.getWorldObject().getType());
                    player.visibleDynamicObjects.add(dynamicObject);
                }
                ++index;
            }
        }
    }

    public static void prepareObjectInteractionMovement(Player player, int objectId, int value2, int value32) {
        player.interactionObjectSizeY = 0;
        player.interactionObjectSizeX = 0;
        player.interactionApproachY = 0;
        player.interactionApproachX = 0;
        player.interactionOffsetX = 0;
        player.interactionOffsetY = 0;
        player.interactionExitX = 0;
        player.interactionExitY = 0;
        player.interactionExitPlane = 0;
        switch (objectId) {
            case 4493: 
            case 4494: 
            case 4495: 
            case 4496: {
                player.interactionObjectSizeX = 2;
                player.interactionObjectSizeY = 4;
                return;
            }
            case 1722: {
                if (value2 == 3099 && value32 == 3266 && player.getPosition().getPlane() == 0) {
                    player.interactionApproachX = 3098;
                    player.interactionApproachY = Math.max(3266, Math.min(3267, player.getPosition().getY()));
                    return;
                }
                player.interactionObjectSizeX = 2;
                player.interactionObjectSizeY = 3;
                return;
            }
            case 1723: {
                player.interactionObjectSizeX = 2;
                player.interactionObjectSizeY = 3;
                return;
            }
            case 1747: 
            case 1750: 
            case 1755: 
            case 2148: 
            case 2609: 
            case 8744: 
            case 9319: {
                // The Wizards' Tower basement ladder faces west into a blocked
                // wall. Players approach it from the east, where the descent
                // from the ground floor also lands.
                if (objectId == 2148 && value2 == 3103 && value32 == 9576
                        && player.getPosition().getPlane() == 0) {
                    player.interactionApproachX = 3104;
                    player.interactionApproachY = 9576;
                    return;
                }
                int objectOrientation = SkillActionHelper.getObjectOrientation(objectId, value2, value32, 0);
                int dx = objectOrientation == 1 ? 1 : (objectOrientation == 3 ? -1 : 0);
                int dy = objectOrientation == 0 ? 1 : (objectOrientation == 2 ? -1 : 0);
                // Some ladders are climbed from the other side, e.g. the Paterdomus mausoleum ladder (3405,9907),
                // whose north tile is solid wall. Use the side the player can actually stand on.
                if (!com.rs2.util.path.PathFinder.isReachable(player, value2 + dx, value32 + dy)
                        && com.rs2.util.path.PathFinder.isReachable(player, value2 - dx, value32 - dy)) {
                    dx = -dx;
                    dy = -dy;
                }
                player.interactionApproachX = value2 + dx;
                player.interactionApproachY = value32 + dy;
                return;
            }
            case 3014: {
                if (value2 == 3098 && value32 == 3107) {
                    // Tutorial Island's first door is entered from the west.
                    // Pin the interaction tile so the scripted eastward step
                    // cannot start from the north/south side of the wall.
                    player.interactionApproachX = 3097;
                    player.interactionApproachY = 3107;
                    return;
                }
                break;
            }
            case 3017: {
                if (value2 == 3079 && value32 == 3084) {
                    // The door is on the west edge of this tile. Walk onto it
                    // before the scripted westward step into the kitchen.
                    player.interactionApproachX = 3079;
                    player.interactionApproachY = 3084;
                    return;
                }
                break;
            }
            case 3019: {
                if (value2 == 3086 && value32 == 3126) {
                    player.interactionApproachX = 3086;
                    player.interactionApproachY = 3126;
                    return;
                }
                break;
            }
            case 3566: {
                player.interactionApproachX = player.getPosition().getX() > 2768 ? 2769 : 2764;
                player.interactionApproachY = player.getPosition().getX() > 2768 ? 9567 : 9569;
                return;
            }
            case 2640: {
                player.interactionObjectSizeX = 1;
                player.interactionObjectSizeY = 2;
                return;
            }
            case 3044: {
                player.interactionObjectSizeX = 5;
                player.interactionObjectSizeY = 5;
                return;
            }
            case 2309: {
                player.interactionApproachX = 2998;
                player.interactionApproachY = 3916;
                return;
            }
            case 2307: 
            case 2308: {
                player.interactionApproachX = 2998;
                player.interactionApproachY = 3931;
                return;
            }
            case 2288: {
                player.interactionApproachX = 3004;
                player.interactionApproachY = 3937;
                return;
            }
            case 2283: {
                player.interactionApproachX = 3005;
                player.interactionApproachY = 3953;
                player.interactionOffsetY = 2;
                return;
            }
            case 2311: {
                player.interactionApproachX = 3002;
                player.interactionApproachY = 3960;
                return;
            }
            case 2297: {
                player.interactionApproachX = 3002;
                player.interactionApproachY = 3945;
                return;
            }
            case 2295: {
                player.interactionApproachX = 2474;
                player.interactionApproachY = 3436;
                return;
            }
            case 2285: {
                player.interactionApproachX = value2;
                player.interactionApproachY = 3426;
                return;
            }
            case 2313: {
                player.interactionApproachX = 2473;
                player.interactionApproachY = 3423;
                return;
            }
            case 2312: {
                player.interactionApproachX = 2477;
                player.interactionApproachY = 3420;
                return;
            }
            case 2314: 
            case 2315: {
                player.interactionApproachX = 2486;
                player.interactionApproachY = 3420;
                return;
            }
            case 2286: {
                player.interactionApproachX = value2;
                player.interactionApproachY = 3425;
                return;
            }
            case 154: {
                player.interactionApproachX = 2484;
                player.interactionApproachY = 3430;
                return;
            }
            case 4058: {
                player.interactionApproachX = 2487;
                player.interactionApproachY = 3430;
                return;
            }
            case 1815: 
            case 1816: 
            case 5959: 
            case 5960: {
                player.interactionObjectSizeY = 0;
                player.interactionObjectSizeX = 0;
                break;
            }
            case 4467: 
            case 4468: 
            case 8959: {
                player.interactionOffsetY = 1;
                break;
            }
            case 4465: 
            case 4466: {
                player.interactionOffsetY = -1;
                break;
            }
            case 4381: {
                player.interactionOffsetY = 2;
                break;
            }
            case 4382: {
                player.interactionOffsetY = -2;
                break;
            }
            case 4419: {
                if (player.isInCastleWarsObstacleArea(player.getPosition().getX(), player.getPosition().getY())) {
                    player.interactionApproachX = 2416;
                    player.interactionApproachY = 3074;
                    player.interactionExitX = 2417;
                    player.interactionExitY = 3077;
                } else {
                    player.interactionApproachX = 2417;
                    player.interactionApproachY = 3077;
                    player.interactionExitX = 2416;
                    player.interactionExitY = 3074;
                }
                player.interactionExitPlane = 0;
                return;
            }
            case 4420: {
                if (player.isInCastleWarsObstacleArea(player.getPosition().getX(), player.getPosition().getY())) {
                    player.interactionApproachX = 2383;
                    player.interactionApproachY = 3133;
                    player.interactionExitX = 2382;
                    player.interactionExitY = 3130;
                } else {
                    player.interactionApproachX = 2382;
                    player.interactionApproachY = 3130;
                    player.interactionExitX = 2383;
                    player.interactionExitY = 3133;
                }
                player.interactionExitPlane = 0;
                return;
            }
            case 2558: {
                player.getPosition().getX();
                if (player.getPosition().getY() > 0) {
                    player.interactionOffsetY = 1;
                }
                player.getPosition().getX();
                player.interactionObjectSizeY = 0;
                player.interactionObjectSizeX = 0;
                break;
            }
            case 4031: 
            case 6706: {
                player.interactionOffsetX = 2;
                break;
            }
            case 6707: {
                player.interactionOffsetY = 3;
                break;
            }
            case 6823: {
                player.interactionOffsetY = 1;
                break;
            }
            case 6772: {
                player.interactionOffsetY = 1;
                break;
            }
            case 6705: {
                player.interactionOffsetY = -1;
                break;
            }
            case 6822: {
                player.interactionOffsetY = 1;
                break;
            }
            case 6704: {
                player.interactionOffsetY = -1;
                break;
            }
            case 6773: {
                player.interactionOffsetX = 1;
                player.interactionOffsetY = 1;
                break;
            }
            case 6703: {
                player.interactionOffsetX = -1;
                break;
            }
            case 6771: {
                player.interactionOffsetX = 1;
                player.interactionOffsetY = 1;
                break;
            }
            case 6702: {
                player.interactionOffsetX = -1;
                break;
            }
            case 6821: {
                player.interactionOffsetX = 1;
                player.interactionOffsetY = 1;
            }
        }
        int[][] integerValues = objectTraversalMappings;
        value2 = 0;
        while (value2 < 12) {
            int[] integerValues2 = integerValues[value2];
            if (objectId == integerValues2[0] && 0 == integerValues2[1] && 0 == integerValues2[2] && player.getPosition().getPlane() == integerValues2[3]) {
                player.interactionApproachX = integerValues2[4];
                player.interactionApproachY = integerValues2[5];
                player.interactionExitX = integerValues2[6];
                player.interactionExitY = integerValues2[7];
                player.interactionExitPlane = integerValues2[8];
                return;
            }
            ++value2;
        }
    }
}
