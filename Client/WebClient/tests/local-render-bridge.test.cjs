'use strict';
const assert = require('node:assert/strict');
const fs = require('node:fs');
const vm = require('node:vm');
const test = require('node:test');
const path = require('node:path');

class FakeWebSocket {
  static OPEN = 1;
  static sockets = [];
  constructor(url) {
    this.url = url;
    this.readyState = 0;
    this.bufferedAmount = 0;
    this.listeners = new Map();
    this.sent = [];
    FakeWebSocket.sockets.push(this);
    queueMicrotask(() => this.fire('open'));
  }
  addEventListener(type, listener) {
    const list = this.listeners.get(type) || [];
    list.push(listener);
    this.listeners.set(type, list);
  }
  fire(type, data) {
    if (type === 'open') this.readyState = FakeWebSocket.OPEN;
    if (type === 'close') this.readyState = 3;
    for (const fn of this.listeners.get(type) || []) fn(data || {});
  }
  send(bytes) { this.sent.push(new Uint8Array(bytes)); }
  close() { this.fire('close'); }
}

const context = { window: {}, location: { protocol: 'http:', host: '192.168.0.5:8082' },
  WebSocket: FakeWebSocket, console, setTimeout, clearTimeout, Uint8Array, Int8Array, ArrayBuffer };
vm.createContext(context);
vm.runInContext(fs.readFileSync(path.join(__dirname, '../web/local-render-bridge.js'), 'utf8'), context);
const { Java_unpackaged_LanWebSocketSocket_nativeOpen: open,
  Java_unpackaged_LanWebSocketSocket_nativeRead: read,
  Java_unpackaged_LanWebSocketSocket_nativeAvailable: available,
  Java_unpackaged_LanWebSocketSocket_nativeWrite: write,
  Java_unpackaged_LanWebSocketSocket_nativeClose: close } = context.window.localRenderSocketNatives;

test('uses same-origin WebSocket and preserves signed byte[] values', async () => {
  const id = await open(null);
  const ws = FakeWebSocket.sockets.at(-1);
  assert.equal(ws.url, 'ws://192.168.0.5:8082/game');
  await write(null, id, new Int8Array([0, -1, 127, -128]));
  assert.deepEqual(Array.from(ws.sent[0]), [0, 255, 127, 128]);
  ws.fire('message', { data: new Uint8Array([65, 255, 0, 128]).buffer });
  assert.equal(available(null, id), 4);
  assert.deepEqual(Array.from(await read(null, id, 1000)), [65, -1, 0, -128]);
  assert.equal(available(null, id), 0);
  close(null, id);
  assert.equal(await read(null, id, 100), null);
});

test('pending read resolves for inbound data, timeout and close', async () => {
  const id = await open(null);
  const ws = FakeWebSocket.sockets.at(-1);
  let promise = read(null, id, 1000);
  queueMicrotask(() => ws.fire('message', { data: new Uint8Array([2, 3]).buffer }));
  assert.deepEqual(Array.from(await promise), [2, 3]);
  assert.equal((await read(null, id, 10)).length, 0);
  promise = read(null, id, 1000);
  ws.close();
  assert.equal(await promise, null);
  close(null, id);
});

test('multiple Java sockets do not share packet queues', async () => {
  const a = await open(null);
  const sockA = FakeWebSocket.sockets.at(-1);
  const b = await open(null);
  const sockB = FakeWebSocket.sockets.at(-1);
  sockB.fire('message', { data: new Uint8Array([7]).buffer });
  assert.equal(available(null, a), 0);
  assert.equal(available(null, b), 1);
  assert.deepEqual(Array.from(await read(null, b, 0)), [7]);
  close(null, a);
  close(null, b);
  assert.equal(sockA.readyState, 3);
});
