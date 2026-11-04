import com.rs2.ServerSettings;
import com.rs2.model.Position;
import com.rs2.model.World;
import com.rs2.model.gameplay.castlewars.CastleWarsEngineeringManager;
import com.rs2.model.gameplay.castlewars.CastleWarsManager;
import com.rs2.model.item.ItemStack;
import java.lang.reflect.Field;
import java.util.Map;
import com.rs2.model.player.Player;
import com.rs2.net.packet.ClientPackets;
import com.rs2.net.packet.IncomingPacket;
import com.rs2.net.packet.InterfaceBridge;
import com.rs2.net.packet.PacketBuffer;
import com.rs2.net.packet.handler.InterfaceActionPacketHandler;

import java.nio.ByteBuffer;

/** Regression checks for the native revision-443 Castle Wars catapult controls. */
public final class CastleWarsCatapult443Checks {
    private static final int ROOT = 11169;
    private static final int GROUP = 54;
    private static final int[] LEGACY_BUTTONS = {11321, 11322, 11323, 11324, 11329, 11259};
    private static final int[] CHILDREN = {150, 151, 152, 153, 158, 163};

    public static void main(String[] args) throws Exception {
        ServerSettings.clientBuild = 443;
        require(InterfaceBridge.translateGroup(ROOT) == GROUP,
                "Catapult interface must open native group 54 instead of auto-firing");
        for (int i = 0; i < CHILDREN.length; i++) {
            int packed = GROUP << 16 | CHILDREN[i];
            require(InterfaceBridge.translate(LEGACY_BUTTONS[i]) == packed,
                    "Wrong forward catapult mapping for child " + CHILDREN[i]);
            require(InterfaceBridge.toLegacyComponent(packed) == LEGACY_BUTTONS[i],
                    "Wrong reverse catapult mapping for child " + CHILDREN[i]);
        }
        for (int i = 0; i < 4; i++) {
            require(InterfaceBridge.translate(19700 + i)
                            == (GROUP << 16 | new int[]{116, 117, 132, 133}[i]),
                    "Wrong native static digit background mapping " + i);
            require(InterfaceBridge.translate(19704 + i)
                            == (GROUP << 16 | 146 + i),
                    "Wrong native foreground numeral mapping " + i);
        }
        require(InterfaceBridge.translate(19708) == (GROUP << 16 | 161),
                "The movable red X must use native group 54 child 161");

        java.lang.reflect.Method markerX = CastleWarsEngineeringManager.class
                .getDeclaredMethod("catapultMarkerOffsetX", int.class);
        java.lang.reflect.Method markerY = CastleWarsEngineeringManager.class
                .getDeclaredMethod("catapultMarkerOffsetY", int.class);
        markerX.setAccessible(true);
        markerY.setAccessible(true);
        require((Integer) markerX.invoke(null, 15) == 0
                && (Integer) markerY.invoke(null, 15) == 0,
                "Initial aiming target must use the native cache marker origin");
        require((Integer) markerX.invoke(null, 16) > 0
                && (Integer) markerY.invoke(null, 16) < 0,
                "Right/up arrow clicks must move the target right/up on the map");
        require((Integer) markerX.invoke(null, 0) < 0
                && (Integer) markerY.invoke(null, 30) < 0,
                "Map offsets must permit the whole 0..30 coordinate range");

        // Group 54 leaves base model 4857 unchanged at 116/117/132/133.
        // The foreground models are 4863..4872 for numeral values 0..9;
        // the original cache shows 4864..4867 ('1'..'4') at 146..149.
        java.lang.reflect.Method digitModel = CastleWarsEngineeringManager.class
                .getDeclaredMethod("catapultDigitModelId", int.class);
        digitModel.setAccessible(true);
        for (int digit = 0; digit <= 9; digit++) {
            require((Integer) digitModel.invoke(null, digit) == 4863 + digit,
                    "Incorrect native catapult digit model " + digit);
        }

        Player player = new Player(null);
        player.isBot = true; // This test requires no network connection.
        player.setOpenInterfaceId(ROOT);
        nativeWidgetSelect(player, 163);
        require(player.getOpenInterfaceId() == 0,
                "Native WIDGET_SELECT close did not reach the catapult handler");

        player.setOpenInterfaceId(ROOT);
        nativeWidgetButton(player, 163);
        require(player.getOpenInterfaceId() == 0,
                "Native INTERFACE_BUTTON close did not reach the catapult handler");

        // An unrelated panel must not accept forged group 54 catapult clicks.
        player.setOpenInterfaceId(6179);
        nativeWidgetButton(player, 163);
        nativeWidgetSelect(player, 163);
        require(player.getOpenInterfaceId() == 6179,
                "Closed catapult clicks unexpectedly closed another interface");

        // Exercise the whole open -> aim -> fire path against a disconnected
        // player fixture registered into the minigame's active team map.
        // isBot suppresses network writes only; operateCatapult still uses
        // the same manual aim state machine and inventory rules.
        Field teamsField = CastleWarsManager.class.getDeclaredField("gamePlayers");
        teamsField.setAccessible(true);
        @SuppressWarnings("unchecked")
        Map<Player, CastleWarsManager.Team> teams =
                (Map<Player, CastleWarsManager.Team>) teamsField.get(null);
        player.setIndex(1);
        World.players[1] = player;
        teams.put(player, CastleWarsManager.Team.SARADOMIN);
        try {
            player.setPosition(new Position(2413, 3088, 0));
            require(player.getInventoryManager().addItem(
                    new ItemStack(CastleWarsEngineeringManager.ROCK_ITEM_ID, 3)),
                    "Unable to supply test catapult ammunition");
            int before = player.getInventoryManager().getItemAmount(
                    CastleWarsEngineeringManager.ROCK_ITEM_ID);
            require(CastleWarsEngineeringManager.operateCatapult(player,
                    CastleWarsEngineeringManager.SARADOMIN_CATAPULT_ID),
                    "Operating a team catapult failed");
            require(player.getOpenInterfaceId() == ROOT,
                    "Operating a 443 catapult must open the aiming interface");
            require(player.getInventoryManager().getItemAmount(
                    CastleWarsEngineeringManager.ROCK_ITEM_ID) == before,
                    "Operating the catapult must not automatically spend a rock");

            require(aimCoordinate(player, "x") == 15 && aimCoordinate(player, "y") == 15,
                    "The initial aim was not centered");
            nativeWidgetSelect(player, 150); // up
            nativeWidgetButton(player, 152); // right
            require(aimCoordinate(player, "x") == 16 && aimCoordinate(player, "y") == 16,
                    "Revision-443 native aim arrows did not update target coordinates");
            require(player.getInventoryManager().getItemAmount(
                    CastleWarsEngineeringManager.ROCK_ITEM_ID) == before,
                    "Aiming must not consume ammunition");

            // Humans must never use the bot-only automatic targeting entry point,
            // even if they belong to a valid Castle Wars team and have ammunition.
            player.isBot = false;
            try {
                require(!CastleWarsEngineeringManager.fireCatapult(player),
                        "A human operator must choose manual catapult coordinates");
            } finally {
                player.isBot = true;
            }
            require(player.getInventoryManager().getItemAmount(
                    CastleWarsEngineeringManager.ROCK_ITEM_ID) == before,
                    "Rejecting automatic targeting must not consume a rock");

            nativeWidgetButton(player, 158); // Fire at the selected coordinates
            require(player.getInventoryManager().getItemAmount(
                    CastleWarsEngineeringManager.ROCK_ITEM_ID) == before - 1,
                    "Fire Catapult did not consume exactly one rock");
            require(player.getOpenInterfaceId() == ROOT,
                    "Firing should leave the aiming interface open");

            // The legacy client must use exactly the same open/aim state
            // machine, with no additional ammunition spent on opening.
            ServerSettings.clientBuild = 377;
            require(CastleWarsEngineeringManager.operateCatapult(player,
                    CastleWarsEngineeringManager.SARADOMIN_CATAPULT_ID),
                    "Operating the legacy catapult failed");
            require(player.getOpenInterfaceId() == ROOT,
                    "Legacy catapult must also open the aiming interface");
            require(aimCoordinate(player, "x") == 15 && aimCoordinate(player, "y") == 15,
                    "Legacy catapult aim was not centered");
            require(player.getInventoryManager().getItemAmount(
                    CastleWarsEngineeringManager.ROCK_ITEM_ID) == before - 1,
                    "Opening the legacy aiming interface consumed a rock");
            require(CastleWarsEngineeringManager.handleCatapultButton(player, 11321),
                    "Legacy aim-up control was not accepted");
            require(aimCoordinate(player, "x") == 16,
                    "Legacy aim-up control did not update the target");
        } finally {
            ServerSettings.clientBuild = 443;
            teams.remove(player);
            World.players[1] = null;
            player.setIndex(-1);
        }
        require(!CastleWarsEngineeringManager.fireCatapult(new Player(null)),
                "Non-bot auto-targeting must be disabled");
        System.out.println("Castle Wars native 443 manual aim, fire and widget routing OK");
    }

