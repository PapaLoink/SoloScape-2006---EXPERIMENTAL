import com.rs2.ServerSettings;
import com.rs2.model.Position;
import com.rs2.model.item.ItemStack;
import com.rs2.net.packet.handler.InterfaceInputPacketHandler;
import java.lang.reflect.Field;
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
 * The 13717 legacy ID serves two distinct purposes:
 * forward text writes go to native 307:1 (the raw-food name), while a
 * click on 307:3 still translates back to 13717 (Cook All).
 */
public final class CookingInterfaceChecks {
    private static final int COOKING_CHATBOX = 1743;
    private static final int NATIVE_COOKING_GROUP = 307;

    public static void main(String[] args) throws Exception {
        ServerSettings.clientBuild = 443;

        require(InterfaceBridge.translateGroup(COOKING_CHATBOX) == NATIVE_COOKING_GROUP,
                "Cooking quantity chatbox is not mapped to native group 307");
        // The title and food-name components must remain separate from
        // native Cook All. Do not overwrite button label 307:3 with food text.
        require(InterfaceBridge.translate(13715) == (NATIVE_COOKING_GROUP << 16 | 0),
                "Cooking title did not map to native 307:0");
        require(InterfaceBridge.translate(13717) == (NATIVE_COOKING_GROUP << 16 | 1),
                "Raw-food name did not map to native 307:1");
        require(InterfaceBridge.translate(13716) == (NATIVE_COOKING_GROUP << 16 | 2),
                "Cooking item model did not map to native 307:2");
        require(InterfaceBridge.translate(13717) != (NATIVE_COOKING_GROUP << 16 | 3),
                "Food-name updates would overwrite the Cook All label");
        require(InterfaceBridge.toLegacyComponent(NATIVE_COOKING_GROUP << 16 | 3) == 13717,
                "Native Cook All click no longer maps to legacy 13717");

        Player player = new Player(null);
        player.isBot = true; // Suppress socket writes in this packet-routing test.
        player.getSkillManager().getCurrentLevels()[7] = 99;
        require(player.getInventoryManager().addItem(new ItemStack(2132, 28)),
                "Could not fill the inventory with raw meat");

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

        // Ranges and (lit) fireplaces/fires share this dialog and its
        // quantity choices. The selected quantity must reach CookingTask.
        int[] children = {3, 5, 6, 4}; // All, 5, 1, X
        int[] expectedAmounts = {28, 5, 1, 3};
        for (String surface : new String[]{"cookRange", "cookFire"}) {
            for (int option = 0; option < children.length; option++) {
                player.setOpenInterfaceId(COOKING_CHATBOX);
                player.setSelectedSkillItemId(2132); // Raw meat.
                player.interfaceAction = surface;
                player.getCookingManager().firePosition = new Position(3200, 3200, 0);
                player.beginInterruptibleAction();
                player.setActiveCycleEvent(null);

                clickNative(player, children[option]);
                if (children[option] == 4) {
                    require(player.getSelectedInterfaceId() == 13718,
                            "Cook X did not request an amount for " + surface);
                    enterAmount(player, 3);
                    require(player.getSelectedInterfaceId() == 0,
                            "Cook X kept its stale amount-input target");
                }
                require(player.getActiveCycleEvent() instanceof CookingTask,
                        "Cooking did not start for " + surface + " option " + children[option]);
                require(remainingActions((CookingTask) player.getActiveCycleEvent())
                                == expectedAmounts[option],
                        "Wrong cooking quantity for " + surface + " option " + children[option]);
                require(player.getOpenInterfaceId() == 0,
                        "Starting to cook did not close the quantity dialog");
            }
        }

        player.setActiveCycleEvent(null);
        enterAmount(player, 3); // Replay without a fresh Cook-X prompt.
        require(player.getActiveCycleEvent() == null, "Stale Cook-X input started cooking");

        // An unrelated or closed chatbox must not authorize the reverse-only ID.
        player.setOpenInterfaceId(6179);
        player.setActiveCycleEvent(null);
        require(!player.isInterfaceIdOpen(13717),
                "Cook All was allowed without the cooking chatbox");
        clickNative(player, 3);
        require(player.getActiveCycleEvent() == null,
                "Cook All started cooking with an unrelated chatbox open");

        System.out.println("Cooking interface checks passed (range/fire, all quantities, closed-dialog guard).");
    }

    private static void clickNative(Player player, int child) {
        ByteBuffer payload = ByteBuffer.allocate(4);
        payload.putInt(NATIVE_COOKING_GROUP << 16 | child);
        payload.flip();
        new InterfaceActionPacketHandler().handle(player,
                new IncomingPacket(ClientPackets.INTERFACE_BUTTON, 4,
                        PacketBuffer.wrapReader(payload)));
    }

    private static void enterAmount(Player player, int amount) {
        ByteBuffer payload = ByteBuffer.allocate(4);
        payload.putInt(amount);
        payload.flip();
        new InterfaceInputPacketHandler().handle(player,
                new IncomingPacket(208, 4, PacketBuffer.wrapReader(payload)));
    }

    private static int remainingActions(CookingTask task) throws Exception {
        Field field = CookingTask.class.getDeclaredField("remainingActions");
        field.setAccessible(true);
        return field.getInt(task);
    }

    private static void require(boolean ok, String message) {
        if (!ok) {
            throw new AssertionError(message);
        }
    }
}
