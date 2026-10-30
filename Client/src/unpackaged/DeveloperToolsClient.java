package unpackaged;

import java.awt.EventQueue;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import jagex.graphics.AbstractImage;
import jagex.utils.Cache;
import jagex.utils.Deque;
import jagex.utils.JString;
import jagex.world.actors.GroundItem;
import jagex.world.actors.Npc;
import jagex.world.actors.Player;

final class DeveloperToolsClient {
    private static final LinkedBlockingQueue<FutureTask<Object>> pending =
            new LinkedBlockingQueue<FutureTask<Object>>(64);
    private static final List<Object> varHistory = new ArrayList<Object>();
    private static final List<Object> chatHistory = new ArrayList<Object>();
    private static final List<Object> scriptHistory = new ArrayList<Object>();
    private static int[] previousVarps;
    private static long eventSequence;
    private static final List<ConditionWait> waiting = new CopyOnWriteArrayList<ConditionWait>();

    static Object await(Callable<Object> action, Map<String, Object> condition, int timeout) throws Exception {
        CompletableFuture<Object> completed = new CompletableFuture<Object>();
        try {
            onClientThread(() -> {
                if (waiting.size() >= 64) throw new IllegalStateException("Too many active waits");
                String before = dialogueFingerprint();
                read("wait_for", condition);
                long afterEvent = eventSequence;
                Object invoked = action == null ? null : action.call();
                waiting.add(new ConditionWait(completed, condition, invoked, before, afterEvent, timeout));
                return null;
            });
            return completed.get(timeout + 5000L, TimeUnit.MILLISECONDS);
        } finally {
            completed.cancel(false);
            waiting.removeIf(wait -> wait.completed == completed);
        }
    }

    private static String dialogueFingerprint() {
        return McpJson.write(McpJson.object("roots", roots(), "dialogue", compactDialogue()));
    }

    private static Map<String, Object> compactDialogue() {
        int group = Class39_Sub5_Sub14.anInt1912;
        List<Object> lines = new ArrayList<Object>();
        if (group >= 0) for (Object entry : widgets(group)) {
            Map<String, Object> widget = McpJson.asObject(entry);
            if (!Boolean.TRUE.equals(widget.get("hidden")) && !"".equals(widget.get("text")))
                lines.add(McpJson.object("componentId", widget.get("componentId"),
                        "text", widget.get("text"), "buttonType", widget.get("buttonType")));
        }
        return McpJson.object("interfaceId", group, "lines", lines);
    }

    private static final class ConditionWait {
        final CompletableFuture<Object> completed;
        final Map<String, Object> condition;
        final Object invoked;
        final String before;
        final long deadline;
        final long afterEvent;

        ConditionWait(CompletableFuture<Object> completed, Map<String, Object> condition,
                Object invoked, String before, long afterEvent, int timeout) {
            this.completed = completed; this.condition = condition;
            this.invoked = invoked; this.before = before; this.afterEvent = afterEvent;
            deadline = System.nanoTime() + TimeUnit.MILLISECONDS.toNanos(timeout);
        }

        void check() {
            if (completed.isDone()) return;
            try {
                boolean matched = "dialogue_changed".equals(condition.get("condition"))
                        ? !before.equals(dialogueFingerprint())
                        : invoked != null && "chat_message".equals(condition.get("condition"))
                                ? chatMatches(string(condition, "textContains", ""), afterEvent)
                                : Boolean.TRUE.equals(read("wait_for", condition));
                if (matched) completed.complete(invoked == null ? McpJson.object("matched", true)
                        : McpJson.object("matched", true, "actionResult", invoked, "snapshot", read("get_snapshot", McpJson.object())));
                else if (System.nanoTime() >= deadline) completed.completeExceptionally(
                        new TimeoutException("Condition timed out: " + condition.get("condition")));
            } catch (Exception exception) { completed.completeExceptionally(exception); }
        }
    }

    static Object onClientThread(Callable<Object> action) throws Exception {
        FutureTask<Object> task = new FutureTask<Object>(action);
        if (!pending.offer(task)) throw new IllegalStateException("Client work queue is full");
        try { return task.get(5, TimeUnit.SECONDS); }
        finally { task.cancel(false); pending.remove(task); }
    }

    static void pump() {
        if (!DeveloperToolsServer.isRunning()) return;
        int work = Math.min(8, pending.size());
        for (int i = 0; i < work; i++) {
            FutureTask<Object> task = pending.poll();
            if (task == null) break;
            task.run();
        }
        for (ConditionWait wait : waiting) wait.check();
        waiting.removeIf(wait -> wait.completed.isDone());
        int[] values = Class66.stateValues;
        if (values == null) { previousVarps = null; return; }
        if (previousVarps != null && values.length == previousVarps.length) {
            for (int i = 0; i < values.length; i++) {
                if (values[i] != previousVarps[i]) record(varHistory,
                        McpJson.object("type", "varp", "id", i, "old", previousVarps[i], "value", values[i]));
            }
        }
        previousVarps = values.clone();
    }

    static void reset() {
        FutureTask<Object> task;
        while ((task = pending.poll()) != null) task.cancel(false);
        for (ConditionWait wait : waiting) wait.completed.cancel(false);
        waiting.clear();
        previousVarps = null;
        varHistory.clear(); chatHistory.clear(); scriptHistory.clear();
    }

    static void chat(JString sender, JString message, int type) {
        if (DeveloperToolsServer.isRunning()) record(chatHistory,
                McpJson.object("type", type, "sender", text(sender), "text", text(message)));
    }

    static void script(int id, Object[] args) {
        if (!DeveloperToolsServer.isRunning()) return;
        List<Object> arguments = new ArrayList<Object>();
        for (int i = 1; i < args.length; i++) {
            Object value = args[i];
            arguments.add(value instanceof JString ? text((JString) value)
                    : value instanceof Number ? value : null);
        }
        record(scriptHistory, McpJson.object("id", id, "args", arguments));
    }

    private static void record(List<Object> history, Map<String, Object> entry) {
        entry.put("timestamp", System.currentTimeMillis()); entry.put("tick", Class2.logicCycle);
        entry.put("sequence", ++eventSequence);
        history.add(entry);
        if (history.size() > 500) history.remove(0);
    }

