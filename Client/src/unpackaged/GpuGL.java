package unpackaged;

import jagex.graphics.DrawingArea;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GLCapabilities;

import static org.lwjgl.opengl.GL43C.*;

/**
 * The OpenGL side of Gpu: an off-screen OpenGL 4.3 context (hidden GLFW window) that draws the recorded
 * triangles into a framebuffer the size of the 3D view. The picture then either stays on the card for
 * GpuPresent (resizable layout) or is copied back into DrawingArea.buffer. The shaders repeat the
 * rasterizer's maths: HSL palette lookups for gouraud faces, the camera-space U/V/W planes and the 4
 * brightness copies for textures, and the 2006 alpha rule (alpha = share of what's behind).
 *
 * Triangles live in one storage buffer, a record of Gpu.TRI_INTS ints each, and the vertex shader reads its
 * own triangle by gl_VertexID. Every triangle gets a depth from its place in the 2006 draw order (later =
 * nearer), so each batch is drawn twice: solid faces nearest first, so the GPU skips pixels already covered,
 * then see-through faces (alpha, or textures with holes) in the original order, tested against that depth.
 */
final class GpuGL {
    // record layout: [0] mode (0 gouraud, 1 flat RGB, 2 textured, -1 empty), [1] RGB, [2] alpha share (256 =
    // solid), [3] texture layer, [4..15] texture planes U0 Ux Uy V0 Vx Vy W0 Wx Wy, size, opaque, W shift,
    // [16..27] x, y, shade (and a spare) for each corner
    private static final String VERTEX =
        "#version 430 core\n"
        + "layout(std430, binding = 0) readonly buffer Tris { int d[]; };\n"
        + "uniform vec2 uSize;\n"
        + "uniform int uFirst;\n"
        + "uniform int uCount;\n"
        + "uniform int uPass;\n"
        + "noperspective out float vShade;\n"
        + "flat out ivec4 vT0;\n"
        + "flat out ivec4 vT1;\n"
        + "flat out ivec4 vT2;\n"
        + "flat out ivec4 vT3;\n"
        + "void main() {\n"
        + "  int local = gl_VertexID / 3;\n"
        + "  int corner = gl_VertexID - local * 3;\n"
        // pass 0 walks the batch backwards (nearest first), pass 1 forwards
        + "  int tri = uFirst + (uPass == 0 ? uCount - 1 - local : local);\n"
        + "  int o = tri * " + Gpu.TRI_INTS + ";\n"
        + "  vT0 = ivec4(d[o], d[o + 1], d[o + 2], d[o + 3]);\n"
        + "  vT1 = ivec4(0); vT2 = ivec4(0); vT3 = ivec4(0); vShade = 0.0;\n"
        + "  bool solid = vT0.z == 256 && (vT0.x != 2 || d[o + 14] != 0);\n"
        + "  if (vT0.x < 0 || solid != (uPass == 0)) { gl_Position = vec4(2.0, 2.0, 2.0, 1.0); return; }\n"
        + "  if (vT0.x == 2) {\n"
        + "    vT1 = ivec4(d[o + 4], d[o + 5], d[o + 6], d[o + 7]);\n"
        + "    vT2 = ivec4(d[o + 8], d[o + 9], d[o + 10], d[o + 11]);\n"
        + "    vT3 = ivec4(d[o + 12], d[o + 13], d[o + 14], d[o + 15]);\n"
        + "  }\n"
        + "  int c = o + 16 + corner * 4;\n"
        + "  vShade = float(d[c + 2]);\n"
        // depth from the place in the whole frame's draw order: later = nearer (room for 4M triangles)
        + "  float z = 1.0 - 2.0 * (float(tri) + 1.0) / 4194306.0;\n"
        // +0.5: the CPU samples a row at its integer y, OpenGL at the pixel centre
        + "  gl_Position = vec4((float(d[c]) + 0.5) / uSize.x * 2.0 - 1.0, (float(d[c + 1]) + 0.5) / uSize.y * 2.0 - 1.0, z, 1.0);\n"
        + "}\n";

    /** Compiled twice: without CUTOUT (no discard, so the GPU can reject hidden pixels early) and with it. */
    private static final String FRAGMENT =
        "noperspective in float vShade;\n"
        + "flat in ivec4 vT0;\n"
        + "flat in ivec4 vT1;\n"
        + "flat in ivec4 vT2;\n"
        + "flat in ivec4 vT3;\n"
        + "uniform usampler2D uPalette;\n"
        + "uniform usampler2DArray uTex;\n"
        + "uniform ivec2 uCenter;\n"
        + "uniform int uBgr;\n"
        + "out vec4 fragColor;\n"
        + "void main() {\n"
        + "  uint c;\n"
        + "  if (vT0.x == 0) {\n"
        + "    int h = clamp(int(vShade), 0, 65535);\n"
        + "    c = texelFetch(uPalette, ivec2(h & 127, h >> 7), 0).r;\n"
        + "  } else if (vT0.x == 1) {\n"
        + "    c = uint(vT0.y);\n"
        + "  } else {\n"
        // 32-bit integer maths on purpose: the 2006 U/V/W planes overflow and rely on wrapping like Java ints
        + "    int x = int(gl_FragCoord.x) - uCenter.x;\n"
        + "    int y = int(gl_FragCoord.y) - uCenter.y;\n"
        + "    int U = vT1.x + vT1.y * x + vT1.z * y;\n"
        + "    int V = vT1.w + vT2.x * x + vT2.y * y;\n"
        + "    int W = (vT2.z + vT2.w * x + vT3.x * y) >> vT3.w;\n"
        + "    int S = vT3.y;\n"
        + "    int u = 0, v = 0;\n"
        + "    if (W != 0) { u = U / W; v = V / W; }\n"
        + "    u = clamp(u, 0, S * (S - 1));\n"
        + "    int shade = max(int(vShade), 0);\n"
        + "    int idx = (v & ((S - 1) * S)) + u / S + ((shade >> 4) & 3) * S * S;\n"
        + "    c = texelFetch(uTex, ivec3(idx % S, idx / S, vT0.w), 0).r >> uint(shade >> 6);\n"
        + "#ifdef CUTOUT\n"
        + "    if (c == 0u && vT3.z == 0) discard;\n"
        + "#endif\n"
        + "  }\n"
        // blue in the red channel: read back as plain RGBA bytes (the fast path), they land as Java's 0x00RRGGBB
        + "  vec3 rgb = vec3(float((c >> 16) & 255u), float((c >> 8) & 255u), float(c & 255u)) / 255.0;\n"
        + "  fragColor = vec4(uBgr != 0 ? rgb.bgr : rgb, float(vT0.z) / 256.0);\n"
        + "}\n";

