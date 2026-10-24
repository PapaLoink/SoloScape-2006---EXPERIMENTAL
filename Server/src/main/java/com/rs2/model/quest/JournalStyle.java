package com.rs2.model.quest;

import java.util.Arrays;
import java.util.Comparator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Period quest journal colours: dark blue text with NPCs, items and locations in dark red.
 * Journals that already choose their own colours (red, struck-through, etc.) are left as written.
 */
public final class JournalStyle {
    private static final String TEXT = "@dbl@", KEY = "@dre@";

    /** Lines carrying any of these tags were coloured by hand and are not touched. */
    private static final Pattern AUTHORED = Pattern.compile("@(str|dre|red|blu|yel|whi|gre|cya|lre|mag|bla|or[0-9]|gr[0-9])@");

    private static final String[] TERMS = {
        // NPCs
        "Sir Amik Varze", "Brother Kojo", "Cook", "Gypsy", "Sir Prysin", "Captain Rovin", "Wizard Traiborn",
        "Doric", "Guild master", "Guildmaster", "Oziach", "Kaqemeex", "Sanfew", "Veronica", "Ernest",
        "Professor Oddenstein", "Dimintheis", "Caleb", "Avan", "Gem Trader", "Boot", "Johnathon", "Chronozon",
        "Chieftain Brundt", "Brundt", "Manni the Reveller", "Olaf the Bard", "Sigmund the Merchant",
        "Sigli the Huntsman", "Swensen the Navigator", "Peer the Seer", "Thorvald the Warrior", "Gertrude",
        "Fluffs", "boys", "Generals Wartface", "Bentnoze", "King Narnode Shareen", "King Narnode", "King Shareen",
        "Hazelmere", "Glough", "Charlie the Cook", "Charlie the Tramp", "Charlie", "Anita", "Achietties",
        "Katrine", "Straven", "Hartigen the Black Knight", "Grip", "Pete", "King Arthur", "Merlin", "Sir Galahad",
        "Fisher King", "Percival", "Wizard Mizgog", "Trufitus Shakaya", "Trufitus", "Squire", "Reldo", "Thurgo",
        "Sir Vyvin", "Imcando", "Adventurers", "leprechaun", "Morgan Le Faye", "Sir Lancelot", "Arhein",
        "Candle maker", "Lady of the lake", "Daero", "Waydar", "Zooknock", "Garkor", "Awowogei", "Kruk",
        "Redbeard Frank", "Luthas", "Hector", "Edmond", "Elena", "Alrena", "Jethick", "Rehnison Family",
        "Rehnisons", "Milli", "Head Mourner", "Bravek", "King Roald", "Drezel", "vampyre", "vampire", "Hassan",
        "Osman", "Leela", "Prince Ali", "prince", "Prison guard Joe", "Lady Keli", "Father Aereck",
        "Father Urhney", "Ghost", "Romeo", "Juliet", "Father Lawrence", "Apothecary", "Duke Horacio",
        "head wizard", "Aubury", "Thormac", "Councillor Halgrive", "Dr Orbon", "Farmer Fred", "Baraek",
        "Jonny the Beard", "curator", "King Bolren", "Bolren", "Commander Montai", "tracker gnomes",
        "Tracker gnome 1", "Tracker gnome 2", "Tracker gnome 3", "Khazard warlord", "Morgan", "Dr Harlow",
        "little boy", "boy", "Hetty", "Black Knight Titan", "Ice Warriors", "Jungle Demon", "King",
        // Locations
        "White Knights' Castle", "Falador", "Black Knights' fortress", "Clock Tower", "East Ardougne",
        "West Ardougne", "Ardougne Zoo", "Ardougne", "Lumbridge Castle", "Lumbridge swamp", "Lumbridge",
        "Kitchen", "Varrock's main square", "Varrock marketplace", "Varrock museum", "Palace of Varrock",
        "Varrock", "Champions' Guild", "Edgeville", "Crandor", "Druids Circle", "Taverley", "Seers village",
        "village of the Seers", "Draynor Manor", "Draynor Village", "White Wolf Mountain", "Wilderness",
        "Obelisk of Air", "Fremennik Longhall", "Rellekka", "Sinclair Mansion", "lumber mill", "Jolly Boar Inn",
        "Goblin Village", "Grand Tree", "Gnome Stronghold", "Yanille", "Karamja Shipyard", "Karamja shipyard",
        "Shilo Village", "toad swamp", "Heroes' Guild", "Entrana", "Brimhaven", "mansion", "Camelot Castle",
        "Camelot", "Catherby", "McGrubor's Wood", "Fisher King's realm", "Wizards' Tower", "Wizard's Tower",
        "Tai Bwo Wannai", "Karamja", "Asgarnia", "Swamp", "Zanaris", "Port Sarim", "chaos altars", "hangar",
        "Blue Moon Inn", "Monastery", "Plague Houses", "Plague House", "sewers", "Edmond's garden", "temple",
        "Al-Kharid", "Sorcerer's Tower", "Rimmington", "Tree Gnome maze", "Port Khazard", "battlefield",
        "Phoenix Gang HQ", "Black Arm Gang HQ", "Phoenix Gang", "Black Arm Gang", "Black Arm", "Phoenix",
        "graveyard crypt", "crypt", "church", "rune shop", "library", "palace", "desert", "cauldron of thunder",
        "Hetty's house", "witches house", "Thurgo's house", "Glough's house", "Glough's home", "Glough's chest",
        // Items
        "bronze med helm", "iron chainbody", "cabbage", "Bucket of milk", "Pot of flour", "Egg", "Silverlight",
        "keys", "Key", "bones", "Clay", "Copper ore", "iron ore", "Raw bear meat", "Raw rat meat", "Raw beef",
        "Raw chicken", "meats", "Pressure gauge", "Rubber tube", "Oil can", "Swordfish", "Bass", "Tuna",
        "Salmon", "Shrimps", "crest piece", "family crest", "ruby ring", "ruby necklace", "perfect gold",
        "antipoison", "bark sample", "twigs", "Daconia rock", "invasion plans", "Entranan Firebird Feather",
        "cooked lava eel", "Master Thieves Armband", "Pete's Candlestick", "candlestick", "magic whistle",
        "magic gold feather", "Holy Grail", "White bead", "Red bead", "Black bead", "Yellow bead", "snake weed",
        "ardrigal", "sito foil", "volencia moss", "rogues purse", "redberry pies", "redberry pie",
        "picture of the sword", "Iron Bars", "Blurite Ore", "sword", "dramen tree", "dramen staff",
        "Lit black candle", "black candle", "bucket full of wax", "Excalibur", "magic words", "Bat bones",
        "crystal", "Gold bar", "Monkey amulet mould", "squad sigil", "amulet", "talisman",
        "bottle of karamjan rum", "karamjan rum", "crate of bananas", "bananas", "crate", "white apron",
        "Dwellberries", "Gasmask", "rope", "buckets of water", "Hangover Cure", "warrant", "water of the Salve",
        "rune essence", "essences", "Pink skirt", "Skirt", "Blonde wig", "Skin paste", "bronze bar",
        "imprint of the key", "beer", "air talisman", "package", "research notes", "Kharid scorpions",
        "scorpions", "poisoned sheep feed", "protective clothing", "prod", "Ball of wool", "book",
        "intelligence report", "shield halves", "shield", "Phoenix crossbows", "certificate", "Logs", "orbs",
        "orb", "ballista", "Garlic", "Stake", "Hammer", "ball", "diary", "Eye of newt", "Rat's tail", "Onion",
        "Burnt meat", "cauldron", "orange goblin armour", "blue goblin armour", "brown goblin armour",
        "Cadava potion", "Cadava berries", "message", "Ghost's Skull", "skull", "Blue Cog", "Black Cog",
        "White Cog", "Red Cog", "cogs", "spindles", "kitten", "Amulet of Accuracy",
    };

