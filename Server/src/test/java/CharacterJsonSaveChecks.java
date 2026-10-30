import com.rs2.model.item.ItemStack;
import com.rs2.model.player.CharacterFileBankTab;
import com.rs2.model.player.CharacterFileRecord;
import com.rs2.util.CharacterJsonCodec;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Arrays;

/** Execute after building the server, from Server/:
 * java -cp "build-release/classes;lib/*" CharacterJsonSaveChecks
 */
public final class CharacterJsonSaveChecks {
    private static void require(boolean ok, String description) {
        if (!ok) throw new AssertionError(description);
    }

    public static void main(String[] args) throws Exception {
        File directory = Files.createTempDirectory("soloscape-json-checks-").toFile();
        File save = new File(directory, "TestHero.json");
        try {
            CharacterFileRecord record = new CharacterFileRecord();
            record.username = "TestHero";
            record.password = "test-only-password";
            record.playerRights = 2;
            record.x = 3210;
            record.y = 3222;
            record.plane = 0;
            record.musicVolume = 85;
            record.inventoryItems[0] = new ItemStack(995, 123456, -1);
            record.equipmentItems[1] = new ItemStack(1359, 1, 7);
            record.bankTabs.clear();
            record.bankTabs.add(CharacterFileBankTab.fromItems(
                    Arrays.asList(new ItemStack(995, 2000, -1), null)));
            record.skillExperience[0] = 737627L;
            record.skillExperience[1] = 1900000000L;
            if (record.questProgress.length > 0) record.questProgress[0] = 42;
            record.botCombatLoadoutItemIds.add(995);
            CharacterJsonCodec.save(save, record);
            require(save.isFile(), "No JSON save file created");
            String json = new String(Files.readAllBytes(save.toPath()), StandardCharsets.UTF_8);
            require(json.contains("\"playerRights\": 2"), "Player rights not readable");
            require(json.contains("\"inventoryItems\""), "Inventory field missing");
            require(json.contains("\"questProgress\""), "Quest field missing");
            require(!new File(directory, "TestHero.dat").exists(),
                    "Saving JSON must not create a legacy binary file");

            CharacterFileRecord restored = CharacterJsonCodec.read(save, "TestHero");
            require(restored.playerRights == 2, "Rights changed during JSON round-trip");
            require(restored.x == 3210 && restored.y == 3222, "Position corrupted");
            require(restored.skillExperience[0] == 737627L
                    && restored.skillExperience[1] == 1900000000L, "XP corrupted");
            require(restored.inventoryItems[0].getAmount() == 123456, "Inventory corrupted");
            require(restored.equipmentItems[1].getMetadata() == 7, "Item metadata corrupted");
            CharacterFileBankTab tab = (CharacterFileBankTab) restored.bankTabs.get(0);
            require(((ItemStack) tab.getItems().get(0)).getAmount() == 2000,
                    "Bank tab contents corrupted");
            require(tab.getItems().get(1) == null, "Bank null-slot alignment changed");
            require(restored.botCombatLoadoutItemIds.size() == 1, "Bot preferences corrupted");

            // Human editing must work without binary-file tooling.
            Files.write(save.toPath(),
                    json.replace("\"playerRights\": 2", "\"playerRights\": 1")
                            .getBytes(StandardCharsets.UTF_8));
            require(CharacterJsonCodec.read(save, "TestHero").playerRights == 1,
                    "Hand-edited rights were ignored");
            boolean failed = false;
            try { CharacterJsonCodec.read(save, "DifferentUser"); }
            catch (IOException expected) { failed = true; }
            require(failed, "Wrong-character JSON must not be loaded");

            // Updating an existing character should keep a previous JSON snapshot.
            CharacterJsonCodec.save(save, record);
            File backup = new File(directory, "TestHero.json.bak");
            require(backup.isFile(), "Updating JSON did not create a backup");
            require(new String(Files.readAllBytes(backup.toPath()), StandardCharsets.UTF_8)
                    .contains("\"playerRights\": 1"), "Backup was overwritten incorrectly");

            String broken = new String(Files.readAllBytes(save.toPath()), StandardCharsets.UTF_8)
                    .replace("\"playerRights\": 2", "\"playerRights\": \"admin\"");
            Files.write(save.toPath(), broken.getBytes(StandardCharsets.UTF_8));
            failed = false;
            try { CharacterJsonCodec.read(save, "TestHero"); }
            catch (IOException expected) { failed = true; }
            require(failed, "Invalid numeric rights should fail closed");
            System.out.println("JSON character save checks passed.");
        } finally {
            for (File file : directory.listFiles()) Files.deleteIfExists(file.toPath());
            Files.deleteIfExists(directory.toPath());
        }
    }
}
