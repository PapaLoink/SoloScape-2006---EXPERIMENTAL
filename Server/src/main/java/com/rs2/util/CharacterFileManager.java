package com.rs2.util;

import com.rs2.CacheCoordinateTranslator;
import com.rs2.HiscoresDatabase;
import com.rs2.Server;
import com.rs2.ServerSettings;
import com.rs2.bot.route.BotWorldRouteWalker;
import com.rs2.model.Entity;
import com.rs2.model.GameplayHelper;
import com.rs2.model.World;
import com.rs2.model.clue.PuzzleBoxHandler;
import com.rs2.model.item.ItemStack;
import com.rs2.model.objects.functions.FlourMillHandler;
import com.rs2.model.player.CharacterFileBankTab;
import com.rs2.model.player.CharacterFileRecord;
import com.rs2.model.player.Player;
import com.rs2.model.quest.QuestDefinition;
import com.rs2.model.skill.farming.AllotmentPatchManager;
import com.rs2.model.skill.farming.BushPatchManager;
import com.rs2.model.skill.farming.CompostBinManager;
import com.rs2.model.skill.farming.FarmingToolStore;
import com.rs2.model.skill.farming.FlowerPatchManager;
import com.rs2.model.skill.farming.FruitTreePatchManager;
import com.rs2.model.skill.farming.HerbPatchManager;
import com.rs2.model.skill.farming.HopsPatchManager;
import com.rs2.model.skill.farming.SpecialCropPatchManager;
import com.rs2.model.skill.farming.SpecialTreePatchManager;
import com.rs2.model.skill.farming.TreePatchManager;
import com.rs2.model.skill.magic.Spellbook;
import com.rs2.net.LoginProtocol;
import com.rs2.net.packet.handler.AppearancePacketHandler;
import com.rs2.util.BackupRepairTimestampComparator;
import com.rs2.util.BackupRestoreTimestampComparator;
import com.rs2.util.ElapsedTimer;
import com.rs2.util.FileUtil;
import com.rs2.util.LoginIpReservation;
import com.rs2.util.PlayerLoginLoadCallback;
import com.rs2.util.PlayerUidLookupQuery;
import com.rs2.util.db.DatabaseQuery;
import com.rs2.util.db.DatabaseService;
import com.rs2.util.db.player.PlayerBankSaveCallback;
import com.rs2.util.db.player.PlayerContactsSaveCallback;
import com.rs2.util.db.player.PlayerEquipmentSaveCallback;
import com.rs2.util.db.player.PlayerInventorySaveCallback;
import com.rs2.util.db.player.PlayerProfileSaveCallback;
import com.rs2.util.db.player.PlayerSaveQueryFactory;
import com.rs2.util.db.player.PlayerSkillsSaveCallback;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilterInputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;
import org.joda.time.DateTime;
import org.joda.time.base.AbstractDateTime;

public final class CharacterFileManager {
    // Retain the retired rope slot to preserve legacy character-save layouts.
    private static final int LEGACY_GOD_WARS_ROPE_CONFIG_ID = 1048;
    public static int[] musicUnlockConfigIds = new int[]{20, 21, 22, 23, 24, 25, 298, 311, 346, 414, 464, 598, 662, 721};
    // Revision 443 unlock bits for the late-2006 tracks (Roc and Roll, High Spirits, Floating Free,
    // the three Christmas tracks and Where Eagles Lair). Saved after the older fields so existing files still load.
    public static final int LATE_MUSIC_UNLOCK_CONFIG_ID = 906;
    private static ConcurrentHashMap loginIpReservations = new ConcurrentHashMap();
    public static ArrayList deadHardcoreIronmanRecords = new ArrayList();
    public static ArrayList liveHiscoreRecords = new ArrayList();
    // Binary serializer is retained only for temporary compatibility staging.
    // The primary on-disk format is now the editable .json CharacterFileRecord.
    private static final ThreadLocal<File> stagedSaveTarget = new ThreadLocal<File>();

    public static boolean savePlayer(Player player) {
        if (player.isBot) {
            if (player.botMode != 4) {
                return true;
            }
            if (ServerSettings.sqliteHiscoresEnabled) {
                HiscoresDatabase.savePlayer(player);
            }
        }
        String username = player.getUsername();
        LoginProtocol.beginCharacterSave(username);

        try {
            File characterDirectory = new File("./data/characters/");
            if ((!characterDirectory.exists() && !characterDirectory.mkdirs()) || !characterDirectory.isDirectory()) {
                System.err.println("Unable to save " + player.getUsername() + ": character save directory is unavailable: " + characterDirectory.getAbsolutePath());
                return false;
            }

            if (ServerSettings.sqlitePlayerSaveEnabled) {
                // Preserve the independently configured database backend.
                CharacterFileManager.writePlayerFile(player);
                player.lastCharacterSaveMillis = System.currentTimeMillis();
                return true;
            }

            File stagingDirectory = new File(characterDirectory, ".json-staging");
            if (!stagingDirectory.isDirectory() && !stagingDirectory.mkdirs()) {
                System.err.println("Unable to create JSON staging directory: " + stagingDirectory);
                return false;
            }
            File staged = new File(stagingDirectory, username + ".dat");
            File json = new File(characterDirectory, username + ".json");
            for (int attempt = 1; attempt <= 3; attempt++) {
                try {
                    stagedSaveTarget.set(staged);
                    try {
                        CharacterFileManager.writePlayerFile(player);
                    } finally {
                        stagedSaveTarget.remove();
                    }
                    // The legacy writer still serializes every gameplay field.
                    // Read it back to convert into a named, editable JSON record.
                    String stagingPath = stagingDirectory.getPath() + File.separator;
                    if (!CharacterFileManager.validateCharacterFile(stagingPath, username))
                        throw new IOException("Temporary legacy save failed validation");
                    CharacterFileRecord record =
                            CharacterFileManager.readCharacterFileRecord(stagingPath, username, true);
                    if (record == null)
                        throw new IOException("Cannot parse temporary legacy character save");
                    CharacterJsonCodec.save(json, record);
                    // Do not keep stale .dat files alongside new canonical JSON saves.
                    File legacy = new File(characterDirectory, username + ".dat");
                    if (legacy.isFile()) {
                        Files.move(legacy.toPath(),
                                new File(characterDirectory, username + ".dat.bak").toPath(),
                                StandardCopyOption.REPLACE_EXISTING);
                    }
                    // Mark the save only after the JSON file was committed successfully.
                    player.lastCharacterSaveMillis = System.currentTimeMillis();
                    return true;
                } catch (Exception exception) {
                    System.err.println("Character JSON save attempt " + attempt
                            + " failed for " + username + ": " + exception);
                } finally {
                    stagedSaveTarget.remove();
                    try { Files.deleteIfExists(staged.toPath()); }
                    catch (IOException ignored) { }
                }
            }
            System.err.println("Failed to save JSON character " + username + " after 3 attempts.");
            return false;
        } finally {
            LoginProtocol.endCharacterSave(username);
        }
    }

