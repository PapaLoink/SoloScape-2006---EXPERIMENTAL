package com.rs2.model.npc;

import com.rs2.ServerSettings;
import com.rs2.cache.CacheArchive;
import com.rs2.cache.CacheStore;
import com.rs2.cache.js5.ConfigReader;
import com.rs2.cache.js5.Definitions;
import com.rs2.model.World;
import com.rs2.model.HistoricalExamines;
import com.rs2.model.npc.combat.NpcCombatDefinition;
import com.rs2.model.npc.combat.NpcDefinitionAttackStyleCombatDefinition;
import com.rs2.model.npc.combat.NpcDefinitionMeleeCombatDefinition;
import com.rs2.util.ByteArrayReader;
import com.rs2.util.FileUtil;
import java.io.IOException;
import java.util.Map;

public final class NpcDefinition {
    private static int customDefinitionCount = 0;
    private int aggressionRange;
    private int spawnRadius = 5;
    private int chaseRadius;
    private int attackBonusTypeId = 0;
    private int respawnDelayTicks = 0;
    private int attackLevel = 0;
    private int strengthLevel = 0;
    private int defenceLevel = 0;
    private int magicLevel = 0;
    private int rangedLevel = 0;
    private int meleeAttackBonus = 0;
    private int meleeStrengthBonus = 0;
    private int magicAttackBonus = 0;
    private int rangedAttackBonus = 0;
    private int[] defenceBonuses;
    private int poisonDamage = 0;
    private double poisonChance = 0.0;
    private static int cacheDefinitionCount;
    private int shopId = -1;
    private int id;
    private int dropTableNpcIdOverride = -1;
    private String name;
    private String description;
    private String[] actions = new String[5];
    public int respawnDelaySeconds = 0;
    private int legacyAttackBonus = 20;
    private int legacyMeleeDefenceBonus = 20;
    private int legacyRangedDefenceBonus = 20;
    private int legacyMagicDefenceBonus = 20;
    private int combatLevel = 0;
    private int hitpoints = 1;
    private int maxHit = 0;
    private int size = 1;
    private int attackDelay = 4;
    private int attackAnimationId = 422;
    private int blockAnimationId = 404;
    private int deathAnimationId = 2304;
    private int hitSoundId = -1;
    private int attackSoundId = -1;
    private int deathSoundId = -1;
    private boolean attackable = false;
    private int aggressionType = 0;
    private boolean protectedFromMelee = false;
    private boolean protectedFromRanged = false;
    private boolean protectedFromMagic = false;