    /**
     * One work group per model (GpuModels): transform the corners with method488's integer maths, cull and
     * bucket the faces as method546 does, rank them in its draw order (see Client/dev/gpu-phase2.md) and
     * write each face's triangle record into the model's reserved slots. Hidden faces leave empty records.
     */
    private static final String COMPUTE =                  // compiled with GROUP = 64 (small models) and 256
        "layout(local_size_x = GROUP) in;\n"
        + "layout(std430, binding = 0) buffer Tris { int d[]; };\n"
        + "layout(std430, binding = 1) readonly buffer Models { int m[]; };\n"
        + "layout(std430, binding = 2) readonly buffer Verts { int vin[]; };\n"
        + "layout(std430, binding = 3) readonly buffer Faces { int fin[]; };\n"
        + "layout(std430, binding = 4) buffer Cam { int vc[]; };\n"
        + "uniform int uZoom;\n"
        + "uniform ivec2 uCenter;\n"
        // one sort key per face: priority << 23 | (2047 - depth bucket) << 12 | face, hidden faces sort last
        + "shared int sk[MAXF];\n"                         // per variant: a big array here limits how many groups run at once
        + "shared int sums[12];\n"
        + "shared int cnts[12];\n"
        + "shared int cuts[3];\n"
        + "const int T = " + Gpu.TRI_INTS + ";\n"
        + "const int HIDDEN = 0x7fffffff;\n"
        + "void main() {\n"
        + "  int h = int(gl_WorkGroupID.x) * " + GpuModels.HEADER + ";\n"
        + "  int lid = int(gl_LocalInvocationID.x);\n"
        + "  int v0 = m[h], nv = m[h + 1], fo = m[h + 2], n = m[h + 3], base = m[h + 4], fl = m[h + 5];\n"
        + "  int centre = m[h + 15], bucketOff = m[h + 16];\n"
        + "  bool hasType = (fl & 2) != 0, hasAlpha = (fl & 4) != 0, prio = (fl & 8) != 0;\n"
        // corners: optional model turn, move next to the camera, yaw, pitch, project (method488)
        + "  for (int v = lid; v < nv; v += GROUP) {\n"
        + "    int x = vin[v0 * 3 + v], y = vin[v0 * 3 + nv + v], z = vin[v0 * 3 + 2 * nv + v];\n"
        + "    if ((fl & 1) != 0) { int t = z * m[h + 6] + x * m[h + 7] >> 16; z = z * m[h + 7] - x * m[h + 6] >> 16; x = t; }\n"
        + "    x += m[h + 12]; y += m[h + 13]; z += m[h + 14];\n"
        + "    int t = z * m[h + 10] + x * m[h + 11] >> 16; z = z * m[h + 11] - x * m[h + 10] >> 16; x = t;\n"
        + "    t = y * m[h + 9] - z * m[h + 8] >> 16; z = y * m[h + 8] + z * m[h + 9] >> 16; y = t;\n"
        + "    int o = (v0 + v) * 5;\n"
        + "    vc[o] = x; vc[o + 1] = y; vc[o + 2] = z;\n"
        + "    vc[o + 3] = uCenter.x + x * uZoom / z; vc[o + 4] = uCenter.y + y * uZoom / z;\n"
        + "  }\n"
        + "  if (lid < 12) { sums[lid] = 0; cnts[lid] = 0; }\n"
        + "  if (lid < 3) cuts[lid] = 1 << 30;\n"
        + "  int size = 1;\n"
        + "  while (size < n) size <<= 1;\n"
        + "  memoryBarrierBuffer(); barrier();\n"
        // faces: hidden type, back faces, depth bucket (method546)
        + "  for (int f = lid; f < size; f += GROUP) {\n"
        + "    int key = HIDDEN;\n"
        + "    if (f < n && (!hasType || fin[fo + 6 * n + f] != -1)) {\n"
        + "      int a = (v0 + fin[fo + f]) * 5, b = (v0 + fin[fo + n + f]) * 5, c = (v0 + fin[fo + 2 * n + f]) * 5;\n"
        + "      if ((vc[a + 3] - vc[b + 3]) * (vc[c + 4] - vc[b + 4]) - (vc[a + 4] - vc[b + 4]) * (vc[c + 3] - vc[b + 3]) > 0) {\n"
        + "        int k = (vc[a + 2] - centre + vc[b + 2] - centre + vc[c + 2] - centre) / 3 + bucketOff;\n"
        + "        if (k >= 0 && k < 2048) {\n"
        + "          int p = prio ? fin[fo + 8 * n + f] : 0;\n"
        + "          key = p << 23 | (2047 - k) << 12 | f;\n"
        + "          atomicAdd(cnts[p], 1);\n"
        + "          atomicAdd(sums[p], k);\n"
        + "        }\n"
        + "      }\n"
        + "    }\n"
        + "    sk[f] = key;\n"
        + "  }\n"
        + "  barrier();\n"
        // bitonic sort: priority first, then deepest bucket, then face order, as in method546's lists
        + "  for (int k = 2; k <= size; k <<= 1) {\n"
        + "    for (int j = k >> 1; j > 0; j >>= 1) {\n"
        + "      for (int i = lid; i < size; i += GROUP) {\n"
        + "        int ij = i ^ j;\n"
        + "        if (ij > i) {\n"
        + "          int a = sk[i], b = sk[ij];\n"
        + "          if ((a > b) == ((i & k) == 0)) { sk[i] = b; sk[ij] = a; }\n"
        + "        }\n"
        + "      }\n"
        + "      barrier();\n"
        + "    }\n"
        + "  }\n"
        + "  int visible = 0, first[12];\n"
        + "  for (int p = 0; p < 12; p++) { first[p] = visible; visible += cnts[p]; }\n"
        // priorities 10 and 11 (list S, sorted right after group 9) slot in before groups 0, 3 and 5
        // while deeper than the average bucket of groups 1-2, 3-4 and 6-8
        + "  int c12 = cnts[1] + cnts[2], c34 = cnts[3] + cnts[4], c68 = cnts[6] + cnts[8];\n"
        + "  int t0 = c12 > 0 ? (sums[1] + sums[2]) / c12 : 0;\n"
        + "  int t3 = c34 > 0 ? (sums[3] + sums[4]) / c34 : 0;\n"
        + "  int t5 = c68 > 0 ? (sums[6] + sums[8]) / c68 : 0;\n"
        + "  int sLen = cnts[10] + cnts[11], s0 = first[10];\n"
        + "  int k0 = sLen, k1 = sLen, k2 = sLen;\n"
        + "  if (sLen > 0) {\n"
        // each cut: the first S item (from the previous cut on) that is not deeper than that threshold
        + "    for (int s = lid; s < sLen; s += GROUP) if (2047 - (sk[s0 + s] >> 12 & 2047) <= t0) atomicMin(cuts[0], s);\n"
        + "    barrier();\n"
        + "    k0 = min(cuts[0], sLen);\n"
        + "    for (int s = k0 + lid; s < sLen; s += GROUP) if (2047 - (sk[s0 + s] >> 12 & 2047) <= t3) atomicMin(cuts[1], s);\n"
        + "    barrier();\n"
        + "    k1 = min(cuts[1], sLen);\n"
        + "    for (int s = k1 + lid; s < sLen; s += GROUP) if (2047 - (sk[s0 + s] >> 12 & 2047) <= t5) atomicMin(cuts[2], s);\n"
        + "    barrier();\n"
        + "    k2 = min(cuts[2], sLen);\n"
        + "  }\n"
        // where each group lands: S[0,k0) G0 G1 G2 S[k0,k1) G3 G4 S[k1,k2) G5..G9 S[k2,end)
        + "  int start[10];\n"
        + "  int at = k0;\n"
        + "  for (int p = 0; p < 10; p++) {\n"
        + "    if (p == 3) at += k1 - k0;\n"
        + "    if (p == 5) at += k2 - k1;\n"
        + "    start[p] = at; at += cnts[p];\n"
        + "  }\n"
        + "  int sB = start[3] - (k1 - k0), sC = start[5] - (k2 - k1), sD = at;\n"
        // write the records
        + "  for (int i = lid; i < visible; i += GROUP) {\n"
        + "    int key = sk[i], f = key & 4095, p = key >> 23, r;\n"
        + "    if (p >= 10) {\n"
        + "      int s = i - s0;\n"
        + "      r = s < k0 ? s : s < k1 ? sB + s - k0 : s < k2 ? sC + s - k1 : sD + s - k2;\n"
        + "    } else r = start[p] + i - first[p];\n"
        + "    int o = (base + r) * T;\n"
        + "    int type = hasType ? fin[fo + 6 * n + f] & 3 : 0;\n"
        + "    int a = v0 + fin[fo + f], b = v0 + fin[fo + n + f], c = v0 + fin[fo + 2 * n + f];\n"
        + "    int c0 = fin[fo + 3 * n + f], c1 = fin[fo + 4 * n + f], c2 = fin[fo + 5 * n + f];\n"
        + "    if (type == 1 || type == 3) { c1 = c0; c2 = c0; }\n"
        + "    d[o] = type >= 2 ? 2 : 0;\n"
        + "    d[o + 1] = 0;\n"
        + "    d[o + 2] = 256 - (hasAlpha ? fin[fo + 7 * n + f] : 0);\n"
        + "    if (type >= 2) {\n"
        + "      int info = fin[fo + 9 * n + f];\n"
        + "      int P = (v0 + fin[fo + 10 * n + f]) * 5, M = (v0 + fin[fo + 11 * n + f]) * 5, N = (v0 + fin[fo + 12 * n + f]) * 5;\n"
        + "      int px = vc[P], py = vc[P + 1], pz = vc[P + 2];\n"
        + "      int mx = px - vc[M], my = py - vc[M + 1], mz = pz - vc[M + 2];\n"
        + "      int nx = vc[N] - px, ny = vc[N + 1] - py, nz = vc[N + 2] - pz;\n"
        + "      bool small = (info & 65536) != 0;\n"
        + "      d[o + 3] = info & 65535;\n"
        + "      d[o + 4] = nx * py - ny * px << 14; d[o + 5] = (ny * pz - nz * py << 8) >> 3; d[o + 6] = nz * px - nx * pz << 5;\n"
        + "      d[o + 7] = mx * py - my * px << 14; d[o + 8] = (my * pz - mz * py << 8) >> 3; d[o + 9] = mz * px - mx * pz << 5;\n"
        + "      d[o + 10] = my * nx - mx * ny << 14; d[o + 11] = (mz * ny - my * nz << 8) >> 3; d[o + 12] = mx * nz - mz * nx << 5;\n"
        + "      d[o + 13] = small ? 64 : 128; d[o + 14] = (info & 131072) != 0 ? 1 : 0; d[o + 15] = small ? 12 : 14;\n"
        + "    }\n"
        + "    a *= 5; b *= 5; c *= 5;\n"
        + "    d[o + 16] = vc[a + 3]; d[o + 17] = vc[a + 4]; d[o + 18] = c0;\n"
        + "    d[o + 20] = vc[b + 3]; d[o + 21] = vc[b + 4]; d[o + 22] = c1;\n"
        + "    d[o + 24] = vc[c + 3]; d[o + 25] = vc[c + 4]; d[o + 26] = c2;\n"
        + "  }\n"
        + "  for (int r = visible + lid; r < n; r += GROUP) d[(base + r) * T] = -1;\n"   // hidden faces: empty slots
        + "}\n";

