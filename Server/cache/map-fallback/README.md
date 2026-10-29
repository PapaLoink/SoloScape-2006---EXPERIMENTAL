# Location map fallbacks

These files contain decoded location streams, loaded by both the client and
server when the bundled encrypted map cannot be decoded. Terrain and object
definitions still come from the bundled revision 443 cache.

## Taverley dungeon entrance

`l45_53.dat` restores the missing surface ladder (`1759`, type 10, rotation 0)
at `2884,3397,0`. The hole decoration and all other placements are retained.
The native underground map already contains the exit ladder (`1755`) at
`2884,9797,0`; the existing server handlers provide travel in both directions.
Restart the server and client to reload the shared map fallback.

`Client/tests/TaverleyDungeonChecks.java` verifies both cached ladder placements,
their revision-443 models and click options, and a round trip through the server
object interaction handler. Compile it with both client/server classes and
`Server/lib/*`, then run from `Server`.

## Recovered Varrock maps

The previous `l49_53.dat`, `l49_54.dat`, `l50_54.dat`, and `l51_54.dat` streams
were incomplete. West Varrock had only 44 wall/corner placements, and the west
bank booth tiles contained object 11402 (an interface button) instead of 2213.

The replacements use archive 5, file 0 of the corresponding named groups in
[OpenRS2 cache 2079](https://archive.openrs2.org/caches/runescape/2079), revision
457. These are compatible historical replacements, not decrypted originals
from revision 443. They restore the bank, castle, and neighbouring buildings
while retaining revision 443 terrain, definitions, and models.

To reproduce, decode each named location group using that cache's `keys.json`.
Keep only object groups with a definition in the bundled archive 2/group 6 and
whose referenced models exist in bundled archive 7. Re-encode the object ID
deltas relative to the last retained ID; retain position deltas and type/rotation
bytes within each retained group. Finish each group and the stream with zero.
Do not filter individual placements without recalculating position deltas.

| Map | Source placements | Removed unsupported placements | Restored wall/corner placements |
| --- | ---: | ---: | ---: |
| 49_53 | 4101 | 0 | 1201 |
| 49_54 | 2587 | 6 | 363 |
| 50_54 | 5050 | 5 | 1893 |
| 51_54 | 2827 | 53 | 753 |

`Client/tests/VarrockMapChecks.java` checks the shared loading path, the west
bank's wall and booth coordinates, and all referenced definitions and models.
Run from `Server` after compiling it with both client and server classes plus
`Server/lib/*` on the classpath. Restart the server and reload the client to
replace maps already held in memory.

## May 2006 restoration (2026-10-25)

Every one of the 88 fallback squares was re-keyed after revision 443. Neither OpenRS2
nor osrs.world has a key from before revision 445 for any of them, so the revision 443
originals cannot be decrypted. Most fallbacks were the earliest decryptable version
(revisions 445-457, January-May 2007) with every object the bundled cache cannot draw
removed. That left holes wherever Jagex remodelled objects in early 2007. One example is
Ardougne Zoo, whose railings became new objects 21239-21242.

Revision 377 (2 May 2006, `rs2-377_2006-05-02` on osrs.world) stores its maps unencrypted.
It contains 85 of the 88 squares (all except 29_79, 30_79 and 33_82), and every object in it
is drawable with the bundled cache. It was used in two ways:

- **Replaced with the May 2006 square (9 squares).** 40_50, 40_51 and 41_51 (East
  Ardougne and the zoo, reworked in February 2007), plus 42_69, 42_70, 43_69, 43_70, 43_71
  and 53_150. Those six had kept under 5% of their objects.
- **Restored from May 2006 (32 squares).** Each keeps all of its previous placements. On
  tiles where the 2007 data had an undrawable placement, the May 2006 placements in the
  same slot (wall, wall decoration, scenery or floor) were added, if that slot was empty.

Ardougne Zoo needed one more step. Its object stream stayed unencrypted until revision 419
(9 July 2006), identical to 377. From 421 onward it was encrypted and changed in 421, 423,
427, 433, 439 and 441, and none of those versions has a known key. The terrain changed
only once, on 4 September 2006 (427). From then on it matches revision 443. That
re-shaped the snake enclosure and walkway (2610-2617, 3266-3285; the scorpion pit's
east fence at 2605-2609 keeps its May 2006 pieces, because its ground did not change), and the February 2007
terrain is the same there, so in that box 40_51 uses the February 2007 objects on all
planes. That includes the plane-1 bridge railings (`814`) over the terrain's plane-1 deck at
2612-2613, 3271-3280, which is already in the revision 443 terrain. Drawable objects are
kept as they are. Railings `21239`/`21240` become `997` for types 0 and 9; the 37 other
pieces are dropped, mostly railing corners.
The rest of the square is the May 2006 stream.

The 40_50 terrain changed once in 2006, in revision 441 (29 November). That revision
added a pen floor at 2605-2608, 3259-3262, south of the zoo path. Its February 2007 objects
were swapped in for 2602-2611, 3256-3263: the pen walls `19995` and `19852` inside it are
both in the revision 443 cache. The railing ring around the pen (`21240`) appears only in
the February 2007 data, not in May 2006, so it was left out rather than converted to `997`.
This is the ferret pen. In `extra_spawns.csv` it gets Ferret 5081 inside and Charlie 5138
on its north side (2607, 3264), at the positions the 2009 spawn data uses. Both NPCs are in
revision 443. The May 2006 railing `997` along y 3264 between Charlie and the pen
(x 2604-2609) was removed from 40_51. The railing now runs from the post at 2612 to x 2610
and ends short of the pen. The quest-start map icon object `2739` (map function 6) was added
at 2606, 3264, as in the February 2007 data, for Charlie and Eagles' Peak (released
28 November 2006).