    /*
     * Handled impossible loop by duplicating code
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    private static void writePlayerFile(Player player) {
        Object value = player;
        if (((Player)value).isBot && ((Player)value).botMode != 4) {
            return;
        }
        try {
            int value2;
            int value3;
            Object value4;
            int value5;
            Object value6;
            writePlayerFileControlExit1: {
                writePlayerFileControlExit2: {
                    writePlayerFileControlExit3: {
                        writePlayerFileControlExit4: {
                            writePlayerFileControlExit5: {
                                writePlayerFileControlExit6: {
                                    writePlayerFileControlExit7: {
                                        writePlayerFileControlExit8: {
                                            writePlayerFileControlExit9: {
                                                writePlayerFileControlExit10: {
                                                    writePlayerFileControlExit11: {
                                                        writePlayerFileControlExit12: {
                                                            writePlayerFileControlExit13: {
                                                                writePlayerFileControlExit14: {
                                                                    writePlayerFileControlExit15: {
                                                                        writePlayerFileControlExit16: {
                                                                            writePlayerFileControlExit17: {
                                                                                writePlayerFileControlExit18: {
                                                                                    writePlayerFileControlExit19: {
                                                                                        writePlayerFileControlExit20: {
                                                                                            writePlayerFileControlExit21: {
                                                                                                writePlayerFileControlExit22: {
                                                                                                    writePlayerFileControlExit23: {
                                                                                                        writePlayerFileControlExit24: {
                                                                                                            writePlayerFileControlExit25: {
                                                                                                                writePlayerFileControlExit26: {
                                                                                                                    writePlayerFileControlExit27: {
                                                                                                                        writePlayerFileControlExit28: {
                                                                                                                            writePlayerFileControlExit29: {
                                                                                                                                writePlayerFileControlExit30: {
                                                                                                                                    writePlayerFileControlExit31: {
                                                                                                                                        writePlayerFileControlExit32: {
                                                                                                                                            writePlayerFileControlExit33: {
                                                                                                                                                writePlayerFileControlExit34: {
                                                                                                                                                    writePlayerFileControlExit35: {
                                                                                                                                                        writePlayerFileControlExit36: {
                                                                                                                                                            writePlayerFileControlExit37: {
                                                                                                                                                                writePlayerFileControlExit38: {
                                                                                                                                                                    writePlayerFileControlExit39: {
                                                                                                                                                                        writePlayerFileControlExit40: {
                                                                                                                                                                            writePlayerFileControlExit41: {
                                                                                                                                                                                writePlayerFileControlExit42: {
                                                                                                                                                                                    writePlayerFileControlExit43: {
                                                                                                                                                                                        writePlayerFileControlExit44: {
                                                                                                                                                                                            writePlayerFileControlExit45: {
                                                                                                                                                                                                writePlayerFileControlExit46: {
                                                                                                                                                                                                    writePlayerFileControlExit47: {
                                                                                                                                                                                                        writePlayerFileControlExit48: {
                                                                                                                                                                                                            writePlayerFileControlExit49: {
                                                                                                                                                                                                                writePlayerFileControlExit50: {
                                                                                                                                                                                                                    writePlayerFileControlExit51: {
                                                                                                                                                                                                                        writePlayerFileControlExit52: {
                                                                                                                                                                                                                            writePlayerFileControlExit53: {
                                                                                                                                                                                                                                writePlayerFileControlExit54: {
                                                                                                                                                                                                                                    writePlayerFileControlExit55: {
                                                                                                                                                                                                                                        writePlayerFileControlExit56: {
                                                                                                                                                                                                                                            writePlayerFileControlExit57: {
                                                                                                                                                                                                                                                writePlayerFileControlExit58: {
                                                                                                                                                                                                                                                    writePlayerFileControlExit59: {
                                                                                                                                                                                                                                                        writePlayerFileControlExit60: {
                                                                                                                                                                                                                                                            writePlayerFileControlExit61: {
                                                                                                                                                                                                                                                                writePlayerFileControlExit62: {
                                                                                                                                                                                                                                                                    writePlayerFileControlExit63: {
                                                                                                                                                                                                                                                                        writePlayerFileControlExit64: {
                                                                                                                                                                                                                                                                            writePlayerFileControlExit65: {
                                                                                                                                                                                                                                                                                writePlayerFileControlExit66: {
                                                                                                                                                                                                                                                                                    writePlayerFileControlExit67: {
                                                                                                                                                                                                                                                                                        writePlayerFileControlExit68: {
                                                                                                                                                                                                                                                                                            writePlayerFileControlExit69: {
                                                                                                                                                                                                                                                                                                writePlayerFileControlExit70: {
                                                                                                                                                                                                                                                                                                    writePlayerFileControlExit71: {
                                                                                                                                                                                                                                                                                                        writePlayerFileControlExit72: {
                                                                                                                                                                                                                                                                                                            writePlayerFileControlExit73: {
                                                                                                                                                                                                                                                                                                                writePlayerFileControlExit74: {
                                                                                                                                                                                                                                                                                                                    writePlayerFileControlExit75: {
                                                                                                                                                                                                                                                                                                                        writePlayerFileControlExit76: {
                                                                                                                                                                                                                                                                                                                            writePlayerFileControlExit77: {
                                                                                                                                                                                                                                                                                                                                writePlayerFileControlExit78: {
                                                                                                                                                                                                                                                                                                                                    writePlayerFileControlExit79: {
                                                                                                                                                                                                                                                                                                                                        writePlayerFileControlExit80: {
                                                                                                                                                                                                                                                                                                                                            writePlayerFileControlExit81: {
                                                                                                                                                                                                                                                                                                                                                writePlayerFileControlExit82: {
                                                                                                                                                                                                                                                                                                                                                    writePlayerFileControlExit83: {
                                                                                                                                                                                                                                                                                                                                                        writePlayerFileControlExit84: {
                                                                                                                                                                                                                                                                                                                                                            writePlayerFileControlExit85: {
                                                                                                                                                                                                                                                                                                                                                                writePlayerFileControlExit86: {
                                                                                                                                                                                                                                                                                                                                                                    writePlayerFileControlExit87: {
                                                                                                                                                                                                                                                                                                                                                                        writePlayerFileControlExit88: {
                                                                                                                                                                                                                                                                                                                                                                            writePlayerFileControlExit89: {
                                                                                                                                                                                                                                                                                                                                                                                writePlayerFileControlExit90: {
                                                                                                                                                                                                                                                                                                                                                                                    writePlayerFileControlExit91: {
                                                                                                                                                                                                                                                                                                                                                                                        writePlayerFileControlExit92: {
                                                                                                                                                                                                                                                                                                                                                                                            writePlayerFileControlExit93: {
                                                                                                                                                                                                                                                                                                                                                                                                writePlayerFileControlExit94: {
                                                                                                                                                                                                                                                                                                                                                                                                    writePlayerFileControlExit95: {
                                                                                                                                                                                                                                                                                                                                                                                                        writePlayerFileControlExit96: {
                                                                                                                                                                                                                                                                                                                                                                                                            writePlayerFileControlExit97: {
                                                                                                                                                                                                                                                                                                                                                                                                                writePlayerFileControlExit98: {
                                                                                                                                                                                                                                                                                                                                                                                                                    writePlayerFileControlExit99: {
                                                                                                                                                                                                                                                                                                                                                                                                                        writePlayerFileControlExit100: {
                                                                                                                                                                                                                                                                                                                                                                                                                            writePlayerFileControlExit101: {
                                                                                                                                                                                                                                                                                                                                                                                                                                writePlayerFileControlExit102: {
                                                                                                                                                                                                                                                                                                                                                                                                                                    writePlayerFileControlExit103: {
                                                                                                                                                                                                                                                                                                                                                                                                                                        writePlayerFileControlExit104: {
                                                                                                                                                                                                                                                                                                                                                                                                                                            writePlayerFileControlExit105: {
                                                                                                                                                                                                                                                                                                                                                                                                                                                writePlayerFileControlExit106: {
                                                                                                                                                                                                                                                                                                                                                                                                                                                    writePlayerFileControlExit107: {
                                                                                                                                                                                                                                                                                                                                                                                                                                                        writePlayerFileControlExit108: {
                                                                                                                                                                                                                                                                                                                                                                                                                                                            writePlayerFileControlExit109: {
                                                                                                                                                                                                                                                                                                                                                                                                                                                                writePlayerFileControlExit110: {
                                                                                                                                                                                                                                                                                                                                                                                                                                                                    writePlayerFileControlExit111: {
                                                                                                                                                                                                                                                                                                                                                                                                                                                                        writePlayerFileControlExit112: {
                                                                                                                                                                                                                                                                                                                                                                                                                                                                            writePlayerFileControlExit113: {
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                writePlayerFileControlExit114: {
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                    writePlayerFileControlExit115: {
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                        writePlayerFileControlExit116: {
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                            if (ServerSettings.sqlitePlayerSaveEnabled) {
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                value = new PlayerSaveQueryFactory((Player)value);
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                DatabaseService.getInstance().submit(((PlayerSaveQueryFactory)value).createProfileSaveQuery(), new PlayerProfileSaveCallback());
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                DatabaseService.getInstance().submit(((PlayerSaveQueryFactory)value).createBankSaveQuery(), new PlayerInventorySaveCallback());
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                DatabaseService.getInstance().submit(((PlayerSaveQueryFactory)value).createInventorySaveQuery(), new PlayerBankSaveCallback());
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                DatabaseService.getInstance().submit(((PlayerSaveQueryFactory)value).createEquipmentSaveQuery(), new PlayerEquipmentSaveCallback());
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                DatabaseService.getInstance().submit(((PlayerSaveQueryFactory)value).createContactsSaveQuery(), new PlayerContactsSaveCallback());
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                DatabaseService.getInstance().submit(((PlayerSaveQueryFactory)value).createSkillsSaveQuery(), new PlayerSkillsSaveCallback());
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                return;
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                            }
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                            new ElapsedTimer();
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                            value6 = stagedSaveTarget.get() != null ? stagedSaveTarget.get()
                                        : new File("./data/characters/" + ((Player)value).getUsername() + ".dat");
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                            if (!((File)value6).exists()) {
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                ((File)value6).createNewFile();
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                            }
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                            value6 = new FileOutputStream((File)value6);
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                            value6 = new DataOutputStream((OutputStream)value6);
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                            ((DataOutputStream)value6).writeShort(31);
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                            ((DataOutputStream)value6).writeUTF(((Player)value).getUsername());
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                            ((DataOutputStream)value6).writeUTF(((Player)value).getPassword());
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                            ((DataOutputStream)value6).writeUTF(((Player)value).getHostAddress());
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                            ((DataOutputStream)value6).writeInt(((Player)value).getPlayerRights());
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                            ((DataOutputStream)value6).writeUTF("");
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                            ((DataOutputStream)value6).writeUTF(((Player)value).getProfileString1());
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                            ((DataOutputStream)value6).writeUTF(((Player)value).getProfileString2());
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                            ((DataOutputStream)value6).writeLong(System.currentTimeMillis());
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                            long totalPlaytimeMillis = ((Player)value).getTotalPlaytimeMillis();
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                            value5 = (int)(totalPlaytimeMillis / 1000L / 60L / 60L);
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                            if (value5 >= 100000) {
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                ((DataOutputStream)value6).writeLong(((Player)value).totalPlaytimeMillis);
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                ((Player)value).logError("Couldn't save playing time, prev: " + ((Player)value).totalPlaytimeMillis + " login: " + ((Player)value).sessionStartMillis + " current: " + System.currentTimeMillis());
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                            } else {
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                ((DataOutputStream)value6).writeLong(((Player)value).getTotalPlaytimeMillis());
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                            }
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                            ((DataOutputStream)value6).writeLong(((Player)value).createdAtMillis);
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                            ((DataOutputStream)value6).writeBoolean(((Player)value).loginRestrictionExempt);
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                            ((DataOutputStream)value6).writeBoolean(((Player)value).hasMemberFlag());
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                            ((DataOutputStream)value6).writeInt(((Player)value).getDonatorPoints());
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                            if (((Player)value).cutsceneReturnPosition == null) {
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                ((DataOutputStream)value6).writeInt(((Entity)value).getPosition().getX());
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                ((DataOutputStream)value6).writeInt(((Entity)value).getPosition().getY());
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                ((DataOutputStream)value6).writeInt(((Entity)value).getPosition().getPlane());
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                            } else {
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                ((DataOutputStream)value6).writeInt(((Player)value).cutsceneReturnPosition.getX());
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                ((DataOutputStream)value6).writeInt(((Player)value).cutsceneReturnPosition.getY());
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                ((DataOutputStream)value6).writeInt(((Player)value).cutsceneReturnPosition.getPlane());
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                            }
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                            ((DataOutputStream)value6).writeInt(((Player)value).getGender());
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                            ((DataOutputStream)value6).writeInt(((Player)value).npcKillCount);
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                            ((DataOutputStream)value6).writeInt(((Player)value).playerKillCount);
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                            ((DataOutputStream)value6).writeInt(((Player)value).deathCount);
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                            ((DataOutputStream)value6).writeInt(((Player)value).easyCluesCompleted);
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                            ((DataOutputStream)value6).writeInt(((Player)value).mediumCluesCompleted);
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                            ((DataOutputStream)value6).writeInt(((Player)value).hardCluesCompleted);
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                            ((DataOutputStream)value6).writeInt(((Player)value).soldItemsValue);
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                            ((DataOutputStream)value6).writeInt(((Player)value).boughtItemsValue);
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                            ((DataOutputStream)value6).writeInt(((Player)value).duelWins);
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                            ((DataOutputStream)value6).writeInt(((Player)value).duelLosses);
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                            ((DataOutputStream)value6).writeInt(((Player)value).legacyQuestPoints);
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                            ((DataOutputStream)value6).writeBoolean(((Player)value).isAutoRetaliate());
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                            ((DataOutputStream)value6).writeInt(((Player)value).getFightMode());
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                            ((DataOutputStream)value6).writeInt(((Player)value).getBrightness());
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                            ((DataOutputStream)value6).writeInt(((Player)value).getMouseButtons());
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                            ((DataOutputStream)value6).writeInt(((Player)value).getPublicChatEffects());
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                            ((DataOutputStream)value6).writeInt(((Player)value).getSplitPrivateChat());
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                            ((DataOutputStream)value6).writeInt(((Player)value).getAcceptAid());
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                            ((DataOutputStream)value6).writeInt(((Player)value).getMusicVolume());
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                            ((DataOutputStream)value6).writeInt(((Player)value).getEffectVolume());
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                            ((DataOutputStream)value6).writeDouble(((Player)value).getSpecialEnergy());
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                            ((DataOutputStream)value6).writeBoolean(((Player)value).getBankPinManager().isChangingPin());
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                            ((DataOutputStream)value6).writeBoolean(((Player)value).getBankPinManager().isDeletingPin());
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                            ((DataOutputStream)value6).writeInt(((Player)value).getBankPinManager().getPinAppendYear());
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                            ((DataOutputStream)value6).writeInt(((Player)value).getBankPinManager().getPinAppendDate());
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                            ((DataOutputStream)value6).writeInt(((Player)value).getBindingNecklaceCharge());
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                            ((DataOutputStream)value6).writeInt(((Player)value).getRingOfForgingLife());
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                            ((DataOutputStream)value6).writeInt(((Player)value).getRingOfRecoilLife());
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                            ((DataOutputStream)value6).writeInt(((Player)value).getSkullTimer());
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                            ((DataOutputStream)value6).writeShort(((Player)value).getRunEnergyRaw());
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                            ((DataOutputStream)value6).writeBoolean(((Entity)value).getMovementQueue().isRunning());
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                            value5 = 0;
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                            while (value5 < ((Player)value).getBankPinManager().getCurrentPin().length) {
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                ((DataOutputStream)value6).writeInt(((Player)value).getBankPinManager().getCurrentPin()[value5]);
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                ++value5;
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                            }
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                            value5 = 0;
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                            while (value5 < ((Player)value).getBankPinManager().getPendingPin().length) {
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                ((DataOutputStream)value6).writeInt(((Player)value).getBankPinManager().getPendingPin()[value5]);
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                ++value5;
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                            }
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                            value5 = 0;
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                            while (value5 < 4) {
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                ((DataOutputStream)value6).writeInt(((Player)value).getEssencePouchAmount(value5));
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                ++value5;
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                            }
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                            value5 = 0;
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                            while (value5 < ((Player)value).getAppearanceParts().length) {
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                ((DataOutputStream)value6).writeInt(((Player)value).getAppearanceParts()[value5]);
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                ++value5;
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                            }
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                            value5 = 0;
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                            while (value5 < ((Player)value).getAppearanceColors().length) {
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                ((DataOutputStream)value6).writeInt(((Player)value).getAppearanceColors()[value5]);
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                ++value5;
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                            }
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                            value5 = 0;
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                            while (value5 < ((Player)value).getSkillManager().getCurrentLevels().length) {
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                ((DataOutputStream)value6).writeInt(((Player)value).getSkillManager().getCurrentLevels()[value5]);
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                ++value5;
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                            }
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                            value5 = 0;
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                            while (value5 < ((Player)value).getSkillManager().getExperience().length) {
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                ((DataOutputStream)value6).writeInt((int)((Player)value).getSkillManager().getExperience()[value5]);
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                ++value5;
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                            }
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                            value5 = 0;
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                            while (value5 < 28) {
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                value4 = ((Player)value).getInventoryManager().getContainer().getItemAt(value5);
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                if (value4 == null) {
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                    ((DataOutputStream)value6).writeInt(65535);
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                } else {
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                    ((DataOutputStream)value6).writeInt(((ItemStack)value4).getId());
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                    ((DataOutputStream)value6).writeInt(((ItemStack)value4).getAmount());
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                    ((DataOutputStream)value6).writeInt(((ItemStack)value4).getMetadata());
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                }
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                ++value5;
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                            }
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                            value5 = 0;
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                            while (value5 < 14) {
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                value4 = ((Player)value).getEquipmentManager().getContainer().getItemAt(value5);
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                if (value4 == null) {
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                    ((DataOutputStream)value6).writeInt(65535);
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                } else {
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                    ((DataOutputStream)value6).writeInt(((ItemStack)value4).getId());
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                    ((DataOutputStream)value6).writeInt(((ItemStack)value4).getAmount());
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                    ((DataOutputStream)value6).writeInt(((ItemStack)value4).getMetadata());
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                }
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                ++value5;
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                            }
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                            ((Player)value).getBankContainer().removeEmptyTabs();
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                            ((DataOutputStream)value6).writeByte(((Player)value).getBankContainer().getTabCount());
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                            value5 = 0;
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                            while (value5 < ((Player)value).getBankContainer().getTabCount()) {
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                ((Player)value).getBankContainer().compactTab(value5);
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                value4 = ((Player)value).getBankContainer().getTab(value5).getItems();
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                value3 = ((ArrayList)value4).size();
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                ((DataOutputStream)value6).writeShort(value3);
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                value2 = 0;
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                while (value2 < value3) {
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                    ItemStack itemStack = (ItemStack)((ArrayList)value4).get(value2);
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                    if (itemStack == null) {
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                        ((DataOutputStream)value6).writeInt(65535);
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                    } else {
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                        ((DataOutputStream)value6).writeInt(itemStack.getId());
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                        ((DataOutputStream)value6).writeInt(itemStack.getAmount());
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                        ((DataOutputStream)value6).writeInt(itemStack.getMetadata());
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                    }
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                    ++value2;
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                }
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                ++value5;
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                            }
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                            value5 = 0;
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                            while (value5 < ((Player)value).getFriendsList().length) {
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                ((DataOutputStream)value6).writeLong(((Player)value).getFriendsList()[value5]);
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                ++value5;
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                            }
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                            value5 = 0;
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                            while (value5 < ((Player)value).getIgnoreList().length) {
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                ((DataOutputStream)value6).writeLong(((Player)value).getIgnoreList()[value5]);
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                ++value5;
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                            }
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                            value5 = 0;
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                            while (value5 < ((Player)value).getQueuedLoginItemIds().length) {
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                ((DataOutputStream)value6).writeInt(((Player)value).getQueuedLoginItemIds()[value5]);
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                ((DataOutputStream)value6).writeInt(((Player)value).getQueuedLoginItemAmounts()[value5]);
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                ++value5;
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                            }
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                            ((DataOutputStream)value6).writeInt(((Player)value).getAbyssMageNpcId());
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                            ((DataOutputStream)value6).writeLong(((Player)value).getMuteExpires());
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                            ((DataOutputStream)value6).writeLong(((Player)value).getBanExpires());
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                            value5 = 0;
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                            while (value5 < 6) {
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                ((DataOutputStream)value6).writeBoolean(((Player)value).isBarrowsBrotherKilled(value5));
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                ++value5;
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                            }
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                            ((DataOutputStream)value6).writeInt(((Player)value).getBarrowsKillCount());
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                            ((DataOutputStream)value6).writeInt(((Player)value).getBarrowsTargetBrotherIndex());
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                            ((DataOutputStream)value6).writeInt(((Entity)value).getPoisonImmunityTimer().getRemainingTicks());
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                            ((DataOutputStream)value6).writeInt(((Entity)value).getAntifireTimer().getRemainingTicks());
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                            ((DataOutputStream)value6).writeInt(((Entity)value).getTeleblockTimer().getRemainingTicks());
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                            ((DataOutputStream)value6).writeDouble(((Entity)value).getPoisonDamage());
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                            value5 = 0;
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                            if (!true) break writePlayerFileControlExit116;
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                            value4 = ((Player)value).getAllotmentPatchManager();
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                            if (value5 >= ((AllotmentPatchManager)value4).growthStages.length) break writePlayerFileControlExit115;
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                        }
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                        do {
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                            value4 = ((Player)value).getAllotmentPatchManager();
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                            ((DataOutputStream)value6).writeInt(((AllotmentPatchManager)value4).growthStages[value5]);
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                            ++value5;
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                            value4 = ((Player)value).getAllotmentPatchManager();
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                        } while (value5 < ((AllotmentPatchManager)value4).growthStages.length);
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                    }
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                    value5 = 0;
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                    if (!true) break writePlayerFileControlExit114;
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                    value4 = ((Player)value).getAllotmentPatchManager();
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                    if (value5 >= ((AllotmentPatchManager)value4).cropIds.length) break writePlayerFileControlExit113;
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                }
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                do {
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                    value4 = ((Player)value).getAllotmentPatchManager();
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                    ((DataOutputStream)value6).writeInt(((AllotmentPatchManager)value4).cropIds[value5]);
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                    ++value5;
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                    value4 = ((Player)value).getAllotmentPatchManager();
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                } while (value5 < ((AllotmentPatchManager)value4).cropIds.length);
                                                                                                                                                                                                                                                                                                                                                                                                                                                                            }
                                                                                                                                                                                                                                                                                                                                                                                                                                                                            value5 = 0;
                                                                                                                                                                                                                                                                                                                                                                                                                                                                            if (!true) break writePlayerFileControlExit112;
                                                                                                                                                                                                                                                                                                                                                                                                                                                                            value4 = ((Player)value).getAllotmentPatchManager();
                                                                                                                                                                                                                                                                                                                                                                                                                                                                            if (value5 >= ((AllotmentPatchManager)value4).harvestAmounts.length) break writePlayerFileControlExit111;
                                                                                                                                                                                                                                                                                                                                                                                                                                                                        }
                                                                                                                                                                                                                                                                                                                                                                                                                                                                        do {
                                                                                                                                                                                                                                                                                                                                                                                                                                                                            value4 = ((Player)value).getAllotmentPatchManager();
                                                                                                                                                                                                                                                                                                                                                                                                                                                                            ((DataOutputStream)value6).writeInt(((AllotmentPatchManager)value4).harvestAmounts[value5]);
                                                                                                                                                                                                                                                                                                                                                                                                                                                                            ++value5;
                                                                                                                                                                                                                                                                                                                                                                                                                                                                            value4 = ((Player)value).getAllotmentPatchManager();
                                                                                                                                                                                                                                                                                                                                                                                                                                                                        } while (value5 < ((AllotmentPatchManager)value4).harvestAmounts.length);
                                                                                                                                                                                                                                                                                                                                                                                                                                                                    }
                                                                                                                                                                                                                                                                                                                                                                                                                                                                    value5 = 0;
                                                                                                                                                                                                                                                                                                                                                                                                                                                                    if (!true) break writePlayerFileControlExit110;
                                                                                                                                                                                                                                                                                                                                                                                                                                                                    value4 = ((Player)value).getAllotmentPatchManager();
                                                                                                                                                                                                                                                                                                                                                                                                                                                                    if (value5 >= ((AllotmentPatchManager)value4).patchStates.length) break writePlayerFileControlExit109;
                                                                                                                                                                                                                                                                                                                                                                                                                                                                }
                                                                                                                                                                                                                                                                                                                                                                                                                                                                do {
                                                                                                                                                                                                                                                                                                                                                                                                                                                                    value4 = ((Player)value).getAllotmentPatchManager();
                                                                                                                                                                                                                                                                                                                                                                                                                                                                    ((DataOutputStream)value6).writeInt(((AllotmentPatchManager)value4).patchStates[value5]);
                                                                                                                                                                                                                                                                                                                                                                                                                                                                    ++value5;
                                                                                                                                                                                                                                                                                                                                                                                                                                                                    value4 = ((Player)value).getAllotmentPatchManager();
                                                                                                                                                                                                                                                                                                                                                                                                                                                                } while (value5 < ((AllotmentPatchManager)value4).patchStates.length);
                                                                                                                                                                                                                                                                                                                                                                                                                                                            }
                                                                                                                                                                                                                                                                                                                                                                                                                                                            value5 = 0;
                                                                                                                                                                                                                                                                                                                                                                                                                                                            if (!true) break writePlayerFileControlExit108;
                                                                                                                                                                                                                                                                                                                                                                                                                                                            value4 = ((Player)value).getAllotmentPatchManager();
                                                                                                                                                                                                                                                                                                                                                                                                                                                            if (value5 >= ((AllotmentPatchManager)value4).lastUpdateTicks.length) break writePlayerFileControlExit107;
                                                                                                                                                                                                                                                                                                                                                                                                                                                        }
                                                                                                                                                                                                                                                                                                                                                                                                                                                        do {
                                                                                                                                                                                                                                                                                                                                                                                                                                                            value4 = ((Player)value).getAllotmentPatchManager();
                                                                                                                                                                                                                                                                                                                                                                                                                                                            ((DataOutputStream)value6).writeLong(((AllotmentPatchManager)value4).lastUpdateTicks[value5]);
                                                                                                                                                                                                                                                                                                                                                                                                                                                            ++value5;
                                                                                                                                                                                                                                                                                                                                                                                                                                                            value4 = ((Player)value).getAllotmentPatchManager();
                                                                                                                                                                                                                                                                                                                                                                                                                                                        } while (value5 < ((AllotmentPatchManager)value4).lastUpdateTicks.length);
                                                                                                                                                                                                                                                                                                                                                                                                                                                    }
                                                                                                                                                                                                                                                                                                                                                                                                                                                    value5 = 0;
                                                                                                                                                                                                                                                                                                                                                                                                                                                    if (!true) break writePlayerFileControlExit106;
                                                                                                                                                                                                                                                                                                                                                                                                                                                    value4 = ((Player)value).getAllotmentPatchManager();
                                                                                                                                                                                                                                                                                                                                                                                                                                                    if (value5 >= ((AllotmentPatchManager)value4).diseaseChanceMultipliers.length) break writePlayerFileControlExit105;
                                                                                                                                                                                                                                                                                                                                                                                                                                                }
                                                                                                                                                                                                                                                                                                                                                                                                                                                do {
                                                                                                                                                                                                                                                                                                                                                                                                                                                    value4 = ((Player)value).getAllotmentPatchManager();
                                                                                                                                                                                                                                                                                                                                                                                                                                                    ((DataOutputStream)value6).writeDouble(((AllotmentPatchManager)value4).diseaseChanceMultipliers[value5]);
                                                                                                                                                                                                                                                                                                                                                                                                                                                    ++value5;
                                                                                                                                                                                                                                                                                                                                                                                                                                                    value4 = ((Player)value).getAllotmentPatchManager();
                                                                                                                                                                                                                                                                                                                                                                                                                                                } while (value5 < ((AllotmentPatchManager)value4).diseaseChanceMultipliers.length);
                                                                                                                                                                                                                                                                                                                                                                                                                                            }
                                                                                                                                                                                                                                                                                                                                                                                                                                            value5 = 0;
                                                                                                                                                                                                                                                                                                                                                                                                                                            if (!true) break writePlayerFileControlExit104;
                                                                                                                                                                                                                                                                                                                                                                                                                                            value4 = ((Player)value).getAllotmentPatchManager();
                                                                                                                                                                                                                                                                                                                                                                                                                                            if (value5 >= ((AllotmentPatchManager)value4).protectionFlags.length) break writePlayerFileControlExit103;
                                                                                                                                                                                                                                                                                                                                                                                                                                        }
                                                                                                                                                                                                                                                                                                                                                                                                                                        do {
                                                                                                                                                                                                                                                                                                                                                                                                                                            value4 = ((Player)value).getAllotmentPatchManager();
                                                                                                                                                                                                                                                                                                                                                                                                                                            ((DataOutputStream)value6).writeBoolean(((AllotmentPatchManager)value4).protectionFlags[value5]);
                                                                                                                                                                                                                                                                                                                                                                                                                                            ++value5;
                                                                                                                                                                                                                                                                                                                                                                                                                                            value4 = ((Player)value).getAllotmentPatchManager();
                                                                                                                                                                                                                                                                                                                                                                                                                                        } while (value5 < ((AllotmentPatchManager)value4).protectionFlags.length);
                                                                                                                                                                                                                                                                                                                                                                                                                                    }
                                                                                                                                                                                                                                                                                                                                                                                                                                    value5 = 0;
                                                                                                                                                                                                                                                                                                                                                                                                                                    if (!true) break writePlayerFileControlExit102;
                                                                                                                                                                                                                                                                                                                                                                                                                                    value4 = ((Player)value).getBushPatchManager();
                                                                                                                                                                                                                                                                                                                                                                                                                                    if (value5 >= ((BushPatchManager)value4).growthStages.length) break writePlayerFileControlExit101;
                                                                                                                                                                                                                                                                                                                                                                                                                                }
                                                                                                                                                                                                                                                                                                                                                                                                                                do {
                                                                                                                                                                                                                                                                                                                                                                                                                                    value4 = ((Player)value).getBushPatchManager();
                                                                                                                                                                                                                                                                                                                                                                                                                                    ((DataOutputStream)value6).writeInt(((BushPatchManager)value4).growthStages[value5]);
                                                                                                                                                                                                                                                                                                                                                                                                                                    ++value5;
                                                                                                                                                                                                                                                                                                                                                                                                                                    value4 = ((Player)value).getBushPatchManager();
                                                                                                                                                                                                                                                                                                                                                                                                                                } while (value5 < ((BushPatchManager)value4).growthStages.length);
                                                                                                                                                                                                                                                                                                                                                                                                                            }
                                                                                                                                                                                                                                                                                                                                                                                                                            value5 = 0;
                                                                                                                                                                                                                                                                                                                                                                                                                            if (!true) break writePlayerFileControlExit100;
                                                                                                                                                                                                                                                                                                                                                                                                                            value4 = ((Player)value).getBushPatchManager();
                                                                                                                                                                                                                                                                                                                                                                                                                            if (value5 >= ((BushPatchManager)value4).cropIds.length) break writePlayerFileControlExit99;
                                                                                                                                                                                                                                                                                                                                                                                                                        }
                                                                                                                                                                                                                                                                                                                                                                                                                        do {
                                                                                                                                                                                                                                                                                                                                                                                                                            value4 = ((Player)value).getBushPatchManager();
                                                                                                                                                                                                                                                                                                                                                                                                                            ((DataOutputStream)value6).writeInt(((BushPatchManager)value4).cropIds[value5]);
                                                                                                                                                                                                                                                                                                                                                                                                                            ++value5;
                                                                                                                                                                                                                                                                                                                                                                                                                            value4 = ((Player)value).getBushPatchManager();
                                                                                                                                                                                                                                                                                                                                                                                                                        } while (value5 < ((BushPatchManager)value4).cropIds.length);
                                                                                                                                                                                                                                                                                                                                                                                                                    }
                                                                                                                                                                                                                                                                                                                                                                                                                    value5 = 0;
                                                                                                                                                                                                                                                                                                                                                                                                                    if (!true) break writePlayerFileControlExit98;
                                                                                                                                                                                                                                                                                                                                                                                                                    value4 = ((Player)value).getBushPatchManager();
                                                                                                                                                                                                                                                                                                                                                                                                                    if (value5 >= ((BushPatchManager)value4).patchStates.length) break writePlayerFileControlExit97;
                                                                                                                                                                                                                                                                                                                                                                                                                }
                                                                                                                                                                                                                                                                                                                                                                                                                do {
                                                                                                                                                                                                                                                                                                                                                                                                                    value4 = ((Player)value).getBushPatchManager();
                                                                                                                                                                                                                                                                                                                                                                                                                    ((DataOutputStream)value6).writeInt(((BushPatchManager)value4).patchStates[value5]);
                                                                                                                                                                                                                                                                                                                                                                                                                    ++value5;
                                                                                                                                                                                                                                                                                                                                                                                                                    value4 = ((Player)value).getBushPatchManager();
                                                                                                                                                                                                                                                                                                                                                                                                                } while (value5 < ((BushPatchManager)value4).patchStates.length);
                                                                                                                                                                                                                                                                                                                                                                                                            }
                                                                                                                                                                                                                                                                                                                                                                                                            value5 = 0;
                                                                                                                                                                                                                                                                                                                                                                                                            if (!true) break writePlayerFileControlExit96;
                                                                                                                                                                                                                                                                                                                                                                                                            value4 = ((Player)value).getBushPatchManager();
                                                                                                                                                                                                                                                                                                                                                                                                            if (value5 >= ((BushPatchManager)value4).lastUpdateTicks.length) break writePlayerFileControlExit95;
                                                                                                                                                                                                                                                                                                                                                                                                        }
                                                                                                                                                                                                                                                                                                                                                                                                        do {
                                                                                                                                                                                                                                                                                                                                                                                                            value4 = ((Player)value).getBushPatchManager();
                                                                                                                                                                                                                                                                                                                                                                                                            ((DataOutputStream)value6).writeLong(((BushPatchManager)value4).lastUpdateTicks[value5]);
                                                                                                                                                                                                                                                                                                                                                                                                            ++value5;
                                                                                                                                                                                                                                                                                                                                                                                                            value4 = ((Player)value).getBushPatchManager();
                                                                                                                                                                                                                                                                                                                                                                                                        } while (value5 < ((BushPatchManager)value4).lastUpdateTicks.length);
                                                                                                                                                                                                                                                                                                                                                                                                    }
                                                                                                                                                                                                                                                                                                                                                                                                    value5 = 0;
                                                                                                                                                                                                                                                                                                                                                                                                    if (!true) break writePlayerFileControlExit94;
                                                                                                                                                                                                                                                                                                                                                                                                    value4 = ((Player)value).getBushPatchManager();
                                                                                                                                                                                                                                                                                                                                                                                                    if (value5 >= ((BushPatchManager)value4).diseaseChanceMultipliers.length) break writePlayerFileControlExit93;
                                                                                                                                                                                                                                                                                                                                                                                                }
                                                                                                                                                                                                                                                                                                                                                                                                do {
                                                                                                                                                                                                                                                                                                                                                                                                    value4 = ((Player)value).getBushPatchManager();
                                                                                                                                                                                                                                                                                                                                                                                                    ((DataOutputStream)value6).writeDouble(((BushPatchManager)value4).diseaseChanceMultipliers[value5]);
                                                                                                                                                                                                                                                                                                                                                                                                    ++value5;
                                                                                                                                                                                                                                                                                                                                                                                                    value4 = ((Player)value).getBushPatchManager();
                                                                                                                                                                                                                                                                                                                                                                                                } while (value5 < ((BushPatchManager)value4).diseaseChanceMultipliers.length);
                                                                                                                                                                                                                                                                                                                                                                                            }
                                                                                                                                                                                                                                                                                                                                                                                            value5 = 0;
                                                                                                                                                                                                                                                                                                                                                                                            if (!true) break writePlayerFileControlExit92;
                                                                                                                                                                                                                                                                                                                                                                                            value4 = ((Player)value).getBushPatchManager();
                                                                                                                                                                                                                                                                                                                                                                                            if (value5 >= ((BushPatchManager)value4).protectionFlags.length) break writePlayerFileControlExit91;
                                                                                                                                                                                                                                                                                                                                                                                        }
                                                                                                                                                                                                                                                                                                                                                                                        do {
                                                                                                                                                                                                                                                                                                                                                                                            value4 = ((Player)value).getBushPatchManager();
                                                                                                                                                                                                                                                                                                                                                                                            ((DataOutputStream)value6).writeBoolean(((BushPatchManager)value4).protectionFlags[value5]);
                                                                                                                                                                                                                                                                                                                                                                                            ++value5;
                                                                                                                                                                                                                                                                                                                                                                                            value4 = ((Player)value).getBushPatchManager();
                                                                                                                                                                                                                                                                                                                                                                                        } while (value5 < ((BushPatchManager)value4).protectionFlags.length);
                                                                                                                                                                                                                                                                                                                                                                                    }
                                                                                                                                                                                                                                                                                                                                                                                    value5 = 0;
                                                                                                                                                                                                                                                                                                                                                                                    if (!true) break writePlayerFileControlExit90;
                                                                                                                                                                                                                                                                                                                                                                                    value4 = ((Player)value).getFlowerPatchManager();
                                                                                                                                                                                                                                                                                                                                                                                    if (value5 >= ((FlowerPatchManager)value4).growthStages.length) break writePlayerFileControlExit89;
                                                                                                                                                                                                                                                                                                                                                                                }
                                                                                                                                                                                                                                                                                                                                                                                do {
                                                                                                                                                                                                                                                                                                                                                                                    value4 = ((Player)value).getFlowerPatchManager();
                                                                                                                                                                                                                                                                                                                                                                                    ((DataOutputStream)value6).writeInt(((FlowerPatchManager)value4).growthStages[value5]);
                                                                                                                                                                                                                                                                                                                                                                                    ++value5;
                                                                                                                                                                                                                                                                                                                                                                                    value4 = ((Player)value).getFlowerPatchManager();
                                                                                                                                                                                                                                                                                                                                                                                } while (value5 < ((FlowerPatchManager)value4).growthStages.length);
                                                                                                                                                                                                                                                                                                                                                                            }
                                                                                                                                                                                                                                                                                                                                                                            value5 = 0;
                                                                                                                                                                                                                                                                                                                                                                            if (!true) break writePlayerFileControlExit88;
                                                                                                                                                                                                                                                                                                                                                                            value4 = ((Player)value).getFlowerPatchManager();
                                                                                                                                                                                                                                                                                                                                                                            if (value5 >= ((FlowerPatchManager)value4).cropIds.length) break writePlayerFileControlExit87;
                                                                                                                                                                                                                                                                                                                                                                        }
                                                                                                                                                                                                                                                                                                                                                                        do {
                                                                                                                                                                                                                                                                                                                                                                            value4 = ((Player)value).getFlowerPatchManager();
                                                                                                                                                                                                                                                                                                                                                                            ((DataOutputStream)value6).writeInt(((FlowerPatchManager)value4).cropIds[value5]);
                                                                                                                                                                                                                                                                                                                                                                            ++value5;
                                                                                                                                                                                                                                                                                                                                                                            value4 = ((Player)value).getFlowerPatchManager();
                                                                                                                                                                                                                                                                                                                                                                        } while (value5 < ((FlowerPatchManager)value4).cropIds.length);
                                                                                                                                                                                                                                                                                                                                                                    }
                                                                                                                                                                                                                                                                                                                                                                    value5 = 0;
                                                                                                                                                                                                                                                                                                                                                                    if (!true) break writePlayerFileControlExit86;
                                                                                                                                                                                                                                                                                                                                                                    value4 = ((Player)value).getFlowerPatchManager();
                                                                                                                                                                                                                                                                                                                                                                    if (value5 >= ((FlowerPatchManager)value4).patchStates.length) break writePlayerFileControlExit85;
                                                                                                                                                                                                                                                                                                                                                                }
                                                                                                                                                                                                                                                                                                                                                                do {
                                                                                                                                                                                                                                                                                                                                                                    value4 = ((Player)value).getFlowerPatchManager();
                                                                                                                                                                                                                                                                                                                                                                    ((DataOutputStream)value6).writeInt(((FlowerPatchManager)value4).patchStates[value5]);
                                                                                                                                                                                                                                                                                                                                                                    ++value5;
                                                                                                                                                                                                                                                                                                                                                                    value4 = ((Player)value).getFlowerPatchManager();
                                                                                                                                                                                                                                                                                                                                                                } while (value5 < ((FlowerPatchManager)value4).patchStates.length);
                                                                                                                                                                                                                                                                                                                                                            }
                                                                                                                                                                                                                                                                                                                                                            value5 = 0;
                                                                                                                                                                                                                                                                                                                                                            if (!true) break writePlayerFileControlExit84;
                                                                                                                                                                                                                                                                                                                                                            value4 = ((Player)value).getFlowerPatchManager();
                                                                                                                                                                                                                                                                                                                                                            if (value5 >= ((FlowerPatchManager)value4).lastUpdateTicks.length) break writePlayerFileControlExit83;
                                                                                                                                                                                                                                                                                                                                                        }
                                                                                                                                                                                                                                                                                                                                                        do {
                                                                                                                                                                                                                                                                                                                                                            value4 = ((Player)value).getFlowerPatchManager();
                                                                                                                                                                                                                                                                                                                                                            ((DataOutputStream)value6).writeLong(((FlowerPatchManager)value4).lastUpdateTicks[value5]);
                                                                                                                                                                                                                                                                                                                                                            ++value5;
                                                                                                                                                                                                                                                                                                                                                            value4 = ((Player)value).getFlowerPatchManager();
                                                                                                                                                                                                                                                                                                                                                        } while (value5 < ((FlowerPatchManager)value4).lastUpdateTicks.length);
                                                                                                                                                                                                                                                                                                                                                    }
                                                                                                                                                                                                                                                                                                                                                    value5 = 0;
                                                                                                                                                                                                                                                                                                                                                    if (!true) break writePlayerFileControlExit82;
                                                                                                                                                                                                                                                                                                                                                    value4 = ((Player)value).getFlowerPatchManager();
                                                                                                                                                                                                                                                                                                                                                    if (value5 >= ((FlowerPatchManager)value4).diseaseChanceMultipliers.length) break writePlayerFileControlExit81;
                                                                                                                                                                                                                                                                                                                                                }
                                                                                                                                                                                                                                                                                                                                                do {
                                                                                                                                                                                                                                                                                                                                                    value4 = ((Player)value).getFlowerPatchManager();
                                                                                                                                                                                                                                                                                                                                                    ((DataOutputStream)value6).writeDouble(((FlowerPatchManager)value4).diseaseChanceMultipliers[value5]);
                                                                                                                                                                                                                                                                                                                                                    ++value5;
                                                                                                                                                                                                                                                                                                                                                    value4 = ((Player)value).getFlowerPatchManager();
                                                                                                                                                                                                                                                                                                                                                } while (value5 < ((FlowerPatchManager)value4).diseaseChanceMultipliers.length);
                                                                                                                                                                                                                                                                                                                                            }
                                                                                                                                                                                                                                                                                                                                            value5 = 0;
                                                                                                                                                                                                                                                                                                                                            if (!true) break writePlayerFileControlExit80;
                                                                                                                                                                                                                                                                                                                                            value4 = ((Player)value).getFruitTreePatchManager();
                                                                                                                                                                                                                                                                                                                                            if (value5 >= ((FruitTreePatchManager)value4).growthStages.length) break writePlayerFileControlExit79;
                                                                                                                                                                                                                                                                                                                                        }
                                                                                                                                                                                                                                                                                                                                        do {
                                                                                                                                                                                                                                                                                                                                            value4 = ((Player)value).getFruitTreePatchManager();
                                                                                                                                                                                                                                                                                                                                            ((DataOutputStream)value6).writeInt(((FruitTreePatchManager)value4).growthStages[value5]);
                                                                                                                                                                                                                                                                                                                                            ++value5;
                                                                                                                                                                                                                                                                                                                                            value4 = ((Player)value).getFruitTreePatchManager();
                                                                                                                                                                                                                                                                                                                                        } while (value5 < ((FruitTreePatchManager)value4).growthStages.length);
                                                                                                                                                                                                                                                                                                                                    }
                                                                                                                                                                                                                                                                                                                                    value5 = 0;
                                                                                                                                                                                                                                                                                                                                    if (!true) break writePlayerFileControlExit78;
                                                                                                                                                                                                                                                                                                                                    value4 = ((Player)value).getFruitTreePatchManager();
                                                                                                                                                                                                                                                                                                                                    if (value5 >= ((FruitTreePatchManager)value4).treeIds.length) break writePlayerFileControlExit77;
                                                                                                                                                                                                                                                                                                                                }
                                                                                                                                                                                                                                                                                                                                do {
                                                                                                                                                                                                                                                                                                                                    value4 = ((Player)value).getFruitTreePatchManager();
                                                                                                                                                                                                                                                                                                                                    ((DataOutputStream)value6).writeInt(((FruitTreePatchManager)value4).treeIds[value5]);
                                                                                                                                                                                                                                                                                                                                    ++value5;
                                                                                                                                                                                                                                                                                                                                    value4 = ((Player)value).getFruitTreePatchManager();
                                                                                                                                                                                                                                                                                                                                } while (value5 < ((FruitTreePatchManager)value4).treeIds.length);
                                                                                                                                                                                                                                                                                                                            }
                                                                                                                                                                                                                                                                                                                            value5 = 0;
                                                                                                                                                                                                                                                                                                                            if (!true) break writePlayerFileControlExit76;
                                                                                                                                                                                                                                                                                                                            value4 = ((Player)value).getFruitTreePatchManager();
                                                                                                                                                                                                                                                                                                                            if (value5 >= ((FruitTreePatchManager)value4).patchStates.length) break writePlayerFileControlExit75;
                                                                                                                                                                                                                                                                                                                        }
                                                                                                                                                                                                                                                                                                                        do {
                                                                                                                                                                                                                                                                                                                            value4 = ((Player)value).getFruitTreePatchManager();
                                                                                                                                                                                                                                                                                                                            ((DataOutputStream)value6).writeInt(((FruitTreePatchManager)value4).patchStates[value5]);
                                                                                                                                                                                                                                                                                                                            ++value5;
                                                                                                                                                                                                                                                                                                                            value4 = ((Player)value).getFruitTreePatchManager();
                                                                                                                                                                                                                                                                                                                        } while (value5 < ((FruitTreePatchManager)value4).patchStates.length);
                                                                                                                                                                                                                                                                                                                    }
                                                                                                                                                                                                                                                                                                                    value5 = 0;
                                                                                                                                                                                                                                                                                                                    if (!true) break writePlayerFileControlExit74;
                                                                                                                                                                                                                                                                                                                    value4 = ((Player)value).getFruitTreePatchManager();
                                                                                                                                                                                                                                                                                                                    if (value5 >= ((FruitTreePatchManager)value4).lastUpdateTicks.length) break writePlayerFileControlExit73;
                                                                                                                                                                                                                                                                                                                }
                                                                                                                                                                                                                                                                                                                do {
                                                                                                                                                                                                                                                                                                                    value4 = ((Player)value).getFruitTreePatchManager();
                                                                                                                                                                                                                                                                                                                    ((DataOutputStream)value6).writeLong(((FruitTreePatchManager)value4).lastUpdateTicks[value5]);
                                                                                                                                                                                                                                                                                                                    ++value5;
                                                                                                                                                                                                                                                                                                                    value4 = ((Player)value).getFruitTreePatchManager();
                                                                                                                                                                                                                                                                                                                } while (value5 < ((FruitTreePatchManager)value4).lastUpdateTicks.length);
                                                                                                                                                                                                                                                                                                            }
                                                                                                                                                                                                                                                                                                            value5 = 0;
                                                                                                                                                                                                                                                                                                            if (!true) break writePlayerFileControlExit72;
                                                                                                                                                                                                                                                                                                            value4 = ((Player)value).getFruitTreePatchManager();
                                                                                                                                                                                                                                                                                                            if (value5 >= ((FruitTreePatchManager)value4).diseaseChanceMultipliers.length) break writePlayerFileControlExit71;
                                                                                                                                                                                                                                                                                                        }
                                                                                                                                                                                                                                                                                                        do {
                                                                                                                                                                                                                                                                                                            value4 = ((Player)value).getFruitTreePatchManager();
                                                                                                                                                                                                                                                                                                            ((DataOutputStream)value6).writeDouble(((FruitTreePatchManager)value4).diseaseChanceMultipliers[value5]);
                                                                                                                                                                                                                                                                                                            ++value5;
                                                                                                                                                                                                                                                                                                            value4 = ((Player)value).getFruitTreePatchManager();
                                                                                                                                                                                                                                                                                                        } while (value5 < ((FruitTreePatchManager)value4).diseaseChanceMultipliers.length);
                                                                                                                                                                                                                                                                                                    }
                                                                                                                                                                                                                                                                                                    value5 = 0;
                                                                                                                                                                                                                                                                                                    if (!true) break writePlayerFileControlExit70;
                                                                                                                                                                                                                                                                                                    value4 = ((Player)value).getFruitTreePatchManager();
                                                                                                                                                                                                                                                                                                    if (value5 >= ((FruitTreePatchManager)value4).protectionFlags.length) break writePlayerFileControlExit69;
                                                                                                                                                                                                                                                                                                }
                                                                                                                                                                                                                                                                                                do {
                                                                                                                                                                                                                                                                                                    value4 = ((Player)value).getFruitTreePatchManager();
                                                                                                                                                                                                                                                                                                    ((DataOutputStream)value6).writeBoolean(((FruitTreePatchManager)value4).protectionFlags[value5]);
                                                                                                                                                                                                                                                                                                    ++value5;
                                                                                                                                                                                                                                                                                                    value4 = ((Player)value).getFruitTreePatchManager();
                                                                                                                                                                                                                                                                                                } while (value5 < ((FruitTreePatchManager)value4).protectionFlags.length);
                                                                                                                                                                                                                                                                                            }
                                                                                                                                                                                                                                                                                            value5 = 0;
                                                                                                                                                                                                                                                                                            if (!true) break writePlayerFileControlExit68;
                                                                                                                                                                                                                                                                                            value4 = ((Player)value).getHerbPatchManager();
                                                                                                                                                                                                                                                                                            if (value5 >= ((HerbPatchManager)value4).growthStages.length) break writePlayerFileControlExit67;
                                                                                                                                                                                                                                                                                        }
                                                                                                                                                                                                                                                                                        do {
                                                                                                                                                                                                                                                                                            value4 = ((Player)value).getHerbPatchManager();
                                                                                                                                                                                                                                                                                            ((DataOutputStream)value6).writeInt(((HerbPatchManager)value4).growthStages[value5]);
                                                                                                                                                                                                                                                                                            ++value5;
                                                                                                                                                                                                                                                                                            value4 = ((Player)value).getHerbPatchManager();
                                                                                                                                                                                                                                                                                        } while (value5 < ((HerbPatchManager)value4).growthStages.length);
                                                                                                                                                                                                                                                                                    }
                                                                                                                                                                                                                                                                                    value5 = 0;
                                                                                                                                                                                                                                                                                    if (!true) break writePlayerFileControlExit66;
                                                                                                                                                                                                                                                                                    value4 = ((Player)value).getHerbPatchManager();
                                                                                                                                                                                                                                                                                    if (value5 >= ((HerbPatchManager)value4).cropIds.length) break writePlayerFileControlExit65;
                                                                                                                                                                                                                                                                                }
                                                                                                                                                                                                                                                                                do {
                                                                                                                                                                                                                                                                                    value4 = ((Player)value).getHerbPatchManager();
                                                                                                                                                                                                                                                                                    ((DataOutputStream)value6).writeInt(((HerbPatchManager)value4).cropIds[value5]);
                                                                                                                                                                                                                                                                                    ++value5;
                                                                                                                                                                                                                                                                                    value4 = ((Player)value).getHerbPatchManager();
                                                                                                                                                                                                                                                                                } while (value5 < ((HerbPatchManager)value4).cropIds.length);
                                                                                                                                                                                                                                                                            }
                                                                                                                                                                                                                                                                            value5 = 0;
                                                                                                                                                                                                                                                                            if (!true) break writePlayerFileControlExit64;
                                                                                                                                                                                                                                                                            value4 = ((Player)value).getHerbPatchManager();
                                                                                                                                                                                                                                                                            if (value5 >= ((HerbPatchManager)value4).harvestAmounts.length) break writePlayerFileControlExit63;
                                                                                                                                                                                                                                                                        }
                                                                                                                                                                                                                                                                        do {
                                                                                                                                                                                                                                                                            value4 = ((Player)value).getHerbPatchManager();
                                                                                                                                                                                                                                                                            ((DataOutputStream)value6).writeInt(((HerbPatchManager)value4).harvestAmounts[value5]);
                                                                                                                                                                                                                                                                            ++value5;
                                                                                                                                                                                                                                                                            value4 = ((Player)value).getHerbPatchManager();
                                                                                                                                                                                                                                                                        } while (value5 < ((HerbPatchManager)value4).harvestAmounts.length);
                                                                                                                                                                                                                                                                    }
                                                                                                                                                                                                                                                                    value5 = 0;
                                                                                                                                                                                                                                                                    if (!true) break writePlayerFileControlExit62;
                                                                                                                                                                                                                                                                    value4 = ((Player)value).getHerbPatchManager();
                                                                                                                                                                                                                                                                    if (value5 >= ((HerbPatchManager)value4).patchStates.length) break writePlayerFileControlExit61;
                                                                                                                                                                                                                                                                }
                                                                                                                                                                                                                                                                do {
                                                                                                                                                                                                                                                                    value4 = ((Player)value).getHerbPatchManager();
                                                                                                                                                                                                                                                                    ((DataOutputStream)value6).writeInt(((HerbPatchManager)value4).patchStates[value5]);
                                                                                                                                                                                                                                                                    ++value5;
                                                                                                                                                                                                                                                                    value4 = ((Player)value).getHerbPatchManager();
                                                                                                                                                                                                                                                                } while (value5 < ((HerbPatchManager)value4).patchStates.length);
                                                                                                                                                                                                                                                            }
                                                                                                                                                                                                                                                            value5 = 0;
                                                                                                                                                                                                                                                            if (!true) break writePlayerFileControlExit60;
                                                                                                                                                                                                                                                            value4 = ((Player)value).getHerbPatchManager();
                                                                                                                                                                                                                                                            if (value5 >= ((HerbPatchManager)value4).lastUpdateTicks.length) break writePlayerFileControlExit59;
                                                                                                                                                                                                                                                        }
                                                                                                                                                                                                                                                        do {
                                                                                                                                                                                                                                                            value4 = ((Player)value).getHerbPatchManager();
                                                                                                                                                                                                                                                            ((DataOutputStream)value6).writeLong(((HerbPatchManager)value4).lastUpdateTicks[value5]);
                                                                                                                                                                                                                                                            ++value5;
                                                                                                                                                                                                                                                            value4 = ((Player)value).getHerbPatchManager();
                                                                                                                                                                                                                                                        } while (value5 < ((HerbPatchManager)value4).lastUpdateTicks.length);
                                                                                                                                                                                                                                                    }
                                                                                                                                                                                                                                                    value5 = 0;
                                                                                                                                                                                                                                                    if (!true) break writePlayerFileControlExit58;
                                                                                                                                                                                                                                                    value4 = ((Player)value).getHerbPatchManager();
                                                                                                                                                                                                                                                    if (value5 >= ((HerbPatchManager)value4).diseaseChanceMultipliers.length) break writePlayerFileControlExit57;
                                                                                                                                                                                                                                                }
                                                                                                                                                                                                                                                do {
                                                                                                                                                                                                                                                    value4 = ((Player)value).getHerbPatchManager();
                                                                                                                                                                                                                                                    ((DataOutputStream)value6).writeDouble(((HerbPatchManager)value4).diseaseChanceMultipliers[value5]);
                                                                                                                                                                                                                                                    ++value5;
                                                                                                                                                                                                                                                    value4 = ((Player)value).getHerbPatchManager();
                                                                                                                                                                                                                                                } while (value5 < ((HerbPatchManager)value4).diseaseChanceMultipliers.length);
                                                                                                                                                                                                                                            }
                                                                                                                                                                                                                                            value5 = 0;
                                                                                                                                                                                                                                            if (!true) break writePlayerFileControlExit56;
                                                                                                                                                                                                                                            value4 = ((Player)value).getHopsPatchManager();
                                                                                                                                                                                                                                            if (value5 >= ((HopsPatchManager)value4).growthStages.length) break writePlayerFileControlExit55;
                                                                                                                                                                                                                                        }
                                                                                                                                                                                                                                        do {
                                                                                                                                                                                                                                            value4 = ((Player)value).getHopsPatchManager();
                                                                                                                                                                                                                                            ((DataOutputStream)value6).writeInt(((HopsPatchManager)value4).growthStages[value5]);
                                                                                                                                                                                                                                            ++value5;
                                                                                                                                                                                                                                            value4 = ((Player)value).getHopsPatchManager();
                                                                                                                                                                                                                                        } while (value5 < ((HopsPatchManager)value4).growthStages.length);
                                                                                                                                                                                                                                    }
                                                                                                                                                                                                                                    value5 = 0;
                                                                                                                                                                                                                                    if (!true) break writePlayerFileControlExit54;
                                                                                                                                                                                                                                    value4 = ((Player)value).getHopsPatchManager();
                                                                                                                                                                                                                                    if (value5 >= ((HopsPatchManager)value4).cropIds.length) break writePlayerFileControlExit53;
                                                                                                                                                                                                                                }
                                                                                                                                                                                                                                do {
                                                                                                                                                                                                                                    value4 = ((Player)value).getHopsPatchManager();
                                                                                                                                                                                                                                    ((DataOutputStream)value6).writeInt(((HopsPatchManager)value4).cropIds[value5]);
                                                                                                                                                                                                                                    ++value5;
                                                                                                                                                                                                                                    value4 = ((Player)value).getHopsPatchManager();
                                                                                                                                                                                                                                } while (value5 < ((HopsPatchManager)value4).cropIds.length);
                                                                                                                                                                                                                            }
                                                                                                                                                                                                                            value5 = 0;
                                                                                                                                                                                                                            if (!true) break writePlayerFileControlExit52;
                                                                                                                                                                                                                            value4 = ((Player)value).getHopsPatchManager();
                                                                                                                                                                                                                            if (value5 >= ((HopsPatchManager)value4).harvestAmounts.length) break writePlayerFileControlExit51;
                                                                                                                                                                                                                        }
                                                                                                                                                                                                                        do {
                                                                                                                                                                                                                            value4 = ((Player)value).getHopsPatchManager();
                                                                                                                                                                                                                            ((DataOutputStream)value6).writeInt(((HopsPatchManager)value4).harvestAmounts[value5]);
                                                                                                                                                                                                                            ++value5;
                                                                                                                                                                                                                            value4 = ((Player)value).getHopsPatchManager();
                                                                                                                                                                                                                        } while (value5 < ((HopsPatchManager)value4).harvestAmounts.length);
                                                                                                                                                                                                                    }
                                                                                                                                                                                                                    value5 = 0;
                                                                                                                                                                                                                    if (!true) break writePlayerFileControlExit50;
                                                                                                                                                                                                                    value4 = ((Player)value).getHopsPatchManager();
                                                                                                                                                                                                                    if (value5 >= ((HopsPatchManager)value4).patchStates.length) break writePlayerFileControlExit49;
                                                                                                                                                                                                                }
                                                                                                                                                                                                                do {
                                                                                                                                                                                                                    value4 = ((Player)value).getHopsPatchManager();
                                                                                                                                                                                                                    ((DataOutputStream)value6).writeInt(((HopsPatchManager)value4).patchStates[value5]);
                                                                                                                                                                                                                    ++value5;
                                                                                                                                                                                                                    value4 = ((Player)value).getHopsPatchManager();
                                                                                                                                                                                                                } while (value5 < ((HopsPatchManager)value4).patchStates.length);
                                                                                                                                                                                                            }
                                                                                                                                                                                                            value5 = 0;
                                                                                                                                                                                                            if (!true) break writePlayerFileControlExit48;
                                                                                                                                                                                                            value4 = ((Player)value).getHopsPatchManager();
                                                                                                                                                                                                            if (value5 >= ((HopsPatchManager)value4).lastUpdateTicks.length) break writePlayerFileControlExit47;
                                                                                                                                                                                                        }
                                                                                                                                                                                                        do {
                                                                                                                                                                                                            value4 = ((Player)value).getHopsPatchManager();
                                                                                                                                                                                                            ((DataOutputStream)value6).writeLong(((HopsPatchManager)value4).lastUpdateTicks[value5]);
                                                                                                                                                                                                            ++value5;
                                                                                                                                                                                                            value4 = ((Player)value).getHopsPatchManager();
                                                                                                                                                                                                        } while (value5 < ((HopsPatchManager)value4).lastUpdateTicks.length);
                                                                                                                                                                                                    }
                                                                                                                                                                                                    value5 = 0;
                                                                                                                                                                                                    if (!true) break writePlayerFileControlExit46;
                                                                                                                                                                                                    value4 = ((Player)value).getHopsPatchManager();
                                                                                                                                                                                                    if (value5 >= ((HopsPatchManager)value4).diseaseChanceMultipliers.length) break writePlayerFileControlExit45;
                                                                                                                                                                                                }
                                                                                                                                                                                                do {
                                                                                                                                                                                                    value4 = ((Player)value).getHopsPatchManager();
                                                                                                                                                                                                    ((DataOutputStream)value6).writeDouble(((HopsPatchManager)value4).diseaseChanceMultipliers[value5]);
                                                                                                                                                                                                    ++value5;
                                                                                                                                                                                                    value4 = ((Player)value).getHopsPatchManager();
                                                                                                                                                                                                } while (value5 < ((HopsPatchManager)value4).diseaseChanceMultipliers.length);
                                                                                                                                                                                            }
                                                                                                                                                                                            value5 = 0;
                                                                                                                                                                                            if (!true) break writePlayerFileControlExit44;
                                                                                                                                                                                            value4 = ((Player)value).getHopsPatchManager();
                                                                                                                                                                                            if (value5 >= ((HopsPatchManager)value4).protectionFlags.length) break writePlayerFileControlExit43;
                                                                                                                                                                                        }
                                                                                                                                                                                        do {
                                                                                                                                                                                            value4 = ((Player)value).getHopsPatchManager();
                                                                                                                                                                                            ((DataOutputStream)value6).writeBoolean(((HopsPatchManager)value4).protectionFlags[value5]);
                                                                                                                                                                                            ++value5;
                                                                                                                                                                                            value4 = ((Player)value).getHopsPatchManager();
                                                                                                                                                                                        } while (value5 < ((HopsPatchManager)value4).protectionFlags.length);
                                                                                                                                                                                    }
                                                                                                                                                                                    value5 = 0;
                                                                                                                                                                                    if (!true) break writePlayerFileControlExit42;
                                                                                                                                                                                    value4 = ((Player)value).getSpecialTreePatchManager();
                                                                                                                                                                                    if (value5 >= ((SpecialTreePatchManager)value4).growthStages.length) break writePlayerFileControlExit41;
                                                                                                                                                                                }
                                                                                                                                                                                do {
                                                                                                                                                                                    value4 = ((Player)value).getSpecialTreePatchManager();
                                                                                                                                                                                    ((DataOutputStream)value6).writeInt(((SpecialTreePatchManager)value4).growthStages[value5]);
                                                                                                                                                                                    ++value5;
                                                                                                                                                                                    value4 = ((Player)value).getSpecialTreePatchManager();
                                                                                                                                                                                } while (value5 < ((SpecialTreePatchManager)value4).growthStages.length);
                                                                                                                                                                            }
                                                                                                                                                                            value5 = 0;
                                                                                                                                                                            if (!true) break writePlayerFileControlExit40;
                                                                                                                                                                            value4 = ((Player)value).getSpecialTreePatchManager();
                                                                                                                                                                            if (value5 >= ((SpecialTreePatchManager)value4).treeIds.length) break writePlayerFileControlExit39;
                                                                                                                                                                        }
                                                                                                                                                                        do {
                                                                                                                                                                            value4 = ((Player)value).getSpecialTreePatchManager();
                                                                                                                                                                            ((DataOutputStream)value6).writeInt(((SpecialTreePatchManager)value4).treeIds[value5]);
                                                                                                                                                                            ++value5;
                                                                                                                                                                            value4 = ((Player)value).getSpecialTreePatchManager();
                                                                                                                                                                        } while (value5 < ((SpecialTreePatchManager)value4).treeIds.length);
                                                                                                                                                                    }
                                                                                                                                                                    value5 = 0;
                                                                                                                                                                    if (!true) break writePlayerFileControlExit38;
                                                                                                                                                                    value4 = ((Player)value).getSpecialTreePatchManager();
                                                                                                                                                                    if (value5 >= ((SpecialTreePatchManager)value4).patchStates.length) break writePlayerFileControlExit37;
                                                                                                                                                                }
                                                                                                                                                                do {
                                                                                                                                                                    value4 = ((Player)value).getSpecialTreePatchManager();
                                                                                                                                                                    ((DataOutputStream)value6).writeInt(((SpecialTreePatchManager)value4).patchStates[value5]);
                                                                                                                                                                    ++value5;
                                                                                                                                                                    value4 = ((Player)value).getSpecialTreePatchManager();
                                                                                                                                                                } while (value5 < ((SpecialTreePatchManager)value4).patchStates.length);
                                                                                                                                                            }
                                                                                                                                                            value5 = 0;
                                                                                                                                                            if (!true) break writePlayerFileControlExit36;
                                                                                                                                                            value4 = ((Player)value).getSpecialTreePatchManager();
                                                                                                                                                            if (value5 >= ((SpecialTreePatchManager)value4).lastUpdateTicks.length) break writePlayerFileControlExit35;
                                                                                                                                                        }
                                                                                                                                                        do {
                                                                                                                                                            value4 = ((Player)value).getSpecialTreePatchManager();
                                                                                                                                                            ((DataOutputStream)value6).writeLong(((SpecialTreePatchManager)value4).lastUpdateTicks[value5]);
                                                                                                                                                            ++value5;
                                                                                                                                                            value4 = ((Player)value).getSpecialTreePatchManager();
                                                                                                                                                        } while (value5 < ((SpecialTreePatchManager)value4).lastUpdateTicks.length);
                                                                                                                                                    }
                                                                                                                                                    value5 = 0;
                                                                                                                                                    if (!true) break writePlayerFileControlExit34;
                                                                                                                                                    value4 = ((Player)value).getSpecialTreePatchManager();
                                                                                                                                                    if (value5 >= ((SpecialTreePatchManager)value4).diseaseChanceMultipliers.length) break writePlayerFileControlExit33;
                                                                                                                                                }
                                                                                                                                                do {
                                                                                                                                                    value4 = ((Player)value).getSpecialTreePatchManager();
                                                                                                                                                    ((DataOutputStream)value6).writeDouble(((SpecialTreePatchManager)value4).diseaseChanceMultipliers[value5]);
                                                                                                                                                    ++value5;
                                                                                                                                                    value4 = ((Player)value).getSpecialTreePatchManager();
                                                                                                                                                } while (value5 < ((SpecialTreePatchManager)value4).diseaseChanceMultipliers.length);
                                                                                                                                            }
                                                                                                                                            value5 = 0;
                                                                                                                                            if (!true) break writePlayerFileControlExit32;
                                                                                                                                            value4 = ((Player)value).getSpecialCropPatchManager();
                                                                                                                                            if (value5 >= ((SpecialCropPatchManager)value4).growthStages.length) break writePlayerFileControlExit31;
                                                                                                                                        }
                                                                                                                                        do {
                                                                                                                                            value4 = ((Player)value).getSpecialCropPatchManager();
                                                                                                                                            ((DataOutputStream)value6).writeInt(((SpecialCropPatchManager)value4).growthStages[value5]);
                                                                                                                                            ++value5;
                                                                                                                                            value4 = ((Player)value).getSpecialCropPatchManager();
                                                                                                                                        } while (value5 < ((SpecialCropPatchManager)value4).growthStages.length);
                                                                                                                                    }
                                                                                                                                    value5 = 0;
                                                                                                                                    if (!true) break writePlayerFileControlExit30;
                                                                                                                                    value4 = ((Player)value).getSpecialCropPatchManager();
                                                                                                                                    if (value5 >= ((SpecialCropPatchManager)value4).cropIds.length) break writePlayerFileControlExit29;
                                                                                                                                }
                                                                                                                                do {
                                                                                                                                    value4 = ((Player)value).getSpecialCropPatchManager();
                                                                                                                                    ((DataOutputStream)value6).writeInt(((SpecialCropPatchManager)value4).cropIds[value5]);
                                                                                                                                    ++value5;
                                                                                                                                    value4 = ((Player)value).getSpecialCropPatchManager();
                                                                                                                                } while (value5 < ((SpecialCropPatchManager)value4).cropIds.length);
                                                                                                                            }
                                                                                                                            value5 = 0;
                                                                                                                            if (!true) break writePlayerFileControlExit28;
                                                                                                                            value4 = ((Player)value).getSpecialCropPatchManager();
                                                                                                                            if (value5 >= ((SpecialCropPatchManager)value4).patchStates.length) break writePlayerFileControlExit27;
                                                                                                                        }
                                                                                                                        do {
                                                                                                                            value4 = ((Player)value).getSpecialCropPatchManager();
                                                                                                                            ((DataOutputStream)value6).writeInt(((SpecialCropPatchManager)value4).patchStates[value5]);
                                                                                                                            ++value5;
                                                                                                                            value4 = ((Player)value).getSpecialCropPatchManager();
                                                                                                                        } while (value5 < ((SpecialCropPatchManager)value4).patchStates.length);
                                                                                                                    }
                                                                                                                    value5 = 0;
                                                                                                                    if (!true) break writePlayerFileControlExit26;
                                                                                                                    value4 = ((Player)value).getSpecialCropPatchManager();
                                                                                                                    if (value5 >= ((SpecialCropPatchManager)value4).lastUpdateTicks.length) break writePlayerFileControlExit25;
                                                                                                                }
                                                                                                                do {
                                                                                                                    value4 = ((Player)value).getSpecialCropPatchManager();
                                                                                                                    ((DataOutputStream)value6).writeLong(((SpecialCropPatchManager)value4).lastUpdateTicks[value5]);
                                                                                                                    ++value5;
                                                                                                                    value4 = ((Player)value).getSpecialCropPatchManager();
                                                                                                                } while (value5 < ((SpecialCropPatchManager)value4).lastUpdateTicks.length);
                                                                                                            }
                                                                                                            value5 = 0;
                                                                                                            if (!true) break writePlayerFileControlExit24;
                                                                                                            value4 = ((Player)value).getSpecialCropPatchManager();
                                                                                                            if (value5 >= ((SpecialCropPatchManager)value4).diseaseChanceMultipliers.length) break writePlayerFileControlExit23;
                                                                                                        }
                                                                                                        do {
                                                                                                            value4 = ((Player)value).getSpecialCropPatchManager();
                                                                                                            ((DataOutputStream)value6).writeDouble(((SpecialCropPatchManager)value4).diseaseChanceMultipliers[value5]);
                                                                                                            ++value5;
                                                                                                            value4 = ((Player)value).getSpecialCropPatchManager();
                                                                                                        } while (value5 < ((SpecialCropPatchManager)value4).diseaseChanceMultipliers.length);
                                                                                                    }
                                                                                                    value5 = 0;
                                                                                                    if (!true) break writePlayerFileControlExit22;
                                                                                                    value4 = ((Player)value).getTreePatchManager();
                                                                                                    if (value5 >= ((TreePatchManager)value4).growthStages.length) break writePlayerFileControlExit21;
                                                                                                }
                                                                                                do {
                                                                                                    value4 = ((Player)value).getTreePatchManager();
                                                                                                    ((DataOutputStream)value6).writeInt(((TreePatchManager)value4).growthStages[value5]);
                                                                                                    ++value5;
                                                                                                    value4 = ((Player)value).getTreePatchManager();
                                                                                                } while (value5 < ((TreePatchManager)value4).growthStages.length);
                                                                                            }
                                                                                            value5 = 0;
                                                                                            if (!true) break writePlayerFileControlExit20;
                                                                                            value4 = ((Player)value).getTreePatchManager();
                                                                                            if (value5 >= ((TreePatchManager)value4).treeIds.length) break writePlayerFileControlExit19;
                                                                                        }
                                                                                        do {
                                                                                            value4 = ((Player)value).getTreePatchManager();
                                                                                            ((DataOutputStream)value6).writeInt(((TreePatchManager)value4).treeIds[value5]);
                                                                                            ++value5;
                                                                                            value4 = ((Player)value).getTreePatchManager();
                                                                                        } while (value5 < ((TreePatchManager)value4).treeIds.length);
                                                                                    }
                                                                                    value5 = 0;
                                                                                    if (!true) break writePlayerFileControlExit18;
                                                                                    value4 = ((Player)value).getTreePatchManager();
                                                                                    if (value5 >= ((TreePatchManager)value4).patchData.length) break writePlayerFileControlExit17;
                                                                                }
                                                                                do {
                                                                                    value4 = ((Player)value).getTreePatchManager();
                                                                                    ((DataOutputStream)value6).writeInt(((TreePatchManager)value4).patchData[value5]);
                                                                                    ++value5;
                                                                                    value4 = ((Player)value).getTreePatchManager();
                                                                                } while (value5 < ((TreePatchManager)value4).patchData.length);
                                                                            }
                                                                            value5 = 0;
                                                                            if (!true) break writePlayerFileControlExit16;
                                                                            value4 = ((Player)value).getTreePatchManager();
                                                                            if (value5 >= ((TreePatchManager)value4).patchStates.length) break writePlayerFileControlExit15;
                                                                        }
                                                                        do {
                                                                            value4 = ((Player)value).getTreePatchManager();
                                                                            ((DataOutputStream)value6).writeInt(((TreePatchManager)value4).patchStates[value5]);
                                                                            ++value5;
                                                                            value4 = ((Player)value).getTreePatchManager();
                                                                        } while (value5 < ((TreePatchManager)value4).patchStates.length);
                                                                    }
                                                                    value5 = 0;
                                                                    if (!true) break writePlayerFileControlExit14;
                                                                    value4 = ((Player)value).getTreePatchManager();
                                                                    if (value5 >= ((TreePatchManager)value4).lastUpdateTicks.length) break writePlayerFileControlExit13;
                                                                }
                                                                do {
                                                                    value4 = ((Player)value).getTreePatchManager();
                                                                    ((DataOutputStream)value6).writeLong(((TreePatchManager)value4).lastUpdateTicks[value5]);
                                                                    ++value5;
                                                                    value4 = ((Player)value).getTreePatchManager();
                                                                } while (value5 < ((TreePatchManager)value4).lastUpdateTicks.length);
                                                            }
                                                            value5 = 0;
                                                            if (!true) break writePlayerFileControlExit12;
                                                            value4 = ((Player)value).getTreePatchManager();
                                                            if (value5 >= ((TreePatchManager)value4).diseaseChanceMultipliers.length) break writePlayerFileControlExit11;
                                                        }
                                                        do {
                                                            value4 = ((Player)value).getTreePatchManager();
                                                            ((DataOutputStream)value6).writeDouble(((TreePatchManager)value4).diseaseChanceMultipliers[value5]);
                                                            ++value5;
                                                            value4 = ((Player)value).getTreePatchManager();
                                                        } while (value5 < ((TreePatchManager)value4).diseaseChanceMultipliers.length);
                                                    }
                                                    value5 = 0;
                                                    if (!true) break writePlayerFileControlExit10;
                                                    value4 = ((Player)value).getTreePatchManager();
                                                    if (value5 >= ((TreePatchManager)value4).protectionFlags.length) break writePlayerFileControlExit9;
                                                }
                                                do {
                                                    value4 = ((Player)value).getTreePatchManager();
                                                    ((DataOutputStream)value6).writeBoolean(((TreePatchManager)value4).protectionFlags[value5]);
                                                    ++value5;
                                                    value4 = ((Player)value).getTreePatchManager();
                                                } while (value5 < ((TreePatchManager)value4).protectionFlags.length);
                                            }
                                            value5 = 0;
                                            if (!true) break writePlayerFileControlExit8;
                                            value4 = ((Player)value).getCompostBinManager();
                                            if (value5 >= ((CompostBinManager)value4).states.length) break writePlayerFileControlExit7;
                                        }
                                        do {
                                            value4 = ((Player)value).getCompostBinManager();
                                            ((DataOutputStream)value6).writeInt(((CompostBinManager)value4).states[value5]);
                                            ++value5;
                                            value4 = ((Player)value).getCompostBinManager();
                                        } while (value5 < ((CompostBinManager)value4).states.length);
                                    }
                                    value5 = 0;
                                    if (!true) break writePlayerFileControlExit6;
                                    value4 = ((Player)value).getCompostBinManager();
                                    if (value5 >= ((CompostBinManager)value4).lastUpdateTicks.length) break writePlayerFileControlExit5;
                                }
                                do {
                                    value4 = ((Player)value).getCompostBinManager();
                                    ((DataOutputStream)value6).writeLong(((CompostBinManager)value4).lastUpdateTicks[value5]);
                                    ++value5;
                                    value4 = ((Player)value).getCompostBinManager();
                                } while (value5 < ((CompostBinManager)value4).lastUpdateTicks.length);
                            }
                            value5 = 0;
                            if (!true) break writePlayerFileControlExit4;
                            value4 = ((Player)value).getCompostBinManager();
                            if (value5 >= ((CompostBinManager)value4).itemIds.length) break writePlayerFileControlExit3;
                        }
                        do {
                            value4 = ((Player)value).getCompostBinManager();
                            ((DataOutputStream)value6).writeInt(((CompostBinManager)value4).itemIds[value5]);
                            ++value5;
                            value4 = ((Player)value).getCompostBinManager();
                        } while (value5 < ((CompostBinManager)value4).itemIds.length);
                    }
                    value5 = 0;
                    if (!true) break writePlayerFileControlExit2;
                    value4 = ((Player)value).getFarmingToolStore();
                    if (value5 >= ((FarmingToolStore)value4).storedAmounts.length) break writePlayerFileControlExit1;
                }
                do {
                    value4 = ((Player)value).getFarmingToolStore();
                    ((DataOutputStream)value6).writeInt(((FarmingToolStore)value4).storedAmounts[value5]);
                    ++value5;
                    value4 = ((Player)value).getFarmingToolStore();
                } while (value5 < ((FarmingToolStore)value4).storedAmounts.length);
            }
            ((DataOutputStream)value6).writeInt(((Player)value).getSlayerManager().slayerMasterId);
            ((DataOutputStream)value6).writeUTF(((Player)value).getSlayerManager().slayerTaskName);
            ((DataOutputStream)value6).writeInt(((Player)value).getSlayerManager().taskAmount);
            int spellbook = value5 = ((Player)value).getSpellbook() == Spellbook.ANCIENT ? 1 : 0;
            if (((Player)value).getSpellbook() == Spellbook.NECROMANCY) {
                value5 = ((Player)value).previousSpellbookBeforeNecromancy == Spellbook.ANCIENT ? 1 : 0;
            }
            ((DataOutputStream)value6).writeBoolean(value5 != 0);
            ((DataOutputStream)value6).writeBoolean(((Player)value).isBrimhavenOpen());
            value4 = value;
            ((DataOutputStream)value6).writeBoolean(((Player)value4).killedClueAttacker);
            int index = 0;
            while (index < musicUnlockConfigIds.length) {
                ((DataOutputStream)value6).writeInt(((Player)value).configStates[musicUnlockConfigIds[index]]);
                index += 1;
            }
            index = 0;
            while (index < QuestDefinition.questStateCapacity) {
                ((DataOutputStream)value6).writeInt(((Player)value).getQuestState(index, true));
                index += 1;
            }
            ((DataOutputStream)value6).writeByte(((Player)value).gangAffiliation);
            ((DataOutputStream)value6).writeByte(((Player)value).piratesTreasureBananaCrateCount);
            ((DataOutputStream)value6).writeBoolean(((Player)value).treasureTrailNavigationTaught);
            ((DataOutputStream)value6).writeByte(((Player)value).getCoalTruckCoalCount());
            if (!((Player)value).ownsClueScroll()) {
                ((Player)value).treasureTrailStepCount = 0;
            }
            ((DataOutputStream)value6).writeByte(((Player)value).treasureTrailStepCount);
            index = PuzzleBoxHandler.isCluePuzzleSolved((Player)value) ? 1 : 0;
            if (!((Player)value).ownsCluePuzzleBox()) {
                index = 0;
            }
            ((DataOutputStream)value6).writeBoolean(index != 0);
            ((DataOutputStream)value6).writeByte(((Player)value).skeletonSkinUnlocked);
            ((DataOutputStream)value6).writeInt(((Player)value).getBossPetUnlockFlags());
            ((DataOutputStream)value6).writeBoolean(((Player)value).barrowsDoorPuzzleSolved);
            ((DataOutputStream)value6).writeBoolean(((Player)value).barrowsChestOpened);
            ((DataOutputStream)value6).writeInt(((Player)value).configStates[452]);
            Object value7 = value;
            ((DataOutputStream)value6).writeByte(((Player)value7).flourMillHopperGrainCount);
            ((DataOutputStream)value6).writeInt(((Player)value).configStates[FlourMillHandler.flourBinConfigId]);
            ((DataOutputStream)value6).writeInt(((Player)value).questRandomSeed);
            value3 = 0;
            while (value3 < QuestDefinition.questStateCapacity) {
                ((DataOutputStream)value6).writeInt(((Player)value).questProgressFlags[value3]);
                ++value3;
            }
            value3 = 0;
            while (value3 < 100) {
                ((DataOutputStream)value6).writeInt(((Player)value).questHookStates[value3]);
                ++value3;
            }
            ((DataOutputStream)value6).writeByte(((Player)value).getPublicChatMode());
            ((DataOutputStream)value6).writeByte(((Player)value).getPrivateChatMode());
            ((DataOutputStream)value6).writeByte(((Player)value).getTradeMode());
            ((DataOutputStream)value6).writeInt(((Player)value).reservedSaveInt1);
            ((DataOutputStream)value6).writeLong(((Player)value).reservedSaveLong1);
            ((DataOutputStream)value6).writeInt(((Player)value).reservedSaveInt2);
            ((DataOutputStream)value6).writeInt(((Player)value).reservedSaveInt3);
            value7 = value;
            ((DataOutputStream)value6).writeUTF("");
            ((DataOutputStream)value6).writeInt(((Player)value).familyCrestGauntletItemId);
            ((DataOutputStream)value6).writeInt(((Player)value).mageArenaFlamesOfZamorakCastsRemaining);
            ((DataOutputStream)value6).writeInt(((Player)value).mageArenaSaradominStrikeCastsRemaining);
            ((DataOutputStream)value6).writeInt(((Player)value).mageArenaClawsOfGuthixCastsRemaining);
            ((DataOutputStream)value6).writeByte(((Player)value).mageArenaProgressStage);
            value3 = 0;
            while (value3 < 6) {
                ((DataOutputStream)value6).writeBoolean(((Player)value).grandExchangeSellOfferFlags[value3]);
                ++value3;
            }
            value3 = 0;
            while (value3 < 6) {
                ((DataOutputStream)value6).writeInt(((Player)value).grandExchangeItemIds[value3]);
                ++value3;
            }
            value3 = 0;
            while (value3 < 6) {
                ((DataOutputStream)value6).writeInt(((Player)value).grandExchangeQuantities[value3]);
                ++value3;
            }
            value3 = 0;
            while (value3 < 6) {
                ((DataOutputStream)value6).writeInt(((Player)value).grandExchangeUnitPrices[value3]);
                ++value3;
            }
            value3 = 0;
            while (value3 < 6) {
                ((DataOutputStream)value6).writeBoolean(((Player)value).grandExchangeCancelledFlags[value3]);
                ++value3;
            }
            value3 = 0;
            while (value3 < 6) {
                ((DataOutputStream)value6).writeInt(((Player)value).grandExchangeCompletedQuantities[value3]);
                ++value3;
            }
            value3 = 0;
            while (value3 < 6) {
                ((DataOutputStream)value6).writeInt(((Player)value).grandExchangeTotalPrices[value3]);
                ++value3;
            }
            value3 = 0;
            while (value3 < 6) {
                ((DataOutputStream)value6).writeInt(((Player)value).grandExchangePrimaryCollectAmounts[value3]);
                ++value3;
            }
            value3 = 0;
            while (value3 < 6) {
                ((DataOutputStream)value6).writeInt(((Player)value).grandExchangeSecondaryCollectAmounts[value3]);
                ++value3;
            }
            value3 = 0;
            while (value3 < 6) {
                ((DataOutputStream)value6).writeBoolean(((Player)value).grandExchangeFinishMessagePending[value3]);
                ++value3;
            }
            ((DataOutputStream)value6).writeInt(((Player)value).getTelekineticTheatreController().pizazzPoints);
            ((DataOutputStream)value6).writeInt(((Player)value).getEnchantmentChamberController().pizazzPoints);
            ((DataOutputStream)value6).writeInt(((Player)value).getAlchemistPlaygroundController().pizazzPoints);
            ((DataOutputStream)value6).writeInt(((Player)value).getCreatureGraveyardController().pizazzPoints);
            ((DataOutputStream)value6).writeBoolean(((Player)value).bonesToPeachesUnlocked);
            ((DataOutputStream)value6).writeByte(((Player)value).getTelekineticTheatreController().mazeIndex);
            ((DataOutputStream)value6).writeBoolean(((Player)value).getTelekineticTheatreController().mazeSolved);
            ((DataOutputStream)value6).writeByte(((Player)value).getTelekineticTheatreController().consecutiveMazesSolved);
            ((DataOutputStream)value6).writeInt(((Player)value).barrowsRewardPotential);
            ((DataOutputStream)value6).writeByte(((Player)value).gameMode);
            ((DataOutputStream)value6).writeInt(((Player)value).barrowsRunsCompleted);
            ((DataOutputStream)value6).writeLong(((Player)value).godWarsLastAltarBlessingMillis);
            ((DataOutputStream)value6).writeInt(((Player)value).configStates[LEGACY_GOD_WARS_ROPE_CONFIG_ID]);
            value3 = 0;
            while (value3 < ((Player)value).godWarsKillCounts.length) {
                ((DataOutputStream)value6).writeInt(((Player)value).godWarsKillCounts[value3]);
                ++value3;
            }
            ((DataOutputStream)value6).writeByte(((Player)value).craftingThreadUseCount);
            ((DataOutputStream)value6).writeLong(((Player)value).membershipExpiresMillis);
            ((DataOutputStream)value6).writeByte(((Player)value).reservedSaveByte);
            ((DataOutputStream)value6).writeInt(((Player)value).configStates[33]);
            ((DataOutputStream)value6).writeShort(ServerSettings.cacheVersion);
            ((DataOutputStream)value6).writeInt(((Player)value).godBookPageFlags);
            ((DataOutputStream)value6).writeBoolean(((Player)value).swampCaveRopeAttached);
            ((DataOutputStream)value6).writeBoolean(((Player)value).lampOilStillFilled);
            ((DataOutputStream)value6).writeInt(((Player)value).enterTheAbyssMiniquestState);
            ((DataOutputStream)value6).writeInt(((Player)value).configStates[872]);
            ((DataOutputStream)value6).writeBoolean(((Player)value).botEnabled);
            if (((Player)value).botEnabled) {
                ((DataOutputStream)value6).writeByte(((Player)value).botMode);
                value3 = -1;
                value2 = -1;
                if (((Player)value).currentBotTask != null) {
                    value3 = ((Player)value).currentBotTask.getTaskTypeId();
                    value2 = ((Player)value).currentBotTask.getTaskIndexForType(value3);
                }
                int initialValue = -1;
                value5 = -1;
                if (((Player)value).deferredBotTask != null) {
                    initialValue = ((Player)value).deferredBotTask.getTaskTypeId();
                    value5 = ((Player)value).deferredBotTask.getTaskIndexForType(initialValue);
                }
                ((DataOutputStream)value6).writeByte(value3);
                ((DataOutputStream)value6).writeInt(value2);
                ((DataOutputStream)value6).writeByte(initialValue);
                ((DataOutputStream)value6).writeInt(value5);
                ((DataOutputStream)value6).writeUTF(((Player)value).botTaskState);
                value5 = 0;
                if (((Player)value).botTaskRequiredItems != null) {
                    value5 = ((Player)value).botTaskRequiredItems.length;
                }
                ((DataOutputStream)value6).writeByte(value5);
                int index2 = 0;
                while (index2 < value5) {
                    ((DataOutputStream)value6).writeInt(((Player)value).botTaskRequiredItems[index2].getId());
                    ((DataOutputStream)value6).writeInt(((Player)value).botTaskRequiredItems[index2].getAmount());
                    ++index2;
                }
                ((DataOutputStream)value6).writeInt(((Player)value).botFoodItemId);
                ((DataOutputStream)value6).writeByte(((Player)value).botPathSegmentIndex);
                ((DataOutputStream)value6).writeByte(((Player)value).botPathWaypointIndex);
                index2 = 0;
                value5 = -1;
                if (((Player)value).currentWorldRouteChoice != null) {
                    index2 = ((Player)value).currentWorldRouteChoice.isReversed() ? 1 : 0;
                    value5 = BotWorldRouteWalker.getRouteIndex(((Player)value).currentWorldRouteChoice);
                }
                ((DataOutputStream)value6).writeBoolean(index2 != 0);
                ((DataOutputStream)value6).writeLong(((Player)value).getBotTaskRuntimeMillis());
                ((DataOutputStream)value6).writeByte(((Player)value).botTaskDurationMinutes);
                ((DataOutputStream)value6).writeByte(value5);
                ((DataOutputStream)value6).writeByte(((Player)value).tradeAdvertMode);
                ((DataOutputStream)value6).writeInt(((Player)value).botAdvertItemId);
                ((DataOutputStream)value6).writeInt(((Player)value).tradeAdvertQuantityRemaining);
                ((DataOutputStream)value6).writeInt(((Player)value).tradeAdvertUnitPrice);
                ((DataOutputStream)value6).writeBoolean(((Player)value).tradeAdvertScam);
                ((DataOutputStream)value6).writeBoolean(((Player)value).tradeAdvertVariableQuantity);
                ((DataOutputStream)value6).writeInt(((Player)value).tradeAdvertLastOfferAmount);
                ((DataOutputStream)value6).writeByte(((Player)value).botShopBuyMode);
                ((DataOutputStream)value6).writeInt(((Player)value).botTaskItemId);
                ((DataOutputStream)value6).writeInt(((Player)value).botShopItemAmount);
                ((DataOutputStream)value6).writeByte(((Player)value).botShopSellItemIds.size());
                value5 = 0;
                while (value5 < ((Player)value).botShopSellItemIds.size()) {
                    ((DataOutputStream)value6).writeInt((Integer)((Player)value).botShopSellItemIds.get(value5));
                    ++value5;
                }
                ((DataOutputStream)value6).writeByte(((Player)value).botCombatLoadoutItemIds.size());
                value5 = 0;
                while (value5 < ((Player)value).botCombatLoadoutItemIds.size()) {
                    ((DataOutputStream)value6).writeInt((Integer)((Player)value).botCombatLoadoutItemIds.get(value5));
                    ++value5;
                }
                ((DataOutputStream)value6).writeByte(((Player)value).botCombatStyle);
                ((DataOutputStream)value6).writeByte(((Player)value).botSkillTargetSkillId);
                ((DataOutputStream)value6).writeByte(((Player)value).botSkillTargetLevel);
                ((DataOutputStream)value6).writeByte(((Player)value).botReservedGoalByte1);
                ((DataOutputStream)value6).writeByte(((Player)value).botReservedGoalByte2);
                ((DataOutputStream)value6).writeByte(((Player)value).botReservedGoalByte3);
                ((DataOutputStream)value6).writeByte(((Player)value).botReservedGoalByte4);
                ((DataOutputStream)value6).writeInt(((Player)value).botCompletionItemId);
                ((DataOutputStream)value6).writeInt(((Player)value).botCompletionItemAmount);
                ((DataOutputStream)value6).writeInt(((Player)value).botSecondaryCompletionItemId);
                ((DataOutputStream)value6).writeInt(((Player)value).botReservedGoalInt2);
                ((DataOutputStream)value6).writeInt(((Player)value).botReservedGoalInt3);
                ((DataOutputStream)value6).writeInt(((Player)value).botReservedGoalInt4);
                ((DataOutputStream)value6).writeBoolean(((Player)value).botTaskReturnToBankRequested);
                ((DataOutputStream)value6).writeBoolean(false);
                ((DataOutputStream)value6).writeBoolean(false);
                ((DataOutputStream)value6).writeBoolean(false);
                ((DataOutputStream)value6).writeBoolean(false);
                ((DataOutputStream)value6).writeBoolean(false);
                ((DataOutputStream)value6).writeByte(((Player)value).botElementalSpellIndex);
                ((DataOutputStream)value6).writeByte(0);
                ((DataOutputStream)value6).writeByte(0);
                ((DataOutputStream)value6).writeByte(0);
                ((DataOutputStream)value6).writeByte(0);
                ((DataOutputStream)value6).writeInt(0);
                ((DataOutputStream)value6).writeInt(0);
                ((DataOutputStream)value6).writeInt(0);
                ((DataOutputStream)value6).writeInt(0);
                ((DataOutputStream)value6).writeInt(0);
                ((DataOutputStream)value6).writeUTF("");
                ((DataOutputStream)value6).writeUTF("");
                ((DataOutputStream)value6).writeUTF("");
                ((DataOutputStream)value6).writeUTF("");
                ((DataOutputStream)value6).writeUTF("");
            }
            ((DataOutputStream)value6).writeLong(((Player)value).homeTeleportAvailableAtMillis);
            ((DataOutputStream)value6).writeBoolean(((Player)value).automaticMusicEnabled);
            ((DataOutputStream)value6).writeInt(((Player)value).configStates[LATE_MUSIC_UNLOCK_CONFIG_ID]);
            ((DataOutputStream)value6).flush();
            ((FilterOutputStream)value6).close();
            return;
        }
        catch (Exception exception) {
            value = exception;
            exception.printStackTrace();
            return;
        }
    }

    /*
     * Handled impossible loop by duplicating code
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    public static void saveCharacterFileRecord(CharacterFileRecord record) {
        if (!Boolean.getBoolean("soloscape.legacyCharacterWriter")) {
            try {
                CharacterJsonCodec.save(new File("./data/characters/" + record.username + ".json"), record);
            } catch (IOException exception) {
                throw new IllegalStateException("Cannot save JSON character " + record.username, exception);
            }
            return;
        }
        Object value = record;
        try {
            int value2;
            Object value3;
            Object value4;
            saveCharacterFileRecordControlExit1: {
                saveCharacterFileRecordControlExit2: {
                    saveCharacterFileRecordControlExit3: {
                        saveCharacterFileRecordControlExit4: {
                            new ElapsedTimer();
                            value4 = value;
                            value3 = new File("./data/characters/" + ((CharacterFileRecord)value4).username + ".dat");
                            if (!((File)value3).exists()) {
                                ((File)value3).createNewFile();
                            }
                            value3 = new FileOutputStream((File)value3);
                            value3 = new DataOutputStream((OutputStream)value3);
                            ((DataOutputStream)value3).writeShort(31);
                            value4 = value;
                            ((DataOutputStream)value3).writeUTF(((CharacterFileRecord)value4).username);
                            Object value5 = value;
                            ((DataOutputStream)value3).writeUTF(((CharacterFileRecord)value5).password);
                            value5 = value;
                            ((DataOutputStream)value3).writeUTF(((CharacterFileRecord)value5).hostAddress);
                            value4 = value;
                            ((DataOutputStream)value3).writeInt(((CharacterFileRecord)value4).playerRights);
                            value5 = value;
                            ((DataOutputStream)value3).writeUTF(((CharacterFileRecord)value5).legacyProfileString);
                            value5 = value;
                            ((DataOutputStream)value3).writeUTF(((CharacterFileRecord)value5).profileString1);
                            value5 = value;
                            ((DataOutputStream)value3).writeUTF(((CharacterFileRecord)value5).profileString2);
                            value5 = value;
                            ((DataOutputStream)value3).writeLong(((CharacterFileRecord)value5).lastSavedMillis);
                            value5 = value;
                            ((DataOutputStream)value3).writeLong(((CharacterFileRecord)value5).totalPlayTimeMillis);
                            value5 = value;
                            ((DataOutputStream)value3).writeLong(((CharacterFileRecord)value5).createdAtMillis);
                            value5 = value;
                            ((DataOutputStream)value3).writeBoolean(((CharacterFileRecord)value5).loginRestrictionExempt);
                            value4 = value;
                            ((DataOutputStream)value3).writeBoolean(((CharacterFileRecord)value4).memberFlag);
                            value5 = value;
                            ((DataOutputStream)value3).writeInt(((CharacterFileRecord)value5).donatorPoints);
                            value5 = value;
                            ((DataOutputStream)value3).writeInt(((CharacterFileRecord)value5).x);
                            value5 = value;
                            ((DataOutputStream)value3).writeInt(((CharacterFileRecord)value5).y);
                            value5 = value;
                            ((DataOutputStream)value3).writeInt(((CharacterFileRecord)value5).plane);
                            value5 = value;
                            ((DataOutputStream)value3).writeInt(((CharacterFileRecord)value5).gender);
                            value5 = value;
                            ((DataOutputStream)value3).writeInt(((CharacterFileRecord)value5).npcKillCount);
                            value5 = value;
                            ((DataOutputStream)value3).writeInt(((CharacterFileRecord)value5).playerKillCount);
                            value5 = value;
                            ((DataOutputStream)value3).writeInt(((CharacterFileRecord)value5).deathCount);
                            value5 = value;
                            ((DataOutputStream)value3).writeInt(((CharacterFileRecord)value5).easyCluesCompleted);
                            value5 = value;
                            ((DataOutputStream)value3).writeInt(((CharacterFileRecord)value5).mediumCluesCompleted);
                            value5 = value;
                            ((DataOutputStream)value3).writeInt(((CharacterFileRecord)value5).hardCluesCompleted);
                            value5 = value;
                            ((DataOutputStream)value3).writeInt(((CharacterFileRecord)value5).soldItemsValue);
                            value5 = value;
                            ((DataOutputStream)value3).writeInt(((CharacterFileRecord)value5).boughtItemsValue);
                            value5 = value;
                            ((DataOutputStream)value3).writeInt(((CharacterFileRecord)value5).duelWins);
                            value5 = value;
                            ((DataOutputStream)value3).writeInt(((CharacterFileRecord)value5).duelLosses);
                            value5 = value;
                            ((DataOutputStream)value3).writeInt(((CharacterFileRecord)value5).legacyQuestPoints);
                            value5 = value;
                            ((DataOutputStream)value3).writeBoolean(((CharacterFileRecord)value5).autoRetaliate);
                            value5 = value;
                            ((DataOutputStream)value3).writeInt(((CharacterFileRecord)value5).fightMode);
                            value5 = value;
                            ((DataOutputStream)value3).writeInt(((CharacterFileRecord)value5).brightness);
                            value5 = value;
                            ((DataOutputStream)value3).writeInt(((CharacterFileRecord)value5).mouseButtons);
                            value5 = value;
                            ((DataOutputStream)value3).writeInt(((CharacterFileRecord)value5).publicChatEffects);
                            value5 = value;
                            ((DataOutputStream)value3).writeInt(((CharacterFileRecord)value5).splitPrivateChat);
                            value5 = value;
                            ((DataOutputStream)value3).writeInt(((CharacterFileRecord)value5).acceptAid);
                            value5 = value;
                            ((DataOutputStream)value3).writeInt(((CharacterFileRecord)value5).musicVolume);
                            value5 = value;
                            ((DataOutputStream)value3).writeInt(((CharacterFileRecord)value5).effectVolume);
                            value5 = value;
                            ((DataOutputStream)value3).writeDouble(((CharacterFileRecord)value5).specialEnergy);
                            value5 = value;
                            ((DataOutputStream)value3).writeBoolean(((CharacterFileRecord)value5).changingBankPin);
                            value5 = value;
                            ((DataOutputStream)value3).writeBoolean(((CharacterFileRecord)value5).deletingBankPin);
                            value5 = value;
                            ((DataOutputStream)value3).writeInt(((CharacterFileRecord)value5).pinAppendYear);
                            value5 = value;
                            ((DataOutputStream)value3).writeInt(((CharacterFileRecord)value5).pinAppendDate);
                            value5 = value;
                            ((DataOutputStream)value3).writeInt(((CharacterFileRecord)value5).bindingNecklaceCharge);
                            value5 = value;
                            ((DataOutputStream)value3).writeInt(((CharacterFileRecord)value5).ringOfForgingLife);
                            value5 = value;
                            ((DataOutputStream)value3).writeInt(((CharacterFileRecord)value5).ringOfRecoilLife);
                            value5 = value;
                            ((DataOutputStream)value3).writeInt(((CharacterFileRecord)value5).skullTimer);
                            value5 = value;
                            ((DataOutputStream)value3).writeShort(((CharacterFileRecord)value5).runEnergyRaw);
                            value5 = value;
                            ((DataOutputStream)value3).writeBoolean(((CharacterFileRecord)value5).running);
                            value2 = 0;
                            while (value2 < ((CharacterFileRecord)value).currentPin.length) {
                                ((DataOutputStream)value3).writeInt(((CharacterFileRecord)value).currentPin[value2]);
                                ++value2;
                            }
                            value2 = 0;
                            while (value2 < ((CharacterFileRecord)value).pendingPin.length) {
                                ((DataOutputStream)value3).writeInt(((CharacterFileRecord)value).pendingPin[value2]);
                                ++value2;
                            }
                            value2 = 0;
                            while (value2 < 4) {
                                ((DataOutputStream)value3).writeInt(((CharacterFileRecord)value).essencePouchAmounts[value2]);
                                ++value2;
                            }
                            value2 = 0;
                            while (value2 < ((CharacterFileRecord)value).appearanceParts.length) {
                                ((DataOutputStream)value3).writeInt(((CharacterFileRecord)value).appearanceParts[value2]);
                                ++value2;
                            }
                            value2 = 0;
                            while (value2 < ((CharacterFileRecord)value).appearanceColors.length) {
                                ((DataOutputStream)value3).writeInt(((CharacterFileRecord)value).appearanceColors[value2]);
                                ++value2;
                            }
                            value2 = 0;
                            if (!true) break saveCharacterFileRecordControlExit4;
                            value4 = value;
                            if (value2 >= ((CharacterFileRecord)value4).currentLevels.length) break saveCharacterFileRecordControlExit3;
                        }
                        do {
                            value4 = value;
                            ((DataOutputStream)value3).writeInt(((CharacterFileRecord)value4).currentLevels[value2]);
                            ++value2;
                            value4 = value;
                        } while (value2 < ((CharacterFileRecord)value4).currentLevels.length);
                    }
                    value2 = 0;
                    if (!true) break saveCharacterFileRecordControlExit2;
                    value4 = value;
                    if (value2 >= ((CharacterFileRecord)value4).skillExperience.length) break saveCharacterFileRecordControlExit1;
                }
                do {
                    value4 = value;
                    ((DataOutputStream)value3).writeInt((int)((CharacterFileRecord)value4).skillExperience[value2]);
                    ++value2;
                    value4 = value;
                } while (value2 < ((CharacterFileRecord)value4).skillExperience.length);
            }
            value2 = 0;
            while (value2 < 28) {
                value4 = ((CharacterFileRecord)value).inventoryItems[value2];
                if (value4 == null) {
                    ((DataOutputStream)value3).writeInt(65535);
                } else {
                    ((DataOutputStream)value3).writeInt(((ItemStack)value4).getId());
                    ((DataOutputStream)value3).writeInt(((ItemStack)value4).getAmount());
                    ((DataOutputStream)value3).writeInt(((ItemStack)value4).getMetadata());
                }
                ++value2;
            }
            value2 = 0;
            while (value2 < 14) {
                value4 = ((CharacterFileRecord)value).equipmentItems[value2];
                if (value4 == null) {
                    ((DataOutputStream)value3).writeInt(65535);
                } else {
                    ((DataOutputStream)value3).writeInt(((ItemStack)value4).getId());
                    ((DataOutputStream)value3).writeInt(((ItemStack)value4).getAmount());
                    ((DataOutputStream)value3).writeInt(((ItemStack)value4).getMetadata());
                }
                ++value2;
            }
            ((DataOutputStream)value3).writeByte(((CharacterFileRecord)value).bankTabs.size());
            value2 = 0;
            while (value2 < ((CharacterFileRecord)value).bankTabs.size()) {
                value4 = ((CharacterFileBankTab)((CharacterFileRecord)value).bankTabs.get(value2)).getItems();
                int value6 = ((ArrayList)value4).size();
                ((DataOutputStream)value3).writeShort(value6);
                int index = 0;
                while (index < value6) {
                    ItemStack itemStack = (ItemStack)((ArrayList)value4).get(index);
                    if (itemStack == null) {
                        ((DataOutputStream)value3).writeInt(65535);
                    } else {
                        ((DataOutputStream)value3).writeInt(itemStack.getId());
                        ((DataOutputStream)value3).writeInt(itemStack.getAmount());
                        ((DataOutputStream)value3).writeInt(itemStack.getMetadata());
                    }
                    ++index;
                }
                ++value2;
            }
            value2 = 0;
            while (value2 < ((CharacterFileRecord)value).friendsList.length) {
                ((DataOutputStream)value3).writeLong(((CharacterFileRecord)value).friendsList[value2]);
                ++value2;
            }
            value2 = 0;
            while (value2 < ((CharacterFileRecord)value).ignoreList.length) {
                ((DataOutputStream)value3).writeLong(((CharacterFileRecord)value).ignoreList[value2]);
                ++value2;
            }
            value2 = 0;
            while (value2 < ((CharacterFileRecord)value).queuedLoginItemIds.length) {
                ((DataOutputStream)value3).writeInt(((CharacterFileRecord)value).queuedLoginItemIds[value2]);
                ((DataOutputStream)value3).writeInt(((CharacterFileRecord)value).queuedLoginItemAmounts[value2]);
                ++value2;
            }
            Object value7 = value;
            ((DataOutputStream)value3).writeInt(((CharacterFileRecord)value7).abyssMageNpcId);
            value7 = value;
            ((DataOutputStream)value3).writeLong(((CharacterFileRecord)value7).muteExpires);
            value7 = value;
            ((DataOutputStream)value3).writeLong(((CharacterFileRecord)value7).banExpires);
            int index2 = 0;
            while (index2 < 6) {
                ((DataOutputStream)value3).writeBoolean(((CharacterFileRecord)value).barrowsKilledBrothers[index2]);
                ++index2;
            }
            Object value8 = value;
            ((DataOutputStream)value3).writeInt(((CharacterFileRecord)value8).barrowsKillCount);
            value8 = value;
            ((DataOutputStream)value3).writeInt(((CharacterFileRecord)value8).barrowsTargetBrotherIndex);
            value8 = value;
            ((DataOutputStream)value3).writeInt(((CharacterFileRecord)value8).poisonImmunityTicks);
            value8 = value;
            ((DataOutputStream)value3).writeInt(((CharacterFileRecord)value8).antifireTicks);
            value8 = value;
            ((DataOutputStream)value3).writeInt(((CharacterFileRecord)value8).teleblockTicks);
            value8 = value;
            ((DataOutputStream)value3).writeDouble(((CharacterFileRecord)value8).poisonDamage);
            int index3 = 0;
            while (index3 < ((CharacterFileRecord)value).allotmentGrowthStages.length) {
                ((DataOutputStream)value3).writeInt(((CharacterFileRecord)value).allotmentGrowthStages[index3]);
                ++index3;
            }
            index3 = 0;
            while (index3 < ((CharacterFileRecord)value).allotmentCropIds.length) {
                ((DataOutputStream)value3).writeInt(((CharacterFileRecord)value).allotmentCropIds[index3]);
                ++index3;
            }
            index3 = 0;
            while (index3 < ((CharacterFileRecord)value).allotmentHarvestAmounts.length) {
                ((DataOutputStream)value3).writeInt(((CharacterFileRecord)value).allotmentHarvestAmounts[index3]);
                ++index3;
            }
            index3 = 0;
            while (index3 < ((CharacterFileRecord)value).allotmentPatchStates.length) {
                ((DataOutputStream)value3).writeInt(((CharacterFileRecord)value).allotmentPatchStates[index3]);
                ++index3;
            }
            index3 = 0;
            while (index3 < ((CharacterFileRecord)value).allotmentLastUpdateTicks.length) {
                ((DataOutputStream)value3).writeLong(((CharacterFileRecord)value).allotmentLastUpdateTicks[index3]);
                ++index3;
            }
            index3 = 0;
            while (index3 < ((CharacterFileRecord)value).allotmentDiseaseChanceMultipliers.length) {
                ((DataOutputStream)value3).writeDouble(((CharacterFileRecord)value).allotmentDiseaseChanceMultipliers[index3]);
                ++index3;
            }
            index3 = 0;
            while (index3 < ((CharacterFileRecord)value).allotmentProtectionFlags.length) {
                ((DataOutputStream)value3).writeBoolean(((CharacterFileRecord)value).allotmentProtectionFlags[index3]);
                ++index3;
            }
            index3 = 0;
            while (index3 < ((CharacterFileRecord)value).bushGrowthStages.length) {
                ((DataOutputStream)value3).writeInt(((CharacterFileRecord)value).bushGrowthStages[index3]);
                ++index3;
            }
            index3 = 0;
            while (index3 < ((CharacterFileRecord)value).bushCropIds.length) {
                ((DataOutputStream)value3).writeInt(((CharacterFileRecord)value).bushCropIds[index3]);
                ++index3;
            }
            index3 = 0;
            while (index3 < ((CharacterFileRecord)value).bushPatchStates.length) {
                ((DataOutputStream)value3).writeInt(((CharacterFileRecord)value).bushPatchStates[index3]);
                ++index3;
            }
            index3 = 0;
            while (index3 < ((CharacterFileRecord)value).bushLastUpdateTicks.length) {
                ((DataOutputStream)value3).writeLong(((CharacterFileRecord)value).bushLastUpdateTicks[index3]);
                ++index3;
            }
            index3 = 0;
            while (index3 < ((CharacterFileRecord)value).bushDiseaseChanceMultipliers.length) {
                ((DataOutputStream)value3).writeDouble(((CharacterFileRecord)value).bushDiseaseChanceMultipliers[index3]);
                ++index3;
            }
            index3 = 0;
            while (index3 < ((CharacterFileRecord)value).bushSavedFlags.length) {
                ((DataOutputStream)value3).writeBoolean(((CharacterFileRecord)value).bushSavedFlags[index3]);
                ++index3;
            }
            index3 = 0;
            while (index3 < ((CharacterFileRecord)value).flowerGrowthStages.length) {
                ((DataOutputStream)value3).writeInt(((CharacterFileRecord)value).flowerGrowthStages[index3]);
                ++index3;
            }
            index3 = 0;
            while (index3 < ((CharacterFileRecord)value).flowerCropIds.length) {
                ((DataOutputStream)value3).writeInt(((CharacterFileRecord)value).flowerCropIds[index3]);
                ++index3;
            }
            index3 = 0;
            while (index3 < ((CharacterFileRecord)value).flowerPatchStates.length) {
                ((DataOutputStream)value3).writeInt(((CharacterFileRecord)value).flowerPatchStates[index3]);
                ++index3;
            }
            index3 = 0;
            while (index3 < ((CharacterFileRecord)value).flowerLastUpdateTicks.length) {
                ((DataOutputStream)value3).writeLong(((CharacterFileRecord)value).flowerLastUpdateTicks[index3]);
                ++index3;
            }
            index3 = 0;
            while (index3 < ((CharacterFileRecord)value).flowerDiseaseChanceMultipliers.length) {
                ((DataOutputStream)value3).writeDouble(((CharacterFileRecord)value).flowerDiseaseChanceMultipliers[index3]);
                ++index3;
            }
            index3 = 0;
            while (index3 < ((CharacterFileRecord)value).fruitTreeGrowthStages.length) {
                ((DataOutputStream)value3).writeInt(((CharacterFileRecord)value).fruitTreeGrowthStages[index3]);
                ++index3;
            }
            index3 = 0;
            while (index3 < ((CharacterFileRecord)value).fruitTreeIds.length) {
                ((DataOutputStream)value3).writeInt(((CharacterFileRecord)value).fruitTreeIds[index3]);
                ++index3;
            }
            index3 = 0;
            while (index3 < ((CharacterFileRecord)value).fruitTreePatchStates.length) {
                ((DataOutputStream)value3).writeInt(((CharacterFileRecord)value).fruitTreePatchStates[index3]);
                ++index3;
            }
            index3 = 0;
            while (index3 < ((CharacterFileRecord)value).fruitTreeLastUpdateTicks.length) {
                ((DataOutputStream)value3).writeLong(((CharacterFileRecord)value).fruitTreeLastUpdateTicks[index3]);
                ++index3;
            }
            index3 = 0;
            while (index3 < ((CharacterFileRecord)value).fruitTreeDiseaseChanceMultipliers.length) {
                ((DataOutputStream)value3).writeDouble(((CharacterFileRecord)value).fruitTreeDiseaseChanceMultipliers[index3]);
                ++index3;
            }
            index3 = 0;
            while (index3 < ((CharacterFileRecord)value).fruitTreeSavedFlags.length) {
                ((DataOutputStream)value3).writeBoolean(((CharacterFileRecord)value).fruitTreeSavedFlags[index3]);
                ++index3;
            }
            index3 = 0;
            while (index3 < ((CharacterFileRecord)value).herbGrowthStages.length) {
                ((DataOutputStream)value3).writeInt(((CharacterFileRecord)value).herbGrowthStages[index3]);
                ++index3;
            }
            index3 = 0;
            while (index3 < ((CharacterFileRecord)value).herbCropIds.length) {
                ((DataOutputStream)value3).writeInt(((CharacterFileRecord)value).herbCropIds[index3]);
                ++index3;
            }
            index3 = 0;
            while (index3 < ((CharacterFileRecord)value).herbHarvestAmounts.length) {
                ((DataOutputStream)value3).writeInt(((CharacterFileRecord)value).herbHarvestAmounts[index3]);
                ++index3;
            }
            index3 = 0;
            while (index3 < ((CharacterFileRecord)value).herbPatchStates.length) {
                ((DataOutputStream)value3).writeInt(((CharacterFileRecord)value).herbPatchStates[index3]);
                ++index3;
            }
            index3 = 0;
            while (index3 < ((CharacterFileRecord)value).herbLastUpdateTicks.length) {
                ((DataOutputStream)value3).writeLong(((CharacterFileRecord)value).herbLastUpdateTicks[index3]);
                ++index3;
            }
            index3 = 0;
            while (index3 < ((CharacterFileRecord)value).herbDiseaseChanceMultipliers.length) {
                ((DataOutputStream)value3).writeDouble(((CharacterFileRecord)value).herbDiseaseChanceMultipliers[index3]);
                ++index3;
            }
            index3 = 0;
            while (index3 < ((CharacterFileRecord)value).hopsGrowthStages.length) {
                ((DataOutputStream)value3).writeInt(((CharacterFileRecord)value).hopsGrowthStages[index3]);
                ++index3;
            }
            index3 = 0;
            while (index3 < ((CharacterFileRecord)value).hopsCropIds.length) {
                ((DataOutputStream)value3).writeInt(((CharacterFileRecord)value).hopsCropIds[index3]);
                ++index3;
            }
            index3 = 0;
            while (index3 < ((CharacterFileRecord)value).hopsHarvestAmounts.length) {
                ((DataOutputStream)value3).writeInt(((CharacterFileRecord)value).hopsHarvestAmounts[index3]);
                ++index3;
            }
            index3 = 0;
            while (index3 < ((CharacterFileRecord)value).hopsPatchStates.length) {
                ((DataOutputStream)value3).writeInt(((CharacterFileRecord)value).hopsPatchStates[index3]);
                ++index3;
            }
            index3 = 0;
            while (index3 < ((CharacterFileRecord)value).hopsLastUpdateTicks.length) {
                ((DataOutputStream)value3).writeLong(((CharacterFileRecord)value).hopsLastUpdateTicks[index3]);
                ++index3;
            }
            index3 = 0;
            while (index3 < ((CharacterFileRecord)value).hopsDiseaseChanceMultipliers.length) {
                ((DataOutputStream)value3).writeDouble(((CharacterFileRecord)value).hopsDiseaseChanceMultipliers[index3]);
                ++index3;
            }
            index3 = 0;
            while (index3 < ((CharacterFileRecord)value).hopsProtectionFlags.length) {
                ((DataOutputStream)value3).writeBoolean(((CharacterFileRecord)value).hopsProtectionFlags[index3]);
                ++index3;
            }
            index3 = 0;
            while (index3 < ((CharacterFileRecord)value).specialTreeGrowthStages.length) {
                ((DataOutputStream)value3).writeInt(((CharacterFileRecord)value).specialTreeGrowthStages[index3]);
                ++index3;
            }
            index3 = 0;
            while (index3 < ((CharacterFileRecord)value).specialTreeIds.length) {
                ((DataOutputStream)value3).writeInt(((CharacterFileRecord)value).specialTreeIds[index3]);
                ++index3;
            }
            index3 = 0;
            while (index3 < ((CharacterFileRecord)value).specialTreePatchStates.length) {
                ((DataOutputStream)value3).writeInt(((CharacterFileRecord)value).specialTreePatchStates[index3]);
                ++index3;
            }
            index3 = 0;
            while (index3 < ((CharacterFileRecord)value).specialTreeLastUpdateTicks.length) {
                ((DataOutputStream)value3).writeLong(((CharacterFileRecord)value).specialTreeLastUpdateTicks[index3]);
                ++index3;
            }
            index3 = 0;
            while (index3 < ((CharacterFileRecord)value).specialTreeDiseaseChanceMultipliers.length) {
                ((DataOutputStream)value3).writeDouble(((CharacterFileRecord)value).specialTreeDiseaseChanceMultipliers[index3]);
                ++index3;
            }
            index3 = 0;
            while (index3 < ((CharacterFileRecord)value).specialCropGrowthStages.length) {
                ((DataOutputStream)value3).writeInt(((CharacterFileRecord)value).specialCropGrowthStages[index3]);
                ++index3;
            }
            index3 = 0;
            while (index3 < ((CharacterFileRecord)value).specialCropIds.length) {
                ((DataOutputStream)value3).writeInt(((CharacterFileRecord)value).specialCropIds[index3]);
                ++index3;
            }
            index3 = 0;
            while (index3 < ((CharacterFileRecord)value).specialCropPatchStates.length) {
                ((DataOutputStream)value3).writeInt(((CharacterFileRecord)value).specialCropPatchStates[index3]);
                ++index3;
            }
            index3 = 0;
            while (index3 < ((CharacterFileRecord)value).specialCropLastUpdateTicks.length) {
                ((DataOutputStream)value3).writeLong(((CharacterFileRecord)value).specialCropLastUpdateTicks[index3]);
                ++index3;
            }
            index3 = 0;
            while (index3 < ((CharacterFileRecord)value).specialCropDiseaseChanceMultipliers.length) {
                ((DataOutputStream)value3).writeDouble(((CharacterFileRecord)value).specialCropDiseaseChanceMultipliers[index3]);
                ++index3;
            }
            index3 = 0;
            while (index3 < ((CharacterFileRecord)value).treeGrowthStages.length) {
                ((DataOutputStream)value3).writeInt(((CharacterFileRecord)value).treeGrowthStages[index3]);
                ++index3;
            }
            index3 = 0;
            while (index3 < ((CharacterFileRecord)value).treeIds.length) {
                ((DataOutputStream)value3).writeInt(((CharacterFileRecord)value).treeIds[index3]);
                ++index3;
            }
            index3 = 0;
            while (index3 < ((CharacterFileRecord)value).treePatchData.length) {
                ((DataOutputStream)value3).writeInt(((CharacterFileRecord)value).treePatchData[index3]);
                ++index3;
            }
            index3 = 0;
            while (index3 < ((CharacterFileRecord)value).treePatchStates.length) {
                ((DataOutputStream)value3).writeInt(((CharacterFileRecord)value).treePatchStates[index3]);
                ++index3;
            }
            index3 = 0;
            while (index3 < ((CharacterFileRecord)value).treeLastUpdateTicks.length) {
                ((DataOutputStream)value3).writeLong(((CharacterFileRecord)value).treeLastUpdateTicks[index3]);
                ++index3;
            }
            index3 = 0;
            while (index3 < ((CharacterFileRecord)value).treeDiseaseChanceMultipliers.length) {
                ((DataOutputStream)value3).writeDouble(((CharacterFileRecord)value).treeDiseaseChanceMultipliers[index3]);
                ++index3;
            }
            index3 = 0;
            while (index3 < ((CharacterFileRecord)value).treeSavedFlags.length) {
                ((DataOutputStream)value3).writeBoolean(((CharacterFileRecord)value).treeSavedFlags[index3]);
                ++index3;
            }
            index3 = 0;
            while (index3 < ((CharacterFileRecord)value).compostBinStates.length) {
                ((DataOutputStream)value3).writeInt(((CharacterFileRecord)value).compostBinStates[index3]);
                ++index3;
            }
            index3 = 0;
            while (index3 < ((CharacterFileRecord)value).compostBinLastUpdateTicks.length) {
                ((DataOutputStream)value3).writeLong(((CharacterFileRecord)value).compostBinLastUpdateTicks[index3]);
                ++index3;
            }
            index3 = 0;
            while (index3 < ((CharacterFileRecord)value).compostBinItemIds.length) {
                ((DataOutputStream)value3).writeInt(((CharacterFileRecord)value).compostBinItemIds[index3]);
                ++index3;
            }
            index3 = 0;
            while (index3 < ((CharacterFileRecord)value).farmingToolStoreAmounts.length) {
                ((DataOutputStream)value3).writeInt(((CharacterFileRecord)value).farmingToolStoreAmounts[index3]);
                ++index3;
            }
            Object value9 = value;
            ((DataOutputStream)value3).writeInt(((CharacterFileRecord)value9).slayerMasterId);
            value9 = value;
            ((DataOutputStream)value3).writeUTF(((CharacterFileRecord)value9).slayerTaskName);
            value9 = value;
            ((DataOutputStream)value3).writeInt(((CharacterFileRecord)value9).slayerTaskAmount);
            value9 = value;
            ((DataOutputStream)value3).writeBoolean(((CharacterFileRecord)value9).usingAncients);
            value9 = value;
            ((DataOutputStream)value3).writeBoolean(((CharacterFileRecord)value9).brimhavenOpen);
            value9 = value;
            ((DataOutputStream)value3).writeBoolean(((CharacterFileRecord)value9).killedClueAttacker);
            int index4 = 0;
            while (index4 < musicUnlockConfigIds.length) {
                ((DataOutputStream)value3).writeInt(((CharacterFileRecord)value).configStates[musicUnlockConfigIds[index4]]);
                ++index4;
            }
            index4 = 0;
            while (index4 < QuestDefinition.questStateCapacity) {
                ((DataOutputStream)value3).writeInt(((CharacterFileRecord)value).questProgress[index4]);
                ++index4;
            }
            Object value10 = value;
            ((DataOutputStream)value3).writeByte(((CharacterFileRecord)value10).gangAffiliation);
            value10 = value;
            ((DataOutputStream)value3).writeByte(((CharacterFileRecord)value10).piratesTreasureBananaCrateCount);
            value10 = value;
            ((DataOutputStream)value3).writeBoolean(((CharacterFileRecord)value10).treasureTrailNavigationTaught);
            value10 = value;
            ((DataOutputStream)value3).writeByte(((CharacterFileRecord)value10).coalTruckAmount);
            value10 = value;
            ((DataOutputStream)value3).writeByte(((CharacterFileRecord)value10).treasureTrailStepCount);
            value10 = value;
            ((DataOutputStream)value3).writeBoolean(((CharacterFileRecord)value10).cluePuzzleSolved);
            ((DataOutputStream)value3).writeByte(((CharacterFileRecord)value).skeletonSkinUnlocked);
            ((DataOutputStream)value3).writeInt(((CharacterFileRecord)value).petUnlockFlags);
            ((DataOutputStream)value3).writeBoolean(((CharacterFileRecord)value).barrowsDoorPuzzleSolved);
            ((DataOutputStream)value3).writeBoolean(((CharacterFileRecord)value).barrowsChestOpened);
            ((DataOutputStream)value3).writeInt(((CharacterFileRecord)value).configStates[452]);
            value10 = value;
            ((DataOutputStream)value3).writeByte(((CharacterFileRecord)value10).flourMillHopperGrainCount);
            ((DataOutputStream)value3).writeInt(((CharacterFileRecord)value).configStates[FlourMillHandler.flourBinConfigId]);
            ((DataOutputStream)value3).writeInt(((CharacterFileRecord)value).questRandomSeed);
            int index5 = 0;
            while (index5 < QuestDefinition.questStateCapacity) {
                ((DataOutputStream)value3).writeInt(((CharacterFileRecord)value).questBitFlags[index5]);
                ++index5;
            }
            index5 = 0;
            while (index5 < 100) {
                ((DataOutputStream)value3).writeInt(((CharacterFileRecord)value).questHookStates[index5]);
                ++index5;
            }
            ((DataOutputStream)value3).writeByte(((CharacterFileRecord)value).publicChatMode);
            ((DataOutputStream)value3).writeByte(((CharacterFileRecord)value).privateChatMode);
            ((DataOutputStream)value3).writeByte(((CharacterFileRecord)value).tradeMode);
            ((DataOutputStream)value3).writeInt(((CharacterFileRecord)value).reservedSaveInt1);
            ((DataOutputStream)value3).writeLong(((CharacterFileRecord)value).reservedSaveLong1);
            ((DataOutputStream)value3).writeInt(((CharacterFileRecord)value).reservedSaveInt2);
            ((DataOutputStream)value3).writeInt(((CharacterFileRecord)value).reservedSaveInt3);
            Object value11 = value;
            ((DataOutputStream)value3).writeUTF(((CharacterFileRecord)value11).reservedVersion11String);
            ((DataOutputStream)value3).writeInt(((CharacterFileRecord)value).familyCrestGauntletItemId);
            ((DataOutputStream)value3).writeInt(((CharacterFileRecord)value).mageArenaFlamesOfZamorakCastsRemaining);
            ((DataOutputStream)value3).writeInt(((CharacterFileRecord)value).mageArenaSaradominStrikeCastsRemaining);
            ((DataOutputStream)value3).writeInt(((CharacterFileRecord)value).mageArenaClawsOfGuthixCastsRemaining);
            ((DataOutputStream)value3).writeByte(((CharacterFileRecord)value).mageArenaProgressStage);
            int index6 = 0;
            while (index6 < 6) {
                ((DataOutputStream)value3).writeBoolean(((CharacterFileRecord)value).grandExchangeSellOfferFlags[index6]);
                ++index6;
            }
            index6 = 0;
            while (index6 < 6) {
                ((DataOutputStream)value3).writeInt(((CharacterFileRecord)value).grandExchangeItemIds[index6]);
                ++index6;
            }
            index6 = 0;
            while (index6 < 6) {
                ((DataOutputStream)value3).writeInt(((CharacterFileRecord)value).grandExchangeQuantities[index6]);
                ++index6;
            }
            index6 = 0;
            while (index6 < 6) {
                ((DataOutputStream)value3).writeInt(((CharacterFileRecord)value).grandExchangeUnitPrices[index6]);
                ++index6;
            }
            index6 = 0;
            while (index6 < 6) {
                ((DataOutputStream)value3).writeBoolean(((CharacterFileRecord)value).grandExchangeCancelledFlags[index6]);
                ++index6;
            }
            index6 = 0;
            while (index6 < 6) {
                ((DataOutputStream)value3).writeInt(((CharacterFileRecord)value).grandExchangeCompletedQuantities[index6]);
                ++index6;
            }
            index6 = 0;
            while (index6 < 6) {
                ((DataOutputStream)value3).writeInt(((CharacterFileRecord)value).grandExchangeTotalPrices[index6]);
                ++index6;
            }
            index6 = 0;
            while (index6 < 6) {
                ((DataOutputStream)value3).writeInt(((CharacterFileRecord)value).grandExchangePrimaryCollectAmounts[index6]);
                ++index6;
            }
            index6 = 0;
            while (index6 < 6) {
                ((DataOutputStream)value3).writeInt(((CharacterFileRecord)value).grandExchangeSecondaryCollectAmounts[index6]);
                ++index6;
            }
            index6 = 0;
            while (index6 < 6) {
                ((DataOutputStream)value3).writeBoolean(((CharacterFileRecord)value).grandExchangeFinishMessagePending[index6]);
                ++index6;
            }
            ((DataOutputStream)value3).writeInt(((CharacterFileRecord)value).telekineticPizazzPoints);
            ((DataOutputStream)value3).writeInt(((CharacterFileRecord)value).enchantmentPizazzPoints);
            ((DataOutputStream)value3).writeInt(((CharacterFileRecord)value).alchemistPizazzPoints);
            ((DataOutputStream)value3).writeInt(((CharacterFileRecord)value).graveyardPizazzPoints);
            ((DataOutputStream)value3).writeBoolean(((CharacterFileRecord)value).bonesToPeachesUnlocked);
            ((DataOutputStream)value3).writeByte(((CharacterFileRecord)value).telekineticMazeIndex);
            ((DataOutputStream)value3).writeBoolean(((CharacterFileRecord)value).telekineticMazeSolved);
            ((DataOutputStream)value3).writeByte(((CharacterFileRecord)value).telekineticConsecutiveMazesSolved);
            ((DataOutputStream)value3).writeInt(((CharacterFileRecord)value).barrowsRewardPotential);
            ((DataOutputStream)value3).writeByte(((CharacterFileRecord)value).gameMode);
            ((DataOutputStream)value3).writeInt(((CharacterFileRecord)value).barrowsRunsCompleted);
            ((DataOutputStream)value3).writeLong(((CharacterFileRecord)value).godWarsLastAltarBlessingMillis);
            ((DataOutputStream)value3).writeInt(((CharacterFileRecord)value).configStates[LEGACY_GOD_WARS_ROPE_CONFIG_ID]);
            index6 = 0;
            while (index6 < ((CharacterFileRecord)value).godWarsKillCounts.length) {
                ((DataOutputStream)value3).writeInt(((CharacterFileRecord)value).godWarsKillCounts[index6]);
                ++index6;
            }
            ((DataOutputStream)value3).writeByte(((CharacterFileRecord)value).craftingThreadUseCount);
            ((DataOutputStream)value3).writeLong(((CharacterFileRecord)value).membershipExpiresMillis);
            ((DataOutputStream)value3).writeByte(((CharacterFileRecord)value).reservedSaveByte);
            ((DataOutputStream)value3).writeInt(((CharacterFileRecord)value).configStates[33]);
            ((DataOutputStream)value3).writeShort(((CharacterFileRecord)value).savedCacheVersion);
            ((DataOutputStream)value3).writeInt(((CharacterFileRecord)value).godBookPageFlags);
            ((DataOutputStream)value3).writeBoolean(((CharacterFileRecord)value).swampCaveRopeAttached);
            ((DataOutputStream)value3).writeBoolean(((CharacterFileRecord)value).lampOilStillFilled);
            ((DataOutputStream)value3).writeInt(((CharacterFileRecord)value).enterTheAbyssMiniquestState);
            ((DataOutputStream)value3).writeInt(((CharacterFileRecord)value).configStates[872]);
            ((DataOutputStream)value3).writeBoolean(((CharacterFileRecord)value).botEnabled);
            if (((CharacterFileRecord)value).botEnabled) {
                ((DataOutputStream)value3).writeByte(((CharacterFileRecord)value).botMode);
                ((DataOutputStream)value3).writeByte(((CharacterFileRecord)value).currentBotTaskTypeId);
                ((DataOutputStream)value3).writeInt(((CharacterFileRecord)value).currentBotTaskIndex);
                ((DataOutputStream)value3).writeByte(((CharacterFileRecord)value).deferredBotTaskTypeId);
                ((DataOutputStream)value3).writeInt(((CharacterFileRecord)value).deferredBotTaskIndex);
                ((DataOutputStream)value3).writeUTF(((CharacterFileRecord)value).botTaskState);
                index6 = 0;
                if (((CharacterFileRecord)value).botTaskRequiredItems != null) {
                    index6 = ((CharacterFileRecord)value).botTaskRequiredItems.length;
                }
                ((DataOutputStream)value3).writeByte(index6);
                int index7 = 0;
                while (index7 < index6) {
                    ((DataOutputStream)value3).writeInt(((CharacterFileRecord)value).botTaskRequiredItems[index7].getId());
                    ((DataOutputStream)value3).writeInt(((CharacterFileRecord)value).botTaskRequiredItems[index7].getAmount());
                    ++index7;
                }
                ((DataOutputStream)value3).writeInt(((CharacterFileRecord)value).botFoodItemId);
                ((DataOutputStream)value3).writeByte(((CharacterFileRecord)value).botPathSegmentIndex);
                ((DataOutputStream)value3).writeByte(((CharacterFileRecord)value).botPathWaypointIndex);
                ((DataOutputStream)value3).writeBoolean(((CharacterFileRecord)value).savedWorldRouteReversed);
                ((DataOutputStream)value3).writeLong(((CharacterFileRecord)value).botTaskSavedElapsedMillis);
                ((DataOutputStream)value3).writeByte(((CharacterFileRecord)value).botTaskDurationMinutes);
                ((DataOutputStream)value3).writeByte(((CharacterFileRecord)value).savedWorldRouteIndex);
                ((DataOutputStream)value3).writeByte(((CharacterFileRecord)value).tradeAdvertMode);
                ((DataOutputStream)value3).writeInt(((CharacterFileRecord)value).botAdvertItemId);
                ((DataOutputStream)value3).writeInt(((CharacterFileRecord)value).tradeAdvertQuantityRemaining);
                ((DataOutputStream)value3).writeInt(((CharacterFileRecord)value).tradeAdvertUnitPrice);
                ((DataOutputStream)value3).writeBoolean(((CharacterFileRecord)value).tradeAdvertScam);
                ((DataOutputStream)value3).writeBoolean(((CharacterFileRecord)value).tradeAdvertVariableQuantity);
                ((DataOutputStream)value3).writeInt(((CharacterFileRecord)value).tradeAdvertLastOfferAmount);
                ((DataOutputStream)value3).writeByte(((CharacterFileRecord)value).botShopBuyMode);
                ((DataOutputStream)value3).writeInt(((CharacterFileRecord)value).botTaskItemId);
                ((DataOutputStream)value3).writeInt(((CharacterFileRecord)value).botShopItemAmount);
                ((DataOutputStream)value3).writeByte(((CharacterFileRecord)value).botShopSellItemIds.size());
                index7 = 0;
                while (index7 < ((CharacterFileRecord)value).botShopSellItemIds.size()) {
                    ((DataOutputStream)value3).writeInt((Integer)((CharacterFileRecord)value).botShopSellItemIds.get(index7));
                    ++index7;
                }
                ((DataOutputStream)value3).writeByte(((CharacterFileRecord)value).botCombatLoadoutItemIds.size());
                index7 = 0;
                while (index7 < ((CharacterFileRecord)value).botCombatLoadoutItemIds.size()) {
                    ((DataOutputStream)value3).writeInt((Integer)((CharacterFileRecord)value).botCombatLoadoutItemIds.get(index7));
                    ++index7;
                }
                ((DataOutputStream)value3).writeByte(((CharacterFileRecord)value).botCombatStyle);
                ((DataOutputStream)value3).writeByte(((CharacterFileRecord)value).botSkillTargetSkillId);
                ((DataOutputStream)value3).writeByte(((CharacterFileRecord)value).botSkillTargetLevel);
                ((DataOutputStream)value3).writeByte(((CharacterFileRecord)value).botReservedGoalByte1);
                ((DataOutputStream)value3).writeByte(((CharacterFileRecord)value).botReservedGoalByte2);
                ((DataOutputStream)value3).writeByte(((CharacterFileRecord)value).botReservedGoalByte3);
                ((DataOutputStream)value3).writeByte(((CharacterFileRecord)value).botReservedGoalByte4);
                ((DataOutputStream)value3).writeInt(((CharacterFileRecord)value).botCompletionItemId);
                ((DataOutputStream)value3).writeInt(((CharacterFileRecord)value).botCompletionItemAmount);
                ((DataOutputStream)value3).writeInt(((CharacterFileRecord)value).botSecondaryCompletionItemId);
                ((DataOutputStream)value3).writeInt(((CharacterFileRecord)value).botReservedGoalInt2);
                ((DataOutputStream)value3).writeInt(((CharacterFileRecord)value).botReservedGoalInt3);
                ((DataOutputStream)value3).writeInt(((CharacterFileRecord)value).botReservedGoalInt4);
                ((DataOutputStream)value3).writeBoolean(((CharacterFileRecord)value).botTaskReturnToBankRequested);
                ((DataOutputStream)value3).writeBoolean(false);
                ((DataOutputStream)value3).writeBoolean(false);
                ((DataOutputStream)value3).writeBoolean(false);
                ((DataOutputStream)value3).writeBoolean(false);
                ((DataOutputStream)value3).writeBoolean(false);
                ((DataOutputStream)value3).writeByte(((CharacterFileRecord)value).botElementalSpellIndex);
                ((DataOutputStream)value3).writeByte(0);
                ((DataOutputStream)value3).writeByte(0);
                ((DataOutputStream)value3).writeByte(0);
                ((DataOutputStream)value3).writeByte(0);
                ((DataOutputStream)value3).writeInt(0);
                ((DataOutputStream)value3).writeInt(0);
                ((DataOutputStream)value3).writeInt(0);
                ((DataOutputStream)value3).writeInt(0);
                ((DataOutputStream)value3).writeInt(0);
                ((DataOutputStream)value3).writeUTF("");
                ((DataOutputStream)value3).writeUTF("");
                ((DataOutputStream)value3).writeUTF("");
                ((DataOutputStream)value3).writeUTF("");
                ((DataOutputStream)value3).writeUTF("");
            }
            ((DataOutputStream)value3).writeLong(((CharacterFileRecord)value).homeTeleportAvailableAtMillis);
            ((DataOutputStream)value3).writeBoolean(((CharacterFileRecord)value).automaticMusicEnabled);
            ((DataOutputStream)value3).writeInt(((CharacterFileRecord)value).configStates[LATE_MUSIC_UNLOCK_CONFIG_ID]);
            ((DataOutputStream)value3).flush();
            ((FilterOutputStream)value3).close();
            return;
        }
        catch (Exception exception) {
            value = exception;
            exception.printStackTrace();
            return;
        }
    }


    /**
     * Automatic, idempotent startup migration. Original .dat files become
     * .dat.bak only after verified JSON is fully written. Invalid legacy files
     * are never deleted; JSON is always authoritative once present.
     */
    public static void migrateLegacyCharacters() {
        if (ServerSettings.sqlitePlayerSaveEnabled) return;
        File directory = new File("./data/characters/");
        File[] characters = directory.listFiles();
        if (characters == null) return;
        int converted = 0;
        int failed = 0;
        for (File legacy : characters) {
            if (!legacy.isFile() || !legacy.getName().endsWith(".dat")) continue;
            String username = legacy.getName().substring(0, legacy.getName().length() - 4);
            File json = new File(directory, username + ".json");
            if (json.exists()) continue;
            try {
                if (!validateCharacterFile(directory.getPath() + File.separator, username))
                    throw new IOException("Invalid legacy .dat");
                CharacterFileRecord record = readCharacterFileRecord(
                        directory.getPath() + File.separator, username, true);
                if (record == null || !username.equalsIgnoreCase(record.username))
                    throw new IOException("Legacy username mismatch or unreadable character");
                CharacterJsonCodec.save(json, record);
                Files.move(legacy.toPath(),
                        new File(directory, username + ".dat.bak").toPath(),
                        StandardCopyOption.REPLACE_EXISTING);
                converted++;
            } catch (Exception exception) {
                failed++;
                System.err.println("[characters] Could not migrate " + username
                        + "; original data kept: " + exception);
            }
        }
        if (converted > 0 || failed > 0) {
            System.out.println("[characters] .dat to JSON: " + converted
                    + " migrated, " + failed + " need attention.");
        }
    }

