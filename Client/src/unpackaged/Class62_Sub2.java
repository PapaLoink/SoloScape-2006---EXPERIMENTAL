package unpackaged;

/* Class62_Sub2 - Decompiled by JODE
 * Visit http://jode.sourceforge.net/
 */
import jagex.io.BufferedFile;
import jagex.io.FileOnDisk;
import jagex.utils.HashTable;
import jagex.utils.Huffmans;
import jagex.world.actors.GroundItem;
import jagex.graphics.JImage;
import jagex.graphics.AbstractImage;
import jagex.graphics.sprites.DirectColorSprite;
import jagex.graphics.sprites.IndexedColorSprite;
import jagex.world.actors.StillGraphic;
import jagex.utils.SubNode;
import jagex.utils.JString;
import jagex.io.JSocket;
import jagex.world.actors.Npc;
import jagex.world.map.TraversalMap;
import jagex.utils.Queue;
import jagex.io.FrameBuffer;
import jagex.utils.Cache;
import java.awt.Graphics;

public class Class62_Sub2 extends Class62
{
    public static int anInt1596;
    public static int anInt1597;
    public static JString aClass3_1598
	= Class39_Sub5_Sub9.createJstring("Freund hinzuf-Ugen");
    public boolean aBoolean1599;
    public Signlink aClass21_1600;
    public static JString aClass3_1601;
    public Resource aClass56_1602;
    public static JString aClass3_1603;
    public boolean aBoolean1604;
    public static JString aClass3_1605;
    public static JString aClass3_1606;
    public static IndexedColorSprite[] aClass39_Sub5_Sub10_Sub4Array1607;
    public byte[] aByteArray1608;
    public static JString aClass3_1609;
    public int anInt1610;
    
    public static void method1072(int i) {
	if (i != 6255)
	    method1083(-28, (byte) 49);
	for (StillGraphic class39_sub5_sub4_sub5
		 = ((StillGraphic)
		    RuntimeException_Sub1.aClass49_1217.getFirst());
	     class39_sub5_sub4_sub5 != null;
	     class39_sub5_sub4_sub5
		 = (StillGraphic) RuntimeException_Sub1
						.aClass49_1217
						.getNext()) {
	    if (class39_sub5_sub4_sub5.anInt2322 != NameTable.height
		|| class39_sub5_sub4_sub5.aBoolean2333)
		class39_sub5_sub4_sub5.unlinkDeque();
	    else if (class39_sub5_sub4_sub5.anInt2336 <= Class2.logicCycle) {
		class39_sub5_sub4_sub5.method532(Class45.anInt856, i - 6253);
		if (class39_sub5_sub4_sub5.aBoolean2333)
		    class39_sub5_sub4_sub5.unlinkDeque();
		else
		    Class44.aClass38_836.method375
			(class39_sub5_sub4_sub5.anInt2322,
			 class39_sub5_sub4_sub5.anInt2323,
			 class39_sub5_sub4_sub5.anInt2321,
			 class39_sub5_sub4_sub5.anInt2347, 60,
			 class39_sub5_sub4_sub5, 0, -1, false);
	    }
	}
    }
    
    public static void method1073(int i) {
	aClass3_1603 = null;
	aClass3_1605 = null;
	aClass3_1598 = null;
	aClass3_1609 = null;
	if (i == -1228312600) {
	    aClass39_Sub5_Sub10_Sub4Array1607 = null;
	    aClass3_1606 = null;
	    aClass3_1601 = null;
	}
    }
    
    public static DirectColorSprite method1074(boolean bool) {
	DirectColorSprite class39_sub5_sub10_sub3
	    = new DirectColorSprite();
	class39_sub5_sub10_sub3.anInt2472 = SubNode.anIntArray1352[0];
	class39_sub5_sub10_sub3.width
	    = Class39_Sub5_Sub9.anIntArray1799[0];
	class39_sub5_sub10_sub3.height = Class39_Sub14.anIntArray1512[0];
	int i = (class39_sub5_sub10_sub3.height
		 * class39_sub5_sub10_sub3.width);
	class39_sub5_sub10_sub3.anIntArray2476 = new int[i];
	byte[] is = TraversalMap.aByteArrayArray517[0];
	class39_sub5_sub10_sub3.anInt2473 = Class46_Sub1.anIntArray1548[0];
	class39_sub5_sub10_sub3.anInt2477 = Class39_Sub5_Sub12.anInt1854;
	class39_sub5_sub10_sub3.anInt2475 = Class13.anInt203;
	for (int i_0_ = 0; i > i_0_; i_0_++)
	    class39_sub5_sub10_sub3.anIntArray2476[i_0_]
		= Class39_Sub11.anIntArray1460[Class34.method342(is[i_0_],
								 255)];
	if (bool != true)
	    return null;
	RuntimeException_Sub1.method1123();
	return class39_sub5_sub10_sub3;
    }
    
