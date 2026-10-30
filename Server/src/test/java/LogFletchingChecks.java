import com.rs2.ServerSettings;
import com.rs2.cache.js5.Interfaces;
import com.rs2.model.item.ItemDefinition;
import com.rs2.model.item.ItemStack;
import com.rs2.model.player.Player;
import com.rs2.model.skill.fletching.logs.LogFletchingMenu;
import com.rs2.model.skill.fletching.logs.LogFletchingTask;
import com.rs2.model.task.CycleEventContainer;
import com.rs2.net.packet.*;
import com.rs2.net.packet.handler.InterfaceActionPacketHandler;
import com.rs2.net.packet.handler.InterfaceInputPacketHandler;
import com.rs2.net.packet.handler.ItemActionPacketHandler;
import java.nio.ByteBuffer;

/** Knife-on-logs must open 443 model choices with per-item Make X/10/5/1. */
public final class LogFletchingChecks {
    public static void main(String[] args) throws Exception {
        ServerSettings.clientBuild = 443;
        ServerSettings.membershipRequirementMode = 0;
        ServerSettings.freeToPlayWorld = false;
        ItemDefinition.loadDefinitions();
        Interfaces.load();

        // Historic 377 interface 8880 is a chatbox, but native 443 group 459
        // is full-screen spinning. Fletching needs the stock 303/304/309
        // one/two/three-product chatboxes, each with four native Make options.
        require(InterfaceBridge.translateGroup(8880) == 459, "Spinning route changed");
        int[][] layouts = {
            {20122, 309, 1, 3, 20210, 20211},
            {20121, 303, 2, 4, 20206, 20208},
            {20120, 304, 3, 5, 20200, 20203}
        };
        for (int[] layout : layouts) {
            int root = layout[0], group = layout[1], count = layout[2],
                    firstAction = layout[3], firstModel = layout[4], firstName = layout[5];
            require(InterfaceBridge.translateGroup(root) == group, "Wrong native chatbox group");
            for (int slot = 0; slot < count; slot++) {
                int childModel = 2 + slot;
                int title = count == 3 ? 17 : count == 2 ? 12 : 7;
                int childName = title + 1 + slot;
                require(Interfaces.forId(group, childModel).type == 6, "Fletching model missing");
                require(InterfaceBridge.translate(firstModel + slot) == (group << 16 | childModel),
                        "Wrong item model mapping");
                require(InterfaceBridge.translate(firstName + slot) == (group << 16 | childName),
                        "Wrong item caption mapping");
                for (int kind = 0; kind < 4; kind++) {
                    int child = firstAction + slot * 4 + kind;
                    require(Interfaces.forId(group, child).actionType == 6,
                            "Native Make X/10/5/1 chatbox action missing");
                }
            }
        }

        int[][] recipes = {
            {1511, 0, 52}, {1511, 1, 50}, {1511, 2, 48},
            {1521, 0, 54}, {1521, 1, 56}, {2862, 0, 4825},
            {1519, 0, 60}, {1519, 1, 58}, {1517, 0, 64}, {1517, 1, 62},
            {1515, 0, 68}, {1515, 1, 66}, {1513, 0, 72}, {1513, 1, 70}
        };
        for (int[] recipe : recipes) {
            for (boolean reverse : new boolean[]{false, true}) {
                Player player = newPlayer(recipe[0], 3);
                useKnife(player, reverse);
                requireNativeMenu(player, recipe[0]);
                double xp = player.getSkillManager().getExperience()[9];
                click(player, recipe[1], 3); // Make 1, for every product
                require(player.getActiveCycleEvent() instanceof LogFletchingTask, "No fletching task");
                tick(player);
                require(player.getInventoryManager().getItemAmount(recipe[0]) == 2, "Log not consumed");
                require(player.getInventoryManager().getItemAmount(recipe[2]) == (recipe[2] == 52 ? 15 : 1),
                        "Wrong product for log " + recipe[0] + " choice " + recipe[1]);
                require(player.getInventoryManager().containsItem(946), "Knife consumed");
                require(player.getSkillManager().getExperience()[9] > xp, "No fletching XP");
            }
        }

        // Make 1, 5, 10, and X all use the same menu and a specific item slot.
        for (int kind = 0; kind < 4; kind++) {
            for (int product = 0; product < 3; product++) {
                Player player = newPlayer(1511, 12);
                useKnife(player, false);
                requireNativeMenu(player, 1511);
                click(player, product, kind);
                int count = new int[]{7, 10, 5, 1}[kind];
                if (kind == 0) {
                    require(player.getSelectedInterfaceId() == 8886 + product * 4,
                            "Make-X did not request the selected product's quantity");
                    enterAmount(player, count);
                }
                require(player.getActiveCycleEvent() instanceof LogFletchingTask, "No quantity task");
                for (int tick = 0; tick < 34; tick++) {
                    if (player.getActiveCycleEvent() == null) break;
                    tick(player);
                }
                int item = new int[]{52, 50, 48}[product];
                require(player.getInventoryManager().getItemAmount(item) ==
                        (item == 52 ? count * 15 : count),
                        "Wrong quantity: product " + product + " kind " + kind);
            }
        }

        // The two-item Oak menu and the single-item Achey menu use distinct
        // native widgets. Their Make-X requests must select the right recipe.
        for (int log : new int[]{1521, 2862}) {
            int slots = log == 1521 ? 2 : 1;
            for (int slot = 0; slot < slots; slot++) {
                Player xPlayer = newPlayer(log, 9);
                useKnife(xPlayer, false);
                requireNativeMenu(xPlayer, log);
                click(xPlayer, slot, 0); // Native Make X, including 309:4.
                require(xPlayer.getSelectedInterfaceId() == 8886 + slot * 4,
                        "Make-X did not select " + log + " recipe " + slot);
                enterAmount(xPlayer, 6);
                require(xPlayer.getActiveCycleEvent() instanceof LogFletchingTask,
                        "Make-X did not start for " + log + " recipe " + slot);
                for (int ticks = 0; ticks < 45; ticks++) {
                    if (xPlayer.getActiveCycleEvent() == null) break;
                    tick(xPlayer);
                }
                int item = log == 2862 ? 4825 : slot == 0 ? 54 : 56;
                require(xPlayer.getInventoryManager().getItemAmount(item) == 6,
                        "Make-X produced wrong count on " + log + " recipe " + slot);
            }
        }
        // Group 309 has Make All at child 3; do not mistake it for Make X.
        Player allPlayer = newPlayer(2862, 3);
        useKnife(allPlayer, false);
        int allPacked = (309 << 16) | 3;
        ByteBuffer allBytes = ByteBuffer.allocate(6).putShort((short)0).putInt(allPacked);
        allBytes.flip();
        new InterfaceActionPacketHandler().handle(allPlayer,
                new IncomingPacket(ClientPackets.WIDGET_SELECT, 6,
                        PacketBuffer.wrapReader(allBytes)));
        require(allPlayer.getSelectedInterfaceId() == -1,
                "Make All incorrectly opened an amount prompt");
        require(allPlayer.getActiveCycleEvent() instanceof LogFletchingTask,
                "Achey Make All failed to begin");

        Player player = newPlayer(1511, 2);
        useKnife(player, false);
        click(player, 1, 3); // Shortbow needs Fletching 5.
        require(player.getActiveCycleEvent() instanceof LogFletchingTask,
                "Valid fletching failed");
        player = newPlayer(1511, 2);
        useKnife(player, false);
        player.getSkillManager().getCurrentLevels()[9] = 1;
        click(player, 1, 3);
        require(player.getActiveCycleEvent() == null, "Low level accepted");

        player = newPlayer(1513, 2);
        useKnife(player, false);
        player.getSkillManager().getCurrentLevels()[9] = 79;
        click(player, 0, 2);
        require(player.getActiveCycleEvent() == null, "Magic batch bypassed level 80");

        player = newPlayer(1511, 2);
        useKnife(player, false);
        player.getInventoryManager().removeItem(new ItemStack(946));
        click(player, 0, 3);
        require(player.getActiveCycleEvent() == null, "Missing knife accepted");

        player = newPlayer(1511, 2);
        useKnife(player, false);
        player.packetSender.closeInterfaces();
        click(player, 0, 3);
        require(player.getActiveCycleEvent() == null, "Closed menu accepted");

        player = newPlayer(1511, 2);
        useKnife(player, false);
        ServerSettings.fletchingEnabled = false;
        click(player, 0, 3);
        require(player.getActiveCycleEvent() == null, "Disabled skill accepted");
        ServerSettings.fletchingEnabled = true;

        // The right-most slot must not be craftable on two-product log types.
        player = newPlayer(1521, 2);
        useKnife(player, false);
        click(player, 2, 3);
        require(player.getActiveCycleEvent() == null, "Hidden product crafted");

        // Make-X must not restart from a consumed or invalid quantity prompt.
        player = newPlayer(1511, 2);
        useKnife(player, false);
        click(player, 2, 0);
        enterAmount(player, 0);
        require(player.getActiveCycleEvent() == null, "Zero Make-X quantity accepted");

        player = newPlayer(1511, 2);
        LogFletchingMenu.restoreSpinningInterface(player);
        require(player.getOpenInterfaceId() == 8880 && "spinning".equals(player.interfaceAction),
                "Spinning screen was not restored");
        require(!LogFletchingMenu.handleNativeWidget(player, 304 << 16 | 8),
                "Spinning Make 1 intercepted by fletching");
        player.setInterfaceAction("unrelated");
        require(!LogFletchingMenu.handleNativeWidget(player, 304 << 16 | 8),
                "Unrelated production action intercepted");
        ServerSettings.clientBuild = 377;
        require(!LogFletchingMenu.open(player, "normalCutting"), "Legacy build intercepted");

        System.out.println("Log fletching checks passed: 443 chatbox item models/labels, every recipe, "
                + "per-product Make X/10/5/1, single-item Make All, guards and spinning isolation.");
    }

