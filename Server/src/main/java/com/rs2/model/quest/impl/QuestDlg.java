package com.rs2.model.quest.impl;

import com.rs2.model.player.Player;
import com.rs2.model.dialogue.DialogueManager;
import java.util.*;

/** Tiny dialogue-script builder for the EasyRS2 step/option dialogue engine. */
public final class QuestDlg {
    public interface Cond { boolean test(Player p); }
    public interface Act { void run(Player p); }

    private static final int NPC = 0, PLAYER = 1, MES = 2, ACT = 3, IF = 4, GO = 5, OPTS = 6, DISP = 7, END = 8, LABEL = 9, KEEP = 10;
    private static final int ANIM = 591;

    private static final class Node {
        int kind; String[] lines; Act act; Cond cond; String target;
        String[] optText; String[] optTarget;
    }

    /** Output sink (real game: the player's dialogue interface). */
    public interface Out {
        void npc(String[] l); void player(String[] l); void mes(String[] l); void opts(String[] t);
        void setStep(int s); void next(int s); void close(); void finish();
    }

    private static final class CwOut implements Out {
        final Player p; CwOut(Player p) { this.p = p; }
        public void npc(String[] l) {
            DialogueManager d = p.getDialogueManager();
            if (l.length == 1) d.showNpcOneLineDialogue(l[0], ANIM);
            else if (l.length == 2) d.showNpcTwoLineDialogue(l[0], l[1], ANIM);
            else if (l.length == 3) d.showNpcThreeLineDialogue(l[0], l[1], l[2], ANIM);
            else d.showNpcFourLineDialogue(l[0], l[1], l[2], l[3], ANIM);
        }
        public void player(String[] l) {
            DialogueManager d = p.getDialogueManager();
            if (l.length == 1) d.showPlayerOneLineDialogue(l[0], ANIM);
            else if (l.length == 2) d.showPlayerTwoLineDialogue(l[0], l[1], ANIM);
            else if (l.length == 3) d.showPlayerThreeLineDialogue(l[0], l[1], l[2], ANIM);
            else d.showPlayerFourLineDialogue(l[0], l[1], l[2], l[3], ANIM);
        }
        public void mes(String[] l) {
            DialogueManager d = p.getDialogueManager();
            if (l.length == 1) d.showOneLineStatement(l[0]);
            else if (l.length == 2) d.showTwoLineStatement(l[0], l[1]);
            else d.showThreeLineStatement(l[0], l[1], l[2]);
        }
        public void opts(String[] t) {
            DialogueManager d = p.getDialogueManager();
            if (t.length == 2) d.showTwoOptions(t[0], t[1]);
            else if (t.length == 3) d.showThreeOptions(t[0], t[1], t[2]);
            else if (t.length == 4) d.showFourOptions(t[0], t[1], t[2], t[3]);
            else d.showFiveOptions(t[0], t[1], t[2], t[3], t[4]);
        }
        public void setStep(int s) { p.getDialogueManager().setDialogueStep(s); }
        public void next(int s) { p.getDialogueManager().setNextDialogueStep(s); }
        public void close() { p.getDialogueManager().finishDialogue(); }
        /** Close the dialogue interface right now (used when the script ends on a click with nothing new to show). */
        public void finish() { p.packetSender.closeInterfaces(); p.getDialogueManager().resetDialogueState(); }
    }

    private final List<Node> nodes = new ArrayList<>();
    private final Map<String, Integer> labels = new HashMap<>();
    private boolean compiled;

    public static String[] wrap(String s, int width) {
        List<String> out = new ArrayList<>();
        StringBuilder cur = new StringBuilder();
        for (String w : s.split(" ")) {
            if (cur.length() > 0 && cur.length() + 1 + w.length() > width) { out.add(cur.toString()); cur.setLength(0); }
            if (cur.length() > 0) cur.append(' ');
            cur.append(w);
        }
        if (cur.length() > 0) out.add(cur.toString());
        return out.toArray(new String[0]);
    }

    private QuestDlg add(Node n) { nodes.add(n); return this; }

    private QuestDlg chunked(int kind, String text, int perBox, int width) {
        String[] l = wrap(text, width);
        for (int k = 0; k < l.length; k += perBox) {
            Node n = new Node(); n.kind = kind;
            n.lines = Arrays.copyOfRange(l, k, Math.min(l.length, k + perBox));
            nodes.add(n);
        }
        return this;
    }