    public static void method1075(byte i) {
	while (Class39_Sub5_Sub11.gameBuffer
		   .getRemainingBits(Huffmans.frameSize)
	       >= 15) {
	    int i_1_
		= Class39_Sub5_Sub11.gameBuffer.getBits(15);
	    if (i_1_ == 32767)
		break;
	    boolean bool = false;
	    if ((GroundItem.aClass39_Sub5_Sub4_Sub4_Sub1Array2241
		 [i_1_])
		== null) {
		GroundItem
		    .aClass39_Sub5_Sub4_Sub4_Sub1Array2241[i_1_]
		    = new Npc();
		bool = true;
	    }
	    Npc class39_sub5_sub4_sub4_sub1
		= (GroundItem.aClass39_Sub5_Sub4_Sub4_Sub1Array2241
		   [i_1_]);
	    Class39_Sub5_Sub4.anIntArray1734[ArchiveWorker.anInt1210++] = i_1_;
	    class39_sub5_sub4_sub4_sub1.anInt2290 = Class2.logicCycle;
	    int i_2_
		= Class39_Sub5_Sub11.gameBuffer.getBits(1);
	    if (i_2_ == 1)
		ArchiveRequest.anIntArray1400[JImage.anInt1586++] = i_1_;
	    class39_sub5_sub4_sub4_sub1.aClass39_Sub5_Sub13_2492
		= ArchiveWorker.getNpcDefinition(Class39_Sub5_Sub11
					 .gameBuffer
					 .getBits(13));
	    int i_3_
		= Class39_Sub5_Sub11.gameBuffer.getBits(5);
	    int i_4_ = (Client.anIntArray1276
			[Class39_Sub5_Sub11.gameBuffer
			     .getBits(3)]);
	    if (bool)
		class39_sub5_sub4_sub4_sub1.anInt2251 = i_4_;
	    if (i_3_ > 15)
		i_3_ -= 32;
	    int i_5_ = Class39_Sub5_Sub11.gameBuffer
			   .getBits(5);
	    int i_6_
		= Class39_Sub5_Sub11.gameBuffer.getBits(1);
	    class39_sub5_sub4_sub4_sub1.anInt2250
		= (class39_sub5_sub4_sub4_sub1.aClass39_Sub5_Sub13_2492
		   .anInt1882);
	    class39_sub5_sub4_sub4_sub1.anInt2297
		= (class39_sub5_sub4_sub4_sub1.aClass39_Sub5_Sub13_2492
		   .anInt1870);
	    class39_sub5_sub4_sub4_sub1.anInt2263
		= (class39_sub5_sub4_sub4_sub1.aClass39_Sub5_Sub13_2492
		   .anInt1872);
	    class39_sub5_sub4_sub4_sub1.anInt2280
		= (class39_sub5_sub4_sub4_sub1.aClass39_Sub5_Sub13_2492
		   .anInt1875);
	    class39_sub5_sub4_sub4_sub1.anInt2264
		= (class39_sub5_sub4_sub4_sub1.aClass39_Sub5_Sub13_2492
		   .anInt1861);
	    class39_sub5_sub4_sub4_sub1.anInt2282
		= (class39_sub5_sub4_sub4_sub1.aClass39_Sub5_Sub13_2492
		   .anInt1879);
	    if (i_5_ > 15)
		i_5_ -= 32;
	    class39_sub5_sub4_sub4_sub1.anInt2317
		= (class39_sub5_sub4_sub4_sub1.aClass39_Sub5_Sub13_2492
		   .anInt1887);
	    if (class39_sub5_sub4_sub4_sub1.anInt2250 == 0)
		class39_sub5_sub4_sub4_sub1.anInt2251 = 0;
	    class39_sub5_sub4_sub4_sub1.anInt2257
		= (class39_sub5_sub4_sub4_sub1.aClass39_Sub5_Sub13_2492
		   .anInt1863);
	    class39_sub5_sub4_sub4_sub1.anInt2262
		= (class39_sub5_sub4_sub4_sub1.aClass39_Sub5_Sub13_2492
		   .anInt1877);
	    class39_sub5_sub4_sub4_sub1.method508
		((i_5_
		  + Cache.aClass39_Sub5_Sub4_Sub4_Sub2_109.anIntArray2255[0]),
		 i_6_ == 1, (byte) -15,
		 i_3_ + (Cache.aClass39_Sub5_Sub4_Sub4_Sub2_109.anIntArray2314
			 [0]));
	}
	if (i != 126)
	    aClass3_1601 = null;
	Class39_Sub5_Sub11.gameBuffer.finishBitAccess();
    }
    
