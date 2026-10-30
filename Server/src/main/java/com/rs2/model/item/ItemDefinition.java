package com.rs2.model.item;

import com.rs2.ServerSettings;
import com.rs2.model.HistoricalExamines;
import com.rs2.cache.CacheArchive;
import com.rs2.cache.CacheStore;
import com.rs2.cache.js5.ConfigReader;
import com.rs2.cache.js5.Definitions;
import com.rs2.model.quest.QuestDefinition;
import com.rs2.model.skill.runecrafting.RunecraftingHandler;
import com.rs2.util.ByteArrayReader;
import com.rs2.util.FileUtil;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Map;
import java.util.logging.Logger;

public class ItemDefinition {
    private static final int DRAGON_CLAWS_ID = 14484;
    public ArrayList grandExchangePriceSamples = new ArrayList();
    private static ItemDefinition[] definitionsById;
    private static int definitionCount;
    private static int customDefinitionCount;
    private boolean destroyOption;
    private String[] groundActions = new String[] {null, null, "Take", null, null};
    private String[] inventoryActions = new String[] {null, null, null, null, "Drop"};
    private int id;
    private String name;
    private String description;
    private boolean note;
    private boolean hasNote;
    private boolean stackable;
    private int unnotedId;
    private int notedId;
    private boolean membersOnly;
    private int tokkulValue;
    private int highAlchemyValue;
    private int lowAlchemyValue;
    private int donatorPointValue;
    private final int[] bonuses;
    private final int[] requiredLevels;
    private final boolean[] requiredQuests;
    private int equipmentSlot;
    private int equipmentAppearanceType;
    private int requiredQuestPoints;
    public int secondaryCurrencyItemId;
    private double weight;
    private boolean twoHanded;
    private int shopValue;
    private boolean untradeable;

    static {
        Logger.getLogger(ItemDefinition.class.getName());
        // Keep room for extended-revision items used by the client.
        definitionsById = new ItemDefinition[20000];
        definitionCount = 0;
    }

    public static ItemDefinition forId(int value2) {
        ItemDefinition itemDefinition;
        if (value2 < 0 || value2 >= definitionsById.length) {
            value2 = 1;
        }
        if (value2 == DRAGON_CLAWS_ID && definitionsById[value2] == null) {
            definitionsById[value2] = createDragonClawsDefinition();
        }
        if ((itemDefinition = definitionsById[value2]) == null) {
            itemDefinition = new ItemDefinition(value2, "# + id", "It's an item!", "NONE", false, false, false, -1, -1, true, 0, 0, 0, 0, new int[14], 0, new int[25], 0, new boolean[QuestDefinition.questCount], 0.0, 0, 0, false);
        }
        return itemDefinition;
    }

    private static ItemDefinition createDragonClawsDefinition() {
        int[] bonuses = new int[]{
                41, 57, -4, 0, 0,
                13, 26, 7, 0, 0,
                56, 0, 0, 0
        };
        int[] requiredLevels = new int[25];
        requiredLevels[0] = 60;

        ItemDefinition definition = new ItemDefinition(
                DRAGON_CLAWS_ID,
                "Dragon claws",
                "A set of fighting claws.",
                "WEAPON",
                false, false, false,
                -1, -1, true,
                0, 0, 0, 0,
                bonuses,
                0,
                requiredLevels,
                0,
                new boolean[QuestDefinition.questCount],
                0.0,
                0,
                0,
                false);
        definition.membersOnly = true;
        definition.twoHanded = true;
        definition.shopValue = 67500;
        definition.highAlchemyValue = 40500;
        definition.lowAlchemyValue = 27000;
        definition.untradeable = false;
        return definition;
    }

