package com.rs2.net.packet;

/** Spell selections accepted by the 443 standard and ancient spellbooks. */
public final class SpellWidgets {
    private SpellWidgets() { }

    public static boolean isSpellWidget(int packedWidget) {
        int group = packedWidget >>> 16;
        return group == 192 || group == 193;
    }

    /**
     * Revision 443's old-format spell icons are top-level widgets. When one is
     * selected the client sends child 65535 and puts the actual spell child in
     * the low 16 bits of the packed widget id. Convert that native child back
     * to the legacy button id consumed by Spellbook.
     */
    public static int toLegacySpellButton(int packedWidget, int selectedChild) {
        int group = packedWidget >>> 16;
        int child = selectedChild == 0xFFFF || selectedChild < 0
                ? packedWidget & 0xFFFF : selectedChild;
        if (group == 192) return modernSpellButton(child);
        if (group == 193) return ancientSpellButton(child);
        return InterfaceBridge.UNMAPPED;
    }

    private static int modernSpellButton(int child) {
        switch (child) {
            case 0: return 1152; case 1: return 1153; case 2: return 1154;
            case 3: return 1155; case 4: return 1156; case 5: return 1157;
            case 6: return 1158; case 8: return 1160; case 9: return 1161;
            case 10: return 1162; case 11: return 1163; case 13: return 1165;
            case 14: return 1166; case 16: return 1168; case 17: return 1169;
            case 19: return 1171; case 20: return 1172; case 21: return 1173;
            case 23: return 1175; case 24: return 1176; case 25: return 1177;
            case 26: return 1178; case 27: return 1179; case 28: return 1180;
            case 29: return 1181; case 30: return 1182; case 31: return 1183;
            case 32: return 1184; case 33: return 1185; case 34: return 1186;
            case 35: return 1187; case 36: return 1188; case 37: return 1189;
            case 38: return 1190; case 39: return 1191; case 40: return 1192;
            case 387: return 1539; case 388: return 1540; case 389: return 1541; case 390: return 1542; case 391: return 1543;
            case 410: return 1562; case 420: return 1572; case 430: return 1582;
            case 440: return 1592; case 500: return 12037; case 511: return 12425;
            case 521: return 12435; case 531: return 12445; case 541: return 12455;
            case 549: return 6003;
            case 591: return 30000;
            // standard teleports and self spells (children from the 443 cache, interface 192)
            case 7: return 1159;  case 12: return 1164; case 15: return 1167;
            case 18: return 1170; case 22: return 1174; case 41: return 1193;
            case 492: return 7455; case 559: return 15877; case 569: return 18470;
            default: return InterfaceBridge.UNMAPPED;
        }
    }

    private static int ancientSpellButton(int child) {
        switch (child) {
            case 5: return 12861; case 15: return 12871; case 25: return 12881;
            case 35: return 12891; case 45: return 12901; case 55: return 12911;
            case 63: return 12919; case 73: return 12929; case 83: return 12939;
            case 95: return 12951; case 107: return 12963; case 119: return 12975;
            case 131: return 12987; case 143: return 12999; case 155: return 13011;
            case 167: return 13023;
            default: return InterfaceBridge.UNMAPPED;
        }
    }
}
