import com.rs2.model.gameplay.castlewars.CastleWarsEngineeringManager;
import com.rs2.model.item.ItemStack;
import com.rs2.model.player.Player;

/** Castle Wars deaths must purge temporary engineering supplies. */
public final class CastleWarsDeathSupplyChecks {
    public static void main(String[] args) {
        Player player = new Player(null);
        int[] minigameItems = {
                CastleWarsEngineeringManager.BRONZE_PICKAXE_ID,
                CastleWarsEngineeringManager.TINDERBOX_ITEM_ID,
                CastleWarsEngineeringManager.EXPLOSIVE_POTION_ID,
                CastleWarsEngineeringManager.ROCK_ITEM_ID,
                CastleWarsEngineeringManager.CLIMBING_ROPE_ITEM_ID,
                CastleWarsEngineeringManager.BARRICADE_ITEM_ID,
                CastleWarsEngineeringManager.TOOLKIT_ID,
                CastleWarsEngineeringManager.EMPTY_BUCKET_ITEM_ID,
                CastleWarsEngineeringManager.BUCKET_OF_WATER_ITEM_ID
        };
        for (int itemId : minigameItems) {
            require(player.getInventoryManager().addItemPartial(new ItemStack(itemId, 1)) == 1,
                    "Could not stage Castle Wars supply " + itemId);
        }
        CastleWarsEngineeringManager.cleanupPlayerSupplies(player);
        for (int itemId : minigameItems) {
            require(player.getInventoryManager().getItemAmount(itemId) == 0,
                    "Temporary supply was retained after cleanup: " + itemId);
        }
        System.out.println("Castle Wars engineering supply cleanup checks passed");
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
