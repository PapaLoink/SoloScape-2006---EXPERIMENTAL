package com.rs2.net.packet;

import java.nio.ByteBuffer;

/** Verified revision 443 client-to-server packet mappings from the 443 client deob. */
public final class ClientPackets {
    public static final int UNMAPPED = Integer.MIN_VALUE;

    public static final int WALK = 99;
    public static final int MINIMAP_WALK = 80;
    public static final int INTERACTION_WALK = 81;

    public static final int OBJECT_OPTION_1 = 47;
    public static final int OBJECT_OPTION_2 = 245;
    public static final int OBJECT_OPTION_3 = 69;
    public static final int OBJECT_OPTION_4 = 202;
    public static final int OBJECT_OPTION_5 = 120;
    public static final int OBJECT_EXAMINE = 36;
    public static final int ITEM_ON_OBJECT = 195;
    public static final int SPELL_ON_OBJECT = 78;

    public static final int NPC_OPTION_1 = 154;
    public static final int NPC_OPTION_2 = 224;
    public static final int NPC_OPTION_3 = 89;
    public static final int NPC_OPTION_4 = 222;
    public static final int NPC_OPTION_5 = 87;
    public static final int NPC_EXAMINE = 158;
    public static final int ITEM_ON_NPC = 146;
    public static final int SPELL_ON_NPC = 200;

    public static final int PLAYER_OPTION_1 = 11;
    public static final int PLAYER_OPTION_2 = 169;
    public static final int PLAYER_OPTION_3 = 229;
    public static final int PLAYER_OPTION_4 = 101;
    public static final int PLAYER_OPTION_5 = 206;
    public static final int ITEM_ON_PLAYER = 104;
    public static final int SPELL_ON_PLAYER = 236;

    public static final int GROUND_ITEM_OPTION_1 = 149;
    public static final int GROUND_ITEM_OPTION_2 = 252;
    public static final int GROUND_ITEM_OPTION_3 = 85;
    public static final int GROUND_ITEM_OPTION_4 = 38;
    public static final int GROUND_ITEM_OPTION_5 = 136;
    public static final int ITEM_ON_GROUND_ITEM = 114;
    public static final int SPELL_ON_GROUND_ITEM = 64;

    public static final int ITEM_OPTION_1 = 0;
    public static final int ITEM_OPTION_2 = 29;
    public static final int ITEM_OPTION_3 = 48;
    public static final int ITEM_OPTION_4 = 182;
    public static final int ITEM_OPTION_5 = 178;
    public static final int ITEM_ON_ITEM = 147;
    public static final int SPELL_ON_ITEM = 243;
    public static final int ITEM_EXAMINE = 219;

    public static final int WIDGET_ITEM_OPTION_1 = 144;
    public static final int WIDGET_ITEM_OPTION_2 = 113;
    public static final int WIDGET_ITEM_OPTION_3 = 188;
    public static final int WIDGET_ITEM_OPTION_4 = 221;
    public static final int WIDGET_ITEM_OPTION_5 = 171;

    public static final int PUBLIC_CHAT = 4;
    public static final int COMMAND = 174;
    public static final int PRIVATE_MESSAGE = 50;
    public static final int ADD_FRIEND = 90;
    public static final int REMOVE_FRIEND = 159;
    public static final int ADD_IGNORE = 198;
    public static final int REMOVE_IGNORE = 250;

    public static final int MOUSE_MOVEMENT = 133;
    public static final int MOUSE_CLICK = 162;
    public static final int CAMERA = 66;
    public static final int FOCUS = 207;
    public static final int IDLE = 192;
    public static final int KEEPALIVE = 86;
    public static final int PING = 254; // Client FPS diagnostics request (int ID)
    public static final int WIDGET_ITEM_DRAG = 190;
    public static final int WIDGET_DRAG_DROP = 46;
    public static final int REGION_LOADED = 21;
    public static final int CLOSE_INTERFACE = 70;
    public static final int AMOUNT_INPUT = 74;
    public static final int NAME_INPUT = 22;
    public static final int APPEARANCE = 118;
    public static final int REPORT_ABUSE = 119;
    public static final int DISPLAY_OPTIONS = 76;
    public static final int SIDEBAR_ACK = 65;
    public static final int SPELL_ON_WIDGET = 145;
    public static final int WIDGET_SELECT = 153;
    public static final int REGION_VALIDATION = 141;