    /** Quest and reward names that contain a term but stay in the text colour. */
    private static final String[] PLAIN = {
        "Shield of Arrav", "Merlin's Crystal", "Lost City", "Dragon Slayer", "Druidic Ritual", "The Grand Tree",
        "Tree Gnome Village", "Doric's Anvils",
    };

    private static final java.util.Set<String> PLAIN_SET = new java.util.TreeSet<String>(String.CASE_INSENSITIVE_ORDER);
    private static final Pattern TERM;
    static {
        PLAIN_SET.addAll(Arrays.asList(PLAIN));
        String[] sorted = Arrays.copyOf(TERMS, TERMS.length + PLAIN.length);
        System.arraycopy(PLAIN, 0, sorted, TERMS.length, PLAIN.length);
        Arrays.sort(sorted, Comparator.comparingInt(String::length).reversed());   // longest name wins
        StringBuilder alternatives = new StringBuilder();
        for (String term : sorted) {
            if (alternatives.length() > 0) alternatives.append('|');
            alternatives.append(Pattern.quote(term));
        }
        TERM = Pattern.compile("(?<![\\w'])(?:" + alternatives + ")(?![\\w])", Pattern.CASE_INSENSITIVE);
    }

    private JournalStyle() {
    }

    public static String format(String line) {
        if (line == null || line.trim().isEmpty() || AUTHORED.matcher(line).find()) return line;
        String text = line.replace(TEXT, "");
        if (text.trim().equalsIgnoreCase("Quest Completed!") || text.trim().equalsIgnoreCase("QUEST COMPLETE!")) {
            return "@red@" + text;
        }
        Matcher matcher = TERM.matcher(text);
        StringBuffer out = new StringBuffer(TEXT);
        while (matcher.find()) {
            String term = matcher.group();
            matcher.appendReplacement(out, Matcher.quoteReplacement(PLAIN_SET.contains(term) ? term : KEY + term + TEXT));
        }
        matcher.appendTail(out);
        String styled = out.toString();
        return styled.endsWith(TEXT) ? styled.substring(0, styled.length() - TEXT.length()) : styled;
    }
}
