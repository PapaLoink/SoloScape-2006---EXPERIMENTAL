import com.rs2.ServerSettings;
import com.rs2.cache.js5.Interfaces;
import com.rs2.model.GameplayHelper;
import com.rs2.model.Position;
import com.rs2.model.World;
import com.rs2.model.item.ItemDefinition;
import com.rs2.model.item.ItemStack;
import com.rs2.model.npc.Npc;
import com.rs2.model.npc.NpcDefinition;
import com.rs2.model.npc.NpcMovementMode;
import com.rs2.model.objects.DynamicObject;
import com.rs2.model.objects.LoadedWorldObject;
import com.rs2.model.objects.ObjectDefinition;
import com.rs2.model.objects.ObjectManager;
import com.rs2.model.objects.WorldObjectLookup;
import com.rs2.model.player.Player;
import com.rs2.model.quest.impl.RestlessGhostCoffinGhostSpawnTask;
import com.rs2.model.quest.impl.RestlessGhostQuest;
import com.rs2.util.path.WalkingCollisionMap;
import com.rs2.util.path.ProjectileCollisionMap;

/** Cache-backed issue #3 regression. Run from Server using Java 8. */
public final class RestlessGhostChecks {
    public static void main(String[] args) throws Exception {
        require(ServerSettings.cacheVersion == 443, "Checks require the bundled 443 cache");
        ServerSettings.clientBuild = 443;
        ItemDefinition.loadDefinitions();
        NpcDefinition.loadDefinitions();
        Interfaces.load();
        WorldObjectLookup.loadWorldObjects();
        WalkingCollisionMap.loadCollisionMaps();
        ProjectileCollisionMap.loadCollisionMaps();

        LoadedWorldObject closed = WorldObjectLookup.findObjectByIdAt(2145, 3249, 3192, 0);
        require(closed != null && closed.getType() == 10 && closed.getOrientation() == 0,
                "Cached quest coffin moved");
        int openId = RestlessGhostQuest.getOpenCoffinId();
        ObjectDefinition open = ObjectDefinition.forId(openId);
        require(openId == 15052 && "Coffin".equals(open.getName()),
                "Open coffin must use the concrete headless coffin model");
        require("Close".equals(open.getAction(0)) && "Search".equals(open.getAction(1)),
                "Open coffin menu slots changed");
        require(open.width == 2 && open.length == 2, "Open coffin footprint differs from closed coffin");

        GameplayHelper.loadNpcSpawns();
        Npc father = Npc.findByDefinitionId(458);
        require(father != null && father.getPosition().equals(new Position(3145, 3175, 0)),
                "Father Urhney is missing from his western swamp hut");
        require(father.getSpawnPosition().equals(father.getPosition())
                && father.getMovementMode() == NpcMovementMode.STATIONARY,
                "Father Urhney can leave his hut");
        require((WalkingCollisionMap.getTileFlags(3145, 3175, 0) & 256) == 0,
                "Father Urhney is inside a solid object");
        int fathers = 0;
        for (Npc npc : World.getNpcs()) if (npc != null && npc.getNpcId() == 458) fathers++;
        require(fathers == 1, "Father Urhney was duplicated");

        Player player = new Player(null);
        player.isBot = true;
        player.setPosition(new Position(3248, 3192, 0));
        player.setQuestState(0, 1);
        RestlessGhostQuest quest = new RestlessGhostQuest(12);
        require(player.getQuestManager().handleFirstObjectAction(2145, 3249, 3192),
                "Unstarted quest did not consume coffin interaction");
        require(ObjectManager.findDynamicObjectAt(3249, 3192, 0) == null,
                "Unstarted quest opened the coffin");

        player.setQuestState(12, 2);
        require(quest.handleNpcDialogue(player, 458, 12, 0, 2), "Urhney amulet dialogue failed");
        require(player.getInventoryManager().containsItem(552) && player.getQuestState(12) == 3,
                "Urhney did not give Ghostspeak amulet or advance quest");

        for (int cycle = 0; cycle < 2; cycle++) {
            require(player.getQuestManager().handleFirstObjectAction(2145, 3249, 3192),
                    "Quest manager did not open coffin");
            assertCoffin(openId);
            require(player.getQuestManager().handleSecondObjectAction(openId, 3249, 3192),
                    "Search menu option was not handled");
            assertCoffin(openId);
            new RestlessGhostCoffinGhostSpawnTask(quest, 1, player).execute();
            new RestlessGhostCoffinGhostSpawnTask(quest, 1, player).execute();
            int ghosts = 0;
            for (Npc npc : World.getNpcs()) if (npc != null && npc.getNpcId() == 457) {
                ghosts++;
                require(npc.getMovementMode() == NpcMovementMode.STATIONARY,
                        "Ghost is allowed to roam");
                require(npc.getPosition().equals(new Position(3250, 3195, 0)),
                        "Ghost is not beside coffin");
            }
            require(ghosts == 1, "Repeated opening created duplicate ghosts");
            require(player.getQuestManager().handleFirstObjectAction(openId, 3249, 3192),
                    "Close menu option was not handled");
            assertCoffin(2145);
        }
        require(!quest.handleFirstObjectAction(player, 2145, 3250, 3192, 3),
                "An unrelated coffin was handled");
        player.setPosition(new Position(3248, 3192, 1));
        require(!quest.handleFirstObjectAction(player, 2145, 3249, 3192, 3),
                "Coffin was handled on wrong plane");
        player.setPosition(new Position(3248, 3192, 0));

        // Follow the existing dialogue and skull pickup through the completion handoff.
        require(quest.handleNpcDialogue(player, 457, 17, 0, 3), "Ghost dialogue failed");
        require(player.getQuestState(12) == 4, "Ghost did not request its skull");
        require(!quest.handleGroundItemInteraction(player, 553, 4), "Skull pickup was blocked");
        player.getInventoryManager().addItem(new ItemStack(553));
        require(player.getQuestState(12) == 5, "Skull pickup did not advance quest");
        require(player.getQuestManager().handleFirstObjectAction(2145, 3249, 3192),
                "Could not reopen coffin for skull");
        require(!quest.handleItemOnObject(player, 553, 2145, 5),
                "Skull accepted by closed coffin");
        require(player.getQuestManager().handleItemOnObject(553, openId),
                "Skull was not accepted by corrected open coffin");
        assertCoffin(2145);
        require(player.cutsceneActive, "Release cutscene did not start");

        System.out.println("Restless Ghost checks passed: cache, coffin menus, ghost, Urhney, amulet and skull handoff.");
    }

    private static void assertCoffin(int id) {
        DynamicObject coffin = ObjectManager.findDynamicObjectAt(3249, 3192, 0);
        require(coffin != null && coffin.getWorldObject().getObjectId() == id,
                "Wrong dynamic coffin model: expected " + id);
        require(coffin.getWorldObject().getType() == 10
                && coffin.getWorldObject().getOrientation() == 0,
                "Coffin placement changed");
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
