package unpackaged;

/**
 * Which 2006 texture sits in which layer of the GPU texture array, and which layers need uploading this frame.
 * A texture's texels are 4 brightness copies of S x S (S = 64 or 128) stacked into an S x 4S layer. Animated
 * textures (water, lava) are rewritten by the client every frame, so they are re-sent each frame they are drawn.
 * Pure Java on purpose (see Gpu): GpuGL does the uploads.
 */
final class GpuTextures {
    static int size;                                       // texel width of every layer, 0 until a texture is drawn
    static int layers;                                     // layers handed out
    static int[] layerOf = new int[0];
    static int[][] sent = new int[0][];                    // the texel array last sent for each texture id
    static int[] sentFrame = new int[0];
    static int[] dirtyLayer = new int[64];
    static int[][] dirtyTexels = new int[64][];
    static int dirty;
    static boolean resized;                                // GpuGL must (re)allocate the array

    private GpuTextures() { }

    static int layer(int id, int[] texels, boolean small) {
        int s = small ? 64 : 128;
        if (texels.length < 4 * s * s) return -1;
        if (s != size || id >= layerOf.length) reset(s, id + 1);
        if (id < 0) return -1;
        int l = layerOf[id];
        if (l < 0) l = layerOf[id] = layers++;
        if (sent[id] != texels || animated(id) && sentFrame[id] != Gpu.frame) {
            sent[id] = texels;
            sentFrame[id] = Gpu.frame;
            if (dirty == dirtyLayer.length) {
                dirtyLayer = java.util.Arrays.copyOf(dirtyLayer, dirty * 2);
                dirtyTexels = java.util.Arrays.copyOf(dirtyTexels, dirty * 2);
            }
            dirtyLayer[dirty] = l;
            dirtyTexels[dirty++] = texels;
        }
        return l;
    }

    private static boolean animated(int id) {
        Class39_Sub8[] all = ((Class55) Class39_Sub5_Sub10_Sub2.anInterface2_2458).aClass39_Sub8Array1256;
        return id < all.length && all[id] != null && all[id].anInt1394 != 0;
    }

    /** New texture size or more ids than before: forget every layer, so each texture is sent again. */
    private static void reset(int s, int ids) {
        Class39_Sub8[] all = ((Class55) Class39_Sub5_Sub10_Sub2.anInterface2_2458).aClass39_Sub8Array1256;
        int n = Math.max(ids, all.length);
        size = s;
        layers = 0;
        layerOf = new int[n];
        java.util.Arrays.fill(layerOf, -1);
        sent = new int[n][];
        sentFrame = new int[n];
        dirty = 0;
        resized = true;
    }

    static void forget() {
        size = 0;
        layerOf = new int[0];
        dirty = 0;
    }
}
