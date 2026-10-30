package unpackaged;

/* Class62_Sub1_Sub1 - Decompiled by JODE
 * Visit http://jode.sourceforge.net/
 */
import jagex.utils.Queue;
import java.io.ByteArrayInputStream;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Receiver;
import javax.sound.midi.Sequence;
import javax.sound.midi.Sequencer;
import javax.sound.midi.ShortMessage;

public class Class62_Sub1_Sub1 extends Class62_Sub1 implements Receiver
{
    public static Sequencer sequencer;
    public static volatile boolean aBoolean2161;
    public static Receiver reciever = null;
    private static BrowserMidiOutput browserMidi;
    
    public synchronized void method1055(int i, int i_0_, int i_1_) {
	if (sequencer != null) {
	    if (i_1_ != 0)
		destroy();
	    method1068((byte) -9, i, -1L, i_0_);
	}
    }
    
    public void method1051(int i, byte[] is, byte i_2_, boolean bool) {
	if (sequencer != null) {
	    try {
		Sequence sequence
		    = MidiSystem.getSequence(new ByteArrayInputStream(is));
		sequencer.setSequence(sequence);
		if (i_2_ <= 73)
		    method1054(-47);
		sequencer.setLoopCount(bool ? -1 : 0);
		method1068((byte) -9, 0, -1L, i);
		ModHook.trackStarting();
		aBoolean2161 = true;
		sequencer.start();
	    } catch (Exception exception) {
		/* empty */
	    }
	}
    }
    
    @Override
    public boolean isPlaying() {
        Sequencer activeSequencer = sequencer;
        return activeSequencer != null && activeSequencer.isRunning();
    }

    public void destroy() {
	if (sequencer != null) {
	    sequencer.close();
	    sequencer = null;
	}
	if (browserMidi != null) {
	    browserMidi.close();
	    browserMidi = null;
	    reciever = null;
	} else if (reciever != null) {
	    reciever.close();
	    reciever = null;
	}
    }
    
    public synchronized void send(MidiMessage midimessage, long l) {
	if (ModHook.midiMuted(midimessage)) return;
	if (aBoolean2161) {
	    byte[] is = midimessage.getMessage();
	    if (!method1060(is[0] & 0xff, is[1], is.length >= 3 ? is[2] : 0, l))
		reciever.send(midimessage, l);
	}
    }
    
    public void method1053(int i, byte i_3_) {
	if (sequencer != null) {
	    method1061(-1L, true, i);
	    if (i_3_ <= 44)
		destroy();
	}
    }
    
    public void method1054(int i) {
	if (i != 0)
	    close();
    }
    
    public void method1065(int i, int i_4_, int i_5_, long l) {
	try {
	    ShortMessage shortmessage = new ShortMessage();
	    shortmessage.setMessage(i, i_4_, i_5_);
	    reciever.send(shortmessage, l);
	} catch (InvalidMidiDataException invalidmididataexception) {
	    /* empty */
	}
    }
    
    public Class62_Sub1_Sub1() {
	try {
	    browserMidi = BrowserMidiOutput.tryOpen();
	    reciever = browserMidi != null ? browserMidi.receiver : MidiSystem.getReceiver();
	    sequencer = MidiSystem.getSequencer(false);
	    sequencer.getTransmitter().setReceiver(this);
	    sequencer.open();
	    method1062(-1L, 0);
	} catch (Exception exception) {
	    Queue.method987(-30574);
	}
    }
    
    public void close() {
	/* empty */
    }
    
    public void method1048(boolean bool) {
	if (sequencer != null) {
	    aBoolean2161 = bool;
	    sequencer.stop();
	    method1062(-1L, 0);
	}
    }
    
    static {
	sequencer = null;
	aBoolean2161 = false;
    }
}