    public static final int INTERFACE_BUTTON = 54;
    public static final int[] INTERFACE_OPERATIONS = {
            13, 217, 161, 3, 40, 73, 57, 167, 28, 8
    };

    private ClientPackets() {
    }

    public static int getLength(int opcode) {
        if (opcode == 19) return 2; // paired client exchange item selection
        if (opcode == WALK || opcode == MINIMAP_WALK || opcode == INTERACTION_WALK) {
            return -1;
        }
        if (isObjectOption(opcode)) return 6;
        if (opcode == OBJECT_EXAMINE) return 2;
        if (opcode == ITEM_ON_OBJECT) return 14;
        if (opcode == SPELL_ON_OBJECT) return 12;
        if (isNpcOption(opcode) || opcode == NPC_EXAMINE) return 2;
        if (opcode == ITEM_ON_NPC) return 10;
        if (opcode == SPELL_ON_NPC) return 8;
        if (isPlayerOption(opcode)) return 2;
        if (opcode == ITEM_ON_PLAYER) return 10;
        if (opcode == SPELL_ON_PLAYER) return 8;
        if (isGroundItemOption(opcode)) return 6;
        if (opcode == ITEM_ON_GROUND_ITEM) return 14;
        if (opcode == SPELL_ON_GROUND_ITEM) return 12;
        if (isItemOption(opcode) || isWidgetItemOption(opcode)) return 8;
        if (opcode == ITEM_ON_ITEM) return 16;
        if (opcode == SPELL_ON_ITEM) return 14;
        if (opcode == ITEM_EXAMINE) return 2;
        if (opcode == PUBLIC_CHAT || opcode == COMMAND || opcode == PRIVATE_MESSAGE
                || opcode == MOUSE_MOVEMENT) return -1;
        if (opcode == ADD_FRIEND || opcode == REMOVE_FRIEND
                || opcode == ADD_IGNORE || opcode == REMOVE_IGNORE) return 8;
        if (opcode == MOUSE_CLICK || opcode == CAMERA) return 4;
        if (opcode == FOCUS) return 1;
        if (opcode == PING) return 12; // ping ID and client send wall-clock millis
        if (opcode == IDLE || opcode == KEEPALIVE || opcode == REGION_LOADED
                || opcode == CLOSE_INTERFACE) return 0;
        if (opcode == WIDGET_ITEM_DRAG) return 9;
        if (opcode == WIDGET_DRAG_DROP) return 12;
        if (opcode == AMOUNT_INPUT) return 4;
        if (opcode == NAME_INPUT) return 8;
        if (opcode == APPEARANCE) return 13;
        if (opcode == REPORT_ABUSE) return 10;
        if (opcode == DISPLAY_OPTIONS) return 3;
        if (opcode == SIDEBAR_ACK) return 1;
        if (opcode == SPELL_ON_WIDGET) return 12;
        if (opcode == WIDGET_SELECT) return 6;
        if (opcode == REGION_VALIDATION) return 4;
        if (opcode == INTERFACE_BUTTON) return 4;
        if (getInterfaceOperation(opcode) != -1) return 6;
        return UNMAPPED;
    }

    public static boolean isObjectOption(int opcode) {
        return getObjectOption(opcode) != -1;
    }

    public static int getObjectOption(int opcode) {
        switch (opcode) {
            case OBJECT_OPTION_1: return 1;
            case OBJECT_OPTION_2: return 2;
            case OBJECT_OPTION_3: return 3;
            case OBJECT_OPTION_4: return 4;
            case OBJECT_OPTION_5: return 5;
            default: return -1;
        }
    }

