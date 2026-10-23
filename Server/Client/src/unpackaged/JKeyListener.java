package unpackaged;

/* Class35 - Decompiled by JODE
 * Visit http://jode.sourceforge.net/
 */

import jagex.io.BufferedFile;
import jagex.utils.HashTable;
import jagex.utils.Huffmans;
import jagex.graphics.JImage;
import jagex.graphics.sprites.IndexedColorSprite;
import jagex.utils.JString;
import jagex.io.JSocket;
import jagex.world.actors.Npc;
import jagex.world.actors.Player;
import jagex.utils.Deque;
import jagex.io.Buffer;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.IOException;

public class JKeyListener implements KeyListener, FocusListener {

    public static JString aClass3_616;
    public static JString aClass3_617 = Class39_Sub5_Sub9.createJstring("Please subscribe)1 or use a different world)3");
    public static int anInt618;
    public static JString aClass3_619;
    public static JString aClass3_620;
    public static int[] anIntArray621;
    public static JString aClass3_622;
    public static JString aClass3_623;
    public static JString aClass3_624;
    public static JString aClass3_625;
    public static long aLong626;
    public static JString aClass3_627;
    public static boolean aBoolean628;
    public static IndexedColorSprite aClass39_Sub5_Sub10_Sub4_629;

    public synchronized void keyPressed(KeyEvent keyevent) {
        KeyEvent filtered = ModHook.keyPressed(keyevent);
        if (filtered == null) {
            keyevent.consume();
            return;
        }
        keyevent = filtered;
        if (Npc.aClass35_2499 != null) {
            Class39_Sub5_Sub7.anInt1779 = 0;
            int i = keyevent.getKeyCode();
            if (i < 0
                    || i >= Player.anIntArray2532.length) {
                i = -1;
            } else {
                i = Player.anIntArray2532[i];
                if ((i & 0x80) != 0) {
                    i = -1;
                }
            }
            int i_0_;
            if (i != 85 && i != 80 && i != 84 && i != 0 && i != 101) {
                i_0_ = Class39_Sub5_Sub16.method744(keyevent, 8);
            } else {
                i_0_ = -1;
            }
            if (BufferedFile.anInt341 >= 0 && i >= 0) {
                Class39_Sub5_Sub11.anIntArray1847[BufferedFile.anInt341] = i;
                BufferedFile.anInt341 = BufferedFile.anInt341 + 1 & 0x7f;
                if (BufferedFile.anInt341 == Class46.anInt879) {
                    BufferedFile.anInt341 = -1;
                }
            }
            if (i >= 0 || i_0_ >= 0) {
                int i_1_ = Class39_Sub5_Sub4_Sub4.anInt2299 + 1 & 0x7f;
                if (i_1_ != Buffer.anInt1368) {
                    Class46.anIntArray883[Class39_Sub5_Sub4_Sub4.anInt2299] = i;
                    Class46_Sub1.anIntArray1558[(Class39_Sub5_Sub4_Sub4.anInt2299)] = i_0_;
                    Class39_Sub5_Sub4_Sub4.anInt2299 = i_1_;
                }
            }
        }
        keyevent.consume();
    }

