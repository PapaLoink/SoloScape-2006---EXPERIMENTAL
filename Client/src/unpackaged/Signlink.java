package unpackaged;

/* Signlink - Decompiled by JODE
 * Visit http://jode.sourceforge.net/
 */

import jagex.io.FileOnDisk;
import java.applet.Applet;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.Socket;
import java.net.URL;

public class Signlink implements Runnable {

    public static Method aMethod398;
    public Resource iterator;
    public String cacheFilePath = null;
    public static String javaVendor;
    public String aString402;
    public String aString403 = null;
    public Interface3 anInterface3_404;
    public Thread thread;
    public boolean aBoolean406;
    public static String javaVersion;
    public String aString408;
    public Runnable_Impl1 aRunnable_Impl1_409;
    public int clientUid;
    public FileOnDisk tableArchive;
    public Resource tail;
    public FileOnDisk mainArchive;
    public static String userHome;
    public static int anInt415 = 3;
    public Applet anApplet416;
    public static Method aMethod417;
    public InetAddress hostAddress;
    public FileOnDisk[] cacheArchives;

    public Resource requestDataInputStream(URL url) {
        return requestResource(4, url, 0);
    }

    public void run() {
        for (;;) {
            Resource resource;
            synchronized (this) {
                for (;;) {
                    if (aBoolean406) {
                        return;
                    }
                    if (iterator != null) {
                        resource = iterator;
                        iterator = iterator.next;
                        if (iterator == null) {
                            tail = null;
                        }
                        break;
                    }
                    try {
                        this.wait();
                    } catch (InterruptedException interruptedexception) {
                        /* empty */
                    }
                }
            }
            try {
                int opcode = resource.opcode;
                if (opcode == 1) {
                    // Opt-in browser-local transport; desktop Java uses native TCP.
                    if ("lan-websocket".equals(System.getProperty("client.transport"))) {
                        resource.returnObject = new LanWebSocketSocket();
                    } else {
                        resource.returnObject = new Socket(hostAddress, resource.intParam);
                    }
                } else if (opcode != 2) {
                    if (opcode == 4) {
                        resource.returnObject = new DataInputStream(((URL) resource.objParam).openStream());
                    } else if (opcode == 9) {
                        Object[] objects = (Object[]) resource.objParam;
                        resource.returnObject = (((Class) objects[0]).getDeclaredMethod((String) objects[1], (Class[]) objects[2]));
                    } else if (opcode == 10) {
                        Object[] objects = (Object[]) resource.objParam;
                        resource.returnObject = ((Class) objects[0]).getDeclaredField((String) objects[1]);
                    } else {
                        throw new Exception();
                    }
                } else {
                    Thread t = new Thread((Runnable) resource.objParam);
                    t.setDaemon(true);
                    t.start();
                    t.setPriority(resource.intParam);
                    resource.returnObject = t;
                }
                resource.returnCode = 1;
            } catch (Exception exception) {
                resource.returnCode = 2;
            }
        }
    }

    public Interface3 method250(byte i) {
        return anInterface3_404;
    }

    public Runnable_Impl1 method251(int i) {
        return aRunnable_Impl1_409;
    }

    public Resource requestThread(Runnable runnable, int priority) {
        return requestResource(2, runnable, priority);
    }

    public void determineFilePaths(String gameName, int storeId) {
        String configuredCachePath = System.getProperty("client.cache");
        if (configuredCachePath != null && configuredCachePath.length() > 0) {
            File file = new File(configuredCachePath).getAbsoluteFile();
            if (!file.isDirectory()) {
                throw new RuntimeException("Configured client cache does not exist: " + file);
            }
            aString408 = aString403 = file.getPath() + "/";
            aString402 = cacheFilePath = file.getPath() + "/";
            return;
        }
        if (storeId < 32 || storeId > 34) {
            storeId = 32;
        }
        if (userHome == null) {
            userHome = "~/";
        }
        String[] paths = { "../Server/cache", "cache", "bin/cache" };
        for (int i_3_ = 0; paths.length > i_3_; i_3_++) {
            try {
                String filePath = paths[i_3_];
                if (filePath.length() > 0) {
                    File file = new File(filePath);
                    if (!file.exists()) {
                        continue;
                    }
                }
                File file = new File(filePath);
                if (file.exists() || file.mkdir()) {
                    if (gameName.length() > 0) {
                        file = new File(file, gameName);
                        if (!file.exists() && !file.mkdir()) {
                            continue;
                        }
                    }
                    aString408 = aString403 = file.getParent() + "/";
                    aString402 = cacheFilePath = file.getPath() + "/";
                    return;
                }
            } catch (Exception exception) {
                /* empty */
            }
        }
        throw new RuntimeException();
    }

