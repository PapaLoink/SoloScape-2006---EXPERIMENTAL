# Dialogue data provenance

Reference: [LostCityRS/Content — May 2, 2006](https://github.com/LostCityRS/Content/tree/4949e619a53f40c0fe83395df89bd35ff1f044f4).

Pinned commit: `4949e619a53f40c0fe83395df89bd35ff1f044f4`.

These are independent preservation reconstructions, not recovered Jagex transcripts. Some reference files explicitly rely on later OSRS or RS3 dialogue. No exact-2006 accuracy claim is made.

Imported source code is covered by [the accompanying MIT notice](../../../../Server/data/content/npcDialogues2006.LICENSE.txt). Jagex retains rights in its game content.

The compiler imports complete dialogue-only entries, with two explicit supplements: NPC 278 uses only the completed Cook conversation; NPC 300 uses only the unstarted Rune Mysteries conversation. Eligibility guards live in HistoricalNpcDialogues; existing quest handlers run first.

NPC/player expressions currently use neutral chat-head animations. Text wraps to 48 characters and four lines per page; long statements are split into further pages. This preserves content and branches, not original pixel-for-pixel layout or expression timing.

| NPC ID | Reference script | Evidence note |
| --- | --- | --- |
| 37 | [script](https://github.com/LostCityRS/Content/blob/4949e619a53f40c0fe83395df89bd35ff1f044f4/scripts/areas/area_yanille/scripts/sigbert_the_adventurer.rs2) | Preservation reconstruction; exact historical text unverified. |
| 66 | [script](https://github.com/LostCityRS/Content/blob/4949e619a53f40c0fe83395df89bd35ff1f044f4/scripts/areas/area_gnome/scripts/gnomes.rs2) | Reference mentions later-era transcript evidence; historical text unverified. |
| 67 | [script](https://github.com/LostCityRS/Content/blob/4949e619a53f40c0fe83395df89bd35ff1f044f4/scripts/areas/area_gnome/scripts/gnomes.rs2) | Reference mentions later-era transcript evidence; historical text unverified. |
| 68 | [script](https://github.com/LostCityRS/Content/blob/4949e619a53f40c0fe83395df89bd35ff1f044f4/scripts/areas/area_gnome/scripts/gnomes.rs2) | Reference mentions later-era transcript evidence; historical text unverified. |
| 162 | [script](https://github.com/LostCityRS/Content/blob/4949e619a53f40c0fe83395df89bd35ff1f044f4/scripts/areas/area_gnome/scripts/gnome_trainer.rs2) | Preservation reconstruction; exact historical text unverified. |
| 210 | [script](https://github.com/LostCityRS/Content/blob/4949e619a53f40c0fe83395df89bd35ff1f044f4/scripts/quests/quest_grail/scripts/grail_maiden.rs2) | Preservation reconstruction; exact historical text unverified. |
| 212 | [script](https://github.com/LostCityRS/Content/blob/4949e619a53f40c0fe83395df89bd35ff1f044f4/scripts/quests/quest_grail/scripts/king_percival.rs2) | Preservation reconstruction; exact historical text unverified. |
| 214 | [script](https://github.com/LostCityRS/Content/blob/4949e619a53f40c0fe83395df89bd35ff1f044f4/scripts/quests/quest_grail/scripts/peasent.rs2) | Preservation reconstruction; exact historical text unverified. |
| 215 | [script](https://github.com/LostCityRS/Content/blob/4949e619a53f40c0fe83395df89bd35ff1f044f4/scripts/quests/quest_grail/scripts/peasent.rs2) | Preservation reconstruction; exact historical text unverified. |
| 226 | [script](https://github.com/LostCityRS/Content/blob/4949e619a53f40c0fe83395df89bd35ff1f044f4/scripts/areas/area_seers/scripts/hemenster/sinister_stranger.rs2) | Preservation reconstruction; exact historical text unverified. |
| 228 | [script](https://github.com/LostCityRS/Content/blob/4949e619a53f40c0fe83395df89bd35ff1f044f4/scripts/areas/area_seers/scripts/hemenster/bigdave.rs2) | Preservation reconstruction; exact historical text unverified. |
| 229 | [script](https://github.com/LostCityRS/Content/blob/4949e619a53f40c0fe83395df89bd35ff1f044f4/scripts/areas/area_seers/scripts/hemenster/joshua.rs2) | Preservation reconstruction; exact historical text unverified. |
| 278 | [script](https://github.com/LostCityRS/Content/blob/4949e619a53f40c0fe83395df89bd35ff1f044f4/scripts/areas/area_lumbridge/scripts/cook.rs2) | Preservation reconstruction; exact historical text unverified. |
| 298 | [script](https://github.com/LostCityRS/Content/blob/4949e619a53f40c0fe83395df89bd35ff1f044f4/scripts/areas/area_falador/scripts/goblin_village/goblin_armed_green.rs2) | Preservation reconstruction; exact historical text unverified. |
| 299 | [script](https://github.com/LostCityRS/Content/blob/4949e619a53f40c0fe83395df89bd35ff1f044f4/scripts/areas/area_falador/scripts/goblin_village/goblin_armed_red.rs2) | Preservation reconstruction; exact historical text unverified. |
| 300 | [script](https://github.com/LostCityRS/Content/blob/4949e619a53f40c0fe83395df89bd35ff1f044f4/scripts/areas/area_wizard_tower/scripts/sedridor.rs2) | Reference mentions later-era transcript evidence; historical text unverified. |
| 344 | [script](https://github.com/LostCityRS/Content/blob/4949e619a53f40c0fe83395df89bd35ff1f044f4/scripts/areas/area_combat_training/scripts/combat_training_camp.rs2) | Preservation reconstruction; exact historical text unverified. |
| 345 | [script](https://github.com/LostCityRS/Content/blob/4949e619a53f40c0fe83395df89bd35ff1f044f4/scripts/areas/area_combat_training/scripts/combat_training_camp.rs2) | Preservation reconstruction; exact historical text unverified. |
| 346 | [script](https://github.com/LostCityRS/Content/blob/4949e619a53f40c0fe83395df89bd35ff1f044f4/scripts/areas/area_combat_training/scripts/combat_training_camp.rs2) | Preservation reconstruction; exact historical text unverified. |
| 347 | [script](https://github.com/LostCityRS/Content/blob/4949e619a53f40c0fe83395df89bd35ff1f044f4/scripts/areas/area_ardougne_west/scripts/mourner.rs2) | Reference mentions later-era transcript evidence; historical text unverified. |
| 348 | [script](https://github.com/LostCityRS/Content/blob/4949e619a53f40c0fe83395df89bd35ff1f044f4/scripts/areas/area_ardougne_west/scripts/mourner.rs2) | Reference mentions later-era transcript evidence; historical text unverified. |
| 352 | [script](https://github.com/LostCityRS/Content/blob/4949e619a53f40c0fe83395df89bd35ff1f044f4/scripts/areas/area_ardougne_west/scripts/woman.rs2) | Preservation reconstruction; exact historical text unverified. |
| 354 | [script](https://github.com/LostCityRS/Content/blob/4949e619a53f40c0fe83395df89bd35ff1f044f4/scripts/areas/area_ardougne_west/scripts/woman.rs2) | Preservation reconstruction; exact historical text unverified. |
| 355 | [script](https://github.com/LostCityRS/Content/blob/4949e619a53f40c0fe83395df89bd35ff1f044f4/scripts/areas/area_ardougne_west/scripts/child.rs2) | Preservation reconstruction; exact historical text unverified. |
| 356 | [script](https://github.com/LostCityRS/Content/blob/4949e619a53f40c0fe83395df89bd35ff1f044f4/scripts/areas/area_ardougne_west/scripts/child.rs2) | Preservation reconstruction; exact historical text unverified. |
| 358 | [script](https://github.com/LostCityRS/Content/blob/4949e619a53f40c0fe83395df89bd35ff1f044f4/scripts/areas/area_ardougne_west/scripts/priest.rs2) | Preservation reconstruction; exact historical text unverified. |
| 362 | [script](https://github.com/LostCityRS/Content/blob/4949e619a53f40c0fe83395df89bd35ff1f044f4/scripts/areas/area_ardougne_west/scripts/woman.rs2) | Preservation reconstruction; exact historical text unverified. |
| 368 | [script](https://github.com/LostCityRS/Content/blob/4949e619a53f40c0fe83395df89bd35ff1f044f4/scripts/areas/area_varrock/scripts/east_gate.rs2) | Preservation reconstruction; exact historical text unverified. |
| 460 | [script](https://github.com/LostCityRS/Content/blob/4949e619a53f40c0fe83395df89bd35ff1f044f4/scripts/areas/area_yanille/scripts/wizard_frumscone.rs2) | Preservation reconstruction; exact historical text unverified. |
| 605 | [script](https://github.com/LostCityRS/Content/blob/4949e619a53f40c0fe83395df89bd35ff1f044f4/scripts/areas/area_falador/scripts/sir_vyvin.rs2) | Preservation reconstruction; exact historical text unverified. |
| 607 | [script](https://github.com/LostCityRS/Content/blob/4949e619a53f40c0fe83395df89bd35ff1f044f4/scripts/areas/area_barbarian_outpost/scripts/gunnjorn.rs2) | Preservation reconstruction; exact historical text unverified. |
| 653 | [script](https://github.com/LostCityRS/Content/blob/4949e619a53f40c0fe83395df89bd35ff1f044f4/scripts/areas/area_lostcity/scripts/fairy_queen.rs2) | Preservation reconstruction; exact historical text unverified. |
| 660 | [script](https://github.com/LostCityRS/Content/blob/4949e619a53f40c0fe83395df89bd35ff1f044f4/scripts/minigames/game_partyroom/scripts/partyroom.rs2) | Reference mentions later-era transcript evidence; historical text unverified. |
| 678 | [script](https://github.com/LostCityRS/Content/blob/4949e619a53f40c0fe83395df89bd35ff1f044f4/scripts/minigames/game_ranging/scripts/guard.rs2) | Preservation reconstruction; exact historical text unverified. |
| 679 | [script](https://github.com/LostCityRS/Content/blob/4949e619a53f40c0fe83395df89bd35ff1f044f4/scripts/minigames/game_ranging/scripts/ranging_guild_door.rs2) | Preservation reconstruction; exact historical text unverified. |
| 712 | [script](https://github.com/LostCityRS/Content/blob/4949e619a53f40c0fe83395df89bd35ff1f044f4/scripts/areas/area_ardougne_west/scripts/carla.rs2) | Preservation reconstruction; exact historical text unverified. |
| 789 | [script](https://github.com/LostCityRS/Content/blob/4949e619a53f40c0fe83395df89bd35ff1f044f4/scripts/quests/quest_hero/scripts/npcs/grubor.rs2) | Preservation reconstruction; exact historical text unverified. |
| 799 | [script](https://github.com/LostCityRS/Content/blob/4949e619a53f40c0fe83395df89bd35ff1f044f4/scripts/areas/area_brimhaven/scripts/pirate_guard.rs2) | Preservation reconstruction; exact historical text unverified. |
| 805 | [script](https://github.com/LostCityRS/Content/blob/4949e619a53f40c0fe83395df89bd35ff1f044f4/scripts/skill_crafting/scripts/crafting_guild/crafting_guild.rs2) | Preservation reconstruction; exact historical text unverified. |
| 837 | [script](https://github.com/LostCityRS/Content/blob/4949e619a53f40c0fe83395df89bd35ff1f044f4/scripts/areas/area_alkharid/scripts/shantay_pass.rs2) | Reference mentions later-era transcript evidence; historical text unverified. |
| 847 | [script](https://github.com/LostCityRS/Content/blob/4949e619a53f40c0fe83395df89bd35ff1f044f4/scripts/skill_cooking/scripts/cooking_guild.rs2) | Preservation reconstruction; exact historical text unverified. |
| 852 | [script](https://github.com/LostCityRS/Content/blob/4949e619a53f40c0fe83395df89bd35ff1f044f4/scripts/areas/area_yanille/scripts/ogre_chieftan.rs2) | Preservation reconstruction; exact historical text unverified. |
| 861 | [script](https://github.com/LostCityRS/Content/blob/4949e619a53f40c0fe83395df89bd35ff1f044f4/scripts/quests/quest_itwatchtower/scripts/ogre_guard.rs2) | Reference mentions later-era transcript evidence; historical text unverified. |
| 877 | [script](https://github.com/LostCityRS/Content/blob/4949e619a53f40c0fe83395df89bd35ff1f044f4/scripts/areas/area_yanille/scripts/tower_guard.rs2) | Preservation reconstruction; exact historical text unverified. |
| 957 | [script](https://github.com/LostCityRS/Content/blob/4949e619a53f40c0fe83395df89bd35ff1f044f4/scripts/minigames/game_duelarena/scripts/mubariz.rs2) | Reference mentions later-era transcript evidence; historical text unverified. |
| 963 | [script](https://github.com/LostCityRS/Content/blob/4949e619a53f40c0fe83395df89bd35ff1f044f4/scripts/minigames/game_duelarena/scripts/zahwa.rs2) | Preservation reconstruction; exact historical text unverified. |
| 986 | [script](https://github.com/LostCityRS/Content/blob/4949e619a53f40c0fe83395df89bd35ff1f044f4/scripts/quests/quest_upass/scripts/quest_upass.rs2) | Reference mentions later-era transcript evidence; historical text unverified. |
| 1008 | [script](https://github.com/LostCityRS/Content/blob/4949e619a53f40c0fe83395df89bd35ff1f044f4/scripts/minigames/game_duelarena/scripts/hamid.rs2) | Reference mentions later-era transcript evidence; historical text unverified. |
| 1037 | [script](https://github.com/LostCityRS/Content/blob/4949e619a53f40c0fe83395df89bd35ff1f044f4/scripts/areas/area_canafis/scripts/werewolfroadblocker.rs2) | Preservation reconstruction; exact historical text unverified. |
| 1075 | [script](https://github.com/LostCityRS/Content/blob/4949e619a53f40c0fe83395df89bd35ff1f044f4/scripts/quests/quest_death/scripts/death_archer.rs2) | Preservation reconstruction; exact historical text unverified. |
| 1081 | [script](https://github.com/LostCityRS/Content/blob/4949e619a53f40c0fe83395df89bd35ff1f044f4/scripts/areas/area_burthorpe/scripts/death_servant.rs2) | Preservation reconstruction; exact historical text unverified. |
| 1092 | [script](https://github.com/LostCityRS/Content/blob/4949e619a53f40c0fe83395df89bd35ff1f044f4/scripts/areas/area_burthorpe/scripts/death_white_knight.rs2) | Preservation reconstruction; exact historical text unverified. |
| 1151 | [script](https://github.com/LostCityRS/Content/blob/4949e619a53f40c0fe83395df89bd35ff1f044f4/scripts/quests/quest_troll/scripts/eadgar_troll_chief_cook.rs2) | Preservation reconstruction; exact historical text unverified. |
