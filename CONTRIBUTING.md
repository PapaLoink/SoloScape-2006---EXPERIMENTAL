# Contributing

Thanks for helping! This is a single-player, offline project, so contributions are mostly game content and polish.

## Good first contributions
- Add a quest that the client lists but the server lacks (the 12 "not added yet" quests are a good start).
- Finish testing Plague City in the real client (plague-house doors, Bravek's door, cell door, staircases, journal text).
- Fix dialogue or step differences from the original quests.

## How to work on it
1. Fork this repository and clone it.
2. Make a branch for your change.
3. Build and play-test (`Start-Here-SoloScape.bat`, option 3 for server only, 2 for client only).
4. Open a pull request that says what you changed and how you tested it in game.

## Guidelines
- Quests live in `Server/src/main/java/com/rs2/model/quest/impl/`. Register new ones in `QuestDefinition.java`; use `QuestDlg` for dialogue.
- Say where dialogue comes from (for example a 2004scape script) in your pull request and in `CREDITS.md`.
- Keep changes to upstream files small, and list them in `CHANGES.md`.
- Test the change by playing it, not just by compiling.