    public static void loadDefinitions() {
        int value;
        int value2;
        int value3;
        int value4;
        Object value5;
        Object value6;
        try {
            value6 = FileUtil.readBytes("data/npcs/npcDefinitions.dat");
            ByteArrayReader byteArrayReader = new ByteArrayReader((byte[])value6);
            value6 = byteArrayReader;
            customDefinitionCount = byteArrayReader.readUnsignedShort();
            int index = 0;
            while (index < customDefinitionCount) {
                value5 = NpcDefinition.forId(index);
                int value7 = ((ByteArrayReader)value6).readUnsignedByte();
                ((NpcDefinition)value5).id = index;
                ((NpcDefinition)value5).hitpoints = 0;
                if (value7 == 1) {
                    ((NpcDefinition)value5).respawnDelaySeconds = 60;
                    ((NpcDefinition)value5).aggressionType = ((ByteArrayReader)value6).readUnsignedByte();
                    ((ByteArrayReader)value6).readUnsignedByte();
                    ((ByteArrayReader)value6).readUnsignedByte();
                    ((NpcDefinition)value5).maxHit = ((ByteArrayReader)value6).readUnsignedByte() - 1;
                    ((NpcDefinition)value5).hitpoints = ((ByteArrayReader)value6).readUnsignedShort() - 1;
                    ((NpcDefinition)value5).attackDelay = ((ByteArrayReader)value6).readUnsignedShort() - 1;
                    ((NpcDefinition)value5).attackAnimationId = ((ByteArrayReader)value6).readUnsignedShort() - 1;
                    ((NpcDefinition)value5).blockAnimationId = ((ByteArrayReader)value6).readUnsignedShort() - 1;
                    ((NpcDefinition)value5).deathAnimationId = ((ByteArrayReader)value6).readUnsignedShort() - 1;
                    ((NpcDefinition)value5).attackSoundId = ((ByteArrayReader)value6).readUnsignedShort() - 1;
                    ((NpcDefinition)value5).hitSoundId = ((ByteArrayReader)value6).readUnsignedShort() - 1;
                    ((NpcDefinition)value5).deathSoundId = ((ByteArrayReader)value6).readUnsignedShort() - 1;
                    ((NpcDefinition)value5).legacyAttackBonus = ((ByteArrayReader)value6).readUnsignedShort() - 1;
                    ((NpcDefinition)value5).legacyMeleeDefenceBonus = ((ByteArrayReader)value6).readUnsignedShort() - 1;
                    ((NpcDefinition)value5).legacyRangedDefenceBonus = ((ByteArrayReader)value6).readUnsignedShort() - 1;
                    ((NpcDefinition)value5).legacyMagicDefenceBonus = ((ByteArrayReader)value6).readUnsignedShort() - 1;
                    ((NpcDefinition)value5).attackBonusTypeId = ((ByteArrayReader)value6).readUnsignedByte() - 1;
                    if (((NpcDefinition)value5).attackBonusTypeId >= 3) {
                        ((NpcDefinition)value5).attackBonusTypeId = 0;
                    }
                    ((NpcDefinition)value5).respawnDelayTicks = ((ByteArrayReader)value6).readUnsignedShort() - 1;
                    ((NpcDefinition)value5).attackLevel = ((ByteArrayReader)value6).readUnsignedShort() - 1;
                    ((NpcDefinition)value5).strengthLevel = ((ByteArrayReader)value6).readUnsignedShort() - 1;
                    ((NpcDefinition)value5).defenceLevel = ((ByteArrayReader)value6).readUnsignedShort() - 1;
                    ((NpcDefinition)value5).magicLevel = ((ByteArrayReader)value6).readUnsignedShort() - 1;
                    ((NpcDefinition)value5).rangedLevel = ((ByteArrayReader)value6).readUnsignedShort() - 1;
                    ((NpcDefinition)value5).meleeAttackBonus = ((ByteArrayReader)value6).readShort() - 1;
                    ((NpcDefinition)value5).meleeStrengthBonus = ((ByteArrayReader)value6).readShort() - 1;
                    ((NpcDefinition)value5).magicAttackBonus = ((ByteArrayReader)value6).readShort() - 1;
                    ((ByteArrayReader)value6).readShort();
                    ((NpcDefinition)value5).rangedAttackBonus = ((ByteArrayReader)value6).readShort() - 1;
                    ((ByteArrayReader)value6).readShort();
                    int value8 = ((ByteArrayReader)value6).readShort() - 1;
                    value4 = ((ByteArrayReader)value6).readShort() - 1;
                    int value9 = ((ByteArrayReader)value6).readShort() - 1;
                    value3 = ((ByteArrayReader)value6).readShort() - 1;
                    value2 = ((ByteArrayReader)value6).readShort() - 1;
                    int[] integerValues = new int[]{value8, value4, value9, value3, value2};
                    ((NpcDefinition)value5).defenceBonuses = integerValues;
                    ((ByteArrayReader)value6).readUnsignedByte();
                    ((ByteArrayReader)value6).readUnsignedByte();
                    ((NpcDefinition)value5).poisonDamage = ((ByteArrayReader)value6).readUnsignedByte();
                    double value10 = ((ByteArrayReader)value6).readShort();
                    ((NpcDefinition)value5).poisonChance = value10 / 1000.0;
                    ((NpcDefinition)value5).spawnRadius = ((ByteArrayReader)value6).readUnsignedByte();
                    ((NpcDefinition)value5).chaseRadius = ((ByteArrayReader)value6).readUnsignedByte();
                    if (((NpcDefinition)value5).aggressionType > 0) {
                        ((NpcDefinition)value5).aggressionRange = ((ByteArrayReader)value6).readUnsignedByte();
                    }
                }
                if (value7 == 2) {
                    ((NpcDefinition)value5).shopId = ((ByteArrayReader)value6).readUnsignedShort() - 2;
                    ((NpcDefinition)value5).spawnRadius = ((ByteArrayReader)value6).readUnsignedByte();
                }
                if (value7 == 3) {
                    int value11 = ((ByteArrayReader)value6).readUnsignedShort() - 1;
                    NpcDefinition npcDefinition = NpcDefinition.forId(value11);
                    ((NpcDefinition)value5).respawnDelaySeconds = npcDefinition.respawnDelaySeconds;
                    ((NpcDefinition)value5).aggressionType = npcDefinition.aggressionType;
                    ((NpcDefinition)value5).maxHit = npcDefinition.maxHit;
                    ((NpcDefinition)value5).hitpoints = npcDefinition.hitpoints;
                    ((NpcDefinition)value5).attackDelay = npcDefinition.attackDelay;
                    ((NpcDefinition)value5).attackAnimationId = npcDefinition.attackAnimationId;
                    ((NpcDefinition)value5).blockAnimationId = npcDefinition.blockAnimationId;
                    ((NpcDefinition)value5).deathAnimationId = npcDefinition.deathAnimationId;
                    ((NpcDefinition)value5).attackSoundId = npcDefinition.attackSoundId;
                    ((NpcDefinition)value5).hitSoundId = npcDefinition.hitSoundId;
                    ((NpcDefinition)value5).deathSoundId = npcDefinition.deathSoundId;
                    ((NpcDefinition)value5).legacyAttackBonus = npcDefinition.legacyAttackBonus;
                    ((NpcDefinition)value5).legacyMeleeDefenceBonus = npcDefinition.legacyMeleeDefenceBonus;
                    ((NpcDefinition)value5).legacyRangedDefenceBonus = npcDefinition.legacyRangedDefenceBonus;
                    ((NpcDefinition)value5).legacyMagicDefenceBonus = npcDefinition.legacyMagicDefenceBonus;
                    ((NpcDefinition)value5).shopId = npcDefinition.shopId;
                    ((NpcDefinition)value5).attackBonusTypeId = npcDefinition.attackBonusTypeId;
                    ((NpcDefinition)value5).respawnDelayTicks = npcDefinition.respawnDelayTicks;
                    ((NpcDefinition)value5).attackLevel = npcDefinition.attackLevel;
                    ((NpcDefinition)value5).strengthLevel = npcDefinition.strengthLevel;
                    ((NpcDefinition)value5).defenceLevel = npcDefinition.defenceLevel;
                    ((NpcDefinition)value5).magicLevel = npcDefinition.magicLevel;
                    ((NpcDefinition)value5).rangedLevel = npcDefinition.rangedLevel;
                    ((NpcDefinition)value5).meleeAttackBonus = npcDefinition.meleeAttackBonus;
                    ((NpcDefinition)value5).meleeStrengthBonus = npcDefinition.meleeStrengthBonus;
                    ((NpcDefinition)value5).magicAttackBonus = npcDefinition.magicAttackBonus;
                    ((NpcDefinition)value5).rangedAttackBonus = npcDefinition.rangedAttackBonus;
                    ((NpcDefinition)value5).defenceBonuses = npcDefinition.defenceBonuses;
                    ((NpcDefinition)value5).poisonDamage = npcDefinition.poisonDamage;
                    ((NpcDefinition)value5).poisonChance = npcDefinition.poisonChance;
                    ((NpcDefinition)value5).aggressionRange = npcDefinition.aggressionRange;
                    ((NpcDefinition)value5).spawnRadius = npcDefinition.spawnRadius;
                    ((NpcDefinition)value5).chaseRadius = npcDefinition.chaseRadius;
                }
                if (ServerSettings.cacheVersion < 319 && ((NpcDefinition)value5).blockAnimationId == 1834) {
                    ((NpcDefinition)value5).blockAnimationId = 424;
                }
                if (ServerSettings.cacheVersion < 327 && ((NpcDefinition)value5).deathAnimationId == 2304) {
                    ((NpcDefinition)value5).deathAnimationId = 836;
                }
                if (ServerSettings.cacheVersion < 337 && index == 750) {
                    NpcDefinition npcDefinition = NpcDefinition.forId(92);
                    ((NpcDefinition)value5).attackAnimationId = npcDefinition.attackAnimationId;
                    ((NpcDefinition)value5).blockAnimationId = npcDefinition.blockAnimationId;
                    ((NpcDefinition)value5).deathAnimationId = npcDefinition.deathAnimationId;
                }
                NpcDefinition npcDefinition2 = (NpcDefinition)value5;
                World.getNpcDefinitions()[npcDefinition2.id] = npcDefinition2;
                ++index;
            }
        }
        catch (Exception exception) {
            value6 = exception;
            exception.printStackTrace();
        }
        if (ServerSettings.cacheVersion == 443) {
            try {
                loadRevision443();
            } catch (IOException exception) {
                throw new IllegalStateException("Unable to load revision 443 NPC definitions", exception);
            }
            NpcDefinition.initializeCombatDefinitions();
            NpcDefinition.copyDefinition(2257, 2258);
            NpcDefinition.copyDefinition(2260, 2261);
            return;
        }
        value6 = CacheStore.getInstance();
        ByteArrayReader byteArrayReader = null;
        try {
            byteArrayReader = new ByteArrayReader(new CacheArchive(((CacheStore)value6).readFile(0, 2)).getFileBytes("npc.dat"));
        }
        catch (Exception exception) {
            value5 = exception;
            exception.printStackTrace();
        }
        cacheDefinitionCount = value = ((CacheStore)value6).getDefinitionIndex().getNpcDefinitionEntries().length;
        byteArrayReader.position = 2;
        int index2 = 0;
        while (index2 < value) {
            byteArrayReader.position = ((CacheStore)value6).getDefinitionIndex().getNpcDefinitionEntry(index2).getDataOffset();
            value4 = index2;
            ByteArrayReader byteArrayReader2 = byteArrayReader;
            NpcDefinition npcDefinition = NpcDefinition.forId(value4);
            while ((value3 = byteArrayReader2.readUnsignedByte()) != 0) {
                if (value3 == 1) {
                    value3 = byteArrayReader2.readUnsignedByte();
                    value2 = 0;
                    while (value2 < value3) {
                        byteArrayReader2.readUnsignedShort();
                        ++value2;
                    }
                    continue;
                }
                if (value3 == 2) {
                    npcDefinition.name = byteArrayReader2.readString();
                    continue;
                }
                if (value3 == 3) {
                    npcDefinition.description = new String(byteArrayReader2.readLineBytes());
                    continue;
                }
                if (value3 == 12) {
                    npcDefinition.size = byteArrayReader2.readByte();
                    if (value4 != 1431 && value4 != 1432) continue;
                    npcDefinition.size = 1;
                    continue;
                }
                if (value3 == 13) {
                    byteArrayReader2.readUnsignedShort();
                    continue;
                }
                if (value3 == 14) {
                    byteArrayReader2.readUnsignedShort();
                    continue;
                }
                if (value3 == 17) {
                    byteArrayReader2.readUnsignedShort();
                    byteArrayReader2.readUnsignedShort();
                    byteArrayReader2.readUnsignedShort();
                    byteArrayReader2.readUnsignedShort();
                    continue;
                }
                if (value3 >= 30 && value3 < 40) {
                    String text = byteArrayReader2.readString();
                    int actionSlot = value3 - 30;
                    if (actionSlot < npcDefinition.actions.length && !text.equalsIgnoreCase("hidden")) {
                        npcDefinition.actions[actionSlot] = text;
                    }
                    if (text.equalsIgnoreCase("attack") && npcDefinition.hitpoints > 0) {
                        npcDefinition.attackable = true;
                    }
                    continue;
                }
                if (value3 == 40) {
                    value3 = byteArrayReader2.readUnsignedByte();
                    value2 = 0;
                    while (value2 < value3) {
                        byteArrayReader2.readUnsignedShort();
                        byteArrayReader2.readUnsignedShort();
                        ++value2;
                    }
                    continue;
                }
                if (value3 == 60) {
                    value3 = byteArrayReader2.readUnsignedByte();
                    value2 = 0;
                    while (value2 < value3) {
                        byteArrayReader2.readUnsignedShort();
                        ++value2;
                    }
                    continue;
                }
                if (value3 == 90) {
                    byteArrayReader2.readUnsignedShort();
                    continue;
                }
                if (value3 == 91) {
                    byteArrayReader2.readUnsignedShort();
                    continue;
                }
                if (value3 == 92) {
                    byteArrayReader2.readUnsignedShort();
                    continue;
                }
                if (value3 == 93) continue;
                if (value3 == 95) {
                    npcDefinition.combatLevel = byteArrayReader2.readUnsignedShort();
                    continue;
                }
                if (value3 == 97) {
                    byteArrayReader2.readUnsignedShort();
                    continue;
                }
                if (value3 == 98) {
                    byteArrayReader2.readUnsignedShort();
                    continue;
                }
                if (value3 == 99) continue;
                if (value3 == 100) {
                    byteArrayReader2.readByte();
                    continue;
                }
                if (value3 == 101) {
                    byteArrayReader2.readByte();
                    continue;
                }
                if (value3 == 102) {
                    value3 = byteArrayReader2.readUnsignedShort();
                    if (value3 == 0) {
                        npcDefinition.protectedFromMelee = true;
                        continue;
                    }
                    if (value3 == 1) {
                        npcDefinition.protectedFromRanged = true;
                        continue;
                    }
                    if (value3 == 2) {
                        npcDefinition.protectedFromMagic = true;
                        continue;
                    }
                    if (value3 != 6) continue;
                    npcDefinition.protectedFromRanged = true;
                    npcDefinition.protectedFromMagic = true;
                    continue;
                }
                if (value3 == 103) {
                    byteArrayReader2.readUnsignedShort();
                    continue;
                }
                if (value3 != 106) continue;
                byteArrayReader2.readUnsignedShort();
                byteArrayReader2.readUnsignedShort();
                value3 = byteArrayReader2.readUnsignedByte();
                value2 = 0;
                while (value2 <= value3) {
                    byteArrayReader2.readUnsignedShort();
                    ++value2;
                }
            }
            ++index2;
        }
        NpcDefinition.initializeCombatDefinitions();
        NpcDefinition.copyDefinition(2257, 2258);
        NpcDefinition.copyDefinition(2260, 2261);
    }

