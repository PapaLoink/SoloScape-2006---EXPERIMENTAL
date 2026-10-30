# SoloScape 18-Dec-2006 Fix Order

Target: **RuneScape revision 443 — 18 December 2006**

Source audit: `AUDIT_2006-12-18.md` — 67 numbered findings.

This order is dependency-driven. Do **not** start by adding missing quests/minigames/skills while the historical-content boundary, save schema, NPC population and ID semantics are still wrong. Otherwise new 2006 content will be built on top of mixed 2007/custom data and will need rework.

## Phase A — Lock down strict 18-Dec-2006 mode (P0)

### 1. Remove ordinary-player progression cheats
- Fix finding **28** first.
- Privilege-gate/remove `::master`, `::setlevel`, `::money`, `::bank`, `::god`, `::quests`, teleport/debug cheats and equivalent normal-player paths.
- Remove the skill-menu path that reaches the unrestricted `setlevel` handler.
- Add tests proving a normal account cannot invoke admin/debug progression commands.

### 2. Create one authoritative historical-content policy
- Fix finding **38**.
- Separate `isCacheDefined443(id)` / target availability from `hasServerMetadata(id)`.
- Normal gameplay must use the 18-Dec-2006 policy for items, NPCs, drops, rewards, spawns, shops, bots and item creation.
- Stop using `ItemDefinition.isDefined()` as a historical-date check.

**Gate A1:** no normal-player path may create/use an item or NPC merely because later custom metadata defines it.
### 3. Disable/remove post-target systems from strict mode
- Findings **3, 4, 5, 12, 24**.
- Grand Exchange: unavailable.
- 2008 bank tabs/search: unavailable; restore period banking behaviour.
- Ironman/UIM/HCIM account rules: unavailable.
- Custom Necromancy and boss-pet systems: unavailable in strict mode.
- Gate all explicitly 2007+ combat/content modules behind non-strict profiles.
- Remove/resolve the Construction-item ID collisions used by pets/Necromancy.

### 4. Purge active post-target economy leaks
- Findings **31, 35, 51**.
- Remove Draconic visage `11286` from KBD.
- Remove Dark bow `11235` from Dark beasts.
- Remove the three out-of-cache static ground-item spawns.
- Replace Treasure Trail `106xx` heraldic shield rewards with native 443 IDs.
- Add an automated scan: every reachable strict-mode reward/drop/spawn must be target-valid.

### 5. Make the historical date explicit
- Findings **6, 36**.
- Stop deriving seasonal content from the host computer calendar.
- Strict profile should behave as **18-Dec-2006** regardless of real-world date.
- Implement the Gublinch/Shanty Claws Christmas event and rewards `10507/10508`.
- Disable random Christmas cracker/Santa-hat scattering.

### 6. Restore target death behaviour
- Finding **29**.
- PvM death drops should use the pre-16-Oct-2007 public behaviour rather than the later private-recovery window.
- Fix recoverable/untradeable handling to match the target rules.

**Gate A2:** only after steps 1–6 should economy/progression testing be considered meaningful.
## Phase B — Migrate server data/schema from the May-2006 horizon to revision 443 (P0/P1)

### 7. Replace the revision-377-era NPC population
- Finding **30**.
- Replace `Npc spawn.dat` with a historically sourced revision-443 / 18-Dec-2006 population.
- Remove the `NPC ID > 3851` rejection only after the replacement data is correct.
- Verify known Jun–Dec 2006 NPCs appear at the correct coordinates.
- Re-check shops/dialogues/activities that are currently orphaned only because their NPC cannot spawn.

### 8. Replace/extend the quest definition dataset
- Findings **9, 34**.
- Current quest data stops at Swan Song / 2-May-2006.
- Extend through **Animal Magnetism / 12-Dec-2006**.
- Restore correct quest order, QP totals, requirements and menu state.
- Target Quest cape requirement should resolve from the complete target dataset, not the current 78-QP truncated total.

### 9. Migrate the skill core to 23 real skills
- Findings **7, 8, 37, 40, 45**.
- Add a real Hunter slot instead of synthesising Hunter `1/0` on the wire.
- Keep Construction and Hunter at the correct revision-443 client indices.
- Version/migrate local character files.
- Fix SQLite load/save/upsert coverage.
- Update hiscores, total level/XP, restore arrays and every hardcoded `21/22` skill loop.
- Fix Construction/Hunter skillcape requirements and trim counting.

**Gate B1:** a fresh account and an upgraded existing save must persist all 23 target skills correctly through logout/login.
### 10. Clean build-443 ID semantic collisions
- Findings **24, 45**.
- Remove/reassign custom uses of genuine Construction-era item IDs.
- Remove obsolete skillcape-bundle ranges `7992–8065` from strict-mode semantics.
- Ensure dropping Construction flatpacks cannot summon pets or invoke Necromancy.
- Add ID-collision tests for target-cache items with custom handlers.