    /**
     * Models of up to GpuModels.TINY faces (most scenery): one thread does a whole model, in method546's own
     * sequential way, so a group of 64 threads handles 64 models instead of one.
     */
    private static final String TINY =
        "#version 430 core\n"
        + "layout(local_size_x = 64) in;\n"
        + "layout(std430, binding = 0) buffer Tris { int d[]; };\n"
        + "layout(std430, binding = 1) readonly buffer Models { int m[]; };\n"
        + "layout(std430, binding = 2) readonly buffer Verts { int vin[]; };\n"
        + "layout(std430, binding = 3) readonly buffer Faces { int fin[]; };\n"
        + "layout(std430, binding = 4) buffer Cam { int vc[]; };\n"
        + "uniform int uZoom;\n"
        + "uniform ivec2 uCenter;\n"
        + "uniform int uCount;\n"
        + "const int T = " + Gpu.TRI_INTS + ";\n"
        + "void main() {\n"
        + "  int mi = int(gl_GlobalInvocationID.x);\n"
        + "  if (mi >= uCount) return;\n"
        + "  int h = mi * " + GpuModels.HEADER + ";\n"
        + "  int v0 = m[h], nv = m[h + 1], fo = m[h + 2], n = m[h + 3], base = m[h + 4], fl = m[h + 5];\n"
        + "  int centre = m[h + 15], bucketOff = m[h + 16];\n"
        + "  bool hasType = (fl & 2) != 0, hasAlpha = (fl & 4) != 0, prio = (fl & 8) != 0;\n"
        + "  for (int v = 0; v < nv; v++) {\n"
        + "    int x = vin[v0 * 3 + v], y = vin[v0 * 3 + nv + v], z = vin[v0 * 3 + 2 * nv + v];\n"
        + "    if ((fl & 1) != 0) { int t = z * m[h + 6] + x * m[h + 7] >> 16; z = z * m[h + 7] - x * m[h + 6] >> 16; x = t; }\n"
        + "    x += m[h + 12]; y += m[h + 13]; z += m[h + 14];\n"
        + "    int t = z * m[h + 10] + x * m[h + 11] >> 16; z = z * m[h + 11] - x * m[h + 10] >> 16; x = t;\n"
        + "    t = y * m[h + 9] - z * m[h + 8] >> 16; z = y * m[h + 8] + z * m[h + 9] >> 16; y = t;\n"
        + "    int o = (v0 + v) * 5;\n"
        + "    vc[o] = x; vc[o + 1] = y; vc[o + 2] = z;\n"
        + "    vc[o + 3] = uCenter.x + x * uZoom / z; vc[o + 4] = uCenter.y + y * uZoom / z;\n"
        + "  }\n"
        + "  int key[" + GpuModels.TINY + "];\n"
        + "  int cnts[12], sums[12];\n"
        + "  for (int p = 0; p < 12; p++) { cnts[p] = 0; sums[p] = 0; }\n"
        + "  int visible = 0;\n"
        + "  for (int f = 0; f < n; f++) {\n"
        + "    if (hasType && fin[fo + 6 * n + f] == -1) continue;\n"
        + "    int a = (v0 + fin[fo + f]) * 5, b = (v0 + fin[fo + n + f]) * 5, c = (v0 + fin[fo + 2 * n + f]) * 5;\n"
        + "    if ((vc[a + 3] - vc[b + 3]) * (vc[c + 4] - vc[b + 4]) - (vc[a + 4] - vc[b + 4]) * (vc[c + 3] - vc[b + 3]) <= 0) continue;\n"
        + "    int k = (vc[a + 2] - centre + vc[b + 2] - centre + vc[c + 2] - centre) / 3 + bucketOff;\n"
        + "    if (k < 0 || k >= 2048) continue;\n"
        + "    int p = prio ? fin[fo + 8 * n + f] : 0;\n"
        + "    int kv = p << 23 | (2047 - k) << 12 | f;\n"
        // insertion sort as we go: priority, then deepest bucket, then face order
        + "    int i = visible++;\n"
        + "    while (i > 0 && key[i - 1] > kv) { key[i] = key[i - 1]; i--; }\n"
        + "    key[i] = kv;\n"
        + "    cnts[p]++; sums[p] += k;\n"
        + "  }\n"
        + "  int first[12];\n"
        + "  int at = 0;\n"
        + "  for (int p = 0; p < 12; p++) { first[p] = at; at += cnts[p]; }\n"
        + "  int c12 = cnts[1] + cnts[2], c34 = cnts[3] + cnts[4], c68 = cnts[6] + cnts[8];\n"
        + "  int t0 = c12 > 0 ? (sums[1] + sums[2]) / c12 : 0;\n"
        + "  int t3 = c34 > 0 ? (sums[3] + sums[4]) / c34 : 0;\n"
        + "  int t5 = c68 > 0 ? (sums[6] + sums[8]) / c68 : 0;\n"
        + "  int sLen = cnts[10] + cnts[11], s0 = first[10];\n"
        + "  int k0 = 0;\n"
        + "  while (k0 < sLen && 2047 - (key[s0 + k0] >> 12 & 2047) > t0) k0++;\n"
        + "  int k1 = k0;\n"
        + "  while (k1 < sLen && 2047 - (key[s0 + k1] >> 12 & 2047) > t3) k1++;\n"
        + "  int k2 = k1;\n"
        + "  while (k2 < sLen && 2047 - (key[s0 + k2] >> 12 & 2047) > t5) k2++;\n"
        + "  int start[10];\n"
        + "  at = k0;\n"
        + "  for (int p = 0; p < 10; p++) {\n"
        + "    if (p == 3) at += k1 - k0;\n"
        + "    if (p == 5) at += k2 - k1;\n"
        + "    start[p] = at; at += cnts[p];\n"
        + "  }\n"
        + "  int sB = start[3] - (k1 - k0), sC = start[5] - (k2 - k1), sD = at;\n"
        + "  for (int i = 0; i < visible; i++) {\n"
        + "    int kv = key[i], f = kv & 4095, p = kv >> 23, r;\n"
        + "    if (p >= 10) {\n"
        + "      int s = i - s0;\n"
        + "      r = s < k0 ? s : s < k1 ? sB + s - k0 : s < k2 ? sC + s - k1 : sD + s - k2;\n"
        + "    } else r = start[p] + i - first[p];\n"
        + "    int o = (base + r) * T;\n"
        + "    int type = hasType ? fin[fo + 6 * n + f] & 3 : 0;\n"
        + "    int a = v0 + fin[fo + f], b = v0 + fin[fo + n + f], c = v0 + fin[fo + 2 * n + f];\n"
        + "    int c0 = fin[fo + 3 * n + f], c1 = fin[fo + 4 * n + f], c2 = fin[fo + 5 * n + f];\n"
        + "    if (type == 1 || type == 3) { c1 = c0; c2 = c0; }\n"
        + "    d[o] = type >= 2 ? 2 : 0;\n"
        + "    d[o + 1] = 0;\n"
        + "    d[o + 2] = 256 - (hasAlpha ? fin[fo + 7 * n + f] : 0);\n"
        + "    if (type >= 2) {\n"
        + "      int info = fin[fo + 9 * n + f];\n"
        + "      int P = (v0 + fin[fo + 10 * n + f]) * 5, M = (v0 + fin[fo + 11 * n + f]) * 5, N = (v0 + fin[fo + 12 * n + f]) * 5;\n"
        + "      int px = vc[P], py = vc[P + 1], pz = vc[P + 2];\n"
        + "      int mx = px - vc[M], my = py - vc[M + 1], mz = pz - vc[M + 2];\n"
        + "      int nx = vc[N] - px, ny = vc[N + 1] - py, nz = vc[N + 2] - pz;\n"
        + "      bool small = (info & 65536) != 0;\n"
        + "      d[o + 3] = info & 65535;\n"
        + "      d[o + 4] = nx * py - ny * px << 14; d[o + 5] = (ny * pz - nz * py << 8) >> 3; d[o + 6] = nz * px - nx * pz << 5;\n"
        + "      d[o + 7] = mx * py - my * px << 14; d[o + 8] = (my * pz - mz * py << 8) >> 3; d[o + 9] = mz * px - mx * pz << 5;\n"
        + "      d[o + 10] = my * nx - mx * ny << 14; d[o + 11] = (mz * ny - my * nz << 8) >> 3; d[o + 12] = mx * nz - mz * nx << 5;\n"
        + "      d[o + 13] = small ? 64 : 128; d[o + 14] = (info & 131072) != 0 ? 1 : 0; d[o + 15] = small ? 12 : 14;\n"
        + "    }\n"
        + "    a *= 5; b *= 5; c *= 5;\n"
        + "    d[o + 16] = vc[a + 3]; d[o + 17] = vc[a + 4]; d[o + 18] = c0;\n"
        + "    d[o + 20] = vc[b + 3]; d[o + 21] = vc[b + 4]; d[o + 22] = c1;\n"
        + "    d[o + 24] = vc[c + 3]; d[o + 25] = vc[c + 4]; d[o + 26] = c2;\n"
        + "  }\n"
        + "  for (int r = visible; r < n; r++) d[(base + r) * T] = -1;\n"
        + "}\n";