    private static int aimCoordinate(Player player, String axis) throws Exception {
        Field stateField = CastleWarsEngineeringManager.class.getDeclaredField("catapultAims");
        stateField.setAccessible(true);
        @SuppressWarnings("unchecked")
        Map<Player, ?> states = (Map<Player, ?>) stateField.get(null);
        Object aim = states.get(player);
        require(aim != null, "No active manual catapult aim");
        Field field = aim.getClass().getDeclaredField(axis);
        field.setAccessible(true);
        return field.getInt(aim);
    }

    private static void nativeWidgetSelect(Player player, int child) {
        ByteBuffer payload = ByteBuffer.allocate(6);
        payload.order(java.nio.ByteOrder.LITTLE_ENDIAN).putShort((short) 0);
        payload.order(java.nio.ByteOrder.BIG_ENDIAN).putInt(GROUP << 16 | child);
        payload.flip();
        new InterfaceActionPacketHandler().handle(player,
                new IncomingPacket(ClientPackets.WIDGET_SELECT, 6,
                        PacketBuffer.wrapReader(payload)));
    }

    private static void nativeWidgetButton(Player player, int child) {
        ByteBuffer payload = ByteBuffer.allocate(4);
        payload.putInt(GROUP << 16 | child);
        payload.flip();
        new InterfaceActionPacketHandler().handle(player,
                new IncomingPacket(ClientPackets.INTERFACE_BUTTON, 4,
                        PacketBuffer.wrapReader(payload)));
    }

    private static void require(boolean value, String message) {
        if (!value) throw new AssertionError(message);
    }
}