### 11. Update the cache validator
- Finding **2**.
- Change stale pre-Christmas expectations to the actual 18-Dec-2006 cache/config counts.
- Keep a validator that proves the bundled cache remains the intended revision-443 snapshot.

**Gate B2:** target cache definitions, server metadata, player-save schema, quest rows and NPC population must all agree on the same historical profile before large content implementation begins.

## Phase C — Correct core mechanics before adding more content (P1)

### 12. Fix player combat defence rolls
- Finding **41**.
- Correct player Magic defence to include the period Magic/Defence contribution.
- Re-test melee/ranged/magic accuracy with representative low/mid/maxed stats.

### 13. Fix weapon timing/classification
- Finding **42** plus the Dorgeshuun continuation finding.
- Restore 2006 maces to the 5-tick base speed rather than the 2021 4-tick value.
- Give Dorgeshuun crossbow its correct period profile when its acquisition content is restored.
- Add profile tests keyed by item ID rather than relying only on broad name matching.
### 14. Fix live 2006 special attacks as one combat workstream
- Findings **56–65, 67**.
- Magic composite bow: add 35% Powershot.
- Dragon battleaxe: restore drain-based Rampage formula.
- Abyssal whip: replace pre-Jun-2005 zero/max-hit special with target Energy Drain behaviour.
- Magic longbow/Seercull: consume one arrow and restore target hit semantics.
- Dragon spear: correct shove eligibility and stun duration.
- Dragon 2h: restore the historical target cap.
- Magic shortbow: correct Snapshot accuracy modifier.
- Rune thrownaxe: charge special energy per chained target as required by the target behaviour.
- Darklight: correct drain amount and NPC/demon support.
- Dragon halberd: restore multi-combat Sweep behaviour.
- Dragon axe: correct Clobber drain magnitude and NPC support.

### 15. Fix Prayer/combat-effect defects
- Findings **54, 66**.
- Redemption must actually apply its `floor(base Prayer / 4)` heal before Prayer is set to zero.
- Protect from Magic must halve Bind/Snare/Entangle duration in the target ruleset.
- Preserve already-correct protection-prayer damage reduction, Smite, Retribution and prayer-drain behaviour.

### 16. Fix target-era consumable/timing defects
- Findings **43, 52**.
- Correct Pineapple pizza, Wild pie and Summer pie healing.
- Restore Wild/Summer pie stat/run effects.
- Correct potato Farming to the target 10-minute global cycle cadence.

### 17. Fix save-bit corruption before expanding unlock systems
- Finding **44**.
- Separate Tai Bwo Wannai Cleanup/favour storage from emote-unlock bits.
- Add migration logic for existing saves if the reserved long has already been used.
- Add tests proving favour changes cannot unlock/erase emotes and vice versa.

**Gate C:** core combat, prayer, death, persistence and consumable regression tests should pass before quest/minigame acceptance testing.
## Phase D — Restore missing 18-Dec-2006 gameplay content (P1)

### 18. Implement Hunter and Construction
- Findings **7, 8, 37, 40**.
- Only start after Phase B skill/save migration and ID cleanup.
- Implement training loops, XP, tool/item interactions, world objects/NPCs and unlocks from target data.
- Construction must use genuine flatpack/POH/item semantics without colliding with custom systems.
- Hunter must be a real persisted skill, not a packet-only shim.

### 19. Implement Lunar Diplomacy and the Lunar spellbook
- Finding **32**.
- Restore the target-date Lunar spell set and Lunar Home Teleport.
- Add the real spellbook switch/access requirements.
- Keep post-18-Dec-2006 Lunar additions excluded.
- Do not retain custom Necromancy as a substitute in strict mode.

### 20. Restore the Jun–Dec 2006 world-system cluster
- Findings **33, 49, 50**.
- Warriors' Guild.
- Stronghold of Security, rewards and related unlocks.
- Trouble Brewing.
- Fairy Tale II / Fairy Rings.
- Pyramid Plunder.
- September 2006 Lumbridge/skill tutors.
- Reconnect already-present cache/map/shop assets once the correct NPC population exists.

### 21. Implement the missing target-date quests
- Findings **9, 34**.
- Treat each quest as gameplay, not only a menu row.
- Restore NPCs, dialogues, objects, cutscenes, items, requirements, rewards and persistent state.
- Work chronologically from 3-May-2006 through Animal Magnetism so dependencies are easier to verify.
### 22. Complete target-era activities/minigames
- Findings **10, 33, 39, 55**.
- Keep already-substantial Castle Wars, Barrows, Fight Caves, Mage Training Arena and Duel Arena under regression tests.
- Implement/fix missing target activities such as Pest Control, Fight Pits, Blast Furnace, Rogues' Den and Temple Trekking where applicable to the target date.
- Implement Giant Mole combat/burrowing, mole claw/skin drops and the Wyson exchange.
- Finish partial activities such as Tai Bwo Wannai Cleanup rather than replacing working pieces unnecessarily.