    public static boolean method343(int i) {
        long l = Class2.getSystemTime();
        int i_2_ = (int) (-Bzip2Block.aLong1063 + l);
        Bzip2Block.aLong1063 = l;
        if (i_2_ > 200) {
            i_2_ = 200;
        }
        Huffmans.anInt748 += i_2_;
        if (Canvas_Sub1.pendingRegularRequests == 0 && Class1.pendingPriorityRequests == 0 && Class41.queuedRegularRequests == 0 && Class39_Sub5_Sub6.queuedPriorityRequests == 0) {
            return true;
        }
        if (Deque.odSocket == null) {
            return false;
        }
        try {
            if (Huffmans.anInt748 > 30000) {
                throw new IOException();
            }
            for (/**/; Class1.pendingPriorityRequests < 20; Class1.pendingPriorityRequests++) {
                if (Class39_Sub5_Sub6.queuedPriorityRequests <= 0) {
                    break;
                }
                OndemandRequest request = (OndemandRequest) Class25.priorityRequestsQueue.quickLookup();
                Buffer buffer = new Buffer(4);
                buffer.putByte(1);
                buffer.putTri((int) request.hash);
                Deque.odSocket.write(buffer.payload, 0, 4);
                CacheIO.pendingPriorityRequests.put(request.hash, request);
                Class39_Sub5_Sub6.queuedPriorityRequests--;
            }
            for (/**/; Canvas_Sub1.pendingRegularRequests < 20 && Class41.queuedRegularRequests > 0; Canvas_Sub1.pendingRegularRequests++) {
                OndemandRequest request = ((OndemandRequest) Class32.regularRequestsQueue.peekFirst());
                Buffer buffer = new Buffer(4);
                buffer.putByte(0);
                buffer.putTri((int) request.hash);
                Deque.odSocket.write(buffer.payload, 0, 4);
                request.unlinkQueue();
                HashTable.pendingRegularRequests.put(request.hash, request);
                Class41.queuedRegularRequests--;
            }
            int cycle = 0;
            for (/**/; cycle < 100; cycle++) {
                int avail = Deque.odSocket.available();
                if (avail < 0) {
                    throw new IOException();
                }
                if (avail == 0) {
                    break;
                }
                Huffmans.anInt748 = 0;
                int amountRead = 0;
                if (FileLoader.currentOdRequest != null) {
                    if (Class39_Sub4.odBlockOffset == 0) {
                        amountRead = 1;
                    }
                } else {
                    amountRead = 8;
                }
                if (amountRead > 0) {
                    int amountBytes = amountRead - ClientApplet.odBuffer.offset;
                    if (amountBytes > avail) {
                        amountBytes = avail;
                    }
                    Deque.odSocket.read(ClientApplet.odBuffer.payload, ClientApplet.odBuffer.offset, amountBytes);
                    if (ClientApplet.encryptionKey != 0) {
                        for (int i_7_ = 0; i_7_ < amountBytes; i_7_++) {
                            ClientApplet.odBuffer.payload[ClientApplet.odBuffer.offset + i_7_] = (byte) (Npc.xor((ClientApplet.odBuffer.payload[(ClientApplet.odBuffer.offset) + i_7_]), ClientApplet.encryptionKey));
                        }
                    }
                    ClientApplet.odBuffer.offset += amountBytes;
                    if (amountRead > ClientApplet.odBuffer.offset) {
                        break;
                    }
                    if (FileLoader.currentOdRequest == null) {
                        ClientApplet.odBuffer.offset = 0;
                        int indexId = ClientApplet.odBuffer.getUbyte();
                        int archiveId = ClientApplet.odBuffer.getUword();
                        int flags = ClientApplet.odBuffer.getUbyte();
                        long hash = (long) (archiveId + (indexId << 16));
                        int size = ClientApplet.odBuffer.getDword();
                        OndemandRequest request = ((OndemandRequest) CacheIO.pendingPriorityRequests.fetch(hash));
                        Class41.isCurrentRequestPriority = true;
                        if (request == null) {
                            request = ((OndemandRequest) HashTable.pendingRegularRequests.fetch(hash));
                            Class41.isCurrentRequestPriority = false;
                        }
                        if (request == null) {
                            throw new IOException();
                        }
                        FileLoader.currentOdRequest = request;
                        int i_13_ = flags != 0 ? 9 : 5;
                        JMouseListener.odArchiveBuffer = new Buffer(i_13_ + (size + (FileLoader.currentOdRequest.footerSize)));
                        JMouseListener.odArchiveBuffer.putByte(flags);
                        JMouseListener.odArchiveBuffer.putDword(size);
                        Class39_Sub4.odBlockOffset = 8;
                        ClientApplet.odBuffer.offset = 0;
                    } else if (Class39_Sub4.odBlockOffset == 0) {
                        if (ClientApplet.odBuffer.payload[0] == -1) {
                            ClientApplet.odBuffer.offset = 0;
                            Class39_Sub4.odBlockOffset = 1;
                        } else {
                            FileLoader.currentOdRequest = null;
                        }
                    }
                } else {
                    int length = (-FileLoader.currentOdRequest.footerSize + JMouseListener.odArchiveBuffer.payload.length);
                    int i_15_ = -Class39_Sub4.odBlockOffset + 512;
                    if (length - JMouseListener.odArchiveBuffer.offset < i_15_) {
                        i_15_ = length - JMouseListener.odArchiveBuffer.offset;
                    }
                    if (avail < i_15_) {
                        i_15_ = avail;
                    }
                    Deque.odSocket.read(JMouseListener.odArchiveBuffer.payload, JMouseListener.odArchiveBuffer.offset, i_15_);
                    if (ClientApplet.encryptionKey != 0) {
                        for (int i_16_ = 0; i_15_ > i_16_; i_16_++) {
                            JMouseListener.odArchiveBuffer.payload[JMouseListener.odArchiveBuffer.offset + i_16_] = (byte) (Npc.xor((JMouseListener.odArchiveBuffer.payload[(JMouseListener.odArchiveBuffer.offset) + i_16_]), ClientApplet.encryptionKey));
                        }
                    }
                    JMouseListener.odArchiveBuffer.offset += i_15_;
                    Class39_Sub4.odBlockOffset += i_15_;
                    if (length == JMouseListener.odArchiveBuffer.offset) {
                        if (FileLoader.currentOdRequest.hash == 16711935L) {
                            JImage.updateTableBuffer = JMouseListener.odArchiveBuffer;
                            for (int i_17_ = 0; i_17_ < 256; i_17_++) {
                                FileLoader class9_sub1 = Class33.fileLoaders[i_17_];
                                if (class9_sub1 != null) {
                                    JImage.updateTableBuffer.offset = i_17_ * 4 + 5;
                                    int checksum = JImage.updateTableBuffer.getDword();
                                    class9_sub1.setChecksum(checksum);
                                }
                            }
                        } else {
                            Class39_Sub5_Sub16.crc.reset();
                            Class39_Sub5_Sub16.crc.update(JMouseListener.odArchiveBuffer.payload, 0, length);
                            int checksum = (int) Class39_Sub5_Sub16.crc.getValue();
                            if (FileLoader.currentOdRequest.checksum != checksum) {
                                try {
                                    Deque.odSocket.stop();
                                } catch (Exception exception) {
                                    /* empty */
                                }
                                ClientApplet.encryptionKey = (byte) (int) (Math.random() * 255.0 + 1.0);
                                Deque.odSocket = null;
                                Class20.anInt393++;
                                return false;
                            }
                            Class65.odErrors = 0;
                            Class20.anInt393 = 0;
                            FileLoader.currentOdRequest.fileLoader.method175(JMouseListener.odArchiveBuffer.payload,
                                    Class41.isCurrentRequestPriority,
                                    (FileLoader.currentOdRequest.hash
                                    & 0xff0000L) == 16711680L,
                                    (int) ((FileLoader.currentOdRequest.hash)
                                    & 0xffffL));
                        }
                        FileLoader.currentOdRequest.unlinkDeque();
                        if (Class41.isCurrentRequestPriority) {
                            Class1.pendingPriorityRequests--;
                        } else {
                            Canvas_Sub1.pendingRegularRequests--;
                        }
                        FileLoader.currentOdRequest = null;
                        JMouseListener.odArchiveBuffer = null;
                        Class39_Sub4.odBlockOffset = 0;
                    } else {
                        if (Class39_Sub4.odBlockOffset != 512) {
                            break;
                        }
                        Class39_Sub4.odBlockOffset = 0;
                    }
                }
            }
            return true;
        } catch (IOException ioexception) {
            try {
                Deque.odSocket.stop();
            } catch (Exception exception) {
                /* empty */
            }
            Deque.odSocket = null;
            Class65.odErrors++;
            return false;
        }
    }

