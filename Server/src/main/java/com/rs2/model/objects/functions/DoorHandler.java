package com.rs2.model.objects.functions;

import com.rs2.ServerSettings;
import com.rs2.model.World;
import com.rs2.model.objects.DynamicObject;
import com.rs2.model.objects.LoadedWorldObject;
import com.rs2.model.objects.ObjectDefinition;
import com.rs2.model.objects.ObjectManager;
import com.rs2.model.objects.WorldObjectLookup;
import com.rs2.model.objects.functions.DoorResetEvent;
import com.rs2.model.player.Player;
import com.rs2.model.skill.SkillActionHelper;
import com.rs2.model.task.CycleEventHandler;
import java.util.ArrayList;
import java.util.List;

public final class DoorHandler {
    private static List doorStates = new ArrayList();
    private boolean open;
    private long lastInteractionMillis;
    private int rapidToggleCount;
    private int currentObjectId;
    private int originalObjectId;
    private int currentX;
    private int currentY;
    private int originalX;
    private int originalY;
    private int plane;
    private int originalOrientation;
    private int currentOrientation;
    private int objectType;
    private boolean initiallyOpen;
    private static int[] initiallyOpenObjectIds = new int[]{1504, 1514, 1517, 1520, 1531, 1534, 2033, 2035, 2037, 2998, 3271, 4468, 4697, 6101, 6103, 6105, 6107, 6109, 6111, 6113, 6115, 6976, 6978, 8696, 8819, 10261, 10263, 10265, 11708, 11710, 11712, 11715, 11994, 12445, 13002};

    private DoorHandler(int currentObjectId, int currentX, int currentY, int plane) {
        int value5;
        this.currentObjectId = currentObjectId;
        this.originalObjectId = currentObjectId;
        this.currentX = currentX;
        this.currentY = currentY;
        this.originalX = currentX;
        this.originalY = currentY;
        this.plane = plane;
        this.originalOrientation = value5 = SkillActionHelper.getObjectOrientation(currentObjectId, currentX, currentY, plane);
        this.currentOrientation = value5;
        this.objectType = currentX = SkillActionHelper.getObjectType(currentObjectId, currentX, currentY, plane);
        this.open = this.initiallyOpen = DoorHandler.isInitiallyOpenObjectId(currentObjectId);
        this.lastInteractionMillis = 0L;
        this.rapidToggleCount = 0;
    }

    private static DoorHandler getOrCreateDoorState(int state, int value22, int value32, int value42) {
        for (Object doorStateObject : doorStates) {
            DoorHandler doorHandler2 = (DoorHandler)doorStateObject;
            if (doorHandler2.currentX != value22 || doorHandler2.currentY != value32 || doorHandler2.plane != value42) continue;
            return doorHandler2;
        }
        DoorHandler doorHandler2 = new DoorHandler(state, value22, value32, value42);
        doorStates.add(doorHandler2);
        return doorHandler2;
    }

    public static boolean hasDoorAt(int value5, int value22, int value32, int value42) {
        return DoorHandler.getOrCreateDoorState(value5, value22, value32, value42) != null;
    }

    private static boolean preservesStaticCollision(DoorHandler doorHandler) {
        return doorHandler.originalObjectId == 3017
                && doorHandler.originalX == 3079
                && doorHandler.originalY == 3084
                && doorHandler.plane == 0;
    }

    private static boolean isClosingOntoPlayer(DoorHandler door, Player player) {
        // Type 9 doors occupy a whole tile rather than a boundary between tiles.
        if (door.objectType != 9 || !door.open || door.initiallyOpen) {
            return false;
        }
        if (occupiesDoorTile(door, player)) {
            return true;
        }
        for (Player other : World.getPlayers()) {
            if (occupiesDoorTile(door, other)) {
                return true;
            }
        }
        return false;
    }

    private static boolean occupiesDoorTile(DoorHandler door, Player player) {
        return player != null && player.getPosition().getPlane() == door.plane
                && player.getPosition().getX() == door.originalX
                && player.getPosition().getY() == door.originalY;
    }

