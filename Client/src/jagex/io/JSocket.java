package jagex.io;

/* Class16 - Decompiled by JODE
 * Visit http://jode.sourceforge.net/
 */

import jagex.utils.Deque;
import jagex.io.Buffer;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.net.Socket;
import unpackaged.Class14;
import unpackaged.Class39_Sub5_Sub16;
import unpackaged.Class39_Sub5_Sub4;
import unpackaged.Class39_Sub5_Sub9;
import unpackaged.Class39_Sub7;
import unpackaged.Class45;
import unpackaged.Class62_Sub1;
import unpackaged.FileLoader;
import jagex.utils.JString;
import unpackaged.Resource;
import unpackaged.Signlink;
import unpackaged.Widget;

public class JSocket implements Runnable {

    public int writePosition = 0;
    private int fpsProbeWriteEnd = -1;
    private int fpsProbeId;
    private long lastSuccessfulWriteNanos;
    public InputStream inputstream;
    public Socket socket;
    public Signlink signlink;
    public static BigInteger aBigInteger292 = (new BigInteger("126281243334509621910786157961571648139029640444686961553514423188662257084412132099345405190869140255510782101811050242941632064063371431334829733868500675557132805905863594614007916107891529024023784039950030199813062171961096886168210646453313260607895180672957089197913358703168034604511968295223858703073"));
    public OutputStream ouputstream;
    public boolean isStopped = false;
    public Resource threadResource;
    public static JString aClass3_296 = Class39_Sub5_Sub9.createJstring("Chat panel redrawn");
    public byte[] buffer;
    public static JString aClass3_298;
    public static JString aClass3_299;
    public static JString aClass3_300;
    public static int anInt301 = 128;
    public static int anInt302;
    public int bufferPosition = 0;
    public static Deque archiveRequests;
    public static JString aClass3_305;
    public static JString aClass3_306;
    public boolean aBoolean307 = false;
    public static JString aClass3_308;
    public static int[] intVariables;
    public static JString aClass3_310;
    public static JString aClass3_311;
    public static FileLoader fileLoader5;
    public static int anInt313;

    public static void method216(int i) {
        aClass3_299 = null;
        archiveRequests = null;
        aBigInteger292 = null;
        fileLoader5 = null;
        intVariables = null;
        aClass3_296 = null;
        aClass3_311 = null;
        aClass3_298 = null;
        aClass3_310 = null;
        aClass3_308 = null;
        aClass3_300 = null;
        aClass3_306 = null;
        aClass3_305 = null;
    }

    public void run() {
        try {
            for (;;) {
                int offset;
                int amountWrite;
                synchronized (this) {
                    if (writePosition == bufferPosition) {
                        if (isStopped) {
                            break;
                        }
                        try {
                            this.wait();
                        } catch (InterruptedException interruptedexception) {
                            /* empty */
                        }
                    }
                    offset = writePosition;
                    if (bufferPosition < writePosition) {
                        amountWrite = 5000 - writePosition;
                    } else {
                        amountWrite = bufferPosition - writePosition;
                    }
                }
                if (amountWrite > 0) {
                    boolean writeSucceeded = false;
                    try {
                        ouputstream.write(buffer, offset, amountWrite);
                        writeSucceeded = true;
                    } catch (IOException ioexception) {
                        aBoolean307 = true;
                    }
                    int deliveredProbeId = 0;
                    long writtenNanos = System.nanoTime();
                    synchronized (this) {
                        int oldPosition = writePosition;
                        writePosition = (writePosition + amountWrite) % 5000;
                        if (writeSucceeded) {
                            lastSuccessfulWriteNanos = writtenNanos;
                        }
                        if (writeSucceeded && fpsProbeWriteEnd >= 0) {
                            int distance = (fpsProbeWriteEnd - oldPosition + 5000) % 5000;
                            if (distance > 0 && distance <= amountWrite) {
                                deliveredProbeId = fpsProbeId;
                                fpsProbeWriteEnd = -1;
                            }
                        }
                    }
                    if (deliveredProbeId != 0) {
                        unpackaged.FpsDebugOverlay.onSocketTransmitted(deliveredProbeId, writtenNanos);
                    }
                    try {
                        if (bufferPosition == writePosition) {
                            ouputstream.flush();
                        }
                    } catch (IOException ioexception) {
                        aBoolean307 = true;
                    }
                }
            }
            try {
                if (inputstream != null) {
                    inputstream.close();
                }
                if (ouputstream != null) {
                    ouputstream.close();
                }
                if (socket != null) {
                    socket.close();
                }
            } catch (IOException ioexception) {
                /* empty */
            }
            buffer = null;
        } catch (Exception exception) {
            Class39_Sub7.method849(exception, 64, null);
        }
    }

    public void read(byte[] dest, int off, int len) throws IOException {
        if (!isStopped) {
            int read;
            for (/**/; len > 0; len -= read) {
                read = inputstream.read(dest, off, len);
                if (read <= 0) {
                    throw new EOFException();
                }
                off += read;
            }
        }
    }

    public void stop() {
        if (!isStopped) {
            synchronized (this) {
                isStopped = true;
                this.notifyAll();
            }
            if (threadResource != null) {
                while (threadResource.returnCode == 0) {
                    Class45.sleep(1L);
                }
                if (threadResource.returnCode == 1) {
                    try {
                        ((Thread) threadResource.returnObject).join();
                    } catch (InterruptedException interruptedexception) {
                        /* empty */
                    }
                }
            }
            threadResource = null;
        }
    }

