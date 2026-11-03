package com.rs2.model.objects.functions;

import java.lang.reflect.Field;

/** The Castle Wars lobby's large double doors open once per server session. */
public final class CastleWarsLobbyEntranceChecks {
    private static final int LOBBY_X = 2441;
    private static final int LOBBY_Y = 3090;

    public static void main(String[] args) throws Exception {
        for (int id : new int[]{1516, 1517, 1519, 1520}) {
            require(DoubleDoorHandler.isCastleWarsLobbyEntranceLeaf(
                    id, LOBBY_X, LOBBY_Y, 0), "Missing lobby door visual ID " + id);
            require(!DoubleDoorHandler.isCastleWarsLobbyEntranceLeaf(
                    id, LOBBY_X, LOBBY_Y, 1), "Other planes must not be affected");
            require(!DoubleDoorHandler.isCastleWarsLobbyEntranceLeaf(
                    id, 3200, 3400, 0), "Doors elsewhere must keep normal behaviour");
        }
        for (int id : new int[]{4423, 4424, 4425, 4426, 4427, 4428, 4429, 4430}) {
            require(!DoubleDoorHandler.isCastleWarsLobbyEntranceLeaf(
                    id, LOBBY_X, LOBBY_Y, 0), "Arena gate was treated as lobby entrance");
        }

        for (int id : new int[]{1516, 1519}) {
            require(DoubleDoorHandler.didOpenCastleWarsLobbyEntranceLeaf(
                    id, id + 1, LOBBY_X, LOBBY_Y, 0, false),
                    "Opening a closed entrance leaf must latch the state");
            require(!DoubleDoorHandler.didOpenCastleWarsLobbyEntranceLeaf(
                    id, id, LOBBY_X, LOBBY_Y, 0, false),
                    "An unchanged door must not latch");
            require(!DoubleDoorHandler.didOpenCastleWarsLobbyEntranceLeaf(
                    id, id + 1, LOBBY_X, LOBBY_Y, 0, true),
                    "An initially-open object must not latch");
            require(!DoubleDoorHandler.didOpenCastleWarsLobbyEntranceLeaf(
                    id, id + 1, 3200, 3400, 0, false),
                    "Doors elsewhere must not latch");
        }

        // Verify that subsequent clicks are ignored once the per-process
        // latch is raised. Restore its previous state for test isolation.
        Field latch = DoubleDoorHandler.class.getDeclaredField(
                "castleWarsLobbyEntranceOpened");
        latch.setAccessible(true);
        boolean original = latch.getBoolean(null);
        try {
            latch.setBoolean(null, false);
            require(!DoubleDoorHandler.isCastleWarsLobbyEntranceLockedOpen(
                    1516, LOBBY_X, LOBBY_Y, 0),
                    "Initial closed entrance must remain operable");
            latch.setBoolean(null, true);
            for (int id : new int[]{1516, 1517, 1519, 1520}) {
                require(DoubleDoorHandler.isCastleWarsLobbyEntranceLockedOpen(
                        id, LOBBY_X, LOBBY_Y, 0),
                        "Opening latch must block further toggles of both leaves");
                require(!DoubleDoorHandler.isCastleWarsLobbyEntranceLockedOpen(
                        id, 3200, 3400, 0),
                        "Opening latch must not lock unrelated doors");
            }
            require(!DoubleDoorHandler.isCastleWarsLobbyEntranceLockedOpen(
                    4423, LOBBY_X, LOBBY_Y, 0),
                    "Castle Wars arena gates must still be closable");
        } finally {
            latch.setBoolean(null, original);
        }
        System.out.println("Castle Wars lobby entrance opens-once checks passed");
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}