    /** Puts each packed CPU-recorded triangle (GpuGL.flush) into its slot of the draw order. */
    private static final String SCATTER =
        "#version 430 core\n"
        + "layout(local_size_x = 64) in;\n"
        + "layout(std430, binding = 0) buffer Tris { int d[]; };\n"
        + "layout(std430, binding = 5) readonly buffer Stage { int s[]; };\n"
        + "layout(std430, binding = 6) readonly buffer Dest { int t[]; };\n"
        + "uniform int uCount;\n"
        + "void main() {\n"
        + "  int i = int(gl_GlobalInvocationID.x);\n"
        + "  if (i >= uCount) return;\n"
        + "  int o = t[i] * " + Gpu.TRI_INTS + ", so = i * " + Gpu.TRI_INTS + ";\n"
        + "  for (int k = 0; k < " + Gpu.TRI_INTS + "; k++) d[o + k] = s[so + k];\n"
        + "}\n";

    private static int scatter, scatterCount, stageBuffer, destBuffer;
    private static long window;
    private static GLCapabilities caps;
    private static Thread owner;
    private static int solid, cutout, vao, triBuffer, palette, textures, fbo, colour, depth;
    private static int sorter, sorterSmall, sorterTiny, modelBuffer, bigModelBuffer, tinyModelBuffer, vertBuffer, faceBuffer, camBuffer;
    private static long triBytes;                          // size of triBuffer this frame
    private static int fbW, fbH, texSize, texLayers, lastCount;
    private static int[] sentPalette;
    private static int paletteSum;
    private static final int[] solidUniforms = new int[6], cutoutUniforms = new int[6];
    static long worldDone;                                 // fence GpuPresent waits on before showing the world texture

