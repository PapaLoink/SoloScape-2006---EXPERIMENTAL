package unpackaged;

import com.rs2.model.World;
import com.rs2.model.combat.WeaponProfile;
import com.rs2.model.combat.special.SpecialAttackDefinition;
import com.rs2.model.item.ItemDefinition;
import com.rs2.model.item.ItemStack;
import com.rs2.model.player.Player;
import com.rs2.model.quest.QuestDefinition;
import com.rs2.net.packet.IncomingPacket;
import com.rs2.net.packet.InterfaceBridge;
import com.rs2.net.packet.PacketBuffer;
import com.rs2.net.packet.handler.ItemSpawnPacketHandler;
import java.lang.reflect.Field;
import java.nio.ByteBuffer;

/** Removed features stay unavailable while old item metadata remains readable. */
public final class RemovedContentChecks {
    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }

    private static void absent(String name) throws Exception {
        try {
            Class.forName(name);
            throw new AssertionError("Removed feature still present: " + name);
        } catch (ClassNotFoundException expected) {
            // Feature implementation was deleted.
        }
    }

    public static void main(String[] args) throws Exception {
        QuestDefinition.loadDefinitions();
        ItemDefinition.loadDefinitions();
        // Treat the entire supported item range as defined: removed IDs must
        // remain blocked even when an extended cache contains their definitions.
        Field count = ItemDefinition.class.getDeclaredField("definitionCount");
        count.setAccessible(true);
        count.setInt(null, 20000);
        Player player = new Player(null);
        player.isBot = true;
        player.setOpenInterfaceId(19103);
        Class39_Sub11.anInt1478 = 505;
        int[] weapons = {11235, 11236, 11686, 11687, 11688, 11689, 11690, 11691,
                11692, 11693, 11694, 11695, 11696,
                11697, 11698, 11699, 11700, 11701, 11702, 11703, 11704,
                11705, 11706, 11707, 11708, 11709, 11710, 11711, 11712,
                11713, 11714, 11715};
        for (int id : weapons) {
            check(ItemDefinition.isRemovedContent(id), "removed item policy: " + id);
            check(!ItemDefinition.isDefined(id), "server cannot spawn or drop removed item: " + id);
            check(ItemDefinition.forId(id) != null, "old save metadata stays readable: " + id);
            check(!GrandExchangeSearch.isSearchable(id), "item picker hides removed item: " + id);
            check(!player.getEquipmentManager().canEquipItem(id), "removed item cannot be equipped: " + id);
            check(WeaponProfile.forItem(new ItemStack(id)) == WeaponProfile.FISTS,
                    "removed item has no combat profile: " + id);
            check(SpecialAttackDefinition.forItem(new ItemStack(id)) == null,
                    "removed item has no special attack: " + id);
            ByteBuffer payload = ByteBuffer.allocate(2).putShort((short)id);
            payload.flip();
            new ItemSpawnPacketHandler().handle(player,
                    new IncomingPacket(19, 2, PacketBuffer.wrapReader(payload)));
            player.handleCommand("sitem", new String[]{String.valueOf(id), "1"}, id + " 1");
            check(!player.getInventoryManager().containsItem(id), "picker and spawn command reject: " + id);
        }
        check(ItemDefinition.isDefined(1205) && !ItemDefinition.isRemovedContent(1205),
                "ordinary dagger remains available");
        check(!ItemDefinition.isRemovedContent(11684) && !ItemDefinition.isRemovedContent(11716),
                "unrelated adjacent item IDs remain available");
        player.getInventoryManager().getContainer().setItem(0, new ItemStack(11235));
        player.getEquipmentManager().equipFromInventorySlot(0);
        check(player.getEquipmentManager().getItemIdAtSlot(3) != 11235,
                "legacy inventory item cannot bypass equip checks");

        absent("com.rs2.model.quest.event.ClueMerchantSpawnTask");
        absent("com.rs2.model.gameplay.godwars.GodWarsDungeonManager");
        absent("com.rs2.model.gameplay.godwars.BandosStrongholdDoorTask");
        absent("unpackaged.GodWarsWidgets");
        check(InterfaceBridge.translateGroup(19556) == InterfaceBridge.UNMAPPED,
                "removed dungeon overlay cannot be sent");
        for (int id = 19557; id <= 19565; id++) {
            check(InterfaceBridge.translate(id) == InterfaceBridge.UNMAPPED,
                    "removed overlay counter cannot be sent");
        }
        Class62_Sub1.widgets = new Widget[500][];
        Class39_Sub5_Sub4.widgetsLoaded = new boolean[500];
        Class68.method1112(19556, (byte)110);
        com.rs2.model.quest.event.ServerMaintenanceEventHook hook =
                new com.rs2.model.quest.event.ServerMaintenanceEventHook(0, 0);
        hook.initialize();
        check(World.getTaskScheduler().getTasks().isEmpty(), "removed merchant adds no spawn task");
        System.out.println("Removed content checks passed.");
        System.exit(0);
    }
}
