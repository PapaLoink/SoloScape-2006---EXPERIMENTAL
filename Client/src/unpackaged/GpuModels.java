package unpackaged;

/**
 * Phase 2 of GPU rendering: whole models handed to the GPU instead of being transformed, culled and sorted
 * face by face on the CPU (Model.method488 / method546). A model takes one triangle slot per face at its
 * place in the draw order; GpuGL's compute shader fills those slots in the same order method546 would
 * have drawn them (see Client/dev/gpu-phase2.md). Models since the last batch are kept in three pools that
 * GpuGL sends with the batch: a header per model, the model-space corners, and the face lists.
 * Pure Java on purpose (see Gpu).
 */
final class GpuModels {
    static final int HEADER = 20;
    static final int FACE_SECTIONS = 13;                   // a b c, colour 1-3, type, alpha, priority, texture info, P M N
    static final int MAX_FACES = 4096;                     // the compute shader keeps one int per face in shared memory
    static final int FLAG_ORIENT = 1, FLAG_TYPE = 2, FLAG_ALPHA = 4, FLAG_PRIORITY = 8, FLAG_TEXTURED = 16;

    static boolean enabled = true;                         // GpuGL clears it when the driver can't run the compute shader
    static boolean wanted;                                 // the setting (off: models stay on the CPU, faster on weak GPUs)
    static int[] headers = new int[HEADER * 256], bigHeaders = new int[HEADER * 64];
    static final int SMALL = 512;                          // up to this many faces: the 64-thread compute groups
    static final int TINY = 32;                            // up to this many faces: one GPU thread sorts the whole model
    static int[] tinyHeaders = new int[HEADER * 1024];
    static int tinyModels;
    static int[] verts = new int[3 * 16384];
    static int[] faces = new int[FACE_SECTIONS * 32768];
    static int models, bigModels, vertCount, faceInts;      // small and big models since the last batch

    private GpuModels() { }

    /**
     * Called from method488 once the model is known to be on screen. Returns false to let the CPU draw it
     * (a model that may cross the near plane, one under the mouse, or one the GPU can't take yet).
     */
    static boolean queue(Model m, int orient, int pitchSin, int pitchCos, int yawSin, int yawCos,
                         int tx, int ty, int tz, int centreZ) {
        if (!wanted || !enabled || !Gpu.capturing) return false;
        int n = m.anInt2366, nv = m.anInt2367;
        if (m.anInt2372 >= 1600) return true;              // method546 never draws these
        if (n <= 0 || n > MAX_FACES) return n <= 0;
        int flags = 0;
        int[] texInfo = null;
        if (m.anInt2355 > 0 && m.anIntArray2378 != null) {
            texInfo = textures(m, n);
            if (texInfo == null) return false;             // a texture isn't loaded yet: the CPU falls back to colour
            flags |= FLAG_TEXTURED;
        }
        int base = Gpu.reserve(n);
        if (base < 0) return false;
        boolean big = n > SMALL, tiny = n <= TINY;
        if (tiny && (tinyModels + 1) * HEADER > tinyHeaders.length) tinyHeaders = java.util.Arrays.copyOf(tinyHeaders, tinyHeaders.length * 2);
        if (big && (bigModels + 1) * HEADER > bigHeaders.length) bigHeaders = java.util.Arrays.copyOf(bigHeaders, bigHeaders.length * 2);
        if (!big && !tiny && (models + 1) * HEADER > headers.length) headers = java.util.Arrays.copyOf(headers, headers.length * 2);
        if (vertCount + nv > verts.length / 3) verts = java.util.Arrays.copyOf(verts, Math.max(verts.length * 2, (vertCount + nv) * 3));
        if (faceInts + FACE_SECTIONS * n > faces.length) faces = java.util.Arrays.copyOf(faces, Math.max(faces.length * 2, faceInts + FACE_SECTIONS * n));

        // corners: x block, y block, z block
        int vo = vertCount * 3;
        System.arraycopy(m.anIntArray2376, 0, verts, vo, nv);
        System.arraycopy(m.anIntArray2356, 0, verts, vo + nv, nv);
        System.arraycopy(m.anIntArray2359, 0, verts, vo + 2 * nv, nv);

        // faces: one block of n per section
        int fo = faceInts;
        System.arraycopy(m.anIntArray2385, 0, faces, fo, n);
        System.arraycopy(m.anIntArray2363, 0, faces, fo + n, n);
        System.arraycopy(m.anIntArray2368, 0, faces, fo + 2 * n, n);
        System.arraycopy(m.anIntArray2354, 0, faces, fo + 3 * n, n);
        System.arraycopy(m.anIntArray2369, 0, faces, fo + 4 * n, n);
        System.arraycopy(m.anIntArray2364, 0, faces, fo + 5 * n, n);
        if (m.anIntArray2378 != null) { System.arraycopy(m.anIntArray2378, 0, faces, fo + 6 * n, n); flags |= FLAG_TYPE; }
        if (m.anIntArray2379 != null) { System.arraycopy(m.anIntArray2379, 0, faces, fo + 7 * n, n); flags |= FLAG_ALPHA; }
        if (m.anIntArray2365 != null) { System.arraycopy(m.anIntArray2365, 0, faces, fo + 8 * n, n); flags |= FLAG_PRIORITY; }
        if (texInfo != null) System.arraycopy(texInfo, 0, faces, fo + 9 * n, 4 * n);

        int[] headers = tiny ? tinyHeaders : big ? bigHeaders : GpuModels.headers;
        int h = (tiny ? tinyModels : big ? bigModels : models) * HEADER;
        headers[h] = vertCount;                            // corner offset (in corners)
        headers[h + 1] = nv;
        headers[h + 2] = fo;                               // face offset (in ints)
        headers[h + 3] = n;
        headers[h + 4] = base;                             // first triangle slot
        headers[h + 5] = flags | (orient != 0 ? FLAG_ORIENT : 0);
        headers[h + 6] = orient != 0 ? Model.anIntArray2394[orient] : 0;
        headers[h + 7] = orient != 0 ? Model.anIntArray2418[orient] : 65536;
        headers[h + 8] = pitchSin;
        headers[h + 9] = pitchCos;
        headers[h + 10] = yawSin;
        headers[h + 11] = yawCos;
        headers[h + 12] = tx;
        headers[h + 13] = ty;
        headers[h + 14] = tz;
        headers[h + 15] = centreZ;
        headers[h + 16] = m.anInt2358;
        if (tiny) tinyModels++; else if (big) bigModels++; else models++;
        vertCount += nv;
        faceInts += FACE_SECTIONS * n;
        return true;
    }