    static int worldTexture() { return colour; }
    static long window() { return window; }
    static GLCapabilities caps() { return caps; }

    private GpuGL() { }

    /** Start of the world: clear the framebuffer, send the palette, make room for this frame's triangles. */
    static void start() {
        bind();
        int w = Gpu.width, h = Gpu.height;
        framebuffer(w, h);
        glBindFramebuffer(GL_FRAMEBUFFER, fbo);
        glViewport(0, 0, w, h);
        glClearColor(0f, 0f, 0f, 0f);
        glClearDepth(1.0);
        glDepthMask(true);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        sendPalette();
        glBindVertexArray(vao);
        glActiveTexture(GL_TEXTURE1);
        glBindTexture(GL_TEXTURE_2D, palette);
        glEnable(GL_DEPTH_TEST);
        glDepthFunc(GL_LESS);
        // a fresh buffer each frame (the driver keeps last frame's until the GPU is done with it)
        triBytes = (long) Math.max(lastCount + lastCount / 4, Gpu.BATCH) * Gpu.TRI_INTS * 4;
        glBindBuffer(GL_SHADER_STORAGE_BUFFER, triBuffer);
        glBufferData(GL_SHADER_STORAGE_BUFFER, triBytes, GL_STREAM_DRAW);
        glBindBufferBase(GL_SHADER_STORAGE_BUFFER, 0, triBuffer);
    }