    public static void loadDefinitions() {
        int value;
        Object value2;
        ByteArrayReader byteArrayReader;
        try {
            byte[] byteValues = FileUtil.readBytes("./data/content/itemDefinitions.dat");
            byteArrayReader = new ByteArrayReader(byteValues);
            int value3 = byteArrayReader.readUnsignedShort();
            customDefinitionCount = value3;
            int index = 0;
            while (index < value3) {
                value2 = ItemDefinition.forId(index);
                ItemDefinition.definitionsById[index] = (ItemDefinition)value2;
                value = byteArrayReader.readUnsignedByte();
                if (value != 200) {
                    int value4;
                    int value5;
                    if (value > 200) {
                        value5 = byteArrayReader.readShort();
                        ItemDefinition itemDefinition = ItemDefinition.forId(value5);
                        ((ItemDefinition)value2).secondaryCurrencyItemId = value5;
                        ((ItemDefinition)value2).equipmentSlot = itemDefinition.equipmentSlot;
                        ((ItemDefinition)value2).untradeable = itemDefinition.untradeable;
                        ((ItemDefinition)value2).twoHanded = itemDefinition.twoHanded;
                        ((ItemDefinition)value2).equipmentAppearanceType = itemDefinition.equipmentAppearanceType;
                        value4 = 0;
                        while (value4 < 25) {
                            ((ItemDefinition)value2).requiredLevels[value4] = itemDefinition.requiredLevels[value4];
                            ++value4;
                        }
                        ((ItemDefinition)value2).requiredQuestPoints = itemDefinition.requiredQuestPoints;
                        value4 = 0;
                        while (value4 < 104) {
                            ((ItemDefinition)value2).requiredQuests[value4] = itemDefinition.requiredQuests[value4];
                            ++value4;
                        }
                        ((ItemDefinition)value2).shopValue = itemDefinition.shopValue;
                        ((ItemDefinition)value2).highAlchemyValue = itemDefinition.highAlchemyValue;
                        ((ItemDefinition)value2).lowAlchemyValue = itemDefinition.lowAlchemyValue;
                        ((ItemDefinition)value2).tokkulValue = itemDefinition.tokkulValue;
                        ((ItemDefinition)value2).weight = itemDefinition.weight;
                        value4 = 0;
                        while (value4 < 14) {
                            ((ItemDefinition)value2).bonuses[value4] = itemDefinition.bonuses[value4];
                            ++value4;
                        }
                        if (value == 202) {
                            ((ItemDefinition)value2).donatorPointValue = byteArrayReader.readShort();
                            if (index == 7999) {
                                if (ServerSettings.membershipRequirementMode == 4) {
                                    ((ItemDefinition)value2).shopValue = ServerSettings.membershipRequirementValue;
                                } else if (ServerSettings.membershipRequirementMode == 5) {
                                    ((ItemDefinition)value2).shopValue = 1000;
                                }
                            }
                        }
                    } else {
                        ((ItemDefinition)value2).equipmentSlot = value - 1;
                        ((ItemDefinition)value2).untradeable = byteArrayReader.readUnsignedByte() == 1;
                        if (value != 0) {
                            ((ItemDefinition)value2).twoHanded = byteArrayReader.readUnsignedByte() == 1;
                            ((ItemDefinition)value2).equipmentAppearanceType = byteArrayReader.readUnsignedByte();
                            while ((value5 = byteArrayReader.readUnsignedByte()) != 0) {
                                int value6;
                                if (value5 == 1) {
                                    value6 = byteArrayReader.readUnsignedByte();
                                    ((ItemDefinition)value2).requiredLevels[value6] = value4 = byteArrayReader.readUnsignedByte();
                                }
                                if (value5 == 2) {
                                    value6 = byteArrayReader.readUnsignedByte();
                                    if (value6 == 250) {
                                        value6 = QuestDefinition.getTotalQuestPointReward();
                                    }
                                    ((ItemDefinition)value2).requiredQuestPoints = value6;
                                }
                                if (value5 != 3) continue;
                                value6 = byteArrayReader.readUnsignedByte();
                                ((ItemDefinition)value2).requiredQuests[value6] = true;
                            }
                            value5 = byteArrayReader.readUnsignedByte();
                            if ((value5 & 1) != 0) {
                                ((ItemDefinition)value2).shopValue = byteArrayReader.readInt();
                            }
                            if ((value5 & 2) != 0) {
                                ((ItemDefinition)value2).highAlchemyValue = byteArrayReader.readInt();
                            }
                            if ((value5 & 4) != 0) {
                                ((ItemDefinition)value2).lowAlchemyValue = byteArrayReader.readInt();
                            }
                            if ((value5 & 8) != 0) {
                                ((ItemDefinition)value2).tokkulValue = byteArrayReader.readInt();
                            }
                            if ((value5 & 0x10) != 0) {
                                byteArrayReader.readInt();
                            }
                            double value7 = byteArrayReader.readShort();
                            ((ItemDefinition)value2).weight = value7 / 1000.0;
                            value = 0;
                            while (value < 14) {
                                double value8 = byteArrayReader.readShort();
                                if (ServerSettings.mod2hsEnabled && (index == 1307 || index == 1309 || index == 1311 || index == 1313 || index == 1315 || index == 1317 || index == 1319 || index == 7158)) {
                                    if (value >= 0 && value <= 4) {
                                        value8 *= ServerSettings.mod2hsAttackBonusRate;
                                    } else if (value == 10) {
                                        value8 *= ServerSettings.mod2hsStrengthBonusRate;
                                    }
                                }
                                ((ItemDefinition)value2).bonuses[value] = (int)value8;
                                ++value;
                            }
                        } else {
                            value5 = byteArrayReader.readUnsignedByte();
                            if ((value5 & 1) != 0) {
                                ((ItemDefinition)value2).shopValue = byteArrayReader.readInt();
                            }
                            if ((value5 & 2) != 0) {
                                ((ItemDefinition)value2).highAlchemyValue = byteArrayReader.readInt();
                            }
                            if ((value5 & 4) != 0) {
                                ((ItemDefinition)value2).lowAlchemyValue = byteArrayReader.readInt();
                            }
                            if ((value5 & 8) != 0) {
                                ((ItemDefinition)value2).tokkulValue = byteArrayReader.readInt();
                            }
                            if ((value5 & 0x10) != 0) {
                                byteArrayReader.readInt();
                            }
                            double value9 = byteArrayReader.readShort();
                            ((ItemDefinition)value2).weight = value9 / 1000.0;
                        }
                    }
                }
                ++index;
            }
        }
        catch (Exception exception) {
            Exception exception2 = exception;
            exception.printStackTrace();
        }
        if (ServerSettings.cacheVersion == 443) {
            try {
                loadRevision443();
            } catch (IOException exception) {
                throw new IllegalStateException("Unable to load revision 443 item definitions", exception);
            }
            return;
        }
        CacheStore cacheStore = CacheStore.getInstance();
        byteArrayReader = null;
        try {
            byteArrayReader = new ByteArrayReader(new CacheArchive(cacheStore.readFile(0, 2)).getFileBytes("obj.dat"));
        }
        catch (Exception exception) {
            exception.printStackTrace();
        }
        definitionCount = cacheStore.getDefinitionIndex().getItemDefinitionEntries().length;
        int index2 = 0;
        while (index2 < definitionCount) {
            byteArrayReader.position = cacheStore.getDefinitionIndex().getItemDefinitionEntry(index2).getDataOffset();
            value = index2;
            value2 = byteArrayReader;
            ItemDefinition itemDefinition = definitionsById[value];
            definitionsById[value].stackable = false;
            itemDefinition.hasNote = false;
            itemDefinition.note = false;
            itemDefinition.membersOnly = false;
            itemDefinition.destroyOption = false;
            loadDefinitionsControlLoop1: while (true) {
                int value10;
                if ((value10 = ((ByteArrayReader)value2).readUnsignedByte()) == 0) {
                    if (value != 4561) break;
                    itemDefinition.stackable = true;
                    break;
                }
                if (value10 == 1) {
                    ((ByteArrayReader)value2).readUnsignedShort();
                    continue;
                }
                if (value10 == 2) {
                    itemDefinition.name = ((ByteArrayReader)value2).readString();
                    continue;
                }
                if (value10 == 3) {
                    String text;
                    itemDefinition.description = text = new String(((ByteArrayReader)value2).readLineBytes());
                    continue;
                }
                if (value10 == 4) {
                    ((ByteArrayReader)value2).readUnsignedShort();
                    continue;
                }
                if (value10 == 5) {
                    ((ByteArrayReader)value2).readUnsignedShort();
                    continue;
                }
                if (value10 == 6) {
                    ((ByteArrayReader)value2).readUnsignedShort();
                    continue;
                }
                if (value10 == 7) {
                    ((ByteArrayReader)value2).readUnsignedShort();
                    continue;
                }
                if (value10 == 8) {
                    ((ByteArrayReader)value2).readUnsignedShort();
                    continue;
                }
                if (value10 == 10) {
                    ((ByteArrayReader)value2).readUnsignedShort();
                    continue;
                }
                if (value10 == 11) {
                    itemDefinition.stackable = true;
                    continue;
                }
                if (value10 == 12) {
                    ((ByteArrayReader)value2).readInt();
                    continue;
                }
                if (value10 == 16) {
                    itemDefinition.membersOnly = true;
                    continue;
                }
                if (value10 == 23) {
                    ((ByteArrayReader)value2).readUnsignedShort();
                    ((ByteArrayReader)value2).readByte();
                    continue;
                }
                if (value10 == 24) {
                    ((ByteArrayReader)value2).readUnsignedShort();
                    continue;
                }
                if (value10 == 25) {
                    ((ByteArrayReader)value2).readUnsignedShort();
                    ((ByteArrayReader)value2).readByte();
                    continue;
                }
                if (value10 == 26) {
                    ((ByteArrayReader)value2).readUnsignedShort();
                    continue;
                }
                if (value10 >= 30 && value10 < 35) {
                    ((ByteArrayReader)value2).readString();
                    continue;
                }
                if (value10 >= 35 && value10 < 40) {
                    String text2 = ((ByteArrayReader)value2).readString();
                    if (!text2.toLowerCase().equals("destroy")) continue;
                    itemDefinition.destroyOption = true;
                    continue;
                }
                if (value10 == 40) {
                    value10 = ((ByteArrayReader)value2).readUnsignedByte();
                    int index3 = 0;
                    while (true) {
                        if (index3 >= value10) continue loadDefinitionsControlLoop1;
                        ((ByteArrayReader)value2).readUnsignedShort();
                        ((ByteArrayReader)value2).readUnsignedShort();
                        ++index3;
                    }
                }
                if (value10 == 78) {
                    ((ByteArrayReader)value2).readUnsignedShort();
                    continue;
                }
                if (value10 == 79) {
                    ((ByteArrayReader)value2).readUnsignedShort();
                    continue;
                }
                if (value10 == 90) {
                    ((ByteArrayReader)value2).readUnsignedShort();
                    continue;
                }
                if (value10 == 91) {
                    ((ByteArrayReader)value2).readUnsignedShort();
                    continue;
                }
                if (value10 == 92) {
                    ((ByteArrayReader)value2).readUnsignedShort();
                    continue;
                }
                if (value10 == 93) {
                    ((ByteArrayReader)value2).readUnsignedShort();
                    continue;
                }
                if (value10 == 95) {
                    ((ByteArrayReader)value2).readUnsignedShort();
                    continue;
                }
                if (value10 == 97) {
                    value10 = ((ByteArrayReader)value2).readUnsignedShort();
                    itemDefinition.note = true;
                    itemDefinition.unnotedId = value10;
                    ItemDefinition itemDefinition2 = definitionsById[value10];
                    definitionsById[value10].hasNote = true;
                    itemDefinition2.notedId = value;
                    continue;
                }
                if (value10 == 98) {
                    ((ByteArrayReader)value2).readUnsignedShort();
                    continue;
                }
                if (value10 >= 100 && value10 < 110) {
                    ((ByteArrayReader)value2).readUnsignedShort();
                    ((ByteArrayReader)value2).readUnsignedShort();
                    continue;
                }
                if (value10 == 110) {
                    ((ByteArrayReader)value2).readUnsignedShort();
                    continue;
                }
                if (value10 == 111) {
                    ((ByteArrayReader)value2).readUnsignedShort();
                    continue;
                }
                if (value10 == 112) {
                    ((ByteArrayReader)value2).readUnsignedShort();
                    continue;
                }
                if (value10 == 113) {
                    ((ByteArrayReader)value2).readByte();
                    continue;
                }
                if (value10 == 114) {
                    ((ByteArrayReader)value2).readByte();
                    continue;
                }
                if (value10 == 115) {
                    ((ByteArrayReader)value2).readUnsignedByte();
                    continue;
                }
                if (value10 == 121) {
                    ((ByteArrayReader)value2).readUnsignedShort();
                    continue;
                }
                if (value10 == 122) {
                    ((ByteArrayReader)value2).readUnsignedShort();
                    continue;
                }
                if (value10 == 140) {
                    value10 = ((ByteArrayReader)value2).readUnsignedByte();
                    int index4 = 0;
                    while (true) {
                        if (index4 >= value10) continue loadDefinitionsControlLoop1;
                        ((ByteArrayReader)value2).readUnsignedShort();
                        ((ByteArrayReader)value2).readUnsignedShort();
                        ++index4;
                    }
                }
                if (value10 != 177) continue;
            }
            ++index2;
        }
        if (!ItemDefinition.isDefined(7936)) {
            RunecraftingHandler.PURE_ESSENCE_ITEM_ID = 1436;
        }
    }