    private static boolean tutorialInstructionMode = false;
    private static Widget tutorialOriginalChild6;
    private static Widget tutorialOriginalChild7;
    private static Widget tutorialOverflowChild6;
    private static Widget tutorialOverflowChild7;
    private static Widget tutorialScrollContainer;
    private static int[] tutorialOriginalParents;
    private static boolean tutorialOriginalChild5Interactive;
    private static int tutorialOriginalChild5ButtonType;
    private static int tutorialOriginalChild5ClickMask;
    private static int tutorialOriginalChild5ActiveClickMask;
    private static JString tutorialOriginalChild5Action;
    private static JString tutorialOriginalChild5Text;
    private static JString tutorialOriginalChild5ActiveText;

    // Legacy 2006/317 interface 6179 positions. 2006Scape's original cache
    // places the title at y=5 and the four body rows at 26/42/58/74. Keep
    // those coordinates here instead of applying message-specific nudges to
    // revision 443's group 214, otherwise Tutorial Island text drifts between
    // stages depending on which stock chatbox layout happened to be reused.
    private static final int[] TUTORIAL_TEXT_Y = {5, 26, 42, 58, 74, 90, 106};
    private static boolean tutorialDialogueGeometryMode = false;
    private static final int[] TUTORIAL_DIALOGUE_GROUPS = {
        64, 65, 66, 67,       // player dialogue, one through four lines
        102,                  // two-item hand-off
        210, 211, 212, 213, 214, // statements, one through five lines
        228, 230, 232, 234,  // option menus, two through five choices
        241, 242, 243, 244,  // NPC dialogue, one through four lines
        249                   // single-item hand-off
    };

    /**
     * Original 2006/317 text baselines recovered from the 2006Scape cache.
     * Entries are child,y pairs for the native revision-443 group that the
     * legacy interface is bridged onto.
     */
    private static int[] getTutorialDialogueTextY(int groupId) {
        switch (groupId) {
            case 64:  return new int[]{1, 0, 2, 40, 3, 80};
            case 65:  return new int[]{1, 0, 2, 28, 3, 56, 4, 80};
            case 66:  return new int[]{1, 0, 2, 20, 3, 40, 4, 60, 5, 80};
            case 67:  return new int[]{1, 0, 2, 16, 3, 32, 4, 48, 5, 64, 6, 80};
            case 102: return new int[]{1, 41, 2, 25, 3, 80};
            case 210: return new int[]{0, 29, 1, 77};
            case 211: return new int[]{0, 13, 1, 45, 2, 77};
            case 212: return new int[]{0, 5, 1, 29, 2, 53, 3, 77};
            case 213: return new int[]{0, 9, 1, 25, 2, 41, 3, 57, 4, 77};
            case 214: return new int[]{0, 0, 1, 16, 2, 32, 3, 48, 4, 64, 5, 80};
            case 228: return new int[]{0, -1, 1, 31, 2, 63};
            case 230: return new int[]{0, -1, 1, 23, 2, 47, 3, 71};
            case 232: return new int[]{0, -1, 1, 16, 2, 36, 3, 56, 4, 76};
            case 234: return new int[]{0, -1, 1, 15, 2, 31, 3, 47, 4, 63, 5, 79};
            case 241: return new int[]{1, 0, 2, 40, 3, 80};
            case 242: return new int[]{1, 0, 2, 28, 3, 56, 4, 80};
            case 243: return new int[]{1, 0, 2, 20, 3, 40, 4, 60, 5, 80};
            case 244: return new int[]{1, 0, 2, 16, 3, 32, 4, 48, 5, 64, 6, 80};
            case 249: return new int[]{1, 30, 2, 80};
            default:  return null;
        }
    }

    /**
     * Castle Wars replacement offers use the native two-choice parchment.
     * Move only its small sword ornaments outward; keep the red heading and
     * the clickable option rows at their stock positions.
     *
     * This runs after the title packet arrives as well as on initial load,
     * and restores stock geometry when another two-choice dialogue opens.
     */
    public static void configureCastleWarsOfferSwords() {
        if (Class62_Sub1.widgets == null || Class62_Sub1.widgets.length <= 228) {
            return;
        }
        Widget[] group = Class62_Sub1.widgets[228];
        if (group == null || group.length <= 3 || group[0] == null) {
            return;
        }

        Widget title = group[0];
        boolean castleWarsOffer = isCastleWarsOfferTitle(title.aClass3_2029);
        Widget leftSword = null;
        Widget rightSword = null;
        for (Widget widget : group) {
            if (widget == null || widget.type != 5
                    || widget.quadWidth <= 0 || widget.quadWidth > 48
                    || widget.quadHeight <= 0 || widget.quadHeight > 40
                    || Math.abs(widget.anInt2024 - title.anInt2024) > 28) {
                continue;
            }
            if (leftSword == null || widget.anInt2090 < leftSword.anInt2090) {
                leftSword = widget;
            }
            if (rightSword == null || widget.anInt2090 > rightSword.anInt2090) {
                rightSword = widget;
            }
        }
        // Do not disturb unrelated sprite decorations if the cache's title
        // has no distinct pair of swords around its heading.
        if (leftSword == null || rightSword == null || leftSword == rightSword
                || rightSword.anInt2090 - leftSword.anInt2090 < 100) {
            return;
        }
        int spread = castleWarsOffer ? 24 : 0;
        leftSword.anInt2091 = leftSword.anInt2090 - spread;
        rightSword.anInt2091 = rightSword.anInt2090 + spread;
    }