    public static void method1076(int i, FileTable class9) {
	HashTable.aClass9_369 = class9;
    }
    
    public void method1053(int i, byte i_7_) {
	if (i_7_ <= 44)
	    method1073(104);
    }
    
    public static void getTableChecksum(FileLoader fileLoader, int indexId) {
	if (JImage.updateTableBuffer != null) {
	    JImage.updateTableBuffer.offset = indexId * 4 + 5;
	    int checksum = JImage.updateTableBuffer.getDword();
	    fileLoader.setChecksum(checksum);
	} else {
	    Class39_Sub10.requestArchive(null, 255, 255, true, 0, (byte) 0);
	    Class33.fileLoaders[indexId] = fileLoader;
	}
    }
    
    public static int method1078(int i, int i_10_, int i_11_, int i_12_,
				 int i_13_, int i_14_, int i_15_) {
	i &= 0x3;
	if ((i_13_ & 0x1) == 1) {
	    int i_16_ = i_15_;
	    i_15_ = i_14_;
	    i_14_ = i_16_;
	}
	if (i == 0)
	    return i_10_;
	if (i == 1)
	    return i_11_;
	if (i == 2)
	    return -i_10_ - i_15_ + 1 + 7;
	return -i_11_ + 7 + (-i_14_ + 1);
    }
    
    public static void method1079(int i) {
	try {
	    if (i == 8) {
		Graphics graphics = Class41.aCanvas778.getGraphics();
		Class39_Sub5_Sub7.aClass57_1778.draw(graphics,
							   553, 205);
	    }
	} catch (Exception exception) {
	    Class41.aCanvas778.repaint();
	}
    }
    
    public static void method1080(byte i) {
	Class26.aFontMetrics491 = null;
	BufferedFile.aFont351 = null;
	Class39_Sub11.anImage1464 = null;
    }
    
    public void method1048(boolean bool) {
	if (aBoolean1599) {
	    try {
		//Class64.method1091("midibox.src=\"c:/silence.mid\";", 19048,
		//		   aClass21_1600.anApplet416);
	    } catch (Throwable throwable) {
		/* empty */
	    }
	    aBoolean1599 = false;
	}
	aClass56_1602 = null;
	if (bool)
	    anInt1597 = 27;
    }
    
    public static Widget method1081(int i, int i_17_, int i_18_) {
	Widget class39_sub5_sub17
	    = Class37.getWidget(i);
	if (i_17_ == -1)
	    return class39_sub5_sub17;
	if (class39_sub5_sub17 == null
	    || class39_sub5_sub17.aClass39_Sub5_Sub17Array2025 == null
	    || i_17_ >= class39_sub5_sub17.aClass39_Sub5_Sub17Array2025.length)
	    return null;
	return class39_sub5_sub17.aClass39_Sub5_Sub17Array2025[i_17_];
    }
    
    public void method1055(int i, int i_19_, int i_20_) {
	if (i_20_ == i_19_)
	    i_19_ = 1;
	int i_21_ = -i + Class39_Sub5_Sub4_Sub4.method512(929, i_19_);
	if (aClass56_1602 == null) {
	    if (aBoolean1599) {
		try {
		    //Class64.method1091("midibox.volume=" + i_21_ + ";", 19048,
			//	       aClass21_1600.anApplet416);
		} catch (Throwable throwable) {
		    /* empty */
		}
	    }
	} else
	    anInt1610 = i_21_;
    }
    
    public void destroy() {

    }
    
