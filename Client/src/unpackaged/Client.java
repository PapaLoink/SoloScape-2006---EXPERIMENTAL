package unpackaged;

/* client - Decompiled by JODE
 * Visit http://jode.sourceforge.net/
 */

import jagex.graphics.BitmapFont;
import jagex.io.BufferedFile;
import jagex.graphics.DrawingArea;
import jagex.utils.HashTable;
import jagex.utils.Huffmans;
import jagex.world.actors.GroundItem;
import jagex.graphics.JImage;
import jagex.graphics.AbstractImage;
import jagex.world.actors.StillGraphic;
import jagex.world.actors.Projectile;
import jagex.utils.SubNode;
import jagex.utils.Node;
import jagex.utils.IsaacPrng;
import jagex.utils.JString;
import jagex.io.JSocket;
import jagex.world.actors.Npc;
import jagex.world.actors.Player;
import jagex.world.map.TraversalMap;
import jagex.utils.Queue;
import jagex.utils.Deque;
import jagex.io.FrameBuffer;
import jagex.io.Buffer;
import jagex.utils.Cache;
import jagex.audio.Filter;
import jagex.audio.Instrument;
import java.net.InetAddress;
import java.net.Socket;

public class Client extends ClientApplet {

    public static long aLong1263;
    public static JString aClass3_1264;
    public static JString aClass3_1265 = Class39_Sub5_Sub9.createJstring("Loaded gamescreen");
    public static JString aClass3_1266 = Class39_Sub5_Sub9.createJstring("level)2");
    public static AbstractImage aClass57_1267;
    public static int[] anIntArray1268;
    public static int anInt1269;
    public static JString aClass3_1270;
    public static JString aClass3_1271 = Class39_Sub5_Sub9.createJstring("Loading config )2 ");
    public static JString aClass3_1272 = Class39_Sub5_Sub9.createJstring("No reply from loginserver)3");
    public static JString aClass3_1273;
    public static int anInt1274 = 0;
    public static int[] incomingSizes;
    public static int[] anIntArray1276;
    public static JString aClass3_1277;
    public static long aLong1278;
    public static JString aClass3_1279;
    public static int anInt1280;

    public static void method34(int i) {
        aClass3_1271 = null;
        aClass3_1279 = null;
        if (i == 111) {
            aClass3_1264 = null;
            aClass3_1277 = null;
            aClass3_1272 = null;
            aClass3_1266 = null;
            aClass3_1270 = null;
            anIntArray1276 = null;
            anIntArray1268 = null;
            aClass3_1273 = null;
            aClass57_1267 = null;
            aClass3_1265 = null;
            incomingSizes = null;
        }
    }

    public void handleOdError(int opcode) {
        if (Class39_Sub5_Sub4.anInt1732 == HashTable.anInt363) {
            HashTable.anInt363 = Bzip2Block.anInt1078;
        } else {
            HashTable.anInt363 = Class39_Sub5_Sub4.anInt1732;
        }
        ObjectDefinition.odStage = 0;
        Projectile.anInt2192++;
        ArchiveWorker.odSocket = null;
        Class20.aClass56_390 = null;
        if (Projectile.anInt2192 < 2 || opcode != 7 && opcode != 9) {
            if (Projectile.anInt2192 < 2 || opcode != 6) {
                if (Projectile.anInt2192 >= 4) {
                    if (Class31.state > 5) {
                        Class65.odConnectionDelay = 3000;
                    } else {
                        this.displayErrorPage("js5connect");
                        Class31.state = 1000;
                    }
                }
            } else {
                this.displayErrorPage("js5connect_outofdate");
                Class31.state = 1000;
            }
        } else if (Class31.state > 5) {
            Class65.odConnectionDelay = 3000;
        } else {
            this.displayErrorPage("js5connect_full");
            Class31.state = 1000;
        }
    }

    public static boolean decodeBitmapFont(FileTable fileTable, int archiveId, int childId) {
        byte[] is = fileTable.lookupFile(archiveId, childId);
        if (is == null) {
            return false;
        }
        Class39_Sub7.decodeBitmapFont(is);
        return true;
    }