    private static boolean isCastleWarsOfferTitle(JString title) {
        String prefix = "A place is available on the ";
        if (title == null || title.bytes == null || title.length < prefix.length()) {
            return false;
        }
        for (int i = 0; i < prefix.length(); ++i) {
            if ((title.bytes[i] & 0xff) != prefix.charAt(i)) {
                return false;
            }
        }
        return true;
    }

    private static void applyTutorialDialogueGeometry(int groupId) {
        int[] textY = getTutorialDialogueTextY(groupId);
        if (textY == null || groupId < 0 || groupId >= Class62_Sub1.widgets.length) {
            return;
        }
        Widget[] group = Class62_Sub1.widgets[groupId];
        if (group == null) {
            return;
        }
        boolean useLegacy = tutorialDialogueGeometryMode
                && !(groupId == 214 && tutorialInstructionMode);
        for (int i = 0; i + 1 < textY.length; i += 2) {
            int child = textY[i];
            if (child < 0 || child >= group.length || group[child] == null) {
                continue;
            }
            Widget line = group[child];
            line.anInt2021 = useLegacy ? textY[i + 1] : line.anInt2024;
            line.anInt2091 = line.anInt2090;
        }
    }

    private static void setTutorialDialogueGeometryMode(boolean enabled) {
        tutorialDialogueGeometryMode = enabled;
        for (int i = 0; i < TUTORIAL_DIALOGUE_GROUPS.length; i++) {
            applyTutorialDialogueGeometry(TUTORIAL_DIALOGUE_GROUPS[i]);
        }
        Class14.aBoolean245 = true;
    }

    private static boolean isTutorialContinuePrompt(JString text) {
        if (text == null) {
            return false;
        }
        JString prompt = Class39_Sub5_Sub9.createJstring("Click here to continue");
        JString promptWithPeriod = Class39_Sub5_Sub9.createJstring("Click here to continue.");
        JString waiting = Class39_Sub5_Sub9.createJstring("Please wait...");
        return text.method97(prompt, -66) || text.method97(promptWithPeriod, -66)
                || text.method97(waiting, -66);
    }

    private static void clearTutorialOverflow() {
        Widget[] group = Class62_Sub1.widgets[214];
        if (group == null || group.length < 9) {
            return;
        }
        JString blank = Class39_Sub5_Sub9.createJstring("");
        for (int i = 6; i <= 7; i++) {
            if (group[i] != null) {
                group[i].aClass3_2029 = blank;
                group[i].aClass3_2048 = blank;
            }
        }
        if (group[8] != null) {
            group[8].anInt1994 = 0;
            group[8].anInt2095 = group[8].quadHeight;
        }
        Class14.aBoolean245 = true;
    }

    public static boolean isTutorialInstructionMode() {
        return tutorialInstructionMode;
    }

    public static JString sanitizeTutorialChatText(int componentId, JString text) {
        // Use native group-214 child 5 as the mode-control channel. Unlike the
        // synthetic scroll-container child 8, child 5 always exists in cache,
        // so a fresh login cannot address a synthetic child before group 214
        // has finished loading/configuring.
        if (componentId == ((214 << 16) | 5)) {
            if (text != null) {
                JString tutorial = Class39_Sub5_Sub9.createJstring("__tutorial__");
                JString tutorialDialogue = Class39_Sub5_Sub9.createJstring("__tutorial_dialogue__");
                JString dialogue = Class39_Sub5_Sub9.createJstring("__dialogue__");
                if (text.method97(tutorial, -66)) {
                    setTutorialDialogueGeometryMode(true);
                    setTutorialInstructionMode(true);
                    clearTutorialOverflow();
                    return Class39_Sub5_Sub9.createJstring("");
                }
                if (text.method97(tutorialDialogue, -66)) {
                    setTutorialInstructionMode(false);
                    setTutorialDialogueGeometryMode(true);
                    return tutorialOriginalChild5Text == null
                            ? Class39_Sub5_Sub9.createJstring("Click here to continue")
                            : tutorialOriginalChild5Text;
                }
                if (text.method97(dialogue, -66)) {
                    setTutorialInstructionMode(false);
                    setTutorialDialogueGeometryMode(false);
                    return tutorialOriginalChild5Text == null
                            ? Class39_Sub5_Sub9.createJstring("Click here to continue")
                            : tutorialOriginalChild5Text;
                }
            }
        }
        if (tutorialInstructionMode && (componentId >>> 16) == 214
                && isTutorialContinuePrompt(text)) {
            return Class39_Sub5_Sub9.createJstring("");
        }
        return text;
    }

