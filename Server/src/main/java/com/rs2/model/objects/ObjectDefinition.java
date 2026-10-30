package com.rs2.model.objects;

import com.rs2.cache.js5.Definitions;
import com.rs2.model.HistoricalExamines;
import com.rs2.util.ByteArrayReader;
import java.io.IOException;
import java.util.Map;
import java.util.logging.Logger;

public class ObjectDefinition {
    public static ObjectDefinition[] definitionsById;
    private int objectId;
    private final String[] actions = new String[5];
    public String name;
    public String description;
    public int width;
    public int length;
    public boolean solid;
    public boolean interactive;
    public boolean blocksProjectiles;
    public boolean projectileCollisionIgnored;

    static {
        Logger.getLogger(ObjectDefinition.class.getName());
    }

    public static int loadRevision443() throws IOException {
        Map<Integer, byte[]> files = Definitions.readGroup(6);
        int maxId = -1;
        for (Integer id : files.keySet()) {
            maxId = Math.max(maxId, id);
        }
        definitionsById = new ObjectDefinition[maxId + 1];
        for (Map.Entry<Integer, byte[]> entry : files.entrySet()) {
            int id = entry.getKey();
            ObjectDefinition definition = forId(id);
            definition.name = "";
            definition.description = null;
            definition.width = 1;
            definition.length = 1;
            definition.solid = true;
            definition.blocksProjectiles = true;
            definition.interactive = false;
            decodeRevision443(definition, entry.getValue());
            switch (id) {
                case 19030:
                case 19031:
                case 19033:
                case 19034:
                case 19035:
                    definition.description = "A large pile of snow.";
                    break;
                case 19036:
                    definition.description = "A strong metal cage.";
                    break;
                case 19037:
                    definition.description = "A strong cage; it has a gublinch in it.";
                    break;
                case 19038:
                    definition.description = "A pretty wintumber tree.";
                    break;
                case 19040:
                    definition.description = "It leads back up to the surface.";
                    break;
                case 20100:
                    definition.description = "These childerkins have been childernapped by the gublinch.";
                    break;
            }
            definition.projectileCollisionIgnored = definition.isProjectileCollisionIgnored();
        }
        return definitionsById.length;
    }

    private static void decodeRevision443(ObjectDefinition definition, byte[] data) throws IOException {
        ByteArrayReader reader = new ByteArrayReader(data);
        int actionFlag = -1;
        boolean hasActions = false;
        while (reader.position < data.length) {
            int opcode = reader.readUnsignedByte();
            if (opcode == 0) {
                if (reader.position != data.length) {
                    throw new IOException("Trailing bytes in 443 object " + definition.objectId);
                }
                definition.interactive = actionFlag == 1 || (actionFlag == -1 && hasActions);
                return;
            }
            if (opcode == 1) {
                int count = reader.readUnsignedByte();
                for (int i = 0; i < count; i++) {
                    reader.readUnsignedShort();
                    reader.readUnsignedByte();
                }
            } else if (opcode == 2) {
                definition.name = readRevision443String(reader, data);
            } else if (opcode == 3) {
                definition.description = readRevision443String(reader, data);
            } else if (opcode == 5) {
                int count = reader.readUnsignedByte();
                for (int i = 0; i < count; i++) reader.readUnsignedShort();
            } else if (opcode == 14) {
                definition.width = reader.readUnsignedByte();
            } else if (opcode == 15) {
                definition.length = reader.readUnsignedByte();
            } else if (opcode == 17) {
                definition.solid = false;
            } else if (opcode == 18) {
                // Separate walking and projectile collision in the 443 format.
                definition.blocksProjectiles = false;
            } else if (opcode == 19) {
                actionFlag = reader.readUnsignedByte();
            } else if (opcode == 21 || opcode == 22 || opcode == 23 || opcode == 27
                    || opcode == 62 || opcode == 64 || opcode == 73 || opcode == 74) {
                if (opcode == 27) definition.solid = true;
            } else if (opcode == 24 || opcode == 60 || opcode == 65 || opcode == 66
                    || opcode == 67 || opcode == 68 || opcode == 82) {
                reader.readUnsignedShort();
            } else if (opcode == 28 || opcode == 69 || opcode == 75 || opcode == 81) {
                reader.readUnsignedByte();
            } else if (opcode == 29 || opcode == 39) {
                reader.readByte();
            } else if (opcode >= 30 && opcode < 35) {
                String action = readRevision443String(reader, data);
                if (!"hidden".equalsIgnoreCase(action)) {
                    definition.actions[opcode - 30] = action;
                    hasActions = true;
                }
            } else if (opcode == 40 || opcode == 41) {
                int count = reader.readUnsignedByte();
                for (int i = 0; i < count; i++) {
                    reader.readUnsignedShort();
                    reader.readUnsignedShort();
                }
            } else if (opcode == 70 || opcode == 71 || opcode == 72) {
                reader.readShort();
            } else if (opcode == 77 || opcode == 92) {
                reader.readUnsignedShort();
                reader.readUnsignedShort();
                if (opcode == 92) reader.readUnsignedShort();
                int count = reader.readUnsignedByte();
                for (int i = 0; i <= count; i++) reader.readUnsignedShort();
            } else if (opcode == 78) {
                reader.readUnsignedShort();
                reader.readUnsignedByte();
            } else if (opcode == 79) {
                reader.readUnsignedShort();
                reader.readUnsignedShort();
                reader.readUnsignedByte();
                int count = reader.readUnsignedByte();
                for (int i = 0; i < count; i++) reader.readUnsignedShort();
            } else if (opcode == 249) {
                int count = reader.readUnsignedByte();
                for (int i = 0; i < count; i++) {
                    boolean stringValue = reader.readUnsignedByte() == 1;
                    reader.readUnsignedByte();
                    reader.readUnsignedByte();
                    reader.readUnsignedByte();
                    if (stringValue) readRevision443String(reader, data);
                    else reader.readInt();
                }
            } else {
                throw new IOException("Unsupported 443 object opcode " + opcode
                        + " for object " + definition.objectId);
            }
        }
        throw new IOException("Unterminated 443 object " + definition.objectId);
    }

