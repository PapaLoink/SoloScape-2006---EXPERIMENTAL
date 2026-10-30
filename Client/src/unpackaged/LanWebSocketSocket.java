package unpackaged;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.Arrays;

/** CheerpJ-only Socket adapter: same-origin WebSocket to the LAN /game TCP relay. */
public final class LanWebSocketSocket extends Socket {
    private final int handle;
    private volatile boolean closed;
    private volatile int soTimeout;

    private final InputStream input = new InputStream() {
        private byte[] chunk;
        private int position;

        @Override
        public synchronized int read() throws IOException {
            byte[] one = new byte[1];
            int n = read(one, 0, 1);
            return n < 0 ? -1 : one[0] & 0xff;
        }

        @Override
        public synchronized int read(byte[] dst, int off, int len) throws IOException {
            if (dst == null) throw new NullPointerException("dst");
            if (off < 0 || len < 0 || off > dst.length - len) throw new IndexOutOfBoundsException();
            if (len == 0) return 0;
            if (closed) throw new SocketException("Socket is closed");
            while (chunk == null || position == chunk.length) {
                chunk = nativeRead(handle, soTimeout);
                position = 0;
                if (chunk == null) return -1; // WebSocket EOF
                if (chunk.length == 0) throw new SocketTimeoutException("LAN WebSocket read timed out");
            }
            int n = Math.min(len, chunk.length - position);
            System.arraycopy(chunk, position, dst, off, n);
            position += n;
            return n;
        }

        @Override
        public synchronized int available() throws IOException {
            if (closed) return 0;
            return (chunk == null ? 0 : chunk.length - position) + nativeAvailable(handle);
        }

        @Override
        public void close() throws IOException { LanWebSocketSocket.this.close(); }
    };

    private final OutputStream output = new OutputStream() {
        @Override
        public void write(int b) throws IOException {
            write(new byte[]{(byte) b}, 0, 1);
        }

        @Override
        public void write(byte[] bytes, int off, int len) throws IOException {
            if (bytes == null) throw new NullPointerException("bytes");
            if (off < 0 || len < 0 || off > bytes.length - len) throw new IndexOutOfBoundsException();
            if (closed) throw new SocketException("Socket is closed");
            if (len > 0) nativeWrite(handle, Arrays.copyOfRange(bytes, off, off + len));
        }

        @Override
        public void close() throws IOException { LanWebSocketSocket.this.close(); }
    };

    public LanWebSocketSocket() throws IOException {
        super();
        try {
            handle = nativeOpen();
        } catch (UnsatisfiedLinkError missingBridge) {
            throw new IOException("LAN socket needs the CheerpJ browser native bridge", missingBridge);
        }
    }

    @Override
    public InputStream getInputStream() throws IOException {
        if (closed) throw new SocketException("Socket is closed");
        return input;
    }

    @Override
    public OutputStream getOutputStream() throws IOException {
        if (closed) throw new SocketException("Socket is closed");
        return output;
    }

    @Override
    public void setSoTimeout(int timeout) throws SocketException {
        if (timeout < 0) throw new IllegalArgumentException("negative timeout");
        if (closed) throw new SocketException("Socket is closed");
        soTimeout = timeout;
    }

    @Override
    public int getSoTimeout() { return soTimeout; }

    @Override
    public void setTcpNoDelay(boolean on) throws SocketException {
        if (closed) throw new SocketException("Socket is closed");
        // Ordering and retransmission are provided by the underlying WebSocket.
    }

    @Override
    public synchronized void close() throws IOException {
        if (!closed) {
            closed = true;
            nativeClose(handle);
        }
    }

    @Override
    public boolean isClosed() { return closed; }

    private static native int nativeOpen();
    private static native byte[] nativeRead(int id, int timeoutMs);
    private static native int nativeAvailable(int id);
    private static native void nativeWrite(int id, byte[] bytes);
    private static native void nativeClose(int id);
}