    private static void copyDefinition(int value3, int value22) {
        NpcDefinition npcDefinition = NpcDefinition.forId(value3);
        NpcDefinition npcDefinition2 = NpcDefinition.forId(value22);
        npcDefinition.respawnDelaySeconds = npcDefinition2.respawnDelaySeconds;
        npcDefinition.aggressionType = npcDefinition2.aggressionType;
        npcDefinition.maxHit = npcDefinition2.maxHit;
        npcDefinition.hitpoints = npcDefinition2.hitpoints;
        npcDefinition.attackDelay = npcDefinition2.attackDelay;
        npcDefinition.attackAnimationId = npcDefinition2.attackAnimationId;
        npcDefinition.blockAnimationId = npcDefinition2.blockAnimationId;
        npcDefinition.deathAnimationId = npcDefinition2.deathAnimationId;
        npcDefinition.attackSoundId = npcDefinition2.attackSoundId;
        npcDefinition.hitSoundId = npcDefinition2.hitSoundId;
        npcDefinition.deathSoundId = npcDefinition2.deathSoundId;
        npcDefinition.legacyAttackBonus = npcDefinition2.legacyAttackBonus;
        npcDefinition.legacyMeleeDefenceBonus = npcDefinition2.legacyMeleeDefenceBonus;
        npcDefinition.legacyRangedDefenceBonus = npcDefinition2.legacyRangedDefenceBonus;
        npcDefinition.legacyMagicDefenceBonus = npcDefinition2.legacyMagicDefenceBonus;
        npcDefinition.shopId = npcDefinition2.shopId;
        npcDefinition.attackBonusTypeId = npcDefinition2.attackBonusTypeId;
        npcDefinition.respawnDelayTicks = npcDefinition2.respawnDelayTicks;
        npcDefinition.attackLevel = npcDefinition2.attackLevel;
        npcDefinition.strengthLevel = npcDefinition2.strengthLevel;
        npcDefinition.defenceLevel = npcDefinition2.defenceLevel;
        npcDefinition.magicLevel = npcDefinition2.magicLevel;
        npcDefinition.rangedLevel = npcDefinition2.rangedLevel;
        npcDefinition.meleeAttackBonus = npcDefinition2.meleeAttackBonus;
        npcDefinition.meleeStrengthBonus = npcDefinition2.meleeStrengthBonus;
        npcDefinition.magicAttackBonus = npcDefinition2.magicAttackBonus;
        npcDefinition.rangedAttackBonus = npcDefinition2.rangedAttackBonus;
        npcDefinition.defenceBonuses = npcDefinition2.defenceBonuses;
        npcDefinition.poisonDamage = npcDefinition2.poisonDamage;
        npcDefinition.poisonChance = npcDefinition2.poisonChance;
        npcDefinition.aggressionRange = npcDefinition2.aggressionRange;
        npcDefinition.spawnRadius = npcDefinition2.spawnRadius;
        npcDefinition.chaseRadius = npcDefinition2.chaseRadius;
        npcDefinition.name = npcDefinition2.name;
        npcDefinition.description = npcDefinition2.description;
        npcDefinition.actions = npcDefinition2.actions.clone();
    }