    private static Player newPlayer(int log, int count) {
        Player player = new Player(null);
        player.isBot = true; // Avoid network packets in this cache-backed fixture.
        player.setQuestState(0, 1);
        player.getSkillManager().getCurrentLevels()[9] = 99;
        player.getInventoryManager().addItem(new ItemStack(946));
        player.getInventoryManager().addItem(new ItemStack(log, count));
        return player;
    }

    private static void useKnife(Player player, boolean reverse) {
        int selectedSlot = reverse ? 1 : 0;
        int targetSlot = reverse ? 0 : 1;
        int selectedId = player.getInventoryManager().getContainer().getItemAt(selectedSlot).getId();
        int targetId = player.getInventoryManager().getContainer().getItemAt(targetSlot).getId();
        int widget = 149 << 16;
        ByteBuffer bytes = ByteBuffer.allocate(16);
        bytes.put((byte)(selectedSlot >> 8)).put((byte)(selectedSlot + 128));
        bytes.put((byte)selectedId).put((byte)(selectedId >> 8));
        bytes.put((byte)targetId).put((byte)(targetId >> 8));
        bytes.put((byte)widget).put((byte)(widget >> 8)).put((byte)(widget >> 16)).put((byte)(widget >> 24));
        bytes.put((byte)(widget >> 16)).put((byte)(widget >> 24)).put((byte)widget).put((byte)(widget >> 8));
        bytes.putShort((short)targetSlot).flip();
        new ItemActionPacketHandler().handle(player,
                new IncomingPacket(ClientPackets.ITEM_ON_ITEM, 16, PacketBuffer.wrapReader(bytes)));
    }

