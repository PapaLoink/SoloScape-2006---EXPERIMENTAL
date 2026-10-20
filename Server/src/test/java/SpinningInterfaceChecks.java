import com.rs2.ServerSettings;
import com.rs2.cache.js5.Interfaces;
import com.rs2.model.GameplayHelper;
import com.rs2.model.interaction.InteractionType;
import com.rs2.model.interaction.ObjectActionRouter;
import com.rs2.model.item.ItemDefinition;
import com.rs2.model.item.ItemStack;
import com.rs2.model.objects.ObjectDefinition;
import com.rs2.model.player.Player;
import com.rs2.model.skill.crafting.SpinningRecipe;
import com.rs2.model.skill.crafting.SpinningTask;
import com.rs2.model.task.CycleEventContainer;
import com.rs2.net.packet.ClientPackets;
import com.rs2.net.packet.IncomingPacket;
import com.rs2.net.packet.InterfaceBridge;
import com.rs2.net.packet.PacketBuffer;
import com.rs2.net.packet.handler.InterfaceActionPacketHandler;
import com.rs2.net.packet.handler.InterfaceInputPacketHandler;
import java.lang.reflect.Field;
import java.nio.ByteBuffer;

/** Cache-backed regression for issue #2: native 443 spinning-wheel interface. */
public final class SpinningInterfaceChecks {
    private static final int LEGACY_ROOT = 8880;
    private static final int NATIVE_GROUP = 459;

    public static void main(String[] args) throws Exception {
        ServerSettings.clientBuild = 443;
        ItemDefinition.loadDefinitions();
        ObjectDefinition.loadRevision443();
        Interfaces.load();

        ObjectDefinition wheel = ObjectDefinition.forId(2644);
        require("Spinning wheel".equals(wheel.getName()), "Wheel object 2644 changed");
        require("Spin".equals(wheel.getAction(1)), "Wheel second option is not Spin");
        require(ObjectActionRouter.legacyRoute(2) == InteractionType.SECOND_OBJECT,
                "Spin no longer reaches the second-object handler");

        require(InterfaceBridge.translateGroup(LEGACY_ROOT) == NATIVE_GROUP,
                "Spinning interface is not mapped to the 443 native group");
        require(InterfaceBridge.translateGroup(8866) == NATIVE_GROUP,
                "Two-product spinning fallback is not mapped");
        require(Interfaces.group(NATIVE_GROUP).size() == 127, "Native spinning group changed");
        int[][] buttons = {
                {92, 8890}, {93, 8891}, {94, 8892}, {95, 8893},
                {97, 8886}, {98, 8887}, {99, 8888}, {100, 8889},
                {104, 8894}, {105, 8895}, {106, 8896}, {107, 8897}
        };
        for (int[] button : buttons) {
            int packed = NATIVE_GROUP << 16 | button[0];
            require(Interfaces.forId(NATIVE_GROUP, button[0]).actionType == 1,
                    "Cache action type changed for native spinning child " + button[0]);
            require(InterfaceBridge.toLegacyComponent(packed) == button[1],
                    "Wrong reverse mapping for native spinning child " + button[0]);
            require(InterfaceBridge.translate(button[1]) == packed,
                    "Wrong forward mapping for spinning button " + button[1]);
            require(SpinningRecipe.forButtonId(button[1]) != null,
                    "Missing spinning recipe for button " + button[1]);
        }

        Player player = newPlayer();
        GameplayHelper.openProductionInterface(player, "spinning");
        require(player.getOpenInterfaceId() == LEGACY_ROOT,
                "Spinning wheel failed to open the native interface");
        require(player.isInterfaceIdOpen(8889), "Make 1 rejected despite spinning screen open");
        player.setOpenInterfaceId(6179);
        require(!player.isInterfaceIdOpen(8889), "Make 1 accepted without spinning screen");

        // Check all quantity modes via the actual native 459 widget click path.
        for (int child : new int[]{100, 99, 98, 97}) {
            player = newPlayer();
            player.getInventoryManager().addItem(new ItemStack(1737, 28));
            player.setOpenInterfaceId(LEGACY_ROOT);
            player.setInterfaceAction("spinning");
            clickNative(player, child);
            if (child == 97) {
                require(player.getSelectedInterfaceId() == 8886, "Make X did not request an amount");
                enterAmount(player, 3);
            }
            require(player.getActiveCycleEvent() instanceof SpinningTask,
                    "Spinning task not scheduled by native child " + child);
            int expected = child == 100 ? 1 : child == 99 ? 5 : child == 98 ? 10 : 3;
            require(remainingActions((SpinningTask)player.getActiveCycleEvent()) == expected,
                    "Wrong spinning quantity for child " + child);
        }

        // Spin 1 must actually convert wool into a ball and award Crafting XP.
        player = newPlayer();
        player.getInventoryManager().addItem(new ItemStack(1737, 2));
        player.setOpenInterfaceId(LEGACY_ROOT);
        player.setInterfaceAction("spinning");
        double xpBefore = player.getSkillManager().getExperience()[12];
        clickNative(player, 100);
        new CycleEventContainer(player, player.getActiveCycleEvent(), 3).execute();
        require(player.getInventoryManager().getItemAmount(1737) == 1,
                "One spinning tick did not consume wool");
        require(player.getInventoryManager().getItemAmount(1759) == 1,
                "One spinning tick did not produce a ball of wool");
        require(player.getSkillManager().getExperience()[12] > xpBefore,
                "Spinning did not grant Crafting experience");

        // Second ingredient and buttons work with the same native interface.
        player = newPlayer();
        player.getInventoryManager().addItem(new ItemStack(1779, 5));
        player.setOpenInterfaceId(LEGACY_ROOT);
        player.setInterfaceAction("spinning");
        clickNative(player, 95);
        require(player.getActiveCycleEvent() instanceof SpinningTask,
                "Native flax Make 1 did not schedule bow-string crafting");

        player = newPlayer();
        player.getInventoryManager().addItem(new ItemStack(1737, 5));
        player.setOpenInterfaceId(6179);
        player.setInterfaceAction("spinning");
        clickNative(player, 100);
        require(player.getActiveCycleEvent() == null,
                "Spinning button accepted with unrelated screen open");

        System.out.println("Spinning interface checks passed: cache, mapping, quantities, XP and closed-screen guard.");
    }

    private static Player newPlayer() {
        Player player = new Player(null);
        player.isBot = true; // Avoid socket output in the detached server fixture.
        player.setQuestState(0, 1);
        player.getSkillManager().getCurrentLevels()[12] = 99;
        return player;
    }

    private static void clickNative(Player player, int child) {
        ByteBuffer payload = ByteBuffer.allocate(4);
        payload.putInt(NATIVE_GROUP << 16 | child);
        payload.flip();
        new InterfaceActionPacketHandler().handle(player,
                new IncomingPacket(ClientPackets.INTERFACE_BUTTON, 4,
                        PacketBuffer.wrapReader(payload)));
    }

    private static void enterAmount(Player player, int amount) {
        ByteBuffer payload = ByteBuffer.allocate(4).putInt(amount);
        payload.flip();
        new InterfaceInputPacketHandler().handle(player,
                new IncomingPacket(208, 4, PacketBuffer.wrapReader(payload)));
    }

    private static int remainingActions(SpinningTask task) throws Exception {
        Field field = SpinningTask.class.getDeclaredField("remainingActions");
        field.setAccessible(true);
        return field.getInt(task);
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
