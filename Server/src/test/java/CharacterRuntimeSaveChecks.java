import com.rs2.ServerSettings;
import com.rs2.model.item.ItemStack;
import com.rs2.model.player.CharacterFileRecord;
import com.rs2.model.player.CharacterFileBankTab;
import com.rs2.model.player.Player;
import com.rs2.util.CharacterFileManager;
import com.rs2.util.CharacterJsonCodec;
import java.io.File;
import java.lang.reflect.Field;
import java.nio.file.Files;

/** Checks the actual live Player -> legacy staging -> JSON save path. */
public final class CharacterRuntimeSaveChecks {
    private static void require(boolean ok, String message) {
        if (!ok) throw new AssertionError(message);
    }

    public static void main(String[] args) throws Exception {
        ServerSettings.sqlitePlayerSaveEnabled = false;
        ServerSettings.startX = 3210;
        ServerSettings.startY = 3222;
        ServerSettings.startPlane = 0;
        String username = "SaveProbe" + Long.toHexString(System.nanoTime() & 0xffff);
        File save = new File("./data/characters/" + username + ".json");
        File backup = new File("./data/characters/" + username + ".json.bak");
        File legacy = new File("./data/characters/" + username + ".dat");
        File staging = new File("./data/characters/.json-staging/" + username + ".dat");
        require(!save.exists() && !legacy.exists(), "Refusing to overwrite a test character");
        try {
            Player player = new Player(null);
            player.setUsername(username);
            player.setPassword("save-test-only");
            Field hostAddress = Player.class.getDeclaredField("hostAddress");
            hostAddress.setAccessible(true);
            hostAddress.set(player, "127.0.0.1");
            player.getInventoryManager().getContainer().setItem(0, new ItemStack(995, 123, -1));
            player.getBankContainer().setTabItem(0, new ItemStack(995, 456, -1), 0);
            player.getSkillManager().getExperience()[0] = 15000.0;

            require(CharacterFileManager.savePlayer(player), "Live player save failed");
            require(save.isFile(), "No character JSON written by live save path");
            CharacterFileRecord record = CharacterJsonCodec.read(save, username);
            require(record.inventoryItems[0] != null &&
                    record.inventoryItems[0].getAmount() == 123, "Inventory not persisted");
            require(!record.bankTabs.isEmpty(), "No bank tabs persisted");
            CharacterFileBankTab tab = (CharacterFileBankTab) record.bankTabs.get(0);
            require(!tab.getItems().isEmpty() && tab.getItems().get(0) != null &&
                    ((ItemStack) tab.getItems().get(0)).getAmount() == 456,
                    "Bank deposit not persisted");
            require(record.skillExperience[0] == 15000L, "XP not persisted");
            require(record.x == 3210 && record.y == 3222, "Position not persisted");

            // Recreate the Player, as happens on the next login, and reload
            // through the server's real character-loading method.
            Player restored = new Player(null);
            restored.setUsername(username);
            CharacterFileManager.loadPlayerFromFile("./data/characters/", restored);
            ItemStack restoredBankItem = restored.getBankContainer().getItemAtTabSlot(0, 0);
            ItemStack restoredInventoryItem = restored.getInventoryManager().getContainer().getItemAt(0);
            require(restoredBankItem != null && restoredBankItem.getAmount() == 456,
                    "Bank deposit not restored on login");
            require(restoredInventoryItem != null && restoredInventoryItem.getAmount() == 123,
                    "Inventory not restored on login");
            require(restored.getSkillManager().getExperience()[0] == 15000.0,
                    "XP not restored on login");
            require(restored.getPosition().getX() == 3210
                    && restored.getPosition().getY() == 3222,
                    "Position not restored on login");
            System.out.println("Live character save and login reload checks passed.");
        } finally {
            Files.deleteIfExists(save.toPath());
            Files.deleteIfExists(backup.toPath());
            Files.deleteIfExists(legacy.toPath());
            Files.deleteIfExists(staging.toPath());
        }
    }
}