    private static void requireNativeMenu(Player player, int log) {
        int count = log == 1511 ? 3 : log == 2862 ? 1 : 2;
        int root = count == 3 ? 20120 : count == 2 ? 20121 : 20122;
        int group = count == 3 ? 304 : count == 2 ? 303 : 309;
        require(player.getOpenInterfaceId() == root,
                "Knife did not open native make-item chatbox for log " + log);
        require(InterfaceBridge.translateGroup(root) == group, "Wrong native chatbox");
    }

    private static void click(Player player, int product, int kind) {
        int count = player.getOpenInterfaceId() == 20120 ? 3
                : player.getOpenInterfaceId() == 20121 ? 2 : 1;
        int group = count == 3 ? 304 : count == 2 ? 303 : 309;
        int first = count == 3 ? 5 : count == 2 ? 4 : 3;
        // Native 309 has Make All first and Make X second.
        int packed = group << 16 | first + product * 4 + kind
                + (count == 1 && kind == 0 ? 1 : 0);
        ByteBuffer bytes = ByteBuffer.allocate(6).putShort((short)0).putInt(packed);
        bytes.flip();
        new InterfaceActionPacketHandler().handle(player,
                new IncomingPacket(ClientPackets.WIDGET_SELECT, 6, PacketBuffer.wrapReader(bytes)));
    }

    private static void enterAmount(Player player, int amount) {
        ByteBuffer bytes = ByteBuffer.allocate(4).putInt(amount);
        bytes.flip();
        new InterfaceInputPacketHandler().handle(player,
                new IncomingPacket(208, 4, PacketBuffer.wrapReader(bytes)));
    }

    private static void tick(Player player) {
        require(player.getActiveCycleEvent() != null, "Missing task");
        new CycleEventContainer(player, player.getActiveCycleEvent(), 1).execute();
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