    /**
     * Draws triangles [from, to) while the CPU carries on with the rest of the world, so the two work at the
     * same time. Depth comes from each triangle's place in the whole frame, so batches still layer correctly.
     */
    static void flush(int from, int to, int recFrom, int recTo) {
        int n = to - from;
        if (n <= 0) return;
        sendTextures();
        long need = (long) to * Gpu.TRI_INTS * 4;
        if (need > triBytes) grow(need, (long) from * Gpu.TRI_INTS * 4);
        // only the triangles the CPU recorded cross to the card (packed); the GPU puts each in its slot.
        // Model slots in between are filled by the compute shader, so sending them would be wasted bandwidth.
        int recs = recTo - recFrom;
        if (recs > 0) {
            upload(stageBuffer, Gpu.tris, recFrom * Gpu.TRI_INTS, recs * Gpu.TRI_INTS);
            upload(destBuffer, Gpu.dest, recFrom, recs);
            glBindBufferBase(GL_SHADER_STORAGE_BUFFER, 5, stageBuffer);
            glBindBufferBase(GL_SHADER_STORAGE_BUFFER, 6, destBuffer);
            glUseProgram(scatter);
            glUniform1i(scatterCount, recs);
            glDispatchCompute((recs + 63) / 64, 1, 1);
        }
        if (GpuModels.models + GpuModels.bigModels + GpuModels.tinyModels > 0) sortModels();
        glMemoryBarrier(GL_SHADER_STORAGE_BARRIER_BIT);   // the draw below reads what the compute passes wrote
        glActiveTexture(GL_TEXTURE2);
        glBindTexture(GL_TEXTURE_2D_ARRAY, textures);
        use(solid, from, n, 0);
        glDepthMask(true);
        glDisable(GL_BLEND);
        glDrawArrays(GL_TRIANGLES, 0, n * 3);
        use(cutout, from, n, 1);
        glDepthMask(false);
        glEnable(GL_BLEND);
        glBlendFuncSeparate(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA, GL_ZERO, GL_ZERO);
        glDrawArrays(GL_TRIANGLES, 0, n * 3);
        // (no glFlush here: NVIDIA stalls on a flush per batch, and drivers submit queued work by themselves)
    }

    /** The models queued since the last batch: send them and let the compute shader fill their slots. */
    private static void sortModels() {
        upload(vertBuffer, GpuModels.verts, GpuModels.vertCount * 3);
        upload(faceBuffer, GpuModels.faces, GpuModels.faceInts);
        glBindBuffer(GL_SHADER_STORAGE_BUFFER, camBuffer);
        glBufferData(GL_SHADER_STORAGE_BUFFER, Math.max(GpuModels.vertCount, 1) * 20L, GL_STREAM_DRAW);
        glBindBufferBase(GL_SHADER_STORAGE_BUFFER, 2, vertBuffer);
        glBindBufferBase(GL_SHADER_STORAGE_BUFFER, 3, faceBuffer);
        glBindBufferBase(GL_SHADER_STORAGE_BUFFER, 4, camBuffer);
        // small models on 64-thread groups (most scenery), big ones on 256: each fills only its own slots
        dispatch(sorterSmall, modelBuffer, GpuModels.headers, GpuModels.models);
        dispatch(sorter, bigModelBuffer, GpuModels.bigHeaders, GpuModels.bigModels);
        if (GpuModels.tinyModels > 0) {                    // tiny ones: 64 models per group, one per thread
            upload(tinyModelBuffer, GpuModels.tinyHeaders, GpuModels.tinyModels * GpuModels.HEADER);
            glBindBufferBase(GL_SHADER_STORAGE_BUFFER, 1, tinyModelBuffer);
            glUseProgram(sorterTiny);
            glUniform1i(glGetUniformLocation(sorterTiny, "uZoom"), Resizable.zoom);
            glUniform2i(glGetUniformLocation(sorterTiny, "uCenter"), Gpu.centerX, Gpu.centerY);
            glUniform1i(glGetUniformLocation(sorterTiny, "uCount"), GpuModels.tinyModels);
            glDispatchCompute((GpuModels.tinyModels + 63) / 64, 1, 1);
        }

        glBindBuffer(GL_SHADER_STORAGE_BUFFER, triBuffer);
        GpuModels.clear();
    }

    private static void dispatch(int program, int buffer, int[] headers, int models) {
        if (models == 0) return;
        upload(buffer, headers, models * GpuModels.HEADER);
        glBindBufferBase(GL_SHADER_STORAGE_BUFFER, 1, buffer);
        glUseProgram(program);
        glUniform1i(glGetUniformLocation(program, "uZoom"), Resizable.zoom);
        glUniform2i(glGetUniformLocation(program, "uCenter"), Gpu.centerX, Gpu.centerY);
        glDispatchCompute(models, 1, 1);
    }

    /** A fresh buffer holding the first n ints of src. */
    private static void upload(int buffer, int[] src, int n) {
        upload(buffer, src, 0, n);
    }

    /** A fresh buffer holding n ints of src from off. */
    private static void upload(int buffer, int[] src, int off, int n) {
        glBindBuffer(GL_SHADER_STORAGE_BUFFER, buffer);
        long bytes = Math.max(n, 1) * 4L;
        glBufferData(GL_SHADER_STORAGE_BUFFER, bytes, GL_STREAM_DRAW);
        if (n == 0) return;
        ByteBuffer b = glMapBufferRange(GL_SHADER_STORAGE_BUFFER, 0, bytes, GL_MAP_WRITE_BIT | GL_MAP_INVALIDATE_BUFFER_BIT);
        if (b == null) throw new IllegalStateException("could not map a model buffer");
        b.order(ByteOrder.nativeOrder()).asIntBuffer().put(src, off, n);
        glUnmapBuffer(GL_SHADER_STORAGE_BUFFER);
    }

    /** More triangles than the frame's buffer holds: a bigger one, keeping the first kept bytes. */
    private static void grow(long need, long kept) {
        long size = Math.max(need, triBytes * 2);
        int bigger = glGenBuffers();
        glBindBuffer(GL_COPY_WRITE_BUFFER, bigger);
        glBufferData(GL_COPY_WRITE_BUFFER, size, GL_STREAM_DRAW);
        if (kept > 0) {
            glMemoryBarrier(GL_BUFFER_UPDATE_BARRIER_BIT);    // compute shaders wrote what is copied
            glBindBuffer(GL_COPY_READ_BUFFER, triBuffer);
            glCopyBufferSubData(GL_COPY_READ_BUFFER, GL_COPY_WRITE_BUFFER, 0, 0, kept);
        }
        glDeleteBuffers(triBuffer);
        triBuffer = bigger;
        triBytes = size;
        glBindBuffer(GL_SHADER_STORAGE_BUFFER, triBuffer);
        glBindBufferBase(GL_SHADER_STORAGE_BUFFER, 0, triBuffer);
    }

