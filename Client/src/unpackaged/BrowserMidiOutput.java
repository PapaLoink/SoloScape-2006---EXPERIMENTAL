package unpackaged;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Collections;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Receiver;
import javax.sound.midi.Synthesizer;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;

/**
 * Renders the game's existing MIDI sequencer through Java 8's software synth.
 * The browser receives its real PCM output rather than trying to play MIDI
 * through Web MIDI (which is absent on iOS and many other browsers).
 *
 * The Java 8 SoftSynthesizer provides openStream; use reflection because it is
 * a JDK-internal implementation detail. Failure cleanly falls back to the
 * client's normal desktop MIDI receiver.
 */
final class BrowserMidiOutput implements Runnable {
    private static final int SAMPLE_RATE = 22050;
    private static final int CHANNELS = 2;
    private static final int FRAMES_PER_CHUNK = 512;

    final Receiver receiver;
    private final Synthesizer synth;
    private final AudioInputStream stream;
    private final Thread thread;
    private volatile boolean running = true;

    static BrowserMidiOutput tryOpen() {
        if (!Boolean.getBoolean("web.audio.remote") || !WebClientBridge.isEnabled())
            return null;
        Synthesizer synth = null;
        AudioInputStream stream = null;
        try {
            synth = MidiSystem.getSynthesizer();
            AudioFormat format = new AudioFormat(SAMPLE_RATE, 16, CHANNELS, true, false);
            Method openStream = synth.getClass().getMethod(
                    "openStream", AudioFormat.class, java.util.Map.class);
            Object result = openStream.invoke(synth, format, Collections.emptyMap());
            if (!(result instanceof AudioInputStream))
                throw new IllegalStateException("Software MIDI synthesizer cannot produce PCM");
            stream = (AudioInputStream) result;
            Receiver receiver = synth.getReceiver();
            return new BrowserMidiOutput(synth, stream, receiver);
        } catch (Exception exception) {
            System.err.println("[webclient] Browser MIDI capture unavailable: " + exception);
            if (stream != null) try { stream.close(); } catch (IOException ignored) {}
            if (synth != null) synth.close();
            return null;
        }
    }

    private BrowserMidiOutput(Synthesizer synth, AudioInputStream stream, Receiver receiver) {
        this.synth = synth;
        this.stream = stream;
        this.receiver = receiver;
        this.thread = new Thread(this, "webclient-midi-pcm");
        this.thread.setDaemon(true);
        this.thread.start();
    }

    public void run() {
        byte[] pcm = new byte[FRAMES_PER_CHUNK * CHANNELS * 2];
        long next = System.nanoTime();
        try {
            while (running) {
                int got = 0;
                while (got < pcm.length && running) {
                    int read = stream.read(pcm, got, pcm.length - got);
                    if (read < 0) return;
                    if (read == 0) break;
                    got += read;
                }
                if (!running) break;
                int aligned = got & ~3;
                if (aligned != 0) {
                    WebClientBridge.publishAudio(2, SAMPLE_RATE, CHANNELS, pcm, aligned);
                }

                // openStream produces bytes as quickly as read; pace it to wall
                // clock so the MIDI runs at the correct speed and doesn't flood.
                next += (long) (aligned / (CHANNELS * 2)) * 1_000_000_000L / SAMPLE_RATE;
                long remaining = next - System.nanoTime();
                if (remaining > 0) {
                    Thread.sleep(remaining / 1_000_000L,
                            (int) (remaining % 1_000_000L));
                } else if (remaining < -200_000_000L) {
                    next = System.nanoTime();
                }
            }
        } catch (Exception exception) {
            if (running) System.err.println("[webclient] Browser MIDI stream stopped: " + exception);
        }
    }

    void close() {
        running = false;
        thread.interrupt();
        try { stream.close(); } catch (IOException ignored) {}
        synth.close();
    }
}
