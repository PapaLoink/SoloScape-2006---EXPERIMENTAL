package unpackaged;

import jagex.io.JSocket;

/** Native cache 307 has no food-name text field; client must add child 8. */
public final class CookingChatboxWidgetChecks {
    public static void main(String[] args) {
        Class39_Sub5_Sub4.widgetsLoaded = new boolean[500];
        Class62_Sub1.widgets = new Widget[500][];

        Widget[] cached = new Widget[8];
        for (int child = 0; child < cached.length; child++) {
            cached[child] = new Widget();
            cached[child].anInt2084 = 307 << 16 | child;
            cached[child].anInt2050 = -1;
            cached[child].type = child < 2 ? 5 : child == 2 ? 6 : 4;
        }
        cached[7].aClass3_2029 = Class39_Sub5_Sub9.createJstring(
                "How many would you like to cook?");
        cached[7].anInt2105 = 496;
        Class62_Sub1.widgets[307] = cached;
        Class39_Sub5_Sub4.widgetsLoaded[307] = true;

        require(JSocket.loadWidget(307), "Unable to load native cooking interface");
        Widget[] actual = Class62_Sub1.widgets[307];
        require(actual.length == 9 && actual[8] != null, "Food label not created");
        Widget name = actual[8];
        require(name.type == 4, "Food label is not a text widget");
        require(name.anInt2084 == (307 << 16 | 8), "Food label id incorrect");
        require(name.anInt2050 == -1, "Food label is not attached to chatbox root");
        require(name.anInt2021 == 96 && name.quadHeight == 17,
                "Food label is not below the item model");
        require(name.anInt2032 == 1 && name.activeQuadColor == 0,
                "Food label not centered in black");
        for (int child = 0; child < 8; child++) {
            require(actual[child] == cached[child],
                    "Native cooking child " + child + " was overwritten");
        }
        require(Class37.getWidget(307 << 16 | 8) == name,
                "The native text-update path cannot address 307:8");

        // A second interface open must preserve the food-name widget and text.
        name.aClass3_2029 = Class39_Sub5_Sub9.createJstring("Raw chicken");
        require(JSocket.loadWidget(307), "Cooking chatbox could not be reopened");
        require(Class62_Sub1.widgets[307][8] == name
                && name.aClass3_2029.method97(
                        Class39_Sub5_Sub9.createJstring("Raw chicken"), -66),
                "Reopening cooking reset the food name");

        System.out.println("Cooking chatbox widget checks passed.");
    }

    private static void require(boolean valid, String message) {
        if (!valid) throw new AssertionError(message);
    }
}