    private static void setTutorialInstructionMode(boolean enabled) {
        tutorialInstructionMode = enabled;
        Widget[] group = Class62_Sub1.widgets[214];
        if (group == null || group.length < 9 || tutorialOriginalParents == null) {
            return;
        }
        int containerId = (214 << 16) | 8;
        if (enabled) {
            group[6] = tutorialOverflowChild6;
            group[7] = tutorialOverflowChild7;
            group[8] = tutorialScrollContainer;
            for (int i = 0; i < 6; i++) {
                if (group[i] != null && tutorialOriginalParents[i] == -1) {
                    group[i].anInt2050 = containerId;
                }
            }
            if (group[5] != null) {
                // Child 5 is an old-format button (button type 6). Clearing
                // only the new-format interactive flag leaves its invisible
                // Continue action alive. Disable the old-format button fields
                // themselves while this group is used as a tutorial overlay.
                group[5].aBoolean2047 = false;
                group[5].aBoolean2106 = false;
                group[5].anInt2089 = 0;
                group[5].anInt2049 = 0;
                group[5].anInt1998 = 0;
                group[5].aClass3_2068 = Class39_Sub5_Sub9.createJstring("");
                if (isTutorialContinuePrompt(group[5].aClass3_2029)) {
                    group[5].aClass3_2029 = Class39_Sub5_Sub9.createJstring("");
                    group[5].aClass3_2048 = Class39_Sub5_Sub9.createJstring("");
                }
            }
        } else {
            for (int i = 0; i < 6; i++) {
                if (group[i] != null) {
                    group[i].anInt2050 = tutorialOriginalParents[i];
                    group[i].anInt2021 = group[i].anInt2024;
                    group[i].anInt2091 = group[i].anInt2090;
                }
            }
            group[6] = tutorialOriginalChild6;
            group[7] = tutorialOriginalChild7;
            group[8] = tutorialScrollContainer;
            if (group[5] != null) {
                group[5].aBoolean2047 = tutorialOriginalChild5Interactive;
                group[5].aBoolean2106 = false;
                group[5].anInt2089 = tutorialOriginalChild5ButtonType;
                group[5].anInt2049 = tutorialOriginalChild5ClickMask;
                group[5].anInt1998 = tutorialOriginalChild5ActiveClickMask;
                group[5].aClass3_2068 = tutorialOriginalChild5Action;
                group[5].aClass3_2029 = tutorialOriginalChild5Text;
                group[5].aClass3_2048 = tutorialOriginalChild5ActiveText;
            }
        }
        Class14.aBoolean245 = true;
    }

    private static Widget createTutorialOverflowLine(Widget source, int child,
            int y, int parentId) {
        Widget line = new Widget();
        line.anInt2084 = (214 << 16) | child;
        line.anInt2050 = parentId;
        line.aBoolean2013 = source.aBoolean2013;
        line.type = 4;
        line.anInt2078 = source.anInt2078;
        line.anInt2090 = line.anInt2091 = source.anInt2091;
        line.anInt2024 = line.anInt2021 = y;
        line.quadWidth = source.quadWidth;
        line.quadHeight = source.quadHeight;
        line.anInt2030 = source.anInt2030;
        line.anInt2032 = source.anInt2032;
        line.anInt1996 = source.anInt1996;
        line.anInt2036 = source.anInt2036;
        line.anInt2105 = source.anInt2105;
        line.aBoolean2059 = source.aBoolean2059;
        line.activeQuadColor = source.activeQuadColor;
        line.inactiveQuadColor = source.inactiveQuadColor;
        line.anInt2041 = source.anInt2041;
        line.anInt2086 = source.anInt2086;
        line.aClass3_2029 = Class39_Sub5_Sub9.createJstring("");
        line.aClass3_2048 = Class39_Sub5_Sub9.createJstring("");
        line.anInt2089 = 0;
        line.anInt2049 = 0;
        line.anInt1998 = 0;
        return line;
    }

    /**
     * Group 214 is the native 443 chatbox used by Tutorial Island. The cache
     * only has six text rows, while some tutorial instructions need more.
     * Add two normal text rows and put the group behind a standard old-format
     * scroll container so the stock client draws and handles the scrollbar.
     */
    private static void configureTutorialChatbox() {
        Widget[] group = Class62_Sub1.widgets[214];
        if (group == null || group.length < 6) {
            return;
        }
        if (tutorialOriginalParents != null) {
            // The native widget group can be recreated while the static
            // tutorial layout state survives. Re-expand/re-attach the runtime
            // children instead of assuming they are still present.
            if (group.length < 9) {
                Widget[] expanded = new Widget[9];
                System.arraycopy(group, 0, expanded, 0, group.length);
                group = expanded;
                Class62_Sub1.widgets[214] = group;
            }
            setTutorialInstructionMode(tutorialInstructionMode);
            if (tutorialInstructionMode) {
                updateTutorialChatScroll();
            } else {
                applyTutorialDialogueGeometry(214);
            }
            return;
        }
        int originalLength = group.length;
        if (group.length < 9) {
            Widget[] expanded = new Widget[9];
            System.arraycopy(group, 0, expanded, 0, group.length);
            group = expanded;
            Class62_Sub1.widgets[214] = group;
        }
        tutorialOriginalParents = new int[6];
        for (int i = 0; i < 6; i++) {
            tutorialOriginalParents[i] = group[i] == null ? -1 : group[i].anInt2050;
        }
        tutorialOriginalChild6 = originalLength > 6 ? group[6] : null;
        tutorialOriginalChild7 = originalLength > 7 ? group[7] : null;
        if (group[5] != null) {
            tutorialOriginalChild5Interactive = group[5].aBoolean2047;
            tutorialOriginalChild5ButtonType = group[5].anInt2089;
            tutorialOriginalChild5ClickMask = group[5].anInt2049;
            tutorialOriginalChild5ActiveClickMask = group[5].anInt1998;
            tutorialOriginalChild5Action = group[5].aClass3_2068;
            tutorialOriginalChild5Text = group[5].aClass3_2029;
            tutorialOriginalChild5ActiveText = group[5].aClass3_2048;
        }

        Widget source = group[4] != null ? group[4] : group[3];
        if (source == null) {
            return;
        }
        int sourceParent = source.anInt2050;
        int firstOverflowY = TUTORIAL_TEXT_Y[5];
        int spacing = TUTORIAL_TEXT_Y[6] - TUTORIAL_TEXT_Y[5];

        int maxRight = 0;
        int maxBottom = 0;
        for (int i = 0; i < 6; i++) {
            Widget widget = group[i];
            if (widget != null && widget.anInt2050 == -1) {
                maxRight = Math.max(maxRight, widget.anInt2091 + widget.quadWidth);
                maxBottom = Math.max(maxBottom, widget.anInt2021 + widget.quadHeight);
            }
        }
        if (maxRight <= 0) {
            maxRight = 479;
        }
        if (maxBottom <= 0) {
            maxBottom = firstOverflowY;
        }

        int containerId = (214 << 16) | 8;
        Widget container = new Widget();
        container.anInt2084 = containerId;
        container.anInt2050 = -1;
        container.type = 0;
        container.aBoolean2013 = false;
        container.anInt2090 = container.anInt2091 = 0;
        container.anInt2024 = container.anInt2021 = 0;
        container.quadWidth = Math.min(496, maxRight);
        container.quadHeight = maxBottom;
        container.anInt2020 = container.quadWidth;
        container.anInt2095 = container.quadHeight;
        container.anInt1994 = 0;

        int overflowParent = sourceParent == -1 ? containerId : sourceParent;
        tutorialOverflowChild6 = createTutorialOverflowLine(source, 6,
                firstOverflowY, overflowParent);
        tutorialOverflowChild7 = createTutorialOverflowLine(source, 7,
                firstOverflowY + spacing, overflowParent);
        tutorialScrollContainer = container;
        group[8] = tutorialScrollContainer;
        setTutorialInstructionMode(tutorialInstructionMode);
        if (tutorialInstructionMode) {
            updateTutorialChatScroll();
        } else {
            applyTutorialDialogueGeometry(214);
        }
    }