    static List<Object> tools() {
        List<Object> tools = new ArrayList<Object>();
        tool(tools, "screenshot", "PNG of the native 765x503 screen; optional crop.",
                "x:integer", "y:integer", "width:integer", "height:integer");
        tool(tools, "get_client_state", "Game state, tick, region, local player and open interface roots.");
        tool(tools, "get_skills", "All revision-443 skill levels and XP.");
        tool(tools, "list_npcs", "Loaded NPCs, optionally filtered by name.", "nameFilter:string");
        tool(tools, "seq_info", "An animation's frame delays (20 ms client cycles) and total length; gfx looks up a graphic's animation.", "id:number", "gfx:number");
        tool(tools, "list_players", "Loaded players and local player.");
        tool(tools, "list_objects", "Scene objects on the current plane, within radius (default 16).", "nameFilter:string", "radius:integer");
        tool(tools, "interact_object", "Use a cache-defined option on the nearest matching scene object.", "nameFilter:string!", "option:string!", "radius:integer");
        tool(tools, "list_ground_items", "Ground items on the current plane, within radius (default 16).", "nameFilter:string", "radius:integer");
        tool(tools, "pickup_item", "Take a ground item at a world tile through the normal menu dispatcher.", "itemId:integer!", "x:integer!", "y:integer!");
        tool(tools, "item_action", "Use a cache-defined action on an inventory item (e.g. Drop, Eat, Wield).", "itemName:string!", "option:string!", "slot:integer");
        tool(tools, "widget_action", "Activate a standard old-format widget button using its normal menu action.", "componentId:integer!");
        tool(tools, "list_interfaces", "Open interface roots and loaded widget group IDs.");
        tool(tools, "dump_interface", "Loaded widget definitions for a group. Does not open it.", "interfaceId:integer!");
        tool(tools, "get_widget", "Loaded widget text, parent, size, relative position, items and ops.", "componentId:integer!");
        tool(tools, "get_inventory", "Client item container (93 inventory, 94 equipment, 95 bank).", "inventoryId:integer!");
        tool(tools, "get_dialogue", "Current chatbox interface and its visible widget text.");
        tool(tools, "get_menu", "Current context menu entries with stable indices for this snapshot.");
        tool(tools, "click_menu_option", "Invoke a matching current menu entry using the client dispatcher.", "option:string!", "target:string");
        tool(tools, "interact_npc", "Use a cache-defined NPC option, default Talk-to.", "npcName:string!", "option:string", "npcIndex:integer");
        tool(tools, "use_item_on_npc", "Use an inventory item on the nearest matching NPC, as Use then clicking the NPC.", "itemName:string!", "npcName:string!", "npcIndex:integer");
        tool(tools, "use_item_on_object", "Use an inventory item on the nearest matching scene object, as Use then clicking the object.", "itemName:string!", "nameFilter:string!", "radius:integer");
        tool(tools, "walk_to", "Route the local player to a world tile through the normal client pathfinder.", "x:integer!", "y:integer!");
        tool(tools, "get_var", "Read varp, varbit, varc_int or varc_string.", "type:string!", "id:integer!");
        tool(tools, "get_var_history", "Recent varp changes, recorded once per client tick.", "sinceMs:integer");
        tool(tools, "get_chat_history", "Chat received since developer tools started.", "sinceMs:integer");
        tool(tools, "get_script_history", "Clientscript calls since developer tools started.", "sinceMs:integer");
        tool(tools, "click", "Click native screen coordinates (button 1 left, 3 right).", "x:integer!", "y:integer!", "button:integer");
        tool(tools, "hover", "Move the mouse in native screen coordinates.", "x:integer!", "y:integer!");
        tool(tools, "drag", "Drag between native screen coordinates.", "x:integer!", "y:integer!", "toX:integer!", "toY:integer!");
        tool(tools, "press_key", "Press ENTER, SPACE, ESCAPE, TAB, arrows, F1-F12, or one character.", "key:string!");
        tool(tools, "type_chat", "Type into the current focused field/chatbox and press Enter. Login must be completed first.", "text:string!");
        tool(tools, "enter_input", "Type into the current dialogue input and press Enter.", "text:string!");
        tool(tools, "continue_dialogue", "Activate the current dialogue's Continue button.");
        tool(tools, "select_option", "Activate a numbered dialogue option (1-5).", "option:integer!");
        tool(tools, "wait_for", "Wait on client ticks for at_tile, dialogue, dialogue_changed, interface_open, interface_closed, chat_message or logged_in. Chat matching ignores case.",
                "condition:string!", "x:integer", "y:integer", "interfaceId:integer", "textContains:string", "timeoutMs:integer");
        tool(tools, "get_snapshot", "Compact state, combat HP/prayer/target/active-prayer data, dialogue and inventory in one read. Prefer this to screenshots for routine inspection.");
        tool(tools, "act_and_wait", "Perform one action and return confirmed fresh state, dialogue and inventory in one call. Prefer this to separate action/wait/read calls. For dialogue use wait.condition=dialogue_changed; for walking use at_tile; for bank use interface_open.",
                "action:string!", "arguments:object!", "wait:object!");
        return tools;
    }

    private static void tool(List<Object> tools, String name, String description, String... fields) {
        Map<String, Object> properties = McpJson.object();
        List<String> required = new ArrayList<String>();
        for (String field : fields) {
            String[] parts = field.replace("!", "").split(":");
            properties.put(parts[0], McpJson.object("type", parts[1]));
            if (field.endsWith("!")) required.add(parts[0]);
        }
        boolean readOnly = name.startsWith("get_") || name.startsWith("list_")
                || name.equals("screenshot") || name.equals("dump_interface") || name.equals("wait_for");
        tools.add(McpJson.object("name", name, "description", description,
                "inputSchema", McpJson.object("type", "object", "properties", properties,
                        "required", required, "additionalProperties", false),
                "annotations", McpJson.object("readOnlyHint", readOnly, "openWorldHint", false)));
    }