    public void method32(boolean bool) {
        if (Class39_Sub7.aBoolean1373) {
            Class39_Sub5_Sub16.method746((byte) 52, Class41.aCanvas778);
            Class53.method982(-15828, Class41.aCanvas778);
            if (Class43.aClass34_814 != null) {
                Class43.aClass34_814.method332(99, Class41.aCanvas778);
            }
            this.method28(64);
            Class13.method188((byte) -70, Class41.aCanvas778);
            ClientScript.method478(Class41.aCanvas778, 13331);
            if (Class43.aClass34_814 != null) {
                Class43.aClass34_814.method337(Class41.aCanvas778, (byte) 114);
            }
        }
        if (bool == true) {
            if (Class31.state != 0) {
                if (Class31.state != 5) {
                    if (Class31.state != 10) {
                        if (Class31.state != 20) {
                            if (Class31.state != 25) {
                                if (Class31.state == 30) {
                                    Buffer.method823(0);
                                } else if (Class31.state != 35) {
                                    if (Class31.state == 40) {
                                        Class26.method294(false, Queue.aClass3_983, Class63.aClass3_1118, -94);
                                    }
                                } else {
                                    ArchiveRequest.method858((byte) -127);
                                }
                            } else if (Class1.anInt33 != 1) {
                                if (Class1.anInt33 != 2) {
                                    Class26.method294(false, NameTable.aClass3_190, null, -109);
                                } else {
                                    if (Class39_Sub5_Sub11.anInt1843 < RuntimeException_Sub1.anInt1222) {
                                        Class39_Sub5_Sub11.anInt1843 = RuntimeException_Sub1.anInt1222;
                                    }
                                    int i = (50 + ((-RuntimeException_Sub1.anInt1222 + Class39_Sub5_Sub11.anInt1843) * 50 / Class39_Sub5_Sub11.anInt1843));
                                    Class26.method294(true, NameTable.aClass3_190, (Class39_Sub5_Sub11.method708((new JString[]{(Class39_Sub5_Sub4_Sub4.aClass3_2319), AbstractImage.method1007((byte) 71, i), Buffer.aClass3_1360}))), 75);
                                }
                            } else {
                                if (Class66.anInt1157 < Varbit.anInt1798) {
                                    Class66.anInt1157 = Varbit.anInt1798;
                                }
                                int i = ((-Varbit.anInt1798 + Class66.anInt1157) * 50 / Class66.anInt1157);
                                Class26.method294(true, NameTable.aClass3_190, (Class39_Sub5_Sub11.method708((new JString[]{Class39_Sub5_Sub4_Sub4.aClass3_2319, AbstractImage.method1007((byte) 71, i), Buffer.aClass3_1360}))), -122);
                            }
                        } else {
                            Class39_Sub5_Sub12.method709((Npc.aClass39_Sub5_Sub10_Sub1_2495), -27620, Class32.aClass39_Sub5_Sub10_Sub1_587);
                        }
                    } else {
                        Class39_Sub5_Sub12.method709((Npc.aClass39_Sub5_Sub10_Sub1_2495), -27620, Class32.aClass39_Sub5_Sub10_Sub1_587);
                    }
                } else {
                    Class39_Sub5_Sub12.method709((Npc.aClass39_Sub5_Sub10_Sub1_2495), -27620, Class32.aClass39_Sub5_Sub10_Sub1_587);
                }
            } else {
                Class34.method340(null, 28366, Class39_Sub5_Sub14.aClass3_1918, Class39_Sub7.anInt1387);
            }
            Class46_Sub1.anInt1547 = 0;
            Class55.anInt1252 = 0;
        }
    }