    public static void loadPlayer(Player player) {
        if (ServerSettings.sqlitePlayerSaveEnabled) {
            Object socketChannel = player.getSocketChannel().socket().getInetAddress().getHostAddress();
            if ((socketChannel = loginIpReservations.putIfAbsent(socketChannel, new LoginIpReservation())) != null) {
                // empty if block
            }
            socketChannel = new PlayerUidLookupQuery("SELECT uid FROM `prs06_users` WHERE username = ?", player);
            DatabaseService.getInstance().submit((DatabaseQuery)socketChannel, new PlayerLoginLoadCallback(player));
            return;
        }
        CharacterFileManager.loadPlayerFromFile("./data/characters/", player);
    }
    public static void saveAllPlayers() {
        Player[] playerArray = World.getPlayers();
        synchronized (playerArray) {
            Player[] players = World.getPlayers();
            int length = players.length;
            int index = 0;
            while (index < length) {
                Player player = players[index];
                if (player != null && player.getIndex() != -1) {
                    try {
                        CharacterFileManager.savePlayer(player);
                    }
                    catch (Exception exception) {
                        exception.printStackTrace();
                    }
                }
                ++index;
            }
            return;
        }
    }

    private static void restorePlayerFromBackup(Player player) {
        File[] fileArray = new File("./data/backups/").listFiles();
        if (fileArray == null) {
            return;
        }
        Arrays.sort(fileArray, new BackupRestoreTimestampComparator());
        int length = fileArray.length;
        int index = 0;
        while (index < length) {
            File backupDirectory = fileArray[index];
            File file = new File(String.valueOf(backupDirectory.getPath()) + "/characters/" + player.getUsername() + ".dat");
            File json = new File(String.valueOf(backupDirectory.getPath()) + "/characters/" + player.getUsername() + ".json");
            if ((file.exists() || json.exists()) && CharacterFileManager.validateCharacterFile(String.valueOf(backupDirectory.getPath()) + "/characters/", player.getUsername())) {
                try {
                    CharacterFileManager.loadPlayerFromFile(String.valueOf(backupDirectory.getPath()) + "/characters/", player);
                    System.out.println("Restoring from backup: " + player);
                    return;
                }
                catch (Exception exception) {
                    exception.printStackTrace();
                }
            }
            ++index;
        }
    }

