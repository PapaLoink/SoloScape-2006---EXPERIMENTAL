import com.rs2.ServerSettings;
import com.rs2.cache.js5.Interfaces;
import com.rs2.model.gameplay.castlewars.CastleWarsManager;
import com.rs2.model.player.Player;
import com.rs2.net.packet.ClientPackets;
import com.rs2.net.packet.IncomingPacket;
import com.rs2.net.packet.InterfaceBridge;
import com.rs2.net.packet.PacketBuffer;
import com.rs2.net.packet.handler.InterfaceActionPacketHandler;
import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.util.Map;

/** Native 443 Castle Wars guide, seasonal scoreboard and vacancy title checks. */
public final class CastleWarsNativePanels443Checks {
    private CastleWarsNativePanels443Checks() {
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }

    public static void main(String[] args) throws Exception {
        ServerSettings.clientBuild = 443;
        Interfaces.load();
        require(InterfaceBridge.translateGroup(837) == 49,
                "Castle Wars guide must open native two-page book group 49");
        require(InterfaceBridge.translateGroup(11333) == 55,
                "Castle Wars scoreboard must open native group 55");

        Map<Integer, Interfaces.Component> book = Interfaces.group(49);
        Map<Integer, Interfaces.Component> scoreboard = Interfaces.group(55);
        Map<Integer, Interfaces.Component> choices = Interfaces.group(228);
        require(book.size() >= 114, "Native guide has missing book widgets");
        require(scoreboard.size() >= 12, "Native seasonal scoreboard is incomplete");
        require(choices.size() >= 9, "Native two-option dialogue is incomplete");

        assertMapping(903, 49, 38, book, 4);
        for (int i = 0; i < 22; i++) {
            assertMapping(843 + i, 49, 87 + i, book, 4);
        }
        assertMapping(14165, 49, 109, book, 4);
        assertMapping(14166, 49, 110, book, 4);
        assertMapping(839, 49, 83, book, 5);
        assertMapping(841, 49, 85, book, 5);
        assertMapping(10162, 49, 112, book, 5);
        for (int i = 0; i < 3; i++) {
            assertMapping(11334 + i, 55, i, scoreboard, 4);
        }
        assertMapping(19730, 55, 10, scoreboard, 5);
        assertMapping(19731, 228, 6, choices, 5);
        assertMapping(19732, 228, 7, choices, 5);

        require(choices.get(6).parentId == (228 << 16 | 8)
                        && choices.get(7).parentId == (228 << 16 | 8),
                "Vacancy swords must both flank the wide title container");
        int left = getStaticInt("VACANCY_LEFT_SWORD_X_OFFSET");
        int right = getStaticInt("VACANCY_RIGHT_SWORD_X_OFFSET");
        require(left > 0 && right < 0,
                "Castle Wars vacancy title swords must move toward its text");

        Player player = new Player(null);
        player.isBot = true;
        CastleWarsManager.openCastleWarsManual(player);
        require(player.activeBookItemId == CastleWarsManager.CASTLE_WARS_MANUAL_ID,
                "The book must record which manual is being read");
        require(player.activeBookPageIndex == 0 && player.getOpenInterfaceId() == 837,
                "The Castle Wars manual must open at page 1");
        nativeWidgetSelect(player, 85); // native next arrow
        require(player.activeBookPageIndex == 1,
                "Native 443 next-page arrow must turn the book");
        nativeWidgetSelect(player, 83); // native previous arrow
        require(player.activeBookPageIndex == 0,
                "Native 443 previous-page arrow must turn back");
        nativeWidgetSelect(player, 112); // native close control
        require(player.activeBookItemId == 0,
                "Closing the guide must clear active book state");
        require(player.getOpenInterfaceId() != 837,
                "Closing the guide must close the native book");

        player.setOpenInterfaceId(11333);
        require(CastleWarsManager.handleScoreboardCloseButton(player, 19730),
                "Native scoreboard close must be handled");
        require(player.getOpenInterfaceId() != 11333,
                "Scoreboard close must exit the native chatbox");
        player.setOpenInterfaceId(837);
        require(!CastleWarsManager.handleScoreboardCloseButton(player, 19730),
                "Scoreboard close cannot dismiss another interface");

        System.out.println("Castle Wars native guide, scoreboard and sword title checks passed");
    }

    private static int getStaticInt(String name) throws Exception {
        Field field = CastleWarsManager.class.getDeclaredField(name);
        field.setAccessible(true);
        return field.getInt(null);
    }

    private static void assertMapping(int legacy, int group, int child,
            Map<Integer, Interfaces.Component> nativeWidgets, int type) {
        int packed = group << 16 | child;
        require(InterfaceBridge.translate(legacy) == packed,
                "Missing 443 mapping " + legacy + " to " + group + ":" + child);
        require(nativeWidgets.get(child) != null && nativeWidgets.get(child).type == type,
                "Wrong native type for " + group + ":" + child);
        require(InterfaceBridge.toLegacyComponent(packed) == legacy,
                "Native 443 reverse mapping missing for " + group + ":" + child);
    }

    private static void nativeWidgetSelect(Player player, int child) {
        ByteBuffer payload = ByteBuffer.allocate(6);
        payload.order(java.nio.ByteOrder.LITTLE_ENDIAN).putShort((short) 0);
        payload.order(java.nio.ByteOrder.BIG_ENDIAN).putInt(49 << 16 | child);
        payload.flip();
        new InterfaceActionPacketHandler().handle(player,
                new IncomingPacket(ClientPackets.WIDGET_SELECT, 6,
                        PacketBuffer.wrapReader(payload)));
    }
}