    public void method31(int i) {
        method34(111);
        JString.method64((byte) 97);
        ClientApplet.method21((byte) -60);
        Class46.method932(-84);
        AbstractImage.method1008(-90);
        Class31.method321(3);
        Buffer.method782((byte) -124);
        JSocket.method216(-1);
        FileLoader.method176((byte) 105);
        BufferedFile.method232(-1);
        CacheIO.method129(true);
        Npc.method523(124);
        FrameBuffer.method839(false);
        BitmapFont.method643();
        Class38.method402();
        TraversalMap.method303(17448960);
        Player.method529(false);
        Deque.method968((byte) 92);
        Widget.method765((byte) -58);
        Class34.method334(i - 502);
        Class43.method904(i ^ ~0x1f7);
        Class45.method920(0);
        Class39_Sub5_Sub4_Sub4.method507(-29);
        Class39_Sub11.method868(28549);
        NpcDefinition.method717((byte) 102);
        HashTable.method243(-126);
        Node.method409(false);
        IsaacPrng.method1041(false);
        Class14.method213(-31054);
        Cache.method136(92);
        Class39_Sub5_Sub11.method704((byte) -58);
        Model.method550();
        Class53.method983((byte) 112);
        RuntimeException_Sub1.method1122((byte) 78);
        Class68.method1114(15);
        Class39_Sub14.method880(i - 498);
        Class10.method181(i ^ ~0x1af);
        Class26.method289(127);
        Class39_Sub5_Sub4.method491(-3157);
        Class36.method348((byte) -45);
        Class44.method915(false);
        Class50.method973(2);
        Class32.method324(239);
        Class17.method223();
        JKeyListener.method344(-13461);
        JMouseListener.method903(-6088);
        Class2.method53(0);
        SubNode.method462(i ^ i);
        FileTable.method150(29320);
        ArchiveWorker.method1117(41);
        Class41.method897(106);
        Class13.method192(i ^ ~0x188);
        Class65.method1093(114);
        Class20.method247((byte) 114);
        Class37.method350(103);
        Queue.method995((byte) -52);
        OndemandRequest.method485(-54);
        Class66.method1102((byte) 126);
        Class39_Sub5_Sub10_Sub2.method652();
        DrawingArea.destroy();
        NameTable.method183(118);
        Class23.method272(i - 504);
        Class63.method1088(i ^ 0x1f7);
        Class39_Sub5_Sub14.method729(102);
        Class39_Sub5_Sub9.method600(i ^ ~0x6328);
        Class39_Sub5_Sub6.method578(false);
        ObjectDefinition.method738(65535);
        ItemDefinition.method472(-30765);
        Class39_Sub5_Sub12.method711(18051);
        Class39_Sub5_Sub18.method780(true);
        Varbit.method598((byte) -81);
        Class39_Sub5_Sub16.method749(0);
        Class55.method998((byte) 126);
        Class39_Sub8.method856();
        Huffmans.method890(true);
        Class30.method316(0);
        Class47.method945(false);
        Class33.method329(28375);
        Class39_Sub13.method874((byte) -79);
        Class39_Sub10.method865(false);
        ClientScript.method482(24311);
        Class15.method215((byte) 117);
        Instrument.method1028();
        Class48.method950((byte) -121);
        Projectile.method493(-30);
        StillGraphic.method531((byte) 121);
        Class12.method186(83);
        ScriptState.method280(false);
        GroundItem.method501((byte) -6);
        Class39_Sub5_Sub4_Sub2.method499(68);
        Class39_Sub4.method458(-95);
        Class4.method100((byte) 121);
        Canvas_Sub1.method42(296);
        Class46_Sub1.method943(false);
        JImage.method1020(true);
        Class67.method1108((byte) -127);
        Filter.method1024();
        Class25.method286(i ^ ~0x1fbf);
        Class51.method977();
        Class39_Sub12.method871(15003);
        Bzip2Decompressor.method266();
        Bzip2Block.method1031(83);
        ArchiveRequest.method859(false);
        Class62.method1058(-43);
        Class39_Sub5_Sub7.method587(-57);
        Class13_Sub1.method198();
        Class13_Sub1_Sub1.method205();
        Class1.method47(29053);
        Class39_Sub7.method847((byte) -87);
        Class39_Sub5_Sub5.method571(1000);
        Class62_Sub1.method1067(-109);
        Class5.method112();
        Class62_Sub2.method1073(i ^ ~0x493691e0);
    }

