import com.rs2.ServerSettings;
import com.rs2.model.player.Player;
import com.rs2.model.skill.cooking.CookingTask;
import com.rs2.net.packet.ClientPackets;
import com.rs2.net.packet.IncomingPacket;
import com.rs2.net.packet.InterfaceBridge;
import com.rs2.net.packet.PacketBuffer;
import com.rs2.net.packet.handler.InterfaceActionPacketHandler;
import java.nio.ByteBuffer;

/**
 * Regression for issue #7: Cook All on the native 443 quantity chatbox must
 * schedule the normal cooking action for ranges and fireplaces.
 *
 * The 13717 component is reverse-only because the same legacy id is also the
 * food-name text widget; it must not require a forward component mapping.
 */
public final class CookingInterfaceChecks {
    private static final int COOKING_CHATBOX = 1743;
    private static final int NATIVE_COOKING_GROUP = 307;

    public static void main(String[] args) {
        ServerSettings.clientBuild = 443;

        require(InterfaceBridge.translateGroup(COOKING_CHATBOX) == NATIVE_COOKING_GROUP,
                "Cooking quantity chatbox is not mapped to native group 307");

        Player player = new Player(null);
        player.isBot = true; // Suppress socket writes in this packet-routing test.

        int[] legacyButtons = {13717, 13718, 13719, 13720};
        for (int child = 3; child <= 6; child++) {
            int legacyButton = legacyButtons[child - 3];
            require(InterfaceBridge.toLegacyComponent(NATIVE_COOKING_GROUP << 16 | child)
                            == legacyButton,
                    "Native cooking child " + child + " has the wrong legacy action");

            player.setOpenInterfaceId(COOKING_CHATBOX);
            require(player.isInterfaceIdOpen(legacyButton),
                    "Native cooking action " + legacyButton + " was rejected as closed");
        }

        // Both surfaces use the same chatbox: Cook All must route for each.
        for (String surface : new String[]{"cookRange", "cookFire"}) {
            player.setOpenInterfaceId(COOKING_CHATBOX);
            player.setSelectedSkillItemId(2132); // Raw meat.
            player.interfaceAction = surface;
            player.beginInterruptibleAction();
            player.setActiveCycleEvent(null);

            clickNative(player, 3); // Cook All
            require(player.getActiveCycleEvent() instanceof CookingTask,
                    "Cook All did not schedule cooking on " + surface);
            require(player.getOpenInterfaceId() == 0,
                    "Starting to cook did not close the quantity dialog");
        }

        // An unrelated or closed chatbox must not authorize the reverse-only ID.
        player.setOpenInterfaceId(6179);
        player.setActiveCycleEvent(null);
        require(!player.isInterfaceIdOpen(13717),
                "Cook All was allowed without the cooking chatbox");
        clickNative(player, 3);
        require(player.getActiveCycleEvent() == null,
                "Cook All started cooking with an unrelated chatbox open");

        System.out.println("Cooking interface checks passed (range/fire, Cook All, closed-dialog guard).");
    }

    private static void clickNative(Player player, int child) {
        ByteBuffer payload = ByteBuffer.allocate(4);
        payload.putInt(NATIVE_COOKING_GROUP << 16 | child);
        payload.flip();
        new InterfaceActionPacketHandler().handle(player,
                new IncomingPacket(ClientPackets.INTERFACE_BUTTON, 4,
                        PacketBuffer.wrapReader(payload)));
    }

    private static void require(boolean ok, String message) {
        if (!ok) {
            throw new AssertionError(message);
        }
    }
}