    private static void initializeCombatDefinitions() {
        initializeCombatDefinitionsControlExit1: {
            if (!ServerSettings.modernCombatSystemEnabled) break initializeCombatDefinitionsControlExit1;
            int index = 0;
            while (index < customDefinitionCount) {
                NpcCombatDefinition npcCombatDefinition;
                NpcDefinition npcDefinition;
                NpcDefinition npcDefinition2;
                initializeCombatDefinitionsControlExit2: {
                    initializeCombatDefinitionsControlExit3: {
                        initializeCombatDefinitionsControlExit4: {
                            npcDefinition = npcDefinition2 = NpcDefinition.forId(index);
                            boolean enabled = NpcCombatDefinition.isRegistered(npcDefinition2.id);
                            npcDefinition = npcDefinition2;
                            npcCombatDefinition = NpcCombatDefinition.forNpcId(npcDefinition.id);
                            if (enabled) break initializeCombatDefinitionsControlExit2;
                            NpcDefinition npcDefinition3 = npcDefinition2;
                            npcDefinition = npcDefinition3;
                            NpcDefinition npcDefinition4 = npcDefinition2;
                            npcDefinition = npcDefinition4;
                            NpcDefinition npcDefinition5 = npcDefinition2;
                            npcDefinition = npcDefinition5;
                            NpcDefinition npcDefinition6 = npcDefinition2;
                            npcDefinition = npcDefinition6;
                            NpcDefinition npcDefinition7 = npcDefinition2;
                            npcDefinition = npcDefinition7;
                            npcDefinition = npcDefinition2;
                            npcCombatDefinition = new NpcDefinitionAttackStyleCombatDefinition(npcDefinition2).setRespawnDelayTicks(npcDefinition3.respawnDelayTicks).addAttackBonuses(npcDefinition4.meleeAttackBonus, npcDefinition5.meleeAttackBonus, npcDefinition6.meleeAttackBonus, npcDefinition7.magicAttackBonus, npcDefinition.rangedAttackBonus).addDefenceBonuses(npcDefinition2.getDefenceBonus(5), npcDefinition2.getDefenceBonus(6), npcDefinition2.getDefenceBonus(7), npcDefinition2.getDefenceBonus(8), npcDefinition2.getDefenceBonus(9));
                            npcDefinition = npcDefinition2;
                            if (npcDefinition.id == 919) break initializeCombatDefinitionsControlExit4;
                            npcDefinition = npcDefinition2;
                            if (npcDefinition.id == 920) break initializeCombatDefinitionsControlExit4;
                            npcDefinition = npcDefinition2;
                            if (npcDefinition.id == 385) break initializeCombatDefinitionsControlExit4;
                            npcDefinition = npcDefinition2;
                            if (npcDefinition.id == 386) break initializeCombatDefinitionsControlExit4;
                            npcDefinition = npcDefinition2;
                            if (npcDefinition.id == 387) break initializeCombatDefinitionsControlExit4;
                            npcDefinition = npcDefinition2;
                            if (npcDefinition.id != 759) break initializeCombatDefinitionsControlExit3;
                        }
                        npcCombatDefinition.setRespawnDelayTicks(150);
                    }
                    npcDefinition = npcDefinition2;
                    if (npcDefinition.name.toLowerCase().equals("fishing spot")) {
                        npcCombatDefinition.setRespawnDelayTicks(50);
                    }
                }
                int[] integerValues = new int[1];
                npcDefinition = npcDefinition2;
                integerValues[0] = npcDefinition.id;
                NpcCombatDefinition.register(integerValues, npcCombatDefinition);
                ++index;
            }
            return;
        }
        int index2 = 0;
        while (index2 < customDefinitionCount) {
            NpcCombatDefinition npcCombatDefinition;
            NpcDefinition npcDefinition;
            NpcDefinition npcDefinition8;
            initializeCombatDefinitionsControlExit5: {
                initializeCombatDefinitionsControlExit6: {
                    initializeCombatDefinitionsControlExit7: {
                        initializeCombatDefinitionsControlExit8: {
                            npcDefinition8 = NpcDefinition.forId(index2);
                            if ((index2 < 1290 || index2 > 1293) && npcDefinition8.hitpoints > 0) {
                                if (npcDefinition8.legacyAttackBonus != 0) {
                                    npcDefinition8.legacyAttackBonus = npcDefinition8.hitpoints;
                                }
                                if (npcDefinition8.legacyMeleeDefenceBonus != 0) {
                                    npcDefinition8.legacyMeleeDefenceBonus = npcDefinition8.hitpoints;
                                }
                                if (npcDefinition8.legacyMagicDefenceBonus != 0) {
                                    npcDefinition8.legacyMagicDefenceBonus = npcDefinition8.hitpoints;
                                }
                                if (npcDefinition8.legacyRangedDefenceBonus != 0) {
                                    npcDefinition8.legacyRangedDefenceBonus = npcDefinition8.hitpoints;
                                }
                            }
                            npcDefinition = npcDefinition8;
                            boolean enabled2 = NpcCombatDefinition.isRegistered(npcDefinition.id);
                            npcDefinition = npcDefinition8;
                            npcCombatDefinition = NpcCombatDefinition.forNpcId(npcDefinition.id);
                            if (!enabled2) break initializeCombatDefinitionsControlExit8;
                            if (!npcCombatDefinition.hasAttackBonuses() && !npcCombatDefinition.hasDefenceBonuses()) {
                                npcCombatDefinition = npcCombatDefinition.addAttackBonuses(npcDefinition8.legacyAttackBonus, npcDefinition8.legacyAttackBonus, npcDefinition8.legacyAttackBonus, npcDefinition8.legacyAttackBonus, npcDefinition8.legacyAttackBonus).addDefenceBonuses(npcDefinition8.legacyMeleeDefenceBonus, npcDefinition8.legacyMeleeDefenceBonus, npcDefinition8.legacyMeleeDefenceBonus, npcDefinition8.legacyMagicDefenceBonus, npcDefinition8.legacyRangedDefenceBonus);
                            } else if (!npcCombatDefinition.hasAttackBonuses()) {
                                npcCombatDefinition = npcCombatDefinition.addAttackBonuses(npcDefinition8.legacyAttackBonus, npcDefinition8.legacyAttackBonus, npcDefinition8.legacyAttackBonus, npcDefinition8.legacyAttackBonus, npcDefinition8.legacyAttackBonus);
                            } else if (!npcCombatDefinition.hasDefenceBonuses()) {
                                npcCombatDefinition = npcCombatDefinition.addDefenceBonuses(npcDefinition8.legacyMeleeDefenceBonus, npcDefinition8.legacyMeleeDefenceBonus, npcDefinition8.legacyMeleeDefenceBonus, npcDefinition8.legacyMagicDefenceBonus, npcDefinition8.legacyRangedDefenceBonus);
                            }
                            break initializeCombatDefinitionsControlExit5;
                        }
                        npcCombatDefinition = new NpcDefinitionMeleeCombatDefinition(npcDefinition8).setRespawnDelaySeconds(npcDefinition8.respawnDelaySeconds).addAttackBonuses(npcDefinition8.legacyAttackBonus, npcDefinition8.legacyAttackBonus, npcDefinition8.legacyAttackBonus, npcDefinition8.legacyAttackBonus, npcDefinition8.legacyAttackBonus).addDefenceBonuses(npcDefinition8.legacyMeleeDefenceBonus, npcDefinition8.legacyMeleeDefenceBonus, npcDefinition8.legacyMeleeDefenceBonus, npcDefinition8.legacyMagicDefenceBonus, npcDefinition8.legacyRangedDefenceBonus);
                        npcDefinition = npcDefinition8;
                        if (npcDefinition.id == 919) break initializeCombatDefinitionsControlExit7;
                        npcDefinition = npcDefinition8;
                        if (npcDefinition.id == 920) break initializeCombatDefinitionsControlExit7;
                        npcDefinition = npcDefinition8;
                        if (npcDefinition.id == 385) break initializeCombatDefinitionsControlExit7;
                        npcDefinition = npcDefinition8;
                        if (npcDefinition.id == 386) break initializeCombatDefinitionsControlExit7;
                        npcDefinition = npcDefinition8;
                        if (npcDefinition.id == 387) break initializeCombatDefinitionsControlExit7;
                        npcDefinition = npcDefinition8;
                        if (npcDefinition.id != 759) break initializeCombatDefinitionsControlExit6;
                    }
                    npcCombatDefinition.setRespawnDelayTicks(150);
                }
                npcDefinition = npcDefinition8;
                if (npcDefinition.name.toLowerCase().equals("fishing spot")) {
                    npcCombatDefinition.setRespawnDelayTicks(50);
                }
            }
            int[] integerValues2 = new int[1];
            npcDefinition = npcDefinition8;
            integerValues2[0] = npcDefinition.id;
            NpcCombatDefinition.register(integerValues2, npcCombatDefinition);
            ++index2;
        }
    }