    public void method22(int i) {
        DeveloperToolsServer.stop();
        if (Cache.aClass31_123 != null) {
            Cache.aClass31_123.aBoolean554 = false;
        }
        Cache.aClass31_123 = null;
        if (Class37.gameSocket != null) {
            Class37.gameSocket.stop();
            Class37.gameSocket = null;
        }
        Class4.method101((byte) -88);
        Huffmans.method888(i - 1886);
        Class43.aClass34_814 = null;
        Class41.method896(-119);
        Class41.method898(0);
        Npc.method525((byte) -127);
        Class39_Sub5_Sub4_Sub4.method517((byte) -125);
        do {
            try {
                if (Class43.mainFile != null) {
                    Class43.mainFile.method226((byte) -80);
                }
                if (Npc.indexFiles != null) {
                    for (int i_3_ = 0; (Npc.indexFiles.length > i_3_); i_3_++) {
                        if (Npc.indexFiles[i_3_] != null) {
                            Npc.indexFiles[i_3_].method226((byte) -125);
                        }
                    }
                }
                if (i != 4258) {
                    method38(126);
                }
                if (Class25.tableFile == null) {
                    break;
                }
                Class25.tableFile.method226((byte) -72);
            } catch (java.io.IOException ioexception) {
                break;
            }
            break;
        } while (false);
    }

    public void method37(int i) {
        if (i != 17151) {
            aClass3_1265 = null;
        }
        if (Class20.anInt393 >= 4) {
            this.displayErrorPage("js5crc");
            Class31.state = 1000;
        } else {
            if (Class65.odErrors >= 4) {
                if (Class31.state <= 5) {
                    this.displayErrorPage("js5io");
                    Class31.state = 1000;
                    return;
                }
                Class65.odConnectionDelay = 3000;
                Class65.odErrors = 3;
            }
            if (Class65.odConnectionDelay-- <= 0) {
                do {
                    try {
                        if (ObjectDefinition.odStage == 0) {
                            Class20.aClass56_390 = Class39_Sub5_Sub9.signlink.requestSocket(HashTable.anInt363);
                            ObjectDefinition.odStage++;
                        }
                        if (ObjectDefinition.odStage == 1) {
                            if (Class20.aClass56_390.returnCode == 2) {
                                handleOdError(-1);
                                break;
                            }
                            if (Class20.aClass56_390.returnCode == 1) {
                                ObjectDefinition.odStage++;
                            }
                        }
                        if (ObjectDefinition.odStage == 2) {
                            ArchiveWorker.odSocket = new JSocket((Socket) (Class20.aClass56_390.returnObject), Class39_Sub5_Sub9.signlink);
                            Buffer buffer = new Buffer(5);
                            buffer.putByte(15);
                            buffer.putDword(443);
                            ArchiveWorker.odSocket.write(buffer.payload, 0, 5);
                            ObjectDefinition.odStage++;
                            OndemandRequest.wroteGatewayTime = Class2.getSystemTime();
                        }
                        if (ObjectDefinition.odStage == 3) {
                            if (Class31.state > 5 && ArchiveWorker.odSocket.available() <= 0) {
                                if ((-OndemandRequest.wroteGatewayTime + Class2.getSystemTime()) > 30000L) {
                                    handleOdError(-2);
                                    break;
                                }
                            } else {
                                int response = ArchiveWorker.odSocket.read();
                                if (response != 0) {
                                    handleOdError(response);
                                    break;
                                }
                                ObjectDefinition.odStage++;
                            }
                        }
                        if (ObjectDefinition.odStage != 4) {
                            break;
                        }
                        GroundItem.writeOdConnect(ArchiveWorker.odSocket, Class31.state > 20);
                        Projectile.anInt2192 = 0;
                        ObjectDefinition.odStage = 0;
                        Class20.aClass56_390 = null;
                        ArchiveWorker.odSocket = null;
                    } catch (java.io.IOException ioexception) {
                        handleOdError(-3);
                        break;
                    }
                    break;
                } while (false);
            }
        }
    }

    public void method38(int i) {
        if (i != 0) {
            aLong1263 = 110L;
        }
        if (Class31.state != 1000) {
            boolean bool = JKeyListener.method343(512);
            if (!bool) {
                method37(17151);
            }
        }
    }

