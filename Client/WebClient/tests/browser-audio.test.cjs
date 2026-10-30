'use strict';

// Run from repo root: node Client/WebClient/tests/browser-audio.test.cjs
const assert = require('node:assert/strict');
const fs = require('node:fs');
const path = require('node:path');
const vm = require('node:vm');

const app = fs.readFileSync(path.join(__dirname, '../web/app.js'), 'utf8');

async function main() {
  const handlers = {};
  const wsEvents = {};
  const outputSources = [];
  let socket;
  let ctx;
  const canvas = {
    getBoundingClientRect: () => ({ left: 0, top: 0, width: 765, height: 503 }),
    getContext: () => ({ fillRect() {}, drawImage() {} }),
    addEventListener(name, fn) { handlers['canvas:' + name] = fn; }
  };
  const status = { classList: { toggle() {} }, textContent: '' };
  const sound = { textContent: '', addEventListener(name, fn) { handlers['sound:' + name] = fn; } };
  const keyboard = { addEventListener() {} };
  const softInput = { addEventListener() {} };
  const elements = { game: canvas, status, sound, keyboard, softInput };
  class MockAudioContext {
    constructor() { ctx = this; this.state = 'suspended'; this.currentTime = 0; this.destination = {}; }
    async resume() { this.state = 'running'; }
    async suspend() { this.state = 'suspended'; }
    createBuffer(channels, count, rate) {
      const samples = Array.from({ length: channels }, () => new Float32Array(count));
      return { duration: count / rate, getChannelData: index => samples[index] };
    }
    createBufferSource() {
      const source = { connect() {}, start(time) { this.when = time; outputSources.push(this); } };
      return source;
    }
  }
  class MockWebSocket {
    static OPEN = 1;
    constructor() { socket = this; this.readyState = MockWebSocket.OPEN; this.sent = []; }
    addEventListener(name, fn) { wsEvents[name] = fn; }
    send(bytes) { this.sent.push([...bytes]); }
  }
  vm.runInNewContext(app, {
    // Share binary constructors with the fake websocket input: instanceof
    // ArrayBuffer otherwise fails across Node vm realms, unlike the browser.
    ArrayBuffer, Uint8Array, DataView, Float32Array,
    window: { AudioContext: MockAudioContext, addEventListener() {} },
    document: { getElementById: id => elements[id], activeElement: null },
    WebSocket: MockWebSocket,
    location: { protocol: 'http:', host: 'localhost:8081' },
    performance: { now: () => 1000 },
    navigator: {},
    setTimeout() {},
    addEventListener() {},
    console
  }, { filename: 'web/app.js' });

  wsEvents.open();
  assert.equal(socket.sent.length, 0, 'audio must be opt-in');
  assert.equal(typeof handlers['sound:click'], 'function', 'sound button exists');
  await handlers['sound:click']();
  assert.equal(ctx.state, 'running', 'tap unlocks Web Audio');
  assert.deepEqual(socket.sent.at(-1), [8, 1], 'browser subscribes to sound');
  assert.equal(sound.textContent, 'Mute sound');

  // Audio 'A' packet: stream 1 SFX, stereo, 22050 Hz, 2 frames, signed LE 16-bit.
  const soundPacket = new Uint8Array([
    65, 1, 2, 0x56, 0x22,
    0xff, 0x7f, 0x00, 0x80,
    0x00, 0x40, 0x00, 0x00
  ]);
  wsEvents.message({ data: soundPacket.buffer });
  assert.equal(outputSources.length, 1, 'received PCM reaches audio output');
  assert.equal(outputSources[0].buffer.getChannelData(0)[0] > 0.99, true);
  assert.equal(outputSources[0].buffer.getChannelData(1)[0], -1);
  assert.ok(outputSources[0].when > ctx.currentTime, 'audio scheduled ahead to prevent crackle');

  const musicPacket = new Uint8Array([
    65, 2, 1, 0x56, 0x22,
    0x00, 0x40, 0x00, 0x00
  ]);
  wsEvents.message({ data: musicPacket.buffer });
  assert.equal(outputSources.length, 2, 'MIDI PCM stream also plays');

  await handlers['sound:click']();
  assert.deepEqual(socket.sent.at(-1), [8, 0], 'mute unsubscribes from audio');
  assert.equal(ctx.state, 'suspended');
  wsEvents.message({ data: soundPacket.buffer });
  assert.equal(outputSources.length, 2, 'muted browser drops audio');

  wsEvents.open();
  assert.notDeepEqual(socket.sent.at(-1), [8, 1], 'muted browser does not resubscribe');

  await handlers['sound:click']();
  wsEvents.open();
  assert.deepEqual(socket.sent.at(-1), [8, 1], 'reconnect resubscribes when enabled');
  console.log('SoloScape browser audio transport checks passed.');
}

main().catch(error => { console.error(error); process.exitCode = 1; });