    public static void method1082(byte i) {
	Class53.method985(false, false);
	Varbit.anInt1798 = 0;
	boolean bool = true;
	for (int i_22_ = 0;
	     i_22_ < Class39_Sub5_Sub16.aByteArrayArray1989.length; i_22_++) {
	    if (ItemDefinition.anIntArray1682[i_22_] != -1
		&& Class39_Sub5_Sub16.aByteArrayArray1989[i_22_] == null) {
		Class39_Sub5_Sub16.aByteArrayArray1989[i_22_]
		    = JSocket.fileLoader5.lookupFile((ItemDefinition
							  .anIntArray1682
							  [i_22_]),
							 0);
		if (Class39_Sub5_Sub16.aByteArrayArray1989[i_22_] == null) {
		    bool = false;
		    Varbit.anInt1798++;
		}
	    }
	    if (Class55.anIntArray1255[i_22_] != -1
		&& Cache.aByteArrayArray104[i_22_] == null) {
		int[] keys = Class14.anIntArrayArray221[i_22_];
		int region = Class65.anIntArray1132[i_22_];
		boolean missingKeys = (keys[0] == 0 && keys[1] == 0
			&& keys[2] == 0 && keys[3] == 0);
		java.io.File fallback = new java.io.File
		    (new java.io.File(Class39_Sub5_Sub9.signlink.cacheFilePath,
				      "map-fallback"),
		     "l" + (region >> 8) + "_" + (region & 0xff) + ".dat");
		if (missingKeys && fallback.isFile()) {
		    try {
			Cache.aByteArrayArray104[i_22_]
			    = java.nio.file.Files.readAllBytes(fallback.toPath());
			System.out.println("[MAPDBG] using landscape fallback region="
				+ (region >> 8) + "_" + (region & 0xff));
		    } catch (java.io.IOException ioexception) {
			System.err.println("Unable to read landscape fallback "
				+ fallback + ": " + ioexception.getMessage());
		    }
		} else {
		    Cache.aByteArrayArray104[i_22_]
			= (JSocket.fileLoader5.lookupFile
			   (Class55.anIntArray1255[i_22_], 0, keys));
		}
		System.out.println("[MAPDBG] landscape region="
			+ (Class65.anIntArray1132[i_22_] >> 8) + "_"
			+ (Class65.anIntArray1132[i_22_] & 0xff)
			+ " archive=" + Class55.anIntArray1255[i_22_]
			+ " bytes=" + (Cache.aByteArrayArray104[i_22_] == null
			    ? -1 : Cache.aByteArrayArray104[i_22_].length)
			+ " keys=" + java.util.Arrays.toString(
			    Class14.anIntArrayArray221[i_22_]));
		if (Cache.aByteArrayArray104[i_22_] == null) {
		    Varbit.anInt1798++;
		    bool = false;
		}
	    }
	}
	if (!bool)
	    Class1.anInt33 = 1;
	else {
	    bool = true;
	    RuntimeException_Sub1.anInt1222 = 0;
	    for (int i_23_ = 0;
		 Class39_Sub5_Sub16.aByteArrayArray1989.length > i_23_;
		 i_23_++) {
		byte[] is = Cache.aByteArrayArray104[i_23_];
		if (is != null) {
		    int i_24_ = ((Class65.anIntArray1132[i_23_] & 0xff) * 64
				 - JKeyListener.anInt618);
		    int i_25_ = (-Class65.anInt1145
				 + (Class65.anIntArray1132[i_23_] >> 8) * 64);
		    if (Class63.aBoolean1120) {
			i_24_ = 10;
			i_25_ = 10;
		    }
		    bool &= Class14.method207(false, i_24_, i_25_, is);
		}
	    }
	    bool &= ChristmasEventSnow.groundSnowModelsReady();
	    System.out.println("[MAPDBG] readiness ok=" + bool
		    + " missingModels=" + RuntimeException_Sub1.anInt1222);
	    if (!bool)
		Class1.anInt33 = 2;
	    else {
		if (Class1.anInt33 != 0)
		    Class26.method294(true, NameTable.aClass3_190, aClass3_1606,
				      -111);
		Class62.method1045(false);
		Class44.aClass38_836.method378();
		System.gc();
		for (int i_26_ = 0; i_26_ < 4; i_26_++)
		    Class39_Sub5_Sub12.aClass27Array1857[i_26_]
			.reset((byte) -123);
		for (int i_27_ = 0; i_27_ < 4; i_27_++) {
		    for (int i_28_ = 0; i_28_ < 104; i_28_++) {
			for (int i_29_ = 0; i_29_ < 104; i_29_++)
			    Class55.tileFlags[i_27_][i_28_]
				[i_29_]
				= (byte) 0;
		    }
		}
		ObjectDefinition.method732(-100);
		int i_30_ = Class39_Sub5_Sub16.aByteArrayArray1989.length;
		NpcDefinition.method723((byte) 124);
		Class53.method985(true, false);
		if (!Class63.aBoolean1120) {
		    for (int i_31_ = 0; i_30_ > i_31_; i_31_++) {
			int i_32_ = ((Class65.anIntArray1132[i_31_] >> 8) * 64
				     - Class65.anInt1145);
			int i_33_
			    = (-JKeyListener.anInt618
			       + (Class65.anIntArray1132[i_31_] & 0xff) * 64);
			byte[] is
			    = Class39_Sub5_Sub16.aByteArrayArray1989[i_31_];
			if (is != null)
			    Class46.method933((Class39_Sub5_Sub12
					       .aClass27Array1857),
					      i_33_, is, (byte) 117, i_32_,
					      (Class65.anInt1147 - 6) * 8,
					      anInt1597 * 8 - 48);
		    }
		    for (int i_34_ = 0; i_34_ < i_30_; i_34_++) {
			int i_35_
			    = (-Class65.anInt1145
			       + (Class65.anIntArray1132[i_34_] >> 8) * 64);
			int i_36_
			    = ((Class65.anIntArray1132[i_34_] & 0xff) * 64
			       - JKeyListener.anInt618);
			byte[] is
			    = Class39_Sub5_Sub16.aByteArrayArray1989[i_34_];
			if (is == null && Class65.anInt1147 < 800)
			    ArchiveWorker.method1116(i_35_, (byte) -17, i_36_, 64,
					       64);
		    }
		    Class53.method985(true, false);
		    for (int i_37_ = 0; i_30_ > i_37_; i_37_++) {
			byte[] is = Cache.aByteArrayArray104[i_37_];
			if (is != null) {
			    int i_38_ = (-Class65.anInt1145
					 + (Class65.anIntArray1132[i_37_]
					    >> 8) * 64);
			    int i_39_ = (-JKeyListener.anInt618
					 + (Class65.anIntArray1132[i_37_]
					    & 0xff) * 64);
			    Class39_Sub5_Sub4_Sub4.method516
				(Class44.aClass38_836, (byte) -93, i_38_,
				 Class39_Sub5_Sub12.aClass27Array1857, is,
				 i_39_);
			}
		    }
		}
		if (Class63.aBoolean1120) {
		    for (int i_40_ = 0; i_40_ < 4; i_40_++) {
			for (int i_41_ = 0; i_41_ < 13; i_41_++) {
			    for (int i_42_ = 0; i_42_ < 13; i_42_++) {
				boolean bool_43_ = false;
				int i_44_ = (Class39_Sub5_Sub6
					     .anIntArrayArrayArray1755[i_40_]
					     [i_41_][i_42_]);
				if (i_44_ != -1) {
				    int i_45_ = (i_44_ & 0x3391a34) >> 24;
				    int i_46_ = (i_44_ & 0xffcc82) >> 14;
				    int i_47_ = i_44_ >> 1 & 0x3;
				    int i_48_ = (i_44_ & 0x3ff9) >> 3;
				    int i_49_ = (i_46_ / 8 << 8) + i_48_ / 8;
				    for (int i_50_ = 0;
					 i_50_ < Class65.anIntArray1132.length;
					 i_50_++) {
					if ((i_49_
					     == Class65.anIntArray1132[i_50_])
					    && ((Class39_Sub5_Sub16
						 .aByteArrayArray1989[i_50_])
						!= null)) {
					    bool_43_ = true;
					    Class14.method209
						(-127, (i_46_ & 0x7) * 8,
						 i_40_,
						 (Class39_Sub5_Sub16
						  .aByteArrayArray1989[i_50_]),
						 (Class39_Sub5_Sub12
						  .aClass27Array1857),
						 i_47_, i_41_ * 8,
						 (i_48_ & 0x7) * 8, i_45_,
						 i_42_ * 8);
					    break;
					}
				    }
				}
				if (!bool_43_)
				    Bzip2Block.method1033(i_40_, i_42_ * 8, true,
						       i_41_ * 8);
			    }
			}
		    }
		    for (int i_51_ = 0; i_51_ < 13; i_51_++) {
			for (int i_52_ = 0; i_52_ < 13; i_52_++) {
			    int i_53_
				= (Class39_Sub5_Sub6.anIntArrayArrayArray1755
				   [0][i_51_][i_52_]);
			    if (i_53_ == -1)
				ArchiveWorker.method1116(i_51_ * 8, (byte) -17,
						   i_52_ * 8, 8, 8);
			}
		    }
		    Class53.method985(true, false);
		    for (int i_54_ = 0; i_54_ < 4; i_54_++) {
			for (int i_55_ = 0; i_55_ < 13; i_55_++) {
			    for (int i_56_ = 0; i_56_ < 13; i_56_++) {
				int i_57_ = (Class39_Sub5_Sub6
					     .anIntArrayArrayArray1755[i_54_]
					     [i_55_][i_56_]);
				if (i_57_ != -1) {
				    int i_58_ = (i_57_ & 0x3994c9a) >> 24;
				    int i_59_ = i_57_ >> 1 & 0x3;
				    int i_60_ = (i_57_ & 0x3fff) >> 3;
				    int i_61_ = (i_57_ & 0xffc3f4) >> 14;
				    int i_62_ = (i_61_ / 8 << 8) + i_60_ / 8;
				    for (int i_63_ = 0;
					 i_63_ < Class65.anIntArray1132.length;
					 i_63_++) {
					if ((i_62_
					     == Class65.anIntArray1132[i_63_])
					    && (Cache.aByteArrayArray104
						[i_63_]) != null) {
					    Queue.method993
						((Cache.aByteArrayArray104
						  [i_63_]),
						 i_59_, i_54_, true, i_55_ * 8,
						 i_58_, Class44.aClass38_836,
						 (i_61_ & 0x7) * 8,
						 (i_60_ & 0x7) * 8, i_56_ * 8,
						 (Class39_Sub5_Sub12
						  .aClass27Array1857));
					    break;
					}
				    }
				}
			    }
			}
		    }
		}
		Class53.method985(true, false);
		Class62.method1045(false);
		ChristmasEventSnow.buildOutdoorGroundSnow(Class44.aClass38_836);
		ClientScript.method479(-128, Class44.aClass38_836,
					    (Class39_Sub5_Sub12
					     .aClass27Array1857));
		Class53.method985(true, false);
		int i_64_ = TraversalMap.anInt518;
		if (i_64_ > NameTable.height)
		    i_64_ = NameTable.height;
		if (NameTable.height - 1 > i_64_)
		    i_64_ = NameTable.height - 1;
		if (!Class45.aBoolean867)
		    Class44.aClass38_836.method390(0);
		else
		    Class44.aClass38_836.method390(TraversalMap.anInt518);
		for (int i_65_ = 0; i_65_ < 104; i_65_++) {
		    for (int i_66_ = 0; i_66_ < 104; i_66_++)
			Class65.updateGroundItems(i_65_, i_66_);
		}
		Class33.method331(-1);
		Class43.aClass7_811.method134(27392);
		if (Class10.frame != null) {
		    FrameBuffer.outgoingGameBuffer
			.putFrame(141);
		    FrameBuffer.outgoingGameBuffer
			.putDword(1057001181);
		}
		if (!Class63.aBoolean1120) {
		    int i_67_ = (anInt1597 - 6) / 8;
		    int i_68_ = (anInt1597 + 6) / 8;
		    int i_69_ = (Class65.anInt1147 + 6) / 8;
		    int i_70_ = (Class65.anInt1147 - 6) / 8;
		    for (int i_71_ = i_67_ - 1; i_71_ <= i_68_ + 1; i_71_++) {
			for (int i_72_ = i_70_ - 1; i_69_ + 1 >= i_72_;
			     i_72_++) {
			    if (i_67_ > i_71_ || i_68_ < i_71_ || i_72_ < i_70_
				|| i_72_ > i_69_) {
				JSocket.fileLoader5.method144
				    ((Class39_Sub5_Sub11.method708
				      ((new JString[]
					{ ClientApplet.aClass3_4,
					  AbstractImage.method1007((byte) 71, i_71_),
					  Class32.aClass3_593,
					  AbstractImage.method1007((byte) 71,
							     i_72_) }))),
				     (byte) 60);
				JSocket.fileLoader5.method144
				    ((Class39_Sub5_Sub11.method708
				      ((new JString[]
					{ ObjectDefinition.aClass3_1946,
					  AbstractImage.method1007((byte) 71, i_71_),
					  Class32.aClass3_593,
					  AbstractImage.method1007((byte) 71,
							     i_72_) }))),
				     (byte) 60);
			    }
			}
		    }
		}
		if (SubNode.anInt1348 != -1)
		    Class39_Sub14.setState(35);
		else
		    Class39_Sub14.setState(30);
		Class39_Sub5_Sub16.method747(-1083);
		FrameBuffer.outgoingGameBuffer.putFrame(21);
		Class41.method893(31603);
	    }
	}
    }
    