    /** Retired broken weapons and their noted/crafting forms remain readable in old saves. */
    public static boolean isRemovedContent(int itemId) {
        return itemId == 11235 || itemId == 11236
                || (itemId >= 11686 && itemId <= 11715);
    }

    public static boolean isDefined(int value2) {
        return !isRemovedContent(value2) && value2 >= 0 && value2 < definitionsById.length
                && (value2 < definitionCount || value2 < customDefinitionCount
                    || value2 == DRAGON_CLAWS_ID);
    }

    /** Applies stock 443 names, options, prices and note links over server metadata. */
    public static void loadRevision443() throws IOException {
        Map<Integer, byte[]> files = Definitions.readGroup(10);
        int maxId = -1;
        for (Integer id : files.keySet()) maxId = Math.max(maxId, id);
        definitionCount = maxId + 1;
        for (Map.Entry<Integer, byte[]> entry : files.entrySet()) {
            int id = entry.getKey();
            ItemDefinition definition = definitionsById[id];
            if (definition == null) {
                definition = forId(id);
                definitionsById[id] = definition;
            }
            definition.stackable = false;
            definition.note = false;
            definition.hasNote = false;
            definition.membersOnly = false;
            definition.destroyOption = false;
            definition.groundActions = new String[] {null, null, "Take", null, null};
            definition.inventoryActions = new String[] {null, null, null, null, "Drop"};
            definition.unnotedId = -1;
            definition.notedId = -1;
            decodeRevision443(definition, entry.getValue());
        }
        for (Map.Entry<Integer, byte[]> entry : files.entrySet()) {
            ItemDefinition definition = definitionsById[entry.getKey()];
            if (definition.note && definition.unnotedId >= 0
                    && definition.unnotedId < definitionsById.length) {
                ItemDefinition original = definitionsById[definition.unnotedId];
                if (original != null) {
                    original.hasNote = true;
                    original.notedId = definition.id;
                }
            }
        }
        // Server metadata uses the later black axe requirement; revision 443 used level 6.
        definitionsById[1361].requiredLevels[8] = 6;
        // These Christmas assets postdate the legacy server metadata. Snowballs are
        // weapons, shards/tree are inventory items, and the hat has no combat bonuses.
        definitionsById[10501].equipmentSlot = 3;
        definitionsById[10506].equipmentSlot = -1;
        definitionsById[10507].equipmentSlot = 0;
        definitionsById[10508].equipmentSlot = -1;
        for (int id : new int[]{10501, 10506, 10507, 10508}) {
            definitionsById[id].untradeable = true;
            java.util.Arrays.fill(definitionsById[id].bonuses, 0);
        }
        load2009scapeExamines();
    }

