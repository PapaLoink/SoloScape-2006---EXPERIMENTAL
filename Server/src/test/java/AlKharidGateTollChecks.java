import com.rs2.ServerSettings;
import com.rs2.model.Position;
import com.rs2.model.interaction.AlKharidGateToll;
import com.rs2.model.interaction.InteractionDispatcher;
import com.rs2.model.interaction.SecondObjectActionTask;
import com.rs2.model.item.ItemDefinition;
import com.rs2.model.item.ItemStack;
import com.rs2.model.objects.ObjectDefinition;
import com.rs2.model.objects.ObjectManager;
import com.rs2.model.objects.WorldObject;
import com.rs2.model.objects.WorldObjectLookup;
import com.rs2.model.player.Player;
import com.rs2.model.quest.QuestDefinition;
import com.rs2.net.IsaacCipher;
import com.rs2.util.path.WalkingCollisionMap;
import java.lang.reflect.Field;
import java.nio.channels.SocketChannel;

/** Al-Kharid Pay-toll action, two-way charging, and Prince Ali Rescue exemption. */
public final class AlKharidGateTollChecks {
    private static Field socket;
    private static SocketChannel transport;

    public static void main(String[] args) {
        try {
            ServerSettings.clientBuild = 443;
            QuestDefinition.loadDefinitions();
            ItemDefinition.loadDefinitions();
            ObjectDefinition.loadRevision443();
            WorldObjectLookup.loadWorldObjects();
            WalkingCollisionMap.loadCollisionMaps();
            require(WorldObjectLookup.findObjectByIdAt(2882, 3268, 3227, 0) != null,
                    "South gate leaf missing from cache");
            require(WorldObjectLookup.findObjectByIdAt(2883, 3268, 3228, 0) != null,
                    "North gate leaf missing from cache");

            transport = SocketChannel.open();
            transport.close();
            socket = Player.class.getDeclaredField("socketChannel");
            socket.setAccessible(true);

            require(AlKharidGateToll.isTollGate(2882, 3268, 3227, 0), "South leaf not recognized");
            require(AlKharidGateToll.isTollGate(2883, 3268, 3228, 0), "North leaf not recognized");
            require(!AlKharidGateToll.isTollGate(2882, 3268, 3229, 0), "Wrong gate coordinates accepted");
            require(!AlKharidGateToll.isTollGate(2882, 3268, 3227, 1), "Wrong plane accepted");

            // The actual second object action must be wired, not just the guard dialogue.
            checkPayTollClick(2882, 3227, 3267, 0, 20, 10, true);
            checkPayTollClick(2883, 3228, 3268, 0, 20, 10, true);
            checkPayTollClick(2882, 3227, 3267, 0, 9, 9, false);
            checkPayTollClick(2883, 3228, 3268, 9, 20, 10, true);
            checkPayTollClick(2882, 3227, 3267, 1, 0, 0, true);
            checkPayTollClick(2883, 3228, 3268, 1, 20, 20, true);
            System.out.println("Al Kharid gate toll checks passed (both sides, funds, quest stage, free passage).");
            System.exit(0);
        } catch (Throwable failure) {
            failure.printStackTrace();
            System.exit(1);
        }
    }

    private static void checkPayTollClick(int gateId, int gateY, int playerX,
                                          int questStage, int coins, int expectedCoins,
                                          boolean shouldCross) throws Exception {
        ObjectManager.activeDynamicObjects.clear();
        Player player = new Player(null);
        player.setIndex(1);
        player.setEncodedIndex(32768);
        socket.set(player, transport);
        player.setOutboundCipher(new IsaacCipher(new int[4]));
        player.setPosition(new Position(playerX, gateY, 0));
        player.setQuestState(11, questStage);
        if (coins > 0) {
            player.getInventoryManager().getContainer().setItem(0, new ItemStack(995, coins));
        }
        player.setInteractionTargetId(gateId);
        player.setInteractionTargetX(3268);
        player.setInteractionTargetY(gateY);
        player.setInteractionTargetPlane(0);
        ObjectManager.prepareObjectInteractionMovement(player, gateId, 3268, gateY);
        WorldObject gate = com.rs2.model.skill.SkillActionHelper.findWorldObjectById(gateId, 3268, gateY, 0);
        require(gate != null, "Gate not found for second-click");
        require(InteractionDispatcher.canReachObjectInteraction(player, gate),
                "Pay-toll cannot be reached from x=" + playerX);

        new SecondObjectActionTask(1, true, player, player.nextActionSequence(),
                gateId, 3268, gateY, 0).execute();
        require(player.getInventoryManager().getItemAmount(995) == expectedCoins,
                "Wrong toll for quest stage " + questStage + " from x=" + playerX);
        require(player.forcedMovementActive == shouldCross,
                "Unexpected gate traversal for quest stage " + questStage + " from x=" + playerX);
        if (shouldCross) {
            player.getMovementQueue().process();
            require(player.getPosition().getX() == (playerX < 3268 ? playerX + 1 : playerX - 1),
                    "Crossing did not move through the gate from x=" + playerX);
        }
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