    public void method1054(int i) {
	if (aClass56_1602 != null && i != aClass56_1602.returnCode) {
	    if (aClass56_1602.returnCode == 1) {
		FileOnDisk class8 = (FileOnDisk) aClass56_1602.returnObject;
		try {
		    class8.write(aByteArray1608, 0, aByteArray1608.length);
		    class8.destroy();
		    try {
			/*Class64.method1091(("midibox.loop="
					    + (!aBoolean1604 ? "0"
					       : "\"infinite\"")
					    + "; midibox.src=\""
					    + class8.method138(true).getPath
						  ().replace('\\', '/')
					    + "\"; midibox.volume=" + anInt1610
					    + ";"),
					   19048, aClass21_1600.anApplet416);*/
			aBoolean1599 = true;
		    } catch (Throwable throwable) {
			/* empty */
		    }
		} catch (Exception exception) {
		    try {
			class8.destroy();
		    } catch (Exception exception_73_) {
			/* empty */
		    }
		}
	    }
	    aClass56_1602 = null;
	}
    }
    
    public void method1051(int i, byte[] is, byte i_74_, boolean bool) {
	if (i_74_ <= 73)
	    aClass56_1602 = null;
	aClass56_1602 = aClass21_1600.method260((byte) -86);
	if (aClass56_1602 != null) {
	    if (i == 0)
		i = 1;
	    anInt1610 = Class39_Sub5_Sub4_Sub4.method512(929, i);
	    aByteArray1608 = is;
	    aBoolean1604 = bool;
	}
    }
    