    private static void load2009scapeExamines() throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                new FileInputStream("./data/content/itemExamines2009scape.tsv"), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isEmpty() || line.charAt(0) == '#') continue;
                String[] parts = line.split("\t", 2);
                if (parts.length != 2) continue;
                int id = Integer.parseInt(parts[0]);
                if (id >= 0 && id < definitionsById.length && definitionsById[id] != null) {
                    ItemDefinition definition = definitionsById[id];
                    if (definition.description == null || definition.description.trim().isEmpty()
                            || definition.description.equals("It's an item!")) {
                        String historical = HistoricalExamines.find("item", id, definition.name);
                        definition.description = historical == null ? parts[1] : historical;
                    }
                }
            }
        }
    }

    private static void decodeRevision443(ItemDefinition definition, byte[] data)
            throws IOException {
        ConfigReader reader = new ConfigReader(data);
        while (reader.position() < reader.length()) {
            int opcode = reader.readUnsignedByte();
            if (opcode == 0) {
                if (reader.position() != reader.length()) {
                    throw new IOException("Trailing bytes in 443 item " + definition.id);
                }
                return;
            }
            if (opcode == 1 || opcode == 4 || opcode == 5 || opcode == 6
                    || opcode == 7 || opcode == 8 || opcode == 10 || opcode == 24
                    || opcode == 26 || opcode == 78 || opcode == 79
                    || opcode == 90 || opcode == 91 || opcode == 92 || opcode == 93
                    || opcode == 95 || opcode == 98 || opcode == 110 || opcode == 111
                    || opcode == 112 || opcode == 121 || opcode == 122) {
                reader.readUnsignedShort();
            } else if (opcode == 2) {
                definition.name = reader.readString();
            } else if (opcode == 3) {
                definition.description = reader.readString();
            } else if (opcode == 11) {
                definition.stackable = true;
            } else if (opcode == 12) {
                int price = reader.readInt();
                if (definition.shopValue == 0) definition.shopValue = price;
            } else if (opcode == 16) {
                definition.membersOnly = true;
            } else if (opcode == 23 || opcode == 25) {
                reader.readUnsignedShort();
                reader.readByte();
            } else if (opcode >= 30 && opcode < 35) {
                String action = reader.readString();
                definition.groundActions[opcode - 30] = "hidden".equalsIgnoreCase(action) ? null : action;
            } else if (opcode >= 35 && opcode < 40) {
                String action = reader.readString();
                definition.inventoryActions[opcode - 35] = action;
                if ("destroy".equalsIgnoreCase(action)) {
                    definition.destroyOption = true;
                }
            } else if (opcode == 40 || opcode == 41 || opcode == 140) {
                int count = reader.readUnsignedByte();
                reader.skip(count * 4);
            } else if (opcode == 97) {
                definition.note = true;
                definition.unnotedId = reader.readUnsignedShort();
            } else if (opcode >= 100 && opcode < 110) {
                reader.skip(4);
            } else if (opcode == 113 || opcode == 114) {
                reader.readByte();
            } else if (opcode == 115) {
                reader.readUnsignedByte();
            } else if (opcode == 177) {
                // Placeholder flag without a payload in this revision.
            } else if (opcode == 249) {
                int count = reader.readUnsignedByte();
                for (int i = 0; i < count; i++) {
                    boolean stringValue = reader.readUnsignedByte() == 1;
                    reader.skip(3);
                    if (stringValue) reader.readString(); else reader.readInt();
                }
            } else {
                throw new IOException("Unsupported 443 item opcode " + opcode
                        + " for item " + definition.id + " at byte "
                        + (reader.position() - 1));
            }
        }
        throw new IOException("Unterminated 443 item " + definition.id);
    }

    public final String getInventoryAction(int slot) {
        return slot < 0 || slot >= inventoryActions.length ? null : inventoryActions[slot];
    }

    public final String getGroundAction(int slot) {
        return slot < 0 || slot >= groundActions.length ? null : groundActions[slot];
    }

    private ItemDefinition(int id, String name, String description, String text32, boolean enabled6, boolean enabled22, boolean enabled32, int value22, int value32, boolean enabled42, int value42, int value52, int value62, int value72, int[] bonuses, int value82, int[] requiredLevels, int value92, boolean[] blArray, double value12, int value103, int value112, boolean enabled52) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.note = false;
        this.hasNote = false;
        this.stackable = false;
        this.unnotedId = -1;
        this.notedId = -1;
        this.membersOnly = true;
        this.tokkulValue = 0;
        this.highAlchemyValue = 0;
        this.lowAlchemyValue = 0;
        this.bonuses = bonuses;
        String text4 = text32;
        this.equipmentSlot = text4.equals("HAT") ? 0 : (text4.equals("CAPE") ? 1 : (text4.equals("AMULET") ? 2 : (text4.equals("WEAPON") ? 3 : (text4.equals("BODY") ? 4 : (text4.equals("SHIELD") ? 5 : (text4.equals("LEGS") ? 7 : (text4.equals("GLOVES") ? 9 : (text4.equals("BOOTS") ? 10 : (text4.equals("RING") ? 12 : (text4.equals("ARROWS") ? 13 : -1))))))))));
        this.equipmentAppearanceType = 0;
        this.requiredLevels = requiredLevels;
        this.requiredQuestPoints = 0;
        this.requiredQuests = blArray;
        this.weight = 0.0;
        this.donatorPointValue = 0;
        this.destroyOption = false;
    }

    public final int getId() {
        return this.id;
    }

    public final String getName() {
        if (this.note && this.unnotedId != -1 && this.unnotedId != this.id) {
            this.name = ItemDefinition.forId(this.unnotedId).getName();
        }
        return this.name;
    }

    public final String getShortName() {
        if (this.id == 249) {
            return "guam";
        }
        if (this.id == 377) {
            return "raw lob";
        }
        if (this.id == 379) {
            return "lob";
        }
        if (this.id == 381) {
            return "rare black lobster";
        }
        if (this.id == 536) {
            return "d bone";
        }
        if (this.id == 556) {
            return "air";
        }
        if (this.id == 558) {
            return "mind";
        }
        if (this.id == 560) {
            return "death";
        }
        if (this.id == 561) {
            return "nat";
        }
        if (this.id == 563) {
            return "law";
        }
        if (this.id == 564) {
            return "cosmic";
        }
        if (this.id == 565) {
            return "blood";
        }
        if (this.id == 1305) {
            return "dlong";
        }
        if (this.id == 1319) {
            return "r2h";
        }
        if (this.id == 1333) {
            return "rune scim";
        }
        if (this.id == 1373) {
            return "rune baxe";
        }
        if (this.id == 1377) {
            return "dbaxe";
        }
        if (this.id == 1436) {
            return "rune ess";
        }
        if (this.id == 1725) {
            return "str ammy";
        }
        if (this.id == 1727) {
            return "mage ammy";
        }
        if (this.id == 1729) {
            return "def ammy";
        }
        if (this.id == 1731) {
            return "power ammy";
        }
        if (this.id == 4099) {
            return "dark mystic hat";
        }
        if (this.id == 4101) {
            return "dark mystic top";
        }
        if (this.id == 4103) {
            return "dark mystic bottom";
        }
        if (this.id == 4105) {
            return "dark mystic gloves";
        }
        if (this.id == 4107) {
            return "dark mystic boots";
        }
        if (this.id == 4109) {
            return "light mystic hat";
        }
        if (this.id == 4111) {
            return "light mystic top";
        }
        if (this.id == 4113) {
            return "light mystic bottom";
        }
        if (this.id == 4115) {
            return "light mystic gloves";
        }
        if (this.id == 4117) {
            return "light mystic boots";
        }
        if (this.id == 4151) {
            return "whip";
        }
        if (this.id == 4153) {
            return "gmaul";
        }
        if (this.id == 6522) {
            return "obby ring";
        }
        if (this.id == 6523) {
            return "obby sword";
        }
        if (this.id == 6524) {
            return "obby shield";
        }
        if (this.id == 6525) {
            return "obby knife";
        }
        if (this.id == 6526) {
            return "obby staff";
        }
        if (this.id == 6527) {
            return "obby mace";
        }
        if (this.id == 6528) {
            return "obby maul";
        }
        if (this.id == 6568) {
            return "obby cape";
        }
        if (this.id == 7936) {
            return "pure ess";
        }
        return null;
    }

    public final String getDisplayName() {
        String shortName = this.getShortName();
        if (shortName == null) {
            return this.getName();
        }
        return shortName;
    }

    public final String getDescription() {
        if (this.note && this.unnotedId != -1 && this.unnotedId != this.id) {
            return "Swap this note at any bank for the equivalent item.";
        }
        return HistoricalExamines.resolve("item", id, name, description);
    }

    public final boolean canBeTransferredByDropping() {
        return this.id == 759 || this.id == 763 || this.id == 765 || this.id == 769 || this.id == 1586 || this.id == 1577 || this.id == 7999;
    }

    public final boolean isNote() {
        return this.note;
    }

    public final boolean hasNote() {
        return this.hasNote;
    }

    public final boolean isStackable() {
        return this.stackable || this.note;
    }

    public final int getUnnotedId() {
        return this.unnotedId;
    }

    public final int getNotedId() {
        return this.notedId;
    }

    public final boolean isMembersOnly() {
        return this.membersOnly;
    }

    public final int getValue() {
        return this.getShopValue();
    }

    public final int getTokkulValue() {
        int value = this.tokkulValue;
        ItemDefinition itemDefinition = this;
        if (itemDefinition.note && this.unnotedId != -1 && this.unnotedId != this.id && value < ItemDefinition.forId(this.unnotedId).getTokkulValue()) {
            value = ItemDefinition.forId(this.unnotedId).getTokkulValue();
        }
        if (value == 0) {
            return 1;
        }
        return value;
    }

    public final int getShopValue() {
        if (this.note && this.unnotedId != -1 && this.unnotedId != this.id && this.shopValue < ItemDefinition.forId(this.unnotedId).getShopValue()) {
            this.shopValue = ItemDefinition.forId(this.unnotedId).getShopValue();
        }
        return this.shopValue;
    }

    public final int getDonatorPointValue() {
        if (this.note && this.unnotedId != -1 && this.unnotedId != this.id && this.donatorPointValue < ItemDefinition.forId(this.unnotedId).getDonatorPointValue()) {
            this.donatorPointValue = ItemDefinition.forId(this.unnotedId).getDonatorPointValue();
        }
        return this.donatorPointValue;
    }

    public final int getLowAlchemyValue() {
        int value = this.lowAlchemyValue;
        if (value == 0) {
            if (this.note && this.unnotedId != -1 && this.unnotedId != this.id) {
                value = ItemDefinition.forId(this.unnotedId).getLowAlchemyValue();
            }
            if (value == 0) {
                value = (this.getShopValue() << 1) / 5;
            }
        }
        return value;
    }

    public final int getHighAlchemyValue() {
        int value = this.highAlchemyValue;
        if (value == 0) {
            if (this.note && this.unnotedId != -1 && this.unnotedId != this.id) {
                value = ItemDefinition.forId(this.unnotedId).getHighAlchemyValue();
            }
            if (value == 0) {
                value = this.getShopValue() * 3 / 5;
            }
        }
        return value;
    }

    public final int[] getBonuses() {
        return this.bonuses;
    }

    public final int getBonus(int value2) {
        return this.bonuses[value2];
    }

    public final int getRequiredLevel(int level) {
        return this.requiredLevels[level];
    }

    public final boolean requiresQuest(int value2) {
        return this.requiredQuests[value2];
    }

    public final double getWeight() {
        return this.weight;
    }

    public final int getEquipmentSlot() {
        return this.equipmentSlot;
    }

    public final int getEquipmentAppearanceType() {
        return this.equipmentAppearanceType;
    }

    public final int getRequiredQuestPoints() {
        return this.requiredQuestPoints;
    }

    public final boolean isTwoHanded() {
        return this.twoHanded;
    }

    public final boolean isUntradeable() {
        boolean enabled = this.untradeable;
        if (this.note && this.unnotedId != -1 && this.unnotedId != this.id) {
            enabled = ItemDefinition.forId(this.unnotedId).isUntradeable();
        }
        return enabled;
    }

    public final boolean hasDestroyOption() {
        boolean enabled = this.destroyOption;
        if (this.note && this.unnotedId != -1 && this.unnotedId != this.id) {
            enabled = ItemDefinition.forId(this.unnotedId).hasDestroyOption();
        }
        return enabled;
    }

    public static int findIdByName(String name) {
        String normalizedName = name.toLowerCase();
        ItemDefinition[] itemDefinitionArray = definitionsById;
        int length = definitionsById.length;
        int index = 0;
        while (index < length) {
            ItemDefinition itemDefinition = itemDefinitionArray[index];
            if (itemDefinition != null && itemDefinition.getName() != null && itemDefinition.getName().toLowerCase().equalsIgnoreCase(normalizedName)) {
                return itemDefinition.id;
            }
            ++index;
        }
        return 0;
    }

}
