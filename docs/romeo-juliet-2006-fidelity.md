# Romeo & Juliet — March–December 2006 fidelity audit

This document tracks the remaining work needed for **verified 1:1 fidelity**.
A successful Java regression test means that server dialogue/state machinery works;
it does **not** mean the original 2006 graphical cutscene was reproduced.

## Reference period

The target is RuneScape after the **7 March 2006** Romeo & Juliet
graphical/scene update and before the 2007 Varrock graphical update.
Modern Old School RuneScape preserves much of the later quest story, but
its modern journal text and world-object layout should not be blindly copied.

Sources:
- https://oldschool.runescape.wiki/w/Romeo_%26_Juliet
- https://oldschool.runescape.wiki/w/Juliet
- https://oldschool.runescape.wiki/w/Phillipa
- https://oldschool.runescape.wiki/w/Varrock_crypt
- https://wiki.darkan.org/Cadava_berry
- https://2007rshelp.com/item/409/cadavaberries

## Repaired by PR #16

- Romeo's incomplete optional question paths no longer display a placeholder.
- Juliet can begin the quest, then proceed with the letter exchange.
- Both main NPCs remain talkable at intermediate quest stages.
- Apothecary explains the berries and remains talkable when the potion is owned.
- Drinking a Cadava potion consumes it and requires obtaining another.
- Potion and crypt portions now have scripted timed story sequences;
  the quest completes after the final crypt story beat.
- Draul Leptoc and Phillipa are instantiated at the mansion in 443 mode,
  using the **verified revision-443 definitions 3324 and 3325**.
  Later OSRS NPC identifiers 5039/5040 are **not valid for the loaded 443
  character models**.
- Both **original 7 March 2006 quest jingles** play in their respective
  story sequences: legacy song IDs 571/572 resolve through the project's
  existing audio map to revision-443 cache groups 120/168. Tests assert
  the exact translation.
- Quest regression tests cover stage entry, dialogue paths, item hand-offs,
  supporting characters and inventory-potion handling.

## Not yet verified as 1:1

1. **Exact NPC spawn tiles and facing directions.** The new mansion spawn tiles
   are provisional; compare against a 2006 snapshot or original map/spawn dump.
2. **Scene cinematography.** The current scene UI, camera targets, timings,
   animation choices, player/NPC staging and dialogue beats are approximations.
   The crypt is cutscene-only in the reference game; its exact tile coordinates
   are undocumented. Do not invent a permanent explorable crypt.
3. **Music playback timing.** The original jingle IDs and cache assets have
   been verified; the original music-to-camera timing has not.
4. **Verbatim 2006 dialogue and its chathead expression IDs.** Existing quest
   lines remain, and missing branches use newly authored stand-ins; they have
   not been checked line-by-line against a dated 2006 transcript.
5. **Quest journal formatting and completion presentation.** The correct
   reward is five Quest Points. Some journal strings and the cutscene-to-reward
   presentation still need graphical verification.
6. **Cadava berry spawn count.** The repository's imported world item-spawn
   data contains a single item 753 spawn at (3260, 3370, 0). Before Varrock's
   later graphical update, Cadava berries appeared as ground items rather than
   harvestable bushes. The original December 2006 count and exact coordinates
   still need an authoritative snapshot before world-data changes.

## Suggested in-game verification

- Start from **Romeo** and separately from **Juliet** on fresh accounts.
- Speak to each NPC several times at every stage; check optional choices,
  decline paths, and ability to recover a lost message.
- Request the Cadava potion, drop or drink it, return for a replacement;
  check that no dialogue bypasses potion consumption.
- Observe the mansion characters on both floors and ensure they are clickable.
- Follow both cinematics; compare camera, animation, music and NPC placement
  with a dated 2006 recording, recording every discrepancy.
- Complete the quest exactly once, check five Quest Points, and revisit all NPCs.
