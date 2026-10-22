# Credits

## Original project
- **SoloScape-2006** — the revision 443 server, client, tools and cache setup this fork is built on:
  https://github.com/SoloScape/SoloScape-2006 (git author of the base commit: Gradwahl). All of the underlying game
  code, engine and tooling belong to its authors. This fork changes only the files listed in [CHANGES.md](CHANGES.md).

## Quest content and dialogue
- **LostCityRS / 2004scape** — https://github.com/LostCityRS/Content and https://github.com/2004Scape/Server.
  The Clock Tower and Sheep Herder quests were ported from the 274 LostCity scripts, and Plague City's dialogue and
  step order follow the `quest_elena` scripts. Quest design and writing originate from Jagex's RuneScape.

## Third-party notices carried over from upstream
- **RS Mod pathfinder** (Copyright (c) 2020 RS Mod, ISC-style permission notice) — see the "Third-party notices"
  section of `Server/README.md` in the original project.

## NPC spawn data
- `Server/data/npcs/extra_spawns.csv` is derived from the May 2006 map data in **LostCityRS/Content** (MIT, see the licensing note below). Jagex owns the underlying game assets.

## This fork
- Client tool bar, XP drops, ID lists, settings, the four quest implementations and the server changes listed in
  CHANGES.md were written in collaboration with Claude (Anthropic) for the fork's maintainer, PapaLoink.

## Trademarks
RuneScape is a trademark of Jagex Ltd. This is a fan project for personal, non-commercial use.

## Licensing note
- **LostCityRS/Content** is MIT licensed (Copyright (c) 2023-2026 Lost City). Its README states that game assets in that repository remain
  the property of Jagex Ltd and are not covered by the MIT license. Code or scripts adapted from it keep that copyright and permission notice;
  the full MIT text is in the LostCityRS/Content repository's LICENSE file.
- The **original SoloScape-2006 repository** did not include a LICENSE file when this fork was prepared. Please check its current terms
  before redistributing, and add a LICENSE file to this fork only once you have confirmed what you are allowed to license.