    public static boolean isNpcOption(int opcode) {
        return getNpcOption(opcode) != -1;
    }

    public static int getNpcOption(int opcode) {
        switch (opcode) {
            case NPC_OPTION_1: return 1;
            case NPC_OPTION_2: return 2;
            case NPC_OPTION_3: return 3;
            case NPC_OPTION_4: return 4;
            case NPC_OPTION_5: return 5;
            default: return -1;
        }
    }

    public static boolean isPlayerOption(int opcode) {
        return getPlayerOption(opcode) != -1;
    }

    public static int getPlayerOption(int opcode) {
        switch (opcode) {
            case PLAYER_OPTION_1: return 1;
            case PLAYER_OPTION_2: return 2;
            case PLAYER_OPTION_3: return 3;
            case PLAYER_OPTION_4: return 4;
            case PLAYER_OPTION_5: return 5;
            default: return -1;
        }
    }

    public static boolean isGroundItemOption(int opcode) {
        return getGroundItemOption(opcode) != -1;
    }

    public static int getGroundItemOption(int opcode) {
        switch (opcode) {
            case GROUND_ITEM_OPTION_1: return 1;
            case GROUND_ITEM_OPTION_2: return 2;
            case GROUND_ITEM_OPTION_3: return 3;
            case GROUND_ITEM_OPTION_4: return 4;
            case GROUND_ITEM_OPTION_5: return 5;
            default: return -1;
        }
    }

    public static boolean isItemOption(int opcode) {
        return getItemOption(opcode) != -1;
    }

    public static int getItemOption(int opcode) {
        switch (opcode) {
            case ITEM_OPTION_1: return 1;
            case ITEM_OPTION_2: return 2;
            case ITEM_OPTION_3: return 3;
            case ITEM_OPTION_4: return 4;
            case ITEM_OPTION_5: return 5;
            default: return -1;
        }
    }

    public static boolean isWidgetItemOption(int opcode) {
        return getWidgetItemOption(opcode) != -1;
    }

    public static int getWidgetItemOption(int opcode) {
        switch (opcode) {
            case WIDGET_ITEM_OPTION_1: return 1;
            case WIDGET_ITEM_OPTION_2: return 2;
            case WIDGET_ITEM_OPTION_3: return 3;
            case WIDGET_ITEM_OPTION_4: return 4;
            case WIDGET_ITEM_OPTION_5: return 5;
            default: return -1;
        }
    }

    public static int getInterfaceOperation(int opcode) {
        for (int index = 0; index < INTERFACE_OPERATIONS.length; index++) {
            if (INTERFACE_OPERATIONS[index] == opcode) return index + 1;
        }
        return -1;
    }

    public static int readIntLittle(PacketReader reader) {
        ByteBuffer buffer = reader.getBuffer();
        int b0 = buffer.get() & 0xFF;
        int b1 = buffer.get() & 0xFF;
        int b2 = buffer.get() & 0xFF;
        int b3 = buffer.get() & 0xFF;
        return b0 | b1 << 8 | b2 << 16 | b3 << 24;
    }

    /** Client method655: bytes are value>>8, value, value>>24, value>>16. */
    public static int readIntMiddle(PacketReader reader) {
        ByteBuffer buffer = reader.getBuffer();
        int b0 = buffer.get() & 0xFF;
        int b1 = buffer.get() & 0xFF;
        int b2 = buffer.get() & 0xFF;
        int b3 = buffer.get() & 0xFF;
        return b2 << 24 | b3 << 16 | b0 << 8 | b1;
    }

    /** Client method663: bytes are value>>16, value>>24, value, value>>8. */
    public static int readIntInverseMiddle(PacketReader reader) {
        ByteBuffer buffer = reader.getBuffer();
        int b0 = buffer.get() & 0xFF;
        int b1 = buffer.get() & 0xFF;
        int b2 = buffer.get() & 0xFF;
        int b3 = buffer.get() & 0xFF;
        return b1 << 24 | b0 << 16 | b3 << 8 | b2;
    }
}
