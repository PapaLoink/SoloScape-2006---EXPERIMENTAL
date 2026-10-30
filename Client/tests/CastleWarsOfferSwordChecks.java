package unpackaged;

import jagex.io.JSocket;

/** Ensures Castle Wars replacement heading decorations frame the text
 * without changing the text or dialogue button positions.
 */
public final class CastleWarsOfferSwordChecks {
    public static void main(String[] args) {
        Class62_Sub1.widgets = new Widget[500][];
        Widget[] group = new Widget[6];
        group[0] = widget(4, 180, 14, 260, 20);
        group[1] = widget(4, 180, 44, 260, 20);
        group[2] = widget(4, 180, 76, 260, 20);
        group[3] = widget(5, 112, 16, 16, 16);
        group[4] = widget(5, 438, 16, 16, 16);
        group[5] = widget(5, 250, 72, 16, 16); // unrelated lower decoration
        Class62_Sub1.widgets[228] = group;

        group[0].aClass3_2029 = Class39_Sub5_Sub9.createJstring(
                "A place is available on the Saradomin team.");
        JSocket.configureCastleWarsOfferSwords();
        require(group[3].anInt2091 == 88 && group[4].anInt2091 == 462,
                "The two heading ornaments were not moved outward");
        require(group[0].anInt2091 == 180 && group[0].anInt2021 == 14,
                "The red title position changed");
        require(group[1].anInt2091 == 180 && group[2].anInt2091 == 180,
                "The clickable dialogue options moved");
        require(group[5].anInt2091 == 250,
                "An unrelated lower sprite moved");

        JSocket.configureCastleWarsOfferSwords();
        require(group[3].anInt2091 == 88 && group[4].anInt2091 == 462,
                "Reopening the offer drifted the swords");

        group[0].aClass3_2029 = Class39_Sub5_Sub9.createJstring(
                "Select an Option");
        JSocket.configureCastleWarsOfferSwords();
        require(group[3].anInt2091 == 112 && group[4].anInt2091 == 438,
                "Other dialogues did not regain their original icon positions");
        System.out.println("Castle Wars sword layout checks passed");
    }

    private static Widget widget(int type, int x, int y, int width, int height) {
        Widget widget = new Widget();
        widget.type = type;
        widget.anInt2090 = widget.anInt2091 = x;
        widget.anInt2024 = widget.anInt2021 = y;
        widget.quadWidth = width;
        widget.quadHeight = height;
        return widget;
    }

    private static void require(boolean valid, String message) {
        if (!valid) throw new AssertionError(message);
    }
}
