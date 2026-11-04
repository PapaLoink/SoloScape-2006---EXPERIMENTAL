import com.rs2.cache.js5.Interfaces;
import java.util.Map;
import java.util.Locale;

/** Native revision-443 Castle Wars book/scoreboard/dialogue discovery. */
public final class CastleWarsNativePanelProbe {
    private static String printable(byte[] data) {
        StringBuilder found = new StringBuilder();
        StringBuilder piece = new StringBuilder();
        for (byte value : data) {
            int v = value & 255;
            if (v >= 32 && v <= 126) piece.append((char)v);
            else {
                if (piece.length() >= 5) found.append(" [").append(piece).append("]");
                piece.setLength(0);
            }
        }
        if (piece.length() >= 5) found.append(" [").append(piece).append("]");
        return found.toString();
    }

    public static void main(String[] args) throws Exception {
        Interfaces.load();
        System.out.println("NATIVE-PANEL-PROBE groups=" + Interfaces.groupCount());
        for (int group : Interfaces.groupIds()) {
            Map<Integer, Interfaces.Component> widgets = Interfaces.group(group);
            for (Interfaces.Component c : widgets.values()) {
                String words = printable(c.getData()).toLowerCase(Locale.ROOT);
                if (words.contains("castle") || words.contains("wins this") ||
                        words.contains("scoreboard") || words.contains("manual") ||
                        words.contains("sara") || words.contains("zamorak") ||
                        words.contains("next page") || words.contains("previous page") ||
                        words.contains("book of")) {
                    System.out.println("CANDIDATE " + group + ":" + c.childId +
                            " type=" + c.type + " xy=" + c.x + "," + c.y +
                            " size=" + c.width + "x" + c.height + " text=" + printable(c.getData()));
                }
            }
        }
        for (int group : new int[] {228, 59, 61, 73, 81, 124, 154, 160, 187, 217, 264, 275, 391}) {
            Map<Integer, Interfaces.Component> widgets = Interfaces.group(group);
            System.out.println("GROUP-PROBE "+group+" children="+widgets.size());
            if (group == 228 || group == 264 || group == 275 || group == 391 || group == 61 || group == 73) {
                for (Interfaces.Component c : widgets.values()) {
                    if (c.childId >= 90) break;
                    System.out.println("CHILD " + group + ":" + c.childId + " type="+c.type+
                        " xy="+c.x+","+c.y+" wh="+c.width+"x"+c.height+
                        " parent="+c.parentId+" action="+c.actionType +
                        " text="+printable(c.getData()));
                }
            }
        }
    }
}