    public void init() {
        if (this.method29(1)) {
            BufferedFile.worldId = Integer.parseInt(this.getParameter("worldid"));
            FileTable.anInt133 = Integer.parseInt(this.getParameter("modewhat"));
            Class39_Sub5_Sub6.mode = Integer.parseInt(this.getParameter("modewhere"));
            String string = this.getParameter("lowmem");
            if (string == null || !string.equals("1")) {
                Deque.method964(1);
            } else {
                Npc.method524((byte) -125);
            }
            String string_5_ = this.getParameter("members");
            if (string_5_ == null || !string_5_.equals("1")) {
                HashTable.isMembers = false;
            } else {
                HashTable.isMembers = true;
            }
            String string_6_ = this.getParameter("lang");
            if (string_6_ != null && string_6_.equals("1")) {
                Cache.method132(-8);
                HashTable.languageId = 1;
            }
            this.method17(48, 503, FileTable.anInt133 + 32, 443, 765);
        }
    }

    public void method33(byte i) {
        DeveloperToolsClient.pump();
        ModHook.pump();
        Class2.logicCycle++;
        method38(0);
        StillGraphic.method534(32257);
        if (i != 19) {
            decodeBitmapFont(null,-99, 61);
        }
        Class41.method895((byte) -93);
        Class39_Sub5_Sub5.method573(6);
        Varbit.method595(3116);
        Class39_Sub5_Sub18.method776((byte) -99);
        if (Class43.aClass34_814 != null) {
            int i_7_ = Class43.aClass34_814.method338(i + 107);
            Class62.anInt1107 = i_7_;
            Class55.anInt1252 += i_7_;
        }
        if (Class31.state != 0) {
            if (Class31.state != 5) {
                if (Class31.state != 10) {
                    if (Class31.state == 20) {
                        FileTable.method156((byte) 125);
                        Class25.method285(-10);
                    } else if (Class31.state == 25) {
                        Class62_Sub2.method1082((byte) -125);
                    }
                } else {
                    FileTable.method156((byte) 121);
                }
            } else {
                Class39_Sub12.method870((byte) 113);
                ScriptState.method275(-33);
            }
        } else {
            Class39_Sub12.method870((byte) -87);
            ScriptState.method275(-33);
        }
        if (Class31.state == 30) {
            FpsDebugOverlay.tick();
            Class34.method336(-72);
        } else if (Class31.state == 35) {
            FpsDebugOverlay.tick();
            Class34.method336(-91);
        } else if (Class31.state == 40) {
            Class25.method285(-10);
        }
    }

    public static void main(String[] strings) {
        try {
            System.setProperty("sun.awt.noerasebackground", "true");   // GPU drawing to the window: no grey flash between frames
            WebClientBridge.startFromProperties();
            DeveloperToolsServer.startFromProperties();
            if (strings.length != 6) {
                JString.printArgsUsage();
            }
            BufferedFile.worldId = Integer.parseInt(strings[0]);
            if (strings[1].equals("live")) {
                Class39_Sub5_Sub6.mode = 0;
            } else if (!strings[1].equals("office")) {
                if (strings[1].equals("local")) {
                    Class39_Sub5_Sub6.mode = 2;
                } else {
                    JString.printArgsUsage();
                }
            } else {
                Class39_Sub5_Sub6.mode = 1;
            }
            if (strings[2].equals("live")) {
                FileTable.anInt133 = 0;
            } else if (strings[2].equals("rc")) {
                FileTable.anInt133 = 1;
            } else if (!strings[2].equals("wip")) {
                JString.printArgsUsage();
            } else {
                FileTable.anInt133 = 2;
            }
            if (strings[3].equals("lowmem")) {
                Npc.method524((byte) -126);
            } else if (strings[3].equals("highmem")) {
                Deque.method964(1);
            } else {
                JString.printArgsUsage();
            }
            if (strings[4].equals("free")) {
                HashTable.isMembers = false;
            } else if (strings[4].equals("members")) {
                HashTable.isMembers = true;
            } else {
                JString.printArgsUsage();
            }
            if (strings[5].equals("english")) {
                HashTable.languageId = 0;
            } else if (!strings[5].equals("german")) {
                JString.printArgsUsage();
            } else {
                Cache.method132(80);
                HashTable.languageId = 1;
            }
            Client var_client = new Client();
            String serverHost = System.getProperty("client.host", "127.0.0.1");
            var_client.method23(443, InetAddress.getByName(serverHost), "runescape", 765, 503, 14, -97, FileTable.anInt133 + 32);
        } catch (Exception exception) {
            Class39_Sub7.method849(exception, 64, null);
        }
    }