    /**
     * Group 137 is a native seven-tile crafting layout repurposed by the
     * server's world teleport selector. Replace its item models with the
     * stock revision-443 button sprites (293/294) and keep the existing text
     * widgets as labels. No generated or external artwork is used here.
     */
    private static void configureWorldTeleportButtons() {
        Widget[] group = Class62_Sub1.widgets[137];
        if (group == null || group.length <= 40) {
            return;
        }
        int[] buttonChildren = {2, 3, 4, 5, 6, 7, 8};
        int[] labelChildren = {12, 16, 20, 24, 28, 32, 36};
        int[] unusedActionChildren = {
                9, 10, 11, 13, 14, 15, 17, 18, 19, 21, 22, 23,
                25, 26, 27, 29, 30, 31, 33, 34, 35
        };
        int[] buttonX = {55, 135, 216, 299, 383, 383, 55};
        int[] buttonY = {82, 82, 82, 82, 82, 215, 215};
        for (int child : unusedActionChildren) {
            Widget unusedAction = group[child];
            if (unusedAction == null) {
                continue;
            }
            unusedAction.anInt2089 = 0;
            unusedAction.anInt2049 = unusedAction.anInt1998 = 0;
            unusedAction.aBoolean2047 = false;
            unusedAction.aBoolean2055 = true;
            unusedAction.aClass3_2068 = Class39_Sub5_Sub9.createJstring("");
        }
        group[0].aBoolean2055 = true;
        group[1].aBoolean2055 = true;
        for (int index = 0; index < buttonChildren.length; index++) {
            Widget button = group[buttonChildren[index]];
            Widget label = group[labelChildren[index]];
            if (button == null || label == null) {
                continue;
            }
            button.type = 5;
            button.anInt2089 = 1;
            button.anInt2049 = button.anInt1998 = 0x400000;
            button.aBoolean2047 = true;
            button.aClass3_2068 = Class39_Sub5_Sub9.createJstring("Select");
            button.anInt2090 = button.anInt2091 = buttonX[index];
            button.anInt2024 = button.anInt2021 = buttonY[index];
            button.quadWidth = 72;
            button.quadHeight = 36;
            button.anInt2093 = 293;
            button.anInt2034 = 294;
            button.anInt2030 = 0;
            button.aBoolean2055 = false;

            label.anInt2089 = 0;
            label.anInt2049 = label.anInt1998 = 0;
            label.aBoolean2047 = false;
            label.aClass3_2068 = Class39_Sub5_Sub9.createJstring("");
            label.anInt2090 = label.anInt2091 = buttonX[index];
            label.anInt2024 = label.anInt2021 = buttonY[index];
            label.quadWidth = 72;
            label.quadHeight = 36;
            label.anInt1996 = 1;
            label.activeQuadColor = 0xffffff;
            label.inactiveQuadColor = 0xffffff;
            label.anInt2041 = 0xffffff;
            label.anInt2086 = 0xffffff;
        }
    }

    public static void updateTutorialChatScroll() {
        if (!tutorialInstructionMode) {
            return;
        }
        Widget[] group = Class62_Sub1.widgets[214];
        if (group == null || group.length < 9 || group[8] == null) {
            return;
        }
        Widget container = group[8];

        // Match the original 2006 interface 6179 exactly: title at y=5,
        // followed by body rows at 26/42/58/74. The previous implementation
        // applied stage-specific offsets, so identical widget rows appeared at
        // different heights from one tutorial instruction to the next.
        for (int i = 0; i <= 4; i++) {
            Widget line = group[i];
            if (line == null) {
                continue;
            }
            line.anInt2021 = TUTORIAL_TEXT_Y[i];
            line.anInt2091 = line.anInt2090;
        }

        // Children 6 and 7 are synthetic overflow rows for instructions that
        // exceed the original four body lines. Continue the authentic 16px
        // body-line spacing without changing the native widgets' saved cache
        // coordinates used by ordinary dialogue mode.
        for (int i = 6; i <= 7; i++) {
            Widget line = group[i];
            if (line == null) {
                continue;
            }
            int y = TUTORIAL_TEXT_Y[i - 1];
            line.anInt2024 = y;
            line.anInt2021 = y;
            line.anInt2091 = line.anInt2090;
        }

        int contentHeight = container.quadHeight;
        for (int i = 0; i <= 7; i++) {
            Widget line = i < group.length ? group[i] : null;
            if (line != null && line.aClass3_2029 != null
                    && line.aClass3_2029.getLength() > 0) {
                contentHeight = Math.max(contentHeight,
                        line.anInt2021 + line.quadHeight + 2);
            }
        }
        container.anInt2095 = Math.max(container.quadHeight, contentHeight);
        int maxScroll = container.anInt2095 - container.quadHeight;
        if (container.anInt1994 > maxScroll) {
            container.anInt1994 = maxScroll;
        }
        if (container.anInt1994 < 0) {
            container.anInt1994 = 0;
        }
        Class14.aBoolean245 = true;
    }