    public static String stripFileExtension(String text2) {
        int value = text2.lastIndexOf(".");
        if (value > 0) {
            text2 = text2.substring(0, value);
        }
        return text2;
    }

    private static void repairBackupCharacterFiles() {
        File[] fileArray = new File("./data/backups/").listFiles();
        ArrayList<File> arrayList = new ArrayList<File>();
        if (fileArray == null) {
            return;
        }
        Arrays.sort(fileArray, new BackupRepairTimestampComparator());
        int length = fileArray.length;
        int index = 0;
        while (index < length) {
            File characterDirectory = new File(String.valueOf(fileArray[index].getPath()) + "/characters");
            File[] fileArray2 = characterDirectory.listFiles();
            if (fileArray2 == null) {
                ++index;
                continue;
            }
            int length2 = fileArray2.length;
            int index2 = 0;
            while (index2 < length2) {
                File backupFile = fileArray2[index2];
                if (!backupFile.isFile() || !(backupFile.getName().endsWith(".dat")
                        || backupFile.getName().endsWith(".json"))) {
                    ++index2;
                    continue;
                }
                String name = CharacterFileManager.stripFileExtension(backupFile.getName());
                if (!CharacterFileManager.validateCharacterFile(String.valueOf(characterDirectory.getPath()) + "/", name)) {
                    System.out.println("corrupted file found: " + backupFile.getPath());
                    arrayList.add(backupFile);
                } else if (!arrayList.isEmpty()) {
                    File file = null;
                    for (File file2 : arrayList) {
                        String name2 = CharacterFileManager.stripFileExtension(file2.getName());
                        if (!name2.equals(name)) continue;
                        System.out.println("valid file found: " + backupFile.getPath());
                        file = file2;
                        try {
                            file2.delete();
                            CharacterFileManager.copyFile(backupFile, file2);
                        }
                        catch (IOException iOException) {
                            iOException.printStackTrace();
                        }
                    }
                    if (file != null) {
                        arrayList.remove(file);
                    }
                }
                ++index2;
            }
            if (arrayList.isEmpty()) {
                return;
            }
            ++index;
        }
    }

