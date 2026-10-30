package com.rs2.model.player;

import java.util.ArrayList;

public final class CharacterFileBankTab {
    ArrayList items = new ArrayList();

    private CharacterFileBankTab() {
    }

    public final ArrayList getItems() {
        return this.items;
    }

    /** Used when reading a complete bank tab from an editable JSON character save. */
    public static CharacterFileBankTab fromItems(java.util.List items) {
        CharacterFileBankTab tab = new CharacterFileBankTab(0);
        tab.items.addAll(items);
        return tab;
    }

    CharacterFileBankTab(byte value2) {
        this();
    }

    CharacterFileBankTab(int value2) {
        this();
    }
}