    static void validate(String name, Map<String, Object> args) {
        Map<String, Object> schema = null;
        for (Object value : tools()) {
            Map<String, Object> tool = McpJson.asObject(value);
            if (name.equals(tool.get("name"))) schema = McpJson.asObject(tool.get("inputSchema"));
        }
        if (schema == null) throw new IllegalArgumentException("Unknown tool: " + name);
        Map<String, Object> properties = McpJson.asObject(schema.get("properties"));
        for (Object required : (List<?>) schema.get("required"))
            if (!args.containsKey(required)) throw new IllegalArgumentException("Missing " + required);
        for (Map.Entry<String, Object> entry : args.entrySet()) {
            if (!properties.containsKey(entry.getKey())) throw new IllegalArgumentException("Unknown argument: " + entry.getKey());
            String type = (String) McpJson.asObject(properties.get(entry.getKey())).get("type");
            Object value = entry.getValue();
            if ("string".equals(type) ? !(value instanceof String)
                    : "object".equals(type) ? !(value instanceof Map)
                    : !(value instanceof Number) || ((Number) value).doubleValue() != ((Number) value).intValue())
                throw new IllegalArgumentException("Invalid " + type + " argument: " + entry.getKey());
        }
    }

    static Object read(String name, Map<String, Object> args) {
        switch (name) {
            case "get_client_state": return state();
            case "get_snapshot": return McpJson.object("state", state(), "combat", combatSnapshot(),
                    "dialogue", compactDialogue(), "inventory", compactInventory());
            case "get_skills": {
                List<Object> skills = new ArrayList<Object>();
                for (int i = 0; i < 21; i++) skills.add(McpJson.object("id", i,
                        "level", Class39_Sub12.anIntArray1491 == null ? null : Class39_Sub12.anIntArray1491[i],
                        "boostedLevel", Class31.anIntArray555 == null ? null : Class31.anIntArray555[i],
                        "xp", Class39_Sub14.anIntArray1543 == null ? null : Class39_Sub14.anIntArray1543[i]));
                return skills;
            }
            case "list_npcs": return npcs(string(args, "nameFilter", ""));
            case "seq_info": {
                int seqId = integer(args, "gfx", -1) >= 0 ? jagex.utils.Huffmans.method881(0, integer(args, "gfx", -1)).anInt2126 : integer(args, "id", -1);
                Class39_Sub5_Sub11 seq = seqId < 0 ? null : Class62_Sub1.method1064(seqId, (byte) 54);
                Map<String, Object> info = new java.util.LinkedHashMap<String, Object>();
                int total = 0;
                List<Object> delays = new ArrayList<Object>();
                if (seq != null && seq.anIntArray1831 != null) for (int d : seq.anIntArray1831) { delays.add(d); total += d; }
                info.put("seq", seqId); info.put("frames", delays.size()); info.put("delays", delays); info.put("cycles", total); info.put("ms", total * 20);
                info.put("loopOffset", seq == null ? null : seq.anInt1825);
                return info;
            }
            case "list_players": {
                List<Object> players = new ArrayList<Object>();
                if (Class31.state == 30 && Class14.aClass39_Sub5_Sub4_Sub4_Sub2Array211 != null)
                    for (int i = 0; i < Class14.aClass39_Sub5_Sub4_Sub4_Sub2Array211.length; i++) {
                        Player player = Class14.aClass39_Sub5_Sub4_Sub4_Sub2Array211[i];
                        if (player != null) players.add(player(player, i));
                    }
                return players;
            }
            case "list_objects": return objects(args);
            case "interact_object": {
                requireLogin();
                List<Object> objects = objects(args);
                objects.sort((left, right) -> Integer.compare(distance(McpJson.asObject(left)), distance(McpJson.asObject(right))));
                String option = string(args, "option", "");
                for (Object entry : objects) {
                    Map<String, Object> object = McpJson.asObject(entry);
                    List<?> actions = (List<?>) object.get("actions");
                    for (int i = 0; i < actions.size(); i++) if (option.equalsIgnoreCase((String) actions.get(i))) {
                        int[] opcodes = {55, 57, 43, 6, 1005};
                        invoke(opcodes[i], ((Number) object.get("x")).intValue() - Class65.anInt1145,
                                ((Number) object.get("y")).intValue() - JKeyListener.anInt618,
                                ((Number) object.get("hash")).intValue(), option);
                        return McpJson.object("invoked", true, "object", object);
                    }
                }
                throw new IllegalArgumentException("No matching scene object with that option");
            }
            case "use_item_on_object": {
                requireLogin();
                List<Object> objects = objects(args);
                if (objects.isEmpty()) throw new IllegalArgumentException("No matching scene object");
                objects.sort((left, right) -> Integer.compare(distance(McpJson.asObject(left)), distance(McpJson.asObject(right))));
                Map<String, Object> object = McpJson.asObject(objects.get(0));
                int[] item = selectInventoryItem(string(args, "itemName", ""));
                invoke(45, ((Number) object.get("x")).intValue() - Class65.anInt1145,
                        ((Number) object.get("y")).intValue() - JKeyListener.anInt618,
                        ((Number) object.get("hash")).intValue(), "Use");
                return McpJson.object("invoked", true, "slot", item[0], "itemId", item[1], "object", object);
            }
            case "list_ground_items": return groundItems(args);
            case "pickup_item": {
                requireLogin();
                int x = integer(args, "x", -1), y = integer(args, "y", -1), id = integer(args, "itemId", -1);
                for (Object entry : groundItems(McpJson.object("radius", 104))) {
                    Map<String, Object> item = McpJson.asObject(entry);
                    if (((Number) item.get("id")).intValue() == id && ((Number) item.get("x")).intValue() == x
                            && ((Number) item.get("y")).intValue() == y) {
                        invoke(34, x - Class65.anInt1145, y - JKeyListener.anInt618, id, "Take");
                        return McpJson.object("invoked", true);
                    }
                }
                throw new IllegalArgumentException("Ground item not present at that tile");
            }
            case "item_action": return itemAction(args);
            case "widget_action": return activate(findWidget(integer(args, "componentId", -1)));
            case "continue_dialogue": case "select_option": return dialogueAction(name, args);
            case "get_widget": return widget(findWidget(integer(args, "componentId", -1)));
            case "dump_interface": return widgets(integer(args, "interfaceId", -1));
            case "list_interfaces": return interfaces();
            case "get_dialogue": return McpJson.object("interfaceId", Class39_Sub5_Sub14.anInt1912,
                    "widgets", Class39_Sub5_Sub14.anInt1912 < 0 ? Collections.emptyList()
                            : widgets(Class39_Sub5_Sub14.anInt1912));
            case "get_inventory": {
                int id = bounded(args, "inventoryId", 0, 0, 65535);
                Class39_Sub13 container = Class14.aClass19_213 == null ? null
                        : (Class39_Sub13) Class14.aClass19_213.fetch(id == 93 ? 0 : id == 94 ? 25 : id == 95 ? 89 : id);
                if (container == null) return McpJson.object("inventoryId", id, "loaded", false, "items", Collections.emptyList());
                List<Object> items = new ArrayList<Object>();
                for (int i = 0; i < container.anIntArray1505.length; i++)
                    items.add(McpJson.object("slot", i, "id", container.anIntArray1505[i], "quantity", container.anIntArray1504[i],
                            "name", container.anIntArray1505[i] < 0 ? "" : text(Class26.getItemDefinition(container.anIntArray1505[i]).aClass3_1661)));
                return McpJson.object("inventoryId", id, "loaded", true, "items", items);
            }
            case "get_menu": return menu();
            case "click_menu_option": {
                requireLogin();
                String option = string(args, "option", ""), target = string(args, "target", "");
                for (int i = Class39_Sub5_Sub11.anInt1841 - 1; i >= 0; i--)
                    if ((text(Class33.aClass3Array601[i]).equalsIgnoreCase(option)
                            || text(Class33.aClass3Array601[i]).toLowerCase(Locale.ROOT).startsWith(option.toLowerCase(Locale.ROOT) + " "))
                            && text(Class33.aClass3Array601[i]).toLowerCase(Locale.ROOT).contains(target.toLowerCase(Locale.ROOT))) {
                        ScriptState.method278(i, 1); return McpJson.object("invoked", true, "option", option);
                    }
                throw new IllegalArgumentException("Menu option not present; hover/right-click the target first");
            }
            case "walk_to": {
                requireLogin();
                int x = integer(args, "x", -1) - Class65.anInt1145;
                int y = integer(args, "y", -1) - JKeyListener.anInt618;
                if (x < 0 || y < 0 || x >= 104 || y >= 104) throw new IllegalArgumentException("Tile outside loaded scene");
                Player p = Cache.aClass39_Sub5_Sub4_Sub4_Sub2_109;
                boolean routed = Class26.method293(24134, 0, y, 0, 0, 0, 0, false, 0,
                        p.anIntArray2314[0], p.anIntArray2255[0], x);
                if (!routed) throw new IllegalStateException("No route to tile");
                return McpJson.object("routeRequested", true);
            }
            case "get_var": return variable(args);
            case "get_var_history": return history(varHistory, args);
            case "get_chat_history": return history(chatHistory, args);
            case "get_script_history": return history(scriptHistory, args);
            case "wait_for": return condition(args);
            case "interact_npc": return interactNpc(args);
            case "use_item_on_npc": return useItemOnNpc(args);
            default: throw new IllegalArgumentException("Unknown client tool: " + name);
        }
    }

