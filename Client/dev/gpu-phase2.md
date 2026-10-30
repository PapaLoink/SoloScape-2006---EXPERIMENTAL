# GPU phase 2: models transformed and sorted on the GPU

Status notes for the multi-session GPU renderer. Phase 1 (done): the CPU rasterizer's triangles are recorded
(`Gpu`), drawn by OpenGL (`GpuGL`) in batches, and shown straight in the window (`GpuPresent`).

## Why
Profile (Varrock, zoomed out, 1867x996): `Model.method546` (per-model face cull + depth bucket sort + priority
order) 35% of the game thread, scene walk (`Class38`) 24%, `Model.method488` vertex transform 9%,
`Model.method552` face dispatch 6%, recording/batching about 10%.

## Plan
- **A1** OpenGL 4.3 context (compute shaders, SSBOs). If the driver has no 4.3, phase 2 stays off.
- **A2** One record per triangle in an SSBO (28 ints: mode, rgb, alpha share, texture layer, 12 texture
  plane ints, then x, y, shade for each corner). The vertex shader reads it by `gl_VertexID` and picks the
  pass itself (solid faces nearest first, see-through faces in order), so the CPU no longer sorts or builds
  index buffers.
- **A3** `Model.method488`: when recording and the model is safe for the GPU, reserve one slot per face at
  this point of the draw order and queue the model instead of transforming and sorting it on the CPU.
  Stays on the CPU: models that may cross the near plane (`i_230_ - i_241_ <= 50`), models under the mouse
  (picking, `bool_244_` / `aBoolean2374`), models with 1600+ depth buckets (never drawn), models whose
  textures are not loaded yet.
- **A4** Compute shader, one work group per model: transform the vertices with the same integer maths
  as method488 (`>> 16` rotations, `* Resizable.zoom / z` projection); backface test as method546
  (screen-space cross product > 0); bucket = (z0 + z1 + z2) / 3 + anInt2358.
- **A5** Draw order inside a model (rank of each visible face; hidden ones become empty triangles at the end):
  - no priorities: bucket descending, then face index ascending;
  - priorities 0-9: groups in order, each bucket descending / index ascending;
  - priorities 10 and 11 form one list S (all of 10, then all of 11, each sorted as above) that is slotted
    in before group 0 while depth > avg(1, 2), before group 3 while depth > avg(3, 4), before group 5 while
    depth > avg(6, 8), and the rest after group 9 (averages are integer means of the bucket values, 0 if
    the groups are empty). The cut points are the first S item failing each test, searched from the
    previous cut.
- **A6** Textured faces: the U/V/W planes from the camera-space corners exactly as `Gpu.textured`;
  texture id to array layer through a small table filled by `GpuTextures`.
## Status (2026-10-29)
A2-A6 are done and behind the setting "GPU model sorting (experimental)", off by default. On Intel UHD
(1867x996, Varrock zoomed out, about 2,300 models and 137k faces a frame) it gives about 80 FPS against
about 70 with the setting off. Measured with forced GPU waits: geometry upload about 1.4 ms, so caching
(B) is a small win. What mattered was compute occupancy. Shared arrays are now sized per variant, and
the compute work is split three ways:
- up to 32 faces: one thread per model (`TINY` shader), 64 models per group;
- up to 512 faces: 64-thread groups;
- larger: 256-thread groups.

- **B** Cache static models on the GPU (scenery never changes shape) so only animated ones are sent each
  frame.