    public static boolean isDefined(int value2) {
        return value2 >= 0 && (value2 < cacheDefinitionCount || value2 < customDefinitionCount);
    }

    /** Applies stock 443 display data over the server's combat/shop metadata. */
    public static void loadRevision443() throws IOException {
        NpcAnimations.load();
        Map<Integer, byte[]> files = Definitions.readGroup(9);
        int maxId = -1;
        for (Integer id : files.keySet()) maxId = Math.max(maxId, id);
        cacheDefinitionCount = maxId + 1;
        if (cacheDefinitionCount > World.getNpcDefinitions().length) {
            throw new IOException("443 NPC definitions exceed world capacity");
        }
        for (Map.Entry<Integer, byte[]> entry : files.entrySet()) {
            int id = entry.getKey();
            NpcDefinition definition = World.getNpcDefinitions()[id];
            if (definition == null) {
                definition = createFallback(id);
                World.getNpcDefinitions()[id] = definition;
            }
            definition.actions = new String[5];
            definition.description = null;
            definition.size = 1;
            definition.combatLevel = 0;
            definition.attackable = false;
            decodeRevision443(definition, entry.getValue());
            // These event NPC IDs differ from the older server examine dataset.
            switch (id) {
                case 828:
                    definition.description = "A sea shanty-singing werewolf wearing a Santa Claus robe.";
                    break;
                case 5017:
                    definition.description = "A sweaty gublinch.";
                    break;
                case 5018:
                    definition.description = "A chilled, smelly gublinch.";
                    break;
                case 5019:
                    definition.description = "A freezing cold gublinch.";
                    break;
            }
        }
    }