    private static Map<String, Object> state() {
        Player local = Cache.aClass39_Sub5_Sub4_Sub4_Sub2_109;
        return McpJson.object("state", Class31.state, "loggedIn", Class31.state == 30,
                "tick", Class2.logicCycle, "width", 765, "height", 503,
                "baseX", Class65.anInt1145, "baseY", JKeyListener.anInt618, "plane", NameTable.height,
                "player", Class31.state == 30 && local != null ? player(local, -1) : null,
                "appearance", local != null && local.aClass45_2516 != null
                        ? local.aClass45_2516.anIntArray848 : null,
                "viewportInterface", Class39_Sub11.anInt1478,
                "chatboxInterface", Class39_Sub5_Sub14.anInt1912,
                "sidebarInterface", jagex.world.actors.StillGraphic.anInt2338);
    }


    private static Map<String, Object> combatSnapshot() {
        Player local = Cache.aClass39_Sub5_Sub4_Sub4_Sub2_109;
        Integer hp = skillLevel(3, true), maxHp = skillLevel(3, false);
        Integer prayer = skillLevel(5, true), maxPrayer = skillLevel(5, false);
        List<Object> activePrayers = new ArrayList<Object>();
        int[] prayerVarps = {83,84,85,86,87,88,89,90,91,92,93,94,95,96,97,98,99,100,862,863,864,865,866,867};
        String[] prayerNames = {"Thick Skin","Burst of Strength","Clarity of Thought","Rock Skin","Superhuman Strength","Improved Reflexes","Rapid Restore","Rapid Heal","Protect Item","Steel Skin","Ultimate Strength","Incredible Reflexes","Protect from Magic","Protect from Range","Protect from Melee","Retribution","Redemption","Smite","Sharp Eye","Mystic Will","Hawk Eye","Mystic Lore","Eagle Eye","Mystic Might"};
        if (Class66.stateValues != null) for (int i = 0; i < prayerVarps.length; i++) {
            int id = prayerVarps[i];
            if (id < Class66.stateValues.length && Class66.stateValues[id] != 0)
                activePrayers.add(McpJson.object("id", i, "name", prayerNames[i], "varp", id));
        }
        Map<String, Object> target = local == null ? null : combatTarget(local);
        return McpJson.object("hp", hp, "maxHp", maxHp, "prayer", prayer, "maxPrayer", maxPrayer,
                "inCombat", target != null, "target", target, "activePrayers", activePrayers);
    }

    private static Integer skillLevel(int id, boolean boosted) {
        int[] values = boosted ? Class31.anIntArray555 : Class39_Sub12.anIntArray1491;
        return values == null || id < 0 || id >= values.length ? null : values[id];
    }

    private static Map<String, Object> combatTarget(Player local) {
        int targetIndex = local.anInt2260;
        if (targetIndex < 0) return null;
        if (targetIndex < 32768 && GroundItem.aClass39_Sub5_Sub4_Sub4_Sub1Array2241 != null
                && targetIndex < GroundItem.aClass39_Sub5_Sub4_Sub4_Sub1Array2241.length) {
            Npc npc = GroundItem.aClass39_Sub5_Sub4_Sub4_Sub1Array2241[targetIndex];
            if (npc == null || npc.aClass39_Sub5_Sub13_2492 == null) return null;
            NpcDefinition definition = npc.aClass39_Sub5_Sub13_2492;
            if (definition.anIntArray1878 != null) definition = definition.method721(0);
            if (definition == null) return null;
            Map<String, Object> target = actor(npc, targetIndex);
            int currentHp = npc.anInt2318, maximumHp = npc.anInt2269;
            target.put("type", "npc"); target.put("id", definition.id);
            target.put("name", text(definition.aClass3_1881));
            target.put("hp", currentHp); target.put("maxHp", maximumHp);
            target.put("hpPercent", maximumHp > 0 ? currentHp * 100 / maximumHp : null);
            target.put("healthBarVisible", Class2.logicCycle < npc.anInt2252);
            return target;
        }
        int playerIndex = targetIndex - 32768;
        if (Class39_Sub13.anInt1501 == playerIndex) playerIndex = 2047;
        if (Class14.aClass39_Sub5_Sub4_Sub4_Sub2Array211 == null || playerIndex < 0
                || playerIndex >= Class14.aClass39_Sub5_Sub4_Sub4_Sub2Array211.length) return null;
        Player player = Class14.aClass39_Sub5_Sub4_Sub4_Sub2Array211[playerIndex];
        if (player == null) return null;
        Map<String, Object> target = player(player, playerIndex);
        target.put("type", "player");
        return target;
    }

