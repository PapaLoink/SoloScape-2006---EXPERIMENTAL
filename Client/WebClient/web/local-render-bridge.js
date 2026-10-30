/* CheerpJ 4.3 JNI native methods: same-origin WebSocket-to-TCP relay.
 * Java byte[] converts to/from an Int8Array as documented by CheerpJ.
 */
(() => {
  'use strict';
  const connections = new Map();
  let nextId = 1;
  const MAX_QUEUED = 4 * 1024 * 1024;
  const MAX_PENDING_SEND = 1024 * 1024;

  function wake(state) {
    if (!state.waiter) return;
    const wakeRead = state.waiter;
    state.waiter = null;
    wakeRead();
  }

  function take(state) {
    if (state.queue.length) {
      const packet = state.queue.shift();
      state.queued -= packet.byteLength;
      return new Int8Array(packet);
    }
    return state.closed ? null : undefined;
  }

  async function nativeOpen() {
    const scheme = location.protocol === 'https:' ? 'wss:' : 'ws:';
    const socket = new WebSocket(scheme + '//' + location.host + '/game');
    socket.binaryType = 'arraybuffer';
    const id = nextId++;
    const state = { socket, queue: [], queued: 0, closed: false, waiter: null };
    connections.set(id, state);

    socket.addEventListener('message', (event) => {
      if (state.closed) return;
      if (!(event.data instanceof ArrayBuffer)) {
        socket.close(1003, 'Binary packets only');
        return;
      }
      state.queued += event.data.byteLength;
      if (state.queued > MAX_QUEUED) {
        console.error('[local-render] inbound game queue exceeded limit');
        socket.close(1009, 'Inbound queue full');
        return;
      }
      state.queue.push(event.data);
      wake(state);
    });
    socket.addEventListener('close', () => { state.closed = true; wake(state); });
    socket.addEventListener('error', () => { state.closed = true; wake(state); });

    try {
      await new Promise((resolve, reject) => {
        socket.addEventListener('open', resolve, { once: true });
        socket.addEventListener('error', () => reject(new Error('Cannot connect to LAN game gateway')), { once: true });
        socket.addEventListener('close', () => reject(new Error('Game gateway closed during connect')), { once: true });
      });
      return id;
    } catch (error) {
      connections.delete(id);
      socket.close();
      throw error;
    }
  }

  async function nativeRead(_lib, id, timeoutMs) {
    const state = connections.get(id);
    if (!state) return null;
    let result = take(state);
    if (result !== undefined) return result;
    let timer;
    await new Promise((resolve) => {
      state.waiter = resolve;
      if (timeoutMs > 0) timer = setTimeout(resolve, timeoutMs);
    });
    if (timer !== undefined) clearTimeout(timer);
    state.waiter = null;
    result = take(state);
    return result === undefined ? new Int8Array(0) : result;
  }

  function nativeAvailable(_lib, id) {
    const state = connections.get(id);
    return state ? state.queued : 0;
  }

  async function nativeWrite(_lib, id, bytes) {
    const state = connections.get(id);
    if (!state || state.closed || state.socket.readyState !== WebSocket.OPEN) {
      throw new Error('LAN WebSocket is disconnected');
    }
    while (state.socket.bufferedAmount > MAX_PENDING_SEND) {
      if (state.closed) throw new Error('LAN WebSocket disconnected during write');
      await new Promise((resolve) => setTimeout(resolve, 10));
    }
    state.socket.send(new Uint8Array(bytes));
  }

  function nativeClose(_lib, id) {
    const state = connections.get(id);
    if (!state) return;
    connections.delete(id);
    state.closed = true;
    state.socket.close();
    wake(state);
  }

  window.localRenderSocketNatives = {
    Java_unpackaged_LanWebSocketSocket_nativeOpen: nativeOpen,
    Java_unpackaged_LanWebSocketSocket_nativeRead: nativeRead,
    Java_unpackaged_LanWebSocketSocket_nativeAvailable: nativeAvailable,
    Java_unpackaged_LanWebSocketSocket_nativeWrite: nativeWrite,
    Java_unpackaged_LanWebSocketSocket_nativeClose: nativeClose
  };
})();
