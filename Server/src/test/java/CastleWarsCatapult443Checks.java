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
        // Regression: changing these values back to background widgets
        // recreates blank/malformed numbers even though packet routing passes.
        String[] digits = {"X_TENS", "X_ONES", "Y_TENS", "Y_ONES"};
        for (int i = 0; i < digits.length; i++) {
            Field component = CastleWarsEngineeringManager.class.getDeclaredField(
                    "CATAPULT_443_" + digits[i] + "_COMPONENT_ID");
            component.setAccessible(true);
            require(component.getInt(null) == 19704 + i,
                    "Digit " + digits[i] + " must update a foreground numeral, not a backing model");
        }

        java.lang.reflect.Method markerX = CastleWarsEngineeringManager.class
                .getDeclaredMethod("catapultMarkerOffsetX", CastleWarsManager.Team.class, int.class);
        java.lang.reflect.Method markerY = CastleWarsEngineeringManager.class
                .getDeclaredMethod("catapultMarkerOffsetY", CastleWarsManager.Team.class, int.class);
        markerX.setAccessible(true);
        markerY.setAccessible(true);

        // Stock group 54 shows Saradomin's catapult at (201,223),
        // Zamorak's at (73,101), and the red-X widget at (93,122).
        // Like the OSRS 00,00 screenshot, the X starts ONE TILE ahead of
        // the operator's own catapult, not at the opposing team or centre.
        int saradominX = (Integer) markerX.invoke(null, CastleWarsManager.Team.SARADOMIN, 0);
        int saradominY = (Integer) markerY.invoke(null, CastleWarsManager.Team.SARADOMIN, 0);
        int zamorakX = (Integer) markerX.invoke(null, CastleWarsManager.Team.ZAMORAK, 0);
        int zamorakY = (Integer) markerY.invoke(null, CastleWarsManager.Team.ZAMORAK, 0);
        require(saradominX + 93 == 197 && saradominY + 122 == 219,
                "00,00 must start the Saradomin red X beside its south-east catapult");
        require(zamorakX + 93 == 77 && zamorakY + 122 == 105,
                "00,00 must start the Zamorak red X beside its north-west catapult");

        // Increasing X/Y aims towards the middle from either team's base.
        // Every button press must move the map marker (no every-other-click freeze).
        require((Integer) markerX.invoke(null, CastleWarsManager.Team.SARADOMIN, 1) < saradominX
                && (Integer) markerY.invoke(null, CastleWarsManager.Team.SARADOMIN, 1) < saradominY,
                "Saradomin arrows must move the X north-west away from its own catapult");
        require((Integer) markerX.invoke(null, CastleWarsManager.Team.ZAMORAK, 1) > zamorakX
                && (Integer) markerY.invoke(null, CastleWarsManager.Team.ZAMORAK, 1) > zamorakY,
                "Zamorak arrows must move the X south-east away from its own catapult");
        require((Integer) markerX.invoke(null, CastleWarsManager.Team.SARADOMIN, 30) < saradominX
                && (Integer) markerY.invoke(null, CastleWarsManager.Team.SARADOMIN, 30) < saradominY
                && (Integer) markerX.invoke(null, CastleWarsManager.Team.ZAMORAK, 30) > zamorakX
                && (Integer) markerY.invoke(null, CastleWarsManager.Team.ZAMORAK, 30) > zamorakY,
                "30,30 target must move both teams' markers inward from their own catapult");

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

            assertArrowAxesMatchMapAndImpact(player, CastleWarsManager.Team.SARADOMIN);
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

            // At the new authentic 00,00 start the target is directly beside
            // the catapult. Move well clear of the disconnected Player fixture
            // before firing, otherwise splash damage queues a combat hit onto
            // an entity without a live network/session in this test.
            for (int step = 1; step < 20; ++step) {
                nativeWidgetSelect(player, 150); // up
                nativeWidgetButton(player, 152); // right
            }
            require(aimCoordinate(player, "x") == 20 && aimCoordinate(player, "y") == 20,
                    "Cannot aim safely away from the catapult before firing");
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
            require(aimCoordinate(player, "x") == 0 && aimCoordinate(player, "y") == 0,
                    "Legacy catapult aim must also start at 00,00");
            require(player.getInventoryManager().getItemAmount(
                    CastleWarsEngineeringManager.ROCK_ITEM_ID) == before - 1,
                    "Opening the legacy aiming interface consumed a rock");
            require(CastleWarsEngineeringManager.handleCatapultButton(player, 11321),
                    "Legacy aim-up control was not accepted");
            require(aimCoordinate(player, "x") == 1,
                    "Legacy aim-up control did not update the target");

            // Zamorak's catapult faces the opposite direction. The same
            // up/down versus left/right control separation must still hold.
            ServerSettings.clientBuild = 443;
            teams.put(player, CastleWarsManager.Team.ZAMORAK);
            player.setPosition(new Position(2384, 3117, 0));
            require(CastleWarsEngineeringManager.operateCatapult(player,
                    CastleWarsEngineeringManager.ZAMORAK_CATAPULT_ID),
                    "Operating Zamorak's 443 catapult failed");
            assertArrowAxesMatchMapAndImpact(player, CastleWarsManager.Team.ZAMORAK);
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

    private static void assertArrowAxesMatchMapAndImpact(
            Player player, CastleWarsManager.Team team) throws Exception {
        require(aimCoordinate(player, "x") == 0 && aimCoordinate(player, "y") == 0,
                "Native catapult must begin at 00,00 for " + team);
        int startX = aimCrossOffset(player, "catapultAimCrossOffsetX");
        int startY = aimCrossOffset(player, "catapultAimCrossOffsetY");
        Position startTarget = aimTarget(player);

        nativeWidgetSelect(player, 150); // top up (top number)
        require(aimCoordinate(player, "x") == 1 && aimCoordinate(player, "y") == 0,
                "Top arrow must change only the top numeral");
        int verticalX = aimCrossOffset(player, "catapultAimCrossOffsetX");
        int verticalY = aimCrossOffset(player, "catapultAimCrossOffsetY");
        Position verticalTarget = aimTarget(player);
        require(verticalX == startX && verticalY != startY,
                "Top up arrow must move X vertically only for " + team);
        require(verticalTarget.getX() == startTarget.getX()
                        && verticalTarget.getY() != startTarget.getY(),
                "Top up arrow must change impact world Y, not world X");

        nativeWidgetButton(player, 152); // bottom right (bottom number)
        require(aimCoordinate(player, "x") == 1 && aimCoordinate(player, "y") == 1,
                "Bottom arrow must change only the bottom numeral");
        int horizontalX = aimCrossOffset(player, "catapultAimCrossOffsetX");
        int horizontalY = aimCrossOffset(player, "catapultAimCrossOffsetY");
        Position horizontalTarget = aimTarget(player);
        require(horizontalX != verticalX && horizontalY == verticalY,
                "Bottom right arrow must move X horizontally only for " + team);
        require(horizontalTarget.getX() != verticalTarget.getX()
                        && horizontalTarget.getY() == verticalTarget.getY(),
                "Bottom right arrow must change impact world X, not world Y");
        int expectedDirection = team == CastleWarsManager.Team.ZAMORAK ? 1 : -1;
        require((verticalY - startY) * expectedDirection > 0
                        && (horizontalX - verticalX) * expectedDirection > 0,
                "Catapult cursor must travel from each team's base toward the centre");
    }

    private static Object activeAim(Player player) throws Exception {
        Field stateField = CastleWarsEngineeringManager.class.getDeclaredField("catapultAims");
        stateField.setAccessible(true);
        @SuppressWarnings("unchecked")
        Map<Player, ?> states = (Map<Player, ?>) stateField.get(null);
        Object aim = states.get(player);
        require(aim != null, "No active manual catapult aim");
        return aim;
    }

    private static int aimCrossOffset(Player player, String methodName) throws Exception {
        Object aim = activeAim(player);
        java.lang.reflect.Method method = CastleWarsEngineeringManager.class
                .getDeclaredMethod(methodName, aim.getClass());
        method.setAccessible(true);
        return ((Integer) method.invoke(null, aim)).intValue();
    }

    private static Position aimTarget(Player player) throws Exception {
        Object aim = activeAim(player);
        java.lang.reflect.Method method = CastleWarsEngineeringManager.class
                .getDeclaredMethod("resolvePlayerCatapultAimTarget", aim.getClass());
        method.setAccessible(true);
        return (Position) method.invoke(null, aim);
    }

    private static int aimCoordinate(Player player, String axis) throws Exception {
        Object aim = activeAim(player);
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