    private static List<Object> objects(Map<String, Object> args) {
        List<Object> result = new ArrayList<Object>();
        if (Class31.state != 30 || Class44.aClass38_836 == null) return result;
        int radius = bounded(args, "radius", 16, 1, 104);
        String filter = string(args, "nameFilter", "").toLowerCase(Locale.ROOT);
        for (int x = 0; x < 104; x++) for (int y = 0; y < 104; y++) {
            if (!near(x, y, radius)) continue;
            int[] hashes = {Class44.aClass38_836.method379(NameTable.height, x, y),
                    Class44.aClass38_836.method363(NameTable.height, x, y),
                    Class44.aClass38_836.method384(NameTable.height, x, y),
                    Class44.aClass38_836.method404(NameTable.height, x, y)};
            for (int type = 0; type < hashes.length; type++) {
                int hash = hashes[type];
                if (hash == 0) continue;
                ObjectDefinition definition = Canvas_Sub1.method40(hash >> 14 & 32767, (byte) 108);
                if (definition.anIntArray1961 != null) definition = definition.method733(0);
                if (definition == null || !text(definition.aClass3_1932).toLowerCase(Locale.ROOT).contains(filter)) continue;
                int flags = Class44.aClass38_836.method359(NameTable.height, x, y, hash);
                result.add(McpJson.object("id", definition.anInt1931, "hash", hash, "name", text(definition.aClass3_1932),
                        "x", x + Class65.anInt1145, "y", y + JKeyListener.anInt618, "plane", NameTable.height,
                        "sceneType", type, "shape", flags & 31, "orientation", flags >> 6 & 3,
                        "actions", strings(definition.aClass3Array1964)));
            }
        }
        return result;
    }

    private static List<Object> groundItems(Map<String, Object> args) {
        List<Object> result = new ArrayList<Object>();
        if (Class31.state != 30 || Class20.groundItems == null) return result;
        int radius = bounded(args, "radius", 16, 1, 104);
        String filter = string(args, "nameFilter", "").toLowerCase(Locale.ROOT);
        for (int x = 0; x < 104; x++) for (int y = 0; y < 104; y++) {
            if (!near(x, y, radius)) continue;
            Deque items = Class20.groundItems[NameTable.height][x][y];
            if (items == null) continue;
            for (GroundItem item = (GroundItem) items.getFirst(); item != null; item = (GroundItem) items.getNext()) {
                String name = text(Class26.getItemDefinition(item.itemId).aClass3_1661);
                if (name.toLowerCase(Locale.ROOT).contains(filter)) result.add(McpJson.object("id", item.itemId,
                        "quantity", item.anInt2243, "name", name, "x", x + Class65.anInt1145,
                        "y", y + JKeyListener.anInt618, "plane", NameTable.height));
            }
        }
        return result;
    }

    private static boolean near(int x, int y, int radius) {
        Player player = Cache.aClass39_Sub5_Sub4_Sub4_Sub2_109;
        return player != null && Math.abs(x - (player.anInt2301 >> 7)) <= radius
                && Math.abs(y - (player.anInt2275 >> 7)) <= radius;
    }

    private static int distance(Map<String, Object> entry) {
        Player player = Cache.aClass39_Sub5_Sub4_Sub4_Sub2_109;
        return Math.abs(((Number) entry.get("x")).intValue() - Class65.anInt1145 - (player.anInt2301 >> 7))
                + Math.abs(((Number) entry.get("y")).intValue() - JKeyListener.anInt618 - (player.anInt2275 >> 7));
    }

    private static Object itemAction(Map<String, Object> args) {
        requireLogin();
        Widget inventory = findWidget(149 << 16);
        String name = string(args, "itemName", ""), option = string(args, "option", "");
        int requested = integer(args, "slot", -1);
        if (inventory.anIntArray2087 != null) for (int slot = 0; slot < inventory.anIntArray2087.length; slot++) {
            if (requested >= 0 && requested != slot) continue;
            int id = inventory.anIntArray2087[slot] - 1;
            if (id < 0) continue;
            ItemDefinition item = Class26.getItemDefinition(id);
            if (!text(item.aClass3_1661).equalsIgnoreCase(name)) continue;
            List<String> actions = strings(item.aClass3Array1657);
            for (int i = 0; i < actions.size(); i++) if (actions.get(i).equalsIgnoreCase(option)
                    || i == 4 && actions.get(i).isEmpty() && option.equalsIgnoreCase("Drop")) {
                if (!inventory.method769(82)) throw new IllegalArgumentException("Inventory item actions are disabled");
                invoke(new int[] {28, 16, 54, 32, 19}[i], slot, inventory.anInt2084, id, option);
                return McpJson.object("invoked", true, "slot", slot, "id", id);
            }
        }
        throw new IllegalArgumentException("No matching inventory item with that action");
    }

    private static Object dialogueAction(String name, Map<String, Object> args) {
        requireLogin();
        int group = Class39_Sub5_Sub14.anInt1912;
        if (group < 0 || Class62_Sub1.widgets == null || group >= Class62_Sub1.widgets.length
                || Class62_Sub1.widgets[group] == null) throw new IllegalStateException("No dialogue open");
        List<Widget> buttons = new ArrayList<Widget>();
        for (Widget widget : Class62_Sub1.widgets[group])
            if (widget != null && !widget.aBoolean2055 && widget.anInt2089 == 6) buttons.add(widget);
        buttons.sort((left, right) -> Integer.compare(left.anInt2021, right.anInt2021));
        if (name.equals("continue_dialogue")) {
            if (buttons.size() != 1) throw new IllegalStateException("Dialogue needs an option, not Continue");
            return activate(buttons.get(0));
        }
        int selected = bounded(args, "option", 1, 1, 5);
        if (buttons.size() < 2 || selected > buttons.size()) throw new IllegalArgumentException("Dialogue option unavailable");
        return activate(buttons.get(selected - 1));
    }