    public static void createTimestampedBackup() {
        File file;
        File file2;
        long value;
        long value2 = value = System.currentTimeMillis();
        Object value3 = new DateTime(value2);
        value3 = String.valueOf(((AbstractDateTime)value3).getHourOfDay()) + "-" + ((AbstractDateTime)value3).getMinuteOfHour();
        value3 = String.valueOf(GameplayHelper.formatDateDayMonthYear(value)) + "-" + (String)value3;
        File[] fileArray = new File("./data/characters/").listFiles();
        int length = fileArray == null ? 0 : fileArray.length;
        int index = 0;
        while (index < length) {
            File sourceFile = fileArray[index];
            if (sourceFile.isFile() && (sourceFile.getName().endsWith(".json")
                    || sourceFile.getName().endsWith(".dat"))) {
                file2 = new File("./data/backups/" + (String)value3 + "/characters");
                file = new File("./data/backups/" + (String)value3 + "/characters/" + sourceFile.getName());
                try {
                    file2.mkdirs();
                    CharacterFileManager.copyFile(sourceFile, file);
                }
                catch (IOException iOException) {
                    iOException.printStackTrace();
                }
            }
            ++index;
        }
        CharacterFileManager.repairBackupCharacterFiles();
        fileArray = new File("./data/logs/").listFiles();
        length = fileArray == null ? 0 : fileArray.length;
        index = 0;
        while (index < length) {
            File sourceFile = fileArray[index];
            if (sourceFile.isFile()) {
                file2 = new File("./data/backups/" + (String)value3 + "/logs");
                file = new File("./data/backups/" + (String)value3 + "/logs/" + sourceFile.getName());
                try {
                    file2.mkdirs();
                    CharacterFileManager.copyFile(sourceFile, file);
                    sourceFile.delete();
                }
                catch (IOException iOException) {
                    iOException.printStackTrace();
                }
            }
            ++index;
        }
    }

    private static void copyFile(File file, File file2) throws IOException {
        Files.copy(file.toPath(), file2.toPath(), new CopyOption[0]);
    }