    /**
     * Native revision-443 cooking group 307 only contains a title, a model,
     * two decorative sprites, and four quantity buttons. There is no food-name
     * text widget in the cache. Append one without changing the stock controls.
     *
     * The server targets synthetic child 8 with the selected raw-food name.
     */
    private static void configureCookingChatbox() {
        Widget[] group = Class62_Sub1.widgets[307];
        if (group == null || group.length < 8 || group[7] == null) {
            return;
        }
        if (group.length < 9) {
            Widget[] expanded = new Widget[9];
            System.arraycopy(group, 0, expanded, 0, group.length);
            Class62_Sub1.widgets[307] = group = expanded;
        }
        if (group[8] != null) {
            return;
        }

        Widget title = group[7];
        Widget name = new Widget();
        name.anInt2084 = (307 << 16) | 8;
        name.anInt2050 = -1;
        name.aBoolean2013 = title.aBoolean2013;
        name.type = 4;
        name.anInt2078 = title.anInt2078;
        name.anInt2090 = name.anInt2091 = 0;
        // The stock chatbox renderer clips group 307 to 96 pixels high
        // (Deque.method955(96, 479, ...)). The previous y=96 put the
        // entire label *outside* that clip, although packet 180 updated it.
        // Place it under the visible raw-food model, but within the clip.
        name.anInt2024 = name.anInt2021 = 78;
        name.quadWidth = 479;
        name.quadHeight = 16;
        name.anInt2105 = title.anInt2105;
        name.anInt2032 = 1; // centered
        name.anInt1996 = title.anInt1996;
        name.anInt2036 = title.anInt2036;
        name.aBoolean2059 = title.aBoolean2059;
        name.activeQuadColor = name.inactiveQuadColor = 0x000000;
        name.aClass3_2029 = Class39_Sub5_Sub9.createJstring("");
        name.aClass3_2048 = Class39_Sub5_Sub9.createJstring("");
        name.anInt2089 = 0;
        name.anInt2049 = name.anInt1998 = 0;
        group[8] = name;
        Class14.aBoolean245 = true;
    }

    /**
     * Native 443 production chatboxes (303, 304, 309) already contain actual
     * item models and per-item Make X/10/5/1 actions. They lack a dedicated
     * visible product-name line. Add passive captions below each item model,
     * inside the 96px-tall chatbox clip, without replacing the native actions.
     *
     * The server targets only these synthetic children when fletching.
     */
    private static void configureFletchingChatbox(int parent) {
        int count = parent == 304 ? 3 : parent == 303 ? 2 : parent == 309 ? 1 : 0;
        if (count == 0) {
            return;
        }
        Widget[] group = Class62_Sub1.widgets[parent];
        int titleChild = parent == 304 ? 17 : parent == 303 ? 12 : 7;
        int firstCaption = titleChild + 1;
        if (group == null || group.length <= titleChild || group[titleChild] == null) {
            return;
        }
        if (group.length < firstCaption + count) {
            Widget[] expanded = new Widget[firstCaption + count];
            System.arraycopy(group, 0, expanded, 0, group.length);
            Class62_Sub1.widgets[parent] = group = expanded;
        }

        Widget title = group[titleChild];
        for (int i = 0; i < count; i++) {
            if (group[firstCaption + i] != null) {
                continue;
            }
            Widget label = new Widget();
            label.anInt2084 = (parent << 16) | firstCaption + i;
            label.anInt2050 = -1;
            label.aBoolean2013 = title.aBoolean2013;
            label.type = 4;
            label.anInt2078 = title.anInt2078;
            // Native group 304's three slots occupy equal 160px columns;
            // group 303 uses two 240px columns, and 309 one full-width slot.
            int left = parent == 304 ? i * 160 : parent == 303 ? i * 240 : 0;
            int width = parent == 304 ? 160 : parent == 303 ? 240 : 479;
            label.anInt2090 = label.anInt2091 = left;
            label.anInt2024 = label.anInt2021 = 78;
            label.quadWidth = width;
            label.quadHeight = 16;
            label.anInt2105 = title.anInt2105;
            label.anInt2032 = 1;
            label.anInt1996 = title.anInt1996;
            label.anInt2036 = title.anInt2036;
            label.aBoolean2059 = title.aBoolean2059;
            label.activeQuadColor = label.inactiveQuadColor = 0x402800;
            label.aClass3_2029 = Class39_Sub5_Sub9.createJstring("");
            label.aClass3_2048 = Class39_Sub5_Sub9.createJstring("");
            label.anInt2089 = 0;
            label.anInt2049 = label.anInt1998 = 0;
            group[firstCaption + i] = label;
        }
        Class14.aBoolean245 = true;
    }

