package com.rs2.model.objects.functions;

import com.rs2.ServerSettings;
import com.rs2.model.objects.DynamicObject;
import com.rs2.model.objects.LoadedWorldObject;
import com.rs2.model.objects.ObjectDefinition;
import com.rs2.model.objects.ObjectManager;
import com.rs2.model.objects.WorldObjectLookup;
import com.rs2.model.skill.SkillActionHelper;
import java.util.ArrayList;
import java.util.List;

public final class DoubleDoorHandler {
    private static List doorStates = new ArrayList();
    // The Castle Wars lobby entrance can only be opened once per world reset.
    // Keep this latch across games; a server restart resets the static field.
    private static boolean castleWarsLobbyEntranceOpened;
    private int currentObjectId;
    private int originalObjectId;
    private boolean initiallyOpen;
    private int currentX;
    private int currentY;
    private int plane;
    private int originalX;
    private int originalY;
    private int currentOrientation;
    private int originalOrientation;
    private static int[] initiallyOpenObjectIds = new int[]{1520, 1517, 11625, 11624};
    private static int[] wideOffsetObjectIds = new int[]{1551, 1553, 1598, 1599, 2261, 2262, 2438, 2439, 2050, 2051, 3015, 3016, 3198, 3197, 3725, 3726, 7049, 7050, 8810, 8811, 12816, 12817, 12986, 12987};

    // 1516/1519 are the closed lobby entrance leaves; 1517/1520 are
    // their open models. Constrain this exception to Castle Wars so other
    // double doors sharing these IDs retain their normal toggle behaviour.
    public static boolean isCastleWarsLobbyEntranceLeaf(int objectId, int x, int y, int plane) {
        return plane == 0
                && x >= 2430 && x <= 2452 && y >= 3078 && y <= 3104
                && (objectId == 1516 || objectId == 1517
                || objectId == 1519 || objectId == 1520);
    }

    public static boolean isCastleWarsLobbyEntranceLockedOpen(
            int objectId, int x, int y, int plane) {
        return castleWarsLobbyEntranceOpened
                && isCastleWarsLobbyEntranceLeaf(objectId, x, y, plane);
    }

    static boolean didOpenCastleWarsLobbyEntranceLeaf(
            int originalObjectId, int visibleObjectId,
            int originalX, int originalY, int plane, boolean initiallyOpen) {
        return !initiallyOpen
                && (originalObjectId == 1516 || originalObjectId == 1519)
                && visibleObjectId == originalObjectId + 1
                && isCastleWarsLobbyEntranceLeaf(originalObjectId, originalX, originalY, plane);
    }

    private static void markCastleWarsLobbyEntranceOpened(DoubleDoorHandler door) {
        if (didOpenCastleWarsLobbyEntranceLeaf(
                door.originalObjectId, door.currentObjectId,
                door.originalX, door.originalY, door.plane, door.initiallyOpen)) {
            castleWarsLobbyEntranceOpened = true;
        }
    }

    private static DoubleDoorHandler getOrCreateDoubleDoorState(int state, int value22, int value32) {
        Object value;
        for (Object doorStateObject : doorStates) {
            DoubleDoorHandler doubleDoorHandler = (DoubleDoorHandler)doorStateObject;
            if (doubleDoorHandler.currentX != state || doubleDoorHandler.currentY != value22 || doubleDoorHandler.plane != value32) continue;
            return doubleDoorHandler;
        }
        value = WorldObjectLookup.findObjectByNameAt("door", state, value22, value32);
        if (value == null && (value = WorldObjectLookup.findObjectByNameAt("gate", state, value22, value32)) == null && (value = WorldObjectLookup.findObjectByNameAt("fence", state, value22, value32)) == null) {
            return null;
        }
        DoubleDoorHandler doubleDoorHandler = new DoubleDoorHandler(((LoadedWorldObject)value).getWorldObject().getObjectId(), state, value22, value32);
        doorStates.add(doubleDoorHandler);
        return doubleDoorHandler;
    }

    public static boolean hasDoubleDoorAt(int value4, int value22, int value32) {
        return DoubleDoorHandler.getOrCreateDoubleDoorState(value4, value22, value32) != null;
    }

