# Live client developer tools

SoloScape includes its own Java 8 MCP bridge for the revision-443 client. It does
not require RuneLite or the OpenRune plugin JAR. Normal play launchers leave it off.

## Start

1. Start the game server with `Start-Server.bat`.
2. Run `Client/Start-Client-Dev-Mode.bat`. This compiles the client and starts
   it with live HotSwap and `-Dsoloscape.mcp.port=7780`. Saving Java source files
   recompiles them automatically; structural edits automatically restart the client.
   `Start-Client-Developer-Tools.bat` forwards to this same launcher.
3. Log in normally in the client.
4. Open <http://127.0.0.1:7780/> to watch calls, results and screenshots.

The MCP endpoint is <http://127.0.0.1:7780/mcp>. It uses Streamable HTTP with JSON
responses. The service binds only to `127.0.0.1`, validates Host and Origin headers,
and closes with the client. If port 7780 is occupied, close the other developer
tools client or run `Client/Start-Client-Dev-Mode.bat -McpPort 7781` and update
your MCP configuration to use that port.

For Claude Code, register the running endpoint with:

```powershell
claude mcp add --transport http soloscape http://127.0.0.1:7780/mcp
```

Other MCP clients can connect to the same URL using their HTTP server configuration.
There is no project-specific account or API key.

## Available tools

| Area | Tools |
| --- | --- |
| Screen and input | `screenshot`, `click`, `hover`, `drag`, `press_key`, `type_chat`, `enter_input` |
| Client | `get_client_state`, `get_skills`, `get_snapshot` |
| World | `list_npcs`, `list_players`, `list_objects`, `list_ground_items`, `interact_npc`, `interact_object`, `pickup_item`, `walk_to` |
| Inventory | `get_inventory`, `item_action` |
| Interfaces | `list_interfaces`, `dump_interface`, `get_widget`, `widget_action` |
| Menus and dialogue | `get_menu`, `click_menu_option`, `get_dialogue`, `continue_dialogue`, `select_option` |
| Confirmed actions | `act_and_wait` |
| State and history | `get_var`, `get_var_history`, `get_chat_history`, `get_script_history`, `wait_for` |

Screenshots and pointer coordinates use the native **765×503** screen, regardless
of desktop window scaling. Crop screenshots with `x`, `y`, `width` and `height`.
Widget positions are **parent-relative**, not screen click coordinates. Packed
widget IDs are `(interfaceId << 16) | childId`.

`get_inventory` accepts **93** inventory, **94** equipment, and **95** bank.
These map to the container keys sent by SoloScape (0, 25, and 89 respectively).
Other IDs read the raw client container key. Unloaded containers report `loaded: false`. `item_action` uses inventory
widget 149:0 and its cache-defined actions. `widget_action` supports standard
old-format buttons in an open interface; other widgets can be operated using their
current menu or a screen click.

Continue and dialogue options use the client's real widget dispatcher. World
interactions use its menu dispatcher and walking uses its normal route finder.
Returned `invoked`/`routeRequested`/`inputDispatched` values confirm dispatch;
use `wait_for` or inspection to verify the server's response.

`get_var` supports `varp`, `varbit`, `varc_int` and `varc_string`. Variable history
records varp changes observed between client ticks; it does not enumerate every
derived varbit or intermediate value. Chat and clientscript history start when
developer tools are enabled. Each history retains its most recent 500 events.
The activity dashboard retains the last 100 tool calls and supports filtering,
errors-only, pause/resume and follow. `/log?after=<id>` returns incremental entries.

`wait_for` supports `logged_in`, `at_tile`, `dialogue`, `interface_open`,
`interface_closed`, `dialogue_changed`, and `chat_message`, with a maximum timeout of 30 seconds.
Wait conditions run every client tick, without HTTP polling or fixed 100 ms sleeps.
Chat matching ignores case. Interface waits cover the viewport, chatbox and external sidebar roots.
The `dialogue` condition means a chatbox interface is open, which can also include
Tutorial Island instructions; inspect `get_dialogue` before responding.

## Faster agent workflows

Prefer `act_and_wait` for direct gameplay actions. It dispatches the action once,
checks the requested outcome on each client tick, and returns `matched`,
`actionResult`, and a fresh `snapshot` containing state, compact dialogue text and
buttons, and occupied inventory slots. An action timeout does not undo the action;
inspect state before deciding whether to repeat it. Invalid conditions are rejected
before dispatch. Supported actions: NPC/object interaction, pickup, item action,
widget action, menu action, walking, Continue and dialogue option selection.

```json
{"action":"interact_npc","arguments":{"npcName":"Banker"},"wait":{"condition":"dialogue_changed","timeoutMs":5000}}
{"action":"continue_dialogue","arguments":{},"wait":{"condition":"dialogue_changed","timeoutMs":5000}}
{"action":"select_option","arguments":{"option":1},"wait":{"condition":"dialogue_changed","timeoutMs":5000}}
{"action":"walk_to","arguments":{"x":3208,"y":3219},"wait":{"condition":"at_tile","x":3208,"y":3219,"timeoutMs":10000}}
{"action":"interact_object","arguments":{"nameFilter":"Sacks","option":"Search"},"wait":{"condition":"chat_message","textContains":"nothing interesting","timeoutMs":5000}}
```

`dialogue_changed` compares current chatbox text/buttons and open interface roots
against the state before the action. It can confirm a new dialogue, a closed
dialogue, or an opened main interface. With `act_and_wait`, chat waits only match
messages recorded after that action starts; standalone `wait_for` can match recent
history. Use a specific `interface_open` condition when the exact destination matters.

Use `get_snapshot {}` for routine inspection and direct action tools instead of
screen clicks where possible. Crop screenshots to the area needed for visual
verification; text checks avoid PNG encoding and image interpretation.

Clicks now stay pressed until the client has had a tick to process them, replacing
the fixed 200 ms hold. Dragging retains its 200 ms motion. Typing sends batches of
eight characters and waits for client processing between batches, replacing the
60 ms delay per character. The server's 600 ms gameplay tick remains unchanged.

## Examples

```text
get_client_state {}
interact_npc {"npcName":"bob", "option":"Talk-to"}
get_dialogue {}
continue_dialogue {}
select_option {"option":2}
get_inventory {"inventoryId":93}
item_action {"itemName":"Logs", "option":"Drop"}
interact_object {"nameFilter":"tree", "option":"Chop down"}
wait_for {"condition":"chat_message", "textContains":"logs", "timeoutMs":15000}
walk_to {"x":3222, "y":3218}
wait_for {"condition":"at_tile", "x":3222, "y":3218}
get_var {"type":"varp", "id":173}
screenshot {}
```

## Verification

Run `tools/developer-tools-checks.ps1` from the repository root. It compiles the
current client with Java 8 and checks the real HTTP transport, JSON parsing,
protocol errors, notifications, origin validation, snapshots, widget inspection,
history, waits, log retention and dialogue/button packet encoding using synthetic
client state. It does not log into a real game session or prove each gameplay
interaction. After a content change, verify its outcome in a logged-in client.