    private static String readRevision443String(ByteArrayReader reader, byte[] data)
            throws IOException {
        int start = reader.position;
        while (reader.position < data.length && data[reader.position] != 0) {
            reader.position++;
        }
        if (reader.position == data.length) {
            throw new IOException("Unterminated 443 string");
        }
        String value = new String(data, start, reader.position - start,
                java.nio.charset.StandardCharsets.ISO_8859_1);
        reader.position++;
        return value;
    }

    public static ObjectDefinition forId(int value3) {
        if (value3 >= 0 && value3 < definitionsById.length) {
            if (definitionsById[value3] == null) {
                int value2 = value3;
                ObjectDefinition.definitionsById[value3] = new ObjectDefinition(value2, "Object: #" + value2, "Its an object!", 1, 1, false, false, false, true, 2);
            }
            return definitionsById[value3];
        }
        return null;
    }

    private ObjectDefinition(int objectId, String name, String text22, int value22, int value32, boolean enabled5, boolean enabled22, boolean enabled32, boolean enabled42, int value42) {
        this.objectId = objectId;
        this.description = text22;
        this.name = name;
        if (name == null) {
            this.name = "";
        }
        this.width = 1;
        this.length = 1;
        this.solid = false;
        this.interactive = false;
        this.blocksProjectiles = true;
        this.projectileCollisionIgnored = this.isProjectileCollisionIgnored();
    }

    public final int getObjectId() {
        return this.objectId;
    }

    public final String getAction(int slot) {
        return slot < 0 || slot >= actions.length ? null : actions[slot];
    }

    public final String getName() {
        return this.name;
    }

    public final String getDescription() {
        return HistoricalExamines.resolve("object", objectId, name, description);
    }

    public final int getWidthForOrientation(int width) {
        if (width == 1 || width == 3) {
            return this.length;
        }
        return this.width;
    }

    public final int getLengthForOrientation(int value2) {
        if (value2 == 1 || value2 == 3) {
            return this.width;
        }
        return this.length;
    }

    public final int getMaxDimension() {
        if (this.length > this.width) {
            return this.length;
        }
        return this.width;
    }

    public final boolean isProjectileCollisionIgnored() {
        if (this.objectId == 1116 || this.objectId == 1117) {
            return false;
        }
        if (this.objectId == 6771 || this.objectId == 6772 || this.objectId == 6773 || this.objectId == 6821 || this.objectId == 6822 || this.objectId == 6823) {
            return false;
        }
        int[] integerValues = new int[]{2440, 2441, 2442, 2443, 2637, 9563, 9565, 14462, 14464, 14465, 14466, 14467, 14468, 14470, 14502, 11754, 3007, 980, 997, 4262, 14437, 14438, 4437, 4439, 3487, 3457};
        int index = 0;
        while (index < 26) {
            int value = integerValues[index];
            if (value == this.objectId) {
                return true;
            }
            ++index;
        }
        if (this.name != null) {
            String objectName = this.name.toLowerCase();
            String[] stringValues = new String[]{"fungus", "mushroom", "sarcophagus", "counter", "plant", "altar", "pew", "log", "stump", "stool", "sign", "cart", "chest", "rock", "bush", "hedge", "chair", "table", "crate", "barrel", "box", "skeleton", "corpse", "vent", "stone", "rockslide"};
            int index2 = 0;
            while (index2 < 26) {
                String ignoredNameFragment = stringValues[index2];
                if (objectName.contains(ignoredNameFragment)) {
                    return true;
                }
                ++index2;
            }
        }
        return false;
    }
}
