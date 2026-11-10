package com.rs2.model.quest;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The 443 quest journal does not wrap its lines, so LostCity entries that leave a long sentence to the client run
 * off the page. Lines longer than the page are broken at spaces, carrying the colour in effect onto the next line.
 */
public final class JournalWrap {
    /** Visible characters that fit on one journal line (LostCity's hand-broken lines run to about 57). */
    private static final int LINE_WIDTH = 58;
    private static final Pattern CODE = Pattern.compile("@[a-z0-9]{3}@");

    private JournalWrap() {
    }

    public static String[] wrap(String[] lines) {
        List<String> out = new ArrayList<String>();
        for (String line : lines) {
            if (line == null || CODE.matcher(line).replaceAll("").length() <= LINE_WIDTH) { out.add(line); continue; }
            StringBuilder cur = new StringBuilder();
            int visible = 0;
            String colour = "";
            for (String word : line.split(" ")) {
                int wordVisible = CODE.matcher(word).replaceAll("").length();
                if (visible > 0 && visible + 1 + wordVisible > LINE_WIDTH) {
                    out.add(cur.toString());
                    cur.setLength(0);
                    cur.append(colour);
                    visible = 0;
                }
                if (visible > 0) { cur.append(' '); visible++; }
                cur.append(word);
                visible += wordVisible;
                Matcher m = CODE.matcher(word);
                while (m.find()) colour = m.group();
            }
            if (cur.length() > 0) out.add(cur.toString());
        }
        return out.toArray(new String[0]);
    }
}