    public QuestDlg npc(String t) { return chunked(NPC, t, 4, 52); }
    public QuestDlg player(String t) { return chunked(PLAYER, t, 4, 52); }
    public QuestDlg mes(String t) { return chunked(MES, t, 3, 60); }
    public QuestDlg act(Act a) { Node n = new Node(); n.kind = ACT; n.act = a; return add(n); }
    public QuestDlg jumpIf(Cond c, String label) { Node n = new Node(); n.kind = IF; n.cond = c; n.target = label; return add(n); }
    public QuestDlg go(String label) { Node n = new Node(); n.kind = GO; n.target = label; return add(n); }
    /** Ends the script without closing the interface (for scripts whose last action opens its own screen). */
    public QuestDlg endKeep() { Node n = new Node(); n.kind = KEEP; return add(n); }
    public QuestDlg end() { Node n = new Node(); n.kind = END; return add(n); }
    public QuestDlg label(String name) { Node n = new Node(); n.kind = LABEL; n.target = name; return add(n); }
    /** pairs: option text, target label, ... (2-5 options) */
    public QuestDlg options(String... pairs) {
        Node n = new Node(); n.kind = OPTS;
        int c = pairs.length / 2;
        n.optText = new String[c]; n.optTarget = new String[c];
        for (int k = 0; k < c; k++) { n.optText[k] = pairs[2 * k]; n.optTarget[k] = pairs[2 * k + 1]; }
        nodes.add(n);
        Node d = new Node(); d.kind = DISP; d.optText = n.optText; d.optTarget = n.optTarget;
        nodes.add(d);
        return this;
    }

    private void compile() {
        if (compiled) return;
        for (int k = 0; k < nodes.size(); k++) if (nodes.get(k).kind == LABEL) labels.put(nodes.get(k).target, k + 1);
        compiled = true;
    }

    /** Fails fast (at server start) if a script references a missing label. */
    public void validate() {
        compile();
        for (Node n : nodes) {
            if (n.kind == GO || n.kind == IF) idx(n.target);
            if (n.kind == OPTS) for (String t : n.optTarget) idx(t);
        }
    }

    private int idx(String label) {
        Integer v = labels.get(label);
        if (v == null) throw new IllegalStateException("no label " + label);
        return v;
    }

    private boolean isEnd(int step) {
        // follow labels/gos to see whether the node we land on closes the dialogue
        for (int g = 0; g < 20; g++) {
            if (step < 1 || step > nodes.size()) return true;
            Node n = nodes.get(step - 1);
            if (n.kind == LABEL) { step++; continue; }
            return n.kind == END || n.kind == KEEP;
        }
        return false;
    }

    private void next(Out o, int step) {
        if (isEnd(step)) o.close(); else o.next(step);
    }

    public boolean run(Player p, int step, int option) { return run(new CwOut(p), p, step, option); }

    /** Run the script at the given step. Returns true if this script handled the request. */
    public boolean run(Out o, Player p, int step, int option) {
        compile();
        boolean shown = false;
        for (int guard = 0; guard < 200; guard++) {
            if (step < 1 || step > nodes.size()) { if (shown) o.close(); else o.finish(); return true; }
            Node n = nodes.get(step - 1);
            switch (n.kind) {
                case LABEL: step++; continue;
                case ACT: n.act.run(p); step++; continue;
                case IF: step = n.cond.test(p) ? idx(n.target) : step + 1; continue;
                case GO: step = idx(n.target); continue;
                case KEEP: o.close(); return true;
                case END: if (shown) o.close(); else o.finish(); return true;
                case NPC: shown = true; o.npc(n.lines); next(o, step + 1); return true;
                case PLAYER: shown = true; o.player(n.lines); next(o, step + 1); return true;
                case MES: shown = true; o.mes(n.lines); next(o, step + 1); return true;
                case OPTS: shown = true; o.opts(n.optText); o.setStep(step); return true;
                case DISP: {
                    int k = option - 1;
                    if (k < 0 || k >= n.optText.length) { o.finish(); return true; }
                    o.player(wrap(n.optText[k], 52));
                    next(o, idx(n.optTarget[k]));
                    return true;
                }
                default: return false;
            }
        }
        return false;
    }
}