41_51 needed the same treatment along its north edge. In the revision 443 terrain, 2654-2677,
3325-3327 is raised to a building floor (height 50). The building's north part is in the native
revision 443 square 41_52 (walls `1602`, roofs `1792`/`5778`/`5779` from y 3328). Its south wall,
doors, fence and roof edge are only in the 2007 data. In that strip, on all planes, 41_51 uses the
2007 objects (78 placements, all drawable) in place of the 13 May 2006 ones.

`world-map-repairs.tsv` lists every changed square with before/after counts and hashes
(`may2006-replacement` and `may2006-restoration`). The other 47 squares lost nothing
or had nothing in May 2006 to restore.

Some squares differ a lot between May 2006 and the 2007 data, and there is no way to
tell which changes came before December 2006. These kept their 2007 layout and were
only restored: 52_153, 40_153, 53_48, 44_55, 39_55 and 42_58.

## Terrain overrides

A file named `m<x>_<y>.dat` in this folder replaces the bundled terrain for that square.
Both the server (`MapGroups.readTerrain`) and the client (`Class62_Sub2`) load it, so tile
heights, floors and flags stay in step. Use it only to reconcile objects with terrain they
were not built for.

`m40_51.dat` is the revision 443 terrain with three tiles taken whole from revision 419
(July 2006): 2608,3272, 2609,3271 and 2610,3270. Only their heights differ. The September
2006 bank had raised those corners to 39, which hid part of the May 2006 scorpion-pit
railing at 2607,3271 to 2609,3269. Restart the server and reload the client after changing
an override.

The pit keeps its May 2006 fence exactly, including rotations; the terrain override above
is what keeps the railing visible from the path. Copying the February 2007 rotations
(turned 180 degrees) was tried and reverted. Those rotations belong to the remodelled
`21239` railing. On the `997` diagonal model, which is drawn from one side only, they made
the railing face away from the path. The May 2006 data also had two railings on the same
edge at the pit's east corner (2609,3268 east edge and 2610,3268 west edge). They drew over
each other, so the 2610,3268 piece was removed.

## Whole-world audit (2026-10-01)

The audit traversed all 792 terrain/location squares in the bundled reference
table, including 704 native revision-443 maps and 88 fallback maps. The latest
OpenRS2 key list for cache 2700 still contains the same 545 keys as the bundled
list, so no additional encrypted originals could be recovered with new keys.
All native location maps passed the definition/model/placement-type checks.

Six more incomplete building maps were recovered from cache 2079, using the
same compatibility filtering described above. These replacements also exclude
placements whose type cannot be rendered by their revision-443 definition.
They retain the bundled terrain; they are historical approximations where the
original encrypted revision-443 location stream remains unavailable.

| Map | Walls/corners before | Walls/corners after |
| --- | ---: | ---: |
| 42_54 | 215 | 1131 |
| 45_54 | 668 | 761 |
| 48_54 | 644 | 690 |
| 51_52 | 17 | 507 |
| 53_54 | 323 | 329 |
| 54_54 | 22 | 655 |

Six other maps keep all their original object IDs, coordinates, rotations, and
models, with unambiguous placement-type corrections to match the existing
revision-443 model shapes:

| Map | Object | Correction | Placements |
| --- | --- | --- | ---: |
| 38_69 | 15506 (rug) | 10 to 22 | 13 |
| 42_58 | 14170 (pile of eggs) | 22 to 10 | 1 |
| 44_54 | 15520 (old bookshelf) | 22 to 10 | 3 |
| 46_54 | 15523 (bookcase) | 22 to 10 | 1 |
| 48_51 | 15506 (rug) | 10 to 22 | 318 |
| 48_61 | 733 (web) | 0 to 10 | 2 |

The repair manifest in `world-map-repairs.tsv` records before/after counts,
excluded unsupported placements, corrections, and SHA-256 hashes for these
twelve repairs. `world-map-audit.tsv` contains one validation row per square.
The two fallback regions 29_79 and 30_79 have no decoded comparison source in
the inspected historical caches; they pass the local asset/shape checks and
were retained. An automated cache audit cannot prove that every historical
placement is present or replace in-game visual inspection.

Run the reproducible whole-world check from the repository root in PowerShell:

```powershell
New-Item -ItemType Directory -Force Client/build/map-checks | Out-Null
javac -cp 'Client/build/classes;Server/build/classes;Server/lib/*' -d Client/build/map-checks Client/tests/WorldMapChecks.java Client/tests/VarrockMapChecks.java
Push-Location Server
java -Xmx1024m -cp '../Client/build/map-checks;../Client/build/classes;build/classes;lib/*' WorldMapChecks cache/map-fallback/world-map-audit.tsv
java -cp '../Client/build/map-checks;../Client/build/classes;build/classes;lib/*' VarrockMapChecks
Pop-Location
```

The whole-world check validates complete terrain and location streams, every
placement's model shape, definition/model references including object morphs,
actual model decoding and triangle indices, and minimum building counts for
the recovered squares. The separate Varrock check verifies its repaired bank.