    public static boolean isOpen(int x, int y, int plane) {
        for (Object o : doorStates) {
            DoubleDoorHandler h = (DoubleDoorHandler)o;
            if (h.originalX == x && h.originalY == y && h.plane == plane) return h.currentX != x || h.currentY != y;
        }
        return false;
    }

    public static int[] currentLeaf(int x, int y, int plane) {
        for (Object o : doorStates) {
            DoubleDoorHandler h = (DoubleDoorHandler)o;
            if (h.originalX == x && h.originalY == y && h.plane == plane) return new int[]{h.currentObjectId, h.currentX, h.currentY};
        }
        return null;
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    public static boolean handleDoubleDoor(int value6, int value22, int value32, int value42) {
        Object value5;
        ObjectDefinition objectDefinition = ObjectDefinition.forId(value6);
        String text = objectDefinition != null && objectDefinition.name != null ? objectDefinition.name.toLowerCase() : "";
        if (!text.contains("fence") && !text.contains("gate") && !text.contains("door") || text.contains("trapdoor") || text.contains("tree")) {
            return false;
        }
        if (value6 == 2882 || value6 == 2883 || value6 == 1589 || value6 == 1590 || value6 == 2155 && value22 == 2592 && value32 == 9490 || value6 == 2154 && value22 == 2593 && value32 == 9490) {
            return false;
        }
        // The open lobby doors cannot be closed again until the world restarts.
        // Consume clicks here rather than allowing a generic door handler to
        // toggle the open objects back to their original positions.
        if (isCastleWarsLobbyEntranceLockedOpen(value6, value22, value32, value42)) {
            return true;
        }
        value5 = DoubleDoorHandler.getOrCreateDoubleDoorState(value22, value32, value42);
        if (value5 == null) {
            return false;
        }
        if (!((DoubleDoorHandler)value5).initiallyOpen) {
            if (((DoubleDoorHandler)value5).originalOrientation == 0) {
                DoubleDoorHandler doubleDoorHandler = DoubleDoorHandler.getOrCreateDoubleDoorState(value22, value32 - 1, value42);
                DoubleDoorHandler doubleDoorHandler2 = DoubleDoorHandler.getOrCreateDoubleDoorState(value22, value32 + 1, value42);
                DoubleDoorHandler doubleDoorHandler3 = null;
                DoubleDoorHandler doubleDoorHandler4 = null;
                if (DoubleDoorHandler.usesWideDoubleDoorOffset(value6)) {
                    doubleDoorHandler3 = DoubleDoorHandler.getOrCreateDoubleDoorState(value22 - 1, value32, value42);
                    doubleDoorHandler4 = DoubleDoorHandler.getOrCreateDoubleDoorState(value22 + 1, value32, value42);
                }
                if (doubleDoorHandler != null) {
                    DoubleDoorHandler.togglePrimaryDoorLeaf(doubleDoorHandler);
                    DoubleDoorHandler.toggleSecondaryDoorLeaf((DoubleDoorHandler)value5);
                    return true;
                } else if (doubleDoorHandler2 != null) {
                    DoubleDoorHandler.togglePrimaryDoorLeaf((DoubleDoorHandler)value5);
                    DoubleDoorHandler.toggleSecondaryDoorLeaf(doubleDoorHandler2);
                    return true;
                } else if (doubleDoorHandler3 != null) {
                    DoubleDoorHandler.togglePrimaryDoorLeaf((DoubleDoorHandler)value5);
                    DoubleDoorHandler.toggleSecondaryDoorLeaf(doubleDoorHandler3);
                    return true;
                } else {
                    if (doubleDoorHandler4 == null) return false;
                    DoubleDoorHandler.togglePrimaryDoorLeaf((DoubleDoorHandler)value5);
                    DoubleDoorHandler.toggleSecondaryDoorLeaf(doubleDoorHandler4);
                }
                return true;
            } else if (((DoubleDoorHandler)value5).originalOrientation == 1) {
                DoubleDoorHandler doubleDoorHandler = DoubleDoorHandler.getOrCreateDoubleDoorState(value22 - 1, value32, value42);
                DoubleDoorHandler doubleDoorHandler5 = DoubleDoorHandler.getOrCreateDoubleDoorState(value22 + 1, value32, value42);
                DoubleDoorHandler doubleDoorHandler6 = null;
                DoubleDoorHandler doubleDoorHandler7 = null;
                if (DoubleDoorHandler.usesWideDoubleDoorOffset(value6)) {
                    doubleDoorHandler6 = DoubleDoorHandler.getOrCreateDoubleDoorState(value22, value32 - 1, value42);
                    doubleDoorHandler7 = DoubleDoorHandler.getOrCreateDoubleDoorState(value22, value32 + 1, value42);
                }
                if (doubleDoorHandler != null) {
                    DoubleDoorHandler.togglePrimaryDoorLeaf(doubleDoorHandler);
                    DoubleDoorHandler.toggleSecondaryDoorLeaf((DoubleDoorHandler)value5);
                    return true;
                } else if (doubleDoorHandler5 != null) {
                    DoubleDoorHandler.togglePrimaryDoorLeaf((DoubleDoorHandler)value5);
                    DoubleDoorHandler.toggleSecondaryDoorLeaf(doubleDoorHandler5);
                    return true;
                } else if (doubleDoorHandler7 != null) {
                    DoubleDoorHandler.togglePrimaryDoorLeaf((DoubleDoorHandler)value5);
                    DoubleDoorHandler.toggleSecondaryDoorLeaf(doubleDoorHandler7);
                    return true;
                } else {
                    if (doubleDoorHandler6 == null) return false;
                    DoubleDoorHandler.togglePrimaryDoorLeaf(doubleDoorHandler6);
                    DoubleDoorHandler.toggleSecondaryDoorLeaf((DoubleDoorHandler)value5);
                }
                return true;
            } else if (((DoubleDoorHandler)value5).originalOrientation == 2) {
                DoubleDoorHandler doubleDoorHandler = DoubleDoorHandler.getOrCreateDoubleDoorState(value22, value32 + 1, value42);
                DoubleDoorHandler doubleDoorHandler8 = DoubleDoorHandler.getOrCreateDoubleDoorState(value22, value32 - 1, value42);
                DoubleDoorHandler doubleDoorHandler9 = null;
                DoubleDoorHandler doubleDoorHandler10 = null;
                if (DoubleDoorHandler.usesWideDoubleDoorOffset(value6)) {
                    doubleDoorHandler9 = DoubleDoorHandler.getOrCreateDoubleDoorState(value22 - 1, value32, value42);
                    doubleDoorHandler10 = DoubleDoorHandler.getOrCreateDoubleDoorState(value22 + 1, value32, value42);
                }
                if (doubleDoorHandler != null) {
                    DoubleDoorHandler.togglePrimaryDoorLeaf(doubleDoorHandler);
                    DoubleDoorHandler.toggleSecondaryDoorLeaf((DoubleDoorHandler)value5);
                    return true;
                } else if (doubleDoorHandler8 != null) {
                    DoubleDoorHandler.togglePrimaryDoorLeaf((DoubleDoorHandler)value5);
                    DoubleDoorHandler.toggleSecondaryDoorLeaf(doubleDoorHandler8);
                    return true;
                } else if (doubleDoorHandler10 != null) {
                    DoubleDoorHandler.togglePrimaryDoorLeaf((DoubleDoorHandler)value5);
                    DoubleDoorHandler.toggleSecondaryDoorLeaf(doubleDoorHandler10);
                    return true;
                } else {
                    if (doubleDoorHandler9 == null) return false;
                    DoubleDoorHandler.togglePrimaryDoorLeaf((DoubleDoorHandler)value5);
                    DoubleDoorHandler.toggleSecondaryDoorLeaf(doubleDoorHandler9);
                }
                return true;
            } else {
                if (((DoubleDoorHandler)value5).originalOrientation != 3) return true;
                DoubleDoorHandler doubleDoorHandler = DoubleDoorHandler.getOrCreateDoubleDoorState(value22 - 1, value32, value42);
                DoubleDoorHandler doubleDoorHandler11 = DoubleDoorHandler.getOrCreateDoubleDoorState(value22 + 1, value32, value42);
                DoubleDoorHandler doubleDoorHandler12 = null;
                DoubleDoorHandler doubleDoorHandler13 = null;
                if (DoubleDoorHandler.usesWideDoubleDoorOffset(value6)) {
                    doubleDoorHandler12 = DoubleDoorHandler.getOrCreateDoubleDoorState(value22, value32 - 1, value42);
                    doubleDoorHandler13 = DoubleDoorHandler.getOrCreateDoubleDoorState(value22, value32 + 1, value42);
                }
                if (doubleDoorHandler != null) {
                    DoubleDoorHandler.togglePrimaryDoorLeaf(doubleDoorHandler);
                    DoubleDoorHandler.toggleSecondaryDoorLeaf((DoubleDoorHandler)value5);
                    return true;
                } else if (doubleDoorHandler11 != null) {
                    DoubleDoorHandler.togglePrimaryDoorLeaf((DoubleDoorHandler)value5);
                    DoubleDoorHandler.toggleSecondaryDoorLeaf(doubleDoorHandler11);
                    return true;
                } else if (doubleDoorHandler12 != null) {
                    DoubleDoorHandler.togglePrimaryDoorLeaf(doubleDoorHandler12);
                    DoubleDoorHandler.toggleSecondaryDoorLeaf((DoubleDoorHandler)value5);
                    return true;
                } else {
                    if (doubleDoorHandler13 == null) return false;
                    DoubleDoorHandler.togglePrimaryDoorLeaf((DoubleDoorHandler)value5);
                    DoubleDoorHandler.toggleSecondaryDoorLeaf(doubleDoorHandler13);
                }
            }
            return true;
        } else {
            if (!((DoubleDoorHandler)value5).initiallyOpen) return true;
            if (((DoubleDoorHandler)value5).originalOrientation == 0) {
                DoubleDoorHandler doubleDoorHandler = DoubleDoorHandler.getOrCreateDoubleDoorState(value22 - 1, value32, value42);
                DoubleDoorHandler doubleDoorHandler14 = DoubleDoorHandler.getOrCreateDoubleDoorState(value22 + 1, value32, value42);
                if (doubleDoorHandler != null) {
                    DoubleDoorHandler.togglePrimaryDoorLeaf(doubleDoorHandler);
                    DoubleDoorHandler.toggleSecondaryDoorLeaf((DoubleDoorHandler)value5);
                    return true;
                } else {
                    if (doubleDoorHandler14 == null) return false;
                    DoubleDoorHandler.togglePrimaryDoorLeaf((DoubleDoorHandler)value5);
                    DoubleDoorHandler.toggleSecondaryDoorLeaf(doubleDoorHandler14);
                }
                return true;
            } else if (((DoubleDoorHandler)value5).originalOrientation == 1) {
                DoubleDoorHandler doubleDoorHandler = DoubleDoorHandler.getOrCreateDoubleDoorState(value22, value32 + 1, value42);
                DoubleDoorHandler doubleDoorHandler15 = DoubleDoorHandler.getOrCreateDoubleDoorState(value22, value32 - 1, value42);
                if (doubleDoorHandler != null) {
                    DoubleDoorHandler.togglePrimaryDoorLeaf(doubleDoorHandler);
                    DoubleDoorHandler.toggleSecondaryDoorLeaf((DoubleDoorHandler)value5);
                    return true;
                } else {
                    if (doubleDoorHandler15 == null) return false;
                    DoubleDoorHandler.togglePrimaryDoorLeaf((DoubleDoorHandler)value5);
                    DoubleDoorHandler.toggleSecondaryDoorLeaf(doubleDoorHandler15);
                }
                return true;
            } else if (((DoubleDoorHandler)value5).originalOrientation == 2) {
                DoubleDoorHandler doubleDoorHandler = DoubleDoorHandler.getOrCreateDoubleDoorState(value22 - 1, value32, value42);
                DoubleDoorHandler doubleDoorHandler16 = DoubleDoorHandler.getOrCreateDoubleDoorState(value22, value32 - 1, value42);
                if (doubleDoorHandler != null) {
                    DoubleDoorHandler.togglePrimaryDoorLeaf(doubleDoorHandler);
                    DoubleDoorHandler.toggleSecondaryDoorLeaf((DoubleDoorHandler)value5);
                    return true;
                } else {
                    if (doubleDoorHandler16 == null) return false;
                    DoubleDoorHandler.togglePrimaryDoorLeaf((DoubleDoorHandler)value5);
                    DoubleDoorHandler.toggleSecondaryDoorLeaf(doubleDoorHandler16);
                }
                return true;
            } else {
                if (((DoubleDoorHandler)value5).originalOrientation != 3) return true;
                DoubleDoorHandler doubleDoorHandler = DoubleDoorHandler.getOrCreateDoubleDoorState(value22, value32 + 1, value42);
                DoubleDoorHandler doubleDoorHandler17 = DoubleDoorHandler.getOrCreateDoubleDoorState(value22, value32 - 1, value42);
                if (doubleDoorHandler != null) {
                    DoubleDoorHandler.togglePrimaryDoorLeaf(doubleDoorHandler);
                    DoubleDoorHandler.toggleSecondaryDoorLeaf((DoubleDoorHandler)value5);
                    return true;
                } else {
                    if (doubleDoorHandler17 == null) return false;
                    DoubleDoorHandler.togglePrimaryDoorLeaf((DoubleDoorHandler)value5);
                    DoubleDoorHandler.toggleSecondaryDoorLeaf(doubleDoorHandler17);
                }
            }
        }
        return true;
    }
    private static void togglePrimaryDoorLeaf(DoubleDoorHandler doubleDoorHandler) {
        int newOrientation = doubleDoorHandler.currentOrientation;
        int xOffset = 0;
        int index = 0;
        ObjectManager.getInstance();
        Object dynamicObjectByIdAt = ObjectManager.findDynamicObjectByIdAt(doubleDoorHandler.currentObjectId, doubleDoorHandler.currentX, doubleDoorHandler.currentY, doubleDoorHandler.plane);
        if (dynamicObjectByIdAt != null) {
            ObjectManager.getInstance();
            ObjectManager.removeWallObjectCollision(doubleDoorHandler.currentX, doubleDoorHandler.currentY, doubleDoorHandler.plane, ((DynamicObject)dynamicObjectByIdAt).getWorldObject().getOrientation());
        }
        if ((dynamicObjectByIdAt = WorldObjectLookup.findObjectByIdAt(doubleDoorHandler.currentObjectId, doubleDoorHandler.currentX, doubleDoorHandler.currentY, doubleDoorHandler.plane)) != null) {
            ObjectManager.getInstance();
            ObjectManager.removeWallObjectCollision(doubleDoorHandler.currentX, doubleDoorHandler.currentY, doubleDoorHandler.plane, ((LoadedWorldObject)dynamicObjectByIdAt).getOrientation());
        }
        if (!doubleDoorHandler.initiallyOpen) {
            if (doubleDoorHandler.originalOrientation == 0 && doubleDoorHandler.currentOrientation == 0) {
                xOffset = -1;
            } else if (doubleDoorHandler.originalOrientation == 1 && doubleDoorHandler.currentOrientation == 1) {
                index = 1;
            } else if (doubleDoorHandler.originalOrientation == 2 && doubleDoorHandler.currentOrientation == 2) {
                xOffset = 1;
            } else if (doubleDoorHandler.originalOrientation == 3 && doubleDoorHandler.currentOrientation == 3) {
                index = -1;
            }
        } else if (doubleDoorHandler.initiallyOpen) {
            if (doubleDoorHandler.originalOrientation == 0 && doubleDoorHandler.currentOrientation == 0) {
                index = -1;
            } else if (doubleDoorHandler.originalOrientation == 1 && doubleDoorHandler.currentOrientation == 1) {
                xOffset = -1;
            } else if (doubleDoorHandler.originalOrientation == 2 && doubleDoorHandler.currentOrientation == 2) {
                xOffset = -1;
            } else if (doubleDoorHandler.originalOrientation == 3 && doubleDoorHandler.currentOrientation == 3) {
                // 2006Scape changeLeftDoor: initially-open face 3 closes west.
                xOffset = -1;
            }
        }
        if (xOffset != 0 || index != 0) {
            ObjectManager.getInstance().removeDynamicObjectAt(doubleDoorHandler.currentX, doubleDoorHandler.currentY, doubleDoorHandler.plane, 0);
            new DynamicObject(ServerSettings.placeholderObjectId, doubleDoorHandler.currentX, doubleDoorHandler.currentY, doubleDoorHandler.plane, 0, 0, ServerSettings.placeholderObjectId, 999999999);
        }
        if (doubleDoorHandler.currentX == doubleDoorHandler.originalX && doubleDoorHandler.currentY == doubleDoorHandler.originalY) {
            doubleDoorHandler.currentX += xOffset;
            doubleDoorHandler.currentY += index;
        } else {
            ObjectManager.getInstance().removeDynamicObjectAt(doubleDoorHandler.currentX, doubleDoorHandler.currentY, doubleDoorHandler.plane, 0);
            new DynamicObject(ServerSettings.placeholderObjectId, doubleDoorHandler.currentX, doubleDoorHandler.currentY, doubleDoorHandler.plane, 0, 0, ServerSettings.placeholderObjectId, 999999999);
            doubleDoorHandler.currentX = doubleDoorHandler.originalX;
            doubleDoorHandler.currentY = doubleDoorHandler.originalY;
        }
        // Match 2006Scape DoubleDoors: normal door leaves swap to the adjacent
        // open/closed object definition. Gates are a separate system there and
        // retain their object ids while their leaves are repositioned.
        if (!DoubleDoorHandler.usesWideDoubleDoorOffset(doubleDoorHandler.originalObjectId)) {
            if (doubleDoorHandler.currentObjectId == doubleDoorHandler.originalObjectId) {
                if (!doubleDoorHandler.initiallyOpen) {
                    ++doubleDoorHandler.currentObjectId;
                } else {
                    --doubleDoorHandler.currentObjectId;
                }
            } else {
                doubleDoorHandler.currentObjectId = doubleDoorHandler.originalObjectId;
            }
        } else {
            doubleDoorHandler.currentObjectId = doubleDoorHandler.originalObjectId;
        }
        ObjectManager.getInstance().removeDynamicObjectAt(doubleDoorHandler.currentX, doubleDoorHandler.currentY, doubleDoorHandler.plane, 0);
        int value = doubleDoorHandler.currentObjectId;
        int value2 = doubleDoorHandler.currentX;
        int value3 = doubleDoorHandler.currentY;
        int value4 = doubleDoorHandler.plane;
        int value5 = doubleDoorHandler.originalOrientation;
        if (!doubleDoorHandler.initiallyOpen) {
            if (doubleDoorHandler.originalOrientation == 0 && doubleDoorHandler.currentOrientation == 0) {
                newOrientation = 3;
            } else if (doubleDoorHandler.originalOrientation == 1 && doubleDoorHandler.currentOrientation == 1) {
                newOrientation = 0;
            } else if (doubleDoorHandler.originalOrientation == 2 && doubleDoorHandler.currentOrientation == 2) {
                newOrientation = 1;
            } else if (doubleDoorHandler.originalOrientation == 3 && doubleDoorHandler.currentOrientation == 3) {
                newOrientation = 0;
            } else if (doubleDoorHandler.originalOrientation != doubleDoorHandler.currentOrientation) {
                newOrientation = doubleDoorHandler.originalOrientation;
            }
        } else if (doubleDoorHandler.initiallyOpen) {
            if (doubleDoorHandler.originalOrientation == 0 && doubleDoorHandler.currentOrientation == 0) {
                newOrientation = 1;
            } else if (doubleDoorHandler.originalOrientation == 1 && doubleDoorHandler.currentOrientation == 1) {
                newOrientation = 2;
            } else if (doubleDoorHandler.originalOrientation == 2 && doubleDoorHandler.currentOrientation == 2) {
                newOrientation = 1;
            } else if (doubleDoorHandler.originalOrientation == 3 && doubleDoorHandler.currentOrientation == 3) {
                newOrientation = 2;
            } else if (doubleDoorHandler.originalOrientation != doubleDoorHandler.currentOrientation) {
                newOrientation = doubleDoorHandler.originalOrientation;
            }
        }
        doubleDoorHandler.currentOrientation = newOrientation;
        new DynamicObject(value, value2, value3, value4, newOrientation, 0, ServerSettings.placeholderObjectId, 999999999);
        markCastleWarsLobbyEntranceOpened(doubleDoorHandler);
    }

    private static void toggleSecondaryDoorLeaf(DoubleDoorHandler doubleDoorHandler) {
        int index = 0;
        int index2 = 0;
        ObjectManager.getInstance();
        Object dynamicObjectByIdAt = ObjectManager.findDynamicObjectByIdAt(doubleDoorHandler.currentObjectId, doubleDoorHandler.currentX, doubleDoorHandler.currentY, doubleDoorHandler.plane);
        if (dynamicObjectByIdAt != null) {
            ObjectManager.getInstance();
            ObjectManager.removeWallObjectCollision(doubleDoorHandler.currentX, doubleDoorHandler.currentY, doubleDoorHandler.plane, ((DynamicObject)dynamicObjectByIdAt).getWorldObject().getOrientation());
        }
        if ((dynamicObjectByIdAt = WorldObjectLookup.findObjectByIdAt(doubleDoorHandler.currentObjectId, doubleDoorHandler.currentX, doubleDoorHandler.currentY, doubleDoorHandler.plane)) != null) {
            ObjectManager.getInstance();
            ObjectManager.removeWallObjectCollision(doubleDoorHandler.currentX, doubleDoorHandler.currentY, doubleDoorHandler.plane, ((LoadedWorldObject)dynamicObjectByIdAt).getOrientation());
        }
        if (!doubleDoorHandler.initiallyOpen) {
            if (doubleDoorHandler.originalOrientation == 0 && doubleDoorHandler.currentOrientation == 0) {
                if (DoubleDoorHandler.usesWideDoubleDoorOffset(doubleDoorHandler.originalObjectId)) {
                    index = -2;
                    index2 = -1;
                } else {
                    index = -1;
                }
            } else if (doubleDoorHandler.originalOrientation == 1 && doubleDoorHandler.currentOrientation == 1) {
                if (DoubleDoorHandler.usesWideDoubleDoorOffset(doubleDoorHandler.originalObjectId)) {
                    index = -1;
                    index2 = 2;
                } else {
                    index2 = 1;
                }
            } else if (doubleDoorHandler.originalOrientation == 2 && doubleDoorHandler.currentOrientation == 2) {
                if (DoubleDoorHandler.usesWideDoubleDoorOffset(doubleDoorHandler.originalObjectId)) {
                    index = 2;
                    index2 = 1;
                } else {
                    index = 1;
                }
            } else if (doubleDoorHandler.originalOrientation == 3 && doubleDoorHandler.currentOrientation == 3) {
                if (DoubleDoorHandler.usesWideDoubleDoorOffset(doubleDoorHandler.originalObjectId)) {
                    index = -1;
                    index2 = -2;
                } else {
                    index2 = -1;
                }
            }
        } else if (doubleDoorHandler.initiallyOpen) {
            if (doubleDoorHandler.originalOrientation == 0 && doubleDoorHandler.currentOrientation == 0) {
                index = 1;
            } else if (doubleDoorHandler.originalOrientation == 1 && doubleDoorHandler.currentOrientation == 1) {
                index = -1;
            } else if (doubleDoorHandler.originalOrientation == 2 && doubleDoorHandler.currentOrientation == 2) {
                index2 = -1;
            } else if (doubleDoorHandler.originalOrientation == 3 && doubleDoorHandler.currentOrientation == 3) {
                index = -1;
            }
        }
        if (index != 0 || index2 != 0) {
            ObjectManager.getInstance().removeDynamicObjectAt(doubleDoorHandler.currentX, doubleDoorHandler.currentY, doubleDoorHandler.plane, 0);
            new DynamicObject(ServerSettings.placeholderObjectId, doubleDoorHandler.currentX, doubleDoorHandler.currentY, doubleDoorHandler.plane, 0, 0, ServerSettings.placeholderObjectId, 999999999);
        }
        if (doubleDoorHandler.currentX == doubleDoorHandler.originalX && doubleDoorHandler.currentY == doubleDoorHandler.originalY) {
            doubleDoorHandler.currentX += index;
            doubleDoorHandler.currentY += index2;
        } else {
            ObjectManager.getInstance().removeDynamicObjectAt(doubleDoorHandler.currentX, doubleDoorHandler.currentY, doubleDoorHandler.plane, 0);
            new DynamicObject(ServerSettings.placeholderObjectId, doubleDoorHandler.currentX, doubleDoorHandler.currentY, doubleDoorHandler.plane, 0, 0, ServerSettings.placeholderObjectId, 999999999);
            doubleDoorHandler.currentX = doubleDoorHandler.originalX;
            doubleDoorHandler.currentY = doubleDoorHandler.originalY;
        }
        // Normal doors swap object ids; wide gates keep their gate ids, just as
        // 2006Scape's separate gate handler does.
        if (!DoubleDoorHandler.usesWideDoubleDoorOffset(doubleDoorHandler.originalObjectId)) {
            if (doubleDoorHandler.currentObjectId == doubleDoorHandler.originalObjectId) {
                if (!doubleDoorHandler.initiallyOpen) {
                    ++doubleDoorHandler.currentObjectId;
                } else {
                    --doubleDoorHandler.currentObjectId;
                }
            } else {
                doubleDoorHandler.currentObjectId = doubleDoorHandler.originalObjectId;
            }
        } else {
            doubleDoorHandler.currentObjectId = doubleDoorHandler.originalObjectId;
        }
        ObjectManager.getInstance().removeDynamicObjectAt(doubleDoorHandler.currentX, doubleDoorHandler.currentY, doubleDoorHandler.plane, 0);
        int value = doubleDoorHandler.currentObjectId;
        int value2 = doubleDoorHandler.currentX;
        int value3 = doubleDoorHandler.currentY;
        int value4 = doubleDoorHandler.plane;
        index = doubleDoorHandler.originalOrientation;
        if (!doubleDoorHandler.initiallyOpen) {
            if (doubleDoorHandler.originalOrientation == 0 && doubleDoorHandler.currentOrientation == 0) {
                index = DoubleDoorHandler.usesWideDoubleDoorOffset(doubleDoorHandler.originalObjectId) ? 3 : 1;
            } else if (doubleDoorHandler.originalOrientation == 1 && doubleDoorHandler.currentOrientation == 1) {
                index = DoubleDoorHandler.usesWideDoubleDoorOffset(doubleDoorHandler.originalObjectId) ? 0 : 2;
            } else if (doubleDoorHandler.originalOrientation == 2 && doubleDoorHandler.currentOrientation == 2) {
                index = DoubleDoorHandler.usesWideDoubleDoorOffset(doubleDoorHandler.originalObjectId) ? 1 : 3;
            } else if (doubleDoorHandler.originalOrientation == 3 && doubleDoorHandler.currentOrientation == 3) {
                index = DoubleDoorHandler.usesWideDoubleDoorOffset(doubleDoorHandler.originalObjectId) ? 0 : 2;
            } else if (doubleDoorHandler.originalOrientation != doubleDoorHandler.currentOrientation) {
                index = doubleDoorHandler.originalOrientation;
            }
        } else if (doubleDoorHandler.initiallyOpen) {
            if (doubleDoorHandler.originalOrientation == 0 && doubleDoorHandler.currentOrientation == 0) {
                index = 3;
            } else if (doubleDoorHandler.originalOrientation == 1 && doubleDoorHandler.currentOrientation == 1) {
                index = 0;
            } else if (doubleDoorHandler.originalOrientation == 2 && doubleDoorHandler.currentOrientation == 2) {
                index = 1;
            } else if (doubleDoorHandler.originalOrientation == 3 && doubleDoorHandler.currentOrientation == 3) {
                index = 2;
            } else if (doubleDoorHandler.originalOrientation != doubleDoorHandler.currentOrientation) {
                index = doubleDoorHandler.originalOrientation;
            }
        }
        doubleDoorHandler.currentOrientation = index;
        new DynamicObject(value, value2, value3, value4, index, 0, ServerSettings.placeholderObjectId, 999999999);
        markCastleWarsLobbyEntranceOpened(doubleDoorHandler);
    }

    private static boolean isInitiallyOpenObjectId(int objectId) {
        int index = 0;
        while (index < initiallyOpenObjectIds.length) {
            if (objectId == initiallyOpenObjectIds[index] || objectId + 3 == initiallyOpenObjectIds[index]) {
                return true;
            }
            ++index;
        }
        return false;
    }

    public static boolean usesWideDoubleDoorOffset(int value2) {
        int index = 0;
        while (index < wideOffsetObjectIds.length) {
            if (value2 == wideOffsetObjectIds[index]) {
                return true;
            }
            ++index;
        }
        return false;
    }

    private DoubleDoorHandler(int currentObjectId, int currentX, int currentY, int plane) {
        this.currentObjectId = currentObjectId;
        this.originalObjectId = currentObjectId;
        this.initiallyOpen = DoubleDoorHandler.isInitiallyOpenObjectId(currentObjectId);
        this.currentX = currentX;
        this.originalX = currentX;
        this.currentY = currentY;
        this.plane = plane;
        this.originalY = currentY;
        this.originalOrientation = this.currentOrientation = SkillActionHelper.getObjectOrientation(currentObjectId, currentX, currentY, plane);
    }
}