    public Resource requestResource(int opcode, Object objParam, int intParam) {
        Resource resource = new Resource();
        resource.objParam = objParam;
        resource.opcode = opcode;
        resource.intParam = intParam;
        synchronized (this) {
            if (tail != null) {
                tail.next = resource;
                tail = resource;
            } else {
                tail = iterator = resource;
            }
            this.notify();
        }
        return resource;
    }

    public Resource requestSocket(int port) {
        return requestResource(1, null, port);
    }

    public Resource method256(int i, int i_9_) {
        if (i_9_ != 0) {
            requestResource(-76, null, -46);
        }
        return requestResource(3, null, i);
    }

    public void method257(byte i) {
        synchronized (this) {
            aBoolean406 = true;
            this.notifyAll();
        }
        try {
            thread.join();
        } catch (InterruptedException interruptedexception) {
            /* empty */
        }
        if (aRunnable_Impl1_409 != null) {
            aRunnable_Impl1_409.method6((byte) 72);
        }
        if (mainArchive != null) {
            try {
                mainArchive.destroy();
            } catch (IOException ioexception) {
                /* empty */
            }
        }
        if (tableArchive != null) {
            try {
                tableArchive.destroy();
            } catch (IOException ioexception) {
                /* empty */
            }
        }
        if (cacheArchives != null) {
            for (int i_10_ = 0; cacheArchives.length > i_10_; i_10_++) {
                if (cacheArchives[i_10_] != null) {
                    try {
                        cacheArchives[i_10_].destroy();
                    } catch (IOException ioexception) {
                        /* empty */
                    }
                }
            }
        }
    }

    public Resource requestDeclaredMethod(Class clazz, String methodName, Class[] params) {
        return requestResource(9, new Object[]{ clazz, methodName, params }, 0);
    }

    public void fetchUid( )
    {
        try {
            File file = new File(aString403 + "uid.dat");
            if (!file.exists() || file.length() < 4L) {
                DataOutputStream dataOutputStream = new DataOutputStream(new FileOutputStream(aString403 + "uid.dat"));
                dataOutputStream.writeInt((int) (Math.random() * 9.9999999E7));
                dataOutputStream.close();
            }
        } catch (Exception exception) {
            /* empty */
        }
        try {
            DataInputStream dataInputStream = new DataInputStream(new FileInputStream(aString403 + "uid.dat"));
            clientUid = dataInputStream.readInt() + 1;
            dataInputStream.close();
        } catch (Exception exception) {
            /* empty */
        }
    }

    public Resource method260(byte i) {
        return null;
    }

    public Resource requestDeclaredField(Class clazz, String fieldName) {
        return requestResource(10, new Object[]{ clazz, fieldName }, 0);
    }

    public Signlink(boolean bool, Applet applet, InetAddress inetaddress, int i, String string, int i_11_) throws IOException {
        aString402 = null;
        iterator = null;
        tail = null;
        aString408 = null;
        aBoolean406 = false;
        mainArchive = null;
        anApplet416 = null;
        clientUid = 0;
        tableArchive = null;
        javaVendor = "Unknown";
        javaVersion = "1.1";
        anApplet416 = applet;
        hostAddress = inetaddress;
        try {
            javaVendor = System.getProperty("java.vendor");
            javaVersion = System.getProperty("java.version");
            userHome = System.getProperty("user.home");
            if (userHome != null) {
                userHome += "/";
            }
        } catch (Exception exception) {
            /* empty */
        }
        try {
            if (applet == null) {
                aMethod398 = (Class.forName("java.awt.Component").getDeclaredMethod("setFocusTraversalKeysEnabled", new Class[]{Boolean.TYPE}));
            } else {
                aMethod398 = (applet.getClass().getMethod("setFocusTraversalKeysEnabled", new Class[]{Boolean.TYPE}));
            }
        } catch (Exception exception) {
            /* empty */
        }
        try {
            if (applet == null) {
                aMethod417 = (Class.forName("java.awt.Container").getDeclaredMethod("setFocusCycleRoot", new Class[]{Boolean.TYPE}));
            } else {
                aMethod417 = applet.getClass().getMethod("setFocusCycleRoot", (new Class[]{Boolean.TYPE}));
            }
        } catch (Exception exception) {
            /* empty */
        }
        if (bool) {
            determineFilePaths(string, i);
            mainArchive = new FileOnDisk(new File(cacheFilePath + "main_file_cache.dat2"), "rw", 52428800L);
            cacheArchives = new FileOnDisk[i_11_];
            for (int i_12_ = 0; i_11_ > i_12_; i_12_++) {
                cacheArchives[i_12_] = new FileOnDisk(new File(cacheFilePath + "main_file_cache.idx" + i_12_), "rw", 1048576L);
            }
            tableArchive = new FileOnDisk(new File(cacheFilePath + "main_file_cache.idx255"), "rw", 1048576L);
            fetchUid();
        }
        aBoolean406 = false;
        thread = new Thread(this);
        thread.setPriority(10);
        thread.setDaemon(true);
        thread.start();
    }
}
