Run `./tools/strict-content-checks.ps1` from the repository root with a JDK on PATH.
It compiles only the cache reader and the checker, runs its fixtures, and writes
`qa-output/strict-content-checks/report.tsv`. A nonzero exit means invalid IDs,
unresolved coverage, source review candidates, malformed data, or a failed fixture.
It never edits content. Run `./tools/strict-content-checks.ps1 -SelfTest` to run only
the scanner fixtures independently of the current content findings.

The target is the bundled revision 443 snapshot (18 December 2006). The allowlist
is the actual file-ID set in JS5 configuration group 10, rather than a numeric
ceiling, `ItemDefinition.isDefined`, custom metadata, or filtered runtime drops.
This assumes the repository's bundled cache remains the target snapshot; it is
not an independently sourced release-date database.

The data scan covers every static ground spawn, member and free-player NPC
spawns, all native NPC table roots (a conservative superset of static and scripted
NPCs), Treasure Trail tables 6420–6422, source-literal spawn/drop roots, aliases,
guaranteed/weighted/independent entries, every item alternative, all implemented
virtual table branches, and essence-pouch variants. Unknown virtual IDs, missing
tables, alias cycles, truncation, and trailing bytes fail the check. Legacy groups
1–4 are parsed but not audited because `NpcDropTable` does not use them.

The Java syntax-tree scan checks literal `ItemStack` IDs and reward/virtual-pool
arrays before filtering. It skips branches directly gated by
`ServerSettings.content2007Enabled`, and ignores comments. It does not guess
dynamic IDs or table/NPC expressions: each appears as `UNRESOLVED` and fails the
check. Constants, factories, computed IDs, and mutable arrays require explicit
data-flow modelling before they can be certified. There are no baseline ignores
or blanket exceptions for unresolved paths.

This is a conservative audit, not a Java interprocedural reachability proof.
Source constructors include consumption, UI, and disabled implementation code;
their invalid IDs are review candidates, not proof of item acquisition. The source
scan can therefore report God Wars implementations whose callers are gated.
Additional literal script roots are also marked `REVIEW` unless already known
from native/static NPCs or clue rewards; the scan does not assert they are reachable.
Review findings before changing gameplay. A clean data scan alone must not be
reported as complete reward coverage: the command intentionally remains red
until source coverage gaps are resolved.

Built-in fixtures cover cache holes below the ceiling, native heraldic IDs versus
106xx duplicates, encoded spawn offsets, malformed spawn data, table aliases and
cycles, unknown virtual references, both drop formats, multi-item alternatives,
all active drop groups, direct strict-mode gates, comments, and dynamic source
IDs. The previous KBD/Dark beast, ground-spawn, and shield migrations are checked
through the real data on each run.
