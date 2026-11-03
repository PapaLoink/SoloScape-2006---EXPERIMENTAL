package com.rs2.model.gameplay.castlewars;

import java.lang.reflect.Field;

/**
 * A defensive Castle Wars hint follows YOUR stolen flag, never the enemy flag.
 * Verifies both teams and all combinations of flag ownership.
 */
public final class CastleWarsFlagHintChecks {
    private CastleWarsFlagHintChecks() {
    }

    public static void main(String[] args) throws Exception {
        Field saradominAtBase = CastleWarsManager.class.getDeclaredField("saradominFlagAtBase");
        Field zamorakAtBase = CastleWarsManager.class.getDeclaredField("zamorakFlagAtBase");
        saradominAtBase.setAccessible(true);
        zamorakAtBase.setAccessible(true);
        boolean originalSaradomin = saradominAtBase.getBoolean(null);
        boolean originalZamorak = zamorakAtBase.getBoolean(null);

        try {
            check(saradominAtBase, zamorakAtBase, true, true, null, null);
            check(saradominAtBase, zamorakAtBase, false, true,
                    CastleWarsManager.Team.SARADOMIN, null);
            check(saradominAtBase, zamorakAtBase, true, false,
                    null, CastleWarsManager.Team.ZAMORAK);
            check(saradominAtBase, zamorakAtBase, false, false,
                    CastleWarsManager.Team.SARADOMIN, CastleWarsManager.Team.ZAMORAK);

            require(CastleWarsManager.getFlagHintTargetTeam(null) == null,
                    "Players outside the arena must not see a Castle Wars flag hint");

            // Recovered flags must remove the indicator even if the opposing
            // team's flag is still missing.
            saradominAtBase.setBoolean(null, true);
            zamorakAtBase.setBoolean(null, false);
            require(CastleWarsManager.getFlagHintTargetTeam(
                    CastleWarsManager.Team.SARADOMIN) == null,
                    "Returning Saradomin's flag must clear the Saradomin hint");
            require(CastleWarsManager.getFlagHintTargetTeam(
                    CastleWarsManager.Team.ZAMORAK) == CastleWarsManager.Team.ZAMORAK,
                    "Zamorak players must still see their own missing flag");
        } finally {
            saradominAtBase.setBoolean(null, originalSaradomin);
            zamorakAtBase.setBoolean(null, originalZamorak);
        }

        System.out.println("Castle Wars own-flag hint checks passed");
        System.exit(0);
    }

    private static void check(Field saradominAtBase, Field zamorakAtBase,
                              boolean saraSafe, boolean zamSafe,
                              CastleWarsManager.Team saraHint,
                              CastleWarsManager.Team zamHint) throws Exception {
        saradominAtBase.setBoolean(null, saraSafe);
        zamorakAtBase.setBoolean(null, zamSafe);
        require(CastleWarsManager.getFlagHintTargetTeam(
                CastleWarsManager.Team.SARADOMIN) == saraHint,
                "Wrong flag hint for Saradomin; flags at base: "
                        + saraSafe + ", " + zamSafe);
        require(CastleWarsManager.getFlagHintTargetTeam(
                CastleWarsManager.Team.ZAMORAK) == zamHint,
                "Wrong flag hint for Zamorak; flags at base: "
                        + saraSafe + ", " + zamSafe);
    }

    private static void require(boolean actual, String message) {
        if (!actual) {
            throw new AssertionError(message);
        }
    }
}