    private static void decodeRevision443(NpcDefinition definition, byte[] data)
            throws IOException {
        ConfigReader reader = new ConfigReader(data);
        int idleAnimationId = -1;
        int walkAnimationId = -1;
        while (reader.position() < reader.length()) {
            int opcode = reader.readUnsignedByte();
            if (opcode == 0) {
                if (reader.position() != reader.length()) {
                    throw new IOException("Trailing bytes in 443 NPC " + definition.id);
                }
                int modelAnimationId = idleAnimationId >= 0 ? idleAnimationId : walkAnimationId;
                definition.attackAnimationId = NpcAnimations.resolve(
                        modelAnimationId, definition.attackAnimationId, 0);
                definition.blockAnimationId = NpcAnimations.resolve(
                        modelAnimationId, definition.blockAnimationId, 1);
                definition.deathAnimationId = NpcAnimations.resolve(
                        modelAnimationId, definition.deathAnimationId, 2);
                return;
            }
            if (opcode == 1 || opcode == 60) {
                int count = reader.readUnsignedByte();
                reader.skip(count * 2);
            } else if (opcode == 2) {
                definition.name = reader.readString();
            } else if (opcode == 3) {
                definition.description = reader.readString();
            } else if (opcode == 12) {
                definition.size = reader.readUnsignedByte();
                if (definition.id == 1431 || definition.id == 1432) definition.size = 1;
            } else if (opcode == 13) {
                idleAnimationId = reader.readUnsignedShort();
            } else if (opcode == 14) {
                walkAnimationId = reader.readUnsignedShort();
            } else if (opcode == 90 || opcode == 91
                    || opcode == 92 || opcode == 97 || opcode == 98 || opcode == 103) {
                reader.readUnsignedShort();
            } else if (opcode == 17) {
                walkAnimationId = reader.readUnsignedShort();
                reader.skip(6);
            } else if (opcode >= 30 && opcode < 35) {
                String action = reader.readString();
                if (!"hidden".equalsIgnoreCase(action)) {
                    definition.actions[opcode - 30] = action;
                    if ("attack".equalsIgnoreCase(action) && definition.hitpoints > 0) {
                        definition.attackable = true;
                    }
                }
            } else if (opcode == 40 || opcode == 41) {
                int count = reader.readUnsignedByte();
                reader.skip(count * 4);
            } else if (opcode == 93 || opcode == 99 || opcode == 107 || opcode == 109
                    || opcode == 111) {
                // Boolean config flags.
            } else if (opcode == 95) {
                definition.combatLevel = reader.readUnsignedShort();
            } else if (opcode == 100 || opcode == 101) {
                reader.readByte();
            } else if (opcode == 102) {
                int icon = reader.readUnsignedShort();
                definition.protectedFromMelee = icon == 0;
                definition.protectedFromRanged = icon == 1 || icon == 6;
                definition.protectedFromMagic = icon == 2 || icon == 6;
            } else if (opcode == 106 || opcode == 118) {
                reader.readUnsignedShort();
                reader.readUnsignedShort();
                if (opcode == 118) reader.readUnsignedShort();
                int count = reader.readUnsignedByte();
                reader.skip((count + 1) * 2);
            } else if (opcode == 249) {
                int count = reader.readUnsignedByte();
                for (int i = 0; i < count; i++) {
                    boolean stringValue = reader.readUnsignedByte() == 1;
                    reader.skip(3);
                    if (stringValue) reader.readString(); else reader.readInt();
                }
            } else {
                throw new IOException("Unsupported 443 NPC opcode " + opcode
                        + " for NPC " + definition.id + " at byte "
                        + (reader.position() - 1));
            }
        }
        throw new IOException("Unterminated 443 NPC " + definition.id);
    }