    public static JString method1083(int i, byte i_75_) {
	JString class3 = AbstractImage.method1007((byte) 71, i);
	for (int i_76_ = class3.getLength() - 3; i_76_ > 0; i_76_ -= 3)
	    class3 = Class39_Sub5_Sub11.method708((new JString[]
						   { class3.method59(0, -1,
								     i_76_),
						     Queue.aClass3_989,
						     class3.method85(-58,
								     i_76_) }));
	if (class3.getLength() > 8)
	    class3 = (Class39_Sub5_Sub11.method708
		      ((new JString[]
			{ Class41.aClass3_783,
			  class3.method59(0, -1, class3.getLength() - 8),
			  Class39_Sub11.aClass3_1468, Cache.aClass3_103,
			  class3, Queue.aClass3_984 })));
	else if (class3.getLength() > 4)
	    class3
		= (Class39_Sub5_Sub11.method708
		   (new JString[] { JString.aClass3_1237,
				   class3.method59(0, -1,
						   class3.getLength() - 4),
				   Class37.aClass3_660, Cache.aClass3_103,
				   class3, Queue.aClass3_984 }));
	return Class39_Sub5_Sub11
		   .method708(new JString[] { aClass3_1605, class3 });
    }
    
    public Class62_Sub2(Signlink class21) {
	aClass21_1600 = class21;
    }
    
    public static void method1084(byte i, int i_77_) {
	Class68.method1112(i_77_, (byte) 110);
    }
    
    static {
	anInt1596 = 0;
	aClass3_1603 = Class39_Sub5_Sub9.createJstring("blinken1:");
	aClass3_1605 = Class39_Sub5_Sub9.createJstring(" ");
	aClass3_1606 = Class39_Sub5_Sub9.createJstring("(X100(U(Y");
	aClass3_1609
	    = Class39_Sub5_Sub9
		  .createJstring("RuneScape is loading )2 please wait)3)3)3");
	aClass3_1601 = aClass3_1609;
    }
}