    /**
     * Group 211 is the stock two-line statement chatbox, but has no icon.
     * Append a passive skill selector and a 25x25 sprite in its unused left
     * margin. The icon uses exactly the same sprite as native Skills tab 320.
     * All ordinary statement widgets and the Continue action stay untouched.
     */
    // Group 211's text widgets are shared with ordinary statements, so keep
    // their native widths and restore them as soon as level-up closes.
    private static final int[] levelUpTextWidths = new int[2];

    private static void configureLevelUpChatbox() {
        Widget[] group = Class62_Sub1.widgets[211];
        if (group == null || group.length < 3 || group[0] == null) {
            return;
        }
        if (group.length < 5) {
            Widget[] expanded = new Widget[5];
            System.arraycopy(group, 0, expanded, 0, group.length);
            Class62_Sub1.widgets[211] = group = expanded;
        }
        if (group[3] == null) {
            Widget selector = new Widget();
            selector.anInt2084 = (211 << 16) | 3;
            selector.anInt2050 = -1;
            selector.type = 4;
            selector.anInt2089 = 0;
            selector.anInt2049 = selector.anInt1998 = 0;
            selector.aBoolean2055 = true; // Server-only control, never painted.
            selector.aClass3_2029 = Class39_Sub5_Sub9.createJstring("");
            selector.aClass3_2048 = Class39_Sub5_Sub9.createJstring("");
            group[3] = selector;
        }
        if (group[4] == null) {
            for (int i = 0; i < levelUpTextWidths.length; i++) {
                levelUpTextWidths[i] = group[i].quadWidth;
            }
            Widget icon = new Widget();
            icon.anInt2084 = (211 << 16) | 4;
            icon.anInt2050 = -1;
            // Original level-up graphics are roughly 60 pixels tall. The
            // Skills-tab sprites are just 25px, so the old-format renderer
            // would draw them tiny even if quadWidth were changed. Use the
            // existing new-format sprite scaling branch (method687) instead.
            icon.aBoolean2013 = true;
            icon.type = 5;
            icon.anInt2090 = icon.anInt2091 = 6;
            icon.anInt2024 = icon.anInt2021 = 16;
            icon.quadWidth = 64;
            icon.quadHeight = 64;
            icon.anInt2093 = icon.anInt2034 = -1;
            icon.anInt2089 = 0;
            icon.anInt2049 = icon.anInt1998 = 0;
            icon.aBoolean2055 = true; // Hidden on ordinary statements.
            group[4] = icon;
        }
    }

    /**
     * Skill order is SkillManager.SKILL_NAMES (0..21). IDs below are the
     * matching original sprite widgets in revision-443 Skills tab group 320,
     * not inferred sprite archive numbers or external image assets.
     */
    public static void updateLevelUpSkillIcon(jagex.utils.JString skillName) {
        Widget[] levelUp = Class62_Sub1.widgets[211];
        if (levelUp == null || levelUp.length < 5 || levelUp[4] == null) {
            return;
        }
        Widget icon = levelUp[4];
        icon.aBoolean2055 = true; // Also clears the icon on ordinary statements.
        if (skillName == null) {
            for (int i = 0; i < levelUpTextWidths.length; i++) {
                Widget line = levelUp[i];
                if (line != null) {
                    line.anInt2091 = line.anInt2090;
                    line.quadWidth = levelUpTextWidths[i];
                }
            }
            Class14.aBoolean245 = true;
            return;
        }

        String[] skillNames = {
            "Attack", "Defence", "Strength", "Hitpoints", "Ranged", "Prayer",
            "Magic", "Cooking", "Woodcutting", "Fletching", "Fishing",
            "Firemaking", "Crafting", "Smithing", "Mining", "Herblore",
            "Agility", "Thieving", "Slayer", "Farming", "Runecrafting",
            "Construction"
        };
        int[] skillSprites = {
            48, 54, 51, 49, 57, 60, 63, 59, 65, 64, 56,
            62, 61, 53, 50, 55, 52, 58, 110, 115, 105, 45
        };
        for (int i = 0; i < skillNames.length; i++) {
            if (!skillName.method97(Class39_Sub5_Sub9.createJstring(skillNames[i]), -66)) {
                continue;
            }
            if (loadWidget(320) && Class62_Sub1.widgets[320] != null) {
                Widget source = Class62_Sub1.widgets[320][skillSprites[i]];
                if (source != null && source.type == 5
                        && (source.anInt2093 >= 0 || source.runtimeSprite != null)) {
                    icon.anInt2093 = icon.anInt2034 = source.anInt2093;
                    icon.runtimeSprite = source.runtimeSprite;
                    icon.anInt2022 = source.anInt2022;
                    icon.anInt2003 = source.anInt2003;
                    icon.aBoolean2107 = source.aBoolean2107;
                    icon.aBoolean2038 = source.aBoolean2038;
                    icon.anInt2030 = source.anInt2030;
                    icon.aBoolean2055 = false;
                    // Shift the blue heading and level line clear of the
                    // 64px icon. Native dialogue text bounds are restored by
                    // the empty selector when this level-up closes.
                    for (int lineIndex = 0; lineIndex < levelUpTextWidths.length; lineIndex++) {
                        Widget line = levelUp[lineIndex];
                        if (line != null) {
                            line.anInt2091 = 76;
                            line.quadWidth = 400;
                        }
                    }
                }
            }
            break;
        }
        // The server clears this selector by sending an empty JString, not
        // null. Restore ordinary statement geometry after that packet.
        if (icon.aBoolean2055) {
            for (int i = 0; i < levelUpTextWidths.length; i++) {
                Widget line = levelUp[i];
                if (line != null) {
                    line.anInt2091 = line.anInt2090;
                    line.quadWidth = levelUpTextWidths[i];
                }
            }
        }
        Class14.aBoolean245 = true;
    }