    public static NpcDefinition forId(int value2) {
        NpcDefinition npcDefinition = World.getNpcDefinitions()[value2];
        if (npcDefinition == null) {
            npcDefinition = NpcDefinition.createFallback(value2);
        }
        if (ServerSettings.clientBuild == 443 && value2 == 3863) {
            npcDefinition.name = "Grand Exchange Clerk";
            npcDefinition.description = "A clerk who can help me trade on the Grand Exchange.";
            npcDefinition.actions = new String[] {"Talk-to", "Exchange", null, null, null};
        }
        return npcDefinition;
    }

    public final boolean isProtectedFromMelee() {
        return this.protectedFromMelee;
    }

    public final boolean isProtectedFromRanged() {
        return this.protectedFromRanged;
    }

    public final boolean isProtectedFromMagic() {
        return this.protectedFromMagic;
    }

    public final int getId() {
        return this.id;
    }

    public final int getHitSoundId() {
        // The inherited content table gives the basic men female_hit.
        if ("Man".equalsIgnoreCase(this.name) && this.hitSoundId == 73) {
            return 72;
        }
        if (this.hitSoundId == 0) {
            return -1;
        }
        return this.hitSoundId;
    }

    public final int getAttackSoundId() {
        if (this.attackSoundId == 0) {
            return -1;
        }
        return this.attackSoundId;
    }