    /** End of the world: draw what is left, then hand the picture on. */
    static void finish(int from, int recFrom) {
        GpuStats.sceneDone();
        flush(from, Gpu.count, recFrom, Gpu.recs);
        lastCount = Gpu.count;
        GpuStats.drawn();
        int w = Gpu.width, h = Gpu.height, size = w * h;
        if (Gpu.direct) {
            // the picture stays on the card: GpuPresent shows it under the HUD. The view gets the "world here"
            // marker, so whatever the 2D code draws over the world afterwards is all that's left in it.
            worldDone = glFenceSync(GL_SYNC_GPU_COMMANDS_COMPLETE, 0);
            glFlush();
            int[] out = DrawingArea.buffer;
            int stride = DrawingArea.bufferWidth;
            for (int row = 0; row < h; row++) {
                int o = (Gpu.offY + row) * stride + Gpu.offX;
                java.util.Arrays.fill(out, o, o + w, GpuPresent.WORLD);
            }
            GpuStats.copied();
            return;
        }
        glPixelStorei(GL_PACK_ALIGNMENT, 4);
        // read into a GPU-side pixel buffer (the card copies it) and map that, instead of a CPU-side read
        int pbo = glGenBuffers();
        glBindBuffer(GL_PIXEL_PACK_BUFFER, pbo);
        glBufferData(GL_PIXEL_PACK_BUFFER, size * 4L, GL_STREAM_READ);
        glReadPixels(0, 0, w, h, GL_RGBA, GL_UNSIGNED_BYTE, 0L);
        ByteBuffer mapped = glMapBufferRange(GL_PIXEL_PACK_BUFFER, 0, size * 4L, GL_MAP_READ_BIT);
        if (mapped == null) throw new IllegalStateException("could not map the pixel buffer");
        IntBuffer px = mapped.order(ByteOrder.nativeOrder()).asIntBuffer();
        GpuStats.read();
        int[] out = DrawingArea.buffer;
        int stride = DrawingArea.bufferWidth;
        if (Gpu.offX == 0 && stride == w) {
            px.get(out, Gpu.offY * stride, size);
        } else {
            for (int row = 0; row < h; row++) {
                px.position(row * w);
                px.get(out, (Gpu.offY + row) * stride + Gpu.offX, w);
            }
        }
        glUnmapBuffer(GL_PIXEL_PACK_BUFFER);
        glBindBuffer(GL_PIXEL_PACK_BUFFER, 0);
        glDeleteBuffers(pbo);
        int err = glGetError();
        if (err != GL_NO_ERROR) throw new IllegalStateException("OpenGL error 0x" + Integer.toHexString(err));
        GpuStats.copied();
    }

    /** Make the context current on the game thread, creating it the first time. */
    private static void bind() {
        if (window == 0) {
            create();
            return;
        }
        if (owner != Thread.currentThread()) {
            GLFW.glfwMakeContextCurrent(window);
            GL.setCapabilities(caps);
            owner = Thread.currentThread();
        }
    }