    public void method20(boolean bool) {
        if (!bool) {
            int serverPort = Integer.getInteger("client.port", -1);
            if (serverPort > 0) {
                Class39_Sub5_Sub4.anInt1732 = serverPort;
                Bzip2Block.anInt1078 = serverPort;
            } else {
                Class39_Sub5_Sub4.anInt1732 = (Class39_Sub5_Sub6.mode == 0 ? 43594 : BufferedFile.worldId + 40000);
                Bzip2Block.anInt1078 = (Class39_Sub5_Sub6.mode != 0 ? BufferedFile.worldId + 50000 : 443);
            }
            HashTable.anInt363 = Class39_Sub5_Sub4.anInt1732;
            Huffmans.method882(116);
            Class13.method188((byte) -95, Class41.aCanvas778);
            ClientScript.method478(Class41.aCanvas778, 13331);
            Class43.aClass34_814 = Projectile.method492(true);
            if (Class43.aClass34_814 != null) {
                Class43.aClass34_814.method337(Class41.aCanvas778, (byte) 87);
            }
            Class37.anInt656 = Signlink.anInt415;
            try {
                if (Class39_Sub5_Sub9.signlink.mainArchive != null) {
                    Class43.mainFile = new BufferedFile((Class39_Sub5_Sub9.signlink.mainArchive), 5200, 0);
                    for (int i = 0; i < 14; i++) {
                        Npc.indexFiles[i] = new BufferedFile((Class39_Sub5_Sub9.signlink.cacheArchives[i]), 6000, 0);
                    }
                    Class25.tableFile = new BufferedFile(Class39_Sub5_Sub9.signlink.tableArchive, 6000, 0);
                    Class14.tableCache = new CacheIO(255, Class43.mainFile, Class25.tableFile, 500000);
                    Class39_Sub5_Sub9.signlink.mainArchive = null;
                    Class39_Sub5_Sub9.signlink.cacheArchives = null;
                    Class39_Sub5_Sub9.signlink.tableArchive = null;
                }
            } catch (java.io.IOException ioexception) {
                Class43.mainFile = null;
                Class25.tableFile = null;
                Class14.tableCache = null;
            }
            OndemandRequest.aBoolean1718 = false;
            Class65.aClass39_Sub5_Sub17_1136 = new Widget();
        }
    }

    static {
        anIntArray1268 = new int[100];
        anInt1269 = 0;
        anIntArray1276 = new int[]{768, 1024, 1280, 512, 1536, 256, 0, 1792};
        aClass3_1273 = null;
        incomingSizes = (new int[]{0, 0, 0, 6, 0, 0, 2, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 10, 0, 6, -1, 0, 0, 0, -2, 0, 4, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 6, 0, 0, 0, 3, 0, 0, 0, 0, 0, 0, 2, 0, 0, 2, 8, 6, 0, 0, -1, 0, 7, 0, 5, 2, 0, 3, 0, 0, 1, 6, 0, 3, 3, 0, 0, 7, 6, 6, 2, 0, 0, 0, 15, 0, 0, 0, 0, 0, 0, 0, 5, 6, 6, 0, 0, 0, 6, 0, 2, 0, 0, 0, -2, 4, 0, 0, 0, 0, 0, 0, 14, -1, 0, 0, 2, 0, 0, 0, 10, 0, 0, 2, 0, 0, 0, 0, 0, 4, 10, 1, 2, 0, 0, 0, 0, 4, 5, 4, -1, 0, -2, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 4, 0, 0, 0, 0, 0, 0, 0, 0, 10, -2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, -2, -2, 0, 0, 0, 0, 0, 3, 0, 0, 0, 0, -2, 2, 0, 5, 0, 0, 0, 0, 0, -2, 0, 0, 0, 0, 0, 2, 0, 4, 0, 0, 0, 0, 1, 6, -2, 0, 2, 0, 8, 0, 5, 0, 1, 0, -2, 0, 0, 6, 0, 0, -2, 4, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0});
        aClass3_1270 = aClass3_1266;
        aClass3_1277 = aClass3_1271;
        aClass3_1264 = aClass3_1272;
        aClass3_1279 = aClass3_1265;
    }
}
