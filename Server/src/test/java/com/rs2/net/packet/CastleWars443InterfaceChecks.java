package com.rs2.net.packet;

/** Checks that revision 443 receives the Castle Wars HUD rather than dropping it. */
public final class CastleWars443InterfaceChecks {
    public static void main(String[] args) {
        require(InterfaceBridge.translateGroup(6673) == 57,
                "Castle Wars waiting overlay must use native group 57");
        require(InterfaceBridge.translateGroup(11344) == 59,
                "Castle Wars game overlay must use native group 59");
        int[][] textComponents = {
                {11346, 1}, {11352, 7}, {11353, 8}, {11356, 11},
                {11358, 13}, {11360, 15}, {11362, 17}
        };
        for (int[] component : textComponents) {
            require(InterfaceBridge.translate(component[0]) == (59 << 16 | component[1]),
                    "Wrong native HUD child for legacy component " + component[0]);
        }
        for (int varp : new int[]{377, 378, 380}) {
            require(InitialVarps.isVerified(varp),
                    "Native Castle Wars varp is blocked: " + varp);
        }
        System.out.println("Castle Wars revision-443 overlay mappings OK");
    }

    private static void require(boolean value, String message) {
        if (!value) throw new AssertionError(message);
    }
}