    /** Clicks the open dialogue's single Continue button; throws when there is none. */
    static void continueDialogue() {
        dialogueAction("continue_dialogue", Collections.<String, Object>emptyMap());
    }

    private static Object activate(Widget widget) {
        requireLogin();
        if (!roots().contains(widget.anInt2084 >>> 16)) throw new IllegalArgumentException("Widget interface is not open");
        if (widget.aBoolean2055) throw new IllegalArgumentException("Widget is hidden");
        int opcode;
        switch (widget.anInt2089) {
            case 1: opcode = 20; break;
            case 2: opcode = 14; break;
            case 3: opcode = 29; break;
            case 4: opcode = 36; break;
            case 5: opcode = 41; break;
            case 6:
                if (Class39_Sub10.anInt1420 != -1) throw new IllegalStateException("Dialogue response already pending");
                opcode = 8; break;
            default: throw new IllegalArgumentException("Widget has no standard button action; use its current menu or screen click");
        }
        invoke(opcode, widget.anInt2089 == 6 || widget.anInt2089 == 2 ? -1 : 0,
                widget.anInt2084, 0, text(widget.aClass3_2068));
        return McpJson.object("invoked", true, "componentId", widget.anInt2084);
    }

    private static Map<String, Object> player(Player player, int index) {
        Map<String, Object> info = actor(player, index);
        info.put("name", text(player.aClass3_2521));
        info.put("local", player == Cache.aClass39_Sub5_Sub4_Sub4_Sub2_109);
        return info;
    }

    private static Map<String, Object> actor(Class39_Sub5_Sub4_Sub4 actor, int index) {
        return McpJson.object("index", index, "x", (actor.anInt2301 >> 7) + Class65.anInt1145,
                "y", (actor.anInt2275 >> 7) + JKeyListener.anInt618, "plane", NameTable.height,
                "animation", actor.anInt2268, "movementAnimation", actor.anInt2303,
                "pathLength", actor.anInt2312);
    }

    private static List<Object> npcs(String filter) {
        List<Object> result = new ArrayList<Object>();
        if (Class31.state != 30 || GroundItem.aClass39_Sub5_Sub4_Sub4_Sub1Array2241 == null) return result;
        for (int i = 0; i < GroundItem.aClass39_Sub5_Sub4_Sub4_Sub1Array2241.length; i++) {
            Npc npc = GroundItem.aClass39_Sub5_Sub4_Sub4_Sub1Array2241[i];
            if (npc == null || npc.aClass39_Sub5_Sub13_2492 == null) continue;
            NpcDefinition definition = npc.aClass39_Sub5_Sub13_2492;
            if (definition.anIntArray1878 != null) definition = definition.method721(0);
            if (definition == null) continue;
            String name = text(definition.aClass3_1881);
            if (!name.toLowerCase(Locale.ROOT).contains(filter.toLowerCase(Locale.ROOT))) continue;
            Map<String, Object> info = actor(npc, i);
            info.put("id", definition.id); info.put("name", name);
            info.put("standAnim", definition.anInt1887); info.put("walkAnim", definition.anInt1861); info.put("size", definition.anInt1870);
            info.put("actions", strings(definition.aClass3Array1866)); result.add(info);
        }
        return result;
    }

    private static Object interactNpc(Map<String, Object> args) {
        requireLogin();
        String name = string(args, "npcName", ""), option = string(args, "option", "Talk-to");
        int requested = integer(args, "npcIndex", -1), selected = -1, action = -1, best = Integer.MAX_VALUE;
        Player local = Cache.aClass39_Sub5_Sub4_Sub4_Sub2_109;
        for (Object entry : npcs(name)) {
            Map<String, Object> info = McpJson.asObject(entry);
            int index = ((Number) info.get("index")).intValue();
            if (requested >= 0 && index != requested) continue;
            Npc npc = GroundItem.aClass39_Sub5_Sub4_Sub4_Sub1Array2241[index];
            List<?> actions = (List<?>) info.get("actions");
            for (int i = 0; i < actions.size(); i++) if (option.equalsIgnoreCase((String) actions.get(i))) {
                int distance = Math.abs(npc.anInt2301 - local.anInt2301) + Math.abs(npc.anInt2275 - local.anInt2275);
                if (distance < best) { selected = index; action = i; best = distance; }
            }
        }
        if (selected < 0) throw new IllegalArgumentException("No matching loaded NPC with that option");
        int[] opcodes = {42, 17, 13, 53, 46};
        invoke(opcodes[action], 0, 0, selected, option);
        return McpJson.object("npcIndex", selected, "option", option, "invoked", true);
    }

    /** Selects an inventory item with its Use action (menu 23), ready for an item-on-target action. Returns {slot, id}. */
    private static int[] selectInventoryItem(String itemName) {
        Widget inventory = findWidget(149 << 16);
        if (inventory.anIntArray2087 != null) for (int i = 0; i < inventory.anIntArray2087.length; i++) {
            int id = inventory.anIntArray2087[i] - 1;
            if (id >= 0 && text(Class26.getItemDefinition(id).aClass3_1661).equalsIgnoreCase(itemName)) {
                invoke(23, i, inventory.anInt2084, id, "Use");
                return new int[]{i, id};
            }
        }
        throw new IllegalArgumentException("No matching inventory item");
    }

    /** Uses an inventory item on the nearest matching NPC (menu 47). */
    private static Object useItemOnNpc(Map<String, Object> args) {
        requireLogin();
        String name = string(args, "npcName", "");
        int requested = integer(args, "npcIndex", -1), selected = -1, best = Integer.MAX_VALUE;
        Player local = Cache.aClass39_Sub5_Sub4_Sub4_Sub2_109;
        for (Object entry : npcs(name)) {
            int index = ((Number) McpJson.asObject(entry).get("index")).intValue();
            if (requested >= 0 && index != requested) continue;
            Npc npc = GroundItem.aClass39_Sub5_Sub4_Sub4_Sub1Array2241[index];
            int distance = Math.abs(npc.anInt2301 - local.anInt2301) + Math.abs(npc.anInt2275 - local.anInt2275);
            if (distance < best) { selected = index; best = distance; }
        }
        if (selected < 0) throw new IllegalArgumentException("No matching loaded NPC");
        int[] item = selectInventoryItem(string(args, "itemName", ""));
        invoke(47, 0, 0, selected, "Use");
        return McpJson.object("invoked", true, "slot", item[0], "itemId", item[1], "npcIndex", selected);
    }