    public final int getDropTableNpcIdOverride() {
        return this.dropTableNpcIdOverride;
    }

    public final void setDropTableNpcIdOverride(int npcId) {
        this.dropTableNpcIdOverride = npcId;
    }

    public final int getDeathSoundId() {
        if ("Man".equalsIgnoreCase(this.name) && this.deathSoundId == 71) {
            return 70;
        }
        if (this.deathSoundId == 0) {
            return -1;
        }
        return this.deathSoundId;
    }

    public final int getPoisonDamage() {
        return this.poisonDamage;
    }

    public final double getPoisonChance() {
        return this.poisonChance;
    }

    public final int getShopId() {
        return this.shopId;
    }

    public final String getName() {
        return this.name;
    }

    public final String getDescription() {
        return HistoricalExamines.resolve("npc", id, name, description);
    }

    public final String getAction(int actionSlot) {
        if (actionSlot < 0 || actionSlot >= this.actions.length) {
            return null;
        }
        return this.actions[actionSlot];
    }

    public final boolean actionStartsWith(int actionSlot, String prefix) {
        String action = this.getAction(actionSlot);
        return action != null
                && prefix != null
                && action.regionMatches(true, 0, prefix, 0, prefix.length());
    }

    public final int getDeathAnimationId() {
        return this.deathAnimationId;
    }

    public final int getBlockAnimationId() {
        return this.blockAnimationId;
    }

    public final int getAttackAnimationId() {
        return this.attackAnimationId;
    }

    public final int resolveAttackAnimationId(int animationId) {
        return ServerSettings.cacheVersion == 443
                ? NpcAnimations.resolveAttack(this.attackAnimationId, animationId)
                : animationId;
    }

    public final int getCombatLevel() {
        return this.combatLevel;
    }

    public final int getSize() {
        return this.size;
    }

    public final int getAggressionType() {
        return this.aggressionType;
    }

    public final int getHitpoints() {
        return this.hitpoints;
    }

    public final int getMaxHit() {
        return this.maxHit;
    }

    public final int getDefenceLevel() {
        return this.defenceLevel;
    }

    public final int getMagicLevel() {
        return this.magicLevel;
    }

    public final int getRangedLevel() {
        return this.rangedLevel;
    }

    public final int getAttackLevel() {
        return this.attackLevel;
    }

    public final int getStrengthLevel() {
        return this.strengthLevel;
    }

    public final int getRespawnDelayTicks() {
        return this.respawnDelayTicks;
    }

    public final int getDefenceBonus(int value2) {
        if (this.defenceBonuses == null) {
            this.defenceBonuses = new int[5];
        }
        if (value2 >= 5) {
            value2 -= 5;
        }
        return this.defenceBonuses[value2];
    }

    public final int getMeleeAttackBonus() {
        return this.meleeAttackBonus;
    }

    public final int getMeleeStrengthBonus() {
        return this.meleeStrengthBonus;
    }

    public final int getMagicAttackBonus() {
        return this.magicAttackBonus;
    }

    public final int getRangedAttackBonus() {
        return this.rangedAttackBonus;
    }

    public final int getAggressionRange() {
        return this.aggressionRange;
    }

    public final int getSpawnRadius() {
        return this.spawnRadius;
    }

    public final int getChaseRadius() {
        return this.chaseRadius;
    }

    public static NpcDefinition createFallback(int value2) {
        NpcDefinition npcDefinition = new NpcDefinition();
        npcDefinition.id = value2;
        npcDefinition.name = value2 == 3863 ? "Grand Exchange Clerk" : "NPC #" + value2;
        return npcDefinition;
    }

    public final boolean isAttackable() {
        return this.attackable;
    }

    public static int getAttackBonusTypeId(NpcDefinition npcDefinition) {
        return npcDefinition.attackBonusTypeId;
    }

    public static int getDefaultMaxHit(NpcDefinition npcDefinition) {
        return npcDefinition.maxHit;
    }

    public static int getDefaultAttackDelay(NpcDefinition npcDefinition) {
        return npcDefinition.attackDelay;
    }

    public static int getDefaultAttackAnimationId(NpcDefinition npcDefinition) {
        return npcDefinition.attackAnimationId;
    }
}