    /*
     * Enabled aggressive exception aggregation
     */
    private static boolean validateCharacterFile(String path, String username) {
        File json = new File(path + username + ".json");
        if (json.isFile()) {
            try {
                CharacterJsonCodec.read(json, username);
                return true;
            } catch (IOException exception) {
                System.err.println("Invalid JSON save: " + json + " - " + exception.getMessage());
                return false;
            }
        }
        Object value = path;
        Object value2 = username;
        if (!((File)(value = new File(String.valueOf(value) + (String)value2 + ".dat"))).exists()) {
            return false;
        }
        try {
            validateCharacterFileControlExit1: {
                value = new FileInputStream((File)value);
                value2 = new DataInputStream((InputStream)value);
                try {
                    int value3;
                    int value4;
                    int value5;
                    int value6;
                    short s = ((DataInputStream)value2).readShort();
                    if (s < 24) {
                        ((FilterInputStream)value2).close();
                        return false;
                    }
                    ((DataInputStream)value2).readUTF();
                    ((DataInputStream)value2).readUTF();
                    ((DataInputStream)value2).readUTF();
                    ((DataInputStream)value2).readInt();
                    ((DataInputStream)value2).readUTF();
                    ((DataInputStream)value2).readUTF();
                    ((DataInputStream)value2).readUTF();
                    ((DataInputStream)value2).readLong();
                    ((DataInputStream)value2).readLong();
                    ((DataInputStream)value2).readLong();
                    ((DataInputStream)value2).readBoolean();
                    ((DataInputStream)value2).readBoolean();
                    ((DataInputStream)value2).readInt();
                    ((DataInputStream)value2).readInt();
                    ((DataInputStream)value2).readInt();
                    ((DataInputStream)value2).readInt();
                    ((DataInputStream)value2).readInt();
                    ((DataInputStream)value2).readInt();
                    ((DataInputStream)value2).readInt();
                    ((DataInputStream)value2).readInt();
                    ((DataInputStream)value2).readInt();
                    ((DataInputStream)value2).readInt();
                    ((DataInputStream)value2).readInt();
                    ((DataInputStream)value2).readInt();
                    ((DataInputStream)value2).readInt();
                    ((DataInputStream)value2).readInt();
                    ((DataInputStream)value2).readInt();
                    ((DataInputStream)value2).readInt();
                    ((DataInputStream)value2).readBoolean();
                    ((DataInputStream)value2).readInt();
                    ((DataInputStream)value2).readInt();
                    ((DataInputStream)value2).readInt();
                    ((DataInputStream)value2).readInt();
                    ((DataInputStream)value2).readInt();
                    ((DataInputStream)value2).readInt();
                    ((DataInputStream)value2).readInt();
                    ((DataInputStream)value2).readInt();
                    ((DataInputStream)value2).readDouble();
                    ((DataInputStream)value2).readBoolean();
                    ((DataInputStream)value2).readBoolean();
                    ((DataInputStream)value2).readInt();
                    ((DataInputStream)value2).readInt();
                    ((DataInputStream)value2).readInt();
                    ((DataInputStream)value2).readInt();
                    ((DataInputStream)value2).readInt();
                    ((DataInputStream)value2).readInt();
                    if (s <= 26) {
                        ((DataInputStream)value2).readDouble();
                    } else {
                        ((DataInputStream)value2).readUnsignedShort();
                    }
                    ((DataInputStream)value2).readBoolean();
                    int index = 0;
                    while (index < 4) {
                        ((DataInputStream)value2).readInt();
                        ++index;
                    }
                    index = 0;
                    while (index < 4) {
                        ((DataInputStream)value2).readInt();
                        ++index;
                    }
                    index = 0;
                    while (index < 4) {
                        ((DataInputStream)value2).readInt();
                        ++index;
                    }
                    index = 0;
                    while (index < 7) {
                        ((DataInputStream)value2).readInt();
                        ++index;
                    }
                    index = 0;
                    while (index < 5) {
                        ((DataInputStream)value2).readInt();
                        ++index;
                    }
                    index = 0;
                    while (index < 22) {
                        ((DataInputStream)value2).readInt();
                        ++index;
                    }
                    index = 0;
                    while (index < 22) {
                        ((DataInputStream)value2).readInt();
                        ++index;
                    }
                    index = 0;
                    while (index < 28) {
                        value6 = ((DataInputStream)value2).readInt();
                        if (value6 != 65535) {
                            ((DataInputStream)value2).readInt();
                            ((DataInputStream)value2).readInt();
                        }
                        ++index;
                    }
                    index = 0;
                    while (index < 14) {
                        value6 = ((DataInputStream)value2).readInt();
                        if (value6 != 65535) {
                            ((DataInputStream)value2).readInt();
                            if (s >= 23) {
                                ((DataInputStream)value2).readInt();
                            }
                        }
                        ++index;
                    }
                    try {
                        if (s < 17) {
                            index = 0;
                            while (index < 352) {
                                value6 = ((DataInputStream)value2).readInt();
                                if (value6 != 65535) {
                                    ((DataInputStream)value2).readInt();
                                    ((DataInputStream)value2).readInt();
                                }
                                ++index;
                            }
                        } else {
                            index = ((DataInputStream)value2).readByte();
                            value6 = 0;
                            while (value6 < index) {
                                value5 = ((DataInputStream)value2).readUnsignedShort();
                                value4 = 0;
                                while (value4 < value5) {
                                    value3 = ((DataInputStream)value2).readInt();
                                    if (value3 != 65535) {
                                        ((DataInputStream)value2).readInt();
                                        ((DataInputStream)value2).readInt();
                                    }
                                    ++value4;
                                }
                                ++value6;
                            }
                        }
                        index = 0;
                        while (index < 200) {
                            ((DataInputStream)value2).readLong();
                            ++index;
                        }
                        index = 0;
                        while (index < 100) {
                            ((DataInputStream)value2).readLong();
                            ++index;
                        }
                        index = 0;
                        while (index < 28) {
                            ((DataInputStream)value2).readInt();
                            ((DataInputStream)value2).readInt();
                            ++index;
                        }
                        ((DataInputStream)value2).readInt();
                        ((DataInputStream)value2).readLong();
                        ((DataInputStream)value2).readLong();
                        index = 0;
                        while (index < 6) {
                            ((DataInputStream)value2).readBoolean();
                            ++index;
                        }
                        ((DataInputStream)value2).readInt();
                        ((DataInputStream)value2).readInt();
                        ((DataInputStream)value2).readInt();
                        ((DataInputStream)value2).readInt();
                        ((DataInputStream)value2).readInt();
                        ((DataInputStream)value2).readDouble();
                        index = 0;
                        while (index < 8) {
                            ((DataInputStream)value2).readInt();
                            ++index;
                        }
                        index = 0;
                        while (index < 8) {
                            ((DataInputStream)value2).readInt();
                            ++index;
                        }
                        index = 0;
                        while (index < 8) {
                            ((DataInputStream)value2).readInt();
                            ++index;
                        }
                        index = 0;
                        while (index < 8) {
                            ((DataInputStream)value2).readInt();
                            ++index;
                        }
                        index = 0;
                        while (index < 8) {
                            ((DataInputStream)value2).readLong();
                            ++index;
                        }
                        index = 0;
                        while (index < 8) {
                            ((DataInputStream)value2).readDouble();
                            ++index;
                        }
                        index = 0;
                        while (index < 8) {
                            ((DataInputStream)value2).readBoolean();
                            ++index;
                        }
                        index = 0;
                        while (index < 4) {
                            ((DataInputStream)value2).readInt();
                            ++index;
                        }
                        index = 0;
                        while (index < 4) {
                            ((DataInputStream)value2).readInt();
                            ++index;
                        }
                        index = 0;
                        while (index < 4) {
                            ((DataInputStream)value2).readInt();
                            ++index;
                        }
                        index = 0;
                        while (index < 4) {
                            ((DataInputStream)value2).readLong();
                            ++index;
                        }
                        index = 0;
                        while (index < 4) {
                            ((DataInputStream)value2).readDouble();
                            ++index;
                        }
                        index = 0;
                        while (index < 4) {
                            ((DataInputStream)value2).readBoolean();
                            ++index;
                        }
                        index = 0;
                        while (index < 4) {
                            ((DataInputStream)value2).readInt();
                            ++index;
                        }
                        index = 0;
                        while (index < 4) {
                            ((DataInputStream)value2).readInt();
                            ++index;
                        }
                        index = 0;
                        while (index < 4) {
                            ((DataInputStream)value2).readInt();
                            ++index;
                        }
                        index = 0;
                        while (index < 4) {
                            ((DataInputStream)value2).readLong();
                            ++index;
                        }
                        index = 0;
                        while (index < 4) {
                            ((DataInputStream)value2).readDouble();
                            ++index;
                        }
                        index = 0;
                        while (index < 4) {
                            ((DataInputStream)value2).readInt();
                            ++index;
                        }
                        index = 0;
                        while (index < 4) {
                            ((DataInputStream)value2).readInt();
                            ++index;
                        }
                        index = 0;
                        while (index < 4) {
                            ((DataInputStream)value2).readInt();
                            ++index;
                        }
                        index = 0;
                        while (index < 4) {
                            ((DataInputStream)value2).readLong();
                            ++index;
                        }
                        index = 0;
                        while (index < 4) {
                            ((DataInputStream)value2).readDouble();
                            ++index;
                        }
                        index = 0;
                        while (index < 4) {
                            ((DataInputStream)value2).readBoolean();
                            ++index;
                        }
                        index = 0;
                        while (index < 4) {
                            ((DataInputStream)value2).readInt();
                            ++index;
                        }
                        index = 0;
                        while (index < 4) {
                            ((DataInputStream)value2).readInt();
                            ++index;
                        }
                        index = 0;
                        while (index < 4) {
                            ((DataInputStream)value2).readInt();
                            ++index;
                        }
                        index = 0;
                        while (index < 4) {
                            ((DataInputStream)value2).readInt();
                            ++index;
                        }
                        index = 0;
                        while (index < 4) {
                            ((DataInputStream)value2).readLong();
                            ++index;
                        }
                        index = 0;
                        while (index < 4) {
                            ((DataInputStream)value2).readDouble();
                            ++index;
                        }
                        index = 0;
                        while (index < 4) {
                            ((DataInputStream)value2).readInt();
                            ++index;
                        }
                        index = 0;
                        while (index < 4) {
                            ((DataInputStream)value2).readInt();
                            ++index;
                        }
                        index = 0;
                        while (index < 4) {
                            ((DataInputStream)value2).readInt();
                            ++index;
                        }
                        index = 0;
                        while (index < 4) {
                            ((DataInputStream)value2).readInt();
                            ++index;
                        }
                        index = 0;
                        while (index < 4) {
                            ((DataInputStream)value2).readLong();
                            ++index;
                        }
                        index = 0;
                        while (index < 4) {
                            ((DataInputStream)value2).readDouble();
                            ++index;
                        }
                        index = 0;
                        while (index < 4) {
                            ((DataInputStream)value2).readBoolean();
                            ++index;
                        }
                        index = 0;
                        while (index < 4) {
                            ((DataInputStream)value2).readInt();
                            ++index;
                        }
                        index = 0;
                        while (index < 4) {
                            ((DataInputStream)value2).readInt();
                            ++index;
                        }
                        index = 0;
                        while (index < 4) {
                            ((DataInputStream)value2).readInt();
                            ++index;
                        }
                        index = 0;
                        while (index < 4) {
                            ((DataInputStream)value2).readLong();
                            ++index;
                        }
                        index = 0;
                        while (index < 4) {
                            ((DataInputStream)value2).readDouble();
                            ++index;
                        }
                        index = 0;
                        while (index < 4) {
                            ((DataInputStream)value2).readInt();
                            ++index;
                        }
                        index = 0;
                        while (index < 4) {
                            ((DataInputStream)value2).readInt();
                            ++index;
                        }
                        index = 0;
                        while (index < 4) {
                            ((DataInputStream)value2).readInt();
                            ++index;
                        }
                        index = 0;
                        while (index < 4) {
                            ((DataInputStream)value2).readLong();
                            ++index;
                        }
                        index = 0;
                        while (index < 4) {
                            ((DataInputStream)value2).readDouble();
                            ++index;
                        }
                        index = 0;
                        while (index < 4) {
                            ((DataInputStream)value2).readInt();
                            ++index;
                        }
                        index = 0;
                        while (index < 4) {
                            ((DataInputStream)value2).readInt();
                            ++index;
                        }
                        index = 0;
                        while (index < 4) {
                            ((DataInputStream)value2).readInt();
                            ++index;
                        }
                        index = 0;
                        while (index < 4) {
                            ((DataInputStream)value2).readInt();
                            ++index;
                        }
                        index = 0;
                        while (index < 4) {
                            ((DataInputStream)value2).readLong();
                            ++index;
                        }
                        index = 0;
                        while (index < 4) {
                            ((DataInputStream)value2).readDouble();
                            ++index;
                        }
                        index = 0;
                        while (index < 4) {
                            ((DataInputStream)value2).readBoolean();
                            ++index;
                        }
                        index = 0;
                        while (index < 4) {
                            ((DataInputStream)value2).readInt();
                            ++index;
                        }
                        index = 0;
                        while (index < 4) {
                            ((DataInputStream)value2).readLong();
                            ++index;
                        }
                        index = 0;
                        while (index < 4) {
                            ((DataInputStream)value2).readInt();
                            ++index;
                        }
                        index = 0;
                        while (index < 18) {
                            ((DataInputStream)value2).readInt();
                            ++index;
                        }
                    }
                    catch (IOException iOException) {
                        throw iOException;
                    }
                    try {
                        ((DataInputStream)value2).readInt();
                        ((DataInputStream)value2).readUTF();
                        ((DataInputStream)value2).readInt();
                    }
                    catch (IOException iOException) {
                        throw iOException;
                    }
                    try {
                        ((DataInputStream)value2).readBoolean();
                    }
                    catch (IOException iOException) {
                        throw iOException;
                    }
                    try {
                        ((DataInputStream)value2).readBoolean();
                    }
                    catch (IOException iOException) {
                        throw iOException;
                    }
                    try {
                        ((DataInputStream)value2).readBoolean();
                    }
                    catch (IOException iOException) {
                        throw iOException;
                    }
                    int index2 = 0;
                    while (index2 < musicUnlockConfigIds.length) {
                        try {
                            ((DataInputStream)value2).readInt();
                        }
                        catch (IOException iOException) {
                            throw iOException;
                        }
                        ++index2;
                    }
                    index2 = 0;
                    while (index2 < QuestDefinition.questStateCapacity) {
                        try {
                            ((DataInputStream)value2).readInt();
                        }
                        catch (IOException iOException) {
                            throw iOException;
                        }
                        ++index2;
                    }
                    ((DataInputStream)value2).readByte();
                    ((DataInputStream)value2).readByte();
                    ((DataInputStream)value2).readBoolean();
                    ((DataInputStream)value2).readByte();
                    ((DataInputStream)value2).readByte();
                    ((DataInputStream)value2).readBoolean();
                    if (s < 2) break validateCharacterFileControlExit1;
                    ((DataInputStream)value2).readByte();
                    if (s >= 3) {
                        ((DataInputStream)value2).readInt();
                    }
                    if (s >= 4) {
                        ((DataInputStream)value2).readBoolean();
                        ((DataInputStream)value2).readBoolean();
                        ((DataInputStream)value2).readInt();
                    }
                    if (s >= 5) {
                        ((DataInputStream)value2).readByte();
                        ((DataInputStream)value2).readInt();
                    }
                    if (s >= 6) {
                        ((DataInputStream)value2).readInt();
                    }
                    if (s >= 7) {
                        index2 = 0;
                        while (index2 < QuestDefinition.questStateCapacity) {
                            try {
                                ((DataInputStream)value2).readInt();
                            }
                            catch (IOException iOException) {
                                throw iOException;
                            }
                            ++index2;
                        }
                        index2 = 0;
                        while (index2 < 100) {
                            try {
                                ((DataInputStream)value2).readInt();
                            }
                            catch (IOException iOException) {
                                throw iOException;
                            }
                            ++index2;
                        }
                    }
                    if (s >= 8) {
                        ((DataInputStream)value2).readByte();
                        ((DataInputStream)value2).readByte();
                        ((DataInputStream)value2).readByte();
                    }
                    if (s >= 9) {
                        ((DataInputStream)value2).readInt();
                    }
                    if (s >= 10) {
                        ((DataInputStream)value2).readLong();
                        ((DataInputStream)value2).readInt();
                        ((DataInputStream)value2).readInt();
                    }
                    if (s >= 11) {
                        ((DataInputStream)value2).readUTF();
                    }
                    if (s >= 12) {
                        ((DataInputStream)value2).readInt();
                    }
                    if (s >= 13) {
                        ((DataInputStream)value2).readInt();
                        ((DataInputStream)value2).readInt();
                        ((DataInputStream)value2).readInt();
                        ((DataInputStream)value2).readByte();
                    }
                    if (s >= 14) {
                        index2 = 0;
                        while (index2 < 6) {
                            ((DataInputStream)value2).readBoolean();
                            ++index2;
                        }
                        index2 = 0;
                        while (index2 < 6) {
                            ((DataInputStream)value2).readInt();
                            ++index2;
                        }
                        index2 = 0;
                        while (index2 < 6) {
                            ((DataInputStream)value2).readInt();
                            ++index2;
                        }
                        index2 = 0;
                        while (index2 < 6) {
                            ((DataInputStream)value2).readInt();
                            ++index2;
                        }
                        index2 = 0;
                        while (index2 < 6) {
                            ((DataInputStream)value2).readBoolean();
                            ++index2;
                        }
                        index2 = 0;
                        while (index2 < 6) {
                            ((DataInputStream)value2).readInt();
                            ++index2;
                        }
                        index2 = 0;
                        while (index2 < 6) {
                            ((DataInputStream)value2).readInt();
                            ++index2;
                        }
                        index2 = 0;
                        while (index2 < 6) {
                            ((DataInputStream)value2).readInt();
                            ++index2;
                        }
                        index2 = 0;
                        while (index2 < 6) {
                            ((DataInputStream)value2).readInt();
                            ++index2;
                        }
                        index2 = 0;
                        while (index2 < 6) {
                            ((DataInputStream)value2).readBoolean();
                            ++index2;
                        }
                    }
                    if (s >= 15) {
                        ((DataInputStream)value2).readInt();
                        ((DataInputStream)value2).readInt();
                        ((DataInputStream)value2).readInt();
                        ((DataInputStream)value2).readInt();
                        ((DataInputStream)value2).readBoolean();
                        ((DataInputStream)value2).readByte();
                        ((DataInputStream)value2).readBoolean();
                        ((DataInputStream)value2).readByte();
                    }
                    if (s >= 16) {
                        ((DataInputStream)value2).readInt();
                    }
                    if (s >= 18) {
                        ((DataInputStream)value2).readByte();
                    }
                    if (s >= 19) {
                        ((DataInputStream)value2).readInt();
                    }
                    if (s >= 21) {
                        ((DataInputStream)value2).readLong();
                        ((DataInputStream)value2).readInt();
                        index2 = 0;
                        while (index2 < 4) {
                            ((DataInputStream)value2).readInt();
                            ++index2;
                        }
                    }
                    if (s >= 22) {
                        ((DataInputStream)value2).readByte();
                    }
                    if (s >= 24) {
                        ((DataInputStream)value2).readLong();
                        ((DataInputStream)value2).readByte();
                    }
                    if (s >= 25) {
                        ((DataInputStream)value2).readInt();
                    }
                    if (s >= 26) {
                        ((DataInputStream)value2).readUnsignedShort();
                    }
                    if (s >= 29) {
                        ((DataInputStream)value2).readInt();
                    }
                    if (s >= 30) {
                        ((DataInputStream)value2).readBoolean();
                        ((DataInputStream)value2).readBoolean();
                        ((DataInputStream)value2).readInt();
                    }
                    if (s >= 31) {
                        ((DataInputStream)value2).readInt();
                    }
                    if (s >= 20) {
                        boolean enabled = ((DataInputStream)value2).readBoolean();
                        index2 = enabled ? 1 : 0;
                        if (enabled) {
                            ((DataInputStream)value2).readByte();
                            ((DataInputStream)value2).readByte();
                            ((DataInputStream)value2).readInt();
                            ((DataInputStream)value2).readByte();
                            ((DataInputStream)value2).readInt();
                            ((DataInputStream)value2).readUTF();
                            byte value7 = ((DataInputStream)value2).readByte();
                            value5 = 0;
                            while (value5 < value7) {
                                ((DataInputStream)value2).readInt();
                                ((DataInputStream)value2).readInt();
                                ++value5;
                            }
                            ((DataInputStream)value2).readInt();
                            ((DataInputStream)value2).readByte();
                            ((DataInputStream)value2).readByte();
                            ((DataInputStream)value2).readBoolean();
                            ((DataInputStream)value2).readLong();
                            ((DataInputStream)value2).readByte();
                            ((DataInputStream)value2).readByte();
                            ((DataInputStream)value2).readByte();
                            ((DataInputStream)value2).readInt();
                            ((DataInputStream)value2).readInt();
                            ((DataInputStream)value2).readInt();
                            ((DataInputStream)value2).readBoolean();
                            ((DataInputStream)value2).readBoolean();
                            ((DataInputStream)value2).readInt();
                            ((DataInputStream)value2).readByte();
                            ((DataInputStream)value2).readInt();
                            ((DataInputStream)value2).readInt();
                            value5 = ((DataInputStream)value2).readByte();
                            value4 = 0;
                            while (value4 < value5) {
                                ((DataInputStream)value2).readInt();
                                ++value4;
                            }
                            value4 = ((DataInputStream)value2).readByte();
                            value3 = 0;
                            while (value3 < value4) {
                                ((DataInputStream)value2).readInt();
                                ++value3;
                            }
                            ((DataInputStream)value2).readByte();
                            ((DataInputStream)value2).readByte();
                            ((DataInputStream)value2).readByte();
                            ((DataInputStream)value2).readByte();
                            ((DataInputStream)value2).readByte();
                            ((DataInputStream)value2).readByte();
                            ((DataInputStream)value2).readByte();
                            ((DataInputStream)value2).readInt();
                            ((DataInputStream)value2).readInt();
                            ((DataInputStream)value2).readInt();
                            ((DataInputStream)value2).readInt();
                            ((DataInputStream)value2).readInt();
                            ((DataInputStream)value2).readInt();
                            ((DataInputStream)value2).readBoolean();
                            ((DataInputStream)value2).readBoolean();
                            ((DataInputStream)value2).readBoolean();
                            ((DataInputStream)value2).readBoolean();
                            ((DataInputStream)value2).readBoolean();
                            ((DataInputStream)value2).readBoolean();
                            ((DataInputStream)value2).readByte();
                            ((DataInputStream)value2).readByte();
                            ((DataInputStream)value2).readByte();
                            ((DataInputStream)value2).readByte();
                            ((DataInputStream)value2).readByte();
                            ((DataInputStream)value2).readInt();
                            ((DataInputStream)value2).readInt();
                            ((DataInputStream)value2).readInt();
                            ((DataInputStream)value2).readInt();
                            ((DataInputStream)value2).readInt();
                            ((DataInputStream)value2).readUTF();
                            ((DataInputStream)value2).readUTF();
                            ((DataInputStream)value2).readUTF();
                            ((DataInputStream)value2).readUTF();
                            ((DataInputStream)value2).readUTF();
                        }
                    }
                }
                catch (Exception exception) {
                    ((FilterInputStream)value2).close();
                    ((FileInputStream)value).close();
                    return false;
                }
            }
            ((FilterInputStream)value2).close();
            ((FileInputStream)value).close();
            return true;
        }
        catch (IOException iOException) {
            return false;
        }
    }

