# JSON character saves and automatic .dat migration

The default file-based character persistence format is now **JSON**. Existing
characters are converted automatically the next time the server starts, and
again on login if a character was not migrated at startup.

## Where are saves stored?

Run the server from the `Server` directory, as the usual launcher does.

- Active player: `Server/data/characters/<username>.json`
- Original binary after migration: `Server/data/characters/<username>.dat.bak`
- Previous JSON version after another save: `Server/data/characters/<username>.json.bak`
- Temporary conversion files: `Server/data/characters/.json-staging/` (removed after saving)
- Server snapshot backups: `Server/data/backups/<timestamp>/characters/*.json`
- Dead hardcore character archives: `Server/data/dead_hcim_characters/*.json`

The old binary `.dat` reader is retained for recovery and migration. New file
saves are `.json`. The existing optional SQLite character backend is unchanged
when `ServerSettings.sqlitePlayerSaveEnabled` is true.

## Migration (automatic)

1. **Back up `Server/data/characters/` before the first upgrade.**
2. Pull the branch, rebuild the server, and stop the old server.
3. Start the upgraded server normally.
4. After item and quest definitions load, it scans existing `.dat` files,
   decodes them with the original binary reader, and writes editable JSON.
5. A successfully converted `.dat` becomes `.dat.bak`. A corrupt or unreadable
   `.dat` is left untouched and logged. Migration can safely run again.
6. All future saves use the JSON file as the authoritative copy.

A player is also migrated automatically when logging in if their legacy
`.dat` was missed by the startup scan. If both a JSON and a legacy binary
save are present, **JSON takes precedence**; there is no accidental rollback to
outdated character progress.

## Editing your character

1. Log out **and stop the server** before making manual changes. Live players
   will be saved on shutdown, potentially overwriting external edits.
2. Open `Server/data/characters/<username>.json` using VS Code or Notepad.
3. Edit a named field inside the `character` object, for example
   `playerRights`, `x`, `y`, `musicVolume`, `skillExperience`, or
   `inventoryItems`. The field names are the Java `CharacterFileRecord` names.
4. Keep the `format`, `version`, `username` and field types intact.
   Fixed-length arrays must retain their length; inventory and equipment
   entries can be `null`, or an object with `id`, `amount`, `metadata`.
   Bank tabs are arrays of those inventory-style entries.
5. Save the JSON file and restart the server.

Examples of editable values:

```json
{
  "playerRights": 0,
  "x": 3222,
  "y": 3218,
  "plane": 0,
  "inventoryItems": [
    {"id": 995, "amount": 1000, "metadata": -1},
    null
  ]
}
```

This is a **field excerpt**, not a complete save file; don't replace your
whole character file with it. The full file also contains quest progress,
farming, banking, contacts, music and other gameplay state.

## Safety

- The save files contain private account data, **including the existing
  authentication password in its current legacy representation**. Do not
  publish JSON saves, add them to Git, or make the saves downloadable by a
  public web server. Replacing legacy password storage with salted password
  hashes is a separate authentication migration.
- Only the server owner should have file-write access. Other players must
  never be allowed to upload edited saves or modify their own rights/items.
- Malformed JSON fails validation; the server does not silently reset it to
  an empty character. Use `.json.bak`, a timestamped backup, or the preserved
  original `.dat.bak` to recover if necessary.
- File saves are committed using a temporary file and atomic rename when
  supported by the filesystem.
- Changing item IDs, positions, or quest progress to invalid combinations
  can affect gameplay. Make backups before editing.

## Developer checks

The plain Java 8 JSON codec is `com.rs2.util.CharacterJsonCodec`. Run the
regression test from the `Server` working directory after compiling the server:

```bat
javac -cp "dist/server.jar;lib/*" -d "build-json-checks" "src/test/java/CharacterJsonSaveChecks.java"
java -cp "build-json-checks;dist/server.jar;lib/*" CharacterJsonSaveChecks
```

This tests JSON round trips, item metadata, bank tabs, XP, manual field edits,
username validation, invalid data and previous-save backups.
