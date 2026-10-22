# NPC spawn import

Adds 2,290 overground NPC spawns that the server was missing (from the LostCityRS/Content May 2006 maps, MIT; Jagex owns the game assets).
- `Server/data/npcs/extra_spawns.csv` - columns npc_id,name,x,y,plane,walk. Empty or delete the file to turn them off.
- `ExtraNpcSpawns` loads it at startup (called from `QuestExtras.start()`).
- Quest-gated NPCs (ones that only appear during a quest step) are NOT spawned; 4,074 candidates were held back (quest NPCs, dungeons, special areas) and are listed with reasons in `docs/audit/npc_spawns_held_back.csv` for a later pass.
- Also fixes Plague City: Milli Rehnison, Elena (sewer) and Edmond (sewer) were never spawned in v2 because `spawnWorld()` was not called.