    public static void archiveDeadHardcoreIronman(Player player) {
        int index = 0;
        while (index < liveHiscoreRecords.size()) {
            CharacterFileRecord characterFileRecord = (CharacterFileRecord)liveHiscoreRecords.get(index);
            if (characterFileRecord.username.toLowerCase().equals(player.getUsername().toLowerCase())) {
                CharacterFileRecord updatedRecord = CharacterFileManager.readCharacterFileRecord("./data/characters/", player.getUsername(), true);
                if (updatedRecord != null) {
                    liveHiscoreRecords.set(index, updatedRecord);
                }
                break;
            }
            ++index;
        }
        File primaryJson = new File("./data/characters/" + player.getUsername() + ".json");
        if (primaryJson.isFile()) {
            try {
                File deadDir = new File("./data/dead_hcim_characters/");
                if (!deadDir.isDirectory()) deadDir.mkdirs();
                Files.copy(primaryJson.toPath(),
                        new File(deadDir, player.getUsername() + ".json").toPath(),
                        StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException exception) {
                System.err.println("Failed to archive hardcore character: " + exception);
            }
        } else {
            byte[] bytes = FileUtil.readBytes("./data/characters/" + player.getUsername() + ".dat");
            FileUtil.writeBytes("./data/dead_hcim_characters/" + player.getUsername() + ".dat", bytes);
        }
        CharacterFileRecord characterFileRecord = CharacterFileManager.readCharacterFileRecord("./data/dead_hcim_characters/", player.getUsername(), true);
        if (characterFileRecord != null) {
            deadHardcoreIronmanRecords.add(characterFileRecord);
        }
    }

    public static void refreshLiveHiscoreRecord(Player player) {
        if (player.isBot && player.botMode != 4) {
            return;
        }
        int index = 0;
        while (index < liveHiscoreRecords.size()) {
            CharacterFileRecord characterFileRecord = (CharacterFileRecord)liveHiscoreRecords.get(index);
            if (characterFileRecord.username.toLowerCase().equals(player.getUsername().toLowerCase())) {
                CharacterFileRecord updatedRecord = CharacterFileManager.readCharacterFileRecord("./data/characters/", player.getUsername(), true);
                if (updatedRecord != null) {
                    liveHiscoreRecords.set(index, updatedRecord);
                }
                return;
            }
            ++index;
        }
        CharacterFileRecord characterFileRecord2 = CharacterFileManager.readCharacterFileRecord("./data/characters/", player.getUsername(), true);
        if (characterFileRecord2 != null) {
            liveHiscoreRecords.add(characterFileRecord2);
        }
    }

    /*
     * Enabled aggressive exception aggregation
     */
    private static CharacterFileRecord readCharacterFileRecord(String path, String username, boolean enabled6) {
        File json = new File(path + username + ".json");
        if (json.isFile()) {
            try {
                return CharacterJsonCodec.read(json, username);
            } catch (IOException exception) {
                System.err.println("Refusing invalid JSON character " + username + ": " + exception);
                return null;
            }
        }
        Object value = path;
        Object value2 = username;
        if (!((File)(value = new File(String.valueOf(value) + (String)value2 + ".dat"))).exists()) {
            return null;
        }
        value2 = new CharacterFileRecord();
        try {
            DataInputStream dataInputStream;
            readCharacterFileRecordControlExit1: {
                value = new FileInputStream((File)value);
                dataInputStream = new DataInputStream((InputStream)value);
                try {
                    int value3;
                    int value4;
                    int value5;
                    short s = dataInputStream.readShort();
                    if (s < 24) {
                        dataInputStream.close();
                        return null;
                    }
                    String text = dataInputStream.readUTF();
                    Object value6 = value2;
                    ((CharacterFileRecord)value2).username = text;
                    text = dataInputStream.readUTF();
                    value6 = value2;
                    ((CharacterFileRecord)value2).password = text;
                    text = dataInputStream.readUTF();
                    value6 = value2;
                    ((CharacterFileRecord)value2).hostAddress = text;
                    int savedRights = dataInputStream.readInt();
                    boolean enabled = false;
                    value6 = value2;
                    ((CharacterFileRecord)value2).playerRights = savedRights;
                    String text2 = dataInputStream.readUTF();
                    value6 = value2;
                    ((CharacterFileRecord)value2).legacyProfileString = text2;
                    text2 = dataInputStream.readUTF();
                    value6 = value2;
                    ((CharacterFileRecord)value2).profileString1 = text2;
                    text2 = dataInputStream.readUTF();
                    value6 = value2;
                    ((CharacterFileRecord)value2).profileString2 = text2;
                    long value7 = dataInputStream.readLong();
                    value6 = value2;
                    ((CharacterFileRecord)value2).lastSavedMillis = value7;
                    value7 = dataInputStream.readLong();
                    value6 = value2;
                    ((CharacterFileRecord)value2).totalPlayTimeMillis = value7;
                    value7 = dataInputStream.readLong();
                    value6 = value2;
                    ((CharacterFileRecord)value2).createdAtMillis = value7;
                    value6 = value2;
                    ((CharacterFileRecord)value2).loginRestrictionExempt = dataInputStream.readBoolean();
                    value6 = value2;
                    ((CharacterFileRecord)value2).memberFlag = dataInputStream.readBoolean();
                    int value8 = dataInputStream.readInt();
                    value6 = value2;
                    ((CharacterFileRecord)value2).donatorPoints = value8;
                    value8 = dataInputStream.readInt();
                    value6 = value2;
                    ((CharacterFileRecord)value2).x = value8;
                    value8 = dataInputStream.readInt();
                    value6 = value2;
                    ((CharacterFileRecord)value2).y = value8;
                    value8 = dataInputStream.readInt();
                    value6 = value2;
                    ((CharacterFileRecord)value2).plane = value8;
                    value8 = dataInputStream.readInt();
                    value6 = value2;
                    ((CharacterFileRecord)value2).gender = value8;
                    value8 = dataInputStream.readInt();
                    value6 = value2;
                    ((CharacterFileRecord)value2).npcKillCount = value8;
                    value8 = dataInputStream.readInt();
                    value6 = value2;
                    ((CharacterFileRecord)value2).playerKillCount = value8;
                    value8 = dataInputStream.readInt();
                    value6 = value2;
                    ((CharacterFileRecord)value2).deathCount = value8;
                    value8 = dataInputStream.readInt();
                    value6 = value2;
                    ((CharacterFileRecord)value2).easyCluesCompleted = value8;
                    value8 = dataInputStream.readInt();
                    value6 = value2;
                    ((CharacterFileRecord)value2).mediumCluesCompleted = value8;
                    value8 = dataInputStream.readInt();
                    value6 = value2;
                    ((CharacterFileRecord)value2).hardCluesCompleted = value8;
                    value8 = dataInputStream.readInt();
                    value6 = value2;
                    ((CharacterFileRecord)value2).soldItemsValue = value8;
                    value8 = dataInputStream.readInt();
                    value6 = value2;
                    ((CharacterFileRecord)value2).boughtItemsValue = value8;
                    value8 = dataInputStream.readInt();
                    value6 = value2;
                    ((CharacterFileRecord)value2).duelWins = value8;
                    value8 = dataInputStream.readInt();
                    value6 = value2;
                    ((CharacterFileRecord)value2).duelLosses = value8;
                    value8 = dataInputStream.readInt();
                    value6 = value2;
                    ((CharacterFileRecord)value2).legacyQuestPoints = value8;
                    value6 = value2;
                    ((CharacterFileRecord)value2).autoRetaliate = dataInputStream.readBoolean();
                    value8 = dataInputStream.readInt();
                    value6 = value2;
                    ((CharacterFileRecord)value2).fightMode = value8;
                    value8 = dataInputStream.readInt();
                    value6 = value2;
                    ((CharacterFileRecord)value2).brightness = value8;
                    value8 = dataInputStream.readInt();
                    value6 = value2;
                    ((CharacterFileRecord)value2).mouseButtons = value8;
                    value8 = dataInputStream.readInt();
                    value6 = value2;
                    ((CharacterFileRecord)value2).publicChatEffects = value8;
                    value8 = dataInputStream.readInt();
                    value6 = value2;
                    ((CharacterFileRecord)value2).splitPrivateChat = value8;
                    value8 = dataInputStream.readInt();
                    value6 = value2;
                    ((CharacterFileRecord)value2).acceptAid = value8;
                    value8 = dataInputStream.readInt();
                    value6 = value2;
                    ((CharacterFileRecord)value2).musicVolume = value8;
                    value8 = dataInputStream.readInt();
                    value6 = value2;
                    ((CharacterFileRecord)value2).effectVolume = value8;
                    value8 = (int)dataInputStream.readDouble();
                    value6 = value2;
                    ((CharacterFileRecord)value2).specialEnergy = value8;
                    value6 = value2;
                    ((CharacterFileRecord)value2).changingBankPin = dataInputStream.readBoolean();
                    value6 = value2;
                    ((CharacterFileRecord)value2).deletingBankPin = dataInputStream.readBoolean();
                    value8 = dataInputStream.readInt();
                    value6 = value2;
                    ((CharacterFileRecord)value2).pinAppendYear = value8;
                    value8 = dataInputStream.readInt();
                    value6 = value2;
                    ((CharacterFileRecord)value2).pinAppendDate = value8;
                    value8 = dataInputStream.readInt();
                    value6 = value2;
                    ((CharacterFileRecord)value2).bindingNecklaceCharge = value8;
                    value8 = dataInputStream.readInt();
                    value6 = value2;
                    ((CharacterFileRecord)value2).ringOfForgingLife = value8;
                    value8 = dataInputStream.readInt();
                    value6 = value2;
                    ((CharacterFileRecord)value2).ringOfRecoilLife = value8;
                    value8 = dataInputStream.readInt();
                    value6 = value2;
                    ((CharacterFileRecord)value2).skullTimer = value8;
                    if (s <= 26) {
                        double value9 = dataInputStream.readDouble();
                        value8 = (int)value9;
                        value6 = value2;
                        double value10 = value8;
                        double value11 = (value10 /= 100.0) * 10000.0;
                        ((CharacterFileRecord)value2).runEnergyRaw = value5 = (int)value11;
                    } else {
                        value5 = dataInputStream.readUnsignedShort();
                        value6 = value2;
                        ((CharacterFileRecord)value2).runEnergyRaw = value5;
                    }
                    value6 = value2;
                    ((CharacterFileRecord)value2).running = dataInputStream.readBoolean();
                    int index = 0;
                    while (index < 4) {
                        ((CharacterFileRecord)value2).currentPin[index] = dataInputStream.readInt();
                        ++index;
                    }
                    index = 0;
                    while (index < 4) {
                        ((CharacterFileRecord)value2).pendingPin[index] = dataInputStream.readInt();
                        ++index;
                    }
                    index = 0;
                    while (index < 4) {
                        ((CharacterFileRecord)value2).essencePouchAmounts[index] = dataInputStream.readInt();
                        ++index;
                    }
                    index = 0;
                    while (index < 7) {
                        ((CharacterFileRecord)value2).appearanceParts[index] = dataInputStream.readInt();
                        ++index;
                    }
                    index = 0;
                    while (index < 5) {
                        ((CharacterFileRecord)value2).appearanceColors[index] = dataInputStream.readInt();
                        ++index;
                    }
                    index = 0;
                    while (index < 22) {
                        int value12 = dataInputStream.readInt();
                        value8 = index++;
                        value6 = value2;
                        ((CharacterFileRecord)value6).currentLevels[value8] = value12;
                    }
                    index = 0;
                    while (index < 22) {
                        int value13 = dataInputStream.readInt();
                        value8 = index++;
                        value6 = value2;
                        ((CharacterFileRecord)value6).skillExperience[value8] = value13;
                    }
                    index = 0;
                    while (index < 28) {
                        value4 = dataInputStream.readInt();
                        if (value4 != 65535) {
                            int value14 = dataInputStream.readInt();
                            value5 = dataInputStream.readInt();
                            ((CharacterFileRecord)value2).inventoryItems[index] = new ItemStack(value4, value14, value5);
                        }
                        ++index;
                    }
                    index = 0;
                    while (index < 14) {
                        value4 = dataInputStream.readInt();
                        if (value4 != 65535) {
                            int value15 = dataInputStream.readInt();
                            value5 = -1;
                            if (s >= 23) {
                                value5 = dataInputStream.readInt();
                            }
                            ((CharacterFileRecord)value2).equipmentItems[index] = new ItemStack(value4, value15, value5);
                        }
                        ++index;
                    }
                    try {
                        if (s < 17) {
                            index = 0;
                            while (index < 352) {
                                int value16 = dataInputStream.readInt();
                                if (value16 != 65535) {
                                    value5 = dataInputStream.readInt();
                                    value3 = dataInputStream.readInt();
                                    ItemStack itemStack = new ItemStack(value16, value5, value3);
                                    ((CharacterFileRecord)value2).setBankTabItem(index, itemStack, 0);
                                }
                                ++index;
                            }
                        } else {
                            index = dataInputStream.readByte();
                            value4 = 0;
                            while (value4 < index) {
                                int value17 = dataInputStream.readUnsignedShort();
                                value5 = 0;
                                while (value5 < value17) {
                                    int value18 = dataInputStream.readInt();
                                    if (value18 != 65535) {
                                        int value19 = dataInputStream.readInt();
                                        int value20 = dataInputStream.readInt();
                                        ItemStack itemStack = new ItemStack(value18, value19, value20);
                                        ((CharacterFileRecord)value2).setBankTabItem(value5, itemStack, value4);
                                    }
                                    ++value5;
                                }
                                ++value4;
                            }
                        }
                        index = 0;
                        while (index < 200) {
                            ((CharacterFileRecord)value2).friendsList[index] = dataInputStream.readLong();
                            ++index;
                        }
                        index = 0;
                        while (index < 100) {
                            ((CharacterFileRecord)value2).ignoreList[index] = dataInputStream.readLong();
                            ++index;
                        }
                        index = 0;
                        while (index < 28) {
                            ((CharacterFileRecord)value2).queuedLoginItemIds[index] = dataInputStream.readInt();
                            ((CharacterFileRecord)value2).queuedLoginItemAmounts[index] = dataInputStream.readInt();
                            ++index;
                        }
                        value8 = dataInputStream.readInt();
                        Object value21 = value2;
                        ((CharacterFileRecord)value2).abyssMageNpcId = value8;
                        long value22 = dataInputStream.readLong();
                        value21 = value2;
                        ((CharacterFileRecord)value2).muteExpires = value22;
                        value22 = dataInputStream.readLong();
                        value21 = value2;
                        ((CharacterFileRecord)value2).banExpires = value22;
                        index = 0;
                        while (index < 6) {
                            ((CharacterFileRecord)value2).barrowsKilledBrothers[index] = dataInputStream.readBoolean();
                            ++index;
                        }
                        int value23 = dataInputStream.readInt();
                        value21 = value2;
                        ((CharacterFileRecord)value2).barrowsKillCount = value23;
                        value23 = dataInputStream.readInt();
                        value21 = value2;
                        ((CharacterFileRecord)value2).barrowsTargetBrotherIndex = value23;
                        value23 = index = dataInputStream.readInt();
                        value21 = value2;
                        ((CharacterFileRecord)value2).poisonImmunityTicks = value23;
                        value23 = value4 = dataInputStream.readInt();
                        value21 = value2;
                        ((CharacterFileRecord)value2).antifireTicks = value23;
                        value23 = dataInputStream.readInt();
                        value21 = value2;
                        ((CharacterFileRecord)value2).teleblockTicks = value23;
                        double value24 = dataInputStream.readDouble();
                        value21 = value2;
                        ((CharacterFileRecord)value2).poisonDamage = value24;
                        int index2 = 0;
                        while (index2 < 8) {
                            ((CharacterFileRecord)value2).allotmentGrowthStages[index2] = dataInputStream.readInt();
                            ++index2;
                        }
                        index2 = 0;
                        while (index2 < 8) {
                            ((CharacterFileRecord)value2).allotmentCropIds[index2] = dataInputStream.readInt();
                            ++index2;
                        }
                        index2 = 0;
                        while (index2 < 8) {
                            if (s <= 27) {
                                dataInputStream.readInt();
                                ((CharacterFileRecord)value2).allotmentHarvestAmounts[index2] = 4;
                            } else {
                                ((CharacterFileRecord)value2).allotmentHarvestAmounts[index2] = dataInputStream.readInt();
                            }
                            ++index2;
                        }
                        index2 = 0;
                        while (index2 < 8) {
                            ((CharacterFileRecord)value2).allotmentPatchStates[index2] = dataInputStream.readInt();
                            ++index2;
                        }
                        index2 = 0;
                        while (index2 < 8) {
                            ((CharacterFileRecord)value2).allotmentLastUpdateTicks[index2] = dataInputStream.readLong();
                            ++index2;
                        }
                        index2 = 0;
                        while (index2 < 8) {
                            ((CharacterFileRecord)value2).allotmentDiseaseChanceMultipliers[index2] = dataInputStream.readDouble();
                            ++index2;
                        }
                        index2 = 0;
                        while (index2 < 8) {
                            ((CharacterFileRecord)value2).allotmentProtectionFlags[index2] = dataInputStream.readBoolean();
                            ++index2;
                        }
                        index2 = 0;
                        while (index2 < 4) {
                            ((CharacterFileRecord)value2).bushGrowthStages[index2] = dataInputStream.readInt();
                            ++index2;
                        }
                        index2 = 0;
                        while (index2 < 4) {
                            ((CharacterFileRecord)value2).bushCropIds[index2] = dataInputStream.readInt();
                            ++index2;
                        }
                        index2 = 0;
                        while (index2 < 4) {
                            ((CharacterFileRecord)value2).bushPatchStates[index2] = dataInputStream.readInt();
                            ++index2;
                        }
                        index2 = 0;
                        while (index2 < 4) {
                            ((CharacterFileRecord)value2).bushLastUpdateTicks[index2] = dataInputStream.readLong();
                            ++index2;
                        }
                        index2 = 0;
                        while (index2 < 4) {
                            ((CharacterFileRecord)value2).bushDiseaseChanceMultipliers[index2] = dataInputStream.readDouble();
                            ++index2;
                        }
                        index2 = 0;
                        while (index2 < 4) {
                            ((CharacterFileRecord)value2).bushSavedFlags[index2] = dataInputStream.readBoolean();
                            ++index2;
                        }
                        index2 = 0;
                        while (index2 < 4) {
                            ((CharacterFileRecord)value2).flowerGrowthStages[index2] = dataInputStream.readInt();
                            ++index2;
                        }
                        index2 = 0;
                        while (index2 < 4) {
                            ((CharacterFileRecord)value2).flowerCropIds[index2] = dataInputStream.readInt();
                            ++index2;
                        }
                        index2 = 0;
                        while (index2 < 4) {
                            ((CharacterFileRecord)value2).flowerPatchStates[index2] = dataInputStream.readInt();
                            ++index2;
                        }
                        index2 = 0;
                        while (index2 < 4) {
                            ((CharacterFileRecord)value2).flowerLastUpdateTicks[index2] = dataInputStream.readLong();
                            ++index2;
                        }
                        index2 = 0;
                        while (index2 < 4) {
                            ((CharacterFileRecord)value2).flowerDiseaseChanceMultipliers[index2] = dataInputStream.readDouble();
                            ++index2;
                        }
                        index2 = 0;
                        while (index2 < 4) {
                            ((CharacterFileRecord)value2).fruitTreeGrowthStages[index2] = dataInputStream.readInt();
                            ++index2;
                        }
                        index2 = 0;
                        while (index2 < 4) {
                            ((CharacterFileRecord)value2).fruitTreeIds[index2] = dataInputStream.readInt();
                            ++index2;
                        }
                        index2 = 0;
                        while (index2 < 4) {
                            ((CharacterFileRecord)value2).fruitTreePatchStates[index2] = dataInputStream.readInt();
                            ++index2;
                        }
                        index2 = 0;
                        while (index2 < 4) {
                            ((CharacterFileRecord)value2).fruitTreeLastUpdateTicks[index2] = dataInputStream.readLong();
                            ++index2;
                        }
                        index2 = 0;
                        while (index2 < 4) {
                            ((CharacterFileRecord)value2).fruitTreeDiseaseChanceMultipliers[index2] = dataInputStream.readDouble();
                            ++index2;
                        }
                        index2 = 0;
                        while (index2 < 4) {
                            ((CharacterFileRecord)value2).fruitTreeSavedFlags[index2] = dataInputStream.readBoolean();
                            ++index2;
                        }
                        index2 = 0;
                        while (index2 < 4) {
                            ((CharacterFileRecord)value2).herbGrowthStages[index2] = dataInputStream.readInt();
                            ++index2;
                        }
                        index2 = 0;
                        while (index2 < 4) {
                            ((CharacterFileRecord)value2).herbCropIds[index2] = dataInputStream.readInt();
                            ++index2;
                        }
                        index2 = 0;
                        while (index2 < 4) {
                            if (s <= 27) {
                                dataInputStream.readInt();
                                ((CharacterFileRecord)value2).herbHarvestAmounts[index2] = 4;
                            } else {
                                ((CharacterFileRecord)value2).herbHarvestAmounts[index2] = dataInputStream.readInt();
                            }
                            ++index2;
                        }
                        index2 = 0;
                        while (index2 < 4) {
                            ((CharacterFileRecord)value2).herbPatchStates[index2] = dataInputStream.readInt();
                            ++index2;
                        }
                        index2 = 0;
                        while (index2 < 4) {
                            ((CharacterFileRecord)value2).herbLastUpdateTicks[index2] = dataInputStream.readLong();
                            ++index2;
                        }
                        index2 = 0;
                        while (index2 < 4) {
                            ((CharacterFileRecord)value2).herbDiseaseChanceMultipliers[index2] = dataInputStream.readDouble();
                            ++index2;
                        }
                        index2 = 0;
                        while (index2 < 4) {
                            ((CharacterFileRecord)value2).hopsGrowthStages[index2] = dataInputStream.readInt();
                            ++index2;
                        }
                        index2 = 0;
                        while (index2 < 4) {
                            ((CharacterFileRecord)value2).hopsCropIds[index2] = dataInputStream.readInt();
                            ++index2;
                        }
                        index2 = 0;
                        while (index2 < 4) {
                            if (s <= 27) {
                                dataInputStream.readInt();
                                ((CharacterFileRecord)value2).hopsHarvestAmounts[index2] = 4;
                            } else {
                                ((CharacterFileRecord)value2).hopsHarvestAmounts[index2] = dataInputStream.readInt();
                            }
                            ++index2;
                        }
                        index2 = 0;
                        while (index2 < 4) {
                            ((CharacterFileRecord)value2).hopsPatchStates[index2] = dataInputStream.readInt();
                            ++index2;
                        }
                        index2 = 0;
                        while (index2 < 4) {
                            ((CharacterFileRecord)value2).hopsLastUpdateTicks[index2] = dataInputStream.readLong();
                            ++index2;
                        }
                        index2 = 0;
                        while (index2 < 4) {
                            ((CharacterFileRecord)value2).hopsDiseaseChanceMultipliers[index2] = dataInputStream.readDouble();
                            ++index2;
                        }
                        index2 = 0;
                        while (index2 < 4) {
                            ((CharacterFileRecord)value2).hopsProtectionFlags[index2] = dataInputStream.readBoolean();
                            ++index2;
                        }
                        index2 = 0;
                        while (index2 < 4) {
                            ((CharacterFileRecord)value2).specialTreeGrowthStages[index2] = dataInputStream.readInt();
                            ++index2;
                        }
                        index2 = 0;
                        while (index2 < 4) {
                            ((CharacterFileRecord)value2).specialTreeIds[index2] = dataInputStream.readInt();
                            ++index2;
                        }
                        index2 = 0;
                        while (index2 < 4) {
                            ((CharacterFileRecord)value2).specialTreePatchStates[index2] = dataInputStream.readInt();
                            ++index2;
                        }
                        index2 = 0;
                        while (index2 < 4) {
                            ((CharacterFileRecord)value2).specialTreeLastUpdateTicks[index2] = dataInputStream.readLong();
                            ++index2;
                        }
                        index2 = 0;
                        while (index2 < 4) {
                            ((CharacterFileRecord)value2).specialTreeDiseaseChanceMultipliers[index2] = dataInputStream.readDouble();
                            ++index2;
                        }
                        index2 = 0;
                        while (index2 < 4) {
                            ((CharacterFileRecord)value2).specialCropGrowthStages[index2] = dataInputStream.readInt();
                            ++index2;
                        }
                        index2 = 0;
                        while (index2 < 4) {
                            ((CharacterFileRecord)value2).specialCropIds[index2] = dataInputStream.readInt();
                            ++index2;
                        }
                        index2 = 0;
                        while (index2 < 4) {
                            ((CharacterFileRecord)value2).specialCropPatchStates[index2] = dataInputStream.readInt();
                            ++index2;
                        }
                        index2 = 0;
                        while (index2 < 4) {
                            ((CharacterFileRecord)value2).specialCropLastUpdateTicks[index2] = dataInputStream.readLong();
                            ++index2;
                        }
                        index2 = 0;
                        while (index2 < 4) {
                            ((CharacterFileRecord)value2).specialCropDiseaseChanceMultipliers[index2] = dataInputStream.readDouble();
                            ++index2;
                        }
                        index2 = 0;
                        while (index2 < 4) {
                            ((CharacterFileRecord)value2).treeGrowthStages[index2] = dataInputStream.readInt();
                            ++index2;
                        }
                        index2 = 0;
                        while (index2 < 4) {
                            ((CharacterFileRecord)value2).treeIds[index2] = dataInputStream.readInt();
                            ++index2;
                        }
                        index2 = 0;
                        while (index2 < 4) {
                            ((CharacterFileRecord)value2).treePatchData[index2] = dataInputStream.readInt();
                            ++index2;
                        }
                        index2 = 0;
                        while (index2 < 4) {
                            ((CharacterFileRecord)value2).treePatchStates[index2] = dataInputStream.readInt();
                            ++index2;
                        }
                        index2 = 0;
                        while (index2 < 4) {
                            ((CharacterFileRecord)value2).treeLastUpdateTicks[index2] = dataInputStream.readLong();
                            ++index2;
                        }
                        index2 = 0;
                        while (index2 < 4) {
                            ((CharacterFileRecord)value2).treeDiseaseChanceMultipliers[index2] = dataInputStream.readDouble();
                            ++index2;
                        }
                        index2 = 0;
                        while (index2 < 4) {
                            ((CharacterFileRecord)value2).treeSavedFlags[index2] = dataInputStream.readBoolean();
                            ++index2;
                        }
                        index2 = 0;
                        while (index2 < 4) {
                            ((CharacterFileRecord)value2).compostBinStates[index2] = dataInputStream.readInt();
                            ++index2;
                        }
                        index2 = 0;
                        while (index2 < 4) {
                            ((CharacterFileRecord)value2).compostBinLastUpdateTicks[index2] = dataInputStream.readLong();
                            ++index2;
                        }
                        index2 = 0;
                        while (index2 < 4) {
                            ((CharacterFileRecord)value2).compostBinItemIds[index2] = dataInputStream.readInt();
                            ++index2;
                        }
                        index2 = 0;
                        while (index2 < 18) {
                            ((CharacterFileRecord)value2).farmingToolStoreAmounts[index2] = dataInputStream.readInt();
                            ++index2;
                        }
                    }
                    catch (IOException iOException) {}
                    try {
                        int value25 = dataInputStream.readInt();
                        Object value26 = value2;
                        ((CharacterFileRecord)value2).slayerMasterId = value25;
                        String text3 = dataInputStream.readUTF();
                        value26 = value2;
                        ((CharacterFileRecord)value2).slayerTaskName = text3;
                        int value27 = dataInputStream.readInt();
                        value26 = value2;
                        ((CharacterFileRecord)value2).slayerTaskAmount = value27;
                    }
                    catch (IOException iOException) {}
                    try {
                        boolean enabled2 = dataInputStream.readBoolean();
                        Object value28 = value2;
                        ((CharacterFileRecord)value2).usingAncients = enabled2;
                    }
                    catch (IOException iOException) {}
                    try {
                        boolean enabled3 = dataInputStream.readBoolean();
                        Object value29 = value2;
                        ((CharacterFileRecord)value2).brimhavenOpen = enabled3;
                    }
                    catch (IOException iOException) {}
                    try {
                        boolean enabled4 = dataInputStream.readBoolean();
                        Object value30 = value2;
                        ((CharacterFileRecord)value2).killedClueAttacker = enabled4;
                    }
                    catch (IOException iOException) {}
                    index = 0;
                    while (index < musicUnlockConfigIds.length) {
                        try {
                            ((CharacterFileRecord)value2).configStates[CharacterFileManager.musicUnlockConfigIds[index]] = dataInputStream.readInt();
                        }
                        catch (IOException iOException) {}
                        ++index;
                    }
                    index = 0;
                    while (index < QuestDefinition.questStateCapacity) {
                        try {
                            ((CharacterFileRecord)value2).questProgress[index] = dataInputStream.readInt();
                        }
                        catch (IOException iOException) {}
                        ++index;
                    }
                    byte value31 = dataInputStream.readByte();
                    Object value32 = value2;
                    ((CharacterFileRecord)value2).gangAffiliation = value31;
                    value31 = dataInputStream.readByte();
                    value32 = value2;
                    ((CharacterFileRecord)value2).piratesTreasureBananaCrateCount = value31;
                    value32 = value2;
                    ((CharacterFileRecord)value2).treasureTrailNavigationTaught = dataInputStream.readBoolean();
                    value31 = dataInputStream.readByte();
                    value32 = value2;
                    ((CharacterFileRecord)value2).coalTruckAmount = value31;
                    value31 = dataInputStream.readByte();
                    value32 = value2;
                    ((CharacterFileRecord)value2).treasureTrailStepCount = value31;
                    value32 = value2;
                    ((CharacterFileRecord)value2).cluePuzzleSolved = dataInputStream.readBoolean();
                    if (s < 2) break readCharacterFileRecordControlExit1;
                    ((CharacterFileRecord)value2).skeletonSkinUnlocked = dataInputStream.readByte();
                    if (s >= 3) {
                        ((CharacterFileRecord)value2).petUnlockFlags = dataInputStream.readInt();
                    }
                    if (s >= 4) {
                        ((CharacterFileRecord)value2).barrowsDoorPuzzleSolved = dataInputStream.readBoolean();
                        ((CharacterFileRecord)value2).barrowsChestOpened = dataInputStream.readBoolean();
                        ((CharacterFileRecord)value2).configStates[452] = dataInputStream.readInt();
                    }
                    if (s >= 5) {
                        value31 = dataInputStream.readByte();
                        value32 = value2;
                        ((CharacterFileRecord)value2).flourMillHopperGrainCount = value31;
                        ((CharacterFileRecord)value2).configStates[FlourMillHandler.flourBinConfigId] = dataInputStream.readInt();
                    }
                    if (s >= 6) {
                        ((CharacterFileRecord)value2).questRandomSeed = dataInputStream.readInt();
                    }
                    if (s >= 7) {
                        index = 0;
                        while (index < QuestDefinition.questStateCapacity) {
                            try {
                                ((CharacterFileRecord)value2).questBitFlags[index] = dataInputStream.readInt();
                            }
                            catch (IOException iOException) {}
                            ++index;
                        }
                        index = 0;
                        while (index < 100) {
                            try {
                                ((CharacterFileRecord)value2).questHookStates[index] = dataInputStream.readInt();
                            }
                            catch (IOException iOException) {}
                            ++index;
                        }
                    }
                    if (s >= 8) {
                        ((CharacterFileRecord)value2).publicChatMode = dataInputStream.readByte();
                        ((CharacterFileRecord)value2).privateChatMode = dataInputStream.readByte();
                        ((CharacterFileRecord)value2).tradeMode = dataInputStream.readByte();
                    }
                    if (s >= 9) {
                        ((CharacterFileRecord)value2).reservedSaveInt1 = dataInputStream.readInt();
                    }
                    if (s >= 10) {
                        ((CharacterFileRecord)value2).reservedSaveLong1 = dataInputStream.readLong();
                        ((CharacterFileRecord)value2).reservedSaveInt2 = dataInputStream.readInt();
                        ((CharacterFileRecord)value2).reservedSaveInt3 = dataInputStream.readInt();
                    }
                    if (s >= 11) {
                        String text4 = dataInputStream.readUTF();
                        value32 = value2;
                        ((CharacterFileRecord)value2).reservedVersion11String = text4;
                    }
                    if (s >= 12) {
                        ((CharacterFileRecord)value2).familyCrestGauntletItemId = dataInputStream.readInt();
                    }
                    if (s >= 13) {
                        ((CharacterFileRecord)value2).mageArenaFlamesOfZamorakCastsRemaining = dataInputStream.readInt();
                        ((CharacterFileRecord)value2).mageArenaSaradominStrikeCastsRemaining = dataInputStream.readInt();
                        ((CharacterFileRecord)value2).mageArenaClawsOfGuthixCastsRemaining = dataInputStream.readInt();
                        ((CharacterFileRecord)value2).mageArenaProgressStage = dataInputStream.readByte();
                    }
                    if (s >= 14) {
                        index = 0;
                        while (index < 6) {
                            ((CharacterFileRecord)value2).grandExchangeSellOfferFlags[index] = dataInputStream.readBoolean();
                            ++index;
                        }
                        index = 0;
                        while (index < 6) {
                            ((CharacterFileRecord)value2).grandExchangeItemIds[index] = dataInputStream.readInt();
                            ++index;
                        }
                        index = 0;
                        while (index < 6) {
                            ((CharacterFileRecord)value2).grandExchangeQuantities[index] = dataInputStream.readInt();
                            ++index;
                        }
                        index = 0;
                        while (index < 6) {
                            ((CharacterFileRecord)value2).grandExchangeUnitPrices[index] = dataInputStream.readInt();
                            ++index;
                        }
                        index = 0;
                        while (index < 6) {
                            ((CharacterFileRecord)value2).grandExchangeCancelledFlags[index] = dataInputStream.readBoolean();
                            ++index;
                        }
                        index = 0;
                        while (index < 6) {
                            ((CharacterFileRecord)value2).grandExchangeCompletedQuantities[index] = dataInputStream.readInt();
                            ++index;
                        }
                        index = 0;
                        while (index < 6) {
                            ((CharacterFileRecord)value2).grandExchangeTotalPrices[index] = dataInputStream.readInt();
                            ++index;
                        }
                        index = 0;
                        while (index < 6) {
                            ((CharacterFileRecord)value2).grandExchangePrimaryCollectAmounts[index] = dataInputStream.readInt();
                            ++index;
                        }
                        index = 0;
                        while (index < 6) {
                            ((CharacterFileRecord)value2).grandExchangeSecondaryCollectAmounts[index] = dataInputStream.readInt();
                            ++index;
                        }
                        index = 0;
                        while (index < 6) {
                            ((CharacterFileRecord)value2).grandExchangeFinishMessagePending[index] = dataInputStream.readBoolean();
                            ++index;
                        }
                    }
                    if (s >= 15) {
                        ((CharacterFileRecord)value2).telekineticPizazzPoints = dataInputStream.readInt();
                        ((CharacterFileRecord)value2).enchantmentPizazzPoints = dataInputStream.readInt();
                        ((CharacterFileRecord)value2).alchemistPizazzPoints = dataInputStream.readInt();
                        ((CharacterFileRecord)value2).graveyardPizazzPoints = dataInputStream.readInt();
                        ((CharacterFileRecord)value2).bonesToPeachesUnlocked = dataInputStream.readBoolean();
                        ((CharacterFileRecord)value2).telekineticMazeIndex = dataInputStream.readByte();
                        ((CharacterFileRecord)value2).telekineticMazeSolved = dataInputStream.readBoolean();
                        ((CharacterFileRecord)value2).telekineticConsecutiveMazesSolved = dataInputStream.readByte();
                    }
                    if (s >= 16) {
                        ((CharacterFileRecord)value2).barrowsRewardPotential = dataInputStream.readInt();
                    }
                    if (s >= 18) {
                        ((CharacterFileRecord)value2).gameMode = dataInputStream.readByte();
                    }
                    if (s >= 19) {
                        ((CharacterFileRecord)value2).barrowsRunsCompleted = dataInputStream.readInt();
                    }
                    if (s >= 21) {
                        ((CharacterFileRecord)value2).godWarsLastAltarBlessingMillis = dataInputStream.readLong();
                        ((CharacterFileRecord)value2).configStates[LEGACY_GOD_WARS_ROPE_CONFIG_ID] = dataInputStream.readInt();
                        index = 0;
                        while (index < ((CharacterFileRecord)value2).godWarsKillCounts.length) {
                            ((CharacterFileRecord)value2).godWarsKillCounts[index] = dataInputStream.readInt();
                            ++index;
                        }
                    }
                    if (s >= 22) {
                        ((CharacterFileRecord)value2).craftingThreadUseCount = dataInputStream.readByte();
                    }
                    if (s >= 24) {
                        ((CharacterFileRecord)value2).membershipExpiresMillis = dataInputStream.readLong();
                        ((CharacterFileRecord)value2).reservedSaveByte = dataInputStream.readByte();
                    }
                    if (s >= 25) {
                        ((CharacterFileRecord)value2).configStates[33] = dataInputStream.readInt();
                    }
                    if (s >= 26) {
                        ((CharacterFileRecord)value2).savedCacheVersion = dataInputStream.readUnsignedShort();
                    }
                    if (s >= 29) {
                        ((CharacterFileRecord)value2).godBookPageFlags = dataInputStream.readInt();
                    }
                    if (s >= 30) {
                        ((CharacterFileRecord)value2).swampCaveRopeAttached = dataInputStream.readBoolean();
                        ((CharacterFileRecord)value2).lampOilStillFilled = dataInputStream.readBoolean();
                        ((CharacterFileRecord)value2).enterTheAbyssMiniquestState = dataInputStream.readInt();
                    }
                    if (s >= 31) {
                        ((CharacterFileRecord)value2).configStates[872] = dataInputStream.readInt();
                    }
                    if (s >= 20) {
                        boolean botEnabled = dataInputStream.readBoolean();
                        ((CharacterFileRecord)value2).botEnabled = botEnabled;
                        if (botEnabled) {
                            ((CharacterFileRecord)value2).botMode = dataInputStream.readByte();
                            ((CharacterFileRecord)value2).currentBotTaskTypeId = dataInputStream.readByte();
                            ((CharacterFileRecord)value2).currentBotTaskIndex = dataInputStream.readInt();
                            ((CharacterFileRecord)value2).deferredBotTaskTypeId = dataInputStream.readByte();
                            ((CharacterFileRecord)value2).deferredBotTaskIndex = dataInputStream.readInt();
                            ((CharacterFileRecord)value2).botTaskState = dataInputStream.readUTF();
                            byte value33 = dataInputStream.readByte();
                            value4 = value33;
                            if (value33 > 0) {
                                ((CharacterFileRecord)value2).botTaskRequiredItems = new ItemStack[value4];
                                int index3 = 0;
                                while (index3 < value4) {
                                    value5 = dataInputStream.readInt();
                                    value3 = dataInputStream.readInt();
                                    ((CharacterFileRecord)value2).botTaskRequiredItems[index3] = new ItemStack(value5, value3);
                                    ++index3;
                                }
                            }
                            ((CharacterFileRecord)value2).botFoodItemId = dataInputStream.readInt();
                            ((CharacterFileRecord)value2).botPathSegmentIndex = dataInputStream.readByte();
                            ((CharacterFileRecord)value2).botPathWaypointIndex = dataInputStream.readByte();
                            ((CharacterFileRecord)value2).savedWorldRouteReversed = dataInputStream.readBoolean();
                            ((CharacterFileRecord)value2).botTaskSavedElapsedMillis = dataInputStream.readLong();
                            ((CharacterFileRecord)value2).botTaskDurationMinutes = dataInputStream.readByte();
                            ((CharacterFileRecord)value2).savedWorldRouteIndex = dataInputStream.readByte();
                            ((CharacterFileRecord)value2).tradeAdvertMode = dataInputStream.readByte();
                            ((CharacterFileRecord)value2).botAdvertItemId = dataInputStream.readInt();
                            ((CharacterFileRecord)value2).tradeAdvertQuantityRemaining = dataInputStream.readInt();
                            ((CharacterFileRecord)value2).tradeAdvertUnitPrice = dataInputStream.readInt();
                            ((CharacterFileRecord)value2).tradeAdvertScam = dataInputStream.readBoolean();
                            ((CharacterFileRecord)value2).tradeAdvertVariableQuantity = dataInputStream.readBoolean();
                            ((CharacterFileRecord)value2).tradeAdvertLastOfferAmount = dataInputStream.readInt();
                            ((CharacterFileRecord)value2).botShopBuyMode = dataInputStream.readByte();
                            ((CharacterFileRecord)value2).botTaskItemId = dataInputStream.readInt();
                            ((CharacterFileRecord)value2).botShopItemAmount = dataInputStream.readInt();
                            byte value34 = dataInputStream.readByte();
                            value5 = 0;
                            while (value5 < value34) {
                                value3 = dataInputStream.readInt();
                                ((CharacterFileRecord)value2).botShopSellItemIds.add(value3);
                                ++value5;
                            }
                            value5 = dataInputStream.readByte();
                            value3 = 0;
                            while (value3 < value5) {
                                int value35 = dataInputStream.readInt();
                                ((CharacterFileRecord)value2).botCombatLoadoutItemIds.add(value35);
                                ++value3;
                            }
                            ((CharacterFileRecord)value2).botCombatStyle = dataInputStream.readByte();
                            ((CharacterFileRecord)value2).botSkillTargetSkillId = dataInputStream.readByte();
                            ((CharacterFileRecord)value2).botSkillTargetLevel = dataInputStream.readByte();
                            ((CharacterFileRecord)value2).botReservedGoalByte1 = dataInputStream.readByte();
                            ((CharacterFileRecord)value2).botReservedGoalByte2 = dataInputStream.readByte();
                            ((CharacterFileRecord)value2).botReservedGoalByte3 = dataInputStream.readByte();
                            ((CharacterFileRecord)value2).botReservedGoalByte4 = dataInputStream.readByte();
                            ((CharacterFileRecord)value2).botCompletionItemId = dataInputStream.readInt();
                            ((CharacterFileRecord)value2).botCompletionItemAmount = dataInputStream.readInt();
                            ((CharacterFileRecord)value2).botSecondaryCompletionItemId = dataInputStream.readInt();
                            ((CharacterFileRecord)value2).botReservedGoalInt2 = dataInputStream.readInt();
                            ((CharacterFileRecord)value2).botReservedGoalInt3 = dataInputStream.readInt();
                            ((CharacterFileRecord)value2).botReservedGoalInt4 = dataInputStream.readInt();
                            ((CharacterFileRecord)value2).botTaskReturnToBankRequested = dataInputStream.readBoolean();
                            dataInputStream.readBoolean();
                            dataInputStream.readBoolean();
                            dataInputStream.readBoolean();
                            dataInputStream.readBoolean();
                            dataInputStream.readBoolean();
                            ((CharacterFileRecord)value2).botElementalSpellIndex = dataInputStream.readByte();
                            dataInputStream.readByte();
                            dataInputStream.readByte();
                            dataInputStream.readByte();
                            dataInputStream.readByte();
                            dataInputStream.readInt();
                            dataInputStream.readInt();
                            dataInputStream.readInt();
                            dataInputStream.readInt();
                            dataInputStream.readInt();
                            dataInputStream.readUTF();
                            dataInputStream.readUTF();
                            dataInputStream.readUTF();
                            dataInputStream.readUTF();
                            dataInputStream.readUTF();
                        }
                    }
                }
                catch (Exception exception) {
                    dataInputStream.close();
                    ((FileInputStream)value).close();
                    return null;
                }
            }
            // Older character files end here and have no home teleport cooldown.
            if (dataInputStream.available() >= 8) {
                ((CharacterFileRecord)value2).homeTeleportAvailableAtMillis = dataInputStream.readLong();
            }
            // Files written before music-mode persistence default to Auto.
            if (dataInputStream.available() >= 1) {
                ((CharacterFileRecord)value2).automaticMusicEnabled = dataInputStream.readBoolean();
            }
            // Files written before late music unlocks were saved relock those tracks.
            if (dataInputStream.available() >= 4) {
                ((CharacterFileRecord)value2).configStates[LATE_MUSIC_UNLOCK_CONFIG_ID] = dataInputStream.readInt();
            }
            dataInputStream.close();
            ((FileInputStream)value).close();
            ((CharacterFileRecord)value2).getStoredItemValue();
            return (CharacterFileRecord)value2;
        }
        catch (IOException iOException) {
            return null;
        }
    }

    public static void loadPlayerFromFile(String path, Player player) {
        File file = new File(String.valueOf(path) + player.getUsername() + ".dat");
        File jsonFile = new File(String.valueOf(path) + player.getUsername() + ".json");
        if (!file.exists() && !jsonFile.exists()) {
            if (Server.getInstance() != null) {
                Server.getInstance().queueLogin(player);
            }
            return;
        }
        if (!CharacterFileManager.validateCharacterFile(path, player.getUsername())
                && path.equals("./data/characters/")) {
            CharacterFileManager.restorePlayerFromBackup(player);
            if (player.loadedCharacterFromBackup) {
                return;
            }
            if (player.isBot) {
                File corruptDirectory = new File("./data/corrupt-bot-characters/");
                if (!corruptDirectory.exists()) {
                    corruptDirectory.mkdirs();
                }
                File quarantinedFile = new File(
                    corruptDirectory,
                    player.getUsername() + "." + System.currentTimeMillis() + ".dat"
                );
                try {
                    Files.move(file.toPath(), quarantinedFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                }
                catch (IOException ioException) {
                    file.delete();
                }
                if (Server.getInstance() != null) {
                    Server.getInstance().queueLogin(player);
                }
            }
            return;
        }
        CharacterFileRecord record = CharacterFileManager.readCharacterFileRecord(path, player.getUsername(), true);
        if (record == null) {
            System.out.println("Account not loading: " + player);
            return;
        }
        // Existing .dat files migrate on login too (including if startup skipped one).
        // Conversion is performed only after a successful legacy parse.
        if (!jsonFile.exists() && file.exists() && path.equals("./data/characters/")) {
            try {
                CharacterJsonCodec.save(jsonFile, record);
                Files.move(file.toPath(),
                        new File(path + player.getUsername() + ".dat.bak").toPath(),
                        StandardCopyOption.REPLACE_EXISTING);
                System.out.println("[characters] Migrated " + player.getUsername() + " to JSON");
            } catch (IOException exception) {
                System.err.println("[characters] Migration failed for " + player.getUsername()
                        + "; original .dat retained: " + exception);
            }
        }
        CharacterFileManager.applyCharacterFileRecordToPlayer(record, player);
        AppearancePacketHandler.validateAppearance(player);
        if (!path.equals("./data/characters/")) {
            player.loadedCharacterFromBackup = true;
            GameplayHelper.appendLogLine(String.valueOf(System.currentTimeMillis()) + "\u00a7" + player.getUsername() + "\u00a7" + player.lastSavedMillis, "restored");
        }
        if (Server.getInstance() != null) {
            Server.getInstance().queueLogin(player);
        }
    }

    // Usernames in data/admins.txt (one per line, # for comments) log in with admin rights.
    private static boolean isListedAdmin(String username) {
        if (username == null) return false;
        try {
            for (String line : Files.readAllLines(new File("./data/admins.txt").toPath())) {
                line = line.trim();
                if (!line.isEmpty() && !line.startsWith("#") && line.equalsIgnoreCase(username)) return true;
            }
        } catch (IOException exception) {
            // No admin list: keep the saved rights.
        }
        return false;
    }

    private static void applyCharacterFileRecordToPlayer(CharacterFileRecord record, Player player) {
        player.setUsername(record.username);
        player.setPassword(record.password);
        player.lastLoginHostAddress = record.hostAddress;
        player.setPlayerRights(CharacterFileManager.isListedAdmin(record.username) ? 2 : record.playerRights);
        player.setProfileString1(record.profileString1);
        player.setProfileString2(record.profileString2);
        player.lastSavedMillis = record.lastSavedMillis;
        player.homeTeleportAvailableAtMillis = record.homeTeleportAvailableAtMillis;
        player.automaticMusicEnabled = record.automaticMusicEnabled;
        player.configStates[LATE_MUSIC_UNLOCK_CONFIG_ID] = record.configStates[LATE_MUSIC_UNLOCK_CONFIG_ID];
        player.totalPlaytimeMillis = record.totalPlayTimeMillis;
        player.createdAtMillis = record.createdAtMillis;
        player.loginRestrictionExempt = record.loginRestrictionExempt;
        player.setMemberFlag(record.memberFlag);
        player.setDonatorPoints(record.donatorPoints);
        player.getPosition().setX(record.x);
        player.getPosition().setPreviousX(record.x);
        player.getPosition().setY(record.y);
        player.getPosition().setPreviousY(record.y + 1);
        player.getPosition().setPlane(record.plane);
        player.setGender(record.gender);
        player.npcKillCount = record.npcKillCount;
        player.playerKillCount = record.playerKillCount;
        player.deathCount = record.deathCount;
        player.easyCluesCompleted = record.easyCluesCompleted;
        player.mediumCluesCompleted = record.mediumCluesCompleted;
        player.hardCluesCompleted = record.hardCluesCompleted;
        player.soldItemsValue = record.soldItemsValue;
        player.boughtItemsValue = record.boughtItemsValue;
        player.duelWins = record.duelWins;
        player.duelLosses = record.duelLosses;
        player.legacyQuestPoints = record.legacyQuestPoints;
        player.setAutoRetaliate(record.autoRetaliate);
        player.setFightMode(record.fightMode);
        player.setBrightness(record.brightness);
        player.setMouseButtons(record.mouseButtons);
        player.setPublicChatEffects(record.publicChatEffects);
        player.setSplitPrivateChat(record.splitPrivateChat);
        player.setAcceptAid(record.acceptAid);
        player.setMusicVolume(record.musicVolume);
        player.setEffectVolume(record.effectVolume);
        player.configStates[872] = record.configStates[872];
        player.setSpecialEnergy(record.specialEnergy);
        player.getBankPinManager().setChangingPin(record.changingBankPin);
        player.getBankPinManager().setDeletingPin(record.deletingBankPin);
        player.getBankPinManager().setPinAppendYear(record.pinAppendYear);
        player.getBankPinManager().setPinAppendDate(record.pinAppendDate);
        player.setBindingNecklaceCharge(record.bindingNecklaceCharge);
        player.setRingOfForgingLife(record.ringOfForgingLife);
        player.setRingOfRecoilLife(record.ringOfRecoilLife);
        if (record.skullTimer > 0) {
            player.addPvpCombatReference(player, record.skullTimer);
        }
        player.setRunEnergyRaw(record.runEnergyRaw);
        player.getMovementQueue().setRunning(record.running);
        CharacterFileManager.copy(record.currentPin, player.getBankPinManager().getCurrentPin());
        CharacterFileManager.copy(record.pendingPin, player.getBankPinManager().getPendingPin());
        int index = 0;
        while (index < record.essencePouchAmounts.length) {
            player.setEssencePouchAmount(index, record.essencePouchAmounts[index]);
            ++index;
        }
        CharacterFileManager.copy(record.appearanceParts, player.getAppearanceParts());
        CharacterFileManager.copy(record.appearanceColors, player.getAppearanceColors());
        CharacterFileManager.copy(record.currentLevels, player.getSkillManager().getCurrentLevels());
        CharacterFileManager.copyLongToDouble(record.skillExperience, player.getSkillManager().getExperience());
        index = 0;
        while (index < record.inventoryItems.length) {
            ItemStack itemStack = CharacterFileManager.normalizeLoadedItem(record.inventoryItems[index], true);
            if (itemStack != null) {
                player.getInventoryManager().getContainer().setItem(index, itemStack);
            }
            ++index;
        }
        index = 0;
        while (index < record.equipmentItems.length) {
            ItemStack itemStack = CharacterFileManager.normalizeLoadedItem(record.equipmentItems[index], false);
            if (itemStack != null) {
                player.getEquipmentManager().getContainer().setItem(index, itemStack);
            }
            ++index;
        }
        CharacterFileManager.applyBankTabs(record, player);
        CharacterFileManager.copy(record.friendsList, player.getFriendsList());
        CharacterFileManager.copy(record.ignoreList, player.getIgnoreList());
        CharacterFileManager.copy(record.queuedLoginItemIds, player.getQueuedLoginItemIds());
        CharacterFileManager.copy(record.queuedLoginItemAmounts, player.getQueuedLoginItemAmounts());
        player.setAbyssMageNpcId(record.abyssMageNpcId);
        player.setMuteExpires(record.muteExpires);
        player.setBanExpires(record.banExpires);
        index = 0;
        while (index < record.barrowsKilledBrothers.length) {
            player.setBarrowsBrotherKilled(index, record.barrowsKilledBrothers[index]);
            ++index;
        }
        player.setBarrowsKillCount(record.barrowsKillCount);
        player.setBarrowsTargetBrotherIndex(record.barrowsTargetBrotherIndex);
        player.getPoisonImmunityTimer().setDelayTicks(record.poisonImmunityTicks);
        player.getPoisonImmunityTimer().reset();
        player.getAntifireTimer().setDelayTicks(record.antifireTicks);
        player.getAntifireTimer().reset();
        player.getTeleblockTimer().setDelayTicks(record.teleblockTicks);
        player.getTeleblockTimer().reset();
        player.setPoisonDamage(record.poisonDamage);
        CharacterFileManager.applyFarmingRecord(record, player);
        player.getSlayerManager().slayerMasterId = record.slayerMasterId;
        player.getSlayerManager().slayerTaskName = record.slayerTaskName;
        player.getSlayerManager().taskAmount = record.slayerTaskAmount;
        player.setSpellbook(record.usingAncients ? Spellbook.ANCIENT : Spellbook.MODERN);
        player.setBrimhavenOpen(record.brimhavenOpen);
        player.killedClueAttacker = record.killedClueAttacker;
        index = 0;
        while (index < CharacterFileManager.musicUnlockConfigIds.length) {
            player.configStates[CharacterFileManager.musicUnlockConfigIds[index]] = record.configStates[CharacterFileManager.musicUnlockConfigIds[index]];
            ++index;
        }
        index = 0;
        while (index < QuestDefinition.questStateCapacity) {
            player.setQuestState(index, record.questProgress[index]);
            ++index;
        }
        player.gangAffiliation = (byte)record.gangAffiliation;
        player.piratesTreasureBananaCrateCount = (byte)record.piratesTreasureBananaCrateCount;
        player.treasureTrailNavigationTaught = record.treasureTrailNavigationTaught;
        player.setCoalTruckCoalCount(record.coalTruckAmount);
        player.treasureTrailStepCount = (byte)record.treasureTrailStepCount;
        player.cluePuzzleSolved = record.cluePuzzleSolved;
        player.skeletonSkinUnlocked = (byte)record.skeletonSkinUnlocked;
        player.setBossPetUnlockFlags(record.petUnlockFlags);
        player.barrowsDoorPuzzleSolved = record.barrowsDoorPuzzleSolved;
        player.barrowsChestOpened = record.barrowsChestOpened;
        player.configStates[452] = record.configStates[452];
        player.flourMillHopperGrainCount = (byte)record.flourMillHopperGrainCount;
        player.configStates[FlourMillHandler.flourBinConfigId] = record.configStates[FlourMillHandler.flourBinConfigId];
        player.questRandomSeed = record.questRandomSeed;
        CharacterFileManager.copy(record.questBitFlags, player.questProgressFlags);
        CharacterFileManager.copy(record.questHookStates, player.questHookStates);
        player.setPublicChatMode(record.publicChatMode);
        player.setPrivateChatMode(record.privateChatMode);
        player.setTradeMode(record.tradeMode);
        player.reservedSaveInt1 = record.reservedSaveInt1;
        player.reservedSaveLong1 = record.reservedSaveLong1;
        player.reservedSaveInt2 = record.reservedSaveInt2;
        player.reservedSaveInt3 = record.reservedSaveInt3;
        player.familyCrestGauntletItemId = record.familyCrestGauntletItemId;
        player.mageArenaFlamesOfZamorakCastsRemaining = record.mageArenaFlamesOfZamorakCastsRemaining;
        player.mageArenaSaradominStrikeCastsRemaining = record.mageArenaSaradominStrikeCastsRemaining;
        player.mageArenaClawsOfGuthixCastsRemaining = record.mageArenaClawsOfGuthixCastsRemaining;
        player.mageArenaProgressStage = (byte)record.mageArenaProgressStage;
        CharacterFileManager.copy(record.grandExchangeSellOfferFlags, player.grandExchangeSellOfferFlags);
        CharacterFileManager.copy(record.grandExchangeItemIds, player.grandExchangeItemIds);
        CharacterFileManager.copy(record.grandExchangeQuantities, player.grandExchangeQuantities);
        CharacterFileManager.copy(record.grandExchangeUnitPrices, player.grandExchangeUnitPrices);
        CharacterFileManager.copy(record.grandExchangeCancelledFlags, player.grandExchangeCancelledFlags);
        CharacterFileManager.copy(record.grandExchangeCompletedQuantities, player.grandExchangeCompletedQuantities);
        CharacterFileManager.copy(record.grandExchangeTotalPrices, player.grandExchangeTotalPrices);
        CharacterFileManager.copy(record.grandExchangePrimaryCollectAmounts, player.grandExchangePrimaryCollectAmounts);
        CharacterFileManager.copy(record.grandExchangeSecondaryCollectAmounts, player.grandExchangeSecondaryCollectAmounts);
        CharacterFileManager.copy(record.grandExchangeFinishMessagePending, player.grandExchangeFinishMessagePending);
        player.getTelekineticTheatreController().pizazzPoints = record.telekineticPizazzPoints;
        player.getEnchantmentChamberController().pizazzPoints = record.enchantmentPizazzPoints;
        player.getAlchemistPlaygroundController().pizazzPoints = record.alchemistPizazzPoints;
        player.getCreatureGraveyardController().pizazzPoints = record.graveyardPizazzPoints;
        player.bonesToPeachesUnlocked = record.bonesToPeachesUnlocked;
        player.getTelekineticTheatreController().mazeIndex = (byte)record.telekineticMazeIndex;
        player.getTelekineticTheatreController().mazeSolved = record.telekineticMazeSolved;
        player.getTelekineticTheatreController().consecutiveMazesSolved = (byte)record.telekineticConsecutiveMazesSolved;
        player.barrowsRewardPotential = record.barrowsRewardPotential;
        player.gameMode = (byte)record.gameMode;
        if (player.gameMode < 0 || player.gameMode > 3) {
            System.err.println("Invalid saved game mode " + player.gameMode + " for " + player.getUsername() + "; resetting to normal mode (0).");
            player.gameMode = 0;
        }
        player.barrowsRunsCompleted = record.barrowsRunsCompleted;
        player.godWarsLastAltarBlessingMillis = record.godWarsLastAltarBlessingMillis;
        player.configStates[LEGACY_GOD_WARS_ROPE_CONFIG_ID] = record.configStates[LEGACY_GOD_WARS_ROPE_CONFIG_ID];
        CharacterFileManager.copy(record.godWarsKillCounts, player.godWarsKillCounts);
        player.craftingThreadUseCount = record.craftingThreadUseCount;
        player.membershipExpiresMillis = record.membershipExpiresMillis;
        player.reservedSaveByte = (byte)record.reservedSaveByte;
        player.configStates[33] = record.configStates[33];
        player.savedCacheVersion = record.savedCacheVersion;
        if (record.savedCacheVersion > 0) {
            CacheCoordinateTranslator.translateSavedDungeonPosition(player);
        }
        player.godBookPageFlags = record.godBookPageFlags;
        player.swampCaveRopeAttached = record.swampCaveRopeAttached;
        player.lampOilStillFilled = record.lampOilStillFilled;
        player.enterTheAbyssMiniquestState = record.enterTheAbyssMiniquestState;
        CharacterFileManager.applyBotRecord(record, player);
    }

    private static ItemStack normalizeLoadedItem(ItemStack itemStack, boolean translateOldTreasureTrailIds) {
        if (itemStack == null || itemStack.getId() >= 11883 || itemStack.getAmount() <= 0) {
            return null;
        }
        if (translateOldTreasureTrailIds && (itemStack.getId() == 2696 || itemStack.getId() == 2699 || itemStack.getId() == 3510)) {
            return new ItemStack(itemStack.getId() - 1, itemStack.getAmount(), itemStack.getMetadata());
        }
        return itemStack;
    }

    private static void applyBankTabs(CharacterFileRecord record, Player player) {
        int tabIndex = 0;
        while (tabIndex < record.bankTabs.size()) {
            CharacterFileBankTab bankTab = (CharacterFileBankTab)record.bankTabs.get(tabIndex);
            ArrayList items = bankTab.getItems();
            int slot = 0;
            while (slot < items.size()) {
                ItemStack itemStack = CharacterFileManager.normalizeLoadedItem((ItemStack)items.get(slot), true);
                if (itemStack != null) {
                    player.getBankContainer().setTabItem(slot, itemStack, tabIndex);
                }
                ++slot;
            }
            ++tabIndex;
        }
    }

    private static void applyFarmingRecord(CharacterFileRecord record, Player player) {
        AllotmentPatchManager allotment = player.getAllotmentPatchManager();
        CharacterFileManager.copy(record.allotmentGrowthStages, allotment.growthStages);
        CharacterFileManager.copy(record.allotmentCropIds, allotment.cropIds);
        CharacterFileManager.copy(record.allotmentHarvestAmounts, allotment.harvestAmounts);
        CharacterFileManager.copy(record.allotmentPatchStates, allotment.patchStates);
        CharacterFileManager.copy(record.allotmentLastUpdateTicks, allotment.lastUpdateTicks);
        CharacterFileManager.copy(record.allotmentDiseaseChanceMultipliers, allotment.diseaseChanceMultipliers);
        CharacterFileManager.copy(record.allotmentProtectionFlags, allotment.protectionFlags);
        BushPatchManager bush = player.getBushPatchManager();
        CharacterFileManager.copy(record.bushGrowthStages, bush.growthStages);
        CharacterFileManager.copy(record.bushCropIds, bush.cropIds);
        CharacterFileManager.copy(record.bushPatchStates, bush.patchStates);
        CharacterFileManager.copy(record.bushLastUpdateTicks, bush.lastUpdateTicks);
        CharacterFileManager.copy(record.bushDiseaseChanceMultipliers, bush.diseaseChanceMultipliers);
        CharacterFileManager.copy(record.bushSavedFlags, bush.protectionFlags);
        FlowerPatchManager flower = player.getFlowerPatchManager();
        CharacterFileManager.copy(record.flowerGrowthStages, flower.growthStages);
        CharacterFileManager.copy(record.flowerCropIds, flower.cropIds);
        CharacterFileManager.copy(record.flowerPatchStates, flower.patchStates);
        CharacterFileManager.copy(record.flowerLastUpdateTicks, flower.lastUpdateTicks);
        CharacterFileManager.copy(record.flowerDiseaseChanceMultipliers, flower.diseaseChanceMultipliers);
        FruitTreePatchManager fruitTree = player.getFruitTreePatchManager();
        CharacterFileManager.copy(record.fruitTreeGrowthStages, fruitTree.growthStages);
        CharacterFileManager.copy(record.fruitTreeIds, fruitTree.treeIds);
        CharacterFileManager.copy(record.fruitTreePatchStates, fruitTree.patchStates);
        CharacterFileManager.copy(record.fruitTreeLastUpdateTicks, fruitTree.lastUpdateTicks);
        CharacterFileManager.copy(record.fruitTreeDiseaseChanceMultipliers, fruitTree.diseaseChanceMultipliers);
        CharacterFileManager.copy(record.fruitTreeSavedFlags, fruitTree.protectionFlags);
        HerbPatchManager herb = player.getHerbPatchManager();
        CharacterFileManager.copy(record.herbGrowthStages, herb.growthStages);
        CharacterFileManager.copy(record.herbCropIds, herb.cropIds);
        CharacterFileManager.copy(record.herbHarvestAmounts, herb.harvestAmounts);
        CharacterFileManager.copy(record.herbPatchStates, herb.patchStates);
        CharacterFileManager.copy(record.herbLastUpdateTicks, herb.lastUpdateTicks);
        CharacterFileManager.copy(record.herbDiseaseChanceMultipliers, herb.diseaseChanceMultipliers);
        HopsPatchManager hops = player.getHopsPatchManager();
        CharacterFileManager.copy(record.hopsGrowthStages, hops.growthStages);
        CharacterFileManager.copy(record.hopsCropIds, hops.cropIds);
        CharacterFileManager.copy(record.hopsHarvestAmounts, hops.harvestAmounts);
        CharacterFileManager.copy(record.hopsPatchStates, hops.patchStates);
        CharacterFileManager.copy(record.hopsLastUpdateTicks, hops.lastUpdateTicks);
        CharacterFileManager.copy(record.hopsDiseaseChanceMultipliers, hops.diseaseChanceMultipliers);
        CharacterFileManager.copy(record.hopsProtectionFlags, hops.protectionFlags);
        SpecialTreePatchManager specialTree = player.getSpecialTreePatchManager();
        CharacterFileManager.copy(record.specialTreeGrowthStages, specialTree.growthStages);
        CharacterFileManager.copy(record.specialTreeIds, specialTree.treeIds);
        CharacterFileManager.copy(record.specialTreePatchStates, specialTree.patchStates);
        CharacterFileManager.copy(record.specialTreeLastUpdateTicks, specialTree.lastUpdateTicks);
        CharacterFileManager.copy(record.specialTreeDiseaseChanceMultipliers, specialTree.diseaseChanceMultipliers);
        SpecialCropPatchManager specialCrop = player.getSpecialCropPatchManager();
        CharacterFileManager.copy(record.specialCropGrowthStages, specialCrop.growthStages);
        CharacterFileManager.copy(record.specialCropIds, specialCrop.cropIds);
        CharacterFileManager.copy(record.specialCropPatchStates, specialCrop.patchStates);
        CharacterFileManager.copy(record.specialCropLastUpdateTicks, specialCrop.lastUpdateTicks);
        CharacterFileManager.copy(record.specialCropDiseaseChanceMultipliers, specialCrop.diseaseChanceMultipliers);
        TreePatchManager tree = player.getTreePatchManager();
        CharacterFileManager.copy(record.treeGrowthStages, tree.growthStages);
        CharacterFileManager.copy(record.treeIds, tree.treeIds);
        CharacterFileManager.copy(record.treePatchData, tree.patchData);
        CharacterFileManager.copy(record.treePatchStates, tree.patchStates);
        CharacterFileManager.copy(record.treeLastUpdateTicks, tree.lastUpdateTicks);
        CharacterFileManager.copy(record.treeDiseaseChanceMultipliers, tree.diseaseChanceMultipliers);
        CharacterFileManager.copy(record.treeSavedFlags, tree.protectionFlags);
        CompostBinManager compost = player.getCompostBinManager();
        CharacterFileManager.copy(record.compostBinStates, compost.states);
        CharacterFileManager.copy(record.compostBinLastUpdateTicks, compost.lastUpdateTicks);
        CharacterFileManager.copy(record.compostBinItemIds, compost.itemIds);
        FarmingToolStore toolStore = player.getFarmingToolStore();
        CharacterFileManager.copy(record.farmingToolStoreAmounts, toolStore.storedAmounts);
    }

    private static void applyBotRecord(CharacterFileRecord record, Player player) {
        if (!record.botEnabled) {
            return;
        }
        // Preserve the live object flags selected by server startup. The original loader
        // consumes the saved bot mode byte but does not apply it during login.
        player.currentBotTaskTypeId = record.currentBotTaskTypeId;
        player.currentBotTaskIndex = record.currentBotTaskIndex;
        player.deferredBotTaskTypeId = record.deferredBotTaskTypeId;
        player.deferredBotTaskIndex = record.deferredBotTaskIndex;
        player.botTaskState = record.botTaskState;
        player.botTaskRequiredItems = record.botTaskRequiredItems;
        player.botFoodItemId = record.botFoodItemId;
        player.botPathSegmentIndex = record.botPathSegmentIndex;
        player.botPathWaypointIndex = record.botPathWaypointIndex;
        player.savedWorldRouteReversed = record.savedWorldRouteReversed;
        player.botTaskSavedElapsedMillis = record.botTaskSavedElapsedMillis;
        player.botTaskDurationMinutes = record.botTaskDurationMinutes;
        player.savedWorldRouteIndex = record.savedWorldRouteIndex;
        player.tradeAdvertMode = record.tradeAdvertMode;
        player.botAdvertItemId = record.botAdvertItemId;
        player.tradeAdvertQuantityRemaining = record.tradeAdvertQuantityRemaining;
        player.tradeAdvertUnitPrice = record.tradeAdvertUnitPrice;
        player.tradeAdvertScam = record.tradeAdvertScam;
        player.tradeAdvertVariableQuantity = record.tradeAdvertVariableQuantity;
        player.tradeAdvertLastOfferAmount = record.tradeAdvertLastOfferAmount;
        player.botShopBuyMode = record.botShopBuyMode;
        player.botTaskItemId = record.botTaskItemId;
        player.botShopItemAmount = record.botShopItemAmount;
        CharacterFileManager.copyList(record.botShopSellItemIds, player.botShopSellItemIds);
        CharacterFileManager.copyList(record.botCombatLoadoutItemIds, player.botCombatLoadoutItemIds);
        player.botCombatStyle = record.botCombatStyle;
        player.botSkillTargetSkillId = record.botSkillTargetSkillId;
        player.botSkillTargetLevel = record.botSkillTargetLevel;
        player.botReservedGoalByte1 = record.botReservedGoalByte1;
        player.botReservedGoalByte2 = record.botReservedGoalByte2;
        player.botReservedGoalByte3 = record.botReservedGoalByte3;
        player.botReservedGoalByte4 = record.botReservedGoalByte4;
        player.botCompletionItemId = record.botCompletionItemId;
        player.botCompletionItemAmount = record.botCompletionItemAmount;
        player.botSecondaryCompletionItemId = record.botSecondaryCompletionItemId;
        player.botReservedGoalInt2 = record.botReservedGoalInt2;
        player.botReservedGoalInt3 = record.botReservedGoalInt3;
        player.botReservedGoalInt4 = record.botReservedGoalInt4;
        player.botTaskReturnToBankRequested = record.botTaskReturnToBankRequested;
        player.botElementalSpellIndex = (byte)record.botElementalSpellIndex;
    }

    private static void copy(int[] source, int[] target) {
        int index = 0;
        while (index < source.length && index < target.length) {
            target[index] = source[index];
            ++index;
        }
    }

    private static void copy(long[] source, long[] target) {
        int index = 0;
        while (index < source.length && index < target.length) {
            target[index] = source[index];
            ++index;
        }
    }

    private static void copy(double[] source, double[] target) {
        int index = 0;
        while (index < source.length && index < target.length) {
            target[index] = source[index];
            ++index;
        }
    }

    private static void copy(boolean[] source, boolean[] target) {
        int index = 0;
        while (index < source.length && index < target.length) {
            target[index] = source[index];
            ++index;
        }
    }

    private static void copyLongToInt(long[] source, int[] target) {
        int index = 0;
        while (index < source.length && index < target.length) {
            target[index] = (int)source[index];
            ++index;
        }
    }

    private static void copyLongToDouble(long[] source, double[] target) {
        int index = 0;
        while (index < source.length && index < target.length) {
            target[index] = source[index];
            ++index;
        }
    }

    private static void copyList(ArrayList source, ArrayList target) {
        target.clear();
        if (source != null) {
            target.addAll(source);
        }
    }


}