    private static int[] texScratch = new int[4 * 1024];

    /**
     * Per face: texture layer | 64-texel flag << 16 | opaque flag << 17, then the P, M, N corner indices.
     * Null when a texture the model uses isn't loaded, so method665 would have drawn it in plain colour.
     */
    private static int[] textures(Model m, int n) {
        if (texScratch.length < 4 * n) texScratch = new int[4 * n];
        int[] out = texScratch;
        Interface2 loader = Class39_Sub5_Sub10_Sub2.anInterface2_2458;
        for (int f = 0; f < n; f++) {
            int type = m.anIntArray2378[f];
            if (type == -1 || (type & 3) < 2) continue;
            int id = m.anIntArray2371[f];
            int info = texture(loader, id);
            if (info < 0) return null;
            int t = type >> 2;
            out[f] = info;
            out[n + f] = m.anIntArray2370[t];
            out[2 * n + f] = m.anIntArray2373[t];
            out[3 * n + f] = m.anIntArray2387[t];
        }
        return out;
    }

    // texture id -> layer | flags, looked up once a frame (faces share a few textures; lookups were 8% of a frame)
    private static int[] texInfo = new int[0], texFrame = new int[0];

    private static int texture(Interface2 loader, int id) {
        if (id < 0) return -1;
        if (id >= texInfo.length) {
            texInfo = java.util.Arrays.copyOf(texInfo, id + 64);
            texFrame = java.util.Arrays.copyOf(texFrame, id + 64);
        }
        if (texFrame[id] == Gpu.frame) return texInfo[id];
        int info = -1;
        int[] texels = loader.method8(id, -9039);
        if (texels != null) {
            boolean small = loader.method10(-11819, id), opaque = loader.method9((byte) -50, id);
            int layer = GpuTextures.layer(id, texels, small);
            if (layer >= 0) info = layer | (small ? 1 << 16 : 0) | (opaque ? 1 << 17 : 0);
        }
        texFrame[id] = Gpu.frame;
        texInfo[id] = info;
        return info;
    }

    static void clear() {
        models = bigModels = tinyModels = vertCount = faceInts = 0;
    }
}
