package unpackaged;

import jagex.io.JSocket;
import jagex.io.FrameBuffer;
import jagex.utils.Huffmans;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

/** All 22 level-up icons must reuse their native 443 Skills-tab sprites. */
public final class LevelUpIconChecks {
    private static final String[] NAMES = {
        "Attack", "Defence", "Strength", "Hitpoints", "Ranged", "Prayer",
        "Magic", "Cooking", "Woodcutting", "Fletching", "Fishing",
        "Firemaking", "Crafting", "Smithing", "Mining", "Herblore",
        "Agility", "Thieving", "Slayer", "Farming", "Runecrafting", "Construction"
    };
    // Corresponding native group-320 sprite widgets, in server skill order.
    private static final int[] SKILL_WIDGETS = {
        48, 54, 51, 49, 57, 60, 63, 59, 65, 64, 56,
        62, 61, 53, 50, 55, 52, 58, 110, 115, 105, 45
    };

    public static void main(String[] args) throws Exception {
        Class39_Sub5_Sub4.widgetsLoaded = new boolean[500];
        Class62_Sub1.widgets = new Widget[500][];

        Widget[] statement = new Widget[3];
        for (int child = 0; child < statement.length; child++) {
            statement[child] = new Widget();
            statement[child].anInt2084 = (211 << 16) | child;
            statement[child].anInt2050 = -1;
            statement[child].type = 4;
            statement[child].anInt2090 = statement[child].anInt2091 = 21;
            statement[child].quadWidth = child == 1 ? 442 : 441;
        }
        Class62_Sub1.widgets[211] = statement;
        Class39_Sub5_Sub4.widgetsLoaded[211] = true;

        Widget[] skillTab = new Widget[150];
        for (int i = 0; i < SKILL_WIDGETS.length; i++) {
            int child = SKILL_WIDGETS[i];
            Widget source = new Widget();
            source.anInt2084 = (320 << 16) | child;
            source.type = 5;
            source.anInt2093 = 1000 + i; // distinguish every possible icon
            source.quadWidth = source.quadHeight = 25;
            skillTab[child] = source;
        }
        Class62_Sub1.widgets[320] = skillTab;
        Class39_Sub5_Sub4.widgetsLoaded[320] = true;

        require(JSocket.loadWidget(211), "Native two-line statement not loadable");
        Widget[] chatbox = Class62_Sub1.widgets[211];
        require(chatbox.length == 5, "Level-up widget was not extended");
        for (int i = 0; i < statement.length; i++) {
            require(chatbox[i] == statement[i], "Original chatbox child overwritten");
        }
        Widget selector = chatbox[3];
        Widget icon = chatbox[4];
        require(selector.type == 4 && selector.aBoolean2055, "Skill selector must be hidden");
        require(icon.type == 5 && icon.aBoolean2055, "Icon must initially be hidden");
        require(icon.anInt2050 == -1 && icon.anInt2089 == 0,
                "Level-up icon must be a passive chatbox child");
        require(icon.quadWidth == 64 && icon.quadHeight == 64
                && icon.anInt2091 == 6 && icon.anInt2021 == 16
                && icon.anInt2021 >= 0 && icon.anInt2021 + icon.quadHeight <= 96,
                "Scaled level-up icon does not fit native 443 chatbox clip");
        require(icon.aBoolean2013 && icon.anInt1997 == -1 && icon.anInt2051 == 0,
                "Large skill icon does not use the built-in scaled-sprite renderer");

        for (int i = 0; i < NAMES.length; i++) {
            sendSkillPacket(NAMES[i]);
            require(!icon.aBoolean2055, NAMES[i] + " level-up icon is missing");
            require(chatbox[0].anInt2091 == 76 && chatbox[0].quadWidth == 400
                    && chatbox[1].anInt2091 == 76 && chatbox[1].quadWidth == 400,
                    NAMES[i] + " has wrong level-up text geometry");
            require(icon.anInt2091 + icon.quadWidth <= chatbox[0].anInt2091,
                    NAMES[i] + " icon overlaps congratulations heading");
            require(icon.anInt2093 == 1000 + i, NAMES[i] + " has wrong skill icon");
            require(selector.aBoolean2055, "Hidden selector became visible");
            require(chatbox[0] == statement[0] && chatbox[1] == statement[1]
                    && chatbox[2] == statement[2], "Statement text/Continue was modified");
        }
        sendSkillPacket("");
        require(icon.aBoolean2055, "Ordinary two-line dialogue retained old level-up icon");
        require(chatbox[0].anInt2091 == 21 && chatbox[0].quadWidth == 441
                && chatbox[1].anInt2091 == 21 && chatbox[1].quadWidth == 442,
                "Ordinary statements retained shifted level-up text geometry");
        require(JSocket.loadWidget(211), "Level-up chatbox cannot reopen");
        require(Class62_Sub1.widgets[211][4] == icon,
                "Reloading chatbox replaced the skill icon");
        System.out.println("Level-up icon checks passed for all " + NAMES.length
                + " server skills, scaled 64px native sprites and dialogue cleanup.");
    }

    private static void sendSkillPacket(String value) throws Exception {
        byte[] text = value.getBytes(java.nio.charset.StandardCharsets.ISO_8859_1);
        byte[] payload = new byte[4 + text.length + 1];
        int widget = (211 << 16) | 3;
        payload[0] = (byte) widget;
        payload[1] = (byte) (widget >>> 8);
        payload[2] = (byte) (widget >>> 16);
        payload[3] = (byte) (widget >>> 24);
        System.arraycopy(text, 0, payload, 4, text.length);
        Class39_Sub5_Sub11.gameBuffer = new FrameBuffer(64);
        Class37.gameSocket = new JSocket(new Socket() {
            public void setSoTimeout(int timeout) {}
            public void setTcpNoDelay(boolean enabled) {}
            public InputStream getInputStream() {
                return new ByteArrayInputStream(payload);
            }
            public OutputStream getOutputStream() {
                return new ByteArrayOutputStream();
            }
        }, null);
        Class4.frameId = 180;
        Huffmans.frameSize = payload.length;
        require(Bzip2Block.readFrame(), "Revision-443 interface text packet not handled");
        require(Class4.frameId == -1, "Incomplete skill icon update");
    }

    private static void require(boolean ok, String message) {
        if (!ok) throw new AssertionError(message);
    }
}