    public static void method344(int i) {
        aClass39_Sub5_Sub10_Sub4_629 = null;
        aClass3_622 = null;
        aClass3_617 = null;
        aClass3_616 = null;
        aClass3_623 = null;
        aClass3_627 = null;
        aClass3_625 = null;
        anIntArray621 = null;
        aClass3_624 = null;
        aClass3_620 = null;
        aClass3_619 = null;
    }

    public void focusGained(FocusEvent focusevent) {
        /* empty */
    }

    public static int method345(byte i) {
        return 5;
    }

    public static void method346(byte i, int i_20_, Class39_Sub5_Sub4_Sub4 class39_sub5_sub4_sub4) {
        Class4.method104(i_20_, class39_sub5_sub4_sub4.anInt2275, i + 99,
                class39_sub5_sub4_sub4.anInt2301);
        if (i != -11) {
            method343(122);
        }
    }

    public synchronized void keyReleased(KeyEvent keyevent) {
        keyevent = ModHook.keyReleased(keyevent);
        if (Npc.aClass35_2499 != null) {
            Class39_Sub5_Sub7.anInt1779 = 0;
            int i = keyevent.getKeyCode();
            if (i >= 0
                    && Player.anIntArray2532.length > i) {
                i = Player.anIntArray2532[i] & ~0x80;
            } else {
                i = -1;
            }
            if (BufferedFile.anInt341 >= 0 && i >= 0) {
                Class39_Sub5_Sub11.anIntArray1847[BufferedFile.anInt341] = i ^ 0xffffffff;
                BufferedFile.anInt341 = BufferedFile.anInt341 + 1 & 0x7f;
                if (BufferedFile.anInt341 == Class46.anInt879) {
                    BufferedFile.anInt341 = -1;
                }
            }
        }
        keyevent.consume();
    }

    public synchronized void focusLost(FocusEvent focusevent) {
        if (Npc.aClass35_2499 != null) {
            BufferedFile.anInt341 = -1;
        }
    }

    public void keyTyped(KeyEvent keyevent) {
        keyevent.consume();
    }

    public static void fetchArchive(boolean bool, CacheIO class6,
            FileLoader class9_sub1, int i) {
        ArchiveRequest class39_sub9 = new ArchiveRequest();
        class39_sub9.aClass9_Sub1_1406 = class9_sub1;
        class39_sub9.cache = class6;
        class39_sub9.hash = (long) i;
        class39_sub9.type = 1;
        synchronized (JSocket.archiveRequests) {
            JSocket.archiveRequests.offerLast(class39_sub9);
        }
        Class65.method1095(117);
    }

    static {
        aClass3_616 = aClass3_617;
        aClass3_619 = Class39_Sub5_Sub9.createJstring("Der Anmelde)2Server ist offline)3");
        anIntArray621 = new int[500];
        aClass3_625 = Class39_Sub5_Sub9.createJstring("Friends");
        aClass3_624 = aClass3_625;
        aLong626 = 0L;
        aClass3_620 = Class39_Sub5_Sub9.createJstring("Checking for updates )2 ");
        aClass3_623 = aClass3_620;
        aClass3_627 = Class39_Sub5_Sub9.createJstring("Loading game screen )2 ");
        aClass3_622 = aClass3_627;
    }
}
