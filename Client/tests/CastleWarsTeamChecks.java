package unpackaged;

import jagex.world.actors.Player;

/** Castle Wars teams must be recognised even when the cache has no team-cape ID. */
public final class CastleWarsTeamChecks {
    public static void main(String[] args) {
        require(Player.castleWarsTeamForCloak(512 + 4514) == 1,
                "Saradomin Castle Wars cloak did not identify its team");
        require(Player.castleWarsTeamForCloak(512 + 4516) == 2,
                "Zamorak Castle Wars cloak did not identify its team");
        require(Player.castleWarsTeamForCloak(0) == 0,
                "Unequipped cloak must not have a team");
        require(Player.castleWarsTeamForCloak(512 + 4515) == 0,
                "A team hood is not a cape");

        Player self = new Player();
        Player ally = new Player();
        Player opponent = new Player();
        self.castleWarsTeam = 1;
        ally.castleWarsTeam = 1;
        opponent.castleWarsTeam = 2;
        require(self.isCastleWarsTeammate(ally), "Own team not detected");
        require(!self.isCastleWarsTeammate(opponent), "Opponents treated as teammates");
        require(!self.isCastleWarsTeammate(null), "Null player treated as teammate");
        self.castleWarsTeam = 0;
        require(!self.isCastleWarsTeammate(ally), "Team detection leaked outside Castle Wars");
        System.out.println("Castle Wars cloak/team checks passed");
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