    private static void invoke(int opcode, int param0, int param1, int identifier, String option) {
        int slot = Class39_Sub5_Sub11.anInt1841;
        if (Class12.anIntArray196 == null || slot >= Class12.anIntArray196.length)
            throw new IllegalStateException("Menu unavailable");
        int old0 = Class12.anIntArray196[slot], old1 = Class43.anIntArray820[slot];
        int oldOp = JKeyListener.anIntArray621[slot], oldId = NameTable.anIntArray176[slot];
        JString oldTarget = Class33.aClass3Array601[slot];
        try {
            Class12.anIntArray196[slot] = param0; Class43.anIntArray820[slot] = param1;
            JKeyListener.anIntArray621[slot] = opcode; NameTable.anIntArray176[slot] = identifier;
            Class33.aClass3Array601[slot] = Class39_Sub5_Sub9.createJstring(option);
            ScriptState.method278(slot, 1);
        } finally {
            Class12.anIntArray196[slot] = old0; Class43.anIntArray820[slot] = old1;
            JKeyListener.anIntArray621[slot] = oldOp; NameTable.anIntArray176[slot] = oldId;
            Class33.aClass3Array601[slot] = oldTarget;
        }
    }

    private static List<Object> menu() {
        List<Object> result = new ArrayList<Object>();
        for (int i = 0; i < Class39_Sub5_Sub11.anInt1841; i++) result.add(McpJson.object("index", i,
                "option", text(Class33.aClass3Array601[i]), "target", text(Class33.aClass3Array601[i]),
                "opcode", JKeyListener.anIntArray621[i], "param0", Class12.anIntArray196[i],
                "param1", Class43.anIntArray820[i], "identifier", NameTable.anIntArray176[i]));
        return result;
    }

    private static Object variable(Map<String, Object> args) {
        String type = string(args, "type", "varp");
        int id = bounded(args, "id", -1, 0, 65535);
        Object value;
        switch (type) {
            case "varp": value = indexed(Class66.stateValues, id); break;
            case "varc_int": value = indexed(AbstractImage.globalIntVars, id); break;
            case "varc_string":
                if (Varbit.globalStrVars == null || id >= Varbit.globalStrVars.length)
                    throw new IllegalArgumentException("Variable unavailable");
                value = text(Varbit.globalStrVars[id]); break;
            case "varbit":
                if (Widget.varbitCache == null || Class66.stateValues == null)
                    throw new IllegalStateException("Varbit definitions not loaded");
                value = Class44.getVarbitValue(id); break;
            default: throw new IllegalArgumentException("Unknown variable type");
        }
        return McpJson.object("type", type, "id", id, "value", value);
    }

    private static int indexed(int[] values, int id) {
        if (values == null || id >= values.length) throw new IllegalArgumentException("Variable unavailable");
        return values[id];
    }

    private static List<Object> history(List<Object> source, Map<String, Object> args) {
        long since = System.currentTimeMillis() - bounded(args, "sinceMs", 60000, 0, Integer.MAX_VALUE);
        List<Object> result = new ArrayList<Object>();
        for (Object entry : source)
            if (((Number) McpJson.asObject(entry).get("timestamp")).longValue() >= since) result.add(entry);
        return result;
    }

    private static Object compactInventory() {
        Map<String, Object> inventory = McpJson.asObject(read("get_inventory", McpJson.object("inventoryId", 93)));
        List<?> items = (List<?>) inventory.get("items");
        List<Object> occupied = new ArrayList<Object>();
        for (Object item : items) if (((Number) McpJson.asObject(item).get("id")).intValue() >= 0) occupied.add(item);
        inventory.put("slotCount", items.size()); inventory.put("items", occupied);
        return inventory;
    }

    private static boolean chatMatches(String text, long afterEvent) {
        for (Object entry : history(chatHistory, McpJson.object("sinceMs", 60000))) {
            Map<String, Object> message = McpJson.asObject(entry);
            if (((Number) message.get("sequence")).longValue() > afterEvent
                    && ((String) message.get("text")).toLowerCase(Locale.ROOT).contains(text.toLowerCase(Locale.ROOT))) return true;
        }
        return false;
    }

    private static boolean condition(Map<String, Object> args) {
        switch (string(args, "condition", "")) {
            case "dialogue_changed": return false;
            case "logged_in": return Class31.state == 30;
            case "dialogue": return Class39_Sub5_Sub14.anInt1912 >= 0;
            case "at_tile": {
                Player p = Cache.aClass39_Sub5_Sub4_Sub4_Sub2_109;
                return Class31.state == 30 && p != null && p.anInt2312 == 0
                        && (p.anInt2301 >> 7) + Class65.anInt1145 == integer(args, "x", -1)
                        && (p.anInt2275 >> 7) + JKeyListener.anInt618 == integer(args, "y", -1);
            }
            case "interface_open": case "interface_closed": {
                int id = bounded(args, "interfaceId", -1, 0, 65535);
                boolean open = roots().contains(id);
                return "interface_open".equals(args.get("condition")) == open;
            }
            case "chat_message":
                if (!args.containsKey("textContains")) throw new IllegalArgumentException("textContains is required");
                return chatMatches((String) args.get("textContains"), -1);
            default: throw new IllegalArgumentException("Unknown wait condition");
        }
    }

    private static List<Integer> roots() {
        return Arrays.asList(Class39_Sub11.anInt1478, Class39_Sub5_Sub14.anInt1912,
                jagex.world.actors.StillGraphic.anInt2338);
    }

    private static Object interfaces() {
        List<Integer> loaded = new ArrayList<Integer>();
        if (Class62_Sub1.widgets != null) for (int i = 0; i < Class62_Sub1.widgets.length; i++)
            if (Class62_Sub1.widgets[i] != null) loaded.add(i);
        return McpJson.object("openRoots", roots(), "loadedGroups", loaded);
    }

