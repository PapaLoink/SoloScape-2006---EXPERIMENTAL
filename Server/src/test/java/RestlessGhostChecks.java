import com.rs2.model.objects.ObjectDefinition;
import com.rs2.model.objects.WorldObjectLookup;
import com.rs2.model.objects.LoadedWorldObject;
import com.rs2.util.ByteArrayReader;
import java.nio.file.Files;
import java.nio.file.Paths;

public final class RestlessGhostChecks {
    public static void main(String[] args) throws Exception {
        WorldObjectLookup.loadWorldObjects();
        com.rs2.util.path.WalkingCollisionMap.loadCollisionMaps();
        java.util.Map<Integer, byte[]> defs = com.rs2.cache.js5.Definitions.readGroup(6);
        for (int id : new int[]{2145,2146,15052,15053}) System.out.println("RAW " + id + " " + java.util.Arrays.toString(defs.get(id)));
        for (int id = 0; id < ObjectDefinition.definitionsById.length; id++) {
            ObjectDefinition d = ObjectDefinition.forId(id);
            if (d.getName().toLowerCase().contains("coffin") || id == 2146) {
                System.out.println("OBJECT " + id + " " + d.getName() + " "
                    + d.getAction(0) + " " + d.getAction(1) + " " + d.width + "x" + d.length);
            }
        }
        for (int x = 3248; x <= 3251; x++) for (int y = 3191; y <= 3195; y++) {
            LoadedWorldObject o = WorldObjectLookup.findObjectAt(x, y, 0);
            if (o != null) System.out.println("MAP " + x + "," + y + " id="
                + o.getWorldObject().getObjectId() + " type=" + o.getType() + " orientation=" + o.getOrientation());
        }
        ByteArrayReader r = new ByteArrayReader(Files.readAllBytes(Paths.get("data/npcs/Npc spawn.dat")));
        r.readUnsignedByte();
        int count = r.readUnsignedShort();
        for (int i = 0; i < count; i++) {
            int id = r.readUnsignedShort() - count - i;
            int members = r.readUnsignedByte(), movement = r.readUnsignedByte();
            int x = r.readUnsignedShort(), y = r.readUnsignedShort(), z = r.readUnsignedByte();
            if (id >= 456 && id <= 458) System.out.println("SPAWN " + id + " " + x + "," + y + "," + z + " movement=" + movement + " members=" + members);
        }
    }
}
