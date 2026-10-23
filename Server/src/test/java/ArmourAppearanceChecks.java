import com.rs2.ServerSettings;
import com.rs2.model.item.ItemDefinition;
import com.rs2.model.item.ItemStack;
import com.rs2.model.player.ModernPlayerUpdateTask;
import com.rs2.model.player.Player;
import com.rs2.model.quest.QuestDefinition;
import com.rs2.net.packet.PacketWriter;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.util.Arrays;

/** Decode the actual 443 appearance payload, using the game's item definitions. */
public final class ArmourAppearanceChecks {
    public static void main(String[] args) throws Exception {
        ServerSettings.clientBuild = 443;
        QuestDefinition.loadDefinitions();
        ItemDefinition.loadDefinitions();
        Method build = ModernPlayerUpdateTask.class.getDeclaredMethod("buildAppearance", Player.class);
        build.setAccessible(true);
        Field gender = Player.class.getDeclaredField("gender");
        gender.setAccessible(true);
        for (boolean bot : new boolean[] {false, true}) {
            for (int sex : new int[] {0, 1}) {
                Player player = new Player(null);
                player.isBot = bot;
                gender.setInt(player, sex);
                int[] parts = player.getAppearanceParts();
                for (int i = 0; i < parts.length; i++) parts[i] = 20 + i;
                int[] bare = {0, 0, 0, 0, 276, 0, 277, 278, 279, 280, 281, sex == 0 ? 282 : 0};
                check(build, player, bare);
                // Bronze full helm/platebody/legs, cape, amulet, dagger, shield, gloves, boots.
                int[] slots = {0, 1, 2, 3, 4, 5, 7, 9, 10};
                int[] items = {1155, 1007, 1704, 1205, 1117, 1173, 1075, 1059, 1061};
                for (int i = 0; i < slots.length; i++) {
                    require(ItemDefinition.forId(items[i]).getEquipmentSlot() == slots[i], "Bad fixture " + items[i]);
                    player.getEquipmentManager().getContainer().setItem(slots[i], new ItemStack(items[i]));
                }
                int[] armoured = {1667, 1519, 2216, 1717, 1629, 1685, 0, 1587, 0, 1571, 1573, 0};
                check(build, player, armoured);
                player.setHideHeldItemsInAppearance(true);
                armoured[3] = armoured[5] = 0;
                check(build, player, armoured);
                player.setHideHeldItemsInAppearance(false);
                // A chainbody leaves sleeves visible; a medium helm hides hair but keeps the beard.
                player.getEquipmentManager().getContainer().setItem(4, new ItemStack(1103));
                player.getEquipmentManager().getContainer().setItem(0, new ItemStack(1139));
                armoured[0] = 1651; armoured[3] = 1717; armoured[4] = 1615; armoured[5] = 1685;
                armoured[6] = 277; armoured[8] = 0; armoured[11] = sex == 0 ? 282 : 0;
                check(build, player, armoured);
                // The native Christmas snowball has a worn model for both genders.
                player.getEquipmentManager().getContainer().setItem(3, new ItemStack(10501, 105));
                armoured[3] = 512 + 10501;
                check(build, player, armoured);
                player.setHideHeldItemsInAppearance(true);
                armoured[3] = armoured[5] = 0;
                check(build, player, armoured);
                player.setHideHeldItemsInAppearance(false);
                armoured[3] = 512 + 10501; armoured[5] = 1685;
                check(build, player, armoured);
                for (int slot : slots) player.getEquipmentManager().getContainer().setItem(slot, null);
                check(build, player, bare);
            }
        }
        System.out.println("Armour appearance checks passed: players/bots, both genders, all visible slots, helmets, sleeves, hidden held items, unequip, packet alignment.");
        System.exit(0);
    }

    private static void check(Method build, Player player, int[] expected) throws Exception {
        ByteBuffer buffer = ((PacketWriter) build.invoke(null, player)).getBuffer();
        buffer.flip();
        require((buffer.get() & 255) == player.getGender(), "Wrong gender");
        buffer.get(); buffer.get();
        int[] actual = new int[12];
        for (int i = 0; i < actual.length; i++) {
            int high = buffer.get() & 255;
            actual[i] = high == 0 ? 0 : (high << 8) | (buffer.get() & 255);
        }
        require(Arrays.equals(actual, expected), "Appearance slots: " + Arrays.toString(actual));
        for (int color : player.getAppearanceColors()) require((buffer.get() & 255) == color, "Shifted colors");
        require((buffer.getShort() & 65535) == player.getStandAnimation(), "Shifted animations");
        buffer.position(buffer.position() + 12);
        require(buffer.getLong() == player.getNameHash(), "Shifted name");
        require((buffer.get() & 255) == player.getCombatLevel(), "Shifted combat level");
        require(buffer.getShort() == 0 && !buffer.hasRemaining(), "Wrong packet tail");
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