    private static Widget findWidget(int id) {
        int group = id >>> 16, child = id & 65535;
        if (id < 0 || Class62_Sub1.widgets == null || group >= Class62_Sub1.widgets.length
                || Class62_Sub1.widgets[group] == null || child >= Class62_Sub1.widgets[group].length
                || Class62_Sub1.widgets[group][child] == null)
            throw new IllegalArgumentException("Widget is not loaded");
        return Class62_Sub1.widgets[group][child];
    }

    private static List<Object> widgets(int group) {
        if (group < 0 || Class62_Sub1.widgets == null || group >= Class62_Sub1.widgets.length
                || Class62_Sub1.widgets[group] == null) throw new IllegalArgumentException("Interface is not loaded");
        List<Object> result = new ArrayList<Object>();
        for (Widget widget : Class62_Sub1.widgets[group]) if (widget != null) result.add(widget(widget));
        return result;
    }

    private static Map<String, Object> widget(Widget widget) {
        return McpJson.object("componentId", widget.anInt2084, "parentId", widget.anInt2050,
                "type", widget.type, "buttonType", widget.anInt2089, "x", widget.anInt2091, "y", widget.anInt2021,
                "width", widget.quadWidth, "height", widget.quadHeight, "hidden", widget.aBoolean2055,
                "text", text(widget.aClass3_2029), "alternateText", text(widget.aClass3_2048),
                "spriteId", widget.anInt2093, "itemId", widget.anInt1997, "quantity", widget.anInt2096,
                "itemIdsPlusOne", widget.anIntArray2087, "itemQuantities", widget.anIntArray2073,
                "actions", strings(widget.aClass3Array2027));
    }

    static void input(String name, Map<String, Object> args) throws Exception {
        if (Class41.aCanvas778 == null) throw new IllegalStateException("Client canvas is not ready");
        if (name.equals("type_chat") || name.equals("enter_input")) {
            if (!Boolean.TRUE.equals(McpJson.asObject(onClientThread(() -> state())).get("loggedIn")))
                throw new IllegalStateException("Log in manually before typing chat or dialogue input");
            String value = string(args, "text", "");
            if (value.length() > 200 || value.chars().anyMatch(c -> c < 32 || c > 255))
                throw new IllegalArgumentException("Text must contain at most 200 printable client characters");
            for (int start = 0; start < value.length(); start += 8) {
                for (int i = start; i < Math.min(start + 8, value.length()); i++) {
                    char c = value.charAt(i);
                    int code = KeyEvent.getExtendedKeyCodeForChar(c);
                    WebClientBridge.key(KeyEvent.KEY_PRESSED, code, c);
                    WebClientBridge.key(KeyEvent.KEY_RELEASED, code, c);
                }
                EventQueue.invokeAndWait(() -> { });
                onClientThread(() -> null);
                onClientThread(() -> null);
            }
            press('\n', KeyEvent.VK_ENTER);
        } else if (name.equals("press_key")) {
            String key = string(args, "key", "");
            int code;
            char character = KeyEvent.CHAR_UNDEFINED;
            if (key.length() == 1) { character = key.charAt(0); code = KeyEvent.getExtendedKeyCodeForChar(character); }
            else {
                String normalized = key.toUpperCase(Locale.ROOT);
                if (!normalized.matches("ENTER|SPACE|ESCAPE|TAB|LEFT|RIGHT|UP|DOWN|BACK_SPACE|F([1-9]|1[0-2])"))
                    throw new IllegalArgumentException("Unsupported key");
                code = KeyEvent.class.getField("VK_" + normalized).getInt(null);
                if (code == KeyEvent.VK_SPACE) character = ' ';
                if (code == KeyEvent.VK_ENTER) character = '\n';
            }
            press(character, code);
        } else {
            int x = bounded(args, "x", -1, 0, 764), y = bounded(args, "y", -1, 0, 502);
            WebClientBridge.mouse(MouseEvent.MOUSE_MOVED, MouseEvent.NOBUTTON, x, y);
            if (name.equals("hover")) return;
            int button = bounded(args, "button", 1, 1, 3);
            int toX = name.equals("drag") ? bounded(args, "toX", -1, 0, 764) : x;
            int toY = name.equals("drag") ? bounded(args, "toY", -1, 0, 502) : y;
            WebClientBridge.mouse(MouseEvent.MOUSE_PRESSED, button, x, y);
            try {
                if (name.equals("drag")) {
                    for (int i = 1; i <= 10; i++) {
                        Thread.sleep(20);
                        WebClientBridge.mouse(MouseEvent.MOUSE_DRAGGED, MouseEvent.NOBUTTON,
                                x + (toX - x) * i / 10, y + (toY - y) * i / 10);
                    }
                } else {
                    EventQueue.invokeAndWait(() -> { });
                    onClientThread(() -> null);
                    onClientThread(() -> null);
                }
            } finally { WebClientBridge.mouse(MouseEvent.MOUSE_RELEASED, button, toX, toY); }
        }
    }

    private static void press(char character, int code) throws InterruptedException {
        WebClientBridge.key(KeyEvent.KEY_PRESSED, code, character);
        try { Thread.sleep(35); }
        finally { WebClientBridge.key(KeyEvent.KEY_RELEASED, code, character); }
    }

    private static void requireLogin() {
        if (Class31.state != 30 || Cache.aClass39_Sub5_Sub4_Sub4_Sub2_109 == null)
            throw new IllegalStateException("Log in before interacting with the world");
    }

    static String text(JString value) {
        return value == null ? "" : new String(value.bytes, 0, value.length, StandardCharsets.ISO_8859_1);
    }

    private static List<String> strings(JString[] values) {
        List<String> result = new ArrayList<String>();
        if (values != null) for (JString value : values) result.add(text(value));
        return result;
    }

    static String string(Map<String, Object> args, String name, String fallback) {
        return args.containsKey(name) ? (String) args.get(name) : fallback;
    }

    static int integer(Map<String, Object> args, String name, int fallback) {
        return args.containsKey(name) ? ((Number) args.get(name)).intValue() : fallback;
    }

    static int bounded(Map<String, Object> args, String name, int fallback, int min, int max) {
        int value = integer(args, name, fallback);
        if (value < min || value > max) throw new IllegalArgumentException(name + " must be " + min + ".." + max);
        return value;
    }
}