    public static boolean handleDoor(Player player, int value10, int value22, int value32, int value42) {
        Object value5;
        Object value6;
        Object value7;
        if (ObjectDefinition.forId(value10) != null) {
            value7 = ObjectDefinition.forId(value10);
            value6 = ((ObjectDefinition)value7).name.toLowerCase();
        } else {
            value6 = value5 = "";
        }
        if (!((String)value6).contains("fence") && !((String)value6).contains("gate") && !((String)value6).contains("door") || ((String)value6).contains("trapdoor") || ((String)value6).contains("tree")) {
            return false;
        }
        if (value10 == 9300 || value10 == 9299 || value10 == 883 || value10 == 1805 || value10 == 2882 || value10 == 2883 || value10 == 2623 || value10 == 2112 || value10 == 1804 || value10 == 2266 || value10 == 2406 || value10 == 2407 || value10 == 2631 || value10 == 2623 || value10 == 8958 || value10 == 8959 || value10 == 8960 || value10 == 1589 || value10 == 1590 || value10 == 2155 && value22 == 2592 && value32 == 9490 || value10 == 2154 && value22 == 2593 && value32 == 9490) {
            return false;
        }
        value5 = DoorHandler.getOrCreateDoorState(value10, value22, value32, value42);
        if (value5 == null) {
            return false;
        }
        if (isClosingOntoPlayer((DoorHandler)value5, player)) {
            player.packetSender.sendGameMessage("Someone is standing in the doorway.");
            return true;
        }
        boolean preserveStaticCollision = DoorHandler.preservesStaticCollision((DoorHandler)value5);
        if (((DoorHandler)value5).rapidToggleCount >= 5 && ((DoorHandler)value5).open) {
            if (System.currentTimeMillis() - ((DoorHandler)value5).lastInteractionMillis >= 10000L) {
                ((DoorHandler)value5).rapidToggleCount = 0;
            } else {
                value7 = player;
                ((Player)value7).packetSender.sendGameMessage("The door is stuck open.");
                return true;
            }
        }
        ObjectManager.getInstance();
        value7 = ObjectManager.findDynamicObjectByIdAt(value10, value22, value32, value42);
        if (!preserveStaticCollision && value7 != null) {
            if (((DoorHandler)value5).objectType == 9) {
                ObjectManager.getInstance();
                ObjectManager.removeTypeNineObjectCollision(value22, value32, value42, 9, ((DynamicObject)value7).getWorldObject().getOrientation());
            } else {
                ObjectManager.getInstance();
                ObjectManager.removeWallObjectCollision(value22, value32, value42, ((DynamicObject)value7).getWorldObject().getOrientation());
            }
        }
        if (!preserveStaticCollision && (value7 = WorldObjectLookup.findObjectByIdAt(value10, value22, value32, value42)) != null) {
            if (((DoorHandler)value5).objectType == 9) {
                ObjectManager.getInstance();
                ObjectManager.removeTypeNineObjectCollision(value22, value32, value42, 9, ((LoadedWorldObject)value7).getOrientation());
            } else {
                ObjectManager.getInstance();
                ObjectManager.removeWallObjectCollision(value22, value32, value42, ((LoadedWorldObject)value7).getOrientation());
            }
        }
        value22 = 0;
        value32 = 0;
        if (((DoorHandler)value5).objectType == 0) {
            if (!((DoorHandler)value5).initiallyOpen) {
                if (((DoorHandler)value5).originalOrientation == 0 && ((DoorHandler)value5).currentOrientation == 0) {
                    value22 = -1;
                } else if (((DoorHandler)value5).originalOrientation == 1 && ((DoorHandler)value5).currentOrientation == 1) {
                    value32 = 1;
                } else if (((DoorHandler)value5).originalOrientation == 2 && ((DoorHandler)value5).currentOrientation == 2) {
                    value22 = 1;
                } else if (((DoorHandler)value5).originalOrientation == 3 && ((DoorHandler)value5).currentOrientation == 3) {
                    value32 = -1;
                }
            } else if (((DoorHandler)value5).initiallyOpen) {
                if (((DoorHandler)value5).originalOrientation == 0 && ((DoorHandler)value5).currentOrientation == 0) {
                    value32 = 1;
                } else if (((DoorHandler)value5).originalOrientation == 1 && ((DoorHandler)value5).currentOrientation == 1) {
                    value22 = 1;
                } else if (((DoorHandler)value5).originalOrientation == 2 && ((DoorHandler)value5).currentOrientation == 2) {
                    value32 = -1;
                } else if (((DoorHandler)value5).originalOrientation == 3 && ((DoorHandler)value5).currentOrientation == 3) {
                    value22 = -1;
                }
            }
        } else if (((DoorHandler)value5).objectType == 9) {
            if (!((DoorHandler)value5).initiallyOpen) {
                if (((DoorHandler)value5).originalOrientation == 0 && ((DoorHandler)value5).currentOrientation == 0) {
                    value22 = 1;
                } else if (((DoorHandler)value5).originalOrientation == 1 && ((DoorHandler)value5).currentOrientation == 1) {
                    value22 = 1;
                } else if (((DoorHandler)value5).originalOrientation == 2 && ((DoorHandler)value5).currentOrientation == 2) {
                    value22 = -1;
                } else if (((DoorHandler)value5).originalOrientation == 3 && ((DoorHandler)value5).currentOrientation == 3) {
                    value22 = -1;
                }
            } else if (((DoorHandler)value5).initiallyOpen) {
                if (((DoorHandler)value5).originalOrientation == 0 && ((DoorHandler)value5).currentOrientation == 0) {
                    value22 = 1;
                } else if (((DoorHandler)value5).originalOrientation == 1 && ((DoorHandler)value5).currentOrientation == 1) {
                    value22 = 1;
                } else if (((DoorHandler)value5).originalOrientation == 2 && ((DoorHandler)value5).currentOrientation == 2) {
                    value22 = -1;
                } else if (((DoorHandler)value5).originalOrientation == 3 && ((DoorHandler)value5).currentOrientation == 3) {
                    value22 = -1;
                }
            }
        }
        if (value22 != 0 || value32 != 0) {
            ObjectManager.getInstance().removeDynamicObjectAt(((DoorHandler)value5).currentX, ((DoorHandler)value5).currentY, ((DoorHandler)value5).plane, 0);
            new DynamicObject(ServerSettings.placeholderObjectId, ((DoorHandler)value5).currentX, ((DoorHandler)value5).currentY, ((DoorHandler)value5).plane, 0, ((DoorHandler)value5).objectType, ServerSettings.placeholderObjectId, 999999999, !preserveStaticCollision);
        }
        if (((DoorHandler)value5).currentX == ((DoorHandler)value5).originalX && ((DoorHandler)value5).currentY == ((DoorHandler)value5).originalY) {
            ((DoorHandler)value5).currentX += value22;
            ((DoorHandler)value5).currentY += value32;
        } else {
            ObjectManager.getInstance().removeDynamicObjectAt(((DoorHandler)value5).currentX, ((DoorHandler)value5).currentY, ((DoorHandler)value5).plane, 0);
            new DynamicObject(ServerSettings.placeholderObjectId, ((DoorHandler)value5).currentX, ((DoorHandler)value5).currentY, ((DoorHandler)value5).plane, 0, ((DoorHandler)value5).objectType, ServerSettings.placeholderObjectId, 999999999, !preserveStaticCollision);
            ((DoorHandler)value5).currentX = ((DoorHandler)value5).originalX;
            ((DoorHandler)value5).currentY = ((DoorHandler)value5).originalY;
        }
        if (value10 == 22 || value10 == 2550 || value10 == 2551 || value10 == 2555 || value10 == 2556 || value10 == 2558 || value10 == 2557 || value10 == 3014 || value10 == 3017 || value10 == 3019) {
            ((DoorHandler)value5).currentObjectId = value10;
        } else if (((DoorHandler)value5).currentObjectId == ((DoorHandler)value5).originalObjectId) {
            if (!((DoorHandler)value5).initiallyOpen) {
                ++((DoorHandler)value5).currentObjectId;
            } else if (((DoorHandler)value5).initiallyOpen) {
                --((DoorHandler)value5).currentObjectId;
            }
        } else if (((DoorHandler)value5).currentObjectId != ((DoorHandler)value5).originalObjectId) {
            if (!((DoorHandler)value5).initiallyOpen) {
                --((DoorHandler)value5).currentObjectId;
            } else if (((DoorHandler)value5).initiallyOpen) {
                ++((DoorHandler)value5).currentObjectId;
            }
        }
        Object value8 = value5;
        value22 = ((DoorHandler)value8).originalOrientation;
        if (((DoorHandler)value8).objectType == 0) {
            if (!((DoorHandler)value8).initiallyOpen) {
                if (((DoorHandler)value8).originalOrientation == 0 && ((DoorHandler)value8).currentOrientation == 0) {
                    value22 = 1;
                } else if (((DoorHandler)value8).originalOrientation == 1 && ((DoorHandler)value8).currentOrientation == 1) {
                    value22 = 2;
                } else if (((DoorHandler)value8).originalOrientation == 2 && ((DoorHandler)value8).currentOrientation == 2) {
                    value22 = 3;
                } else if (((DoorHandler)value8).originalOrientation == 3 && ((DoorHandler)value8).currentOrientation == 3) {
                    value22 = 0;
                } else if (((DoorHandler)value8).originalOrientation != ((DoorHandler)value8).currentOrientation) {
                    value22 = ((DoorHandler)value8).originalOrientation;
                }
            } else if (((DoorHandler)value8).initiallyOpen) {
                if (((DoorHandler)value8).originalOrientation == 0 && ((DoorHandler)value8).currentOrientation == 0) {
                    value22 = 3;
                } else if (((DoorHandler)value8).originalOrientation == 1 && ((DoorHandler)value8).currentOrientation == 1) {
                    value22 = 0;
                } else if (((DoorHandler)value8).originalOrientation == 2 && ((DoorHandler)value8).currentOrientation == 2) {
                    value22 = 1;
                } else if (((DoorHandler)value8).originalOrientation == 3 && ((DoorHandler)value8).currentOrientation == 3) {
                    value22 = 2;
                } else if (((DoorHandler)value8).originalOrientation != ((DoorHandler)value8).currentOrientation) {
                    value22 = ((DoorHandler)value8).originalOrientation;
                }
            }
        } else if (((DoorHandler)value8).objectType == 9) {
            if (!((DoorHandler)value8).initiallyOpen) {
                if (((DoorHandler)value8).originalOrientation == 0 && ((DoorHandler)value8).currentOrientation == 0) {
                    value22 = 3;
                } else if (((DoorHandler)value8).originalOrientation == 1 && ((DoorHandler)value8).currentOrientation == 1) {
                    value22 = 2;
                } else if (((DoorHandler)value8).originalOrientation == 2 && ((DoorHandler)value8).currentOrientation == 2) {
                    value22 = 1;
                } else if (((DoorHandler)value8).originalOrientation == 3 && ((DoorHandler)value8).currentOrientation == 3) {
                    value22 = 0;
                } else if (((DoorHandler)value8).originalOrientation != ((DoorHandler)value8).currentOrientation) {
                    value22 = ((DoorHandler)value8).originalOrientation;
                }
            } else if (((DoorHandler)value8).initiallyOpen) {
                if (((DoorHandler)value8).originalOrientation == 0 && ((DoorHandler)value8).currentOrientation == 0) {
                    value22 = 3;
                } else if (((DoorHandler)value8).originalOrientation == 1 && ((DoorHandler)value8).currentOrientation == 1) {
                    value22 = 0;
                } else if (((DoorHandler)value8).originalOrientation == 2 && ((DoorHandler)value8).currentOrientation == 2) {
                    value22 = 1;
                } else if (((DoorHandler)value8).originalOrientation == 3 && ((DoorHandler)value8).currentOrientation == 3) {
                    value22 = 2;
                } else if (((DoorHandler)value8).originalOrientation != ((DoorHandler)value8).currentOrientation) {
                    value22 = ((DoorHandler)value8).originalOrientation;
                }
            }
        }
        ((DoorHandler)value8).currentOrientation = value22;
        int value9 = value22;
        ObjectManager.getInstance().removeDynamicObjectAt(((DoorHandler)value5).currentX, ((DoorHandler)value5).currentY, ((DoorHandler)value5).plane, 0);
        new DynamicObject(((DoorHandler)value5).currentObjectId, ((DoorHandler)value5).currentX, ((DoorHandler)value5).currentY, ((DoorHandler)value5).plane, value9, ((DoorHandler)value5).objectType, ServerSettings.placeholderObjectId, 999999, !preserveStaticCollision);
        boolean enabled = ((DoorHandler)value5).open = !((DoorHandler)value5).open;
        ((DoorHandler)value5).rapidToggleCount = System.currentTimeMillis() - ((DoorHandler)value5).lastInteractionMillis < 1000L ? ++((DoorHandler)value5).rapidToggleCount : 1;
        value7 = player;
        ((Player)value7).packetSender.sendSoundEffect(318, 1, 0);
        ((DoorHandler)value5).lastInteractionMillis = System.currentTimeMillis();
        return true;
    }