### 23. Restore the 2006 random-event roster
- Finding **47**.
- Wire Sandwich Lady into an actual spawn/trigger path.
- Restore target-era randoms that are currently absent/unreachable.
- Connect legitimate random-event completion to the repaired emote-unlock storage.
- Keep random events date-correct; do not import later versions/rewards blindly.

### 24. Rebuild Slayer target coverage
- Finding **48**.
- Rebuild master task pools against an 18-Dec-2006 roster.
- Add target-valid Mogre, Fever spider, Zygomite and Cave horror rules/access where missing.
- Remove/hard-gate the stale NPC-3887 "Edgeville master" path.
- Test master, Slayer-level, quest prerequisite and monster-equipment requirements.

### 25. Finish the July-2006 crossbow update
- Finding **53**.
- Implement stock + limb + crossbow-string assembly.
- Add sapphire/emerald/ruby/diamond/dragonstone/onyx bolt-tip production.
- Add target-era Enchant Crossbow Bolt spells/effects.
- Keep later ammunition/effects out of strict mode.

### 26. Finish 5-Dec-2006 Treasure Trails
- Finding **51**.
- Implement emote clues, equipment/location/emote validation and Uri progression.
- Keep Third Age/elegant/composite/god reward expansion already present.
- Validate every rollable clue reward against the native 443 cache.
### 27. Finish Capes of Accomplishment
- Finding **45**.
- Restore historically correct cape acquisition from skill masters/Wise Old Man paths.
- Fix Hunter/Construction requirements after the 23-skill migration.
- Fix trim counting so all target skills are included.
- Quest cape must require the complete target quest set / **232 QP** rather than the truncated dataset total.

## Phase E — Interaction completeness and historical acceptance (P2)

### 28. Work through the interaction-audit backlog by system
Do not attack the raw counts randomly. Clear them alongside the system being restored.

Current verified backlog from the completed audit:
- **955** object actions with `missing-option-handler`.
- **61** NPCs with `missing-shop`.
- **55** NPCs with `missing-pickpocket-definition`.
- **7,990** object interactions marked `review-legacy`.
- **3,566** widgets marked `review-native-or-unmapped`.
- **1,610** inventory-item interactions marked `review-legacy`.
- **636** NPC interactions marked `review-legacy`.

For every restored area, clear its relevant backlog entries before calling that area complete.

### 29. Preserve systems that already passed the historical audit
Do not rewrite these without a regression proving a need:
- revision-443 cache/map loading;
- ordinary two-stage player trade flow;
- protection-prayer PvM/PvP reduction model;
- run-energy drain/recovery;
- special-energy regeneration;
- poison cadence and antipoison immunity durations;
- music loading;
- canoes;
- substantial existing Castle Wars/Barrows/Fight Caves/MTA/Duel Arena code.
### 30. Add golden historical acceptance tests
For each system, test normal-player reachability end-to-end:
- exact NPC/object/item IDs and coordinates;
- prerequisites and quest states;
- XP/rewards/drop tables;
- attack speeds, special costs/effects and timers;
- interfaces, configs/varps and spellbook state;
- death/drop behaviour;
- save/relogin persistence;
- strict-mode rejection of post-18-Dec-2006 content.

### 31. Final release gate
Before calling the server "18-Dec-2006 accurate":
1. Build server successfully.
2. Build client successfully.
3. Run `tools/interaction-audit.ps1 -Check` successfully.
4. Run all new strict-date/content-boundary tests.
5. Run save migration tests for old and new characters.
6. Run automated scans proving no reachable normal drop/shop/spawn/reward exceeds the target content policy.
7. Run representative live-client playthroughs for Tutorial Island, combat/death, banking/trading, every skill, quest dependencies, key transports, random events, clue tiers and major minigames.
8. Re-audit only newly changed/local systems; another whole-repo discovery pass should not be necessary unless a new systemic root cause appears.

## Condensed execution order

**Do these in this exact broad order:**

`cheat lockdown -> historical content policy -> remove later/custom systems -> economy leak purge -> fixed historical date/Christmas -> death rules -> 443 NPC population -> quest dataset -> 23-skill/save migration -> ID collision cleanup -> cache validator -> core combat/prayer/save bugs -> Hunter/Construction -> Lunar -> Jun-Dec world systems -> missing quests -> activities/randoms/Slayer -> crossbows/Treasure Trails/skillcapes -> interaction backlog -> golden verification`

The most important rule is: **finish Phases A and B before large content implementation.** They are the foundation that prevents later/custom data from contaminating every system added afterward.