    private static void create() {
        if (!GLFW.glfwInit()) throw new IllegalStateException("GLFW could not start");
        GLFW.glfwDefaultWindowHints();
        GLFW.glfwWindowHint(GLFW.GLFW_VISIBLE, GLFW.GLFW_FALSE);
        GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MAJOR, 4);
        GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MINOR, 3);
        GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_PROFILE, GLFW.GLFW_OPENGL_CORE_PROFILE);
        GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_FORWARD_COMPAT, GLFW.GLFW_TRUE);
        window = GLFW.glfwCreateWindow(16, 16, "SoloScape GPU", 0L, 0L);
        if (window == 0) throw new IllegalStateException("no OpenGL 4.3 context (graphics driver too old?)");
        GLFW.glfwMakeContextCurrent(window);
        caps = GL.createCapabilities();
        owner = Thread.currentThread();
        System.out.println("[GPU] " + glGetString(GL_RENDERER) + " / OpenGL " + glGetString(GL_VERSION));

        solid = program("");
        cutout = program("#define CUTOUT\n");
        solidUniforms[5] = cutoutUniforms[5] = 0;
        vao = glGenVertexArrays();                         // empty: the vertex shader fetches everything itself
        stageBuffer = glGenBuffers();
        destBuffer = glGenBuffers();
        scatter = glCreateProgram();
        int sc = shader(GL_COMPUTE_SHADER, SCATTER);
        glAttachShader(scatter, sc);
        glLinkProgram(scatter);
        if (glGetProgrami(scatter, GL_LINK_STATUS) == GL_FALSE) throw new IllegalStateException("scatter shader: " + glGetProgramInfoLog(scatter));
        glDeleteShader(sc);
        scatterCount = glGetUniformLocation(scatter, "uCount");
        triBuffer = glGenBuffers();
        modelBuffer = glGenBuffers();
        vertBuffer = glGenBuffers();
        faceBuffer = glGenBuffers();
        camBuffer = glGenBuffers();
        bigModelBuffer = glGenBuffers();
        tinyModelBuffer = glGenBuffers();
        try {
            sorter = computeProgram(256);
            sorterSmall = computeProgram(64);
            sorterTiny = glCreateProgram();
            int ts = shader(GL_COMPUTE_SHADER, TINY);
            glAttachShader(sorterTiny, ts);
            glLinkProgram(sorterTiny);
            if (glGetProgrami(sorterTiny, GL_LINK_STATUS) == GL_FALSE) throw new IllegalStateException(glGetProgramInfoLog(sorterTiny));
            glDeleteShader(ts);
            GpuModels.enabled = true;
        } catch (Throwable t) {                            // models stay on the CPU; the rest of the GPU path carries on
            GpuModels.enabled = false;
            System.out.println("[GPU] models stay on the CPU: " + t.getMessage());
        }
        lastCount = 0;

        palette = integerTexture(GL_TEXTURE_2D);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_R32UI, 128, 512, 0, GL_RED_INTEGER, GL_UNSIGNED_INT, (IntBuffer) null);
        textures = integerTexture(GL_TEXTURE_2D_ARRAY);
        texSize = texLayers = 0;
        GpuTextures.forget();
    }

    private static int computeProgram(int group) {
        int p = glCreateProgram();
        int cs = shader(GL_COMPUTE_SHADER, "#version 430 core\n#define GROUP " + group + "\n#define MAXF "
            + (group == 64 ? GpuModels.SMALL : GpuModels.MAX_FACES) + "\n" + COMPUTE);
        glAttachShader(p, cs);
        glLinkProgram(p);
        if (glGetProgrami(p, GL_LINK_STATUS) == GL_FALSE) throw new IllegalStateException(glGetProgramInfoLog(p));
        glDeleteShader(cs);
        return p;
    }

    private static int program(String defines) {
        int p = glCreateProgram();
        int vs = shader(GL_VERTEX_SHADER, VERTEX), fs = shader(GL_FRAGMENT_SHADER, "#version 430 core\n" + defines + FRAGMENT);
        glAttachShader(p, vs);
        glAttachShader(p, fs);
        glLinkProgram(p);
        if (glGetProgrami(p, GL_LINK_STATUS) == GL_FALSE) throw new IllegalStateException("shader link: " + glGetProgramInfoLog(p));
        glDeleteShader(vs);
        glDeleteShader(fs);
        glUseProgram(p);
        glUniform1i(glGetUniformLocation(p, "uPalette"), 1);
        glUniform1i(glGetUniformLocation(p, "uTex"), 2);
        glUniform1i(glGetUniformLocation(p, "uBgr"), 1);
        return p;
    }

    private static void use(int p, int first, int count, int pass) {
        glUseProgram(p);
        int[] l = p == solid ? solidUniforms : cutoutUniforms;
        if (l[5] != p) {                                   // looked up once per program, not every batch
            l[0] = glGetUniformLocation(p, "uSize");
            l[1] = glGetUniformLocation(p, "uFirst");
            l[2] = glGetUniformLocation(p, "uCount");
            l[3] = glGetUniformLocation(p, "uPass");
            l[4] = glGetUniformLocation(p, "uCenter");
            l[5] = p;
        }
        glUniform2f(l[0], Gpu.width, Gpu.height);
        glUniform1i(l[1], first);
        glUniform1i(l[2], count);
        glUniform1i(l[3], pass);
        glUniform2i(l[4], Gpu.centerX, Gpu.centerY);
    }

    private static int shader(int type, String source) {
        int s = glCreateShader(type);
        glShaderSource(s, source);
        glCompileShader(s);
        if (glGetShaderi(s, GL_COMPILE_STATUS) == GL_FALSE) throw new IllegalStateException("shader compile: " + glGetShaderInfoLog(s));
        return s;
    }

    private static int integerTexture(int target) {
        int t = glGenTextures();
        glBindTexture(target, t);
        glTexParameteri(target, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        glTexParameteri(target, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
        return t;
    }

    private static void framebuffer(int w, int h) {
        if (fbo != 0 && w == fbW && h == fbH) return;
        if (fbo != 0) { glDeleteFramebuffers(fbo); glDeleteTextures(colour); glDeleteRenderbuffers(depth); }
        fbo = glGenFramebuffers();
        colour = glGenTextures();                          // a texture, so GpuPresent can draw it to the window
        glBindTexture(GL_TEXTURE_2D, colour);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA8, w, h, 0, GL_RGBA, GL_UNSIGNED_BYTE, (ByteBuffer) null);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
        depth = glGenRenderbuffers();
        glBindRenderbuffer(GL_RENDERBUFFER, depth);
        glRenderbufferStorage(GL_RENDERBUFFER, GL_DEPTH_COMPONENT24, w, h);
        glBindFramebuffer(GL_FRAMEBUFFER, fbo);
        glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, colour, 0);
        glFramebufferRenderbuffer(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, GL_RENDERBUFFER, depth);
        if (glCheckFramebufferStatus(GL_FRAMEBUFFER) != GL_FRAMEBUFFER_COMPLETE) throw new IllegalStateException("framebuffer incomplete");
        fbW = w;
        fbH = h;
    }

    /** The HSL palette changes with the brightness setting: re-sent when a sample of it no longer matches. */
    private static void sendPalette() {
        int[] hsl = Class39_Sub5_Sub10_Sub2.anIntArray2452;
        int sum = 0;
        for (int i = 0; i < 65536; i += 61) sum = sum * 31 + hsl[i];
        if (hsl == sentPalette && sum == paletteSum) return;
        sentPalette = hsl;
        paletteSum = sum;
        glBindTexture(GL_TEXTURE_2D, palette);
        glPixelStorei(GL_UNPACK_ALIGNMENT, 4);
        glTexSubImage2D(GL_TEXTURE_2D, 0, 0, 0, 128, 512, GL_RED_INTEGER, GL_UNSIGNED_INT, hsl);
    }

    private static void sendTextures() {
        int s = GpuTextures.size;
        if (s == 0) return;
        glBindTexture(GL_TEXTURE_2D_ARRAY, textures);
        int need = Math.max(GpuTextures.layerOf.length, 1);
        if (GpuTextures.resized || s != texSize || need > texLayers) {
            glTexImage3D(GL_TEXTURE_2D_ARRAY, 0, GL_R32UI, s, 4 * s, need, 0, GL_RED_INTEGER, GL_UNSIGNED_INT, (IntBuffer) null);
            texSize = s;
            texLayers = need;
            GpuTextures.resized = false;
        }
        for (int i = 0; i < GpuTextures.dirty; i++) {
            int[] t = GpuTextures.dirtyTexels[i];
            if (t.length != 4 * s * s) t = java.util.Arrays.copyOf(t, 4 * s * s);
            glTexSubImage3D(GL_TEXTURE_2D_ARRAY, 0, 0, 0, GpuTextures.dirtyLayer[i], s, 4 * s, 1, GL_RED_INTEGER, GL_UNSIGNED_INT, t);
            GpuTextures.dirtyTexels[i] = null;
        }
        GpuTextures.dirty = 0;
    }

    static void dispose() {
        try { GpuPresent.dispose(); } catch (Throwable ignored) { }
        worldDone = 0;
        if (window != 0) {
            GLFW.glfwDestroyWindow(window);
            window = 0;
        }
        fbo = 0;
        fbW = fbH = 0;
        sentPalette = null;
        GpuTextures.forget();
    }
}