    public static void handleDoorMovement(Player player, int value7, int value22, int value32, int value42, int value52, int value62) {
        handleDoorMovement(player, value7, value22, value32, value42, value52, value62, 2);
    }

    /** Same as above, but the door stays open for {@code closeDelayTicks} game ticks before it closes again. */
    public static void handleDoorMovement(Player player, int value7, int value22, int value32, int value42, int value52, int value62, int closeDelayTicks) {
        DoorHandler doorHandler = DoorHandler.getOrCreateDoorState(value7, value22, value32, value42);
        if (doorHandler == null) {
            player.setActionLocked(false);
            return;
        }
        DoorHandler.handleDoor(player, value7, value22, value32, value42);
        DoorHandler doorHandler2 = doorHandler;
        Player player2 = player;
        // Door traversal deliberately crosses the door's collision boundary.
        player2.packetSender.queueRelativeMovementStep(value52, value62, true);
        CycleEventHandler.getInstance().schedule(player, new DoorResetEvent(player, doorHandler2), closeDelayTicks);
    }

    private static boolean isInitiallyOpenObjectId(int objectId) {
        int index = 0;
        while (index < initiallyOpenObjectIds.length) {
            if (initiallyOpenObjectIds[index] == objectId) {
                return true;
            }
            ++index;
        }
        return false;
    }

    static int getCurrentObjectId(DoorHandler doorHandler) {
        return doorHandler.currentObjectId;
    }

    static int getCurrentX(DoorHandler doorHandler) {
        return doorHandler.currentX;
    }

    static int getCurrentY(DoorHandler doorHandler) {
        return doorHandler.currentY;
    }

    static int getPlane(DoorHandler doorHandler) {
        return doorHandler.plane;
    }
}
