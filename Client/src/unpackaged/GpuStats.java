package unpackaged;

/** GPU profiling: with -Dsoloscape.gpustats=true, averages are printed to the client log every 5 seconds. */
final class GpuStats {
    static long frameStart, lastFrame, sceneStart, mark;
    static long frames, frameNs, sceneNs, uploadNs, drawNs, readNs, copyNs, tris, lastPrint;

    static void begin() {
        long now = System.nanoTime();
        if (lastFrame != 0) frameNs += now - lastFrame;
        lastFrame = now;
        sceneStart = now;
        frames++;
    }

    static void sceneDone() { long now = System.nanoTime(); sceneNs += now - sceneStart; mark = now; tris += Gpu.count; }
    static void uploaded() { long now = System.nanoTime(); uploadNs += now - mark; mark = now; }
    static void drawn() { long now = System.nanoTime(); drawNs += now - mark; mark = now; }
    static void read() { long now = System.nanoTime(); readNs += now - mark; mark = now; }

    static void copied() {
        long now = System.nanoTime();
        copyNs += now - mark;
        if (lastPrint == 0) lastPrint = now;
        if (now - lastPrint < 5_000_000_000L || frames < 2) return;
        if (!Boolean.getBoolean("soloscape.gpustats")) {    // java -Dsoloscape.gpustats=true to print
            frames = frameNs = sceneNs = uploadNs = drawNs = readNs = copyNs = tris = 0;
            lastFrame = 0;
            lastPrint = now;
            return;
        }
        double f = frames;
        System.out.printf("[GPU] %.1f fps | frame %.2f ms | scene cpu %.2f | upload %.2f | draw %.2f | readback %.2f | copy %.2f | %d tris %dx%d%n",
            f * 1e9 / (now - lastPrint), frameNs / 1e6 / (f - 1), sceneNs / 1e6 / f, uploadNs / 1e6 / f, drawNs / 1e6 / f,
            readNs / 1e6 / f, copyNs / 1e6 / f, tris / frames, Gpu.width, Gpu.height);
        frames = frameNs = sceneNs = uploadNs = drawNs = readNs = copyNs = tris = 0;
        lastFrame = 0;
        lastPrint = now;
    }
}
