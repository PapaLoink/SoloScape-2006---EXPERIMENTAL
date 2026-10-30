# Examine descriptions

`examines2006.tsv` is extracted from the pinned [LostCityRS/Content May 2006
preservation source](https://github.com/LostCityRS/Content/tree/4949e619a53f40c0fe83395df89bd35ff1f044f4).
Its reconstructed configs override its unpacked revision 377 baseline by symbol.
The pack files map symbols to IDs. The MIT notice is in `examines2006.LICENSE.txt`.
These are preservation reconstructions, not a guarantee of exact revision 443 text.

`examines2009scape.tsv` supplements missing descriptions using
[2009scape commit c6322d0976986b176691eee9775b596bda071a9f](https://gitlab.com/2009scape/2009scape/-/tree/c6322d0976986b176691eee9775b596bda071a9f).
NPC descriptions come from `Server/data/configs/npc_configs.json`. Object
descriptions come from `Server/data/configs/object_configs.json`, with names
verified against `dumps/530/config/dump.loc`. Only descriptions whose names match
the local cache are included. Additional variants inherit text only when all
source descriptions for that exact name agree. Its AGPLv3 notice is included in
`examines2009scape.LICENSE.txt`; game content remains subject to Jagex's rights.
Later text is a fallback and is not claimed to be exact 2006 wording.

Resolution preserves cache/server descriptions first, then name-matched May 2006
text, then name-matched supplements. Items retain the existing
`itemExamines2009scape.tsv` fallback and bank-note description. The custom Grand
Exchange clerk has a local description. Remaining gaps use the entity's name;
unnamed definitions use a generic message. No examine returns empty text.

Regenerate from the repository root:

```powershell
python tools/cache-assets/extract-examines.py
./tools/examine-checks.ps1
python tools/cache-assets/extract-examine-supplements.py
./tools/examine-checks.ps1
```

The May 2006 extractor uses the local pinned checkout at
`qa-output/dialogue-reference` (or `--reference`). The supplement extractor
downloads pinned inputs into `qa-output/examine-reference` and reads the coverage
inventory. The committed TSV files require no network at runtime.

`qa-output/examine-checks/coverage.tsv` records every revision 443 item, NPC and
object's resolved text and identifies the remaining named fallbacks. The check
also exercises incoming examine packets and their outgoing game messages over
a local socket, including notes, custom replacements, precedence and invalid IDs.