    public static boolean loadWidget(int parent) {
        if (unpackaged.ChristmasEventSnow.loadOverlayWidget(parent)) return true;
        if (unpackaged.GrandExchangeWidgets.load(parent)) return true;
        if (unpackaged.ClassicInterface.load(parent)) return true;
        if (parent < 0
                || parent >= Class39_Sub5_Sub4.widgetsLoaded.length
                || parent >= Class62_Sub1.widgets.length) {
            return false;
        }
        if (Class39_Sub5_Sub4.widgetsLoaded[parent]) {
            if (parent == 214) {
                configureTutorialChatbox();
            } else if (parent == 211) {
                configureLevelUpChatbox();
            } else if (parent == 307) {
                configureCookingChatbox();
            } else if (parent == 303 || parent == 304 || parent == 309) {
                configureFletchingChatbox(parent);
            } else if (parent == 137) {
                configureWorldTeleportButtons();
            } else {
                applyTutorialDialogueGeometry(parent);
            }
            if (parent == 228) {
                configureCastleWarsOfferSwords();
            }
            return true;
        }
        if (!Class39_Sub5_Sub16.wigetFileLoader.hasArchive(parent)) {
            return false;
        }
        int amountChildren = Class39_Sub5_Sub16.wigetFileLoader.getAmountChildren(parent);
        if (amountChildren == 0) {
            Class39_Sub5_Sub4.widgetsLoaded[parent] = true;
            return true;
        }
        if (Class62_Sub1.widgets[parent] == null) {
            Class62_Sub1.widgets[parent] = new Widget[amountChildren];
        }
        for (int i_6_ = 0; i_6_ < amountChildren; i_6_++) {
            if (Class62_Sub1.widgets[parent][i_6_]
                    == null) {
                byte[] is = Class39_Sub5_Sub16.wigetFileLoader.lookupFile(parent,
                        i_6_);
                if (is != null) {
                    Class62_Sub1.widgets[parent][i_6_] = new Widget();
                    Class62_Sub1.widgets[parent][i_6_].anInt2084 = i_6_ + (parent << 16);
                    if (is[0] == -1) {
                        Class62_Sub1.widgets[parent][i_6_].decodeNewFormat(new Buffer(is));
                    } else {
                        Class62_Sub1.widgets[parent][i_6_].decodeOldFormat(new Buffer(is));
                    }
                }
            }
        }
        if (parent == 214) {
            configureTutorialChatbox();
        } else if (parent == 211) {
            configureLevelUpChatbox();
        } else if (parent == 307) {
            configureCookingChatbox();
        } else if (parent == 303 || parent == 304 || parent == 309) {
            configureFletchingChatbox(parent);
        } else if (parent == 137) {
            configureWorldTeleportButtons();
        } else {
            applyTutorialDialogueGeometry(parent);
        }
        if (parent == 228) {
            configureCastleWarsOfferSwords();
        }
        Class39_Sub5_Sub4.widgetsLoaded[parent] = true;
        return true;
    }

    public int read() throws IOException {
        if (isStopped) {
            return 0;
        }
        return inputstream.read();
    }

    @Override
    public void finalize() {
        stop();
    }

    public int available() throws IOException {
        if (isStopped) {
            return 0;
        }
        return inputstream.available();
    }

    /** Mark the end of an FPS probe in the asynchronous TCP write ring. */
    public synchronized void markFpsProbe(int pingId) {
        fpsProbeId = pingId;
        fpsProbeWriteEnd = bufferPosition;
        if (writePosition == bufferPosition) {
            // The writer already drained this packet before we marked it.
            fpsProbeWriteEnd = -1;
            unpackaged.FpsDebugOverlay.onSocketTransmitted(
                    pingId, lastSuccessfulWriteNanos == 0L
                            ? System.nanoTime() : lastSuccessfulWriteNanos);
        }
    }

    public void write(byte[] src, int off, int len) throws IOException {
        if (!isStopped) {
            if (aBoolean307) {
                aBoolean307 = false;
                throw new IOException();
            }
            if (buffer == null) {
                buffer = new byte[5000];
            }
            synchronized (this) {
                int i_9_ = 0;
                for (/**/; len > i_9_; i_9_++) {
                    buffer[bufferPosition] = src[off + i_9_];
                    bufferPosition = (bufferPosition + 1) % 5000;
                    if (bufferPosition == (writePosition + 4900) % 5000) {
                        throw new IOException();
                    }
                }
                if (threadResource == null) {
                    threadResource = signlink.requestThread(this, 3);
                }
                this.notifyAll();
            }
        }
    }

    public JSocket(Socket sock, Signlink sign) throws IOException {
        signlink = sign;
        socket = sock;
        socket.setSoTimeout(30000);
        socket.setTcpNoDelay(true);
        inputstream = socket.getInputStream();
        ouputstream = socket.getOutputStream();
    }

    static {
        aClass3_298 = Class39_Sub5_Sub9.createJstring("Error connecting to server)3");
        aClass3_299 = aClass3_298;
        aClass3_300 = Class39_Sub5_Sub9.createJstring("und Ihr Passwort ein)3");
        aClass3_306 = Class39_Sub5_Sub9.createJstring("Username: ");
        aClass3_305 = aClass3_306;
        archiveRequests = new Deque();
        aClass3_310 = Class39_Sub5_Sub9.createJstring("runes");
        aClass3_311 = Class39_Sub5_Sub9.createJstring("cyan:");
        anInt313 = 0;
        aClass3_308 = aClass3_311;
    }
}
