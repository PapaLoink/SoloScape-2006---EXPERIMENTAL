package unpackaged;

/* Class13_Sub1_Sub2 - Decompiled by JODE
 * Visit http://jode.sourceforge.net/
 */
import jagex.world.actors.StillGraphic;
import java.awt.Component;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

public class Class13_Sub1_Sub2 extends Class13_Sub1
{
    public byte[] aByteArray1612;
    public SourceDataLine aSourceDataLine1613;
    public AudioFormat anAudioFormat1614;
    public static Class aClass1615;
    
    public void method200(int i) throws LineUnavailableException {
	try {
	    DataLine.Info info
		= (new DataLine.Info
		   ((aClass1615 == null
		     ? (aClass1615
			= method206("javax.sound.sampled.SourceDataLine"))
		     : aClass1615),
		    anAudioFormat1614,
		    i << (StillGraphic.aBoolean2342 ? 2 : 1)));
	    aSourceDataLine1613 = (SourceDataLine) AudioSystem.getLine(info);
	    aSourceDataLine1613.open();
            if (Boolean.getBoolean("web.audio.remote")) {
                try {
                    if (aSourceDataLine1613.isControlSupported(
                            javax.sound.sampled.BooleanControl.Type.MUTE)) {
                        ((javax.sound.sampled.BooleanControl) aSourceDataLine1613.getControl(
                                javax.sound.sampled.BooleanControl.Type.MUTE)).setValue(true);
                    } else if (aSourceDataLine1613.isControlSupported(
                            javax.sound.sampled.FloatControl.Type.MASTER_GAIN)) {
                        javax.sound.sampled.FloatControl gain =
                                (javax.sound.sampled.FloatControl) aSourceDataLine1613.getControl(
                                        javax.sound.sampled.FloatControl.Type.MASTER_GAIN);
                        gain.setValue(gain.getMinimum());
                    }
                } catch (IllegalArgumentException ignored) {
                    // Some mixers cannot mute individual SourceDataLines.
                }
            }
            aSourceDataLine1613.start();
	} catch (LineUnavailableException lineunavailableexception) {
	    aSourceDataLine1613 = null;
	    throw lineunavailableexception;
	}
    }
    
    public void method199() {
	int i = 256;
	if (StillGraphic.aBoolean2342)
	    i <<= 1;
	for (int i_0_ = 0; i_0_ < i; i_0_++) {
	    int i_1_ = anIntArray1310[i_0_];
	    if ((i_1_ + 8388608 & ~0xffffff) != 0)
		i_1_ = 0x7fffff ^ i_1_ >> 31;
	    aByteArray1612[i_0_ * 2] = (byte) (i_1_ >> 8);
	    aByteArray1612[i_0_ * 2 + 1] = (byte) (i_1_ >> 16);
	}
        // Capture the already-mixed RuneScape sound effects for remote browsers.
        // Keep feeding the Java SourceDataLine so its timing/available() logic
        // continues to work even when the host mixer supports muting.
        if (Boolean.getBoolean("web.audio.remote")) {
            WebClientBridge.publishAudio(1, Class15.anInt274,
                    StillGraphic.aBoolean2342 ? 2 : 1, aByteArray1612, i << 1);
        }
        aSourceDataLine1613.write(aByteArray1612, 0, i << 1);
    }
    
    public void method202(Component component, int i, boolean bool) {
	Class15.anInt274 = i;
	StillGraphic.aBoolean2342 = bool;
	anAudioFormat1614
	    = new AudioFormat((float) Class15.anInt274, 16,
			      !StillGraphic.aBoolean2342 ? 1 : 2,
			      true, false);
	aByteArray1612
	    = new byte[256 << (StillGraphic.aBoolean2342 ? 2 : 1)];
    }
    
    public int method201() {
	return (aSourceDataLine1613.available()
		>> (StillGraphic.aBoolean2342 ? 2 : 1));
    }
    
    public void destroy() {
	if (aSourceDataLine1613 != null) {
	    aSourceDataLine1613.close();
	    aSourceDataLine1613 = null;
	}
    }
    
    public static Class method206(String string) {
	Class var_class;
	try {
	    var_class = Class.forName(string);
	} catch (ClassNotFoundException classnotfoundexception) {
	    return null;
	}
	return var_class;
    }
}
